<template>
  <el-dialog :title="item.label" :visible.sync="dialogVisible" width="800px" class="subfrom-popup-warp">
    <!-- 子表单表格显示 -->
    <div v-if="item.field.formStyle==='0'||!item.field.formStyle" class="subform">
      <span class="titles">
        <span v-for="(title,index) in item.componentList" :key="index" v-if="title.field_param.terminalPc === '1'">{{title.label}}</span>
      </span>
      <div class="list" v-for="(subform,index) in dataDetail[item.name]" :key="index">
        <div class="subform-child" v-for="(items,index) in subform" :key="index" :class="getType(index)+'-box'" v-if="showPC(item.componentList, index)">
          <span v-if="getType(index) === 'personnel' || getType(index) === 'department'"  class="personnel">
            <head-image v-for="(child,index) in items" :key="index" :head="child"></head-image>
          </span>
          <span v-else-if="getType(index) === 'datetime'"  v-text="formatDateTo(items,item.componentList, index)"></span>
          <span v-else-if="getType(index) === 'picklist'  || getType(index) === 'mutlipicklist'" :class="{picklist: items.length}">
            <span v-for="(child,index) in items" :key="index" :style="{'background':child.color}" :class="{'white-font':!child.color || child.color === '#FFFFFF'}">{{child.label}}</span>
          </span>
          <span v-else-if="getType(index) === 'multi'">
            <span v-for="(child,index) in items" :key="index">{{child.label}}<small v-if="items.length !== index + 1">/</small></span>
          </span>
          <span v-else-if="getType(index) === 'reference'" class="reference">
            <span v-for="(child,indexs) in items" :key="indexs" @click="childGoReferenceModel(item.componentList, index, child.id)">{{child.name}}</span>
          </span>
          <span v-else-if="getType(index) === 'textarea'" class="textarea" v-html="textareaNewline(items)"></span>
          <div v-else-if="getType(index) === 'multitext'" class="multitext" v-html="items" style="overflow-x: auto;"></div>
          <span v-else-if="getType(index) === 'area'">{{items | areaTo}}</span>
          <span v-else-if="getType(index) === 'location'" v-text="locationShift(items)"></span>
          <span v-else-if="getType(index) === 'attachment'" :class="{attachment: items.length}">
            <file-item v-for="(file,index) in items" :key="index" :file="file" :isDownload="true"></file-item>
          </span>
          <span v-else-if="getType(index) === 'picture'" class="picture">
            <img v-for="(file,index) in items" :key="index" :src="file.file_url+'&TOKEN='+token" @click="openPreview(file)">
          </span>
          <span v-else-if="getType(index) === 'url'" @click="openUrl(items)" class="url">{{items}}</span>
          <span v-else-if="getType(index) === 'number'" v-text="numberTo(items,item.componentList, index)"></span>
          <span v-else-if="getType(index) === 'formula' || getType(index) === 'functionformula' || getType(index) === 'seniorformula'" v-text="formatFormuls(items,item.componentList, index)"></span>
          <span v-else class="textarea">{{items}}</span>
        </div>
      </div>
    </div>
    <div v-else-if="item.field.formStyle==='1'" style="width:100%;">
      <!-- 子表单卡片显示 -->
      <span>
        <div class="subform-box-card" v-for="(subform,key) in dataDetail[item.name]" :key="key">
          <div class="subform-box-card-item" v-for="(title,index1) in item.componentList" :key="index1" v-if="title.field_param.terminalPc === '1'">
            <div class="card-title">
              <span>{{title.label}}</span>
            </div>
            <div style="flex:1;">
              <div v-for="(items,index) in subform" :key="index" :class="getType(index) === 'multitext'?'multitext-card-box':getType(index)+'-box'" v-if="showPC(item.componentList, index)&&title.field===index">
                <div class="card-body">
                  <span v-if="getType(index) === 'personnel' || getType(index) === 'department'"  class="personnel">
                    <head-image v-for="(child,index) in items" :key="index" :head="child"></head-image>
                  </span>
                  <span v-else-if="getType(index) === 'datetime'"  v-text="formatDateTo(items,item.componentList, index)"></span>
                  <span v-else-if="getType(index) === 'picklist'  || getType(index) === 'mutlipicklist'" :class="{picklist: items.length}">
                    <span v-for="(child,index) in items" :key="index" :style="{'background':child.color}" :class="{'white-font':!child.color || child.color === '#FFFFFF'}">{{child.label}}</span>
                  </span>
                  <span v-else-if="getType(index) === 'multi'">
                    <span v-for="(child,index) in items" :key="index">{{child.label}}<small v-if="items.length !== index + 1">/</small></span>
                  </span>
                  <span v-else-if="getType(index) === 'reference'" class="reference">
                    <span v-for="(child,indexs) in items" :key="indexs" @click="childGoReferenceModel(item.componentList, index, child.id)">{{child.name}}</span>
                  </span>
                  <span v-else-if="getType(index) === 'textarea'" class="textarea" v-html="textareaNewline(items)"></span>
                  <div v-else-if="getType(index) === 'multitext'" class="multitext" v-html="items" style="overflow-x: auto;"></div>
                  <span v-else-if="getType(index) === 'area'">{{items | areaTo}}</span>
                  <span v-else-if="getType(index) === 'location'" v-text="locationShift(items)"></span>
                  <span v-else-if="getType(index) === 'attachment'" :class="{attachment: items.length}">
                    <file-item v-for="(file,index) in items" :key="index" :file="file" :isDownload="true"></file-item>
                  </span>
                  <span v-else-if="getType(index) === 'picture'" class="picture">
                    <img v-for="(file,index) in items" :key="index" :src="file.file_url+'&TOKEN='+token" @click="openPreview(file)">
                  </span>
                  <span v-else-if="getType(index) === 'url'" @click="openUrl(items)" class="url">{{items}}</span>
                  <span v-else-if="getType(index) === 'number'" v-text="numberTo(items,item.componentList, index)"></span>
                  <span v-else-if="getType(index) === 'formula' || getType(index) === 'functionformula' || getType(index) === 'seniorformula'" v-text="formatFormuls(items,item.componentList, index)"></span>
                  <span v-else class="textarea">{{items}}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </span>
    </div>
    <!-- <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="dialogVisible = false">确 定</el-button>
    </span> -->
