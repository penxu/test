<template>
  <el-container class="data-detail-wrip" >
    <el-header>
      <icon-img :type="moduleIcon.type" :url="moduleIcon.url" :size="moduleIcon.style" :isModule="true"></icon-img>
      <div class="title" v-if="isArea">
        {{detailTitle | areaTo}}
      </div>
      <div class="title" v-else>
        {{detailTitle}}
      </div>
      <i class="el-icon-close" @click="closeDialog"></i>
    </el-header>
    <el-main>
      <div id="referenceTitle" class="reference-title" v-if="referenceList.length > 1">
        <div class="item" v-for="(reference,index) in referenceList" :key="index" v-if="reference.show === '1'" :title="reference.moduleLabel"
        :class="{active: reference.active}" :data-propty="JSON.stringify(reference)" @click="shiftReferenceList(referenceList, reference)">
          {{reference.moduleLabel}}
        </div>
        <div class="item more" v-if="hideReferenceList.length > 0">
          <el-dropdown trigger="click" placement="bottom" @command="handleCommand">
            <span class="el-dropdown-link">
              <span class="reference-name" :class="{'active-color': moreReferenceName !=='更多'}">{{moreReferenceName}}</span><i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown" class="more-reference">
              <el-dropdown-item :command="item" v-for="(item, index) in hideReferenceList" :key="index" :class="{active: item.moduleLabel === moreReferenceName}">{{item.moduleLabel}}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
      <div class="reference-show">
        <span class="name">{{referenceIndex.moduleLabel}}</span>
        <el-dropdown trigger="click" placement="bottom" @command="handleControl" v-if="referenceIndex.moduleName === 'basic'" v-show="getAuth(-1)">
          <span class="el-dropdown-link">
            <i class="icon-dian-shu iconfont"></i>
          </span>
          <el-dropdown-menu slot="dropdown"  class="more-handle">
            <el-dropdown-item :command="{id:dataId,type:-1}" v-if="otherShow.dynamic">动态</el-dropdown-item>
            <el-dropdown-item :command="{id:dataId,type:0}" v-if="getAuth(7) && !highSeaId && dataDetail['lockedState'] != '1'">转移负责人</el-dropdown-item>
            <el-dropdown-item :command="{id:dataId,type:1}" v-if="getAuth(1) && !highSeaId">复制</el-dropdown-item>
            <el-dropdown-item :command="{id:dataId,type:2}" v-if="getAuth(6) && !highSeaId && dataDetail['lockedState'] != '1'">转换</el-dropdown-item>
            <el-dropdown-item :command="{id:dataId,type:3}" v-if="getAuth(5) && !highSeaId && dataDetail['lockedState'] != '1'">删除</el-dropdown-item>
            <el-dropdown-item :command="{id:dataId,type:4}" v-if="getAuth(4) && !highSeaId && dataDetail['lockedState'] != '1'">共享</el-dropdown-item>
            <el-dropdown-item :command="{id:dataId,type:5}" v-if="getAuth(8) && !highSeaId">打印</el-dropdown-item>
            <el-dropdown-item :command="{id:dataId,type:10}" >数据链接</el-dropdown-item>
            <el-dropdown-item :command="{id:dataId,type:6}" v-if="highSeaId && highSeasAmdin === '1'">编辑</el-dropdown-item>
            <el-dropdown-item :command="{id:dataId,type:7}" v-if="highSeaId && highSeasAmdin === '1'">分配</el-dropdown-item>
            <el-dropdown-item :command="{id:dataId,type:8}" v-if="highSeaId && highSeasAmdin === '1'">移动</el-dropdown-item>
            <el-dropdown-item :command="{id:dataId,type:9}" v-if="highSeaId && highSeasAmdin === '1'">删除</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
        <el-button type="primary" size="mini" @click="openEdit" v-if="referenceIndex.moduleName === 'basic' && !highSeaId" :disabled="!(getAuth(3) || dataDetail['lockedState'] != '1')">编 辑</el-button>
        <el-button type="primary" size="mini" @click="getCustomer" v-if="referenceIndex.moduleName === 'basic' && highSeaId">领 取</el-button>
        <span class="checked-number" v-if="checkedList.length > 0">已选择<small>{{checkedList.length}}</small>条记录</span>
        <el-button type="primary" size="mini" v-if="referenceIndex.moduleName !== 'basic' && checkedList.length === 0" @click="createData(referenceIndex)">新 增</el-button>
        <el-button plain size="mini" v-if="checkedList.length > 0" @click="referenceDelete(referenceIndex)">删 除</el-button>
        <el-button type="primary" size="mini" v-if="checkedList.length === 1" @click="referenceEdit(referenceIndex)">编 辑</el-button>
      </div>
      <div id="scrollBox" class="scroll-box">
        <div class="tab-content">
          <div class="basic-info" v-if="referenceIndex.moduleName === 'basic'">
            <custom-header-img :datas="headerImg"></custom-header-img>
            <el-collapse id="capture" v-model="columOpen" class="el-tabs-warp" @change="editorImg">
              <el-collapse-item v-for="(colum,index) in layout" :key="index" :name="colum.name" v-if="colum.isHideInDetail === '0' && colum.terminalPc === '1'">
                <template slot="title" >
                  <i class="icon-pc-paper-more iconfont"></i>
                  <span>{{colum.title}}</span>
                </template>
                <layout-detail :bean="bean" :rows="colum.rows" :dataDetail="dataDetail"></layout-detail>
              </el-collapse-item>
            </el-collapse>
          </div>
          <module-other :bean="bean" :dataId="dataId" :detail="dataDetail" :show="otherShow" v-if="referenceIndex.moduleName === 'basic'"></module-other>
          <div class="reference-tab" v-else>
            <div class="list-box" v-for="(reference,index) in referenceList" :key="index" v-if="reference.show === '1'  && reference.moduleName === referenceIndex.moduleName">
              <module-list :bean="reference.moduleName" :fieldName="reference.fieldName" :query="reference.queryWhere" :dataId="dataDetail.id"></module-list>
            </div>
          </div>
        </div>
      </div>
      <div class="dynamic-shade" v-if="openDynamic"></div>
      <transition name="slide">
        <module-dynamic v-if="openDynamic" :bean="bean" :dataId="dataId" :style="{top: referenceList.length > 1 ? 95 : 51  + 'px'}"></module-dynamic>
      </transition>
    </el-main>
  </el-container>
