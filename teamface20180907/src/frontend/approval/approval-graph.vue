<template>
  <!-- 审批流程图 -->
  <div class="approval-process">
    <!-- 审批步骤显示 -->
    <ul>
      <li v-for="(item,index) in approvalData" :key="item.id" class="approval-item" v-if="!(item.task_name === '审批已撤销' && item.task_status_id ==='4' && index !== (approvalData.length-1))">
        <!-- 进度条圆点 -->
        <!-- 固定流程的节点控制 -->
        <span class="progress-dot" v-if="(index === 0 || !((approvalData[index].task_key === approvalData[index-1].task_key)&&approvalData[index-1].task_status_id !=='4'))&&item.process_type===0" :style="progressDot(item.task_status_id)"></span>
        <!-- 自由流程的节点控制 -->
        <span class="progress-dot" v-if="(index === 0 || !((approvalData[index].task_key === approvalData[index-1].task_key)&&approvalData[index-1].task_status_id ==='5'))&&item.process_type===1" :style="progressDot(item.task_status_id)"></span>
        <!-- 进度条身体 -->
        <span class="progress-body" :style="progressBody(item,index)"></span>
        <!-- 审批负责人及状态 -->
        <p class="top-step">
          <!-- 有头像图片则不需要imgName -->
          <img :src="item.approval_employee_picture+'&TOKEN='+token" v-if="item.approval_employee_picture && index !== (approvalData.length-1) && item.normal !== '0'" alt="">
          <!-- 正常的imgName -->
          <span class="name-picture" v-if="!item.approval_employee_picture && index !== (approvalData.length-1) && !(item.task_approval_type === 3 && item.task_status_id === 0) && item.task_approval_type !==2 && item.task_approval_type !==1 && item.normal !== '0'">{{item.approval_employee_name | limitTo(-2)}}</span>
          <!-- 待确定-审批角色的imgName -->
          <span class="name-picture" v-if="(item.task_approval_type===3 && item.task_status_id === '0') || item.task_approval_type===2 || item.task_approval_type===1" style="backgroundColor:#ccc;"><i class="iconfont">&#xe6f6;</i></span>
          <!-- 最后一个imgName 完成/审批中/待审批 -->
          <!-- <i class="iconfont wb-success" :class="{'active': (item.task_status_id ==='2')}" v-if="(item.task_status_id === '0' && item.task_key ==='endEvent' ) || (item.task_status_id === '1' && item.task_key ==='endEvent' ) || (item.task_status_id === '2' && item.task_key ==='endEvent' )">&#xe679;</i> -->
          <i class="iconfont wb-success" :class="{'active': (item.task_status_id ==='2')}" v-if="(item.task_status_id === '0' && item.task_key ==='endEvent' ) || (item.task_status_id === '-2' && item.task_key ==='endEvent' ) || (item.task_status_id === '2' && item.task_key ==='endEvent' )">&#xe679;</i>
          <!-- 最后一个imgName 驳回/已撤销 -->
          <i class="iconfont wb-reject" :class="{'active': (item.task_status_id ==='3')}" v-if="(item.task_status_id ==='3' && item.task_key ==='endEvent' ) || (item.task_status_id ==='4' && item.task_key ==='endEvent' )">&#xe6e9;</i>
          <!-- 名字 -->
          <span class="top-name">{{item.task_key === 'endEvent'?item.approval_message:item.approval_employee_name}} </span>
          <!-- 职位 -->
          <span class="top-job" v-if="item.approval_employee_post"> ( {{item.approval_employee_post}} ) </span>
          <!-- 状态 -->
          <span class="top-status" :class="{'active-status': item.classSta}"><i class="iconfont" v-if="item.classSta">&#xe705;</i> {{item.task_status_name}}</span>
        </p>
        <!-- 审批意见 -->
        <p class="center-step" v-if=" !(item.task_approval_type === 3 && item.task_status_id === 0) && item.task_approval_type!==2 && item.task_approval_type!==1" :class="{'active-message':item.approval_message === '正在审批中...'}">
          {{item.approval_message}}
        </p>
        <!-- 审批时间 -->
        <p class="bottom-step">
          {{item.approval_time | formatDate('yyyy-MM-dd HH:mm')}}
        </p>
      </li>
    </ul>
  </div>
