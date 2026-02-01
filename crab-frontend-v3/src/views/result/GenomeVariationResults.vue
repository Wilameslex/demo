<template>
  <div class="variation-results-container">
    <h1 class="page-title">Genome Variation Results</h1>

    <!-- 1. 结果区间提示（基因/染色体） -->
    <el-alert
        v-if="resultInfo"
        :message="resultInfo"
        type="info"
        show-icon
        class="mb-4"
    ></el-alert>

    <!-- 新增：基因信息表格 -->
    <el-card
        v-if="geneDataList.length > 0"
        header="Gene Information"
        class="mt-4"
    >
      <el-table
          :data="geneDataList"
          border
          stripe
          style="width: 100%"
      >
        <el-table-column
            prop="gene_id"
            label="Gene ID"
            width="180"
        ></el-table-column>
        <el-table-column
            prop="gene_abbrev"
            label="Gene Abbreviation"
            width="200"
        ></el-table-column>
        <el-table-column
            prop="description"
            label="Description"
        ></el-table-column>
        <el-table-column
            prop="chr"
            label="Chromosome"
            width="100"
        ></el-table-column>
        <el-table-column
            prop="start"
            label="Start Position"
            width="120"
        ></el-table-column>
        <el-table-column
            prop="end"
            label="End Position"
            width="120"
        ></el-table-column>
        <el-table-column
            prop="strand"
            label="Strand"
            width="80"
        ></el-table-column>
      </el-table>
    </el-card>

    <!-- 无数据时显示 -->
    <el-empty
        v-else-if="showGeneDataEmpty"
        description="No gene information found in this range"
        class="mt-4"
    ></el-empty>

    <!-- 基因组结构视图 -->
    <el-card
        v-if="geneStructureData.length > 0"
        header="Genome Structure View"
        class="mb-6"
    >
      <!-- 卡片头部：标题 + 下拉下载按钮 -->
      <template #header>
        <div class="card-header-flex">
          <span>Genome Structure View</span>
          <!-- 下拉下载按钮 -->
          <el-dropdown
              @command="(cmd) => handleChartDownload('gene', cmd)"
              placement="bottom-end"
              trigger="hover"
          >
            <el-button type="primary" size="small">
              Download charts
              <el-icon class="ml-1"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="svg">Download SVG</el-dropdown-item>
                <el-dropdown-item command="pdf">Download PDF</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </template>
      <div
          ref="geneStructureChart"
          :style="{
        height: `${calcChartHeight()}px`,
        width: '100%',
        minHeight: '400px', // 强制最小高度，避免为0
        border: '1px solid #eee' // 临时添加边框，便于观察容器范围
      }"
      ></div>
    </el-card>
    <el-empty
        v-else-if="showGeneStructureEmpty"
        description="No genome structure data in this range"
        class="mb-6"
    ></el-empty>

    <!-- 2. FST散点图区域（优先展示，有数据才渲染容器） -->
    <el-card header="FST Scatter Plot" class="mb-6">
      <!--下载按钮-->
      <template #header>
        <div class="card-header-flex">
          <span>FST {{ isFstBar ? 'Bar Chart' : 'Scatter Plot' }}</span>
          <el-button
              type="primary"
              size="small"
              @click="toggleFstChartType"
              style="margin-right: 10px;"
          >
            {{ isFstBar ? '切换为散点图' : '切换为柱状图' }}
          </el-button>
          <el-dropdown
              @command="(cmd) => handleChartDownload('fst', cmd)"
              placement="bottom-end"
              trigger="hover"
          >
            <el-button type="primary" size="small">
              Download charts
              <el-icon class="ml-1"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="svg">Download SVG</el-dropdown-item>
                <el-dropdown-item command="pdf">Download PDF</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </template>
      <div v-loading="loading" element-loading-text="Loading FST data, please wait...">
        <div 
            v-if="fstMap && Object.keys(fstMap).length > 0" 
            ref="fstChart" 
            class="chart-container">
        </div>
        <el-empty 
            v-else-if="!loading" 
            description="No valid FST data in this range">
        </el-empty>
      </div>
    </el-card>

    <!-- Pi散点图区域 -->
    <el-card header="Pi Scatter Plot" class="mb-6">
      <!--下载按钮-->
      <template #header>
        <div class="card-header-flex">
          <span>Pi {{ isPiBar ? 'Bar Chart' : 'Scatter Plot' }}</span>
          <el-button
              type="primary"
              size="small"
              @click="togglePiChartType"
              style="margin-right: 10px;"
          >
            {{ isPiBar ? 'change to scatter plot' : 'change to bar plot' }}
          </el-button>
          <el-dropdown
              @command="(cmd) => handleChartDownload('pi', cmd)"
              placement="bottom-end"
              trigger="hover"
          >
            <el-button type="primary" size="small">
              Download charts
              <el-icon class="ml-1"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="svg">Download SVG</el-dropdown-item>
                <el-dropdown-item command="pdf">Download PDF</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </template>
      <div v-loading="loading" element-loading-text="Loading Pi data, please wait...">
        <div 
            v-if="piMap && Object.keys(piMap).length > 0" 
            ref="piChart" 
            class="chart-container">
        </div>
        <el-empty 
            v-else-if="!loading" 
            description="No valid Pi data in this range">
        </el-empty>
      </div>
    </el-card>

    <!-- 3. 变异数据表格区域 -->
    <!-- SNP表格 -->
    <el-card class="mb-6" v-if="snps.length || snpTotal > 0">
      <div slot="header" class="card-header">
        <span>SNP Variants</span>
        <span class="total-count">Total: {{ snpTotal }}</span>
        <el-button
            type="success"
            icon="el-icon-download"
            size="small"
        @click="downloadVariant('snp')"
        class="download-btn"
        >
        Download All SNP
        </el-button>
      </div>
      <el-table
          :data="snps"
          border
          stripe
          style="width: 100%"
          v-loading="loading"
          empty-text="No SNP data in this range"
      >
        <el-table-column label="Position" prop="pos" width="180"></el-table-column>
        <el-table-column label="Variant" prop="variant" width="250"></el-table-column>
        <el-table-column label="Ya" prop="ya" width="80" ></el-table-column>
        <el-table-column label="HP" prop="hp" width="80" ></el-table-column>
        <el-table-column label="JP" prop="jp" width="80" ></el-table-column>
        <el-table-column label="LR" prop="lr" width="80" ></el-table-column>
        <el-table-column label="MJ" prop="mj" width="80" ></el-table-column>
        <el-table-column label="RU" prop="ru" width="80" ></el-table-column>
        <el-table-column label="YR" prop="yr" width="80" ></el-table-column>
        <el-table-column label="Type" prop="variantType" width="100"></el-table-column>
      </el-table>
      <div class="table-note" v-if="snps.length < snpTotal">
        Showing first 10 results. Click "Download All SNP" to get complete data.
      </div>
    </el-card>

    <!-- INDEL表格 -->
    <el-card class="mb-6" v-if="indels.length || indelTotal > 0">
      <div slot="header" class="card-header">
        <span>INDEL Variants</span>
        <span class="total-count">Total: {{ indelTotal }}</span>
        <el-button
            type="success"
            icon="el-icon-download"
            size="small"
            @click="downloadVariant('indel')"
            class="download-btn"
        >
          Download All INDEL
        </el-button>
      </div>
      <el-table
          :data="indels"
          border
          stripe
          style="width: 100%"
          v-loading="loading"
          empty-text="No INDEL data in this range"
      >
        <el-table-column label="Position" prop="pos" width="180"></el-table-column>
        <el-table-column label="Variant" prop="variant" width="250"></el-table-column>
        <el-table-column label="Ya" prop="ya" width="80" ></el-table-column>
        <el-table-column label="HP" prop="hp" width="80" ></el-table-column>
        <el-table-column label="JP" prop="jp" width="80" ></el-table-column>
        <el-table-column label="LR" prop="lr" width="80" ></el-table-column>
        <el-table-column label="MJ" prop="mj" width="80" ></el-table-column>
        <el-table-column label="RU" prop="ru" width="80" ></el-table-column>
        <el-table-column label="YR" prop="yr" width="80" ></el-table-column>
        <el-table-column label="Type" prop="variantType" width="100"></el-table-column>
      </el-table>
      <div class="table-note" v-if="indels.length < indelTotal">
        Showing first 10 results. Click "Download All INDEL" to get complete data.
      </div>
    </el-card>

    <!-- SV表格 -->
    <el-card class="mb-6" v-if="svs.length || svTotal > 0">
      <div slot="header" class="card-header">
        <span>SV Variants</span>
        <span class="total-count">Total: {{ svTotal }}</span>
        <el-button
            type="success"
            icon="el-icon-download"
            size="small"
            @click="downloadVariant('sv')"
            class="download-btn"
        >
          Download All SV
        </el-button>
      </div>
      <el-table
          :data="svs"
          border
          stripe
          style="width: 100%"
          v-loading="loading"
          empty-text="No SV data in this range"
      >
        <el-table-column label="Position" prop="pos" width="180"></el-table-column>
        <el-table-column label="Variant" prop="variant" width="250"></el-table-column>
        <el-table-column label="Ya" prop="ya" width="80" ></el-table-column>
        <el-table-column label="HP" prop="hp" width="80" ></el-table-column>
        <el-table-column label="JP" prop="jp" width="80" ></el-table-column>
        <el-table-column label="LR" prop="lr" width="80" ></el-table-column>
        <el-table-column label="MJ" prop="mj" width="80" ></el-table-column>
        <el-table-column label="RU" prop="ru" width="80" ></el-table-column>
        <el-table-column label="YR" prop="yr" width="80" ></el-table-column>
        <el-table-column label="Type" prop="variantType" width="100"></el-table-column>
      </el-table>
      <div class="table-note" v-if="svs.length < svTotal">
        Showing first 10 results. Click "Download All SV" to get complete data.
      </div>
    </el-card>

    <!-- 无任何变异数据提示 -->
    <el-alert
        v-if="!snps.length && !indels.length && !svs.length && !loading"
        title="No variation data found in this range"
        type="warning"
        show-icon
    ></el-alert>
  </div>
