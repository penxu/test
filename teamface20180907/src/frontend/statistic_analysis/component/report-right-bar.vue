<template>
   <div class="drc-right-container">
     <div class="drc-right-title"><span>数据源</span></div>

     <div class="drc-search-box">
       <el-input v-model="input" placeholder="快速查找"  suffix-icon="el-icon-search"></el-input>
     </div>
     <div class="drc-dimension-box" :style="{'height': rightBoxHeight}">
        <div class="drc-dimension-title"><span>维度</span></div>
        <el-collapse v-model="activeNames">
          <el-collapse-item :name="index+1" v-for="(dimension, index) in dimensionList" Consistency :key="dimension.name">
            <template slot="title">
              <div class="field-title" :title="dimension.title">{{dimension.title}}</div>
            </template>
            <draggable :options="XdragOption" @start="startDrag($event, index, 0)" v-model=" dimension.fields" @end="endDrap($event)">
              <div class="field-item" v-for="field in dimension.fields" :key="field.name" v-show="field.label.includes(input)">
                <span>{{field.label}}</span>
              </div>
            </draggable>
          </el-collapse-item>
        </el-collapse>
        <div class="drc-dimension-title" v-if="reportType != 0"><span>数值</span></div>
        <el-collapse v-model="numValActive" v-for="(numVal, index) in numberValueList" Consistency :key="numVal.name" v-if="reportType != 0">
          <el-collapse-item :name="index+1" >
            <template slot="title">
              {{numVal.title}}
            </template>
            <draggable :options="YdragOption" @start="startDrag($event, index, 1)" v-model="numVal.fields" @end="endDrap($event)">
              <div class="field-item" v-for="field in numVal.fields" v-show="field.label.includes(input)" :key="field.name">
                <span>{{field.label}}</span>
              </div>
            </draggable>
          </el-collapse-item>
        </el-collapse>
     </div>

    </div>
</template>

<script>
import { HTTPServer } from '@/common/js/ajax.js'
import draggable from 'vuedraggable'
export default {
  name: 'rightBar',
  components: {
    draggable
  },
  props: ['rightHeight'],
  data () {
    return {
      sourceModuleVal: '',
      sourceModule: [],
      input: '',
      activeNames: [1, 2],
      numValActive: [1, 2],
      dimensionList: [],
      bgColor: false,
      searchVal: '',
      allFieldLabel: '',
      sourceRelationList: [],
      numberValueList: [],
      rightBoxHeight: 500,
      reportType: 0
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
        group: { name: 'field', pull: 'clone', put: false },
        sort: false,
        ghostClass: 'group-data'
      }
    }
  },
  methods: {
    searchField () {
      if (event.keyCode === 13) {
        // this.getSourceField(this.input)
      }
    },
    getClientHeight () {
      this.availHeight = window.screen.availHeight + 'px'
      console.log(window.screen.availHeight, window.screen.height, '可用高度')
      this.rightBoxHeight = (window.screen.availHeight - 520) + 'px'
      console.log(this.rightBoxHeight, 'rightHeight')
    },
    // 获取数据源字段
    getSourceField (value) {
      HTTPServer.getSourceField({'relationModuleBean': this.$route.query.been}).then((res) => {
        console.log(res, '获取数据源字段列表')
        this.dimensionList = res.dimensionFields
        this.numberValueList = res.numberFields
      })
    },
    startDrag (event, index, type) {
      console.log(type, '开始拖动', event)
      var data = (type === 0) ? this.dimensionList[index].fields[event.oldIndex] : this.numberValueList[index].fields[event.oldIndex]
      this.$bus.emit('report-start-drag', data, type)
    },
    endDrap (data) {
    },
    // 计算总字段数
    combineFile () {
      this.dimensionList.map((item, index) => {
        this.allFieldLabel += item.label
      })
    },
    handleSearch () {
      if (this.allFieldLabel.includes(this.searchVal)) {
      }
    },
    currentField (data) {
      console.log(1111, data)
    }
  },
  mounted () {
    // this.getClientHeight()
    this.getSourceField()
    console.log(22222222222, this.$route.query.been)
    this.$bus.on('reportTypeChange', (value) => {
      this.reportType = value
    })
  }
}
</script>
<style lang="scss">
  .drc-right-container {
    width: 100%;
    height: 100%;
    overflow: auto;
    .bg-color {
      background-color: #20BF9A
    }
    .drc-right-title {
      width: 100%;
      line-height: 50px;
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
             line-height: 30px;
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
      border-bottom: 1px solid #E7E7E7;
      padding-left: 18px;
      padding-bottom: 15px;
      height:calc(100% - 300px);
      overflow: auto;
      .drc-dimension-title {
        margin-top: 15px;
      }
      .el-collapse {
        border: none;
         .field-title {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
      .field-item {
        line-height: 25px;
        // padding-left: 22px;
      }
      .el-collapse-item__arrow {
        float: left;
      }
      .el-icon-arrow-right:before {
        content: '\E60E';
      }
      .el-collapse-item__header {
        height:35px;
        font-size: 15px;
        border:none;
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
