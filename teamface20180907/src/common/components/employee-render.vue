<template>
  <span class="tree-expand">
    <template>
      <span class="tree-label" v-bind:class="(DATA.type == 1) ? 'emp':'dep'">
          <img v-if='DATA.picture && DATA.type == 1' :src="DATA.picture + '&TOKEN=' + token" />
          <div v-if='!DATA.picture && DATA.type == 1' class="simpName backImage">{{DATA.name | limitTo(-2)}}</div>
        <span class="name" :title="DATA.name">{{DATA.name}}</span>
      </span>
      <i class="iconfont" :class="'icon-pc-member-sign' + (DATA.checked ? '-ok':'') + ' ' + ((DATA.type == 1) ? 'emp':'dep')"></i>
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
