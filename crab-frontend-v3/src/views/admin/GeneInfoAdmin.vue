<template>
  <div class="gene-info-admin-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>基因信息管理</h2>
      <p class="desc">管理基因数据库的基础信息，支持新增、编辑、删除和批量操作</p>
    </div>

    <!-- 搜索+操作栏 -->
    <el-card shadow="hover" class="search-bar">
      <el-row :gutter="20" align="middle">
        <!-- 模糊搜索（基因ID/名称） -->
        <el-col :span="8">
          <el-input
              v-model="queryForm.keyword"
              placeholder="输入基因ID/名称搜索"
              :prefix-icon="Search"
              clearable
              @keyup.enter.prevent="getGeneInfoList"
          />
        </el-col>

        <!-- 状态筛选 -->
        <el-col :span="6">
          <el-select
              v-model="queryForm.status"
              placeholder="选择状态"
              clearable
          >
            <el-option label="全部" value=""></el-option>
            <el-option label="启用（前台可见）" value="1"></el-option>
            <el-option label="禁用（前台不可见）" value="0"></el-option>
          </el-select>
        </el-col>

        <!-- 新增按钮 -->
        <el-col :span="10" class="text-right">
          <el-button type="primary" icon="Plus" @click="openAddDialog">
            新增基因信息
          </el-button>
          <!-- 批量删除按钮（选中时显示） -->
          <el-button
              type="danger"
              icon="Delete"
              @click="handleBatchDelete"
              v-if="selectedIds.length > 0"
              style="margin-left: 10px"
          >
            批量删除（{{ selectedIds.length }}条）
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 基因列表表格 -->
    <el-card shadow="never" class="table-container">
      <el-table
          :data="geneInfoList"
          v-loading="loading"
          @selection-change="handleSelectionChange"
          border
          stripe
          style="width: 100%"
          :row-key="(row) => `${row.gene}-${row.protein}`"
      >
        <!-- 复选框列 -->
        <el-table-column type="selection" width="55" align="center" />

        <!-- 序号列 -->
        <el-table-column label="序号" width="80" align="center">
          <template #default="scope">
            {{ (queryForm.pageNum - 1) * queryForm.pageSize + scope.$index + 1 }}
          </template>
        </el-table-column>

        <!-- 基因信息列 -->
        <el-table-column prop="gene" label="基因ID" min-width="120" />
        <el-table-column prop="name" label="基因名称" min-width="150" />
        <el-table-column prop="chromosome" label="染色体" width="100" align="center" />
        <el-table-column prop="start" label="起始位置" width="120" align="center" />
        <el-table-column prop="end" label="结束位置" width="120" align="center" />
        <el-table-column prop="protein" label="蛋白ID" min-width="150" />
        <el-table-column prop="product" label="产物" min-width="150" />
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后修改时间" min-width="180" align="center" />

        <!-- 操作列 -->
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button
                type="primary"
                size="small"
                icon="Edit"
                @click="openEditDialog(scope.row)"
            >
              编辑
            </el-button>
            <el-button
                type="danger"
                size="small"
                icon="Delete"
                @click="handleSingleDelete(scope.row.id)"
                style="margin-left: 8px"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页控件 -->
      <el-pagination
          class="pagination"
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="totalCount"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogType === 'add' ? '新增基因信息' : '编辑基因信息'"
        width="700px"
        :close-on-click-modal="false"
    >
      <el-form
          ref="formRef"
          :model="form"
          :rules="formRules"
          label-width="120px"
          size="medium"
      >
        <!-- 基因ID -->
        <el-form-item label="基因ID" prop="gene">
          <el-input
              v-model="form.gene"
              placeholder="请输入基因ID（如LOC127000280）"
              :disabled="dialogType === 'edit'"
          />
        </el-form-item>

        <!-- 基因名称 -->
        <el-form-item label="基因名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入基因名称" />
        </el-form-item>

        <!-- 染色体 -->
        <el-form-item label="染色体" prop="chromosome">
          <el-input v-model="form.chromosome" placeholder="请输入染色体（如char1）" />
        </el-form-item>

        <!-- 起始位置 -->
        <el-form-item label="起始位置" prop="start">
          <el-input v-model="form.start" type="number" placeholder="请输入非负整数" />
        </el-form-item>

        <!-- 结束位置 -->
        <el-form-item label="结束位置" prop="end">
          <el-input v-model="form.end" type="number" placeholder="请输入大于起始位置的非负整数" />
        </el-form-item>

        <!-- 蛋白ID（可选） -->
        <el-form-item label="蛋白ID" prop="protein">
          <el-input v-model="form.protein" placeholder="可选，输入蛋白ID" />
        </el-form-item>

        <!-- 产物（可选） -->
        <el-form-item label="产物" prop="product">
          <el-input v-model="form.product" placeholder="可选，输入产物描述" />
        </el-form-item>

        <!-- 描述（可选） -->
        <el-form-item label="描述" prop="description">
          <el-input
              v-model="form.description"
              type="textarea"
              rows="3"
              placeholder="可选，输入基因描述（不超过500字符）"
          />
        </el-form-item>

        <!-- 状态 -->
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="启用（前台可见）" value="1"></el-option>
            <el-option label="禁用（前台不可见）" value="0"></el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 弹窗底部按钮 -->
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 1. 响应式数据
// 查询参数
// 新增：生成联合主键作为row-key（确保唯一）
const getRowKey = (row) => {
  return row.gene + '-' + row.protein; // 用Gene+Protein拼接唯一key
}

