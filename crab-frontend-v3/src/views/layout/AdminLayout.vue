<template>
  <div class="admin-layout">
    <!-- 顶部导航栏 -->
    <el-header class="admin-header">
      <h2>中华绒螯蟹数据库-管理员后台</h2>
      <div class="user-info">
        <span>{{ adminUsername }}</span>
        <el-button size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>
    <div class="admin-main">
      <!-- AdminLayout.vue 侧边栏部分 -->
      <el-aside class="admin-aside">
        <el-menu :default-active="$route.path" router>
          <!-- index必须是字符串，且和路由path一致 -->
          <el-menu-item index="/admin/home">
            <el-icon><Home /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/admin/geneInfo-admin">
            <el-icon><Database /></el-icon>
            <span>基因数据管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <!-- 子页面渲染区域（AdminHome会在这里显示） -->
      <el-main class="admin-content">
        <router-view />
      </el-main>
    </div>
  </div>
</template>
<script setup>
import { Home, Database } from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
// 定义变量存储用户名
const adminUsername = ref('')

onMounted(() => {
  // 在setup中读取localStorage，再赋值给变量
  adminUsername.value = localStorage.getItem('adminUsername') || '管理员'
})

const handleLogout = () => {
  localStorage.removeItem('adminToken')
  localStorage.removeItem('adminUsername')
  router.push('/admin/login')
  ElMessage.success('已退出登录')
}
</script>
<style scoped>
/* 简单样式，可自定义 */
.admin-header { display: flex; justify-content: space-between; align-items: center; padding: 0 20px; background: #409EFF; color: #fff; }
.admin-main { display: flex; height: calc(100vh - 60px); }
.admin-aside { width: 200px; background: #f5f7fa; }
.admin-content { flex: 1; padding: 20px; overflow: auto; }
</style>