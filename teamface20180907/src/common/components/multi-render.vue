<template>
  <span class="tree-expand">
    <template>
      <span class="tree-label">
          <i class="back iconfont icon-pc-member-organ" v-if="DATA.type == 0"></i>
          <i class="back iconfont icon-pc-member-company" v-if="DATA.type == 4"></i>
          <img v-if='DATA.picture && DATA.type == 1' v-bind:src="DATA.picture + '&TOKEN=' + token" />
          <div v-if='!DATA.picture && DATA.type == 1' class="simpName backImage">{{DATA.name | limitTo(2)}}</div>
        <span class="name">{{DATA.name}}</span>
      </span>
      <i class="iconfont" :class="'icon-pc-member-sign' + (DATA.checked ? '-ok':'') + ' ' + ((DATA.type == 1) ? 'emp':'dep')" v-if="DATA.type != 2"></i>
      <i class="iconfont" :class="'icon-pc-member-sign' + (DATA.checked ? '-ok':'')" v-if="DATA.type == 2"></i>
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
  }
}
</script>
