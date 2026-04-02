<template>
  <el-container style="position: relative; min-height: 100vh;">
    <!-- 头部导航栏保持不变 -->
    <el-header style="background: #fff; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);">
      <div style="width: 1200px; margin: 0 auto;">
        <el-row style="display: flex; justify-content: space-between; align-items: center;">
          <el-col :span="5">
            <div style="display: flex; align-items: center;">
              <img src="@/assets/logo.png" alt="EGBD Logo" style="width: 60px; height: 60px; cursor: pointer;">
              <div style="font-size: 30px; font-weight: bold; margin-left: 10px;">EGBD</div>
              <div style="font-size: 8px; font-weight: bold; margin-left: 10px; color: #666;">
                Eriocheir sinensis Genome Breeding Database
              </div>
            </div>
          </el-col>

          <el-col :span="18">
            <el-menu
                router
                mode="horizontal"
                unique-opened
                trigger="hover"
                active-text-color="#409EFF"
            >
              <el-menu-item index="/front/index">
                <span><strong>Home</strong></span>
              </el-menu-item>

              <el-sub-menu index="data">
                <template #title>
                  <span><strong>Data</strong></span>
                </template>

                <!-- 新增：Genome Info 子菜单 -->
                <el-sub-menu index="data/genome-info">
                  <template #title>
                    <span>Genome Info</span>
                  </template>
                  <!-- 后续可添加：Genome Overview 菜单 -->
                  <el-menu-item
                      @click="$router.push({ name: 'GenomeOverview' })"
                      index="data/genome-info/overview"
                  >
                    Genome Overview
                  </el-menu-item>
                  <!-- 线粒体基因组菜单 -->
                  <el-menu-item
                      @click="$router.push({ name: 'MitochondrionGenome' })"
                      index="data/genome-info/mitochondrion"
                  >
                    Mitochondrion Genome
                  </el-menu-item>
                </el-sub-menu>

                <el-menu-item @click="goToGeneSearch" index="data/gene-info">
                  Gene Info
                </el-menu-item>

                <el-sub-menu index="data/expression">
                  <template #title>
                    <span>Expression</span>
                  </template>
                  <el-menu-item @click="goToExpressionSearch" index="data/expression/transcriptome">
                    Transcriptome
                  </el-menu-item>
                  <el-menu-item
                      @click="$router.push({ name: 'ProcessExpressionSearch' })"
                      index="data/process-expression"
                  >
                    Process Expression
                  </el-menu-item>
                  <el-menu-item
                      @click="$router.push({ name: 'GeneNetworkSearch' })"
                      index="data/expression/gene-network">
                    Gene Network
                  </el-menu-item>
                </el-sub-menu>

                <el-menu-item @click="goToPathwaySearch" index="data/pathway">
                  Pathway
                </el-menu-item>
                <el-menu-item  @click="goToGenomeVariationSearch"  index="/data/genome-variation">
                  Genome Variation
                </el-menu-item>
                <el-menu-item
                    @click="$router.push({ name: 'PhenotypeData' })"
                    index="data/phenotype"
                >
                  Phenotype Data
                </el-menu-item>
              </el-sub-menu>

              <el-sub-menu index="tools">
                <template #title>
                  <span><strong>Tools</strong></span>
                </template>
                <el-menu-item index="/front/tools/sequence-fetch">
                  Sequence Fetch
                </el-menu-item>
                <el-menu-item index="/front/tools/gene-search">
                  Gene Search
                </el-menu-item>
                <el-menu-item @click="goToEnrichment" index="tools/enrichment">
                  GO/KEGG Enrichment
                </el-menu-item>
              </el-sub-menu>

              <el-menu-item @click="goToDownloadCenter" index="/download">
                <span><strong>Download</strong></span>
              </el-menu-item>
              <el-sub-menu index="about">
                <template #title>
                  <span><strong>About</strong></span>
                </template>
                <!-- 修正 index 为完整路径 /front/about/contact -->
                <el-menu-item index="/front/about/contact">
                  Contact us
                </el-menu-item>
                <!-- 修正 index 为完整路径 /front/about/help -->
                <el-menu-item index="/front/about/help">
                  Help
                </el-menu-item>
              </el-sub-menu>
            </el-menu>
          </el-col>

