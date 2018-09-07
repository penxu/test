<template>
  <el-container class="data-filter-wrip">
    <el-header>
      <a @click="closeDialog"><i class="iconfont icon-pc-paper-nextpa"></i>返回</a>
      <a @click="clearContent">清空</a>
    </el-header>
    <el-main>
      <el-collapse>
        <el-collapse-item v-for="field in layoutFilter" :key="field.name" v-if="field.type !== 'mutlipicklist'">
          <template slot="title">
            <span class="titles">{{field.name}}</span>
          </template>
          <div class="box">
            <div class="picklist" v-if="field.type === 'picklist' || field.type === 'multi'">
              <div class="item" v-for="(item,index) in field.entrys" :key="index" @click="selectCondition(field,item)">
                <small :style="{'background':item.color}"></small>{{item.label}}<i class="el-icon-check" v-show="item.isSelect"></i>
              </div>
            </div>
            <div class="personnel" v-else-if="field.type === 'personnel' || field.type === 'department'">
              <div class="item" v-for="(item,index) in field.member" :key="index" @click="selectCondition(field,item)">
                <img :src="item.picture+'&TOKEN='+token" alt="" v-if="item.picture !== ''">
                <i v-else class="name-img">{{item.name | limitTo(-2)}}</i>
                {{item.name}}<i class="el-icon-check" v-show="item.isSelect"></i>
              </div>
            </div>
            <div class="number" v-else-if="field.type === 'number'">
              <el-input v-model="field.min_value" placeholder="最小" @change="resetSelect(field)" type="number"></el-input>至
              <el-input v-model="field.max_value" placeholder="最大" @change="resetSelect(field)" type="number"></el-input>
               <div class="picklist">
                <div class="item" v-for="(item,index) in field.entrys" :key="index" @click="selectCondition(field,item)">
                  <small></small>{{item.label}}<i class="el-icon-check" v-show="item.isSelect"></i>
                </div>
              </div>
            </div>
            <div class="datetime" v-else-if="field.type === 'datetime'">
              <div class="picklist" >
                <div class="item" v-for="(item,index) in field.entrys" :key="index" @click="selectCondition(field,item)">
                  <small></small>{{item.label}}<i class="el-icon-check" v-show="item.isSelect"></i>
                </div>
              </div>
              <el-date-picker v-model="field.start" type="datetime" value-format="timestamp" placeholder="选择日期时间" @change="resetTime(field)"></el-date-picker>至
              <el-date-picker v-model="field.end" type="datetime" value-format="timestamp" placeholder="选择日期时间" @change="resetTime(field)"></el-date-picker>
            </div>
            <div class="area" v-else-if="field.type === 'area'">
              <vue-area v-model="field.search" :property="{name: field.id,field: {fieldControl: '0'}}" :styleType="'vertical'"></vue-area>
            </div>
            <div class="text" v-else>
              <el-input v-model.trim="field.search" placeholder="请输入内容" @change="resetSelect(field)"></el-input>
              <div class="picklist">
                <div class="item" v-for="(item,index) in field.entrys" :key="index" @click="selectCondition(field,item)">
                  <small></small>{{item.label}}<i class="el-icon-check" v-show="item.isSelect"></i>
                </div>
              </div>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
      <el-button type="primary" size="mini" plain class="confirm" @click="filterField">确 定</el-button>
    </el-main>
  </el-container>
</template>

