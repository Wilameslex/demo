<template>
  <div class="process-expression-results-container">
    <!-- 头部标题：修复多余符号 << 和 </ 错误 -->
    <div class="header">
      <h1><i class="el-icon-collection"></i> Biological Process Expression Overview</h1>
      <div class="sub-header">
        <el-tag type="info">
          process：{{ processMap[queryParams.process] || '未知过程' }}
        </el-tag>
        <el-tag type="success">
          pipeline：{{ queryParams.pipeline === 'stringtie' ? 'STAR+Stringtie' : 'STAR+RSEM' }}
        </el-tag>
        <el-button icon="el-icon-back" @click="backToSearch" size="small">返回查询</el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <el-loading v-if="loading" fullscreen text="Analyzing..."></el-loading>

    <!-- 无数据提示 -->
    <el-empty v-else-if="!hasData" description="Could not find relevant transcriptome"></el-empty>

    <!-- 结果内容（仅保留柱状图+下载功能） -->
    <div v-else class="results-content">
      <!-- 1. 柱状图：每类样本的表达基因数（核心保留） -->
      <el-card class="chart-card" shadow="never">
        <div class="card-header">
          <h3>1. Expression genes of each sample</h3>
        </div>
        <div id="barChartDom" class="chart-container"></div>
      </el-card>

      <!-- 2. 核心下载功能区（保留前20基因+所有特异基因下载） -->
      <el-card class="download-card" shadow="never">
        <div class="card-header">
          <h3>2. Download the specific genes</h3>
          <div class="download-buttons">
            <!-- 按样本类下载前20基因 -->
            <el-dropdown @command="downloadTop20" class="mr-2">
              <el-button type="primary" size="small">
                Download samples top 20 genes <Download style="margin-left: 5px;" />
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="cls in sampleClasses" :key="cls" :command="cls">
                    {{ cls }} top20 genes
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <!-- 下载所有特异基因 -->
            <el-button
                type="success"
                size="small"
                @click="downloadSpecificGenes"
                :disabled="!specGene.length"
            >
              Download all specific genes <Download style="margin-left: 5px;" />
            </el-button>
          </div>
        </div>

        <!-- 特异基因预览表格（修正字段名，匹配实体类） -->
        <el-table
            v-if="specGene.length"
            :data="specGene.slice(0, 10)"
            border
            stripe
            style="width: 100%; margin-top: 20px;"
            max-height="300"
        >
          <el-table-column prop="geneId" label="gene/transcriptID" width="180"></el-table-column>
          <el-table-column prop="sampleClass" label="sample class" width="120"></el-table-column>
          <!-- 修正：匹配实体类ProcessSpecGene的字段名（intraMean/otherMean） -->
          <el-table-column label="sample expression(mean)" width="150">
            <template #default="{ row }">{{ (row?.intraMean ?? 0).toFixed(4) }}</template>
          </el-table-column>
          <el-table-column label="other samples expression(mean)" width="150">
            <template #default="{ row }">{{ (row?.otherMean ?? 0).toFixed(4) }}</template>
          </el-table-column>
        </el-table>
        <p v-if="specGene.length" class="table-note">Only show the first 10 genes, you can download by click the button</p>
        <p v-else class="no-spec-gene">no specific genes</p>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, nextTick, markRaw } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import { Download } from '@element-plus/icons-vue';
import { getProcessExprData } from '@/api/expression';
import { exportToExcel } from '@/utils/exportUtils';

const processMap = {
  metamorphosis: 'metamorphosis',
  maturity: 'maturity',
  molting: 'molting',
  carcinization: 'carcinization',    // 新增
  premature: 'premature',          // 新增
  outwater: 'outwater',        // 新增
  photoperiodschange: 'photoperiodschange', // 新增
  relimb: 'relimb'
};

// 路由实例
const router = useRouter();

// **************************
// 1. 响应式数据定义（保留核心，增加空值初始化）
// **************************
const loading = ref(true);
const hasData = ref(false);
const queryParams = ref({});
const exprCount = ref([]); // 柱状图数据（默认空数组）
const specGene = ref([]); // 特异基因数据（默认空数组）
const sampleClasses = ref([]); // 样本类（默认空数组）

// **************************
// 2. 仅保留柱状图实例（删除冗余图表资源）
// **************************
let barChart = null;
const barResizeHandler = ref(null);

// **************************
// 3. 核心方法：数据获取（适配request.js，增加日志+空值校验）
// **************************
const fetchPreCalcData = async () => {
  try {
    // 校验必填参数（避免参数缺失导致后端报错）
    const requiredParams = ['process', 'pipeline', 'searchType'];
    const missingParams = requiredParams.filter(key => !queryParams.value[key]);
    if (missingParams.length > 0) {
      ElMessage.error(`缺少必填参数：${missingParams.join(', ')}`);
      return;
    }

    const params = {
      process: queryParams.value.process,
      pipeline: queryParams.value.pipeline,
      searchType: queryParams.value.searchType
    };
    console.log('📡 请求参数:', params); // 新增：打印请求参数，确认传递正确

    // 发起请求（适配request.js：返回的是后端Result中的data字段）
    const res = await getProcessExprData(params);
    console.log('✅ 后端返回数据:', res); // 新增：打印响应数据，排查结构问题

    // 空值校验：如果res未定义，直接返回
    if (!res) {
      ElMessage.error('后端未返回有效数据');
      return;
    }

    // 提取数据（增加空值校验，避免字段缺失报错）
    exprCount.value = res?.exprCount ?? [];
    specGene.value = res?.specGene ?? [];
    sampleClasses.value = exprCount.value.map(item => item?.sampleClass || '未知类').sort();

    // 判断是否有数据（基于柱状图数据长度）
    hasData.value = exprCount.value.length > 0;
  } catch (error) {
    ElMessage.error('获取数据失败：' + (error.message || '服务器错误'));
    console.error('📢 数据获取错误详情:', error); // 打印完整错误，方便排查
  } finally {
    loading.value = false;
  }
};

