<template>
  <el-dialog :title="dialogTitle" width="540px" :visible.sync="visible" class="member-commit-wrip common-dialog">
    <div class="content">
      <p v-if="type === 1">将当前数据的负责人转移给其他负责人？转移成功后，该操作将无法恢复</p>
      <div class="form-people">
        <label for="people">{{memberLabel}}</label>
        <div class="people-list">
          <div class="add-people" @click="openPeople(peopleType,'memberCommit' + type)">
            <i class="el-icon-plus"></i>
          </div>
          <div class="people-item" v-for="(people,index) in peopleList" :key="index">
            <head-image :head="people" :size="36"></head-image>
          </div>
        </div>
      </div>
      <div class="form-item" v-if="type === 2 || type === 3">
        <label for="">访问权限</label>
        <el-select v-model="jurisdiction" placeholder="请选择">
          <el-option
            v-for="item in jurisdictions"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </div>
      <el-checkbox v-model="isTransferShare" v-if="type === 1">转移后共享给当前数据负责人</el-checkbox>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button type="primary" size="mini" @click="saveData">保 存</el-button>
      <el-button size="mini" @click="visible = false">取 消</el-button>
    </span>
  </el-dialog>
</template>

<script>
import HeadImage from '@/common/components/head-image'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'MemberCommit',
  components: {
    HeadImage
  },
  data () {
    return {
      bean: '',
      type: '',
      ids: '',
      visible: false,
      jurisdictions: [
        {
          value: '0',
          label: '只读'
        },
        {
          value: '1',
          label: '读写'
        },
        {
          value: '2',
          label: '读写删'
        }
      ],
      jurisdiction: '只读',
      peopleList: [],
      isTransferShare: false,
      isSingleShare: false,
      updataId: ''
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
  },
  methods: {
    // 打开选人控件
    openPeople (type, id) {
      let types = type === '0' ? 1 : ''
      let navKey = type === '0' ? '' : '1,0'
      // 给父组件传值
      this.$bus.emit('commonMember',
        { 'type': types,
          'prepareData': this.peopleList,
          'prepareKey': id,
          'seleteForm': true,
          'banData': [],
          'navKey': navKey,
          'removeData': [],
          'index': 0
        })
    },
    // 删除人员
    delPeople (index) {
      this.peopleList.splice(index, 1)
    },
    // 提交数据
    saveData () {
      let peopleText = []
      let peopleId = []
      this.peopleList.map((item, index) => {
        peopleText.push(item.name)
        peopleId.push(item.id)
      })
      let transfer = {
        bean: this.bean,
        id: this.ids,
        data: peopleId.toString(),
        share: this.isTransferShare ? 1 : 0
      }
      let share = {
        bean_name: this.bean,
        dataId: this.ids,
        basics: {
          access_permissions: this.jurisdiction === '只读' ? '0' : this.jurisdiction,
          allot_employee: this.peopleList,
          target_lable: peopleText.toString()
        }
      }
      let allot = {
        id: this.ids,
        bean: this.bean,
        employee_id: peopleId.toString()
      }
      if (this.type === 0) {
        if (allot.employee_id) {
          this.ajaxHighSeasPoolDistribute(allot)
        } else {
          this.$message({
            message: '负责人不能为空',
            duration: 1000,
            type: 'warning'
          })
          return
        }
      } else if (this.type === 1) {
        if (transfer.data) {
          this.ajaxTransferPrincipal(transfer)
        } else {
          this.$message({
            message: '负责人不能为空',
            duration: 1000,
            type: 'warning'
          })
          return
        }
      } else if (this.type === 2) {
        if (share.basics.allot_employee.length) {
          this.ajaxAddDataShare(share)
        } else {
          this.$message({
            message: '共享人不能为空',
            duration: 1000,
            type: 'warning'
          })
          return
        }
      } else if (this.type === 3) {
        share.id = this.updataId
        if (share.basics.allot_employee.length) {
          this.ajaxUpdataDataShare(share)
        } else {
          this.$message({
            message: '共享人不能为空',
            duration: 1000,
            type: 'warning'
          })
          return
        }
      }
      this.visible = false
    },
    // 公海池分配
    ajaxHighSeasPoolDistribute (data) {
      HTTPServer.highSeasPoolDistribute(data, 'Loading').then((res) => {
        this.$message.success('分配成功!')
        this.$bus.emit('refreshList')
        this.$bus.emit('setCheckEmpty')
      })
    },
    // 转移负责人
    ajaxTransferPrincipal (data) {
      HTTPServer.transferPrincipal(data, 'Loading').then((res) => {
        this.$message.success('保存成功!')
        this.$bus.emit('refreshList')
        this.$bus.emit('refreshDtl', true)
        this.$bus.emit('setCheckEmpty')
      })
    },
    // 新增数据共享
    ajaxAddDataShare (data) {
      HTTPServer.addDataShare(data, 'Loading').then((res) => {
        this.$message.success('保存成功!')
        if (this.isSingleShare) {
          this.$bus.emit('refreashShareList')
        }
        this.$bus.emit('setCheckEmpty')
      })
    },
    // 修改数据共享
    ajaxUpdataDataShare (data) {
      HTTPServer.updataDataShare(data, 'Loading').then((res) => {
        this.$message.success('保存成功!')
        if (this.isSingleShare) {
          this.$bus.emit('refreashShareList')
        }
        this.$bus.emit('setCheckEmpty')
      })
    }
  },
  mounted () {
    // 打开弹框
    this.$bus.off('openMemberCommit')
    this.$bus.on('openMemberCommit', value => {
      this.visible = true
      this.isTransferShare = false
      this.peopleList = []
      this.bean = value.bean
      this.type = value.type
      this.ids = value.ids
      this.jurisdiction = '只读'
      this.isSingleShare = value.isSingleShare
      if (value.share) {
        this.updataId = value.share.id
        this.peopleList = value.share.allot_employee
        this.jurisdiction = value.share.access_permissions
      }
    })
    // 获取人员多选数据
    // this.$bus.off('selectEmpDepRoleMulti')
    this.$bus.on('selectEmpDepRoleMulti', value => {
      if (value.prepareKey === 'memberCommit' + this.type) {
        this.peopleList = value.prepareData
      }
    })
    // 获取人员单选数据
    this.$bus.on('selectMemberRadio', value => {
      if (value.prepareKey === 'memberCommit' + this.type) {
        this.peopleList = value.prepareData
      }
    })
  },
  computed: {
    dialogTitle () {
      if (this.type === 0) {
        return '分配'
      } else if (this.type === 1) {
        return '转移负责人'
      } else if (this.type === 2 || this.type === 3) {
        return '数据共享'
      }
    },
    memberLabel () {
      if (this.type === 0) {
        return '分配给'
      } else if (this.type === 1) {
        return '转移给'
      } else if (this.type === 2 || this.type === 3) {
        return '共享给'
      }
    },
    peopleType () {
      if (this.type === 2 || this.type === 3) {
        return '1'
      } else {
        return '0'
      }
    }
  }
}
</script>

