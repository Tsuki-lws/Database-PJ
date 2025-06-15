<script setup lang="ts">
// 不需要引入额外组件，仅使用路由系统
</script>

<template>
  <router-view />
</template>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
  overflow-x: hidden;
  background-color: #f0f2f5;
}

#app {
  width: 100%;
  height: 100vh;
}

/* 解决对话框无法点击的问题 */
.el-overlay {
  z-index: 3000 !important;
}

.el-dialog {
  z-index: 3001 !important;
}

.el-message-box {
  z-index: 3001 !important;
}
</style>
