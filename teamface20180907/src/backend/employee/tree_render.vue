<template>
  <span class="tree-expand">
    <template>
      <span class="tree-label">
        <span>{{DATA.name}}</span>
      </span>
      <!-- <span @click='departmentSet(1)'>设置</span> -->
      <!-- <span> -->
            <el-dropdown trigger="click" @command="departmentSet" :class="'floor' + DATA.floor">
              <span class="el-dropdown-link"><i class="iconfont icon-nav-personal-setting"></i></span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="0">添加子部门</el-dropdown-item>
              <el-dropdown-item command="1">编辑部门</el-dropdown-item>
              <el-dropdown-item command="2" v-if="DATA.id !== 1">删除部门</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        <!-- </span> -->
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
      TEAMFACEPWD: 'hjhq2017Teamface',
      formLabelWidth: '70px',
      dialogWidth: '540px'
    }
  },
  /* 页面加载后执行 */
  mounted () {

  },
  methods: {
    departmentSet: function (data) {
      this.$bus.emit('department-set', data)
    }
  }
}
</script>
