<template>
  <div class="task-custom-container" >
    <div class="btn-box bordera flex">
      <div>
        <el-button @click="handleSaveAllLayout(false)"> <i class="iconfont icon-preview"></i> 预览</el-button>
        <el-button @click="handleSaveAllLayout(true)" type="primary">保存</el-button>
      </div>
    </div>
    <div class="task-custom-main flex">
      <div class="task-custom-left ">
        <taskdragleft :activeFields="allLayout.componentLayout.rows" ref="taskDragLeft" v-if="childVisable"></taskdragleft>
      </div>
      <div class="task-custom-middle ">
        <taskdragcenter :enableFields="allLayout.enableLayout.rows" ref="taskDragMiddle" v-if="childVisable"></taskdragcenter>
      </div>
      <div class="task-custom-right ">
        <taskdragright></taskdragright>
      </div>
    </div>
  <transition name="fade">
      <div class="difined-preview-mask" v-show="previewVisable" @click="previewVisable = false">
      </div>
  </transition>
  <transition name="slide">
    <div class="task-defined-preview-box" v-if="previewVisable">
      <definedPreview></definedPreview>
    </div>
  </transition>
  <definedFormula :taskOrProjectBean="'project_custom'"></definedFormula>
  <definedIdentifer></definedIdentifer>
  <highCondition></highCondition>
  </div>
</template>

