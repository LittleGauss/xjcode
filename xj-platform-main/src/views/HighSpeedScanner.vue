<template>
  <div class="home-container">
    <!-- 使用通用页头 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @logout="handleLogout"
      @navigate="handleNavigate"
    />

    <div
      class="min-h-screen bg-gray-100 font-sans antialiased flex items-center justify-center p-4"
    >
      <div class="bg-white rounded-lg shadow-xl p-8 w-full max-w-4xl">
        <h1 class="text-3xl font-bold text-gray-800 mb-6 text-center">
          高拍仪图片上传及保存
        </h1>

        <!-- 上传附件按钮：放在标题下方 -->
        <div class="flex justify-center mb-6">
          <label
            class="inline-flex items-center bg-gray-100 hover:bg-gray-200 border rounded px-3 py-2 cursor-pointer text-sm"
            title="上传附件并添加到已拍摄照片"
          >
            <input
              type="file"
              accept="image/*"
              multiple
              @change="importPhotos"
              class="hidden"
            />
            <i class="fas fa-paperclip mr-2"></i>
            上传附件
          </label>
        </div>

        <!-- 操作员信息 -->
        <div class="mb-8 grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label
              class="block text-gray-700 text-sm font-bold mb-2"
              for="operatorName"
            >
              操作员姓名:
            </label>
            <input
              v-model="operatorName"
              type="text"
              placeholder="请输入操作员姓名"
              class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              id="operatorName"
            />
          </div>
          <div>
            <label
              class="block text-gray-700 text-sm font-bold mb-2"
              for="scannerId"
            >
              扫描仪编号:
            </label>
            <input
              v-model="scannerId"
              type="text"
              placeholder="请输入扫描仪编号"
              class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              id="scannerId"
            />
          </div>
        </div>

        <!-- 业务类型选择 -->
        <div class="mb-8">
          <label
            class="block text-gray-700 text-sm font-bold mb-2"
            for="businessType"
          >
            选择业务类型:
          </label>
          <select
            v-model="selectedBusinessType"
            class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline bg-white"
            id="businessType"
          >
            <option value="businessAcceptance">业务受理</option>
            <option value="sampleTransfer">样品传递</option>
            <option value="supervisionInspection">监督检查</option>
            <option value="assetManagement">资产管理</option>
          </select>
        </div>

        <!-- 业务编号输入 -->
        <div class="mb-8">
          <label
            class="block text-gray-700 text-sm font-bold mb-2"
            for="businessNumber"
          >
            业务编号:
          </label>
          <input
            v-model="businessNumber"
            type="text"
            placeholder="请输入业务编号"
            class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            id="businessNumber"
          />
        </div>

        <!-- 备注信息 -->
        <div class="mb-8">
          <label
            class="block text-gray-700 text-sm font-bold mb-2"
            for="remarks"
          >
            备注信息:
          </label>
          <textarea
            v-model="remarks"
            placeholder="请输入备注信息"
            rows="3"
            class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            id="remarks"
          ></textarea>
        </div>

        <!-- 高拍仪操作区域 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
          <!-- 视频预览区 -->
          <div
            class="bg-gray-200 rounded-lg p-4 flex flex-col items-center justify-center h-80 relative overflow-hidden"
          >
            <video
              autoplay
              class="w-full h-full object-cover rounded-lg"
              ref="videoElement"
              playsinline
            ></video>
            <div v-show="!cameraActive" class="absolute text-gray-500 text-lg">
              <i class="fas fa-video-slash text-4xl mb-2"></i>
              <p>摄像头未启动或不可用</p>
            </div>
          </div>

          <!-- 拍照及操作按钮 -->
          <div class="flex flex-col justify-between">
            <div class="mb-4">
              <label
                class="block text-gray-700 text-sm font-bold mb-2"
                for="cameraSelect"
              >
                选择高拍仪设备:
              </label>
              <select
                v-model="selectedCamera"
                class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline bg-white"
                id="cameraSelect"
              >
                <option value="">-- 请选择设备 --</option>
                <option
                  v-for="device in cameraDevices"
                  :key="device.deviceId"
                  :value="device.deviceId"
                >
                  {{
                    device.label ||
                    `摄像头 ${cameraDevices.indexOf(device) + 1}`
                  }}
                </option>
              </select>
            </div>

            <div class="flex flex-col space-y-4">
              <button
                @click="toggleCamera"
                :class="[
                  'text-white font-bold py-3 px-6 rounded-lg shadow-md transition duration-300 ease-in-out flex items-center justify-center',
                  cameraActive
                    ? 'bg-red-500 hover:bg-red-700'
                    : 'bg-blue-500 hover:bg-blue-700',
                ]"
              >
                <i class="fas fa-video mr-2"></i>
                {{ cameraActive ? "停止高拍仪" : "启动高拍仪" }}
              </button>

              <button
                @click="takePhoto"
                :disabled="!cameraActive"
                :class="[
                  'text-white font-bold py-3 px-6 rounded-lg shadow-md transition duration-300 ease-in-out flex items-center justify-center',
                  cameraActive
                    ? 'bg-green-500 hover:bg-green-700'
                    : 'bg-green-300 cursor-not-allowed',
                ]"
              >
                <i class="fas fa-plus-circle mr-2"></i>
                拍照
              </button>

              <button
                @click="savePhotos"
                :disabled="capturedPhotos.length === 0 || saving"
                :class="[
                  'text-white font-bold py-3 px-6 rounded-lg shadow-md transition duration-300 ease-in-out flex items-center justify-center',
                  capturedPhotos.length > 0 && !saving
                    ? 'bg-purple-500 hover:bg-purple-700'
                    : 'bg-purple-300 cursor-not-allowed',
                ]"
              >
                <i class="fas fa-save mr-2"></i>
                {{ saving ? "保存中..." : "保存所有照片" }}
              </button>

              <button
                @click="loadHistoryPhotos"
                :disabled="loadingHistory"
                :class="[
                  'text-white font-bold py-3 px-6 rounded-lg shadow-md transition duration-300 ease-in-out flex items-center justify-center',
                  !loadingHistory
                    ? 'bg-orange-500 hover:bg-orange-700'
                    : 'bg-orange-300 cursor-not-allowed',
                ]"
              >
                <i class="fas fa-history mr-2"></i>
                {{ loadingHistory ? "加载中..." : "查看历史记录" }}
              </button>
            </div>

            <div class="mt-6 text-sm text-gray-600 text-center">
              <p>请确保您的设备已连接高拍仪并授权摄像头访问。</p>
            </div>
          </div>
        </div>

        <!-- 已拍摄照片预览区 -->
        <div class="mb-8">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-xl font-semibold text-gray-800">
              已拍摄照片
              <span class="text-blue-600">({{ capturedPhotos.length }})</span>
            </h2>
            <div class="flex items-center space-x-3">
              <label
                class="inline-flex items-center bg-gray-100 hover:bg-gray-200 border rounded px-3 py-2 cursor-pointer text-sm"
              >
                <input
                  type="file"
                  accept="image/*"
                  multiple
                  @change="importPhotos"
                  class="hidden"
                />
                <i class="fas fa-file-import mr-2"></i>
                选择照片导入
              </label>
              <button
                v-if="capturedPhotos.length > 0"
                @click="displaySelected"
                class="bg-blue-500 text-white px-3 py-2 rounded text-sm hover:bg-blue-600"
              >
                显示所选 ({{ selectedPhotoIds.length }})
              </button>

              <button
                v-if="showOnlySelected"
                @click="showAll"
                class="bg-gray-200 text-gray-800 px-3 py-2 rounded text-sm hover:bg-gray-300"
              >
                显示全部
              </button>

              <button
                v-if="capturedPhotos.length > 0"
                @click="clearAllPhotos"
                class="text-red-600 hover:text-red-800 text-sm flex items-center"
              >
                <i class="fas fa-trash mr-1"></i>
                清空所有
              </button>
            </div>
          </div>
          <div
            class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4 bg-gray-50 p-4 rounded-lg min-h-[150px] max-h-[400px] overflow-y-auto border border-gray-200"
          >
            <!-- 照片预览 -->
            <div
              v-for="(photo, index) in displayedPhotos"
              :key="photo.id"
              class="relative group rounded-lg overflow-hidden shadow-md border border-gray-200"
            >
              <img
                :src="photo.thumbnail || photo.data"
                :alt="`拍摄照片 ${index + 1}`"
                class="w-full h-32 object-cover transition-transform duration-300 group-hover:scale-105"
                @click="viewPhoto(photo)"
              />
              <!-- 选择复选框 -->
              <div class="absolute top-1 left-1">
                <button
                  @click.stop="toggleSelectPhoto(photo)"
                  :title="isSelected(photo) ? '取消选择' : '选择显示'"
                  class="bg-white bg-opacity-80 rounded-full p-1 border text-sm shadow"
                >
                  <i
                    v-if="isSelected(photo)"
                    class="fas fa-check text-green-600"
                  ></i>
                  <i v-else class="far fa-square text-gray-600"></i>
                </button>
              </div>

              <div class="absolute top-1 right-1 flex space-x-1">
                <button
                  @click.stop="deletePhoto(photo)"
                  class="bg-red-600 hover:bg-red-700 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity duration-300"
                  title="删除照片"
                >
                  <i class="fas fa-times text-xs"></i>
                </button>
              </div>
              <div
                class="absolute bottom-1 left-1 bg-black bg-opacity-50 text-white text-xs px-1 rounded"
              >
                {{ index + 1 }}
              </div>
            </div>

            <!-- 无照片提示 -->
            <div
              v-if="capturedPhotos.length === 0"
              class="col-span-full text-center text-gray-500 py-8"
            >
              <i class="fas fa-image text-4xl mb-2"></i>
              <p>暂无照片，请点击"拍照"按钮添加。</p>
            </div>
          </div>
        </div>

        <!-- 操作日志/提示信息 -->
        <div
          :class="[
            'border px-4 py-3 rounded relative',
            statusType === 'success'
              ? 'bg-green-50 border-green-200 text-green-700'
              : statusType === 'error'
              ? 'bg-red-50 border-red-200 text-red-700'
              : 'bg-blue-50 border-blue-200 text-blue-700',
          ]"
          role="alert"
        >
          <strong class="font-bold">{{ statusTitle }}:</strong>
          <span class="block sm:inline ml-2">{{ statusMessage }}</span>
        </div>
      </div>

      <!-- 图片查看模态框 -->
      <div
        v-if="showPhotoModal"
        class="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50 p-4"
        @click="showPhotoModal = false"
      >
        <div
          class="bg-white rounded-lg max-w-4xl max-h-full overflow-auto"
          @click.stop
        >
          <div class="p-4 border-b flex justify-between items-center">
            <h3 class="text-lg font-semibold">图片预览</h3>
            <button
              @click="showPhotoModal = false"
              class="text-gray-500 hover:text-gray-700"
            >
              <i class="fas fa-times"></i>
            </button>
          </div>
          <div class="p-4">
            <img
              :src="currentPhoto.data"
              alt="预览图片"
              class="max-w-full max-h-96 mx-auto"
            />
          </div>
        </div>
      </div>

      <!-- 历史记录模态框 -->
      <div
        v-if="showHistoryModal"
        class="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50 p-4"
        @click="showHistoryModal = false"
      >
        <div
          class="bg-white rounded-lg max-w-6xl max-h-full w-full overflow-auto"
          @click.stop
        >
          <div class="p-4 border-b flex justify-between items-center">
            <h3 class="text-lg font-semibold">历史扫描记录</h3>
            <button
              @click="showHistoryModal = false"
              class="text-gray-500 hover:text-gray-700"
            >
              <i class="fas fa-times"></i>
            </button>
          </div>
          <div class="p-4">
            <div
              v-if="historyRecords.length === 0"
              class="text-center text-gray-500 py-8"
            >
              <i class="fas fa-history text-4xl mb-2"></i>
              <p>暂无历史记录</p>
            </div>
            <div v-else class="space-y-4">
              <div
                v-for="record in historyRecords"
                :key="record.id"
                class="border rounded-lg p-4 hover:bg-gray-50 cursor-pointer"
                @click="viewRecordDetail(record)"
              >
                <div class="flex justify-between items-start">
                  <div>
                    <h4 class="font-semibold">记录ID: {{ record.id }}</h4>
                    <p class="text-sm text-gray-600">
                      业务类型: {{ record.functionSelected }}
                    </p>
                    <p class="text-sm text-gray-600">
                      操作员: {{ record.operatorName }}
                    </p>
                  </div>
                  <div class="text-right">
                    <p class="text-sm text-gray-500">
                      {{ formatDate(record.createdAt) }}
                    </p>
                    <button
                      @click.stop="deleteRecord(record.id)"
                      class="text-red-600 hover:text-red-800 text-sm mt-2"
                    >
                      <i class="fas fa-trash mr-1"></i>删除
                    </button>
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
// 使用您现有的http实例或创建新的
import http from "@/utils/request";
import HeaderComponent from "@/components/HeaderComponent.vue";
import FooterComponent from "@/components/FooterComponent.vue";
import { getUserToken, removeToken, removeUserToken } from "@/utils/auth";

