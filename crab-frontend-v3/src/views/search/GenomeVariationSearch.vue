<template>
  <div class="variation-search-container">
    <h1 class="page-title">Genome Variation Search</h1>
    <el-card class="search-card ">
      <el-tabs v-model="activeTab" type="card">
        <!-- 标签页1：按基因查询 -->
        <el-tab-pane label="Search by Gene" name="gene">
          <el-form :model="geneForm" label-width="150px" class="mt-4">
            <el-form-item label="Gene ID">
              <el-input
                  v-model="geneForm.geneId"
                  placeholder="Enter gene ID (e.g. LOC127007452)"
                  style="width: 300px"
              ></el-input>
            </el-form-item>
            <el-form-item label="Flanking Region">
              <el-row :gutter="30">
                <el-col :span="10">
                  <el-input-number
                      v-model="geneForm.upstream"
                      :min="0"
                      :max="10000"
                      label="Upstream (bp)"
                  ></el-input-number>
                </el-col>
                <el-col :span="10">
                  <el-input-number
                      v-model="geneForm.downstream"
                      :min="0"
                      :max="10000"
                      label="Downstream (bp)"
                  ></el-input-number>
                </el-col>
              </el-row>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchByGene">Search</el-button>
              <el-button @click="resetGeneForm">Reset</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 标签页2：按染色体位置查询 -->
        <el-tab-pane label="Search by Chromosome Position" name="chrPos">
          <el-form :model="chrForm" label-width="150px" class="mt-4">
            <el-form-item label="Chromosome">
              <el-select
                  v-model="chrForm.chr"
                  placeholder="Select chromosome"
                  style="width: 300px"
              >
                <!-- 从后端加载所有染色体（示例选项，实际需接口获取） -->
                <el-option label="chr1" value="chr1"></el-option>
                <el-option label="chr2" value="chr2"></el-option>
                <el-option label="chr3" value="chr3"></el-option>
                <el-option label="chr4" value="chr4"></el-option>
                <el-option label="chr5" value="chr5"></el-option>
                <el-option label="chr6" value="chr6"></el-option>
                <el-option label="chr7" value="chr7"></el-option>
                <el-option label="chr8" value="chr8"></el-option>
                <el-option label="chr9" value="chr9"></el-option>
                <el-option label="chr10" value="chr10"></el-option>
                <el-option label="chr11" value="chr11"></el-option>
                <el-option label="chr12" value="chr12"></el-option>
                <el-option label="chr13" value="chr13"></el-option>
                <el-option label="chr14" value="chr14"></el-option>
                <el-option label="chr15" value="chr15"></el-option>
                <el-option label="chr16" value="chr16"></el-option>
                <el-option label="chr17" value="chr17"></el-option>
                <el-option label="chr18" value="chr18"></el-option>
                <el-option label="chr19" value="chr19"></el-option>
                <el-option label="chr20" value="chr20"></el-option>
                <el-option label="chr21" value="chr21"></el-option>
                <el-option label="chr22" value="chr22"></el-option>
                <el-option label="chr23" value="chr23"></el-option>
                <el-option label="chr24" value="chr24"></el-option>
                <el-option label="chr25" value="chr25"></el-option>
                <el-option label="chr26" value="chr26"></el-option>
                <el-option label="chr27" value="chr27"></el-option>
                <el-option label="chr28" value="chr28"></el-option>
                <el-option label="chr29" value="chr29"></el-option>
                <el-option label="chr30" value="chr30"></el-option>
                <el-option label="chr31" value="chr31"></el-option>
                <el-option label="chr32" value="chr32"></el-option>
                <el-option label="chr33" value="chr33"></el-option>
                <el-option label="chr34" value="chr34"></el-option>
                <el-option label="chr35" value="chr35"></el-option>
                <el-option label="chr36" value="chr36"></el-option>
                <el-option label="chr37" value="chr37"></el-option>
                <el-option label="chr38" value="chr38"></el-option>
                <el-option label="chr39" value="chr39"></el-option>
                <el-option label="chr40" value="chr40"></el-option>
                <el-option label="chr41" value="chr41"></el-option>
                <el-option label="chr42" value="chr42"></el-option>
                <el-option label="chr43" value="chr43"></el-option>
                <el-option label="chr44" value="chr44"></el-option>
                <el-option label="chr45" value="chr45"></el-option>
                <el-option label="chr46" value="chr46"></el-option>
                <el-option label="chr47" value="chr47"></el-option>
                <el-option label="chr48" value="chr48"></el-option>
                <el-option label="chr49" value="chr49"></el-option>
                <el-option label="chr50" value="chr50"></el-option>
                <el-option label="chr51" value="chr51"></el-option>
                <el-option label="chr52" value="chr52"></el-option>
                <el-option label="chr53" value="chr53"></el-option>
                <el-option label="chr54" value="chr54"></el-option>
                <el-option label="chr55" value="chr55"></el-option>
                <el-option label="chr56" value="chr56"></el-option>
                <el-option label="chr57" value="chr57"></el-option>
                <el-option label="chr58" value="chr58"></el-option>
                <el-option label="chr59" value="chr59"></el-option>
                <el-option label="chr60" value="chr60"></el-option>
                <el-option label="chr61" value="chr61"></el-option>
                <el-option label="chr62" value="chr62"></el-option>
                <el-option label="chr63" value="chr63"></el-option>
                <el-option label="chr64" value="chr64"></el-option>
                <el-option label="chr65" value="chr65"></el-option>
                <el-option label="chr66" value="chr66"></el-option>
                <el-option label="chr67" value="chr67"></el-option>
                <el-option label="chr68" value="chr68"></el-option>
                <el-option label="chr69" value="chr69"></el-option>
                <el-option label="chr70" value="chr70"></el-option>

                <!-- ... 补全所有染色体 -->
              </el-select>
            </el-form-item>
            <el-form-item label="Position Range">
              <el-row :gutter="20">
                <el-col :span="10">
                  <el-input
                      v-model="chrForm.start"
                      placeholder="Start position (e.g. 10000)"
                      type="number"
                  ></el-input>
                </el-col>
                <el-col :span="2" class="text-center">~</el-col>
                <el-col :span="10">
                  <el-input
                      v-model="chrForm.end"
                      placeholder="End position (e.g. 20000)"
                      type="number"
                  ></el-input>
                </el-col>
              </el-row>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchByChrPos">Search</el-button>
              <el-button @click="resetChrForm">Reset</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <!-- 🔴 新增：SVG展示区域（搜索栏下方，标签页上方） -->
      <div class="svg-display-area">
        <!-- 第一张SVG：染色体FST值展示 -->
        <div class="svg-item">
          <h3 class="svg-title">Chromosome Fst</h3>
          <img
              src="@/assets/svg/chromosome_fst.svg"
              alt="Chromosome FST Distribution"
              class="svg-image"
          >
        </div>
        <!-- 第二张SVG：染色体Pi值展示 -->
        <div class="svg-item">
          <h3 class="svg-title">Chromosome Pi</h3>
          <img
              src="@/assets/svg/chromosome_pi.svg"
              alt="Chromosome Pi Distribution"
              class="svg-image"
          >
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'GenomeVariationResults',
  data() {
    return {
      activeTab: "gene",  // 默认选中基因查询
      // 基因查询表单
      geneForm: {
        geneId: "",
        upstream: 1000,    // 默认上游1000bp
        downstream: 1000   // 默认下游1000bp
      },
      // 染色体位置查询表单
      chrForm: {
        chr: "",
        start: null,
        end: null
      }
    };
  },
  methods: {
    // 重置基因表单
    resetGeneForm() {
      this.geneForm = { geneId: "", upstream: 1000, downstream: 1000 };
    },
    // 重置染色体表单
    resetChrForm() {
      this.chrForm = { chr: "", start: null, end: null };
    },
    // 按基因查询：跳转结果页，携带参数
    searchByGene() {
      if (!this.geneForm.geneId.trim()) {
        return this.$message.error("Please enter a gene ID");
      }
      this.$router.push({
        name: "GenomeVariationResults",
        query: {
          type: "gene",
          geneId: this.geneForm.geneId.trim(),
          upstream: this.geneForm.upstream,
          downstream: this.geneForm.downstream,
          page: 1,
          size: 20
        }
      });
    },
    // 按染色体位置查询：跳转结果页，携带参数
    searchByChrPos() {
      if (!this.chrForm.chr || !this.chrForm.start || !this.chrForm.end) {
        return this.$message.error("Please fill all chromosome position fields");
      }
      if (this.chrForm.start >= this.chrForm.end) {
        return this.$message.error("Start position must be less than end");
      }
      this.$router.push({
        name: "GenomeVariationResults",
        query: {
          type: "chrPos",
          chr: this.chrForm.chr,
          start: this.chrForm.start,
          end: this.chrForm.end,
          page: 1,
          size: 20
        }
      });
    }
  }
};
</script>

<style scoped>
.variation-search-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}
.page-title {
  font-size: 24px;
  margin-bottom: 20px;
  border-bottom: 2px solid #409eff;
  padding-bottom: 10px;
}
.search-card {
  margin-bottom: 20px;
}
.svg-display-area {
  margin: 20px 0; /* 上下间距20px，与搜索栏、标签页分隔开 */
  display: flex;
  flex-direction: column;
  gap: 30px; /* 两张SVG之间的垂直间距30px */
}

.svg-item {
  width: 100%; /* 适配父容器宽度 */
}

.svg-title {
  font-size: 16px;
  color: #303133;
  margin-bottom: 10px;
  font-weight: 500;
}

.svg-image {
  width: 100%; /*  SVG宽度占满父容器，响应式适配 */
  height: auto; /* 高度自动，避免拉伸变形 */
  border-radius: 4px; /* 轻微圆角，与页面风格统一 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05); /* 轻微阴影，提升视觉层次 */
}

/* 响应式适配：小屏幕下调整间距 */
@media (max-width: 768px) {
  .svg-display-area {
    gap: 20px;
    margin: 15px 0;
  }
  .svg-title {
    font-size: 14px;
  }
}
</style>