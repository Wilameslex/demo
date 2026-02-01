<template>
  <div class="enrichment-search">
    <h1>
      <el-icon><el-icon-s-operation /></el-icon> 富集分析
    </h1>
    <el-card class="search-card">
      <el-form label-position="top">
        <el-form-item label="输入基因列表">
          <el-input
            type="textarea"
            v-model="geneList"
            :rows="8"
            placeholder="每行输入一个基因ID，例如：
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
              <el-button size="small" type="primary">上传文件</el-button>
              <span class="upload-tip">(支持.txt, .csv格式)</span>
            </el-upload>
          </div>
        </el-form-item>

        <!-- 分析类型选择 -->
        <el-form-item label="分析类型">
          <el-radio-group v-model="analysisType">
            <el-radio label="GO">GO富集分析</el-radio>
            <el-radio label="KEGG">KEGG富集分析</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :icon="ElIconSPromotion"
            @click="submitAnalysis"
            :loading="loading"
          >
            开始分析
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="info-panel">
      <el-alert title="使用说明" type="info" :closable="false">
        <ul>
          <li>输入基因列表：每行一个基因ID，或上传文本文件</li>
          <li>GO富集分析：基因本体论富集分析</li>
          <li>KEGG富集分析：KEGG通路富集分析</li>
          <li>分析结果将包含图表和可下载的数据表格</li>
        </ul>
      </el-alert>
    </div>
    <el-card v-if="debugMode" class="debug-panel">
      <h3>调试信息</h3>
      <pre>{{ debugInfo }}</pre>
    </el-card>
  </div>
</template>

<script>
import {
  SOperation as ElIconSOperation,
  SPromotion as ElIconSPromotion,
} from '@element-plus/icons'
import { mapActions } from 'vuex'

export default {
  data() {
    return {
      geneList: '',
      analysisType: 'GO',
      loading: false,
      // 开发环境设为true
      debugMode: true,
      debugInfo: {},
      ElIconSPromotion,
    }
  },
  components: {
    ElIconSOperation,
  },
  name: 'EnrichmentSearch',
  methods: {
    ...mapActions('enrichment', ['runEnrichmentAnalysis']),

    //处理文件上传
    handleFileUpload(file) {
      const reader = new FileReader()
      reader.onload = (e) => {
        this.geneList = e.target.result
      }
      reader.readAsText(file.raw)
    },

    //提交分析
    async submitAnalysis() {
      if (!this.geneList.trim()) {
        this.$message.error('请输入基因列表')
        return
      }

      this.loading = true
      try {
        const genes = this.geneList
          .trim()
          .split('\n')
          .map((gene) => gene.trim())
          .filter((gene) => gene)

        const params = {
          genes,
          analysisType: this.analysisType,
        }

        console.log('提交分析参数:', params)

        // 直接调用 API 而不是通过 Vuex
        const response = await this.$api.enrichment.runEnrichmentAnalysis(
          params
        )

        if (response && response.task_id) {
          console.log('分析任务提交成功，任务ID:', response.task_id)

          // 存储任务ID到本地存储
          localStorage.setItem('enrichmentTaskId', response.task_id)

          // 跳转到结果页面
          this.$router.push({
            name: 'EnrichmentResults',
            query: { taskId: response.task_id },
          })
        } else {
          this.$message.error('分析提交失败: 未获取到任务ID')
        }

        this.debugInfo = {
          params,
          response,
          error: null,
        }
      } catch (error) {
        console.error('分析出错:', error)
        let errorMessage = '服务器错误'

        if (error.response) {
          errorMessage =
            error.response.data?.message || error.response.statusText
        } else if (error.message) {
          errorMessage = error.message
        }

        this.$message.error(`分析失败: ${errorMessage}`)
      } finally {
        this.loading = false
      }
    },

    resetForm() {
      this.geneList = ''
      this.analysisType = 'GO'
    },
  },
}
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
