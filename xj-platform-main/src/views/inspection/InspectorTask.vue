<template>
  <div class="inspector-task-container">
    <!-- 筛选栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm" class="search-form">
        <el-form-item label="任务状态">
          <el-select
            v-model="filterForm.status"
            placeholder="全部状态"
            clearable
          >
            <el-option label="待接收" value="PENDING"></el-option>
            <el-option label="已接收" value="RECEIVED"></el-option>
            <el-option label="已提交" value="SUBMIT"></el-option>
            <el-option label="已完成" value="COMPLETED"></el-option>
            <el-option label="重做中" value="REDO"></el-option>
            <el-option label="已逾期" value="OVERDUE"></el-option>
            <el-option label="已终止" value="CANCELLED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="任务标题">
          <el-input
            v-model="filterForm.taskTitle"
            placeholder="请输入任务标题"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchTasks">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 任务列表 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="taskList"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column
          prop="assignment.id"
          label="任务ID"
          width="100"
        ></el-table-column>

        <el-table-column label="任务标题" min-width="200">
          <template slot-scope="scope">
            <span v-if="scope.row.mainTask">
              {{ scope.row.mainTask.taskTitle || "未知任务" }}
            </span>
            <span v-else>未知任务</span>
          </template>
        </el-table-column>
        <el-table-column label="截止时间" width="180">
          <template slot-scope="scope">
            <span v-if="scope.row.mainTask && scope.row.mainTask.deadline">
              {{ new Date(scope.row.mainTask.deadline).toLocaleString() }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column label="任务状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="getStatusTagType(scope.row.assignment.taskStatus)">
              {{ getStatusText(scope.row.assignment.taskStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="任务文件" min-width="150">
          <template slot-scope="scope">
            <el-button
              type="primary"
              size="mini"
              @click="downloadTemplate(scope.row.mainTask)"
            >
              <span v-if="scope.row.mainTask">
                {{ getFileNameFromPath(scope.row.mainTask.inspectionFormUrl) }}
              </span>
              <span v-else>-</span>
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.assignment.taskStatus == 'PENDING'"
              type="primary"
              size="small"
              icon="el-icon-check"
              @click="receiveTask(scope.row)"
            >
              接收任务
            </el-button>
            <el-button
              v-if="
                scope.row.assignment.taskStatus == 'RECEIVED' ||
                scope.row.assignment.taskStatus == 'REDO'
              "
              type="success"
              size="small"
              icon="el-icon-upload"
              @click="openSubmitDialog(scope.row)"
            >
              提交结果
            </el-button>
            <el-button
              type="text"
              size="small"
              icon="el-icon-view"
              @click="viewTaskDetail(scope.row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
        <template slot="empty">
          <div style="text-align: center; padding: 20px">
            <el-icon name="empty"></el-icon>
            <p>暂无符合条件的任务数据</p>
          </div>
        </template>
      </el-table>

      <!-- 分页控件 -->
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.current"
        :page-sizes="[10, 20, 50]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        style="margin-top: 20px; text-align: right"
      ></el-pagination>
    </el-card>

    <!-- 提交结果弹窗 -->
    <el-dialog
      title="提交检查结果"
      :visible.sync="submitDialogVisible"
      width="600px"
      :close-on-click-modal="true"
    >
      <el-form
        ref="submitForm"
        :model="submitForm"
        label-width="100px"
        :rules="submitRules"
      >
        <el-form-item label="任务ID" prop="assignmentId">
          <el-input v-model="submitForm.assignmentId" disabled></el-input>
        </el-form-item>
        <el-form-item label="任务标题" prop="taskTitle">
          <el-input v-model="submitForm.taskTitle" disabled></el-input>
        </el-form-item>
        <el-form-item label="待上传检查文件" prop="filledFormFile">
          <el-upload
            ref="upload"
            action="#"
            :auto-upload="false"
            :file-list="submitForm.fileList"
            :on-change="handleFileChange"
            :limit="1"
            accept=".doc,.docx,.pdf,.xlsx,.xls"
            class="upload-demo"
          >
            <el-button size="small" type="primary">选择文件</el-button>
            <div slot="tip" class="el-upload__tip">
              支持doc/docx/pdf/xlsx/xls格式，大小不超过10MB
            </div>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="submitTaskResult"
          :loading="submitLoading"
        >
          提交
        </el-button>
      </div>
    </el-dialog>

    <!-- 任务详情弹窗 -->
    <el-dialog
      title="任务详情"
      :visible.sync="detailDialogVisible"
      :close-on-click-modal="true"
      width="800px"
      v-if="detailForm"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="主任务ID">
          {{ detailForm.mainTask.id }}
        </el-descriptions-item>
        <el-descriptions-item label="子任务ID">{{
          detailForm.assignment.id
        }}</el-descriptions-item>
        <el-descriptions-item label="任务标题">{{
          detailForm.mainTask.taskTitle
        }}</el-descriptions-item>
        <el-descriptions-item label="任务描述">
          {{ detailForm.mainTask.taskDesc || "无" }}
        </el-descriptions-item>
        <el-descriptions-item label="发起人员">
          <span v-if="detailForm.mainTask">
            {{
              (getUserNickname(detailForm.mainTask.initiatorId) !== "未知用户"
                ? getUserNickname(detailForm.mainTask.initiatorId)
                : detailForm.mainTask.initiatorName) || "未知发起人"
            }}
          </span>
          <span v-else>未知发起人</span></el-descriptions-item
        >
        <el-descriptions-item label="当前执行人员">
          <span v-if="detailForm.assignment">
            {{
              (getInspectorNickname(detailForm.assignment.inspectorId) !==
              "未知检查员"
                ? getInspectorNickname(detailForm.assignment.inspectorId)
                : detailForm.assignment.inspectorName) || "无"
            }}
          </span>
        </el-descriptions-item>

        <el-descriptions-item label="截止时间">
          {{
            detailForm.mainTask.deadline
              ? new Date(detailForm.mainTask.deadline).toLocaleString()
              : "-"
          }}
        </el-descriptions-item>
        <el-descriptions-item label="主任务状态">
          <el-tag :type="getStatusTagType(detailForm.mainTask.status)">
            {{ getMainStatusText(detailForm.mainTask.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="任务原始模板文件" span="2">
          <el-button
            v-if="detailForm.mainTask.inspectionFormUrl"
            type="text"
            icon="el-icon-download"
            @click="downloadTemplate(detailForm.mainTask)"
          >
            下载任务模板文件
          </el-button>
          <span v-else>无</span>
        </el-descriptions-item>
        <el-descriptions-item label="已提交文件" span="2">
          <el-button
            v-if="detailForm.assignment.filledFormUrl"
            type="text"
            icon="el-icon-download"
            @click="downloadFilledFile(detailForm.assignment.filledFormUrl)"
          >
            下载已提交文件{{
              getFileNameFromPath(detailForm.assignment.filledFormUrl)
            }}
          </el-button>
          <span v-else>未提交</span>
        </el-descriptions-item>
        <el-descriptions-item label="驳回原因" span="2">
          <span v-if="detailForm.assignment.rejectReason" class="text-danger">
            {{ detailForm.assignment.rejectReason }}
          </span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{
            detailForm.mainTask
              ? new Date(detailForm.mainTask.createTime).toLocaleString()
              : "-"
          }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{
            detailForm.assignment
              ? new Date(detailForm.assignment.updateTime).toLocaleString()
              : "-"
          }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { inspectionApi, templateApi } from "@/api/inspection";
import { userApi } from "@/api/usermag"; // 新增

export default {
  name: "InspectorTask",
  data() {
    return {
      allUsers: [], // 存储所有用户（含发起人和检查员的nickname）
      allInspectors: [], // 存储所有检查员（复用）
      // 加载状态
      loading: false,
      submitLoading: false,
      // 筛选表单
      filterForm: {
        status: "",
        taskTitle: "",
      },
      // 任务列表
      taskList: [],
      // 分页参数
      pagination: {
        current: 1,
        size: 10,
        total: 0,
      },
      // 选中的任务
      selectedTasks: [],
      // 提交弹窗相关
      submitDialogVisible: false,
      submitForm: {
        assignmentId: "",
        taskTitle: "",
        fileList: [],
        filledFormFile: null,
        taskStatus: "",
      },
      submitRules: {
        filledFormFile: [
          {
            required: true,
            message: "请选择要上传的检查文件",
            trigger: "change",
          },
        ],
      },
      // 详情弹窗相关
      detailDialogVisible: false,
      detailForm: null,
      // 当前登录检查员信息
      currentInspector: this.login_user,
    };
  },
  props: ["login_user", "userRoles", "userPermissions"],
  created() {
    this.fetchTasks();
    this.loadAllUsers(); // 新增：加载所有用户信息
  },
  methods: {
    // 新增：加载所有用户（含发起人和检查员）
    async loadAllUsers() {
      try {
        // 若有区分角色的接口，可按角色加载；若无则加载所有用户
        const res = await userApi.getUsersByRole([
          "ADMIN",
          "SUPERVISION",
          "SUPERVISION_MANAGER",
        ]);
        this.allUsers = res.data;
        console.log("加载的用户列表：", this.allUsers); // 新增日志
        // 也可单独加载检查员（和之前逻辑一致）
        const inspectorRes = await userApi.getUsersByRole([
          "SUPERVISION",
          "SUPERVISION_MANAGER",
        ]);
        this.allInspectors = inspectorRes.data;
      } catch (err) {
        console.error("加载用户信息失败：", err);
      }
    },
    getUserNickname(userId) {
      // 先判断userId是否存在，避免空值报错
      if (!userId) return "未知用户";
      const user = this.allUsers.find((item) => item.id === userId);
      // 有nickname返回nickname，无则返回"未知用户"
      return user?.nickname || "未知用户";
    },
    // 同步优化检查员昵称方法
    getInspectorNickname(inspectorId) {
      if (!inspectorId) return "";
      const inspector = this.allInspectors.find(
        (item) => item.id === inspectorId
      );
      return inspector?.nickname || "";
    },
    /**
     * 获取任务列表
     */
    async fetchTasks() {
      this.loading = true;
      try {
        const res = await inspectionApi.getTodoTasks(this.currentInspector.id);
        if (res.code == 200) {
          this.taskList = res.data;
          this.pagination.total = res.data.length;
          console.log("this.taskList" + this.taskList);
          console.log("this.pagination.total" + this.pagination.total);
        } else {
          this.$message.error("获取任务列表失败：" + res.msg);
        }
      } catch (err) {
        this.$message.error("获取任务列表失败：" + err.message);
      } finally {
        this.loading = false;
      }
    },
    getFileNameFromPath(filePath) {
      // 这里直接调用工具函数
      return inspectionApi.extractFileName(filePath);
    },
    // 下载模板
    async downloadTemplate(row) {
      try {
        if (row == null || row.inspectionFormUrl == null) {
          return;
        }
        const res = await templateApi.downloadTemplateWithPath(
          row.inspectionFormUrl
        );
        if (res.code == 200) {
          // 1. 获取预签名链接
          const downloadUrl = res.data;
          // 1. 通过 fetch 获取文件流（避免直接访问 MinIO 链接的签名问题）
          const response = await fetch(downloadUrl);
          console.log("response", response);
          if (!response.ok) {
            throw new Error("文件下载链接无效");
          }
          // 2. 转换为 Blob 流
          const blob = await response.blob();
          // 3. 手动创建下载链接，强制指定原始文件名
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement("a");
          a.href = url;
          a.download = this.getFileNameFromPath(row.inspectionFormUrl); // 文件名字
          a.click();
          // 4. 释放临时资源
          window.URL.revokeObjectURL(url);
        } else {
          this.$message.error(res.msg || "生成下载链接失败");
        }
      } catch (err) {
        this.$message.error("模板下载失败：" + err.message);
      }
    },
    /**
     * 重置筛选条件
     */
    resetFilter() {
      this.filterForm = {
        status: "",
        taskTitle: "",
      };
      this.fetchTasks();
    },

    /**
     * 分页大小变更
     */
    handleSizeChange(val) {
      this.pagination.size = val;
      this.fetchTasks();
    },

    /**
     * 当前页变更
     */
    handleCurrentChange(val) {
      this.pagination.current = val;
      this.fetchTasks();
    },

    /**
     * 选择任务变更
     */
    handleSelectionChange(val) {
      this.selectedTasks = val;
    },

    /**
     * 接收任务
     */
    async receiveTask(row) {
      try {
        await this.$confirm(
          "确定要接收该任务吗？接收后请及时完成并提交结果",
          "提示",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          }
        );
        const res = await inspectionApi.receiveTask(
          row.assignment.id,
          this.currentInspector.id
        );
        if (res.code == 200) {
          this.$message.success("任务接收成功");
          this.fetchTasks();
        } else {
          this.$message.error("任务接收失败：" + res.msg);
        }
      } catch (err) {
        if (err !== "cancel") {
          this.$message.info("已取消任务接收");
        }
      }
    },

    /**
     * 打开提交弹窗
     */
    openSubmitDialog(row) {
      this.submitForm = {
        assignmentId: row.assignment.id,
        taskTitle: row.mainTask.taskTitle,
        fileList: [],
        filledFormFile: null,
        taskStatus: row.assignment.taskStatus,
      };
      this.submitDialogVisible = true;
    },

    /**
     * 文件选择变更
     */
    handleFileChange(file, fileList) {
      // 限制只能选择一个文件
      if (fileList.length > 1) {
        fileList.splice(0, fileList.length - 1);
      }
      this.submitForm.fileList = fileList;
      this.submitForm.filledFormFile = file.raw;
    },

    /**
     * 提交任务结果
     */
    async submitTaskResult() {
      try {
        // 表单验证
        await this.$refs.submitForm.validate();

        this.submitLoading = true;
        // 构建FormData
        const formData = new FormData();
        formData.append("assignmentId", this.submitForm.assignmentId); //任务id
        formData.append("inspectorId", this.currentInspector.id); //当前用户
        formData.append("filledFormFile", this.submitForm.filledFormFile); //文件对象

        // 判断是普通提交还是重做提交
        let res;
        res = await inspectionApi.completeTask(formData); //普通提交 不用区分重提什么的，数据库有对应记录

        if (res.code == 200) {
          this.$message.success("任务提交成功，等待管理员审核");
          this.submitDialogVisible = false;
          this.fetchTasks();
        } else {
          this.$message.error("任务提交失败：" + res.msg);
        }
      } catch (err) {
        this.$message.error("任务提交失败：" + err.message);
      } finally {
        this.submitLoading = false;
      }
    },

    /**
     * 查看任务详情
     */
    viewTaskDetail(row) {
      this.detailForm = JSON.parse(JSON.stringify(row));
      console.log("查看任务详情：", row);
      this.detailDialogVisible = true;
    },
    // 下载已提交文件
    async downloadFilledFile(filledFormUrl) {
      try {
        if (filledFormUrl == null) {
          return;
        }
        const res = await templateApi.downloadTemplateWithPath(filledFormUrl);
        if (res.code == 200) {
          // 1. 获取预签名链接
          const downloadUrl = res.data;
          // 1. 通过 fetch 获取文件流（避免直接访问 MinIO 链接的签名问题）
          const response = await fetch(downloadUrl);
          console.log("response", response);
          if (!response.ok) {
            throw new Error("文件下载链接无效");
          }
          // 2. 转换为 Blob 流
          const blob = await response.blob();
          // 3. 手动创建下载链接，强制指定原始文件名
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement("a");
          a.href = url;
          a.download = this.getFileNameFromPath(filledFormUrl); // 文件名字
          a.click();
          // 4. 释放临时资源
          window.URL.revokeObjectURL(url);
        } else {
          this.$message.error(res.msg || "生成下载链接失败");
        }
      } catch (err) {
        this.$message.error("模板下载失败：" + err.message);
      }
    },

    /**
     * 获取状态标签类型PENDING//RECEIVED/SUBMIT/COMPLETED/REDO/OVERDUE/CANCELLED
     */
    getStatusTagType(status) {
      switch (status) {
        case "PENDING":
          return "info";
        case "RECEIVED":
          return "primary";
        case "COMPLETED":
          return "success";
        case "SUBMIT":
          return "success";
        case "REDO":
          return "warning";
        case "CANCELLED":
          return "danger";
        case "OVERDUE":
          return "danger";
        default:
          return "";
      }
    },

    /**
     * 获取状态文本
     */
    getStatusText(status) {
      switch (status) {
        case "PENDING":
          return "待接收";
        case "RECEIVED":
          return "已接收";
        case "SUBMIT":
          return "已提交";
        case "COMPLETED":
          return "已完成";
        case "REDO":
          return "重做中";
        case "OVERDUE":
          return "已逾期";
        case "CANCELLED":
          return "已终止";
        default:
          return status || "未知状态";
      }
    },
    /**
     * 获取状态文本
     */
    getMainStatusText(status) {
      switch (status) {
        case "IN_PROGRESS":
          return "进行中";
        case "COMPLETED":
          return "已完成";
        case "CANCELLED":
          return "已终止";
        case "OVERDUE":
          return "已逾期";
        default:
          return status || "未知状态";
      }
    },
  },
};
</script>

<style scoped>
.inspector-task-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.filter-card {
  margin-bottom: 20px;
}

.text-danger {
  color: #f56c6c;
}

.el-upload__tip {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}
</style>
