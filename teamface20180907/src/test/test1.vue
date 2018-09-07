<template>
  <div>
   测试生命周期1
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
    console.log('beforeCreate--1')
  },
  created () {
    console.log('created--1')
  },
  beforeMount () {
    console.log('beforeMount--1')
  },
  mounted () {
    console.log('mounted--1')
  },
  beforeUpdate () {
    console.log('beforeUpdate--1')
  },
  updated () {
    console.log('updated--1')
  },
  activated () {
    console.log('activated--1')
  },
  deactivated () {
    console.log('deactivated--1')
  },
  beforeDestroy () {
    console.log('beforeDestroy--1')
  },
  destroyed () {
    console.log('destroyed--1')
  },
  errorCaptured () {
    console.log('errorCaptured--1')
  }
}
</script>
