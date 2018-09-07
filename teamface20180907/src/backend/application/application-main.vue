<template>
  <div>
    <div class="application-main-wrip">
    <div v-if="judgeApply" class="application">
      <div class='header'>
        <i class="iconfont icon-zidingyiguanlix"></i>
        <span>应用中心</span>
      </div>
      <apply-list :judgeApply="judge"></apply-list>
    </div>
  </div>
  <upload-apply v-if="uploadDialogVisible" :dataId="dataId" :appId="'notEditable'"></upload-apply>
  </div>
</template>
<script>
import ApplyList from '@/backend/components/apply-list'
import UploadApply from '@/common/components/upload-apply.1'
export default {
  name: 'Application-mainMain',
  components: {ApplyList, UploadApply},
  data () {
    return {
      judgeApply: true,
      uploadDialogVisible: false,
      dataId: '',
      judge: false
    }
  },
  mounted () {
    this.$bus.on('listentTouploadApply', (value) => {
      this.judgeApply = false
      this.uploadDialogVisible = true
      this.dataId = value
    })
    this.$bus.on('listentReturn', (value) => {
      this.uploadDialogVisible = value
      this.judgeApply = true
      this.judge = true
    })
  }
}
</script>

<style lang="scss" scoped>
.application-main-wrip{
  height: 100%;
  padding: 0 25px 0 30px;
  overflow: auto;
  .application{
    .header{
    border-bottom: 1px solid #E7E7E7;
    overflow: hidden;
    height: 60px;
    margin-bottom:25px;
    line-height: 60px;
    i{
      float: left;
      margin: 20px 10px 0 0;
      font-size: 20px;
      line-height: 1;
      }
      span{
        font-size: 18px;
        color: #69696C;
      }
    }
  }
}
</style>
