<template>
  <div class="oa-homepage">
    <!-- 使用通用页头 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @logout="handleLogout"
      @navigate="handleNavigate"
    />

    <!-- 主要内容区域 -->
    <div class="content">
      <div class="content-center">
        <div
          class="flex flex-col md:flex-row justify-between items-start md:items-center mb-4 gap-4"
        >
          <h2 class="text-xl font-semibold text-gray-700 whitespace-nowrap">
            低值易耗品管理
          </h2>
          <div class="flex space-x-2">
            <select
              class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              v-model="categoryFilter"
              @change="filterInventory"
            >
              <option value="">所有分类</option>
              <option v-for="cat in categories" :key="cat" :value="cat">
                {{ cat }}
              </option>
            </select>
            <select
              class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              v-model="warningFilter"
              @change="filterInventory"
            >
              <option value="">所有状态</option>
              <option value="stock">库存预警</option>
              <option value="expire">保质期预警</option>
            </select>
          </div>
          <div class="flex flex-wrap gap-4 items-center">
            <!-- 公示管理要加权限，库存管理权限的人才能看到 -->
            <div class="flex gap-2" v-if="$hasPermission('SUPPLY:MANAGE')">
              <button
                class="common-small-btn bg-red-500 hover:bg-orange-700 text-white px-4 py-2 rounded-lg flex items-center"
                @click="goNotice"
              >
                <i class="fas fa-file-alt mr-2"></i> 易耗品公示管理
              </button>
            </div>
            <!-- 第一组：核心操作（高频，直接展示） -->
            <div class="flex gap-2">
              <button
                class="common-small-btn bg-red-600 hover:bg-orange-700 text-white px-4 py-2 rounded-lg flex items-center"
                v-if="$hasPermission(['SUPPLY:MANAGE'])"
                @click="goScrap"
              >
                <i class="fas fa-file-alt mr-2"></i> 报废管理
              </button>
            </div>

            <!-- 第二组：我的申请下拉菜单 -->
            <el-dropdown trigger="click" class="dropdown-btn-group">
              <button
                class="common-small-btn bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-lg flex items-center"
              >
                <i class="fas fa-clipboard-list mr-2"></i> 我的申请
                <i class="fas fa-caret-down ml-1"></i>
              </button>
              <el-dropdown-menu slot="dropdown">
                <!-- 我的领用申请 -->
                <el-dropdown-item @click.native="showMyApplications">
                  <i class="fas fa-hand-holding mr-2"></i> 我的领用申请
                </el-dropdown-item>
                <!-- 我的入库申请（IT中心用户可见） -->
                <el-dropdown-item
                  @click.native="showMyStockInApplications"
                  v-if="
                    $hasPermission('SUPPLY:IT_ITEM') &&
                    !$hasPermission('SUPPLY:MANAGE')
                  "
                >
                  <i class="fas fa-inbox mr-2"></i> 我的入库申请
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>

            <!-- 第三组：待我审批下拉菜单 -->
            <el-dropdown
              trigger="click"
              class="dropdown-btn-group"
              v-if="$hasPermission('SUPPLY:MANAGE')"
            >
              <button
                class="common-small-btn bg-blue-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg flex items-center"
              >
                <i class="fas fa-check-circle mr-2"></i> 待我审批
                <i class="fas fa-caret-down ml-1"></i>
              </button>
              <el-dropdown-menu slot="dropdown">
                <!-- 领用申请审批 -->
                <el-dropdown-item @click.native="showApprovalList">
                  <i class="fas fa-clipboard-check mr-2"></i> 领用申请审批
                </el-dropdown-item>
                <!-- 入库申请审批 -->
                <el-dropdown-item @click.native="showStockInApprovalList">
                  <i class="fas fa-clipboard-check mr-2"></i> 入库申请审批
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>

            <!-- 第四组：库存管理下拉菜单（收纳 新增物品/新增分类/批量导入/导出Excel） -->
            <el-dropdown
              trigger="click"
              class="dropdown-btn-group"
              v-if="$hasPermission(['SUPPLY:MANAGE', 'SUPPLY:IT_ITEM'])"
            >
              <button
                class="common-small-btn bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center"
              >
                <i class="fas fa-database mr-2"></i> 库存管理
                <i class="fas fa-caret-down ml-1"></i>
              </button>
              <el-dropdown-menu slot="dropdown">
                <!-- 后保部新增（无需审批） -->
                <el-dropdown-item
                  @click.native="openAddItemModal"
                  v-if="$hasPermission('SUPPLY:MANAGE')"
                >
                  <i class="fas fa-plus mr-2"></i> 新增物品
                </el-dropdown-item>

                <!-- IT部新增物品（需审批） -->
                <el-dropdown-item
                  @click.native="openAddItemModal"
                  v-else-if="$hasPermission('SUPPLY:IT_ITEM')"
                >
                  <i class="fas fa-plus mr-2"></i> 新增物品
                </el-dropdown-item>
                <!-- 新增分类 -->
                <el-dropdown-item
                  @click.native="showCategoryModal = true"
                  v-if="$hasPermission('SUPPLY:MANAGE')"
                >
                  <i class="fas fa-plus mr-2"></i> 新增分类
                </el-dropdown-item>
                <!-- 批量导入 -->
                <el-dropdown-item
                  @click.native="showImportModal = true"
                  v-if="$hasPermission('SUPPLY:IMPORT')"
                >
                  <i class="fas fa-file-import mr-2"></i> 批量导入
                </el-dropdown-item>
                <!-- 导出Excel -->
                <el-dropdown-item
                  @click.native="exportExcel"
                  v-if="$hasPermission('SUPPLY:EXPORT')"
                >
                  <i class="fas fa-file-export mr-2"></i> 导出Excel
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>

            <!-- 出入库记录 -->
            <button
              v-if="$hasPermission('SUPPLY:EXPORT')"
              class="common-small-btn bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg flex items-center"
              @click="goToInOutRecords"
            >
              <i class="fas fa-history mr-2"></i> 出入库记录
            </button>
          </div>
        </div>

        <!-- 修复：闭合表格外层 div（原代码缺失闭合标签） -->
        <div class="bg-white rounded-lg shadow-md p-4 mb-6">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-blue-50">
              <tr>
                <!-- 修复：表头与表体内容对应（原表头是申请相关，表体是物品库存相关，统一改为物品库存表头） -->
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
                >
                  物品名称
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
                >
                  分类
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
                >
                  型号规格
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
                >
                  单价(元)
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
                >
                  库存数量
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
                >
                  总金额(元)
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
                >
                  供货商
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
                >
                  预警值
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
                >
                  过期日期
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
                >
                  状态
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
                >
                  操作
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-100">
              <tr
                v-for="item in paginatedInventory"
                :key="item.id"
                :class="{
                  'bg-red-50': isStockWarning(item),
                  'bg-yellow-50':
                    isExpireWarning(item) && !isStockWarning(item),
                  'hover:bg-gray-50 transition-colors': true,
                }"
              >
                <td
                  class="px-4 py-3 whitespace-nowrap text-sm font-medium text-gray-900"
                >
                  {{ item.name || "未命名物品" }}
                  <span v-if="!item.name" class="text-red-500 text-xs"
                    >(无名称)</span
                  >
                </td>
                <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-500">
                  {{ item.category || "未分类" }}
                  <span v-if="!item.category" class="text-red-500 text-xs"
                    >(无分类)</span
                  >
                </td>
                <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-500">
                  {{ item.spec || "-" }}
                </td>
                <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-500">
                  {{ item.unitPrice ? item.unitPrice.toFixed(2) : "0.00" }}
                </td>
                <td
                  class="px-4 py-3 whitespace-nowrap text-sm font-medium"
                  :class="{ 'text-red-600': isStockWarning(item) }"
                >
                  {{ item.quantity }}
                </td>
                <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-500">
                  {{
                    ((item.unitPrice || 0) * (item.quantity || 0)).toFixed(2)
                  }}
                </td>
                <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-500">
                  {{ item.supplier || "-" }}
                </td>
                <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-500">
                  {{ item.warningValue || "-" }}
                </td>
                <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-500">
                  {{ item.expireDate || "-" }}
                </td>
                <td class="px-4 py-3 whitespace-nowrap text-sm">
                  <span
                    v-if="isStockWarning(item)"
                    class="text-red-600 bg-red-100 px-2 py-1 rounded text-xs"
                  >
                    库存不足
                  </span>
                  <span
                    v-else-if="isExpireWarning(item)"
                    class="text-yellow-600 bg-yellow-100 px-2 py-1 rounded text-xs"
                  >
                    保质期临近
                  </span>
                  <span v-else class="text-gray-500 text-xs">-</span>
                </td>
                <td class="px-4 py-3 whitespace-nowrap text-sm text-gray-500">
                  <div class="flex space-x-2">
                    <button
                      class="text-blue-600 hover:text-blue-800 p-1 rounded hover:bg-blue-50"
                      @click="openApplyModal(item)"
                      title="领用"
                    >
                      <i class="fas fa-hand-holding"></i>
                    </button>
                    <button
                      v-if="$hasPermission('SUPPLY:MANAGE')"
                      class="text-yellow-600 hover:text-yellow-800 p-1 rounded hover:bg-yellow-50"
                      @click="openEditModal(item)"
                      title="编辑"
                    >
                      <i class="fas fa-edit"></i>
                    </button>
                    <button
                      v-if="$hasPermission('SUPPLY:MANAGE')"
                      class="text-red-600 hover:text-red-800 p-1 rounded hover:bg-red-50"
                      @click="deleteItem(item.id)"
                      title="删除"
                    >
                      <i class="fas fa-trash"></i>
                    </button>
                    <!-- 新增报废按钮 -->
                    <button
                      v-if="$hasPermission('SUPPLY:MANAGE')"
                      class="text-red-600 hover:text-red-800 p-1 rounded hover:bg-red-50"
                      @click="openScrapModal(item)"
                      title="报废"
                    >
                      <i class="fas fa-ban"></i>
                    </button>
                  </div>
                </td>
              </tr>

              <!-- 空数据提示 -->
              <tr v-if="paginatedInventory.length === 0">
                <!-- 修复：colspan 与表头数量一致（11列） -->
                <td colspan="11" class="px-4 py-8 text-center text-gray-500">
                  <i class="fas fa-inbox text-4xl mb-2 block"></i>
                  暂无数据
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <!-- 闭合表格外层 div -->
      </div>

      <!-- 库存表格容器结束标签前添加分页组件 -->
      <div class="mt-4 flex justify-between items-center">
        <div class="text-sm text-gray-500">
          显示第 {{ (pagination.currentPage - 1) * pagination.pageSize + 1 }} 到
          {{
            Math.min(
              pagination.currentPage * pagination.pageSize,
              pagination.total
            )
          }}
          条记录， 共 {{ pagination.total }} 条记录
        </div>
        <div class="flex items-center space-x-2">
          <select
            class="px-2 py-1 border border-gray-300 rounded-md text-sm"
            v-model="pagination.pageSize"
            @change="handlePageSizeChange"
          >
            <option value="10">10条/页</option>
            <option value="20">20条/页</option>
            <option value="50">50条/页</option>
            <option value="100">100条/页</option>
          </select>
          <button
            class="px-3 py-1 border border-gray-300 rounded-md text-sm"
            :disabled="pagination.currentPage === 1"
            @click="handlePageChange(pagination.currentPage - 1)"
          >
            上一页
          </button>
          <span class="text-sm">
            第 {{ pagination.currentPage }} 页，共
            {{ Math.ceil(pagination.total / pagination.pageSize) }} 页
          </span>
          <button
            class="px-3 py-1 border border-gray-300 rounded-md text-sm"
            :disabled="
              pagination.currentPage >=
              Math.ceil(pagination.total / pagination.pageSize)
            "
            @click="handlePageChange(pagination.currentPage + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </div>

    <!-- 批量导入模态框 -->
    <ImportModal
      v-if="showImportModal"
      :show="showImportModal"
      :categoryIdMap="categoryIdMap"
      @close="closeImportModal"
      @submit="handleBatchImportSubmit"
    />

    <!-- 编辑物品模态框 -->
    <EditItemModal
      v-if="showEditModal"
      :show="showEditModal"
      :item="editItem"
      :categories="categories"
      :categoryIdMap="categoryIdMap"
      :operator-id="login_user.id"
      @close="showEditModal = false"
      @saved="handleEditSaved"
    />

    <!-- 我的申请模态框 -->
    <MyApplicationsModal
      v-if="showMyApplicationsModal"
      :show="showMyApplicationsModal"
      :applications="myApplications"
      @close="showMyApplicationsModal = false"
      @withdraw="handleWithdrawApplication"
    />

    <!-- 领用审批模态框 -->
    <ApprovalModal
      v-if="showApprovalModal"
      :show="showApprovalModal"
      :approvalApplications="approvalApplications"
      :logisticsApproverOptions="logisticsApproverOptions"
      :isSuperAdmin="isSuperAdmin"
      @close="showApprovalModal = false"
      @submit="handleApprovalSubmit"
    />

    <!-- 审批历史模态框 -->
    <div
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      v-if="showApprovalHistoryModal"
    >
      <div
        class="bg-white rounded-lg shadow-xl p-6 w-full max-w-4xl max-h-[80vh] overflow-y-auto"
      >
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-semibold">我的审批历史</h3>
          <button
            class="text-gray-500 hover:text-gray-700"
            @click="showApprovalHistoryModal = false"
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
                  物品名称
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
                >
                  申请人
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
                >
                  部门
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
                >
                  数量
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
                >
                  状态
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
                >
                  审批时间
                </th>
                <th
                  class="px-4 py-3 text-left text-xs font-medium text-blue-700 uppercase"
                >
                  审批意见
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-100">
              <tr v-for="item in approvalHistory" :key="item.id">
                <td class="px-4 py-3 text-sm">{{ item.itemName }}</td>
                <td class="px-4 py-3 text-sm">{{ item.applicant }}</td>
                <td class="px-4 py-3 text-sm">{{ item.department }}</td>
                <td class="px-4 py-3 text-sm">{{ item.quantity }}</td>
                <td class="px-4 py-3 text-sm">
                  <span
                    :class="{
                      'text-green-600': item.status === '已批准',
                      'text-red-600': item.status === '已驳回',
                    }"
                  >
                    {{ item.status }}
                  </span>
                </td>
                <td class="px-4 py-3 text-sm">{{ item.approveTime }}</td>
                <td class="px-4 py-3 text-sm">{{ item.comment || "-" }}</td>
              </tr>
              <tr v-if="approvalHistory.length === 0">
                <td colspan="7" class="px-4 py-8 text-center text-gray-500">
                  暂无审批记录
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 新增物品模态框组件 -->
    <AddItemModal
      v-if="showAddItemModal"
      :show="showAddItemModal"
      :categories="filteredCategories"
      :categoryIdMap="categoryIdMap"
      :has-storage-permission="hasStoragePermission"
      :is-it-center-user="isItCenterUser"
      :logistics-approvers="logisticsApproverOptions"
      :operator-id="login_user.id"
      @success="handleAddItemSuccess"
      @close="showAddItemModal = false"
    />

    <!-- 新增分类模态框组件 -->
    <CategoryModal
      v-if="showCategoryModal"
      :show="showCategoryModal"
      :categories="categories"
      @add="handleAddCategory"
      @delete="handleDeleteCategory"
      @close="showCategoryModal = false"
    />

    <!-- 领用申请模态框组件 -->
    <ApplyModal
      v-if="showApplyModal"
      :show="showApplyModal"
      :item="selectedItem"
      :departmentList="departmentList"
      :firstApproverOptions="firstApproverOptions"
      :defaultDepartmentId="userDepartmentId"
      :login_user="login_user"
      :categoryMap="categoryMap"
      @submit="handleApplySubmit"
      @close="showApplyModal = false"
    />
    <!-- 报废申请弹窗 -->
    <ScrapModal
      v-if="showScrapModal"
      :show="showScrapModal"
      :selected-item="scrapSelectedItem"
      :login_user="login_user"
      :departmentList="departmentList"
      :logisticsApproverOptions="logisticsApproverOptions"
      @close="showScrapModal = false"
      @success="handleScrapSuccess"
    />
    <!-- 入库审批弹窗 -->
    <StockInApprovalModal
      v-if="showStockInApprovalModal"
      :show="showStockInApprovalModal"
      :approvalApplications="stockInApprovalApplications"
      @close="showStockInApprovalModal = false"
      @submit="handleStockInApprovalSubmit"
    />
    <!-- 我的入库申请弹窗 -->
    <MyStockInApplicationsModal
      v-if="showMyStockInApplicationsModal"
      :show="showMyStockInApplicationsModal"
      :applications="myStockInApplications"
      @close="showMyStockInApplicationsModal = false"
    />
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>
<script>
import * as echarts from "echarts";
import {
  getInventoryList,
  deleteInventory,
  getCategoryList,
  deleteCategory,
  addCategory,
  batchAddInventory,
  submitConsumableApplication,
  getMyApplications,
  getPendingApprovalApplications,
  withdrawApplication,
  approveApplication,
  rejectApplication,
  approveFinalApplication,
  rejectFinalApplication,
  getApprovalHistory,
  //IT部入库申请相关
  getMyStockInApplications,
  getPendingStockInApprovals,
  approveStockIn,
  rejectStockIn,
} from "@/api/inventory";
import FooterComponent from "@/components/FooterComponent";
import HeaderComponent from "@/components/HeaderComponent";
import { getUserToken, removeToken, removeUserToken } from "@/utils/auth";
import { userApi } from "@/api/usermag";
import request from "@/utils/request";
import AddItemModal from "@/components/consumable/modals/AddItemModal.vue";
import ApplyModal from "@/components/consumable/modals/ApplyModal.vue";
import CategoryModal from "@/components/consumable/modals/CategoryModal.vue";
import ImportModal from "@/components/consumable/modals/ImportModal.vue";
import ApprovalModal from "@/components/consumable/modals/ApprovalModal.vue";
import EditItemModal from "@/components/consumable/modals/EditItemModal.vue";
import MyApplicationsModal from "@/components/consumable/modals/MyApplicationsModal.vue";
import MyStockInApplicationsModal from "@/components/consumable/modals/MyStockInApplicationsModal.vue";
import ScrapModal from "@/components/consumable/modals/ScrapModal.vue";
import StockInApprovalModal from "@/components/consumable/modals/StockInApprovalModal.vue";

