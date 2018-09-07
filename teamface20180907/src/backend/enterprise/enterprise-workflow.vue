<template>
  <el-container class="enterprise-container" id="enterprise-container" :style="{height: allHeight}">
    <el-main>
      <div v-if="!isShowModify">
        <div class="add-btn"><el-button  type="primary" @click="handleClickAddNew(true)" icon="el-icon-plus">新建工作流</el-button></div>
        <div class="wf_list-container">
          <div v-for="(item, index) in workFlowList" :key="index" class="flex-box list-box">
            <div class="list-item" v-show="item.name !== ''">
              <div class="flex-box bandle-box">
                <div @click="handleCheckWorkFlow(item.id)"><span class="list-title">{{item.name}}</span></div>
                <div class="list-btn-box">
                  <el-button type="text" @click="handleClickEdit(item.id)">编辑</el-button>
                  <el-button type="text" @click="handleCliceDelete(item.id)">删除</el-button>
                </div>
                </div>
              <div style="padding-bottom: 20px;" class="over-ellipsis"><span class="list-describe">{{item.describe}}</span></div>
              <div class="name-box"><span class="use-member">使用人员:</span> <span v-for="(member, index) in item.members" :key="index" class="member-name">{{member.name}}</span></div>
            </div>
          </div>
        </div>
        <div class="detail-dialog" :class="{'full-dialog': isFull}">
          <el-dialog
            :title="allProcessData.name"
            :visible.sync="detailVisable"
            @close="handleCloseDialog"
            :fullscreen="isFull">
            <div class="wf_draw-box">
              <i class="iconfont icon-yulan preview" @click="hanleFullScreen" v-show="!isFull"></i>
              <drawTool v-if="isShowDetail" :draw="allProcessData" :from="'view'" :isFull="isFull"></drawTool>
            </div>
          </el-dialog>
        </div>
        <div class="full-dialog">
          <el-dialog
            :title="allProcessData.name"
            :visible.sync="fullVisable"
            @close="handleCloseDialog"
            :fullscreen="true">
            <div class="wf_draw-box">
              <!-- <i class="iconfont icon-yulan preview" v-show="this.from === 'view'" @click="hanleFullScreen"></i> -->
              <drawTool v-if="isShowFull" :draw="allProcessData" :from="'view'" :isFull="isFull"></drawTool>
            </div>
          </el-dialog>
        </div>
      </div>
      <WorkflowModify v-if="isShowModify" :workflowId="allProcessData.id"></WorkflowModify>
    </el-main>
  </el-container>
