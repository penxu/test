<template>
  <div class="module-dynamic-wrip">
    <div class="header">
      <span>动态</span>
      <i class="el-icon-close" @click="closeDialog"></i>
    </div>
    <div class="dynamic-box">
      <ul v-for="dynamic in dynamicList" :key="dynamic.id">
        <li>{{dynamic.datetime_time | formatDate('yyyy-MM-dd HH:mm:ss')}}</li>
        <li><span>{{dynamic.employee_name}}</span>{{dynamic.content}}</li>
      </ul>
    </div>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'ModuleDynamic',
  components: {
  },
  props: ['bean', 'dataId'],
  data () {
    return {
      dynamicList: []
    }
  },
  created () {
    let id = {
      id: this.dataId,
      bean: this.bean
    }
    this.ajaxGetDynamicList(id)
  },
  methods: {
    // 关闭动态
    closeDialog () {
      this.$bus.emit('closeDynamicModal', true)
    },
    // 获取动态列表
    ajaxGetDynamicList (data) {
      HTTPServer.getDynmicList(data, 'Loading').then((res) => {
        this.dynamicList = res
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.slide-enter, .slide-leave-to
/* .slide-fade-leave-active for below version 2.1.8 */ {
  transform: translateX(350px);
}
.module-dynamic-wrip{
  position: absolute;
  width: 350px;
  bottom: 0;
  top: 95px;
  right: 0;
  background: #FFFFFF;
  box-shadow: 0 1px 8px 0 rgba(0,0,0,0.10);
  border-radius: 4px;
  z-index: 10;
  .header{
    height: 50px;
    line-height: 50px;
    padding: 0 20px;
    color: #060606;
    box-shadow: inset 0 -1px 0 0 rgba(217,217,217,0.50);
    .el-icon-close{
      float: right;
      font-size: 18px;
      margin: 16px 0;
      color: #333333;
      cursor: pointer;
    }
  }
  .dynamic-box{
    padding: 10px 20px;overflow: auto;
    height: calc(100% - 50px);
    ul{
      margin: 0 0 16px;
      li{
        margin: 0 0 8px;
        font-size: 14px;
        line-height: 20px;
        &:first-child{
          color: #69696C;
        }
        &:last-child{
          color: #4A4A4A;
          span{
            color: #26D0E0;
            margin: 0 5px 0 0;
          }
        }
      }
    }
  }
}
</style>
