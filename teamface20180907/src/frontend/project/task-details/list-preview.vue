<template>
  <div class="list-preview-warp">
    <div class="cardItem" v-for="(item,index) in dataList" :key="index">
      <div class="title" @click="openList(item)">
        <i class="iconfont icon-htmal5icon03" v-if="item.isactive"></i>
        <i class="el-icon-caret-right" v-else></i>
        <span>{{item.title}}</span>
      </div>
      <div v-for="(v, k) in item.tasks" :key="k" class="text-item" v-if="item.isactive">
        <div class="cardBoxFlex" @click="openPersonalDetails(v,item)">
          <div class="itemCardRight">
            <div class="personnelItem" v-if="v.personnel_execution&&JSON.stringify(v.personnel_execution)!=='[]'" :class="!v.personnel_execution[0].picture||v.personnel_execution[0].picture=='null'?'activeName':''">
              <img v-if="v.personnel_execution[0].picture&&v.personnel_execution[0].picture!='null'" :src="v.personnel_execution[0].picture + '&TOKEN=' + token" :title="v.personnel_execution[0].name">
              <span v-if="!v.personnel_execution[0].picture||v.personnel_execution[0].picture=='null'">{{sliceName(v.personnel_execution[0].name)}}</span>
            </div>
            <div v-if="v.picklist_tag && v.picklist_tag.length > 1">
              <el-popover placement="top" trigger="hover" popper-class="personalListpreview">
                <span v-for="(v1, k1) in v.picklist_tag" :key="k1" class="topTagsList" :style="{'background':v1.colour}">
                  <span class="topTagsList" v-text="v1.name">这是一段内容这是一段内容确定删除吗？</span>
                </span>
                <i class="iconfont icon-Rectangle" slot="reference"></i>
              </el-popover>
            </div>
            <div v-if="v.picklist_tag && v.picklist_tag.length > 0" class="topTagsListNext">
              <div :style="{'background':v.picklist_tag[0].colour}">{{v.picklist_tag[0].name}}</div>
            </div>
            <!-- 子任务和完成的子任务数 -->
            <div class="subTaskCount" v-if="v.subtotal"><i class="iconfont icon-zirenwu"></i>
            <span>{{v.subfinishtotal}}</span>
            /
            <span>{{v.subtotal}}</span>
            </div>
            <div v-if="item.title==='个人任务'" class="timeStyleColor" :class="v.complete_status=='0'&&judgeTimeOld(v.datetime_deadline)?'colorisRed':''">
              <span class="subTaskCount"><i v-if="v.datetime_starttime || v.datetime_deadline" class="iconfont icon-shenpi-shijian"></i></span>
              <el-tooltip class="item subTaskCount" effect="dark" placement="top">
                <div slot="content" class="showTimeTooltip">开始时间 {{v.datetime_starttime | formatDate('yyyy-MM-dd HH:mm')}}</div>
                <span v-if="v.datetime_starttime">{{editorTime(v.datetime_starttime)}}</span>
              </el-tooltip>
              <span v-if="v.datetime_starttime&&v.datetime_deadline">~</span>
              <el-tooltip class="item subTaskCount" effect="dark" placement="top">
                <div slot="content"  class="showTimeTooltip">截止时间 {{v.datetime_deadline | formatDate('yyyy-MM-dd HH:mm')}}</div>
                <span v-if="v.datetime_deadline">{{editorTime(v.datetime_deadline)}}</span>
              </el-tooltip>
            </div>
            <div v-else class="timeStyleColor" :class="v.complete_status=='0'&&judgeTimeOld(v.end_time)?'colorisRed':''">
              <span class="subTaskCount"><i v-if="v.datetime_starttime || v.end_time" class="iconfont icon-shenpi-shijian"></i></span>
              <el-tooltip class="item subTaskCount" effect="dark" placement="top">
                <div slot="content" class="showTimeTooltip">开始时间 {{v.datetime_starttime | formatDate('yyyy-MM-dd HH:mm')}}</div>
                <span v-if="v.datetime_starttime">{{editorTime(v.datetime_starttime)}}</span>
              </el-tooltip>
              <span v-if="v.datetime_starttime&&v.end_time">~</span>
              <el-tooltip class="item subTaskCount" effect="dark" placement="top">
                <div slot="content"  class="showTimeTooltip">截止时间 {{v.end_time | formatDate('yyyy-MM-dd HH:mm')}}</div>
                <span v-if="v.end_time">{{editorTime(v.end_time)}}</span>
              </el-tooltip>
            </div>
            <div class="activationCount" v-if="v.activate_number||v.complete_number">
              <!-- <span>{{v.from===1?v.activate_number:v.complete_number}}</span> -->
              <el-tooltip class="item subTaskCount" effect="dark" placement="top">
                <div slot="content" class="showTimeTooltip">激活次数</div>
                <span>{{v.from===1?v.activate_number:v.complete_number}}</span>
              </el-tooltip>
            </div>
          </div>
          <span class="taskStatus">
            <div class="checkedActive" @click.stop="complateOrActive(v,item)" :class="v.complete_status==='1'?'complate':''">
              <i class="iconfont icon-pc-paper-optfor" v-show="v.complete_status==='1'"></i>
            </div>
          </span>
          <span class="taskName" :style="{'color':v.complete_status==='1'?'#B3B4B5':''}">{{v.text_name || v.task_name}}</span>
        </div>
      </div>
    </div>
    <!-- 激活或者编辑原因填写 -->
    <el-dialog :visible.sync="activtionAndEditor" width="400px" id="activtionListPreview" append-to-body>
      <div class="titleHeader">
        <!-- <span style="color:red;">*</span> -->
        <span>激活原因</span>
      </div>
      <div>
        <el-input type="textarea" rows="6" v-model="activationReason"></el-input>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="activtionAndEditor = false;activationReason=''">取 消</el-button>
        <el-button type="primary" @click="saveRElationDetail(1)">激 活</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'ListPreviewWarp',
  data () {
    return {
      dataList: [{title: '个人任务', isactive: true, tasks: []}],
      token: '',
      isActiveData: {},
      project_id: '',
      activationReason: '',
      activtionAndEditor: false,
      parentData: {},
      projectBaseInfo: {}, // 缓存项目基本信息
      TaskAuthRoleInfoList: [], // 项目任务权限
      rolTaskList: [] // 项目中的角色
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
  },
  mounted () {
    this.$bus.on('personalTaskData', (res) => {
      let data = JSON.parse(res)
      data.dataList.forEach((v, k) => {
        v.isactive = false
        v.isactive = k === 0
        // if (k === 0) {
        //   v.tasks.map((v2, k2) => {
        //     v2.start_time_task = this.getStartTimeTask(v2)
        //   })
        // }
      })
      this.dataList = data.dataList
      this.searchData = data.searchData
    })
    this.getProjectLayout()
  },
  methods: {
    getData (data) { // 获取任务列表
      HTTPServer.queryTaskListByCondition(data, 'Loading').then((res) => {
        // res[0].isactive = true
        // this.dataList[0] = res[0]
        res.map((val, key) => {
          if (key === 0) {
            val.isactive = true
          } else {
            val.isactive = false
          }
        })
        this.dataList = res
      })
    },
    complateOrActivePerRole (v) { // 获取个人在个人任务中的角色
      HTTPServer.getTaskRoleFromPersonelTask({taskId: v.id, fromType: 0}, 'loading').then((res) => {
        if (res.role === 0 || res.role === 1) {
          let html = ''
          if (v.complete_status === '1') {
            html = '确定要激活此任务吗'
          } else {
            html = '确定要完成此任务吗'
          }
          this.$confirm(html, '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            HTTPServer.updateForFinish({taskId: v.id}, 'Loading').then((res) => {
              this.getData(this.searchData)
              this.$message({
                type: 'success',
                message: '操作完成!'
              })
            })
          }).catch(() => { console.log('') })
        } else {
          this.$message({
            message: '你无权进行此操作！',
            type: 'warning'
          })
        }
      })
    },
    complateOrActive (v, item) { // 完成或激活任务
      if (v.from !== 1) { // 项目任务完成或者激活
        this.project_id = v.project_id
        this.parentData = item
        this.getTaskAuth(v)
        return false
      }
      this.complateOrActivePerRole(v)
    },
    openList (item) { // 打开任务列表，获取项目任务
      if (!item.isactive && item.title !== '个人任务') {
        this.projectId = item.projectId
        HTTPServer.findTaskListByProjectId({projectId: item.projectId, queryType: this.searchData.queryType}, 'Loading').then((res) => {
          item.tasks = res
        })
      }
      item.isactive = !item.isactive
    },
    getStartTimeTask (v) { // 获取开始时间
      this.layout.map((val, key) => {
        if (val.label === '开始时间') {
          return v[val.name]
        }
      })
    },
    getProjectLayout () { // 获取任务自定义布局
      HTTPServer.getAllLayout({'bean': 'project_custom'}, 'loading').then((res) => {
        this.layout = res.enableLayout.rows
      })
    },
    editorTime (time) {
      if (time) {
        let newtime = tool.formatDate(time, 'yyyy-MM-dd HH:mm')
        return newtime.slice(5)
      }
    },
    getTaskAuth (value) { // 获取项目任务权限
      HTTPServer.queryTaskAuthList({id: value.project_id}, 'Loading').then((res) => {
        this.TaskAuthRoleInfoList = res
        this.getRolAuth(value)
      })
    },
    getRolAuth (v) { // 获取用户任务角色
      let senddata = {
        id: v.project_id,
        taskId: v.id,
        typeStatus: 1,
        all: 0
      }
      HTTPServer.queryCollaboratorList(senddata, 'Loading').then((res) => { // 获取用户任务角色
        this.rolTaskList = res.dataList
        if (this.judgeTaskAuth(2)) {
          this.activationReason = ''
          let data = {
            id: v.id,
            completeStatus: 1 // 0未已完成状态，1已完成状态
          }
          if (v.complete_status === '1') {
            data.completeStatus = 0
            this.getBaseSetting(v.project_id, data, v)
            this.isActiveData = v
          } else {
            this.$confirm('是否确定完成此任务?', '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              HTTPServer.updateTaskStatus(data, 'Loading').then((res) => {
                HTTPServer.findTaskListByProjectId({projectId: this.projectId, queryType: this.searchData.queryType}, 'Loading').then((res) => {
                  this.dataList.map((v1, k1) => {
                    if (this.parentData.id === v1.id) {
                      v1.tasks = res
                    }
                  })
                })
              })
            }).catch(() => { console.log('') })
          }
        } else {
          this.$message({
            message: '无权进行此操作！',
            type: 'warning'
          })
        }
      })
    },
    saveRElationDetail () { // 激活原因保存
      let senddata = {
        id: this.isActiveData.id,
        remark: this.activationReason, // 激活原因
        completeStatus: 0 // 0未已完成状态，1已完成状态
      }
      HTTPServer.updateTaskStatus(senddata, 'Loading').then((res) => {
        HTTPServer.findTaskListByProjectId({projectId: this.projectId, queryType: this.searchData.queryType}, 'Loading').then((res) => {
          this.dataList.map((v1, k1) => {
            if (this.parentData.id === v1.id) {
              v1.tasks = res
            }
          })
        })
      })
    },
    getBaseSetting (id, data, value) { // 获取项目基本设置信息判断是否要填写就激活原因
      HTTPServer.projectQueryById({id: id}, 'Loading').then((res) => {
        if (res.project_progress_status === '1') {
          this.activtionAndEditor = true
        } else {
          this.$confirm('是否确定激活此任务?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            HTTPServer.updateTaskStatus(data, 'Loading').then((res) => {
              HTTPServer.findTaskListByProjectId({projectId: this.projectId, queryType: this.searchData.queryType}, 'Loading').then((res) => {
                this.dataList.map((v1, k1) => {
                  if (this.parentData.id === v1.id) {
                    v1.tasks = res
                  }
                })
              })
            })
          }).catch(() => { console.log('') })
        }
        this.projectBaseInfo = res
      })
    },
    judgeTaskAuth (number) { // 判断任务权限  // 0创建人 1执行人 2协作人
      let status = false
      this.rolTaskList.forEach((v, k) => {
        this.TaskAuthRoleInfoList.forEach((v1, k1) => {
          if (v.project_task_role === v1.role_type) {
            for (let i in v1) {
              if (i.indexOf('auth') !== -1) {
                if (v1['auth_' + number] === '1') {
                  status = true
                }
              }
            }
          }
        })
      })
      return status
    },
    judgeTimeOld (time) { // 判断任务有无过期
      if (time) {
        let newtime = new Date()
        return newtime.getTime() > time
      }
    },
    openPersonalDetails (v, item) { // 打开个人任务或，项目任务详情
      sessionStorage.setItem('chooseProName', JSON.stringify({name: item.title}))
      v.isPeroOpenProTask = true
      this.$bus.$emit('diffTypesDetails', JSON.stringify(v))
    },
    changeOpenOrHidden () {},
    sliceName (name) {
      if (name) {
        return name.slice(-2)
      }
    }
  },
  beforeDestroy () { // 注销
    this.$bus.off('personalTaskData')
  }
}
</script>

