<template>
<div class="custom-content-wrip">
  <div class="titles">
    <a>
      <i class="iconfont icon-diannao"></i>
      <span @click.stop="switchLayout(pageType)">{{activePage.page_name}}</span>
      <el-popover
        v-model="visible"
        placement="bottom"
        width="270"
        popper-class="page-more-popover"
        trigger="click">
        <div class="content">
          <div class="item" v-for="(item, index) in pageList" :key="index">
            <span v-if="item.edit_show" @click="switchPage(item)">{{item.page_name}}</span>
            <i class="iconfont icon-module-menu-1" v-if="item.page_num > 0 && item.edit_show" @click.stop="item.edit_show = false"></i>
            <i class="el-icon-delete" v-if="item.page_num > 0 && item.edit_show" @click="delPageLayout(item)"></i>
            <el-input v-model="item.page_name" placeholder="请输入内容" :autofocus="true" v-if="item.page_num > 0 && !item.edit_show" @blur="editLayout(item)" @focus="editBeforeName = item.page_name"></el-input>
          </div>
          <div class="item-button"><el-button type="text" size="mini" icon="el-icon-plus" @click="addChildLayout">新增页面</el-button></div>
        </div>
        <i class="el-icon-caret-bottom" slot="reference"></i>
      </el-popover>
    </a>
    <a @click.stop="switchLayout(-1)">
      <i class="iconfont icon-shouji"></i>
      <span  @click.stop="switchLayout(-1)">APP列表布局</span>
    </a>
    <!-- <a @click.stop="switchLayout(-2)">
      <i class="iconfont icon-shouji"></i>
      <span  @click.stop="switchLayout(-2)">web表单</span>
    </a> -->
    <el-button type="primary" size="mini" @click="saveLayout(isPcLayout)" v-if="isPcLayout >= 0">保 存</el-button>
  </div>
  <custom-drag  v-if="isPcLayout === 0"></custom-drag>
  <custom-drag-page :pageNum="activePage.page_num"  v-if="isPcLayout > 0"></custom-drag-page>
  <app-list-layout ref="appList"  v-if="isPcLayout === -1"></app-list-layout>
  <!-- <custom-webform v-if="isPcLayout === -2"></custom-webform> -->
  <webformList v-if="isPcLayout === -2"></webformList>
</div>
</template>

