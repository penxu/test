 <!-- 头像组件 -->
<template>
  <div class="avatar-mian">
      <div v-if="!employeeData.picture" class="simpName" @click="openemployeeForm($event, employeeData)">{{employeeData.name | limitTo(-2)}}</div>
      <img v-if="employeeData.picture" :src="employeeData.picture + '&TOKEN=' + token" @click="openemployeeForm($event, employeeData)" />

      <el-dialog title='个人名片' :visible.sync='employeeForm' class='employeeForm' :modal="false">
        <div class="employee-data">
            <img v-if="employeeInfo.picture" :src="employeeInfo.picture + '&TOKEN=' + token" />
            <div class="simpName" v-if="!employeeInfo.picture">{{employeeInfo.employee_name | limitTo(-2)}}</div>
            <div>
                <div class="praise">
                    <i class="iconfont icon-pc-paper-fighti" :class="employeeInfo.fabulous_status === 1 ? 'active': ''" @click="fabulous"></i>
                    <span class="num">{{employeeInfo.fabulous_count || 0}}</span>
                </div>
                <div><span class="name">{{employeeInfo.employee_name}}</span>
                  <i v-if="employeeInfo.sex === '0'" class="iconfont icon-gender-female"></i>
                  <i v-if="employeeInfo.sex === '1'" class="iconfont icon-gender-man"></i>
                </div>
                <div>
                  <span v-for="(depInfo, index) in departmentInfo" :key="index" class="department" v-if="depInfo.is_main == 1">{{depInfo.department_name}}</span>
                  <span class="postion" v-if="employeeInfo.post_name">-&nbsp;{{employeeInfo.post_name}}</span></div>
            </div>
        </div>
        <div class="signature">
          <i v-if="!employeeInfo.mood" class="iconfont icon-pc-paper-face" @click="openEmoji" title="表情"></i>
          <span v-if="employeeInfo.mood" v-html="traverseEmoji(employeeInfo.mood)" @click="openEmoji" :title="employeeInfo.mood"></span>
            <input v-if="employeeInfo.id == curEmployeeInfo.id" type="text" v-model="employeeInfo.sign" v-on:blur="changeSign($event)" />
            <input v-if="employeeInfo.id != curEmployeeInfo.id" type="text" readonly="readonly" v-model="employeeInfo.sign" />
        </div>
        <!-- <div class="signature">
          <i v-if="!employeeInfo.mood" class="iconfont icon-icon-test" @click="openEmoji" title="上传"></i>
          <span v-if="employeeInfo.mood" v-html="traverseEmoji(employeeInfo.mood)" @click="openEmoji" :title="employeeInfo.mood"></span>
            <input v-if="employeeInfo.id == curEmployeeInfo.id" type="text" v-model="employeeInfo.sign" v-on:blur="changeSign($event)" />
            <input v-if="employeeInfo.id != curEmployeeInfo.id" type="text" readonly="readonly" v-model="employeeInfo.sign" />
        </div> -->
    </el-dialog>
  </div>
</template>

