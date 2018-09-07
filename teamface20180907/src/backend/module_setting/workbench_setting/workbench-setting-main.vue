<template>
  <div class="workbench-set-container">
    <!-- 工作台头部 -->
    <div>
      <div class="workbench-title">
        <i class="iconfont icon-fanhui" style="margin-right:10px;font-size: 24px;cursor: pointer;" @click="gotoBaseMoudel"></i>
        <i class="iconfont icon-module-setting" style="margin-right:10px;font-size: 22px;"></i>
        <span>基础模块</span>
      </div>
      <div class="workbench-title workbench-title-new">
        <span class="worktable-icon"><i class="iconfont icon-nav-workbench-o"></i></span>
        <div>
          <p>工作台</p>
          <p>可视化的组织工具，可以帮助您更快，更准确地管理和监督项目的开发</p>
        </div>
      </div>
    </div>
    <div v-if="!detailVisable">
      <div class="workbench-btn"><el-button @click="handleAddNew()" type="primary"> <i class="iconfont icon-pc-paper-additi"></i> 新建</el-button></div>
      <div>
        <div class="table-box">
          <div class="table-header show-flex">
            <div><span>名称</span></div>
            <div><span>权限</span></div>
            <div><span>修改人</span></div>
            <div><span>修改时间</span></div>
            <div><span>操作</span></div>
          </div>
          <div>
            <div class="table-body show-flex" v-for="(item, index) in fixdWorkbenList" :key="index">
              <div>{{item.workbench_name}}</div>
                <div>{{item.workbench_auth_arr}}</div>
                <div>{{item.modify_by}}</div>
                <div>{{item.modify_time}}</div>
                <div>
                  <el-button type="text" v-if="index==0" @click="openSetting(item)">设置</el-button>
                  <el-button type="text" v-else disabled>编辑</el-button>
                  <!-- <el-button type="text" disabled>删除</el-button> -->
              </div>
            </div>
            <draggable :options="dragOption" @update="upDateSort" v-model="sortWorkbenList">
              <div class="table-body show-flex" v-for="(item, index) in sortWorkbenList" :key="index">
                  <div class="over-ellipsis" :title="item.workbench_name">{{item.workbench_name}}</div>
                  <div class="over-ellipsis" :title="item.workbench_auth_arr">{{item.workbench_auth_arr}}</div>
                  <div>{{item.modify_by}}</div>
                  <div>{{item.modify_time | formatDate('yyyy-MM-dd HH:mm')}}</div>
                  <div>
                    <el-button type="text" @click="handleEdit(item.id)">编辑</el-button>
                    <el-button type="text" @click="handleDelWorkBench(item.id)"><span style="color: #FF4949;">删除</span></el-button>
                  </div>
              </div>
            </draggable>
          </div>
        </div>
      </div>
    </div>
    <WorkbenchDetail v-if="detailVisable" :workbenchId="editId"></WorkbenchDetail>
    <add-workbench-moudel></add-workbench-moudel>
    <el-dialog title="设置" :visible.sync="settingTimeWorkFlow" class="settingTimeWorkFlow" width="580px">
      <div>
        <p>通过设置，将拉取更多的数据到时间工作台呈现</p>
        <p>
          <el-button type="primary" size="mini" @click="openOrEditor('')"><i class="iconfont icon-pc-paper-additi"></i> 新增数据源</el-button>
        </p>
        <div>
          <div class="table-box">
            <div class="table-header show-flex table-header-new">
              <div><span>数据源</span></div>
              <div><span>修改人</span></div>
              <div><span>修改时间</span></div>
              <div><span>操作</span></div>
            </div>
            <div class="show-list-item">
              <div class="table-body show-flex table-body-new" v-for="(item, index) in workbenchNewList" :key="index">
                  <div class="over-ellipsis" :title="item.name">{{item.name}}</div>
                  <div>{{item.personnel_modify_by}}</div>
                  <div>{{item.datetime_modify_time | formatDate('yyyy-MM-dd HH:mm')}}</div>
                  <div>
                    <el-button type="text" @click="openOrEditor(item)">编辑</el-button>
                    <el-button type="text" @click="deleteSettingMoudel(item)"><span style="color: #FF4949;">删除</span></el-button>
                  </div>
              </div>
              <div v-if="workbenchNewList.length == 0" style="padding:10px 0;text-align:center;color:#999;">
                暂无数据
              </div>
            </div>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="settingTimeWorkFlow = false">取 消</el-button>
        <el-button type="primary" @click="settingTimeWorkFlow = false">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import WorkbenchDetail from '@/backend/module_setting/workbench_setting/workbench-detail'
