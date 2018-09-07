const asyncScript = function (array) {
  return new Promise(function (resolve, reject) {
    try {
      let obj
      array.forEach((o, i) => {
        let oldjs = null
        oldjs = document.getElementById(o.id)
        if (!oldjs) {
          var scriptObj = document.createElement('script')
          scriptObj.src = o.url
          scriptObj.type = 'text/javascript'
          scriptObj.id = o.id
          document.getElementsByTagName('head')[0].appendChild(scriptObj)
          if (i === array.length - 1) {
            obj = scriptObj
            obj.onload = function () {
              resolve()
            }
          }
        } else {
          resolve()
        }
      })
    } catch (error) {
      reject(error)
    }
  })
}
export default{
  YS_ueditor (callback = () => {}) {
    asyncScript([{
      id: 'ue_config',
      url: '/static/YS-frame/UE/ueditor.config.js'
    }, {
      id: 'ue_all',
      url: '/static/YS-frame/UE/ueditor.all.js'
    }
    ]).then(() => {
      asyncScript([
        {
          id: 'ue_css',
          url: '/static/YS-frame/UE/lang/zh-cn/zh-cn.js'
        },
        {
          id: 'ue_parse',
          url: '/static/YS-frame/UE/ueditor.parse.min.js'
        }
      ]).then(() => {
        callback()
      })
    })
  },
  YS_wordExport (callback = () => {}) {
    asyncScript([{
      id: 'word_filesaver',
      url: '/static/YS-frame/wordexport/FileSaver.js'
    }, {
      id: 'word_wordexport',
      url: '/static/YS-frame/wordexport/jquery.wordexport.js'
    }
    ]).then(() => {
      callback()
    })
  }
}
