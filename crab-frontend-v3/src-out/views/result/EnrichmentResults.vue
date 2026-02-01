<template>
  <div class="enrichment-results">
    <div class="header">
      <h1>
        <el-icon><el-icon-s-data /></el-icon> 富集分析结果
      </h1>
      <div class="sub-header">
        <el-tag :type="analysisType === 'GO' ? 'success' : 'warning'">
          {{ analysisType }} 分析
        </el-tag>
        <el-button :icon="ElIconBack" size="small" @click="backToSearch">
          返回分析
        </el-button>
      </div>
    </div>

    <el-card class="result-card">
      <!--加载状态-->
      <div v-if="loading" class="loading-container">
        <el-progress type="circle" :percentage="progress"></el-progress>
        <p>正在生成分析结果，请稍候...</p>
        <p>已用时: {{ elapsedTime }} 秒</p>
      </div>

      <div v-else>
        <div v-if="error" class="error-message">
          <el-alert :title="error" type="error" show-icon :closable="false">
            <el-button type="primary" size="small" @click="retryAnalysis">
              返回重新分析
            </el-button>
          </el-alert>
        </div>

        <div v-else>
          <div class="chart-controls">
            <el-select v-model="chartType" size="small">
              <el-option label="点图" value="dot"></el-option>
              <el-option label="柱状图" value="bar"></el-option>
              <el-option label="网络图" value="network"></el-option>
            </el-select>

            <el-input-number
              v-model="categoryCount"
              :min="5"
              :max="30"
              size="small"
              label="展示条目数"
            ></el-input-number>
          </div>

          <div class="result-header">
            <h2>富集分析图表</h2>
            <div class="actions">
              <el-dropdown @command="handleExportChart">
                <el-button type="primary" :icon="ElIconDownload">
                  下载图表<el-icon class="el-icon--right"
                    ><el-icon-arrow-down
                  /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="png">PNG格式</el-dropdown-item>
                    <el-dropdown-item command="pdf">PDF格式</el-dropdown-item>
                    <el-dropdown-item command="svg">SVG格式</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <div class="chart-container">
            <img :src="chartImage" alt="富集分析图表" v-if="chartImage" />
            <div v-else class="no-chart">
              <el-icon><el-icon-picture-outline /></el-icon>
              <p>未生成图表</p>
            </div>
          </div>

          <div class="result-header">
            <h2>分析结果数据</h2>
            <div class="actions">
              <el-dropdown @command="handleExport">
                <el-button type="primary">
                  导出数据<el-icon class="el-icon--right"
                    ><el-icon-arrow-down
                  /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="csv">CSV格式</el-dropdown-item>
                    <el-dropdown-item command="excel"
                      >Excel格式</el-dropdown-item
                    >
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <p class="data-tip">表格数据仅供下载，不在此展示</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import {
  SData as ElIconSData,
  ArrowDown as ElIconArrowDown,
  PictureOutline as ElIconPictureOutline,
  Back as ElIconBack,
  Download as ElIconDownload,
} from '@element-plus/icons'
import { mapState, mapActions } from 'vuex'

