var AgentId = 1000002
var Secret = '4qSUrmuWmWSmwUIIsTgHOTP1ekW7V89tETIMxdOt1ds'
var url = "http://192.168.1.9:8080/custom-gateway/"
function GetQueryString(name) {
  var reg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)')
  var r = window.location.search.substr(1).match(reg)
  if (r != null) return unescape(r[2])
  return null
}
console.log('code', GetQueryString('code'))
console.log('corpid', GetQueryString('appid'))
console.log('state', GetQueryString('state'))
console.log('AgentId', AgentId)
console.log('Secret', Secret)
$(function () {
  /** 获取 */
  if(GetQueryString('state')){
    code.ddGettoken()
  }else{
    code.xxGettoken()
  }
})
var code = {
  /** 企业微信 */
  xxGettoken: function(){
    $.ajax({
      type: 'get',
      url: url + 'qw/callBackLogin',
      data: { 'code': GetQueryString('code'), 'state': Secret },
      dataType: 'json',
      success: function (data) {
        console.log(data)
        location.href = "/#/login2"
      },
      error: function (data) {
        console.log(JSON.stringify(data))
      }
    })
  },
  /** 钉钉 */
  ddGettoken: function(){
    $.ajax({
      type: 'get',
      url: url + 'dingding/callBackLogin',
      data: { 'code': GetQueryString('code'), 'state': GetQueryString('state') },
      dataType: 'json',
      success: function (data) {
        console.log(data)
        location.href = "/#/login2"
      },
      error: function (data) {
        console.log(JSON.stringify(data))
      }
    })
  },
}