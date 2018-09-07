<template>
  <div class="clear noti-container">
    <div class="clear">
      <div class="ft-title">
        <span>消息推送</span>
        <span>基于某种条件发送消息提醒指定的成员。</span>
      </div>
      <div class="noti-addbtn">
        <el-button type="primary" icon="el-icon-plus" @click="handleAddNew()">新增</el-button>
      </div>
    </div>
    <div class="ft-table">
      <el-table
        :data="tem_tabledata"
        style="width: 100%"
        v-loading="getListLoading"
        element-loading-text="拼命加载中..."
        element-loading-spinner="el-icon-loading"
        element-loading-background="rgba(0, 0, 0, 0)">
        <el-table-column
          label="触发动作"
          width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.active }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="规则条件"
          width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.rule }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="提醒时间"
          width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.remindTime }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="被提醒人"
          width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.remindeder }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="提醒内容"
          width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.remindAbout }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="修改人"
          width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.modifyBy }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="修改时间"
          width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.modifyTime | formatDate('yyyy-MM-dd HH:mm') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button
              type="text"
              @click="handleEdit(scope.$index, scope.row)" v-if="scope.row.status === 0">编辑
            </el-button>
            <el-button
              type="text"
              @click="disableNotiRule('forbidon',scope.$index, scope.row)" v-if="scope.row.status === 0 ">禁用</el-button>
            <el-button
              type="text"
              @click="disableNotiRule('onUsed',scope.$index, scope.row)" v-if="scope.row.status === 1">启用</el-button>
            <el-button
              type="text"
              @click="deleteNotiRule(scope.$index, scope.row)"
              v-if="scope.row.status === 1"
              >删除
              </el-button>
          </template>
        </el-table-column>
        <template>
          <div v-show="notiHisList.length === 0" slot="empty">
            <div style="width: 200px; height: 200px;">
              <img src="/static/img/no-data.png" style="width: 100%; height: 100%;">
              <p>暂无数据~</p>
            </div>
          </div>
        </template>
      </el-table>
    </div>
    <div class="clear">
      <div class="block ft-page pull-right">
        <el-pagination
          :current-page="currentPage"
          :page-sizes="[5, 10, 20, 50]"
          :page-size="sizePage"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalData"
          @size-change ="handleSizeChange($event)"
          @current-change = "handleCurrentChange($event)">
        </el-pagination>
      </div>
    </div>
    <el-dialog
      title="添加规则"
      :visible.sync="noti_dialogVisible"
      width="700px"
      @close = "closeDialog()">
      <div class="ds-dialog-content">
        <div class="ds-item clear">
          <div class="pull-left ds-text"><span>触发事件</span></div>
          <div class="pull-left ds-input">
            <el-select
            v-model="triggerModel"
            placeholder="请选择"
             @change="modelChange('trigger',$event)" value-key="name">
              <el-option
              v-for="item in initSetData.triggerEvent"
              :key="item.name"
              :label="item.name"
              :value="item">
              </el-option>
            </el-select>
          </div>
        </div>
        <div class="clear ds-item">
            <div class="pull-left ds-text"><span>规则条件</span> </div>
            <!-- <el-radio v-model="isEveyCondition" label="1">任何条件</el-radio>
            <el-radio v-model="isEveyCondition" label="2">指定条件</el-radio> -->
              <el-radio v-model="isEveyCondition" label="1"  @change="modelChange('condition',$event)" :disabled="this.notiSaveData.push_type_id === '8'">任何条件</el-radio>
              <el-radio v-model="isEveyCondition" label="2"  @change="modelChange('condition',$event)">指定条件</el-radio>
        </div>
        <div class="ds-right" v-if="isEveyCondition === '2'">
          <conditionComponent :allCondition="initSetData.initData" :selCondition="notiSaveData.condition" :highWhere = "notiSaveData.high_where" ref="conditionComponent"></conditionComponent>
        </div>
        <div class="ds-item clear">
            <div class="pull-left ds-text"><span>提醒方式</span></div>
            <div class="pull-left ds-input">
                <el-select v-model="remindMethodModel" placeholder="请选择" @change="modelChange('remindMethod',$event)" value-key="name">
                  <el-option
                    v-for="item in initSetData.methodInfo"
                    :key="item.name"
                    :label="item.name"
                    :value="item">
                  </el-option>
                </el-select>
            </div>
        </div>
        <div class="clear ds-item">
            <div class="pull-left ds-text"><span>提醒时间</span> </div>
              <el-radio v-model="notiTime" label="1" @change="modelChange('remindTime',$event)" :disabled="this.notiSaveData.push_type_id === '8'">事件发生时</el-radio>
              <el-radio v-model="notiTime" label="2" @change="modelChange('remindTime',$event)">自定义提醒时间</el-radio>
        </div>
        <div class="ds-right  clear noti-time-box" v-show="notiTime === '2'">
            <div class="clear noti-time-right">
              <div class="pull-left noti-time-text"><span>提醒类型</span></div>
                <div class="pull-left noti-type-select ">
                  <el-select v-model="remindTypeModel" placeholder="请选择" value-key="label" @change = "modelChange('remindType',$event)">
                  <el-option
                    v-for="item in remindTypeList"
                    :key="item.value"
                    :label="item.label"
                    :value="item">
                  </el-option>
                </el-select>
              </div>
            </div>
            <div class="clear noti-time-right">
              <div class="pull-left noti-time-text"><span>提醒频率</span></div>
              <div class="pull-left noti-time-date">
              <div class="pull-left noti-time__left">
                <div class="pull-left">
                  <span>每</span>
                </div>
                <div class="pull-left noti-time-input">
                  <el-input v-model="notiSaveData.timer_setting.alert_frequency"  @change="modelChange('mouth-day',$event)"></el-input>
                </div>
                <div class="pull-left " v-show= "remindType === '1'">
                  <span>天</span>
                </div>
                <div class="pull-left "  v-show= "remindType ==='2'">
                  <span>周</span>
                </div>
                <div class="pull-left "  v-show= "remindType ==='3'">
                  <span>月</span>
                </div>
                <div class="pull-left "  v-show= "remindType ==='4'">
                  <span>年</span>
                </div>
              </div>
              <div class="pull-left noti-time__right" v-show= "remindType ==='2'">
                <div class="pull-left noti-week">
                  <span>每</span>
                </div>
                <div class="pull-left noti-week-select noti-type-select">
                  <el-select v-model="weekModel" placeholder="请选择" value-key="label" @change= "modelChange('week-day',$event)">
                    <el-option
                      v-for="item in weekList"
                      :key="item.value"
                      :label="item.label"
                      :value="item">
                    </el-option>
                  </el-select>
                </div>
              </div>
                <div class="pull-left noti-time__right" v-show= "remindType === '4'">
                  <div class="pull-left noti-month-select first noti-type-select">
                    <el-select v-model="monthModel" placeholder="请选择" value-key="label" @change="modelChange('month',$event)">
                      <el-option
                        v-for="item in initMonthList"
                        :key="item.value"
                        :label="item.label"
                        :value="item">
                      </el-option>
                    </el-select>
                  </div>
                  <div class="pull-left noti-day-select noti-type-select">
                    <el-select v-model="dayModel" placeholder="请选择"  value-key="label" @change="modelChange('day',$event)">
                      <el-option
                        v-for="item in dayList"
                        :key="item.value"
                        :label="item.label"
                        :value="item">
                      </el-option>
                    </el-select>
                  </div>
                </div>
                <div class="pull-left noti-time__right" v-show= "remindType === '3'" >
                  <div class="pull-left noti-type-select mouth-day-select">
                    <el-select v-model="dayModel" placeholder="请选择" value-key="label" @change="modelChange('day',$event)">
                      <el-option
                        v-for="item in dayList"
                        :key="item.value"
                        :label="item.label"
                        :value="item">
                      </el-option>
                    </el-select>
                  </div>
                </div>
              </div>
            </div>
            <div class="clear noti-time-right">
              <div class="pull-left noti-time-text"><span>提醒时间</span></div>
                <div class="pull-left noti-type-select ">
                  <el-select v-model="notiSaveData.timer_setting.alert_time" value-key="label" placeholder="请选择" @change="modelChange('remind-time',$event)">
                  <el-option
                    v-for="item in initRemindTime"
                    :key="item.value"
                    :label="item.label"
                    :value="item">
                  </el-option>
                </el-select>
              </div>
            </div>
            <div class="clear noti-time-right">
              <div class="pull-left noti-time-text"><span>结束时间</span></div>
                <div class="pull-left noti-type-select  noti-time-end">
                  <el-select v-model="endTypeModel" placeholder="请选择" value-key="label" @change="modelChange('endType',$event)">
                  <el-option
                    v-for="item in endTypeList"
                    :key="item.value"
                    :label="item.label"
                    :value="item">
                  </el-option>
                </el-select>
              </div>
              <div class="noti-end-times pull-left" v-show="endType ==='2'">
                <div class="pull-left noti-end-left"><span>发生</span></div>
                <div class="noti-end-input pull-left "><el-input v-model="notiSaveData.timer_setting.alert_end_content" @change="modelChange('number', $event)"></el-input></div>
                <div class="pull-left noti-end-right">次之后</div>
              </div>

              <div class="noti-end-input pull-left "  v-show="endType==='3'">
                <el-date-picker
                v-model="endDate"
                type="date"
                placeholder="选择日期"
                @change="modelChange('date-picker',$event)">
              </el-date-picker>
              </div>

            </div>
            <div class="clear noti-time-right">
              <div class="pull-left noti-time-text"><span>结果</span></div>
                <div class="pull-left noti-time-text noti-res" >
                <span>{{this.notiSaveData.timer_setting.alert_result}}</span>
              </div>
            </div>
        </div>
        <div class="clear ds-item">
            <div class="pull-left ds-text"><span>被提醒人</span> </div>
            <div class="pull-left  noti-person-box">
              <div class="pull-left clear ds-person-add">
                <div class="pull-left ds-person ds-addbtn" @click="selectMember()"><i class="el-icon-plus"></i>
                </div>
              </div>
              <div class="pull-left noti-person" v-for="(member,index) in notiSaveData.selected_people" :key="index">
                <div class=" pull-left ds-person" v-if="member.type === 1">
                  <div class="img-head pull-left"  v-if="member.picture !== '' && member.picture !== undefined">
                    <img :src="member.picture+'&TOKEN='+token">
                  </div>
                  <div class="head-name"  v-if=" member.picture === '' || member.picture === undefined">
                    <span>{{member.name | limitTo('-2')}}</span>
                  </div>
                </div>
                <div class="pull-left ds-name" v-if="member.type === 1"> <span>{{member.name}}</span></div>
                <div class="pull-left noti-dep" v-if="member.type === 0"><span>{{member.name}}</span></div>
                <div class="pull-left noti-dep" v-if="member.type === 4"><span>{{member.name}}</span></div>
                <div class="pull-left noti-dep" v-if="member.type === 2"><span>{{member.name}}</span></div>
                <div class="pull-left noti-dep" v-if="member.type === 3"><span>{{member.name}}</span></div>
                <div class="pull-left" @click="handleDel('member',index)"><i class="el-icon-close ds-del"></i></div>
              </div>
            </div>
        </div>
        <div class="clear ds-item ">
          <div class="pull-left ds-text"><span>推送内容</span></div>
          <div class="pull-left ds-input">
            <el-input v-model="notiSaveData.push_content.content" @change="pushContentChange($event)" maxlength="30"></el-input>
            </div>
        </div>
        <div class="ds-right clear">
            <div class="pull-left clear noti-select" v-for="(field,index) in showFieldList" :key="index">
              <div class="pull-left noti-select-box">
                <el-select v-model="field.field_label" placeholder="请选择" @change="modelChange('content',$event,index)" value-key="value">
                <el-option
                  v-for="item in initSetData.initData"
                  :key="item.value"
                  :label="item.label"
                  :value="item"
                  :disabled="isDisabled(item.type)">
                </el-option>
              </el-select>
              </div>
              <div class="ds-close pull-left" v-if="index !== 0" @click="handleDel('notiValue',index)"><i class="el-icon-close"></i></div>
            </div>
            <div class="ds-item ds-addbtn">
              <el-button type="text" @click="handleAddItem('content')"><i class="el-icon-plus"></i>添加</el-button>
            </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="closeDialog()">取 消</el-button>
        <el-button type="primary" @click="handleSaveNotiRule()">保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import empDepRoleMulti from '@/common/components/emp-dep-role-multi'
