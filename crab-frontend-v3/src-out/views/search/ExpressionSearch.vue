<template>
  <div class="expression-search-container">
    <div class="header">
      <h1>
        <el-icon><el-icon-search /></el-icon> 基因表达搜索
      </h1>
      <p class="sub-title">从特定转录组中搜索多个基因的表达数据</p>
    </div>

    <el-card class="search-card" shadow="hover">
      <el-form label-position="top">
        <!-- 流程选择 -->
        <el-form-item label="分析流程">
          <el-radio-group v-model="form.pipeline">
            <el-radio-button label="stringtie">STAR+Stringtie</el-radio-button>
            <el-radio-button label="rsem">STAR+RSEM</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 转录组选择 -->
        <el-form-item label="转录组">
          <el-select
            v-model="form.transcript"
            placeholder="选择转录组"
            class="full-width"
          >
            <el-option
              v-for="item in transcriptOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
              <span>{{ item.label }}</span>
              <!--              <el-tag v-if="item.value === 'maturity'" size="mini" type="info" style="margin-left: 10px">-->
              <!--                点击下载原始数据-->
              <!--              </el-tag>-->
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 搜索类型单选 -->
        <el-form-item label="搜索类型">
          <el-radio-group v-model="form.searchType">
            <el-radio label="gene">基因表达量搜索</el-radio>
            <el-radio label="transcript">转录本表达量搜索</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 动态输入区域 -->
        <el-form-item
          :label="form.searchType === 'gene' ? '基因列表' : '转录本列表'"
        >
          <el-input
            v-model="form.targetInput"
            type="textarea"
            :rows="5"
            :placeholder="getPlaceholder()"
          ></el-input>
          <div class="example-link">
            <el-link type="info" @click="fillExample">
              <el-icon><el-icon-info /></el-icon> 填充示例数据
            </el-link>
          </div>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            :icon="ElIconSearch"
            @click="handleSearch"
            :loading="loading"
          >
            开始搜索
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { Search as ElIconSearch, Info as ElIconInfo } from '@element-plus/icons'
import { searchExpression } from '@/api/expression'

export default {
  data() {
    return {
      loading: false,
      form: {
        pipeline: 'stringtie',
        transcript: 'maturity',
        searchType: 'gene',
        targetInput: '',
      },
      transcriptOptions: [
        { value: 'maturity', label: '2year maturity' },
        { value: 'metamorphosis', label: 'Metamorphosis' },
      ],
      ElIconSearch,
    }
  },
  components: {
    ElIconSearch,
    ElIconInfo,
  },
  name: 'ExpressionSearch',
  methods: {
    getPlaceholder() {
      return this.form.searchType === 'gene'
        ? '每行输入一个基因ID，例如：\nLOC127000532\nLOC127000280'
        : '每行输入一个转录本ID，例如：\nXM_050863983.1\nXM_050864059.1'
    },
    fillExample() {
      this.form.targetInput =
        this.form.searchType === 'gene'
          ? 'LOC127000532\nLOC127000280\nLOC127000531'
          : 'XM_050863983.1\nXM_050864059.1\nXM_050864060.1'
      this.$message.success('已填充示例数据')
    },
    async handleSearch() {
      if (!this.form.targetInput.trim()) {
        this.$message.warning('请输入要搜索的基因或转录本ID')
        return
      }

      this.loading = true
      try {
        const targetIds = this.form.targetInput
          .split('\n')
          .map((id) => id.trim())
          .filter((id) => id)

        if (targetIds.length > 100) {
          this.$message.warning('一次最多查询100个ID')
          return
        }

        // 修复点：正确构建请求参数
        const requestParams = {
          pipeline: this.form.pipeline,
          transcriptome: this.form.transcript,
          searchType: this.form.searchType,
          targetIds: targetIds,
        }
        // 保存参数到sessionStorage
        sessionStorage.setItem('expressionQuery', JSON.stringify(requestParams))

        console.log('发送搜索请求:', requestParams)

        // 修复点：传递正确的参数
        const success = await this.$store.dispatch(
          'expression/searchExpression',
          requestParams
        )

        if (success) {
          this.$router.push({
            name: 'ExpressionResults',
            query: requestParams, // 通过URL参数传递
          })
        } else {
          this.$message.error('搜索失败，请重试')
        }
      } catch (error) {
        console.error('搜索过程出错:', {
          message: error.message,
          response: error.response,
          config: error.config,
        })

        let errorMessage = '搜索失败: '
        if (error.response) {
          errorMessage += `[${error.response.status}] ${
            error.response.data?.message || '服务器错误'
          }`
        } else {
          errorMessage += error.message || '未知错误'
        }

        this.$message.error(errorMessage)
      } finally {
        this.loading = false
      }
    },
    resetForm() {
      this.form = {
        pipeline: 'stringtie',
        transcript: 'maturity',
        searchType: 'gene',
        targetInput: '',
      }
      this.$message.success('已重置表单')
    },
  },
}
</script>

<style scoped>
.expression-search-container {
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
  padding: 20px;
}

.full-width {
  width: 100%;
}

.example-link {
  margin-top: 8px;
  text-align: right;
}

.el-radio-button {
  margin-right: 10px;
}

.el-textarea {
  font-family: monospace;
}
</style>