<!--          <el-col :span="4">-->
<!--            <div style="display: flex; justify-content: right; align-items: center;">-->
<!--              <el-avatar-->
<!--                  :size="30"-->
<!--                  shape="circle"-->
<!--                  :src="img"-->
<!--              ></el-avatar>-->

<!--              <el-dropdown>-->
<!--                <span class="el-dropdown-link" style="cursor: pointer; color: #409EFF; margin-left: 10px;">-->
<!--                  管理员-->
<!--                  <i class="el-icon-arrow-down el-icon&#45;&#45;right"></i>-->
<!--                </span>-->
<!--                <template #dropdown>-->
<!--                  <el-dropdown-menu>-->
<!--                    <el-dropdown-item>个人信息</el-dropdown-item>-->
<!--                    <el-dropdown-item>修改密码</el-dropdown-item>-->
<!--                    <el-dropdown-item>退出登录</el-dropdown-item>-->
<!--                  </el-dropdown-menu>-->
<!--                </template>-->
<!--              </el-dropdown>-->
<!--            </div>-->
<!--          </el-col>-->
        </el-row>
      </div>
    </el-header>

    <!-- 核心修改：添加路由判断 + 子路由渲染出口 -->
    <div style="width: 1200px; margin: 0 auto; padding: 30px 0 100px;">
      <!-- 1. 静态内容：仅在首页（/front/index）显示 -->
      <div v-if="isHomePage">
        <!-- 第一部分：介绍文字和图片 -->
        <el-row :gutter="30" style="margin-bottom: 50px;">
          <el-col :span="16">
            <div class="intro-section">
              <h1 style="color: #409EFF; margin-bottom: 20px;">Welcome to EGBD</h1>
              <p style="line-height: 1.8; text-align: justify;">
                The Chinese mitten crab (<i>Eriocheir sinensis</i>) belongs to the genus <i>Eriocheir</i>, family Varunidae, order Decapoda, class Malacostraca. As a representative and economically important aquaculture crustacean in China, it holds substantial commercial value and broad industrial prospects. In 2022, our group updated the high-quality reference genome of <i>E. sinensis</i> (BioProject: 
                <el-link type="primary" href="https://www.ncbi.nlm.nih.gov/bioproject/PRJNA737102/" target="_blank" underline="hover">group
                  PRJNA737102
                </el-link>) and deposited it in the NCBI database. To support genetic research and molecular breeding of this vital aquaculture species, we constructed the Eriocheir sinensis Genome Breeding Database (EGBD), an integrated omics platform based on long-term accumulated multi-omics datasets. This database integrates comprehensive phenotypic records, high-quality genome assemblies, transcriptomic profiles, and genome-wide genetic variations across diverse geographical populations of the Chinese mitten crab. Additionally, EGBD provides a suite of practical analysis tools, including 
                <el-link type="primary" @click="goToGeneSearch" underline="hover"><strong>Gene Search</strong></el-link>, 
                <el-link type="primary" @click="goToEnrichment" underline="hover"><strong>KEGG/GO Enrichment Analysis</strong></el-link>, 
                <el-link type="primary" href="/front/tools/genome-browser" underline="hover"><strong>Genome Browser</strong></el-link>. Detailed descriptions of data resources and user guidelines are available on the 
                <el-link type="primary" @click="$router.push('/front/about/help')" underline="hover"><strong>Help</strong></el-link> page.
              </p>
              <p style="line-height: 1.8; margin-top: 15px;">
              <strong>Note: This database is strictly for academic and non-commercial use only!</strong>
              </p>
              <p style="line-height: 1.8;">
              <strong>Hope the EGBD can help your Study!</strong>
              </p>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="image-section">
              <img
                  src="@/assets/crab_image.jpg"
                  alt="Chinese Mitten Crab"
                  style="width: 100%; border-radius: 8px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);"
              >
              <p style="text-align: center; margin-top: 10px; color: #666;">
                Chinese Mitten Crab (<i>Eriocheir sinensis</i>)
              </p>
            </div>
          </el-col>
        </el-row>

        <!-- ===================== 新增：种质资源展示模块 ===================== -->
