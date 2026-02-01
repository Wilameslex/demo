<template>
  <div class="simple-enrichment">
    <h1>富集分析</h1>

    <div>
      <h3>输入基因列表 (每行一个基因ID):</h3>
      <textarea
        v-model="geneList"
        rows="10"
        style="width: 100%; padding: 10px"
        placeholder="例如：&#10;LOC126982165&#10;LOC126986157&#10;LOC127008625"
      ></textarea>
    </div>

    <div style="margin: 20px 0">
      <h3>分析类型:</h3>
      <label>
        <input type="radio" v-model="analysisType" value="GO" /> GO富集分析
      </label>
      <label style="margin-left: 20px">
        <input type="radio" v-model="analysisType" value="KEGG" /> KEGG富集分析
      </label>
    </div>

    <div>
      <button
        @click="submitAnalysis"
        :disabled="loading"
        style="
          padding: 10px 20px;
          background: #409eff;
          color: white;
          border: none;
        "
      >
        {{ loading ? '分析中...' : '开始分析' }}
      </button>
      <button @click="resetForm" style="margin-left: 10px; padding: 10px 20px">
        重置
      </button>
    </div>

    <div v-if="error" style="color: red; margin-top: 20px">
      {{ error }}
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      geneList: '',
      analysisType: 'GO',
      loading: false,
      error: null,
    }
  },
  methods: {
    async submitAnalysis() {
      if (!this.geneList.trim()) {
        this.error = '请输入基因列表'
        return
      }

      this.loading = true
      this.error = null

      try {
        const genes = this.geneList
          .trim()
          .split('\n')
          .map((gene) => gene.trim())
          .filter((gene) => gene)

        const response = await this.$api.enrichment.runEnrichmentAnalysis({
          genes,
          analysisType: this.analysisType,
        })

        if (response && response.task_id) {
          localStorage.setItem('enrichmentTaskId', response.task_id)
          this.$router.push({
            name: 'EnrichmentResults',
            query: { taskId: response.task_id },
          })
        } else {
          this.error = '分析提交失败: 未获取到任务ID'
        }
      } catch (err) {
        this.error = `分析失败: ${err.message || '服务器错误'}`
      } finally {
        this.loading = false
      }
    },

    resetForm() {
      this.geneList = ''
      this.analysisType = 'GO'
      this.error = null
    },
  },
}
</script>

<style scoped>
.simple-enrichment {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
</style>
