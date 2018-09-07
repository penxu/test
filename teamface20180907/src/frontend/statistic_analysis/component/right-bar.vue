<template>
   <div class="drc-right-container">
     <div class="drc-right-title"><span>数据源</span></div>
     <div :style="{'height':fieldHeight}" class="field-set-box">
      <div class="drc-sel-source">
        <div>
          <el-select v-model="sourceModuleVal" placeholder="请选择" @change="sourceChange($event)" >
            <el-option
              v-for="item in sourceModule"
              :key="item.value"
              :label="item.chinese_name"
              :value="item.english_name"
              :title="item.chinese_name"
              style="width: 172px">
            </el-option>
          </el-select>
        </div>
        <div class="drc-source-list" v-show="sourceRelationList.length !==0">
          <div v-for="(sourceRelation, index) in  sourceRelationList" :key="sourceRelation.name" :class="{'selected':sourceRelation.selected}" @click="sourceRelaClick(index)" class="source-item" :title="sourceRelation.title" ><span>{{sourceRelation.title}}</span></div>
        </div>
        <div class="drc-source-title"><span>请选择数据源</span></div>
      </div>
      <div class="drc-search-box">
        <el-input v-model="searchVal" placeholder="快速查找"  suffix-icon="el-icon-search" @keyup.native="handleSearch()"></el-input>
      </div>
      <div class="drc-dimension-box">
          <div class="drc-dimension-title"><span>维度</span></div>
          <el-collapse v-model="activeNames">
            <el-collapse-item  v-for="(dimension, dimensionIdx) in dimensionList" :key="dimension.name" :name="dimensionIdx+1" >
              <template slot="title" :title="dimension.title">
                <div :title="dimension.title" class="field-title">{{dimension.title}}</div>
              </template>
              <draggable :options="XdragOption" @start="startDrag($event)" v-model=" dimension.fields" @end="endDrap($event)" @add="addDrap($event)">
                <div class="field-item" id="dimension" v-for="field in dimension.fields" :key="field.name"  v-show="field.label.includes(searchVal)">
                  <div :title="field.label">{{field.label}}</div>
                </div>
              </draggable>
            </el-collapse-item>
          </el-collapse>
          <div class="drc-dimension-title"><span>数值</span></div>
          <el-collapse v-model="numValActive" v-for="(numVal, numValIndex) in numberValueList" :key="numVal.name">
            <el-collapse-item :name="numValIndex+1">
              <template slot="title">
                <div :title="numVal.title" class="field-title">{{numVal.title}}</div>
              </template>
              <draggable :options="YdragOption" @start="startDrag($event)" v-model="numVal.fields" @end="endDrap($event)">
                <div class="field-item" id="value" v-for="field in numVal.fields" :key="field.name" v-show="field.label.includes(searchVal)">
                  <div :title="field.label">{{field.label}}</div>
                </div>
              </draggable>
            </el-collapse-item>
          </el-collapse>
      </div>
     </div>
    </div>
</template>

