<template>
  <div class="tag-box-wrip">
    <div class="tag-button">
      <el-button type="text" size="mini" icon="el-icon-plus" @click="openPeople">添加成员</el-button>
      <el-dropdown trigger="click" placement="bottom-start" @visible-change="getList" @command="insertField" v-if="type === 0">
        <span class="el-dropdown-link">
          <i class="el-icon-plus"></i>插入字段
        </span>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item v-for="(item, index) in fieldList" :key="index" :command="item">{{item.label}}</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
    <div class="tag-list">
      <el-tag v-for="(tag,index) in tagList" :key="index" closable size="medium" color="#E9EDF2" @close="handleClose(index)">
        {{tag.name}}
      </el-tag>
      <el-input class="input-new-tag" v-if="inputVisible" ref="saveTagInput" v-model="inputValue" placeholder="输入邮箱地址" @keyup.enter.native="handleInputConfirm" @blur="handleInputConfirm"></el-input>
      <el-button v-else class="button-new-tag" icon="iconfont icon-module-menu-1" @click="showInput" v-show="type === 0">输入邮箱地址</el-button>
    </div>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'TagBox',
  props: {
    type: {
      type: Number,
      default: 0 // 邮件
    },
    name: {
      type: String
    },
    tagVal: {
      type: Array
    }
  },
  data () {
    return {
      tagList: this.tagVal,
      inputValue: '',
      inputVisible: false,
      fieldList: []
    }
  },
  created () {
    console.log(this.name)
    // this.tagList = this.tagVal
  },
  methods: {
    // 删除tag
    handleClose (index) {
      this.tagList.splice(index, 1)
    },
    // 切换显示新增tag按钮
    showInput () {
      this.inputVisible = true
      this.$nextTick(_ => {
        this.$refs.saveTagInput.$refs.input.focus()
      })
    },
    // 手动输入新增tag
    handleInputConfirm () {
      let inputValue = this.inputValue
      let regex = new RegExp('^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$')
      if (inputValue && regex.test(inputValue)) {
        this.tagList.push({name: inputValue, id: inputValue, category: 'email'})
        this.$emit('input', this.tagList)
      } else if (inputValue && !regex.test(inputValue)) {
        this.$message({
          duration: 1000,
          message: '请输入正确的邮箱地址',
          type: 'warning'
        })
      }
      this.inputVisible = false
      this.inputValue = ''
    },
    // 打开选人控件
    openPeople () {
      let people = []
      this.tagList.map((item, index) => {
        if (item.category !== 'email' && item.category !== 'field') {
          people.push(item)
        }
      })
      this.$bus.emit('commonMember',
        { 'type': this.type === 1 ? 1 : '',
          'prepareData': people,
          'prepareKey': this.name,
          'seleteForm': true,
          'bean': this.$route.query.bean,
          'banData': [],
          'navKey': '1,3',
          'removeData': []
        }
      )
    },
    // 插入字段
    insertField (field) {
      let data = {
        category: 'field',
        name: field.label,
        id: field.name
      }
      this.tagList.push(data)
      this.$emit('input', this.tagList)
    },
    // 获取插入字段
    getList (evt) {
      if (evt && this.fieldList.length === 0) {
        let data = {
          moduleBean: this.$route.query.bean,
          type: 4
        }
        this.ajaxGetModuleFields(data)
      }
    },
    // 自定义工作流获取模块下插入字段
    ajaxGetModuleFields (data) {
      HTTPServer.getFieldsOfWorkflow(data).then((res) => {
        this.fieldList = res
      })
    }
  },
  mounted () {
    // 获取人员单选数据
    this.$bus.on('selectMemberRadio', (value) => {
      if (value.prepareKey === this.name) {
        value.prepareData.map((item) => {
          let tag = {
            category: item.type === 3 ? 'peopleField' : 'people',
            id: item.type === 3 ? item.identifer : item.id,
            name: item.name,
            identifer: item.identifer,
            picture: item.picture,
            sign_id: item.sign_id,
            type: item.type,
            value: item.value
          }
          this.tagList = [tag]
          this.$emit('input', this.tagList)
        })
      }
    })
    // 获取人员多选数据
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value.prepareKey === this.name) {
        // 剔除非人员字段
        let people = []
        this.tagList.map((item, index) => {
          if (item.category === 'email' || item.category === 'field') {
            people.push(item)
          }
        })
        this.tagList = people
        value.prepareData.map((item) => {
          let tag = {
            category: item.type === 3 ? 'peopleField' : 'people',
            id: item.type === 3 ? item.identifer : item.id,
            name: item.name,
            identifer: item.identifer,
            picture: item.picture,
            sign_id: item.sign_id,
            type: item.type,
            value: item.value
          }
          this.tagList.push(tag)
          this.$emit('input', this.tagList)
        })
      }
    })
  }
}
</script>

<style lang="scss">
.tag-box-wrip{
  .tag-button{
    .el-button{
      margin: 0 20px 0 0;
    }
    .el-dropdown{
      width: 90px;
      .el-dropdown-link {
        color: #549AFF;
        cursor: pointer;
        .el-icon-plus{
          margin: 0 5px 0 0;
        }
      }
    }
  }
  .tag-list{
    .el-tag{
      margin: 0 10px 10px 0;
      color: #4A4A4A;
      border-color: #FFFFFF;
      .el-icon-close{
        font-size: 16px;
        color: #bfbfbf;
        &:hover{
          background-color: #E9EDF2;
        }
      }
    }
    .button-new-tag {
      padding: 4px 10px;
      .iconfont{
        margin: 0 5px 0 0;
      }
    }
    .input-new-tag {
      height: 30px;
      line-height: 30px;
      input{
        height: 30px;
        line-height: 30px;
      }
    }
  }
}
</style>
