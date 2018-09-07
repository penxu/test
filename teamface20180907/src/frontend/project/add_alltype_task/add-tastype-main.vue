<template>
  <div class="add-tasktype-main-warp">
    <module-create-new v-if="strStatus === 'custom'||strStatus === 'approval'" :modules="modules" :dtlData="dataDtl"></module-create-new>
    <task-create v-if="strStatus === 'task'" :projectId="newprojectid" :autoWorkFlow="autoWorkFlow"></task-create>
    <div class="sideMyTaskAdd" v-if="strStatus === 'memorandum'">
      <div class="closeModal" @click="closeModal"><i class="iconfont icon-xuanrenkongjian-icon4"></i></div>
      <memo-editor :memoDetail="memoDetailData" :isEdit="true"></memo-editor>
      <div class="memoBtn">
        <el-button @click="closeModal">取消</el-button>
        <el-button type="primary" @click="submitMemo">确定</el-button>
      </div>
    </div>
  </div>
</template>
<script>
import ModuleCreateNew from '@/frontend/module/module-create-new'// 自定义新增界面
import TaskCreate from '@/frontend/project/project-details/task-create'// 自定义新增界面
// import memoEditor from '@/frontend/memo/memo-editor' //  备忘录
export default {
  name: 'addTasktypeMain',
  components: {ModuleCreateNew, TaskCreate},
  props: ['allTypeModules', 'allTypeData', 'status', 'projectId', 'autoWorkFlow'],
  // props: {
  //   allTypeModules: {
  //     type: Object
  //   },
  //   allTypeData: {
  //     type: Object
  //   },
  //   status: {
  //     type: String
  //   },
  //   projectId: {
  //     type: String
  //   },
  //   autoWorkFlow: {
  //     type: Boolean
  //   }
  // },
  data () {
    return {
      data: {},
      modules: this.allTypeModules,
      dataDtl: this.allTypeData,
      strStatus: this.status,
      newprojectid: this.projectId,
      memoDetailData: {}
    }
  },
  mounted () {
  },
  methods: {
    closeModal () {
      this.$bus.$emit('closeProTaskCreateModal')
    },
    submitMemo () {
      this.$bus.$emit('dialogSureBtn', 1)
      setTimeout(() => {
        this.$bus.$emit('closeProTaskCreateModal')
        this.$bus.$emit('addSuccess')
      }, 500)
    }
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
  .add-tasktype-main-warp{
    .sideMyTaskAdd{
      .closeModal{
        position: absolute;top:20px;right:20px;&:hover{cursor: pointer;}
      }
      .memoBtn{
        text-align: right;padding-right:20px;margin-top:10px;
      }
    }
  }
  .add-tasktype-main-warp.addEmail{padding-left:30px;}
</style>
<style lang="scss">
.add-tasktype-main-warp{
  .memo-editor .editor-area .edit-box{padding: 20px 20px  0 20px;min-height:480px;}
  .editor-area{
    min-height:500px;
  }
}
</style>
<style lang="scss">
.add-tasktype-main-warp{
  background: #FFFFFF;
  position: fixed;
  width: 800px;
  top: 0;
  bottom: 0;
  right: 0;
  z-index: 10;
  box-shadow: -2px 2px 4px 0 rgba(155, 155, 155, 0.3), 2px -2px 4px 0;
}
</style>
