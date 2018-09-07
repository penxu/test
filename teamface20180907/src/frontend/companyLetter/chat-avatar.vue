 <!-- 头像组件 -->
<template>
  <div class="chat-avatar-mian">
    <!-- 群 -->
    <img src="/static/img/im/company.png" v-if="avatar.chat_type == 1 && avatar.type == 0" />
    <img src="/static/img/im/group.png" v-if="avatar.chat_type == 1 && avatar.type == 1" />
    <!-- 成员 -->
    <img v-if="avatar.chat_type == 2 && avatar.picture" :src="avatar.picture+'&TOKEN='+token" />
    <div class="simpName" v-if="avatar.chat_type == 2 && !avatar.picture">{{(avatar.employee_name || avatar.name) | limitTo(-2)}}</div>
    <!-- 助手 -->
    <img :src="'/static/img/im/' + avatar.type + '.png'" v-if="avatar.chat_type == 3 && avatar.type != 1" />
    <div class="assistant" v-if="avatar.chat_type == 3 && avatar.type == 1" :style='(avatar.icon_type == 1) ? "" : {background:avatar.icon_color}'>
        <img class="icon" :src="avatar.icon_url + '&TOKEN=' + token" v-if="avatar.icon_type == 1" />
        <i class="iconfont" :class="avatar.icon_url + '-b'" v-if="avatar.icon_type != 1"></i>
    </div>
    <!-- <icon-img v-if="avatar.chat_type == 3 && avatar.type == 1" :type="avatar.icon_type" :url="avatar.icon_url" :size="iconStyle(avatar.icon_color)" :isModule="true"></icon-img> -->
  </div>
</template>

<script>
import IconImg from '@/common/components/icon-img'
export default {
  props: ['avatar'],
  components: {IconImg},
  data () {
    return {
      token: '',
      responseData: this.avatar
    }
  },
  watch: {
  },
  /* 页面加载后执行 */
  mounted () {
    this.token = JSON.parse(sessionStorage.requestHeader).TOKEN
  },
  methods: {
    // 图标样式
    iconStyle (color) {
      return {
        border: '30px',
        content: '26px',
        background: color,
        radius: '50%'
      }
    }
  }
}
</script>

<style lang='scss'>
</style>