export default {
  name: "ConsumableManage",
  components: {
    HeaderComponent,
    FooterComponent,
    AddItemModal,
    ApplyModal,
    CategoryModal,
    ImportModal,
    ApprovalModal,
    EditItemModal,
    MyApplicationsModal,
    MyStockInApplicationsModal, //IT部入库申请
    ScrapModal,
    StockInApprovalModal, //IT部入库审批
  },
  data() {
    return {
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0,
      },
      // 批量导入相关
      showImportModal: false,
      importFile: null,
      importPreviewData: [],
      username: "",
      roleName: "",
      userDepartmentId: "",
      // 数据存储
      inventoryData: [],
      categories: [],
      categoryMap: {},
      categoryIdMap: {},
      applications: [],
      departmentStats: {},
      departmentList: [], // 部门列表

      // 筛选条件
      categoryFilter: "",
      warningFilter: "", // 新增：预警类型筛选
      departmentFilter: "",
      selectedDepartment: "",
      departmentDetails: [],

      // 模态框状态
      showAddItemModal: false,
      showEditModal: false,
      showApplyModal: false,
      showScrapModal: false, //新增报废窗口
      scrapInitialGoods: null, //新增报废窗口
      showCategoryModal: false,

      selectedItem: null, // 添加这个，用于存储申领选中的物品
      firstApproverOptions: [],
      logisticsApprover: [], //后保部的人员
      logisticsApproverOptions: [], //后保部的全部人员

      // 新增：我的申请和审批相关
      showMyApplicationsModal: false,
      showApprovalModal: false,
      myApplications: [],
      approvalApplications: [],
      selectedApplication: null,
      //IT部入库审批相关
      showStockInApprovalModal: false,
      stockInApprovalApplications: [], // IT部入库审批列表
      // IT部入库申请相关
      myStockInApplications: [],
      showMyStockInApplicationsModal: false,
      // 审批历史相关
      showApprovalHistoryModal: false,
      approvalHistory: [],
      // 图表实例
      pieChart: null,
    };
  },

  computed: {
    filteredFullInventory() {
      return this.inventoryData.filter((item) => {
        const matchesCategory =
          !this.categoryFilter || item.category === this.categoryFilter;
        const matchesWarning =
          this.warningFilter === "" ||
          (this.warningFilter === "stock" && this.isStockWarning(item)) ||
          (this.warningFilter === "expire" && this.isExpireWarning(item));
        return matchesCategory && matchesWarning;
      });
    },
    paginatedInventory() {
      const filtered = this.filteredFullInventory;
      const start =
        (this.pagination.currentPage - 1) * this.pagination.pageSize;
      const end = start + this.pagination.pageSize;
      // eslint-disable-next-line vue/no-side-effects-in-computed-properties
      this.pagination.total = filtered.length;
      return filtered.slice(start, end);
    },

    // 判断是否是超级管理员
    isSuperAdmin() {
      console.log("isSuperAdmin:", this.login_user);
      return this.login_user.username == "super_admin";
    },

    // 核心：判断当前用户是否有权限入库受限分类
    hasStoragePermission() {
      const roleCodes = this.userRoles.map((item) => item.code || "");
      return roleCodes.some((code) =>
        ["ROLE_IT_CENTER", "ROLE_LOGISTICS", "ROLE_DEPT_SPECIAL"].includes(code)
      );
    },

    // 判断是否是信息中心用户
    isItCenterUser() {
      const roleCodes = this.userRoles.map((item) => item.code || "");
      return roleCodes.includes("ROLE_IT_CENTER");
    },
    // 过滤分类下拉框
    filteredCategories() {
      // 提取角色code
      const roleCodes = this.userRoles.map((item) => item.code || "");
      // 1. 判断是否有ROLE_LOGISTICS（后保部）角色
      const hasLogistics = roleCodes.includes("ROLE_LOGISTICS");
      // 2. 判断是否有信息中心角色
      const hasITCenter = roleCodes.includes("ROLE_IT_CENTER");

      // 同时有两个角色 → 显示全部
      if (hasLogistics && hasITCenter) {
        return this.categories;
      }
      // 仅信息中心 → 仅危化品、电子耗材
      else if (hasITCenter) {
        return this.categories.filter((cat) =>
          ["危化品", "电子耗材"].includes(cat)
        );
      }
      // 仅后保部 → 显示全部
      else if (hasLogistics) {
        return this.categories;
      }
      // 无权限 → 排除危化品、电子耗材
      else {
        return this.categories.filter(
          (cat) => !["电子耗材", "危化品"].includes(cat)
        );
      }
    },
  },
  created() {
    const storedUserInfo = getUserToken();
    if (storedUserInfo && storedUserInfo.user) {
      this.login_user = storedUserInfo.user;
      this.userRoles = storedUserInfo.roles || [];
      this.userPermissions = storedUserInfo.permissions || [];
      this.username = this.login_user.username || "";
      this.roleName = this.login_user.role || "";
      this.userDepartmentId = this.login_user.department_id || "";
      // 打印权限信息用于调试
      console.log("用户角色:", this.userRoles);
      console.log("用户权限:", this.userPermissions);
      console.log(
        "是否有SUPPLY:IT_ITEM权限:",
        this.userPermissions.includes("SUPPLY:IT_ITEM")
      );
      console.log(
        "是否有SUPPLY:MANAGE权限:",
        this.userPermissions.includes("SUPPLY:MANAGE")
      );
    } else {
      this.$message.warning("您尚未登录，请先登录！");
      this.$router.push("/login");
    }
    this.loadDepartments();
    this.loadData();
  },

  methods: {
    // 显示我的入库申请
    async showMyStockInApplications() {
      try {
        const response = await getMyStockInApplications();
        if (response.code === 200 || response.code === "200") {
          this.myStockInApplications = response.data || [];
          this.showMyStockInApplicationsModal = true;
        } else {
          this.$message.error("获取我的入库申请失败：" + response.message);
        }
      } catch (error) {
        console.error("获取我的入库申请失败:", error);
        this.$message.error("获取我的入库申请失败：" + error.message);
      }
    },
    // 显示入库审批列表
    async showStockInApprovalList() {
      try {
        const response = await getPendingStockInApprovals();
        if (response.code === 200 || response.code === "200") {
          this.stockInApprovalApplications = response.data || [];
          this.showStockInApprovalModal = true;
        } else {
          this.$message.error("获取入库审批列表失败：" + response.message);
        }
      } catch (error) {
        console.error("获取入库审批列表失败:", error);
        this.$message.error("获取入库审批列表失败：" + error.message);
      }
    },

    // 处理入库审批提交
    async handleStockInApprovalSubmit(data) {
      try {
        const { approvalId, action, remark } = data;

        if (action === "approve") {
          // 批准入库申请
          const response = await approveStockIn(approvalId, remark);
          if (response.code === 200 || response.code === "200") {
            this.$message.success("入库申请已批准！");
          } else {
            this.$message.error("批准失败：" + response.message);
          }
        } else {
          // 驳回入库申请
          const response = await rejectStockIn(approvalId, remark);
          if (response.code === 200 || response.code === "200") {
            this.$message.success("入库申请已驳回！");
          } else {
            this.$message.error("驳回失败：" + response.message);
          }
        }

        // 刷新审批列表
        await this.showStockInApprovalList();
      } catch (error) {
        console.error("处理入库审批失败:", error);
        this.$message.error("处理失败：" + error.message);
      }
    },
    // 打开新增物品弹窗（通用方法）
    async openAddItemModal() {
      // 只有IT中心用户需要加载审批人
      if (
        this.$hasPermission("SUPPLY:IT_ITEM") &&
        !this.$hasPermission("SUPPLY:MANAGE")
      ) {
        await this.loadLogisticsApprovers();
        if (this.logisticsApproverOptions.length === 0) {
          this.$message.warning("未找到后保部审批人");
          return;
        }
      }

      this.$nextTick(() => {
        this.showAddItemModal = true;
      });
    },
    goScrap() {
      this.$router.push({
        path: "/consumable/scrap",
      });
    },
    // 新增：跳转到公示管理界面
    goNotice() {
      this.$router.push({
        path: "/consumable/noticeManage",
      });
    },
    // 新增：跳转到出入库记录页面
    goToInOutRecords() {
      if (this.$route.name !== "InOutRecords") {
        this.$router.push({ name: "InOutRecords" });
      }
    },
    async loadDepartments() {
      const res = await userApi.getDepartmentList();
      this.departmentList = res.data || [];
    },
    filterInventory() {
      this.pagination.currentPage = 1;
    },

    handlePageSizeChange() {
      this.pagination.currentPage = 1;
    },

    handlePageChange(page) {
      this.pagination.currentPage = page;
    },

    resetPagination() {
      this.pagination.currentPage = 1;
      this.pagination.pageSize = 10;
    },
    handleLogout() {
      removeUserToken();
      this.login_user = null;
      removeToken();
      this.$router.push("/login");
    },

    handleNavigate(routeName) {
      this.$router.push({ name: routeName });
    },

    handleAddItemSuccess() {
      this.loadData();
      this.$message.success("物品添加成功，列表已更新！");
      this.showAddItemModal = false; // 确保模态框关闭
    },

    // 打开报废弹窗
    async openScrapModal(item) {
      this.scrapSelectedItem = item;
      // 1. 等待后保部人员加载完成
      await this.loadLogisticsApprovers();
      // 2. 等Vue把数据更新到响应式系统，再显示弹窗
      this.$nextTick(() => {
        this.showScrapModal = true;
      });
    },

    // 报废申请提交成功回调
    handleScrapSuccess() {
      this.loadData(); // 刷新库存数据
      this.$message.success("报废申请已提交，等待审核");
    },

    async loadData() {
      try {
        const categoryRes = await getCategoryList();
        if (categoryRes.code === 200 || categoryRes.code === "200") {
          this.categoryMap = {};
          this.categoryIdMap = {};
          this.categories = [];
          if (Array.isArray(categoryRes.data)) {
            categoryRes.data.forEach((cat) => {
              if (cat.id && cat.categoryName) {
                this.categoryMap[cat.id] = cat.categoryName;
                this.categoryIdMap[cat.categoryName] = cat.id;
                this.categories.push(cat.categoryName);
              }
            });
          }
        } else {
          this.$message.error("获取分类数据失败：" + categoryRes.message);
        }

        const inventoryRes = await getInventoryList();
        if (inventoryRes.code === 200 || inventoryRes.code === "200") {
          this.inventoryData = inventoryRes.data.map((item) => {
            const categoryName =
              this.categoryMap[item.categoryId] || "未知分类";
            return {
              id: item.id,
              name: item.itemName || item.name,
              category: categoryName,
              categoryId: item.categoryId,
              spec: item.spec,
              supplier: item.supplier || "", // 新增供货商字段
              quantity: item.quantity,
              warningValue: item.warningValue,
              unit: item.unit,
              brand: item.brand,
              storageLocation: item.storageLocation,
              status: item.status,
              purchaseDate: item.purchaseDate,
              expireDate: item.expireDate,
              unitPrice: item.unitPrice,
              createdAt: item.createdAt,
            };
          });
        } else {
          this.$message.error("获取库存数据失败：" + inventoryRes.message);
        }
        this.validateData();
      } catch (error) {
        console.error("加载数据失败:", error);
        this.$message.error("数据加载失败: " + error.message);
      }
      this.resetPagination();
    },

    validateData() {
      console.group("数据验证");
      console.log("库存数据:", this.inventoryData);
      console.log("分类映射:", this.categoryMap);
      console.log("分类ID映射:", this.categoryIdMap);
      if (this.inventoryData.length > 0) {
        this.inventoryData.forEach((item, index) => {
          console.log(`物品${index}:`, {
            名称: item.name,
            分类: item.category,
            分类ID: item.categoryId,
            映射结果: this.categoryMap[item.categoryId],
          });
        });
      }
      console.groupEnd();
    },

    // 新增：库存预警判断
    isStockWarning(item) {
      return item.warningValue !== null && item.quantity <= item.warningValue;
    },
    // 新增：保质期预警判断（30天内到期）
    isExpireWarning(item) {
      if (!item.expireDate) return false;
      const expireDate = new Date(item.expireDate);
      const now = new Date();
      const diffDays = Math.ceil((expireDate - now) / (1000 * 60 * 60 * 24));
      return diffDays > 0 && diffDays <= 30;
    },

    getStatusClass(status) {
      switch (status) {
        case "待审批":
          return "text-yellow-600";
        case "已批准":
          return "text-blue-600";
        case "已发放":
          return "text-green-600";
        default:
          return "";
      }
    },

    openApplyModal(item) {
      console.log("item", item);
      this.selectedItem = item; // 保存选中的物品

      // 先设置默认值，避免空值
      this.firstApproverOptions = [];

      // 异步加载审批人选项
      this.loadApproverOptions(item.categoryId);

      // 延迟显示模态框，确保数据加载完成
      setTimeout(() => {
        this.showApplyModal = true;
      }, 100);
    },
    // 处理领用申请提交
    async handleApplySubmit(applicationData) {
      try {
        const response = await submitConsumableApplication(applicationData);

        if (response.code == 200 || response.code == "200") {
          this.$message.success("领用申请已提交，等待审批！");
          await this.loadData(); // 重新加载数据
        } else {
          this.$message.error(
            "提交失败：" + (response.message || "服务器异常")
          );
        }
      } catch (error) {
        console.error("提交领用申请失败详情：", error);
        if (error.response && error.response.data) {
          this.$message.error("提交失败：" + error.response.data.message);
        } else {
          this.$message.error("网络错误，提交失败，请重试！");
        }
      }
    },
    async loadApproverOptions(itemCategory) {
      try {
        console.log("itemCategory:", itemCategory);
        const resp = await userApi.getFirstApprover(itemCategory);
        // 过滤掉当前用户
        this.firstApproverOptions = resp.data.filter(
          (approver) => approver.id !== this.login_user?.id
        );
        console.log("firstApproverOptions:", this.firstApproverOptions);
      } catch (e) {
        console.error("加载审批人失败:", e);
        this.firstApproverOptions = [];
      }
    },
    async loadLogisticsApprovers() {
      console.log("===== 开始加载后保部人员 =====");
      try {
        // 这里只要是后保部角色都可以，还有后保部指定人员
        let resss = await userApi.getUsersByRole([
          "ROLE_DEPT_SPECIAL",
          "ROLE_LOGISTICS",
        ]);
        console.log("后保部人员接口返回：", resss); // 新增：打印接口返回数据
        if (resss.code == 200 || resss.code == "200") {
          // 过滤掉当前用户
          this.logisticsApproverOptions = resss.data.filter(
            (approver) => approver.id !== this.login_user?.id
          );
        }
        console.log("过滤后的后保部人员：", this.logisticsApproverOptions); // 新增：打印最终列表
      } catch (error) {
        console.error("加载后保部审批人失败:", error);
        this.$message.error("加载最终审批人列表失败");
      }
    },

    // 辅助方法：根据分类ID获取耗材类型electronic/experimental/general
    getConsumableType(categoryId) {
      // 这里根据实际情况判断耗材类型，可以根据分类名称的关键词来区分
      if (categoryId == 2) {
        return "electronic";
      } else if (categoryId == 3) {
        return "experimental";
      }
      return "general"; // 默认
    },

    // 打开编辑模态框（带分类刷新）
    openEditModal(item) {
      this.editItem = {
        ...item,
        expireDate: item.expireDate || "",
        purchaseDate:
          item.purchaseDate || new Date().toISOString().split("T")[0],
        unit: item.unit || "个",
      };
      this.showEditModal = true;
    },
    // 处理编辑保存成功
    async handleEditSaved() {
      await this.loadData();
    },

    async deleteItem(id) {
      this.$confirm("确定要删除这个物品吗？", "确认删除", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
        customClass: "delete-confirm-dialog",
      })
        .then(async () => {
          try {
            const response = await deleteInventory(id);
            if (response.code === 200 || response.code === "200") {
              this.$message.success("物品删除成功！");
              await this.loadData();
            } else {
              this.$message.error("删除物品失败：" + response.message);
            }
          } catch (error) {
            console.error("删除物品失败:", error);
            this.$message.error("删除物品失败");
          }
        })
        .catch(() => {
          // 用户点击取消，不做任何操作
        });
    },

    async handleAddCategory(categoryName) {
      const newCategory = categoryName.trim();
      if (newCategory) {
        try {
          const response = await addCategory(newCategory);
          if (response.code === 200 || response.code === "200") {
            if (!this.categories.includes(newCategory)) {
              this.categories.push(newCategory);
              this.newCategoryName = "";
              this.$message.success("分类添加成功！");
              await this.loadData();
            } else {
              this.$message.warning("该分类已存在！");
            }
          } else {
            this.$message.error("分类添加失败：" + response.message);
          }
        } catch (error) {
          console.error("添加分类失败:", error);
          this.$message.error("分类添加失败");
        }
      }
    },

    async handleDeleteCategory(category) {
      try {
        const categoryId = this.categoryIdMap[category];
        if (!categoryId) {
          this.$message.error("未找到该分类的ID");
          return;
        }

        // 检查该分类下是否有物品
        const hasItems = this.inventoryData.some(
          (item) => item.categoryId === categoryId
        );
        if (hasItems) {
          this.$message.error(
            "该分类下存在物品，请先删除或转移物品后再删除分类"
          );
          return;
        }

        // 直接调用 API（确认对话框已在 CategoryModal 组件中处理）
        const response = await deleteCategory(categoryId);

        if (response.code === 200 || response.code === "200") {
          this.$message.success("分类删除成功！");
          await this.loadData(); // 重新加载数据以获取更新后的分类列表
        } else {
          this.$message.error("分类删除失败：" + response.message);
        }
      } catch (error) {
        console.error("删除分类失败:", error);
        this.$message.error("分类删除失败：" + error.message);
      }
    },

    async exportExcel() {
      try {
        // 提供给用户选择：直接下载或导出到MinIO
        this.$confirm("请选择导出方式：", "导出Excel", {
          confirmButtonText: "直接下载",
          cancelButtonText: "导出到MinIO",
          distinguishCancelAndClose: true,
          type: "info",
        })
          .then(() => {
            // 直接下载
            this.downloadExcelDirectly();
          })
          .catch((action) => {
            if (action === "cancel") {
              // 导出到MinIO
              this.exportToMinIO();
            }
          });
      } catch (error) {
        console.error("导出失败:", error);
        this.$message.error("导出失败：" + error.message);
      }
    },

    // 直接下载Excel
    async downloadExcelDirectly() {
      try {
        const XLSX = await import("xlsx");
        const { saveAs } = await import("file-saver");

        const exportData = this.filteredFullInventory;
        console.log(exportData);
        if (!exportData || exportData.length === 0) {
          this.$message.warning("当前没有可导出的库存数据！");
          return;
        }

        const wsData = [
          [
            "物品名称",
            "分类",
            "型号规格",
            "品牌",
            "供货商",
            "单价(元)",
            "库存数量",
            "总金额(元)",
            "预警值",
            "入库日期",
            "过期日期",
            "单位",
          ],
          ...exportData.map((item) => [
            item.name || "未命名物品",
            item.category || "未分类",
            item.spec || "-",
            item.brand || "-",
            item.supplier || "-", // 新增供货商数据
            item.unitPrice ? item.unitPrice.toFixed(2) : "0.00",
            item.quantity || 0,
            ((item.unitPrice || 0) * (item.quantity || 0)).toFixed(2),
            item.warningValue || "-",
            item.purchaseDate || "-",
            item.expireDate || "-",
            item.unit || "个",
          ]),
        ];

        const wb = XLSX.utils.book_new();
        const ws = XLSX.utils.aoa_to_sheet(wsData);
        const wscols = [
          { wch: 15 },
          { wch: 10 },
          { wch: 20 },
          { wch: 10 },
          { wch: 10 },
          { wch: 10 },
          { wch: 12 },
          { wch: 8 },
          { wch: 12 },
          { wch: 12 },
          { wch: 6 },
        ];
        ws["!cols"] = wscols;

        XLSX.utils.book_append_sheet(wb, ws, "库存记录");
        const wbout = XLSX.write(wb, { bookType: "xlsx", type: "array" });
        saveAs(
          new Blob([wbout], { type: "application/octet-stream" }),
          `库存记录_${new Date().toISOString().slice(0, 10)}.xlsx`
        );

        this.$message.success("Excel导出成功！");
      } catch (error) {
        console.error("直接下载Excel失败:", error);
        this.$message.error("下载失败：" + error.message);
      }
    },
    // 导出到MinIO
    async exportToMinIO() {
      this.$message.info("正在导出到MinIO，请稍候...");

      try {
        const response = await request.get("/consumable-goods/export/minio");

        if (response.code === 200 || response.code === "200") {
          const downloadUrl = response.data;
          this.$message.success("导出到MinIO成功！");

          // 提供下载链接给用户
          this.$confirm("已成功导出到MinIO，是否立即下载？", "导出成功", {
            confirmButtonText: "立即下载",
            cancelButtonText: "稍后下载",
            type: "success",
          })
            .then(() => {
              window.open(downloadUrl, "_blank");
            })
            .catch(() => {
              // 用户选择稍后下载
              this.$message.info(
                "您可以在需要时通过下载链接访问：" + downloadUrl
              );
            });
        } else {
          this.$message.error("导出到MinIO失败：" + response.message);
        }
      } catch (error) {
        console.error("导出到MinIO失败:", error);
        this.$message.error(
          "导出失败：" + (error.response?.data?.message || error.message)
        );
      }
    },

    // 处理批量导入提交
    async handleBatchImportSubmit({ previewData, categoryIdMap }) {
      if (previewData.length === 0) {
        this.$message.warning("没有可导入的有效数据");
        return;
      }

      try {
        this.$message.info("正在提交导入数据，请稍候...");

        // 1. 批量数据转换 + 严格校验
        const importDataList = [];
        for (let i = 0; i < previewData.length; i++) {
          const item = previewData[i];
          const rowNum = i + 2;

          // 校验必填项
          if (!item.name.trim()) {
            throw new Error(`第 ${rowNum} 行：物品名称不能为空`);
          }
          if (!item.category.trim()) {
            throw new Error(`第 ${rowNum} 行：分类不能为空`);
          }
          if (!item.purchaseDate.trim()) {
            throw new Error(`第 ${rowNum} 行：入库日期不能为空`);
          }
          if (item.quantity <= 0) {
            throw new Error(`第 ${rowNum} 行：数量必须大于0`);
          }

          // 校验分类是否存在
          const categoryId = categoryIdMap[item.category.trim()];
          if (!categoryId) {
            throw new Error(
              `第 ${rowNum} 行：分类"${item.category}"不存在，请先添加`
            );
          }

          // 格式化日期
          const purchaseDate = new Date(item.purchaseDate.trim());
          if (isNaN(purchaseDate.getTime())) {
            throw new Error(
              `第 ${rowNum} 行：入库日期格式错误（需为 yyyy-MM-dd）`
            );
          }
          const formatPurchaseDate = purchaseDate.toISOString().split("T")[0];

          // 处理过期日期
          let expireDate = null;
          if (item.expireDate && item.expireDate.trim() !== "") {
            const expire = new Date(item.expireDate.trim());
            if (isNaN(expire.getTime())) {
              throw new Error(
                `第 ${rowNum} 行：过期日期格式错误（需为 yyyy-MM-dd）`
              );
            }
            expireDate = expire.toISOString().split("T")[0];
          }

          // 添加到批量数据列表（字段名与后端完全一致）
          importDataList.push({
            itemName: item.name.trim(),
            categoryId: categoryId,
            spec: item.spec || "",
            brand: item.brand || "",
            supplier: item.supplier || "",
            unitPrice: item.unitPrice || 0.0,
            quantity: item.quantity,
            warningValue: item.warningValue || null,
            purchaseDate: formatPurchaseDate,
            unit: item.unit || "个",
            status: "1",
            expireDate: expireDate,
            operatorId: this.login_user.id,
          });
        }

        // 2. 提交批量数据
        const response = await batchAddInventory(importDataList);
        if (response.code === 200 || response.code === "200") {
          this.$message.success(
            `批量导入成功！共导入${importDataList.length}条数据`
          );
          await this.loadData();
        } else {
          this.$message.error(
            "批量导入失败：" + (response.message || "未知错误")
          );
        }
      } catch (error) {
        console.error("批量导入失败详情：", error);
        this.$message.error("批量导入失败：" + error.message);
      }
    },
    closeImportModal() {
      this.showImportModal = false;
    },

    initPieChart() {
      if (this.pieChart) {
        this.pieChart.dispose();
      }

      const chartDom = this.$refs.pieChart;
      if (!chartDom) return;

      this.pieChart = echarts.init(chartDom);

      const departmentData = Object.keys(this.departmentStats).map((dept) => {
        const total = this.departmentStats[dept].reduce(
          (sum, item) => sum + item.quantity,
          0
        );
        return { name: dept, value: total };
      });

      const option = {
        title: {
          text: "各部门领用比例",
          left: "center",
        },
        tooltip: {
          trigger: "item",
          formatter: "{a} <br/>{b}: {c} ({d}%)",
        },
        legend: {
          orient: "vertical",
          left: "left",
          data: Object.keys(this.departmentStats),
        },
        series: [
          {
            name: "领用数量",
            type: "pie",
            radius: "50%",
            data: departmentData,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: "rgba(0, 0, 0, 0.5)",
              },
            },
            label: {
              formatter: "{b}: {c} ({d}%)",
            },
          },
        ],
      };

      this.pieChart.setOption(option);

      window.addEventListener("resize", () => {
        this.pieChart && this.pieChart.resize();
      });
    },

    // 新增：获取状态文本
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

    // 新增：获取我的申请数据
    async loadMyApplications() {
      try {
        // 调用API获取我的申请列表，传入当前用户ID
        const applicantId = this.login_user?.id;
        if (!applicantId) {
          this.$message.error("无法获取用户信息，请重新登录");
          return;
        }

        const response = await getMyApplications(applicantId);

        if (response.code === 200 || response.code === "200") {
          this.myApplications = response.data || [];
          return true;
        } else {
          this.$message.error(
            "获取申请列表失败：" + (response.message || "未知错误")
          );
          return false;
        }
      } catch (error) {
        console.error("获取我的申请失败：", error);
        this.$message.error("获取申请列表失败：" + error.message);
        return false;
      }
    },

    // 显示我的申请（修改后的版本）
    async showMyApplications() {
      const success = await this.loadMyApplications();
      if (success) {
        this.showMyApplicationsModal = true;
      }
    },

    // 处理撤回申请（保留逻辑，但从子组件调用）
    async handleWithdrawApplication(applicationId) {
      try {
        await this.$confirm("确定要撤回该申请吗？", "确认撤回", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });

        const response = await withdrawApplication(applicationId);

        if (response.code === 200 || response.code === "200") {
          this.$message.success("申请已成功撤回");
          // 重新加载我的申请列表
          await this.loadMyApplications();
        } else {
          this.$message.error(
            "撤回申请失败：" + (response.message || "未知错误")
          );
        }
      } catch (error) {
        if (error !== "cancel") {
          console.error("撤回申请失败：", error);
          this.$message.error("撤回申请失败：" + error.message);
        }
      }
    },

    // 显示审批列表
    async showApprovalList() {
      console.log("1 showApprovalList方法被调用");
      console.log("2user", this.login_user);
      console.log("3userid", this.login_user?.id);
      try {
        const userId = this.login_user?.id;
        if (!userId) {
          this.$message.error("无法获取用户信息，请重新登录");
          return;
        }
        // 调用API获取待我审批申请列表
        let response = await getPendingApprovalApplications(userId);
        console.log("待我审批申请列表：", response);
        if (response.code == 200 || response.code == "200") {
          // 修复：过滤掉consumableApption为null/undefined的项
          this.approvalApplications = (response.data || []).filter((app) => {
            return (
              app.consumableApption !== null &&
              app.consumableApption !== undefined
            );
          });
          this.showApprovalModal = true;
          // 加载后保部审批人列表
          await this.loadLogisticsApprovers();
        } else {
          this.$message.error(
            "获取审批列表失败：" + (response.message || "未知错误")
          );
        }
      } catch (error) {
        console.error("获取审批列表失败：", error);
        this.$message.error("获取审批列表失败：" + error.message);
      }
    },

    // 处理审批提交
    async handleApprovalSubmit(data) {
      try {
        let response = null;
        const {
          taskId,
          applicationId,
          action,
          reason,
          firstOrfinal,
          logisticsApprover,
          actualDistributeQuantity,
        } = data;

        if (firstOrfinal) {
          // 最终审批
          const requestData = {
            taskId,
            applicationId,
            finalApproved: action === "approve",
            comment: reason,
            actualDistributeQuantity:
              action === "approve" ? actualDistributeQuantity : undefined,
          };

          if (action === "approve") {
            response = await approveFinalApplication(requestData);
          } else {
            response = await rejectFinalApplication(requestData);
          }
        } else {
          // 一级审批
          const requestData = {
            taskId,
            applicationId,
            firstApproved: action === "approve",
            comment: reason,
            logisticsApprover:
              action === "approve" ? logisticsApprover : undefined,
          };

          if (action === "approve") {
            response = await approveApplication(requestData);
          } else {
            response = await rejectApplication(requestData);
          }
        }

        if (response.code == 200) {
          this.$message.success(
            `${action === "approve" ? "审批通过" : "驳回"}成功`
          );
          await this.showApprovalList(); // 刷新列表
        } else {
          this.$message.error(
            `${action === "approve" ? "审批通过" : "驳回"}失败: ` +
              response.message
          );
        }
      } catch (error) {
        this.$message.error(`审批操作失败: ` + error.message);
      }
    },
    // 显示审批历史
    async showApprovalHistory() {
      try {
        const approverId = this.login_user?.id;
        if (!approverId) {
          this.$message.error("无法获取用户信息，请重新登录");
          return;
        }

        // 调用API获取真实的领用审批历史数据
        const response = await getApprovalHistory(approverId);
        alert("Approval History Response:", response);
        if (response.code === 200 || response.code === "200") {
          // 格式化数据以供表格显示
          this.approvalHistory = response.data.map((app) => ({
            id: app.id,
            itemName: app.itemName,
            applicant: app.applicantName || "未知申请人",
            department: app.applicantDept || "未知部门",
            quantity: app.quantity,
            status: this.getStatusText(app.finalStatus),
            approveTime: app.applyTime
              ? new Date(app.applyTime).toLocaleString()
              : "未知时间",
            comment:
              app.assigneeComment || app.logisticsComment || "无审批意见",
          }));

          this.showApprovalHistoryModal = true;
        } else {
          this.$message.error(
            "获取审批历史失败：" + (response.message || "未知错误")
          );
        }
      } catch (error) {
        console.error("获取审批历史失败:", error);
        this.$message.error("获取审批历史失败: " + error.message);
      }
    },
  },

  mounted() {
    this.loadData();

    // 检查路由参数，如果包含 showApproval=true，则自动打开待审批列表
    if (this.$route.query.showApproval === "true") {
      this.showApprovalList();
    }
  },

  beforeUnmount() {
    if (this.pieChart) {
      this.pieChart.dispose();
    }
  },
};
</script>
<style lang="scss" scoped>
$headHeight: 148px;
$bottomHeight: 161px;
/* 新增：页面整体间距优化 */
.oa-homepage {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fcfcfc;
}
.content {
  flex: 1; /* 关键：自动填充所有可用空间 */
  overflow-y: auto; /* 内容多时允许滚动 */
  padding: 20px 0; /* 上下留白 */
}

