<template>
  <div class="custom-drag-wrip">
    <custom-drag-left-page :pageNum="pageNum"></custom-drag-left-page>
    <custom-drag-center-page :pageNum="pageNum"></custom-drag-center-page>
    <custom-drag-right-page :pageNum="pageNum"></custom-drag-right-page>
  </div>
</template>

<script>
import CustomDragLeftPage from '@/backend/custom/components/custom-drag-left-page'
import CustomDragCenterPage from '@/backend/custom/components/custom-drag-center-page'
import CustomDragRightPage from '@/backend/custom/components/custom-drag-right-page'
export default {
  name: 'CustomDragPage',
  components: {
    CustomDragLeftPage,
    CustomDragCenterPage,
    CustomDragRightPage
  },
  props: ['pageNum'],
  data () {
    return {

    }
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
