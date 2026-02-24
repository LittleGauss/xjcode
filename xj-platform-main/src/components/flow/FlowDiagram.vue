<template>
  <el-dialog
    :title="dialogTitle"
    :visible="visible"
    width="50%"
    :close-on-click-modal="true"
    @close="handleClose"
    @update:visible="$emit('update:visible', $event)"
  >
    <!-- 操作工具栏 -->
    <div class="toolbar" v-if="!isLoading && hasValidSvg">
      <el-button-group>
        <el-button size="mini" @click="zoomIn" title="放大">
          <i class="el-icon-zoom-in"></i>
        </el-button>
        <el-button size="mini" @click="zoomOut" title="缩小">
          <i class="el-icon-zoom-out"></i>
        </el-button>
        <el-button size="mini" @click="zoomReset" title="重置" type="primary">
          <i class="el-icon-refresh-left"></i>
        </el-button>
        <el-button size="mini" @click="handleClose" title="关闭" type="danger">
          <i class="el-icon-close"></i>
        </el-button>
      </el-button-group>
    </div>

    <!-- 流程基础信息 -->
    <div class="process-info" v-if="processInstanceId">
      <span>流程实例ID：{{ processInstanceId }}</span>
      <span>流程名称：{{ processName }}</span>
      <span style="margin-left: 20px; color: #ff9800"
        >当前流程节点显示为黄色</span
      >
      <span v-if="svgInfo" style="margin-left: 20px; color: #666">
        缩放: {{ (zoom * 100).toFixed(0) }}% | 尺寸: {{ svgInfo.width }} ×
        {{ svgInfo.height }}
      </span>
    </div>

    <!-- SVG 流程图容器 -->
    <div class="svg-container" v-loading="isLoading">
      <div
        class="svg-wrapper"
        ref="svgWrapper"
        @mousedown="handleDragStart"
        @mousemove="handleDragMove"
        @mouseup="handleDragEnd"
        @mouseleave="handleDragEnd"
        @wheel="handleZoom"
      >
        <div
          class="svg-content"
          ref="svgContent"
          :style="svgTransformStyle"
        ></div>
      </div>
    </div>

    <!-- 错误提示 -->
    <div v-if="!isLoading && !hasValidSvg" class="error-message">
      <el-empty description="暂无流程图数据或加载失败"></el-empty>
    </div>
  </el-dialog>
</template>

