<template>
  <div class="layout-detail-wrip">
    <div class="item" v-for="(item,index) in rows" :key="index" :style="{width:item.width}" :class="{'top-bottom':item.field.structure === '0'}" v-if="item.field.terminalPc === '1' && item.field.selectHide !== '1'">
      <div class="components">
        <label :style="{'line-height':item.label.length>6?'18px':'40px'}">{{item.label}}</label>
        <div v-if="item.type === 'personnel' || item.type === 'department'" class="personnel">
          <head-image v-for="(child,index) in dataDetail[item.name]" :key="index" :head="child"></head-image>
        </div>
        <div v-else-if="item.type === 'datetime'">{{dataDetail[item.name] | formatDate(item.field.formatType)}}</div>
        <div v-else-if="item.type === 'picklist' || item.type === 'mutlipicklist'" class="picklist">
          <span v-for="(child,index) in dataDetail[item.name]" :key="index" :style="{'background':child.color}" :class="{'white-font':!child.color || child.color === '#FFFFFF'}">{{child.label}}</span>
        </div>
        <div v-else-if="item.type === 'multi'">
          <span v-for="(child,index) in dataDetail[item.name]" :key="index">{{child.label}}<small v-if="dataDetail[item.name].length !== index + 1">/</small></span>
        </div>
        <div v-else-if="item.type === 'reference'" class="reference">
          <span v-for="(child,index) in dataDetail[item.name]" :key="index" @click="goReferenceModel(item, child.id)">{{child.name}}</span>
        </div>
        <div v-else-if="item.type === 'textarea'" class="textarea" v-html="textareaNewline(dataDetail[item.name])"></div>
        <div v-else-if="item.type === 'multitext'" class="multitext multitext-out-box" v-html="dataDetail[item.name]" style="overflow-x: auto;"></div>
        <div v-else-if="item.type === 'area'">{{dataDetail[item.name] | areaTo}}</div>
        <div v-else-if="item.type === 'location'" class="textarea" v-text="locationShift(dataDetail[item.name])"></div>
        <div v-else-if="item.type === 'attachment'" class="attachment">
          <file-item v-for="(file,index) in dataDetail[item.name]" :key="index" :file="file" :isDownload="true"></file-item>
        </div>
        <div v-else-if="item.type === 'picture'" class="picture">
          <img v-for="(file,index) in dataDetail[item.name]" :key="index" :src="file.file_url+'&TOKEN='+token" @click="openPreview(file)">
        </div>
        <div v-else-if="item.type === 'url'" @click="openUrl(dataDetail[item.name])" class="url">{{dataDetail[item.name]}}</div>
        <div v-else-if="item.type === 'number'">{{dataDetail[item.name] | pointTo(item.field)}}</div>
        <div v-else-if="item.type === 'formula' || item.type === 'functionformula' || item.type === 'seniorformula'">{{dataDetail[item.name] | formulsTo(item.field)}}</div>
        <div v-else-if="item.type === 'barcode'" class="barcode"><span class="code">{{dataDetail[item.name]}}</span>
          <el-popover placement="bottom-end" width="350" trigger="hover" popper-class="barcode" @show="showCode(dataDetail[item.name],item)" v-if="dataDetail[item.name]">
            <div class="qrcode-box">
              <img :src="barcodeImg" :style="{'width':editorImgWidth}">
              <p>使用Teamface APP扫描访问数据</p>
              <el-button type="primary" size='mini'>打印</el-button>
            </div>
            <i class="iconfont icon-tiaoma" slot="reference"></i>
          </el-popover>
          <i class="iconfont icon-tiaoma" v-else></i>
        </div>
        <!-- 子表单表格显示 -->
        <span v-else-if="item.type === 'subform'&&(item.field.formStyle==='0'||!item.field.formStyle)" class="subform">
          <span class="titles">
            <span v-for="(title,index) in item.componentList" :key="index" v-if="title.field.terminalPc === '1'" :style="{'line-height':title.label.length>9?'18px':'40px'}">{{title.label}}</span>
          </span>
          <div class="list" v-for="(subform,index) in dataDetail[item.name]" :key="index">
            <!-- start 此种显示方式或造成表格错位 start -->
            <!-- <div class="subform-child" v-for="(items,index) in subform" :key="index" :class="getType(index)+'-box'" v-if="showPC(item.componentList, index)">
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
            </div> -->
            <!-- end 此种显示方式或造成表格错位 end -->
            <div class="subform-child" v-for="(datanew,indexnew) in item.componentList" :key="indexnew" :class="getType(datanew.name)+'-box'" v-if="showPC(item.componentList, datanew.name)">
              <span v-if="getType(datanew.name) === 'personnel' || getType(datanew.name) === 'department'"  class="personnel">
                <head-image v-for="(child,index) in subform[datanew.name]" :key="index" :head="child"></head-image>
              </span>
              <span v-else-if="getType(datanew.name) === 'datetime'"  v-text="formatDateTo(subform[datanew.name],item.componentList, datanew.name)"></span>
              <span v-else-if="getType(datanew.name) === 'picklist'  || getType(datanew.name) === 'mutlipicklist'" :class="{picklist: subform[datanew.name].length}">
                <span v-for="(child,index) in subform[datanew.name]" :key="index" :style="{'background':child.color}" :class="{'white-font':!child.color || child.color === '#FFFFFF'}">{{child.label}}</span>
              </span>
              <span v-else-if="getType(datanew.name) === 'multi'">
                <span v-for="(child,index) in subform[datanew.name]" :key="index">{{child.label}}<small v-if="subform[datanew.name].length !== index + 1">/</small></span>
              </span>
              <span v-else-if="getType(datanew.name) === 'reference'" class="reference">
                <span v-for="(child,indexs) in subform[datanew.name]" :key="indexs" @click="childGoReferenceModel(item.componentList, datanew.name, child.id)">{{child.name}}</span>
              </span>
              <span v-else-if="getType(datanew.name) === 'textarea'" class="textarea" v-html="textareaNewline(subform[datanew.name])"></span>
              <div v-else-if="getType(datanew.name) === 'multitext'" class="multitext" v-html="subform[datanew.name]" style="overflow-x: auto;"></div>
              <span v-else-if="getType(datanew.name) === 'area'">{{subform[datanew.name] | areaTo}}</span>
              <span v-else-if="getType(datanew.name) === 'location'" v-text="locationShift(subform[datanew.name])"></span>
              <span v-else-if="getType(datanew.name) === 'attachment'" :class="{attachment: subform[datanew.name].length}">
                <file-item v-for="(file,index) in subform[datanew.name]" :key="index" :file="file" :isDownload="true"></file-item>
              </span>
              <span v-else-if="getType(datanew.name) === 'picture'" class="picture">
                <img v-for="(file,index) in subform[datanew.name]" :key="index" :src="file.file_url+'&TOKEN='+token" @click="openPreview(file)">
              </span>
              <span v-else-if="getType(datanew.name) === 'url'" @click="openUrl(subform[datanew.name])" class="url">{{subform[datanew.name]}}</span>
              <span v-else-if="getType(datanew.name) === 'number'" v-text="numberTo(subform[datanew.name],item.componentList, datanew.name)"></span>
              <span v-else-if="getType(datanew.name) === 'formula' || getType(datanew.name) === 'functionformula' || getType(datanew.name) === 'seniorformula'" v-text="formatFormuls(subform[datanew.name],item.componentList, datanew.name)"></span>
              <span v-else class="textarea">{{subform[datanew.name]}}</span>
            </div>
          </div>
        </span>
        <!-- 子表单卡片显示 -->
        <span v-else-if="item.type === 'subform'&&item.field.formStyle==='1'" style="width:100%;">
          <div class="subform-box-card" v-for="(subform,key) in dataDetail[item.name]" :key="key">
            <div class="subform-box-card-item" v-for="(title,index1) in item.componentList" :key="index1" v-if="title.field.terminalPc === '1'">
              <div class="card-title" :style="{'line-height':title.label.length>6?'18px':'40px'}">
                <span>{{title.label}}</span>
              </div>
              <div style="flex:1;">
                <div v-for="(items,index) in subform" :key="index" :class="getType(index) === 'multitext'?'multitext-card-box':getType(index)+'-box'" v-if="showPC(item.componentList, index)&&title.name===index">
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
                      <img v-for="(file,index) in items" :key="index" :src="file.file_url+'&TOKEN='+token" @click="openPreview(file)" :style="{'width':title.field.imageSize&&title.field.imageSize=='30px*30px'?'30px':title.field.imageSize?'60px':'40px'}">
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
        <div v-else class="textarea">{{dataDetail[item.name]}}</div>
      </div>
    </div>
 </div>
