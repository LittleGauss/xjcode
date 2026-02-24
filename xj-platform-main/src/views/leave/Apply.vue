<template>
  <div class="leave-apply-view">
    <div class="leave-apply">
      <el-card>
        <div slot="header">
          <span>请假申请</span>
        </div>

        <el-form
          :model="form"
          :rules="rules"
          ref="leaveForm"
          label-width="120px"
        >
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="请假类型" prop="leaveType">
                <el-select
                  v-model="form.leaveType"
                  placeholder="请选择类型"
                  style="width: 100%"
                >
                  <el-option label="年假" value="annual"></el-option>
                  <el-option label="病假" value="sick"></el-option>
                  <el-option label="事假" value="personal"></el-option>
                  <el-option label="婚假" value="marriage"></el-option>
                  <el-option label="产假" value="maternity"></el-option>
                  <el-option label="葬假" value="funeral"></el-option>
                  <el-option label="育儿假" value="parental"></el-option>
                  <el-option label="探亲假" value="visiting"></el-option>
                  <el-option label="陪产假" value="paternity"></el-option>
                  <el-option label="出差" value="business_trip"></el-option>
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="申请人" prop="userName">
                <el-input v-model="form.userName" disabled></el-input>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="所在部门" prop="departmentId">
                <el-select
                  v-model="form.departmentId"
                  placeholder="请选择部门"
                  style="width: 100%"
                >
                  <el-option
                    v-for="d in departments"
                    :key="d.id"
                    :label="d.name"
                    :value="d.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="请假时长">
                <div style="display: flex; align-items: center">
                  <el-input
                    v-model="form.days"
                    disabled
                    placeholder="自动计算"
                    style="flex: 0 0 160px"
                  >
                    <template slot="append">天</template>
                  </el-input>
                  <span
                    v-if="remainingForSelectedType !== null"
                    class="remaining-info"
                    style="margin-left: 12px; color: #666; font-size: 13px"
                  >
                    剩余{{ remainingForSelectedType }}天
                  </span>
                  <span
                    v-else
                    style="margin-left: 12px; color: #999; font-size: 13px"
                    >无固定额度</span
                  >
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="开始时间" prop="startDate">
                <div style="display: flex; gap: 10px">
                  <el-date-picker
                    v-model="form.startDate"
                    type="date"
                    placeholder="选择开始日期"
                    value-format="yyyy-MM-dd"
                    style="flex: 1"
                    @change="calculateDuration"
                  >
                  </el-date-picker>
                  <el-select
                    v-model="form.startPeriod"
                    placeholder="时段"
                    style="width: 100px"
                    @change="calculateDuration"
                  >
                    <el-option label="上午" value="am"></el-option>
                    <el-option label="下午" value="pm"></el-option>
                  </el-select>
                </div>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="结束时间" prop="endDate">
                <div style="display: flex; gap: 10px">
                  <el-date-picker
                    v-model="form.endDate"
                    type="date"
                    placeholder="选择结束日期"
                    value-format="yyyy-MM-dd"
                    style="flex: 1"
                    @change="calculateDuration"
                  >
                  </el-date-picker>
                  <el-select
                    v-model="form.endPeriod"
                    placeholder="时段"
                    style="width: 100px"
                    @change="calculateDuration"
                  >
                    <el-option label="上午" value="am"></el-option>
                    <el-option label="下午" value="pm"></el-option>
                  </el-select>
                </div>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="请假原因" prop="reason">
            <el-input
              type="textarea"
              v-model="form.reason"
              :rows="4"
              placeholder="请输入请假原因"
            >
            </el-input>
          </el-form-item>

          <el-divider content-position="left">审批流程设置</el-divider>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="部门主管" prop="deptManager">
                <el-select
                  v-model="form.deptManager"
                  placeholder="请选择部门主管"
                  filterable
                  clearable
                  :loading="approverLoading"
                  style="width: 100%"
                >
                  <el-option
                    v-for="opt in deptManagerOptions"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item label="行政办审批" prop="adminUser">
                <el-select
                  v-model="form.adminUser"
                  placeholder="请选择行政办人员"
                  filterable
                  clearable
                  :loading="approverLoading"
                  style="width: 100%"
                >
                  <el-option
                    v-for="opt in adminUserOptions"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12" v-if="form.days > 0.5">
              <el-form-item
                label="分管领导"
                prop="viceLeader"
                :rules="form.days > 0.5 ? rules.viceLeader : []"
              >
                <el-select
                  v-model="form.viceLeader"
                  placeholder="时长>0.5天需分管审批"
                  filterable
                  clearable
                  :loading="approverLoading"
                  style="width: 100%"
                >
                  <el-option
                    v-for="opt in viceLeaderOptions"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="12" v-if="form.days > 3">
              <el-form-item
                label="主要领导"
                prop="mainLeader"
                :rules="form.days > 3 ? rules.mainLeader : []"
              >
                <el-select
                  v-model="form.mainLeader"
                  placeholder="时长>3天需主要领导审批"
                  filterable
                  clearable
                  :loading="approverLoading"
                  style="width: 100%"
                >
                  <el-option
                    v-for="opt in mainLeaderOptions"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="附件">
            <Attachment
              ref="attachment"
              :leave-id="leaveId"
              :operator-id="form.userId"
              :upload-data="{ leaveId: leaveId, uploadUserId: form.userId }"
              :allow-delete="true"
              @upload-success="onAttachmentUploadSuccess"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="submitForm('submit')">
              提交申请
            </el-button>
            <el-button @click="submitForm('draft')">保存草稿</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card style="margin-top: 20px" v-if="drafts.length > 0">
        <div slot="header">
          <span>我的草稿</span>
        </div>
        <el-table :data="drafts" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column prop="leaveType" label="请假类型" width="120">
            <template slot-scope="scope">
              {{ getLeaveTypeName(scope.row.leaveType) }}
            </template>
          </el-table-column>
          <el-table-column
            prop="startDate"
            label="开始时间"
            width="180"
          ></el-table-column>
          <el-table-column
            prop="endDate"
            label="结束时间"
            width="180"
          ></el-table-column>
          <el-table-column
            prop="duration"
            label="天数"
            width="80"
          ></el-table-column>
          <el-table-column
            prop="updatedAt"
            label="更新时间"
            width="180"
          ></el-table-column>
          <el-table-column label="操作">
            <template slot-scope="scope">
              <el-button size="mini" @click="editDraft(scope.row)">
                编辑
              </el-button>
              <el-button
                size="mini"
                type="success"
                @click="submitDraft(scope.row)"
              >
                提交
              </el-button>
              <el-button
                size="mini"
                type="danger"
                @click="deleteDraft(scope.row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script>