<script>
import { pendingApi } from "@/api/flowable";
export default {
  name: "FlowDiagram",
  props: {
    visible: {
      type: Boolean,
      required: true,
    },
    processInstanceId: {
      type: String,
      required: true,
    },
    dialogTitle: {
      type: String,
      default: "流程可视化",
    },
    // 新增：默认缩放比例属性
    defaultScale: {
      type: Number,
      default: 2.0, // 默认放大2倍
    },
    // 新增：弹框宽度属性
    dialogWidth: {
      type: String,
      default: "50%", // 默认弹框宽度为50%
    },
  },
  data() {
    return {
      svgContent: "",
      svgInfo: null,
      processName: "",
      isLoading: false,
      // 缩放/拖拽相关状态
      zoom: 1,
      panX: 0,
      panY: 0,
      isDragging: false,
      startX: 0,
      startY: 0,
      startPanX: 0,
      startPanY: 0,
      // 记录初始居中位置
      initialCenterX: 0,
      initialCenterY: 0,
      initialScale: 1,
    };
  },
  computed: {
    svgTransformStyle() {
      return {
        transform: `translate(${this.panX}px, ${this.panY}px) scale(${this.zoom})`,
        transformOrigin: "0 0",
        transition: this.isDragging ? "none" : "transform 0.2s ease",
      };
    },
    // 检查是否有有效的SVG内容
    hasValidSvg() {
      return !!this.svgContent && this.svgContent.trim().length > 0;
    },
  },
  watch: {
    processInstanceId: {
      handler(newVal, oldVal) {
        if (newVal && newVal !== oldVal && this.visible) {
          this.clearSvgContent();
          this.loadFlowData();
        }
      },
      immediate: true,
    },
    visible(newVal) {
      if (newVal && this.processInstanceId) {
        this.loadFlowData();
      } else {
        this.resetSvgState();
      }
    },
    svgContent(newVal) {
      if (newVal && this.$refs.svgContent) {
        this.$nextTick(() => {
          this.renderSvgContent(newVal);
        });
      } else if (!newVal) {
        this.clearSvgDom();
      }
    },
  },
  methods: {
    // 清空SVG DOM内容
    clearSvgDom() {
      if (this.$refs.svgContent) {
        this.$refs.svgContent.innerHTML = "";
        this.svgInfo = null;
      }
    },

    // 清空SVG内容数据
    clearSvgContent() {
      this.svgContent = "";
      this.processName = "";
      this.svgInfo = null;
      this.clearSvgDom();
    },

    // 渲染SVG内容
    renderSvgContent(svgString) {
      try {
        this.clearSvgDom();

        if (!svgString || typeof svgString !== "string") {
          console.warn("无效的SVG字符串");
          return;
        }

        if (!svgString.includes("<svg")) {
          console.warn("SVG字符串不包含<svg>标签");
          return;
        }

        // 清理SVG字符串并添加样式
        const cleanSvg = svgString
          .replace(/<\?xml.*?\?>/g, "")
          .replace(/<svg/, '<svg style="display: block;"')
          .trim();

        this.$refs.svgContent.innerHTML = cleanSvg;

        const svgElement = this.$refs.svgContent.querySelector("svg");
        if (!svgElement) {
          console.warn("SVG元素未成功创建");
          this.clearSvgDom();
          return;
        }

        // 获取SVG尺寸信息
        this.getSvgInfo(svgElement);

        // 自动适应容器 - 这里延迟一下确保DOM完全渲染
        setTimeout(() => {
          this.fitToContainer();
        }, 100);
      } catch (error) {
        console.error("渲染SVG失败:", error);
        this.clearSvgDom();
        this.svgContent = "";
      }
    },

    // 获取SVG尺寸信息
    getSvgInfo(svgElement) {
      try {
        // 首先尝试获取viewBox
        const viewBox = svgElement.getAttribute("viewBox");
        let width, height;

        if (viewBox) {
          const parts = viewBox.split(/\s+|,/);
          if (parts.length >= 4) {
            width = parseFloat(parts[2]);
            height = parseFloat(parts[3]);
          }
        }

        // 如果没有viewBox，尝试获取width/height属性
        if (!width || !height) {
          width = parseFloat(svgElement.getAttribute("width")) || 0;
          height = parseFloat(svgElement.getAttribute("height")) || 0;
        }

        // 如果仍然没有，使用getBoundingClientRect
        if (!width || !height) {
          const rect = svgElement.getBoundingClientRect();
          width = rect.width || 0;
          height = rect.height || 0;
        }

        // 设置默认最小尺寸
        if (width <= 0) width = 100;
        if (height <= 0) height = 100;

        this.svgInfo = {
          width: Math.round(width),
          height: Math.round(height),
          viewBox: viewBox || null,
        };

        console.log("SVG尺寸信息:", this.svgInfo);
      } catch (error) {
        console.warn("获取SVG尺寸失败:", error);
        this.svgInfo = {
          width: 100,
          height: 100,
          viewBox: null,
        };
      }
    },

    fitToContainer() {
      if (!this.svgInfo) return;

      this.$nextTick(() => {
        const wrapper = this.$refs.svgWrapper;
        if (!wrapper) return;

        const wrapperRect = wrapper.getBoundingClientRect();
        const { width: svgWidth, height: svgHeight } = this.svgInfo;

        console.log("========== 智能缩放计算 ==========");
        console.log("SVG原始尺寸:", `${svgWidth} × ${svgHeight}`);
        console.log(
          "容器尺寸:",
          `${wrapperRect.width} × ${wrapperRect.height}`
        );

        // 1. 获取用户指定的默认缩放
        const userDefaultScale = this.defaultScale || 2.0;

        // 2. 计算基础缩放（适应容器）
        const baseScaleX = wrapperRect.width / svgWidth;
        const baseScaleY = wrapperRect.height / svgHeight;
        const baseScale = Math.min(baseScaleX, baseScaleY);

        console.log("基础缩放（适应容器）:", baseScale);
        console.log("用户默认缩放:", userDefaultScale);

        // 3. 智能选择目标缩放
        let targetScale;

        // 情况1：如果基础缩放 > 1，说明容器比SVG大，使用用户指定的缩放
        if (baseScale > 1) {
          targetScale = Math.max(baseScale, userDefaultScale);
          console.log("情况1：容器比SVG大，使用较大值:", targetScale);
        }
        // 情况2：如果基础缩放 < 1，说明SVG比容器大
        else {
          // 如果用户想要放大，允许超过容器大小
          if (userDefaultScale > 1) {
            targetScale = userDefaultScale;
            console.log("情况2：SVG比容器大，用户想放大，使用:", targetScale);
          } else {
            // 用户没指定或想缩小，使用基础缩放
            targetScale = baseScale;
            console.log(
              "情况2：SVG比容器大，用户没指定，使用基础缩放:",
              targetScale
            );
          }
        }

        // 4. 重新计算缩放后尺寸
        const scaledWidth = svgWidth * targetScale;
        const scaledHeight = svgHeight * targetScale;

        console.log("缩放后尺寸:", `${scaledWidth} × ${scaledHeight}`);

        // 5. 智能定位
        let centerX, centerY;

        // 宽度方向：如果缩放后宽度 <= 容器宽度，居中；否则从左边开始
        if (scaledWidth <= wrapperRect.width) {
          centerX = (wrapperRect.width - scaledWidth) / 2;
          console.log("宽度方向：居中，X =", centerX);
        } else {
          centerX = 0; // 从左边开始
          console.log("宽度方向：从左边开始，X = 0");
        }

        // 高度方向：如果缩放后高度 <= 容器高度，居中；否则从顶部开始
        if (scaledHeight <= wrapperRect.height) {
          centerY = (wrapperRect.height - scaledHeight) / 2;
          console.log("高度方向：居中，Y =", centerY);
        } else {
          centerY = 0; // 从顶部开始
          console.log("高度方向：从顶部开始，Y = 0");
        }

        // 6. 限制缩放范围
        targetScale = Math.max(0.5, Math.min(5, targetScale));

        console.log("最终结果:");
        console.log(
          "- 缩放比例:",
          targetScale,
          `(${(targetScale * 100).toFixed(0)}%)`
        );
        console.log("- 显示位置:", `X=${centerX}, Y=${centerY}`);
        console.log("- 缩放后尺寸:", `${scaledWidth} × ${scaledHeight}`);
        console.log(
          "- 容器尺寸:",
          `${wrapperRect.width} × ${wrapperRect.height}`
        );
        console.log("========== 计算结束 ==========");

        // 应用变换
        this.zoom = targetScale;
        this.panX = centerX;
        this.panY = centerY;

        // 保存初始状态
        this.initialScale = targetScale;
        this.initialCenterX = centerX;
        this.initialCenterY = centerY;
      });
    },

    // 重置到初始状态
    zoomReset() {
      if (
        this.initialScale &&
        this.initialCenterX !== undefined &&
        this.initialCenterY !== undefined
      ) {
        this.zoom = this.initialScale;
        this.panX = this.initialCenterX;
        this.panY = this.initialCenterY;
      } else {
        this.fitToContainer();
      }
    },

    async loadFlowData() {
      this.clearSvgContent();

      if (!this.processInstanceId) {
        this.$message.warning("流程实例ID为空");
        return;
      }

      this.isLoading = true;
      try {
        console.log("开始加载流程图，流程实例ID:", this.processInstanceId);

        const res = await pendingApi.getFlowSvg(this.processInstanceId);
        console.log("接口返回：", res);

        if (res.code == 200 && res.data && res.data.diagramImage) {
          this.svgContent = res.data.diagramImage;
          this.processName = res.data.processName || "未命名流程";
        } else {
          console.warn("接口返回数据格式异常:", res);
          throw new Error(res.msg || "接口返回数据格式异常");
        }
      } catch (error) {
        console.error("加载流程图失败:", error);
        this.$message.error(`加载流程图失败`);
        // 确保清空所有SVG内容
        this.clearSvgContent();
      } finally {
        this.isLoading = false;
      }
    },

    handleDragStart(event) {
      if (![0, 1, 2].includes(event.button)) return;
      this.isDragging = true;
      this.startX = event.clientX;
      this.startY = event.clientY;
      this.startPanX = this.panX;
      this.startPanY = this.panY;
      this.$refs.svgWrapper.style.cursor = "move";
      event.preventDefault();
    },

    handleDragMove(event) {
      if (!this.isDragging) return;
      const dx = event.clientX - this.startX;
      const dy = event.clientY - this.startY;
      this.panX = this.startPanX + dx;
      this.panY = this.startPanY + dy;
    },

    handleDragEnd() {
      this.isDragging = false;
      this.$refs.svgWrapper.style.cursor = "default";
    },

    handleZoom(event) {
      event.preventDefault();

      // 获取鼠标相对于容器的位置
      const wrapper = this.$refs.svgWrapper;
      const rect = wrapper.getBoundingClientRect();
      const mouseX = event.clientX - rect.left;
      const mouseY = event.clientY - rect.top;

      // 计算缩放增量
      const delta = event.deltaY > 0 ? -0.1 : 0.1;
      const oldZoom = this.zoom;
      const newZoom = Math.max(0.1, Math.min(5, oldZoom + delta));

      // 以鼠标位置为中心缩放
      const scaleRatio = newZoom / oldZoom;

      // 调整位置，使鼠标位置保持固定
      this.panX = mouseX - (mouseX - this.panX) * scaleRatio;
      this.panY = mouseY - (mouseY - this.panY) * scaleRatio;
      this.zoom = newZoom;
    },

    zoomIn() {
      this.zoom = Math.min(5, this.zoom + 0.2);
    },

    zoomOut() {
      this.zoom = Math.max(0.1, this.zoom - 0.2);
    },

    resetSvgState() {
      this.zoom = 1;
      this.panX = 0;
      this.panY = 0;
      this.svgContent = "";
      this.processName = "";
      this.svgInfo = null;
    },

    handleClose() {
      this.resetSvgState();
      this.$emit("close");
    },
  },
};
</script>

