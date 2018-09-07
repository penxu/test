<template>
    <div class="wf_modify-container" >
      <div class="flex-box wf_title-box">
        <div class="wf-text"><span style="color: red">*</span>工作流名称：</div> <div class="input" style="width: 400px"><el-input v-model="allWorkFlowData.name" placeholder="请输入工作流名称，限制25个字" maxlength="25"></el-input></div>
      </div>
      <div class="flex-box wf_member-box">
        <div class="wf-text"> <span>使用人员：</span></div>
        <div class=" member-item">
          <div class="member-btn pull-left" @click.stop="handleSelUseMember()"><span><i class="el-icon-plus"></i> 添加成员</span></div>
          <div v-for="(member, index) in allWorkFlowData.members" :key="index" class="member over-ellipsis pull-left" :style="{width: calcMemberWidth(member.type) }"><i class="iconfont " :class="{'icon-pc-member-organ': member.type === 0 || member.type === 4, 'icon-jiaosequanxian': member.type === 2}"></i><span :title="member.name">{{member.name}}</span> <i class="iconfont icon-pc-member-close" @click.stop="handleDelMember(index)"></i></div>
        </div>
      </div>
      <div class="flex-box wf_describe-box">
        <div class="wf-text"> <span>工作流描述：</span> </div>
        <div style="width: 400px;">
          <el-input
          type="textarea"
          :rows="4"
          placeholder="请输入内容，限制1000个字"
          v-model="allWorkFlowData.describe"
          maxlength="1000">
        </el-input>
        </div>
      </div>
      <div class="process-command-box">
        <drawTool v-if="isShowDraw" :draw="allWorkFlowData" :from="'edit'"></drawTool>
    </div>
    <div class="btn-box">
      <el-button @click="handleCancel()">取消</el-button>
      <el-button type="primary" @click="handleSave()">保存</el-button>
    </div>
    </div>
