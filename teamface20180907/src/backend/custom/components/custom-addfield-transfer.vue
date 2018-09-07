<template>
  <div class="ft-dialog-container clear" v-if="ft_dialogVisible">
    <el-dialog
        title="新增字段转换"
        :visible.sync="ft_dialogVisible"
        width="700px">
        <div class="ft-dialog-content">
          <div class="ft-basemsg">
            <span>基础信息</span>
          </div>
          <div class="clear ft-margin-div first">
            <div class="pull-left ft-text"><span>名称</span></div>
            <div class="pull-left ft-input" ><el-input v-model="allData.basics.title" placeholder="请输入内容"></el-input></div>
          </div>
          <div class="clear ft-margin-div">
            <div class="pull-left ft-text"><span>源模块</span></div>
            <div  class="pull-left ft-name"><span>{{moduleName.name}}</span></div>
          </div>
          <div class="clear ft-margin-div">
            <div class="pull-left ft-text"><span>目标模块</span></div>
            <div class="pull-left ft-input" >
              <el-select v-model="allData.basics.target_label" placeholder="请选择" @change="modelChange('module',$event)" >
                <li style="width: 360px; height: 36px;" class="search-input">
                  <div class="flex-box">
                    <el-input size="medium" placeholder="搜索"  v-model="searchValue"><i slot="prefix" class="el-input__icon el-icon-search"></i></el-input>
                  </div>
                </li>
                <el-option
                  v-for="item in moduleList"
                  :key="item.value"
                  :label="item.chinese_name"
                  :value="item.chinese_name"
                  :disabled="item.chinese_name === moduleName.name"
                  v-show="item.chinese_name.includes(searchValue)"
                  style="width: 514px">
                </el-option>
              </el-select>
            </div>
          </div>
          <div class="clear ft-margin-div">
            <div class="pull-left ft-text"><span>描述</span></div>
            <div class="pull-left ft-input">
                <el-input
                  type="textarea"
                  :rows="3"
                  placeholder="请输入内容"
                  v-model="allData.basics.description">
                </el-input>
            </div>
          </div>
          <div class="clear  ft-margin-div ">
            <div class="pull-left ft-input ft-checked">
              <div> <el-checkbox v-model="isDelSource" @change="modelChange('isDel',$event)">是否删除原有记录？</el-checkbox></div>
              <!-- <div> <el-checkbox v-model="isAlertNew" @change="modelChange('isAlert',$event)">点击转换按钮后，弹出目标模块的新增页面？</el-checkbox></div> -->
            </div>

          </div>

          <div class="ft-basemsg ft-relation">
            <span>字段对应关系</span>
          </div>
          <div class="clear ft-relation-box">
            <div class="pull-left ft-select-box" v-for="(field, index) in this.allData.fieldsrelation" :key="field.name">
              <div class="pull-left ft-text over-ellipsis">
                <span :title="field.source_label">{{field.source_label}}</span>
              </div>
              <div class="pull-left ft-select">
                <el-select v-model="field.target_label" placeholder="请选择" @change="fieldModelChange(index, $event)" @focus="
                filterUnTransField(field.type)" clearable >
                  <el-option
                    v-for="item in temtargetFieldList"
                    :key="item.value"
                    :label="item.label"
                    :value="item.label"
                    :disabled="fieldFlagObj[item.name]">
                  </el-option>
                </el-select>
              </div>
            </div>
          </div>
          <div>
          <div v-for=" (sourceSub,index) of allData.subformrelation" :key="sourceSub.name">
            <div class="ft-basemsg ft-relation ft-subform">
              <span>子表单字段对应关系{{index+1}}</span>
            </div>
            <div class="clear ft-select-all" >
              <div class="pull-left ft-text" ><span>选择子表单</span></div>
                <div class="pull-left ft-select">
                  <el-select v-model="sourceSub.subTargetLabel" placeholder="请选择" @change="modelChange('selectSub',$event, sourceSub)" clearable value-key="name">
                    <el-option
                      v-for="item in targetSubformList"
                      :key="item.value"
                      :label="item.label"
                      :value="item"
                      >
                    </el-option>
                  </el-select>
                </div>
            </div>
            <div class="clear ft-relation-box">
              <div class="pull-left ft-select-box" v-for="(subform,idx) in sourceSub.rows" :key="subform.name">
                <div class="pull-left ft-text">
                  <span>{{subform.source_label}}</span>
                </div>
                <div class="pull-left ft-select">
                  <el-select v-model="subform.target_label" placeholder="请选择" @change="subformModelChange(index,idx,$event)" @focus="filterUnTransSubField(sourceSub, subform.type)" clearable>
                    <el-option
                      v-for="item in temTargetSubFieldList"
                      :key="item.value"
                      :label="item.label"
                      :value="item.label"
                      :disabled="sourceSub.selectedObj[item.name]">
                    </el-option>
                  </el-select>
                </div>
              </div>
            </div>
          </div>
        </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="ft_dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleSave()">保 存</el-button>
        </span>
    </el-dialog>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
