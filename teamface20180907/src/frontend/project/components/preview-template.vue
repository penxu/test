<template>
  <el-dialog :visible.sync="previewVisible" :close-on-click-modal="false" width="800px" append-to-body id="proviewTemplate">
    <template slot="title">
      <div class="titleSlot">
        <span>预览项目模版</span>
        <span></span>
      </div>
    </template>
    <div class="proviewTemplateMain">
      <draggable v-model="list" :options="parentDraggable" @start="hiddleLine" @update="updata1($event,list)" @add="changedata1(list)" @end="showLine(list)" class="no-drag">
        <div v-for="(v, k) in list" :key="k" class="allMain" :id="'offsetHeightNew'+k">
          <span class="topLine"></span>
          <span v-if="k!==(list.length-1)" class="BottomLineNew" :id="'BottomLineNew'+k" draggable='false' ondragstart="return false;"></span>
          <div class="outBox">
            <div class="circle">
              <span><span></span></span>
            </div>
            <div class="parentCss" :class="'styleClass'+v.isshow" @click.stop="openShowOne(v,('offsetHeightNew'+k),('BottomLineNew'+k),$event)">
              <div class="firstItem">
                <i class="iconfont icon-yidong"></i>
                <!-- <span><span>0</span>/<span>4</span></span> -->
                <span v-text="v.name" style="font-size:18px;"></span>
              </div>
            </div>
          </div>
          <draggable v-model="v.subnodeArr" :options="subDraggable" @start="hiddleLine" @add="changedata2($event,v.list,('offsetHeightNew'+k),('BottomLineNew'+k))" @end="empty($event,v.list,('offsetHeightNew'+k),('BottomLineNew'+k))" class="second no-drag" v-if="v.isshow==1">
            <div v-for="(v1, k1) in v.subnodeArr" :key="k1" class="secondItem" :id="'batchAdd' + k1">
              <div class="threeItem"  v-if="v1.name">
                <span class="showOrHidden">
                  <i class="iconfont icon-yidong"></i>
                  <span v-text="v1.name"></span>
                </span>
              </div>
              <div v-else class="threeItem" style="text-align:center">
                <span style="color:#ccc;">-- 暂无数据 --</span>
              </div>
            </div>
          </draggable>
        </div>
      </draggable>
    </div>
    <span slot="footer" class="dialog-footer" v-if="fromIndexOrTask === 'fromIndex'">
      <el-button type="primary" @click.stop="useTemplate">使用该模板</el-button>
    </span>
  </el-dialog>