<style scoped>
/* 工具栏样式 */
.toolbar {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 200;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 6px;
  padding: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  border: 1px solid #ebeef5;
}

/* 流程信息样式 */
.process-info {
  margin-bottom: 10px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 4px;
  font-size: 14px;
  border-left: 4px solid #1890ff;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 15px;
}

.process-info span {
  white-space: nowrap;
}

/* SVG 容器样式 */
.svg-container {
  width: 100%;
  height: 500px; /* 减小容器高度 */
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  background: #fafafa;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* SVG 包装器 */
.svg-wrapper {
  width: 100%;
  height: 100%;
  cursor: default;
  user-select: none;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

/* SVG 内容容器 */
.svg-content {
  position: absolute;
  top: 0;
  left: 0;
}

/* 错误消息样式 */
.error-message {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  color: #999;
}

/* 当前流程节点高亮样式 */
.svg-content .active-node {
  stroke: #f57c00 !important;
  stroke-width: 3px !important;
  fill: rgba(255, 152, 0, 0.15) !important;
  animation: highlight 2s infinite alternate;
}

@keyframes highlight {
  from {
    filter: drop-shadow(0 0 5px rgba(255, 152, 0, 0.5));
  }
  to {
    filter: drop-shadow(0 0 8px rgba(255, 152, 0, 0.8));
  }
}
</style>
