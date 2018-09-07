<template>
  <div class="webform-drag-center">
    <!-- <custom-header-img :datas="headerImg" v-if="headerImg.type !== 0"></custom-header-img> -->
    <div class="colum-box">
      <div class="title-box">
        <span>{{formProps.title}}</span>
      </div>
      <draggable v-model="enableLayout" :options="dragOptions" @add="dragAdd($event)" @update="dragUpdate($event)" class="box-item clear">
        <div v-for="(item,index) in enableLayout" class="width-100 components-box" :key="item.name" @click="settings(item,index)">
          <i class="el-icon-close" @click.stop="deleteComponent(index)"></i>
          <!-- 自定义组件区 -->
          <div class="" v-if="!item.source">
            <div class="components" :class="{'structure-type':item.field.structure === '0', 'active-compons':item.active === '1'}" v-if="!item.source">
              <span><i class="red" v-if="item.field.fieldControl === '2'">*</i>{{item.label}}</span>
              <div class="type-text" :class="item.type" :style="componentBgImg(item.type, item.width, item.field.structure)"></div>
            </div>
          </div>
          <!-- 辅助组件区 -->
          <webformLayout :item="item"></webformLayout>
        </div>
      </draggable>
      <!-- 提交区 -->
      <div class="submit-box" @click="handleSubmitSetting()" v-show="isShowSubmit" >
        <div class="submit-area" :class="{'submit-auto': submitAreaData.field.widthType === '0'}">{{submitAreaData.field.text}}</div>
      </div>
    </div>
  </div>
</template>