</template>
<script>
export default {
  name: 'approvalGraph',
  data () {
    return {
      approvalData: [],
      classSta: false,
      token: ''
    }
  },
  mounted () {
    // 获取父组件传送过来的流程图数据
    this.$bus.$off('getApprovalGraphData')
    this.$bus.$on('getApprovalGraphData', (value) => {
      // 赋值给this.approvalData
      this.approvalData = value
      this.apprStatus()
    })
  },
  methods: {
    // 流程图状态高亮-'已等待xxx天'
    apprStatus () {
      this.approvalData.map((item, index) => {
        if (item.task_status_name) {
          if (item.task_status_name.includes('已等待')) {
            item['classSta'] = true
          }
        }
      })
    },
    // 圆点颜色  BBBBC3灰色 FA3F39红色
    progressDot (data) {
      if (data === '0' || data === '1' || data === '-2' || data === undefined) return 'border-color:#BBBBC3;'
      if (data === '3') return 'border-color:#FA3F39;'
      if (data === '4') return 'border-color:#BBBBC3;'
    },
    // 流程进度条样式控制
    progressBody (data, index) {
      if (data.task_status_id === '0' || data.task_status_id === '1' || data.task_status_id === '-2' || data.task_status_id === undefined) return 'border:1px dashed #BBBBC3;border-color:#BBBBC3;height: 99%;'
      if (data.task_status_id === '3') return 'border-color:#FA3F39;'
      if (data.task_status_id === '4') return 'border-color:#BBBBC3;'
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
  },
  computed: {
    // showItem () {
    //   return !(this.nodeType === 'start' || this.nodeType === 'end')
    // },
    // // 单人审批要显示的项目
    // singleShow () {
    //   return !(this.nodeType === 'start' || this.nodeType === 'end') && this.taskData.approverType === '0'
    // },
    // // 多人审批要显示的项目
    // multiShow () {
    //   return !(this.nodeType === 'start' || this.nodeType === 'end') && this.taskData.approverType.value === '1'
    // },
    // // 角色审批要显示的项目
    // roleShow () {
    //   return !(this.nodeType === 'start' || this.nodeType === 'end') && this.taskData.approverType.value === '4'
    // },
    // // 单级审批
    // singleStageShow () {
    //   return !(this.nodeType === 'start' || this.nodeType === 'end') && this.taskData.approverType.value === '2'
    // },
    // // 多级审批人
    // multiStageShow () {
    //   return !(this.nodeType === 'start' || this.nodeType === 'end') && this.taskData.approverType.value === '3'
    // },
    // // 发起人自己
    // ownSelfShow () {
    //   return (this.nodeType === 'start' || this.nodeType === 'end') || this.taskData.approverType.value === '3' || this.taskData.approverType.value === '5' ||
    //   this.taskData.approverType.value === '2'
    // }
  }
}
</script>
<style lang="scss">
// 审批流程
.approval-process {
  margin: 0px 30px 50px 30px;
  position: relative;
  >ul{
    padding: 20px 0px 30px 0px;
    overflow: hidden;
    // 具体步骤显示
    .approval-item{
      padding-bottom: 35px;
      min-height: 100px;
      width: 85%;
      float: right;
      position: relative;
      border-bottom: 1px solid  #E7E7E7;
      // 进度条圆点
      .progress-dot {
        position: absolute;
        z-index: 2;
        left: -73px;
        top: 14px;
        width: 10px;
        height: 10px;
        border-radius: 50%;
        border: 2px solid #409EFF;
        background-color: #fff;
      }
      // 进度条身体
      .progress-body {
        position: absolute;
        left: -69px;
        top: 20px;
        height: 101%;
        border:1px solid #409EFF;
      }
      >p {
        width: 100%;
      }
      // 审批负责人及状态
      .top-step {
        margin-top: 7px;
        position: relative;
        >img {
          background-color: #cccccc;
          width: 30px;
          height:30px;
          border-radius: 50%;
          position: absolute;
          left: -40px;
          top: -4px;
        }
        .name-picture {
          width: 30px;
          height: 30px;
          background-color: #409EFF;
          color: #ffffff;
          line-height: 30px;
          text-align: center;
          border-radius: 50%;
          position: absolute;
          left: -40px;
          top: -4px;
          font-size: 12px;
          >i {
            font-size: 30px;
            color: #D3D3D3;
            background-color: #fff;
          }
        }
        .wb-success,.wb-reject {
          position: absolute;
          left: -40px;
          top: -6px;
          font-size: 30px;
          color: #D3D3D3;
          &.active {
            color: #409EFF;
          }
        }
        .wb-reject {
          &.active {
            color: #FA3F39;
          }
        }
        .top-name {
          font-size: 14px;
          color: #4A4A4A;
        }
        .top-job{
          font-size: 12px;
          color: #A0A0AE;
        }
        .top-status {
          position: absolute;
          font-size: 12px;
          color: #A0A0AE;
          right: 0;
          &.active-status {
            color: #FFA92E;
          }
          >i {
            font-size: 12px;
          }
        }
      }
      // 审批意见
      .center-step {
        word-break: break-all;
        margin-top: 5px;
        font-size: 12px;
        color: #69696C;
        &.active-message{
          color:#F94C4A;
        }
      }
      // 审批时间
      .bottom-step {
        position: absolute;
        bottom: 0px;
        margin-top: 12px;
        margin-bottom: 5px;
        font-size: 12px;
        color: #A0A0AE;
      }
      &:last-child{
        padding-bottom: 0;
        border-bottom: 0;
        min-height: 50px;
        .progress-dot{
          top: 15px;
        }
        .progress-body{
          display: none;
        }
        .top-step {
          top: 9px;
          margin-top: 0;
          .top-job,.top-status {
            display: none;
          }
        }
        .center-step,.bottom-step {
          display: none;
        }
      }
    }
  }
}
</style>
