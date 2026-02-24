<template>
  <el-descriptions :column="1" border size="medium">
    <el-descriptions-item label="物品名称">{{
      data.itemName || "-"
    }}</el-descriptions-item>
    <el-descriptions-item label="申请人">{{
      data.applicantName || "-"
    }}</el-descriptions-item>
    <el-descriptions-item label="申请部门">{{
      data.applicantDept || "-"
    }}</el-descriptions-item>
    <el-descriptions-item label="耗材类型">
      {{ formatConsumableType(data.consumableType) }}
    </el-descriptions-item>
    <el-descriptions-item label="申请数量">{{
      data.quantity || 0
    }}</el-descriptions-item>
    <el-descriptions-item label="用途">{{
      data.purpose || "-"
    }}</el-descriptions-item>
    <el-descriptions-item label="申请时间">
      {{ formatDateTime(data.applyTime) }}
    </el-descriptions-item>
    <el-descriptions-item label="当前状态">
      {{ formatStatus(data.finalStatus) }}
    </el-descriptions-item>
  </el-descriptions>
</template>

<script>
export default {
  name: "ConsumableDetail",
  props: {
    data: {
      type: Object,
      required: true,
      default: () => ({}),
    },
  },
  data() {
    return {
      consumableTypeMap: {
        general: "通用耗材",
        office: "办公耗材",
        technical: "技术耗材",
      },
      statusMap: {
        pending_first_approval: "待部门审批",
        pending_second_approval: "待行政审批",
        approved: "已批准",
        rejected: "已拒绝",
      },
    };
  },
  methods: {
    formatConsumableType(type) {
      return this.consumableTypeMap[type] || type;
    },
    formatStatus(status) {
      return this.statusMap[status] || status;
    },
    formatDateTime(dateStr) {
      if (!dateStr) return "-";
      try {
        const date = new Date(dateStr);
        return date.toLocaleString("zh-CN", {
          year: "numeric",
          month: "2-digit",
          day: "2-digit",
          hour: "2-digit",
          minute: "2-digit",
        });
      } catch {
        return dateStr;
      }
    },
  },
};
</script>