<style lang="scss" scoped>
.list-preview-warp{
  padding-top:10px;
  .clearfix{
    span{font-size: 20px;font-weight: bold; }
  }
  .cardItem {
    margin-bottom: 10px;
    .title{
      i.el-icon-caret-right{font-size:15px;}
      i.icon-htmal5icon03{
        margin-right:3px;
      }
      height:50px;line-height:50px;padding:0 8px;span{color:#666666;}i{font-size:12px;color:#999999;}cursor: pointer;i.open{transform: rotate(40deg);}
    }
    .text-item{
      .cardBoxFlex{
        padding:0 20px 0 10px;border-bottom:1px solid #E9E9E9;margin:0 10px;height:50px;line-height:50px;
        &:hover{
          cursor: pointer;border:0;box-shadow: 0 -2px 4px 0 #E7E7E7, 0 2px 4px 0 rgba(185,185,185,0.50);border-radius: 4px;
        }
        .itemCardRight{
          float:right;height:50px;line-height:50px;>div{margin-right:10px;float: right;color:#BBBBC3;font-size:12px;span,i.icon-shenpi-shijian{font-size:12px;}i{font-size:15px;}}
          .personnelItem{
            border-radius: 50%;height:30px;width:30px;line-height: 30px;color:#fff;img{vertical-align: sub;border-radius: 50%;width:100%;height:100%;}text-align: center;margin-top:10px;
          }
          .personnelItem.activeName{background: #60CDFF;}
          .topTagsListNext{
            div{
              color:#fff;padding:0 10px;
              max-width:180px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;border-radius: 4px;
              height:20px;line-height: 20px;margin-top:15px;
            }
          }
          .activationCount{
            >span{padding:0 2px;background: #F5A623;color:#fff;}
          }
          .timeStyleColor.colorisRed{span{color:red;}}
        }
      }
      .taskStatus{
        margin:0 6px 0 0;position: relative;top:3px;width:16px;
        >div.checkedActive{
          height:16px;width:16px;border:1px solid #D9D9D9;border-radius: 2px;position: relative;display: inline-block;
          i{font-size:12px;position: absolute;color:#fff;top:0;left:0;line-height:16px;transform: scale(.7);}
        }
        >div.checkedActive.complate{background:#1890FF;border:0;}
      }
      .taskStatus,.taskName{
        float:left;
      }
      .taskName{
        max-width: 400px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}
</style>
<style lang="scss">
.list-preview-warp{}
.personalListpreview{
  .topTagsList{
    padding:0 6px;color:#fff;border-radius:4px;margin-right:5px;
  }
}
.showTimeTooltip{font-size:12px;}
#activtionListPreview{
  .el-dialog__header{display:none;}
  .titleHeader{color:#9B9B9B;margin-bottom:10px;}
}
</style>
