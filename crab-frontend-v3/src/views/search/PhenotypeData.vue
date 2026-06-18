<template>
  <div class="phenotype-container">
    <div class="page-header" v-once>
      <h1><i class="el-icon-menu"></i> Phenotype Data</h1>
      <p class="sub-title">Crab, crawfish and snail phenotype data overview</p>
    </div>

    <el-tabs v-model="activeTab" class="module-tabs">
      <el-tab-pane label="Eriocheir sinensis" name="eriocheir">
        <div class="main-layout" v-if="appearanceData.length > 0 || growthData.length > 0">
          <div class="layout-left">
            <div class="card-wrapper">
              <div class="card-header">
                <h3>Phenotype Scheme</h3>
              </div>
              <div class="svg-container">
                <img
                  src="@/assets/phenotype/phenotype_scheme.png"
                  alt="河蟹表型指标示意图"
                  class="svg-embed"
                  v-loading="schemeLoading"
                  element-loading-text="加载示意图中..."
                  @error="handleSchemeError"
                >
                <div class="resource-error" v-if="schemeError">
                  <el-icon class="error-icon"><WarningFilled /></el-icon>
                  <span>示意图加载失败</span>
                </div>
              </div>
              <div class="remark-text">
                <p>(a) Positions of measurement points on the dorsal surface of the dorsal armor (A1-A7; B1-B4; L1-L3; S1-S7).</p>
                <p>(b) Positions of dorsal armor lateral measurement points (C1-C4).</p>
                <p>(c) Positions of measuring points of the ventral armor (L4, L5).</p>
                <p>(d) Positions of measuring points of the fourth pereiopod (F1, F2).</p>
                <p>(e) Position of foot measurement points of the fifth pereiopod (F3, F4).</p>
                <p>A1: 1-1′, A2: 2-2′, A3: 3-3′, A4: 4-4′, A5: 5-5′, A6: 6-6′, A7: 7-7′; B1: 7-8, B2: 7-9, B3: 7-10, B4: 7-11; C1: 12-8, C2: 12-9, C3: 12-10, C4: 12-11; L1: 13-14, L2: 13-15, L3: 13-14, L4: 12-11; L5: 16-13; H: Height at measurement point 16, S1: 12-17, S2: 12-18, S3: 17-3, S4: 17-4, S5: 17-5, S6: 17-6, S7: 17-7; F1: 19-20, F2: 21-22; F3: 23-24, F4: Measure the width at point 23.</p>
              </div>
            </div>
          </div>

          <div class="layout-right">
            <div class="card-wrapper mb-20">
              <div class="card-header">
                <h3>Population Appearance Data</h3>
                <el-tag type="info"> {{ appearanceData.length }} populations in total</el-tag>
              </div>
              <div class="table-scroll">
                <el-table :data="appearanceData" stripe border highlight-current-row style="width: 100%" max-height="300" size="small">
                  <el-table-column prop="population" label="Population" width="150" align="center"></el-table-column>
                  <el-table-column prop="a1" label="A1" width="120" align="center"></el-table-column>
                  <el-table-column prop="a2" label="A2" width="120" align="center"></el-table-column>
                  <el-table-column prop="a3" label="A3" width="120" align="center"></el-table-column>
                  <el-table-column prop="a4" label="A4" width="120" align="center"></el-table-column>
                  <el-table-column prop="a5" label="A5" width="120" align="center"></el-table-column>
                  <el-table-column prop="a6" label="A6" width="120" align="center"></el-table-column>
                  <el-table-column prop="b1" label="B1" width="120" align="center"></el-table-column>
                  <el-table-column prop="b2" label="B2" width="120" align="center"></el-table-column>
                  <el-table-column prop="b3" label="B3" width="120" align="center"></el-table-column>
                  <el-table-column prop="b4" label="B4" width="120" align="center"></el-table-column>
                  <el-table-column prop="c1" label="C1" width="120" align="center"></el-table-column>
                  <el-table-column prop="c2" label="C2" width="120" align="center"></el-table-column>
                  <el-table-column prop="c3" label="C3" width="120" align="center"></el-table-column>
                  <el-table-column prop="c4" label="C4" width="120" align="center"></el-table-column>
                  <el-table-column prop="s1" label="S1" width="120" align="center"></el-table-column>
                  <el-table-column prop="s2" label="S2" width="120" align="center"></el-table-column>
                  <el-table-column prop="s3" label="S3" width="120" align="center"></el-table-column>
                  <el-table-column prop="s4" label="S4" width="120" align="center"></el-table-column>
                  <el-table-column prop="s5" label="S5" width="120" align="center"></el-table-column>
                  <el-table-column prop="s6" label="S6" width="120" align="center"></el-table-column>
                  <el-table-column prop="s7" label="S7" width="120" align="center"></el-table-column>
                </el-table>
              </div>
            </div>

            <div class="card-wrapper mb-20">
              <div class="card-header">
                <h3>Population Growth Data</h3>
                <el-tag type="info"> {{ growthData.length }} populations in total</el-tag>
              </div>
              <el-table :data="growthData" stripe border highlight-current-row style="width: 100%" max-height="250" size="small">
                <el-table-column prop="population" label="Population" width="150" align="center"></el-table-column>
                <el-table-column prop="date" label="Date" width="120" align="center"></el-table-column>
                <el-table-column prop="number" label="Sample Number" width="120" align="center"></el-table-column>
                <el-table-column prop="carapaceLength" label="Carapace Length (mm)" width="160" align="center"></el-table-column>
                <el-table-column prop="carapaceWidth" label="Carapace Width (mm)" width="160" align="center"></el-table-column>
                <el-table-column prop="fatFactor" label="Condition Factor" width="140" align="center"></el-table-column>
                <el-table-column prop="hepatopancreasIndex" label="Hepatopancreas Index (%)" width="180" align="center"></el-table-column>
                <el-table-column prop="gonadIndex" label="Gonad Index (%)" width="140" align="center"></el-table-column>
              </el-table>
            </div>

            <div class="card-wrapper">
              <div class="download-container">
                <h3>Total Phenotype Download</h3>
                <el-button
                  type="primary"
                  size="medium"
                  @click="downloadTotalData"
                  class="download-btn"
                  :disabled="totalDataCount === 0"
                >
                  Download all phenotype data of Eriocheir sinensis （Excel）
                </el-button>
                <p class="download-tip">All phenotype data ( {{ totalDataCount }} records in total)</p>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无表型数据" class="empty-tip"></el-empty>
      </el-tab-pane>

      <el-tab-pane label="Procambarus clarkii" name="crawfish">
        <div class="main-layout">
          <div class="layout-left">
            <div class="card-wrapper">
              <div class="card-header">
                <h3>Procambarus clarkii</h3>
              </div>
              <div class="svg-container crawfish-figure-container">
                <img :src="crawfishImg" alt="Procambarus clarkii phenotype scheme" class="svg-embed crawfish-figure-image">
              </div>
              <div class="remark-text">
                <p>a. 额剑长(rostrum length); b. 头甲长(length of cephalic region); c. 胸甲长(length of thoracic region); d. 腹部长(length of abdomen); e. 尾节长(length of tail-fan); f. 额剑基底宽(width of rostrum base); g(CW). 头胸甲宽(width of carapace);</p>
                <p>h. 第二腹节长(length of second segment); i. 第二腹节远体端宽(width of distal end of second segment); j. 第六腹节长(length of sixth segment); k. 第六腹节体端宽(width of distal end of sixth segment);</p>
                <p>L. 第二步足长度(length of second pereiopod); CL=btc. 头胸甲长(length of carapace); TL=a+b+c+d+e. 总长(total length); BL=b+c+d+e. 体长(body length)</p>
              </div>
            </div>
          </div>

          <div class="layout-right">
            <div class="card-wrapper mb-20">
              <div class="card-header">
                <h3>crawfish_phenotype Preview</h3>
                <el-tag type="info">First {{ crawfishPreview.length }} rows</el-tag>
              </div>
              <div class="table-scroll">
                <el-table :data="crawfishPreview" stripe border highlight-current-row style="width: 100%" max-height="560" size="small">
                  <el-table-column v-for="column in crawfishColumns" :key="column.prop" :prop="column.prop" :label="column.label" :width="column.width" align="center"></el-table-column>
                </el-table>
              </div>
            </div>

            <div class="card-wrapper">
              <div class="download-container">
                <h3>Download Dataset</h3>
                <el-button type="primary" size="medium" class="download-btn" @click="downloadCrawfishData">
                  Download al phenotype data of crawfish (Excel)
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="Snail" name="snail">
        <div class="main-layout">
          <div class="layout-left">
            <div class="card-wrapper">
              <div class="card-header">
                <h3>Snail</h3>
              </div>
              <div class="svg-container">
                <img :src="snailImg" alt="Snail phenotype scheme" class="svg-embed">
              </div>
              <div class="remark-text">
                <p>SW. 螺旋部宽(Spire width); W2W. 第二螺层宽(Second whorl width); AW. 壳口宽(Aperture width); SWd. 壳宽(Shell width); SH. 螺旋部高(Spire height);</p>
                <p>BWH. 体螺层高(Body whorl height); W2H. 第二螺层高(Second whorl height); W1H. 第一螺层高(First whorl height); AH. 壳口高(Aperture height); SHt. 壳高(Shell height)</p>
              </div>
            </div>
          </div>

          <div class="layout-right">
            <div class="card-wrapper mb-20">
              <div class="card-header">
                <h3>snail_phenotype Preview</h3>
                <el-tag type="info">first {{ snailPreview.length }} rows</el-tag>
              </div>
              <div class="table-scroll">
                <el-table :data="snailPreview" stripe border highlight-current-row style="width: 100%" max-height="560" size="small">
                  <el-table-column v-for="column in snailColumns" :key="column.prop" :prop="column.prop" :label="column.label" :width="column.width" align="center"></el-table-column>
                </el-table>
              </div>
            </div>

            <div class="card-wrapper">
              <div class="download-container">
                <h3>Download Dataset</h3>
                <el-button type="primary" size="medium" class="download-btn" @click="downloadSnailData">
                  Download all phenotype data of snail_phenotype (Excel)
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { phenotypeApi } from '@/api/phenotype';
import { ElMessage } from 'element-plus';
import { WarningFilled } from '@element-plus/icons-vue';
import crawfishImg from '@/assets/phenotype/crawfish.png';
import snailImg from '@/assets/phenotype/snail.png';

