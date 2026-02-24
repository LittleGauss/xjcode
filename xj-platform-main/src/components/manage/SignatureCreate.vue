<template>
  <div class="signature-pad">
    <!-- 添加图片上传功能 -->
    <div class="upload-section" style="margin-bottom: 15px">
      <el-upload
        class="signature-uploader"
        action=""
        :auto-upload="false"
        :show-file-list="false"
        :on-change="handleImageUpload"
        accept="image/*"
      >
        <el-button size="small" type="primary">上传签名图片</el-button>
      </el-upload>
      <div
        class="upload-hint"
        style="font-size: 12px; color: #999; margin-top: 5px"
      >
        支持JPG、PNG、GIF等图片格式，文件大小≤1MB
      </div>
    </div>
    <div class="canvas-container">
      <canvas
        ref="canvas"
        width="400"
        height="200"
        @mousedown="startDraw"
        @mousemove="drawing"
        @mouseup="endDraw"
        @touchstart="handleTouchStart"
        @touchmove="handleTouchMove"
        @touchend="endDraw"
        :style="{ cursor: uploadedImage ? 'default' : 'crosshair' }"
      ></canvas>
      <!-- 添加签名提示 -->
      <div
        v-if="!signBase64 && !uploadedImage"
        class="signature-tip"
        :style="{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          color: '#999',
          fontSize: '14px',
          pointerEvents: 'none',
        }"
      >
        请在此处签名或查看已上传的签名图片
      </div>
    </div>
    <div class="form-group">
      <el-input
        v-model="remark"
        :placeholder="defaultRemark || '请输入签名备注（可选）'"
        size="small"
        style="margin-bottom: 10px"
      />
    </div>
    <div class="button-group">
      <el-button @click="clearSign" size="small">清空</el-button>
      <el-button
        type="primary"
        @click="submitSign"
        size="small"
        :loading="submitting"
        >提交签名</el-button
      >
    </div>
  </div>
</template>

<script>
import signatureApi from "@/api/signature";
import { getUserToken } from "@/utils/auth";

