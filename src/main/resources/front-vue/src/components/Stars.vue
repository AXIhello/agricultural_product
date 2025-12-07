<template>
  <span class="stars">
    <i v-for="i in 5" :key="i" :class="i <= solid ? 'solid' : 'hollow'">★</i>
  </span>
</template>

<script setup>
const props = defineProps({ rating: { type: Number, default: 0 } })
const solid = Math.round(props.rating || 0)
</script>

<style scoped>
.stars { font-size: 14px; letter-spacing: 2px; }
.stars .solid { color: #ffd43b; }
.stars .hollow { color: #e0e0e0; }
</style>