<template>
 <div class="developing-wrip">
    <div class="border"></div>
    <p>功能正在开发中～敬请期待～～</p>
    <a>知道了</a>
  </div>
</template>

<script>
export default {
  name: 'Developing',
  data () {
    return {
    }
  },
  created () {
  },
  methods: {

  }
}
</script>

<style lang="scss" scoped>
.developing-wrip{
  width: 600px;
  height: 360px;
  margin: 0 auto;
  background: url('../../assets/developing.png') no-repeat center;
  .border{
    height: 10px;
    background: #0062B1;
    box-shadow: 0 0 2px 0 rgba(0,0,0,0.50);
  }
  >p{
    font-size: 16px;
    color: rgba(0,0,0,0.85);
    line-height: 24px;
    margin: 230px 0 30px;
  }
  >a{
    display: inline-block;
    width: 146px;
    height: 38px;
    line-height: 38px;
    color: #1890FF;
    border: 1px solid #1890FF;
    border-radius: 5px;
    cursor: pointer;
  }
}
</style>
