<template>
  <div class="integration-main-wrip">
    <div class='header'>
        <i class="iconfont icon-jiekoujicheng"></i>
          <span>数据接口集成</span>
    </div>
    <div class="integration-content">
      <h3>OpenAPI服务接口</h3>
      <div class="APIsite"><span>API地址：</span><input type="text" placeholder="http:\\"><a href="javascript:;">接口说明</a></div>
      <div class="Authtoken"><span>Authtoken：</span><input type="text" placeholder="http:\\"><a href="javascript:;">重置</a></div>
      <div class="annotation">注：Authtoken是重要的接口调用凭证，请注意保密！如不慎泄密，请立即重置您的Authtoken，重置后将新Authtoken发送给接口调用方修改凭证！</div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'IntegrationMain',
  data () {
    return {
    }
  }
}
</script>

<style lang="scss" scoped>
.integration-main-wrip{
  height: 100%;
  padding: 0 25px 0 30px;
  overflow: auto;
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
  .integration-content{
      padding:0 40px;
      h3{
        line-height: 80px;
        font-size: 20px;
        font-weight: 700;
        color: #000;
        }
      .APIsite{
          margin-left: 20px;
        }
      .APIsite,.Authtoken{
          margin-bottom: 30px;
          color: #000;
          font-size: 15px;
        input{
            width: 500px;
            margin:0 20px;
            padding-left: 15px;
            height: 35px;
            border: 1px solid #ccc
          }
        a{
            font-size: 14px;
            color: #0099CC;
          }
      }
    .annotation{
      color: red;
      font-size: 16px;
    }
  }
}
</style>
