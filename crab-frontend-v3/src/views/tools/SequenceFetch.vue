<template>
  <div class="sequence-fetch-container">
    <h1 class="page-title">Sequence Fetch</h1>
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="120px" size="small">
        <!-- 搜索类型选择 -->
        <el-form-item label="Search By">
          <select v-model="searchForm.type" @change="handleTypeChange">
            <option value="gene">Gene Names/IDs</option>
            <option value="region">By Chromosome Region</option>
          </select>
        </el-form-item>

        <!-- 基因名/ID输入（支持批量，换行分隔） -->
        <el-form-item
            label="Gene Names/IDs"
            v-if="searchForm.type === 'gene'"
        >
          <el-input
              type="textarea"
              v-model="searchForm.genes"
              placeholder="Enter gene names/IDs (one per line)"
              :rows="4"
          ></el-input>
        </el-form-item>

        <!-- 染色体区域输入（如 chr1:1000-5000） -->
        <el-form-item
            label="Chromosome Region"
            v-if="searchForm.type === 'region'"
        >
          <el-input
              type="textarea"
              v-model="searchForm.region"
              placeholder="Enter chromosome regions (one per line), e.g.&#10;chr1:10000-12000&#10;NC_066509.1:20000-25000"
              :rows="4"
          ></el-input>
        </el-form-item>

        <!-- 序列类型选择 -->
        <el-form-item label="Sequence Type">
          <el-select v-model="searchForm.sequenceType" clearable :disabled="searchForm.type === 'region'" >
            <el-option label="Genomic (full region)" value="genomic"></el-option>
            <el-option label="CDS" value="cds"></el-option>
            <el-option label="Exons" value="exons"></el-option>
            <el-option label="mRNA" value="mrna"></el-option>
          </el-select>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" @click="fetchSequences">Fetch Sequences</el-button>
          <el-button @click="resetForm">Reset</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 结果展示区域 -->
    <el-card class="result-card" v-if="sequences.length > 0">
      <div slot="header">
        <span>Results ({{ sequences.length }})</span>
        <el-button
            type="text"
            icon="el-icon-download"
            @click="downloadFasta"
            style="float: right"
        >
          Download FASTA
        </el-button>
      </div>
      <pre class="fasta-result">{{ formattedFasta }}</pre>
    </el-card>
  </div>
</template>

<script>
export default {
  data() {
    return {
      searchForm: {
        type: "gene", // 默认按基因搜索
        genes: "",
        region: "",
        sequenceType: "genomic"
      },
      sequences: [] // 存储获取的序列 {id, sequence}
    };
  },
  computed: {
    // 格式化FASTA文本
    formattedFasta() {
      return this.sequences.map(item => `>${item.id}\n${item.sequence}`).join("\n");
    }
  },
  methods: {
    handleTypeChange() {
      // 切换搜索类型时清空对应输入
      if (this.searchForm.type === "gene") this.searchForm.region = "";
      else this.searchForm.genes = "";
    },
    resetForm() {
      this.searchForm = {
        type: "gene",
        genes: "",
        region: "",
        sequenceType: "genomic"
      };
      this.sequences = [];
    },
    async fetchSequences() {
      // 表单验证
      if (this.searchForm.type === "gene" && !this.searchForm.genes) {
        return this.$message.error("Please enter gene names/IDs");
      }
      if (this.searchForm.type === "region" && !this.searchForm.region) {
        return this.$message.error("Please enter chromosome region");
      }

      // 调用后端API获取序列（根据实际接口调整）
      try {
        const response = await this.$axios.post("/api/tools/fetch-sequence", this.searchForm);
        this.sequences = response.data;
      } catch (error) {
        this.$message.error("Failed to fetch sequences: " + error.message);
      }
    },
    downloadFasta() {
      // 下载FASTA文件
      const blob = new Blob([this.formattedFasta], { type: "text/fasta" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = "sequences.fasta";
      a.click();
      URL.revokeObjectURL(url);
    }
  }
};
</script>

<style scoped>
.sequence-fetch-container {
  padding: 20px;
  max-width: 1200px;
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
.result-card {
  white-space: pre-wrap;
}
.fasta-result {
  font-family: "Courier New", monospace;
  line-height: 1.5;
  max-height: 500px;
  overflow-y: auto;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 4px;
}
select:disabled {
  background-color: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}
.hint-text {
  color: #666;
  font-size: 0.8em;
  margin: 5px 0 0 0;
}
</style>