<template>
  <transition-group name="slide">
    <ModuleDetail v-for="(itemThis,index) in dataAll" :key="index"  :data="itemThis" :dtlKey="index"></ModuleDetail>
  </transition-group>
</template>
<script>
import ModuleDetail from '@/frontend/module/module-detail'// 自定义详情
export default {
  name: 'otherDetail',
  components: {ModuleDetail},
  props: ['dataAll', 'moduleId', 'moduleName', 'dtlKey'],
  data () {
    return {
      data: {},
      detailList: [] // 自定义列表页面
    }
  },
  mounted () {
    // 打开详情窗口
    this.$bus.on('openDataDtl', (value, id, bean) => { // 自定义详情
      let data = {
        bean: bean,
        dataId: id,
        moduleId: this.moduleId,
        moduleName: this.moduleName
      }
      this.dataAll.push(data)
    })
    // 关闭自定义详情窗口
    this.$bus.on('closeDetailModal', value => {
      this.dataAll.splice(value, 1)
      if (this.dataAll.length === 0) {
        this.$bus.emit('closeTaskModal', JSON.stringify({dtlKey: this.dtlKey}))
      }
    })
  }
}
</script>
<style lang="scss" scoped>

</style>
<style lang="scss">

</style>
