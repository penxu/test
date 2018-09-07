<template>
  <div class="sign-set">
    <!-- 按钮栏 -->
    <div class="btn-bar">
      <el-button type="info" plain style="width:94px;" @click="openDel()">删 除</el-button>
      <el-button type="success" plain @click="addSign()"><i class="iconfont icon-pc-paper-additi"></i>添加名片签名</el-button>
      <el-button type="success" plain @click="addText()"><i class="iconfont icon-pc-paper-additi"></i>添加文本签名</el-button>
    </div>
    <!-- 签名列表 -->
    <el-table ref="multipleTable" :data="tableData3" tooltip-effect="dark" style="width: 100%;" @selection-change="handleSelectionChange">
      <!-- 选择框 -->
      <el-table-column type="selection" width="60px"></el-table-column>
      <!-- 签名标题 -->
      <el-table-column prop="title" label="签名标题" width="260px"></el-table-column>
      <!-- 适用账号 -->
      <el-table-column prop="account" label="适用账号" width="210px"></el-table-column>
      <!-- 创建人 -->
      <el-table-column prop="employee_name" label="创建人" width="150px"></el-table-column>
      <!-- 启用 -->
      <el-table-column label="启用" width="120px">
        <template slot-scope="scope">
          <i class="iconfont openIcon icon-Rectangle3" @click="openClose(scope.row.status, scope.row.id)" v-if="scope.row.status === '0'"></i>
          <i class="iconfont openIcon icon-icon-test2" @click="openClose(scope.row.status, scope.row.id)" v-else style="color:#F15A4A;"></i>
        </template>
      </el-table-column>
      <!-- 默认 -->
      <el-table-column label="默认" width="120px">
        <template slot-scope="scope">
          <i class="iconfont defaultIcon icon-Rectangle3" @click="defaulSet(scope.row.signature_default,scope.row.id)" v-if="scope.row.signature_default === '0'"></i>
          <i class="iconfont defaultIcon icon-weiban-icon" @click="defaulSet(scope.row.signature_default,scope.row.id)" v-else style="color:#BBBBC3;"></i>
        </template>
      </el-table-column>
      <!-- 操作 -->
      <el-table-column label="操作">
        <template slot-scope="scope">
          <span class="operation" @click="edit(scope.row.signature_type,scope.row.id)">编辑</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination :current-page='currentPage4'
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :page-sizes='[10, 20, 50, 100]'
      :page-size='pageSizes'
      layout='total, sizes, prev, pager, next, jumper'
      :total='totalRows'>
    </el-pagination>

    <!-- 添加文本签名弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="addTextSignVisible"
      :closeOnClickModal="false"
      width="600px">
      <!-- 添加账号表单内容 -->
      <el-form ref="form" :model="form" label-width="80px">
        <!-- 签名标题 -->
        <el-form-item label="签名标题" prop="title" :rules="[{ required: true, message: '签名标题不能为空'}]">
          <el-input :maxlength='15' v-model.trim="form.title"></el-input>
        </el-form-item>
        <!-- 适用账号 -->
        <el-form-item label="适用账号" prop="account" :rules="[{ required: true, message: '适用账号不能为空'}]">
          <el-select v-model="form.account" placeholder="请选择" @focus="getAccountList">
            <el-option v-for="(item,index) in accountList" :key="index" :label='item.account' :value="item.id">{{item.account}}</el-option>
          </el-select>
        </el-form-item>
        <!-- 富文本 -->
        <el-form-item label="">
          <mailSimpleUeditor v-if="addTextSignVisible" :textContent='textContent'></mailSimpleUeditor>
          <!-- <mailSimpleUeditor :textContent='textContent'></mailSimpleUeditor> -->
        </el-form-item>
        <!-- 默认签名 -->
        <el-form-item label="默认签名">
          <el-radio-group v-model="form.signature_default">
            <el-radio label="0">是</el-radio>
            <el-radio label="1">否</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="onSubmit('form','0')">保 存</el-button>
        <el-button @click="addTextSignVisible = false">取 消</el-button>
      </span>
    </el-dialog>

    <!-- 添加名片签名弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="addCardSignVisible"
      :closeOnClickModal="false"
      width="800px">
      <!-- 添加账号表单内容 -->
      <el-form ref="form" :model="form" label-width="80px">
        <!-- 签名标题 -->
        <el-form-item label="签名标题" prop="title" :rules="[{ required: true, message: '签名标题不能为空'}]">
          <el-input :maxlength='15' v-model.trim="form.title"></el-input>
        </el-form-item>
        <!-- 适用账号 -->
        <el-form-item label="适用账号" prop="account" :rules="[{ required: true, message: '适用账号不能为空'}]">
          <el-select v-model="form.account" placeholder="请选择" @focus="getAccountList">
            <el-option v-for="(item,index) in accountList" :key="index" :label='item.account' :value="item.id">{{item.account}}</el-option>
          </el-select>
        </el-form-item>
        <!-- 默认签名 -->
        <el-form-item label="默认签名">
          <el-radio-group v-model="form.signature_default">
            <el-radio label="0">是</el-radio>
            <el-radio label="1">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- 名片设计 -->
        <div class="card-design">
          <!-- 资料项 -->
           <div class="information">
             <!-- 左边部分 -->
             <div class="left pull-left">
               <!-- 名片签名组件 ======================== -->
               <mailCardSign v-if="addCardSignVisible" :card-data="cardData"></mailCardSign>
             </div>
             <!-- 右边部分 -->
             <div class="right pull-right">
               <ul>
                 <li>
                   <span>姓名 : </span>
                   <input type="text" v-model="cardData.name">
                 </li>
                 <li>
                   <span>职务 : </span>
                   <input type="text" v-model="cardData.job">
                 </li>
                 <li>
                   <span>邮箱 : </span>
                   <input type="text" v-model="cardData.mail">
                 </li>
                 <li>
                   <span>公司 : </span>
                   <input type="text" v-model="cardData.company">
                 </li>
                 <li>
                   <span>地址 : </span>
                   <input type="text" v-model="cardData.address">
                 </li>
                 <li>
                   <span>手机 : </span>
                   <input type="text" v-model="cardData.mobile">
                 </li>
                 <li>
                   <span>电话 : </span>
                   <input type="text" v-model="cardData.tel">
                 </li>
                 <li>
                   <span>资料 : </span>
                   <input type="text" v-model="cardData.material">
                 </li>
                 <!-- <div v-if="addInfoShow">
                   <span @click="addInfo">增加资料项</span>
                 </div> -->
               </ul>
             </div>
           </div>
          <!-- 名片样式 -->
          <div class="card-style">
            <p>名片样式</p>
            <ul>
              <li class="card-style-item" v-for="(index,item) in cardArr" :key="index" @click="activeStyle(item)" :style="'background-position:'+ item * -240 +'px 0px;'">
                <i class="iconfont" v-if="index === cardData.currentCard">&#xe679;</i>
              </li>
            </ul>
          </div>
        </div>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="onSubmit('form','1')">保 存</el-button>
        <el-button @click="addCardSignVisible = false">取 消</el-button>
      </span>
    </el-dialog>

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
import mailCardSign from '@/frontend/Email/mail-set/mail-card-sign.vue' // 名片签名组件
import {HTTPServer} from '@/common/js/ajax.js'
import mailSimpleUeditor from '@/frontend/Email/mail-set/mail-simple-ueditor.vue' // 富文本

