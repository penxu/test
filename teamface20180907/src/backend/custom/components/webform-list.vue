<template>
  <div class="webform-container">
    <!-- web 表单列表 -->
    <div class="webform-list-container" v-if="!isShowFromCustom">
      <div class="add-btn"><el-button type="primary" size="small" @click="handleAddNew">新建表单</el-button></div>
      <el-table
        :data="webformList"
        style="width: 100%">
        <el-table-column
          prop="title"
          label="表单名称"
          width="180">
          <template slot-scope="scope">
            <div class="over-ellipsis" :title="scope.row.title"><span>{{scope.row.title}}</span></div>
          </template>
        </el-table-column>
        <el-table-column
          prop="accessAuth"
          label="访问权限"
          width="150">
          <template slot-scope="scope">
            <p>{{scope.row.accessAuth | toText(scope.row.accessAuth)}}</p>
            <p style="font-size: 10px; color: #ccc" v-show="scope.row.accessAuth === '0'">{{scope.row.accessPassword}}</p>
        </template>
        </el-table-column>
        <el-table-column
          prop="expandLink"
          label="链接地址"
          width="600">
            <template slot-scope="scope">
              <div class="flex-box link-box">
                <div class="over-ellipsis link-value" ><span>{{scope.row.externalLink}}</span></div>
                <div>
                  <el-button size="mini" type="text" @click="handleCopy(scope.$index, scope.row.externalLink)" :disabled="scope.row.publish === '0'">复制 </el-button>
                  <el-button size="mini" type="text" @click="handleOpenLink(scope.$index, scope.row.externalLink)" :disabled="scope.row.publish === '0'">打开</el-button>
                  <el-popover
                  placement="bottom"
                  width="150"
                  trigger="click">
                    <div class="link-item-box" style="height: 140px;">
                      <qrcode :val="scope.row.externalLink" :size="120" class="qrcode_box" style="padding: 36px 0 0 36px;" bg-color="#ffffff" fg-color="#000000" level="L"></qrcode>
                      <div><span style="font-size: 12px;">扫二维码，分享给好友</span></div>
                    </div>
                    <el-button size="mini" type="text" slot="reference" icon="el-icon-menu" style="padding-left: 10px;" :disabled="scope.row.publish === '0'"></el-button>
                  </el-popover>
                </div>
                <div class="show-link">
                  <el-popover
                  placement="bottom-end"
                  width="580"
                  trigger="click">
                  <div class="link-item-box" >
                    <div>
                      <span>拓展链接</span>
                      <el-popover
                        placement="top-bottom"
                        width="264"
                        trigger="hover"
                        content="">
                        <span style="font-size: 12px">对同一个表单赋予多个链接地址，用于标识不同数据提交来源。</span>
                      <i class="el-icon-warning" slot="reference" style="color: #F7BA2A"></i>
                      </el-popover>
                    </div>
                    <div v-for="(link, index) in scope.row.expandLink" :key="index" class="flex-box link-item" v-if="scope.row.expandLink.length > 0">
                      <div class="over-ellipsis" :title="link.name"><span>{{link.name}}</span></div>
                      <div><span>{{link.url}}</span></div>
                        <div>
                          <el-button size="mini" type="text" @click="handleCopy(scope.$index, link.url)" :disabled="scope.row.publish === '0'">复制 </el-button>
                          <el-button size="mini" type="text" @click="handleOpenLink(scope.$index, link.url)" :disabled="scope.row.publish === '0'">打开</el-button>
                          <!-- <el-button size="mini" type="text" icon="el-icon-menu" @click="handleEdit(scope.$index, scope.row)"></el-button> -->
                          <el-popover
                          placement="bottom"
                          width="150"
                          trigger="click">
                            <div class="link-item-box" style="height: 140px;">
                              <qrcode :val="link.url" :size="120" class="qrcode_box" style="padding: 36px 0 0 36px;" bg-color="#ffffff" fg-color="#000000" level="L"></qrcode>
                              <div><span style="font-size: 12px;">扫二维码，分享给好友</span></div>
                            </div>
                            <el-button size="mini" type="text" slot="reference" icon="el-icon-menu" style="padding-left: 10px;"></el-button>
                          </el-popover>
                        </div>
                    </div>
                  </div>
                  <el-button size="mini" type="text" slot="reference" icon="el-icon-arrow-down"></el-button>
                </el-popover>
                </div>
              </div>
            </template>
        </el-table-column>
        <el-table-column
          prop="modifyBy"
          label="修改人"
          width="220">
          <template slot-scope="scope">
            <div><span>{{scope.row.modifyBy.employee_name}} {{scope.row.modifyTime | formatDate('yyyy-MM-dd HH:mm')}}</span></div>
        </template>
        </el-table-column>
        <el-table-column
          label="操作">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              @click="handleAnnounce(scope.$index, scope.row.id)" v-if="scope.row.publish === '0'">发布</el-button>
            <el-button
              size="mini"
              type="text"
              @click="handleClose(scope.$index, scope.row.id)" v-if="scope.row.publish === '1'">关闭</el-button>
            <el-button
              size="mini"
              type="text"
              @click="handleEdit(scope.$index, scope.row.id)">编辑</el-button>
            <el-button
              size="mini"
              type="text"
              @click="handleDelete(scope.$index, scope.row.id)">删除</el-button>
        </template>
        </el-table-column>
        <template>
          <div slot="empty">
            <div style="width: 200px; height: 200px;">
              <img src="/static/img/no-data.png" style="width: 100%; height: 100%;">
              <p>暂无数据~</p>
            </div>
          </div>
        </template>
      </el-table>
    </div>
    <!-- 布局设置 -->
    <div v-if="isShowFromCustom" style="height: 100%;">
      <CustomWebform :webformId="webformId"></CustomWebform>
    </div>
  </div>
