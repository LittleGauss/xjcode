<template>
  <!-- 分类管理模态框 -->
  <div
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    v-if="show"
    @click.self="close"
  >
    <div class="bg-white rounded-lg shadow-xl p-6 w-full max-w-md">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-lg font-semibold">管理分类</h3>
        <button
          class="text-gray-500 hover:text-gray-700 transition-colors"
          @click="close"
        >
          <i class="fas fa-times"></i>
        </button>
      </div>
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1"
            >新增分类名称</label
          >
          <div class="flex">
            <input
              class="flex-1 px-3 py-2 border border-gray-300 rounded-l-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              v-model="newCategoryName"
              type="text"
              placeholder="输入分类名称"
              @keyup.enter="addCategory"
            />
            <button
              class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-r-md flex items-center"
              @click="addCategory"
              :disabled="adding"
            >
              <i class="fas fa-plus mr-1"></i> 添加
            </button>
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1"
            >现有分类</label
          >
          <ul
            class="border border-gray-300 rounded-md divide-y divide-gray-200 max-h-40 overflow-y-auto"
            v-if="categories.length > 0"
          >
            <li
              class="px-3 py-2 flex justify-between items-center hover:bg-gray-50"
              v-for="category in categories"
              :key="category"
            >
              <span>{{ category }}</span>
              <button
                class="delete-category-btn text-red-500 hover:text-red-700 transition-colors"
                @click="confirmDeleteCategory(category)"
                :disabled="deleting"
                title="删除分类"
              >
                <i class="fas fa-trash"></i>
              </button>
            </li>
          </ul>
          <div v-else class="text-center py-4 text-gray-500">
            <i class="fas fa-folder-open text-2xl mb-2 block"></i>
            暂无分类
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "CategoryModal",
  props: {
    show: {
      type: Boolean,
      default: false,
    },
    categories: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      newCategoryName: "",
      adding: false,
      deleting: false,
    };
  },
  watch: {
    show(newVal) {
      if (newVal) {
        this.newCategoryName = "";
      }
    },
  },
  methods: {
    close() {
      this.$emit("update:show", false);
      this.$emit("close");
    },
    addCategory() {
      const categoryName = this.newCategoryName.trim();
      if (!categoryName) {
        this.$message.warning("请输入分类名称");
        return;
      }

      this.adding = true;
      this.$emit("add", categoryName);
      this.newCategoryName = "";
      this.adding = false;
    },
    confirmDeleteCategory(category) {
      this.$confirm(`确定要删除分类"${category}"吗？`, "确认删除", {
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          this.deleting = true;
          this.$emit("delete", category);
          this.deleting = false;
        })
        .catch(() => {
          // 用户取消
        });
    },
  },
};
</script>

<style scoped>
.delete-category-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.fixed {
  animation: fadeIn 0.2s ease-out;
}

.bg-white {
  animation: slideIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideIn {
  from {
    transform: translateY(-20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
