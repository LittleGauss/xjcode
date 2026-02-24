<template>
  <div class="template-manager-container">
    <el-card title="模板文件管理">
      <!-- 上传区域 -->
      <el-upload
        class="upload-demo"
        action="#"
        :auto-upload="false"
        :on-change="handleFileChange"
        :file-list="fileList"
        multiple
        accept=".doc,.docx,.pdf,.xlsx,.xls"
        :key="uploadKey"
        ref="uploadRef"
      >
        <el-button type="primary" size="mini">选择模板文件</el-button>
      </el-upload>
      <el-button
        type="success"
        size="mini"
        @click="submitUpload"
        style="margin-left: 10px"
      >
        文件选择好后，点击此处可以批量上传。
      </el-button>
      <el-divider content-position="left">已上传模板列表</el-divider>

      <!-- 模板列表 -->
      <el-table :data="templateList" border stripe v-loading="loading">
        <el-table-column prop="id" label="模板ID" width="100"></el-table-column>
        <el-table-column
          prop="templateName"
          label="模板名称"
          min-width="200"
        ></el-table-column>
        <el-table-column
          prop="fileType"
          label="文件类型"
          width="100"
        ></el-table-column>
        <el-table-column prop="fileSize" label="文件大小(KB)" width="120">
          <template slot-scope="scope">
            {{ (scope.row.fileSize / 1024).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="uploadTime" label="上传时间" width="200">
          <template slot-scope="scope">
            {{
              scope.row.uploadTime
                ? new Date(scope.row.uploadTime).toLocaleString()
                : "-"
            }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button
              type="primary"
              size="mini"
              @click="downloadTemplate(scope.row)"
            >
              下载
            </el-button>
            <el-button
              type="danger"
              size="mini"
              @click="deleteTemplate(scope.row)"
              style="margin-left: 10px"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { templateApi } from "@/api/inspection";

export default {
  name: "TemplateManager",
  data() {
    return {
      fileList: [],
      templateList: [],
      loading: false,
      currentFiles: [],
      uploadKey: 0, // 新增，用于强制重新渲染上传组件
    };
  },
  props: ["login_user", "userRoles", "userPermissions"],
  created() {
    this.getTemplateList();
  },
  methods: {
    // 选择文件
    handleFileChange(file, fileList) {
      // 去重处理，根据文件名去重
      const uniqueFiles = [];
      const fileNames = new Set();

      fileList.forEach((item) => {
        if (!fileNames.has(item.name)) {
          fileNames.add(item.name);
          uniqueFiles.push(item);
        }
      });

      this.fileList = uniqueFiles;
      this.currentFiles = this.fileList.map((item) => item.raw);
    },

    // 提交上传
    async submitUpload() {
      if (this.currentFiles.length == 0) {
        this.$message.warning("请先选择模板文件");
        return;
      }
      try {
        this.loading = true;
        for (const file of this.currentFiles) {
          const formData = new FormData();
          formData.append("templateFile", file);
          formData.append("uploaderId", this.login_user.id); // 实际应从登录信息获取
          formData.append("uploaderName", this.login_user.username); // 实际应从登录信息获取
          await templateApi.uploadTemplate(formData);
        }
        this.$message.success("模板上传成功！");
        this.uploadKey++;

        this.fileList = [];
        this.currentFiles = [];
        // 重要：手动清除 el-upload 组件的文件列表
        if (this.$refs.uploadRef) {
          this.$refs.uploadRef.clearFiles();
        }
        this.getTemplateList(); // 刷新列表
      } catch (err) {
        this.$message.error("模板上传失败：" + err.message);
      } finally {
        this.loading = false;
      }
    },

    // 获取模板列表
    async getTemplateList() {
      this.loading = true;
      try {
        const res = await templateApi.getTemplateList();
        this.templateList = res.data;
      } catch (err) {
        this.$message.error("加载模板列表失败：" + err.message);
      } finally {
        this.loading = false;
      }
    },

    // 下载模板
    async downloadTemplate(row) {
      try {
        const res = await templateApi.downloadTemplate(row.id);
        if (res.code == 200) {
          // 1. 获取预签名链接
          const downloadUrl = res.data;
          // 1. 通过 fetch 获取文件流（避免直接访问 MinIO 链接的签名问题）
          const response = await fetch(downloadUrl);
          console.log("response", response);
          if (!response.ok) {
            throw new Error("文件找不到");
          }
          // 2. 转换为 Blob 流
          const blob = await response.blob();
          // 3. 手动创建下载链接，强制指定原始文件名
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement("a");
          a.href = url;
          a.download = row.templateName; // 这里是原始文件名，如 0602-3_________.doc
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

    // 删除模板
    async deleteTemplate(row) {
      this.$confirm("确定要删除该模板吗？删除后不可恢复！", "提示", {
        type: "warning",
      })
        .then(async () => {
          try {
            await templateApi.deleteTemplate(row.id);
            this.$message.success("模板删除成功！");
            this.getTemplateList(); // 刷新列表
          } catch (err) {
            this.$message.error("模板删除失败：" + err.message);
          }
        })
        .catch(() => {
          this.$message.info("已取消删除操作");
        });
    },
  },
};
</script>

<style scoped>
.template-manager-container {
  padding: 20px;
}
.upload-demo {
  margin-bottom: 20px;
}
</style>
