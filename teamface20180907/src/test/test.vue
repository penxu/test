<template>
  <div>
    <p>测试界面</p>
    <el-button type="danger" plain @click="condition = !condition">危险按钮</el-button>
    <div v-if="condition">
      <test1></test1>
    </div>
    <div v-if="!condition">
      <test2></test2>
    </div>
  </div>
</template>

<script>
import Test1 from './test1'
import Test2 from './test2'
export default {
  name: 'Test',
  components: {
    Test1,
    Test2
  },
  data () {
    return {
      condition: true
    }
  }
}
</script>

<style>
</style>
