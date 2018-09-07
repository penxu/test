import {HTTPServer} from '@/common/js/ajax'
import * as types from '../mutations-types'

// initial state
// shape: [{ id, quantity }]
const state = {
  initFieldConditions: [],
  allFieldList: [],
  currentBean: null
}

// getters
// const getters = {
//   checkoutStatus: state => state.checkoutStatus,

//   cartProducts: (state, getters, rootState) => {
//     return state.added.map(({ id, quantity }) => {
//       const product = rootState.products.all.find(product => product.id === id)
//       return {
//         title: product.title,
//         price: product.price,
//         quantity
//       }
//     })
//   },
//   cartTotalPrice: (state, getters) => {
//     return getters.cartProducts.reduce((total, product) => {
//       return total + product.price * product.quantity
//     }, 0)
//   }
// }

// actions
const actions = {
  getInitCondition: ({ commit, state }) => {
    HTTPServer.getInitCondition(state.currentBean).then((res) => {
      commit('SET_CONDITIONS', res)
    })
  },
  getAllFieldList: ({commit, state}) => {
    HTTPServer.getAllFieldList(state.currentBean).then((res) => {
      commit('SET_ALL_FIELDS', res)
    })
  }
}

// mutations
const mutations = {
  [types.SET_CURRENT_BEAN] (state, bean) {
    state.currentBean = {'bean': bean}
    console.log(state.currentBean, 'mutations')
  },
  [types.SET_CONDITIONS] (state, data) {
    state.initFieldConditions = data
    console.log(state.initFieldConditions, 'action,高级条件初始化条件')
  },
  [types.SET_ALL_FIELDS] (state, data) {
    state.allFieldList = data
    console.log(state.allFieldList, 'action,当前模块全部字段')
  }
}

export default {
  state,
  // getters,
  actions,
  mutations
}
