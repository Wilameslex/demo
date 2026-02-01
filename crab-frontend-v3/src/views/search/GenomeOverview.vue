<template>
  <div class="genome-overview-container">
    <!-- 页面标题（固定为中华绒螯蟹） -->
    <div class="page-header" v-once>
      <h1><i class="el-icon-dna"></i> Genome Overview</h1>
      <p class="sub-title">The Genome Overview of Chinese mitten crab (Eriocheir sinensis)</p>
    </div>

    <!-- 核心内容：上下布局（移除组装选择卡片） -->
    <div class="content-wrapper">
      <!-- 上部分：参考基因组信息（字段罗列式） -->
      <el-card shadow="hover" class="assembly-detail-card">
        <div slot="header">
          <h3>Genome Assembly</h3>
          <el-tag type="primary">ASM2467909v1</el-tag>
        </div>

        <div class="assembly-detail-content">
          <!-- 字段罗列：每行一个字段 -->
          <div class="detail-row">
            <div class="detail-label">Assembly Name</div>
            <div class="detail-value">{{ assemblyDetail.assemblyName || '-' }}</div>
          </div>
          <div class="detail-divider"></div>

          <div class="detail-row">
            <div class="detail-label">Species</div>
            <div class="detail-value">
              {{ assemblyDetail.species || '-' }}
              <el-link
                  v-if="assemblyDetail.taxonomicId"
                  :href="`https://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=${assemblyDetail.taxonomicId}`"
                  target="_blank"
                  type="primary"
                  underline="hover"
              >
                (TaxID: {{ assemblyDetail.taxonomicId }})
              </el-link>
            </div>
          </div>
          <div class="detail-divider"></div>

          <div class="detail-row">
            <div class="detail-label">Infraspecific Breed</div>
            <div class="detail-value">{{ assemblyDetail.infraspecificBreed || '-' }}</div>
          </div>
          <div class="detail-divider"></div>

          <div class="detail-row">
            <div class="detail-label">Annotation Name</div>
            <div class="detail-value">{{ assemblyDetail.annotationName || '-' }}</div>
          </div>
          <div class="detail-divider"></div>

          <div class="detail-row">
            <div class="detail-label">Total Sequence Length</div>
            <div class="detail-value">
              {{ formatBigNumber(assemblyDetail.totalSequenceLength) }} bp
            </div>
          </div>
          <div class="detail-divider"></div>

          <div class="detail-row">
            <div class="detail-label">Assembly Level</div>
            <div class="detail-value">{{ assemblyDetail.assemblyLevel || '-' }}</div>
          </div>
          <div class="detail-divider"></div>

          <div class="detail-row">
            <div class="detail-label">Chromosomes</div>
            <div class="detail-value">{{ assemblyDetail.chromosomes || 0 }} </div>
          </div>
          <div class="detail-divider"></div>

          <div class="detail-row">
            <div class="detail-label">Contig N50</div>
            <div class="detail-value">{{ formatBigNumber(assemblyDetail.contigN50) }} bp</div>
          </div>
          <div class="detail-divider"></div>

          <div class="detail-row">
            <div class="detail-label">Scaffold N50</div>
            <div class="detail-value">{{ formatBigNumber(assemblyDetail.scaffoldN50) }} bp</div>
          </div>
          <div class="detail-divider"></div>

          <div class="detail-row">
            <div class="detail-label">GC Percent</div>
            <div class="detail-value">{{ assemblyDetail.gcPercent || 0 }} %</div>
          </div>
          <div class="detail-divider"></div>

          <div class="detail-row">
            <div class="detail-label">BUSCO Assessment</div>
            <div class="detail-value">{{ assemblyDetail.busco || '-' }}</div>
          </div>
          <div class="detail-divider"></div>

          <div class="detail-row">
            <div class="detail-label">Sequencing Technology</div>
            <div class="detail-value">{{ assemblyDetail.sequencingTech || '-' }}</div>
          </div>
          <div class="detail-divider"></div>

          <div class="detail-row">
            <div class="detail-label">BioProject Accession</div>
            <div class="detail-value">
              <el-link
                  v-if="assemblyDetail.bioprojectAccession"
                  :href="`https://www.ncbi.nlm.nih.gov/bioproject/${assemblyDetail.bioprojectAccession}`"
                  target="_blank"
                  type="primary"
                  underline="hover"
              >
                {{ assemblyDetail.bioprojectAccession }}
              </el-link>
              <span v-else>-</span>
            </div>
          </div>
          <div class="detail-divider"></div>

          <div class="detail-row">
            <div class="detail-label">Total Genes</div>
            <div class="detail-value">{{ assemblyDetail.genes || 0 }} 个</div>
          </div>
          <div class="detail-divider"></div>

          <div class="detail-row">
            <div class="detail-label">Protein-coding Genes</div>
            <div class="detail-value">{{ assemblyDetail.proteinCodingGenes || 0 }} 个</div>
          </div>
        </div>
      </el-card>

      <!-- 下部分：染色体信息（NCBI风格表格，prop从chromosomeNum改为chromosomes） -->
      <el-card shadow="hover" class="chromosome-table-card" v-if="chromosomeList.length > 0">
        <div slot="header" class="card-header-flex">
          <h3>Chromosome Information</h3>
          <el-button
              type="success"
              size="small"
              icon="el-icon-download"
              @click="downloadChromosomeTable"
          >
            Download Chromosome Table
          </el-button>
        </div>

        <el-table
            :data="chromosomeList"
            stripe
            border
            highlight-current-row
            style="width: 100%"
            max-height="600"
            size="small"
            @sort-change="handleTableSort"
        >
          <el-table-column
              prop="chromosomes"
              label="Chromosome"
              align="center"
              sortable="custom"
          >
          <template #default="{ row }">
            <el-link
                :href="`https://www.ncbi.nlm.nih.gov/nuccore/${row.refseqAccession}`"
                target="_blank"
                type="primary"
                underline="hover"
            >
              {{ row.chromosomes }}
            </el-link>
          </template>
          </el-table-column>
          <el-table-column
              prop="refseqAccession"
              label="RefSeq Accession"
              align="center"
          ></el-table-column>
          <el-table-column
              prop="gcCount"
              label="GC Count"
              align="center"
              sortable="custom"
          >
            <template #default="{ row }">
              {{ formatBigNumber(row.gcCount) }}
            </template>
          </el-table-column>
          <el-table-column
              prop="gcPercent"
              label="GC Percent"
              align="center"
              sortable="custom"
          >
            <template #default="{ row }">
              {{ row.gcPercent || 0 }} %
            </template>
          </el-table-column>
          <el-table-column
              prop="length"
              label="Length (bp)"
              align="center"
              sortable="custom"
          >
            <template #default="{ row }">
              {{ formatBigNumber(row.length) }}
            </template>
          </el-table-column>
        </el-table>
        <div class="note-tip">
          <span>Note: This genome assembly includes 2090 unplaced scaffolds.</span>
        </div>
      </el-card>

      <!-- 无染色体数据提示 -->
      <el-empty
          description="暂无中华绒螯蟹染色体信息"
          class="empty-tip"
          v-else-if="!loading"
      ></el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { genomeOverviewApi } from '@/api/genomeOverview';
