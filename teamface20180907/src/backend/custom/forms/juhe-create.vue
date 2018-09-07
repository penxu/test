<template>
  <div class="juhe-create" @click="hiddenAddRow()">
    <!-- 顶部通栏 -->
    <div class="top-bar">
      <div class="back" @click="backHome(1)">
        <i class="iconfont icon-fanhui"></i>
      </div>
      <!-- <el-input id="juhe-name" v-model.trim="juheName" :readonly="juheNameReadonly" @blur="juheNameReadonly = true" :maxlength="25"></el-input> -->
      <el-input id="juhe-name" v-model.trim="juheName" :maxlength="25"></el-input>
      <i class="iconfont icon-xiebeiwangchangtai" @click="juheNameEdit($event)"></i>
      <el-button type="primary" @click="saveJuhe()">确定</el-button>
      <el-button @click="backHome(1)">取消</el-button>
    </div>
    <!-- 内容区域 -->
    <div class="content-area">
      <!-- 展示区 -->
      <div class="show-area">
        <!-- 汇总式 -->
        <!-- <report-summary :reportDatas="huizonData"></report-summary> -->
        <!-- 矩阵式 -->
        <!-- <report-matrix v-if="juzhenData.data.tableData.length > 1" :reportDatas="juzhenData"></report-matrix> -->
        <juhe-table v-if="juzhenData.data.tableData.length > 1"></juhe-table>
        <div class="no-data" v-else>
          <img src="/static/img/no-data.png"/>
          <p>暂无数据，请从右侧添加～</p>
        </div>
      </div>
      <!-- 设置区 -->
      <div class="set-area">
        <!-- 数据来源 -->
        <div class="data-source" @click="dataSourceDialog = true">
          <div class="data-source-btn">数据来源</div>
          <span class="data-source-title" v-if="dataSourceTitile !== ''">{{dataSourceTitile}}</span>
        </div>
        <!-- 行表头 -->
        <div class="data-source">
          <div class="data-title">
            <span class="blockage"></span>
            <span class="title-name pull-left">行表头</span>
            <div class="add pull-right" v-if="dataSourceTitile !== ''" @click="addRow()">
              <i class="iconfont icon-nav-quickly-add"></i>
              <span>添加</span>
            </div>
            <!-- 行表头-添加框 -->
            <div class="add-box" id="add-box" v-if="showAddRow && dataSourceTitile !== ''">
              <p class="add-box-title">
                <i class="iconfont icon-icon-test7"></i>
                <span v-if="dataSourceTitile !== ''">{{dataType2 == '0'? dataSourceTitile:'关联字段'}}</span>
              </p>
              <p class="add-box-item" :class="{'add-box-active':item.isSelect}" v-for="(item,index) in rowListAll" :key="index" @click="selectRow(item,index,rowList,rowListAll)">
                <span class="item-name pull-left">{{item.label}}</span>
                <i class="iconfont icon-pc-paper-optfor pull-right"></i>
              </p>
            </div>
          </div>
          <draggable :options="columndropOptionField" v-model="rowList" class="drag-box">
            <div class="data-item" v-for="(item, index) in rowList" :key="index">
              <i class="iconfont icon-yidong my-handle"></i>
              <el-input class="item-name" v-model.trim="item.label"></el-input>
              <i class="iconfont icon-huishouzhan" @click="rowListDel(item,index)"></i>
              <i class="iconfont icon-liebiao-bianji" @click="getFocus($event)"></i>
            </div>
          </draggable>
        </div>
        <!-- 列表头 -->
        <div class="data-source">
          <div class="data-title">
            <span class="blockage"></span>
            <span class="title-name pull-left">列表头</span>
            <div class="add pull-right" v-if="dataSourceTitile !== ''" @click="addLine()">
              <i class="iconfont icon-nav-quickly-add"></i>
              <span>添加</span>
            </div>
            <!-- 列表头-添加框 -->
            <div class="add-box" id="add-box-line" v-if="showAddLine && dataSourceTitile !== ''">
              <p class="add-box-title">
                <i class="iconfont icon-icon-test7"></i>
                <span>{{dataType2 == '0'? dataSourceTitile:'关联字段'}}</span>
              </p>
              <p class="add-box-item" :class="{'add-box-active':item.isSelect}" v-for="(item,index) in lineListAll" :key="index" @click="selectRow(item,index,lineList,lineListAll)">
                <span class="item-name pull-left">{{item.label}}</span>
                <i class="iconfont icon-pc-paper-optfor pull-right"></i>
              </p>
            </div>
          </div>
          <draggable :options="columndropOptionField" v-model="lineList" class="drag-box">
            <div class="data-item" v-for="(item, index) in lineList" :key="index">
              <i class="iconfont icon-yidong my-handle"></i>
              <el-input class="item-name" v-model.trim="item.label"></el-input>
              <i class="iconfont icon-huishouzhan" @click="lineListDel(item,index)"></i>
              <i class="iconfont icon-liebiao-bianji" @click="getFocus($event)"></i>
            </div>
          </draggable>
        </div>
        <!-- 统计指标 -->
        <div class="data-source">
          <div class="data-title">
            <span class="blockage"></span>
            <span class="title-name pull-left">统计指标</span>
            <div class="add pull-right" v-if="dataSourceTitile !== ''" @click="countTargetDialog = true">
              <i class="iconfont icon-nav-quickly-add"></i>
              <span>添加</span>
            </div>
          </div>
          <draggable :options="columndropOptionField" v-model="countTargetList" class="drag-box">
            <div class="data-item" v-for="(item, index) in countTargetList" :key="index">
              <i class="iconfont icon-yidong my-handle"></i>
              <el-input class="item-name" v-model.trim="item.name"></el-input>
              <i class="iconfont icon-huishouzhan" @click="countTargetList.splice(index,1)"></i>
              <i class="iconfont icon-gaojigongshi" @click="countTargetEdit(item,index)"></i>
            </div>
          </draggable>
        </div>
        <!-- 筛选条件 -->
        <div class="data-source">
          <div class="data-title">
            <span class="blockage"></span>
            <span class="title-name pull-left">筛选条件</span>
            <div class="add pull-right" v-if="dataSourceTitile !== ''" @click="filtConditionVisible = true">
              <i class="iconfont icon-nav-quickly-add"></i>
              <span>添加</span>
            </div>
          </div>
          <div class="condition-ul" v-if="condition[0].field_label !== ''">
            <conditionShow :conditionList='condition' :highCondition='high_where'></conditionShow>
          </div>
        </div>
        <!-- 可见性 -->
        <div class="data-source">
          <div class="data-title">
            <span class="blockage"></span>
            <span class="title-name pull-left">可见性</span>
            <div class="add pull-right" @click='addEmpDepRole'>
              <i class="iconfont icon-nav-quickly-add"></i>
              <span>添加</span>
            </div>
          </div>
          <div class="dash-permision">
            <div class="permision-sel">
              <span class="note" v-if="userList.length == 0">可选择部门、成员、角色，支持多选</span>
              <span v-for="(empItem, index) in userList" :key="index" class="prepare-box" v-if="index <= visibilityLength">
                <div class="name">{{empItem.name}}</div>
                <i class="iconfont icon-pc-shanchu" @click="removeEmpDepRole(empItem)"></i>
              </span>
              <span class="prepare-box" v-if="userList.length - visibilityLength > 1">
                <div class="simpName" style="line-height: 1; background: none;">+{{userList.length - visibilityLength - 1}}</div>
              </span>
            </div>
          </div>
        </div>
        <!-- 运行按钮 -->
        <div class="run-btn">
          <el-button @click="gotoRun(1)">运 行</el-button>
        </div>
      </div>
    </div>
    <!-- 数据来源弹窗 -->
    <div class="data-source-dialog">
      <el-dialog title="数据来源"  width="600px" :visible.sync="dataSourceDialog">
        <div class="data-source-content">
          <!-- 数据来源 -->
          <div class="first-item">
            <span class="name">数据来源</span>
            <span class="single" :class="{'data-type-active':dataType == '0'}" @click="dataType = '0'">单表数据</span>
            <span class="multi" :class="{'data-type-active':dataType == '1'}" @click="dataType = '1'">多表关联</span>
          </div>
          <!-- 数据表 -->
          <div class="second-item">
            <span class="name">数据表</span>
            <!-- 级联单选 -->
            <div v-if="dataType == '0'" class="simple-select">
              <el-cascader
                v-model="selectedOptions3"
                :options="options"
                :placeholder='simpleValue.length < 1 ? "请选择":""'
                @change="addSimpleOptions"
                :show-all-levels="true"
              ></el-cascader>
              <div class="tags">
                <el-tag :key="index"
                v-for="(item,index) in simpleValue"
                closable
                :disable-transitions="true"
                @close="handleCloseSimple(item)"
                >{{item.label}}</el-tag>
              </div>
            </div>
            <!-- 级联多选 -->
            <div v-else class="multi-select">
              <el-cascader
                v-model="selectedOptions3"
                :placeholder='multiValue.length < 1 ? "请选择":""'
                :options="options"
                @change="addMultiOptions"
                :show-all-levels="true"
              ></el-cascader>
              <div class="tags">
                <el-tag :key="index"
                v-for="(item,index) in multiValue"
                closable
                :disable-transitions="true"
                @close="handleClose(item)"
                >{{item.label}}</el-tag>
              </div>
            </div>
          </div>
          <!-- 字段关联 -->
          <div class="third-item" v-if="dataType == '1' && multiValue.length > 1">
            <span class="name pull-left">字段关联</span>
            <div class="table-area pull-left">
              <el-table
                :data="tableData"
                style="width: 100%"
                border>
                <el-table-column
                  fixed
                  width="30">
                  <template slot-scope="scope">
                    <div class="sort-span" @mouseover="showDel($event,scope.$index)"  @mouseout="hiddenDel()">
                      <i class="iconfont icon-Rectangle8" @click="delTableData(scope.$index)" v-if="scope.$index === currentIndex && scope.$index !== 0"></i>
                      <span v-else>{{scope.$index + 1}}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column
                  v-for="(item,index) in multiValue"
                  :key="index"
                  :label="item.label"
                  width="160">
                  <!-- tableData[scope.$index].multiValue[index].selectValue -->
                  <template slot-scope="scope">
                    <el-select v-model="tableData[scope.$index].multiValue[index].selectValue" value-key = 'label' placeholder="请选择">
                      <el-option
                        v-for="val in tableData[scope.$index].multiValue[index].dataList"
                        :key="val.name"
                        :label="val.label"
                        :value="val">
                      </el-option>
                    </el-select>
                  </template>
                </el-table-column>
              </el-table>
              <div class="add-btn" @click="addRowTableData()">
                <i class="iconfont icon-nav-quickly-add"></i>
                <span>添加</span>
              </div>
            </div>
          </div>
          <div class="no-data-third-item" v-if="dataType == '1' && multiValue.length < 2">请选择关联数据</div>
        </div>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dataSourceDialog = false">取 消</el-button>
          <el-button type="primary" @click="dataSourceSure()">确 定</el-button>
        </div>
      </el-dialog>
    </div>
    <!-- 统计指标弹窗 -->
    <div class="count-target-dialog">
      <el-dialog title="统计指标"  width="700px" :visible.sync="countTargetDialog">
        <div class="count-target-content">
          <formula-panel v-if="countTargetDialog" :module-list="moduleList" ref="formulaPanel"></formula-panel>
        </div>
        <div slot="footer" class="dialog-footer">
          <el-button @click="countTargetDialog = false">取 消</el-button>
          <el-button type="primary" @click="countTargetSure()">确 定</el-button>
        </div>
      </el-dialog>
    </div>
    <!-- 筛选条件弹窗 -->
    <div class="filt-condition-dialog">
      <el-dialog
        title="筛选条件"
        :visible.sync="filtConditionVisible"
        width="600px">
        <div class="condition-father">
          <conditionComponent v-if="filtConditionVisible" :allCondition="initFieldList" :highWhere="high_where" :selCondition="condition" ref="conditionComponent"></conditionComponent>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="filtConditionVisible = false">取 消</el-button>
          <el-button type="primary" @click="saveCondition()">确 定</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>
