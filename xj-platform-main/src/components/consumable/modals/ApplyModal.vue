<template>
  <!-- 领用申请模态框 -->
  <div
    class="fixed inset-0 bg-black bg-opacity-50 z-50 overflow-y-auto"
    v-if="show"
    @click.self="handleClose"
  >
    <div class="min-h-screen flex items-start justify-center p-4 pt-20 pb-10">
      <div
        class="bg-white rounded-lg shadow-xl p-6 w-full max-w-md transform transition-all"
      >
        <div
          class="flex justify-between items-center mb-4 sticky top-0 bg-white pb-2"
        >
          <h3 class="text-lg font-semibold">申请领用</h3>
          <button
            class="text-gray-500 hover:text-gray-700 transition-colors"
            @click="handleClose"
          >
            <i class="fas fa-times"></i>
          </button>
        </div>

        <form
          class="space-y-4 overflow-y-auto max-h-[calc(80vh-100px)]"
          @submit.prevent="submit"
        >
          <input v-model="form.itemId" type="hidden" />
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >物品名称</label
            >
            <input
              class="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-100"
              v-model="form.itemName"
              readonly
              type="text"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >当前库存</label
            >
            <input
              class="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-100"
              v-model="form.itemStock"
              readonly
              type="text"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >申请数量 <span class="text-red-500">*</span></label
            >
            <input
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              v-model.number="form.quantity"
              min="1"
              :max="form.itemStock"
              required
              type="number"
              @input="validateQuantity"
            />
            <p class="text-xs text-gray-500 mt-1">
              最大可申请：{{ form.itemStock }}
            </p>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >申请部门 <span class="text-red-500">*</span></label
            >
            <select
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              v-model="form.department"
              required
            >
              <option value="">请选择部门</option>
              <option
                v-for="dept in departmentList"
                :key="dept.id"
                :value="dept.id"
              >
                {{ dept.departmentName || dept.name }}
              </option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >申请用途</label
            >
            <textarea
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
              v-model="form.purpose"
              rows="3"
              placeholder="请输入申请用途..."
              maxlength="500"
            ></textarea>
            <p class="text-xs text-gray-500 text-right mt-1">
              {{ form.purpose.length }}/500
            </p>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >审核人 <span class="text-red-500">*</span></label
            >
            <select
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              v-model="form.assigneeId"
              required
            >
              <option value="">请选择审核人</option>
              <option
                v-for="approval in firstApproverOptions"
                :key="approval.id"
                :value="approval.id"
              >
                {{ approval.nickname }}
              </option>
            </select>
          </div>
          <div class="pt-2 sticky bottom-0 bg-white pb-2">
            <button
              class="w-full bg-blue-600 hover:bg-blue-700 text-white py-3 px-4 rounded-md transition-colors flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed"
              type="submit"
              :disabled="submitting"
            >
              <span v-if="submitting" class="flex items-center">
                <i class="fas fa-spinner fa-spin mr-2"></i>
                提交中...
              </span>
              <span v-else class="flex items-center">
                <i class="fas fa-paper-plane mr-2"></i>
                提交申请
              </span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "ApplyModal",
  props: {
    show: {
      type: Boolean,
      default: false,
    },
    item: {
      type: Object,
      default: () => ({}),
    },
    departmentList: {
      type: Array,
      default: () => [],
    },
    firstApproverOptions: {
      type: Array,
      default: () => [],
    },
    defaultDepartmentId: {
      type: [String, Number],
      default: "",
    },
    login_user: {
      type: Object,
      default: () => ({}),
    },
    categoryMap: {
      type: Object,
      default: () => ({}),
    },
  },
  data() {
    return {
      submitting: false,
      form: {
        itemId: null,
        itemName: "",
        itemStock: 0,
        quantity: 1,
        department: "",
        purpose: "",
        assigneeId: "",
        categoryId: null,
        supplier: "",
      },
    };
  },
  watch: {
    show(newVal) {
      if (newVal) {
        this.resetForm();
        this.loadItemData();
        // 防止背景滚动
        document.body.style.overflow = "hidden";
      } else {
        this.resetForm();
        document.body.style.overflow = "";
      }
    },
    defaultDepartmentId(newVal) {
      if (newVal) {
        this.form.department = newVal;
      }
    },
  },
  mounted() {
    if (this.show) {
      this.resetForm();
      this.loadItemData();
    }
  },
  beforeUnmount() {
    // 确保组件销毁时恢复滚动
    document.body.style.overflow = "";
  },
  methods: {
    handleClose() {
      this.$emit("close");
      this.resetForm();
    },
    resetForm() {
      this.form = {
        itemId: null,
        itemName: "",
        itemStock: 0,
        quantity: 1,
        department: this.defaultDepartmentId,
        purpose: "",
        assigneeId: "",
        categoryId: null,
      };
    },
    loadItemData() {
      if (this.item && this.item.id) {
        this.form.itemId = this.item.id;
        this.form.itemName = this.item.name || "未命名物品";
        this.form.itemStock = this.item.quantity || 0;
        this.form.categoryId = this.item.categoryId;
        this.form.supplier = this.item.supplier || "";
      }
    },
    validateQuantity() {
      if (this.form.quantity < 1) {
        this.form.quantity = 1;
      }
      if (this.form.quantity > this.form.itemStock) {
        this.form.quantity = this.form.itemStock;
        this.$message.warning(`申请数量不能超过库存量`);
      }
    },
    validateForm() {
      if (!this.form.quantity || this.form.quantity < 1) {
        this.$message.warning("申请数量必须大于0");
        return false;
      }
      if (this.form.quantity > this.form.itemStock) {
        this.$message.warning("申请数量不能超过库存量");
        return false;
      }
      if (!this.form.department) {
        this.$message.warning("请选择申请部门");
        return false;
      }
      if (!this.form.assigneeId) {
        this.$message.warning("请选择审核人");
        return false;
      }
      return true;
    },
    getConsumableType() {
      // 根据分类ID判断耗材类型
      const categoryId = this.form.categoryId;
      if (categoryId == 2) {
        return "electronic";
      } else if (categoryId == 3) {
        return "experimental";
      } else if (categoryId == 5) {
        //新增危化品分类
        return "hazardous";
      }
      return "general";
    },
    async submit() {
      if (!this.validateForm()) {
        return;
      }

      this.submitting = true;
      try {
        // 获取选中的部门名称
        const selectedDept = this.departmentList.find(
          (dept) => dept.id === this.form.department
        );
        const deptName = selectedDept
          ? selectedDept.departmentName || selectedDept.name
          : this.form.department;

        // 获取选中审核人信息
        const selectedApprover = this.firstApproverOptions.find(
          (approver) => approver.id === this.form.assigneeId
        );

        // 准备提交给后端的数据
        const applicationData = {
          itemId: this.form.itemId,
          itemName: this.form.itemName,
          supplier: this.form.supplier,
          applicantId: this.login_user?.id,
          applicantName: this.login_user?.name || this.login_user?.username,
          applicantDept: deptName,
          consumableType: this.getConsumableType(),
          quantity: this.form.quantity,
          purpose: this.form.purpose,
          assigneeId: this.form.assigneeId,
          assigneeName: selectedApprover ? selectedApprover.username : "",
        };

        // 触发父组件的提交事件
        this.$emit("submit", applicationData);
        this.handleClose(); // 关闭并重置表单
      } catch (error) {
        console.error("提交申请失败:", error);
        this.$message.error("提交失败，请重试");
      } finally {
        this.submitting = false;
      }
    },
  },
};
</script>

<style scoped>
/* 模态框动画效果 */
.fixed {
  animation: fadeIn 0.3s ease-in-out;
}

.bg-white {
  animation: slideUp 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
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

/* 自定义滚动条样式 */
form::-webkit-scrollbar {
  width: 6px;
}

form::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

form::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

form::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
