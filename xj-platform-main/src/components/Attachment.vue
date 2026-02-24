<template>
  <div class="attachment-component">
    <el-upload
      ref="upload"
      class="upload-demo"
      :action="uploadUrl"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
      :file-list="fileList"
      :data="safeUploadData"
      multiple
      :limit="limit"
      :on-exceed="handleExceed"
      :with-credentials="true"
      :headers="uploadHeaders"
      name="file"
    >
      <el-button size="small" type="primary">点击上传</el-button>
      <div slot="tip" class="el-upload__tip">
        只能上传jpg/png/pdf/doc等文件，且不超过10MB
      </div>
    </el-upload>

    <div
      v-if="showUploadedFiles && attachments.length > 0"
      class="uploaded-files"
    >
      <h4>已上传附件：</h4>
      <ul>
        <li v-for="attachment in attachments" :key="attachment.id">
          <a :href="attachment.fileUrl" target="_blank">{{
            attachment.fileName
          }}</a>
          <span
            v-if="allowDelete"
            class="delete-btn"
            @click="deleteAttachment(attachment.id)"
            >删除</span
          >
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import { getAttachments, deleteAttachment } from "@/api/leave";

export default {
  name: "AttachmentUploader",
  props: {
    leaveId: {
      type: [Number, String],
      default: null,
    },
    operatorId: {
      type: [Number, String],
      default: null,
    },
    limit: {
      type: Number,
      default: 5,
    },
    showUploadedFiles: {
      type: Boolean,
      default: true,
    },
    allowDelete: {
      type: Boolean,
      default: false,
    },
    uploadData: {
      type: Object,
      default: () => ({}),
    },
  },
  data() {
    return {
      fileList: [],
      attachments: [],
      // 后端 AttachmentController: /api/attachments/leave
      uploadUrl: process.env.VUE_APP_BASE_API + "/api/attachments/leave",
    };
  },

  computed: {
    // 【新增这个计算属性】
    safeUploadData() {
      const data = {};

      // 1. 处理 uploadUserId (必须传)
      // 注意：从 props.uploadData 中获取
      if (this.uploadData && this.uploadData.uploadUserId) {
        data.uploadUserId = this.uploadData.uploadUserId;
      }

      // 2. 处理 leaveId (可选)
      // 关键点：只有当 this.leaveId 有值且不为 null 时，才放入对象
      if (
        this.leaveId !== null &&
        this.leaveId !== undefined &&
        this.leaveId !== ""
      ) {
        data.leaveId = this.leaveId;
      }

      return data;
    },
    uploadHeaders() {
      // 动态从本地读取 token 并放入 Authorization，兼容后端鉴权
      try {
        // 延迟加载，避免循环引用
        // eslint-disable-next-line global-require
        const { getToken } = require("@/utils/auth");
        const token = getToken && getToken();
        if (token) return { Authorization: token };
      } catch (e) {
        // ignore
      }
      return {};
    },
  },
  watch: {
    leaveId: {
      handler(newVal) {
        if (newVal) {
          this.loadAttachments();
        }
      },
      immediate: true,
    },
  },
  methods: {
    async loadAttachments() {
      if (!this.leaveId) return;

      try {
        const res = await getAttachments(this.leaveId);
        const list = res.data || [];
        // 拼接下载链接（由 LeaveProcessController 提供下载端点）
        this.attachments = list.map((it) => ({
          ...it,
          fileUrl: `/api/attachment/${it.id}/download`,
        }));
      } catch (err) {
        console.error("加载附件失败:", err);
        this.$message.error("加载附件失败");
      }
    },

    handleSuccess(response, file, fileList) {
      this.$message.success("上传成功");
      console.log("上传成功:", file.name, fileList);
      this.$emit("upload-success", response);
      // 修复：使用正确的 prop 名称 `leaveId`（之前误用 this.Id）
      if (this.leaveId) {
        this.loadAttachments();
      } else {
        // 如果没有传入 leaveId，尝试触发父组件刷新或给出提示
        console.warn(
          "Attachment: missing leaveId, attachments list not refreshed."
        );
      }
    },

    handleError(err, file, fileList) {
      console.log("上传失败:", file.name, fileList);
      this.$message.error("上传失败");
      this.$emit("upload-error", err);
    },

    beforeUpload(file) {
      // 1. 检查文件大小
      const isLt10M = file.size / 1024 / 1024 < 10;
      if (!isLt10M) {
        this.$message.error("上传文件大小不能超过 10MB!");
        return false;
      }

      // 2. 检查必需参数：uploadUserId (对应父组件传入的 uploadData.uploadUserId)
      // 由于 uploadData 是 props，我们直接从 props.uploadData 读取 uploadUserId
      const uploadUserId = this.uploadData?.uploadUserId;
      if (!uploadUserId || isNaN(Number(uploadUserId))) {
        this.$message.error("缺少操作人ID (uploadUserId)，无法上传附件。");
        console.error("上传失败: 缺少 uploadUserId 参数");
        return false;
      }

      return isLt10M;
    },

    handleExceed(files, fileList) {
      console.log("上传成功:", files.name, fileList);
      this.$message.warning(`当前限制上传 ${this.limit} 个文件`);
    },
    async deleteAttachment(id) {
      try {
        await this.$confirm("确认删除该附件?", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });

        // 优先使用传入的 operatorId，其次尝试从 store，如果都没有则报错提示
        const operatorId = this.operatorId || this.$store?.state?.user?.id;
        if (!operatorId) {
          this.$message.error("缺少操作人ID，无法删除附件");
          return;
        }
        await deleteAttachment(id, { operatorId });
        this.$message.success("删除成功");
        this.loadAttachments();
        this.$emit("delete-success");
      } catch (err) {
        if (err !== "cancel") {
          this.$message.error("删除失败");
        }
      }
    },
    clearFiles() {
      this.fileList = [];
      this.attachments = [];
      if (this.$refs.upload) {
        this.$refs.upload.clearFiles();
      }
    },
  },
};
</script>

<style scoped>
.uploaded-files {
  margin-top: 20px;
}

.uploaded-files h4 {
  margin-bottom: 10px;
}

.uploaded-files ul {
  list-style: none;
  padding: 0;
}

.uploaded-files li {
  padding: 5px 0;
}

.delete-btn {
  color: #f56c6c;
  cursor: pointer;
  margin-left: 10px;
}
</style>
