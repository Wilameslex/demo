<template>
  <div class="mitochondrion-container">
    <!-- 页面标题 -->
    <div class="page-header" v-once>
      <h1>Mitochondrion Genome Browser</h1>
      <p class="sub-title">The information of the mitochondrion genome of different Chinese mitten crabs' population</p>
    </div>

    <!-- 物种选择栏 -->
    <div class="species-bar">
      <label class="species-label">Select the population</label>
      <el-select
          v-model="selectedSpecies"
          placeholder="Please select the population"
          @change="handleSpeciesChange"
          class="species-select"
      >
        <el-option
            v-for="species in speciesList"
            :key="species.value"
            :label="species.label"
            :value="species.value"
        ></el-option>
      </el-select>
    </div>

    <!-- 核心：强制左右布局 -->
    <div class="main-layout" v-if="selectedSpecies">
      <!-- 左侧：结构图 -->
      <div class="layout-left">
        <div class="card-wrapper">
          <div class="card-header">
            <h3>Mitochondrion Genome Structure</h3>
            <el-button
                type="success"
                size="small"
                icon="el-icon-download"
                @click="downloadSvg"
                :disabled="!svgUrl || svgError"
            >
              Download picture
            </el-button>
          </div>
          <div class="svg-container">
            <img
                :src="svgUrl"
                alt="Mitochondrion Genome Structure"
                class="svg-embed"
                v-loading="svgLoading"
                element-loading-text="loading..."
                @error="handleResourceError('svg')"
            >
            <div class="resource-error" v-if="svgError">
              <el-icon class="error-icon"><WarningFilled /></el-icon>
              <span>结构图加载失败</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：注释表（修复v-else-if错误） -->
      <div class="layout-right">
        <div class="card-wrapper">
          <div class="card-header">
            <h3>The annotation</h3>
            <div class="header-actions">
              <el-tag type="info"> {{ mitoInfo.length }} records in total</el-tag>
              <el-button
                  type="success"
                  size="small"
                  icon="el-icon-download"
                  @click="downloadAnnotationTable"
              >
                Download Tablie
              </el-button>
            </div>
          </div>
          <!-- 修复1：用v-if+v-else替代v-else-if，避免无相邻v-if -->
          <template v-if="mitoInfo.length > 0">
            <el-table
                :data="mitoInfo"
                stripe
                border
                highlight-current-row
                style="width: 100%"
                max-height="600"
                size="small"
            >
              <el-table-column prop="name" label="Name" width="120" align="center"></el-table-column>
              <el-table-column prop="feature" label="Feature" width="100" align="center"></el-table-column>
              <el-table-column prop="start" label="Start" width="80" align="center"></el-table-column>
              <el-table-column prop="end" label="End" width="80" align="center"></el-table-column>
              <el-table-column prop="length" label="Length" width="80" align="center"></el-table-column>
              <el-table-column prop="strand" label="Strand" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.strand === '+' ? 'success' : 'warning'" size="mini">
                    {{ row.strand || '-' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </template>
          <template v-else>
            <!-- 修复2：v-if单独使用，避免v-else-if无前置条件 -->
            <el-empty
                description="暂无注释信息"
                class="empty-tip"
                v-if="!infoLoading"
            ></el-empty>
          </template>
        </div>
      </div>
    </div>

    <!-- 修复3：确保v-else对应相邻的v-if -->
    <el-empty
        description="Please select the population"
        class="empty-tip"
        v-else
    ></el-empty>
  </div>
</template>

<script setup>
// 脚本部分与之前一致，无需修改
import { ref, onMounted, watch } from 'vue';
import { mitochondrionApi } from '@/api/genome';
import { ElMessage, ElEmpty } from 'element-plus';
import { WarningFilled } from '@element-plus/icons-vue';
import { exportToExcel } from '@/utils/exportUtils';

const selectedSpecies = ref('');
const speciesList = ref([]);
const mitoInfo = ref([]);
const svgUrl = ref('');
const infoLoading = ref(false);
const svgLoading = ref(false);
const svgError = ref(false);

onMounted(async () => {
  await fetchSpeciesList();
});

const fetchSpeciesList = async () => {
  try {
    const res = await mitochondrionApi.getSpeciesList();
    speciesList.value = Object.freeze(res || []);
    if (speciesList.value.length > 0) {
      selectedSpecies.value = speciesList.value[0].value;
    }
  } catch (error) {
    handleResourceError('species', error);
  }
};

const handleResourceError = (type, error = null) => {
  const errorMap = {
    species: 'The population loading fails',
    svg: 'The structure loading fails',
    annotation: 'The annotation loading fails',
  };
  const msg = errorMap[type] + (error ? `：${error.message}` : '');
  ElMessage.error(msg);
  if (type === 'svg') {
    svgError.value = true;
    svgLoading.value = false;
  }
};

const handleSpeciesChange = async (species) => {
  if (!species) return;

  const [svgRes, annotationRes] = await Promise.allSettled([
    (async () => {
      svgLoading.value = true;
      svgError.value = false;
      const url = await mitochondrionApi.getSvgUrl(species);
      if (!url) throw new Error("SVG路径为空");
      svgUrl.value = url;
      return url;
    })(),
    (async () => {
      infoLoading.value = true;
      const res = await mitochondrionApi.getMitoInfo(species);
      mitoInfo.value = Object.freeze(res || []);
      if (mitoInfo.value.length === 0) {
        ElMessage.warning('no annotation temporarily');
      }
      return res;
    })()
  ]);

  if (svgRes.status === 'rejected') {
    handleResourceError('svg', svgRes.reason);
  } else {
    svgLoading.value = false;
  }

  if (annotationRes.status === 'rejected') {
    handleResourceError('annotation', annotationRes.reason);
    mitoInfo.value = Object.freeze([]);
  }
  infoLoading.value = false;
};

const downloadSvg = () => {
  if (!svgUrl.value) {
    ElMessage.warning('SVG loading fails');
    return;
  }
  const speciesLabel = speciesList.value.find(item => item.value === selectedSpecies.value)?.label || '未知物种';
  const a = document.createElement('a');
  a.href = svgUrl.value;
  a.download = `${speciesLabel.replace(/\s|\(|\)/g, '_')}_mitogenome.svg`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  ElMessage.success('Download is loading...');
};

const downloadAnnotationTable = () => {
  if (mitoInfo.value.length === 0) {
    ElMessage.warning('no annotation temporarily');
    return;
  }
  const speciesLabel = speciesList.value.find(item => item.value === selectedSpecies.value)?.label || '未知物种';
  const exportData = mitoInfo.value.map(item => ({
    "Name": item.name || '-',
    "Feature": item.feature || '-',
    "Start": item.start || '-',
    "End": item.end || '-',
    "Length": item.length || '-',
    "Strand": item.strand || '-'
  }));
  const fileName = `${speciesLabel.replace(/\s|\(|\)/g, '_')}_mitogenome_annotation`;
  exportToExcel(exportData, fileName, 'The annotation of Mitochondrion Genome');
  ElMessage.success('Download is loading...');
};

watch(selectedSpecies, (newVal) => {
  if (newVal) {
    handleSpeciesChange(newVal);
  }
}, { immediate: true });
</script>

<style scoped>
/* 外层容器：撑满宽度 */
.mitochondrion-container {
  width: 100%;
  max-width: 1920px;
  margin: 0 auto;
  padding: 10px 20px;
  box-sizing: border-box;
}

/* 物种选择栏 */
.species-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
}
.species-label {
  font-weight: 500;
  color: #333;
}
.species-select {
  width: 280px;
}

/* 核心左右布局 */
.main-layout {
  display: flex;
  gap: 15px;
  flex-wrap: nowrap;
  align-items: flex-start;
  width: 100%;
}

/* 左右栏平分宽度 */
.layout-left, .layout-right {
  flex: 1;
  min-width: 400px;
}

/* 卡片容器 */
.card-wrapper {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
  background: #fff;
  box-sizing: border-box;
  width: 100%;
}

/* 卡片标题 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 8px;
}

/* 结构图容器 */
.svg-container {
  width: 100%;
  min-height: 480px;
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}
.svg-embed {
  width: 100%;
  max-height: 480px;
  object-fit: contain;
}

/* 表格样式 */
.el-table {
  width: 100% !important;
  font-size: 12px;
}
.el-table__cell {
  padding: 5px 0;
}

/* 响应式：超小屏幕换行 */
@media (max-width: 800px) {
  .main-layout {
    flex-wrap: wrap;
  }
  .layout-left, .layout-right {
    min-width: 100%;
  }
}

/* 基础样式 */
.page-header {
  text-align: center;
  margin-bottom: 15px;
}
.page-header h1 {
  font-size: 22px;
  color: #333;
}
.sub-title {
  font-size: 13px;
  color: #666;
}
.resource-error {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #f56c6c;
  font-size: 14px;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
.error-icon {
  font-size: 18px;
}
.empty-tip {
  margin: 20px 0;
}
</style>