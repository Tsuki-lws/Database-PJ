<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <el-menu
        router
        :default-active="route.path"
        background-color="#1f2d3d"
        text-color="#ffffff"
        active-text-color="#409EFF"
        class="el-menu-vertical">
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-sub-menu index="/questions">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>问题管理</span>
          </template>
          <el-menu-item index="/questions">问题列表</el-menu-item>
          <el-menu-item index="/questions/create">创建问题</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header>
        <span class="header-title">LLM评测平台</span>
      </el-header>
      
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { HomeFilled, Document } from '@element-plus/icons-vue'

const route = useRoute()

// 添加hover效果和修复文字颜色问题
const customStyles = document.createElement('style')
customStyles.innerHTML = `
  .el-menu-item:hover, .el-sub-menu__title:hover {
    background-color: #283446 !important;
  }
  .el-menu--popup {
    background-color: #1f2d3d !important;
  }
  .el-menu--popup .el-menu-item {
    color: #ffffff !important;
  }
  .el-menu--popup .el-menu-item:hover {
    background-color: #283446 !important;
  }
  .el-menu--popup .el-menu-item.is-active {
    color: #409EFF !important;
  }
`
document.head.appendChild(customStyles)
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100%;
}

.el-aside {
  background-color: #1f2d3d;
  color: #fff;
  height: 100%;
}

.el-menu {
  border-right: none;
  background-color: #1f2d3d;
}

.el-menu-vertical {
  width: 200px;
  min-height: 100%;
}

.el-menu-item, .el-menu .el-sub-menu__title {
  color: #ffffff !important;
  font-weight: 500;
}

.el-menu-item span, .el-sub-menu__title span {
  color: #ffffff !important;
}

.el-menu-item .el-icon, .el-sub-menu__title .el-icon {
  color: #ffffff !important;
}

.el-menu-item.is-active {
  color: #409EFF !important;
  background-color: #283446;
}

.el-menu-item.is-active span {
  color: #409EFF !important;
}

.el-header {
  background-color: #ffffff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  height: 60px;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}
</style> 