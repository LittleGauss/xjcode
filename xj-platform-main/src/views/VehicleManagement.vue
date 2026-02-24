<template>
  <div class="home-container">
    <!-- 使用通用页头 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @logout="handleLogout"
      @navigate="handleNavigate"
    />
    <div class="container mx-auto px-4 py-8">
      <!-- 标题区域 -->
      <div class="flex justify-between items-center mb-6">
        <h1 class="text-2xl font-bold text-gray-800">公车管理系统</h1>
        <div class="flex space-x-4">
          <button
            v-if="canAddVehicle && $hasPermission('VEHICLE:MANAGE')"
            class="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded flex items-center"
            @click="openVehicleModal"
          >
            <i class="fas fa-car mr-2"></i>新增车辆
          </button>
          <button
            v-if="canAddDispatch && $hasPermission('VEHICLE:RECORD')"
            class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded flex items-center"
            @click="openAddModal"
          >
            <i class="fas fa-plus mr-2"></i>新增派车记录
          </button>
        </div>
      </div>

      <!-- 标签页切换 (车队负责人可见) -->
      <div class="mb-6">
        <div class="border-b border-gray-200">
          <nav class="-mb-px flex space-x-8">
            <button
              @click="activeTab = 'vehicles'"
              :class="{
                'border-blue-500 text-blue-600': activeTab === 'vehicles',
                'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300':
                  activeTab !== 'vehicles',
              }"
              class="whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm"
            >
              <i class="fas fa-car mr-2"></i>车辆列表
            </button>
            <!-- 派车记录标签已移除 -->
          </nav>
        </div>
      </div>

      <!-- 车辆列表表格 (按权限 + 标签切换显示) -->
      <div
        v-if="activeTab === 'vehicles' && $hasPermission('VEHICLE:VIEW')"
        class="bg-white rounded-lg shadow overflow-hidden mt-8"
      >
        <div class="overflow-x-auto">
          <table class="w-full striped-table">
            <thead class="bg-gray-100">
              <tr>
                <th
                  v-for="col in columns"
                  :key="col"
                  class="px-6 py-3 text-xs text-gray-500"
                >
                  {{ col }}
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200">
              <tr v-for="(item, index) in records" :key="item.id">
                <td class="px-6 py-4 text-sm">{{ item.plateNumber }}</td>
                <td class="px-6 py-4 text-sm">{{ item.brand }}</td>
                <td class="px-6 py-4 text-sm">{{ item.model }}</td>
                <td class="px-6 py-4 text-sm">
                  {{ item.displacement || "-" }}
                </td>
                <td class="px-6 py-4 text-sm">{{ item.fuelType || "-" }}</td>
                <td class="px-6 py-4 text-sm">
                  <span
                    :class="{
                      'px-2 py-1 rounded text-xs font-semibold': true,
                      'bg-green-100 text-green-800':
                        item.vehicleStatus === '正常',
                      'bg-yellow-100 text-yellow-800':
                        item.vehicleStatus === '报停',
                      'bg-red-100 text-red-800': item.vehicleStatus === '报废',
                    }"
                  >
                    {{ item.vehicleStatus || "正常" }}
                  </span>
                </td>
                <td class="px-6 py-4 text-sm">
                  {{
                    getDepartmentName(item.departmentId) ||
                    item.department ||
                    "-"
                  }}
                </td>
                <td class="px-6 py-4 text-sm">{{ item.mileage }}</td>
                <td class="px-6 py-4 text-sm">
                  {{ formatDate(item.createdAt) }}
                </td>
                <td class="px-6 py-4 text-sm">
                  <button
                    class="text-blue-500 hover:text-blue-700 mr-2"
                    v-if="$hasPermission('VEHICLE:MANAGE')"
                    @click="editItem(index)"
                  >
                    <i class="fas fa-edit"></i>
                  </button>
                  <button
                    class="text-red-500 hover:text-red-700"
                    @click="deleteItem(index)"
                  >
                    <i class="fas fa-trash"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <!-- 派车记录表格已移除 -->

      <!-- 统计图表区域（放在记录最下方，按权限显示） -->
      <div class="bg-white rounded-lg shadow overflow-hidden mt-8 mb-8 p-6">
        <div class="flex justify-between items-center mb-6">
          <div>
            <h2 class="text-xl font-semibold mb-2">统计分析</h2>
            <div class="text-sm text-gray-600">
              <span v-if="statisticsDimension === 'vehicle'">
                按<strong>单车</strong>统计各项费用和油耗数据
              </span>
              <span v-else>
                按<strong>{{
                  period === "month"
                    ? "月度"
                    : period === "quarter"
                    ? "季度"
                    : "年度"
                }}</strong
                >统计费用类型堆叠柱状图
              </span>
            </div>
          </div>
          <div class="flex space-x-4 items-center">
            <button
              class="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded flex items-center"
              v-if="$hasPermission('NOTICE:EXPORT_UNREAD')"
              @click="exportToExcel"
            >
              <i class="fas fa-file-excel mr-2"></i>导出Excel
            </button>
            <!-- 统计维度选择 -->
            <div class="relative">
              <select
                v-model="statisticsDimension"
                @change="onStatisticsDimensionChange"
                class="appearance-none bg-white border border-gray-300 rounded px-4 py-2 pr-8 font-semibold"
              >
                <option value="time">时间维度</option>
                <option value="vehicle">单车维度</option>
              </select>
              <div
                class="absolute inset-y-0 right-0 flex items-center px-2 pointer-events-none"
              >
                <i class="fas fa-chevron-down text-gray-500"></i>
              </div>
            </div>
            <!-- 时间维度的周期选择 -->
            <div v-if="statisticsDimension === 'time'" class="relative">
              <select
                v-model="period"
                class="appearance-none bg-white border border-gray-300 rounded px-4 py-2 pr-8"
              >
                <option value="month">月度统计</option>
                <option value="quarter">季度统计</option>
                <option value="year">年度统计</option>
              </select>
              <div
                class="absolute inset-y-0 right-0 flex items-center px-2 pointer-events-none"
              >
                <i class="fas fa-chevron-down text-gray-500"></i>
              </div>
            </div>
            <div
              v-if="
                statisticsDimension === 'time' &&
                (period === 'month' || period === 'quarter')
              "
              class="relative"
            >
              <select
                v-model="selectedYear"
                class="appearance-none bg-white border border-gray-300 rounded px-4 py-2 pr-8"
              >
                <option v-for="y in availableYears" :key="y" :value="y">
                  {{ y }} 年
                </option>
              </select>
              <div
                class="absolute inset-y-0 right-0 flex items-center px-2 pointer-events-none"
              >
                <i class="fas fa-chevron-down text-gray-500"></i>
              </div>
            </div>
            <div v-if="period === 'year'" class="flex space-x-2 items-center">
              <div class="relative">
                <select
                  v-model="startYear"
                  class="appearance-none bg-white border border-gray-300 rounded px-3 py-2 pr-8"
                >
                  <option v-for="y in availableYears" :key="y" :value="y">
                    {{ y }}
                  </option>
                </select>
                <div
                  class="absolute inset-y-0 right-0 flex items-center px-2 pointer-events-none"
                >
                  <i class="fas fa-chevron-down text-gray-500"></i>
                </div>
              </div>
              <span class="text-sm text-gray-600">至</span>
              <div class="relative">
                <select
                  v-model="endYear"
                  class="appearance-none bg-white border border-gray-300 rounded px-3 py-2 pr-8"
                >
                  <option
                    v-for="y in availableYears"
                    :key="y + '-end'"
                    :value="y"
                  >
                    {{ y }}
                  </option>
                </select>
                <div
                  class="absolute inset-y-0 right-0 flex items-center px-2 pointer-events-none"
                >
                  <i class="fas fa-chevron-down text-gray-500"></i>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 时间维度统计表（替代柱状图） -->
        <div v-if="statisticsDimension === 'time'" class="overflow-x-auto">
          <table class="w-full border-collapse">
            <thead class="bg-gray-100">
              <tr>
                <th
                  class="border px-4 py-3 text-sm font-semibold text-gray-700"
                >
                  统计周期
                </th>
                <th
                  class="border px-4 py-3 text-sm font-semibold text-gray-700"
                >
                  累计里程(km)
                </th>
                <th
                  class="border px-4 py-3 text-sm font-semibold text-gray-700"
                >
                  燃油费(元)
                </th>
                <th
                  class="border px-4 py-3 text-sm font-semibold text-gray-700"
                >
                  过路费(元)
                </th>
                <th
                  class="border px-4 py-3 text-sm font-semibold text-gray-700"
                >
                  停车费(元)
                </th>
                <th
                  class="border px-4 py-3 text-sm font-semibold text-gray-700"
                >
                  维修费(元)
                </th>
                <th
                  class="border px-4 py-3 text-sm font-semibold text-gray-700"
                >
                  保险费(元)
                </th>
                <th
                  class="border px-4 py-3 text-sm font-semibold text-gray-700"
                >
                  年审费(元)
                </th>
                <th
                  class="border px-4 py-3 text-sm font-semibold text-gray-700"
                >
                  合计(元)
                </th>
                <th
                  class="border px-4 py-3 text-sm font-semibold text-blue-700 bg-blue-50"
                >
                  百公里油耗(升/100km)
                </th>
                <th
                  class="border px-4 py-3 text-sm font-semibold text-gray-700"
                >
                  备注
                </th>
                <th
                  v-if="period === 'month'"
                  class="border px-4 py-3 text-sm font-semibold text-gray-700"
                >
                  操作
                </th>
              </tr>
            </thead>
            <tbody>
              <template v-for="(row, idx) in timeSummaryRows">
                <tr :key="'row-' + idx" class="hover:bg-gray-50">
                  <td class="border px-4 py-3 text-sm">
                    {{ row["统计周期"] }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right text-blue-600">
                    {{ toFixed0(row["累计里程(km)"]) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ toFixed2(row["燃油费(元)"]) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ toFixed2(row["过路费(元)"]) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ toFixed2(row["停车费(元)"]) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ toFixed2(row["维修费(元)"]) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ toFixed2(row["保险费(元)"]) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ toFixed2(row["年审费(元)"]) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right font-semibold">
                    {{ toFixed2(row["合计(元)"]) }}
                  </td>
                  <td
                    class="border px-4 py-3 text-sm text-center font-bold text-blue-700 bg-blue-50"
                  >
                    {{ row["百公里油耗(升/100km)"] }}
                  </td>
                  <td class="border px-4 py-3 text-sm">
                    <span class="text-gray-700">{{ row["备注"] || "-" }}</span>
                  </td>
                  <td
                    v-if="period === 'month'"
                    class="border px-4 py-3 text-sm"
                  >
                    <button
                      class="px-3 py-1 text-xs bg-blue-100 text-blue-700 rounded"
                      @click="
                        toggleMonthExpand(parseMonthFromLabel(row['统计周期']))
                      "
                    >
                      <span
                        v-if="
                          isMonthExpanded(parseMonthFromLabel(row['统计周期']))
                        "
                        >收起车辆</span
                      >
                      <span v-else>展开车辆</span>
                    </button>
                  </td>
                </tr>
                <tr
                  :key="'expand-' + idx"
                  v-show="
                    period === 'month' &&
                    isMonthExpanded(parseMonthFromLabel(row['统计周期']))
                  "
                >
                  <td
                    :colspan="period === 'month' ? 12 : 11"
                    class="border px-4 py-3 bg-gray-50"
                  >
                    <div
                      class="text-sm font-semibold mb-2 flex items-center justify-between"
                    >
                      <span>{{ row["统计周期"] }} - 车辆月度明细</span>
                      <button
                        class="px-2 py-1 text-xs bg-green-100 text-green-700 rounded flex items-center"
                        @click="
                          exportMonthlyVehicleStatsSingle(
                            parseMonthFromLabel(row['统计周期'])
                          )
                        "
                      >
                        <i class="fas fa-file-excel mr-1"></i>导出Excel
                      </button>
                    </div>
                    <div class="overflow-x-auto">
                      <table class="w-full border-collapse">
                        <thead class="bg-gray-100">
                          <tr>
                            <th class="border px-3 py-2 text-xs text-gray-700">
                              车牌号
                            </th>
                            <th class="border px-3 py-2 text-xs text-gray-700">
                              品牌型号
                            </th>
                            <th class="border px-3 py-2 text-xs text-gray-700">
                              累计里程(km)
                            </th>
                            <th class="border px-3 py-2 text-xs text-gray-700">
                              燃油费(元)
                            </th>
                            <th class="border px-3 py-2 text-xs text-gray-700">
                              过路费(元)
                            </th>
                            <th class="border px-3 py-2 text-xs text-gray-700">
                              停车费(元)
                            </th>
                            <th class="border px-3 py-2 text-xs text-gray-700">
                              保险费(元)
                            </th>
                            <th class="border px-3 py-2 text-xs text-gray-700">
                              年审费(元)
                            </th>
                            <th class="border px-3 py-2 text-xs text-gray-700">
                              维修费(元)
                            </th>
                            <th class="border px-3 py-2 text-xs text-gray-700">
                              合计(元)
                            </th>
                            <th
                              class="border px-3 py-2 text-xs text-blue-700 bg-blue-50"
                            >
                              百公里油耗(升/100km)
                            </th>
                            <th class="border px-3 py-2 text-xs text-gray-700">
                              操作
                            </th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr
                            v-for="vs in getMonthlyVehicleStats(
                              parseMonthFromLabel(row['统计周期'])
                            )"
                            :key="vs.plateNumber"
                            class="hover:bg-white"
                          >
                            <td class="border px-3 py-2 text-xs">
                              {{ vs.plateNumber }}
                            </td>
                            <td class="border px-3 py-2 text-xs">
                              {{ vs.brand }} {{ vs.model }}
                            </td>
                            <td
                              class="border px-3 py-2 text-xs text-right text-blue-600"
                            >
                              {{ vs.totalMileage.toFixed(0) }}
                            </td>
                            <td class="border px-3 py-2 text-xs text-right">
                              {{ vs.fuelCost.toFixed(2) }}
                            </td>
                            <td class="border px-3 py-2 text-xs text-right">
                              {{ vs.tollFee.toFixed(2) }}
                            </td>
                            <td class="border px-3 py-2 text-xs text-right">
                              {{ vs.parkingFee.toFixed(2) }}
                            </td>
                            <td class="border px-3 py-2 text-xs text-right">
                              {{ vs.insuranceFee.toFixed(2) }}
                            </td>
                            <td class="border px-3 py-2 text-xs text-right">
                              {{ vs.annualInspectionFee.toFixed(2) }}
                            </td>
                            <td class="border px-3 py-2 text-xs text-right">
                              {{ vs.repairCost.toFixed(2) }}
                            </td>
                            <td
                              class="border px-3 py-2 text-xs text-right font-semibold"
                            >
                              {{ vs.totalCost.toFixed(2) }}
                            </td>
                            <td
                              class="border px-3 py-2 text-xs text-center font-bold text-blue-700 bg-blue-50"
                            >
                              <span v-if="vs.fuelConsumptionPer100km > 0">{{
                                vs.fuelConsumptionPer100km.toFixed(2)
                              }}</span>
                              <span v-else class="text-gray-400">-</span>
                            </td>
                            <td class="border px-3 py-2 text-xs">
                              <button
                                class="px-2 py-1 text-xs bg-blue-100 text-blue-700 rounded mr-2"
                                @click="
                                  openVehicleMonthModal(
                                    parseMonthFromLabel(row['统计周期']),
                                    vs.plateNumber
                                  )
                                "
                              >
                                明细
                              </button>
                            </td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>

        <!-- 单车维度统计表格 -->
        <div v-if="statisticsDimension === 'vehicle'">
          <div class="overflow-x-auto">
            <table class="w-full border-collapse">
              <thead class="bg-gray-100">
                <tr>
                  <th
                    class="border px-4 py-3 text-sm font-semibold text-gray-700"
                  >
                    车牌号
                  </th>
                  <th
                    class="border px-4 py-3 text-sm font-semibold text-gray-700"
                  >
                    品牌型号
                  </th>
                  <th
                    class="border px-4 py-3 text-sm font-semibold text-gray-700"
                  >
                    累计里程(km)
                  </th>
                  <th
                    class="border px-4 py-3 text-sm font-semibold text-gray-700"
                  >
                    燃油费(元)
                  </th>
                  <th
                    class="border px-4 py-3 text-sm font-semibold text-gray-700"
                  >
                    过路费(元)
                  </th>
                  <th
                    class="border px-4 py-3 text-sm font-semibold text-gray-700"
                  >
                    停车费(元)
                  </th>
                  <th
                    class="border px-4 py-3 text-sm font-semibold text-gray-700"
                  >
                    保险费(元)
                  </th>
                  <th
                    class="border px-4 py-3 text-sm font-semibold text-gray-700"
                  >
                    年审费(元)
                  </th>
                  <th
                    class="border px-4 py-3 text-sm font-semibold text-gray-700"
                  >
                    维修费(元)
                  </th>
                  <th
                    class="border px-4 py-3 text-sm font-semibold text-gray-700"
                  >
                    总费用(元)
                  </th>
                  <th
                    class="border px-4 py-3 text-sm font-semibold text-blue-700 bg-blue-50"
                  >
                    百公里油耗(升/100km)
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="stat in vehicleStatistics"
                  :key="stat.plateNumber"
                  class="hover:bg-gray-50"
                >
                  <td class="border px-4 py-3 text-sm font-semibold">
                    {{ stat.plateNumber }}
                  </td>
                  <td class="border px-4 py-3 text-sm">
                    {{ stat.brand }} {{ stat.model }}
                  </td>
                  <td
                    class="border px-4 py-3 text-sm text-right font-semibold text-blue-600"
                  >
                    {{ stat.totalMileage.toFixed(0) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ stat.fuelCost.toFixed(2) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ stat.tollFee.toFixed(2) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ stat.parkingFee.toFixed(2) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ stat.insuranceFee.toFixed(2) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ stat.annualInspectionFee.toFixed(2) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ stat.repairCost.toFixed(2) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right font-semibold">
                    {{ stat.totalCost.toFixed(2) }}
                  </td>
                  <td
                    class="border px-4 py-3 text-sm text-center font-bold text-blue-700 bg-blue-50"
                  >
                    <span v-if="stat.fuelConsumptionPer100km > 0">
                      {{ stat.fuelConsumptionPer100km.toFixed(2) }}
                    </span>
                    <span v-else class="text-gray-400">-</span>
                  </td>
                </tr>
                <!-- 合计行 -->
                <tr class="bg-yellow-50 font-bold">
                  <td class="border px-4 py-3 text-sm" colspan="2">合计</td>
                  <td class="border px-4 py-3 text-sm text-right text-blue-600">
                    {{ totalStatistics.totalMileage.toFixed(0) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ totalStatistics.fuelCost.toFixed(2) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ totalStatistics.tollFee.toFixed(2) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ totalStatistics.parkingFee.toFixed(2) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ totalStatistics.insuranceFee.toFixed(2) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ totalStatistics.annualInspectionFee.toFixed(2) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ totalStatistics.repairCost.toFixed(2) }}
                  </td>
                  <td class="border px-4 py-3 text-sm text-right">
                    {{ totalStatistics.totalCost.toFixed(2) }}
                  </td>
                  <td
                    class="border px-4 py-3 text-sm text-center text-blue-700 bg-blue-100"
                  >
                    <span v-if="totalStatistics.avgFuelConsumptionPer100km > 0">
                      {{
                        totalStatistics.avgFuelConsumptionPer100km.toFixed(2)
                      }}
                    </span>
                    <span v-else class="text-gray-400">-</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 百公里油耗计算说明 -->
          <div class="mt-4 p-4 bg-blue-50 rounded border border-blue-200">
            <h4 class="text-sm font-semibold text-blue-800 mb-2">
              <i class="fas fa-info-circle mr-2"></i>百公里油耗计算公式
            </h4>
            <div class="text-xs text-blue-700 space-y-1">
              <p>
                📊 <strong>单车百公里油耗</strong> = (该车累计燃油费 ÷ 6.5 ÷
                该车累计里程) × 100
              </p>
              <p>
                📊 <strong>平均百公里油耗</strong> = (所有车辆燃油费总和 ÷ 6.5 ÷
                所有车辆里程总和) × 100
              </p>
              <p class="text-blue-600 mt-2">
                💡 说明：基于实际发生的里程和燃油费用计算，单位为"升/100公里"
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- 新增车辆模态框 -->
      <div
        v-if="showVehicleModal"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      >
        <div
          class="bg-white rounded-lg shadow-xl w-full max-w-3xl max-h-[90vh] overflow-y-auto"
        >
          <div
            class="flex justify-between items-center border-b px-6 py-4 sticky top-0 bg-white"
          >
            <h3 class="text-lg font-semibold">新增车辆</h3>
            <button @click="closeVehicleModal">
              <i class="fas fa-times"></i>
            </button>
          </div>
          <div class="p-6 space-y-4">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <input
                v-model="vehicleForm.plateNumber"
                placeholder="车牌号"
                class="border p-2 rounded"
              />
              <input
                v-model="vehicleForm.brand"
                placeholder="品牌"
                class="border p-2 rounded"
              />
              <input
                v-model="vehicleForm.model"
                placeholder="型号"
                class="border p-2 rounded"
              />
              <el-select
                v-model="vehicleForm.departmentId"
                placeholder="归属部门"
                class="w-full"
                clearable
              >
                <el-option
                  v-for="d in departmentList"
                  :key="d.id"
                  :label="d.name"
                  :value="d.id"
                />
              </el-select>
              <input
                v-model="vehicleForm.mileage"
                type="number"
                placeholder="总里程 (公里)"
                class="border p-2 rounded"
              />
              <input
                v-model="vehicleForm.purchaseDate"
                type="date"
                placeholder="购置日期"
                class="border p-2 rounded"
              />
              <el-select
                v-model="vehicleForm.insuranceStatus"
                placeholder="保险状态"
                class="w-full"
                clearable
              >
                <el-option
                  v-for="opt in insuranceOptions"
                  :key="opt"
                  :label="opt"
                  :value="opt"
                />
              </el-select>
              <textarea
                v-model="vehicleForm.remark"
                placeholder="备注信息"
                class="border p-2 rounded md:col-span-2"
                rows="3"
              ></textarea>
            </div>
          </div>
          <div class="flex justify-end space-x-3 border-t px-6 py-4">
            <button class="px-4 py-2 border rounded" @click="closeVehicleModal">
              取消
            </button>
            <button
              class="px-4 py-2 bg-green-500 text-white rounded"
              @click="saveVehicle"
            >
              提交
            </button>
          </div>
        </div>
      </div>

      <!-- 新增/编辑费用记录模态框 -->
      <div
        v-if="showModal && canAddDispatch"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
        style="z-index: 200"
      >
        <div
          class="bg-white rounded-lg shadow-xl w-full max-w-4xl max-h-screen overflow-y-auto"
        >
          <div
            class="flex justify-between items-center border-b px-6 py-4 sticky top-0 bg-white"
          >
            <h3 class="text-lg font-semibold">{{ modalTitle }}</h3>
            <button @click="closeModal"><i class="fas fa-times"></i></button>
          </div>
          <div class="p-6">
            <!-- 基本信息区域 -->
            <div class="mb-6">
              <h4
                class="text-md font-semibold mb-3 text-gray-700 border-b pb-2"
              >
                <i class="fas fa-car mr-2"></i>车辆基本信息
              </h4>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <select
                  v-model="form.plateNumber"
                  @change="onPlateSelected"
                  class="border p-2 rounded"
                >
                  <option value="">-- 选择车牌号 * --</option>
                  <option
                    v-for="v in vehicleList"
                    :key="v.id"
                    :value="v.plateNumber"
                  >
                    {{ v.plateNumber }} - {{ v.brand }} {{ v.model }}
                  </option>
                </select>
                <input
                  v-model="form.brand"
                  placeholder="品牌"
                  class="border p-2 rounded bg-gray-50"
                  readonly
                />
                <input
                  v-model="form.model"
                  placeholder="型号"
                  class="border p-2 rounded bg-gray-50"
                  readonly
                />
              </div>
            </div>

            <!-- 用车信息区域 -->
            <div class="mb-6">
              <h4
                class="text-md font-semibold mb-3 text-gray-700 border-b pb-2"
              >
                <i class="fas fa-calendar-alt mr-2"></i>用车信息
              </h4>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <!-- 派车单号：创建时由后端生成；编辑时只读展示 -->
                <div>
                  <input
                    v-if="editId"
                    v-model="form.dispatchNumber"
                    placeholder="派车单号"
                    class="border p-2 rounded w-full bg-gray-50"
                    readonly
                  />
                  <input
                    v-else
                    placeholder="系统自动生成，无需填写"
                    class="border p-2 rounded w-full bg-gray-100"
                    readonly
                  />
                </div>
                <div>
                  <label class="block text-xs text-gray-600 mb-1"
                    >用车日期 *</label
                  >
                  <input
                    v-model="form.useDate"
                    type="date"
                    class="border p-2 rounded w-full"
                  />
                </div>
                <div>
                  <label class="block text-xs text-gray-600 mb-1"
                    >出车时间</label
                  >
                  <input
                    v-model="form.departureTime"
                    type="time"
                    class="border p-2 rounded w-full"
                  />
                </div>
                <div>
                  <label class="block text-xs text-gray-600 mb-1"
                    >返回时间</label
                  >
                  <input
                    v-model="form.returnTime"
                    type="time"
                    class="border p-2 rounded w-full"
                  />
                </div>
                <div>
                  <label class="block text-xs text-gray-600 mb-1"
                    >用车部门</label
                  >
                  <el-select
                    v-model="form.departmentId"
                    placeholder="请选择用车部门"
                    clearable
                    class="w-full"
                    :key="departmentSelectKey"
                    @change="onDepartmentChange"
                  >
                    <el-option
                      v-for="d in departmentList"
                      :key="d.id"
                      :label="d.name"
                      :value="Number(d.id)"
                    />
                  </el-select>
                </div>
                <div>
                  <label class="block text-xs text-gray-600 mb-1">用车人</label>
                  <el-select
                    v-model="form.userName"
                    placeholder="请选择用车人"
                    filterable
                    clearable
                    class="w-full"
                  >
                    <el-option
                      v-for="u in userList"
                      :key="u.id"
                      :label="formatUserLabel(u)"
                      :value="formatUserValue(u)"
                    />
                  </el-select>
                </div>
              </div>
            </div>

            <!-- 里程信息区域 -->
            <div class="mb-6">
              <h4
                class="text-md font-semibold mb-3 text-gray-700 border-b pb-2"
              >
                <i class="fas fa-road mr-2"></i>里程信息
              </h4>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label class="block text-xs text-gray-600 mb-1"
                    >出车里程数（公里）</label
                  >
                  <input
                    v-model.number="form.startMileage"
                    type="number"
                    placeholder="出车时里程表读数"
                    class="border p-2 rounded w-full"
                    @input="calculateMileage"
                  />
                </div>
                <div>
                  <label class="block text-xs text-gray-600 mb-1"
                    >回车里程数（公里）</label
                  >
                  <input
                    v-model.number="form.endMileage"
                    type="number"
                    placeholder="回车时里程表读数"
                    class="border p-2 rounded w-full"
                    @input="calculateMileage"
                  />
                </div>
                <div>
                  <label class="block text-xs text-gray-600 mb-1"
                    >本次行驶里程（公里）</label
                  >
                  <input
                    v-model.number="form.tripMileage"
                    type="number"
                    placeholder="自动计算"
                    class="border p-2 rounded w-full bg-blue-50 font-semibold"
                    readonly
                  />
                </div>
              </div>
            </div>

            <!-- 费用信息区域 -->
            <div class="mb-6">
              <h4
                class="text-md font-semibold mb-3 text-gray-700 border-b pb-2"
              >
                <i class="fas fa-dollar-sign mr-2"></i>费用信息
              </h4>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-xs text-gray-600 mb-1"
                    >燃油费（元）</label
                  >
                  <input
                    v-model.number="form.fuelCost"
                    type="number"
                    step="0.01"
                    placeholder="加油费用"
                    class="border p-2 rounded w-full"
                  />
                </div>
                <div>
                  <label class="block text-xs text-gray-600 mb-1"
                    >过路费（元）</label
                  >
                  <input
                    v-model.number="form.tollFee"
                    type="number"
                    step="0.01"
                    placeholder="高速/过路费"
                    class="border p-2 rounded w-full"
                  />
                </div>
                <div>
                  <label class="block text-xs text-gray-600 mb-1"
                    >停车费（元）</label
                  >
                  <input
                    v-model.number="form.parkingFee"
                    type="number"
                    step="0.01"
                    placeholder="停车费用"
                    class="border p-2 rounded w-full"
                  />
                </div>
                <div>
                  <label class="block text-xs text-gray-600 mb-1"
                    >保险费（元）</label
                  >
                  <input
                    v-model.number="form.insuranceFee"
                    type="number"
                    step="0.01"
                    placeholder="保险费用"
                    class="border p-2 rounded w-full"
                  />
                </div>
                <div>
                  <label class="block text-xs text-gray-600 mb-1"
                    >年审费（元）</label
                  >
                  <input
                    v-model.number="form.annualInspectionFee"
                    type="number"
                    step="0.01"
                    placeholder="年审费用"
                    class="border p-2 rounded w-full"
                  />
                </div>
                <div>
                  <label class="block text-xs text-gray-600 mb-1"
                    >维修费（元）</label
                  >
                  <input
                    v-model.number="form.repairCost"
                    type="number"
                    step="0.01"
                    placeholder="维修保养费用"
                    class="border p-2 rounded w-full"
                  />
                </div>
              </div>
            </div>

            <!-- 备注信息 -->
            <div class="mb-4">
              <h4
                class="text-md font-semibold mb-3 text-gray-700 border-b pb-2"
              >
                <i class="fas fa-comment mr-2"></i>备注说明
              </h4>
              <textarea
                v-model="form.remark"
                placeholder="请填写用车事由、目的地等信息"
                class="border p-2 rounded w-full"
                rows="3"
              ></textarea>
            </div>

            <!-- 附件上传区域 (按权限显示) -->
            <div v-if="$hasPermission('VEHICLE:UPLOAD')" class="border-t pt-4">
              <h4 class="text-sm font-semibold mb-3 text-gray-700">
                <i class="fas fa-paperclip mr-2"></i>附件上传（支持照片）
              </h4>
              <div class="mb-3">
                <label
                  class="inline-flex items-center px-4 py-2 bg-blue-50 text-blue-600 rounded cursor-pointer hover:bg-blue-100 border border-blue-300"
                >
                  <i class="fas fa-upload mr-2"></i>选择文件
                  <input
                    type="file"
                    ref="fileInput"
                    @change="handleFileSelect"
                    accept="image/*"
                    multiple
                    class="hidden"
                  />
                </label>
                <span class="ml-3 text-xs text-gray-500"
                  >支持JPG、PNG、GIF等图片格式，单个文件最大5MB</span
                >
              </div>

              <!-- 已选择的附件列表 -->
              <div v-if="attachments.length > 0" class="space-y-2">
                <div
                  v-for="(file, index) in attachments"
                  :key="index"
                  class="flex items-center justify-between bg-gray-50 p-2 rounded"
                >
                  <div class="flex items-center space-x-3 flex-1">
                    <!-- 图片预览 -->
                    <img
                      v-if="file.preview"
                      :src="file.preview"
                      class="w-12 h-12 object-cover rounded"
                      alt="预览"
                    />
                    <div class="flex-1 min-w-0">
                      <p class="text-sm text-gray-700 truncate">
                        {{ file.name }}
                      </p>
                      <p class="text-xs text-gray-500">
                        {{ formatFileSize(file.size) }}
                      </p>
                    </div>
                  </div>
                  <button
                    @click="removeAttachment(index)"
                    class="text-red-500 hover:text-red-700 ml-2"
                  >
                    <i class="fas fa-times"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div class="flex justify-end space-x-3 border-t px-6 py-4">
            <button class="px-4 py-2 border rounded" @click="closeModal">
              取消
            </button>
            <button
              class="px-4 py-2 bg-blue-500 text-white rounded"
              @click="submitForm"
            >
              提交
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 派车详情弹窗（美化替代 alert） -->
    <div
      v-if="showDetailModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      sytle="z-index: 150"
    >
      <div
        class="bg-white rounded-lg shadow-xl w-full max-w-3xl max-h-[90vh] overflow-y-auto"
      >
        <div
          class="flex justify-between items-center border-b px-6 py-4 sticky top-0 bg-white"
        >
          <h3 class="text-lg font-semibold">派车单详情</h3>
          <button
            @click="closeDetailModal"
            class="text-gray-600 hover:text-gray-900"
          >
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="p-6 space-y-4">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <div class="text-sm text-gray-500">派车单号</div>
              <div class="text-lg font-semibold">
                {{ detailRecord.dispatchNumber || "-" }}
              </div>
            </div>
            <div>
              <div class="text-sm text-gray-500">车辆</div>
              <div class="text-lg">
                {{ detailRecord.plateNumber || "-" }}
                <span class="text-gray-500"
                  >{{ detailRecord.brand || "" }}
                  {{ detailRecord.model || "" }}</span
                >
              </div>
            </div>
            <div>
              <div class="text-sm text-gray-500">用车日期</div>
              <div>{{ detailRecord.useDate || "-" }}</div>
            </div>
            <div>
              <div class="text-sm text-gray-500">用车人</div>
              <div>{{ detailRecord.userName || "-" }}</div>
            </div>
            <div>
              <div class="text-sm text-gray-500">用车部门</div>
              <div>{{ detailRecord.department || "-" }}</div>
            </div>
            <div>
              <div class="text-sm text-gray-500">出 / 回车时间</div>
              <div>
                {{ detailRecord.departureTime || "-" }} /
                {{ detailRecord.returnTime || "-" }}
              </div>
            </div>
          </div>

          <div class="border-t pt-4">
            <h4 class="text-md font-semibold mb-2">里程信息</h4>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <div class="text-sm text-gray-500">出车里程</div>
                <div>{{ detailRecord.startMileage || 0 }} km</div>
              </div>
              <div>
                <div class="text-sm text-gray-500">回车里程</div>
                <div>{{ detailRecord.endMileage || 0 }} km</div>
              </div>
              <div>
                <div class="text-sm text-gray-500">本次行驶</div>
                <div class="font-semibold text-blue-600">
                  {{ detailRecord.tripMileage || 0 }} km
                </div>
              </div>
            </div>
          </div>

          <div class="border-t pt-4">
            <h4 class="text-md font-semibold mb-2">费用信息</h4>
            <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div>
                <div class="text-sm text-gray-500">燃油费</div>
                <div>¥{{ detailRecord.fuelCost || 0 }}</div>
              </div>
              <div>
                <div class="text-sm text-gray-500">过路费</div>
                <div>¥{{ detailRecord.tollFee || 0 }}</div>
              </div>
              <div>
                <div class="text-sm text-gray-500">停车费</div>
                <div>¥{{ detailRecord.parkingFee || 0 }}</div>
              </div>
              <div>
                <div class="text-sm text-gray-500">维修费</div>
                <div>¥{{ detailRecord.repairCost || 0 }}</div>
              </div>
              <div>
                <div class="text-sm text-gray-500">保险费</div>
                <div>¥{{ detailRecord.insuranceFee || 0 }}</div>
              </div>
              <div>
                <div class="text-sm text-gray-500">年审费</div>
                <div>¥{{ detailRecord.annualInspectionFee || 0 }}</div>
              </div>
            </div>
            <div class="mt-3 text-right font-semibold">
              合计：¥{{
                (
                  Number(detailRecord.fuelCost || 0) +
                  Number(detailRecord.tollFee || 0) +
                  Number(detailRecord.parkingFee || 0) +
                  Number(detailRecord.insuranceFee || 0) +
                  Number(detailRecord.annualInspectionFee || 0) +
                  Number(detailRecord.repairCost || 0)
                ).toFixed(2)
              }}
            </div>
          </div>

          <div class="border-t pt-4">
            <h4 class="text-md font-semibold mb-2">备注</h4>
            <div class="whitespace-pre-line text-sm text-gray-700">
              {{ detailRecord.remark || "-" }}
            </div>
          </div>

          <div
            v-if="detailRecord.attachments && detailRecord.attachments.length"
            class="border-t pt-4"
          >
            <h4 class="text-md font-semibold mb-2">
              附件（{{ detailRecord.attachments.length }}）
            </h4>
            <div class="grid grid-cols-3 gap-3">
              <div
                v-for="(att, idx) in detailRecord.attachments"
                :key="idx"
                class="border rounded p-2 flex flex-col items-center"
              >
                <img
                  v-if="att.preview || att.fileUrl"
                  :src="att.preview || att.fileUrl"
                  class="w-28 h-20 object-cover mb-2"
                />
                <div class="text-xs text-gray-600 truncate w-28 text-center">
                  {{ att.name || "附件-" + (idx + 1) }}
                </div>
                <div class="mt-2 flex gap-2">
                  <button
                    class="px-2 py-1 text-xs bg-blue-100 text-blue-700 rounded"
                    @click.prevent="previewAttachment(att)"
                    title="预览"
                  >
                    <i class="fas fa-eye"></i>
                  </button>
                  <button
                    class="px-2 py-1 text-xs bg-green-100 text-green-700 rounded"
                    @click.prevent="downloadAttachment(att)"
                    title="下载"
                  >
                    <i class="fas fa-download"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div class="flex justify-end pt-4">
            <button
              class="px-4 py-2 border rounded mr-2"
              @click="closeDetailModal"
            >
              关闭
            </button>
          </div>
        </div>
      </div>
    </div>
    <!-- 使用通用页脚 -->
    <!-- 车辆月度明细弹窗 -->
    <div
      v-if="showVehicleMonthModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      style="z-index: 160"
    >
      <div
        class="bg-white rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] overflow-y-auto"
      >
        <div
          class="flex justify-between items-center border-b px-6 py-4 sticky top-0 bg-white"
        >
          <h3 class="text-lg font-semibold">{{ vehicleMonthModalTitle }}</h3>
          <button
            @click="closeVehicleMonthModal"
            class="text-gray-600 hover:text-gray-900"
          >
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="p-6">
          <div class="overflow-x-auto">
            <table class="w-full border-collapse">
              <thead class="bg-gray-100">
                <tr>
                  <th class="border px-3 py-2 text-xs text-gray-700">
                    派车单号
                  </th>
                  <th class="border px-3 py-2 text-xs text-gray-700">
                    用车日期
                  </th>
                  <th class="border px-3 py-2 text-xs text-gray-700">用车人</th>
                  <th class="border px-3 py-2 text-xs text-gray-700">
                    行驶里程(km)
                  </th>
                  <th class="border px-3 py-2 text-xs text-gray-700">
                    燃油费(元)
                  </th>
                  <th class="border px-3 py-2 text-xs text-gray-700">
                    过路费(元)
                  </th>
                  <th class="border px-3 py-2 text-xs text-gray-700">
                    停车费(元)
                  </th>
                  <th class="border px-3 py-2 text-xs text-gray-700">
                    维修费(元)
                  </th>
                  <th class="border px-3 py-2 text-xs text-gray-700">
                    合计(元)
                  </th>
                  <th class="border px-3 py-2 text-xs text-gray-700">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="dr in vehicleMonthRecords"
                  :key="dr.id"
                  class="hover:bg-gray-50"
                >
                  <td class="border px-3 py-2 text-xs">
                    {{ dr.dispatchNumber || "-" }}
                  </td>
                  <td class="border px-3 py-2 text-xs">
                    {{ dr.useDate || "-" }}
                  </td>
                  <td class="border px-3 py-2 text-xs">
                    {{ dr.userName || "-" }}
                  </td>
                  <td class="border px-3 py-2 text-xs text-right text-blue-600">
                    {{ dr.tripMileage || 0 }}
                  </td>
                  <td class="border px-3 py-2 text-xs text-right">
                    {{
                      (dr.fuelCost || 0).toFixed
                        ? dr.fuelCost.toFixed(2)
                        : Number(dr.fuelCost || 0).toFixed(2)
                    }}
                  </td>
                  <td class="border px-3 py-2 text-xs text-right">
                    {{
                      (dr.tollFee || 0).toFixed
                        ? dr.tollFee.toFixed(2)
                        : Number(dr.tollFee || 0).toFixed(2)
                    }}
                  </td>
                  <td class="border px-3 py-2 text-xs text-right">
                    {{
                      (dr.parkingFee || 0).toFixed
                        ? dr.parkingFee.toFixed(2)
                        : Number(dr.parkingFee || 0).toFixed(2)
                    }}
                  </td>
                  <td class="border px-3 py-2 text-xs text-right">
                    {{
                      (dr.repairCost || 0).toFixed
                        ? dr.repairCost.toFixed(2)
                        : Number(dr.repairCost || 0).toFixed(2)
                    }}
                  </td>
                  <td class="border px-3 py-2 text-xs text-right font-semibold">
                    {{
                      (
                        Number(dr.fuelCost || 0) +
                        Number(dr.tollFee || 0) +
                        Number(dr.parkingFee || 0) +
                        Number(dr.repairCost || 0) +
                        Number(dr.insuranceFee || 0) +
                        Number(dr.annualInspectionFee || 0)
                      ).toFixed(2)
                    }}
                  </td>
                  <td class="border px-3 py-2 text-xs">
                    <button
                      class="px-2 py-1 text-xs bg-blue-100 text-blue-700 rounded mr-2"
                      @click="editDispatchRecordById(dr.id)"
                    >
                      编辑
                    </button>
                    <button
                      class="px-2 py-1 text-xs bg-red-100 text-red-700 rounded"
                      @click="deleteDispatchRecordById(dr.id)"
                    >
                      删除
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="flex justify-end pt-4">
            <button
              class="px-4 py-2 border rounded"
              @click="closeVehicleMonthModal"
            >
              关闭
            </button>
          </div>
        </div>
      </div>
    </div>
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>

<script>
import { vehicleApi } from "@/api/vehicle";
import { userApi } from "@/api/usermag";
import { removeToken, getUserToken, removeUserToken } from "@/utils/auth";
// import * as echarts from "echarts"; // 已移除图表渲染
import * as XLSX from "xlsx";
import { saveAs } from "file-saver";

import HeaderComponent from "@/components/HeaderComponent.vue";
import FooterComponent from "@/components/FooterComponent.vue";

export default {
  name: "VehicleManagement",
  components: {
    HeaderComponent,
    FooterComponent,
  },
  data() {
    return {
      currentPage: "home",
      activeTab: "vehicles", // 当前激活的标签页
      statisticsDimension: "time", // 统计维度：time-时间维度, vehicle-单车维度
      period: "month",
      selectedYear: new Date().getFullYear(),
      availableYears: [],
      startYear: null,
      endYear: null,
      columns: [
        "车牌号",
        "品牌",
        "型号",
        "排量",
        "燃油类型",
        "车辆状态",
        "部门",
        "总里程",
        "创建时间",
        "操作",
      ],
      vehicleList: [],
      departmentList: [],
      userList: [],
      records: [],
      dispatchRecords: [], // 派车记录列表
      expenseRecords: [],
      chart: null,
      chartTypes: ["燃油费", "维修费", "保险费", "其他"],
      chartCategories: [],
      chartSeries: [],
      // 时间维度统计表数据（可编辑）
      timeSummaryRows: [],
      showModal: false,
      showVehicleModal: false,
      // 任务/派车详情模态
      showDetailModal: false,
      detailRecord: null,
      modalTitle: "新增公车记录",
      vehicleModalTitle: "新增车辆基本信息",
      form: {
        plateNumber: "",
        vehicleId: null,
        brand: "",
        model: "",
        departmentId: null,
        dispatchNumber: "", // 派车单号
        useDate: "", // 用车日期
        departureTime: "", // 出车时间
        returnTime: "", // 返回时间
        userName: "", // 用车人
        startMileage: "", // 出车里程
        endMileage: "", // 回车里程
        tripMileage: 0, // 本次行驶里程（自动计算）
        fuelCost: 0, // 燃油费
        tollFee: 0, // 过路费
        parkingFee: 0, // 停车费
        insuranceFee: 0, // 保险费
        annualInspectionFee: 0, // 年审费
        repairCost: 0, // 维修费
        remark: "", // 备注
      },
      vehicleForm: {
        plateNumber: "",
        brand: "",
        model: "",
        displacement: "",
        fuelType: "",
        vehicleStatus: "正常",
        departmentId: null,
        mileage: "",
        purchaseDate: "",
        insuranceStatus: "",
        remark: "",
      },
      // 保险状态可选项
      insuranceOptions: ["已投保", "有效", "未投保", "过期"],
      editIndex: null,
      editId: null,
      attachments: [], // 附件列表
      maxFileSize: 5 * 1024 * 1024, // 最大文件大小 5MB
      vehicleStatistics: [], // 单车统计数据
      totalStatistics: {
        // 汇总统计
        totalMileage: 0,
        tripCount: 0,
        fuelCost: 0,
        tollFee: 0,
        parkingFee: 0,
        repairCost: 0,
        totalCost: 0,
        avgFuelConsumptionPer100km: 0,
      },
      // 月度展开与明细
      expandedMonths: new Set(),
      monthlyVehicleStats: {},
      showVehicleMonthModal: false,
      vehicleMonthModalTitle: "车辆月度明细",
      vehicleMonthRecords: [],
      vehicleMonthInfo: { month: null, label: "" },
    };
  },
  computed: {
    // 是否有新增车辆权限（后保部）
    canAddVehicle() {
      return this.hasRole && this.hasRole("后保部");
    },
    // 是否有新增派车权限（车队负责人）
    canAddDispatch() {
      return this.hasRole && this.hasRole("车队负责人");
    },
    // 依赖编辑记录和当前选择的部门ID，强制 el-select 在编辑态重新渲染
    departmentSelectKey() {
      const id = this.editId || 0;
      const dept =
        this.form && this.form.departmentId != null
          ? this.form.departmentId
          : "null";
      return `dept-${id}-${dept}`;
    },
  },
  created() {
    // 在组件创建后安全地获取用户信息
    const storedUserInfo = getUserToken();
    if (storedUserInfo && storedUserInfo.user) {
      this.login_user = storedUserInfo.user;
      this.userRoles = storedUserInfo.roles;
      this.userPermissions = storedUserInfo.permissions || [];
    } else {
      this.$message.warning("您尚未登录，请先登录！");
      // 如果未登录，跳转到登录页面
      this.$router.push("/login");
    }
    this.fetchRecords();
    // 加载派车记录：默认在页面创建时尝试加载一次，避免初次进入为空
    this.fetchDispatchRecords();
    // 加载部门列表以供下拉选择（与 UserManagement.vue 行为一致）
    this.loadDepartments();
    // 加载用户列表供“用车人”下拉选择
    this.loadUsers();
    // 费用记录可以按需加载
    // this.fetchExpenseRecords();
  },
  methods: {
    /**
     * 通用角色判断（兼容多种存储形态）
     * - 支持后端 roles: [{id, code, name}, ...]
     * - 支持  简化的 roleName 字符串
     */
    hasRole(roleName) {
      if (!roleName) return false;
      const roles = this.userRoles || [];
      if (!Array.isArray(roles) || roles.length === 0) return false;

      // 如果用户包含超级管理员角色，短路放行（支持 code 或 name）
      for (const r of roles) {
        if (!r) continue;
        if (typeof r === "object") {
          if (r.code && r.code === "SUPER_ADMIN") return true;
          if (r.name && r.name === "超级管理员") return true;
        } else if (typeof r === "string") {
          if (
            r === "SUPER_ADMIN" ||
            r === "ROLE_SUPER_ADMIN" ||
            r === "超级管理员"
          )
            return true;
        }
      }

      // 普通匹配逻辑：优先匹配 role.code（后端常用），其次匹配 role.name
      for (const r of roles) {
        if (!r) continue;
        if (typeof r === "string") {
          if (r === roleName || r === roleName.toUpperCase()) return true;
        } else if (typeof r === "object") {
          if (
            r.code &&
            (r.code === roleName || r.code === roleName.toUpperCase())
          )
            return true;
          if (r.name && r.name === roleName) return true;
        }
      }
      return false;
    },
    async fetchRecords() {
      this.loading = true;
      try {
        console.log("开始获取车辆记录 (vehicleApi)...");
        // 按照 UserManagement.vue 的风格使用封装好的 API
        const params = {};
        const res = await vehicleApi.getVehicles(params);
        // vehicleApi/请求封装会返回后端的业务对象 { code, data, message }
        if (res && (res.code === 200 || res.code === "200")) {
          //  返回 data 为数组
          this.records = Array.isArray(res.data) ? res.data : [];
          this.vehicleList = Array.isArray(res.data) ? res.data : [];
        } else {
          this.$message && this.$message.error
            ? this.$message.error(res.message || "获取车辆列表失败")
            : console.error("获取车辆列表失败", res);
          this.records = [];
          this.vehicleList = [];
        }
      } catch (err) {
        console.error("获取记录失败:", err);
        this.$message && this.$message.error
          ? this.$message.error("获取数据失败: " + (err.message || err))
          : console.error("获取数据失败: " + (err.message || "请检查网络连接"));
        this.records = [];
        this.vehicleList = [];
      } finally {
        this.loading = false;
      }
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
    // 统一格式化用户下拉的展示与取值
    formatUserLabel(u) {
      if (!u) return "";
      return u.nickname || u.name || u.username || "ID:" + u.id;
    },
    formatUserValue(u) {
      if (!u) return "";
      return u.nickname || u.name || u.username || "";
    },
    async loadDepartments() {
      try {
        const res = await userApi.getDepartmentList();
        this.departmentList = Array.isArray(res.data)
          ? res.data
          : res.data || [];
        // 构建名称与ID的快速索引，提升编辑态下拉选择的响应速度
        this._deptNameToId = Object.create(null);
        this._deptIdToName = Object.create(null);
        for (const d of this.departmentList) {
          if (!d) continue;
          this._deptNameToId[d.name] = d.id;
          this._deptIdToName[d.id] = d.name;
        }
      } catch (e) {
        console.error("加载部门列表失败:", e);
        this.departmentList = [];
        this._deptNameToId = Object.create(null);
        this._deptIdToName = Object.create(null);
      }
    },

    // 加载用户列表（用于“用车人”下拉）
    async loadUsers() {
      try {
        const res = await userApi.getUserList({ pageNum: 1, pageSize: 2000 });
        const list = Array.isArray(res && res.data)
          ? res.data
          : (res && res.data && (res.data.records || res.data.list)) || [];
        this.userList = Array.isArray(list) ? list : [];
      } catch (e) {
        console.error("加载用户列表失败:", e);
        this.userList = [];
      }
    },

    getDepartmentName(id) {
      if (!id) return null;
      // 先走 O(1) 映射，回退再遍历
      const name = this._deptIdToName && this._deptIdToName[id];
      if (name) return name;
      const d = (this.departmentList || []).find(
        (x) => Number(x.id) === Number(id)
      );
      return d ? d.name : null;
    },
    onDepartmentChange(val) {
      // 保证类型为数字，且同步名称，避免编辑态延迟显示
      const idNum = val != null ? Number(val) : null;
      this.form.departmentId = idNum;
      this.form.department =
        this.getDepartmentName(idNum) || this.form.department || "";
    },
    async fetchDispatchRecords() {
      try {
        console.log("开始获取派车记录...");
        // 使用 vehicleApi 的后端接口获取派车记录
        const res = await vehicleApi.getDispatchRecords();
        console.log("派车记录响应:", res);

        if (res && (res.code === 200 || res.code === "200") && res.data) {
          this.dispatchRecords = res.data || [];
          console.log("成功获取派车记录:", this.dispatchRecords);
          // 同步生成用于统计的费用记录
          try {
            await this.fetchExpenseRecords();
          } catch (e) {
            console.error("生成统计费用数据失败:", e);
          }
        } else {
          this.dispatchRecords = [];
        }
      } catch (err) {
        console.error("获取派车记录失败:", err);
        this.dispatchRecords = [];
      }
    },

    async fetchExpenseRecords() {
      try {
        // 使用后端真实的派车记录接口作为费用/记录数据源
        // 旧的  接口 vehicleApi.getRecords() 已弃用，改为使用 dispatch-records
        const res = await vehicleApi.getDispatchRecords();
        if (res && (res.code === 200 || res.code === "200") && res.data) {
          // 后端返回的派车记录里包含 fuelCost/tollFee/parkingFee/repairCost 等字段
          // 将派车记录展开为统计需要的 { date, type, amount } 格式
          const dispatch = Array.isArray(res.data) ? res.data : [];
          const expenses = [];
          dispatch.forEach((r) => {
            // 规范化日期：优先 useDate，其次 date、createdAt；确保为可解析的 Date
            const rawDate = r.useDate || r.date || r.createdAt || null;
            const parsed = rawDate ? new Date(rawDate) : null;
            const isoDate =
              parsed && !isNaN(parsed.getTime()) ? parsed.toISOString() : null;

            const addIf = (type, val) => {
              const amount = Number(val || 0);
              // 只有 amount>0 并且有可解析日期时才加入，用于时间维度聚合
              if (!isNaN(amount) && amount > 0 && isoDate) {
                expenses.push({ date: isoDate, type, amount });
              }
            };

            addIf("燃油费", r.fuelCost);
            addIf("过路费", r.tollFee);
            addIf("停车费", r.parkingFee);
            addIf("维修费", r.repairCost);
          });
          this.expenseRecords = expenses;
          console.log("expenseRecords (for chart):", this.expenseRecords);
        } else {
          this.expenseRecords = [];
        }
      } catch (err) {
        console.error("获取费用记录失败:", err);
        this.expenseRecords = [];
      }
      // 生成可选年份（供年度选择）
      const yearsSet = new Set();
      this.expenseRecords.forEach((r) => {
        const d = new Date(r.date || r.createdAt);
        if (!isNaN(d.getTime())) yearsSet.add(d.getFullYear());
      });
      const years = Array.from(yearsSet);
      if (!years.includes(this.selectedYear)) {
        years.push(this.selectedYear);
      }
      this.availableYears = years.sort((a, b) => b - a); // 倒序展示

      // 设置年度范围默认值（startYear -> 最小年，endYear -> 最大年）
      if (this.availableYears.length) {
        this.endYear = this.availableYears[0];
        this.startYear = this.availableYears[this.availableYears.length - 1];
      } else {
        const cy = new Date().getFullYear();
        this.startYear = cy;
        this.endYear = cy;
      }

      // 初始化或刷新图表和统计
      this.$nextTick(() => {
        this.computeStatistics();
        this.computeVehicleStatistics();
        // 基于统计结果生成时间维度表格（只读展示）
        this.buildTimeSummaryRows();
      });
    },

    /**
     * 计算单车统计数据（含百公里油耗）
     */
    computeVehicleStatistics() {
      const vehicleMap = new Map();

      // 遍历所有派车记录，按车辆分组统计
      this.dispatchRecords.forEach((record) => {
        const plateNumber = record.plateNumber;
        if (!vehicleMap.has(plateNumber)) {
          // 查找车辆信息
          const vehicle =
            this.vehicleList.find((v) => v.plateNumber === plateNumber) || {};
          vehicleMap.set(plateNumber, {
            plateNumber: plateNumber,
            brand: vehicle.brand || record.brand || "",
            model: vehicle.model || record.model || "",
            totalMileage: 0,
            tripCount: 0,
            fuelCost: 0,
            tollFee: 0,
            parkingFee: 0,
            insuranceFee: 0,
            annualInspectionFee: 0,
            repairCost: 0,
            totalCost: 0,
            fuelConsumptionPer100km: 0,
          });
        }

        const stat = vehicleMap.get(plateNumber);
        stat.totalMileage += Number(record.tripMileage) || 0;
        stat.tripCount += 1;
        stat.fuelCost += Number(record.fuelCost) || 0;
        stat.tollFee += Number(record.tollFee) || 0;
        stat.parkingFee += Number(record.parkingFee) || 0;
        stat.insuranceFee += Number(record.insuranceFee) || 0;
        stat.annualInspectionFee += Number(record.annualInspectionFee) || 0;
        stat.repairCost += Number(record.repairCost) || 0;
      });

      // 计算每辆车的总费用和百公里油耗
      this.vehicleStatistics = Array.from(vehicleMap.values()).map((stat) => {
        stat.totalCost =
          stat.fuelCost +
          stat.tollFee +
          stat.parkingFee +
          stat.repairCost +
          stat.insuranceFee +
          stat.annualInspectionFee;

        // 计算百公里油耗：(燃油费 / 6.5 / 里程) × 100 -> 单位：升/100km
        if (stat.totalMileage > 0 && stat.fuelCost > 0) {
          stat.fuelConsumptionPer100km =
            (stat.fuelCost / 6.5 / stat.totalMileage) * 100;
        } else {
          stat.fuelConsumptionPer100km = 0;
        }

        return stat;
      });

      // 按车牌号排序
      this.vehicleStatistics.sort((a, b) =>
        a.plateNumber.localeCompare(b.plateNumber)
      );

      // 计算总计
      this.totalStatistics = {
        totalMileage: 0,
        tripCount: 0,
        fuelCost: 0,
        tollFee: 0,
        parkingFee: 0,
        insuranceFee: 0,
        annualInspectionFee: 0,
        repairCost: 0,
        totalCost: 0,
        avgFuelConsumptionPer100km: 0,
      };

      this.vehicleStatistics.forEach((stat) => {
        this.totalStatistics.totalMileage += stat.totalMileage;
        this.totalStatistics.tripCount += stat.tripCount;
        this.totalStatistics.fuelCost += stat.fuelCost;
        this.totalStatistics.tollFee += stat.tollFee;
        this.totalStatistics.parkingFee += stat.parkingFee;
        this.totalStatistics.insuranceFee += stat.insuranceFee;
        this.totalStatistics.annualInspectionFee += stat.annualInspectionFee;
        this.totalStatistics.repairCost += stat.repairCost;
        this.totalStatistics.totalCost += stat.totalCost;
      });

      // 计算平均百公里油耗（升/100km）
      if (
        this.totalStatistics.totalMileage > 0 &&
        this.totalStatistics.fuelCost > 0
      ) {
        this.totalStatistics.avgFuelConsumptionPer100km =
          (this.totalStatistics.fuelCost /
            6.5 /
            this.totalStatistics.totalMileage) *
          100;
      }

      console.log("单车统计数据:", this.vehicleStatistics);
      console.log("汇总统计:", this.totalStatistics);
    },

    // 基于 prepareExportData 的汇总结果构建只读行
    buildTimeSummaryRows() {
      const { summary } = this.prepareExportData();
      this.timeSummaryRows = summary.map((row) => ({
        ...row,
        备注: row["备注"] || "",
      }));
      // 合并后重新计算合计
      this.timeSummaryRows.forEach((r) => this.recalcRow(r));
      // 月度下构建车辆维度明细缓存
      if (this.period === "month") {
        this.computeMonthlyVehicleStats();
      }
    },

    // 行内编辑后，重算合计与百公里油耗
    recalcRow(row) {
      const mileage = Number(row["累计里程(km)"]) || 0;
      const fuel = Number(row["燃油费(元)"]) || 0;
      const toll = Number(row["过路费(元)"]) || 0;
      const park = Number(row["停车费(元)"]) || 0;
      const repair = Number(row["维修费(元)"]) || 0;
      const insurance = Number(row["保险费(元)"]) || 0;
      const annual = Number(row["年审费(元)"]) || 0;
      row["合计(元)"] = (
        fuel +
        toll +
        park +
        repair +
        insurance +
        annual
      ).toFixed(2);
      row["百公里油耗(升/100km)"] =
        mileage > 0 && fuel > 0
          ? ((fuel / 6.5 / mileage) * 100).toFixed(2)
          : "-";
    },

    // 统计表为只读展示，无编辑/保存逻辑

    toFixed0(n) {
      const v = Number(n) || 0;
      return v.toFixed(0);
    },
    toFixed2(n) {
      const v = Number(n) || 0;
      return v.toFixed(2);
    },

    formatDate(dateString) {
      if (!dateString) return "";
      try {
        const date = new Date(dateString);
        return date.toLocaleDateString("zh-CN");
      } catch (e) {
        return dateString;
      }
    },

    openVehicleModal() {
      this.vehicleModalTitle = "新增车辆基本信息";
      this.editId = null;
      this.showVehicleModal = true;
    },

    closeVehicleModal() {
      this.showVehicleModal = false;
      this.vehicleForm = {
        plateNumber: "",
        brand: "",
        model: "",
        displacement: "",
        fuelType: "",
        vehicleStatus: "正常",
        departmentId: null,
        mileage: "",
        purchaseDate: "",
        insuranceStatus: "",
        remark: "",
      };
    },

    async saveVehicle() {
      if (
        !this.vehicleForm.plateNumber ||
        !this.vehicleForm.brand ||
        !this.vehicleForm.model
      ) {
        this.$message && this.$message.error
          ? this.$message.error("请填写车牌号、品牌和车辆型号！")
          : console.warn("请填写车牌号、品牌和车辆型号！");
        return;
      }

      const vehicleData = { ...this.vehicleForm };

      // 如果是编辑模式，添加id
      if (this.editId) {
        vehicleData.id = this.editId;
      }

      try {
        // 确保向后端发送的是 department 名称（数据库列为 `department`），如果前端只选择了 departmentId，则转换为名称
        vehicleData.department =
          this.getDepartmentName(vehicleData.departmentId) ||
          vehicleData.department;

        console.log("保存车辆数据:", vehicleData);
        // 防止重复车牌号导致后端唯一索引冲突（前端轻量校验）
        const existing = this.vehicleList.find(
          (v) => v.plateNumber === vehicleData.plateNumber
        );
        if (existing) {
          // 如果是编辑模式并且找到的是同一条记录，则允许继续
          if (!this.editId || (this.editId && existing.id !== this.editId)) {
            this.$message && this.$message.warning
              ? this.$message.warning(
                  `车牌号 ${vehicleData.plateNumber} 已存在，请检查后再保存（避免数据库唯一键冲突）。`
                )
              : console.warn(
                  `车牌号 ${vehicleData.plateNumber} 已存在，请检查后再保存（避免数据库唯一键冲突）。`
                );
            return;
          }
        }
        const result = await vehicleApi.saveVehicle(vehicleData);
        console.log("保存响应:", result);

        const saved = result && result.data ? result.data : null;

        if (result && (result.code === 200 || result.code === "200")) {
          const vehicleSavedMsg = this.editId
            ? "车辆信息已更新！"
            : "车辆基本信息已保存！";
          if (this.$message && this.$message.success) {
            this.$message.success(vehicleSavedMsg);
          } else {
            console.log(vehicleSavedMsg);
          }

          if (this.editId) {
            // 更新模式：替换现有记录
            const index = this.records.findIndex((r) => r.id === this.editId);
            if (index !== -1) {
              const updatedVehicle =
                saved && saved.id ? saved : { ...vehicleData, id: this.editId };
              this.records.splice(index, 1, updatedVehicle);
              this.vehicleList.splice(index, 1, updatedVehicle);
            }
          } else {
            // 新增模式：添加到列表
            const newVehicle =
              saved && saved.id
                ? saved
                : {
                    ...vehicleData,
                    id: Date.now(),
                    createdAt: new Date().toISOString(),
                  };
            this.vehicleList = [newVehicle, ...this.vehicleList];
            this.records = [newVehicle, ...this.records];
          }

          this.closeVehicleModal();
        } else {
          this.$message && this.$message.error
            ? this.$message.error((result && result.message) || "保存失败！")
            : console.error((result && result.message) || "保存失败！");
        }
      } catch (err) {
        console.error("保存车辆失败:", err);
        this.$message && this.$message.error
          ? this.$message.error("保存失败，请重试！")
          : console.error("保存失败，请重试！");
      }
    },

    openAddModal() {
      this.modalTitle = "新增派车记录";
      this.resetForm();
      this.showModal = true;
    },

    editItem(index) {
      const record = this.records[index];
      this.editId = record.id;
      this.editIndex = index;

      // 打开车辆编辑模态（开放权限：所有用户均可编辑车辆信息）
      this.vehicleModalTitle = "编辑车辆基本信息";
      this.vehicleForm = {
        plateNumber: record.plateNumber || "",
        brand: record.brand || "",
        model: record.model || "",
        displacement: record.displacement || "",
        fuelType: record.fuelType || "",
        vehicleStatus: record.vehicleStatus || "正常",
        departmentId:
          record.departmentId ||
          (record.department
            ? (
                this.departmentList.find((d) => d.name === record.department) ||
                {}
              ).id
            : null),
        mileage: record.mileage || "",
        purchaseDate: record.purchaseDate || "",
        insuranceStatus: record.insuranceStatus || "",
        remark: record.remark || "",
      };
      this.showVehicleModal = true;
    },

    async deleteItem(index) {
      const record = this.records[index];
      this.$confirm
        ? this.$confirm("确定要删除这条记录吗？", "提示", {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          })
            .then(async () => {
              try {
                const res = await vehicleApi.deleteVehicle(record.id);
                console.log("删除响应:", res);
                if (res && (res.code == 200 || res.code == "200")) {
                  if (this.$message && this.$message.success) {
                    this.$message.success("删除成功！");
                  } else {
                    console.log("删除成功！");
                  }
                  this.fetchRecords();
                } else {
                  const errMsg =
                    res && (res.message || res.msg)
                      ? res.message || res.msg
                      : "删除失败！";
                  if (this.$message && this.$message.error) {
                    this.$message.error(errMsg);
                  } else {
                    console.error("删除失败！", res);
                  }
                }
              } catch (err) {
                console.error("删除失败:", err);
                this.$message && this.$message.error
                  ? this.$message.error("删除失败，请重试！")
                  : console.error("删除失败，请重试！");
              }
            })
            .catch(() => {
              // 用户取消，不做处理
            })
        : (() => {
            if (confirm("确定要删除这条记录吗？")) {
              // 回退到原生实现（极端兼容）
              vehicleApi
                .deleteVehicle(record.id)
                .then((res) => {
                  if (res && (res.code == 200 || res.code == "200")) {
                    if (this.$message && this.$message.success) {
                      this.$message.success("删除成功！");
                    } else {
                      console.log("删除成功！");
                    }
                    this.fetchRecords();
                  } else {
                    const errMsg =
                      res && (res.message || res.msg)
                        ? res.message || res.msg
                        : "删除失败！";
                    if (this.$message && this.$message.error) {
                      this.$message.error(errMsg);
                    } else {
                      console.error(errMsg);
                    }
                  }
                })
                .catch((err) => {
                  console.error(err);
                  if (this.$message && this.$message.error) {
                    this.$message.error("删除失败，请重试！");
                  } else {
                    console.error("删除失败，请重试！");
                  }
                });
            }
          })();
    },

    closeModal() {
      this.showModal = false;
      this.resetForm();
      this.clearAttachments();
    },

    async submitForm() {
      // 验证必填字段
      if (!this.form.plateNumber) {
        this.$message && this.$message.error
          ? this.$message.error("请选择车牌号！")
          : console.warn("请选择车牌号！");
        return;
      }
      if (!this.form.useDate) {
        this.$message && this.$message.error
          ? this.$message.error("请选择用车日期！")
          : console.warn("请选择用车日期！");
        return;
      }

      const recordData = {
        vehicleId: this.form.vehicleId,
        plateNumber: this.form.plateNumber,
        brand: this.form.brand,
        model: this.form.model,
        // 兼容后端：优先传 departmentId（若后端需要名称可用 department 字段回退）
        departmentId: this.form.departmentId || null,
        department:
          (this.form.departmentId &&
            this.getDepartmentName(this.form.departmentId)) ||
          this.form.department ||
          "",
        useDate: this.form.useDate,
        departureTime: this.form.departureTime || "",
        returnTime: this.form.returnTime || "",
        userName: this.form.userName || "",
        startMileage: this.form.startMileage || 0,
        endMileage: this.form.endMileage || 0,
        tripMileage: this.form.tripMileage || 0,
        fuelCost: this.form.fuelCost || 0,
        tollFee: this.form.tollFee || 0,
        parkingFee: this.form.parkingFee || 0,
        insuranceFee: this.form.insuranceFee || 0,
        annualInspectionFee: this.form.annualInspectionFee || 0,
        repairCost: this.form.repairCost || 0,
        remark: this.form.remark || "",
        // attachments will be populated after uploading new files to backend/MinIO
        attachments: [],
      };

      // dispatchNumber is generated by backend on create. Preserve when editing.
      if (this.editId) {
        recordData.dispatchNumber = this.form.dispatchNumber;
      }

      // 如果是编辑，添加id
      if (this.editId) {
        recordData.id = this.editId;
      }

      try {
        console.log("提交派车记录数据:", recordData);

        // --- 1) 先上传附件（仅对新选择的文件上传），将返回的元数据放入 recordData.attachments ---
        const uploadTasks = this.attachments.map(async (f) => {
          // 如果是新上传的文件（有原生 File 对象），先上传到后端，由后端保存到 MinIO
          if (f.file) {
            const fd = new FormData();
            fd.append("file", f.file);
            // 可按需传递上传者 ID：fd.append('uploadUserId', this.login_user?.id || '');
            const upl = await vehicleApi.uploadDispatchAttachment(fd);
            if (
              upl &&
              (upl.code === 200 || upl.code === "200") &&
              upl.data &&
              upl.data.attachment
            ) {
              return upl.data.attachment;
            }
            throw new Error(
              "附件上传失败: " + (upl && (upl.message || upl.msg))
            );
          }

          // 已存在的附件（编辑时从后端返回的 metadata）
          if (f.filePath) {
            return {
              name: f.name,
              filePath: f.filePath,
              fileUrl: f.fileUrl || null,
              fileSize: f.size,
              fileType: f.type,
            };
          }

          // 回退：保留 dataUrl（未上传到 MinIO 的临时数据）
          return {
            name: f.name,
            dataUrl: f.dataUrl,
            fileSize: f.size,
            fileType: f.type,
          };
        });

        // 并行上传并收集结果
        const uploaded = await Promise.all(uploadTasks);
        recordData.attachments = uploaded;

        // 根据是否有editId判断是新增还是编辑
        let res;
        if (this.editId) {
          // 编辑：使用 vehicleApi 更新后端派车记录
          res = await vehicleApi.updateDispatchRecord(this.editId, recordData);
        } else {
          // 新增：使用 vehicleApi 新增派车记录
          res = await vehicleApi.saveDispatchRecord(recordData);
        }

        console.log("提交响应:", res);
        if (res && (res.code === 200 || res.code === "200")) {
          const dispatchSavedMsg = this.editId
            ? "派车记录已更新！"
            : "派车记录已保存！";
          if (this.$message && this.$message.success) {
            this.$message.success(dispatchSavedMsg);
          } else {
            console.log(dispatchSavedMsg);
          }
          // 刷新派车记录列表
          await this.fetchDispatchRecords();
          // 更新车辆累计里程
          await this.updateVehicleMileage(this.form.plateNumber);
          // 刷新车辆列表以显示更新后的里程
          await this.fetchRecords();
          this.closeModal();
        } else {
          this.$message && this.$message.error
            ? this.$message.error(res.message || res.msg || "保存失败！")
            : console.error(res.message || res.msg || "保存失败！");
        }
      } catch (err) {
        console.error("保存派车记录失败:", err);
        this.$message && this.$message.error
          ? this.$message.error("保存失败，请重试！")
          : console.error("保存失败，请重试！");
      }
    },

    resetForm() {
      this.form = {
        plateNumber: "",
        vehicleId: null,
        brand: "",
        model: "",
        departmentId: null,
        dispatchNumber: "",
        useDate: "",
        departureTime: "",
        returnTime: "",
        userName: "",
        startMileage: "",
        endMileage: "",
        tripMileage: 0,
        fuelCost: 0,
        tollFee: 0,
        parkingFee: 0,
        repairCost: 0,
        remark: "",
      };
      this.editId = null;
      this.editIndex = null;
    },

    /**
     * 计算本次行驶里程
     */
    calculateMileage() {
      const start = Number(this.form.startMileage) || 0;
      const end = Number(this.form.endMileage) || 0;
      if (start > 0 && end > start) {
        this.form.tripMileage = end - start;
      } else {
        this.form.tripMileage = 0;
      }
    },

    /**
     * 更新车辆累计里程（按派车单逐笔累计）
     */
    async updateVehicleMileage(plateNumber) {
      try {
        // 获取该车辆的所有派车记录
        const vehicleRecords = this.dispatchRecords.filter(
          (record) => record.plateNumber === plateNumber
        );

        // 累计所有派车单的里程
        const totalMileage = vehicleRecords.reduce((sum, record) => {
          return sum + (Number(record.tripMileage) || 0);
        }, 0);

        // 更新车辆列表中的里程数据
        const vehicleIndex = this.vehicleList.findIndex(
          (v) => v.plateNumber === plateNumber
        );

        if (vehicleIndex !== -1) {
          const vehicle = this.vehicleList[vehicleIndex];
          const updateData = {
            ...vehicle,
            mileage: totalMileage,
          };

          // 调用车辆更新接口
          // 保证更新时也带上 department 名称，避免后端收到只有 departmentId 的情况
          updateData.department =
            this.getDepartmentName(updateData.departmentId) ||
            updateData.department;

          const result = await vehicleApi.saveVehicle(updateData);
          const saved = result && result.data ? result.data : null;

          if (result && (result.code === 200 || result.code === "200")) {
            // 更新本地数据
            // 使用持久化后的对象更新本地条目（若返回对象存在）
            if (saved && saved.id) {
              this.vehicleList[vehicleIndex] = {
                ...this.vehicleList[vehicleIndex],
                ...saved,
              };
              this.records[vehicleIndex] = {
                ...this.records[vehicleIndex],
                ...saved,
              };
            } else {
              this.vehicleList[vehicleIndex].mileage = totalMileage;
              this.records[vehicleIndex].mileage = totalMileage;
            }

            console.log(
              `车辆 ${plateNumber} 累计里程已更新为: ${totalMileage} 公里`
            );
          }
        }
      } catch (err) {
        console.error("更新车辆里程失败:", err);
      }
    },

    /**
     * 编辑派车记录
     */
    editDispatchRecord(index) {
      const record = this.dispatchRecords[index];
      this.modalTitle = "编辑派车记录";
      this.editId = record.id;
      this.editIndex = index;
      // 逐字段赋值，确保 Vue2 响应式稳定
      const r = record || {};
      this.form.dispatchNumber =
        r.dispatchNumber || this.form.dispatchNumber || "";
      this.form.plateNumber = r.plateNumber || this.form.plateNumber || "";
      this.form.vehicleId = r.vehicleId || this.form.vehicleId || null;
      this.form.brand = r.brand || this.form.brand || "";
      this.form.model = r.model || this.form.model || "";
      this.form.useDate = r.useDate || this.form.useDate || "";
      this.form.departureTime =
        r.departureTime || this.form.departureTime || "";
      this.form.returnTime = r.returnTime || this.form.returnTime || "";
      this.form.userName =
        r.userName || r.requesterName || this.form.userName || "";
      this.form.startMileage = r.startMileage || this.form.startMileage || "";
      this.form.endMileage = r.endMileage || this.form.endMileage || "";
      this.form.tripMileage = r.tripMileage || this.form.tripMileage || 0;
      this.form.fuelCost = r.fuelCost || this.form.fuelCost || 0;
      this.form.tollFee = r.tollFee || this.form.tollFee || 0;
      this.form.parkingFee = r.parkingFee || this.form.parkingFee || 0;
      this.form.repairCost = r.repairCost || this.form.repairCost || 0;
      this.form.remark = r.remark || this.form.remark || "";
      // 兼容后端可能返回 department (name) 或 departmentId
      const deptId =
        r.departmentId ||
        (r.department
          ? (this._deptNameToId && this._deptNameToId[r.department]) || null
          : null);
      this.form.departmentId =
        deptId != null ? Number(deptId) : this.form.departmentId || null;
      this.form.department =
        this.getDepartmentName(this.form.departmentId) ||
        r.department ||
        this.form.department ||
        "";
      // 加载附件
      if (record.attachments && Array.isArray(record.attachments)) {
        this.attachments = record.attachments.map((att) => ({
          name: att.name,
          size: att.fileSize || att.size,
          type: att.fileType || att.type,
          // 优先使用后端返回的 fileUrl，否则使用 dataUrl
          preview: att.fileUrl || att.dataUrl || null,
          dataUrl: att.dataUrl || null,
          filePath: att.filePath || null,
          fileUrl: att.fileUrl || null,
          uploadId: att.uploadId || null,
        }));
      }
      this.showModal = true;
    },

    /**
     * 查看派车单详情
     */
    viewDispatchDetail(record) {
      // 使用内置弹窗展示更美观的详情视图
      this.detailRecord = record || {};
      // normalize attachments array
      if (
        !this.detailRecord.attachments ||
        !Array.isArray(this.detailRecord.attachments)
      )
        this.detailRecord.attachments = [];
      this.showDetailModal = true;
    },

    closeDetailModal() {
      this.showDetailModal = false;
      this.detailRecord = null;
    },

    /**
     * 预览附件（打开新窗口）。支持 fileUrl 或 dataUrl(preview)
     */
    previewAttachment(att) {
      try {
        if (!att) return;
        if (att.fileUrl) {
          window.open(att.fileUrl, "_blank");
          return;
        }
        if (att.preview) {
          // data URL or base64 preview
          window.open(att.preview, "_blank");
          return;
        }
        if (att.filePath) {
          // fallback: try to open file path
          window.open(att.filePath, "_blank");
          return;
        }
        if (this.$message) this.$message.info("无可预览的附件");
        else console.info("无可预览的附件");
      } catch (e) {
        console.error("previewAttachment error:", e);
        if (this.$message) this.$message.error("预览附件失败");
        else console.error("预览附件失败");
      }
    },

    /**
     * 下载附件。支持远程 URL 或 dataUrl
     */
    downloadAttachment(att) {
      try {
        if (!att) return;
        const filename = att.name || "attachment";
        if (att.fileUrl) {
          // Try to trigger download via anchor (may be blocked cross-origin)
          const a = document.createElement("a");
          a.href = att.fileUrl;
          a.target = "_blank";
          a.download = filename;
          document.body.appendChild(a);
          a.click();
          a.remove();
          return;
        }
        if (att.preview) {
          // data URL -> blob
          const blob = this.dataUrlToBlob(att.preview);
          const url = URL.createObjectURL(blob);
          const a = document.createElement("a");
          a.href = url;
          a.download = filename;
          document.body.appendChild(a);
          a.click();
          a.remove();
          URL.revokeObjectURL(url);
          return;
        }
        if (att.filePath) {
          const a = document.createElement("a");
          a.href = att.filePath;
          a.target = "_blank";
          a.download = filename;
          document.body.appendChild(a);
          a.click();
          a.remove();
          return;
        }
        if (this.$message) this.$message.info("无可下载的附件");
        else console.info("无可下载的附件");
      } catch (e) {
        console.error("downloadAttachment error:", e);
        if (this.$message) this.$message.error("下载附件失败");
        else console.error("下载附件失败");
      }
    },

    dataUrlToBlob(dataurl) {
      try {
        const arr = dataurl.split(",");
        const mimeMatch = arr[0].match(/:(.*?);/);
        const mime = mimeMatch ? mimeMatch[1] : "application/octet-stream";
        const bstr = atob(arr[1]);
        let n = bstr.length;
        const u8arr = new Uint8Array(n);
        while (n--) u8arr[n] = bstr.charCodeAt(n);
        return new Blob([u8arr], { type: mime });
      } catch (e) {
        console.error("dataUrlToBlob error:", e);
        return new Blob();
      }
    },

    /**
     * 删除派车记录
     */
    async deleteDispatchRecord(index) {
      const record = this.dispatchRecords[index];
      this.$confirm
        ? this.$confirm(
            `确定要删除派车单"${record.dispatchNumber}"吗？`,
            "提示",
            {
              confirmButtonText: "确定",
              cancelButtonText: "取消",
              type: "warning",
            }
          )
            .then(async () => {
              try {
                const res = await vehicleApi.deleteDispatchRecord(record.id);
                if (res && (res.code === 200 || res.code === "200")) {
                  this.$message && this.$message.success
                    ? this.$message.success("派车记录已删除！")
                    : console.log("派车记录已删除！");
                  // 刷新派车记录列表
                  await this.fetchDispatchRecords();
                  // 重新计算车辆累计里程
                  await this.updateVehicleMileage(record.plateNumber);
                  // 刷新车辆列表以显示更新后的里程
                  await this.fetchRecords();
                } else {
                  this.$message && this.$message.error
                    ? this.$message.error(res.msg || "删除失败！")
                    : console.error(res.msg || "删除失败！");
                }
              } catch (err) {
                console.error("删除派车记录失败:", err);
                this.$message && this.$message.error
                  ? this.$message.error("删除失败，请重试！")
                  : console.error("删除失败，请重试！");
              }
            })
            .catch(() => {})
        : (async () => {
            if (confirm(`确定要删除派车单"${record.dispatchNumber}"吗？`)) {
              try {
                const res = await vehicleApi.deleteDispatchRecord(record.id);
                if (res && (res.code === 200 || res.code === "200")) {
                  this.$message && this.$message.success
                    ? this.$message.success("派车记录已删除！")
                    : console.log("派车记录已删除！");
                  await this.fetchDispatchRecords();
                  await this.updateVehicleMileage(record.plateNumber);
                  await this.fetchRecords();
                } else {
                  this.$message && this.$message.error
                    ? this.$message.error(res.msg || "删除失败！")
                    : console.error(res.msg || "删除失败！");
                }
              } catch (err) {
                console.error(err);
                this.$message && this.$message.error
                  ? this.$message.error("删除失败，请重试！")
                  : console.error("删除失败，请重试！");
              }
            }
          })();
    },

    /**
     * 处理文件选择
     */
    handleFileSelect(event) {
      const files = Array.from(event.target.files);

      files.forEach((file) => {
        // 检查文件大小
        if (file.size > this.maxFileSize) {
          this.$message && this.$message.error
            ? this.$message.error(`文件 ${file.name} 超过最大限制 5MB`)
            : console.warn(`文件 ${file.name} 超过最大限制 5MB`);
          return;
        }

        // 检查是否为图片
        if (!file.type.startsWith("image/")) {
          this.$message && this.$message.error
            ? this.$message.error(`文件 ${file.name} 不是图片格式`)
            : console.warn(`文件 ${file.name} 不是图片格式`);
          return;
        }

        // 读取文件并生成预览
        const reader = new FileReader();
        reader.onload = (e) => {
          this.attachments.push({
            name: file.name,
            size: file.size,
            type: file.type,
            file: file,
            preview: e.target.result,
            dataUrl: e.target.result,
          });
        };
        reader.readAsDataURL(file);
      });

      // 清空input，允许重复选择同一文件
      event.target.value = "";
    },

    /**
     * 移除附件
     */
    removeAttachment(index) {
      const att = this.attachments[index];
      // 如果附件已经上传（包含 filePath），同时请求后端删除 MinIO 对象
      if (att && att.filePath) {
        vehicleApi
          .deleteDispatchAttachment(att.filePath)
          .then(() => {
            // no-op
          })
          .catch((e) => {
            console.warn("删除 MinIO 附件失败（忽略）:", e);
          });
      }
      this.attachments.splice(index, 1);
    },

    /**
     * 清空所有附件
     */
    clearAttachments() {
      this.attachments = [];
      if (this.$refs.fileInput) {
        this.$refs.fileInput.value = "";
      }
    },

    /**
     * 格式化文件大小
     */
    formatFileSize(bytes) {
      if (bytes === 0) return "0 Bytes";
      const k = 1024;
      const sizes = ["Bytes", "KB", "MB", "GB"];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + " " + sizes[i];
    },

    // 当在新增记录表单中选择车牌时，自动填充品牌/型号等字段
    onPlateSelected() {
      const selected = this.vehicleList.find(
        (v) => v.plateNumber === this.form.plateNumber
      );
      if (selected) {
        // 填充品牌/型号/部门，并记录车辆 id 以便后端插入时使用
        this.form.brand = selected.brand || this.form.brand;
        this.form.model = selected.model || this.form.model;
        this.form.vehicleId = selected.id || this.form.vehicleId;
      }
    },

    // 计算统计数据（按 period: month/quarter/year）
    computeStatistics() {
      const period = this.period;
      const records = Array.isArray(this.expenseRecords)
        ? this.expenseRecords
        : [];

      // types 确保包含所有类型
      const typesSet = new Set(this.chartTypes);
      records.forEach((r) => typesSet.add(r.type || "其他"));
      const types = Array.from(typesSet);

      let categories = [];
      let buckets = [];

      const currentYear = this.selectedYear || new Date().getFullYear();

      if (period === "month") {
        categories = Array.from({ length: 12 }, (_, i) => `${i + 1}月`);
        buckets = Array.from({ length: 12 }, () => ({}));
        records.forEach((r) => {
          const d = new Date(r.date || r.createdAt);
          if (d.getFullYear() !== currentYear) return; // 仅统计所选年
          const m = d.getMonth();
          const t = r.type || "其他";
          const amt = Number(r.amount || 0);
          buckets[m][t] = (buckets[m][t] || 0) + amt;
        });
      } else if (period === "quarter") {
        categories = ["Q1", "Q2", "Q3", "Q4"];
        buckets = Array.from({ length: 4 }, () => ({}));
        records.forEach((r) => {
          const d = new Date(r.date || r.createdAt);
          if (d.getFullYear() !== currentYear) return; // 仅统计所选年
          const q = Math.floor(d.getMonth() / 3);
          const t = r.type || "其他";
          const amt = Number(r.amount || 0);
          buckets[q][t] = (buckets[q][t] || 0) + amt;
        });
      } else if (period === "year") {
        // 使用 startYear/endYear 构建年份区间并统计
        let s = Number(this.startYear) || new Date().getFullYear();
        let e = Number(this.endYear) || new Date().getFullYear();
        if (s > e) {
          // 交换以保证 s <= e
          const tmp = s;
          s = e;
          e = tmp;
        }
        // 限制范围长度，避免渲染过多项（例如最多 50 年）
        const MAX_RANGE = 50;
        if (e - s + 1 > MAX_RANGE) {
          e = s + MAX_RANGE - 1;
        }

        const years = [];
        for (let y = s; y <= e; y++) years.push(y);
        categories = years.map((y) => `${y}年`);
        buckets = years.map(() => ({}));
        records.forEach((r) => {
          const d = new Date(r.date || r.createdAt);
          const y = d.getFullYear();
          if (y < s || y > e) return;
          const idx = years.indexOf(y);
          if (idx === -1) return;
          const t = r.type || "其他";
          const amt = Number(r.amount || 0);
          buckets[idx][t] = (buckets[idx][t] || 0) + amt;
        });
      }

      // 构造 series
      const series = types.map((t) => ({
        name: t,
        type: "bar",
        stack: "总量",
        emphasis: { focus: "series" },
        data: buckets.map((b) => Number((b[t] || 0).toFixed(2))),
      }));
      this.chartCategories = categories;
      this.chartSeries = series;
      console.log("chartCategories:", this.chartCategories);
      console.log("chartSeries (sample):", this.chartSeries.slice(0, 5));
      try {
        console.log("chartSeries (json):", JSON.stringify(this.chartSeries));
      } catch (e) {
        console.warn("无法将 chartSeries 转为 JSON:", e);
      }
    },

    // 已移除柱状图渲染；时间维度展示为可编辑表格

    /**
     * 统计维度切换
     */
    onStatisticsDimensionChange() {
      console.log("切换统计维度:", this.statisticsDimension);
      if (this.statisticsDimension === "time") {
        // 切换到时间维度：确保 expenseRecords 已准备好，必要时重新生成，然后刷新图表
        this.$nextTick(async () => {
          try {
            // 重新生成 expenseRecords（从 dispatchRecords 提取费用），保证时间维度有数据源
            await this.fetchExpenseRecords();
          } catch (e) {
            console.warn("切换到时间维度时重新生成费用记录失败", e);
          }
          this.computeStatistics();
          this.buildTimeSummaryRows();
          if (this.period === "month") {
            this.computeMonthlyVehicleStats();
          }
        });
      } else if (this.statisticsDimension === "vehicle") {
        // 切换到单车维度，计算单车统计
        this.computeVehicleStatistics();
      }
    },

    /**
     * 计算当月每辆车的统计（与 computeVehicleStatistics 同结构，限定月份）
     */
    computeMonthlyVehicleStats() {
      const year = Number(this.selectedYear) || new Date().getFullYear();
      const buckets = Array.from({ length: 12 }, () => new Map());
      const list = Array.isArray(this.dispatchRecords)
        ? this.dispatchRecords
        : [];
      list.forEach((r) => {
        const d = new Date(r.useDate || r.createdAt);
        if (isNaN(d.getTime())) return;
        const y = d.getFullYear();
        if (y !== year) return;
        const m = d.getMonth() + 1;
        const map = buckets[m - 1];
        const plate = r.plateNumber;
        if (!map.has(plate)) {
          const v = this.vehicleList.find((x) => x.plateNumber === plate) || {};
          map.set(plate, {
            plateNumber: plate,
            brand: v.brand || r.brand || "",
            model: v.model || r.model || "",
            totalMileage: 0,
            tripCount: 0,
            fuelCost: 0,
            tollFee: 0,
            parkingFee: 0,
            insuranceFee: 0,
            annualInspectionFee: 0,
            repairCost: 0,
            totalCost: 0,
            fuelConsumptionPer100km: 0,
          });
        }
        const stat = map.get(plate);
        stat.totalMileage += Number(r.tripMileage) || 0;
        stat.tripCount += 1;
        stat.fuelCost += Number(r.fuelCost) || 0;
        stat.tollFee += Number(r.tollFee) || 0;
        stat.parkingFee += Number(r.parkingFee) || 0;
        stat.insuranceFee += Number(r.insuranceFee) || 0;
        stat.annualInspectionFee += Number(r.annualInspectionFee) || 0;
        stat.repairCost += Number(r.repairCost) || 0;
      });

      const result = {};
      for (let m = 1; m <= 12; m++) {
        const arr = Array.from(buckets[m - 1].values()).map((s) => {
          s.totalCost =
            s.fuelCost +
            s.tollFee +
            s.parkingFee +
            s.repairCost +
            s.insuranceFee +
            s.annualInspectionFee;
          if (s.totalMileage > 0 && s.fuelCost > 0) {
            s.fuelConsumptionPer100km =
              (s.fuelCost / 6.5 / s.totalMileage) * 100;
          } else {
            s.fuelConsumptionPer100km = 0;
          }
          return s;
        });
        arr.sort((a, b) => a.plateNumber.localeCompare(b.plateNumber));
        result[m] = arr;
      }
      this.monthlyVehicleStats = result;
    },

    // 展开控制与工具方法
    toggleMonthExpand(month) {
      if (!month) return;
      if (this.expandedMonths.has(month)) this.expandedMonths.delete(month);
      else this.expandedMonths.add(month);
      if (this.period === "month") this.computeMonthlyVehicleStats();
    },
    isMonthExpanded(month) {
      return this.expandedMonths.has(month);
    },
    getMonthlyVehicleStats(month) {
      return (
        (this.monthlyVehicleStats && this.monthlyVehicleStats[month]) || []
      );
    },
    parseMonthFromLabel(label) {
      const m = String(label || "").match(/(\d+)月/);
      return m ? Number(m[1]) : null;
    },
    formatMonthLabel(month) {
      const y = Number(this.selectedYear) || new Date().getFullYear();
      return `${y}年${month}月`;
    },

    // 打开某月某车的派车记录明细，提供编辑/删除入口
    openVehicleMonthModal(month, plateNumber) {
      const y = Number(this.selectedYear) || new Date().getFullYear();
      const filtered = (this.dispatchRecords || []).filter((r) => {
        const d = new Date(r.useDate || r.createdAt);
        if (isNaN(d.getTime())) return false;
        return (
          d.getFullYear() === y &&
          d.getMonth() + 1 === Number(month) &&
          r.plateNumber === plateNumber
        );
      });
      this.vehicleMonthRecords = filtered;
      this.vehicleMonthInfo = { month, label: this.formatMonthLabel(month) };
      this.vehicleMonthModalTitle = `${this.formatMonthLabel(
        month
      )} - ${plateNumber} 派车明细`;
      this.showVehicleMonthModal = true;
    },
    closeVehicleMonthModal() {
      this.showVehicleMonthModal = false;
      this.vehicleMonthRecords = [];
      this.vehicleMonthInfo = { month: null, label: "" };
    },
    editDispatchRecordById(id) {
      const idx = (this.dispatchRecords || []).findIndex((r) => r.id === id);
      if (idx !== -1) this.editDispatchRecord(idx);
    },
    deleteDispatchRecordById(id) {
      const idx = (this.dispatchRecords || []).findIndex((r) => r.id === id);
      if (idx !== -1) this.deleteDispatchRecord(idx);
    },

    /**
     * 加入收藏夹功能
     * 说明：需要用户手动确认，不同浏览器API不同
     */
    addFavorite() {
      const siteUrl = window.location.href; // 当前页面URL
      const siteName = document.title; // 网站名称（使用页面标题）

      try {
        // IE浏览器
        window.external.AddFavorite(siteUrl, siteName);
      } catch (e) {
        try {
          // Chrome/Firefox/Safari等现代浏览器
          window.sidebar.addPanel(siteName, siteUrl, "");
        } catch (err) {
          // 部分浏览器不支持API，提示手动收藏
          const msg =
            "请手动将本网站加入收藏夹：\n1. 按 Ctrl+D（Windows）或 Command+D（Mac）\n2. 点击确认收藏";
          if (this.$alert) {
            this.$alert(msg, "提示", { confirmButtonText: "确定" });
          } else if (this.$message) {
            this.$message.info("请按 Ctrl+D 或 使用浏览器收藏功能");
            console.info(msg);
          } else {
            alert(msg);
          }
        }
      }
    },

    /**
     * 导出统计数据到Excel
     */
    exportToExcel() {
      try {
        // 创建工作簿
        const workbook = XLSX.utils.book_new();

        if (this.statisticsDimension === "vehicle") {
          // 单车维度导出
          this.exportVehicleStatistics(workbook);
        } else {
          // 时间维度导出
          this.exportTimeStatistics(workbook);
        }

        // 生成文件名
        const fileName = this.generateFileName();

        // 导出文件
        const wbout = XLSX.write(workbook, { bookType: "xlsx", type: "array" });
        saveAs(
          new Blob([wbout], { type: "application/octet-stream" }),
          fileName
        );

        this.$message && this.$message.success
          ? this.$message.success("数据导出成功！")
          : console.log("数据导出成功！");
      } catch (error) {
        console.error("导出Excel失败:", error);
        this.$message && this.$message.error
          ? this.$message.error("导出失败，请重试！")
          : console.error("导出失败，请重试！");
      }
    },

    /**
     * 导出单车维度统计数据
     */
    exportVehicleStatistics(workbook) {
      // 1. 单车统计汇总表
      const vehicleStats = this.vehicleStatistics.map((stat) => ({
        车牌号: stat.plateNumber,
        品牌: stat.brand,
        型号: stat.model,
        "累计里程(km)": stat.totalMileage.toFixed(0),
        "燃油费(元)": stat.fuelCost.toFixed(2),
        "过路费(元)": stat.tollFee.toFixed(2),
        "停车费(元)": stat.parkingFee.toFixed(2),
        "保险费(元)": stat.insuranceFee.toFixed(2),
        "年审费(元)": stat.annualInspectionFee.toFixed(2),
        "维修费(元)": stat.repairCost.toFixed(2),
        "总费用(元)": stat.totalCost.toFixed(2),
        "百公里油耗(升/100km)":
          stat.fuelConsumptionPer100km > 0
            ? stat.fuelConsumptionPer100km.toFixed(2)
            : "-",
      }));

      // 添加合计行
      vehicleStats.push({
        车牌号: "合计",
        品牌: "",
        型号: "",
        "累计里程(km)": this.totalStatistics.totalMileage.toFixed(0),
        "燃油费(元)": this.totalStatistics.fuelCost.toFixed(2),
        "过路费(元)": this.totalStatistics.tollFee.toFixed(2),
        "停车费(元)": this.totalStatistics.parkingFee.toFixed(2),
        "保险费(元)": this.totalStatistics.insuranceFee.toFixed(2),
        "年审费(元)": this.totalStatistics.annualInspectionFee.toFixed(2),
        "维修费(元)": this.totalStatistics.repairCost.toFixed(2),
        "总费用(元)": this.totalStatistics.totalCost.toFixed(2),
        "百公里油耗(升/100km)":
          this.totalStatistics.avgFuelConsumptionPer100km > 0
            ? this.totalStatistics.avgFuelConsumptionPer100km.toFixed(2)
            : "-",
      });

      const vehicleSheet = XLSX.utils.json_to_sheet(vehicleStats);
      XLSX.utils.book_append_sheet(workbook, vehicleSheet, "单车统计汇总");

      // 2. 派车记录明细表
      const dispatchDetails = this.dispatchRecords.map((record) => ({
        派车单号: record.dispatchNumber,
        车牌号: record.plateNumber,
        品牌型号: `${record.brand} ${record.model}`,
        用车日期: record.useDate,
        出车时间: record.departureTime || "-",
        返回时间: record.returnTime || "-",
        用车部门: record.department || "-",
        用车人: record.userName || "-",
        "出车里程(km)": record.startMileage || 0,
        "回车里程(km)": record.endMileage || 0,
        "行驶里程(km)": record.tripMileage || 0,
        "燃油费(元)": (record.fuelCost || 0).toFixed(2),
        "过路费(元)": (record.tollFee || 0).toFixed(2),
        "停车费(元)": (record.parkingFee || 0).toFixed(2),
        "维修费(元)": (record.repairCost || 0).toFixed(2),
        "保险费(元)": (record.insuranceFee || 0).toFixed(2),
        "年审费(元)": (record.annualInspectionFee || 0).toFixed(2),
        "费用合计(元)": (
          Number(record.fuelCost || 0) +
          Number(record.tollFee || 0) +
          Number(record.parkingFee || 0) +
          Number(record.repairCost || 0) +
          Number(record.insuranceFee || 0) +
          Number(record.annualInspectionFee || 0)
        ).toFixed(2),
        备注: record.remark || "",
      }));

      const detailSheet = XLSX.utils.json_to_sheet(dispatchDetails);
      XLSX.utils.book_append_sheet(workbook, detailSheet, "派车记录明细");

      // 3. 车辆基础信息表
      const vehicleInfo = this.vehicleList.map((v) => ({
        车牌号: v.plateNumber,
        品牌: v.brand,
        型号: v.model,
        排量: v.displacement || "-",
        燃油类型: v.fuelType || "-",
        车辆状态: v.vehicleStatus || "正常",
        所属部门: v.department || "-",
        "当前总里程(km)": v.mileage || 0,
        购置日期: v.purchaseDate || "-",
        保险状态: v.insuranceStatus || "-",
        备注: v.remark || "",
      }));

      const vehicleInfoSheet = XLSX.utils.json_to_sheet(vehicleInfo);
      XLSX.utils.book_append_sheet(workbook, vehicleInfoSheet, "车辆基础信息");
    },

    /**
     * 导出时间维度统计数据
     */
    exportTimeStatistics(workbook) {
      // 准备导出数据
      const exportData = this.prepareExportData();

      // 创建统计汇总表
      const summarySheet = XLSX.utils.json_to_sheet(exportData.summary);
      XLSX.utils.book_append_sheet(workbook, summarySheet, "费用统计汇总");

      // 创建派车记录明细表
      const dispatchDetails = this.dispatchRecords.map((record) => ({
        派车单号: record.dispatchNumber,
        车牌号: record.plateNumber,
        用车日期: record.useDate,
        用车部门: record.department || "-",
        用车人: record.userName || "-",
        "行驶里程(km)": record.tripMileage || 0,
        "燃油费(元)": (record.fuelCost || 0).toFixed(2),
        "过路费(元)": (record.tollFee || 0).toFixed(2),
        "停车费(元)": (record.parkingFee || 0).toFixed(2),
        "维修费(元)": (record.repairCost || 0).toFixed(2),
        "保险费(元)": (record.insuranceFee || 0).toFixed(2),
        "年审费(元)": (record.annualInspectionFee || 0).toFixed(2),
        "费用合计(元)": (
          Number(record.fuelCost || 0) +
          Number(record.tollFee || 0) +
          Number(record.parkingFee || 0) +
          Number(record.repairCost || 0) +
          Number(record.insuranceFee || 0) +
          Number(record.annualInspectionFee || 0)
        ).toFixed(2),
        备注: record.remark || "",
      }));

      const detailSheet = XLSX.utils.json_to_sheet(dispatchDetails);
      XLSX.utils.book_append_sheet(workbook, detailSheet, "派车记录明细");

      // 当周期为“月”且存在已展开月份时，追加每个展开月份的车辆月度明细工作表
      if (
        this.period === "month" &&
        this.expandedMonths &&
        this.expandedMonths.size > 0
      ) {
        Array.from(this.expandedMonths).forEach((m) => {
          this.appendMonthlyVehicleStatsSheet(workbook, m);
        });
      }
    },

    /**
     * 追加指定月份的车辆月度明细为工作表
     */
    appendMonthlyVehicleStatsSheet(workbook, month) {
      const stats = this.getMonthlyVehicleStats(month);
      // 映射为导出字段
      const rows = stats.map((vs) => ({
        车牌号: vs.plateNumber,
        品牌型号: `${vs.brand || ""} ${vs.model || ""}`.trim(),
        "累计里程(km)": Number(vs.totalMileage || 0).toFixed(0),
        "燃油费(元)": Number(vs.fuelCost || 0).toFixed(2),
        "过路费(元)": Number(vs.tollFee || 0).toFixed(2),
        "停车费(元)": Number(vs.parkingFee || 0).toFixed(2),
        "保险费(元)": Number(vs.insuranceFee || 0).toFixed(2),
        "年审费(元)": Number(vs.annualInspectionFee || 0).toFixed(2),
        "维修费(元)": Number(vs.repairCost || 0).toFixed(2),
        "合计(元)": Number(vs.totalCost || 0).toFixed(2),
        "百公里油耗(升/100km)":
          vs.fuelConsumptionPer100km > 0
            ? Number(vs.fuelConsumptionPer100km).toFixed(2)
            : "-",
      }));

      // sheet 名：车辆月度明细_YYYY-MM
      const ym = `${this.selectedYear || new Date().getFullYear()}-${String(
        month
      ).padStart(2, "0")}`;
      const sheetName = `车辆月度明细_${ym}`;
      const sheet = XLSX.utils.json_to_sheet(rows);
      XLSX.utils.book_append_sheet(workbook, sheet, sheetName);
    },

    /**
     * 单月导出：导出当前展开月份的车辆月度明细
     */
    exportMonthlyVehicleStatsSingle(month) {
      try {
        const workbook = XLSX.utils.book_new();
        this.appendMonthlyVehicleStatsSheet(workbook, month);

        const ym = `${this.selectedYear || new Date().getFullYear()}-${String(
          month
        ).padStart(2, "0")}`;
        const fileName = `车辆月度明细_${ym}.xlsx`;

        const wbout = XLSX.write(workbook, { bookType: "xlsx", type: "array" });
        saveAs(
          new Blob([wbout], { type: "application/octet-stream" }),
          fileName
        );

        this.$message && this.$message.success
          ? this.$message.success("车辆月度明细导出成功！")
          : console.log("车辆月度明细导出成功！");
      } catch (error) {
        console.error("车辆月度明细导出失败:", error);
        this.$message && this.$message.error
          ? this.$message.error("导出失败，请重试！")
          : console.error("导出失败，请重试！");
      }
    },

    /**
     * 准备导出数据（时间维度）
     */
    prepareExportData() {
      const period = this.period;
      const records = Array.isArray(this.dispatchRecords)
        ? this.dispatchRecords
        : [];

      // 统计汇总数据
      const summaryData = [];
      let categories = [];
      let buckets = [];

      const currentYear = this.selectedYear || new Date().getFullYear();

      if (period === "month") {
        categories = Array.from({ length: 12 }, (_, i) => `${i + 1}月`);
        buckets = Array.from({ length: 12 }, () => ({
          燃油费: 0,
          过路费: 0,
          停车费: 0,
          维修费: 0,
          保险费: 0,
          年审费: 0,
          里程: 0,
        }));

        records.forEach((r) => {
          const d = new Date(r.useDate || r.createdAt);
          if (d.getFullYear() !== currentYear) return;
          const m = d.getMonth();

          buckets[m]["燃油费"] += Number(r.fuelCost || 0);
          buckets[m]["过路费"] += Number(r.tollFee || 0);
          buckets[m]["停车费"] += Number(r.parkingFee || 0);
          buckets[m]["维修费"] += Number(r.repairCost || 0);
          buckets[m]["保险费"] += Number(r.insuranceFee || 0);
          buckets[m]["年审费"] += Number(r.annualInspectionFee || 0);
          buckets[m]["里程"] += Number(r.tripMileage || 0);
        });

        // 生成汇总数据
        buckets.forEach((bucket, index) => {
          const totalCost =
            bucket["燃油费"] +
            bucket["过路费"] +
            bucket["停车费"] +
            bucket["维修费"] +
            bucket["保险费"] +
            bucket["年审费"];
          const row = {
            统计周期: `${currentYear}年${categories[index]}`,
            "累计里程(km)": bucket["里程"].toFixed(0),
            "燃油费(元)": bucket["燃油费"].toFixed(2),
            "过路费(元)": bucket["过路费"].toFixed(2),
            "停车费(元)": bucket["停车费"].toFixed(2),
            "维修费(元)": bucket["维修费"].toFixed(2),
            "保险费(元)": bucket["保险费"].toFixed(2),
            "年审费(元)": bucket["年审费"].toFixed(2),
            "合计(元)": totalCost.toFixed(2),
            "百公里油耗(升/100km)":
              bucket["里程"] > 0
                ? ((bucket["燃油费"] / 6.5 / bucket["里程"]) * 100).toFixed(2)
                : "-",
          };
          summaryData.push(row);
        });
      } else if (period === "quarter") {
        categories = ["Q1", "Q2", "Q3", "Q4"];
        buckets = Array.from({ length: 4 }, () => ({
          燃油费: 0,
          过路费: 0,
          停车费: 0,
          维修费: 0,
          保险费: 0,
          年审费: 0,
          里程: 0,
        }));

        records.forEach((r) => {
          const d = new Date(r.useDate || r.createdAt);
          if (d.getFullYear() !== currentYear) return;
          const q = Math.floor(d.getMonth() / 3);

          buckets[q]["燃油费"] += Number(r.fuelCost || 0);
          buckets[q]["过路费"] += Number(r.tollFee || 0);
          buckets[q]["停车费"] += Number(r.parkingFee || 0);
          buckets[q]["维修费"] += Number(r.repairCost || 0);
          buckets[q]["保险费"] += Number(r.insuranceFee || 0);
          buckets[q]["年审费"] += Number(r.annualInspectionFee || 0);
          buckets[q]["里程"] += Number(r.tripMileage || 0);
        });

        // 生成汇总数据
        buckets.forEach((bucket, index) => {
          const totalCost =
            bucket["燃油费"] +
            bucket["过路费"] +
            bucket["停车费"] +
            bucket["维修费"] +
            bucket["保险费"] +
            bucket["年审费"];
          const row = {
            统计周期: `${currentYear}年${categories[index]}`,
            "累计里程(km)": bucket["里程"].toFixed(0),
            "燃油费(元)": bucket["燃油费"].toFixed(2),
            "过路费(元)": bucket["过路费"].toFixed(2),
            "停车费(元)": bucket["停车费"].toFixed(2),
            "维修费(元)": bucket["维修费"].toFixed(2),
            "保险费(元)": bucket["保险费"].toFixed(2),
            "年审费(元)": bucket["年审费"].toFixed(2),
            "合计(元)": totalCost.toFixed(2),
            "百公里油耗(升/100km)":
              bucket["里程"] > 0
                ? ((bucket["燃油费"] / 6.5 / bucket["里程"]) * 100).toFixed(2)
                : "-",
          };
          summaryData.push(row);
        });
      } else if (period === "year") {
        let s = Number(this.startYear) || new Date().getFullYear();
        let e = Number(this.endYear) || new Date().getFullYear();
        if (s > e) {
          const tmp = s;
          s = e;
          e = tmp;
        }

        const years = [];
        for (let y = s; y <= e; y++) years.push(y);
        categories = years.map((y) => `${y}年`);
        buckets = years.map(() => ({
          燃油费: 0,
          过路费: 0,
          停车费: 0,
          维修费: 0,
          保险费: 0,
          年审费: 0,
          里程: 0,
        }));

        records.forEach((r) => {
          const d = new Date(r.useDate || r.createdAt);
          const y = d.getFullYear();
          if (y < s || y > e) return;
          const idx = years.indexOf(y);
          if (idx === -1) return;

          buckets[idx]["燃油费"] += Number(r.fuelCost || 0);
          buckets[idx]["过路费"] += Number(r.tollFee || 0);
          buckets[idx]["停车费"] += Number(r.parkingFee || 0);
          buckets[idx]["维修费"] += Number(r.repairCost || 0);
          buckets[idx]["保险费"] += Number(r.insuranceFee || 0);
          buckets[idx]["年审费"] += Number(r.annualInspectionFee || 0);
          buckets[idx]["里程"] += Number(r.tripMileage || 0);
        });

        // 生成汇总数据
        buckets.forEach((bucket, index) => {
          const totalCost =
            bucket["燃油费"] +
            bucket["过路费"] +
            bucket["停车费"] +
            bucket["维修费"] +
            bucket["保险费"] +
            bucket["年审费"];
          const row = {
            统计周期: categories[index],
            "累计里程(km)": bucket["里程"].toFixed(0),
            "燃油费(元)": bucket["燃油费"].toFixed(2),
            "过路费(元)": bucket["过路费"].toFixed(2),
            "停车费(元)": bucket["停车费"].toFixed(2),
            "维修费(元)": bucket["维修费"].toFixed(2),
            "保险费(元)": bucket["保险费"].toFixed(2),
            "年审费(元)": bucket["年审费"].toFixed(2),
            "合计(元)": totalCost.toFixed(2),
            "百公里油耗(升/100km)":
              bucket["里程"] > 0
                ? ((bucket["燃油费"] / 6.5 / bucket["里程"]) * 100).toFixed(2)
                : "-",
          };
          summaryData.push(row);
        });
      }

      // 添加总计行
      const totalRow = {
        统计周期: "总计",
        "累计里程(km)": summaryData
          .reduce((sum, row) => sum + Number(row["累计里程(km)"]), 0)
          .toFixed(0),
        "燃油费(元)": summaryData
          .reduce((sum, row) => sum + Number(row["燃油费(元)"]), 0)
          .toFixed(2),
        "过路费(元)": summaryData
          .reduce((sum, row) => sum + Number(row["过路费(元)"]), 0)
          .toFixed(2),
        "停车费(元)": summaryData
          .reduce((sum, row) => sum + Number(row["停车费(元)"]), 0)
          .toFixed(2),
        "维修费(元)": summaryData
          .reduce((sum, row) => sum + Number(row["维修费(元)"]), 0)
          .toFixed(2),
        "保险费(元)": summaryData
          .reduce((sum, row) => sum + Number(row["保险费(元)"]), 0)
          .toFixed(2),
        "年审费(元)": summaryData
          .reduce((sum, row) => sum + Number(row["年审费(元)"]), 0)
          .toFixed(2),
        "合计(元)": summaryData
          .reduce((sum, row) => sum + Number(row["合计(元)"]), 0)
          .toFixed(2),
        "百公里油耗(升/100km)": "-",
      };

      // 计算总计的平均百公里油耗
      const totalMileage = Number(totalRow["累计里程(km)"]);
      const totalFuel = Number(totalRow["燃油费(元)"]);
      if (totalMileage > 0 && totalFuel > 0) {
        totalRow["百公里油耗(升/100km)"] = (
          (totalFuel / 6.5 / totalMileage) *
          100
        ).toFixed(2);
      }

      summaryData.push(totalRow);

      return {
        summary: summaryData,
      };
    },

    /**
     * 生成导出文件名
     */
    generateFileName() {
      const now = new Date();
      const dateStr = `${now.getFullYear()}${String(
        now.getMonth() + 1
      ).padStart(2, "0")}${String(now.getDate()).padStart(2, "0")}`;
      const timeStr = `${String(now.getHours()).padStart(2, "0")}${String(
        now.getMinutes()
      ).padStart(2, "0")}${String(now.getSeconds()).padStart(2, "0")}`;

      let periodName = "";
      if (this.period === "month") {
        periodName = `月度统计_${this.selectedYear}年`;
      } else if (this.period === "quarter") {
        periodName = `季度统计_${this.selectedYear}年`;
      } else {
        periodName = `年度统计_${this.startYear}-${this.endYear}年`;
      }

      return `公车费用统计_${periodName}_${dateStr}_${timeStr}.xlsx`;
    },
  },
  watch: {
    statisticsDimension() {
      // 统计维度改变时触发
      this.onStatisticsDimensionChange();
    },
    period() {
      // 当 period 改变时，重新计算并渲染图表（仅时间维度）
      if (this.statisticsDimension === "time") {
        this.computeStatistics();
        this.buildTimeSummaryRows();
        if (this.period === "month") this.computeMonthlyVehicleStats();
      }
    },
    selectedYear() {
      // 当所选年份改变时，重新计算并渲染图表（仅当 period 是 month/quarter 且为时间维度时）
      if (
        this.statisticsDimension === "time" &&
        (this.period === "month" || this.period === "quarter")
      ) {
        this.computeStatistics();
        this.buildTimeSummaryRows();
        if (this.period === "month") this.computeMonthlyVehicleStats();
      }
    },
    startYear() {
      if (this.statisticsDimension === "time" && this.period === "year") {
        this.computeStatistics();
        this.buildTimeSummaryRows();
      }
    },
    endYear() {
      if (this.statisticsDimension === "time" && this.period === "year") {
        this.computeStatistics();
        this.buildTimeSummaryRows();
      }
    },
    dispatchRecords() {
      // 派车记录变化时，重新计算单车统计
      this.computeVehicleStatistics();
      if (this.statisticsDimension === "time" && this.period === "month") {
        this.computeMonthlyVehicleStats();
      }
    },
  },
  beforeDestroy() {
    // 移除图表生命周期
  },
};
</script>

<style lang="scss" scoped>
$headHeight: 160px; // 修改顶部高度
$bottomHeight: 180px; // 修改底部高度

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
      padding: 50px; // 修改顶部内边距
      right: 10%;
      position: relative;
      z-index: 40; /* 设置一个较大的层级值 */
    }
  }

  .striped-table tbody tr:nth-child(odd) {
    background-color: #f9f9f9;
  }
  .striped-table tbody tr:nth-child(even) {
    background-color: #ffffff;
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
      padding-top: 80px; // 修改底部中间区域上边距
      p {
        text-align: center;
        color: #fff;
        // 添加底部链接样式
        .footer-link {
          color: #fff;
          text-decoration: none;

          &:hover {
            text-decoration: underline;
            color: #cce5ff;
          }
        }
      }
    }
    &-right {
      width: 30%;
    }
  }
}
</style>
