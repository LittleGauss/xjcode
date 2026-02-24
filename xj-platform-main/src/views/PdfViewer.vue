<template>
  <div class="pdf-viewer" :class="{ 'pdf-viewer--fullscreen': isFullscreen }">
    <div class="pdf-viewer__header">
      <div class="pdf-viewer__title">{{ title }}</div>
      <div class="pdf-viewer__controls">
        <button
          class="pdf-viewer__btn"
          @click="prevPage"
          :disabled="currentPage <= 1 || !pdfReady"
        >
          <i class="fas fa-chevron-left"></i>
        </button>
        <span class="pdf-viewer__page-info">
          第 {{ currentPage }} 页 / 共 {{ totalPages || "--" }} 页
        </span>
        <button
          class="pdf-viewer__btn"
          @click="nextPage"
          :disabled="currentPage >= totalPages || !pdfReady"
        >
          <i class="fas fa-chevron-right"></i>
        </button>

        <!-- 下载按钮（根据 allowDownload 显示） -->
        <button
          v-if="allowDownload"
          class="pdf-viewer__btn pdf-viewer__download-btn"
          @click="downloadPdf"
          title="下载PDF"
        >
          <i class="fas fa-download"></i>
        </button>

        <!-- 全屏按钮 -->
        <button
          class="pdf-viewer__btn pdf-viewer__fullscreen-btn"
          @click="toggleFullscreen"
          :title="isFullscreen ? '退出全屏' : '全屏'"
        >
          <i :class="isFullscreen ? 'fas fa-compress' : 'fas fa-expand'"></i>
        </button>

        <button class="pdf-viewer__close" @click="handleClose">
          <i class="fas fa-times"></i> 关闭
        </button>
      </div>
    </div>
    <div class="pdf-viewer__body" @contextmenu.prevent>
      <div v-if="loading" class="pdf-viewer__loading">
        <i class="fas fa-spinner fa-spin"></i>
        <p>正在加载PDF...</p>
      </div>

      <div v-else-if="error" class="pdf-viewer__error">
        <i class="fas fa-exclamation-triangle"></i>
        <p>{{ error }}</p>
        <button class="pdf-viewer__retry" @click="loadPdf">重试</button>
      </div>

      <!-- 关键修改：确保 Canvas 始终在 DOM 中 -->
      <div v-else class="pdf-viewer__content">
        <div v-if="!pdfReady" class="pdf-viewer__preparing">
          <i class="fas fa-cog fa-spin"></i>
          <p>准备渲染PDF...</p>
        </div>
        <canvas
          v-show="pdfReady"
          ref="canvas"
          class="pdf-viewer__canvas"
          @contextmenu.prevent
        ></canvas>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "PdfViewer",
  props: {
    src: {
      type: String,
      required: false,
      default: null,
    },
    title: {
      type: String,
      default: "PDF 预览",
    },
    // 新增：是否允许下载
    allowDownload: {
      type: Boolean,
      default: false,
    },
    // 新增：下载时的文件名（可选）
    downloadFileName: {
      type: String,
      default: null,
    },
    visible: {
      type: Boolean,
      default: true, // 默认 true 向下兼容，祖先不传也能用
    },
  },

  data() {
    return {
      loading: false,
      error: null,
      currentPage: 1,
      totalPages: 0,
      pdfDoc: null,
      scale: 1.5,
      pdfReady: false,
      renderRetryCount: 0,
      maxRetries: 5,
      isFullscreen: false,
      isClosing: false, // 新增：标记是否正在关闭
      pdfBlob: null, // 新增：保存PDF blob用于下载
      renderedOnce: false, // 新增
    };
  },

  watch: {
    src: {
      handler: "loadPdf",
      immediate: true,
    },
    visible: {
      handler(v) {
        if (v && this.pdfDoc && !this.renderedOnce) {
          this.$nextTick(() => this.renderWithRetry());
        }
      },
    },
  },

  mounted() {
    console.log("PdfViewer mounted");
    this.disableKeyboardEvents();

    // 监听全屏变化事件
    this.setupFullscreenListeners();
  },

  beforeDestroy() {
    this.enableKeyboardEvents();

    // 清理全屏监听器
    this.cleanupFullscreenListeners();

    // 修复：只在文档活跃且在全屏状态时才退出全屏
    if (this.isFullscreen && document && this.isDocumentActive()) {
      this.safeExitFullscreen();
    }
  },

  methods: {
    // 新增：下载PDF方法
    async downloadPdf() {
      if (!this.src) {
        console.error("没有可下载的PDF文件");
        return;
      }

      try {
        // 如果已经有blob数据，直接使用
        if (this.pdfBlob) {
          this.triggerDownload(this.pdfBlob);
          return;
        }

        // 否则重新获取PDF文件
        console.log("开始下载PDF文件...");
        const response = await fetch(this.src, {
          method: "GET",
          mode: "cors",
          credentials: "omit",
        });

        if (!response.ok) {
          throw new Error(
            `下载失败: ${response.status} ${response.statusText}`
          );
        }

        const blob = await response.blob();
        this.triggerDownload(blob);
      } catch (err) {
        console.error("PDF下载失败:", err);
        alert("下载失败: " + err.message);
      }
    },

    // 触发下载
    triggerDownload(blob) {
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;

      // 设置下载文件名
      const fileName =
        this.downloadFileName ||
        this.title ||
        this.getFileNameFromUrl(this.src) ||
        "document.pdf";
      a.download = fileName.endsWith(".pdf") ? fileName : fileName + ".pdf";

      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);

      // 清理URL
      setTimeout(() => URL.revokeObjectURL(url), 100);

      console.log("PDF下载完成:", fileName);
    },

    // 从URL中提取文件名
    getFileNameFromUrl(url) {
      if (!url) return null;
      try {
        const urlObj = new URL(url);
        const pathname = urlObj.pathname;
        return pathname.split("/").pop() || "document.pdf";
      } catch {
        return "document.pdf";
      }
    },

    // 修改loadPdf方法，保存blob用于下载
    async loadPdf() {
      if (!this.src) {
        this.error = "未提供PDF文件URL";
        return;
      }

      try {
        this.loading = true;
        this.error = null;
        this.pdfReady = false;
        this.renderRetryCount = 0;

        console.log("开始加载PDF，URL:", this.src);

        await this.ensurePdfJs();

        // 通过 fetch 获取 PDF 数据
        console.log("通过 fetch 获取 PDF 数据...");
        const response = await fetch(this.src, {
          method: "GET",
          mode: "cors",
          credentials: "omit",
        });

        if (!response.ok) {
          throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const pdfBlob = await response.blob();
        console.log("PDF Blob:", pdfBlob);

        if (pdfBlob.size === 0) {
          throw new Error("PDF文件为空");
        }

        // 保存blob用于下载（如果允许下载）
        if (this.allowDownload) {
          this.pdfBlob = pdfBlob;
        }

        const blobUrl = URL.createObjectURL(pdfBlob);
        console.log("创建 Blob URL:", blobUrl);

        // 使用 Blob URL 加载 PDF
        this.pdfDoc = await window.pdfjsLib.getDocument(blobUrl).promise;
        this.totalPages = this.pdfDoc.numPages;
        this.currentPage = 1;

        console.log("PDF文档加载成功，总页数:", this.totalPages);

        // 开始渲染
        // 关键：只有当已经可见了再开始画
        if (this.visible) {
          await this.$nextTick();
          this.renderWithRetry();
        }
      } catch (err) {
        console.error("PDF加载失败:", err);
        this.error = this.getErrorMessage(err);
      } finally {
        this.loading = false;
      }
    },

    // 新增：检查文档是否活跃
    isDocumentActive() {
      return (
        document &&
        document.visibilityState === "visible" &&
        document.hasFocus()
      );
    },

    // 新增：安全的退出全屏方法
    safeExitFullscreen() {
      try {
        // 检查是否在全屏状态
        const fullscreenElement =
          document.fullscreenElement ||
          document.webkitFullscreenElement ||
          document.mozFullScreenElement ||
          document.msFullscreenElement;

        if (fullscreenElement) {
          this.exitFullscreen();
        }
      } catch (err) {
        console.warn("安全退出全屏时发生警告:", err.message);
        // 忽略错误，不抛出
      }
    },

    // 修改：关闭处理方法
    handleClose() {
      this.isClosing = true;

      // 如果在全屏状态，先退出全屏再关闭
      if (this.isFullscreen) {
        this.safeExitFullscreen();
        // 给一点时间让全屏退出动画完成
        setTimeout(() => {
          this.$emit("close");
        }, 100);
      } else {
        this.$emit("close");
      }
    },

    async ensurePdfJs() {
      if (window.pdfjsLib) {
        window.pdfjsLib.GlobalWorkerOptions.workerSrc =
          "https://cdn.jsdelivr.net/npm/pdfjs-dist@2.16.105/build/pdf.worker.min.js";
        return true;
      }

      return new Promise((resolve, reject) => {
        const script = document.createElement("script");
        script.src =
          "https://cdn.jsdelivr.net/npm/pdfjs-dist@2.16.105/build/pdf.min.js";
        script.onload = () => {
          console.log("PDF.js loaded successfully");
          window.pdfjsLib.GlobalWorkerOptions.workerSrc =
            "https://cdn.jsdelivr.net/npm/pdfjs-dist@2.16.105/build/pdf.worker.min.js";
          resolve(true);
        };
        script.onerror = () => {
          console.error("PDF.js 加载失败");
          reject(new Error("PDF.js 加载失败"));
        };
        document.head.appendChild(script);
      });
    },

    /* ===== 1. 新增：等 canvas 真正出现 ===== */
    async waitForCanvas(max = 50) {
      // 最多 1s
      while (!(this.$refs.canvas && this.$refs.canvas.getContext)) {
        if (--max <= 0) throw new Error("Canvas 一直未出现");
        await new Promise((r) => setTimeout(r, 20));
      }
    },

    /* ===== 2. 修改：渲染入口 ===== */
    async renderWithRetry() {
      try {
        this.pdfReady = true; // 让模板开始渲染 canvas
        await this.waitForCanvas(); // 关键：真正等到 canvas 出现
        await this.renderPage(this.currentPage);
        this.renderedOnce = true;
      } catch (err) {
        console.error("首次渲染失败:", err);

        if (this.renderRetryCount < this.maxRetries) {
          this.renderRetryCount++;
          console.log(`第 ${this.renderRetryCount} 次重试...`);
          setTimeout(() => this.renderWithRetry(), 200 * this.renderRetryCount);
        } else {
          this.error = `渲染失败，已重试 ${this.maxRetries} 次`;
        }
      }
    },

    async renderPage(pageNum) {
      if (!this.pdfDoc) {
        throw new Error("PDF文档未加载");
      }

      const canvas = this.$refs.canvas;
      if (!canvas) {
        throw new Error("Canvas 元素未找到");
      }

      console.log(`开始渲染第 ${pageNum} 页，Canvas:`, canvas);

      try {
        const page = await this.pdfDoc.getPage(pageNum);
        const ctx = canvas.getContext("2d");

        // 根据容器大小计算合适的缩放比例
        const container = this.$el.querySelector(".pdf-viewer__content");
        const containerWidth = container ? container.clientWidth - 40 : 800; // 减去 padding
        const viewport = page.getViewport({ scale: 1 });
        const scale = Math.min(containerWidth / viewport.width, 1.5);

        const scaledViewport = page.getViewport({ scale });
        canvas.width = scaledViewport.width;
        canvas.height = scaledViewport.height;

        console.log(
          `渲染尺寸: ${canvas.width}x${canvas.height}, 缩放: ${scale}`
        );

        const renderContext = {
          canvasContext: ctx,
          viewport: scaledViewport,
        };

        await page.render(renderContext).promise;
        console.log(`第 ${pageNum} 页渲染完成`);
      } catch (err) {
        console.error("页面渲染失败:", err);
        throw err;
      }
    },

    // 全屏功能
    setupFullscreenListeners() {
      this.fullscreenChangeHandler = () => {
        this.isFullscreen = !!(
          document.fullscreenElement ||
          document.webkitFullscreenElement ||
          document.mozFullScreenElement ||
          document.msFullscreenElement
        );
      };

      document.addEventListener(
        "fullscreenchange",
        this.fullscreenChangeHandler
      );
      document.addEventListener(
        "webkitfullscreenchange",
        this.fullscreenChangeHandler
      );
      document.addEventListener(
        "mozfullscreenchange",
        this.fullscreenChangeHandler
      );
      document.addEventListener(
        "MSFullscreenChange",
        this.fullscreenChangeHandler
      );
    },

    cleanupFullscreenListeners() {
      if (this.fullscreenChangeHandler) {
        document.removeEventListener(
          "fullscreenchange",
          this.fullscreenChangeHandler
        );
        document.removeEventListener(
          "webkitfullscreenchange",
          this.fullscreenChangeHandler
        );
        document.removeEventListener(
          "mozfullscreenchange",
          this.fullscreenChangeHandler
        );
        document.removeEventListener(
          "MSFullscreenChange",
          this.fullscreenChangeHandler
        );
      }
    },

    toggleFullscreen() {
      if (this.isFullscreen) {
        this.safeExitFullscreen();
      } else {
        this.enterFullscreen();
      }
    },

    async enterFullscreen() {
      const element = this.$el;

      try {
        if (element.requestFullscreen) {
          await element.requestFullscreen();
        } else if (element.webkitRequestFullscreen) {
          await element.webkitRequestFullscreen();
        } else if (element.mozRequestFullScreen) {
          await element.mozRequestFullScreen();
        } else if (element.msRequestFullscreen) {
          await element.msRequestFullscreen();
        } else {
          console.warn("全屏API不支持");
          return;
        }
      } catch (err) {
        console.error("进入全屏失败:", err);
      }
    },

    exitFullscreen() {
      try {
        if (document.exitFullscreen) {
          document.exitFullscreen();
        } else if (document.webkitExitFullscreen) {
          document.webkitExitFullscreen();
        } else if (document.mozCancelFullScreen) {
          document.mozCancelFullScreen();
        } else if (document.msExitFullscreen) {
          document.msExitFullscreen();
        }
      } catch (err) {
        // 修复：捕获但不重新抛出错误
        console.warn("退出全屏时发生警告（可忽略）:", err.message);
      }
    },

    getErrorMessage(err) {
      if (
        err.message.includes("CORS") ||
        err.message.includes("cross-origin")
      ) {
        return "跨域访问被拒绝，请联系管理员配置CORS";
      } else if (err.message.includes("Invalid PDF")) {
        return "PDF文件格式错误或损坏";
      } else if (err.message.includes("HTTP")) {
        return `文件加载失败: ${err.message}`;
      } else {
        return err.message || "PDF加载失败";
      }
    },

    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--;
        this.renderPage(this.currentPage);
      }
    },

    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++;
        this.renderPage(this.currentPage);
      }
    },

    disableKeyboardEvents() {
      this.keyboardHandler = (e) => {
        if (
          (e.ctrlKey || e.metaKey) &&
          (e.key === "s" || e.key === "p" || e.key === "c")
        ) {
          e.preventDefault();
          e.stopPropagation();
        }
        if (e.key === "F12") {
          e.preventDefault();
          e.stopPropagation();
        }
        // ESC 键退出全屏
        if (e.key === "Escape" && this.isFullscreen) {
          this.safeExitFullscreen();
        }
      };
      document.addEventListener("keydown", this.keyboardHandler, true);
    },

    enableKeyboardEvents() {
      if (this.keyboardHandler) {
        document.removeEventListener("keydown", this.keyboardHandler, true);
      }
    },
  },
};
</script>

