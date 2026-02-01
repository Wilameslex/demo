<template>
  <div class="gene-table-container">
    <el-table
        :data="tableData"
        :loading="loading"
        stripe
        border
        height="400"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="Gene" label="基因ID" width="120" sortable />
      <el-table-column
          prop="Name"
          label="基因名称"
          width="150"
          show-overflow-tooltip
      />
      <el-table-column
          prop="Chromosome"
          label="染色体"
          width="100"
          align="center"
      />
      <el-table-column prop="Start" label="起始位置" width="120" sortable />
      <el-table-column prop="End" label="结束位置" width="120" sortable />
      <el-table-column prop="Protein" label="蛋白ID" width="150" />
      <el-table-column prop="Product" label="产物" width="150" show-overflow-tooltip />
      <el-table-column
          prop="Description"
          label="描述"
          min-width="200"
          show-overflow-tooltip
      />
<!--      <el-table-column label="操作" width="120" align="center" fixed="right">-->
<!--        <template #default="scope">-->
<!--          <el-button type="primary" size="small" @click="handleView(scope.row)">-->
<!--            查看-->
<!--          </el-button>-->
<!--        </template>-->
<!--      </el-table-column>-->
    </el-table>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

// 定义 props
const props = defineProps({
  tableData: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

// 定义 emits
const emit = defineEmits(['selection-change', 'sort-change', 'view'])

// 处理方法
const handleSelectionChange = (selection) => {
  emit('selection-change', selection)
}

const handleSortChange = (sortInfo) => {
  emit('sort-change', sortInfo)
}

const handleView = (row) => {
  emit('view', row)
}
</script>

<style scoped>
.gene-table-container {
  width: 100%;
}

.el-table {
  font-size: 14px;
}

.el-table :deep(.el-table__header th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

.el-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>