</template>

<script>
import * as echarts from 'echarts';
// 新增：引入markRaw，让ECharts实例脱离响应式
import { markRaw } from 'vue';
import jsPDF from 'jspdf';
import { ArrowDown } from '@element-plus/icons-vue';


export default {
  components: {
    ArrowDown // 注册下拉箭头图标
  },
  name: 'GenomeVariationResults',
  data() {
    return {
      loading: false,
      currentGene: {}, // 当前选中的基因
      transcriptColors: ['#ff7f50', '#4682b4', '#3cb371', '#9932cc', '#ffd700'],
      // 新增：存储基因整合信息
      geneDataList: [],
      // 控制空状态显示（避免初始加载时闪烁）
      showGeneDataEmpty: false,
      // 新增：基因组结构数据和实例
      geneStructureData: [],
      geneStructureChartInstance: null,
      showGeneStructureEmpty: false,
      fstMap: {},          // FST分组数据（后端返回，按群体对分组）
      //fstChartInstance: null, // ECharts实例，用于销毁和resize
      // 新增：Pi数据
      piMap: {},
      //piChartInstance: null,
      isChartInitializing: false, // 新增：图表初始化锁
      // 变异数据（后端返回，前10行+总数）
      snps: [],    snpTotal: 0,
      indels: [],  indelTotal: 0,
      svs: [],     svTotal: 0,
      resultInfo: '',       // 结果区间提示文本
      queryParams: {},       // 路由传递的查询参数（type/chr/start等）
      isFstBar: false, // FST图表类型
      isPiBar: false,  // Pi图表类型
      // 🔴 新增：存储计算后的均值数据（柱状图用）
      fstMeanData: [], // FST各群体对的均值：[{name: "CJvsMJ", value: 0.23}, ...]
      piMeanData: []   // Pi各群体的均值：[{name: "CJ", value: 0.08}, ...]
    };
  },
  created() {
    // 1. 从路由获取查询参数（页面加载时初始化）
    this.queryParams = { ...this.$route.query };
    // 2. 加载变异和FST数据
    this.loadResults();
    // 新增：加载基因整合信息
    this.loadGeneData();
  },
  mounted() {
    console.log("DOM has been mounted, start charts initializing");
    // 初始化实例变量（非响应式）
    this.fstChartInstance = null;
    this.piChartInstance = null;
    // 🔴 统一绑定一次Resize事件
    window.addEventListener('resize', this.handleChartResize);
    // 确保基因数据加载完成后，再初始化基因结构图
    if (this.geneStructureData.length > 0) {
      this.initGeneStructureChart();
    } else {
      // 若数据未加载完成，监听数据变化后再初始化
      this.$watch('geneStructureData', (newVal) => {
        if (newVal.length > 0 && newVal[0].transcripts.length > 0) {
          this.initGeneStructureChart();
        }
      }, { immediate: true });
    }
    // 初始化FST和Pi图表（DOM已就绪）
    if (this.fstMap && Object.keys(this.fstMap).length > 0) {
      this.initFstChart();
    }
    if (this.piMap && Object.keys(this.piMap).length > 0) {
      this.initPiChart();
    }
  },

  beforeUnmount() {
    // 移除窗口resize事件监听
    window.removeEventListener('resize', this.handleChartResize);
    if (this.geneStructureChartInstance) {
      this.geneStructureChartInstance.dispose();
    }
    if (this.fstChartInstance) {
      this.fstChartInstance.dispose();
    }
    if (this.piChartInstance) {
      this.piChartInstance.dispose();
    }
  },
  watch: {
    geneStructureData: {
      handler(newVal) {
        // 核心修复：先验证 newVal[0] 存在，再判断 transcripts
        if (newVal.length > 0 && newVal[0] && newVal[0].transcripts?.length > 0) {
          this.currentGene = newVal[0];
          this.showGeneStructureEmpty = false;
          console.log("currentGene assignment:", this.currentGene);
          setTimeout(() => {
            this.$nextTick().then(() => {
              this.initGeneStructureChart();
            });
          }, 100);
        } else {
          // 打印详细日志，定位 newVal 结构问题
          console.log("Current gene has no data(newVal):", newVal);
          this.showGeneStructureEmpty = true;
        }
      },
      immediate: true
    }
  },
  methods: {
    calcChartHeight() {
      // 计算所有基因的转录本总数
      const transcriptTotal = this.geneStructureData.reduce(
          (total, gene) => total + gene.transcripts.length,
          0
      );
      return Math.max(transcriptTotal * 60, 400);
    },
    /**
     * 核心：加载变异数据（SNP/INDEL/SV）和FST数据
     */
    async loadResults() {
      this.loading = true;
      try {
        // 将start/end转为数字（路由参数默认是字符串）
        this.queryParams.start = Number(this.queryParams.start);
        this.queryParams.end = Number(this.queryParams.end);
        let response, resultData = {};

        // 分场景请求后端接口
        if (this.queryParams.type === 'gene') {
          // 按基因查询：调用/gene接口
          response = await this.$axios.get('/api/variant/by-gene', {
            params: {
              geneId: this.queryParams.geneId,
              upstream: this.queryParams.upstream || 1000,
              downstream: this.queryParams.downstream || 1000,
              size: 10
            }
          });
          resultData = response.data.data || {};
          const gene = resultData[0] || {}; // 假设返回单个基因对象

          // 关键：同步基因的染色体区间到queryParams
          if (gene.chr && gene.start && gene.end) {
            this.queryParams.chr = gene.chr;
            this.queryParams.start = gene.start;
            this.queryParams.end = gene.end;
          }

          // 组装基因区间提示
          const geneCoord = resultData.geneCoord || {};
          const geneStruct = this.geneStructureData[0] || {};
          // ########### 关键修改：同步基因区间到queryParams ###########
          if (geneCoord.chr && geneCoord.startExtend && geneCoord.endExtend) {
            this.queryParams.chr = geneCoord.chr || geneStruct.chr || '';// 同步染色体
            this.queryParams.start = geneCoord.startExtend || geneStruct.start || 0; // 同步扩展后的起始位置
            this.queryParams.end = geneCoord.endExtend || geneStruct.end || 0; // 同步扩展后的结束位置
          }
          // ##########################################################

          // 组装基因区间提示（逻辑不变）
          this.resultInfo = `Gene: ${this.queryParams.geneId} (${this.queryParams.chr}: ${this.queryParams.start}-${this.queryParams.end})`;
        } else if (this.queryParams.type === 'chrPos') {
          // 按染色体查询：调用/by-chr-pos接口（复用Service统筹逻辑）
          response = await this.$axios.get('/api/variant/by-chr-pos', {
            params: {
              chr: this.queryParams.chr,
              start: this.queryParams.start,
              end: this.queryParams.end,
              variantType: 'all',
              size: 10
            }
          });
          resultData = response.data.data || {};
          // 组装染色体区间提示
          this.resultInfo = `Chromosome Range: ${this.queryParams.chr}: ${this.queryParams.start}-${this.queryParams.end}`;
        }

        // 1. 解析变异数据（SNP/INDEL/SV）
        this.snps = resultData.snps || [];
        // console.log("SNP第一条数据结构：", this.snps[0]);
        this.snpTotal = resultData.snpTotal || 0;
        this.indels = resultData.indels || [];
        // console.log("indel第一条数据结构：", this.indels[0]);
        this.indelTotal = resultData.indelTotal || 0;
        this.svs = resultData.svs || [];
        // console.log("sv第一条数据结构：", this.svs[0]);
        this.svTotal = resultData.svTotal || 0;
        // 2. 解析FST，pi数据 + 初始化图表（关键：等待DOM渲染后执行）
        this.fstMap = resultData.fstMap || {};
        this.piMap = resultData.piMap || {};
        // 新增3行日志，查看数据是否解析成功
        // console.log("FST数据（解析后）:", this.fstMap);
        // console.log("Pi数据（解析后）:", this.piMap);
         await this.$nextTick(); // 等待el-card（图表容器）渲染完成
         this.initFstChart();
         this.initPiChart();

        console.log("Variant/FST/Pi finish loading");


      } catch (error) {
        // 错误处理：兼容后端不同错误格式
        const errorMsg = error.response?.data?.error || error.response?.data?.message || error.message;
        this.$message.error(`Load failed: ${errorMsg}`);
        // 异常时清空数据，避免残留旧值
        this.clearAllData();
        this.resultInfo = `Load failed: ${errorMsg}`;
      } finally {
        this.loading = false;
        this.isChartInitializing = false;
      }
    },

    // 新增：加载基因整合信息
    async loadGeneData() {
      try {
        console.log("current queryParams：", this.queryParams); // 确认chr、start、end是否存在
        this.showGeneDataEmpty = false; // 重置空状态
        this.showGeneStructureEmpty = false; // 新增
        let response;

        if (this.queryParams.type === 'gene') {
          // 场景1：搜索基因（按gene_id查询）
          response = await this.$axios.get('/api/gene-data/by-name-with-exons', {
            params: { geneName: this.queryParams.geneId }
          });
        } else if (this.queryParams.type === 'chrPos') {
          // 场景2：搜索染色体范围（按区间查询）
          response = await this.$axios.get('/api/gene-data/by-region', {
            params: {
              chr: this.queryParams.chr,
              start: Number(this.queryParams.start),
              end: Number(this.queryParams.end)
            }
          });
        }

        this.geneDataList = response.data || [];
        // 新增：打印exons数据，确认是否获取成功
        console.log("gene data from api:", this.geneDataList);
        console.log("raw gene data:", this.geneDataList); // 查看原始chr、start、end
        console.log("query chromosome:", this.queryParams.chr); // 新增日志，对比同步前后

        this.showGeneDataEmpty = this.geneDataList.length === 0;

        // 严格过滤有效基因数据（确保gene_id、start、end存在且有效）
        this.geneStructureData = this.geneDataList
            .filter(gene =>
                gene.gene_id &&
                !isNaN(Number(gene.start)) &&
                !isNaN(Number(gene.end)) &&
                Number(gene.start) < Number(gene.end)
            )
            .map(gene => {
              const validTranscripts = (gene.transcripts || [])
                  .map(transcript => ({
                    transcript_id: transcript.transcript_id || 'Unknown',
                    exons: (transcript.exons || [])
                        .map(exon => ({
                          exon_id: exon.exon_id || 'Unknown',
                          start: Number(exon.start),
                          end: Number(exon.end)
                        }))
                        .filter(exon => !isNaN(exon.start) && !isNaN(exon.end) && exon.start < exon.end)
                  }))
                  .filter(transcript => transcript.exons.length > 0); // 只保留有外显子的转录本

              // 新增日志：验证每个基因的有效转录本数量
              console.log(`gene ${gene.gene_id} transcripts :`, validTranscripts.length);
              return {
                geneId: gene.gene_id,
                name: gene.description || gene.gene_id,
                chr: gene.chr,
                start: Number(gene.start),
                end: Number(gene.end),
                strand: gene.strand || '+',
                gene_abbrev: gene.gene_abbrev || '-',
                transcripts: validTranscripts
              };
            });

        console.log("Final data of gene structure", this.geneStructureData);
        console.log("Filtered data of gene structure:", this.geneStructureData); // 此时应不为空

        this.showGeneStructureEmpty = this.geneStructureData.length === 0;

        // ##########################################################
        // 关键修改1：移到这里！在图表初始化前同步queryParams参数
        // ##########################################################
        if (this.geneStructureData.length > 0) {
          const gene = this.geneStructureData[0];
          this.queryParams.chr = this.queryParams.chr || gene.chr; // 优先用已有参数，没有则用基因的chr
          this.queryParams.start = this.queryParams.start || gene.start; // 同步基因的start
          this.queryParams.end = this.queryParams.end || gene.end; // 同步基因的end
          // console.log("查询参数同步后:", this.queryParams); // 新增日志，确认参数已更新
        }

        // 初始化基因组结构图表（此时参数已同步完成）
        // await this.$nextTick();
        // this.initGeneStructureChart();

        // console.log("基因数据加载完成，等待DOM挂载后初始化图表");

      } catch (error) {
        // console.error('加载基因信息失败:', error);
        this.$message.error('fail to load gene data');
        // 错误时重置空状态，避免报错
        this.geneDataList = [];
        this.geneStructureData = [];
        this.showGeneDataEmpty = true;
        this.showGeneStructureEmpty = true;
      } finally {
        // ##########################################################
        // 关键修改2：删除finally块内的参数同步逻辑（已移到上面）
        // 只保留空状态处理
        // ##########################################################
        this.$nextTick().then(() => {
          console.log("final geneStructureData:", this.geneStructureData);
          if (this.geneStructureData.length === 0) {
            this.showGeneStructureEmpty = true;
          }
        });
      }
    },

    initGeneStructureChart() {
      // 重置空状态，释放初始化锁（避免死锁）
      this.showGeneStructureEmpty = false;
      if (this.isChartInitializing) return;
      this.isChartInitializing = true;

      // 1. 容器校验
      const chartDom = this.$refs.geneStructureChart;
      if (!chartDom) {
        console.warn('miss the container of geneStructureChart');
        this.showGeneStructureEmpty = true;
        this.isChartInitializing = false; // 释放锁
        return;
      }

      // 2. 尺寸校验
      if (chartDom.offsetWidth <= 0 || chartDom.offsetHeight <= 0) {
        console.warn('geneStructureChart is invalid');
        this.showGeneStructureEmpty = true;
        this.isChartInitializing = false; // 释放锁
        // 尝试100ms后重新初始化（避免DOM未就绪）
        setTimeout(() => this.initGeneStructureChart(), 100);
        return;
      }

      // 3. 校验基因数据
      const geneStructureData = this.geneStructureData;
      if (geneStructureData.length === 0) {
        console.warn('invalid gene data');
        this.showGeneStructureEmpty = true;
        this.isChartInitializing = false;
        return;
      }

      // 4. 初始化ECharts实例（复用或新建）
      if (!this.geneStructureChartInstance) {
        this.geneStructureChartInstance = markRaw(echarts.init(chartDom));
      } else {
        this.geneStructureChartInstance.clear(); // 清除旧数据
      }

      // 5. ✅ 遍历所有基因数据（支持多基因显示)
      // 收集所有基因的所有转录本ID
      const allTranscripts = [];
      geneStructureData.forEach(gene => {
        if (gene.transcripts && gene.transcripts.length > 0) {
          gene.transcripts.forEach(transcript => {
            allTranscripts.push({
              transcript_id: transcript.transcript_id,
              gene_id: gene.geneId,
              exons: transcript.exons
            });
          });
        }
      });

      if (allTranscripts.length === 0) {
        this.renderEmptyState([], chartDom, geneStructureData[0]);
        this.isChartInitializing = false;
        return;
      }

      // 6. 准备基础数据（Y轴为所有转录本ID）
      const transcriptIds = allTranscripts.map(t => t.transcript_id);
      const yAxisData = transcriptIds; // Y轴数据为所有转录本ID

      // ########## ✅ 修复：收集所有基因的所有外显子坐标 ##########
      const allStartValues = [];
      const allEndValues = [];

      // 遍历所有基因
      geneStructureData.forEach(gene => {
        allStartValues.push(Number(gene.start));
        allEndValues.push(Number(gene.end));
        
        // 遍历该基因的所有转录本
        if (gene.transcripts && gene.transcripts.length > 0) {
          gene.transcripts.forEach(transcript => {
            if (transcript.exons && transcript.exons.length > 0) {
              transcript.exons.forEach(exon => {
                allStartValues.push(Number(exon.start));
                allEndValues.push(Number(exon.end));
              });
            }
          });
        }
      });

      // 计算xMin/xMax（确保有默认值，避免NaN）
      const xMin = allStartValues.length > 0 ? Math.min(...allStartValues) : Number(geneStructureData[0].start) - 1000;
      const xMax = allEndValues.length > 0 ? Math.max(...allEndValues) : Number(geneStructureData[0].end) + 1000;
      // 修复：padding至少为1000，避免范围过小（原代码padding可能因xMax-xMin小导致可视区窄）
      const padding = Math.max((xMax - xMin) * 0.05, 1000); // 兜底1000px边距
      const adjustedXMin = xMin - padding;
      const adjustedXMax = xMax + padding;

      // ########## 新增日志：确认x轴范围 ##########
      console.log(`x axis：adjustedXMin=${adjustedXMin}, adjustedXMax=${adjustedXMax}`);
      console.log(`exons: min=${Math.min(...allStartValues)}, max=${Math.max(...allEndValues)}`);

      // 7. 校验是否有有效外显子
      const allExons = allTranscripts.flatMap(t => t.exons).filter(e => !isNaN(Number(e.start)) && !isNaN(Number(e.end)) && e.start < e.end);
      const hasValidExon = allExons.length > 0;
      if (!hasValidExon) {
        console.warn('none transcripts has vaild exons (tart >= end)');
        this.renderEmptyState([], chartDom, geneStructureData[0]);
        this.isChartInitializing = false;
        return;
      }

      // 8. 构建系列数据（基因条 + 外显子 + 连接线）
      const series = [];

      // 8.1 绘制基因范围条（Custom系列，无格式冲突）
      const geneDisplayStart = Math.max(xMin, adjustedXMin);
      const geneDisplayEnd = Math.min(xMax, adjustedXMax);
      series.push({
        type: 'custom',
        name: 'gene-range',
        data: [{
          start: geneDisplayStart,
          end: geneDisplayEnd,
          color: '#e6f7ff',
          opacity: 0.8
        }],
        coordinateSystem: 'cartesian2d',
        z: 1, // 层级最低，不遮挡外显子
        renderItem: (params, api) => {
          const data = params.data || {};
          // ✅ 使用 api.coord() 转换坐标
          const startPoint = api.coord([data.start, 0]);
          const endPoint = api.coord([data.end, yAxisData.length - 1]);
          
          return {
            type: 'rect',
            shape: {
              x: startPoint[0],
              y: startPoint[1],
              width: endPoint[0] - startPoint[0],
              height: endPoint[1] - startPoint[1]
            },
            style: {
              fill: data.color,
              opacity: data.opacity
            }
          };
        }
      });

      // 8.2 按转录本绘制外显子和连接线
      allTranscripts.forEach((transcript, yIndex) => {
        const transcriptId = transcript.transcript_id;
        if (!transcript || !transcript.exons || transcript.exons.length === 0) {
          console.warn(`transcripts ${transcriptId} have none exons, skip`);
          return;
        }

        // 处理外显子数据（过滤无效值 + 截断到可视范围）
        let exons = transcript.exons
            .map(exon => ({
              ...exon,
              start: Number(exon.start),
              end: Number(exon.end),
              displayStart: Math.max(Number(exon.start), adjustedXMin),
              displayEnd: Math.min(Number(exon.end), adjustedXMax)
            }))
            .filter(exon => !isNaN(exon.start) && !isNaN(exon.end) && exon.start > 0 && exon.end > 0 && exon.displayStart < exon.displayEnd)
            .sort((a, b) => a.displayStart - b.displayStart);

        if (exons.length === 0) {
          console.warn(`exons ${transcriptId} is over the visible range,skip`);
          return;
        }

        // 构建外显子渲染数据（终极过滤）
        const exonCustomData = transcript.exons
            .map(exon => {
              // 强制转数字，避免NaN
              const displayStart = Number(exon.start) || 0;
              const displayEnd = Number(exon.end) || 0;
              return {
                value: [displayStart, yIndex], // ECharts需要的坐标值
                x: displayStart,
                width: displayEnd - displayStart,
                // 🔴 强制兜底info和id，确保始终存在
                info: {
                  id: exon.exon_id || `exon-${transcriptId}-${Date.now()}`, // 用转录本ID+时间戳兜底
                  transcriptId: transcriptId,
                  start: exon.start, // 补充原始start（可选，用于tooltip）
                  end: exon.end      // 补充原始end（可选，用于tooltip）
                }
              };
            })
            .filter(item => !isNaN(item.x));

        if (exonCustomData.length === 0) {
          console.warn(`exons of ${transcriptId} has invalid data,skip`);
          return;
        }

        // 添加外显子Custom系列
        series.push({
          type: 'custom',
          name: transcriptId,
          data: exonCustomData,
          coordinateSystem: 'cartesian2d',
          z: 3,
          // 🔴 使用闭包传递 exonCustomData 和 yIndex
          renderItem: (function(customData, yIdx) {
            return (params, api) => {
              // 通过 dataIndex 从闭包中的 customData 获取数据
              const dataIndex = params.dataIndex;
              const data = customData[dataIndex];


              // 防御性判断：避免数据缺失
              if (!data || !data.info || !data.info.id) {
                console.error("lack exons data:", data);
                return null;
              }

              // 从数据中提取坐标信息
              const startPos = Number(data.x) || 0;
              const endPos = startPos + (Number(data.width) || 0);
              const color = data.color || '#ff0000'; // 亮红色

              // ✅ 使用 api.coord() 将数据坐标转换为像素坐标
              const startPoint = api.coord([startPos, yIdx]);
              const endPoint = api.coord([endPos, yIdx]);
              const height = 16; // 固定16px高度，避免不同转录本的外显子形成竖直矩形

              return {
                type: 'rect',
                shape: {
                  x: startPoint[0],
                  y: startPoint[1] - 8, // 固定偏移8px（高度的一半），所有外显子在同一垂直位置
                  width: endPoint[0] - startPoint[0],
                  height: height
                },
                style: {
                  fill: color,
                  stroke: '#000',
                  lineWidth: 1
                }
              };
            };
          })(exonCustomData, yIndex), // 传递完整数据数组和Y索引
          tooltip: {
            formatter: params => {
              const info = params.data.info;
              return `
        <div style="font-weight: bold;">exon ${info.id}</div>
        <div>转录本: ${info.transcriptId}</div>
        <div>位置: ${info.start || 'known'} - ${info.end || 'unknown'}</div>
      `;
            }
          }
        });

        // 8.3 绘制外显子连接线（内含子）
        if (exons.length > 1) {
          // 为每对相邻外显子绘制连接线
          for (let i = 0; i < exons.length - 1; i++) {
            const currentExon = exons[i];
            const nextExon = exons[i + 1];

            // 连接线：当前外显子的结束位置 → 下一个外显子的起始位置
            const lineData = [
              [currentExon.displayEnd, yIndex],
              [nextExon.displayStart, yIndex]
            ];

            console.log(`draw the link line ${i}:`, {
              from: currentExon.displayEnd,
              to: nextExon.displayStart,
              yIndex: yIndex,
              lineData: lineData
            });

            series.push({
              type: 'line',
              name: `${transcriptId}-intron-${i}`,
              data: lineData,
              coordinateSystem: 'cartesian2d',
              lineStyle: {
                color: '#000',    // 黑色实线
                width: 2,         // 线宽
                type: 'solid'     // 实线
              },
              symbol: 'none',     // 不显示端点符号
              z: 2,               // 在外显子下方
              tooltip: { show: false },
              silent: true        // 不响应鼠标事件
            });
          }
        } else {
          console.warn(`${transcriptId} has ${exons.length} exon, link line would not be drawed`);
        }
      });

      // 9. 配置图表选项（使用统一的x轴范围，确保三图联动）
      const xAxisRange = this.calculateXAxisRange();
      const gridConfig = {
        left: 10, // 与FST/Pi保持一致，消除左侧空白
        right: 30,
        top: 40,
        bottom: 40,
        containLabel: true
      };
      console.log('🔵 基因结构图 x轴范围:', xAxisRange);
      console.log('🔵 基因结构图 grid配置:', gridConfig);
      const option = {
        tooltip: { trigger: 'item' },
        xAxis: {
          type: 'value',
          name: 'Genomic Position (bp)',
          min: xAxisRange.min,
          max: xAxisRange.max,
          boundaryGap: false,
          axisLabel: {
            formatter: value => value.toLocaleString(),
            rotate: 45,
            fontSize: 12,
          },
          splitLine: { show: true, lineStyle: { type: 'dashed', color: '#eee' } },
          nameTextStyle: { fontSize: 14, fontWeight: 500 }
        },
        yAxis: {
          type: 'category',
          data: yAxisData,
          axisLabel: {
            fontSize: 11,
            truncate: { maxWidth: 180, ellipsis: '...' }
          },
          splitLine: { show: true, lineStyle: { type: 'solid', color: '#f5f5f5' } },
          nameTextStyle: { fontSize: 14, fontWeight: 500 }
        },
        series: series,
        grid: gridConfig,
        legend: { show: false }
      };

      // 10. 渲染图表（捕获异常）
      try {
        this.geneStructureChartInstance.setOption(option, true);
        this.geneStructureChartInstance.resize();
        this.showGeneStructureEmpty = false;
      } catch (renderError) {
        console.error('基因结构图渲染失败，100ms后重试:', renderError);
        this.$message.error('基因结构图加载中...');
        this.showGeneStructureEmpty = false;
        this.isChartInitializing = false;
        // 🔴 重试前销毁旧实例，避免堆积
        if (this.geneStructureChartInstance) {
          this.geneStructureChartInstance.dispose();
          this.geneStructureChartInstance = null;
        }
        setTimeout(() => this.initGeneStructureChart(), 100);
        return;
      }


      // 11. 绑定窗口resize事件
      window.removeEventListener('resize', this.handleChartResize); // 先移除旧绑定
      window.addEventListener('resize', this.handleChartResize); // 确保绑定的是统一的handleChartResize

      // 12. 释放初始化锁
      this.isChartInitializing = false;
    },


// ##########################################################
// 保留并优化原有的renderEmptyState（适配多转录本空状态）
// ##########################################################
    renderEmptyState(series, chartDom, gene, isOutOfRange = false) {
      const chartInstance = this.geneStructureChartInstance;
      if (!chartInstance) return;
      const geneName = gene?.name || gene?.geneId || 'Unknown Gene';
      const geneStart = Number(gene?.start) || 0;
      const geneEnd = Number(gene?.end) || 1000;
      const xMin = geneStart - 1000;
      const xMax = geneEnd + 1000;
      const emptySeries = [];

      // 空状态基因条：改用 Custom 系列（避免 Bar 格式错误）
      if (gene) {
        emptySeries.push({
          type: 'custom',
          name: 'empty-gene-range',
          data: [{
            x: geneStart,
            width: geneEnd - geneStart,
            height: 0.8,
            color: '#e6f7ff',
            opacity: 0.8
          }],
          coordinateSystem: 'cartesian2d',
          z: 1,
          renderItem: (params, api) => {
            const data = params.data || {};
            return {
              type: 'rect',
              shape: { x: data.x, y: 0, width: data.width, height: data.height },
              style: { fill: data.color, opacity: data.opacity }
            };
          }
        });
      }

      // 空状态文本（居中显示）
      emptySeries.push({
        type: 'scatter',
        name: 'empty',
        data: [[(xMin + xMax) / 2, 0]],
        label: {
          show: true,
          formatter: isOutOfRange ? '所有外显子均超出当前查询范围' : '无有效外显子数据',
          fontSize: 14,
          color: '#999',
          position: 'top',
          fontWeight: 500
        },
        symbolSize: 0
      });

      // 空状态配置
      chartInstance.setOption({
        xAxis: {
          type: 'value',
          name: 'Genomic Position (bp)',
          min: xMin,
          max: xMax,
          axisLabel: { formatter: val => val.toLocaleString() },
          splitLine: { show: true, lineStyle: { type: 'dashed' } }
        },
        yAxis: {
          type: 'category',
          data: [geneName],
          axisLabel: { fontSize: 12 }
        },
        series: emptySeries,
        grid: { left: '18%', right: '5%', top: '8%', bottom: '20%', containLabel: true }
      }, true);
      chartInstance.resize();
    },



    /**
     * 初始化FST散点图（ECharts）
     */
    initFstChart() {
      const chartDom = this.$refs.fstChart;
      if (!chartDom) return;

      // 销毁旧实例
      if (this.fstChartInstance) {
        this.fstChartInstance.dispose();
        this.fstChartInstance = null;
      }
      this.fstChartInstance = markRaw(echarts.init(chartDom, null, { renderer: 'canvas' }));

      // 数据校验
      if (this.isFstBar) {
        this.calculateFstMean();
        if (this.fstMeanData.length === 0) {
          this.renderEmptyChart(chartDom, 'No valid FST mean data', 'fst');
          return;
        }
      } else {
        if (!this.fstMap || Object.keys(this.fstMap).length === 0) {
          this.renderEmptyChart(chartDom, 'No valid FST data', 'fst');
          return;
        }
      }

      const FST_POPULATION_COLOR_MAP = {
        "HPvsJP": "#ff9f40", "YavsHP": "#38F527", "YavsYR": "#36a2eb",
        "HPvsRU": "#ffce56", "YavsRU": "#9966ff", "YavsLR": "#2738F5",
        "YavsJP": "#4bc0c0", "YavsMJ": "#ff6384"
      };

      let seriesData = [];
      let yAxisConfig = {
        type: 'value',
        name: 'FST Value',
        min: -0.2,
        max: 0.2,
        axisLabel: { formatter: '{value}' },
        splitLine: { show: true, lineStyle: { type: 'dashed' } }
      };

      // 散点图/柱状图配置
      if (!this.isFstBar) {
        seriesData = this.formatFstData();
        seriesData = seriesData.filter(series => series.data && series.data.length > 0);
        if (seriesData.length === 0) {
          this.renderEmptyChart(chartDom, 'No valid FST scatter data', 'fst');
          return;
        }
      } else {
        // 🔴 修复1：增大barWidth，确保柱子显示
        const barWidth = '45%';
        // 🔴 修复2：保证x轴data与series顺序完全一致
        const xAxisData = this.fstMeanData.map(item => item.name);
        const barData = this.fstMeanData.map(item => item.value);
        const barColors = this.fstMeanData.map(item => FST_POPULATION_COLOR_MAP[item.name] || this.getRandomColor());
        seriesData = [{
          name: 'FST',
          type: 'bar',
          data: barData,
          itemStyle: {
            color: (params) => barColors[params.dataIndex], // 按索引对应颜色
            opacity: 0.8
          },
          barWidth: '40%',
          tooltip: {
            formatter: (params) => `${xAxisData[params.dataIndex]}<br/>平均FST: ${params.value.toFixed(4)}`
          }
        }];
      }

      // 🔴 修复3：减小grid.left，消除左侧空白
      const fstXAxisRange = this.calculateXAxisRange();
      const fstGridConfig = {
        left: 30, // 从220→120，大幅减少左侧空白
        right: 30,
        top: 60,
        bottom: 80,
        containLabel: true
      };
      const xAxisData = this.isFstBar ? this.fstMeanData.map(item => item.name) : [];

      const option = {
        tooltip: { trigger: 'item' },
        // 🔴 修复4：图例移到右侧，不挤压x轴
        legend: {
          data: seriesData.map(item => item.name),
          bottom: 20, // 底部20px，避免贴边
          left: 'center', // 水平居中
          orient: 'horizontal', // 水平排列
          textStyle: { fontSize: 10 }, // 缩小字体避免重叠
          itemWidth: 12, // 缩小图例项宽度
          itemHeight: 12,
          formatter: (name) => name.length > 8 ? name.slice(0,8)+'...' : name, // 超长名称截断
          show: seriesData.length > 0
        },
        xAxis: {
          type: this.isFstBar ? 'category' : 'value',
          data: xAxisData,
          name: this.isFstBar ? 'Population Pairs' : 'Position (bp)',
          min: this.isFstBar ? undefined : fstXAxisRange.min,
          max: this.isFstBar ? undefined : fstXAxisRange.max,
          nameGap: 20, // 增大x轴name与label的距离，避免交叉
          axisTick: {
            show: true, // 强制显示刻度线
            length: 6, // 刻度线长度，确保可见
            lineStyle: { color: '#666' } // 刻度线颜色，避免与背景融合
          },
          // 🔴 修复5：调整标签旋转/对齐，适配长度
          axisLabel: {
            show: true,
            formatter: this.isFstBar ? (val) => val : (val) => val.toLocaleString(),
            rotate: 45, // 从60→45，减少左侧占用
            fontSize: 11,
            align: 'center', // 居中对齐，避免偏右
            interval: 0,
            margin: 25,
            minInterval: 1
          },
          splitLine: { show: true, lineStyle: { type: 'dashed', color: '#eee' } }
        },
        yAxis: yAxisConfig,
        grid: fstGridConfig,
        series: seriesData
      };

      try {
        this.fstChartInstance.setOption(option, true);
        this.fstChartInstance.resize();
      } catch (error) {
        console.error('FST图表渲染失败:', error);
        this.renderEmptyChart(chartDom, 'Failed to render FST chart', 'fst');
      }

      window.addEventListener('resize', this.handleChartResize);
    },

    /**
     * 初始化Pi散点图（ECharts）
     */
    initPiChart() {
      const chartDom = this.$refs.piChart;
      if (!chartDom) return;

      // 销毁旧实例
      if (this.piChartInstance) {
        this.piChartInstance.dispose();
        this.piChartInstance = null;
      }
      this.piChartInstance = markRaw(echarts.init(chartDom, null, { renderer: 'canvas' }));

      // 数据校验
      if (this.isPiBar) {
        this.calculatePiMean();
        if (this.piMeanData.length === 0) {
          this.renderEmptyChart(chartDom, 'No valid Pi mean data', 'pi');
          return;
        }
      } else {
        if (!this.piMap || Object.keys(this.piMap).length === 0) {
          this.renderEmptyChart(chartDom, 'No valid Pi data', 'pi');
          return;
        }
      }

      const PI_POPULATION_COLOR_MAP = {
        "RU": "#2738F5", "LR": "#36a2eb", "JP": "#ff9f40",
        "HP": "#ffce56", "Ya": "#4bc0c0", "YR": "#ff6384", "MJ": "#9966ff"
      };

      let seriesData = [];
      let yAxisConfig = {
        type: 'value',
        name: 'Pi Value',
        min: 0,
        max: 0.2,
        axisLabel: { formatter: '{value}' },
        splitLine: { show: true, lineStyle: { type: 'dashed' } }
      };

      // 散点图/柱状图配置
      if (!this.isPiBar) {
        seriesData = this.formatPiData();
        seriesData = seriesData.filter(series => series.data && series.data.length > 0);
        if (seriesData.length === 0) {
          this.renderEmptyChart(chartDom, 'No valid Pi scatter data', 'pi');
          return;
        }
      } else {
        // 🔴 修复1：增大barWidth
        const barWidth = '50%';
        // 🔴 修复2：保证x轴与series顺序一致
        const xAxisData = this.piMeanData.map(item => item.name);
        const barData = this.piMeanData.map(item => item.value);
        const barColors = this.piMeanData.map(item => PI_POPULATION_COLOR_MAP[item.name] || this.getRandomColor());
        seriesData = [{
          name: 'Pi',
          type: 'bar',
          data: barData,
          itemStyle: {
            color: (params) => barColors[params.dataIndex],
            opacity: 0.8
          },
          barWidth: '45%',
          tooltip: {
            formatter: (params) => `${xAxisData[params.dataIndex]}<br/>平均Pi: ${params.value.toFixed(4)}`
          }
        }];
      }

      // 🔴 修复3：减小grid.left，消除左侧空白
      const piXAxisRange = this.calculateXAxisRange();
      const piGridConfig = {
        left: 30, // 从200→100，减少左侧空白
        right: 30,
        top: 60,
        bottom: 80,
        containLabel: true
      };
      const xAxisData = this.isPiBar ? this.piMeanData.map(item => item.name) : [];

      const option = {
        tooltip: { trigger: 'item' },
        // 🔴 修复4：图例移到右侧
        legend: {
          data: seriesData.map(item => item.name),
          bottom: 20, // 底部20px，避免贴边
          left: 'center', // 水平居中
          orient: 'horizontal', // 水平排列
          textStyle: { fontSize: 10 }, // 缩小字体避免重叠
          itemWidth: 12, // 缩小图例项宽度
          itemHeight: 12,
          formatter: (name) => name.length > 8 ? name.slice(0,8)+'...' : name, // 超长名称截断
          show: seriesData.length > 0
        },
        xAxis: {
          type: this.isPiBar ? 'category' : 'value',
          data: xAxisData,
          name: this.isPiBar ? 'Populations' : 'Position (bp)',
          min: this.isPiBar ? undefined : piXAxisRange.min,
          max: this.isPiBar ? undefined : piXAxisRange.max,
          nameGap: 20,
          axisTick: {
            show: true,
            length: 6,
            lineStyle: { color: '#666' }
          },
          // 🔴 修复5：调整标签布局
          axisLabel: {
            formatter: this.isFstBar ? (val) => val : (val) => val.toLocaleString(),
            rotate: 30, // 从45→30，减少左侧占用
            fontSize: 11,
            align: 'center',
            interval: 0,
            margin: 25,
            minInterval: 1
          },
          splitLine: { show: true, lineStyle: { type: 'dashed', color: '#eee' } }
        },
        yAxis: yAxisConfig,
        grid: piGridConfig,
        series: seriesData
      };

      try {
        this.piChartInstance.setOption(option, true);
        this.piChartInstance.resize();
      } catch (error) {
        console.error('Pi图表渲染失败:', error);
        this.renderEmptyChart(chartDom, 'Failed to render Pi chart', 'pi');
      }

      window.addEventListener('resize', this.handleChartResize);
    },

    /**
     * 计算各FST群体对的平均值（用于柱状图）
     */
    calculateFstMean() {
      const fstMean = [];
      // 遍历FST群体对数据
      Object.entries(this.fstMap).forEach(([populationPair, fstList]) => {
        // 过滤有效FST值（排除NaN、超出[-1,1]范围的数据）
        const validFst = fstList.filter(item => {
          const val = item.weirAndCockerhamFst;
          return typeof val === 'number' && !isNaN(val) && val >= -1 && val <= 1;
        }).map(item => item.weirAndCockerhamFst);

        if (validFst.length === 0) {
          fstMean.push({ name: populationPair, value: 0 }); // 无数据时填充0
          return;
        }

        // 计算均值（保留4位小数）
        const mean = validFst.length > 0
            ? (validFst.reduce((sum, val) => sum + val, 0) / validFst.length).toFixed(4)
            : '0'; // 避免NaN
        fstMean.push({
          name: populationPair,
          value: Number(mean) // 确保转成数值
        });
      });

      this.fstMeanData = fstMean;
      return fstMean;
    },

    /**
     * 计算各Pi群体的平均值（用于柱状图）
     */
    calculatePiMean() {
      const piMean = [];
      // 遍历Pi群体数据
      Object.entries(this.piMap).forEach(([population, piList]) => {
        // 过滤有效Pi值（排除NaN、超出[0,0.6]范围的数据）
        const validPi = piList.filter(item => {
          const val = item.piValue;
          return typeof val === 'number' && !isNaN(val) && val >= 0 && val <= 0.6;
        }).map(item => item.piValue);

        if (validPi.length === 0) {
          piMean.push({ name: population, value: 0 }); // 无数据时填充0
          return;
        }

        // 计算均值（保留4位小数）
        const mean = validPi.length > 0
            ? (validPi.reduce((sum, val) => sum + val, 0) / validPi.length).toFixed(4)
            : '0'; // 避免NaN
        piMean.push({
          name: population,
          value: Number(mean) // 确保转成数值
        });
      });
      this.piMeanData = piMean;
      return piMean;
    },


    /**
     * 处理FST数据：宽表转ECharts散点格式
     */
    formatFstData() {
      const FST_POPULATION_COLOR_MAP = {
        "YavsMJ": "#ff6384",
        "YavsHP": "#38F527",
        "YavsJP": "#4bc0c0",
        "YavsRU": "#9966ff",
        "HPvsRU": "#ffce56",
        "HPvsJP": "#ff9f40",
        "YavsLR": "#2738F5",
        "YavsYR": "#36a2eb",
      };
      const series = [];
      Object.entries(this.fstMap).forEach(([populationPair, fstList]) => {
        // console.log("FST单组数据示例:", populationPair, fstList[0]);
        const validData = fstList
            .filter(item => {
              const fst = item.weirAndCockerhamFst;
              return typeof fst === 'number' && !isNaN(fst) && fst >= -1 && fst <= 1;
            })
            // 修复：用item.weirAndCockerhamFst替代未定义的fst
            .map(item => [Number(item.pos), item.weirAndCockerhamFst]);

        // console.log(`${populationPair} 有效数据量：${validData.length}`);
        if (validData.length > 0) {
          series.push({
            name: populationPair,
            type: 'scatter',
            data: validData,
            symbolSize: 6,
            itemStyle: {
              opacity: 0.8,
              color: FST_POPULATION_COLOR_MAP[populationPair] || this.getRandomColor()
            }
          });
        }
      });
      // console.log("FST系列数据长度：", series.length);
      return series;
    },

    /**
     * 处理Pi数据：宽表转ECharts散点格式（单独方法，仅处理Pi）
     */
    formatPiData() {
      const PI_POPULATION_COLOR_MAP = {
        "YR": "#ff6384", "LR": "#36a2eb", "Ya": "#4bc0c0",
        "MJ": "#9966ff", "HP": "#ffce56", "JP": "#ff9f40", "RU": "#2738F5"
      };
      const series = [];
      // 仅遍历Pi数据（this.piMap）
      Object.entries(this.piMap).forEach(([population, piList]) => {
        const validData = piList
            .filter(item => {
              const pi = item.piValue;
              return typeof pi === 'number' && !isNaN(pi) && pi >= 0 && pi <= 0.6;
            })
            .map(item => [Number(item.pos), item.piValue]);
        // console.log(`${population} Pi有效数据量：${validData.length}`); // 新增日志

        if (validData.length > 0) {
          series.push({
            name: population,
            type: 'scatter',
            data: validData,
            symbolSize: 6,
            itemStyle: {
              opacity: 0.8,
              color: PI_POPULATION_COLOR_MAP[population] || this.getRandomColor()
            }
          });
        }
      });
      // console.log("Pi系列数据长度：", series.length); // 新增日志
      return series;
    },


    /**
     * 计算统一的x轴范围（用于三图联动）
     * 使用查询参数的范围，确保不同查询显示不同结果
     */
    calculateXAxisRange() {
      // 优先使用查询参数的范围（实际查询范围）
      if (this.queryParams.start && this.queryParams.end) {
        const start = Number(this.queryParams.start);
        const end = Number(this.queryParams.end);
        console.log('🔵 使用查询范围作为X轴:', { min: start, max: end });
        return {
          min: start,
          max: end
        };
      }

      // 兜底：如果没有查询参数，使用基因范围
      if (!this.geneStructureData || this.geneStructureData.length === 0) {
        console.warn('⚠️ 无查询参数且无基因数据，使用默认范围');
        return {
          min: 0,
          max: 100000
        };
      }

      const gene = this.geneStructureData[0];
      const allStartValues = [Number(gene.start)];
      const allEndValues = [Number(gene.end)];

      // 收集所有外显子坐标
      gene.transcripts.forEach(transcript => {
        if (transcript.exons && transcript.exons.length > 0) {
          transcript.exons.forEach(exon => {
            allStartValues.push(Number(exon.start));
            allEndValues.push(Number(exon.end));
          });
        }
      });

      const xMin = Math.min(...allStartValues);
      const xMax = Math.max(...allEndValues);
      const padding = Math.max((xMax - xMin) * 0.05, 1000);

      console.log('🔵 使用基因范围作为X轴（兜底）:', { min: xMin - padding, max: xMax + padding });
      return {
        min: xMin - padding,
        max: xMax + padding
      };
    },

    /**
     * 计算自适应的grid配置（用于三图联动，支持响应式）
     */
    calculateGridConfig(containerWidth) {
      const width = containerWidth || 1200;
      // 🔴 增大左边距，启用containLabel
      const left = Math.max(220, Math.floor(width * 0.2)); // 左边距调整为20%或220px
      const right = Math.max(40, Math.floor(width * 0.05));
      const top = 60;
      const bottom = 100; // 增大底部边距容纳x轴标签
      return {
        left,
        right,
        top,
        bottom,
        containLabel: true // 关键：让grid自动调整，容纳所有标签
      };
    },

    /**
     * 处理图表窗口大小变化
     */
    handleChartResize() {
      // 统一处理所有图表的resize
      if (this.geneStructureChartInstance) {
        this.geneStructureChartInstance.resize();
      }
      if (this.fstChartInstance) {
        this.fstChartInstance.resize();
      }
      if (this.piChartInstance) {
        this.piChartInstance.resize();
      }
    },

    /**
     * 辅助：生成随机颜色（区分不同群体对）
     */
    getRandomColor() {
      const colors = [
        '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399',
        '#722ED1', '#13C2C2', '#FF7A45', '#FF4D4F', '#52C41A'
      ];
      return colors[Math.floor(Math.random() * colors.length)];
    },

    /**
     * 切换FST图表类型（散点→柱状/柱状→散点）
     */
    toggleFstChartType() {
      this.isFstBar = !this.isFstBar;
      // 🔴 先销毁旧实例，再重新初始化
      if (this.fstChartInstance) {
        this.fstChartInstance.dispose();
        this.fstChartInstance = null;
      }
      // 等待DOM更新后再初始化（避免容器状态未就绪）
      this.$nextTick(() => {
        if (this.$refs.fstChart) {
          this.initFstChart();
          this.handleChartResize();
        }
      });
    },

    /**
     * 切换Pi图表类型（散点→柱状/柱状→散点）
     */
    togglePiChartType() {
      this.isPiBar = !this.isPiBar;
      if (this.piChartInstance) {
        this.piChartInstance.dispose();
        this.piChartInstance = null;
      }
      this.$nextTick(() => {
        if (this.$refs.piChart) {
          this.initPiChart();
          this.handleChartResize();
        }
      });
    },

    /**
     * 新增：渲染空图表状态（无均值数据时）
     */
    renderEmptyChart(chartDom, description, chartType) {
      // 双重保险：销毁对应旧实例
      if (chartType === 'fst' && this.fstChartInstance) {
        this.fstChartInstance.dispose();
        this.fstChartInstance = null;
      } else if (chartType === 'pi' && this.piChartInstance) {
        this.piChartInstance.dispose();
        this.piChartInstance = null;
      }

      // 强制用canvas渲染空状态图表
      const chartInstance = markRaw(echarts.init(chartDom, null, { renderer: 'canvas' }));
      chartInstance.setOption({
        xAxis: { type: 'category', data: [] },
        yAxis: { type: 'value' },
        series: [],
        graphic: {
          type: 'text',
          left: 'center',
          top: 'center',
          style: { text: description, fontSize: 14, color: '#999' }
        }
      });

      if (chartType === 'fst') {
        this.fstChartInstance = chartInstance;
      } else if (chartType === 'pi') {
        this.piChartInstance = chartInstance;
      }
    },

    /**
     * 统一处理图表下载：接收图表类型和格式
     * @param chartType 图表类型：'gene'（基因组）/'fst'/'pi'
     * @param format 下载格式：'svg'/'pdf'
     */
    handleChartDownload(chartType, format) {
      // 1. 根据图表类型获取对应的 ECharts 实例
      let chartInstance = null;
      let chartName = '';
      switch (chartType) {
        case 'gene':
          chartInstance = this.geneStructureChartInstance;
          chartName = 'genome_structure'; // 文件名前缀
          break;
        case 'fst':
          chartInstance = this.fstChartInstance;
          chartName = 'fst_scatter';
          break;
        case 'pi':
          chartInstance = this.piChartInstance;
          chartName = 'pi_scatter';
          break;
        default:
          this.$message.warning('未知图表类型');
          return;
      }

      // 2. 校验图表实例是否初始化
      if (!chartInstance) {
        this.$message.warning('图表未加载完成，请稍后再试');
        return;
      }

      // 3. 分发到对应格式的下载方法
      if (format === 'svg') {
        this.downloadAsSVG(chartInstance, chartName);
      } else if (format === 'pdf') {
        this.downloadAsPDF(chartInstance, chartName);
      }
    },

    /**
     * 导出为 SVG 矢量图（保真，支持无限放大）
     */
    downloadAsSVG(chartInstance, chartName) {
      try {
        // ECharts 原生方法：直接导出 SVG 字符串（type 设为 'svg'）
        const svgDataUrl = chartInstance.getDataURL({
          type: 'svg',
          backgroundColor: '#fff',
          excludeComponents: ['toolbox']
        });
// 新增：校验DataURL格式
        if (!svgDataUrl || !svgDataUrl.includes(',')) {
          this.$message.error('SVG生成失败，图表数据异常');
          return;
        }
        // 解析 DataURL 并触发下载
        const svgBase64 = svgDataUrl.split(',')[1]; // 去掉 DataURL 前缀
        const svgBlob = atob(svgBase64);
        const arrayBuffer = new ArrayBuffer(svgBlob.length);
        const uint8Array = new Uint8Array(arrayBuffer);
        for (let i = 0; i < svgBlob.length; i++) {
          uint8Array[i] = svgBlob.charCodeAt(i);
        }
        const blob = new Blob([uint8Array], { type: 'image/svg+xml;charset=utf-8' });
        const url = URL.createObjectURL(blob);

        const a = document.createElement('a');
        a.href = url;
        a.download = `${chartName}_${new Date().getTime()}.svg`; // 时间戳防重名
        a.click();

        URL.revokeObjectURL(url); // 释放内存
        this.$message.success('SVG 矢量图下载成功');
      } catch (error) {
        console.error('SVG 下载失败:', error);
        this.$message.error('SVG 下载失败，请重试');
      }
    },

    /**
     * 导出为 PDF 格式（嵌入高清图片，兼容大多数设备）
     */
    downloadAsPDF(chartInstance, chartName) {
      try {
        // 1. 获取图表高清 PNG（2倍像素，避免模糊）
        const imgData = chartInstance.getDataURL('png', 2); // 第二个参数是像素比
        // 2. 初始化 PDF（横向A4，适配图表宽高）
        const pdf = new jsPDF('landscape', 'mm', 'a4'); // landscape：横向，a4：纸张大小
        // 3. 嵌入图片（x:10, y:10 是边距，width:280 是图片宽度，适配A4横向）
        pdf.addImage(imgData, 'PNG', 10, 10, 280, 150);
        // 4. 触发下载
        pdf.save(`${chartName}_${new Date().getTime()}.pdf`);
        this.$message.success('PDF 下载成功');
      } catch (error) {
        console.error('PDF 下载失败:', error);
        this.$message.error('PDF 下载失败，请重试');
      }
    },

    /**
     * 下载指定类型的全部变异数据
     */
    downloadVariant(variantType) {
      // 新增日志：打印当前queryParams的所有参数
      console.log("当前queryParams：", this.queryParams);
      // 1. 校验核心参数是否存在（chr、start、end为必传）
      const requiredParams = ['chr', 'start', 'end'];
      const missingParams = requiredParams.filter(param => !this.queryParams[param]);
      if (missingParams.length > 0) {
        this.$message.error(`下载失败：缺少必要参数${missingParams.join(', ')}`);
        return;
      }

      // 2. 组装参数（确保参数名与后端一致）
      const downloadParams = {
        variantType: variantType, // 变异类型（snp/indel/sv）
        chr: this.queryParams.chr, // 染色体（与后端@RequestParam("chr")匹配）
        start: this.queryParams.start, // 起始位置
        end: this.queryParams.end // 结束位置
      };

      // 3. 打开下载链接（使用encodeURIComponent避免特殊字符问题）
      const queryString = new URLSearchParams(downloadParams).toString();
      window.open(`/api/variant/download?${queryString}`);
    },


    /**
     * 辅助：清空所有数据（异常时调用）
     */
    clearAllData() {
      this.fstMap = {};
      this.snps = []; this.snpTotal = 0;
      this.indels = []; this.indelTotal = 0;
      this.svs = []; this.svTotal = 0;
    }
  }
};
</script>

