<template>
  <el-container>
    <div class="applayout-container clear">
        <div class="preview pull-left">
          <img src="../../../assets/custom/app_list_layout.png" alt="">
        </div>
        <div class="applayout-setting pull-left">
          <div class="text-div"><span>列表布局</span>
            <div class="save-btn"><el-button type="primary" size="small" @click="saveListLayout()">保存</el-button></div>
          </div>
          <div class="option-div">
              <el-select v-model="oneLineModel" placeholder="请选择" value-key="label" @change="oneModelChange($event,0)">
                <el-option
                  v-for="(item,index) in listData"
                  :key="index"
                  :label="item.label"
                  :value="item"
                  v-show="item.type !== 'multitext'">
                </el-option>
              </el-select>
          </div>
          <!-- 第二行 -->
          <div class="select-box clear" v-show="twoColVisable">
            <div class="pull-left option-div" >
                <div v-for="(twoList,index) of secondLine" :key="index" :class="{'two-width': secondLine.length ===2,'three-width': secondLine.length ===3}" class="pull-left one-width">
                <el-select v-model="twoList.label" placeholder="请选择" value-key="" @change="twoModelChange($event,index)">
                  <el-option
                    v-for="twoitem in listData"
                    :key="twoitem.value"
                    :label="twoitem.label"
                    :value="twoitem"
                    v-show="twoitem.type !== 'multitext'">
                  </el-option>
                </el-select>
                <div class="del-col" @click="handleDelete('twoCol',index)">
                  <i class="el-icon-error"></i>
                </div>
                </div>
            </div>
            <div class="add-btn-div pull-left">
              <el-button type="primary" icon="el-icon-plus" @click="handleAdd('twoCol')">添加</el-button>
            </div>
          </div>
          <!-- 第三行 -->
          <div class="select-box clear" v-show="threeColVisable">
            <div class="pull-left option-div" >
              <div v-for="(list,index) in thirdLine" :key="index" :class="{'two-width': thirdLine.length === 2,'three-width': thirdLine.length === 3}" class="pull-left one-width">
              <el-select v-model="list.label" placeholder="请选择" value-key="label" @change="threeModelChange($event,index)">
                <el-option
                  v-for="item in listData"
                  :key="item.value"
                  :label="item.label"
                  :value="item"
                  v-show="item.type !== 'multitext'">
                </el-option>
              </el-select>
                <div class="del-col" @click="handleDelete('threeCol',index)">
                  <i class="el-icon-error"></i>

                </div>

              </div>
            </div>
            <div class="add-btn-div pull-left">
              <el-button type="primary" icon="el-icon-plus" @click="handleAdd('threeCol')">添加</el-button>
            </div>
          </div>
          <div class="add-btn-div add-btn">
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd('line')">添加</el-button>
          </div>
        </div>
    </div>
  </el-container>
