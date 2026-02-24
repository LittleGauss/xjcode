<template>
  <!-- 我的入库申请弹窗 -->
  <div
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    v-if="show"
    @click.self="$emit('close')"
  >
    <div
      class="bg-white rounded-lg shadow-xl p-6 w-full max-w-4xl max-h-[80vh] overflow-y-auto"
    >
      <div class="flex justify-between items-center mb-6">
        <h3 class="text-lg font-semibold">我的入库申请</h3>
        <button
          class="text-gray-500 hover:text-gray-700"
          @click="$emit('close')"
        >
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
                数量
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                申请时间
              </th>
              <th
                class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
              >
                状态
              </th>
         
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-100">
            <tr v-for="app in applications" :key="app.id">
              <td class="px-4 py-3 text-sm">
                {{ app.applyNo }}
              </td>
              <td class="px-4 py-3 text-sm">
                {{ app.itemName }}
              </td>
              <td class="px-4 py-3 text-sm">
                {{ app.quantity }}
              </td>
              <td class="px-4 py-3 text-sm">
                {{ formatDateTime(app.applyTime) }}
              </td>
              <td class="px-4 py-3 text-sm">
                <span
                  :class="{
                    'bg-yellow-100 text-yellow-800': app.status === 'PENDING',
                    'bg-green-100 text-green-800': app.status === 'APPROVED',
                    'bg-red-100 text-red-800': app.status === 'REJECTED',
                  }"
                  class="px-2 py-1 rounded-full text-xs font-medium"
                >
                  {{ getStatusText(app.status) }}
                </span>
              </td>
            </tr>

            <tr v-if="applications.length === 0">
              <td colspan="6" class="px-4 py-8 text-center text-gray-500">
                暂无入库申请记录
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "MyStockInApplicationsModal",
  props: {
    show: {
      type: Boolean,
      default: false,
    },
    applications: {
      type: Array,
      default: () => [],
    },
  },
  methods: {
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
  },
};
</script>
