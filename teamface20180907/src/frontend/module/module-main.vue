<template>
  <el-container class="module-main-wrap">
    <module-nav :list="moduleList"></module-nav>
    <el-container>
      <el-header>
        <div class="list-top">
          <span class="title" v-if="checkedList.length === 0">{{menuName}}</span>
          <div class="control-list" v-if="checkedList.length !== 0">
            <span>已选择<span>{{checkedList.length}}</span>条记录</span>
            <a @click="openEdit()" v-if="checkedList.length === 1" v-show="getAuth(3) && highSeaId === ''"><i class="iconfont icon-liebiao-bianji"></i>编辑</a>
            <a @click="openEdit()" v-if="checkedList.length === 1" v-show="highSeasShow"><i class="iconfont icon-liebiao-bianji"></i>编辑</a>
            <a @click="openMemberCommit(1)" v-if="getAuth(7) && highSeaId === ''"><i class="iconfont icon-liebiao-zhuanyi"></i>转移</a>
            <a @click="getCustomer" v-if="getCustomerShow"><i class="iconfont icon-lingqu"></i>领取</a>
            <a @click="openMemberCommit(0)" v-if="highSeasShow"><i class="iconfont icon-fenpei"></i>分配</a>
            <a @click="openHighSeas(1)" v-if="highSeasShow"><i class="iconfont icon-yidong"></i>移动</a>
            <a @click="deleteData(1)" v-if="getAuth(5) && highSeaId === ''"><i class="iconfont icon-pc-delete"></i>删除</a>
            <a @click="deleteData(1)" v-if="highSeasShow"><i class="iconfont icon-pc-delete"></i>删除</a>
            <a @click="openMemberCommit(2)" v-show="getAuth(4) && highSeaId === ''"><i class="iconfont icon-liebiao-gongxiang"></i>共享</a>
            <a @click="openHighSeas(0)" v-if="!getCustomerShow && haveHighSeas"><i class="iconfont icon-tuihui"></i>退回公海池</a>
          </div>
          <div class="button-list">
            <transition name="slide-fade">
              <div class="filter-box" v-if="showFilter">
                <el-input placeholder="请输入关键字搜索" prefix-icon="el-icon-search" size="medium " v-model="filterContent" @keyup.enter.native="globalSearch" clearable></el-input>
                <a @click="globalSearch">搜索</a>
                <a @click="openFilter">高级搜索</a>
                <a @click="showFilter = false"><i class="el-icon-close"></i></a>
              </div>
            </transition>
            <i class="iconfont icon-guanlianxiangmu-icon" title="筛选" @click="showFilter = true" v-if="!showFilter"></i>
            <i class="iconfont icon-daoru" title="导入" v-if="getAuth(1)" @click="openImportData"></i>
            <i class="iconfont icon-daochu" title="导出" v-if="getAuth(2)" @click="exportFile"></i>
            <el-button type="primary" size="mini" @click="createDataNew" v-if="getAuth(1) && highSeaId === ''">新增</el-button>
            <el-button type="primary" size="mini" @click="createDataNew" v-if="highSeasShow">新增</el-button>
          </div>
        </div>
      </el-header>
      <el-main class="module-list-main" v-if="moduleLoadFinish">
        <module-list :bean="bean" :fieldName="false" :showSort="editViewFieldsFlag"></module-list>
      </el-main>
    </el-container>
    <div class="shade" v-if="openCreateModal || detailList.length > 0"></div>
    <transition name="slide">
      <module-create-new v-if="openCreateModal" :modules="modules" :dtlData="dtlData"></module-create-new>
    </transition>
    <transition-group name="slide" tag="div">
      <module-detail v-for="(data,index) in detailList" :key="index" :data="data" :dtlKey="index"></module-detail>
    </transition-group>
  </el-container>
