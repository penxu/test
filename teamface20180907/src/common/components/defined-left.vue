<template>
  <div class="task-left-container">
        <div class="component-header clear">
          <span>字段组件</span>
            <el-popover
              placement="right-start"
              width="300"
              trigger="click"
              popper-class="component-popper">
              <div class="poper-container">
                <div class="flex-box popper-title">
                  <span>组件库</span><span><i class="iconfont icon-pc-member-close" @click="handleSwithpoper()"></i></span>
                </div>
                <div class="component-active drag-box">
                  <div>
                    <p class="type-title">基础字段</p>
                    <draggable v-model="list1" :options="dragOption" class="left-components-box">
                      <div v-for="(item, index) in list1" class="components" :key="item.type" :data-type="item.type" :class="{'selected': item.selected}" @click="handleSelectComponent('list1', index)">
                        <i class="iconfont" :class="item.icon"></i>
                        <span>{{item.label}}</span>
                      </div>
                    </draggable>
                  </div>
                  <div>
                    <p class="type-title">高级类组件</p>
                    <draggable v-model="list2" :options="dragOption" class="left-components-box">
                      <div v-for="(item, index) in list2" class="components" :key="item.type" :data-type="item.type" @click="handleSelectComponent('list2', index)" :class="{'selected': item.selected}">
                        <i class="iconfont" :class="item.icon"></i>
                        <span>{{item.label}}</span>
                      </div>
                    </draggable>
                  </div>
                </div>
              </div>
              <el-button slot="reference" type="text" class="pull-right" id="popperId"><i class="iconfont icon-pc-use-function"></i></el-button>
            </el-popover>
        </div>
        <div class="drag-box">
          <div v-show="!switchUnuseField" class="component-active">
            <!-- <draggable v-model="componentList" :options="dragOption" class="left-components-box">
              <div v-for="item in componentList" class="components" :key="item.type" :data-type="item.type" v-if="item.selected">
                <i class="iconfont" :class="item.icon"></i>
                <span>{{item.label}}</span>
              </div>
            </draggable> -->
            <div>
              <p class="type-title">基础字段</p>
              <draggable v-model="list1" :options="dragOption" class="left-components-box">
                <div v-for="item in list1" class="components" :key="item.type" :data-type="item.type" v-show="item.selected">
                  <i class="iconfont" :class="item.icon"></i>
                  <span>{{item.label}}</span>
                </div>
              </draggable>
            </div>
            <div>
              <p class="type-title" v-show="showHight">高级类组件</p>
              <draggable v-model="list2" :options="dragOption" class="left-components-box">
                <div v-for="item in list2" class="components" :key="item.type" :data-type="item.type" v-show="item.selected">
                  <i class="iconfont" :class="item.icon"></i>
                  <span>{{item.label}}</span>
                </div>
              </draggable>
            </div>
          </div>
          <div class="unused-field-box" :class="{'unfold-box': switchUnuseField}">
            <p class="unused-field">未使用字段 <i class="iconfont icon-shouqi" :class="{'unfold': switchUnuseField}"  @click="handleCollapse()"></i></p>
            <draggable v-model="unUsedLists" :options="unUsedDragOption" class="unused-components-box" :class="{'unused-components-unfold': switchUnuseField}" @end="unUsedragEnd">
              <div v-for="(item, index) in unUsedLists" class="del-components" :key="item.name" data-type="renew" >
                <div class="type-label" :title="item.label"><span>{{item.label}}</span></div>
                <div class="type-text">{{item.typeText}}</div>
                <i class="el-icon-close" @click="handleDelUnuse(index)" v-show="isDelete(item) "></i>
            </div>
          </draggable>
          </div>
        </div>
        <p>高级类组件</p>
        <draggable v-model="list2" :options="dragOption" class="left-components-box">
          <div v-for="item in list2" class="components" :key="item.type" :data-type="item.type">
            <i class="iconfont" :class="item.icon"></i>
            <span>{{item.label}}</span>
          </div>
        </draggable>
  </div>
</template>

