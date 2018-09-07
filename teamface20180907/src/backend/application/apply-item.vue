<template>
  <div class='apply-item-main' @click="applyDetail($event, apply)">
    <div class="icon-box" :style='{background:apply.icon_color}'>
      <img class="icon" :src="apply.icon_url + '&TOKEN=' + token" v-if="apply.icon_type == 1" />
      <i class="iconfont icon" :class="apply.icon_url" v-if="apply.icon_type != 1"></i>
    </div>
    <div class="content">
      <span class="app-name" v-if="apply.template_name.length > 6">{{apply.template_name | limitTo(4)}}…</span>
      <span class="app-name" v-if="apply.template_name.length <= 6">{{apply.template_name}}</span>
      <div class="app-type">{{apply.type_name}}</div>
      <el-rate v-model="apply.star_level" disabled :colors="['#F5A623', '#F5A623', '#F5A623']" allow-half score-template="{value}"></el-rate>
      <p v-if="!apply.price || apply.price == 0" class="price">免费</p>
      <p class="price" style="color: #FF6F00;" v-else>￥{{apply.price}}</p>
    </div>
  </div>
</template>
<script>
import {appIndustry} from '@/common/js/constant.js'
import tool from '@/common/js/tool.js'
export default {
  components: {},
  props: ['applyData'],
  data () {
    return {
      responseData: this.applyData,
      activeName: '1',
      seachvalue: '',
      token: JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN,
      apply: {
        template_name: ''
      }
    }
  },
  watch: {
    applyData (data) {}
  },
  mounted () {
    this.apply = JSON.parse(JSON.stringify(this.applyData))
    var contains = tool.contains(appIndustry.classification, 'id', this.apply, 'func_type')
    this.apply.type_name = contains ? contains.label : ''
    this.apply.star_level = Math.round(this.apply.star_level)
  },
  methods: {
    /** 进入详情 */
    applyDetail (e, data) {
      this.$router.push({name: 'ApplicationParticulars', query: {userId: data.template_id}})
    }
  }
}
</script>

<style lang="scss">
.apply-item-main{
  display: inline-block;
  margin: 20px 20px 0 0;
  .icon-box{
    display: inline-block;
    border-radius: 4px;
    text-align: center;
    width: 80px;
    height: 80px;
    line-height: 80px;
    float: left;
    img{
      width: 43px;
      height: 43px;
    }
    .iconfont{
      font-size: 43px;
      color: #fff;
    }
  }
  .content{
    display: inline-block;
    padding-left: 8px;
    .app-name{
      display: inline-block;
      width: 84px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      font-size: 14px;
      color: #333;
    }
    .app-type{
      font-size: 12px;
      color: #666;
      margin-bottom: 5px;
    }
    .el-rate__icon{
      font-size: 14px;
      color: #D8D8D8;
    }
    .price{
      font-size: 13px;
      color: #666;
    }
  }
}
</style>