export default {
  name: "DocumentCamera",
  components: {
    HeaderComponent,
    FooterComponent,
  },
  data() {
    return {
      // 后端接口字段
      operatorName: "",
      scannerId: "",
      selectedBusinessType: "businessAcceptance",
      businessNumber: "",
      remarks: "",
      selectedCamera: "",

      // 系统状态
      cameraDevices: [],
      mediaStream: null,
      cameraActive: false,
      capturedPhotos: [],
      // 新增：已选择要显示的照片 id 列表
      selectedPhotoIds: [],
      // 是否仅显示已选择的照片
      showOnlySelected: false,
      statusMessage: "等待操作...",
      statusTitle: "提示",
      statusType: "info",
      saving: false,
      loadingHistory: false,
      showPhotoModal: false,
      showHistoryModal: false,
      currentPhoto: null,
      historyRecords: [],
      login_user: null,
      userRoles: [],
    };
  },
  computed: {
    // 根据 showOnlySelected 决定展示全部还是已选
    displayedPhotos() {
      if (this.showOnlySelected) {
        return this.capturedPhotos.filter((p) =>
          this.selectedPhotoIds.includes(p.id)
        );
      }
      return this.capturedPhotos;
    },
  },
  mounted() {
    this.enumerateDevices();
    this.setStatus('请点击"启动高拍仪"按钮开始。', "info");
  },
  beforeUnmount() {
    if (this.mediaStream) {
      this.stopCamera();
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
    // 退出登录
    handleLogout() {
      // 1. 清除 Vuex 状态
      removeUserToken();
      this.login_user = null;
      removeToken();
      this.$router.push("/login");
    },
    handleNavigate(routeName) {
      if (routeName == "UserCenter") {
        return;
      }
      this.$router.push({ name: routeName });
    },
    // 枚举可用摄像头设备
    async enumerateDevices() {
      try {
        const devices = await navigator.mediaDevices.enumerateDevices();
        this.cameraDevices = devices.filter(
          (device) => device.kind === "videoinput"
        );

        if (this.cameraDevices.length > 0) {
          this.setStatus(
            `找到 ${this.cameraDevices.length} 个视频设备。`,
            "info"
          );
        } else {
          this.setStatus("未找到视频输入设备。", "error");
        }
      } catch (err) {
        console.error("枚举设备失败:", err);
        this.setStatus(`枚举设备失败: ${err.message}`, "error");
      }
    },

    // 启动/停止摄像头
    async toggleCamera() {
      if (this.cameraActive) {
        this.stopCamera();
        return;
      }

      if (!this.selectedCamera) {
        this.setStatus("请先选择一个高拍仪设备。", "error");
        return;
      }

      this.setStatus("正在尝试启动高拍仪...", "info");
      try {
        this.mediaStream = await navigator.mediaDevices.getUserMedia({
          video: {
            deviceId: this.selectedCamera
              ? { exact: this.selectedCamera }
              : undefined,
            width: { ideal: 1920 },
            height: { ideal: 1080 },
          },
        });

        this.$refs.videoElement.srcObject = this.mediaStream;
        this.cameraActive = true;
        this.setStatus("高拍仪已启动。", "success");
      } catch (err) {
        console.error("无法访问高拍仪:", err);
        this.setStatus(`启动高拍仪失败: ${err.message}`, "error");
      }
    },

    // 停止摄像头
    stopCamera() {
      if (this.mediaStream) {
        this.mediaStream.getTracks().forEach((track) => track.stop());
        this.$refs.videoElement.srcObject = null;
        this.mediaStream = null;
      }
      this.cameraActive = false;
      this.setStatus("高拍仪已停止。", "info");
    },

    // 拍照
    takePhoto() {
      if (!this.cameraActive) {
        this.setStatus("请先启动高拍仪。", "error");
        return;
      }

      const video = this.$refs.videoElement;
      const canvas = document.createElement("canvas");
      const thumbnailCanvas = document.createElement("canvas");

      // 原图画质
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
      const context = canvas.getContext("2d");
      context.drawImage(video, 0, 0, canvas.width, canvas.height);
      const dataUrl = canvas.toDataURL("image/jpeg", 0.8);

      // 缩略图
      thumbnailCanvas.width = 200;
      thumbnailCanvas.height = 150;
      const thumbnailContext = thumbnailCanvas.getContext("2d");
      thumbnailContext.drawImage(video, 0, 0, 200, 150);
      const thumbnailUrl = thumbnailCanvas.toDataURL("image/jpeg", 0.6);

      const photoData = {
        id: Date.now() + Math.random(),
        data: dataUrl,
        thumbnail: thumbnailUrl,
        timestamp: new Date().toISOString(),
        sequence: this.capturedPhotos.length + 1,
        fileName: `photo_${Date.now()}_${this.capturedPhotos.length + 1}.jpg`,
      };

      this.capturedPhotos.push(photoData);
      this.setStatus(
        `已拍摄第 ${this.capturedPhotos.length} 张照片。`,
        "success"
      );
    },

    // 删除照片（传入 photo 对象）
    deletePhoto(photo) {
      const idx = this.capturedPhotos.findIndex((p) => p.id === photo.id);
      if (idx !== -1) {
        this.capturedPhotos.splice(idx, 1);
        // 如果被删除的照片在已选择列表中，也将其移除
        this.selectedPhotoIds = this.selectedPhotoIds.filter(
          (id) => id !== photo.id
        );
        this.setStatus("照片已删除。", "info");
      }
    },

    // 清空所有照片
    clearAllPhotos() {
      if (confirm("确定要清空所有照片吗？")) {
        this.capturedPhotos = [];
        this.setStatus("所有照片已清空。", "info");
      }
    },

    // 导入本地图片文件为照片
    importPhotos(event) {
      const files = Array.from(event.target.files || []);
      if (files.length === 0) return;

      files.forEach((file) => {
        const reader = new FileReader();
        reader.onload = (e) => {
          const dataUrl = e.target.result;
          const img = new Image();
          img.onload = () => {
            // 创建缩略图
            const thumbnailCanvas = document.createElement("canvas");
            thumbnailCanvas.width = 200;
            thumbnailCanvas.height = 150;
            const ctx = thumbnailCanvas.getContext("2d");
            ctx.drawImage(img, 0, 0, 200, 150);
            const thumbnailUrl = thumbnailCanvas.toDataURL("image/jpeg", 0.7);

            const photoData = {
              id: Date.now() + Math.random(),
              data: dataUrl,
              thumbnail: thumbnailUrl,
              timestamp: new Date().toISOString(),
              sequence: this.capturedPhotos.length + 1,
              fileName: file.name,
            };

            this.capturedPhotos.push(photoData);
            this.setStatus(`已导入 ${file.name}`, "success");
          };
          img.src = dataUrl;
        };
        reader.readAsDataURL(file);
      });

      // 清空 input 值以便下一次能选择同一文件
      event.target.value = null;
    },

    // 切换照片选择
    toggleSelectPhoto(photo) {
      if (this.selectedPhotoIds.includes(photo.id)) {
        this.selectedPhotoIds = this.selectedPhotoIds.filter(
          (id) => id !== photo.id
        );
      } else {
        this.selectedPhotoIds.push(photo.id);
      }
    },

    // 检查照片是否被选中
    isSelected(photo) {
      return this.selectedPhotoIds.includes(photo.id);
    },

    // 显示所选照片（仅显示已选）
    displaySelected() {
      if (this.selectedPhotoIds.length === 0) {
        this.setStatus("未选择任何照片。", "error");
        return;
      }
      this.showOnlySelected = true;
      this.setStatus(
        `已切换为仅显示 ${this.selectedPhotoIds.length} 张照片。`,
        "info"
      );
    },

    // 恢复显示全部
    showAll() {
      this.showOnlySelected = false;
      this.setStatus("已恢复显示全部照片。", "info");
    },

    // 查看照片
    viewPhoto(photo) {
      this.currentPhoto = photo;
      this.showPhotoModal = true;
    },

    // 将base64转换为Blob对象
    dataURLtoBlob(dataUrl) {
      const arr = dataUrl.split(",");
      const mime = arr[0].match(/:(.*?);/)[1];
      const bstr = atob(arr[1]);
      let n = bstr.length;
      const u8arr = new Uint8Array(n);
      while (n--) {
        u8arr[n] = bstr.charCodeAt(n);
      }
      return new Blob([u8arr], { type: mime });
    },

    // 保存照片到后端
    async savePhotos() {
      if (this.capturedPhotos.length === 0) {
        this.setStatus("没有照片可以保存。", "error");
        return;
      }

      if (!this.operatorName || !this.scannerId) {
        this.setStatus("请填写操作员姓名和扫描仪编号。", "error");
        return;
      }

      const confirmSave = confirm(
        `确定要保存这 ${this.capturedPhotos.length} 张照片吗？`
      );
      if (!confirmSave) {
        this.setStatus("保存操作已取消。", "info");
        return;
      }

      this.saving = true;
      this.setStatus(
        `正在上传 ${this.capturedPhotos.length} 张照片...`,
        "info"
      );

      try {
        // 1. 创建扫描记录
        const recordData = {
          scannerId: this.scannerId,
          operatorName: this.operatorName,
          useDate: new Date().toISOString(),
          functionSelected: this.selectedBusinessType,
          cameraSelected: this.selectedCamera,
          remarks: this.remarks,
          businessNumber: this.businessNumber,
        };

        const { data: recordRes } = await http.post(
          "/scanner-record",
          recordData
        );

        if (recordRes.code !== 200) {
          throw new Error(recordRes.msg || "创建扫描记录失败");
        }

        const recordId = recordRes.data.id;

        // 2. 批量上传照片
        const formData = new FormData();
        formData.append("recordId", recordId);

        this.capturedPhotos.forEach((photo) => {
          const blob = this.dataURLtoBlob(photo.data);
          formData.append("files", blob, photo.fileName);
        });

        const { data: uploadRes } = await http.post(
          "/scanner-record/del/batch",
          formData,
          {
            headers: {
              "Content-Type": "multipart/form-data",
            },
          }
        );

        if (uploadRes.code !== 200) {
          throw new Error(uploadRes.msg || "上传照片失败");
        }

        this.setStatus(
          `成功保存 ${this.capturedPhotos.length} 张照片！`,
          "success"
        );

        // 保存成功后清空照片列表
        setTimeout(() => {
          this.capturedPhotos = [];
          this.remarks = "";
        }, 1000);
      } catch (err) {
        console.error("保存照片失败:", err);
        this.setStatus(`保存失败: ${err.message}`, "error");
      } finally {
        this.saving = false;
      }
    },

    // 加载历史记录
    async loadHistoryPhotos() {
      this.loadingHistory = true;
      this.setStatus("正在加载历史记录...", "info");

      try {
        const { data: res } = await http.get("/scanner-record/page", {
          params: {
            page: 1,
            size: 20,
            businessNumber: this.businessNumber || undefined,
          },
        });

        if (res.code === 200) {
          this.historyRecords = res.data.records || [];
          this.showHistoryModal = true;
          this.setStatus(
            `已加载 ${this.historyRecords.length} 条历史记录。`,
            "success"
          );
        } else {
          throw new Error(res.msg || "加载历史记录失败");
        }
      } catch (err) {
        console.error("加载历史记录失败:", err);
        this.setStatus(`加载失败: ${err.message}`, "error");
      } finally {
        this.loadingHistory = false;
      }
    },

    // 查看记录详情
    async viewRecordDetail(record) {
      try {
        const { data: res } = await http.get(`/scanner-record/${record.id}`);
        if (res.code === 200) {
          alert(
            `记录详情:\n操作员: ${res.data.operatorName}\n业务类型: ${res.data.functionSelected}\n扫描仪: ${res.data.scannerId}\n备注: ${res.data.remarks}`
          );
        } else {
          throw new Error(res.msg || "获取记录详情失败");
        }
      } catch (err) {
        console.error("获取记录详情失败:", err);
        this.setStatus(`获取详情失败: ${err.message}`, "error");
      }
    },

    // 删除记录
    async deleteRecord(recordId) {
      if (!confirm("确定要删除这条记录吗？")) {
        return;
      }

      try {
        const { data: res } = await http.delete(`/scanner-record/${recordId}`);
        if (res.code === 200) {
          this.historyRecords = this.historyRecords.filter(
            (record) => record.id !== recordId
          );
          this.setStatus("记录删除成功。", "success");
        } else {
          throw new Error(res.msg || "删除记录失败");
        }
      } catch (err) {
        console.error("删除记录失败:", err);
        this.setStatus(`删除失败: ${err.message}`, "error");
      }
    },

    // 设置状态消息
    setStatus(message, type = "info") {
      this.statusMessage = message;
      this.statusType = type;
      this.statusTitle =
        type === "error" ? "错误" : type === "success" ? "成功" : "提示";
    },

    // 格式化日期
    formatDate(dateString) {
      return new Date(dateString).toLocaleString("zh-CN");
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
      z-index: 9999; /* 设置一个较大的层级值 */
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
</style>