</template>
<script>
import { HTTPServer } from '@/common/js/ajax.js'
import drawTool from '@/backend/enterprise/enterprise-workflow/draw-workflow'
export default {
  name: 'WorkflowModify',
  components: {drawTool},
  props: ['workflowId'],
  data () {
    return {
      allWorkFlowData: {
        id: this.workflowId,
        name: '',
        describe: '',
        members: [],
        nodeDataArray: [
          {
            'category': 'Start',
            'text': '开始',
            'key': 'start',
            'loc': '0 0'
          },
          {
            'category': 'End',
            'text': '结束',
            'key': 'end',
            'loc': '0 400'
          }
        ],
        linkDataArray: [
          {
            'from': 'start',
            'to': 'end',
            'fromPort': 'B',
            'toPort': 'T'
            // 'visible': true,
            // 'text': 'Y'
          }
        ]
      },
      isShowModify: false,
      isShowDraw: false
    }
  },
  methods: {
    /** ***************** 接口 ********************************************************* */
    // 获取要编辑的工作流
    getWorkFlowDetail (params) {
      HTTPServer.queryWorkFlow(params, true)
        .then((res) => {
          this.allWorkFlowData = res
          console.log(this.allWorkFlowData, '获取到的工作流详情')
          this.isShowDraw = true
        })
    },
    // 新增工作流
    saveWorkFlow (data) {
      HTTPServer.saveWorkFlow(data)
        .then((res) => {
          console.log(res, '新增工作流成功')
          this.$message({type: 'success', showClose: 'true', message: '新增成功！'})
          this.$bus.emit('workFlowBack', false)
        })
    },
    // 编辑工作流
    editWorkFlow (data) {
      HTTPServer.editWorkFlow(data)
        .then((res) => {
          console.log(res, '编辑成功')
          this.$message({type: 'success', showClose: 'true', message: '编辑成功！'})
          this.$bus.emit('workFlowBack', false)
        })
    },
    /**  end ****************************************************************************************/

    // 点击保存
    handleSave () {
      this.allWorkFlowData.linkDataArray.map((item, index) => {
        if (item.points) { delete item.points }
      })
      if (this.allWorkFlowData.name === '') {
        this.$message({type: 'warning', showClose: 'true', message: '工作流名称不能为空！'})
        return
      }
      if (this.allWorkFlowData.id === '') {
        this.saveWorkFlow(this.allWorkFlowData)
      } else {
        this.editWorkFlow(this.allWorkFlowData)
      }
      console.log(JSON.stringify(this.allWorkFlowData), '最终数据')
    },
    // 选择使用人员
    handleSelUseMember () {
      this.$bus.$emit('commonMember', {'prepareData': this.allWorkFlowData.members, 'prepareKey': 'workFlow', 'seleteForm': true, 'banData': [], 'navKey': '1,0,2', 'removeData': []})
    },
    // 点击取消
    handleCancel () {
      this.$bus.emit('workFlowBack', false)
    },
    // 删除成员
    handleDelMember (index) {
      this.allWorkFlowData.members.splice(index, 1)
    },
    // 计算成员宽度
    calcMemberWidth (type) {
      return type === 0 || type === 2 || type === 4 ? '100px' : '80px'
    }
  },
  computed: {
  },
  created () {
  },
  mounted () {
    // this.getClientHeight()
    // this.initPalette()
    // this.$bus.off('selectEmpDepRoleMulti')
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value) {
        this.allWorkFlowData.members = JSON.parse(JSON.stringify(value.prepareData))
        console.log(this.allWorkFlowData, 'allWorkFlowData')
      }
    })
    if (this.allWorkFlowData.id !== '') {
      this.getWorkFlowDetail({id: this.allWorkFlowData.id})
    } else {
      this.isShowDraw = true
    }
  }
}
</script>
<style lang="scss">
.wf_modify-container {
  height: calc(100% - 72px);
  position: relative;
  padding-top: 20px;
  overflow: auto;
  // 通用
  .wf-text {
    width: 100px;
    line-height: 36px;
    margin-right: 20px;
    text-align: right;
  }
  .wf_member-box,.wf_describe-box {
    margin-top: 20px;
  }
  .wf_title-box {
    .el-input__inner {
      height: 36px;
    }
  }
  .wf_member-box {
    .member-item {
      border: 1px solid #E7E7E7;
      border-radius: 3px;
      max-height: 82px;
      overflow-y: auto;
      max-width: calc(100% - 120px);
      min-width: 400px;
      // flex-wrap: wrap;
      padding: 0 5px 6px 5px;
      // align-items: center;
      .member-btn {
        cursor: pointer;
        margin-top: 6px;
        height: 26px;
        span, i {
          color: #549AFF;
        }
        span {
          line-height: 26px;
        }
      }
      .member {
        width: 81px;
        height: 26px;
        background: #E9EDF2;
        border-radius: 2px;
        margin-left: 10px;
        padding-left: 8px;
        position: relative;
        padding-right: 20px;
        margin-top: 6px;
        span {
          line-height: 26px;
          font-size: 12px;
        }
        i.icon-pc-member-close {
          position: absolute;
          right: 5px;
          font-size: 10px;
          opacity: .3;
          top: 6px;
          cursor: pointer;
        }
        i.icon-pc-member-organ{
          color: #1890FF;
          font-size: 10px;
          padding-right: 5px;
        }
        i.icon-jiaosequanxian {
          color: #1890FF;
          padding-right: 5px;
        }
      }
    }
  }
  .process-command-box {
    margin-top: 20px;
    height: 800px;
    .grid-content {
      width: 100%;
      border: 1px solid #E7E7E7;
      box-sizing: border-box;
      .el-button {
        padding: 10px 20px;
      }
    }
    .co-process-box {
      // background: #F5F6F7;
      height: calc(100% - 40px)
    }
  }
  .btn-box {
    padding-top: 20px;
    padding-bottom: 20px;
    position: fixed;
    bottom: 0;
    width: 100%;
    background: #fff;
    .el-button {
      padding: 8px 19px;
    }
  }
}
</style>
