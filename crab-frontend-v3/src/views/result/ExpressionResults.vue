<template>
  <div class="expression-results-container">
    <div class="header">
      <h1><i class="el-icon-collection"></i> Gene Expression Search Result</h1>
      <div class="sub-header">
        <el-tag type="info"> {{ expressionData.length }} records in total</el-tag>
        <el-button icon="el-icon-back" @click="backToSearch" size="small">
          Return Search
        </el-button>
      </div>
    </div>
    <!-- 调试信息（开发环境显示） -->
    <div v-if="developmentMode" class="debug-section">
      <el-alert title="Debug information" type="info" :closable="false">
        <p>Query params: {{ queryParams }}</p>
        <p>No.samples: {{ selectedSamples.length }} | No.genes: {{ expressionData.length }}</p>
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
            Select all
          </el-button>
          <el-button
              size="mini"
              @click="clearSamples"
              :disabled="selectedSamples.length === 0"
          >
            Clear all
          </el-button>
        </div>
        <el-checkbox-group
            v-model="selectedSamples"
            class="sample-checkboxes"
        >
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
          @click="downloadTable('csv')"
      >
        <!-- 正确写法：用el-icon包裹图标，添加el-icon--right类让图标居右 -->
        CSV
        <el-icon class="el-icon--right">
          <Download />
        </el-icon>
      </el-button>
      <el-button
          type="success"
          style="margin-bottom: 20px; margin-left: 100px"
          @click="downloadTable('excel')"
      >
        Excel
        <el-icon class="el-icon--right">
          <Download />
        </el-icon>
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
          :row-key="row => row.gene_id"
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
            <span :class="{'zero-value': Number(row[sample]) === 0}">
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
        <el-dropdown @command="handleHeatmapDownload">
          <el-button type="primary" size="small" icon="el-icon-download">
            下载热图<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="png">PNG</el-dropdown-item>
              <el-dropdown-item command="svg">SVG</el-dropdown-item>
              <el-dropdown-item command="pdf">PDF</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <div ref="heatmap" class="heatmap-container"></div>
    </el-card>


  </div>
</template>

<script>
import { Download }  from "@element-plus/icons-vue";
import * as echarts from 'echarts';
import { searchExpression } from '@/api/expression';
import { exportExpressionToCsv, exportToExcel } from '@/utils/exportUtils';
import { jsPDF } from 'jspdf';
import html2canvas from 'html2canvas';

