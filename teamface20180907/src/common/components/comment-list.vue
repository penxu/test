<template>
  <div class="comment-list-mian">
    <div style="text-align: center; margin: 10px 0;" v-if="queryCommentData.length > hideNum && ishide">
      <a href="javascript:;" class="fileName" style="color: #1890FF;" @click="moreComment($event)">展开 {{queryCommentData.length - hideNum + 1}} 条更多评论</a>
    </div>
    <div class="item" v-for="(item, index) in queryCommentData" :key="index" v-if="queryCommentData.length - hideNum < index">
      <avatar-component :employeeData="{'id': item.employee_id, 'name': item.employee_name, 'picture': item.picture, 'sign_id': item.sign_id}"></avatar-component>
      <div class="header">{{item.employee_name}}<span class="time">{{item.datetime_time | formatDate('MM-dd HH:mm')}}</span></div>
      <div class="dynamic-content" v-html="traverseEmoji(item.content)"></div>
      <div class="dynamic-content dynamic-file" v-for="(file, index2) in item.information" :key="index2">
        <img v-if="returnType(file.file_type).fileType == 'img'" :class="file.paste == 1 ? 'paste' : 'file'" :src="file.file_url+'&TOKEN='+token" @click="openFilePreview(file)" />
        <i v-if="returnType(file.file_type).fileType != 'img'" :class="file.file_name | ressuffix" class="file iconfont" @click="openFilePreview(file)"></i>
        <a href="javascript:;" class="fileName" @click="openFilePreview(file)">{{file.file_name}}</a>
        <i class="iconfont icon-pc-paper-download download" @click="download(file)"></i>
        <span class="size">{{file.file_size | fileSize}}</span>
      </div>
    </div>
    <div style="margin: 10px 0;" v-if="commentData.type == 1 && !ishide">
      <a href="javascript:;" style="color: #1890FF;" @click="distance(0)">返回顶部</a>
    </div>
    <!-- <div class="item">
      <span class="simpName">容量</span>
      <div class="header">容量  <span class="time">07-17 15:16</span></div>
      <div class="associated">
        <span class="title">关联</span>
        <div class="right">
          <div class="s-item">模块名称 - 第一个字段值</div>
          <div class="s-item">模块名称2 - 第一个字段值</div>
          <div class="s-item">模块名称3 - 第一个字段值</div>
          <div class="s-item">模块名称4 - 第一个字段值</div>
        </div>
      </div>
    </div> -->
  </div>
</template>

