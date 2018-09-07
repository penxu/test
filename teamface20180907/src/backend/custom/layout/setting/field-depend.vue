<template>
  <div class="fd-container">
    <ralationMap v-if="currentTag === 'first'"></ralationMap>
    <relationDepend v-if="currentTag === 'second'"></relationDepend>
    <optionDepend v-if="currentTag === 'third'"></optionDepend>
    <optionCtrl v-if="currentTag === 'fourth'"></optionCtrl>
    <dataRelation v-if="currentTag === 'fifth'"></dataRelation>
  </div>

</template>
<script>
import ralationMap from '@/backend/custom/layout/setting/field-depend/relation-maping'
import relationDepend from '@/backend/custom/layout/setting/field-depend/relation-depend'
import optionCtrl from '@/backend/custom/layout/setting/field-depend/option-ctrl'
import optionDepend from '@/backend/custom/layout/setting/field-depend/option-depend'
import dataRelation from '@/backend/custom/layout/setting/field-depend/data-relation' // 数据联动组件
/** params:  rm_* 开头是关联映射的数据
 *           rr_* 开头是关联依赖的数据
 *           od_* 开头是选项依赖的数据
 *           oc_* 开头是选项控制的数据
 **************************************************************************************/
export default {
  name: 'fieldDepend',
  components: {ralationMap, relationDepend, optionCtrl, optionDepend, dataRelation},
  data () {
    return {
      currentTag: 'first'
    }
  },
  watch: {
    // 监听路由变化
    $route (to, from) {
      this.currentTag = to.query.tab
      console.log(this.currentTag, 'currentTag')
    }
  },
  methods: {

  },
  computed: {
  },
  mounted () {
    this.currentTag = this.$route.query.tab
  }
}
</script>
<style lang="scss">
  .fd-container {
    // margin-left:25px;
    height: 100%;
    .el-tabs__header{
      display: none;
    }
    .fd-title {
      line-height: 60px;
      text-align: left;
      span:first-child{
        font-size: 18px;
        color: #17171A;
      }
      span {
        color: #BBBBC3;
      }
    }
    .fd-tabs {
      height: 100%;
      .el-tabs {
        height: 100%;
      }
      .el-tabs__nav {
        // padding-left: 35px;
        .el-tabs__active-bar{
          display:none;
        }
      }
      .el-tabs__nav-scroll {
        line-height: 60px;
        .el-tabs__item {
          font-size: 16px;
        }
      }
      .el-tabs__content {
        height: 100%;
        .el-tab-pane {
          height: 100%;
        }
      }
    }
    // .rm-container {
    //   // height: 100%;
    //   .el-button {
    //     padding: 5px 13px;
    //     height: 30px;
    //     // width: 80px;
    //   }
    // }
    .fd-text {
      font-size:16px;
      color: #17171A;
      line-height: 50px;
      border-bottom: 1px solid #f2f2f2;
      height: 50px;
    }
    .fd-addbtn {
      margin: 20px 0;
      .el-button {
        padding: 5px 13px;
        height: 30px;
        // width: 80px;
      }
    }
    .rm-table {
      // padding-left: 25px;
      height: calc(100% - 160px);
      overflow: auto;
      .el-table {
        height: 100%;
        overflow: auto;
      }
      .el-table thead {
        color: #17171A;
      }
      .el-table__body-wrapper {
        height: calc(100% - 55px);
        overflow:auto;
        .cell {
          text-align: left;
        }
      }
    }
    //弹出框
    .el-dialog {
      width: 45%;
      height: 400px;
      .el-dialog__header {
        background: #1989FA;

        .el-dialog__title {
          color:#ffffff;
        }
        .el-dialog__headerbtn {
          .el-dialog__close {
            font-size: 23px;
            color:#ffffff
          }
        }

      }
      .inner-diglog {
        .el-button {
          padding: 5px 13px;
          height: 30px;
          width: 80px;
        }
      }
    }
    .el-dialog__footer {
        position: absolute;
        right: 10px;
        bottom: 10px;
        // top: 470px;
      .el-button {
        padding: 5px 13px;
        height: 30px;
        width: 80px;
      }
    }
    .fd-dialog_content {
      margin-left:50px;
      .fd-dialog_item:first-child {
        margin-top:10px;

      }
      .fd-dialog_item {
        margin-bottom: 25px;
      }
      .text {
        line-height: 35px;
        margin-right:40px;
      }
      .el-input__inner {
        height: 35px;
      }
      .fd-dialog_sel {
        width: 380px;
        .el-select {
          width: 100%;
          .el-input {
            width: 100%;
          }
        }
      }
      .text-warn {
        margin-left: 96px;
        font-size: 16px;
        color: #51D0B1;
        padding-top: 50px;
      }
    }
     ::-webkit-scrollbar{
      width: 5px;
      height: 1px;
    }
    .el-table {
      th {
        .cell:first-child {
          border-left: 2px solid #E7E7E7;
          margin-bottom: 15px;
          font-size: 15px;
          line-height: 15px;
          font-weight: normal;
          color: #17171A;
        }
      }

    }
    .tab-item {
      font-size: 16px;
    }
    .selected-option {
      color:#20bf9a;
      cursor: pointer;
    }
    .fd-page {
      // float: right;
      margin-top:10px;
      text-align: right;
      margin-top: 8px;
    }
  }
  .inner-container {
    width: 100%;
    padding-bottom: 50px;
    justify-content: center;
    display: flex;
    background: #fff;
    .inner-title {
      span:first-child {
        font-size: 16px;
        color: #1989FA;
      }
      span:last-child {
        font-size: 16px;
      }
    }
    .inner-box {
      width: 40%;
      .inner-content{
        width: 100%;
        height: 340px;
        border:  1px solid #D9D9D9;
        overflow: auto;
        border-radius: 2px;
        .el-radio__input {
          display: none
        }
        .el-radio-group{
          div{
            padding-top:16px;
          }
        }
        .el-checkbox-group{
          div{
            padding-top:7px;
          }
          .el-checkbox__input {
            display:none

          }
        }
        .selected-option {
          color: #1989FA;
          cursor: pointer;
        }
      }
    }
    .inner-arrow {
      width: 5%;
      height: 340px;
      line-height: 340px;
      text-align: center;
      font-size: 24px;
      color: #BBBBC3;
    }
    .option-field {
      white-space:nowrap;
      text-overflow:ellipsis;
      cursor: pointer;
      padding:6px 15px 0 8px;
      justify-content: space-between;
      i {
        font-size: 10px;
        color: #1989FA;
      }
    }
    .dialog-footer {
      position: absolute;
      right: 10px;
      bottom: 10px;
      .el-button {
          padding: 5px 13px;
          height: 30px;
          width: 80px;
      }
    }
    // .inner-checkbox {
    //   .el-checkbox-group {
    //     .el-checkbox__input.is-checked+.el-checkbox__label {
    //       color:#20bf9a;
    //     }
    //   }
    // }
  }
  .fd-container {
    .el-dialog__body {
      background: #fff;
      position: relative;
    }
    .el-dialog__header {
      background: #1989FA;

      span.el-dialog__title,i.el-dialog__close {
          // color:#ffffff !important
      }
      .el-dialog__headerbtn {
        .el-dialog__close {
          font-size: 23px;
          color:#ffffff
        }
      }
    }
    .el-dialog__footer {
      .el-button {
          padding: 5px 13px;
          height: 30px;
          width: 80px;
      }
    }
  }
</style>
