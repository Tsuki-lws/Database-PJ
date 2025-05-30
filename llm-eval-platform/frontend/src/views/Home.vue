<template>
  <div class="home">
    <h1>欢迎使用LLM评测平台</h1>
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>标准问题总数</span>
              </div>
            </template>
            <div class="card-content">
              <el-statistic :value="statistics.questionCount">
                <template #title>
                  <div style="display: inline-flex; align-items: center">
                    问题数量
                    <el-icon style="margin-left: 4px">
                      <Question />
                    </el-icon>
                  </div>
                </template>
              </el-statistic>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>标准答案总数</span>
              </div>
            </template>
            <div class="card-content">
              <el-statistic :value="statistics.answerCount">
                <template #title>
                  <div style="display: inline-flex; align-items: center">
                    答案数量
                    <el-icon style="margin-left: 4px">
                      <Document />
                    </el-icon>
                  </div>
                </template>
              </el-statistic>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>数据集版本数</span>
              </div>
            </template>
            <div class="card-content">
              <el-statistic :value="statistics.datasetCount">
                <template #title>
                  <div style="display: inline-flex; align-items: center">
                    数据集数量
                    <el-icon style="margin-left: 4px">
                      <DataLine />
                    </el-icon>
                  </div>
                </template>
              </el-statistic>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>评测次数</span>
              </div>
            </template>
            <div class="card-content">
              <el-statistic :value="statistics.evaluationCount">
                <template #title>
                  <div style="display: inline-flex; align-items: center">
                    评测数量
                    <el-icon style="margin-left: 4px">
                      <TrendCharts />
                    </el-icon>
                  </div>
                </template>
              </el-statistic>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Document, DataLine, TrendCharts } from '@element-plus/icons-vue'

const statistics = ref({
  questionCount: 0,
  answerCount: 0,
  datasetCount: 0,
  evaluationCount: 0
})

// TODO: 从后端获取统计数据
</script>

<style scoped>
.home {
  padding: 20px;
}

.stats-cards {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  text-align: center;
}
</style> 