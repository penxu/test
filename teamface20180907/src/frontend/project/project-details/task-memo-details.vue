<template>
  <div class="task-memo-details">
    <i class="el-icon-close" @click="clostSideModel"></i>
    <memoDetailNew v-if="(JSON.stringify(memoDetailData) !== '{}')" :memoDetailProp="memoDetailData" :dropOptionValue="dropOptionValue"></memoDetailNew>
  </div>
</template>
<script>
// import memoDetails from '@/frontend/memo/memo-detail'
import memoDetailNew from '@/frontend/project/project-details/memo-details-new'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'TaskMemoDetails',
  props: ['dtlKey', 'id'],
  components: {memoDetailNew},
  data () {
    return {
      data: {},
      memoDetailData: {},
      dropOptionValue: 0
    }
  },
  mounted () {
    this.getMemoDetail(this.id)
  },
  methods: {
    // 获取详情
    getMemoDetail (id) {
      this.classFocusIndex = id
      HTTPServer.findMemoDetail({id: id}, true).then((res) => {
        // this.memoDetailData = res
        this.getGuanlianList(id, res)
      })
    },
    // 根据id获取关联列表
    getGuanlianList (id, res1) {
      // 根据当前id获取关联列表
      this.$http.memoFindRelationList({'id': id}).then(res => {
        res1.guanlianList = res.moduleDataList
        res1.guanlianList.map(item => {
          if (typeof (item.id) === 'object') {
            item.beanId = item.id.value
          } else {
            item.beanId = item.id
          }
        })
        this.memoDetailData = res1
      })
    },
    clostSideModel () {
      this.$bus.emit('closeTaskModal', JSON.stringify({dtlKey: this.dtlKey}))
    }
  }
}
</script>
<style lang="scss" scoped>

</style>

<style lang="scss">
// @import '../../common/scss/dialog.scss'; // 弹窗公共样式
.task-memo-details {
  background: #FFFFFF;
  position: fixed;
  width: 800px;
  top: 0;
  bottom: 0;
  right: 0;
  z-index: 10;
  box-shadow: -2px 2px 4px 0 rgba(155, 155, 155, 0.3), 2px -2px 4px 0;
  height: 100%;
  >i.el-icon-close{
    position: absolute;top:22px;right:30px;font-size:20px;&:hover{cursor:pointer;}
  }
}
</style>
