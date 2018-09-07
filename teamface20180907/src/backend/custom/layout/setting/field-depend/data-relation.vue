<template>
  <div class="dr-container" style="height: 100%">
    <div class="clear" >
      <div class="fd-text">
        <span>对已有的表单数据进行预计算（预处理），以备进一步调用
        </span>
      </div>
      <div class="fd-addbtn">
        <el-button type="primary" icon="el-icon-plus" @click="dRVisible = true">新增</el-button>
      </div>
    </div>
    <div class="rm-table">
      <el-table
        :data="dr_tabledata"
        style="width: 100%">
        <el-table-column
          label="联动表单"
          width="182">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ JSON.parse(scope.row.form).label }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="联动条件"
          width="234">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ `${JSON.parse(scope.row.relation).value1.label}=${JSON.parse(scope.row.relation).value2.label}` }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="联动字段"
          width="388">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ showfieldList(JSON.parse(scope.row.fieldlist))}}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="创建人"
          width="142">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.personnel_create_by}}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="创建时间"
          width="208">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.datetime_create_time | formatDate('yyyy-MM-dd HH:mm')}}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button type="text" @click="openEdit(scope.row.id)">编辑</el-button>
            <el-button type="text" @click="openDel(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
        <template>
          <div v-show="dr_tabledata.length === 0" slot="empty">
            <div style="width: 200px; height: 200px;">
              <img src="/static/img/no-data.png" style="width: 100%; height: 100%;">
              <p>暂无数据~</p>
            </div>
          </div>
        </template>
      </el-table>
    </div>
    <div class="clear">
      <div class="block fd-page">
        <el-pagination
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalRows"
          @size-change ="handleSizeChange($event)"
          @current-change="handlePageChange($event)">
        </el-pagination>
      </div>
    </div>
    <!-- 新增弹窗 -->
    <div class="dr-create-dialog">
      <el-dialog
        title="数据联动"
        :visible.sync="dRVisible"
        v-if="dRVisible"
        width="660px">
        <div class="dr-content">
          <!-- 联动表单 -->
          <div class="first-item">
            <span class="title-item pull-left"><i> * </i>联动表单</span>
            <div class="option-item pull-left">
              <el-cascader
                :options="options"
                v-model="selectedOptions"
                @change="getLinkModule()"
                :show-all-levels="false"
              ></el-cascader>
            </div>
          </div>
          <!-- 联动条件 -->
          <div class="first-item second-item">
            <span class="title-item pull-left"><i> * </i>联动条件</span>
            <div class="option-item pull-left">
              <span class="pull-left" style="margin: 10px 10px 0px 0px;">本模块</span>
              <div class="relation1 pull-left">
                <el-select v-model="relationValue.value1" @change="relationValue.value2 = {}" @visible-change="fieldListAllFilt" placeholder="请选择" value-key="name">
                <!-- <el-select v-model="relationValue.value1" @visible-change="fieldListAllFilt" placeholder="请选择" value-key="name"> -->
                  <el-option
                    v-for="item in fieldListAllOptions"
                    :key="item.name"
                    :label="item.label"
                    :value="item">
                  </el-option>
                </el-select>
              </div>
              <span class="tag1 pull-left">的值等于</span>
              <div class="relation2 pull-left">
                <el-select v-model="relationValue.value2" placeholder="请选择" :disabled="!relationValue.value1" value-key="name">
                  <el-option
                    v-for="item in fieldListAll2Type(relationValue.value1,1)"
                    :key="item.name"
                    :label="item.label"
                    :value="item">
                  </el-option>
                </el-select>
              </div>
              <span class="tag2 pull-left">的值时</span>
            </div>
          </div>
          <!-- 联动字段 -->
          <div class="first-item third-item">
            <span class="title-item pull-left"><i> * </i>联动字段</span>
            <div class="option-item pull-left">
              <div class="option-item-box" v-for="(item,index) in fieldlist" @mouseover="showDel(index)"  @mouseout="hiddenDel()" :key="index">
                <i class="iconfont icon-Rectangle8" @click="fieldlist.splice(index,1)" v-if="index === currentIndex && index !== 0"></i>
                <span v-else class="tag0 pull-left" :style="{padding: index > 8 ? '10px 6px':'10px'}">{{index + 1}}</span>
                <span class="pull-left" style="margin: 10px 10px 0 0;"> 本模块 </span>
                <div class="relation1 pull-left">
                  <el-select v-model="item.value1" value-key="name" placeholder="请选择" @visible-change="fieldListAllFilt" @change="item.value2 = {}">
                  <!-- <el-select v-model="item.value1" value-key="name" placeholder="请选择" @visible-change="fieldListAllFilt"> -->
                    <el-option
                      v-for="item2 in fieldListAllOptions"
                      :key="item2.name"
                      :label="item2.label"
                      :value="item2">
                    </el-option>
                  </el-select>
                </div>
                <span class="tag1 pull-left">联动显示</span>
                <div class="relation2 pull-left">
                  <el-select v-model="item.value2" value-key="name" placeholder="请选择" :disabled="!item.value1">
                    <el-option
                      v-for="item2 in fieldListAll2Type(item.value1,2)"
                      :key="item2.name"
                      :label="item2.label"
                      :value="item2">
                    </el-option>
                  </el-select>
                </div>
                <span class="tag2 pull-left">的值</span>
              </div>
              <span class="add-btn" @click="fieldlist.push({value1: {}, value2: {}})">
                <i class="iconfont icon-nav-quickly-add"></i>
                添加
              </span>
            </div>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="dRVisible = false">取 消</el-button>
          <el-button type="primary" @click="saveBtn()">确 定</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>
