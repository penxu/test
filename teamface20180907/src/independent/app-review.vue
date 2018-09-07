<template>
  <el-container class="common-module-review">
    <el-header style="height:50px;">
      <span>预览模式</span>
      <i class="el-icon-close" @click="closeWindow"></i>
    </el-header>
    <el-main>
      <div class="container-left">
        <ul style="background: rgba(0,0,0,.17);">
          <li>
            <img src="../assets/head1.jpg">
            <!-- <span>游客</span> -->
          </li>
        </ul>
        <ul class="module-more">
          <!-- <li v-for="(item, index) in moduleList" :key="index"  @click="shiftModule(item.english_name)" class="o" :class="index==0?'oneHover':''">
            <img class="icon" :src="item.icon_url + '&TOKEN=' + token" v-if="item.icon_type == 1" />
            <i class="iconfont" :class="item.icon_url" v-if="item.icon_type != 1"></i>
            <i class="iconfont" :class="item.icon_url2 || item.icon_url + '-b'"  v-if="item.icon_type != 1"></i>
            <span class="name">{{item.chinese_name | limitTo(4)}}</span>
          </li> -->
          <li class="o ">
            <i class="iconfont icon-nav-workbench"></i>
            <i class="iconfont icon-nav-workbench-o"></i>
            <span class="name">工作台</span>
          </li>
          <li class="o ">
            <i class="iconfont icon-system-library-5"></i>
            <i class="iconfont icon-system-library-5-b"></i>
            <span class="name">企信</span>
          </li>
          <li class="o isactive">
            <i class="iconfont" :class="icon_url_new"></i>
            <i class="iconfont" :class="icon_url_new+'-b'"></i>
            <span class="name">{{template_name | limitTo(4)}}</span>
          </li>
          <li class="o ">
            <i class="iconfont icon-system-library-6"></i>
            <i class="iconfont icon-system-library-6-b"></i>
            <span class="name">应用</span>
          </li>
          <li class="o ">
            <i class="iconfont icon-nav-quickly-add"></i>
            <i class="iconfont icon-nav-quickly-add"></i>
            <span class="name">快速新增</span>
          </li>
        </ul>
        <ul class="nav-app bottom">
          <li class="o">
            <i class="iconfont icon-system-library-3"></i>
            <i class="iconfont icon-system-library-3-b"></i>
            <span class="ellipsis name">统计分析</span>
          </li>
          <li class="o">
            <i class="iconfont icon-youjian1"></i>
            <i class="iconfont icon-youjian"></i>
            <span class="name">邮件</span>
          </li>
        </ul>
        <div class="login-box">
          <div class="transparency"></div>
          <span class="simpName">张三</span>
          <!-- <img class="transparency" v-if="currentEmployee.picture" :src="currentEmployee.picture + '&TOKEN=' + token" /> -->
          <span class="name">张三</span>
        </div>
      </div>
      <div class="flex-box">
        <div class="container-center">
          <div class="modules-name">
            <i class="iconfont" :class="muduleIcon"></i>
            <span>{{nativeLayout.applicationName}}</span>
          </div>
          <!-- <div class="button-list">
            <el-button size="mini" icon="iconfont icon-tianjia">添加子菜单</el-button>
            <el-button size="mini" icon="iconfont icon-shezhi">标签设置</el-button>
          </div> -->
          <div class="menu-list">
            <!-- <span class="active">全部{{muduleName}}</span>
            <span>我创建的</span> -->
            <el-collapse v-model="activeName" accordion @change="moduleChange">
              <el-collapse-item v-for="(item, index) in moduleList" :key="index" :name="String(item.id)">
                <!-- <span>{{item.name}}</span> -->
                <template slot="title">
                  <icon-img :type="item.icon_type" :url="item.icon_url" :size="iconStyle" :isModule="true" :noBorder="true"></icon-img>
                  <span class="name">{{item.chinese_name}}</span>
                  <el-dropdown>
                    <span class="el-dropdown-link"><i class="iconfont icon-Rectangle"></i></span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item>添加子菜单</el-dropdown-item>
                      <el-dropdown-item>标签设置</el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </template>
                <div class="menu-box">
                  <div class="item" v-for="(menu,index) in item.submenu.defaultSubmenu" :key="index" :class="{'active':menu.active}"  @click="shiftModule(menu, item.submenu.defaultSubmenu)">
                    <i class="icon-img-flex icon-img-border" v-if="index<item.submenu.defaultSubmenu.length-1||item.submenu.newSubmenu.length > 0"></i>
                    <i class="icon-img-flex icon-img-border-last" v-if="item.submenu.newSubmenu.length == 0&&index==item.submenu.defaultSubmenu.length-1"></i>
                    <span>{{menu.name}}</span>
                  </div>
                </div>
                <draggable v-model="item.submenu.newSubmenu" v-if="item.submenu.newSubmenu.length > 0" :options="dragOption" class="menu-box drag">
                  <div class="item" v-for="(menu,index) in item.submenu.newSubmenu" :key="index" :class="{'active':menu.active}" @click="shiftModule(menu, item.submenu.newSubmenu)">
                    <i class="icon-img-flex icon-img-border" v-if="index<item.submenu.newSubmenu.length-1"></i>
                    <i class="icon-img-flex icon-img-border-last" v-if="index==item.submenu.newSubmenu.length-1"></i>
                    <span>{{menu.name}}</span>
                    <el-dropdown trigger="click">
                      <span class="el-dropdown-link">
                        <i class="iconfont icon-nav-personal-setting"></i>
                      </span>
                      <el-dropdown-menu slot="dropdown" class="common-dropdown">
                        <el-dropdown-item><i class="el-icon-edit" style="margin-right:10px"></i>编 辑</el-dropdown-item>
                        <el-dropdown-item><i class="el-icon-delete" style="margin-right:10px"></i>删 除</el-dropdown-item>
                      </el-dropdown-menu>
                    </el-dropdown>
                  </div>
                </draggable>
              </el-collapse-item>
            </el-collapse>
          </div>
        </div>
        <div class="container-right">
          <div class="titles">
            <span>{{muduleName}}</span>
            <div class="button-list">
              <i class="iconfont icon-daoru" title="导入"></i>
              <i class="iconfont icon-daochu" title="导出"></i>
              <i class="iconfont icon-pc-filter" title="筛选"></i>
              <el-button type="primary" size="mini" @click="openCreateData">新增</el-button>
            </div>
          </div>
          <div class="list-box">
            <div class="list-title">
              <el-checkbox v-model="checkAll"></el-checkbox>
              <div class="item" v-for="(title,index) in titleList" :key="index" :style="{'width':100/titleList.length+'%'}">
                <span>{{title.label}}</span>
                <i class="el-icon-d-caret"></i>
              </div>
            </div>
            <div class="list">
              <div class="item" v-for="(list,index) in dataList" :key="index">
                <el-checkbox v-model="checkAll"></el-checkbox>
                <div class="rows" v-for="(item,index2) in list.rows" :key="index2" :style="{'width':100/list.rows.length+'%'}" @click="openDataDtl(index)">
                  <div class="value" v-if="item.type === 'area'">
                    {{item.value | areaTo}}
                  </div>
                  <div class="value" v-else-if="item.type === 'datetime'">
                    {{item.value | formatDate(item.format)}}
                  </div>
                  <div class="value" v-else-if="item.type === 'multitext'" v-html="item.value.value">
                  </div>
                  <div class="value" v-else-if="item.type === 'location'">
                    {{item.value.value}}
                  </div>
                  <div class="value" v-else-if="item.type === 'personnel'">
                    <span v-for="(child,index) in item.value" :key="index" class="personnel-span">{{child.name}}</span>
                  </div>
                  <div class="value" v-else-if="item.type === 'reference'">
                    <span v-for="(child,index) in item.value" :key="index">{{child.name}}</span>
                  </div>
                  <div class="value" v-else-if="item.type === 'picklist' || item.type === 'multi' || item.type === 'mutlipicklist'">
                    <span v-for="(child,index) in item.value" :key="index" :style="{'background':child.color}" :class="{'white-font':child.color === '#FFFFFF' || child.color === undefined}">{{child.label}}</span>
                  </div>
                  <div class="value" v-else>
                    {{item.value}}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-main>
    <div class="shade" v-if="openForm || openDetail"></div>
    <transition name="slide">
      <!-- <module-create-new v-if="openCreateModal" :modules="modules" :dtlData="dtlData" :layout="layout"></module-create-new> -->
      <review-create v-if="openForm" :modules="modules" :nativeLayout="nativeLayout"></review-create>
    </transition>
    <transition name="slide">
      <review-detail :data="dtlData" v-if="openDetail"></review-detail>
    </transition>
    <review-member :datas="member"></review-member>
  </el-container>
