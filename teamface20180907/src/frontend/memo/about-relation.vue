<template>
  <div class="about-relation">
    <!-- 上 -->
    <div class="top-bar">
      <p class="top-bar-title">应用</p>
      <div class="first-select">
        <el-select v-model="valueFirst" @change="firstSelectChange()" placeholder="请选择">
          <el-option
            v-for="item in optionsFirst"
            :key="item.value"
            :label="item.name"
            :value="item.name"
            style="width:170px"
            @click.native="firstSelected(item.modules)">
          </el-option>
        </el-select>
      </div>
      <div class="second-select">
        <el-select v-model="valueSecond" placeholder="请选择">
          <el-option
            v-for="item in optionsSecond"
            :key="item.value"
            :label="item.chinese_name"
            :value="item.chinese_name"
            style="width:296px"
            @click.native="secondSelected(item)">
          </el-option>
        </el-select>
      </div>
    </div>
    <!-- 中 -->
    <div class="center-bar">
      <div class="third-input">
        <el-input v-model="inputThird" readonly="readonly"></el-input>
      </div>
    </div>
    <!-- 下 -->
    <div class="bottom-bar">
      <div class="search-bar">
          <el-input placeholder="请搜索内容" @change="search(searchValue)" prefix-icon="el-icon-search" v-model="searchValue">
          </el-input>
      </div>
      <div class="search-list">
        <ul>
          <li v-for="(item,index) in dataList" :key="index" @click="selectCurrentData(item)">{{item.title}}</li>
        </ul>
      </div>
    </div>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'

export default {
  name: 'aboutRelation',
  // props: ['dialogVisibleAbout'],
  data () {
    return {
      valueFirst: '', // 应用名称
      optionsFirst: [], // 应用数据列表
      valueSecond: '', // 模块名称
      optionsSecond: [], // 应用数据列表
      modulesList: [], // 下拉框-模块数据列表
      inputThird: '', // 模块数据
      searchValue: '', // 搜索值
      searchList: [], // 搜索列表
      dataList: [], // 具体数据列表
      currentBean: '', // 模块的bean
      itemsArr: [], // 最终关联数据数组
      moduleId: '' // 模块id
    }
  },
  created () {
    // 获取应用数据
    HTTPServer.getAddModuleList().then((res) => {
      console.log(res, '应用数据')
      this.optionsFirst = res
    })
  },
  methods: {
    firstSelectChange () {
      // 清空optionsSecond
      this.valueSecond = ''
    },
    // 选择应用
    firstSelected (data) {
      this.optionsSecond = data
    },
    // 选择模块
    secondSelected (data) {
      console.log(data, '模块数据')
      this.moduleId = data.id
      this.currentBean = data.english_name
      // 默认选好模块就展示出所有数据
      this.search()
    },
    // 输入框搜索操作
    search (data) {
      // 根据模块查询数据
      let obj = {
        bean: this.currentBean,
        fieldValue: data || ''
      }
      HTTPServer.getFirstFieldFromModule(obj).then((res) => {
        console.log(res, '具体数据')
        this.dataList = res
      })
    },
    selectCurrentData (data) {
      this.inputThird = `${this.valueFirst}-${this.valueSecond}-${data.title}`
      // 发送选好的值给父组件 (type 1项目 2自定义   先写死自定义)
      // this.itemsArr.push({id: data.id, name: this.inputThird, bean: this.currentBean, type: 2})
      this.$bus.$emit('relationData', {id: data.id, title: this.inputThird, module: this.currentBean, type: '2', beanNameChinese: data.title, moduleId: this.moduleId})
    }
  },
  updated () {
    // console.log(this.valueFirst, 'valueFirst')
  }
}
</script>
<style lang="scss">
  .about-relation {
    .top-bar {
      overflow: hidden;
      .top-bar-title,.first-select,.second-select {
        float: left;
      }
      .top-bar-title {
        font-size: 16px;
        color: #909090;
        margin-top: 7px;
      }
      .first-select {
        height: 40px;
        margin-left: 20px;
        input {
          width: 170px;
        }
      }
      .second-select {
        height: 40px;
        margin-left: 20px;
        input {
          width: 298px;
        }
      }
    }
    .center-bar {
      float: left;
      margin-left: 52px;
      margin-top: 20px;
      .third-input {
        height: 40px;
        input {
          width: 488px;
        }
      }
    }
    .bottom-bar {
      width: 488px;
      float: left;
      margin-left: 52px;
      margin-top: 5px;
      margin-bottom: 20px;
      .search-list {
        border: 1px solid #E7E7E7;
        ul {
          height: 200px;
          overflow-y: auto;
          li {
            height: 30px;
            line-height: 30px;
            padding-left: 20px;
            cursor: pointer;
            &:hover {
              background-color:#ECEEF1;
            }
          }
        }
      }
    }
    .el-select-dropdown__item {
      width: 230px;
    }
  }
</style>
