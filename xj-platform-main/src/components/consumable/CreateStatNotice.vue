<template>
  <el-dialog
    title="创建领用统计公示"
    :visible.sync="dialogVisible"
    width="60%"
    :before-close="handleClose"
    :close-on-click-modal="false"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      @submit.native.prevent
    >
      <el-form-item label="部门名称" prop="deptId">
        <el-select
          v-model="form.deptId"
          placeholder="请选择部门"
          clearable
          filterable
          style="width: 100%"
        >
          <el-option
            v-for="dept in departmentList"
            :key="dept.id"
            :label="dept.name || dept.deptName"
            :value="dept.id"
          >
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="统计月份" required>
        <el-radio-group v-model="form.dateType" @change="handleDateTypeChange">
          <el-radio-button label="single">单月</el-radio-button>
          <el-radio-button label="range">跨月</el-radio-button>
        </el-radio-group>

        <div class="date-selection-container" v-if="form.dateType == 'single'">
          <el-form-item prop="singleMonth">
            <el-date-picker
              v-model="form.singleMonth"
              type="month"
              placeholder="选择月份"
              style="width: 100%"
              value-format="yyyy-MM"
            />
          </el-form-item>
        </div>

        <div class="date-range-container" v-else>
          <el-col :span="11">
            <el-form-item prop="startMonth">
              <el-date-picker
                v-model="form.startMonth"
                type="month"
                placeholder="开始月份"
                style="width: 100%"
                value-format="yyyy-MM"
                :picker-options="startMonthPickerOptions"
              />
            </el-form-item>
          </el-col>
          <el-col :span="2" class="text-center" style="text-align: center">
            至
          </el-col>
          <el-col :span="11">
            <el-form-item prop="endMonth">
              <el-date-picker
                v-model="form.endMonth"
                type="month"
                placeholder="结束月份"
                style="width: 100%"
                value-format="yyyy-MM"
                :picker-options="endMonthPickerOptions"
              />
            </el-form-item>
          </el-col>
        </div>
      </el-form-item>

      <el-form-item label="公示标题" prop="title">
        <el-input
          v-model="form.title"
          placeholder="请输入公示标题，不填则使用默认标题"
          show-word-limit
          maxlength="100"
        />
      </el-form-item>
    </el-form>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button
        type="primary"
        @click="handleCreate"
        :loading="loading"
        :disabled="!canSubmit"
      >
        确认创建
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { createStatisticalNotice } from "@/api/consumnotice";

