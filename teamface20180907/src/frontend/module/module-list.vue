<template>
  <el-container class="module-list-wrip">
    <div class="titles" :style="{'margin-left':-scrollDistance+'px'}">
      <el-checkbox :indeterminate="isIndeterminate" v-model="checkAll" @change="handleCheckAllChange"></el-checkbox>
      <div class="item" v-for="(title,index) in dataTitleListShow" :key="index" :style="{'width':100/dataTitleListShow.length+'%'}">
        <span>{{title.label}}</span>
        <i class="el-icon-d-caret" @click="sortList(title.field)"></i>
      </div>
      <i class="iconfont icon-zidingyiziduan" @click="openSetListShow" v-if="showSort === '1'"></i>
    </div>
    <el-checkbox-group v-model="checkedData" @change="handleCheckedCitiesChange" @scroll.native="getScroll">
      <div class="list" v-for="(data,index) in dataList"  :key="index" :class="{'border-list':data.color !== ''}">
        <el-checkbox :label="data.id.value" :disabled="data.lockedState === true" :style="{'border-color':data.color}"></el-checkbox>
        <div class="rows" v-for="(item,index) in data.row" :key="index" :style="{'width':100/data.row.length+'%', 'border-color':data.color}" @click="openDataDtl(data.id.value)">
          <span v-if="getType(item.name) === 'datetime'">{{item.value | formatDate(item.format.formatType)}}</span>
          <span v-else-if="getType(item.name) === 'number'">{{item.value | pointTo(item.format)}}</span>
          <span v-else-if="getType(item.name) === 'location'">{{item.value.value}}</span>
          <span v-else-if="getType(item.name) === 'picklist' || getType(item.name) === 'multi' || getType(item.name) === 'mutlipicklist'">
            <span v-for="(child,index) in item.value" :key="index" :style="{'background':child.color}" :class="{'white-font':child.color === '#FFFFFF' || child.color === undefined}">{{child.label}}</span>
          </span>
          <span v-else-if="getType(item.name) === 'area'">{{item.value | areaTo}}</span>
          <span v-else-if="getType(item.name) === 'personnel' || getType(item.name) === 'department'">
            <span v-for="(child,index) in item.value" :key="index" class="personnel-span">{{child.name}}</span>
          </span>
          <span v-else-if="getType(item.name) === 'reference'" v-for="(child,index) in item.value" :key="index">{{child.name}}</span>
          <span v-else-if="getType(item.name) === 'picture'" @click.stop="showPicDetail(item,data)">
            <i class="iconfont icon-JPGPNG-" :style="{'opacity':item.value > 0?'1':'.3'}"></i>
          </span>
          <span v-else-if="getType(item.name) === 'subform'" @click.stop="showSubformDetail(item,data)">
            <i class="iconfont icon-Rectangle13" :style="{'opacity':item.value > 0?'1':'.3'}"></i>
          </span>
          <span v-else-if="getType(item.name) === 'formula' || getType(item.name) === 'functionformula' || getType(item.name) === 'seniorformula'">{{item.value | formulsTo(item.format)}}</span>
          <span v-else-if="getType(item.name) === 'barcode'" class="barcode"><span class="code">{{item.value}}</span>
            <el-popover placement="bottom-end" :width="editorImgWidthBox" trigger="hover" popper-class="barcode" @show="showCode(item.value,item)" v-if="item.value">
              <div class="qrcode-box">
                <img :src="barcodeImg" :style="{'width':editorImgWidth}">
                <p>使用Teamface APP扫描访问数据</p>
                <el-button type="primary" size='mini' @click="printqrcode($event)">打印</el-button>
              </div>
              <i class="iconfont icon-tiaoma" slot="reference"></i>
            </el-popover>
            <i class="iconfont icon-tiaoma" v-else style="margin-left:5px;font-size:18px;color: #4A4A4A;"></i>
          </span>
          <span v-else >{{item.value}}</span>
        </div>
      </div>
      <div class="list-empty" v-if="showList && dataList.length === 0"><span>暂无数据</span></div>
    </el-checkbox-group>
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="currentPage"
      :page-sizes="pageSizes"
      :page-size="pageSize"
      :layout="layout"
      :total="total">
    </el-pagination>
    <!-- 自表单列表弹窗 -->
    <subfrom-popup></subfrom-popup>
  </el-container>