<style scoped>
.variation-results-container {
  padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  margin-bottom: 20px;
  border-bottom: 2px solid #409eff;
  padding-bottom: 10px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.total-count {
  color: #666;
  font-size: 14px;
}

.download-btn {
  margin-left: auto; /* 右对齐下载按钮 */
}

.table-note {
  color: #666;
  font-size: 12px;
  margin-top: 10px;
  padding-left: 10px;
}

/* 确保图表容器有固定高度，避免空白 */
.chart-container {
  width: 100%;
  height: 400px !important;
  margin-top: 10px;
}

.result-container {
  padding: 20px;
}
.mt-4 {
  margin-top: 16px !important;
}
.el-table {
  font-size: 14px;
}
.el-table .cell {
  white-space: normal; /* 允许描述信息换行 */
  word-break: break-all;
}
.card-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  flex-wrap: nowrap; /* 禁止换行，确保按钮和标题在同一行 */
  overflow: visible; /* 避免按钮被隐藏 */
  padding: 0 5px; /* 增加左右内边距，避免内容贴边 */
}

/* 下拉按钮图标与文字间距 */
.el-button .el-icon {
  margin-left: 4px;
  white-space: nowrap;
}
.card-header-flex span {
  max-width: 50%; /* 标题最多占50%宽度，剩余空间给按钮 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis; /* 文本过长时显示省略号 */
}

/* 下拉菜单选项 hover 样式（可选，与主题统一） */
.el-dropdown-menu__item:hover {
  background-color: #e6f7ff;
  color: #1890ff;
}
</style>