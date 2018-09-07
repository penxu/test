<template>
    <div class="safety_log_mian">
        <header><span :class="(logType == 0)?'active':''" @click="navCheck(0)">操作日志</span><span :class="(logType == 1)?'active':''" @click="navCheck(1)">登陆日志</span></header>
        <div class="seach-box">
            <div class="item">
                <span class="title">操作人</span>
                <i class="iconfont icon-pc-fine-add-staff" @click="addseachMember"></i>
            </div>
            <div class="item">
                <span class="title">部门</span>
                <i class="iconfont icon-pc-fine-add-staff" @click="addseachMember"></i>
            </div>
            <div class="item time-item">
                <span class="title">操作时间</span>
                <el-input placeholder="请输入内容"></el-input>-<el-input placeholder="请输入内容"></el-input>
            </div>
        </div>
    </div>
</template>
<script>
// import bus from '../../bus'
export default {
  name: 'logData',
  components: {},
  props: ['logData2'],
  data () {
    return {
      logData: this.logData2,
      logType: 0
    }
  },
  watch: {
    logData (value) {
      console.log(value)
    }
  },
  /* 页面加载后执行 */
  mounted () {
    this.token = JSON.parse(sessionStorage.requestHeader).TOKEN
  },
  methods: {
    /* 切换 */
    navCheck (type) {
      this.logType = type
    },
    addseachMember () {

    }
  }
}
</script>

<style lang="scss">
.safety_log_mian{
    header{height: 57px;margin-top: -30px;line-height: 60px;border-bottom: 1px solid #f2f2f2;
        span{border-left: 4px solid #fff;padding: 0 10px;cursor: pointer;margin-left: 20px;}
        span.active{color: #20BF9A;border-left: 4px solid #20BF9A;}
    }
    .seach-box{
        .item{display: inline-block;width: 308px;padding-left: 22px;line-height: 36px;height: 36px;margin-top: 22px;float: left;
            .title{padding-right: 20px;float: left;}
            .iconfont{font-size: 34px;}
        }
        .time-item{width: 440px;
            .title{padding-right: 10px;}
            .el-input{padding: 0 10px;margin: 0 10px;width: 150px;
                input{height: 36px;width: 150px;}
            }
        }
    }
}
</style>