const activeTab = ref('eriocheir');
const appearanceData = ref([]);
const growthData = ref([]);
const totalDataCount = ref(0);
const crawfishPreview = ref([]);
const snailPreview = ref([]);
const schemeLoading = ref(false);
const schemeError = ref(false);

const crawfishColumns = [
  { prop: 'sampleId', label: 'sample_id', width: 100 },
  { prop: 'sourceId', label: 'source_id', width: 100 },
  { prop: 'a', label: 'a', width: 90 },
  { prop: 'b', label: 'b', width: 90 },
  { prop: 'c', label: 'c', width: 90 },
  { prop: 'd', label: 'd', width: 90 },
  { prop: 'e', label: 'e', width: 90 },
  { prop: 'f', label: 'f', width: 90 },
  { prop: 'CW', label: 'CW', width: 90 },
  { prop: 'h', label: 'h', width: 90 },
  { prop: 'i', label: 'i', width: 90 },
  { prop: 'j', label: 'j', width: 90 },
  { prop: 'k', label: 'k', width: 90 },
  { prop: 'L', label: 'L', width: 90 },
  { prop: 'CL', label: 'CL', width: 90 },
  { prop: 'TL', label: 'TL', width: 90 },
  { prop: 'BL', label: 'BL', width: 90 },
  { prop: 'sampleTime', label: 'sample_time', width: 170 }
];

