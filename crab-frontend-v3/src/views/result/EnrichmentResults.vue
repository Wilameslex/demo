<template>
  <div class="enrichment-results">
    <div class="header">
      <h1><i class="el-icon-s-data"></i> Enrichment Results</h1>
      <div class="sub-header">
        <el-tag :type="analysisType === 'GO' ? 'success' : analysisType === 'KEGG' ? 'info' : 'warning'">
          {{ analysisType }} Analysis
        </el-tag>
        <el-button
            icon="el-icon-back"
            size="small"
            @click="backToSearch"
        >
          Return Analysis
        </el-button>
      </div>
    </div>

    <el-card class="result-card">
      <!--加载状态-->
      <div v-if="loading" class="loading-container">
        <el-progress type="circle" :percentage="progress"></el-progress>
        <p>Results are generating, please wait</p>
        <p>Time has been used {{ elapsedTime }} seconds</p>
      </div>

      <div v-else>
        <div v-if="error" class="error-message">
          <el-alert
              :title="error"
              type="error"
              show-icon
              :closable="false">
            <el-button
                type="primary"
                size="small"
                @click="retryAnalysis">
              Back to re-analysis
            </el-button>
          </el-alert>
        </div>

        <div v-else>
          <div class="chart-controls">
<!--            <el-select v-model="chartType" size="small">-->
<!--              <el-option label="点图" value="dot"></el-option>-->
<!--              <el-option label="柱状图" value="bar"></el-option>-->
<!--              <el-option label="网络图" value="network"></el-option>-->
<!--            </el-select>-->

            <el-input-number
                v-model="categoryCount"
                :min="5"
                :max="30"
                size="small"
                label="Number of items"
            ></el-input-number>
          </div>

            <div class="result-header">
              <h2>Result Figure</h2>
              <div class="actions">
                <el-dropdown @command="handleExportChart">
                  <el-button type="primary" icon="el-icon-download">
                    Download<i class="el-icon-arrow-down el-icon--right"></i>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="png">PNG</el-dropdown-item>
                      <el-dropdown-item command="pdf">PDF</el-dropdown-item>
                      <el-dropdown-item command="svg">SVG</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>

          <div v-if="results && results.status === 'completed'">
            <h3>Result Table</h3>
            <div class="chart-container">
              <!-- 直接根据chartImage是否存在渲染 -->
              <img
                  :src="chartImage"
                  alt="Enrichment Result Table"
                  class="chart-image"
                  @error="handleImageError"
                  v-if="chartImage"
              />
              <div v-else class="no-chart">
                <i class="el-icon-picture-outline"></i>
                <p>no tables are generated, please check the backend</p>
              </div>
            </div>
          </div>

          <div class="result-header">
            <h2>Result Data</h2>
            <div class="actions">
              <el-dropdown @command="handleExport">
                <el-button type="primary">
                  Export the data<i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="csv">CSV</el-dropdown-item>
                    <el-dropdown-item command="excel">Excel</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <p class="data-tip">Tables can be download here</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';

