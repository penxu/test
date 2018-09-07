// import {HTTPServer} from '@/common/js/ajax'
import * as types from '../mutations-types'

// initial state
// shape: [{ id, quantity }]
const state = {
  currentSelBean: {},
  sourceSelBean: {},
  sourceModuleList: [],
  dashIsAdd: false,
  isChange: false,
  isChangeSource: false, // 是否通过手动改变时数据源
  isChartPage: false,
  // dashId: {} // 当前编辑仪表盘ID
  currentField: '',
  isDrag: false // 字段是否在拖动
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
  [types.SWITCH_CURRENT_BEAN] (state, currentBean) {
    // console.log(currentBean, '切换当前bean')
    state.currentSelBean = currentBean
  },
  [types.SWITCH_SOURCE_BEAN] (state, sourceBean) {
    // console.log(sourceBean, '切换数据源bean')
    state.sourceSelBean = sourceBean
  },
  [types.SWITCH_SOURCE_LIST] (state, sourceList) {
    // console.log(sourceList, '数据源列表LIST')
    state.sourceModuleList = sourceList
  },
  [types.CHANGE_CURRENT_BEAN] (state, isChange) {
    console.log(isChange, 'isChange')
    state.isChangeSource = isChange
  },
  [types.CHART_PAGE] (state, status) {
    console.log(status, '状态')
    state.isChartPage = status
  },
  [types.SET_CURRENT_FIELD] (state, fields) {
    console.log(fields, '当前FIELDS')
    state.currentField = fields
  },
  [types.SET_IS_DRAG] (state, isDrag) {
    console.log(isDrag, '当前isDrag')
    state.isDrag = isDrag
  }
}

export default {
  state,
  // getters,
  actions,
  mutations
}
