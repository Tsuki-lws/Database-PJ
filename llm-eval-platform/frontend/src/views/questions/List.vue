<template>
  <div class="question-list">
    <div class="header">
      <h2>标准问题列表</h2>
      <el-button type="primary" @click="$router.push('/questions/create')">
        创建问题
      </el-button>
    </div>

    <el-form :inline="true" class="search-form">
      <el-form-item label="问题类型">
        <el-select v-model="queryParams.questionType" placeholder="选择问题类型" clearable>
          <el-option label="主观题" value="subjective" />
          <el-option label="客观题" value="objective" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadQuestions">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="questionList" style="width: 100%" v-loading="loading">
      <el-table-column prop="standardQuestionId" label="ID" width="80" />
      <el-table-column prop="content" label="问题内容" show-overflow-tooltip />
      <el-table-column prop="questionType" label="问题类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.questionType === 'subjective' ? 'success' : 'info'">
            {{ row.questionType === 'subjective' ? '主观题' : '客观题' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="version" label="版本" width="80" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/questions/edit/${row.standardQuestionId}`)">
            编辑
          </el-button>
          <el-button link type="primary" @click="viewHistory(row)">
            历史版本
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getQuestionList } from '@/api/question'
import type { StandardQuestion, QueryParams } from '@/api/question'

const loading = ref(false)
const total = ref(0)
const questionList = ref<StandardQuestion[]>([])

const queryParams = reactive<QueryParams>({
  current: 1,
  size: 10
})

const loadQuestions = async () => {
  loading.value = true
  try {
    const res = await getQuestionList(queryParams)
    questionList.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryParams.questionType = undefined
  queryParams.current = 1
  loadQuestions()
}

const handleSizeChange = (val: number) => {
  queryParams.size = val
  loadQuestions()
}

const handleCurrentChange = (val: number) => {
  queryParams.current = val
  loadQuestions()
}

const viewHistory = (row: StandardQuestion) => {
  // TODO: 实现查看历史版本功能
}

onMounted(() => {
  loadQuestions()
})
</script>

<style scoped>
.question-list {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 