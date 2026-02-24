<template>
  <div class="home-container">
    <!-- 头部标题 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @logout="handleLogout"
      @navigate="handleNavigate"
    />
    <div class="bg-gray-100 font-sans min-h-screen">
      <div class="container mx-auto px-4 py-8 flex flex-col md:flex-row gap-6">
        <!-- 侧边导航栏 -->
        <div class="w-full md:w-64 shrink-0 sticky top-4 self-start">
          <div class="pixel-card bg-white p-6 rounded-lg h-full flex flex-col">
            <h1 class="text-2xl font-bold text-gray-800 mb-8 text-center">
              日常监督检查
            </h1>
            <nav class="space-y-3 flex-grow">
              <button
                class="pixel-btn w-full bg-blue-500 text-white px-4 py-3 rounded-md flex items-center"
                @click="goToScanner"
              >
                <i class="fas fa-upload mr-3 w-5 text-center"></i>
                上传记录
              </button>
              <!-- 检查历史按钮 -->
              <button
                class="pixel-btn w-full bg-green-500 text-white px-4 py-3 rounded-md flex items-center"
                @click="isHistoryModalShow = true"
              >
                <i class="fas fa-history mr-3 w-5 text-center"></i>
                检查历史
              </button>
              <!-- 监督员信息 -->
              <div class="border-t border-gray-200 my-4 pt-4">
                <p class="text-sm text-gray-500 mb-2 px-2">监督员信息</p>
                <div class="flex items-center p-2 bg-gray-50 rounded">
                  <img
                    src="https://design.gemcoder.com/staticResource/echoAiSystemImages/d3e68187036e6f447539c0d6ff287751.png"
                    alt="用户头像"
                    class="w-10 h-10 rounded-full object-cover mr-3"
                  />
                  <div>
                    <p class="font-medium text-sm">监督员</p>
                    <p class="text-xs text-gray-500">质量部监督员</p>
                  </div>
                </div>
              </div>
            </nav>
          </div>
        </div>

        <!-- 主内容区域 -->
        <div class="flex-grow min-h-[calc(100vh-200px)]">
          <!-- 系统提醒区域（响应式控制控制显示） -->
          <div
            class="bg-yellow-100 border-l-4 border-yellow-500 text-yellow-700 p-4 mb-4"
            role="alert"
            v-if="isReminderShow"
          >
            <p class="font-bold">系统提醒</p>
            <p>{{ reminderText }}</p>
          </div>

          <!-- 上传记录模态框（Vue 2 响应式控制） -->
          <div
            class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
            v-if="isUploadModalShow"
            @click.self="closeUploadModal"
          >
            <div class="bg-white rounded-lg p-6 w-full max-w-2xl">
              <div class="flex justify-between items-center mb-4">
                <h2 class="text-xl font-bold">上传监督检查记录</h2>
                <button
                  class="text-gray-500 hover:text-gray-700"
                  @click="closeUploadModal"
                >
                  <i class="fas fa-times"></i>
                </button>
              </div>
              <div class="mb-4">
                <p class="text-gray-600 mb-2">
                  请连接高拍仪上传纸质文档扫描件：
                </p>
                <div
                  class="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center cursor-pointer"
                  @click="triggerFileInput"
                >
                  <i class="fas fa-camera text-4xl text-gray-400 mb-2"></i>
                  <p class="text-gray-500">点击或拖拽文件到此处上传</p>
                  <!-- 文件输入框（隐藏，通过 ref 绑定） -->
                  <input
                    class="hidden"
                    type="file"
                    multiple
                    @change="handleFileSelect"
                    ref="fileInputRef"
                  />
                  <button
                    class="pixel-btn bg-blue-500 text-white px-4 py-2 rounded-md mt-4"
                    @click.stop="triggerFileInput"
                  >
                    选择文件
                  </button>
                </div>
              </div>
              <!-- 文件预览区域（响应式渲染） -->
              <div
                class="grid grid-cols-3 gap-4 mb-4"
                v-if="previewFiles.length > 0"
              >
                <div
                  class="relative group"
                  v-for="(file, index) in previewFiles"
                  :key="index"
                >
                  <img
                    :src="file.url"
                    alt="文件预览"
                    class="w-full h-32 object-cover rounded border border-gray-200"
                  />
                  <div
                    class="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
                  >
                    <button
                      class="text-white bg-blue-500 px-2 py-1 rounded text-sm"
                    >
                      <i class="fas fa-search-plus mr-1"></i>
                      查看
                    </button>
                  </div>
                  <!-- 删除预览文件按钮 -->
                  <button
                    class="absolute top-1 right-1 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
                    @click.stop="removePreviewFile(index)"
                  >
                    <i class="fas fa-times text-xs"></i>
                  </button>
                </div>
              </div>
              <!-- 确认上传按钮 -->
              <div class="flex justify-end">
                <button
                  class="pixel-btn bg-green-500 text-white px-6 py-2 rounded-md"
                  @click="handleConfirmUpload"
                >
                  确认上传
                </button>
              </div>
            </div>
          </div>

          <!-- 检查历史模态框（Vue 2 响应式控制） -->
          <div
            class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center"
            v-if="isHistoryModalShow"
            @click.self="closeHistoryModal"
          >
            <div
              class="bg-white rounded-lg p-6 w-full max-w-4xl max-h-[80vh] overflow-y-auto"
            >
              <div class="flex justify-between items-center mb-4">
                <h2 class="text-xl font-bold">监督检查历史记录</h2>
                <button
                  class="text-gray-500 hover:text-gray-700"
                  @click="closeHistoryModal"
                >
                  <i class="fas fa-times"></i>
                </button>
              </div>
              <!-- 统计卡片 -->
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                <div class="pixel-card bg-white p-4 rounded-lg">
                  <div class="flex items-center">
                    <div class="p-3 bg-blue-100 rounded-full mr-4">
                      <i class="fas fa-calendar-alt text-blue-500"></i>
                    </div>
                    <div>
                      <p class="text-gray-500 text-sm">本月检查次数</p>
                      <p class="text-2xl font-bold">3</p>
                    </div>
                  </div>
                </div>
                <div class="pixel-card bg-white p-4 rounded-lg">
                  <div class="flex items-center">
                    <div class="p-3 bg-green-100 rounded-full mr-4">
                      <i class="fas fa-user text-green-500"></i>
                    </div>
                    <div>
                      <p class="text-gray-500 text-sm">监督员</p>
                      <p class="text-2xl font-bold">张监督</p>
                    </div>
                  </div>
                </div>
                <div class="pixel-card bg-white p-4 rounded-lg">
                  <div class="flex items-center">
                    <div class="p-3 bg-purple-100 rounded-full mr-4">
                      <i class="fas fa-file-alt text-purple-500"></i>
                    </div>
                    <div>
                      <p class="text-gray-500 text-sm">总记录数</p>
                      <p class="text-2xl font-bold">24</p>
                    </div>
                  </div>
                </div>
              </div>
              <!-- 历史记录列表（响应式渲染） -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div
                  class="pixel-card bg-white p-4 rounded-lg"
                  v-for="(record, idx) in historyRecords"
                  :key="idx"
                >
                  <div class="flex justify-between items-center mb-2">
                    <h3 class="font-bold">{{ record.date }} 检查记录</h3>
                    <span class="text-sm text-gray-500">
                      监督员: {{ record.supervisor }}
                    </span>
                  </div>
                  <div class="flex flex-wrap gap-2">
                    <div
                      class="relative group"
                      v-for="(img, imgIdx) in record.images"
                      :key="imgIdx"
                    >
                      <img
                        :src="img"
                        alt="检查记录"
                        class="w-24 h-32 object-cover rounded border border-gray-200"
                      />
                      <div
                        class="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
                      >
                        <button
                          class="text-white bg-blue-500 px-2 py-1 rounded text-sm"
                        >
                          <i class="fas fa-search-plus mr-1"></i>
                          查看
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 底部区域 -->
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>