</template>

<script>
import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'
import SubfromPopup from '@/common/alert/subfrom-popup'
// import { mapState, mapGetters, mapMutations } from 'vuex'
export default {
  name: 'CommonModuleList',
  props: ['bean', 'fieldName', 'query', 'dataId', 'showSort'],
  components: {SubfromPopup},
  data () {
    return {
      token: '',
      myBean: this.bean,
      editorImgWidth: '340px',
      editorImgWidthBox: '',
      menuId: '',
      fieldNames: this.fieldName,
      querys: this.query,
      checkAll: false,
      checkedData: [],
      dataList: [],
      dataTitleList: [],
      showList: false,
      isIndeterminate: false,
      layout: 'total, sizes, prev, pager, next, jumper',
      currentPage: 1,
      pageSize: 20,
      pageSizes: [20, 30, 50, 100],
      total: 0,
      scrollDistance: 0,
      getList: {
        bean: this.bean,
        // menuId: this.menuId, // 菜单id
        fuzzyMatching: '',
        queryWhere: {// 条件非必传参数，不传查询条件时查询所有
        },
        sortField: '', // desc 降序、asc 升序
        pageInfo: {// 分页信息
          pageNum: 1,
          pageSize: 20
        }
      },
      sortField: '',
      barcodeImg: ''
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    if (!this.fieldNames) {
      let bean = {bean: this.myBean, menuId: this.$route.query.menuId}
      this.getList.menuId = this.$route.query.menuId
      this.ajaxGetDataShowList(bean)
      this.ajaxGetDataList(this.getList)
    } else if (this.fieldNames === -1) {
      // 邮件模块调用
      let bean = {bean: this.myBean, menuId: ''}
      this.ajaxGetDataShowList(bean)
      this.ajaxGetDataList(this.getList)
    } else {
      let bean = {bean: this.myBean, menuId: ''}
      this.ajaxGetDataShowList(bean)
      this.getList.relationWhere = this.querys
      this.ajaxGetDataList(this.getList)
    }
  },
  methods: {
    // 获取滚动距离
    getScroll (evt) {
      this.scrollDistance = evt.target.scrollLeft
    },
    // 打开列表显示设置窗口
    openSetListShow () {
      let data = {bean: this.myBean, menuId: this.$route.query.menuId, type: 0, list: this.dataTitleList}
      this.$bus.emit('openShowDrag', data)
    },
    // 全选
    handleCheckAllChange (val) {
      let list = []
      this.dataList.map((item, index) => {
        if (!item.lockedState) {
          list.push(item.id.value)
        }
      })
      this.checkedData = val ? list : []
      this.isIndeterminate = false
    },
    // 单选
    handleCheckedCitiesChange (value) {
      let checkedCount = value.length
      this.checkAll = checkedCount === this.dataList.length
      this.isIndeterminate = checkedCount > 0 && checkedCount < this.dataList.length
      console.log(this.checkedData)
    },
    showSubformDetail (item, data) { // 打开子表单
      if (item.value > 0) {
        let senddata = {
          bean: this.$route.query.bean,
          name: item.name,
          id: data.id.value
        }
        HTTPServer.findDetailByName(senddata, 'Loading').then((res) => {
          this.$bus.$emit('openSubformPopup', res)
        })
      }
    },
    showPicDetail (item, data) { // 打开图片
      if (item.value > 0) {
        let senddata = {
          bean: this.$route.query.bean,
          name: item.name,
          id: data.id.value
        }
        HTTPServer.findDetailByName(senddata, 'Loading').then((res) => {
          res.dataList.map((v, k) => {
            v.file_url += '&TOKEN=' + this.token
          })
          this.openPreview(res)
        })
      }
    },
    // 图片打开预览
    openPreview (file) {
      file.isModuleListShowPics = true
      file.fileType = 'img'
      this.$bus.emit('file-preview', file)
    },
    // 根据组件类型显示列表字段
    getType (name) {
      let type = name.split('_')[0]
      return type
    },
    // 打开详情侧弹
    openDataDtl (id) {
      this.$bus.emit('openDataDtl', true, id, this.myBean)
      this.checkAll = false
      this.isIndeterminate = false
      this.checkedData = []
    },
    // 列表排序
    sortList (name) {
      let desc = ':desc'
      let asc = ':asc'
      let field
      if (this.sortField === name + desc) {
        field = name + asc
      } else {
        field = name + desc
      }
      this.sortField = name + desc
      this.getList.sortField = field
      this.ajaxGetDataList(this.getList)
    },
    // 分页-每页多少条
    handleSizeChange (val) {
      console.log(`每页 ${val} 条`)
      this.getList.pageInfo.pageSize = val
      this.ajaxGetDataList(this.getList)
    },
    // 分页-多少页
    handleCurrentChange (val) {
      console.log(`当前页: ${val}`)
      this.getList.pageInfo.pageNum = val
      this.ajaxGetDataList(this.getList)
    },
    // 展示条形码
    showCode (code, item) {
      this.ajaxGetBarcodeImg(code, item)
    },
    // 获取条形码
    ajaxGetBarcodeImg (barcode, item) {
      let data = {
        bean: this.bean,
        barcodeValue: barcode
      }
      this.$http.getBarcodeImg(data).then((res) => {
        this.barcodeImg = res.barcodePic
        this.dataTitleListShow.map((v, k) => {
          if (v.field === item.name) {
            if (v.field_param.codeStyle === 'Data Matrix') {
              this.editorImgWidth = '220px'
              this.editorImgWidthBox = '300'
            } else {
              this.editorImgWidth = '340px'
              this.editorImgWidthBox = '400'
            }
          }
        })
      })
    },
    // 封装导出数据
    exportList (titleList, dataList, name) {
      let tableHeader = []
      titleList.map((item, index) => {
        if (item.show === '1') {
          tableHeader.push({
            colname: item.field,
            coltext: item.label
          })
        }
      })
      let tableList = []
      dataList.map((item) => {
        let obj = {}
        item.row.map((item2) => {
          let value
          if (this.getType(item2.name) === 'personnel' || this.getType(item2.name) === 'department' || this.getType(item2.name) === 'reference') {
            let person = []
            if (Array.isArray(item2.value)) {
              item2.value.map((item3, index3) => {
                person.push(item3.name)
              })
            }
            value = person.toString()
          } else if (this.getType(item2.name) === 'picklist' || this.getType(item2.name) === 'multi' || this.getType(item2.name) === 'mutlipicklist') {
            let picklist = []
            if (Array.isArray(item2.value)) {
              item2.value.map((item3, index3) => {
                picklist.push(item3.label)
              })
            }
            value = picklist.toString()
          } else if (this.getType(item2.name) === 'datetime') {
            if (item2.value) {
              value = tool.formatDate(item2.value, 'yyyy-MM-dd HH:mm:ss')
            } else {
              value = ''
            }
          } else if (this.getType(item2.name) === 'area') {
            value = tool.utilClass.areaTo(item2.value)
          } else {
            value = item2.value
          }
          obj[item2.name] = value
        })
        tableList.push(obj)
      })
      tool.JSONToCSV(tableList, tableHeader, name)
    },
    // 获取列表显示
    ajaxGetDataShowList (data) {
      HTTPServer.getCustomListShow(data, 'Loading').then((res) => {
        if (res.fields) {
          this.dataTitleList = res.fields
        }
      })
    },
    // 获取列表数据
    ajaxGetDataList (data) {
      if (this.fieldNames === -1) {
        // 邮件模块调用
        data.fromType = 'email'
      }
      HTTPServer.getCustomList(data, 'Loading').then((res) => {
        res.dataList.map((item, index) => {
          item.row.map((item2, index2) => {
            this.dataTitleList.map((item3, index3) => {
              if (item3.field === item2.name) {
                item2.format = item3.field_param
              }
            })
          })
        })
        this.currentPage = res.pageInfo.pageNum
        this.pageSize = res.pageInfo.pageSize
        this.total = res.pageInfo.totalRows
        this.dataList = res.dataList
        this.$nextTick(() => {
          this.showList = true
        })
      })
    },
    // 获取所有列表数据导出
    exportAllData (data, name) {
      data.pageInfo.pageNum = 1
      data.pageInfo.pageSize = this.total
      HTTPServer.getCustomList(data, 'Loading').then((res) => {
        this.exportList(this.dataTitleList, res.dataList, name)
      })
    },
    printqrcode (e) {
    }
    // ...mapMutations({
    //   setCustomModuleList: 'CUSTOM_MODULE_LIST'
    // })
  },
  mounted () {
    this.$bus.off('setCheckEmpty')
    this.$bus.on('setCheckEmpty', value => {
      this.isIndeterminate = false
      this.checkAll = false
      this.checkedData = []
    })
    // 刷新列表数据
    this.$bus.off('refreshList')
    this.$bus.on('refreshList', value => {
      this.checkedData = []
      if (value) {
        if (value.bean) {
          this.myBean = value.bean
          this.getList.bean = value.bean
        }
        this.getList.fuzzyMatching = value.fuzzyMatching ? value.fuzzyMatching : ''
        this.getList.menuId = value.menuId ? value.menuId : ''
        this.menuId = value.menuId ? value.menuId : ''
        this.getList.queryWhere = value.filter ? value.filter : {}
        if (value.seas_pool_id) {
          this.getList.seas_pool_id = value.seas_pool_id
        } else {
          this.getList.seas_pool_id = null
        }
      }
      if (this.fieldNames) {
        // 详情里关联列表查询
        this.getList = {
          bean: this.myBean,
          relationWhere: this.querys,
          pageInfo: {// 分页信息
            pageNum: 1,
            pageSize: 20
          }
        }
      }
      let bean = {bean: this.myBean, menuId: this.menuId}
      this.ajaxGetDataShowList(bean)
      this.ajaxGetDataList(this.getList)
    })
    // 导出数据
    this.$bus.off('exportFile')
    this.$bus.on('exportFile', (value) => {
      if (value.allData) {
        this.exportAllData(this.getList, value.name)
      } else {
        let dataList = []
        this.dataList.map((item) => {
          this.checkedData.map((item2) => {
            if (item.id.value === item2) {
              dataList.push(item)
            }
          })
        })
        this.exportList(this.dataTitleList, dataList, value.name)
      }
    })
  },
  computed: {
    dataTitleListShow () {
      let list = []
      if (this.dataTitleList.length > 0) {
        this.dataTitleList.map((item, index) => {
          if (item.show === '1') {
            list.push(item)
          }
        })
        return list
      }
    }
    // ...mapGetters([
    //   'custom_module_list'
    // ]),
    // ...mapState({
    //   custom_module_list: state => state.custom.custom_module_list
    // })
  },
  watch: {
    checkedData (val, oldValue) {
      this.$bus.emit('checkedData', this.myBean, val)
    }
  },
  beforeDestroy () {
    console.log('销毁list')
  }
}
</script>
<style lang="scss" scoped>
.module-list-wrip{
  display: block;
  position: relative;
  height: calc(100% - 20px);
  min-height: 50px;
  .titles{
    display: flex;
    width: 100%;
    height: 50px;
    line-height: 50px;
    padding: 0 10px;
    border-bottom: 1px solid #F2F2F2;
    box-sizing: border-box;
    .el-checkbox{
      margin: 0 5px 0 0;
    }
    .item{
      min-width: 160px;
      max-width: 280px;
      padding: 0 0 0 8px;
      box-sizing: border-box;
      margin-bottom: -1px;
      border-bottom: 1px solid #F2F2F2;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      span{
        font-size: 14px;
        color: #17171A;
        font-weight: bold;
      }
      i{
        float: right;
        font-size: 16px;
        margin: 17px 0 0 0;
        padding: 0 8px 0 0;
        color: #B2B2B2;
        border-right: 1px solid #E7E7E7;
        cursor: pointer;
      }
    }
    >.iconfont{
      position: absolute;
      width: 50px;
      font-size: 20px;
      right: -20px;
      top: 15px;
      height: 20px;
      line-height: 20px;
      padding: 0 0 0 5px;
      color: #51D0B1;
      background-color: #FFFFFF;
      cursor: pointer;
    }
  }
  .el-checkbox-group{
    height: calc(100% - 94px);
    min-height: 43px;
    overflow: auto;
    .list{
      display: flex;
      height: 50px;
      line-height: 50px;
      padding: 0 10px 0 4px;
      border-bottom: 1px solid #F2F2F2;
      cursor: pointer;
      &:hover{
        background: #F2F2F2;
        .rows{
          background: #F2F2F2;
        }
      }
      .el-checkbox{
        // flex: 1;
        padding: 0 6px;
      }
      .rows{
        min-width: 160px;
        max-width: 280px;
        padding: 0 0 0 8px;
        font-size: 14px;
        color: #69696C;
        box-sizing: border-box;
        margin-bottom: -1px;
        border-bottom: 1px solid #F2F2F2;
        >span{
          display: block;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          .icon-Rectangle13,.icon-JPGPNG-{
            font-size:24px;
            color:#B5B5C6;
          }
          span{
            display: inline-block;
            max-width: 160px;
            min-width: 52px;
            height: 24px;
            line-height: 24px;
            font-size: 12px;
            color: #FFFFFF;
            padding: 0 8px;
            margin: 0 10px 0 0 ;
            border-radius: 4px;
            text-align: center;
            vertical-align: middle;
            box-sizing: border-box;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          .personnel-span{
            display: inline-block;
            max-width: auto;
            min-width: auto;
            font-size: 14px;
            color: #69696C;
            padding: 0;
            margin: 0 5px 0 0 ;
          }
          .white-font{
            display: inline;
            color: #4A4A4A;
            line-height: 46px;
            font-size: 14px;
            padding: 0;
            text-align: left;
            background: none !important;
          }
        }
        >.barcode{
          display: flex;
          height: 50px;
          align-items: center;
          >span.code{
            display: inline-block;
            width: calc(100% - 25px);
            max-width: 100%;
            font-size: 14px;
            padding: 0;
            margin: 0;
            color: #69696C;
            text-align: left;
            vertical-align: text-top;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          >span{
            align-items: center;
            min-width: auto;
            padding: 0;
            margin: 0 0 0 5px;
            i{
              font-size: 18px;
              color: #4A4A4A;
              cursor: pointer;
            }
          }
        }
      }
    }
    .list.border-list{
      padding: 0 10px 0 0;
      >label{
        border-left: 4px solid;
        border-top: 1px solid;
        border-bottom: 1px solid;
        border-top-left-radius: 2px;
        border-bottom-left-radius: 2px;
      }
      .rows{
        margin: 0;
        border-top: 1px solid;
        border-bottom: 1px solid;
        &:last-child{
          border-right: 1px solid;
        }
      }
    }
    .list-empty{
      position: relative;
      width: 100px;
      height: 100px;
      margin: 70px auto 0;
      background-image: url('/static/img/custom/list_empty.png');
      background-repeat: no-repeat;
      background-size: cover;
      span{
        position: absolute;
        bottom: -25px;
        left: 24px;
        color: #D0D0D0;
      }
    }
  }
  .el-pagination {
    position: absolute;
    width: 100%;
    bottom: 0;
    left: 0;
    text-align: right;
    padding: 15px 0 0;
    border-top: 1px solid #E7E7E7;
    background: #FFFFFF;
    z-index: 2;
  }
}

</style>
<style lang="scss">
.module-list-wrip{
  .el-checkbox{
    >.el-checkbox__label{
      display: none;
    }
  }
}
.el-popover.barcode{
  padding: 18px 8px 10px;
  margin-top: 5px;
  .qrcode-box{
    img{
      width: 340px;
      height: auto;
      margin: 0 0 5px;
    }
  }
}
</style>
