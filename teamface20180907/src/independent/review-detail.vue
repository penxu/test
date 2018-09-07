<template>
  <el-container class="data-detail">
    <el-header>
      <!-- <el-button type="primary" size="mini">编辑</el-button> -->
      <span v-if="isArea" class="headerTitle">{{detailTitle | areaTo}}</span>
      <span v-else class="headerTitle">{{detailTitle}}</span>
      <i class="el-icon-close" @click="closeDialog"></i>
    </el-header>
    <el-main>
      <!-- <div class="title" v-if="isArea">
        {{detailTitle | areaTo}}
      </div>
      <div class="title" v-else>
        {{detailTitle}}
      </div> -->
      <div class="reference-show">
        <span class="name">基本资料</span>
        <el-dropdown trigger="click" placement="bottom">
          <span class="el-dropdown-link">
            <i class="icon-dian-shu iconfont"></i>
          </span>
          <el-dropdown-menu slot="dropdown"  class="more-handle">
            <el-dropdown-item :command="{id:-1,type:-1}">动态</el-dropdown-item>
            <el-dropdown-item :command="{id:0,type:0}">转移负责人</el-dropdown-item>
            <el-dropdown-item :command="{id:1,type:1}">复制</el-dropdown-item>
            <el-dropdown-item :command="{id:2,type:2}">转换</el-dropdown-item>
            <el-dropdown-item :command="{id:3,type:3}">删除</el-dropdown-item>
            <el-dropdown-item :command="{id:4,type:4}">共享</el-dropdown-item>
            <el-dropdown-item :command="{id:5,type:5}">打印</el-dropdown-item>
            <el-dropdown-item :command="{id:6,type:10}" >数据链接</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
        <el-button type="primary" size="mini">编 辑</el-button>
      </div>
      <div class="scroll-box">
        <custom-header-img :datas="headerImg"></custom-header-img>
        <el-collapse v-model="columOpen" >
          <el-collapse-item v-for="(colum,index) in layout" :key="index" :name="colum.name" :class="{'hide-colum':colum.isHideColumnName === '1'}" v-if="colum.isHideInDetail !== '1'">
            <template slot="title" >
              <i class="icon-pc-paper-more iconfont"></i>
              <span>{{colum.title}}</span>
            </template>
            <div class="colum-box">
              <div class="item" v-for="(item,index) in colum.rows" :key="index" :style="{width:item.width}" :class="{'top-bottom':item.field.structure === '0'}">
                <div class="components">
                  <label>{{item.label}}</label>
                  <span v-if="item.type === 'personnel' || item.type === 'department'"><span v-for="(child,index) in dataDetail[item.name]" :key="index">{{child.name}}</span></span>
                  <span v-else-if="item.type === 'datetime'">{{dataDetail[item.name] | formatDate(item.field.formatType)}}</span>
                  <span v-else-if="item.type === 'picklist' || item.type === 'mutlipicklist'">
                    <span class="picklist" v-for="(child,index) in dataDetail[item.name]" :key="index" :style="{'background':child.color}" :class="{'white-font':child.color === '#FFFFFF'}">{{child.label}}</span>
                  </span>
                  <span v-else-if="item.type === 'multi'">
                    <span v-for="(child,index) in dataDetail[item.name]" :key="index">{{child.label}}</span>
                  </span>
                  <span v-else-if="item.type === 'reference'">
                    <span v-for="(child,index) in dataDetail[item.name]" :key="index">{{child.name}}</span>
                  </span>
                  <span v-else-if="item.type === 'multitext'" class="multitext" v-html="dataDetail[item.name]"></span>
                  <span v-else-if="item.type === 'textarea'" class="textarea">{{dataDetail[item.name]}}</span>
                  <span v-else-if="item.type === 'area'">{{dataDetail[item.name] | areaTo}}</span>
                  <span v-else-if="item.type === 'location'" v-text="locationShift(dataDetail[item.name])"></span>
                  <span v-else-if="item.type === 'picture' || item.type === 'attachment'" class="picture">
                    <div class="item" v-for="(file,index) in dataDetail[item.name]" :key="index">
                      <img :src="file.file_url+'&TOKEN='+token" alt="" v-if="fileType(file.file_type).fileType === 'img'">
                      <i class="iconfont" v-else :class="fileType(file.file_type).icon"></i>
                      <div class="content">
                        <div class="title">{{file.file_name}}</div>
                        <div class="detail">
                          <span class="name">{{file.upload_by | limitTo(-2)}}</span>
                          <span class="date">{{file.upload_time | formatDate('yyyy-MM-dd')}}</span>
                          <span class="size">{{file.file_size | fileSize(file.file_size)}}</span>
                        </div>
                      </div>
                    </div>
                  </span>
                  <span v-else-if="item.type === 'subform'" class="subform">
                    <div class="titles">
                      <span v-for="(title,index) in item.componentList" :key="index" v-if="title.type !== 'formula' && title.type !== 'functionformula' && title.type !== 'seniorformula'">{{title.label}}</span>
                    </div>
                    <div class="list" v-for="(subform,index) in dataDetail[item.name]" :key="index">
                      <div class="subform-child" v-for="(items,index) in subform" :key="index">
                        <span v-if="getType(index) === 'picklist'">
                          <span class="picklist" v-for="(child,index) in items" :key="index" :style="{'background':child.color}" :class="{'white-font':child.color === '#FFFFFF'}">{{child.label}}</span>
                        </span>
                        <span v-else-if="getType(index) === 'personnel' || getType(index) === 'department'">
                          <span v-for="(child,index) in items" :key="index">{{child.name}}</span>
                        </span>
                        <span v-else-if="getType(index) === 'datetime'">{{items | formatDate('yyyy-MM-dd')}}</span>
                        <span v-else-if="getType(index) === 'location'">{{items.value}}</span>
                        <span v-else-if="getType(index) === 'reference'">{{items[0].name}}</span>
                        <span v-else-if="getType(index) === 'multitext'" class="multitext" v-html="items"></span>
                        <span v-else-if="getType(index) === 'picture' || getType(index) === 'attachment'" class="picture">
                          <div class="item" v-for="(file,index) in items" :key="index">
                            <img :src="file.file_url+'&TOKEN='" alt="" v-if="fileType(file.file_type).fileType === 'img'">
                            <i class="iconfont" v-else :class="fileType(file.file_type).icon"></i>
                            <div class="content">
                              <div class="title">{{file.file_name}}</div>
                              <div class="detail">
                                <span class="name">{{file.upload_by | limitTo(-2)}}</span>
                                <span class="date">{{file.upload_time | formatDate('yyyy-MM-dd')}}</span>
                                <span class="size">{{file.file_size | fileSize(file.file_size)}}</span>
                              </div>
                            </div>
                          </div>
                        </span>
                        <span v-else>{{items}}</span>
                      </div>
                    </div>
                  </span>
                  <span v-else-if="item.type === 'url'" @click="openUrl(dataDetail[item.name])" class="url">{{dataDetail[item.name]}}</span>
                  <span v-else>{{dataDetail[item.name]}}</span>
                </div>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
      <!-- <module-other :bean="bean" :dataId="dataId" :show="otherShow"></module-other> -->
    </el-main>
  </el-container>