<script>
import { HTTPServer } from '@/common/js/ajax.js'
import {plist, emojiUrl, traverseEmoji, locationEmoji} from '@/common/js/emoji.js'
import EmojiForm from '@/common/components/emoji'
// import * as tool from '../../common/js/tool.js'
export default {
  props: ['employeeData'],
  components: {EmojiForm},
  data () {
    return {
      employeeInfo: {},
      companyInfo: {},
      departmentInfo: {},
      employeeForm: false,
      visible1: false,
      time: new Date().getTime(),
      data5: {'id': 3, 'name': '三六', 'picture': '', 'type': 1, 'sign_id': 34, 'value': '1:3'},
      plist: plist,
      emojiUrl: emojiUrl,
      traverseEmoji: traverseEmoji,
      locationEmoji: locationEmoji,
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      curEmployeeInfo: {}
    }
  },
  watch: {
  },
  /* 页面加载后执行 */
  mounted () {
    this.$bus.on('checkemoji', (value) => {
      if (value) {
        if (value.id === ('personal' + this.employeeData.sign_id + '-' + this.time) && value.data) {
          this.editEmployeeDetail({'data': {'mood': value.data.key}})
        }
      }
    })
  },
  methods: {
    /** 点赞取消 */
    fabulous () {
      var jsondata = {'id': this.employeeInfo.id, 'status': (this.employeeInfo.fabulous_status === 0) ? 1 : 0}
      HTTPServer.postWhetherFabulous(jsondata, false).then((res) => {
        this.$message({
          type: 'success',
          message: '修改成功!'
        })
        this.queryEmployeeInfo({'sign_id': this.employeeInfo.sign_id})
      })
    },
    openEmoji (event) {
      if (this.employeeData.sign_id !== this.curEmployeeInfo.sign_id) {
        return
      }
      this.$bus.emit('getMmojiData', {'id': 'personal' + this.employeeData.sign_id + '-' + this.time})
      this.locationEmoji(event)
    },
    /** 开启弹窗 */
    openemployeeForm (event, data) {
      this.curEmployeeInfo = JSON.parse(sessionStorage.userInfo).employeeInfo
      console.log(event)
      if (data.seleteForm === false) {
        return
      }
      this.employeeForm = true
      this.time = new Date().getTime()
      var getLeft = event.clientX - event.offsetX
      var getTop = event.clientY - event.offsetY
      var mmodel = event.target.nextElementSibling
      var mmWidth = document.body.clientWidth
      var mmHeight = document.body.clientHeight
      var clientHeight = event.target.clientHeight
      var left = getLeft
      var top = getTop + clientHeight + 10
      if (getTop + 190 > mmHeight) {
        top = getTop - 145
      }
      if (getLeft + 340 > mmWidth) {
        left = getLeft - 340 + 36
      }
      mmodel.children[0].style.margin = top + 'px 0 0 ' + left + 'px'
      var jsondata = {}
      if (data.sign_id) {
        jsondata.sign_id = data.sign_id
      }
      this.queryEmployeeInfo(jsondata)
    },
    /** 获取员工信息 */
    queryEmployeeInfo (jsondata) {
      this.employeeInfo = {}
      this.companyInfo = {}
      this.departmentInfo = {}
      HTTPServer.getQueryEmployeeInfo(jsondata, '').then((res) => {
        this.employeeInfo = res.employeeInfo || {}
        this.companyInfo = res.companyInfo || {}
        this.departmentInfo = res.departmentInfo || {}
        this.employeeInfo.fabulous_count = res.fabulous_count
        this.employeeInfo.fabulous_status = res.fabulous_status
      })
    },
    changeSign (event) {
      console.log(this.employeeInfo.sign.length)
      console.log(this.employeeInfo.sign.trim().length)
      if (this.employeeInfo.sign) {
        if (this.employeeInfo.sign.trim().length > 12) {
          this.$message.error('签名长度不能超过12字符！')
          return
        }
      }
      var jsondata = {'data': {
        'sign': this.employeeInfo.sign.trim()
      }}
      console.log(jsondata)
      this.editEmployeeDetail(jsondata)
    },
    /** 修改个人中心信息 */
    editEmployeeDetail (jsondata) {
      HTTPServer.postEditEmployeeDetail(jsondata, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '修改成功!'
        })
        this.queryEmployeeInfo({'sign_id': this.employeeInfo.sign_id})
      })
    }
  }
}
</script>

<style lang='scss'>
@import './static/css/emoji.scss';
.avatar-mian {display: inline-block;
    .simpName {
        width: 36px;
        height: 36px;
        line-height: 36px;
        display: inline-block;
        font-size: 12px;
    }
    >img{width: 36px;height: 36px;border-radius: 50%;}
    .employeeForm{overflow: hidden;
        .el-dialog{width: 340px!important;background: #FFFFFF;box-shadow: 2px -2px 4px 0 rgba(155,155,155,0.30), -2px 2px 4px 0 rgba(155,155,155,0.30);border-radius: 5px;
        height: 144px!important;}
        .el-dialog__header{display: none;}
        .el-dialog__body{padding: 25px 20px 10px!important;height: auto;}
        .employee-data{overflow: hidden;border-bottom: 1px solid #E7E7E7;text-align: left;height: 76px;line-height: 24px;
            .simpName{width: 50px!important;height: 50px!important;float: left!important;border-radius: 50%!important;line-height: 50px!important;font-size: 16px!important;margin: 0 10px 0 0!important;}
            .name{font-size: 16px;color: #17171A;margin: 0!important;}
            img{width: 50px!important;height: 50px!important;float: left;border-radius: 50%;margin: 0 10px 0 0!important;}
            .iconfont{font-size: 14px;color: #17171A;margin-left: 10px;}
            .department{font-size: 14px;color: #69696C;}
            .postion{font-size: 14px;color: #69696C;}
            .praise{float: right;
                .iconfont{font-size: 20px;}
                .icon-pc-paper-fighti.active{color: #FF6260;}
                .icon-pc-paper-fighti{color: #f2f2f2;cursor: pointer;}
                .num{font-size: 12px;color: #69696C;float: right;margin: 3px 0 0 4px;line-height: 1;}
            }
        }
        .signature{
            .iconfont{float: left;margin: 10px 0 0 0;font-size: 20px;line-height: 1;}
            img{float: left;margin: 10px 0 0 0;width: 20px!important;height: 20px!important;}
            input{height: 24px;margin-top: 9px;background: #fff;width: calc(100% - 35px);margin: 10px 0 0 10px;border: none;float: left;}
            input:focus{box-shadow: none;border: none;}
        }
    }
}
</style>
