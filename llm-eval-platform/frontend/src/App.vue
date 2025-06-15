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
</style>
