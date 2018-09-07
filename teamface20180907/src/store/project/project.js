import * as types from '../mutations-types'
const state = {
  project_status: ''
}
// const getters = {
//   project_status: state => state.project_status
// }
const actions = {
}
const mutations = {
  [types.PROJECT_STATUS] (state, projectStatus) {
    state.project_status = projectStatus
    // console.log(memoDialogIsOpen, 'memoContent')
  }
}
export default {
  state,
  // getters,
  actions,
  mutations
}
