<template>
  <div class="safety-main-wrip">
    <div class='header'>
        <i class="iconfont icon-anquanguanli"></i>
          <span>安全管理</span>
    </div>
    <div class="safety_body">
      <header>
        <div class="item" :class="(activeName === 0) ? 'active':''" name="0" @click="handleClick(0)">密码策略</div>
        <div class="item" :class="(activeName === 1) ? 'active':''" name="1" @click="handleClick(1)">会话设置</div>
        <div class="item" :class="(activeName === 2) ? 'active':''" name="2" @click="handleClick(2)">数据备份</div>
        <div class="item" :class="(activeName === 3) ? 'active':''" name="3" @click="handleClick(3)">IP限制</div>
        <div class="item" :class="(activeName === 4) ? 'active':''" name="4" @click="handleClick(4)">日志管理</div>
      </header>
      <div class="password-policy" v-if="activeName == 0">
        <div class="form-item">
          <span class="title">用户密码有效期</span>
          <div class="right">
             <!-- v-model='form.pwd_term' -->
            <el-select placeholder='请选择' v-model="value1">
              <el-option v-for="(item,index) in option1" :key="index" :label='item.label' :value='item.id'></el-option>
            </el-select>
          </div>
        </div>
        <div class="form-item">
          <span class="title">密码最小长度</span>
          <div class="right">
             <!-- v-model='form.pwd_length' -->
            <el-select placeholder='请选择' v-model="value2">
              <el-option v-for="(item,index) in option2" :key="index" :label='item.label' :value='item.id'></el-option>
            </el-select>
          </div>
        </div>
        <div class="form-item">
          <span class="title">密码复杂性要求</span>
          <div class="right">
             <!-- v-model='form.pwd_complex' -->
            <el-select placeholder='请选择' v-model="value3">
              <el-option v-for="(item,index) in option3" :key="index" :label='item.label' :value='item.id'></el-option>
            </el-select>
          </div>
        </div>
        <div class="form-item">
          <span class="title">密码输入错误</span>
          <div class="right">
             <!-- v-model='form.pwd_phone' -->
            <el-select placeholder='请选择' v-model="value4">
              <el-option v-for="(item,index) in option4" :key="index" :label='item.label' :value='item.id'></el-option>
            </el-select>
            <span class="remark">次后启用手机短信验证</span>
          </div>
        </div>
        <div class="form-item">
          <span class="title"></span>
          <div class="right">
             <!-- v-model='form.pwd_lock' -->
            <el-select placeholder='请选择' v-model="value5">
              <el-option v-for="(item,index) in option5" :key="index" :label='item.label' :value='item.id'></el-option>
            </el-select>
            <span class="remark">次后锁定帐户15分钟</span>
          </div>
        </div>
        <div class="form-item">
          <span class="title">全员重置密码</span>
          <div class="right" style="width: calc(100% - 146px);">
             <!-- v-model="value6" -->
            <el-checkbox v-model="value6"></el-checkbox>
            勾选保存后让企业所有成员立即从当前的登录设备中退出登录，重置密码后才能登录。
          </div>
        </div>
        <div class="form-item" style="margin-top: 30px;">
          <span class="title"></span>
          <div class="right" style="width: calc(100% - 146px);">
            <el-button type='primary' @click='savePolicy'>保存设置</el-button>
          </div>
        </div>
      </div>
      <div class="chat-setup" v-if="activeName == 1">
        <div class="form-item">
          <span class="title">长时间未操作后自动登出</span>
          <div class="right">
            <el-select placeholder='请选择' v-model='LogOut'>
              <el-option v-for="(item,index) in option6" :key="index" :label='item.label' :value='item.id'></el-option>
            </el-select>
          </div>
        </div>
        <div class="form-item" style="margin-top: 30px;">
          <span class="title"></span>
          <div class="right" style="width: calc(100% - 200px);">
            <el-button type='primary' @click='savePolicy1'>保存设置</el-button>
          </div>
        </div>
      </div>
      <div class="data-backup" v-if="activeName == 2">
        <div style="font-size: 16px;color: #4A4A4A;margin-left: 30px;">平台自带完善的自动备份数据机制，需备份文件请拨打官网客服400电话。（待定）</div>
      </div>
      <div class="ip-limit" v-if="activeName == 3">
        <div class="rejectLogin">IP设置默认针对全员，白名单账号不受登录限制：</div>
        <div class="white-list">
          <div class="title">白名单</div>
          <div class="right">
            <div style="float:left;"  v-for="(item, index) in whiteData" :key="index">
              <span>
                <div v-if="!item.picture" class="simpName">{{item.name | limitTo(-2)}}</div>
                <img v-if="item.picture" :src="item.picture+'&TOKEN='+token" />
                <i class="el-icon-error" @click="deletewhiteData(item.del_id)"></i>
              </span>
              <p class="empName">{{item.name}}</p>
            </div>
            <i class="addMember el-icon-plus" @click="addWhiteMember"></i>
          </div>
        </div>
        <div class="line"></div>
        <div class="ip-header">
          <div class="title">企业成员仅在以下IP登录：</div>
          <div class="right">
            <a href="javascript:;" @click="addipform = true;addIpArr = [];">添加IP</a>
            <a href="javascript:;" style="display:none;" @click="addipSegmentform = true">添加IP段</a>
            <a href="javascript:;" @click="deleteIpSegmentform=true">删除</a>
          </div>
        </div>
        <el-table :data="tableData"  @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="ip" label="登录IP"> </el-table-column>
          <el-table-column prop="employee_name" label="添加人"> </el-table-column>
          <el-table-column label="登录时间">
            <template slot-scope='scope'>
              <span>{{ scope.row.create_time | formatDate('yyyy-MM-dd  mm:ss') }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <!-- 分页 -->
          <div class="Pagination" v-if="activeName == 3">
              <el-pagination @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum' :page-sizes='[10, 20, 50, 100]' :page-size='pageSize' layout='total, sizes, prev, pager, next, jumper' :total='totalNumber'></el-pagination>
          </div>
      <div class="log-manage" v-if="activeName == 4">
        <!-- <safety-log :logData2="logData"></safety-log> -->
        该功能暂未开发
      </div>
    </div>

  <div style="height: 0;">
    <el-dialog title='添加登录IP' :visible.sync='addipform' class='addIp'>
      <div class="content">为了保证企业信息更加安全,企业所有者可以设置登录IP限制规则。设置后，只能在限制的IP下才可以登录。</div>
      <div class="ip-segment">
        <el-input placeholder="请输入内容" v-model="ipInput">
          <!-- <template slot="append"><div @click="addIpSite(ipInput)" class="addIpSite">继续添加</div></template> -->
        </el-input>
      </div>
      <p v-if="addIpArr.length">如：192.168.22.98</p>
      <!-- <div v-for="(item,index) in addIpArr" :key="index" class="IpShowBox">
        <span class="IpShow">{{item}}</span>
        <i class="el-icon-error" @click="deleteIp(index)"></i>
      </div> -->
      <div slot='footer' class='dialog-footer'>
        <el-button type='primary' @click="addIpConfirm(1)">保 存</el-button>
        <el-button @click='addipform = false'>取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title='添加登录IP段' :visible.sync='addipSegmentform' class='addIp'>
      <div class="content">为了保证企业信息更加安全,企业所有者可以设置登录IP限制规则。设置后，只能在限制的IP下才可以登录。</div>
      <div class="ip-segment">
        <input type="text" placeholder="请输入" v-model="IpParagraph1"/> - <input type="text" placeholder="请输入" v-model="IpParagraph2"/>
      </div>
      <p>如：192.168.22.98--192.168.33.1</p>
      <div slot='footer' class='dialog-footer'>
        <el-button type='primary' @click='addIpConfirm(2)'>保 存</el-button>
        <el-button @click='addipSegmentform = false'>取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title='删除' :visible.sync='deleteIpSegmentform' class='addIp'>
      <div class="content">您确定删除选中的数据吗？</div>
      <div slot='footer' class='dialog-footer'>
        <el-button type='primary' @click='deleteIpParagraphConfirm'>确 定</el-button>
        <el-button @click='deleteIpSegmentform = false'>取 消</el-button>
      </div>
    </el-dialog>
  </div>
</div>
</template>

<script>
// import SafetyLog from '@/backend/safety/safety-log'
import {HTTPServer} from '@/common/js/ajax.js'
import {regular} from '@/common/js/constant.js'
export default {
  name: 'SafetyMain',
  // components: {SafetyLog},
  data () {
    return {
      activeName: 0,
      form: {},
      value1: '',
      value2: '',
      value3: '',
      value4: '',
      value5: '',
      value6: '',
      LogOut: '0',
      whiteData: [],
      ipInput: '',
      IpParagraph1: '',
      option1: [
        {'id': '0', 'label': '无限制'},
        {'id': '1', 'label': '30天'},
        {'id': '2', 'label': '60天'},
        {'id': '3', 'label': '90天'},
        {'id': '4', 'label': '一年'}
      ],
      option2: [
        {'id': '6', 'label': '6'},
        {'id': '8', 'label': '8'},
        {'id': '10', 'label': '10'}
      ],
      option3: [
        {'id': '0', 'label': '无限制'},
        {'id': '1', 'label': '包含字母和数字字符'},
        {'id': '2', 'label': '包含字母、数字和特殊字符'},
        {'id': '3', 'label': '包含数字和大小写字母'},
        {'id': '4', 'label': '包含数字、大小写字母和特殊字符'}
      ],
      option4: [
        {'id': '3', 'label': '3'},
        {'id': '4', 'label': '4'},
        {'id': '5', 'label': '5'},
        {'id': '6', 'label': '6'}
      ],
      option5: [
        {'id': '0', 'label': '无限制'},
        {'id': '7', 'label': '7'},
        {'id': '8', 'label': '8'},
        {'id': '9', 'label': '9'},
        {'id': '10', 'label': '10'}
      ],
      option6: [
        {'id': '0', 'label': '无限制'},
        {'id': '1', 'label': '30分钟'},
        {'id': '2', 'label': '1小时'},
        {'id': '3', 'label': '3小时'},
        {'id': '4', 'label': '1天'},
        {'id': '5', 'label': '7天'}
      ],
      IpParagraph2: '',
      addIpArr: [],
      deleteIpArr: [],
      addIpParagraph: '',
      addipform: false,
      addipSegmentform: false,
      deleteIpSegmentform: false,
      logData: {},
      judge: false,
      pageNum: 1,
      ipRegularJUdge: /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])(\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])){3}$/,
      tableData: [],
      pageSize: 20,
      totalNumber: 0
    }
  },
  /* 页面加载后执行 */
  mounted () {
    this.token = JSON.parse(sessionStorage.requestHeader).TOKEN
    this.getRecentlyCompanyPassword()
    /** 多选集合窗口 */
    // this.$bus.off('selectEmpDepRoleMulti')
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value) {
        if (value.prepareKey === 'white') {
          this.addWhiteList(value.prepareData)
        }
      }
    })
  },
  methods: {
    // 获取最近登录公司密码策略
    getRecentlyCompanyPassword () {
      HTTPServer.getRecentlyCompanyPasswordStrategy({}, 'Loading').then((res) => {
        this.form = res
        if (this.form.pwd_term || this.form.pwd_length || this.form.pwd_complex || this.form.pwd_phone || this.form.pwd_lock || this.form.link_set) {
          this.judge = true
        }
        this.value1 = (this.form.pwd_term || 0).toString() // 有效期
        this.value2 = (this.form.pwd_length || 6).toString() // 密码长度
        this.value3 = (this.form.pwd_complex || 0).toString() // 密码复杂性
        this.value4 = (this.form.pwd_phone || 3).toString() // 密码错误几次手机验证
        this.value5 = (this.form.pwd_lock || 0).toString() // 密码错误几次锁定
        if (this.form.link_set) {
          this.LogOut = this.option6[this.form.link_set].label // 会话设置
        }
      })
    },
    // 通过label取id方法
    getValueId (arr, value) {
      for (var i = 0; i < arr.length; i++) {
        if (value === arr[i].label) {
          value = arr[i].id
          return value
        } else if (value === arr[i].id) {
          value = arr[i].id
          return value
        }
      }
    },
    /* 切换标题， 为3的时候调用接口请求白名单成员 */
    handleClick (type) {
      this.activeName = type
      if (type === 3) {
        var jsondata = {'pageNum': this.pageNum, 'pageSize': this.pageSize}
        HTTPServer.whiteListMember(jsondata, 'Loading').then((res) => {
          this.tableData = res.dataList
          var data = res.pageInfo
          this.totalNumber = data.totalRows
          this.pageSize = data.pageSize
          this.pageNum = data.pageNum
          this.whiteList()
        })
      }
    },
    /** 保存策略 */
    savePolicy () {
      console.log(this.value1, this.value2, this.value3, this.value4, this.value5)
      if (this.value1) {
        this.value1 = this.getValueId(this.option1, this.value1)
        this.value3 = this.getValueId(this.option3, this.value3)
      }
      console.log(this.value1, this.value2, this.value3, this.value4, this.value5)
      var jsondata = {
        'pwd_term': this.value1,
        'pwd_length': this.value2,
        'pwd_complex': this.value3,
        'pwd_phone': this.value4,
        'pwd_lock': this.value5,
        'pwd_reset': this.value6 ? '1' : '0'
      }
      console.log(jsondata)
      HTTPServer.retentionPolicy(jsondata, 'Loading').then((res) => {
        this.$message({
          message: '设置成功',
          type: 'success'
        })
      })
    },
    // 保存自动登出设置
    savePolicy1 () {
      var jsondata = {'link_set': this.LogOut}
      HTTPServer.autoLogout(jsondata, 'Loading').then((res) => {
        this.$message({
          message: '设置成功',
          type: 'success'
        })
      })
    },
    /** 添加白名单成员 */
    addWhiteMember () {
      console.log(this.whiteData, 1111)
      this.$bus.emit('commonMember', {'prepareData': this.whiteData, 'prepareKey': 'white', 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': []}) // 给父组
    },
    // 删除成员
    deletewhiteData (id) {
      HTTPServer.deleteMember({'id': id}, 'Loading').then((res) => {
        this.$message({
          message: '删除成功',
          type: 'success'
        })
        this.whiteList()
      })
    },
    // 添加ip
    addIpSite (data) {
      if (data !== '') {
        if (regular.ipRegularJUdge.test(data)) {
          if (this.addIpArr.length === 0) {
            this.addIpArr.push(data)
          } else {
            var judge = true
            for (var i = 0; i < this.addIpArr.length; i++) {
              if (data === this.addIpArr[i]) {
                judge = false
                this.$message.error('ip地址重复')
              }
            }
            if (judge) {
              HTTPServer.validationIpInterface({'ip': data}, 'Loading').then((res) => {
                this.addIpArr.push(data)
              })
            }
          }
        } else {
          this.$message.error('ip输入格式错误')
        }
      } else {
        this.$message.error('设置ip不能为空')
      }
      this.ipInput = ''
    },
    // 删除ip
    deleteIp (i) {
      this.addIpArr.splice(i, 1)
    },
    /** 显示每页条数 */
    handleSizeChange (val) {
      this.pageSize = val
    },
    /** 跳转第几页 */
    handleCurrentChange (val) {
      this.pageNum = val
    },
    // 添加ip的确定按钮
    addIpConfirm (judge) {
      if (!this.ipInput) {
        this.$message.error('设置ip不能为空')
        return
      }
      if (!regular.ipRegularJUdge.test(this.ipInput)) {
        this.$message.error('ip输入格式错误')
        return
      }
      HTTPServer.addIpConnector({'ip': [this.ipInput]}, 'Loading').then((res) => {
        this.$message({
          message: '添加IP成功',
          type: 'success'
        })
        this.IpParagraph1 = ''
        this.IpParagraph2 = ''
        this.addipSegmentform = false
        this.addipform = false
        this.handleClick(3)
      })
    },
    // 删除ip的确定按钮
    deleteIpParagraphConfirm () {
      if (this.deleteIpArr.length > 0) {
        var arr = []
        for (var i = 0; i < this.deleteIpArr.length; i++) {
          arr.push(this.deleteIpArr[i].id)
        }
        HTTPServer.deleteIpConnector({'id': String(arr)}, 'Loading').then((res) => {
          this.$message({
            message: '删除IP成功',
            type: 'success'
          })
          this.deleteIpSegmentform = false
          this.handleClick(3)
        })
      } else {
        this.$message.error('请选择一条数据')
      }
    },
    /* 白名单选择 */
    handleSelectionChange (arr) {
      console.log(arr)
      this.deleteIpArr = arr
    },
    // 白名单添加人员
    addWhiteList (data) {
      var arr = []
      for (var i = 0; i < data.length; i++) {
        arr.push(data[i].id)
      }
      console.log(data)
      HTTPServer.addWhiteListMember({'employee_id': String(arr)}, 'Loading').then((res) => {
        this.$message({
          message: '设置白名单成员成功',
          type: 'success'
        })
        this.whiteList()
      })
    },
    // 获取白名单成员
    whiteList () {
      HTTPServer.getWhiteListMember({}, 'Loading').then((res) => {
        this.whiteData = res
        for (var i = 0; i < this.whiteData.length; i++) {
          this.whiteData[i].name = this.whiteData[i].employee_name
          this.whiteData[i].value = 1 + '-' + this.whiteData[i].id
        }
      })
    }
  }
}
</script>

<style lang="scss">
.safety-main-wrip{
  height: 100%;
  padding: 0 25px 0 30px;
  overflow: auto;
  .header{
    border-bottom: 1px solid #E7E7E7;
    overflow: hidden;
    height: 60px;
    line-height: 60px;
    i{
      float: left;
      margin: 20px 10px 0 0;
      font-size: 20px;
      line-height: 1;
      }
      span{
        font-size: 18px;
        color: #69696C;
      }
    }
  .safety_body{
    >header{padding-left: 10px;border-bottom: 1px solid #f2f2f2;margin-bottom: 30px;
      .item{display: inline-block;height: 53px;line-height: 58px;border-bottom: 3px solid #fff;cursor: pointer;padding: 0 18px;font-size: 16px;color: #69696C;}
      .item.active{border-bottom: 3px solid #66b1ff;color:#66b1ff;}
    }
    .form-item{margin-top: 20px;
      .title{display: inline-block;float: left;padding-right: 20px;width: 146px;text-align: right;font-size: 14px;color: #69696C;line-height: 40px;}
      .right{margin-left: 146px;line-height: 40px;
        .el-select{width: 400px;}
        >input{width: 400px;}
        .remark{margin-left: 20px;}
      }
    }
    .chat-setup{
      .form-item{margin-top: 20px;
        .title{width: 200px;}
        .right{margin-left: 200px;
        }
      }
    }
    .ip-limit{
      height: 730px;
      .rejectLogin{font-size: 16px;color: #17171A;margin-left: 30px;}
      .white-list{margin-top: 22px;
        .title{display: inline-block;float: left;padding-right: 20px;width: 90px;text-align: right;line-height: 36px;}
        .right{margin-left: 0px;display: inline-block;width: calc(100% - 100px);
          .addMember{text-align: center;line-height:38px;display: inline-block;width: 40px;border:1px solid #c5c5c5;border-radius: 50%;color:#b9b9b9;font-size:20px;margin-left:20px;cursor: pointer;}
          .empName{float: right;line-height: 50px;margin:0 20px 0 5px;}
          .simpName{width: 50px;height: 50px;line-height: 50px;display: inline-block;background: #51D0B1;color: #fff;font-size: 12px;border-radius: 50%;text-align: center;}
          span{float: left;line-height: 1;width: 50px;height: 50px;position: relative;
              img{width: 50px;height: 50px;border-radius: 50%;}
              i{position: absolute;top:0;right:0; color:red;cursor: pointer;}
          }
        }
      }
      .line{background: #F2F2F2;margin: 20px 0 30px;height: 1px;}
      .ip-header{margin-bottom: 20px;
        .title{font-size: 16px;color: #4A4A4A;margin-left: 25px;display: inline-block;line-height: 36px;}
        .right{float: right;
          a{border: 1px solid #66b1ff;border-radius: 3px;width: 100px;height: 36px;line-height: 36px;text-align: center;display: inline-block;margin-left: 15px;color: #66b1ff;}
          a:hover{background: #66b1ff;color: #fff;}
        }
      }
    }

  }
      .IpShowBox{width: 200px;padding: 5px 0;}
      .addIp{
      .el-dialog{width: 540px;}
      .el-dialog__header{background-color: #66b1ff; span{color:#fff;} i{color:#fff;}}
      .el-dialog__body{padding: 20px 30px;}
      .content{margin-bottom: 30px;font-size: 16px;color: #4A4A4A;}
      p{font-size: 14px;color: #BBBBC3;padding-left: 10px;margin-top: 10px;}
      i.el-icon-error{color:red;margin-left:20px;float: right;line-height: 24px;cursor: pointer;}
      .IpShow{color:#000;}
      .el-input-group__append{width: 120px;padding: 0;text-align: center;cursor: pointer;background: #51D0B1;color: #fff;font-size: 14px;letter-spacing: 1.56px;border: none;}
      .ip-segment{line-height: 34px;
        >input{height: 34px;background: #FBFBFB;border: 1px solid #E7E7E7;border-radius: 3px;padding: 0 10px;width: 230px;}
        >input:focus{border: 1px solid #66b1ff;}
        div.addIpSite{width: 100%;height: 37px;line-height: 40px;cursor: pointer;}
      }
    }
    .Pagination{
      float: right;
      margin-top: 15px;
      margin-right: 20px;
    }
}
</style>
