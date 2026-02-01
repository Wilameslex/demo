<template>
  <div class="gene-search-container">
    <!-- 头部标题区域 -->
    <div class="header">
      <h1><el-icon><search /></el-icon> Gene Information Search</h1>
      <p class="sub-title">You can search by gene id/name, protein id or the production</p>
    </div>

    <!-- 主搜索卡片 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" label-position="top">
        <!-- 基因搜索项 -->
        <el-form-item label="Gene id/name" prop="gene">
          <el-input
              v-model="searchForm.gene"
              type="textarea"
              :rows="3"
              placeholder="Multiple genes can be separated by commas or line breaks, eg：LOC127000280, LOC127010379"
              clearable
              @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button
                  @click="fillExample('gene')"
                  title="Fill the example"
              >
                <el-icon><collection/></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <!-- 蛋白搜索项 -->
        <el-form-item label="Protein id" prop="protein">
          <el-input
              v-model="searchForm.protein"
              type="textarea"
              :rows="3"
              placeholder="Multiple proteins can be separated by commas or line breaks, eg：XP_050685756.1, XP_050685757.1"
              clearable
          >
            <template #append>
              <el-button
                  icon="el-icon-collection"
                  @click="fillExample('protein')"
                  title="Fill the example"
              >
                <el-icon><collection/></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <!-- 产物搜索项 -->
        <el-form-item label="Product" prop="product">
          <el-input
              v-model="searchForm.product"
              type="textarea"
              :rows="3"
              placeholder="Multiple products can be separated by commas or line breaks, eg：zinc finger, AMP deaminase"
              clearable
          >
            <template #append>
              <el-button
                  @click="fillExample('product')"
                  title="Fill the example"
              />
            </template>
            <el-icon><collection/></el-icon>
          </el-input>
        </el-form-item>

        <!-- 操作按钮组 -->
        <el-form-item class="action-buttons">
          <el-button
              type="primary"
              :loading="loading"
              :disabled="!hasSearchCriteria"
              @click="handleSearch"
          >
            <el-icon><search /></el-icon>
            Start Search ({{ criteriaCount }})
          </el-button>
          <el-button @click="resetForm">
            <el-icon><refresh /></el-icon>
            Reset
          </el-button>
          <el-tooltip content="Advanced Search" placement="top">
            <el-button @click="showAdvanced = true">
              <el-icon><set-up /></el-icon>
            </el-button>
          </el-tooltip>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 示例区域 -->
    <el-collapse-transition>
      <div v-if="showExamples" class="examples-panel">
        <h3 class="examples-title">
          <i class="el-icon-light-rain"></i> Search skills
        </h3>
        <ul class="examples-list">
          <li v-for="(example, field) in examples" :key="field">
            <span class="field-name">{{ getFieldLabel(field) }}：</span>
            <el-tag
                type="info"
                effect="plain"
                class="example-tag"
                @click="fillExample(field)"
            >
              {{ example }}
              <i class="el-icon-copy-document copy-icon"></i>
            </el-tag>
          </li>
        </ul>
      </div>
    </el-collapse-transition>

    <!-- 高级搜索对话框 -->
    <el-dialog
        title="Advanced Search choice"
        v-model="showAdvanced"
        width="600px"
    >
      <!-- 这里可以添加分页设置等高级选项 -->
      <div class="advanced-options">
        <el-form label-width="120px">
          <el-form-item label="lines per age">
            <el-input-number
                v-model="advancedParams.size"
                :min="5"
                :max="100"
            />
          </el-form-item>
          <el-form-item label="Exact match">
            <el-switch v-model="advancedParams.exactMatch" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showAdvanced = false">Cancel</el-button>
        <el-button type="primary" @click="applyAdvanced">Apply</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { mapActions, mapMutations } from 'vuex'
import {Search, Collection, Refresh, SetUp} from "@element-plus/icons-vue";

