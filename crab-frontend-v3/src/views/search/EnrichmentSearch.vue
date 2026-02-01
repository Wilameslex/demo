<template>
  <div class="enrichment-search">
    <h1><i class="el-icon-s-operation"></i> Enrichment Analysis</h1>
    <el-card class="search-card">
      <el-form label-position="top">
        <el-form-item label="Please input the your list of genes">
          <el-input
              type="textarea"
              v-model="geneList"
              :rows="8"
              placeholder="Each gene id per row, eg：
LOC126982165
LOC126986157
LOC127008625"
          ></el-input>
          <div class="upload-area">
            <el-upload
                action="#"
                :auto-upload="false"
                :on-change="handleFileUpload"
                :show-file-list="false"
            >
              <el-button size="small" type="primary">Upload your file</el-button>
              <span class="upload-tip">(allowed .txt, .csv)</span>
            </el-upload>
          </div>
        </el-form-item>

        <!-- 分析类型选择 -->
        <el-form-item label="分析类型">
          <el-radio-group v-model="analysisType">
            <el-radio label="GO">GO enrichment</el-radio>
            <el-radio label="KEGG">KEGG enrichment</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button
              type="primary"
              icon="el-icon-s-promotion"
              @click="submitAnalysis"
              :loading="loading"
          >
            Start Analysis
          </el-button>
          <el-button @click="resetForm">Reset</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="info-panel">
      <el-alert title="使用说明" type="info" :closable="false">
        <ul>
          <li>Please enter the gene list: One gene ID per line, or upload a text file</li>
          <li>GO enrichment analysis: Gene Ontology enrichment analysis</li>
          <li>KEGG enrichment analysis: KEGG pathway enrichment analysis</li>
          <li>The analysis results will include charts and downloadable data tables</li>
        </ul>
      </el-alert>
    </div>
    <el-card v-if="debugMode" class="debug-panel">
      <h3>Debug Information</h3>
      <pre>{{ debugInfo }}</pre>
    </el-card>
  </div>
</template>

<script>
import { mapActions } from 'vuex';

export default {
  name: 'EnrichmentSearch',
  data() {
    return {
      geneList: '',
      analysisType: 'GO',
      loading: false,
      debugMode: true, // 开发环境设为true
      debugInfo: {}
    };
  },
  methods: {
    ...mapActions('enrichment', ['runEnrichmentAnalysis']),

    //处理文件上传
    handleFileUpload(file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        this.geneList = e.target.result;
      };
      reader.readAsText(file.raw);
    },

    //提交分析
    async submitAnalysis() {
      if (!this.geneList.trim()) {
        this.$message.error('Please input the gene list');
        return;
      }

      this.loading = true;
      try {
        // 1. 处理基因列表（原有逻辑不变，保持）
        const genes = this.geneList.trim().split('\n')
            .map(gene => gene.trim())
            .filter(gene => gene);

        const params = {
          genes,
          analysisType: this.analysisType
        };
        console.log("Please submit analyzing params:", params);

        // 2. 调用 API（原有逻辑不变）
        const response = await this.$api.enrichment.runEnrichmentAnalysis(params);
        console.log("Backend response:", response); // 新增：打印完整响应，确认数据结构

        // 🔴 核心修复：从 response.data 中提取 task_id（axios 响应数据在 data 字段）
        const taskId = response?.data?.task_id; // 可选链操作，避免空指针
        console.log("Task id:", taskId); // 新增：验证 taskId 是否正确

        // 3. 验证 taskId 并处理
        if (taskId) {
          console.log("Analyzing has been submit, task id:", taskId);
          // 存储任务ID到本地存储
          localStorage.setItem('enrichmentTaskId', taskId);
          // 跳转到结果页面（query 参数传递 taskId）
          this.$router.push({
            name: 'EnrichmentResults',
            query: { taskId: taskId } // 传递正确的 taskId
          });
        } else {
          // 容错：明确提示响应结构问题，方便调试
          const errorMsg = response?.data
              ? 'Analysis submision is failed: no response of task_id'
              : 'Analysis submision is failed: no response from the backend';
          this.$message.error(errorMsg);
          // 打印响应详情，帮助定位后端问题
          console.error("Details of unobtained task_id:", response);
        }

        // 4. 调试信息：保留完整响应，方便排查
        this.debugInfo = {
          params,
          response: response, // 保留完整响应
          taskId: taskId, // 显示提取到的 taskId
          error: null
        };

      } catch (error) {
        console.error('Analysis error:', error);
        let errorMessage = 'errors from server';
        // 错误处理优化：更精准的错误提示
        if (error.response) {
          errorMessage = `Backend error: ${error.response.status} - ${error.response.data?.message || error.response.statusText}`;
        } else if (error.message) {
          errorMessage = `Internet error: ${error.message}`;
        }
        this.$message.error(`Analysis fails: ${errorMessage}`);
        this.debugInfo.error = errorMessage;
      } finally {
        this.loading = false;
      }
    },

    resetForm() {
      this.geneList = '';
      this.analysisType = 'GO';
    }
  }
};
</script>

<style scoped>
.enrichment-search {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.upload-area {
  margin-top: 10px;
}

.upload-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.info-panel {
  margin-top: 30px;
}

.info-panel ul {
  padding-left: 20px;
  margin: 10px 0;
}

.info-panel li {
  line-height: 1.8;
}
.debug-panel {
  margin-top: 20px;
  background-color: #f8f8f8;
  max-height: 300px;
  overflow: auto;
}
</style>