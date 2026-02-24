<!-- src/components/consumable/modals/ScrapModal.vue -->
<template>
  <div
    v-if="show"
    class="fixed inset-0 bg-black bg-opacity-50 z-50 overflow-y-auto"
    @click.self="close"
  >
    <div class="min-h-screen flex items-start justify-center p-4 pt-20 pb-10">
      <div
        class="bg-white rounded-lg shadow-xl p-6 w-full max-w-md transform transition-all"
      >
        <div
          class="flex justify-between items-center mb-4 sticky top-0 bg-white pb-2"
        >
          <h3 class="text-lg font-semibold">提交报废申请</h3>
          <button
            class="text-gray-500 hover:text-gray-700 transition-colors"
            @click="close"
          >
            <i class="fas fa-times"></i>
          </button>
        </div>

        <form
          class="space-y-4 overflow-y-auto max-h-[calc(80vh-100px)]"
          @submit.prevent="handleSubmit"
        >
          <!-- 报废物品信息 -->
          <div class="p-3 bg-gray-50 rounded">
            <div class="flex justify-between items-center mb-2">
              <span class="text-sm font-medium text-gray-700">报废物品</span>
              <span class="text-sm text-gray-500"
                >库存：{{ selectedItem.quantity }}
                {{ selectedItem.unit || "个" }}</span
              >
            </div>
            <div class="grid grid-cols-2 gap-3 text-sm">
              <div>
                <span class="text-gray-600">名称：</span>
                <span class="font-medium">{{ selectedItem.name }}</span>
              </div>
              <div>
                <span class="text-gray-600">分类：</span>
                <span>{{ selectedItem.category }}</span>
              </div>
              <div>
                <span class="text-gray-600">规格型号：</span>
                <span>{{ selectedItem.spec || "-" }}</span>
              </div>
              <div>
                <span class="text-gray-600">单价：</span>
                <span>¥{{ (selectedItem.unitPrice || 0).toFixed(2) }}</span>
              </div>
            </div>
          </div>

          <!-- 报废数量 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              报废数量 <span class="text-red-500">*</span>
            </label>
            <div class="flex items-center space-x-3">
              <input
                type="number"
                v-model="form.quantity"
                :max="selectedItem.quantity"
                min="1"
                required
                class="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="请输入报废数量"
                @input="validateQuantity"
              />
              <span class="text-sm text-gray-500 whitespace-nowrap"
                >最大：{{ selectedItem.quantity }}</span
              >
            </div>
          </div>

          <!-- 处理方式 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              处理方式 <span class="text-red-500">*</span>
            </label>
            <div class="flex space-x-4">
              <label class="inline-flex items-center">
                <input
                  type="radio"
                  v-model="form.processMethod"
                  value="RECYCLE"
                  class="mr-2"
                />
                <span>回收</span>
              </label>
              <label class="inline-flex items-center">
                <input
                  type="radio"
                  v-model="form.processMethod"
                  value="DESTROY"
                  class="mr-2"
                />
                <span>销毁</span>
              </label>
            </div>
          </div>

          <!-- 申请部门 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              申请部门 <span class="text-red-500">*</span>
            </label>
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

          <!-- 申请人信息（只读显示） -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              申请人
            </label>
            <input
              class="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-100"
              :value="login_user.nickname"
              readonly
              type="text"
            />
          </div>

          <!-- 报废原因 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              报废原因 <span class="text-red-500">*</span>
            </label>
            <textarea
              v-model="form.reason"
              rows="3"
              required
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
              placeholder="请详细描述报废原因（如：损坏、过期、技术淘汰等）"
              maxlength="500"
            ></textarea>
            <div class="text-right text-sm text-gray-500 mt-1">
              {{ form.reason.length }}/500
            </div>
          </div>

          <!-- 审核人 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              审核人 <span class="text-red-500">*</span>
            </label>
            <select
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              v-model="form.assigneeId"
              required
            >
              <option value="">请选择审核人</option>
              <option
                v-for="approval in logisticsApproverOptions"
                :key="approval.id"
                :value="approval.id"
              >
                {{ approval.nickname || approval.username || approval.name }}
              </option>
            </select>
          </div>

          <!-- 提交按钮 -->
          <div class="pt-2 sticky bottom-0 bg-white pb-2">
            <button
              type="submit"
              :disabled="submitting"
              class="w-full bg-red-600 hover:bg-red-700 text-white py-3 px-4 rounded-md transition-colors flex items-center justify-center disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="submitting" class="flex items-center">
                <i class="fas fa-spinner fa-spin mr-2"></i>提交中...
              </span>
              <span v-else class="flex items-center">
                <i class="fas fa-trash-alt mr-2"></i>
                提交报废申请
              </span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { scrapApi } from "@/api/inventory";

