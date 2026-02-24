/*
 * @Description:
 * @Date: 2022-04-20 09:18:07
 * @LastEditTime: 2022-05-09 13:40:27
 */
const mixin = {
  data: function () {
    return {
      // 查询的数据
      searchData: {},
      // 添加 编辑 查看的数据
      formData: {},
      // 弹框的标题
      dialogTitle: "新增",
      // 弹框点击确认的提交地址
      dialogUrl: "",
      // 请求的类型
      requestType: "",
      // 控制对话框的显示和隐藏
      dialogFormVisible: false,
      // 提交按钮的loding
      submitLoding: false,
      // 表格数据
      tableData: [],
      // 表格loading
      loading: false,
      // 表格选中的数据
      multipleSelection: [],
      // 表单label的宽度
      formLabelWidth: "120px",
      // 表单对齐方式
      labelPosition: "right",
      // 分页的数据
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: null,
      },
      paginationOne: {
        pageNum: 1,
        pageSize: 10,
        total: null,
      },
      paginationTwo: {
        pageNum: 1,
        pageSize: 5,
        total: null,
      },
      cityList: [],
      // 删除弹框
      delCenterDialogVisible: false,
      // 新增弹框
      addCenterDialogVisible: false,
      // 编辑弹框
      viewCenterDialogVisible: false,
      // 审核流程图弹框
      examineCenterDialogVisible: false,
    };
  },
  methods: {
    // 搜索重置
    resetSearch() {
      this.searchData = {};
      (this.pagination.pageNum = 1), (this.pagination.pageSize = 10);
      this.search();
    },
    // 重置表单
    resetFrom() {
      this.$refs.form && this.$refs.form.resetFields();
      this.formData = {};
    },
    // 关闭弹框
    onCancel() {
      this.dialogFormVisible = false;
    },

    // 多选框选中数据
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },

    handleSizeChange(val) {
      this.pagination.pageSize = val;
      this.search();
    },
    handleCurrentChange(val) {
      this.pagination.pageNum = val;
      this.search();
    },

    handleSizeChangeTwo(val) {
      this.paginationTwo.pageSize = val;
      this.searchStaff();
    },
    handleCurrentChangeTwo(val) {
      this.paginationTwo.pageNum = val;
      this.searchStaff();
    },
    add() {
      this.addCenterDialogVisible = true;
    },
    del() {
      this.delCenterDialogVisible = true;
    },
    view() {
      this.viewCenterDialogVisible = true;
    },
  },
};
export default mixin;
