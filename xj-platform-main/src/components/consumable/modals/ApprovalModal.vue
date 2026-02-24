<template>
  <!-- 领用审批模态框 -->
  <div
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    v-if="show"
  >
    <div
      class="bg-white rounded-lg shadow-xl p-6 w-full max-w-6xl max-h-[80vh] overflow-y-auto"
    >
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-lg font-semibold">
          待我审批的领用申请 -
          {{ isSuperAdmin ? "超级管理员模式" : "普通审批模式" }}
        </h3>
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
                申请人
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                申请物品
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                申请数量
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                申请部门
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                申请用途
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                申请时间
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                当前状态
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                任务名称
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                审核人
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                操作
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-100">
            <tr v-for="app in approvalApplications" :key="app.taskId">
              <td class="px-4 py-3 text-sm">
                {{ app.consumableApption.applicantNickname }}
              </td>
              <td class="px-4 py-3 text-sm">
                {{ app.consumableApption.itemName }}
              </td>
              <td class="px-4 py-3 text-sm">
                {{ app.consumableApption.quantity }}
              </td>
              <td class="px-4 py-3 text-sm">
                {{ app.consumableApption.applicantDept }}
              </td>
              <td class="px-4 py-3 text-sm">
                <span
                  v-if="app.consumableApption.purpose"
                  class="text-sm text-gray-600"
                  :title="app.consumableApption.purpose"
                >
                  {{
                    app.consumableApption.purpose.length > 15
                      ? app.consumableApption.purpose.substring(0, 15) + "..."
                      : app.consumableApption.purpose
                  }}
                </span>
                <span v-else class="text-gray-400 text-sm">-</span>
              </td>
              <td class="px-4 py-3 text-sm">
                {{ app.consumableApption.applyTime }}
              </td>
              <td class="px-4 py-3 text-sm">
                {{ getStatusText(app.consumableApption.finalStatus) }}
              </td>
              <td class="px-4 py-3 text-sm">{{ app.taskName }}</td>
              <td class="px-4 py-3 text-sm">
                {{ app.consumableApption.assigneeNickname }}
              </td>
              <td class="px-4 py-3 text-sm">
                <div class="flex space-x-2">
                  <!-- 通过按钮 -->
                  <button
                    class="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded text-sm"
                    @click="
                      openApprovalDialog(
                        app.taskId,
                        app.applicationId,
                        'approve',
                        (firstOrfinal =
                          app.consumableApption.finalStatus ==
                          'pending_final_approval'),
                        app.consumableApption.quantity
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
                        app.taskId,
                        app.applicationId,
                        'reject',
                        (firstOrfinal =
                          app.consumableApption.finalStatus ==
                          'pending_final_approval'),
                        app.consumableApption.quantity
                      )
                    "
                  >
                    驳回
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="approvalApplications.length == 0">
              <td
                :colspan="isSuperAdmin ? 9 : 8"
                class="px-4 py-8 text-center text-gray-500"
              >
                暂无待审批申请
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
              {{ approvalAction === "approve" ? "审批通过" : "审批驳回" }}
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
              <label class="block text-sm font-medium text-gray-700 mb-1">
                {{
                  approvalAction === "approve"
                    ? "通过理由（可选）"
                    : "驳回理由*"
                }}
              </label>
              <textarea
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                v-model="approvalReason"
                rows="3"
                :placeholder="
                  approvalAction == 'approve'
                    ? '请输入通过理由...'
                    : '请输入驳回理由...'
                "
                :required="approvalAction == 'reject'"
              ></textarea>
            </div>
            <!-- 添加最终审批人选择框（仅在通过时显示） -->
            <div
              class="mb-4"
              v-if="approvalAction == 'approve' && !firstOrfinal"
            >
              <label class="block text-sm font-medium text-gray-700 mb-1">
                下一个审批人 <span class="text-red-500">*</span>
              </label>
              <select
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                v-model="logisticsApprover"
                required
              >
                <option value="">请选择下一个审批人</option>
                <option
                  v-for="approver in logisticsApproverOptions"
                  :key="approver.id"
                  :value="approver.id"
                >
                  {{ approver.nickname }}
                </option>
              </select>
            </div>
            <!-- 添加实际领用数量输入框 -->
            <div
              class="mb-4"
              v-if="approvalAction == 'approve' && firstOrfinal"
            >
              <label class="block text-sm font-medium text-gray-700 mb-1">
                实际领用数量 <span class="text-red-500">*必填</span>
              </label>
              <input
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                v-model.number="actualDistributeQuantity"
                @input="validateActualQuantity"
                type="number"
                min="0"
                :max="maxQuantity"
                required
              />
              <p class="text-xs text-gray-500 mt-1">
                默认值为申请数量: {{ maxQuantity }}
              </p>
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
                {{ approvalAction == "approve" ? "确认通过" : "确认驳回" }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "ApprovalModal",
  props: {
    show: {
      type: Boolean,
      default: false,
    },
    approvalApplications: {
      type: Array,
      default: () => [],
    },
    logisticsApproverOptions: {
      type: Array,
      default: () => [],
    },
    isSuperAdmin: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      showApprovalDialog: false,
      approvalAction: "approve", // 'approve' 或 'reject'
      approvalReason: "",
      currentTaskId: null,
      applicationId: null,
      firstOrfinal: false, //是否最终审批 false就是一级 true就是二级
      actualDistributeQuantity: 0, //领用数量，默认就是申请值
      logisticsApprover: "",
      maxQuantity: 0,
    };
  },
  methods: {
    handleClose() {
      this.$emit("close");
    },

    getStatusText(status) {
      const statusMap = {
        pending: "待提交",
        pending_first_approval: "等待一级审批",
        pending_final_approval: "等待二级审批",
        first_approved: "一级审批通过",
        first_rejected: "一级审批驳回",
        final_rejected: "二级审批驳回",
        issued: "已发放",
      };
      return statusMap[status] || status;
    },

    openApprovalDialog(taskId, applicationId, action, firstOrfinal, quantity) {
      this.currentTaskId = taskId;
      this.approvalAction = action;
      this.approvalReason = "";
      this.applicationId = applicationId;
      this.firstOrfinal = firstOrfinal;
      this.showApprovalDialog = true;
      this.actualDistributeQuantity = quantity;
      this.maxQuantity = quantity;
      this.logisticsApprover = "";
    },

    closeApprovalDialog() {
      this.showApprovalDialog = false;
      this.approvalReason = "";
      this.actualDistributeQuantity = 0;
      this.logisticsApprover = "";
    },

    validateActualQuantity(event) {
      const value = Number(event.target.value);
      if (value > this.maxQuantity) {
        this.$message.warning(
          `实际领用数量不能超过申请数量 ${this.maxQuantity}`
        );
        this.actualDistributeQuantity = this.maxQuantity;
      }
    },

    handleSubmit() {
      if (this.approvalAction === "reject" && !this.approvalReason.trim()) {
        this.$message.warning("请输入驳回理由");
        return;
      }

      if (
        this.approvalAction === "approve" &&
        this.firstOrfinal &&
        !this.actualDistributeQuantity
      ) {
        this.$message.warning("请输入实际领用数量");
        return;
      }

      if (
        this.approvalAction === "approve" &&
        !this.firstOrfinal &&
        !this.logisticsApprover
      ) {
        this.$message.error("请选择审核人！");
        return;
      }

      // 获取选中的审批人对象
      const selectedApprover = this.logisticsApproverOptions.find(
        (approver) => approver.id == this.logisticsApprover
      );

      const data = {
        taskId: this.currentTaskId,
        applicationId: this.applicationId,
        action: this.approvalAction,
        reason: this.approvalReason,
        firstOrfinal: this.firstOrfinal,
        logisticsApprover: selectedApprover?.id,
        actualDistributeQuantity: this.actualDistributeQuantity,
      };

      this.$emit("submit", data);
      this.closeApprovalDialog();
    },
  },
};
</script>