import { ElMessage, ElEmpty } from 'element-plus';
import { exportToExcel } from '@/utils/exportUtils';

// 响应式数据（移除selectedAssembly/assemblyList）
const assemblyDetail = ref({});            // 参考基因组详情
const chromosomeList = ref([]);            // 染色体列表
const loading = ref(false);                // 加载状态

// 页面初始化：加载中华绒螯蟹固定数据
onMounted(async () => {
  await Promise.all([
    fetchAssemblyDetail(),
    fetchChromosomes()
  ]);
});

// 1. 加载中华绒螯蟹参考基因组详情
const fetchAssemblyDetail = async () => {
  loading.value = true;
  try {
    const res = await genomeOverviewApi.getAssemblyDetail();
    assemblyDetail.value = res || {};
  } catch (error) {
    ElMessage.error('加载基因组详情失败：' + (error.message || '未知错误'));
    assemblyDetail.value = {};
  } finally {
    loading.value = false;
  }
};

// 2. 加载中华绒螯蟹染色体信息
const fetchChromosomes = async () => {
  loading.value = true;
  try {
    const res = await genomeOverviewApi.getChromosomes();
    chromosomeList.value = res || [];
  } catch (error) {
    ElMessage.error('加载染色体信息失败：' + (error.message || '未知错误'));
    chromosomeList.value = [];
  } finally {
    loading.value = false;
  }
};