<script>
import {HTTPServer, ajaxGetRequest} from '@/common/js/ajax.js'
import { mapGetters, mapState, mapMutations } from 'vuex'
import tool from '@/common/js/tool.js'
import axios from 'axios'
import CustomDrag from '@/backend/custom/components/custom-drag'
import CustomDragPage from '@/backend/custom/components/custom-drag-page'
import AppListLayout from '@/backend/custom/components/app-list-layout' // app列表布局
import webformList from '@/backend/custom/components/webform-list' // web表单列表
// import CustomWebform from '@/backend/custom/layout/custom-webform' // web表单
const dragOptions = {
  animation: 100,
  group: { name: 'compontents', pull: true, put: true },
  ghostClass: 'ghost',
  filter: '.no-drag'
}
export default {
  name: 'CustomContent',
  components: {
    CustomDrag,
    CustomDragPage,
    AppListLayout,
    webformList
  },
  data () {
    return {
      isPcLayout: 0,
      pageType: 0,
      visible: false,
      activePage: {page_num: 0, page_name: '标准页面'},
      activePages: {},
      editBeforeName: '',
      pageList: []
    }
  },
  created () {
    this.ajaxGetChildLayout()
    this.$bus.on('shiftLayout', (value, layoutOrPage, page) => {
      if (value) {
        if (layoutOrPage === 'layout') {
          this.isPcLayout = page
        } else {
          this.visible = false
          this.activePage = page
          this.activePages = JSON.parse(JSON.stringify(page))
          this.isPcLayout = page.page_num
          this.pageType = page.page_num
        }
      }
    })
  },
  methods: {
    // 切换布局
    switchLayout (type) {
      this.isPcLayout = type
      if (type === -1 || type === -2) {
        this.$bus.emit('layoutContentChange', undefined, 'layout', type)
      } else {
        this.isPcLayout = type
      }
    },
    // 切换多页面
    switchPage (page) {
      // this.visible = false
      // this.activePage = page
      // this.activePages = JSON.parse(JSON.stringify(page))
      // this.isPcLayout = page.page_num
      // this.pageType = page.page_num
      this.$bus.emit('layoutContentChange', undefined, 'page', page)
    },
    // 新增子布局
    addChildLayout () {
      let data = JSON.parse(JSON.stringify(this.custom_layout))
      data.pageNum = this.pageList[this.pageList.length - 1].page_num + 1
      data.pageName = '子页面' + (this.pageList[this.pageList.length - 1].page_num + 1)
      data.enableLayout = {}
      data.disableLayout = {rows: []}
      data.enableLayout.layout = data.layout
      delete data.layout
      this.ajaxCommitLayout(data)
    },
    // 修改子页面名称
    editLayout (page) {
      this.pageList.map((item) => {
        item.edit_show = true
      })
      if (page.page_name && page.page_num === this.activePage.page_num) {
        let layout = JSON.parse(JSON.stringify(this.child_custom_layout))
        let layoutCommit = JSON.parse(JSON.stringify(this.child_custom_layout))
        layout.pageName = page.page_name
        layoutCommit.pageName = page.page_name
        this.setChildLayout(layout)
        layoutCommit.enableLayout = {}
        layoutCommit.disableLayout = this.child_unused_layout
        layoutCommit.enableLayout.layout = layoutCommit.layout
        delete layoutCommit.layout
        this.ajaxCommitLayout(layoutCommit)
      } else if (page.page_name && page.page_num !== this.activePage.page_num) {
        let data = {bean: this.$route.query.bean, operationType: 1, pageNum: page.page_num}
        this.ajaxAllLayout(page, ajaxGetRequest(data, 'layout/getEnableLayout'), ajaxGetRequest(data, 'layout/getDisableLayout'))
      } else {
        page.page_name = this.editBeforeName
      }
    },
    // 保存按钮
    saveLayout (type) {
      if (type === -1) {
        // app列表
      } else if (type === 0) {
        // 主布局
        let layout = JSON.parse(JSON.stringify(this.preview_layout))
        let layoutCommit = JSON.parse(JSON.stringify(this.preview_layout))
        if (!layoutCommit.title) {
          this.$message({
            type: 'warning',
            message: '模块名称不能为空!',
            duration: 1000
          })
        } else if (this.fieldIsRepetition(layoutCommit.layout) === 0) {
          this.$message({
            type: 'warning',
            message: '字段名称不能为空!',
            duration: 1000
          })
        } else if (Array.isArray(this.fieldIsRepetition(layoutCommit.layout))) {
          this.$message({
            type: 'warning',
            message: `字段名称【${this.fieldIsRepetition(layoutCommit.layout)[0]}】重复!`,
            duration: 1000
          })
        } else {
          this.$bus.emit('headerSendModuleName')
          this.setLayout(layout)
          layoutCommit.enableLayout = {}
          layoutCommit.disableLayout = this.enable_layout
          layoutCommit.enableLayout.layout = layoutCommit.layout
          delete layoutCommit.layout
          this.ajaxCommitLayout(layoutCommit)
        }
      } else {
        // 子布局
        let layout = JSON.parse(JSON.stringify(this.child_custom_layout))
        let layoutCommit = JSON.parse(JSON.stringify(this.child_custom_layout))
        if (!layoutCommit.title) {
          this.$message({
            type: 'warning',
            message: '模块名称不能为空!',
            duration: 1000
          })
        } else if (this.fieldIsRepetition(layoutCommit.layout) === 0) {
          this.$message({
            type: 'warning',
            message: '字段名称不能为空!',
            duration: 1000
          })
        } else if (Array.isArray(this.fieldIsRepetition(layoutCommit.layout))) {
          this.$message({
            type: 'warning',
            message: `字段名称【${this.fieldIsRepetition(layoutCommit.layout)[0]}】重复!`,
            duration: 1000
          })
        } else {
          this.setChildLayout(layout)
          this.$bus.emit('setChildLayout', JSON.parse(JSON.stringify(this.child_custom_layout)))
          layoutCommit.enableLayout = {}
          layoutCommit.disableLayout = this.child_unused_layout
          layoutCommit.enableLayout.layout = layoutCommit.layout
          delete layoutCommit.layout
          this.ajaxCommitLayout(layoutCommit)
        }
      }
    },
    // 删除子页面布局
    delPageLayout (page) {
      this.$confirm('此操作将永久删除该页面, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let data = {id: page.id, pageNum: page.page_num}
        this.ajaxDelChildLayout(data)
      }).catch(() => {
      })
    },
    // 判断字段是否重复
    fieldIsRepetition (list) {
      let name = []
      list.map((item) => {
        item.rows.map((item2) => {
          name.push(item2.label)
          if (item2.type === 'subform') {
            item2.componentList.map((item3) => {
              name.push(item3.label)
            })
          }
        })
      })
      if (name.includes('')) {
        return 0
      } else if (!name.includes('') && JSON.stringify(new Set(name)) === JSON.stringify(name)) {
        return 2
      } else {
        return tool.arrayRepeation(Array.from(name))
      }
    },
    // 获取全部布局
    ajaxAllLayout (page, ...ajaxs) {
      axios.all([...ajaxs]).then(
        axios.spread((...res) => {
          // 两个请求现在都执行完成
          console.log(res)
          let layoutEnable = res[0].data.data
          let layoutDisable = res[1].data.data
          layoutEnable.layout.map((item, index) => {
            item.dragOptions = dragOptions
            if (item.name === 'systemInfo') {
              // 系统信息分栏不显示
              layoutEnable.layout.splice(index, 1)
            }
            item.rows.map((e, index2) => {
              // 去掉组件焦点
              e.active = '0'
            })
          })
          layoutEnable.pageName = page.page_name
          layoutEnable.pageNum = page.page_num
          layoutEnable.enableLayout = {}
          layoutEnable.disableLayout = layoutDisable
          layoutEnable.enableLayout.layout = layoutEnable.layout
          delete layoutEnable.layout
          this.ajaxCommitLayout(layoutEnable)
        })).catch((err) => {
        console.log(err)
      })
    },
    // 获取子页面列表
    ajaxGetChildLayout () {
      let data = {bean: this.$route.query.bean}
      HTTPServer.getChildLayout(data, 'Loading').then((res) => {
        res.map((item) => {
          item.edit_show = true
        })
        this.pageList = res
      })
    },
    // 提交布局
    ajaxCommitLayout (data) {
      HTTPServer.submitLayout(data, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '保存成功!',
          duration: 1000
        })
        this.activePages = JSON.parse(JSON.stringify(this.activePage))
        this.ajaxGetChildLayout()
      }).catch((err) => {
        if (err.code === 1000) {
          console.log(this.activePage, this.activePages)
          this.activePage = this.activePages
          this.ajaxGetChildLayout()
        }
      })
    },
    // 删除布局
    ajaxDelChildLayout (data) {
      HTTPServer.deleteModule(data, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '删除成功!',
          duration: 1000
        })
        this.ajaxGetChildLayout()
        this.activePage = {page_num: 0, page_name: '标准页面'}
        this.isPcLayout = 0
        this.pageType = 0
      })
    },
    ...mapMutations({
      setLayout: 'CUSTOM_LAYOUT',
      setChildLayout: 'CHILD_CUSTOM_LAYOUT'
    })
  },
  computed: {
    ...mapGetters([
      'child_custom_layout',
      'child_unused_layout',
      'custom_layout',
      'preview_layout',
      'enable_layout'
    ]),
    ...mapState({
      appListLayout: state => state.custom.app_list_layout
    })
  },
  watch: {
    activePage: {
      handler: function (val, oldval) {
        console.log(val)
      },
      deep: true
    }
  }
}
</script>

