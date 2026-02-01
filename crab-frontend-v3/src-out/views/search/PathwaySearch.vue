<template>
  <div class="pathway-search-container">
    <div class="header">
      <h1>
        <el-icon><el-icon-search /></el-icon> KEGG通路搜索
      </h1>
      <p class="sub-title">
        支持批量输入基因名称、KO号或通路名称（多个用逗号或换行分隔）
      </p>
    </div>

    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" label-position="top">
        <!-- 基因搜索项 -->
        <el-form-item label="基因名称" prop="genes">
          <el-input
            v-model="searchForm.genes"
            type="textarea"
            :rows="3"
            placeholder="多个基因用逗号或换行分隔，例如：LOC127000280, LOC127010379"
            clearable
          />
        </el-form-item>

        <!-- KO号搜索项 -->
        <el-form-item label="KO号" prop="kos">
          <el-input
            v-model="searchForm.kos"
            type="textarea"
            :rows="3"
            placeholder="多个KO号用逗号或换行分隔，例如：K00001, K00002"
            clearable
          />
        </el-form-item>

        <!-- 通路名称搜索项 -->
        <el-form-item label="通路名称" prop="pathways">
          <el-input
            v-model="searchForm.pathways"
            type="textarea"
            :rows="3"
            placeholder="多个通路名称用逗号或换行分隔，例如：Metabolic pathways, Biosynthesis"
            clearable
          />
        </el-form-item>

        <el-form-item class="action-buttons">
          <el-button
            type="primary"
            :icon="ElIconSearch"
            :loading="loading"
            :disabled="!hasSearchCriteria"
            @click="handleSearch"
          >
            开始搜索 ({{ criteriaCount }})
          </el-button>
          <el-button :icon="ElIconRefresh" @click="resetForm">
            重置条件
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import {
  Search as ElIconSearch,
  Refresh as ElIconRefresh,
} from '@element-plus/icons'
import { mapActions, mapMutations } from 'vuex'

export default {
  data() {
    return {
      searchForm: {
        genes: '',
        kos: '',
        pathways: '',
      },
      loading: false,
      ElIconSearch,
      ElIconRefresh,
    }
  },
  components: {
    ElIconSearch,
  },
  name: 'PathwaySearch',
  computed: {
    hasSearchCriteria() {
      return Object.values(this.searchForm).some(
        (value) => value && value.trim().length > 0
      )
    },
    criteriaCount() {
      return Object.values(this.searchForm).filter(
        (value) => value && value.trim().length > 0
      ).length
    },
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
        .map((item) => item.trim())
        .filter((item) => item)
    },

    async handleSearch() {
      if (!this.hasSearchCriteria) {
        this.$message.warning('请至少输入一个搜索条件')
        return
      }

      this.loading = true
      try {
        const params = {
          genes: this.formatInput(this.searchForm.genes),
          kos: this.formatInput(this.searchForm.kos),
          pathways: this.formatInput(this.searchForm.pathways),
          page: 1,
          size: 10,
        }

        this.SET_SEARCH_PARAMS(params)
        const success = await this.searchPathway(params)

        if (success) {
          this.$router.push({ name: 'PathwayResults' })
        } else {
          this.$message.error('搜索失败，请检查输入条件')
        }
      } catch (error) {
        console.error('搜索出错:', error)
        this.$notify.error({
          title: '搜索错误',
          message: error.response?.data?.message || '服务器连接异常',
          duration: 3000,
        })
      } finally {
        this.loading = false
      }
    },

    resetForm() {
      this.searchForm = {
        genes: '',
        kos: '',
        pathways: '',
      }
      this.$message.success('搜索条件已重置')
    },
  },
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