import {HTTPServer} from '@/common/js/ajax.js'
import conditionComponent from '@/common/components/condition'
import {formatDate} from '@/filter/filter.js'

export default {
  name: 'notificationRule',
  components: {
    empDepRoleMulti,
    conditionComponent
  },
  data () {
    return {
      tabledata: [
      ],
      noti_dialogVisible: false,
      value: '',
      input: '',
      options: {value: 0, label: '1'},
      currentPage: 1, // 当前页
      textarea: '',
      checked: '',
      radio: '',
      tem_tabledata: [],
      sizePage: 10,
      showColor: '',
      isSenior: false,
      isEveyCondition: '1',
      conditionList: [{
        'field_label': '',
        'field_value': '',
        'operator_label': '',
        'operator_value': '',
        'result_label': '',
        'result_value': ''
      }],
      memberList: [], // 会员列表
      notiTime: '1', // 提醒时间
      remindTime: false,
      notiSaveData:
      {
        'bean_name': this.$route.query.bean, // bean名称
        'push_type_id': '1', // 触发事件类型id
        'push_type_name': '新增数据', // 触发事件类型名称
        'condition_trigger': {
          'push_condition': '1', // 1.任何条件；2.指定条件；
          'push_conditon_name': '任何条件'// 1.任何条件；2.指定条件；
        },
        'conditionValue': '',
        'condition': // 推送条件指定为2时填充
        [
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
        'high_where': '',
        'setting_status': 0, // 0.正常；1.禁用；2.删除
        'alert_tunnel': '1', // 1.消息推送；2.短信提醒；3.邮件提醒；4.微信提醒；5.钉钉提醒
        'alert_method': {
          'push_method': '1', // 1.事件发生时；2.自定义提醒时间
          'push_method_name': '事件发生时'// 1.事件发生时；2.自定义提醒时间
        },
        'timer_setting': {// 提醒时间指定为2时填充
          'alert_type': '', // 1.每天；2.每周；3.每月；4.每年
          'alert_frequency': '1', // 提醒频率
          'week': {'value': '', 'label': ''}, // 提醒类型为每周时填充几周
          'day': {'value': '', 'label': ''}, // 提醒类型为每月和每年时填充几号
          'month': {'value': '', 'label': ''}, // 提醒类型为每年时填充月份
          'alert_time': '', // 时：分
          'alert_end_type': '', // 1.永不；2.次数；3.日期
          'alert_end_content': '', // 次数或者日期
          'alert_result': {'type': '天', 'day': '1天', 'time': '09:00', 'endType': '不结束'}
        },
        'alert_people_name': '',
        'selected_people': [],
        'push_content': {
          'content': '收到新的客户协作邀请', // 推送消息内容
          'show_field': [
            {
              'field_label': '',
              'field_value': ''
            }
          ]
        }
      },
      mebCompVisable: false,
      currentBean: {'bean': this.$route.query.bean},
      remindTypeList: [ {value: '1', label: '天'}, {value: '2', label: '周'}, {value: '3', label: '月'}, {value: '4', label: '年'} ],
      remindTypeModel: '每天', // 提醒类型
      remindType: '1', // 提醒类型0:天，1，周，2：年
      weekList: [
        {value: 1, label: '周日'},
        {value: 2, label: '周一'},
        {value: 3, label: '周二'},
        {value: 4, label: '周三'},
        {value: 5, label: '周四'},
        {value: 6, label: '周五'},
        {value: 7, label: '周六'}
      ],
      perModel: '1', // 每1周，一天，一年
      monthModel: '1月',
      monthList: [],
      dayList: [],
      dayModel: '1号',
      // yearModel: '1',
      weekModel: '周一',
      endDate: '',
      endType: 0, // 0:永不，1：次数，2:日期
      endTypeList: [
        {value: '1', label: '永不'},
        {value: '2', label: '次数'},
        {value: '3', label: '日期'}
      ],
      endTypeModel: '永不',
      remindMethodModel: '消息推送',
      initSetData: {
        'initData': [
        ],
        'triggerEvent': [
        ],
        'methodInfo': [
        ]
      },
      triggerModel: '新增数据',
      timeRes: {type: '天', day: '1天', time: '09:00', endType: '不结束'}, // 设置编号结果
      operatorList: [],
      showFieldList: [ {'field_label': '', 'field_value': ''} ],
      notiHisList: [],
      formulaErr: false,
      delNotiVisable: false,
      isEdit: false,
      // empDepRoleMultiDatas: {}
      allOk: true,
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      moduleName: this.$route.query.moduleName,
      getListLoading: false
    }
  },
  methods: {
    showDialog () {
      this.noti_dialogVisible = true
    },
    closeDialog () {
      this.isEdit = false
      this.noti_dialogVisible = false
      this.isEveyCondition = false
    },
    // 点击新增
    handleAddNew () {
      // 清空数据
      // this.notiSaveData = {}
      this.notiSaveData = {
        'bean_name': this.$route.query.bean,
        'push_type_id': '1', // 触发事件类型id
        'push_type_name': '新增数据', // 触发事件类型名称
        'condition_trigger': {
          'push_condition': '1', // 1.任何条件；2.指定条件；
          'push_conditon_name': '任何条件'// 1.任何条件；2.指定条件；
        },
        'conditionValue': '',
        'condition': // 推送条件指定为2时填充
        [
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
        'high_where': '',
        'setting_status': 0, // 0.正常；1.禁用；2.删除
        'alert_tunnel': '1', // 1.消息推送；2.短信提醒；3.邮件提醒；4.微信提醒；5.钉钉提醒
        'alert_method': {
          'push_method': '1', // 1.事件发生时；2.自定义提醒时间
          'push_method_name': '事件发生时'// 1.事件发生时；2.自定义提醒时间
        },
        'timer_setting': { // 提醒时间指定为2时填充
          'alert_type': '1', // 1.每天；2.每周；3.每月；4.每年
          'alert_frequency': '1', // 提醒频率
          'week': {'value': '2', 'label': '周一'}, // 提醒类型为每周时填充几周
          'day': {'value': '1', 'label': '1号'}, // 提醒类型为每月和每年时填充几号
          'month': {'value': '1', 'label': '1月'}, // 提醒类型为每年时填充月份
          'alert_time': '09:00', // 时：分
          'alert_end_type': '1', // 1.永不；2.次数；3.日期
          'alert_end_content': '', // 次数或者日期
          'alert_result': '每1天09:00，不结束' // 显示结果
        },
        'alert_people_name': '',
        'selected_people': [
        //   {
        //   'selected_type': '',    // 1.成员ID;2.部门ID;3.角色ID;4.选择动态参数
        //   'selected_content': {  // 1,2,3为选择的ID和选择的名字 4为选择的字段名和值
        //     'id': '',  // ("field_name":"负责人")
        //     'name': ''  // ("field_value":"principle")
        //   }
        // }
        ],
        'push_content': {
          'content': `收到新的${this.moduleName}`, // 推送消息内容
          'show_field': [
            {
              'field_label': this.initSetData.initData[0].label,
              'field_value': this.initSetData.initData[0].value
            }
          ]
        }
      }
      this.showFieldList = [{
        'field_label': this.initSetData.initData[0].label,
        'field_value': this.initSetData.initData[0].value
      }]
      /* *modify 2018-2-3  */

      /* end */
      this.triggerModel = '新增数据'
      this.isEveyCondition = '1'
      this.conditionList = [{
        'field_label': '',
        'field_value': '',
        'operator_label': '',
        'operator_value': '',
        'result_label': '',
        'result_value': ''
      }]
      this.notiTime = '1'
      // this.notiSaveData.high_where = ''
      // this.notiSaveData.setting_status = 0
      // this.alert_tunnel = '1'
      // this.alert_method.push_method = 1
      // this.alert_method.push_method_name = '事件发生时'
      // this.notiSaveData.timer_setting.alert_type
      this.remindTypeModel = '天'
      this.endTypeModel = '永不'
      this.memberList = []
      // this.showFieldList = [ {'field_label': '', 'field_value': ''} ]
      console.log(this.notiSaveData, '新增数据')
      this.showDialog()
    },
    // 点击编辑操作
    handleEdit (index, data) {
      console.log(index, data, '正在编辑的项目')
      this.isEdit = true
      this.noti_dialogVisible = true
      this.notiSaveData = this.notiHisList[index]
      console.log(this.notiSaveData, '编辑数据')
      this.triggerModel = this.notiSaveData.push_type_name
      this.isEveyCondition = this.notiSaveData.condition_trigger.push_condition
      if (this.notiSaveData.condition.length > 0) { // 判断条件
        this.conditionList = []
        this.notiSaveData.condition.map((item, index) => {
          this.conditionList.push(item)
        })
      }
      console.log(this.conditionList, '条件列表')
      this.notiTime = this.notiSaveData.alert_method.push_method // 提醒时间
      if (this.notiSaveData.alert_method.push_method === '2') {
        this.remindType = this.notiSaveData.timer_setting.alert_type
        switch (this.notiSaveData.timer_setting.alert_type) {
          case '1':
            this.remindTypeModel = '天'
            break
          case '2':
            this.remindTypeModel = '周'
            break
          case '3':
            this.remindTypeModel = '月'
            break
          case '4':
            this.remindTypeModel = '年'
            break
        }
        this.weekModel = this.notiSaveData.timer_setting.week.label
        this.monthModel = this.notiSaveData.timer_setting.month.label
        this.dayModel = this.notiSaveData.timer_setting.day.label
        switch (this.notiSaveData.timer_setting.alert_end_type) {
          case '1':
            this.endTypeModel = '永不'
            break
          case '2':
            this.endTypeModel = '次数'
            break
          case '3':
            this.endTypeModel = '日期'
            break
        }
      }
      this.memberList = []
      this.memberList = this.notiSaveData.selected_people
      console.log(this.memberList, '会员列表')
      if (this.notiSaveData.push_content.show_field.length > 0) {
        this.showFieldList = []
        this.notiSaveData.push_content.show_field.map((item, index) => {
          this.showFieldList.push(item)
        })
      }
      console.log(this.notiSaveData, '初始化编辑项目')
    },
    // 切换每页数量
    handleSizeChange (data) {
      this.tem_tabledata = this.tabledata.slice(0, data)
      this.sizePage = data
    },
    // 切换当前页
    handleCurrentChange (data) {
      console.log(data, '当前页')
      let start = (data - 1) * this.sizePage
      this.tem_tabledata = this.tabledata.slice(start, start + this.sizePage)
      console.log(this.tem_tabledata, '临时数据')
    },
    // 判断高级公式是否合法
    judgeFormula (data) {
      console.log(data, '高级公式')
      let len = data.length
      let leftNum = 0 // 保存左边括号的个数
      for (let i = 0; i < len; i++) {
        let temp = data.charAt(i)
        switch (temp) {
          case '(':
            leftNum++
            break
          case ')':
            leftNum--
            break
        }
      }
      if (leftNum === 0) {
        this.formulaErr = false
      } else {
        this.formulaErr = true
      }
    },
    // 删除操作
    handleDel (type, data) {
      console.log(type, data)
      switch (type) {
        case 'member':
          // this.memberList.splice(data, 1)
          this.notiSaveData.selected_people.splice(data, 1)
          this.getName()
          console.log(this.notiSaveData.selected_people, '删除成员后')
          break
        case 'condition':
          this.conditionList.splice(data, 1)
          this.notiSaveData.condition.splice(data, 1)
          console.log(this.conditionList)
          break
        case 'notiValue':
          this.showFieldList.splice(data, 1)
          this.notiSaveData.push_content.show_field.splice(data, 1)
          console.log(this.showFieldList, '通知内容')
          break
        default:
          break
      }
    },
    handleAddItem (type) {
      switch (type) {
        case 'condition':
          let obj = {fieldModel: '', operatorModel: '', input: ''}
          this.conditionList.push(obj)
          let obj2 = {// 条件设置
            'field_label': '',
            'field_value': '',
            'operator_label': '',
            'operator_value': '',
            'result_label': '',
            'result_value': ''
          }
          this.notiSaveData.condition.push(obj2)
          break
        case 'content':
          if (this.showFieldList.length === 3) {
            this.$message({
              showClose: true,
              message: '最多只能添加三条数据',
              type: 'warning'
            })
            return
          }
          let item = {'field_label': '', 'field_value': '', 'type': ''}
          this.showFieldList.push(item)
          this.notiSaveData.push_content.show_field.push(item)
          break
        default:
          break
      }
    },
    // 选择提醒成员
    selectMember () {
      console.log(this.notiSaveData.selected_people, '选择成员')
      this.$bus.emit('commonMember', {
        'prepareData': this.notiSaveData.selected_people,
        'prepareKey': 'notification',
        'seleteForm': true,
        'banData': [],
        'navKey': '0,1,2,3',
        'removeData': [],
        'bean': this.$route.query.bean
      })
    },
    // 初始化设置信息
    getInitParams () {
      HTTPServer.getInitialParameter(this.currentBean).then((res) => {
        console.log(res)
        this.initSetData.initData = res.initData
        this.initSetData.methodInfo = res.methodInfo
        this.initSetData.triggerEvent = res.triggerEvent
      })
    },
    // 消息提示
    alertMsg (value) {
      this.$message({
        showClose: true,
        type: 'warning',
        message: value
      })
    },
    // 处理规则条件显示
    hanleHighCondition () {
      // 处理高级公式
      if (this.notiSaveData.high_where === '') {
        this.notiSaveData.conditionValue = ''
        let len = this.notiSaveData.condition.length
        this.notiSaveData.condition.map((item, index) => {
          switch (item.show_type) {
            case 'text':
              let text = item.field_label + item.operator_label + item.result_value
              if (index === len - 1) {
                this.notiSaveData.conditionValue += `${text}`
              } else {
                this.notiSaveData.conditionValue += `${text}，`
              }
              break
            case 'personnel':
              let persons = item.field_label + item.operator_label
              let personLen = item.result_value.length
              console.log(item.result_value instanceof Array, '是否为数组')
              if (item.result_value instanceof Array) {
                item.result_value.map((person, idx) => {
                  if (idx === personLen - 1) {
                    persons += `${person.name}`
                  } else {
                    persons += `${person.name}，`
                  }
                })
              } else {
                persons = ''
              }
              if (index === len - 1) {
                this.notiSaveData.conditionValue += `${persons}`
              } else {
                this.notiSaveData.conditionValue += `${persons}，`
              }
              break
            case 'picklist':
              let options = item.field_label + item.operator_label
              let pickLen = item.result_value.length
              item.result_value.map((entrys, idx) => {
                if (idx === pickLen - 1) {
                  options += `${entrys.label}`
                } else {
                  options += `${entrys.label}，`
                }
              })
              if (index === len - 1) {
                this.notiSaveData.conditionValue += `${options}`
              } else {
                this.notiSaveData.conditionValue += `${options}，`
              }
              break
            case 'datetime':
              if (item.operator_value === 'BETWEEN') { // 时间位于的情况
                // let between = item.field_label + item.operator_label
                let dateArr = item.result_value.split(',')
                let date = `${formatDate(dateArr[0], 'yyyy-MM-dd')}至` + formatDate(dateArr[1], 'yyyy-MM-dd')
                let dateValue
                if (index === len - 1) {
                  dateValue = item.field_label + item.operator_label + date
                } else {
                  dateValue = item.field_label + item.operator_label + date + '，'
                }
                this.notiSaveData.conditionValue += dateValue
              } else {
                let day
                if (index === len - 1) {
                  day = item.field_label + `${item.result_value}天` + `${item.operator_label}`
                } else {
                  day = item.field_label + `${item.result_value}天` + `${item.operator_label}，`
                }

                this.notiSaveData.conditionValue += day
              }
              break
            default:
              let text1 = item.field_label + item.operator_label + item.result_value
              if (index === len - 1) {
                this.notiSaveData.conditionValue += `${text1}`
              } else {
                this.notiSaveData.conditionValue += `${text1}，`
              }
              break
          }
        })
        console.log(this.notiSaveData.conditionValue, '处理后的高级条件')
      } else {
        this.notiSaveData.conditionValue = this.notiSaveData.high_where
      }
    },
    // 点击保存推送规则
    handleSaveNotiRule () {
      console.log(this.notiSaveData, 'notidata')
      if (this.notiSaveData.condition_trigger.push_condition === '2') {
        this.notiSaveData.condition = this.$refs.conditionComponent.handleLastData()
        if (!this.$refs.conditionComponent.judgeCondition()) {
          this.allOk = false
          return
        }
        // this.notiSaveData.condition = this.$refs.conditionComponent.handleLastData()
        this.notiSaveData.high_where = this.$refs.conditionComponent.high_where
      }
      if (this.notiSaveData.selected_people.length === 0) {
        this.alertMsg('提醒人不能为空！')
        this.allOk = false
        return false
      } else {
        this.allOk = true
      }
      this.notiSaveData.push_content.show_field.map((item, index) => {
        if (item.field_value === '') {
          this.alertMsg('提醒字段不能为空！')
          this.allOk = false
          return false
        } else {
          this.allOk = true
        }
      })
      this.hanleHighCondition()
      console.log(JSON.stringify(this.notiSaveData), '最终消息推送数据')
      if (this.allOk) {
        this.saveNotiRule()
      }
    },
    // 保存消息推送规则
    saveNotiRule () {
      if (this.isEdit) {
        this.editNotiRule()
        this.closeDialog()
      } else {
        HTTPServer.savePushSettings(this.notiSaveData).then((res) => {
          this.$message({
            showClose: true,
            message: '新增规则成功',
            type: 'success'
          })
          this.getNotiRuleList()
          this.closeDialog()
        })
      }
    },
    // 删除消息推送规则
    deleteNotiRule (index, data) {
      console.log(index, data, '删除选项')
      this.$confirm('此操作将永久删除该数据， 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let id = {'id': data.id}
        console.log(id)
        HTTPServer.deletePushSettings(id).then((res) => {
          this.$message({
            showClose: true,
            message: '删除规则成功',
            type: 'success'
          })
          this.getNotiRuleList()
        })
        this.delNotiVisable = false
      }).catch(_ => {})
    },
    // 编辑消息推送规则
    editNotiRule () {
      HTTPServer.editPushSettings(this.notiSaveData).then((res) => {
        this.$message({
          showClose: true,
          message: '编辑成功！',
          type: 'success'
        })
        this.getNotiRuleList()
      })
    },
    // 禁用/启用消息推送规则
    disableNotiRule (type, index, data) {
      let text = type === 'forbidon' ? '禁用' : '启用'
      this.$confirm(`此操作将${text}该规则, 是否继续?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let id = {'id': data.id}
        HTTPServer.forbidPushSettings(id).then((res) => {
          this.$message({
            showClose: true,
            message: `${text}规则成功`,
            type: 'success'
          })
          this.getNotiRuleList()
        })
      }).catch(() => {

      })
      console.log(index, data, '禁用信息')
    },
    // 获取消息推送列表
    getNotiRuleList () {
      this.getListLoading = true
      this.tabledata = []
      HTTPServer.getPushList(this.currentBean, 'Loading')
        .then((res) => {
          console.log(res + '444444')
          this.getListLoading = false
          this.notiHisList = res
          this.notiHisList.map((item, index) => {
            let obj = {}
            // let remindeder = ''
            let rule = ''
            this.$set(obj, 'active', item.push_type_name)
            if (item.alert_method.push_method === '1') {
              this.$set(obj, 'remindTime', item.alert_method.push_method_name)
            } else {
            // todo
              this.$set(obj, 'remindTime', item.alert_method.push_method_name)
            }
            rule = item.conditionValue
            if (rule === '') {
              rule = '任何条件'
            }
            this.$set(obj, 'rule', rule)
            this.$set(obj, 'remindeder', item.alert_people_name)
            let temStr = item.push_content.content.length > 30 ? `${item.push_content.content.substring(0, 30)}...` : item.push_content.content
            this.$set(obj, 'remindAbout', temStr)
            this.$set(obj, 'id', item._id.$oid)
            this.$set(obj, 'status', item.setting_status)
            this.$set(obj, 'modifyBy', item.modifyBy)
            this.$set(obj, 'modifyTime', item.modifyTime)
            this.tabledata.push(obj)
            console.log(this.tabledata, '展示数据')
          })
          this.handleSizeChange(this.sizePage)
        })
    },
    // 选择月份初始化天数
    initDayList (data) {
      this.dayList = []
      let len = 30
      console.log(typeof data)
      if (typeof data === 'object') {
        if (data.value === 2) { // 二月
          let year = new Date().getFullYear()
          console.log(year, '今年年份')
          if ((year % 4 === 0 && year % 100 !== 0) || year % 400 === 0) { // 判断是否闰年
            len = 28
          } else {
            len = 27
          }
        } else if (data.value === 3 || data.value === 5 || data.value === 8 || data.value === 10) {
          len = 29
        }
      } else if (typeof data === 'string') {
        if (data === 2) { // 二月
          let year = new Date().getFullYear()
          console.log(year, '今年年份')
          if ((year % 4 === 0 && year % 100 !== 0) || year % 400 === 0) { // 判断是否闰年
            len = 28
          } else {
            len = 27
          }
        } else if (data.value === 4 || data.value === 6 || data.value === 9 || data.value === 11) {
          len = 29
        }
      }
      for (let i = 0; i <= len; i++) {
        let obj = {value: i, label: (i + 1) + '号'}
        this.dayList.push(obj)
      }
    },
    // model发生改变
    modelChange (type, data, index) {
      console.log(type, data, index)
      if (this.isEdit) { // 编辑状态
        switch (type) {
          case 'remindType':
            this.remindType = data.value
            console.log(data, '结果')
            this.notiSaveData.timer_setting.alert_type = data.value
            console.log(this.notiSaveData, '配置数据')
            this.getAlertResult()
            if (data.value === '3') { // 月的时候
              this.initDayList('1')
            } else if (data.value === '4') {
              this.initDayList({value: 1, label: '1号'})
            }
            break
          case 'month':
            this.initDayList(data)
            this.notiSaveData.timer_setting.month = data
            this.getAlertResult()
            break
          case 'endType':
            this.endTypeModel = data.label
            this.endType = data.value
            this.notiSaveData.timer_setting.alert_end_type = data.value
            this.notiSaveData.timer_setting.alert_end_content = ''
            this.endDate = ''
            this.getAlertResult()
            break
          case 'trigger':
            this.triggerModel = data.name
            this.notiSaveData.push_type_id = data.id.toString()
            this.notiSaveData.push_type_name = data.name
            console.log(this.notiSaveData, '配置数据')
            /* modify 2018-2-3 */
            switch (this.notiSaveData.push_type_id) {
              case '1':
                this.notiSaveData.push_content.content = `收到新的${this.moduleName}`
                break
              case '2':
                this.notiSaveData.push_content.content = `收到共享的${this.moduleName}`
                break
              case '3':
                this.notiSaveData.push_content.content = `${this.moduleName}被转换为新模块数据`
                break
              case '4':
                this.notiSaveData.push_content.content = `收到被转移的${this.moduleName}`
                break
              case '5':
                this.notiSaveData.push_content.content = `${this.moduleName}被删除`
                break
              case '6':
                this.notiSaveData.push_content.content = `${this.moduleName}数据被修改`
                break
              case '7':
                this.notiSaveData.push_content.content = `${this.moduleName}被评论`
                break
              case '8':
                this.notiSaveData.push_content.content = `请及时跟进${this.moduleName}`
                break
              default:
                break
            }
            /* *end */
            break
          case 'remindMethod':
            this.remindMethodModel = data.name
            this.notiSaveData.alert_tunnel = data.id
            break
          case 'condition':
            this.notiSaveData.condition_trigger.push_condition = data
            if (data === '1') {
              this.notiSaveData.condition_trigger.push_conditon_name = '任意条件'
            } else {
              this.notiSaveData.condition_trigger.push_conditon_name = '指定条件'
            }
            console.log(this.notiSaveData, '配置数据')
            break
          case 'remindTime':
            this.notiSaveData.alert_method.push_method = data
            if (data === '1') {
              this.notiSaveData.alert_method.push_method_name = '事件发生时'
            } else {
              this.notiSaveData.alert_method.push_method_name = '自定义提醒时间'
            }
            console.log(this.notiSaveData, '配置数据')
            break
          case 'week-day':
            this.notiSaveData.timer_setting.week = data
            console.log(this.notiSaveData, '配置数据')
            this.getAlertResult()
            break
          case 'day':
            this.notiSaveData.timer_setting.day = data
            this.getAlertResult()
            console.log(this.notiSaveData, '配置数据')
            break
          case 'mouth-day':
            this.initDayList(data)
            this.getAlertResult()
            break
          case 'remind-time':
            this.notiSaveData.timer_setting.alert_time = data.label
            this.getAlertResult()
            break
            // case 'remind-time':
            //   this.notiSaveData.timer_setting.alert_time = data.label
            //   break
          case 'date-picker':
            console.log(typeof data)
            let date = data.getTime().toString()
            this.notiSaveData.timer_setting.alert_end_content = date
            console.log(this.notiSaveData, '配置数据')
            this.getAlertResult()
            break
          case 'condition-field':
            this.operatorList = data.operator
            this.notiSaveData.condition[index].field_label = data.label
            this.notiSaveData.condition[index].field_value = data.value
            console.log(this.conditionList, this.operatorList, 'operatorList')
            console.log(this.notiSaveData, '配置数据')
            break
          case 'opertion':
            this.conditionList[index].operator_label = data.label
            this.notiSaveData.condition[index].operator_label = data.label
            this.notiSaveData.condition[index].operator_value = data.type
            console.log(this.conditionList, this.operatorList, 'operatorList')
            console.log(this.notiSaveData, '配置数据')
            break
          case 'condition-input':
            this.notiSaveData.condition[index].result_value = data
            console.log(this.notiSaveData, '配置数据')
            break
          case 'content':
            this.showFieldModel = data.lebel
            this.notiSaveData.push_content.show_field[index].field_label = data.label
            this.notiSaveData.push_content.show_field[index].field_value = data.value
            this.notiSaveData.push_content.show_field[index].type = data.type
            console.log(this.notiSaveData, '配置数据')
            break
          case 'number':
            this.getAlertResult()
            break
          default:
            break
        }
      } else { // 新增状态
        switch (type) {
          case 'remindType':
            this.remindType = data.value
            console.log(data, '结果')
            this.notiSaveData.timer_setting.alert_type = data.value
            console.log(this.notiSaveData, '配置数据')
            this.getAlertResult()
            if (data.value === '3') { // 月的时候
              this.initDayList('1')
            } else if (data.value === '4') {
              this.initDayList({value: 1, label: '1号'})
            }
            break
          case 'month':
            this.initDayList(data)
            this.notiSaveData.timer_setting.month = data
            this.getAlertResult()
            break
          case 'endType':
            this.endTypeModel = data.label
            this.endType = data.value
            this.notiSaveData.timer_setting.alert_end_type = data.value
            this.notiSaveData.timer_setting.alert_end_content = ''
            this.endDate = ''
            this.getAlertResult()
            break
          case 'trigger':
            this.triggerModel = data.name
            this.notiSaveData.push_type_id = data.id.toString()
            this.notiSaveData.push_type_name = data.name
            console.log(this.notiSaveData, '配置数据')
            /* modify 2018-2-3 */
            switch (this.notiSaveData.push_type_id) {
              case '1':
                this.notiSaveData.push_content.content = `收到新的${this.moduleName}`
                break
              case '2':
                this.notiSaveData.push_content.content = `收到共享的${this.moduleName}`
                break
              case '3':
                this.notiSaveData.push_content.content = `${this.moduleName}被转换为新模块数据`
                break
              case '4':
                this.notiSaveData.push_content.content = `收到被转移的${this.moduleName}`
                break
              case '5':
                this.notiSaveData.push_content.content = `${this.moduleName}被删除`
                break
              case '6':
                this.notiSaveData.push_content.content = `${this.moduleName}数据被修改`
                break
              case '7':
                this.notiSaveData.push_content.content = `${this.moduleName}被评论`
                break
              case '8':
                this.notiSaveData.push_content.content = `请及时跟进${this.moduleName}`
                break
              default:
                break
            }
            /* *end */
            /* modify by 2018/6/4 */
            if (this.notiSaveData.push_type_id === '8') {
              this.notiSaveData.alert_method.push_method = '2'
              this.notiTime = '2'
              this.notiSaveData.alert_method.push_method_name = '自定义提醒时间'
              this.notiSaveData.condition_trigger.push_condition = '2'
              this.isEveyCondition = '2'
              this.notiSaveData.condition_trigger.push_conditon_name = '指定条件'
            }
            /* end */
            break

          case 'remindMethod':
            this.remindMethodModel = data.name
            this.notiSaveData.alert_tunnel = data.id
            break
          case 'condition':
            this.notiSaveData.condition_trigger.push_condition = data
            if (data === '1') {
              this.notiSaveData.condition_trigger.push_conditon_name = '任意条件'
            } else {
              this.notiSaveData.condition_trigger.push_conditon_name = '指定条件'
            }
            console.log(this.notiSaveData, '配置数据')
            break
          case 'remindTime':
            this.notiSaveData.alert_method.push_method = data
            if (data === '1') {
              this.notiSaveData.alert_method.push_method_name = '事件发生时'
            } else {
              this.notiSaveData.alert_method.push_method_name = '自定义提醒时间'
            }
            console.log(this.notiSaveData, '配置数据')
            break
          case 'week-day':
            this.notiSaveData.timer_setting.week = data
            console.log(this.notiSaveData, '配置数据')
            this.getAlertResult()
            break
          case 'day':
            this.notiSaveData.timer_setting.day = data
            this.getAlertResult()
            console.log(this.notiSaveData, '配置数据')
            break
          case 'mouth-day':
            this.initDayList(data)
            this.getAlertResult()
            break
          case 'remind-time':
            this.notiSaveData.timer_setting.alert_time = data.label
            this.getAlertResult()
            break
            // case 'remind-time':
            //   this.notiSaveData.timer_setting.alert_time = data.label
            //   break
          case 'date-picker':
            console.log(typeof data)
            let date = data.getTime().toString()
            this.notiSaveData.timer_setting.alert_end_content = date
            console.log(this.notiSaveData, '配置数据')
            this.getAlertResult()
            break
          case 'condition-field':
            this.operatorList = data.operator
            this.notiSaveData.condition[index].field_label = data.label
            this.notiSaveData.condition[index].field_value = data.value
            console.log(this.conditionList, this.operatorList, 'operatorList')
            console.log(this.notiSaveData, '配置数据')
            break
          case 'opertion':
            this.conditionList[index].operator_label = data.label
            this.notiSaveData.condition[index].operator_label = data.label
            this.notiSaveData.condition[index].operator_value = data.type
            console.log(this.conditionList, this.operatorList, 'operatorList')
            console.log(this.notiSaveData, '配置数据')
            break
          case 'condition-input':
            this.notiSaveData.condition[index].result_value = data
            console.log(this.notiSaveData, '配置数据')
            break
          case 'content':
            // this.showFieldModel = data.lebel
            this.notiSaveData.push_content.show_field[index].field_label = data.label
            this.notiSaveData.push_content.show_field[index].field_value = data.value
            this.notiSaveData.push_content.show_field[index].type = data.type
            console.log(this.notiSaveData, '配置数据')
            break
          case 'number':
            this.getAlertResult()
            break
          default:
            break
        }
      }
    },
    // 获取时间提醒设置结果
    getAlertResult () {
      let type, day, time, end, frequency
      if (this.notiSaveData.timer_setting.alert_type === '1') { // 天
        type = '天'
        day = ''
      } else if (this.notiSaveData.timer_setting.alert_type === '2') { // 周
        type = '周'
        day = this.notiSaveData.timer_setting.week.label
      } else if (this.notiSaveData.timer_setting.alert_type === '3') { // 月
        type = '月'
        day = this.notiSaveData.timer_setting.day.label
      } else { // 年
        type = '年'
        day = `${this.notiSaveData.timer_setting.month.label}${this.notiSaveData.timer_setting.day.label}`
      }
      frequency = this.notiSaveData.timer_setting.alert_frequency
      time = this.notiSaveData.timer_setting.alert_time
      if (this.notiSaveData.timer_setting.alert_end_type === '1') { // 永不
        end = '不结束'
      } else if (this.notiSaveData.timer_setting.alert_end_type === '2') { // 次数
        end = `${this.notiSaveData.timer_setting.alert_end_content}次结束`
      } else { // 日期
        if (this.notiSaveData.timer_setting.alert_end_content !== '') { // 日期
          end = `${formatDate(this.notiSaveData.timer_setting.alert_end_content, 'yyyy-MM-dd')}结束`
        } else {
          end = '结束'
        }
      }
      this.notiSaveData.timer_setting.alert_result = `每${frequency}${type}${day}${time}，${end}`
    },
    // 取出选择成员姓名
    getName () {
      let len = this.notiSaveData.selected_people.length
      this.notiSaveData.alert_people_name = ''
      this.notiSaveData.selected_people.map((item, index) => {
        if (index === len - 1) {
          this.notiSaveData.alert_people_name += item.name
        } else {
          this.notiSaveData.alert_people_name += item.name + ','
        }
      })
    },
    // 过滤不能选择的字段
    isDisabled (type) {
      if (type === 'picture' || type === 'subform' || type === 'attachment' || type === 'role' || type === 'department') {
        return true
      }
    },
    // 推送内容发生变化
    pushContentChange (data) {
      if (data === '') {
        /* modify 2018-2-3 */
        switch (this.notiSaveData.push_type_id) {
          case '1':
            this.notiSaveData.push_content.content = `收到新的${this.moduleName}`
            break
          case '2':
            this.notiSaveData.push_content.content = `收到共享的${this.moduleName}`
            break
          case '3':
            this.notiSaveData.push_content.content = `${this.moduleName}被转换为新模块数据`
            break
          case '4':
            this.notiSaveData.push_content.content = `收到被转移的${this.moduleName}`
            break
          case '5':
            this.notiSaveData.push_content.content = `${this.moduleName}被删除`
            break
          case '6':
            this.notiSaveData.push_content.content = `${this.moduleName}数据被修改`
            break
          case '7':
            this.notiSaveData.push_content.content = `${this.moduleName}被评论`
            break
          case '8':
            this.notiSaveData.push_content.content = `请及时跟进${this.moduleName}`
            break
          default:
            break
        }
        /* *end */
      }
    }
  },
  computed: {
    totalData () {
      return this.tabledata.length
    },
    initMonthList () { // 初始化月份
      let monthList = []
      for (let i = 1; i <= 12; i++) {
        let obj = {value: i, label: i + '月'}
        monthList.push(obj)
      }
      return monthList
    },
    initRemindTime () { // 初始化提醒时间
      let remindTime = []
      for (let i = 0; i <= 23; i++) {
        let obj = {value: i, label: i >= 10 ? i + ':00' : '0' + i + ':00'}
        remindTime.push(obj)
      }
      return remindTime
    }
  },
  mounted () { /** 多选集合窗口 */
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      console.log(value, '成员')
      if (value.prepareKey === 'notification') {
        // this.notiSaveData.selected_people = value.prepareData
        this.notiSaveData.selected_people = []
        value.prepareData.map((people, index) => {
          if (people.type === 3) { // 动态参数
            // TODO
            /** ***** 再见2017, modify 2017.12.30 ******/
            /** modify 2018-1-24 by pen */
            // cosnole.log(identifer, 'identifer')
            people.field_name = people.name
            people.field_value = people.identifer
            // delete people.id
            // delete people.name
            this.notiSaveData.selected_people.push(people)
          } else {
            this.notiSaveData.selected_people.push(people)
          }
        })
        this.getName()
      }
      console.log(this.notiSaveData, '选择提醒人后')
    })
    this.getNotiRuleList()
    this.getInitParams()
  },
  beforeDestory () {
    // this.$bus.off('selectEmpDepRoleMulti')
  }
}
</script>
<style lang="scss">
.noti-container {
    text-align: left;
    height: 100%;
  .ft-title {
    line-height: 60px;
    span:first-child{
      font-size: 18px;
      color: #17171A;
    }
    span {
      color: #BBBBC3;
    }
  }
  .noti-addbtn {
    line-height: 60px;
    .el-button {
      padding: 5px 13px;
      height: 30px;
    }
  }
  .ft-page {
    margin-top:15px;
  }
  .ft-table {
    height: calc(100% - 170px);
    overflow: auto;
    .el-table {
      height: 100%;
      th {
        .cell:first-child {
          border-left: 2px solid #E7E7E7;
          margin-bottom: 15px;
          font-size: 15px;
          line-height: 15px;
          font-weight: 400;
          color: #17171A;
        }
      }
      .el-table__body-wrapper {
        // min-height: 500px;
        // max-height: 500px;
        height: calc(100% - 55px);
        overflow:auto;
        .cell {
          text-align: left;
        }
      }
    }
  }
  .el-dialog__header {
    background: #409EFF;

    span.el-dialog__title,i.el-dialog__close {
        color:#ffffff !important
    }
    .el-dialog__headerbtn {
      .el-dialog__close {
        font-size: 23px;
        color:#ffffff
      }
    }
  }
  .el-dialog__footer {
      // position: absolute;
      // right: 10px;
      // bottom: 10px;
  .el-button {
      padding: 5px 13px;
      height: 30px;
      width: 80px;
    }
  }
  .ds-dialog-content {
    width: 90%;
    margin-left:auto;
    margin-right:auto;
    .ds-item {
      line-height: 54px;
      .ds-input {
        .el-input__inner {
          height: 35px;
        }
      }
      div.ds-text{
        padding-right:10px;
        box-sizing: border-box;
        width: 70px;
        text-align: left
      }
      div.ds-input{
        width: 80%;
        .el-select {
          width: 100%;
        }
      }
      .ds-person-add {
        .ds-person {
          background: #ECEFF1;
          width: 36px;
          height: 36px;
          border-radius: 18px;
          box-sizing: border-box;
          margin-top:9px;
          position: relative;
          padding-right:3px;
          margin-right: 15px;
        }
          i{
            position: absolute;
            left: 9px;
            top: 9px;
            font-size: 18px;
            color: #A0A0AE;
          }
      }
      .noti-person {
        margin-right:15px;
        position: relative;
        .ds-person {
          background: #51D0B1;
          background-repeat: no-repeat;
          box-sizing: border-box;
          margin-top:9px;
          position: relative;
          padding-right:3px;
          width: 36px;
          height: 36px;
          border-radius: 18px;
          // background-size: 100% 100%;
          .img-head {
              width: 36px;
              height: 36px;
              overflow: hidden;
              //margin-top:9px;
              border-radius: 50%;
            img {
              width: 100%;
              height: 100%;
              // border-radius: 50%;
              vertical-align: top;
            }
          }
          .head-name {
            height: 36px;
            position: absolute;
            span {
              //position: absolute;
            }
          }
          div {
            //position: absolute;
            top: -10px;
            text-align: center;
            overflow: hidden;
            color: #fff;
            font-size: 10px;
            width: 100%;
          }
        }
      .ds-del {
          position: absolute;
          right: -10px;
          top: 13px;
          color: #fff;
          height: 12px;
          width: 12px;
          background: red;
          border-radius: 6px;
          font-size: 7px;
          cursor: pointer;
        }
        .noti-dep{
          height: 54px;
          border-radius: 2px;
          line-height: 25px;
          padding-top: 15px;
          text-align: center;
          box-sizing: border-box;
          span {
            background: #E7E7E7;
            padding: 3px 5px;
          }
        }
        .ds-name {
          padding-left: 5px;
        }
      }
      .ds-person {

      }
      div.input-right {
        width: 100%;
      }
      .ds-selcolor {
        margin-top:10px;
        .el-color-picker__trigger {
          height: 35px;
          width: 120px;
        }
      }
      .noti-person-box {
        border: 1px solid #D9D9D9;
        box-sizing: border-box;
        width: 80%;
        padding-left:10px;
        padding-bottom: 10px
      }
      .ds-addbtn {
        // margin-right:15px;
        cursor: pointer;
      }
    }
    .ds-right {
      margin-left: 70px;
      width: 80%;
      margin-bottom: 10px;
      .ds-addbtn {
        font-size: 16px;
        i {
          font-size: 16px;
          padding-right:3px;
          box-sizing: border-box
        }
      }
      .ds-hight {
        .el-button {
            padding: 5px 13px;
            height: 30px;
            width: 115px;
        }
      }
      .ds-mark {
        p{
          color: #69696C;
        }
      }
      .noti-select {
        margin-top:15px;
        width: 100%;
        .noti-select-box{
          width: 90%;
          .el-select {
            width: 100%;
            .el-input__inner {
              height: 30px;
            }
          }
        }
        .ds-close {
          i {
            font-size: 21px;
            padding-left: 5px;
            color: red;
            line-height: 30px;
            cursor: pointer;
          }
        }
      }
    }
    .noti-select-item {
      margin-top:10px;
      width: 100%;
      .ds-select {
        width: 28%;
        margin-right:10px;
      .el-input__inner{
        height: 30px;
        }
      }
      .ds-close {
        i {
          color: red;
          font-size: 21px;
          line-height: 30px
        }
      }
    }
    .noti-time-box {
      //width: 80%;
      .noti-time-right {
        width: 100%;
        margin-top:10px;
        .noti-time-input {
          width: 50%;
          margin-right:4%;
          margin-left:4%;
          .el-input__inner {
            height: 30px;
          }
        }
        .noti-week-select {
          width: 90%;
          //margin-left:2%
        }
        .noti-month-select {
          width: 48%;
          padding-right:4%
        }
        .noti-day-select{
          width: 48%;

        }
        .mouth-day-select {
          width: 100%;
        }
        .noti-week {
          width: 10%
        }
        .noti-time__left {
          span {
            line-height: 30px
          }
          width: 35%;
        }
        .noti-time__right {
          span {
            line-height: 30px
          }
          width: 65%;
        }
        .noti-time-date {
          width: 80%;
        }
        .noti-res {
          width: 80%;
        }
      }
      .noti-type-select {
        width: 80%;
        .el-select {
          width: 100%;
        .el-input__inner{
          height: 30px;
          }
        }
      }
      .noti-time-text {
        //margin-right: 10px;
        font-size: 12px;
        color: #4A4A4A;
        line-height: 30px;
        width: 15%;
      }
      .noti-time-end {
        width: 38%;
      }
      .noti-end-input {
        width: 45%;
        margin-left:2%;

        .el-input__inner{
          height: 30px;
        }
        .el-date-editor.el-input {
          width: 89%;
        }
        .el-input__icon {
          line-height: 30px;
        }
      }
      .noti-end-times {
        margin-left:3%;
        width: 40%;
        .noti-end-left {
          line-height: 30px;
          font-size: 12px;
          width: 20%;
        }
        .noti-end-right {
          text-align: right;
          width: 30%;
          line-height: 30px;
          font-size: 12px;
        }
      }
    }
  }
  ::-webkit-scrollbar{
    width: 5px;
    height: 5px;
    background: #ddd
  //display: none
 }
  ::-webkit-scrollbar-thumb  {
    //background: #ccc
  }
}
</style>
