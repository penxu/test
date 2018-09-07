<template>
  <div class="approval-relation">
    <el-button class="add-btn" @click="relationVisible = true" type="primary"><i class="iconfont icon-nav-quickly-add"></i>添加</el-button>
    <!-- 关联审批单列表 -->
    <div class="relation-list">
      <el-table :data="relationList" style="width: 100%">
        <el-table-column prop="relevance_type" label="关联审批单" width="246">
        </el-table-column>
        <el-table-column label="修正状态" width="446">
          <template slot-scope="scope">
            <span>{{ scope.row.relevance_status }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <span class="edit">编辑</span>
            <span class="del" @click="delVisible = true">删除</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 添加关联审批单弹窗 -->
    <el-dialog
      title="关联审批单"
      class="relation-dialog"
      :visible.sync="relationVisible"
      width="600px">
      <!-- 关联审批单 -->
      <div class="inputWiFi">
        <span>关联审批单</span>
        <el-select v-model="relevanceType" placeholder="请选择">
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
        <p class="wifi-tip" v-if="relevanceType === ''">请选择关联审批单</p>
      </div>
      <!-- 修正状态 -->
      <div class="inputWiFi">
        <span>修正状态</span>
        <el-select v-model="relevanceStatus" placeholder="请选择">
          <el-option key="1" label="缺卡" value="1"></el-option>
          <el-option key="2" label="旷工" value="2"></el-option>
          <el-option key="0" label="不修正" value="0"></el-option>
        </el-select>
        <p class="wifi-tip" v-if="relevanceStatus === '1'">审批通过后，该时间段内的考勤“缺卡”状态将会被自动修正为“正常”。</p>
        <p class="wifi-tip" v-if="relevanceStatus === '2'">审批通过后，对应时间段内所有正常上班日期内的考勤统计将都不会以旷工处理。</p>
        <p class="wifi-tip" v-if="relevanceStatus === '0'">审批通过后，该审批单会纳入考勤月统计当中</p>
      </div>
      <!-- 开始时间 -->
      <div class="inputWiFi">
        <span>开始时间对应时间</span>
        <el-select v-model="startTime" placeholder="请选择">
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
        <p class="wifi-tip" v-if="startTime === ''">请选择开始时间对应时间</p>
      </div>
      <!-- 结束时间 -->
      <div class="inputWiFi">
        <span>结束时间对应时间</span>
        <el-select v-model="endTime" placeholder="请选择">
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
        <p class="wifi-tip" v-if="endTime === ''">请选择结束时间对应时间</p>
      </div>
      <!-- 时长 -->
      <div class="inputWiFi">
        <span>时长对应字段</span>
        <el-select v-model="duration" placeholder="请选择">
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
        <p class="wifi-tip" v-if="duration === ''">请选择时长对应字段</p>
      </div>
      <!-- 计算单位 -->
      <div class="inputWiFi">
        <span>时长计算单位</span>
        <el-radio-group v-model="durationUnit">
          <el-radio :label="0">按天</el-radio>
          <el-radio :label="1">按小时</el-radio>
        </el-radio-group>
        <p class="radio-tip">考勤统计报表会根据时长计算单位输出结果</p>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="relationVisible = false">取 消</el-button>
        <el-button type="primary" @click="relationVisible = false">确 定</el-button>
      </span>
    </el-dialog>
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
        <el-button type="primary" @click="delVisible = false">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
export default {
  name: 'approvalRelation',
  data () {
    return {
      // 关联审批单列表
      relationList: [
        {
          'id': 1, // 记录ID
          'relevance_type': 0, // 关联审批单类型
          'relevance_status': 0 // 修正状态
        },
        {
          'id': 2, // 记录ID
          'relevance_type': 0, // 关联审批单类型
          'relevance_status': 0 // 修正状态
        },
        {
          'id': 3, // 记录ID
          'relevance_type': 0, // 关联审批单类型
          'relevance_status': 0 // 修正状态
        }
      ],
      options: [
        {
          value: '选项1',
          label: '黄金糕'
        }, {
          value: '选项2',
          label: '双皮奶'
        }, {
          value: '选项3',
          label: '蚵仔煎'
        }, {
          value: '选项4',
          label: '龙须面'
        }, {
          value: '选项5',
          label: '北京烤鸭'
        }],
      relationVisible: false, // 添加关联审批单弹窗
      delVisible: false, // 删除弹窗
      relevanceType: '', // 关联审批单类型
      relevanceStatus: '0', // 修正状态 缺卡1 旷工2 不修正0
      startTime: '', // 开始时间
      endTime: '', // 结束时间
      duration: '', // 时长字段
      durationUnit: 1 // 时长计算单位 0按天  1按小时
    }
  }
}
</script>
<style lang="scss">
.approval-relation {
  margin-top: 5px;
  .el-button {
    height: 32px;
    padding: 0 20px;
  }
  .add-btn {
    margin-left: 20px;
  }
  .relation-list {
    margin-top: 20px;
  }
  // 添加关联审批单弹窗
  .relation-dialog {
    .inputWiFi {
      overflow: hidden;
      >span {
        margin-top: 20px;
        font-size: 14px;
        color: #333333;
        float: left;
      }
      .el-select {
        margin-top: 10px;
        margin-bottom: 10px;
        width: 422px;
        float: right;
        margin-right: 10px;
        .el-input__inner {
          background-color: #FBFBFB;
        }
      }
      .el-radio-group {
        margin-bottom: 10px;
        float: right;
        margin-right: 284px;
        margin-top: 24px;
      }
    }
    .radio-tip {
      margin-top: 20px;
      margin-bottom: 10px;
      margin-left: 128px;
      font-size: 12px;
      color: #666666;
      float: left;
    }
    .wifi-tip {
      margin-left: 128px;
      font-size: 12px;
      color: #FF0000;
      float: left;
    }
  }
}
</style>