<script>
import draggable from 'vuedraggable'
import tool from '@/common/js/tool.js'
import formulaPanel from './formula-panel' // 公式面板组件
import juheTable from './juhe-table' // 聚合表格组件
import conditionComponent from '@/common/components/condition.vue' // 条件组件
import conditionShow from '@/common/components/condition-show.vue' // 条件回显组件
export default {
  name: 'juheCreate',
  components: {
    draggable,
    formulaPanel,
    conditionComponent,
    conditionShow,
    juheTable
  },
  props: ['juheId'],
  data () {
    return {
      isConfirm: false, // 退出时是否需要二次确认
      juheNameReadonly: true, // 聚合表名称是否只读
      aggregationSurfaceId: '', // 编辑时使用
      currentId: '',
      condition: [
        {
          'field_label': '',
          'field_value': '',
          'operator_label': '',
          'operator_value': '',
          'result_label': '',
          'result_value': '',
          'show_type': '',
          'operators': [],
          'entrys': [],
          'selList': [],
          'selTime': []
        }
      ],
      high_where: '', // 高级条件
      initFieldList: [], // 条件初始化字段列表
      filtConditionVisible: false, // 筛选条件弹窗
      dataSourceDialog: false, // 数据来源弹窗
      juheName: '聚合表的名称',
      isJuheNameEdit: false,
      // 统计指标列表
      countTargetList: [],
      showAddRow: false, // 行表头-添加框
      showAddLine: false, // 列表头-添加框
      visibilityLength: 999999,
      userList: [], // 可见性人员数组
      // 行表头模块名称列表
      rowListAll: [],
      // 列表头模块名称列表
      lineListAll: [],
      // 行表头列表(最终数据)
      rowList: [],
      // 列表头列表(最终数据)
      lineList: [],
      // 数据源-级联数据
      options: [],
      selectedOptions3: [],
      multiValue: [], // 多表数据选中值
      multiValue2: [], // 多表数据选中值(最终值)
      simpleValue: [], // 单表数据选中值
      simpleValue2: [], // 单表数据选中值(最终值)
      dataType: '0', // 数据来源(0单表数据 1多表关联)
      dataType2: '0', // 数据来源(0单表数据 1多表关联)(最终值)
      tableData: [], // 字段关联表格数据
      currentIndex: '', // 当前鼠标移入的索引
      dataSourceTitile: '', // 数据源标题
      countTargetDialog: false, // 统计指标弹窗
      moduleList: [], // 已选中的数据源模块列表
      // 汇总式模拟数据
      huizonData: {
        // data: [{'reportType': 'data', 'reportObj': [{'from': 'bean1533799231541_13_text_1523851995489L=SA##bean1533799231541_13_text_1533799248388L=S规格1', 'bean1533799231541_13_number_1533799308069': 9000, 'bean1533799231541_13_number_1533799321629': 8000, 'bean1533799231541_13_formula_1533799327658': 1000, 'bean1533799231541_13_text_1523851995489': 'A', 'bean1533799231541_13_text_1533799248388': '规格1', 'rowArr': [{'row': 0, 'key': 'bean1533799231541_13_text_1523851995489', 'rowNum': 2}], 'jsonId': 0}, {'jsonId': 1, 'bean1533799231541_13_text_1533799248388': '小计', 'bean1533799231541_13_formula_1533799327658': 1000.0, 'bean1533799231541_13_number_1533799321629': 8000.0, 'bean1533799231541_13_number_1533799308069': 9000.0}, {'from': 'bean1533799231541_13_text_1523851995489L=SB##bean1533799231541_13_text_1533799248388L=S规格2', 'bean1533799231541_13_number_1533799308069': 8000, 'bean1533799231541_13_number_1533799321629': 6000, 'bean1533799231541_13_formula_1533799327658': 2000, 'bean1533799231541_13_text_1523851995489': 'B', 'bean1533799231541_13_text_1533799248388': '规格2', 'rowArr': [{'row': 2, 'key': 'bean1533799231541_13_text_1523851995489', 'rowNum': 2}], 'jsonId': 2}, {'jsonId': 3, 'bean1533799231541_13_text_1533799248388': '小计', 'bean1533799231541_13_formula_1533799327658': 2000.0, 'bean1533799231541_13_number_1533799321629': 6000.0, 'bean1533799231541_13_number_1533799308069': 8000.0}, {'from': 'bean1533799231541_13_text_1523851995489L=SC##bean1533799231541_13_text_1533799248388L=S规格三', 'bean1533799231541_13_number_1533799308069': 6000, 'bean1533799231541_13_number_1533799321629': 9000, 'bean1533799231541_13_formula_1533799327658': -3000, 'bean1533799231541_13_text_1523851995489': 'C', 'bean1533799231541_13_text_1533799248388': '规格三', 'rowArr': [{'row': 4, 'key': 'bean1533799231541_13_text_1523851995489', 'rowNum': 2}], 'jsonId': 4}, {'jsonId': 5, 'bean1533799231541_13_text_1533799248388': '小计', 'bean1533799231541_13_formula_1533799327658': -3000.0, 'bean1533799231541_13_number_1533799321629': 9000.0, 'bean1533799231541_13_number_1533799308069': 6000.0}, {'from': 'bean1533799231541_13_text_1523851995489L=SD##bean1533799231541_13_text_1533799248388L=S规格四', 'bean1533799231541_13_number_1533799308069': 1000, 'bean1533799231541_13_number_1533799321629': 9000, 'bean1533799231541_13_formula_1533799327658': -8000, 'bean1533799231541_13_text_1523851995489': 'D', 'bean1533799231541_13_text_1533799248388': '规格四', 'rowArr': [{'row': 6, 'key': 'bean1533799231541_13_text_1523851995489', 'rowNum': 2}], 'jsonId': 6}, {'jsonId': 7, 'bean1533799231541_13_text_1533799248388': '小计', 'bean1533799231541_13_formula_1533799327658': -8000.0, 'bean1533799231541_13_number_1533799321629': 9000.0, 'bean1533799231541_13_number_1533799308069': 1000.0}, {'from': 'bean1533799231541_13_text_1523851995489L=SE##bean1533799231541_13_text_1533799248388L=S规格五', 'bean1533799231541_13_number_1533799308069': 5000, 'bean1533799231541_13_number_1533799321629': 9800, 'bean1533799231541_13_formula_1533799327658': -4800, 'bean1533799231541_13_text_1523851995489': 'E', 'bean1533799231541_13_text_1533799248388': '规格五', 'rowArr': [{'row': 8, 'key': 'bean1533799231541_13_text_1523851995489', 'rowNum': 2}], 'jsonId': 8}, {'jsonId': 9, 'bean1533799231541_13_text_1533799248388': '小计', 'bean1533799231541_13_formula_1533799327658': -4800.0, 'bean1533799231541_13_number_1533799321629': 9800.0, 'bean1533799231541_13_number_1533799308069': 5000.0}, {'bean1533799231541_13_number_1533799308069': 29000, 'bean1533799231541_13_number_1533799321629': 41800, 'bean1533799231541_13_formula_1533799327658': -12800, 'bean1533799231541_13_text_1523851995489': '合计', 'rowArr': [{'colNum': 2, 'row': 10, 'key': 'bean1533799231541_13_text_1523851995489'}]}]}, {'reportType': 'title', 'reportObj': [{'name': 'bean1533799231541_13_text_1523851995489', 'label': '商品名称'}, {'name': 'bean1533799231541_13_text_1533799248388', 'label': '规格'}, {'name': 'bean1533799231541_13_number_1533799308069', 'label': '入库数量'}, {'name': 'bean1533799231541_13_number_1533799321629', 'label': '出库数量'}, {'name': 'bean1533799231541_13_formula_1533799327658', 'label': '库存'}]}],
        data: [],
        isPreview: 1
      },
      // 矩阵模拟数据
      juzhenData: {
        data: {
          tableData: [
            // {'1': '单位', '2': '', '3': '台', '4': '', '5': '个', '6': '', '7': '支', '8': '', '9': '总计', '10': '', 'rowArr': [{'colNum': 2, 'row': 0, 'key': '1'}, {'colNum': 2, 'row': 0, 'key': '3'}, {'colNum': 2, 'row': 0, 'key': '5'}, {'colNum': 2, 'row': 0, 'key': '7'}, {'colNum': 2, 'row': 0, 'key': '9'}]},
            // {'1': '物品名称', '2': '规格', '3': '入库数量', '4': '库存', '5': '入库数量', '6': '库存', '7': '入库数量', '8': '库存', '9': '入库数量', '10': '库存'},
            // {'1': 'A', '2': '规格一', '3': '100', '4': '200', '5': '/', '6': '/', '7': '/', '8': '/', '9': '100', '10': '200'},
            // {'1': 'B', '2': '规格二', '3': '/', '4': '/', '5': '100', '6': '200', '7': '/', '8': '/', '9': '100', '10': '200'},
            // {'1': 'C', '2': '规格三', '3': '/', '4': '/', '5': '/', '6': '/', '7': '200', '8': '300', '9': '200', '10': '300'},
            // {'1': 'D', '2': '规格四', '3': '/', '4': '/', '5': '20', '6': '100', '7': '/', '8': '/', '9': '20', '10': '100'},
            // {'1': '合计', '2': '', '3': '100', '4': '200', '5': '120', '6': '300', '7': '200', '8': '300', '9': '420', '10': '800', 'rowArr': [{'key': '1', 'row': 6, 'colNum': 2}]}
          ],
          // columns: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10']
          columns: []
        },
        isPreview: 1,
        colgroup: [],
        summary: []
      }
    }
  },
  created () {
    console.log(this.juheId, 'juheId')
    if (this.juheId !== '') {
      // 根据id获取详情数据
      this.currentId = this.juheId
      this.getDetailById(this.juheId)
    }
    // 获取数据源级联列表
    this.$http.getAllApplications().then(res => {
      this.options = res
    })
  },
  mounted () {
    document.querySelector('.top-bar > .el-input .el-input__inner').style.width = this.juheName.length * 20 + 'px'
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value) {
        if (value.prepareKey === 'juhe-create') {
          this.userList = value.prepareData
          this.permisionInit()
        }
      }
    })
    // 接收设置好的公式
    this.$bus.off('getFormula')
    this.$bus.on('getFormula', val => {
      console.log(val, 'getFormula')
      this.formula = val
    })
  },
  methods: {
    // 根据id获取详情数据
    getDetailById (id) {
      this.$http.getJuheById({'id': id}).then(res => {
        console.log(res, '根据id获取详情数据')
        this.aggregationSurfaceId = res.aggregationSurfaceId
        this.dataType = this.dataType2 = res.dataSource.length > 1 ? '1' : '0'// 数据源类型
        this.dataType2 === '0' ? this.simpleValue = res.dataSource : this.multiValue = res.dataSource // 数据源
        this.tableData = res.tableData // 关联字段
        this.rowList = res.rowList // 行表头
        this.lineList = res.lineList // 列表头
        this.countTargetList = res.countTargetList // 统计指标列表
        this.condition = res.condition // 条件
        this.high_where = res.high_where // 高级条件
        this.userList = res.userList // 可见人员列表
        // 初始化人员显示样式
        this.permisionInit()
        this.juheName = res.title // 聚合表名称
        this.dataSourceTitile = res.source_lable // 数据源标题
        if (this.dataType2 === '0') {
          // 单表
          this.rowListAll = JSON.parse(JSON.stringify(this.simpleValue[0].dataList))
          this.lineListAll = JSON.parse(JSON.stringify(this.simpleValue[0].dataList))
          this.moduleList = JSON.parse(JSON.stringify(this.simpleValue))
          this.simpleValue2 = JSON.parse(JSON.stringify(this.simpleValue))
        } else {
          // 多表
          this.rowListAll = JSON.parse(JSON.stringify(this.tableData))
          this.lineListAll = JSON.parse(JSON.stringify(this.tableData))
          this.moduleList = JSON.parse(JSON.stringify(this.multiValue))
          this.multiValue2 = JSON.parse(JSON.stringify(this.multiValue))
        }
        // 运行聚合表
        this.gotoRun()
      })
    },
    // 运行聚合表
    gotoRun (tag) {
      // 非空验证
      if (this.simpleValue2.length < 1 && this.multiValue2.length < 1) {
        this.$message.error('数据来源不能为空')
        return false
      }
      if (this.rowList.length < 1) {
        this.$message.error('行表头不能为空')
        return false
      }
      if (this.countTargetList.length < 1) {
        this.$message.error('统计指标不能为空')
        return false
      }
      let obj = {
        'dataType': this.dataType2, // 数据源类型
        'dataSource': this.dataType2 === '0' ? this.simpleValue2 : this.multiValue2, // 数据源
        'tableData': this.tableData, // 关联字段
        'rowList': this.rowList, // 行表头
        'lineList': this.lineList, // 列表头
        'countTargetList': this.countTargetList, // 统计指标列表
        'condition': this.condition, // 条件
        'high_where': this.high_where, // 高级条件
        'userList': this.userList // 可见人员列表
      }
      this.$http.gotoRun(obj).then(res => {
        if (tag) {
          this.isConfirm = true
        }
        this.$set(this.juzhenData.data, 'columns', res.columns || [])
        this.$set(this.juzhenData.data, 'tableData', res.tableData || [])
        this.$set(this.juzhenData, 'data', this.juzhenData.data)
        setTimeout(() => {
          this.$bus.emit('reportDatasUpdate', this.juzhenData)
        }, 10)
      })
    },
    // 新增聚合表
    saveJuhe () {
      // 非空验证
      if (this.juheName === '') {
        this.$message.error('聚合表名称不能为空')
        return false
      }
      if (this.simpleValue2.length < 1 && this.multiValue2.length < 1) {
        this.$message.error('数据来源不能为空')
        return false
      }
      if (this.rowList.length < 1) {
        this.$message.error('行表头不能为空')
        return false
      }
      if (this.countTargetList.length < 1) {
        this.$message.error('统计指标不能为空')
        return false
      }
      let obj = {
        'dataType': this.dataType2, // 数据源类型
        'dataSource': this.dataType2 === '0' ? this.simpleValue2 : this.multiValue2, // 数据源
        'tableData': this.tableData, // 关联字段
        'rowList': this.rowList, // 行表头
        'lineList': this.lineList, // 列表头
        'countTargetList': this.countTargetList, // 统计指标列表
        'condition': this.condition, // 条件
        'high_where': this.high_where, // 高级条件
        'userList': this.userList, // 可见人员列表
        'title': this.juheName, // 聚合表名称
        'source_lable': this.dataSourceTitile // 数据源标题
      }
      if (this.currentId !== '') {
        // 编辑
        obj.id = this.currentId
        obj.aggregationSurfaceId = this.aggregationSurfaceId
        this.$http.updateJuhe(obj).then(res => {
          this.$message({
            message: '编辑成功',
            type: 'success'
          })
          // 返回列表
          this.backHome()
          this.currentId = ''
          this.aggregationSurfaceId = ''
        })
      } else {
        // 新增
        this.$http.saveJuhe(obj).then(res => {
          console.log(res, '保存聚合表')
          this.$message({
            message: '保存成功',
            type: 'success'
          })
          // 返回列表
          this.backHome()
        })
      }
    },
    // 删除rowList
    rowListDel (item, index) {
      this.rowList.splice(index, 1)
      // rowListAll同步去掉对勾
      this.rowListAll.some((val, index) => {
        if (val.name === item.name) {
          // 去除勾选
          delete this.rowListAll[index].isSelect
          this.$set(this.rowListAll, index, this.rowListAll[index])
          return true
        }
      })
    },
    // 删除lineList
    lineListDel (item, index) {
      this.lineList.splice(index, 1)
      // rowListAll同步去掉对勾
      this.lineListAll.some((val, index) => {
        if (val.name === item.name) {
          // 去除勾选
          delete this.lineListAll[index].isSelect
          this.$set(this.lineListAll, index, this.lineListAll[index])
          return true
        }
      })
    },
    // 保存条件(保存条件数据时要调用的)
    saveCondition () {
      if (this.$refs.conditionComponent.judgeFilter()) {
        this.condition = JSON.parse(JSON.stringify(this.$refs.conditionComponent.handleLastData()))
        this.high_where = JSON.parse(JSON.stringify(this.$refs.conditionComponent.high_where))
      }
      console.log(this.high_where, 'high_where')
      console.log(this.condition, 'condition')
      this.filtConditionVisible = false
    },
    // 获取条件初始化数据
    getInitCondition () {
      let chooseBean = []
      if (this.dataType2 === '0') {
        // 单表
        this.simpleValue2.map(item => {
          chooseBean.push(item.english_name)
        })
      } else {
        // 多表
        this.multiValue2.map(item => {
          chooseBean.push(item.english_name)
        })
      }
      // chooseBean 选择的维度+数值来源bean
      this.$http.getReportEditLayoutFilterFields({'chooseBean': chooseBean.join(',')}).then((res) => {
        this.initFieldList = res
        // this.advancedFilterForm = true
        // this.conditionData = this.attributeData.condition
        // this.highCondition = this.attributeData.highWhere
      })
    },
    // 编辑统计指标数据
    countTargetEdit (data, index) {
      // 获取data对象,发送给子组件显示
      console.log(data)
      this.countTargetDialog = true
      setTimeout(() => {
        this.$bus.emit('editShowFormula', {data: data, index: index})
      }, 100)
    },
    // 统计指标 - 确定按钮
    countTargetSure () {
      this.$refs.formulaPanel.saveFormula()
      // 非空验证
      if (this.formula.name === '') {
        this.$message.error('统计指标名称不能为空')
        return
      }
      if (this.formula.en === '') {
        this.$message.error('统计指标公式不能为空')
        return
      }
      if (this.formula.isEdit) {
        // 编辑
        this.countTargetList.splice(this.formula.indexEdit, 1, this.formula)
      } else {
        // 新增
        this.countTargetList.push(this.formula)
      }
      this.countTargetDialog = false
    },
    // 数组对比,获取选中的数据加标识
    listDeal (newArr, allArr) {
      allArr.map((item, index) => {
        newArr.map(val => {
          if (val.name === item.name) {
            item['isSelect'] = true
          }
        })
        this.$set(allArr, index, item)
      })
    },
    // 选择行/列表头
    selectRow (item, num, newArr, oldArr) {
      if (newArr.length < 1) {
        newArr.push(item)
        oldArr[num]['isSelect'] = true
        this.$set(oldArr, num, oldArr[num])
        // 行列表头数据汇总
        // this.dataList = this.rowList.concat(this.lineList)
      } else {
        // toggle处理
        let tag = true
        newArr.some((val, index) => {
          if (val.name === item.name) {
            // 去除勾选
            delete oldArr[num].isSelect
            this.$set(oldArr, num, oldArr[num])
            newArr.splice(index, 1)
            // 行列表头数据汇总
            // this.dataList = this.rowList.concat(this.lineList)
            tag = false
            return true
          }
        })
        if (tag) {
          // 加上勾选
          newArr.push(item)
          oldArr[num]['isSelect'] = true
          this.$set(oldArr, num, oldArr[num])
          // 行列表头数据汇总
          // this.dataList = this.rowList.concat(this.lineList)
          return true
        }
      }
    },
    // 添加行表头
    addRow () {
      this.showAddRow = true
      this.showAddLine = false
      this.listDeal(this.rowList, this.rowListAll)
    },
    // 点击到了add-box以外的区域隐藏添加框
    hiddenAddRow () {
      if (this.showAddRow) {
        let sp = document.getElementById('add-box')
        if (sp) {
          if (!sp.contains(event.target)) {
            this.showAddRow = false
            this.showAddLine = false
          }
        }
      } else {
        let sp = document.getElementById('add-box-line')
        if (sp) {
          if (!sp.contains(event.target)) {
            this.showAddRow = false
            this.showAddLine = false
          }
        }
      }
    },
    // 添加列表头
    addLine () {
      this.showAddLine = true
      this.showAddRow = false
      this.listDeal(this.lineList, this.lineListAll)
    },
    // 获取焦点
    getFocus (e) {
      e.path[1].childNodes[2].children[0].focus()
    },
    /** 成员样式展示 */
    permisionInit () {
      setTimeout(() => {
        var children = document.getElementsByClassName('permision-sel')[0].children
        var width = 0
        for (var i = 0; i < children.length; i++) {
          if (children[i].className.indexOf('prepare-box') >= 0) {
            width += children[i].clientWidth
          }
          if (width + i * 12 > 200) {
            this.visibilityLength = i - 1
            break
          }
        }
      }, 20)
    },
    //* *添加成员、部门、角色  */
    addEmpDepRole () {
      this.$bus.emit('commonMember', {'prepareData': this.userList, 'prepareKey': 'juhe-create', 'seleteForm': true, 'banData': [], 'navKey': '1,0,2', 'removeData': []}) // 给父组件传值
    },
    //* *移除成员、部门、角色  */
    removeEmpDepRole (data) {
      var contains = tool.contains(this.userList, 'value', data, 'value')
      if (contains) {
        this.userList.splice(contains.i, 1)
      }
    },
    // 聚合表名称获取焦点
    juheNameEdit (event) {
      // this.juheNameReadonly = false
      document.querySelector('.top-bar > .el-input .el-input__inner').focus()
      // this.moveToEnd(document.querySelector('.top-bar > .el-input .el-input__inner'))
    },
    // 光标一直最后
    moveToEnd (el) {
      if (typeof el.selectionStart === 'number') {
        el.selectionStart = el.selectionEnd = el.value.length
      } else if (typeof el.createTextRange !== 'undefined') {
        el.focus()
        var range = el.createTextRange()
        range.collapse(false)
        range.select()
      }
    },
    // 回到报表首页
    backHome (tag) {
      if (tag) {
        // 需要判断是否加二次弹窗
        if (this.isConfirm) {
          this.$confirm('数据还未保存,是否退出?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.$bus.emit('showJuheCreate')
          }).catch(() => {
            // 留在原地
          })
        } else {
          this.$bus.emit('showJuheCreate')
        }
      } else {
        // 直接返回
        this.$bus.emit('showJuheCreate')
      }
    },
    // 添加多选模块
    addMultiOptions (value) {
      this.selectedOptions3 = []
      // 去重
      let flag = false
      this.multiValue.some(item => {
        if (item.value === value[1]) {
          this.$message.error('该数据已存在')
          flag = true
          return true
        }
      })
      if (flag) {
        return
      }
      // 根据value值遍历所有children数组 获取完整对象
      let obj = {}
      this.options.some(item => {
        item.children.some(val => {
          if (value[1] === val.value) {
            obj = val
          }
        })
      })
      obj['selectValue'] = ''
      // 根据obj.english_name 获取数据列表
      this.$http.getDataListFieldsForJuhe({'bean': obj.english_name}).then(res => {
        obj['dataList'] = res
        this.$set(this.multiValue, this.multiValue.length, obj)
        // 初始化tableData字段关联数据
        this.innitTableData()
        // 添加列
        this.addLineTableData()
      })
    },
    // 添加单表数据
    addSimpleOptions (value) {
      this.selectedOptions3 = []
      // 根据value值遍历所有children数组 获取完整对象
      let obj = {}
      this.options.some(item => {
        item.children.some(val => {
          if (value[1] === val.value) {
            obj = val
          }
        })
      })
      // 根据obj.english_name 获取数据列表
      this.$http.getDataListFieldsForJuhe({'bean': obj.english_name}).then(res => {
        obj['dataList'] = res
        // 选中的obj赋值给this.simpleValue
        this.simpleValue.splice(0, 1, obj)
      })
    },
    // 删除多选已选模块
    handleClose (value) {
      let index = this.multiValue.indexOf(value)
      this.multiValue.splice(index, 1)
      // tableData同步更新
      this.tableData.map(item => {
        item.multiValue.splice(index, 1)
      })
    },
    // 删除单选已选模块
    handleCloseSimple (value) {
      this.simpleValue.splice(this.simpleValue.indexOf(value), 1)
    },
    // 添加字段关联表格(添加行)
    addRowTableData () {
      let obj = {
        multiValue: JSON.parse(JSON.stringify(this.multiValue)),
        name: new Date().getTime() + ''
      }
      this.$set(this.tableData, this.tableData.length, JSON.parse(JSON.stringify(obj)))
    },
    // 添加字段关联表格(添加列)
    addLineTableData () {
      this.tableData.map((item, index) => {
        this.$set(item.multiValue, this.multiValue.length - 1, JSON.parse(JSON.stringify(this.multiValue[this.multiValue.length - 1])))
      })
    },
    // 初始化tableData字段关联数据(至少有1条数据)
    innitTableData () {
      if (this.tableData.length < 1) {
        this.addRowTableData()
      }
    },
    // 鼠标移入序号显示删除图标
    showDel (e, index) {
      console.log(index)
      this.currentIndex = index
    },
    // 隐藏删除图标
    hiddenDel () {
      this.currentIndex = ''
    },
    // 删除TableData
    delTableData (index) {
      this.tableData.splice(index, 1)
    },
    // 数据源 - 确定按钮
    dataSourceSure () {
      // 非空验证
      if (this.dataType === '1' && this.multiValue.length < 1) {
        this.$message.error('数据表不能为空')
        return
      }
      if (this.dataType === '0' && this.simpleValue.length < 1) {
        this.$message.error('数据表不能为空')
        return
      }
      if (this.dataType === '1') {
        let flag = false
        this.tableData.some(item => {
          item.multiValue.some(item => {
            if (item.selectValue === '') {
              flag = true
              return true
            }
          })
        })
        if (flag) {
          this.$message.error('字段关联不能为空')
          return
        }
      }
      // 数据格式验证
      // if (this.dataType === '1') {
      //   let flag = false
      //   this.tableData.some(item => {
      //     item.multiValue.some(val => {
      //       // 判断所选类型是否不一致(增加需求:如果是单行文本则可以匹配所有类型)
      //       if (item.multiValue[0].selectValue.type !== val.selectValue.type && item.multiValue[0].selectValue.type !== 'text') {
      //         flag = true
      //         return true
      //       }
      //     })
      //   })
      //   if (flag) {
      //     this.$message.error('所选关联字段数据类型不一致')
      //     return
      //   }
      // }

      // 如果数据源有值,则需要弹框提示
      if (this.dataSourceTitile !== '') {
        // 需确认
        this.$confirm('此操作将重置之前设置的参数, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          // 确定修改数据源后的操作
          this.afterSave()
        })
      } else {
        // 无需确认
        this.afterSave()
      }
    },
    // 确定修改数据源后的操作
    afterSave () {
      // 重置所有参数
      this.rowList = []
      this.lineList = []
      this.rowListAll = []
      this.lineListAll = []
      this.countTargetList = []
      this.condition = [
        {
          'field_label': '',
          'field_value': '',
          'operator_label': '',
          'operator_value': '',
          'result_label': '',
          'result_value': '',
          'show_type': '',
          'operators': [],
          'entrys': [],
          'selList': [],
          'selTime': []
        }
      ]
      this.high_where = ''
      let arr = []
      if (this.dataType === '1') {
        // 多表类型
        this.multiValue.map(item => {
          arr.push(item.label)
        })
        // 给tableData每一项添加label和field(设置面板展示用)
        this.tableData.map((item, index) => {
          // let arr1 = []
          // item.multiValue.map(val => {
          //   arr1.push(val.selectValue.label)
          // })
          // item.label = arr1.join('-')
          item.label = item.multiValue[0].selectValue.label
          item.field = item.multiValue[0].selectValue.name
        })
        // 把选中的数据表数据赋值给行列模块列表
        this.rowListAll = JSON.parse(JSON.stringify(this.tableData))
        this.lineListAll = JSON.parse(JSON.stringify(this.tableData))
        this.moduleList = JSON.parse(JSON.stringify(this.multiValue))
        this.multiValue2 = JSON.parse(JSON.stringify(this.multiValue))
        this.dataType2 = this.dataType
      } else {
        // 单表类型
        this.simpleValue.map(item => {
          arr.push(item.label)
        })
        // 把选中的数据表数据赋值给行列模块列表
        this.rowListAll = JSON.parse(JSON.stringify(this.simpleValue[0].dataList))
        this.lineListAll = JSON.parse(JSON.stringify(this.simpleValue[0].dataList))
        this.moduleList = JSON.parse(JSON.stringify(this.simpleValue))
        this.simpleValue2 = JSON.parse(JSON.stringify(this.simpleValue))
        this.dataType2 = this.dataType
      }
      this.dataSourceTitile = arr.join(';')
      this.dataSourceDialog = false
      // 聚合表区域隐藏
      this.juzhenData.data.tableData = []
      this.juzhenData.data.columns = []
    }
  },
  watch: {
    // 监听聚合表名称长度
    juheName () {
      if (this.juheName.length * 20 <= 100) {
        return
      }
      document.querySelector('.top-bar > .el-input .el-input__inner').style.width = this.juheName.length * 20 + 'px'
    },
    // 监控条件弹窗
    filtConditionVisible () {
      if (this.filtConditionVisible) {
        // 获取条件初始化数据
        this.getInitCondition()
      }
    },
    // 监听数据源弹窗
    dataSourceDialog () {
      if (this.dataSourceDialog) {
        // 开窗时,把实际值赋给展示值
        this.dataType = JSON.parse(JSON.stringify(this.dataType2))
        this.simpleValue = JSON.parse(JSON.stringify(this.simpleValue2))
        this.multiValue = JSON.parse(JSON.stringify(this.multiValue2))
      }
    }
  },
  computed: {
    // 拖拽配置
    columndropOptionField () {
      return {
        animation: 200,
        group: { name: 'field', pull: false, put: false },
        sort: true,
        ghostClass: 'ghost',
        handle: '.my-handle'
      }
    }
  }
}
</script>
<style lang="scss">
.juhe-create {
  height: 100%;
  .el-dialog {
    .el-dialog__header {
      padding: 15px 20px 15px;
      border-bottom: 1px solid #E5E5E5;
      .el-dialog__title {
        font-size: 16px;
      }
    }
    .el-dialog__body {
      padding: 20px;
    }
    .el-dialog__footer {
      padding: 10px 20px 10px;
      border-top: 1px solid #E5E5E5;
      .el-button {
        padding: 7px 20px;
      }
    }
  }
  // 顶部通栏
  .top-bar {
    padding-right: 42px;
    height: 60px;
    background: #4A5458;
    .back {
      float: left;
      width: 92px;
      height: 100%;
      background: #3E484D;
      border-right: 1px solid #000;
      text-align: center;
      cursor: pointer;
      >i {
        line-height: 60px;
        color: #fff;
        font-size: 25px;
      }
    }
    .el-input {
      float: left;
      margin-top: 10px;
      width: auto;
      .el-input__inner {
        background: #4A5458;
        color: #fff;
        font-size: 20px;
        width: 120px;
        border: 0;
        padding: 0;
        margin-left: 20px;
      }
    }
    >i {
      float: left;
      color: #fff;
      font-size: 30px;
      margin-top: 14px;
      margin-left: 10px;
      cursor: pointer;
    }
    >.el-button {
      float: right;
      margin-top: 14px;
      margin-right: 8px;
      width: 65px;
      height: 32px;
      padding: 8px 0;
    }
  }
  // 内容区域
  .content-area {
    height: calc(100% - 60px);
    overflow: hidden;
    // 展示区
    .show-area {
      float: left;
      width: calc(100% - 320px);
      height: 100%;
      padding: 10px;
      .no-data {
        width: 100%;
        height: 100%;
        padding-top: 130px;
        text-align: center;
        >img {
          width: 200px;
        }
        >p {
          font-size: 14px;
          color: #666666;
        }
      }
    }
    // 设置区
    .set-area {
      padding: 0 10px;
      width: 320px;
      height: 100%;
      float: right;
      background: #F5F6F7;
      box-shadow: 0 1px 8px -3px;
      overflow-y: auto;
      padding-bottom: 15px;
      .data-source {
        width: 300px;
        min-height: 60px;
        text-align: center;
        border-radius: 4px;
        background-color: #fff;
        padding: 10px 15px;
        margin-top: 10px;
        box-shadow: 0 1px 8px 0 rgba(0,0,0,0.10);
        .condition-ul {
          text-align: left;
          .condition-show {
            .condition-list {
              .item {
                color: #333;
                >span {
                  color: #333;
                }
              }
            }
            >p {
              font-size: 14px;
              color: #333333;
            }
          }
        }
        .data-source-title {
          display: inline-block;
          margin-top: 10px;
        }
        .data-source-btn {
          width: 270px;
          height: 36px;
          line-height: 36px;
          color: #fff;
          text-align: center;
          border-radius: 4px;
          background-color: #0070D2;
          cursor: pointer;
        }
        .data-title {
          position: relative;
          height: 20px;
          margin-bottom: 8px;
          .blockage {
            position: absolute;
            top: 4px;
            left: -15px;
            width: 3px;
            height: 13px;
            background-color: #6C7582;
          }
          .title-name {
            font-size: 14px;
            color: rgba(0,0,0,0.85);
          }
          .add {
            font-size: 12px;
            color: #1890FF;
            cursor: pointer;
          }
          // 添加框
          .add-box {
            width: 280px;
            padding-bottom: 10px;
            box-shadow: 2px -2px 4px 0 rgba(155,155,155,0.30), -2px 2px 4px 0 rgba(155,155,155,0.30);
            border-radius: 4px;
            background-color: #FFFFFF;
            position: absolute;
            z-index: 999;
            top: 28px;
            left: -5px;
            .add-box-title {
              overflow: hidden;
              height: 40px;
              padding-top: 10px;
              padding-left: 12px;
              border-bottom: 1px solid #D9D9D9;
              >i {
                float: left;
                margin-right: 5px;
              }
              >span {
                float: left;
              }
            }
            .add-box-item {
              overflow: hidden;
              padding: 8px 14px 8px 30px;
              >i {
                font-size: 12px;
                margin-top: 4px;
                display: none;
              }
              &:hover {
                background: #F2F2F2;
              }
              .item-name {
                max-width: 210px;
                overflow: hidden;
                height: 20px;
              }
            }
            .add-box-active {
              color: #1890FF;
              >i {
                display: inline-block;
                color: #1890FF;
              }
            }
          }
        }
        .data-item {
          overflow: hidden;
          margin-top: 5px;
          background: #FFFFFF;
          border: 1px solid #E9E9E9;
          border-radius: 4px;
          height: 36px;
          .icon-yidong {
            font-size: 24px;
            color: #BBBBC3;
            float: left;
            margin-top: 4px;
            cursor: move;
          }
          .item-name {
            font-size: 14px;
            color: #333333;
            float: left;
            margin-top: 6px;
            margin-left: 5px;
          }
          .el-input {
            width: auto;
            .el-input__inner {
              width: 180px;
              padding: 0;
              border: 0;
              height: auto;
              line-height: 22px;
            }
          }
          .icon-liebiao-bianji,.icon-huishouzhan,.icon-gaojigongshi {
            color: #666;
            float: right;
            font-size: 14px;
            cursor: pointer;
            margin-top: 8px;
            margin-right: 10px;
            display: none;
          }
          .icon-gaojigongshi {
            color: #447ED9;
            font-size: 18px;
            margin-top: 7px;
          }
        }
        .data-item:hover .icon-liebiao-bianji {
          display: inline-block;
        }
        .data-item:hover .icon-huishouzhan {
          display: inline-block;
        }
        .data-item:hover .icon-gaojigongshi {
          display: inline-block;
        }
        // 可见性-选人控件
        .dash-permision {
          display: inline-block;
          width: 100%;
          .permision-sel {
            width: 100%;
            border: 1px solid #E7E7E7;
            border-radius: 3px;
            height: 30px;
            .icon-pc-paper-accret{float: right;margin: -11px 5px 0 0;cursor: pointer;}
            .note{font-size: 14px;color: #979797;float: left;margin: -10px 0 0 10px;}
          }
          .permision-title {
            padding-left: 13px;
            padding-right: 13px;
            margin-right: 10px;
          }
          div {
            display: inline-block;
            line-height: 50px;
            vertical-align: middle;
            color: #4A4A4A;
          }
          .prepare-box{
            position: relative;
            float: left;
            line-height: 24px;
            padding: 0 4px;
            margin: 2px 8px 0 4px;
            background: #E9EDF2;
            color: #4A4A4A;
            border-radius: 3px;
            .name{line-height: 24px;}
            .iconfont{
              font-size: 12px;
              color: #ccc;
              line-height: 1;
              cursor: pointer;
              &:hover {
                color: #666;
              }
            }
          }
        }
      }
      .run-btn {
        width: 300px;
        margin-top: 10px;
        .el-button {
          width: 100%;
        }
      }
    }
  }
  // 数据来源弹窗
  .data-source-dialog {
    .data-source-content {
      min-height: 300px;
      .first-item {
        overflow: hidden;
        margin-bottom: 20px;
        >span {
          float: left;
          margin-right: 15px;
        }
        .name {
          font-size: 14px;
          color: #666666;
          margin-top: 7px;
        }
        .single,.multi {
          width: 92px;
          height: 32px;
          text-align: center;
          line-height: 32px;
          border: 1px solid #D9D9D9;
          border-radius: 4px;
          cursor: pointer;
        }
        .data-type-active {
          color: #1890FF;
          border-color: #1890FF;
        }
      }
      .second-item {
        .name {
          margin-right: 25px;
        }
        .simple-select {
          margin-bottom: 96px;
        }
        .simple-select,.multi-select {
          display: inline-block;
          position: relative;
          width: 470px;
          .el-cascader {
            width: 100%;
            line-height: 36px;
            height: 36px;
            .el-input {
              input {
                height: 36px;
                line-height: 36px;
              }
            }
          }
          .tags {
            position: absolute;
            top: 4px;
            left: 7px;
            max-width: 428px;
            height: 28px;
            overflow: hidden;
            .el-tag {
              margin-right: 3px;
              height: 28px;
              line-height: 26px;
              background: #E9EDF2;
              border: none;
              color: #4A4A4A;
              .el-icon-close {
                color: #ccc;
                &:hover {
                  background-color: #999;
                }
              }
            }
          }
        }
      }
      .third-item {
        overflow: hidden;
        margin-top: 16px;
        .name {
          margin-top: 15px;
        }
        .table-area {
          max-width: 470px;
          margin-left: 14px;
          .add-btn {
            margin-top: 14px;
            width: 60px;
            >i {
              font-size: 16px;
              color: #1890FF;
            }
            >span {
              font-size: 16px;
              color: #1890FF;
            }
            cursor: pointer;
          }
          .el-table {
            td {
              padding:0;
              height: 40px;
              >.cell {
                padding: 0;
              }
            }
            .sort-span {
              display: inline-block;
              width: 100%;
              text-align: center;
              cursor: pointer;
              >i {
                color: #FF3B30;
              }
            }
            th {
              >.cell {
                text-align: center;
                font-size: 14px;
                color: #000000;
                font-weight: 400;
              }
            }
            .el-select {
              margin: 0 10px;
              .el-input__inner {
                height: 30px;
                line-height: 30px;
              }
            }
          }
        }
      }
      .no-data-third-item {
        height: 190px;
        background: #E9EDF2;
        border-radius: 4px;
        text-align: center;
        line-height: 200px;
        margin: 17px 20px 0 72px;
      }
    }
  }
}
</style>
