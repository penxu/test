<template>
  <div class="custom-drag-left-wrip">
    <el-tabs v-model="activeName" class="component-tab">
      <el-tab-pane label="字段组件" name="components">
        <div class="components-box-wrip">
          <p>基础字段</p>
          <draggable v-model="list1" :options="dragOption" @start="start" @end="end" class="left-components-box">
            <div v-for="item in list1" class="components" :key="item.type" :data-type="item.type">
              <i class="iconfont" :class="item.icon"></i>
              <span>{{item.label}}</span>
            </div>
          </draggable>
          <p>高级类组件</p>
          <draggable v-model="list2" :options="dragOption" class="left-components-box">
            <div v-for="item in list2" class="components" :key="item.type" :data-type="item.type">
              <i class="iconfont" :class="item.icon"></i>
              <span>{{item.label}}</span>
            </div>
          </draggable>
        </div>
        <div class="unused-box-wrip" :style="{height: unusedShow ? '500px' : ''}">
          <p class="unused-field">未使用字段 <i class="iconfont" :class="{'icon-shouqi': !unusedShow,'icon-zhankai': unusedShow}" @click="unusedShow = !unusedShow"></i></p>
          <draggable v-model="unUsedLists" :options="unUsedDragOption" class="unused-components-box" :style="{overflow: unusedShow ? '' : 'hidden'}" @end="dragEnd">
            <div v-for="(item, index) in unUsedLists" class="del-components" :key="item.name">
              <div class="type-label" :style="{'line-height':item.label.length>7?'18px':'40px'}"><span>{{item.label}}</span></div>
              <div class="type-text">{{item.typeText}}</div>
              <i class="el-icon-close" @click="handleDelUnuse(index)"></i>
            </div>
          </draggable>
        </div>
      </el-tab-pane>
      <el-tab-pane label="常用字段" name="common">
        <draggable v-model="commonList" :options="dragOption" @start="start" @end="end" class="left-components-box">
          <div v-for="(item, index) in commonList" class="components" :key="index" :data-type="item.type" :data-field="index">
            <i class="iconfont" :class="item.icon"></i>
            <span>{{item.label}}</span>
          </div>
        </draggable>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { mapState, mapGetters, mapMutations } from 'vuex'
import {component1, component2, commonField} from '@/common/js/constant'
import draggable from 'vuedraggable'
export default {
  name: 'CustomDragLeft',
  components: {
    draggable
  },
  data () {
    return {
      activeName: 'components',
      list1: component1,
      list2: component2,
      unUsedLists: [],
      unusedShow: false,
      commonList: commonField
    }
  },
  created () {
    this.unUsedLists = JSON.parse(JSON.stringify(this.enable_layout)).rows
  },
  methods: {
    start (evt) {
      console.log('拖动开始', evt.item)
    },
    end (evt) {
      console.log('拖动结束', evt.item)
    },
    dragEnd () {
      let unUsedLists = JSON.parse(JSON.stringify(this.unUsedLists))
      this.setEnableLayout({rows: unUsedLists})
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
    ...mapMutations({
      setEnableLayout: 'ENABLE_LAYOUT'
    })
  },
  computed: {
    ...mapGetters([
      'enable_layout'
    ]),
    ...mapState({
      enable_layout: state => state.custom.enable_layout
    }),
    dragOption () {
      return {
        animation: 100,
        group: { name: 'compontents', pull: 'clone', put: false },
        sort: false,
        scroll: false,
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
    enable_layout: {
      handler: function (val, oldval) {
        console.log('未使用字段变了')
        this.unUsedLists = JSON.parse(JSON.stringify(this.enable_layout)).rows
      },
      deep: true// 对象内部的属性监听，也叫深度监听
    }
    // unUsedLists (val, oldVal) {
    //   let enableList = JSON.parse(JSON.stringify(val))
    //   this.setEnableLayout({rows: enableList})
    // }
  }
}
</script>

<style lang="scss" scoped>
.custom-drag-left-wrip{
  flex: 0 0 300px;
  background: #E9EFF3;
  border-radius: 0 0 3px 3px;
  text-align: left;
  .el-tab-pane{
    position: relative;
    height: 100%;
    .components-box-wrip{
      overflow: auto;
      height: 100%;
      padding: 20px 0 112px;
      >p{
        font-size: 14px;
        color: #59656B;
        padding: 0 20px;
      }
    }
    .unused-box-wrip{
      position: absolute;
      width: 100%;
      height: 130px;
      bottom: 0;
      left: 0;
      background: #E9EFF3;
      transition:height 0.75s;
      .unused-field{
        height: 50px;
        line-height: 50px;
        font-size: 16px;
        color: #4A4A4A;
        padding: 0 16px 0 27px;
        border-bottom: 1px solid #4891CD;
        box-shadow: 0 -2px 5px rgba(0, 0, 0, 0.18);
        .iconfont{
          float: right;
          cursor: pointer;
        }
      }
    }
    .left-components-box{
      margin: 10px 0;
      padding: 0 15px 0 25px;
      .components{
        display: block;
        float: left;
        width: 118px;
        height: 28px;
        line-height: 28px;
        margin: 0 10px 10px 0;
        padding: 0 5px 0 14px;
        background: #FFFFFF;
        border: 1px solid #E7E7E7;
        border-radius: 2px;
        cursor: move;
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
        >.iconfont.icon-fuwenben{
          font-size: 16px;
          margin-left: 2px;
        }
      }
    }
    .unused-components-box{
      padding: 12px 8px;
      overflow: auto;
      height: calc(100% - 50px);
      .del-components{
        display: flex;
        min-height: 40px;
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
          line-height: 18px;
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
}
</style>
<style lang="scss">
.custom-drag-left-wrip{
  .component-tab{
    height: 100%;
    .el-tabs__header {
      margin:0;
    }
    .el-tabs__content {
      // overflow: auto;
      height: calc(100% - 50px);
    }
    .el-tabs__item{
      width: 150px;
      height: 50px;
      line-height: 50px;
      text-align: center;
      font-size: 16px;
    }
  }
}
</style>