</template>
<script>
import draggable from 'vuedraggable'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'PreviewTemplate',
  components: {draggable},
  data () {
    return {
      data: {},
      list: [],
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      previewVisible: false,
      fromIndexOrTask: null
    }
  },
  mounted () {
    this.$bus.on('PreviewTemplateStatus', res => {
      let data = JSON.parse(res)
      this.data = data
      if (data.status === 'open') {
        this.fromIndexOrTask = 'fromTask'
      } else if (data.status === 'IndexOpen') {
        this.fromIndexOrTask = 'fromIndex'
        this.getqueryAllNode(data)
        this.list = []
      }
      this.previewVisible = true
    })
  },
  methods: {
    useTemplate () { // 使用模版
      this.$bus.$emit('fromPreviewTemplate', JSON.stringify(this.data))
      this.previewVisible = false
    },
    getqueryAllNode (v) { // 获取模板任务分组
      HTTPServer.queryProjectTemplateAllNode({tempId: v.id}, 'Loading').then((res) => {
        res.dataList.map((v1, k1) => {
          v1.isshow = 1
        })
        this.list = res.dataList
      })
    },
    eduitorHeight (str, str1) { // 动态改变连接线的高度
      let ele = document.getElementById(str)
      setTimeout(function () {
        let ele1 = document.getElementById(str1)
        if (ele1) {
          ele1.style.height = (ele.offsetHeight - 10) + 'px'
        }
      }, 10)
    },
    openShowOne (v, str, str1, e) {
      if (!v.subnodeArr || v.subnodeArr.length === 0) {
        return false
      }
      v.isshow = v.isshow === 1 ? 0 : 1
      this.eduitorHeight(str, str1)
      let ele = e.target.parentElement
      if (v.name) {
        if (v.isshow === 1) {
          ele.setAttribute('style', 'border-bottom:0;border-bottom-left-radius:0;border-bottom-right-radius:0;box-shadow: 0 -1px 1px #ddd;')
        } else {
          ele.setAttribute('style', 'border:1px solid #ddd;border-radius:5px;')
        }
      }
    },
    showOrHiddenLine (status) {
      let ele = document.querySelectorAll('.BottomLineNew')
      let index = ele.length
      for (let i = 0; i < index; i++) {
        if (status === 1) {
          ele[i].style.display = 'block'
        } else {
          ele[i].style.display = 'none'
        }
      }
    },
    hiddleLine () {
      this.showOrHiddenLine(0)
    },
    showLine (list) {
      this.showOrHiddenLine(1)
      list.forEach((v, k) => {
        this.eduitorHeight('offsetHeightNew' + k, 'BottomLineNew' + k)
      })
    },
    changedata1 (list, str, str1) {
      this.delAtrrbuite(list)
      this.eduitorHeight(str, str1)
    },
    changedata2 (e, list, str, str1) {
      this.delAtrrbuite(list)
      this.eduitorHeight(str, str1)
    },
    updata1 (e) {
      let ele = e.target.getElementsByClassName('parentCss')
      if (ele) {
        let index = ele.length
        for (let i = 0; i < index; i++) {
          if (ele[i].className.indexOf('styleClass1') !== -1) {
            ele[i].setAttribute('style', 'border-bottom:0;border-bottom-left-radius:0;border-bottom-right-radius:0;box-shadow: 0 -1px 1px #ddd;')
          } else {
            ele[i].setAttribute('style', 'border:1px solid #ddd;border-radius:5px;')
          }
        }
      }
      console.log(e.target)
    },
    delAtrrbuite (list, str, str1) {
      let index = ''
      if (list && list.length > 0) {
        list.forEach((v, k) => {
          if (!v.name) {
            index = k
          }
        })
      }
      if (index !== '') {
        list.splice(index, 1)
      }
    },
    empty (e, list, str, str1) {
      if (list && list.length === 0) {
        list.push({name: '', list: []})
      }
      this.eduitorHeight(str, str1)
      this.showOrHiddenLine(1)
    }
  },
  computed: {
    /** 列表字段 */
    parentDraggable () {
      return {
        animation: 200,
        group: { name: 'parent', pull: true, put: true },
        sort: true,
        ghostClass: 'ghost',
        filter: '.no-drag'
      }
    },
    subDraggable () {
      return {
        animation: 200,
        group: {name: 'sone', pull: true, put: true},
        sort: true,
        ghostClass: 'ghost',
        filter: '.no-drag'
      }
    }
  },
  beforeDestroy () {
    this.$bus.off('PreviewTemplateStatus')
  }
}
</script>
<style lang='scss' scoped>
.proviewTemplateMain{
    height: 600px;padding:10px 20px 0 20px;
    overflow: auto;
    .subItem{
      margin: 20px 0;
      height: 50px;
      >div{float: left;}
      >div:first-child{
        width: 10%;height: 50px;
        position: relative;
        >div{
          position: absolute;
          top: 0;
          left: 0;
          bottom: 0;
          right: 0;
          margin: auto;
          height: 20px;
          width: 20px;
          border-radius: 50%;
          border: 1px solid #E2E2E2;
          box-shadow: 0 0 10px #ddd;
          >span.itemBottom,>span.itemTop{
            position: absolute;
            left: 7px;
            height: 26px;
            width: 1px;
            border: 2px solid #F4F5F6;
          }
          >span.itemBottom{top: 19px;}
          >span.itemTop{bottom: 19px;}
        }
      }
      >div:last-child{width:80%;border: 1px solid #DADADA;box-shadow: 0 0 16px #ddd;height: 50px;line-height: 50px;border-radius: 5px;font-size: 16px;i{margin: 0 30px 0 10px;}&:hover{cursor: pointer;}}
    }
  }
  .titleSlot{text-align: center;}
  .allMain:after{
    content: '';
    display: table;clear: both;
  }
  .allMain{
    position: relative;  margin: 10px auto;
    .BottomLineNew{
      position: absolute;height: 40px;width:  1px;top: 35px; left: 23px;border: 2px solid #D7D7D7;
    }
  }
  .outBox{
    height: 50px;width: 750px;
    .circle{
      height: 50px;line-height: 50px; width: 50px; float: left; position: relative;
      >span{
        position: absolute; top: 0;left: 0;bottom: 0;right: 0;margin: auto; height: 20px;width: 20px; border-radius: 50%;border: 4px solid #D7D7D7;
      }
    }
    .parentCss{
      background: #fff;height: 50px;line-height: 50px;float: left; border: 1px solid #DADADA;width: 700px;border-radius: 5px;
      border-bottom:0;border-bottom-left-radius:0;border-bottom-right-radius:0;box-shadow: 0 -1px 1px #ddd;
      &:hover{cursor: pointer;}
      .firstItem{
        >i{margin-left: 10px;font-size: 18px;color:#BBBBC3;}
        >span{margin-left: 10px;}
      }
    }
  }
  .second{
    width: 700px;margin-left: 50px;background: #fff;border-radius: 5px;padding: 20px;border: 1px solid #ddd;border-top:0;
    border-top-left-radius:0;border-top-right-radius:0;
    .threeItem{
      &:hover{cursor: pointer;}
      height: 50px;line-height: 50px; background: #F1F3F4; border-radius: 5px;
      .showOrHidden{
        i{margin-left: 10px;}
        >span{margin-left: 10px;}
      }
    }
    .batchOperationSet{
      position: relative; height: 70px;padding: 5px 20px;display: none;border-right:1px solid #ddd;border-left:1px solid #ddd;
      >div:first-child{
        height: 70px;
        >div{
          height: 35px;
          line-height: 35px;
        }
        >div:last-child span{font-size: 12px;color: #ccc;margin-left: 10px;}
      }
      >div:last-child{
        position: absolute;top: 17px;right: 19px;text-align: center;line-height: 30px;height: 30px;color: #fff; &:hover{cursor: pointer;}
        background: #1890FF;border-radius: 5px;padding: 0 20px;
      }
    }
  }
  .secondItem{
    margin-bottom: 5px;
  }
</style>
<style lang='scss'>
#proviewTemplate{
  .el-dialog__header{border-bottom: 1px solid #ddd;background:#fff;color:#4A4A4A;span{font-size:16px;}}
  .el-dialog__header .el-dialog__headerbtn .el-dialog__close{color:#4A4A4A;}
  .el-dialog__body{background:#EBEDF0;padding:0;}
  .el-dialog__footer{padding:10px;border-top:1px solid #ddd;.el-button.el-button--primary{width:110px;margin-right:30px;padding: 6px 13px;}}
}
</style>
