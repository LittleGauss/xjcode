<template>
  <div class="task-monitor-container">
    <el-card>
      <el-button
        type="primary"
        icon="el-icon-plus"
        @click="$router.push('/inspection/initiate-task')"
      >
        发起新任务
      </el-button>
      <el-table :data="taskList" border stripe style="margin-top: 20px">
        <el-table-column
          prop="initiatorName"
          label="任务发起人"
          width="120"
          :show-overflow-tooltip="true"
        ></el-table-column>
        <el-table-column
          prop="taskTitle"
          label="任务标题"
          width="120"
          :show-overflow-tooltip="true"
        ></el-table-column>
        <!--- IN_PROGRESS/COMPLETED/CANCELLED/OVERDUE-->
        <el-table-column prop="status" label="任务状态" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status == 'IN_PROGRESS'" type="warning"
              >进行中</el-tag
            >
            <el-tag v-if="scope.row.status == 'COMPLETED'" type="success"
              >已完成</el-tag
            >
            <el-tag v-if="scope.row.status == 'CANCELLED'" type="danger"
              >已终止</el-tag
            >
            <el-tag v-if="scope.row.status == 'OVERDUE'" type="danger"
              >逾期终止</el-tag
            >
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止日期" width="160">
          <template slot-scope="scope">
            {{
              scope.row.deadline
                ? new Date(scope.row.deadline).toLocaleString()
                : "-"
            }}
          </template>
        </el-table-column>
        <el-table-column prop="overdueFlag" label="是否逾期" width="80">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.overdueFlag === 'YES'" type="danger"
              >已逾期</el-tag
            >
            <el-tag v-else type="success">未逾期</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="completionRate" label="完成率" width="70">
          <template slot-scope="scope">
            {{
              scope.row.completionRate ? scope.row.completionRate + "%" : "0%"
            }}
          </template>
        </el-table-column>
        <el-table-column
          prop="processStatus"
          label="检查模板"
          width="190"
          :show-overflow-tooltip="true"
        >
          <template slot-scope="scope">
            <el-button
              type="danger"
              size="mini"
              @click="downloadTemplate(scope.row)"
            >
              {{ getFileNameFromPath(scope.row.inspectionFormUrl) }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template slot-scope="scope">
            <div class="op-btn-group">
              <el-button
                type="primary"
                size="mini"
                @click="viewDetail(scope.row)"
                >详情</el-button
              >
              <el-button
                type="primary"
                size="mini"
                @click="adjustInspectors(scope.row)"
                v-if="scope.row.status === 'IN_PROGRESS'"
              >
                换人
              </el-button>
              <el-button
                type="primary"
                size="mini"
                @click="auditTasks(scope.row)"
                >审核</el-button
              >
              <el-button
                type="danger"
                size="mini"
                @click="terminateTask(scope.row)"
                v-if="scope.row.status === 'IN_PROGRESS'"
              >
                终止
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 审核弹窗 -->
    <el-dialog
      title="审核检查结果"
      :visible.sync="auditDialogVisible"
      :close-on-click-modal="true"
      width="800px"
    >
      <el-table :data="currentAssignments" border stripe>
        <el-table-column
          prop="inspectorId"
          label="检查员ID"
          width="100px"
        ></el-table-column>
        <el-table-column label="检查员姓名" width="120px"
          ><template slot-scope="scope">
            {{ getInspectorNickname(scope.row.inspectorId) }}
          </template></el-table-column
        >
        <el-table-column prop="taskStatus" label="状态" width="100px">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.taskStatus == 'PENDING'" type="danger"
              >待接收</el-tag
            >
            <el-tag v-if="scope.row.taskStatus == 'RECEIVED'" type="warning"
              >已接收</el-tag
            >
            <el-tag v-if="scope.row.taskStatus == 'SUBMIT'" type="warning"
              >已提交</el-tag
            >
            <el-tag v-if="scope.row.taskStatus == 'COMPLETED'" type="success"
              >已完成</el-tag
            >
            <el-tag v-if="scope.row.taskStatus == 'REDO'" type="danger"
              >重做中</el-tag
            >
            <el-tag v-if="scope.row.taskStatus == 'OVERDUE'" type="danger"
              >已逾期</el-tag
            >
            <el-tag v-if="scope.row.taskStatus == 'CANCELLED'" type="danger"
              >已终止</el-tag
            >
          </template>
        </el-table-column>
        <el-table-column prop="filledFormUrl" label="已提交文件" width="280">
          <template slot-scope="scope">
            <el-button
              type="danger"
              size="mini"
              @click="downloadFile(scope.row)"
              class="file-name-btn"
            >
              {{
                getFileNameFromPath(scope.row.filledFormUrl)
                  ? getFileNameFromPath(scope.row.filledFormUrl)
                  : "尚未上传"
              }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <!-- 当任务状态为已提交或已重提时，显示审核按钮 -->
            <template v-if="scope.row.taskStatus == 'SUBMIT'">
              <el-button type="success" size="mini" @click="passTask(scope.row)"
                >通过</el-button
              >
              <el-button
                type="danger"
                size="mini"
                @click="showRejectForm(scope.row)"
              >
                驳回
              </el-button>
            </template>
            <!-- 其他状态时，显示提示文本 -->
            <template v-else>
              <span style="color: #999; font-size: 12px"
                >尚未提交|已驳回|已终止，无法审核</span
              >
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 驳回原因表单 -->
      <el-form
        v-if="currentRejectAssignment"
        label-width="100px"
        style="margin-top: 20px"
      >
        <el-form-item label="驳回原因">
          <el-input
            type="textarea"
            v-model="rejectReason"
            placeholder="请输入驳回原因"
            rows="3"
          ></el-input>
        </el-form-item>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
        <el-button @click="currentRejectAssignment = null">取消</el-button>
      </el-form>
    </el-dialog>

    <!-- 调整人员弹窗 -->
    <el-dialog
      title="调整检查人员"
      :visible.sync="adjustDialogVisible"
      :close-on-click-modal="true"
      width="600px"
    >
      <el-form>
        <el-form-item label="新增检查员">
          <el-select
            v-model="addInspectors"
            multiple
            placeholder="选择新增检查员"
            style="width: 100%"
          >
            <el-option
              v-for="inspector in allInspectors"
              :key="inspector.id"
              :label="inspector.nickname"
              :value="inspector.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="移除检查员">
          <el-select
            v-model="removeInspectors"
            multiple
            placeholder="选择移除检查员"
            style="width: 100%"
          >
            <el-option
              v-for="item in currentAssignments"
              :key="item.inspectorId"
              :label="getInspectorNickname(item.inspectorId)"
              :value="item.inspectorId"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdjust">确认调整</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { inspectionApi, templateApi } from "@/api/inspection";
import { userApi } from "@/api/usermag";

export default {
  name: "TaskMonitor",
  data() {
    return {
      taskList: [],
      initiatorId: this.login_user.id, // 当前登录负责人ID
      // 审核弹窗相关
      auditDialogVisible: false,
      currentAssignments: [],
      currentRejectAssignment: null,
      rejectReason: "",
      // 调整人员弹窗相关
      adjustDialogVisible: false,
      currentTaskId: null,
      allInspectors: [],
      addInspectors: [],
      removeInspectors: [],
    };
  },
  props: ["login_user", "userRoles", "userPermissions"],
  created() {
    this.loadTaskList();
    this.loadAllInspectors();
  },
  methods: {
    // 加载负责人的任务列表
    async loadTaskList() {
      try {
        const res = await inspectionApi.getTaskListByInitiatorId(
          this.initiatorId
        );
        let tasks = res.data;

        // 并行处理所有任务的 completionRate 和 processStatus
        const updatedTasks = await Promise.all(
          tasks.map(async (task) => {
            try {
              const [rateRes, statusRes] = await Promise.all([
                inspectionApi.calculateCompletionRate(task.id),
                inspectionApi.getProcessStatus(task.id),
              ]);

              return {
                ...task,
                completionRate: rateRes.data,
                processStatus: statusRes.data,
              };
            } catch (err) {
              // 如果某个任务的数据获取失败，保留原数据并记录错误
              console.error(`获取任务 ${task.id} 的附加信息失败:`, err);
              return {
                ...task,
                completionRate: task.completionRate || 0,
                processStatus: task.processStatus || "",
              };
            }
          })
        );
        // 一次性更新整个 taskList，确保响应式更新
        this.taskList = updatedTasks;
      } catch (err) {
        this.$message.error("加载任务列表失败：" + err.message);
      }
    },
    // 加载所有检查员
    async loadAllInspectors() {
      try {
        const res = await userApi.getUsersByRole([
          "SUPERVISION",
          "SUPERVISION_MANAGER",
        ]);
        this.allInspectors = res.data;
      } catch (err) {
        this.$message.error("加载检查员列表失败：" + err.message);
      }
    },
    // 根据检查员ID获取昵称
    getInspectorNickname(inspectorId) {
      const inspector = this.allInspectors.find(
        (item) => item.id === inspectorId
      );
      // 如果找到则返回nickname，否则返回原姓名（兜底）
      return inspector
        ? inspector.nickname
        : this.currentAssignments.find(
            (item) => item.inspectorId === inspectorId
          )?.inspectorName || "未知人员";
    },

    // 查看任务详情
    viewDetail(row) {
      this.$router.push(`/inspection/task-detail/${row.id}`);
    },

    // 审核任务
    async auditTasks(row) {
      try {
        const res = await inspectionApi.getAssignmentsByTaskId(row.id);
        this.currentAssignments = res.data;
        this.auditDialogVisible = true;
      } catch (err) {
        this.$message.error("加载任务分配列表失败：" + err.message);
      }
    },

    // 审核通过
    async passTask(row) {
      try {
        await inspectionApi.auditTask(row.id, this.initiatorId, "PASS", "同意");
        this.$message.success("审核通过！");
        this.auditDialogVisible = false;
        this.loadTaskList();
      } catch (err) {
        this.$message.error("审核失败：" + err.message);
      }
    },

    // 显示驳回表单
    showRejectForm(row) {
      this.currentRejectAssignment = row;
    },

    // 确认驳回
    async confirmReject() {
      if (!this.rejectReason) {
        this.$message.warning("请输入驳回原因");
        return;
      }
      try {
        await inspectionApi.auditTask(
          this.currentRejectAssignment.id,
          this.initiatorId,
          "REJECT",
          this.rejectReason
        );
        this.$message.success("驳回成功！");
        this.auditDialogVisible = false;
        this.currentRejectAssignment = null;
        this.rejectReason = "";
        this.loadTaskList();
      } catch (err) {
        this.$message.error("驳回失败：" + err.message);
      }
    },

    // 调整人员
    async adjustInspectors(row) {
      this.currentTaskId = row.id;
      try {
        const res = await inspectionApi.getAssignmentsByTaskId(row.id);
        this.currentAssignments = res.data;
        this.adjustDialogVisible = true;
      } catch (err) {
        this.$message.error("加载任务分配列表失败：" + err.message);
      }
    },

    // 提交人员调整
    async submitAdjust() {
      try {
        await inspectionApi.adjustInspectors(
          this.currentTaskId,
          this.addInspectors,
          this.removeInspectors
        );
        this.$message.success("人员调整成功！");
        this.adjustDialogVisible = false;
        this.addInspectors = [];
        this.removeInspectors = [];
        this.loadTaskList();
      } catch (err) {
        this.$message.error("人员调整失败：" + err.message);
      }
    },

    // 终止任务
    async terminateTask(row) {
      this.$confirm("确定要终止该任务吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          try {
            await inspectionApi.terminateTask(
              row.id,
              "负责人手动终止",
              this.initiatorId
            );
            this.$message.success("任务终止成功！");
            this.loadTaskList();
          } catch (err) {
            this.$message.error("任务终止失败：" + err.message);
          }
        })
        .catch(() => {
          this.$message.info("已取消终止操作");
        });
    },
    // 下载模板
    async downloadTemplate(row) {
      try {
        const res = await templateApi.downloadTemplateWithPath(
          row.inspectionFormUrl
        );
        if (res.code == 200) {
          // 1. 获取预签名链接
          const downloadUrl = res.data;
          // 1. 通过 fetch 获取文件流（避免直接访问 MinIO 链接的签名问题）
          const response = await fetch(downloadUrl);
          console.log("response", response);
          if (!response.ok) {
            throw new Error("文件下载链接无效");
          }
          // 2. 转换为 Blob 流
          const blob = await response.blob();
          // 3. 手动创建下载链接，强制指定原始文件名
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement("a");
          a.href = url;
          a.download = this.getFileNameFromPath(row.inspectionFormUrl); // 文件名字
          a.click();
          // 4. 释放临时资源
          window.URL.revokeObjectURL(url);
        } else {
          this.$message.error(res.msg || "生成下载链接失败");
        }
      } catch (err) {
        this.$message.error("模板下载失败：" + err.message);
      }
    },
    // 下载模板
    async downloadFile(row) {
      try {
        if (row == null || row.filledFormUrl == null) {
          return;
        }
        const res = await templateApi.downloadTemplateWithPath(
          row.filledFormUrl
        );
        if (res.code == 200) {
          // 1. 获取预签名链接
          const downloadUrl = res.data;
          // 1. 通过 fetch 获取文件流（避免直接访问 MinIO 链接的签名问题）
          const response = await fetch(downloadUrl);
          console.log("response", response);
          if (!response.ok) {
            throw new Error("文件下载链接无效");
          }
          // 2. 转换为 Blob 流
          const blob = await response.blob();
          // 3. 手动创建下载链接，强制指定原始文件名
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement("a");
          a.href = url;
          a.download = this.getFileNameFromPath(row.filledFormUrl); // 文件名字
          a.click();
          // 4. 释放临时资源
          window.URL.revokeObjectURL(url);
        } else {
          this.$message.error(res.msg || "生成下载链接失败");
        }
      } catch (err) {
        this.$message.error("模板下载失败：" + err.message);
      }
    },
    getFileNameFromPath(filePath) {
      // 这里直接调用工具函数
      return inspectionApi.extractFileName(filePath);
    },
  },
};
</script>

<style scoped>
.task-monitor-container {
  padding: 20px;
}
/* 关键样式：覆盖表格单元格默认样式 */
:deep(.el-table .cell) {
  white-space: normal !important; /* 取消强制不换行 */
  word-wrap: break-word; /* 长单词/URL换行 */
  word-break: break-all; /* 兼容中英文换行 */
  line-height: 1.4; /* 优化行高，提升可读性 */
}

:deep(.op-btn-group) {
  flex-wrap: wrap; /* 按钮超出宽度自动换行 */
  justify-content: flex-start; /* 左对齐（可选：space-around 均匀分布/space-between 两端对齐） */
  width: 100%; /* 占满列宽 */
}

/* 可选：给按钮设置统一最小宽度，文字不同也对齐 */
:deep(.op-btn-group .el-button) {
  min-width: 80px; /* 统一按钮宽度，根据文字调整 */
}
</style>
