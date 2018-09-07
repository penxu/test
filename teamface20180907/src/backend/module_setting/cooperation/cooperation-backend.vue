<template>
  <div class="cooperation-backend-wrap backend_main">
    <div class="header">
      <i class="iconfont icon-fanhui" style="margin-right:10px;font-size: 24px;cursor: pointer;" @click="gotoBaseMoudel"></i>
      <i class="iconfont icon-module-setting" style="margin-right:10px;font-size: 22px;"></i>
      <span>基础模块</span>
    </div>
    <div class="header workbench-title-new">
      <span class="worktable-icon"><i class="iconfont icon icon-nav-project-o"></i></span>
      <div>
        <p>协作</p>
        <p>可视化的组织工具，可以帮助您更快，更准确地管理和监督项目的开发</p>
      </div>
    </div>
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane label="项目权限" name="first">
          <div>
            <div class="projectJurisdiction">
              <div class="moduleTitle">
                <p><span class="name">项目角色权限</span>为项目成员在项目中处于不同角色所需的权限设置匹配的项目、任务的操作权限。</p>
                <!-- <el-button style="float: right;">添加角色权限</el-button> -->
                <button type="button" class="el-button el-button--primary" @click="addRolePermissions" style="padding: 8px 20px;margin: 20px 0;"><span>+</span>添加角色权限</button>
              </div>
              <div class="moduleList">
                <el-table :data="roleListContent" style="width: 100%">
                  <el-table-column  prop="name" label="项目角色" width="180"></el-table-column>
                  <el-table-column label="角色描述">
                    <template slot-scope="scope">
                      <span class="textContent">{{scope.row.describle}}</span>
                    </template>
                  </el-table-column>
                  <el-table-column  prop="update_by_name" label="更新人员" width="150"></el-table-column>
                  <el-table-column label="更新时间" width="150">
                    <template slot-scope="scope">
                      <span>{{scope.row.update_time | formatDate('yyyy-MM-dd  HH:mm')}}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="编辑" width="150">
                    <template slot-scope="scope">
                      <a @click="editorRolePermissions(scope.row)" href="javascript:;" class="edit" v-if="scope.row.id != 1">编辑</a>
                      <a href="javascript:;" @click="deleteRole(scope.row.id)" v-if="scope.row.id > 3" class="delete">删除</a>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
            <div class="promptDialogBox">
              <el-dialog :title='title' :visible.sync='outerVisible' :class="deleteData ? 'prompt-dialog':'projectRole add-dialog'">
                <div class="editorContent" v-if="roleContent">
                  <el-form :model='form'>
                    <el-form-item label='角色名称' :label-width='formLabelWidth' class="must">
                      <el-input v-model='name' placeholder="请输入"></el-input>
                      <span class="err" v-if="isname"><i class="iconfont icon-delete-tip"></i>{{isname}}</span>
                    </el-form-item>
                    <el-form-item label='角色描述' :label-width='formLabelWidth'>
                      <el-input v-model='describe' type="textarea" placeholder="请输入"></el-input>
                    </el-form-item>
                  </el-form>
                  <div class="box2">
                    <p class="role-header"><span>权限配置</span>
                      <el-checkbox class="checkAll" v-model="checkAll" @change="handleCheckAllChange" style="float: right;">全选</el-checkbox>
                      <!-- <el-checkbox :indeterminate="isIndeterminate" v-model="checkAll" @change="handleCheckAllChange" style="margin-left: 15px;">全选</el-checkbox> -->
                    </p>
                    <div class="item" v-for="(item, index) in cityOptions" :key="index">
                      <span class="title">{{item.name}}</span>
                      <!-- <div class="check-right">
                        <el-checkbox v-for="(sub, index2) in item.sub" :label="sub.id" :key="index2" v-model="">{{sub.name}}</el-checkbox>
                      </div> -->
                      <el-checkbox-group v-model="checkedPermissions" @change="handleCheckedPermissionsChange">
                        <el-checkbox v-for="(sub, index2) in item.sub" :label="sub.id"  :key="index2">{{sub.name}}</el-checkbox>
                      </el-checkbox-group>
                    </div>
                  </div>
                </div>
                <div class="content" v-if="deleteData" style="font-size: 16px;"><i class="iconfont icon-delete-tip"></i>您确定删除该角色？</div>
                <div slot="footer" class="dialog-footer">
                  <el-button @click="outerVisible = false">取 消</el-button>
                  <el-button v-if="editor1" type="primary" @click="addDetermine">确定</el-button>
                  <el-button v-if="editor2" type="primary" @click="editorDetermine">确定</el-button>
                  <el-button v-if="deleteData" type="primary" @click="deleteConfirm">确定</el-button>
                </div>
              </el-dialog>
            </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="标签管理" name="second">
        <div>
          <div class="projectLabel">
            <div class="moduleTitle">
                <p><span class="name">项目标签</span>为项目任务通过标签进行有效的管理。</p>
                <!-- <el-button @click="title1 = '新增';outerVisible1 = true;judgeAdd1=true;judgeAdd2=false;judgeAdd3=false;">新增</el-button>
                <el-button @click="title1 = '新增分类';outerVisible1 = true;judgeAdd1=false;judgeAdd2=true;judgeAdd3=false;">新增分类</el-button> -->
                <el-input placeholder="请输入内容" v-model="input5" class="input-with-select" style="width: 303px;" @keyup.enter.native="searchTag" prefix-icon="el-icon-search">
                </el-input>
                <button type="button" class="el-button el-button--primary" @click="openTagForm"><span>+</span>新增</button>
              </div>
              <!-- v-show="field.label.includes(input)" -->
              <div class="accordion">
                <!-- <i class="el-icon-sort" @click="TimeAscending(1)"></i> -->
                <!-- <draggable v-model="list" :options="parentOption" @update="updata1($event,list)" @add="changedata1($event,list)">
                  <el-collapse v-for="(v,k) in list" :key="k" class="margindiv" v-model="activeNames" @change="getSubclassification(v, k)">
                    <el-collapse-item :name="'status'+(k)">
                      <template slot="title">
                        <div style="color:#000;">
                          <i class="iconfont icon-yidong"></i>
                          <span v-text="v.name"></span>
                          <a href="javascript:;"  class="delete" @click="deleteTag(v, 0)">删除</a>
                          <a  href="javascript:;" class="edit" @click="fatherClassification(v)">编辑</a>
                        </div>
                      </template>
                      <draggable v-model="v.list" :options="sonOption" @update="updata2(k,v.list)" @add="changedata2(k,v.list)"  @end="empty(v.list, k)">
                        <div class="collapse2" v-for="(v1,k1) in v.list" :key="k1"  v-show="v1.name.includes(searchTagName)">
                          <i class="iconfont icon-yidong"></i>
                          <span class="tab-name" v-text="v1.name" style="color: #fff;" :style="v1.colour?'background:' + v1.colour:''"></span>
                          <i class="el-icon-delete"  @click="deleteTag(v1, 1)"></i>
                          <i class="el-icon-edit" @click="sonClassification(v1, v.id, k)"></i>
                        </div>
                      </draggable>
                    </el-collapse-item>
                  </el-collapse>
                </draggable> -->
                <draggable v-model="list" :options="parentOption" @update="updata1($event,list)" @add="changedata1($event,list)">
                  <div class="item" v-for="(v,k) in list" :key="k" :class="activeNames.includes(v.id) ? 'is-current':''">
                    <div class="item-title" @click="getSubclassification(v, k, $event)">
                      <i class="iconfont icon-yidong"></i>
                      <i class="el-icon-caret-right"></i>
                      <span v-text="v.name"></span>
                      <a href="javascript:;"  class="delete" @click="deleteTag(v, 0)">删除</a>
                      <a  href="javascript:;" class="edit" @click="fatherClassification(v)">编辑</a>
                    </div>
                    <draggable class="content" v-model="v.list" :options="sonOption" @update="updata2(k,v.list)" @add="changedata2(k,v.list)"  @end="empty(v.list, k)" v-show="activeNames.indexOf(v.id) >= 0">
                      <div class="item-content" v-for="(v1,k1) in v.list" :key="k1"  v-show="v1.name.includes(searchTagName)" v-if="v1.name">
                        <i class="iconfont icon-yidong"></i>
                        <span class="tab-name" v-text="v1.name" style="color: #fff;" :style="v1.colour?'background:' + v1.colour:''"></span>
                        <i class="el-icon-delete"  @click="deleteTag(v1, 1)"></i>
                        <i class="el-icon-edit" @click="sonClassification(v1, v.id, k)"></i>
                      </div>
                    </draggable>
                  </div>
                </draggable>
              </div>
              <div class="labelDialogBox">
                <el-dialog :title='classify == 1 ? "编辑标签":"新增标签"' :visible.sync='createTagForm' class='addForm createTagForm add-dialog'>
                  <el-form :model='form'>
                    <el-form-item label='分类' :label-width='formLabelWidth' class="must">
                      <el-select v-model='form.tagclassify' placeholder='请选择' @change="folderMemberData = []" style="width: 285px;">
                        <el-option v-for="option in list" :key="option.id" :label='option.name' :value='option.id'></el-option>
                      </el-select>
                      <button type="button" class="el-button el-button--primary add-classify" @click="createClassifydialog = true;form.classifyname = '';isclassifyname = '';classify = 0;"><span>+</span>新增</button>
                      <span class="err" v-if="istagclassify"><i class="iconfont icon-pc-paper-cancel"></i>必填项</span>
                    </el-form-item>
                    <el-form-item>
                      <span class="color-span" @click="openColorDialog" :style='{background:form.color}' ><i class="iconfont icon-nav-on-module"></i></span>
                      <el-input v-model='form.tagname' placeholder="请输入标签名字" maxlength="25"></el-input>
                      <span class="err" v-if="istagname" style="left: 84px;"><i class="iconfont icon-pc-paper-cancel"></i>必填项</span>
                    </el-form-item>
                  </el-form>
                  <div slot='footer' class='dialog-footer'>
                    <el-button type='primary' @click="createTag">确 定</el-button>
                    <el-button @click='createTagForm = false'>取 消</el-button>
                  </div>
                </el-dialog>

                <el-dialog title='选择颜色' :visible.sync='colordialog' class='colordialog'>
                  <div class="item" v-for="colors in colorData" :key="colors.id" :style='{background:colors.name}' @click="checkColor(colors)">
                    <i v-if="colors.checked" class="el-icon-check"></i>
                  </div>
                  <el-color-picker  @change="modelhange($event)">
                  </el-color-picker>
                  <div slot='footer' class='dialog-footer'>
                    <el-button type='primary' @click="primaryColor">确 定</el-button>
                    <el-button @click='colordialog = false'>取 消</el-button>
                  </div>
                </el-dialog>

                <el-dialog :title='classify == 1 ? "编辑分类":"添加分类"' :visible.sync='createClassifydialog' class='createClassifydialog add-dialog'>
                   <el-form :model='form'>
                    <el-form-item>
                      <el-input v-model='form.classifyname' placeholder="请输入" maxlength="25"></el-input>
                      <span class="err" v-if="isclassifyname"><i class="iconfont icon-pc-paper-cancel"></i>必填项</span>
                    </el-form-item>
                  </el-form>
                  <div slot='footer' class='dialog-footer'>
                    <el-button type='primary' @click="createClassify">确 定</el-button>
                    <el-button @click='createClassifydialog = false'>取 消</el-button>
                  </div>
                </el-dialog>

                <el-dialog title='分类' :visible.sync='tagclassifydialog' class='prompt-dialog'>
                  <div class="content">
                    <i class="iconfont icon-delete-tip"></i>
                    {{classify == 1 ? '确定要删除该标签吗？ ':'确定要删除该分类吗？ '}}
                  </div>
                  <div slot='footer' class='dialog-footer'>
                    <el-button type='primary' @click="tagclassifyPrinary">确 定</el-button>
                    <el-button @click='tagclassifydialog = false'>取 消</el-button>
                  </div>
                </el-dialog>

            </div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="任务自定义" name="third">
        <TaskCustom v-if="activeName === 'third'"></TaskCustom>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script>