</el-dialog>
</template>
<script>
import FileItem from '@/common/components/file-item'
import HeadImage from '@/common/components/head-image'
import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'
import $ from 'jquery'
export default {
  name: 'SubfromPopup',
  components: {
    FileItem,
    HeadImage
  },
  data () {
    return {
      token: '',
      data: {},
      dialogVisible: false,
      item: {componentList: [], field: {formStyle: ''}},
      dataDetail: {}
    }
  },
  mounted () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.$bus.$on('openSubformPopup', (val) => {
      this.setmultitext(val.dataList)
      this.item = val.subformObj
      this.item.componentList = val.fields
      this.dataDetail[val.subformObj.name] = val.dataList
      console.log(this.item)
      this.dialogVisible = true
    })
  },
  methods: {
    setmultitext (list) {
      let reg = /<img/g
      list.map((v1, k1) => {
        for (let v2 in v1) {
          if (v2.indexOf('multitext') !== -1) { // 子表单富文本添加点击事件
            if (v1[v2] && JSON.stringify(v1[v2]) !== '[]' && JSON.stringify(v1[v2]) !== '{}') {
              v1[v2] = v1[v2].replace(reg, '<img onclick="openMuliteImgsLast(this)" style="cursor:pointer;" class="submultitextInnerImg"')
            }
          }
        }
      })
      setTimeout(() => {
        this.editorImg()
      }, 200)
    },
    editorImg () {
      this.$nextTick(() => {
        // 表格显示-子表单富文本图片缩略图
        let TableImg = $('.multitext-box')
        if (TableImg && TableImg.length > 0) {
          let TableImgIndex = TableImg.length
          for (let i = 0; i < TableImgIndex; i++) {
            $(TableImg[i]).find('.submultitextInnerImg').width(TableImg[i].offsetWidth - 20)
            let TableChildImg = $(TableImg[i]).find('.submultitextInnerImg')
            if (TableChildImg && TableChildImg.length > 0) {
              let TableChildIndex = TableChildImg.length
              for (let j = 0; j < TableChildIndex; j++) {
                if (TableChildImg[j].offsetWidth > TableImg[i].offsetWidth) {
                  $(TableChildImg[j]).width(TableImg[i].offsetWidth - 20)
                }
              }
            }
          }
        }
        // 卡片显示-子表单富文本图片缩略图
        let CardImg = $('.subform-box-card')
        if (CardImg && CardImg.length > 0) {
          let CardImgIndex = CardImg.length
          for (let i = 0; i < CardImgIndex; i++) {
            console.log(CardImg[i])
            let CardChildImg = $(CardImg[i]).find('.submultitextInnerImg')
            if (CardChildImg && CardChildImg.length > 0) {
              let CardChildImgIndex = CardChildImg.length
              for (let j = 0; j < CardChildImgIndex; j++) {
                if ($(CardChildImg[j]).width() > 650) {
                  $(CardChildImg[j]).width(650)
                }
              }
            }
          }
        }
      })
    },
    // 地址组件转换
    locationShift (data) {
      if (data) {
        return data.value
      } else {
        return ''
      }
    },
    // 多行文本转义符
    textareaNewline (data) {
      if (data) {
        data = data.replace(/\n/g, '<br>')
        return data
      }
    },
    // 根据组件类型显示列表字段
    getType (name) {
      let type = name.split('_')[1]
      return type
    },
    // 判断文件类型
    fileType (type) {
      let file = tool.determineFileType(type)
      return file
    },
    // 超链接打开网站
    openUrl (url) {
      let linkurl = ''
      if (url.includes('https://')) {
        linkurl = url
      } else {
        linkurl = 'https://' + url
      }
      window.open(linkurl)
    },
    // 图片打开预览
    openPreview (file) {
      file.fileForm = true
      file.file_url += '&TOKEN=' + this.token
      this.$bus.emit('file-preview', file)
    },
    // 关联关系跳转
    goReferenceModel (item, id) {
      if (id) {
        let data = {
          id: id,
          bean: item.relevanceModule.moduleName
        }
        this.ajaxGetReferenceModuleAuth(data)
      }
    },
    // 子表单关联关系跳转
    childGoReferenceModel (list, name, id) {
      let property
      list.map((item) => {
        if (item.name === name) {
          property = item
        }
      })
      if (id) {
        let data = {
          id: id,
          bean: property.relevanceModule.moduleName
        }
        this.ajaxGetReferenceModuleAuth(data)
      }
    },
    // pc显示
    showPC (list, name) {
      let property
      list.map((item) => {
        if (item.field === name) {
          property = item.field_param.terminalPc
        }
      })
      return property === '1'
    },
    // 数字组件格式
    numberTo (value, list, name) {
      let property
      list.map((item) => {
        if (item.field === name) {
          property = item.field_param
        }
      })
      let number = tool.numberFormat(value, property)
      return number
    },
    // 日期组件格式
    formatDateTo (value, list, name) {
      let property
      list.map((item) => {
        if (item.field === name) {
          property = item.field_param.formatType
        }
      })
      let number = tool.formatDate(value, property)
      return number
    },
    // 公式组件格式
    formatFormuls (value, list, name) {
      if (value) {
        let property
        list.map((item) => {
          if (item.field === name) {
            property = item.field_param
          }
        })
        let number = tool.formulsFormat(value, property)
        return number
      }
    },
    // 展示条形码
    showCode (code) {
      this.ajaxGetBarcodeImg(code)
    },
    // 获取条形码
    ajaxGetBarcodeImg (barcode) {
      let data = {
        bean: this.bean,
        barcodeValue: barcode
      }
      this.$http.getBarcodeImg(data).then((res) => {
        this.barcodeImg = res.barcodePic
      })
    },
    // 判断是否具有进入关联模块权限
    ajaxGetReferenceModuleAuth (data) {
      HTTPServer.getReferenceModuleAuth(data, 'Loading').then((res) => {
        if (res.isAuth) {
          this.$bus.emit('openDataDtl', true, data.id, data.bean)
        } else {
          this.$message({
            showClose: true,
            message: res.reason,
            type: 'warning',
            duration: 1500
          })
        }
      })
    },
    getKeyName (obj) {
      if (obj) {
        for (let i in obj) {
          return i
        }
      }
    }
  },
  filters: {
    toBarcodeImg (code) {
      let url = this.ajaxGetBarcodeImg(code)
      return url
    }
  },
  destroyed () {
    this.$bus.$off('openSubformPopup')
  }
}
</script>
<style lang="scss" scoped>
.subfrom-popup-warp{
  padding:0 5px 10px 5px;
  div.subform{
    width: 100%;
    margin: 8px 0 8px 0;
    overflow: auto;
    border: 1px solid #ddd;
    .titles{
      display: flex;
      width: 100%;
      height: 30px;
      line-height: 30px;
      >span{
        flex: 1;
        min-width: 160px;
        padding: 0 10px;
        border-right:1px solid #E7E7E7;
        background: #F5F5F5;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        &:last-child{
          border-right: none;
        }
      }
    }
    .list{
      display: flex;
      width: 100%;
      line-height: 20px;
      .subform-child{
        flex: 1;
        min-width: 160px;
        min-height: 35px;
        padding: 10px;
        word-wrap: break-word;
        border-top: 1px solid #E7E7E7;
        border-right:1px solid #E7E7E7;
        &:last-child{
          border-right: none;
        }
        span.picklist{
          padding: 10px 0;
          >span{
            position: relative;
            display: inline-block;
            max-width: 220px;
            height: 24px;
            line-height: 24px;
            font-size: 13px;
            color: #FFFFFF;
            padding: 0 8px;
            margin: 0 15px 10px 0 ;
            border-radius: 4px;
            text-align: center;
            vertical-align: middle;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          .white-font{
            color: #5A676E;
            &::after{
              content: ';';
              position: absolute;
              right: 0px;
            }
          }
        }
        >span.personnel{
          display: flex;
          padding: 0;
          .head-image-wrip{
            margin: 0 5px 5px 0;
          }
        }
        span.picture{
          img{
            width: 40px;
            flex: 0 0 40px;
            height: auto;
            margin: 0 15px 15px 0;
            cursor: pointer;
          }
        }
        span.attachment{
          display: inline-block;
          min-width: 265px;
          overflow: auto;
          .file-item-wrip{
            margin: 0 0 10px 0;
          }
        }
        span.textarea{
          line-height: 20px;
        }
        span.url{
          cursor: pointer;
          color: #008FE5;
        }
        span.reference{
          span{
            cursor: pointer;
            color: #008FE5;
          }
        }
      }
      .attachment-box{
        overflow: auto;
      }
      .picklist-box{
        overflow: auto;
      }
    }
  }
  .top-bottom{
    .components{
      display: block;
      line-height: 30px;
      padding: 0 0 10px;
      >label{
        display: block;
      }
      >span{
        display: block;
      }
    }
  }
}
.subfrom-popup-warp{
  .subform-box-card{
    padding:10px;border-radius: 3px;width:100%;border:1px solid #ddd;border-radius:4px;margin-bottom:15px;position: relative;
    .lineLayout{
      padding-bottom:20px;
    }
    .delete-card{
      position: absolute;right:-40px;top:10px;height:24px;line-height: 24px;width:24px;text-align: center;border-radius: 50%;
      box-shadow: 0 2px 4px 0 #C8C8C8;i{font-size:14px;color:#FA5555;}cursor: pointer;
    }
    .subform-box-card-item{
      display:flex;min-height:44px;line-height:44px;
      .card-title{width:100px;overflow:hidden; text-overflow:ellipsis;white-space:nowrap}
      .card-icon{width:30px;}
      .card-body{
        flex:1;
        .url{color:#008FE5;cursor: pointer;}
        .reference{color:#008FE5;span{color:#008FE5;}cursor: pointer;}
        .personnel{
          display: flex;
          >div{margin-right:5px;}
        }
      }
      .upload-card{display: none;}
      .attachment-box {
        .picture{
          position: relative;
          display: inline-block;
          padding: 0 0 0 10px;
          img{
            width: 36px;
            height: 36px;
          }
          i{
            position: absolute;
            right: -5px;
            top: 0;
            font-size: 12px;
            color: #F94C4A;
            cursor: pointer;
          }
        }
        .attachment-show{
          position: relative;
          padding: 0 0 0 10px;
          >i{
            position: absolute;
            left: 35px;
            top: 0;
            font-size: 12px;
            color: #F94C4A;
            cursor: pointer;
          }
        }
      }
      .people-box{
        flex: 1;
        display: flex;
        flex-wrap: wrap;
        padding: 10px 0 0 10px;
        .people-item{
          position: relative;
          margin: 0 10px 10px 0;
          &:hover{
              i.el-icon-circle-close{visibility:visible;}cursor: pointer;
          }
          i.el-icon-circle-close{
            position: absolute;
            visibility: hidden;
            right: -10px;
            top: -5px;
            font-size: 12px;
            color: #F94C4A;
            cursor: pointer;
          }
        }
        .people-add{
          width: 30px;
          height: 32px;
          .iconfont{
            line-height: 32px;
            font-size: 30px;
            color: #ACB8C5;
            cursor: pointer;
          }
        }
      }
      .location-box,.reference-box{
        display: flex;i{cursor:pointer; margin-left:5px;}
      }
    }
  }
}
</style>
<style lang="scss">
.subfrom-popup-warp{
  .subform-box-card .subform-box-card-item{
    .select-group{
      .el-select{margin-right:10px;}
    }
    .el-select,.el-date-editor{width:100%;}
  }
  .el-dialog__header{
    border-bottom:1px solid #ddd;
  }
  .el-dialog__body{
    max-height:600px;padding:20px;
    >div{
      max-height:560px;width: 100%;overflow: auto;
    }
  }
}
</style>