<script>
import tool from '@/common/js/tool.js'
export default {
  name: 'dataRelation', // 数据联动组件
  data () {
    return {
      fieldListAllOptions: [],
      dataLinkageId: '', // 仅编辑使用
      linkModuleObj: {}, // 联动表单模块对象
      currentBean: this.$route.query.bean,
      currentId: '',
      selectedOptions: [], // 联动表单选中值
      currentIndex: '', // 关联字段hover索引值
      dr_tabledata: [], // 数据联动列表
      currentPage: 1,
      pageSize: 10,
      totalRows: 0,
      dRVisible: false, // 新增弹窗
      // 联动表单级联列表
      options: [],
      // 本地模块的字段列表
      fieldListAll: [],
      // 联动模块的字段列表
      fieldListAll2: [],
      // 联动条件选中值
      relationValue: {value1: {}, value2: {}},
      // 联动字段选中值
      fieldlist: [{value1: {}, value2: {}}],
      countTargetList: [], // 聚合表-统计指标
      rowList: [] // 聚合表-行表头
    }
  },
  created () {
    // 获取联动数据列表
    this.getDataList()
    // 获取联动级联列表
    this.getOptions()
    // 根据bean获取本地模块的联动字段
    this.getFieldByBean(this.currentBean, 0)
  },
  methods: {
    // 打开删除弹窗
    openDel (id) {
      this.currentId = id
      // 打开删除弹窗
      this.$confirm('此操作将永久删除该规则, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.aggregationLinkageDel({'id': this.currentId})
          .then((res) => {
            this.$message({message: '删除成功', type: 'success'})
            // 重载列表
            this.getDataList()
            this.currentId = ''
          })
      })
    },
    // 打开编辑
    openEdit (id) {
      this.currentId = id
      this.getFieldByBean(this.currentBean, 0)
      // 根据id获取详情
      this.$http.getLinkageDetail({'id': this.currentId}).then(res => {
        this.linkModuleObj = res.form // 联动表单模块对象
        // 联动表单回显 需进一步转换为级联格式
        this.selectedOptions = [this.linkModuleObj.fatherId, this.linkModuleObj.value]
        // 这里需要区分是模块还是聚合表
        if (this.linkModuleObj.fatherId === 0) {
          // 聚合表
          this.getFieldByValue(this.linkModuleObj.value).then(() => {
            this.dataLinkageId = res.dataLinkageId // 该参数仅编辑时使用
            this.currentBean = res.bean
            this.relationValue = JSON.parse(JSON.stringify(res.relation)) // 联动条件
            this.fieldlist = JSON.parse(JSON.stringify(res.fieldlist)) // 联动字段
            this.fieldListAllOptions = JSON.parse(JSON.stringify(this.fieldListAll))
            this.dRVisible = true
          })
        } else {
          // 模块
          this.getFieldByBean(this.linkModuleObj.english_name, 1).then(() => {
            // 根据bean获取联动模块的联动字段
            this.dataLinkageId = res.dataLinkageId // 该参数仅编辑时使用
            this.currentBean = res.bean
            this.relationValue = JSON.parse(JSON.stringify(res.relation)) // 联动条件
            this.fieldlist = JSON.parse(JSON.stringify(res.fieldlist)) // 联动字段
            this.fieldListAllOptions = JSON.parse(JSON.stringify(this.fieldListAll))
            this.dRVisible = true
          })
        }
      })
    },
    // 根据bean获取模块的联动字段 (bean:模块bena,flag:0为当前模块,1为需联动模块)
    getFieldByBean (bean, flag) {
      let obj = {
        'bean': bean,
        'flag': flag // 传0:本模块会过滤掉二维码
      }
      let promise = this.$http.getFieldsForLinkage(obj).then(res => {
        console.log(res, '根据bean获取模块的联动字段')
        if (flag) {
          // 结果赋值给联动模块的字段列表
          this.fieldListAll2 = res
        } else {
          // 结果赋值给本地模块的字段列表
          this.fieldListAll = res
        }
      })
      return promise
    },
    // 根据this.linkModuleObj.value获取layout
    getFieldByValue (value) {
      let promise = this.$http.getJuheById({'id': value}).then(res => {
        let arr = []
        res.countTargetList.map(item => {
          let obj = {}
          obj.label = item.name
          obj.name = item.en
          // 去掉obj.name的前后#
          obj.name = obj.name.substr(1)
          obj.name = obj.name.substr(0, obj.name.length - 1)
          obj.type = 'number'
          arr.push(obj)
        })
        this.countTargetList = arr
        this.rowList = res.rowList
      })
      return promise
    },
    // 获取联动数据列表
    getDataList () {
      let obj = {
        'bean': this.currentBean,
        'pageNum': this.currentPage,
        'pageSize': this.pageSize
      }
      this.$http.getLinkageList(obj).then(res => {
        this.dr_tabledata = res.dataList
        this.pageSize = res.pageInfo.pageSize
        this.currentPage = res.pageInfo.pageNum
        this.totalRows = res.pageInfo.totalRows
      })
    },
    // 获取联动级联列表
    getOptions () {
      this.$http.getAllApplications({'tag': '1'}).then(res => {
        this.options = res
      })
    },
    // 获取联动表单模块
    getLinkModule () {
      // 获取联动表单选择的模块
      let fatherId
      this.options.map(item => {
        if (this.selectedOptions[0] === item.value) {
          fatherId = item.value
          item.children.map(item2 => {
            if (this.selectedOptions[1] === item2.value) {
              this.linkModuleObj = JSON.parse(JSON.stringify(item2))
              this.linkModuleObj.fatherId = fatherId
            }
          })
        }
      })
      // 判断选中的是模块还是聚合表
      if (this.linkModuleObj.fatherId === 0) {
        // 聚合表
        // 根据this.linkModuleObj.value获取layout
        this.getFieldByValue(this.linkModuleObj.value)
      } else {
        // 模块
        // 根据bean获取联动模块的联动字段
        this.getFieldByBean(this.linkModuleObj.english_name, 1)
      }
      // 联动条件及联动字段数据清空
      this.relationValue = {value1: {}, value2: {}}
      this.fieldlist = [{value1: {}, value2: {}}]
    },
    // 点击确定按钮
    saveBtn () {
      // 非空验证
      if (this.selectedOptions.length < 1) {
        this.$message.error('联动表单不能为空')
        return
      }
      // if (this.relationValue.value1 === null || this.relationValue.value2 === null) {
      if (JSON.stringify(this.relationValue.value1) === '{}' || JSON.stringify(this.relationValue.value2) === '{}') {
        this.$message.error('联动条件不能为空')
        return
      }
      // 遍历fieldList,进行非空验证
      let flag = false
      this.fieldlist.some(item => {
        if (JSON.stringify(item.value1) === '{}' || JSON.stringify(item.value2) === '{}') {
          flag = true
          return true
        }
      })
      if (flag) {
        this.$message.error('联动字段不能为空')
        return
      }
      // 调用数据联动新增接口
      let obj = {
        'bean': this.currentBean, // 当前模块bean
        'form': this.linkModuleObj, // 联动表单
        'relation': this.relationValue, // 联动条件
        'fieldlist': this.fieldlist // 联动字段
      }
      console.log(this.fieldlist, 'fieldlist')

      if (this.currentId !== '') {
        // 编辑
        obj.id = this.currentId
        obj.dataLinkageId = this.dataLinkageId
        this.$http.aggregationLinkageEdit(obj).then(res => {
          console.log(res, '数据联动编辑结果')
          this.dRVisible = false
          this.$message({
            message: '编辑成功',
            type: 'success'
          })
          this.currentId = ''
          // 重载列表
          this.getDataList()
        })
      } else {
        // 新增
        this.$http.aggregationLinkageSave(obj).then(res => {
          console.log(res, '数据联动新增结果')
          this.dRVisible = false
          this.$message({
            message: '新增成功',
            type: 'success'
          })
          this.currentId = ''
          // 重载列表
          this.getDataList()
        })
      }
    },
    // 列表展示关联字段名称用
    showfieldList (data) {
      let arr = []
      data.map(item => {
        arr.push(`${item.value1.label}=${item.value2.label}`)
      })
      return arr.join(';')
    },
    // fieldListAll2已选值去重
    fieldListAll2Filt (data) {
      // arr 代表已选中的数组
      let arr = this.fieldlist[0].value2 ? JSON.parse(JSON.stringify(this.fieldlist)) : []
      if (this.relationValue.value2) {
        arr.push(JSON.parse(JSON.stringify(this.relationValue)))
      }
      let arr2 = []
      // 如果已选值为空,就返回整个data
      if (arr.length < 1) {
        arr2 = JSON.parse(JSON.stringify(data))
      } else {
        // data过滤arr中已选值
        let a = []
        arr.map(item => {
          if (item.value2) {
            a.push(item.value2)
          }
        })
        arr2 = tool.arrayRemainder(data, a)
      }
      // arr去重
      arr2 = tool.uniqueObject(arr2)
      return arr2
    },
    // 同类型选项限制
    fieldListAll2Type (val, tag) {
      if (val) {
        let arr = []
        let arr2 = []
        // 判断是否为聚合表数据
        if (this.linkModuleObj.fatherId === 0) {
          // 聚合表
          // 在判断是显示行表头还是统计指标
          if (tag === 1) {
            // 显示行表头
            arr2 = JSON.parse(JSON.stringify(this.rowList))
          } else {
            // 显示统计指标
            arr2 = JSON.parse(JSON.stringify(this.countTargetList))
          }
        } else {
          // 模块
          arr2 = JSON.parse(JSON.stringify(this.fieldListAll2))
        }
        arr2.map(item => {
          if ((item.type === val.type) && (val.type !== 'number' || val.type !== 'formula' || val.type !== 'seniorformula' || val.type !== 'functionformula' || val.type !== 'reference' || val.type !== 'text' || val.type !== 'barcode')) {
            arr.push(item)
          }
          // 如果是数字/简单公式/高级公式/函数公式 则可以看为类型相等
          if ((val.type === 'number' || val.type === 'formula' || val.type === 'seniorformula' || val.type === 'functionformula') && (item.type === 'number' || item.type === 'formula' || item.type === 'seniorformula' || item.type === 'functionformula')) {
            arr.push(item)
          }
          // 如果是单行文本/关联关系/二维码 则可以看为类型相等
          if ((val.type === 'reference' || val.type === 'text' || val.type === 'barcode') && (item.type === 'reference' || item.type === 'text' || item.type === 'barcode')) {
            arr.push(item)
          }
        })
        // arr去重
        arr = Array.from(new Set(arr))
        // arr = this.fieldListAll2Filt(JSON.parse(JSON.stringify(arr))) 加去重后删除会出问题
        return arr
      }
    },
    // 鼠标移入序号显示删除图标
    showDel (index) {
      this.currentIndex = index
    },
    // 隐藏删除图标
    hiddenDel () {
      this.currentIndex = ''
    },
    // 切换每页数量
    handleSizeChange (data) {
      console.log(data)
      this.pageSize = data
      this.getDataList()
    },
    // 切换到哪一页
    handlePageChange (data) {
      console.log(data)
      this.currentPage = data
      this.getDataList()
    },
    fieldListAllFilt (e) {
      if (e) {
        // arr 代表已选中的数组
        let arr = this.fieldlist[0].value1 ? JSON.parse(JSON.stringify(this.fieldlist)) : []
        if (this.relationValue.value2) {
          arr.push(JSON.parse(JSON.stringify(this.relationValue)))
        }
        let arr2 = []
        // 如果已选值为空,就返回整个fieldListAll
        if (arr.length < 1) {
          arr2 = JSON.parse(JSON.stringify(this.fieldListAll))
        } else {
          // fieldListAll过滤arr中已选值
          let a = []
          arr.map(item => {
            if (item.value1) {
              a.push(item.value1)
            }
          })
          arr2 = tool.arrayRemainder(this.fieldListAll, a)
        }
        // arr去重
        arr2 = tool.uniqueObject(arr2)
        // return arr2
        this.fieldListAllOptions = arr2
      }
    }
  },
  watch: {
    // 监听新增窗口
    dRVisible () {
      if (!this.dRVisible) {
        // 如果窗口关闭则清空所有数据
        this.selectedOptions = []
        this.relationValue = {value1: {}, value2: {}}
        this.fieldlist = [{value1: {}, value2: {}}]
        this.currentId = ''
        this.linkModuleObj = {}
      } else {
        // 开窗时
        // 根据bean获取本地模块的联动字段
        this.getFieldByBean(this.currentBean, 0)
        if (this.selectedOptions.length < 1) {
          this.fieldListAll2 = []
        }
      }
    }
  },
  updated () {
    // console.log(this.fieldlist, 'fieldlist')
  }
}
</script>
<style lang="scss">
.dr-container {
  .dr-create-dialog {
    .el-dialog {
      height: auto;
      .el-dialog__header {
        padding: 13px 20px 13px;
        background-color: #fff;
        border-bottom: 1px solid #e5e5e5;
        .el-dialog__title {
          font-size: 16px;
          color: #323232;
        }
        .el-dialog__headerbtn {
          top: 14px;
          >.el-dialog__close {
            color: #323232;
          }
        }
      }
      .el-dialog__body {
        padding: 20px;
      }
      .el-dialog__footer {
        position: relative;
        padding: 10px 20px 10px;
        right: 0px;
        bottom: 0px;
        border-top: 1px solid #e5e5e5;
      }
    }
    .dr-content {
      // 联动表单
      .first-item {
        overflow: hidden;
        margin-bottom: 16px;
        .title-item {
          font-size: 14px;
          color: #2A2A2A;
          margin-top: 10px;
          >i {
            color: red;
          }
        }
        .option-item {
          margin-left: 33px;
          width: 520px;
          .el-cascader {
            width: 448px;
          }
          .relation1,.relation2 {
            .el-select {
              width: 160px;
            }
          }
          .tag1,.tag2 {
            margin: 10px;
            font-size: 14px;
            color: #666666;
          }
          .tag0 {
            padding: 10px;
            font-size: 14px;
            color: #000;
            cursor: pointer;
          }
          .option-item-box {
            overflow: hidden;
            margin-bottom: 5px;
            padding: 4px 0px;
            &:hover{
              background-color: #F2F2F2;
            }
            >i {
              float: left;
              color: #FF3B30;
              padding: 10px 6px;
              cursor: pointer;
            }
          }
          .add-btn {
            cursor: pointer;
            font-size: 16px;
            color: #1890FF;
            margin-left: 9px;
            >i {
              font-size: 16px;
            }
          }
        }
      }
      .third-item {
        .option-item {
          margin-left: 5px;
          width: 548px;
        }
      }
    }
  }
  ::-webkit-scrollbar{
    width: 5px;
    height: 5px;
    background: #ddd
  //display: none
 }
}
</style>
