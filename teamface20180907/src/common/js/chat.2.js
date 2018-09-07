import Vue from 'vue'
import tool from '@/common/js/tool.js'
import {HTTPServer, llURL} from '@/common/js/ajax.js' // wsURL
const vues = new Vue()

let WS
let WS0
let wsURL = ''
let usCmdID = 1 // 命令类型ID   uint16_t
let ucVer = 1 // 版本号 uint8_t
let ucDeviceType = 1 // 设备类型     uint8_t
let ucFlag = 1 // 标志位：IM_TYPE_REQUEST---请求   IM_TYPE_RESPONSE---响应 uint8_t
let ServerTimes = 1 // int64_t  //服务器时间戳.由服务器来填充
let clientTimes = ((new Date()).getTime()) // 客户端填充时间戳,微秒, int64_t
let OneselfIMID = 0 // 自己的IMID
let senderID = 0 // 发送者ID号,登录请求包可以不填充. int64_t
let RAND = Math.ceil(Math.random() * 1000)
let receiverID = 1 // 接收者ID.当群发的时候为群ID
let timeInterval = 0
let chatList = []
let employeeInfo
let companyInfo
let myMsg = {}
let loginTime = 1
let wsCommand = 0
let linkNum = 0
let dbName = 'TF_message' // 数据库名
let daVer = 1 // 数据库版本号
let db = '' // 用于数据库对象
let dbData = [] // 用于临时存放数据的数组
let table = 'message' // 用于数据库表