<script>
import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'
import VueArea from '@/common/components/vue-area'
export default {
  name: 'ModuleFilter',
  components: {
    VueArea
  },
  props: ['bean', 'menuId'],
  data () {
    return {
      token: '',
      layoutFilter: []
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    let bean = {bean: this.bean}
    this.ajaxFilterField(bean)
  },
  methods: {
    // 根据组件类型显示列表字段
    getType (name) {
      let type = name.split('_')[1]
      return type
    },
    // 关闭弹框
    closeDialog () {
      this.$bus.emit('closeFilterModal', false)
    },
    // 清空所有内容
    clearContent () {
      this.layoutFilter.map((item, index) => {
        item.search = item.start = item.end = item.min_value = item.max_value =
        item.province = item.city = item.county = ''
        if (item.entrys) {
          item.entrys.map((item2, index2) => {
            item2.isSelect = false
          })
        }
        if (item.member) {
          item.member.map((item2, index2) => {
            item2.isSelect = false
          })
        }
      })
    },
    // 选定条件
    selectCondition (field, items) {
      switch (field.type) {
        case 'picklist': case 'multi':
          items.isSelect = !items.isSelect
          break
        case 'personnel': case 'department':
          items.isSelect = !items.isSelect
          break
        case 'datetime':
          field.entrys.map((item, index) => {
            item.isSelect = false
          })
          items.isSelect = true
          field.start = ''
          field.end = ''
          break
        default:
          field.entrys.map((item, index) => {
            item.isSelect = false
          })
          items.isSelect = true
          field.writeType = items.value
          if (items.value === 1) {
            field.search = ''
            field.min_value = ''
            field.max_value = ''
          }
          break
      }
    },
    // 提交筛选
    filterField () {
      let filter = {}
      this.layoutFilter.map((item, index) => {
        if (item.type === 'picklist' || item.type === 'multi') {
          let list = []
          item.entrys.map((item2, index2) => {
            if (item2.isSelect) {
              list.push(item2.value)
            }
          })
          if (list.length !== 0) {
            filter[item.id] = list.toString()
          }
        } else if (item.type === 'personnel' || item.type === 'department') {
          let list = []
          if (item.member) {
            item.member.map((item2, index2) => {
              if (item2.isSelect) {
                list.push(item2.id)
              }
            })
          }
          if (list.length !== 0) {
            filter[item.id] = list.toString()
          }
        } else if (item.type === 'datetime') {
          item.entrys.map((item2, index2) => {
            if (item2.isSelect) {
              let time = this.getTimeRange(item2.value, item.start, item.end)
              filter[item.id] = time
            }
          })
        } else if (item.type === 'number') {
          if (item.writeType === 0) {
            filter[item.id] = 'ISNOTNULL'
          } else if (item.writeType === 1) {
            filter[item.id] = 'ISNULL'
          } else if (item.writeType === 2) {
            if (item.min_value !== '' || item.max_value !== '') {
              filter[item.id] = {minValue: item.min_value, maxValue: item.max_value}
            }
          }
        } else if (item.type === 'area') {
          if (item.search) {
            filter[item.id] = item.search
          }
        } else {
          if (item.writeType === 0) {
            filter[item.id] = 'ISNOTNULL'
          } else if (item.writeType === 1) {
            filter[item.id] = 'ISNULL'
          } else if (item.writeType === 2 && item.search !== '') {
            filter[item.id] = item.search
          }
        }
      })
      let value = {filter: filter, menuId: this.menuId}
      this.$bus.emit('refreshList', value)
      this.$bus.emit('closeFilterModal', false)
    },
    // 重置选中状态
    resetSelect (field) {
      if (field.type === 'number') {
        // if (field.min_value !== '' || field.max_value !== '') {
        //   field.writeType = 2
        //   field.entrys.map((item) => {
        //     item.isSelect = false
        //   })
        // }
      } else {
        // if (field.search !== '') {
        //   field.writeType = 2
        //   field.entrys.map((item) => {
        //     item.isSelect = false
        //   })
        // }
      }
    },
    // 重置时间状态
    resetTime (field) {
      if (field.start !== '' || field.end !== '') {
        field.entrys.map((item) => {
          if (item.value === 8) {
            item.isSelect = true
          } else {
            item.isSelect = false
          }
        })
      }
      if (!field.start && !field.end) {
        field.entrys.map((item) => {
          item.isSelect = false
        })
      }
    },
    // 获取时间范围
    getTimeRange (type, start, end) {
      let now = new Date().setHours(0, 0, 0, 0)
      let startTime
      // eslint-disable-next-line
      let endTime
      switch (type) {
        case 0:
        // 今天
          startTime = now
          endTime = startTime + 86399000
          break
        case 1:
        // 昨天
          startTime = now - 86400000
          endTime = startTime + 86399000
          break
        case 2:
        // 过去七天
          startTime = now - 86400000 * 7
          endTime = now + 86399000
          break
        case 3:
        // 过去30天
          startTime = now - 86400000 * 30
          endTime = now + 86399000
          break
        case 4:
        // 本月
          startTime = new Date(now).setDate(1)
          endTime = startTime + 86400000 * (new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0).getDate() - 1) + 86399000
          break
        case 5:
        // 上月
          startTime = new Date(new Date().getFullYear(), new Date().getMonth() - 1, 1).getTime()
          endTime = new Date(new Date().getFullYear(), new Date().getMonth() - 1, new Date(new Date().getFullYear(), new Date().getMonth(), 0).getDate()).getTime()
          break
        case 6:
        // 本季度
          startTime = tool.getQuarterStartDate().getTime()
          endTime = tool.getQuarterEndDate().getTime()
          break
        case 7:
        // 上季度
          startTime = tool.getPriorSeasonFirstDay(new Date().getFullYear(), new Date().getMonth() + 1).getTime()
          endTime = startTime + 90 * 86400000 + 86399000
          break
        case 8:
        // 范围
          startTime = start
          endTime = end
          break
        default:
          break
      }
      return {startTime: startTime, endTime: endTime}
    },
    /**
     * AJAX获取筛选字段
     */
    ajaxFilterField (data) {
      HTTPServer.getFilterList(data, 'Loading').then((res) => {
        res.map((item, index) => {
          if (item.type === 'datetime') {
            item.start = ''
            item.end = ''
            item.entrys = [
              {
                label: '今天',
                value: 0,
                isSelect: false
              },
              {
                label: '昨天',
                value: 1,
                isSelect: false
              },
              {
                label: '过去七天',
                value: 2,
                isSelect: false
              },
              {
                label: '过去30天',
                value: 3,
                isSelect: false
              },
              {
                label: '本月',
                value: 4,
                isSelect: false
              },
              {
                label: '上月',
                value: 5,
                isSelect: false
              },
              {
                label: '本季度',
                value: 6,
                isSelect: false
              },
              {
                label: '上季度',
                value: 7,
                isSelect: false
              },
              {
                label: '范围',
                value: 8,
                isSelect: false
              }
            ]
          } else if (item.type === 'picklist' || item.type === 'multi') {
            if (item.entrys) {
              item.entrys.map((item, index) => {
                item.isSelect = false
              })
            }
          } else if (item.type === 'personnel' || item.type === 'department') {
            if (item.member) {
              item.member.map((item, index) => {
                item.isSelect = false
              })
            }
          } else if (item.type === 'number') {
            item.writeType = 2
            item.min_value = ''
            item.max_value = ''
            item.entrys = [
              {
                label: '已填写',
                value: 0,
                isSelect: false
              },
              {
                label: '未填写',
                value: 1,
                isSelect: false
              }
            ]
          } else {
            item.writeType = 2
            item.search = ''
            item.entrys = [
              {
                label: '已填写',
                value: 0,
                isSelect: false
              },
              {
                label: '未填写',
                value: 1,
                isSelect: false
              }
            ]
          }
        })
        this.layoutFilter = res
      })
    }
  },
  watch: {
  }
}
</script>

