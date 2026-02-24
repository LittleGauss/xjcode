<template>
  <!-- 我的申请模态框 -->
  <div
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    v-if="show"
  >
    <div
      class="bg-white rounded-lg shadow-xl p-6 w-full max-w-4xl max-h-[80vh] overflow-y-auto"
    >
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-lg font-semibold">我的领用申请</h3>
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
                物品名称
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
                操作
              </th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-100">
            <tr v-for="app in applications" :key="app.id">
              <td class="px-4 py-3 text-sm">{{ app.itemName }}</td>
              <td class="px-4 py-3 text-sm">{{ app.quantity }}</td>
              <td class="px-4 py-3 text-sm">{{ app.applicantDept }}</td>
              <td class="px-4 py-3 text-sm">{{ app.purpose || "-" }}</td>
              <td class="px-4 py-3 text-sm">{{ app.applyTime }}</td>
              <td class="px-4 py-3 text-sm">
                {{ getStatusText(app.finalStatus) }}
              </td>
              <td class="px-4 py-3 text-sm">
                <button
                  v-if="app.finalStatus === 'pending_first_approval'"
                  class="text-red-600 hover:text-red-800"
                  @click="handleWithdraw(app.id)"
                >
                  撤回
                </button>
                <span v-else class="text-gray-400">-</span>
              </td>
            </tr>
            <tr v-if="applications.length === 0">
              <td colspan="7" class="px-4 py-8 text-center text-gray-500">
                暂无申请记录
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
  name: "MyApplicationsModal",
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

    handleWithdraw(applicationId) {
      this.$emit("withdraw", applicationId);
    },
  },
};
</script>

<style lang="scss" scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

.modal-container {
  background: white;
  border-radius: 0.75rem;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  width: 90%;
  max-width: 80rem;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;

  .modal-title {
    font-size: 1.25rem;
    font-weight: 600;
    color: #111827;
    margin: 0;
  }

  .modal-close {
    color: #6b7280;
    background: none;
    border: none;
    cursor: pointer;
    padding: 0.25rem;
    border-radius: 0.25rem;

    &:hover {
      color: #374151;
      background: #f3f4f6;
    }
  }
}

.modal-body {
  flex: 1;
  overflow: hidden;
  padding: 1.5rem;
}

.table-container {
  height: 100%;
  overflow-y: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;

  thead {
    background: #eff6ff;

    th {
      padding: 0.75rem 1rem;
      text-align: left;
      font-weight: 600;
      color: #1e40af;
      text-transform: uppercase;
      font-size: 0.75rem;
      border-bottom: 1px solid #dbeafe;
      white-space: nowrap;
    }
  }

  tbody {
    tr {
      border-bottom: 1px solid #f3f4f6;
      transition: background-color 0.2s;

      &:hover {
        background-color: #f9fafb;
      }

      td {
        padding: 0.75rem 1rem;
        color: #4b5563;

        &.purpose-cell {
          max-width: 200px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }

  .empty-cell {
    padding: 3rem;
    text-align: center;
    color: #9ca3af;

    i {
      font-size: 2rem;
      margin-bottom: 0.75rem;
      display: block;
      opacity: 0.5;
    }
  }
}

.status-pending {
  color: #f59e0b;
  background: #fef3c7;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-waiting {
  color: #3b82f6;
  background: #dbeafe;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-approved {
  color: #10b981;
  background: #d1fae5;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-rejected {
  color: #ef4444;
  background: #fee2e2;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-issued {
  color: #8b5cf6;
  background: #ede9fe;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-default {
  color: #6b7280;
  background: #f3f4f6;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  font-weight: 500;
}

.btn-withdraw {
  color: #ef4444;
  background: none;
  border: 1px solid #ef4444;
  padding: 0.25rem 0.75rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #ef4444;
    color: white;
  }
}

.no-action {
  color: #9ca3af;
  font-size: 0.75rem;
}
</style>
