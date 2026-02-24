<template>
  <!-- 编辑物品模态框 -->
  <div class="modal-overlay" v-if="show" @click.self="handleClose">
    <div class="modal-container">
      <div class="modal-header">
        <h3 class="modal-title">编辑物品信息</h3>
        <button class="modal-close" @click="handleClose">
          <i class="fas fa-times"></i>
        </button>
      </div>

      <form class="modal-form" @submit.prevent="handleSave">
        <input v-model="formData.id" type="hidden" />

        <div class="form-content">
          <div class="form-group">
            <label class="form-label">
              物品名称 <span class="required">*</span>
            </label>
            <input
              class="form-input"
              v-model="formData.name"
              required
              type="text"
              placeholder="请输入物品名称"
            />
          </div>

          <div class="form-group">
            <label class="form-label"> 型号规格 </label>
            <input
              class="form-input"
              v-model="formData.spec"
              type="text"
              placeholder="请输入型号规格"
            />
          </div>

          <div class="form-group">
            <label class="form-label">
              分类 <span class="required">*</span>
            </label>
            <select class="form-input" v-model="formData.category" required>
              <option value="">请选择分类</option>
              <option v-for="cat in categories" :key="cat" :value="cat">
                {{ cat }}
              </option>
            </select>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label">
                库存数量 <span class="required">*</span>
              </label>
              <input
                class="form-input"
                v-model.number="formData.quantity"
                min="0"
                required
                type="number"
                placeholder="0"
              />
            </div>

            <div class="form-group">
              <label class="form-label"> 预警值 </label>
              <input
                class="form-input"
                v-model.number="formData.warningValue"
                min="0"
                type="number"
                placeholder="不设置"
              />
            </div>
          </div>

          <div class="form-group">
            <label class="form-label"> 供货商 </label>
            <input
              class="form-input"
              v-model="formData.supplier"
              type="text"
              placeholder="请输入供货商名称"
            />
          </div>

          <div class="form-group">
            <label class="form-label"> 单价(元) </label>
            <input
              class="form-input"
              v-model.number="formData.unitPrice"
              min="0"
              step="0.01"
              type="number"
              placeholder="0.00"
            />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label"> 过期日期 </label>
              <input
                class="form-input"
                v-model="formData.expireDate"
                type="date"
              />
            </div>

            <div class="form-group">
              <label class="form-label"> 采购日期 </label>
              <input
                class="form-input"
                v-model="formData.purchaseDate"
                type="date"
              />
            </div>
          </div>

          <div class="form-group">
            <label class="form-label"> 单位 </label>
            <select class="form-input" v-model="formData.unit">
              <option value="个">个</option>
              <option value="件">件</option>
              <option value="台">台</option>
              <option value="套">套</option>
              <option value="箱">箱</option>
              <option value="米">米</option>
              <option value="千克">千克</option>
              <option value="升">升</option>
            </select>
          </div>
        </div>

        <div class="modal-footer">
          <button
            type="button"
            class="btn-cancel"
            @click="handleClose"
            :disabled="saving"
          >
            取消
          </button>
          <button type="submit" class="btn-submit" :disabled="saving">
            <span v-if="saving">保存中...</span>
            <span v-else>保存修改</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { updateInventory } from "@/api/inventory";