</template>

<script>
import ModuleList from './module-list'
import ModuleOther from './module-other'
import ModuleDynamic from './module-dynamic'
import LayoutDetail from '@/common/layout/layout-detail'
import IconImg from '@/common/components/icon-img'
import CustomHeaderImg from '@/common/components/custom-header-img'
import Comment from '@/common/components/comment.1'
import {HTTPServer, ajaxGetRequest} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
import $ from 'jquery'
import axios from 'axios'
window.openMuliteImgsLast = function (v) { // 打开富文本图片
  let newwin = window.open()
  newwin.document.write('<img src=' + v.src + ' /><style>body{margin:50px 100px;background:#212121;position: relative;}img{max-width:100%;max-height:100%;text-align: center;position: absolute;margin: auto;top: 0;right: 0;bottom: 0;left: 0;}</style>')
}
export default {
  name: 'ModuleDetail',
  components: {
    ModuleList,
    ModuleOther,
    ModuleDynamic,
    LayoutDetail,
    IconImg,
    CustomHeaderImg,
    Comment
  },
  props: ['data', 'dtlKey'],
  data () {
    return {
      openReference: false,
      haveReferenceList: false,
      activeName: 'basic',
      dataDetail: {},
      firstField: {},
      referenceList: [],
      hideReferenceList: [],
      checkedList: [],
      referenceLayout: [],
      layout: [],
      auth: [],
      highSeaId: '',
      highSeasAmdin: '',
      otherShow: {
        comment: false,
        dynamic: false
      },
      openDynamic: false,
      picklistHide: [],
      moduleIcon: {
        style: {}
      },
      headerImg: {},
      moreReferenceName: '更多',
      referenceIndex: {
        moduleLabel: '基本资料',
        moduleName: 'basic',
        active: true,
        show: '1'
      }
    }
  },
  created () {
    this.bean = this.data.bean
    this.dataId = this.data.dataId
    this.moduleName = this.data.moduleName
    this.highSeaId = this.data.highSeaId
    this.highSeasAmdin = this.data.highSeasAmdin
    let id = {
      id: this.dataId,
      bean: this.bean
    }
    let bean = {bean: this.bean, operationType: 4}
    let auth = {moduleId: this.data.moduleId, bean: this.bean}
    this.ajaxGetAuthList(auth)
    if (this.highSeaId) {
      bean.isSeasPool = this.highSeaId
    }
    this.ajaxLayoutAndData(ajaxGetRequest(id, 'moduleOperation/findDataDetail'), ajaxGetRequest(bean, 'layout/getEnableLayout'))
    this.ajaxGetReferenceList(id)

    this.$bus.on('detail-page-roll', (type) => {
      console.log(type)
      this.toScrollTop(type)
    })
    this.$bus.on('closeDynamicModal', (value) => {
      if (value) {
        this.openDynamic = false
      }
    })
  },
  methods: {
    // 判断权限码
    getAuth (code) {
      if (code === -1) {
        let boo = this.auth.length !== 0
        return boo
      } else {
        let boo = JSON.stringify(this.auth).includes('"auth_code":' + code)
        return boo
      }
    },
    // 关闭详情弹框
    closeDialog () {
      this.$bus.emit('closeDetailModal', this.dtlKey, this.haveReferenceList)
    },
    // 关联数据打开新增窗口
    createData (data) {
      let modules = {bean: data.moduleName, moduleName: data.moduleLabel, type: 6, plist_relyon: 1}
      this.$bus.emit('customOpenCreateModal', modules, this.dataDetail)
    },
    // 打开编辑窗口
    openEdit () {
      let modules = {bean: this.bean, moduleName: this.moduleName, type: 3, plist_relyon: 1}
      this.$bus.emit('customOpenCreateModal', modules, this.dataDetail)
    },
    // 更多关联模块
    handleCommand (index) {
      this.haveReferenceList = true
      this.referenceList.map((item) => {
        item.active = false
      })
      this.referenceIndex = index
      this.moreReferenceName = index.moduleLabel
      this.checkedList = []
    },
    // 切换关联关系列表
    shiftReferenceList (list, index) {
      this.haveReferenceList = true
      list.map((item) => {
        item.active = false
      })
      index.active = true
      this.referenceIndex = index
      this.moreReferenceName = '更多'
      this.checkedList = []
    },
    // 更多操作
    handleControl (common) {
      switch (common.type) {
        case -1:// 打开动态
          this.openDynamic = true
          break
        case 0:// 转移负责人
          let value = {bean: this.bean, type: 1, ids: this.dataId}
          this.$bus.emit('openMemberCommit', value)
          break
        case 1:// 复制
          let modules = {bean: this.bean, moduleName: this.moduleName, type: 5}
          this.$bus.emit('customOpenCreateModal', modules, this.dataDetail)
          break
        case 2:// 转换
          let value2 = {bean: this.bean, dataId: this.dataId}
          this.$bus.emit('openConvert', value2)
          break
        case 3:// 删除
          this.deleteData()
          break
        case 4:// 共享
          let value4 = {bean: this.bean, dataId: this.dataId}
          this.$bus.emit('openDataShare', value4)
          break
        case 5:// 打印
          let value5 = {bean: this.bean, dataId: this.dataId}
          this.$bus.emit('openPrint', value5)
          break
        case 6:// 公海池编辑
          let value6 = {bean: this.bean, moduleName: this.moduleName, type: 3, highSeaId: this.highSeaId}
          this.$bus.emit('customOpenCreateModal', value6, this.dataDetail)
          break
        case 7:// 公海池分配
          let value7 = {bean: this.bean, type: 0, ids: this.dataId}
          this.$bus.emit('openMemberCommit', value7)
          break
        case 8:// 公海池移动
          let bean = {bean: this.bean}
          HTTPServer.getHighSeasList(bean, 'Loading', '保存').then((res) => {
            let value = {bean: this.bean, type: 1, ids: this.dataId, poolList: res}
            this.$bus.emit('openHighSeas', value)
          })
          break
        case 9:// 公海池删除
          this.deleteData()
          break
        case 10:// 数据链接
          let value10 = {bean: this.bean, ids: this.dataId, highSeaId: this.highSeaId}
          this.ajaxGetDataLinksInfo(value10)
          break
        default:
          break
      }
    },
    // 删除数据
    deleteData () {
      let ids = String(this.dataId)
      this.$alert('<p style="margin:15px 0 35px 0">删除成功之后，该操作将无法恢复。</p>确认要删除该数据吗？', '提示', {
        dangerouslyUseHTMLString: true,
        showCancelButton: true
      }).then(_ => {
        let data = {
          bean: this.bean,
          ids: ids
        }
        this.ajaxDeleteData(data, true)
      }).catch(_ => {})
    },
    // 关联数据打开编辑窗口
    referenceEdit (data) {
      let modules = {bean: data.moduleName, moduleName: data.moduleLabel, type: 3, plist_relyon: 1}
      let id = {
        id: this.checkedList.toString(),
        bean: data.moduleName
      }
      this.ajaxGetDataDtlForReference(id, modules)
    },
    // 关联数据转移负责人
    referenceTransfer (data) {
      let value = {bean: data.moduleName, type: 1, ids: this.checkedList.toString()}
      this.$bus.emit('openMemberCommit', value)
    },
    // 关联数据删除
    referenceDelete (datas) {
      let ids = this.checkedList.toString()
      this.$alert('<p style="margin:15px 0 35px 0">删除成功之后，该操作将无法恢复。</p>确认要删除该数据吗？', '提示', {
        dangerouslyUseHTMLString: true,
        showCancelButton: true
      }).then(_ => {
        let data = {
          bean: datas.moduleName,
          ids: ids
        }
        this.ajaxDeleteData(data)
      }).catch(_ => {})
    },
    // 关联数据分享
    referenceShare (data) {
      let value = {bean: data.moduleName, type: 2, ids: this.checkedList.toString()}
      this.$bus.emit('openMemberCommit', value)
    },
    // 导入导出
    importExport (data) {
      if (data.type === 0) {
        // 导入
        let value = {bean: data.reference.moduleName, progressActive: 1, importButton: '下一步', finishStatus: 'success'}
        this.$bus.emit('openImportData', value)
      } else if (data.type === 1) {
        // 导出
        let value = {name: data.reference.moduleLabel, bean: data.reference.moduleName}
        this.$bus.emit('exportFile', value)
      }
    },
    // 滑动到顶部
    toScrollTop (target) {
      var timer = setInterval(function () {
        var scrolltop = document.getElementById('scrollBox').scrollTop
        switch (target) {
          case 0:
            document.getElementById('scrollBox').scrollBy(0, -100)
            if (Math.ceil(scrolltop) <= 0) {
              clearInterval(timer)
            };
            break
          case 1:
            document.getElementById('scrollBox').scrollBy(0, 100)
            let scrolltop1 = Math.ceil(scrolltop)
            let offsetHeight1 = document.getElementById('scrollBox').offsetHeight
            let scrollHeight1 = document.getElementById('scrollBox').scrollHeight
            console.log(scrolltop1, offsetHeight1, scrollHeight1)
            if (Math.ceil(scrolltop1) + Math.ceil(offsetHeight1) >= scrollHeight1) {
              clearInterval(timer)
            };
            break
        }
      }, 30)
    },
    // 领取客户到公海池
    getCustomer () {
      let data = {bean: this.bean, id: this.dataId, seas_pool_id: this.highSeaId}
      HTTPServer.highSeasPoolGet(data, 'Loading').then((res) => {
        this.$message.success('领取成功!')
        let value = {
          seas_pool_id: this.highSeaId
        }
        this.$bus.emit('refreshList', value)
        this.$bus.emit('closeDetailModal', this.dtlKey)
      })
    },
    // tab页点击-关联关系
    handleClick (tab, event) {
      this.referenceBean = tab.name
    },
    // 获取字段后面所有的字段
    getUnderField (name) {
      let list = []
      let index = ''
      let fields = []
      this.layout.map((item) => {
        item.rows.map((item2, index2) => {
          list.push(item2)
        })
      })
      list.map((item, index2) => {
        if (item.name === name) {
          index = index2
        }
        if (index !== '' && index2 > index) {
          fields.push({name: item.name, label: item.label, selected: false})
        }
      })
      return fields
    },
    // 返回隐藏字段
    returnHideList (hidePool, field, list) {
      if (hidePool.length !== 0) {
        if (JSON.stringify(hidePool).includes(field.name)) {
          // 如果字段被上一个选项控制隐藏，那么他本身的隐藏属性不触发
          return hidePool
        } else {
          // 字段没有被上个选项控制隐藏，其本身隐藏字段则生效，本字段下显示的字段要显示出来
          let show = this.getUnderField(field.name) // 控制字段下所有字段
          let lists = tool.arrayRemainder(show, list) // 控制字段要显示的字段
          return tool.arrayRemainder(hidePool, lists) // 最终隐藏的字段
        }
      } else {
        return list
      }
    },
    // 下拉选项默认值控制隐藏
    defaultHideField (fields) {
      fields = JSON.parse(JSON.stringify(fields))
      let hidePool = []
      let showPool = this.getUnderField(fields[0].name)
      this.pickShowField(showPool)
      fields.map((item) => {
        item.entrys.map((item2) => {
          if (item2.value === item.field.defaultEntrys[0].value) {
            item2.hidenFields.map((item3) => {
              delete item3.type
            })
            hidePool = this.returnHideList(hidePool, item, item2.hidenFields)
          }
        })
      })
      this.pickHideField(hidePool)
      console.log(showPool, '显示字段')
      console.log(hidePool, '隐藏字段')
    },
    // 封装下拉选项控制隐藏
    pickHideField (list) {
      this.layout.map((item, index) => {
        item.rows.map((item2, index2) => {
          for (let i in list) {
            if (list[i].name === item2.name) {
              this.$set(item2.field, 'selectHide', '1')
              return
            }
          }
        })
      })
    },
    // 封装下拉选项控制显示
    pickShowField (list) {
      this.layout.map((item, index) => {
        item.rows.map((item2, index2) => {
          for (let i in list) {
            if (list[i].name === item2.name) {
              this.$set(item2.field, 'selectHide', '0')
            }
          }
        })
      })
      console.log(this.layout)
    },
    // AJAX获取布局和数据
    ajaxLayoutAndData (...ajaxs) {
      axios.all([...ajaxs]).then(
        axios.spread((...res) => {
          // 两个请求现在都执行完成
          console.log(res)
          let dataRes = res[0].data.data
          let layoutRes = res[1].data.data
          // 详情数据返回
          this.dataDetail = dataRes
          if (this.dataDetail) {
            let reg = /<img/g
            for (let key in dataRes) {
              if (key.indexOf('multitext') !== -1) { // 富文本添加点击事件
                this.dataDetail[key] = this.dataDetail[key].replace(reg, '<img onclick="openMuliteImgsLast(this)" style="cursor:pointer;" class="multitextInnerImg"')
              }
              if (key.indexOf('subform') !== -1) {
                this.dataDetail[key].map((v1, k1) => {
                  for (let v2 in v1) {
                    if (v2.indexOf('multitext') !== -1) { // 子表单富文本添加点击事件
                      if (v1[v2] && JSON.stringify(v1[v2]) !== '[]' && JSON.stringify(v1[v2]) !== '{}') {
                        v1[v2] = v1[v2].replace(reg, '<img onclick="openMuliteImgsLast(this)" style="cursor:pointer;" class="submultitextInnerImg"')
                      }
                    }
                  }
                })
              }
            }
          }
          setTimeout(() => {
            this.editorImg()
          }, 200)
          // 布局接口返回
          let picklistHide = []
          this.otherShow.comment = layoutRes.commentControl === '1'
          this.otherShow.dynamic = layoutRes.dynamicControl === '1'
          this.firstField = layoutRes.layout && layoutRes.layout[0].rows[0].name ? layoutRes.layout[0].rows[0] : {}
          // 详情隐藏下拉控制选项字段
          layoutRes.layout.map((item) => {
            item.rows.map((item2, index2) => {
              if (item2.type === 'picklist' && item2.field.chooseType === '0' && JSON.stringify(item2.entrys).includes('hidenFields')) {
                if (this.dataDetail[item2.name].length > 0) {
                  item2.field.defaultEntrys = this.dataDetail[item2.name]
                  picklistHide.push(item2)
                }
              }
            })
          })
          console.log(picklistHide)
          this.layout = layoutRes.layout
          this.moduleIcon = {
            type: layoutRes.icon_type,
            url: layoutRes.icon_url,
            style: {
              border: '40px',
              content: '28px',
              background: layoutRes.icon_color,
              radius: '4px'
            }
          }
          this.headerImg = {
            title: layoutRes.appearance.headerModuleName,
            describe: layoutRes.appearance.headerModuleDescribe,
            backgroundType: layoutRes.appearance.headerBgType,
            backgroundColor: layoutRes.appearance.headerBgColor,
            backgroundImg: layoutRes.appearance.headerBgImg,
            backgroundOpacity: layoutRes.appearance.headerBgOpacity,
            titleColor: layoutRes.appearance.headerTextColor,
            titleSize: layoutRes.appearance.headerTextSize,
            describeColor: layoutRes.appearance.describeTextColor,
            describeSize: layoutRes.appearance.describeTextSize
          }
          this.picklistHide = picklistHide
        })).catch((err) => {
        console.log(err)
      })
    },
    editorImg () {
      this.$nextTick(() => {
        // let elements = document.getElementsByClassName('multitextInnerImg')
        // let ele1 = document.getElementsByClassName('submultitextInnerImg')
        // if (elements && elements.length > 0) {
        //   let index = elements.length
        //   for (let i = 0; i < index; i++) {
        //     // 获取label宽度判断是左右布局还是上下布局
        //     let parentWidth1 = elements[i].parentNode.parentNode.previousSibling.previousElementSibling.clientWidth
        //     if (parentWidth1 > 150) { // 上下布局
        //       if (elements[i].width > 700) {
        //         elements[i].style.width = '700px'
        //       }
        //     } else {
        //       if (elements[i].width > 580) { // 左右布局
        //         elements[i].style.width = '582px'
        //       }
        //     }
        //   }
        // }
        // if (ele1 && ele1.length > 0) {
        //   let index1 = ele1.length
        //   for (let j = 0; j < index1; j++) {
        //     let parentWidth = ele1[j].parentNode.clientWidth
        //     if (ele1[j].width > parentWidth) {
        //       ele1[j].style.width = parentWidth + 'px'
        //     }
        //     let labelwidth = ele1[j].parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.previousSibling.previousElementSibling.clientWidth
        //     let parentnodenode = ele1[j].parentNode.parentNode.parentNode.className
        //     if (parentnodenode.indexOf('card-body') !== -1) {
        //       if (labelwidth > 150) {
        //         if (ele1[j].width > 570) {
        //           ele1[j].style.width = '570px'
        //         }
        //       } else {
        //         if (ele1[j].width > 465) {
        //           ele1[j].style.width = '465px'
        //         }
        //       }
        //     }
        //   }
        // }
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
            let labelEle = $(CardImg[i]).parent().prev()[0].offsetWidth
            let CardChildImg = $(CardImg[i]).find('.submultitextInnerImg')
            if (CardChildImg && CardChildImg.length > 0) {
              let CardChildImgIndex = CardChildImg.length
              for (let j = 0; j < CardChildImgIndex; j++) {
                if (labelEle > 100) {
                  if ($(CardChildImg[j]).width() > 595) {
                    $(CardChildImg[j]).width(595)
                  }
                } else {
                  if ($(CardChildImg[j]).width() > 495) {
                    $(CardChildImg[j]).width(495)
                  }
                }
              }
            }
          }
        }
        // 富文本图片缩略图
        let layoutMultitextImg = $('.multitext-out-box')
        if (layoutMultitextImg && layoutMultitextImg.length > 0) {
          let layoutMultitextImgIndex = layoutMultitextImg.length
          for (let i = 0; i < layoutMultitextImgIndex; i++) {
            let labelEle = $(layoutMultitextImg[i]).prev()[0].offsetWidth
            let CardChildImg = $(layoutMultitextImg[i]).find('.multitextInnerImg')
            if (CardChildImg && CardChildImg.length > 0) {
              let CardChildImgIndex = CardChildImg.length
              for (let j = 0; j < CardChildImgIndex; j++) {
                if (labelEle > 100) {
                  if ($(CardChildImg[j]).width() > 700) {
                    $(CardChildImg[j]).width(700)
                  }
                } else {
                  if ($(CardChildImg[j]).width() > 600) {
                    $(CardChildImg[j]).width(600)
                  }
                }
              }
            }
          }
        }
      })
    },
    // AJAX获取权限列表
    ajaxGetAuthList (data) {
      HTTPServer.getFunAuthList(data, 'Loading').then((res) => {
        this.auth = res
      })
    },
    // AJAX获取数据详情-关联关系
    ajaxGetDataDtlForReference (data, modules) {
      HTTPServer.customDtlData(data, 'Loading').then((res) => {
        this.$bus.emit('customOpenCreateModal', modules, res)
      })
    },
    // AJAX获取关联关系数组
    ajaxGetReferenceList (data) {
      HTTPServer.customDtlRelationData(data, 'Loading').then((res) => {
        res.refModules.map((item) => {
          item.active = false
        })
        res.refModules.unshift({moduleLabel: '基本资料', moduleName: 'basic', active: true, show: '1'})
        this.referenceList = res.refModules
      })
    },
    // 删除数据
    ajaxDeleteData (data, delDtl) {
      HTTPServer.customDeleteData(data, 'Loading').then((res) => {
        this.$message.success('删除成功!')
        this.$bus.emit('refreshList')
        this.$bus.emit('setCheckEmpty')
        if (delDtl) {
          this.closeDialog()
        }
      })
    },
    // 获取数据链接信息
    ajaxGetDataLinksInfo (value) {
      let data = {
        moduleBean: value.bean,
        dataId: value.ids
      }
      this.$http.getDataLinksInfo(data).then((res) => {
        let linkInfo = {
          bean: value.bean,
          dataId: value.ids,
          externalLink: res.externalLink,
          insideOnOff: res.insideOnOff,
          externalOnOff: res.externalOnOff,
          dataLinkAuth: res.dataLinkAuth,
          linkUrl: window.location.origin + `#independent/dataLinks?code=${res.externalLink}`
        }
        if (res.externalOnOff || res.insideOnOff) {
          this.$bus.emit('openDataLinks', linkInfo, this.highSeaId)
        } else {
          this.$message({
            message: '未开启数据链接!',
            type: 'warning',
            duration: 1500
          })
        }
      })
    }
  },
  mounted () {
    // 选中条目
    this.$bus.on('checkedData', (bean, value) => {
      if (bean === this.referenceIndex.moduleName) {
        this.checkedList = value
      }
    })
    // 刷新详情数据
    this.$bus.off('refreshDtl')
    this.$bus.on('refreshDtl', value => {
      if (value) {
        let id = {
          id: this.dataId,
          bean: this.bean
        }
        let bean = {bean: this.bean, operationType: 4}
        if (this.highSeaId) {
          bean.isSeasPool = this.highSeaId
        }
        this.picklistHide = []
        this.ajaxLayoutAndData(ajaxGetRequest(id, 'moduleOperation/findDataDetail'), ajaxGetRequest(bean, 'layout/getEnableLayout'))
      }
    })
    setTimeout(() => {
      this.editorImg()
    }, 200)
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
      if (this.firstField.name) {
        let text
        let list = []
        let type = this.firstField.name.split('_')[0]
        switch (type) {
          case 'picklist': case 'multi': case 'mutlipicklist':
            if (this.dataDetail[this.firstField.name]) {
              this.dataDetail[this.firstField.name].map((item, index) => {
                list.push(item.label)
              })
            }
            text = list.toString()
            break
          case 'personnel': case 'department': case 'reference':
            if (this.dataDetail[this.firstField.name]) {
              this.dataDetail[this.firstField.name].map((item, index) => {
                list.push(item.name)
              })
            }
            text = list.toString()
            break
          case 'datetime':
            text = tool.formatDate(this.dataDetail[this.firstField.name], this.firstField.field.formatType)
            break
          case 'attachment': case 'picture':case 'subform': case 'multitext':
            text = ''
            break
          case 'location':
            if (this.dataDetail[this.firstField.name]) {
              text = this.dataDetail[this.firstField.name].value
            }
            break
          default:
            text = this.dataDetail[this.firstField.name]
            break
        }
        return text
      }
    },
    isArea () {
      if (this.firstField.name && this.firstField.name.split('_')[0] === 'area') {
        return true
      } else {
        return false
      }
    }
  },
  watch: {
    picklistHide: {
      deep: true,
      handler (newVal, oldVal) {
        if (newVal.length > 0 && oldVal.length === 0) {
          this.defaultHideField(newVal)
        }
      }
    },
    referenceList (newVal, oldVal) {
      if (oldVal.length === 0 && newVal.length > 1) {
        this.$nextTick(() => {
          let width = 0
          let hideReferenceList = []
          let list = document.getElementById('referenceTitle').children
          for (const key in list) {
            if (list.hasOwnProperty(key)) {
              if (!list[key].className.includes('more')) {
                width += list[key].clientWidth
                if (width > 644) {
                  hideReferenceList.push(JSON.parse(list[key].dataset.propty))
                }
              }
            }
          }
          console.log(hideReferenceList)
          this.hideReferenceList = hideReferenceList
        })
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
  transform: translateX(780px);
}
.more-reference{
  margin: 0;
  .active{
    background-color: #ecf5ff;
    color: #66b1ff;
  }
}
.more-handle{
  margin: 0;
  .el-dropdown-menu__item{
    width: 140px;
  }
}
.data-detail-wrip{
  background: #FFFFFF;
  position: fixed;
  width: 780px;
  top: 0;
  bottom: 0;
  right: 0;
  z-index: 10;
  box-shadow: -2px 2px 4px 0 rgba(155,155,155,0.30), 2px -2px 4px 0 ;
  .el-header{
    display: flex;
    align-items: center;
    line-height: 60px;
    padding: 0 15px;
    box-shadow: inset 0 -1px 0 0 #EBEBF0;
    z-index: 10;
    >.icon-img-wrap{
      flex: 0 0 40px;
      margin: 0 12px 0 0;
    }
    >.title{
      flex: 1;
      font-size: 18px;
      color: #333333;
    }
    >i.el-icon-close{
      flex: 0 0 20px;
      font-size: 20px;
      color: #666666;
      cursor: pointer;
    }
  }
  >.el-main{
    position: relative;
    height: 100%;
    padding: 0;
    background:#F4F5F5;
    overflow: hidden;
    .reference-title{
      position: relative;
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      height: 44px;
      line-height: 44px;
      padding: 0 126px 0 0;
      color: #6B737B;
      overflow: hidden;
      .item{
        min-width: 90px;
        max-width: 126px;
        height: 44px;
        padding: 0 15px;
        text-align: center;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        cursor: pointer;
      }
      .item.active{
        border-bottom: 3px solid #1890FF;
      }
      .item.more{
        position: absolute;
        width: 126px;
        right: 0;
        .el-dropdown{
          width: 100%;
          .reference-name{
            display: inline-block;
            max-width: 75px;
            vertical-align: bottom;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          .active-color{
            color: #65A6F2;
          }
        }
      }
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
    .scroll-box{
      width: 100%;
      height: calc(100% - 100px);
      overflow: auto;
      .tab-content{
        height: 100%;
        padding: 15px;
        .basic-info{
          padding: 0 0 15px 0;
        }
        .reference-tab{
          height: 100%;
          padding: 0 16px;
          background: #FFFFFF;
          .list-box{
            overflow: hidden;
            height: 100%;
          }
        }
      }
    }
    .dynamic-shade{
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      z-index: 5;
    }
  }
}
</style>
<style lang="scss" >
.data-detail-wrip{
  .el-collapse{
    width: 750px;
    border:none;
    .el-collapse-item__header{
      height: 40px;
      line-height: 40px;
      padding: 0 15px;
      border-bottom: 1px dotted #D9D9D9;
      .el-icon-circle-check{
        font-size: 16px;
        color:#51D0B1;
        margin:0 10px 0 0;
      }
      .el-collapse-item__arrow{
        font-size:16px;
        color: #424242;
        float: none;
        display: none;

      }
      i.iconfont{
        display: inline-block;
        margin: 0 10px 0 0;
        transform: rotate(-90deg);
        transition: transform .3s;
      }
      span{
        display: inline-block;
        max-width: calc(100% - 30px);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        vertical-align: top;
        font-size: 14px;
        color: #424242;
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
}
</style>
