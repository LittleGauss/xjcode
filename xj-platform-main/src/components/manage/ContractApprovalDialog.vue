<template>
  <el-dialog
    title="合同审批处理"
    :visible.sync="dialogVisible"
    :close-on-click-modal="true"
    width="600px"
    @close="handleClose"
    append-to-body
  >
    <el-form :model="form" ref="form" label-width="100px">
      <el-form-item label="合同信息">
        <el-card shadow="never" class="info-card">
          <div class="info-item">
            <span class="label">合同名称:</span>
            <span class="value">{{ contract.contractName }}</span>
          </div>
          <div class="info-item">
            <span class="label">申请人:</span>
            <span class="value">{{ contract.applicantName }}</span>
          </div>
          <div class="info-item">
            <span class="label">当前状态:</span>
            <span class="value">{{ contract.status }}</span>
          </div>
        </el-card>
      </el-form-item>

      <template v-if="isModifyStage">
        <el-alert
          title="当前处于“待使用部门修改”环节，可在提交审批前更新合同信息"
          type="info"
          show-icon
          class="mb-3"
        />
        <el-form-item label="合同名称" required>
          <el-input
            v-model="editForm.contractName"
            placeholder="请输入合同名称"
          />
        </el-form-item>
        <el-form-item label="提交部门">
          <el-select
            v-model="editForm.department"
            filterable
            clearable
            placeholder="请选择提交部门"
            style="width: 100%"
          >
            <el-option
              v-for="d in departments"
              :key="d.id || d.name"
              :label="d.name"
              :value="d.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            type="textarea"
            :rows="3"
            v-model="editForm.reviewComments"
            placeholder="补充备注（可选）"
          />
        </el-form-item>
        <el-form-item label="补充附件">
          <input
            type="file"
            @change="onFileChange"
            class="block w-full text-sm text-gray-500 file:mr-2 file:px-3 file:py-1 file:rounded-md file:border-0 file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
          />
        </el-form-item>
      </template>

      <el-form-item label="审批结果" prop="approved" required>
        <el-radio-group v-model="form.approved">
          <el-radio :label="true">同意</el-radio>
          <el-radio :label="false">驳回</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="审批意见" prop="comment" :required="!form.approved">
        <el-input
          type="textarea"
          v-model="form.comment"
          :rows="4"
          :placeholder="
            form.approved ? '请输入审批意见（可选）' : '请输入驳回理由（必填）'
          "
        ></el-input>
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
export default {
  name: "ContractApprovalDialog",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    contract: {
      type: Object,
      default: () => ({}),
    },
    departments: {
      type: Array,
      default: () => [],
    },
    loading: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      form: {
        approved: true,
        comment: "",
      },
      editForm: {
        contractName: "",
        department: "",
        reviewComments: "",
      },
      uploadFile: null,
    };
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible;
      },
      set(val) {
        this.$emit("update:visible", val);
      },
    },
    isModifyStage() {
      const status = this.contract?.status || "";
      return status === "待使用部门修改";
    },
  },
  watch: {
    visible(val) {
      if (val) {
        // 每次打开重置表单，默认为同意
        this.form.approved = true;
        this.form.comment = "";
        this.editForm.contractName = this.contract?.contractName || "";
        this.editForm.department = this.contract?.department || "";
        this.editForm.reviewComments = this.contract?.reviewComments || "";
        this.uploadFile = null;
      }
    },
  },
  methods: {
    handleClose() {
      this.dialogVisible = false;
    },
    onFileChange(e) {
      const [file] = e.target.files || [];
      this.uploadFile = file || null;
    },
    handleSubmit() {
      if (this.isModifyStage && !this.editForm.contractName) {
        this.$message.warning("请填写合同名称");
        return;
      }
      // 驳回时必须填写意见
      if (!this.form.approved && !this.form.comment) {
        this.$message.warning("驳回操作必须填写理由");
        return;
      }

      this.$emit("submit", {
        approved: this.form.approved,
        comment: this.form.comment || (this.form.approved ? "同意" : ""),
        changes: this.isModifyStage
          ? {
              contractName: this.editForm.contractName,
              department: this.editForm.department,
              reviewComments: this.editForm.reviewComments,
            }
          : null,
        newFile: this.isModifyStage ? this.uploadFile : null,
      });
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
.info-item .label {
  font-weight: bold;
  width: 80px;
  color: #606266;
}
.info-item .value {
  flex: 1;
  color: #303133;
}
</style>
