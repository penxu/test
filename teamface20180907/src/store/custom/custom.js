import {HTTPServer} from '@/common/js/ajax'
import * as types from '../mutations-types'
const dragOptions = {
  animation: 100,
  group: { name: 'compontents', pull: true, put: true },
  ghostClass: 'ghost',
  filter: '.no-drag'
}
// initial state
// shape: [{ id, quantity }]
const state = {
  custom_layout: {},
  enable_layout: {},
  preview_layout: {},
  child_custom_layout: {},
  child_unused_layout: {},
  design_change: {},
  custom_appearance: {},
  custom_module_list: [],
  app_list_layout: false,
  select_hide_field: [],
  select_show_field: [],
  linkage_fields_data: {} // 联动数据
}

// getters
const getters = {
  custom_layout: state => state.custom_layout,
  enable_layout: state => state.enable_layout,
  preview_layout: state => state.preview_layout,
  design_change: state => state.design_change,
  custom_appearance: state => state.custom_appearance,
  child_custom_layout: state => state.child_custom_layout,
  child_unused_layout: state => state.child_unused_layout,
  custom_module_list: state => state.custom_module_list,
  select_hide_field: state => state.select_hide_field,
  select_show_field: state => state.select_show_field,
  linkage_fields_data: state => state.linkage_fields_data
}

// actions
const actions = {
  getEnableLayout: ({ commit, state }, data) => {
    HTTPServer.getEnableLayout(data, 'Loading').then((res) => {
      res.layout.map((item, index) => {
        item.dragOptions = dragOptions
        if (item.name === 'systemInfo') {
          // 系统信息分栏不显示
          res.layout.splice(index, 1)
        }
        item.rows.map((e, index2) => {
          // 去掉组件焦点
          e.active = '0'
        })
      })
      commit('CUSTOM_LAYOUT', res)
      commit('PREVIEW_LAYOUT', res)
      commit('DESIGN_CHANGE', res)
      let appearance = res.appearance
      commit('CUSTOM_APPEARANCE', appearance)
    })
  },
  getDisbaleLayout: ({ commit, state }, data) => {
    HTTPServer.getDisableLayout(data, 'Loading').then((res) => {
      commit('ENABLE_LAYOUT', {rows: res.rows})
    })
  }
}

// mutations
const mutations = {
  [types.CUSTOM_LAYOUT] (state, layout) {
    state.custom_layout = layout
  },
  [types.ENABLE_LAYOUT] (state, layout) {
    state.enable_layout = layout
  },
  [types.PREVIEW_LAYOUT] (state, layout) {
    state.preview_layout = layout
  },
  [types.DESIGN_CHANGE] (state, layout) {
    state.design_change = layout
  },
  [types.CUSTOM_APPEARANCE] (state, layout) {
    state.custom_appearance = layout
  },
  [types.CHILD_CUSTOM_LAYOUT] (state, layout) {
    state.child_custom_layout = layout
  },
  [types.CHILD_UNUSED_LAYOUT] (state, layout) {
    state.child_unused_layout = layout
  },
  [types.APP_LSIT_LAYOUT] (state, layout) {
    state.app_list_layout = layout
  },
  [types.CUSTOM_MODULE_LIST] (state, layout) {
    state.custom_module_list = layout
  },
  [types.SELECT_HIDE_FIELD] (state, layout) {
    state.select_hide_field = layout
  },
  [types.SELECT_SHOW_FIELD] (state, layout) {
    state.select_show_field = layout
  },
  [types.LINKAGE_FIELDS_DATA] (state, layout) {
    state.linkage_fields_data = layout
  }
}

export default {
  state,
  getters,
  actions,
  mutations
}
