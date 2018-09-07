<template>
  <transition name="slider">
    <div class="project-defined-all bordera">
      <div class="project-defined-container">
        <div class="btn-box  bordera flex">
          <div class="project-defined-title"><span>任务自定义</span></div>
          <div class="project-defined-btn">
            <div @click="handleClose()" class="pull-right close"><i class="iconfont icon-pc-member-close"></i></div>
            <div class="pull-right btn">
              <el-button @click="handleSaveAllLayout(false)" type="text"> <i class="iconfont icon-preview"></i> 预 览</el-button>
              <el-button @click="handleSaveAllLayout(true)" type="primary">保 存</el-button>
            </div>
          </div>
        </div>
        <div class="project-defined-main flex">
          <div class="project-defined-left bordera">
            <DefinedLeft :activeFields="allLayout.componentLayout.rows" ref="taskDragLeft" v-if="childVisable"></DefinedLeft>
          </div>
          <div class="project-defined-middle" :class="{' big-middle': !definedRightVisable}">
             <i class="iconfont icon-tongyong-shouqi defined-right-swith" @click="switchDefinedRight()" v-show="!definedRightVisable"></i>
            <DefinedMiddle :enableFields="allLayout.enableLayout.rows" ref="taskDragMiddle" v-if="childVisable"></DefinedMiddle>
          </div>
          <transition name="show-right">
            <div class="project-defined-right" v-show="definedRightVisable">
              <i class="iconfont icon-tongyong-shouqi defined-right-swith" @click="switchDefinedRight()" ></i>
              <DefinedRight></DefinedRight>
            </div>
          </transition>
        </div>
        <transition name="fade">
            <div class="difined-preview-mask" v-show="previewVisable" @click="previewVisable = false">
            </div>
        </transition>
        <transition name="slide">
          <div class="task-defined-preview-box" v-if="previewVisable">
            <DefinedPreview></DefinedPreview>
          </div>
        </transition>
      </div>
      <DefinedFormula :taskOrProjectBean="allLayout.bean"></DefinedFormula>
      <DefinedIdentifer></DefinedIdentifer>
      <HighCondition></HighCondition>
    </div>
  </transition>
