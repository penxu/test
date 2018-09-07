<template>
  <el-dialog title="提示" :visible.sync="dialogVisible" :close-on-click-modal="false" id="templatePro" width="800px">
    <el-container>
      <el-header>
        <div class="headerTop">
          <!-- <span :class="activeName=='1'?'active':''" @click="changeModule('1')">项目模版</span>
          <span :class="activeName=='2'?'active':''" @click="changeModule('2')">我的模版</span> -->
          <span class="chooseMoudelTitle">选择模版</span>
          <i class="iconfont icon-pc-member-close" @click="dialogVisible=false"></i>
        </div>
      </el-header>
      <el-main>
        <div>
          <el-container style="height:650px;">
            <el-aside width="150px">
              <div v-for="(v, k) in systemTemplate" :key="k" class="listTemplate" @click="changeTemplate(k, v)" :class="v.active==1&&activeName!='2'?'chooseActive':''">
                <span v-text="v.name" :class="v.active==1&&activeName!='2'?'subListTem':''"></span>
              </div>
              <div class="listTemplate" @click="changeModule('2')" :class="activeName=='2'?'chooseActive':''">我的模版</div>
            </el-aside>
            <el-main style="padding:10px 20px;" v-if="activeName=='1'">
              <el-row :gutter="20" style="height:630px;overflow:auto;">
                <el-col :span="8" v-for="(val, key) in templateList" :key="key">
                  <div class="grid-content myTemplate" :class="'system-template-'+val.id" :style="{'background-image':val.pic_url?'url('+val.pic_url+')':''}"  @click="preview(val)">
                    <!-- <img src="/static/img/app_custom_app.7b20546.png" alt="" class="templateImgStyle"> -->
                    <span v-text="val.name" class="templateSpan"></span>
                    <div class="templteDiv templteDivFirst">
                      <span class="iconTopSpan" v-if="val.active === 0" @click.stop="changeIcon(val)"></span>
                      <i v-if="val.active === 1" class="iconfont icon-Rectangle3"></i>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </el-main>
            <el-main style="padding:10px 20px;" v-if="activeName=='2'">
              <el-row :gutter="20" style="height:630px;overflow:auto;">
                <el-col :span="8" v-for="(val, key) in templateNameList" :key="key">
                  <div class="grid-content myTemplate" :class="!val.pic_url && val.system_default_pic?'grid-content-'+val.system_default_pic:!val.pic_url && !val.system_default_pic?'grid-content-one':''" :style="{'background-image':val.pic_url?'url('+val.pic_url+')':''}"  @click="preview(val)">
                    <!-- <img src="/static/img/app_custom_app.7b20546.png" alt="" class="templateImgStyle"> -->
                    <span v-text="val.name" class="templateSpan"></span>
                    <div class="templteDiv templteDivFirst">
                      <span class="iconTopSpan" v-if="val.active === 0" @click.stop="changeIcon(val)"></span>
                      <i v-if="val.active === 1" class="iconfont icon-Rectangle3"></i>
                    </div>
                    <span class="delete-icon-perview" @click.stop="deleteMyTemplate(val, key)">
                      <i class="iconfont icon-pc-delete"></i>
                    </span>
                  </div>
                </el-col>
              </el-row>
            </el-main>
          </el-container>
          <div class="projectBtn">
            <el-button @click="dialogVisible=false">取消</el-button>
            <el-button type="primary"  @click="sybmitTemplte">确认</el-button>
          </div>
        </div>
        <PreviewTemplate></PreviewTemplate>
      </el-main>
    </el-container>
  </el-dialog>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import draggable from 'vuedraggable'
