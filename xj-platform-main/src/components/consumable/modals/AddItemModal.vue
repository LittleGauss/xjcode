<template>
  <!-- 新增物品模态框（自适应屏幕优化版） -->
  <div
    class="fixed inset-0 bg-black bg-opacity-50 flex items-start justify-center z-[1000] p-4"
    v-if="show"
    @click.self="handleClose"
    style="overflow: visible !important"
  >
    <!-- 模态框容器：强制开启滚动 + 下移 + 固定最大高度 -->
    <div
      class="bg-white rounded-lg shadow-xl p-5 w-full max-w-2xl min-w-[300px] mx-auto mt-20 mb-8 relative z-[1001]"
      style="
        max-height: 85vh !important;
        overflow-y: auto !important;
        box-sizing: border-box;
      "
    >
      <!-- 粘性标题栏（单独容器，避免滚动时遮挡） -->
      <div
        class="flex justify-between items-center mb-4 bg-white pb-2 border-b border-gray-100 z-10"
      >
        <div>
          <h3 class="text-lg font-semibold">{{ modalTitle }}</h3>
          <p v-if="needApproval" class="text-sm text-blue-600 mt-1">
            <i class="fas fa-info-circle mr-1"></i>
            此申请需后保部审批通过后方可入库
          </p>
        </div>
        <button
          class="text-gray-500 hover:text-gray-700 transition-colors"
          @click="handleClose"
        >
          <i class="fas fa-times"></i>
        </button>
      </div>

      <!-- 表单容器：独立高度，确保滚动区域明确 -->
      <div style="padding-bottom: 10px">
        <form
          class="grid grid-cols-1 md:grid-cols-2 gap-4 pt-2"
          @submit.prevent="submit"
        >
          <!-- 物品名称（占满整行） -->
          <div class="md:col-span-2">
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >物品名称 <span class="text-red-500">*</span></label
            >
            <input
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow"
              v-model="form.name"
              required
              type="text"
              placeholder="请输入物品名称"
            />
          </div>

          <!-- 型号规格 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >型号规格</label
            >
            <input
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow"
              v-model="form.spec"
              type="text"
              placeholder="请输入型号规格"
            />
          </div>

          <!-- 品牌 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >品牌</label
            >
            <input
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow"
              v-model="form.brand"
              type="text"
              placeholder="请输入品牌"
            />
          </div>

          <!-- 供货商字段 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
              供货商
            </label>
            <input
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              v-model="form.supplier"
              type="text"
              placeholder="请输入供货商名称"
            />
          </div>

          <!-- 单价(元) -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >单价(元)</label
            >
            <input
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow"
              v-model.number="form.unitPrice"
              min="0"
              step="0.01"
              type="number"
              placeholder="0.00"
            />
          </div>

          <!-- 过期日期 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >过期日期</label
            >
            <input
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow"
              v-model="form.expireDate"
              type="date"
              :min="form.date"
            />
          </div>

          <!-- 分类 -->
          <div class="md:col-span-2">
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >分类 <span class="text-red-500">*</span></label
            >
            <select
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow"
              v-model="form.category"
              required
              @change="handleCategoryChange"
            >
              <option value="">请选择分类</option>
              <option
                v-for="cat in availableCategories"
                :key="cat"
                :value="cat"
              >
                {{ cat }}
              </option>
            </select>
          </div>

          <!-- 数量 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >数量 <span class="text-red-500">*</span></label
            >
            <input
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow"
              v-model.number="form.quantity"
              min="1"
              required
              type="number"
              placeholder="1"
            />
          </div>

          <!-- 入库日期 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >入库日期 <span class="text-red-500">*</span></label
            >
            <input
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow"
              v-model="form.date"
              required
              type="date"
            />
          </div>

          <!-- 预警值 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >预警值</label
            >
            <input
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow"
              v-model.number="form.warningValue"
              min="0"
              placeholder="留空则不设置预警"
              type="number"
            />
          </div>

          <!-- 单位 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >单位</label
            >
            <select
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow"
              v-model="form.unit"
            >
              <option value="个">个</option>
              <option value="台">台</option>
              <option value="套">套</option>
              <option value="件">件</option>
              <option value="盒">盒</option>
              <option value="箱">箱</option>
              <option value="米">米</option>
              <option value="卷">卷</option>
            </select>
          </div>

          <!-- 后保部审批人（当需要审批时显示） -->
          <div v-if="showApproverField" class="md:col-span-2">
            <label class="block text-sm font-medium text-gray-700 mb-1">
              后保部审批人 <span class="text-red-500">*</span>
              <span class="text-xs text-gray-500 ml-2"
                >请选择负责审核的后保部人员</span
              >
            </label>
            <select
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 transition-shadow"
              v-model="form.logisticsApproverId"
              required
            >
              <option value="">请选择后保部审批人</option>
              <option
                v-for="approver in logisticsApprovers"
                :key="approver.id"
                :value="approver.id"
              >
                {{ approver.nickname || approver.username }}
                <span v-if="approver.departmentName"
                  >- {{ approver.departmentName }}</span
                >
              </option>
            </select>
          </div>

          <!-- 入库说明（当需要审批时显示） -->
          <div v-if="showApproverField" class="md:col-span-2">
            <label class="block text-sm font-medium text-gray-700 mb-1">
              入库说明
              <span class="text-xs text-gray-500 ml-2"
                >请说明入库物品的用途、来源等信息</span
              >
            </label>
            <textarea
              v-model="form.remark"
              rows="3"
              class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
              placeholder="请详细描述入库原因（如：项目需求、替换旧设备等）"
              maxlength="500"
            ></textarea>
            <div class="text-right text-sm text-gray-500 mt-1">
              {{ form.remark.length }}/500
            </div>
          </div>

          <!-- 提交按钮 -->
          <div class="md:col-span-2 pt-2 pb-4">
            <button
              class="w-full text-white py-2 px-4 rounded-md transition-colors flex items-center justify-center"
              :class="submitButtonClass"
              type="submit"
              :disabled="loading"
            >
              <span v-if="loading" class="flex items-center">
                <i class="fas fa-spinner fa-spin mr-2"></i>
                提交中...
              </span>
              <span v-else class="flex items-center">
                <i class="fas fa-check mr-2"></i>
                {{ submitButtonText }}
              </span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { addInventory } from "@/api/inventory";