export const chat = {
  /** 关闭 ws */
  chatWSclose (value) {
    console.log('开始关闭...')
    if (WS) {
      WS.close()
      wsCommand = '1005'
      linkNum = 0
    }
  },
  /** 企信模块的发送 */
  send (data, msg) {
    if (msg) myMsg = msg
    console.log(msg)
    WS.send(data)
  },
  // 外部模块发送
  send2 (data, msg) {
    msg.times = clientTimes
    msg.clientTimes = clientTimes
    msg.chatId = ''
    var contains
    usCmdID = data.usCmdID
    receiverID = data.receiverID
    if (data.usCmdID === 5) {
      contains = tool.contains(chatList, 'receiver_id', data, 'receiverID')
    } else {
      contains = tool.contains(chatList, 'id', data, 'receiverID')
    }
    let senddata = this.init_packet(data)
    let newblob
    msg.senderAvatar = employeeInfo.picture
    msg.senderName = employeeInfo.name
    msg.atList = []
    if (contains) {
      msg.chatId = chatList[contains.i].id
      newblob = new Blob([senddata, JSON.stringify(msg)])
      myMsg = {
        msg: msg,
        times: clientTimes,
        sender: OneselfIMID
      }
      WS.send(newblob)
    } else {
      HTTPServer.addSingleChat({'receiverId': data.receiverID}, '').then((res) => {
        msg.chatId = res.id
        newblob = new Blob([senddata, JSON.stringify(msg)])
        myMsg = {
          msg: msg,
          times: clientTimes,
          sender: OneselfIMID
        }
        WS.send(newblob)
      })
    }
    return true
  },
  /** json聊天发送 */
  sendjson (data, packet, obj) {
    data.senderAvatar = employeeInfo.picture
    data.senderName = employeeInfo.name
    var senddata = this.init_packet(packet)
    if (data.chatId === 0) {
      HTTPServer.addSingleChat({'receiverId': obj.sign_id}, '').then((res) => {
        data.chatId = res.id
        let chatblob = new Blob([senddata, JSON.stringify(data)])
        myMsg = {
          msg: data,
          times: clientTimes,
          sender: OneselfIMID
        }
        WS.send(chatblob)
      })
    } else {
      let chatblob = new Blob([senddata, JSON.stringify(data)])
      myMsg = {
        msg: data,
        times: clientTimes,
        sender: OneselfIMID
      }
      WS.send(chatblob)
    }
  },
  /** 存储消息键名 */
  returnMsgId (id) {
    return companyInfo.id + '-' + OneselfIMID + '-' + id
  },
  returnData () {
    return {'list': chatList, 'ServerTimes': ServerTimes, 'timeInterval': timeInterval}
  },
  iscpChat () {
    return (location.href.indexOf('cpChat') >= 0)
  },
  // 连接初始化
  ws_connect (url) { // 连接服务器
    wsURL = url
    linkNum++ // 链接次数
    this.chatWSclose()
    employeeInfo = sessionStorage.userInfo ? (JSON.parse(sessionStorage.userInfo)).employeeInfo : {}
    companyInfo = sessionStorage.userInfo ? (JSON.parse(sessionStorage.userInfo)).companyInfo : {}

    clientTimes = ((new Date()).getTime()) // 客户端填充时间戳,微秒, int64_t
    OneselfIMID = employeeInfo.sign_id || 1 // 自己的IMID
    senderID = employeeInfo.sign_id || 1 // 发送者ID号,登录请求包可以不填充. int64_t
    RAND = Math.ceil(Math.random() * 1000)
    receiverID = 0 // 接收者ID.当群发的时候为群ID
    timeInterval = 0
    usCmdID = 1
    var arr = localStorage.getItem('chatList' + OneselfIMID) || '[]'
    chatList = JSON.parse(arr)
    WS = new WebSocket(wsURL)// 连接socket服务器
    WS.onopen = (data) => { // 连接成功的回调函数
      if (WS.readyState === 1) {
        console.log('ws连接成功!')
      }
      WS.binaryType = 'blob'
      let connentdata = this.init_packet()
      var buf = new ArrayBuffer(52)// 填充登录结构体
      let newblob = new Blob([connentdata, buf])
      WS.send(newblob)// 登录

      this.ws_onmessage()// 监听信息
    }
    WS.onclose = (event) => { // 连接失败的回调函数
      let code = event.code
      if (WS.readyState === 3) {
        console.log('IM 连接已断开')
        if (wsCommand !== '17106' && wsCommand !== '1005' && code === 1006) {
          console.log('准备重新连接...')
          setTimeout(() => {
            if (linkNum < 6) this.ws_connect()
          }, 3000)
        }
      }
    }
  },
  /** 负载通讯
  * net_flag: dataview.getUint32(0, true) 占4字节,固定为十六进制0x51534eba,十进制1364414138
  * state: dataview.getUint8(4, true) 0代表正确返回
  * Imid: this.getVal_64(5, 13, dataview)
  * ServerTime: this.getVal_64(13, 20, dataview)
  */
  loadBalance () {
    employeeInfo = sessionStorage.userInfo ? (JSON.parse(sessionStorage.userInfo)).employeeInfo : {}
    OneselfIMID = employeeInfo.sign_id || 1 // 自己的IMID
    WS0 = new WebSocket(llURL)// 连接负载通讯
    WS0.onopen = (data) => { // 连接成功的回调函数
      if (WS0.readyState === 1) {
        console.log('WS0连接成功!')
        WS0.binaryType = 'blob'
        let historyBuffer = new ArrayBuffer(14)
        let hisoryBlob = new DataView(historyBuffer)
        hisoryBlob.setUint32(0, '0x51534eba', true)
        hisoryBlob.setUint8(4, 0, true)
        this.translate(OneselfIMID, 'Imid', hisoryBlob)
        hisoryBlob.setUint8(13, 1, true)
        let newblob = new Blob([hisoryBlob])
        WS0.send(newblob)
        WS0.onmessage = (event) => {
          WS0.close()
          let newblob = event.data
          let newblob2 = event.data.slice(21, event.data.size)
          let reader = new FileReader()
          reader.readAsArrayBuffer(newblob)
          reader.onload = () => { // 读取blob对象成功的回调函数
            reader.readAsText(newblob2, 'utf-8')
            reader.onload = () => {
              if (reader.result) {
                console.log('WS0路劲返回：', 'wss://' + reader.result)
                this.ws_connect('wss://' + reader.result)
              }
            }
          }
        }
      }
    }
    WS0.onclose = (event) => {
      if (WS0.readyState === 3) {
        console.log('负载通讯 连接已断开')
      }
    }
  },
  /** IM来消息传递 */
  transmit (msg, type, tag1) {
    if (this.iscpChat()) {
      vues.$bus.emit('transmit', {data: msg, type: type, tag1: tag1})
    }
    if (type === 'chatList') {
      localStorage.setItem('chatList' + OneselfIMID, JSON.stringify(msg))
    }
  },
  /** 提示音 */
  audioPlay () {
    var audio = document.getElementById('promptTone')
    audio.play()
  },
  // 监听websocket信息
  ws_onmessage () {
    WS.binaryType = 'blob'
    WS.onmessage = (event) => {
      let newblob1 = event.data.slice(0, 49) // 截取数据包头
      let newblob2 = event.data.slice(49, event.data.size)// 截取实际聊天信息
      let reader = new FileReader()
      reader.readAsArrayBuffer(newblob1)
      reader.onload = () => { // 读取blob对象成功的回调函数
        let dataview = new DataView(reader.result)
        if (dataview.byteLength === 0) {
          return
        }
        console.log('监听websocket信息', dataview.getUint16(8, true))
        let sender = this.getVal_64(21, 28, dataview)/** 接收人 */
        // console.log('ServerTimes', ServerTimes, timeInterval)
        // console.log('oneId', this.getVal_64(0, 7, dataview))
        // console.log('usCmdID', this.getVal_64(8, 9, dataview))
        // console.log('senderID', this.getVal_64(21, 28, dataview))
        // console.log('receiverID', this.getVal_64(29, 36, dataview))
        // console.log('clientTimes', this.getVal_64(37, 44, dataview))
        // console.log('RAND', this.getVal_64(45, 49, dataview))
        // console.log('sender', sender)
        wsCommand = dataview.getUint16(8, true)
        switch (dataview.getUint16(8, true)) {
          case 1 : // 登陆返回包
            let Erreader = new FileReader()
            Erreader.readAsArrayBuffer(newblob2)
            Erreader.onload = () => {
              let ErrerBuf = new DataView(Erreader.result)
              if (ErrerBuf.getInt32(0, true) === 0) {
                console.log('登录成功', WS)
                linkNum = 0
                loginTime = ((new Date()).getTime())
                this.getChatList()
                let ServerTimes2 = this.getVal_64(13, 20, dataview)
                if (ServerTimes2 === 0) {
                  ServerTimes = ((new Date()).getTime()) + timeInterval
                } else {
                  ServerTimes = ServerTimes2
                  timeInterval = ServerTimes2 - ((new Date()).getTime())
                }
                this.transmit({ServerTimes: ServerTimes, timeInterval: timeInterval}, 1)
              }
            }
            break
          case 5 :// 对聊
            reader.readAsText(newblob2, 'utf-8')
            reader.onload = () => {
              let receivemsg = {
                msg: JSON.parse(reader.result),
                times: ((new Date()).getTime()) + timeInterval,
                sender: sender,
                RAND: this.getVal_64(45, 49, dataview),
                clientTimes: this.getVal_64(37, 44, dataview)
              }
              this.audioPlay()
              if (sender !== OneselfIMID) this.statisticalChatList(2, receivemsg.msg.chatId)
              this.addStorage(receivemsg)
              this.updateChatList(receivemsg.msg, 5, sender)
              this.transmit(receivemsg, 5)
            }
            this.translate(OneselfIMID, 'oneselfIMID', dataview)
            usCmdID = 11 // 11返回服务器的命令ID,之前协定好
            dataview.setUint16(8, usCmdID, true)// 修改命令ID
            dataview.setUint8(11, ucDeviceType)
            WS.send(dataview.buffer)
            break
          case 6 :// 群聊
            reader.readAsText(newblob2, 'utf-8')
            reader.onload = () => {
              let receivemsg = {
                msg: JSON.parse(reader.result),
                times: ((new Date()).getTime()) + timeInterval,
                sender: sender,
                RAND: this.getVal_64(45, 49, dataview),
                clientTimes: this.getVal_64(37, 44, dataview)
              }
              console.log('群聊', receivemsg)
              this.audioPlay()
              this.addStorage(receivemsg)
              this.updateChatList(receivemsg.msg, 6)
              this.transmit(receivemsg, 6)
              if (sender !== OneselfIMID) this.statisticalChatList(1, receivemsg.msg.chatId)
            }
            this.translate(OneselfIMID, 'oneselfIMID', dataview)
            usCmdID = 12 //
            dataview.setUint16(8, usCmdID, true)
            dataview.setUint8(11, ucDeviceType)
            WS.send(dataview.buffer)
            break
          case 7: // 推送的信息
            reader.readAsText(newblob2, 'utf-8')
            reader.onload = () => {
              let receivemsg = {
                msg: JSON.parse(reader.result),
                times: ((new Date()).getTime()) + timeInterval,
                sender: sender
              }
              console.log('推送的信息', receivemsg)
              usCmdID = 9
              receiverID = employeeInfo.sign_id
              OneselfIMID = employeeInfo.sign_id
              senderID = sender
              RAND = this.getVal_64(45, 49, dataview)
              clientTimes = this.getVal_64(37, 44, dataview)
              let senddata = this.init_packet()
              let chatblob = new Blob([senddata, JSON.stringify({})])
              WS.send(chatblob)
              var pushType = [2, 3, 4, 5, 7, 8, 9, 25, 26] // 小助手推送类型
              if (pushType.includes(receivemsg.msg.type)) {
                this.audioPlay()
                this.updateChatList(receivemsg.msg, 7)
                this.statisticalChatList(3, receivemsg.msg.assistant_id)
                this.transmit(receivemsg, 7)
              } else {
                this.imSync(receivemsg)
              }
            }
            break
          case 8 :// 群推送
            reader.readAsText(newblob2, 'utf-8')
            reader.onload = () => {
              let receivemsg = {
                msg: JSON.parse(reader.result),
                times: ((new Date()).getTime()) + timeInterval,
                sender: sender,
                RAND: this.getVal_64(45, 49, dataview),
                clientTimes: this.getVal_64(37, 44, dataview)
              }
              console.log('群推送', receivemsg)
              usCmdID = 10
              senderID = sender
              RAND = this.getVal_64(45, 49, dataview)
              receiverID = this.getVal_64(29, 36, dataview)
              clientTimes = this.getVal_64(37, 44, dataview)
              let senddata = this.init_packet()
              let chatblob = new Blob([senddata, JSON.stringify({})])
              WS.send(chatblob)
              this.groupPush(JSON.parse(reader.result))
            }
            break
          case 11: // 单用户消息
            myMsg.clientTimes = this.getVal_64(37, 44, dataview)
            myMsg.ServerTimes = ((new Date()).getTime()) + timeInterval
            myMsg.RAND = this.getVal_64(45, 49, dataview)
            console.log(111, myMsg)
            this.addStorage(myMsg)
            this.updateChatList(myMsg.msg, 11)
            this.transmit(myMsg, 'unread_nums')
            this.transmit(myMsg, 11)
            break
          case 12: // 群用户消息
            myMsg.clientTimes = this.getVal_64(37, 44, dataview)
            myMsg.ServerTimes = ((new Date()).getTime()) + timeInterval
            myMsg.RAND = this.getVal_64(45, 49, dataview)
            this.addStorage(myMsg)
            this.updateChatList(myMsg.msg, 12)
            this.transmit(myMsg, 'unread_nums')
            this.transmit(myMsg, 12)
            break
          case 17: // 错误返回错误代码
            let Erreader2 = new FileReader()
            Erreader2.readAsArrayBuffer(newblob2)
            Erreader2.onload = () => {
              let ErrerBuf2 = new DataView(Erreader2.result)
              var errT = ErrerBuf2.getInt32(0, true)
              if (usCmdID === 6 || usCmdID === 7) {
                myMsg.warning = 1
                this.addStorage(myMsg)
              }
              /**
               * 99 没有登录
               * 100 登录的socket和本次通讯的socket不相等（可能socket创建了多次，也可能账号在别处登录）
               * 101 发送的登录数据包长度不够
               * 102 imid没填充
               * 103
               * 104 imid查询不存在
               * 105 设备类型不识别
               * 106 账号在别的地方登陆,服务器返回给客户端
               * 107 聊天或者推送的接收者IMID redis中不存在
               * 108 聊天或者推送的群id redis中不存在
               * 109 群中没有该成员，不能发送消息
               * 110 群消息替别人发消息(OneselfIMID 和senderID 不相等)
               */
              console.log('im错误类型', errT)
              if (errT === 106) {
                wsCommand += errT.toString()
                // vues.$message.error('您的帐号在另一地点登录，您被迫下线，如非本人操作，请尽快修改密码。')
                // setTimeout(() => {
                //   console.log('跳转登录界面')
                //   vues.$router.push({ path: '/' })
                // }, 3000)
              }
            }
            break
          case 18 :// 个人消息已读
            // let sender1 = this.getVal_64(29, 36, dataview)/** 发送人 */
            this.changeGroupState(this.getVal_64(29, 36, dataview), this.getVal_64(21, 28, dataview), 18)
            this.readSync(this.getVal_64(29, 36, dataview), this.getVal_64(21, 28, dataview), 18)
            break
          case 19 :// 群消息已读
            // sender1 = this.getVal_64(29, 36, dataview)/** 发送群ID */
            this.changeGroupState(this.getVal_64(29, 36, dataview), this.getVal_64(0, 7, dataview), 19)
            this.readSync(this.getVal_64(29, 36, dataview), this.getVal_64(0, 7, dataview), 19)
            break
          case 20: // 拉取历史消息,客户端发送过来
            reader.readAsText(newblob2, 'utf-8')
            reader.onload = () => {
              var msg = reader.result.substring(5, reader.result.length)
              if (msg) {
                let receivemsg = {
                  msg: JSON.parse(msg),
                  times: ((new Date()).getTime()) + timeInterval,
                  sender: sender,
                  RAND: this.getVal_64(45, 49, dataview),
                  clientTimes: this.getVal_64(37, 44, dataview)
                }
                this.addStorage(receivemsg)
                this.transmit(receivemsg, 20)
                console.log('拉取历史消息', receivemsg)
              }
            }
            let reader2 = new FileReader()
            reader2.readAsArrayBuffer(newblob2)
            reader2.onload = () => { // 读取blob对象成功的回调函数
              let dataview2 = new DataView(reader2.result)
              var msgType = dataview2.getUint8(0, true)
              var sumCount = dataview2.getUint16(1, true)
              var nowCount = dataview2.getUint16(3, true)
              console.log('1人,2群：', msgType, '总条数：', sumCount, '当前条数：', nowCount)
              if (sumCount === nowCount) {
                this.transmit((msgType === 2) ? this.getVal_64(29, 36, dataview) : this.getVal_64(21, 28, dataview), 'history', msgType)
              }
            }
            break
          case 21: // 返回个人历史消息
            reader.readAsText(newblob2, 'utf-8')
            reader.onload = () => {
              let receivemsg = {
                msg: JSON.parse(reader.result),
                times: ((new Date()).getTime()) + timeInterval,
                sender: sender,
                RAND: this.getVal_64(45, 49, dataview),
                clientTimes: this.getVal_64(37, 44, dataview)
              }
              this.addStorage(receivemsg)
              this.transmit(receivemsg, 21)
            }
            break
          case 22: // 返回群历史消息
            reader.readAsText(newblob2, 'utf-8')
            reader.onload = () => {
              let receivemsg = {
                msg: JSON.parse(reader.result),
                times: ((new Date()).getTime()) + timeInterval,
                sender: sender,
                RAND: this.getVal_64(45, 49, dataview),
                clientTimes: this.getVal_64(37, 44, dataview)
              }
              this.addStorage(receivemsg)
              this.transmit(receivemsg, 22)
            }
            break
          case 23: // 返回拉取历史消息全部完成
            console.log('返回拉取历史消息全部完成')
            this.transmit(null, 23)
            break
          case 24: // 撤销个人聊天
            this.modifyRevocationReply(0, dataview.getUint16(8, true), sender, this.getVal_64(29, 36, dataview), this.getVal_64(37, 44, dataview), this.getVal_64(45, 49, dataview))
            break
          case 25: // 撤销个人聊天成功 <----服务器返回的成功命令
            this.modifyRevocationReply(1, dataview.getUint16(8, true), sender, this.getVal_64(29, 36, dataview), this.getVal_64(37, 44, dataview), this.getVal_64(45, 49, dataview))
            break
          case 26: // 撤销群聊天成功
            this.modifyRevocationReply(0, dataview.getUint16(8, true), sender, this.getVal_64(29, 36, dataview), this.getVal_64(37, 44, dataview), this.getVal_64(45, 49, dataview))
            break
          case 27: // 撤销群聊天成功  <---服务器返回的成功命令
            this.modifyRevocationReply(1, dataview.getUint16(8, true), sender, this.getVal_64(29, 36, dataview), this.getVal_64(37, 44, dataview), this.getVal_64(45, 49, dataview))
        }
      }
    }
  },
  // 取64位的值
  getVal_64 (startNum, endNum, DataView) { // 获取64位数据
    let data16 = []
    for (let i = startNum; i < endNum; i++) {
      if (i < endNum) {
        let data = DataView.getUint8(i)
        let _str16 = data.toString(16)
        if (_str16.length === 1) {
          _str16 = '0' + _str16
        }
        data16.splice(0, 0, _str16)
      }
    }
    let dataStr = ''
    data16.forEach(element => {
      dataStr = dataStr + element
    })
    return Number('0x' + dataStr)
  },
  // 初始化数据包头
  init_packet (data) {
    if (data) {
      OneselfIMID = data.OneselfIMID || OneselfIMID // 自己的IMID
      senderID = data.senderID || senderID // 发送者ID号,登录请求包可以不填充. int64_t
      receiverID = data.receiverID || receiverID // 接收者ID.当群发的时候为群ID
      usCmdID = data.usCmdID || usCmdID // 命令类型ID   uint16_t
      ucVer = data.ucVer || ucVer // 版本号 uint8_t
      ucDeviceType = data.ucDeviceType || ucDeviceType // 设备类型     uint8_t
      ucFlag = data.ucFlag || ucFlag // 标志位：IM_TYPE_REQUEST---请求   IM_TYPE_RESPONSE---响应 uint8_t
      ServerTimes = data.ServerTimes || ((new Date()).getTime()) + timeInterval // int64_t  //服务器时间戳.由服务器来填充
      clientTimes = data.clientTimes || clientTimes // 客户端填充时间戳,微秒, int64_t
      RAND = data.RAND || RAND
    }
    // 数据包头
    let buffer = new ArrayBuffer(49)
    let headData = new DataView(buffer)
    // 设置自己的IMID
    this.translate(OneselfIMID, 'oneselfIMID', headData)
    // 命令类型
    this.translate(usCmdID, 'usCmdID', headData)
    // 版本号
    this.translate(ucVer, 'ucVer', headData)
    // 设备类型
    this.translate(ucDeviceType, 'ucDeviceType', headData)
    // 标志位
    this.translate(ucFlag, 'ucFlag', headData)
    // 服务器时间戳
    headData.setUint8(21, ServerTimes)
    // 发送者ID
    this.translate(senderID, 'senderID', headData)
    // 接受者ID
    this.translate(receiverID, 'receiverID', headData)
    // 客户端时间戳
    this.translate(clientTimes, 'clientTimes', headData)
    // 信息标识随机数
    this.translate(RAND, 'RAND', headData)
    return headData.buffer // 返回数据包头
  },
  // 写入64位数据
  translate (num, type, headData) {
    let str16 = num.toString(16)
    if (str16.length % 2 !== 0) {
      str16 = '0' + str16
    }
    console.log('translate - str16', str16)
    let arr16 = str16.split('').reverse()
    let len = arr16.length
    let index = 0
    for (let i = 0; i <= len; i++) {
      if (i % 2 === 0 && i !== 0) {
        let data = Number('0x' + `${arr16[i - 1]}` + `${arr16[i - 2]}`)
        switch (type) {
          case 'oneselfIMID':
            headData.setUint8(0 + index, data) // 设置自己的IMID
            index++
            break
          case 'usCmdID':
            headData.setUint8(8 + index + index, data)
            index++
            break
          case 'ucVer':
            headData.setUint8(10 + index, data)
            index++
            break
          case 'ucDeviceType':
            headData.setUint8(11 + index, data)
            index++
            break
          case 'ucFlag':
            headData.setUint8(12 + index, data)
            index++
            break
          case 'senderID':
            headData.setUint8(21 + index, data)
            index++
            break
          case 'receiverID':
            headData.setUint8(29 + index, data)
            index++
            break
          case 'clientTimes':
            headData.setUint8(37 + index, data)
            index++
            break
          case 'RAND':
            headData.setUint8(45 + index, data)
            index++
            break
          case 'setId':
            headData.setUint8(1 + index, data)
            index++
            break
          case 'setTimeStamp':
            headData.setUint8(9 + index, data)
            index++
            break
          case 'Imid':
            headData.setUint8(5 + index, data)
            index++
            break
        }
      }
    }
  },
  /** 获取回话列表 */
  getChatList () {
    var total = 0
    if (chatList.length > 0) {
      chatList.map((item) => {
        total += (item.unread_nums || 0)
      })
      vues.$bus.emit('chat-unread-nums', total) /** 总计未读数 */
      this.transmit(chatList, 'chatList', 0) /** 更新chat页面会话列表 */
    } else {
      HTTPServer.getListInfo({}, '').then((res) => {
        for (var i = 0; i < res.length; i++) {
          res[i].latest_push_time = res[i].latest_push_time || res[i].update_time
          if (res[i].chat_type === 3) {
            res[i].unread_nums = res[i].unread_nums || 0
          }
          total += (res[i].unread_nums || 0)
        }
        vues.$bus.emit('chat-unread-nums', total) /** 总计未读数 */
        chatList = res
        localStorage.setItem('chatList' + OneselfIMID, JSON.stringify(res))
        this.transmit(res, 'chatList', 0) /** 更新chat页面会话列表 */
      })
    }
  },
  /** 本地更新会话列表（来消息） */
  updateChatList (data, type, sender) {
    console.log('本地更新会话列表（来消息）', data)
    var jsondata = {}
    if (type === 7) {
      /** 小助手 */
      let myVar = setInterval(() => {
        if (chatList.length > 0) {
          var contains2 = tool.contains(chatList, 'id', data, 'assistant_id')
          if (contains2) {
            chatList[contains2.i].latest_push_time = new Date().getTime()
            if (contains2.is_hide === '1') {
              this.hideSession({'id': contains2.id, 'status': '0', 'chat_type': contains2.chat_type})
            }
            chatList[contains2.i].is_hide = '0'
            this.transmit(chatList, 'chatList')
          } else {
            jsondata = {'application_id': data.data_id, 'chat_type': 3, 'create_time': data.create_time, 'icon_color': data.icon_color, 'icon_type': data.icon_type, 'icon_url': data.icon_url, 'id': data.assistant_id, 'is_hide': '0', 'latest_push_time': new Date().getTime(), 'name': data.bean_name_chinese, 'top_status': '0', 'type': 1, 'unread_nums': 1, 'update_time': data.create_time}
            chatList.unshift(jsondata)
            this.transmit(chatList, 'chatList')
          }
          clearInterval(myVar)
        }
      }, 1000)
    } else {
      var myVar2 = setInterval(() => {
        if (chatList.length > 0) {
          window.clearTimeout(myVar2)
          var contains = tool.contains(chatList, 'id', data, 'chatId')
          console.log(contains)
          if (contains) {
            chatList[contains.i].latest_push_time = new Date().getTime()
            if (contains.is_hide === '1') {
              chatList[contains.i].is_hide = '0'
              this.hideSession({'id': contains.id, 'status': '0', 'chat_type': contains.chat_type})
            }
            this.transmit(chatList, 'chatList')
          } else {
            if (type === 5) {
              jsondata = {'is_hide': '0', 'chat_type': 2, 'receiver_id': sender, 'top_status': '0', 'employee_name': data.senderName, 'id': data.chatId, 'no_bother': '0', 'picture': data.senderAvatar}
              jsondata.latest_push_time = new Date().getTime()
              chatList.unshift(jsondata)
              this.transmit(chatList, 'chatList')
            } else {
              HTTPServer.getGroupInfo({'groupId': data.chatId}, '').then((res) => {
                res.groupInfo.latest_push_time = new Date().getTime()
                chatList.unshift(res.groupInfo)
                this.transmit(chatList, 'chatList')
              })
            }
          }
        }
      }, 1000)
    }
  },
  /** 本地更新会话列表（根据对象更新-添加） */
  updateChatList2 (data) {
    var contains = tool.contains(chatList, 'id', data, 'id')
    var jsondata = {}
    if (contains) {
      if (contains.is_hide === '1') {
        data.is_hide = '0'
        this.hideSession({'id': jsondata.id, 'status': '0', 'chat_type': jsondata.chat_type})
      }
      jsondata = chatList[contains.i]
      jsondata.latest_push_time = new Date().getTime()
      jsondata.is_hide = '0'
      chatList[contains.i] = jsondata
      this.transmit(chatList, 'chatList')
    }
  },
  /** 本地更新会话列表（根据对象更新-替换） */
  updateChatList3 (data) {
    chatList = data
  },
  /** 添加本地数据 */
  addStorage (data) {
    var msgId = companyInfo.id + '-' + OneselfIMID + '-' + data.msg.chatId
    var IMessage = JSON.parse(localStorage.getItem(msgId)) || {}
    data.isRead = []
    data.id = 'im' + data.RAND + data.clientTimes
    IMessage[data.id] = data
    localStorage.setItem(msgId, JSON.stringify(IMessage))
  },
  /** 读取本地 */
  readStorage (chatId) {
    var msgId = companyInfo.id + '-' + OneselfIMID + '-' + chatId
    ServerTimes = ((new Date()).getTime()) + this.timeInterval
    var IMessage = JSON.parse(localStorage.getItem(msgId)) || {}
    var arr = []
    for (var key in IMessage) {
      IMessage[key].timeText = tool.formatDate(IMessage[key].clientTimes, 'yyyy-MM-dd HH:mm:ss')
      arr.push(IMessage[key])
    }
    return arr.sort(tool.compare('clientTimes'))
  },
  /** 修改回话隐藏-显示 */
  hideSession (jsondata, data) {
    if (!jsondata.id) {
      return
    }
    HTTPServer.hideSessionWithStatus(jsondata, false).then((res) => {})
  },
  /** 修改撤销命令 本地内容 */
  modifyRevocationReply (type, usCmdID, senderID, receiverID, clientTimes, RAND) {
    let msg
    if (OneselfIMID === senderID) {
      msg = '你撤回了一条消息'
    } else if (usCmdID === 24) {
      msg = '对方撤回了一条消息'
    }
    let chatId
    if (usCmdID === 24 || usCmdID === 25) {
      var contains = tool.contains(chatList, 'receiver_id', (type === 1) ? receiverID : senderID)
      if (contains) {
        chatId = chatList[contains.i].id
      }
    } else {
      chatId = receiverID
    }
    let msgId = companyInfo.id + '-' + OneselfIMID + '-' + chatId
    let IMessage = JSON.parse(localStorage.getItem(msgId))
    if (IMessage) {
      var im = IMessage['im' + RAND + clientTimes]
      if (im) {
        im.msg.type = 7
        im.msg.msg = (usCmdID === 26 && !msg) ? im.msg.senderName + '撤回了一条消息' : msg
      }
      IMessage['im' + RAND + clientTimes] = im
      localStorage.setItem(msgId, JSON.stringify(IMessage))
    }
    this.transmit({usCmdID: usCmdID, senderID: senderID, receiverID: receiverID, clientTimes: clientTimes, RAND: RAND}, usCmdID)
  },
  /** 添加单聊 */
  addSingleChat (data) {
    console.log('添加单聊', data)
    var contains = tool.contains(chatList, 'receiver_id', data, 'sign_id')
    if (contains) {
      chatList[contains.i].latest_push_time = new Date().getTime()
      if (contains.is_hide === '1') {
        this.hideSession({'id': contains.id, 'status': '0', 'chat_type': contains.chat_type})
      }
      chatList[contains.i].is_hide = '0'
      this.transmit(chatList, 'chat_object', chatList[contains.i]) /** 更新chat页面会话列表 */
    } else {
      HTTPServer.addSingleChat({'receiverId': data.sign_id}, '').then((res) => {
        res.latest_push_time = new Date().getTime()
        chatList.unshift(res)
        this.transmit(chatList, 'chat_object', res) /** 更新chat页面会话列表 */
      })
    }
  },
  /** 修改群已读未读状态 */
  changeGroupState (receiverId, sendId, type) {
    var chatId = (type === 18) ? 'receiver_id' : 'id'
    var contains = tool.contains(chatList, chatId, receiverId)
    if (!contains) {
      return
    }
    var msgId = this.returnMsgId(contains.id)
    let IMessage = JSON.parse(localStorage.getItem(msgId))
    for (var key in IMessage) {
      if (IMessage[key].isRead) {
        if (IMessage[key].isRead.indexOf(receiverId) < 0 && type === 18) {
          IMessage[key].isRead.push(receiverId)
        } else if (IMessage[key].isRead.indexOf(sendId) < 0 && type === 19) {
          IMessage[key].isRead.push(sendId)
        }
      }
    }
    localStorage.setItem(msgId, JSON.stringify(IMessage))
    this.transmit({receiverID: receiverId, id: contains.id, 'sendId': sendId}, type)
  },
  readSync (receiverId, sendId, type) {
    var contains = tool.contains(chatList, (type === 18) ? 'receiver_id' : 'id', (type === 18) ? sendId : receiverId)
    console.log('type', type, 'receiverId', receiverId, 'sendId', sendId, 'contains', contains)
    if ((type === 18 && OneselfIMID === receiverId && sendId === contains.receiver_id) || (type === 19 && OneselfIMID === sendId && receiverId === contains.id)) {
      chatList[contains.i].unread_nums = 0
      this.transmit(chatList, 'chatList') /** 更新chat页面会话列表 */
    }
  },
  /** 更新回话列表数据 (现：群数据) */
  updateChatData (list) {
    chatList = JSON.parse(JSON.stringify(list))
    localStorage.setItem('chatList' + OneselfIMID, JSON.stringify(list))
  },
  /** 添加自己未读数 1:群 2：单人 */
  statisticalChatList (type, id) {
    var total = 0
    if (type === 1 || type === 2 || type === 3) {
      chatList.map((item, index) => {
        if (item.id === id) {
          var num2 = item.unread_nums
          chatList[index].unread_nums = num2 ? num2 + 1 : 1
          chatList[index].is_hide = '0'
        }
        total += parseInt(chatList[index].unread_nums || 0)
      })
    }
    vues.$bus.emit('chat-unread-nums', total) /** 总计未读数 */
    this.transmit(chatList, 'chatList') /** 更新chat页面会话列表 */
  },
  /** 移除列表未读数 */
  emptyChatListNum (type, id) {
    var total = 0
    if (type === 1 || type === 2 || type === 3) {
      chatList.map((item, index) => {
        if (item.id === id) {
          delete chatList[index].unread_nums
        }
        total += parseInt(chatList[index].unread_nums || 0)
      })
    }
    vues.$bus.emit('chat-unread-nums', total) /** 总计未读数 */
    this.transmit(chatList, 'chatList') /** 更新chat页面会话列表 */
  },
  /** 群操作推送--相关处理 */
  groupOperate (data) {
    /** 下线通知 type = 1001 */
    if (data.type === 1001) {
      /** 阻止离线消息下线， 暂定 5s */
      if ((new Date().getTime()) - loginTime > 5000) {
        this.chatWSclose(0)
        setTimeout(() => {
          console.log('下线通知...')
          vues.$router.push({ path: '/' })
        }, 2000)
      }
    } else {
      var contains = tool.contains(chatList, 'id', data, 'group_id')
      if (contains) {
        HTTPServer.getGroupInfo({'groupId': data.group_id}, '').then((res) => {
          chatList[contains.i] = res.groupInfo
          this.transmit(res, 8) /** 更新chat页面会话列表 */
        })
      }
    }
  },
  /** 清除回话内容和未读数 */
  clearReply (id) {
    let msgId = companyInfo.id + '-' + OneselfIMID + '-' + id
    localStorage.removeItem(msgId)
    var total = 0
    chatList.map((item, index) => {
      if (item.id === id) {
        delete chatList[index].unread_nums
      }
      total += parseInt(chatList[index].unread_nums || 0)
    })
    vues.$bus.emit('chat-unread-nums', total) /** 总计未读数 */
    this.transmit(chatList, 'chatList') /** 更新chat页面会话列表 */
    console.log('清空成功！')
  },
  /** 保存助手消息 */
  saveAssistantData (data) {
    var assistant = localStorage.getItem('assistant' + data.id)
    if (!assistant[data.msg.id]) {
      assistant[data.msg.id] = data
      localStorage.setItem('assistant' + data.id, assistant)
    }
  },
  /** 修改助手消息 */
  editAssistantData (data) {
    var assistant = localStorage.getItem('assistant' + data.id)
    if (assistant[data.msg.id]) {
      localStorage.setItem('assistant' + data.id, assistant)
    }
  },
  /** 获取助手消息 */
  getAssistantData (data) {
    var assistant = localStorage.getItem('assistant' + data.id)
    var arr = []
    for (var key in assistant) {
      if (assistant[key]) {
        arr.push(assistant[key])
      }
    }
    return arr
  },
  /** 移除 助手消息 */
  delAssistantData (data) {
    localStorage.removeItem('assistant' + data.id)
  },
  /** 消息同步接收 */
  imSync (data) {
    console.log('imSync', data)
    var type = data.msg.type
    var contains
    var total = 0
    if (type === 1) {
      contains = tool.contains(chatList, 'id', data.msg.group_id)
      if (contains) {
        this.clearReply(contains.id)
        chatList.splice(contains.i, 1)
        this.transmit(chatList, 'chatList', data.msg)
      }
    } else if (type === 15) { // 小助手置顶/取消置顶
      contains = tool.contains(chatList, 'id', data.msg.assistant_id)
      if (contains) {
        chatList[contains.i].top_status = (contains.top_status === '1') ? '1' : '0'
        chatList[contains.i].latest_push_time = new Date().getTime()
      }
      this.transmit(chatList, 'chatList')
    } else if (type === 16) { // 读单条消息
      this.transmit(data, 7)
      chatList.map((item, index) => {
        if (item.id === data.msg.assistant_id) {
          chatList[index].unread_nums--
        }
        total += parseInt(chatList[index].unread_nums || 0)
      })
      vues.$bus.emit('chat-unread-nums', total) /** 总计未读数 */
    } else if (type === 17) { // 读全部消息
      this.transmit(data, 7)
      chatList.map((item, index) => {
        if (item.id === data.msg.assistant_id) {
          chatList[index].unread_nums = 0
        }
        total += parseInt(chatList[index].unread_nums || 0)
      })
      vues.$bus.emit('chat-unread-nums', total) /** 总计未读数 */
    } else if (type === 18) { // 免打扰
    } else if (type === 19) { // 只查看未读消息
    } else if (type === 20) { // 只查看未读消息
      this.transmit(data, 7)
    }
  },
  /** 群推送 */
  groupPush (data) {
    var contains
    if (data.type === 1002) {
      console.log('修改应用', data)
      contains = tool.contains(chatList, 'id', data, 'assistant_id')
      if (contains) {
        chatList[contains.i].icon_url = data.icon_url
        chatList[contains.i].icon_type = data.icon_type
        chatList[contains.i].icon_color = data.icon_color
        chatList[contains.i].name = data.application_name
        this.transmit(chatList, 'chatList', data) /** 更新chat页面会话列表 */
      }
      console.log('修改hou', chatList)
    } else if (data.type === 1003) {
      this.transmit(data, 8, data.type.toString())
    } else if (data.type === 1004) {
      contains = tool.contains(chatList, 'id', data, 'assistant_id')
      if (contains) {
        chatList.splice(contains.i, 1)
        this.transmit(chatList, 'chatList', data) /** 更新chat页面会话列表 */
      }
    } else if (data.type === 1005) { // 模块禁用、启用
      contains = tool.contains(chatList, 'id', data, 'assistant_id')
      if (contains) {
        chatList[contains.i].is_hide = (data.style === 1) ? '0' : '1'
        this.transmit(chatList, 'chatList') /** 更新chat页面会话列表 */
      }
    } else {
      this.groupOperate(data)
    }
  },
  /** 改变聊天对象 */
  editChatObj (data) {
    this.addSingleChat(data, 'obj')
  },

  /**
   * indexeddb
   */
  init_table (id) {
    table = id
    console.log(11111111, table)
  },
  openDB: function (imid) {
    var request = indexedDB.open(dbName + '_' + imid, daVer)
    request.onsuccess = function (e) {
      db = e.target.result
      console.log('连接数据库成功')
      // 数据库连接成功后渲染表格
    }
    request.onerror = function () {
      console.log('连接数据库失败')
    }
    request.onupgradeneeded = function (e) {
      db = e.target.result
      // 如果不存在Users对象仓库则创建
      if (!db.objectStoreNames.contains(table)) {
        var store = db.createObjectStore(table, {keyPath: 'id', autoIncrement: true})
        store.createIndex('index', 'obj', {unique: false})
      }
    }
  },
  saveData: function (data) {
    var tx = db.transaction(table, 'readwrite')
    var store = tx.objectStore(table)
    var req = store.put(data)
    req.onsuccess = function (res) {
      console.log('成功保存id为' + res.result + '的数据')
    }
  },
  deleteOneData: function (id) {
    var tx = db.transaction(table, 'readwrite')
    var store = tx.objectStore(table)
    var req = store.delete(id)
    req.onsuccess = function () {
      // 删除数据成功之后重新渲染表格
      this.getData()
    }
  },
  searchData: function (callback) {
    var tx = db.transaction(table, 'readonly')
    var store = tx.objectStore(table)
    var range = IDBKeyRange.lowerBound(1)
    var req = store.openCursor(range, 'next')
    // 每次检索重新清空存放数据数组
    dbData = []
    req.onsuccess = function (cursor) {
      if (cursor) {
        // 把检索到的值push到数组中
        dbData.push(cursor.value)
        cursor.continue()
      } else {
        // 数据检索完成后执行回调函数
        callback && callback()
      }
    }
  }

}
export default chat
