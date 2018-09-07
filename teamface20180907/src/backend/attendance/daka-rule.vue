<template>
  <div class="daka-rule">
    <!-- 添加按钮 -->
    <div class="add-btn" v-if="!openDakaSet">
      <el-button type="primary" @click="openDakaSet = true"><i class="iconfont icon-pc-paper-additi"></i> 添加考勤组</el-button>
    </div>
    <!-- 打卡规则设置组件 -->
    <daka-set v-if="openDakaSet" :detail="detail"></daka-set>
    <!-- 考勤组列表 -->
    <div class="manage-list" v-else>
      <el-table :data="manageList" style="width: 100%">
        <el-table-column prop="name" label="考勤名称" width="230">
        </el-table-column>
        <el-table-column label="考勤人数" width="150">
          <template slot-scope="scope">
            <span>{{ scope.row.attendance_number + '人' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="考勤类型" width="170">
          <template slot-scope="scope">
            <span>{{ scope.row.attendance_type === '0'? '固定班制': scope.row.attendance_type === '1' ? '排班制':'自由工时'}}</span>
          </template>
        </el-table-column>
        <el-table-column label="考勤时间" width="530">
          <template slot-scope="scope">
            <span>{{ scope.row.attendance_time }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <span class="edit" @click="openEdit(scope.row.id)">编辑</span>
            <span class="del" @click="openDel(scope.row.id)">删除</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 删除弹窗 -->
    <el-dialog
      title="删除"
      class="del-dialog"
      :visible.sync="delVisible"
      width="436px">
      <div class="del-content">
        <div class="pull-left">
          <i class="iconfont icon-bohui"></i>
        </div>
        <div class="pull-right">
          <p class="up-tip">提示：删除后不可恢复</p>
          <p class="down-tip">您确定要删除吗？</p>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="delVisible = false">取 消</el-button>
        <el-button type="primary" @click="sureDel()">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import dakaSet from './daka-set.vue'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'dakaRule', // 打卡规则组件
  components: {dakaSet},
  data () {
    return {
      detail: '', // 详情数据
      delVisible: false,
      openDakaSet: false, // 是否显示打卡规则设置组件
      // 班次管理列表
      manageList: [
        {
          'id': 2, // 记录ID
          'name': '考勤组A', // 考勤组名字
          'attendance_number': 10, // 考勤组人数
          'attendance_type': '1', // 考勤类型  0固定 1排班 2自由
          'attendance_time': '星期一,星期四,星期五,星期六' // 考勤时间
        },
        {
          'id': 2, // 记录ID
          'name': '考勤组A', // 考勤组名字
          'attendance_number': 10, // 考勤组人数
          'attendance_type': '2', // 考勤类型  0固定 1排班 2自由
          'attendance_time': '星期一,星期四,星期五,星期六' // 考勤时间
        }
      ]
    }
  },
  created () {
    // 获取列表
    this.getList()
  },
  mounted () {
    // 监听关闭打卡设置组件
    this.$bus.off('closeDakaSet')
    this.$bus.on('closeDakaSet', val => {
      this.openDakaSet = val
      this.detail = ''
      // 刷新列表
      this.getList()
    })
  },
  methods: {
    openEdit (id) {
      this.currentId = id
      // 根据id获取详情
      HTTPServer.scheduleFindDetail({'id': id}).then((res) => {
        console.log(res, '根据id获取详情')
        // 打开设置组件,并把详情数据发送过去
        this.detail = res
        this.openDakaSet = true
      })
    },
    sureDel () {
      HTTPServer.scheduleDel({'id': this.currentId}).then((res) => {
        this.$message({
          message: '删除成功',
          type: 'success'
        })
        // 关闭窗口
        this.delVisible = false
        // 刷新班次管理列表
        this.getList()
        this.currentId = ''
      })
    },
    openDel (id) {
      this.currentId = id
      this.delVisible = true
    },
    // 获取列表
    getList () {
      HTTPServer.scheduleFindWebList().then((res) => {
        this.manageList = res.dataList
      })
    }
  }
}
</script>
<style lang="scss">
  .daka-rule {
    // 添加按钮
    .add-btn {
      >.el-button {
        height: 32px;
        padding: 1px 10px;
        margin-left: 30px;
        margin-top: 5px;
        margin-bottom: 20px;
        .iconfont {
          font-size: 12px;
        }
      }
    }
  }
</style>
