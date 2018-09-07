<template>
<!-- 邮件的公共列表 -->
  <div class="Email-list-wrap">
    <div class="Null" v-if="tableList.length===0"><img src="./img/Bitmap.png" alt=""></div>
    <div class="content" v-else>
      <el-table ref='multipleTable' :data='tableList' tooltip-effect='dark' style='width: 100%' @selection-change='handleSelectionChange' @row-click="rowSkip">
        <!-- 多选框 -->
        <el-table-column type='selection' width='55'>
        </el-table-column>
        <!-- 状态图标 -->
        <el-table-column  width='130'>
          <template  slot-scope='scope'>
             <!-- approval_status // 2 审批通过 3 审批驳回 4 已撤销 10 没有审批 -->
             <!-- mail_box_id // 1收件箱 2已发送 3草稿箱 4已删除 5垃圾箱 -->
             <!-- 草稿箱和已删除没有已读未读状态 -->
            <i class="iconfont icon-youjian" v-if="scope.row.read_status==='0' && scope.row.mail_box_id !== 3 && scope.row.mail_box_id !== 4" title="未读邮件" style="color: #EC9829;margin-right:5px;font-size:20px;"></i>
            <i class="iconfont icon-Shape" v-if="scope.row.is_emergent==='1'" title="紧急邮件" style="color: #F94C4A;"></i>
            <!-- 草稿图标只有草稿箱和已删除能看到 -->
            <i class="iconfont icon-caogao" v-if="((scope.row.draft_status==='1' && scope.row.mail_box_id===4) || (scope.row.draft_status==='1' && scope.row.mail_box_id===3))" title="草稿"></i>
            <!-- 部分成功和失败 已发送显示 -->
            <img src="./img/Group 24(2).png" v-if="scope.row.send_status==='2' && scope.row.mail_box_id===2"  title="部分成功">
            <img src="./img/Group 24(1).png"  v-if="scope.row.send_status==='0' && scope.row.mail_box_id===2" title="发送失败">
            <!-- 审批图标只有草稿箱和已删除能看到 -->
            <i class="iconfont icon-tongguo" v-if="scope.row.approval_status==='2' && (scope.row.mail_box_id===3 || scope.row.mail_box_id===4)" title="审批通过" style="color: #78C06E;"></i>
            <i class="iconfont icon-bohui" v-if="scope.row.approval_status==='3' && (scope.row.mail_box_id===3 || scope.row.mail_box_id===4)" title="审批驳回" style="color: #FFC057;"></i>
          </template>
        </el-table-column>
        <!-- 主题 -->
        <el-table-column label='主题'>
          <template slot-scope='scope'>
            <span class="theme">{{ scope.row.subject }}</span>
          </template>
        </el-table-column>
        <!-- 邮件标签 -->
        <el-table-column >
          <template  slot-scope='scope'>
            <span class="mail_tags" v-if="scope.row.mail_colors" v-for="(item,index) in scope.row.mail_tags" :key="index" :style="{'background-color':scope.row.mail_colors[index]}">
              {{item}}
            </span>
          </template>
        </el-table-column>
        <!-- 发件人(已发送是收件人) -->
        <el-table-column :label="navId.id === 2 ? '收件人':'发件人'">
          <template slot-scope='scope'>
            <span v-if="scope.row.to_recipients.length > 1">
              {{navId.id !== 2 ? scope.row.from_recipient : scope.row.to_recipients[0].mail_account+'...'}}
            </span>
            <span v-else>
              {{navId.id !== 2 ? scope.row.from_recipient : scope.row.to_recipients[0].mail_account}}
            </span>
          </template>
        </el-table-column>
        <!-- 时间 -->
        <el-table-column label='时间'>
          <template slot-scope='scope'>{{ scope.row.timer_task_time || scope.row.create_time | formatDate('yyyy-MM-dd HH:mm:ss') }}<i class="iconfont icon-time" v-if="scope.row.timer_status==='1' && scope.row.mail_box_id===3" style="margin-left:15px;font-size:23px;color:#FF7878;"></i></template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 分页 -->
    <div class="Pagination">
      <el-pagination @size-change='handleSizeChange'
        @current-change='handleCurrentChange'
        :current-page='pageNum'
        :page-sizes='[10, 20, 50, 100]'
        :page-size='pageSize'
        layout='total, sizes, prev, pager, next, jumper'
        :total='totalNumber'>
      </el-pagination>
    </div>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'EmailList',
  // recordObj 来往记录的账户个类型 {'type': num, 'accountName': data}
  // listContent 列表数组
  props: ['listContent', 'navId', 'recordObj'],
  data () {
    return {
      pageSize: 20,
      pageNum: 1,
      ParticularsContent: {},
      tableList: [],
      judge: false,
      totalNumber: 0,
      id: '',
      recordObjTemp: {}
    }
  },
  mounted () {
    this.recordObjTemp = JSON.parse(JSON.stringify(this.recordObj))
    if (this.listContent.pageInfo) {
      this.tableList = this.listContent.dataList
      this.pageSize = this.listContent.pageInfo.pageSize
      this.pageNum = this.listContent.pageInfo.pageNum
      this.totalNumber = this.listContent.pageInfo.totalRows
    }

    // 监听正常列表请求,recordObj
    this.$bus.off('recordObj')
    this.$bus.on('recordObj', val => {
      this.recordObjTemp = val
    })
    // 监听正常列表请求,从而复位recordObjTemp
    this.$bus.off('noRecordListRequest')
    this.$bus.on('noRecordListRequest', value => {
      this.recordObjTemp = {}
    })

    // 监听其他组件的跳页请求
    this.$bus.off('jumpPageRequest')
    this.$bus.on('jumpPageRequest', value => {
      // this.recordObjTemp = {}
      this.handleCurrentChange(value)
    })

    // 监听其他组件的页数量请求
    this.$bus.off('jumpPageSizeRequest')
    this.$bus.on('jumpPageSizeRequest', value => {
      // this.recordObjTemp = {}
      this.handleSizeChange(value)
    })
  },
  watch: {
    listContent () {
      console.log(this.listContent, 'listContent')
      this.tableList = this.listContent.dataList
      var pageInfo = this.listContent.pageInfo
      this.pageSize = pageInfo.pageSize
      this.pageNum = pageInfo.pageNum
      this.totalNumber = pageInfo.totalRows
    }
  },
  methods: {
    // 跳转到第几页
    handleCurrentChange (val) {
      this.pageNum = val
      // 将页数发送给各组件使用
      this.$bus.emit('currentPageNum', this.pageNum)
      if (this.navId.label) {
        if (JSON.stringify(this.recordObjTemp) !== '{}') {
          // 获取来往记录列表
          this.AndRecords(this.recordObjTemp.type, this.recordObjTemp.accountName)
        } else {
          // 标签获取列表
          this.getEmailLabelList()
        }
      } else if (JSON.stringify(this.recordObjTemp) !== '{}') {
        // 获取来往记录列表
        this.AndRecords(this.recordObjTemp.type, this.recordObjTemp.accountName)
      } else {
        // 其他组件获取列表
        this.getEmailList()
      }
    },
    // 每页的条数改变
    handleSizeChange (val) {
      this.pageSize = val
      // 将页数发送给各组件使用
      this.$bus.emit('currentPageSize', this.pageSize)
      if (this.navId.label) {
        if (JSON.stringify(this.recordObjTemp) !== '{}') {
          // 获取来往记录列表
          this.AndRecords(this.recordObjTemp.type, this.recordObjTemp.accountName)
        } else {
          // 标签获取列表
          this.getEmailLabelList()
        }
      } else if (JSON.stringify(this.recordObjTemp) !== '{}') {
        // 获取来往记录列表
        this.AndRecords(this.recordObjTemp.type, this.recordObjTemp.accountName)
      } else {
        // 其他组件获取列表
        this.getEmailList()
      }
    },
    // 列表选择(多选框)
    handleSelectionChange (val) {
      this.$bus.emit('listenEmailParticulars', val)
    },
    // 行选择(各个组件点击选项,跳转详情)
    rowSkip (val) {
      console.log(val)
      // 如果是收件箱点击,则提示回执操作
      if (val.mail_box_id === 1 && val.type === undefined && val.is_notification === '1') {
        // 弹窗回执
        this.$confirm(` <${val.from_recipient}> 要求发送 "已读回执" , 是否发送 ?`, '提示', {
          distinguishCancelAndClose: true,
          confirmButtonText: '发送',
          cancelButtonText: '不发送',
          type: 'warning'
        })
          .then(() => {
            // 调用发送回执接口
            val['isSend'] = '1'
            this.$http.receiptMail(val).then(res => {
            })
            // 进邮件详情
            this.$bus.emit('listenEmailParticularsId', val)
            setTimeout(() => {
              // 刷新收件箱未读数,草稿箱总数
              this.$bus.emit('listenToGetTitleCount', true)
            }, 300)
          })
          .catch(action => {
            if (action === 'cancel') {
              // 调用不发送回执接口
              val['isSend'] = '0'
              this.$http.receiptMail(val).then(res => {})
              // 进邮件详情
              this.$bus.emit('listenEmailParticularsId', val)
              setTimeout(() => {
                // 刷新收件箱未读数,草稿箱总数
                this.$bus.emit('listenToGetTitleCount', true)
              }, 300)
            }
          })
      } else {
        // 进邮件详情
        this.$bus.emit('listenEmailParticularsId', val)
        setTimeout(() => {
          // 刷新收件箱未读数,草稿箱总数
          this.$bus.emit('listenToGetTitleCount', true)
        }, 300)
      }
    },
    // 标签获取列表数据
    getEmailLabelList () {
      if (this.$route.query.id) {
        // 获取当前route上的id
        var jsondata = {'pageNum': this.pageNum, 'pageSize': this.pageSize, 'tagId': this.$route.query.id}
        HTTPServer.getEmailLabelList(jsondata, 'Loading').then((res) => {
          this.tableList = res.dataList
          for (var i = 0; i < this.tableList.length; i++) {
            this.tableList[i].mail_tags = this.tableList[i].mail_tags.split(',')
            this.tableList[i].mail_colors = this.tableList[i].mail_colors.split(',')
          }
        })
      }
    },
    // 获取列表数据
    getEmailList () {
      var jsondata = {'pageNum': this.pageNum, 'pageSize': this.pageSize, 'boxId': this.navId.id}
      HTTPServer.getEmailListContent(jsondata, 'Loading').then((res) => {
        this.tableList = res.dataList
        for (var i = 0; i < this.tableList.length; i++) {
          this.tableList[i].mail_tags = this.tableList[i].mail_tags.split(',')
          this.tableList[i].mail_colors = this.tableList[i].mail_colors.split(',')
        }
      })
    },
    // 获取来往记录的列表数据
    AndRecords (num, data) {
      var jsondata = {'pageNum': this.pageNum, 'pageSize': this.pageSize, 'accountName': data, 'type': num}
      HTTPServer.getEmailAndRecordsList(jsondata, 'Loading').then((res) => {
        // this.SendToMeName = data
        this.tableList = res.dataList
        for (var i = 0; i < this.tableList.length; i++) {
          this.tableList[i].mail_tags = this.tableList[i].mail_tags.split(',')
          this.tableList[i].mail_colors = this.tableList[i].mail_colors.split(',')
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.Email-list-wrap{
  height: 100%;
  border-bottom: 1px solid #e6ebf5;
  .Null{
     height: 100%;
     text-align: center;
    img{
      margin-top: 10%;
    }
  }
  .content{
    height: 100%;
    overflow: auto;
    border-bottom: 1px solid #eee;
    .theme{
      display:inline-block;
      overflow: hidden;
      width:100%;
      white-space: nowrap;
      text-overflow: ellipsis;
    }
  }
  .Pagination{
    float: right;
    padding-top: 20px;
  }
  .mail_tags{
    display:inline-block;
    padding:0 5px;
    color:#fff;
    font-size:12px;
    border-radius: 3px;
    margin-right:5px;
  }
}
</style>