</template>
<script>
import CustomWebform from '@/backend/custom/layout/custom-webform' // web表单
import qrcode from '@/common/components/Qrcode'
export default {
  name: 'webformList',
  components: {
    CustomWebform,
    qrcode
  },
  data () {
    return {
      webformList: [
        // { 'modifyBy': {'employee_name': '曹建华', 'id': '1', 'picture': ''}, 'modifyTime': 1531798035730, 'publish': '0', 'accessAuth': '1', 'id': '5b4d62137e20ca265405d93c', 'title': '第一个web表单1sddddddddddddddddd1111', 'expandLink': [{name: '', url: 'www.baidu.com/teamrace/skdlfjklsdfsdfsdfdksdjfklasjkldfklsdjkl'}], 'accessPassword': '' },
        // { 'modifyBy': {'employee_name': '曹建华', 'id': '1', 'picture': ''},
        //   'modifyTime': '1531798035730',
        //   'publish': '0',
        //   'accessAuth': '1',
        //   'id': '5b4d62137e20ca265405d93c',
        //   'title': '第一个web表单11111',
        //   'expandLink': [{name: '微信', url: 'www.baidu.com'}, {name: '微博', url: 'www.baidu.com'}, {name: '知乎', url: 'www.baidu.com'}, {name: '今日头条', url: 'www.baidu.com'}],
        //   'accessPassword': ''
        // },
        // { 'modifyBy': {'employee_name': '曹建华', 'id': '1', 'picture': ''}, 'modifyTime': '1531798035730', 'publish': '0', 'accessAuth': '1', 'id': '5b4d62137e20ca265405d93c', 'title': '第一个web表单11111', 'expandLink': [{name: '', url: 'www.baidu.com'}], 'accessPassword': '' },
        // { 'modifyBy': {'employee_name': '曹建华', 'id': '1', 'picture': ''}, 'modifyTime': '1531798035730', 'publish': '0', 'accessAuth': '1', 'id': '5b4d62137e20ca265405d93c', 'title': '第一个web表单11111', 'externalLink': 'https://huijuhuaqi.com/fjkdsla32k', 'expandLink': [{name: '百度', url: 'http://www.baidu.com'}, {name: '百度', url: 'http://www.baidu.com'}, {name: '百度', url: 'http://www.baidu.com'}], 'accessPassword': '' }
      ],
      moduleBean: {moduleBean: this.$route.query.bean},
      webformId: {id: ''},
      isShowFromCustom: false
    }
  },
  methods: {
    // 点击编辑
    handleEdit (index, data) {
      console.log(data, 'id')
      this.webformId.id = data
      this.isShowFromCustom = true
    },
    // 点击删除
    handleDelete (index, data) {
      this.$confirm('确定要删除该web表单吗？', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let id = {id: data}
        console.log(id, 'id')
        this.$http.deleteWebformLayout(id).then(() => {
          this.$message({type: 'success', message: '删除成功！', showClose: true})
          this.getwebformList()
        })
      }).catch(() => {
      })
    },
    // 点击新增
    handleAddNew () {
      this.webformId.id = ''
      this.isShowFromCustom = true
    },
    // 获取表单列表
    getwebformList () {
      this.$http.getWebformList(this.moduleBean).then((res) => {
        console.log(res, '获取到的表单列表')
        this.webformList = res
      })
    },
    // 点击发布
    handleAnnounce (index, data) {
      this.$confirm('确定要发布该web表单吗？', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let id = {id: data}
        console.log(id, 'id')
        this.$http.openWebformLayout(id).then(() => {
          this.$message({type: 'success', message: '发布成功！', showClose: true})
          this.getwebformList()
        })
      }).catch(() => {
      })
    },
    // 点击关闭
    handleClose (index, data) {
      this.$confirm('确定要关闭该web表单吗？', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let id = {id: data}
        console.log(id, 'id')
        this.$http.closeWebformLayout(id).then(() => {
          this.$message({type: 'success', message: '关闭成功！', showClose: true})
          this.getwebformList()
        })
      }).catch(() => {
      })
    },
    // 打开链接
    handleOpenLink (index, data) {
      window.open(data)
    },
    // 点击复制
    handleCopy (index, data) {
      console.log(index, data, '复制的信息')
      const input = document.createElement('input')
      input.setAttribute('readonly', 'readonly')
      input.setAttribute('value', data)
      console.log(input, input.value.length, 'input')
      document.body.appendChild(input)
      // input.setSelectionRange(0, input.value.length)
      input.select()
      if (document.execCommand('copy')) {
        document.execCommand('copy')
        console.log('复制成功')
        this.$message({type: 'success', message: '已复制至剪切板', showClose: true})
      }
      document.body.removeChild(input)
    }
  },
  mounted () {
    this.getwebformList()
    this.$bus.off('toWebformList')
    this.$bus.on('toWebformList', (boole) => {
      this.isShowFromCustom = boole
      this.getwebformList()
    })
  },
  filters: {
    toText (text) {
      console.log(text, 'text')
      return text === '0' ? '密码访问' : '公开访问'
    }
  }
}
</script>
<style lang="scss">
.webform-container {
  height: calc(100% - 60px);
  .webform-list-container {
    height: 100%;
    padding: 20px;
    border-top: 1px solid #ddd;
    box-sizing: border-box;
    background: #fff;
    position: relative;
    .add-btn {
      position: absolute;
      right: 30px;
      top: -50px;
    }
    .el-table {
      height: 100%;
      .el-table__body-wrapper {
        height: calc(100% - 44px);
      }
      .el-table__row {
        div.cell {
          text-align: left;
        }
      }
      th {
        div.cell {
          border-left: 1px solid #E7E7E7;
          line-height: 16px;
          font-weight: initial;
          color: #17171A;
        }
      }
      .link-box {
        background: #FFFFFF;
        border: 1px solid #E7E7E7;
        border-radius: 4px;
        padding:  0 10px;
        height: 36px;
        position: relative;
        .link-value {
          width: 384px;
          span {
            line-height: 34px;
          }
        }
        .show-link {
          height: 34px;
          width: 33px;
          text-align: center;
          border-left: 1px solid #E7E7E7;
          position: absolute;
          right: 0;
          button {
            width: 33px;
          }
        }
      }
    }
  }
  .link-item-box {
    .link-item {
      height: 60px;
      border-bottom: 1px solid #EAEAEA;
      box-sizing: border-box;
      >div {
        line-height: 60px;
        padding-left: 10px;
      }
      >div:nth-child(2) {
        padding: 0 20px;
        width: 380px;
      }
      >div:nth-child(1) {
        width: 40px;
      }
    }
  }
}
</style>
