<template>
  <div class="work-enterprise" :style="'min-width:' + (headerData.length * 300 + (headerData.length - 1) * 20) + 'px'">
    <div class="work-item" v-for="(headItem, index) in headerData" :key="index">
      <header v-if="headItem.columnList"><span class="header-name">{{headItem.text}}</span><span  class="header-length">（{{headItem.columnList.length}}）</span></header>
      <draggable v-model="headItem.columnList" :options="headItem.dragOptions" :move="current" @add="dragAdd($event,headItem)" @update="dragUpdate($event,headItem)" class="work-content">
        <div class="work-panel" v-for="(column, index2) in headItem.columnList" :key="index2" @click="moduleDetail($event, column)">
          <!-- 卡片组件 -->
          <task-work-card :column="column" :index="index2" :columnList="headItem.columnList"></task-work-card>
        </div>
      </draggable>
    </div>
  </div>
</template>

<script>
import draggable from 'vuedraggable'
import taskWorkCard from '@/frontend/project/components/task-work-card' // 任务卡片
import { Loading } from 'element-ui'
// loading样式的配置
const loadingOptions = {
  lock: true,
  text: 'Loading',
  spinner: 'el-icon-loading',
  background: 'rgba(0, 0, 0, 0.4)'
}
// 拖拽配置项
const dragOptions = {
  animation: 100,
  sort: false,
  group: { name: 'compontents', pull: true, put: true },
  ghostClass: 'ghost',
  filter: '.no-drag'
}
export default {
  name: 'workbench',
  components: {draggable, taskWorkCard},
  props: ['nodeKeyList'],
  data () {
    return {
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      // 块列表
      headerData: [],
      currentTaskInfoId: '', // 当前移动数据的id
      mask: ''
    }
  },
  created () {
    this.headerData = JSON.parse(JSON.stringify(this.nodeKeyList))
    this.getColumnList()
  },
  mounted () {
    this.$bus.on('updateColumnList', res => {
      this.headerData = res
      // 根据传过来的nodeKeyList,获取各模块数据
      this.getColumnList()
    })
  },
  methods: {
    current (data) {
      this.currentTaskInfoId = data.draggedContext.element.taskInfoId
      console.log(this.currentTaskInfoId, 'this.currentKey')
    },
    // 根据传过来的nodeKeyList,获取各模块columnList详情数据
    getColumnList () {
      // 开启全局遮罩层
      this.mask = Loading.service(loadingOptions)
      // 遍历headerData,给每个item添加属性columnList
      this.headerData.map((item, index) => {
        // 给每个块列表加拖拽配置
        this.headerData[index].dragOptions = dragOptions
        // 此处columnList是根据key动态获取的
        this.$http.queryFlowWorkbenchWebList({'flowId': item.key}, false).then(res => {
          this.headerData[index].columnList = res.dataList
          this.$set(this.headerData, index, this.headerData[index])
          if (index === this.headerData.length - 1) {
            // 关闭全局遮罩层
            this.mask.close()
          }
        })
      })
    },
    dragUpdate (event, data) {
      console.log('dragUpdate')
    },
    dragAdd (event, data) {
      // 调用拖动接口
      let obj = {
        'taskId': this.currentTaskInfoId, // 当前移动任务ID
        'flowId': data.key // 移动到对应分列的企业流程子id
      }
      this.$http.moveFlowWorkbench(obj).then(res => {
        console.log(res)
      })
      this.initHeaderData()
    },
    // 请求模块详情
    moduleDetail (event, data) {
    },
    // checkbox
    workChange (event, data, index, index2) {
      this.initHeaderData()
    },
    initHeaderData () {
      /** 更新表单数据 */
      this.headerData.map((item, index) => {
        var columnList = this.headerData[index].columnList
        columnList.map((item2, index2) => {
          // columnList[index2].checkbox = !!columnList[index2].checkbox
          this.$set(this.headerData[index].columnList, index2, columnList[index2])
        })
        this.$set(this.headerData, index, this.headerData[index])
      })
    }
  },
  watch: {
    nodeKeyList () {
      this.headerData = JSON.parse(JSON.stringify(this.nodeKeyList))
      // 根据传过来的nodeKeyList,获取各模块数据
      this.getColumnList()
    }
  },
  updated () {
    // console.log(this.headerData, 'headerData-updated')
  }
}
</script>
