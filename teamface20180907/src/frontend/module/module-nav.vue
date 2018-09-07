<template>
  <el-aside class='module-nav-wrip' width="220px">
    <el-header>
      <em>{{appName}}</em>
    </el-header>
    <el-main>
      <el-collapse v-model="activeName" accordion @change="moduleChange">
        <el-collapse-item v-for="(item, index) in moduleList" :key="index" :name="String(item.id)">
          <template slot="title">
            <icon-img :type="item.icon_type" :url="item.icon_url" :size="iconStyle" :isModule="true" :noBorder="true"></icon-img>
            <span class="name">{{item.chinese_name}}</span>
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link"><i class="iconfont icon-Rectangle"></i></span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :command="{type:0,bean:item.english_name,id: item.id}">添加子菜单</el-dropdown-item>
                <el-dropdown-item :command="{type:1,bean:item.english_name}" v-if="employee.role_id === 1 || employee.role_id === 2">标签设置</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
          <div class="menu-box">
            <div class="item" v-for="(menu,index) in item.submenu.defaultSubmenu" :key="index" @click="menuFilter(menu)" :class="{'active':menu.active}">
              <i class="icon-img-flex" :class="{'icon-img-border': !isLast(index, 0, item.submenu), 'icon-img-border-last': isLast(index, 0, item.submenu)}"></i>
              <span>{{menu.name}}</span>
            </div>
          </div>
          <draggable v-model="item.submenu.newSubmenu" v-if="item.submenu.newSubmenu.length > 0" :options="dragOption" @end="drop($event, item.submenu.newSubmenu)" class="menu-box drag">
            <div class="item" v-for="(menu,index) in item.submenu.newSubmenu" :key="index" @click="menuFilter(menu)" :class="{'active':menu.active}">
              <i class="icon-img-flex" :class="{'icon-img-border': !isLast(index, 1, item.submenu), 'icon-img-border-last': isLast(index, 1, item.submenu)}"></i>
              <span>{{menu.name}}</span>
              <el-dropdown trigger="click" @command="deleteMenu" v-if="menu.editViewFieldsFlag === '1'">
                <span class="el-dropdown-link">
                  <i class="iconfont icon-nav-personal-setting"></i>
                </span>
                <el-dropdown-menu slot="dropdown" class="common-dropdown">
                  <el-dropdown-item :command="{menu:menu,type:0}"><i class="el-icon-edit"></i>编 辑</el-dropdown-item>
                  <el-dropdown-item :command="{id:menu.id,type:1}"><i class="el-icon-delete"></i>删 除</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </draggable>
        </el-collapse-item>
      </el-collapse>
    </el-main>
  </el-aside>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
