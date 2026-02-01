<template>
  <div class="expression-results-container">
    <div class="header">
      <h1>
        <el-icon><el-icon-collection /></el-icon> 基因表达搜索结果
      </h1>
      <div class="sub-header">
        <el-tag type="info">共 {{ expressionData.length }} 条记录</el-tag>
        <el-button :icon="ElIconBack" @click="backToSearch" size="small">
          返回搜索
        </el-button>
      </div>
    </div>
    <!-- 调试信息（开发环境显示） -->
    <div v-if="developmentMode" class="debug-section">
      <el-alert title="调试信息" type="info" :closable="false">
        <p>查询参数: {{ queryParams }}</p>
        <p>
          样本数: {{ selectedSamples.length }} | 基因数:
          {{ expressionData.length }}
        </p>
        <p>首行数据: {{ firstRow }}</p>
      </el-alert>
    </div>

    <!-- 样本选择器 -->
    <el-card shadow="never" class="filter-card">
      <div class="sample-selector">
        <div class="selector-header">
          <span class="selector-title">选择样本:</span>
          <el-button
            size="mini"
            @click="selectAllSamples"
            :disabled="selectedSamples.length === allSamples.length"
          >
            全选
          </el-button>
          <el-button
            size="mini"
            @click="clearSamples"
            :disabled="selectedSamples.length === 0"
          >
            清空
          </el-button>
        </div>
        <el-checkbox-group v-model="selectedSamples" class="sample-checkboxes">
          <el-checkbox
            v-for="sample in allSamples"
            :key="sample"
            :label="sample"
            class="sample-checkbox"
          >
            {{ sample }}
          </el-checkbox>
        </el-checkbox-group>
      </div>
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button
        type="primary"
        style="margin-bottom: 20px; margin-left: 100px"
        :icon="ElIconDownload elIcon-Right"
        @click="downloadTable('csv')"
      >
        下载CSV
      </el-button>
      <el-button
        type="success"
        :icon="ElIconDownload elIcon-Right"
        @click="downloadTable('excel')"
      >
        下载Excel
      </el-button>
    </div>

    <!-- 结果表格 -->
    <el-card shadow="never" class="results-card">
      <el-table
        :key="tableKey"
        :data="filteredExpressionData"
        v-loading="loading"
        stripe
        border
        highlight-current-row
        style="width: 100%"
        :height="tableHeight"
        :row-key="(row) => row.gene_id"
      >
        <!-- ID列 -->
        <el-table-column
          prop="gene_id"
          label="ID"
          width="150"
          fixed="left"
          align="center"
        >
          <template #default="{ row }">
            <el-tag type="success">{{ row.gene_id }}</el-tag>
          </template>
        </el-table-column>

        <!-- 动态样本列 -->
        <el-table-column
          v-for="(sample, index) in selectedSamples"
          :key="'col-' + index + '-' + sample.replace(/\s+/g, '-')"
          :prop="sample"
          :label="sample"
          width="120"
          align="center"
        >
          <template #default="{ row }">
            <span :class="{ 'zero-value': Number(row[sample]) === 0 }">
              {{ formatTPM(row[sample]) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 热图展示 -->
    <el-card shadow="never" class="heatmap-card">
      <div class="heatmap-header">
        <h3>表达量热图</h3>
        <el-button
          type="primary"
          size="small"
          :icon="ElIconDownload"
          @click="downloadHeatmap"
        >
          下载热图
        </el-button>
      </div>
      <div ref="heatmap" class="heatmap-container"></div>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { searchExpression } from '@/api/expression'
import { exportToCsv, exportToExcel } from '@/utils/exportUtils'

export default {
  name: 'ExpressionResults',
  data() {
    return {
      developmentMode: process.env.NODE_ENV === 'development',
      loading: true,
      expressionData: [], // 从后端获取的原始数据
      allSamples: [], // 所有样本列名
      selectedSamples: [], // 用户选择的样本
      targetIds: [], // 查询的基因ID列表
      queryParams: null, // 查询参数
      heatmapChart: null, // ECharts实例
      heatmapData: [], // 热图数据，格式: [gene_id, sample, value]
      tableKey: 0, // 用于强制表格重新渲染
    }
  },
  computed: {
    // 根据选择的样本过滤数据（这里不需要过滤，只是列过滤，数据已经由后端返回）
    filteredExpressionData() {
      return this.expressionData
    },
    tableHeight() {
      // 基础高度 + 每行高度 × 行数
      const baseHeight = 120 // 表头等固定高度
      const rowHeight = 40 // 每行高度
      const rowCount = this.expressionData.length
      const maxHeight = 600 // 最大高度

      // 计算高度，限制在200-600px
      return Math.min(
        Math.max(200, baseHeight + rowHeight * rowCount),
        maxHeight
      )
    },
    firstRow() {
      return this.expressionData.length > 0 ? this.expressionData[0] : {}
    },
  },
  async mounted() {
    // 从路由参数中获取查询条件
    this.queryParams = this.$route.query
    await this.fetchData()
  },
  beforeUnmount() {
    if (this.heatmapChart) {
      this.heatmapChart.dispose()
    }
  },
  methods: {
    formatTPM(value) {
      if (value === null || value === undefined || value === '') return '-'

      try {
        const num = Number(value)
        return isNaN(num) ? '-' : num.toFixed(4)
      } catch (e) {
        console.error('格式化TPM错误:', e, value)
        return '-'
      }
    },

    async fetchData() {
      this.loading = true
      try {
        // 构建请求参数，包括用户选择的样本（初始为空，则返回所有样本）
        const params = {
          ...this.queryParams,
          selectedSamples: this.selectedSamples,
        }

        const response = await searchExpression(params)
        this.expressionData = response.expressionData
        this.allSamples = response.samples
        this.targetIds = this.queryParams.targetIds || []

        // 如果selectedSamples为空，则默认选择前5个样本
        if (this.selectedSamples.length === 0) {
          this.selectedSamples = this.allSamples.slice(
            0,
            Math.min(5, this.allSamples.length)
          )
        }

        // 准备热图数据
        this.prepareHeatmapData()

        // 渲染热图
        this.$nextTick(() => {
          this.renderHeatmap()
        })
      } catch (error) {
        console.error('获取数据失败:', error)
        this.$message.error('获取数据失败: ' + (error.message || '未知错误'))
      } finally {
        this.loading = false
      }
    },

    // 准备热图数据
    prepareHeatmapData() {
      this.heatmapData = []
      this.expressionData.forEach((row) => {
        this.selectedSamples.forEach((sample) => {
          this.heatmapData.push([row.gene_id, sample, row[sample] || 0])
        })
      })
    },

    renderHeatmap() {
      if (!this.heatmapData.length || !this.selectedSamples.length) return

      if (this.heatmapChart) {
        this.heatmapChart.dispose()
      }

      const chartDom = this.$refs.heatmap
      if (!chartDom) return

      this.heatmapChart = echarts.init(chartDom)

      // 准备热图数据：格式为 [sampleIndex, geneIndex, value]
      const dataForHeatmap = this.heatmapData.map((item) => {
        return [
          this.selectedSamples.indexOf(item[1]),
          this.targetIds.indexOf(item[0]),
          item[2],
        ]
      })

      const option = {
        tooltip: {
          position: 'top',
          formatter: function (params) {
            return `基因: ${this.targetIds[params.value[1]]}<br>样本: ${
              this.selectedSamples[params.value[0]]
            }<br>TPM: ${params.value[2].toFixed(4)}`
          }.bind(this),
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '10%',
          containLabel: true,
        },
        xAxis: {
          type: 'category',
          data: this.selectedSamples,
          splitArea: {
            show: true,
          },
        },
        yAxis: {
          type: 'category',
          data: this.targetIds,
          splitArea: {
            show: true,
          },
        },
        visualMap: {
          min: 0,
          max: Math.max(...this.heatmapData.map((item) => item[2])) || 100,
          calculable: true,
          orient: 'vertical',
          left: 'right',
          top: 'center',
          inRange: {
            color: [
              '#313695',
              '#4575b4',
              '#74add1',
              '#abd9e9',
              '#e0f3f8',
              '#ffffbf',
              '#fee090',
              '#fdae61',
              '#f46d43',
              '#d73027',
              '#a50026',
            ],
          },
        },
        series: [
          {
            name: 'TPM',
            type: 'heatmap',
            data: dataForHeatmap,
            label: {
              show: false,
            },
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowColor: 'rgba(0, 0, 0, 0.5)',
              },
            },
          },
        ],
      }

      this.heatmapChart.setOption(option)
    },

    selectAllSamples() {
      this.selectedSamples = [...this.allSamples]
      this.refreshData()
    },

    clearSamples() {
      this.selectedSamples = []
    },

    // 当样本选择变化时，重新获取数据（因为后端需要根据选择的样本来返回数据）
    refreshData() {
      this.fetchData()
    },

    backToSearch() {
      this.$router.push({ name: 'ExpressionSearch' })
    },

    downloadTable(format) {
      const data = this.filteredExpressionData
      const filename = `expression_data_${new Date()
        .toISOString()
        .slice(0, 10)}`

      if (format === 'csv') {
        exportToCsv(data, filename)
      } else {
        exportToExcel(data, filename)
      }
      this.$message.success(`已导出${format.toUpperCase()}文件`)
    },

    downloadHeatmap() {
      if (!this.heatmapChart) return

      const image = this.heatmapChart.getDataURL({
        type: 'png',
        pixelRatio: 2,
        backgroundColor: '#fff',
      })

      const link = document.createElement('a')
      link.href = image
      link.download = `expression_heatmap_${new Date()
        .toISOString()
        .slice(0, 10)}.png`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    },
  },
  watch: {
    selectedSamples: {
      // 当选择的样本变化时，重新获取数据（因为后端需要过滤列）
      handler() {
        // 当样本选择变化时，增加 tableKey 强制表格重新渲染
        this.tableKey += 1

        // 如果热图数据需要重新计算
        this.prepareHeatmapData()

        // 如果需要重新获取数据（取决于您的实现）
        // this.fetchData();
      },
      deep: true,
    },
  },
}
</script>

<style scoped>
.zero-value {
  color: #999;
  font-style: italic;
}

/* 调试信息样式 */
.debug-section {
  margin-bottom: 15px;
}
.expression-results-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 15px;
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

.filter-card {
  margin-bottom: 10px;
  padding: 5px; /* 减小内边距 */
}

.sample-selector {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.selector-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.selector-title {
  font-weight: bold;
  margin-right: 15px;
}

.sample-checkboxes {
  display: flex;
  flex-wrap: wrap;
}

.sample-checkbox {
  margin-right: 10px;
  margin-bottom: 6px;
  min-width: 110px;
}

.results-card {
  margin-bottom: 20px;
}

.heatmap-card {
  margin-bottom: 30px;
}

.heatmap-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.heatmap-container {
  width: 100%;
  height: 500px;
}

.action-buttons {
  text-align: center;
  margin-top: 20px;
  padding-right: 20px; /* 添加右边距 */
}

.action-buttons .el-button {
  margin: 0 10px;
  width: 150px;
}
</style>
