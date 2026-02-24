<template>
  <el-descriptions :column="1" border size="medium">
    <el-descriptions-item label="合同名称">{{
      data.contractName || "-"
    }}</el-descriptions-item>
    <el-descriptions-item label="申请人">{{
      data.applicantName || "-"
    }}</el-descriptions-item>
    <el-descriptions-item label="申请部门">{{
      data.department || "-"
    }}</el-descriptions-item>
    <el-descriptions-item label="合同类型">{{
      data.contractType || "-"
    }}</el-descriptions-item>
    <el-descriptions-item label="提交日期">
      {{ formatDate(data.submissionDate) }}
    </el-descriptions-item>
    <el-descriptions-item label="当前状态">{{
      data.status || "-"
    }}</el-descriptions-item>
    <el-descriptions-item label="审核意见">
      {{ data.reviewComments || "暂无意见" }}
    </el-descriptions-item>
  </el-descriptions>
</template>

<script>
export default {
  name: "ContractDetail",
  props: {
    data: {
      type: Object,
      required: true,
      default: () => ({}),
    },
  },
  methods: {
    formatDate(dateStr) {
      if (!dateStr) return "-";
      try {
        return new Date(dateStr).toLocaleDateString("zh-CN");
      } catch {
        return dateStr;
      }
    },
  },
};
</script>