<script>
import DefinedFormula from '@/backend/custom/components/defined-formula' // 高级公式函数公式弹框
import DefinedIdentifer from '@/backend/custom/components/defined-identifer' // 自动编号弹框
import HighCondition from '@/common/alert/high-condition' // 高级条件
import DefinedLeft from '@/common/components/defined-left'
import DefinedRight from '@/common/components/defined-right'
import DefinedMiddle from '@/common/components/defined-middle'
import TaskCustomPreview from '@/backend/module_setting/cooperation/components/task-custom-preview'
// import {taskDefaultFields} from '@/common/js/constant'
import { HTTPServer } from '@/common/js/ajax.js'
import { mapMutations, mapState } from 'vuex'
import DefinedPreview from '@/common/components/defined-preview'
import LayoutForm from '@/common/layout/layout-form'
export default {
  name: 'TaskCustom',
  components: {
    taskdragleft: DefinedLeft,
    taskdragcenter: DefinedMiddle,
    taskdragright: DefinedRight,
    taskCustomPreview: TaskCustomPreview,
    definedFormula: DefinedFormula,
    definedIdentifer: DefinedIdentifer,
    highCondition: HighCondition,
    definedPreview: DefinedPreview,
    layoutForm: LayoutForm
  },
  data () {
    return {
      allLayout: {
        'bean': 'project_custom',
        'version': '#version#',
        'companyId': '',
        'terminalPc': '1',
        'terminalApp': '1',
        'enableLayout': {// 启用字段
          'rows': []
        },
        'disableLayout': { // 禁用字段
          'rows': []
        },
        'componentLayout': { // 启用组件字段
          'rows': []
        }
      },
      fixedBean: {bean: 'project_custom'}, // 项目固定project
      previewVisable: false,
      containerHeight: 760,
      childVisable: false,
      saveData: {} // 编辑或保存的数据
    }
  },
  methods: {
    // 获取屏幕高度
    getScreenHeight () {
      // this.containerHeight = window.screen.availHeight - 80 + 'px'
    },
    // 保存/预览布局
    handleSaveAllLayout (type) {
      console.log(this.allLayout, 'allLayout')
      this.allLayout.componentLayout.rows = this.$refs.taskDragLeft.componentList
      this.allLayout.disableLayout.rows = this.$refs.taskDragLeft.unUsedLists
      this.allLayout.enableLayout.rows = this.$refs.taskDragMiddle.enableLayout
      if (type) { // 保存
        console.log(this.allLayout, '最终数据')
        console.log(this.$refs.taskDragLeft.unUsedLists, 'refs')
        HTTPServer.saveAllLayout(this.allLayout)
          .then((res) => {
            this.$message({
              message: '保存成功！',
              type: 'success',
              showClose: 'true'
            })
          })
      } else { // 预览
        console.log(this.allLayout, '.......')
        this.setEnableFields(JSON.parse(JSON.stringify(this.allLayout.enableLayout.rows)))
        this.previewVisable = true
        // console.log(JSON.stringify(this.allLayout), '预览最终数据')
      }
    },
    // 获取布局
    getTaskAllLayout () {
      HTTPServer.getAllLayout(this.fixedBean)
        .then((res) => {
          if (JSON.stringify(res) !== '{}') {
            this.allLayout = res
            console.log(res, '获取到的所有布局字段')
            console.log(this.allLayout, '赋值后的所有布局字段')
            let obj = {
              type: 'renew',
              field: this.allLayout.disableLayout.rows
            }
            this.setUnUseFields(obj)
          }
          this.childVisable = true
        })
    },
    // 获取标签列表
    getAllLabelList () {
      HTTPServer.getCooperationParentLabel({type: 2})
        .then((res) => {
          console.log(res, '获取到的标签')
        })
    },
    ...mapMutations({
      setUnUseFields: 'SET_UNUSE_COMPONENT',
      setEnableFields: 'SET_ENABLE_FIELDS'
    })
  },
  beforeDestory () {
    this.$bus.off('sendTaskProperty') // 销毁
    this.$bus.off('initEnableLayout') // 销毁
    this.$bus.off('sendTaskUnusedField')
  },
  computed: {
    ...mapState({
      unUseComponent: state => state.taskCustom.unUseComponent
    })
  },
  watch: {
    unUseComponent (val) {
      // this.allLayout.disableLayout.rows = val
    }
  },
  created () {
    this.getTaskAllLayout()
  },
  mounted () {
    this.getAllLabelList()
    this.$bus.off('closePreview')
    this.$bus.on('closePreview', value => {
      this.previewVisable = false
    })
  }
}
</script>
<style lang="scss" scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity .5s
}
.fade-enter, .fade-leave-to /* .fade-leave-active in below version 2.1.8 */ {
  opacity: 0
}
.slide-enter-active {
  transition: all .5s linear;
}
.slide-leave-active {
  transition: all .5s linear;
}
.slide-enter, .slide-leave-to
/* .slide-fade-leave-active for below version 2.1.8 */ {
  transform: translateX(900px);
}
.task-custom-container {
  height: 100%;
  position: relative;
  .flex {
    display: -webkit-flex; /* Safari */
    display: flex;
  }
  // border: 1px solid #ccc;
  .btn-box {
    justify-content: flex-end;
    height: 40px;
    box-shadow: 0 1px 4px 0 rgba(155,155,155,0.30);
    margin-bottom: 3px;
    >div {
      line-height: 40px;
    }
    .el-button {
      padding: 6px 20px;
    }
  }
  .task-custom-main {
    justify-content: space-between;
    height: calc(100% - 43px);
    // 左边
    .task-custom-left {
      width: 300px;
      box-shadow: 1px 0 4px 0 rgba(155,155,155,0.30);
      border-left: 1px solid rgba(155,155,155,0.30);
      height: 100%;
    }
    // 中间
    .task-custom-middle {
      width: calc(100% - 600px);
      height: 100%;
    }
    // 右边
    .task-custom-right {
      width: 300px;
      box-shadow: -1px 0 4px 0 rgba(155,155,155,0.30);
      border-right: 1px solid  rgba(155,155,155,0.30);
      height: 100%
    }
  }
  .task-defined-preview-box {
    width: 900px;
    position: absolute;
    right: 0;
    top: 0;
    background: #fff;
    box-shadow: -1px 0 4px 0 rgba(155, 155, 155, 0.3);
    // padding: 30px 30px 0 30px;
    height: 100%;
    // overflow: auto;
    z-index: 10;
    .task-dialog-footer {
      margin-top: 10px;
    }
    // .header {
    //   height: 60px;
    //   background: #549AFF;
    //   span {
    //     line-height: 60px;
    //     font-size: 16px;
    //     color: #fff;
    //   }
    // }
  }
  .difined-preview-mask {
    width: 100%;
    height: 100%;
    top:0;
    background:rgba(0, 0, 0, 0.5);
    position: absolute;
  }
}
</style>
