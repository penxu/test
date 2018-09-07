<template>
  <div class="out-links-wrip">
    <div class="ft-title">
      <span>数据外链</span>
      <span>将数据发布为独立的公开链接，无需登录即可访问数据。</span>
    </div>
    <div class="link-box">
      <div class="titles">
        <label>开启数据外链</label>
        <el-switch v-model="switchOut" active-color="#1890FF" inactive-color="#C0CCDA" @change="save"></el-switch>
      </div>
      <div class="contents" v-if="switchOut">
        <label>数据访问权限</label>
        <el-radio-group v-model="auth" @change="save">
          <el-radio :label="0">公开访问</el-radio>
          <el-radio :label="1">凭密码访问</el-radio>
        </el-radio-group>
        <el-input v-model="password" placeholder="设置密码" size="mini" v-if="auth === 1" @change="save"></el-input>
      </div>
      <div class="contents"  v-if="switchOut">
        <label>字段访问权限</label>
        <el-button type="primary" size="small" @click="visible = true">设置外链字段</el-button>
      </div>
    </div>
    <div class="ft-title">
      <span>数据内链</span>
      <span>开启数据内链，通过Teamface APP扫一扫功能可直接访问数据。</span>
    </div>
    <div class="link-box">
      <div class="titles">
        <label>开启数据内链</label>
        <el-switch v-model="switchIn" active-color="#1890FF" inactive-color="#C0CCDA" @change="save"></el-switch>
      </div>
    </div>
    <el-dialog width="540px" title="选择可访问字段" class="common-dialog" :visible.sync="visible">
      <div class="content">
        <p>可以将本模块哪些字段可以数据外链。</p>
        <div class="table-titles">
          <span>字段名称</span>
          <span>全选 <el-checkbox v-model="checkedAll" @change="checkAll"></el-checkbox></span>
        </div>
        <div class="table-list">
          <div class="item" v-for="(item, index) in fieldList" :key="index">
            <span>{{item.label}}</span>
            <span>可访问<el-checkbox v-model="item.selected"></el-checkbox></span>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button size="mini" @click="visible = false">取 消</el-button>
        <el-button size="mini" @click="save">保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'OutLink',
  data () {
    return {
      visible: false,
      switchOut: false,
      switchIn: false,
      checkedAll: false,
      auth: 0,
      password: '',
      fieldList: []
    }
  },
  created () {
    let data = {moduleBean: this.$route.query.bean}
    this.getOutLinksSettings(data)
  },
  methods: {
    // 全选
    checkAll (evt) {
      if (evt) {
        this.fieldList.map((item) => {
          item.selected = true
        })
      } else {
        this.fieldList.map((item) => {
          item.selected = false
        })
      }
    },
    // 保存设置
    save () {
      if (this.auth === 1 && this.password === '') {
        return
      }
      let data = {
        moduleBean: this.$route.query.bean,
        externalOnOff: this.switchOut,
        insideOnOff: this.switchIn,
        dataLinkAuth: {
          type: this.auth,
          pwd: this.password
        },
        externalLinkFields: this.fieldList
      }
      console.log(data)
      this.visible = false
      this.saveOutLinksSettings(data)
    },
    // 获取数据外链信息
    getOutLinksSettings (data) {
      HTTPServer.getOutLinksSettings(data, 'Loading').then((res) => {
        this.switchOut = res.externalOnOff
        this.switchIn = res.insideOnOff
        this.auth = res.dataLinkAuth.type
        this.password = res.dataLinkAuth.pwd
        this.fieldList = res.externalLinkFields
      })
    },
    // 保存数据外链
    saveOutLinksSettings (data) {
      HTTPServer.saveOutLinksSettings(data, 'Loading').then((res) => {
        this.$message({
          message: '保存成功!',
          type: 'success',
          duration: 1500
        })
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.out-links-wrip{
  min-width: 600px;
  .ft-title{
    height: 60px;
    line-height: 60px;
    border-bottom: 1px solid #E7E7E7;
    span{
      &:first-child{
        font-size: 18px;
        color: #17171A;
      }
      &:last-child{
        color: #BBBBC3;
      }
    }
  }
  .link-box{
    margin: 20px 0 70px 0;
    .titles{
      align-items: center;
      >label{
        display: inline-block;
        width: 125px;
        font-size: 16px;
        color: #333333;
      }
    }
    .contents{
      line-height: 30px;
      align-items: center;
      margin: 20px 0 0;
      >label{
        display: inline-block;
        width: 125px;
        font-size: 14px;
        color: #333333;
      }
      >div{
        display: inline-block;
      }
      >.el-input{
        width: 240px;
        margin: 0 0 0 20px;
      }
    }
  }
  .common-dialog{
    .content{
      >p{
        color: #333333;
        margin: 0 0 10px;
      }
      >.table-titles{
        display: flex;
        align-items: center;
        height: 50px;
        line-height: 50px;
        padding: 0 10px;
        border-bottom: 1px solid #E7E7E7;
        span{
          flex: 0 0 120px;
          height: 16px;
          line-height: 16px;
          padding: 0 10px;
          color: #212121;
          border-left: 1px solid #E7E7E7;
          &:first-child{
            flex: 1;
          }
          >.el-checkbox{
            margin: 0 0 0 20px;
          }
        }
      }
      >.table-list{
        height: 300px;
        overflow: auto;
        .item{
          display: flex;
          align-items: center;
          height: 50px;
          line-height: 50px;
          padding: 0 10px;
          border-bottom: 1px solid #E7E7E7;
        }
        span{
          flex: 0 0 120px;
          line-height: 16px;
          padding: 0 10px;
          color: #212121;
          &:first-child{
            flex: 1;
          }
          >.el-checkbox{
            margin: 0 0 0 5px;
          }
        }
      }
    }
  }
}
</style>