export default {
  name: "EditItemModal",
  props: {
    show: {
      type: Boolean,
      default: false,
    },
    item: {
      type: Object,
      default: () => ({}),
    },
    categories: {
      type: Array,
      default: () => [],
    },
    categoryIdMap: {
      type: Object,
      default: () => ({}),
    },
  },
  data() {
    return {
      formData: {
        id: null,
        name: "",
        category: "",
        spec: "",
        quantity: 0,
        supplier: "",
        unitPrice: "",
        warningValue: null,
        expireDate: "",
        purchaseDate: "",
        unit: "个",
        status: "1",
      },
      saving: false,
    };
  },
  watch: {
    item: {
      immediate: true,
      handler(newItem) {
        if (newItem && newItem.id) {
          this.formData = {
            id: newItem.id,
            name: newItem.name || "",
            category: newItem.category || "",
            spec: newItem.spec || "",
            supplier: newItem.supplier || "",
            unitPrice: newItem.unitPrice || "",
            quantity: newItem.quantity || 0,
            warningValue: newItem.warningValue || null,
            expireDate: newItem.expireDate || "",
            purchaseDate:
              newItem.purchaseDate || new Date().toISOString().split("T")[0],
            unit: newItem.unit || "个",
            status: newItem.status || "1",
          };
        }
      },
    },
  },
  methods: {
    handleClose() {
      this.$emit("close");
    },

    validateForm() {
      if (!this.formData.name || !this.formData.category) {
        this.$message.warning("物品名称和分类不能为空！");
        return false;
      }
      if (this.formData.quantity < 0) {
        this.$message.warning("库存数量不能为负数！");
        return false;
      }
      if (this.formData.warningValue && this.formData.warningValue < 0) {
        this.$message.warning("预警值不能为负数！");
        return false;
      }
      return true;
    },

    async handleSave() {
      if (!this.validateForm()) {
        return;
      }

      try {
        this.saving = true;

        const categoryId = this.categoryIdMap[this.formData.category];
        if (!categoryId) {
          this.$message.error(
            `分类"${this.formData.category}"不存在，请重新选择`
          );
          return;
        }

        const updateData = {
          id: this.formData.id,
          itemName: this.formData.name,
          categoryId: categoryId,
          spec: this.formData.spec || "",
          supplier: this.formData.supplier || "",
          unitPrice: this.formData.unitPrice
            ? Number(this.formData.unitPrice)
            : "",
          quantity: this.formData.quantity,
          warningValue: this.formData.warningValue,
          expireDate: this.formData.expireDate || null,
          purchaseDate: this.formData.purchaseDate,
          unit: this.formData.unit || "个",
          status: "1",
        };

        console.log("编辑提交参数：", updateData);
        const response = await updateInventory(updateData);

        if (
          response.code === 200 ||
          response.code === "200" ||
          response.code === 20000
        ) {
          this.$message.success("物品信息更新成功！");
          this.$emit("saved");
          this.handleClose();
        } else {
          this.$message.error(
            "更新失败：" + (response.message || "服务器异常")
          );
        }
      } catch (error) {
        console.error("编辑失败详情：", error);
        this.$message.error("更新物品失败：" + error.message);
      } finally {
        this.saving = false;
      }
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
  z-index: 10000;
  padding: 1rem;
}

.modal-container {
  background: white;
  border-radius: 0.75rem;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  width: 90%;
  max-width: 32rem;
  max-height: 90vh;
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
    font-size: 1.25rem;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 2rem;
    height: 2rem;

    &:hover {
      color: #374151;
      background: #f3f4f6;
    }
  }
}

.modal-form {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.form-content {
  padding: 1.5rem;
  flex: 1;
  overflow-y: auto;
  max-height: calc(90vh - 8rem); /* 减去头部和底部的高度 */
}

.form-group {
  margin-bottom: 1rem;

  .form-label {
    display: block;
    font-size: 0.875rem;
    font-weight: 500;
    color: #374151;
    margin-bottom: 0.375rem;

    .required {
      color: #ef4444;
    }
  }

  .form-input {
    width: 100%;
    padding: 0.625rem 0.75rem;
    border: 1px solid #d1d5db;
    border-radius: 0.375rem;
    font-size: 0.875rem;
    color: #374151;
    background: white;
    transition: all 0.2s;

    &:focus {
      outline: none;
      border-color: #3b82f6;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }

    &::placeholder {
      color: #9ca3af;
    }

    &[type="date"] {
      min-height: 2.5rem;
    }

    &[type="number"] {
      &::-webkit-inner-spin-button,
      &::-webkit-outer-spin-button {
        opacity: 1;
      }
    }
  }
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

@media (max-width: 640px) {
  .modal-container {
    width: 95%;
    max-width: 95%;
    margin: 0.5rem;
  }

  .modal-header {
    padding: 1rem;
  }

  .form-content {
    padding: 1rem;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }
}

.modal-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid #e5e7eb;
  display: flex;
  gap: 0.75rem;
  flex-shrink: 0;

  button {
    flex: 1;
    padding: 0.625rem 1rem;
    border-radius: 0.375rem;
    font-weight: 500;
    font-size: 0.875rem;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid transparent;

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }

  .btn-cancel {
    background: white;
    border-color: #d1d5db;
    color: #374151;

    &:hover:not(:disabled) {
      background: #f9fafb;
    }
  }

  .btn-submit {
    background: #3b82f6;
    color: white;

    &:hover:not(:disabled) {
      background: #2563eb;
    }
  }
}

/* 滚动条样式 */
.form-content::-webkit-scrollbar {
  width: 6px;
}

.form-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.form-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.form-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
