<template>
 <div class="component-item" :class="{'top-bottom':property.field.structure === '0','input-error':error}">
    <label :for="property.name" :style="{'line-height':property.label.length>6?'20px':'40px'}">
      <span class="required-box" v-if="property.field.fieldControl === '2'"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <div class="people-box">
      <div class="people-item" v-for="(people,index) in peopleList" :key="index">
        <head-image :head="people"></head-image>
        <i class="el-icon-circle-close" @click="delPeople(index)" v-if="property.field.fieldControl !== '1'"></i>
      </div>
      <div class="people-add">
        <i class="iconfont icon-xinzengrenyuan" @click="openPeople(property.field.chooseType)" v-if="property.field.fieldControl !== '1'"></i>
      </div>
    </div>
    <p v-show="error" :class="{'error-p':error}"><i class="el-icon-warning"></i> 必填项</p>
  </div>
</template>

<script>
import HeadImage from '@/common/components/head-image'
import {HTTPServer} from '@/common/js/ajax.js'
import { mapMutations, mapState } from 'vuex'
export default {
  name: 'LayoutPersonnel',
  props: ['property', 'saveData', 'LinkageFields'],
  components: {
    HeadImage
  },
  data () {
    return {
      error: false,
      token: '',
      employee: {},
      peopleList: this.property.field.defaultPersonnel
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.employee = JSON.parse(sessionStorage.getItem('userInfo')).employeeInfo
    if (location.href.indexOf('tag=1') !== -1) {
      this.employee = {
        'id': 1,
        'name': '张三',
        'picture': '',
        'type': 1,
        'sign_id': 10000,
        'value': '1:1',
        'checked': true
      }
    }
  },
  methods: {
    ...mapMutations({
      linkageFieldsData: 'LINKAGE_FIELDS_DATA'
    }),
    changevalue (val) { // 判断是否是数据联动
      setTimeout(() => {
        if (this.LinkageFields && this.LinkageFields.length > 0) {
          this.LinkageFields.map((v, k) => {
            if (v === this.property.name && val) {
              this.findAggregationDataLinkageList(val)
            }
          })
        }
      }, 100)
    },
    findAggregationDataLinkageList (val) { // 联动字段变更请求相应数据接口文档
      let people = []
      val.map((item, index) => {
        people.push(item.id)
      })
      let senddata = {
        bean: this.$route.query.bean,
        field: this.property.name,
        value: people.toString()
      }
      HTTPServer.findAggregationDataLinkageList(senddata).then((res) => {
        if (res && JSON.stringify(res) !== '{}' && JSON.stringify(res) !== '[]') {
          this.linkageFieldsData(res)
        }
      })
    },
    // 打开选人控件
    openPeople (type) {
      let types = type === '0' ? 1 : ''
      let navKey = type === '0' ? '' : '1'
      if (this.property.field.chooseRange.length !== 0) {
        // 可选范围
        if (type === '0') {
          types = 5
        } else {
          types = 6
        }
      }
      // 给父组件传值
      if (!this.property.field.choosePersonnel) {
        let peopleList = []
        this.peopleList.map((item) => {
          item.type = 1
          item.value = '1-' + item.id
          peopleList.push(item)
        })
        this.$bus.emit('commonMember',
          { 'type': types,
            'prepareData': peopleList,
            'prepareKey': this.property.name,
            'rangeData': this.property.field.chooseRange,
            'seleteForm': true,
            'banData': [],
            'navKey': navKey,
            'removeData': [],
            'index': 0
          })
      } else {
        // 审批
        this.$bus.emit('approvalMember', this.property.field.choosePersonnel, this.property.name)
      }
    },
    // 删除人员
    delPeople (index) {
      this.peopleList.splice(index, 1)
      this.setValue()
    },
    // 组件赋值
    setValue () {
      let people = []
      this.peopleList.map((item, index) => {
        people.push(item.id)
      })
      this.saveData[this.property.name] = people.toString()
    }
  },
  mounted () {
    if (this.property.field.defaultPersonnel.length > 0) {
      this.property.field.defaultPersonnel.map((item) => {
        if (item.value === '3-personnel_create_by_superior') {
          item.id = this.employee.id
          item.name = this.employee.name
          item.picture = this.employee.picture
          item.value = '1-' + this.employee.id
        }
      })
      this.setValue()
    }
    // 获取人员单选数据
    this.$bus.on('selectMemberRadio', (value) => {
      if (value.prepareKey === this.property.name) {
        this.peopleList = value.prepareData
        this.setValue()
      }
    })
    // 获取人员多选数据
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value.prepareKey === this.property.name) {
        this.peopleList = value.prepareData
        this.setValue()
      }
    })
    // 获取可选范围人员单选数据
    this.$bus.on('select-optional-scope-radio', (value) => {
      if (value.prepareKey === this.property.name) {
        this.peopleList = value.prepareData
        this.setValue()
      }
    })
    // 获取可选范围人员多选数据
    this.$bus.on('select-optional-scope-multi', (value) => {
      if (value.prepareKey === this.property.name) {
        this.peopleList = value.prepareData
        this.setValue()
      }
    })
    // 关联映射
    this.$bus.on('setValue', mapId => {
      let key = Object.keys(mapId).toString()
      if (key.includes(this.property.name)) {
        this.peopleList = mapId[this.property.name]
        this.setValue()
      }
    })
    // 校验
    this.$bus.on('sendValidator', name => {
      if (name === this.property.name) {
        this.error = true
      }
    })
  },
  computed: {
    ...mapState({
      linkage_fields_data: state => state.custom.linkage_fields_data
    })
  },
  watch: {
    peopleList (newVal) {
      if (newVal.length !== 0) {
        this.error = false
        this.changevalue(newVal)
      }
    },
    linkage_fields_data: {
      deep: true,
      handler (newVal) {
        if (newVal && JSON.stringify(newVal) !== '{}') {
          for (let i in newVal) {
            if (i === this.property.name && newVal[i]) {
              this.peopleList = newVal[i]
              this.setValue()
            }
          }
        }
      }
    }
  },
  beforeDestroy () {
    // this.$bus.off('selectMemberRadio') // 销毁
    // this.$bus.off('selectEmpDepRoleMulti') // 销毁
    // this.$bus.off('select-optional-scope-radio') // 销毁
    // this.$bus.off('select-optional-scope-multi') // 销毁
    // this.$bus.off('setValue') // 销毁
    // this.$bus.off('sendValidator') // 销毁
  }
}
</script>