import AddWorkbenchMoudel from '@/backend/module_setting/workbench_setting/add-workbench-moudel'
import draggable from 'vuedraggable'
import { HTTPServer } from '@/common/js/ajax.js'
export default {
  name: 'WorkbenchSettingMain',
  components: {
    draggable,
    WorkbenchDetail,
    AddWorkbenchMoudel
  },
  data () {
    return {
      detailVisable: false,
      workbenchList: [],
      settingTimeWorkFlow: false,
      workbenchId: '',
      workbenchNewList: [],
      // 不能拖动的列表
      fixdWorkbenList: [
        // {
        //   'id': 1, // 工作台记录ID
        //   'name': '时间工作台', // 工作台名字
        //   'auth': '全公司', // 分配给
        //   'create_by': 123456, // 创建人
        //   'create_time': 123456, // 创建时间
        //   'modify_by': 123456, // 修改人
        //   'modify_time': 123456, // 修改时间
        //   'sort': 5 // 工作台的排序序号
        // },
        // {
        //   'id': 1, // 工作台记录ID
        //   'name': '时间工作台', // 工作台名字
        //   'auth': '全公司', // 分配给
        //   'create_by': 123456, // 创建人
        //   'create_time': 123456, // 创建时间
        //   'modify_by': 123456, // 修改人
        //   'modify_time': 123456, // 修改时间
        //   'sort ': 5 // 工作台的排序序号
        // }
      ],
      sortWorkbenList: [ // 可拖动列表
        // {
        //   'id': 1, // 工作台记录ID
        //   'name': '时间工作台', // 工作台名字
        //   'auth': '全公司', // 分配给
        //   'create_by': 123456, // 创建人
        //   'create_time': 123456, // 创建时间
        //   'modify_by': 123456, // 修改人
        //   'modify_time': 123456, // 修改时间
        //   'sort ': 5 // 工作台的排序序号
        // },
        // {
        //   'id': 1, // 工作台记录ID
        //   'name': '时间工作台', // 工作台名字
        //   'auth': '全公司', // 分配给
        //   'create_by': 123456, // 创建人
        //   'create_time': 123456, // 创建时间
        //   'modify_by': 123456, // 修改人
        //   'modify_time': 123456, // 修改时间
        //   'sort ': 5 // 工作台的排序序号
        // },
        // {
        //   'id': 1, // 工作台记录ID
        //   'name': '时间工作台', // 工作台名字
        //   'auth': '全公司', // 分配给
        //   'create_by': 123456, // 创建人
        //   'create_time': 123456, // 创建时间
        //   'modify_by': 123456, // 修改人
        //   'modify_time': 123456, // 修改时间
        //   'sort ': 5 // 工作台的排序序号
        // },
        // {
        //   'id': 1, // 工作台记录ID
        //   'name': '时间工作台', // 工作台名字
        //   'auth': '全公司', // 分配给
        //   'create_by': 123456, // 创建人
        //   'create_time': 123456, // 创建时间
        //   'modify_by': 123456, // 修改人
        //   'modify_time': 123456, // 修改时间
        //   'sort ': 5 // 工作台的排序序号
        // },
        // {
        //   'id': 1, // 工作台记录ID
        //   'name': '时间工作台', // 工作台名字
        //   'auth': '全公司', // 分配给
        //   'create_by': 123456, // 创建人
        //   'create_time': 123456, // 创建时间
        //   'modify_by': 123456, // 修改人
        //   'modify_time': 123456, // 修改时间
        //   'sort ': 5 // 工作台的排序序号
        // }
      ],
      editId: null
    }
  },
  methods: {
    // 获取工作台列表
    getWorkbenchList () {
      HTTPServer.queryworkbenchList('', true).then((res) => {
        console.log(res, 'res')
        // this.getAuthName(res)
        this.fixdWorkbenList = []
        this.sortWorkbenList = []
        res.map((item, index) => {
          if (item.is_default === '0') {
            this.fixdWorkbenList.push(item)
          } else {
            let temName = ''
            item.workbench_auth_arr.map((member, memberIndex) => {
              if (memberIndex < item.workbench_auth_arr.length - 1) {
                temName += `${member.name},`
              } else {
                temName += member.name
              }
            })
            item.workbench_auth_arr = temName
            this.sortWorkbenList.push(item)
          }
        })
      })
    },
    openSetting (item) { // 打开时间工作流设置
      this.workbenchId = item.id
      this.queryRelationModuleList(item)
    },
    gotoBaseMoudel () { // 跳转到基础模块页面
      this.$router.push({path: '/backend/enterprise?fromPage=basemoudel'})
    },
    // 点击新建
    handleAddNew () {
      this.editId = null
      this.hanldeSwitchDetail()
      console.log(this.editId, 'editId')
    },
    // 切换详情
    hanldeSwitchDetail () {
      this.detailVisable = !this.detailVisable
    },
    // 删除工作台
    handleDelWorkBench (id) {
      console.log(id, 'id')
      this.$confirm('确定要删除该工作台吗？ ', '', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let listId = {id: id}
        HTTPServer.delWorkbench(listId).then(res => {
          this.$message({
            type: 'success',
            message: '删除成功!'
          })
          this.getWorkbenchList()
        })
      }).catch(() => {
        // this.$message({
        //   type: 'info',
        //   message: '已取消删除'
        // })
      })
    },
    // 取出成员姓名
    getAuthName (arr) {
      arr.map((item, index) => {
        if (item.is_default === '1') {
          let temName = ''
          item.workbench_auth_arr.map((member, memberIndex) => {
            if (memberIndex < item.workbench_auth_arr.length - 1) {
              temName += `${member.name},`
            } else {
              temName += member.name
            }
          })
          item.workbench_auth_arr = temName
        }
      })
    },
    // 排序
    upDateSort () {
      console.log(this.sortWorkbenList)
      HTTPServer.sortWorkbench(this.sortWorkbenList).then(res => {
        console.log(res, '排序成功')
      })
    },
    // 点击编辑
    handleEdit (data) {
      console.log(data, '正在编辑的工作台')
      this.editId = data
      this.hanldeSwitchDetail()
    },
    queryRelationModuleList (res) { // 获取工作台关联模块列表
      HTTPServer.queryRelationModuleList({workbenchId: res.id}).then(res => {
        console.log(res, '排序成功')
        this.workbenchNewList = res.dataList
        this.settingTimeWorkFlow = true
      })
    },
    openOrEditor (v) { // 编辑或新增数据源
      // this.settingTimeWorkFlow = false
      this.$bus.$emit('openOrEditorMoudel', this.workbenchId, v)
    },
    deleteSettingMoudel (v) { // 删除关联模块
      this.$confirm('确定要删除吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        HTTPServer.workbenchRelationDel({id: v.id}).then(res => {
          this.$message({
            type: 'success',
            message: '删除成功!'
          })
          this.queryRelationModuleList({id: this.workbenchId})
        })
      }).catch(() => { console.log('') })
    }
  },
  computed: {
    dragOption () {
      return {
        animation: 200,
        group: { name: 'workbench', pull: false, put: false },
        sort: true,
        ghostClass: 'sorting'
      }
    }
  },
  created () {
    this.getWorkbenchList()
  },
  mounted () {
    this.$bus.off('switchWorkbench')
    this.$bus.on('switchWorkbench', value => {
      if (!value) {
        this.detailVisable = !this.detailVisable
        this.getWorkbenchList()
      }
    })
    this.$bus.off('updataSettingMoudel')
    this.$bus.on('updataSettingMoudel', value => {
      this.queryRelationModuleList({id: this.workbenchId})
    })
  }
}
</script>

