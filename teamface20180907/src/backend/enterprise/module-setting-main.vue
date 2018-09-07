<template>
  <div class="module-setting-main-wrip">
    <!-- <div class='header'>
        <i class="iconfont icon-jichumokuaishezhi"></i>
        <span>基础模块设置</span>
      </div> -->
    <div class='module_body'>
      <div class="item header-item">
        <span class="col1">名称</span>
        <span>状态</span>
        <span>操作</span>
      </div>
      <div class="item" v-for="(item, index) in dataList" :key="index">
        <div class="col1">
          <i class="iconfont" :class="' ' + item.icon" :style="'color: ' + item.color"></i><span>{{item.bean_name_chinese}}</span>
        </div>
        <div>
          <el-switch v-model="item.state" active-color="#1890FF" inactive-color="#ccc" @change="changeSwitch(item)" :disabled="item.disabled"></el-switch>
        </div>
        <div>
          <router-link :to="item.url">设置</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
export default {
  name: 'ModuleSettingMain',
  data () {
    return {
      employeeInfo: {},
      value2: true,
      dataList: [{icon: 'icon-kaoqin', bean_name_chinese: '工作台', bean_name: 'workbench', url: '/backend/WorkbenchSetting', color: '#CD5D82', state: true, disabled: true}, {icon: 'icon-xiezuo', bean_name_chinese: '协作', bean_name: 'project', url: '/backend/cooperation', color: '#F88F41', state: true}, {icon: 'icon-shenpi1', bean_name_chinese: '审批', bean_name: 'approval', url: '/backend/approvalManage', color: '#FABC01', state: true, disabled: true}, {icon: 'icon-wenjianku', bean_name_chinese: '文件库', bean_name: 'library', url: '/backend/libraryManage', color: '#F88F41', state: true}, {icon: 'icon-youjian3', bean_name_chinese: '邮件', bean_name: 'email', url: '/backend/mailbackstagemain', color: '#51D0B1', state: true}],
      token: JSON.parse(sessionStorage.requestHeader).TOKEN
    }
  },
  /* 页面加载后执行 */
  mounted () {
    this.findModuleList()
  },
  methods: {
    /** 禁用、启用模块 */
    changeSwitch (data) {
      console.log(data.state, data)
      data.state = !data.state
      var jsondata = {onoff_status: data.state ? 0 : 1, id: data.id, bean: data.bean_name}
      HTTPServer.updateSystemData(jsondata, 'Loading').then((res) => {
        data.state = !data.state
      })
    },
    /** 获取模块数据权限 */
    findModuleList () {
      HTTPServer.findSystemModuleList({}, 'Loading').then((res) => {
        res.map((item, index) => {
          var contains = tool.contains(this.dataList, 'bean_name', item, 'bean')
          if (contains) {
            this.dataList[contains.i].state = (item.onoff_status === '1')
            this.dataList[contains.i].id = item.id
            this.$set(this.dataList, contains.i, this.dataList[contains.i])
          }
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.module-setting-main-wrip{
  padding: 0;
  .header{
    border-bottom: 1px solid #E7E7E7;
    overflow: hidden;
    height: 60px;
    line-height: 60px;
    i{
      float: left;
      margin: 20px 10px 0 0;
      font-size: 20px;
      line-height: 1;
      }
    span{
      font-size: 18px;
      color: #69696C;
      }
    }
  .module_body{
        .item{height: 70px;line-height: 70px;border-bottom: 1px solid #f2f2f2;
            a{
              font-size: 14px;
              color: #1890FF;
            }
            i{font-size: 40px;float: left;margin: 15px 10px 0 0;line-height: 1;border-radius: 4px;}
            >div{
              display: inline-block;
              width: 200px;
              padding-left: 10px;
            }
            >div.col1{
              width: calc(100% - 410px);
            }
        }
        .item:hover{background: #f2f2f2;}
        .header-item:hover{
            background: none;
        }
        .header-item span{
            display: inline-block;
            width: 200px;
            padding-left: 10px;
            font-size: 16px;
            color: #17171A;
        }
        .header-item span:first-child{
            width: calc(100% - 410px);
        }
    }
}
</style>
