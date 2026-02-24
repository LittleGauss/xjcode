<template>
  <div class="app-container">
    <el-card class="box-card" shadow="always">
      <div slot="header" class="clearfix">
        <span style="font-weight: bold">统计报表</span>
      </div>

      <div class="filter-container">
        <div class="mb-4 flex items-center justify-between">
          <div class="flex items-center space-x-3">
            <label class="text-sm font-bold">查看维度：</label>
            <el-radio-group v-model="mode" @change="handleModeChange">
              <el-radio-button label="department">按部门汇总</el-radio-button>
              <el-radio-button label="person">按个人明细</el-radio-button>
            </el-radio-group>

            <label class="text-sm ml-4">年份：</label>
            <el-select
              v-model="year"
              placeholder="选择年份"
              size="small"
              style="width: 100px"
            >
              <el-option v-for="y in years" :key="y" :label="y" :value="y" />
            </el-select>

            <label class="text-sm ml-4">统计周期：</label>
            <el-select
              v-model="period"
              placeholder="选择周期"
              size="small"
              style="width: 100px"
            >
              <el-option label="年度" value="year" />
              <el-option label="季度" value="quarter" />
              <el-option label="月度" value="month" />
            </el-select>

            <template v-if="period === 'month'">
              <label class="text-sm ml-4">月份：</label>
              <el-select
                v-model="selectedMonth"
                placeholder="选择月份"
                size="small"
                style="width: 100px"
              >
                <el-option
                  v-for="m in 12"
                  :key="m"
                  :label="`${m}月`"
                  :value="m"
                />
              </el-select>
            </template>

            <template v-if="period === 'quarter'">
              <label class="text-sm ml-4">季度：</label>
              <el-select
                v-model="selectedQuarter"
                placeholder="选择季度"
                size="small"
                style="width: 100px"
              >
                <el-option label="第一季度" value="1" />
                <el-option label="第二季度" value="2" />
                <el-option label="第三季度" value="3" />
                <el-option label="第四季度" value="4" />
              </el-select>
            </template>

            <label class="text-sm ml-4">部门：</label>
            <el-select
              v-model="selectedDepartment"
              placeholder="全中心"
              size="small"
              clearable
              style="width: 160px"
              @change="handleDeptChange"
            >
              <el-option
                v-for="d in departments"
                :key="d.id"
                :label="d.name"
                :value="d.id"
              />
            </el-select>

            <template v-if="mode === 'person'">
              <label class="text-sm ml-4">人员：</label>
              <el-select
                v-model="selectedPerson"
                placeholder="全部人员"
                size="small"
                clearable
                filterable
                style="width: 160px"
              >
                <el-option
                  v-for="p in filteredPeople"
                  :key="p.id"
                  :label="p.name"
                  :value="p.id"
                />
              </el-select>
            </template>

            <el-button
              type="primary"
              size="small"
              icon="el-icon-search"
              @click="query"
              :loading="loading"
              style="margin-left: 10px"
            >
              查询
            </el-button>
          </div>

          <div class="flex space-x-2">
            <el-button
              size="small"
              icon="el-icon-download"
              @click="exportExcel"
              :loading="exporting"
            >
              导出Excel
            </el-button>
          </div>
        </div>

        <el-table
          :data="tableData"
          stripe
          border
          style="width: 100%"
          v-loading="loading"
        >
          <el-table-column prop="dept" label="部门" min-width="120" />

          <template v-if="mode === 'department'">
            <el-table-column
              prop="leaveTypeName"
              label="请假类型"
              min-width="120"
            />
            <el-table-column
              prop="year"
              label="年份"
              width="80"
              align="center"
            />
            <el-table-column
              prop="count"
              label="申请次数"
              width="100"
              align="center"
            />
            <el-table-column
              prop="used"
              label="合计天数(天)"
              width="120"
              align="center"
            />
          </template>

          <template v-else>
            <el-table-column label="姓名" min-width="120">
              <template slot-scope="scope">
                {{ scope.row.person }}
              </template>
            </el-table-column>
            <el-table-column
              prop="year"
              label="年份"
              width="80"
              align="center"
            />
            <el-table-column
              prop="totalDays"
              label="年假总额(天)"
              width="120"
              align="center"
            />
            <el-table-column
              prop="used"
              label="已用(天)"
              width="100"
              align="center"
            >
              <template slot-scope="scope">
                <span
                  :class="{
                    'text-red-500 font-bold':
                      scope.row.used > scope.row.totalDays,
                  }"
                >
                  {{ scope.row.used }}
                </span>
              </template>
            </el-table-column>
            <el-table-column
              prop="remaining"
              label="剩余(天)"
              width="100"
              align="center"
            >
              <template slot-scope="scope">
                <span
                  :class="{
                    'text-green-600 font-bold': scope.row.remaining > 0,
                  }"
                >
                  {{ scope.row.remaining }}
                </span>
              </template>
            </el-table-column>
          </template>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted } from "@vue/composition-api";
