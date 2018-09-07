<template>
  <div class="webform-left-container">
        <!-- <div class="component-header clear">
          <span>基础字段</span>
        </div> -->
        <div class="drag-box">
          <div v-show="!switchUnuseField" class="component-active">
            <div>
              <p class="type-title">基础字段</p>
              <draggable v-model="list1" :options="dragOption" class="left-components-box">
                <div v-for="item in list1" class="components" :key="item.type" :data-type="item.type">
                  <i class="iconfont" :class="item.icon"></i>
                  <span>{{item.label}}</span>
                </div>
              </draggable>
            </div>
          </div>
          <div class="unused-field-box" :class="{'unfold-box': switchUnuseField}">
            <p class="unused-field">未使用字段 <span style="font-size: 12px">如需修改请在标准页面设置</span> <i class="iconfont icon-shouqi" :class="{'unfold': switchUnuseField}"  @click="handleCollapse()"></i></p>
            <draggable v-model="unUsedLists" :options="unUsedDragOption" class="unused-components-box" :class="{'unused-components-unfold': switchUnuseField}" @end="unUsedragEnd">
              <div v-for="(item, index) in unUsedLists" class="del-components" :key="index" data-type="renew">
                <div class="type-label" :title="item.label"><span>{{item.label}}</span></div>
                <div class="type-text">{{item.typeText}}</div>
                <!-- <i class="el-icon-close" @click="handleDelUnuse(index)"></i> -->
            </div>
          </draggable>
          </div>
        </div>
  </div>
</template>

<script>
import { mapState, mapGetters, mapMutations } from 'vuex'
import { webformComponents } from '@/common/js/constant'
import draggable from 'vuedraggable'
// const test = [{'field': {'fieldControl': '0', 'defaultEntrys': [], 'editView': '1', 'terminalPc': '1', 'terminalApp': '1', 'addView': '1', 'structure': '1', 'chooseType': '0'}, 'typeText': '下拉选项', 'name': 'picklist_1523348879487', 'width': '50%', 'active': '0', 'label': '下拉选项', 'state': '1', 'type': 'picklist', 'remove': '1', 'entrys': [{'color': '#FFFFFF', 'label': '选项1', 'value': '0'}, {'color': '#FFFFFF', 'label': '选项2', 'value': '1'}, {'color': '#FFFFFF', 'label': '选项3', 'value': '2'}]}, {'field': {'fieldControl': '0', 'editView': '1', 'phoneType': '0', 'terminalPc': '1', 'repeatCheck': '0', 'terminalApp': '1', 'pointOut': '', 'addView': '1', 'phoneLenth': '0', 'structure': '1'}, 'typeText': '电话', 'name': 'phone_1523348881237', 'width': '50%', 'active': '1', 'label': '电话', 'state': '1', 'type': 'phone', 'remove': '1'}]
export default {
  name: 'CustomWebfromLeft',
  components: {
    draggable
  },
  data () {
    return {
      activeName: 'components',
      list1: webformComponents,
      // list2: component2,
      unUsedLists: [],
      switchUnuseField: false
    }
  },
  created () {
    this.unUsedLists = JSON.parse(JSON.stringify(this.getDisableLayout()))
  },
  methods: {
    // dragEnd () {
    //   let unUsedLists = JSON.parse(JSON.stringify(this.unUsedLists))
    //   this.setEnableLayout({rows: unUsedLists})
    // },
    // 恢复结束方法
    unUsedragEnd () {
      let obj = {
        type: 'renew',
        field: this.unUsedLists
      }
      this.setUnUseFields(obj)
    },
    // 点击删除未使用组件
    handleDelUnuse (index) {
      this.$confirm('此操作将永久删除该组件及组件的属性设置, 是否继续?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.unUsedLists.splice(index, 1)
        let unUsedLists = JSON.parse(JSON.stringify(this.unUsedLists))
        this.setEnableLayout({rows: unUsedLists})
      }).catch(() => {
      })
    },
    // 展开或收起未使用字段
    handleCollapse () {
      this.switchUnuseField = !this.switchUnuseField
      this.isActive = true
    },
    ...mapMutations({
      setEnableLayout: 'ENABLE_LAYOUT',
      setUnUseFields: 'SET_DISAB_FIELDS'
    })
  },
  computed: {
    ...mapGetters([
      'getDisableLayout'
    ]),
    ...mapState({
      webform_layout: state => state.webformCustom.webform_layout
    }),
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
        animation: 100,
        group: { name: 'compontents', pull: true, put: false },
        sort: false,
        ghostClass: 'ghost'
      }
    }
  },
  watch: {
    'webform_layout.disableLayout': {
      handler: function (val, oldval) {
        console.log(this.webform_layout.disableLayout, '未使用字段变了')
        this.unUsedLists = JSON.parse(JSON.stringify(this.getDisableLayout()))
      },
      deep: true// 对象内部的属性监听，也叫深度监听
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
.webform-left-container{
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
      height: 40%;
      overflow: auto;
      width: 100%;
    }
    p.type-title{
      font-size: 14px;
      color: #59656B;
      padding: 20px 20px 0 20px;
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
      min-height: 150px;
      height: calc(100% - 50px);
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
      height: calc(100% - 264px);
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
