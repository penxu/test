<template>
  <div class="task-left-container">
        <div class="component-header clear">
          <span>字段组件</span>
          <el-button type="text" @click="switchComponentVisbale(true)" class="pull-right"> <i class=" iconfont icon-pc-paper-additi"></i></el-button>
          <!-- <el-button type="primary" icon="el-icon-edit" circle></el-button> -->
        </div>
        <div class="drag-box">
          <draggable v-model="componentList" :options="dragOption" class="left-components-box">
            <div v-for="item in componentList" class="components" :key="item.type" :data-type="item.type" v-if="item.selected">
              <i class="iconfont" :class="item.icon"></i>
              <span>{{item.label}}</span>
            </div>
          </draggable>
          <div>
            <p class="unused-field">未使用字段 <i class="iconfont" :class="'icon-pc-background-module'"></i></p>
            <draggable v-model="unUsedLists" :options="unUsedDragOption" class="unused-components-box" @end="unUsedragEnd">
              <div v-for="(item, index) in unUsedLists" class="del-components" :key="item.name" data-type="renew" >
                <div class="type-label"><span>{{item.label}}</span></div>
                <div class="type-text">{{item.typeText}}</div>
                <i class="el-icon-close" @click="handleDelUnuse(index)"></i>
            </div>
          </draggable>
          </div>
        </div>
        <!-- <p>高级类组件</p>
        <draggable v-model="list2" :options="dragOption" class="left-components-box">
          <div v-for="item in list2" class="components" :key="item.type" :data-type="item.type">
            <i class="iconfont" :class="item.icon"></i>
            <span>{{item.label}}</span>
          </div>
        </draggable> -->
        <div class="task-left-dialog">
          <el-dialog
            title="组件库"
            :visible.sync="componentVisable"
            width="600">
            <div>
              <div class="add-component-box flex">
                <div v-for="(component, index) in allComponents" :key="component.name" class="add-component" :class="{'selected': component.selected}" @click="handleSelectComponent(index)">
                  <span>{{component.label}}</span>
                </div>
              </div>
            </div>
            <span slot="footer" class="dialog-footer">
              <el-button @click="handleSaveAddComponent(false)">取 消</el-button>
              <el-button type="primary" @click="handleSaveAddComponent(true)">确 定</el-button>
            </span>
          </el-dialog>
        </div>
  </div>
</template>

