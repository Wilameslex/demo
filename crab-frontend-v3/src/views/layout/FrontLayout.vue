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
                The Chinese mitten crab (<i>Eriocheir sinensis</i>) is a species under the genus <i>Eriocheir</i> of the family Varunidae, order Decapoda, class Malacostraca. As a characteristic aquacultural crustacean in China, it possesses extremely high economic value and industrial potential. In 2022, our laboratory updated the reference genome of this species (PRJNA737102) on NCBI. To provide an initial platform for genetic research and breeding of this important aquaculture species, we hereby release an initial omics information retrieval and analysis platform for the Chinese mitten crab based on omics research data accumulated in recent years. All these data include A large amount of phenotypic data collected over a long period of time and all known genomes, transcriptomes, of this species, as well as variation sites among different geographical populations. Meanwhile, this database also allows access to a number of useful tools, including Gene Search, KEGG/GO Enrichment,Genome Browser and phenotype data statistics. For detailed information about the database data and operation instructions, please refer to the "<strong>About</strong>" page.
              </p>
              <p style="line-height: 1.8; margin-top: 15px;">
                <strong>Do not use this database for commercial purposes!</strong>
              </p>
              <p style="line-height: 1.8;">
                <strong>If you use the data from this database, please kindly cite the relevant paper! ()</strong>
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
              <p style="text-align: center; margin-top: 10px; color: #666; font-style: italic;">
                Chinese Mitten Crab (Eriocheir sinensis)
              </p>
            </div>
          </el-col>
        </el-row>

        <!-- 第二部分：科研成果和平台更新 -->
        <el-row style="margin-bottom: 50px;">
          <el-col :span="24">
            <div class="news-section">
              <h2 style="color: #409EFF; border-bottom: 2px solid #409EFF; padding-bottom: 10px; margin-bottom: 20px;">
                Latest Research & Platform Updates
              </h2>

              <el-timeline>
                <el-timeline-item timestamp="2026/2/1" placement="top">
                  <el-card>
                    <h4>Some bugs have been fixed</h4>
                    <p>We have solved some problems and improved this database</p>
                  </el-card>
                </el-timeline-item>
                <el-timeline-item timestamp="2026/1/28" placement="top">
                  <el-card>
                    <h4>EGBD has been completed primaril</h4>
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
