<template>
  <div class="main-bg">
    <!-- 主体面板：农户入口 -->
    <div class="panel">
      <header>
        <h1>农产通</h1>
        <p>农户入口</p>
      </header>

      <!-- 农户按钮 -->
      <button class="choice" @click="$emit('select', 'farmer')">
        <span>🌾 农户</span>
      </button>
    </div>
  </div>
</template>

<script setup>

</script>

<style scoped>
.main-bg {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #000;
  color: #fff;
}

/* 面板区域 */
.panel {
  text-align: center;
  padding: 20px;
  border: 1px solid #444;
  border-radius: 8px;
}

.choice {
  margin-top: 16px;
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  background: #222;
  color: #fff;
  cursor: pointer;
}
.choice:hover {
  background: #333;
}
</style>
