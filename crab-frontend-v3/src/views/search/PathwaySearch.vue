<template>
  <div class="pathway-search-container">
    <div class="header">
      <h1><i class="el-icon-search"></i> Gene Pathway Search</h1>
      <p class="sub-title">Allow batch search by gene id/name, KO number or pathway</p>
    </div>

    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" label-position="top">
        <!-- 基因搜索项 -->
        <el-form-item label="Gene id/name" prop="genes">
          <el-input
              v-model="searchForm.genes"
              type="textarea"
              :rows="3"
              placeholder="Multiple genes can be separated by commas or line breaks, eg:LOC127000280, LOC127010379"
              clearable
          />
        </el-form-item>

        <!-- KO号搜索项 -->
        <el-form-item label="KO number" prop="kos">
          <el-input
              v-model="searchForm.kos"
              type="textarea"
              :rows="3"
              placeholder="Multiple KO numbers can be separated by commas or line breaks, eg:K00001, K00002"
              clearable
          />
        </el-form-item>

        <!-- 通路名称搜索项 -->
        <el-form-item label="Pathway" prop="pathways">
          <el-input
              v-model="searchForm.pathways"
              type="textarea"
              :rows="3"
              placeholder="Multiple Pathways can be separated by commas or line breaks, eg:Metabolic pathways, Biosynthesis"
              clearable
          />
        </el-form-item>

        <el-form-item class="action-buttons">
          <el-button
              type="primary"
              icon="el-icon-search"
              :loading="loading"
              :disabled="!hasSearchCriteria"
              @click="handleSearch"
          >
            Start Search ({{ criteriaCount }})
          </el-button>
          <el-button icon="el-icon-refresh" @click="resetForm">
            Reset
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { mapActions, mapMutations } from 'vuex'

export default {
  name: 'PathwaySearch',
  data() {
    return {
      searchForm: {
        genes: '',
        kos: '',
        pathways: ''
      },
      loading: false
    }
  },
  computed: {
    hasSearchCriteria() {
      return Object.values(this.searchForm).some(
          value => value && value.trim().length > 0
      )
    },
    criteriaCount() {
      return Object.values(this.searchForm).filter(
          value => value && value.trim().length > 0
      ).length
    }
  },
  methods: {
    ...mapActions('pathway', ['searchPathway']),
    ...mapMutations('pathway', ['SET_SEARCH_PARAMS']),

    // 格式化用户输入为数组
    formatInput(input) {
      if (!input || typeof input !== 'string') return []
      return input
          .replace(/[\n,，、]/g, ',')
          .split(',')
          .map(item => item.trim())
          .filter(item => item)
    },

    async handleSearch() {
      if (!this.hasSearchCriteria) {
        this.$message.warning('Please input at least a search criteria')
        return
      }

      this.loading = true
      try {
        const geneInput = this.formatInput(this.searchForm.genes);
        const params = {
          genes: geneInput, // 用于匹配 Gene 字段（基因ID）
          names: geneInput, // 新增：用于匹配 Name 字段（基因名称
          kos: this.formatInput(this.searchForm.kos),
          pathways: this.formatInput(this.searchForm.pathways),
          page: 1,
          size: 10
        }

        this.SET_SEARCH_PARAMS(params)
        const success = await this.searchPathway(params)

        if (success) {
          this.$router.push({ name: 'PathwayResults' })
        } else {
          this.$message.error('Search fails, Please check your search criteria')
        }
      } catch (error) {
        console.error('Search errors', error)
        this.$notify.error({
          title: 'Search errors',
          message: error.response?.data?.message || 'Server error',
          duration: 3000
        })
      } finally {
        this.loading = false
      }
    },

    resetForm() {
      this.searchForm = {
        genes: '',
        kos: '',
        pathways: ''
      }
      this.$message.success('Reset search criteria successfully')
    }
  }
}
</script>

<style scoped>
.pathway-search-container {
  max-width: 700px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h1 {
  color: #303133;
  font-size: 28px;
  margin-bottom: 10px;
}

.sub-title {
  color: #909399;
  font-size: 14px;
}

.search-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.action-buttons {
  text-align: center;
  margin-top: 30px;
}
</style>