export default {
  name: "CreateStatNotice",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    // 从父组件接收部门列表数据
    departments: {
      type: Array,
      default: () => [],
    },
    login_user: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      loading: false,
      form: {
        deptId: "",
        dateType: "single", // 单月single 或 跨月range
        singleMonth: "",
        startMonth: "",
        endMonth: "",
        title: "",
      },
      rules: {
        deptId: [{ required: true, message: "请选择部门", trigger: "change" }],
        singleMonth: [
          { required: true, message: "请选择月份", trigger: "change" },
        ],
        startMonth: [
          { required: true, message: "请选择开始月份", trigger: "change" },
        ],
        endMonth: [
          { required: true, message: "请选择结束月份", trigger: "change" },
          {
            validator: (rule, value, callback) => {
              if (!value) {
                callback(new Error("请选择结束月份"));
              } else if (
                this.form.startMonth &&
                new Date(value) < new Date(this.form.startMonth)
              ) {
                callback(new Error("结束月份不能早于开始月份"));
              } else if (
                this.form.startMonth &&
                this.isMoreThanOneYear(this.form.startMonth, value)
              ) {
                callback(new Error("跨月范围不能超过一年"));
              } else {
                callback();
              }
            },
            trigger: "change",
          },
        ],
      },
    };
  },
  // 在组件的 watch 中添加处理默认值的逻辑
  watch: {
    // 监听dialog显示状态，当显示时设置默认值
    visible: {
      handler(newVal) {
        if (newVal) {
          this.$nextTick(() => {
            // 设置默认部门为第一个
            if (this.departmentList.length > 0 && !this.form.deptId) {
              this.form.deptId = this.departmentList[0].id;
            }

            // 如果是首次打开且没有设置标题，则生成默认标题
            if (!this.form.title) {
              this.generateDefaultTitle();
            }
          });
        }
      },
      immediate: true,
    },
    // 监听部门变化，更新标题
    "form.deptId"() {
      this.updateTitleBasedOnDept();
    },
    // 监听日期变化，更新标题
    "form.dateType"() {
      this.generateDefaultTitle();
    },
    "form.singleMonth"() {
      if (this.form.dateType === "single") {
        this.generateDefaultTitle();
      }
    },
    "form.startMonth"() {
      if (this.form.dateType === "range") {
        this.generateDefaultTitle();
      }
    },
    "form.endMonth"() {
      if (this.form.dateType === "range") {
        this.generateDefaultTitle();
      }
    },
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible;
      },
      set(val) {
        this.$emit("update:visible", val);
      },
    },
    canSubmit() {
      if (!this.form.deptId) return false;

      if (this.form.dateType == "single") {
        return !!this.form.singleMonth;
      } else {
        return this.form.startMonth && this.form.endMonth;
      }
    },
    // 使用传递过来的部门列表
    departmentList() {
      return this.departments;
    },
    startMonthPickerOptions() {
      return {
        disabledDate: (time) => {
          if (this.form.endMonth) {
            return time.getTime() > new Date(this.form.endMonth).getTime();
          }
          return false;
        },
      };
    },
    endMonthPickerOptions() {
      return {
        disabledDate: (time) => {
          if (this.form.startMonth) {
            // 检查是否超过一年(12个月)
            const startDate = new Date(this.form.startMonth);
            const maxDate = new Date(
              startDate.getFullYear(),
              startDate.getMonth() + 12,
              0
            ); // 加12个月然后取上一个月的最后一天

            return (
              time.getTime() < new Date(this.form.startMonth).getTime() ||
              time.getTime() > maxDate.getTime()
            );
          }
          return false;
        },
      };
    },
  },
  methods: {
    handleClose() {
      this.$emit("update:visible", false);
      this.resetForm();
    },
    // 判断两个日期之间是否超过一年
    isMoreThanOneYear(startMonth, endMonth) {
      const startDate = new Date(startMonth);
      const endDate = new Date(endMonth);

      // 计算月份差
      const monthDiff =
        (endDate.getFullYear() - startDate.getFullYear()) * 12 +
        (endDate.getMonth() - startDate.getMonth());

      // 如果月份差大于11个月(即超过一年)，返回true
      return monthDiff > 11;
    },
    resetForm() {
      this.$refs.formRef && this.$refs.formRef.resetFields();
      this.form = {
        deptId: "",
        dateType: "single",
        singleMonth: "",
        startMonth: "",
        endMonth: "",
        title: "",
      };

      // 设置默认部门并生成默认标题
      this.$nextTick(() => {
        if (this.departmentList.length > 0) {
          this.form.deptId = this.departmentList[0].id;
          this.generateDefaultTitle();
        }
      });

      this.loading = false;
    },

    async handleCreate() {
      // 验证表单
      const valid = await this.$refs.formRef.validate().catch(() => false);
      if (!valid) {
        return;
      }

      this.loading = true;
      try {
        // 获取部门名称
        const selectedDept = this.departmentList.find(
          (dept) => dept.id == this.form.deptId
        );
        const deptName = selectedDept
          ? selectedDept.name || selectedDept.deptName
          : "";

        // 确保标题不为空
        if (!this.form.title) {
          if (this.form.dateType == "single") {
            this.form.title = `${deptName}领用统计公示(${this.form.singleMonth})`;
          } else {
            this.form.title = `${deptName}领用统计公示(${this.form.startMonth}至${this.form.endMonth})`;
          }
        }

        // 构造请求参数
        let params;
        if (this.form.dateType == "single") {
          // 单月模式，转换为该月的起止日期
          const [year, month] = this.form.singleMonth.split("-");
          const startDate = `${year}-${month}-01 00:00:00`;
          const endDate = `${year}-${month}-${this.getLastDayOfMonth(
            year,
            month
          )} 23:59:59`;

          params = {
            deptId: this.form.deptId,
            deptName: deptName,
            startDate: startDate,
            endDate: endDate,
            title: this.form.title,
            noticeType: "STAT", // 统计类型
            creatorId: this.login_user.id,
            creatorName: this.login_user.nickname,
          };
        } else {
          // 跨月模式，转换为所选月份的起止日期
          const [startYear, startMonth] = this.form.startMonth.split("-");
          const [endYear, endMonth] = this.form.endMonth.split("-");
          const startDate = `${startYear}-${startMonth}-01 00:00:00`;
          const endDate = `${endYear}-${endMonth}-${this.getLastDayOfMonth(
            endYear,
            endMonth
          )} 23:59:59`;

          params = {
            deptId: this.form.deptId,
            deptName: deptName,
            startDate: startDate,
            endDate: endDate,
            title: this.form.title,
            noticeType: "STAT", // 统计类型
            creatorId: this.login_user.id,
            creatorName: this.login_user.nickname,
          };
        }

        // 调用API创建统计公示
        const res = await createStatisticalNotice(params);

        if (res.code == 200) {
          this.$message.success(res.data);
          this.handleClose();
          this.$emit("created"); // 触发父组件刷新列表
        } else {
          this.$message.error(res.msg);
        }
      } catch (error) {
        this.$message.error(error.message);
      } finally {
        this.loading = false;
      }
    },

    // 获取指定年月的最后一天
    getLastDayOfMonth(year, month) {
      const lastDay = new Date(year, month, 0);
      return lastDay.getDate();
    },
    updateTitleBasedOnDept() {
      if (!this.form.title || this.form.title.startsWith("领用统计公示")) {
        this.generateDefaultTitle();
      }
    },

    // 生成默认标题
    generateDefaultTitle() {
      if (!this.form.deptId) return;

      // 获取部门名称
      const selectedDept = this.departmentList.find(
        (dept) => dept.id == this.form.deptId
      );
      const deptName = selectedDept
        ? selectedDept.name || selectedDept.deptName
        : "";

      let titleSuffix = "";

      if (this.form.dateType === "single" && this.form.singleMonth) {
        // 单月模式
        titleSuffix = this.form.singleMonth;
      } else if (
        this.form.dateType === "range" &&
        this.form.startMonth &&
        this.form.endMonth
      ) {
        // 跨月模式
        titleSuffix = `${this.form.startMonth}-${this.form.endMonth}`;
      }

      if (titleSuffix) {
        this.form.title = `${deptName}领用统计公示${titleSuffix}`;
      } else {
        // 如果还没有月份信息，使用默认格式
        this.form.title = `${deptName}领用统计公示`;
      }
    },

    // 日期类型切换处理
    handleDateTypeChange() {
      // 清空对应类型的日期值
      if (this.form.dateType == "single") {
        this.form.startMonth = "";
        this.form.endMonth = "";
      } else {
        this.form.singleMonth = "";
      }

      // 重新生成标题
      this.generateDefaultTitle();
    },
  },
};
</script>

<style scoped>
.text-center {
  text-align: center;
  line-height: 36px;
}
.dialog-footer {
  text-align: right;
}
.date-selection-container {
  margin-top: 10px;
}
.date-range-container {
  margin-top: 10px;
}
</style>
