<template>
  <el-dialog title="选择成员" :visible.sync="visible" width="310px" class="false-member-modal" >
    <div class="content">
      <div class="titles">公司成员</div>
      <div class="box">
        <el-collapse v-model="activeName">
          <el-radio-group v-model="radio">
            <el-collapse-item name="1">
              <template slot="title">
                <i class="el-icon-caret-right"></i>开发部
              </template>
              <div class="member-box">
                <!-- <el-radio :label="1" class="item"> -->
                <div class="item" @click="chooseUser(1)">
                  <img src="../assets/head1.jpg" alt="">
                  <span>张三</span>
                  <i v-if="isactiveRadio!=1" class="iconfont icon-pc-member-sign emp"></i>
                  <i v-if="isactiveRadio==1" class="iconfont icon-pc-member-sign-ok emp"></i>
                </div>
                <!-- </el-radio> -->
                <!-- <el-radio :label="2" class="item"> -->
                  <div class="item" @click="chooseUser(2)">
                    <img src="../assets/head2.jpg" alt="">
                    <span>李四</span>
                    <i v-if="isactiveRadio!=2" class="iconfont icon-pc-member-sign emp"></i>
                    <i v-if="isactiveRadio==2" class="iconfont icon-pc-member-sign-ok emp"></i>
                  </div>
                <!-- </el-radio> -->
              </div>
            </el-collapse-item>
            <el-collapse-item name="2">
              <template slot="title">
                <i class="el-icon-caret-right"></i>产品部
              </template>
              <div class="member-box">
                <!-- <el-radio :label="3" class="item"> -->
                <div class="item" @click="chooseUser(3)">
                  <img src="../assets/head1.jpg" alt="">
                  <span>王五</span>
                  <i v-if="isactiveRadio!=3" class="iconfont icon-pc-member-sign emp"></i>
                  <i v-if="isactiveRadio==3" class="iconfont icon-pc-member-sign-ok emp"></i>
                </div>
                <!-- </el-radio> -->
              </div>
            </el-collapse-item>
            <el-collapse-item name="3">
              <template slot="title">
                <i class="el-icon-caret-right"></i>人事部
              </template>
              <div class="member-box">
                <!-- <el-radio :label="4" class="item"> -->
                <div class="item" @click="chooseUser(4)">
                  <img src="../assets/head2.jpg" alt="">
                  <span>赵六</span>
                  <i v-if="isactiveRadio!=4" class="iconfont icon-pc-member-sign emp"></i>
                  <i v-if="isactiveRadio==4" class="iconfont icon-pc-member-sign-ok emp"></i>
                </div>
                <!-- </el-radio> -->
              </div>
            </el-collapse-item>
          </el-radio-group>
        </el-collapse>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="save">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: 'ReviewMember',
  props: ['datas'],
  data () {
    return {
      visible: false,
      activeName: '1',
      radio: 2,
      isactiveRadio: 0
    }
  },
  methods: {
    // 保存成员
    save () {
      let name
      if (this.isactiveRadio === 1) {
        name = '张三'
      } else if (this.isactiveRadio === 2) {
        name = '李四'
      } else if (this.isactiveRadio === 3) {
        name = '王五'
      } else if (this.isactiveRadio === 4) {
        name = '赵六'
      }
      let member = [
        { 'id': this.isactiveRadio,
          'name': name,
          'picture': '',
          'type': 1,
          'sign_id': 10000,
          'value': '1:1',
          'checked': true
        }
      ]
      let data = {
        'type': 1,
        'prepareData': member,
        'prepareKey': this.datas.prepareKey,
        'seleteForm': true,
        'banData': [],
        'navKey': '',
        'removeData': [],
        'index': 0
      }
      this.$bus.emit('selectMemberRadio', data)
      this.visible = false
    },
    chooseUser (v) {
      this.isactiveRadio = v
    }
  },
  mounted () {
  },
  watch: {
    datas () {
      this.visible = true
    }
  }
}
</script>

<style lang='scss'>
.false-member-modal{
  .el-dialog__header {
    border-bottom:1px solid #ddd;
    i.el-dialog__close {
      color: #979797 !important
    }
    .el-dialog__headerbtn {
      .el-dialog__close {
        font-size: 23px;
        color:#ffffff
      }
    }
  }
  .el-dialog__footer{
    padding: 20px 16px 10px;
    .el-button {
      width: 80px;
      padding: 6px 20px;
    }
  }
  .el-dialog__body{
    padding: 20px 20px 0;
    .titles{
      color: #1890ff;;
    }
    .el-radio-group{
      display: block;
    }
    .box{
      height: 400px;
      border: 1px solid #E7E7E7;
      .el-collapse{
        border-top:none;
      }
      .el-collapse-item{
        border: none;
        .is-active .el-icon-caret-right {
          transform: rotate(90deg);
        }
        .el-collapse-item__header{
          height: 40px;
          line-height: 40px;
          font-size: 14px;
          color: #17171A;
          border: none;
          .el-collapse-item__arrow {
              display: none;
          }
          .el-icon-caret-right {
              font-size: 12px;
              margin: 0 7px;
              color: #c0c4cc;
          }
        }
        .el-collapse-item__content{
          padding: 0 0 10px;
        }
        .el-collapse-item__wrap{
          border: none;
          .member-box{
            .item{
              display: block;
              height: 40px;
              line-height: 40px;
              margin: 0;
              &:hover{
                cursor: pointer;
                background-color: #f5f7fa;
              }
              img{
                width: 30px;
                height: 30px;
                border-radius: 50%;
                margin: 5px 10px;
                vertical-align: top;
              }
              span{
                vertical-align: top;
                color: #17171A;
              }
              .el-radio__input{
                float: right;
                margin: 13px 20px;
              }
              .iconfont{
                float:right;
                margin-right: 20px;
                cursor: pointer;
              }
              .icon-pc-member-sign-ok{
                color: #1890ff;
              }
            }
          }
        }
      }
    }
  }

}
</style>