<style lang="scss">
@import '~@/../static/css/dialog.scss';
.member-commit-wrip{
  .content{
    >p{
      font-size: 14px;
      color: #17171A;
      margin: 0 0 20px 0;
      padding: 0 0 0 10px;
    }
    .el-checkbox{
      display: block;
      padding: 0 0 0 10px;
      margin: 20px 0 5px 0;
    }
  }
  .form-item{
    display: flex;
    height: 54px;
    line-height: 54px;
    label{
      flex: 0 0 70px;
      margin: 0 16px 0 10px;
      text-align: left;
    }
    .el-input,.el-select{
      flex: 1;
      margin: 0 25px 0 0;
      input{
        height: 35px;
        line-height: 35px;
      }
    }
  }
  .form-people{
    display: flex;
    label{
      flex: 0 0 70px;
      line-height: 36px;
      margin: 0 16px 0 10px;
      text-align: left;
    }
    .people-list{
      flex: 1;
      .add-people{
        display: inline-block;
        width: 36px;
        height: 36px;
        line-height: 42px;
        margin: 0 16px 0 0;
        text-align: center;
        border-radius: 50%;
        background: #ECEFF1;
        vertical-align: text-top;
        i{
          font-size: 20px;
          color:#ACB8C5;
          cursor: pointer;
        }
      }
      .people-item{
        display: inline-block;
        margin: 0 16px 0 0;
        vertical-align: text-top;
        img{
          width: 36px;
          height: 36px;
          border-radius: 50%;
          margin: 0 8px 0 0;
          vertical-align: middle;
        }
        .name-img{
          margin: 0 8px 0 0;
          vertical-align: middle;
        }
        span{
          position: relative;
          font-size: 16px;
          color: #17171A;
          vertical-align: middle;
          i{
            position: absolute;
            right: -10px;
            top: -5px;
            font-size: 12px;
            color: #F94C4A;
            cursor: pointer;
          }
        }
      }
    }
  }
}
</style>
