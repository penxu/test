<template>
  <div class="custom-drag-wrip">
    <custom-drag-left></custom-drag-left>
    <custom-drag-center></custom-drag-center>
    <custom-drag-right></custom-drag-right>
  </div>
</template>

<script>
import CustomDragLeft from '@/backend/custom/components/custom-drag-left'
import CustomDragCenter from '@/backend/custom/components/custom-drag-center'
import CustomDragRight from '@/backend/custom/components/custom-drag-right'
export default {
  name: 'CustomDrag',
  components: {
    CustomDragLeft,
    CustomDragCenter,
    CustomDragRight
  },
  data () {
    return {

    }
  },
  created () {
  },
  methods: {

  }
}
</script>

<style lang="scss" scoped>
.custom-drag-wrip{
  display: flex;
  height: calc(100% - 60px);
  overflow: hidden;
}
</style>