<el-row style="margin-bottom: 50px;">
  <el-col :span="24">
    <div class="germplasm-section">
      <h2 style="color: #409EFF; border-bottom: 2px solid #409EFF; padding-bottom: 10px; margin-bottom: 10px;">
        The mitten crab germplasm source we have collected
      </h2>
      <p style="color: #666; font-size: 13px; margin-bottom: 20px;">
        As of January 2026
      </p>

      <!-- 表格开始 -->
      <div class="germplasm-table-wrapper">
        <table class="germplasm-table">
          <thead>
            <tr>
              <th colspan="3">Populations from China</th>
              <th colspan="3">Populations from Other Region</th>
            </tr>
            <tr>
              <th>Germplasm Source</th>
              <th>Number of Sampling</th>
              <th>Number of Samples</th>
              <th>Germplasm Source</th>
              <th>Number of Sampling</th>
              <th>Number of Samples</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>Tumen River</td>
              <td>1</td>
              <td>30</td>
              <td>Hokkaido, Japan and other regions</td>
              <td>8</td>
              <td>98</td>
            </tr>
            <tr>
              <td>Liao River</td>
              <td>6</td>
              <td>209</td>
              <td>Vladivostok, Russia</td>
              <td>4</td>
              <td>82</td>
            </tr>
            <tr>
              <td>Yellow River</td>
              <td>4</td>
              <td>213</td>
              <td>Elbe River, Germany</td>
              <td>1</td>
              <td>51</td>
            </tr>
            <tr>
              <td>Yangtz River</td>
              <td>25</td>
              <td>1691</td>
              <td>Rhine River, the Netherlands</td>
              <td>1</td>
              <td>40</td>
            </tr>         
            <tr>
              <td>Ou River</td>
              <td>1</td>
              <td>40</td>
              <td>San Francisco Bay, the USA</td>
              <td>1</td>
              <td>30</td>
            </tr>
            <tr>
              <td>Min River</td>
              <td>6</td>
              <td>243</td>
              <td>Thames River, the UK</td>
              <td>1</td>
              <td>29</td>
            </tr>
            <tr>
              <td>Nanliu River</td>
              <td>7</td>
              <td>135</td>
              <td>Kagoshima, Japan</td>
              <td>1</td>
              <td>21</td>
            </tr>
            <tr>
              <td>Pearl River</td>
              <td>1</td>
              <td>40</td>
              <td>North Korea</td>
              <td>1</td>
              <td>4</td>
            </tr>
            <tr>
              <td>Xinjiang Uygur Autonomous Region</td>
              <td>5</td>
              <td>93</td>
              <td></td>
              <td></td>
              <td></td>
            </tr>
            <tr>
              <td>Ningxia Hui Autonomous Region</td>
              <td>41</td>
              <td>1242</td>
              <td></td>
              <td></td>
              <td></td>
            </tr>
            <tr>
              <td>3-year-old Crabs</td>
              <td>1</td>
              <td>27</td>
              <td></td>
              <td></td>
              <td></td>
            </tr>
            <tr>
              <td>National Crab Competition</td>
              <td>785</td>
              <td>12342</td>
              <td></td>
              <td></td>
              <td></td>
            </tr>
            <tr>
              <td>Cultivated Populations (including M breed, Chongming No.1, breeding populations, etc.)</td>
              <td>104</td>
              <td>16923</td>
              <td></td>
              <td></td>
              <td></td>
            </tr>
            <tr>
              <td>Freshwater Fisheries Research Center + Guanghe Breed</td>
              <td>140</td>
              <td>1586</td>
              <td></td>
              <td></td>
              <td></td>
            </tr>
            <tr class="total-row">
              <td><strong>Total</strong></td>
              <td><strong>1127</strong></td>
              <td><strong>35079</strong></td>
              <td><strong>Total</strong></td>
              <td><strong>18</strong></td>
              <td><strong>355</strong></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 底部灰色小字，点击跳转到 Contact us -->
      <p style="color: #666; font-size: 13px; margin-top: 15px; text-align: center;">
        If you want to learn more information, you can contact
        <el-link
          type="primary"
          style="margin-left: 4px;"
          @click="$router.push('/front/about/contact')"
        >
          us
        </el-link>
      </p>
    </div>
  </el-col>