const snailColumns = [
  { prop: 'sampleId', label: 'sample_id', width: 100 },
  { prop: 'sourceId', label: 'source_id', width: 100 },
  { prop: 'SW', label: 'SW', width: 90 },
  { prop: 'W2W', label: 'W2W', width: 90 },
  { prop: 'AW', label: 'AW', width: 90 },
  { prop: 'SWd', label: 'SWd', width: 90 },
  { prop: 'SH', label: 'SH', width: 90 },
  { prop: 'BWH', label: 'BWH', width: 90 },
  { prop: 'W2H', label: 'W2H', width: 90 },
  { prop: 'W1H', label: 'W1H', width: 90 },
  { prop: 'AH', label: 'AH', width: 90 },
  { prop: 'SHt', label: 'SHt', width: 90 },
  { prop: 'sampleTime', label: 'sample_time', width: 170 }
];

onMounted(async () => {
  await Promise.all([
    fetchAppearanceData(),
    fetchGrowthData(),
    fetchTotalDataCount(),
    fetchCrawfishPreview(),
    fetchSnailPreview()
  ]);
});

const fetchAppearanceData = async () => {
  try {
    appearanceData.value = await phenotypeApi.getPopulationAppearance() || [];
  } catch (error) {
    appearanceData.value = [];
    ElMessage.error('加载河蟹群体外观数据失败：' + (error.message || '未知错误'));
  }
};