export default {
  name: 'CustomAddfieldTransfer',
  data () {
    return {
      ft_dialogVisible: false,
      tem_tabledata: [],
      moduleList: [],
      currentBean: {'bean': this.$route.query.bean},
      // applicationId: {'id': this.$route.query.id}
      moduleName: {'name': this.$route.query.moduleName},
      allData: {
        'bean': this.$route.query.bean,
        'basics': {
          'title': '',
          'source_label': this.$route.query.moduleName,
          'source_bean': this.$route.query.bean,
          'target_label': '',
          'target_bean': '',
          'description': '',
          'del_record': '0',
          'after_success': '0'
        },
        'fieldsrelation': [],
        'subformrelation': []
      },
      isDelSource: false, // 是否删除原有记录
      targetBean: {'bean': ''}, //
      targetFieldList: [],
      targetSubformList: [],
      targetSubValue: '', // 当前选中的子表单
      targetsubFieldList: [],
      temTargetSubFieldList: [],
      // currentSubformList: []
      temtargetFieldList: [], // 临时的目标字段列表
      fieldFlagObj: {}, // 已选的的字段
      subFieldFlagObj: {}, // 已选择的子表单字段
      searchValue: ''
    }
  },
  methods: {
    swithDialog () {
      this.ft_dialogVisible = !this.ft_dialogVisible
    },
    // 获取所有模块
    getAllModule () {
      // let url = 'module/findModuleList'
      HTTPServer.getAllModule()
        .then((res) => {
          console.log(res, '获取所有模块')
          this.moduleList = res
          console.log(this.moduleList)
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 获取模块非子表单字段
    getCurrentModule () {
      // let url = 'layout/getFieldsByNotSubform'
      HTTPServer.getFieldsByNotSubform(this.currentBean)
        .then((res) => {
          console.log(res, '获取当前模块非子表单')
          res.map((item, index) => {
            let obj = {
              'source_label': item.label,
              'source_name': item.name,
              'target_label': '',
              'target_name': '',
              'type': item.type
            }
            this.allData.fieldsrelation.push(obj)
          // console.log(this.allData.basics.fieldsrelation, '转换列表')
          })
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 获取目标模块的非子表单所有字段
    getTargetAllField () {
      // let url = 'layout/getFieldsByNotSubform'
      console.log(this.targetBean, '目标bean')
      HTTPServer.getFieldsByNotSubform(this.targetBean)
        .then((res) => {
          console.log(res, '获取目标模块')
          this.targetFieldList = res
          console.log(this.targetFieldList, 'targetFieldList')
          /** modify by 2018-2-3 ****************/
          if (!this.allData.id) {
            this.allData.fieldsrelation.map((field, index) => {
              for (let item of this.targetFieldList) {
                if (field.source_label === item.label) {
                  if (item.type !== 'identifier' && item.type !== 'seniorformula' && item.type !== 'barcode') {
                    field.target_label = item.label
                    field.target_name = item.name
                    this.fieldFlagObj[item.name] = true
                  }
                  break
                } else {
                  field.target_label = ''
                  field.target_name = ''
                }
              }
            })
          }

        /* *end */
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 获取当前模块子表单字段
    getCurrentSubform (params) {
      // let url = 'layout/getFieldsBySubform'
      HTTPServer.getFieldsBySubform(this.currentBean)
        .then((res) => {
          console.log(res, '获取当前模块子表单')
          res.map((item, index) => {
          // this.currentSubformList.push(item)
            let dataObj = {
              'subSourceName': item.name,
              'rows': [],
              'subSourceLabel': item.label,
              'subTargetName': '',
              'subTargetLabel': '',
              'selectedObj': {},
              'targetsubFieldList': []
            }
            item.rows.map((list, idx) => {
              let obj = {
                'source_label': list.label,
                'source_name': list.name,
                'target_label': '',
                'target_name': '',
                'type': list.type
              }
              // this.allData.basics.subformrelation.push(obj)
              dataObj.rows.push(obj)
            })
            this.allData.subformrelation.push(dataObj)
            console.log(this.allData.subformrelation, '子表单字段')
          })
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 获取目标模块的子表单字段
    getTargetSubform () {
      // let url = 'layout/getFieldsBySubform'
      HTTPServer.getFieldsBySubform(this.targetBean)
        .then((res) => {
          console.log(res, '获取目标模块子表单')
          this.targetSubformList = res
          // this.targetSubValue = ''
          console.log(this.targetSubformList, this.allData, '目标模块子表单')
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 点击保存
    handleSave () {
      if (this.allData.basics.title === '') {
        this.$message({
          showClose: true,
          message: '请填写规则名称！',
          type: 'warning'
        })
        return
      }
      if (this.allData.basics.target_bean === '') {
        this.$message({
          showClose: true,
          message: '请选择转换的目标模块！',
          type: 'warning'
        })
        return
      }
      if (this.allData.id) {
        this.editFieldTrans()
      } else {
        this.addFieldTrans()
      }
      this.swithDialog()
    },
    // 新增转换数据
    addFieldTrans () {
      HTTPServer.addFieldTransform(this.allData)
        .then((res) => {
          this.$message({
            showClose: true,
            type: 'success',
            message: '新增成功!'
          })
          this.$bus.emit('freshFieldTrans', this.allData)
        })
        .catch((err) => {
          console.log(err)
          this.$message({
            showClose: true,
            type: 'warning',
            message: err.describe
          })
        })
    },
    // model发生改变
    modelChange (type, data, currentData) {
      console.log(type, data)
      switch (type) {
        case 'isDel':
          if (data) {
            this.allData.basics.del_record = '1'
          } else {
            this.allData.basics.del_record = '0'
          }
          break
        case 'module':
          this.allData.basics.target_label = data
          this.moduleList.map((item, index) => {
            if (item.chinese_name === data) {
              this.targetBean.bean = item.english_name
              this.allData.basics.target_bean = item.english_name
            }
          })
          this.getTargetAllField()
          this.getTargetSubform()
          console.log(this.allData, '总数据')
          break
        case 'selectSub':
          console.log(currentData, '45656465465')
          if (data === '') { // 清空情况操作
            // this.allData.subformrelation.map((item, index) => {
            currentData.rows.map((field, index) => {
              field.target_label = ''
              field.target_name = ''
            })
            // })
            currentData.subTargetName = ''
            currentData.targetsubFieldList = []
            currentData.subTargetLabel = ''
            currentData.selectedObj = {}
            // this.allData.target_subform.value = ''
            // this.allData.target_subform.label = ''
          } else {
            if (currentData.subTargetName === '') { // 第一次选择
              currentData.subTargetName = data.name
              currentData.targetsubFieldList = data.rows
              currentData.subTargetLabel = data.label
            } else {
              this.allData.subformrelation.map((item, index) => {
                item.rows.map((field, index) => {
                  field.target_label = ''
                  field.target_name = ''
                })
              })
              currentData.subTargetName = data.name
              currentData.targetsubFieldList = data.rows
              currentData.subTargetLabel = data.label
            }
          }
          console.log(this.targetsubFieldList, this.allData, '45656465465')
          break
        default:
          break
      }
    },
    // // 点击编辑
    handleEdit (index, data, arr) {
      this.allData = tool.deepClone(arr)
      this.targetBean.bean = this.allData.basics.target_bean
      this.getTargetAllField()
      console.log(this.allData, '当前编辑对象')
      if (this.allData.basics.del_record === '1') {
        this.isDelSource = true
      } else {
        this.isDelSource = false
      }
    //
    },
    // 编辑转换数据
    editFieldTrans () {
      HTTPServer.editFieldTransform(this.allData)
        .then((res) => {
          console.log(res, '123123')
          this.$message({
            showClose: true,
            type: 'success',
            message: '编辑成功!'
          })
          this.$bus.emit('freshFieldTrans')
        })
        .catch((err) => {
          this.$message({
            showClose: true,
            type: 'warning',
            message: err.describe
          })
        })
    },
    // 删除转换数据
    delFieldTrans (index, data) {
      console.log(index, data)
      this.$confirm('此操作将永久删除该数据, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let id = {id: data.id}
        HTTPServer.delFieldTransform(id)
          .then((res) => {
            this.$message({
              showClose: true,
              type: 'success',
              message: '删除成功!'
            })
            this.$bus.emit('freshFieldTrans')
          })
          .catch(() => {
            this.$message({
              showClose: true,
              type: 'error',
              message: '删除失败!'
            })
          })
      }).catch(() => {
        this.$message({
          showClose: true,
          type: 'info',
          message: '已取消删除'
        })
      })
    },
    // 判断哪些字段不能转换
    isDisabled (source, target) {
      if (source === 'textarea') {
        if (target === 'picture' || target === 'personnel' || target === 'attachment') {
          return true
        }
      }
      if (source === 'text') {
        if (target === 'textarea') {
          return true
        }
      }
      if (source !== target) {
        return true
      }
    },
    // 字段发生改变
    fieldModelChange (idx, data) {
      console.log(idx, data)
      this.targetFieldList.map((item, index) => {
        if (data === item.label) {
          if (this.allData.fieldsrelation[idx].target_name !== '') {
            if (!this.fieldFlagObj[item.name]) {
              this.fieldFlagObj[item.name] = true
              this.fieldFlagObj[this.allData.fieldsrelation[idx].target_name] = false
              console.log(this.fieldFlagObj, '已选择的字段')
            }
          } else {
            this.fieldFlagObj[item.name] = true
            console.log(this.fieldFlagObj, '已选择的字段')
          }
          this.allData.fieldsrelation[idx].target_name = item.name
        } else if (data === '') { // 处理清空的状态
          this.fieldFlagObj[this.allData.fieldsrelation[idx].target_name] = false
          this.allData.fieldsrelation[idx].target_name = ''
        }
      })
      console.log(this.allData.fieldsrelation, '转换列表')
    },
    // 子表单字段发生改变
    subformModelChange (i, idx, data) {
      console.log(idx, data)
      this.allData.subformrelation[i].targetsubFieldList.map((item, index) => {
        if (data === item.label) {
          if (this.allData.subformrelation[i].rows[idx].target_name !== '') {
            if (!this.allData.subformrelation[i].selectedObj[item.name]) {
              this.allData.subformrelation[i].selectedObj[item.name] = true
              this.allData.subformrelation[i].selectedObj[this.allData.subformrelation[i].rows[idx].target_name] = false
              console.log(this.subFieldFlagObj, '已选择的子表单字段')
            }
          } else {
            this.allData.subformrelation[i].selectedObj[item.name] = true
            console.log(this.allData.subformrelation[i].selectedObj, '已选择的子表单字段')
          }
          this.allData.subformrelation[i].rows[idx].target_name = item.name
        } else if (data === '') { // 处理清空的状态
          this.allData.subformrelation[i].selectedObj[this.allData.subformrelation[i].rows[idx].target_name] = false
          this.allData.subformrelation[i].rows[idx].target_name = ''
        }
      })
      console.log(this.allData.subformrelation, this.targetSubformList)
    },
    // 过滤掉不能转换的字段
    filterUnTransField (data) {
      console.log(data, '惨啦，别点我.......')
      this.temtargetFieldList = []
      this.targetFieldList.map((item, index) => {
        switch (data) {
          case 'text':
            if (item.type === 'text' || item.type === 'textarea') {
              this.temtargetFieldList.push(item)
            }
            break
          case 'phone': case 'email': case 'number': case 'datetime': case 'location': case 'url': case 'area':
            if (item.type === 'text' || item.type === data) {
              this.temtargetFieldList.push(item)
            }
            break
          case 'textarea': case 'picklist': case 'multi': case 'personnel': case 'reference': case 'department':
            if (item.type === data) {
              this.temtargetFieldList.push(item)
            }
            break
          case 'mutlipicklist':
            if (item.type === data || item.type === 'text') {
              this.temtargetFieldList.push(item)
            }
            break
          case 'identifier':
            if (item.type === 'text') {
              this.temtargetFieldList.push(item)
            }
            break
          case 'seniorformula': case 'formula':
            if (item.type === 'text' || item.type === 'number') {
              this.temtargetFieldList.push(item)
            }
            break
          case 'multitext':
            if (item.type === 'multitext') {
              this.temtargetFieldList.push(item)
            }
            break
          case 'barcode':
            if (item.type === 'text') {
              this.temtargetFieldList.push(item)
            }
            break
          default:
            break
        }
      })
      console.log(this.allData, '触发改变后')
    },
    // 过滤掉不能转换的子表单字段
    filterUnTransSubField (subform, data) {
      console.log(subform, data, this.targetSubformList, '惨啦，别点我.......')
      // this.targetsubFieldList = subform.rows
      this.temTargetSubFieldList = []
      subform.targetsubFieldList.map((item, index) => {
        switch (data) {
          case 'text':
            if (item.type === 'text' || item.type === 'textarea') {
              this.temTargetSubFieldList.push(item)
            }
            break
          case 'phone': case 'email': case 'number': case 'datetime': case 'location': case 'url': case 'area':
            if (item.type === 'text' || item.type === data) {
              this.temTargetSubFieldList.push(item)
            }
            break
          case 'textarea': case 'picklist': case 'multi': case 'mutlipicklist': case 'personnel': case 'reference': case 'department':
            if (item.type === data) {
              this.temTargetSubFieldList.push(item)
            }
            break
          case 'identifier':
            if (item.type === 'text') {
              this.temTargetSubFieldList.push(item)
            }
            break
          case 'seniorformula': case 'formula':
            if (item.type === 'text' || item.type === 'number') {
              this.temTargetSubFieldList.push(item)
            }
            break
          case 'multitext':
            if (item.type === 'multitext') {
              this.temTargetSubFieldList.push(item)
            }
            break
          case 'barcode':
            if (item.type === 'text') {
              this.temTargetSubFieldList.push(item)
            }
            break
          default:
            break
        }
      })
      console.log(this.allData, '触发改变后')
    }
  },
  computed: {
  },
  created () {
  },
  mounted () {
    this.getAllModule()
    this.$bus.off('openTransfer')
    this.$bus.on('openTransfer', (type, value) => {
      this.swithDialog()
      if (type === 'add') { // 新增
        this.allData = {
          'bean': this.$route.query.bean,
          'basics': {
            'title': '',
            'source_label': this.$route.query.moduleName,
            'source_bean': this.$route.query.bean,
            'target_label': '',
            'target_bean': '',
            'description': '',
            'del_record': '0',
            'after_success': '0'
          },
          'fieldsrelation': [],
          'subformrelation': []
          // 'target_subform': {'value': '', 'label': ''}
        }
        this.getCurrentModule()
        this.getCurrentSubform()
        this.targetSubValue = ''
        this.isDelSource = false
      } else if (type === 'edit') { // 编辑
        this.allData = value.arrdata
        console.log(value.arrdata, '编辑数据')
        // this.targetSubValue = this.allData.target_subform.label
        this.targetBean.bean = this.allData.basics.target_bean
        this.getTargetAllField()
        this.getTargetSubform()
        if (this.allData.basics.del_record === '1') {
          this.isDelSource = true
        } else {
          this.isDelSource = false
        }
      }
    })
    console.log(this.$route.query, 'params')
  }
}
</script>
<style lang="scss">
 .ft-dialog-container {
  margin-left:25px;
  .ft-title {
    line-height: 60px;
    span:first-child{
      font-size: 18px;
      color: #17171A;
    }
    span {
      color: #BBBBC3;
    }
  }
  .ft-addbtn {
    line-height: 60px;
    .el-button {
      padding: 5px 13px;
      height: 30px;
      width: 80px;
    }
  }
  .ft-page {
    margin-top:15px;
  }
  .ft-table {
    .el-table {
      th {
        .cell:first-child {
          border-left: 2px solid #E7E7E7;
          margin-bottom: 15px;
          font-size: 15px;
          line-height: 15px;
          font-weight: bold;
          color: #17171A;
        }
      }
    }
  }
  .el-dialog__header {
    background: #409EFF;

    span.el-dialog__title,i.el-dialog__close {
        color:#ffffff !important
    }
    .el-dialog__headerbtn {
      .el-dialog__close {
        font-size: 23px;
        color:#ffffff
      }
    }
  }
  .el-dialog__footer {
      // position: absolute;
      // right: 10px;
      // bottom: 10px;
  .el-button {
      padding: 5px 13px;
      height: 30px;
      width: 80px;
    }
  }
  .ft-basemsg {
    font-size: 16px;
    color: #17171A;
    border-bottom: 1px solid #F5F5F5;
  }
  .el-input__inner {
    height: 35px;
  }
  .ft-dialog-content {
    text-align: left;
    .first{
        margin-top:10px;
    }
  }

  .ft-margin-div {
    height: auto;
    margin-top:15px;
    .ft-input {
      width: 78%;
      .el-select {
        width: 100%;
      }
    }
    .ft-text {
      text-align: right;
      width: 17%;
      padding-right:10px;
      box-sizing:border-box;
      line-height: 35px;
    }
    .ft-checked {
      margin-left:17%;
    }
    .ft-name {
      line-height: 35px;
    }
  }

  .ft-relation-box {
    margin-top:15px;
  }
  .ft-relation {
    margin-top: 15px;
  }
  .ft-select-box {
    width: 50%;
    box-sizing: border-box;
    margin-top: 15px;
    .ft-text {
      text-align: right;
      width: 33%;
      padding-right:10px;
      box-sizing:border-box;
      line-height: 35px;
    }
    .ft-select{
      width: 58%;
      .el-select{
        width: 100%;
      }
    }
  }
  .ft-subform {
    margin-top: 30px;
  }
  .ft-select-all {
    width: 100%;
    margin-top:15px;
    .ft-text {
      text-align: right;
      padding-right:10px;
      box-sizing:border-box;
      line-height: 35px;
    }
    .ft-select {
      width: 79%;
    }
  }
    ::-webkit-scrollbar{
   width: 5px;
   height: 1px;
 }
}
</style>
