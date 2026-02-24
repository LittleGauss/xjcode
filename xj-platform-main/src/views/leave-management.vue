<template>
  <div class="home-container">
    <!-- 使用通用页头 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @logout="handleLogout"
      @navigate="handleNavigate"
    />

    <div class="bg-gray-100 min-h-screen flex flex-col">
      <!-- 主内容区 -->
      <div class="w-full flex-1">
        <el-menu
          :default-active="$route.path"
          mode="horizontal"
          router
          background-color="#545c64"
          text-color="#fff"
          active-text-color="#ffd04b"
        >
          <el-menu-item index="/leave/apply">请假申请</el-menu-item>
          <el-menu-item index="/leave/todo">待办审批</el-menu-item>
          <el-menu-item index="/leave/history">我的记录</el-menu-item>
          <el-menu-item index="/leave/report">部门维度统计报表</el-menu-item>
        </el-menu>
        <div class="bg-white rounded-lg shadow p-6 h-full">
          <!-- 子路由内容：在请销假模块内部切换页面时，导航保持可见 -->
          <div class="leave-router-view">
            <router-view />
          </div>
        </div>
      </div>

      <!-- 详情模态框 -->
      <div
        v-if="showDetailModal"
        class="fixed inset-0 bg-gray-600 bg-opacity-50 flex items-center justify-center z-50"
        @click="closeModal"
      >
        <div class="bg-white rounded-lg shadow-xl w-full max-w-2xl" @click.stop>
          <div class="flex justify-between items-center p-4 border-b">
            <h3 class="text-lg font-semibold">{{ detailTitle }}</h3>
            <button
              @click="closeModal"
              class="text-gray-500 hover:text-gray-700"
            >
              <i class="fas fa-times"></i>
            </button>
          </div>
          <div class="p-6" v-if="currentDetail">
            <div class="space-y-4">
              <!-- 审批流程 -->
              <div class="bg-gray-50 p-4 rounded-lg">
                <h4 class="text-sm font-medium text-gray-500 mb-2">审批流程</h4>
                <div class="flex justify-between">
                  <div
                    v-for="step in currentDetail.approvalSteps"
                    :key="step.name"
                    class="flex flex-col items-center w-1/5"
                  >
                    <div
                      class="w-10 h-10 rounded-full flex items-center justify-center mb-2"
                      :class="getStepClass(step.status)"
                    >
                      <i class="fas" :class="getStepIcon(step.status)"></i>
                    </div>
                    <span
                      class="text-xs text-center"
                      :class="getStepTextClass(step.status)"
                    >
                      {{ step.name }}
                    </span>
                  </div>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-4">
                <div>
                  <p class="text-sm text-gray-500">申请类型</p>
                  <p class="font-medium">{{ currentDetail.leaveType }}</p>
                </div>
                <div>
                  <p class="text-sm text-gray-500">申请人姓名</p>
                  <p class="font-medium">{{ currentDetail.employeeName }}</p>
                </div>
                <div>
                  <p class="text-sm text-gray-500">申请时间</p>
                  <p class="font-medium">{{ currentDetail.createdAt }}</p>
                </div>
                <div>
                  <p class="text-sm text-gray-500">请假天数</p>
                  <p class="font-medium">{{ currentDetail.days }}</p>
                </div>
                <div v-if="currentDetail.destination">
                  <p class="text-sm text-gray-500">目的地</p>
                  <p class="font-medium">{{ currentDetail.destination }}</p>
                </div>
                <div v-if="currentDetail.transport">
                  <p class="text-sm text-gray-500">交通工具</p>
                  <p class="font-medium">{{ currentDetail.transport }}</p>
                </div>
                <div>
                  <p class="text-sm text-gray-500">所在部门</p>
                  <p class="font-medium">{{ currentDetail.department }}</p>
                </div>
                <div>
                  <p class="text-sm text-gray-500">当前状态</p>
                  <p
                    class="font-medium"
                    :class="{
                      'text-green-600': currentDetail.status === '已确认',
                      'text-yellow-600': currentDetail.status === '待确认',
                      'text-gray-600': !['已确认', '待确认'].includes(
                        currentDetail.status
                      ),
                    }"
                  >
                    {{ currentDetail.status }}
                  </p>
                </div>
              </div>

              <div>
                <p class="text-sm text-gray-500">申请原因</p>
                <p class="font-medium">{{ currentDetail.reason }}</p>
              </div>
              <div>
                <p class="text-sm text-gray-500">附件</p>
                <p class="font-medium">{{ currentDetail.attachments }}</p>
              </div>

              <!-- 操作按钮 -->
              <div
                v-if="currentDetail.status === '已同意'"
                class="mt-4 flex justify-end"
              >
                <button
                  @click="confirmCancel(currentDetail.id)"
                  class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md"
                >
                  <i class="fas fa-check mr-1"></i>确认销假
                </button>
              </div>

              <div
                v-if="currentDetail.status === '待审批'"
                class="mt-6 flex justify-end space-x-3"
              >
                <!-- 同意 -->
                <button
                  @click="approveApplication(currentDetail.id)"
                  class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md"
                >
                  <i class="fas fa-check-circle mr-1"></i>同意
                </button>
                <!-- 拒绝 -->
                <button
                  @click="rejectApplication(currentDetail.id)"
                  class="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-md"
                >
                  <i class="fas fa-times-circle mr-1"></i>拒绝
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 销假确认模态框 -->
      <div
        v-if="showCancelModal"
        class="fixed inset-0 bg-gray-600 bg-opacity-50 flex items-center justify-center z-50"
        @click="closeCancelModal"
      >
        <div class="bg-white rounded-lg shadow-xl w-full max-w-md" @click.stop>
          <div class="flex justify-between items-center p-4 border-b">
            <h3 class="text-lg font-semibold">销假确认</h3>
            <button
              @click="closeCancelModal"
              class="text-gray-500 hover:text-gray-700"
            >
              <i class="fas fa-times"></i>
            </button>
          </div>
          <div class="p-6">
            <form @submit.prevent="submitCancelConfirm" class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >销假时间</label
                >
                <input
                  v-model="cancelForm.cancelTime"
                  type="datetime-local"
                  class="w-full border border-gray-300 rounded-md p-2"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >实际返回时间</label
                >
                <input
                  v-model="cancelForm.actualReturnTime"
                  type="datetime-local"
                  class="w-full border border-gray-300 rounded-md p-2"
                  required
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >留言</label
                >
                <textarea
                  v-model="cancelForm.note"
                  class="w-full border border-gray-300 rounded-md p-2"
                  rows="3"
                ></textarea>
              </div>
              <button
                type="submit"
                class="w-full bg-blue-500 hover:bg-blue-600 text-white py-2 px-4 rounded-md"
              >
                确认销假
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
    <!-- 使用通用页脚 -->
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>