</template>
<script>
import DefinedFormula from '@/backend/custom/components/defined-formula' // 高级公式函数公式弹框
import DefinedIdentifer from '@/backend/custom/components/defined-identifer' // 自动编号弹框
import HighCondition from '@/common/alert/high-condition' // 高级条件
import DefinedLeft from '@/common/components/defined-left'
import DefinedRight from '@/common/components/defined-right'
import DefinedMiddle from '@/common/components/defined-middle'
import DefinedPreview from '@/common/components/defined-preview'
import {taskDefaultFields} from '@/common/js/constant'
import {HTTPServer} from '@/common/js/ajax'
import { mapMutations, mapState } from 'vuex'
export default {
  name: 'projectDefined',
  components: {
    DefinedLeft, DefinedRight, DefinedMiddle, DefinedPreview, DefinedFormula, DefinedIdentifer, HighCondition
  },
  data () {
    return {
      projectDefinedVisable: false,
      allLayout: {
        'bean': `project_custom_${this.$route.query.projectId}`,
        'version': '#version#',
        'companyId': '',
        'terminalPc': '1',
        'terminalApp': '1',
        'enableLayout': {// 启用字段
          'rows': JSON.parse(JSON.stringify(taskDefaultFields))
        },
        'disableLayout': { // 禁用字段
          'rows': []
        },
        'componentLayout': { // 启用组件字段
          'rows': []
        }
      },
      previewVisable: false,
      definedRightVisable: true,
      projectBean: {bean: `project_custom_${this.$route.query.projectId}`}, // 项目bean
      childVisable: false
    }
  },
  methods: {
    // 点击关闭
    handleClose () {
      this.$bus.$emit('projectDefined', false)
    },
    // 点击展示/隐藏右边
    switchDefinedRight () {
      this.definedRightVisable = !this.definedRightVisable
    },
    // 点击保存
    handleSaveAllLayout (type) {
      if (type) { // 保存
        this.allLayout.disableLayout.rows = this.$refs.taskDragLeft.unUsedLists
        this.allLayout.enableLayout.rows = this.$refs.taskDragMiddle.enableLayout
        this.allLayout.componentLayout.rows = this.$refs.taskDragLeft.componentList
        if (this.fieldIsRepetition(this.allLayout.enableLayout) === '0') {
          this.$message({
            message: '字段名称不能为空！',
            type: 'warning',
            showClose: 'true'
          })
        } else if (this.fieldIsRepetition(this.allLayout.enableLayout) === '1') {
          this.$message({
            message: '字段名称不能重复！',
            type: 'warning',
            showClose: 'true'
          })
        } else {
          HTTPServer.saveAllLayout(this.allLayout)
            .then((res) => {
              this.$message({
                message: '保存成功！',
                type: 'success',
                showClose: 'true'
              })
            })
        }
      } else { // 预览
        this.allLayout.enableLayout.rows = this.$refs.taskDragMiddle.enableLayout
        this.setEnableFields(JSON.parse(JSON.stringify(this.allLayout.enableLayout.rows)))
        this.previewVisable = !this.previewVisable
        console.log(this.allLayout.enableLayout, '预览最终数据')
      }
    },
    // 获取布局
    getTaskAllLayout () {
      HTTPServer.getAllLayout(this.projectBean)
        .then((res) => {
          if (JSON.stringify(res) !== '{}') {
            // this.allLayout.enableLayout = res.enableLayout
            // this.allLayout.disableLayout = res.disableLayout
            // this.allLayout.componentLayout = res.componentLayout
            this.allLayout = res
            console.log(res, '获取到的所有布局字段')
            console.log(this.allLayout, '赋值后的所有布局字段')
            // this.setEnableFields(this.allLayout.enableLayout.rows)
            // this.$refs.taskDragMiddle.updateField()
            let obj = {
              type: 'renew',
              field: this.allLayout.disableLayout.rows
            }
            this.setUnUseFields(obj)
          }
          this.childVisable = true
        })
    },
    // 判断字段是否重复
    fieldIsRepetition (list) {
      let name = []
      list.rows.map((item2) => {
        name.push(item2.label)
        if (item2.type === 'subform') {
          item2.componentList.map((item3) => {
            name.push(item3.label)
          })
        }
      })
      if (name.includes('')) {
        return '0'
      } else if (!name.includes('') && JSON.stringify(new Set(name)) === JSON.stringify(name)) {
        return '2'
      } else {
        return '1'
      }
    },
    ...mapMutations({
      setUnUseFields: 'SET_UNUSE_COMPONENT',
      setEnableFields: 'SET_ENABLE_FIELDS'
    })
  },
  computed: {
    ...mapState({
      usnUseComponent: state => state.taskCustom.unUseComponent
    })
  },
  mounted () {
    console.log(taskDefaultFields, 'taskDefaultFields')
    this.$bus.$on('changeProjectId', (value) => {
      console.log(value)
      if (value) {
        this.projectBean.bean = `project_custom_${value}`
        this.allLayout.bean = `project_custom_${value}`
      }
    })
    console.log(this.$route.query.projectId, '.......')
    this.$bus.off('closePreview')
    this.$bus.on('closePreview', value => {
      this.previewVisable = false
    })
  },
  created () {
    this.getTaskAllLayout()
  }
}
</script>
<style lang="scss">
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
.project-defined-all {
  width: 100%;
  height: 100%;
  position: relative;
  .project-defined-container {
    width: 90%;
    // min-width: 1100px;
    height: 100%;
    // border: 1px solid #ccc;
    position: absolute;
    top:0;
    right:0;
    box-shadow: 1px 0 4px 0 rgba(155,155,155,0.30);
    background: #fff;
    // transform:  translateX(90%);
    .flex {
      display: -webkit-flex; /* Safari */
      display: flex;
    }
    // border: 1px solid #ccc;
    .btn-box {
      padding-left: 20px;
      padding-right: 20px;
      justify-content: space-between;
      height: 60px;
      box-sizing: border-box;
      box-shadow: 0 1px 4px 0 rgba(155,155,155,0.30);
      margin-bottom: 4px;
      .project-defined-title {
        line-height: 60px;
        span {
          font-size: 16px;
        }
      }
      .project-defined-btn {
        div.btn {
          line-height: 60px;
        }
        div.close {
          margin-left:20px;
          padding-left: 17px;
          border-left: 1px solid #E5E5E5;
          cursor: pointer;
          margin-top: 16px;
          padding-top: 5px;
          padding-bottom: 5px;
        }
        .el-button {
          padding: 6px 20px;
        }
      }
    }
    .project-defined-main {
      justify-content: space-between;
      height: calc(100% - 60px);
      // 左边
      .project-defined-left {
        width: 300px;
        box-shadow: 1px 0 4px 0 rgba(155,155,155,0.30);
        height:100%;
      }
      // 中间
      .project-defined-middle {
        width: calc(100% - 600px);
        // box-shadow: 2px 0 4px 0 rgba(155,155,155,0.30);
        margin:0 4px;
        height:100%;
        position: relative;
        i.defined-right-swith {
          position: absolute;
          right: 0;
          color: #ccc;
          top: 324px;
          font-size: 25px;
          cursor: pointer;
          z-index: 1;
          right: -12px;
          transform: rotate(180deg);
        }
      }
      .big-middle {
        width: calc(100% - 300px);
        // transition: width .5s linear;
      }
      // 右边
      .project-defined-right {
        box-shadow: -1px 0 4px 0 rgba(155,155,155,0.30);
        width: 300px;
        position: relative;
        i.defined-right-swith {
          position: absolute;
          left: -8px;
          color: #ccc;
          top: 324px;
          font-size: 25px;
          cursor: pointer;
          z-index: 1;
        }
      }
    }
  }
  .task-defined-preview-box {
    width: 900px;
    position: absolute;
    right: 0;
    top: 0;
    background: #fff;
    box-shadow: -1px 0 4px 0 rgba(155, 155, 155, 0.3);
    height: 100%;
    // overflow: auto;
    z-index: 10;
  }
  .difined-preview-mask {
    width: 100%;
    height: 100%;
    top:0;
    background:rgba(0, 0, 0, 0.5);
    position: absolute;
  }
}
.show-right-enter-active, .show-right-leave-active {
  transition: all .3s linear;
}
.show-right-enter, .show-right-leave-to{
  transform:  translateX(300px);
  transition: all .2s linear;
}
.show-mask-enter-active, .show-mask-leave-active {
  transition: all .2s linear;
}
.show-mask-enter, .show-mask-leave-to{
  opacity: 0;
  transition: all .2s linear;
}
</style>
