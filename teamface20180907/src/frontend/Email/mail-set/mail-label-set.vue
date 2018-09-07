<template>
  <div class="label-set">
    <!-- 按钮栏 -->
    <div class="btn-bar">
      <el-button type="info" plain style="width:94px;" @click="openDel()">删 除</el-button>
      <el-button type="success" plain @click="add()"><i class="iconfont icon-pc-paper-additi"></i>添加标签</el-button>
    </div>
    <!-- 列表 -->
    <el-table ref="multipleTable" :data="tableData3" tooltip-effect="dark" style="width: 100%;" @selection-change="handleSelectionChange">
      <!-- 选择框 -->
      <el-table-column type="selection" width="60px"></el-table-column>
      <!-- 标签 -->
      <el-table-column prop="name" label="标签" width="500px"></el-table-column>
      <!-- 状态 -->
      <el-table-column label="状态" width="200px">
        <template slot-scope="scope">{{ scope.row.status === '0'?'启用':'禁用' }}</template>
      </el-table-column>
      <!-- 操作 -->
      <el-table-column prop="date" label="操作">
        <template slot-scope="scope">
          <span class="operation" @click="edit(scope.row.id)">编辑</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加标签弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="addTextSignVisible"
      :closeOnClickModal="false"
      width="600px">
      <!-- 添加表单内容 -->
      <el-form ref="form" :model="form" label-width="80px">
        <!-- 标签名称 -->
        <el-form-item label="标签名称" prop="name" :rules="[{ required: true, message: '标签名称不能为空'}]">
          <el-input :maxlength="10" v-model.trim="form.name"></el-input>
        </el-form-item>
        <!-- 状态 -->
        <el-form-item label="状态" prop="status" :rules="[{ required: true, message: '状态不能为空'}]">
          <el-select v-model="form.status" placeholder="请选择">
            <el-option label="启用" value="0"></el-option>
            <el-option label="禁用" value="1"></el-option>
          </el-select>
        </el-form-item>
        <!-- 选择颜色 -->
        <el-form-item label="选择颜色">
          <span class="label-box" :style="{background: currentColor}">{{this.form.name}}</span>
        </el-form-item>
        <!-- 颜色选择 -->
        <el-form-item label='' :label-width='formLabelWidth' class="colorbox">
          <el-row :gutter="20">
            <el-col :span="4"  v-for="color in colorData" :key="color.id">
              <div class="grid-content bg-purple" :style="{background: color.name}" @click="checkColor(color.name)">
                <i v-if="color.checked" class="el-icon-check"></i>
              </div>
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="onSubmit('form',currentId)">保 存</el-button>
        <el-button @click="addTextSignVisible = false">取 消</el-button>
      </span>
    </el-dialog>
    <!-- 分页 -->
    <el-pagination :current-page='currentPage4'
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :page-sizes='[10, 20, 50, 100]'
      :page-size='pageSizes'
      layout='total, sizes, prev, pager, next, jumper'
      :total='totalRows'>
    </el-pagination>

    <!-- 删除弹窗 -->
    <el-dialog
      title="删除"
      :visible.sync="delVisible"
      width="300px">
      <!-- 添加账号表单内容 -->
      你确定要删除吗?
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="del">确 定</el-button>
        <el-button @click="delVisible = false">取 消</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'

