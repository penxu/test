<template>
  <span class="tree-expand">
    <template>
      <span class="tree-label">
        <span>{{DATA.name}}</span>
      </span>
            <el-dropdown trigger="click" @command="roleSet" v-if="DATA.id != 'zu' && DATA.id != 1" class="indexAnalysis">
              <span class="el-dropdown-link"><i class="iconfont icon-role-group"></i></span>
              <el-dropdown-menu slot="dropdown" >
                <el-dropdown-item command="0" v-if="dataId.indexOf(21) >= 0"><i class="iconfont icon-jiaoseguanlix1"></i>管理成员</el-dropdown-item>
                <el-dropdown-item command="1" v-if="dataId.indexOf(22) >= 0"><i class="iconfont icon-liebiao-bianji"></i>编辑</el-dropdown-item>
                <el-dropdown-item command="2" v-if="dataId.indexOf(23) >= 0"><i class="iconfont icon-pc-delete"></i>删除</el-dropdown-item>
              </el-dropdown-menu>
          </el-dropdown>
    </template>
  </span>
</template>

<script>
export default {
  components: {},
  props: [
    'DATA', 'roleModelData'
  ],
  data () {
    return {
      TEAMFACEPWD: 'hjhq2017Teamface',
      formLabelWidth: '70px',
      dialogWidth: '540px',
      dataPermission: [],
      dataId: this.roleModelData
    }
  },
  /* 页面加载后执行 */
  mounted () {
    console.log(this.dataId)
  },
  watch: {
    roleModelData (data) {
    }
  },
  methods: {
    roleSet: function (data) {
      this.$bus.emit('center-role-set', data)
    }
  }
}
</script>
