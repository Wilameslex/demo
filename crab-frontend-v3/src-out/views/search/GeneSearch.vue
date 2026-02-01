<template>
  <div class="gene-search-container">
    <!-- 头部标题区域 -->
    <div class="header">
      <h1>
        <el-icon><search /></el-icon> 基因信息搜索
      </h1>
      <p class="sub-title">支持基因名称、蛋白ID和产物描述的精确/模糊搜索</p>
    </div>

    <!-- 主搜索卡片 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" label-position="top">
        <!-- 基因搜索项 -->
        <el-form-item label="基因名称" prop="gene">
          <el-input
            v-model="searchForm.gene"
            type="textarea"
            :rows="3"
            placeholder="多个基因用逗号或换行分隔，例如：LOC127000280, LOC127010379"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="fillExample('gene')" title="填充示例">
                <el-icon><collection /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <!-- 蛋白搜索项 -->
        <el-form-item label="蛋白ID" prop="protein">
          <el-input
            v-model="searchForm.protein"
            type="textarea"
            :rows="3"
            placeholder="多个蛋白用逗号分隔，例如：XP_050685756.1, XP_050685757.1"
            clearable
          >
            <template #append>
              <el-button
                :icon="ElIconCollection"
                @click="fillExample('protein')"
                title="填充示例"
              >
                <el-icon><collection /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <!-- 产物搜索项 -->
        <el-form-item label="产物" prop="product">
          <el-input
            v-model="searchForm.product"
            type="textarea"
            :rows="3"
            placeholder="多个产物用逗号分隔，例如：zinc finger, AMP deaminase"
            clearable
          >
            <template #append>
              <el-button @click="fillExample('product')" title="填充示例" />
            </template>
            <el-icon><collection /></el-icon>
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
            开始搜索 ({{ criteriaCount }})
          </el-button>
          <el-button @click="resetForm">
            <el-icon><refresh /></el-icon>
            重置条件
          </el-button>
          <el-tooltip content="高级搜索" placement="top">
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
          <el-icon><el-icon-light-rain /></el-icon> 搜索技巧
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
              <el-icon class="copy-icon"><el-icon-copy-document /></el-icon>
            </el-tag>
          </li>
        </ul>
      </div>
    </el-collapse-transition>

    <!-- 高级搜索对话框 -->
    <el-dialog title="高级搜索选项" v-model="showAdvanced" width="600px">
      <!-- 这里可以添加分页设置等高级选项 -->
      <div class="advanced-options">
        <el-form label-width="120px">
          <el-form-item label="每页显示数量">
            <el-input-number
              v-model="advancedParams.size"
              :min="5"
              :max="100"
            />
          </el-form-item>
          <el-form-item label="精确匹配">
            <el-switch v-model="advancedParams.exactMatch" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showAdvanced = false">取消</el-button>
        <el-button type="primary" @click="applyAdvanced">应用</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  LightRain as ElIconLightRain,
  CopyDocument as ElIconCopyDocument,
  Collection as ElIconCollection,
} from '@element-plus/icons'
import { mapActions, mapMutations } from 'vuex'
import { Search, Collection, Refresh, SetUp } from '@element-plus/icons-vue'

export default {
  data() {
    return {
      searchForm: {
        gene: '',
        protein: '',
        product: '',
      },
      advancedParams: {
        size: 10,
        exactMatch: false,
      },
      showAdvanced: false,
      showExamples: true,
      loading: false,
      examples: {
        gene: 'LOC127000280, LOC127010379',
        protein: 'XP_050685756.1, XP_050685757.1',
        product: 'zinc finger, AMP deaminase',
      },
      ElIconCollection,
    }
  },
  components: {
    Search,
    SetUp,
    Refresh,
    Collection,
    ElIconLightRain,
    ElIconCopyDocument,
  },
  name: 'GeneSearch',
  computed: {
    // 检查是否有搜索条件
    hasSearchCriteria() {
      return Object.values(this.searchForm).some(
        (value) => value && value.trim().length > 0
      )
    },
    // 计算当前搜索条件数量
    criteriaCount() {
      return Object.values(this.searchForm).filter(
        (value) => value && value.trim().length > 0
      ).length
    },
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
        .map((item) => item.trim())
        .filter((item) => item)
    },

    // 执行搜索
    async handleSearch() {
      if (!this.hasSearchCriteria) {
        this.$message.warning('请至少输入一个搜索条件')
        return
      }

      this.loading = true
      this.$notify.info({
        title: '搜索中',
        message: '正在查询基因数据，请稍候...',
        duration: 2000,
      })

      try {
        const params = {
          genes: this.formatInput(this.searchForm.gene),
          proteins: this.formatInput(this.searchForm.protein),
          products: this.formatInput(this.searchForm.product),
          page: 1,
          size: this.advancedParams.size,
          exactMatch: this.advancedParams.exactMatch,
        }

        // 保存参数到Vuex
        this.SET_SEARCH_PARAMS(params)

        // 调用Vuex action
        const success = await this.searchGenes(params)

        if (success) {
          this.$router.push({
            name: 'GeneResults',
            query: { t: Date.now() }, // 防止缓存
          })
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

    // 填充示例数据
    fillExample(field) {
      this.searchForm[field] = this.examples[field]
      this.$message.success(`已填充${this.getFieldLabel(field)}示例`)
    },

    // 获取字段显示名称
    getFieldLabel(field) {
      const labels = {
        gene: '基因名称',
        protein: '蛋白ID',
        product: '产物',
      }
      return labels[field] || field
    },

    // 重置表单
    resetForm() {
      this.searchForm = {
        gene: '',
        protein: '',
        product: '',
      }
      this.$message.success('搜索条件已重置')
    },

    // 应用高级选项
    applyAdvanced() {
      this.showAdvanced = false
      this.$message.success('高级设置已应用')
    },
  },
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
  border-left: 4px solid #409eff;
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
  color: #409eff;
}

.copy-icon {
  margin-left: 5px;
  font-size: 12px;
}

.advanced-options {
  padding: 0 20px;
}
</style>