export default {
  name: 'mailSignSet', // 邮件-签名设置组件
  components: {mailCardSign, mailSimpleUeditor},
  data () {
    return {
      tableData3: [],
      multipleSelection: [], // 已选账户数据
      dialogTitle: '',
      addCardSignVisible: false, // 添加名片签名弹窗
      addTextSignVisible: false, // 添加文本签名弹窗
      form: {
        'signature_default': '0', // 是否为默认签名 0 否 1 是
        'title': '', // 签名标题
        'content': '', // 签名内容
        'account': '', // 适用账号
        'mail_account_id': '', // 适用账号id
        'signature_type': '', // 0 文本签名 1 名片签名
        'status': '' // 启用与否
      },
      telShow: false, // 隐藏电话选项
      addInfoShow: true, // 显示增加资料项
      editorSimpleContent: '', // 富文本内容
      // 名片数据
      cardData: {
        name: '', // 名字
        job: '', // 职务
        mail: '', // 邮箱
        company: '', // 公司
        address: '', // 地址
        mobile: '', // 手机号码
        tel: '', // 电话号码
        material: '', // 资料
        currentCard: '' // 当前选中的名片(索引值)
      },
      cardArr: [0, 1, 2, 3, 4, 5], // 六种名片样式
      delVisible: false,
      textContent: '', // 编辑时获取的文本数据(传递给富文本组件使用)
      accountList: [], // 个人适用账号列表
      currentId: '',
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
    addSign () {
      this.dialogTitle = '添加名片签名'
      this.addCardSignVisible = true
    },
    addText () {
      this.dialogTitle = '添加文本签名'
      this.addTextSignVisible = true
    },
    // 已选账号
    handleSelectionChange (val) {
      this.multipleSelection = val
      console.log(this.multipleSelection, 'this.multipleSelection')
    },
    // 保存添加账号信息
    onSubmit (formName, signatureType) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          // 遍历accountList, 根据this.form.account, 获取this.form.mail_account_id
          // this.accountList.map((item, index) => {
          //   console.log(item, index)
          //   console.log(this.form.account, 'this.form.account')

          //   if (this.form.account === item.account) {
          //     this.form.mail_account_id = item.id
          //   }
          // })

          var obj = {
            'title': this.form.title, // 签名标题
            'mail_account_id': typeof (this.form.account) === 'number' ? this.form.account : this.form.mail_account_id, // 邮箱账户ID
            'content': signatureType === '0' ? this.editorSimpleContent : this.cardData, // 签名内容
            'signature_type': signatureType, // 0 文本签名 1 名片签名
            'signature_default': this.form.signature_default // 是否为默认签名 0 否 1 是
          }
          console.log(obj, 'this.form文本签名')
          if (this.currentId) {
            // 编辑账户
            obj.id = this.currentId
            HTTPServer.mailSignatureEdit(obj).then((res) => {
              console.log(res, '编辑账户请求结果')
              this.addCardSignVisible = false
              this.addTextSignVisible = false
              this.currentId = ''
              this.getSignList()
              this.$message({
                message: '执行成功',
                type: 'success'
              })
            })
          } else {
            // 添加账户
            HTTPServer.addMailSignature(obj).then((res) => {
              console.log(res, '添加账户请求结果')
              // if (res.response.code === 1001) {
              this.addCardSignVisible = false
              this.addTextSignVisible = false
              this.currentId = ''
              this.getSignList()
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
    // 增加资料项
    addInfo () {
      this.telShow = true // 显示电话选项
      this.addInfoShow = false // 隐藏增加资料项
      // 名片组件相对应的也要增加一个电话选项-------------
    },
    // 选中的样式选项
    activeStyle (item) {
      this.cardData.currentCard = item
      // 发送currentCard给名片组件
      this.$bus.$emit('currentCard', this.cardData.currentCard)
    },
    // 设置一页显示的数据条数
    handleSizeChange (val) {
      this.pageSizes = val
      this.getSignList()
    },
    // 当前第几页
    handleCurrentChange (val) {
      this.currentPage4 = val
      this.getSignList()
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
      HTTPServer.delMailSignature({'id': idArr.join()}).then((res) => {
        console.log(res, '删除签名结果')
        // if (res.response.code === 1001) {
        // 关闭弹窗
        this.delVisible = false
        // 提示操作结果
        this.$message({
          message: '删除成功',
          type: 'success'
        })
        // 刷新列表
        this.getSignList()
        // }
      })
    },
    // 启用禁用
    openClose (status, id) {
      console.log('启用')
      // 获取当前status及id===============
      // 发送启用禁用请求
      HTTPServer.openOrSignature({'status': status === '1' ? '0' : '1', 'id': id}).then((res) => {
        console.log(res, '启用禁用')
        // if (res.response.code === 1001) {
        this.getSignList()
        // 提示操作结果
        this.$message({
          message: '执行成功',
          type: 'success'
        })
        // }
      })
    },
    defaulSet (signatureDefault, id) {
      console.log(signatureDefault, id)
      HTTPServer.setDefaultSignature({signature_default: signatureDefault === '0' ? '1' : '0', id: id}).then((res) => {
        console.log(res, '签名设置-默认账户')
        // if (res.response.code === 1001) {
        this.getSignList()
        // 提示操作结果
        this.$message({
          message: '执行成功',
          type: 'success'
        })
        // }
      })
    },
    // 编辑签名
    edit (signatureType, id) {
      this.currentId = id
      // 根据id查询签名
      HTTPServer.mailSignatureQueryById({'id': id}).then((res) => {
        console.log(res, '根据id查询签名')
        this.form = res
        // 判断是文本签名还是名片签名
        if (signatureType === '0') {
          // 文本
          // 将this.form.content发送给富文本组件显示
          this.textContent = this.form.content
          this.dialogTitle = '编辑文本签名'
          this.addTextSignVisible = true
        } else {
          // 名片
          this.dialogTitle = '编辑名片签名'
          this.addCardSignVisible = true
          // 将this.form.content发送给名片组件显示
          this.cardData = this.form.content
          this.activeStyle(this.form.content.currentCard)
        }
      })
    },
    // 获取签名列表
    getSignList () {
      HTTPServer.mailSignatureQueryList({pageSize: this.pageSizes, pageNum: this.currentPage4}).then((res) => {
        console.log(res, '获取签名列表')
        this.tableData3 = res.dataList
        // 分页
        this.currentPage4 = res.pageInfo.pageNum // 当前页码
        this.pageSizes = res.pageInfo.pageSize // 一页总条数
        this.totalRows = res.pageInfo.totalRows // 总条数
      })
    },
    // 获取适用账号列表
    getAccountList () {
      HTTPServer.queryPersonnelAccount().then((res) => {
        console.log(res, '获取适用账号列表')
        this.accountList = res
      })
    }
  },
  mounted () {
    this.$bus.$on('editorSimple', (value) => {
      // 富文本组件内容
      this.editorSimpleContent = value
    })
  },
  watch: {
    // 关闭名片弹窗时就复位form
    addCardSignVisible: function (newaddAccountVisible, oldaddAccountVisible) {
      if (!newaddAccountVisible) {
        // 清空this.form
        this.form = {
          'signature_default': '0', // 是否为默认签名 0 否 1 是
          'title': '', // 签名标题
          'content': '', // 签名内容
          'account': '', // 适用账号
          'mail_account_id': '',
          'signature_type': '', // 0 文本签名 1 名片签名
          'status': '' // 启用与否
        }
        // 名片数据重置
        this.cardData = {
          name: '', // 名字
          job: '', // 职务
          mail: '', // 邮箱
          company: '', // 公司
          address: '', // 地址
          mobile: '', // 手机号码
          tel: '', // 电话号码
          material: '', // 资料
          currentCard: '' // 当前选中的名片(索引值)
        }
        // 重置表单
        this.$refs['form'].resetFields()
        this.currentId = ''
      }
    },
    // 关闭文本弹窗时就复位form
    addTextSignVisible: function (newaddAccountVisible, oldaddAccountVisible) {
      if (!newaddAccountVisible) {
        // 清空this.form
        this.form = {
          'signature_default': '0', // 是否为默认签名 0 否 1 是
          'title': '', // 签名标题
          'content': '', // 签名内容
          'account': '', // 适用账号
          'mail_account_id': '',
          'signature_type': '', // 0 文本签名 1 名片签名
          'status': '' // 启用与否
        }
        this.textContent = ''
        // 重置表单
        this.$refs['form'].resetFields()
        this.currentId = ''
        // 关闭弹窗时销毁富文本
        // if (window.UE) {
        //   window.UE.delEditor('editor')
        // }
      }
    }
  },
  updated () {
    console.log(this.form, 'form')
  }
}
</script>
<style lang="scss">
  .sign-set {
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
    // 添加账号表单
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
          left:5px;
        }
        // SSL端口
        .el-form-item {
          margin-left: 25px;
          .el-input {
            width: 96%;
            input {
              width: 160px;
              height: 36px;
              float: right;
            }
          }
          .el-form-item__content {
            margin-left: 60px!important;
          }
          // 报错提示信息
          .el-form-item__error {
            top: 67%;
            left:0;
          }
        }
        // 昵称
        &:nth-of-type(3){
          .el-form-item__label {
            margin-left: 11px;
          }
        }
        // 已发箱是否同步
        &:nth-of-type(9){
          .el-form-item__label {
            margin-right: 42px;
          }
        }
        // 发件是否同步
        &:nth-of-type(10){
          .el-form-item__label {
            margin-right: 57px;
          }
        }
        // STARTTLS加密传输
        &:nth-of-type(11){
          .el-form-item__label {
            margin-right: 20px;
          }
        }
        // 默认发送邮箱
        &:nth-of-type(12){
          .el-form-item__label {
            margin-right: 58px;
          }
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
          // width: 400px;
          height: 36px;
          float: right;
        }
      }
      // 接收服务器类型 下拉框
      .el-select {
        width: 100%;
        float: right;
        margin-bottom: 15px;
        .el-input {
          height: 36px;
        }
      }
      // SSL
      .el-checkbox {
        float: left;
      }
    }
    // 启用图标 / 默认图标
    .openIcon,.defaultIcon {
      font-size: 20px;
      color:#4C9E20;
      margin-left: 4px;
      cursor: pointer;
    }
    // 操作
    .operation {
      font-size: 14px;
      color: #409EFF;
      cursor: pointer;
    }
    // 名片设计
    .card-design {
      // 资料项
      .information {
        overflow: hidden;
        // 左边部分
        .left {
          width: 368px;
          height: 220px;
          border: 1px solid #979797;
          border-radius: 6px;
        }
        // 右边部分
        .right {
          width: 368px;
          // height: 310px;
          border: 1px solid #D9D9D9;
          border-radius: 5px;
          >ul {
            padding: 14px;
            >li {
              height: 40px;
              line-height: 40px;
              border-bottom: 1px solid #ddd;
              padding-left: 27px;
              >input {
                width: 86%;
                border: 0;
              }
            }
            >div {
              font-size: 14px;
              color: #008FE5;
              width: auto;
              text-align: center;
              padding: 5px;
              >span {
                cursor: pointer;
              }
            }
          }
        }
      }
      // 名片样式
      .card-style {
        >ul {
          overflow: hidden;
          >li {
            border: 1px solid #ccc;
            width: 220px;
            height: 131px;
            float: left;
            margin-right: 20px;
            margin-top: 20px;
            border-radius: 2.26px;
            cursor: pointer;
            background: url('card_style_small.png');
            background-size:auto 132px;
            >i {
              margin-left: 5px;
              color: #51D0B1;
            }
          }
        }
      }
    }
    // 解决富文本显示出来的多个textarea
    .el-dialog{
      .el-form{
        textarea[name=editorValue] {display: none!important}
      }
    }
  }
</style>
