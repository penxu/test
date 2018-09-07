// import {HTTPServer} from '@/common/js/ajax'
import * as types from '../mutations-types'

// initial state
// shape: [{ id, quantity }]
const state = {
  allComponent: {},
  componentFlag: '',
  unUseComponent: [], // 未使用字段
  enableFields: [], // 使用的字段
  currentIndex: 0,
  previewLayout: null
}

// getters
const getters = {
  getCurrentFields: (state) => (index) => { // 当前编辑的字段
    return state.enableFields[index]
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
  [types.SET_ALL_LAYOUT] (state, allLayout) {
    console.log('总布局')
    state.allComponent = allLayout
  },
  [types.SET_UNUSE_COMPONENT] (state, component) {
    console.log(component, '删除')
    if (component.type === 'delete') {
      state.unUseComponent.push(component.field)
    } else if (component.type === 'renew') {
      state.unUseComponent = component.field
    }
    console.log(state.unUseComponent, 'VUE删除的字段')
  },
  [types.SET_ENABLE_FIELDS] (state, value) {
    state.enableFields = value
    console.log(state.enableFields, 'vuex最新布局')
  },
  [types.SET_CURRENT_INDEX] (state, value) {
    console.log(value, '新INDEX')
    state.currentIndex = value
  },
  [types.SET_PREVIEW_LAYOUT] (state, value) {
    console.log(value, '预览的布局')
    state.priviewLayout = value
  }
}

export default {
  state,
  getters,
  actions,
  mutations
}
