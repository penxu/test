<template>
  <div class="content">
    <span class="changePassword">修改密码</span>
    <ul>
      <li><div class="particulars">修改密码时需要输入当前密码，如果你忘记了当前密码，可以联系管理员找回你的密码。</div></li>
      <li><span>旧密码</span><input type="password" v-model="form.oldPassword"></li>
      <li><span>新密码</span><input type="password" v-model="form.newPassword"></li>
      <li><span>确认密码</span><input type="password" v-model="form.confirmPassword"></li>
    </ul>
    <el-button @click="preserve" class="block">保存</el-button>
  </div>
</template>
<script>
import {ajaxPostRequest, ajaxGetRequest} from '@/common/js/ajax.js'
import {regular, TFParameter} from '@/common/js/constant.js'
import md5 from 'md5'
export default {
  data () {
    return {
      form: {},
      TEAMFACEPWD: 'hjhq2017Teamface',
      dataPermission: [],
      dataId: []
    }
  },
  mounted () {
    const loading = this.$loading({
      lock: true,
      text: 'Loading',
      spinner: 'el-icon-loading',
      background: 'rgba(0, 0, 0, 0.5)'
    })
    var jsondata = {'moduleId': 9}
    ajaxGetRequest(jsondata, '/centerRole/queryFchBtnAuth')
      .then((response) => {
        this.dataPermission = response.data.data
        for (var i = 0; i < this.dataPermission.length; i++) {
          this.dataId[i] = this.dataPermission[i].auth_code
        }
        console.log('获取员工模块功能权限', response)
        loading.close()
      })
      .catch((err) => {
        console.log(err)
        loading.close()
      })
  },
  methods: {
    /** 修改密码 */
    preserve () {
      console.log(this.dataId)
      if (this.dataId.indexOf(24) < 0) {
        this.$message.error('您没有修改密码的权限')
        return
      }
      if (!this.form.oldPassword) {
        this.$message.error('旧密码不能为空')
        return
      }
      if (!this.form.newPassword) {
        this.$message.error('新密码不能为空')
        return
      }
      if (!this.form.confirmPassword) {
        this.$message.error('确认密码不能为空')
        return
      }
      if (!regular.pwdRegx.test(this.form.newPassword)) {
        this.$message.error('密码必须由6-16个英文字母和数字的字符串组成')
        return
      }
      if (this.form.newPassword !== this.form.confirmPassword) {
        this.$message.error('两次输入的密码不相同')
        return
      }
      var passWord = md5(md5(this.form.confirmPassword + TFParameter.TEAMFACEPWD))
      var jsondata = {'passWord': passWord, 'pwd': md5(md5(this.form.oldPassword + TFParameter.TEAMFACEPWD))}
      ajaxPostRequest(jsondata, '/centerUser/editPwd')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.$message({
              message: '修改密码成功',
              type: 'success'
            })
            this.form = {}
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    }
  }
}
</script>
<style lang="scss" scoped>
.content{
  padding-bottom:30px;
  border-bottom:1px solid #E7E7E7;
  .changePassword{
    border-bottom:1px solid #E7E7E7;
    font-size: 18px;
    line-height: 60px;
    display: block;
    padding: 0 15px;
  }
  ul{
    margin-top: 30px;
    li{
      height: 50px;
      span{
        line-height: 50px;
        width: 80px;
        display: inline-block;
      }
      input{
        border: 1px solid #ccc;
        border-radius: 3px;
        height: 40px;
        width:500px;
        padding-left: 20px;
      }
      .particulars{
        font-size: 16px;
        color: #BBBBC3;
      }
    }
  }
  .block{
    background-color: #262626;
    color: #fff;
    width: 100px;
    margin-left: 80px;
    margin-top: 30px;
  }
}
</style>
