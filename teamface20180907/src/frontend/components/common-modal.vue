<template>
  <el-container>
    <module-menu></module-menu>
    <high-seas></high-seas>
    <member-commit></member-commit>
    <module-convert></module-convert>
    <data-share></data-share>
    <data-import></data-import>
    <show-drag></show-drag>
    <detail-print></detail-print>
    <data-links></data-links>
    <vue-map></vue-map>
    <file-preview></file-preview>
    <emp-dep-role-multi :empDepRoleMultiData="empDepRoleMultiDatas"></emp-dep-role-multi>
    <employee-radio :employeeRadioData="employeeRadioDatas"></employee-radio>
    <department-radio :departmentRadioData='departmentRadioDatas'></department-radio>
    <role-radio :roleRadioData='roleRadioDatas'></role-radio>
    <optional-scope-radio  :optionalScopeRadio="optionalScopeRadios"></optional-scope-radio>
    <optional-scope-multi :optionalScopeMulti='optionalScopeMultis'></optional-scope-multi>
    <emoji-form :emojiForm="emojiData"></emoji-form>
    <div class="shade" v-if="openFilter" style="background: rgba(0, 0, 0, 0)"></div>
    <transition name="slide">
      <module-filter v-if="openFilter" :bean="bean" :menuId="menuId"></module-filter>
    </transition>
  </el-container>
</template>

<script>
import ModuleMenu from '../module/module-menu' // 子菜单
import ModuleFilter from '../module/module-filter'
import HighSeas from './high-seas'// 公海池移动,退回
import MemberCommit from './member-commit'// 转移负责人,数据共享,公海池分配
import ModuleConvert from './module-convert'// 数据转换
import DataShare from './data-share'// 数据分析
import DataImport from './data-import'// 数据导入
import ShowDrag from './show-drag'// 拖拽设置是否显示
import DetailPrint from './detail-print'// 打印
import DataLinks from './data-links'// 外部链接
import VueMap from '@/common/alert/vue-map'// 地图
// import FilePreview from '@/common/components/file-preview' // 文件预览
import FilePreview from '@/common/preview/preview-main' // 文件预览
import DepartmentRadio from '@/common/components/department-radio'/** 部门单选 */
import EmployeeRadio from '@/common/components/employee-radio'/** 成员单选 */
import RoleRadio from '@/common/components/role-radio'/** 角色单选 */
import EmpDepRoleMulti from '@/common/components/emp-dep-role-multi'/** 多选集合窗口 */
import optionalScopeMulti from '@/common/components/optional-scope-multi'/** 可选范围多选集合窗口 */
import optionalScopeRadio from '@/common/components/optional-scope-radio'/** 可选范围单选集合窗口 */
import EmojiForm from '@/common/components/emoji' /** 表情 */
export default {
  name: 'common-modal',
  components: {
    ModuleMenu,
    ModuleFilter,
    HighSeas,
    MemberCommit,
    ModuleConvert,
    DataShare,
    DataImport,
    ShowDrag,
    DetailPrint,
    DataLinks,
    VueMap,
    FilePreview,
    EmpDepRoleMulti,
    EmployeeRadio,
    DepartmentRadio,
    RoleRadio,
    optionalScopeRadio,
    optionalScopeMulti,
    EmojiForm
  },
  data () {
    return {
      openCreate: false,
      openFilter: false,
      bean: '',
      menuId: '',
      employeeRadioDatas: {},
      empDepRoleMultiDatas: {},
      departmentRadioDatas: {},
      roleRadioDatas: {},
      optionalScopeRadios: {},
      optionalScopeMultis: {},
      peopleList: [],
      emojiData: {}
    }
  },
  mounted () {
    /** prepareData: 已选对象列表；
     * prepareKey：选择键名；
     * seleteForm：弹窗显示；
     * banData： 不可选择的对象；
     * navKey：多选项、单选成员（0：部门；1：成员；2：角色；3：动态）；
     * removeData: 没有列 (填写value值)
     * haveData: 需要显示的成员（传value）
     * bean: 模块 bean
     * range: 是否正针对于prepareData中选取部门公司的范围
     * style: 1:请求第二种动态参数
     * rangeData： 可选范围数组对象（针对于可选范围组件） */
    /** type:（0：部门；1：成员；2：角色；3：动态） 5:可选范围（单） 6:可选范围(多)  */
    /** 接收方法名 单选部门: selectEmpDepRoleMulti 单选成员: selectEmpDepRoleMulti 单选角色: selectEmpDepRoleMulti 多选: selectEmpDepRoleMulti 单选范围: select-optional-scope-radio 多选范围: select-optional-scope-multi */
    /**
     * 注： 1:prepareData数组对象中基本对象 id、name、sign_id、picture、value、type（value=type + '-' + id）
     */
    this.$bus.off('commonMember')
    this.$bus.on('commonMember', (jsondata) => {
      console.log(jsondata, '选择成员')
      if (jsondata.type === 0) {
        this.departmentRadioDatas = jsondata
      } else if (jsondata.type === 1) {
        this.employeeRadioDatas = jsondata
      } else if (jsondata.type === 2) {
        this.roleRadioDatas = jsondata
      } else if (jsondata.type === 5) {
        this.optionalScopeRadios = jsondata
      } else if (jsondata.type === 6) {
        this.optionalScopeMultis = jsondata
      } else {
        this.empDepRoleMultiDatas = jsondata
      }
    })
    this.$bus.off('getMmojiData')
    this.$bus.on('getMmojiData', (jsondata) => {
      this.emojiData = jsondata
    })
    // 打开筛选窗口
    this.$bus.off('openFilterModal')
    this.$bus.on('openFilterModal', (value, id) => {
      this.openFilter = true
      this.bean = value
      this.menuId = id
    })
    // 关闭筛选窗口
    this.$bus.off('closeFilterModal')
    this.$bus.on('closeFilterModal', value => {
      this.openFilter = value
    })
    // // 打开新增编辑窗口
    // this.$bus.off('openCreateModal')
    // this.$bus.on('openCreateModal', (modules, dtlData) => {
    //   this.openForm = true
    //   this.$nextTick(() => {
    //     this.$bus.emit('sendLayout', modules, dtlData)
    //   })
    // })
    // 关闭表单窗口
    // this.$bus.off('openForm')
    // this.$bus.on('openForm', value => {
    //   this.openForm = false
    // })
  }
}
</script>
