<template>
  <div v-if="visible" class="img-mask" @click="close">
    <img :src="src" class="img-preview" @click.stop />
  </div>
</template>

<script setup>
defineProps({
  visible: Boolean,
  src: String
})
const emit = defineEmits(['close'])
const close = () => emit('close')
</script>

<style scoped>
.img-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.img-preview {
  max-width: 80vw;
  max-height: 80vh;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.6);
}
</style>