export default {
  name: "ScrapModal",
  props: {
    show: {
      type: Boolean,
      default: false,
    },
    selectedItem: {
      type: Object,
      required: true,
    },
    login_user: {
      type: Object,
      required: true,
    },
    // 新增props：部门列表、审核人列表
    departmentList: {
      type: Array,
      default: () => [],
    },
    logisticsApproverOptions: {
      type: Array,
      default: () => [],
    },
  },
  emits: ["close", "success"],
  data() {
    return {
      form: {
        quantity: 1,
        processMethod: "RECYCLE",
        reason: "",
        department: "",
        assigneeId: "",
      },
      submitting: false,
    };
  },
  watch: {
    show(newVal) {
      if (newVal) {
        this.resetForm();
        this.setDefaultValues();
        // 防止背景滚动
        document.body.style.overflow = "hidden";
      } else {
        this.resetForm();
        document.body.style.overflow = "";
      }
    },
  },
  mounted() {
    if (this.show) {
      this.resetForm();
      this.setDefaultValues();
    }
  },
  beforeUnmount() {
    document.body.style.overflow = "";
  },
  methods: {
    resetForm() {
      this.form = {
        quantity: 1,
        processMethod: "RECYCLE",
        reason: "",
        department: "",
        assigneeId: "",
      };
      this.submitting = false;
    },
    setDefaultValues() {
      // 设置默认部门（当前登录用户的部门）
      if (this.login_user.departmentId) {
        this.form.department = this.login_user.departmentId;
      }
    },
    validateQuantity() {
      if (this.form.quantity < 1) {
        this.form.quantity = 1;
      }
      if (this.form.quantity > this.selectedItem.quantity) {
        this.form.quantity = this.selectedItem.quantity;
        this.$message.warning(`报废数量不能超过库存量`);
      }
    },
    validateForm() {
      if (this.form.quantity < 1) {
        this.$message.warning("报废数量必须大于0");
        return false;
      }

      if (this.form.quantity > this.selectedItem.quantity) {
        this.$message.warning(
          `报废数量不能超过库存数量（${this.selectedItem.quantity}）`
        );
        return false;
      }

      if (!this.form.reason.trim()) {
        this.$message.warning("请填写报废原因");
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
    async handleSubmit() {
      try {
        if (!this.validateForm()) {
          return;
        }

        this.submitting = true;

        // 获取选中的部门信息
        const selectedDept = this.departmentList.find(
          (dept) => dept.id == this.form.department
        );
        const deptName = selectedDept
          ? selectedDept.departmentName || selectedDept.name
          : "";

        // 获取选中的审核人信息
        const selectedApprover = this.logisticsApproverOptions.find(
          (approver) => approver.id == this.form.assigneeId
        );

        // 构建报废申请数据
        const scrapData = {
          reason: this.form.reason,
          processMethod: this.form.processMethod,
          applyUserId: this.login_user.id,
          applyUserName: this.login_user.username || this.login_user.name,
          applyDept: deptName,
          assigneeId: this.form.assigneeId,
          assigneeName: selectedApprover ? selectedApprover.username : "",
          items: [
            {
              goodsId: this.selectedItem.id,
              goodsName: this.selectedItem.name,
              specification: this.selectedItem.spec || "",
              quantity: this.form.quantity,
              unitPrice: this.selectedItem.unitPrice || 0,
              totalPrice:
                (this.selectedItem.unitPrice || 0) * this.form.quantity,
            },
          ],
        };

        console.log("提交报废申请数据:", scrapData); // 添加调试日志

        // 调用报废API
        const response = await scrapApi.submitScrap(scrapData);

        console.log("API返回结果:", response); // 添加调试日志

        // 修改这里：根据实际返回结构判断
        if (
          response &&
          (response.code === 200 || response.code === "200" || !response.code)
        ) {
          // 如果返回有code且为200，或者没有code（可能被拦截器处理过了）
          this.$message.success("报废申请提交成功，等待审核");
          this.$emit("success", response.data || response);
          this.close();
        } else {
          // 如果有code但不是200
          this.$message.error(response.message || "提交失败");
        }
      } catch (error) {
        console.error("提交报废申请失败:", error);
        console.error("错误详情:", error.response); // 添加详细错误日志

        // 显示更详细的错误信息
        if (
          error.response &&
          error.response.data &&
          error.response.data.message
        ) {
          this.$message.error("提交失败：" + error.response.data.message);
        } else {
          this.$message.error("提交失败：" + (error.message || "网络错误"));
        }
      } finally {
        this.submitting = false;
      }
    },
    close() {
      this.$emit("close");
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
