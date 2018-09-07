<template>
  <!-- <div class="work-time" style="min-width: 1830px;"> -->
  <div class="work-time" style="min-width: 1300px;">
    <!-- 超期未完成 -->
    <div class="work-item">
      <header>超期未完成<span>（{{columnList0.length}}）</span></header>
      <draggable :options="noDropOption" @add="fieldAdd($event, 1)" @update="dragUpdate($event, 1)" v-model="columnList0" class="work-content no-drag" style="max-height: calc(100% - 50px);">
        <div class="work-panel" v-for="(column, index) in columnList0" :key="index" @click="moduleDetail($event, column)">
          <!-- 卡片组件 -->
          <task-work-card :column="column" :index="index" :columnList="columnList0" :status="'work-item'"></task-work-card>
        </div>
      </draggable>
    </div>
    <!-- 今日要做 -->
    <div class="work-item">
      <header>今日要做<span>（{{columnList1.length}}）</span></header>
      <draggable :options="columndropOptionField" @add="fieldAdd($event, 2)" :move="current" @update="dragUpdate($event, 2)" v-model="columnList1" class="work-content" :style="{'max-height':lookPeoplesList.length>0?'calc(100% - 50px)':'calc(100% - 100px)'}">
        <div class="work-panel" v-for="(column, index) in columnList1" :key="index" @click="moduleDetail($event, column)">
          <!-- 卡片组件 -->
          <task-work-card :column="column" :index="index" :columnList="columnList1" :status="'work-item'"></task-work-card>
        </div>
      </draggable>
      <footer @click="addProTask('today')" v-if="lookPeoplesList.length==0">
        <i class="iconfont icon-pc-paper-additi"></i>新建
      </footer>
    </div>
    <!-- 明日要做 -->
    <div class="work-item">
      <header>明日要做<span>（{{columnList2.length}}）</span></header>
      <draggable :options="columndropOptionField" @add="fieldAdd($event, 3)" :move="current" @update="dragUpdate($event, 3)" v-model="columnList2" class="work-content" :style="{'max-height':lookPeoplesList.length>0?'calc(100% - 50px)':'calc(100% - 100px)'}">
        <div class="work-panel" v-for="(column, index) in columnList2" :key="index" @click="moduleDetail($event, column)">
          <!-- 卡片组件 -->
          <task-work-card :column="column" :index="index" :columnList="columnList2" :status="'work-item'"></task-work-card>
        </div>
      </draggable>
      <footer @click="addProTask('tomorrow')" v-if="lookPeoplesList.length==0">
        <i class="iconfont icon-pc-paper-additi"></i>新建
      </footer>
    </div>
    <!-- 以后要做 -->
    <div class="work-item">
      <header>以后要做<span>（{{columnList3.length}}）</span></header>
      <draggable :options="columndropOptionField" @add="fieldAdd($event, 4)" :move="current" @update="dragUpdate($event, 4)" v-model="columnList3" class="work-content" :style="{'max-height':lookPeoplesList.length>0?'calc(100% - 50px)':'calc(100% - 100px)'}">
        <div class="work-panel" v-for="(column, index) in columnList3" :key="index" @click="moduleDetail($event, column)">
          <!-- 卡片组件 -->
          <task-work-card :column="column" :index="index" :columnList="columnList3" :status="'work-item'"></task-work-card>
        </div>
      </draggable>
      <footer @click="addProTask('afterTomorrow')" v-if="lookPeoplesList.length==0">
        <i class="iconfont icon-pc-paper-additi"></i>新建
      </footer>
    </div>
    <!-- 后期再做，数据显示 -->
    <!-- <div class="work-item work-item-show-picture">
      <img data-v-3809f50c="" src="/static/img/no-data.png" alt="">
      <p><span>暂无数据</span></p>
    </div> -->
  </div>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
