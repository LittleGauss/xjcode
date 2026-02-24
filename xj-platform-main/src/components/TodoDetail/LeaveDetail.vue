<template>
  <el-descriptions :column="1" border size="medium">
    <el-descriptions-item label="请假类型">{{
      formatLeaveType(data.leaveType)
    }}</el-descriptions-item>
    <el-descriptions-item label="开始时间">{{
      formatDate(data.startDate)
    }}</el-descriptions-item>
    <el-descriptions-item label="结束时间">{{
      formatDate(data.endDate)
    }}</el-descriptions-item>
    <el-descriptions-item label="请假时长"
      >{{ data.duration }} 天</el-descriptions-item
    >
    <el-descriptions-item label="请假原因">{{
      data.reason || "-"
    }}</el-descriptions-item>
    <el-descriptions-item label="当前状态">{{
      formatStatus(data.status)
    }}</el-descriptions-item>
    <el-descriptions-item label="申请人">{{
      data.userName || "未知"
    }}</el-descriptions-item>
    <el-descriptions-item label="申请部门">{{
      getDepartmentName(data.departmentId)
    }}</el-descriptions-item>
  </el-descriptions>
</template>

<script>
export default {
  name: "LeaveDetail",
  props: {
    data: {
      type: Object,
      required: true,
      default: () => ({}),
    },
  },
  data() {
    return {
      // 请假类型映射
      leaveTypeMap: {
        personal: "事假",
        sick: "病假",
        annual: "年假",
        marriage: "婚假",
        maternity: "产假",
      },
      // 状态映射
      statusMap: {
        SUBMITTED: "已提交",
        APPROVED: "已批准",
        REJECTED: "已拒绝",
        CANCELED: "已取消",
      },
      // 部门映射（实际项目中应从接口获取）
      departmentMap: {
        1: "技术研发部",
        2: "综合管理部",
        3: "市场部",
      },
    };
  },
  methods: {
    formatLeaveType(type) {
      return this.leaveTypeMap[type] || type;
    },
    formatStatus(status) {
      return this.statusMap[status] || status;
    },
    formatDate(dateStr) {
      if (!dateStr) return "-";
      try {
        return new Date(dateStr).toLocaleDateString("zh-CN");
      } catch {
        return dateStr;
      }
    },
    getDepartmentName(deptId) {
      return this.departmentMap[deptId] || deptId || "-";
    },
  },
};
</script>
