<template>
  <el-dialog title="数据共享设置" :visible.sync="visible" width="600px" class="data-share-wrip common-dialog">
    <div class="content">
        <div class="title-button">指定的数据共享设置。可将部门或成员一起共享此数据。<el-button size="mini" type="primary"  plain @click="addData">新 增</el-button></div>
        <div class="table">
          <div class="title">
            <span>共享给</span><span>访问权限</span><span>操作</span>
          </div>
        <div class="list" v-for="share in shareSetList" :key="share.id">
          <span>{{share.target_lable}}</span>
          <span>{{share.access_permissions | visitAuth(share.access_permissions)}}</span>
          <span><a @click="updataShare(share)">编辑</a><a @click="delShare(share.id)">删除</a></span>
        </div>
        </div>
    </div>
  </el-dialog>
</template>

<script>
import MemberCommit from './member-commit'// 转移负责人,数据共享,公海池分配
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'DataShare',
  components: {
    MemberCommit
  },
  data () {
    return {
      visible: false,
      bean: '',
      dataId: '',
      shareSetList: []
    }
  },
  methods: {
    // 新增数据
    addData () {
      let value = {bean: this.bean, type: 2, ids: this.dataId, isSingleShare: true}
      this.$bus.emit('openMemberCommit', value)
    },
    // 编辑分享设置
    updataShare (data) {
      let share = {
        id: data.id,
        access_permissions: data.access_permissions,
        allot_employee: data.allot_employee
      }
      let value = {bean: this.bean, type: 3, ids: this.dataId, isSingleShare: true, share: share}
      this.$bus.emit('openMemberCommit', value)
    },
    // 删除分享设置
    delShare (id) {
      this.$confirm('确定要删除吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.ajaxDelShareData({id: id})
      }).catch(() => {
      })
    },
    // 获取分享列表
    getShareSetList (data) {
      HTTPServer.getShareSetList(data, 'Loading').then((res) => {
        this.shareSetList = res
      })
    },
    // 删除分享数据
    ajaxDelShareData (data) {
      HTTPServer.deleteShareData(data, 'Loading').then((res) => {
        this.$message.success('删除成功!')
        this.getShareSetList({dataId: this.dataId})
      })
    }
  },
  mounted () {
    // 打开弹框
    this.$bus.off('openDataShare')
    this.$bus.on('openDataShare', value => {
      this.visible = true
      this.bean = value.bean
      this.dataId = value.dataId
      this.getShareSetList({dataId: this.dataId})
    })
    // 刷新列表
    this.$bus.off('refreashShareList')
    this.$bus.on('refreashShareList', value => {
      this.getShareSetList({dataId: this.dataId})
    })
  },
  filters: {
    visitAuth (code) {
      let text
      if (code === '0') {
        text = '只读'
      } else if (code === '1') {
        text = '读写'
      } else {
        text = '读写删'
      }
      return text
    }
  }
}
</script>

<style lang="scss">
@import '~@/../static/css/dialog.scss';
.data-share-wrip{
  .title-button{
    height: 30px;
    line-height: 30px;
    color: #4A4A4A;
    .el-button{
      width: 80px;
      float: right;
    }
  }
  .table{
    overflow: auto;
    margin: 10px 0 0 0;
    max-height: 500px;
    .title{
      display: flex;
      height: 46px;
      line-height: 46px;
      padding: 0 0 0 10px;
      border-top:1px solid #D9D9D9;
      border-bottom:1px solid #D9D9D9;
      box-sizing: content-box;
      span{
        display: block;
        flex: 0 0 80px;
        border-left: 1px solid #e7e7e7;
        padding: 10px;
        height: 10px;
        box-sizing: border-box;
        line-height: 3px;
        margin: 10px;
        &:first-child{
          flex: 1;
          border: none;
          padding: 0;
          margin: 0;
          height: 46px;
          line-height: 46px;
        }
      }
    }
    .list{
      display: flex;
      height: 50px;
      line-height: 50px;
      padding: 0 0 0 10px;
      border-bottom:1px solid #D9D9D9;
      span{
        display: block;
        &:nth-child(1){
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        &:nth-child(2){
          flex: 0 0 100px;
          padding: 0 0 0 10px;
          box-sizing: border-box;
        }
        &:nth-child(3){
          flex: 0 0 90px;
          text-align: center;
          a{
            font-size: 12px;
            color: #51D0B1;
            margin: 0 8px;
            cursor: pointer;
          }
        }
      }
    }
  }
}
</style>
