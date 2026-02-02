<template>
  <div class="pathway-results-container">
    <div class="header">
      <h1><i class="el-icon-collection"></i> Pathway Search Result</h1>
      <div class="sub-header">
        <el-tag type="info">{{ totalItems }} records in total</el-tag>
        <el-button
            icon="el-icon-back"
            @click="backToSearch"
            size="small"
        >
          return search
        </el-button>
      </div>
    </div>

    <el-card shadow="never" class="search-params-card" style="margin-bottom: 20px;">
      <div class="params-header">
        <span class="params-title"><i class="el-icon-search"></i> Search Criteria</span>
      </div>
      <div class="params-content">
        <!-- 通路ID（如ko04141） -->
        <div v-if="searchParams.pathwayId?.length > 0" class="param-item">
          <span class="param-label">Pathway ID：</span>
          <el-tag type="primary" effect="plain" class="param-tag" v-for="id in searchParams.pathwayId" :key="`pathwayId-${id}`">
            {{ id }}
          </el-tag>
        </div>
        <!-- 通路名称（如Endocytosis） -->
        <div v-if="searchParams.pathwayName" class="param-item">
          <span class="param-label">Pathway Name：</span>
          <el-tag type="success" effect="plain" class="param-tag">
            {{ searchParams.pathwayName }}
          </el-tag>
        </div>
        <!-- 基因列表（批量查询的基因ID） -->
        <div v-if="searchParams.genes?.length > 0" class="param-item">
          <span class="param-label">Gene List：</span>
          <el-tag type="info" effect="plain" class="param-tag" v-for="gene in searchParams.genes" :key="`gene-${gene}`">
            {{ gene }}
          </el-tag>
        </div>
        <!-- 容错显示（无有效条件时） -->
        <div v-if="!hasAnyParams" class="param-empty">
          No valid search criteria (fault tolerance display)
        </div>
      </div>
    </el-card>

    <!-- 筛选工具栏 -->
    <el-card shadow="never" class="filter-card">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-input
              v-model="filterText"
              placeholder="search in the results..."
              prefix-icon="el-icon-search"
              clearable
          />
        </el-col>
        <el-col :span="8" class="text-right">
          <el-dropdown trigger="click" @command="handleExport">
            <el-button type="primary" icon="el-icon-download">
              Export results<i class="el-icon-arrow-down el-icon--right"></i>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="csv">CSV</el-dropdown-item>
                <el-dropdown-item command="txt">TXT</el-dropdown-item>
                <el-dropdown-item command="excel">Excel</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-col>
      </el-row>
    </el-card>

    <!-- 结果表格 -->
    <el-card shadow="never">
      <el-table
          :data="searchResults"
          v-loading="loading"
          stripe
          border
          highlight-current-row
          style="width: 100%"
          :height="tableHeight"
      >
        <el-table-column
            prop="gene"
            label="Gene"
            width="120"
        />
        <el-table-column
            prop="description"
            label="Description"
            min-width="200"
        />
        <el-table-column
            prop="name"
            label="Name"
            min-width="180"
        />
        <el-table-column
            prop="ec"
            label="EC"
            min-width="150"
        >
          <template #default="{ row }">
            <div v-if="row.ec">
              <span v-for="(ec, index) in splitValues(row.ec)" :key="index">
                <a :href="'https://www.genome.jp/entry/EC:' + ec" target="_blank" class="link">{{ ec }}</a>
                <span v-if="index < splitValues(row.ec).length - 1">, </span>
              </span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column
            prop="ko"
            label="KO"
            min-width="150"
        >
          <template #default="{ row }">
            <div v-if="row.ko">
              <span v-for="(ko, index) in row.ko.split(',')" :key="index">
                <a :href="'https://www.genome.jp/entry/' + ko"
                   target="_blank"
                   class="link">
                  {{ ko }}</a>
                <span v-if="index < row.ko.split(',').length - 1">, </span>
              </span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column
            prop="keggGene"
            label="KEGG Gene ID"
            min-width="150"
        >
          <template #default="{ row }">
            <div v-if="row.keggGene">
              <a :href="'https://www.kegg.jp/entry/' + row.keggGene" target="_blank" class="link">{{ row.keggGene }}</a>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column
            prop="score"
            label="Score"
            width="100"
        >
          <template #default="{ row }">
            <span v-if="row.score">{{ row.score }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页控件 -->
      <el-pagination
          class="pagination"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalItems"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
      />
    </el-card>
    <!-- 导出进度对话框 -->
    <el-dialog
        title="exporting progress"
        v-model="exportDialogVisible"
        width="30%"
        :close-on-click-modal="false"
        :show-close="false"
    >
      <el-progress
          :percentage="exportProgress"
          :status="exportStatus"
          :text-inside="true"
          :stroke-width="20"
      />
      <p v-if="exportStatus === 'success'" class="success-message">
        {{ exportMessage }}
      </p>
      <p v-else class="export-message">{{ exportMessage }}</p>
      <template #footer>
        <el-button
            v-if="exportStatus === 'success'"
            type="primary"
            @click="exportDialogVisible = false"
        >
          Completed
        </el-button>
        <el-button
            v-else-if="exportStatus === 'exception'"
            @click="exportDialogVisible = false"
        >
          Close
        </el-button>
        <el-button v-else type="danger" @click="cancelExport">
          Cancel
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'
import * as XLSX from 'xlsx'

export default {
  name: 'PathwayResults',
  data() {
    return {
      filterText: '',
      currentPage: 1,
      pageSize: 10,
      loading: false,
      exportDialogVisible: false,
      exportProgress: 0,
      exportStatus: '',
      exportMessage: 'Data is exporting...',
      exportFormat: '',
      exportTimer: null
    }
  },
  computed: {
    ...mapState('pathway', [
      'searchResults',
      'searchParams',
      'totalItems'
    ]),

    hasAnyParams() {
      return (this.searchParams?.pathwayId?.length > 0) ||
          (this.searchParams?.pathwayName) ||
          (this.searchParams?.genes?.length > 0);
    },

    filteredResults() {
      if (!this.filterText) return this.searchResults

      const keyword = this.filterText.toLowerCase()
      return this.searchResults.filter(item => {
        return Object.values(item).some(value =>
            String(value).toLowerCase().includes(keyword)
        )
      })
    },

    tableHeight() {
      return window.innerHeight - 320 + 'px'
    }
  },
  methods: {
    ...mapActions('pathway', ['searchPathway']),
    backToSearch() {
      this.$router.push({ name: 'PathwaySearch' })
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.refreshResults()
    },
    handlePageChange(page) {
      this.currentPage = page
      this.refreshResults()
    },
    async refreshResults() {
      this.loading = true
      try {
        await this.searchPathway({
          ...this.searchParams,
          page: this.currentPage,
          size: this.pageSize
        })
      } catch (error) {
        console.error('Fail to refresh:', error)
      } finally {
        this.loading = false
      }
    },
    handleExport(command) {
      this.exportFormat = command
      this.exportDialogVisible = true
      this.exportStatus = ''
      this.exportProgress = 0
      this.exportMessage = 'Data is exporting...'

      this.exportTimer = setInterval(() => {
        if (this.exportProgress < 90) {
          this.exportProgress += 10
          this.exportMessage = `Data is exporting (${this.exportProgress}%)...`
        }
      }, 200)

      setTimeout(() => {
        this.performExport()
      }, 500)
    },

    performExport() {
      try {
        const exportData = this.filteredResults
        if (!exportData || exportData.length === 0) {
          throw new Error('no data to export')
        }

        let content = ''
        let filename = `KEGG pathway search result_${new Date().toLocaleDateString().replace(/\//g, '-')}`

        switch (this.exportFormat) {
          case 'txt':
            content = this.exportToTxt(exportData)
            filename += '.txt'
            break
          case 'csv':
            content = this.exportToCsv(exportData)
            filename += '.csv'
            break
          case 'excel':
            this.exportToExcel(exportData, filename)
            clearInterval(this.exportTimer)
            this.exportProgress = 100
            this.exportStatus = 'success'
            this.exportMessage = 'Excel is downloaded successfully'
            return
          default:
            throw new Error('type is not supported')
        }

        const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = filename
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)

        clearInterval(this.exportTimer)
        this.exportProgress = 100
        this.exportStatus = 'success'
        this.exportMessage = `file has been generated: ${filename}`
      } catch (error) {
        clearInterval(this.exportTimer)
        this.exportProgress = 100
        this.exportStatus = 'exception'
        this.exportMessage = `fail to export: ${error.message}`
        console.error('exporting errors:', error)
      }
    },

    exportToTxt(data) {
      const headers = ['Gene', 'Description', 'Name', 'EC', 'KO', 'KEGG Gene ID', 'Score']
      const fields = ['gene', 'description', 'name', 'ec', 'ko', 'keggGene', 'score']

      let txtContent = 'KEGG pathway search result\n\n'
      txtContent += `export time: ${new Date().toLocaleString()}\n`
      txtContent += `records: ${data.length}\n\n`
      txtContent += headers.join('\t') + '\n'
      txtContent += '-'.repeat(100) + '\n'

      data.forEach(item => {
        const row = fields.map(field => {
          let value = item[field] || ''
          return value.toString().replace(/\n/g, '; ')
        })
        txtContent += row.join('\t') + '\n'
      })

      return txtContent
    },

    exportToCsv(data) {
      const headers = ['Gene', 'Description', 'Name', 'EC', 'KO', 'KEGG Gene ID', 'Score']
      const fields = ['gene', 'description', 'name', 'ec', 'ko', 'keggGene', 'score']

      let csvContent = headers.map(h => `"${h}"`).join(',') + '\n'

      data.forEach(item => {
        const row = fields.map(field => {
          let value = item[field] || ''
          if (typeof value === 'string') {
            value = value.replace(/"/g, '""')
            if (value.includes(',') || value.includes('\n') || value.includes('"')) {
              value = `"${value}"`
            }
          }
          return value
        })
        csvContent += row.join(',') + '\n'
      })

      return csvContent
    },

    exportToExcel(data, filename) {
      const headers = ['Gene', 'Description', 'Name', 'EC', 'KO', 'KEGG Gene ID', 'Score']
      const fields = ['gene', 'description', 'name', 'ec', 'ko', 'keggGene', 'score']

      const excelData = [
        headers,
        ...data.map(item => fields.map(field => item[field] || ''))
      ]

      const wb = XLSX.utils.book_new()
      const ws = XLSX.utils.aoa_to_sheet(excelData)
      XLSX.utils.book_append_sheet(wb, ws, 'KEGG pathway result result')
      XLSX.writeFile(wb, `${filename}.xlsx`)
    },

    cancelExport() {
      clearInterval(this.exportTimer)
      this.exportDialogVisible = false
    },
    // 分割逗号分隔的字符串，并去除空格
    splitValues(value) {
      if (!value) return []
      return value.split(',').map(item => item.trim()).filter(item => item)
    }
  },


  mounted() {
    this.currentPage = this.searchParams.page || 1
    this.pageSize = this.searchParams.size || 10
  }
}
</script>

<style scoped>
.pathway-results-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.sub-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.search-params-card {
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.params-header {
  margin-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 8px;
}

.params-title {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.params-content {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
  padding-top: 10px;
}

.param-item {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.param-label {
  color: #606266;
  font-size: 14px;
  white-space: nowrap;
}

.param-tag {
  margin-bottom: 4px;
  cursor: default;
  font-size: 13px;
}

.param-empty {
  color: #909399;
  font-size: 14px;
  padding: 5px 0;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.link {
  color: #409EFF;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}
/* 添加导出相关样式 */
.export-message {
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
  color: #606266;
}

.success-message {
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
  color: #67C23A;
  font-weight: bold;
}

.text-right {
  text-align: right;
}
</style>