export default {
  name: 'EnrichmentResults',
  data() {
    return {
      loading: false,
      progress: 0,
      progressInterval: null,
      elapsedTime: 0,
      timer: null,
      pollTimer: null,  // 统一轮询计时器变量名
      chartType: 'dot', // 图表类型默认值
      categoryCount: 10, // 展示条目数默认值（5-30之间）
      taskId: '' // 任务ID存储
    };
  },
  computed: {
    ...mapState('enrichment', [
      'results',
      'error',
      'analysisType'
    ]),

    chartImage() {
      // 关键：从results中直接读取chart_image（后端返回的字段名）
      const chartImage = this.results?.chart_image || '';
      console.log("Rendered chart URL:", chartImage); // 新增日志
      return chartImage;
    }
  },
  methods: {
    ...mapActions('enrichment', ['fetchResults']),

    backToSearch() {
      this.$router.push({ name: 'EnrichmentSearch' });
    },

    retryAnalysis() {
      try {
        this.$store.dispatch('enrichment/fetchResults');
        this.progress = 0;
        this.simulateProgress();
      } catch (error) {
        console.error('Retry fails:', error);
        this.$message.error(`Retry fails: ${error.message || '未知错误'}`);
      }
    },

    async handleExportChart(format) {
      if (!this.results?.chart_image) {
        this.$message.warning('no charts to download');
        return;
      }
      try {
        const response = await fetch(`/api/enrichment/images/${this.taskId}`);
        if (!response.ok) throw new Error('Download failed');
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `enrichment_chart_${this.taskId}.png`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
        this.$message.success('Chart downloaded successfully');
      } catch (error) {
        this.$message.error('Download failed: ' + error.message);
      }
    },

    // 导出数据
    handleExport(format) {
      if (!this.taskId) {
        this.$message.error('could not find task id');
        return;
      }

      this.$message.info(`${format.toUpperCase()} is exporting...`);
      const exportUrl = `/api/enrichment/export?taskId=${this.taskId}&format=${format}`;
      const link = document.createElement('a');
      link.href = exportUrl;
      link.download = `enrichment_${this.analysisType}_results_${this.taskId}.${format}`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      this.$api.enrichment.exportResults({
        taskId: this.taskId, // 关键：传递 taskId
        format: format,
        chartType: this.chartType
      }).then(response => {
        // 处理下载逻辑...
        const blob = new Blob([response.data], {
          type: format === 'csv' ? 'text/csv' : 'application/json'
        });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `enrichment_results_${this.taskId}.${format}`;
        a.click();
        URL.revokeObjectURL(url);
      }).catch(error => {
        this.$message.error('Export fails: ' + (error.message || '未知错误'));
      });
    },

    // 基础64转Blob
    b64toBlob(b64Data, contentType = '', sliceSize = 512) {
      const byteCharacters = atob(b64Data);
      const byteArrays = [];

      for (let offset = 0; offset < byteCharacters.length; offset += sliceSize) {
        const slice = byteCharacters.slice(offset, offset + sliceSize);
        const byteNumbers = new Array(slice.length);

        for (let i = 0; i < slice.length; i++) {
          byteNumbers[i] = slice.charCodeAt(i);
        }

        const byteArray = new Uint8Array(byteNumbers);
        byteArrays.push(byteArray);
      }

      return new Blob(byteArrays, { type: contentType });
    },

    simulateProgress() {
      this.progressInterval = setInterval(() => {
        if (this.progress < 90) {
          this.progress += 10;
        }
      }, 1000);
    },

    async fetchEnrichmentResults() {
      // 如果已经加载完成或出错，不再执行
      if (!this.loading && (this.results || this.error)) return;
      // 若没有 taskId，直接返回
      if (!this.taskId) {
        this.error = 'could not get task id';
        return;
      }

      this.loading = true;
      try {
        const response = await this.$api.enrichment.getEnrichmentResults({
          taskId: this.taskId,
          chartType: this.chartType,
          categoryCount: this.categoryCount
        });
        console.log("total response from the backend", response); // 新增：打印完整响应
        console.log("Charts base64:", response.chart_image?.length || 0); // 确认长度


        if (response.status === 'completed') {
          this.$store.commit('enrichment/setResults', response); // 假设你有这个mutation
          this.progress = 100;
          clearInterval(this.progressInterval);
          clearInterval(this.pollTimer);
        } else if (response.status === 'processing') {
          this.$message.info('Analysis is in progress, please wait...');
        } else if (response.status === 'error') {
          this.$store.commit('enrichment/setError', response.error || 'Analysis fails'); // 假设你有这个mutation
          clearInterval(this.progressInterval);
          clearInterval(this.pollTimer);
        }
      } catch (error) {
        this.$store.commit('enrichment/setError', 'Fail to get the result ' + (error.message || '未知错误'));
        clearInterval(this.progressInterval);
        clearInterval(this.pollTimer);
      } finally {
        this.loading = false;
      }
    },
    handleImageError() {
      this.error = 'Fail to load the chart，please check your documents';
      console.error('Fail to load the chart,URL:', this.chartImage);
    }
  },
  watch: {
    chartType(newVal) {
      // 修复方法名错误，使用正确的方法名
      this.fetchEnrichmentResults();
    },
    categoryCount() {
      // 增加条目数变化时重新获取图表
      this.fetchEnrichmentResults();
    },
    // 监听 taskId 变化，若为空则停止轮询
    taskId(newVal) {
      if (!newVal) {
        clearInterval(this.pollTimer);
      }
    }
  },
  created() {
    this.simulateProgress();

    // 添加调试信息
    const taskId = this.$route.query.taskId || localStorage.getItem('enrichmentTaskId');
    if (!taskId) {
      this.$message.error('Could not find task id');
      this.$router.push({ name: 'EnrichmentSearch' });
      return;
    }
    console.log("Enter the result, task id:", taskId);
    // 添加结果监听
    // 存储任务ID到本地
    this.taskId = taskId;
    console.log('Enter the result, task id:', this.taskId); // 确认控制台输出正确的ID

    if (!this.taskId) {
      this.error = 'Task is missing, please submit again';
      return;
    }

    // 初始化时立即获取一次结果
    this.fetchEnrichmentResults();

    // 启动轮询（使用正确的 taskId 判断）
    this.pollTimer = setInterval(() => {
      if (this.taskId && !this.loading && !this.results && !this.error) {
        this.fetchEnrichmentResults();
      }
    }, 5000);// 每5秒检查一次

    // 在created中添加计时器
    this.timer = setInterval(() => {
      this.elapsedTime++;
    }, 1000);
  },


  beforeUnmount() {
    clearInterval(this.progressInterval);
    clearInterval(this.pollTimer); // 清理轮询计时器
    clearInterval(this.timer);
  },

};
</script>

<style scoped>
.enrichment-results {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.sub-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.result-card {
  min-height: 500px;
}

.loading-container {
  text-align: center;
  padding: 50px 0;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-container {
  background-color: #f8f9fa;
  border: 1px solid #eaeaea;
  border-radius: 4px;
  padding: 20px;
  min-height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.chart-container img {
  max-width: 100%;
  max-height: 600px;
}

.no-chart {
  text-align: center;
  color: #909399;
}

.no-chart i {
  font-size: 60px;
  margin-bottom: 10px;
}

.data-tip {
  text-align: center;
  color: #909399;
  font-style: italic;
  margin: 30px 0;
}

.error-message {
  margin: 20px 0;
}
.actions {
  display: flex;
  gap: 10px;
}
</style>