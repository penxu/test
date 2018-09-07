<template>
  <div class="line-card-task-warp">
    <!-- 任务 -->
    <div class="taskListDetail" :class="cloumn.complete_status=='1'||cloumn.completeStatus==1?'opacityClassShow':''" v-if="cloumn.dataType===2||cloumn.from==1" :style="{'border-left-color': cloumn.picklist_priority&&cloumn.picklist_priority.length>0?cloumn.picklist_priority[0].color:''}">
      <div></div>
      <div><i class="iconfont icon-yidong"></i></div>
      <div class="taskOpenOrclose">
        <div class="checkedActive" @click.stop="completeStatus(cloumn,listItem)" :class="cloumn.complete_status=='1'||cloumn.completeStatus==1?'chooseCheckedActive':''">
          <i v-if="cloumn.complete_status=='1'||cloumn.completeStatus==1" class="iconfont icon-pc-paper-optfor"></i>
        </div>
      </div>
      <div style="margin-right:0;">
        <div class="taskoverHidden">
          <div class="work-logo-next">
            <span class="taskSuboverHidden" v-if="cloumn.datetime_starttime&&cloumn.datetime_deadline" v-text="cloumn.text_name" :style="{color:cloumn.complete_status=='1'||cloumn.completeStatus==1?'#999':''}" :class="isjudageOverYear(cloumn.datetime_starttime, cloumn.datetime_deadline)?'':'overYear'">孤独感豆腐干地</span>
            <span v-else class="taskSuboverHidden overYearnew" v-text="cloumn.text_name" :style="{color:cloumn.complete_status=='1'||cloumn.completeStatus==1?'#999':''}">孤独感豆腐干地</span>
            <span class="subTaskCount" v-show="cloumn.sub_task_count"><i class="iconfont icon-zirenwu"></i><span>{{cloumn.sub_task_complete_count}}</span>/<span>{{cloumn.sub_task_count}}</span></span>
            <!-- <template>
              <span class="subTaskCount" style="margin-right:5px;" :style="{color:(cloumn.complete_status=='0'||cloumn.completeStatus==0)&&judgeTimeOld(cloumn.datetime_deadline)?'red':''}">
                <i v-if="cloumn.datetime_deadline || cloumn.datetime_starttime" class="iconfont icon-shenpi-shijian"></i>
              </span>
              <el-tooltip class="item subTaskCount" effect="dark" placement="top">
                <div slot="content" class="showTimeTooltip">
                  开始时间 <span class="showTimeTooltip">{{cloumn.datetime_starttime | formatDate('yyyy-MM-dd HH:mm')}}</span>
                </div>
                <span v-if="cloumn.datetime_starttime" :style="{color:(cloumn.complete_status=='0'||cloumn.completeStatus==0)&&judgeTimeOld(cloumn.datetime_deadline)?'red':''}">{{editorTime(cloumn.datetime_starttime)}}</span>
              </el-tooltip>
              <span class="subTaskCount" v-if="cloumn.datetime_starttime&&cloumn.datetime_deadline" :style="{color:(cloumn.complete_status=='0'||cloumn.completeStatus==0)&&judgeTimeOld(cloumn.datetime_deadline)?'red':''}">~</span>
              <el-tooltip class="item subTaskCount" effect="dark" placement="top">
                <div slot="content"  class="showTimeTooltip">截止时间 <span class="showTimeTooltip">{{cloumn.datetime_deadline | formatDate('yyyy-MM-dd HH:mm')}}</span></div>
                <span v-if="cloumn.datetime_deadline" :style="{color:(cloumn.complete_status=='0'||cloumn.completeStatus==0)&&judgeTimeOld(cloumn.datetime_deadline)?'red':''}">{{editorTime(cloumn.datetime_deadline)}}</span>
              </el-tooltip>
            </template> -->
            <span v-if="cloumn.datetime_deadline&&cloumn.datetime_starttime" class="bgcolorspan" :style="{color:cloumn.complete_status=='0'&&judgeTimeOld(cloumn.datetime_deadline)?'red':''}">
              <span>
                <span v-if="isjudageOverYear(cloumn.datetime_starttime, cloumn.datetime_deadline)" :style="{color:cloumn.complete_status=='0'&&judgeTimeOld(cloumn.datetime_deadline)?'red':''}">{{editorTime(cloumn.datetime_starttime)}}</span>
                <span v-else :style="{color:cloumn.complete_status=='0'&&judgeTimeOld(cloumn.datetime_deadline)?'red':''}">{{cloumn.datetime_starttime | formatDate('yyyy-MM-dd HH:mm')}}</span>
              </span>&nbsp;至&nbsp;
              <span>
                <span v-if="isjudageOverYear(cloumn.datetime_starttime, cloumn.datetime_deadline)" :style="{color:cloumn.complete_status=='0'&&judgeTimeOld(cloumn.datetime_deadline)?'red':''}">{{editorTime(cloumn.datetime_deadline)}}</span>
                <span v-else :style="{color:cloumn.complete_status=='0'&&judgeTimeOld(cloumn.datetime_deadline)?'red':''}">{{cloumn.datetime_deadline | formatDate('yyyy-MM-dd HH:mm')}}</span>
              </span>
            </span>
            <span v-else-if="cloumn.datetime_deadline||cloumn.datetime_starttime" class="bgcolorspan">
              <span v-if="cloumn.datetime_deadline"  :style="{color:cloumn.complete_status=='0'&&judgeTimeOld(cloumn.datetime_deadline)?'red':''}">
                截止时间  <span  :style="{color:cloumn.complete_status=='0'&&judgeTimeOld(cloumn.datetime_deadline)?'red':''}">{{editorTime(cloumn.datetime_deadline)}}</span>
              </span>
              <span v-if="cloumn.datetime_starttime" :style="{color:cloumn.complete_status=='0'&&judgeTimeOld(cloumn.datetime_deadline)?'red':''}">
                开始时间  <span :style="{color:cloumn.complete_status=='0'&&judgeTimeOld(cloumn.datetime_deadline)?'red':''}">{{editorTime(cloumn.datetime_starttime)}}</span>
              </span>
            </span>
          </div>
          <!-- <div>
            <span v-for="(item1,key1) in projectBaseInfo.projectLabelsContent" :key="key1" class="foreachSpantask" v-if="item1.name!=='picklist_priority'">
              <span v-if="filedTypeJudge(item1.name) === 'personnel'&&item1.name!=='personnel_execution'">
                <span v-for="(child,index) in cloumn[item1.name]" :key="index">{{child.name}}</span>
              </span>
              <span v-else-if="filedTypeJudge(item1.name) === 'datetime'">{{cloumn[item1.name] | formatDate('yyyy-MM-dd HH:mm')}}</span>
              <span v-else-if="filedTypeJudge(item1.name) === 'picklist' || filedTypeJudge(item1.name) === 'mutlipicklist'">
                <span class="picklist" v-for="(child,index) in cloumn[item1.name]" :key="index" :style="{'background':child.color}" :class="{'white-font':child.color === '#FFFFFF'}">{{child.label}}</span>
              </span>
              <span v-else-if="filedTypeJudge(item1.name) === 'multi'">
                <span v-for="(child,index) in cloumn[item1.name]" :key="index">{{child.label}}</span>
              </span>
              <span v-else-if="filedTypeJudge(item1.name) === 'reference'">
                <span v-for="(child,index) in cloumn[item1.name]" :key="index">{{child.name}}</span>
              </span>
              <span v-else-if="filedTypeJudge(item1.name) === 'multitext'" v-html="filterEditor(cloumn[item1.name])" class="multitext"></span>
              <span v-else-if="filedTypeJudge(item1.name) === 'textarea'" class="textarea" v-html="textareaNewline(cloumn[item1.name])"></span>
              <span v-else-if="filedTypeJudge(item1.name) === 'area'">{{cloumn[item1.name] | areaTo}}</span>
              <span v-else-if="filedTypeJudge(item1.name) === 'location'" v-text="locationShift(cloumn[item1.name])"></span>
              <span v-else-if="filedTypeJudge(item1.name) === 'url'" @click="openUrl(cloumn[item1.name])" class="url">{{cloumn[item1.name]}}</span>
              <span v-else-if="filedTypeJudge(item1.name) === 'number'">{{cloumn[item1.name] | pointTo(filedTypeJudge(item1.name))}}</span>
              <span v-else-if="item1.name!=='text_name'&&item1.name!=='picklist_tag'">{{cloumn[item1.name]}}</span>
            </span>
          </div> -->
        </div>
        <div class="threeSubTaskList" v-if="cloumn.picklist_tag&&cloumn.picklist_tag.length>0">
          <span class="activate threeSubTaskListTags" v-if="cloumn.complete_number">
            {{cloumn.complete_number}}
          </span>
          <span class="threeSubTaskListTags" v-for="(v3, k3) in cloumn.picklist_tag" :key="k3" :style="{background:v3.colour?v3.colour:''}">
            <span v-text="v3.name"></span>
          </span>
        </div>
      </div>
      <!-- 显示头像或者名称 (固定位置) -->
      <div v-if="cloumn.personnel_execution&&JSON.stringify(cloumn.personnel_execution)!=='[]'" :style="JSON.stringify(cloumn.personnel_execution)==='[]'&&cloumn.personnel_execution[0].name?'background:#fff;':''" :class="cloumn.personnel_execution&&(!cloumn.personnel_execution[0].picture||cloumn.personnel_execution[0].picture=='null')&&cloumn.personnel_execution[0].name?'showNameBgColor':''">
        <img v-if="cloumn.personnel_execution[0].picture&&cloumn.personnel_execution[0].picture!='null'" :src="cloumn.personnel_execution[0].picture + '&TOKEN=' + token" :title="cloumn.personnel_execution[0].name">
        <span v-if="!cloumn.personnel_execution[0].picture||cloumn.personnel_execution[0].picture=='null'">{{sliceName(cloumn.personnel_execution[0].name)}}</span>
      </div>
      <div v-else></div>
    </div>
    <!-- 自定义模块 -->
    <div class="customListDetail" v-if="cloumn.dataType===3">
      <div><i class="iconfont icon-yidong"></i></div>
      <div><span :style="{background:cloumn.icon_color?cloumn.icon_color:'#549AFF'}"><i class="iconfont " :class="cloumn.icon_url? cloumn.icon_url:'icon-zidingyitubiao-o1'" :style="{fontsize:'19px',background:cloumn.icon_color?cloumn.icon_color:'#549AFF'}"></i></span></div>
      <div>
        <div>
          {{editorNameSlice(cloumn.module_name,6) + ':'}}
          <span class="title" v-if="isArea">
            <!-- {{detailTitle | areaTo}} -->
          </span>
          <span class="title" v-else style="color:#797979;">
            {{editorNameSlice(detailTitle)}}
          </span>
        </div>
        <!-- <div class="customItemStyle">
          <div v-for="(item1, key1) in cloumn.row" :key="key1" v-if="key1>0">
            <span v-if="filedTypeJudge(item1.name) === 'personnel'"><span v-for="(child,index) in item1.value" :key="index">{{child.name}}</span></span>
            <span v-else-if="filedTypeJudge(item1.name) === 'datetime'">{{item1.value | formatDate('yyyy-MM-dd HH:mm')}}</span>
            <span v-else-if="filedTypeJudge(item1.name) === 'picklist' || filedTypeJudge(item1.name) === 'mutlipicklist'">
              <span class="picklist" v-for="(child,index) in item1.value" :key="index" :style="{'background':child.color}" :class="{'white-font':child.color === '#FFFFFF'}">{{child.label}}</span>
            </span>
            <span v-else-if="filedTypeJudge(item1.name) === 'multi'">
              <span v-for="(child,index) in item1.value" :key="index">{{child.label}}</span>
            </span>
            <span v-else-if="filedTypeJudge(item1.name) === 'reference'">
              <span v-for="(child,index) in item1.value" :key="index">{{child.name}}</span>
            </span>
            <span v-else-if="filedTypeJudge(item1.name) === 'multitext'" v-html="item1.value"></span>
            <span v-else-if="filedTypeJudge(item1.name) === 'textarea'" class="textarea" v-html="textareaNewline(item1.value)"></span>
            <span v-else-if="filedTypeJudge(item1.name) === 'area'">{{item1.value | areaTo}}</span>
            <span v-else-if="filedTypeJudge(item1.name) === 'location'" v-text="locationShift(item1.value)"></span>
            <span v-else-if="filedTypeJudge(item1.name) === 'picture' || filedTypeJudge(item1.name) === 'attachment'" class="picture">
              <div class="item" v-for="(file,index) in item1.value" :key="index">
                <img :src="file.file_url+'&TOKEN='+token" alt="" v-if="fileType(file.file_type).fileType === 'img'">
                <i class="iconfont" v-else :class="fileType(file.file_type).icon"></i>
                <div class="content">
                  <div class="title">{{file.file_name}}</div>
                  <div class="detail">
                    <span class="name">{{file.upload_by | limitTo(-2)}}</span>
                    <span class="date">{{file.upload_time | formatDate('yyyy-MM-dd')}}</span>
                    <span class="size">{{file.file_size | fileSize(file.file_size)}}</span>
                  </div>
                </div>
              </div>
            </span>
            <span v-else-if="filedTypeJudge(item1.name) === 'url'" @click="openUrl(item1.value)" class="url">{{item1.value}}</span>
            <span v-else-if="filedTypeJudge(item1.name) === 'number'">{{item1.value | pointTo(filedTypeJudge(item1.name))}}</span>
            <span v-else>{{item1.value}}</span>
          </div>
        </div> -->
      </div>
      <div>
        <div v-for="(item,key) in cloumn.row" :key="key" v-if="item.name === 'personnel_principal'" :style="item.value[0].picture&&item.value[0].picture!='null'?'':'background:#60CDFF;'">
          <img v-if="item.value[0].picture&&item.value[0].picture!='null'" :src="item.value[0].picture + '&TOKEN=' + token" :title="item.value[0].name">
          <span v-if="!item.value[0].picture||item.value[0].picture=='null'" v-text="sliceName(item.value[0].name)" style="font-size:12px;">人员</span>
        </div>
      </div>
    </div>
    <!-- 审批 -->
    <div class="approvalListDetail" v-if="cloumn.dataType===4">
      <span><i class="iconfont icon-yidong"></i></span>
      <span><i class="iconfont icon-approval-module-setup" style="color:#FFA92E;"></i></span>
      <span>审批 : </span>
      <span>{{editorNameSlice(cloumn.begin_user_name+'-'+cloumn.process_name,25)}}</span>
      <!-- <span><i class="iconfont icon-shenpi-shijian"></i>{{cloumn.create_time | formatDate('yyyy-MM-dd HH:mm')}}</span>
      <span class="showmoreTextAppro" v-text="cloumn.process_name">果ia</span> -->
      <span>
        <!-- <span>
          <span :class="'circleCss' + cloumn.process_status"></span>
          <span v-text="approvalstatusType[cloumn.process_status]">待审批</span>
        </span> -->
        <span class="peopleBgColor">
          <!-- <img :src="'http://192.168.1.188:8081/custom-gateway/common/file/imageDownload?bean=company&fileName=13/company/1522237286284.201803281941260.jpg'+ '&TOKEN=' + token" alt=""> -->
          <span v-text="sliceName(cloumn.begin_user_name)">人员</span>
        </span>
      </span>
    </div>
    <!-- 备忘录 -->
    <div class="memoListDetail" v-if="cloumn.dataType===1">
      <span><i class="iconfont icon-yidong"></i></span>
      <span><i class="iconfont icon-beiwanglu"></i></span>
      <span>备忘录 : </span>
      <span >{{editorNameSlice(cloumn.title, 25)}}</span>
      <!-- <span><i class="iconfont icon-shenpi-shijian"></i>{{cloumn.create_time | formatDate('yyyy-MM-dd HH:mm')}}</span> -->
      <span v-show="cloumn.createObj">
        <span></span>
        <span class="showBgcolor" :class="cloumn.createObj.picture&&cloumn.createObj.picture!='null'?'':'addBgcolor'">
          <img v-if="cloumn.createObj.picture&&cloumn.createObj.picture!='null'" :src="cloumn.createObj.picture + '&TOKEN=' + token" :title="cloumn.createObj.employee_name">
          <span v-if="!cloumn.createObj.picture||cloumn.createObj.picture=='null'" v-text="sliceName(cloumn.createObj.employee_name)" style="font-size:12px;">人员</span>
        </span>
      </span>
    </div>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
