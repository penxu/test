const utilClass = {
  contains: function (arr, key1, obj, key2) {
    var i = arr.length
    var data = {}
    var obj2 = key2 ? obj[key2] : obj
    while (i--) {
      if (arr[i][key1] + '' === obj2 + '') {
        data = arr[i]
        data.i = i
        return data
      }
    }
    return false
  },
  // 数组去重 (简单数组)
  unique: function (arr) {
    return Array.from(new Set(arr))
  },
  // 数组去重 (对象数组)
  uniqueObject: function (arr) {
    let stringArr = []
    let objectArr = []
    arr.map((item) => {
      stringArr.push(JSON.stringify(item))
    })
    Array.from(new Set(stringArr)).map((item) => {
      objectArr.push(JSON.parse(item))
    })
    return objectArr
  },
  /**
   * 数组排序
   * arr = arr.sort(this.compare('clientTimes'))
   */
  compare (prop) {
    return function (obj1, obj2) {
      var val1 = obj1[prop]
      var val2 = obj2[prop]
      if (!isNaN(Number(val1)) && !isNaN(Number(val2))) {
        val1 = Number(val1)
        val2 = Number(val2)
      }
      if (val1 < val2) {
        return -1
      } else if (val1 > val2) {
        return 1
      } else {
        return 0
      }
    }
  },
  // 两数组取相同部分 (对象数组)
  arrayIntersection: function (arr1, arr2) {
    let array = []
    arr1.map((item) => {
      arr2.map((item2) => {
        if (JSON.stringify(item) === JSON.stringify(item2)) {
          array.push(item)
        }
      })
    })
    return array
  },
  // 获取数组重复部分 (简单数组)
  arrayRepeation: function (arr) {
    let ary = arr.sort()// 数组排序
    let cffwxmsAry = []
    // 所有重复元素添加进新数组内
    for (let i = 0; i < ary.length; i++) {
      if (ary[i] === ary[i + 1]) {
        cffwxmsAry.push(ary[i])
      }
    }
    let result = []
    let isRepeated
    // 对重复元素数组进行元素去重
    for (let k = 0; k < cffwxmsAry.length; k++) {
      isRepeated = false
      for (var j = 0; j < result.length; j++) {
        if (cffwxmsAry[k] === result[j]) {
          isRepeated = true
          break
        }
      }
      if (!isRepeated) {
        result.push(cffwxmsAry[k])
      }
    }
    return result
  },
  // 数组A去除和数组B相同的部分
  arrayRemainder: function (arrA, arrB) {
    let a = []
    let b = []
    let c = []
    arrA.map((item) => {
      a.push(JSON.stringify(item))
    })
    arrB.map((item) => {
      b.push(JSON.stringify(item))
    })
    let aSet = new Set(a)
    let bSet = new Set(b)
    Array.from(new Set(a.concat(b).filter(v => aSet.has(v) && !bSet.has(v)))).map((item) => {
      c.push(JSON.parse(item))
    })
    return c
  },
  // 获取元素的纵坐标（相对于窗口）
  getTop: function (e) { var offset = e.offsetTop; if (e.offsetParent != null) offset += this.getTop(e.offsetParent); return offset },
  // 获取元素的横坐标（相对于窗口）
  getLeft: function (e) { var offset = e.offsetLeft; if (e.offsetParent != null) offset += this.getLeft(e.offsetParent); return offset },
  /** 根据父级class获取父级 */
  getParent: function (e, className) {
    var menu = ''
    var parentNode = function (event) {
      if (event.className === className) {
        menu = event
        return false
      } else {
        parentNode(event.parentNode)
      }
    }
    parentNode(e.target)
    return menu
  },
  /** 判断文件类型,区分文件图标 */
  determineFileType: function (types) {
    types = (types.substring(0, 1) === '.') ? types.substr(1) : types
    var fileType = types.toLowerCase()
    var icon = 'iconfont '
    var imgArr = ['bmp', 'jpg', 'png', 'tiff', 'gif', 'pcx', 'tga', 'exif', 'fpx', 'psd', 'cdr', 'pcd', 'dxf', 'ufo', 'eps', 'ai', 'raw', 'WMF', 'webp']
    var mp4Arr = ['wmv', 'asf', 'asx', 'rm', 'rmvb', 'mpg', 'mpeg', 'mpe', '3gp', 'mov', 'mp4', 'm4v', 'avi', 'dat', 'mkv', 'flv', 'vob']
    var mp3Arr = ['mp3', 'wma', 'ape', 'wav', 'midi', 'au', 'aif', 'ra', 'rm', 'rmx', 'amr']
    if (imgArr.includes(fileType)) {
      fileType = 'img'
      icon = 'img'
    } else if (fileType === 'svg') {
      fileType = 'img'
      icon = 'svg'
    } else if (mp4Arr.includes(fileType)) {
      fileType = 'video'
      icon += 'icon-pc-paper-video'
    } else if (mp3Arr.includes(fileType)) {
      fileType = 'voice'
      icon += 'icon-pc-paper-voice'
    } else if (fileType === 'pdf') {
      fileType = 'pdf'
      icon += 'icon-pc-paper-pdf'
    } else if (fileType === 'doc' || fileType === 'docx') {
      fileType = 'doc'
      icon += 'icon-pc-paper-doc'
    } else if (fileType === 'zip' || fileType === 'rar') {
      fileType = 'zip'
      icon += 'icon-pc-paper-zip'
    } else if (fileType === 'xls' || fileType === 'xlsx' || fileType === 'csv') {
      fileType = 'xls'
      icon += 'icon-pc-paper-xls'
    } else if (fileType === 'ai') {
      fileType = 'ai'
      icon += 'icon-pc-paper-ai'
    } else if (fileType === 'cdr') {
      fileType = 'cdr'
      icon += 'icon-pc-paper-cdr'
    } else if (fileType === 'dwg') {
      fileType = 'dwg'
      icon += 'icon-pc-paper-dwg'
    } else if (fileType === 'ppt' || fileType === 'pptx') {
      fileType = 'ppt'
      icon += 'icon-pc-paper-ppt'
    } else if (fileType === 'txt') {
      fileType = 'txt'
      icon += 'icon-pc-paper-txt'
    } else {
      fileType = 'anyFile'
      icon += 'icon-pc-paper-any-file'
    }
    return {'fileType': fileType, 'icon': icon} // [类型, 文件图标路劲]
  },
  // 判断对象的数据类型
  isClass: function (o) {
    if (o === null) return 'Null'
    if (o === undefined) return 'Undefined'
    return Object.prototype.toString.call(o).slice(8, -1)
  },
  // 深度克隆
  deepClone: function (obj) {
    return JSON.parse(JSON.stringify(obj))
  },
  // 时间戳转字符串
  formatDate: function (value, type) {
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
  },
  // 获得某月的天数
  getMonthDays: function (myMonth) {
    var monthStartDate = new Date(new Date().getFullYear(), myMonth, 1)
    var monthEndDate = new Date(new Date().getFullYear(), myMonth + 1, 1)
    var days = (monthEndDate - monthStartDate) / (1000 * 60 * 60 * 24)
    return days
  },
  // 获得本季度的开始月份
  getQuarterStartMonth: function () {
    var quarterStartMonth = 0
    if (new Date().getMonth() < 3) {
      quarterStartMonth = 0
    }
    if (new Date().getMonth() > 2 && new Date().getMonth() < 6) {
      quarterStartMonth = 3
    }
    if (new Date().getMonth() > 5 && new Date().getMonth() < 9) {
      quarterStartMonth = 6
    }
    if (new Date().getMonth() > 8) {
      quarterStartMonth = 9
    }
    return quarterStartMonth
  },
  // 获得本月的开始时间
  getMonthStartDate () {
    var monthStartDate = new Date(new Date().getFullYear(), new Date().getMonth(), 1)
    return monthStartDate
  },
  // 获得本季度的开始日期
  getQuarterStartDate: function () {
    var quarterStartDate = new Date(new Date().getFullYear(), utilClass.getQuarterStartMonth(), 1)
    return quarterStartDate
  },
  // 获得本季度的结束日期
  getQuarterEndDate: function () {
    var quarterEndMonth = utilClass.getQuarterStartMonth() + 2
    var quarterStartDate = new Date(new Date().getFullYear(), quarterEndMonth, utilClass.getMonthDays(quarterEndMonth))
    return quarterStartDate
  },
  // 上季度第一天
  getPriorSeasonFirstDay: function (year, month) {
    var spring = 0 // 春
    var summer = 3 // 夏
    var fall = 6 // 秋
    var winter = 9// 冬
    // 月份从0-11
    switch (month) { // 季度的其实月份
      case spring:
        // 如果是第一季度则应该到去年的冬季
        year--
        month = winter
        break
      case summer:
        month = spring
        break
      case fall:
        month = summer
        break
      case winter:
        month = fall
        break
    }
    return new Date(year, month, 1)
  },
  // 根据该月的第一天获得该月的最后一天
  getMonthEndDate (firstDay) {
    var date = new Date(firstDay)
    var currentMonth = date.getMonth()
    var nextMonth = ++currentMonth
    var nextMonthFirstDay = new Date(date.getFullYear(), nextMonth, 1)
    var oneDay = 1000 * 60 * 60 * 24
    return new Date(nextMonthFirstDay - oneDay).getTime()
  },
  // 根据时间戳获取星期数
  getDayofWeek (date) {
    var dt = new Date(date)
    var weekDay = ['日', '一', '二', '三', '四', '五', '六']
    return weekDay[dt.getDay()]
  },
  // 根据某月开始时间获取一个月中的所有日期数组
  getEveryDay: function (begin) {
    // (begin:月开始时间)
    begin = utilClass.formatDate(begin, 'yyyy-MM-dd')
    var end = utilClass.getMonthEndDate(begin)
    end = utilClass.formatDate(end, 'yyyy-MM-dd')
    var dateAllArr = []
    var ab = begin.split('-')
    var ae = end.split('-')
    var db = new Date(new Date().toLocaleDateString())
    db.setUTCFullYear(ab[0], ab[1] - 1, ab[2])
    var de = new Date(new Date().toLocaleDateString())
    de.setUTCFullYear(ae[0], ae[1] - 1, ae[2])
    var unixDb = db.getTime() - 24 * 60 * 60 * 1000
    var unixDe = de.getTime() - 24 * 60 * 60 * 1000
    for (var k = unixDb; k <= unixDe;) {
      dateAllArr.push({'dayOfWeek': utilClass.getDayofWeek(k), 'timeStamp': k})
      k = k + 24 * 60 * 60 * 1000
    }
    return dateAllArr
  },
  /** json 转 CSV文件 */
  JSONToCSV: function (tableBody, tableHeader, fileName) {
    var csv = '\ufeff'
    var keys = []
    tableHeader.forEach(function (item) {
      csv += '"' + item.coltext + '",'
      keys.push(item.colname)
    })
    csv = csv.replace(/[,]$/, '\n')
    tableBody.forEach(function (item) {
      keys.forEach(function (key) {
        csv += '"' + item[key] + '",'
      })
      csv = csv.replace(/[,]$/, '\n')
    })
    csv = csv.replace(/"null"/g, '""')
    var blob = new Blob([csv], {
      type: 'text/csv,charset=UTF-8'
    })
    var csvUrl = window.URL.createObjectURL(blob)
    var a = document.createElement('a')
    var now = new Date()

    function to2 (num) {
      return num > 9 ? num : '0' + num
    }
    fileName = fileName || '下载' + now.getFullYear() + to2((now.getMonth() + 1)) + to2(now.getDate()) + to2(now.getHours()) + to2(now.getMinutes()) + to2(now.getSeconds())
    a.download = fileName + '.csv'
    a.href = csvUrl
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
  },
  options: {
    id: '',
    canvasId: 'verifyCanvas',
    width: '100',
    height: '30',
    type: 'blend',
    code: ''
  },
  /** 初始化方法 */
  verifyInit: function (options) {
    if (Object.prototype.toString.call(options) === '[object Object]') {
      for (var i in options) {
        this.options[i] = options[i]
      }
    } else {
      this.options.id = options
    }
    this.options.numArr = '0,1,2,3,4,5,6,7,8,9'.split(',')
    this.options.letterArr = this.getAllLetter()
    this.refresh()
    // var con = document.getElementById(this.options.id)
    var canvas = document.createElement('canvas')
    canvas.id = this.options.canvasId
    canvas.width = this.options.width
    canvas.height = this.options.height
    canvas.style.cursor = 'pointer'
    canvas.innerHTML = '您的浏览器版本不支持canvas'
    // con.appendChild(canvas)
  },
  /** 生成验证码**/
  refresh: function () {
    this.options.code = ''
    var canvas = document.getElementById(this.options.canvasId)
    if (canvas.getContext) {
      var ctx = canvas.getContext('2d')
    } else {
      return
    }
    ctx.textBaseline = 'middle'
    ctx.fillStyle = this.randomColor(180, 240)
    ctx.fillRect(0, 0, this.options.width, this.options.height)
    var txtArr = ''
    if (this.options.type === 'blend') {
      txtArr = this.options.numArr.concat(this.options.letterArr)
    } else if (this.options.type === 'number') {
      txtArr = this.options.numArr
    } else {
      txtArr = this.options.letterArr
    }
    for (var i = 1; i <= 4; i++) {
      var txt = txtArr[this.randomNum(0, txtArr.length)]
      this.options.code += txt
      ctx.font = this.randomNum(this.options.height / 2, this.options.height) + 'px SimHei'
      ctx.fillStyle = this.randomColor(50, 160)
      ctx.shadowOffsetX = this.randomNum(-3, 3)
      ctx.shadowOffsetY = this.randomNum(-3, 3)
      ctx.shadowBlur = this.randomNum(-3, 3)
      ctx.shadowColor = 'rgba(0, 0, 0, 0.3)'
      var x = this.options.width / 5 * i
      var y = this.options.height / 2
      var deg = this.randomNum(-30, 30) /** 设置旋转角度和坐标原点**/
      ctx.translate(x, y)
      ctx.rotate(deg * Math.PI / 180)
      ctx.fillText(txt, 0, 0) /** 恢复旋转角度和坐标原点**/
      ctx.rotate(-deg * Math.PI / 180)
      ctx.translate(-x, -y)
    }
    /** 绘制干扰线**/
    for (var LT = 0; LT < 4; LT++) {
      ctx.strokeStyle = this.randomColor(40, 180)
      ctx.beginPath()
      ctx.moveTo(this.randomNum(0, this.options.width), this.randomNum(0, this.options.height))
      ctx.lineTo(this.randomNum(0, this.options.width), this.randomNum(0, this.options.height))
      ctx.stroke()
    }
    /** 绘制干扰点**/
    for (var FL = 0; FL < this.options.width / 40; FL++) {
      ctx.fillStyle = this.randomColor(0, 255)
      ctx.beginPath()
      ctx.arc(this.randomNum(0, this.options.width), this.randomNum(0, this.options.height), 1, 0, 2 * Math.PI)
      ctx.fill()
    }
  },
  /** 验证验证码**/
  validateCode: function (code) {
    code = code.toLowerCase()
    var vCode = this.options.code.toLowerCase()
    console.log(vCode)
    if (code === vCode) {
      return true
    } else {
      return false
    }
  },
  /** 生成字母数组**/
  getAllLetter: function () {
    var letterStr = 'a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z'
    return letterStr.split(',')
  },
  /** 生成一个随机数**/
  randomNum: function (min, max) {
    return Math.floor(Math.random() * (max - min) + min)
  },
  /** 生成一个随机色**/
  randomColor: function (min, max) {
    var r = this.randomNum(min, max)
    var g = this.randomNum(min, max)
    var b = this.randomNum(min, max)
    return 'rgb(' + r + ',' + g + ',' + b + ')'
  },
  // JS操作cookies方法!
  // 写cookies
  // 读取cookies
  getCookie: function (name) {
    var aCookie = document.cookie.split('; ')
    for (var i = 0; i < aCookie.length; i++) {
      var aCrumb = aCookie[i].split('=')
      if (name === aCrumb[0]) {
        return unescape(aCrumb[1]).substring(0, unescape(aCrumb[1]).length - 7)
      }
    }
    return null
  },
  // 删除cookies
  delCookie: function (name) {
    console.log(name)
    var exp = new Date()
    exp.setTime(exp.getTime() - 1)
    var cval = this.getCookie(name)
    if (cval != null) document.cookie = name + '=' + cval + 'expires=' + exp.toGMTString()
  },
  // 使用示例
  // 如果需要设定自定义过期时间
  // 那么把上面的函数换成下面两个函数就ok
  // 程序代码
  setCookie: function (name, value, time) {
    var strsec = this.getsec(time)
    var exp = new Date()
    exp.setTime(exp.getTime() + strsec * 1)
    document.cookie = name + '=' + escape(value) + 'expires=' + exp.toGMTString()
  },
  // 这是有设定过期时间的使用示例：
  // s20是代表20秒
  // h是指小时，如12小时则是：h12
  // d是天数，30天则：d30
  // 暂时只写了这三种
  getsec: function (str) {
    if (str) {
      var str1 = str.substring(1, str.length) * 1
      var str2 = str.substring(0, 1)
      if (str2 === 's') {
        return str1 * 1000
      } else if (str2 === 'h') {
        return str1 * 60 * 60 * 1000
      } else if (str2 === 'd') {
        return str1 * 24 * 60 * 60 * 1000
      }
    }
  },
  /** 密码策略 */
  pwdtest (str, type, length) {
    length = length || 6
    var bool = !(str.length >= length)
    if (bool) {
      bool = '不符合密码最小长度' + length + '位'
      return bool
    }
    if (type === 1) {
      bool = !(/[a-zA-Z]/.test(str) && /[0-9]/.test(str) && !/[^\w]/.test(str))
      if (bool) {
        bool = '需包含字母和数字字符'
        return bool
      }
    } else if (type === 2) {
      bool = !(/[a-zA-Z]/.test(str) && /[0-9]/.test(str) && /[^\w]/.test(str))
      if (bool) {
        bool = '需包含字母、数字和特殊字符'
        return bool
      }
    } else if (type === 3) {
      bool = !(/[a-z]/.test(str) && /[0-9]/.test(str) && /[A-Z]/.test(str) && !/[^\w]/.test(str))
      if (bool) {
        bool = '需包含数字和大小写字母'
        return bool
      }
    } else if (type === 4) {
      bool = !(/[a-zA-Z]/.test(str) && /[A-Z]/.test(str) && /[0-9]/.test(str) && /[^\w]/.test(str))
      if (bool) {
        bool = '需包含数字、大小写字母和特殊字符'
        return bool
      }
    }
    return false
  },
  /** 密码策略提示 */
  PwdPopover (res) {
    var popover = ''
    res.pwd_length = res.pwd_length || 6
    res.pwd_complex = res.pwd_complex || 0
    if (res.pwd_complex === 0) {
      popover = '密码最小长度至少' + res.pwd_length + '个字符'
    } else if (res.pwd_complex === 1) {
      popover = '密码最小长度至少' + res.pwd_length + '个字符且必须包含字母和数字字符'
    } else if (res.pwd_complex === 2) {
      popover = '密码最小长度至少' + res.pwd_length + '个字符且必须包含字母、数字和特殊字符'
    } else if (res.pwd_complex === 3) {
      popover = '密码最小长度至少' + res.pwd_length + '个字符且必须包含数字和大小写字母'
    } else if (res.pwd_complex === 4) {
      popover = '密码最小长度至少' + res.pwd_length + '个字符且必须包含数字、大小写字母和特殊字符'
    }
    return popover
  },
  /** 音频播放 */
  audioPaly: function (event) {
    var audio = document.getElementsByTagName('audio')
    for (var i = 0; i < audio.length; i++) {
      if (event.target !== audio[i]) {
        audio[i].pause()
      }
    }
  },
  /** 视频播放 */
  videoPaly: function (event) {
    var video = document.getElementsByTagName('video')
    for (var i = 0; i < video.length; i++) {
      if (event.target !== video[i]) {
        video[i].pause()
      }
    }
  },
  /** 自定义字段解析 */
  fieldValueInit (data) {
    var arr = []
    var list
    var value = data.field_value
    if (data.type === 'datetime') {
      return this.formatDate(value, 'yyyy-MM-dd HH:mm')
    } else if (data.type === 'area') {
      value.split(',').map((item) => {
        var area1 = item.split(':')
        arr.push(area1[1])
      })
      return arr.join('')
    } else if (data.type === 'location') {
      return JSON.parse(value).value
    } else if (data.type === 'reference') {
      list = JSON.parse(value)
      list.map((item) => {
        arr.push(item.name)
      })
      return arr.join(';')
    } else if (data.type === 'picklist') {
      list = JSON.parse(value)
      list.map((item) => {
        arr.push(item.label)
      })
      return arr.join('')
    } else if (data.type === 'multi') {
      list = JSON.parse(value)
      list.map((item) => {
        arr.push(item.label)
      })
      return arr.join('')
    } else {
      return value
    }
  },
  /** 获取浏览器相关信息 */
  browserInfo: function () {
    var browser = {
      msie: false,
      chrome: false,
      firefox: false,
      opera: false,
      safari: false,
      name: 'unknown',
      version: 0 /** 正使用浏览器的版本号 **/
    }
    var userAgent = window.navigator.userAgent.toLowerCase() /** 使用正则对用户当前浏览器进行判断 **/
    if (/(msie|chrome|firefox|opera|netscape)\D+(\d[\d.]*)/.test(userAgent)) {
      browser[RegExp.$1] = true
      browser.name = RegExp.$1
      browser.version = RegExp.$2
    } else if (/version\D+(\d[\d.]*).*safari/.test(userAgent)) {
      browser.safari = true
      browser.name = 'safari'
      browser.version = RegExp.$2
    }
    return browser
  },
  _insertimg (str, id) {
    document.getElementById(id).focus()
    var selection = window.getSelection ? window.getSelection() : document.selection
    var range = selection.createRange ? selection.createRange() : selection.getRangeAt(0)
    if (!window.getSelection) {
      range.pasteHTML(str)
      range.collapse(false)
      range.select()
    } else {
      document.getElementById(id).focus()
      selection.addRange(range)
      range.collapse(false)
      var hasR = range.createContextualFragment(str)
      var hasRLastChild = hasR.lastChild
      while (hasRLastChild && hasRLastChild.nodeName.toLowerCase() === 'br' && hasRLastChild.previousSibling && hasRLastChild.previousSibling.nodeName.toLowerCase() === 'br') {
        var e = hasRLastChild
        hasRLastChild = hasRLastChild.previousSibling
        hasR.removeChild(e)
      }
      range.insertNode(hasR)
      if (hasRLastChild) {
        range.setEndAfter(hasRLastChild)
        range.setStartAfter(hasRLastChild)
      }
      selection.removeAllRanges()
      selection.addRange(range)
    }
  },
  /* 处理粘贴图片 */
  pastePicture: function (e, bus) {
    let that = this
    console.log(e)
    var imgReader = function (item) {
      var blob = item.getAsFile()
      var reader = new FileReader()
      var browser = that.browserInfo()
      reader.readAsDataURL(blob)
      reader.onload = function (ev) {
        var img = new Image()
        img.src = ev.target.result
        console.log(browser)
        if (browser.chrome) {
          if (bus) {
            bus.emit('file-basc64', ev.target.result)
          } else {
            that._insertimg('<img src="' + ev.target.result + '" />', 'messageBox2')
          }
        }
      }
    }
    var clipboardData = e.clipboardData
    var i = 0
    var items, item, types
    if (clipboardData) {
      items = clipboardData.items
      if (!items) {
        return
      }
      item = items[0]
      types = clipboardData.types || []
      for (; i < types.length; i++) {
        if (types[i] === 'Files') {
          item = items[i]
          break
        }
      }
      if (item && item.kind === 'file' && item.type.match(/^image\//i)) {
        imgReader(item)
      }
    }
  },
  /* basc64转文件对象 （2018-5-8 后台暂不支持多文件） */
  basc64ToFile: function (img) {
    var formData = new FormData()
    var blobBin = window.atob(img)
    var array = []
    for (var i = 0; i < blobBin.length; i++) {
      array.push(blobBin.charCodeAt(i))
    }
    var blob = new Blob([new Uint8Array(array)], {
      type: 'image/png'
    })
    var file = new File([blob], 'paste' + ((new Date()).getTime()) + '.png', {
      type: 'image/png'
    })
    formData.append('file[]', file)
    console.log(formData)
    return formData
  },
  // 下载方法
  saveFile (data, filename) {
    var saveLink = document.createElementNS('http://www.w3.org/1999/xhtml', 'a')
    saveLink.href = data
    saveLink.download = filename
    var event = document.createEvent('MouseEvents')
    event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null)
    saveLink.dispatchEvent(event)
  },
  // 函数节流
  throttle: function (method, delay) {
    let last = 0
    return function () {
      let now = +new Date()
      console.log(now, 'now')
      if (now - last > delay) {
        method.apply(this, arguments)
        last = now
      }
    }
  },
  // 颜色透明度
  colorOpacity: function (color, opacity) {
    let value = color
    if (color && opacity && opacity !== '不透明') {
      value = color.slice(0, 7) + Number((opacity.slice(0, 1) / 10 * 255).toFixed(0)).toString(16).toUpperCase()
    }
    return value
  },
  // 数字保留x位小数（非四舍五入）
  numberCut: function (value, decimal) {
    let number = ''
    if (value !== undefined && decimal !== undefined) {
      number = Math.floor(value * Math.pow(100, Number(decimal))) / (Math.pow(100, Number(decimal)))
      return number.toFixed(decimal)
    } else {
      return number
    }
  },
  // 数字格式
  numberFormat: function (value, property) {
    if (value !== '' && value !== undefined) {
      if (property.numberType === '0') {
        return utilClass.numberCut(value, property.numberLenth)
      } else if (property.numberType === '2') {
        return utilClass.numberCut(value, property.numberLenth) + ' %'
      } else {
        return utilClass.numberCut(value, 0)
      }
    }
  },
  // 公式格式
  formulsFormat: function (value, property) {
    if (value !== '' && value !== undefined) {
      if (property.numberType === '3') {
        // 文本
        return value
      } else if (property.numberType === '4') {
        // 日期
        return utilClass.formatDate(value, property.decimalLen)
      } else {
        // 数字
        // if (property.numberType === '0') {
        //   return utilClass.numberCut(Number(value), property.decimalLen)
        // } else if (property.numberType === '2') {
        //   return utilClass.numberCut(Number(value), property.decimalLen) + ' %'
        // } else {
        //   return utilClass.numberCut(Number(value), 0)
        // }
        return value
      }
    }
  },
  // 省市区转文本
  areaTo: function (value) {
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
}
export default utilClass