</template>
<script>
import {baseURL} from '@/common/js/ajax.js'
import axios from 'axios'
import draggable from 'vuedraggable'
import ReviewCreate from './review-create1'// 新增
import ReviewDetail from './review-detail'// 详情
import ReviewMember from './review-member'// 假成员
import IconImg from '@/common/components/icon-img'

export default {
  name: 'ModuleReview',
  components: {
    draggable,
    ReviewCreate,
    ReviewDetail,
    ReviewMember,
    IconImg
  },
  data () {
    return {
      activeName: '',
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      an: false,
      openForm: false,
      openDetail: false,
      bean: '',
      muduleName: '',
      muduleIcon: '',
      moduleList: [],
      checkAll: '',
      member: '',
      nativeLayout: [],
      layout: [],
      titleList: [],
      dataList: [],
      dtlList: [],
      dtlData: {},
      newNavList: [],
      template_name: '',
      icon_url_new: '',
      iconStyle: {
        border: '20px',
        content: '20px',
        background: '#4A4A4A',
        radius: '0'
      }
    }
  },
  created () {
    let id = {template_application_id: this.$route.query.id}
    this.icon_url_new = this.$route.query.iconUrl
    this.template_name = this.$route.query.templateName
    axios({
      method: 'get',
      url: 'applicationCenter/getTemplateModuleByTemplateId',
      baseURL: baseURL,
      params: id
    }).then((res) => {
      res.data.data.map((v, k) => {
        if (v.submenu) {
          if (v.submenu.defaultSubmenu && v.submenu.defaultSubmenu.length > 0) {
            v.submenu.defaultSubmenu.map((v1, k1) => {
              v1.english_name = v.english_name
              v1.active = false
            })
          } else {

          }
          if (v.submenu.newSubmenu && v.submenu.newSubmenu.length > 0) {
            v.submenu.newSubmenu.map((v1, k1) => {
              v1.english_name = v.english_name
              v1.active = false
            })
          }
        } else {
          v.submenu = {defaultSubmenu: [], newSubmenu: []}
          v.submenu.defaultSubmenu[0] = JSON.parse(JSON.stringify(v))
          v.submenu.defaultSubmenu[0].name = '全部' + v.chinese_name
          v.submenu.defaultSubmenu[0].active = false
          v.submenu.defaultSubmenu[1] = JSON.parse(JSON.stringify(v))
          v.submenu.defaultSubmenu[1].name = v.chinese_name
          v.submenu.defaultSubmenu[1].active = false
        }
        if (k === 0) {
          v.submenu.defaultSubmenu[0].active = true
        }
      })
      this.moduleList = res.data.data
      this.activeName = String(this.moduleList[0].id)
      this.bean = res.data.data[0].english_name
      this.getLayout(this.moduleList[0].submenu.defaultSubmenu[0])
    })
      .catch((err) => {
        console.log(err)
      })
    // 关闭新增窗口
    this.$bus.on('customCloseCreateModal', value => {
      this.openForm = false
    })
  },
  methods: {
    // 关闭窗口
    closeWindow () {
      var browserName = navigator.appName
      // var browserVer = parseInt(navigator.appVersion)
      if (browserName === 'Microsoft Internet Explorer') {
        var ie7 = !!((document.all && !window.opera && window.XMLHttpRequest))
        if (ie7) {
          window.open('', '_parent', '')
          window.close()
        } else {
          this.focus()
          self.opener = this
          self.close()
        }
      } else {
        try {
          this.focus()
          self.opener = this
          self.close()
        } catch (e) {
        }
        try {
          window.open('', '_self', '')
          window.close()
        } catch (e) {
        }
      }
    },
    // 切换模块
    shiftModule (item, list) {
      this.moduleList.map((item) => {
        item.submenu.defaultSubmenu.map((menu) => {
          this.$set(menu, 'active', false)
        })
        item.submenu.newSubmenu.map((menu) => {
          this.$set(menu, 'active', false)
        })
      })
      item.active = true
      this.getLayout(item)
      this.dataList = []
      this.dtlList = []
    },
    // 获取布局
    getLayout (item) {
      let data = {bean: item.english_name, operationType: 2, plist_relyon: 1}
      this.muduleName = item.name
      axios({
        method: 'get',
        url: 'applicationCenter/getLayoutByTemplateModule',
        baseURL: baseURL,
        params: data
      }).then((res) => {
        console.log(res.data)
        this.nativeLayout = res.data.data
        // this.muduleName = res.data.data.title
        this.muduleIcon = res.data.data.icon
        let data = []
        let titleList = []
        res.data.data.layout.map((item) => {
          item.rows.map((item2) => {
            if (item2.field.addView === '1' && item2.field.terminalPc === '1') {
              data.push(item2)
              if (item2.type !== 'attachment' && item2.type !== 'picture' && item2.type !== 'subform' && item2.type !== 'multitext' && titleList.length < 6) {
                titleList.push(item2)
              }
            }
          })
        })
        this.layout = data
        this.titleList = titleList
      }).catch((err) => {
        console.log(err)
      })
    },
    // 打开新增窗口
    openCreateData () {
      this.openForm = true
      this.modules = {bean: this.bean, moduleName: this.nativeLayout.title}
      // let modules = {bean: this.bean, moduleName: this.nativeLayout.title, type: 2}
      this.$nextTick(() => {
        // this.$bus.emit('sendLayout', modules, this.nativeLayout.layout)
      })
    },
    // 模块变化
    moduleChange (id) {
      // this.moduleList.map((item) => {
      //   if (String(item.id) === id) {
      //     this.getLayout(item.submenu.defaultSubmenu[0])
      //   }
      // })
    },
    // 打开详情侧弹
    openDataDtl (index) {
      let data = {
        bean: this.bean,
        layout: this.nativeLayout,
        data: this.dtlList[index]
      }
      this.dtlData = data
      this.openDetail = true
    }
  },
  mounted () {
    this.$bus.on('commonMember', (jsondata) => {
      this.member = jsondata
      if (jsondata.type === 0) {
      } else if (jsondata.type === 1) {
      }
    })
    // 添加本地数据
    this.$bus.on('setLocalData', value => {
      for (let i in value.data) {
        if (i.includes('personnel')) {
          value.data[i] = [{id: 3, name: '张三', picture: ''}]
        }
      }
      let time = new Date().getTime()
      value.data.personnel_create_by = [{id: 4, name: '李四', picture: ''}]
      value.data.datetime_create_time = time
      value.data.personnel_modify_by = [{id: 5, name: '王五', picture: ''}]
      value.data.datetime_modify_time = time
      let data = value.data
      this.dtlList.push(data)
      let list = []
      this.layout.map((item, index) => {
        if (item.type !== 'attachment' && item.type !== 'picture' && item.type !== 'subform') {
          if (list.length < 6) {
            list.push({
              type: item.type,
              name: item.name,
              label: item.label,
              format: item.field.formatType,
              value: data[item.name]
            })
          }
        }
      })
      this.dataList.push({id: value.bean, rows: list})
    })
    // 关闭窗口
    this.$bus.on('colseLocalCreate', value => {
      this.openForm = false
    })
    // 关闭窗口
    this.$bus.on('colseLocalDetail', value => {
      this.openDetail = false
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
  }
}
</script>

<style lang="scss">
.common-module-review{
  height: 100%;
  >.el-header{
    text-align: center;
    background: #4C5459;
    box-shadow: 0 1px 4px 0 rgba(0,21,41,0.12);
    span{
      font-size: 18px;
      color: #fff;
      line-height: 50px;
    }
    i{
      font-size: 20px;
      float: right;
      margin: 15px 10px;
      cursor: pointer;
      color:#fff;
    }
  }
  >.el-main{
    height: 100%;
    padding: 0;
    .container-left{
      position: absolute;
      left: 0;
      top: 50px;
      float: left;
      width: 60px;
      height: 95%;
      background: #0079D8;
      transition:width 0.5s;
      z-index: 4;
      li{
        height: 60px;
        line-height: 60px;
        cursor: pointer;
        &:hover{
          &::before{
            content: "";
            display: inline-block;
            width: 100%;
            background: #fff;
            opacity: 0.1;
            position: absolute;
            top: 0;
            left: 0;
            height: 100%;
          }
        }
        img{
          height: 45px;
          width: 45px;
          margin: 7px;
          border-radius: 4px;
        }
        .iconfont{
          font-size: 24px;
          float: left;
          color: #fff;
          line-height: 1;
          margin-left: 17px;
        }
        span{
          font-size: 12px;
          color: #fff;
          opacity: 0;
          float: left;
          margin: 5px 0 0;
          line-height: 1;
          width: 100%;
          text-align: center;
        }
      }
      .module-more{
        height: calc(100% - 300px);
        overflow-y: auto;
        overflow-x: hidden;
        width: 60px;
        li{
          height: 60px;
          line-height: 60px;
          position: relative;
          margin: 1px 0;
          cursor: pointer;
          padding-top: 18px;
          transition: padding-top .5s,top .5s;
        }
        li.o{
          .iconfont:nth-child(2){display: none;}
        }
        li.o.isactive,li.o:hover{
          padding-top: 8px;
          .iconfont:nth-child(1){display: none;}
          .iconfont:nth-child(2){display: inline-block;}
          >.name{opacity: 1;}
          .total{
            top: 4px;
          }
        }
        li.o.isactive{
          background:#1986DC;
        }
      }
      .login-box{
        height: 74px;
        position: absolute;
        bottom: 0;
        left: 0;
        div.transparency{width: 44px;height: 44px;opacity: 0.4;background: #fff;border-radius: 50%;margin: 10px 0 0 10px;float: left;}
        .simpName{width: 44px;height: 44px;text-align: center;line-height: 44px;font-size: 14px;color: #fff;float: left;margin: 10px 0 0 -44px;background: none;}
        .name{
          display: inline-block;
          margin: 20px 0 0 10px;
          color: #fff;
          opacity: 0;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          width: 0;
        }
        img{
          width: 44px;
          height: 44px;
          float: left;
          margin: 10px 0 0 8px;
          border-radius: 50%;
        }
      }
      .nav-app{width: 65px;
        li{height: 60px;line-height: 60px;position: relative;margin: 1px 0;cursor: pointer;padding-top: 18px;
          transition: padding-top 0.5s, top 0.5s;
          -webkit-transition: padding-top 0.5s, top 0.5s; /* Safari */
          >.iconfont{font-size: 24px;float: left;color: #fff;line-height: 1;margin-left: 20px;}
          >img{width: 24px;height: 24px;margin: 0 0 0 20px;float: left;line-height: 1;}
          >.name{font-size: 12px;color: #fff;opacity: 0;float: left;margin: 5px 0 0 0;line-height: 1;width: 100%;text-align: center;}
          .total{color: #fff;
            font-size: 10px;
            padding: 0.5px 3px;
            background: red;
            opacity: 1 !important;
            line-height: 1;
            border-radius: 8px;
            position: absolute;
            top: 14px;
            left: 28px;
            transition: top 0.5s;
            -webkit-transition: top 0.5s; /* Safari */
          }
        }
        li.o{
          .iconfont:nth-child(2){display: none;}
        }
        li.o:hover{
          padding-top: 8px;
          .iconfont:nth-child(1){display: none;}
          .iconfont:nth-child(2){display: inline-block;}
          >.name{opacity: 1;}
          .total{
            top: 4px;
          }
        }
        li.router-link-active{
          .iconfont:nth-child(1){display: none;}
          .iconfont:nth-child(2){display: inline-block;}
        }
        li:hover::before{content: "";display: inline-block;width: 100%;background: #fff;opacity: 0.1;position: absolute;top: 0;left: 0;height: 100%;}
        .router-link-active::before{content: "";display: inline-block;width: 100%;background: #fff;opacity: 0.1;position: absolute;top: 0;left: 0;height: 100%;}
        .app-box{
          position: absolute;
          left: 60px;
          top: -55px;
          z-index: 9;
          width: 336px;
          height: 232px;
          background: #FCFEFF;
          box-shadow: inset 0 0 1px 0 rgba(169,172,172,0.50);
          padding: 10px 0 0 20px;
          cursor: default;
          display: none;
          .app-header{
            line-height: 40px;
          }
          .app-item{
            display: inline-block;
            line-height: 1;
            .icon{
              line-height: 1;
              display: inline-block!important;
            }
            .app-name{
              text-overflow: ellipsis;
              width: 58px;
              line-height: 20px;
            }
          }
          .app-item:nth-child(5){
            margin-right: 0;
          }
        }
      }
      .nav-app.bottom{
        position: absolute;
        bottom: 80px;
      }
      .nav-app.bottom{
        .quickly-add:hover{
          .app-box{
            display: inline-block;
          }
        }
      }
    }
    .flex-box{
      display: flex;
      height: 100%;
      margin: 0 0 0 60px;
      .container-center{
        flex: 0 0 220px;
        height: 100%;
        background: #EBEDF0;
        .modules-name{
          width: 220px;
          height: 60px;
          line-height: 60px;
          padding:0 0 0 15px;
          box-shadow: 0 1px 2px 0 rgba(185, 185, 185, 0.5);
          text-overflow: ellipsis;
          white-space: nowrap;
          overflow: hidden;
          border-bottom:1px solid #ddd;background: #f4f6f7;
          .iconfont{
            font-size: 24px;
            margin: 0 5px 0 0;
          }
          span{
            font-size: 16px;
            color: #4A4A4A;
            vertical-align: top;
          }
        }
        .button-list{
          padding: 14px 0;
          text-align: center;
          .el-button{
            width: 90px;
            padding: 4px 0;
            background: #EBEDF0;
            border: 1px solid #20BF9A;
            color: #20BF9A;
            span{
              font-size: 12px;
            }
            .iconfont {
              font-size: 14px;
              margin: 0 5px 0 0;
            }
            &:hover{
              background: #20BF9A;
              color: #FFFFFF;
            }
          }
        }
        .menu-list{
          height:93%;
          .active{
            background: #D7DCE0;
          }
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
                background: url('../../static/img/custom/icon_img_border.png') no-repeat center;
              }
              .icon-img-border-last{
                background: url('../../static/img/custom/icon_img_border_last.png') no-repeat center;
              }
              .el-dropdown{
                visibility: hidden;
                flex: 0 0 20px;
                vertical-align: middle;
              }
            }
          }
        }
      }
      .container-right{
        flex: 1;
        height: 100%;
        padding: 0 24px;
        .titles{
          height: 60px;
          line-height: 60px;
          padding: 0 0 0 10px;
          border-bottom: 1px solid #E7E7E7;
          >span{
            font-size: 16px;
            color: #4A4A4A;
          }
          .button-list{
            float: right;
            .iconfont{
              font-size: 20px;
              margin: 0 20px 0 0;
              color: #979797;
            }
          }
        }
        .list-box{
          height: calc(100% - 80px);
          .list-title{
            display: flex;
            width: 100%;
            height: 50px;
            line-height: 50px;
            padding: 0 10px;
            border-bottom: 1px solid #F2F2F2;
            box-sizing: border-box;
            .el-checkbox{
              margin: 0 5px 0 0;
            }
            .item{
              min-width: 160px;
              max-width: 280px;
              padding: 0 0 0 8px;
              box-sizing: border-box;
              margin-bottom: -1px;
              border-bottom: 1px solid #F2F2F2;
              span{
                font-size: 14px;
                color: #17171A;
                font-weight: bold;
              }
              i{
                float: right;
                font-size: 16px;
                margin: 17px 0 0 0;
                padding: 0 8px 0 0;
                color: #B2B2B2;
                border-right: 1px solid #E7E7E7;
                cursor: pointer;
              }
            }
          }
          .list{
            height: calc(100% - 60px);
            overflow: auto;
            .item{
              display: flex;
              height: 50px;
              line-height: 50px;
              padding: 0 10px;
              border-bottom: 1px solid #F2F2F2;
              cursor: pointer;
              &:hover{
                background: #F2F2F2;
              }
              .el-checkbox{
                margin: 0 5px 0 0;
              }
              .rows{
                min-width: 160px;
                max-width: 280px;
                padding: 0 0 0 8px;
                font-size: 14px;
                color: #69696C;
                box-sizing: border-box;
                margin-bottom: -1px;
                border-bottom: 1px solid #F2F2F2;
                .value{
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                  span{
                    display: inline-block;
                    max-width: 160px;
                    min-width: 52px;
                    height: 24px;
                    line-height: 24px;
                    font-size: 12px;
                    color: #FFFFFF;
                    padding: 0 8px;
                    margin: 0 10px 0 0;
                    border-radius: 37px;
                    text-align: center;
                    vertical-align: middle;
                    box-sizing: border-box;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                  }
                  span.white-font{
                    color: #4A4A4A;
                    height: 100%;
                    line-height: 46px;
                    font-size: 14px;
                    padding: 0;
                    text-align: left;
                    background: none !important;
                  }
                  span.personnel-span {
                    display: inline-block;
                    max-width: auto;
                    min-width: auto;
                    font-size: 14px;
                    color: #69696C;
                    padding: 0;
                    margin: 0 5px 0 0;
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  .shade {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.4);
    z-index: 5;
  }
}
</style>