export default {
  name: 'LineCardTask',
  props: ['cloumn', 'listItem', 'GroupItem', 'projectBaseInfo', 'isGuanLian'],
  data () {
    return {
      data: {},
      approvalstatusType: ['待审批', '审批中', '审批通过', '审批驳回', '已撤销', '已转交', '待提交'],
      token: ''
    }
  },
  mounted () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
  },
  methods: {
    completeStatus (v, parentVal) {
      if (v.from && v.from === 1) {
        this.submitPersonalTask(v) // 个人任务完成
        return false
      }
      if (!this.isGuanLian) { // 层级视图激活或者完成
        this.$bus.$emit('listComplateActiveStatus', v, parentVal)
      }
    },
    submitPersonalTask (v) { // 个人任务完成
      let html = ''
      if (v.completeStatus === 1) {
        html = '确定要激活此任务吗'
      } else {
        html = '确定要完成此任务吗'
      }
      this.$confirm(html, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        if (v.task_id) {
          HTTPServer.updateSubForFinish({taskId: v.quoteTaskId}, 'Loading').then((res) => { // 个人子任务
            // this.$bus.$emit('taskCardComplateOrActive', this.status)
            this.$message({
              type: 'success',
              message: '操作完成!'
            })
            v.completeStatus = v.completeStatus === 1 ? 0 : 1
          })
        } else {
          HTTPServer.updateForFinish({taskId: v.quoteTaskId}, 'Loading').then((res) => { // 个人主任务
            // this.$bus.$emit('taskCardComplateOrActive', this.status)
            this.$message({
              type: 'success',
              message: '操作完成!'
            })
            v.completeStatus = v.completeStatus === 1 ? 0 : 1
          })
        }
      }).catch(() => { console.log('') })
    },
    sliceName (name) {
      if (name) {
        return name.slice(-2)
      }
    },
    editorNameSlice (text, i) {
      if (text) {
        if (text.length > i) {
          return text.slice(0, i) + '...'
        } else {
          return text
        }
      }
    },
    judgeTimeOld (time) { // 判断任务有无过期
      if (time) {
        return new Date().getTime() > time
      }
    },
    editorTime (time) {
      if (time) {
        let newtime = tool.formatDate(time, 'yyyy-MM-dd HH:mm')
        return newtime.slice(5)
      }
    },
    isjudageOverYear (startTime, endTime) { // 判断是否跨年
      if (startTime && endTime) {
        let newstartTime = tool.formatDate(startTime, 'yyyy-MM-dd HH:mm').split('-')[0]
        let newsendTime = tool.formatDate(endTime, 'yyyy-MM-dd HH:mm').split('-')[0]
        return newstartTime === newsendTime
      }
    },
    showFirstSpan (startTime, endTime) {
      if (!startTime || !endTime) {
        return true
      }
    },
    // 判断字段类型
    filedTypeJudge (data) {
      if (data) {
        return data.split('_')[0]
      }
    },
    // 地址组件转换
    locationShift (data) {
      if (data) {
        return data.value
      } else {
        return ''
      }
    },
    textareaNewline (data) {
      if (data) {
        data = data.replace(/\n/g, '<br>')
        return data
      }
    },
    // 根据组件类型显示列表字段
    getType (name) {
      let type = name.split('_')[1]
      return type
    },
    // 判断文件类型
    fileType (type) {
      let file = tool.determineFileType(type)
      return file
    },
    // 超链接打开网站
    openUrl (url) {
      let linkurl = ''
      if (url.includes('https://')) {
        linkurl = url
      } else {
        linkurl = url
      }
      window.open(linkurl)
    },
    filterEditor (html) { // 过滤富文本
      let newHtml = html.replace(/<\/?(img|a|p|b)[^>]*>/gi, '')
      return newHtml
    }
  },
  computed: {
    detailTitle () {
      if (this.cloumn.row[0].name) {
        let text
        let list = []
        let type = this.cloumn.row[0].name.split('_')[0]
        switch (type) {
          case 'picklist': case 'multi': case 'mutlipicklist':
            if (this.cloumn.row[0].value && Object.prototype.toString.call(this.cloumn.row[0].value) === '[object Array]') {
              this.cloumn.row[0].value.map((item, index) => {
                list.push(item.label)
              })
            } else {
              list = ['未知名称的数据']
            }
            text = list.toString()
            break
          case 'personnel': case 'department': case 'reference':
            if (this.cloumn.row[0].value && Object.prototype.toString.call(this.cloumn.row[0].value) === '[object Array]') {
              this.cloumn.row[0].value.map((item, index) => {
                list.push(item.name)
              })
            } else {
              list = ['未知名称的数据']
            }
            text = list.toString()
            break
          case 'datetime':
            text = tool.formatDate(this.cloumn.row[0].value, 'yyyy-MM-dd HH:mm')
            break
          case 'attachment': case 'picture':case 'subform': case 'multitext':
            text = ''
            break
          case 'location':
            if (this.cloumn.row[0].value) {
              text = this.cloumn.row[0].value.value
            }
            break
          default:
            text = this.cloumn.row[0].value ? this.cloumn.row[0].value : '未知名称的数据'
            break
        }
        return text
      }
    },
    isArea () {
      if (this.cloumn.row[0].name && this.cloumn.row[0].name.split('_')[0] === 'area') {
        return true
      } else {
        return false
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.line-card-task-warp{
  .approvalListDetail,.memoListDetail{
    height:50px; line-height: 50px;padding: 0 20px 0 5px;>span{float:left;margin-right:10px;}>span:nth-child(2){i{color:#F56C6C;font-size:25px;}}
    >span:nth-child(3){max-width:200px;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;}
    >span:nth-child(4){i{font-size: 14px;margin-right:5px;}}
    >span:nth-child(4),>span:nth-child(5){font-size:14px;color:#797979;}
    >span:last-child{
      float:right;margin:0;line-height:50px;height:50px;
      >span:first-child{
        float: left;width:90px;height:50px;line-height:50px;
        >span:first-child{height:10px;width:10px;border-radius: 50%;}
        >span:first-child.circleCss0{background: #FFA92E;}
        >span:first-child.circleCss1{background: #008FE5;}
        >span:first-child.circleCss2{background: #00A85B;}
        >span:first-child.circleCss3{background: #FA3F39;}
        >span:first-child.circleCss4{background: #CACACA;}
        >span:first-child.circleCss5{background: #00A85B;}
        >span:first-child.circleCss6{background: #00A85B;}
      }
      >span:last-child{
        float: left;height:30px;width:30px;border-radius: 50%;text-align: center;line-height: 30px;color:#fff;margin:10px 0 0 5px;
        img{width:30px;height:30px;border-radius: 50%;vertical-align: sub;border-radius:50%; }
      }
      .showmoreTextAppro{
        max-width: 145px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    span.showBgcolor.addBgcolor{background: #60CDFF;}
    >span:nth-child(1),>span:nth-child(2){height:50px;}
  }
  .approvalListDetail .peopleBgColor{background: #60CDFF;span{font-size: 12px;}}
  .customListDetail{
    min-height:50px;padding: 10px 10px 10px 4px;
    >div{float:left;margin-right:10px;}
    >div:nth-child(2){
      span{display:inline-block;height:27px;width:25px;text-align:center;line-height: 27px;background: #549AFF;color:#fff;border-radius:2px;}
    }
    >div:nth-child(3){
      max-width:84%;margin-right:0;
      >div:first-child{
        // overflow: hidden;text-overflow:ellipsis;white-space: nowrap;
        padding:5px 0;
      }
      // >div:last-child{font-size: 12px;color:#AFAFB5;overflow: hidden;}
    }
    >div:last-child{
      float: right;
      >div{
        height:30px;width:30px;border-radius: 50%;text-align: center;line-height: 30px;color:#fff;
        img{width:30px;height:30px;border-radius: 50%;vertical-align: middle;}
      }
    }
    div.customItemStyle{
      width: 100%;height:20px;
      >div{
        float:left;
        max-width: 190px;
        overflow: hidden;
        text-overflow:ellipsis;
        white-space: nowrap;
        margin-right:10px;
        span{font-size:12px;}
      }
    }
  }
  .customListDetail:after,.taskListDetail:after{content:'';display: table;clear:both;}
  .taskListDetail{
    border-left:4px solid transparent;min-height:50px;padding: 10px 20px 10px 0;>div{float:left;margin-right:10px;}
    >div.taskOpenOrclose{
      margin-top:6px;
      .chooseCheckedActive{background: #1890FF;border-color:#1890FF;>i{color:#fff;}}
    }
    >div:first-child{width:4px;height:100%;background: #3EA8FF;}
    >div:first-child,>div:nth-child(2){height:100%;}
    >div:nth-child(4){
      >div{padding:5px;min-height:25px;width:580px;overflow: hidden;}
      >div.twoSubTaskList{font-size:12px;color:#AFAFB5;max-width:580px;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;}
      >div.threeSubTaskList{
        min-height:28px;padding-bottom:0;
        .threeSubTaskListTags{
          margin-bottom:5px;padding:0 5px;background:#51D0B1;color:#fff;border-radius: 4px;font-size:12px;margin-right:5px;margin-bottom:5px;display: inline-block;overflow: hidden;
        }
        .threeSubTaskListTags.activate{
          padding: 1.5px 5px;
          border-radius: 2px;
          background: #F5A623;
        }
      }
    }
    >div:last-child{
      float: right;height:30px;width:30px;border-radius: 50%;text-align: center;line-height: 30px;color:#fff;margin-right:0;
      img{width:30px;height:30px;border-radius: 50%;vertical-align: sub;border-radius: 50%;}
      span{font-size:12px;}
    }
    >div.showNameBgColor{background: #60CDFF;}
    .taskoverHidden{
      >div>span{float:left;margin-right:10px;}
      span.taskSuboverHidden{
        max-width:338px;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;
      }
      span.taskSuboverHidden.overYear{
        max-width:270px;
      }
      span.taskSuboverHidden.overYearnew{
        max-width:380px;
      }
      // >span.taskSuboverHidden{display:inline-block;height:25px;max-width:280px;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;}
      // >span:nth-child(2){color:#AFAFB5;margin:0 10px;>span{font-size: 12px;}i{margin-right:5px;}}
      // >span:nth-child(3){color:red;>span,i{font-size:12px;}}
      span.foreachSpantask{
        font-size:12px;max-height: 19px;
        span{font-size:12px;color:#A2A2A8;}
        .multitext{
          // height:19px;overflow: hidden;
        }
        max-width:190px;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;
      }
      .subTaskCount{
        font-size:12px;color:#A2A2A8;
        i{font-size:12px;}
        span{font-size:12px;}
        i.icon-zirenwu{font-size:15px;}
      }
      >div.work-logo-next{
        padding-bottom:5px;
        .bgcolorspan{
          background: #EEEEEE;padding:0 5px;font-size:12px;
          border-radius: 2px;margin:2px 0 0 0;
          span{font-size:12px;}
        }
      }
    }
    .taskoverHidden>div:after{content:'';display: table;clear:both;}
  }
  .taskListDetail .taskOpenOrclose{
    div{
      height:18px;width:18px;position:relative;border: 1px solid #B9B9C1;border-radius: 3px;
      i{
        position:absolute;font-size:12px;color: #B7B7BF;top:2px;left:-1px;height:12px;transform:scale(.8);
      }
    }
  }
  .taskListDetail.file{border-left-color:#36CFC9;}
  .taskListDetail.stop{border-left-color:#8A96AD;}
  .taskListDetail.del{border-left-color:#FF5800;}
  .taskListDetail.use{border-left-color:#52C41A;}
  .icon-yidong{
    font-size: 20px;
    color: #BBBBC3;
  }
}
.line-card-task-warp{
  .opacityClassShow{
    opacity: .5;
  }
}
</style>
<style lang="scss">
.line-card-task-warp{

}
</style>
