<template>
  <div class="gene-network-search-container">
    <div class="header">
      <h1><i class="el-icon-search"></i> Gene Network Search</h1>
      <p class="sub-title">查询目标基因在特定网络中的Top20关联基因</p>
    </div>

    <el-card class="search-card" shadow="hover">
      <el-form label-position="top">
        <!-- 网络类型选择（metamorphosis/maturity） -->
        <el-form-item label="Biological Process" required>
          <el-select
              v-model="form.networkType"
              placeholder="select biological process"
              class="full-width"
          >
            <el-option label="Metamorphosis Network" value="metamorphosis"></el-option>
            <el-option label="Maturity Network" value="maturity"></el-option>
          </el-select>
        </el-form-item>

        <!-- 目标基因输入 -->
        <el-form-item label="Target Gene ID" required>
          <el-input
              v-model="form.targetGeneId"
              placeholder="Please input your target gene id（eg.LOC127000532）"
              class="full-width"
          ></el-input>
          <div class="example-link">
            <el-link type="info" @click="fillExample">
              <i class="el-icon-info"></i> fill the example gene
            </el-link>
          </div>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              icon="el-icon-search"
              @click="handleSearch"
              :loading="loading"
          >
            Start Search
          </el-button>
          <el-button @click="resetForm">Reset</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { searchGeneNetwork } from '@/api/geneNetwork';

export default {
  name: 'GeneNetworkSearch',
  data() {
    return {
      loading: false,
      form: {
        networkType: '', // 网络类型：metamorphosis/maturity
        targetGeneId: '' // 目标基因ID
      }
    };
  },
  methods: {
    fillExample() {
      this.form.targetGeneId = 'LOC127000532';
      this.$message.success('Example gene has been filled');
    },
    async handleSearch() {
      // 表单校验
      if (!this.form.networkType) {
        return this.$message.warning('Please select a biological process');
      }
      if (!this.form.targetGeneId.trim()) {
        return this.$message.warning('Please input your target gene id');
      }

      this.loading = true;
      try {
        // 构建查询参数
        const queryParams = {
          networkType: this.form.networkType,
          targetGeneId: this.form.targetGeneId.trim()
        };
        // 保存参数到sessionStorage，供结果页使用
        sessionStorage.setItem('geneNetworkQuery', JSON.stringify(queryParams));

        // 4. 调用接口（适配request.js逻辑：成功返回data，失败抛出错误）
        await searchGeneNetwork(queryParams); // 无需接收返回值，只要不报错即成功

        // 5. 跳转结果页（携带参数，确保结果页能获取）
        this.$router.push({
          name: 'GeneNetworkResults',
          query: queryParams // 路由参数，供结果页初始化
        });
      } catch (error) {
        this.$message.error('Search fails：' + (error.message || '未知错误'));
      } finally {
        this.loading = false;
      }
    },
    resetForm() {
      this.form = { networkType: '', targetGeneId: '' };
      this.$message.success('The form has been reset successfully');
    }
  }
};
</script>

<style scoped>
.gene-network-search-container {
  max-width: 700px;
  margin: 0 auto;
  padding: 20px;
}
.header {
  text-align: center;
  margin-bottom: 30px;
}
.header h1 {
  color: #303133;
  font-size: 28px;
  margin-bottom: 10px;
}
.sub-title {
  color: #909399;
  font-size: 14px;
}
.search-card {
  padding: 20px;
  border-radius: 8px;
}
.full-width {
  width: 100%;
}
.example-link {
  margin-top: 8px;
  text-align: right;
}
</style>