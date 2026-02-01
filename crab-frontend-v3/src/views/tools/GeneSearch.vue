<template>
  <div class="gene-search-container">
    <h1 class="page-title">Gene Search</h1>
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="120px" inline>
        <el-form-item label="Gene ID">
          <el-input
              v-model="searchForm.geneId"
              placeholder="Enter gene ID (e.g. LOC127003481)"
              style="width: 300px"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchGene">Search</el-button>
          <el-button @click="resetSearch">Reset</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- JBrowse2 嵌入区域：修复路径 -->
    <el-card v-if="geneLocation" class="jbrowse-card">
      <div slot="header">Genomic Visualization for {{ searchForm.geneId }}</div>
      <iframe
          :src="jbrowseSrc"
          width="100%"
          height="450px"
      ></iframe>
    </el-card>
    <!-- 新增：相关查询链接卡片 -->
    <el-card v-if="geneId" class="related-links-card">
      <div slot="header">Related Queries</div>
      <div class="links-group">
        <router-link
            :to="{ name: 'GeneResults', query: { geneId: geneId } }"
            class="related-link"
        >
          View Gene Info for {{ geneId }}
        </router-link>
        <router-link
            :to="{ name: 'PathwayResults', query: { geneId: geneId } }"
            class="related-link"
        >
          View Pathways for {{ geneId }}
        </router-link>
        <!-- 可扩展：表达分析、富集分析等其他链接 -->
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  data() {
    return {
      searchForm: { geneId: "" },
      geneLocation: null, // 格式：{chr: "NC_066509.1", start: 115640, end: 117640}
      geneId: "" // 新增：存储当前搜索的基因ID
    };
  },
  computed: {
    jbrowseSrc() {
      if (!this.geneLocation) return "";
      // 处理 start/end 为纯数字字符串（无逗号）
      const start = this.geneLocation.start.toString().replace(/,/g, "");
      const end = this.geneLocation.end.toString().replace(/,/g, "");
      return `/jbrowse2/index.html?config=/mydata/config.json&assembly=genome&tracks=genome-ReferenceSequenceTrack,genes&loc=${this.geneLocation.chr}:${start}-${end}`;
    },
  },
  methods: {
    resetSearch() {
      this.searchForm.geneId = "";
      this.geneLocation = null;
    },
    async searchGene() {
      if (!this.searchForm.geneId.trim()) {
        return this.$message.error("Please enter a gene ID");
      }

      try {
        const response = await this.$axios.get("/api/gene/location", {  // 明确后端接口端口（若后端端口不是3003，需修改）
          params: { geneId: this.searchForm.geneId.trim() }
        });
        this.geneLocation = response.data;
        this.geneId = this.searchForm.geneId.trim();// 关键赋值行
        console.log("GeneId set to:", this.geneId); // 新增：打印赋值结果
        console.log("GeneLocation:", this.geneLocation);

        // 验证返回格式是否正确
        if (!this.geneLocation || !this.geneLocation.chr || !this.geneLocation.start || !this.geneLocation.end) {
          throw new Error("Invalid gene location (missing chr/start/end)");
        }
        if (this.geneLocation.start >= this.geneLocation.end) {
          throw new Error("Start position must be less than end position");
        }

        this.$message.success("Gene found, loading visualization...");
      } catch (error) {
        this.geneLocation = null;
        let errMsg = "Search failed";
        if (error.response) {
          // 根据后端状态码提示
          if (error.response.status === 404) {
            errMsg = "Gene ID not found (check if it exists in database)";
          } else if (error.response.status === 400) {
            errMsg = "Missing or invalid gene ID";
          } else if (error.response.status === 500) {
            errMsg = "Server error, please try again later";
          }
        }
        this.$message.error(`Error: ${errMsg}`);
      }
    }
  }
};
</script>

<style scoped>
.gene-search-container {
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
.jbrowse-card {
  min-height: 450px;
}
.related-links-card {
  margin-top: 20px;
}
.links-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 10px;
}
.related-link {
  color: #409eff;
  text-decoration: underline;
  cursor: pointer;
}
.related-link:hover {
  color: #66b1ff;
}
</style>