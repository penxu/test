import tool from '@/common/js/tool.js'
/*
 * 时间戳 转 时间
 * yyyy-MM-dd HH:mm:ss
 */
let formatDate = (value, type) => {
  if (value) {
    var date = new Date(parseInt(value))
    if (/(y+)/.test(type)) {
      type = type.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length))
    }
    let o = {
      'M+': date.getMonth() + 1,
      'd+': date.getDate(),
      'H+': date.getHours(),
      'm+': date.getMinutes(),
      's+': date.getSeconds()
    }
    var padLeftZero = function (str) {
      return ('00' + str).substr(str.length)
    }
    for (let k in o) {
      if (new RegExp(`(${k})`).test(type)) {
        let str = o[k] + ''
        type = type.replace(RegExp.$1, (RegExp.$1.length === 1) ? str : padLeftZero(str))
      }
    }
    return type
  }
}
export { formatDate }

/*
* 截取
*/
let limitTo = (value, length) => {
  if (value) {
    length = parseInt(length)
    value = (length > 0) ? value.substr(0, length) : value.substr(value.length + length, -length)
    return value
  }
}
export { limitTo }

/*
* 截取
*/
let number = (value, length) => {
  if (value) {
    return value.toFixed(length)
  }
}
export { number }

/*
* 文件大小
*/
let fileSize = (value, empty) => {
  value = parseInt(value)
  let kb = value / 1024
  let mb = value / 1024 / 1024
  let gb = value / 1024 / 1024 / 1024
  var math = function (num) {
    return (Math.ceil(num * 10)) / 10
  }
  if (!gb) {
    return empty
  } else if (gb > 1024) {
    return (gb / 1024) + 'T'
  } else if (mb > 1024) {
    return math(mb / 1024) + 'G'
  } else if (kb > 1024) {
    return math(kb / 1024) + 'M'
  } else if (value > 1024) {
    return math(kb) + 'kb'
  } else {
    return '1kb'
  }
}
export { fileSize }

/*
* 省市区代码转文本
*/
let areaTo = (value) => {
  let text = ''
  let list = []
  if (value) {
    value.split(',').map((item) => {
      list.push(item.split(':')[1])
    })
    list.map((item) => {
      text += item
    })
  }
  return text
}
export { areaTo }

/*
* 文件
*/
let ressuffix = (fileName) => {
  if (fileName) {
    var getFileExt = fileName.replace(/.+\./, '')
    var fileObject = tool.determineFileType(getFileExt)
    return fileObject.icon
  } else {
    return ''
  }
}
export { ressuffix }

/*
* 数字组件格式化
*/
let pointTo = (value, property) => {
  if (value !== '' && value !== undefined) {
    if (property.numberType === '0') {
      return tool.numberCut(value, property.numberLenth)
    } else if (property.numberType === '2') {
      return tool.numberCut(value, property.numberLenth) + ' %'
    } else {
      return tool.numberCut(value, 0)
    }
  }
}
export { pointTo }
/*
* 公式组件格式化
*/
let formulsTo = (value, property) => {
  if (value !== '' && value !== undefined) {
    if (property.numberType === '3') {
      // 文本
      return value
    } else if (property.numberType === '4') {
      // 日期
      return formatDate(value, property.decimalLen)
    } else {
      // 数字
      // if (property.numberType === '0') {
      //   return tool.numberCut(Number(value), property.decimalLen)
      // } else if (property.numberType === '2') {
      //   return tool.numberCut(Number(value), property.decimalLen) + ' %'
      // } else {
      //   return tool.numberCut(Number(value), 0)
      // }
      return value
    }
  }
}
export { formulsTo }

/** 字符串转HTML */
let encodeHtml = (str) => {
  let encodedStr = ''
  if (str === '') {
    return encodedStr
  } else {
    for (var i = 0; i < str.length; i++) {
      encodedStr += '&#' + str.substring(i, i + 1).charCodeAt().toString(10) + ';'
    }
    console.log(encodedStr, '2222222')
    return encodedStr
  }
}
export { encodeHtml }