<script>
import { getUserToken, removeToken, removeUserToken } from "@/utils/auth";
import HeaderComponent from "@/components/HeaderComponent";
import FooterComponent from "@/components/FooterComponent";
export default {
  name: "DailySupervisionSystem",
  components: { FooterComponent, HeaderComponent },
  // Vue 2 响应式数据定义
  data() {
    return {
      isUploadModalShow: false, // 上传模态框显示状态
      isHistoryModalShow: false, // 历史模态框显示状态
      isReminderShow: false, // 系统提醒显示状态
      reminderText: "", // 系统提醒文本
      previewFiles: [], // 文件预览列表
      // 从  文件导入历史记录数据
      historyRecords: [],
    };
  },
  // 页面加载完成后执行（Vue 2 生命周期钩子）
  mounted() {
    // 初始化历史记录数据
    //this.historyRecords = supervisionData.historyRecords;
    // 页面初始化时检查系统提醒
    this.checkSystemReminder();
    // 检查是否已加载过reload.js
    if (!window.reloadScriptLoaded) {
      this.loadExternalScript("https://res.gemcoder.com/js/reload.js");
      window.reloadScriptLoaded = true;
    }
  },
  created() {
    // 在组件创建后安全地获取用户信息
    const storedUserInfo = getUserToken();
    if (storedUserInfo && storedUserInfo.user) {
      this.login_user = storedUserInfo.user;
      this.userRoles = storedUserInfo.roles || [];
      this.userPermissions = storedUserInfo.permissions || [];
    } else {
      this.$message.warning("您尚未登录，请先登录！");
      // 如果未登录，跳转到登录页面
      this.$router.push("/login");
    }
  },
  methods: {
    handleNavigate(routeName) {
      this.$router.push({ name: routeName });
    },
    // 退出登录
    /**
     * 用户登出功能
     *
     * 执行以下操作：
     * 1. 清除Vuex中的token状态
     * 2. 移除sessionStorage中的登录用户信息
     * 3. 清空当前组件的用户数据
     * 4. 移除token
     * 5. 重定向到登录页面
     */
    handleLogout() {
      // 1. 清除 Vuex 状态
      removeUserToken();
      this.login_user = null;
      removeToken();
      this.$router.push("/login");
    },
    goToScanner() {
      try {
        // 检查路由实例是否存在
        if (!this.$router) {
          console.error("Vue Router 未正确注入组件");
          return;
        }
        // 使用正确的路由路径(小写)
        this.$router.push("/scanner").catch((err) => {
          console.error("路由跳转失败:", err);
        });
      } catch (error) {
        console.error("跳转逻辑错误:", error);
      }
    },
    // 日常监督检查按钮点击事件
    handleDailySupervisionClick() {
      alert("进入日常监督检查页签");
      // 自动弹出上传模态框（原逻辑保留）
      this.isUploadModalShow = true;
    },
    // 关闭上传模态框（重置状态）
    closeUploadModal() {
      this.isUploadModalShow = false;
      this.previewFiles = []; // 清空预览列表
      if (this.$refs.fileInputRef) {
        this.$refs.fileInputRef.value = ""; // 清空文件输入框，兼容Vue2写法
      }
    },
    // 关闭历史模态框
    closeHistoryModal() {
      this.isHistoryModalShow = false;
    },
    // 触发文件选择（通过ref调用隐藏的input点击事件）
    triggerFileInput() {
      if (this.$refs.fileInputRef) {
        this.$refs.fileInputRef.click();
      }
    },
    // 处理文件选择（生成预览图）
    handleFileSelect(e) {
      const files = e.target.files;
      if (!files || files.length === 0) return;

      // 遍历文件，生成base64预览URL
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        const reader = new FileReader();

        // 箭头函数保留this指向（Vue实例）
        reader.onload = (loadEvent) => {
          this.previewFiles.push({
            name: file.name, // 文件名
            url: loadEvent.target.result, // 预览URL
            raw: file, // 原始文件（后续上传用）
          });
        };

        // 读取文件为base64格式
        reader.readAsDataURL(file);
      }
    },
    // 移除单个预览文件
    removePreviewFile(index) {
      this.previewFiles.splice(index, 1);
      // 若预览列表为空，清空文件输入框（避免重复选择同一文件）
      if (this.previewFiles.length === 0 && this.$refs.fileInputRef) {
        this.$refs.fileInputRef.value = "";
      }
    },
    // 确认上传逻辑
    handleConfirmUpload() {
      if (this.previewFiles.length === 0) {
        alert("请先选择要上传的文件");
        return;
      }

      // 模拟上传（实际项目中替换为axios请求，携带this.previewFiles中的raw文件）
      alert("文件上传成功！");

      // 上传后重置状态
      this.previewFiles = [];
      if (this.$refs.fileInputRef) {
        this.$refs.fileInputRef.value = "";
      }
      this.isUploadModalShow = false;
    },
    // 检查系统提醒（原逻辑：每月1号显示）
    checkSystemReminder() {
      const today = new Date();
      if (today.getDate() === 1) {
        // 获取当前日期（1-31）
        this.isReminderShow = true;
        this.reminderText =
          "系统提醒：本月监督工作已开始，请及时上传记录扫描件。";
      } else {
        this.isReminderShow = false;
      }
    },
    // 加载外部脚本（原页面的reload.js）
    loadExternalScript(src) {
      const script = document.createElement("script");
      script.src = src;
      script.async = true; // 异步加载，不阻塞页面
      document.body.appendChild(script);
    },
  },
};
</script>

