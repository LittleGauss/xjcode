<template>
  <!-- 入库审批弹窗 -->
  <div
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    v-if="show"
  >
    <div
      class="bg-white rounded-lg shadow-xl p-6 w-full max-w-6xl max-h-[80vh] overflow-y-auto"
    >
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-lg font-semibold">待我审批的入库申请</h3>
        <button class="text-gray-500 hover:text-gray-700" @click="handleClose">
          <i class="fas fa-times"></i>
        </button>
      </div>
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-blue-50">
            <tr>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                入库单号
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                物品名称
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                规格型号
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                数量
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                申请人
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                申请部门
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                申请时间
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                入库说明
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                操作
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-100">
            <tr v-for="app in approvalApplications" :key="app.id">
              <td class="px-4 py-3 text-sm">
                {{ app.applyNo }}
              </td>
              <td class="px-4 py-3 text-sm">
                {{ app.itemName }}
                <span
                  v-if="app.categoryName"
                  class="text-xs text-gray-500 ml-1"
                >
                  ({{ app.categoryName }})
                </span>
              </td>
              <td class="px-4 py-3 text-sm">
                {{ app.spec || "-" }}
              </td>
              <td class="px-4 py-3 text-sm">
                {{ app.quantity }}
              </td>
              <td class="px-4 py-3 text-sm">
                {{ app.applicantNickname || app.applicantName }}
              </td>
              <td class="px-4 py-3 text-sm">
                {{ app.applicantDept }}
              </td>
              <td class="px-4 py-3 text-sm">
                {{ formatDateTime(app.applyTime) }}
              </td>
              <td class="px-4 py-3 text-sm">
                <span
                  v-if="app.remark"
                  class="text-sm text-gray-600"
                  :title="app.remark"
                >
                  {{
                    app.remark.length > 20
                      ? app.remark.substring(0, 20) + "..."
                      : app.remark
                  }}
                </span>
                <span v-else class="text-gray-400 text-sm">-</span>
              </td>
              <td class="px-4 py-3 text-sm">
                <div class="flex space-x-2">
                  <!-- 通过按钮 -->
                  <button
                    class="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded text-sm"
                    @click="
                      openApprovalDialog(
                        app.id,
                        'approve',
                        app.itemName,
                        app.quantity
                      )
                    "
                  >
                    通过
                  </button>
                  <!-- 驳回按钮 -->
                  <button
                    class="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded text-sm"
                    @click="
                      openApprovalDialog(
                        app.id,
                        'reject',
                        app.itemName,
                        app.quantity
                      )
                    "
                  >
                    驳回
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="approvalApplications.length == 0">
              <td colspan="9" class="px-4 py-8 text-center text-gray-500">
                暂无待审批的入库申请
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 通用审批对话框 -->
      <div
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
        v-if="showApprovalDialog"
      >
        <div class="bg-white rounded-lg shadow-xl p-6 w-full max-w-md">
          <div class="flex justify-between items-center mb-4">
            <h3 class="text-lg font-semibold">
              {{
                approvalAction === "approve" ? "批准入库申请" : "驳回入库申请"
              }}
            </h3>
            <button
              class="text-gray-500 hover:text-gray-700"
              @click="closeApprovalDialog"
            >
              <i class="fas fa-times"></i>
            </button>
          </div>
          <form @submit.prevent="handleSubmit">
            <div class="mb-4">
              <div class="p-3 bg-gray-50 rounded mb-3">
                <p class="text-sm font-medium">
                  申请物品：{{ selectedItemName }}
                </p>
                <p class="text-sm text-gray-500">
                  申请数量：{{ selectedQuantity }}
                </p>
              </div>

              <label class="block text-sm font-medium text-gray-700 mb-1">
                {{
                  approvalAction === "approve"
                    ? "审批意见（可选）"
                    : "驳回理由*"
                }}
              </label>
              <textarea
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                v-model="approvalReason"
                rows="3"
                :placeholder="
                  approvalAction == 'approve'
                    ? '请输入审批意见...'
                    : '请输入驳回理由...'
                "
                :required="approvalAction == 'reject'"
                maxlength="500"
              ></textarea>
              <div class="text-right text-sm text-gray-500 mt-1">
                {{ approvalReason.length }}/500
              </div>
            </div>

            <div class="flex space-x-3">
              <button
                class="flex-1 px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
                @click="closeApprovalDialog"
                type="button"
              >
                取消
              </button>
              <button
                class="flex-1 px-4 py-2"
                :class="
                  approvalAction == 'approve'
                    ? 'bg-green-600 hover:bg-green-700 text-white'
                    : 'bg-red-600 hover:bg-red-700 text-white'
                "
                type="submit"
              >
                {{ approvalAction == "approve" ? "确认批准" : "确认驳回" }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- 详情对话框 -->
      <div
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
        v-if="showDetailDialog"
      >
        <div
          class="bg-white rounded-lg shadow-xl p-6 w-full max-w-2xl max-h-[80vh] overflow-y-auto"
        >
          <div class="flex justify-between items-center mb-4">
            <h3 class="text-lg font-semibold">入库申请详情</h3>
            <button
              class="text-gray-500 hover:text-gray-700"
              @click="closeDetailDialog"
            >
              <i class="fas fa-times"></i>
            </button>
          </div>

          <div v-if="selectedDetail" class="space-y-4">
            <!-- 申请信息 -->
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >入库单号</label
                >
                <div class="p-2 bg-gray-50 rounded">
                  {{ selectedDetail.applyNo }}
                </div>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >状态</label
                >
                <div
                  class="p-2 rounded"
                  :class="{
                    'bg-yellow-100 text-yellow-800':
                      selectedDetail.status === 'PENDING',
                    'bg-green-100 text-green-800':
                      selectedDetail.status === 'APPROVED',
                    'bg-red-100 text-red-800':
                      selectedDetail.status === 'REJECTED',
                  }"
                >
                  {{ getStatusText(selectedDetail.status) }}
                </div>
              </div>
            </div>

            <!-- 申请人信息 -->
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >申请人</label
                >
                <div class="p-2 bg-gray-50 rounded">
                  {{
                    selectedDetail.applicantNickname ||
                    selectedDetail.applicantName
                  }}
                </div>
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >申请部门</label
                >
                <div class="p-2 bg-gray-50 rounded">
                  {{ selectedDetail.applicantDept }}
                </div>
              </div>
            </div>

            <!-- 物品信息 -->
            <div>
              <h4 class="text-md font-medium text-gray-900 mb-2">物品信息</h4>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1"
                    >物品名称</label
                  >
                  <div class="p-2 bg-gray-50 rounded">
                    {{ selectedDetail.itemName }}
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1"
                    >分类</label
                  >
                  <div class="p-2 bg-gray-50 rounded">
                    {{
                      selectedDetail.categoryName || selectedDetail.categoryId
                    }}
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1"
                    >规格型号</label
                  >
                  <div class="p-2 bg-gray-50 rounded">
                    {{ selectedDetail.spec || "-" }}
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1"
                    >品牌</label
                  >
                  <div class="p-2 bg-gray-50 rounded">
                    {{ selectedDetail.brand || "-" }}
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1"
                    >数量</label
                  >
                  <div class="p-2 bg-gray-50 rounded">
                    {{ selectedDetail.quantity }}
                    {{ selectedDetail.unit || "个" }}
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1"
                    >单价</label
                  >
                  <div class="p-2 bg-gray-50 rounded">
                    {{
                      selectedDetail.unitPrice
                        ? "¥" + selectedDetail.unitPrice.toFixed(2)
                        : "-"
                    }}
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1"
                    >供货商</label
                  >
                  <div class="p-2 bg-gray-50 rounded">
                    {{ selectedDetail.supplier || "-" }}
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1"
                    >入库日期</label
                  >
                  <div class="p-2 bg-gray-50 rounded">
                    {{ selectedDetail.purchaseDate || "-" }}
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1"
                    >过期日期</label
                  >
                  <div class="p-2 bg-gray-50 rounded">
                    {{ selectedDetail.expireDate || "-" }}
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1"
                    >预警值</label
                  >
                  <div class="p-2 bg-gray-50 rounded">
                    {{ selectedDetail.warningValue || "-" }}
                  </div>
                </div>
              </div>
            </div>

            <!-- 入库说明 -->
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1"
                >入库说明</label
              >
              <div class="p-3 bg-gray-50 rounded min-h-[60px]">
                {{ selectedDetail.remark || "无说明" }}
              </div>
            </div>

            <!-- 审批信息（如果已审批） -->
            <div
              v-if="
                selectedDetail.status === 'APPROVED' ||
                selectedDetail.status === 'REJECTED'
              "
            >
              <h4 class="text-md font-medium text-gray-900 mb-2">审批信息</h4>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1"
                    >审批人</label
                  >
                  <div class="p-2 bg-gray-50 rounded">
                    {{ selectedDetail.approverName }}
                  </div>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1"
                    >审批时间</label
                  >
                  <div class="p-2 bg-gray-50 rounded">
                    {{ formatDateTime(selectedDetail.approveTime) }}
                  </div>
                </div>
                <div class="col-span-2">
                  <label class="block text-sm font-medium text-gray-700 mb-1"
                    >审批意见</label
                  >
                  <div class="p-3 bg-gray-50 rounded min-h-[60px]">
                    {{ selectedDetail.approveRemark || "无意见" }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="mt-6 flex justify-end">
            <button
              class="px-4 py-2 bg-gray-300 hover:bg-gray-400 text-gray-800 rounded-md transition-colors"
              @click="closeDetailDialog"
            >
              关闭
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "StockInApprovalModal",
  props: {
    show: {
      type: Boolean,
      default: false,
    },
    approvalApplications: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      showApprovalDialog: false,
      showDetailDialog: false,
      approvalAction: "approve", // 'approve' 或 'reject'
      approvalReason: "",
      selectedApprovalId: null,
      selectedItemName: "",
      selectedQuantity: 0,
      selectedDetail: null,
    };
  },
  methods: {
    handleClose() {
      this.$emit("close");
      this.resetDialog();
    },

    getStatusText(status) {
      const statusMap = {
        PENDING: "待审批",
        APPROVED: "已批准",
        REJECTED: "已驳回",
      };
      return statusMap[status] || status;
    },

    formatDateTime(dateTime) {
      if (!dateTime) return "-";
      const date = new Date(dateTime);
      return date.toLocaleString();
    },

    openApprovalDialog(approvalId, action, itemName, quantity) {
      this.selectedApprovalId = approvalId;
      this.approvalAction = action;
      this.approvalReason = "";
      this.selectedItemName = itemName;
      this.selectedQuantity = quantity;
      this.showApprovalDialog = true;
    },

    closeApprovalDialog() {
      this.showApprovalDialog = false;
      this.approvalReason = "";
      this.selectedApprovalId = null;
      this.selectedItemName = "";
      this.selectedQuantity = 0;
    },
    closeDetailDialog() {
      this.showDetailDialog = false;
      this.selectedDetail = null;
    },

    resetDialog() {
      this.showApprovalDialog = false;
      this.showDetailDialog = false;
      this.approvalAction = "approve";
      this.approvalReason = "";
      this.selectedApprovalId = null;
      this.selectedItemName = "";
      this.selectedQuantity = 0;
      this.selectedDetail = null;
    },

    handleSubmit() {
      if (this.approvalAction === "reject" && !this.approvalReason.trim()) {
        this.$message.warning("请输入驳回理由");
        return;
      }

      const data = {
        approvalId: this.selectedApprovalId,
        action: this.approvalAction,
        remark: this.approvalReason.trim(),
      };

      this.$emit("submit", data);
      this.closeApprovalDialog();
    },
  },
};
</script>

<style scoped>
/* 保持原有样式，可以根据需要调整 */
.fixed {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.bg-white {
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 状态标签样式 */
.bg-yellow-100 {
  background-color: #fef3c7;
}
.text-yellow-800 {
  color: #92400e;
}

.bg-green-100 {
  background-color: #d1fae5;
}
.text-green-800 {
  color: #065f46;
}

.bg-red-100 {
  background-color: #fee2e2;
}
.text-red-800 {
  color: #991b1b;
}

/* 表格样式优化 */
table.min-w-full {
  th,
  td {
    padding: 12px 16px;
    vertical-align: middle;
  }

  th {
    background-color: #f8fafc;
    color: #1e40af;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }

  tbody tr:hover {
    background-color: #f9fafb;
  }
}
</style>
