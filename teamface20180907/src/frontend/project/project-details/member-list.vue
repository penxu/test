<template>
  <el-container class="main-member-list-wrap">
    <el-header class="topHeader" style="height:50px;">
      <div @click="closeAside"><i class="el-icon-close"></i></div>
      <span>成员（18）</span>
    </el-header>
    <el-main>
      <div class="allUserList">
        <div @click="openUserList" class="member"><span><i class="iconfont icon-jiaren"></i></span><span>邀请成员</span></div>
        <div v-for="(v, k) in showUserList" :key="k" class="userListSty">
          <span class="userlistSub" :style="!v.employee_pic?'background:'+v.bgColor:''">
            <img v-if="v.employee_pic" :src="v.employee_pic+ '&TOKEN=' + token" alt="" style="width:100%;">
            <span v-else v-text="uditorName(v.employee_name)" style="font-size:12px;"></span>
          </span>
          <span v-text="v.employee_name"></span>
          <!-- 项目管理员显示 -->
          <span v-if="v.project_role==0" style="margin-left:5px;">
            <el-tooltip class="item" effect="dark" content="项目负责人" placement="top">
              <i class="iconfont icon-jiaosequanxian1" style="color:#1DBB96;"></i>
            </el-tooltip>
          </span>
          <!-- 公司人员职位 -->
          <span class="proUserRole" v-if="v.duty_name"> (<span v-text="v.duty_name">职位</span>)</span>
          <!-- 激活未激活 -->
          <div v-if="v.active_status==0" class="activeStatus"><span></span></div>
          <!-- 设置按钮 -->
          <i class="iconfont icon-gerenshezhi showMore" @mouseleave="changeNone">
            <div class="delUseror">
              <div>
                <span  class="projectType" @mouseleave="changeProStatus(v)">
                  <span @click="openjiaose($event,0,v)" :class="proIconColor===0?'changeColor':''">项目角色<i class="iconfont" :class="proIconColor===0&&proIconStatus===1?'icon-nav-out-module':'icon-nav-on-module'"></i></span>
                  <div class="jiaose">
                    <!-- 0负责人 1执行人2协作人3访客4外部 -->
                    <div @click.stop="UpdateProRole(v, 0)">项目负责人</div>
                    <div @click.stop="UpdateProRole(v, 1)">项目执行人</div>
                    <div @click.stop="UpdateProRole(v, 2)">项目协作人</div>
                    <div @click.stop="UpdateProRole(v, 3)">访客</div>
                    <div @click.stop="UpdateProRole(v, 4)">外部人员</div>
                  </div>
                </span>
              </div>
              <div :class="proIconColor===1?'changeColor':''" @click="openjiaose($event,1,v,'del')">移除成员</div>
            </div>
          </i>
        </div>
      </div>
      <div class="bottomNotActive">
        <div><span></span></div>
        <div>未激活人员</div>
      </div>
    </el-main>
  </el-container>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'MemberList',
  data () {
    return {
      data: {},
      proRole: ['负责人', '执行人', '协作人', '访客', '外部'], // 项目角色 0负责人 1执行人2协作人3访客4外部
      proTaskRole: ['负责人', '执行人', '协作人', '访客', '外部'], // 项目任务角色 0负责人 1执行人2协作人3访客4外部
      proIconColor: null, // 项目或删除状态改变
      proIconStatus: 0,
      roleIconColor: null, // 角色状态改变
      proRoleListData: [], // 点击获取并存放项目的角色
      userList: [], // 获取选人模块中的人员
      showUserList: [], // 显示列表中的人员
      projectId: '',
      token: JSON.parse(sessionStorage.requestHeader).TOKEN
    }
  },
  mounted () {
    this.projectId = this.$route.query.projectId
    this.getMemberList(this.projectId)
    this.$bus.on('changeProjectId', (projectId) => {
      this.projectId = projectId
      this.getMemberList(projectId)
    })
    this.$bus.on('selectEmpDepRoleMulti', (data) => {
      if (data.prepareKey === 'memberList') {
        this.userList = JSON.parse(JSON.stringify(data.prepareData))
        this.addUserList(data.prepareData)
      }
    })
  },
  methods: {
    getMemberList (id) {
      HTTPServer.MemberQueryList({'id': id}, 'Loading').then((res) => {
        console.log(res)
        res.dataList.forEach((v, k) => {
          v.bgColor = this.randomColor()
        })
        this.showUserList = res.dataList
      })
    },
    addUserList (list) {
      let arr = []
      list.forEach((v, k) => {
        arr.push(v.id)
      })
      let senddata = {
        projectId: this.projectId,
        employeeIds: arr.join(',')
      }
      HTTPServer.MemberSave(senddata, 'Loading').then((res) => {
        console.log(res)
        this.getMemberList(this.projectId)
      })
    },
    closeAside () { // 关闭按钮
      this.$bus.$emit('MemberAside', 'close')
    },
    openUserList () {
      // hecked:true
      // id:1
      // name:"苏苏苏"
      // picture:"http://192.168.1.9:8080/custom-gateway/common/file/imageDownload?bean=company&fileName=2/company/1522207110061.新闻详情3-03@2x.png"
      // sign_id:10001
      // type:1
      // value:"1-1"
      let arr = []
      this.showUserList.forEach((v, k) => {
        arr.push({id: v.employee_id, picture: v.employee_pic, name: v.employee_name})
      })
      this.userList = arr
      let senddata = {
        type: 3, 'prepareData': this.userList, 'prepareKey': 'memberList', 'seleteForm': true, 'banData': [], 'navKey': '1,0', 'removeData': [], invitationOutMember: true
      }
      this.$bus.emit('commonMember', senddata)
    },
    uditorName (str) {
      return str.slice(-2)
    },
    randomColor () { // 随机改变颜色
      return '#' + Math.floor(Math.random() * 16777215).toString(16)
    },
    openjiaose (e, status, v, str) {
      this.proIconColor = status
      if (str && str === 'del') {
        this.$confirm('移除成员：移除成员后，该成员在项目内的数据不进行移除且成员对项目内负责的数据不在允许其进行操作，只能在各业务模块中进行查看', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          HTTPServer.MemberDelete({id: v.id}, 'Loading').then((res) => {
            this.getMemberList(this.projectId)
          })
        }).catch(() => {})
      } else {
        let ele = e.target.nextSibling.nextElementSibling
        ele.style.display = ele.style.display === 'block' ? 'none' : 'block'
        if (ele.style.display === 'block') {
          this.proIconStatus = 1
        } else { this.proIconStatus = 0 }
      }
    },
    UpdateProRole (v, status) {
      let senddata = {
        id: v.id, // 记录id
        projectRole: status
      }
      HTTPServer.MemberUpdate(senddata, 'Loading').then((res) => {
        this.getMemberList(this.projectId)
      })
    },
    changeNone () {
      this.proIconColor = null
      let ele = document.querySelectorAll('.jiaose')
      ele.forEach((v, k) => {
        v.style.display = 'none'
      })
    },
    changeProStatus (v) {
      this.proIconStatus = 0
      this.proIconColor = null
      let ele = document.querySelectorAll('.jiaose')
      ele.forEach((v, k) => {
        v.style.display = 'none'
      })
      console.log(11111111111)
    }
  },
  beforeDestroy () {
    // this.$bus.off('selectEmpDepRoleMulti')
    this.$bus.off('changeProjectId')
  }
}
</script>
<style lang="scss" scoped>
.main-member-list-wrap{
  height: 100%;
  .topHeader{
    overflow: hidden; line-height: 50px;border-bottom: 1px solid #ddd;width: 100%;
    >span{
      font-size: 18px;
    }
    >div{
      float:right;cursor:pointer;
    }
  }
  .el-main{
    position: relative;
    padding: 0;
    .allUserList{height: 82vh;overflow: auto;}
    .member{
      padding: 10px 20px;
      overflow: hidden;
      &:hover{cursor: pointer;background: #F2F2F2;}
      >span:nth-child(1){
        display: inline-block;
        height: 30px;
        width: 30px;
        line-height: 30px;
        text-align: center;
        border: 1px solid #ccc;
        margin-right: 10px;
        border-radius: 50%;
        i{color:#ccc;}
      }
      >span:nth-child(2){
        color: #ccc;
      }
    }
    .projectType{
      position:relative;
      >span{
        position: relative;display: inline-block;width:100%;height:100%;&:hover{color:#3195F0;}padding:0 10px;
        i{position: absolute;top:0;right:10px;}
        i.icon-nav-out-module{color:#3195F0;}
      }
    }
    .changeColor{color:#3195F0;}
    .userListSty{
      padding: 10px 20px;line-height: 50px;height: 50px;position: relative;
      >span{float:left;overflow: hidden;}
      >span.proUserRole{line-height: 30px;color:#ccc;font-size: 12px;}
      &:hover{cursor: pointer;background: #F2F2F2;}
      .delUseror{
        display: none;
        position: absolute;
        text-align: center;
        bottom: -100px;
        right: 0px;
        height: 100px;
        width: 120px;
        box-shadow: 0 0 5px #ccc;
        border-radius: 5px;
        background: #fff;
        z-index: 10000;
        >div{
          &:hover{cursor: pointer;}
          .jiaose{
            display: none;
            position: absolute;
            top: 30px;
            left: -30px;
            width: 120px;
            box-shadow: 0 0 5px #ccc;
            border-radius: 5px;
            background: #fff;
            >div:hover{background: #F2F2F2;color:#3195F0;}
          }
        }
        >div:last-child:hover{color:#3195F0;}
      }
      i.showMore{
        position: absolute;
        top: 0;
        right: 10px;
        height: 40px;
        &:hover{
          cursor: pointer;
          .delUseror{display: block;}
        }
      }
      >span.userlistSub{
        float: left;
        height: 30px;
        line-height: 30px;
        text-align: center;
        color: #fff;
        width: 30px;
        margin-right: 10px;
        border-radius: 50%;
        >img{border-radius: 50%;width:100%;height:100%;}
        >span{color:#fff;background: #8080FF;display: inline-block;width:30px;height:30px;border-radius: 50%;}
      }
      >span:nth-child(2),>span:nth-child(3){
        float: left;
        height: 30px;
        line-height: 30px;
      }
      >span:nth-child(3){
        margin-left: 20px;
        color: #ccc;
      }
      .activeStatus{
        float:left;height:30px;line-height:30px;width:30px;text-align: center;
        span{display: inline-block;height:8px;width:8px;border-radius: 50%;background: #F31C0D;}
      }
    }
    .bottomNotActive{
      height:50px;border-top:1px solid #ddd;line-height: 50px;position: absolute;bottom:2px;left:0;width:100%;
      div{float: left;}
      >div:first-child{
        height:50px;width:50px;text-align: center;line-height: 50px;
        >span{display: inline-block;width:8px;height:8px;background: #F31C0D;border-radius: 50%;}
      }
    }
  }
}
</style>
