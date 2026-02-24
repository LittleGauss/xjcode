<template>
  <div class="home-container">
    <!-- 使用通用页头 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @logout="handleLogout"
      @navigate="handleNavigate"
    />
    <div class="bg-gray-100 min-h-screen flex">
      <!-- 左侧导航栏 -->
      <div class="w-64 bg-white shadow-md flex flex-col">
        <!-- Logo区域 -->

        <!-- 导航菜单 -->
        <div class="flex-1 overflow-y-auto">
          <div class="p-4">
            <!-- 合同管理 -->
            <div class="mb-2">
              <div
                class="sidebar-item w-full px-5 py-7 rounded-md cursor-pointer flex items-center justify-between text-gray-700 hover:bg-gray-100 hover:bg-opacity-80 transition-colors select-none text-base tracking-wide"
                :class="{ active: contractMenuOpen }"
                @click="toggleContractMenu"
                role="button"
                tabindex="0"
                @keydown.enter.prevent="toggleContractMenu"
                @keydown.space.prevent="toggleContractMenu"
              >
                <div class="flex items-center">
                  <i class="fas fa-file-contract mr-3"></i>
                  <span>合同管理</span>
                </div>
                <i
                  class="fas fa-chevron-down text-xs transition-transform duration-200"
                  :class="{ 'rotate-180': contractMenuOpen }"
                ></i>
              </div>

              <!-- 子菜单 -->
              <div
                class="submenu pl-8 mt-1"
                :class="{ active: contractMenuOpen }"
                v-show="contractMenuOpen"
              >
                <div
                  class="w-full px-5 py-2 rounded-md cursor-pointer flex items-center text-gray-700 hover:bg-gray-100 hover:bg-opacity-70 transition-colors select-none"
                  v-if="$hasPermission('CONTRACT:UPLOAD')"
                  @click="navigateTo('create-contract')"
                  role="button"
                  tabindex="0"
                  @keydown.enter.prevent="navigateTo('create-contract')"
                  @keydown.space.prevent="navigateTo('create-contract')"
                >
                  <i class="fas fa-file-circle-plus mr-3"></i>
                  <span>创建合同</span>
                </div>
                <div
                  class="w-full px-5 py-2 rounded-md cursor-pointer flex items-center text-gray-700 hover:bg-gray-100 hover:bg-opacity-70 transition-colors select-none"
                  v-if="
                    $hasPermission(['CONTRACT:VIEW_DEPT', 'CONTRACT:VIEW_ALL'])
                  "
                  @click="navigateTo('contract-list')"
                  role="button"
                  tabindex="0"
                  @keydown.enter.prevent="navigateTo('contract-list')"
                  @keydown.space.prevent="navigateTo('contract-list')"
                >
                  <i class="fas fa-list-ul mr-3"></i>
                  <span>合同列表</span>
                </div>
                <div
                  class="w-full px-5 py-2 rounded-md cursor-pointer flex items-center text-gray-700 hover:bg-gray-100 hover:bg-opacity-70 transition-colors select-none"
                  v-if="
                    $hasPermission(['CONTRACT:VIEW_DEPT', 'CONTRACT:VIEW_ALL'])
                  "
                  @click="navigateTo('contract-statistics')"
                  role="button"
                  tabindex="0"
                  @keydown.enter.prevent="navigateTo('contract-statistics')"
                  @keydown.space.prevent="navigateTo('contract-statistics')"
                >
                  <i class="fas fa-chart-bar mr-3"></i>
                  <span>合同统计</span>
                </div>
                <div
                  class="w-full px-5 py-2 rounded-md cursor-pointer flex items-center text-gray-700 hover:bg-gray-100 hover:bg-opacity-70 transition-colors select-none"
                  v-if="$hasPermission('CONTRACT:LEGAL_REVIEW')"
                  @click="navigateTo('contract-review')"
                  role="button"
                  tabindex="0"
                  @keydown.enter.prevent="navigateTo('contract-review')"
                  @keydown.space.prevent="navigateTo('contract-review')"
                >
                  <i class="fas fa-gavel mr-3"></i>
                  <span>合同审核</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 主内容区 -->
      <div class="flex-1 flex flex-col overflow-hidden">
        <!-- 顶部导航 -->
        <div class="bg-white shadow-sm p-4 flex items-center justify-between">
          <div class="flex items-center">
            <h1 class="text-xl font-semibold text-gray-800 ml-4">
              合同管理系统
            </h1>
          </div>
        </div>

        <!-- 内容区域 -->
        <div class="flex-1 overflow-y-auto p-6 bg-gray-50">
          <!-- 欢迎内容 -->
          <div
            v-if="currentPage === 'home'"
            class="bg-white rounded-lg shadow-sm p-6"
          >
            <h2 class="text-xl font-semibold text-gray-800 mb-6">
              欢迎使用合同管理系统
            </h2>
            <p class="text-gray-600 mb-4">
              请从左侧导航栏选择您要操作的功能模块。
            </p>
            <div class="grid grid-cols-1 gap-6">
              <div class="bg-blue-50 p-4 rounded-lg border border-blue-100">
                <div class="flex items-center mb-3">
                  <i
                    class="fas fa-file-contract text-blue-500 mr-3 text-xl"
                  ></i>
                  <h3 class="font-medium text-blue-800">合同管理</h3>
                </div>
                <p class="text-blue-600 text-sm">
                  管理您的所有合同文档，查看合同列表和统计数据。
                </p>
              </div>
            </div>
          </div>

          <!-- 创建合同内容 -->
          <div
            v-if="currentPage === 'create-contract'"
            class="bg-white rounded-lg shadow-sm p-6"
          >
            <h2 class="text-xl font-semibold text-gray-800 mb-6">创建新合同</h2>

            <el-form
              :model="newContract"
              :rules="contractRules"
              ref="contractForm"
              label-width="120px"
              class="space-y-4"
            >
              <el-form-item label="合同名称" prop="contractName">
                <el-input
                  v-model="newContract.contractName"
                  placeholder="请输入合同名称"
                ></el-input>
              </el-form-item>

              <el-form-item label="申请人" prop="applicantName">
                <el-input
                  v-model="newContract.applicantName"
                  disabled
                ></el-input>
              </el-form-item>

              <el-form-item label="提交部门" prop="department">
                <el-select
                  v-model="newContract.department"
                  placeholder="请选择提交部门"
                >
                  <el-option
                    v-for="d in departments"
                    :key="d.id || d.name"
                    :label="d.name"
                    :value="d.name"
                  >
                  </el-option>
                  <el-option v-if="departments.length === 0" value="" disabled>
                    正在加载部门...
                  </el-option>
                </el-select>
              </el-form-item>

              <el-form-item label="审批人设置" required>
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item prop="gmUser">
                      <el-select
                        v-model="newContract.gmUser"
                        placeholder="综合管理部会签"
                        filterable
                        clearable
                        :loading="approverLoading"
                        style="width: 100%"
                      >
                        <el-option
                          v-for="opt in gmUserOptions"
                          :key="opt.value"
                          :label="opt.label"
                          :value="opt.value"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item prop="legalUser">
                      <el-select
                        v-model="newContract.legalUser"
                        placeholder="行政办(法务)"
                        filterable
                        clearable
                        :loading="approverLoading"
                        style="width: 100%"
                      >
                        <el-option
                          v-for="opt in legalUserOptions"
                          :key="opt.value"
                          :label="opt.label"
                          :value="opt.value"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form-item>
              <el-form-item label="上传合同文件">
                <input
                  type="file"
                  @change="handleFileUpload"
                  class="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
                />
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  @click="submitContract"
                  :disabled="loading"
                >
                  {{ loading ? "提交中..." : "提交审核" }}
                </el-button>
                <el-button @click="resetContractForm">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 合同审核内容 -->
          <div
            v-if="currentPage === 'contract-review'"
            class="bg-white rounded-lg shadow-sm p-6"
          >
            <h2 class="text-xl font-semibold text-gray-800 mb-6">合同审核</h2>
            <div class="flex items-center mb-4 space-x-4">
              <input
                v-model="searchKeyword"
                @input="filterContracts"
                type="text"
                placeholder="搜索合同名称"
                class="border border-gray-300 rounded-md py-2 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                style="width: 220px"
              />
              <button
                @click="loadPendingContracts"
                class="px-3 py-2 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700"
                :disabled="pendingLoading"
              >
                {{ pendingLoading ? "加载中..." : "刷新" }}
              </button>
            </div>

            <div v-if="pendingLoading" class="text-center py-8">
              <i class="fas fa-spinner fa-spin text-2xl text-blue-500"></i>
              <p class="mt-2 text-gray-600">正在加载待审核合同...</p>
            </div>

            <div
              v-else-if="pendingContracts.length === 0"
              class="text-center py-8"
            >
              <p class="text-gray-600">当前无待审核合同</p>
            </div>

            <div v-else class="overflow-x-auto">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      合同名称
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      申请人
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      提交部门
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      提交时间
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      操作
                    </th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr v-for="c in pendingContracts" :key="c.id">
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-gray-900"
                    >
                      {{ c.contractName }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-gray-500"
                    >
                      {{ c.applicantName }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-gray-500"
                    >
                      {{ c.department }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-gray-500"
                    >
                      {{ c.submissionDate }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-gray-500"
                    >
                      <button
                        @click="viewContract(c)"
                        class="text-blue-600 hover:text-blue-900 mr-3"
                      >
                        查看
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 合同列表内容 -->
          <div
            v-if="currentPage === 'contract-list'"
            class="bg-white rounded-lg shadow-sm p-6"
          >
            <h2 class="text-xl font-semibold text-gray-800 mb-6">合同列表</h2>
            <div class="flex items-center mb-4 space-x-4">
              <div class="relative">
                <select
                  v-model="statusFilter"
                  @change="filterContracts"
                  class="appearance-none bg-white border border-gray-300 rounded-md py-2 pl-3 pr-8 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="all">全部状态</option>
                  <option value="待审批">待审批</option>
                  <option value="审批中">审批中</option>
                  <option value="已完成">已完成</option>
                </select>
                <div
                  class="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-700"
                >
                  <i class="fas fa-chevron-down text-xs"></i>
                </div>
              </div>
              <!-- 合同搜索 -->
              <input
                v-model="searchKeyword"
                @input="filterContracts"
                type="text"
                placeholder="搜索合同名称"
                class="border border-gray-300 rounded-md py-2 px-3 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                style="width: 180px"
              />
              <button
                @click="loadContracts"
                class="px-3 py-2 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700"
                :disabled="loading"
              >
                {{ loading ? "加载中..." : "刷新" }}
              </button>
            </div>

            <!-- 加载状态 -->
            <div
              v-if="loading && contracts.length === 0"
              class="text-center py-8"
            >
              <i class="fas fa-spinner fa-spin text-2xl text-blue-500"></i>
              <p class="mt-2 text-gray-600">正在加载合同数据...</p>
            </div>

            <!-- 空数据状态 -->
            <div
              v-if="!loading && contracts.length === 0"
              class="text-center py-8"
            >
              <i class="fas fa-file-contract text-4xl text-gray-400 mb-4"></i>
              <p class="text-gray-600">暂无合同数据</p>
              <button
                @click="loadContracts"
                class="mt-4 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
              >
                重新加载
              </button>
            </div>

            <!-- 合同列表表格 -->
            <div v-else class="overflow-x-auto">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      合同名称
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      提交部门
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      提交时间
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      状态
                    </th>
                    <th
                      class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      操作
                    </th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr v-for="contract in filteredContracts" :key="contract.id">
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-gray-900"
                    >
                      {{ contract.contractName }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-gray-500"
                    >
                      {{ contract.department }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-gray-500"
                    >
                      {{ contract.submissionDate }}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <span
                        class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
                        :class="getStatusClass(contract.status)"
                        >{{ contract.status }}</span
                      >
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-gray-500"
                    >
                      <button
                        @click="viewContract(contract)"
                        class="text-blue-600 hover:text-blue-900 mr-3"
                      >
                        查看
                      </button>
                      <button
                        @click="deleteContract(contract)"
                        class="text-red-600 hover:text-red-900"
                      >
                        删除
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
              <!-- 分页按钮 -->
              <div class="flex items-center mt-4 space-x-2 justify-center">
                <button
                  @click="goToPrevPage"
                  :disabled="currentListPage === 1"
                  class="px-3 py-1 bg-gray-200 text-gray-700 rounded hover:bg-gray-300 disabled:opacity-50"
                >
                  上一页
                </button>
                <span class="text-sm text-gray-600"
                  >第 {{ currentListPage }} / {{ totalPages }} 页</span
                >
                <button
                  @click="goToNextPage"
                  :disabled="currentListPage === totalPages"
                  class="px-3 py-1 bg-gray-200 text-gray-700 rounded hover:bg-gray-300 disabled:opacity-50"
                >
                  下一页
                </button>
              </div>
            </div>
          </div>

          <!-- 合同详情内容 -->
          <div
            v-if="currentPage === 'contract-detail'"
            class="bg-white rounded-lg shadow-sm p-6"
          >
            <div class="flex justify-between items-center mb-6">
              <h2 class="text-xl font-semibold text-gray-800">合同详情</h2>
              <div class="flex space-x-2">
                <button
                  @click="navigateTo('contract-list')"
                  class="px-4 py-2 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200"
                >
                  <i class="fas fa-arrow-left mr-2"></i>返回列表
                </button>
                <button
                  v-if="username === 'admin' || username === 'rev'"
                  @click="openUploadModal(selectedContract)"
                  class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                >
                  <i class="fas fa-upload mr-2"></i>上传附件
                </button>
                <!-- 审核操作：当合同为待审批时显示通过/驳回按钮 -->
                <template
                  v-if="
                    selectedContract &&
                    [
                      '待综合管理部审批',
                      '待法务审核',
                      '待行政办终审',
                      '待使用部门修改',
                    ].includes(selectedContract.status)
                  "
                >
                  <button
                    @click="openApprovalDialog(selectedContract)"
                    :disabled="loading"
                    class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                  >
                    <i class="fas fa-gavel mr-2"></i> 审批
                  </button>
                </template>
              </div>
            </div>
            <!-- PDF 预览组件（默认隐藏） -->
            <PdfViewer
              v-if="showPdfViewer && previewAttachmentUrl"
              :src="previewAttachmentUrl"
              :title="previewAttachmentName || '合同附件预览'"
              :allow-download="true"
              :download-file-name="previewAttachmentName"
              @close="closePdfViewer"
            />

            <!-- 合同基本信息 -->
            <div class="bg-gray-50 p-4 rounded-lg mb-6" v-if="selectedContract">
              <h3 class="text-lg font-medium text-gray-700 mb-4">合同信息</h3>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-500"
                    >申请人</label
                  >
                  <p class="mt-1 text-sm text-gray-900">
                    {{ selectedContract.applicantName }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-500"
                    >合同名称</label
                  >
                  <p class="mt-1 text-sm text-gray-900">
                    {{ selectedContract.contractName }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-500"
                    >提交部门</label
                  >
                  <p class="mt-1 text-sm text-gray-900">
                    {{ selectedContract.department }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-500"
                    >提交时间</label
                  >
                  <p class="mt-1 text-sm text-gray-900">
                    {{ selectedContract.submissionDate }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-500"
                    >当前状态</label
                  >
                  <p class="mt-1 text-sm text-gray-900">
                    {{ selectedContract.status }}
                  </p>
                </div>
              </div>

              <!-- 合同附件 -->
              <div class="mt-6">
                <h3 class="text-lg font-medium text-gray-700 mb-4">合同附件</h3>
                <div
                  v-if="
                    selectedContract.attachments &&
                    selectedContract.attachments.length > 0
                  "
                  class="grid grid-cols-1 gap-4"
                >
                  <div
                    v-for="attachment in selectedContract.attachments"
                    :key="attachment.id || attachment.name"
                    class="border border-gray-200 rounded-lg p-4 flex items-center justify-between"
                  >
                    <div class="flex items-center">
                      <i
                        :class="getFileIcon(attachment.type)"
                        class="text-2xl mr-3"
                      ></i>
                      <div>
                        <p class="text-sm font-medium text-gray-800">
                          {{ attachment.name }}
                        </p>
                        <p class="text-xs text-gray-500">
                          上传时间: {{ attachment.uploadTime }}
                        </p>
                        <p
                          v-if="!attachment.storePath"
                          class="text-xs text-red-400 mt-1"
                        >
                          （缺少存储路径，无法下载）
                        </p>
                      </div>
                    </div>
                    <div class="flex items-center space-x-3">
                      <button
                        v-if="
                          attachment.storePath &&
                          attachment.name.endsWith('.pdf')
                        "
                        @click="previewAttachment(attachment)"
                        class="px-3 py-1 text-xs bg-indigo-100 text-indigo-700 rounded hover:bg-indigo-200 flex items-center"
                      >
                        <i class="fas fa-eye mr-1"></i> 预览
                      </button>
                      <button
                        v-if="attachment.storePath"
                        @click="downloadAttachment(attachment)"
                        class="px-3 py-1 text-xs bg-green-600 text-white rounded hover:bg-green-700"
                      >
                        下载附件
                      </button>
                    </div>
                  </div>
                </div>
                <div v-else class="text-sm text-gray-500">暂无附件</div>
              </div>

              <!-- 审批意见 -->
              <div class="mt-6">
                <h3 class="text-lg font-medium text-gray-700 mb-4">审批意见</h3>
                <div v-if="approvalComments.length > 0">
                  <el-table :data="approvalComments" style="width: 100%">
                    <el-table-column
                      prop="approverName"
                      label="审批人"
                      width="100"
                    ></el-table-column>
                    <el-table-column
                      prop="approvalNode"
                      label="审批节点"
                      width="120"
                    ></el-table-column>
                    <el-table-column
                      prop="approvalResult"
                      label="审批结果"
                      width="100"
                    >
                      <template slot-scope="scope">
                        <el-tag
                          :type="
                            getApprovalResultType(scope.row.approvalResult)
                          "
                        >
                          {{ getApprovalResultLabel(scope.row.approvalResult) }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column
                      prop="comment"
                      label="意见"
                    ></el-table-column>
                    <el-table-column
                      prop="approvedTime"
                      label="审批时间"
                      width="180"
                    ></el-table-column>
                  </el-table>
                </div>
                <div v-else class="text-sm text-gray-500">暂无审批意见</div>
              </div>
            </div>
          </div>

          <!-- 合同统计内容 -->
          <div
            v-if="currentPage === 'contract-statistics'"
            class="bg-white rounded-lg shadow-sm p-6"
          >
            <div class="flex justify-between items-center mb-6">
              <h2 class="text-xl font-semibold text-gray-800">合同统计</h2>
              <div class="flex items-center space-x-4">
                <div class="relative">
                  <select
                    v-model="departmentFilter"
                    @change="updateChart"
                    class="appearance-none bg-white border border-gray-300 rounded-md py-2 pl-3 pr-8 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="all">全部部门</option>
                    <option
                      v-for="d in departments"
                      :key="d.id || d.name"
                      :value="d.name"
                    >
                      {{ d.name }}
                    </option>
                    <option v-if="departments.length === 0" disabled value="">
                      加载中...
                    </option>
                  </select>
                  <div
                    class="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-700"
                  >
                    <i class="fas fa-chevron-down text-xs"></i>
                  </div>
                </div>
                <div class="flex space-x-2">
                  <button
                    v-for="period in timePeriods"
                    :key="period.value"
                    @click="setTimePeriod(period.value)"
                    class="px-3 py-1 text-sm rounded-md"
                    :class="
                      selectedTimePeriod === period.value
                        ? 'bg-blue-100 text-blue-800'
                        : 'text-gray-600 hover:bg-gray-100'
                    "
                  >
                    {{ period.label }}
                  </button>
                </div>
                <button
                  @click="exportStatistics"
                  :disabled="exportLoading"
                  class="px-3 py-1 text-sm bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
                >
                  <i class="fas fa-download mr-1"></i>
                  {{ exportLoading ? "导出中..." : "导出Excel" }}
                </button>
              </div>
            </div>
            <div ref="chartContainer" class="w-full h-96"></div>
          </div>
        </div>
      </div>

      <!-- 上传合同模态框 -->
      <div
        v-if="showUploadModal"
        class="fixed inset-0 bg-gray-600 bg-opacity-50 flex items-center justify-center z-50"
      >
        <div class="bg-white rounded-lg shadow-xl p-6 w-full max-w-md">
          <div class="flex justify-between items-center mb-4">
            <h3 class="text-lg font-semibold text-gray-800">上传合同</h3>
            <button
              @click="closeUploadModal"
              class="text-gray-500 hover:text-gray-700"
            >
              <i class="fas fa-times"></i>
            </button>
          </div>
          <form @submit.prevent="uploadContract" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1"
                >合同名称</label
              >
              <input
                type="text"
                v-model="uploadForm.contractName"
                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                required
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1"
                >文件格式</label
              >
              <div class="flex space-x-4">
                <label class="inline-flex items-center">
                  <input
                    type="radio"
                    v-model="uploadForm.fileFormat"
                    value="word"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300"
                  />
                  <span class="ml-2 text-gray-700">Word (.docx)</span>
                </label>
                <label class="inline-flex items-center">
                  <input
                    type="radio"
                    v-model="uploadForm.fileFormat"
                    value="pdf"
                    class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300"
                  />
                  <span class="ml-2 text-gray-700">PDF (.pdf)</span>
                </label>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1"
                >选择文件</label
              >
              <input
                type="file"
                @change="handleUploadFileChange"
                class="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
                required
              />
            </div>
            <div class="flex justify-end space-x-3 pt-2">
              <button
                type="button"
                @click="closeUploadModal"
                class="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              >
                取消
              </button>
              <button
                type="submit"
                :disabled="loading"
                class="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
              >
                {{ loading ? "上传中..." : "上传" }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
    <ContractApprovalDialog
      v-if="selectedContract"
      :visible.sync="contractApprovalVisible"
      :contract="selectedContract"
      :departments="departments"
      :loading="approvalLoading"
      @submit="handleContractApprovalSubmit"
    />
    <!-- 使用通用页脚 -->
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>

<script>
import * as echarts from "echarts";
import request from "@/utils/request";
import contractApi from "@/api/contract";
import { userApi } from "@/api/usermag";
import { getUserToken, removeToken, removeUserToken } from "@/utils/auth";
import HeaderComponent from "@/components/HeaderComponent.vue";
import FooterComponent from "@/components/FooterComponent.vue";
import PdfViewer from "@/views/PdfViewer.vue";
import ContractApprovalDialog from "@/components/manage/ContractApprovalDialog.vue";

export default {
  name: "ContractManagement",
  components: {
    HeaderComponent,
    FooterComponent,
    PdfViewer, // 注册组件
    ContractApprovalDialog, // 注册合同审批弹窗组件
  },
  data() {
    return {
      // --- 2. 新增：用于存储当前登录用户的信息 ---
      userId: null,
      username: "",

      currentPage: "home",
      contractMenuOpen: false,
      statusFilter: "all",
      departmentFilter: "all",
      selectedTimePeriod: "month",
      showUploadModal: false,
      selectedContract: null,
      chartInstance: null,
      loading: false,
      exportLoading: false, // 导出Excel的加载状态
      showPdfViewer: false, // 控制 PDF 预览组件显示/隐藏
      previewAttachmentUrl: "", // 存储 PDF 附件的临时 URL
      previewAttachmentName: "", // 预览附件的文件名（用于标题和下载）
      contractApprovalVisible: false, // 必须有这个变量，初始为 false
      approvalLoading: false, // 控制确定按钮的 loading

      // 新建合同表单
      newContract: {
        contractName: "",
        applicantName: "", // 将由 created 自动填充
        department: "",
        file: null,
        submissionDate: "",
        // -- 新增工作流审批人字段 --
        gmUser: "", // 对应 ${gmUser}
        legalUser: "", // 对应 ${legalUser}
      },

      // --- 4. 新增：表单验证规则 (模拟 LeaveApply.vue) ---
      contractRules: {
        contractName: [
          { required: true, message: "请输入合同名称", trigger: "blur" },
        ],
        department: [
          { required: true, message: "请选择提交部门", trigger: "change" },
        ],
        gmUser: [
          {
            required: true,
            message: "请选择综合管理部审批人",
            trigger: "change",
          },
        ],
        legalUser: [
          { required: true, message: "请选择行政办审批人", trigger: "change" },
        ],
      },
      // --- 5. 新增：审批人选项 (模拟 LeaveApply.vue) ---
      approverLoading: false,
      gmUserOptions: [],
      legalUserOptions: [],
      // 上传表单
      uploadForm: {
        contractName: "",
        fileFormat: "word",
        file: null,
      },

      // 时间周期选项
      timePeriods: [
        { value: "month", label: "月度" },
        { value: "quarter", label: "季度" },
        { value: "year", label: "年度" },
      ],

      // 合同列表数据
      contracts: [],
      searchKeyword: "",
      // 待审核合同（合同审核页使用）
      pendingContracts: [],
      pendingLoading: false,

      // 部门数据（动态从后端加载）
      departments: [],

      // 分页相关
      currentListPage: 1,
      pageSize: 10,
      totalPages: 1,
      totalContracts: 0,

      // 统计图表数据
      chartData: {
        month: {
          all: {
            categories: [],
            submitted: [],
            approved: [],
          },
        },
        quarter: {
          all: {
            categories: [],
            submitted: [],
            approved: [],
          },
        },
        year: {
          all: {
            categories: [],
            submitted: [],
            approved: [],
          },
        },
      },

      // 合同审批意见
      approvalComments: [],
    };
  },
  created() {
    // 在组件创建后安全地获取用户信息
    const storedUserInfo = getUserToken();
    if (storedUserInfo && storedUserInfo.user) {
      this.login_user = storedUserInfo.user;
      this.userRoles = storedUserInfo.roles;
      // --- 6. 新增：设置当前用户信息 ---
      this.userId = this.login_user.id;
      this.userName = this.login_user.name || this.login_user.username;
      // 自动填充申请人
      this.newContract.applicantName =
        this.login_user.nickname || this.userName;
    } else {
      this.$message.warning("您尚未登录，请先登录！");
      // 如果未登录，跳转到登录页面
      this.$router.push("/login");
    }
  },

  computed: {
    filteredContracts() {
      let result = this.contracts;
      if (this.statusFilter !== "all") {
        result = result.filter(
          (contract) => contract.status === this.statusFilter
        );
      }
      if (this.searchKeyword) {
        const kw = this.searchKeyword.trim().toLowerCase();
        result = result.filter(
          (contract) =>
            contract.contractName &&
            contract.contractName.toLowerCase().includes(kw)
        );
      }
      // 分页处理
      const start = (this.currentListPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return result.slice(start, end);
    },
  },

  mounted() {
    this.checkPermission();
    console.log("Vue组件已挂载");
    // 初始化时加载合同数据
    this.loadContracts();
    // 加载部门数据
    this.loadDepartments();
    // --- 7. 新增：加载审批人选项 ---
    this.loadApproverOptions();
    // 预加载待审核列表（可按需）
    this.loadPendingContracts();
  },

  beforeUnmount() {
    if (this.chartInstance) {
      this.chartInstance.dispose();
    }
  },

  methods: {
    checkPermission() {
      // 检查用户是否有 CONTRACT:UPLOAD 上传合同权限
      if (
        !this.$hasPermission([
          "CONTRACT:UPLOAD",
          "CONTRACT:VIEW_DEPT",
          "CONTRACT:VIEW_ALL",
        ])
      ) {
        // 如果没有权限，显示提示信息并跳转到主页
        this.$router.push("/");
        this.$message.warning("您无权限访问此页面,请联系管理员赋权");
      }
    },

    // 预览附件（PDF 专用）
    async previewAttachment(attachment) {
      // 仅处理 PDF 类型附件（可根据需要扩展其他类型）
      if (!attachment.name?.endsWith(".pdf")) {
        this.$message.warning("仅支持 PDF 格式附件预览");
        return;
      }

      try {
        // 复用下载逻辑中的「获取 MinIO 临时 URL」接口
        const res = await contractApi.getAttachmentDownloadUrl(
          this.selectedContract.id,
          attachment.id,
          600 // URL 有效期 10 分钟，与下载逻辑一致
        );
        const data = res?.data ?? res;
        const tempUrl = data?.url;

        if (!tempUrl) {
          this.$message.error("未获取到预览链接");
          return;
        }

        // 赋值预览数据，显示组件
        this.previewAttachmentUrl = tempUrl;
        this.previewAttachmentName = attachment.name;
        this.showPdfViewer = true;
      } catch (e) {
        console.error("PDF 预览失败:", e);
        this.$message.error("预览失败，请重试");
      }
    },

    // 关闭 PDF 预览组件
    closePdfViewer() {
      this.showPdfViewer = false;
      this.previewAttachmentUrl = ""; // 清空 URL 避免缓存
      this.previewAttachmentName = "";
    },
    //API 调用方法
    // --- 8. 新增：加载审批人方法 (模拟 LeaveApply.vue) ---
    async loadApproverOptions() {
      // 严格按部门名称筛选；兼容仅返回 department_id 的情况
      this.approverLoading = true;
      try {
        // 旧的全量用户列表不再需要，直接按角色获取

        const toOption = (u) => ({
          value: u.id,
          label:
            u.nickname || u.name || u.username || u.realName || `ID:${u.id}`,
        });

        // 获取用户部门名称
        // 注意：已改为按部门ID精确筛选，故不再需要按名称匹配

        // 直接从后端按角色拉取，避免前端解析不一致
        const ROLE_MANAGEMENT_DEPT = "ROLE_MANAGEMENT_DEPT";
        const ROLE_ADMIN_OFFICE = "ROLE_ADMIN_OFFICE";

        const [gmRes, adminRes] = await Promise.all([
          userApi.getUsersByRole(ROLE_MANAGEMENT_DEPT),
          userApi.getUsersByRole(ROLE_ADMIN_OFFICE),
        ]);

        const extractList = (resp) => {
          const data = resp?.data ?? resp;
          if (Array.isArray(data)) return data;
          if (Array.isArray(data?.records)) return data.records;
          if (Array.isArray(data?.list)) return data.list;
          return [];
        };

        const gmList = extractList(gmRes);
        const adminList = extractList(adminRes);

        console.log("[Approver] 后端角色查询（GM）人数:", gmList.length);
        console.log(
          "[Approver] 后端角色查询（AdminOffice）人数:",
          adminList.length
        );

        this.gmUserOptions = gmList.map(toOption);
        this.legalUserOptions = adminList.map(toOption);

        // 保持空集合，不做其他部门兜底，确保“只显示该部门”要求
      } catch (e) {
        console.error("加载审批人列表失败", e);
        this.gmUserOptions = [];
        this.legalUserOptions = [];
      } finally {
        this.approverLoading = false;
      }
    },
    // 统一状态映射（英->中）
    normalizeStatus(status) {
      const map = {
        draft: "待审批",
        approved: "审批中",
        completed: "已完成",
      };
      return map[status] || status || "";
    },
    // 退出登录
    handleLogout() {
      // 1. 清除 Vuex 状态
      removeUserToken();
      this.login_user = null;
      removeToken();
      this.$router.push("/login");
    },
    handleNavigate(routeName) {
      this.$router.push({ name: routeName });
    },

    // 规范化合同对象，补齐常用字段
    normalizeContract(obj = {}) {
      const res = { ...obj };
      res.status = this.normalizeStatus(res.status);
      res.contractName = res.contractName || res.name || "";
      res.applicantName = res.applicantName || res.applicant || "";
      res.department = res.department || res.dept || "";
      res.submissionDate = res.submissionDate || "";
      res.createdAt = res.createdAt || "";
      return res;
    },

    // 规范化审批意见字段，兼容不同后端命名
    transformApprovalComments(list = []) {
      if (!Array.isArray(list)) {
        // 部分接口可能返回 { list: [...] }
        const maybeList = list.list || list.records || [];
        if (!Array.isArray(maybeList)) return [];
        list = maybeList;
      }

      const pickFirst = (...vals) =>
        vals.find((v) => v !== undefined && v !== null && v !== "");

      return list.map((item = {}) => {
        const approvalResult = pickFirst(
          item.approvalResult,
          item.result,
          item.status,
          item.outcome,
          item.decision
        );

        return {
          approverName: pickFirst(
            item.approverName,
            item.approver,
            item.userName,
            item.user,
            item.username,
            item.realName,
            item.operator
          ),
          approvalNode: pickFirst(
            item.approvalNode,
            item.node,
            item.nodeName,
            item.taskName,
            item.activityName,
            item.stepName,
            item.stage
          ),
          approvalResult,
          comment: pickFirst(
            item.comment,
            item.opinion,
            item.remark,
            item.message,
            item.reason,
            item.content,
            item.memo
          ),
          approvedTime: pickFirst(
            item.approvedTime,
            item.approveTime,
            item.time,
            item.createTime,
            item.updateTime,
            item.completedAt,
            item.finishTime,
            item.timestamp
          ),
        };
      });
    },

    // 加载合同列表数据（输出已规范化的列表项）
    async loadContracts() {
      console.log("开始加载合同数据");
      this.loading = true;
      try {
        const params = { page: this.currentListPage, size: this.pageSize };
        const result = await contractApi.getContractList(params);
        console.log("API结果:", result);

        // 兼容多种返回结构
        const data = result?.data ?? result;
        let list = [];
        if (Array.isArray(data)) list = data;
        else if (Array.isArray(data?.records)) list = data.records;
        else if (Array.isArray(data?.list)) list = data.list;

        this.contracts = list.map((item) =>
          this.normalizeContract({
            id: item.id,
            contractName: item.contractName,
            department: item.department,
            status: item.status,
            submissionDate: item.submissionDate,
            applicantName: item.applicantName,
          })
        );
        this.totalContracts =
          (data && (data.total ?? data.count)) ??
          result.total ??
          list.length ??
          0;
        this.totalPages = Math.max(
          1,
          Math.ceil(this.totalContracts / this.pageSize)
        );
        console.log("合同数据加载成功:", this.contracts);
      } catch (error) {
        console.error("API调用失败:", error);
        this.$message?.error?.("合同数据加载失败");
        this.contracts = [];
        this.totalContracts = 0;
        this.totalPages = 1;
      } finally {
        this.loading = false;
      }
    },

    // 加载部门数据（后端接口 /department/list）
    async loadDepartments() {
      try {
        const res = await request({ url: "/department/list", method: "get" });
        const data = res?.data ?? res;
        let list = [];
        if (Array.isArray(data)) list = data;
        else if (Array.isArray(data?.records)) list = data.records;
        else if (Array.isArray(data?.list)) list = data.list;
        // 规范化为 { id, name, code }
        this.departments = list.map((d) => ({
          id: d.id,
          name:
            d.name || d.departmentName || d.deptName || d.code || "未知部门",
          code: d.code || "",
        }));
        // 如果后端返回为空，使用一个兜底静态集合
        if (this.departments.length === 0) {
          this.departments = [
            { name: "市场部" },
            { name: "采购部" },
            { name: "技术部" },
            { name: "财务部" },
            { name: "人事部" },
          ];
        }
      } catch (e) {
        console.error("部门数据加载失败", e);
        // 失败兜底：保持与原来硬编码一致
        this.departments = [
          { name: "市场部" },
          { name: "采购部" },
          { name: "技术部" },
          { name: "财务部" },
          { name: "人事部" },
        ];
      }
    },

    // 加载合同详情（与列表合并，统一规范化）
    async loadContractDetail(id) {
      console.log("加载合同详情, ID:", id);
      this.loading = true;
      try {
        const [detailRes, commentsRes] = await Promise.all([
          contractApi.getContractDetail(id),
          contractApi.getApprovalComments(id).catch((e) => {
            console.error("审批意见接口异常", e);
            return null;
          }),
        ]);
        console.log("合同详情API结果:", detailRes);

        const source = detailRes?.data ?? detailRes;
        const detail = Array.isArray(source)
          ? source.find((x) => String(x.id) === String(id))
          : source;

        const base =
          this.contracts.find((x) => String(x.id) === String(id)) || {};

        if (detail && typeof detail === "object") {
          // 先用详情，再用列表兜底，最后统一规范化
          const merged = { ...detail, ...base };
          const normalized = this.normalizeContract(merged);
          // 审批意见兼容多种返回字段
          const extractComments = (data) => {
            if (!data) return [];
            if (Array.isArray(data)) return data;
            if (Array.isArray(data?.records)) return data.records;
            if (Array.isArray(data?.list)) return data.list;
            return [];
          };

          const apiComments = extractComments(commentsRes?.data ?? commentsRes);
          const rawComments =
            (apiComments && apiComments.length > 0 ? apiComments : null) ||
            normalized.approvalComments ||
            normalized.comments ||
            normalized.approvalHistory ||
            normalized.approvalRecords ||
            normalized.taskComments ||
            normalized.flowRecords ||
            [];
          const comments = this.transformApprovalComments(rawComments);

          this.selectedContract = {
            ...normalized,
            attachments: Array.isArray(normalized.attachments)
              ? normalized.attachments
              : [],
            approvalSteps: Array.isArray(normalized.approvalSteps)
              ? normalized.approvalSteps
              : [],
          };
          this.approvalComments = comments;
          console.log("合同详情加载成功:", this.selectedContract);
        } else {
          this.selectedContract = null;
          this.approvalComments = [];
          this.$message?.error?.("未找到该合同详情");
        }
      } catch (error) {
        console.error("API调用失败:", error);
        this.selectedContract = null;
        this.approvalComments = [];
        this.$message?.error?.("合同详情加载失败");
      } finally {
        this.loading = false;
      }
    },
    // 删除合同方法
    async deleteContract(contract) {
      if (!contract.id) return;
      if (!confirm("确定要删除该合同吗？")) return;
      this.loading = true;
      try {
        const result = await contractApi.deleteContract(contract.id);
        if (result?.success || result?.code == 200 || result?.code === "200") {
          alert("删除成功");
          await this.loadContracts();
        } else {
          alert(result.message || "删除失败");
        }
      } catch (e) {
        alert("删除失败");
      } finally {
        this.loading = false;
      }
    },
    async loadStatistics() {
      try {
        const params = {};
        if (this.departmentFilter && this.departmentFilter !== "all") {
          params.department = this.departmentFilter;
        }
        const result = await contractApi.getStatistics(params);
        // 期望结构：{ success, data: { monthlyData: { categories, submitted, approved }, ... } }
        if (
          (result?.success || result?.code == 200 || result?.code === "200") &&
          result.data?.monthlyData
        ) {
          // 用接口数据覆盖本地 chartData，ECharts 仍复用你现有的 initChart()
          const key = this.departmentFilter || "all";
          if (!this.chartData.month[key])
            this.chartData.month[key] = {
              categories: [],
              submitted: [],
              approved: [],
            };
          if (!this.chartData.quarter[key])
            this.chartData.quarter[key] = {
              categories: [],
              submitted: [],
              approved: [],
            };
          if (!this.chartData.year[key])
            this.chartData.year[key] = {
              categories: [],
              submitted: [],
              approved: [],
            };
          this.chartData.month[key] = {
            categories: result.data.monthlyData.categories || [],
            submitted: result.data.monthlyData.submitted || [],
            approved: result.data.monthlyData.approved || [],
          };
          this.chartData.quarter[key] = {
            categories: result.data.quarterData.categories || [],
            submitted: result.data.quarterData.submitted || [],
            approved: result.data.quarterData.approved || [],
          };
          this.chartData.year[key] = {
            categories: result.data.yearData.categories || [],
            submitted: result.data.yearData.submitted || [],
            approved: result.data.yearData.approved || [],
          };
        }
      } catch (e) {
        console.error("统计接口异常：", e);
        // 出错时继续用你现有的本地 chartData 作为兜底，不做额外处理
      } finally {
        // 数据准备好（或兜底）后再绘图
        this.initChart();
      }
    },

    // --- 9. 重写：submitContract (关键修改) ---
    async submitContract() {
      if (!this.$refs.contractForm) {
        console.warn("合同表单引用不存在");
        return;
      }
      // 1. 表单验证
      this.$refs.contractForm.validate(async (valid) => {
        if (!valid) {
          console.log("表单验证失败");
          return;
        }

        console.log("提交合同，开始创建记录:", this.newContract);
        this.loading = true;

        try {
          const now = new Date().toISOString();
          // 1. 先创建合同记录（后端 ContractReview.create 接口）
          const createPayload = {
            contractName: this.newContract.contractName,
            applicantName: this.newContract.applicantName,
            department: this.newContract.department,
            contractType: this.newContract.type || "",
            submissionDate: now,
            status: "draft",
            reviewComments: this.newContract.comments || "",
          };

          const createRes = await contractApi.createContract(createPayload);
          console.log("创建合同返回:", createRes);

          if (
            !createRes ||
            !(
              createRes?.success ||
              createRes?.code == 200 ||
              createRes?.code === "200"
            )
          ) {
            this.$message.error(createRes?.message || "创建合同失败");
            return;
          }

          // 提取数值型 ID（兼容不同后端结构）
          let createdId =
            createRes.data?.id ?? createRes?.id ?? createRes?.data;
          // 如果返回是对象或字符串且不可用，则尝试从 data 中直接读
          if (typeof createdId === "object") createdId = createdId?.id;

          if (!createdId) {
            this.$message.error("未能获取新建合同ID，无法上传附件或启动流程");
            return;
          }

          // 2. 如果有附件，使用数值合同 ID 上传（避免将流程实例ID当作合同ID）
          if (this.newContract.file) {
            try {
              console.log("正在上传附件到合同ID:", createdId);
              const fd = new FormData();
              fd.append("file", this.newContract.file);
              const uploadRes = await contractApi.uploadAttachment(
                createdId,
                fd
              );
              console.log("附件上传返回:", uploadRes);
              if (!uploadRes?.success && uploadRes?.code != 200) {
                this.$message.warning(
                  "合同创建成功，但附件上传失败，请在详情页重试"
                );
              }
            } catch (uploadError) {
              console.error("附件上传异常:", uploadError);
              this.$message.warning("合同创建成功，但附件上传出错");
            }
          }

          // 3. 启动流程：将已创建的 contractReview 对象（带 id）和审批人信息一起传给后端
          const requestData = {
            contractReview: {
              id: createdId,
              contractName: this.newContract.contractName,
              applicantId: this.userId,
              applicantName: this.newContract.applicantName,
              department: this.newContract.department,
              status: "待审批",
              reviewComments: this.newContract.comments || "",
              submissionDate: now,
            },
            starter: String(this.userId),
            gmUser: this.newContract.gmUser,
            legalUser: this.newContract.legalUser,
          };

          const startRes = await contractApi.startContractProcess(requestData);
          console.log("流程启动返回:", startRes);

          if (
            startRes?.success ||
            startRes?.code == 200 ||
            startRes?.code === "200"
          ) {
            this.$message.success("合同提交审核成功");
            this.resetContractForm();
            await this.loadContracts();
            this.navigateTo("contract-list");
          } else {
            // 启动流程失败但合同已创建
            this.$message.warning(
              startRes?.message || "合同已创建，但启动流程失败，请在详情页重试"
            );
            await this.loadContracts();
            this.navigateTo("contract-list");
          }
        } catch (error) {
          console.error("提交失败:", error);
          const msg = error?.response?.data?.message || "提交审核失败";
          this.$message.error(msg);
        } finally {
          this.loading = false;
        }
      });
    },
    // --- 10. 新增：重置表单方法 ---
    async resetContractForm() {
      if (this.$refs.contractForm) {
        this.$refs.contractForm.resetFields();
      }
      this.newContract.file = null;
      // 重新填充申请人信息
      this.newContract.applicantName = this.login_user.nickname;
    },
    async uploadContract() {
      if (!this.uploadForm.file) {
        alert("请选择要上传的文件");
        return;
      }

      console.log("上传附件:", this.uploadForm);
      this.loading = true;
      try {
        const fd = new FormData();
        fd.append("file", this.uploadForm.file);
        const result = await contractApi.uploadAttachment(
          this.selectedContract.id,
          fd
        );
        console.log("上传附件结果:", result);

        if (result?.success || result?.code == 200 || result?.code === "200") {
          alert(result.message || "上传成功");
          this.closeUploadModal();
          await this.loadContractDetail(this.selectedContract.id);
        } else {
          alert("上传失败");
        }
      } catch (error) {
        console.error("上传失败:", error);
        this.$message?.error?.("上传失败");
      } finally {
        this.loading = false;
      }
    },

    toggleContractMenu() {
      this.contractMenuOpen = !this.contractMenuOpen;
    },

    navigateTo(page) {
      console.log("导航到页面:", page);
      this.currentPage = page;

      if (page === "create-contract") {
        // 每次打开创建页面时，重置表单并填充申请人
        this.resetContractForm();
      }
      if (page !== "contract-list") {
        this.selectedContract = null;
      }

      // 当导航到统计页面时初始化图表（静态图表数据）
      // if (page === "contract-statistics") {
      //   this.$nextTick(() => {
      //     this.initChart();
      //   });
      // }

      // 当导航到统计页面时：先拉统计数据，再绘图
      if (page === "contract-statistics") {
        this.$nextTick(() => {
          this.loadStatistics();
        });
      }
      // 如果不是合同管理相关页面，关闭合同菜单
      if (
        ![
          "create-contract",
          "contract-list",
          "contract-statistics",
          "contract-detail",
          "contract-review",
        ].includes(page)
      ) {
        this.contractMenuOpen = false;
      } else {
        this.contractMenuOpen = true;
      }

      if (page !== "contract-statistics" && this.chartInstance) {
        this.chartInstance.dispose();
        this.chartInstance = null;
      }
      // 当导航到合同审核页时，加载待审核列表
      if (page === "contract-review") {
        this.$nextTick(() => {
          this.loadPendingContracts();
        });
      }
    },

    async viewContract(contract) {
      console.log("查看合同详情:", contract);
      await this.loadContractDetail(contract.id);
      this.currentPage = "contract-detail";
    },

    downloadContract(contract) {
      alert(`下载合同: ${contract.contractName}`);
    },

    async downloadAttachment(attachment) {
      try {
        // 1) 用带token的请求向后端索取临时URL
        const res = await contractApi.getAttachmentDownloadUrl(
          this.selectedContract.id,
          attachment.id,
          600
        );
        const data = res?.data ?? res;
        const tempUrl = data?.url;
        if (!tempUrl) {
          this.$message?.error?.("未获取到下载链接");
          return;
        }
        // 2) 直接从对象存储取二进制，前端强制保存为原文件名
        const resp = await fetch(tempUrl, {
          method: "GET",
          credentials: "omit",
        });
        if (!resp.ok) {
          this.$message?.error?.("文件获取失败");
          return;
        }
        const blob = await resp.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = attachment.name || "attachment";
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
      } catch (e) {
        console.error("下载失败", e);
        this.$message?.error?.("下载失败");
      }
    },

    openUploadModal(contract) {
      this.uploadForm.contractName = contract.contractName;
      this.showUploadModal = true;
    },

    closeUploadModal() {
      this.showUploadModal = false;
      this.uploadForm = {
        contractName: "",
        fileFormat: "word",
        file: null,
      };
    },

    handleFileUpload(event) {
      this.newContract.file = event.target.files[0];
      console.log("选择文件:", this.newContract.file);
    },

    handleUploadFileChange(event) {
      this.uploadForm.file = event.target.files[0];
      console.log("选择上传文件:", this.uploadForm.file);
    },

    filterContracts() {
      // 过滤逻辑已在computed中处理
      // 切换筛选时重置到第一页
      this.currentListPage = 1;
      console.log("过滤合同, 状态:", this.statusFilter);
    },

    // 加载待审核合同（合同审核页使用）
    async loadPendingContracts() {
      this.pendingLoading = true;
      try {
        // 不再强制向后端传递单一 status，改为拉取较大页量后在前端做兼容性过滤，
        // 以便同时展示后端返回的 'draft' / 'pending' 等不同表示方式。
        const params = { page: 1, size: 200 };
        const res = await contractApi.getContractList(params);
        const data = res?.data ?? res;
        let list = [];
        if (Array.isArray(data)) list = data;
        else if (Array.isArray(data?.records)) list = data.records;
        else if (Array.isArray(data?.list)) list = data.list;

        // 规范化并兼容多种后端 status 表示：
        // - 使用 normalizeContract 进行中文映射
        // - 同时保留原始 rawStatus，前端过滤时判定 raw 字段是否包含 draft/pending 或 normalize 后为 '待审批'
        this.pendingContracts = list
          .map((item) => {
            const normalized = this.normalizeContract({
              id: item.id,
              contractName: item.contractName,
              department: item.department,
              status: item.status,
              submissionDate: item.submissionDate,
              applicantName: item.applicantName,
            });
            return {
              ...normalized,
              rawStatus: item.status,
            };
          })
          .filter((c) => {
            const raw = String(c.rawStatus || "").toLowerCase();
            // 命中任一条件则认为是待审核：
            // 1) 规范化后为 '待审批'
            // 2) 后端原始状态中包含 draft / pend / 待 等关键词
            return (
              c.status === "待审批" ||
              raw.includes("draft") ||
              raw.includes("pend") ||
              raw.includes("待")
            );
          });
      } catch (e) {
        console.error("加载待审核合同失败", e);
        this.pendingContracts = [];
      } finally {
        this.pendingLoading = false;
      }
    },

    async fetchCurrentTaskId(contractId) {
      try {
        // this.userId 来自组件 data 中存储的当前登录用户ID
        const res = await contractApi.getMyTask(contractId, this.userId);
        if (res && (res.success || res.code == 200)) {
          return res.data; // 返回 taskId
        } else {
          this.$message.error("未找到您的审批任务，可能已被处理或您无权审批");
          return null;
        }
      } catch (e) {
        console.error("获取任务失败", e);
        this.$message.error("无法获取任务信息，请检查网络或联系管理员");
        return null;
      }
    },
    // 【修改点5】打开审批弹窗
    openApprovalDialog(contract) {
      if (!contract || !contract.id) return;
      this.selectedContract = contract; // 确保选中
      this.contractApprovalVisible = true;
    },

    // 【修改点6】处理弹窗提交
    async handleContractApprovalSubmit(formData) {
      const contract = this.selectedContract;

      // 1. 获取任务ID (复用之前的 fetchCurrentTaskId 方法)
      const taskId = await this.fetchCurrentTaskId(contract.id);
      if (!taskId) {
        this.contractApprovalVisible = false;
        return;
      }

      this.approvalLoading = true;
      try {
        // 先处理退回修改场景的合同更新与补充附件
        if (formData.changes) {
          const updatePayload = {
            contractName: formData.changes.contractName,
            department: formData.changes.department,
            reviewComments: formData.changes.reviewComments,
          };
          await contractApi.updateContract(contract.id, updatePayload);

          if (formData.newFile) {
            const fd = new FormData();
            fd.append("file", formData.newFile);
            await contractApi.uploadAttachment(contract.id, fd);
          }

          // 立即刷新详情，保证界面展示最新数据
          await this.loadContractDetail(contract.id);
        }

        const payload = {
          taskId: taskId,
          contractId: contract.id,
          approved: formData.approved, // true or false
          comment: formData.comment,
        };

        const res = await contractApi.completeTask(payload);

        if (res?.success || res?.code == 200) {
          this.$message.success(formData.approved ? "审批通过" : "已驳回");
          this.contractApprovalVisible = false; // 关闭弹窗

          // 刷新数据并返回列表 (或者留在详情页并刷新)
          this.currentPage = "contract-list";
          await this.loadContracts();

          // 如果有待办列表数据，也刷新一下
          if (this.pendingContracts.length > 0) {
            await this.loadPendingContracts();
          }
        } else {
          this.$message.warning(res?.message || "操作未成功");
        }
      } catch (e) {
        console.error("审批失败", e);
        this.$message.error("审批操作失败");
      } finally {
        this.approvalLoading = false;
      }
    },

    // 分页相关方法
    goToPrevPage() {
      if (this.currentListPage > 1) {
        this.currentListPage--;
        this.loadContracts();
      }
    },
    goToNextPage() {
      if (this.currentListPage < this.totalPages) {
        this.currentListPage++;
        this.loadContracts();
      }
    },

    getStatusClass(status) {
      switch (status) {
        case "已完成":
          return "bg-green-100 text-green-800";
        case "待审批":
          return "bg-yellow-100 text-yellow-800";
        case "审批中":
          return "bg-blue-100 text-blue-800";
        default:
          return "bg-gray-100 text-gray-800";
      }
    },

    getApprovalResultType(result) {
      const r = (result || "").toUpperCase();
      if (r === "APPROVED" || r === "PASS" || r === "AGREE") return "success";
      if (r === "PENDING" || r === "SUBMITTED") return "info";
      return "danger";
    },

    getApprovalResultLabel(result) {
      const r = (result || "").toUpperCase();
      const map = {
        APPROVED: "同意",
        PASS: "同意",
        AGREE: "同意",
        REJECTED: "拒绝",
        REFUSED: "拒绝",
        PENDING: "待审批",
        SUBMITTED: "待审批",
      };
      return map[r] || result || "";
    },

    // getStepClass(status) {
    //   if (status === "completed") {
    //     return "bg-blue-500 text-white";
    //   } else {
    //     return "bg-gray-200 text-gray-500";
    //   }
    // },
    getStepClass(step, contractStatus) {
      // 如果整个合同的状态是'待审批'，并且当前步骤的标题是'提交申请'
      if (contractStatus === "待审批" && step.title === "提交申请") {
        // 那么'提交申请'这个节点也应该显示为蓝色（已完成）
        return "bg-blue-500 text-white";
      } else if (contractStatus === "审批中" && step.title === "部门审批") {
        return "bg-blue-500 text-white";
      } else if (contractStatus === "已完成" && step.title === "已完成") {
        return "bg-blue-500 text-white";
      }
      // 规则3：其他所有情况，都显示为灰色（未完成）
      return "bg-gray-200 text-gray-500";
    },

    getFileIcon(type) {
      switch (type) {
        case "word":
          return "fas fa-file-word text-blue-500";
        case "pdf":
          return "fas fa-file-pdf text-red-500";
        default:
          return "fas fa-file text-gray-500";
      }
    },

    setTimePeriod(period) {
      this.selectedTimePeriod = period;
      this.updateChart();
    },

    updateChart() {
      // 部门筛选变更时，重新取统计数据（带department参数）并再绘图
      if (this.currentPage === "contract-statistics") {
        this.loadStatistics();
        return;
      }
      if (this.chartInstance) {
        this.initChart();
      }
    },

    // 导出统计数据为 Excel（模拟请销假的导出写法）
    async exportStatistics() {
      if (this.exportLoading) return;
      this.exportLoading = true;
      try {
        const XLSX = await import("xlsx");

        const key = this.departmentFilter || "all";
        const current =
          (this.chartData[this.selectedTimePeriod] &&
            this.chartData[this.selectedTimePeriod][key]) ||
          this.chartData[this.selectedTimePeriod].all;

        if (
          !current ||
          !Array.isArray(current.categories) ||
          current.categories.length === 0
        ) {
          this.$message?.warning?.("暂无数据可导出");
          return;
        }

        const periodText =
          this.selectedTimePeriod === "year"
            ? "年度"
            : this.selectedTimePeriod === "quarter"
            ? "季度"
            : "月度";
        const deptText =
          this.departmentFilter === "all" ? "全中心" : this.departmentFilter;

        const headers = ["时间段", "提交合同数", "完成审批数"];
        const dataRows = current.categories.map((cat, i) => [
          cat || "",
          Number(current.submitted?.[i] ?? 0) || 0,
          Number(current.approved?.[i] ?? 0) || 0,
        ]);

        const wb = XLSX.utils.book_new();
        const ws = XLSX.utils.aoa_to_sheet([headers, ...dataRows]);
        ws["!cols"] = [{ wch: 18 }, { wch: 16 }, { wch: 16 }];
        XLSX.utils.book_append_sheet(wb, ws, `${periodText}统计`);

        const dateStr = new Date()
          .toLocaleDateString("zh-CN")
          .replace(/\//g, "");
        const fileName = `合同统计_${periodText}_${deptText}_${dateStr}.xlsx`;
        XLSX.writeFile(wb, fileName);

        this.$message?.success?.("导出成功");
      } catch (e) {
        console.error("导出失败", e);
        this.$message?.error?.("导出失败：" + (e?.message || "未知错误"));
      } finally {
        this.exportLoading = false;
      }
    },

    initChart() {
      if (!this.$refs.chartContainer) {
        console.log("图表容器未找到");
        return;
      }

      console.log("初始化图表");

      // 销毁现有图表实例
      if (this.chartInstance) {
        this.chartInstance.dispose();
        console.log("销毁旧图表实例");
      }
      // 创建新图表实例
      this.chartInstance = echarts.init(this.$refs.chartContainer);

      const key = this.departmentFilter || "all";
      const currentData =
        this.chartData[this.selectedTimePeriod][key] ||
        this.chartData[this.selectedTimePeriod].all;

      const option = {
        tooltip: {
          trigger: "axis",
          axisPointer: {
            type: "shadow",
          },
        },
        legend: {
          data: [
            this.departmentFilter && this.departmentFilter !== "all"
              ? `提交合同（${this.departmentFilter}）`
              : "提交合同",
            this.departmentFilter && this.departmentFilter !== "all"
              ? `完成审批（${this.departmentFilter}）`
              : "完成审批",
          ],
          orient: "horizontal", // 横向一行
          left: "center", // 水平居中
          bottom: 8, // 放到图表底部
          itemGap: 28, // 两项间距
          padding: [0, 0, 0, 0],
        },
        grid: {
          left: "3%",
          right: "4%",
          //bottom: "3%",
          bottom: 30,
          containLabel: true,
        },
        xAxis: {
          type: "category",
          data: currentData.categories,
        },
        yAxis: {
          type: "value",
        },
        series: [
          {
            name:
              this.departmentFilter && this.departmentFilter !== "all"
                ? `提交合同（${this.departmentFilter}）`
                : "提交合同",
            type: "bar",
            data: currentData.submitted,
            itemStyle: {
              color: "#4f46e5",
            },
          },
          {
            name:
              this.departmentFilter && this.departmentFilter !== "all"
                ? `完成审批（${this.departmentFilter}）`
                : "完成审批",
            type: "bar",
            data: currentData.approved,
            itemStyle: {
              color: "#10b981",
            },
          },
        ],
      };

      this.chartInstance.setOption(option);

      // 监听窗口大小变化
      window.addEventListener("resize", () => {
        if (this.chartInstance) {
          this.chartInstance.resize();
        }
      });
    },
  },

  watch: {
    currentPage(newPage) {
      if (newPage === "contract-statistics") {
        this.$nextTick(() => {
          //loadStatistics()，无论是通过侧边栏导航还是其它方式切到统计页，都会先取接口数据再渲染图表
          this.loadStatistics();
        });
      }
    },
  },
};
</script>

<style lang="scss" scoped>
/* 13. 新增：由于我们引入了 Element UI (el-form)，
  需要确保它的样式不会被 Tailwind CSS 严重破坏。
  您可能需要调整全局 CSS 或这里的局部样式。
  为了模拟 LeaveApply.vue，我们假设 Element UI 样式已全局引入。
*/
.el-select,
.el-date-picker {
  width: 100%;
}
$headHeight: 148px;
$bottomHeight: 161px;

.home-container {
  width: 100vw;
  height: 100vh;
  background: #e5e5e5;
  .head {
    height: $headHeight;
    background-image: url("@/assets/home/top.png");
    &-operate {
      display: flex;
      justify-content: flex-end;
      padding: 65px;
      right: 10%;
    }
  }

  .sidebar-item.active {
    background-color: #f3f4f6;
    border-left: 4px solid #3b82f6;
  }

  .submenu {
    display: none;
  }

  .submenu.active {
    display: block;
  }

  .rotate-180 {
    transform: rotate(180deg);
  }

  /* 确保图表容器有正确的尺寸 */
  .chart-container {
    width: 100%;
    height: 400px;
  }
  .bottom {
    height: $bottomHeight;
    background: #397dcd;
    width: 100%;
    display: flex;
    &-left {
      width: 30%;
    }
    &-middle {
      width: 50%;
      padding-top: 70px;
      p {
        text-align: center;
        color: #fff;
      }
    }
    &-right {
      width: 30%;
    }
  }
}
</style>
