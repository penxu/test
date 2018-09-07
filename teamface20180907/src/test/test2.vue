<template>
  <div>
   测试生命周期2
  </div>
</template>

<script>
export default {
  name: 'Test1',
  data () {
    return {

    }
  },
  beforeCreate () {
    console.log('beforeCreate--2')
  },
  created () {
    console.log('created--2')
  },
  beforeMount () {
    console.log('beforeMount--2')
  },
  mounted () {
    console.log('mounted--2')
  },
  beforeUpdate () {
    console.log('beforeUpdate--2')
  },
  updated () {
    console.log('updated--2')
  },
  activated () {
    console.log('activated--2')
  },
  deactivated () {
    console.log('deactivated--2')
  },
  beforeDestroy () {
    console.log('beforeDestroy--2')
  },
  destroyed () {
    console.log('destroyed--2')
  },
  errorCaptured () {
    console.log('errorCaptured--2')
  }
}
</script>