<script>
// import {component1, component2} from '@/common/js/constant'
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
// const multitext = {
//   'label': '富文本',
//   'icon': 'icon-danhangwenben',
//   'type': 'multitext',
//   'isDrag': true,
//   'typeText': '富文本',
//   'field': {}
// }
// const allComponents = component1.concat(component2)
export default {
  name: 'DefinedLeft',
  components: {
    draggable
  },
  props: ['activeFields', 'unUseFields'],
  data () {
    return {
      activeName: 'components',
      unUsedLists: [], // 未使用字段
      componentVisable: false, // 添加组件弹窗
      allComponents: null, // 总组件列表
      componentList: this.activeFields,
      switchUnuseField: false,
      isActive: false,
      list1: [],
      list2: [],
      showHightComponent: false
    }
  },
  created () {
    // this.list1 = component1.concat(multitext)
    // this.list2 = component2
    console.log(this.list1, 'allcomponents')
  },
  methods: {
    // 处理已选择的组件
    handleSelComponent () {
      // let hash = {}
      this.componentList.map((item, index) => {
        if (item.type === 'identifier' || item.type === 'reference' || item.type === 'subform' || item.type === 'functionformula' || item.type === 'seniorformula') {
          this.list2.push(item)
          console.log(this.list2, 'list2')
        } else {
          this.list1.push(item)
        }
      })
    },
    // 选择添加组件
    handleSelectComponent (list, index) {
      console.log(index, 'index')
      if (list === 'list1') {
        this.list1[index].selected = !this.list1[index].selected
        // this.componentList.map((item, index) => {
        //   if (item.type === this.list1[index].type) {
        //     item.selected = !item.selected
        //   }
        // })
      } else {
        this.list2[index].selected = !this.list2[index].selected
      }
      console.log(this.componentList, 'componentList')
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
    // 展开或收起未使用字段
    handleCollapse () {
      this.switchUnuseField = !this.switchUnuseField
      this.isActive = true
    },
    // 切换弹框
    handleSwithpoper () {
      document.getElementById('popperId').click()
    },
    // 显示高级组件
    showHight (type) {
      // if (type === 'identifier' || type === 'reference' || type === 'subform' || type === 'functionformula' || type === 'seniorformula') {
      //   this.showHightComponent = true
      // } else {
      //   this.showHightComponent = false
      // }
      this.list2.some((item, index) => {
        return item.selected
      })
    },
    // 初始化未使用字段不能删除
    isDelete (data) {
      if (data.remove === '0' || data.source === 'project_defined') {
        return false
      } else {
        return true
      }
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
    if (this.componentList.length === 0) {
      console.log('初始化进的来吗')
      this.handleAllComponent()
    }
    this.unUsedLists = JSON.parse(JSON.stringify(this.unUseComponent))
    console.log(this.unUsedLists, 'unUsedLists')
    this.handleSelComponent()
  },
  watch: {
    // 监听未使用字段
    unUseComponent (val) {
      console.log(val, '删除字段改变啦、、、')
      this.unUsedLists = JSON.parse(JSON.stringify(val))
    },
    activeFields (val) {
      console.log(val, '左边初始化发生改变')
      this.componentList = val
    }
  }
}
</script>

<style lang="scss" >
  .component-popper {
    height: calc(100% - 63px);
    .poper-container {
      // padding: 10px;
      .popper-title {
        padding: 10px;
        justify-content: space-between;
        border-bottom: 1px solid #fafafa;
        span:nth-child(1) {
          font-size: 16px;
        }
        span:nth-child(2) {
          i {
            cursor: pointer;
            font-size: 12px;
          }
        }
      }
    }
  .left-components-box {
      // display: flex;
      // flex-wrap: wrap;
      margin: 10px 0;
      padding: 0 0 0 20px;
      // min-height: 400px;
      .components{
        margin: 0 10px 10px 0;
        padding: 0 12px;
        background: #FFFFFF;
        border: 1px solid #E7E7E7;
        border-radius: 2px;
        width: 115px;
        height: 28px;
        line-height: 28px;
        text-align: center;
        font-size: 16px;
        // cursor: move;
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
      .selected {
        color: #ccc;
        i.iconfont {
          color: #ccc;
        }
      }
    }
  }
.task-left-container{
  height: 100%;
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
    padding-bottom: 20px;
    height: 100%;
    .component-active {
      height: 60%;
      overflow: auto;
      width: 100%;
    }
    p.type-title{
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
      // min-height: 400px;
      .components{
        margin: 0 10px 10px 0;
        padding: 0 11px;
        background: #FFFFFF;
        border: 1px solid #E7E7E7;
        border-radius: 2px;
        width: 113px;
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
      height: 220px;
      overflow: auto;
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
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
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
    .unused-field-box {
      height: calc(100% - 450px);
      .unfold{
        transform: rotate(180deg);
        transition: all .3s linear;
      }
    }
    .unfold-box {
        // transition: all 0.5s linear;
        height: calc(100% - 32px);
      .unused-components-unfold {
        overflow: auto;
        height: calc(100% - 50px);
      }
    }
  }
  .task-left-dialog {
    // z-index: 101;
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
    }
  }
}
</style>
