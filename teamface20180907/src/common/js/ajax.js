import Vue from 'vue'
import axios from 'axios'
import { Loading } from 'element-ui'
import path from './port.js'
import router from '../../router'
const _this = new Vue()

const baseURL = 'http://192.168.1.202:8080/custom-gateway/' /* 曹 */
// const baseURL = 'http://192.168.1.52:8080/custom-gateway/' /* 刘
// const baseURL = 'http://192.168.1.58:8281/custom-gateway/' /* 莫 */
// const baseURL = 'http://192.168.1.172:8080/custom-gateway/' /* 张 */
// const baseURL = 'http://192.168.1.48:8080/custom-gateway/' /* 徐兵 */
// const baseURL = 'http://192.168.1.60:8083/custom-gateway/' /* 罗军 */
// const baseURL = 'http://192.168.1.62:8083/custom-gateway/' /* 黄镇 */
// const baseURL = 'http://192.168.1.57:8084/custom-gateway/' /* 陈壮利 */
// const baseURL = 'http://192.168.1.9:8090/custom-gateway/' /* 测试 */
// const baseURL = 'http://192.168.1.188:8081/custom-gateway/' /* 188测试 */
// const baseURL = 'http://192.168.1.180:8080/custom-gateway/' /* 180测试 */
// const baseURL = 'http://192.168.1.181:8080/custom-gateway/' /* 181测试 */
// const baseURL = 'http://192.168.1.183:8081/custom-gateway/' /* 183测试 */
// const baseURL = 'http://192.168.1.186:8081/custom-gateway/' /* 186测试 */
// const baseURL = 'https://file.teamface.cn/custom-gateway/' /* 外网测试 */
// const wsURL = 'wss://192.168.1.168:9002' /* 本地测试 */
// const wsURL = 'wss://192.168.1.188:9003' /* 180测试 */
// const wsURL = 'wss://192.168.1.188:9004' /* 181测试 */
const wsURL = 'wss://192.168.1.188:9006' /* 183测试 */
// const wsURL = 'wss://192.168.1.188:9005' /* 186测试 */
// const wsURL = 'wss://push.teamface.cn ' /* 外网测试 */
const llURL = 'wss://192.168.1.188:1131' /** 负载均衡 */

const options = {
  lock: true,
  text: 'Loading...',
  spinner: 'el-icon-loading',
  background: 'rgba(0, 0, 0, 0.4)'
}
/****
 * @params
 *  params //传递的数据
 *  url  //接口地址
 *  mask //遮罩层，不用遮罩层不用传或者false，若使用字符串用于文字显示，如 '保存中...'
 */
function ajaxGet (url, params, mask) {
  if (mask) {
    Loading.service(options)
  }
  let promise = new Promise((resolve, reject) => {
    axios({
      method: 'get',
      url: url,
      baseURL: baseURL,
      params: params,
      headers: JSON.parse(sessionStorage.requestHeader)
    })
      .then((res) => {
        if (mask) {
          let loadingInstance = Loading.service(options)
          _this.$nextTick(() => {
            loadingInstance.close()
          // console.log('关闭LOADING')
          })
        }
        if (res.data.response.code === 1001) {
          if (res.data.hasOwnProperty('identifier')) {
            console.log('进来关联映射')
            resolve(res.data)
          } else {
            resolve(res.data.data)
          }
        } else if (res.data.response.code === 1212) {
          _this.$message.error('登录过期！')
          router.push({ path: '/' })
          // window.location.href = '/#/'
        } else {
          _this.$message.error(res.data.response.describe)
          reject(res.response)
          console.log('请求失败，原因是：' + res.response.describe)
        }
      })
      .catch((err) => {
        let loadingInstance = Loading.service(options)
        _this.$nextTick(() => {
          loadingInstance.close()
        })
        reject(err)
        console.log(err)
      })
  })
  return promise
}
/****
 * @params
 *  data //传递的数据
 *  url  //接口地址
 *  mask //遮罩层，不用遮罩层不用传或者false，若使用字符串用于文字显示，如 '保存中...'
 */
function ajaxPost (url, data, mask) {
  if (mask !== false) {
    if (mask) {
      options.text = mask
    }
    Loading.service(options)
  }
  let promise = new Promise((resolve, reject) => {
    axios({
      method: 'post',
      url: url,
      baseURL: baseURL,
      data: data,
      headers: JSON.parse(sessionStorage.requestHeader)
    })
      .then((res) => {
        let loadingInstance = Loading.service(options)
        _this.$nextTick(() => { // 以服务的方式调用的 Loading 需要异步关闭
          loadingInstance.close()
        })
        if (res.data.response.code === 1001) {
          resolve(res.data.data)
        // if (msg) {S
        //   msgOptions.message = `${msg}成功！`
        //   msgOptions.type = 'success'
        //   // _this.$message(msgOptions)
        //   // setTimeout(() => {
        //   //   Message.close()
        //   // }, 2000)
        // }
        } else if (res.data.response.code === 1212) {
          _this.$message.error('登录过期！')
          router.push({ path: '/' })
          // window.location.href = '/#/'
        } else {
          _this.$message.error(res.data.response.describe)
          reject(res.data.response)
        }
      })
      .catch((err) => {
        reject(err)
        let loadingInstance = Loading.service(options)
        _this.$nextTick(() => { // 以服务的方式调用的 Loading 需要异步关闭
          loadingInstance.close()
        })
        console.log(err)
        _this.$message.error('服务器错误')
      })
  })
  return promise
}