<script>
import { HTTPServer } from '@/common/js/ajax.js'
import {plist, emojiUrl, locationEmoji} from '@/common/js/emoji.js'
import tool from '@/common/js/tool.js'
import $ from 'jquery'
import AvatarComponent from '@/common/components/employee-avatar'/** 显示成员信息的头像组件 */
/** commentData id: 关联ID  "bean" 模块标识   parameter: 指定评论需要传的参数 type : 类型 null或者0：默认， 1：自定义详情  style: 文件库导航专用 */
export default {
  props: ['commentData'],
  components: {AvatarComponent},
  data () {
    return {
      responseData: this.commentData,
      visible1: false,
      empDepRoleMultiData2: {},
      data3: [],
      token: '',
      flase: false,
      ishide: true,
      hideNum: 5,
      queryCommentData: [],
      plist: plist,
      emojiUrl: emojiUrl,
      menuId: 'commonComment',
      locationEmoji: locationEmoji,
      pasteObj: {}
    }
  },
  watch: {
    commentData (data) {
      console.log('评论动态', data)
      this.parameter = data.parameter
      if (data.id) {
        this.queryDynamicList()
      }
    }
  },
  /* 页面加载后执行 */
  mounted () {
    console.log(333, this.commentData)
    this.ishide = true
    // this.menuId = 'commonComment' + (new Date().getTime())
    this.token = JSON.parse(sessionStorage.requestHeader).TOKEN
    if (this.commentData.id) {
      this.queryDynamicList()
    }
    this.$bus.off('update-comment-list')
    this.$bus.on('update-comment-list', (value, style) => {
      console.log('update-comment-list', value)
      if (this.commentData.id === value.id && this.commentData.bean === value.bean) {
        this.queryDynamicList(style)
        if (style === 1) {
          this.hideNum++
        }
      }
    })
  },
  methods: {
    /** 返回类型 */
    returnType (type) {
      return tool.determineFileType(type)
    },
    /** 播放处理 */
    audioPaly (event) {
      tool.audioPaly(event)
    },
    /** 播放处理 */
    videoPaly (event) {
      tool.videoPaly(event)
    },
    /** 表情简析 */
    traverseEmoji (params) {
      let list = plist.dict
      for (var i = 0; i < list.length; i++) {
        params = params.replace(new RegExp('\\' + list[i].key, 'ig'), '<img class="emojiImg" src="' + emojiUrl + list[i].string + '.gif">')
      }
      var name = params.match(/@\S+/g)
      if (name) {
        for (var j = 0; j < name.length; j++) {
          params = params.replace(new RegExp('\\' + name[j], 'ig'), '<span class="at">' + name[j] + '</span>')
        }
      }
      return params
    },
    /** 添加 @ 成员 */
    seleteMember () {
      var messageBox = document.getElementById(this.menuId)
      var arr = []
      var atNode = messageBox.getElementsByClassName('at')
      for (var i = 0; i < atNode.length; i++) {
        var empId = atNode[i].getAttribute('data-id')
        var contains = tool.contains(this.data3, 'id', parseInt(empId))
        if (contains) {
          arr.push(this.data3[contains.i])
        }
      }
      this.data3 = arr
      this.$bus.emit('commonMember', {'prepareData': [], 'prepareKey': 'common' + this.commentData.bean + '-' + this.commentData.id, 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': []}) // 给父组件传值
      setTimeout(() => {
        messageBox.focus()
      }, 100)
    },
    /** 评论列表 */
    queryDynamicList (style) {
      if (!this.commentData.id) {
        return
      }
      this.commentData.getlist = false
      var jsondata = { id: this.commentData.id, bean: this.commentData.bean }
      HTTPServer.getQueryCommentDetail(jsondata, 'Loading').then((res) => {
        console.log('评论列表', res)
        this.queryCommentData = res
        this.$bus.emit('getCommentCount', res.length)
        for (var i = 0; i < this.queryCommentData.length; i++) {
          if (this.queryCommentData[i].information) {
            for (var x = 0; x < this.queryCommentData[i].information.length; x++) {
              this.queryCommentData[i].information[x].fileType = tool.determineFileType(this.queryCommentData[i].information[x].file_type)
            }
          }
        }
        if (!this.ishide) {
          this.hideNum = this.queryCommentData.length + 1
        }
        if (style === 1) {
          this.distance(style)
        }
        let menu = $('.comment-list-mian')
        let num = 0
        let fun = setInterval(() => {
          num++
          if (num > 10) window.clearInterval(fun)
          menu[0].scrollTop = menu[0].scrollHeight
        }, 100)
      })
    },
    /** 展开更多 */
    moreComment (event) {
      this.ishide = false
      this.hideNum = this.queryCommentData.length + 1
      let menu = $(event.target).parents('.comment-list-mian')
      let num = 0
      let fun = setInterval(() => {
        num++
        if (num > 10) window.clearInterval(fun)
        console.log('scrollHeight', menu[0].scrollHeight)
        menu[0].scrollTop = menu[0].scrollHeight
      }, 100)
    },
    /** 置顶
     * type : 0 - 置顶   1 - 置底
     */
    distance (type) {
      this.$bus.emit('detail-page-roll', type)
    },
    /** 文件下载 */
    download (data) {
      tool.saveFile(data.file_url + '&TOKEN=' + this.token, data.file_name)
    },
    openFilePreview (file) {
      var data = JSON.parse(JSON.stringify(file))
      data.fileForm = true
      data.file_url = data.file_url + '&TOKEN=' + this.token
      this.$bus.emit('file-preview', data)
    }
  }
}
</script>

<style lang='scss'>
.comment-list-mian{
  height: calc(100% - 150px);margin-bottom: 10px;overflow-y: auto;
  background: #fff;
  .item{margin-bottom: 20px;
    .avatar-mian{float: left;
      >img{height: 30px;width: 30px;border-radius: 50%;float: left;}
    }
    .header{margin-left: 40px;padding-top: 5px;
      font-size: 14px;
      color: #666;
      .time{margin-left: 8px;font-size: 12px;color: #999;}
    }
    .dynamic-content{margin-left: 40px;margin-top: 10px;word-wrap: break-word;
      .file{font-size: 30px;width: 30px;height: 30px;
        float: left;
        line-height: 1;
      }
      .fileName{
        display: inline-block;
        margin: 8px 0 0 5px;
        font-size: 14px;
        color: #69696C;
        line-height: 1.2;
        max-width: calc(100% - 110px);
        white-space: nowrap;
        text-overflow: ellipsis;
        overflow: hidden;
      }
      .paste{max-width: 240px;}
      .emojiImg{width: 30px;}
      .at{color: #409eff;}
      .download{
        float: right;
        font-size: 16px;
        color: #999;
        margin: 6px 5px 0 0;
      }
      .size{
        float: right;
        font-size: 14px;
        color: #999;
        margin: 7px 8px 0 0;
      }
    }
    .dynamic-file{
      background: #F2F2F2;
      border-radius: 2px;
      padding: 5px 10px;
      overflow: hidden;
    }
    .simpName {height: 30px;width: 30px;float: left;line-height: 30px;text-align: center;color: #fff;margin: 0 10px auto 10px;font-size: 12px;}
  }
  .associated{
    display: inline-block;
    overflow: hidden;
    font-size: 14px;
    .title{
      float: left;
      width: 48px;
      text-align: right;
      padding-right: 20px;
      color: #909090;
    }
    .right{
      width: calc(100% - 48px);
      margin-left: 48px;
      .s-item{
        color: #3689E9;
      }
    }
  }
}
</style>