export default {
  name: "SignatureCreate",
  props: {
    tableId: {
      type: [String, Number],
      default: null,
    },
  },
  data() {
    return {
      ctx: null,
      isDrawing: false,
      signBase64: "",
      submitting: false,
      remark: "", // 签名备注
      uploadedImage: null, // 新增：存储上传的图片
    };
  },
  mounted() {
    this.initCanvas();
  },
  // 在 computed 中添加
  computed: {
    defaultRemark() {
      if (this.uploadedImage) {
        return "这是我的签名图片";
      } else if (this.signBase64) {
        return "这是我的手写签名";
      }
      return ""; // 默认为空
    },
  },
  methods: {
    // 新增：处理图片上传
    handleImageUpload(file) {
      const maxSize = 1 * 1024 * 1024; // 1MB
      if (file.raw.size > maxSize) {
        this.$message.error("图片大小不能超过1MB");
        return false;
      }

      const reader = new FileReader();
      reader.onload = (e) => {
        this.uploadedImage = e.target.result;
        this.drawUploadedImage(e.target.result);
      };
      reader.readAsDataURL(file.raw);
      return false;
    },
    // 新增：在画布上绘制上传的图片
    drawUploadedImage(imageSrc) {
      const canvas = this.$refs.canvas;
      const ctx = this.ctx;

      // 先清空画布但不清空 uploadedImage 状态
      this.ctx.fillStyle = "#fff";
      this.ctx.fillRect(0, 0, canvas.width, canvas.height);
      this.ctx.fillStyle = "#000";
      this.signBase64 = ""; // 在绘制前清空
      this.remark = ""; // 清空备注

      // 绘制上传的图片
      const img = new Image();
      img.onload = () => {
        // 计算合适的缩放比例以适应画布
        const scale = Math.min(
          canvas.width / img.width,
          canvas.height / img.height,
          1
        );

        const width = img.width * scale;
        const height = img.height * scale;
        const x = (canvas.width - width) / 2;
        const y = (canvas.height - height) / 2;

        ctx.drawImage(img, x, y, width, height);
        this.signBase64 = canvas.toDataURL("image/png");
      };
      img.src = imageSrc;
    },
    initCanvas() {
      const canvas = this.$refs.canvas;
      this.ctx = canvas.getContext("2d");
      this.ctx.lineWidth = 2;
      this.ctx.strokeStyle = "#000";
      this.ctx.lineCap = "round";
      this.ctx.lineJoin = "round";
      this.clearSign();
    },

    getCoordinates(e) {
      const canvas = this.$refs.canvas;
      const rect = canvas.getBoundingClientRect();

      if (e.touches && e.touches.length > 0) {
        // 触摸事件
        return {
          x: e.touches[0].clientX - rect.left,
          y: e.touches[0].clientY - rect.top,
        };
      } else {
        // 鼠标事件
        return {
          x: e.clientX - rect.left,
          y: e.clientY - rect.top,
        };
      }
    },

    startDraw(e) {
      if (this.uploadedImage) return; // 如果已有上传图片，则不允许绘制

      e.preventDefault();
      this.isDrawing = true;
      const pos = this.getCoordinates(e);
      this.ctx.beginPath();
      this.ctx.moveTo(pos.x, pos.y);
    },

    drawing(e) {
      if (this.uploadedImage) return; // 如果已有上传图片，则不允许绘制

      e.preventDefault();
      if (!this.isDrawing) return;
      const pos = this.getCoordinates(e);
      this.ctx.lineTo(pos.x, pos.y);
      this.ctx.stroke();
    },

    endDraw() {
      this.isDrawing = false;
      this.signBase64 = this.$refs.canvas.toDataURL("image/png");
    },

    handleTouchStart(e) {
      this.startDraw(e);
    },

    handleTouchMove(e) {
      this.drawing(e);
    },

    clearSign() {
      const canvas = this.$refs.canvas;
      this.ctx.fillStyle = "#fff";
      this.ctx.fillRect(0, 0, canvas.width, canvas.height);
      this.ctx.fillStyle = "#000";
      this.signBase64 = "";
      this.remark = "";
      this.uploadedImage = null;
      this.isDrawing = false; // 重置绘制状态
    },

    async submitSign() {
      if (!this.signBase64) {
        this.$message.warning("请先绘制签名或上传签名图片");
        return;
      }
      // 如果备注为空，根据签名类型自动填充
      if (!this.remark) {
        this.remark = this.uploadedImage
          ? "这是我的签名图片"
          : "这是我的手写签名";
      }
      this.submitting = true;

      try {
        // 将base64转换为Blob
        const blob = this.dataURItoBlob(this.signBase64);

        // 创建FormData对象
        const formData = new FormData();
        formData.append("file", blob, "signature.png");
        if (this.remark) {
          formData.append("ext1", this.remark);
        }

        // 获取当前用户ID
        const currentUser = getUserToken();
        if (!currentUser || !currentUser.user) {
          throw new Error("未获取到用户信息");
        }
        formData.append("userId", currentUser.user.id);

        // 调用上传接口
        const response = await signatureApi.uploadSignature(formData);

        if (response.code == 200) {
          this.$message.success("签名保存成功");
          this.$emit("success", response.data);
          this.clearSign();
        } else {
          this.$message.error(response.msg || "签名保存失败");
        }
      } catch (error) {
        this.$message.error("签名保存失败: " + error.message);
      } finally {
        this.submitting = false;
      }
    },

    // base64转Blob
    dataURItoBlob(dataURI) {
      let byteString;
      if (dataURI.split(",")[0].indexOf("base64") >= 0) {
        byteString = atob(dataURI.split(",")[1]);
      } else {
        byteString = unescape(dataURI.split(",")[1]);
      }

      const mimeString = dataURI.split(",")[0].split(":")[1].split(";")[0];
      const ia = new Uint8Array(byteString.length);

      for (let i = 0; i < byteString.length; i++) {
        ia[i] = byteString.charCodeAt(i);
      }

      return new Blob([ia], { type: mimeString });
    },
  },
};
</script>

<style scoped>
.signature-pad {
  text-align: center;
}

.canvas-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  display: inline-block;
  margin-bottom: 15px;
  position: relative; /* 添加相对定位 */
}
/* 添加预览对话框样式 */
.image-preview-dialog {
  width: auto !important;
  min-width: 300px;
  max-width: 90vw;
}
/* 签名提示文字样式 */
.signature-tip {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #999;
  font-size: 14px;
  pointer-events: none;
  user-select: none;
}

.image-preview-dialog .el-message-box__content {
  text-align: center;
}

canvas {
  cursor: crosshair;
}

.form-group {
  margin-bottom: 15px;
}

.button-group {
  text-align: center;
}

.button-group .el-button {
  margin: 0 5px;
}
</style>
