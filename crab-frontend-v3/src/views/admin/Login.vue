<template>
  <!-- 登录页面容器 -->
  <div class="login-container">
    <el-card shadow="hover" class="login-card">
      <h2 class="login-title">中华绒螯蟹数据库-管理员登录</h2>
      <!-- 登录表单 -->
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="80px">
        <el-form-item label="管理员账号" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入账号" clearable />
        </el-form-item>
        <el-form-item label="登录密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" class="login-btn">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request' // 复用你项目中已有的请求工具

// 路由实例
const router = useRouter()

// 表单相关
const loginFormRef = ref(null)
const loading = ref(false)
const loginForm = ref({
  username: '',
  password: ''
})
// 表单校验规则
const loginRules = ref({
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }]
})

// 登录请求逻辑
const handleLogin = async () => {
  // 先校验表单
  await loginFormRef.value.validate()
  loading.value = true
  try {
    // 调用后端登录接口
    const res = await request.post('/admin/login', loginForm.value)
    const { token, username } = res;

    // 存储Token和用户名到localStorage
    localStorage.setItem('adminToken', token)
    localStorage.setItem('adminUsername', username)

    ElMessage.success('登录成功！')
    // 跳转到管理员首页
    const redirectPath = router.currentRoute.value.query.redirect || '/admin/home'
    router.push(redirectPath)
  } catch (error) {
    // 捕获后端错误信息
    ElMessage.error(error.response?.data?.message || '登录失败，请检查账号密码')
  } finally {
    loading.value = false
  }
}

// 页面挂载时，自动聚焦账号输入框
onMounted(() => {
  loginFormRef.value?.$el?.querySelector('input').focus()
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}
.login-card {
  width: 400px;
  padding: 20px;
}
.login-title {
  text-align: center;
  color: #409eff;
  margin-bottom: 20px;
}
.login-btn {
  width: 100%;
}
</style>