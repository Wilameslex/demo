<template>
  <div class="phenotype-container">
    <!-- 页面标题 -->
    <div class="page-header" v-once>
      <h1><i class="el-icon-menu"></i> Phenotype Data</h1>
      <p class="sub-title">Overview of the phenotype data from each Chinese mitten crab population</p>
    </div>

    <!-- 核心布局：左侧示意图 + 右侧数据 -->
    <div class="main-layout" v-if="appearanceData.length > 0 || growthData.length > 0">
      <!-- 左侧：示意图 + 文字说明（固定宽） -->
      <div class="layout-left">
        <div class="card-wrapper">
          <div class="card-header">
            <h3>Diagram of all Phenotype Indicators</h3>
          </div>
          <!-- 示意图容器 -->
          <div class="svg-container">
            <img
                src="@/assets/phenotype/phenotype_scheme.png"
                alt="Diagram of all Phenotype Indicators"
                class="svg-embed"
                v-loading="schemeLoading"
                element-loading-text="loading..."
                @error="handleSchemeError"
            >
            <div class="resource-error" v-if="schemeError">
              <el-icon class="error-icon"><WarningFilled /></el-icon>
              <span>示意图加载失败</span>
            </div>
          </div>

          <!-- 新增：文字说明 -->
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


      <!-- 右侧：数据展示（外观数据 + 生长数据 + 下载按钮） -->
      <div class="layout-right">
        <!-- 1. 群体外观数据表格 -->
        <div class="card-wrapper mb-20">
          <div class="card-header">
            <h3>Appearance Data</h3>
            <el-tag type="info"> {{ appearanceData.length }} populations in total </el-tag>
          </div>
          <!-- 横向滚动适配多列（参考Mitochondrion） -->
          <div class="table-scroll">
            <el-table
                :data="appearanceData"
                stripe
                border
                highlight-current-row
                style="width: 100%"
                max-height="300"
                size="small"
            >
              <el-table-column
                  prop="population"
                  label="Population"
                  width="150"
                  align="center"
              ></el-table-column>
              <!-- 外观指标列（A1-S7） -->
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

        <!-- 2. 群体生长数据表格 -->
        <div class="card-wrapper mb-20">
          <div class="card-header">
            <h3>Growth Traits Data</h3>
            <el-tag type="info"> {{ growthData.length }} populations in total </el-tag>
          </div>
          <el-table
              :data="growthData"
              stripe
              border
              highlight-current-row
              style="width: 100%"
              max-height="250"
              size="small"
          >
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

        <!-- 3. 总表型数据下载按钮 -->
        <div class="card-wrapper">
          <div class="download-container">
            <h3>All Raw Data</h3>
            <el-button
                type="primary"
                size="medium"
                icon="el-icon-download"
                @click="downloadTotalData"
                class="download-btn"
                :disabled="totalDataCount === 0"
            >
              Download all raw phenotype data（Excel）
            </el-button>
            <p class="download-tip"> {{ totalDataCount }} records in total </p>
          </div>
        </div>
      </div>
    </div>

    <!-- 无数据提示 -->
    <el-empty
        description="暂无表型数据"
        class="empty-tip"
        v-else
    ></el-empty>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { phenotypeApi } from '@/api/phenotype';
import { ElMessage, ElEmpty } from 'element-plus';
import { WarningFilled } from '@element-plus/icons-vue';

// 响应式数据
const appearanceData = ref([]); // 群体外观数据
const growthData = ref([]);     // 群体生长数据
const totalDataCount = ref(0);  // 总表型数据条数（用于提示）
const schemeLoading = ref(false); // 示意图加载状态
const schemeError = ref(false);   // 示意图加载错误

// 页面初始化：加载所有数据
onMounted(async () => {
  await Promise.all([
    fetchAppearanceData(),
    fetchGrowthData(),
    fetchTotalDataCount()
  ]);
  // 单独获取条数，失败不影响其他功能
  try {
    const res = await phenotypeApi.getTotalDataCount();
    totalDataCount.value = res || 0;
  } catch (error) {
    totalDataCount.value = '未知'; // 兜底显示，不影响下载
    ElMessage.warning('获取数据条数失败，不影响下载功能');
  }
});

// 加载群体外观数据
const fetchAppearanceData = async () => {
  try {
    const res = await phenotypeApi.getPopulationAppearance();
    appearanceData.value = res || [];
  } catch (error) {
    ElMessage.error('加载群体外观数据失败：' + (error.message || '未知错误'));
    appearanceData.value = [];
  }
};

// 加载群体生长数据
const fetchGrowthData = async () => {
  try {
    const res = await phenotypeApi.getPopulationGrowth();
    growthData.value = res || [];
  } catch (error) {
    ElMessage.error('加载群体生长数据失败：' + (error.message || '未知错误'));
    growthData.value = [];
  }
};

