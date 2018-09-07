<template>
    <div  class="process-container" id="process-container">
      <div class="process-border"></div>
      <div class="process-header  clear">
        <div class="pull-left process-name-box">
          <span class="process-name"><i class="iconfont icon-kehuliucheng"></i>{{allData.processName}}</span>
        </div>
        <div class="pull-right">
          <div class="pull-left save-btn"><el-button type="primary" @click="handleSave(false)">保存</el-button></div>
          <div class="pull-left save-use"><el-button plain  @click="handleSave(true)">保存并使用</el-button></div>
        </div>
      </div>
        <el-row type="flex" class="row-bg">
          <!-- <el-col :span="4">
          </el-col>         -->
          <!-- <el-col :span="20"> -->
            <div class="process-middle">
              <div class="process-command-box clear">
                <div class="pull-left process-command">
                  <el-button type="text" icon="iconfont icon-chexiao1" @click="handleReset()">重置</el-button>
                  <el-button type="text" icon="iconfont icon-chexiao" @click="handleUndo()">撤销</el-button>
                  <el-button type="text" icon="iconfont icon-pc-delete" @click="handleDeleteNode()">删除</el-button>
                </div>
                <div class="pull-left palette-box">
                  <div class="pull-left palette-icon"><i class="iconfont icon-Rectangle1"></i></div>
                  <div id= "PaletteDiv">

                  </div>
                </div>
                <div class="pull-right ">
                  <div class="pull-left scalc-btn">
                    <el-button icon="iconfont icon-fangda" @click="handleincreaseZoom()" type="text"></el-button>
                    </div>
                  <div class="pull-left"><span>{{scaleValue}}%</span></div>
                  <div class="pull-left scalc-btn">   <el-button icon="iconfont icon-suoxiao" @click="handledecreaseZoom()" type="text"></el-button></div>
                  <div class="pull-left scalc-btn"><el-button icon="iconfont icon-dingwei1" @click="handleShowOverView()" type="text"></el-button></div>
                </div>
              </div>
              <div class="canvas-box bg-purple">
                <div id="process-box"
                  style="width:100%;"
                   class="process-box">
                </div>
                <div class="over-view" id="overView" :class="{'show-over-view': !isShowOverView}">
              </div>
              </div>
            </div>
          <!-- </el-col> -->
            <div class="process-prop-box process-property">
              <!-- <el-container class="process-property"> -->
                <!-- 属性选项  -->
                <!-- <el-col :span="24"> -->
                  <div class="grid-content bg-purple property-container">
                    <div class="seetting-name">
                      <el-tabs v-model="settingName">
                        <el-tab-pane label="节点属性" name="first" class="tab-item">
                          <div v-show="showNode" class="show-node" :style="{height:client_height}"><span>请选择节点</span></div>
                          <div class="seetting-detail" v-show="!showNode" :style="{height:client_height}">
                            <el-collapse v-model="activeNames">
                                <!-- 节点名称 -->
                                <el-collapse-item name="0">
                                  <template slot="title">
                                    <div class="margin-title">
                                      <span class="require">*</span>
                                      <span>节点名称</span>
                                    </div>
                                  </template>
                                  <div class="input-box">
                                    <el-input v-model="taskData.text" placeholder="只能输入10个字符" class="input" @keyup.native="nodeModelChange('name',$event)"  :maxlength = "10" ></el-input>
                                  </div>
                                </el-collapse-item>
                                <!-- end -->
                                <!-- 分支条件 -->
                                  <el-collapse-item name="1" v-if="showItem">
                                  <template slot="title">
                                    <div class="margin-title">
                                      <span class="require">*</span>
                                      <span>分支条件</span>
                                    </div>
                                  </template>
                                  <div class="pa_permisson-item">
                                    <div>
                                      <el-select v-model="taskData.branchWhere" @change="conditionChange('branch',$event)">
                                        <el-option v-for="condition in conditionType" :key="condition.value"
                                        :label="condition.label"
                                        :value="condition">
                                        </el-option>
                                      </el-select>
                                    </div>
                                    <div class="set-condition" v-show="taskData.branchWhere.value === '1'">
                                      <el-button plain @click="setCondition()">设置分支条件</el-button>
                                    </div>
                                    <!-- <div class="condition-text" v-show="taskData.branchWhere.value === '1'">
                                      <span>
                                        {{conditionValue}}
                                      </span>
                                    </div>                                      -->
                                  </div>
                                </el-collapse-item>
                                <!-- end -->
                                <!-- 审批人类型 -->
                                <el-collapse-item name="2" v-if="showItem">
                                  <template slot="title">
                                    <div class="margin-title">
                                      <span class="require">*</span>
                                      <span>审批人类型</span>
                                    </div>
                                  </template>
                                  <div class="pa_permisson-item">
                                    <div>
                                      <el-select v-model="taskData.approverType" @change="modelChange('approverType',$event)">
                                        <el-option v-for="approver in approvers" :key="approver.value"
                                        :label="approver.label"
                                        :value="approver">
                                        </el-option>
                                      </el-select>
                                    </div>
                                  </div>
                                </el-collapse-item>
                                <!-- end -->
                                <!-- 单人/多人审批 选择审批人-->
                                <el-collapse-item name="3" v-if="!ownSelfShow">
                                  <template slot="title">
                                    <div class="margin-title">
                                      <span class="require">*</span>
                                      <span>选择审批人</span>
                                    </div>
                                  </template>
                                  <div class="sel-member-box clear">
                                    <div class="pull-right" @click="selectApprover()"><i class="iconfont icon-pc-paper-accret"></i></div>
                                    <!-- <el-input v-model="value" placeholder="请输入内容..."  class="input"></el-input> -->
                                    <div class="member-name pull-left" v-for="(member,index) in taskData.approverObj" :key="member.id"><span>{{member.name}}</span><i class="iconfont icon-pc-member-close" @click="deleteMember(index)"></i></div>
                                  </div>
                                </el-collapse-item>
                                <!-- end -->
                                <!-- 终止审批人 -->
                                <el-collapse-item name="8" v-if="multiStageShow">
                                  <template slot="title">
                                    <div class="margin-title">
                                      <span>终止审批人</span>
                                    </div>
                                  </template>
                                  <div class="sel-member-box clear">
                                    <div class="pull-right" @click="selectApprover()"><i class="iconfont icon-pc-paper-accret"></i></div>
                                    <!-- <el-input v-model="value" placeholder="请输入内容..."  class="input"></el-input> -->
                                    <div class="member-name pull-left" v-for="(member,index) in taskData.approverObj" :key="member.id"><span>{{member.name}}</span><i class="iconfont icon-pc-member-close" @click="deleteMember(index)"></i></div>
                                  </div>
                                  <div class="last-des">
                                    <p>只要终止审批人审批过后，审批单就不再继续往上流转、终止审批人可以多选。</p>
                                  </div>
                                </el-collapse-item>
                                <!-- end -->
                                <!-- 多人审批-审批方式 -->
                                <el-collapse-item name="4" v-if="multiShow">
                                    <template slot="title">
                                      <div class="margin-title">
                                        <span>审批方式</span>
                                      </div>
                                    </template>
                                    <div class="margin-div">
                                      <div class="height-div">
                                        <el-radio-group size="mini" v-model="taskData.approvalType"
                                        @change="modelChange('approvalType', $event)">
                                          <div class="height-div">
                                            <el-radio label='0'>依次审批 <span class="explain">（本环节内审批人依次审批）</span></el-radio>
                                          </div>
                                          <div class="height-div height-div-middle">
                                            <el-radio label='1'>会签 <span class="explain">（所有审批人同时收到审批，且所有人需同意方可算审批通过）</span></el-radio>
                                          </div>
                                          <div class="height-div">
                                            <el-radio label='2'>或签 <span class="explain">（所有审批人同时收到审批，只需一个人审批通过或拒绝即可）</span>  </el-radio>
                                          </div>
                                        </el-radio-group>
                                      </div>
                                    </div>
                                  </el-collapse-item>
                                <!-- end -->
                                <!-- 角色审批-审批方式 -->
                                <el-collapse-item name="12" v-if="roleShow">
                                    <template slot="title">
                                      <div class="margin-title">
                                        <span>审批方式</span>
                                      </div>
                                    </template>
                                    <div class="margin-div">
                                      <div class="height-div">
                                        <el-radio-group size="mini" v-model="taskData.approvalType"
                                        @change="modelChange('approvalType', $event)">
                                          <!-- <div class="height-div">
                                            <el-radio label='0'>发起人/审批人从角色中自选一个</el-radio>
                                          </div> -->
                                          <div class="height-div height-div-middle">
                                            <el-radio label='1'>会签 <span class="explain">（所有审批人同时收到审批，且所有人需同意方可算审批通过）</span></el-radio>
                                          </div>
                                          <div class="height-div">
                                            <el-radio label='2'>或签 <span class="explain">（所有审批人同时收到审批，只需一个人审批通过或拒绝即可）</span>  </el-radio>
                                          </div>
                                        </el-radio-group>
                                      </div>
                                    </div>
                                  </el-collapse-item>
                                <!-- end -->
                                <!-- 单级审批 -->
                                <el-collapse-item name="5" v-if="singleStageShow">
                                      <template slot="title">
                                        <div class="margin-title">
                                          <span>选择审批人</span>
                                        </div>
                                      </template>
                                      <div class="pa_permisson-item">
                                        <div class="pull-left"><span>发起人的</span></div>
                                        <div class="pull-left sin-select-item">
                                          <el-select v-model="taskData.approverDepartmentSingle" @change="modelChange('singeStage',$event)">
                                            <el-option v-for="singleApproval in hierarchyList" :key="singleApproval.value"
                                            :label="singleApproval.label"
                                            :value="singleApproval">
                                            </el-option>
                                          </el-select>
                                        </div>
                                      </div>
                                  </el-collapse-item>
                                <!-- 待审递补  -->
                                <el-collapse-item name="6" v-if="multiStageShow || singleStageShow">
                                  <template slot="title">
                                    <div class="margin-title">
                                      <span>待审递补</span>
                                    </div>
                                  </template>
                                  <div class="pa_permisson-item">
                                    <div class="pa_pendding">
                                      <el-checkbox v-model="taskData.approvalReplace"
                                      @change="modelChange('replace',$event)">若该审批人空缺，由其在通讯录中的上级部门负责人代审批</el-checkbox>
                                    </div>
                                  </div>
                                </el-collapse-item>
                                <!-- end -->
                                <!-- 选择抄送人 -->
                                <el-collapse-item name="7" v-if="taskData.approverType.value !== '5'">
                                  <template slot="title">
                                    <div class="margin-title">
                                      <span>选择抄送人</span>
                                    </div>
                                  </template>
                                  <div class="sel-member-box clear">
                                    <div class="pull-right" @click="selectCCer()"><i class="iconfont icon-pc-paper-accret"></i></div>
                                    <!-- <el-input v-model="value" placeholder="请输入内容..."  class="input"></el-input> -->
                                    <div class="member-name pull-left" v-for="(member,index) in taskData.ccTo" :key="member.id"><span>{{member.name}}</span><i class="iconfont icon-pc-member-close" @click="deleteCcMember(index)"></i></div>
                                  </div>
                                </el-collapse-item>
                                <!-- end -->
                                <!-- 驳回方式 -->
                                <el-collapse-item name="9" v-if="showItem">
                                  <template slot="title">
                                    <div class="margin-title">
                                      <span class="require">*</span>
                                      <span>驳回方式</span>
                                      <el-popover
                                        placement="bottom-start"
                                        width="115"
                                        trigger="hover">
                                        <div class="attention-box">
                                          <p >审批人可以根据审批内容选择驳回方式，可复选</p>
                                        </div>
                                        <i class="iconfont icon-tip tip" slot="reference"></i>
                                      </el-popover>
                                    </div>
                                  </template>
                                  <div class="pa_permisson-item">
                                  <el-checkbox-group v-model="taskData.rejectType" @change="modelChange('rejectType',$event)" :min="1">
                                    <div><el-checkbox label="0">驳回给上一节点审批人</el-checkbox></div>
                                    <div><el-checkbox label="1">驳回到发起人</el-checkbox></div>
                                    <div><el-checkbox label="2">驳回到指定节点</el-checkbox></div>
                                    <div><el-checkbox label="3" >驳回并结束</el-checkbox></div>
                                  </el-checkbox-group>
                                  </div>
                                </el-collapse-item>
                                <!-- end -->
                                <!-- 抄送方式 -->
                                <el-collapse-item name="10" v-if="nodeType ==='end'">
                                  <template slot="title">
                                    <div class="margin-title">
                                      <span>抄送方式</span>
                                    </div>
                                  </template>
                                  <div class="pa_permisson-item">
                                    <!-- <div>
                                      <el-checkbox v-model="checked">审批通过抄送</el-checkbox>
                                    </div>
                                    <div>
                                      <el-checkbox v-model="checked">审批驳回抄送</el-checkbox>
                                    </div> -->
                                  <el-checkbox-group v-model="taskData.ccType" @change="modelChange('ccType',$event)">
                                    <div><el-checkbox label="0">审批通过抄送</el-checkbox></div>
                                    <div><el-checkbox label="1">审批驳回抄送</el-checkbox></div>
                                  </el-checkbox-group>
                                  </div>
                                </el-collapse-item>
                                <!-- end -->
                                <!-- 字段权限 -->
                                <el-collapse-item name="11">
                                  <template slot="title">
                                    <div class="margin-title">
                                      <span class="require">*</span>
                                      <span>字段权限</span>
                                      <el-popover
                                        placement="bottom-start"
                                        width="136"
                                        trigger="hover">
                                        <div class="attention-box">
                                          <p >当前环节审批人与抄送人能够看见的字段，审批人能够编辑的字段。</p>
                                        </div>
                                        <i class="iconfont icon-tip tip" slot="reference"></i>
                                      </el-popover>
                                    </div>
                                  </template>
                                  <div class="input-box field-item clear">
                                    <div class="pa_permisson-item">
                                      <div class="item-left pull-left"><span>字段名称</span></div>
                                      <div class="item-middle pull-left"><span>可见</span></div>
                                      <div class="item-right pull-left" v-show="showEndEdit"><span>编辑</span></div>
                                    </div>
                                    <!-- <div class="pa_permisson-item">
                                      <div class="item-left pull-left"><span>全选</span></div>
                                      <div class="item-middle pull-left item-select"><el-checkbox v-model="isAllVisbale" @change="selectAllShow($event)"> </el-checkbox></div>

                                      <div class="item-right pull-left item-select"><el-checkbox v-model="isAllEdit" @change="selectAllEdit($event)"> </el-checkbox></div>
                                    </div> -->
                                    <div class="pa_permisson-item " v-for="(field,index) in taskData.fieldAuth" :key = "field.name">
                                      <div class="item-left pull-left ellipsis"><span :title="field.label">{{field.label}}</span></div>
                                      <div class="item-middle pull-left item-select"><el-checkbox v-model="field.view" @change="nodeModelChange('view',$event,index)" :disabled="field.view === '2'"></el-checkbox></div>
                                      <div class="item-right pull-left item-select" v-show="taskData.key !== 'endEvent'"><el-checkbox v-model="field.edit" :disabled="!field.view && field.name !== 'allSelect' || field.editDisable === '1'" @change="nodeModelChange('edit',$event,index)"></el-checkbox></div>
                                    </div>
                                  </div>
                                </el-collapse-item>
                                <!-- end -->
                            </el-collapse>
                          </div>
                      </el-tab-pane>
                      <el-tab-pane label="流程属性" name="second" class="tab-item">
                        <div >
                          <el-collapse v-model="processProperty">
                          <!-- 流程方式 -->
                          <el-collapse-item name="0">
                            <template slot="title">
                              <div class="margin-title">
                                <span class="require">*</span>
                                <span>流程方式</span>
                              </div>
                            </template>
                            <div class="margin-div">
                              <div class="height-div">
                                <el-radio-group size="mini" v-model="allData.processType" @change="modelChange('processType', $event)">
                                  <div class="height-div">
                                    <el-radio label= '0'>固定流程</el-radio>
                                      <el-popover
                                          placement="top-start"
                                          width="136"
                                          trigger="hover">
                                          <div class="attention-box">
                                            <p >按照设定好的流程来执行审批</p>
                                          </div>
                                          <i class="iconfont icon-tip tip" slot="reference"></i>
                                      </el-popover>
                                  </div>
                                  <div class="height-div">
                                    <el-radio label= '1'>自由流程</el-radio>
                                    <el-popover
                                        placement="bottom-start"
                                        width="136"
                                        trigger="hover">
                                        <div class="attention-box">
                                          <p >不需要设定流程，由前台操作人来决定审批流转流程</p>
                                        </div>
                                        <i class="iconfont icon-tip tip" slot="reference"></i>
                                    </el-popover>
                                  </div>
                                </el-radio-group>
                              </div>
                            </div>
                          </el-collapse-item>
                          <!-- END -->
                          <!-- 流程审批 -->
                          <el-collapse-item name="1">
                            <template slot="title">
                              <div class="margin-title">
                                <span class="require">*</span>
                                <span>审批流程</span>
                              </div>
                            </template>
                            <div class="margin-div">
                              <div class="height-div">
                                <el-radio-group size="mini" v-model="allData.ownerInvisible">
                                  <div class="height-div">
                                    <el-radio label='0'>发起人可见</el-radio>
                                  </div>
                                  <div class="height-div">
                                    <el-radio label='1'>发起人不可见</el-radio>
                                  </div>
                                </el-radio-group>
                              </div>
                            </div>
                          </el-collapse-item>
                          <!-- END -->
                          <!-- 流程提醒  -->
                          <el-collapse-item name="2">
                            <template slot="title">
                              <div class="margin-title">
                                <span>流程提醒</span>
                              </div>
                            </template>
                            <div class="pa_permisson-item">
                              <div>
                                <el-checkbox v-model="remindOwner" @change="modelChange('remindOwner',$event)">每个节点审批通过都提醒发起人</el-checkbox>
                              </div>
                            </div>
                          </el-collapse-item>
                          <!-- end -->
                          <!-- 操作 -->
                          <el-collapse-item name="3">
                            <template slot="title">
                              <div class="margin-title">
                                <span>操作</span>
                              </div>
                            </template>
                            <div class="pa_permisson-item">
                              <el-checkbox-group v-model="processHandleList" @change="modelChange('handle',$event)">
                                <div><el-checkbox label="0">允许发起人撤回审批</el-checkbox>
                                  <el-popover
                                      placement="bottom-start"
                                      width="136"
                                      trigger="hover">
                                      <div class="attention-box">
                                        <p >是否允许发起人在审批单未被审批之前撤回</p>
                                      </div>
                                      <i class="iconfont icon-tip tip" slot="reference"></i>
                                  </el-popover> </div>
                                <div><el-checkbox label="1">允许审批人转交审批</el-checkbox></div>
                                <div><el-checkbox label="2">允许发起人抄送审批</el-checkbox></div>
                                <div><el-checkbox label="3">允许审批人抄送审批</el-checkbox></div>
                                <div><el-checkbox label="4" >允许抄送人抄送审批</el-checkbox></div>
                              </el-checkbox-group>
                            </div>
                          </el-collapse-item>
                          <!-- end -->
                          <!-- 审批人去重 -->
                          <el-collapse-item name="4">
                            <template slot="title">
                              <div class="margin-title">
                                <span>审批人去重</span>
                              </div>
                            </template>
                            <div class="pa_permisson-item">
                              <div>
                                <el-select v-model="allData.approverDuplicate" @change="modelChange('duplication',$event)">
                                  <el-option v-for="item in reDuplications" :key="item.value"
                                  :label="item.label"
                                  :value="item">
                                  </el-option>
                                </el-select>
                              </div>
                            </div>
                          </el-collapse-item>
                          <!-- end -->
                          <div class="del-process" >
                            <el-button  @click="delProcessApproval()" :disabled="isProcess">删除流程</el-button>
                          </div>
                          </el-collapse>
                        </div>
                      </el-tab-pane>
                    </el-tabs>
                  </div>
                </div>
              <!-- </el-col>
              </el-container> -->
            </div>
        </el-row>
        <!-- 条件弹窗 -->
        <el-dialog
          title="分支条件"
          :visible.sync="conditionDialog"
          width="700px">
          <div class="conditon-container" v-if="conditionDialog">
                <conditionComponent :allCondition="initFieldList" :selCondition="taskData.branchWhereObj.relevanceWhere" :highWhere="taskData.branchWhereObj.seniorWhere" ref="conditionComponent" ></conditionComponent>
          </div>
          <span slot="footer" class="dialog-footer">
            <el-button type="primary" @click="saveCondition()">保 存</el-button>
            <el-button @click="conditionDialog = false">取 消</el-button>
          </span>
        </el-dialog>
        <employeeRadio :employeeRadioData="employeeRadioDatas"></employeeRadio>
        <empDepRoleMulti  :empDepRoleMultiData="empDepRoleMultiDatas"></empDepRoleMulti>
        <!-- <departmentRadio :departmentRadioData="departmentRadioDatas"></departmentRadio> -->
        <roleRadio :roleRadioData="roleRadioDatas"></roleRadio>
    </div>
    <!-- end -->
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import go from 'gojs'
import { GuidedDraggingTool } from '@/common/js/GuidDragginTool'
import employeeRadio from '@/common/components/employee-radio'
// import employeeMulti from '@/common/components/employee-multi'
import empDepRoleMulti from '@/common/components/emp-dep-role-multi'
// import departmentRadio from '@/common/components/department-radio'
// import EmpDepRoleMulti from '@/common/components/emp-dep-role-multi'/** 多选集合窗口 */
import roleRadio from '@/common/components/role-radio'
import utilClass from '@/common/js/tool.js'
import conditionComponent from '@/common/components/condition'
import { mapGetters, mapState, mapMutations } from 'vuex'
export default {
  name: 'processApproval',
  components: {empDepRoleMulti, conditionComponent, employeeRadio, roleRadio},
  data () {
    return {
      processData: '',
      $: go.GraphObject.make, // 初始化Go对象
      /** 属性设置数据**/
      activeNames: ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12'],
      title: '',
      value: '',
      currentBean: {'moduleBean': this.$route.query.bean},
      settingName: 'second',
      activeModule: ['one', 'two'],
      processProperty: ['0', '1', '2', '3', '4', '5', '6', '7'],
      checked: false,
      conditionVal: '',
      conditionType: [
        {value: '0', label: '任意条件均可流入该节点'},
        {value: '1', label: '满足指定条件才可流入该节点'}
      ],
      approvers: [ // 审批方式
        {value: '0', label: '单人审批'},
        {value: '1', label: '多人审批'},
        {value: '2', label: '部门负责人-单级'},
        {value: '3', label: '部门负责人-多级'},
        {value: '4', label: '指定角色'},
        {value: '5', label: '发起人自己'}
      ],
      approverMedel: '',
      singleApprovals: [
        {valuel: '0', label: '第一级部门负责人'},
        {valuel: '1', label: '第二级部门负责人'},
        {valuel: '2', label: '第三级部门负责人'},
        {valuel: '3', label: '第四级部门负责人'}
      ],
      singleApproModel: '',
      conditionDialog: false,
      fieldList: [], // 当前模块所有字段
      fieldModel: '',
      initFieldList: [], // 条件设置初始条件
      operatorList: [],
      operationModel: '',
      resultModel: '',
      formulaErr: false, // 判断高级公式是否合法
      conditionIndex: 1,
      processType: '0',
      reDuplications: [
        {value: '0', label: '不启用自动去重'},
        {value: '1', label: '同一个审批人在流程中多次出现，自动去重'},
        {value: '2', label: '同一个审批人在流程中连续出现，自动去重'}
      ],
      reDuplicationModel: '',
      remindOwner: false,
      processHandleList: ['0', '1', '2', '3', '4'],
      canvasHeight: '',
      client_height: '760px',
      approvalReplace: false, // 待审递补
      candidateRadioEmpForm: false, // 展示单选人控件
      candidateEmployeeForm: false, // 展示多选成员控件
      hierarchyList: [], // 公司层级
      employeeMultiDatas: {}, // duox选
      employeeRadioDatas: {},
      empDepRoleMultiDatas: {},
      departmentRadioDatas: {}, // 部门单选
      roleRadioDatas: {}, // 角色单选
      rejectTypeList: ['0'],
      ccTypeList: ['0'],
      conditionList: [],
      nodeType: 'taskFlow', // 判断目前选中的是什么
      conditionValue: '',
      seniorIndex: [],
      seniorShow: false,
      scaleValue: 100,
      overView: '',
      data: null,
      showNode: true,
      isAllVisbale: false,
      isAllEdit: false,
      showEndEdit: true,
      conditionOk: true,
      /** ********************** END *****************************/
      allData: {
        'processKey': '', // 流程id
        'processName': this.$route.query.moduleName, // 流程名称（同模块名称）
        'moduleBean': this.$route.query.bean, // 模块bean
        'processType': '0', // 流程方式：0固定流程，1自由流程
        'ownerInvisible': '0', // 审批流程：0发起人可见，1发起人不可见
        'remindOwner': '0', // 每个节点审批通过都提醒发起人：0未勾选，1勾选
        'processOperation': '0,1,2,3,4', // 操作
        // 0允许发起人撤回审批
        // 1允许审批人转交审批
        // 2允许发起人抄送审批
        // 3允许审批人抄送审批
        // 4允许抄送人抄送审批
        'approverDuplicate': {value: '0', label: '不启用自动去重'}, // 审批人去重
        // 0不启用自动去重
        // 1同一个审批人在流程中多次出现，自动去重
        // 2同一个审批人在流程中连续出现，自动去重
        'saveStartStatus': '0', // 保存启用状态（0保存，1保存并启用）
        'taskList': [
          { // 流程开始节点
            'key': 'firstTask',
            'loc': '100 0',
            'background': '#ffffff',
            'color': '#000000',
            'taskKey': 'firstTask', // 节点id
            'taskName': '流程开始节点', // 节点名称
            'text': '流程开始节点',
            'taskType': 'start', // 节点类型
            'ccTo': [], // 抄送人
            'width': 130,
            'fieldAuth': [// 字段权限
            ]
          },
          { // 流程结束节点
            'key': 'endEvent',
            'loc': '100 500',
            'background': '#ffffff',
            'color': '#000000',
            'taskKey': 'endEvent',
            'taskName': '流程结束节点',
            'text': '流程结束节点',
            'taskType': 'end',
            'ccTo': [],
            'ccType': '0',
            'width': 130,
            'fieldAuth': []
          }
        ],
        'sequenceFlow': [ ] // 节点连接线]

      },
      taskData: { // 临时数据
        'key': '',
        'loc': '100 100',
        'background': '#ffffff',
        'color': '#000000',
        'taskKey': '', // 节点id
        'text': '新节点',
        'taskName': '新节点', // 节点名称
        'taskType': 'taskFlow', // 节点类型
        'branchWhere': {
          'value': '0', 'label': '任意条件均可流入该节点'
        }, // 分支条件：0任意条件均可提交、流转，1满足指定条件才可提交、流转
        'branchWhereObj': {// 指定条件
          'relevanceWhere': [// 规则条件
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
          'seniorWhere': ''// 高级条件
        },
        'approverType': {
          'value': '0', 'label': '单人审批'
        }, // 审批人类型：0单人审批，1多人审批，2部门负责人-单级，3部门负责人-多级，4指定角色，5发起人自己
        'approverObj': [], // 审批对象（单人、多人、角色）
        'approvalType': '1', // 审批方式：0依次审批，1会签，2或签
        'approvalReplace': '1', // 待审递补：0未勾选，1勾选（部门负责人-单级、部门负责人多级）
        'approverDepartmentSingle': { // 单级审批
        },
        'rejectType': '3', // 驳回方式：0驳回给上一节点审批人，1驳回到发起人，2驳回到指定节点，3驳回并结束
        'ccTo': [], // 抄送人
        'ccType': '0', // 抄送方式：0审批通过抄送，1审批驳回抄送
        'fieldAuth': [// 字段权限
          // {
          //   'fieldName': '', // 字段名称
          //   'view': '0', // 可见：0不可见，1可见
          //   'edit': '0' // 可编辑：0不可编辑，1可编辑
          // }
        ],
        'width': 130
      },
      barObj: {},
      isProcess: true, // 判断存不存在流程 transform: rotate(45deg)
      Palette: null,
      isShowOverView: true
    }
  },
  methods: {
    initGo () {
      this.$ = go.GraphObject.make
      this.initToolbar()
      this.processData = this.$(go.Diagram, 'process-box', {
        initialContentAlignment: go.Spot.Center, // center Diagram contents
        'undoManager.isEnabled': true, // enable Ctrl-Z to undo and Ctrl-Y to redo
        scrollsPageOnFocus: false,
        allowDrop: true,
        allowTextEdit: true,
        allowDelete: true,
        draggingTool: new GuidedDraggingTool(),
        // 'draggingTool.guidelineWidth': 2.5,
        'ModelChanged': (e) => { // 保存并更新视图
          if (e.isTransactionFinished) {
            console.log(e.model.nodeDataArray, e.model.linkDataArray, '初始化......')
            e.model.linkDataArray.map((link, index) => {
              if (!link.id) {
                this.$set(link, 'sourceRef', link.from)
                this.$set(link, 'targetRef', link.to)
                this.$set(link, 'id', 'sequenceFlow' + new Date().getTime())
              }
            })
            // console.log(this.allData, 'allData')
            // console.log(this.taskData, 'taskData')
            if (this.allData.processType === '1') {
              this.processData.isEnabled = false
              this.processData.allowDrop = false
            } else {
              this.processData.isEnabled = true
              this.processData.allowDrop = true
            }
          }
        },
        'BackgroundSingleClicked': (e) => {
          // console.log(e, '背景')
          this.clickBackground()
        }
      })
      this.setNodeStyle()
      this.setLinkStyle()
      this.setOverview()
      this.setLingking()
      this.processData.model = new go.GraphLinksModel(this.allData.taskList, this.allData.sequenceFlow)
      // this.listenModify()
    },
    /**
     初始化图例面板
    *
    */
    // 初始化拖动面板
    initToolbar () {
      this.barObj = utilClass.deepClone(this.taskData)
      // this.barObj = { }
      this.barObj.key = 'task' + new Date().getTime()
      this.barObj.taskName = '添加节点'
      delete this.barObj.branchWhere
      delete this.barObj.branchWhereObj
      // this.barObj.fieldAuth = utilClass.deepClone(this.taskData.fieldAuth)
      console.log(this.barObj, '拖动面板')
      this.Palette = this.$(go.Palette, 'PaletteDiv',
        {
          scrollsPageOnFocus: false,
          // nodeTemplateMap: this.processData.nodeTemplateMap,
          'animationManager.isEnabled': false,
          model: new go.GraphLinksModel([
            this.barObj
          ])
        }
      )
      this.Palette.nodeTemplate =
        this.$(go.Node, 'Spot',
          this.$(go.TextBlock, { cursor: 'move', verticalAlignment: go.Spot.Center, height: 38, stroke: '#69696C', font: 'normal 15px Helvetica Neue, PingFang SC, Microsoft Yahei' },
            new go.Binding('text', 'taskName')),
          { locationSpot: go.Spot.Center },
          {
            selectionAdorned: false, // don't bother with any selection adornment
            selectionChanged: this.onSelectionChanged
          }
        )
      return this.Palette
    },
    /* 设置节点样式
    */
    setNodeStyle () {
      console.log('设置节点样式')
      this.processData.nodeTemplate = this.$(go.Node, 'Spot',
        this.$(go.Shape, 'RoundedRectangle',
          { name: 'Icon',
            // width: 130,
            height: 40,
            // minSize: new go.Size(100, 30),
            strokeWidth: 0.3,
            fill: '#ffffff'
            // cursor: 'pointer'
          },
          /** 2018-8-1 删除 */
          // new go.Binding('fill', '#fff'),
          new go.Binding('width', 'width')
        ),
        this.$(go.TextBlock, 'Default Text',
          {margin: 5, cursor: 'auto'},
          new go.Binding('text', 'text'),
          new go.Binding('stroke', 'color')
        ),
        {
          locationSpot: go.Spot.Center, // 节点位置在中间
          fromSpot: go.Spot.Bottom, // coming out from middle-right
          toSpot: go.Spot.Top // going into at middle-left
        },
        new go.Binding('location', 'loc', go.Point.parse).makeTwoWay(go.Point.stringify),
        {
          selectionAdorned: false, // don't bother with any selection adornment
          selectionChanged: this.onSelectionChanged
        },
        // 4个连接点
        this.makeNodePort('T', go.Spot.Top, false, true),
        this.makeNodePort('L', go.Spot.Left, false, false),
        this.makeNodePort('R', go.Spot.Right, false, false),
        this.makeNodePort('B', go.Spot.Bottom, true, false),
        {
          mouseOver: (e, node) => {
            this.showNodePort(node, true)
          },
          mouseLeave: (e, node) => { this.showNodePort(node, false) }
          // click: (e, node) => {
          //   this.setProperty(e, node)
          // }
          // selectionChanged: (node) => {
          //   console.log('正在拖动')
          //   this.setProperty(node)
          // }
        }
      )
    },
    /**
     * @params name: 流程图对象的节点ID
     *         spot: 控制怎么连线
     *         output,input: 控制是否可以从节点画线
     * **/
    makeNodePort (name, spot, output, input) {
      // the port is basically just a small circle that has a white stroke when it is made visible
      return this.$(go.Shape, 'Circle',
        {
          fill: 'transparent',
          stroke: null, // this is changed to "white" in the showPorts function
          desiredSize: new go.Size(9, 9),
          alignment: spot,
          alignmentFocus: spot, // align the port on the main Shape
          portId: name, // declare this object to be a "port"
          fromSpot: spot,
          toSpot: spot, // declare where links may connect at this port
          fromLinkable: output,
          toLinkable: input, // declare whether the user may draw links to/from here
          cursor: 'pointer' // show a different cursor to indicate potential link point CFF0FC
        })
    },
    /**
     * 选中节点的样式
     * @returns {*}
     */
    onSelectionChanged (node) {
      console.log(node, '改变选择')
      let icon = node.findObject('Icon')
      if (icon !== null) {
        if (node.isSelected) {
          icon.fill = '#CFF0FC'
        } else {
          icon.fill = '#ffffff'
        }
      }
      /* *modify by 2018-2-6 */
      if (node.isSelected && node.data.key) { // 处理字段数据互相影响
        console.log(node.data.key, '当前选中')
        this.allData.taskList.map((task, index) => {
          if (task.key === node.data.key) {
            // task = JSON.parse(JSON.stringify(node.data))
            this.setProperty(node)
          }
        })
      } else {
        console.log(node.data, '当前选中的是线')
        this.processData.allowDelete = true
      }
    },
    // 连线时的样式
    setLingking () {
      this.processData.toolManager.linkingTool.temporaryLink =
        this.$(go.Link,
          {layerName: 'Tool'},
          this.$(go.Shape,
            { stroke: '#26D0E0', strokeWidth: 2 })
        )
      this.processData.toolManager.linkingTool.temporaryFromNode =
        this.$(go.Node,
          { layerName: 'Tool' },
          this.$(go.Shape, 'Rectangle',
            { stroke: '#26D0E0',
              strokeWidth: 2,
              fill: null,
              portId: '',
              width: 10,
              height: 10
            })
        )
      this.processData.toolManager.linkingTool.temporaryToNode =
        this.$(go.Node,
          { layerName: 'Tool' },
          this.$(go.Shape, 'Rectangle',
            { stroke: '#26D0E0',
              strokeWidth: 2,
              fill: null,
              portId: '',
              width: 10,
              height: 10
            })
        )
      this.processData.toolManager.linkingTool.temporaryFromPort = this.processData.toolManager.linkingTool.temporaryFromNode.port
      this.processData.toolManager.linkingTool.temporaryToPort = this.processData.toolManager.linkingTool.temporaryToNode.port
    },
    makeNodeSelectionAdornmentTemplate () {
      return this.$(go.Adornment, 'Auto',
        this.$(go.Shape, 'Rectangle',
          { fill: null,
            stroke: '#29AB91'
            // strokeWidth: 3,
            // width: 100,
            // height: 30,
            // maxSize: new go.Size(150, 30)
          }),
        this.$(go.Placeholder)
      )
    },
    /** end */
    /* /**
      /*设置连线样式
      */
    // 线选中时的样式
    linkSelectionAdornmentTemplate () {
      let linkSelecttion = this.$(go.Adornment, 'Link',
        this.$(go.Shape,
          // isPanelMain declares that this Shape shares the Link.geometry
          { isPanelMain: true, fill: '#26D0E0', stroke: '#26D0E0', strokeWidth: 3 }) // use selection object's strokeWidth
      )
      return linkSelecttion
    },
    setLinkStyle () {
      this.processData.linkTemplate = this.$(go.Link, // the whole link panel
        {
          routing: go.Link.AvoidsNodes,
          curve: go.Link.JumpOver,
          corner: 5,
          toShortLength: 4,
          relinkableFrom: true,
          relinkableTo: true,
          reshapable: true,
          resegmentable: true,
          smoothness: 1.5,
          // path: ''
          // mouse-overs subtly highlight links:
          mouseEnter: function (e, link) { link.findObject('HIGHLIGHT').stroke = 'rgba(30,144,255,0.2)' },
          mouseLeave: function (e, link) {
            link.findObject('HIGHLIGHT').stroke = 'transparent'
          }
        },
        { selectable: true, selectionAdornmentTemplate: this.linkSelectionAdornmentTemplate },
        {selectionChanged: this.onSelectionChanged},
        new go.Binding('points').makeTwoWay(),
        this.$(go.Shape, // the highlight shape, normally transparent
          { isPanelMain: true, strokeWidth: 4, stroke: 'transparent', name: 'HIGHLIGHT' }),
        this.$(go.Shape, // 连线路径样式
          { isPanelMain: true, stroke: '#26D0E0', strokeWidth: 1 }),
        this.$(go.Shape, // 箭头样式
          {toArrow: 'standard', stroke: null, fill: '#26D0E0'}),
        this.$(go.Panel, 'Auto', // 连线文字，默认不可见
          {visible: false, name: 'LABEL', segmentIndex: 2, segmentFraction: 0.5},
          new go.Binding('visible', 'visible').makeTwoWay(),
          this.$(go.Shape, 'RoundedRectangle', // 文字图形
            { fill: '#F8F8F8', stroke: null }),
          this.$(go.TextBlock, 'Yes', // 文字
            {
              textAlign: 'center',
              font: '10pt helvetica, arial, sans-serif',
              stroke: '#333333',
              editable: true
            },
            new go.Binding('text').makeTwoWay())
        )
      )
    },
    /**
     * 是否显示步骤的连接点
     * @param node
     * @param show
     */
    showNodePort (node, show) {
      node.ports.map((port, index) => {
        if (port.portId !== '') { // don't change the default port, which is the big shape
          // console.log(port.portId, 'portId')
          port.fill = show ? 'rgba(41,171,145,0.5)' : null
        }
      })
    },

    // 节点选中事件
    setProperty (node) {
      console.log(node.data, '000')
      // this.taskData.taskKey = node.data.key
      // this.taskData.taskName = node.data.text
      this.settingName = 'first'
      this.showNode = false
      switch (node.data.taskType) {
        case 'start':
          this.nodeType = 'start'
          // this.$set(this.taskData, 'text', node.data.text)
          // this.$set(this.taskData, 'ccTo', node.data.ccTo)
          this.taskData.text = node.data.text
          this.taskData.ccTo = node.data.ccTo
          this.taskData.key = node.data.key
          this.taskData.fieldAuth = utilClass.deepClone(node.data.fieldAuth)
          this.handViewAndEdit()
          console.log(this.taskData, '开始')
          this.showEndEdit = true
          break
        case 'end':
          this.nodeType = 'end'
          this.$set(this.taskData, 'text', node.data.text)
          this.$set(this.taskData, 'key', node.data.key)
          this.$set(this.taskData, 'ccTo', node.data.ccTo)
          this.taskData.fieldAuth = utilClass.deepClone(node.data.fieldAuth)
          if (node.data.ccType.length === 1) {
            this.taskData.ccType = node.data.ccType.split('')
          } else {
            this.taskData.ccType = node.data.ccType.split(',')
          }
          this.handViewAndEdit()
          console.log(this.taskData, '结束')
          this.showEndEdit = false
          break
        default: // 节点
          this.nodeType = 'taskFlow'
          this.taskData.text = node.data.text
          this.taskData.key = node.data.key
          this.taskData.approverObj = node.data.approverObj
          this.taskData.ccTo = node.data.ccTo
          // let len = node.data.fieldAuth.length
          if (node.data.fieldAuth.length === 0) { // 没有字段的时候
            if (JSON.stringify(this.taskData.fieldAuth) !== '{}') {
              this.taskData.fieldAuth.map((item, idx) => {
                item.view = true
                item.edit = false
              })
            }
            console.log(this.taskData.fieldAuth, '当前')
            node.data.fieldAuth = utilClass.deepClone(this.taskData.fieldAuth)
            this.isAllVisbale = true
            this.isAllEdit = false
          } else {
            this.taskData.fieldAuth = []
            this.taskData.fieldAuth = utilClass.deepClone(node.data.fieldAuth)
            if (JSON.stringify(this.taskData.fieldAuth) !== '{}') { // 判断字段是否为空
              this.taskData.fieldAuth.forEach((field, index) => {
                // let viewCount = 0
                // const editCount = 0
                if (field.view === '1') {
                  field.view = true
                  // viewCount += 1
                } else if (field.view === '0') {
                  field.view = false
                }
                if (field.edit === '1') {
                  field.edit = true
                } else if (field.edit === '0') {
                  field.edit = false
                }
              })
            }
          }
          this.taskData.rejectType = node.data.rejectType.split(',')
          node.data.approvalReplace === '1' ? this.taskData.approvalReplace = true : this.taskData.approvalReplace = false
          /** 审批人类型 */
          this.taskData.approverType = node.data.approverType
          this.taskData.approvalType = node.data.approvalType
          this.taskData.approverDepartmentSingle = node.data.approverDepartmentSingle
          this.taskData.ccType = node.data.ccType.split(',')
          /** end */
          /** 条件分支 */
          if (node.data.branchWhere === undefined) {
            console.log(node.data, '不存在条件')
            this.$set(this.taskData, 'branchWhere', {label: '任意条件均可流入该节点', value: '0'})
            this.$set(this.taskData, 'branchWhereObj', {relevanceWhere: [], seniorWhere: ''})
            let obj = {
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
            this.taskData.branchWhereObj.relevanceWhere.push(obj)
            node.data.branchWhere = utilClass.deepClone(this.taskData.branchWhere)
            node.data.branchWhereObj = utilClass.deepClone(this.taskData.branchWhereObj)
            console.log('第一次进来')
          } else {
            console.log(node.data.branchWhere, '进来时')
            this.taskData.branchWhere = utilClass.deepClone(node.data.branchWhere)
            this.taskData.branchWhereObj = utilClass.deepClone(node.data.branchWhereObj)
          }

          /** end */
          console.log(this.taskData, '处理完的节点')
          this.showEndEdit = true
          break
      }
      this.disableDeleteCommand()
    },
    //
    /* modify by 2018-6-7  修改DELETE不能删除默认节点 */
    disableDeleteCommand () {
      let temKey = this.allData.taskList[2].key
      let nowKey = this.taskData.key
      if (nowKey === 'firstTask' || nowKey === 'endEvent' || nowKey === temKey) {
        this.processData.allowDelete = false
      } else {
        this.processData.allowDelete = true
      }
    },
    // 概览
    setOverview () {
      this.overView = this.$(go.Overview, 'overView',
        {observed: this.processData,
          contentAlignment: go.Spot.Center
        })
      this.overView.box.findObject('BOXSHAPE').stroke = '#29AB91'
      console.log(this.overView.box, 'box')
    },
    /** ***************** end ********************** */
    // 获取屏幕高度
    getHeight () {
      this.client_height = (document.body.clientHeight - 159) + 'px'
      this.canvasHeight = document.body.clientHeight - 200 + 'px'
      // console.log(this.client_height)
    },
    // 高度自适应
    autoHeight () {
      window.addEventListener('resize', () => {
        this.getHeight()
      })
    },
    // 切换高级条件弹框
    switchDialog () {
      this.conditionDialog = !this.conditionDialog
    },
    // 初始化配置信息,条件
    initConfig () {
      let beanObj = {'bean': this.currentBean.moduleBean, dynamicFlag: '1'}
      if (this.currentBean.moduleBean !== undefined) {
        HTTPServer.getInitCondition(beanObj)
          .then((res) => {
            this.initFieldList = res
            console.log(this.initFieldList, '初始化条件')
          })
      }
    },
    // 处理字段可见和编辑
    handViewAndEdit () {
      this.taskData.fieldAuth.forEach((field, index) => {
        if (field.view === '1') {
          field.view = true
        } else if (field.view === '0') {
          field.view = false
        }
        if (field.edit === '1') {
          field.edit = true
        } else if (field.edit === '0') {
          field.edit = false
        }
      })
    },
    // 返回当前编辑节点

    // model发生改变
    modelChange (type, data) {
      console.log(type, data, this.taskData, '数据发生改变')
      switch (type) {
        case 'remindOwner':
          data ? this.allData.remindOwner = '1' : this.allData.remindOwner = '0'
          console.log(this.allData, '总数据')
          break
        case 'handle':
          this.allData.processOperation = ''
          this.allData.processOperation = data.join(',')
          console.log(this.allData, '总数据')
          break
        case 'ccType':
          this.allData.taskList.map((node, index) => {
            if (node.key === this.taskData.key) {
              node.ccType = data.toString()
            }
          })
          console.log(this.allData, '总数据数据')
          break
        case 'rejectType':
          this.allData.taskList.map((node, index) => {
            if (node.key === this.taskData.key) {
              node.rejectType = data.toString()
            }
          })
          break
        case 'replace':
          this.allData.taskList.forEach((item, index) => {
            if (item.key === this.taskData.key) {
              data ? item.approvalReplace = '1' : item.approvalReplace = '0'
            }
          })
          console.log(this.allData.taskList, '节点数据')
          break
        case 'approvalType':
          this.allData.taskList.forEach((item, index) => {
            if (item.key === this.taskData.key) {
              item.approvalType = data
              // item.approverObj = []
            }
          })
          // this.taskData.approverObj = []
          console.log(this.allData.taskList, '节点数据')
          break
        case 'approverType':
          this.taskData.approverObj = []
          this.allData.taskList.forEach((item, index) => {
            if (item.key === this.taskData.key) {
              item.approverType = data
              item.approverObj = []
              if (data.value === '1') { // 多人审批
                item.approvalType = '0' // 设置为会签
              } else if (data.value === '4') { // 指定角色
                item.approvalType = '1'
              }
            }
          })
          this.taskData.approverObj = []
          if (data.value === '1') { // 多人审批
            this.taskData.approvalType = '0' // 设置为会签
          } else if (data.value === '4') { // 指定角色
            this.taskData.approvalType = '1'
          }
          break
        case 'singeStage':
          this.allData.taskList.forEach((item, index) => {
            if (item.key === this.taskData.key) {
              item.approverDepartmentSingle = data
            }
          })
          console.log(this.allData.taskList, '节点数据')
          break
        case 'field':

          break
        case 'operator':
          this.allData.taskList.forEach((item, index) => {
            if (item.key === this.taskData.key) {
              item.branchWhereObj.relevanceWhere.fieldName = data.value
            }
          })
          break
        case 'processType':
          if (data === '1') {
            this.processData.isEnabled = false
            this.processData.allowDrop = false
            // this.reGetProcessApproval()
          } else {
            this.processData.isEnabled = true
            this.processData.allowDrop = true
          }
          console.log(this.allData, '节点数据')
          break
        default:
          break
      }
    },
    // 节点属性发生变化
    nodeModelChange (type, event, index) {
      console.log(type, event, index, '节点属性')
      switch (type) {
        case 'name':
          this.allData.taskList.map((node, index) => {
            if (node.key === this.taskData.key) {
              console.log(node, this.taskData, '当前编辑项目')
              this.processData.model.setDataProperty(node, 'text', this.taskData.text)
              let len = this.taskData.text.length
              if (len > 8) {
                this.processData.model.setDataProperty(node, 'width', 130 + len * 6)
              } else {
                this.processData.model.setDataProperty(node, 'width', 130)
              }

              console.log(this.allData.taskList, '节点数据')
            }
            // this.processData.model = new go.GraphLinksModel(this.nodeData, this.linkData)
          })
          break
        case 'view':
          this.allData.taskList.map((node, idx) => {
            if (node.key === this.taskData.key) {
              console.log(node.key, this.taskData.key, 'key')
              event ? node.fieldAuth[index].view = '1' : node.fieldAuth[index].view = '0'
              if (!event) { // 不可见状态
                this.taskData.fieldAuth[index].edit = false
                node.fieldAuth[index].view = '0'
              }
              console.log(node.fieldAuth[index], '0000000')
              console.log(this.barObj, '拖动面板的数据')
              if (index === 0) { // 处理选中全选状态
                if (!event) { // 取消选中
                  this.taskData.fieldAuth[0].edit = false
                  node.fieldAuth[0].edit = false
                }
                this.taskData.fieldAuth.map((field, ids) => {
                  event ? field.view = true : field.view = false
                  if (!event) {
                    field.edit = false
                  }
                })
                node.fieldAuth.map((field, ids) => {
                  event ? field.view = '1' : field.view = '0'
                })
              } else { // 不是全选选中的字段
                if (event) { // 取消选择
                  let len = this.taskData.fieldAuth.length - 1
                  let num = 0
                  this.taskData.fieldAuth.map((field, idy) => {
                    if (field.view) {
                      num += 1
                    }
                  })
                  console.log(num, '可见的数量')
                  num === len ? this.taskData.fieldAuth[0].view = true : this.taskData.fieldAuth[0].view = false
                  num === len ? node.fieldAuth[0].view = true : node.fieldAuth[0].view = false
                } else {
                  this.taskData.fieldAuth[0].view = false
                  node.fieldAuth[0].view = '0'
                }
              }
            }
          })
          console.log(this.allData.taskList, '节点数据')
          break
        case 'edit':
          this.allData.taskList.map((node, idx) => {
            if (node.key === this.taskData.key) {
              event ? node.fieldAuth[index].edit = '1' : node.fieldAuth[index].edit = '0'
              /** modify by 2018-1-31 ********/
              if (index === 0) { // 处理选中全选状态
                this.taskData.fieldAuth.map((field, ids) => {
                  if (ids > 0) {
                    if (field.editDisable !== '1') { // 非只读的字段
                      event && field.view ? field.edit = true : field.edit = false
                    }
                  }
                })
                node.fieldAuth.map((_field, idz) => {
                  if (idz > 0) {
                    if (_field.editDisable !== '1') {
                      event && _field.view ? _field.edit = '1' : _field.edit = '0'
                    }
                  }
                })
              } else {
                if (event) { // 不是全选选中的字段
                  // let len = this.taskData.fieldAuth.length - 1
                  let numView = 0 // 可见的数量
                  let numEdit = 0 // 可编辑的数量
                  let canEditLen = 0 // 可以编辑的长度
                  this.taskData.fieldAuth.map((field, idy) => {
                    if (field.edit && idy > 0 && field.editDisable !== '1') {
                      numEdit += 1
                    }
                    if (idy > 0 && field.editDisable !== '1') {
                      canEditLen += 1
                    }
                    if (field.view && idy > 0) {
                      numView += 1
                    }
                  })
                  console.log(numView, numEdit, '可见的数量')
                  canEditLen === numEdit ? this.taskData.fieldAuth[0].edit = true : this.taskData.fieldAuth[0].edit = false
                  canEditLen === numEdit ? node.fieldAuth[0].edit = true : node.fieldAuth[0].edit = false
                } else {
                  this.taskData.fieldAuth[0].edit = false
                  node.fieldAuth[0].edit = '0'
                }
              }
              /* ****end */
            }
          })
          console.log(this.allData.taskList, '节点数据')
          console.log(this.taskData.fieldAuth, 'taskData')
          break
        default:
          break
      }
    },
    // 条件MODEL 发生变化
    conditionChange (type, data, index) {
      console.log(type, data, index, '条件发生改变')
      switch (type) {
        case 'branch':
          this.allData.taskList.forEach((item, idx) => {
            if (item.key === this.taskData.key) {
              item.branchWhere.value = data.value
              item.branchWhere.label = data.label
              if (data.value === '0') {
                let obj = {// 指定条件
                  'relevanceWhere': [// 规则条件
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
                  'seniorWhere': ''// 高级条件
                }
                item.branchWhereObj = JSON.parse(JSON.stringify(obj))
                this.taskData.branchWhereObj = JSON.parse(JSON.stringify(obj))
              }
            }
          })
          console.log(this.barObj, '改变后的拖动面板')
          break
        case 'senior':
          this.allData.taskList.forEach((item, idx) => {
            if (item.key === this.taskData.key) {
              item.branchWhereObj.seniorWhere = data
            }
          })
          break
        default:
          break
      }
    },
    // 获取当前模块所有字段
    /**/
    // @params isExist //判断原来有没有流程图
    /***/
    getAllFieldList (isExist) {
      let url = 'layout/getFieldsByModule'
      let paramsObj = {
        'bean': this.currentBean.moduleBean,
        'systemField': '0'
      }
      HTTPServer.getAllFieldList(paramsObj, url)
        .then((res) => {
          console.log(res, '获取所有字段')
          this.taskData.fieldAuth = res
          console.log(this.taskData.fieldAuth, '所有字段')
          this.taskData.fieldAuth.map((field, index) => {
            this.$set(field, 'view', true)
            this.$set(field, 'edit', false)
          })
          let allSel = { // 加一个全选字段
            view: true,
            edit: false,
            label: '全选',
            name: 'allSelect'
          }
          this.taskData.fieldAuth.splice(0, 0, allSel)
          if (isExist) { // 判断原来有没有路程图: 不存在的情况
            this.allData.taskList.map((node, index) => {
              node.fieldAuth = utilClass.deepClone(this.taskData.fieldAuth)
              if (node.key === 'firstTask') {
                node.fieldAuth.map((field, index) => {
                  if (field.editDisable !== '1') {
                    field.edit = true
                  }
                })
              }
              if (node.key === 'endEvent') {
                node.fieldAuth.map((field, index) => {
                  delete field.edit
                })
              }
            })
          }
          // this.barObj.fieldAuth = utilClass.deepClone(res.data.data)
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 处理保存的数据
    formatData () {
      // this.allData.processOperation = this.allData.processOperation.join(',')
      this.allData.taskList.map((node, index) => {
        node.taskKey = node.key
        node.taskName = node.text
        node.fieldAuth.map((item, idx) => {
          // item.fieldName = item.label
          if (item.view === true) {
            item.view = '1'
          } else if (item.view === false) {
            item.view = '0'
          }
          if (item.edit === true) {
            item.edit = '1'
          } else if (item.edit === false) {
            item.edit = '0'
          }
        })
      })
      this.allData.sequenceFlow.map((link, index) => {
        if (link.points) {
          delete link.points
        }
      })
      console.log(this.allData, '处理后')
    },
    // 点击保存或者保存并使用
    handleSave (isUse) {
      console.log(this.allData, '最终数据')
      isUse ? this.allData.saveStartStatus = '1' : this.allData.saveStartStatus = '0'
      this.allData.processKey = 'process' + new Date().getTime()
      let allOk = true
      this.formatData()
      if (this.allData.processType === '0') {
        this.allData.taskList.forEach((item, index) => {
          if (item.taskName === '') {
            this.$message({
              showClose: true,
              message: '节点名称不能为空！',
              type: 'warning'
            })
            allOk = false
            return false
          }
          if (item.approverObj && item.approverObj.length === 0) {
            console.log('审批人....')
            if (item.approverType.value === '0' || item.approverType.value === '1' || item.approverType.value === '4') {
              this.$message({
                showClose: true,
                message: '请选择审批人！',
                type: 'warning'
              })
              allOk = false
              return false
            }
          }
        })
      }
      if (allOk) {
        this.saveProcessApproval()
      }
    },
    // 过滤字段权限不必要的属性
    filterFieldAuth () {
      this.fieldList.map((item, index) => {
        let fieldObj = {
          // 'fieldName': item.label, // 字段名称
          'view': item.view ? '1' : '0', // 可见：0不可见，1可见
          'edit': item.edit ? '1' : '0' // 可编辑：0不可编辑，1可编辑
        }
        this.taskData.fieldAuth.push(fieldObj)
      })
      console.log(this.taskData, '节点数据')
    },
    // 选择成员
    selectApprover () { // 选择审批人/终止审批人
      console.log(this.taskData.approverType.value, 56456456)
      switch (this.taskData.approverType.value) {
        case '0':
          this.employeeRadioDatas = {
            'prepareData': this.taskData.approverObj,
            'prepareKey': 'approver',
            'seleteForm': true,
            'banData': [],
            'navKey': '1',
            'key': this.taskData.key
          }
          // this.$bus.emit('commonMember', obj0)
          // console.log(obj0, 'kjk')
          break
        case '1':
          this.empDepRoleMultiDatas = {
            'prepareData': this.taskData.approverObj,
            'prepareKey': 'approver',
            'seleteForm': true,
            'banData': [],
            'navKey': '1',
            'type': 1,
            'key': this.taskData.key
          }
          // this.$bus.emit('commonMember', obj1)
          break
        case '3':
          this.empDepRoleMultiDatas = {
            'prepareData': this.taskData.approverObj,
            'prepareKey': 'approver3',
            'seleteForm': true,
            'banData': [],
            'key': this.taskData.key,
            'navKey': '0'
          }
          break
        case '4':
          this.roleRadioDatas = {
            'prepareData': this.taskData.approverObj,
            'prepareKey': '4',
            'seleteForm': true,
            'banData': [],
            'key': this.taskData.key
          }
          break
        default:
          break
      }
    },
    // 删除成员
    deleteMember (index) {
      this.taskData.approverObj.splice(index, 1)
      this.allData.taskList.forEach((item, index) => {
        if (item.key === this.taskData.key) {
          item.branchWhereObj.relevanceWhere.splice(index, 1)
        }
      })
    },
    // 删除抄送成员
    deleteCcMember (index) {
      this.taskData.ccTo.splice(index, 1)
      // this.allData.taskList.forEach((item, index) => {
      //   if (item.key === this.taskData.key) {
      //     item.branchWhereObj.relevanceWhere.splice(index, 1)
      //   }
      // })
    },
    selectCCer () { // 选择抄送人
      console.log(this.taskData.ccTo, 'cc')
      this.empDepRoleMultiDatas = {
        'prepareData': this.taskData.ccTo,
        'prepareKey': 'CCer',
        'seleteForm': true,
        'banData': [],
        'navKey': '1',
        'key': this.taskData.key
      }
    },
    // 获取公司层级
    getDepLevel () {
      HTTPServer.queryDepartmentLevel()
        .then((res) => {
          this.hierarchyList = res
          console.log(this.hierarchyList, '公司层级列表')
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 保存审批流程
    saveProcessApproval () {
      HTTPServer.saveProcessApproval(this.allData).then((res) => {
        this.isProcess = false
        this.$message({
          showClose: true,
          message: '保存审批流程成功',
          type: 'success'
        })
        console.log(res, 'data')
        let layout = JSON.parse(JSON.stringify(this.custom_layout))
        layout.processId = res.processId
        this.setLayout(layout)
      })
    },
    // 获取审批流程
    getProcessApproval () {
      // 获取审批流程
      if (this.currentBean.moduleBean === undefined) {
        this.$message({
          showClose: true,
          message: '请先创建模块再创建流程!',
          type: 'warning'
        })
      } else {
        HTTPServer.getProcessApproval(this.currentBean, true)
          .then((data) => {
            console.log(data, '000')
            console.log(data, '获取审批流程成功')
            if (data.processKey) {
              this.isProcess = false
              console.log('存在流程')
              this.allData.processKey = data.processKey
              this.allData.processName = data.processName
              this.allData.moduleBean = data.moduleBean
              this.allData.processType = data.processType
              this.allData.ownerInvisible = data.ownerInvisible
              if (data.remindOwner === '1') {
                this.remindOwner = true
              } else {
                this.remindOwner = false
              }
              this.allData.processOperation = data.processOperation
              this.processHandleList = data.processOperation.split(',')
              this.allData.remindOwner = data.remindOwner
              this.allData.approverDuplicate = data.approverDuplicate
              this.allData.sequenceFlow = data.sequenceFlow
              this.allData.taskList = data.taskList
              console.log(this.allData, '获取到的流程审批')
              // this.initProcess()
              this.getAllFieldList(false)
              this.initGo()
            } else {
              this.isProcess = true
              console.log('不存在审批流程')
              this.getAllFieldList(true)
              this.initProcess()
              this.initGo()
            }
          })
          .catch((err) => {
            console.log(err)
          })
      }
    },
    // 初始化流程图
    initProcess () {
      let nodeObj = utilClass.deepClone(this.taskData)
      // let nodeObj = {}
      nodeObj.key = 'task' + new Date().getTime()
      /** modify 2018-2-7 */
      nodeObj.loc = '100 100'
      nodeObj.text = '新节点'
      nodeObj.branchWhere = { 'value': '0', 'label': '任意条件均可流入该节点' }
      nodeObj.branchWhereObj = {
        'relevanceWhere': [
          {
            'field_label': '',
            'field_value': '',
            'operator_label': '',
            'operator_value': '',
            'result_label': '',
            'result_value': '',
            'showType': '',
            'operators': [],
            'entrys': [],
            'selList': [],
            'selTime': []
          }
        ],
        'seniorWhere': ''
      }
      nodeObj.approverType = {'value': '0', 'label': '单人审批'}
      nodeObj.approverObj = []
      nodeObj.approvalType = '0'
      nodeObj.approvalReplace = '1'
      nodeObj.approverDepartmentSingle = { }
      nodeObj.rejectType = '3'
      nodeObj.ccTo = []
      nodeObj.ccType = '0'
      nodeObj.fieldAuth = []
      nodeObj.width = 130
      /* *end  */
      this.allData.taskList.push(nodeObj)
      console.log(this.allData, '初始化一个点')
      let linkData = [
        {
          'from': 'firstTask',
          'to': nodeObj.key,
          'sourceRef': 'firstTask',
          'targetRef': nodeObj.key,
          'id': 'sequenceFlow' + new Date().getTime()
        },
        {
          'from': nodeObj.key,
          'to': 'endEvent',
          'sourceRef': nodeObj.key,
          'targetRef': 'endEvent',
          'id': 'sequenceFlow' + new Date().getTime()
        }
      ]
      this.allData.sequenceFlow = linkData
    },
    // 点击设置条件
    setCondition () {
      // if (this.initFieldList.length === 0) {
      //   this.initConfig()
      // }
      console.log(this.taskData.branchWhereObj.relevanceWhere, '857498496')
      this.switchDialog()
    },
    // 点击删除节点
    handleDeleteNode () {
      // let temKey = this.allData.taskList[2].key
      // let nowKey = this.taskData.key
      // if (nowKey === 'firstTask' || nowKey === 'endEvent' || nowKey === temKey) {
      //   this.$message({
      //     message: '初始化的流程节点不能删除！',
      //     type: 'warning',
      //     showClose: true
      //   })
      //   return
      // }
      if (this.processData.allowDelete) {
        this.processData.commandHandler.deleteSelection()
      } else {
        this.$message({
          message: '初始化的流程节点不能删除！',
          type: 'warning',
          showClose: true
        })
      }
    },
    // 点击撤销操作
    handleUndo () {
      this.processData.commandHandler.undo()
    },
    // 点击重置操作
    handleReset () {
      this.$confirm('重置后流程会恢复初始化样式，所有已经设置好的规则都将会被还原。 你确定要重置吗？', '重置', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.reInitProcessApproval()
      })
    },
    // 放大操作
    handleincreaseZoom () {
      this.scaleValue += 10
      this.processData.commandHandler.increaseZoom(1.05)
    },
    // 缩小操作
    handledecreaseZoom () {
      this.scaleValue -= 10
      this.processData.commandHandler.decreaseZoom(0.95)
    },
    // 重置比例
    handleShowOverView () {
      this.isShowOverView = !this.isShowOverView
    },
    // 重置审批流程
    reInitProcessApproval () {
      this.allData.taskList = []
      let startObj = { // 开始节点
        'key': 'firstTask',
        'loc': '100 0',
        'background': '#ffffff',
        'color': '#000000',
        'taskKey': 'firstTask', // 节点id
        'taskName': '流程开始节点', // 节点名称
        'text': '流程开始节点',
        'taskType': 'start', // 节点类型
        'ccTo': [], // 抄送人
        'width': 130,
        'fieldAuth': [] // 字段权限
      }
      let endObj = { // 流程结束节点
        'key': 'endEvent',
        'loc': '100 500',
        'background': '#ffffff',
        'color': '#000000',
        'taskKey': 'endEvent',
        'taskName': '流程结束节点',
        'text': '流程结束节点',
        'taskType': 'end',
        'ccTo': [],
        'ccType': '0',
        'width': 130,
        'fieldAuth': []
      }
      this.allData.taskList.push(startObj)
      this.allData.taskList.push(endObj)
      this.allData.sequenceFlow = []
      this.initProcess()
      this.getAllFieldList(true)
      console.log(this.allData, '删除后')
      this.processData.model = new go.GraphLinksModel(this.allData.taskList, this.allData.sequenceFlow)
    },
    // 保存条件
    saveCondition () {
      this.taskData.branchWhereObj.relevanceWhere = this.$refs.conditionComponent.handleLastData()
      this.taskData.branchWhereObj.seniorWhere = this.$refs.conditionComponent.high_where
      if (this.$refs.conditionComponent.judgeCondition()) {
        this.allData.taskList.map((task, index) => {
          if (task.key === this.taskData.key) {
            task.branchWhereObj.relevanceWhere = utilClass.deepClone(this.taskData.branchWhereObj.relevanceWhere)
            task.branchWhereObj.seniorWhere = this.taskData.branchWhereObj.seniorWhere
          }
        })
        this.switchDialog()
      }
    },
    // 处理填写的公式 /**用不上，浪费脑子 */
    handleSoniorWhere () {
      if (this.taskData.branchWhereObj.relevanceWhere.length === 1) {
        this.conditionValue = this.taskData.branchWhereObj.relevanceWhere[0].fieldLabel + this.taskData.branchWhereObj.relevanceWhere[0].operationLabel + this.taskData.branchWhereObj.relevanceWhere[0].resultValue
      } else if (this.taskData.branchWhereObj.seniorWhere !== '') {
        console.log(this.taskData.branchWhereObj.seniorWhere, '数字')
        let str = this.taskData.branchWhereObj.seniorWhere.toUpperCase()
        str = str.replace(/AND/ig, '且')
        str = str.replace(/OR/ig, '或')
        console.log(str, '去掉数字')
        let len = str.length
        let arr = []
        for (let i = 0; i <= len; i++) {
          let res = /^\+?[1-9][0-9]*$/.test(str.charAt(i))
          console.log(res, str.charAt(i))
          if (res) {
            this.seniorIndex += str.charAt(i)
          } else if (this.seniorIndex !== '') {
            arr.push(this.seniorIndex)
            this.seniorIndex = ''
            console.log(arr, this.seniorIndex, '填写的INDEX')
          }
        }
        if (arr.length > 1) {
          console.log(arr.length, '数组的长度')
          arr.forEach((item, index) => {
            if (item >= 0 && item <= this.taskData.branchWhereObj.relevanceWhere.length) {
              let reg = new RegExp(Number(item))
              console.log(item, this.taskData.branchWhereObj, 'index')
              let des = this.taskData.branchWhereObj.relevanceWhere[item - 1].fieldLabel + this.taskData.branchWhereObj.relevanceWhere[item - 1].operationLabel + this.taskData.branchWhereObj.relevanceWhere[item - 1].resultValue
              console.log(item, des)
              str = str.replace(reg, des)
            }
          })
        }

        console.log(str, '0000')
        this.conditionValue = str
      }
    },
    // 展示高级条件
    showSenior () {
      if (this.taskData.branchWhereObj.relevanceWhere.length < 2) {
        this.$message({
          showClose: true,
          message: '条件大于两个才能设置高级条件!',
          type: 'warning'
        })
        return
      }
      this.seniorShow = true
    },
    // 删除流程
    delProcessApproval () {
      this.$confirm('此操作将永久删除该流程, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        HTTPServer.delProcessApproval(this.currentBean)
          .then(() => {
            this.$message({
              showClose: true,
              message: '删除流程成功！',
              type: 'success'
            })
            this.isProcess = true
            this.reInitProcessApproval()
          })
          .catch(() => {
            this.$message({
              showClose: true,
              message: '删除流程失败！',
              type: 'error'
            })
          })
      })
    },
    // 点击背景
    clickBackground () {
      this.settingName = 'second'
      this.showNode = true
    },
    // 全选可见
    selectAllShow (data) {
      console.log(data, '全选中事件')
      this.taskData.fieldAuth.map((item, index) => {
        if (data) {
          item.view = true
        } else {
          item.view = false
        }
      })
      this.allData.taskList.map((node, idx) => {
        if (node.key === this.taskData.key) {
          // event ? node.fieldAuth[index].view = '1' : node.fieldAuth[index].view = '0'
          node.fieldAuth.map((field, _idx) => {
            this.taskData.fieldAuth.map((item, index) => {
              item.view ? field.view = '1' : field.view = '0'
            })
          })
        }
      })
      console.log(this.allData.taskList, '节点')
    },
    // 全选编辑
    selectAllEdit (data) {
      console.log(data, '全选编辑')
      this.taskData.fieldAuth.map((item, index) => {
        if (data) {
          if (item.view) {
            item.edit = true
          }
        } else {
          item.edit = false
        }
      })
      console.log(this.taskData, '临时')
      this.allData.taskList.map((node, idx) => {
        if (node.key === this.taskData.key) {
          node.fieldAuth.map((field, _idx) => {
            this.taskData.fieldAuth.map((item, index) => {
              if (field.field === item.field) {
                item.edit ? field.edit = '1' : field.edit = '0'
              }
            })
          })
        }
      })
      console.log(this.allData.taskList, '节点')
    },
    ...mapMutations({
      setLayout: 'CUSTOM_LAYOUT'
    })
  },
  computed: {
    showItem () {
      return !(this.nodeType === 'start' || this.nodeType === 'end')
    },
    // 单人审批要显示的项目
    singleShow () {
      return !(this.nodeType === 'start' || this.nodeType === 'end') && this.taskData.approverType === '0'
    },
    // 多人审批要显示的项目
    multiShow () {
      return !(this.nodeType === 'start' || this.nodeType === 'end') && this.taskData.approverType.value === '1'
    },
    // 角色审批要显示的项目
    roleShow () {
      return !(this.nodeType === 'start' || this.nodeType === 'end') && this.taskData.approverType.value === '4'
    },
    // 单级审批
    singleStageShow () {
      return !(this.nodeType === 'start' || this.nodeType === 'end') && this.taskData.approverType.value === '2'
    },
    // 多级审批人
    multiStageShow () {
      return !(this.nodeType === 'start' || this.nodeType === 'end') && this.taskData.approverType.value === '3'
    },
    // 发起人自己
    ownSelfShow () {
      return (this.nodeType === 'start' || this.nodeType === 'end') || this.taskData.approverType.value === '3' || this.taskData.approverType.value === '5' ||
      this.taskData.approverType.value === '2'
    },
    // mapGetters
    ...mapGetters([
      'custom_layout'
    ]),
    ...mapState({
      custom_layout: state => state.custom.custom_layout
    })
  },
  created () {
  },
  mounted () {
    this.getHeight()
    // this.autoHeight()
    this.getProcessApproval()
    this.initConfig()
    this.getDepLevel()
    // 成员单选
    this.$bus.on('selectMemberRadio', value => {
      if (value.prepareKey === 'approver') {
        this.taskData.approverObj = value.prepareData
        this.allData.taskList.map((item, index) => {
          if (item.key === value.key) {
            item.approverObj = value.prepareData
          }
        })
      }
    })
    /** 多选集合窗口 */
    this.$bus.on('selectEmpDepRoleMulti', value => {
      if (value) {
        if (value.prepareKey === 'approver') { // 1审批
          this.taskData.approverObj = value.prepareData.slice(0, 20)
          this.allData.taskList.map((item, index) => {
            if (item.key === value.key) {
              console.log(value.prepareData.slice(0, 20), '去除后')
              item.approverObj = this.taskData.approverObj
              // item.approverObj = item.approverObj.slice(0, 20)
            }
          })
        } else if (value.prepareKey === 'CCer') { // 2抄送
          this.taskData.ccTo = value.prepareData
          this.allData.taskList.map((item, index) => {
            if (item.key === value.key) {
              item.ccTo = value.prepareData
            }
          })
        } else if (value.prepareKey === 'approver3') { // 部门多级
          this.taskData.approverObj = value.prepareData
          this.allData.taskList.map((item, index) => {
            if (item.key === value.key) {
              // console.log(value.prepareData.slice(0, 20), '去除后')
              item.approverObj = this.taskData.approverObj
              // item.approverObj = item.approverObj.slice(0, 20)
            }
          })
        }
      }
    })
    /** 部门单选 */
    this.$bus.on('selectDepartmentRadio', value => {
      console.log(value, '部门单选')
      if (value) {
        this.taskData.approverObj = value.prepareData
        this.allData.taskList.map((item, index) => {
          if (item.key === value.key) {
            item.approverObj = value.prepareData
          }
        })
      }
      console.log(this.allData.taskList, '节点数据')
    })
    /** 角色单选 */
    this.$bus.on('selectRoleRadio', value => {
      console.log(value, '角色单选')
      if (value) {
        this.taskData.approverObj = value.prepareData
        this.allData.taskList.map((item, index) => {
          if (item.key === value.key) {
            item.approverObj = value.prepareData
          }
        })
      }
    })
  },
  beforeDestroy () {
    // this.$bus.off('commonMember')
    // this.$bus.off('selectEmpDepRoleMulti')
    // this.$bus.off('selectDepartmentRadio')
    // this.$bus.off('selectRoleRadio')
  }
}
</script>
<style lang="scss">
  // @import '../../../common/scss/dialog.scss';
// .el-main {
//   height: 100%;
// }
#process-container.process-container {
    background: #fff;
    margin: 0 80px;
    height: 100%;
    .process-border {
      height: 10px;
      background: #0062B1;
      -webkit-box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.5);
      box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.5);
    }
    .save-btn {
      line-height: 60px;
      margin-right: 15px;
      .el-button {
        padding: 5px 13px;
        height: 30px;
        width: 80px;
      }
    }
    .save-use {
      line-height: 60px;
      margin-right: 45px;
      .el-button {
        padding: 5px 13px;
        height: 30px;
        width: 90px;
      }
    }
    .el-main {
      padding: 0;
      background: #FFF;
      .process-header{
        height: 60px;
        line-height: 60px;
        border-top: 6px solid #0062B1;
        overflow: hidden;
      }
    }
    .process-name-box {
      margin-left: 20px;
      .process-name {
        font-size: 16px;
        color: #17171A;
        line-height: 60px;
        border-left: 3px solid #29AB91;
        padding-left: 5px;
        line-height: 50px;
        // color: #29AB91;
        i{
          margin-right: 10px;
        }
      }
    }
    //流程图头部
    //border: 1px solid #ccc;
    .process-command-box {
      height: 50px;
      border-top: 1px solid #E7E7E7;
      border-bottom: 1px solid #E7E7E7;
      line-height: 50px;
      box-sizing: border-box;
      padding-left:24px;
      .el-button--text {
        color: #69696C;
      }
      .scalc-btn {
        margin-left: 15px;
        margin-right: 15px;
      }
      .process-command {
        .iconfont {
          font-size: 14px;
          color: #69696C;
        }
        span {
          padding-left: 3px;
        }
      }
    }
    .row-bg {
      height: calc(100% - 70px)
    }
     // canvas 框
     .process-middle {
       width: calc(100% - 320px);
       .process-box {
          outline: none;
        //  canvas: {
        //  }
         :focus {
            outline: none;
         }
       }
       .canvas-box {
         height: calc(100% - 50px);
         position: relative;
         .process-box {
           height: 100%;
         }
       }
     }
    //属性设置
    .process-prop-box {
      width: 320px;
      border-left: 1px solid #E7E7E7;
      border-top: 1px solid #E7E7E7;
      // overflow: auto;
      box-sizing: border-box;
      height: 100%;
      .property-container {
        height: 100%;
        .seetting-name {
          height: 100%;
          .el-tabs {
            height: 100%;
          }
        }
      }
      .el-tabs__content {
        height:calc(100% - 48px);
        overflow: auto;
      }
      .pa_permisson-item {
        line-height: 30px;
        margin-left:30px;
        .item-left {
          width: 40%;
          white-space: nowrap;
          text-overflow: ellipsis;
          overflow: hidden;
        }
        .item-middle {
          width: 30%;
        }
        .item-right {
          width: 30%;
        }
        .sin-select-item {
          margin-left:14px;
          //box-sizing: border-box;
          width: 72%;
          margin-bottom:20px;
          // .el-select {
          //   //width: 60%;
          // }
        }
        .pa_pendding {
          width: 89%;
          white-space:normal;
          .el-checkbox__input {
            margin-top:-37px;
          }
          .el-checkbox__label {
            white-space:normal;
          }
        }
        .tip {
          font-size: 12px;
        }
      }
      .item-select {
        padding-left:6px;
        box-sizing: border-box;
      }
      .el-select {
        width: 89%;
      }
      .del-process {
        margin-bottom: 15px;
        button.el-button{
          width: 89%;
          border-color: #20bf9a;
          color: #20bf9a;
          font-size: 14px;
          // font-weight: bold;
          vertical-align: middle;
          height: 30px;
          line-height: 1px;
        }
      }
      .condition-text {
        border: 1px solid #E7E7E7;
        padding:5px;
        width: 85%;
        line-height:18px;
        color: #A0A0AE;
        margin-top:10px;
        min-height: 50px;
      }
      .height-div-middle {
        margin-top:10px;
        margin-bottom: 10px;
      }
      .height-div {
        line-height: 30px;
        position: relative;
        //margin-bottom: 10px;
        .explain {
          line-height: 15px;
          position: absolute;
          font-size: 12px;
          color: #cccccc;
          white-space:normal;
          // //padding-left:10px;
          width: 190px;
        }
      }
      .del-process {
        width: 90%;
        margin-top: 20px;
        margin-left:30px;
        // margin-bottom:30px;
      }
      .last-des {
        margin-top:5px;
        margin-left: 30px;
        font-size: 12px;
        width: 80%;
        color: #69696C;
      }
      .sel-member-box {
        border-radius: 2px;
        padding: 5px;
        width: 80%;
        margin-left:30px;
        border: 1px solid #E7E7E7;
        box-sizing: border-box;
        i{
          cursor: pointer;
        }
        .member-name {
          background: #EBEDF0;
          border-radius: 5px;
          color: #69696C;
          padding-left: 5px;
          padding-right: 5px;
          margin-top:3px;
          margin-left: 4px;
          span {
            padding-right: 3px
          }
          i {
            font-size: 10px;
          }
        }
      }
      .field-item {
        min-height: 200px;
      }
      .require {
        color: red
      }
      .set-condition {
        margin-top: 10px;
        .el-button {
          border-color: #409EFF;
          color: #409EFF;
          font-size: 14px;
          // font-weight: bold;
          width: 90%;
          vertical-align: middle;
          height: 30px;
          padding: 0;
        }
      }
      .el-tabs__header {
        width: 318px;
      }
    }
    .palette-box {
      width: 120px;
      height: 100%;
      position: relative;
      margin-left: 15px;
      i.iconfont {
        font-size: 14px;
      }
    }
    .bg-purple {
      position: relative;
    .show-over-view {
      display: none
    }
    .over-view {
      right: 5px;
      top: 3px;
      z-index: 1;
      position: absolute;
      height: 150px;
      width: 200px;
      box-shadow: -2px 2px 3px 0 rgba(155,155,155,0.30), 2px -2px 3px 0 rgba(155,155,155,0.20);
    }
    :focus {
      outline: none;
    }
    }
    #PaletteDiv {
      width: 100px;
      height: 100%;
      canvas {
      padding-top:5px;
      box-sizing: border-box;
      }
    }
    :focus {
      outline: none;
    }
    .conditon-container {
      width: 90%;
      margin-left: auto;
      margin-right: auto;
    }
    .show-node {
      padding-top: 100px;
      color: #989898;
      text-align: center;
      span {
        font-size: 24px;
      }
    }
    .palette-icon {
      .i {
        color: #69696C;
      }
      cursor: move;
      position: absolute;
    }
    .margin-div {
      width: 80%;
      margin-left: 30px;
      .tip {
        font-size: 12px;
      }
    }
    .seetting-detail {
      // overflow: auto;
      .label-name {
        margin-left: 30px;
        line-height: 41px;
      }
      .margin-div {
        width: 80%;
        margin-left: 30px;
        .height-div {
          line-height: 30px;
        }
        .default-val {
          margin-top: 20px;
        }
        .label {
          margin-top: 10px;
        }
        .datetime-sel {
          margin-top: 10px;
        }
      }
      .isActive {
        .icon-arrow {
          transform: rotate(45deg);
          transition: transform 0.3s, -webkit-transform 0.3s;
        }
      }
    }
    .tab-item {
      padding-left: 20px;
    }
    .dropdown-box {
      //width: 100%;
      .el-select {
        width: 100%;
      }
    }
    .process-property{
      .property-container {
        overflow: auto;
        .el-collapse {
          border-bottom: none;
        }
        .margin-title {
          font-size: 14px;
          .tip {
            font-size: 12px;
          }
        }
        .module-icon{
          padding: 20px 30px;
          .icon-box{
            width:100%;
            height: 80px;
            line-height: 80px;
            text-align: center;
            margin: 10px 0;
            background: #F1F7FF;
            border: 1px solid #CBE0FD;
            i{
              font-size: 60px;
              color: #FF6D5D;
              vertical-align: middle;
            }
          }
        }
        .module-classify {
          padding: 20px 30px;
          border-bottom: 1px solid #E7E7E7;
        }
        .el-tag {
          margin: 10px 10px 0 0;
        }
        .input-box {
          .el-input {
            width: 80%;

            .el-input__inner {
              margin-left: 30px;
            }
          }
        }
        .el-input__inner {
          height: 30px;
        }
        .el-collapse-item__header {
          margin-left: 7px;
          border-bottom: none;
          .el-collapse-item__arrow::before {
            //display: none;
            content: "\E60E";
          }
        }
        .el-collapse-item {
          .el-collapse-item__arrow.is-active {
            transform: rotate(45deg);
          }
        }
        .el-collapse-item__arrow {
          float: left;
        }
        .tab-item {
          padding-left: 0 !important;
        }
        .dropdown-box {
          margin-top: 10px;
          .el-input__inner {
            margin-left: 0;
          }
          .el-input {
            width: 100%;
          }
          .el-input__suffix {
            border-left: 0.1px solid #ccc;
          }
        }
        .drop-input {
          margin-top: 10px;
          float: left;
          .el-input {
            //width: 50px;
            .el-input__inner {
              width: 100px;
              border-color: transparent;
            }

            .el-input__inner:focus {
              border-color: #20BF9A;
            }
          }
        }
        .drop-color {
          margin-top: 10px;
          float: left;
          margin-left: 40px;
          .el-input {
            width: 114px;
            height: 24px;
          }
          .el-input__suffix {
            top: 3px;
          }
          .el-color-picker__trigger {
            height: 30px;
            width: 60px;
          }
        }
        .drop-delete {
          float: right;
          //margin-right: 34px;
          margin-top: 8px;
          font-size: 20px;
          cursor: pointer;
          i {
            color: red;
          }
        }
        .el-tabs__header {
          margin: 0;
        }
        .el-tabs__item {
          font-size: 16px;
          // margin-top: 16px;
          height: auto;
          line-height: 48px;
          width: 50%;
          text-align: center;
          padding:0;
        }
        .el-tabs__item.is-active {
          background: #267DC5;
          color: #fff;
        }
        .el-tabs__active-bar {
          display: none;
        }

        .el-tabs__nav {
          // margin-left: 65px;
          width: 100%;
          height: 48px;
          text-align: center;
          // .el-tabs__item:nth-child(2) {
          //   border-right: 2px solid #e7e7e7;
          // }
        }
        .el-tabs__nav-wrap::after {
          height: 1px;
        }

        .add-btn {
          width: 100%;
          //margin-left: 30px;
          margin-top: 10px;
          .el-button {
            //padding: 7px 15.5px;
            border-color: #409EFF;
            color: #409EFF;
            font-size: 14px;
            // font-weight: bold;
            width: 100%;
            vertical-align: middle;
            height: 30px;
            padding: 0;
          }
        }

      }
    }
    .el-dialog__header {
      background-color: #409EFF;
      span,i{
        color: #fff;
      }
    }
    .dialog-footer {
      .el-button {
        padding: 5px 13px;
        height: 30px;
        width: 80px;
      }
    }
}
.attention-box {
  p {
    font-size: 12px;
  }
}
</style>