const queryForm = reactive({
  keyword: '', // 基因ID/名称模糊搜索
  status: '',  // 状态筛选
  pageNum: 1,  // 页码
  pageSize: 10 // 每页条数
})

// 列表数据
const geneInfoList = ref([])
const totalCount = ref(0)
const loading = ref(false)

// 选中的ID（批量删除用）
const selectedIds = ref([])

// 弹窗相关
const dialogVisible = ref(false)
const dialogType = ref('add') // add/edit
const formRef = ref(null)
const form = reactive({
  gene: '',
  name: '',
  chromosome: '',
  start: undefined,
  end: undefined,
  protein: '',
  product: '',
  description: '',
  status: '1' // 默认启用
})

// 表单校验规则
const formRules = reactive({
  gene: [
    { required: true, message: '请输入基因ID', trigger: 'blur' },
    { max: 50, message: '基因ID长度不能超过50字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入基因名称', trigger: 'blur' },
    { max: 100, message: '基因名称长度不能超过100字符', trigger: 'blur' }
  ],
  chromosome: [
    { required: true, message: '请输入染色体', trigger: 'blur' },
    { max: 20, message: '染色体长度不能超过20字符', trigger: 'blur' }
  ],
  start: [
    { required: true, message: '请输入起始位置', trigger: 'blur' },
    { type: 'number', min: 0, message: '起始位置必须为非负整数', trigger: 'blur' }
  ],
  end: [
    { required: true, message: '请输入结束位置', trigger: 'blur' },
    { type: 'number', min: 0, message: '结束位置必须为非负整数', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        if (form.start !== undefined && value <= form.start) {
          callback(new Error('结束位置必须大于起始位置'))
        } else {
          callback()
        }
      }, trigger: 'blur' }
  ],
  protein: [
    { max: 50, message: '蛋白ID长度不能超过50字符', trigger: 'blur' }
  ],
  product: [
    { max: 200, message: '产物长度不能超过200字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '描述长度不能超过500字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
})

// 2. 生命周期：页面挂载时加载列表
onMounted(() => {
  getGeneInfoList()
})

// 3. 核心方法
/**
 * 分页查询基因列表
 */
const getGeneInfoList = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/geneInfo/list', { params: queryForm })
    // 新增容错：确保res存在且有records和total
    if (res && res.records && res.total !== undefined) {
      geneInfoList.value = res.records
      totalCount.value = res.total
    } else {
      geneInfoList.value = []
      totalCount.value = 0
      ElMessage.warning('获取数据格式异常，请联系开发人员')
    }
  } catch (error) {
    geneInfoList.value = []
    totalCount.value = 0
    ElMessage.error('获取基因列表失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

/**
 * 每页条数变化
 */
const handleSizeChange = (size) => {
  queryForm.pageSize = size
  queryForm.pageNum = 1 // 重置为第一页
  getGeneInfoList()
}

/**
 * 页码变化
 */
const handleCurrentChange = (page) => {
  queryForm.pageNum = page
  getGeneInfoList()
}

/**
 * 选中行变化（批量删除用）
 */
const handleSelectionChange = (val) => {
  selectedIds.value = val.map(item => item.id)
}

/**
 * 打开新增弹窗
 */
const openAddDialog = () => {
  dialogType.value = 'add'
  // 重置表单
  form.id = undefined
  form.gene = ''
  form.name = ''
  form.chromosome = ''
  form.start = undefined
  form.end = undefined
  form.protein = ''
  form.product = ''
  form.description = ''
  form.status = '1'
  formRef.value?.resetFields()
  // 显示弹窗
  dialogVisible.value = true
}

/**
 * 打开编辑弹窗（回显数据）
 */
const openEditDialog = async (row) => {
  dialogType.value = 'edit'
  try {
    // 1. 关键修改：删除id路径参数，改用查询参数传递gene和protein（联合主键）
    // 原代码：const res = await request.get(`/admin/geneInfo/getById/${row.id}`)
    const data = await request.get(`/admin/geneInfo/getById`, {
      params: { gene: row.gene, protein: row.protein }
    });
    // 判断数据是否为空
    if (!data || Object.keys(data).length === 0) {
      ElMessage.warning('未查询到对应基因信息，请刷新后重试');
      dialogVisible.value = false;
      return;
    }

    // 关键新增：判断data是否存在且有值（避免data为空时访问属性）
    if (!data || Object.keys(data).length === 0) {
      ElMessage.warning('未查询到对应基因信息，请刷新后重试')
      dialogVisible.value = false
      return // 终止后续回显逻辑
    }

    // 3. 其余回显逻辑保持不变（确保gene和protein正常回显）
    form.gene = data.gene
    form.name = data.name
    form.chromosome = data.chromosome
    form.start = data.start
    form.end = data.end
    form.protein = data.protein || ''
    form.product = data.product || ''
    form.description = data.description || ''
    form.status = (data.status || 1).toString() // 转为字符串，匹配下拉框value类型

    // 显示弹窗
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取基因信息失败：' + (error.message || '未知错误'))
  }
}

/**
 * 提交表单（新增/编辑）
 */
const submitForm = async () => {
  // 1. 表单校验
  await formRef.value.validate()

  // 2. 提交接口
  try {
    if (dialogType.value === 'add') {
      // 新增
      await request.post('/admin/geneInfo/add', form)
      ElMessage.success('新增基因信息成功')
    } else {
      // 编辑
      await request.put('/admin/geneInfo/update', form)
      ElMessage.success('编辑基因信息成功')
    }
    // 3. 关闭弹窗+刷新列表
    dialogVisible.value = false
    getGeneInfoList()
  } catch (error) {
    ElMessage.error(`${dialogType.value === 'add' ? '新增' : '编辑'}失败：` + (error.message || '未知错误'))
  }
}

/**
 * 单个删除
 */
const handleSingleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除？', '警告', { type: 'warning' })
  try {
    // 改用post方法，后端对应调整为@PostMapping("/batchDelete")
    await request.post('/admin/geneInfo/batchDelete', [id])
    ElMessage.success('删除成功')
    getGeneInfoList()
  } catch (error) {
    ElMessage.error('删除失败：' + (error.message || '未知错误'))
  }
}

// 2. 批量删除（handleBatchDelete）
const handleBatchDelete = async () => {
  await ElMessageBox.confirm(`确定删除${selectedIds.value.length}条？`, '警告', { type: 'warning' })
  try {
    // 改用post方法，传递ids数组
    await request.post('/admin/geneInfo/batchDelete', selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    getGeneInfoList()
  } catch (error) {
    ElMessage.error('批量删除失败：' + (error.message || '未知错误'))
  }
}
</script>

<style scoped>
.gene-info-admin-container {
  padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 20px;
  color: #333;
  margin-bottom: 8px;
}

.page-header .desc {
  font-size: 14px;
  color: #666;
}

.search-bar {
  margin-bottom: 20px;
}

.table-container {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 15px;
  text-align: right;
}

.text-right {
  text-align: right;
}
</style>