</template>
<script>
import draggable from 'vuedraggable'
import ModuleNav from './module-nav'
import ModuleList from './module-list'
import ModuleCreateNew from '@/frontend/module/module-create-new'// 新增界面
import ModuleDetail from '@/frontend/module/module-detail'// 详情
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'ModuleMain',
  components: {
    draggable,
    ModuleNav,
    ModuleList,
    ModuleDetail,
    ModuleCreateNew
  },
  data () {
    return {
      bean: '',
      moduleId: '',
      moduleName: '',
      menuName: '',
      moduleLoadFinish: false,
      moduleList: [],
      token: '',
      employee: {},
      checkedList: [],
      dataId: '',
      authList: [],
      highSeaId: '',
      highSeasAmdin: '',
      haveHighSeas: false,
      data: {},
      detailList: [],
      openCreateModal: false,
      modules: {},
      dtlData: {},
      editViewFieldsFlag: '',
      filterContent: '',
      showFilter: false
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.employee = JSON.parse(sessionStorage.getItem('userInfo')).employeeInfo
    if (this.$route.params.addForm) {
      // 快速新增
      let myApp = {application_id: this.$route.query.appId}
      let quick = this.$route.query.muduleId
      this.ajaxGetAllMenuOfApp(myApp, quick)
    } else {
      let myApp = {application_id: this.$route.query.appId}
      this.ajaxGetAllMenuOfApp(myApp)
    }
    // 接收子菜单名称
    this.$bus.on('setMenuName', (value) => {
      this.menuName = value.name
      if (value.editViewFieldsFlag !== undefined) {
        this.editViewFieldsFlag = value.editViewFieldsFlag
      }
      if (value.is_seas_pool === '1') {
        this.highSeaId = value.id
        this.highSeasAmdin = value.is_seas_admin
      } else {
        this.highSeaId = ''
        this.highSeasAmdin = ''
      }
      console.log(this.menuName)
      let auth = {moduleId: this.$route.query.moduleId, bean: this.$route.query.bean}
      this.ajaxGetAuthList(auth)
      this.showFilter = false
      this.filterContent = ''
    })
    // 接收公海池判断
    this.$bus.on('haveSeas', (value) => {
      this.haveHighSeas = value
    })
  },
  methods: {
    // 判断权限码
    getAuth (code) {
      let boo = JSON.stringify(this.authList).includes('"auth_code":' + code)
      return boo
    },
    // 打开新增窗口
    createDataNew () {
      this.openCreateModal = true
      this.modules = {bean: this.$route.query.bean, moduleName: this.$route.query.moduleName, type: 2, highSeaId: this.highSeaId}
    },
    // 打开编辑窗口
    openEdit () {
      let id = {
        id: this.checkedList.toString(),
        bean: this.$route.query.bean
      }
      this.ajaxGetDataDtl(id)
    },
    // 打开筛选窗口
    openFilter () {
      this.$bus.emit('openFilterModal', this.$route.query.bean, this.$route.query.menuId)
    },
    // 打开公海池
    openHighSeas (type) {
      let bean = {bean: this.$route.query.bean}
      HTTPServer.getHighSeasList(bean, 'Loading').then((res) => {
        let value = {bean: this.$route.query.bean, type: type, ids: this.checkedList.toString(), poolList: res}
        if (type === 0 && res.length === 1) {
          // 若公海池只有一个，直接退回
          let data = {id: this.checkedList.toString(), bean: this.$route.query.bean, seas_pool_id: res[0].id}
          HTTPServer.highSeasPoolBack(data, 'Loading').then((res) => {
            this.$message.success('已退回公海池!')
            this.$bus.emit('refreshList')
            this.$bus.emit('setCheckEmpty')
          })
        } else {
          this.$bus.emit('openHighSeas', value)
        }
      })
    },
    // 打开提交人员弹框
    openMemberCommit (type) {
      let value = {bean: this.$route.query.bean, type: type, ids: this.checkedList.toString()}
      this.$bus.emit('openMemberCommit', value)
    },
    // 领取客户到公海池
    getCustomer () {
      let data = {bean: this.$route.query.bean, id: this.checkedList.toString(), seas_pool_id: this.highSeaId}
      HTTPServer.highSeasPoolGet(data, 'Loading').then((res) => {
        this.$message.success('领取成功!')
        this.$bus.emit('refreshList')
        this.$bus.emit('setCheckEmpty')
      })
    },
    // 删除数据
    deleteData (type) {
      let ids = type === 0 ? String(this.dataId) : this.checkedList.toString()
      this.$alert('<p style="margin:15px 0 35px 0">删除成功之后，该操作将无法恢复。</p>确认要删除该数据吗？', '提示', {
        dangerouslyUseHTMLString: true,
        showCancelButton: true
      }).then(_ => {
        let data = {
          bean: this.$route.query.bean,
          ids: ids
        }
        this.ajaxDeleteData(data)
      }).catch(_ => {})
    },
    // 打开导入弹框
    openImportData () {
      let data = {bean: this.$route.query.bean, progressActive: 1, importButton: '下一步', finishStatus: 'success'}
      this.$bus.emit('openImportData', data)
    },
    // 导出数据
    exportFile () {
      if (this.checkedList.length) {
        let value = {name: this.$route.query.moduleName, bean: this.$route.query.bean}
        this.$bus.emit('exportFile', value)
      } else {
        // 导出全部数据
        let value = {name: this.$route.query.moduleName, bean: this.$route.query.bean, allData: true}
        this.$bus.emit('exportFile', value)
      }
    },
    // 全局搜索
    globalSearch () {
      let value = {fuzzyMatching: this.filterContent, menuId: this.$route.query.menuId}
      this.$bus.emit('refreshList', value)
    },
    // 获取应用下所有模块及子菜单
    ajaxGetAllMenuOfApp (data, quick) {
      HTTPServer.getAllMenuOfApp(data, 'Loading').then((res) => {
        this.moduleList = res
        if (res.length > 0) {
          let current = res[0]
          if (quick !== undefined) {
            res.map((item) => {
              // console.log(item.chinese_name, item.id, quick)
              if (item.id === quick) {
                current = item
              }
            })
          }
          current.submenu.defaultSubmenu[0].active = true
          this.editViewFieldsFlag = current.submenu.defaultSubmenu[0].editViewFieldsFlag
          let params = {
            appId: current.application_id,
            appName: current.application_name,
            bean: current.english_name,
            moduleId: current.id,
            menuId: current.submenu.defaultSubmenu[0].id,
            moduleName: current.chinese_name
          }
          this.$router.push({name: 'DataModule', query: params, params: {addForm: this.$route.params.addForm}})
          this.bean = current.english_name
          this.moduleId = current.id
          this.moduleName = current.chinese_name
          this.menuName = '全部' + current.chinese_name
          this.haveHighSeas = JSON.stringify(current.submenu.defaultSubmenu).includes('is_seas_pool')
          let auth = {moduleId: current.id, bean: current.english_name}
          this.ajaxGetAuthList(auth)
          this.moduleLoadFinish = true
          this.$nextTick(() => {
            if (this.$route.params.addForm) {
              this.createDataNew()
            }
          })
        }
      })
    },
    // 获取功能权限
    ajaxGetAuthList (data) {
      HTTPServer.getFunAuthList(data, 'Loading').then((res) => {
        this.authList = res
      })
    },
    // 菜单排序
    ajaxSortMenu (data) {
      HTTPServer.sortSubMenu(data, null).then((res) => {
        // this.$message.success('保存成功!')
      })
    },
    // 删除数据
    ajaxDeleteData (data) {
      HTTPServer.customDeleteData(data, 'Loading').then((res) => {
        this.$message.success('删除成功!')
        this.$bus.emit('refreshList')
        this.$bus.emit('setCheckEmpty')
        this.$bus.emit('closeDetailModal', false)
      })
    },
    // AJAX获取数据详情
    ajaxGetDataDtl (data) {
      HTTPServer.customDtlData(data, 'Loading').then((res) => {
        this.modules = {bean: this.$route.query.bean, moduleName: this.$route.query.moduleName, type: 3, highSeaId: this.highSeaId}
        this.dtlData = res
        this.openCreateModal = true
      })
    }
  },
  mounted () {
    // 选中条目
    this.$bus.off('checkedData')
    this.$bus.on('checkedData', (bean, value) => {
      if (bean === this.$route.query.bean) {
        this.checkedList = value
      }
    })
    // 删除数据
    this.$bus.off('deleteType')
    this.$bus.on('deleteType', (value) => {
      this.dataId = value
      this.deleteData(0)
    })
    // 打开新增编辑窗口
    this.$bus.off('customOpenCreateModal')
    this.$bus.on('customOpenCreateModal', (modules, data) => {
      this.modules = modules
      this.dtlData = data
      this.openCreateModal = true
    })
    // 打开详情窗口
    this.$bus.off('openDataDtl')
    this.$bus.on('openDataDtl', (value, id, bean) => {
      let data = {
        bean: bean,
        dataId: id,
        moduleId: this.$route.query.moduleId,
        moduleName: this.$route.query.moduleName,
        highSeaId: this.highSeaId,
        highSeasAmdin: this.highSeasAmdin
      }
      this.detailList.push(data)
      this.$bus.emit('closeFilterModal', false)
    })
    // 关闭详情窗口
    this.$bus.off('closeDetailModal')
    this.$bus.on('closeDetailModal', (value, refresh) => {
      this.detailList.splice(value)
      if (refresh) {
        this.moduleLoadFinish = false
        setTimeout(() => {
          this.bean = this.$route.query.bean
          this.moduleLoadFinish = true
        }, 50)
      }
    })
    // 关闭新增窗口
    // this.$bus.off('customCloseCreateModal')
    this.$bus.on('customCloseCreateModal', value => {
      this.openCreateModal = false
    })
  },
  computed: {
    dragOption () {
      return {
        animation: 100,
        group: { name: 'menu', pull: true, put: false },
        sort: true,
        ghostClass: 'ghost'
      }
    },
    // 公海池领取退回
    getCustomerShow () {
      if (this.highSeaId !== '') {
        return true
      } else {
        return false
      }
    },
    // 公海池分配,移动
    highSeasShow () {
      if (this.highSeaId !== '' && this.highSeasAmdin === '1') {
        return true
      } else {
        return false
      }
    }
  },
  watch: {
    // 监听路由变化
    $route (to, from) {
      if (!to.query.bean && !to.query.moduleId) {
        console.log('切换应用了')
        this.moduleLoadFinish = false
        let myApp = {application_id: this.$route.query.appId}
        if (this.$route.params.addForm) {
          let quick = this.$route.query.muduleId
          this.ajaxGetAllMenuOfApp(myApp, quick)
        } else {
          this.ajaxGetAllMenuOfApp(myApp)
        }
      }
    }
  }
}
</script>
<style  lang="scss">
.module-main-wrap{
  height: 100%;
  >.el-container{
    height: 100%;
    .el-header{
      line-height: 60px;
      border: none;
      .list-top{
        border-bottom: 1px solid #E7E7E7;
        min-width: 930px;
        .title{
          font-size: 16px;
          color: #4A4A4A;
          padding: 0 0 0 10px;
        }
        .control-list{
          display: inline-block;
          >span{
            vertical-align: middle;
            margin: 0 30px 0 0;
            span{
              color: #FF6D5D;
            }
          }
          a{
            padding: 0 12px;
            border-left: 1px solid #D8D8D8;
            cursor: pointer;
            i{
              font-size: 16px;
              margin: 0 8px 0 0;
            }
          }
        }
        .button-list{
          display: flex;
          float: right;
          align-items: center;
          >.el-button{
            margin: 0 0 0 25px;
          }
          >i{
            font-size: 20px;
            margin: 0 0 0 20px;
            vertical-align: middle;
            cursor: pointer;
          }
          .filter-box{
            display: flex;
            width: 486px;
            flex-wrap: wrap;
            align-items: center;
            .el-input{
              flex: 1;
              .el-input__inner{
                border-top-right-radius: 0;
                border-bottom-right-radius: 0;
                &:focus {
                  border-color: #dcdfe6;
                }
              }
            }
            a{
              height: 36px;
              line-height: 36px;
              color: #666666;
              text-align: center;
              border-top: 1px solid #dcdfe6;
              border-bottom: 1px solid #dcdfe6;
              border-right: 1px solid #dcdfe6;
              cursor: pointer;
              &:nth-child(2){
                flex: 0 0 60px;
              }
              &:nth-child(3){
                flex: 0 0 78px;
              }
              &:last-child{
                flex: 0 0 48px;
                border-top-right-radius: 4px;
                border-bottom-right-radius: 4px;
                i{
                  font-size: 20px;
                  vertical-align: text-bottom;
                }
              }
            }
          }
        }
      }
    }
  }
  .module-list-main{
    height: 100%;
    overflow: hidden;
    padding: 0 20px 0;
  }
  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s
  }
  .fade-enter, .fade-leave-to /* .fade-leave-active in below version 2.1.8 */ {
    opacity: 0
  }
  .slide-fade-enter-active {
    transition: all .3s ease;
  }
  .slide-fade-leave-active {
    transition: all .3s cubic-bezier(1.0, 0.5, 0.8, 1.0);
  }
  .slide-fade-enter, .slide-fade-leave-to {
    transform: translateX(100px);
    opacity: 0;
  }
}
.shade{
  position: fixed;
  top:0;
  left:0;
  width:100%;
  height:100%;
  background: rgba(0, 0, 0, 0.4);
  z-index: 5;
}
</style>
