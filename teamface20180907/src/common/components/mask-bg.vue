<template>
  <div class="mask-bg-wrap" v-if="show" @click="closeAlert">
  </div>
</template>

<script>
export default {
  name: 'MaskBg',
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
    }
  },
  methods: {
    closeAlert () {
      this.$bus.emit('closeMask', false)
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
.mask-bg-wrap{
  position: fixed;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: transparent;
}
</style>