<script>
import draggable from 'vuedraggable'
// import CustomDragLeft from '@/backend/custom/components/custom-drag-left'
// import CustomHeaderImg from '@/common/components/custom-header-img'
import webformLayout from '@/backend/custom/components/webform-layout'
import { mapMutations, mapGetters, mapState } from 'vuex'
export default {
  name: 'CustomWebformMiddle',
  components: {
    draggable,
    webformLayout
  },
  props: ['enableFields'],
  data () {
    return {
      enableLayout: [],
      dragOptions: {
        'animation': 200,
        'group': { name: 'compontents', pull: false, put: true },
        'sort': true,
        'ghostClass': 'component-box',
        'filter': '.no-drag'
      },
      temField: null,
      currentIndex: null,
      formProps: {},
      currentLayout: {},
      submitAreaData: {
        type: 'submitArea',
        field: {
          widthType: '0', // 0 自适应 1 与表单同宽
          text: '提交',
          feedbackType: '0', // 0 提交成功提示，1 跳转链接
          jumpUrl: '',
          description: '',
          title: ''
        }

      }
    }
  },
  methods: {
    // 拖拽新增
    dragAdd (evt, line) {
      // this.enableLayout = JSON.parse(JSON.stringify(this.enableLayout))
      console.log(evt, evt.item.dataset.type, 'event')
      this.currentIndex = evt.newIndex
      let type = evt.item.dataset.type
      let layout = {
        field: {}
      }
      if (type === 'renew') {
        layout = this.enableLayout[evt.newIndex]
      } else {
        let label
        let timestampName = type + '_' + new Date().getTime()
        switch (type) {
          case 'description':
            label = '文本描述'
            layout.text = '' // 描述内容，HTML片段
            // layout.field.structure = ''
            // layout.field.fieldControl = ''
            break
          case 'splitLine':
            label = '分割线'
            layout.field.lineType = 'solid' // solid: 实线， dashed：虚线，dotted，点画线，double 双实线
            layout.field.lineColor = '#000'
            layout.field.lineWidth = '1px'
            break
          case 'button':
            label = '按钮'
            layout.field.text = '按钮文字'
            layout.field.color = '#1890FF'
            layout.field.fontSize = 14
            layout.field.bold = false
            layout.field.icon = ''
            layout.field.radius = 4
            layout.field.height = 40
            layout.field.width = 200
            layout.field.widthType = '1'
            layout.position = 'center'
            layout.eventType = '0' // 0 无， 1， 跳转链接， 2 拨打电话 3 发短信， 4 发邮件
            layout.eventText = ''
            layout.styleType = '0'
            layout.imgSrc = '/static/img/chart_big/0.png'
            break
          case 'imageShow':
            label = '图片'
            layout.count = '1'
            layout.imageList = [{imageUrl: '', isJumpUrl: false, jumpUrl: ''}]
            layout.isToUrl = '0'
            layout.field.grid = '1'
            break
          case 'imagecarousel':
            label = '图片轮播'
            layout.imageList = [{img: '', isJumpUrl: '0', url: '', title: ''}]
            layout.field.interval = 3
            layout.field.height = '120'
            break
          case 'video':
            label = '视频展示'
            layout.url = '/static/file/mov_bbb.mp4'
            break
          case 'staticMap':
            label = '静态地图'
            layout.address = ''
            layout.isMarkMsg = '0'
            layout.markMsg = ''
            layout.scale = 10
            break
          case 'timer':
            label = '计时器'
            layout.form = '0' // 0 计时， 1 倒计时
            layout.formatType = '2'
            layout.initialTime = 50000
            layout.arrivalTime = ''
            layout.countDownType = '1'
            break
          case 'attendance':
            label = '签到'
            layout.title = ''
            layout.description = ''
            layout.verifyField = {value: '0', label: '姓名'}
            layout.isAllowOpenSignIn = false
            layout.qrCode = ''
            break
          default:
            break
        }
        layout.name = timestampName // 唯一ID
        layout.label = label // 字段名称
        layout.type = type // 组件类型
        layout.typeText = label // 组件中文名称
        layout.source = 'webform'
        // layout.field.fieldControl = '1'
        // layout.width = width // 组件长度
        // layout.state = '1' // 组件状态
        // layout.remove = '1' // 组件是否可删除
        // layout.active = '1' // 组件active
        // layout.field.addView = '1' // 新增显示
        // layout.field.editView = '1' // 编辑显示
        // layout.field.structure = '1' // 结构(0上下布局 '1'左右布局)
        // layout.field.isShowCard = '0' // 是否展示任务卡片(0 不显示，1 显示)
      }
      this.enableLayout.map((item, index) => {
        item.active = '0'
      })
      this.$set(this.enableLayout, evt.newIndex, layout)
      console.log(this.enableLayout, 'enableLayout')
      this.set_webform_enable(this.enableLayout)
      this.currentLayout = layout
      console.log(this.currentLayout, '当前布局')
      this.$bus.emit('sendTaskProperty', layout, this.enableLayout)
    },
    // 拖拽更新
    dragUpdate (evt) {
      // this.enableLayout.map((item, index) => {
      //   item.active = '0'
      // })
      // this.enableLayout[evt.newIndex].active = '1'
      // this.$bus.emit('sendProperty', this.enableLayout[evt.newIndex], this.columnNumber) // 给属性设置传值
    },
    // 设置模块属性
    setModule (evt) {
      this.$bus.emit('sendModuleName', evt)
    },
    // 设置属性
    settings (list, index) {
      this.enableLayout.map((item, index) => {
        // item.active = '0'
      })
      // list.active = '1'
      this.currentIndex = index
      this.temField = JSON.parse(JSON.stringify(list))
      this.$bus.emit('sendTaskProperty', list, this.columnNumber) // 给属性设置传值
    },
    // 删除字段
    deleteComponent (index) {
      console.log(index, 'index')
      let rmItem = this.enableLayout[index]
      this.enableLayout.splice(index, 1)
      console.log(this.enableLayout, rmItem, '删除字段')
      if (!rmItem.source === 'webform') {
        let obj = {
          type: 'delete',
          field: rmItem
        }
        this.setDisableFields(obj)
      }
    },
    // 组件背景
    // 组件背景图
    componentBgImg (type, width, structure) {
      let url = 'url(/static/img/custom/component-'
      if (width === '50%' && structure === '1') {
        url += 's-left-'
      } else if (width === '50%' && structure === '0') {
        url += 's-top-'
      } else if (width === '100%' && structure === '1') {
        url += 's-left-'
      } else if (width === '100%' && structure === '0') {
        url += 's-top-'
      }
      let background = {
        'background-image': url + type + '.png)'
      }
      return background
    },
    // 点击提交区
    handleSubmitSetting () {
      this.$bus.emit('sendTaskProperty', this.submitAreaData, this.enableLayout)
    },
    ...mapMutations({
      setSingeLayout: 'SET_SINGLE_LAYOUT',
      set_webform_enable: 'SET_WEBFOTM_ENABLE',
      setDisableFields: 'SET_DISAB_FIELDS',
      setCurrentIndex: 'SET_CURRENT_INDEX'
    })
  },
  created () {
  },
  computed: {
    // 判断有没有来源于自定义字段
    isShowSubmit () {
      return this.enableLayout.some((item, index) => {
        return !item.source
      })
    },
    ...mapGetters({
      getEnableLayout: 'getEnableLayout',
      getFromProps: 'getFromProps'
    }),
    ...mapState({
      webform_layout: state => state.webformCustom.webform_layout
    })
  },
  mounted () {
    // console.log(this.enableFields, this.getCurrentFields, '父组件穿过来的')
    this.$bus.on('webformEditorContent', value => {
      console.log(this.currentIndex, 8789789789)
      this.enableLayout[this.currentIndex].text = value
      console.log(this.enableLayout[this.currentIndex], '修改过后的富文本')
    })
    this.enableLayout = JSON.parse(JSON.stringify(this.getEnableLayout()))
    console.log(this.enableLayout, '已存在的布局')
    this.formProps = JSON.parse(JSON.stringify(this.getFromProps()))
    console.log(this.formProps, '中间的属性')
  },
  watch: {
    enableLayout (newVal) {
      // console.log(newVal, 'watch新数据')
    },
    'webform_layout.formProps' (newVal) {
      console.log(newVal, 'watch中间属性')
      this.formProps = newVal
    }
  }
}
</script>

