<template>
  <div class="task-preview-box">
    <div class="header"><span>预览</span></div>
    <div class="task-middle">
      <div class="layout-box">
        <div v-for="field in previewFields" :key="field.name" class="components" :style="{width:field.width}">
          <layout-form :property="field" :saveData="saveData"></layout-form>
        </div>
      </div>
    </div>
    <div class="task-dialog-footer">
        <el-button @click="handleClosePreview" size="small">取 消</el-button>
        <el-button type="primary" @click="handleClosePreview" size="small">确 定</el-button>
    </div>
  </div>
</template>

<script>
import Uediter from '@/common/components/ueditor-compent'
import LayoutForm from '@/common/layout/layout-form'
import { mapState } from 'vuex'
export default {
  name: 'DefinedPreview',
  components: { Uediter, LayoutForm },
  props: {
    // value: ''
  },
  data () {
    return {
      activeName: 'components',
      unUsedLists: [],
      componentVisable: false, // 添加组件弹窗
      allComponents: null, // 总组件列表
      componentList: this.activeFields,
      UediterStr: '',
      previewFields: {},
      saveData: {} // 编辑或保存的数据
    }
  },
  created () {
  },
  methods: {
    // ...mapMutations({
    //   setSingeLayout: 'SET_SINGLE_LAYOUT',
    //   setAllLayout: 'SET_ALL_LAYOUT',
    //   setUnUseFields: 'SET_UNUSE_COMPONENT'
    // })
    handleClosePreview () {
      this.$bus.emit('closePreview', false)
    }
  },
  computed: {
    ...mapState({
      enableFields: state => state.taskCustom.enableFields
    })
  },
  beforeDestory () {

  },
  mounted () {
    this.previewFields = JSON.parse(JSON.stringify(this.enableFields))
    console.log(this.previewFields, '预览的数据')
  },
  watch: {
  }
}
</script>

<style lang="scss" >
  .task-preview-box {
    width: 100%;
    position: relative;
    background: #EEF0F8;
    height: 100%;
    overflow-x: hidden;
    .header {
      background: #549AFF;
      padding: 0 30px;
      height: 60px;
      span {
        line-height: 60px;
        padding-left: 20px;
        color: #fff;
        font-size: 16px;
      }
    }
    .task-middle {
      height: calc(100% - 120px);
      padding: 40px 50px 0px;
      background: #EEF0F8;
      overflow-y: auto;
      overflow-x: hidden;
      .layout-box {
        min-height: 100%;
        padding: 10px;
        background: #fff;
        box-shadow: -2px 2px 4px 0 rgba(155, 155, 155, 0.3);
      }
    }
    .task-dialog-footer {
      height: 60px;
      border-top: 1px solid #ddd;
      line-height: 60px;
      background: #fff;
      text-align: right;
      padding-right: 15px;
    }
  }
</style>