</template>

<script>
// import ModuleOther from './module-other'
import tool from '@/common/js/tool.js'
import CustomHeaderImg from '@/common/components/custom-header-img'
export default {
  name: 'ReviewDetail',
  components: {
    CustomHeaderImg
  },
  props: ['data'],
  data () {
    return {
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      openReference: false,
      activeName: 'basic',
      headerImg: {},
      dataDetail: this.data.data,
      firstField: '',
      referenceList: [],
      checkedList: [],
      referenceBean: 'basic',
      referenceLayout: [],
      layout: [],
      otherShow: {
        comment: false,
        dynamic: false,
        approve: false
      }
    }
  },
  created () {
    this.layout = this.data.layout.layout
    this.otherShow.comment = this.data.layout.commentControl === '1'
    this.otherShow.dynamic = this.data.layout.dynamicControl === '1'
    this.otherShow.approve = this.data.layout.processId !== null
    this.firstField = this.data.layout.layout[0].rows[0].name ? this.layout[0].rows[0].name : 'id'
    this.$nextTick(() => {
      this.headerImg = {
        title: this.data.layout.appearance.headerModuleName,
        describe: this.data.layout.appearance.headerModuleDescribe,
        backgroundType: this.data.layout.appearance.headerBgType,
        backgroundColor: this.data.layout.appearance.headerBgColor,
        backgroundImg: this.data.layout.appearance.headerBgImg,
        backgroundOpacity: this.data.layout.appearance.headerBgOpacity,
        titleColor: this.data.layout.appearance.headerTextColor,
        titleSize: this.data.layout.appearance.headerTextSize,
        describeColor: this.data.layout.appearance.describeTextColor,
        describeSize: this.data.layout.appearance.describeTextSize
      }
    })
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
    // 根据组件类型显示列表字段
    getType (name) {
      if (name) {
        let type = name.split('_')[1]
        return type
      } else {
        return ''
      }
    },
    // 判断文件类型
    fileType (type) {
      let file = tool.determineFileType(type)
      return file
    },
    // 超链接打开网站
    openUrl (url) {
      window.open(url)
    },
    // 关闭详情弹框
    closeDialog () {
      this.$bus.emit('colseLocalDetail')
    }
  },
  mounted () {
  },
  computed: {
    // 默认打开的分栏
    columOpen: {
      get: function () {
        let name = []
        this.layout.map((item, index) => {
          if (!(item.isSpread === '1' && item.isHideColumnName === '0')) {
            name.push(item.name)
          }
        })
        return name
      },
      set: function (newValue) {
      }
    },
    detailTitle () {
      let text
      let list = []
      let type = this.firstField.split('_')[0]
      switch (type) {
        case 'picklist': case 'multi': case 'mutlipicklist':
          if (this.dataDetail[this.firstField]) {
            this.dataDetail[this.firstField].map((item, index) => {
              list.push(item.label)
            })
          }
          text = list.toString()
          break
        case 'personnel': case 'department': case 'reference':
          if (this.dataDetail[this.firstField]) {
            this.dataDetail[this.firstField].map((item, index) => {
              list.push(item.name)
            })
          }
          text = list.toString()
          break
        case 'datetime':
          text = new Date(parseInt(this.dataDetail[this.firstField])).toLocaleString()
          break
        case 'attachment': case 'picture':
          text = '附件和图片不要放在第一位'
          break
        case 'location':
          if (this.dataDetail[this.firstField]) {
            text = this.dataDetail[this.firstField].value
          }
          break
        case 'area':
          text = this.dataDetail[this.firstField]
          break
        case 'subform':
          text = '子表单不能放在第一位'
          break
        default:
          text = this.dataDetail[this.firstField]
          break
      }
      return text
    },
    isArea () {
      if (this.firstField.split('_')[0] === 'area') {
        return true
      } else {
        return false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity .5s
}
.fade-enter, .fade-leave-to /* .fade-leave-active in below version 2.1.8 */ {
  opacity: 0
}
.slide-enter-active {
  transition: all .5s linear;
}
.slide-leave-active {
  transition: all .5s linear;
}
.slide-enter, .slide-leave-to
/* .slide-fade-leave-active for below version 2.1.8 */ {
  transform: translateX(800px);
}
.data-detail{
  background: #FFFFFF;
  position: fixed;
  width: 780px;
  top: 0;
  bottom: 0;
  right: 0;
  z-index: 10;
  box-shadow: -2px 2px 4px 0 rgba(155,155,155,0.30), 2px -2px 4px 0 ;
  .el-header{
    line-height: 60px;
    padding: 0 30px;
    box-shadow: 0 1px 2px 0 rgba(185,185,185,0.50);
    z-index: 10;
    .headerTitle{
      font-size: 18px;
      color: #333;
    }
    .el-button{
      width: 80px;
    }
    .el-dropdown{
      margin: 0 0 0 20px;
      height: 25px;
      line-height: 25px;
      i{
        vertical-align: middle;
        font-size: 30px;
        cursor: pointer;
      }
    }
    i.el-icon-close{
      float: right;
      font-size: 20px;
      margin: 20px 0;
      cursor: pointer;
    }
  }
  .el-main{
    position: relative;
    height: 100%;
    padding: 0;
    background: #f4f5f5;
    overflow: hidden;
    .title{
      height: 55px;
      line-height: 55px;
      font-size: 18px;
      color: #17171A;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .reference-show{
      height: 50px;
      line-height: 50px;
      align-items: center;
      background: #FFFFFF;
      .name{
        display: inline-block;
        max-width: calc(100% - 320px);
        margin: 0 20px 0 0;
        font-size: 16px;
        color: #333333;
        vertical-align: top;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .el-button{
        float: right;
        margin: 10px 15px 10px 0;
      }
      .el-dropdown{
        float: right;
        margin: 0 15px 0 0;
        .icon-dian-shu{
          font-size: 28px;
          cursor: pointer;
        }
      }
      .checked-number{
        color: #4A4A4A;
        vertical-align: top;
        small{
          padding: 0 5px;
          color: #ff6d5d;
        }
      }
    }
    .reference-show:before{
      content: '';
      display: inline-block;
      width: 4px;
      height: 20px;
      margin: 0px 5px -4px 0;
      background: #36CFC9;
    }
  }
  .colum-box{
    padding: 10px 15px;
    .item{
      display: inline-block;
      width: 50%;
      .components{
        display: flex;
        align-items: center;
        min-height: 44px;
        line-height: 44px;
        box-shadow: inset 0 -1px 0 0 #f2f2f2;
        >label{
          flex: 0 0 85px;
          line-height: normal;
          margin: 0 15px 0 0;
          padding: 12px 0;
          text-align: left;
          font-size: 14px;
          color: #6b737b;
        }
        >span{
          display: block;
          flex:1;
          font-size: 14px;
          color: #4A4A4A;
          line-height: 44px;
          span.picklist{
            display: inline-block;
            max-width: 100px;
            min-width: 52px;
            height: 24px;
            line-height: 24px;
            font-size: 12px;
            color: #FFFFFF;
            padding: 0 8px;
            margin: 0 10px 0 0 ;
            border-radius: 37px;
            text-align: center;
            vertical-align: middle;
            box-sizing: border-box;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          .white-font{
            color: #4A4A4A !important;
            height: 100%!important;
            line-height: 36px!important;
            font-size: 14px!important;
            padding: 0!important;
            text-align: left!important;
          }
        }
        >span.textarea {
          line-height: 20px;
          max-height: 60px;
          overflow: auto;
          white-space: normal;
        }
        >span.picture{
          width: 100%;
          .item{
            display: flex;
            width: 100%;
            height: 46px;
            padding: 3px;
            margin: 0 0 8px 0;
            box-sizing: border-box;
            img{
              width: 40px;
              flex: 0 0 40px;
              height: 40px;
              margin: 0 15px 0 0;
            }
            .iconfont{
              font-size: 40px;
              margin: 0 15px 0 0;
            }
            .content{
              flex:1;
              box-sizing: border-box;
              .title{
                max-width: 150px;
                height: auto;
                font-size: 14px;
                line-height: 20px;
                color: #4A4A4A;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                .download{
                  float: right;
                  a{
                    font-size: 12px;
                    color: #29AB91;
                    cursor: pointer;
                  }
                  span{
                    display: inline-block;
                    width: 1px;
                    height: 14px;
                    line-height: 14px;
                    background: #E7E7E7;
                    margin: 0 8px;
                    vertical-align: middle;
                  }
                }
              }
              .detail{
                line-height: 20px;
                position: relative;
                span{
                  font-size: 12px;
                  color: #A0A0AE;
                }
                .name{
                  display: inline-block;
                  width: 40px;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                }
                .date{
                  position: absolute;
                  left: 40px;
                }
                .size{
                  position: absolute;
                  left: 115px;
                }
              }
            }
          }
        }
        >span.subform{
          width: 100%;
          border:1px solid #E7E7E7;
          overflow: auto;
          .titles{
            display: flex;
            width: 100%;
            height: 30px;
            line-height: 30px;
            span{
              flex: 1;
              min-width: 160px;
              padding: 0 10px;
              border-right:1px solid #E7E7E7;
              &:last-child{
                border-right: none;
              }
            }
          }
          .list{
            display: flex;
            width: 100%;
            height: 40px;
            line-height: 40px;
            .subform-child{
              flex: 1;
              min-width: 160px;
              padding: 0 10px;
              border-top: 1px solid #E7E7E7;
              border-right:1px solid #E7E7E7;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
              &:last-child{
                border-right: none;
              }
              span.picklist{
                display: inline-block;
                max-width: 100px;
                min-width: 52px;
                height: 24px;
                line-height: 24px;
                font-size: 12px;
                color: #FFFFFF;
                padding: 0 8px;
                margin: 0 10px 0 0 ;
                border-radius: 37px;
                text-align: center;
                vertical-align: middle;
                box-sizing: border-box;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }
              .white-font{
                color: #4A4A4A !important;
                height: 100%!important;
                line-height: 36px!important;
                font-size: 14px!important;
                padding: 0!important;
                text-align: left!important;
              }
              span.picture{
                width: 100%;
                .item{
                  display: flex;
                  width: 100%;
                  height: 40px;
                  box-sizing: border-box;
                  img{
                    width: 40px;
                    flex: 0 0 40px;
                    height: 40px;
                    margin: 0 15px 0 0;
                  }
                  .iconfont{
                    font-size: 40px;
                  }
                  .content{
                    flex:1;
                    box-sizing: border-box;
                    .title{
                      height: auto;
                      font-size: 14px;
                      line-height: 20px;
                      color: #4A4A4A;
                      overflow: hidden;
                      text-overflow: ellipsis;
                      white-space: nowrap;
                      .download{
                        float: right;
                        a{
                          font-size: 12px;
                          color: #29AB91;
                          cursor: pointer;
                        }
                        span{
                          display: inline-block;
                          width: 1px;
                          height: 14px;
                          line-height: 14px;
                          background: #E7E7E7;
                          margin: 0 8px;
                          vertical-align: middle;
                        }
                      }
                    }
                    .detail{
                      line-height: 20px;
                      position: relative;
                      span{
                        font-size: 12px;
                        color: #A0A0AE;
                      }
                      .name{
                        display: inline-block;
                        width: 40px;
                        overflow: hidden;
                        text-overflow: ellipsis;
                        white-space: nowrap;
                      }
                      .date{
                        position: absolute;
                        left: 40px;
                      }
                      .size{
                        position: absolute;
                        left: 115px;
                      }
                    }
                  }
                }
              }
            }
          }
        }
        >span.url{
          color: #008FE5;
          cursor: pointer;
        }
      }
    }
    .top-bottom{
      .components{
        display: block;
        line-height: 30px;
        >label{
          display: block;
        }
        >span{
          display: block;
        }
      }
    }
  }
}
</style>
<style lang="scss">
.data-detail{
  .el-collapse{
    width: 750px;
    border:none;
    .el-collapse-item__header{
      height: 40px;
      line-height: 40px;
      padding: 0 15px;
      border-bottom: 1px dotted #d9d9d9;
      .el-icon-circle-check{
        font-size: 16px;
        color:#51D0B1;
        margin:0 10px 0 0;
      }
      .el-collapse-item__arrow{
        font-size:16px;
        color: #51D0B1;
        display:none;
      }
      span{
        font-size: 14px;
        color: #17171A;
      }
      i.iconfont{
        display: inline-block;
        margin: 0 10px 0 0;
        transform: rotate(-90deg);
        transition: transform .3s;
      }
    }
    .el-collapse-item__header.is-active{
      i.iconfont{
        transform: rotate(0deg);
        transition: transform .3s;
      }
    }
    .hide-colum{
      .el-collapse-item__header{
        display: none;
      }
    }
    .el-collapse-item__wrap{
      border:none;
      .el-collapse-item__content{
        padding:0;
      }
    }
  }
  .scroll-box{
    width:100%;
    height: calc(100% - 50px);
    overflow: auto;
    padding:15px;
    .reference-box{
      height: 60px;
      line-height: 60px;
      margin: -15px 0 0 0;
      border-bottom: 1px solid #E7E7E7;
      .control-box{
        display: inline-block;
        >span{
          vertical-align: middle;
          margin: 0 30px 0 0;
          span{
            color: #FF6D5D;
          }
        }
        a{
          padding: 0 12px;
          border-left: 1px solid #D8D8D8;
          cursor: pointer;
          i{
            font-size: 16px;
            margin: 0 8px 0 0;
          }
        }
      }
      .el-button{
        width:80px;
      }
      .el-dropdown{
        float: right;
        width: 20px;
        height: 20px;
        line-height: 20px;
        margin: 20px 0;
        cursor: pointer;
        .iconfont {
          font-size:20px;
        }
      }
    }
    .list-box{
      overflow: hidden;
      height: calc(100% - 60px);
    }
    .el-tabs{
      .el-tabs__header{
        margin: 0;
      }
      .el-tab-pane{
        margin:15px 0 0 0;
      }
    }
  }
  .reference-tab{
    height: calc(100% - 80px);
    .el-tabs__content{
      height: calc(100% - 15px);
      .el-tab-pane{
        height:100%
      }
    }
  }
}
</style>