// 3. 获取总表型数据条数（后端新增接口，下文会提）
const fetchTotalDataCount = async () => {
  try {
    const res = await phenotypeApi.getTotalDataCount();
    console.log("接口返回的原始值：", res); // 应打印 1004
    totalDataCount.value = res || 0;
    console.log("赋值后totalDataCount的值：", totalDataCount.value); // 应打印 1004
  } catch (error) {
    totalDataCount.value = 0;
    console.log("获取条数失败：", error);
  }
};

// 4. 下载总表型数据（复用Mitochondrion的a标签跳转方式，避免流处理问题）
const downloadTotalData = async () => {
  try {
    ElMessage.info('开始下载，正在获取文件流...');
    // 方式1：通过blob流下载（关键：确保responseType: 'blob'）
    const response = await phenotypeApi.downloadTotalData();
    console.log('=== 下载接口响应信息 ===');
    console.log('响应状态码：', response.status); // 正确：从response对象取status
    console.log('响应头：', response.headers);
    console.log('是否为Blob：', response.data instanceof Blob); // Blob在response.data中
    console.log('Blob大小（字节）：', response.data.size);

    // 验证blob类型 + 大小（空blob可能是后端错误）
    if (response.status !== 200 || !(response.data instanceof Blob) || response.data.size === 0) {
      const text = await new Response(response.data).text();
      console.error('下载失败，后端返回内容：', text);
      throw new Error(`下载失败：${text || '文件流异常'}`);
    }

    // 创建下载链接（正常流程）
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = '河蟹总表型数据.xlsx';
    document.body.appendChild(a);
    a.click();

    // 释放资源
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
    ElMessage.success('下载成功！请在浏览器下载列表查看');
  } catch (error) {
    console.error('=== 下载失败详细错误 ===', error);
    // 备用下载方式（直接跳转接口）
    try {
      ElMessage.warning('尝试备用下载方式...');
      window.open('/phenotype/total/download', '_blank');
    } catch (err) {
      ElMessage.error(`下载失败：${error.message || '请联系管理员'}`);
    }
  }
};

// 示意图加载错误处理
const handleSchemeError = () => {
  schemeError.value = true;
  schemeLoading.value = false;
  ElMessage.error('表型指标示意图加载失败');
};
</script>

<style scoped>
/* 外层容器 */
.phenotype-container {
  width: 100%;
  max-width: 1920px;
  margin: 0 auto;
  padding: 10px 20px;
  box-sizing: border-box;
}

/* 页面标题 */
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

/* 核心左右布局（完全参考Mitochondrion） */
.main-layout {
  display: flex !important;
  flex-direction: row !important;
  gap: 15px;
  flex-wrap: nowrap;
  align-items: flex-start;
  width: 100%;
  min-height: 600px;
}

/* 左侧：固定宽，示意图+文字说明 */
.layout-left {
  flex: 0 0 30% !important; /* 占比从35%缩为30%，给右侧更多空间 */
  max-width: 450px;
  min-width: 350px; /* 最小宽度从400缩为350，适配窄屏 */
  box-sizing: border-box;
}

/* 右侧：自适应宽，内部上下排列 */
.layout-right {
  flex: 1 !important;
  min-width: 550px; /* 最小宽度从600缩为550，适配1080px宽的竖屏 */
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* 卡片样式（复用Mitochondrion） */
.card-wrapper {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
  background: #fff;
  box-sizing: border-box;
  width: 100%;
}
/* 卡片间距 */
.mb-20 {
  margin-bottom: 20px;
}

/* 卡片标题（复用Mitochondrion） */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 8px;
}

/* 示意图容器（复用Mitochondrion） */
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

/* 文字说明样式 */
.remark-text {
  margin-top: 15px;
  padding: 10px;
  font-size: 12px;
  line-height: 1.6;
  color: #333;
  border-top: 1px solid #f0f0f0;
}
.remark-text p {
  margin: 5px 0;
}

/* 表格横向滚动（参考Mitochondrion） */
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

/* 下载按钮容器 */
.download-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 15px 0;
}
.download-btn {
  min-width: 280px;
  height: 45px;
  font-size: 16px;
}
.download-tip {
  color: #666;
  font-size: 13px;
  margin: 0;
}

/* 错误提示（复用Mitochondrion） */
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

/* 无数据提示（复用Mitochondrion） */
.empty-tip {
  margin: 50px 0;
}

/* 响应式：超小屏幕换行（参考Mitochondrion） */
@media (max-width: 900px) {
  .main-layout {
    flex-wrap: wrap !important;
  }
  .layout-left, .layout-right {
    flex: 0 0 100% !important;
    min-width: 100%;
  }
}

/* 确保启用状态的按钮显示蓝色（覆盖自定义样式干扰） */
.download-btn.el-button--primary:not(.is-disabled) {
  background-color: #409eff !important;
  border-color: #409eff !important;
  color: #fff !important;
  cursor: pointer !important;
}

/* 禁用状态保持灰色（可选，确保禁用样式正常） */
.download-btn.el-button--primary.is-disabled {
  background-color: #f5f7fa !important;
  border-color: #e4e7ed !important;
  color: #c0c4cc !important;
  cursor: not-allowed !important;
}
</style>