</el-row>
<!-- ===================== 种质资源模块结束 ===================== -->
        
        <!-- 第二部分：科研成果和平台更新 -->
        <el-row style="margin-bottom: 50px;">
          <el-col :span="24">
            <div class="news-section">
              <h2 style="color: #409EFF; border-bottom: 2px solid #409EFF; padding-bottom: 10px; margin-bottom: 20px;">
                Latest Research & Platform Updates
              </h2>

              <el-timeline>
                <el-timeline-item timestamp="2026/3/30" placement="top">
                  <el-card>
                    <h4>New samples have been sampled</h4>
                    <p>209 samples of cultivated population from the Yellow River Basin have been collected, and the corresponding phenotype data was updated</p>
                  </el-card>
                </el-timeline-item>
                <el-timeline-item timestamp="2026/1/28" placement="top">
                  <el-card>
                    <h4>EGBD has been completed primarily</h4>
                    <p>Now,we have developed all functions of EGBD! </p>
                  </el-card>
                </el-timeline-item>
                <el-timeline-item timestamp="2025/09/22" placement="top">
                  <el-card>
                    <h4>New articles published in <i>Genomics, Proteomics & Bioinformatics</i>(GPB)</h4>
                    <!-- 核心修改：用 el-link 包裹需要跳转的标题，配置 href 和 target -->
                    <p>
                      <el-link
                          href="https://academic.oup.com/gpb/advance-article/doi/10.1093/gpbjnl/qzaf079/8254453?searchresult=1"
                          target="_blank"
                      type="primary"
                      underline="hover"
                      >
                      Genomic Insights into Hybridization and Speciation of Mitten Crabs in the Eriocheir Genus.
                      </el-link>
                    </p>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-col>
        </el-row>

        <!-- 第三部分：快速导航按钮 -->
        <el-row>
          <el-col :span="24">
            <div class="quick-access-section">
              <h2 style="color: #409EFF; text-align: center; margin-bottom: 30px;">Quick Access</h2>

              <div style="display: flex; justify-content: center; gap: 20px; flex-wrap: wrap;">
                <el-button
                    type="primary"
                    size="large"
                    @click="goToGeneSearch"
                    style="min-width: 150px; height: 60px; font-size: 16px;"
                >
                  <i class="el-icon-search" style="margin-right: 8px;"></i>
                  Gene Info
                </el-button>

                <el-button
                    type="success"
                    size="large"
                    @click="goToPathwaySearch"
                    style="min-width: 150px; height: 60px; font-size: 16px;"
                >
                  <i class="el-icon-share" style="margin-right: 8px;"></i>
                  Pathway
                </el-button>

                <el-button
                    type="warning"
                    size="large"
                    @click="goToGenomeVariationSearch"
                    style="min-width: 150px; height: 60px; font-size: 16px;"
                >
                  <i class="el-icon-data-analysis" style="margin-right: 8px;"></i>
                  Genome Variation
                </el-button>

                <el-button
                    type="info"
                    size="large"
                    @click="goToDownloadCenter"
                    style="min-width: 150px; height: 60px; font-size: 16px;"
                >
                  <i class="el-icon-download" style="margin-right: 8px;"></i>
                  Download
                </el-button>
                <el-button
                    type="primary"
                    size="large"
                    @click="goToProcessExpressionSearch"
                    style="min-width: 150px; height: 60px; font-size: 16px;"
                >
                  <i class="el-icon-s-data" style="margin-right: 8px;"></i>
                  Process Expression
                </el-button>

                <!-- 新增快捷按钮2：Gene Network（基因网络） -->
                <el-button
                    type="success"
                    size="large"
                    @click="goToGeneNetworkSearch"
                    style="min-width: 150px; height: 60px; font-size: 16px;"
                >
                  <i class="el-icon-share-alt" style="margin-right: 8px;"></i>
                  Gene Network
                </el-button>

                <!-- 新增快捷按钮3：Enrichment Analysis（富集分析） -->
                <el-button
                    type="warning"
                    size="large"
                    @click="goToEnrichment"
                    style="min-width: 150px; height: 60px; font-size: 16px;"
                >
                  <i class="el-icon-chart-pie" style="margin-right: 8px;"></i>
                  Enrichment
                </el-button>

                <!-- 新增快捷按钮4：Sequence Fetch（序列提取） -->
                <el-button
                    type="info"
                    size="large"
                    @click="goToSequenceFetch"
                    style="min-width: 150px; height: 60px; font-size: 16px;"
                >
                  <i class="el-icon-file-text" style="margin-right: 8px;"></i>
                  Sequence Fetch
                </el-button>

                <!-- 新增快捷按钮5：Phenotype Data（表型数据） -->
                <el-button
                    type="primary"
                    size="large"
                    @click="goToPhenotypeData"
                    style="min-width: 150px; height: 60px; font-size: 16px;"
                >
                  <i class="el-icon-user" style="margin-right: 8px;"></i>
                  Phenotype Data
                </el-button>

                <!-- 新增快捷按钮6：Genome Overview（基因组概览） -->
                <el-button
                    type="success"
                    size="large"
                    @click="goToGenomeOverview"
                    style="min-width: 150px; height: 60px; font-size: 16px;"
                >
                  <i class="el-icon-globe" style="margin-right: 8px;"></i>
                  Genome Overview
                </el-button>

                <!-- 新增快捷按钮7：Mitochondrion Genome（线粒体基因组） -->
                <el-button
                    type="warning"
                    size="large"
                    @click="goToMitochondrionGenome"
                    style="min-width: 150px; height: 60px; font-size: 16px;"
                >
                  <i class="el-icon-dna" style="margin-right: 8px;"></i>
                  Mitochondrion Genome
                </el-button>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 2. 子路由渲染出口：所有子路由（GeneSearch、PathwaySearch等）都在这里渲染 -->
      <router-view />
    </div>

    <el-footer style="padding: 10px 0; box-sizing: border-box; text-align: center; color: #fff; background: #409EFF; position: absolute; bottom: 0; width: 100%;">
      <p style="line-height: 30px; font-size: 14px;">
        Released 2025 | Developed by wlx from Shanghai Ocean University, College of Fisheries and Science
      </p>
      <p style="line-height: 30px; font-size: 14px; margin: 0;">
        Copyright © 2025 . All rights reserved.
      </p>
    </el-footer>
  </el-container>