<script>
import {component1, component2} from '@/common/js/constant'
import draggable from 'vuedraggable'
import { mapMutations, mapState } from 'vuex'
// const unUsedLists = [{'field': {'fieldControl': '0', 'defaultEntrys': [], 'editView': '1', 'terminalPc': '1', 'terminalApp': '1', 'addView': '1', 'structure': '1', 'chooseType': '0'}, 'typeText': '下拉选项', 'name': 'picklist_1523348879487', 'width': '50%', 'active': '0', 'label': '下拉选项', 'state': '1', 'type': 'picklist', 'remove': '1', 'entrys': [{'color': '#FFFFFF', 'label': '选项1', 'value': '0'}, {'color': '#FFFFFF', 'label': '选项2', 'value': '1'}, {'color': '#FFFFFF', 'label': '选项3', 'value': '2'}]}, {'field': {'fieldControl': '0', 'editView': '1', 'phoneType': '0', 'terminalPc': '1', 'repeatCheck': '0', 'terminalApp': '1', 'pointOut': '', 'addView': '1', 'phoneLenth': '0', 'structure': '1'}, 'typeText': '电话', 'name': 'phone_1523348881237', 'width': '50%', 'active': '1', 'label': '电话', 'state': '1', 'type': 'phone', 'remove': '1'}]
// 初始化的字段组件
// const initComponents = [
//   {
//     label: '单行文本',
//     icon: 'icon-danhangwenben',
//     type: 'text',
//     isDrag: true,
//     typeText: '单行文本',
//     field: {},
//     selected: true
//   },
//   {
//     label: '多行文本',
//     icon: 'icon-duohangwenben',
//     type: 'textarea',
//     isDrag: true,
//     typeText: '多行文本',
//     field: {},
//     selected: true
//   },
//   {
//     label: '下拉选项',
//     icon: 'icon-xialaxuanxiang',
//     isDrag: true,
//     typeText: '下拉选项',
//     field: {},
//     selected: true,
//     type: 'picklist'
//   },
//   {
//     label: '数字',
//     icon: 'icon-shuzi',
//     type: 'number',
//     isDrag: true,
//     typeText: '数字',
//     field: {},
//     selected: true
//   },
//   {
//     label: '日期/时间',
//     icon: 'icon-riqishijian',
//     type: 'datetime',
//     isDrag: true,
//     typeText: '日期/时间',
//     field: {},
//     selected: true
//   },
//   {
//     label: '附件',
//     icon: 'icon-fujian',
//     type: 'attachment',
//     isDrag: true,
//     typeText: '附件',
//     field: {},
//     selected: true
//   }
// ]
const multitext = {
  label: '富文本',
  icon: 'icon-danhangwenben',
  type: 'multitext',
  isDrag: true,
  typeText: '富文本',
  field: {}
}
// const allComponents = component1.concat(component2)
export default {
  name: 'TaskDragLeft',
  components: {
    draggable
  },
  props: ['activeFields', 'unUseFields'],
  data () {
    return {
      activeName: 'components',
      unUsedLists: [],
      componentVisable: false, // 添加组件弹窗
      allComponents: null, // 总组件列表
      componentList: this.activeFields
    }
  },
  created () {
    this.allComponents = component1.concat(component2).concat(multitext)
    console.log(this.allComponents, 'allcomponents')
  },
  methods: {
    // 点击添加组件
    switchComponentVisbale (type) {
      if (type) {
        this.componentVisable = true
        this.handleSelComponent()
      } else {
        this.componentVisable = false
      }
    },
    // 处理已选择的组件
    handleSelComponent () {
      let hash = {}
      this.componentList.map((item, index) => {
        hash[item.type] = true
      })
      this.allComponents.map((item, index) => {
        if (hash[item.type]) {
          item.selected = true
        } else {
          item.selected = false
        }
      })
    },
    // 选择添加组件
    handleSelectComponent (index) {
      this.allComponents[index].selected = !this.allComponents[index].selected
    },
    // 保存已选择的组件
    handleSaveAddComponent (type) {
      if (type) {
        this.componentList = []
        this.allComponents.map((item, index) => {
          if (item.selected) {
            this.componentList.push(JSON.parse(JSON.stringify(item)))
          }
        })
        this.$bus.emit('useLayout', this.componentList)
        console.log(this.componentList, 'com')
      } else {
        console.log('取消、、、、、、、')
        this.handleSelComponent()
      }
      this.componentVisable = false
    },
    // 处理所有组件
    handleAllComponent () {
      // this.activeFields = []
      this.allComponents.map((item, index) => {
        if (item.type === 'text' || item.type === 'textarea' || item.type === 'picklist' || item.type === 'datetime' || item.type === 'attachment') {
          this.$set(item, 'selected', true)
          this.componentList.push(JSON.parse(JSON.stringify(item)))
        } else {
          this.$set(item, 'selected', false)
        }
      })
    },
    // 点击删除
    handleDelUnuse (index) {
      this.$confirm('此操作将永久删除该字段数据, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.unUsedLists.splice(index, 1)
        let obj = {
          type: 'renew',
          field: this.unUsedLists
        }
        this.setUnUseFields(obj)
      })
        .catch(() => {})
    },
    // 恢复结束方法
    unUsedragEnd () {
      let obj = {
        type: 'renew',
        field: this.unUsedLists
      }
      this.setUnUseFields(obj)
    },
    ...mapMutations({
      setSingeLayout: 'SET_SINGLE_LAYOUT',
      setAllLayout: 'SET_ALL_LAYOUT',
      setUnUseFields: 'SET_UNUSE_COMPONENT'
    })
  },
  computed: {
    dragOption () {
      return {
        animation: 100,
        group: { name: 'compontents', pull: 'clone', put: false },
        sort: false,
        ghostClass: 'ghost'
      }
    },
    unUsedDragOption () {
      return {
        animation: 200,
        group: { name: 'compontents', pull: true, put: false },
        sort: false,
        ghostClass: 'ghost'
      }
    },
    ...mapState({
      unUseComponent: state => state.taskCustom.unUseComponent
    })
  },
  beforeDestory () {
    this.$bus.off('sendAllLayout') // 销毁
    this.$bus.off('sendTaskUnusedField') // 销毁
  },
  mounted () {
    console.log(this.activeFields, 'allComponent')
    this.$bus.off('sendTaskUnusedField')
    this.$bus.on('sendTaskUnusedField', (value) => {
      console.log(value, '删除的字段')
      this.unUsedLists.push(value)
    })
    if (this.componentList.length === 0) {
      this.handleAllComponent()
    }
  },
  watch: {
    unUseComponent (val) {
      console.log(val, '删除字段改变啦、、、')
      this.unUsedLists = JSON.parse(JSON.stringify(val))
      // this.$bus.emit('sendTaskUnusedField', 'renew', val)
    },
    activeFields (val) {
      console.log(val, '左边初始化发生改变')
      this.componentList = val
    }
  }
}
</script>

