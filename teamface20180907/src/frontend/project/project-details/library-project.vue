<template>
  <el-container class="main-project-library-wrap">
    <div class="project-library-left">
      <div class="header">文库
        <i v-if="priviledge_ids.includes(19) && projectData.project_status == 0" class="iconfont icon-renwuxinzeng" @click="addfolderForm = true;setObject = {};form = {};setType = 1;isFolderName = false;"></i>
      </div>
      <div class="seach">
        <el-input v-model="seachValue" prefix-icon="el-icon-search" placeholder="请输入内容" @keyup.enter.native="searchFile"></el-input>
      </div>
      <div class="folder-tree">
        <!-- :default-checked-keys="[defaultCheckedKeys]" -->
        <!-- <el-tree :data='treeData' :props='defaultProps' @node-click='classifyCheck' node-key="id" :render-content="renderContent" ref="tree"
                :default-expanded-keys="[1]" :expand-on-click-node='false'></el-tree> -->
        <div class="custom-tree">
          <div class="tree-item" v-for="(item, index) in treeData" :key="index">
            <div class="tree-content" @click="classifyCheck(item, $event, 0)" :id="'projectTree' + index"><i class="el-icon-caret-right"></i><span class="name">{{item.file_name}}</span><i class="iconfont icon-pc-paper-admini2" v-if="item.library_type == 1 && projectData.project_status == 0"></i></div>
            <div class="tree-child">
              <div class="tree-item" v-for="(child, index2) in item.children" :key="index2">
                <div class="tree-content" :class="(curObject.data_id == child.data_id) ? 'is-current' : ''" @click="classifyCheck(child, $event, 1)">
                  <span class="name">{{child.file_name}}</span><i class="iconfont icon-project-more" v-if="item.library_type == 1 && projectData.project_status == 0"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <input type="file" id="updateTaskFile" @change="updateTaskFile($event)" />
    </div>
    <div class="project-library-right">
      <v-table v-if="fileList.length > 0" is-horizontal-resize is-vertical-resize="" :vertical-resize-offset='5' style="width: 100%;" :height="tableHeight" :columns="columns" :table-data="fileList" :row-click="rowClick"></v-table>
      <div class="no-data" v-if="fileList.length == 0">
        <img src="/static/img/no-data.png" />
        <p v-if="keyWord">无数据</p>
        <p v-if="!keyWord">您还没有添加文件</p>
        <p v-if="!keyWord && curObject.id && curObject.library_type == 1 && curObject.create_by == employeeInfo.id && projectData.project_status == 0"><a href="javascript:;" @click="folderSetCheck(5)">现在就添加</a></p>
      </div>
      <el-pagination v-if="fileList.length > 0" @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum'
          :page-sizes='[10, 20, 50, 100]' :page-size='pageSize' layout='total, sizes, prev, pager, next, jumper' :total='tableTotal'></el-pagination>
    </div>
    <div class="height: 0;">
      <el-dialog :title='(setType == 1) ? "添加文件夹":"编辑文件夹"' :visible.sync='addfolderForm' class='addfolderForm add-dialog' :close-on-click-modal='false'>
        <el-form :model='form'>
          <el-form-item label='文件夹名称' :label-width='formLabelWidth' class="must">
            <el-input v-model='form.name' :maxlength='25'></el-input>
            <span class="err" v-if="isFolderName"><i class="iconfont icon-pc-paper-cancel"></i>{{isFolderName}}</span>
          </el-form-item>
        </el-form>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='addfolder'>确 定</el-button>
          <el-button @click='addfolderForm = false'>取 消</el-button>
        </div>
      </el-dialog>

      <el-dialog title='共享文件' :visible.sync='sharedfileForm' class='sharedfolderForm'>
        <div style="font-size: 16px;color: #323232;">共享给</div>
        <div>
            <div v-for="shared in sharedMemberData" :key="shared.id" class="item">
              <span v-if="!shared.picture" class="simpName">{{shared.name | limitTo(-2)}}</span>
              <img v-if="shared.picture" :src="shared.picture + '&TOKEN=' + token" :title="shared.name" />
            </div>
            <i class="iconfont icon-xinzengrenyuan" @click="addFolderManage(sharedMemberData)"></i>
        </div>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='shareFileLibaray'>确 定</el-button>
          <el-button @click='sharedfileForm = false'>取 消</el-button>
        </div>
      </el-dialog>

      <el-dialog title="文件夹设置" :visible.sync="folderSetForm" class='folderSetForm' :modal='false' top="0">
        <div class="arrow"></div>
        <div class="item" @click="folderSetCheck(1)" v-if="!setObject.parent_data_id && priviledge_ids.includes(19)">添加子文件夹</div>
        <div class="item" @click="folderSetCheck(2)" v-if="setObject.parent_data_id && setObject.create_by == employeeInfo.id">
          添加文件
        </div>
        <div class="item" @click="folderSetCheck(3)" v-if="priviledge_ids.includes(20)">编辑文件夹</div>
        <div class="item" @click="folderSetCheck(4)" v-if="priviledge_ids.includes(21)">删除文件夹</div>
      </el-dialog>
      <el-dialog title='删除' :visible.sync='deleteFolderForm' class='prompt-dialog'>
        <div class="content"><i class="iconfont icon-delete-tip"></i>确定要删除该文件夹吗？</div>
        <!-- <div class="prompt">你确定要删除吗？</div> -->
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='deleteFolder'>确 定</el-button>
          <el-button @click='deleteFolderForm = false'>取 消</el-button>
        </div>
      </el-dialog>
      <el-dialog title='删除' :visible.sync='deleteFileForm' class='prompt-dialog'>
        <div class="content"><i class="iconfont icon-delete-tip"></i>确定要删除该文件吗？</div>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='deleteFile'>确 定</el-button>
          <el-button @click='deleteFileForm = false'>取 消</el-button>
        </div>
      </el-dialog>
    </div>
  </el-container>
