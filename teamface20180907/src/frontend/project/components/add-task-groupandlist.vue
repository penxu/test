<template>
  <el-dialog :visible.sync="addNewListOne" width="600px" id="addListAndQueto">
    <template slot="title">
      <div class="addListHeader">
        <div v-if="isnewAddListStatus"><span @click.stop="changeAddOrQuote(1)" :class="addListActive===1? 'addListActive':''">创建子任务列表</span></div>
        <div v-else><span @click.stop="changeAddOrQuote(1)" :class="addListActive===1? 'addListActive':''">创建任务分组</span></div>
        <div><span @click.stop="changeAddOrQuote(0)" :class="addListActive===0? 'addListActive':''">使用模板添加</span></div>
      </div>
    </template>
    <div @click.stop class="boxAddList" :class="addListActive===1?'isAddTaskList':''">
      <div v-if="addListActive===1" class="firstAddList">
        <div class="creatTaskGroup">
          <i class="iconfont icon-yidong"></i>
          <el-input v-model="addNewListInput" placeholder="任务分组名称" :disabled="isnewAddListStatus"></el-input>
        </div>
        <div class="creatListTask" v-for="(v,k) in addNewListData" :key="k">
          <i class="iconfont icon-yidong"></i>
          <el-input v-model="v.listName" placeholder="任务列表名称"></el-input>
          <i class="iconfont icon-pc-member-close" @click.stop="delNewList(k)"></i>
        </div>
        <p class="clickSubmit" @click.stop="SubmitSave"><span>+</span> 新增任务列表</p>
      </div>
      <div v-if="addListActive===0" class="subBoxAddListTwo">
        <div class="inputGroupName">
          <el-row style="height:60px;">
            <el-col :span="5" v-if="isnewAddListStatus" class="showlieorgroup">任务列表名称:</el-col>
            <el-col :span="5" v-else class="showlieorgroup">任务分组名称:</el-col>
            <el-col :span="19">
              <el-input v-model="inputGroupName" placeholder="请输入任务分组名称" :disabled="isnewAddListStatus"></el-input>
            </el-col>
          </el-row>
        </div>
        <div v-for="(v,k) in addNewmodule" :key="k">
          <div><span class="perviewDiv" @click.stop="perviewTemplate(v)" v-text="v.name"></span></div>
          <div class="previewAddTask">
            <span>
              使用人员: <span v-for="(v1, k1) in v.members" :key="k1"><span style="padding:0 5px;">{{v1.name}}</span></span>
            </span>
            <span>
              <span></span>
              <!-- <span @click.stop="chooseTemplate(v)" :class="v.isactive==1?'isactive':''">使用</span> -->
              <el-checkbox v-model="v.isactive" @change="chooseTemplate(v)"></el-checkbox>
            </span>
          </div>
        </div>
      </div>
      <el-dialog width="590px" title="工作流" :visible.sync="innerVisible" append-to-body class="innerPerviewVisible">
        <div class="boxInner">
          <div class="taskinner"><i class="iconfont icon-yidong"></i><span>{{innerTitle.name}}</span></div>
          <div class="listinner" v-for="(v, k) in workflowData" :key="k">
            <i class="iconfont icon-yidong"></i>
            <span v-text="v.text">市场调研</span>
          </div>
        </div>
        <div slot="footer" class="dialog-footer">
          <el-button @click="innerVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitInner">确定</el-button>
        </div>
      </el-dialog>
    </div>
    <span @click.stop slot="footer" class="dialog-footer">
      <el-button @click.stop="addNewListOne = false">取 消</el-button>
      <el-button v-if="addListActive===1" type="primary" @click.stop="saveSubNode">确 定</el-button>
      <el-button v-if="addListActive===0" type="primary" @click.stop="useGroupPreview">确 定</el-button>
    </span>
  </el-dialog>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'addTaskGroupandlist',
  props: ['projectId'],
  data () {
    return {
      data: {},
      list: [], // 分组和列表数据
      addNewListOne: false,
      twoOrThree: '', // 两层还是三层结构
      isnewAddList: {name: ''}, // 新增工作流的
      isnewAddListStatus: false,
      innerVisible: false,
      taskListAddClickData: {}, // 新建任务列表点击暂存数据
      addNewmodule: [ // 新建列表引用模板
      ],
      addListActive: 1, // 默认选中新建
      addNewListInput: '', // 新建分组名称输入,
      addNewListData: [{listName: ''}], // 新建列表增加或删除
      inputGroupName: '', // 使用模版添加  新建分组名称
      workflowData: {},
      innerTitle: {name: ''}
    }
  },
  methods: {
    changeAddOrQuote (status, v) { // 切换新增列表还是引用模板列表
      this.addListActive = status
      if (this.isnewAddListStatus) {
        this.addNewListInput = this.isnewAddList.name
        this.inputGroupName = this.isnewAddList.name
      }
      if (status === 0) {
        this.getProWorkflow()
      }
    },
    getProWorkflow () { // 获取企业工作流
      HTTPServer.querProjectyWorkflowList({}, 'Loading').then((res) => {
        res.map((v, k) => {
          v.isactive = false
          v.members = JSON.parse(v.members)
        })
        this.addNewmodule = res
        console.log(res)
      })
    },
    getQueryNodesNameById (v, data) {
      HTTPServer.queryNodesNameById({id: v.id}, 'Loading').then((res) => {
        if (data) {
          let arr = []
          res.nodes.map((val, kye) => {
            arr.push({name: val.text, flowId: val.key})
          })
          data.subnodeArr = arr
          if (this.isnewAddListStatus) {
            HTTPServer.saveSubNode(data, 'Loading').then((res) => {
              this.addNewListOne = false
              this.$message({message: '新增成功！', type: 'success'})
              this.$bus.$emit('successAddTaskList', this.isnewAddList)
            })
          } else {
            if (this.twoOrThree) {
              data.children_data_type = parseInt(this.twoOrThree)
            }
            HTTPServer.saveMainNode(data, 'Loading').then((res) => {
              this.$message({message: '使用模版成功！', type: 'success'})
              this.addNewListOne = false
              this.$bus.$emit('successAddTaskList', '')
            })
          }
        } else {
          this.workflowData = res.nodes
          this.innerTitle = res.workflow
          this.innerVisible = true
        }
      })
    },
    delNewList (k) { // 删除列表
      this.addNewListData.splice(k, 1)
    },
    SubmitSave () { // 新增列表
      this.addNewListData.push({listName: ''})
    },
    perviewTemplate (v) { // 预览模版
      // let data = {}
      // data.status = 'open'
      // this.$bus.$emit('PreviewTemplateStatus', JSON.stringify(data))
      this.taskListAddClickData = v
      this.getQueryNodesNameById(v)
    },
    chooseTemplate (v) { // 新增分组，引用模版
      if (v.isactive) {
        this.addNewmodule.forEach((val, key) => {
          if (val.id !== v.id) {
            val.isactive = false
          }
        })
      }
    },
    useGroupPreview () { // 保存模版添加
      if (!this.inputGroupName || this.inputGroupName.length > 25) {
        this.$message({message: '任务分组名称必需在25字以内，且必填', type: 'warning'})
        return false
      }
      let senddata = {
        id: parseInt(this.projectId),
        subnodeArr: [], // 列表数据
        name: this.inputGroupName, // 分组名称
        flowId: '',
        flowStatus: 1
      }
      if (this.isnewAddListStatus) {
        senddata.name = this.isnewAddList.name
        senddata.nodeId = this.isnewAddList.id
        senddata.node_level = 3
        senddata.projectId = parseInt(this.projectId)
      }
      this.addNewmodule.map((val, key) => {
        if (val.isactive) {
          senddata.flowId = val.id
          this.getQueryNodesNameById(val, senddata)
        }
      })
      console.log(senddata)
    },
    saveSubNode () { // 新建分组和列表的保存
      // let isHave = 0
      let isListName = 0
      if (!this.addNewListInput || this.addNewListInput.length > 25) {
        this.$message({message: '任务分组名称必需在25字以内，且必填', type: 'warning'})
        return false
      }
      this.addNewListData.forEach((v, k) => {
        if (!v.listName) {
          isListName = 1
        }
        if (v.listName && v.listName.length > 25) {
          isListName = 1
        }
      })
      if (isListName === 1) {
        this.$message({message: '列表名称必需在25字以内，且必填', type: 'warning'})
        return false
      }
      // this.list.forEach((v, k) => {
      //   if (v.name === this.addNewListInput) {
      //     isHave = 1
      //   }
      // })
      // // if (isHave === 1) {
      // //   this.$message({message: '分组名称不能重复', type: 'warning'})
      // //   return false
      // // }
      // let isRepeat = this.changeRepeat(this.addNewListData, 'listName')
      // if (isRepeat) {
      //   this.$message({message: '分组内的列表名称不能重复', type: 'warning'})
      //   return false
      // }
      // this.copyList = JSON.parse(JSON.stringify(this.list))
      let _this = this
      let senddata = {
        id: parseInt(_this.projectId),
        // nodeId: data.id,
        subnodeArr: [], // 列表数据
        name: this.addNewListInput // 分组名称
      }
      this.addNewListData.forEach((v, k) => {
        senddata.subnodeArr.push({name: v.listName})
      })
      if (this.isnewAddListStatus) { // 新增子任务列表
        senddata.nodeId = this.isnewAddList.id
        senddata.node_level = 3
        senddata.projectId = parseInt(_this.projectId)
        HTTPServer.saveSubNode(senddata, 'Loading').then((res) => {
          this.addNewListOne = false
          this.$message({message: '新增成功！', type: 'success'})
          this.$bus.$emit('successAddTaskList', this.isnewAddList)
        })
      } else { // 新增分组和列表
        if (this.twoOrThree) {
          senddata.children_data_type = parseInt(this.twoOrThree)
        }
        HTTPServer.saveMainNode(senddata, 'Loading').then((res) => {
          this.addNewListOne = false
          this.$message({message: '新增成功！', type: 'success'})
          this.$bus.$emit('successAddTaskList', '')
          // this.getQueryMainNode()
        })
      }
    },
    submitInner () { // 选择模版
      this.addNewmodule.forEach((val, key) => {
        // innerTitle
        if (val.id === this.taskListAddClickData.id) {
          val.isactive = true
        } else {
          val.isactive = false
        }
      })
      this.innerVisible = false
    },
    changeRepeat (arr, str) { // 判断是否有重复数据
      let hash = {}
      let isRepeat = false
      for (let i in arr) {
        if (!hash[arr[i][str]]) {
          hash[arr[i][str]] = true
        } else {
          isRepeat = true
        }
      }
      return isRepeat
    }
  },
  mounted () {
    this.$bus.$on('creatTaskAndQuoteTemplate', (res, status) => {
      this.addNewListData = [{listName: ''}]
      this.isnewAddListStatus = false
      if (res && status !== 'addGroup') {
        this.isnewAddList = res
        if (JSON.stringify(res) !== '{}') {
          this.addNewListInput = res.name
          this.isnewAddListStatus = true
        }
      } else {
        this.addNewListInput = ''
        this.inputGroupName = ''
      }
      if (status === 'addGroup') {
        this.twoOrThree = res
      } else {
        this.twoOrThree = ''
      }
      this.addNewListOne = true
      this.changeAddOrQuote(1)
    })
  },
  beforeDestroy () {
    this.$bus.off('creatTaskAndQuoteTemplate')
  }
}
</script>
<style lang="scss" scoped>
  .addListHeader{
    width: 100%;
    height: 50px;
    border-bottom:1px solid #ddd;
    div{
      float: left;
      width: 50%;
      text-align: center;
      height: 50px;
      line-height: 50px;
      span{
        display: inline-block;
        height: 49px;
        font-size:17px;
        color:#999;
        &:hover{cursor: pointer;border-bottom:2px solid #2596FF;}
      }
      .addListActive {border-bottom:2px solid #2596FF;color:#333;}
    }
  }
  .boxAddList{
    margin:20px;
    .listBoxItem{
      // max-height: 185px;
      // overflow: auto;
      padding:0 20px;
      .subBoxAddListOne{
        height: 40px;
        line-height: 40px;
        border: 1px solid #ddd;
        border-radius: 5px;
        position: relative;
        padding-left:15px;
        margin-top:10px;
        >i{position: absolute;top:0;right:-10px;color:#9C9C9C;&:hover{cursor: pointer;}}
      }
    }
    .firstAddList{padding:20px 20px 20px 0;}
    .firstAddList>div.creatListTask{
      background:#F1F3F4;
      border-radius: 4px;
      display: flex;
      position: relative;
      margin: 10px 0 15px 30px;
      padding:8px 0;
      >i:nth-child(1){width:20px;color: #BBBBC3;font-size:23px;margin:6px 10px 0 0;}
      >i.icon-pc-member-close{width:12px;color: #BBBBC3;font-size:12px;margin:10px 20px 0 20px;cursor: pointer;}
    }
    .creatTaskGroup{
      display: flex;margin-bottom:30px;
      >i:nth-child(1){width:20px;color: #BBBBC3;font-size:23px;margin:6px 10px 0 0;}
    }
    .clickSubmit{
      height:50px;line-height: 50px;text-align: center;background: #1890FF;border-radius: 4px;color:#fff;font-size:16px;margin-left:30px;opacity: 0.58;
      &:hover{
        opacity: 1;
      }
      span{font-size: 25px;display: inline-block;}cursor: pointer;
    }
    .subBoxAddListTwo{
      padding:0 20px;
      >div{min-height:80px;border-bottom:1px dashed #ddd;padding:15px 20px 5px 0;}
      >div.inputGroupName{
        border:0;
        .showlieorgroup{
          height:60px;line-height: 60px;
        }
      }
      .previewAddTask{
        min-height:35px;line-height: 35px;display:flex;
        >span:first-child{color: #9D9D9D;font-size:12px;span{font-size:12px;}width:88%;line-height: 30px;}
        >span:last-child{
          flex:1;//float: right;
          >span:nth-child(1){color:#62D9D4;font-size:12px;cursor: pointer;}
          >span:nth-child(2){
            padding:2px 10px;border-radius: 5px;background: #F5F5F5;border: 1px solid #ddd;margin-left:10px;font-size:12px;
            &:hover{background: #1890FF;color:#fff;cursor: pointer;border-color: #1890FF;}
          }
          >span.isactive{background: #1890FF;color:#fff;border-color: #1890FF;}
        }
      }
      .perviewDiv{
        &:hover{color:#1890FF;cursor: pointer;}
      }
    }
  }
  .boxAddList.isAddTaskList{border:1px solid #E9E9E9;border-radius:4px;}
</style>
<style lang="scss">
  #addListAndQueto{
    .el-dialog__header{background:#fff;height:50px;padding:0;.el-dialog__headerbtn{display:none;}}
    .el-dialog__body{padding:0;}
    .el-dialog__footer{
      height:52px;line-height:52px;padding:0;.el-button{padding:8px 13px;}padding-right:30px;border-top:1px solid #ddd;
    }
    .creatListTask,.creatTaskGroup{
      .el-input__inner{
        height:34px;line-height:34px;
      }
    }
    .creatTaskGroup{.el-input__inner{background:#FBFBFB;}}
  }
  .innerPerviewVisible{
    .el-dialog__body{padding:0;}
    .el-dialog__header{border-bottom:1px solid #ddd;.el-dialog__title{font-size:16px;}}
    .el-dialog__footer{
      height: 52px;
      line-height: 52px;
      padding: 0;
      padding-right: 30px;
      border-top: 1px solid #ddd;
      .dialog-footer{
        .el-button{
          padding:8px 13px;
        }
      }
    }
    .boxInner{
      margin:20px;border:1px solid #D2D2D2;border-radius:4px;min-height:322px;
      >div{i{float:left;span{float:left;}}}
      .taskinner{
        height:50px;line-height:50px;span{font-size: 18px;color: #323232;}i{color:#BBBBC3;font-size:23px;}
      }
      .listinner{
        height:40px;line-height:40px;background: #F1F3F4;border-radius: 3px;margin:10px 20px;i{color:#BBBBC3;font-size:23px;}
      }
    }
  }
</style>