import PreviewTemplate from '@/frontend/project/components/preview-template'
export default {
  name: 'Template',
  components: {draggable, PreviewTemplate},
  data () {
    return {
      activeName: '1',
      data: '',
      gotoProjectAdd: {},
      dialogVisible: false,
      previewVisible: false, // 预览弹窗
      templateNameList: [], // 我的模版
      systemTemplate: [ // 系统模版类别
        {name: '所有模版', active: 1, id: 0},
        {name: '通用模板', active: 0, id: 1},
        {name: '售后服务', active: 0, id: 2},
        {name: '产品研发', active: 0, id: 3},
        {name: '行政人事', active: 0, id: 4},
        {name: '市场营销', active: 0, id: 5}
      ],
      templateList: [], // 系统模版列表
      previewData: {}, // 预览数据存放
      list: []
    }
  },
  mounted () {
    this.$bus.on('templateStatus', data => {
      if (data === 'open') {
        this.dialogVisible = true
        this.activeName = '1'
        this.systemTemplate.map((v, k) => {
          v.active = k === 0 ? 1 : 0
        })
        this.getTemplateType({templateRole: 0, tempType: 0})
      }
    })
    this.$bus.on('fromPreviewTemplate', res => {
      let data = JSON.parse(res)
      this.gotoProjectAdd = data
      console.log(data)
      if (data.template_role === 1) {
        this.templateNameList.map((v, k) => {
          if (v.id === data.id) {
            v.active = 1
          } else {
            v.active = 0
          }
        })
      } else {
        this.templateList.map((v, k) => {
          if (v.id === data.id) {
            v.active = 1
          } else {
            v.active = 0
          }
        })
      }
    })
  },
  methods: {
    getTemplateType (data) { // 获取模版
      HTTPServer.queryProjectTemplateList(data, 'Loading').then((res) => {
        console.log(res)
        res.dataList.forEach((val, key) => {
          val.active = 0
        })
        if (this.activeName === '2') {
          this.templateNameList = res.dataList
        } else {
          this.templateList = res.dataList
        }
      })
    },
    changeModule (v) {
      this.activeName = v
      this.getTemplateType({templateRole: 1})
    },
    changeTemplate (index, value) { // 项目模版
      this.activeName = '1'
      this.systemTemplate.forEach((v, k) => {
        if (k === index) {
          v.active = 1
        } else {
          v.active = 0
        }
      })
      this.getTemplateType({templateRole: 0, tempType: value.id})
    },
    deleteMyTemplate (v, k) { // 删除模板
      this.$confirm('确定要删除模版吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        HTTPServer.deleteProjectTemplate({tmepId: v.id}, 'Loading').then((res) => {
          this.templateNameList = this.templateNameList.splice(1, k)
          this.$message({
            message: '操作成功！',
            type: 'success'
          })
          this.getTemplateType({templateRole: 1})
        })
      }).catch(() => { })
    },
    sybmitTemplte () {
      this.$bus.$emit('gotoAddProjectData', this.gotoProjectAdd)
      this.dialogVisible = false
    },
    changeIcon (val) {
      if (this.activeName === '1') {
        this.templateList.forEach((v, k) => {
          if (v.id === val.id) {
            v.active = 1
          } else {
            v.active = 0
          }
        })
      } else {
        this.templateNameList.forEach((v, k) => {
          if (v.id === val.id) {
            v.active = 1
          } else {
            v.active = 0
          }
        })
      }
      this.gotoProjectAdd = val
    },
    preview (v) { // 预览
      console.log(111)
      this.previewData = v
      v.status = 'IndexOpen'
      this.$bus.$emit('PreviewTemplateStatus', JSON.stringify(v))
    }
  },
  beforeDestroy () {
    this.$bus.off('templateStatus')
    this.$bus.off('fromPreviewTemplate')
  }
}
</script>
<style lang='scss' scoped>
#templatePro{
  .grid-content-one{background: url('/static/img/project/1@2x.png') no-repeat;}
  .grid-content-two{background: url('/static/img/project/2@2x.png') no-repeat;}
  .grid-content-three{background: url('/static/img/project/3@2x.png') no-repeat;}
  .grid-content-four{background: url('/static/img/project/4@2x.png') no-repeat;}
  .grid-content-five{background: url('/static/img/project/5@2x.png') no-repeat;}
  .grid-content-six{background: url('/static/img/project/6@2x.png') no-repeat;}
  .system-template-1{background: url('/static/img/project/system-template-1.png') no-repeat;}
  .system-template-2{background: url('/static/img/project/system-template-2.png') no-repeat;}
  .system-template-3{background: url('/static/img/project/system-template-3.png') no-repeat;}
  .system-template-4{background: url('/static/img/project/system-template-4.png') no-repeat;}
  .system-template-5{background: url('/static/img/project/system-template-5.png') no-repeat;}
  .system-template-6{background: url('/static/img/project/system-template-6.png') no-repeat;}
  .system-template-7{background: url('/static/img/project/system-template-7.png') no-repeat;}
  .system-template-8{background: url('/static/img/project/system-template-8.png') no-repeat;}
  .system-template-9{background: url('/static/img/project/system-template-9.png') no-repeat;}
  .system-template-10{background: url('/static/img/project/system-template-10.png') no-repeat;}
  .system-template-11{background: url('/static/img/project/system-template-11.png') no-repeat;}
  .system-template-12{background: url('/static/img/project/system-template-12.png') no-repeat;}
  .system-template-13{background: url('/static/img/project/system-template-13.png') no-repeat;}
  .system-template-14{background: url('/static/img/project/system-template-14.png') no-repeat;}
  .system-template-15{background: url('/static/img/project/system-template-15.png') no-repeat;}
  .system-template-16{background: url('/static/img/project/system-template-16.png') no-repeat;}
  .headerTop{
    text-align:center;.chooseMoudelTitle{font-size: 16px;color:#323232;}
    i{
      float: right;margin-right: 20px;color:#979797;
      &:hover{cursor: pointer;}
    }
  }
  .listTemplate{
    padding:12.5px 20px;border-bottom: 1px solid #ddd; text-align: center;color: #868686;border-right:3px solid transparent;
    &:hover{cursor: pointer;color: #3AA6FF;}
    .subListTem{color: #3AA6FF;}
  }
  .chooseActive{
    color:#1890FF;border-right-color: #1890FF;
  }
  .projectBtn{
    height:50px;border-top:1px solid #ddd;text-align:right;line-height:50px;padding-right: 20px;
  }
  .grid-content{
    position: relative;height: 120px;font-size: 16px;text-align: center;
    color: #fff; margin: 8px 0;line-height: 120px;border-radius: 5px;
    .delete-icon-perview{
      position: absolute;
      z-index:5;
      bottom:0;
      right:0;
      height:30px;width:30px;
      i{
        position: absolute;
        display: inline-block;
        left:10px;
        top: -50px;
        font-size:14px;
        color:#fff;
        display: none;
      }
    }
    >div{
      position: absolute;bottom: -8px;right: 0;width: 100px;height: 50px;line-height: 50px;
    }
    .saveIcon{
      margin-left: 20px;
      i{opacity: .3;&:hover{cursor: pointer;}}
      i.iconShow{
        opacity: 1;
      }
    }
    &:hover{
      .delete-icon-perview i{display: block;}
    }
  }
  .myTemplate{
    &:hover{cursor: pointer;.templteDiv .iconTopSpan{display: inline-block;}}
    .templateSpan{position: absolute;left:10px;top:5px;font-size: 14px;height:22px;line-height: 22px;}
    .templateImgStyle{
      position: absolute;left:0;right:0;width:100%;height:100%;border-radius: 5px;opacity: .9;
    }
    .previewBottom{
      position: absolute;
      bottom: -8px;
      right: 22%;
    }
    .templteDiv{
      position: absolute;top: -10px;right: -35px;
      >i{&:hover{cursor: pointer;}color:#fff;}
      i.iconTop{
        opacity: 1;
      }
      i.iconfont.icon-Rectangle3{
        position: relative;top:-2px;font-size:17px;
      }
      .iconTopSpan{
        display: none;border: 1px solid #fff;border-radius: 50%;height:15px;width:15px;cursor: pointer;
      }
    }
  }
  .templteDiv.templteDivFirst{
    width:50px;
    right: -10px;
  }
}
.titleSlot{
    text-align: center;
    position: relative;
    >span:first-child{
      font-size: 18px;
    }
  }
  .proviewTemplateMain{
    height: 600px;
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
  .allMain:after{
    content: '';
    display: table;clear: both;
  }
  .allMain{
    position: relative;  margin: 10px auto;
    .BottomLine{
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
      background: #fff;height: 50px;line-height: 50px;float: left; border: 1px solid #DADADA;box-shadow: 0 0 10px #ddd;width: 700px;border-radius: 5px;
      &:hover{cursor: pointer;}
      .firstItem{
        >i{margin-left: 10px;}
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
#templatePro{
  .el-dialog__header{display: none;}
  .el-dialog__body{
    padding: 0;height: 760px;max-height: 760px;
    .el-header{
      height: 60px;padding: 0;line-height: 60px;border-bottom: 1px solid #ddd;
    }
    .el-main{
      height: 700px;
      padding: 0;
      .el-aside{
        border-right: 1px solid #ddd;background: #fff;
      }
      .el-button{
        padding: 8px 20px;
      }
    }
  }
}
#proviewTemplate{
  .el-dialog__header{border-bottom: 1px solid #ddd;}
}
</style>