</template>

<script>
export default {
  name: "FrontLayout",
  data() {
    return {
      img: require("@/assets/logo.png")
    }
  },
  computed: {
    // 新增：判断当前是否为首页（/front/index），控制静态内容显示/隐藏
    isHomePage() {
      return this.$route.path === "/front/index";
    }
  },
  methods: {
    goToGeneSearch() {
      this.$router.push({name: 'GeneInfoSearch'});
    },
    goToPathwaySearch() {
      this.$router.push({name: 'PathwaySearch'});
    },
    goToExpressionSearch() {
      this.$router.push({name: 'ExpressionSearch'});
    },
    goToDownloadCenter() {
      this.$router.push({name: 'DownloadCenter'});
    },
    goToEnrichment() {
      this.$router.push({name: 'EnrichmentSearch'});
    },
    goToGenomeVariationSearch() {
      // 通过路由名称跳转（推荐，与路由配置的name字段匹配）
      this.$router.push({name: "GenomeVariationSearch"});
    },
    goToProcessExpressionSearch() {
      this.$router.push({name: "ProcessExpressionSearch"});
    },

    // 新增跳转方法2：Gene Network
    goToGeneNetworkSearch() {
      this.$router.push({name: "GeneNetworkSearch"});
    },

    // 新增跳转方法3：Sequence Fetch
    goToSequenceFetch() {
      this.$router.push({name: "SequenceFetch"});
    },

    // 新增跳转方法4：Phenotype Data
    goToPhenotypeData() {
      this.$router.push({name: "PhenotypeData"});
    },

    // 新增跳转方法5：Genome Overview
    goToGenomeOverview() {
      this.$router.push({name: "GenomeOverview"});
    },

    // 新增跳转方法6：Mitochondrion Genome
    goToMitochondrionGenome() {
      this.$router.push({name: "MitochondrionGenome"});
    }
  }
}
</script>

