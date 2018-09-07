<template>
  <el-dialog title="子菜单" :visible.sync="visible" width="700px"  class="module-menu-wrip common-dialog">
    <div class="content">
      <div class="form-item">
        <label for="name">名称</label>
        <el-input v-model="name" placeholder="请输入内容" id="name"></el-input>
      </div>
      <div class="form-condition">
        <label for="condition">筛选条件</label>
        <condition :allCondition="initFieldList" :selCondition="conditionData" :highWhere="highWhere" ref="conditionComponent" v-if="visible"></condition>
      </div>
      <div class="form-people">
        <label for="people">可见成员</label>
          <div class="people-list">
            <div class="add-people" @click="openPeople('1','childMenu')">
              <i class="el-icon-plus"></i>
            </div>
            <div class="people-item" v-for="(people,index) in peopleList" :key="index">
              <head-image :head="people" :size="36"></head-image>
            </div>
          </div>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button size="mini" @click="submitMenu">发 布</el-button>
      <el-button size="mini" @click="visible = false">取 消</el-button>
    </span>
  </el-dialog>
</template>

<script>
import Condition from '@/common/components/condition'
import HeadImage from '@/common/components/head-image'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'ModuleMunu',
  components: {
    Condition,
    HeadImage
  },
  data () {
    return {
      visible: false,
      bean: '',
      moduleId: '',
      menuId: '',
      type: 0,
      name: '',
      peopleList: [],
      initFieldList: [],
      conditionData: [
        {
          'field_label': '',
          'field_value': '',
          'operator_label': '',
          'operator_value': '',
          'result_label': '',
          'result_value': '',
          'show_type': '',
          'operators': [],
          'entrys': [],
          'selList': [],
          'selTime': []
        }
      ],
      highWhere: ''
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
    // 新建或编辑菜单
    submitMenu () {
      if (!this.name) {
        this.$message.error('子菜单名称必填!')
        return
      }
      let conditionCorrect = this.$refs.conditionComponent.judgeFilter()
      if (!conditionCorrect) {
        return
      }
      let conditionData = this.$refs.conditionComponent.handleLastData()
      let highWhere = this.$refs.conditionComponent.high_where
      let data = {
        moduleId: this.moduleId,
        basics: {
          name: this.name,
          high_where: highWhere,
          allot_employee: this.peopleList
        },
        relevanceWhere: conditionData
      }
      if (this.type === 0) {
        HTTPServer.addSubMenu(data, 'Loading').then((res) => {
          this.$message.success('保存成功!')
          let refresh = {
            bean: this.bean,
            menuId: res.menu_id,
            filter: {}
          }
          if (this.bean === this.$route.query.bean) {
            // 在当前界面添加子菜单
            this.$bus.emit('refreshMenuList', res)
            this.$bus.emit('refreshList', refresh)
          } else {
            // 不用刷新列表和当前子菜单active
            this.$bus.emit('refreshMenuList', res, true)
          }
          this.visible = false
        })
      } else {
        data.basics.id = this.menuId
        HTTPServer.updateSubMenu(data, 'Loading').then((res) => {
          this.$message.success('保存成功!')
          let refresh = {
            bean: this.bean,
            menuId: this.menuId,
            filter: {}
          }
          let modules = {
            menu_id: this.menuId,
            menu_name: this.name
          }
          this.$bus.emit('refreshMenuList', modules)
          this.$bus.emit('refreshList', refresh)
          this.visible = false
        })
      }
    },
    // 初始化字段列表
    ajaxInitCondition (bean) {
      HTTPServer.getInitCondition(bean).then((res) => {
        this.initFieldList = res
      })
    }
  },
  mounted () {
    // 打开子菜单
    this.$bus.off('openModuleMunu')
    this.$bus.on('openModuleMunu', value => {
      this.type = value.type
      this.bean = value.bean
      this.moduleId = value.moduleId
      this.ajaxInitCondition({bean: value.bean})
      if (value.type === 1) {
        // 编辑
        this.menuId = value.menuId
        this.name = value.name
        this.conditionData = value.conditions
        this.highWhere = value.highWhere
        this.peopleList = value.peopleList
      } else {
        this.name = ''
        this.peopleList = []
        this.conditionData = [
          {
            'field_label': '',
            'field_value': '',
            'operator_label': '',
            'operator_value': '',
            'result_label': '',
            'result_value': '',
            'show_type': '',
            'operators': [],
            'entrys': [],
            'selList': [],
            'selTime': []
          }
        ]
      }
      this.visible = true
    })
    // 获取人员多选数据
    this.$bus.on('selectEmpDepRoleMulti', value => {
      if (value.prepareKey === 'childMenu') {
        this.peopleList = value.prepareData
      }
    })
  },
  computed: {
  }
}
</script>

<style lang="scss">
@import '~@/../static/css/dialog.scss';
.module-menu-wrip{
  .content{
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
    .form-condition{
      display: flex;
      label{
        flex: 0 0 70px;
        line-height: 45px;
        margin: 0 16px 0 10px;
        text-align: left;
      }
      .condition-box{
        flex: 1;
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
        }
      }
    }
  }
}
</style>
