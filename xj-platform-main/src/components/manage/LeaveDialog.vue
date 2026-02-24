<template>
  <el-dialog
    title="审批处理"
    :visible.sync="dialogVisible"
    :close-on-click-modal="true"
    width="600px"
    @close="handleClose"
  >
    <el-form :model="form" ref="form" label-width="100px">
      <el-form-item label="请假信息">
        <el-card shadow="never" class="info-card">
          <div class="info-item">
            <span class="label">申请人:</span>
            <span class="value">{{ getUserName(leave && leave.userId) }}</span>
          </div>
          <div class="info-item">
            <span class="label">请假类型:</span>
            <span class="value">{{
              getLeaveTypeName(leave && leave.leaveType)
            }}</span>
          </div>
          <div class="info-item">
            <span class="label">时间:</span>
            <span class="value">
              {{ leave && leave.startDate }} 至 {{ leave && leave.endDate }}
            </span>
          </div>
          <div class="info-item">
            <span class="label">原因:</span>
            <span class="value">{{ leave && leave.reason }}</span>
          </div>
        </el-card>
      </el-form-item>

      <el-form-item label="审批结果" prop="approved" required>
        <el-radio-group v-model="form.approved">
          <el-radio :label="true">同意</el-radio>
          <el-radio :label="false">拒绝</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item
        label="下一审批人"
        v-if="form.approved && nextApproverOptions.length > 0"
      >
        <el-select
          v-model="form.nextApprover"
          placeholder="请选择下一审批人"
          filterable
          clearable
          style="width: 100%"
        >
          <el-option
            v-for="opt in nextApproverOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
        <div class="sub-label" v-if="task && task.name === '一级审批'">
          仅显示分管领导
        </div>
      </el-form-item>

      <el-form-item label="审批意见" prop="comment" required>
        <el-input
          type="textarea"
          v-model="form.comment"
          :rows="4"
          placeholder="请输入审批意见"
        ></el-input>
      </el-form-item>

      <el-form-item label="附件" v-if="leave && leave.id">
        <Attachment
          :leave-id="leave.id"
          :show-uploaded-files="true"
          :allow-delete="false"
        />
      </el-form-item>
    </el-form>

    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取 消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit"
        >确 定</el-button
      >
    </span>
  </el-dialog>
</template>

<script>
import Attachment from "@/components/Attachment.vue";

export default {
  name: "ApprovalDialog",
  components: {
    Attachment,
  },
  props: {
    // 控制显示隐藏
    visible: {
      type: Boolean,
      default: false,
    },
    // 当前正在处理的任务对象
    task: {
      type: Object,
      default: () => ({}),
    },
    // 当前任务关联的请假详情
    leave: {
      type: Object,
      default: () => ({}),
    },
    // 所有用户列表，用于计算下一审批人和显示名字
    users: {
      type: Array,
      default: () => [],
    },
    // 提交按钮的 loading 状态
    loading: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      form: {
        approved: true,
        nextApprover: "",
        comment: "",
      },
    };
  },
  computed: {
    // 双向绑定处理 visible，避免直接修改 prop
    dialogVisible: {
      get() {
        return this.visible;
      },
      set(val) {
        this.$emit("update:visible", val);
      },
    },
    // 动态计算下一审批人选项
    nextApproverOptions() {
      if (!this.task || !this.users || this.users.length === 0) return [];

      // 辅助函数：判断是否为分管领导/副总
      const isViceLeader = (u) => {
        const jt = u.job_title || u.jobTitle || "";
        const nn = u.nickname || u.name || "";
        const un = u.username || "";
        return (
          jt.includes("副") ||
          jt.includes("分管") ||
          jt.includes("副总") ||
          nn.includes("副") ||
          nn.includes("分管") ||
          un.includes("vice_leader")
        );
      };

      // 逻辑：如果是“一级审批”，只返回分管领导
      if (this.task.name === "一级审批") {
        return this.users
          .filter((u) => isViceLeader(u))
          .map((u) => ({
            value: u.id,
            label: u.nickname || u.name || u.username || `ID:${u.id}`,
          }));
      }

      return [];
    },
  },
  watch: {
    // 每次打开弹窗时，重置表单
    visible(val) {
      if (val) {
        this.resetForm();
      }
    },
  },
  methods: {
    handleClose() {
      this.dialogVisible = false;
    },
    resetForm() {
      this.form = {
        approved: true,
        nextApprover: "",
        comment: "",
      };
      if (this.$refs.form) {
        this.$refs.form.clearValidate();
      }
    },
    handleSubmit() {
      // 1. 校验必填项
      if (!this.form.comment) {
        this.$message.warning("请输入审批意见");
        return;
      }

      // 2. 校验特殊逻辑：一级审批同意时必须选下一人
      const needNextApprover =
        this.form.approved &&
        this.task &&
        this.task.name === "一级审批" &&
        this.nextApproverOptions.length > 0;

      if (needNextApprover && !this.form.nextApprover) {
        this.$message.warning("请选择下一审批人");
        return;
      }

      // 3. 将数据抛给父组件
      this.$emit("submit", {
        approved: this.form.approved,
        nextApprover: this.form.nextApprover,
        comment: this.form.comment,
      });
    },
    // 工具方法：获取用户名
    getUserName(userId) {
      if (!userId && userId !== 0) return "";
      const user = this.users.find((u) => u.id === userId);
      return user
        ? user.nickname || user.name || user.username
        : `ID:${userId}`;
    },
    // 工具方法：获取请假类型名称
    getLeaveTypeName(type) {
      const types = {
        annual: "年假",
        sick: "病假",
        personal: "事假",
        marriage: "婚假",
        maternity: "产假",
        paternity: "陪产假",
      };
      return types[type] || type;
    },
  },
};
</script>

<style scoped>
.info-card {
  background-color: #f5f7fa;
  border: 1px solid #ebeef5;
}
.info-item {
  margin-bottom: 8px;
  display: flex;
}
.info-item:last-child {
  margin-bottom: 0;
}
.info-item .label {
  font-weight: bold;
  width: 80px;
  color: #606266;
}
.info-item .value {
  flex: 1;
  color: #303133;
}
.sub-label {
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
  margin-top: 4px;
}
</style>