import { userApi } from "@/api/usermag";
import { leaveApi } from "@/api/leave";
import { getUserToken, removeUserToken } from "@/utils/auth";
import { removeToken } from "@/utils/auth";

export default {
  name: "LeaveReport",
  data() {
    return {
      login_user: null,
      userRoles: [],
      exporting: false,
    };
  },
  created() {
    const storedUserInfo = getUserToken();
    if (storedUserInfo && storedUserInfo.user) {
      this.login_user = storedUserInfo.user;
      this.userRoles = storedUserInfo.roles;
    }
  },
  methods: {
    handleLogout() {
      removeUserToken();
      this.login_user = null;
      removeToken();
      try {
        this.$router.push("/login");
      } catch (e) {
        console.warn("navigate to login failed:", e);
      }
    },
    handleNavigate(routeName) {
      if (!routeName) return;
      this.$router.push({ name: routeName });
    },
  },

  setup(props, { root }) {
    // --- 状态定义 ---
    const mode = ref("department");
    const year = ref(new Date().getFullYear());
    const years = ref([
      new Date().getFullYear() - 1,
      new Date().getFullYear(),
      new Date().getFullYear() + 1,
    ]);
    const period = ref("year");
    const selectedMonth = ref(new Date().getMonth() + 1);
    const selectedQuarter = ref(Math.ceil((new Date().getMonth() + 1) / 3));

    const departments = ref([]);
    const people = ref([]);
    const seniorityTable = ref([]);

    const selectedDepartment = ref(null);
    const selectedPerson = ref(null);
    const tableData = ref([]);
    const loading = ref(false);

    // --- 计算属性 ---
    const filteredPeople = computed(() => {
      if (!selectedDepartment.value) return people.value;
      return people.value.filter((p) => p.deptId === selectedDepartment.value);
    });

    // --- 核心业务逻辑 ---

    // 1. 计算工龄
    const getYearsOfService = (hireDate) => {
      if (!hireDate) return 0;
      const h = new Date(hireDate);
      if (isNaN(h)) return 0;
      const now = new Date();
      const diff = now - h;
      const years = diff / (1000 * 60 * 60 * 24 * 365);
      return Math.floor(years * 100) / 100;
    };

    // 2. 计算个人权益 (前端计算)
    const computeEntitlements = (person) => {
      const years = getYearsOfService(person.hireDate);
      let annual = 0;

      if (seniorityTable.value.length > 0) {
        for (const row of seniorityTable.value) {
          if (years >= row.min && years <= row.max) {
            annual = row.days;
            break;
          }
        }
      }

      return { yearsOfService: years, annualLeave: annual };
    };

    const leaveTypeNameMap = {
      annual: "年假",
      sick: "病假",
      personal: "事假",
      marriage: "婚假",
      maternity: "产假",
      funeral: "葬假",
      parental: "育儿假",
      visiting: "探亲假",
      paternity: "陪产假",
      business_trip: "出差",
    };

    const normalizeLeaveType = (t) =>
      t && String(t).trim() ? String(t).trim() : "unknown";

    const getLeaveTypeName = (type) =>
      leaveTypeNameMap[type] || type || "未知类型";

    const parseDuration = (l) =>
      Number(l.duration || l.days || l.durationDays || 0) || 0;

    const isLeaveInPeriod = (leave, targetYear, periodFilter) => {
      const sd = leave.startDate ? new Date(leave.startDate) : null;
      if (!sd || !sd.getFullYear) return false;
      if (sd.getFullYear() !== Number(targetYear)) return false;

      const month = sd.getMonth() + 1;
      const quarter = Math.ceil(month / 3);

      if (periodFilter.type === "year") return true;
      if (periodFilter.type === "quarter")
        return quarter === Number(periodFilter.quarter);
      if (periodFilter.type === "month")
        return month === Number(periodFilter.month);
      return true;
    };

    const buildPeriodFilter = () => {
      const filter = { type: period.value };
      if (period.value === "month") filter.month = selectedMonth.value;
      if (period.value === "quarter")
        filter.quarter = parseInt(selectedQuarter.value, 10);
      return filter;
    };

    const getPersonUsedDays = async (personId, targetYear, periodFilter) => {
      try {
        const r = await leaveApi.getMyLeaves(personId);
        const leaves = (r && r.data) || [];
        const list = Array.isArray(leaves)
          ? leaves
          : leaves.records || leaves.list || [];

        return list.reduce((s, l) => {
          try {
            if (!isLeaveInPeriod(l, targetYear, periodFilter)) return s;
            const dur = parseDuration(l);
            return s + dur;
          } catch (e) {
            // ignore
          }
          return s;
        }, 0);
      } catch (e) {
        console.warn(`获取用户 ${personId} 请假记录失败`);
        return 0;
      }
    };

    // --- 数据加载 ---
    const loadDepartmentsAndPeople = async () => {
      loading.value = true;
      try {
        const dres = await userApi.getDepartmentList();
        const dlist = (dres && dres.data) || [];
        if (Array.isArray(dlist)) {
          departments.value = dlist.map((d) => ({
            id: d.id || d.deptId || d.departmentId,
            name: d.name || d.deptName,
          }));
          if (!selectedDepartment.value && departments.value.length > 0) {
            selectedDepartment.value = departments.value[0].id;
          }
        }

        const ures = await userApi.getUserList({ pageNum: 1, pageSize: 2000 });
        let ulist = [];
        if (Array.isArray(ures && ures.data)) ulist = ures.data;
        else if (ures && ures.data && (ures.data.records || ures.data.list))
          ulist = ures.data.records || ures.data.list;

        if (Array.isArray(ulist)) {
          people.value = ulist.map((u) => ({
            id: u.id,
            name: u.name || u.realName || u.username,
            deptId:
              u.deptId ||
              u.departmentId ||
              (u.department && u.department.id) ||
              null,
            hireDate: u.hireDate || u.entryDate || null,
            married: u.married || false,
            gender: u.gender || u.sex || null,
          }));
        }

        try {
          const sres = await leaveApi.getSeniorityTable();
          const sl = (sres && sres.data) || [];
          if (Array.isArray(sl)) {
            seniorityTable.value = sl.map((item) => ({
              min: item.minYears || item.min || 0,
              max: item.maxYears || item.max || 0,
              days: item.days || item.daysCount || 0,
            }));
          }
        } catch (se) {
          // ignore rule loading error
        }
      } catch (e) {
        console.warn("加载基础数据失败:", e);
      } finally {
        loading.value = false;
      }
    };

    // --- 查询逻辑 ---
    const query = async () => {
      if (!departments.value.length) return;
      loading.value = true;
      tableData.value = [];

      try {
        if (mode.value === "department") {
          const targetDepts = selectedDepartment.value
            ? departments.value.filter((d) => d.id === selectedDepartment.value)
            : departments.value;

          const periodFilter = buildPeriodFilter();
          const allRows = [];

          for (const d of targetDepts) {
            const deptPeople = people.value.filter((p) => p.deptId === d.id);
            const typeAgg = {};

            const personLeaves = await Promise.all(
              deptPeople.map(async (person) => {
                try {
                  const res = await leaveApi.getMyLeaves(person.id);
                  const leaves = Array.isArray(res && res.data)
                    ? res.data
                    : res?.data?.records || res?.data?.list || [];
                  return { person, leaves };
                } catch (err) {
                  return { person, leaves: [] };
                }
              })
            );

            for (const { leaves } of personLeaves) {
              for (const l of leaves) {
                if (!isLeaveInPeriod(l, year.value, periodFilter)) continue;
                const type = normalizeLeaveType(
                  l.leaveType || l.type || l.leaveTypeCode
                );
                const dur = parseDuration(l);
                if (!typeAgg[type]) typeAgg[type] = { days: 0, count: 0 };
                typeAgg[type].days += dur;
                typeAgg[type].count += 1;
              }
            }

            const hasData = Object.keys(typeAgg).length > 0;
            const typesToRender = hasData ? Object.keys(typeAgg) : ["all"];

            for (const t of typesToRender) {
              const stats = typeAgg[t] || { days: 0, count: 0 };
              allRows.push({
                dept: d.name,
                leaveType: t,
                leaveTypeName: t === "all" ? "全部类型" : getLeaveTypeName(t),
                year: year.value,
                count: stats.count,
                used: Math.round(stats.days * 10) / 10,
              });
            }
          }
          tableData.value = allRows;
        } else {
          let persons = [];
          if (selectedPerson.value) {
            persons = people.value.filter((p) => p.id === selectedPerson.value);
          } else if (selectedDepartment.value) {
            persons = people.value.filter(
              (p) => p.deptId === selectedDepartment.value
            );
          } else {
            persons = people.value.slice(0, 50);
            if (people.value.length > 50) {
              if (root.$message) {
                root.$message.warning("未选择部门，仅展示前50名员工");
              }
            }
          }

          const periodFilter = buildPeriodFilter();
          const rows = await Promise.all(
            persons.map(async (p) => {
              const used = await getPersonUsedDays(
                p.id,
                year.value,
                periodFilter
              );
              const ent = computeEntitlements(p);

              return {
                dept:
                  departments.value.find((d) => d.id === p.deptId)?.name || "",
                person: p.name,
                year: year.value,
                totalDays: ent.annualLeave,
                used: used,
                remaining: Math.max(0, ent.annualLeave - used),
                entitlements: ent,
              };
            })
          );
          tableData.value = rows;
        }
      } finally {
        loading.value = false;
      }
    };

    const handleModeChange = () => {
      tableData.value = [];
      selectedPerson.value = null;
      if (mode.value === "department") {
        query();
      }
    };

    const handleDeptChange = () => {
      selectedPerson.value = null;
    };

    const exportExcel = async () => {
      if (!tableData.value.length) {
        if (root.$message) root.$message.warning("暂无数据可导出");
        return;
      }

      root.exporting = true;
      try {
        const XLSX = await import("xlsx");

        const periodText =
          period.value === "year"
            ? "年度"
            : period.value === "quarter"
            ? `第${selectedQuarter.value}季度`
            : `${selectedMonth.value}月`;
        const sheetName = `${year.value}年${periodText}统计`;
        const workbook = XLSX.utils.book_new();

        const headers =
          mode.value === "department"
            ? ["部门", "请假类型", "年份", "申请次数", "请假天数(天)"]
            : ["部门", "姓名", "年份", "年假总额(天)", "已用(天)", "剩余(天)"];

        const dataRows =
          mode.value === "department"
            ? tableData.value.map((r) => [
                r.dept,
                r.leaveTypeName,
                r.year,
                r.count,
                r.used,
              ])
            : tableData.value.map((r) => [
                r.dept,
                r.person,
                r.year,
                r.totalDays,
                r.used,
                r.remaining,
              ]);

        const worksheetData = [headers, ...dataRows];
        const worksheet = XLSX.utils.aoa_to_sheet(worksheetData);

        worksheet["!cols"] =
          mode.value === "department"
            ? [{ wch: 15 }, { wch: 15 }, { wch: 10 }, { wch: 10 }, { wch: 12 }]
            : [
                { wch: 15 },
                { wch: 15 },
                { wch: 10 },
                { wch: 14 },
                { wch: 10 },
                { wch: 10 },
              ];

        XLSX.utils.book_append_sheet(workbook, worksheet, sheetName);

        const fileName = `请假统计_${year.value}年${periodText}_${new Date()
          .toLocaleDateString("zh-CN")
          .replace(/\//g, "")}.xlsx`;

        XLSX.writeFile(workbook, fileName);

        if (root.$message) root.$message.success("导出成功");
      } catch (e) {
        console.error("导出失败:", e);
        if (root.$message) root.$message.error("导出失败：" + e.message);
      } finally {
        root.exporting = false;
      }
    };

    onMounted(() => {
      loadDepartmentsAndPeople().then(() => {
        if (departments.value.length > 0) {
          query();
        }
      });
    });

    return {
      mode,
      year,
      years,
      period,
      selectedMonth,
      selectedQuarter,
      departments,
      people,
      filteredPeople,
      selectedDepartment,
      selectedPerson,
      tableData,
      loading,
      query,
      exportExcel,
      handleModeChange,
      handleDeptChange,
      getLeaveTypeName,
    };
  },
};
</script>

<style scoped>
/* 核心修复样式：增加外边距，使页面不会贴边 */
.app-container {
  padding: 20px;
}
</style>