<style lang="scss" scoped>
.webform-drag-center {
  // overflow: auto;
  height: 100%;
  width: 470px;
  flex: 1;
  padding: 42px 48px;
  background: #FFFFFF;
  // overflow: auto;
  text-align: left;
  .colum-box {
    padding: 0;
    overflow: auto;
    border-radius: 3px;
    height: 100%;
    border: 1px dashed #d9d9d9;
    .title-box {
      height: 44px;
      background: #0084FF;
      border-radius: 4px 4px 0 0;
      text-align: center;
      width: 100%;
      span {
        color: #FFFFFF;
        line-height: 44px;
      }
    }
    .line {
      height: 40px;
      line-height: 40px;
      background-color: #f5f5f5;
      border-radius: 3px 3px 0 0;
      span {
        font-size: 16px;
        color: #17171a;
        margin: 0 60px 0 0;
      }
      i.el-icon-success {
        color: #51d0b1;
        margin: 0 10px;
      }
      i.el-icon-close {
        float: right;
        margin: 12px 10px;
      }
    }
    .box-item {
      display: block;
      width: 100%;
      min-height: 50px;
      margin: 0;
      // margin-bottom: 20px;
      min-height: calc(100% - 124px);
    }
    //
    .components-box {
      position: relative;
        >i {
          visibility: hidden;
          position: absolute;
          font-size: 10px;
          color: #979797;
          right: 5px;
          top: 5px;
          cursor: pointer;
        }
        &:hover {
          background: #F0F9FF;
          border: 1px dashed #D9D9D9;
          border-radius: 4px;
          >i {
            visibility: visible;
            z-index: 10;
          }
        }
    }
    .submit-box {
      height: 80px;
      padding: 20px 0;
      box-sizing: border-box;
      text-align: center;
      .submit-area {
        color: #fff;
        width: 100%;
        height: 40px;
        background: #1890FF;
        text-align: center;
        line-height: 40px;
        border-radius: 4px;
      }
      .submit-area.submit-auto {
        padding: 0 20px;
        width: auto;
        display: inline-block;
      }
      &:hover {
        background: #F0F9FF;
        border: 1px dashed #D9D9D9;
        border-radius: 4px;
        box-sizing: border-box;
      }
    }
  }
  .components.width-100 {
    width: calc(50% - 50px);
  }
  .components.structure-type {
    // height: 80px;
    span {
      display: block;
      width: calc(100% - 50px);
      height: 20px;
      line-height: 20px;
      margin: 10px 0 0 15px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .type-text {
      // margin: 0 0 0 15px;
      width: calc(100% - 80px);
    }
    &:hover {
      .delete-button {
        top: 30px;
      }
    }
  }
  .components.width-100 {
    width: 100%;
    .type-text{
      flex: 0 0 630px;
    }
  }
  .components.width-100.structure-type {
    display: block;
    .type-text{
      width: calc(100% - 20px)
    }
  }
  .components.structure-type {
    display: block;
    span {
      display: block;
      width: calc(100% - 50px);
      height: 20px;
      line-height: 20px;
      margin: 0 0 10px;
    }
    .type-text {
      width: 100%;
    }
    .type-text.personnel, .type-text.department{
      width: 30px;
      height: 30px;
    }
    .type-text.attachment, .type-text.picture{
      width: 106px;
      height: 32px;
    }
  }
  .components {
    position: relative;
    display: flex;
    align-items: center;
    width: 100%;
    margin: 0 0 2px 0;
    padding: 22px 10px 20px;
    border-radius: 2px;
    font-size: 12px;
    color: #4a4a4a;
    // cursor: move;
    // &:hover {
    //   // background-color: #edfaf7;
    //   // box-shadow: 0 3px 10px 0 #e7e7e7;
    //   .type-text {
    //     border-color: #ffffff;
    //   }
    //   .delete-buttond {
    //     visibility: visible;
    //   }
    // }
    span {
      flex:0 0 68px;
      text-align: left;
      margin: 0 8px 0 0;
      font-size: 14px;
      color: #4a4a4a;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      i.red {
        font-style: normal;
        color: #f94c4a;
        margin: 0 5px 0 0;
      }
    }
    .type-text {
      flex: 0 0 265px;
      height: 30px;
      border-radius: 2px;
      background-repeat: no-repeat;
      background-size: cover;
      background-size:100% 100%
    }
    .type-text.textarea{
      height: 88px;
    }
    .type-text.subform{
      height: 71px;
    }
    .type-text.multi{
      height: 16px;
    }
    .type-text.multitext{
      height: 256px;
    }
    .type-text.personnel, .type-text.department{
      flex: 0 0 30px;
      height: 30px;
    }
    .type-text.attachment, .type-text.picture{
      flex: 0 0 106px;
      height: 32px;
    }
    // .delete-buttond {
    //   visibility: hidden;
    //   position: absolute;
    //   right: 4px;
    //   top: 5px;
    //   width: 12px;
    //   height: 12px;
    //   line-height: 12px;
    //   text-align: center;
    //   cursor: pointer;
    //   i {
    //     font-size: 12px;
    //     color: #B6B6B6;
    //   }
    // }
  }
  >.el-button{
    margin: 10px 0 0 0;
  }
  .draging {
    background: #F0F9FF;
    border: 1px dashed #D9D9D9;
    border-radius: 4px;
    height: 140px;
    width: 100%;
  }
}
</style>
<style lang="scss">
.webform-drag-center {
  ol {
    padding-left: 20px !important;
  }
  ol {
    li {
      list-style-type:decimal !important;
      // list-style-position:inside !important;
      p {
        // display: inline !important;
      }
    }
  }
}
</style>
