// import {HTTPServer} from '@/common/js/ajax'
import * as types from '../mutations-types'

const state = {
  webform_layout: {},
  delete_omponent: {}
}

// getters
const getters = {
  getCurrentLayout: (state) => (index) => { // 当前编辑的字段
    return state.enableFields[index]
  },
  getEnableLayout: (state) => () => { // 获取使用的布局
    return state.webform_layout.enableLayout
  },
  getDisableLayout: (state) => () => { // 获取未使用的字段
    return state.webform_layout.disableLayout
  },
  getFromProps: (state) => () => { // 获取表单属性
    return state.webform_layout.formProps
  }
  // cartProducts: (state, getters, rootState) => {
  //   return state.added.map(({ id, quantity }) => {
  //     const product = rootState.products.all.find(product => product.id === id)
  //     return {
  //       title: product.title,
  //       price: product.price,
  //       quantity
  //     }
  //   })
  // },
  // cartTotalPrice: (state, getters) => {
  //   return getters.cartProducts.reduce((total, product) => {
  //     return total + product.price * product.quantity
  //   }, 0)
  // }
}
// actions
const actions = {
  // getInitCondition: ({ commit, state }) => {
  //   HTTPServer.getInitCondition(state.currentBean)
  //   .then((res) => {
  //     commit('SET_CONDITIONS', res)
  //   })
  // },
  // getAllFieldList: ({commit, state}) => {
  //   HTTPServer.getAllFieldList(state.currentBean)
  //   .then((res) => {
  //     commit('SET_ALL_FIELDS', res)
  //   })
  // }
  // setModuleBean: ({commit, state}, {sourceParams}) => {
  //   console.log('切换数据源')
  //   commit('SWITCH_SOURCE_BEAN', sourceParams)
  // }
}

// mutations
const mutations = {
  [types.SET_WEBFORM_LAYOUT] (state, allLayout) {
    state.webform_layout = allLayout
    console.log(state.webform_layout, '总布局')
  },
  [types.SET_DISAB_FIELDS] (state, component) {
    console.log(component, '删除')
    if (component.type === 'delete') {
      state.webform_layout.disableLayout.push(component.field)
    } else if (component.type === 'renew') {
      state.webform_layout.disableLayout = component.field
    }
    console.log(state.webform_layout, 'VUE删除的字段')
  },
  [types.SET_WEBFOTM_ENABLE] (state, value) { // 设置使用的布局
    state.webform_layout.enableLayout = JSON.parse(JSON.stringify(value))
    console.log(state.webform_layout.enableLayout, 'vuex最新布局')
  },
  [types.SET_WEBFOTM_PROPS] (state, value) {
    console.log(value, '新表单属性')
    state.webform_layout.formProps = JSON.parse(JSON.stringify(value))
  },
  [types.SET_PREVIEW_LAYOUT] (state, value) {
    console.log(value, '预览的布局')
    state.webform_layout.formProps = JSON.parse(JSON.stringify(value))
  }
}

export default {
  state,
  getters,
  actions,
  mutations
}