<style lang="scss" scoped>
$headHeight: 148px;
$bottomHeight: 161px;

.home-container {
  width: 100vw;
  height: 100vh;
  background: #e5e5e5;
  .head {
    height: $headHeight;
    background-image: url("@/assets/home/top.png");
    &-operate {
      display: flex;
      justify-content: flex-end;
      padding: 65px;
      right: 10%;
      position: relative;
    }
  }
  /* 自定义滚动条样式 */
  ::-webkit-scrollbar {
    width: 8px;
    height: 8px;
  }
  ::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 10px;
  }
  ::-webkit-scrollbar-thumb {
    background: #888;
    border-radius: 10px;
  }
  ::-webkit-scrollbar-thumb:hover {
    background: #555;
  }
  .bottom {
    height: $bottomHeight;
    background: #397dcd;
    width: 100%;
    display: flex;
    &-left {
      width: 30%;
    }
    &-middle {
      width: 50%;
      padding-top: 70px;
      p {
        text-align: center;
        color: #fff;
      }
    }
    &-right {
      width: 30%;
    }
  }
}

/* 像素风格卡片样式（原CSS保留） */
.pixel-card {
  border: 2px solid #333;
  box-shadow: 4px 4px 0 #000;
  transition: all 0.2s ease;
}
.pixel-card:hover {
  transform: translate(-2px, -2px);
  box-shadow: 6px 6px 0 #000;
}

/* 像素风格按钮样式（原CSS保留） */
.pixel-btn {
  border: 2px solid #333;
  box-shadow: 3px 3px 0 #000;
  transition: all 0.2s ease;
  position: relative; /* 添加相对定位 */
  overflow: hidden; /* 防止背景色溢出 */
}
.pixel-btn:hover {
  transform: translate(-1px, -1px);
  box-shadow: 4px 4px 0 #000;
}
.pixel-btn:active {
  transform: translate(1px, 1px);
  box-shadow: 2px 2px 0 #000;
}
/* 修复被选中菜单项的样式 */
.pixel-btn.bg-blue-500 {
  overflow: hidden;
  z-index: 1;
}
</style>