export default {
  name: 'GeneInfoSearch',
  components: {Search, SetUp, Refresh, Collection},
  data() {
    return {
      searchForm: {
        gene: '',
        protein: '',
        product: ''
      },
      advancedParams: {
        size: 10,
        exactMatch: false
      },
      showAdvanced: false,
      showExamples: true,
      loading: false,
      examples: {
        gene: 'LOC127000280, LOC127010379',
        protein: 'XP_050685756.1, XP_050685757.1',
        product: 'zinc finger, AMP deaminase'
      }
    }
  },
  computed: {
    // 检查是否有搜索条件
    hasSearchCriteria() {
      return Object.values(this.searchForm).some(
          value => value && value.trim().length > 0
      )
    },
    // 计算当前搜索条件数量
    criteriaCount() {
      return Object.values(this.searchForm).filter(
          value => value && value.trim().length > 0
      ).length
    }
  },
  methods: {
    ...mapActions('gene', ['searchGenes']),
    ...mapMutations('gene', ['SET_SEARCH_PARAMS']),

    // 格式化用户输入为数组
    formatInput(input) {
      if (!input || typeof input !== 'string') return []
      return input
          .replace(/[\n,，、]/g, ',') // 处理多种分隔符
          .split(',')
          .map(item => item.trim())
          .filter(item => item)
    },

    // 执行搜索
    async handleSearch() {
      if (!this.hasSearchCriteria) {
        this.$message.warning('Please input at least a search criteria')
        return
      }

      this.loading = true
      this.$notify.info({
        title: 'Search in progress',
        message: 'Please wait...',
        duration: 2000
      })

      const geneInput = this.formatInput(this.searchForm.gene);

      try {
        const params = {
          genes: geneInput,
          names: geneInput,
          proteins: this.formatInput(this.searchForm.protein),
          products: this.formatInput(this.searchForm.product),
          page: 1,
          size: this.advancedParams.size,
          exactMatch: this.advancedParams.exactMatch
        }

        // 保存参数到Vuex
        this.SET_SEARCH_PARAMS(params)

        // 调用Vuex action
        const success = await this.searchGenes(params)

        if (success) {
          this.$router.push({
            name: 'GeneResults',
            query: { t: Date.now() } // 防止缓存
          })
        } else {
          this.$message.error('Search fails, please check your search criteria')
        }
      } catch (error) {
        console.error('Search errors:', error)
        this.$notify.error({
          title: 'Search errors',
          message: error.response?.data?.message || 'the Internet is wrong',
          duration: 3000
        })
      } finally {
        this.loading = false
      }
    },

    // 填充示例数据
    fillExample(field) {
      this.searchForm[field] = this.examples[field]
      this.$message.success(`${this.getFieldLabel(field)}has been filled`)
    },

    // 获取字段显示名称
    getFieldLabel(field) {
      const labels = {
        gene: 'Gene Name',
        protein: 'Protein id',
        product: 'Product'
      }
      return labels[field] || field
    },

    // 重置表单
    resetForm() {
      this.searchForm = {
        gene: '',
        protein: '',
        product: ''
      }
      this.$message.success('Reset successfully')
    },

    // 应用高级选项
    applyAdvanced() {
      this.showAdvanced = false
      this.$message.success('Advanced settings are applied')
    }
  }
}
</script>

<style scoped>
.gene-search-container {
  max-width: 900px;
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

.examples-panel {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-top: 20px;
  border-left: 4px solid #409EFF;
}

.examples-title {
  color: #303133;
  font-size: 16px;
  margin-top: 0;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.examples-list {
  padding-left: 20px;
  color: #606266;
  list-style: none;
}

.examples-list li {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.field-name {
  display: inline-block;
  width: 80px;
  font-weight: bold;
}

.example-tag {
  cursor: pointer;
  margin-left: 10px;
  transition: all 0.3s;
}

.example-tag:hover {
  background-color: #ecf5ff;
  color: #409EFF;
}

.copy-icon {
  margin-left: 5px;
  font-size: 12px;
}

.advanced-options {
  padding: 0 20px;
}
</style>