<template>
  <div class="simple-enrichment">
    <h1>Enrichment Analysis</h1>

    <div>
      <h3>Please input the gene list (each gene id per line):</h3>
      <textarea
          v-model="geneList"
          rows="10"
          style="width: 100%; padding: 10px;"
          placeholder="eg：&#10;LOC126982165&#10;LOC126986157&#10;LOC127008625"
      ></textarea>
    </div>

    <div style="margin: 20px 0;">
      <h3>Analyzing type:</h3>
      <label>
        <input type="radio" v-model="analysisType" value="GO"> GO Enrichment
      </label>
      <label style="margin-left: 20px;">
        <input type="radio" v-model="analysisType" value="KEGG"> KEGG Enrichment
      </label>
    </div>

    <div>
      <button
          @click="submitAnalysis"
          :disabled="loading"
          style="padding: 10px 20px; background: #409eff; color: white; border: none;"
      >
        {{ loading ? 'Analyzing...' : 'Start Analysis' }}
      </button>
      <button
          @click="resetForm"
          style="margin-left: 10px; padding: 10px 20px;"
      >
        Reset
      </button>
    </div>

    <div v-if="error" style="color: red; margin-top: 20px;">
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
      error: null
    };
  },
  methods: {
    async submitAnalysis() {
      if (!this.geneList.trim()) {
        this.error = 'Please input the gene list';
        return;
      }

      this.loading = true;
      this.error = null;

      try {
        const genes = this.geneList.trim().split('\n')
            .map(gene => gene.trim())
            .filter(gene => gene);

        const response = await this.$api.enrichment.runEnrichmentAnalysis({
          genes,
          analysisType: this.analysisType
        });

        if (response && response.task_id) {
          localStorage.setItem('enrichmentTaskId', response.task_id);
          this.$router.push({
            name: 'EnrichmentResults',
            query: { taskId: response.task_id }
          });
        } else {
          this.error = 'Analysis fails: miss the task id';
        }
      } catch (err) {
        this.error = `Analysis fails: ${err.message || 'Server error'}`;
      } finally {
        this.loading = false;
      }
    },

    resetForm() {
      this.geneList = '';
      this.analysisType = 'GO';
      this.error = null;
    }
  }
};
</script>

<style scoped>
.simple-enrichment {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
</style>