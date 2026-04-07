<template>
  <el-container style="min-height: 100vh;">
    <!-- 左侧快捷导航目录 -->
    <el-aside width="260px" style="background: #f5f7fa; padding: 20px 0; overflow-y: auto; height: 100vh; position: sticky; top: 0;">
      <el-menu
          default-active="1"
          class="help-menu"
          @select="handleMenuSelect"
          style="border-right: none;"
      >
        <el-menu-item index="1">
          <a href="#overview">1. Database Overview</a>
        </el-menu-item>
        <el-menu-item index="2">
          <a href="#quick-nav">2. Quick Navigation</a>
        </el-menu-item>
        <el-menu-item index="3">
          <a href="#gene-search">3. Gene Search Module</a>
        </el-menu-item>
        <el-menu-item index="4">
          <a href="#pathway-search">4. Pathway Search Module</a>
        </el-menu-item>
        <el-menu-item index="5">
          <a href="#expression-analysis">5. Expression Analysis Module</a>
        </el-menu-item>
        <el-menu-item index="6">
          <a href="#genome-variation">6. Genome Variation Module</a>
        </el-menu-item>
        <el-menu-item index="7">
          <a href="#process-transcriptome">7. Process Transcriptome Module</a>
        </el-menu-item>
        <el-menu-item index="8">
          <a href="#data-download">8. Data Download Center</a>
        </el-menu-item>
        <el-menu-item index="9">
          <a href="#faq">9. Frequently Asked Questions (FAQ)</a>
        </el-menu-item>
        
      </el-menu>
    </el-aside>

    <!-- 右侧帮助内容主体 -->
    <el-main style="padding: 40px 60px; line-height: 1.8; font-size: 15px; color: #333;">
      <!-- 1. 数据库概述 -->
      <section id="overview" style="margin-bottom: 60px;">
        <h2 style="color: #1989fa; border-bottom: 2px solid #e6f7ff; padding-bottom: 10px; margin-bottom: 30px;">1. Database Overview</h2>
        <p>This database integrates multi-omics data (transcriptome, genome, phenotype) of crustaceans, focusing on key biological processes such as metamorphosis, maturity, molting, carcinization, premature development, outwater respiration, and photoperiod change.</p>
        <p>Core functions include gene information retrieval, pathway enrichment analysis, gene expression quantification, genome variation detection, and process-specific transcriptome analysis, providing a comprehensive data support platform for crustacean molecular biology research.</p>
      </section>

      <!-- 2. 快捷导航 -->
      <section id="quick-nav" style="margin-bottom: 60px;">
        <h2 style="color: #1989fa; border-bottom: 2px solid #e6f7ff; padding-bottom: 10px; margin-bottom: 30px;">2. Quick Navigation</h2>
        <p>The homepage provides one-click access to core functional modules via shortcut buttons, with the following functions:</p>
        <ul style="margin-left: 20px; list-style: disc; margin-bottom: 20px;">
          <li><strong>Gene Info</strong>: Jump to gene basic information search page</li>
          <li><strong>Pathway</strong>: Jump to pathway enrichment and annotation page</li>
          <li><strong>Genome Variation</strong>: Jump to genome variation detection and analysis page</li>
          <li><strong>Download</strong>: Jump to multi-omics data download center</li>
          <li><strong>Process Expression</strong>: Jump to process-specific transcriptome analysis page</li>
          <li><strong>Gene Network</strong>: Jump to gene co-expression network analysis page</li>
          <li><strong>Enrichment</strong>: Jump to functional enrichment analysis page</li>
          <li><strong>Sequence Fetch</strong>: Jump to gene/transcript sequence extraction page</li>
        </ul>
      </section>

      <!-- 3. 基因搜索模块 -->
      <section id="gene-search" style="margin-bottom: 60px;">
        <h2 style="color: #1989fa; border-bottom: 2px solid #e6f7ff; padding-bottom: 10px; margin-bottom: 30px;">3. Gene Search Module</h2>
        <h3 style="color: #409eff; margin-bottom: 15px;">3.1 Search Criteria</h3>
        <ul style="margin-left: 20px; list-style: disc; margin-bottom: 20px;">
          <li>Gene ID (e.g., LOC126980185)</li>
          <li>Gene name (e.g., Hsp70)</li>
          <li>Functional annotation keywords (e.g., heat shock protein)</li>
        </ul>
        <h3 style="color: #409eff; margin-bottom: 15px;">3.2 Output Results</h3>
        <p>The module returns comprehensive gene information, including:</p>
        <ul style="margin-left: 20px; list-style: disc; margin-bottom: 20px;">
          <li>Basic information: Gene ID, transcript ID, sequence length, chromosome location</li>
          <li>Functional annotation: GO terms, KEGG pathway annotation, Swiss-Prot homology</li>
          <li>Expression profile: Expression levels across different tissues and biological processes</li>
        </ul>
      </section>

      <!-- 4. 通路搜索模块 -->
      <section id="pathway-search" style="margin-bottom: 60px;">
        <h2 style="color: #1989fa; border-bottom: 2px solid #e6f7ff; padding-bottom: 10px; margin-bottom: 30px;">4. Pathway Search Module</h2>
        <h3 style="color: #409eff; margin-bottom: 15px;">4.1 Search Methods</h3>
        <ul style="margin-left: 20px; list-style: disc; margin-bottom: 20px;">
          <li>Pathway ID (e.g., ko04141)</li>
          <li>Pathway name (e.g., Endocytosis)</li>
          <li>Gene list (batch query of pathways enriched by target genes)</li>
        </ul>
        <h3 style="color: #409eff; margin-bottom: 15px;">4.2 Analysis Results</h3>
        <p>Output includes pathway topological structure, gene composition, and:</p>
        <ul style="margin-left: 20px; list-style: disc; margin-bottom: 20px;">
          <li>Enrichment significance (P-value, FDR)</li>
          <li>Expression heatmap of pathway-related genes</li>
          <li>Downloadable pathway annotation table</li>
        </ul>
      </section>

      <!-- 5. 表达分析模块 -->
      <section id="expression-analysis" style="margin-bottom: 60px;">
        <h2 style="color: #1989fa; border-bottom: 2px solid #e6f7ff; padding-bottom: 10px; margin-bottom: 30px;">5. Expression Analysis Module</h2>
        <h3 style="color: #409eff; margin-bottom: 15px;">5.1 Input Parameters</h3>
        <ul style="margin-left: 20px; list-style: disc; margin-bottom: 20px;">
          <li>Analysis pipeline: StringTie / RSEM</li>
          <li>Transcriptome type: Maturity / Metamorphosis / Molt</li>
          <li>Target gene IDs (single or batch input)</li>
          <li>Sample selection (optional, default all samples)</li>
        </ul>
        <h3 style="color: #409eff; margin-bottom: 15px;">5.2 Visualization Results</h3>
        <ul style="margin-left: 20px; list-style: disc; margin-bottom: 20px;">
          <li>Expression level bar chart (per sample/class)</li>
          <li>Expression distribution boxplot</li>
          <li>Sample-specific gene scatter plot</li>
          <li>Downloadable expression matrix (CSV format)</li>
        </ul>
      </section>

      <!-- 6. 基因组变异模块 -->
      <section id="genome-variation" style="margin-bottom: 60px;">
        <h2 style="color: #1989fa; border-bottom: 2px solid #e6f7ff; padding-bottom: 10px; margin-bottom: 30px;">6. Genome Variation Module</h2>
        <h3 style="color: #409eff; margin-bottom: 15px;">6.1 Variation Type Support</h3>
        <ul style="margin-left: 20px; list-style: disc; margin-bottom: 20px;">
          <li>Single Nucleotide Polymorphism (SNP)</li>
          <li>Insertion/Deletion (InDel)</li>
          <li>Structural Variation (SV)</li>
        </ul>
        <h3 style="color: #409eff; margin-bottom: 15px;">6.2 Query & Analysis</h3>
        <p>Users can search variations by:</p>
        <ul style="margin-left: 20px; list-style: disc; margin-bottom: 20px;">
          <li>Chromosome region (e.g., Chr1:100000-200000)</li>
          <li>Gene ID (variations within gene coding region)</li>
          <li>Variation ID (unique identifier of detected variation)</li>
        </ul>
        <p>Results include variation location, allele frequency, and functional impact (e.g., non-synonymous mutation).</p>
      </section>

      <!-- 7. 过程转录组模块 -->
      <section id="process-transcriptome" style="margin-bottom: 60px;">
        <h2 style="color: #1989fa; border-bottom: 2px solid #e6f7ff; padding-bottom: 10px; margin-bottom: 30px;">7. Process Transcriptome Module</h2>
        <h3 style="color: #409eff; margin-bottom: 15px;">7.1 Supported Biological Processes</h3>
        <ul style="margin-left: 20px; list-style: disc; margin-bottom: 20px;">
          <li>Metamorphosis</li>
          <li>Maturity (2-year adult)</li>
          <li>Molting</li>
          <li>Carcinization (Abdominal deformation)</li>
          <li>Premature development</li>
          <li>Outwater respiration</li>
          <li>Photoperiod change</li>
        </ul>
        <h3 style="color: #409eff; margin-bottom: 15px;">7.2 Core Functions</h3>
        <ul style="margin-left: 20px; list-style: disc; margin-bottom: 20px;">
          <li>Count of expressed genes (bar chart visualization)</li>
          <li>Top 20 expressed genes download</li>
          <li>Sample-specific gene display & download</li>
          <li>Expression distribution across sample classes (boxplot)</li>
        </ul>
      </section>

      <!-- 8. 数据下载中心 -->
      <section id="data-download" style="margin-bottom: 60px;">
        <h2 style="color: #1989fa; border-bottom: 2px solid #e6f7ff; padding-bottom: 10px; margin-bottom: 30px;">8. Data Download Center</h2>
        <h3 style="color: #409eff; margin-bottom: 15px;">8.1 Available Data Types</h3>
        <ul style="margin-left: 20px; list-style: disc; margin-bottom: 20px;">
          <li>Raw transcriptome sequencing data (FASTQ)</li>
          <li>Gene/transcript expression matrix (TPM/FPKM)</li>
          <li>Genome variation calling results (VCF)</li>
          <li>Functional annotation database (GO/KEGG)</li>
          <li>Process-specific transcriptome analysis results</li>
        </ul>
        <h3 style="color: #409eff; margin-bottom: 15px;">8.2 Download Instructions</h3>
        <p>All data are compressed in ZIP format, with MD5 checksum provided for data integrity verification. Users need to accept the data usage agreement before downloading.</p>
      </section>

      <!-- 9. 常见问题 -->
      <section id="faq" style="margin-bottom: 60px;">
        <h2 style="color: #1989fa; border-bottom: 2px solid #e6f7ff; padding-bottom: 10px; margin-bottom: 30px;">9. Frequently Asked Questions (FAQ)</h2>
        <div style="margin-bottom: 15px;">
          <strong>Q1: Why are there no results for the gene abbreviation I searched in the gene-info module?</strong><br>
          A1: The data in this module are based on the annotation file of the reference genome. The abbreviated names of some genes may differ from your usage habits. You can search by their full names or corresponding products.
        </div>
        <div style="margin-bottom: 15px;">
          <strong>Q2: How to add new biological processes to the Expression search module?</strong><br>
          A2: In this module, high-quality transcriptomics data will be added on a regular basis.If you wish to enable retrieval of your targeted omics processes in this module,you may contact us via the navigation bar above: <strong>About → Contact</strong> us.
        </div>
        <div style="margin-bottom: 15px;">
          <strong>Q3: What should I do if I encounter "Tunnel mittencrab.nat100.top not found" when opening the website?</strong><br>
          A3: It means the server is under maintenance. Please just wait, we will fix it as soon as possible.
        </div>
      </section>
    </el-main>
  </el-container>
</template>

<script setup>
// 左侧菜单选择事件（锚点跳转自动生效，此处仅预留扩展）
const handleMenuSelect = (index) => {
  console.log(`Selected menu item: ${index}`);
};
</script>

<style scoped>
/* 左侧菜单样式优化 */
.help-menu :deep(.el-menu-item) {
  padding-left: 30px !important;
}
.help-menu :deep(.el-menu-item a) {
  color: #333;
  text-decoration: none;
  display: block;
  width: 100%;
}
.help-menu :deep(.el-menu-item a:hover) {
  color: #409eff;
}
.help-menu :deep(.el-menu-item.is-active a) {
  color: #1989fa;
  font-weight: bold;
}

/* 右侧内容样式优化 */
section h2 {
  font-size: 20px;
  font-weight: bold;
}
section h3 {
  font-size: 16px;
  font-weight: 600;
}
section p {
  margin-bottom: 15px;
}
section ul {
  margin-bottom: 20px;
}

/* 响应式适配（小屏幕下左侧菜单折叠） */
@media (max-width: 768px) {
  .el-aside {
    width: 200px !important;
  }
  .el-main {
    padding: 20px 20px !important;
  }
}
</style>
