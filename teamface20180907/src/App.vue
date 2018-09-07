<template>
  <div id="app">
    <router-view/>
    <audio src="/static/file/8855.mp3" id="promptTone">
      您的浏览器不支持 audio 标签。
    </audio>
  </div>
</template>

<script>
// import chatjs from '@/common/js/chat.2.js'
import {employee} from '@/common/js/employee.js'
export default {
  name: 'App',
  watch: {
    // 监听路由变化
    $route (to, from) {
      /** 监听更新个人信息 */
      if (to.name === 'MainBackend') {
        employee.queryInfo(this)
      }
      if (from.matched.length > 0) {
        if (to.matched[0].name === 'Frontend' && from.matched[0].name === 'MainBackend') {
          employee.queryInfo(this)
        }
      }
    }
  },
  /* 页面加载后执行 */
  mounted () {
    // chatjs.ws_connect(this)
  }
}
</script>

<style>
#app {height: 100%;
}
</style>