<style lang="scss" >
.task-left-container{
  .flex {
    display: -webkit-flex; /* Safari */
    display: flex;
  }
  flex: 0 0 300px;
  background: #E9EFF3;
  border-radius: 0 0 3px 3px;
  text-align: left;
  .component-header {
    background: #fff;
    height: 50px;
    line-height: 50px;
    padding-left: 20px;
    padding-right: 20px;
    span {
      font-size: 16px;
      color: #797979;
    }
    i {
      line-height: 25px;
    }
  }
  .drag-box{
    padding: 20px 0;
    >p{
      font-size: 14px;
      color: #59656B;
      padding: 0 20px;
    }
    .unused-field{
      height: 50px;
      line-height: 50px;
      font-size: 16px;
      color: #4A4A4A;
      padding: 0 16px 0 20px;
      // border-bottom: 1px solid #4891CD;
      box-shadow: 0 -2px 5px rgba(0, 0, 0, 0.18);
      background: #fff;
      .iconfont{
        float: right;
        cursor: pointer;
      }
    }
    .left-components-box{
      // display: flex;
      // flex-wrap: wrap;
      margin: 10px 0;
      padding: 0 15px 0 25px;
      min-height: 400px;
      .components{
        margin: 0 10px 10px 0;
        padding: 0 14px;
        background: #FFFFFF;
        border: 1px solid #E7E7E7;
        border-radius: 2px;
        width: 118px;
        height: 28px;
        line-height: 28px;
        text-align: center;
        font-size: 16px;
        cursor: move;
        display: inline-block;
        box-sizing: border-box;
        &:hover{
          box-shadow: 2px 2px 5px rgba(0,0,0,0.18);
          border:1px solid #FFFFFF;
        }
        >span{
          font-size: 12px;
          vertical-align: top;
        }
        >.iconfont{
          color: #69696C;
          font-size: 20px;
          margin: 0 10px 0 0;
        }
      }
    }
    .unused-components-box{
      padding: 12px 8px;
      min-height: 150px;
      .del-components{
        display: flex;
        height: 40px;
        line-height: 40px;
        margin: 0 0 10px;
        padding: 0 8px;
        background: #F4F6F9;
        border-radius: 4px;
        cursor: move;
        &:hover{
          background: #FFFFFF;
          box-shadow: 0 2px 4px 0 rgba(96,96,96,0.24);
          border-radius: 4px;
          >i{
            visibility: visible;
          }
        }
        .type-label{
          flex: 0 0 110px;
          color: #797979;
        }
        .type-text{
          flex: 1;
          height: 30px;
          line-height: 30px;
          margin: 5px 0;
          padding: 0 8px;
          color: #BBBBC3;
          border: 1px solid #BBBBC3;
          border-radius: 3px;
        }
        >i{
          visibility: hidden;
          font-size: 16px;
          color: #BBBBC3;
          margin: 12px 0 0 7px;
          cursor: pointer;
        }
      }
    }
  }
  .task-left-dialog {
    .add-component-box {
        flex-wrap: wrap;
      .add-component {
        border: 1px solid #ccc;
        width: 70px;
        height: 60px;
        line-height: 60px;
        text-align: center;
        margin: 10px;
        cursor: pointer;
        // padding-left: 15px;
        // padding-right: 15px;
      }
      .selected {
        border: 2px solid red
      }
    }
  }
}
</style>
// <style lang="scss">
// .custom-drag-left-wrip{
//   .component-tab{
//     .el-tabs__header {
//       margin:0;
//     }
//     .el-tabs__item{
//       width: 150px;
//       height: 50px;
//       line-height: 50px;
//       text-align: center;
//       font-size: 16px;
//     }
//   }
// }
//   i.el-icon-plus {
//     font-size: 20px;
//     line-height: 37px;
//   }
// </style>
