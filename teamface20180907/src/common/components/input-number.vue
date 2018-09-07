<template>
  <div class="input-number-wrip">
    <a @click="handleDown"><i class="el-icon-minus"></i></a>
    <input type="number" :value="currentVal" @change="handleChange">
    <span class="after-text">px</span>
    <a @click="handleUp"><i class="el-icon-plus"></i></a>
  </div>
</template>

<script>
import { mapState, mapGetters, mapMutations } from 'vuex'
export default {
  name: 'InputNumber',
  props: {
    name: {
      type: String
    },
    max: {
      type: Number,
      default: Infinity
    },
    min: {
      type: Number,
      default: Infinity
    },
    value: {
      type: Number,
      default: 0
    }
  },
  data () {
    return {
      currentVal: this.value,
      appearance: {}
    }
  },
  created () {
    this.appearance = JSON.parse(JSON.stringify(this.custom_appearance))
  },
  mounted () {
    this.updateValue(this.value)
  },
  methods: {
    // 更新值
    updateValue (val) {
      if (val > this.max) val = this.max
      if (val < this.min) val = this.min
      this.currentVal = val
    },
    // 增加
    handleUp () {
      if (this.currentVal >= this.max) return
      this.currentVal += 1
    },
    // 减少
    handleDown () {
      if (this.currentVal <= this.min) return
      this.currentVal -= 1
    },
    // 值变化
    handleChange (event) {
      let val = Number(event.target.value.trim())
      this.currentVal = val
      if (val > this.max) {
        this.currentVal = this.max
      } else if (val < this.min) {
        this.currentVal = this.min
      }
    },
    ...mapMutations({
      setCustomAppearance: 'CUSTOM_APPEARANCE'
    })
  },
  computed: {
    ...mapGetters([
      'custom_appearance'
    ]),
    ...mapState({
      custom_appearance: state => state.custom.custom_appearance
    })
  },
  watch: {
    currentVal (newVal) {
      let appearance = JSON.parse(JSON.stringify(this.custom_appearance))
      appearance[this.name] = newVal
      this.setCustomAppearance(appearance)
    },
    value (val) {
      this.updateValue(val)
    }
  }
}
</script>

<style lang="scss" scoped>
.input-number-wrip{
  position: relative;
  display: flex;
  width: 126px;
  margin: 0 0 0 -1px;
  >a{
    display: block;
    flex: 0 0 30px;
    height: 30px;
    background: #FFFFFF;
    border: 1px solid #CACAD0;
    text-align: center;
    cursor: pointer;
    i{
      font-size: 16px;
      color: #979797;
    }
  }
  >input{
    width: 60px;
    height: 30px;
    line-height: 30px;
    font-size: 14px;
    padding: 0 8px;
    color: #424242;
    border: none;
    border-radius: 0;
    border-top: 1px solid #CACAD0;
    border-bottom: 1px solid #CACAD0;
  }
  .after-text{
    position: absolute;
    line-height: 30px;
    top: 0;
    right: 40px;
    font-size: 12px;
    color: #B2B2B2;
  }
}
</style>
