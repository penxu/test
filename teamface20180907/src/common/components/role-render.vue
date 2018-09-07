<template>
  <span class="tree-expand">
    <template>
      <span class="tree-label role">
          <img v-if='DATA.picture && DATA.type == 2' v-bind:src="DATA.picture" />
          <div v-if='!DATA.picture && DATA.type == 2' class="simpName backImage">{{DATA.name | limitTo(2)}}</div>
        <span>{{DATA.name}}</span>
      </span>
      <i class="iconfont" :class="'icon-pc-member-sign' + (DATA.checked ? '-ok':'') + ' ' + ((DATA.type == 2) ? '':'none')"></i>
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
    }
  },
  /* 页面加载后执行 */
  mounted () {

  },
  methods: {
  }
}
</script>