<style lang="scss" scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity .5s
}
.fade-enter, .fade-leave-to /* .fade-leave-active in below version 2.1.8 */ {
  opacity: 0
}
.slide-enter-active {
  transition: all .5s linear;
}
.slide-leave-active {
  transition: all .5s linear;
}
.slide-enter, .slide-leave-to
/* .slide-fade-leave-active for below version 2.1.8 */ {
  transform: translateX(320px);
}
.data-filter-wrip{
  background: #FFFFFF;
  position: fixed;
  width: 320px;
  top: 0;
  bottom: 0;
  right: 0;
  z-index: 10;
  box-shadow: -2px 2px 4px 0 rgba(155,155,155,0.30), 2px -2px 4px 0 ;
  .el-header{
    line-height: 60px;
    padding: 0 20px;
    box-shadow: 0 1px 2px 0 rgba(185,185,185,0.50);
    z-index: 10;
    a{
      cursor: pointer;
      font-size: 16px;
      &:first-child{
        color: #69696C;
        .iconfont{
          font-size: 16px;
          margin: 0 5px 0 0;
        }
      }
      &:last-child{
        color: #BBBBC3;
        float: right;
      }
    }
  }
  .el-main{
    padding: 0 20px;
  }
}
</style>
<style lang="scss">
.data-filter-wrip{
  overflow-y: auto;
  overflow-x: hidden;
  padding: 0 0 50px 0;
  .el-collapse{
    height: 100%;
  }
  .el-collapse-item{
    span.titles{
      display: inline-block;
      width: calc(100% - 50px);
      margin: 0 0 0 10px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .box{
      padding:10px;
      .text{
        .el-input{
          height: 50px;
          box-sizing: border-box;
        }
      }
      .datetime,.number{
        .el-input{
          display: inline-block;
          width:200px;
          margin:5px 10px 5px 0;
          box-sizing: border-box;
        }
      }
      .area{
        .el-selest{
          margin: 0 0 10px 0;
        }
      }
      .picklist{
        .item{
          height: 50px;
          line-height:50px;
          padding:0 5px 0 20px;
          cursor: pointer;
          &:hover{
            background: #F2F2F2;
          }
          small{
            display: inline-block;
            width: 8px;
            height: 8px;
            border-radius:50%;
            background: #CBE0FD;
            margin:0 12px 0 0;
          }
          .el-icon-check{
            float: right;
            font-size:20px;
            margin:15px 0 0 0;
          }
        }
      }
      .personnel{
        .item{
          height: 50px;
          line-height:50px;
          padding:0 5px 0 20px;
          cursor: pointer;
          &:hover{
            background: #F2F2F2;
          }
          img{
            width: 30px;
            height: 30px;
            border-radius:2px;
            margin:0 20px 0 0;
            vertical-align: middle;
            background: rgb(39, 49, 66);
            display: inline-block;
             border-radius:50%;
          }
          .name-img{
            width: 30px;
            height: 30px;
            line-height:30px;
            margin:0 20px 0 0;
            background: rgb(39, 49, 66);
            display: inline-block;
            border-radius:50%;
            font-style: normal;
            color: #FFF;
            font-size:12px;
            text-align:center;
          }
          .el-icon-check{
            float: right;
            font-size:20px;
            margin:15px 0 0 0;
          }
        }
      }
    }
  }
  .confirm{
    position: fixed;
    bottom: 10px;
    right: 60px;
    width: 200px;
    z-index: 15;
  }
}
</style>
