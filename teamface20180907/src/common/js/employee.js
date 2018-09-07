import {HTTPServer} from '@/common/js/ajax.js'
import $ from 'jquery'
export const employee = {
  funInit (arr, prepareData) {
    console.log(arr)
    for (var i = 0; i < arr.length; i++) {
      arr[i].type = 0
      if (arr[i].sign === 'gs') {
        arr[i].type = 3
      }
      arr[i].checked = employee.prepareCheckInit(prepareData, arr[i], arr[i].type)
      if (arr[i].childList) {
        if (arr[i].childList.length > 0) {
          employee.funInit(arr[i].childList)
        }
      }
      if (arr[i].users) {
        if (arr[i].users.length > 0) {
          var users = arr[i].users
          for (var j = 0; j < users.length; j++) {
            arr[i].childList.push({ 'id': users[j].id, 'name': users[j].employee_name, 'picture': users[j].picture, 'type': 1, 'sign_id': users[j].sign_id, 'value': users[j].value, 'checked': employee.prepareCheckInit(prepareData, users[j], 1) })
          }
        }
        delete arr[i].users
      }
    }
  },
  prepareCheckInit (arr, data, type) {
    console.log(arr)
    var bools = false
    for (var i = 0; i < arr.length; i++) {
      if (arr[i].type === type && arr[i].id === data.id) {
        bools = true
      }
    }
    return bools
  },
  conversion (data) {
    var arr = []
    for (var i = 0; i < data.length; i++) {
      arr.push({'id': data[i].id, 'name': data[i].name, 'picture': data[i].picture, 'type': data[i].type, 'sign_id': data[i].sign_id, 'value': data[i].value, 'post_name': data[i].post_name})
    }
    return arr
  },
  refactoring (data) {
    return {'id': data.id, 'name': data.name, 'picture': data.picture, 'type': data.type, 'sign_id': data.sign_id, 'value': data.value, 'post_name': data.post_name}
  },
  /** 更新个人本地信息 */
  /* 获取登录人相关的信息 */
  queryInfo (vue, type) {
    HTTPServer.queryInfo({}, 'Loading').then((res) => {
      sessionStorage.userInfo = JSON.stringify(res)
      vue.$bus.emit('overloadEmployee', true)
      if (type === 'reload') {
        location.reload()
      } else if (type === 'frontend') {
        vue.$router.push({ path: '/frontend/workbench' })
      }
    })
  },
  nodeExpand (tag1, tag2, tag3, tag4) {
    console.log(tag1, tag2, tag3, tag4)
    if (tag1.value === '0-1') {
      $(tag3.$el).parents('.departmentTree')[0].childNodes[0].style.width = tag4 + 'px'
      return
    }
    console.log(tag1, tag2, tag3, tag4)
    setTimeout(() => {
      var conetnt = tag3.$el.getElementsByClassName('el-tree-node__content')
      var label = tag3.$el.getElementsByClassName('tree-label')
      var width = tag4
      for (var i = 0; i < conetnt.length; i++) {
        var w = label[i].offsetWidth + 54 + parseInt(conetnt[i].style.paddingLeft)
        if (w > tag4 && w > width) {
          width = w
        }
      }
      console.log($(tag3.$el).parents('.departmentTree'))
      $(tag3.$el).parents('.departmentTree')[0].childNodes[0].style.width = width + 9 + 'px'
    }, 50)
  },
  nodeCollapse (tag1, tag2, tag3, tag4) {
    if (tag1.value === '0-1') {
      $(tag3.$el).parents('.departmentTree')[0].childNodes[0].style.width = tag4 + 'px'
      return
    }
    console.log(tag1, tag2, tag3, tag4)
    setTimeout(() => {
      var conetnt = tag3.$el.getElementsByClassName('el-tree-node__content')
      var label = tag3.$el.getElementsByClassName('tree-label')
      var width = label[0].offsetWidth + 54 + parseInt(conetnt[0].style.paddingLeft)
      if (width <= tag4) {
        width = tag4
      }
      console.log(width)
      $(tag3.$el).parents('.departmentTree')[0].childNodes[0].style.width = width + 9 + 'px'
    }, 50)
  }
}
export default employee