<style lang="scss">
.custom-content-wrip{
  width: 1450px;
  height: 100%;
  margin: auto;
  text-align: center;
  .titles{
    height: 60px;
    line-height: 60px;
    padding: 0 20px;
    background: #FFFFFF;
    box-shadow: 0 0 2px 0 rgba(0,0,0,0.50);
    text-align: left;
    >a{
      display: inline-block;
      margin: 0 40px 0 0;
      cursor: pointer;
      >.iconfont{
        font-size: 30px;
        color: #69696C;
        margin: 0 10px 0 0;
      }
      >span{
        font-size: 16px;
        color: #69696C;
        vertical-align: top;
      }
      >.el-input{
        width: 100px;
        vertical-align: top;
        input{
          height: 30px;
          line-height: 30px;
        }
      }
      >.icon-module-menu-1{
        font-size: 16px;
        vertical-align: super;
      }
      .el-popover__reference{
        vertical-align: text-top;
      }
    }
    >.el-button{
      float: right;
      width: 90px;
      margin: 16px 0 0 16px;
    }
  }
}
.el-popover.page-more-popover{
  min-width: 100px;
  padding: 12px 0 5px;
  .content{
    .item{
      display: flex;
      align-items: center;
      height: 34px;
      line-height: 34px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      &:hover{
        background: #F0F0F0;
        i{
          visibility: visible;
        }
      }
      span{
        flex: 1;
        padding:0 5px 0 20px;
        cursor: pointer;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .el-input{
        background: #FBFBFB;
        border: 1px solid #E7E7E7;
        box-shadow: 0 2px 4px 0 rgba(96,96,96,0.24);
        border-radius: 2px;
        input{
          height: 34px;
          line-height: 34px;
        }
      }
      i{
        flex: 0 0 16px;
        visibility: hidden;
        color: #1989FA;
        margin: 0 10px 0 0;
        font-size: 16px;
        cursor: pointer;
      }
    }
    .item-button{
      text-align: center;
    }
  }
}
</style>
