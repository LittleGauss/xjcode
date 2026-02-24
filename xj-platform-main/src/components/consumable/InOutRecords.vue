<template>
  <div class="in-out-records-page">
    <!-- 头部组件 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @navigate="handleNavigate"
      @logout="handleLogout"
    />

    <!-- 主要内容 -->
    <main class="in-out-records-main">
      <div class="in-out-records">
        <!-- 头部导航 -->
        <div class="mb-6">
          <button
            class="px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded-lg flex items-center"
            @click="$router.go(-1)"
          >
            <i class="fas fa-arrow-left mr-2"></i> 返回
          </button>
        </div>

        <!-- 权限检查 -->
        <div
          v-if="!hasPermission"
          class="bg-red-50 border border-red-200 rounded-lg p-6 text-center"
        >
          <i class="fas fa-exclamation-triangle text-red-500 text-4xl mb-4"></i>
          <h2 class="text-xl font-bold text-red-700 mb-2">权限不足</h2>
          <p class="text-gray-600">您没有权限访问出入库记录页面</p>
          <p class="text-sm text-gray-500 mt-2">
            该功能仅限后保部(ROLE_LOGISTICS)角色使用
          </p>
        </div>

        <!-- 主要内容 -->
        <div v-else>
          <!-- 标题 -->
          <div class="flex justify-between items-center mb-6">
            <div>
              <h1 class="text-2xl font-bold text-gray-800">耗材出入库记录</h1>
              <p class="text-gray-600 mt-1">
                查看详细的物品出入库记录，包括领用人、时间、供货商等信息
              </p>
            </div>
            <div class="text-sm text-gray-500">
              <i class="fas fa-shield-alt mr-1"></i> 后保部专属功能
            </div>
          </div>

          <!-- 筛选工具栏 -->
          <div class="bg-white rounded-lg shadow-md p-4 mb-6">
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <!-- 时间范围 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">
                  时间范围
                </label>
                <select
                  v-model="filters.timeRange"
                  @change="handleTimeRangeChange"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="month">本月</option>
                  <option value="quarter">本季度</option>
                  <option value="year">本年</option>
                  <option value="custom">自定义</option>
                </select>
              </div>

              <!-- 自定义日期 -->
              <div
                v-if="filters.timeRange === 'custom'"
                class="grid grid-cols-2 gap-2"
              >
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">
                    开始日期
                  </label>
                  <input
                    type="date"
                    v-model="filters.startDate"
                    @change="handleFilterChange"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">
                    结束日期
                  </label>
                  <input
                    type="date"
                    v-model="filters.endDate"
                    @change="handleFilterChange"
                    class="w-full px-3 py-2 border border-gray-300 rounded-md"
                  />
                </div>
              </div>

              <!-- 供货商筛选 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">
                  供货商
                </label>
                <select
                  v-model="filters.supplier"
                  @change="handleFilterChange"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">全部供货商</option>
                  <option
                    v-for="supplier in supplierOptions"
                    :key="supplier"
                    :value="supplier"
                  >
                    {{ supplier }}
                  </option>
                </select>
              </div>

              <!-- 操作类型 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">
                  操作类型
                </label>
                <select
                  v-model="filters.operationType"
                  @change="handleFilterChange"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">全部类型</option>
                  <option value="IN">入库</option>
                  <option value="OUT">出库</option>
                </select>
              </div>

              <!-- 物品名称搜索 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">
                  物品名称
                </label>
                <input
                  type="text"
                  v-model="filters.itemName"
                  @input="handleFilterChange"
                  placeholder="输入物品名称..."
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <!-- 按钮组 -->
              <div class="flex items-end space-x-2">
                <button
                  @click="resetFilters"
                  class="px-4 py-2 bg-gray-200 hover:bg-gray-300 text-gray-700 rounded-lg flex items-center"
                >
                  <i class="fas fa-redo mr-2"></i> 重置
                </button>
                <button
                  @click="exportExcel"
                  class="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg flex items-center"
                >
                  <i class="fas fa-file-export mr-2"></i> 导出Excel
                </button>
              </div>
            </div>
          </div>

          <!-- 统计卡片 -->
          <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
            <div class="bg-white rounded-lg shadow-md p-4">
              <div class="flex items-center">
                <div class="p-3 bg-blue-100 rounded-lg">
                  <i class="fas fa-box text-blue-600 text-xl"></i>
                </div>
                <div class="ml-4">
                  <p class="text-sm text-gray-500">入库总量</p>
                  <p class="text-2xl font-bold text-blue-600">
                    {{ statistics.totalIn }}
                  </p>
                </div>
              </div>
            </div>
            <div class="bg-white rounded-lg shadow-md p-4">
              <div class="flex items-center">
                <div class="p-3 bg-green-100 rounded-lg">
                  <i class="fas fa-sign-out-alt text-green-600 text-xl"></i>
                </div>
                <div class="ml-4">
                  <p class="text-sm text-gray-500">出库总量</p>
                  <p class="text-2xl font-bold text-green-600">
                    {{ statistics.totalOut }}
                  </p>
                </div>
              </div>
            </div>
            <div class="bg-white rounded-lg shadow-md p-4">
              <div class="flex items-center">
                <div class="p-3 bg-purple-100 rounded-lg">
                  <i class="fas fa-building text-purple-600 text-xl"></i>
                </div>
                <div class="ml-4">
                  <p class="text-sm text-gray-500">涉及供货商</p>
                  <p class="text-2xl font-bold text-purple-600">
                    {{ statistics.supplierCount }}
                  </p>
                </div>
              </div>
            </div>
            <div class="bg-white rounded-lg shadow-md p-4">
              <div class="flex items-center">
                <div class="p-3 bg-yellow-100 rounded-lg">
                  <i class="fas fa-users text-yellow-600 text-xl"></i>
                </div>
                <div class="ml-4">
                  <p class="text-sm text-gray-500">操作人员</p>
                  <p class="text-2xl font-bold text-yellow-600">
                    {{ statistics.operatorCount }}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- 数据表格 -->
          <div class="bg-white rounded-lg shadow-md overflow-hidden">
            <div class="overflow-x-auto">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-blue-50">
                  <tr>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-blue-700 uppercase tracking-wider"
                    >
                      操作时间
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-blue-700 uppercase tracking-wider"
                    >
                      操作类型
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-blue-700 uppercase tracking-wider"
                    >
                      物品名称
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-blue-700 uppercase tracking-wider"
                    >
                      分类名称
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-blue-700 uppercase tracking-wider"
                    >
                      数量变化
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-blue-700 uppercase tracking-wider"
                    >
                      操作前库存
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-blue-700 uppercase tracking-wider"
                    >
                      操作后库存
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-blue-700 uppercase tracking-wider"
                    >
                      操作人
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-blue-700 uppercase tracking-wider"
                    >
                      供货商
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-blue-700 uppercase tracking-wider"
                    >
                      单价
                    </th>
                    <!-- 新增：unitPrice -->
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-blue-700 uppercase tracking-wider"
                    >
                      出入库总价
                    </th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr
                    v-for="record in allRecords"
                    :key="record.id"
                    class="hover:bg-gray-50 transition-colors"
                  >
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-gray-900"
                    >
                      {{ formatDateTime(record.operationTime) }}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <span
                        :class="getOperationTypeClass(record.operationType)"
                        class="px-2 py-1 rounded-full text-xs font-medium"
                      >
                        {{ getOperationTypeText(record.operationType) }}
                      </span>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <div class="text-sm font-medium text-gray-900">
                        {{ record.itemName }}
                      </div>
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-gray-500"
                    >
                      {{ record.categoryName || "-" }}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <span
                        :class="
                          record.quantityChange >= 0
                            ? 'text-green-600'
                            : 'text-red-600'
                        "
                      >
                        {{ record.quantityChange >= 0 ? "+" : ""
                        }}{{ record.quantityChange }}
                      </span>
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-gray-500"
                    >
                      {{ record.quantityBefore }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-gray-500"
                    >
                      {{ record.quantityAfter }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-gray-900"
                    >
                      <div>{{ record.operatorNickname }}</div>
                      <div
                        v-if="record.operatorDept"
                        class="text-xs text-gray-500"
                      >
                        {{ record.operatorDept }}
                      </div>
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-gray-500"
                    >
                      <span
                        v-if="record.supplier"
                        class="bg-gray-100 px-2 py-1 rounded text-xs"
                      >
                        {{ record.supplier }}
                      </span>
                      <span v-else class="text-gray-400 text-xs">-</span>
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-gray-500"
                    >
                      {{
                        record.unitPrice ? record.unitPrice.toFixed(2) : "0.00"
                      }}
                      <!-- 新增：显示unitPrice（保留两位小数） -->
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-gray-500"
                    >
                      {{
                        record.totalPrice
                          ? record.totalPrice.toFixed(2)
                          : "0.00"
                      }}
                      <!-- 新增：显示totalPrice（保留两位小数） -->
                    </td>
                  </tr>

                  <!-- 空数据提示 -->
                  <tr v-if="allRecords.length === 0 && !loading">
                    <td
                      colspan="12"
                      class="px-6 py-8 text-center text-gray-500"
                    >
                      <i
                        class="fas fa-inbox text-4xl mb-2 block text-gray-300"
                      ></i>
                      <p class="text-gray-500">暂无出入库记录</p>
                      <p class="text-sm text-gray-400 mt-1">
                        请尝试调整筛选条件或进行库存操作
                      </p>
                    </td>
                  </tr>

                  <!-- 加载中 -->
                  <tr v-if="loading">
                    <td colspan="12" class="px-6 py-8 text-center">
                      <div class="flex justify-center items-center">
                        <div
                          class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"
                        ></div>
                        <span class="ml-2 text-gray-500">加载中...</span>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 分页 -->
          <div
            v-if="pagination.total > 0"
            class="mt-6 flex justify-between items-center bg-white rounded-lg shadow-md p-4"
          >
            <div class="text-sm text-gray-500">
              显示第
              {{ (pagination.currentPage - 1) * pagination.pageSize + 1 }} 到
              {{
                Math.min(
                  pagination.currentPage * pagination.pageSize,
                  pagination.total
                )
              }}
              条， 共 {{ pagination.total }} 条记录
            </div>
            <div class="flex items-center space-x-2">
              <select
                v-model="pagination.pageSize"
                @change="handlePageSizeChange"
                class="px-2 py-1 border border-gray-300 rounded-md text-sm"
              >
                <option value="10">10条/页</option>
                <option value="20">20条/页</option>
                <option value="50">50条/页</option>
                <option value="100">100条/页</option>
              </select>
              <button
                @click="prevPage"
                :disabled="pagination.currentPage === 1"
                class="px-3 py-1 border border-gray-300 rounded-md text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
              >
                上一页
              </button>
              <span class="text-sm">
                第 {{ pagination.currentPage }} 页，共 {{ totalPages }} 页
              </span>
              <button
                @click="nextPage"
                :disabled="pagination.currentPage >= totalPages"
                class="px-3 py-1 border border-gray-300 rounded-md text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
              >
                下一页
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 页脚组件 -->
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>

<script>
import {
  getInOutRecords,
  getInOutStatistics,
  getSuppliers,
} from "@/api/inventory";
import HeaderComponent from "@/components/HeaderComponent.vue";
import FooterComponent from "@/components/FooterComponent.vue";
import { removeToken, getUserToken, removeUserToken } from "@/utils/auth";

export default {
  name: "InOutRecords",
  components: {
    HeaderComponent,
    FooterComponent,
  },
  data() {
    return {
      // 用户信息
      login_user: null,
      userRoles: [],

      // 筛选条件
      filters: {
        timeRange: "month",
        startDate: "",
        endDate: "",
        supplier: "",
        operationType: "",
        itemName: "",
      },

      // 数据
      allRecords: [],
      supplierOptions: [],

      // 统计信息
      statistics: {
        totalIn: 0,
        totalOut: 0,
        supplierCount: 0,
        operatorCount: 0,
      },

      // 分页
      pagination: {
        currentPage: 1,
        pageSize: 20,
        total: 0,
      },

      // 加载状态
      loading: false,
    };
  },

  computed: {
    // 用户是否有权限访问
    hasPermission() {
      // 使用userRoles进行权限判断，避免未使用的警告
      const roleCodes = this.userRoles.map((item) => item.code || "");
      const storedUserInfo = getUserToken();
      const userPermissions = storedUserInfo?.permissions || [];

      // 检查是否有后保部角色或导出权限
      return (
        roleCodes.includes("ROLE_LOGISTICS") ||
        userPermissions.includes("SUPPLY:EXPORT")
      ); // 保留原有true，可根据实际需求调整
    },

    // 总页数
    totalPages() {
      return Math.ceil(this.pagination.total / this.pagination.pageSize) || 1;
    },
  },

  created() {
    // 获取用户信息
    this.getUserInfo();

    // 初始化日期范围
    this.initDateRange();

    // 加载数据
    this.loadData();
  },

  methods: {
    // 获取用户信息
    getUserInfo() {
      const storedUserInfo = getUserToken();
      if (storedUserInfo && storedUserInfo.user) {
        this.login_user = storedUserInfo.user;
        this.userRoles = storedUserInfo.roles || [];
      } else {
        this.$message.warning("您尚未登录，请先登录！");
        this.$router.push("/login");
      }
    },

    // 处理头部导航事件
    handleNavigate(routeName) {
      if (routeName === "InOutRecords") {
        return; // 如果已经在当前页面，不进行跳转
      }
      this.$router.push({ name: routeName });
    },

    // 退出登录
    handleLogout() {
      // 清除用户信息
      removeUserToken();
      this.login_user = null;
      this.userRoles = [];
      removeToken();
      this.$router.push("/login");
    },

    // 初始化日期范围
    initDateRange() {
      const now = new Date();
      const today = now.toISOString().split("T")[0];

      if (this.filters.timeRange === "month") {
        const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
        this.filters.startDate = firstDay.toISOString().split("T")[0];
        this.filters.endDate = today;
      } else if (this.filters.timeRange === "quarter") {
        const quarter = Math.floor(now.getMonth() / 3);
        const firstDay = new Date(now.getFullYear(), quarter * 3, 1);
        this.filters.startDate = firstDay.toISOString().split("T")[0];
        this.filters.endDate = today;
      } else if (this.filters.timeRange === "year") {
        this.filters.startDate = `${now.getFullYear()}-01-01`;
        this.filters.endDate = today;
      }
    },

    // 加载数据
    async loadData() {
      try {
        this.loading = true;

        // 构建查询参数
        const params = {
          timeRange: this.filters.timeRange,
          startDate: this.filters.startDate,
          endDate: this.filters.endDate,
          supplier: this.filters.supplier,
          operationType: this.filters.operationType,
          itemName: this.filters.itemName,
          current: this.pagination.currentPage,
          size: this.pagination.pageSize,
        };
        console.log("发送请求参数:", params); // 添加日志
        // 获取出入库记录
        const recordsRes = await getInOutRecords(params);
        console.log("获取出入库记录响应:", recordsRes); // 添加日志

        if (recordsRes.code == 200) {
          // 确保响应式更新
          this.allRecords = recordsRes.data.records
            ? [...recordsRes.data.records]
            : [];
          this.pagination.total = recordsRes.data.total || 0;
          console.log("allRecords:", this.allRecords);
        } else {
          this.$message.error(
            "获取出入库记录失败：" + (recordsRes.message || "未知错误")
          );
        }

        // 获取供货商列表
        const suppliersRes = await getSuppliers();
        console.log("获取供货商列表响应:", suppliersRes);
        if (suppliersRes.code == 200) {
          this.supplierOptions = suppliersRes.data || [];
        }

        // 获取统计信息
        const statsParams = {
          startDate: this.filters.startDate,
          endDate: this.filters.endDate,
        };
        const statsRes = await getInOutStatistics(statsParams);
        console.log("获取统计信息响应:", statsRes);
        if (statsRes.code == 200) {
          this.statistics = statsRes.data || {};
        }
      } catch (error) {
        console.error("加载数据失败:", error);
        this.$message.error("数据加载失败：" + (error.message || "未知错误"));
      } finally {
        this.loading = false;
      }
    },

    // 处理时间范围变化
    handleTimeRangeChange() {
      this.initDateRange();
      this.pagination.currentPage = 1;
      this.loadData();
    },

    // 处理筛选变化
    handleFilterChange() {
      this.pagination.currentPage = 1;
      this.loadData();
    },

    // 重置筛选条件
    resetFilters() {
      this.filters = {
        timeRange: "month",
        startDate: "",
        endDate: "",
        supplier: "",
        operationType: "",
        itemName: "",
      };
      this.initDateRange();
      this.pagination.currentPage = 1;
      this.loadData();
    },

    // 导出Excel前端本地生成并导出Excel
    async exportExcel() {
      try {
        // 动态导入xlsx和file-saver库（无需提前全局引入，按需加载）
        const XLSX = await import("xlsx");
        const { saveAs } = await import("file-saver");

        // 导出数据：使用当前筛选后的所有出入库记录
        const exportData = this.allRecords;
        console.log("待导出的出入库记录：", exportData);

        // 校验是否有可导出数据
        if (!exportData || exportData.length === 0) {
          this.$message.warning("当前没有可导出的出入库记录！");
          return;
        }

        // 1. 定义Excel表头（与表格列对应，可自定义）
        const wsData = [
          [
            "操作时间",
            "操作类型",
            "物品名称",
            "分类名称",
            "数量变化",
            "操作前库存",
            "操作后库存",
            "操作人",
            "操作人部门",
            "供货商",
            "单价", // 新增
            "出入库总价", // 新增
          ],
          // 2. 映射表格数据（对应表头顺序，处理空值）
          ...exportData.map((item) => [
            this.formatDateTime(item.operationTime) || "-", // 格式化时间
            this.getOperationTypeText(item.operationType) || "-", // 转换操作类型为中文
            item.itemName || "未命名物品",
            item.categoryName || "-",
            // 数量变化：带正负号展示
            (item.quantityChange >= 0 ? "+" : "") + (item.quantityChange || 0),
            item.quantityBefore || 0,
            item.quantityAfter || 0,
            item.operatorNickname || "-",
            item.operatorDept || "-",
            item.supplier || "-",
            item.unitPrice ? item.unitPrice.toFixed(2) : "0.00", // 新增
            item.totalPrice ? item.totalPrice.toFixed(2) : "0.00", // 新增
          ]),
        ];

        // 3. 创建Excel工作簿和工作表
        const wb = XLSX.utils.book_new(); // 新建工作簿
        const ws = XLSX.utils.aoa_to_sheet(wsData); // 将数组转成工作表

        // 4. 设置列宽（优化展示效果，对应表头数量和宽度）
        const wscols = [
          { wch: 20 }, // 操作时间
          { wch: 10 }, // 操作类型
          { wch: 18 }, // 物品名称
          { wch: 12 }, // 分类名称
          { wch: 12 }, // 数量变化
          { wch: 12 }, // 操作前库存
          { wch: 12 }, // 操作后库存
          { wch: 10 }, // 操作人
          { wch: 12 }, // 操作人部门
          { wch: 15 }, // 供货商
          { wch: 10 },
          { wch: 10 },
        ];
        ws["!cols"] = wscols; // 给工作表设置列宽

        // 5. 将工作表添加到工作簿，并命名工作表
        XLSX.utils.book_append_sheet(wb, ws, "出入库记录");

        // 6. 生成Excel文件并下载
        const wbout = XLSX.write(wb, { bookType: "xlsx", type: "array" }); // 生成二进制数组
        // 构建Blob对象并下载
        saveAs(
          new Blob([wbout], { type: "application/octet-stream" }),
          // 文件名：出入库记录_年月日.xlsx
          `出入库记录_${new Date().toISOString().slice(0, 10)}.xlsx`
        );

        this.$message.success("Excel导出成功！");
      } catch (error) {
        console.error("导出Excel失败:", error);
        this.$message.error("下载失败：" + (error.message || "未知错误"));
      }
    },

    // 分页操作
    handlePageSizeChange() {
      this.pagination.currentPage = 1;
      this.loadData();
    },

    prevPage() {
      if (this.pagination.currentPage > 1) {
        this.pagination.currentPage--;
        this.loadData();
      }
    },

    nextPage() {
      if (this.pagination.currentPage < this.totalPages) {
        this.pagination.currentPage++;
        this.loadData();
      }
    },

    // 辅助方法
    formatDateTime(dateTime) {
      if (!dateTime) return "-";
      try {
        const date = new Date(dateTime);
        return date
          .toLocaleString("zh-CN", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
            hour: "2-digit",
            minute: "2-digit",
          })
          .replace(/\//g, "-");
      } catch (e) {
        return dateTime;
      }
    },

    getOperationTypeText(type) {
      const typeMap = {
        IN: "入库",
        OUT: "出库",
        SCRAP: "报废",
      };
      return typeMap[type] || type;
    },

    getOperationTypeClass(type) {
      const classMap = {
        IN: "bg-green-100 text-green-800",
        OUT: "bg-red-100 text-red-800",
        SCRAP: "bg-red-100 text-red-800",
      };
      return classMap[type] || "bg-gray-100 text-gray-800";
    },
  },
};
</script>

<style scoped>
.in-out-records-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.in-out-records-main {
  flex: 1;
  padding: 20px 0;
  background-color: #f9fafb;
}

.in-out-records {
  padding: 0 20px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
}

/* 加载动画 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .in-out-records-main {
    padding: 16px 0;
  }

  .in-out-records {
    padding: 0 12px;
  }

  .grid-cols-4,
  .grid-cols-2,
  .grid-cols-1 {
    grid-template-columns: 1fr;
  }

  table {
    font-size: 12px;
  }

  .px-6 {
    padding-left: 8px;
    padding-right: 8px;
  }

  .py-4 {
    padding-top: 8px;
    padding-bottom: 8px;
  }
}
</style>