<script>
import { reactive, ref, computed } from "@vue/composition-api";
import { leaveApi } from "@/api/leave";
import { removeToken, getUserToken, removeUserToken } from "@/utils/auth";
import HeaderComponent from "@/components/HeaderComponent.vue";
import FooterComponent from "@/components/FooterComponent.vue";

export default {
  name: "LeaveManagementSystem",
  components: {
    HeaderComponent,
    FooterComponent,
  },

  data() {
    return {};
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
      this.$router.push({ name: routeName });
    },
  },
  created() {
    // 在组件创建后安全地获取用户信息
    const storedUserInfo = getUserToken();
    if (storedUserInfo && storedUserInfo.user) {
      this.login_user = storedUserInfo.user;
      this.userRoles = storedUserInfo.roles;
    } else {
      this.$message.warning("您尚未登录，请先登录！");
      // 如果未登录，跳转到登录页面
      this.$router.push("/login");
    }
    // 从主页进入请销假模块时默认落到“请假申请”页
    if (this.$route.path === "/leave") {
      this.$router.replace("/leave/apply");
    }
  },
  /**
   * Vue 组件的 setup 函数
   *
   * @returns 返回组件所需的状态、方法和计算属性
   */
  setup() {
    // —— 页面状态 ——
    const activeMenu = ref("my-application");
    const showDetailModal = ref(false);
    const showCancelModal = ref(false);
    const currentDetail = ref(null);
    const cancelApplicationId = ref(""); // —— 列表数据（改为服务端返回） ——

    const myApplications = ref([]);
    const approveApplications = ref([]);
    const cancelApplications = ref([]);

    // —— 表单数据 ——
    const leaveForm = reactive({
      employeeName: "",
      leaveType: "business",
      transport: "飞机",
      department: "",
      destination: "",
      startTime: "",
      endTime: "",
      days: "",
      reason: "",
      files: [],
    });

    const cancelForm = reactive({
      cancelTime: "",
      actualReturnTime: "",
      note: "",
    }); // 详情弹窗标题随菜单变化

    const detailTitle = computed(() => {
      if (activeMenu.value === "approve-application") return "审批详情";
      if (activeMenu.value === "cancel-leave") return "销假详情";
      return "申请详情";
    });

    // —— 工具类（保留你原有样式逻辑） ——
    const getStatusClass = (status) => {
      const classes = {
        审批中: "bg-blue-100 text-blue-800",
        待审批: "bg-yellow-100 text-yellow-800",
        已批准: "bg-green-100 text-green-800",
        已审批: "bg-green-100 text-green-800",
        已销假: "bg-green-100 text-green-800",
        待确认: "bg-yellow-100 text-yellow-800",
        已拒绝: "bg-red-100 text-red-800",
        已同意: "bg-blue-100 text-blue-800",
      };
      return classes[status] || "bg-gray-100 text-gray-800";
    };

    const getStepClass = (status) => {
      const classes = {
        completed: "bg-blue-500 text-white",
        current: "bg-blue-200 text-blue-800",
        pending: "bg-gray-200 text-gray-500",
        rejected: "bg-red-500 text-white",
      };
      return classes[status] || "bg-gray-200 text-gray-500";
    };

    const getStepIcon = (status) => {
      const icons = {
        completed: "fa-check",
        current: "fa-spinner fa-pulse",
        pending: "fa-clock",
        rejected: "fa-times",
      };
      return icons[status] || "fa-clock";
    };

    const getStepTextClass = (status) => {
      const classes = {
        completed: "text-blue-600 font-medium",
        current: "text-blue-600",
        pending: "text-gray-500",
        rejected: "text-red-600",
      };
      return classes[status] || "text-gray-500";
    };

    // 统一成功判断
    const isOk = (res) =>
      res && (res.success === true || res.code === 200 || res.code === "200");

    //待我审批列表
    const loadApproveApplications = async () => {
      const { data: res } = await leaveApi.getApproveApplications();
      if (isOk(res)) approveApplications.value = res.data || [];
    };
    //销假列表
    const loadCancelApplications = async () => {
      const { data: res } = await leaveApi.getCancelApplications();
      if (isOk(res)) cancelApplications.value = res.data || [];
    };

    // —— 详情加载 ——
    const loadDetail = async (id) => {
      const { data: res } = await leaveApi.getApplicationDetail(id);
      console.log("[loadDetail]", res);
      if (isOk(res)) {
        currentDetail.value = res.data;
        showDetailModal.value = true;
      } else {
        currentDetail.value = null;
      }
    };

    // —— 导航切换：切页即加载对应列表 ——
    const changeMenu = async (menuId) => {
      activeMenu.value = menuId;
      if (menuId === "my-application");
      if (menuId === "approve-application") await loadApproveApplications();
      if (menuId === "cancel-leave") await loadCancelApplications();
    };

    // —— 详情弹窗 ——
    const showDetail = (id) => loadDetail(id);
    const closeModal = () => {
      showDetailModal.value = false;
      currentDetail.value = null;
    };

    // —— 销假确认弹窗 ——
    const confirmCancel = (id) => {
      cancelApplicationId.value = id;
      showCancelModal.value = true;
    };
    const closeCancelModal = () => {
      showCancelModal.value = false;
      cancelApplicationId.value = "";
      cancelForm.cancelTime = "";
      cancelForm.actualReturnTime = "";
      cancelForm.note = "";
    };

    const submitCancelConfirm = async () => {
      const id = cancelApplicationId.value;
      try {
        const { data: res } = await leaveApi.confirmCancel(id, {
          cancelTime: cancelForm.cancelTime,
          actualReturnTime: cancelForm.actualReturnTime,
          note: cancelForm.note,
        });
        if (res?.success) {
          // 刷新销假列表与详情
          await loadCancelApplications();
          await loadDetail(id);
        }
      } finally {
        closeCancelModal();
      }
    };

    // —— 审批（同意/拒绝） ——
    const approveApplication = async (id) => {
      const { data: res } = await leaveApi.approveApplication(id);
      if (res?.success) {
        await loadApproveApplications();
        closeModal();
      }
    };
    const rejectApplication = async (id) => {
      const { data: res } = await leaveApi.rejectApplication(id);
      if (res?.success) {
        await loadApproveApplications();
        closeModal();
      }
    };

    // —— 类型联动 & 天数计算 & 附件 ——（保留）
    const onLeaveTypeChange = () => {
      leaveForm.transport = leaveForm.leaveType === "business" ? "飞机" : "";
    };
    const calculateDays = () => {
      if (leaveForm.startTime && leaveForm.endTime) {
        const start = new Date(leaveForm.startTime);
        const end = new Date(leaveForm.endTime);
        const diffTime = Math.abs(end - start);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        leaveForm.days = diffDays + "天";
      }
    };
    const handleFileUpload = (event) => {
      leaveForm.files = Array.from(event.target.files);
    };

    // —— 提交请假/出差 ——（改为走接口）
    const submitLeaveForm = async () => {
      const payload = {
        employeeName: leaveForm.employeeName,
        leaveType: leaveForm.leaveType,
        transport:
          leaveForm.leaveType === "business" ? leaveForm.transport : "",
        department: leaveForm.department,
        destination: leaveForm.destination,
        startTime: leaveForm.startTime,
        endTime: leaveForm.endTime,
        days: leaveForm.days,
        reason: leaveForm.reason,
        attachments: leaveForm.files?.length ? "已上传" : "",
      };
      const { data: res } = await leaveApi.createApplication(payload);
      console.log("[submitLeaveForm]", res);
      if (isOk(res)) {
        alert(res.message || "申请已提交！");
        Object.assign(leaveForm, {
          employeeName: "",
          leaveType: "business",
          transport: "飞机",
          department: "",
          destination: "",
          startTime: "",
          endTime: "",
          days: "",
          reason: "",
          files: [],
        });
        await changeMenu("my-application");
      }
    };

    return {
      // 状态
      activeMenu,
      showDetailModal,
      showCancelModal,
      currentDetail,
      leaveForm,
      cancelForm, // 文案
      detailTitle, // 列表
      myApplications,
      approveApplications,
      cancelApplications,
      // 方法
      changeMenu,
      getStatusClass,
      getStepClass,
      getStepIcon,
      getStepTextClass,
      showDetail,
      closeModal,
      confirmCancel,
      closeCancelModal,
      submitCancelConfirm,
      approveApplication,
      rejectApplication,
      onLeaveTypeChange,
      calculateDays,
      handleFileUpload,
      submitLeaveForm,
    };
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

  .sidebar-item.active {
    background-color: #3b82f6;
    color: white;
  }

  .sidebar-item:hover:not(.active) {
    background-color: #e5e7eb;
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

/* 确保字体图标正常显示 */
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css");
</style>