</template>
<script>
import Vue from 'vue'
import tool from '@/common/js/tool.js'
import {HTTPServer, baseURL, ajaxGetRequest} from '@/common/js/ajax.js'
import $ from 'jquery'
export default {
  props: ['projectId'],
  data () {
    return {
      getProjectId: this.projectId,
      seachValue: '',
      keyWord: '',
      token: (JSON.parse((sessionStorage.requestHeader))).TOKEN,
      treeData: [],
      defaultCheckedKeys: 5,
      defaultProps: {
        children: 'children',
        label: 'file_name'
      },
      formLabelWidth: '97px',
      addfolderForm: false,
      isFolderName: false,
      form: {},
      curObject: {},
      setObject: {},
      projectData: {},
      employeeInfo: {},
      priviledge_ids: '',
      setType: 0,
      folderSetForm: false,
      sharedfileForm: false,
      sharedMemberData: [],
      columns: [{'field': 'file_name', 'title': '文件名称', 'width': 280, 'titleAlign': 'left', 'columnAlign': 'left', 'isResize': true}, {'field': 'file_type', 'title': '文件类型', 'width': 90, 'titleAlign': 'left', 'columnAlign': 'left', 'isResize': true}, {'field': 'file_size', 'title': '文件大小', 'width': 90, 'titleAlign': 'left', 'columnAlign': 'left', 'isResize': true}, {'field': 'employee_name', 'title': '上传人', 'width': 100, 'titleAlign': 'left', 'columnAlign': 'left', 'isResize': true}, {'field': 'create_time', 'title': '上传时间', 'width': 120, 'titleAlign': 'left', 'columnAlign': 'left', 'isResize': true}, {field: 'custome-adv', title: '操作', width: 120, titleAlign: 'left', columnAlign: 'left', componentName: 'table-operation', isResize: true}],
      fileList: [],
      deleteFolderForm: false,
      deleteFileForm: false,
      pageNum: 1,
      pageSize: 20,
      tableHeight: 810,
      tableTotal: 0
    }
  },
  watch: {
    projectId (value) {
      console.log(value, '000000')
      this.getProjectId = value
    }
  },
  mounted () {
    var userInfo = JSON.parse(sessionStorage.userInfo)
    this.employeeInfo = userInfo.employeeInfo
    this.getWorkbenchDetail()
    this.projectQueryById(1)
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      console.log(1111, value)
      if (value) {
        if (value.prepareKey === 'project-library-shared-member') {
          this.sharedMemberData = value.prepareData
        }
      }
    })
    /** 项目切换 */
    this.$bus.off('changeProjectId')
    this.$bus.on('changeProjectId', (value) => {
      console.log(value)
      this.getProjectId = value
      this.treeData = []
      this.fileList = []
      this.queryProjectLibraryList()
    })
    /** 行操作-接收 */
    this.$bus.off('project-library-handle')
    this.$bus.on('project-library-handle', (value) => {
      if (value) {
        // console.log(value)
        if (value.type === 0) {
          console.log('下载', value)
          this.saveFile(value)
        } else if (value.type === 1) {
          console.log('分享', value)
          this.setObject = value.rowData
          this.sharedfileForm = true
          this.sharedMemberData = []
        } else {
          console.log('删除', value)
          this.deleteFileForm = true
          this.setObject = value.rowData
        }
      }
    })
    /** 项目操作(状态) */
    this.$bus.off('projectStatusChange')
    this.$bus.on('projectStatusChange', (value) => {
      this.projectQueryById()
    })
  },
  methods: {
    /** 获取到的工作台详情 */
    getWorkbenchDetail () {
      HTTPServer.queryManagementRoleInfo({'eid': this.employeeInfo.id, 'projectId': this.projectId}, true).then((res) => {
        console.log(res, '角色权限')
        this.priviledge_ids = res.priviledge.priviledge_ids
      })
    },
    /** 获取到的项目详情 */
    projectQueryById (type) {
      HTTPServer.projectQueryById({'id': this.projectId}, true).then((res) => {
        console.log(res, '获取到的工作台详情')
        this.projectData = res
        if (type) {
          this.queryProjectLibraryList()
        }
      })
    },
    /** 获取文件夹列表 */
    queryProjectLibraryList (type) {
      HTTPServer.queryProjectLibraryList({'id': this.getProjectId, 'pageSize': 9999, 'pageNum': 1}, 'Loading').then((res) => {
        if (type) {
          res.dataList.map((item, index) => {
            var contains = tool.contains(this.treeData, 'data_id', item, 'data_id')
            if (!contains) {
              this.treeData.push(item)
            }
          })
        } else {
          this.treeData = res.dataList
          setTimeout(() => {
            if (this.treeData.length > 0) this.queryProjectLibraryList2(this.treeData[0], document.getElementById('projectTree0'), 1)
          }, 200)
        }
      })
    },
    /** 获取子文件夹列表 */
    queryProjectLibraryList2 (data, event, type) {
      let id = data.parent_data_id || data.data_id
      HTTPServer.queryProjectFileLibraryList({'id': id, 'library_type': data.library_type, 'pageSize': 9999, 'pageNum': 1}, 'Loading').then((res) => {
        res.dataList.map((item, index) => {
          res.dataList[index].parent_data_id = id
        })
        var contains = tool.contains(this.treeData, 'data_id', id)
        console.log(contains)
        if (contains) {
          if (event) {
            this.treeData[contains.i].children = res.dataList
            this.$set(this.treeData, contains.i, this.treeData[contains.i])
            $(event).parent('.tree-item').addClass('expanded')
            setTimeout(() => {
              $(event).siblings('.tree-child').slideDown(200)
            }, 200)
            if (type === 1 && res.dataList.length > 0) {
              this.curObject = res.dataList[0]
              this.queryTaskLibraryList()
            }
          } else {
            this.treeData[contains.i].children = res.dataList
            this.$set(this.treeData, contains.i, this.treeData[contains.i])
            if (this.curObject.data_id === this.setObject.data_id) {
              this.curObject = this.treeData[0].children[0]
              this.pageSize = 20
              this.pageNum = 1
              this.keyWord = false
              this.queryTaskLibraryList()
            }
          }
        }
      })
    },
    /** 选择文件夹 */
    classifyCheck (data, event, type) {
      var className = event.target.className
      if (className.indexOf('iconfont') >= 0 || className.indexOf('el-dropdown') >= 0) {
        this.folderSet(event)
        this.setObject = data
        return
      }
      // /** 判断是否组 */
      if (type === 0) {
        className = event.currentTarget.parentNode.className
        if (className.indexOf('expanded') >= 0) {
          $(event.currentTarget).parent('.tree-item').removeClass('expanded')
          $(event.currentTarget).siblings('.tree-child').slideUp(10)
        } else {
          this.queryProjectLibraryList2(data, event.currentTarget)
        }
      } else {
        this.curObject = data
        this.keyWord = false
        this.pageSize = 20
        this.pageNum = 1
        this.queryTaskLibraryList()
      }
    },
    /** 附件列表 */
    queryTaskLibraryList () {
      if (!this.curObject.data_id) {
        return
      }
      this.tableHeight = document.getElementsByClassName('project-library-right')[0].clientHeight - 60
      var jsondata = {'id': this.curObject.data_id, 'library_type': this.curObject.library_type, 'keyWord': this.keyWord ? this.seachValue : '', 'pageSize': this.pageSize, 'pageNum': this.pageNum}
      if (this.keyWord) {
        delete jsondata.id
        delete jsondata.library_type
      }
      HTTPServer.queryTaskLibraryList(jsondata, 'Loading').then((res) => {
        var funtype = function (type) {
          if (type === 'img') {
            return '图片'
          } if (type === 'video') {
            return '视频'
          } if (type === 'voice') {
            return '音频'
          } else {
            return '文档'
          }
        }
        var fileSize = (value, empty) => {
          value = parseInt(value)
          let kb = value / 1024
          let mb = value / 1024 / 1024
          let gb = value / 1024 / 1024 / 1024
          if (!gb) {
            return empty
          } else if (gb > 1024) {
            return parseInt(gb / 1024) + 'T'
          } else if (mb > 1024) {
            return parseInt(mb / 1024) + 'G'
          } else if (kb > 1024) {
            return parseInt(kb / 1024) + 'M'
          } else if (value > 1024) {
            return parseInt(kb) + 'kb'
          } else {
            return '1kb'
          }
        }
        res.dataList.map((item, idx) => {
          item.create_time = tool.formatDate(item.create_time, 'yyyy-MM-dd HH:mm')
          item.file_type = funtype(tool.determineFileType(item.suffix).fileType)
          item.file_size = fileSize(item.size)
          item.isDel = (this.employeeInfo.id === this.projectData.leader || this.employeeInfo.id === item.create_by)
        })
        this.fileList = res.dataList
        console.log(this.fileList)
        this.tableTotal = res.pageInfo.totalRows
      })
    },
    /** 搜索文件夹 */
    searchFile () {
      this.pageSize = 20
      this.pageNum = 1
      this.keyWord = true
      this.queryTaskLibraryList()
    },
    /** 设置窗 */
    folderSet (event) {
      console.log(event)
      this.folderSetForm = true
      var atGroupStyle = document.getElementsByClassName('folderSetForm')[0].childNodes[0].style
      atGroupStyle.top = tool.getTop(event.target) + 45 + 'px'
      atGroupStyle.left = tool.getLeft(event.target) - 95 + 'px'
    },
    /** 文件夹设置选择 */
    folderSetCheck (type) {
      this.setType = type
      this.isFolderName = false
      if (type === 1) {
        this.addfolderForm = true
        this.form = {}
      } else if (type === 2) {
        $('#updateTaskFile').click()
      } else if (type === 3) {
        this.addfolderForm = true
        this.form.name = this.setObject.file_name
      } else if (type === 4) {
        this.deleteFolderForm = true
      } else if (type === 5) {
        $('#updateTaskFile').click()
      }
      setTimeout(() => {
        this.folderSetForm = false
      }, 10)
    },
    /** 上传文件 */
    updateTaskFile (event) {
      setTimeout(() => {
        console.log(event)
        var data = (this.setType === 2) ? this.setObject : this.curObject
        var formdata = new FormData()
        formdata.append('userlogo', event.target.files[0])
        formdata.append('id', data.data_id)
        formdata.append('parent_id', data.parent_data_id)
        formdata.append('bean', 'project')
        console.log(formdata)
        event.target.value = ''
        // uploadForMemo projectPersonalUpload
        HTTPServer.projectPersonalUpload(formdata, 'Loading').then((res) => {
          this.queryTaskLibraryList()
        })
      }, 200)
    },
    /** 选人组件 */
    addFolderManage (data) {
      this.$bus.emit('commonMember', {'prepareData': data || [], 'prepareKey': 'project-library-shared-member', 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': []}) // 给父组件传值
    },
    /** 添加、编辑文件夹 */
    addfolder () {
      if (!this.form.name) {
        this.isFolderName = '必填项'
        return
      }
      if (this.form.name.length > 25) {
        this.isFolderName = '25字以内'
        return
      }
      this.isFolderName = false
      var jsondata
      if (this.setType === 1) {
        console.log(this.setObject)
        var isadd = !!this.setObject.id
        jsondata = {'name': this.form.name, 'parent_id': isadd ? this.setObject.data_id : this.getProjectId, 'type': isadd ? 1 : 0, 'project_id': this.getProjectId}
        console.log(jsondata)
        HTTPServer.savaProjectFileLibrary(jsondata, 'Loading').then((res) => {
          this.addfolderForm = false
          if (isadd) {
            this.queryProjectLibraryList2(this.setObject)
          } else {
            this.queryProjectLibraryList(1)
          }
        })
      } else {
        jsondata = {'name': this.form.name, 'id': this.setObject.id, 'project_id': this.getProjectId}
        HTTPServer.editProjectLibrary(jsondata, 'Loading').then((res) => {
          this.addfolderForm = false
          if (this.setObject.parent_data_id) {
            this.queryProjectLibraryList2(this.setObject)
          } else {
            this.treeData.map((item, index) => {
              if (item.data_id === this.setObject.data_id) {
                this.treeData[index].file_name = this.form.name
                this.$set(this.treeData, index, this.treeData[index])
              }
            })
          }
        })
      }
    },
    /** 文件夹删除确认 */
    deleteFolder () {
      var jsondata = {'id': this.setObject.id, 'project_id': this.getProjectId, 'name': this.setObject.file_name}
      console.log(jsondata)
      HTTPServer.delProjectLibrary(jsondata, 'Loading').then((res) => {
        this.deleteFolderForm = false
        if (this.setObject.parent_data_id) {
          this.queryProjectLibraryList2(this.setObject)
        } else {
          this.treeData.map((item, index) => {
            if (item.data_id === this.setObject.data_id) {
              this.treeData.splice(index, 1)
            }
          })
        }
      })
    },
    /** 分享确认 */
    shareFileLibaray () {
      console.log('分享确认', this.sharedMemberData)
      var ids = []
      if (this.sharedMemberData.length === 0) {
        this.$message.error('分享成员不能为空！')
        return
      }
      this.sharedMemberData.map((item) => {
        ids.push(item.id)
      })
      var jsondata = {'id': this.setObject.id, 'employee_id': ids.toString(), 'project_id': this.getProjectId}
      HTTPServer.sharProjectLibrary(jsondata, 'Loading').then((res) => {
        this.sharedfileForm = false
      })
    },
    /** 文件删除确认 */
    deleteFile () {
      var jsondata = {'id': this.setObject.id, 'project_id': this.getProjectId}
      console.log(jsondata)
      HTTPServer.delProjectLibrary(jsondata, 'Loading').then((res) => {
        this.deleteFileForm = false
        this.queryTaskLibraryList()
      })
    },
    /** 显示每页条数 */
    handleSizeChange (val) {
      this.pageSize = val
      this.pageNum = 1
      this.queryTaskLibraryList()
    },
    /** 跳转第几页 */
    handleCurrentChange (val) {
      this.pageNum = val
      this.queryTaskLibraryList()
    },
    /** 保存文件 */
    saveFile (data) {
      var jsondata = {'id': data.rowData.id, 'project_id': this.getProjectId, 'TOKEN': this.token}
      ajaxGetRequest(jsondata, 'common/file/projectDownload')
        .then((response) => {
          console.log('文件库下载文件', response)
          var saveLink = document.createElementNS('http://www.w3.org/1999/xhtml', 'a')
          saveLink.href = baseURL + 'common/file/projectDownload?TOKEN=' + this.token + '&id=' + data.rowData.id + '&project_id=' + this.getProjectId
          saveLink.download = data.rowData.file_name
          var event = document.createEvent('MouseEvents')
          event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null)
          saveLink.dispatchEvent(event)
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 数据行点击 */
    rowClick (rowIndex, rowData, column) {
      console.log(rowIndex, rowData, column)
      if (column.field === 'file_name') {
        var data = {}
        data.fileForm = true
        data.file_name = rowData.file_name
        data.file_type = rowData.suffix
        data.file_size = rowData.size
        data.file_url = baseURL + 'common/file/projectDownload?TOKEN=' + this.token + '&id=' + rowData.id
        this.$bus.emit('file-preview', data)
      }
    }
  }
}
// 自定义列组件--- 表格操作项
Vue.component('table-operation', {
  template: `<span>
            <i class="iconfont icon-pc-paper-download operation" title="下载" @click.stop.prevent="handleRow(rowData, index, 0)"></i>
            <i class="iconfont icon-pc-paper-share operation" title="分享" @click.stop.prevent="handleRow(rowData, index, 1)"></i>
            <i class="iconfont icon-pc-delete operation" title="删除" @click.stop.prevent="handleRow(rowData, index, 2)" v-if="rowData.isDel"></i>
        </span>`,
  data () {
    return {
      roleId: 1
    }
  },
  props: {
    rowData: {
      type: Object
    },
    field: {
      type: String
    },
    index: {
      type: Number
    }
  },
  mounted () {
    console.log(22, this)
  },
  methods: {
    handleRow (row, index, type) {
      console.log(row, index, type)
      // 参数根据业务场景随意构造
      let params = {type: type, index: index, rowData: row}
      this.$bus.emit('project-library-handle', params)
    }
  }
})
</script>

<style lang="scss">
.main-project-library-wrap{
  height: 100%;
  padding: 10px;
  #updateTaskFile{width: 0;height: 0;display: none;}
  .project-library-left{
    width: 308px;
    float: left;
    background: #FAFAFA;
    .header{
      height: 63px;
      line-height: 64px;
      border-bottom: 1px solid #e9e9e9;
      padding-left: 10px;
      font-size: 20px;
      color: #323232;
      .iconfont{
        font-size: 30px;
        float: right;
        color: #333;
        line-height: 1;
        margin: 16px 20px 0 0;
        cursor: pointer;
      }
    }
    .seach{
      height: 63px;
      line-height: 64px;
      padding: 0 12px;
    }
    .folder-tree{
      height: calc(100% - 126px);
      overflow-y: auto;
      .el-tree{
        background: #fafafa;
      }
      .el-tree-node__content{
        height: 40px;
        margin-top: 1px;
        .el-tree-node__expand-icon{
          color: #666;
          font-size: 14px;
          margin-left: 15px;
        }
        .el-tree-node__expand-icon.is-leaf{
          // color: transparent;
        }
        .iconfont{margin-right: 10px;}
      }
      .el-tree-node__content:hover{
        background: #f2f2f2!important;
      }
      .is-current{
        >.el-tree-node__content{
          background: transparent;
        }
      }
      .is-checked{
        >.el-tree-node__content{
          background: #f2f2f2;
        }
      }
    }
  }
  .project-library-right{
    width: calc(100% - 328px);
    background: #fff;
    margin-left: 10px;
    table{
      .v-table-header-row{color: #000;}
      td:first-child{
        >div{
          padding-left: 20px;
        }
        >.v-table-body-cell{
          span{
            width: 100%;
            display: inline-block;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            line-height: 1.2;
            margin-top: 13px;
          }
        }
      }
      td{
        border-bottom: 1px solid #f2f2f2;
        line-height: 50px;
        height: 50px;
        .operation{
          font-size: 18px;
          margin-right: 20px;
        }
      }
    }
    .no-data{text-align: center;
      img{margin-top: 250px;width: 200px;}
      p{
        font-size: 18px;
        color: #909090;
        a{
          font-size: 18px;
          color: #3689E9;
          letter-spacing: 0.43px;
        }
      }
    }
    .el-pagination{text-align: right;padding: 16px 20px;border-top: 1px solid #f2f2f2;}
  }

  .addfolderForm{
    .el-dialog{width: 600px;}
    .color-box{
      .color-item{display: inline-block;width: 24px;height: 24px;margin: 5px 10px 0 0;border-radius: 3px;}
    }
    .colorbox{
      .el-form-item__label{width: 92px!important;}
    }
    .el-row{width: calc(100% - 15px);
      .el-col{width: 34px;padding: 5px!important;
        .grid-content{height: 24px;background: red;border-radius: 3px;cursor: pointer;}
        .grid-content{
          i{color: #fff;font-size: 16px;float: left;margin: 4px 0 0 4px;}
        }
      }
    }
  }
  .folderSetForm{
    .el-dialog{margin: 0;width: 120px;
      .el-dialog__header{display: none;}
      .el-dialog__body{padding: 9px 0 10px;max-height: 180px;overflow-y: auto;
        .item{line-height: 40px;cursor: pointer;padding-left: 15px;margin-top: 1px;}
        .item:hover{background: #f2f2f2;}
        .arrow{width:9px;position: absolute;top: -20px;right: 18px;height: 20px;}
        .arrow:before {content: '';width: 0;height: 0;border: 10px solid transparent;border-bottom-color: #D9D9D9;position: absolute;left: 0;top: 0;margin-top: 0;}
        .arrow:after {content: "";width: 0;height: 0;border: 9px solid transparent;border-bottom-color: #fff;position: absolute;left: 1px;top: 2px;}
      }
    }
  }
  .sharedfolderForm{
    .el-dialog{
      width: 540px;
    }
    .el-dialog__body{
      padding: 20px;
      overflow: hidden;
      .item{
        float: left;
        margin: 12px 10px 12px 0;
        .simpName{
          width: 34px;
          height: 34px;
          line-height: 34px;
          display: inline-block;
        }
        img{
          width: 34px;
          height: 34px;
          border-radius: 50%;
        }
      }
      .iconfont{
        float: left;
        font-size: 34px;
        margin: 12px 10px 12px 0;
        line-height: 1;
      }
    }
  }
}

.custom-tree{
  >.tree-item{
    border-top: 1px solid #e9e9e9;
    line-height: 40px;
    .tree-content{
      cursor: pointer;
      height: 40px;
      padding-left: 10px;
      >i:first-child{
        float: left;
        margin: 13px 6px 0 0;
      }
      .iconfont{
        display: none;
        float: right;
        margin: 0 10px 0 0;
      }
      .name{
        display: inline-block;
        overflow: hidden;
        text-overflow: ellipsis;
        width: calc(100% - 60px);
        white-space: nowrap;
        color: #333;
        margin: 0;
        text-align: left;
      }
    }
    .tree-content:hover{
      background: #f2f2f2;
      .iconfont{
        display: inline-block;
      }
    }
    .tree-content.is-current{
      background: #f2f2f2;
      .iconfont{
        display: inline-block;
      }
    }
    >.tree-child{
      >.tree-item{
        >.tree-content{
          padding-left: 40px;
          margin-top: 1px;
        .name{
          width: calc(100% - 40px);
        }
        }
      }
    }
    .tree-child{
      display: none;
    }
  }
  .tree-item.expanded{
    .tree-content{
      i:first-child{
        transform: rotate(90deg);
      }
    }
  }
}
</style>