// **************************
// 4. 柱状图渲染（增加空值校验，避免数据异常）
// **************************
const renderBarChart = (retryCount = 3) => {
  const chartDom = document.getElementById('barChartDom');
  if (!chartDom) {
    if (retryCount > 0) setTimeout(() => renderBarChart(retryCount - 1), 100);
    return;
  }

  barChart = markRaw(echarts.init(chartDom, null, { renderer: 'canvas' }));
  // 数据处理：增加空值校验，避免undefined影响图表渲染
  const seriesData = exprCount.value.map(item => item?.expressedGeneCount ?? 0);
  const xAxisData = exprCount.value.map(item => item?.sampleClass || '未知类');

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: ({ axisIndex }) => {
        const sampleClass = xAxisData[axisIndex];
        const count = seriesData[axisIndex];
        return `${sampleClass}: ${count} 个表达基因`;
      }
    },
    xAxis: { type: 'category', data: xAxisData, axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', name: '表达基因数量', min: 0 },
    series: [{
      name: '表达基因数', type: 'bar', data: seriesData,
      itemStyle: { color: '#409EFF' }, label: { show: true, position: 'top' },
      barWidth: '60%'
    }],
    grid: { left: '8%', right: '5%', top: '15%', bottom: '15%' }
  };

  barChart.setOption(option);
  barResizeHandler.value = () => barChart.resize();
  window.addEventListener('resize', barResizeHandler.value);
};

// **************************
// 5. 下载功能（修复 ref 响应式数据访问错误 + 空值校验）
// **************************
const downloadTop20 = (sampleClass) => {
  // 筛选当前样本类的特异基因（增加空值校验）
  const classGenes = specGene.value
      .filter(gene => gene?.sampleClass === sampleClass)
      .sort((a, b) => (b?.intraMean ?? 0) - (a?.intraMean ?? 0)) // 按类内均值降序
      .slice(0, 20);

  if (!classGenes.length) {
    ElMessage.warning(`${sampleClass}类无特异基因可下载`);
    return;
  }

  // 构建下载数据（字段名匹配实体类，增加空值格式化）
  const exportData = classGenes.map(gene => ({
    'gene/transcriptID': gene?.geneId || '未知ID',
    'sample class': gene?.sampleClass || '未知类',
    'sample expression(mean)': (gene?.intraMean ?? 0).toFixed(4),
    'other samples expression(mean)': (gene?.otherMean ?? 0).toFixed(4)
  }));

  const filename = `${queryParams.value.process}_${sampleClass}_top20_${queryParams.value.searchType}`;
  exportToExcel(exportData, filename, `${sampleClass}类前20特异基因`);
  ElMessage.success(`已开始下载${sampleClass}类前20基因`);
};

const downloadSpecificGenes = () => {
  // 修复：ref数据必须加 .value 访问length
  if (!specGene.value.length) {
    ElMessage.warning('无特异基因可下载');
    return;
  }

  const exportData = specGene.value.map(gene => ({
    'gene/transcriptID': gene?.geneId || '未知ID',
    'sample class': gene?.sampleClass || '未知类',
    'sample expression(mean)': (gene?.intraMean ?? 0).toFixed(4),
    'other samples expression(mean)': (gene?.otherMean ?? 0).toFixed(4)
  }));

  const filename = `${queryParams.value.process}_specific_genes_${queryParams.value.searchType}`;
  exportToExcel(exportData, filename, 'all samples specific genes');
  ElMessage.success('已开始下载所有特异基因表格');
};

// **************************
// 6. 页面交互（保留返回功能）
// **************************
const backToSearch = () => {
  router.push({ name: 'ProcessExpressionSearch' });
};

// **************************
// 7. 初始化逻辑（增加参数校验）
// **************************
const init = async () => {
  // 获取查询参数（从sessionStorage或路由query）
  queryParams.value = JSON.parse(sessionStorage.getItem('processExpressionQuery')) || router.currentRoute.query;
  console.log('🔧 初始化查询参数:', queryParams.value); // 打印初始化参数，确认正确

  // 请求数据并渲染图表
  await fetchPreCalcData();
  if (hasData.value) {
    await nextTick();
    renderBarChart();
  }
};

// **************************
// 8. 生命周期钩子（仅销毁柱状图资源）
// **************************
onMounted(() => {
  init();
});

onBeforeUnmount(() => {
  // 移除resize事件，避免内存泄漏
  if (barResizeHandler.value) window.removeEventListener('resize', barResizeHandler.value);
  // 销毁图表实例
  if (barChart) barChart.dispose();
});
</script>

<style scoped>
.process-expression-results-container {
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
  flex-wrap: wrap;
  gap: 10px;
}

/* 柱状图卡片样式 */
.chart-card {
  margin-bottom: 30px;
  padding: 20px;
  border-radius: 8px;
}

/* 下载功能卡片样式 */
.download-card {
  margin-bottom: 30px;
  padding: 20px;
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.download-buttons {
  display: flex;
  gap: 10px;
}

/* 柱状图容器样式 */
.chart-container {
  width: 100% !important;
  height: 500px !important;
  min-height: 400px !important;
  box-sizing: border-box;
}

.table-note {
  text-align: right;
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}

.no-spec-gene {
  text-align: center;
  color: #909399;
  font-size: 14px;
  padding: 20px 0;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .chart-container {
    height: 300px;
  }
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  .download-buttons {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}

</style>
