 <!-- 头像组件 -->
<template>
  <div class="employee-card-mian">
    <div class="isEnableActive" v-if="employeeData.is_enable==='0'">
      <span></span>
      <span>未激活</span>
    </div>
    <div class="firstchildDiv">
      <div class="personPic">
        <div class="topPicAddName">
          <div class="peoplePic">
            <p class="userGender" v-if="employeeData.sex">
              <i class="iconfont icon-xingbie1" v-if="employeeData.sex==='1'"></i>
              <i class="iconfont icon-xingbie" v-else></i>
            </p>
            <p class="userName" v-show="!employeeData.picture||employeeData.picture=='null'">{{employeeData.employee_name | limitTo(-2)}}</p>
            <img v-show="employeeData.picture&&employeeData.picture!='null'" :src="employeeData.picture + '&TOKEN=' + token" alt="">
          </div>
          <div style="font-size: 16px;margin: 6px 0px 5px;line-height: 21px;">{{employeeData.employee_name}}</div>
          <div style="color:#A0A0AE;">{{employeeData.post_name}}</div>
        </div>
        <div>
          <el-button type="primary" @click="openEmailOrMemo(0)" :disabled="curEmployeeInfo.sign_id == employeeData.sign_id">
            <i class="iconfont icon-pinglun"></i>
          </el-button>
          <el-button type="primary" @click="openEmailOrMemo(1)" :disabled="!employeeData.email"><i class="iconfont icon-youjian2"></i></el-button>
        </div>
      </div>
      <div class="depInfo">
        <p><span>部门 : </span><span v-text="initdepName(departmentData)"></span></p>
        <p><span>上级 : </span><span>{{employeeData.superior}}</span></p>
        <p><span>座机 : </span><span>{{employeeData.mobile_phone}}</span></p>
        <p><span>手机 : </span><span>{{employeeData.phone}}</span></p>
        <p><span>邮箱 : </span><span>{{employeeData.email}}</span></p>
      </div>
    </div>
  </div>
</template>

<script>
import chatjs from '@/common/js/chat.2.js'
export default {
  props: ['companyData', 'departmentData', 'employeeData'],
  components: {},
  data () {
    return {
      companyInfo: this.companyData,
      departmentInfo: this.departmentData || [],
      employeeInfo: this.employeeData,
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      curEmployeeInfo: JSON.parse(sessionStorage.userInfo).employeeInfo
    }
  },
  watch: {
  },
  /* 页面加载后执行 */
  mounted () {
  },
  methods: {
    openEmailOrMemo (type) {
      var name = this.$route.name
      if (type === 0) {
        if (name === 'CpChat') {
          chatjs.addSingleChat(this.employeeInfo, 'obj')
        } else {
          this.$router.push({ path: '/frontend/cpChat', 'params': {sign_id: this.employeeInfo.sign_id} })
        }
      } else {
        this.$router.push({path: '/frontend/EmailMain/EmailEpistolize', 'query': {judgeData: 'sendEmail', nickname: this.employeeInfo.email_name, account: this.employeeInfo.email}})
      }
      this.$bus.emit('employee-card-close', true)
    },
    /** 解析部门名称 */
    initdepName (arr) {
      var name = ''
      if (arr) {
        if (arr[0]) {
          name = arr[0].department_name
        }
      }
      return name
    }
  }
}
</script>

<style lang='scss'>
.employee-card-mian{
  height:192px;width:366px;background: #FFF;
  >div.firstchildDiv{
    padding:24px 0;display: flex;padding-bottom:10px;
    .topPicAddName{min-height:103px;img{height:48px;width:48px;}}
    .personPic{
      width:128px;text-align: center;border-right:1px solid #ddd;height:144px;
      >div:first-child{
        .peoplePic{
          height:48px;width:48px;border-radius: 50%;text-align:center;line-height:48px;display:inline-block;position: relative;
          p.userName{color:#fff;background: #37AEFF;border-radius: 50%;}
          img{border-radius: 50%;}
          p.userGender{
            position: absolute;bottom:0;right:0;height:15px;width:15px;border:1px solid #fff;border-radius: 50%;background:#8DB7F7;text-align:center;line-height:15px;
            i{
              font-size:12px;color:#fff;position: absolute;top:0;left:0;transform: scale(.8);
            }
          }
        }
      }
      >div:last-child{
        display: flex;text-align:center;margin-top:15px;padding-left:20px;
        .el-button{
          padding: 3px 11px;
          .iconfont{
            color: #fff;
          }
        }
        >div:last-child{i{font-size:18px;}}
      }
    }
    .depInfo{
      width:240px;padding:0 10px;
      >p{margin-bottom:12px;
        line-height: 19px;
        text-align: left;
        span:first-child{color:#A0A0AE;}
      }
    }
  }
  >div.secondchildDiv{
    height:40px;line-height: 40px;padding:0 20px;background: #F7F9FA;
    span{cursor: pointer;}
    .delPerer{float:right;color:#FA5555;}
    .editorPer{
      color:#888;
    }
  }
  >div.isEnableActive{
    position: absolute;top:0;width:80px;right:0;display: flex;height:30px;line-height:30px;margin-top:5px;
    >span:first-child{width:10px;height:10px;border-radius: 50%;background: #FAAD14;margin:10px 10px 0 0;}
    >span:last-child{color: rgba(0,0,0,0.45);}
  }
}
</style>
