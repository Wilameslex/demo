<template>
  <div class="admin-home">
    <el-card shadow="never">
      <h2>欢迎回来，{{ username }}！</h2>
      <div class="stats-container">
        <el-statistic title="基因数据总数" :value="12000" suffix="条" />
        <el-statistic title="通路数据总数" :value="320" suffix="条" />
        <el-statistic title="表达量样本数" :value="24" suffix="个" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 获取存储的用户名
const username = ref('')

onMounted(() => {
  username.value = localStorage.getItem('adminUsername') || '管理员'
})
</script>

<style scoped>
.admin-home {
  padding: 20px;
}
.stats-container {
  display: flex;
  gap: 20px;
  margin-top: 20px;
}
</style>