<style scoped>
.pdf-viewer__download-btn {
  background: #10b981 !important;
  color: white !important;
  border-color: #10b981 !important;
}

.pdf-viewer__download-btn:hover:not(:disabled) {
  background: #059669 !important;
  border-color: #059669 !important;
}
.pdf-viewer {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  background: #f8fafc;
  transition: all 0.3s ease;
}

.pdf-viewer--fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 9999;
  background: #000;
}

.pdf-viewer__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #ffffff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.pdf-viewer--fullscreen .pdf-viewer__header {
  background: #1f2937;
  color: white;
  border-bottom-color: #374151;
}

.pdf-viewer__title {
  font-weight: 600;
  font-size: 16px;
  color: #1f2937;
}

.pdf-viewer--fullscreen .pdf-viewer__title {
  color: white;
}

.pdf-viewer__controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pdf-viewer__btn {
  background: #f3f4f6;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 6px 12px;
  cursor: pointer;
  color: #374151;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pdf-viewer__btn:hover:not(:disabled) {
  background: #e5e7eb;
  border-color: #9ca3af;
}

.pdf-viewer__btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pdf-viewer--fullscreen .pdf-viewer__btn {
  background: #374151;
  border-color: #4b5563;
  color: white;
}

.pdf-viewer--fullscreen .pdf-viewer__btn:hover:not(:disabled) {
  background: #4b5563;
}

