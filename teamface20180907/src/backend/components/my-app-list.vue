<template>
<div class="my-app-list-wrap">
  <draggable class="app-list-drag clear"  v-model="applicationList" :options="dragOption" @end="drop">
    <div class="item" v-for="(app,index) in applicationList" :key="index">
      <div class="box" :style="{background: app.icon_color}" @click="goModules(app)">
        <icon-img :type="app.icon_type" :url="app.icon_url" :size="iconStyle(app.icon_color)" :isApp="true"></icon-img>
      </div>
      <span>{{app.name}}</span>
    </div>
  </draggable>
</div>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
import IconImg from '@/common/components/icon-img'
import draggable from 'vuedraggable'
export default {
  name: 'MyAppList',
  components: {
    draggable,
    IconImg
  },
  data () {
    return {
      showMask: false,
      dialogVisible: false,
      applicationList: [],
      list: []
    }
  },
  created () {
    this.ajaxGetAppList()
    // 刷新列表
    this.$bus.on('refreshAppList', () => {
      this.ajaxGetAppList()
    })
  },
  methods: {
    // 图标样式
    iconStyle (color) {
      return {
        border: '80px',
        content: '42px',
        background: color,
        radius: '4px'
      }
    },
    // 应用拖拽
    drop (evt, list) {
      let data = []
      this.applicationList.map((item, index) => {
        data.push(item.id)
      })
      this.ajaxSortApp(data)
    },
    // 去模块界面
    goModules (app) {
      // 将图标信息存入sessionStrage
      sessionStorage.setItem('appIcon', JSON.stringify(app))
      let query = {id: app.id}
      this.$router.push({name: 'CustomModule', query: query})
    },
    // 获取应用列表
    ajaxGetAppList (data) {
      HTTPServer.getApplicationList(data, 'Loading').then((res) => {
        this.applicationList = res
        // 存储我的应用数量
        sessionStorage.setItem('appNumber', res.length)
      })
    },
    // 应用排序
    ajaxSortApp (data) {
      HTTPServer.sortApp(data).then((res) => {
      })
    }
  },
  computed: {
    dragOption () {
      return {
        animation: 100,
        group: { name: 'app', pull: true, put: false },
        sort: true,
        ghostClass: 'ghost',
        filter: '.no-drag'
      }
    }
  }
}
</script>

<style lang="scss">
.my-app-list-wrap{
  width: 100%;
  padding: 5px 10px;
  .item{
    position: relative;
    float: left;
    width: 80px;
    height: 110px;
    margin: 0 20px 20px 0;
    background: #FFFFFF;
    cursor: pointer;
    .box{
      margin: 0 0 10px 0;
      text-align: center;
      border-radius: 4px;
    }
    >span{
      display: block;
      font-size: 12px;
      color: #333333;
      text-align: center;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}
</style>
