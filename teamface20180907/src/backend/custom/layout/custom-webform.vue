<template>
<div class="custom-webform-wrip flex-box">
  <!-- <developing></developing> -->
  <div class="save-btn"><el-button  type="primary" size="small" @click="handleSave()">保存</el-button></div>
  <div style="width: 300px; height: 100%">
    <CustomWebformLeft v-if="isDone" ref="webformLeft"></CustomWebformLeft>
  </div>
  <div style="height: 100%;" class="webform-middle" >
    <CustomWebformMiddle v-if="isDone" ref="webformMiddle"></CustomWebformMiddle>
  </div>
  <div  style="width: 300px; height: 100%;">
    <CustomWebformRight v-if="isDone"></CustomWebformRight>
  </div>
</div>
</template>

<script>
import CustomWebformLeft from '@/backend/custom/components/custom-webform-left'
import CustomWebformRight from '@/backend/custom/components/custom-webform-right'
import CustomWebformMiddle from '@/backend/custom/components/custom-webform-middle'
import qrcode from '@/common/components/Qrcode'
import { mapGetters, mapMutations, mapState } from 'vuex'
import { HTTPServer } from '@/common/js/ajax.js'
export default {
  name: 'CustomWebform',
  props: ['webformId'],
  components: {
    CustomWebformLeft,
    CustomWebformRight,
    CustomWebformMiddle,
    qrcode
  },
  data () {
    return {
      qrval: 'http://192.168.1.49:8088/#/',
      size: 100,
      allLayout: {
        'moduleBean': this.$route.query.bean, // 模块英文名称
        'formProps': {
          'title': '第一个web表单', // 模块中文名称
          'isAppearance': '0', //  是否启用外观设置，0 不启用，1 启用
          'accessAuth': '1', // 填写权限： 0 密码访问，1 公开访问
          'accessPassword': '', // 公开访问为空
          'expandLink': [], // 拓展链接列表
          'shareTitle': '', // 分享标题
          'shareDescription': '', // 分享内容
          'isSignIn': '0', // 0 不签到，1 签到
          'signInMsg': {
            'title': '',
            'content': '',
            'verfifyField': '',
            'isOpenSign': '0',
            'isClientOnce': '0'
          }
        },
        'enableLayout': [// 使用字段布局
        ],
        'disableLayout': [// 未使用字段布局
        ]
      },
      isDone: false // 获取布局完成
    }
  },
  created () {
  },
  methods: {
    ...mapMutations({
      setWebformLayout: 'SET_WEBFORM_LAYOUT',
      setWebformEnable: 'SET_WEBFOTM_ENABLE'
    }),
    // 点击保存
    handleSave () {
      console.log(this.$refs.webformMiddle.submitAreaData, '000000000')
      this.$refs.webformMiddle.enableLayout.push(this.$refs.webformMiddle.submitAreaData)
      this.setWebformEnable(JSON.parse(JSON.stringify(this.$refs.webformMiddle.enableLayout)))
      // this.webform_layout.disableLayout = JSON.parse(JSON.stringify(this.$refs.webformLeft.unUsedLists))
      console.log(this.webform_layout, '最终的布局')
      HTTPServer.saveWebformLayout(this.webform_layout).then((res) => {
        this.$message({type: 'success', message: '保存成功！', showClose: true})
        this.$bus.emit('toWebformList', false)
      }).catch(() => {
        this.$refs.webformMiddle.enableLayout.splice(this.$refs.webformMiddle.enableLayout.length - 1, 1)
      })
    },
    // 获取布局
    getAllLayout () {
      let getParams = {id: this.webformId.id, moduleBean: this.$route.query.bean}
      console.log(getParams, 'params')
      HTTPServer.getWebformLayout(getParams, true).then((res) => {
        // this.$message({type: 'success', message: '保存成功！', showClose: true})
        console.log(res, '获取到的布局')
        // res.formProps = this.allLayout.formProps
        this.setWebformLayout(res)
        this.isDone = true
      })
    }
  },
  mounted () {
    this.getAllLayout()
  },
  computed: {
    ...mapGetters({
      getEnableLayout: 'getEnableLayout',
      getFromProps: 'getFromProps'
    }),
    ...mapState({
      webform_layout: state => state.webformCustom.webform_layout
    })
  }
}
</script>

<style lang="scss" scoped>
.custom-webform-wrip {
  height: 100%;
  justify-content: space-between;
  // margin: 250px auto 0;
  background: #FFFFFF;
  border-top: 1px solid #ddd;
  position: relative;
  .webform-middle {
    // margin-top: 50px;
    box-shadow: 0 0 15px 1px rgba(154, 158, 161, 0.15)
  }
  .save-btn {
    position: absolute;
    right: 30px;
    top: -50px;
  }
}
</style>