const fetchGrowthData = async () => {
  try {
    growthData.value = await phenotypeApi.getPopulationGrowth() || [];
  } catch (error) {
    growthData.value = [];
    ElMessage.error('加载河蟹群体生长数据失败：' + (error.message || '未知错误'));
  }
};

const fetchTotalDataCount = async () => {
  try {
    totalDataCount.value = await phenotypeApi.getTotalDataCount() || 0;
  } catch (error) {
    totalDataCount.value = 0;
  }
};

const fetchCrawfishPreview = async () => {
  try {
    crawfishPreview.value = await phenotypeApi.getCrawfishPreview(20) || [];
  } catch (error) {
    crawfishPreview.value = [];
    ElMessage.error('加载小龙虾表型预览失败：' + (error.message || '未知错误'));
  }
};

const fetchSnailPreview = async () => {
  try {
    snailPreview.value = await phenotypeApi.getSnailPreview(20) || [];
  } catch (error) {
    snailPreview.value = [];
    ElMessage.error('加载螺丝表型预览失败：' + (error.message || '未知错误'));
  }
};

const downloadBlob = async (requestFn, fileName, fallbackUrl) => {
  try {
    const response = await requestFn();
    if (response.status !== 200 || !(response.data instanceof Blob) || response.data.size === 0) {
      const text = await new Response(response.data).text();
      throw new Error(text || '文件流异常');
    }
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
    ElMessage.success('下载成功');
  } catch (error) {
    try {
      window.open(fallbackUrl, '_blank');
    } catch (e) {
      ElMessage.error(`下载失败：${error.message || '请联系管理员'}`);
    }
  }
};

const downloadTotalData = () => downloadBlob(
  phenotypeApi.downloadTotalData,
  'Eriocheir_sinensis_phenotype_total.xlsx',
  '/phenotype/total/download'
);

const downloadCrawfishData = () => downloadBlob(
  phenotypeApi.downloadCrawfishData,
  'Procambarus_clarkii_phenotype.xlsx',
  '/phenotype/crawfish/download'
);

const downloadSnailData = () => downloadBlob(
  phenotypeApi.downloadSnailData,
  'Snail_phenotype.xlsx',
  '/phenotype/snail/download'
);

const handleSchemeError = () => {
  schemeError.value = true;
  schemeLoading.value = false;
  ElMessage.error('表型指标示意图加载失败');
};
</script>

<style scoped>
.phenotype-container {
  width: 100%;
  max-width: 1920px;
  margin: 0 auto;
  padding: 10px 20px;
  box-sizing: border-box;
}

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

.module-tabs {
  margin-top: 12px;
}

.main-layout {
  display: flex !important;
  flex-direction: row !important;
  gap: 15px;
  flex-wrap: nowrap;
  align-items: flex-start;
  width: 100%;
  min-height: 600px;
}

.layout-left {
  flex: 0 0 30% !important;
  max-width: 450px;
  min-width: 350px;
  box-sizing: border-box;
}

.layout-right {
  flex: 1 !important;
  min-width: 550px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.card-wrapper {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
  background: #fff;
  box-sizing: border-box;
  width: 100%;
}

.mb-20 {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 8px;
}

.svg-container {
  width: 100%;
  min-height: 400px;
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.svg-embed {
  width: 100%;
  max-height: 400px;
  object-fit: contain;
}

.crawfish-figure-container {
  min-height: auto;
  aspect-ratio: 1.93 / 1;
  background: #000;
  border-radius: 4px;
}

.crawfish-figure-image {
  height: 100%;
  max-height: none;
}

.remark-text {
  margin-top: 15px;
  padding: 10px;
  font-size: 12px;
  line-height: 1.7;
  color: #333;
  border-top: 1px solid #f0f0f0;
}

.remark-text p {
  margin: 6px 0;
  text-align: justify;
}

.table-scroll {
  width: 100%;
  overflow-x: auto;
}

.el-table {
  width: 100% !important;
  font-size: 12px;
}

.el-table__cell {
  padding: 5px 0;
}

.download-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 15px 0;
}

.download-btn {
  min-width: 320px;
  height: 45px;
  font-size: 15px;
}

.download-tip {
  color: #666;
  font-size: 13px;
  margin: 0;
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
  margin: 50px 0;
}

@media (max-width: 900px) {
  .main-layout {
    flex-wrap: wrap !important;
  }

  .layout-left,
  .layout-right {
    flex: 0 0 100% !important;
    min-width: 100%;
  }
}
</style>
