<template>
  <el-container class="main-project-task-wrap">
    <!-- <el-main style="padding-left:10%;"> -->
    <el-main>
      <draggable style="width:1000px;margin:0 auto;" v-model="list" :options="parentDraggable" @start="hiddleLine" @update="updata1($event,list)" @end="showLine(list)" @clone="clonedraggableTask1(list,'cloneOffsetHeight')" :class="projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'?'':'no-drag'">
        <div v-for="(v, k) in list" :key="k" class="allMain" :id="'offsetHeight'+k" :class="'cloneOffsetHeight'+k" draggable='false'>
          <span class="topLine"></span>
          <span v-if="k!==(list.length-1)" class="BottomLine" :id="'BottomLine'+k"></span>
          <div class="outBox">
            <div class="circle">
              <span><span></span></span>
            </div>
            <div class="parentCss" :class="['styleClass'+v.isshow,{0: 'hidden', 1: 'show'}[v.isshow]]" @click.stop="openShowOne(v,('offsetHeight'+k),('BottomLine'+k),$event,list)">
              <div class="firstItem">
                <i class="iconfont icon-yidong"></i>
                <span v-text="v.name" style="font-size:18px;color:#212121;"></span>
                <div class="rightSetting rightSettingBox">
                  <!-- 新建列表 -->
                  <span v-if="judgeProjectRoleInfo(20)&&v.flow_status=='0'&&projectBaseInfo.project_status !== '1'&&projectBaseInfo.project_status !== '2'" @click.stop="addTaskList(v,('offsetHeight'+k),('BottomLine'+k),$event,list)"><i class="iconfont icon-nav-quickly-add"></i></span>
                  <!-- 编辑和删除任务分组 -->
                  <span class="mouseEnterCss" @click.stop v-if="(judgeProjectRoleInfo(17)||judgeProjectRoleInfo(19))&&projectBaseInfo.project_status !== '1'&&projectBaseInfo.project_status !== '2'">
                    <i class="iconfont icon-Rectangle"></i>
                    <div class="delDiv">
                      <p @click.stop="setRename(v, 'task')" v-if="judgeProjectRoleInfo(17)"><i class="iconfont icon-module-menu-1"></i>重命名</p>
                      <p @click.stop="delTaskOrTaskList(v, 'task')" v-if="judgeProjectRoleInfo(19)"><i class="iconfont icon-huishouzhan1"></i>删除</p>
                    </div>
                  </span>
                </div>
              </div>
            </div>
          </div>
            <draggable v-model="v.subnodeArr" :options="{
              animation: 200,
              group: {name: v.flow_status==='1'?'':'sone'+k, pull: true, put: true},
              sort: true,
              ghostClass: 'ghost',
              filter: '.no-drag'
            }" @start="hiddleLine('',v)" @update="updata2($event,v.subnodeArr,v)" @add="changedata2($event,v,('offsetHeight'+k),('BottomLine'+k),2,list)" @end="empty($event,v.subnodeArr,('offsetHeight'+k),('BottomLine'+k),2,list)" class="second" :class="v.flow_status==='1'||projectBaseInfo.project_status=='1'||projectBaseInfo.project_status=='2'?'no-drag':''" v-if="v.isshow==1" @clone="clonedraggableTask1(v.subnodeArr,'clonetwoTaskList')">
            <div v-for="(v1, k1) in v.subnodeArr" :key="k1" class="secondItem" :id="'batchAdd' +k+k1" :class="'clonetwoTaskList'+k1">
              <div class="threeItem"  v-if="v1.id" :class="['styleClass'+v1.isshow,{0: 'hidden', 1: 'show'}[v1.isshow]]" @click.stop="openShowTwo(v1,('offsetHeight'+k),('BottomLine'+k),('batchAdd'+k+k1),$event, list)">
                <span class="showOrHidden">
                  <i class="iconfont icon-yidong"></i>
                  <!-- <span><span>0</span>/<span>4</span></span> -->
                  <span v-text="v1.name" style="font-size:16px;"></span>
                  <div class="rightSetting">
                    <!-- 新建任务 -->
                    <!-- <el-popover placement="bottom-start" width="120" trigger="click" popper-class="new-add-nav-quickly">
                      <div class="add-list-task">
                        <p @click="addRowData(v1)" v-if="v1.children_data_type=='1'||v1.children_data_type=='0'">新增子任务列表</p>
                        <p @click.stop="addTaskQuote(v1, v)" v-if="v1.children_data_type=='2'||v1.children_data_type=='0'">新增任务</p>
                      </div>
                      <span slot="reference" @click.stop v-if="judgeProjectRoleInfo(25)&&projectBaseInfo.project_status != '1'&&projectBaseInfo.project_status != '2'" class="new-add-nav-span"><i class="iconfont icon-nav-quickly-add"></i></span>
                    </el-popover> -->
                    <template v-if="v1.children_data_type=='1'">
                      <span @click.stop="addRowData(v1)" v-if="judgeProjectRoleInfo(25)&&projectBaseInfo.project_status != '1'&&projectBaseInfo.project_status != '2'" class="new-add-nav-span"><i class="iconfont icon-nav-quickly-add"></i></span>
                    </template>
                    <template v-if="v1.children_data_type=='2'">
                      <span @click.stop="addTaskQuote(v1, v)" v-if="judgeProjectRoleInfo(25)&&projectBaseInfo.project_status != '1'&&projectBaseInfo.project_status != '2'" class="new-add-nav-span"><i class="iconfont icon-nav-quickly-add"></i></span>
                    </template>
                    <!-- 编辑和删除列表，批量操作 -->
                    <span class="mouseEnterCss" @click.stop v-if="(judgeProjectRoleInfo(21)||judgeProjectRoleInfo(23)||judgeProjectRoleInfo(24))&&projectBaseInfo.project_status !== '1'&&projectBaseInfo.project_status !== '2'">
                      <i class="iconfont icon-Rectangle"></i>
                      <div class="delDiv listDelDiv">
                        <p @click.stop="setRename(v,'list', v1)" v-if="judgeProjectRoleInfo(21)&&v.flow_status==='0'"><i class="iconfont icon-module-menu-1"></i>重命名</p>
                        <p @click.stop="batchOperation(v1, ('batchAdd'+k+k1),('offsetHeight'+k),('BottomLine'+k),list,v)" v-if="judgeProjectRoleInfo(24)&&v1.children_data_type!=='1'"><i class="iconfont icon-Rectangle5"></i>批量操作</p>
                        <p @click.stop="delTaskOrTaskList(v, 'list', v1)" v-if="judgeProjectRoleInfo(23)&&v.flow_status==='0'"><i class="iconfont icon-huishouzhan1"></i>删除</p>
                      </div>
                    </span>
                  </div>
                </span>
              </div>
              <div v-if="!v1.id && v.isAddTaskList===0" class="threeItem no-drag" style="text-align:center" draggable="false" ondragstart="return false;">
                <span style="color:#ccc;">-- 暂无数据 --</span>
              </div>
              <div class="batchOperationSet" v-if="v1.isshowBatch === 1">
                <div>
                  <div class="batchTop">
                    <span class="endtimeCss"><span>截止时间</span></span>
                    <span class="batchEditor">
                      <el-date-picker v-model="v1.dateTime" type="datetime"  placeholder="选择日期时间" @blur="completeBatch(v1, 'date')" value-format="timestamp"></el-date-picker>
                    </span>
                    <span @click.stop="distributionPerson(v1)"><el-button>分配</el-button></span>
                    <span @click.stop="moveTask(v1)"><el-button>移动</el-button></span>
                  </div>
                  <div>
                    <!-- <el-radio v-model="v1.checked" @change="allChecked('',v1)" label="1">全选</el-radio> -->
                    <el-checkbox v-model="v1.checked" @change="allChecked('',v1)">全选</el-checkbox>
                    <span>已选择<span v-text="v1.checkedCount" style="margin:0 10px;color:#FF7610;">1</span>条数据</span>
                  </div>
                </div>
                <div @click.stop="completeBatchNew(v1,'success',list)">完成</div>
              </div>
              <draggable v-model="v1.subnodeArr" :options="{
                animation: 200,
                group: {name: v.flow_status==='1'?'':'soneItem'+k1, pull: true, put: true},
                sort: true,
                ghostClass: 'ghost',
                filter: '.no-drag'
              }"  @start="hiddleLine(v1.isshowBatch,v1)" @update="updata3($event,v1)" @add="changedata3(v1,('offsetHeight'+k),('BottomLine'+k),list)" @end="empty('',v1.subnodeArr,('offsetHeight'+k),('BottomLine'+k),'taskEmpty',list)" class="four" :class="v.flow_status==='1'||projectBaseInfo.project_status=='1'||projectBaseInfo.project_status=='2'?'no-drag':''" v-if="v1.isshow==1&&v1.children_data_type!=='1'">
                <!-- 任务列表 -->
                <div v-for="(v2,k2) in v1.subnodeArr" :key="k2" class="fourItme" :class="[v2.batchCheckOut===1?'batchCheckOutActive':'','batchSetStyle' + k2]" @click.stop="clickBatchCheckOut(v2,$event,v1)">
                  <div :style="v2.beanName==='empty'?'text-align:center;line-height:50px;':''" :class="v1.isshowBatch === 1?'subfourItemIn':'subfourItemOut'">
                    <div @click="openDeAllTypesdetalis(v2,v1,v)" v-if="v2.beanName!== 'empty'" style="position:relative;">
                      <!-- 待检验 -->
                      <i class="iconfont icon-CombinedShape" v-if="v2.check_status==='1'&&v2.dataType===2&&v2.passed_status=='0'&&v2.complete_status=='1'"></i>
                      <!-- 已通过 -->
                      <i class="iconfont icon-icon-test6" v-if="v2.check_status==='1'&&v2.dataType===2&&v2.passed_status=='1'&&v2.complete_status=='1'"></i>
                      <!-- 已驳回 -->
                      <i class="iconfont icon-bohui1" v-if="v2.check_status==='1'&&v2.dataType===2&&v2.passed_status=='2'&&v2.complete_status=='0'"></i>
                      <line-card-task :cloumn="v2" :listItem="v1" :projectBaseInfo="projectBaseInfo"></line-card-task>
                    </div>
                    <span v-if="v2.beanName==='empty'" style="color:#ccc;">-- 暂无数据 --</span>
                  </div>
                  <span v-if="v2.batchCheckOut===1" class="checkOutIcon"><i class="iconfont icon-pc-paper-optfor"></i></span>
                </div>
              </draggable>
              <draggable v-model="v1.subnodeArr" :options="{
                animation: 200,
                group: {name: v.flow_status==='1'?'':'soneItemNext'+k1, pull: true, put: true},
                sort: true,
                ghostClass: 'ghost',
                filter: '.no-drag'
              }"  @start="hiddleLine(v1.isshowBatch,v1)" @update="updata3($event,v1)" @add="changedata3(v1,('offsetHeight'+k),('BottomLine'+k),list)" @end="empty('',v1.subnodeArr,('offsetHeight'+k),('BottomLine'+k),'taskEmpty',list)" class="four" :class="v.flow_status==='1'||projectBaseInfo.project_status=='1'||projectBaseInfo.project_status=='2'?'no-drag':''" v-if="v1.isshow==1&&v1.children_data_type==='1'">
                <!-- 子任务列表 -->
                <div v-for="(item1,index1) in v1.subnodeArr" :key="index1" class="sub-new-box">
                  <div v-if="item1.id" class="sub-new-list" @click.stop="openShowThree(item1,list)" :class="['styleClass'+item1.isshow,{0: 'hidden', 1: 'show'}[item1.isshow]]">
                    <span class="showOrHidden showOrHiddennew">
                      <i class="iconfont icon-yidong"></i>
                      <span v-text="item1.name" style="font-size:16px;"></span>
                      <div class="rightSetting">
                        <!-- 新建任务 -->
                         <span slot="reference" @click.stop="addTaskQuote(v1, v, item1)" v-if="judgeProjectRoleInfo(25)&&projectBaseInfo.project_status != '1'&&projectBaseInfo.project_status != '2'"><i class="iconfont icon-nav-quickly-add"></i></span>
                        <!-- 编辑和删除列表，批量操作 -->
                        <span class="mouseEnterCss" @click.stop v-if="(judgeProjectRoleInfo(21)||judgeProjectRoleInfo(23)||judgeProjectRoleInfo(24))&&projectBaseInfo.project_status !== '1'&&projectBaseInfo.project_status !== '2'">
                          <i class="iconfont icon-Rectangle"></i>
                          <div class="delDiv listDelDiv">
                            <p @click.stop="setRename(v,'sublist', v1,item1)" v-if="judgeProjectRoleInfo(21)&&v1.flow_status!='1'"><i class="iconfont icon-module-menu-1"></i>重命名</p>
                            <p @click.stop="batchOperation(item1, ('batchAdd'+k+k1),('offsetHeight'+k),('BottomLiitme1ne'+k),list,v1)" v-if="judgeProjectRoleInfo(24)"><i class="iconfont icon-Rectangle5"></i>批量操作</p>
                            <p @click.stop="delTaskOrTaskList(v, 'sublist', v1,item1)" v-if="judgeProjectRoleInfo(23)&&v1.flow_status!='1'"><i class="iconfont icon-huishouzhan1"></i>删除</p>
                          </div>
                        </span>
                      </div>
                    </span>
                  </div>
                  <div v-if="!item1.id" class="threeItem no-drag" style="text-align:center;background:#fff;box-shadow: 0 0 5px #ccc;" draggable="false" ondragstart="return false;">
                    <span style="color:#ccc;">-- 暂无数据 --</span>
                  </div>
                  <div class="batchOperationSet" v-if="item1.isshowBatch === 1">
                    <div>
                      <div class="batchTop">
                        <span class="endtimeCss"><span>截止时间</span></span>
                        <span class="batchEditor">
                          <el-date-picker v-model="item1.dateTime" type="datetime"  placeholder="选择日期时间" @blur="completeBatch(item1, 'date')" value-format="timestamp"></el-date-picker>
                        </span>
                        <span @click.stop="distributionPerson(item1)"><el-button>分配</el-button></span>
                        <span @click.stop="moveTask(item1)"><el-button>移动</el-button></span>
                      </div>
                      <div>
                        <!-- <el-radio v-model="v1.checked" @change="allChecked('',v1)" label="1">全选</el-radio> -->
                        <el-checkbox v-model="item1.checked" @change="allChecked('',item1)">全选</el-checkbox>
                        <span>已选择<span v-text="item1.checkedCount" style="margin:0 10px;color:#FF7610;">1</span>条数据</span>
                      </div>
                    </div>
                    <div @click.stop="completeBatchNew(item1,'success',list)">完成</div>
                  </div>
                  <draggable v-model="item1.subnodeArr" :options="{
                    animation: 200,
                    group: {name: item1.flow_status==='1'?'':'sublistItem'+index1, pull: true, put: true},
                    sort: true,
                    ghostClass: 'ghost',
                    filter: '.no-drag'
                  }"  @start="hiddleLine(item1.isshowBatch,item1)" @update="updata4($event,item1)" @add="changedata4(item1,('offsetHeight'+k),('BottomLine'+k),list)" @end="empty('',item1.subnodeArr,'','','taskEmpty',list)" class="five" :class="v1.flow_status==='1'||projectBaseInfo.project_status=='1'||projectBaseInfo.project_status=='2'?'no-drag':''" v-if="item1.isshow==1">
                   <!-- <draggable v-model="item1.subnodeArr" :options="{
                    animation: 200,
                    group: {name: item1.flow_status==='1'?'':'sublistItem'+index1, pull: true, put: true},
                    sort: true,
                    ghostClass: 'ghost',
                    filter: '.no-drag'
                  }"  @start="hiddleLine(item1.isshowBatch,item1)" @update="updata4($event,item1)" @add="changedata4(item1,('offsetHeight'+k),('BottomLine'+k),list)" @end="empty('',item1.subnodeArr,'','','taskEmpty',list)" class="five no-drag" v-if="item1.isshow==1"> -->
                  <!-- 任务列表 -->
                    <div v-for="(v3,k3) in item1.subnodeArr" :key="k3" class="fourItme" :class="[v3.batchCheckOut===1?'batchCheckOutActive':'','batchSetStyle' + k3]" @click.stop="clickBatchCheckOut(v3,$event,item1)">
                      <div :style="v3.beanName==='empty'?'text-align:center;line-height:50px;':''" :class="item1.isshowBatch === 1?'subfourItemIn':'subfourItemOut'">
                        <div @click="openDeAllTypesdetalis(v3,item1,v,v1)" v-if="v3.beanName!== 'empty'" style="position:relative;">
                          <!-- 待检验 -->
                          <i class="iconfont icon-CombinedShape" v-if="v3.check_status==='1'&&v3.dataType===2&&v3.passed_status=='0'&&v3.complete_status=='1'"></i>
                          <!-- 已通过 -->
                          <i class="iconfont icon-icon-test6" v-if="v3.check_status==='1'&&v3.dataType===2&&v3.passed_status=='1'&&v3.complete_status=='1'"></i>
                          <!-- 已驳回 -->
                          <i class="iconfont icon-bohui1" v-if="v3.check_status==='1'&&v3.dataType===2&&v3.passed_status=='2'&&v3.complete_status=='0'"></i>
                          <line-card-task :cloumn="v3" :listItem="item1" :projectBaseInfo="projectBaseInfo"></line-card-task>
                        </div>
                        <span v-if="v3.beanName==='empty'" style="color:#ccc;" class="no-drag">-- 暂无数据 --</span>
                      </div>
                      <span v-if="v3.batchCheckOut===1" class="checkOutIcon"><i class="iconfont icon-pc-paper-optfor"></i></span>
                    </div>
                  </draggable>
                </div>
              </draggable>
            </div>
            <!-- 新增单个列表 -->
            <div class="addInput" v-if="v.isAddTaskList===1">
              <div>
                <el-input v-model="v.teaskGroupInput" placeholder="请输入任务列表名称" :autofocus="true"></el-input>
              </div>
              <div>
                <el-button @click="cancelAddTasklist(v,('offsetHeight'+k),('BottomLine'+k),list)">取消</el-button>
                <el-button type="primary" plain @click="addSubmit(v,('offsetHeight'+k),('BottomLine'+k))">确定</el-button>
              </div>
            </div>
          </draggable>
        </div>
      </draggable>
      <div v-if="judgeProjectRoleInfo(16)&&projectBaseInfo.project_status!=='1'&&projectBaseInfo.project_status!=='2'" style="width:1000px;margin:0 auto;">
      <!-- <div style="width:1000px;margin:0 auto;"> -->
        <div class="addNewList">
          <div>
            <span></span>
            <div>
              <!-- <div class="spanCss" @click="addRowData('')"><span>+</span><span>新建任务分组</span></div> -->
              <div class="spanCss" @click="dialogVisible=true;chooseradio='';"><span>+</span><span>新建任务分组</span></div>
            </div>
          </div>
        </div>
      </div>
      <!-- 新建分组列表弹窗 -->
      <add-task-groupandlist :projectId="projectId"></add-task-groupandlist>
      <!-- 移动至弹窗 -->
      <el-dialog title="移动至" :visible.sync="moveTakVisible" width="400px" center id="moveDilog">
        <div>
          <div class="moveBox">
            <el-row>
              <el-col :span="8"><div class="moveTaskCss">任务分组</div></el-col>
              <el-col :span="16">
                <el-select v-model="moveData.moveProject" placeholder="请选择">
                  <el-option
                    v-for="item in moveData.moveProjectList"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value" @click.stop.native="moveChange(item)">
                  </el-option>
                </el-select>
              </el-col>
            </el-row>
          </div>
          <div class="moveBox">
            <el-row>
              <el-col :span="8"><div class="moveTaskCss">任务列表</div></el-col>
              <el-col :span="16">
                <el-select v-model="moveData.moveTask" placeholder="请选择">
                  <el-option
                    v-for="item in moveData.moveTaskList"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value" @click.native="moveChangeSub(item)">
                  </el-option>
                </el-select>
              </el-col>
            </el-row>
          </div>
          <div class="moveBox" v-if="isHaveSubList">
            <el-row>
              <el-col :span="8"><div class="moveTaskCss">子任务列表</div></el-col>
              <el-col :span="16">
                <el-select v-model="moveData.moveSub" placeholder="请选择">
                  <el-option
                    v-for="item in moveData.moveSubList"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
              </el-col>
            </el-row>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="moveTakVisible = false">取 消</el-button>
          <el-button type="primary" @click="completeBatch('', 'move')">确 定</el-button>
        </span>
      </el-dialog>
      <!-- 分配给弹窗 -->
      <el-dialog title="分配给" :visible.sync="distributionVisible" width="350px" center id="distributionDilog">
        <div class="distribtionParentBox">
          <div class="distributionToOther" v-for="(v,k) in distribution" :key="k" @click.stop="chooseProPerson(v)">
            <span>
              <span v-if="!v.employee_pic" v-text="sliceName(v.employee_name)">人员</span>
              <img  v-if="v.employee_pic" :src="v.employee_pic + '&TOKEN=' + token" alt="">
            </span>
            <span v-text="v.employee_name">姓名</span>
            <span class="depPost" v-if="v.post_name">（<span v-text="v.post_name">设计师</span>）</span>
            <!-- 项目角色 -->
            <span class="projectPerson" v-if="v.project_role == '1'">
              <el-tooltip class="item" effect="dark" content="项目负责人" placement="top-start">
                <i class="iconfont icon-jiaosequanxian1"></i>
              </el-tooltip>
            </span>
            <!-- 激活状态 -->
            <span class="personIsactive" v-if="v.is_enable != 1">
              <el-tooltip class="item" effect="dark" content="未激活" placement="top-start">
                <span></span>
              </el-tooltip>
            </span>
            <span class="personIsclick" v-if="v.isactive === 1">
              <i class="iconfont icon-pc-paper-optfor"></i>
            </span>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click.stop="distributionVisible = false">取 消</el-button>
          <el-button type="primary" @click.stop="completeBatch('', 'distribtion')">确 定</el-button>
        </span>
      </el-dialog>
      <!-- 激活原因填写 -->
      <el-dialog :visible.sync="completeActiveStatus" width="400px" id="completeActiveStatus">
        <div class="titleHeader"><span style="color:red;">*</span> <span>激活原因</span></div>
        <div>
          <el-input type="textarea" rows="6" v-model="activationReason"></el-input>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="completeActiveStatus = false">取 消</el-button>
          <el-button type="primary" @click="completeStatusNext">激 活</el-button>
        </span>
      </el-dialog>
      <PreviewTemplate></PreviewTemplate>
      <!-- <AddTask :addNewTaskData="addNewTaskData"></AddTask> -->
      <new-add-task :addNewTaskData="addNewTaskData" :projectOrRelationStatus="true"></new-add-task>
      <el-dialog title="提示" :visible.sync="dialogVisible" width="400px" class="chooseCengjiListTaskPorject">
        <div>
          <p>请选择新建层级</p>
          <div>
            <el-radio v-model="chooseradio" label="2"><i class="iconfont icon-liangceng"></i>两层<span class="colorDislog">（分组+列名）</span></el-radio>
            <el-radio v-model="chooseradio" label="1"><i class="iconfont icon-sanceng"></i>三层<span class="colorDislog">（分组+列名+子任务列表）</span></el-radio>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="addRowDatanew()">确 定</el-button>
        </span>
      </el-dialog>
    </el-main>
  </el-container>
