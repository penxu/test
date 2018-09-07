 <!-- 头像组件 -->
<template>
  <div class="avatar-mian-1" @click="openemployeeForm($event, employeeData)">
    <div v-if="!employeeData.picture" class="simpName open">{{employeeData.name | limitTo(-2)}}</div>
    <img v-if="employeeData.picture" :src="employeeData.picture + '&TOKEN=' + token" class="open" />
    <span class="name open" v-show="employeeData.is_name">{{employeeData.name}}</span>
    <el-dialog title='个人名片' :visible.sync='employeeForm' class='personal-card-dialog' :modal="false">
      <employee-card :employeeData="employeeInfo" :departmentData="departmentInfo" :companyData="companyInfo"></employee-card>
    </el-dialog>
  </div>
</template>

<script>
import { HTTPServer } from '@/common/js/ajax.js'
import employeeCard from '@/common/components/employee-card'/** 个人名片 */
import $ from 'jquery'
export default {
  props: ['employeeData'],
  components: {employeeCard},
  data () {
    return {
      employeeInfo: {},
      companyInfo: {},
      departmentInfo: {},
      employeeForm: false,
      visible1: false,
      time: new Date().getTime(),
      data5: {'id': 3, 'name': '三六', 'picture': '', 'type': 1, 'sign_id': 34, 'value': '1:3'},
      token: JSON.parse(sessionStorage.requestHeader).TOKEN
    }
  },
  watch: {
  },
  /* 页面加载后执行 */
  mounted () {
    this.$bus.on('employee-card-close', (value) => {
      if (value) {
        this.employeeForm = false
      }
    })
  },
  methods: {
    /** 开启弹窗 */
    openemployeeForm (event, data) {
      if (event.target.className.indexOf('open') < 0) {
        return
      }
      var jsondata = {}
      if (data.sign_id) {
        jsondata.sign_id = data.sign_id
      }
      this.queryEmployeeInfo(jsondata, event.currentTarget)
    },
    /** 获取员工信息 368 * 192 */
    queryEmployeeInfo (jsondata, event) {
      console.log(event)
      console.log($(event).offset())
      this.employeeInfo = {}
      this.companyInfo = {}
      this.departmentInfo = {}
      HTTPServer.getQueryEmployeeInfo(jsondata, '').then((res) => {
        this.employeeForm = true
        this.employeeInfo = res.employeeInfo || {}
        this.companyInfo = res.companyInfo || {}
        this.departmentInfo = res.departmentInfo || {}
        this.employeeInfo.fabulous_count = res.fabulous_count
        this.employeeInfo.fabulous_status = res.fabulous_status
        setTimeout(() => {
          this.time = new Date().getTime()
          var offset = $(event).offset()
          var width = $(event).width()
          var height = $(event).height()
          var mmWidth = document.body.clientWidth
          var mmHeight = document.body.clientHeight
          console.log(offset)
          console.log('mmWidth', mmWidth, 'mmHeight', mmHeight)
          var left = offset.left + width + 10
          var top = offset.top - 97 + (height / 2)
          if (offset.left + width + 378 > mmWidth) {
            left = offset.left - 378
          }
          console.log(offset.top + 98 + (height / 2), offset.top + 98 + (height / 2) > mmHeight)
          if (offset.top + 98 + (height / 2) > mmHeight) {
            top = mmHeight - 195
          }
          console.log(left, top)
          event.children[2].children[0].style.margin = top + 'px 0 0 ' + left + 'px'
        }, 10)
      })
    }
  }
}
</script>

<style lang='scss'>
@import './static/css/emoji.scss';
.avatar-mian-1 {display: inline-block;
    .simpName {
        width: 36px;
        height: 36px;
        line-height: 36px;
        display: inline-block;
        font-size: 12px;
    }
    >img{width: 36px;height: 36px;border-radius: 50%;}
    .personal-card-dialog{overflow: hidden;
        .el-dialog{background: #FFFFFF;box-shadow: 2px -2px 4px 0 rgba(155,155,155,0.30), -2px 2px 4px 0 rgba(155,155,155,0.30);border-radius: 5px;width: 368px;}
        .el-dialog__header{display: none;}
        .el-dialog__body{padding: 0;height: auto;min-height: 190px;min-width: 360px;}
    }
}
</style>