/* 内容居中容器 */
.content-center {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto; /* 水平居中 */
  padding: 0 15px; /* 左右边距 */
  box-sizing: border-box;
}
/* 页脚：被 flex 自动推到底部 */
.footer-component {
  margin-top: auto; /* 关键：在 flex 容器中沉底 */
  width: 100%;
}
/* 新增：表格容器间距优化 */
.bg-white.rounded-lg.shadow-md {
  margin: 12px 0;
}

/* 按钮容器：保留换行，优化间距 */
.flex.flex-nowrap.space-x-4 {
  margin: 10px 0 !important;
  flex-wrap: wrap !important;
  gap: 6px !important; /* 缩小按钮之间的间距 */
}
/* 按钮样式：紧凑化，统一高度 */
.flex.flex-nowrap.space-x-4 button {
  padding: 6px 10px !important; /* 比之前更紧凑 */
  font-size: 12px !important; /* 缩小按钮文字 */
  height: 32px !important; /* 统一按钮高度，更整齐 */
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
/* 按钮内图标：缩小间距 */
.flex.flex-nowrap.space-x-4 button i {
  margin-right: 4px !important; /* 图标和文字间距缩小 */
  font-size: 12px !important; /* 图标也缩小 */
}

/* 表格内边距优化 */
table.min-w-full th,
table.min-w-full td {
  padding: 12px 16px;
}
/* 响应式适配 */
@media (max-width: 768px) {
  .content {
    padding: 10px 0;
  }
  .content-center {
    padding: 0 10px;
  }
}
/* 标题区域优化 */
/* 原标题样式 */
h2.text-xl.font-semibold.text-gray-700 {
  font-size: 1rem !important; /* 原 text-xl 是 1.25rem，缩小到 1rem */
  font-weight: 600;
  margin-right: 0.5rem !important; /* 减少右侧间距 */
}
/* 筛选下拉框样式 */
select.px-3.py-2.border.border-gray-300 {
  padding: 6px 10px !important; /* 和按钮内边距一致 */
  height: 32px !important; /* 和按钮高度一致 */
  font-size: 12px !important; /* 缩小文字 */
  min-width: 100px !important; /* 缩小最小宽度 */
}

/* 筛选栏容器：允许换行，间距更紧凑 */
.flex.space-x-2 {
  gap: 6px !important;
  flex-wrap: wrap;
  margin-bottom: 6px;
}
/* 标题所在的 flex 容器优化 */
.flex.flex-col.md\:flex-row.justify-between.items-start.md\:items-center {
  align-items: flex-start !important; /* 小屏幕上顶部对齐，更整洁 */
  @media (min-width: 768px) {
    align-items: center !important; /* 大屏依然垂直居中 */
    gap: 12px; /* 增加元素之间的间距，避免挤在一起 */
  }
}
/* 下拉按钮组样式，保持和原有按钮一致 */
.dropdown-btn-group {
  button {
    // padding: 6px 10px !important;
    // font-size: 12px !important;
    // height: 32px !important;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border: none;
    cursor: pointer;
  }

  /* 下拉菜单样式适配，hover效果统一 */
  .el-dropdown-menu {
    padding: 8px 0;
    .el-dropdown-item {
      display: flex;
      align-items: center;
      padding: 8px 16px;
      font-size: 12px;
      i {
        margin-right: 4px;
        font-size: 12px;
      }
      &:hover {
        background-color: #f5f7fa;
      }
    }
  }
}
.common-small-btn {
  padding: 6px 10px !important;
  font-size: 12px !important;
  height: 32px !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  border: none !important;
  cursor: pointer !important;
}
/* 移除原按钮的强制不换行，优化响应式 */
.flex-nowrap {
  flex-wrap: wrap !important;
  white-space: normal !important;
}

.warning {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(239, 68, 68, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(239, 68, 68, 0);
  }
}
</style>