.pdf-viewer__page-info {
  font-size: 14px;
  color: #6b7280;
  min-width: 100px;
  text-align: center;
}

.pdf-viewer--fullscreen .pdf-viewer__page-info {
  color: #d1d5db;
}

.pdf-viewer__fullscreen-btn {
  margin-left: 8px;
}

.pdf-viewer__close {
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.pdf-viewer__close:hover {
  background: #dc2626;
}
.pdf-viewer__body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: auto;
  background: #525252;
  padding: 20px;
}

.pdf-viewer--fullscreen .pdf-viewer__body {
  background: #000;
  padding: 10px;
}

.pdf-viewer__content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  gap: 16px;
}

.pdf-viewer__canvas {
  max-width: 100%;
  max-height: 100%;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1),
    0 2px 4px -1px rgba(0, 0, 0, 0.06);
  background: white;
}

.pdf-viewer--fullscreen .pdf-viewer__canvas {
  box-shadow: none;
}

.pdf-viewer__loading,
.pdf-viewer__error,
.pdf-viewer__preparing {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: white;
  text-align: center;
}

.pdf-viewer__loading i,
.pdf-viewer__error i,
.pdf-viewer__preparing i {
  font-size: 32px;
  color: #e5e7eb;
}

.pdf-viewer__retry {
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
}

.pdf-viewer__retry:hover {
  background: #2563eb;
}
</style>
