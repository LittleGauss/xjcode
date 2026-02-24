<template>
  <!-- 批量导入模态框 -->
  <div
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    v-if="show"
  >
    <div class="bg-white rounded-lg shadow-xl p-6 w-full max-w-md">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-lg font-semibold">批量导入物品</h3>
        <button class="text-gray-500 hover:text-gray-700" @click="handleClose">
          <i class="fas fa-times"></i>
        </button>
      </div>
      <div class="space-y-4">
        <!-- 模板下载 -->
        <div class="text-sm text-gray-600">
          <p>1. 下载导入模板，按格式填写数据：</p>
          <button
            class="text-blue-600 hover:text-blue-800 mt-1 inline-block"
            @click="downloadImportTemplate"
          >
            <i class="fas fa-download mr-1"></i> 下载Excel模板
          </button>
        </div>

        <!-- 上传文件 -->
        <div>
          <p class="text-sm font-medium text-gray-700 mb-1">
            2. 上传填写好的Excel文件：
          </p>
          <label
            class="flex flex-col items-center justify-center w-full h-32 border-2 border-dashed border-gray-300 rounded-lg cursor-pointer bg-gray-50 hover:bg-gray-100"
          >
            <div class="flex flex-col items-center justify-center pt-5 pb-6">
              <i class="fas fa-cloud-upload text-3xl text-gray-400 mb-2"></i>
              <p class="mb-1 text-sm text-gray-600">
                <span class="font-semibold">点击上传</span> 或拖放文件
              </p>
              <p class="text-xs text-gray-500">支持 .xlsx 格式（不超过10MB）</p>
            </div>
            <input
              class="hidden"
              type="file"
              accept=".xlsx"
              @change="handleFileUpload"
            />
          </label>
        </div>

        <!-- 上传文件信息 -->
        <div v-if="importFile" class="bg-gray-50 p-3 rounded-md">
          <p class="text-sm flex items-center">
            <i class="fas fa-file-excel text-green-500 mr-2"></i>
            {{ importFile.name }}
            <button
              class="ml-auto text-red-500 hover:text-red-700"
              @click="clearUploadFile"
            >
              <i class="fas fa-times"></i>
            </button>
          </p>
        </div>

        <!-- 解析结果预览 -->
        <div v-if="importPreviewData.length > 0" class="mt-2">
          <p class="text-sm font-medium text-gray-700 mb-1">
            3. 数据预览（共{{ importPreviewData.length }}条）：
          </p>
          <div
            class="max-h-40 overflow-y-auto border border-gray-200 rounded-md"
          >
            <table class="min-w-full divide-y divide-gray-200 text-sm">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-2 py-1 text-left">物品名称</th>
                  <th class="px-2 py-1 text-left">分类</th>
                  <th class="px-2 py-1 text-left">数量</th>
                  <th class="px-2 py-1 text-left">状态</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-100">
                <tr v-for="(item, index) in importPreviewData" :key="index">
                  <td class="px-2 py-1">{{ item.name || "缺失" }}</td>
                  <td class="px-2 py-1">{{ item.category || "缺失" }}</td>
                  <td class="px-2 py-1">{{ item.quantity || 0 }}</td>
                  <td class="px-2 py-1 text-green-600">有效</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 底部按钮 -->
        <div class="flex space-x-3 mt-4">
          <button
            class="flex-1 px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
            @click="handleClose"
          >
            取消
          </button>
          <button
            class="flex-1 px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-md"
            @click="handleSubmit"
            :disabled="!importFile || importPreviewData.length === 0"
          >
            确认导入
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "ImportModal",
  props: {
    show: {
      type: Boolean,
      default: false,
    },
    categoryIdMap: {
      type: Object,
      default: () => ({}),
    },
  },
  data() {
    return {
      importFile: null,
      importPreviewData: [],
    };
  },
  methods: {
    handleClose() {
      this.importFile = null;
      this.importPreviewData = [];
      this.$emit("close");
    },

    clearUploadFile() {
      this.importFile = null;
      this.importPreviewData = [];
    },

    async downloadImportTemplate() {
      try {
        const XLSX = await import("xlsx");
        const { saveAs } = await import("file-saver");

        const templateData = [
          [
            "物品名称*",
            "分类*",
            "型号规格",
            "品牌",
            "供货商",
            "单价(元)",
            "数量*",
            "预警值",
            "入库日期(yyyy-MM-dd)*",
            "单位",
            "过期日期",
          ],
          [
            "示例：笔记本电脑",
            "示例：电子耗材",
            "示例：联想小新Pro14",
            "示例：联想",
            "示例：京东自营",
            "示例：5999.00",
            "示例：10",
            "示例：2",
            "示例：2025-11-15",
            "示例：台",
            "示例：2026-11-15",
          ],
        ];

        const wb = XLSX.utils.book_new();
        const ws = XLSX.utils.aoa_to_sheet(templateData);
        XLSX.utils.book_append_sheet(wb, ws, "导入模板");
        ws["!cols"] = [
          { wch: 15 },
          { wch: 12 },
          { wch: 20 },
          { wch: 10 },
          { wch: 10 },
          { wch: 8 },
          { wch: 8 },
          { wch: 18 },
          { wch: 6 },
          { wch: 12 },
        ];

        const wbout = XLSX.write(wb, { bookType: "xlsx", type: "array" });
        saveAs(
          new Blob([wbout], { type: "application/octet-stream" }),
          "物品导入模板.xlsx"
        );
      } catch (error) {
        console.error("下载模板失败:", error);
        this.$message.error("下载导入模板失败");
      }
    },

    async handleFileUpload(event) {
      const file = event.target.files[0];
      if (!file) return;

      if (
        file.type !==
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" &&
        !file.name.endsWith(".xlsx")
      ) {
        this.$message.error("请上传 .xlsx 格式的Excel文件");
        return;
      }

      if (file.size > 10 * 1024 * 1024) {
        this.$message.error("文件大小不能超过10MB");
        return;
      }

      this.importFile = file;
      this.$message.info("正在解析文件，请稍候...");

      try {
        const XLSX = await import("xlsx");
        const fileReader = new FileReader();

        fileReader.onload = (e) => {
          try {
            const data = new Uint8Array(e.target.result);
            const wb = XLSX.read(data, { type: "array" });
            const wsName = wb.SheetNames[0];
            const ws = wb.Sheets[wsName];
            const jsonData = XLSX.utils.sheet_to_json(ws, {
              header: 1,
              skipRows: 1,
            });

            const parsedData = [];
            for (let i = 0; i < jsonData.length; i++) {
              const row = jsonData[i];
              if (row.length === 0) continue;

              try {
                const name = row[0]?.toString().trim() || "";
                if (!name) throw new Error("物品名称不能为空");

                const category = row[1]?.toString().trim() || "";
                if (!category) throw new Error("分类不能为空");

                let purchaseDateStr = row[7]?.toString().trim() || "";
                let purchaseDateObj;
                if (!isNaN(purchaseDateStr) && purchaseDateStr !== "") {
                  const excelDays = Number(purchaseDateStr);
                  const baseDate = new Date(1900, 0, 0);
                  purchaseDateObj = new Date(
                    baseDate.getTime() + (excelDays - 1) * 24 * 60 * 60 * 1000
                  );
                } else {
                  purchaseDateObj = new Date(purchaseDateStr);
                }

                if (isNaN(purchaseDateObj.getTime())) {
                  throw new Error(
                    "入库日期格式错误（支持yyyy-MM-dd或Excel数字日期）"
                  );
                }

                const year = purchaseDateObj.getFullYear();
                if (year < 1000 || year > 9999) {
                  throw new Error("入库日期超出范围（1000-01-01至9999-12-31）");
                }

                const purchaseDate = `${year}-${String(
                  purchaseDateObj.getMonth() + 1
                ).padStart(2, "0")}-${String(
                  purchaseDateObj.getDate()
                ).padStart(2, "0")}`;

                const quantity = row[5] ? Number(row[5]) : 0;
                if (quantity <= 0) throw new Error("数量必须大于0");

                let expireDate = null;
                const expireDateStr = row[9]?.toString().trim() || "";
                if (expireDateStr) {
                  let expireDateObj;
                  if (!isNaN(expireDateStr)) {
                    const excelDays = Number(expireDateStr);
                    const baseDate = new Date(1900, 0, 0);
                    expireDateObj = new Date(
                      baseDate.getTime() + (excelDays - 1) * 24 * 60 * 60 * 1000
                    );
                  } else {
                    expireDateObj = new Date(expireDateStr);
                  }
                  if (!isNaN(expireDateObj.getTime())) {
                    const expireYear = expireDateObj.getFullYear();
                    if (expireYear >= 1000 && expireYear <= 9999) {
                      expireDate = `${expireYear}-${String(
                        expireDateObj.getMonth() + 1
                      ).padStart(2, "0")}-${String(
                        expireDateObj.getDate()
                      ).padStart(2, "0")}`;
                    }
                  }
                }

                parsedData.push({
                  name: name,
                  category: category,
                  spec: row[2]?.toString().trim() || "",
                  brand: row[3]?.toString().trim() || "",
                  supplier: row[4]?.toString().trim() || "",
                  unitPrice: row[5] ? Number(row[5]) : 0.0,
                  quantity: quantity,
                  warningValue: row[6] ? Number(row[6]) : null,
                  purchaseDate: purchaseDate,
                  unit: row[8]?.toString().trim() || "个",
                  expireDate: expireDate,
                });
              } catch (error) {
                this.$message.warning(
                  `第 ${i + 2} 行数据无效：${error.message}，已跳过`
                );
              }
            }

            this.importPreviewData = parsedData;
            this.$message.success(`解析成功！共${parsedData.length}条有效数据`);
          } catch (error) {
            console.error("解析Excel失败:", error);
            this.$message.error("解析Excel文件失败，请检查文件格式");
          }
        };

        fileReader.readAsArrayBuffer(file);
      } catch (error) {
        console.error("加载Excel解析依赖失败:", error);
        this.$message.error("导入功能依赖加载失败，请刷新页面重试");
      }
    },

    handleSubmit() {
      this.$emit("submit", {
        previewData: this.importPreviewData,
        categoryIdMap: this.categoryIdMap,
      });
      this.handleClose();
    },
  },
};
</script>