<style lang="scss">
  .workbench-set-container {
    padding: 0 30px 0 25px;
    height: 100%;
    .show-flex {
      display: flex;
    }
    .workbench-title {
      height: 60px;
      line-height: 60px;
      border-bottom: 1px solid #E7E7E7;
      display: flex;
      .worktable-icon{
        width:50px;height:50px;line-height: 50px;text-align: center;border-radius: 5px;background: #C1E3FF;margin:20px 10px 0 0;
        >i{
          color:#31A4FF;font-size:40px;
        }
      }
      i {
        font-size: 23px;
      }
      span {
        font-size: 18px;
      }
    }
    .workbench-title-new{
      height:90px;line-height: 90px;
      >div{
        >p{line-height: 25px;}padding-top:22px;
        >p:nth-child(1){font-size:16px;}
        >p:nth-child(2){font-size:14px;color:#A0A0AE;}
      }
    }
    .workbench-btn {
      height: 72px;
      line-height: 72px;
      .el-button {
        padding: 7px 20px;
        i {
          font-size: 14px;
        }
      }
    }
    .workbench-type-box {
    }
    .border {
      border: 1px solid #ccc;
    }
    .table-box {
      .table-header {
        border-bottom: 1px solid #F2F2F2;
        padding: 0 25px;
        justify-content: space-between;
        height: 50px;
        line-height: 50px;
        font-weight: bold;
        div {
          width: calc((100% - 100px) / 6);
          span {
            padding-left: 10px;
            border-left: 1px solid  #E7E7E7;
          }
        }
      }
      .table-header-new{
        padding:0;
        >div:nth-child(1){width:180px;}
        >div:nth-child(2){width:80px;}
        >div:nth-child(3){width:150px;}
        >div:nth-child(4){width:130px;}
        >div span{
          border:0;
        }
      }
      .table-body {
        border-bottom: 1px solid #F2F2F2;
        padding: 0 25px;
        justify-content: space-between;
        height: 50px;
        line-height: 50px;
          div {
            width: calc((100% - 100px) / 6)
          }
        &:hover {
          background: #efefef;
        }
      }
      .table-body-new{
        padding:0;>div{padding-left:10px;}
        >div:nth-child(1){width:180px;}
        >div:nth-child(2){width:80px;}
        >div:nth-child(3){width:150px;}
        >div:nth-child(4){width:130px;}
      }
      .sorting {
        background: #eee;
      }
    }
    .settingTimeWorkFlow{
      .el-dialog__header{
        padding:15px 20px;border-bottom:1px solid #ddd;
      }
      .el-dialog__footer{
        padding:10px 20px;border-top:1px solid #ddd;
        .el-button{
          padding:0;width:62px;text-align: cneter;height:32px;line-height: 32px;
        }
      }
      .el-dialog__body{
        padding:20px;
        >div{
          >p:nth-child(1){margin-bottom:10px;color:#999999;font-size:12px;}
          >p:nth-child(2){margin-bottom:10px;}
          >div{
            .show-list-item{
              max-height:500px;
              overflow: auto;
            }
          }
        }
      }
    }
  }

</style>
