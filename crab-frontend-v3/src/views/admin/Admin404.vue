<template>
  <div class="admin-404">
    <el-empty
        description="页面不存在（管理员模块）"
        image="https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a82d72cc76d7f11.png"
    >
      <el-button type="primary" @click="goHome">返回管理员首页</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
const goHome = () => {
  router.push('/admin/home')
}
</script>

<style scoped>
.admin-404 {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}
</style>