<style scoped>
/* 原有样式保持不变 */
:deep(.el-menu--horizontal > .el-menu-item span strong),
:deep(.el-menu--horizontal > .el-sub-menu .el-sub-menu__title span strong) {
  font-size: 16px !important; /* 字号从默认14px→16px，可按需调整到17px/18px */
  font-weight: 600; /* 可选：保持加粗，让文字更醒目 */
}

.el-menu--horizontal {
  border-bottom: none;
}

.el-menu--horizontal > .el-menu-item,
.el-menu--horizontal > .el-sub-menu .el-sub-menu__title {
  padding: 0 40px !important; /* 内边距从0 15px→0 20px，增加菜单项之间的间距 */
  height: 60px; /* 保持原高度，确保文字垂直居中 */
  line-height: 60px; /* 与height一致，确保文字垂直居中（字号加大后不偏移） */
}

:deep(.el-sub-menu .el-menu-item) {
  font-size: 15px !important; /* 子项字号略小于主菜单，保持层级感 */
  padding: 0 40px !important;
}

:deep(.el-menu-item.is-active),
:deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: #409EFF !important;
  border-bottom: 2px solid #409EFF !important;
}

:deep(.el-menu) {
  z-index: 1000 !important;
}

/* 新增主页内容样式 */
.intro-section {
  background: #f8f9fa;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.news-section {
  background: #fff;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.quick-access-section {
  background: #f8f9fa;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

  /* 种质资源展示样式 */
.germplasm-section {
  background: #fff;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.germplasm-table-wrapper {
  width: 100%;
  overflow-x: auto;
}
.germplasm-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  text-align: center;
}
.germplasm-table th,
.germplasm-table td {
  border: 1px solid #ebeef5;
  padding: 10px 8px;
  white-space: nowrap;
}
.germplasm-table th {
  background-color: #f5f7fa;
  font-weight: bold;
  color: #303133;
}
.germplasm-table tbody tr:nth-child(even) {
  background-color: #fafbfc;
}
.total-row {
  background-color: #e6f7ff !important;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .intro-section,
  .news-section,
  .quick-access-section {
    padding: 15px;
  }

  .quick-access-section .el-button {
    min-width: 120px !important;
    height: 50px !important;
    font-size: 14px !important;
  }
}

</style>