</template>
<script>
import { HTTPServer } from '@/common/js/ajax.js'
// import go from 'gojs'
// import { GuidedDraggingTool } from '@/common/js/GuidDragginTool.js'
import WorkflowModify from '@/backend/enterprise/enterprise-workflow/workflow-modify'
import drawTool from '@/backend/enterprise/enterprise-workflow/draw-workflow'
export default {
  name: 'enterpriseWorkflow',
  components: {WorkflowModify, drawTool},
  data () {
    return {
      process: null,
      $$: null, // 初始化Go对象
      allHeight: '', // 总高度
      canvasHeight: '',
      allProcessData: {
        id: '',
        name: '产品工作流程',
        describe: '',
        members: [],
        nodeDataArray: [
        ],
        linkDataArray: [
        ]
      },
      workFlowList: [{name: '', members: {}, describe: ''}],
      detailVisable: false,
      isShowModify: false,
      pageParams: {pageNo: 1, pageSize: 10},
      isShowDetail: false,
      isFull: false,
      fullVisable: false,
      isShowFull: false
    }
  },
  methods: {
    /** ***************** 接口 ********************************************************* */
    // 获取项目工作流列表
    getWorkFlowList () {
      HTTPServer.queryWorkFlowList(this.pageParams)
        .then((res) => {
          this.workFlowList = res.dataList
          console.log(this.workFlowList, '获取到的工作流列表')
        })
    },
    // 获取要编辑的工作流
    getWorkFlowDetail (params) {
      HTTPServer.queryWorkFlow(params)
        .then((res) => {
          this.allProcessData = res
          console.log(this.allProcessData, '获取到的工作流详情')
          this.isShowDetail = true
          this.detailVisable = true
        })
    },
    // 删除工作流
    deleteWrokFlow (data) {
      HTTPServer.deleteWrokFlow(data)
        .then((res) => {
          console.log(res, '删除成功')
          this.$message({message: '删除成功！', type: 'success', showClose: 'true'})
          this.getWorkFlowList()
        })
    },
    /**  end ****************************************************************************************/
    // 获取屏幕高度
    getClientHeight () {
      this.allHeight = (window.screen.availHeight - 155) + 'px'
      console.log(this.allHeight, '可用高度')
      // this.settiingBoxHeight = (window.screen.availHeight - 310) + 'px'
    },
    // 点击新增
    handleClickAddNew (type) {
      this.isShowModify = true
      // if (type) {
      //   setTimeout(() => {
      //     this.initProcess()
      //   }, 300)
      // }
      this.allProcessData.id = ''
    },
    // 点击编辑
    handleClickEdit (params) {
      // let id = {id: params}
      // this.getWorkFlowDetail(id)
      this.allProcessData.id = params
      this.isShowModify = true
    },
    // 点击删除
    handleCliceDelete (params) {
      this.$confirm('确定要删除该工作流吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let id = {id: params}
        this.deleteWrokFlow(id)
      })
    },
    // 点击查看流程图
    handleCheckWorkFlow (params) {
      this.getWorkFlowDetail({id: params}, true)
    },
    // 关闭弹窗
    handleCloseDialog () {
      // this.isFull = false
      if (this.isFull) {
        this.isFull = false
        this.detailVisable = true
        this.isShowDetail = true
      } else {
        this.isFull = false
        this.isShowDetail = false
      }
    },
    // 点击全屏预览
    hanleFullScreen () {
      // this.fullVisable = true
      // this.isShowFull = true
      // this.process.requestUpdate()
      this.isFull = true
    }
  },
  computed: {
  },
  created () {
  },
  mounted () {
    this.getClientHeight()
    this.getWorkFlowList()
    this.$bus.off('workFlowBack')
    this.$bus.on('workFlowBack', value => {
      this.isShowModify = value
      this.getWorkFlowList()
    })
  }
}
</script>
<style lang="scss">
  #enterprise-container.enterprise-container {
    // overflow: auto;
    position: relative;
    .process-middle {
      height: calc(100% - 50px);
      .grid-content {
        height: calc(100% - 50px);
        .palette {
          border: 1px solid #ccc;
          width: 200px;
          height: 100%;
        }
        .co-process-box {
          // height: 100%;
          width: calc(100% - 200px);
          border: 1px solid #ccc;
          height: 100%;
        }
      }
    }
    // 列表
    .wf_list-container {
      margin-top: 20px;
      .list-box {
        justify-content: space-between;
        width: 100%;
        .list-item {
          width: 100%;
          padding-bottom: 20px;
          border-bottom: 1px solid #E5E5E5;
          .bandle-box {
            justify-content: space-between;
          }
          .name-box {
            padding-left: 70px;
            position: relative;
            min-height: 20px;
          }
        }
      }
      .list-title {
        // font-size: 20px;
        font-weight: bold;
        line-height: 40px;
        cursor: pointer;
        &:hover {
          color: #1890FF;
        }
      }
      .list-describe {
        // font-size: 18px;
        color: #999999;
        // line-height: 30px;
      }
      .use-member {
        // font-size: 16px;
        line-height: 30px;
        padding-right: 10px;
        position: absolute;
        left: 0;
        top: 0;
      }
      .member-name {
        padding-right: 15px;
        color: #475669;
        line-height: 30px;
      }
      .list-btn-box {
        line-height: 40px;
        button:nth(2) {
          span {
            color: #FF6260;
          }
        }
      }
    }
    .add-btn {
       margin-top: 20px;
      .el-button {
        padding: 8px 14.5px;
      }
    }
    .detail-dialog {
      .el-dialog__header {
        background: #fff;
        span {
          color: #323232;
        }
        i {
          color: #323232;
        }
      }
      .el-dialog__body {
        background: #F5F6F7;
        height: 700px;
        padding: 0;
      }
      .wf_draw-box {
        position: relative;
        height: 100%;
        height: 100%;
        i.preview {
          position: absolute;
          color: #666666;
          right: 21px;
          top: 17px;
          z-index: 10;
          cursor: pointer;
          font-size: 20px;
          opacity: .7;
        }
      }
    }
    .full-dialog {
      .el-dialog__body {
        height: calc(100% - 54px);
        padding: 0;
        background: #F5F6F7;
        .wf_draw-box {
          height: 100%
        }
      }
    }
}
</style>
