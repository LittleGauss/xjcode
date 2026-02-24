<template>
  <div class="oa-homepage">
    <!-- 头部组件 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @navigate="handleNavigate"
      @logout="handleLogout"
    />

    <div class="news-manage">
      <h2>通知管理</h2>

      <!-- 新增新闻表单 -->
      <div class="add-form">
        <el-form
          :model="newsForm"
          :rules="newsFormRules"
          ref="newsForm"
          label-width="80px"
          @submit.prevent="addNews"
        >
          <el-form-item label="标题" prop="title">
            <el-input v-model="newsForm.title"></el-input>
          </el-form-item>
          <el-form-item label="内容" prop="content">
            <el-input
              type="textarea"
              v-model="newsForm.content"
              rows="15"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="addNews">新增通知</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 新闻列表 -->
      <el-table :data="newsList" border style="width: 100%; margin-top: 20px">
        <el-table-column prop="id" label="ID"></el-table-column>
        <el-table-column prop="title" label="标题"></el-table-column>
        <el-table-column prop="createTime" label="创建时间"></el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button type="primary" @click="viewNews(scope.row)"
              >查看</el-button
            >
            <el-button type="danger" @click="deleteNews(scope.row.id)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <el-dialog
        :visible.sync="dialogVisible"
        :close-on-click-modal="true"
        title="通知详情"
        width="60%"
      >
        <div>
          <h3>{{ selectedNews.title }}</h3>
          <p><strong>发布时间:</strong> {{ selectedNews.createTime }}</p>
          <div v-html="selectedNews.content"></div>
        </div>
        <span slot="footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </el-dialog>
    </div>

    <!-- 使用通用页脚 -->
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>

<script>
import HeaderComponent from "@/components/HeaderComponent.vue";
import FooterComponent from "@/components/FooterComponent.vue";
import { removeToken, getUserToken, removeUserToken } from "@/utils/auth";
import { addNews, deleteNews, getAllNews } from "@/api/news";

export default {
  name: "NewsPage",
  components: {
    HeaderComponent,
    FooterComponent,
  },
  data() {
    return {
      login_user: null,
      userRoles: [],
      newsForm: {
        title: "",
        content: "",
      },
      newsList: [],
      dialogVisible: false,
      selectedNews: {},
      newsFormRules: {
        title: [
          { required: true, message: "请输入标题", trigger: "blur" },
          {
            min: 1,
            max: 100,
            message: "长度在 1 到 100 个字符",
            trigger: "blur",
          },
        ],
        content: [
          { required: true, message: "请输入内容", trigger: "blur" },
          {
            min: 1,
            max: 1000,
            message: "长度在 1 到 1000 个字符",
            trigger: "blur",
          },
        ],
      },
    };
  },
  mounted() {
    this.loadNewsList();
  },
  methods: {
    async loadNewsList() {
      const res = await getAllNews();
      this.newsList = res.data;
    },
    viewNews(news) {
      this.selectedNews = news;
      this.dialogVisible = true;
    },
    async addNews() {
      this.$refs.newsForm.validate(async (valid) => {
        if (valid) {
          await addNews(this.newsForm);
          this.$message.success("新增成功！");
          this.newsForm = { title: "", content: "" };
          this.loadNewsList();
        } else {
          this.$message.error("请填写必填项");
          return false;
        }
      });
    },
    async deleteNews(id) {
      await this.$confirm("确定删除该新闻吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      });
      await deleteNews(id);
      this.$message.success("删除成功！");
      this.loadNewsList();
    },
    handleNavigate(routeName) {
      // 处理头部组件的导航事件
      if (routeName === "AboutPage") {
        return; // 如果已经在关于我们页面，不进行跳转
      }
      this.$router.push({ name: routeName });
    },

    // 退出登录
    handleLogout() {
      // 1. 清除 Vuex 状态
      removeUserToken();
      this.login_user = null;
      removeToken();
      this.$router.push("/login");
    },
  },
  created() {
    // 从会话存储获取用户信息
    const storedUserInfo = getUserToken();
    if (storedUserInfo && storedUserInfo.user) {
      this.login_user = storedUserInfo.user;
      this.userRoles = storedUserInfo.roles;
    } else {
      this.$message.warning("您尚未登录，请先登录！");
      // 如果未登录，跳转到登录页面
      this.$router.push("/login");
    }
  },
};
</script>

<style scoped>
.oa-homepage {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.news-manage {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 15px 20px;
  width: 100%;
}

.about-container {
  padding: 0 16px;
}

.content-wrapper {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 24px;
}

/* 标题栏样式 */
.title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.title-text {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

/* 关于我们卡片样式 */
.about-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
}

.about-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.about-content {
  padding: 20px;
}

.organization-name {
  font-size: 18px;
  color: #333;
  text-align: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.about-text p {
  margin-bottom: 16px;
  line-height: 1.8;
  color: #555;
  text-align: justify;
  text-indent: 2em;
}

.about-text p:last-child {
  margin-bottom: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .about-main {
    padding: 16px 0;
  }

  .about-container {
    padding: 0 12px;
  }

  .content-wrapper {
    padding: 16px;
  }

  .title-text {
    font-size: 18px;
  }

  .organization-name {
    font-size: 16px;
  }

  .about-text p {
    text-indent: 1.5em;
    font-size: 14px;
  }

  .add-form {
    background: #f5f5f5;
    padding: 20px;
    border-radius: 5px;
    margin-bottom: 20px;
  }
}
</style>
