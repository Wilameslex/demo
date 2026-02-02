<template>
  <div class="process-expression-search-container">
    <div class="header">
      <h1><i class="el-icon-search"></i> Biological Process Expression</h1>
      <p class="sub-title">Search the overall expression of certain biological process (eg.Metamorphosis)</p>
    </div>

    <el-card class="search-card" shadow="hover">
      <el-form label-position="top" :model="form" ref="searchForm">
        <!-- 1. 生物学过程选择（如metamorphosis/maturity/molt） -->
        <el-form-item label="Biological process" prop="process" required>
          <el-select v-model="form.process" placeholder="Select target process" class="full-width">
            <el-option label="Metamorphosis" value="metamorphosis"></el-option>
            <el-option label="2year Maturity" value="maturity"></el-option>
            <el-option label="Molt" value="molt"></el-option>
            <el-option label="Carcinization" value="carcinization"></el-option>
            <el-option label="Premature" value="premature"></el-option>
            <el-option label="Out water" value="outwater"></el-option>
            <el-option label="Photoperiods Change" value="photoperiodschange"></el-option>
            <el-option label="Limb Regeneration" value="relimb"></el-option>
          </el-select>
        </el-form-item>

        <!-- 2. 分析方法选择（复用原有逻辑） -->
        <el-form-item label="Pipeline" prop="pipeline" required>
          <el-radio-group v-model="form.pipeline">
            <el-radio-button label="stringtie">STAR+Stringtie</el-radio-button>
            <el-radio-button label="rsem">STAR+RSEM</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 3. 搜索类型（基因/转录本，复用原有逻辑） -->
        <el-form-item label="search type" prop="searchType" required>
          <el-radio-group v-model="form.searchType">
            <el-radio label="gene">Gene</el-radio>
            <el-radio label="transcript">Transcript</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item class="action-buttons">
          <el-button
              type="primary"
              icon="el-icon-search"
              @click="handleSearch"
              :loading="loading"
          >
            Start analyze
          </el-button>
          <el-button icon="el-icon-refresh" @click="resetForm">Reset</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'ProcessExpressionSearch',
  data() {
    return {
      loading: false,
      form: {
        process: 'metamorphosis', // 默认变态发育
        pipeline: 'stringtie',   // 默认分析方法
        searchType: 'gene'      // 默认基因水平
      }
    };
  },
  methods: {
    // 重置表单
    resetForm() {
      this.form = {
        process: 'metamorphosis',
        pipeline: 'stringtie',
        searchType: 'gene'
      };
      this.$message.success('Reset search criteria successfully');
    },

    // 提交查询，跳转结果页
    async handleSearch() {
      this.loading = true;
      try {
        // 1. 保存参数到sessionStorage（供结果页使用）
        const queryParams = {
          process: this.form.process,
          pipeline: this.form.pipeline,
          searchType: this.form.searchType
        };
        sessionStorage.setItem('processExpressionQuery', JSON.stringify(queryParams));

        // 2. 跳转结果页
        this.$router.push({
          name: 'ProcessExpressionResults',
          query: queryParams
        });
      } catch (error) {
        this.$message.error('Search fails：' + (error.message || 'unknown errors'));
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
/* 复用原有表达搜索的样式，保持UI一致 */
.process-expression-search-container {
  max-width: 900px;
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
  margin-bottom: 20px;
  border-radius: 8px;
  padding: 20px;
}

.full-width {
  width: 100%;
}

.action-buttons {
  text-align: center;
  margin-top: 30px;
}

</style>
