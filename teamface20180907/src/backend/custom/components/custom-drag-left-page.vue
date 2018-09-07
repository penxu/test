<template>
  <div class="custom-drag-left-wrip">
    <p class="unused-field">未使用字段 <i class="iconfont" :class="'icon-pc-background-module'"></i></p>
    <draggable v-model="unUsedLists" :options="unUsedDragOption" class="unused-components-box" @end="dragEnd">
      <div v-for="(item, index) in unUsedLists" class="del-components" :key="index">
        <div class="type-label"><span>{{item.label}}</span></div>
        <div class="type-text">{{item.typeText}}</div>
      </div>
    </draggable>
  </div>
</template>

<script>
import { mapState, mapGetters, mapMutations } from 'vuex'
import {HTTPServer} from '@/common/js/ajax.js'
import draggable from 'vuedraggable'
export default {
  name: 'CustomDragLeftPage',
  components: {
    draggable
  },
  props: {
    pageNum: {
      type: Number,
      default: 0
    }
  },
  data () {
    return {
      unUsedLists: []
    }
  },
  created () {
    let data = {bean: this.$route.query.bean, operationType: 1, pageNum: this.pageNum}
    this.getDisbaleLayout(data)
    this.$bus.on('shiftLayout', value => {
      this.getDisbaleLayout(data)
    })
  },
  methods: {
    dragEnd () {
      let unUsedLists = JSON.parse(JSON.stringify(this.unUsedLists))
      this.setUnusedLayout({rows: unUsedLists})
    },
    getDisbaleLayout (data) {
      HTTPServer.getDisableLayout(data, 'Loading').then((res) => {
        this.unUsedLists = res.rows
        this.setUnusedLayout({rows: res.rows})
      })
    },
    ...mapMutations({
      setUnusedLayout: 'CHILD_UNUSED_LAYOUT'
    })
  },
  computed: {
    ...mapGetters([
      'child_unused_layout'
    ]),
    ...mapState({
      child_unused_layout: state => state.custom.child_unused_layout
    }),
    unUsedDragOption () {
      return {
        animation: 100,
        group: { name: 'compontents', pull: true, put: false },
        sort: false,
        ghostClass: 'ghost'
      }
    }
  },
  watch: {
    pageNum (newVal) {
      if (newVal > 0) {
        let data = {bean: this.$route.query.bean, operationType: 1, pageNum: newVal}
        this.getDisbaleLayout(data)
      }
    },
    child_unused_layout: {
      handler: function (val, oldval) {
        console.log('未使用字段变了')
        this.unUsedLists = JSON.parse(JSON.stringify(this.child_unused_layout)).rows
      },
      deep: true// 对象内部的属性监听，也叫深度监听
    }
  }
}
</script>

<style lang="scss" scoped>
.custom-drag-left-wrip{
  flex: 0 0 300px;
  background: #E9EFF3;
  border-radius: 0 0 3px 3px;
  text-align: left;
  >p{
    font-size: 14px;
    color: #59656B;
    padding: 0 20px;
  }
  .unused-field{
    height: 50px;
    line-height: 50px;
    font-size: 16px;
    color: #4A4A4A;
    padding: 0 16px 0 27px;
    border-bottom: 1px solid #4891CD;
    box-shadow: 0 -2px 5px rgba(0, 0, 0, 0.18);
    .iconfont{
      float: right;
      cursor: pointer;
    }
  }
  .unused-components-box{
    padding: 12px 8px;
    .del-components{
      display: flex;
      height: 40px;
      line-height: 40px;
      margin: 0 0 10px;
      padding: 0 8px;
      background: #F4F6F9;
      border-radius: 4px;
      cursor: move;
      &:hover{
        background: #FFFFFF;
        box-shadow: 0 2px 4px 0 rgba(96,96,96,0.24);
        border-radius: 4px;
        >i{
          visibility: visible;
        }
      }
      .type-label{
        flex: 0 0 110px;
        color: #797979;
      }
      .type-text{
        flex: 1;
        height: 30px;
        line-height: 30px;
        margin: 5px 0;
        padding: 0 8px;
        color: #BBBBC3;
        border: 1px solid #BBBBC3;
        border-radius: 3px;
      }
      >i{
        visibility: hidden;
        font-size: 16px;
        color: #BBBBC3;
        margin: 12px 0 0 7px;
        cursor: pointer;
      }
    }
  }
}
</style>