import draggable from 'vuedraggable'
import IconImg from '@/common/components/icon-img'
export default {
  name: 'ModuleNav',
  components: {
    draggable,
    IconImg
  },
  props: ['list'],
  data () {
    return {
      employee: {},
      activeName: '',
      bean: '',
      appId: this.$route.query.appId,
      appName: this.$route.query.appName,
      moduleList: [],
      iconStyle: {
        border: '20px',
        content: '20px',
        background: '#4A4A4A',
        radius: '0'
      },
      highSeaId: '',
      highSeasAmdin: '',
      haveHighSeas: false,
      data: {}
    }
  },
  created () {
    this.employee = JSON.parse(sessionStorage.getItem('userInfo')).employeeInfo
  },
  methods: {
    // 是否是最后一个列表
    isLast (index, type, submenu) {
      if (type === 0) {
        // 默认菜单
        if (submenu.newSubmenu.length === 0) {
          return submenu.defaultSubmenu.length === index + 1
        } else {
          return false
        }
      } else {
        // 自定义菜单
        return submenu.newSubmenu.length === index + 1
      }
    },
    // 模块变化
    moduleChange (id) {
      this.moduleList.map((item) => {
        if (String(item.id) === id) {
          let params = {
            appId: item.application_id,
            appName: item.application_name,
            bean: item.english_name,
            moduleId: item.id,
            menuId: this.$route.query.menuId,
            moduleName: item.chinese_name
          }
          this.$router.push({name: 'DataModule', query: params})
          this.$bus.emit('haveSeas', JSON.stringify(item.submenu.defaultSubmenu).includes('is_seas_pool'))
        }
      })
    },
    // 模块设置
    handleCommand (command) {
      if (command.type === 0) {
        let value = {bean: command.bean, moduleId: command.id, type: 0}
        this.$bus.emit('openModuleMunu', value)
      } else {
        let id = {
          bean: command.bean,
          flag: '0'
        }
        this.ajaxGetReferenceList(id)
      }
    },
    // 菜单排序
    drop (evt, menuList) {
      let data = []
      menuList.map((item, index) => {
        data.push(item.id)
      })
      this.ajaxSortMenu(data)
    },
    // 编辑删除菜单
    deleteMenu (command) {
      console.log(command)
      if (command.type === 0) {
        let value = {
          bean: this.$route.query.bean,
          moduleId: this.$route.query.moduleId,
          menuId: command.menu.id,
          type: 1,
          name: command.menu.name,
          conditions: command.menu.relevanceWhere,
          highWhere: command.menu.high_where,
          peopleList: command.menu.allot_employee
        }
        this.$bus.emit('openModuleMunu', value)
      } else {
        this.$alert('<p style="margin:15px 0 35px 0">删除成功之后，该操作将无法恢复。</p>确认要删除该数据吗？', '提示', {
          dangerouslyUseHTMLString: true,
          showCancelButton: true
        }).then(_ => {
          let id = {id: command.id}
          this.ajaxDeleteMenu(id)
        }).catch(_ => {
        })
      }
    },
    // 子菜单筛选列表
    menuFilter (items) {
      this.editViewFieldsFlag = items.editViewFieldsFlag
      this.moduleList.map((item) => {
        item.submenu.defaultSubmenu.map((menu) => {
          this.$set(menu, 'active', false)
        })
        item.submenu.newSubmenu.map((menu) => {
          this.$set(menu, 'active', false)
        })
      })
      this.$set(items, 'active', true)
      console.log(this.moduleList)
      this.$bus.emit('setMenuName', items)
      let refresh = {
        bean: this.$route.query.bean,
        menuId: items.id,
        filter: {}
      }
      if (items.is_seas_pool === '1') {
        this.highSeaId = items.id
        this.highSeasAmdin = items.is_seas_admin
        refresh.menuId = null
        refresh.seas_pool_id = items.id
        this.data.highSeaId = items.id
        this.data.highSeasAmdin = items.is_seas_admin
      } else {
        this.highSeaId = ''
        this.highSeasAmdin = ''
        this.data.highSeaId = ''
        this.data.highSeasAmdin = ''
      }
      this.$bus.emit('refreshList', refresh)
      let params = {
        appId: this.$route.query.appId,
        appName: this.$route.query.appName,
        bean: this.$route.query.bean,
        moduleId: this.$route.query.moduleId,
        menuId: items.id,
        moduleName: this.$route.query.moduleName
      }
      this.$router.push({name: 'DataModule', query: params})
    },
    // 获取应用下所有模块及子菜单
    ajaxGetAllMenuOfApp (data, menuId, noRefresh) {
      HTTPServer.getAllMenuOfApp(data, 'Loading').then((res) => {
        if (res.length > 0) {
          if (noRefresh) {
            // 不刷新 active
            res.map((item) => {
              item.submenu.newSubmenu.map((item2) => {
                if (item2.id === this.$route.query.menuId) {
                  item2.active = true
                }
              })
            })
          } else {
            // 刷新 active
            res.map((item) => {
              item.submenu.newSubmenu.map((item2) => {
                if (item2.id === menuId) {
                  item2.active = true
                }
              })
            })
          }
          this.moduleList = res
        }
      })
    },
    // AJAX获取关联关系数组
    ajaxGetReferenceList (data) {
      HTTPServer.customRelationList(data, 'Loading').then((res) => {
        console.log(res)
        let data = {bean: this.$route.query.bean, menuId: this.$route.query.menuId, type: 1, list: res}
        this.$bus.emit('openShowDrag', data)
      })
    },
    // 删除菜单
    ajaxDeleteMenu (data) {
      HTTPServer.deleteSubMenu(data, 'Loading').then((res) => {
        this.$message.success('删除成功!')
        let myApp = {application_id: this.$route.query.appId}
        this.ajaxGetAllMenuOfApp(myApp, this.moduleId, true)
      })
    },
    // 菜单排序
    ajaxSortMenu (data) {
      HTTPServer.sortSubMenu(data, null).then((res) => {
        // this.$message.success('保存成功!')
      })
    }
  },
  mounted () {
    // 刷新子菜单列表
    this.$bus.off('refreshMenuList')
    this.$bus.on('refreshMenuList', (value, noRefresh) => {
      if (!noRefresh) {
        this.$bus.emit('setMenuName', {name: value.menu_name})
      }
      let myApp = {application_id: this.$route.query.appId}
      this.ajaxGetAllMenuOfApp(myApp, value.menu_id, noRefresh)
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
    }
  },
  watch: {
    list (newVal, oldVal) {
      if (newVal.length > 0) {
        console.log(newVal)
        this.appName = this.$route.query.appName
        this.moduleList = newVal
        if (this.$route.params.addForm) {
          this.activeName = String(this.$route.query.moduleId)
        } else {
          this.activeName = String(newVal[0].id)
        }
        console.log(this.activeName)
      }
    }
  }
}
</script>

<style lang="scss">
.el-message-box{
  .el-message-box__header{
    border-bottom: 1px solid #E7E7E7;
  }
  .el-message-box__message{
    P{
      >p{
        margin: 15px 0 10px 0;
      }
    }
  }
}
.el-aside.module-nav-wrip{
  background: #EBEEF0;
  overflow:hidden;
  .el-header{
    line-height: 60px;
    padding: 0  0 0 15px;
    background: #F4F6F7;
    box-shadow: inset 0 -1px 0 0 #D6D6D6;
    em{
      display: inline-block;
      width: calc(100% - 40px);
      vertical-align: middle;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      font-size: 16px;
      font-style: normal;
      color: #4A4A4A;
    }
    span{
      display:inline-block;
      transform:rotate(270deg);
      -ms-transform:rotate(270deg);
      -moz-transform:rotate(270deg);
      -webkit-transform:rotate(270deg);
      -o-transform:rotate(270deg);
      i{
        font-size: 20px;
        vertical-align: middle;
        color: #797979;
      }
    }
  }
  .el-main{
    height: 100%;
    padding: 10px 0;
    .el-collapse{
      height: calc(100% - 50px);
      overflow: auto;
      .el-collapse-item{
        .el-collapse-item__header{
          display: flex;
          align-items: center;
          height: 40px;
          line-height: 40px;
          padding: 0 0 0 25px;
          background: #EBEDF0;
          border: none;
          &:hover{
            >.el-dropdown{
              visibility: visible;
            }
          }
          .el-collapse-item__arrow{
            display: none;
          }
          >.icon-img-wrap{
            margin: 0 5px 0 0;
          }
          >.name{
            flex: 0 0 125px;
            color: #333333;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          >.el-dropdown{
            visibility: hidden;
            flex: 1;
            padding: 0 10px 0 10px;
            text-align: right;
            span{
              display: inline-block;
              width: 100%;
            }
          }
        }
        .el-collapse-item__header.is-active{
          background: #D7DCE0;
          >.el-dropdown{
            visibility: visible;
          }
        }
        .el-collapse-item__wrap{
          border: none;
          .el-collapse-item__content{
            padding: 0;
            background: #EBEDF0;
          }
        }
      }
    }
    .menu-box{
      .item.active{
        background: #D7DCE0;
        color: #4A4A4A;
        .el-dropdown{
          visibility: visible;
        }
        i{
          visibility: visible;
        }
      }
      .item{
        display: flex;
        width: 100%;
        height: 40px;
        line-height: 40px;
        box-sizing: border-box;
        padding: 0 5px 0 30px;
        font-size: 14px;
        color: #797979;
        cursor: pointer;
        border-right: 2px solid transparent;
        span{
          display: block;
          flex: 1;
          height: 40px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        &:hover{
          background: #D7DCE0;
          color: #4A4A4A;
          .el-dropdown{
            visibility: visible;
          }
        }
        .icon-img-flex{
          flex: 0 0 15px;
          height: 40px;
          margin: 0 5px 0 0;
          background-size: cover;
        }
        .icon-img-border{
          background: url('../../../static/img/custom/icon_img_border.png') no-repeat center;
        }
        .icon-img-border-last{
          background: url('../../../static/img/custom/icon_img_border_last.png') no-repeat center;
        }
        .el-dropdown{
          visibility: hidden;
          flex: 0 0 20px;
          vertical-align: middle;
        }
      }
    }
    .menu-box.drag{
      display: inherit;
      .item{
        cursor: move;
      }
    }
  }
}
</style>
<style lang="scss" scoped>
.el-dropdown-menu{
  margin: 0;
}
.common-dropdown{
  li{
    >i{
      margin: 0 10px 0 0;
    }
  }
}
</style>