</template>
<script>
import { HTTPServer } from '@/common/js/ajax.js'
import { mapState, mapMutations } from 'vuex'
export default {
  name: 'AppListLayout',
  data () {
    return {
      threeColVisable: false, // 显示三列
      twoColVisable: false, // 显示双列
      secondLine: [],
      thirdLine: [],
      currentBean: {'bean': this.$route.query.bean},
      listData: [],
      oneLineModel: '',
      secondLineModel: '',
      thirdLineModel: '',
      initData: {label: '', name: '', type: ''},
      listDataLayout: {
        'bean': this.$route.query.bean,
        'companyId': '',
        'terminal': '',
        'title': '',
        'fields': [[{'field': '', 'show': '1', 'label': '', 'type': ''}]]
      },
      allOk: true,
      temListLayout: {}
    }
  },
  methods: {
    handleAdd (type) {
      console.log(this.secondLine, this.twoLine, type, 5644654698)
      switch (type) {
        case 'twoCol':
          this.twoColVisable = true
          let len1 = this.secondLine.length
          if (len1 < 3) {
            let twoData = {label: '', field: '', type: '', show: '1'}
            this.secondLine.push(twoData)
          } else {
            this.messageAlert('列')
          }
          console.log(this.secondLine, '.....')
          break
        case 'threeCol':
          let len2 = this.thirdLine.length
          if (len2 < 3) {
            let threeData = {label: '', field: '', type: '', show: '1'}
            this.thirdLine.push(threeData)
          } else {
            this.messageAlert('列')
          }
          break
        case 'line':
          if (this.twoColVisable && this.threeColVisable) {
            this.messageAlert('行')
          } else if (this.twoColVisable) {
            if (this.thirdLine.length === 0) {
              let thirdLineData = {label: '', field: '', type: '', show: '1'}
              this.thirdLine.push(thirdLineData)
              // this.twoColVisable = true
            }
            this.threeColVisable = true
            console.log('警告')
          } else {
            if (this.secondLine.length === 0) {
              let secondLineData = {label: '', field: '', type: '', show: '1'}
              this.secondLine.push(secondLineData)
            }
            this.twoColVisable = true
            console.log(this.secondLine, '第二行数据')
          }
          break
        default:
          break
      }
    },
    handleDelete (type, index) {
      console.log(type, index)
      switch (type) {
        case 'twoCol':
          this.secondLine.splice(index, 1)
          if (this.secondLine.length === 0) {
            this.twoColVisable = false
          }
          break
        case 'threeCol':
          this.thirdLine.splice(index, 1)
          if (this.thirdLine.length === 0) {
            this.threeColVisable = false
          }
          break
      }
    },
    messageAlert (type) {
      this.$message({
        showClose: true,
        message: '最多只能添加3' + type + '数据',
        type: 'warning'
      })
    },
    // 获取当前模块所有字段
    ajaxGetDataList (data) {
      console.log(this.currentBean, '3245646')
      HTTPServer.getAllFieldList(this.currentBean)
        .then((res) => {
          console.log(res, '模块字段')
          res.map((item, index) => {
            if (item.type !== 'picture' && item.type !== 'attachment' && item.type !== 'subform') {
              this.listData.push(item)
            }
          })
          console.log(this.listData, '字段列表')
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 获取列表布局
    ajaxGetlistLayout () { // 获取列表布局
      let obj = { bean: this.currentBean.bean, terminal: 1 }
      HTTPServer.getDataListFields(obj, true)
        .then((res) => {
          console.log(res, '获取列表布局')
          // this.listDataLayout.bean = res.bean
          this.listDataLayout.companyId = res.companyId
          this.listDataLayout.terminal = res.terminal
          this.listDataLayout.title = res.title
          this.oneLineModel = res.fields[0][0].label
          this.listDataLayout.fields[0] = res.fields[0]
          if (res.fields[1] !== undefined) {
            this.secondLine = res.fields[1]
          }
          if (res.fields[2] !== undefined) {
            this.thirdLine = res.fields[2]
          } else {
            this.thirdLine = []
          }

          this.secondLine.length > 0 ? this.twoColVisable = true : this.twoColVisable = false

          this.thirdLine.length > 0 ? this.threeColVisable = true : this.threeColVisable = false
          this.temListLayout = JSON.parse(JSON.stringify(res))
          console.log(this.listDataLayout, '列表布局')
          console.log(this.oneLine, this.secondLine, this.thirdLine, '1,2,3')
        })
        .catch((err) => {
          console.log(err)
        })
    },
    ajaxSaveListLayout (data) { // 保存列表布局
      HTTPServer.saveAppDataListFields(data)
        .then((res) => {
          this.$message({
            showClose: true,
            message: '保存列表布局成功！',
            type: 'success'
          })
          this.ajaxGetlistLayout()
        })
        .catch((err) => {
          console.log(err)
          this.$message({
            showClose: true,
            message: '保存失败!',
            type: 'error'
          })
        })
    },
    oneModelChange (data, index) {
      console.log(data, index, '第一行')
      this.oneLineModel = data.label
      this.listDataLayout.fields[0][index].label = data.label
      // this.$set(this.listDataLayout.fields[0], index, data.lebel)
      this.listDataLayout.fields[0][index].type = data.type
      this.listDataLayout.fields[0][index].field = data.field
      if (data.type === 'datetime') {
        this.listDataLayout.fields[0][index].format = data.format
      }
      console.log(this.listDataLayout, '列表数据')
    },
    twoModelChange (data, index) {
      console.log(data, index, '第二行')
      this.secondLine[index].label = data.label
      this.secondLine[index].field = data.field
      this.secondLine[index].type = data.type
      if (data.type === 'datetime') {
        this.secondLine[index].format = data.format
      }
      console.log(this.secondLine, 222222)
    },
    threeModelChange (data, index) {
      this.thirdLine[index].label = data.label
      this.thirdLine[index].field = data.field
      this.thirdLine[index].type = data.type
      if (data.type === 'datetime') {
        this.thirdLine[index].format = data.format
      }
      console.log(this.thirdLine, '33333')
    },
    saveListLayout () {
      if (this.listDataLayout.fields[0].field === '') {
        this.$message({
          showClose: true,
          message: '字段不能为空',
          type: 'warning'
        })
        this.allOk = false
        return
      } else {
        this.allOk = true
      }
      this.secondLine.forEach((item, index) => {
        if (item.field === '') {
          this.$message({
            showClose: true,
            message: '字段不能为空',
            type: 'warning'
          })
          this.allOk = false
          return false
        } else {
          this.allOk = true
        }
      })
      this.thirdLine.forEach((item, index) => {
        if (item.field === '') {
          console.log(item, '.....')
          this.$message({
            showClose: true,
            message: '字段不能为空',
            type: 'warning'
          })
          this.allOk = false
          return false
        } else {
          this.allOk = true
        }
      })
      this.listDataLayout.fields.splice(1, 2)
      console.log(this.listDataLayout.fields, 11111111)
      if (this.secondLine.length !== 0) {
        this.listDataLayout.fields.push(this.secondLine)
      }
      if (this.thirdLine.length !== 0) {
        this.listDataLayout.fields.push(this.thirdLine)
      }
      if (this.allOk) {
        this.ajaxSaveListLayout(this.listDataLayout)
      }
    },
    ...mapMutations({
      setAppListLayout: 'APP_LSIT_LAYOUT'
    })
  },
  created () {
    console.log(this.$route.query, '获取bean')
  },
  computed: {
    ...mapState({
      appListLayout: state => state.custom.app_list_layout
    })
  },
  mounted () {
    console.log(this.secondLine, this.thirdLine)
    this.ajaxGetDataList()
    this.ajaxGetlistLayout()
    console.log(this.secondLine, typeof this.secondLine, '第二行数组')
  }
}
</script>
<style lang="scss">
  .applayout-container {
    padding: 30px 0 30px 124px;
    width: 100%;
    background-color: #fff;
    border-top: 1px solid #ddd;
    position: relative;
    .preview {
      width: 300px;
      height: 620px;
      // border: 1px solid #dddddd;
      font-size: 30px;
      box-sizing: border-box;
    }
    .applayout-setting {
      //border:1px solid #ccc;
      margin-left: 30px;
      width: 70%;
      .text-div{
        text-align: left;
        >span {
          font-size: 20px;
          color: #17171A;
        }
      }
      .select-box {
        width: 100%;
        .one-width {
          width: 100%;
          position: relative;
        }
        .two-width {
          width: 48%;
          position: relative;
          .el-select {
            width: 100%;
          }
        .el-input__inner {
          width: 100%;
          height: 35px;
        }
        &:nth-child(2) {
          margin-left:23px;
        }
      }
      .three-width {
        width:31%;
        position: relative;
        .el-select {
           width: 100%;
        }
          .el-input__inner {
          width: 100%;
          height: 35px;
        }
      &:nth-child(2){
        margin-left:21px;
      }
      &:nth-child(3) {
        margin-left:21px;
      }
      }
    }
      .option-div {
        width: 600px;
        // .item {
        //   width: 100%;
        //   .el-badge__content {
        //     width: 14px;
        //     text-align: center;
        //     padding:0;
        //     height: 14px;
        //     line-height: 13px;
        //     border-radius: 14px;
        //   }
        // }
        .del-col{
          position: absolute;
          right: -5px;
          top: -8px;
          color:red;
          cursor: pointer;
        }
        .el-select {
          width: 100%;
        }
      }
      [class$="-div"] {
        margin-top:23px;
        .el-input__inner {
          width: 100%;
          height: 35px;
        }
      }
      .add-btn-div {
        margin-top: 23px;
        margin-left:15px;
        text-align: left;
        .el-button {
          height: 35px;
          padding: 10px 20px;
        }
      }
      .add-btn {
        margin-left:0;
      }
    }
    .save-btn {
      position: absolute;
      right: 30px;
      top: -50px;
    }
  }
</style>
