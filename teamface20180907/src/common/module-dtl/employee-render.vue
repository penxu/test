<template>
  <span class="tree-expand">
    <template>
      <span class="tree-label" v-bind:class="'type' + DATA.type">
          <img v-if='DATA.picture && DATA.type == 1' :src="DATA.picture + '&TOKEN=' + token" />
          <div v-if='!DATA.picture && DATA.type == 1' class="simpName backImage">{{DATA.name | limitTo(-2)}}</div>
        <span>{{DATA.name}}</span>
      </span>
      <i v-if="DATA.type == 1" class="iconfont icon-pc-paper-privat" @click="privateChat($event, DATA)"></i>
    </template>
  </span>
</template>

<script>
export default {
  components: {},
  props: [
    'DATA'
  ],
  data () {
    return {
      token: JSON.parse(sessionStorage.requestHeader).TOKEN
    }
  },
  /* 页面加载后执行 */
  mounted () {

  },
  methods: {
    privateChat (event, data) {
      this.$bus.emit('private-chat', event, data)
    }
  }
}
</script>