// 3. 表格排序（按染色体号/GC/长度）
const handleTableSort = async (sortInfo) => {
  const { prop, order } = sortInfo;
  if (!prop || !order) return;

  // 前端临时排序
  chromosomeList.value.sort((a, b) => {
    const valA = a[prop];
    const valB = b[prop];
    if (order === 'ascending') {
      return valA - valB;
    } else {
      return valB - valA;
    }
  });
};

// 4. 下载染色体表格（Excel）
const downloadChromosomeTable = () => {
  if (chromosomeList.value.length === 0) {
    ElMessage.warning('暂无染色体数据可下载');
    return;
  }

  // 格式化导出数据
  const exportData = chromosomeList.value.map(item => ({
    "Chromosome": item.chromosomes || '-', // 字段名调整
    "RefSeq Accession": item.refseqAccession || '-',
    "GC Count": genomeOverviewApi.formatBigNumber(item.gcCount) || '-',
    "GC Percent": `${item.gcPercent || 0} %`,
    "Length (bp)": genomeOverviewApi.formatBigNumber(item.length) || '-'
  }));

  // 固定文件名：中华绒螯蟹染色体数据
  const fileName = "Eriocheir_sinensis_chromosomes";
  exportToExcel(exportData, fileName, '中华绒螯蟹染色体信息');
  ElMessage.success('染色体数据下载中...');
};

// 5. 复用大数字格式化方法
const formatBigNumber = (num) => {
  return genomeOverviewApi.formatBigNumber(num);
};
</script>

<style scoped>
/* 外层容器 */
.genome-overview-container {
  max-width: 1600px;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 20px;
}
.page-header h1 {
  font-size: 26px;
  color: #333;
}
.sub-title {
  font-size: 14px;
  color: #666;
}

/* 核心上下布局 */
.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 参考基因组详情卡片（NCBI风格） */
.assembly-detail-card {
  padding: 20px;
}
.assembly-detail-content {
  margin-top: 15px;
}
/* 字段行：标签右对齐，值左对齐 */
.detail-row {
  display: flex;
  margin-bottom: 8px;
  line-height: 28px;
}
.detail-label {
  width: 220px;
  min-width: 220px;
  text-align: right;
  font-weight: 500;
  color: #666;
  padding-right: 15px;
  box-sizing: border-box;
}
.detail-value {
  flex: 1;
  color: #333;
  font-size: 14px;
}
/* 分隔线 */
.detail-divider {
  height: 1px;
  background-color: #f0f0f0;
  margin: 8px 0;
}

/* 染色体表格卡片 */
.chromosome-table-card {
  padding: 20px;
}
/* 卡片Header：标题+下载按钮 */
.card-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.note-tip {
  margin-top: 15px;
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-left: 3px solid #409eff;
  font-size: 13px;
  color: #666;
}
.el-table {
  font-size: 13px;
}
.el-table__cell {
  padding: 8px 0;
}

/* 无数据提示 */
.empty-tip {
  margin: 50px 0;
}

/* 响应式适配 */
@media (max-width: 992px) {
  .detail-label {
    width: 180px;
    min-width: 180px;
  }
}

@media (max-width: 600px) {
  .detail-row {
    flex-direction: column;
  }
  .detail-label {
    width: 100%;
    text-align: left;
    padding-right: 0;
    margin-bottom: 4px;
  }
}
</style>