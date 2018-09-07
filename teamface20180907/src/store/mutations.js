import * as types from './mutations-types'

const mutations = {
  [types.SET_COMPONENT] (state, component) {
    state.component = component
  },
  [types.SET_PROPERTY] (state, property) {
    state.property = property
  },
  [types.SWITCH_SOURCE_BEAN] (state, sourceBean) {
    state.source_bean = sourceBean
  },
  [types.SWITCH_SOURCE_LIST] (state, sourceList) {
    state.source_list = sourceList
  },
  // 备忘录
  [types.MEMO_DIALOG_ISOPEN] (state, memoDialogIsOpen) {
    state.memo_dialog_isopen = memoDialogIsOpen
  },
  // 项目
  [types.PROJECT_STATUS] (state, projectStatus) {
    state.project_status = projectStatus
  }
  // 自定义模块
  // [types.CUSTOM_LAYOUT] (state, layout) {
  //   state.custom_layout = layout
  // }
}

export default mutations
