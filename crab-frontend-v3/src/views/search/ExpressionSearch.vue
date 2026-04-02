<template>
  <div class="expression-search-container">
    <div class="header">
      <h1><i class="el-icon-search"></i> Expression Data of Genes</h1>
      <p class="sub-title">You can search genes' expression from certain transcriptome</p>
    </div>

    <el-card class="search-card" shadow="hover">
      <el-form label-position="top">
        <!-- 流程选择 -->
        <el-form-item label="Pipeline">
          <el-radio-group v-model="form.pipeline">
            <el-radio-button label="stringtie">STAR+Stringtie</el-radio-button>
            <el-radio-button label="rsem">STAR+RSEM</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 转录组选择 -->
        <el-form-item label="Transcriptome">
          <el-select
              v-model="form.transcript"
              placeholder="Choose a transcriptome"
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
        <el-form-item label="Type">
          <el-radio-group v-model="form.searchType">
            <el-radio label="gene">Expression of gene(s)</el-radio>
            <el-radio label="transcript">Expression of transcript(s)</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 动态输入区域 -->
        <el-form-item
            :label="form.searchType === 'gene' ? 'gene list' : 'transcript list'"
        >
          <el-input
              v-model="form.targetInput"
              type="textarea"
              :rows="5"
              :placeholder="getPlaceholder()"
          ></el-input>
          <div class="example-link">
            <el-link type="info" @click="fillExample">
              <i class="el-icon-info"></i> example
            </el-link>
          </div>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              icon="el-icon-search"
              @click="handleSearch"
              :loading="loading"
          >
            Start search
          </el-button>
          <el-button @click="resetForm">Reset</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { searchExpression } from '@/api/expression';

export default {
  name: 'ExpressionSearch',
  data() {
    return {
      loading: false,
      form: {
        pipeline: 'stringtie',
        transcript: 'maturity',
        searchType: 'gene',
        targetInput: ''
      },
      transcriptOptions: [
        { value: 'maturity', label: 'Tissues of 2year Individuals (CRA 003690)' },
        { value: 'metamorphosis', label: 'Metamorphosis (CRA 003690)' },
        { value: 'molt', label: 'Molting (PRJNA271233)' },
        { value: 'carcinization', label: 'Carcinization (PRJNA644959)' },
        { value: 'premature', label: 'Precocious (PRJNA488872)' },
        { value: 'outwater', label: 'Aerial Respiration (PRJNA480555)' },
        { value: 'photoperiodschange', label: 'Different Photoperiods (PRJNA501841)' },
        { value: 'relimb', label: 'Limb Regeneration (PRJNA733310)' },
      ]
    };
  },
  methods: {
    getPlaceholder() {
      return this.form.searchType === 'gene'
          ? 'Each gene id per line, eg:\nLOC127000532\nLOC127000280'
          : 'Each transcript id per line, eg:\nXM_050863983.1\nXM_050864059.1';
    },
    fillExample() {
      this.form.targetInput = this.form.searchType === 'gene'
          ? 'LOC127000532\nLOC127000280\nLOC127000531'
          : 'XM_050863983.1\nXM_050864059.1\nXM_050864060.1';
      this.$message.success('Example data have been filled');
    },
    async handleSearch() {
      if (!this.form.targetInput.trim()) {
        this.$message.warning('Please input your target genes or transcripts');
        return;
      }

      this.loading = true;
      try {
        const targetIds = this.form.targetInput
            .split('\n')
            .map(id => id.trim())
            .filter(id => id);

        if (targetIds.length > 100) {
          this.$message.warning('At most 100 ids are allowed for each search');
          return;
        }

        // 修复点：正确构建请求参数
        const requestParams = {
          pipeline: this.form.pipeline,
          transcriptome: this.form.transcript,
          searchType: this.form.searchType,
          targetIds: targetIds
        };
        // 保存参数到sessionStorage
        sessionStorage.setItem('expressionQuery', JSON.stringify(requestParams));

        console.log('Send search request:', requestParams);

        // 修复点：传递正确的参数
        const success = await this.$store.dispatch(
            'expression/searchExpression',
            requestParams
        );

        if (success) {
          this.$router.push({
            name: 'ExpressionResults' })
        } else {
          this.$message.error('Search fails, please try again');
        }
      } catch (error) {
        console.error('errors from search', {
          message: error.message,
          response: error.response,
          config: error.config
        });

        let errorMessage = 'Search fails: ';
        if (error.response) {
          errorMessage += `[${error.response.status}] ${error.response.data?.message || '服务器错误'}`;
        } else {
          errorMessage += error.message || 'unknown errors';
        }

        this.$message.error(errorMessage);
      } finally {
        this.loading = false;
      }
    },
    resetForm() {
      this.form = {
        pipeline: 'stringtie',
        transcript: 'maturity',
        searchType: 'gene',
        targetInput: ''
      };
      this.$message.success('The form has been reset successfully');
    }
  }
};
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