<script>
import { HTTPServer } from '@/common/js/ajax.js'
import draggable from 'vuedraggable'
import { mapActions, mapState, mapMutations } from 'vuex'
export default {
  name: 'rightBar',
  components: {
    draggable
  },
  props: ['rightHeight', 'switchSource', 'sourceRelationListProp'],
  data () {
    return {
      sourceModuleVal: '',
      sourceModule: [
      ],
      input: '',
      activeNames: [1, 2],
      numValActive: [1, 2],
      dimensionList: [],
      bgColor: false,
      searchVal: '',
      allFieldLabel: '',
      sourceRelationList: this.sourceRelationListProp,
      numberValueList: [],
      availHeight: '',
      fieldHeight: '',
      isSearchVal: false
    }
  },
  computed: {
    XdragOption () {
      return {
        animation: 200,
        group: { name: 'field', pull: 'clone', put: false },
        sort: false,
        ghostClass: 'group-data'
      }
    },
    YdragOption () {
      return {
        animation: 200,
        group: { name: 'number', pull: 'clone', put: false },
        sort: false,
        ghostClass: 'group-data'
      }
    },
    ...mapState({
      currentSelBean: state => state.definedChart.currentSelBean,
      sourceSelBean: state => state.definedChart.sourceSelBean,
      sourceModuleList: state => state.definedChart.sourceModuleList,
      isChangeSource: state => state.definedChart.isChangeSource,
      currentField: state => state.definedChart.currentField
    })
  },
  methods: {
    getClientHeight () {
      // this.availHeight =  + 'px'
      this.fieldHeight = (document.body.clientHeight - 168) + 'px'
    },
    // 获取数据源模块列表
    getAllModuleList () {
      HTTPServer.getAppAllModuleList()
        .then((res) => {
          console.log(res, '获取所有模块列表')
          this.sourceModule = res
        })
    },
    // 获取数据源关联模块
    getSourceRelationModule (source) {
      HTTPServer.getSourceRelationModule(source)
        .then((res) => {
          console.log(res, '获取数据源关联模块')
          this.sourceRelationList = res
          this.sourceRelationList.map((item, index) => {
            this.$set(item, 'selected', false)
          })
        })
    },
    // 获取数据源字段
    getSourceField (relationModuleBean) {
      HTTPServer.getSourceField(relationModuleBean)
        .then((res) => {
          console.log(res, '获取数据源字段列表')
          this.dimensionList = res.dimensionFields
          this.numberValueList = res.numberFields
        })
    },
    // 数据源发生变化
    sourceChange (data) {
      console.log(data, '数据源发生变化')
      let sourceParams = {
        sourceModuleBean: '',
        sourceModuleTitle: ''
      }
      this.sourceModule.map((item, index) => {
        if (data === item.english_name) {
          sourceParams.sourceModuleBean = data
          sourceParams.sourceModuleTitle = item.chinese_name
          // this.sourceModuleVal = item.chinese_name
          console.log(this.sourceModuleVal, '当前模块')
        }
      })
      console.log(sourceParams, '改变后')
      this.changeSource(!this.isChangeSource)
      this.switchCurrentBean(sourceParams)
      console.log(this.currentSelBean, '改变选择后的状态')
      this.dimensionList = []
      this.numberValueList = []
      this.getSourceRelationModule(sourceParams)
    },
    // 点击数据源
    sourceRelaClick (index) {
      this.sourceRelationList.map((item, idx) => {
        item.selected = false
      })
      this.sourceRelationList[index].selected = true
      let relationBean = {
        relationModuleBean: this.sourceRelationList[index].bean
      }
      console.log(relationBean, this.sourceModuleVal, '传送数据源')
      this.switchSourceBean(relationBean)
      this.switchSourceList(this.sourceRelationList)
      // bus.$emit('sourceModuleMsg', this.sourceModuleVal, relationBean, this.sourceRelationList)
      // this.getSourceField(relationBean)
    },
    startDrag (data) {
      // console.log(data.target.id, '开始拖动')
      // this.setCurrentField(data.target.id)
      this.setCurrentField(data.item.id)
      this.setIsDrag(true)
    },
    endDrap (data) {
      // console.log(data, '结束拖动')
      // console.log(data.item.id, '结束拖动')
      this.setCurrentField('')
    },
    addDrap (data) {
      // console.log(data, '结束拖动')
      // console.log(data.to.className, '结束拖动')
      // if (data.to.className === '') {
      //   this.$message({
      //     message: '只能拖入同类型字段!',
      //     type: 'warning',
      //     showClose: 'true'
      //   })
      // }
    },
    // 计算总字段数
    combineFile () {
      this.dimensionList.map((item, index) => {
        this.allFieldLabel += `${item.label},`
      })
      this.numberValueList.map((item1, index1) => {
        this.allFieldLabel += `${item1.label},`
      })
    },
    // 搜索字段
    handleSearch () {
      // console.log(this.allFieldLabel, this.searchVal)
      // this.dimensionList.map((dimemsion, dimemsionIndex) => {
      //   dimemsion.fields.map((item1, index1) => {
      //     if (this.searchVal === '') {
      //       item1.active = false
      //     } else {
      //       if (item1.label.includes(this.searchVal)) {
      //         item1.active = true
      //       } else {
      //         item1.active = false
      //       }
      //     }
      //   })
      // })
      // this.numberValueList.map((value, valueIndex) => {
      //   value.fields.map((item2, index2) => {
      //     if (this.searchVal === '') {
      //       item2.active = false
      //     } else {
      //       if (item2.label.includes(this.searchVal)) {
      //         item2.active = true
      //       } else {
      //         item2.active = false
      //       }
      //     }
      //   })
      // })
    },
    // 状态管理
    ...mapMutations({
      switchCurrentBean: 'SWITCH_CURRENT_BEAN',
      switchSourceBean: 'SWITCH_SOURCE_BEAN',
      switchSourceList: 'SWITCH_SOURCE_LIST',
      changeSource: 'CHANGE_CURRENT_BEAN',
      setCurrentField: 'SET_CURRENT_FIELD',
      setIsDrag: 'SET_IS_DRAG'
    }),
    ...mapActions([
      'setModuleBean'
    ])
  },
  watch: {
    currentSelBean (val) {
      this.sourceModuleVal = val.sourceModuleTitle
      // console.log(this.sourceModuleVal, 'right,改变后的bean')
    },
    sourceSelBean (val) {
      // console.log(val, '当前选择数据源')
      if (val.relationModuleBean !== '') {
        this.getSourceField(val)
      } else {
        this.dimensionList = []
        this.numberValueList = []
      }
    },
    sourceModuleList (value) {
      // console.log(value, '当前数据源列表LIST')
      if (value) {
        this.sourceRelationList = JSON.parse(JSON.stringify(value))
      }
    },
    sourceModuleVal: (val) => {
      this.sourceModuleVal = val
      // console.log(this.sourceModuleVal, '数据源发生改变watch')
    }
  },
  mounted () {
    // console.log(this.currentSelBean, 'bean')
    this.getClientHeight()
    // this.combineFile()
    this.getAllModuleList()
  }
}
</script>
<style lang="scss">
  .drc-right-container {
    width: 100%;
    height: 100%;
    .field-set-box {
      overflow: auto;
    }
    .bg-color {
      background-color: #20BF9A
    }
    .drc-right-title {
      width: 100%;
      line-height: 48px;
      text-align: center;
      border-bottom: 1px solid #E7E7E7;
      span {
        font-size: 16px;
      }
    }
    .drc-sel-source {
      padding: 12px 14px;
      border-bottom: 1px solid #E7E7E7;
      .drc-source-title {
        span {
          font-size: 12px;
          color: #A0A0AE;
          line-height: 22px;
        }
      }
      .el-input__inner {
        height: 30px;
      }
      .drc-source-list {
        width: 100%;
        border: 1px solid #E7E7E7;
        box-sizing: border-box;
        margin-top: 5px;
        padding: 7px 10px;
         div {
           span {
             line-height: 25px;
           }
         }
        :hover {
          cursor: pointer;
          // background: #E7E7E7;
        }
        .selected {
          background: #E7E7E7;
          border-radius: 3px;
          padding-left: 3px;
        }
        .source-item {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
    .drc-search-box {
      padding: 12px 14px;
      .el-input__icon {
        line-height: 30px;
      }
      .el-input__inner {
        height: 30px;
      }
      border-bottom: 1px solid #E7E7E7;
    }
    .drc-dimension-box{
      overflow: auto;
      padding-left: 18px;
      padding-bottom: 15px;
      overflow: auto;
      .drc-dimension-title {
        margin-top: 15px;
      }
      .el-collapse {
        border: none;
      }
      .field-item {
        line-height: 25px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .el-collapse-item__arrow {
        float: left;
      }
      .el-icon-arrow-right:before {
        content: '\E60E';
      }
      .is-active.el-icon-arrow-right {
        transform: rotate(90deg)
      }
      .el-collapse-item__header {
        height:35px;
        font-size: 15px;
        border:none;
        .field-title {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
      .el-collapse-item__wrap {
        border: none;
        padding-left: 20px;
      }
      .el-collapse-item__content {
        padding-bottom: 0;
      }
      .el-collapse-item {
        .el-collapse-item__arrow {
          line-height: 1;
          margin-top:17px;
        }
      }
    }
  }
</style>