function ajaxGetRequest (params, url) {
  return axios({
    method: 'get',
    url: url,
    baseURL: baseURL,
    params: params,
    headers: JSON.parse(sessionStorage.requestHeader)
  })
}
function ajaxPostRequest (data, url) {
  return axios({
    method: 'post',
    url: url,
    baseURL: baseURL,
    data: data,
    headers: JSON.parse(sessionStorage.requestHeader)
  })
}
// 具体接口
const HTTPServer = {
  commonUpload: function (params, mask) {
    return ajaxPost(path.commonUpload, params, mask)
  },
  customUpload: function (params, mask) {
    return ajaxPost(path.customUpload, params, mask)
  },
  /* *** 聚合表 START************************** */
  getAllApplications: function (params, mask) {
    return ajaxGet(path.getAllApplications, params, mask)
  },
  getDataListFieldsForJuhe: function (params, mask) {
    return ajaxGet(path.getDataListFieldsForJuhe, params, mask)
  },
  getNumicFieldsForJuhe: function (params, mask) {
    return ajaxGet(path.getNumicFieldsForJuhe, params, mask)
  },
  getJuheList: function (params, mask) {
    return ajaxGet(path.getJuheList, params, mask)
  },
  saveJuhe: function (params, mask) {
    return ajaxPost(path.saveJuhe, params, mask)
  },
  aggregationLinkageSave: function (params, mask) {
    return ajaxPost(path.aggregationLinkageSave, params, mask)
  },
  aggregationLinkageEdit: function (params, mask) {
    return ajaxPost(path.aggregationLinkageEdit, params, mask)
  },
  aggregationLinkageDel: function (params, mask) {
    return ajaxPost(path.aggregationLinkageDel, params, mask)
  },
  gotoRun: function (params, mask) {
    return ajaxPost(path.gotoRun, params, mask)
  },
  delJuhe: function (params, mask) {
    return ajaxPost(path.delJuhe, params, mask)
  },
  updateJuhe: function (params, mask) {
    return ajaxPost(path.updateJuhe, params, mask)
  },
  getJuheById: function (params, mask) {
    return ajaxGet(path.getJuheById, params, mask)
  },
  getLinkageList: function (params, mask) {
    return ajaxGet(path.getLinkageList, params, mask)
  },
  getFieldsForLinkage: function (params, mask) {
    return ajaxGet(path.getFieldsForLinkage, params, mask)
  },
  getLinkageDetail: function (params, mask) {
    return ajaxGet(path.getLinkageDetail, params, mask)
  },
  getLinkageFieldsForCustom: function (params, mask) {
    return ajaxGet(path.getLinkageFieldsForCustom, params, mask)
  },
  findAggregationDataLinkageList: function (params, mask) {
    return ajaxPost(path.findAggregationDataLinkageList, params, mask)
  },
  /* *** 聚合表 END*************************** */

  /* *** 工作台 START************************** */
  queryTimeWorkbenchWebList: function (params, mask) {
    return ajaxGet(path.queryTimeWorkbenchWebList, params, mask)
  },
  queryListByFilterAuth: function (params, mask) {
    return ajaxGet(path.queryListByFilterAuth, params, mask)
  },
  queryWorkflowListBy: function (params, mask) {
    return ajaxGet(path.queryWorkflowListBy, params, mask)
  },
  moveTimeWorkbench: function (params, mask) {
    return ajaxPost(path.moveTimeWorkbench, params, mask)
  },
  sortTimeWorkbench: function (params, mask) {
    return ajaxPost(path.sortTimeWorkbench, params, mask)
  },
  queryFlowWorkbenchWebList: function (params, mask) {
    return ajaxGet(path.queryFlowWorkbenchWebList, params, mask)
  },
  moveFlowWorkbench: function (params, mask) {
    return ajaxPost(path.moveFlowWorkbench, params, mask)
  },
  /* *** 工作台 END************************** */

  /* *** 考勤 START************************** */
  findWebList: function (params, mask) {
    return ajaxGet(path.findWebList, params, mask)
  },
  attendanceClassSave: function (params, mask) {
    return ajaxPost(path.attendanceClassSave, params, mask)
  },
  attendanceClassUpdate: function (params, mask) {
    return ajaxPost(path.attendanceClassUpdate, params, mask)
  },
  attendanceClassDel: function (params, mask) {
    return ajaxPost(path.attendanceClassDel, params, mask)
  },
  attendanceWaySave: function (params, mask) {
    return ajaxPost(path.attendanceWaySave, params, mask)
  },
  attendanceWayUpdate: function (params, mask) {
    return ajaxPost(path.attendanceWayUpdate, params, mask)
  },
  attendanceWayDel: function (params, mask) {
    return ajaxPost(path.attendanceWayDel, params, mask)
  },
  attendanceClassFindDetail: function (params, mask) {
    return ajaxGet(path.attendanceClassFindDetail, params, mask)
  },
  attendanceWayFindWebList: function (params, mask) {
    return ajaxGet(path.attendanceWayFindWebList, params, mask)
  },
  attendanceWayFindDetail: function (params, mask) {
    return ajaxGet(path.attendanceWayFindDetail, params, mask)
  },
  scheduleFindWebList: function (params, mask) {
    return ajaxGet(path.scheduleFindWebList, params, mask)
  },
  scheduleFindDetail: function (params, mask) {
    return ajaxGet(path.scheduleFindDetail, params, mask)
  },
  scheduleSave: function (params, mask) {
    return ajaxPost(path.scheduleSave, params, mask)
  },
  scheduleDel: function (params, mask) {
    return ajaxPost(path.scheduleDel, params, mask)
  },
  scheduleUpdate: function (params, mask) {
    return ajaxPost(path.scheduleUpdate, params, mask)
  },
  scheduleTableSave: function (params, mask) {
    return ajaxPost(path.scheduleTableSave, params, mask)
  },
  findScheduleList: function (params, mask) {
    return ajaxGet(path.findScheduleList, params, mask)
  },
  scheduleFindDetailById: function (params, mask) {
    return ajaxGet(path.scheduleFindDetailById, params, mask)
  },
  attendanceManagementUpdate: function (params, mask) {
    return ajaxPost(path.attendanceManagementUpdate, params, mask)
  },
  attendanceCycleSave: function (params, mask) {
    return ajaxPost(path.attendanceCycleSave, params, mask)
  },
  attendanceCycleFindDetail: function (params, mask) {
    return ajaxGet(path.attendanceCycleFindDetail, params, mask)
  },
  attendanceScheduleQueryList: function (params, mask) {
    return ajaxGet(path.attendanceScheduleQueryList, params, mask)
  },
  attendanceCycleUpdate: function (params, mask) {
    return ajaxPost(path.attendanceCycleUpdate, params, mask)
  },
  attendanceCycleDel: function (params, mask) {
    return ajaxPost(path.attendanceCycleDel, params, mask)
  },
  otherSetFindDetail: function (params, mask) {
    return ajaxGet(path.otherSetFindDetail, params, mask)
  },
  attendanceSettingSaveAdmin: function (params, mask) {
    return ajaxPost(path.attendanceSettingSaveAdmin, params, mask)
  },
  attendanceSettingSaveRemind: function (params, mask) {
    return ajaxPost(path.attendanceSettingSaveRemind, params, mask)
  },
  attendanceSettingSaveCount: function (params, mask) {
    return ajaxPost(path.attendanceSettingSaveCount, params, mask)
  },
  attendanceSettingSaveLate: function (params, mask) {
    return ajaxPost(path.attendanceSettingSaveLate, params, mask)
  },
  attendanceSettingSaveHommization: function (params, mask) {
    return ajaxPost(path.attendanceSettingSaveHommization, params, mask)
  },
  attendanceSettingSaveAbsenteeism: function (params, mask) {
    return ajaxPost(path.attendanceSettingSaveAbsenteeism, params, mask)
  },
  /* *** 考勤 END************************** */
  /** 登录注册 */
  login: function (params, mask) {
    return ajaxPost(path.login, params, mask)
  },
  personalInfo: function (params, mask) {
    return ajaxPost(path.personalInfo, params, mask)
  },
  sendSmsCode: function (params, mask) {
    return ajaxPost(path.sendSmsCode, params, mask)
  },
  verifySmsCode: function (params, mask) {
    return ajaxPost(path.verifySmsCode, params, mask)
  },
  modifyPwd: function (params, mask) {
    return ajaxPost(path.modifyPwd, params, mask)
  },
  editPassWord: function (params, mask) {
    return ajaxPost(path.editPassWord, params, mask)
  },
  getCompanySet: function (params, mask) {
    return ajaxGet(path.getCompanySet, params, mask)
  },
  queryInfo: function (params, mask) {
    return ajaxGet(path.queryInfo, params, mask)
  },
  userRegister: function (params, mask) {
    return ajaxPost(path.userRegister, params, mask)
  },
  queryCode: function (params, mask) {
    return ajaxGet(path.queryCode, params, mask)
  },
  scanCodeLogin: function (params, mask) {
    return ajaxGet(path.scanCodeLogin, params, mask)
  },
  companyList: function (params, mask) {
    return ajaxGet(path.companyList, params, mask)
  },
  companyLogin: function (params, mask) {
    return ajaxPost(path.companyLogin, params, mask)
  },
  signOutAccount: function (params, mask) {
    return ajaxGet(path.signOutAccount, params, mask)
  },
  /** 登录注册  END */
  /** ************************选成员组件************************************/
  findUsersByCompany: function (params, mask) {
    return ajaxGet(path.findUsersByCompany, params, mask)
  },
  findCompanyDepartment: function (params, mask) {
    return ajaxGet(path.findCompanyDepartment, params, mask)
  },
  queryRangeEmployeeList: function (params, mask) {
    return ajaxPost(path.queryRangeEmployeeList, params, mask)
  },
  queryRangeDepartmentList: function (params, mask) {
    return ajaxPost(path.queryRangeDepartmentList, params, mask)
  },
  getSharePersonalFields: function (params, mask) {
    return ajaxGet(path.getSharePersonalFields, params, mask)
  },
  /** **********选成员组件 END******************************************************/
  getApplicationList: function (params, mask) {
    return ajaxGet(path.getApplicationList, params, mask)
  },
  getAllModule: function (params, mask) {
    return ajaxGet(path.getAllModule, params, mask)
  },
  getFindModuleListByAuth: function (params, mask) {
    return ajaxGet(path.getFindModuleListByAuth, params, mask)
  },
  getModuleList: function (params, mask) {
    return ajaxGet(path.getModuleList, params, mask)
  },
  getAppAllModuleList: function (params, mask) {
    return ajaxGet(path.getAppAllModuleList, params, mask)
  },
  getAllFieldList: function (params, mask) {
    return ajaxGet(path.getAllFieldList, params, mask)
  },
  getModuleRelations: function (params, mask) {
    return ajaxGet(path.getModuleRelations, params, mask)
  },
  getModuleRefFields: function (params, mask) {
    return ajaxGet(path.getModuleRefFields, params, mask)
  },
  getFieldsBySubform: function (params, mask) {
    return ajaxGet(path.getFieldsBySubform, params, mask)
  },
  getFieldsByNotSubform: function (params, mask) {
    return ajaxGet(path.getFieldsByNotSubform, params, mask)
  },
  getFunAuthList: function (params, mask) {
    return ajaxGet(path.getFunAuthList, params, mask)
  },
  getInitCondition: function (params, mask) {
    return ajaxGet(path.getInitCondition, params, mask)
  },
  getDynmicList: function (params, mask) {
    return ajaxGet(path.getDynmicList, params, mask)
  },
  getPicklistField: function (params, mask) {
    return ajaxGet(path.getPicklistField, params, mask)
  },
  queryDepartmentLevel: function (params, mask) {
    return ajaxGet(path.queryDepartmentLevel, params, mask)
  },
  getProcessApproval: function (params, mask) {
    return ajaxGet(path.getProcessApproval, params, mask)
  },
  getFieldTransList: function (params, mask, message) {
    return ajaxGet(path.getFieldTransList, params, mask)
  },
  addFieldTransform: function (data, mask, message) {
    return ajaxPost(path.addFieldTransform, data, mask)
  },
  delFieldTransform: function (data, mask, message) {
    return ajaxPost(path.delFieldTransform, data, mask)
  },
  getFieldTransformation: function (params, mask, message) {
    return ajaxGet(path.getFieldTransformation, params, mask)
  },
  editFieldTransform: function (data, mask, message) {
    return ajaxPost(path.editFieldTransform, data, mask)
  },
  queryInitAllotDateil: function (data, mask, message) {
    return ajaxPost(path.queryInitAllotDateil, data, mask)
  },
  getAllotList: function (params, mask) {
    return ajaxGet(path.queryAllotList, params, mask)
  },
  saveAllotRule: function (data, mask) {
    return ajaxPost(path.saveAllotRule, data, mask)
  },
  editAllotRule: function (data, mask) {
    return ajaxPost(path.editAllotRule, data, mask)
  },
  getAutoColorList: function (data, mask) {
    return ajaxGet(path.queryColourList, data, mask)
  },
  saveAtutoColor: function (data, mask) {
    return ajaxPost(path.saveAtutoColor, data, mask)
  },
  editColourRule: function (data, mask) {
    return ajaxPost(path.editColourRule, data, mask)
  },
  getHighSeasList: function (data, mask) {
    return ajaxGet(path.getHighSeasList, data, mask)
  },
  addHighSeas: function (data, mask) {
    return ajaxPost(path.addHighSeas, data, mask)
  },
  editHighSeas: function (data, mask) {
    return ajaxPost(path.editHighSeas, data, mask)
  },
  deleteHighSeas: function (data, mask) {
    return ajaxPost(path.deleteHighSeas, data, mask)
  },
  getEditHighSeas: function (data, mask) {
    return ajaxGet(path.getTeapoolById, data, mask)
  },
  savePushSettings: function (data, mask) {
    return ajaxPost(path.savePushSettings, data, mask)
  },
  saveAppDataListFields: function (data, mask) {
    return ajaxPost(path.saveAppDataListFields, data, mask)
  },
  delProcessApproval: function (data, mask) {
    return ajaxPost(path.delProcessApproval, data, mask)
  },
  getMaplist: function (params, mask) {
    return ajaxGet(path.getMaplist, params, mask)
  },
  addRelationMap: function (data, mask) {
    return ajaxPost(path.addRelationMap, data, mask)
  },
  deleRelationMap: function (data, mask) {
    return ajaxPost(path.deleRelationMap, data, mask)
  },
  editRelationMap: function (data, mask) {
    return ajaxPost(path.editRelationMap, data, mask)
  },
  addRelationRely: function (data, mask) {
    return ajaxPost(path.addRelationRely, data, mask)
  },
  deleteRelationRely: function (data, mask) {
    return ajaxPost(path.deleteRelationRely, data, mask)
  },
  editRelationRely: function (data, mask) {
    return ajaxPost(path.editRelationRely, data, mask)
  },
  addOptionFieldDepd: function (data, mask) {
    return ajaxPost(path.addOptionFieldDepd, data, mask)
  },
  editOptionDepd: function (data, mask) {
    return ajaxPost(path.editOptionDepd, data, mask)
  },
  delOptionDepd: function (data, mask) {
    return ajaxPost(path.delOptionDepd, data, mask)
  },
  addOptionFieldCtrl: function (data, mask) {
    return ajaxPost(path.addOptionFieldCtrl, data, mask)
  },
  delOptionCtrl: function (data, mask) {
    return ajaxPost(path.delOptionCtrl, data, mask)
  },
  editOptionCtrl: function (data, mask) {
    return ajaxPost(path.editOptionCtrl, data, mask)
  },
  getDataListFields: function (data, mask) {
    return ajaxGet(path.getDataListFields, data, mask)
  },
  uploadPrintFile: function (data, mask) {
    return ajaxPost(path.uploadPrintFile, data, mask)
  },
  downloadPrintFile: function (data, mask) {
    return ajaxPost(path.downloadPrintFile, data, mask)
  },
  createPrintTemplate: function (data, mask) {
    return ajaxPost(path.createPrintTemplate, data, mask)
  },
  updatePrintTemplate: function (data, mask) {
    return ajaxPost(path.updatePrintTemplate, data, mask)
  },
  delPrintTemplate: function (data, mask) {
    return ajaxPost(path.delPrintTemplate, data, mask)
  },
  getPrintTemplateList: function (params, mask) {
    return ajaxGet(path.getPrintTemplateList, params, mask)
  },
  getPreviewPDF: function (params, mask) {
    return ajaxPost(path.getPreviewPDF, params, mask)
  },
  getModuleFieldList: function (params, mask) {
    return ajaxGet(path.getModuleFieldList, params, mask)
  },
  submitLayout: function (data, mask) {
    return ajaxPost(path.submitLayout, data, mask)
  },
  getEnableLayout: function (params, mask) {
    return ajaxGet(path.getEnableLayout, params, mask)
  },
  getDisableLayout: function (params, mask) {
    return ajaxGet(path.getDisableLayout, params, mask)
  },
  getRepeatCheckList: function (params, mask) {
    return ajaxGet(path.getRepeatCheckList, params, mask)
  },
  getReferenceList: function (data, mask, message) {
    return ajaxPost(path.getReferenceList, data, mask)
  },
  customSubmitData: function (data, mask, message) {
    return ajaxPost(path.customSubmitData, data, mask)
  },
  customEditData: function (data, mask) {
    return ajaxPost(path.customEditData, data, mask)
  },
  customDeleteData: function (data, mask) {
    return ajaxPost(path.customDeleteData, data, mask)
  },
  getCustomList: function (data, mask) {
    return ajaxPost(path.getCustomList, data, mask)
  },
  customDtlData: function (params, mask) {
    return ajaxGet(path.customDtlData, params, mask)
  },
  customDtlRelationData: function (params, mask) {
    return ajaxGet(path.customDtlRelationData, params, mask)
  },
  customRelationList: function (params, mask) {
    return ajaxGet(path.customRelationList, params, mask)
  },
  getCustomListShow: function (params, mask) {
    return ajaxGet(path.getCustomListShow, params, mask)
  },
  getFilterList: function (params, mask) {
    return ajaxGet(path.getFilterList, params, mask)
  },
  transferPrincipal: function (data, mask) {
    return ajaxPost(path.transferPrincipal, data, mask)
  },
  addDataShare: function (data, mask) {
    return ajaxPost(path.addDataShare, data, mask)
  },
  updataDataShare: function (data, mask) {
    return ajaxPost(path.updataDataShare, data, mask)
  },
  highSeasPoolMove: function (data, mask) {
    return ajaxPost(path.highSeasPoolMove, data, mask)
  },
  highSeasPoolDistribute: function (data, mask) {
    return ajaxPost(path.highSeasPoolDistribute, data, mask)
  },
  highSeasPoolGet: function (data, mask) {
    return ajaxPost(path.highSeasPoolGet, data, mask)
  },
  highSeasPoolBack: function (data, mask) {
    return ajaxPost(path.highSeasPoolBack, data, mask)
  },
  addSubMenu: function (data, mask) {
    return ajaxPost(path.addSubMenu, data, mask)
  },
  updateSubMenu: function (data, mask) {
    return ajaxPost(path.updateSubMenu, data, mask)
  },
  sortSubMenu: function (data, mask) {
    return ajaxPost(path.sortSubMenu, data, mask)
  },
  deleteSubMenu: function (data, mask) {
    return ajaxPost(path.deleteSubMenu, data, mask)
  },
  convertData: function (data, mask) {
    return ajaxPost(path.convertData, data, mask)
  },
  getShareSetList: function (params, mask) {
    return ajaxGet(path.getShareSetList, params, mask)
  },
  deleteShareData: function (data, mask) {
    return ajaxPost(path.deleteShareData, data, mask)
  },
  dataImport: function (data, mask) {
    return ajaxPost(path.dataImport, data, mask)
  },
  uploadImportFile: function (data, mask) {
    return ajaxPost(path.uploadImportFile, data, mask)
  },
  saveDataListShow: function (data, mask) {
    return ajaxPost(path.saveDataListShow, data, mask)
  },
  saveLabelShow: function (data, mask) {
    return ajaxPost(path.saveLabelShow, data, mask)
  },
  deleteApp: function (data, mask) {
    return ajaxPost(path.deleteApp, data, mask)
  },
  createApp: function (data, mask) {
    return ajaxPost(path.createApp, data, mask)
  },
  updateApp: function (data, mask) {
    return ajaxPost(path.updateApp, data, mask)
  },
  sortApp: function (data, mask) {
    return ajaxPost(path.sortApp, data, mask)
  },
  sortModules: function (data, mask) {
    return ajaxPost(path.sortModules, data, mask)
  },
  deleteModule: function (data, mask) {
    return ajaxPost(path.deleteModule, data, mask)
  },
  getSubmenuList: function (params, mask) {
    return ajaxGet(path.getSubmenuList, params, mask)
  },
  getAccountEmailList: function (params, mask) {
    return ajaxGet(path.getAccountEmailList, params, mask)
  },
  getAddModuleList: function (params, mask) {
    return ajaxGet(path.getAddModuleList, params, mask)
  },
  queryApprovalCount: function (params, mask) {
    return ajaxGet(path.queryApprovalCount, params, mask)
  },
  querySearchMenu: function (params, mask) {
    return ajaxGet(path.querySearchMenu, params, mask)
  },
  queryApprovalList: function (data, mask) {
    return ajaxPost(path.queryApprovalList, data, mask)
  },
  queryApprovalData: function (data, mask) {
    return ajaxPost(path.queryApprovalData, data, mask)
  },
  approvalRead: function (data, mask) {
    return ajaxPost(path.approvalRead, data, mask)
  },
  ccTo: function (data, mask) {
    return ajaxPost(path.ccTo, data, mask)
  },
  urgeTo: function (data, mask) {
    return ajaxPost(path.urgeTo, data, mask)
  },
  revoke: function (data, mask) {
    return ajaxPost(path.revoke, data, mask)
  },
  removeProcessApproval: function (data, mask) {
    return ajaxPost(path.removeProcessApproval, data, mask)
  },
  getApprovalTypeList: function (params, mask) {
    return ajaxGet(path.getApprovalTypeList, params, mask)
  },
  getApprovalManageList: function (data, mask) {
    return ajaxGet(path.getApprovalManageList, data, mask)
  },
  delApprovalManageData: function (data, mask) {
    return ajaxPost(path.delApprovalManageData, data, mask)
  },
  getApprovalFlowList: function (params, mask) {
    return ajaxGet(path.getApprovalFlowList, params, mask)
  },
  /* *** 备忘录 START************************** */
  uploadForMemo: function (data, mask) {
    return ajaxPost(path.uploadForMemo, data, mask)
  },
  saveMemo: function (data, mask) {
    return ajaxPost(path.saveMemo, data, mask)
  },
  findMemoList: function (params, mask) {
    return ajaxGet(path.findMemoList, params, mask)
  },
  findMemoListTask: function (params, mask) {
    return ajaxGet(path.findMemoListTask, params, mask)
  },
  findMemoDetail: function (params, mask) {
    return ajaxGet(path.findMemoDetail, params, mask)
  },
  memoEdit: function (data, mask) {
    return ajaxPost(path.memoEdit, data, mask)
  },
  memoDel: function (data, mask) {
    return ajaxPost(path.memoDel, data, mask)
  },
  memoFindRelationList: function (data, mask) {
    return ajaxGet(path.memoFindRelationList, data, mask)
  },
  memoUpdateRelationById: function (data, mask) {
    return ajaxPost(path.memoUpdateRelationById, data, mask)
  },
  getFirstFieldFromModule: function (params, mask) {
    return ajaxGet(path.getFirstFieldFromModule, params, mask)
  },
  getFuncAuthWithCommunal: function (params, mask) {
    return ajaxGet(path.getFuncAuthWithCommunal, params, mask)
  },
  /* *** 备忘录 END***************************** */
  /* *自定义图表********* */
  getSourceRelationModule: function (params, mask) {
    return ajaxGet(path.getSourceRelationModule, params, mask)
  },
  getSourceField: function (params, mask) {
    return ajaxGet(path.getSourceField, params, mask)
  },
  getDashList: function (params, mask) {
    return ajaxGet(path.getDashList, params, mask)
  },
  saveDashBoard: function (params, mask) {
    return ajaxPost(path.saveDashBoard, params, mask)
  },
  previewSinge: function (data, mask) {
    return ajaxPost(path.previewSinge, data, mask)
  },
  showSingleForReport: function (data, mask) {
    return ajaxPost(path.showSingleForReport, data, mask)
  },
  getDashLayout: function (data, mask) {
    return ajaxGet(path.getDashLayout, data, mask)
  },
  delDashLayout: function (data, mask) {
    return ajaxPost(path.delDashLayout, data, mask)
  },
  editDashLayout: function (data, mask) {
    return ajaxPost(path.editDashLayout, data, mask)
  },
  runSinge: function (data, mask) {
    return ajaxPost(path.runSinge, data, mask)
  },
  /* *end */
  saveMailAccount: function (params, mask) {
    return ajaxPost(path.saveMailAccount, params, mask)
  },
  editMailAccount: function (params, mask) {
    return ajaxPost(path.editMailAccount, params, mask)
  },
  mailAccountQueryById: function (params, mask) {
    return ajaxGet(path.mailAccountQueryById, params, mask)
  },
  delMailAccount: function (params, mask) {
    return ajaxPost(path.delMailAccount, params, mask)
  },
  mailAccountQueryList: function (params, mask) {
    return ajaxGet(path.mailAccountQueryList, params, mask)
  },
  validateAccount: function (params, mask) {
    return ajaxPost(path.validateAccount, params, mask)
  },
  openOrCloseAccount: function (params, mask) {
    return ajaxPost(path.openOrCloseAccount, params, mask)
  },
  setDefaultAccount: function (params, mask) {
    return ajaxPost(path.setDefaultAccount, params, mask)
  },
  addMailSignature: function (params, mask) {
    return ajaxPost(path.addMailSignature, params, mask)
  },
  mailSignatureQueryById: function (params, mask) {
    return ajaxGet(path.mailSignatureQueryById, params, mask)
  },
  mailSignatureEdit: function (params, mask) {
    return ajaxPost(path.mailSignatureEdit, params, mask)
  },
  delMailSignature: function (params, mask) {
    return ajaxPost(path.delMailSignature, params, mask)
  },
  mailSignatureQueryList: function (params, mask) {
    return ajaxGet(path.mailSignatureQueryList, params, mask)
  },
  openOrSignature: function (params, mask) {
    return ajaxPost(path.openOrSignature, params, mask)
  },
  setDefaultSignature: function (params, mask) {
    return ajaxPost(path.setDefaultSignature, params, mask)
  },
  mailWhiteBlackQueryList: function (params, mask) {
    return ajaxGet(path.mailWhiteBlackQueryList, params, mask)
  },
  saveMailWhiteBlack: function (params, mask) {
    return ajaxPost(path.saveMailWhiteBlack, params, mask)
  },
  mailCatalogqueryList: function (params, mask) {
    return ajaxGet(path.mailCatalogqueryList, params, mask)
  },
  delMailWhiteBlack: function (params, mask) {
    return ajaxPost(path.delMailWhiteBlack, params, mask)
  },
  queryPersonnelAccount: function (params, mask) {
    return ajaxGet(path.queryPersonnelAccount, params, mask)
  },
  mailTagQueryList: function (params, mask) {
    return ajaxGet(path.mailTagQueryList, params, mask)
  },
  mailTagSave: function (params, mask) {
    return ajaxPost(path.mailTagSave, params, mask)
  },
  delMailTag: function (params, mask) {
    return ajaxPost(path.delMailTag, params, mask)
  },
  mailTagQueryById: function (params, mask) {
    return ajaxGet(path.mailTagQueryById, params, mask)
  },
  mailTagEdit: function (params, mask) {
    return ajaxPost(path.mailTagEdit, params, mask)
  },
  mailReceiveRegulationQueryList: function (params, mask) {
    return ajaxGet(path.mailReceiveRegulationQueryList, params, mask)
  },
  getInitailParameters: function (params, mask) {
    return ajaxGet(path.getInitailParameters, params, mask)
  },
  mailTagQueryTagList: function (params, mask) {
    return ajaxGet(path.mailTagQueryTagList, params, mask)
  },
  saveMailReceiveRegulation: function (params, mask) {
    return ajaxPost(path.saveMailReceiveRegulation, params, mask)
  },
  editMailReceiveRegulation: function (params, mask) {
    return ajaxPost(path.editMailReceiveRegulation, params, mask)
  },
  mailReceiveRegulationQueryById: function (params, mask) {
    return ajaxGet(path.mailReceiveRegulationQueryById, params, mask)
  },
  openOrCloseRegulation: function (params, mask) {
    return ajaxPost(path.openOrCloseRegulation, params, mask)
  },
  delmailReceiveRegulation: function (params, mask) {
    return ajaxPost(path.delmailReceiveRegulation, params, mask)
  },
  backOperationBlurMail: function (params, mask) {
    return ajaxGet(path.backOperationBlurMail, params, mask)
  },
  delBackOperation: function (params, mask) {
    return ajaxPost(path.delBackOperation, params, mask)
  },
  getEmailWhere: function (params, mask) {
    return ajaxGet(path.getEmailWhere, params, mask)
  },
  delMailCatalog: function (params, mask) {
    return ajaxPost(path.delMailCatalog, params, mask)
  },
  saveMailCatalog: function (params, mask) {
    return ajaxPost(path.saveMailCatalog, params, mask)
  },
  getProcessTypeForMail: function (params, mask) {
    return ajaxGet(path.getProcessTypeForMail, params, mask)
  },
  emailImageUpload: function (params, mask) {
    return ajaxPost(path.emailImageUpload, params, mask)
  },
  saveReport: function (params, mask) {
    return ajaxPost(path.saveReport, params, mask)
  },
  getReportList: function (params, mask) {
    return ajaxGet(path.getReportList, params, mask)
  },
  removeReport: function (params, mask) {
    return ajaxPost(path.removeReport, params, mask)
  },
  getReportLayoutDetail: function (params, mask) {
    return ajaxGet(path.getReportLayoutDetail, params, mask)
  },
  getReportDataDetail: function (params, mask) {
    return ajaxPost(path.getReportDataDetail, params, mask)
  },
  getFilterFields: function (params, mask) {
    return ajaxGet(path.getFilterFields, params, mask)
  },
  getReportFilterFields: function (params, mask) {
    return ajaxGet(path.getReportFilterFields, params, mask)
  },
  getReportEditLayoutFilterFields: function (params, mask) {
    return ajaxGet(path.getReportEditLayoutFilterFields, params, mask)
  },
  checkReportNameExist: function (params, mask) {
    return ajaxGet(path.checkReportNameExist, params, mask)
  },
  getQueryCommentDetail: function (params, mask) {
    return ajaxGet(path.getQueryCommentDetail, params, mask)
  },
  postSavaCommonComment: function (params, mask) {
    return ajaxPost(path.postSavaCommonComment, params, mask)
  },
  postFileVersionUpload: function (params, mask) {
    return ajaxPost(path.postFileVersionUpload, params, mask)
  },
  fileLibraryUpload: function (params, mask) {
    return ajaxPost(path.fileLibraryUpload, params, mask)
  },
  queryfileCatalog: function (params, mask) {
    return ajaxGet(path.queryfileCatalog, params, mask)
  },
  postWhetherFabulous: function (params, mask) {
    return ajaxPost(path.postWhetherFabulous, params, mask)
  },
  getQueryEmployeeInfo: function (params, mask) {
    return ajaxGet(path.getQueryEmployeeInfo, params, mask)
  },
  postEditEmployeeDetail: function (params, mask) {
    return ajaxPost(path.postEditEmployeeDetail, params, mask)
  },
  getApplyList: function (params, mask) {
    return ajaxGet(path.getApplyList, params, mask)
  },
  modificationCompany: function (params, mask) {
    return ajaxPost(path.modificationCompany, params, mask)
  },
  queryCompanyBanner: function (params, mask) {
    return ajaxGet(path.queryCompanyBanner, params, mask)
  },
  transferEnterprise: function (params, mask) {
    return ajaxPost(path.transferEnterprise, params, mask)
  },
  imageUpload: function (params, mask) {
    return ajaxPost(path.imageUpload, params, mask)
  },
  whiteListMember: function (params, mask) {
    return ajaxGet(path.whiteListMember, params, mask)
  },
  getRecentlyCompanyPasswordStrategy: function (params, mask) {
    return ajaxGet(path.getRecentlyCompanyPasswordStrategy, params, mask)
  },
  retentionPolicy: function (params, mask) {
    return ajaxPost(path.retentionPolicy, params, mask)
  },
  autoLogout: function (params, mask) {
    return ajaxPost(path.autoLogout, params, mask)
  },
  deleteMember: function (params, mask) {
    return ajaxPost(path.deleteMember, params, mask)
  },
  addIpConnector: function (params, mask) {
    return ajaxPost(path.addIpConnector, params, mask)
  },
  deleteIpConnector: function (params, mask) {
    return ajaxPost(path.deleteIpConnector, params, mask)
  },
  addWhiteListMember: function (params, mask) {
    return ajaxPost(path.addWhiteListMember, params, mask)
  },
  getWhiteListMember: function (params, mask) {
    return ajaxGet(path.getWhiteListMember, params, mask)
  },
  changePhoneNumber: function (params, mask) {
    return ajaxPost(path.changePhoneNumber, params, mask)
  },
  savePasswordSetting: function (params, mask) {
    return ajaxPost(path.savePasswordSetting, params, mask)
  },
  applyCommentRequest: function (params, mask) {
    return ajaxGet(path.applyCommentRequest, params, mask)
  },
  centralControlParticularsRequest: function (params, mask) {
    return ajaxGet(path.centralControlParticularsRequest, params, mask)
  },
  submitApplyAudit: function (params, mask) {
    return ajaxPost(path.submitApplyAudit, params, mask)
  },
  auditRequest: function (params, mask) {
    return ajaxPost(path.auditRequest, params, mask)
  },
  applyCompileSave: function (params, mask) {
    return ajaxPost(path.applyCompileSave, params, mask)
  },
  getInquireApplyList: function (params, mask) {
    return ajaxGet(path.getInquireApplyList, params, mask)
  },
  getMyUploadApply: function (params, mask) {
    return ajaxGet(path.getMyUploadApply, params, mask)
  },
  deleteMyUploadApply: function (params, mask) {
    return ajaxPost(path.deleteMyUploadApply, params, mask)
  },
  queryTemplateMoneyList: function (params, mask) {
    return ajaxGet(path.queryTemplateMoneyList, params, mask)
  },
  applyParticularsRequest: function (params, mask) {
    return ajaxGet(path.applyParticularsRequest, params, mask)
  },
  commentMoreParticularsRequest: function (params, mask) {
    return ajaxGet(path.commentMoreParticularsRequest, params, mask)
  },
  commentParticularsRequest: function (params, mask) {
    return ajaxPost(path.commentParticularsRequest, params, mask)
  },
  installApply: function (params, mask) {
    return ajaxPost(path.installApply, params, mask)
  },
  getEstablishModule: function (params, mask) {
    return ajaxGet(path.getEstablishModule, params, mask)
  },
  getFindCompanyDepartment: function (params, mask) {
    return ajaxGet(path.getFindCompanyDepartment, params, mask)
  },
  getFindUsersByCompany: function (params, mask) {
    return ajaxGet(path.getFindUsersByCompany, params, mask)
  },
  getRoleGroupList: function (params, mask) {
    return ajaxGet(path.getRoleGroupList, params, mask)
  },
  getPersonalFields: function (params, mask) {
    return ajaxGet(path.getPersonalFields, params, mask)
  },
  getQueryEmployee: function (params, mask) {
    return ajaxGet(path.getQueryEmployee, params, mask)
  },
  shareSave: function (params, mask) { // 协作分享新增
    return ajaxPost(path.shareSave, params, mask)
  },
  shareEdit: function (params, mask) { // 协作分享修改
    return ajaxPost(path.shareEdit, params, mask)
  },
  shareDelete: function (params, mask) { // 协作分享删除
    return ajaxPost(path.shareDelete, params, mask)
  },
  shareEditRelevance: function (params, mask) { // 协作分享关联变更
    return ajaxPost(path.shareEditRelevance, params, mask)
  },
  shareStick: function (params, mask) { // 协作分享置顶
    return ajaxPost(path.shareStick, params, mask)
  },
  sharePraise: function (params, mask) { // 协作分享点赞
    return ajaxPost(path.sharePraise, params, mask)
  },
  shareQueryById: function (params, mask) { // 协作分享详情
    return ajaxGet(path.shareQueryById, params, mask)
  },
  shareQueryList: function (params, mask) { // 协作分享列表数据
    return ajaxGet(path.shareQueryList, params, mask)
  },
  getSavaBack: function (params, mask) { // 个人偏好保存
    return ajaxPost(path.getSavaBack, params, mask)
  },
  saveInformation: function (params, mask) { // 基本设置保存项目信息
    return ajaxPost(path.saveInformation, params, mask)
  },
  saveSetting: function (params, mask) { // 项目设置保存
    return ajaxPost(path.saveSetting, params, mask)
  },
  editStatus: function (params, mask) { // 状态设置保存
    return ajaxPost(path.editStatus, params, mask)
  },
  editTaskAuth: function (params, mask) { // 任务权限变更
    return ajaxPost(path.editTaskAuth, params, mask)
  },
  editLabels: function (params, mask) { // 任务标签变更
    return ajaxPost(path.editLabels, params, mask)
  },
  projectQueryById: function (params, mask) { // 根据ID查询基本设置详情
    return ajaxGet(path.projectQueryById, params, mask)
  },
  queryTaskAuthList: function (params, mask) { // 根据项目ID 获取任务权限
    return ajaxGet(path.queryTaskAuthList, params, mask)
  },
  queryLabelsList: function (params, mask) { // 根据项目ID 获取标签列表
    return ajaxGet(path.queryLabelsList, params, mask)
  },
  projectControllerSave: function (params, mask) { // 保存项目信息
    return ajaxPost(path.projectControllerSave, params, mask)
  },
  queryMainNode: function (params, mask) { // 获取项目主节点
    return ajaxGet(path.queryMainNode, params, mask)
  },
  querySubNode: function (params, mask) { // 获取项目子节点
    return ajaxGet(path.querySubNode, params, mask)
  },
  queryAllNode: function (params, mask) { // 获取项目所有节点
    return ajaxGet(path.queryAllNode, params, mask)
  },
  saveMainNode: function (params, mask) { // 保存项目主节点
    return ajaxPost(path.saveMainNode, params, mask)
  },
  editMainNode: function (params, mask) { // 主节点重命名
    return ajaxPost(path.editMainNode, params, mask)
  },
  deleteMainNode: function (params, mask) { // 删除主节点
    return ajaxPost(path.deleteMainNode, params, mask)
  },
  sortMainNode: function (params, mask) { // 主节点拖动排序
    return ajaxPost(path.sortMainNode, params, mask)
  },
  saveSubNode: function (params, mask) { // 保存子节点
    return ajaxPost(path.saveSubNode, params, mask)
  },
  editSubNode: function (params, mask) { // 子节点重命名
    return ajaxPost(path.editSubNode, params, mask)
  },
  deleteSubNode: function (params, mask) { // 删除项目子节点
    return ajaxPost(path.deleteSubNode, params, mask)
  },
  sortSubNode: function (params, mask) { // 子节点拖动排序
    return ajaxPost(path.sortSubNode, params, mask)
  },
  queryTaskList: function (params, mask) { // 获取子节点下任务列表
    return ajaxGet(path.queryTaskList, params, mask)
  },
  queryAllWebList: function (params, mask) { // 获取全部项目列表
    return ajaxGet(path.queryAllWebList, params, mask)
  },
  queryMyLeaderWebList: function (params, mask) { // 获取我负责的项目列表
    return ajaxGet(path.queryMyLeaderWebList, params, mask)
  },
  queryMyJoinWebList: function (params, mask) { // 获取我参加的项目列表
    return ajaxGet(path.queryMyJoinWebList, params, mask)
  },
  queryMyCreateWebList: function (params, mask) { // 获取我创建的项目列表
    return ajaxGet(path.queryMyCreateWebList, params, mask)
  },
  queryOngoingWebList: function (params, mask) { // 获取进行中的项目列表
    return ajaxGet(path.queryOngoingWebList, params, mask)
  },
  queryCompletedWebList: function (params, mask) { // 获取已完成的项目列表
    return ajaxGet(path.queryCompletedWebList, params, mask)
  },
  MemberQueryList: function (params, mask) { // 获取成员列表
    return ajaxGet(path.MemberQueryList, params, mask)
  },
  MemberQueryById: function (params, mask) { // 根据ID查询详情
    return ajaxGet(path.MemberQueryById, params, mask)
  },
  MemberSave: function (params, mask) { // 新增成员
    return ajaxPost(path.MemberSave, params, mask)
  },
  MemberUpdate: function (params, mask) { // 修改成员
    return ajaxPost(path.MemberUpdate, params, mask)
  },
  MemberDelete: function (params, mask) { // 删除成员
    return ajaxPost(path.MemberDelete, params, mask)
  },
  deleteTaskMember: function (params, mask) { // 删除协作人
    return ajaxPost(path.deleteTaskMember, params, mask)
  },
  queryTaskWebList: function (params, mask) { // 获取子节点下任务列表
    return ajaxGet(path.queryTaskWebList, params, mask)
  },
  saveTaskWeb: function (params, mask) { // 新增任务
    return ajaxPost(path.saveTaskWeb, params, mask)
  },
  quoteTaskWeb: function (params, mask) { // 引用任务
    return ajaxPost(path.quoteTaskWeb, params, mask)
  },
  batchTaskWeb: function (params, mask) { // 任务批量操作
    return ajaxPost(path.batchTaskWeb, params, mask)
  },
  queryByIdTaskWeb: function (params, mask) { // 根据任务id查详情
    return ajaxGet(path.queryByIdTaskWeb, params, mask)
  },
  updateTaskWeb: function (params, mask) { // 修改任务
    return ajaxPost(path.updateTaskWeb, params, mask)
  },
  deleteTaskWeb: function (params, mask) { // 删除任务
    return ajaxPost(path.deleteTaskWeb, params, mask)
  },
  sortTaskWeb: function (params, mask) { // 任务拖拽排序
    return ajaxPost(path.sortTaskWeb, params, mask)
  },
  saveSubTaskWeb: function (params, mask) { // 新增子任务
    return ajaxPost(path.saveSubTaskWeb, params, mask)
  },
  updateSubTaskWeb: function (params, mask) { // 修改子任务
    return ajaxPost(path.updateSubTaskWeb, params, mask)
  },
  deleteSubTaskWeb: function (params, mask) { // 删除子任务
    return ajaxPost(path.deleteSubTaskWeb, params, mask)
  },
  querySubByIdTaskWeb: function (params, mask) { // 根据子任务id查详情
    return ajaxGet(path.querySubByIdTaskWeb, params, mask)
  },
  querySubListTaskWeb: function (params, mask) { // 获取任务下子任务列表
    return ajaxGet(path.querySubListTaskWeb, params, mask)
  },
  queryRelationListTaskWeb: function (params, mask) { // 获取任务下关联列表
    return ajaxGet(path.queryRelationListTaskWeb, params, mask)
  },
  queryByRelationListTaskWeb: function (params, mask) { // 获取任务下被关联列表
    return ajaxGet(path.queryByRelationListTaskWeb, params, mask)
  },
  saveRelationTaskWeb: function (params, mask) { // 子任务新增关联
    return ajaxPost(path.saveRelationTaskWeb, params, mask)
  },
  quoteRelationTaskWeb: function (params, mask) { // 子任务引用关联
    return ajaxPost(path.quoteRelationTaskWeb, params, mask)
  },
  cancleRelationTaskWeb: function (params, mask) { // 取消子任务关联
    return ajaxPost(path.cancleRelationTaskWeb, params, mask)
  },
  updateTaskStatus: function (params, mask) { // 任务勾选完成状态复选框
    return ajaxPost(path.updateTaskStatus, params, mask)
  },
  updateSubTaskStatus: function (params, mask) { // 子任务勾选完成状态复选框
    return ajaxPost(path.updateSubTaskStatus, params, mask)
  },
  tasksharePraise: function (params, mask) { // 任务上的点赞与取消点赞
    return ajaxPost(path.tasksharePraise, params, mask)
  },
  updatePassedStatus: function (params, mask) { // 任务上的通过按钮
    return ajaxPost(path.updatePassedStatus, params, mask)
  },
  updateSubPassedStatus: function (params, mask) { // 子任务上的通过按钮
    return ajaxPost(path.updateSubPassedStatus, params, mask)
  },
  saveTaskMember: function (params, mask) { // 新增项目任务协作人
    return ajaxPost(path.saveTaskMember, params, mask)
  },
  getProjectEnableLayout: function (params, mask) { // 获取协作人列表
    return ajaxGet(path.getProjectEnableLayout, params, mask)
  },
  updateTaskDetail: function (params, mask) { // 修改任务
    return ajaxPost(path.updateTaskDetail, params, mask)
  },
  queryCollaboratorList: function (params, mask) { // 获取任务自定义使用字段布局
    return ajaxGet(path.queryCollaboratorList, params, mask)
  },
  saveProjectLayout: function (params, mask) { // 保存任务自定义业务数据
    return ajaxPost(path.saveProjectLayout, params, mask)
  },
  editProjectLayout: function (params, mask) { // 修改任务自定义数据
    return ajaxPost(path.editProjectLayout, params, mask)
  },
  queryProjectDataDetail: function (params, mask) { // 修改任务自定义数据
    return ajaxGet(path.queryProjectDataDetail, params, mask)
  },
  queryByHierarchy: function (params, mask) { // 任务层级关系接口
    return ajaxGet(path.queryByHierarchy, params, mask)
  },
  queryManagementRoleInfo: function (params, mask) { // 查询获取成员列表成员的权限
    return ajaxGet(path.queryManagementRoleInfo, params, mask)
  },
  findApprovalModuleList: function (params, mask) { // 获取审批模块列表 或  审批获取模块数据列表
    return ajaxGet(path.findApprovalModuleList, params, mask)
  },
  queryProjectApprovaList: function (params, mask) { // 获取审批模块列表 或  审批获取模块数据列表
    return ajaxPost(path.queryProjectApprovaList, params, mask)
  },
  queryProjectTaskConditions: function (params, mask) { // 获取任务筛选自定义条件接口
    return ajaxGet(path.queryProjectTaskConditions, params, mask)
  },
  queryProjectTaskListByCondition: function (params, mask) { // 项目任务筛选接口
    return ajaxPost(path.queryProjectTaskListByCondition, params, mask)
  },
  updateTaskSubNode: function (params, mask) { // 移动任务指定分列下面
    return ajaxPost(path.updateTaskSubNode, params, mask)
  },
  editProgress: function (params, mask) { // 更新项目项目进度
    return ajaxPost(path.editProgress, params, mask)
  },
  editTaskSort: function (params, mask) { // 项目任务设置，修改任务排序与任务设置
    return ajaxPost(path.editTaskSort, params, mask)
  },
  updateProjectAsterisk: function (params, mask) { // 更新项目星标
    return ajaxPost(path.updateProjectAsterisk, params, mask)
  },
  queryTaskListByCondition: function (params, mask) { // 获取任务列表(个人任务筛选)
    return ajaxPost(path.queryTaskListByCondition, params, mask)
  },
  findAllModuleListByAuth: function (params, mask) { // 获取所有自定义模块根据当前登陆人权限
    return ajaxGet(path.findAllModuleListByAuth, params, mask)
  },
  findDataListByModuleAuth: function (params, mask) { // 搜索模块数据
    return ajaxGet(path.findDataListByModuleAuth, params, mask)
  },
  personelTaskSave: function (params, mask) { // 新增个人任务数据
    return ajaxPost(path.personelTaskSave, params, mask)
  },
  deletePersonelData: function (params, mask) { // 删除个人任务数据
    return ajaxPost(path.deletePersonelData, params, mask)
  },
  queryProjectEmployee: function (params, mask) { // 人员卡片信息
    return ajaxGet(path.queryProjectEmployee, params, mask)
  },
  queryShareRelationList: function (params, mask) { // 协作分享获取关联内容
    return ajaxGet(path.queryShareRelationList, params, mask)
  },
  shareCancleRelation: function (params, mask) { // 协作分享取消关联内容
    return ajaxPost(path.shareCancleRelation, params, mask)
  },
  shareSaveRelation: function (params, mask) { // 协作分享新增关联内容
    return ajaxPost(path.shareSaveRelation, params, mask)
  },
  saveProjectToTemplate: function (params, mask) { // 保存项目模板
    return ajaxPost(path.saveProjectToTemplate, params, mask)
  },
  queryProjectTemplateList: function (params, mask) { // 获取项目模板
    return ajaxGet(path.queryProjectTemplateList, params, mask)
  },
  deleteProjectTemplate: function (params, mask) { // 删除项目模板
    return ajaxPost(path.deleteProjectTemplate, params, mask)
  },
  queryProjectTemplateAllNode: function (params, mask) { // 获取模板任务分组
    return ajaxGet(path.queryProjectTemplateAllNode, params, mask)
  },
  queryQuoteTask: function (params, mask) { // 获取关联任务
    return ajaxGet(path.queryQuoteTask, params, mask)
  },
  queryProjectLibraryList: function (params, mask) {
    return ajaxGet(path.queryProjectLibraryList, params, mask)
  },
  queryProjectFileLibraryList: function (params, mask) {
    return ajaxGet(path.queryProjectFileLibraryList, params, mask)
  },
  queryTaskLibraryList: function (params, mask) {
    return ajaxGet(path.queryTaskLibraryList, params, mask)
  },
  savaProjectFileLibrary: function (params, mask) { // 保存
    return ajaxPost(path.savaProjectFileLibrary, params, mask)
  },
  editProjectLibrary: function (params, mask) { // 修改文件夹
    return ajaxPost(path.editProjectLibrary, params, mask)
  },
  delProjectLibrary: function (params, mask) { // 删除文件、夹
    return ajaxPost(path.delProjectLibrary, params, mask)
  },
  sharProjectLibrary: function (params, mask) { // 共享文件
    return ajaxPost(path.sharProjectLibrary, params, mask)
  },
  projectDownload: function (params, mask) { // 下载文件
    return ajaxGet(path.projectDownload, params, mask)
  },
  projectPersonalUpload: function (params, mask) { // 上传文件
    return ajaxPost(path.projectPersonalUpload, params, mask)
  },
  findPersonelTaskConditions: function (params, mask) { // 获取任务筛选条件
    return ajaxGet(path.findPersonelTaskConditions, params, mask)
  },
  queryQuoteList: function (params, mask) { // 获取引用任务列表
    return ajaxGet(path.queryQuoteList, params, mask)
  },
  queryTaskViewStatus: function (params, mask) { // 查看任务/子任务状态
    return ajaxGet(path.queryTaskViewStatus, params, mask)
  },
  copyTask: function (params, mask) { // 复制任务/子任务
    return ajaxPost(path.copyTask, params, mask)
  },
  getTaskRemind: function (params, mask) { // 获取设置任务提醒
    return ajaxGet(path.getTaskRemind, params, mask)
  },
  saveTaskRemind: function (params, mask) { // 保存设置任务提醒
    return ajaxPost(path.saveTaskRemind, params, mask)
  },
  getPersonelDataDetail: function (params, mask) { // 获取个人任务详情
    return ajaxGet(path.getPersonelDataDetail, params, mask)
  },
  updateForFinish: function (params, mask) { // 勾选个人任务完成
    return ajaxPost(path.updateForFinish, params, mask)
  },
  querySubTaskByTaskId: function (params, mask) { // 根据任务id所有子任务
    return ajaxGet(path.querySubTaskByTaskId, params, mask)
  },
  getPersonelSubDataDetail: function (params, mask) { // 获取个人子任务详情
    return ajaxGet(path.getPersonelSubDataDetail, params, mask)
  },
  savePersonelSubData: function (params, mask) { // 新增个人子任务数据
    return ajaxPost(path.savePersonelSubData, params, mask)
  },
  deletePersonelSubData: function (params, mask) { // 删除个人子任务数据
    return ajaxPost(path.deletePersonelSubData, params, mask)
  },
  updateSubForFinish: function (params, mask) { // 勾选个人子任务完成
    return ajaxPost(path.updateSubForFinish, params, mask)
  },
  saveAssociatesData: function (params, mask) { // 新增关联数据
    return ajaxPost(path.saveAssociatesData, params, mask)
  },
  deleteAssociatesData: function (params, mask) { // 删除关联数据
    return ajaxPost(path.deleteAssociatesData, params, mask)
  },
  queryTaskAssociatesByTaskIdAndType: function (params, mask) { // 根据任务id,任务类型获取任务关联
    return ajaxGet(path.queryTaskAssociatesByTaskIdAndType, params, mask)
  },
  queryBeTaskAssociatesByTaskIdAndType: function (params, mask) { // 根据任务id,任务名称获取被关联的（子任务不会被关联）
    return ajaxGet(path.queryBeTaskAssociatesByTaskIdAndType, params, mask)
  },
  savePersonelMemberData: function (params, mask) { // 添加任务协作人
    return ajaxPost(path.savePersonelMemberData, params, mask)
  },
  deletePersonelMemberData: function (params, mask) { // 删除任务协作人
    return ajaxPost(path.deletePersonelMemberData, params, mask)
  },
  queryMembersTaskId: function (params, mask) { // 获取任务协作人
    return ajaxGet(path.queryMembersTaskId, params, mask)
  },
  savePraiseRecordData: function (params, mask) { // 点赞/取消点赞
    return ajaxPost(path.savePraiseRecordData, params, mask)
  },
  getPraisePerson: function (params, mask) { // 获取点赞人
    return ajaxGet(path.getPraisePerson, params, mask)
  },
  saveTaskRemindData: function (params, mask) { // 新增任务设置数据
    return ajaxPost(path.saveTaskRemindData, params, mask)
  },
  deleteTaskRemindData: function (params, mask) { // 删除任务设置数据
    return ajaxPost(path.deleteTaskRemindData, params, mask)
  },
  findTaskListByProjectId: function (params, mask) { // 根据项目id获取项目任务列表
    return ajaxGet(path.findTaskListByProjectId, params, mask)
  },
  personelSubupdate: function (params, mask) { // 编辑个人 子 任务数据
    return ajaxPost(path.personelSubupdate, params, mask)
  },
  personelTaskupdate: function (params, mask) { // 编辑个人任务数据
    return ajaxPost(path.personelTaskupdate, params, mask)
  },
  praiseQueryList: function (params, mask) { // 任务上的点赞列表（子任务与任务）
    return ajaxGet(path.praiseQueryList, params, mask)
  },
  updateTaskPersonelMemberData: function (params, mask) { // 协作人可见不可见
    return ajaxPost(path.updateTaskPersonelMemberData, params, mask)
  },
  projectFileUpload: function (params, mask) { // 任务上的上传接口
    return ajaxPost(path.projectFileUpload, params, mask)
  },
  queryAllTagList: function (params, mask) { // 获取所有标签带结构
    return ajaxGet(path.queryAllTagList, params, mask)
  },
  queryPersonelTaskViewStatus: function (params, mask) { // 获取个人任务的状态
    return ajaxGet(path.queryPersonelTaskViewStatus, params, mask)
  },
  queryNodesNameById: function (params, mask) { // 获取工作流及其节点信息
    return ajaxGet(path.queryNodesNameById, params, mask)
  },
  querProjectyWorkflowList: function (params, mask) { // 查询项目企业工作流列表
    return ajaxGet(path.querProjectyWorkflowList, params, mask)
  },
  getPersonQueryTaskList: function (params, mask) { // 获取个人任务业务数据列表
    return ajaxGet(path.getPersonQueryTaskList, params, mask)
  },
  getPersonQueryCommonTaskList: function (params, mask) { // 获取项目任务个人任务业务数据列表
    return ajaxGet(path.getPersonQueryCommonTaskList, params, mask)
  },
  queryModuleList: function (params, mask) { // 获取自动化流程，所有模块
    return ajaxGet(path.queryModuleList, params, mask)
  },
  projectQueryAutomationById: function (params, mask) { // 获取协作自动化设置信息
    return ajaxGet(path.projectQueryAutomationById, params, mask)
  },
  projectEditAutomation: function (params, mask) { // 协作自动化设置修改
    return ajaxPost(path.projectEditAutomation, params, mask)
  },
  projectSaveAutomationRule: function (params, mask) { // 自动化设置添加
    return ajaxPost(path.projectSaveAutomationRule, params, mask)
  },
  projectQueryAutomationList: function (params, mask) { // 自动化设置列表
    return ajaxGet(path.projectQueryAutomationList, params, mask)
  },
  projectDeleteAutomation: function (params, mask) { // 协作自动化删除
    return ajaxPost(path.projectDeleteAutomation, params, mask)
  },
  projectQueryAutomationBean: function (params, mask) { // 获取模块及关联模块下拉数据
    return ajaxGet(path.projectQueryAutomationBean, params, mask)
  },
  projectQueryBeanField: function (params, mask) { // 获取模块下字段下拉数据
    return ajaxGet(path.projectQueryBeanField, params, mask)
  },
  queryProjectWorkbenchWebList: function (params, mask) { // 获取项目工作台数据
    return ajaxGet(path.queryProjectWorkbenchWebList, params, mask)
  },
  ProjectyquerWorkflowList: function (params, mask) { // 任务匹配初始化查询
    return ajaxGet(path.ProjectyquerWorkflowList, params, mask)
  },
  findTaskListForPageByProjectId: function (params, mask) { // 获取项目任务业务数据列表
    return ajaxGet(path.findTaskListForPageByProjectId, params, mask)
  },
  queryTaskEmployee: function (params, mask) { // 项目工作流获取人员，编辑任务反选
    return ajaxPost(path.queryTaskEmployee, params, mask)
  },
  getTaskRemindList: function (params, mask) { // 获取个人任务的提醒数据
    return ajaxGet(path.getTaskRemindList, params, mask)
  },
  personelTaskupdateData: function (params, mask) { // 修改个人任务设置
    return ajaxPost(path.personelTaskupdateData, params, mask)
  },
  personelTaskRepeatSaveData: function (params, mask) { // 新增任务重复设置
    return ajaxPost(path.personelTaskRepeatSaveData, params, mask)
  },
  updateTaskRepeatData: function (params, mask) { // 编辑任务重复设置
    return ajaxPost(path.updateTaskRepeatData, params, mask)
  },
  getTaskRepeatList: function (params, mask) { // 获取任务重复设置列表
    return ajaxGet(path.getTaskRepeatList, params, mask)
  },
  projectDataImport: function (params, mask) { // 模板数据处理
    return ajaxPost(path.projectDataImport, params, mask)
  },
  getTaskRoleFromPersonelTask: function (params, mask) { // 获取个人在个人任务中的角色
    return ajaxGet(path.getTaskRoleFromPersonelTask, params, mask)
  },
  queryTaskAnalysisByProjectId: function (params, mask) { // 查看任务进度
    return ajaxGet(path.queryTaskAnalysisByProjectId, params, mask)
  },
  updateRemind: function (params, mask) { // 项目任务-编辑任务设置提醒数据
    return ajaxPost(path.updateRemind, params, mask)
  },
  queryKanbanTaskNodeList: function (params, mask) { // 通过分组获取列表以及任务
    return ajaxGet(path.queryKanbanTaskNodeList, params, mask)
  },
  queryAllTaskList: function (params, mask) { // 获取项目下所有任务
    return ajaxGet(path.queryAllTaskList, params, mask)
  },
  queryTaskModuleList: function (params, mask) { // 获取项目下面的所有模块
    return ajaxGet(path.queryTaskModuleList, params, mask)
  },
  queryTaskListByModuleId: function (params, mask) { // 通过模块获取模块下面的任务
    return ajaxPost(path.queryTaskListByModuleId, params, mask)
  },
  projectTasksaveData: function (params, mask) { // 项目任务设置重复任务
    return ajaxPost(path.projectTasksaveData, params, mask)
  },
  projectTasksupdateData: function (params, mask) { // 项目任务编辑重复任务
    return ajaxPost(path.projectTasksupdateData, params, mask)
  },
  projectTaskgetTaskRepeatList: function (params, mask) { // 获取项目任务重复任务设置
    return ajaxGet(path.projectTaskgetTaskRepeatList, params, mask)
  },
  updateSubNodeDataType: function (params, mask) { // 修改任务列表数类型
    return ajaxPost(path.updateSubNodeDataType, params, mask)
  },
  queryRelationModuleList: function (params, mask) { // 获取工作台关联模块列表
    return ajaxGet(path.queryRelationModuleList, params, mask)
  },
  getRelationModuleInfo: function (params, mask) { // 获取工作台关联模块信息
    return ajaxGet(path.getRelationModuleInfo, params, mask)
  },
  workbenchRelationSave: function (params, mask) { // 保存工作台关联模板
    return ajaxPost(path.workbenchRelationSave, params, mask)
  },
  workbenchRelationUpdate: function (params, mask) { // 工作台修改关联模板
    return ajaxPost(path.workbenchRelationUpdate, params, mask)
  },
  workbenchRelationDel: function (params, mask) { // 删除关联模块
    return ajaxPost(path.workbenchRelationDel, params, mask)
  },
  projectWorkbenchEmployeeList: function (params, mask) { // 获取工作台可查看人员列表
    return ajaxGet(path.projectWorkbenchEmployeeList, params, mask)
  },
  changeEmployeePrivilege: function (params, mask) { // 任务工作台是否有权限切换用户
    return ajaxGet(path.changeEmployeePrivilege, params, mask)
  },
  queryTaskWorkbenchList: function (params, mask) { // 任务工作台是否有权限切换用户
    return ajaxGet(path.queryTaskWorkbenchList, params, mask)
  },
  backendAddRolePermissions: function (params, mask) {
    return ajaxPost(path.backendAddRolePermissions, params, mask)
  },
  backendEditorRolePermissions: function (params, mask) {
    return ajaxPost(path.backendEditorRolePermissions, params, mask)
  },
  saveProcessApproval: function (params, mask) {
    return ajaxPost(path.saveProcessApproval, params, mask)
  },
  getAllPermissions: function (params, mask) {
    return ajaxGet(path.getAllPermissions, params, mask)
  },
  getRoleMembersList: function (params, mask) {
    return ajaxGet(path.getRoleMembersList, params, mask)
  },
  deleteRole: function (params, mask) {
    return ajaxPost(path.deleteRole, params, mask)
  },
  getAuthByRole: function (params, mask) {
    return ajaxGet(path.getAuthByRole, params, mask)
  },
  dataAuthGetRoleGroupList: function (params, mask) {
    return ajaxGet(path.dataAuthGetRoleGroupList, params, mask)
  },
  dataAuthGetAuthName: function (params, mask) {
    return ajaxGet(path.dataAuthGetAuthName, params, mask)
  },
  modifyAuthByRole: function (params, mask) {
    return ajaxPost(path.modifyAuthByRole, params, mask)
  },
  getfieldListWithoutReference: function (params, mask) {
    return ajaxGet(path.getfieldListWithoutReference, params, mask)
  },
  getfieldListSelectField: function (params, mask) {
    return ajaxGet(path.getfieldListSelectField, params, mask)
  },
  getInitialParameter: function (params, mask) {
    return ajaxGet(path.getInitialParameter, params, mask)
  },
  deletePushSettings: function (params, mask) {
    return ajaxPost(path.deletePushSettings, params, mask)
  },
  editPushSettings: function (params, mask) {
    return ajaxPost(path.editPushSettings, params, mask)
  },
  forbidPushSettings: function (params, mask) {
    return ajaxPost(path.forbidPushSettings, params, mask)
  },
  getPushList: function (params, mask) {
    return ajaxGet(path.getPushList, params, mask)
  },
  getInitColourDateil: function (params, mask) {
    return ajaxGet(path.getInitColourDateil, params, mask)
  },
  deleteAutoColor: function (params, mask) {
    return ajaxPost(path.deleteAutoColor, params, mask)
  },
  getAppDetail: function (params, mask) {
    return ajaxGet(path.getAppDetail, params, mask)
  },
  findPcAllModuleList: function (params, mask) {
    return ajaxGet(path.findPcAllModuleList, params, mask)
  },
  getTemplateModuleByTemplateId: function (params, mask) {
    return ajaxGet(path.getTemplateModuleByTemplateId, params, mask)
  },
  saveApplicationModuleUsed: function (params, mask) {
    return ajaxPost(path.saveApplicationModuleUsed, params, mask)
  },
  queryModuleStatistics: function (params, mask) {
    return ajaxGet(path.queryModuleStatistics, params, mask)
  },
  getCooperationParentLabel: function (params, mask) {
    return ajaxGet(path.getCooperationParentLabel, params, mask)
  },
  newlyAddedSubclassification: function (params, mask) {
    return ajaxPost(path.newlyAddedSubclassification, params, mask)
  },
  SearchLabelContent: function (params, mask) {
    return ajaxGet(path.SearchLabelContent, params, mask)
  },
  deleteLabel: function (params, mask) {
    return ajaxPost(path.deleteLabel, params, mask)
  },
  editLabel: function (params, mask) {
    return ajaxPost(path.editLabel, params, mask)
  },
  getModuleShareById: function (params, mask) { // 新增数据点击编辑
    return ajaxGet(path.getModuleShareById, params, mask)
  },
  getShareSave: function (params, mask) { // 新增数据点击保存
    return ajaxPost(path.getShareSave, params, mask)
  },
  getShareDel: function (params, mask) { // 新增数据点击保存
    return ajaxPost(path.getShareDel, params, mask)
  },
  getShareupdate: function (params, mask) { // 编辑数据共享
    return ajaxPost(path.getShareupdate, params, mask)
  },
  getModuleDataShares: function (params, mask) { // 获取字段共享列表
    return ajaxGet(path.getModuleDataShares, params, mask)
  },
  getPicklistControlList: function (params, mask) {
    return ajaxGet(path.getPicklistControlList, params, mask)
  },
  getMoudleIdByBean: function (params, mask) {
    return ajaxGet(path.getMoudleIdByBean, params, mask)
  },
  getChildLayout: function (params, mask) {
    return ajaxGet(path.getChildLayout, params, mask)
  },
  // 新增工作流自动化设置
  saveWorkFlowAutoSettings: function (data, mask) {
    return ajaxPost(path.saveWorkFlowAutoSettings, data, mask)
  },
  // 获取工作流自动化设置列表
  getWorkFlowAutoSettingsList: function (params, mask) {
    return ajaxGet(path.getWorkFlowAutoSettingsList, params, mask)
  },
  // 获取工作流自动化设置详情
  getWorkFlowAutoSettingsDtl: function (params, mask) {
    return ajaxGet(path.getWorkFlowAutoSettingsDtl, params, mask)
  },
  // 修改工作流自动化设置
  updateWorkFlowAutoSettings: function (data, mask) {
    return ajaxPost(path.updateWorkFlowAutoSettings, data, mask)
  },
  // 删除工作流自动化设置
  deleteWorkFlowAutoSettings: function (data, mask) {
    return ajaxPost(path.deleteWorkFlowAutoSettings, data, mask)
  },
  getModuleAndRelModule: function (params, mask) {
    return ajaxGet(path.getModuleAndRelModule, params, mask)
  },
  // 获取模块下字段
  getModuleAndRelModuleField: function (params, mask) {
    return ajaxGet(path.getModuleAndRelModuleField, params, mask)
  },
  changeOrder: function (data, mask) {
    return ajaxPost(path.changeOrder, data, mask)
  },
  EmailTitleCount: function (data, mask) {
    return ajaxGet(path.EmailTitleCount, data, mask)
  },
  getEmailLabelTitle: function (data, mask) {
    return ajaxGet(path.getEmailLabelTitle, data, mask)
  },
  getEmailListContent: function (data, mask) {
    return ajaxGet(path.getEmailListContent, data, mask)
  },
  EMailAllMarkRead: function (data, mask) {
    return ajaxPost(path.EMailAllMarkRead, data, mask)
  },
  EmailMarkRead: function (data, mask) {
    return ajaxPost(path.EmailMarkRead, data, mask)
  },
  EmailMoveLabel: function (data, mask) {
    return ajaxPost(path.EmailMoveLabel, data, mask)
  },
  getEmailListParticulars: function (data, mask) {
    return ajaxGet(path.getEmailListParticulars, data, mask)
  },
  getEmailAndRecordsList: function (data, mask) {
    return ajaxGet(path.getEmailAndRecordsList, data, mask)
  },
  EmailRejection: function (data, mask) {
    return ajaxPost(path.EmailRejection, data, mask)
  },
  getAuthByBean: function (data, mask) {
    return ajaxGet(path.getAuthByBean, data, mask)
  },
  fileEditMember: function (data, mask) {
    return ajaxPost(path.fileEditMember, data, mask)
  }, //  修改文件夹管理员
  queryFileList: function (data, mask) {
    return ajaxGet(path.queryFileList, data, mask)
  }, //  获取公司文件根目录列表
  queryFilePartList: function (data, mask) {
    return ajaxGet(path.queryFilePartList, data, mask)
  }, //  公司子级目录
  queryAppFileList: function (data, mask) {
    return ajaxGet(path.queryAppFileList, data, mask)
  }, //  应用--应用文件夹
  queryModuleFileList: function (data, mask) {
    return ajaxGet(path.queryModuleFileList, data, mask)
  }, //  应用--模块文件夹
  queryModulePartFileList: function (data, mask) {
    return ajaxGet(path.queryModulePartFileList, data, mask)
  }, //  应用--模块文件
  fileQuitShare: function (data, mask) {
    return ajaxPost(path.fileQuitShare, data, mask)
  }, //  退出共享
  fileCancelShare: function (data, mask) {
    return ajaxPost(path.fileCancelShare, data, mask)
  }, //  取消共享
  delFileLibrary: function (data, mask) {
    return ajaxPost(path.delFileLibrary, data, mask)
  }, //  删除文件
  copyFileLibrary: function (data, mask) {
    return ajaxPost(path.copyFileLibrary, data, mask)
  }, //  复制文件至...
  fileEditRename: function (data, mask) {
    return ajaxPost(path.fileEditRename, data, mask)
  }, //  文件重命名
  shiftFileLibrary: function (data, mask) {
    return ajaxPost(path.shiftFileLibrary, data, mask)
  }, //  移动文件夹
  shareFileLibaray: function (data, mask) {
    return ajaxPost(path.shareFileLibaray, data, mask)
  }, //  文件共享
  editFolder: function (data, mask) {
    return ajaxPost(path.editFolder, data, mask)
  }, //  编辑文件夹提交
  fileEditManageStaff: function (data, mask) {
    return ajaxPost(path.fileEditManageStaff, data, mask)
  }, //  保存管理员
  queryFolderInitDetail: function (data, mask) {
    return ajaxGet(path.queryFolderInitDetail, data, mask)
  }, //  文件夹详情
  fileUpdateSetting: function (data, mask) {
    return ajaxPost(path.fileUpdateSetting, data, mask)
  }, //  保存成员权限
  getBlurResultParentInfo: function (data, mask) {
    return ajaxGet(path.getBlurResultParentInfo, data, mask)
  }, //  获取文件夹路劲
  whetherFabulous: function (data, mask) {
    return ajaxPost(path.whetherFabulous, data, mask)
  }, //  文件点赞
  queryVersionList: function (data, mask) {
    return ajaxGet(path.queryVersionList, data, mask)
  }, //  查询文件版本记录
  queryDownLoadList: function (data, mask) {
    return ajaxGet(path.queryDownLoadList, data, mask)
  }, //  查询文件下载记录
  fileBlurSearchFile: function (data, mask) {
    return ajaxGet(path.fileBlurSearchFile, data, mask)
  }, //  搜索文件夹
  savaFileLibrary: function (data, mask) {
    return ajaxPost(path.savaFileLibrary, data, mask)
  }, //  新建文件夹
  queryAidePower: function (data, mask) {
    return ajaxGet(path.queryAidePower, data, mask)
  }, //  小助手进入文件库权限
  queryFileLibarayDetail: function (data, mask) {
    return ajaxGet(path.queryFileLibarayDetail, data, mask)
  },
  queryFileLibraryUrl: function (data, mask) {
    return ajaxGet(path.queryFileLibraryUrl, data, mask)
  },
  openUrlSava: function (data, mask) {
    return ajaxPost(path.openUrlSava, data, mask)
  },
  queryOpenUrlEmail: function (data, mask) {
    return ajaxGet(path.queryOpenUrlEmail, data, mask)
  },
  onlinePreview: function (data, mask) {
    return ajaxPost(path.onlinePreview, data, mask)
  },
  // 企信
  addSingleChat: function (data, mask) {
    return ajaxGet(path.addSingleChat, data, mask)
  },
  getListInfo: function (data, mask) {
    return ajaxGet(path.getListInfo, data, mask)
  },
  hideSession: function (data, mask) {
    return ajaxPost(path.hideSession, data, mask)
  },
  changeGroupMember: function (data, mask) {
    return ajaxPost(path.changeGroupMember, data, mask)
  },
  modifyGroupInfo: function (data, mask) {
    return ajaxPost(path.modifyGroupInfo, data, mask)
  },
  quitGroup: function (data, mask) {
    return ajaxGet(path.quitGroup, data, mask)
  },
  releaseGroup: function (data, mask) {
    return ajaxGet(path.releaseGroup, data, mask)
  },
  imsetTop: function (data, mask) {
    return ajaxPost(path.imsetTop, data, mask)
  },
  getGroupInfo: function (data, mask) {
    return ajaxGet(path.getGroupInfo, data, mask)
  },
  addGroupChat: function (data, mask) {
    return ajaxPost(path.addGroupChat, data, mask)
  },
  getAllGroupsInfo: function (data, mask) {
    return ajaxGet(path.getAllGroupsInfo, data, mask)
  },
  IMreadMessage: function (data, mask) {
    return ajaxGet(path.IMreadMessage, data, mask)
  },
  getAssisstantInfo: function (data, mask) {
    return ajaxGet(path.getAssisstantInfo, data, mask)
  },
  markReadOption: function (data, mask) {
    return ajaxGet(path.markReadOption, data, mask)
  },
  markAllRead: function (data, mask) {
    return ajaxGet(path.markAllRead, data, mask)
  },
  IMfindModuleList: function (data, mask) {
    return ajaxGet(path.IMfindModuleList, data, mask)
  },
  getAssistantMessage: function (data, mask) {
    return ajaxGet(path.getAssistantMessage, data, mask)
  },
  hideSessionWithStatus: function (data, mask) {
    return ajaxPost(path.hideSessionWithStatus, data, mask)
  },
  transferGroup: function (data, mask) {
    return ajaxPost(path.transferGroup, data, mask)
  },
  EmailDeleteCancel: function (data, mask) {
    return ajaxPost(path.EmailDeleteCancel, data, mask)
  },
  EmailCompletelyCancel: function (data, mask) {
    return ajaxPost(path.EmailCompletelyCancel, data, mask)
  },
  getEmailLabelList: function (data, mask) {
    return ajaxGet(path.getEmailLabelList, data, mask)
  },
  getEmailReceiving: function (data, mask) {
    return ajaxGet(path.getEmailReceiving, data, mask)
  },
  EmailisJunkMail: function (data, mask) {
    return ajaxPost(path.EmailisJunkMail, data, mask)
  },
  EmailDustbinTransfer: function (data, mask) {
    return ajaxPost(path.EmailDustbinTransfer, data, mask)
  },
  getEmailContactsList: function (data, mask) {
    return ajaxGet(path.getEmailContactsList, data, mask)
  },
  SelectContactFromModule: function (data, mask) {
    return ajaxGet(path.SelectContactFromModule, data, mask)
  },
  CommonRequestMethodSubmenusModuleClassification: function (data, mask) {
    return ajaxGet(path.CommonRequestMethodSubmenusModuleClassification, data, mask)
  },
  selectModuleGetEmailContact: function (data, mask) {
    return ajaxGet(path.selectModuleGetEmailContact, data, mask)
  },
  EmailSaveDraft: function (data, mask) {
    return ajaxPost(path.EmailSaveDraft, data, mask)
  },
  saveAllLayout: function (data, mask) {
    return ajaxPost(path.saveAllLayout, data, mask)
  },
  getAllLayout: function (data, mask) {
    return ajaxGet(path.getAllLayout, data, mask)
  },
  compileSaveDraft: function (data, mask) {
    return ajaxPost(path.compileSaveDraft, data, mask)
  },
  EmailSendTransmission: function (data, mask) {
    return ajaxPost(path.EmailSendTransmission, data, mask)
  },
  EmailInterfaceSend: function (data, mask) {
    return ajaxPost(path.EmailInterfaceSend, data, mask)
  },
  EmailForwardingSend: function (data, mask) {
    return ajaxPost(path.EmailForwardingSend, data, mask)
  },
  EmailDraftManualSend: function (data, mask) {
    return ajaxPost(path.EmailDraftManualSend, data, mask)
  },
  removeMailTag: function (data, mask) {
    return ajaxPost(path.removeMailTag, data, mask)
  },
  receiptMail: function (data, mask) {
    return ajaxPost(path.receiptMail, data, mask)
  },
  queryEmailFileAuth: function (data, mask) {
    return ajaxGet(path.queryEmailFileAuth, data, mask)
  },
  queryworkbenchList: function (data, mask) {
    return ajaxGet(path.queryworkbenchList, data, mask)
  },
  delWorkbench: function (data, mask) {
    return ajaxPost(path.delWorkbench, data, mask)
  },
  addWorkbench: function (data, mask) {
    return ajaxPost(path.addWorkbench, data, mask)
  },
  editWorkbench: function (data, mask) {
    return ajaxPost(path.editWorkbench, data, mask)
  },
  sortWorkbench: function (data, mask) {
    return ajaxPost(path.sortWorkbench, data, mask)
  },
  getWorkbenchDetail: function (data, mask) {
    return ajaxGet(path.editWorkbenchDetail, data, mask)
  },
  validationIpInterface: function (data, mask) {
    return ajaxPost(path.validationIpInterface, data, mask)
  },
  // 企信 END
  addRoleGroup: function (data, mask) {
    return ajaxPost(path.addRoleGroup, data, mask)
  },
  renameRoleGroup: function (data, mask) {
    return ajaxPost(path.renameRoleGroup, data, mask)
  },
  deleteRoleGroup: function (data, mask) {
    return ajaxPost(path.deleteRoleGroup, data, mask)
  },
  addAuthRole: function (data, mask) {
    return ajaxPost(path.addAuthRole, data, mask)
  },
  modifyAuthRole: function (data, mask) {
    return ajaxPost(path.modifyAuthRole, data, mask)
  },
  deleteAuthRole: function (data, mask) {
    return ajaxPost(path.deleteAuthRole, data, mask)
  },
  modifyUserByRole: function (data, mask) {
    return ajaxPost(path.modifyUserByRole, data, mask)
  },
  getUserByRole: function (data, mask) {
    return ajaxGet(path.getUserByRole, data, mask)
  },

  /** 中央控制台 */
  savaFormalUser: function (data, mask) {
    return ajaxPost(path.savaFormalUser, data, mask)
  },
  queryRegisterUserById: function (data, mask) {
    return ajaxGet(path.queryRegisterUserById, data, mask)
  },
  editFormalCompanyUser: function (data, mask) {
    return ajaxPost(path.editFormalCompanyUser, data, mask)
  },
  queryFormalUserList: function (data, mask) {
    return ajaxGet(path.queryFormalUserList, data, mask)
  },
  queryCenterUserList: function (data, mask) {
    return ajaxGet(path.queryCenterUserList, data, mask)
  },
  disableFormalCompanyUser: function (data, mask) {
    return ajaxPost(path.disableFormalCompanyUser, data, mask)
  },
  enableFormalCompanyUser: function (data, mask) {
    return ajaxPost(path.enableFormalCompanyUser, data, mask)
  },
  delFormalCompanyUser: function (data, mask) {
    return ajaxPost(path.delFormalCompanyUser, data, mask)
  },
  queryRegisterUserList: function (data, mask) {
    return ajaxGet(path.queryRegisterUserList, data, mask)
  },
  centerAdoptAccount: function (data, mask) {
    return ajaxPost(path.centerAdoptAccount, data, mask)
  },
  centerPullBlacklist: function (data, mask) {
    return ajaxPost(path.centerPullBlacklist, data, mask)
  },
  centerDelCompanyUser: function (data, mask) {
    return ajaxPost(path.centerDelCompanyUser, data, mask)
  },
  centerDelInviteCode: function (data, mask) {
    return ajaxPost(path.centerDelInviteCode, data, mask)
  },
  centerSavaInvite: function (data, mask) {
    return ajaxPost(path.centerSavaInvite, data, mask)
  },
  centerQueryInviteList: function (data, mask) {
    return ajaxGet(path.centerQueryInviteList, data, mask)
  },
  centerQueryFchBtnAuth: function (data, mask) {
    return ajaxGet(path.centerQueryFchBtnAuth, data, mask)
  },
  /**   企业工作流 */
  queryWorkFlowList: function (data, mask) {
    return ajaxGet(path.queryWorkFlowList, data, mask)
  },
  queryWorkFlow: function (data, mask) {
    return ajaxGet(path.queryWorkFlow, data, mask)
  },
  saveWorkFlow: function (data, mask) {
    return ajaxPost(path.saveWorkFlow, data, mask)
  },
  editWorkFlow: function (data, mask) {
    return ajaxPost(path.editWorkFlow, data, mask)
  },
  /** 项目统计分析 */
  queryProjectStatistical: function (params, mask) {
    return ajaxGet(path.queryProjectStatistical, params, mask)
  },
  queryProjectsForStatistical: function (params, mask) {
    return ajaxGet(path.queryProjectsForStatistical, params, mask)
  },
  queryProjectsDetailForStatistical: function (data, mask) {
    return ajaxPost(path.queryProjectsDetailForStatistical, data, mask)
  },
  queryTaskAnalysis: function (data, mask) {
    return ajaxPost(path.queryTaskAnalysis, data, mask)
  },
  getModulePageList: function (params, mask) {
    return ajaxGet(path.getModulePageList, params, mask)
  },
  modifyModulePageAuth: function (params, mask) {
    return ajaxPost(path.modifyModulePageAuth, params, mask)
  },
  findSubpageist: function (params, mask) {
    return ajaxGet(path.findSubpageist, params, mask)
  },
  getChartListByModule: function (params, mask) {
    return ajaxGet(path.getChartListByModule, params, mask)
  },
  getChartLayoutByModuleId: function (params, mask) {
    return ajaxGet(path.getChartLayoutByModuleId, params, mask)
  },
  getWebformLayout: function (params, mask) {
    return ajaxGet(path.getWebformLayout, params, mask)
  },
  saveWebformLayout: function (data, mask) {
    return ajaxPost(path.saveWebformLayout, data, mask)
  },
  getWebformList: function (params, mask) {
    return ajaxGet(path.getWebformList, params, mask)
  },
  deleteWebformLayout: function (data, mask) {
    return ajaxPost(path.deleteWebformLayout, data, mask)
  },
  openWebformLayout: function (data, mask) {
    return ajaxPost(path.openWebformLayout, data, mask)
  },
  closeWebformLayout: function (data, mask) {
    return ajaxPost(path.closeWebformLayout, data, mask)
  },
  getAllMenuOfApp: function (params, mask) {
    return ajaxGet(path.getAllMenuOfApp, params, mask)
  },
  getMaplistUrl: function (params, mask) {
    return ajaxGet(path.getMaplistUrl, params, mask)
  },
  getRelyList: function (params, mask) {
    return ajaxGet(path.getRelyList, params, mask)
  },
  getOptionDepd: function (params, mask) {
    return ajaxGet(path.getOptionDepd, params, mask)
  },
  getOptionctrl: function (params, mask) {
    return ajaxGet(path.getOptionctrl, params, mask)
  },
  deleteWrokFlow: function (data, mask) {
    return ajaxPost(path.deleteWrokFlow, data, mask)
  },
  getFieldsOfWorkflow: function (params, mask) {
    return ajaxGet(path.getFieldsOfWorkflow, params, mask)
  },
  saveOutLinksSettings: function (data, mask) {
    return ajaxPost(path.saveOutLinksSettings, data, mask)
  },
  getOutLinksSettings: function (params, mask) {
    return ajaxGet(path.getOutLinksSettings, params, mask)
  },
  getDataLinksInfo: function (params, mask) {
    return ajaxGet(path.getDataLinksInfo, params, mask)
  },
  getRandomBarcode: function (params, mask) {
    return ajaxGet(path.getRandomBarcode, params, mask)
  },
  getBarcodeImg: function (params, mask) {
    return ajaxGet(path.getBarcodeImg, params, mask)
  },
  getRecordLockingList: function (params, mask) {
    return ajaxGet(path.getRecordLockingList, params, mask)
  },
  getRecordLockingDetail: function (params, mask) {
    return ajaxGet(path.getRecordLockingDetail, params, mask)
  },
  saveRecordLocking: function (data, mask) {
    return ajaxPost(path.saveRecordLocking, data, mask)
  },
  delRecordLocking: function (data, mask) {
    return ajaxPost(path.delRecordLocking, data, mask)
  },
  getReferenceModuleAuth: function (params, mask) {
    return ajaxGet(path.getReferenceModuleAuth, params, mask)
  },
  findDetailByName: function (data, mask) {
    return ajaxGet(path.findDetailByName, data, mask)
  },
  findSystemModuleList: function (data, mask) {
    return ajaxGet(path.findSystemModuleList, data, mask)
  },
  updateSystemData: function (data, mask) {
    return ajaxPost(path.updateSystemData, data, mask)
  }
}
// 两个都执行完成才执行
// asyncAjax: function (ajax_one, ajax_two) {
//   axios.all()
// }

export {HTTPServer, baseURL, wsURL, llURL, ajaxGetRequest, ajaxPostRequest}