import draggable from 'vuedraggable'
import {HTTPServer} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
import TaskCustom from '@/backend/module_setting/cooperation/task-custom'
import $ from 'jquery'
export default {
  name: 'CooperationBackend',
  components: {
    draggable,
    TaskCustom
  },
  data () {
    return {
      formLabelWidth: '84px',
      form: {},
      isname: '',
      classify: 0,
      istagclassify: '',
      istagname: '',
      isclassifyname: '',
      activeNames: [],
      list: [],
      outerVisible: false,
      outerVisible1: false,
      outerVisible2: false,
      title: '',
      title1: '',
      value: '',
      title2: '',
      labelName: '',
      input6: '',
      colorData: [{'id': 1, 'name': '#CD5D82', 'checked': true}, {'id': 2, 'name': '#D97978'}, {'id': 3, 'name': '#F88F41'}, {'id': 4, 'name': '#FABC01'}, {'id': 5, 'name': '#F9B239'}, {'id': 6, 'name': '#99D85B'}, {'id': 7, 'name': '#66C060'}, {'id': 8, 'name': '#4CAC68'}, {'id': 9, 'name': '#19BFA4'}, {'id': 10, 'name': '#45AC91'}, {'id': 11, 'name': '#70D6D7'}, {'id': 12, 'name': '#34A7B6'}, {'id': 13, 'name': '#2A8DB7'}, {'id': 14, 'name': '#487BCC'}, {'id': 15, 'name': '#4E8AF9'}, {'id': 16, 'name': '#7076FA'}, {'id': 17, 'name': '#8186EC'}, {'id': 18, 'name': '#8E75CB'}, {'id': 19, 'name': '#C472EE'}, {'id': 20, 'name': '#BC74CB'}, {'id': 21, 'name': '#EF7EDE'}, {'id': 22, 'name': '#F969AA'}, {'id': 23, 'name': '#FA5A55'}, {'id': 24, 'name': '#FF7748'}],
      colordialog: false,
      alternative: '',
      createClassifydialog: false,
      tagclassifydialog: false,
      name: '',
      describe: '',
      input5: '',
      searchTagName: '',
      deleteId: '',
      judgeData: {name: ''},
      editor1: false,
      activeName: 'first',
      addLabelName: '',
      editor2: false,
      checkedPermissions: [],
      checkAll: false,
      cityOptions: [],
      isIndeterminate: true,
      roleListContent: [],
      id: '',
      pageNum: 1,
      pageSize: 20,
      arrId: [],
      deleteData: false,
      judgeAdd1: false,
      judgeAdd2: false,
      editId: '',
      fatherId: '',
      fatherIndex: '',
      judgeAdd3: false,
      roleContent: true,
      judgeNumber: 0,
      personalPermissions: {},
      selectPermissionsId: '',
      selectAllPermissionsId: [],
      createTagForm: false
    }
  },
  mounted () {
    this.getListRoleMembers()
  },
  computed: {
    /** 父节点 */
    parentOption () {
      return {
        animation: 200,
        group: { name: 'parent', pull: true, put: true },
        sort: true,
        ghostClass: 'ghost'
      }
    },
    /** 子节点 */
    sonOption () {
      return {
        animation: 200,
        group: { name: 'son', pull: true, put: true },
        sort: true,
        ghostClass: 'ghost'
      }
    }
  },
  methods: {
    gotoBaseMoudel () { // 跳转到基础模块页面
      this.$router.push({path: '/backend/enterprise?fromPage=basemoudel'})
    },
    // 获取列表数据
    getListRoleMembers () {
      var jsondata = {'pageNum': this.pageNum, 'pageSize': this.pageSize}
      HTTPServer.getRoleMembersList(jsondata, 'Loading').then((res) => {
        this.roleListContent = res.dataList
      })
    },
    // 点击切换tab获取分类
    handleClick (tab) {
      this.judgeData.name = tab.name
      if (tab.name === 'first') {
        this.getListRoleMembers()
      } else if (tab.name === 'second') {
        this.getCooperationParentLabel()
      }
    },
    // 标签管理 - 获取标签分类
    getCooperationParentLabel (type) {
      var jsondata = {'type': 1, 'isTime': 0}
      this.searchTagName = ''
      HTTPServer.getCooperationParentLabel(jsondata, 'Loading').then((res) => {
        if (type) {
          res.map((item, index) => {
            var contains = tool.contains(this.list, 'id', item, 'id')
            if (!contains) {
              this.list.push(item)
            }
          })
        } else {
          this.activeNames = []
          this.list = res
        }
      })
    },
    // 标签管理 - 保存分类
    createClassify () {
      if (!this.form.classifyname) {
        this.isclassifyname = true
        return
      }
      this.isclassifyname = false
      if (this.classify === 0) {
        var jsondata = {'name': this.form.classifyname, 'type': 1}
        HTTPServer.newlyAddedSubclassification(jsondata, 'Loading').then((res) => {
          this.$message({
            message: '新增成功',
            type: 'success'
          })
          this.createClassifydialog = false
          this.getCooperationParentLabel(1)
        })
      } else {
        this.editLabel({'id': this.form.id, 'name': this.form.classifyname})
      }
    },
    // 标签管理 - 添加标签弹窗
    openTagForm (type) {
      this.classify = 0
      this.createTagForm = true
      this.form = {}
      this.istagclassify = false
      this.istagname = false
      this.colorData.map((item, index) => {
        this.colorData[index].checked = false
        if (item.id === 9) {
          this.colorData[index].checked = true
          this.form.color = item.name
        }
      })
    },
    // 标签管理 - 保存标签
    createTag () {
      if (!this.form.tagclassify) {
        this.istagclassify = true
        return
      }
      this.istagclassify = false
      if (!this.form.tagname) {
        this.istagname = true
        return
      }
      this.istagname = false
      if (this.classify === 0) {
        var jsondata = {'name': this.form.tagname, 'colour': this.form.color, 'parent_id': this.form.tagclassify, 'type': 2}
        console.log(jsondata)
        HTTPServer.newlyAddedSubclassification(jsondata, 'Loading').then((res) => {
          this.$message({
            message: '新增成功',
            type: 'success'
          })
          for (var index = 0; index < this.list.length; index++) {
            if (this.list[index].id === this.form.tagclassify) {
              this.getSubclassification({id: this.form.tagclassify}, index)
              break
            }
          }
          this.createTagForm = false
        })
      } else {
        console.log(this.form)
        this.editLabel({'id': this.form.id, 'name': this.form.tagname, 'colour': this.form.color})
      }
    },
    // 标签管理 - 获取分类下面的子分类
    getSubclassification (obj, index, event) {
      if (event) {
        if (event.target.className === 'delete' || event.target.className === 'edit') {
          return
        }
      }
      if (!obj.list || !event) {
        var jsondata = {'type': 2, 'id': obj.id, 'isTime': 0}
        HTTPServer.getCooperationParentLabel(jsondata, 'Loading').then((res) => {
          this.list[index].list = res
          this.$set(this.list, index, this.list[index])
          this.judgeNumber = 1
          this.arrId.push(obj.id)
          if (event) {
            setTimeout(() => {
              $(event.target).parents('.item').find('.content').slideDown(100)
            }, 100)
            this.activeNames.push(obj.id)
          }
          console.log(this.activeNames)
        })
      } else {
        var indexs = this.activeNames.indexOf(obj.id)
        this.activeNames.splice(indexs, 1)
        delete obj.list
        console.log(this.activeNames)
      }
    },
    /** 标签管理 - 打开颜色窗口 */
    openColorDialog () {
      this.colordialog = true
    },
    /** 标签管理 - 颜色选择器 */
    modelhange (data) {
      console.log('颜色选择器', data)
      this.alternative = data
      this.colorData.map((item, index) => {
        item.checked = false
        this.$set(this.colorData, index, item)
      })
    },
    /** 标签管理 - 选择颜色 */
    checkColor (data) {
      this.colorData.map((item, index) => {
        item.checked = false
        if (item.name === data.name) {
          item.checked = true
        }
        this.$set(this.colorData, index, item)
      })
      console.log(this.colorData)
    },
    /** 标签管理 - 确认颜色 */
    primaryColor () {
      var contains = tool.contains(this.colorData, 'checked', true)
      if (contains) {
        this.form.color = contains.name
      } else {
        this.form.color = JSON.parse(JSON.stringify(this.alternative))
      }
      console.log('颜色选择器', this.form)
      this.colordialog = false
    },
    // 标签管理 - 编辑分类弹窗
    fatherClassification (content) {
      this.classify = 1
      this.form.classifyname = content.name
      this.form.id = content.id
      this.isclassifyname = false
      this.createClassifydialog = true
    },
    // 标签管理 - 编辑分类、标签
    editLabel (jsondata) {
      HTTPServer.editLabel(jsondata, 'Loading').then((res) => {
        this.$message({
          message: '编辑成功',
          type: 'success'
        })
        this.createTagForm = false
        this.initCollapse(jsondata, 1)
        this.createClassifydialog = false
      })
    },
    /** 标签管理 - 初始化分类、标签 */
    initCollapse (data, type) {
      var fun = function (arr) {
        for (var index = 0; index < arr.length; index++) {
          if (arr[index].id === data.id) {
            if (type === 0) {
              arr.splice(index, 1)
            } else {
              arr[index].name = data.name
              arr[index].colour = data.colour
            }
            break
          }
          if (arr[index].list) fun(arr[index].list)
        }
      }
      fun(this.list)
    },
    // 标签管理 - 打开编辑标签
    sonClassification (content, id, index) {
      this.classify = 1
      console.log(content, id, index)
      this.form.tagname = content.name
      this.form.id = content.id
      this.form.tagclassify = id
      this.form.color = content.colour
      this.istagname = false
      this.istagclassify = false
      this.createTagForm = true
    },
    // 标签管理 - 删除分类、标签 弹窗
    deleteTag (data, type) {
      this.tagclassifydialog = true
      this.form.id = data.id
      this.classify = type
    },
    // 标签管理 - 删除分类、标签
    tagclassifyPrinary () {
      var jsondata = {'id': this.form.id}
      HTTPServer.deleteLabel(jsondata, 'Loading').then((res) => {
        this.$message({
          message: '删除成功',
          type: 'success'
        })
        this.tagclassifydialog = false
        this.initCollapse(jsondata, 0)
      })
    },
    // 全选权限
    handleCheckAllChange (val) {
      var dataId = []
      if (val) {
        this.cityOptions.map((item) => {
          item.sub.map((item2) => {
            dataId.push(Number(item2.id))
          })
        })
      }
      this.checkedPermissions = val ? dataId : []
      this.isIndeterminate = false
      this.selectPermissionsId = JSON.parse(JSON.stringify(this.checkedPermissions))
      console.log(this.selectPermissionsId)
    },
    // 单选某个权限
    handleCheckedPermissionsChange (value) {
      console.log(value)
      var total = 0
      this.cityOptions.map((item) => {
        item.sub.map((item2) => {
          total++
        })
      })
      console.log(value.length, this.selectPermissionsId.length)
      /** 控制移动分组和移动列表的权限同时联动 **/
      if (value.length > this.selectPermissionsId.length) {
        if (value.includes(18) && !value.includes(22)) {
          value.push(22)
        }
        if (!value.includes(18) && value.includes(22)) {
          value.push(18)
        }
      } else if (value.length < this.selectPermissionsId.length) {
        if (value.includes(18) && !value.includes(22)) {
          value.splice(value.indexOf(18), 1)
        }
        if (value.includes(22) && !value.includes(18)) {
          value.splice(value.indexOf(22), 1)
        }
      }
      /** 控制移动分组和移动列表的权限同时联动 **/
      var checkedCount = value.length
      this.selectPermissionsId = JSON.parse(JSON.stringify(value))
      this.checkAll = checkedCount === total
      this.isIndeterminate = checkedCount > 0 && checkedCount < this.cityOptions.length
      console.log(value)
    },
    // 添加角色权限
    addRolePermissions () {
      this.isname = ''
      this.editor1 = true
      this.editor2 = false
      this.deleteData = false
      this.roleContent = true
      this.title = '添加角色权限'
      HTTPServer.getAllPermissions({}, 'Loading').then((res) => {
        this.cityOptions = res
        this.outerVisible = true
        this.name = ''
        this.describe = ''
        this.checkedPermissions = []
      })
    },
    // 编辑角色权限
    editorRolePermissions (Role) {
      this.isname = ''
      this.editor1 = false
      this.deleteData = false
      this.editor2 = true
      this.roleContent = true
      this.title = '编辑角色权限'
      this.personalPermissions = Role
      this.id = Role.id
      var PermissionsId = Role.priviledge_ids.split(',')
      this.name = this.personalPermissions.name
      this.describe = this.personalPermissions.describle
      HTTPServer.getAllPermissions({}, 'Loading').then((res) => {
        this.cityOptions = res
        for (var i = 0; i < PermissionsId.length; i++) {
          PermissionsId[i] = Number(PermissionsId[i])
        }
        var le = 0
        res.map((item) => {
          le += item.sub.length
        })
        this.checkAll = (PermissionsId.length === le && le !== 0)
        console.log(PermissionsId, le)
        this.checkedPermissions = PermissionsId
        this.outerVisible = true
      })
    },
    // 新增的确定按钮
    addDetermine () {
      if (!this.name) {
        this.isname = '角色名称不能为空'
        return
      }
      this.isname = ''

      var jsondata = {
        'name': this.name,
        'describle': this.describe,
        'priviledge_ids': String(this.selectPermissionsId)
      }
      HTTPServer.backendAddRolePermissions(jsondata, 'Loading').then((res) => {
        this.$message({
          message: '新增角色成功',
          type: 'success'
        })
        this.getListRoleMembers()
        this.outerVisible = false
      })
    },
    // 编辑的确定按钮
    editorDetermine () {
      if (!this.name) {
        this.isname = '角色名称不能为空'
        return
      }
      this.isname = ''
      var jsondata = {
        'id': this.personalPermissions.id,
        'name': this.name,
        'describle': this.describe,
        'priviledge_ids': String(this.selectPermissionsId)
      }
      HTTPServer.backendEditorRolePermissions(jsondata, 'Loading').then((res) => {
        this.$message({
          message: '编辑角色成功',
          type: 'success'
        })
        this.getListRoleMembers()
        this.outerVisible = false
      })
    },
    // 删除角色
    deleteRole (id) {
      this.title = '提示'
      this.id = id
      this.editor1 = false
      this.editor2 = false
      this.outerVisible = true
      this.roleContent = false
      this.deleteData = true
    },
    // 删除的确定
    deleteConfirm () {
      HTTPServer.deleteRole({'id': this.id}, 'Loading').then((res) => {
        this.$message({
          message: '删除成功',
          type: 'success'
        })
        this.getListRoleMembers()
        this.outerVisible = false
      })
    },
    //  标签管理 - 项目标签的搜索
    searchTag () {
      this.searchTagName = JSON.parse(JSON.stringify(this.input5))
      // if (this.input5 !== '') {
      //   var jsondata = {'content': this.input5, 'pageNum': this.pageNum, 'pageSize': this.pageSize}
      //   HTTPServer.SearchLabelContent(jsondata, 'Loading').then((res) => {
      //     console.log(res)
      //     this.input5 = ''
      //   })
      // } else {
      //   this.$message.error('搜索内容不能为空')
      // }
    },
    // 按时间顺序排序
    TimeAscending (judge, id, index) {
      if (judge === 1) {
        var jsondata = {'type': 1, 'isTime': 1}
        HTTPServer.getCooperationParentLabel(jsondata, 'Loading').then((res) => {
          this.list = res
        })
      } else {
        var jsondata1 = {'type': 2, 'id': id, 'isTime': 1}
        HTTPServer.getCooperationParentLabel(jsondata1, 'Loading').then((res) => {
          this.list[index].list = res
          this.$set(this.list, index, this.list[index])
        })
      }
    },
    // 拖拽改变顺序
    changeOrderFn (jsondata) {
      HTTPServer.changeOrder(jsondata, 'Loding').then((res) => {
      })
    },
    // 新增分类的确定按钮
    labelDetermine1 () {
      if (this.addLabelName !== '') {
        var jsondata = {'name': this.addLabelName, 'type': 1}
        HTTPServer.newlyAddedSubclassification(jsondata, 'Loading').then((res) => {
          this.$message({
            message: '新增子分类成功',
            type: 'success'
          })
          this.outerVisible1 = false
          this.getCooperationParentLabel(1)
        })
      } else {
        this.$message.error('项目标签名不能为空')
      }
    },
    handleChange (val) {
      console.log(val)
    },
    updata1 (e, list) {
      this.changeOrderFn(this.list)
      console.log(list, 1111)
    },
    updata2 (index, list) {
      console.log(list, 2222)
      this.list[index].list = list
      this.$set(this.list, index, this.list[index])
      this.changeOrderFn(this.list)
    },
    changedata1 (e, list) {
      console.log(list, 3333)
      this.changeOrderFn(this.list)
    },
    changedata2 (index, list) {
      console.log(list, 4444)
      this.list[index].list = list
      this.$set(this.list, index, this.list[index])
      this.changeOrderFn(this.list)
    },
    empty (list, index) {
      if (list && list.length === 0) {
        list.push({name: '', list: []})
      }
      this.list[index].list = list
      this.changeOrderFn(this.list)
      // this.getSubclassification()
      console.log(this.list, 5555)
    }
  }
}
</script>
<style lang="scss" scoped>
.cooperation-backend-wrap{
  padding: 0 25px 0 30px;
  height: 100%;
  .projectJurisdiction{
    .moduleTitle{
      >p{
        color: #999;
        line-height: 20px;
        .name{
          font-size: 16px;
          color: #333;
          margin-right: 10px;
        }
      }
      .el-button{
        padding: 9px 13px 6px;
        margin: 20px 0;
        span{
          font-size: 18px;
          float: left;
          margin: -2px 13px 0 0px;
        }
      }
    }
    .moduleList{
      .textContent{
        overflow: hidden;
        text-overflow:ellipsis;
        white-space: nowrap;
      }
      i{
        cursor: pointer;
      }
    }
  }
  .projectLabel{
    .moduleTitle{
      >p{
        color: #999;
        line-height: 20px;
        .name{
          font-size: 16px;
          color: #333;
          margin-right: 10px;
        }
      }
      .el-button--primary{
        padding: 9px 13px 6px;
        margin: 20px 0;
        span{
          font-size: 18px;
          float: left;
          margin: -2px 13px 0 0px;
        }
      }
    }
  }
}
.cooperation-backend-wrap.backend_main{
  >div.header.workbench-title-new{
    height:90px;line-height: 90px;display: flex;
    .worktable-icon{
      width:50px;height:50px;line-height: 50px;text-align: center;border-radius: 5px;background: #FECCD7;margin:20px 10px 0 0;
      >i{
        color:#FC587B;font-size:40px;
      }
    }
    >div{
      >p{line-height: 25px;}padding-top:22px;
      >p:nth-child(1){font-size:16px;}
      >p:nth-child(2){font-size:14px;color:#A0A0AE;}
    }
  }
}
</style>
<style lang="scss">
.cooperation-backend-wrap {
  .el-tabs {
    height: calc(100% - 150px);
    .el-tabs__content {
      height: calc(100% - 70px);
      #pane-third {
        height: 100%;
      }
    }
    .el-tabs__nav-scroll{
      padding-left: 8px;
    }
    .el-tabs__nav-wrap::after{
      height: 1px;
    }
    .el-tabs__item{
      height: 55px;
      line-height: 55px;
    }
    .accordion{
      margin-top: 10px;
      .el-collapse{border-top: 0;}
      .el-collapse-item__content{padding: 0 0 0 20px;
        .tab-name{padding: 1px 6px;border-radius: 3px;color: #fff;}
      }
    }
  }
  .moduleList{
    .edit{
      font-size: 14px;
      color: #1890FF;
      margin-right: 10px;
    }
    .edit.gray{
      color: #ccc;
    }
    .delete{
      font-size: 13px;
      color: #FF4949;
    }
  }
  .must{
    .el-form-item__label::before{content: "*";display: inline-block;color: red;line-height: 1;margin: 0 5px 0 0;}
  }
  .promptDialogBox{
    .projectRole{
      .el-dialog{
        width: 540px;
        .el-dialog__header{
          padding: 16px 20px 10px;
          border-bottom: 1px solid #e5e5e5;
          .el-dialog__title{
            font-size: 16px;
          }
        }
        .el-dialog__body{
          padding: 20px;
          .el-form-item{
            margin-bottom: 20px;
          }
          .box2{
            .role-header{
              margin-bottom: 20px;
              padding: 0 0 10px 15px;
              border-bottom: 1px solid #e5e5e5;
            }
            .item{
              line-height: 20px;
              .title{
                float: left;
                width: 93px;
                text-align: left;
                padding-left: 15px;
              }
              .el-checkbox-group{
                margin-left: 83px;
                overflow: hidden;
                .el-checkbox{
                  float: left;
                  margin: 0 20px 15px 0;
                  line-height: 1;
                }
              }
            }
          }
          // .el-checkbox-group{
          //   .el-checkbox{
          //     min-width: 110px;
          //   }
          //   .el-checkbox:first-child{
          //     margin-left: 30px;
          //   }
          // }
        }
        .el-dialog__footer{
          padding: 10px 20px 10px;
          border-top: 1px solid #e5e5e5;
          .el-button{
            padding: 8px 18px;
          }
        }
      }
    }
  }
  .projectLabel{
    .input-with-select{
      height: 32px;
      .el-input__inner{
        height: 32px;
      }
      .el-input__icon{
        line-height: 32px;
      }
    }
    .accordion{
      width: 70%;
      min-width: 400px;
      .icon-yidong{
        font-size: 24px;
        color: #BBBBC3;
        float: left;
        margin: 12px 0 0 0;
        line-height: 1;
      }
      .el-collapse-item__arrow{
        display: none;
      }
      .el-collapse-item__header{
        height: 46px;
        line-height: 46px;
      }
      .el-collapse-item__header.is-active{
        background: #FAFAFA;
      }
      .edit{
        float: right;
        color: #20A0FF;
        margin-right: 16px;
      }
      .delete{
        float: right;
        color: #FF4949;
      }
      .el-icon-edit, .el-icon-delete{
        font-size: 16px;
        color: #BBBBC3;
        float: right;
        margin-top: 15px;
      }
      .el-icon-edit{
        margin-right: 30px;
      }
      .collapse2{
        color:#000;
        overflow: hidden;
        height: 46px;
        line-height: 47px;
        border-bottom: 1px solid #e5e5e5;
      }
      .collapse2:last-child{
        margin-bottom: -1px;
      }
      .item{
        line-height: 46px;
        .item-title{
          height: 46px;
          border-bottom: 1px solid #e5e5e5;
          padding-right: 20px;
        }
        .content{
          display: none;
          min-height: 30px;
          .item-content{
            height: 46px;
            border-bottom: 1px solid #e5e5e5;
            padding-right: 20px;
          }
          .item-content:last-child{
            margin-bottom: -1px;
          }
        }
        .tab-name{
          border-radius: 4px;
          padding: 2px 12px;
        }
      }
      .item.is-current{
        .item-title{
          background: #FAFAFA;
          border-bottom: none;
        }
        .el-icon-caret-right {
          transform: rotate(90deg);
        }
        .content{
          display: block;
        }
      }
    }
  }
  .createTagForm{
    .el-select{
      height: 36px;
      .el-input{
        height: 36px;
        line-height: 36px;
        input{
          height: 36px;
          line-height: 36px;
          border-top-right-radius: 0;
          border-bottom-right-radius: 0;
        }
      }
    }
    .el-button{
      padding: 8px 13px;
    }
    .add-classify{
      padding: 10px 22px 8px;
      border-top-left-radius: 0;
      border-bottom-left-radius: 0;
      margin: 0 0 0 -6px;
      span{
        font-size: 18px;
        float: left;
        margin: -2px 13px 0 0px;
      }
    }
    .color-span{
      float: left;
      background: #19BFA4;
      color: #fff;
      padding: 11px 17px 9px;
      line-height: 1;
      margin: 2px 0 0 35px;
    }
    .color-span+.el-input{
      width: 380px;
      height: 36px;
      line-height: 36px;
      input{
        width: 100%;
        height: 36px;
        line-height: 36px;
        border-top-left-radius: 0;
        border-bottom-left-radius: 0;
      }
    }
  }
  .colordialog{
    .el-dialog {
      width: 300px;
      .el-dialog__header{
        display: none;
      }
      .el-dialog__body{
        padding: 10px 0 0 8px;
        .item{
          width: 30px;
          height: 30px;
          display: inline-block;
          float: left;
          margin: 5px 0 0 5px;
          cursor: pointer;
          i{
            float: left;
            margin: 7px 0 0 6px;
            color: #fff;
            font-size: 17px;
          }
        }
        .el-color-picker__trigger{
          border: none;
        }
      }
      .el-dialog__footer{
        padding: 10px 20px 10px;
        border-top: 1px solid #e5e5e5;
        .el-button{
          padding: 8px 18px;
        }
      }
    }
  }
  .createClassifydialog{
    .el-dialog{
      width: 400px;
      .el-dialog__body{
        padding: 20px;
      }
    }
  }
}
</style>
