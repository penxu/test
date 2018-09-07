<template>
  <span class="edit-div"
        v-html="innerText"
        :contenteditable="canEdit"
        @focus="isLocked = true"
        @blur="isLocked = false"
        @keydown="stopEnter($event)"
        @input="changeText">
  </span>
</template>
<script>
export default{
  name: 'editDiv',
  props: {
    value: {
      type: String,
      default: ''
    },
    canEdit: {
      type: Boolean,
      default: true
    }
  },
  data () {
    return {
      // innerText: ' ' + this.value,
      innerText: this.value,
      isLocked: false
    }
  },
  watch: {
    'value' () {
      // if (!this.isLocked && !this.innerText) {
      if (!this.isLocked || !this.innerText) {
        this.innerText = this.value
      }
    }
  },
  methods: {
    changeText () {
      this.$emit('input', this.$el.innerHTML)
    },
    stopEnter (event) {
      // 阻止换行
      if (event.keyCode === 13) {
        event.preventDefault()
      }
    }
  },
  destroyed () {
    this.innerText = ''
    // this.isNum = 0
  }
}
</script>
<style lang="scss">
    .edit-div {
      display: inline-block;
      width: 91%;
      // height: 100%;
      min-height: 24px;
      min-width: 1px;
      // overflow: auto;
      word-break: break-all;
      // outline: none;
      user-select: text;
      // white-space: pre-wrap;
      // text-align: left;
      &[contenteditable = true]{
        -webkit-user-modify: read-write-plaintext-only;
        // user-modify: read-write-plaintext-only;
        // &:empty:before {
        //     content: attr(placeholder);
        //     display: block;
        //     color: #ccc;
        // }
      }
    }
</style>
