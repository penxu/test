<template>
  <el-container class="main-Backend-wrap">
    <el-aside width="200px">
      <backend-nav></backend-nav>
    </el-aside>
    <el-main style="height: 100%;">
    <router-view></router-view>
    <vue-map></vue-map>
    <emp-dep-role-multi :empDepRoleMultiData="empDepRoleMultiDatas"></emp-dep-role-multi>
    <employee-radio :employeeRadioData="employeeRadioDatas"></employee-radio>
    <department-radio :departmentRadioData='departmentRadioDatas'></department-radio>
    <role-radio :roleRadioData='roleRadioDatas'></role-radio>
    <optional-scope-multi :optionalScopeMulti='optionalScopeMultis'></optional-scope-multi>
    </el-main>
  </el-container>
</template>

<script>
import BackendNav from '@/backend/components/backend-nav'
import VueMap from '@/common/alert/vue-map'// 地图
import DepartmentRadio from '@/common/components/department-radio'/** 部门单选 */
import EmployeeRadio from '@/common/components/employee-radio'/** 成员单选 */
import RoleRadio from '@/common/components/role-radio'/** 角色单选 */
import EmpDepRoleMulti from '@/common/components/emp-dep-role-multi'/** 多选集合窗口 */
import optionalScopeMulti from '@/common/components/optional-scope-multi'/** 多选集合窗口 */
export default {
  name: 'MainBackend',
  components: {BackendNav, VueMap, EmpDepRoleMulti, EmployeeRadio, DepartmentRadio, RoleRadio, optionalScopeMulti},
  data () {
    return {
      employeeRadioDatas: {},
      empDepRoleMultiDatas: {},
      departmentRadioDatas: {},
      roleRadioDatas: {},
      optionalScopeMultis: {},
      peopleList: [],
      emojiData: {}
    }
  },
  created () {
    // this.employeeInfo = (JSON.parse(sessionStorage.userInfo)).employeeInfo
    // if (this.employeeInfo.role_id !== 1 && this.employeeInfo.role_id !== 2) {
    //   this.$message.error('您没有访问后台的权限！')
    //   this.$router.push({ path: '/frontend/cpChat' })
    // }
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
     * rangeData： 可选范围数组对象（针对于可选范围组件） */
    /** type:（0：部门；1：成员；2：角色；3：动态） 5:可选范围（单） 6:可选范围(多)  */
    /** 接收方法名 单选部门: selectEmpDepRoleMulti 单选成员: selectEmpDepRoleMulti 单选角色: selectEmpDepRoleMulti 多选: selectEmpDepRoleMulti 单选范围: select-optional-scope-radio 多选范围: select-optional-scope-multi */
    /**
     * 注： 1:prepareData数组对象中基本对象 id、name、sign_id、picture、value、type（value=type + '-' + id）
     */
    // this.$bus.off('commonMember')
    this.$bus.on('commonMember', (jsondata) => {
      console.log(1111, jsondata)
      if (jsondata.type === 0) {
        this.departmentRadioDatas = jsondata
      } else if (jsondata.type === 1) {
        this.employeeRadioDatas = jsondata
      } else if (jsondata.type === 2) {
        this.roleRadioDatas = jsondata
      } else if (jsondata.type === 5) {
        this.roleRadioDatas = jsondata
      } else if (jsondata.type === 6) {
        this.optionalScopeMultis = jsondata
      } else {
        this.empDepRoleMultiDatas = jsondata
      }
    })
  }
}
</script>

<style lang="scss">
.main-Backend-wrap{
  height: 100%;
  .el-aside{height: 100%;}
  .el-main{
    padding: 0;
    .backend_main{
      width: 100%;
      height: 100%;
      padding: 0 25px 0 30px;
      overflow: auto;
      >.header{border-bottom: 1px solid #E7E7E7;overflow: hidden;height: 60px;line-height: 60px;
        >i{float: left;margin: 20px 10px 0 0;font-size: 20px;line-height: 1;}
        >span{font-size: 18px;color: #69696C;}
      }
    }
  }
}
</style>
