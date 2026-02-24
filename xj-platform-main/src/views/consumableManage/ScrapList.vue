<template>
  <div class="scrap-page">
    <!-- 页头组件 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @navigate="handleNavigate"
      @logout="handleLogout"
    />

    <main class="scrap-main">
      <div class="scrap-content">
        <!-- 页面标题和切换按钮 -->
        <div class="mb-6 flex items-center justify-between">
          <div>
            <el-button
              :type="listType === 'my' ? 'primary' : 'default'"
              @click="switchList('my')"
            >
              我的申请
            </el-button>
            <el-button
              :type="listType === 'todo' ? 'primary' : 'default'"
              @click="switchList('todo')"
            >
              待我审批
            </el-button>
          </div>
          <div>
            <button
              class="px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded-lg"
              @click="$router.go(-1)"
            >
              返回
            </button>
          </div>
        </div>

        <!-- 权限提示 -->
        <div
          v-if="!hasPermission"
          class="bg-red-50 border border-red-200 rounded-lg p-6 text-center"
        >
          <i class="fas fa-exclamation-triangle text-red-500 text-4xl mb-4"></i>
          <h2 class="text-xl font-bold text-red-700 mb-2">权限不足</h2>
          <p class="text-gray-600">您没有权限访问报废申请页面</p>
        </div>

        <!-- 报废列表 -->
        <div v-else>
          <!-- 我的申请列表 -->
          <div v-if="listType === 'my'">
            <h3 class="text-lg font-semibold mb-4">我的报废申请</h3>

            <el-table
              :data="myScrapList"
              style="width: 100%"
              v-loading="loading"
            >
              <el-table-column label="报废物品" width="250">
                <template slot-scope="scope">
                  <div v-if="scope.row.items && scope.row.items.length > 0">
                    <div class="flex items-center">
                      <span class="text-sm font-medium truncate">{{
                        getFirstItem(scope.row).goodsName
                      }}</span>
                      <span class="text-xs text-gray-500 ml-2 whitespace-nowrap"
                        >×{{ getFirstItem(scope.row).quantity }}</span
                      >
                    </div>
                    <div
                      v-if="getFirstItem(scope.row).specification"
                      class="text-xs text-gray-400 truncate"
                    >
                      {{ getFirstItem(scope.row).specification }}
                    </div>
                    <div class="text-xs text-blue-600 mt-1">
                      ¥{{ formatPrice(getFirstItem(scope.row).totalPrice) }}
                    </div>
                  </div>
                  <div v-else class="text-gray-400 text-sm">无物品信息</div>
                </template>
              </el-table-column>

              <el-table-column
                prop="applyUserNickname"
                label="申请人"
                width="120"
              />
              <el-table-column prop="applyDept" label="部门" width="150" />
              <el-table-column label="状态" width="120">
                <template slot-scope="scope">
                  <el-tag :type="getStatusType(scope.row.status)" size="small">
                    {{ statusMap[scope.row.status] || scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column
                prop="reason"
                label="报废原因"
                width="200"
                :show-overflow-tooltip="true"
              />
              <el-table-column
                prop="processMethod"
                label="处理方式"
                width="100"
              >
                <template slot-scope="scope">
                  {{
                    processMethodMap[scope.row.processMethod] ||
                    scope.row.processMethod
                  }}
                </template>
              </el-table-column>
              <el-table-column
                prop="assigneeNickName"
                label="审核人"
                width="120"
              />
              <el-table-column prop="applyTime" label="申请时间" width="180">
                <template slot-scope="scope">
                  {{ formatDate(scope.row.applyTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template slot-scope="scope">
                  <el-button
                    v-if="scope.row.status === 'REVIEW'"
                    size="mini"
                    type="warning"
                    @click="withdrawScrap(scope.row.id)"
                  >
                    撤回
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 无数据提示 -->
            <div
              v-if="myScrapList.length === 0 && !loading"
              class="text-center py-10"
            >
              <i class="fas fa-inbox text-gray-400 text-4xl mb-4"></i>
              <p class="text-gray-600">暂无报废申请记录</p>
            </div>
          </div>

          <!-- 待我审批列表 -->
          <div v-else-if="listType === 'todo'">
            <div class="flex justify-between items-center mb-4">
              <h3 class="text-lg font-semibold">待我审批的报废申请</h3>
              <button
                class="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg"
                @click="loadToApproveScrapList"
              >
                <i class="fas fa-sync-alt mr-2"></i>刷新
              </button>
            </div>

            <el-table
              :data="toApproveScrapList"
              style="width: 100%"
              v-loading="loading"
              :key="'todo-table-' + tableKey"
            >
              <el-table-column label="报废物品" width="250">
                <template slot-scope="scope">
                  <div v-if="scope.row.items && scope.row.items.length > 0">
                    <div class="flex items-center">
                      <span class="text-sm font-medium truncate">{{
                        getFirstItem(scope.row).goodsName
                      }}</span>
                      <span class="text-xs text-gray-500 ml-2 whitespace-nowrap"
                        >×{{ getFirstItem(scope.row).quantity }}</span
                      >
                    </div>
                    <div
                      v-if="getFirstItem(scope.row).specification"
                      class="text-xs text-gray-400 truncate"
                    >
                      {{ getFirstItem(scope.row).specification }}
                    </div>
                    <div class="text-xs text-blue-600 mt-1">
                      ¥{{ formatPrice(getFirstItem(scope.row).totalPrice) }}
                    </div>
                  </div>
                  <div v-else class="text-gray-400 text-sm">无物品信息</div>
                </template>
              </el-table-column>

              <el-table-column
                prop="applyUserNickname"
                label="申请人"
                width="120"
              />
              <el-table-column prop="applyDept" label="部门" width="150" />
              <el-table-column label="状态" width="120">
                <template slot-scope="scope">
                  <el-tag :type="getStatusType(scope.row.status)" size="small">
                    {{ statusMap[scope.row.status] || scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column
                prop="reason"
                label="报废原因"
                width="200"
                :show-overflow-tooltip="true"
              />
              <el-table-column label="处理方式" width="100">
                <template slot-scope="scope">
                  {{ processMethodMap[scope.row.processMethod] }}
                </template>
              </el-table-column>
            
              <el-table-column label="审批阶段" width="120">
                <template slot-scope="scope">
                  {{ getApprovalStage(scope.row) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template slot-scope="scope">
                 
                  <el-button
                    v-if="canReview(scope.row)"
                    size="mini"
                    type="primary"
                    @click="openReviewModal(scope.row)"
                    :disabled="checkingPermission"
                  >
                    审核
                  </el-button>
                  <el-button
                    v-if="canApprove(scope.row)"
                    size="mini"
                    type="success"
                    @click="openApproveModal(scope.row)"
                    :disabled="checkingPermission"
                  >
                    审批
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 无数据提示 -->
            <div
              v-if="toApproveScrapList.length === 0 && !loading"
              class="text-center py-10"
            >
              <i class="fas fa-check-circle text-green-500 text-4xl mb-4"></i>
              <p class="text-gray-600">暂无待审批的报废申请</p>
            </div>
          </div>
        </div>
      </div>
    </main>


    <!-- 审核弹窗（后保部） -->
    <el-dialog title="后保部审核" :visible.sync="showReviewModal" width="500px">
      <el-form :model="reviewForm" ref="reviewFormRef" :rules="reviewRules">
        <el-form-item label="审核结果" prop="status">
          <el-radio-group v-model="reviewForm.status">
            <el-radio label="REVIEWED">通过</el-radio>
            <el-radio label="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item
          v-if="reviewForm.status === 'REVIEWED'"
          label="分管领导审批人"
          prop="nextApproverId"
          :rules="[
            { required: true, message: '请选择分管领导', trigger: 'change' },
          ]"
        >
          <el-select
            v-model="reviewForm.nextApproverId"
            placeholder="请选择分管领导"
            style="width: 100%"
            filterable
            :disabled="checkingPermission"
          >
            <el-option
              v-for="leader in viceLeaders"
              :key="leader.id"
              :label="
                leader.nickname +
                (leader.departmentName ? ` (${leader.departmentName})` : '')
              "
              :value="leader.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="审核意见" prop="remark">
          <el-input
            type="textarea"
            v-model="reviewForm.remark"
            placeholder="请输入审核意见"
            rows="3"
            :disabled="checkingPermission"
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showReviewModal = false" :disabled="reviewLoading"
          >取消</el-button
        >
        <el-button
          type="primary"
          @click="submitReview"
          :loading="reviewLoading"
          :disabled="checkingPermission"
        >
          提交
        </el-button>
      </div>
    </el-dialog>

    <!-- 审批弹窗（分管领导） -->
    <el-dialog
      title="分管领导审批"
      :visible.sync="showApproveModal"
      width="500px"
    >
      <el-form :model="approveForm" ref="approveFormRef" :rules="approveRules">
        <el-form-item label="审批结果" prop="status">
          <el-radio-group v-model="approveForm.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="审批意见" prop="remark">
          <el-input
            type="textarea"
            v-model="approveForm.remark"
            placeholder="请输入审批意见"
            rows="3"
            :disabled="checkingPermission"
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showApproveModal = false" :disabled="approveLoading"
          >取消</el-button
        >
        <el-button
          type="primary"
          @click="submitApprove"
          :loading="approveLoading"
          :disabled="checkingPermission"
        >
          提交
        </el-button>
      </div>
    </el-dialog>

    <!-- 页脚组件 -->
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>

<script>
import { scrapApi } from "@/api/inventory";
import HeaderComponent from "@/components/HeaderComponent.vue";
import FooterComponent from "@/components/FooterComponent.vue";
import { getUserToken, removeUserToken, removeToken } from "@/utils/auth";

export default {
  name: "ScrapListPage",
  components: {
    HeaderComponent,
    FooterComponent,
  },
  data() {
    return {
      tableKey: 0,
      login_user: null,
      userRoles: [],

      // 列表类型
      listType: "my", // my: 我的申请, todo: 待我审批

      // 加载状态
      loading: false,
      checkingPermission: false,

      // 数据列表
      myScrapList: [],
      toApproveScrapList: [],

      // 模态框控制

      showReviewModal: false,
      showApproveModal: false,

      // 当前选中的报废单
      currentReviewScrap: null,
      currentApproveScrap: null,

      // 审核表单
      reviewForm: {
        scrapId: null,
        status: "REVIEWED",
        remark: "",
        nextApproverId: "",
        nextApproverName: "",
      },
      reviewLoading: false,
      reviewRules: {
        remark: [
          { required: true, message: "请输入审核意见", trigger: "blur" },
        ],
      },

      // 审批表单
      approveForm: {
        scrapId: null,
        status: "APPROVED",
        remark: "",
      },
      approveLoading: false,
      approveRules: {
        remark: [
          { required: true, message: "请输入审批意见", trigger: "blur" },
        ],
      },

      // 分管领导列表
      viceLeaders: [],

      // 状态映射
      statusMap: {
        DRAFT: "已撤回",
        REVIEW: "后保部审核中",
        REVIEWED: "待分管领导审批",
        APPROVED: "审批通过",
        REJECTED: "后保部审核驳回",
        FINAL_REJECTED: "分管领导审批驳回",
      },

      // 处理方式映射
      processMethodMap: {
        RECYCLE: "回收",
        DESTROY: "销毁",
      },
    };
  },
  computed: {
    hasPermission() {
      const roleCodes = this.userRoles.map((r) => r.code || "");
      return (
        roleCodes.includes("ROLE_LOGISTICS") ||
        roleCodes.includes("ROLE_LEADER") ||
        roleCodes.includes("ROLE_DEPT_SPECIAL") ||
        roleCodes.includes("ROLE_VICE_LEADER")
      );
    },
  },
  created() {
    this.getUserInfo();
    this.loadMyScrapList();
  },
  methods: {
    getUserInfo() {
      const user = getUserToken();
      if (user && user.user) {
        this.login_user = user.user;
        this.userRoles = user.roles || [];
      } else {
        this.$router.push("/login");
      }
    },

    switchList(type) {
      this.listType = type;
      if (type === "my") {
        this.loadMyScrapList();
      } else if (type === "todo") {
        this.loadToApproveScrapList();
      }
    },

    // 加载我的报废申请
    async loadMyScrapList() {
      this.loading = true;
      try {
        const response = await scrapApi.getMyScrap();
        this.myScrapList = Array.isArray(response) ? response : [];
        console.log("我的报废申请数据:", this.myScrapList);
      } catch (error) {
        console.error("加载我的报废申请失败:", error);
        this.myScrapList = [];
        this.$message.error("加载失败: " + (error.message || "未知错误"));
      } finally {
        this.loading = false;
      }
    },

    // 加载待我审批的报废申请
    async loadToApproveScrapList() {
      this.loading = true;
      try {
        const response = await scrapApi.getToApproveScrap();
        this.toApproveScrapList = Array.isArray(response) ? response : [];
        console.log("待审批列表数据:", this.toApproveScrapList);
      } catch (error) {
        console.error("加载待审批报废申请失败:", error);
        console.error("错误详情:", error.response?.data);
        this.toApproveScrapList = [];
        this.$message.error("加载失败: " + (error.message || "未知错误"));
      } finally {
        this.loading = false;
      }
    },

    // 加载分管领导列表
    async loadViceLeaders() {
      try {
        const response = await scrapApi.getViceLeaders();
        this.viceLeaders = Array.isArray(response) ? response : [];
      } catch (error) {
        console.error("加载分管领导列表失败:", error);
        this.viceLeaders = [];
      }
    },

    getStatusType(status) {
      const map = {
        DRAFT: "info",
        REVIEW: "warning",
        REVIEWED: "primary",
        APPROVED: "success",
        REJECTED: "danger",
        FINAL_REJECTED: "danger",
      };
      return map[status] || "info";
    },

    getApprovalStage(scrap) {
      if (scrap.status === "REVIEW") {
        return "后保部审核";
      } else if (scrap.status === "REVIEWED") {
        return "分管领导审批";
      }
      return "已完成";
    },

    canReview(scrap) {
      const roleCodes = this.userRoles.map((r) => r.code || "");
      const isReviewer =
        roleCodes.includes("ROLE_LOGISTICS") ||
        roleCodes.includes("ROLE_DEPT_SPECIAL");

      const currentUserId = this.login_user?.id;

      return (
        isReviewer &&
        scrap.status === "REVIEW" &&
        scrap.assigneeId === currentUserId
      );
    },

    canApprove(scrap) {
      const roleCodes = this.userRoles.map((r) => r.code || "");
      const isLeader =
        roleCodes.includes("ROLE_LEADER") ||
        roleCodes.includes("ROLE_VICE_LEADER");

      const currentUserId = this.login_user?.id;

      return (
        isLeader &&
        scrap.status === "REVIEWED" &&
        scrap.nextApproverId === currentUserId
      );
    },

    getFirstItem(scrap) {
      if (scrap.items && scrap.items.length > 0) {
        return scrap.items[0];
      }
      return {
        goodsName: "无物品",
        quantity: 0,
        totalPrice: 0,
        specification: "",
      };
    },

    async openReviewModal(scrap) {
      try {
        this.checkingPermission = true;

        // 权限验证
        if (!this.canReview(scrap)) {
          this.$message.error("您没有权限审核此申请");
          return;
        }

        await this.loadViceLeaders();
        this.currentReviewScrap = scrap;
        this.reviewForm = {
          scrapId: scrap.id,
          status: "REVIEWED",
          remark: "",
          nextApproverId: "",
          nextApproverName: "",
        };
        this.showReviewModal = true;
      } catch (error) {
        console.error("打开审核模态框失败:", error);
        this.$message.error("打开审核页面失败");
      } finally {
        this.checkingPermission = false;
      }
    },

    async openApproveModal(scrap) {
      try {
        this.checkingPermission = true;

        // 权限验证
        if (!this.canApprove(scrap)) {
          this.$message.error("您没有权限审批此申请");
          return;
        }

        this.currentApproveScrap = scrap;
        this.approveForm = {
          scrapId: scrap.id,
          status: "APPROVED",
          remark: "",
        };
        this.showApproveModal = true;
      } catch (error) {
        console.error("打开审批模态框失败:", error);
        this.$message.error("打开审批页面失败");
      } finally {
        this.checkingPermission = false;
      }
    },

    // 提交审核
    async submitReview() {
      this.$refs.reviewFormRef.validate(async (valid) => {
        if (!valid) {
          console.log("表单验证失败");
          return;
        }

        // 详细调试
        console.log("当前用户ID:", this.login_user?.id);
        console.log("当前用户名:", this.login_user?.username);
        console.log("报废单ID:", this.reviewForm.scrapId);
        console.log("审核状态:", this.reviewForm.status);
        console.log("选择的nextApproverId:", this.reviewForm.nextApproverId);
        console.log(
          "选择的nextApproverName:",
          this.reviewForm.nextApproverName
        );
        console.log("remark:", this.reviewForm.remark);

        if (this.reviewForm.status === "REVIEWED") {
          if (!this.reviewForm.nextApproverId) {
            this.$message.error("审核通过必须选择分管领导");
            return;
          }

          const selectedLeader = this.viceLeaders.find(
            (leader) => leader.id === this.reviewForm.nextApproverId
          );
          if (selectedLeader) {
            this.reviewForm.nextApproverName = selectedLeader.username;
          }
        }

        this.reviewLoading = true;
        try {
          // 构建请求参数
          const requestData = {
            scrapId: this.reviewForm.scrapId,
            status: this.reviewForm.status,
            remark: this.reviewForm.remark,
          };

          // 只有审核通过时才传递下一级审批人信息
          if (this.reviewForm.status === "REVIEWED") {
            requestData.nextApproverId = this.reviewForm.nextApproverId;
            requestData.nextApproverName = this.reviewForm.nextApproverName;
          }

          console.log(
            "提交的完整请求数据:",
            JSON.stringify(requestData, null, 2)
          );

          await scrapApi.reviewScrap(requestData);
          this.$message.success("审核成功");
          this.showReviewModal = false;
          this.loadToApproveScrapList();
          this.loadMyScrapList();
        } catch (error) {
          console.error("审核失败详细错误:", error);
          console.error("错误响应数据:", error.response?.data);
          this.$message.error(
            "审核失败: " +
              (error.response?.data?.message || error.message || "未知错误")
          );
        } finally {
          this.reviewLoading = false;
        }
      });
    },

    // 提交审批
    async submitApprove() {
      this.$refs.approveFormRef.validate(async (valid) => {
        if (!valid) {
          this.$message.error("请完善表单信息");
          return;
        }

        // 权限二次验证
        if (!this.canApprove(this.currentApproveScrap)) {
          this.$message.error("您没有权限审批此申请");
          return;
        }

        this.approveLoading = true;
        try {
          await scrapApi.approveScrap(this.approveForm);
          this.$message.success("审批成功");
          this.showApproveModal = false;
          this.loadToApproveScrapList(); // 刷新待审批列表
          this.loadMyScrapList(); // 刷新我的申请列表
        } catch (error) {
          console.error("审批失败:", error);
          this.$message.error("审批失败: " + (error.message || "未知错误"));
        } finally {
          this.approveLoading = false;
        }
      });
    },

    async withdrawScrap(scrapId) {
      try {
        await this.$confirm("确定要撤回该报废申请吗？", "确认撤回", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });

        await scrapApi.withdrawScrap(scrapId);
        this.$message.success("撤回成功");
        this.loadMyScrapList();
      } catch (error) {
        if (error !== "cancel") {
          console.error("撤回失败:", error);
          this.$message.error("撤回失败: " + (error.message || "未知错误"));
        }
      }
    },

    formatDate(dateTime) {
      if (!dateTime) return "";
      try {
        return new Date(dateTime).toLocaleString();
      } catch (e) {
        return dateTime;
      }
    },

    formatPrice(price) {
      if (!price) return "0.00";
      try {
        return parseFloat(price).toFixed(2);
      } catch (e) {
        return "0.00";
      }
    },

    handleNavigate(routeName) {
      this.$router.push({ name: routeName });
    },

    handleLogout() {
      removeUserToken();
      removeToken();
      this.login_user = null;
      this.userRoles = [];
      this.$router.push("/login");
    },
  },
};
</script>

<style scoped>
.scrap-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.scrap-main {
  flex: 1;
  padding: 20px;
  background-color: #f9fafb;
}
.scrap-content {
  max-width: 1200px;
  margin: 0 auto;
}

/* 详情弹窗样式 */
.el-dialog__body {
  max-height: 70vh;
  overflow-y: auto;
}
</style>
