
const plist = {'dict': [{'key': '[呲牙]', 'string': '1'}, {'key': '[调皮]', 'string': '2'}, {'key': '[汗]', 'string': '3'}, {'key': '[偷笑]', 'string': '4'}, {'key': '[拜拜]', 'string': '5'}, {'key': '[打你]', 'string': '6'}, {'key': '[擦汗]', 'string': '7'}, {'key': '[猪头]', 'string': '8'}, {'key': '[玫瑰]', 'string': '9'}, {'key': '[流泪]', 'string': '10'}, {'key': '[快哭了]', 'string': '11'}, {'key': '[嘘]', 'string': '12'}, {'key': '[酷]', 'string': '13'}, {'key': '[抓狂]', 'string': '14'}, {'key': '[委屈]', 'string': '15'}, {'key': '[屎]', 'string': '16'}, {'key': '[炸弹]', 'string': '17'}, {'key': '[菜刀]', 'string': '18'}, {'key': '[可爱]', 'string': '19'}, {'key': '[色]', 'string': '20'}, {'key': '[害羞]', 'string': '21'}, {'key': '[得意]', 'string': '22'}, {'key': '[吐]', 'string': '23'}, {'key': '[微笑]', 'string': '24'}, {'key': '[发火]', 'string': '25'}, {'key': '[尴尬]', 'string': '26'}, {'key': '[惊恐]', 'string': '27'}, {'key': '[冷汗]', 'string': '28'}, {'key': '[心]', 'string': '29'}, {'key': '[嘴唇]', 'string': '30'}, {'key': '[白眼]', 'string': '31'}, {'key': '[傲慢]', 'string': '32'}, {'key': '[难过]', 'string': '33'}, {'key': '[惊讶]', 'string': '34'}, {'key': '[疑问]', 'string': '35'}, {'key': '[睡]', 'string': '36'}, {'key': '[亲亲]', 'string': '37'}, {'key': '[憨笑]', 'string': '38'}, {'key': '[爱情]', 'string': '39'}, {'key': '[衰]', 'string': '40'}, {'key': '[撇嘴]', 'string': '41'}, {'key': '[奸笑]', 'string': '42'}, {'key': '[奋斗]', 'string': '43'}, {'key': '[发呆]', 'string': '44'}, {'key': '[右哼哼]', 'string': '45'}, {'key': '[抱抱]', 'string': '46'}, {'key': '[坏笑]', 'string': '47'}, {'key': '[企鹅亲]', 'string': '48'}, {'key': '[鄙视]', 'string': '49'}, {'key': '[晕]', 'string': '50'}, {'key': '[大兵]', 'string': '51'}, {'key': '[拜托]', 'string': '52'}, {'key': '[强]', 'string': '53'}, {'key': '[垃圾]', 'string': '54'}, {'key': '[握手]', 'string': '55'}, {'key': '[胜利]', 'string': '56'}, {'key': '[抱拳]', 'string': '57'}, {'key': '[枯萎]', 'string': '58'}, {'key': '[米饭]', 'string': '59'}, {'key': '[蛋糕]', 'string': '60'}, {'key': '[西瓜]', 'string': '61'}, {'key': '[啤酒]', 'string': '62'}, {'key': '[瓢虫]', 'string': '63'}, {'key': '[勾引]', 'string': '64'}, {'key': '[哦了]', 'string': '65'}, {'key': '[手势]', 'string': '66'}, {'key': '[咖啡]', 'string': '67'}, {'key': '[月亮]', 'string': '68'}, {'key': '[匕首]', 'string': '69'}, {'key': '[发抖]', 'string': '70'}, {'key': '[菜]', 'string': '71'}, {'key': '[拳头]', 'string': '72'}]
}

/* <link href="./static/css/style.css" rel="stylesheet"> */
const emojiUrl = './static/img/emoji/'

function traverseEmoji (params) {
  let list = plist.dict
  for (var i = 0; i < list.length; i++) {
    params = params.replace(new RegExp('\\' + list[i].key, 'ig'), '<img class="emojiImg" src="' + emojiUrl + list[i].string + '.gif">')
  }
  return params
}

function locationEmoji (event) {
  console.log(event) // 437
  var mw = 437
  var offsetWidth = event.target.offsetWidth
  var getLeft = event.clientX - event.offsetX
  var getTop = event.clientY - event.offsetY
  var mmWidth = document.body.clientWidth
  console.log(getLeft, getTop, mmWidth, offsetWidth)
  var left = getLeft + (offsetWidth / 2) - (mw / 2) + 40
  var top = getTop - 235
  if (getTop - 225 < 0) {
    top = getTop + 20
  }
  console.log(left, top)
  if (getLeft + mw / 2 > mmWidth) {
    console.log(111111)
    left = mmWidth - 437
  }
  console.log(left, top)
  if (getLeft - mw / 2 < 0) {
    left = 0
  }
  var mmodel = document.getElementsByClassName('emojiForm')
  console.log(mmodel)
  var dialog = mmodel[mmodel.length - 1].childNodes
  dialog[0].style.margin = top + 'px 0 0 ' + left + 'px'
  return event
}

export {plist, emojiUrl, traverseEmoji, locationEmoji}