import draggable from 'vuedraggable'
import taskWorkCard from '@/frontend/project/components/task-work-card' // 任务卡片
export default {
  name: 'workbench',
  components: {draggable, taskWorkCard},
  props: ['taskList0', 'taskList1', 'taskList2', 'taskList3', 'lookPeoplesList'],
  data () {
    return {
      // 超期未完成列表
      columnList0: [],
      // 今日要做列表
      columnList1: [],
      // 明日要做列表
      columnList2: [],
      // 以后要做列表
      columnList3: [],
      currentTimeId: '' // 当前移动的id
    }
  },
  created () {
  },
  methods: {
    // 获取当前移动的数据
    current (data) {
      this.currentTimeId = data.draggedContext.element
    },
    // 模块内部排序更新
    dragUpdate (event, type) {
      console.log('dragUpdate')
      let dataList = []
      if (type === 2) {
        dataList = JSON.parse(JSON.stringify(this.columnList1))
      } else if (type === 3) {
        dataList = JSON.parse(JSON.stringify(this.columnList2))
      } else if (type === 4) {
        dataList = JSON.parse(JSON.stringify(this.columnList3))
      }
      // 调用数据排序接口
      let obj = {
        'dataList': dataList
      }
      HTTPServer.sortTimeWorkbench(obj).then(res => {
        console.log(res, '调用数据排序接口')
      })
    },
    // 模块拖动
    fieldAdd (event, type) {
      /** 更新表单数据 */
      console.log('fieldAdd-type', type)
      let dataList = []
      if (type === 2) {
        dataList = JSON.parse(JSON.stringify(this.columnList1))
      } else if (type === 3) {
        dataList = JSON.parse(JSON.stringify(this.columnList2))
      } else if (type === 4) {
        dataList = JSON.parse(JSON.stringify(this.columnList3))
      }
      // 调用数据拖拽接口
      let obj = {
        'timeId': this.currentTimeId.timeId, // 当前移动任务ID todo
        'workbenchTag': type, // 移动到对应工作台标签，1超期未完成，2今日要做，3明日要做，4以后要做
        'dataList': dataList
      }
      if (this.currentTimeId.dataType === 6) {
        obj.bean_name = this.currentTimeId.beanName
        obj.bean_id = this.currentTimeId.beanId
        obj.bean_type = this.currentTimeId.dataType
      }
      HTTPServer.moveTimeWorkbench(obj).then(res => {
        console.log(res, '调用数据拖拽接口')
      })
    },
    addProTask (v) { // 新增任务
      this.$bus.$emit('showTaskCreateWork', v)
      sessionStorage.setItem('listTaskAddTask', 'add')
    },
    // 点击数据卡片,打开相应详情
    moduleDetail (event, data) {
    }
  },
  computed: {
    /** 列表字段 */
    columndropOptionField () {
      return {
        animation: 200,
        group: { name: 'field', pull: true, put: true },
        sort: true,
        ghostClass: 'ghost'
      }
    },
    // 不可拖动配置(超期未完成)
    noDropOption () {
      return {
        animation: 200,
        group: { name: 'field', pull: false, put: false },
        sort: true,
        ghostClass: 'ghost',
        filter: '.no-drag'
      }
    }
  },
  watch: {
    taskList0 () {
      // 超期未完成列表
      this.columnList0 = JSON.stringify(this.taskList0) === '{}' ? [] : JSON.parse(JSON.stringify(this.taskList0))
    },
    taskList1 () {
      // 今日要做列表
      this.columnList1 = JSON.stringify(this.taskList1) === '{}' ? [] : JSON.parse(JSON.stringify(this.taskList1))
    },
    taskList2 () {
      // 明日要做列表
      this.columnList2 = JSON.stringify(this.taskList2) === '{}' ? [] : JSON.parse(JSON.stringify(this.taskList2))
    },
    taskList3 () {
      // 以后要做列表
      this.columnList3 = JSON.stringify(this.taskList3) === '{}' ? [] : JSON.parse(JSON.stringify(this.taskList3))
    }
  }
}
</script>
