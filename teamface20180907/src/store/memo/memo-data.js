// import {HTTPServer} from '@/common/js/ajax'
import * as types from '../mutations-types'

// initial state
// shape: [{ id, quantity }]
const state = {
  memoDialogIsOpen: null
}

// actions
const actions = {

}

// mutations
const mutations = {
  [types.MEMO_DIALOG_ISOPEN] (state, memoDialogIsOpen) {
    state.memoDialogIsOpen = memoDialogIsOpen
    // console.log(memoDialogIsOpen, 'memoContent')
  }
}

export default {
  state,
  // getters,
  actions,
  mutations
}
