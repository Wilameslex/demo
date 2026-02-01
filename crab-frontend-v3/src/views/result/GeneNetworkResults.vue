<template>
  <div class="gene-network-results-container">
    <div class="header">
      <h1><i class="el-icon-collection"></i> Gene Network Results</h1>
      <div class="sub-header">
        <el-tag type="info">
          Network Type：{{ networkType === 'metamorphosis' ? 'Metamorphosis' : 'Maturity' }}
        </el-tag>
        <el-tag type="success">Target gene：{{ targetGeneId }}</el-tag>
        <el-tag type="info">Associate genes：{{ relatedGenesWithInfo.length }}</el-tag>
        <el-button icon="el-icon-back" @click="backToSearch" size="small">
          Return Search
        </el-button>
      </div>
    </div>
    <!-- 新增：目标基因基础信息展示 -->
    <el-card shadow="hover" class="target-gene-card" v-if="targetGeneInfo">
      <div slot="header">
        <h3>Target Gene Information</h3>
      </div>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="Gene ID">{{ targetGeneInfo.geneId }}</el-descriptions-item>
        <el-descriptions-item label="Gene Name">{{ targetGeneInfo.geneAbbrev || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Chromosome">{{ targetGeneInfo.chr || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Start">{{ targetGeneInfo.start?.toString() || '-' }}</el-descriptions-item>
        <el-descriptions-item label="End">{{ targetGeneInfo.end?.toString() || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Description" :span="2">{{ targetGeneInfo.description || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <el-button
          type="success"
          icon="el-icon-download"
          @click="downloadTable('excel')"
      >
        Download Result
      </el-button>
    </div>

    <!-- 相关基因表格（合并基础信息+网络数据） -->
    <el-card shadow="never" class="results-card">
      <el-table :data="relatedGenesWithInfo" v-loading="loading" stripe border highlight-current-row style="width: 100%">
        <el-table-column prop="relatedGeneId" label="Related Gene ID" width="180" fixed="left"></el-table-column>
        <el-table-column prop="correlation" label="Correlation" width="120">
          <template #default="{ row }">{{ row.correlation ? row.correlation.toFixed(4) : '-' }}</template>
        </el-table-column>
        <el-table-column prop="pvalue" label="P Value" width="120">
          <template #default="{ row }">{{ row.pvalue ? row.pvalue.toFixed(4) : '-' }}</template>
        </el-table-column>
        <el-table-column prop="qvalue" label="Q Value" width="120">
          <template #default="{ row }">{{ row.qvalue ? row.qvalue.toFixed(4) : '-' }}</template>
        </el-table-column>
        <el-table-column prop="geneAbbrev" label="Gene Name" width="150"></el-table-column>
        <el-table-column prop="chr" label="Chromosome" width="100"></el-table-column>
        <el-table-column prop="start" label="Start" width="150">
          <template #default="{ row }">{{ row.start?.toString() || '-' }}</template>
        </el-table-column>
        <el-table-column prop="end" label="End" width="100">
          <template #default="{ row }">{{ row.end?.toString() || '-' }}</template>
        </el-table-column>
        <el-table-column prop="description" label="Description" min-width="300"></el-table-column>
      </el-table>
    </el-card>

    <!-- 新增：基因表达量展示（表格+热力图可选） -->
    <!-- 基因表达量展示（新增下载按钮） -->
    <el-card shadow="never" class="expr-card">
      <div slot="header" class="network-header"> <!-- 复用network-header样式，确保布局一致 -->
        <h3>Gene Expression in Samples</h3>
        <!-- 新增：下载表达量按钮 -->
        <el-button
            type="primary"
            size="small"
            icon="el-icon-download"
            @click="downloadExprTable"
        >
          Download Expr Table
        </el-button>
      </div>
      <!-- 表达量表格（原有代码不变） -->
      <el-table :data="exprTableData" v-loading="loading" stripe border style="width: 100%" max-height="400">
        <el-table-column prop="geneId" label="Gene ID" width="180" fixed="left"></el-table-column>
        <el-table-column prop="geneName" label="Gene Name" width="150"></el-table-column>
        <el-table-column
            v-for="sample in sampleList"
            :label="sample"
            :key="sample"
            width="120"
        >
          <template #default="{ row }">{{ row.exprData[sample] ? row.exprData[sample].toFixed(2) : '-' }}</template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 基因网络图（ECharts力导向图） -->
    <el-card shadow="never" class="network-card">
      <div class="network-header">
        <h3>Gene Network Graph</h3>
        <el-button
            type="primary"
            size="small"
            icon="el-icon-download"
            @click="downloadNetworkGraph"
        >
          Download network graph
        </el-button>
      </div>
      <div ref="networkGraph" class="network-container"></div>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { searchGeneNetwork } from '@/api/geneNetwork';
import { exportToExcel } from '@/utils/exportUtils';

export default {
  name: 'GeneNetworkResults',
  data() {
    return {
      loading: true,
      targetGeneId: '',
      networkType: '',
      targetGeneInfo: null, // 目标基因基础信息
      relatedGenesWithInfo: [], // 相关基因（含基础信息）
      geneExpressionMap: {}, // 表达量映射
      sampleList: [], // 样本列表
      exprTableData: [], // 表达量表格数据
      networkChart: null
    };
  },
  async mounted() {
    // 优先从sessionStorage获取参数（避免路由参数丢失）
    const sessionParams = JSON.parse(sessionStorage.getItem('geneNetworkQuery'));
    // 若sessionStorage无数据，从路由query获取
    const queryParams = sessionParams || this.$route.query;

    this.targetGeneId = queryParams.targetGeneId;
    this.networkType = queryParams.networkType;

    if (!this.targetGeneId || !this.networkType) {
      this.$message.error('query params is missing, return to the search page');
      this.backToSearch();
      return;
    }

    // 加载网络数据
    await this.fetchNetworkData();
  },
  beforeUnmount() {
    // 销毁ECharts实例，避免内存泄漏
    if (this.networkChart) {
      this.networkChart.dispose();
    }
  },
  methods: {
    async fetchNetworkData() {
      this.loading = true;
      try {
        const queryParams = {
          networkType: this.networkType,
          targetGeneId: this.targetGeneId
        };
        // 1. 调用接口（res直接是后端返回的data对象）
        const res = await searchGeneNetwork(queryParams);
        // 2. 增加空值校验，避免res为undefined
        const data = res || {};
        // 3. 直接访问data中的字段（去掉.data）
        this.targetGeneInfo = data.targetGeneInfo || {};
        this.relatedGenesWithInfo = data.relatedGenesWithInfo || []; // 表格绑定的是这个变量
        this.geneExpressionMap = data.geneExpressionMap || {}; // 表达量数据，不是relatedExpr
        this.hasData = this.relatedGenesWithInfo.length > 0;

        // 处理表达量数据
        this.handleExprData();
        // 渲染网络图
        this.$nextTick(() => this.renderNetworkGraph());
      } catch (error) {
        this.$message.error('Fail to get data：' + (error.message || '未知错误'));
      } finally {
        this.loading = false;
      }
    },
    // 处理表达量数据，生成表格和样本列表
    handleExprData() {
      const exprList = Object.values(this.geneExpressionMap);
      if (exprList.length === 0) return;

      // 提取样本列表（从第一个基因的表达量中获取）
      const firstExpr = exprList[0];
      this.sampleList = firstExpr.exprData ? Object.keys(firstExpr.exprData) : [];

      // 构建表达量表格数据（关联基因名称）
      this.exprTableData = exprList.map(item => {
        const geneInfo = item.geneId === this.targetGeneId
            ? this.targetGeneInfo
            : this.relatedGenesWithInfo.find(g => g.relatedGeneId === item.geneId);
        return {
          geneId: item.geneId,
          geneName: geneInfo?.geneAbbrev || '-', // 改为geneAbbrev
          exprData: item.exprData || {}
        };
      });
    },
    // 渲染基因网络图（力导向图）
    renderNetworkGraph() {
      const chartDom = this.$refs.networkGraph;
      if (!chartDom || !this.relatedGenesWithInfo || this.relatedGenesWithInfo.length === 0) {
        return;
      }

      // 销毁旧实例
      if (this.networkChart) {
        this.networkChart.dispose();
      }

      this.networkChart = echarts.init(chartDom);

      // 1. 构建节点数据（中心节点+周边节点）
      const nodes = [
        // 中心节点（目标基因）
        {
          id: this.targetGeneId,
          name: this.targetGeneId,
          symbolSize: 30, // 节点大小
          itemStyle: { color: '#ff4d4f' } // 红色突出
        },
        // 周边节点（相关基因）
        ...this.relatedGenesWithInfo.map(gene => ({
          id: gene.relatedGeneId,
          name: gene.relatedGeneId,
          // 修复3：确保correlation存在（避免null/undefined导致的symbolSize异常）
          symbolSize: 20 + (gene.correlation || 0) * 10,
          itemStyle: { color: '#409eff' } // 蓝色
        }))
      ];

      // 2. 构建边数据（目标基因与相关基因的连接）
      const links = this.relatedGenesWithInfo.map(gene => ({
        source: this.targetGeneId,
        target: gene.relatedGeneId,
        lineStyle: {
          // 修复5：确保correlation存在（避免null/undefined导致的lineWidth异常）
          width: (gene.correlation || 0) * 3,
          color: (gene.correlation || 0) > 0.8 ? '#ff4d4f' : '#666'
        },
        correlation: (gene.correlation || 0).toFixed(4)
      }));


      // 3. ECharts配置项
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: params => {
            if (params.type === 'node') {
              return `Gene: ${params.name}`;
            }
            return `Correlation: ${params.data.correlation}`;
          }
        },
        series: [{
          type: 'graph',
          layout: 'force',
          data: nodes,
          links: links,
          roam: true,
          label: { show: true, fontSize: 12 },
          force: {
            repulsion: 300,
            edgeLength: 150,
            gravity: 0.1
          },
          lineStyle: {
            opacity: 0.8,
            curveness: 0
          }
        }]
      };

      this.networkChart.setOption(option);
      window.addEventListener('resize', () => this.networkChart.resize());
    },
    // 下载结果表格
    downloadTable(format) {
      // 关键：使用this.relatedGenesWithInfo替代this.relatedGenes
      const exportData = this.relatedGenesWithInfo.map(gene => ({
        Target_Gene_ID: this.targetGeneId,
        Related_Gene_ID: gene.relatedGeneId,
        Correlation: gene.correlation ? gene.correlation.toFixed(4) : '-',
        P_Value: gene.pvalue ? gene.pvalue.toFixed(4) : '-',
        Q_Value: gene.qvalue ? gene.qvalue.toFixed(4) : '-',
        Gene_Name: gene.geneAbbrev || '-', // 对应表格的Gene Name列
        Chromosome: gene.chr || '-',       // 对应表格的Chromosome列
        Start: gene.start ? gene.start.toString() : '-', // 避免Long类型科学计数法
        End: gene.end ? gene.end.toString() : '-',
        Description: gene.description || '-',
        Network_Type: this.networkType === 'metamorphosis' ? 'Metamorphosis' : 'Maturity'
      }));
      const fileName = `gene_network_${this.targetGeneId}_${this.networkType}`;
      exportToExcel(exportData, fileName, 'Gene Network Data');
      this.$message.success('Download successfully.');
    },
    // 新增：下载表达量表格
    downloadExprTable() {
      // 1. 校验表达量数据是否存在
      if (this.exprTableData.length === 0) {
        return this.$message.warning('no expression data to download');
      }

      // 2. 构建下载数据（基因ID+基因名+所有样本表达量）
      const exportData = this.exprTableData.map(gene => {
        // 基础字段（基因ID、基因名）
        const baseData = {
          Gene_ID: gene.geneId,
          Gene_Name: gene.geneName || '-'
        };
        // 动态添加每个样本的表达量（从exprData中提取）
        this.sampleList.forEach(sample => {
          baseData[`Sample_${sample}`] = gene.exprData[sample] ? gene.exprData[sample].toFixed(2) : '-';
        });
        // 补充网络类型（便于区分数据来源）
        baseData.Network_Type = this.networkType === 'metamorphosis' ? 'Metamorphosis' : 'Maturity';
        return baseData;
      });

      // 3. 调用导出工具，生成Excel
      const fileName = `gene_expression_${this.targetGeneId}_${this.networkType}`;
      exportToExcel(exportData, fileName, 'Gene Expression Data');
      this.$message.success('Download successfully.');
    },
    // 下载网络图（PNG格式）
    downloadNetworkGraph() {
      if (!this.networkChart) return;
      const imgUrl = this.networkChart.getDataURL({
        type: 'png',
        pixelRatio: 2, // 高清图
        backgroundColor: '#fff'
      });
      const link = document.createElement('a');
      link.href = imgUrl;
      link.download = `gene_network_graph_${this.targetGeneId}.png`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      this.$message.success('Graph download successfully.');
    },
    // 返回搜索页
    backToSearch() {
      this.$router.push({ name: 'GeneNetworkSearch' });
    }
  }
};
</script>

<style scoped>
.gene-network-results-container {
  max-width: 1400px;
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
.action-buttons {
  margin-bottom: 20px;
  text-align: right;
}
.results-card {
  margin-bottom: 30px;
}
.network-card {
  margin-bottom: 30px;
}
.network-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.network-container {
  width: 100%;
  height: 600px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}
.target-gene-card {
  margin-bottom: 20px;
}
/* 表达量表格样式 */
.expr-card {
  margin-bottom: 30px;
}
/* 保留原有样式 */
.gene-network-results-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}
</style>