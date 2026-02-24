<template>
  <div class="initiate-task-container">
    <el-card title="发起检查任务">
      <el-form
        ref="taskForm"
        :model="taskForm"
        label-width="120px"
        :rules="rules"
      >
        <!-- 原有表单字段 -->
        <el-form-item label="任务标题" prop="taskTitle">
          <el-input
            v-model="taskForm.taskTitle"
            placeholder="请输入检查任务标题"
          ></el-input>
        </el-form-item>
        <el-form-item label="任务描述" prop="taskDesc">
          <el-input
            type="textarea"
            v-model="taskForm.taskDesc"
            placeholder="请输入检查任务描述"
          ></el-input>
        </el-form-item>
        <el-form-item label="选择检查员" prop="inspectorIds">
          <el-select
            v-model="taskForm.inspectorIds"
            multiple
            placeholder="请选择检查员"
            style="width: 100%"
          >
            <el-option
              v-for="inspector in inspectorList"
              :key="inspector.id"
              :label="inspector.nickname"
              :value="inspector.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker
            v-model="taskForm.deadline"
            type="datetime"
            placeholder="选择截止日期"
            style="width: 100%"
            format="yyyy-MM-dd HH:mm:ss"
            value-format="yyyy-MM-dd HH:mm:ss"
            :picker-options="datePickerOptions"
          ></el-date-picker>
        </el-form-item>

        <el-form-item label="选择模板" prop="templateId">
          <el-select
            v-model="taskForm.templateId"
            placeholder="请选择系统模板,若无模板可选择，请到模板管理界面上传模板。"
            style="width: 100%"
          >
            <el-option
              v-for="template in templateList"
              :key="template.id"
              :label="template.templateName"
              :value="template.id"
            ></el-option>
          </el-select>
          <el-button
            v-if="taskForm.templateId"
            type="text"
            @click="previewTemplate"
            style="margin-top: 10px"
          >
            点击可下载模板
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitTask">发起任务</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { inspectionApi, templateApi } from "@/api/inspection";
import { userApi } from "@/api/usermag";

export default {
  name: "InitiateTask",
  data() {
    return {
      taskForm: {
        taskTitle: "",
        taskDesc: "",
        deadline: null,
        inspectorIds: [],
        templateId: "", // 添加模板ID字段
      },
      datePickerOptions: {
        disabledDate(time) {
          // 禁用今天及以前的日期
          // time.getTime() 是当前遍历的日期时间戳
          // Date.now() 是当前时间戳
          // 24 * 3600 * 1000 是一天的毫秒数
          // 这里设置只能选择今天之后（明天开始）的日期
          return time.getTime() < Date.now() - 24 * 3600 * 1000;
        },
      },
      selectedTemplateId: "",
      inspectorList: [],
      // 新增模板相关数据
      templateList: [],
      rules: {
        taskTitle: [
          { required: true, message: "请输入任务标题", trigger: "blur" },
        ],
        inspectorIds: [
          {
            required: true,
            message: "请选择检查员",
            trigger: "change",
            type: "array",
            min: 1,
          },
        ],
        deadline: [
          { required: true, message: "请选择截止日期", trigger: "change" },
        ],
        templateId: [
          {
            required: true,
            message: "请选择模板,模板为空时请到模板管理页面添加上传检查模板。",
            trigger: "change",
          },
        ],
      },
    };
  },
  props: ["login_user", "userRoles", "userPermissions"],
  created() {
    this.loadInspectors();
    this.loadTemplateList(); // 加载模板列表
  },
  methods: {
    // 加载检查员列表
    async loadInspectors() {
      try {
        const res = await userApi.getUsersByRole([
          "SUPERVISION",
          "SUPERVISION_MANAGER",
        ]);
        this.inspectorList = res.data;
      } catch (err) {
        this.$message.error("加载检查员列表失败：" + err.message);
      }
    },

    // 加载模板列表
    async loadTemplateList() {
      try {
        const res = await templateApi.getTemplateList();
        this.templateList = res.data;
      } catch (err) {
        this.$message.error("加载模板列表失败：" + err.message);
      }
    },

    // 预览/下载模板
    previewTemplate() {
      this.selectedTemplateId = this.taskForm.templateId;
      if (!this.selectedTemplateId) {
        this.$message.warning("请先选择模板");
        return;
      }
      const selectedTemplate = this.templateList.find(
        (item) => item.id === this.selectedTemplateId
      );
      this.$confirm(
        `确定要下载模板【${selectedTemplate.templateName}】吗？`,
        "提示",
        {
          type: "info",
        }
      ).then(() => {
        this.downloadTemplate(selectedTemplate);
      });
    },

    // 下载模板
    async downloadTemplate(template) {
      try {
        const res = await templateApi.downloadTemplate(template.id);
        const blob = new Blob([res.data]);
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = downloadUrl;
        a.download = template.templateName;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(downloadUrl);
        document.body.removeChild(a);
      } catch (err) {
        this.$message.error("模板下载失败：" + err.message);
      }
    },

    // 提交任务
    async submitTask() {
      this.$refs.taskForm.validate(async (valid) => {
        if (valid) {
          const formData = new FormData();
          // 基础任务信息
          // 分别添加各个字段，而不是整个对象
          formData.append("taskTitle", this.taskForm.taskTitle);
          formData.append("taskDesc", this.taskForm.taskDesc);
          formData.append("initiatorId", this.login_user.id);
          formData.append("initiatorName", this.login_user.nickname);
          formData.append("deadline", this.taskForm.deadline);
          formData.append("templateId", this.taskForm.templateId);

          // 检查员需要单独处理，因为是数组
          this.taskForm.inspectorIds.forEach((id) => {
            formData.append("inspectorIds", id);
          });
          try {
            const res = await inspectionApi.initiateTask(formData);
            if (res.code == 200) {
              this.$message.success("任务发起成功！任务ID：" + res.data);
              this.resetForm();
            } else {
              this.$message.error(res.msg || "任务发起失败");
            }
          } catch (err) {
            this.$message.error("任务发起失败：" + err.message);
          } finally {
            // 无论成功或失败都跳转到任务监控页
            this.$router.push("/inspection/task-monitor"); // 任务监控页的路由名称
          }
        }
      });
    },

    // 重置表单
    resetForm() {
      this.$refs.taskForm.resetFields();
      this.taskForm.inspectorIds = [];
      this.taskForm.templateId = "";
    },
  },
};
</script>

<style scoped>
.initiate-task-container {
  padding: 20px;
}
</style>
