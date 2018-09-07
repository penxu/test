<template>
  <div class="condition-show-wrip">
    <div class="condition-list">
      <div class="item" v-for="(item,index) in conditionList" :key="index">
        {{index+1}}. {{item.field_label}}<span>{{item.operator_label}}</span>{{item.result_value | toText(item.show_type, item)}}
      </div>
    </div>
    <p v-if="highCondition && conditionList.length > 1">高级条件：{{highCondition}}</p>
  </div>
</template>

<script>
export default {
  name: 'ConditionShow',
  props: ['conditionList', 'highCondition'],
  data () {
    return {
    }
  },
  filters: {
    toText (value, type, item) {
      let text
      let list = []
      switch (type) {
        case 'personnel':case 'department':
          if (value) {
            value.map((item) => {
              list.push(item.name)
            })
            text = list.toString()
          }
          break
        case 'picklist': case 'multi':
          if (value) {
            value.map((item) => {
              list.push(item.label)
            })
            text = list.toString()
          }
          break
        case 'datetime':
          if (value) {
            switch (item.operator_value) {
              case 'BETWEEN':
                list = value.split(',')
                text = new Date(Number(list[0])).toLocaleDateString() + '到' + new Date(Number(list[1])).toLocaleDateString()
                break
              case 'BEFORE': case 'AFTER':
                text = value + '天'
                break
              default:
                text = ''
                break
            }
          }
          break
        default:
          text = value
          break
      }
      return text
    }
  }
}
</script>

<style lang="scss">
.condition-show-wrip {
  .condition-list{
    margin: 5px 0 0 0;
    color: rgba(0,0,0,0.65);
    .item{
      height: 20px;
      line-height: 20px;
      margin: 0 0 10px 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      span{
        color: #1890FF;
        margin: 0 10px;
      }
    }
  }
  p{
    font-size: 16px;
    color: rgba(0,0,0,0.85);
    line-height: 24px;
    margin: 0 0 16px 0;
  }
}
</style>