import { leaveApi } from "@/api/leave";
import Attachment from "@/components/Attachment.vue";
import { userApi } from "@/api/usermag";
import { getUserToken, removeUserToken } from "@/utils/auth";

// 固定节假日（按月-日），用于年假/事假时长扣除
const FIXED_HOLIDAYS = new Set([
  "01-01", // 元旦
  "01-02",
  "01-03",
  "05-01", // 五一
  "05-02",
  "05-03",
  "10-01", // 国庆
  "10-02",
  "10-03",
  "10-04",
  "10-05",
  "10-06",
  "10-07",
]);

export default {
  name: "LeaveApply",
  components: {
    Attachment,
  },
  data() {
    return {
      login_user: null,
      userRoles: [],
      form: {
        userId: 1,
        userName: "",
        departmentId: null,
        leaveType: "",
        startDate: "",
        endDate: "",
        startPeriod: "am", // 上午am/下午pm
        endPeriod: "pm", // 上午am/下午pm
        days: 0, // 新增：请假天数
        reason: "",
        // 流程变量字段
        deptManager: "", // 部门主管
        adminUser: "", // 行政办
        viceLeader: "", // 分管领导
        mainLeader: "", // 主要领导
      },
      // 请假余额信息
      leaveBalances: {},
      rules: {
        leaveType: [
          { required: true, message: "请选择请假类型", trigger: "change" },
        ],
        startDate: [
          { required: true, message: "请选择开始时间", trigger: "change" },
        ],
        endDate: [
          { required: true, message: "请选择结束时间", trigger: "change" },
        ],
        reason: [
          { required: true, message: "请输入请假原因", trigger: "blur" },
        ],
        deptManager: [
          { required: true, message: "请选择部门主管", trigger: "change" },
        ],
        adminUser: [
          { required: true, message: "请选择行政办人员", trigger: "change" },
        ],
        // 动态规则：viceLeader 和 mainLeader 在 template 中通过 v-if 控制
        viceLeader: [
          { required: true, message: "请选择分管领导", trigger: "change" },
        ],
        mainLeader: [
          { required: true, message: "请选择主要领导", trigger: "change" },
        ],
      },
      leaveId: null,
      uploadedAttachmentIds: [],
      drafts: [],
      approverLoading: false,
      // 审批人选项列表
      deptManagerOptions: [],
      adminUserOptions: [],
      viceLeaderOptions: [],
      mainLeaderOptions: [],
      departments: [],
    };
  },
  computed: {
    attachmentUploadData() {
      const userId = this.form.userId;
      const finalUserId = userId && !isNaN(Number(userId)) ? Number(userId) : 1;
      const data = { uploadUserId: finalUserId };
      if (this.leaveId && !isNaN(Number(this.leaveId))) {
        data.leaveId = this.leaveId;
      }
      return data;
    },
    // 当前选中类型的剩余额（数值或 null）
    remainingForSelectedType() {
      const t = this.form.leaveType;
      if (!t || !this.leaveBalances) return null;
      const info = this.leaveBalances[t];
      if (!info) return null;
      return info.remaining;
    },
  },
  mounted() {
    // 从会话中获取登录用户ID（后端登录已保存 userInfo 到 session）
    const loginUser = getUserToken();
    // 填充通用头需要的用户信息
    if (loginUser) {
      this.login_user = loginUser.user || loginUser;
      this.userRoles = loginUser.roles || [];
    }
    const uid = loginUser && loginUser.user && loginUser.user.id;
    const uname =
      loginUser &&
      loginUser.user &&
      (loginUser.user.nickname ||
        loginUser.user.username ||
        loginUser.user.realName);
    if (uid) this.form.userId = uid;
    if (uname) this.form.userName = uname;

    this.loadDrafts();
    this.loadAllApprovers(); // 改名：加载所有类型审批人
    this.loadDepartments();
    // 计算并加载请假余额
    this.computeLeaveBalances();
  },
  methods: {
    handleLogout() {
      removeUserToken();
      try {
        this.$router.push("/login");
      } catch (e) {
        console.warn("navigate to login failed:", e);
      }
    },
    handleNavigate(routeName) {
      if (!routeName) return;
      try {
        this.$router.push({ name: routeName });
      } catch (e) {
        try {
          this.$router.push(routeName);
        } catch (err) {
          console.warn("navigate failed:", err);
        }
      }
    },
    normalizeDate(val) {
      if (!val) return val;
      if (typeof val === "string") {
        return val.replace(" ", "T");
      }
      if (val instanceof Date) {
        const pad = (n) => (n < 10 ? "0" + n : "" + n);
        const y = val.getFullYear();
        const m = pad(val.getMonth() + 1);
        const d = pad(val.getDate());
        const hh = pad(val.getHours());
        const mm = pad(val.getMinutes());
        const ss = pad(val.getSeconds());
        return `${y}-${m}-${d}T${hh}:${mm}:${ss}`;
      }
      return val;
    },
    // 将日期与时段组合成完整时间字符串，供后端 LocalDateTime 解析
    combineDatePeriod(dateStr, period, isEnd) {
      if (!dateStr) return dateStr;
      const p = period || "am";
      let time = "00:00:00";
      if (isEnd) {
        time = p === "am" ? "12:00:00" : "23:59:59";
      } else {
        time = p === "am" ? "00:00:00" : "12:00:00";
      }
      return `${dateStr} ${time}`;
    },
    // 新增：计算天数逻辑
    calculateDuration() {
      const start = this.toDateSafe(this.form.startDate);
      const end = this.toDateSafe(this.form.endDate);

      if (!start || !end) {
        this.form.days = 0;
        return;
      }

      const isSkipWeekend =
        this.form.leaveType === "annual" || this.form.leaveType === "personal";

      let days = 0;

      // 判断是否同一天
      const isSameDay = start.toDateString() === end.toDateString();

      if (isSameDay) {
        // 同一天的情况
        const startPeriod = this.form.startPeriod || "am";
        const endPeriod = this.form.endPeriod || "pm";

        if (startPeriod === "am" && endPeriod === "pm") {
          days = 1; // 上午到下午，1天
        } else if (startPeriod === endPeriod) {
          days = 0.5; // 同一时段，0.5天
        } else {
          days = 0; // 下午到上午，不合理，设为0
        }
      } else if (end < start) {
        // 结束日期早于开始日期
        days = 0;
      } else {
        // 跨天的情况
        if (isSkipWeekend) {
          days = this.computeWorkdayDurationWithPeriod(
            start,
            end,
            this.form.startPeriod,
            this.form.endPeriod
          );
        } else {
          const diff = end.getTime() - start.getTime();
          const fullDays = diff / (1000 * 60 * 60 * 24);

          // 根据时段调整
          const startPeriod = this.form.startPeriod || "am";
          const endPeriod = this.form.endPeriod || "pm";

          days = fullDays;
          if (startPeriod === "pm") days -= 0.5; // 开始是下午，减0.5天
          if (endPeriod === "am") days -= 0.5; // 结束是上午，减0.5天
          if (startPeriod === "am" && endPeriod === "pm") days += 1; // 开始上午结束下午，加1天
        }
      }

      this.form.days = Math.max(0, Math.round(days * 2) / 2); // 四舍五入到0.5
      this.updateRemainingForSelectedType();
    },

    // 按天累加忽略周末与固定节假日，支持时段的场景
    computeWorkdayDurationWithPeriod(start, end, startPeriod, endPeriod) {
      let cursor = new Date(start);
      cursor.setHours(0, 0, 0, 0);
      const endDate = new Date(end);
      endDate.setHours(0, 0, 0, 0);

      let workdays = 0;

      // 计算完整工作日
      while (cursor <= endDate) {
        if (!this.isWeekend(cursor) && !this.isHoliday(cursor)) {
          workdays += 1;
        }
        cursor.setDate(cursor.getDate() + 1);
      }

      // 根据时段调整
      const startP = startPeriod || "am";
      const endP = endPeriod || "pm";

      if (startP === "pm") workdays -= 0.5; // 开始是下午，减0.5天
      if (endP === "am") workdays -= 0.5; // 结束是上午，减0.5天
      if (startP === "am" && endP === "pm") workdays += 0; // 标准情况，不调整

      return Math.max(0, workdays);
    },

    // 按天累加忽略周末与固定节假日，支持跨天且包含半天的场景
    computeWorkdayDuration(start, end) {
      const dayMs = 24 * 60 * 60 * 1000;
      let cursor = new Date(start);
      let totalMs = 0;

      while (cursor < end) {
        const dayStart = new Date(cursor);
        const nextMidnight = new Date(cursor);
        nextMidnight.setHours(24, 0, 0, 0);
        const segmentEnd = new Date(
          Math.min(nextMidnight.getTime(), end.getTime())
        );

        if (!this.isWeekend(dayStart) && !this.isHoliday(dayStart)) {
          totalMs += segmentEnd.getTime() - dayStart.getTime();
        }

        cursor = nextMidnight;
      }

      return totalMs / dayMs;
    },

    isWeekend(date) {
      const day = date.getDay();
      return day === 0 || day === 6;
    },

    isHoliday(date) {
      const pad = (n) => (n < 10 ? "0" + n : "" + n);
      const key = `${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
      return FIXED_HOLIDAYS.has(key);
    },

    toDateSafe(val) {
      if (!val) return null;
      if (val instanceof Date) return isNaN(val) ? null : val;
      if (typeof val === "string") {
        const normalized = val.replace(" ", "T");
        const d = new Date(normalized);
        if (!isNaN(d)) return d;
      }
      const d2 = new Date(val);
      return isNaN(d2) ? null : d2;
    },

    // 获取并计算用户请假已用/剩余额度
    async computeLeaveBalances() {
      try {
        if (!this.form.userId) return;
        // 拉取我的请假记录
        const res = await leaveApi.getMyLeaves(this.form.userId);
        const leaves = (res && res.data) || [];
        const list = Array.isArray(leaves)
          ? leaves
          : leaves.records || leaves.list || [];

        // 汇总已用天数按类型
        const usedByType = {};

        const parseDur = (l) => {
          return Number(l.duration || l.days || l.durationDays || 0) || 0;
        };

        for (const l of list) {
          try {
            const type = l.leaveType || l.type || l.leaveTypeCode;
            if (!type) continue;
            // skip drafts
            if (l.status && String(l.status).toLowerCase() === "draft")
              continue;
            const dur = parseDur(l);
            usedByType[type] = (usedByType[type] || 0) + dur;
          } catch (e) {
            // ignore malformed record
          }
        }

        // 准备 entitlement 规则
        const entitlements = this.computeEntitlementsForUser();

        // 构建 leaveBalances
        const balances = {};
        const allTypes = [
          "annual",
          "sick",
          "personal",
          "marriage",
          "maternity",
          "funeral",
          "parental",
          "visiting",
          "paternity",
          "business_trip",
        ];

        for (const t of allTypes) {
          const ent = entitlements[t];
          const used = Math.round((usedByType[t] || 0) * 10) / 10;
          if (ent != null) {
            const remaining = Math.round(Math.max(0, ent - used) * 10) / 10;
            balances[t] = { entitlement: ent, used, remaining };
          } else {
            balances[t] = { entitlement: null, used, remaining: null };
          }
        }

        this.leaveBalances = balances;
        this.updateRemainingForSelectedType();
      } catch (e) {
        console.warn("计算请假余额失败", e);
      }
    },

    // 根据用户信息计算各类假的标准额度（尽量按前端规则估算）
    computeEntitlementsForUser() {
      const u = this.login_user || {};
      // 默认值
      const ent = {};

      // 年假：根据 User.createdAt（入职时间）计算工龄
      // User 表中 createdAt 字段记录用户的入职时间
      let annual = 0;
      try {
        const hire = u.createdAt;

        if (hire) {
          const years = (() => {
            const h = new Date(hire);
            if (isNaN(h)) return 0;
            // 计算工龄：(当前时间 - 入职时间) / 一年的毫秒数
            return (
              Math.floor(
                ((new Date() - h) / (1000 * 60 * 60 * 24 * 365)) * 100
              ) / 100
            );
          })();
          // 简单规则：工龄 < 10:5, 10-20:10, >=20:15
          if (years < 10) annual = 5;
          else if (years < 20) annual = 10;
          else annual = 15;
          console.log(
            `[年假计算] 入职时间(createdAt): ${hire}, 工龄: ${years}年, 年假: ${annual}天`
          );
        }
      } catch (e) {
        console.warn("计算年假失败", e);
      }
      ent.annual = annual;

      // 病假/事假：通常没有固定年额度，置为 null（不显示）
      ent.sick = null;
      ent.personal = null;

      // 婚假：23 天
      ent.marriage = 23;

      // 产假：女 158 天；陪产/男方护理假 20 天
      ent.maternity = 158;
      ent.paternity = 20;

      // 葬假：3 天
      ent.funeral = 3;

      // 育儿假：每年10天
      ent.parental = 10;

      // 探亲假：根据婚姻状态粗略处理
      // 若有已婚标记：探望父母三年一次 30 天 -> 我们以 3 年周期 30 天，按当次剩余计算
      // 若未婚：每年20天（同时可支持两年一次45天的复杂规则，这里使用每年20天作为更常用的计算）
      const married = !!(
        u.married ||
        u.isMarried ||
        u.maritalStatus === "married"
      );
      if (married)
        ent.visiting = 30; // 三年一次，但前端展示以 30 为额度（后端可校验周期性限制）
      else ent.visiting = 20;

      return ent;
    },

    // 更新当前表单选中类型的剩余额显示（供模板使用）
    updateRemainingForSelectedType() {
      // 触发视图刷新（computed will read leaveBalances and form.leaveType）
      this.$forceUpdate && this.$forceUpdate();
    },

    submitForm(action) {
      this.$refs.leaveForm.validate(async (valid) => {
        if (!valid) return;

        try {
          const formattedStart = this.normalizeDate(
            this.combineDatePeriod(
              this.form.startDate,
              this.form.startPeriod,
              false
            )
          );
          const formattedEnd = this.normalizeDate(
            this.combineDatePeriod(this.form.endDate, this.form.endPeriod, true)
          );
          const durationVal = parseFloat(this.form.days) || 0;

          // =======================================================
          // 【修改点 1】: 获取选中的部门名称
          // =======================================================
          let targetDeptName = "";
          if (this.form.departmentId && this.departments.length > 0) {
            // 在下拉选项列表中查找当前选中的 ID 对应的对象
            const deptObj = this.departments.find(
              (d) => d.id === this.form.departmentId
            );
            if (deptObj) {
              targetDeptName = deptObj.name;
            }
          }
          // =======================================================
          if (action === "submit") {
            // 构造符合 BPMN 流程的数据结构
            const requestData = {
              leaveProcess: {
                userId: this.form.userId,
                departmentId: this.form.departmentId,
                leaveType: this.form.leaveType,
                startDate: formattedStart,
                endDate: formattedEnd,
                reason: this.form.reason,
                duration: durationVal,
                // =======================================================
                // 【修改点 2】: 将姓名和部门名称放入实体对象中
                // =======================================================
                username: this.form.userName, // 直接取表单中的申请人姓名
                departmentName: targetDeptName, // 取上面查找到的部门名称
                // =======================================================
              },
              // --- 核心修复开始：参数名映射 ---
              starter: this.form.userId.toString(),

              // 1. 部门主管：前端(deptManager) -> 后端DTO(firstApprover)
              firstApprover: this.form.deptManager,

              // 2. 分管领导：前端(viceLeader) -> 后端DTO(secondApprover)
              secondApprover: this.form.viceLeader || "",

              // 3. 行政办和主要领导 (如果你的 DTO 已经加了这两个字段)
              adminUser: this.form.adminUser,
              mainLeader: this.form.mainLeader || "",
              // --- 核心修复结束 ---

              // 流程变量
              days: durationVal,
              isLeader: false,
            };

            // 如果有已上传但尚未关联 leave 的附件，传给后端，后端会在创建请假后把这些附件的 leave_id 更新为新请假单ID
            if (
              this.uploadedAttachmentIds &&
              this.uploadedAttachmentIds.length > 0
            ) {
              requestData.attachmentIds = this.uploadedAttachmentIds;
            }

            const res = await leaveApi.submitLeave(requestData);
            console.log("提交成功:", res.message || res.data);
            this.$message.success("提交成功");
            this.resetForm();
          } else {
            // 保存草稿 (草稿的字段通常直接存 JSON，如果没有严格 DTO 校验，可以保持原样，或者也统一一下)
            const draftData = {
              id: this.leaveId || this.form.id,
              userId: this.form.userId,
              leaveType: this.form.leaveType,
              startDate: formattedStart,
              endDate: formattedEnd,
              reason: this.form.reason,
              status: "DRAFT",
              duration: durationVal,
              // =======================================================
              // 【修改点 3】: 草稿最好也保存这两个字段，方便回显
              // =======================================================
              username: this.form.userName,
              departmentName: targetDeptName,
              // =======================================================
              // 草稿表如果直接存 deptManager 字段，这里可以不用改；
              // 如果也是映射，请根据你的草稿实体类调整。通常草稿为了回显方便，存 deptManager 没问题。
              deptManager: this.form.deptManager,
            };

            await leaveApi.saveDraft(draftData);
            this.$message.success("草稿保存成功");
            this.loadDrafts();
          }
        } catch (err) {
          console.error(err); // 打印完整错误以便调试
          const msg =
            err?.response?.data?.message ||
            (action === "submit" ? "提交失败" : "保存失败");
          this.$message.error(msg);
        }
      });
    },
    resetForm() {
      this.$refs.leaveForm.resetFields();
      this.leaveId = null;
      this.form.days = 0;
      // 补回用户信息
      if (this.login_user) {
        this.form.userId = this.login_user.id;
        this.form.userName =
          this.login_user.name ||
          this.login_user.username ||
          this.login_user.realName;
        // 补回部门（若登录信息包含部门）
        if (this.login_user.departmentId)
          this.form.departmentId = this.login_user.departmentId;
      }
    },

    onAttachmentUploadSuccess(res) {
      // 收集返回的 attachment id，后续在提交请假时一并发送给后端
      try {
        const att = res && res.data && res.data.attachment;
        if (att && att.id) {
          // 去重
          const id = att.id;
          if (!this.uploadedAttachmentIds.includes(id)) {
            this.uploadedAttachmentIds.push(id);
          }
        }
        // 如果上传返回了 leaveId（附件直接关联了请假单），优先使用之
        this.leaveId = (att && att.leaveId) || this.leaveId;
      } catch (e) {
        console.warn("处理上传返回值失败", e);
      }
    },

    async loadDrafts() {
      try {
        const res = await leaveApi.getDrafts(this.form.userId);
        const list = res.data || [];
        this.drafts = list.map((d) => ({
          ...d,
          id: d && d.id != null ? String(d.id) : d.id,
        }));
      } catch (err) {
        console.error("加载草稿失败:", err);
      }
    },

    editDraft(draft) {
      // 【核心修改】数据回显
      this.form = { ...draft };

      // 1. 回显请假时长
      // 如果后端存的是 duration，这里映射回 form.days
      if (draft.duration != null) {
        this.form.days = draft.duration;
      } else {
        // 如果旧数据没有 duration，重新计算
        this.calculateDuration();
      }

      this.leaveId = draft.id;

      // 回显部门
      if (draft.departmentId != null) {
        this.form.departmentId = draft.departmentId;
      }

      if (this.login_user && !this.form.userName) {
        this.form.userName =
          this.login_user.name ||
          this.login_user.username ||
          this.login_user.realName;
      }
    },

    // 从草稿提交
    async submitDraft(draft) {
      // 简单校验必填
      if (!this.form.deptManager || !this.form.adminUser) {
        this.$message.warning("请先编辑草稿补全审批人信息");
        return;
      }
      // 逻辑同 submitForm，这里简化处理，建议用户先点编辑再提交
      this.editDraft(draft);
      this.$message.info("已加载草稿，请核对信息后点击底部'提交申请'");
    },

    async deleteDraft(draft) {
      try {
        await this.$confirm("确认删除该草稿?", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });
        await leaveApi.deleteDraft(draft.id, { userId: this.form.userId });
        this.$message.success("删除成功");
        this.loadDrafts();
      } catch (err) {
        if (err !== "cancel") {
          this.$message.error("删除失败");
        }
      }
    },

    getLeaveTypeName(type) {
      const types = {
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
      return types[type] || type;
    },

    // 加载所有类型的审批人
    async loadAllApprovers() {
      this.approverLoading = true;
      try {
        const ROLE_DEPT_MANAGER = "ROLE_DEPT_MANAGER"; // 部门主管
        const ROLE_ADMIN_OFFICE = "ROLE_ADMIN_OFFICE"; // 行政办
        const ROLE_VICE_LEADER = "ROLE_VICE_LEADER"; // 分管领导
        const ROLE_MAIN_LEADER = "ROLE_MAIN_LEADER"; // 主要领导

        const toOption = (u) => ({
          value: u.id,
          label: u.nickname || u.name || u.username || `ID:${u.id}`,
        });

        // 并行向后端请求四类角色用户
        const [deptRes, adminRes, viceRes, mainRes] = await Promise.all([
          userApi.getUsersByRole(ROLE_DEPT_MANAGER),
          userApi.getUsersByRole(ROLE_ADMIN_OFFICE),
          userApi.getUsersByRole(ROLE_VICE_LEADER),
          userApi.getUsersByRole(ROLE_MAIN_LEADER),
        ]);

        const extractList = (resp) => {
          const data = resp?.data ?? resp;
          if (Array.isArray(data)) return data;
          if (Array.isArray(data?.records)) return data.records;
          if (Array.isArray(data?.list)) return data.list;
          return [];
        };

        const deptList = extractList(deptRes);
        const adminList = extractList(adminRes);
        const viceList = extractList(viceRes);
        const mainList = extractList(mainRes);

        this.deptManagerOptions = deptList.map(toOption);
        this.adminUserOptions = adminList.map(toOption);
        this.viceLeaderOptions = viceList.map(toOption);
        this.mainLeaderOptions = mainList.map(toOption);

        // 调试输出，便于确认后端角色接口返回
        console.log("[Leave] 角色人数:", {
          deptManager: deptList.length,
          adminOffice: adminList.length,
          viceLeader: viceList.length,
          mainLeader: mainList.length,
        });
      } catch (e) {
        console.error("加载审批人列表失败", e);
        this.deptManagerOptions = [];
        this.adminUserOptions = [];
        this.viceLeaderOptions = [];
        this.mainLeaderOptions = [];
      } finally {
        this.approverLoading = false;
      }
    },

    // 新增：加载部门列表
    async loadDepartments() {
      try {
        const res = await userApi.getDepartmentList();
        const list = (res && res.data) || [];
        if (Array.isArray(list)) {
          this.departments = list.map((d) => ({
            id: d.id || d.name,
            name: d.name || d.deptName || d.label,
          }));
          if (!this.form.departmentId && this.departments.length) {
            this.form.departmentId = this.departments[0].id;
          }
        }
      } catch (e) {
        console.warn("加载部门列表失败", e);
      }
    },
  },
};
</script>