export default {
  name: 'mailLabelSet', // 邮件-标签设置组件
  data () {
    return {
      tableData3: [],
      multipleSelection: [], // 已选账户数据
      addCardSignVisible: false, // 添加名片签名弹窗
      addTextSignVisible: false, // 添加文本签名弹窗
      form: {
        name: '',
        status: '0'
      },
      formLabelWidth: '97px',
      colorData: [
        {'id': 1, 'name': '#F9B239', 'checked': true},
        {'id': 2, 'name': '#FF1D32'},
        {'id': 3, 'name': '#FF7777'},
        {'id': 4, 'name': '#DD361A'},
        {'id': 5, 'name': '#DB6A20'},
        {'id': 6, 'name': '#F57221'},
        {'id': 7, 'name': '#FFEB57'},
        {'id': 8, 'name': '#B4E550'},
        {'id': 9, 'name': '#73B32D'},
        {'id': 10, 'name': '#119E29'},
        {'id': 11, 'name': '#00781B'},
        {'id': 12, 'name': '#51D0B1'},
        {'id': 13, 'name': '#00C2C1'},
        {'id': 14, 'name': '#5CC1FC'},
        {'id': 15, 'name': '#0091FA'},
        {'id': 16, 'name': '#3057E5'},
        {'id': 17, 'name': '#0051AF'},
        {'id': 18, 'name': '#FF85BE'},
        {'id': 19, 'name': '#F52E94'},
        {'id': 20, 'name': '#9856D9'},
        {'id': 21, 'name': '#5821A7'},
        {'id': 22, 'name': '#C0C0C0'},
        {'id': 23, 'name': '#8C8C8C'},
        {'id': 24, 'name': '#000000'}
      ],
      currentColor: '#F9B239', // 当前标签颜色
      delVisible: false,
      currentId: '',
      dialogTitle: '添加标签',
      currentPage4: 1, // 当前页码
      pageSizes: 10, // 一页总条数
      totalRows: 0 // 总条数
    }
  },
  methods: {
    openDel () {
      // 列表没有数据不能打开
      if (this.tableData3.length < 1) {
        return false
      }
      // 没有选择不能弹窗
      if (this.multipleSelection.length < 1) {
        this.$message.error('请选择至少一条数据')
        return false
      }
      this.delVisible = true
    },
    add () {
      this.dialogTitle = '添加标签'
      this.addTextSignVisible = true
    },
    // 已选账号
    handleSelectionChange (val) {
      this.multipleSelection = val
      console.log(this.multipleSelection, 'this.multipleSelection')
    },
    // 保存添加账号信息
    onSubmit (formName, id) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          // 获取表单内容,发送保存请求
          let obj = {
            'name': this.form.name, // 标签名称
            'status': this.form.status, // 标签状态
            'boarder': this.currentColor // 标签边框
          }
          if (id) {
            obj.id = id
            // 编辑
            HTTPServer.mailTagEdit(obj).then((res) => {
              // if (res.response.code === 1001) {
              // 关闭弹窗
              this.addTextSignVisible = false
              this.getLabelList()
              // 清空表单内容
              this.form = {
                'name': '', // 标签名称
                'status': '' // 标签状态
              }
              this.currentId = ''
              // 重置表单
              this.$refs['form'].resetFields()
              // 刷新父组件标签列表
              this.$bus.$emit('refreshLabelContent', true)
              this.$message({
                message: '执行成功',
                type: 'success'
              })
              // }
            })
          } else {
            // 添加
            HTTPServer.mailTagSave(obj).then((res) => {
              console.log(res, '保存添加账号信息')
              // 保存成功,关闭弹窗,清空选项
              // if (res.response.code === 1001) {
              // 关闭弹窗
              this.addTextSignVisible = false
              this.getLabelList()
              // 清空表单内容
              this.form = {
                'name': '', // 标签名称
                'status': '' // 标签状态
              }
              // 重置表单
              this.$refs['form'].resetFields()
              // 刷新父组件标签列表
              this.$bus.$emit('refreshLabelContent', true)
              this.$message({
                message: '执行成功',
                type: 'success'
              })
              // }
            })
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    // 删除
    del () {
      if (this.multipleSelection.length < 1) {
        this.$message.error('请选择至少一条数据')
        return false
      }
      let idArr = []
      // 获取multipleSelection每个选项的id,拼接字符串
      this.multipleSelection.map((item) => {
        idArr.push(item.id)
      })
      console.log(idArr.join())
      // 发送删除请求===========
      HTTPServer.delMailTag({'id': idArr.join()}).then((res) => {
        console.log(res, '删除的结果')
        // 删除成功 关闭窗口/提示消息
        this.delVisible = false
        this.getLabelList()
        // 刷新父组件标签列表
        this.$bus.$emit('refreshLabelContent', true)
        this.$message({
          message: res.response.describe,
          type: 'success'
        })
      })
    },
    // 编辑
    edit (id) {
      this.dialogTitle = '编辑标签'
      this.currentId = id
      // 根据id获取内容
      HTTPServer.mailTagQueryById({'id': id}).then((res) => {
        console.log(res, '根据id获取内容')
        this.form = res
        this.currentColor = res.boarder
        // 显示弹窗
        this.addTextSignVisible = true
        this.checkColor(this.currentColor)
      })
    },
    /** 选择颜色  传入16进制颜色值 */
    checkColor (data) {
      for (var i = 0; i < this.colorData.length; i++) {
        this.colorData[i].checked = false
        if (this.colorData[i].name === data) {
          this.colorData[i].checked = true
          this.$set(this.colorData, i, this.colorData[i])
        }
      }
      this.currentColor = data
      console.log(data, 'color')
    },
    // 设置一页显示的数据条数
    handleSizeChange (val) {
      this.pageSizes = val
      this.getLabelList()
    },
    // 当前第几页
    handleCurrentChange (val) {
      this.currentPage4 = val
      this.getLabelList()
    },
    // 获取标签列表
    getLabelList () {
      HTTPServer.mailTagQueryList({pageSize: this.pageSizes, pageNum: this.currentPage4}).then((res) => {
        console.log(res, '获取标签列表')
        this.tableData3 = res.dataList
        this.currentPage4 = res.pageInfo.pageNum // 当前页码
        this.pageSizes = res.pageInfo.pageSize // 一页总条数
        this.totalRows = res.pageInfo.totalRows // 总条数
      })
    }
  },
  watch: {
    // 关闭文本弹窗时就复位form
    addTextSignVisible: function (newaddAccountVisible, oldaddAccountVisible) {
      if (!newaddAccountVisible) {
        // 清空this.form
        this.form = {
          name: '',
          status: '0'
        }
        // 重置颜色
        this.currentColor = '#F9B239'
        this.checkColor(this.currentColor)
        // 重置表单
        this.$refs['form'].resetFields()
      }
    }
  }
}
</script>

<style lang="scss">
  .label-set {
    height: 100%;
    // 按钮栏
    .btn-bar {
      i {
        font-size: 13px;
        margin: 0 10px 0 -5px;
      }
      button {
        float: right;
        width: 140px;
        height: 36px;
        padding-top: 10px;
        margin-left: 14px;
        border: none;
      }
      .el-button--success.is-plain {
        background: #409EFF;
        color: #fff;
      }
    }
    // 添加账号弹窗
    .el-dialog {
      .el-dialog__body {
        padding-left: 25px;
      }
    }
    // 操作
    .operation {
      font-size: 14px;
      color: #409EFF;
      cursor: pointer;
    }
    .colorbox{
      .el-form-item__label{width: 92px!important;}
    }
    .el-row{width: calc(100% - 15px);
      .el-col{width: 34px;padding: 5px!important;
        .grid-content{height: 24px;background: red;border-radius: 3px;}
        .grid-content{
          i{color: #fff;font-size: 16px;float: left;margin: 4px 0 0 4px;}
        }
      }
    }
    // 表单样式
    .el-form {
      // 表单项
      .el-form-item {
        margin-bottom: 4px;
        .el-form-item__label {
          padding: 0;
        }
        // 报错提示信息
        .el-form-item__error {
          top: 67%;
          left:10px;
        }
      }
      // 收件服务器选项
      .el-checkbox-group {
        .el-form-item {
          display: inline-block;
        }
      }
      // 已发箱是否同步
      .el-form-item__label {
        width: auto !important;
      }
      .el-input {
        width: 99%;
        input {
          width: 455px;
          height: 36px;
          float: right;
        }
      }
      // 接收服务器类型 下拉框
      .el-select {
        float: right;
        margin-bottom: 15px;
        .el-input {
          height: 36px;
        }
      }
      // SSL
      // .el-checkbox {
      //   float: left;
      // }
      .label-box {
        margin-left: 14px;
        display: inline-block;
        line-height: 20px;
        min-width: 96px;
        text-align: center;
        border-radius: 3px;
        color: #fff;
      }
    }
  }
</style>