export default {
  name: 'ExpressionResults',
  components: {
    Download
  },
  data() {
    return {
      developmentMode: process.env.NODE_ENV === 'development',
      loading: true,
      expressionData: [],  // 从后端获取的原始数据
      allSamples: [],      // 所有样本列名
      selectedSamples: [], // 用户选择的样本
      targetIds: [],       // 查询的基因ID列表
      queryParams: null,   // 查询参数
      heatmapChart: null,  // ECharts实例
      heatmapData: [],      // 热图数据，格式: [gene_id, sample, value]
      tableKey: 0 // 用于强制表格重新渲染
    };
  },
  computed: {
    // 根据选择的样本过滤数据（这里不需要过滤，只是列过滤，数据已经由后端返回）
    filteredExpressionData() {
      return this.expressionData;
    },
    tableHeight() {
      const baseHeight = 120;
      const rowHeight = 40;
      // 因 expressionData 已兜底为空数组，length 不会报错
      const rowCount = this.expressionData.length;
      const maxHeight = 600;
      return Math.min(Math.max(200, baseHeight + rowHeight * rowCount), maxHeight);
    },
    firstRow() {
      return this.expressionData.length > 0 ? this.expressionData[0] : {};
    }

  },
  async mounted() {
    // 从路由参数中获取查询条件
    this.queryParams = JSON.parse(sessionStorage.getItem('expressionQuery')) || this.$route.query || {};
    await this.fetchData();
  },
  beforeUnmount() {
    if (this.heatmapChart) {
      this.heatmapChart.dispose();
    }
  },
  methods: {
    formatTPM(value) {
      if (value === null || value === undefined || value === '') return '-';

      try {
        const num = Number(value);
        return isNaN(num) ? '-' : num.toFixed(4);
      } catch (e) {
        console.error('type of TPM is wrong:', e, value);
        return '-';
      }
    },

    async fetchData() {
      if (!this.queryParams.pipeline || !this.queryParams.targetIds) {
        this.$message.error('Params have been expired, please try again');
        this.$router.push({ name: 'ExpressionSearch' });
        this.loading = false;
        return;
      }
      // 开启加载状态，避免用户重复操作
      this.loading = true;
      try {
        // **************************
        // 第一步：处理targetIds格式（强制转为数组）
        // **************************
        let targetIds = this.queryParams.targetIds;
        // 若为字符串（如"LOC127006214"或"LOC127006214,LOC127006215"），按逗号分割为数组
        if (typeof targetIds === 'string') {
          targetIds = targetIds
              .split(',')        // 按逗号分割
              .map(item => item.trim())  // 去除前后空格
              .filter(item => item);     // 过滤空字符串（避免传空值给后端）
        }
        // 若为undefined/null，直接设为空数组
        else if (!targetIds) {
          targetIds = [];
        }

        // **************************
        // 第二步：构建最终请求参数
        // **************************
        const params = {
          ...this.queryParams,
          targetIds: targetIds,        // 传入处理后的数组格式targetIds
          selectedSamples: this.selectedSamples || []  // 兼容空值
        };

        // 日志：打印最终传给后端的参数（验证格式是否正确）
        console.log('🚀 request params finally:', JSON.stringify(params, null, 2));

        // **************************
        // 第三步：调用后端接口（关键：直接获取业务数据，无需解析Result）
        // **************************
        // request.js拦截器已处理Result，返回的直接是业务数据（{expressionData: [...], samples: [...]}）
        const businessData = await searchExpression(params);

        // 日志：打印后端返回的业务数据（核心验证点）
        console.log('📊 Business Data Response:', {
          expressionData: businessData.expressionData,
          samples: businessData.samples,
          expressionDataLength: businessData.expressionData?.length || 0,
          samplesLength: businessData.samples?.length || 0
        });

        // **************************
        // 第四步：初始化样本列表（先赋值，再处理expressionData）
        // **************************
        this.allSamples = businessData.samples || [];
        console.log('📋 List of initialized samples:', this.allSamples);

        // **************************
        // 第五步：处理表达量数据（匹配gene_id字段，过滤无效数据）
        // **************************
        this.expressionData = (businessData.expressionData || [])
            .map(item => {
              // 提取基因ID（兼容后端可能的字段名：gene_id/gene/geneId/id）
              const geneId = item.gene_id || item.gene || item.geneId || item.id;
              // 日志：打印单个数据项和提取的geneId（验证字段匹配）
              console.log('🔍 Single expression items:', { item, geneId });

              // 无基因ID则跳过（避免渲染空数据）
              if (!geneId) {
                console.warn('⚠️ Skip items withou gene id:', item);
                return null;
              }

              // 构建最终渲染数据（匹配表格prop，保留所有样本列）
              return {
                gene_id: geneId,  // 表格ID列绑定的prop是gene_id，必须赋值
                // 导出功能用：过滤出仅样本列的表达量（基于allSamples）
                expressionValues: Object.fromEntries(
                    Object.entries(item).filter(([key]) => this.allSamples.includes(key))
                ),
                ...item  // 展开所有样本列（如eyestalk1、ganglion5等，用于表格渲染）
              };
            })
            .filter(Boolean);  // 过滤null项（无基因ID的数据）

        // 日志：打印最终渲染的表达量数据（验证是否有值）
        console.log('✅ Final rended expression data:', this.expressionData);

        // **************************
        // 第六步：初始化其他变量（目标基因、默认样本选择）
        // **************************
        // 初始化目标基因ID
        this.targetIds = businessData.targetIds || targetIds;
        // 默认选择前5个样本（提升用户体验）
        const safeSelected = this.selectedSamples || [];
        if (safeSelected.length === 0 && this.allSamples.length > 0) {
          this.selectedSamples = this.allSamples.slice(0, Math.min(5, this.allSamples.length));
        }

        // **************************
        // 第七步：渲染热图
        // **************************
        this.prepareHeatmapData();
        // 确保DOM更新后再渲染热图（避免容器未加载）
        this.$nextTick(() => {
          this.renderHeatmap();
        });

      } catch (error) {
        // 异常处理：打印错误+友好提示+重置数据
        console.error('❌ Fail to get expression data:', error);
        this.$message.error('Fail to get data: ' + (error.message || '未知错误'));
        this.expressionData = [];
        this.allSamples = [];
        this.selectedSamples = [];
      } finally {
        // 无论成功/失败，关闭加载状态
        this.loading = false;
      }
    },

    // 准备热图数据
    prepareHeatmapData() {
      this.heatmapData = [];
      this.expressionData.forEach(row => {
        this.selectedSamples.forEach(sample => {
          this.heatmapData.push([
            row.gene_id,
            sample,
            row[sample] || 0
          ]);
        });
      });
    },

    renderHeatmap() {
      if (!this.heatmapData.length || !this.selectedSamples.length) return;

      if (this.heatmapChart) {
        this.heatmapChart.dispose();
      }

      const chartDom = this.$refs.heatmap;
      if (this.selectedSamples.length > 20) {
        chartDom.style.height = `${this.selectedSamples.length * 20 + 100}px`; // 动态增高
      } else {
        chartDom.style.height = '500px'; // 默认高度
      }

      if (!chartDom) return;

      this.heatmapChart = echarts.init(chartDom);

      // 准备热图数据：格式为 [sampleIndex, geneIndex, value]
      const dataForHeatmap = this.heatmapData.map(item => {
        return [
          this.selectedSamples.indexOf(item[1]),
          this.targetIds.indexOf(item[0]),
          item[2]
        ];
      });

      const option = {
        tooltip: {
          position: 'top',
          formatter: function(params) {
            return `gene: ${this.targetIds[params.value[1]]}<br>sample: ${this.selectedSamples[params.value[0]]}<br>TPM: ${params.value[2].toFixed(4)}`;
          }.bind(this)
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: this.selectedSamples,
          splitArea: {
            show: true
          }
        },
        yAxis: {
          type: 'category',
          data: this.targetIds,
          splitArea: {
            show: true
          }
        },
        visualMap: {
          min: 0,
          max: Math.max(...this.heatmapData.map(item => item[2])) || 100,
          calculable: true,
          orient: 'vertical',
          left: 'right',
          top: 'center',
          inRange: {
            color: ['#313695', '#4575b4', '#74add1', '#abd9e9', '#e0f3f8', '#ffffbf', '#fee090', '#fdae61', '#f46d43', '#d73027', '#a50026']
          }
        },
        series: [{
          name: 'TPM',
          type: 'heatmap',
          data: dataForHeatmap,
          label: {
            show: false
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      };

      this.heatmapChart.setOption(option);
    },

    selectAllSamples() {
      this.selectedSamples = [...this.allSamples];
      this.refreshData();
      this.fetchData();
    },

    clearSamples() {
      this.selectedSamples = [];
      this.fetchData();
    },

    // 当样本选择变化时，重新获取数据（因为后端需要根据选择的样本来返回数据）
    refreshData() {
      this.fetchData();
    },

    backToSearch() {
      this.$router.push({ name: 'ExpressionSearch' });
    },

    downloadTable(format) {
      const data = this.filteredExpressionData; // 基因表达数据（格式：[{geneId, expressionValues: {样本1: 值1...}}]）
      const filename = `expression_data_${new Date().toISOString().slice(0, 10)}`;

      try {
        if (format === 'csv') {
          exportExpressionToCsv(data, filename);
        } else {
          exportToExcel(data, filename, 'Expression Data');
        }
        this.$message.success(`Successfully export${format.toUpperCase()}文件`);
      } catch (error) {
        this.$message.error(`Export ${format.toUpperCase()} fails：${error.message || '未知错误'}`);
        console.error(`Export ${format.toUpperCase()} fails：`, error);
      }
    },

    handleHeatmapDownload(format) {
      if (!this.heatmapChart) {
        this.$message.warning('Heatmap is missing, please select samples');
        return;
      }
      const timestamp = new Date().toISOString().slice(0, 10);
      const filename = `expression_heatmap_${timestamp}.${format}`;

      switch (format) {
        case 'png':
          this.downloadPng(filename);
          break;
        case 'svg':
          this.downloadSvg(filename);
          break;
        case 'pdf':
          this.downloadPdf(filename);
          break;
        default:
          this.$message.error('不支持的格式，请选择PNG/SVG/PDF');
      }
    },

    downloadPng(filename) {
      const pngData = this.heatmapChart.getDataURL({
        type: 'png',
        pixelRatio: 2,
        backgroundColor: '#fff'
      });
      this.downloadFile(pngData, filename, 'image/png');
      this.$message.success(`PNG has been download：${filename}`);
    },
    
    downloadSvg(filename) {
      try {
        let svgDataUrl = this.heatmapChart.getDataURL({
          type: 'svg',
          backgroundColor: '#fff',
          excludeComponents: ['toolbox']
        });

        if (!svgDataUrl || !svgDataUrl.startsWith('data:image/svg+xml') || !svgDataUrl.includes(',')) {
          const sourceDom = this.$refs.heatmap;
          const rect = sourceDom ? sourceDom.getBoundingClientRect() : null;
          const offscreenDom = document.createElement('div');
          offscreenDom.style.position = 'fixed';
          offscreenDom.style.left = '-10000px';
          offscreenDom.style.top = '-10000px';
          offscreenDom.style.width = rect && rect.width ? `${Math.ceil(rect.width)}px` : '800px';
          offscreenDom.style.height = rect && rect.height ? `${Math.ceil(rect.height)}px` : '600px';
          document.body.appendChild(offscreenDom);

          let offscreenChart = null;
          try {
            offscreenChart = echarts.init(offscreenDom, null, { renderer: 'svg' });
            const option = this.heatmapChart.getOption();
            offscreenChart.setOption(option, true);
            offscreenChart.resize();
            svgDataUrl = offscreenChart.getDataURL({
              type: 'svg',
              backgroundColor: '#fff',
              excludeComponents: ['toolbox']
            });
          } finally {
            if (offscreenChart) {
              offscreenChart.dispose();
            }
            if (offscreenDom && offscreenDom.parentNode) {
              offscreenDom.parentNode.removeChild(offscreenDom);
            }
          }
        }

        if (!svgDataUrl || !svgDataUrl.startsWith('data:image/svg+xml') || !svgDataUrl.includes(',')) {
          this.$message.error('SVG生成失败，图表数据异常');
          return;
        }

        const a = document.createElement('a');
        a.href = svgDataUrl;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        this.$message.success('SVG has been download：' + filename);
      } catch (error) {
        console.error('SVG下载失败:', error);
        this.$message.error('SVG下载失败，请重试');
      }
    },

    downloadPdf(filename) {
      const chartDom = this.$refs.heatmap;
      html2canvas(chartDom, {
        scale: 2,
        useCORS: true,
        backgroundColor: '#fff'
      }).then(canvas => {
        const pdf = new jsPDF({
          orientation: 'landscape',
          unit: 'mm',
          format: 'a4'
        });
        const pageWidth = pdf.internal.pageSize.getWidth();
        const imgWidth = pageWidth - 20;
        const imgHeight = (canvas.height * imgWidth) / canvas.width;
        pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 10, 10, imgWidth, imgHeight);
        pdf.save(filename);
        this.$message.success(`PDF has been download：${filename}`);
      }).catch(error => {
        console.error('Fails to generate PDF：', error);
        this.$message.error('Fails to generate PDF, Please try again');
      });
    },

    // 通用文件下载工具函数（复用）
    downloadFile(url, filename, mimeType) {
      const link = document.createElement('a');
      link.href = url;
      link.download = filename;
      link.type = mimeType;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    },
  },
  watch: {
    selectedSamples: {
      handler() {
        this.tableKey += 1; // 强制表格重新渲染
        this.fetchData(); // 关键：重新获取选中样本的表达数据
      },
      deep: true // 监听数组内部元素变化
    }
  }
};

</script>

<style scoped>
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
