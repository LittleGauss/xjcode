<template>
  <footer class="footer">
    <div class="footer-content">
      <div class="footer-links">
        <a href="/about-us" class="footer-link">关于我们</a>
        <a href="/contract-us" class="footer-link">联系我们</a>
        <a href="/help-center" class="footer-link">帮助中心</a>
        <a href="/privacy-policy" class="footer-link">隐私政策</a>
        <a href="/terms-of-use" class="footer-link">使用条款</a>
        <template v-if="rolesLoaded">
          <a
            href="#"
            class="footer-link"
            @click.prevent="goToOpLog"
            v-if="isAdmin()"
          >
            系统日志
          </a>
          <!-- 系统设置：仅管理员可见 + 跳转逻辑 -->
          <a
            href="#"
            class="footer-link"
            @click.prevent="goToUserManagement"
            v-if="isAdmin() || isUserAdmin()"
          >
            系统设置
          </a>
          <!-- 系统设置：仅管理员可见 + 跳转逻辑 -->
          <a
            href="#"
            class="footer-link"
            @click.prevent="goToNewsManagement"
            v-if="isAdmin()"
          >
            发布新闻
          </a>
        </template>
      </div>
      <div class="copyright">
        <p>
          新疆维吾尔自治区纤维质量监测中心（地址：乌鲁木齐市新市区河北路188号）
        </p>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: "FooterComponent",
  props: {
    userRoles: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      rolesLoaded: false, // 添加加载状态标识
    };
  },
  watch: {
    // 监听 userRoles 的变化，确保登录后加载数据
    userRoles: {
      immediate: true, // 组件初始化时也执行一次
      handler(newVal) {
        if (newVal && newVal.length > 0) {
          this.isAdmin();
          this.rolesLoaded = true;
        } else if (newVal == null || newVal == undefined) {
          // 数据未加载完成，保持 rolesLoaded 为 false
          this.rolesLoaded = false;
        }
      },
    },
  },
  methods: {
    // 与头部组件一致的管理员判断逻辑：检测是否有 SUPER_ADMIN 角色
    isAdmin() {
      return (
        this.userRoles &&
        this.userRoles.some((role) => role.code == "SUPER_ADMIN")
      );
    },
    isUserAdmin() {
      return (
        this.userRoles &&
        this.userRoles.some((role) => role.code == "USER_ADMIN")
      );
    },
    goToOpLog() {
      this.$emit("navigate", "oplog");
    },
    goToUserManagement() {
      this.$emit("navigate", "User");
    },
    goToNewsManagement() {
      this.$emit("navigate", "NewsManage");
    },
  },
};
</script>

<style scoped>
.footer {
  background: #2c3e50;
  color: white;
  padding: 30px 20px;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.footer-link {
  color: #bdc3c7;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-link:hover {
  color: white;
}

.copyright p {
  margin: 5px 0;
  color: #95a5a6;
  font-size: 0.9rem;
}
</style>