export default {
  data() {
    return {
      progress: 0,
      progressInterval: null,
      elapsedTime: 0,
      timer: null,
      ElIconBack,
      ElIconDownload,
    }
  },
  components: {
    ElIconSData,
    ElIconArrowDown,
    ElIconPictureOutline,
  },
  name: 'EnrichmentResults',
  computed: {
    ...mapState('enrichment', ['loading', 'results', 'error', 'analysisType']),

    chartImage() {
      return this.results?.chartImage || ''
    },
  },
  methods: {
    ...mapActions('enrichment', ['fetchResults']),

    backToSearch() {
      this.$router.push({ name: 'EnrichmentSearch' })
    },

    retryAnalysis() {
      try {
        this.$store.dispatch('enrichment/fetchResults')
        this.progress = 0
        this.simulateProgress()
      } catch (error) {
        console.error('重试失败:', error)
        this.$message.error(`重试失败: ${error.message || '未知错误'}`)
      }
    },

    handleExportChart(format) {
      this.$message.info(`正在导出${format.toUpperCase()}格式数据...`)
      if (!this.chartImage) {
        this.$message.warning('没有可下载的图表')
        return
      }

      // 获取基础64编码的数据部分
      const base64Data = this.chartImage.replace(/^data:image\/\w+;base64,/, '')
      const blob = this.b64toBlob(base64Data, `image/${format}`)
      const url = URL.createObjectURL(blob)

      const link = document.createElement('a')
      link.href = url
      link.download = `enrichment_${
        this.analysisType
      }_${new Date().getTime()}.${format}`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    },

    // 导出数据
    handleExportData(format) {
      this.$message.info(`正在导出${format.toUpperCase()}格式数据...`)

      // 获取当前任务ID
      const taskId = this.$store.state.enrichment.analysisParams?.taskId

      if (!taskId) {
        this.$message.error('无法获取分析任务ID')
        return
      }

      // 构建导出URL
      const exportUrl = `${this.$api.defaults.baseURL}/api/enrichment/export?taskId=${taskId}&format=${format}`

      const link = document.createElement('a')
      link.href = exportUrl
      link.download = `enrichment_${this.analysisType}_results.${format}`
      link.style.display = 'none'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    },

    // 基础64转Blob
    b64toBlob(b64Data, contentType = '', sliceSize = 512) {
      const byteCharacters = atob(b64Data)
      const byteArrays = []

      for (
        let offset = 0;
        offset < byteCharacters.length;
        offset += sliceSize
      ) {
        const slice = byteCharacters.slice(offset, offset + sliceSize)
        const byteNumbers = new Array(slice.length)

        for (let i = 0; i < slice.length; i++) {
          byteNumbers[i] = slice.charCodeAt(i)
        }

        const byteArray = new Uint8Array(byteNumbers)
        byteArrays.push(byteArray)
      }

      return new Blob(byteArrays, { type: contentType })
    },

    simulateProgress() {
      this.progressInterval = setInterval(() => {
        if (this.progress < 90) {
          this.progress += 10
        }
      }, 1000)
    },

    async fetchResults() {
      this.loading = true
      try {
        const response = await this.$api.enrichment.getEnrichmentResults({
          taskId: this.taskId,
        })

        // 处理结果数据
        if (response.status === 'completed') {
          this.results = {
            chartImage: response.chart_image,
            data: response.results,
          }
          this.progress = 100
        } else if (response.status === 'processing') {
          this.$message.info('分析仍在进行中，请稍候...')
        } else if (response.status === 'error') {
          this.error = response.error || '分析失败'
        }
      } catch (error) {
        console.error('获取结果失败:', error)
        this.error = error.message || '获取结果失败'
      } finally {
        this.loading = false
      }
    },
  },
  created() {
    this.simulateProgress()

    // 添加调试信息
    const taskId =
      this.$route.query.taskId || localStorage.getItem('enrichmentTaskId')

    if (!taskId) {
      this.$message.error('未找到分析任务ID')
      this.$router.push({ name: 'EnrichmentSearch' })
      return
    }
    console.log('进入结果页面，任务ID:', taskId)
    // 添加结果监听
    // 存储任务ID到本地
    this.taskId = taskId

    // 开始获取结果
    this.fetchResults()

    // 添加轮询机制
    this.pollingInterval = setInterval(() => {
      if (!this.loading && !this.results && !this.error) {
        this.fetchResults()
      }
    }, 5000) // 每5秒检查一次

    // 在created中添加计时器
    this.timer = setInterval(() => {
      this.elapsedTime++
    }, 1000)
  },
  beforeUnmount() {
    clearInterval(this.progressInterval)
    clearInterval(this.pollingInterval) // 清理轮询
    clearInterval(this.timer)
  },
}
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
