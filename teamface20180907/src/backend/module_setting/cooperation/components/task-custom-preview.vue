<template>
  <div class="task-preview-box">
    <!-- 富文本 -->
    <!-- <Uediter :value='UediterStr'></Uediter> -->
    <div v-for="field in previewFields" :key="field.name" class="components" :style="{width:field.width}">
      <layout-form :property="field" :saveData="saveData"></layout-form>
    </div>
  </div>
</template>

<script>
import Uediter from '@/common/components/ueditor-compent'
import LayoutForm from '@/common/layout/layout-form'
import { mapState } from 'vuex'
export default {
  name: 'TaskCustomPreview',
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
    padding: 30px;
    overflow-y: auto;
    overflow-x: hidden;
  }
</style>