</template>
<script>
import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'
import draggable from 'vuedraggable'
import PreviewTemplate from '@/frontend/project/components/preview-template' // 预览模版
import addTaskGroupandlist from '@/frontend/project/components/add-task-groupandlist' // 新增分组和列表
import AddTask from '@/frontend/project/components/add-task' // 新建任务弹窗
import NewAddTask from '@/frontend/project/components/new-add-task' // 新建任务弹窗（新）
import LineCardTask from '@/frontend/project/components/line-card-task' // 显示长形卡片组件
import { mapState } from 'vuex'
export default {
  name: 'TaskProject',
  components: {draggable, PreviewTemplate, AddTask, addTaskGroupandlist, LineCardTask, NewAddTask},
  props: ['workbenchStatus'],
  data () {
    return {
      isHaveSubList: false, // 判断是否有第三季列表
      showWorkbench: false,
      dialogVisible: false, // 两层三层选择
      chooseradio: '',
      userInfo: {},
      moveActiveDetails: {},
      batchSetingObj: { // 批量操作主节点，子节点
        orgMainnodeId: '',
        orgSubnodeId: ''
      },
      projectBaseInfo: {}, // 项目基本信息
      approvalstatusType: ['待审批', '审批中', '审批通过', '审批驳回', '已撤销', '已转交', '待提交'],
      projectRoleInfoList: '', // 权限信息列表
      activationReason: '', // 激活原因
      isActiveData: {}, // 存放激活数据
      completeActiveStatus: false, // 激活弹窗
      token: JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN,
      activeListData: {}, // 存储单个列表信息用于批量操作
      data: '',
      projectId: '', // 项目id
      addNewListOne: false, // 新建列表显示隐藏
      addNewListData: [{listName: ''}], // 新建列表增加或删除
      distributionVisible: false, // 分配给弹窗
      showUserList: [], // 项目成员
      distribution: [], // 分配人员列表
      dateTime: '',
      layout: [],
      teaskGroupInput: '',
      TaskAuthRoleInfoList: [], // 项目权限
      rolTaskList: [], // 任务角色
      list: [],
      copyList: [],
      isshowAdd: 0,
      isChangeTopPro: false,
      isactiveOraomplateParentVal: {},
      moveTakVisible: false, // 批量操作的移动至
      moveData: {
        moveProject: '',
        moveTask: '',
        moveProjectList: [], // 移动至 任务分组列表
        moveTaskList: [], // 移动至 列名列表
        moveSub: '', // 子任务列表
        moveSubList: [] // 子任务列表
      },
      addNewTaskData: {}, // 新建任务保存数据
      addTaskSaveGroupAndListId: { // 保存分组和列表的id
        groupId: '',
        listId: '',
        subListId: ''
      },
      allTaskSaveSubmit: {},
      fromDetailCrumbs: { // 判断是否是从详情页面跳转过来的
        fromDetailCrumbsStatus: 0,
        status: null
      },
      querywhere: {}
    }
  },
  mounted () {
    this.userInfo = (JSON.parse(sessionStorage.getItem('userInfo'))).employeeInfo
    if (this.workbenchStatus === 'two') {
      this.showWorkbench = true
    }
    this.projectId = parseInt(this.$route.query.projectId)
    this.$bus.on('changeProjectId', (projectId) => {
      this.projectId = projectId
      this.isChangeTopPro = true
      this.getBaseSetting(this.projectId)
      this.getData(projectId)
      this.getProjectRoleInfo({eid: this.userInfo.id, projectId: this.projectId})
    })
    // if (sessionStorage.getItem('storageProjectId')) {
    //   this.projectId = sessionStorage.getItem('storageProjectId')
    // }
    // this.getData(this.projectId)
    this.$bus.on('memoSaveSuccess', (res) => { // 备忘录/任务/审批/自定义   '新建'   保存成功后返回
      if (sessionStorage.getItem('isAddOrRelationTask') !== 'true') {
        let istaskOrMemoOrcuston = sessionStorage.getItem('istaskOrMemoOrcuston')
        let data = JSON.parse(res)
        if (istaskOrMemoOrcuston) {
          if (istaskOrMemoOrcuston === 'approval') {
            this.allTaskSaveSubmit.bean_type = 4
          } else if (istaskOrMemoOrcuston === 'custom') {
            this.allTaskSaveSubmit.bean_type = 3
          } else if (istaskOrMemoOrcuston === 'memo') {
            this.allTaskSaveSubmit.bean_type = 1
          } else if (istaskOrMemoOrcuston === 'task') {
            this.allTaskSaveSubmit.bean_type = 2
          }
        }
        this.allTaskSaveSubmit.bean = data.bean
        this.allTaskSaveSubmit.moduleName = data.moduleName
        this.allTaskSaveSubmit.moduleId = data.moduleId
        this.allTaskSaveSubmit.dataId = data.dataId
        this.allTaskSaveSubmit.checkStatus = data.checkStatus
        this.allTaskSaveSubmit.checkMember = data.checkMember
        this.allTaskSaveSubmit.associatesStatus = data.associatesStatus
        this.allTaskSaveSubmit.joinStatus = data.joinStatus
        this.allTaskSaveSubmit.taskName = data.taskName
        this.allTaskSaveSubmit.endTime = data.endTime
        this.allTaskSaveSubmit.startTime = data.startTime
        this.saveTaskLast()
      }
    })
    this.$bus.$on('successAddTaskList', (res) => {
      if (!res) {
        this.getQueryMainNode()
      } else {
        this.getSubData(res)
        // this.updateSubNodeDataType(res)
      }
    })
    this.$bus.on('quoteSaveSuccess', (res) => { // 备忘录/任务/审批/自定义   '引用'   保存成功后返回Y
      if (sessionStorage.getItem('isAddOrRelationTask') !== 'true') {
        let data = JSON.parse(sessionStorage.getItem('taskGroupAndList'))
        let sendata = {}
        console.log(this.list)
        this.list.forEach((v, k) => {
          if (v.id === data.groupId) {
            v.subnodeArr.forEach((v1, k1) => {
              if (data.subListId && v1.id === data.listId) {
                v1.subnodeArr.map((v2, k2) => {
                  if (v2.id === data.subListId) {
                    sendata = v2
                  }
                })
              } else {
                if (v1.id === data.listId) {
                  sendata = v1
                }
              }
            })
          }
        })
        this.getTaskList(sendata, this.list)
      }
    })
    this.$bus.on('delCompleteUpdata', (data, data1) => { // 详情中，删除任务或者移动任务后，重新刷新层级列表
      if (data1) {
        let arr = []
        let index = ''
        this.list.forEach((v, k) => {
          if (v.id === data1.parentId) {
            v.subnodeArr.forEach((v1, k1) => {
              if (v1.id === data1.id) {
                v1.subnodeArr.forEach((v2, k2) => {
                  if (v2.taskInfoId === data1.taskid) {
                    arr = v1.subnodeArr
                    index = k2
                  }
                })
                if (v1.subnodeArr && v1.subnodeArr.length === 0) {
                  v1.subnodeArr = [{beanName: 'empty'}]
                }
              }
            })
          }
        })
        arr.splice(index, 1)
        this.getTaskList(data, this.list, 1)
      } else {
        this.getTaskList(data, this.list, 1)
      }
    })
    this.$bus.on('fromDetailCrumbsTwo', (res) => { // 从详情跳转到层级界面，并打开相应的分组或者列表
      let data = JSON.parse(res)
      this.projectId = parseInt(data.projectId)
      // let data1 = JSON.parse(sessionStorage.getItem('navCrumbs'))
      this.fromDetailCrumbs.fromDetailCrumbsStatus = 1
      this.fromDetailCrumbs.status = data.status
      this.getBaseSetting(this.projectId)
      this.getData(this.projectId)
    })
    this.$bus.on('screenProjectTask', (res) => { // 点击筛选确定按钮
      // let data = JSON.parse(res)
      this.querywhere = JSON.parse(res)
      // this.getTaskList()
      // if (data.dateFormat || data.queryType || data.sortField || JSON.stringify(data.queryWhere)) {
      //   this.querywhere = true
      //   for (let i in data.queryWhere) {
      //     if (!data.queryWhere[i]) {
      //       this.querywhere = false
      //     }
      //   }
      // } else {
      //   this.querywhere = false
      // }
      // this.getScreenData(data)
    })
    let isFromOtherPage = JSON.parse(sessionStorage.getItem('isFromOtherPage'))
    if (isFromOtherPage && (isFromOtherPage.status === 0 || isFromOtherPage.status === 1 || isFromOtherPage.status === 2 || isFromOtherPage.status === 3)) {
      this.fromDetailCrumbs.fromDetailCrumbsStatus = 1
      this.fromDetailCrumbs.status = isFromOtherPage.status
      this.projectId = parseInt(isFromOtherPage.projectId)
      this.getData(this.projectId)
      this.getBaseSetting(this.projectId)
      sessionStorage.removeItem('isFromOtherPage')
    } else {
      this.getData(this.projectId)
      this.getBaseSetting(this.projectId)
    }
    this.getProjectRoleInfo({eid: this.userInfo.id, projectId: this.projectId})
    this.$bus.on('screenCompleteList', (res) => {
      let data = JSON.parse(res)
      console.log(data)
    })
    this.$bus.$on('listComplateActiveStatus', (v, parentVal) => {
      this.completeStatus(v, parentVal)
    })
  },
  methods: {
    getBaseSetting (id) { // 获取项目基本设置信息
      HTTPServer.projectQueryById({id: id}, 'Loading').then((res) => {
        if (res.project_labels_content) {
          res.projectLabelsContent = JSON.parse(res.project_labels_content)
        }
        this.projectBaseInfo = res
      })
      this.getTaskAuth(id)
      this.getProjectLayout('project_custom_' + this.projectId)
    },
    getTaskAuth (id) { // 获取任务权限
      HTTPServer.queryTaskAuthList({id: id}, 'Loading').then((res) => {
        this.TaskAuthRoleInfoList = res
      })
    },
    getRolAuth (v, parentVal) { // 获取用户任务角色
      let senddata = {
        id: this.projectId,
        taskId: v.taskInfoId,
        typeStatus: 1,
        all: 0
      }
      HTTPServer.queryCollaboratorList(senddata, 'Loading').then((res) => { // 获取用户任务角色
        this.rolTaskList = res.dataList
        if (this.judgeTaskAuth(2)) {
          this.activationReason = ''
          let senddata = {
            id: v.taskInfoId,
            completeStatus: 1 // 0未已完成状态，1已完成状态
          }
          if (v.complete_status === 1) {
            this.isActiveData = v
            if (this.projectBaseInfo.project_complete_status === '1') {
              this.completeActiveStatus = true
            } else {
              this.completeStatusNext(true)
            }
          } else {
            this.$confirm('是否确定完成此任务?', '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              HTTPServer.updateTaskStatus(senddata, 'Loading').then((res) => {
                this.$message({
                  type: 'success',
                  message: '完成任务!'
                })
                this.getTaskList(parentVal, this.list)
              })
            }).catch(() => { console.log('') })
          }
        } else {
          this.$message({
            message: '无权进行此操作！',
            type: 'warning'
          })
        }
      })
    },
    getProjectLayout (bean) { // 获取任务自定义布局
      HTTPServer.getAllLayout({'bean': bean}, 'loading').then((res) => {
        if (JSON.stringify(res) !== '{}') {
          this.layout = res.enableLayout.rows
        }
      })
    },
    getData (id, str, str1) { // 获取主节点或获取全部节点数据
      let data1 = JSON.parse(sessionStorage.getItem('navCrumbs'))
      let _this = this
      let itemData = {}
      HTTPServer.queryAllNode({'id': id}, 'Loading').then((res) => {
        res.dataList.forEach((v, k) => {
          v.isshow = 0
          v.isAddTaskList = 0
          if (this.fromDetailCrumbs.fromDetailCrumbsStatus === 1 && this.fromDetailCrumbs.status !== 0) {
            if (v.id === data1.taskGroup.id) {
              v.isshow = 1
            }
          }
          if (v.subnodeArr) {
            if (v.subnodeArr.length === 0) {
              v.subnodeArr.push({id: '', name: ''})
            } else {
              v.subnodeArr.forEach((v1, k1) => {
                if (v1.children_data_type === '1') {
                  v1.isshow = 0
                  if (v1.subnodeArr && v1.subnodeArr.length > 0) {
                    v1.subnodeArr.map((v2, k2) => {
                      v2.isshow = 0
                      v2.isshowBatch = 0
                      v2.checkedCount = 0 // 批量操作选择个数
                      v2.checked = false // 批量操作全选
                      v2.dateTime = '' // 批量操作时间
                      if (this.fromDetailCrumbs.fromDetailCrumbsStatus === 1 && this.fromDetailCrumbs.status === 3) {
                        if (v.id === data1.taskGroup.id && v1.id === data1.taskList.id && v2.id === data1.subTaskList.id) {
                          v1.isshow = 1
                          v2.isshow = 1
                          itemData = v2
                        }
                      }
                      if (this.fromDetailCrumbs.fromDetailCrumbsStatus === 1 && this.fromDetailCrumbs.status === 2) {
                        if (v.id === data1.taskGroup.id && v1.id === data1.taskList.id) {
                          v1.isshow = 1
                        }
                      }
                      if (v2.subnodeArr) {
                        if (v2.subnodeArr.length === 0) {
                          v2.subnodeArr.push({beanName: 'empty'})
                        }
                      } else {
                        v2.subnodeArr = []
                        v2.subnodeArr.push({beanName: 'empty'})
                      }
                    })
                  } else {
                    v1.subnodeArr = [{id: '', name: ''}]
                  }
                } else {
                  v1.isshow = 0
                  v1.isshowBatch = 0
                  v1.checkedCount = 0 // 批量操作选择个数
                  v1.checked = false // 批量操作全选
                  v1.dateTime = '' // 批量操作时间
                  // if (this.fromDetailCrumbs.fromDetailCrumbsStatus === 1 && this.fromDetailCrumbs.status !== 1 && this.fromDetailCrumbs.status !== 0) {
                  if (this.fromDetailCrumbs.fromDetailCrumbsStatus === 1 && this.fromDetailCrumbs.status === 2) {
                    if (v.id === data1.taskGroup.id && v1.id === data1.taskList.id) {
                      v1.isshow = 1
                      itemData = v1
                    }
                  }
                  if (v1.subnodeArr) {
                    if (v1.subnodeArr.length === 0) {
                      v1.subnodeArr.push({beanName: 'empty'})
                    }
                  } else {
                    v1.subnodeArr = []
                    v1.subnodeArr.push({beanName: 'empty'})
                  }
                }
              })
            }
          } else {
            v.subnodeArr = []
            v.subnodeArr.push({id: '', name: ''})
          }
        })
        if (!this.isChangeTopPro) {
          if (this.copyList && this.copyList.length > 0) {
            this.copyList.forEach((v, k) => {
              res.dataList.forEach((v1, k1) => {
                if (v1.id === v.id) {
                  v1.isshow = v.isshow
                }
              })
            })
          }
        } else {
          this.editorLine()
        }
        this.list = res.dataList
        console.log(this.list)
        if (this.fromDetailCrumbs.fromDetailCrumbsStatus === 1) {
          this.$nextTick(function () {
            _this.eduitorHeight('', '', _this.list)
          })
        }
        if (this.fromDetailCrumbs.fromDetailCrumbsStatus === 1) { // 从详情页面跳转过来打开相应的分组和列表
          // let itemData = {}
          // this.list.forEach((v, k) => {
          //   if (v.id === data1.taskGroup.id) {
          //     if (this.fromDetailCrumbs.status === 2) {
          //       v.subnodeArr.forEach((v1, k1) => {
          //         if (v1.id === data1.taskList.id) {
          //           if (data1.subTaskList.id) {
          //             v1.subnodeArr.map((v4, k4) => {
          //               if (v4.id === data1.subTaskList.id) {
          //                 itemData = v4
          //               }
          //             })
          //           } else {
          //             itemData = v1
          //           }
          //         }
          //       })
          //     }
          //   }
          // })
          if (this.fromDetailCrumbs.status === 2 || this.fromDetailCrumbs.status === 3) {
            if (this.fromDetailCrumbs.status === 2 && JSON.stringify(data1.subTaskList) !== '{}') {
              return false
            }
            this.getTaskList(itemData, this.list)
          }
          this.fromDetailCrumbs.fromDetailCrumbsStatus = 0
        } else {
          this.$nextTick(function () {
            _this.eduitorHeight('', '', _this.list)
          })
        }
      })
      this.isChangeTopPro = false
    },
    getSubData (value) { // 获取子节点
      HTTPServer.querySubNode({'id': value.id, node_level: 3}, 'Loading').then((res) => {
        this.list.forEach((val, key) => {
          if (val.id === value.main_id) {
            val.subnodeArr.map((v1, k1) => {
              if (v1.id === value.id) {
                v1.isshow = 1
                res.dataList.map((v2, k2) => {
                  v2.isshow = 0
                  v2.isshowBatch = 0
                  v2.checkedCount = 0 // 批量操作选择个数
                  v2.checked = false // 批量操作全选
                  v2.dateTime = '' // 批量操作时间
                  if (v2.subnodeArr) {
                    if (v2.subnodeArr.length === 0) {
                      v2.subnodeArr.push({beanName: 'empty'})
                    }
                  } else {
                    v2.subnodeArr = []
                    v2.subnodeArr.push({beanName: 'empty'})
                  }
                })
                v1.subnodeArr = res.dataList
              }
            })
            // if (res.dataList && res.dataList.length > 0) {
            //   res.dataList.forEach((v1, k1) => {
            //     val.subnodeArr.forEach((v2, k2) => {
            //       if (v2.id === v1.id) {
            //         v1.isshow = v2.isshow
            //         v1.subnodeArr = v2.subnodeArr
            //       }
            //     })
            //   })
            //   val.subnodeArr = res.dataList
            // } else {
            //   val.subnodeArr = [{name: '', id: ''}]
            // }
          }
        })
        this.$nextTick(function () {
          this.eduitorHeight('', '', this.list)
        })
      })
    },
    getProjectRoleInfo (data) { // 获取项目权限
      let _this = this
      HTTPServer.queryManagementRoleInfo(data, 'Loading').then((res) => {
        // 缓存项目的权限
        if (res && res.priviledge) {
          sessionStorage.setItem('projectRoleInfo', JSON.stringify(res.priviledge.priviledge_ids))
          this.projectRoleInfoList = res.priviledge.priviledge_ids
          this.$nextTick(function () {
            if (!_this.judgeProjectRoleInfo(18)) {
              let eles = document.getElementsByClassName('main-project-task-wrap')
              eles[0].setAttribute('ondragstart', 'return false;')
              eles[0].setAttribute('draggable', 'false')
            }
          })
        }
      })
    },
    judgeProjectRoleInfo (id) { // 判断是否有权限
      let arr = this.projectRoleInfoList.split(',')
      let status = false
      arr.forEach((v, k) => {
        if (parseInt(v) === id) {
          status = true
        }
      })
      return status
      // return this.projectRoleInfoList.includes(id)
    },
    getScreenData (data) { // 获取筛选后的数据
      HTTPServer.queryProjectTaskListByCondition(data, 'Loading').then((res) => {
        let list = JSON.parse(JSON.stringify(this.list))
        list.forEach((val, key) => {
          if (val.subnodeArr && val.subnodeArr.length > 0) {
            val.subnodeArr.forEach((val1, key1) => {
              val1.subnodeArr = [{beanName: 'empty'}]
            })
          }
        })
        res.forEach((v, k) => {
          list.forEach((v1, k1) => {
            if (parseInt(v.groupId) === parseInt(v1.id)) {
              v1.subnodeArr.forEach((v2, k2) => {
                if (parseInt(v.id) === parseInt(v2.id) && v.name === v2.name) {
                  v2.subnodeArr = v.dataList
                }
              })
            }
          })
        })
        console.log(list)
        this.list = list
        this.eduitorHeight('', '', list)
      })
    },
    addRowData (v) { // 新增列表或引用模版
      // this.addNewListInput = ''
      // this.addNewListData = [{listName: ''}]
      // this.addNewListOne = true
      this.$bus.$emit('creatTaskAndQuoteTemplate', v)
    },
    addTaskList (v, str, str1, e, list) { // 新建列表
      v.teaskGroupInput = ''
      if (v.isshow === 0) {
        v.isshow = 1
      }
      v.isAddTaskList = 1
      this.eduitorHeight(str, str1, list)
    },
    cancelAddTasklist (v, str, str1, list) { // 取消新增任务列表
      v.isAddTaskList = 0
      this.eduitorHeight(str, str1, list)
    },
    judgeTaskAuth (number) { // 判断任务权限  // 0创建人 1执行人 2协作人
      let status = false
      this.rolTaskList.forEach((v, k) => {
        this.TaskAuthRoleInfoList.forEach((v1, k1) => {
          if (v.project_task_role === v1.role_type) {
            for (let i in v1) {
              if (i.indexOf('auth') !== -1) {
                if (v1['auth_' + number] === '1') {
                  status = true
                }
              }
            }
          }
        })
      })
      return status
    },
    completeStatus (v, parentVal) { // 完成或者打开激活弹窗
      this.getBaseSetting(this.projectId)
      setTimeout(() => {
        if (this.projectBaseInfo.project_status !== '1' && this.projectBaseInfo.project_status !== '2') {
          this.getRolAuth(v, parentVal)
          this.isactiveOraomplateParentVal = parentVal
        }
      }, 200)
    },
    completeStatusNext (status) { // 确认激活
      if (!status) {
        if (!this.activationReason) {
          this.$message({
            message: '请填写激活原因！',
            type: 'warning'
          })
          return false
        }
      }
      let senddata = {
        id: this.isActiveData.taskInfoId,
        completeStatus: 0 // 0未已完成状态，1已完成状态
      }
      if (!status) {
        senddata.remark = this.activationReason
        HTTPServer.updateTaskStatus(senddata, 'Loading').then((res) => {
          this.getTaskList(this.isactiveOraomplateParentVal, this.list)
        })
      } else {
        this.$confirm('确定激活任务吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          HTTPServer.updateTaskStatus(senddata, 'Loading').then((res) => {
            this.getTaskList(this.isactiveOraomplateParentVal, this.list)
          })
        }).catch(() => {})
      }
      this.completeActiveStatus = false
    },
    addSubmit (v, str, str1) { // 单个列表的新增
      if (!v.teaskGroupInput || v.teaskGroupInput.length > 25) {
        this.$message({message: '任务分组名称必需在25字以内，且必填', type: 'warning'})
        return false
      }
      this.copyList = JSON.parse(JSON.stringify(this.list))
      // let isHave = 0
      // v.subnodeArr.forEach((value, key) => {
      //   if (v.teaskGroupInput === value.name) {
      //     isHave = 1
      //   }
      // })
      // if (isHave === 1) {
      //   this.$message({message: '分组内的列表名称不能重复', type: 'warning'})
      //   return false
      // }
      let senddata = {
        projectId: parseInt(this.projectId),
        nodeId: v.id,
        node_level: 2,
        children_data_type: '2',
        subnodeArr: [{name: v.teaskGroupInput}]
      }
      if (v.subnodeArr && v.subnodeArr.length > 0 && v.subnodeArr[0].children_data_type === '1') {
        senddata.node_level = 2
        senddata.children_data_type = '1'
      }
      HTTPServer.saveSubNode(senddata, 'Loading').then((res) => {
        this.$message({message: '新增成功！', type: 'success'})
        this.addNewListOne = false
        this.getData(this.projectId)
        // v.isAddTaskList = 0
      })
    },
    setRename (v, str, subV, newsubv) { // 重命名或删除任务分组  项目负责人才有权限
      this.copyList = JSON.parse(JSON.stringify(this.list))
      const h = this.$createElement
      let senddata = {
        projectId: this.projectId,
        nodeId: v.id,
        name: ''
      }
      let newName = ''
      if (str === 'task') {
        newName = v.name
      } else if (str === 'list') {
        senddata.subnodeId = subV.id
        newName = subV.name
      } else {
        senddata.subnodeId = newsubv.id
        newName = newsubv.name
      }
      let elm = document.getElementById('editName')
      let elm1 = document.getElementById('deleteId')
      if (elm) { elm.value = newName }
      if (elm1) { elm1.value = newName }
      this.$msgbox({
        title: '重命名',
        message: h('p', null, [
          h('input', {style: 'height: 40px;width:100%;', attrs: {value: newName, id: 'editName'}}, newName)
        ]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        beforeClose: (action, instance, done) => {
          if (action === 'confirm') {
            if (!document.getElementById('editName').value) {
              this.$message({
                message: '名称不能为空！',
                type: 'warning'
              })
              return false
            }
            senddata.name = document.getElementById('editName').value
            // document.getElementById('editName').value = ''
            if (str === 'task') {
              // let isHave = 0
              // this.list.forEach((v, k) => {
              //   if (v.name === senddata.name) {
              //     isHave = 1
              //   }
              // })
              // if (isHave === 1) {
              //   this.$message({ message: '分组名称不能重复', type: 'warning' })
              //   return false
              // }
              done()
            } else {
              // let isHave = 0
              // v.subnodeArr.forEach((v1, k1) => {
              //   if (v1.name === senddata.name) {
              //     isHave = 1
              //   }
              // })
              // if (isHave === 1) {
              //   this.$message({ message: '列表名称不能重复', type: 'warning' })
              //   return false
              // }
              done()
            }
          } else {
            done()
          }
        }
      }).then(action => {
        if (str === 'task') {
          HTTPServer.editMainNode(senddata, 'Loading').then((res) => {
            v.name = senddata.name
            this.$message({message: '重命名成功！', type: 'success'})
            // this.getData(this.projectId)
          })
        } else {
          HTTPServer.editSubNode(senddata, 'Loading').then((res) => {
            this.$message({message: '重命名成功！', type: 'success'})
            if (str === 'list') {
              subV.name = senddata.name
            } else {
              newsubv.name = senddata.name
            }
            // this.getData(this.projectId)
          })
        }
      })
    },
    addTaskQuote (v, parentVal, subVal) { // 新增项目或引用项目模版
      sessionStorage.setItem('newlyIncreasedTask', 'add')
      this.addTaskSaveGroupAndListId.groupId = parentVal.id
      this.addTaskSaveGroupAndListId.listId = v.id
      if (subVal) {
        this.addTaskSaveGroupAndListId.subListId = subVal.id
        this.addTaskSaveGroupAndListId.subListData = subVal
      } else {
        this.addTaskSaveGroupAndListId.subListId = ''
      }
      sessionStorage.setItem('taskGroupAndList', JSON.stringify(this.addTaskSaveGroupAndListId))
      this.$bus.$emit('addTaskOpen', JSON.stringify({status: 'open', projectIdNew: this.projectId}))
      sessionStorage.setItem('isAddOrRelationTask', 'false')
      sessionStorage.setItem('isSubTaskstatus', 'false')
    },
    saveTaskLast () { // 新建任务最终的保存
      console.log(this.addNewTaskData)
      let senddata = {
        'projectId': parseInt(this.projectId),
        'dataId': this.allTaskSaveSubmit.dataId, // 数据id
        'subnodeId': this.addTaskSaveGroupAndListId.listId, // 子节点ID
        'bean': this.addNewTaskData.bean, // 模块的bean
        'bean_type': this.allTaskSaveSubmit.bean_type ? this.allTaskSaveSubmit.bean_type : ''
      }
      let judagListOrSubList = JSON.parse(sessionStorage.getItem('taskGroupAndList'))
      if (judagListOrSubList.subListId) {
        senddata.subnodeId = this.addTaskSaveGroupAndListId.subListId
      }
      //  else {
      //   HTTPServer.updateSubNodeDataType({nodeId: this.addTaskSaveGroupAndListId.listId, children_data_type: 2}, 'Loading').then((res) => {
      //     this.list.map((item1, index1) => {
      //       if (item1.subnodeArr && item1.subnodeArr.length > 0) {
      //         item1.subnodeArr.map((item2, index2) => {
      //           if (this.addTaskSaveGroupAndListId.listId === item2.id) {
      //             item2.children_data_type = '2'
      //           }
      //         })
      //       }
      //     })
      //   })
      // }
      // 任务新建
      if (this.addNewTaskData.bean === 'project_custom') {
        senddata.bean = 'project_custom_' + this.projectId
        senddata.checkStatus = this.allTaskSaveSubmit.checkStatus
        senddata.checkMember = this.allTaskSaveSubmit.checkMember
        senddata.joinStatus = this.allTaskSaveSubmit.joinStatus
        senddata.associatesStatus = this.allTaskSaveSubmit.associatesStatus
        senddata.taskName = this.allTaskSaveSubmit.taskName
        senddata.endTime = this.allTaskSaveSubmit.endTime
        senddata.startTime = this.allTaskSaveSubmit.startTime
        if (sessionStorage.getItem('taskNameAddAndEditor')) {
          let obj = JSON.parse(sessionStorage.getItem('taskNameAddAndEditor'))
          senddata.taskName = obj.taskName
          senddata.endTime = obj.endTime
          senddata.executorId = obj.executorId
        }
      }
      // 审批或者自定义
      if (this.addNewTaskData.bean === 'custom' || this.addNewTaskData.bean === 'approval') {
        senddata.bean = this.allTaskSaveSubmit.bean
        if (this.addNewTaskData.bean === 'custom') {
          senddata.moduleName = this.allTaskSaveSubmit.moduleName
          senddata.moduleId = this.allTaskSaveSubmit.moduleId
        }
      }
      HTTPServer.saveTaskWeb(senddata, 'Loading').then((res) => { // 新增任务保存
        let data = JSON.parse(sessionStorage.getItem('taskGroupAndList'))
        let sendata = {}
        this.list.forEach((v, k) => {
          if (v.id === data.groupId) {
            v.subnodeArr.forEach((v1, k1) => {
              if (v1.id === data.listId) {
                if (data.subListId) {
                  v1.subnodeArr.map((v2, k2) => {
                    if (v2.id === data.subListId) {
                      sendata = v2
                    }
                  })
                } else {
                  sendata = v1
                }
              }
            })
          }
        })
        if (judagListOrSubList.subListId) {
          this.getTaskList(sendata)
        } else {
          this.getTaskList(sendata, this.list)
        }
      })
    },
    delTaskOrTaskList (v, str, subV, newsubv) { // 删除列，和批量操作 项目负责人才有权限
      this.copyList = JSON.parse(JSON.stringify(this.list))
      let senddata = {
        projectId: this.projectId,
        nodeId: v.id
      }
      const h = this.$createElement
      let html1 = ''
      let html2 = ''
      let html3 = ''
      if (str === 'task') {
        html3 = '删除分组'
        html1 = '确定要删除分组“' + v.name + '”吗？删除后该分组下的所有列表和任务将同时被删除。'
        html2 = '请输入要删除的任务分组名称'
      } else if (str === 'list') {
        senddata.subnodeId = subV.id
        html1 = '确定要删除列表“' + subV.name + '”吗？删除后该列表下的所有信息将同时被删除。'
        html2 = '请输入要删除的列表名称'
        html3 = '删除列表'
      } else {
        senddata.subnodeId = newsubv.id
        html1 = '确定要删除子任务列表“' + newsubv.name + '”吗？删除后该列表下的所有信息将同时被删除。'
        html2 = '请输入要删除的子任务列表名称'
        html3 = '删除子任务列表'
      }
      let elm = document.getElementById('deleteId')
      let elm1 = document.getElementById('editName')
      if (elm) { elm.value = '' }
      if (elm1) { elm1.value = '' }
      this.$msgbox({
        title: html3,
        message: h('p', null, [
          h('p', null, html1),
          h('p', {style: 'margin:20px 0 10px 0;'}, html2),
          h('input', {style: 'height: 40px;width:100%;', attrs: {id: 'deleteId'}}, '请输入')
        ]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        beforeClose: (action, instance, done) => {
          if (action === 'confirm') {
            setTimeout(() => {
              let val = document.getElementById('deleteId').value
              if (str === 'task') {
                if (val !== v.name) {
                  this.$message({message: '输入的任务名称不一致！', type: 'warning'})
                  return false
                }
                document.getElementById('deleteId').value = ''
                done()
              } else if (str === 'list') {
                if (val !== subV.name) {
                  this.$message({message: '输入的列表名称不一致！', type: 'warning'})
                  return false
                }
                document.getElementById('deleteId').value = ''
                done()
              } else {
                if (val !== newsubv.name) {
                  this.$message({message: '输入的列表名称不一致！', type: 'warning'})
                  return false
                }
                document.getElementById('deleteId').value = ''
                done()
              }
            }, 100)
          } else {
            done()
          }
        }
      }).then(action => {
        if (str === 'task') {
          HTTPServer.deleteMainNode(senddata, 'Loading').then((res) => {
            this.$message({message: '删除成功！', type: 'success'})
            this.getQueryMainNode()
            // this.getData(this.projectId)
          })
        } else {
          HTTPServer.deleteSubNode(senddata, 'Loading').then((res) => {
            this.$message({message: '删除成功！', type: 'success'})
            if (str === 'list') {
              // this.getSubData(v)
              this.getData(this.projectId)
            } else {
              this.getquerySubNode(subV)
            }
            // this.getData(this.projectId)
          })
        }
      })
    },
    getQueryMainNode () { // 获取项目主节点
      HTTPServer.queryMainNode({id: this.projectId}, 'Loading').then((res) => {
        let arr = []
        let obj = {}
        res.dataList.forEach((v, k) => {
          this.list.forEach((v1, k1) => {
            if (v.id === v1.id) {
              arr.push(v1)
            }
          })
        })
        if (res.dataList.length > this.list.length) {
          res.dataList.forEach((v3, k3) => {
            arr.forEach((v4, k4) => {
              if (v3.id !== v4.id) {
                obj = v3
                return true
              }
            })
          })
          HTTPServer.querySubNode({'id': obj.id}, 'Loading').then((res) => {
            if (res.dataList && res.dataList.length > 0) {
              res.dataList.forEach((val, key) => {
                val.isshow = 0
                val.isshowBatch = 0
                val.checkedCount = 0 // 批量操作选择个数
                val.checked = false // 批量操作全选
                val.dateTime = '' // 批量操作事件
                if (val.subnodeArr) {
                  if (val.subnodeArr.length === 0) {
                    val.subnodeArr.push({beanName: 'empty'})
                  }
                } else {
                  val.subnodeArr = []
                  val.subnodeArr.push({beanName: 'empty'})
                }
              })
              obj.subnodeArr = res.dataList
            } else {
              obj.subnodeArr = [{id: '', name: ''}]
            }
            obj.isshow = 0
            obj.isshowBatch = 0
            obj.isAddTaskList = 0
            // arr = arr.concat({id: obj.id, name: obj.name, flow_status: obj.flow_status, isshow: 0, isshowBatch: 0, isAddTaskList: 0, subnodeArr: obj.subnodeArr})
            arr = arr.concat(obj)
            this.list = arr
            this.eduitorHeight('', '', this.list)
          })
        }
        this.list = arr
        this.eduitorHeight('', '', this.list)
      })
    },
    batchOperation (v, id, str, str1, list, parentV) { // 批量操作显示或隐藏
      this.batchSetingObj.orgSubnodeId = v.id
      this.batchSetingObj.orgMainnodeId = parentV.id
      let eles = document.getElementsByClassName('fourItme')
      for (let i = 0, index = eles.length; i < index; i++) {
        eles[i].setAttribute('ondragstart', 'return false;')
        eles[i].setAttribute('draggable', 'false')
      }
      // let ele = document.getElementById(id)
      let status = 0
      // let index = 0
      v.subnodeArr.forEach((v1, k1) => {
        v1.batchCheckOut = 0
      })
      if (v.subnodeArr && v.subnodeArr.length === 1) {
        v.subnodeArr.forEach((val, key) => {
          if (!val.id) {
            status = 1
          }
        })
      }
      if (v.isshow === 1 && status !== 1) {
        v.isshowBatch = 1
      }
      this.eduitorHeight(str, str1, list)
    },
    completeBatchNew (v, list) { // 批量操作 完成
      let eles = document.getElementsByClassName('fourItme')
      for (let i = 0, index = eles.length; i < index; i++) {
        eles[i].removeAttribute('ondragstart')
        eles[i].setAttribute('draggable', 'true')
      }
      v.subnodeArr.forEach((v1, k1) => {
        v1.batchCheckOut = 0
      })
      v.checked = false
      v.isshowBatch = 0
      this.eduitorHeight('', '', list)
    },
    completeBatch (value, status) { // 批量操作保存
      if (status === 'distribtion') {
        let issave = 0
        this.distribution.forEach((v, k) => {
          if (v.isactive === 1) {
            issave = 1
          }
        })
        if (issave === 0) {
          this.$message({
            message: '请选择分配人！',
            type: 'warning'
          })
          return false
        }
      }
      let v = value || this.activeListData
      let taskIds = []
      let senddata = {
        type: 0, // 0截止时间 1分配  2移动
        // subnodeId: v.id, // 子节点ID
        taskIds: '', // 任务ID逗号分隔
        endTime: '', // 截止时间
        executorId: '', // 任务执行人ID
        toMainodeId: '', // 目标主节点ID
        toSubnodeId: '', // 目标子节点ID
        orgSubnodeId: this.batchSetingObj.orgSubnodeId, // 原子节点
        orgMainnodeId: this.batchSetingObj.orgMainnodeId // 原主节点
      }
      v.subnodeArr.forEach((v1, k1) => {
        if (v1.batchCheckOut === 1) {
          taskIds.push(v1.taskInfoId)
        }
        v1.batchCheckOut = 0
      })
      if (taskIds.length === 0) {
        this.$message({
          message: '请最少选择一个任务！',
          type: 'warning'
        })
        return false
      }
      senddata.taskIds = taskIds.join(',')
      switch (status) {
        case 'date': // 截止时间
          senddata.type = 0
          senddata.endTime = v.dateTime
          this.activeListData = value
          break
        case 'distribtion': // 分配
          this.distributionVisible = false
          senddata.type = 1
          this.distribution.forEach((v1, k1) => {
            if (v1.isactive === 1) {
              senddata.executorId = v1.employee_id
            }
          })
          break
        case 'move':
          this.moveTakVisible = false
          senddata.type = 2 // 移动
          senddata.toMainodeId = this.moveData.moveProject
          senddata.toSubnodeId = this.moveData.moveTask
          if (this.isHaveSubList) {
            senddata.toMainodeId = this.moveData.moveTask
            senddata.toSubnodeId = this.moveData.moveSub
          }
          break
      }
      v.checked = false
      v.isshowBatch = 0
      this.batchSetSave(senddata)
    },
    batchSetSave (senddata) { // 批量操作的单个保存
      HTTPServer.batchTaskWeb(senddata, 'Loading').then((res) => {
        this.$message({message: '操作成功！', type: 'success'})
        this.getTaskList(this.activeListData, this.list)
      })
    },
    chooseProPerson (v) { // 点击选择分配人员
      v.isactive = 1
      this.distribution.forEach((val, key) => {
        if (v.id !== val.id) {
          val.isactive = 0
        }
      })
    },
    moveTask (v) { // 点击打开移动至的弹窗
      let taskLists = []
      v.subnodeArr.forEach((val, key) => {
        if (val.batchCheckOut === 1) {
          taskLists.push(val)
        }
      })
      if (taskLists.length === 0) {
        this.$message({
          message: '请最少选择一个任务！',
          type: 'warning'
        })
        return false
      }
      this.activeListData = v
      HTTPServer.queryAllNode({'id': this.projectId}, 'Loading').then((res) => {
        let arr = []
        let arr1 = []
        // let arr2 = []
        res.dataList.forEach((val, key) => {
          arr.push({value: val.id, label: val.name, list: val.subnodeArr})
          if (key === 0) {
            if (val.subnodeArr && val.subnodeArr.length > 0) {
              val.subnodeArr.forEach((value, k) => {
                arr1.push({value: value.id, label: value.name, list: value.subnodeArr, children_data_type: value.children_data_type})
                if (value.subnodeArr && value.subnodeArr.length > 0 && value.children_data_type === '1' && k === 0) {
                  this.moveChangeSub(arr1[0])
                }
              })
            }
          }
        })
        this.$set(this.moveData, 'moveProjectList', arr)
        this.moveData.moveProject = arr[0].value
        this.moveData.moveTaskList = arr1
        this.moveData.moveTask = arr1[0].value
      })
      this.moveTakVisible = true
    },
    moveChange (v) { // 移动至的分组改变
      let arr = []
      v.list.forEach((val, key) => {
        arr.push({value: val.id, label: val.name, list: val.subnodeArr, children_data_type: val.children_data_type})
      })
      this.moveData.moveTaskList = arr
      this.moveData.moveTask = arr[0].value
      this.moveChangeSub(arr[0])
    },
    moveChangeSub (v) { // 移动至的列表改变
      if (v.children_data_type !== '1') {
        this.isHaveSubList = false
        return false
      } else {
        this.isHaveSubList = true
      }
      let arr = []
      v.list.map((v1, k1) => {
        arr.push({value: v1.id, label: v1.name})
      })
      this.moveData.moveSubList = arr
      this.moveData.moveSub = arr[0].value
      console.log(this.moveData.moveTask)
    },
    notRepeat (list, str) {
      let mapArr = list.map(item => item[str])
      let setArr = new Set(mapArr)
      return setArr.size < mapArr.length
    },
    addTask () { // 切换新增和input输入
      this.isshowAdd = 1
    },
    openDeAllTypesdetalis (v, parentVal, parentParentVal) { // 打开不同分类的详情
      this.$bus.$emit('diffTypesDetails', JSON.stringify(v))
      // sessionStorage.setItem('navCrumbs', JSON.stringify({taskGroup: parentParentVal, taskList: parentVal, task: v}))
    },
    eduitorHeight (str, str1, list) { // 动态改变连接线的高度
      // let ele = document.getElementById(str)
      // setTimeout(function () {
      //   let ele1 = document.getElementById(str1)
      //   if (ele1) {
      //     ele1.style.height = (ele.offsetHeight - 10) + 'px'
      //   }
      // }, 10)
      let index = this.list.length
      for (let i = 0; i < index; i++) {
        let ele = document.getElementById('offsetHeight' + i)
        if (ele) {
          setTimeout(function () {
            let ele1 = document.getElementById('BottomLine' + i)
            if (ele1) {
              ele1.style.height = (ele.offsetHeight - 5) + 'px'
            }
          }, 10)
        }
      }
    },
    openShowOne (v, str, str1, e, list) { // 打开任务分组列表
      v.isshow = v.isshow === 1 ? 0 : 1
      this.eduitorHeight(str, str1, list)
      // this.$nextTick(function () {
      //   let elements = document.getElementsByClassName('ismoveFlow1')
      //   let index = elements.length
      //   console.log(elements)
      //   for (let i = 0; i < index; i++) {
      //     elements[i].setAttribute('ondragstart', 'return false;')
      //     elements[i].setAttribute('draggable', 'false')
      //   }
      // })
    },
    openShowTwo (v, str, str1, id, e, list) { // 显示和隐藏任务列表
      v.isshowBatch = 0
      if (v.isshow === 0) {
        // if (sessionStorage.getItem('judgeIsScreen') === 'false' && v.children_data_type !== '1') {
        if (v.children_data_type !== '1') {
          this.getTaskList(v, list)
        } else {
          v.isshow = 1
        }
      } else {
        v.isshow = 0
      }
      if (!v.subnodeArr || (v.subnodeArr && v.subnodeArr.length === 0)) {
        v.subnodeArr = [{beanName: 'empty'}]
      }
      this.eduitorHeight('', '', list)
    },
    openShowThree (v, list) { // 显示或隐藏子任务列表
      v.isshowBatch = 0
      if (v.isshow === 0) {
        // if (sessionStorage.getItem('judgeIsScreen') === 'false') {
        // if (!this.querywhere) {
        //   this.getTaskList(v, list)
        // } else {
        //   v.isshow = 1
        // }
        this.getTaskList(v, list)
      } else {
        v.isshow = 0
      }
      if (!v.subnodeArr || (v.subnodeArr && v.subnodeArr.length === 0)) {
        v.subnodeArr = [{beanName: 'empty'}]
      }
      this.eduitorHeight('', '', list)
    },
    clickBatchCheckOut (v, e, value) { // 批量操作时的勾选
      if (value.isshowBatch === 1 && v.complete_status === 0 && v.dataType === 2) {
        v.batchCheckOut = v.batchCheckOut === 1 ? 0 : 1
        let count = 0
        let countnext = 0
        let status = true
        value.subnodeArr.forEach((val, key) => {
          if (val.batchCheckOut === 1) {
            count++
          }
          if (val.complete_status === 0 && val.dataType === 2) {
            countnext++
            if (val.batchCheckOut !== 1) {
              status = false
            }
          }
        })
        value.checkedCount = count
        if (!status) {
          value.checked = false
        } else if (count === countnext) {
          value.checked = true
        }
      }
    },
    saveActiveId (id) { // 当前移动的id
      console.log(id)
    },
    allChecked (status, v) { // 批量操作的全选
      let count = 0
      // v.checked = '1'
      // v.checked = !v.checked
      v.subnodeArr.forEach((v1, k1) => {
        if (v.checked) {
          if (v1.complete_status !== 1 && v1.dataType === 2) {
            count++
            v1.batchCheckOut = 1
          }
        } else {
          count = 0
          v1.batchCheckOut = 0
        }
      })
      v.checkedCount = count
    },
    distributionPerson (v) { // 批量操作的分配给
      let arr = []
      v.subnodeArr.forEach((val, key) => {
        if (val.batchCheckOut === 1) {
          arr.push(val)
        }
      })
      if (arr.length === 0) {
        this.$message({
          message: '请至少选择一行！',
          type: 'warning'
        })
        return false
      }
      this.activeListData = v
      HTTPServer.MemberQueryList({'id': this.projectId}, 'Loading').then((res) => {
        res.dataList.forEach((v, k) => {
          // if (k === 0) {
          //   v.isactive = 1
          // } else {
          v.isactive = 0
          // }
        })
        this.distribution = res.dataList
        this.distributionVisible = true
      })
    },
    getTaskList (v, list, status, data) { // 获取某一列的任务列表
      let senddata = {
        id: v.id
      }
      if (JSON.stringify(this.querywhere) !== '{}') {
        let newSenddata = JSON.parse(JSON.stringify(this.queryWhere))
        senddata.filterParam = {
          bean: newSenddata.bean,
          sortField: newSenddata.sortField,
          queryType: newSenddata.queryType,
          queryWhere: newSenddata.queryWhere
        }
      }
      v.subnodeArr = []
      this.eduitorHeight('', '', list)
      HTTPServer.queryTaskWebList(senddata, 'Loading').then((res) => {
        v.isshow = 1
        if (res.dataList && res.dataList.length > 0) {
          res.dataList.map((value, key) => {
            if (value.dataType === 2) {
              value.start_time_task = this.getStartTimeTask(value)
            }
          })
          v.isshow = 1
          v.subnodeArr = res.dataList
        } else {
          v.subnodeArr = [{beanName: 'empty'}]
        }
        if (status === 1) {
          list.forEach((v1, k1) => {
            if (v1.id === v.parentId) {
              v1.subnodeArr.forEach((v2, k2) => {
                if (v2.id === v.id) {
                  v2.isshow = 1
                  if (res.dataList && res.dataList.length > 0) {
                    v2.subnodeArr = res.dataList
                  } else {
                    v2.subnodeArr = [{beanName: 'empty'}]
                  }
                }
              })
            }
          })
        }
        // if (data) {
        //   this.getTaskList(data, list, 1)
        // }
        this.eduitorHeight('', '', list)
        this.$nextTick(function () {
          this.changeTaskIconPosition()
        })
      })
      this.$nextTick(function () {
        this.changeTaskIconPosition()
      })
    },
    getStartTimeTask (v) { // 获取开始时间
      this.layout.map((val, key) => {
        if (val.label === '开始时间') {
          return v[val.name]
        }
      })
    },
    showOrHiddenLine (status) { // 连接线的样式
      let ele = document.querySelectorAll('.BottomLine')
      let index = ele.length
      for (let i = 0; i < index; i++) {
        if (status === 1) {
          ele[i].style.display = 'block'
          ele[i].style.borderColor = '#D7D7D7'
        } else {
          ele[i].style.display = 'none'
          ele[i].style.borderColor = '#F2F3F7'
        }
      }
    },
    hiddleLine (val, active) { // 拖动排序时隐藏连接线
      console.log(val)
      if (active && active.flow_status === '1') {
        return false
      }
      if (active) {
        this.moveActiveDetails = JSON.parse(JSON.stringify(active))
      }
      if (val && val === 0) {
        this.showOrHiddenLine(0)
      } else if (val && val === 1) {
        console.log('')
      } else {
        if (this.judgeProjectRoleInfo(18)) {
          this.showOrHiddenLine(0)
        }
      }
    },
    showLine (list) { // 显示连接线
      this.showOrHiddenLine(1)
      // list.forEach((v, k) => {
      //   this.eduitorHeight('offsetHeight' + k, 'BottomLine' + k)
      // })
      this.eduitorHeight('', '', list)
    },
    changedata2 (e, v, str, str1, status, list) { // 列表拖动排序
      this.delAtrrbuite(v.subnodeArr, status)
      this.eduitorHeight(str, str1, list)
      let activeId = ''
      this.moveActiveDetails.subnodeArr.forEach((value, key) => {
        v.subnodeArr.forEach((value1, key1) => {
          if (value.id === value1.id) {
            activeId = value1.id
          }
        })
      })
      let senddata = {
        projectId: parseInt(this.projectId),
        toNodeId: v.id,
        dataList: v.subnodeArr,
        originalNodeId: this.moveActiveDetails.id,
        activeNodeId: activeId
      }
      HTTPServer.sortSubNode(senddata, 'Loading').then((res) => {
        // this.getData(this.projectId)
      })
    },
    changedata3 (v, str, str1, psrentList) { // 任务拖动排序
      this.delAtrrbuite(v.subnodeArr, 'taskEmpty')
      this.eduitorHeight(str, str1, psrentList)
      let senddata = {
        toSubnodeId: v.id,
        dataList: v.subnodeArr,
        originalNodeId: this.moveActiveDetails.id
      }
      if (this.moveActiveDetails.children_data_type === '1') {
        senddata.activeNodeId = this.moveActiveDetails.id
      } else {
        this.moveActiveDetails.subnodeArr.forEach((value, key) => {
          v.subnodeArr.forEach((value1, key1) => {
            if (value.taskInfoId === value1.taskInfoId) {
              senddata.taskInfoId = value1.taskInfoId
            }
          })
        })
      }
      this.changeTaskIconPosition()
      HTTPServer.sortTaskWeb(senddata, 'Loading').then((res) => {
        // this.getData(this.projectId)
      })
    },
    changedata4 (v) { // 子任务拖动排序
      this.delAtrrbuite(v.subnodeArr, 'taskEmpty')
      let count = 0
      v.subnodeArr.map((v3, k3) => {
        if (v3.beanName === 'empty') {
          count++
        }
      })
      if (count >= 1) {
        return false
      }
      this.eduitorHeight()
      let senddata = {
        toSubnodeId: v.id,
        dataList: v.subnodeArr,
        originalNodeId: this.moveActiveDetails.id,
        taskInfoId: ''
      }
      this.moveActiveDetails.subnodeArr.forEach((value, key) => {
        v.subnodeArr.forEach((value1, key1) => {
          if (value.taskInfoId === value1.taskInfoId) {
            senddata.taskInfoId = value1.taskInfoId
          }
        })
      })
      this.changeTaskIconPosition()
      HTTPServer.sortTaskWeb(senddata, 'Loading').then((res) => {
        // this.getData(this.projectId)
      })
    },
    updata1 (e) { // 分组拖动排序
      let senddata = {
        projectId: parseInt(this.projectId),
        dataList: []
      }
      this.list.forEach((val, key) => {
        senddata.dataList.push(val)
      })
      HTTPServer.sortMainNode(senddata, 'Loading').then((res) => {
        // this.getData(this.projectId)
      })
    },
    updata2 (e, list, v) {
      let senddata = {
        projectId: parseInt(this.projectId),
        toNodeId: v.id,
        dataList: list,
        originalNodeId: v.id,
        activeNodeId: v.id
      }
      HTTPServer.sortSubNode(senddata, 'Loading').then((res) => {
        // this.getData(this.projectId)
      })
    },
    updata3 (e, v, value) {
      // console.log(e.target)
      let senddata = {
        toSubnodeId: v.id,
        dataList: v.subnodeArr
      }
      if (v.children_data_type === '1') {
        let senddatanew = {
          projectId: parseInt(this.projectId),
          toNodeId: v.id,
          dataList: v.subnodeArr,
          originalNodeId: v.id,
          activeNodeId: v.id
        }
        HTTPServer.sortSubNode(senddatanew, 'Loading').then((res) => {
          // this.getData(this.projectId)
        })
      } else {
        senddata.taskInfoId = v.subnodeArr[e.newIndex].taskInfoId
        HTTPServer.sortTaskWeb(senddata, 'Loading').then((res) => {
          // this.getData(this.projectId)
        })
      }
      this.$nextTick(function () {
        this.changeTaskIconPosition()
      })
    },
    updata4 (e, v) {
      let senddata = {
        toSubnodeId: v.id,
        dataList: v.subnodeArr,
        taskInfoId: v.subnodeArr[e.newIndex].taskInfoId
      }
      this.$nextTick(function () {
        this.changeTaskIconPosition()
      })
      HTTPServer.sortTaskWeb(senddata, 'Loading').then((res) => {
        // this.getData(this.projectId)
      })
    },
    clonedraggableTask1 (list, str) {
      if (!this.judgeProjectRoleInfo(18)) {
        list.forEach((v, k) => {
          let eles = document.getElementsByClassName(str + k)
          if (eles.length > 1) {
            let index = eles.length
            for (let i = 0; i < index; i++) {
              if (eles[i] && eles[i].style.display && eles[i].style.display === 'none') {
                eles[i].parentNode.removeChild(eles[i])
              }
            }
          }
        })
      }
    },
    clonedraggableTask3 (list, val, parentVal) {
      if (parentVal.flow_status === '1') {
        return false
      }
      if (val === 1 || !this.judgeProjectRoleInfo(18)) {
        list.forEach((v, k) => {
          let eles = document.getElementsByClassName('batchSetStyle' + k)
          if (eles.length > 1) {
            let index = eles.length
            for (let i = 0; i < index; i++) {
              if (eles[i] && eles[i].style.display && eles[i].style.display === 'none') {
                eles[i].parentNode.removeChild(eles[i])
              }
            }
          }
        })
      }
    },
    delAtrrbuite (list, status) {
      let index = ''
      if (list && list.length > 0) {
        list.forEach((v, k) => {
          if (status === 'taskEmpty') {
            if (v.beanName === 'empty') {
              index = k
            }
          } else {
            if (!v.name) {
              index = k
            }
          }
        })
      }
      if (index !== '') {
        list.splice(index, 1)
      }
    },
    empty (e, list, str, str1, status, parentList) { // 移动结束
      if (list && list.length === 0) {
        if (status === 'taskEmpty') {
          list.push({beanName: 'empty'})
        } else {
          list.push({name: '', list: []})
        }
      }
      this.eduitorHeight(str, str1, parentList)
      this.showOrHiddenLine(1)
      this.changeTaskIconPosition()
    },
    editorLine () { // 连接线初始化
      let ele = document.getElementsByClassName('BottomLine')
      let index = ele.length
      for (let i = 0; i < index; i++) {
        ele[i].style.height = 40 + 'px'
      }
    },
    changeTaskIconPosition () { // 动态修改任务列表中icon的位置
      let eles = document.getElementsByClassName('taskListDetail')
      let index = eles.length
      for (let i = 0; i < index; i++) {
        eles[i].children[1].style.marginTop = (eles[i].offsetHeight / 2 - 20) + 'px'
      }
    },
    updateSubNodeDataType (v) { // 修改任务列表数类型
      let senddata = {
        nodeId: v.id,
        children_data_type: 1
      }
      HTTPServer.updateSubNodeDataType(senddata, 'Loading').then((res) => {
        // this.getData(this.projectId)
        this.list.map((v1, k1) => {
          if (v1.subnodeArr && v1.subnodeArr.length > 0) {
            v1.subnodeArr.map((v2, k2) => {
              if (v.id === v2.id) {
                this.getquerySubNode(v2)
              }
            })
          }
        })
      })
    },
    getquerySubNode (item) { // huoqu querySubNode
      let senddata = {
        id: item.id,
        node_level: 3
      }
      HTTPServer.querySubNode(senddata, 'Loading').then((res) => {
        if (res.dataList && res.dataList.length > 0) {
          res.dataList.map((v2, k2) => {
            v2.isshow = 0
            v2.isshowBatch = 0
            v2.checkedCount = 0 // 批量操作选择个数
            v2.checked = false // 批量操作全选
            v2.dateTime = '' // 批量操作事件
            if (v2.subnodeArr) {
              if (v2.subnodeArr.length === 0) {
                v2.subnodeArr.push({beanName: 'empty'})
              }
            } else {
              v2.subnodeArr = []
              v2.subnodeArr.push({beanName: 'empty'})
            }
          })
          item.subnodeArr = res.dataList
        }
      })
    },
    addRowDatanew (v) { // 新增列表或引用模版
      this.dialogVisible = false
      let data = this.chooseradio
      let str = ''
      if (v) {
        data = v
      } else {
        str = 'addGroup'
      }
      this.$bus.$emit('creatTaskAndQuoteTemplate', data, str)
    },
    sliceName (name) {
      if (name) {
        return name.slice(-2)
      }
    },
    judgeTimeOld (time) { // 判断任务有无过期
      if (time) {
        return new Date().getTime() > time
      }
    },
    editorTime (time) {
      if (time) {
        let newtime = tool.formatDate(time, 'yyyy-MM-dd HH:mm')
        return newtime.slice(5)
      }
    },
    // 判断字段类型
    filedTypeJudge (data) {
      if (data) {
        return data.split('_')[0]
      }
    },
    // 地址组件转换
    locationShift (data) {
      if (data) {
        return data.value
      } else {
        return ''
      }
    },
    textareaNewline (data) {
      if (data) {
        data = data.replace(/\n/g, '<br>')
        return data
      }
    },
    // 根据组件类型显示列表字段
    getType (name) {
      let type = name.split('_')[1]
      return type
    },
    // 判断文件类型
    fileType (type) {
      let file = tool.determineFileType(type)
      return file
    },
    // 超链接打开网站
    openUrl (url) {
      let linkurl = ''
      if (url.includes('https://')) {
        linkurl = url
      } else {
        linkurl = url
      }
      window.open(linkurl)
    },
    filterEditor (html) { // 过滤富文本
      let newHtml = html.replace(/<\/?(img|a|p|b)[^>]*>/gi, '')
      return newHtml
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
    ...mapState({
      projectStatus: state => state.projectData.project_status
    }),
    subDraggable (v) {
      // let put = v.flow_status === '1' || false
      // let put = false
      return {
        animation: 200,
        group: {name: 'sone', pull: true, put: false},
        sort: true,
        ghostClass: 'ghost',
        filter: '.no-drag'
      }
    },
    subItemDraggable (v) {
      // let put = v.flow_status === '1' || false
      // let put = false
      return {
        animation: 200,
        group: {name: 'soneItem', pull: true, put: true},
        sort: true,
        ghostClass: 'ghost',
        filter: '.no-drag'
      }
    }
  },
  watch: {
    projectStatus (val, oldVal) {
      this.getBaseSetting(this.projectId)
    }
  },
  beforeDestroy () {
    this.$bus.off('changeProjectId')
    this.$bus.off('memoSaveSuccess')
    this.$bus.off('fromDetailCrumbsTwo')
    this.$bus.off('quoteSaveSuccess')
    this.$bus.off('delCompleteUpdata')
    this.$bus.off('screenProjectTask')
    this.$bus.off('successAddTaskList')
    this.$bus.off('listComplateActiveStatus')
  }
}
</script>

<style lang="scss" scoped>
  .main-project-task-wrap{
    // height: 100%;
    height: 92vh;
    padding: 20px 20px 0;
    .el-main{
      padding: 0;
      .allMain:after{
        content: '';
        display: table;clear: both;
      }
      .icon-zirenwu{font-size:18px;margin-right:5px;}
      .rightSetting.rightSettingBox{
        height: 60px;
        >span{height: 60px;line-height: 60px;}
      }
      .rightSetting{
        float: right;min-width: 50; text-align: center; height: 50px;display:flex;margin-right:10px;
        .new-add-nav-span{
          height: 50px;
          display: inline-block;
        }
        >span{
          width: 40px;height: 50px;text-align: center;line-height: 50px;
        }
        >span.mouseEnterCss{
          position: relative;
          >div{
            position: absolute;top: 40px;width: 120px;left: 0;max-height: 100px;background: #fff; z-index: 1000;display:none;text-align:left;
            >p{height: 40px;line-height: 40px;i{margin: 0 15px;}}
          }
          .delDiv{box-shadow: 2px -2px 4px 0 rgba(155,155,155,0.30), -2px 2px 4px 0 rgba(155,155,155,0.30);border-radius: 4px;padding:10px 0;}
          >div.listDelDiv{max-height: 140px;}
        }
        .mouseEnterCss{
          &:hover{
            >div.delDiv{
              display: block;
              >p{&:hover{color:#1890FF;background: #F2F2F2;i{color:#1890FF;}}}
            }
          }
        }
      }
      .addNewList{
        // width: 60%;
         height: 50px;text-align: center; margin: 0 auto;
        >div{
          line-height: 50px;width:950px;height:50px;
          >span,>div{float: left;&:hover{cursor: pointer;}}
          >span{
            width: 50px; height: 50px;
          }
          >div{
            width: 800px; height: 50px;  background: #1890FF; color: #fff; border-radius: 5px;opacity: 0.58;
            &:hover{opacity: 1;}
            >div.spanCss{
              height: 50px;
              >span:first-child{
                font-size: 25px; margin-right: 20px;
              }
              >span:last-child{
                font-size: 16px;
              }
            }
          }
        }
        .addInputnew{height: 50px;
          border-radius: 5px;
          line-height: 50px;
          >div:first-child{
            width: 50%; float:left;  padding: 0 20px;
          }
          >div:last-child{
            width: 30%; float:right;
          }}
      }
      .allMain{
        // width: 60%;
        position: relative;  margin: 10px auto;
        .BottomLine{
          position: absolute;height: 40px;width:  1px;top: 35px; left: 23px;border: 2px solid #D7D7D7;
        }
      }
      .outBox{
        height: 60px;width: 950px;
        .circle{
          height: 60px;line-height: 60px; width: 50px; float: left; position: relative;
          >span{
            position: absolute; top: 0;left: 0;bottom: 0;right: 0;margin: auto; height: 20px;width: 20px; border-radius: 50%;border: 4px solid #D7D7D7;
          }
        }
        .parentCss{
          background: #fff;height: 60px;line-height: 60px;float: left; border: 1px solid #DADADA;box-shadow: 0 0 10px #ddd;width: 800px;border-radius: 5px;
          &:hover{cursor: pointer;}
          .firstItem{
            >i{margin-left: 10px;float:left;}&:hover{cursor:pointer;}
            >span{
              margin-left: 10px;float:left;
              width: 650px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
          }
        }
        .parentCss.show{border-bottom:0;border-bottom-left-radius:0;border-bottom-right-radius:0;box-shadow: 0 -1px 1px #ddd;}
        .parentCss.hidden{border:1px solid #ddd;border-radius:5px;}
      }
      .second{
        width: 800px;margin-left: 50px;background: #fff;border-radius: 5px;padding: 20px;border: 1px solid #ddd;border-top:0;
        border-top-left-radius:0;border-top-right-radius:0;
        .threeItem{
          &:hover{cursor: pointer;}
          height: 50px;line-height: 50px; background: #F1F3F4; border-radius: 5px;
          .showOrHidden{
            i{margin-left: 10px;float:left;}
            >span{margin-left: 10px;float:left;width:620px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;}
          }
        }
        .threeItem.show{border:1px solid #ddd;border-bottom:0;border-bottom-left-radius:0;border-bottom-right-radius:0;}
        .threeItem.hidden{border:0;border-radius:5px;}
        .batchOperationSet{
          position: relative; height: 70px;padding: 5px 20px;border-right:1px solid #ddd;border-left:1px solid #ddd;
          >div:first-child{
            height: 70px;
            >div{
              height: 35px;
              line-height: 35px;
            }
            >div:last-child span{font-size: 12px;color: #848484;margin-left: 10px;}
          }
          >div:last-child{
            position: absolute;top: 17px;right: 19px;text-align: center;line-height: 30px;height: 30px;color: #fff; &:hover{cursor: pointer;}
            background: #1890FF;border-radius: 5px;padding: 0 20px;
          }
          .batchTop{
            >span{display: inline-block;height:35px;margin-right:10px;}
            >span:first-child{span{padding:4.5px 10px;border:1px solid #ddd;border-radius: 5px;}}
          }
        }
        .addInput{
          height: 50px;
          border:1px solid transparent;
          background: #EDF5F9;
          border-radius: 5px;
          line-height: 50px;
          &:hover{
            border-color:#19ABFD;
          }
          >div:first-child{
            width: 75%; float:left;  padding: 0 20px;
          }
          >div:last-child{
            width: 20%; float:right;
          }
        }
      }
      .four{
        width: 758px;border-radius: 5px; border: 1px solid #ddd;border-top-left-radius: 0;border-top-right-radius: 0;border-top: 0;box-shadow: 0 1px 1px #ddd;padding: 10px 20px;
        .icon-CombinedShape,.icon-icon-test6,.icon-bohui1{
          position: absolute;top:0;right:70px;font-size:40px;
        }
        .icon-CombinedShape{color:#F5A623;}
        .icon-bohui1{color:#FF3B30;}
        .icon-icon-test6{color:#38D3A9;}
        .fourItme{
          margin: 8px 0;
          background: #fff;
          min-height:50px;
          border: 1px solid transparent;
          border-radius: 5px;
          box-shadow: 0 0 5px #ccc;
          overflow: hidden;
          background: #fff;&:hover{cursor: pointer;}
          .approvalListDetail,.memoListDetail{
            height:50px; line-height: 50px;padding: 0 20px 0 5px;>span{float:left;margin-right:10px;}>span:nth-child(2){i{color:#F56C6C;font-size:25px;}}
            >span:nth-child(3){max-width:300px;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;}
            >span:nth-child(4){i{font-size: 12px;margin-right:5px;}}
            >span:nth-child(4),>span:nth-child(5){font-size:12px;color:#ccc;}
            >span:last-child{
              float:right;margin:0;line-height:50px;height:50px;
              >span:first-child{
                float: left;width:90px;height:50px;line-height:50px;>span{float:left;}
                >span:first-child{height:10px;width:10px;border-radius: 50%;margin:20px 10px 0 0;}
                >span:first-child.circleCss0{background: #FFA92E;}
                >span:first-child.circleCss1{background: #008FE5;}
                >span:first-child.circleCss2{background: #00A85B;}
                >span:first-child.circleCss3{background: #FA3F39;}
                >span:first-child.circleCss4{background: #CACACA;}
                >span:first-child.circleCss5{background: #00A85B;}
                >span:first-child.circleCss6{background: #00A85B;}
              }
              >span:last-child{
                float: left;height:30px;width:30px;border-radius: 50%;text-align: center;line-height: 30px;color:#fff;margin:10px 0 0 5px;
                img{width:30px;height:30px;border-radius: 50%;vertical-align: sub;border-radius:50%; }
              }
            }
            span.showBgcolor.addBgcolor{background: #60CDFF;}
          }
          .approvalListDetail .peopleBgColor{background: #60CDFF;span{font-size: 12px;}}
          .customListDetail{
            min-height:50px;padding: 10px 10px 10px 4px;
            >div{float:left;margin-right:10px;}
            >div:nth-child(2){
              span{display:inline-block;height:27px;width:25px;text-align:center;line-height: 27px;background: #549AFF;color:#fff;border-radius:2px;}
            }
            >div:nth-child(3){
              max-width:84%;margin-right:0;
              >div:first-child{
                overflow: hidden;text-overflow:ellipsis;white-space: nowrap;padding:5px 0;
              }
              >div:last-child{font-size: 12px;color:#AFAFB5;overflow: hidden;}
            }
            >div:last-child{
              float: right;
              >div{
                height:30px;width:30px;border-radius: 50%;text-align: center;line-height: 30px;color:#fff;
                img{width:30px;height:30px;border-radius: 50%;vertical-align: middle;}
              }
            }
            div.customItemStyle{
              width: 100%;height:20px;
              >div{
                float:left;
                max-width: 190px;
                overflow: hidden;
                text-overflow:ellipsis;
                white-space: nowrap;
                margin-right:10px;
                span{font-size:12px;}
              }
            }
          }
          .customListDetail:after,.taskListDetail:after{content:'';display: table;clear:both;}
          .taskListDetail{
            border-left:4px solid transparent;min-height:50px;padding: 10px 20px 10px 0;>div{float:left;margin-right:10px;}
            >div.taskOpenOrclose{
              margin-top:6px;
              .chooseCheckedActive{background: #1890FF;border-color:#1890FF;>i{color:#fff;}}
            }
            >div:first-child{width:4px;height:100%;background: #3EA8FF;}
            >div:first-child,>div:nth-child(2){height:100%;}
            >div:nth-child(4){
              >div{padding:5px;min-height:25px;width:580px;overflow: hidden;}
              >div.twoSubTaskList{font-size:12px;color:#AFAFB5;max-width:580px;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;}
              >div.threeSubTaskList{
                min-height:28px;padding-bottom:0;
                .threeSubTaskListTags{
                  margin-bottom:5px;padding:2px 5px;background:#51D0B1;color:#fff;border-radius: 4px;font-size:12px;margin-right:5px;margin-bottom:5px;display: inline-block;overflow: hidden;
                }
              }
            }
            >div:last-child{
              float: right;height:30px;width:30px;border-radius: 50%;text-align: center;line-height: 30px;color:#fff;margin-right:0;
              img{width:30px;height:30px;border-radius: 50%;vertical-align: sub;border-radius: 50%;}
              span{font-size:12px;}
            }
            >div.showNameBgColor{background: #60CDFF;}
            .taskoverHidden{
              >div>span{float:left;margin-right:10px;}
              span.taskSuboverHidden{
                max-width:100%;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;
              }
              // >span.taskSuboverHidden{display:inline-block;height:25px;max-width:280px;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;}
              // >span:nth-child(2){color:#AFAFB5;margin:0 10px;>span{font-size: 12px;}i{margin-right:5px;}}
              // >span:nth-child(3){color:red;>span,i{font-size:12px;}}
              span.foreachSpantask{
                font-size:12px;max-height: 19px;
                span{font-size:12px;color:#A2A2A8;}
                .multitext{
                  // height:19px;overflow: hidden;
                }
                max-width:190px;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;
              }
              .subTaskCount{
                font-size:12px;color:#A2A2A8;
                i{font-size:12px;}
                span{font-size:12px;}
                i.icon-zirenwu{font-size:15px;}
              }
            }
            .taskoverHidden>div:after{content:'';display: table;clear:both;}
          }
          .taskListDetail .taskOpenOrclose{
            div{
              height:18px;width:18px;position:relative;border: 1px solid #B9B9C1;border-radius: 3px;
              i{
                position:absolute;font-size:12px;color: #B7B7BF;top:2px;left:-1px;height:12px;transform:scale(.8);
              }
            }
          }
          .taskListDetail.file{border-left-color:#36CFC9;}
          .taskListDetail.stop{border-left-color:#8A96AD;}
          .taskListDetail.del{border-left-color:#FF5800;}
          .taskListDetail.use{border-left-color:#52C41A;}
          .checkOutIcon{
            position:absolute;top:-20px;left:-20px;>i{position: absolute;color:#fff;top:-6px;right:-21px;font-size: 12px;transform: rotate(-42deg);}
            border:20px solid transparent;border-right-color: #178FFF;transform:rotate(45deg);
          }
          .subfourItemIn{pointer-events:none;cursor:default;opacity:.6;}
          .subfourItemOut{pointer-events:auto;cursor:pointer;opacity:1;}
        }
        .fourItme.batchCheckOutActive{border:1px solid#419BF9;position:relative;}
        .fourItme{&:hover{cursor:pointer;}}
        .five{
          width: 716px;
          border-radius: 5px;
          border: 1px solid #ddd;
          border-top-left-radius: 0;
          border-top-right-radius: 0;
          border-top: 0;
          box-shadow: 0 1px 1px #ddd;
          padding: 10px 20px;
          background: #EBEDF0;
        }
        .sub-new-box{
          margin:10px 0;
        }
        .sub-new-list{
          padding: 0 10px;
          height: 50px;
          line-height: 50px;
          box-shadow: 0 0 4px 0 #ccc;
          border-radius: 4px;
          .showOrHidden.showOrHiddennew{
            display: inline-block;
            >i{float:left;}
            >span{
              float:left;margin-left: 10px;
              width: 570px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
          }
        }
        .sub-new-list.show{
          border: 1px solid #ddd;
          border-bottom: 0;
          border-bottom-left-radius: 0;
          border-bottom-right-radius: 0;
        }
        .sub-new-list.hidden{border: 0;border-radius: 5px;}
      }
      .secondItem{
        margin-bottom: 5px;&:hover{cursor:pointer;}
      }
    }
    .rightSetting{i{margin: 0;color:#A9A9A9;}i.icon-nav-quickly-add{font-size:24px;}span{&:hover{cursor: pointer;}}}
    .moveBox{
      margin: 5px;
      .moveTaskCss{height: 40px;line-height: 40px;text-align: center;color: #7C7C7C;}
    }
    .taskGroup{
      margin-bottom: 10px;
      border: 1px solid #ddd;
      padding: 8px 20px;
      position: relative;
      width: 100%;
      >i{
        position: absolute;
        top: 15px;
        right: 25px;
        &:hover{cursor: pointer;}
      }
    }
    #distributionDilog{
      .distribtionParentBox{
        max-height:650px;overflow: auto;
        .distributionToOther{
          height:60px;line-height: 60px;width:100%;&:hover{cursor:pointer;background: #F2F2F2;}padding:0 20px;position: relative;
          >span{float:left;margin-right:10px;height:60px;}
          >span:nth-child(1){height:60px;width:40px;span,img{margin-top:10px;height:40px;width:40px;border-radius: 50%;}span{float:left;background: #1890FF;color:#fff;line-height: 40px;text-align: center;}}
          .projectPerson{color:#1CBE98;i{font-size: 16px;}}
          >span.depPost{font-size:12px;color:#A6A6B3;span{font-size:12px;}}
          >span.personIsclick{
            position: absolute;top:0;right: 20px;i{color:#208AF4;font-size: 12px;}
          }
          >span.personIsactive{
            span{float: left;height:10px;width:10px;border-radius: 50%;background: red;margin-top:25px;}
          }
        }
        .distributionToOther:after{content:'';display: table;clear:both;}
      }
    }
    .icon-yidong{font-size:20px;color:#BBBBC3;}
    .showTimeTooltip{font-size:12px;}
  }
</style>
<style lang="scss">
  .showTimeTooltip{font-size:12px;}
  .addInput{
    .el-button{
      padding: 8px 15px;
    }
    .el-button--primary.is-plain{border-color:#409EFF;}
    .el-input__inner{
      height: 36px;
    }
  }
  .main-project-task-wrap{
    .batchTop{
      .el-button.el-button--default{
        padding: 7px 20px;
      }
    }
    #moveDilog {
      .el-dialog.el-dialog--center{
        .el-dialog__header{border-bottom: 1px solid #ddd;background:#fff;}
      }
      .el-dialog.el-dialog--center .el-dialog__footer{text-align: right;}
    }
    .inputGroupName{
      height:60px;line-height:60px;padding: 15px 0 5px 0;
      .el-input__inner{height:36px;background: #FBFBFB;}
    }
    .batchEditor{
      position: absolute;
      left: 0;
      top: 0;
      opacity: 0;
      .el-date-editor.el-input{
        width: 100px;
        padding-left:20px;
        &:hover{cursor: pointer;}
        .el-input__inner{
          &:hover{cursor: pointer;.endtimeCss{background:#ECF5FF;color:#409EFF;}}
          padding:0;
        }
      }
    }
    #addListAndQueto{
      .el-dialog__header{background:#fff;height:50px;padding:0;.el-dialog__headerbtn{display:none;}}
      .el-dialog__body{padding:0;}
      .el-dialog__footer{
        height:52px;line-height:52px;padding:0;.el-button{padding:8px 13px;}padding-right:30px;border-top:1px solid #ddd;
      }
    }
    .taskGroup{
      .el-input{width: 250px;}
    }
    .listBoxItem{
      .el-input{width: 220px;}
    }
    .taskListDetail{
      .el-checkbox__inner{width: 18px;height: 18px;}
      .el-checkbox__inner::after{height: 10px;left: 6px;}
    }
    #distributionDilog{
      .el-dialog__header{padding:13px 20px;text-align:left;border-bottom:1px solid #ddd;}
      .el-dialog__footer{padding:15px 20px;border-top:1px solid #ddd;text-align:right;.el-button{padding:10px 20px;}}
      .el-dialog__body{padding:0;}
    }
    #completeActiveStatus{
      .el-dialog__header{display:none;}
      .titleHeader{color:#9B9B9B;margin-bottom:10px;}
    }
  .el-message-box__wrapper{
    .el-message-box__header{background:#fff;.el-message-box__title span{color:#000;}.el-message-box__close.el-icon-close{color:#000;}}
  }
  .creatListTask,.creatTaskGroup{
    .el-input__inner{
      height:34px;line-height:34px;
    }
  }
  .creatTaskGroup{.el-input__inner{background:#FBFBFB;}}
  .five{
    .line-card-task-warp>.taskListDetail>div:nth-child(4)>div{
      width: 550px;
    }
  }
  .chooseCengjiListTaskPorject{
    .el-dialog__header{
      padding:10px 20px;border-bottom:1px solid #ddd;
      .el-dialog__headerbtn{top:15px;}
    }
    .el-dialog__body{
      padding:20px;
      >div{
        font-size:16px;
        >div{
          .el-radio{margin:20px 0 0 0;}
          .icon-liangceng,.icon-sanceng{
            margin-right:10px;
          }
          .icon-sanceng{font-size:18px;}
          .colorDislog{color: #2A2A2A;}
        }
      }
    }
    .el-dialog__footer{
      padding:10px 20px;border-top:1px solid #ddd;
      .el-button{
        padding:0;width:65px;text-align:center;height:32px;line-height:32px;
      }
    }
  }
}
.new-add-nav-quickly{
  width:120px;padding:10px 0;
  .add-list-task{
    >p{
      height:36px;line-height: 36px;padding-left:10px;cursor: pointer;
      &:hover{
        color:#1890FF;background: #F2F2F2;
      }
    }
  }
}
</style>