export default {
  name: "AddItemModal",
  props: {
    show: {
      type: Boolean,
      default: false,
    },
    categories: {
      type: Array,
      default: () => [],
    },
    categoryIdMap: {
      type: Object,
      default: () => ({}),
    },
    // 新增：是否有权限入库电子耗材/危化品（父组件传）
    hasStoragePermission: {
      type: Boolean,
      default: false,
    },
    // 新增：是否是信息中心用户（父组件传）
    isItCenterUser: {
      type: Boolean,
      default: false,
    },
    // 新增：后保部审批人列表（当isItCenterUser为true时传入）
    logisticsApprovers: {
      type: Array,
      default: () => [],
    },
    // 新增：操作人ID
    operatorId: {
      type: [String, Number],
      required: true,
    },
  },
  data() {
    return {
      loading: false,
      form: {
        name: "",
        category: "",
        spec: "",
        brand: "",
        supplier: "",
        unitPrice: null,
        expireDate: "",
        quantity: 1,
        date: "",
        warningValue: null,
        unit: "个",
        logisticsApproverId: "", // 后保部审批人ID
        remark: "", // 入库说明
      },
    };
  },
  computed: {
    // 是否需要审批（IT中心用户且选择了受限分类）
    needApproval() {
      // 如果有管理权限，直接不需要审批
      if (this.$hasPermission("SUPPLY:MANAGE")) {
        return false;
      }

      // 如果没有IT物品权限，不需要审批
      if (!this.$hasPermission("SUPPLY:IT_ITEM")) {
        return false;
      }

      const selectedCategory = this.form.category;
      const restrictedCategories = ["电子耗材", "危化品"];

      return restrictedCategories.includes(selectedCategory);
    },

    // 是否显示审批人字段
    showApproverField() {
      return this.needApproval;
    },

    // 可用分类（根据用户权限过滤）
    availableCategories() {
      if (this.isItCenterUser && !this.hasStoragePermission) {
        // IT中心用户无权限时，只能看到电子耗材、危化品
        return this.categories.filter((cat) =>
          ["电子耗材", "危化品"].includes(cat)
        );
      }
      return this.categories;
    },

    // 模态框标题
    modalTitle() {
      if (this.needApproval) {
        return "IT部物品入库申请";
      }
      return "新增物品入库";
    },

    // 提交按钮文本
    submitButtonText() {
      if (this.needApproval) {
        return "提交入库申请（需后保部审批）";
      }
      return "提交入库";
    },

    // 提交按钮样式
    submitButtonClass() {
      if (this.needApproval) {
        return "bg-blue-600 hover:bg-blue-700";
      }
      return "bg-green-600 hover:bg-green-700";
    },
  },
  watch: {
    show(newVal) {
      if (newVal) {
        this.setDefaultDate();
        this.$nextTick(() => {
          const modal = this.$el.querySelector('[style*="max-height: 85vh"]');
          if (modal) {
            modal.scrollTop = 0;
          }
        });
      } else {
        this.resetForm();
      }
    },
    // 新增：监听入库日期变化
    "form.date"(newDate) {
      // 如果过期日期存在且早于新的入库日期，清空过期日期
      if (this.form.expireDate && newDate && this.form.expireDate < newDate) {
        this.form.expireDate = "";
      }
    },
  },
  mounted() {
    this.setDefaultDate();
  },
  methods: {
    setDefaultDate() {
      const today = new Date().toISOString().split("T")[0];
      this.form.date = today;
    },
    resetForm() {
      this.form = {
        name: "",
        category: "",
        spec: "",
        brand: "",
        supplier: "",
        unitPrice: null,
        expireDate: "",
        quantity: 1,
        date: new Date().toISOString().split("T")[0],
        warningValue: null,
        unit: "个",
        logisticsApproverId: "",
        remark: "",
      };
    },
    handleCategoryChange() {
      // 分类改变时重置审批人选择（如果需要的话）
      if (!this.needApproval) {
        this.form.logisticsApproverId = "";
      }
    },
    handleClose() {
      this.$emit("close");
      this.resetForm();
    },
    validateForm() {
      // 必填项验证
      if (!this.form.name || !this.form.category || !this.form.quantity) {
        this.$message.warning("请填写必填项！");
        return false;
      }

      // 数量验证
      if (this.form.quantity <= 0) {
        this.$message.warning("物品数量必须大于0！");
        return false;
      }

      // 预警值验证
      if (this.form.warningValue && this.form.warningValue < 0) {
        this.$message.warning("预警值不能为负数！");
        return false;
      }

      // 单价验证
      if (this.form.unitPrice && this.form.unitPrice < 0) {
        this.$message.warning("单价不能为负数！");
        return false;
      }

      // 新增：验证过期日期不能早于入库日期
      if (
        this.form.expireDate &&
        this.form.date &&
        this.form.expireDate < this.form.date
      ) {
        this.$message.warning("过期日期不能早于入库日期！");
        return false;
      }

      // IT中心权限验证
      const restrictedCategories = ["电子耗材", "危化品"];
      const isRestricted = restrictedCategories.includes(this.form.category);

      if (isRestricted && !this.hasStoragePermission) {
        this.$message.error("您没有权限入库电子耗材/危化品！");
        return false;
      }

      // 如果需要审批，验证审批人
      if (this.needApproval && !this.form.logisticsApproverId) {
        this.$message.warning("请选择后保部审批人！");
        return false;
      }

      return true;
    },
    async submit() {
      try {
        if (!this.validateForm()) {
          return;
        }

        this.loading = true;

        const categoryId = this.categoryIdMap[this.form.category];
        if (!categoryId) {
          this.$message.error("选择的分类不存在！");
          return;
        }

        // 构建提交数据
        const newItemData = {
          itemName: this.form.name,
          categoryId: categoryId,
          spec: this.form.spec,
          quantity: this.form.quantity,
          warningValue: this.form.warningValue,
          unit: this.form.unit || "个",
          purchaseDate: this.form.date,
          status: "1", // 默认状态
          brand: this.form.brand || "",
          supplier: this.form.supplier || "",
          unitPrice: this.form.unitPrice || 0.0,
          expireDate: this.form.expireDate || null,
          operatorId: this.operatorId,

          // 新增字段：是否需要审批
          needApproval: this.needApproval,

          // 如果needApproval为true，则传入审批人ID和备注
          ...(this.needApproval && {
            logisticsApproverId: this.form.logisticsApproverId,
            remark: this.form.remark || "",
          }),
        };

        console.log("提交物品数据:", newItemData);

        const response = await addInventory(newItemData);

        if (response.code === 200 || response.code === "200") {
          // 根据是否需要审批显示不同提示
          if (this.needApproval) {
            this.$message.success("入库申请已提交，请等待后保部审批！");
          } else {
            this.$message.success("物品添加成功！");
          }

          this.$emit("success");
          this.handleClose();
        } else {
          this.$message.error("提交失败：" + response.message);
        }
      } catch (error) {
        console.error("提交失败:", error);
        this.$message.error("提交失败：" + (error.message || "网络错误"));
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style scoped>
/* 模态框背景动画 */
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

/* 模态框内容动画 */
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

/* 禁用状态的按钮样式 */
button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

button:disabled:hover {
  background-color: #3b82f6; /* bg-blue-600 */
}

/* 表单输入框聚焦效果增强 */
input:focus,
select:focus {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 强制显示滚动条（解决部分浏览器隐藏滚动条问题） */
div[style*="max-height: 85vh"] {
  scrollbar-width: auto !important;
  -ms-overflow-style: auto !important;
}

div[style*="max-height: 85vh"]::-webkit-scrollbar {
  width: 8px !important;
  display: block !important;
}

div[style*="max-height: 85vh"]::-webkit-scrollbar-track {
  background: #f1f1f1 !important;
  border-radius: 4px !important;
}

div[style*="max-height: 85vh"]::-webkit-scrollbar-thumb {
  background: #ccc !important;
  border-radius: 4px !important;
}

div[style*="max-height: 85vh"]::-webkit-scrollbar-thumb:hover {
  background: #999 !important;
}

/* 粘性标题栏样式强化 */
.sticky {
  position: sticky !important;
  top: 0 !important;
  z-index: 999 !important;
}
</style>
