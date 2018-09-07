import Vue from 'vue'
import Vuex from 'vuex'
import * as actions from './actions'
import * as getters from './getters'
import mutations from './mutations'
import createLogger from 'vuex/dist/logger'
import moduleSetting from './modules/moduleSetting'
import definedChart from './analysis/defined-chart' // 统计分析
import memoData from './memo/memo-data' // 备忘录
import custom from './custom/custom' // 自定义
import taskCustom from './task_custom/task-custom' // 任务自定义
import webformCustom from './webform_custom/webform-custom' // 任务自定义
import projectData from './project/project' // 项目

Vue.use(Vuex)

const debug = process.env.NODE_ENV !== 'production'

export default new Vuex.Store({
  actions,
  getters,
  mutations,
  strict: debug,
  plugins: debug ? [createLogger()] : [],
  modules: {
    moduleSetting,
    definedChart,
    memoData,
    custom,
    taskCustom,
    webformCustom,
    projectData
  }
})