</template>

<script>
import FileItem from '@/common/components/file-item'
import HeadImage from '@/common/components/head-image'
import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'LayoutDetail',
  components: {
    FileItem,
    HeadImage
  },
  props: ['rows', 'dataDetail', 'bean'],
  data () {
    return {
      token: '',
      barcodeImg: '',
      editorImgWidth: '340px'
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    console.log(this.dataDetail)
  },
  methods: {
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
      if (name) {
        let type = name.split('_')[1]
        return type
      }
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
        if (item.name === name) {
          property = item.field.terminalPc
        }
      })
      return property === '1'
    },
    // 数字组件格式
    numberTo (value, list, name) {
      let property
      list.map((item) => {
        if (item.name === name) {
          property = item.field
        }
      })
      let number = tool.numberFormat(value, property)
      return number
    },
    // 日期组件格式
    formatDateTo (value, list, name) {
      let property
      list.map((item) => {
        if (item.name === name) {
          property = item.field.formatType
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
          if (item.name === name) {
            property = item.field
          }
        })
        let number = tool.formulsFormat(value, property)
        return number
      }
    },
    // 展示条形码
    showCode (code, item) {
      this.ajaxGetBarcodeImg(code, item)
    },
    // 获取条形码
    ajaxGetBarcodeImg (barcode, item) {
      let data = {
        bean: this.bean,
        barcodeValue: barcode
      }
      this.$http.getBarcodeImg(data).then((res) => {
        this.barcodeImg = res.barcodePic
        if (item.type === 'barcode') {
          if (item.field.codeStyle === 'Data Matrix') {
            this.editorImgWidth = '220px'
          } else {
            this.editorImgWidth = '340px'
          }
        }
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
  }
}
</script>
<style lang="scss" scoped>
.layout-detail-wrip{
  padding:0 5px 10px 5px;
  .item{
    display: inline-block;
    width: 50%;
    padding: 0 10px;
    .components{
      display: flex;
      align-items: center;
      min-height: 44px;
      line-height:44px;
      box-shadow: inset 0 -1px 0 0 #F2F2F2;
      >label{
        flex: 0 0 85px;
        line-height: initial;
        margin: 0 15px 0 0;
        padding: 12px 0;
        text-align:left;
        font-size: 14px;
        color: #6B737B;
      }
      >div{
        display: block;
        flex:1;
        font-size: 14px;
        color: #4A4A4A;
        line-height: initial;
        word-wrap: break-word;
        >span{
          margin: 0 5px 0 0;
        }
      }
      >div.picklist{
        >span{
          position: relative;
          display: inline-block;
          max-width: 220px;
          height: 24px;
          line-height: 24px;
          font-size: 13px;
          color: #FFFFFF;
          padding: 0 8px;
          margin: 0 15px 0 0 ;
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
      >div.textarea {
        padding: 8px 0;
        overflow: auto;
        white-space: normal;
      }
      >div.multitext {
        padding: 8px 0;
      }
      >div.attachment{
        width: 100%;
        padding: 8px 0;
      }
      >div.picture{
        width: 100%;
        padding: 10px 0 0 0;
        img{
          width: 80px;
          height: auto;
          margin: 0 10px 10px 0;
          cursor: pointer;
        }
      }
      >div.reference{
        cursor: pointer;
        color: #008FE5;
      }
      >div.barcode{
        >span.code{
          display: inline-block;
          width: calc(100% - 25px);
          vertical-align: text-top;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        >span{
          margin: 0;
          i{
            vertical-align: text-top;
            font-size: 18px;
            cursor: pointer;
          }
        }
      }
      >div.personnel{
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        padding: 5px 0 0 0;
        .head-image-wrip{
          margin: 0 5px 5px 0;
        }
      }
      >div.url{
        color: #008FE5;
        word-wrap: break-word;
        overflow: auto;
        cursor: pointer;
      }
      >span.subform{
        width: 100%;
        margin: 8px 0 8px 0;
        border:1px solid #E7E7E7;
        overflow: auto;
        .titles{
          display: flex;
          width: 100%;
          min-height: 30px;
          line-height: 30px;
          >span{
            flex: 1;
            min-width: 160px;
            padding: 0 10px;
            border-right:1px solid #E7E7E7;
            background: #F5F5F5;
            word-wrap: break-word;
            word-break: break-all;
            // overflow: hidden;
            // text-overflow: ellipsis;
            // white-space: nowrap;
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
.layout-detail-wrip{
  .subform-box-card{
    padding:10px;border-radius: 3px;width:100%;border:1px solid #ddd;border-radius:4px;margin-bottom:10px;position: relative;
    .lineLayout{
      padding-bottom:20px;
    }
    .delete-card{
      position: absolute;right:-40px;top:10px;height:24px;line-height: 24px;width:24px;text-align: center;border-radius: 50%;
      box-shadow: 0 2px 4px 0 #C8C8C8;i{font-size:14px;color:#FA5555;}cursor: pointer;
    }
    .subform-box-card-item{
      display:flex;margin-bottom:10px;
      .card-title{
        width:100px;
        word-wrap: break-word;
        word-break: break-all;
        // overflow:hidden; text-overflow:ellipsis;white-space:nowrap
      }
      .card-icon{width:30px;}
      .card-body{
        flex:1;
        .url{color:#008FE5;cursor: pointer;}
        .reference{color:#008FE5;span{color:#008FE5;}cursor: pointer;}
        .personnel{
          display: flex;
          >div{
            margin-right:5px;
          }
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
.subform-box-card .subform-box-card-item{
  .select-group{
    .el-select{margin-right:10px;}
  }
  .el-select,.el-date-editor{width:100%;}
}
</style>
