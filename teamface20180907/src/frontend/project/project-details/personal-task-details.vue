<template>
  <el-container class="personal-task-details">
    <!-- 人员卡片 -->
    <el-dialog :visible.sync="employeeDialogVisible" width="368px" append-to-body class="myTaskEmployeePersonal">
      <employee-card :employeeData="cardPersonDetails" :departmentData="departmentInfo"></employee-card>
    </el-dialog>
    <!-- 任务详情头部 -->
    <el-header  v-if="(itemValue.bean_name&&itemValue.bean_name.indexOf('project_custom')!==-1&&itemValue.from===1)">
      <i class="el-icon-close" @click="closeDialog"></i>
      <span class="linestyle"></span>
      <el-dropdown trigger="click" @command="handleControl" style="float:right;margin:14px 10px;">
        <span class="el-dropdown-link">
          <i class="icon-Rectangle iconfont"></i>
        </span>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item :command="{id:0,type:6}"><i class="iconfont icon-dongtai iconCustomRx"></i>动态</el-dropdown-item>
          <el-dropdown-item :command="{id:0,type:1}"><i class="iconfont icon-shenpi-shijian iconCustomRx"></i>设置任务提醒</el-dropdown-item>
          <el-dropdown-item :command="{id:0,type:2}" v-if="isParentOrSub===0&&personalTaskRole!=2&&personalTaskRole!=3"><i class="iconfont iconCustomRx icon-icon-test5"></i>设置重复任务</el-dropdown-item>
          <el-dropdown-item :command="{id:0,type:5}" v-if="personalTaskRole!=2&&personalTaskRole!=3"><i class="el-icon-delete iconCustomRx"></i>删除任务</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <!-- 主任务编辑 -->
      <el-button v-if="isParentOrSub===0&&taskDetail.complete_status=='0'&&personalTaskRole!=2&&personalTaskRole!=3" size="mini" style="float:right;margin:14px 0 14px 10px;" @click="editorTask(itemValue,0)">编辑</el-button>
      <!-- 子任务编辑 -->
      <el-button v-if="isParentOrSub===1&&taskDetail.complete_status=='0'&&personalTaskRole!=2&&personalTaskRole!=3" size="mini" style="float:right;margin:14px 0 14px 10px;" @click="editorTask(itemValue,1)">编辑</el-button>
      <div class="activation">激活 <span v-text="taskDetail.activate_number?taskDetail.activate_number:0">12</span> 次</div>
      <!-- 点赞和点赞人数 -->
      <div class="heartIcon">
        <el-tooltip class="item" effect="dark" :content="shareUserList" placement="bottom">
          <i v-if="shareStatusismy===1" class="iconfont icon-xin2" @click="shareThumbs(0)"></i>
          <i v-if="shareStatusismy===0" class="iconfont icon-xin" @click="shareThumbs(1)"></i>
        </el-tooltip>
        <span v-if="taskDetail.shareArr" v-text="taskDetail.shareArr.length">2</span>
      </div>
      <span v-if="isParentOrSub===0">任务</span>
      <span v-if="isParentOrSub===1" style="cursor:pointer;">
        <span style="float:left;">属于任务 : </span>
        <span @click="fromSubTaskToTask" class="subTaskSpan" v-text="taskDetail.task_name">父任务名称</span>
      </span>
    </el-header>
    <!-- 任务详情主体 -->
    <el-main class="el-main-wrap"  v-if="(itemValue.bean_name&&itemValue.bean_name.indexOf('project_custom')!==-1&&itemValue.from===1)">
      <i class="iconfont icon-CombinedShape" v-if="isParentOrSub===0&&taskDetail.complete_status=='1'&&taskDetail.check_member == userInfo.id"></i>
      <div class="all-pain-box">
        <div class="checkAndTitle">
          <div :style="{'border-left-color': taskDetail.picklist_priority&&taskDetail.picklist_priority.length>0?taskDetail.picklist_priority[0].color:''}">
            <!-- 完成或者激活任务 -->
            <el-tooltip class="item" effect="dark" content="子任务尚未全部完成，无法完成该任务" placement="top" v-if="showIscomplete===0&&isParentOrSub===0">
              <span>
              </span>
            </el-tooltip>
            <span v-else @click="isActivation('激活原因',0)" :class="taskDetail.complete_status=='1'?'chooseCheckedActive':''">
              <i v-if="taskDetail.complete_status=='1'" class="iconfont icon-pc-paper-optfor"></i>
            </span>
          </div>
          <div v-if="isParentOrSub===0" v-text="dataDetail.text_name">gfdgdfgdfg</div>
          <div v-if="isParentOrSub===1" v-text="taskDetail.name">gfdgdfgdfg</div>
        </div>
        <!-- 自定义布局 -->
        <div class="colum-box">
          <div class="item" v-for="(item,index) in layout" :key="index" :style="{width:item.width}" :class="{'top-bottom':item.field.structure === '0'}">
            <div class="components">
              <label>{{item.label}}</label>
              <div v-if="item.type === 'personnel' || item.type === 'department'" class="personnel">
                <head-image v-for="(child,index) in dataDetail[item.name]" :key="index" :head="child"></head-image>
              </div>
              <div v-else-if="item.type === 'datetime'">{{dataDetail[item.name] | formatDate('yyyy-MM-dd')}}</div>
              <div v-else-if="(item.type === 'picklist' || item.type === 'mutlipicklist')&&item.name==='picklist_tag'">
                <span class="picklist" v-for="(child,index) in dataDetail[item.name]" :key="index" :style="{'background':child.colour}" :class="{'white-font':child.color === '#FFFFFF'}">{{child.name}}</span>
              </div>
              <div v-else-if="item.type === 'picklist' || item.type === 'mutlipicklist'" class="picklist">
                <span v-for="(child,index) in dataDetail[item.name]" :key="index" :style="{'background':child.color}" :class="{'white-font':!child.color || child.color === '#FFFFFF'}">{{child.label}}</span>
              </div>
              <div v-else-if="item.type === 'multi'">
                <span v-for="(child,index) in dataDetail[item.name]" :key="index">{{child.label}}<small v-if="dataDetail[item.name].length !== index + 1">/</small></span>
              </div>
              <div v-else-if="item.type === 'reference' && item.name === 'reference_relation'">
                <span @click="goReferenceModel(taskDetail,-1)" style="color: #008fe5;cursor:pointer;">{{taskDetail.relation_data}}</span>
              </div>
              <div v-else-if="item.type === 'reference'" class="reference">
                <span v-for="(child,index) in dataDetail[item.name]" :key="index" @click="goReferenceModel(item,child.id)" style="color: #008fe5;cursor:pointer;">{{child.name}}</span>
              </div>
              <span v-else-if="item.type === 'multitext'" v-html="dataDetail[item.name]" class="multitext multitext-out-box" style="overflow-x: auto;"></span>
              <div v-else-if="item.type === 'textarea'" class="textarea" v-html="textareaNewline(dataDetail[item.name])"></div>
              <div v-else-if="item.type === 'area'">{{dataDetail[item.name] | areaTo}}</div>
              <div v-else-if="item.type === 'location'" v-text="locationShift(dataDetail[item.name])"></div>
              <div v-else-if="item.type === 'attachment'" class="attachment">
                <file-item v-for="(file,index) in dataDetail[item.name]" :key="index" :file="file" :isDownload="true"></file-item>
              </div>
              <div v-else-if="item.type === 'picture'" class="picture">
                <img v-for="(file,index) in dataDetail[item.name]" :key="index" :src="file.file_url+'&TOKEN='+token" @click="openPreview(file)">
              </div>
              <div v-else-if="item.type === 'url'" @click="openUrl(dataDetail[item.name])" class="url">{{dataDetail[item.name]}}</div>
              <div v-else-if="item.type === 'number'">{{dataDetail[item.name] | pointTo(item.field)}}</div>
              <div v-else-if="item.type === 'formula' || item.type === 'functionformula' || item.type === 'seniorformula'">{{dataDetail[item.name] | formulsTo(item.field)}}</div>
              <!-- 子表单表格 -->
              <span v-else-if="item.type === 'subform'&&(item.field.formStyle==='0'||!item.field.formStyle)" class="subform">
                <span class="titles">
                  <span v-for="(title,index) in item.componentList" :key="index" v-if="title.field.terminalPc === '1'">{{title.label}}</span>
                </span>
                <div class="list" v-for="(subform,index) in dataDetail[item.name]" :key="index">
                  <div class="subform-child" v-for="(items,index) in subform" :key="index" :class="getType(index)+'-box'" v-if="showPC(item.componentList, index)">
                    <span v-if="getType(index) === 'personnel' || getType(index) === 'department'"  class="personnel">
                      <head-image v-for="(child,index) in items" :key="index" :head="child"></head-image>
                    </span>
                    <span v-else-if="getType(index) === 'datetime'"  v-text="formatDateTo(items,item.componentList, index)"></span>
                    <span v-else-if="getType(index) === 'picklist'  || getType(index) === 'mutlipicklist'" :class="{picklist: items.length}">
                      <span v-for="(child,index) in items" :key="index" :style="{'background':child.color}" :class="{'white-font':!child.color || child.color === '#FFFFFF'}">{{child.label}}</span>
                    </span>
                    <div v-else-if="getType(index) === 'multitext'" class="multitext" v-html="items" style="overflow-x: auto;"></div>
                    <span v-else-if="getType(index) === 'multi'">
                      <span v-for="(child,index) in items" :key="index">{{child.label}}<small v-if="items.length !== index + 1">/</small></span>
                    </span>
                    <span v-else-if="getType(index) === 'reference'" class="reference" @click="childGoReferenceModel(item.componentList, index,child.id)">
                      <span v-for="(child,index) in items" :key="index" style="color: #008fe5;cursor:pointer;">{{child.name}}</span>
                    </span>
                    <span v-else-if="getType(index) === 'textarea'" class="textarea" v-html="textareaNewline(items)"></span>
                    <span v-else-if="getType(index) === 'area'">{{items | areaTo}}</span>
                    <span v-else-if="getType(index) === 'location'" v-text="locationShift(items)"></span>
                    <span v-else-if="getType(index) === 'attachment'" :class="{attachment: items.length}">
                      <file-item v-for="(file,index) in items" :key="index" :file="file" :isDownload="true"></file-item>
                    </span>
                    <span v-else-if="getType(index) === 'picture'" class="picture">
                      <img v-for="(file,index) in items" :key="index" :src="file.file_url+'&TOKEN='+token" @click="openPreview(file)">
                    </span>
                    <span v-else-if="getType(index) === 'url'" @click="openUrl(items)" class="url">{{items}}</span>
                    <span v-else-if="getType(index) === 'number'" v-text="numberTo(items,item.componentList, index)"></span>
                    <span v-else-if="getType(index) === 'formula' || getType(index) === 'functionformula' || getType(index) === 'seniorformula'" v-text="formatFormuls(items,item.componentList, index)"></span>
                    <span v-else class="textarea">{{items}}</span>
                  </div>
                </div>
              </span>
              <!-- 子表单卡片 -->
              <span v-else-if="item.type === 'subform'&&item.field.formStyle==='1'" style="width:100%;">
                <div class="subform-box-card" v-for="(subform,key) in dataDetail[item.name]" :key="key">
                  <div class="subform-box-card-item" v-for="(title,index1) in item.componentList" :key="index1" v-if="title.field.terminalPc === '1'">
                    <div class="card-title">
                      <i class="required" v-if="title.field.fieldControl === '2'">*</i>
                      <span>{{title.label}}</span>
                    </div>
                    <div>
                      <div v-for="(items,index) in subform" :key="index" :class="getType(index)+'-box'" v-if="showPC(item.componentList, index)&&title.name===index">
                        <div class="card-body">
                          <span v-if="getType(index) === 'personnel' || getType(index) === 'department'"  class="personnel">
                            <head-image v-for="(child,index) in items" :key="index" :head="child"></head-image>
                          </span>
                          <span v-else-if="getType(index) === 'datetime'"  v-text="formatDateTo(items,item.componentList, index)"></span>
                          <span v-else-if="getType(index) === 'picklist'  || getType(index) === 'mutlipicklist'" :class="{picklist: items.length}">
                            <span v-for="(child,index) in items" :key="index" :style="{'background':child.color}" :class="{'white-font':!child.color || child.color === '#FFFFFF'}">{{child.label}}</span>
                          </span>
                          <span v-else-if="getType(index) === 'multi'">
                            <span v-for="(child,index) in items" :key="index">{{child.label}}<small v-if="items.length !== index + 1">/</small></span>
                          </span>
                          <span v-else-if="getType(index) === 'reference'" class="reference" @click="childGoReferenceModel(item.componentList, index,child.id)">
                            <span v-for="(child,index) in items" :key="index" style="color: #008fe5;cursor:pointer;">{{child.name}}</span>
                          </span>
                          <span v-else-if="getType(index) === 'textarea'" class="textarea" v-html="textareaNewline(items)"></span>
                          <div v-else-if="getType(index) === 'multitext'" class="multitext" v-html="items" style="overflow-x: auto;"></div>
                          <span v-else-if="getType(index) === 'area'">{{items | areaTo}}</span>
                          <span v-else-if="getType(index) === 'location'" v-text="locationShift(items)"></span>
                          <span v-else-if="getType(index) === 'attachment'" :class="{attachment: items.length}">
                            <file-item v-for="(file,index) in items" :key="index" :file="file" :isDownload="true"></file-item>
                          </span>
                          <span v-else-if="getType(index) === 'picture'" class="picture">
                            <img v-for="(file,index) in items" :key="index" :src="file.file_url+'&TOKEN='+token" @click="openPreview(file)">
                          </span>
                          <span v-else-if="getType(index) === 'url'" @click="openUrl(items)" class="url">{{items}}</span>
                          <span v-else-if="getType(index) === 'number'" v-text="numberTo(items,item.componentList, index)"></span>
                          <span v-else-if="getType(index) === 'formula' || getType(index) === 'functionformula' || getType(index) === 'seniorformula'" v-text="formatFormuls(items,item.componentList, index)"></span>
                          <span v-else class="textarea">{{items}}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </span>
              <div v-else class="textarea">{{dataDetail[item.name]}}</div>
            </div>
          </div>
        </div>
        <!-- 协作人 -->
        <div class="xiezuoPerson">
          <div class="xiezuoUser">
            <div>
              <span :style="xiezuoUserStatus?'color:#1890FF;':''">仅参与者可见</span>
              <el-switch v-model="xiezuoUserStatus" v-if="personalTaskRole!=2&&personalTaskRole!=3" @click.native="changeXiezuoren"></el-switch>
              <el-switch v-model="xiezuoUserStatus" v-else disabled></el-switch>
            </div>
            <!-- <div v-else></div> -->
            <div><i class="iconfont icon-duoren" style="color:#C1C1C8;"></i><span style="margin-left:10px;">协作人</span></div>
          </div>
          <div class="xzuserList">
            <span v-for="(v,k) in xiezuoPerosonalList" :key="k" class="peopleList" @click="openPersonCard(v,$event)">
              <span class="imgCss" v-if="v.picture&&v.picture!='null'"><img :src="v.picture+'&TOKEN='+token" :title="v.employee_name"></span>
              <span class="nameList" v-if="!v.picture||v.picture=='null'">{{sliceName(v.employee_name)}}</span>
              <!-- 移除协作人 -->
              <i @click.stop="delXiezuoUser(v,k)" class="iconfont icon-pc-shanchu" v-if="personalTaskRole!=2&&personalTaskRole!=3&&(!v.is_delete||v.is_delete!=1)"></i>
            </span>
            <!-- 添加协作人 -->
            <span @click="getMemberList" v-if="personalTaskRole!=2&&personalTaskRole!=3"><i class="iconfont icon-jiaren"></i></span>
          </div>
        </div>
        <!-- 添加子任务 （在子任务中隐藏）-->
        <div class="addSubTask" v-if="isParentOrSub===0">
          <div class="subTaskHeader">
            <span class="addspanstyle"></span>
            <span><i class="iconfont icon-zirenwu"></i><span>子任务</span></span>
            <span class="subTaskProgress"><el-progress :percentage="taskDetail.progressPercentage" color="#51D0B1"></el-progress></span>
            <span class="progressSub">
              <span v-text="taskDetail.progressCount">2</span> /
              <span v-if="taskDetail.subTaskArr" v-text="taskDetail.subTaskArr.length">6</span>
            </span>
          </div>
          <div>
            <div class="subTaskInput taskListSubInput" v-for="(v,k) in taskDetail.subTaskArr" :key="k" @click="openOtherDetails(v)">
              <div class="checkedActive subtaskChecked" @click.stop="isActivation('激活原因',1,v, 'sub')" :class="v.complete_status=='1'?'chooseCheckedActive':''">
                <i v-if="v.complete_status === '1'" class="iconfont icon-pc-paper-optfor"></i>
              </div>
              <div class="subTaskInputName"><span v-text="v.name" class="line-clamp-span" :style="{color:v.complete_status=='1'?'#B3B4B5':''}"></span></div>
              <div class="rightAdduserAndTime">
                <div>
                  <span class="addTimeSubTask">
                    <i class="iconfont icon-shenpi-shijian"></i>
                    <span>{{v.end_time | formatDate('yyyy-MM-dd HH:mm')}}</span>
                  </span>
                </div>
                <div class="taskList">
                  <span class="showPeople">
                    <img v-if="v.picture&&v.picture!='null'" :src="v.picture+ '&TOKEN=' + token" :title="v.employee_name">
                    <span v-if="!v.picture||v.picture=='null'" v-text="sliceName(v.employee_name)" class="bgcolorUser"></span>
                  </span>
                </div>
              </div>
              <i class="iconfont icon-huishouzhan" @click.stop="delSubTask(v,k)" v-if="personalTaskRole!=2&&personalTaskRole!=3"></i>
            </div>
          </div>
          <!-- 新增子任务 -->
          <div class="showOrHiddenAddSubTask" v-if="showOrHiddenAddSubTask">
            <div class="subTaskInput">
              <el-checkbox disabled></el-checkbox>
              <div class="subTaskInputName">
                <!-- <el-input placeholder="子任务名称" v-model="subTaskName"></el-input> -->
                <el-input
                  type="textarea"
                  :autosize="{minRows: 1, maxRows: 5}"
                  placeholder="子任务名称"
                  v-model="subTaskName">
                </el-input>
              </div>
              <!-- <div class="rightAdduserAndTime" style="width:182px;"> -->
              <div class="rightAdduserAndTime">
                <div style="line-height:40px;width:140px;text-align:right;">
                  <span v-if="subTaskEndtime" class="addTimeSubTask">
                    <i class="iconfont icon-shenpi-shijian"></i>
                    <span>{{subTaskEndtime | formatDate('yyyy-MM-dd HH:mm')}}</span>
                    <i class="iconfont icon-shanchu deliconshanchutime" @click="subTaskEndtime=''"></i>
                  </span>
                  <el-tooltip class="item" effect="dark" content="截止时间" placement="top" v-if="!subTaskEndtime">
                    <i class="iconfont icon-riqi"></i>
                  </el-tooltip>
                  <el-date-picker v-model="subTaskEndtime" type="datetime" class="chooseSubTaskTime" @change="changeEndTime" value-format="timestamp" popper-class="personalTaskDetailsClass" @click.native="showDatePosition"></el-date-picker>
                </div>
                <div>
                  <span @click="addSubTaskUser" v-if="JSON.stringify(subTaskUser)==='{}'" class="addUserIcon">
                  <!-- <span @click="getMemberList" v-if="JSON.stringify(subTaskUser)==='{}'" class="addUserIcon"> -->
                    <el-tooltip class="item" effect="dark" content="执行人" placement="top">
                      <i class="iconfont icon-jiaren"></i>
                    </el-tooltip>
                  </span>
                  <span v-else class="showPeople">
                    <span class="deliconperson" @click="subTaskUser={}" :style="subTaskUser.picture&&subTaskUser.picture!='null'?'top:-10px;':'top:-17px;'"><i class="iconfont icon-shanchu"></i></span>
                    <!-- <img src="../../../assets/head1.jpg" alt=""> -->
                    <img v-if="subTaskUser.picture&&subTaskUser.picture!='null'" :src="subTaskUser.picture+ '&TOKEN=' + token" alt="">
                    <span v-if="!subTaskUser.picture||subTaskUser.picture=='null'" v-text="sliceName(subTaskUser.name)" class="bgcolorUser" style="margin-top:6px;"></span>
                  </span>
                </div>
              </div>
            </div>
            <div class="rightBtn">
              <el-button @click.stop="showOrHiddenAddSubTask=false">取 消</el-button>
              <el-button type="primary" @click.stop="saveSubTask">保 存</el-button>
            </div>
          </div>
          <div class="addSubTaskAndQuoteNew" v-if="taskDetail.complete_status=='0'">
            <span @click="addSubTask" v-if="personalTaskRole!=2&&personalTaskRole!=3"><i class="iconfont icon-pc-fine-add-staff"></i><span>添加子任务</span></span>
          </div>
        </div>
        <!-- 关联任务列表 -->
        <div class="associatedTask">
          <div class="addAssociatedTask">
            <span v-show="delQutoeOrComplete===0&&addAssociatedTaskList&&addAssociatedTaskList.length>0" @click="cancelQutoe"><span>取消关联</span></span>
            <span v-show="delQutoeOrComplete===1" @click="completeQutoe"><span>完成</span></span>
            <span><i class="iconfont icon-guanlianyuanwang"></i><span>关联内容</span></span>
          </div>
           <div v-for="(item,index) in addAssociatedTaskList" :key="index" v-if="addAssociatedTaskList">
             <div class="guanlianTitle">
              <span v-text="item.module_name"></span>
              <span></span>
            </div>
            <div v-for="(v,k) in item.moduleDataList" :key="k"  class="guanlidetails" v-if="addAssociatedTaskList" @click="chooseQutoe(v)" :class="v.isQutoe?'guanlidetailsAndChoos':''">
              <div @click.stop="openDeAllTypesdetalis(v,'guanlian')" :class="delQutoeOrComplete===1?'subfourItemIn':'subfourItemOut'">
                <line-card-task :cloumn="v" :listItem="''" :isGuanLian="true"></line-card-task>
              </div>
              <span v-if="v.isQutoe" class="checkOutIcon"><i class="iconfont icon-pc-paper-optfor"></i></span>
            </div>
           </div>
          <div class="addSubTaskAndQuoteNew">
            <span @click.stop="addTaskQuote" v-if="personalTaskRole!=2&&personalTaskRole!=3"><i class="iconfont icon-pc-fine-add-staff"></i><span>添加关联</span></span>
          </div>
        </div>
        <!-- 被关联任务列表 （子任务隐藏）-->
        <div class="associatedTask" v-if="isParentOrSub===0 && (relationTaskList&&JSON.stringify(relationTaskList)!=='[]')">
          <div class="addAssociatedTask">
            <div><i class="iconfont icon-guanlianyuanwang" style="margin-right:10px;color:#C1C1C8;"></i><span>被关联内容</span></div>
          </div>
          <div v-for="(item,index) in relationTaskList" :key="index">
            <div class="guanlianTitle">
              <span v-text="item.module_name"></span>
              <span></span>
            </div>
            <div v-for="(v,k) in item.moduleDataList" :key="k" class="guanlidetails" @click="openDeAllTypesdetalis(v,'guanlian')">
              <line-card-task :cloumn="v" :listItem="''" :isGuanLian="true"></line-card-task>
            </div>
          </div>
        </div>
        <div>
          <module-other :bean="isParentOrSub===0?'personel_task':'personel_sub_task'" :dataId="itemValue.id" :detail="[]" :show="otherShow" :isPersonal="true" v-show="referenceBean === 'basic'" :taskState="isParentOrSub"></module-other>
        </div>
      </div>
      <div class="dynamic-shade" v-if="openDynamic"></div>
      <transition name="slide">
        <module-dynamic v-if="openDynamic" :bean="isParentOrSub===0?'personel_task':'personel_sub_task'" :dataId="itemValue.id" style="top:0;"></module-dynamic>
      </transition>
      <!-- <module-other-next :bean="isParentOrSub===0?'personel_task':'personel_sub_task'" :dataId="itemValue.id" :detail="[]" :show="otherShow" v-show="referenceBean === 'basic'" :taskState="isParentOrSub" :isPersonal="true"></module-other-next> -->
      <!-- 激活或者编辑原因填写 -->
      <el-dialog :visible.sync="activtionAndEditor" width="400px" class="activtionAndEditor" append-to-body>
        <div class="titleHeader"><span style="color:red;">*</span> <span>{{activtionAndEditorStr}}</span></div>
        <div>
          <el-input type="textarea" rows="6" v-model="activationReason"></el-input>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="activtionAndEditor = false">取 消</el-button>
          <!-- <el-button v-if="activtionAndEditorStr==='编辑原因'" type="primary" @click="saveRElationDetail(0)">保 存</el-button> -->
          <el-button v-if="activtionAndEditorStr==='激活原因'" type="primary" @click="saveRElationDetail(1)">激 活</el-button>
        </span>
      </el-dialog>
    </el-main>
    <!-- 审批详情 -->
    <task-apprdetails v-if="itemValue.dataType === 4" ref="TaskApprdetails" :dtlKey="dtlKey"></task-apprdetails>
    <!-- 备忘录详情 -->
    <task-memo-details v-if="itemValue.dataType === 1" :memoDetailProp="{}" :dropOptionValue="0" :dtlKey="dtlKey" :id="itemValue.id"></task-memo-details>
    <!-- 自定义详情 -->
    <otherDetail v-if="itemValue.dataType===3" :dataAll="detailList" :moduleId="this.itemValue.allDetails.module_id" :moduleName="this.itemValue.allDetails.module_name" :dtlKey="dtlKey"></otherDetail>
    <!-- 项目任务详情 -->
    <!-- <module-task-detail v-if="itemValue.dataType === 2&&!itemValue.from" v-for="(v,k) in taskDataList" :key="k" :dtlKey="k" :projectId="projectId" :itemValue="v"></module-task-detail> -->
  </el-container>
</template>

<script>
import ModuleOtherNext from '@/frontend/module/module-other-next'
import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'
import TaskApprdetails from '@/frontend/project/project-details/task-apprdetails'/** 审批详情组件 **/
import TaskMemoDetails from '@/frontend/project/project-details/task-memo-details' /** 备忘录详情 **/
import otherDetail from '@/frontend/project/project-details/other-detail'// 自定义详情
import ModuleTaskDetail from '@/frontend/project/components/module-task-detail' // 任务详情
import HeadImage from '@/common/components/head-image'
import FileItem from '@/common/components/file-item'
import EmployeeCard from '@/common/components/employee-card'
import ModuleOther from '@/frontend/module/module-other'
import ModuleDynamic from '@/frontend/module/module-dynamic'
import LineCardTask from '@/frontend/project/components/line-card-task'
import $ from 'jquery'
window.openPersonalImg = function (v) { // 打开富文本图片
  let newwin = window.open()
  newwin.document.write('<img src=' + v.src + ' /><style>body{margin:50px 100px;background:#212121;position: relative;}img{max-width:100%;max-height:100%;text-align: center;position: absolute;margin: auto;top: 0;right: 0;bottom: 0;left: 0;}</style>')
}
export default {
  name: 'personalTaskDetails',
  components: {
    ModuleOtherNext, TaskApprdetails, TaskMemoDetails, otherDetail, ModuleTaskDetail, HeadImage, FileItem, ModuleOther, ModuleDynamic, EmployeeCard, LineCardTask
  },
  props: ['itemValue', 'dtlKey'],
  data () {
    return {
      bean: 'project_custom',
      projectBaseInfo: {}, // 项目基本信息
      departmentInfo: [],
      detailList: [],
      employeeDialogVisible: false,
      belongToWhichOneTask: '',
      chooseSessionQutoe: {}, // 缓存需要取消的关联任务
      delQutoeOrComplete: 0, // 取消关联或者完成
      showIscomplete: 1,
      showPersonCardnew: false, // 显示隐藏人员卡片
      cardPersonDetails: {},
      dataThisAll: {},
      openDynamic: false,
      xiezuoPerosonalList: [], // 协作人列表
      istaskOrMemoOrcuston: '',
      approvalstatusType: ['待审批', '审批中', '审批通过', '审批驳回', '已撤销', '已转交', '待提交'],
      getMemberListIsSubOrXz: '', // 是协作人打开还是子任务负责人
      projectRoleInfoList: '', // 项目权限列表
      TaskAuthRoleInfoList: [], // 任务权限
      rolTaskList: [],
      isclickSubCompalte: false,
      userInfo: {}, // 用户信息
      shareStatusismy: 0, // 用户有无点赞
      shareUserList: '', // 点赞人员
      memoDetailData: {}, // 备忘录详情
      navCrumbs: {taskGroup: {}, taskList: {}, task: {}}, // 导航面包屑，点击跳转到层级页面打开相应分组和列表
      openReference: false,
      activtionAndEditor: false, // 激活或者编辑原因填写
      isActiveData: {}, // 保存激活或者编辑原因信息
      activtionAndEditorStr: '',
      activationReason: '', // 激活原因
      showUserList: [], // 可选择的协作人列表
      addXiezuoUser: false, // 打开添加协作人弹窗
      xiezuoUserStatus: false, // 仅协作人可见
      taskDetail: {
        associatesArr: [], // 存放已经选择的协作人
        relationArr2: [],
        shareArr: [],
        complete_number: 0,
        subTaskArr: []
      }, // 任务详情数据
      subTaskUser: {}, // 子任务负责人
      subTaskName: '', // 子任务名称
      subTaskEndtime: '', // 子任务截止时间
      showOrHiddenAddSubTask: false, // 显示或隐藏新增子任务
      allTaskSaveSubmit: {},
      isParentOrSub: 0, // 判断 0：主任务，1：子任务
      subTaskList: [], // 子任务列表
      addAssociatedTask: false, // 显示或隐藏关联任务
      addAssociatedTaskList: [], // 关联任务列表
      relationTaskList: [], // 被关联任务列表
      activeName: 'basic',
      dataDetail: {}, // 自定义布局详情数据
      firstField: '',
      referenceList: [],
      checkedList: [],
      personalTaskRole: '', // 获取个人在个人任务中的角色
      referenceBean: 'basic',
      referenceLayout: [],
      token: '',
      layout: [],
      auth: [],
      employeeInfo: {},
      highSeaId: '',
      highSeasAmdin: '',
      // otherShow: {
      //   comment: true,
      //   dynamic: true,
      //   approve: false,
      //   seeState: true // 查看状态
      // },
      otherShow: {
        comment: true,
        seeState: true
      },
      moduleInfo: {},
      taskDataList: []
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.userInfo = (JSON.parse(sessionStorage.getItem('userInfo'))).employeeInfo
    // if ((this.itemValue.bean && this.itemValue.bean.indexOf('project_custom') !== -1) || this.itemValue.dataType === 2) {
    //   this.getBaseSetting(this.projectId)
    //   this.getProjectRoleInfo({eid: this.userInfo.id, projectId: this.projectId})
    // }
    if (this.itemValue.bean_name && this.itemValue.bean_name.indexOf('project_custom') !== -1 && this.itemValue.from === 1) {
      this.getProjectLayout('project_custom')
      if (this.itemValue.fromType === 1) {
        // 查询子任务
        this.isParentOrSub = 1
        this.getPersonelSubDataDetail(this.itemValue.id)
        this.getTaskRoleFromPersonelTask({taskId: this.itemValue.id, fromType: this.isParentOrSub})
        sessionStorage.setItem('isSubTaskstatus', 'false')
        sessionStorage.setItem('isguanlianTask', 'false')
      } else {
        // 查询主任务  或者  查询关联任务
        this.getPersonelDataDetail(this.itemValue.id)
        this.isParentOrSub = 0
        this.getTaskRoleFromPersonelTask({taskId: this.itemValue.id, fromType: this.isParentOrSub})
        sessionStorage.setItem('isSubTaskstatus', 'false')
        sessionStorage.setItem('isguanlianTask', 'false')
      }
    } else if (this.itemValue.dataType === 4) {
      this.getApprovalList(this.itemValue.allDetails)
    } else if (this.itemValue.dataType === 3) {
      let data = {
        bean: this.itemValue.allDetails.beanName,
        dataId: this.itemValue.allDetails.beanId,
        moduleId: '',
        moduleName: ''
      }
      this.dataThisAll = data
      this.detailList.push(data)
    }
    //  else if (this.itemValue.dataType === 2 && !this.itemValue.from) {
    //   this.taskDataList.push(this.itemValue)
    // }
  },
  methods: {
    getProjectLayout (bean) { // 获取个人任务自定义布局
      HTTPServer.getAllLayout({'bean': 'project_custom'}, 'loading').then((res) => {
        if (JSON.stringify(res) !== '{}') {
          this.layout = res.enableLayout.rows
        }
      })
    },
    getTaskRoleFromPersonelTask (data) { // 获取个人在个人任务中的角色
      HTTPServer.getTaskRoleFromPersonelTask(data, 'loading').then((res) => {
        this.personalTaskRole = res.role
      })
    },
    getPersonelDataDetail (taskId) { // 根据任务id查主任务详情
      this.shareStatusismy = 0
      HTTPServer.getPersonelDataDetail({taskId: taskId}, 'loading').then((res) => {
        if (res.relationArr && res.relationArr.length > 0) {
          res.relationArr.forEach((val, key) => {
            val.isQutoe = false
          })
        }
        // this.addAssociatedTaskList = res.relationArr // 关联
        // this.relationTaskList = res.relationArr2 // 被关联
        if (res.participants_only === '1') {
          this.xiezuoUserStatus = true
        } else {
          this.xiezuoUserStatus = false
        }
        this.taskDetail = res
        this.dataDetail = res.customLayout ? res.customLayout : {}
        this.querySubTaskByTaskId(res.id)
        this.openUserList()
        this.getPraisePerson()
        this.queryTaskAssociatesByTaskIdAndType({taskId: this.taskDetail.id, fromType: this.isParentOrSub})
        this.queryBeTaskAssociatesByTaskIdAndType(this.taskDetail.id)
        this.changeImgWidth(res.customLayout)
        setTimeout(() => {
          this.editorimg()
        }, 200)
      })
    },
    getPersonelSubDataDetail (subTaskid) { // 查询子任务详情
      HTTPServer.getPersonelSubDataDetail({taskId: subTaskid}, 'loading').then((res) => {
        // this.addAssociatedTaskList = res.relationArr // 关联
        if (res.participants_only === '1') {
          this.xiezuoUserStatus = true
        } else {
          this.xiezuoUserStatus = false
        }
        this.taskDetail = res
        this.dataDetail = res.customLayout ? res.customLayout : {}
        this.openUserList()
        this.getPraisePerson()
        this.queryTaskAssociatesByTaskIdAndType({taskId: this.taskDetail.id, fromType: this.isParentOrSub})
        this.changeImgWidth(res.customLayout)
        setTimeout(() => {
          this.editorimg()
        }, 200)
      })
    },
    changeImgWidth (customArr) { // 修改富文本图片大小
      if (customArr) {
        let reg = /<img/g
        for (let key in customArr) {
          if (key.indexOf('multitext') !== -1) { // 富文本添加点击事件
            customArr[key] = customArr[key].replace(reg, '<img onclick="openPersonalImg(this)" class="multitextInnerImg"')
          }
          if (key.indexOf('subform') !== -1) {
            customArr[key].map((v1, k1) => {
              for (let v2 in v1) {
                if (v2.indexOf('multitext') !== -1) { // 子表单富文本添加点击事件
                  if (v1[v2]) {
                    v1[v2] = v1[v2].replace(reg, '<img onclick="openPersonalImg(this)" style="cursor:pointer;" class="submultitextInnerImg"')
                  }
                }
              }
            })
          }
        }
      }
    },
    editorimg () {
      this.$nextTick(() => {
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
                  if ($(CardChildImg[j]).width() > 490) {
                    $(CardChildImg[j]).width(490)
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
    queryMembersTaskId (data) { // 获取协作人
      HTTPServer.queryMembersTaskId(data, 'Loading').then((res) => {
        this.taskDetail.associatesArr = res
        this.xiezuoPerosonalList = res
      })
    },
    getPraisePerson () { // 获取点赞人员列表
      HTTPServer.getPraisePerson({taskId: this.itemValue.id, fromType: this.isParentOrSub}, 'Loading').then((res) => {
        // this.taskDetail.shareArr = res
        this.$set(this.taskDetail, 'shareArr', res)
        let arr = []
        if (res && res.length > 0) {
          res.map((v, k) => {
            arr.push(v.employee_name)
            if (v.employee_id === this.userInfo.id) {
              this.shareStatusismy = 1
            }
          })
          this.shareUserList = arr.join('、') + '点赞'
        } else {
          this.shareStatusismy = 0
          this.shareUserList = '暂无点赞'
        }
      })
    },
    getMemoDetail (id) { // 获取备忘录详情
      HTTPServer.findMemoDetail({id: id}, true).then((res) => {
        console.log(res, '备忘录详情')
        this.memoDetailData = res
      })
    },
    openPersonCard (v, e) { // 打开人员卡片弹窗
      this.cardPersonNewDetails = v
      this.cardPersonDetails = {}
      this.getqueryEmployeeInfo(v, e.currentTarget)
    },
    getqueryEmployeeInfo (v, event) { // 获取成员详细信息
      HTTPServer.queryProjectEmployee({employeeId: v.employee_id}).then((res) => {
        res.superior = res.main_employee_name
        this.cardPersonDetails = res
        this.departmentInfo = [{
          department_name: res.department_name
        }]
        this.employeeDialogVisible = true
        setTimeout(() => {
          let ele1 = document.getElementsByClassName('myTaskEmployeePersonal')[this.dtlKey]
          let ele = document.getElementsByClassName('v-modal')[0]
          ele.style.background = 'transparent'
          let offset = $(event).offset()
          let width = $(event).width()
          let height = $(event).height()
          let mmWidth = document.body.clientWidth
          let mmHeight = document.body.clientHeight
          let left = offset.left + width + 10
          let top = offset.top - 97 + (height / 2)
          if (offset.left + width + 378 > mmWidth) {
            left = offset.left - 378
          }
          if (offset.top + 98 + (height / 2) > mmHeight) {
            top = mmHeight - 195
          }
          $(ele1).children().css('margin', top + 'px 0 0 ' + left + 'px')
        }, 10)
      })
    },
    openEmailOrMemo (v) { // 发邮件或者聊天
      this.$message({
        message: '功能正在开发中',
        type: 'warning'
      })
    },
    cancelQutoe () { // 取消关联
      this.delQutoeOrComplete = 1
    },
    completeQutoe () { // 完成取消关联
      if (JSON.stringify(this.chooseSessionQutoe) !== '{}') {
        this.cancelGuanlian(this.chooseSessionQutoe)
      } else {
        this.delQutoeOrComplete = 0
      }
    },
    chooseQutoe (v) { // 选择取消关联的数据
      if (this.delQutoeOrComplete === 0) {
        return false
      }
      this.chooseSessionQutoe = {}
      let value = JSON.parse(JSON.stringify(v))
      this.addAssociatedTaskList.forEach((val, key) => {
        val.moduleDataList.map((v1, k1) => {
          v1.isQutoe = false
        })
      })
      v.isQutoe = !value.isQutoe
      this.addAssociatedTaskList.forEach((val, key) => {
        val.moduleDataList.map((v1, k1) => {
          if (v1.isQutoe) {
            this.chooseSessionQutoe = v
          }
        })
      })
    },
    getMemberList (str) { // 添加协作人
      let arr = []
      this.xiezuoPerosonalList.map((v, k) => {
        arr.push({
          name: v.employee_name,
          picture: v.picture,
          id: v.employee_id
        })
      })
      let senddata = {
        type: 3, 'prepareData': arr, 'prepareKey': 'personalTask', 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': []
      }
      this.$bus.emit('commonMember', senddata)
    },
    getProjectRoleInfo (data) { // 获取项目权限
      HTTPServer.queryManagementRoleInfo(data, 'Loading').then((res) => {
        // 缓存项目的权限
        if (res && res.priviledge) {
          this.projectRoleInfoList = res.priviledge.priviledgeList
          if (this.projectRoleInfoList[20].status !== 1) {
            this.otherShow.comment = false
          }
        }
      })
    },
    getTaskAuth (id) { // 获取任务权限
      HTTPServer.queryTaskAuthList({id: id}, 'Loading').then((res) => {
        this.TaskAuthRoleInfoList = res
      })
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
    getApprovalList (senddata) { // 获取审批详情
      let _this = this
      this.$nextTick(function () {
        _this.$refs.TaskApprdetails.apprDetail(senddata)
      })
    },
    shareThumbs (v) { // 点赞
      // if (this.personalTaskRole === 2) {
      //   return false
      // }
      let senddata = {
        task_id: this.itemValue.id,
        status: v,
        from_type: this.isParentOrSub // 点赞类型，0 分享，1任务，2子任务
      }
      HTTPServer.savePraiseRecordData(senddata, 'Loading').then((res) => {
        if (!this.taskDetail.fromType) {
          // this.getPersonelDataDetail(this.itemValue.id)
          this.getPraisePerson()
          this.isParentOrSub = 0
        } else {
          // 查询子任务
          // this.getPersonelSubDataDetail(this.itemValue.id)
          this.isParentOrSub = 1
          this.getPraisePerson()
          sessionStorage.setItem('isSubTaskstatus', 'false')
        }
      })
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
    // 关闭详情弹框
    closeDialog () {
      this.$bus.emit('closePersonalTaskModal', JSON.stringify({dtlKey: this.dtlKey}))
    },
    fromSubTaskToTask () { // 子任务打开主任务详情
      let senddata = {dtlKey: this.dtlKey}
      if (this.taskDetail.task_id) {
        senddata.bean_name = this.taskDetail.bean_name
        senddata.task_id = this.taskDetail.task_id
      }
      this.$bus.emit('closePersonalTaskModal', JSON.stringify(senddata))
    },
    // 打开编辑窗口
    openEdit () {
      // let modules = {bean: this.bean, moduleName: this.moduleName, type: 3, plist_relyon: 1}
      // this.$bus.emit('openCreateModal', modules, this.dataDetail)
    },
    handleControl (common) { // 更多操作
      if (common.type === 5) {
        this.$confirm('删除任务后，所有子任务也会被删除。', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => { // 确定删除
          if (this.isParentOrSub === 0) {
            HTTPServer.deletePersonelData({ids: this.itemValue.id}, '').then((res) => {
              this.$message({ type: 'success', message: '删除成功!' })
              this.$bus.$emit('delCompleteUpdata', 'getPerosonalList')
              this.closeDialog()
            })
          } else if (this.isParentOrSub === 1) {
            HTTPServer.deletePersonelSubData({ids: this.itemValue.id}, '').then((res) => {
              this.$message({ type: 'success', message: '删除成功!' })
              this.$bus.$emit('delCompleteUpdata', 'getPerosonalList')
              this.closeDialog()
            })
          }
        }).catch(() => { // 取消删除
          console.log('')
        })
      } else if (common.type === 6) {
        this.openDynamic = true
      } else {
        this.topEditorAside = true
        this.$bus.emit('nextEditorAside', JSON.stringify({
          type: common.type,
          taskId: this.itemValue.id,
          navCrumbs: this.navCrumbs,
          taskDetail: this.taskDetail,
          isPersonalProject: true,
          isParentOrSub: this.isParentOrSub
        }))
      }
    },
    changeXiezuoren () { // 协作人是否可见switch
      let senddata = {
        participants_only: this.xiezuoUserStatus ? 1 : 0,
        task_id: this.taskDetail.id,
        from_type: this.isParentOrSub
      }
      HTTPServer.updateTaskPersonelMemberData(senddata, '').then((res) => {
        // console.log(res)
        this.taskDetail.participants_only = senddata.participants_only + ''
      })
    },
    // 图片打开预览
    openPreview (file) {
      file.fileForm = true
      file.file_url += '&TOKEN=' + this.token
      this.$bus.emit('file-preview', file)
    },
    // 数字组件格式
    numberTo (value, list, name) {
      let property
      list.map((item) => {
        if (item.name === name) {
          property = item.field
        }
      })
      let number = tool.numberFormat(value, property)
      return number
    },
    // 公式组件格式
    formatFormuls (value, list, name) {
      if (value) {
        let property
        list.map((item) => {
          if (item.name === name) {
            property = item.field
          }
        })
        let number = tool.formulsFormat(value, property)
        return number
      }
    },
    // 日期组件格式
    formatDateTo (value, list, name) {
      let property
      list.map((item) => {
        if (item.name === name) {
          property = item.field.formatType
        }
      })
      let number = tool.formatDate(value, property)
      return number
    },
    // 关联关系跳转
    goReferenceModel (item, id) {
      // let data = {
      //   bean: item.relevanceModule.moduleName
      // }
      // this.ajaxGetMoudleId(data, item.relevanceModule.moduleLabel)
      if (id === -1) {
        if (item.from_status === '1') { // 项目
          sessionStorage.setItem('chooseProName', JSON.stringify({name: '', id: ''}))
          this.$router.push({path: '/frontend/project/detailsMain', query: {projectId: item.relation_id}})
          sessionStorage.setItem('storageProjectId', item.relation_id)
        } else if (item.from_status === '2') { // 自定义
          let senddata = {
            bean: item.bean_name,
            dataId: item.relation_id,
            moduleName: item.moduleName
          }
          this.ajaxGetMoudleId(senddata, '')
        }
      } else {
        let data = {
          bean: item.relevanceModule.moduleName,
          dataId: id,
          moduleName: item.relevanceModule.moduleLabel
        }
        this.ajaxGetMoudleId(data, item.relevanceModule.moduleLabel)
      }
    },
    // 子表单关联关系跳转
    childGoReferenceModel (list, name, id) {
      let property
      list.map((item) => {
        if (item.name === name) {
          property = item
        }
      })
      let data = {
        bean: property.relevanceModule.moduleName,
        dataId: id,
        moduleName: property.relevanceModule.moduleLabel
      }
      this.ajaxGetMoudleId(data, property.relevanceModule.moduleLabel)
      // let data = {
      //   bean: property.relevanceModule.moduleName
      // }
      // this.ajaxGetMoudleId(data, property.relevanceModule.moduleLabel)
    },
    // 根据bean获取模块ID
    ajaxGetMoudleId (data, name) {
      HTTPServer.getMoudleIdByBean(data, 'Loading').then((res) => {
        // 跳转到关联关系模块
        data.moduleId = res
        data.dataType = 3
        data.beanName = data.bean
        data.beanId = data.dataId
        data.module_id = data.moduleId
        data.module_name = data.moduleName
        this.openDeAllTypesdetalis(data)
        // window.open(window.location.origin + `#frontend/module?bean=${data.bean}&moduleId=${res}&moduleName=${name}`)
      })
    },
    // AJAX获取权限列表
    ajaxGetAuthList (data) {
      HTTPServer.getFunAuthList(data, 'Loading').then((res) => {
        this.auth = res
      })
    },
    // 获取任务自定义数据详情
    ajaxGetProjectData (data) {
      HTTPServer.queryProjectDataDetail(data, 'Loading').then((res) => {
        this.dataDetail = res
      })
    },
    // AJAX获取数据详情-关联关系
    ajaxGetDataDtlForReference (data, modules) {
      HTTPServer.customDtlData(data, 'Loading').then((res) => {
        // this.referenceDataDtl = res
        this.$bus.emit('openCreateModal', modules, res)
      })
    },
    // AJAX获取关联关系数组
    ajaxGetReferenceList (data) {
      HTTPServer.customDtlRelationData(data, 'Loading').then((res) => {
        this.referenceList = res.refModules
      })
    },
    // AJAX获取布局
    ajaxGetEnableLayout (data) {
      if (this.highSeaId) {
        data.isSeasPool = this.highSeaId
      }
      HTTPServer.getEnableLayout(data, 'Loading').then((res) => {
        this.moduleInfo = {
          moduleName: res.title,
          moduleId: res.moduleId,
          moduleIcon: res.icon
        }
        this.layout = res.layout
        this.otherShow.comment = res.commentControl === '1'
        this.otherShow.dynamic = res.dynamicControl === '1'
        this.otherShow.approve = res.processId !== null
        this.firstField = res.layout[0].rows[0].name ? this.layout[0].rows[0].name : 'id'
      })
    },
    openUserList () { // 获取协作人
      let senddata = {
        taskId: this.taskDetail.id,
        fromType: this.isParentOrSub
      }
      this.queryMembersTaskId(senddata)
    },
    sliceName (name) {
      if (name) {
        return name.slice(-2)
      } else {
        return name
      }
    },
    addSubTask () { // 打开添加子任务
      this.showOrHiddenAddSubTask = true
      this.subTaskName = ''
      this.subTaskEndtime = ''
      this.subTaskUser = {}
    },
    addSubTaskUser () { // 新建子任务添加负责人
      let senddata = {
        type: 1, 'prepareData': [this.subTaskUser], 'prepareKey': 'personalSubTask', 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': []
      }
      this.$bus.emit('commonMember', senddata)
    },
    changeEndTime (v) { // 新增子任务事件确定
      // console.log(v)
      if (this.taskDetail.customLayout.datetime_deadline) {
        if (this.subTaskEndtime > this.taskDetail.customLayout.datetime_deadline) {
          this.subTaskEndtime = ''
          this.$message.error('子任务截止时间必须小于主任务截止时间！')
          return false
        }
      }
    },
    uditorTime (v) { // 时间转换成时间戳
      let time = new Date(v)
      return time.getTime()
    },
    openOtherDetails (v) { // 查子任务详情
      sessionStorage.setItem('isSubTaskstatus', 'true')
      sessionStorage.setItem('isguanlianTask', 'false')
      // v.id = v.id
      v.bean_name = 'project_custom'
      v.beanName = 'project_custom'
      v.fromType = 1
      this.$bus.emit('openPersonalTaskDetails', JSON.stringify(v))
    },
    addTaskQuote () { // 打开添加关联弹窗
      this.$bus.$emit('addTaskOpen', JSON.stringify({status: 'open', personalTask: true}))
      // 用于判断是关联任务还是新建任务
      sessionStorage.setItem('isAddOrRelationTask', 'true')
      sessionStorage.setItem('belongToWhichOneTask', this.itemValue.id)
    },
    delSubTask (v, index) { // 删除子任务
      this.$confirm('确定删除子任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        HTTPServer.deletePersonelSubData({ids: v.id}, 'Loading').then((res) => {
          this.$message.success('删除成功!')
          this.querySubTaskByTaskId(v.task_id)
          this.openUserList()
        })
      }).catch(() => {})
    },
    cancelGuanlian (v) { // 取消关联
      this.$confirm('确定取消该关联任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        HTTPServer.deleteAssociatesData({ids: v.associatesTaskInfoId, fromType: this.isParentOrSub, taskId: this.itemValue.id}, 'Loading').then((res) => {
          this.$message.success('取消关联成功!')
          this.queryTaskAssociatesByTaskIdAndType({taskId: this.taskDetail.id, fromType: this.isParentOrSub})
          this.delQutoeOrComplete = 0
          this.chooseSessionQutoe = {}
        })
      }).catch(() => {
        this.addAssociatedTaskList.forEach((val, key) => {
          val.moduleDataList.map((v1, k1) => {
            v1.isQutoe = false
          })
        })
        this.chooseSessionQutoe = {}
      })
    },
    isActivation (str, value, item, status) { // 打开激活或编辑原因弹窗，完成任务
      if (this.personalTaskRole === 2 && this.personalTaskRole === 3) {
        return false
      }
      if (value === 0) {
        this.isActiveData = this.taskDetail
        this.isActiveData.activeStatus = status
      } else {
        this.isActiveData = item
        this.isActiveData.activeStatus = status
      }
      if (status === 'sub') {
        this.isActiveData.fromType = 1
      }
      let html = ''
      if (this.isActiveData.complete_status === '1') {
        html = '确定激活该任务吗?'
      } else {
        html = '确定完成该任务吗?'
      }
      let senddata = {
        taskId: parseInt(this.isActiveData.id)
      }
      this.$confirm(html, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.saveIscompleteOrActive(senddata)
      }).catch(() => { console.log('') })
    },
    saveRElationDetail () { // 激活原因保存
      let senddata = {
        taskId: parseInt(this.isActiveData.id)
      }
      this.saveIscompleteOrActive(senddata)
    },
    queryBeTaskAssociatesByTaskIdAndType (id) { // 查询被关联数据
      HTTPServer.queryBeTaskAssociatesByTaskIdAndType({taskId: id}, 'Loading').then((res) => {
        res.dataList.map((v, k) => {
          v.start_time_task = this.getStartTimeTask(v)
        })
        this.taskDetail.relationArr2 = res.dataList
        this.relationTaskList = res.dataList
      })
    },
    saveIscompleteOrActive (data) { // // 打开激活或编辑原因弹窗，完成任务
      if (this.isActiveData.fromType === 0) {
        HTTPServer.updateForFinish(data, 'Loading').then((res) => { // 主任务
          this.getsubguanliandata()
        })
      } else {
        HTTPServer.updateSubForFinish(data, 'Loading').then((res) => { // 子任务
          this.getsubguanliandata()
        })
      }
    },
    getsubguanliandata () {
      // 查询主任务  或者  查询关联任务
      switch (this.isActiveData.activeStatus) {
        case 'sub':
          this.querySubTaskByTaskId(this.itemValue.id)
          this.isclickSubCompalte = true
          break
        case 'guanlian':
          this.queryTaskAssociatesByTaskIdAndType({taskId: this.taskDetail.id, fromType: this.isParentOrSub})
          break
        case 'beiguanlian':
          this.queryBeTaskAssociatesByTaskIdAndType(this.itemValue.id)
          break
        default:
          if (this.isParentOrSub === 0) {
            this.getPersonelDataDetail(this.itemValue.id)
          } else {
            this.getPersonelSubDataDetail(this.itemValue.id)
          }
      }
      sessionStorage.setItem('isSubTaskstatus', 'false')
      sessionStorage.setItem('isguanlianTask', 'false')
    },
    saveSubTask () { // 保存子任务
      let senddata = {
        task_id: this.itemValue.id,
        name: this.subTaskName, // 任务名称
        end_time: this.subTaskEndtime, // 截止时间
        executor_id: this.subTaskUser.id, // 负责人id
        project_custom_id: this.taskDetail.project_custom_id,
        bean_name: this.taskDetail.bean_name,
        relation_id: this.taskDetail.relation_id,
        relation_data: this.taskDetail.relation_data
      }
      if (!senddata.name || senddata.name.length > 500) {
        this.$message.error('子任务名称不能超过500个字,且为必填')
        return false
      }
      if (!senddata.end_time) {
        this.$message.error('子任务截止时间必填')
        return false
      }
      if (this.dataDetail.datetime_starttime) {
        if (this.dataDetail.datetime_starttime > senddata.end_time) {
          this.$message.error('子任务开始时间必填且必须大于主任务开始时间')
          return false
        }
      }
      if (this.dataDetail.datetime_deadline) {
        if (this.dataDetail.datetime_deadline < senddata.end_time) {
          this.$message.error('子任务截止时间必须小于主任务截止时间')
          return false
        }
      }
      if (!senddata.executor_id) {
        this.$message.error('子任务负责人为必填')
        return false
      }
      HTTPServer.savePersonelSubData(senddata, 'Loading').then((res) => {
        this.subTaskName = ''
        this.subTaskUser = {}
        this.subTaskEndtime = ''
        console.log(res)
        this.querySubTaskByTaskId(senddata.task_id)
        this.openUserList()
      })
    },
    querySubTaskByTaskId (taskId) { // 获取任务下子任务列表
      HTTPServer.querySubTaskByTaskId({taskId: taskId}, 'Loading').then((res) => {
        this.taskDetail.progressCount = 0 // 子任务完成个数
        if (res && res.length > 0) {
          res.forEach((val, key) => {
            if (val.complete_status === '1') {
              this.taskDetail.progressCount++
            }
            if (val.complete_status === '0') {
              this.showIscomplete = 0
            }
          })
          if (this.taskDetail.progressCount === res.length && this.isclickSubCompalte) {
            this.getPersonelDataDetail(this.itemValue.id)
            this.isclickSubCompalte = false
          } else {
            if (this.taskDetail.progressCount === res.length) {
              this.showIscomplete = 1
            }
            this.taskDetail.progressPercentage = (this.taskDetail.progressCount / res.length).toFixed(2) * 100 // 子任务完成的进度百分比
            this.$set(this.taskDetail, 'subTaskArr', res)
          }
        } else {
          this.$set(this.taskDetail, 'subTaskArr', [])
        }
      })
    },
    openDeAllTypesdetalis (v) { // 打开不同分类的详情
      // let data = {id: 2, bean: 'bean1525160417234'}
      sessionStorage.setItem('isSubTaskstatus', 'false')
      sessionStorage.setItem('isguanlianTask', 'true')
      if (v.beanName.indexOf('project_custom') !== -1) {
        if (v.beanName.indexOf('project_custom_') !== -1) {
          // this.$bus.$emit('openOtherDetails', JSON.stringify(v))
          this.$bus.$emit('fromPersonalTaskToProjectTask', JSON.stringify(v))
        } else {
          v.fromType = 0
          this.$bus.$emit('openPersonalTaskDetails', JSON.stringify(v))
        }
      } else {
        this.$bus.$emit('openPersonalTaskDetails', JSON.stringify(v))
      }
    },
    editorTask (v, status) { // 编辑任务
      sessionStorage.setItem('getSearchTaskId', v.id)
      this.$bus.$emit('openPersonalTaskCreate', JSON.stringify({status: 'task', isEditorTaskData: true, editor: this.taskDetail}))
      sessionStorage.setItem('listTaskAddTask', 'editor')
      if (status === 1) {
        sessionStorage.setItem('isSubTaskstatus', 'true')
      } else {
        sessionStorage.setItem('isSubTaskstatus', 'false')
      }
    },
    saveRelationTask (quoteId) { // 新建关联保存
      this.istaskOrMemoOrcuston = sessionStorage.getItem('istaskOrMemoOrcuston')
      let senddata = {
        'projectId': this.projectId,
        'relation_id': this.allTaskSaveSubmit.relation_id, // 数据id
        'bean_name': this.allTaskSaveSubmit.bean, // this.taskDetail.bean_name, // 模块的bean
        'task_id': this.itemValue.id, // 任务id
        'from_type': this.isParentOrSub // 0 属于任务的关联 ，1属于子任务的关联
      }
      if (this.taskDetail.bean_name && this.taskDetail.bean_name.indexOf('project_custom') !== -1) {
        if (sessionStorage.getItem('taskNameAddAndEditor')) {
          let obj = JSON.parse(sessionStorage.getItem('taskNameAddAndEditor'))
          senddata.taskName = obj.taskName
        }
        if (this.istaskOrMemoOrcuston === 'task') {
          senddata.bean_name = 'project_custom'
        }
        senddata.associatesStatus = this.allTaskSaveSubmit.associatesStatus
      }
      if (this.istaskOrMemoOrcuston === 'memorandum' || this.istaskOrMemoOrcuston === 'memo') {
        senddata.bean_name = 'memo'
      }
      if (this.istaskOrMemoOrcuston === 'custom') {
        senddata.bean_name = this.allTaskSaveSubmit.bean
      }
      if (quoteId) {
        senddata.relation_id = quoteId
      }
      HTTPServer.saveAssociatesData(senddata, 'Loading').then((res) => { // 新增关联保存
        this.$bus.$emit('addSuccess')
        this.$bus.$emit('closeProTaskCreateModal', 'personalAddTask')
        this.queryTaskAssociatesByTaskIdAndType({taskId: this.taskDetail.id, fromType: this.isParentOrSub})
      })
      sessionStorage.setItem('isAddOrRelationTask', 'false')
    },
    saveAssociatesData (data) { // 关联新建引用的保存
      let senddata = {
        'bean_name': data.bean,
        'module_id': data.personalData.moduleId,
        'module_name': data.personalData.moduleName,
        'relation_id': data.quoteTaskId,
        'task_id': this.itemValue.id,
        'from_type': this.isParentOrSub, // 0 属于任务的关联 ，1属于子任务的关联
        // 'taskType': data.isPersonalOrproject, // 0 个人任务，1项目任务
        'projectId': data.personalData.projectId
      }
      if (data.bean === 'project_curstom') {
        senddata.bean_name = 'project_curstom'
        senddata.taskType = data.isPersonalOrproject
      }
      HTTPServer.saveAssociatesData(senddata, 'Loading').then((res) => { // 引用任务保存
        this.$bus.$emit('addSuccess')
        this.queryTaskAssociatesByTaskIdAndType({taskId: this.taskDetail.id, fromType: this.isParentOrSub})
      })
      sessionStorage.setItem('isAddOrRelationTask', 'false')
    },
    queryTaskAssociatesByTaskIdAndType (data) { // 获取任务下关联数据
      HTTPServer.queryTaskAssociatesByTaskIdAndType(data, 'Loading').then((res) => { // 引用任务保存
        res.dataList.forEach((val, key) => {
          val.moduleDataList.map((v1, k1) => {
            v1.isQutoe = false
          })
        })
        this.addAssociatedTaskList = res.dataList
      })
    },
    saveRemindPerson (arr) { // 添加保存协作人
      let newarr = []
      arr.map((v, k) => {
        let id = v.employee_id ? v.employee_id : v.id
        newarr.push(id)
      })
      let data = {
        fromType: this.isParentOrSub,
        taskId: this.taskDetail.id,
        employeeIds: newarr.join(',')
      }
      HTTPServer.savePersonelMemberData(data, 'Loading').then((res) => {
        this.$message({ type: 'success', message: '保存成功!' })
        this.addXiezuoUser = false
        this.openUserList()
      })
    },
    delXiezuoUser (v, k) { // 删除协作人
      this.$confirm('确定删除该协作人吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let arr = []
        this.xiezuoPerosonalList.map((val, key) => {
          if (val.employee_id !== v.employee_id) {
            arr.push(val)
          }
        })
        this.saveRemindPerson(arr)
        // let data = {
        //   fromType: this.isParentOrSub,
        //   taskId: this.taskDetail.id,
        //   employeeIds: v.id
        // }
        // HTTPServer.deletePersonelMemberData(data, 'Loading').then((res) => {
        //   this.$message({ type: 'success', message: '删除成功!' })
        //   this.openUserList()
        // })
      }).catch(() => { console.log('') })
    },
    downloadFile (data, filename) { // 下载附件
      var saveLink = document.createElementNS('http://www.w3.org/1999/xhtml', 'a')
      saveLink.href = data + '&definitionFileName=' + filename
      saveLink.download = filename
      var event = document.createEvent('MouseEvents')
      event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null)
      saveLink.dispatchEvent(event)
    },
    edtiorName (name, status) {
      if (name) {
        let newname = status === 2 ? name.slice(-2) : name.slice(-3)
        return newname
      }
    },
    filterEditor (html) { // 过滤富文本
      if (html) {
        let newHtml = html.replace(/<\/?(img|a|p|b)[^>]*>/gi, '')
        return newHtml
      }
    },
    editorTime (time) {
      if (time) {
        let newtime = tool.formatDate(time, 'yyyy-MM-dd HH:mm')
        return newtime.slice(5)
      }
    },
    judgeTimeOld (time) { // 判断任务有无过期
      if (time) {
        let newtime = new Date()
        return newtime.getTime() > time
      }
    },
    getStartTimeTask (v) { // 获取开始时间
      this.layout.map((val, key) => {
        if (val.label === '开始时间') {
          return v[val.name]
        }
      })
    },
    showDatePosition () {
      setTimeout(() => {
        let ele = document.getElementsByClassName('personalTaskDetailsClass')[0]
        ele.style.left = (ele.offsetLeft - 35) + 'px'
      }, 50)
    },
    // pc显示
    showPC (list, name) {
      let property
      list.map((item) => {
        if (item.name === name) {
          property = item.field.terminalPc
        }
      })
      return property === '1'
    }
  },
  mounted () {
    this.showOrHiddenAddSubTask = false
    this.$bus.on('memoSaveSuccess', (res) => { // 备忘录/任务/审批/自定义新建关联保存成功后返回
      let belongToWhichOneTask = parseInt(sessionStorage.getItem('belongToWhichOneTask'))
      if (sessionStorage.getItem('isAddOrRelationTask') === 'true') {
        if (belongToWhichOneTask === parseInt(this.itemValue.id)) {
          let data = JSON.parse(res)
          console.log(data)
          this.allTaskSaveSubmit.bean = data.bean
          this.allTaskSaveSubmit.relation_id = data.dataId
          this.saveRelationTask()
        }
      }
    })
    this.$bus.on('quoteRelationTask', (res) => { // 引用关联新建
      let belongToWhichOneTask = parseInt(sessionStorage.getItem('belongToWhichOneTask'))
      if (belongToWhichOneTask === parseInt(this.itemValue.id)) {
        let data = JSON.parse(res)
        this.saveAssociatesData(data)
      }
    })
    // 子任务选中负责人
    // this.$bus.off('selectMemberRadio')
    this.$bus.on('selectMemberRadio', (data) => {
      if (data.prepareKey === 'personalSubTask') {
        this.subTaskUser = data.prepareData[0]
      }
    })
    // 更新详情数据
    this.$bus.on('updataPersonalDetailsData', (res) => {
      let data = JSON.parse(res)
      if (sessionStorage.getItem('isSubTaskstatus') === 'true') {
        // 查询子任务
        if (this.itemValue.id === data.id) {
          this.getPersonelSubDataDetail(this.itemValue.id)
          this.isParentOrSub = 1
          sessionStorage.setItem('isSubTaskstatus', 'false')
        }
      } else {
        // 查询主任务
        if (this.itemValue.id === data.id) {
          this.getPersonelDataDetail(this.itemValue.id)
          this.isParentOrSub = 0
        }
      }
    })
    this.$bus.on('selectEmpDepRoleMulti', (data) => {
      if (data.prepareKey === 'personalTask') {
        // this.$set(this.taskDetail, 'associatesArr', data.prepareData)
        // this.taskDetail.associatesArr = data.prepareData
        // this.xiezuoPerosonalList = data.prepareData
        this.saveRemindPerson(data.prepareData)
      }
    })
    this.$bus.on('personalQuoteTaskAdd', (res) => { // 任务关联添加
      let belongToWhichOneTask = parseInt(sessionStorage.getItem('belongToWhichOneTask'))
      if (belongToWhichOneTask === parseInt(this.itemValue.id)) {
        let data = JSON.parse(res)
        this.allTaskSaveSubmit.bean = data.bean
        this.allTaskSaveSubmit.relation_id = data.dataId
        this.saveRelationTask()
      }
    })
    // 打开项目任务详情
    // this.$bus.on('openOtherDetails', (res) => { // 打开任务详情
    //   let data = JSON.parse(res)
    //   let sendata = {
    //     id: data.id,
    //     bean: data.beanName,
    //     task_id: data.task_id,
    //     dataType: data.dataType
    //   }
    //   if (data.beanName.indexOf('project_custom') !== -1) {
    //     this.projectId = data.beanName.split('_')[2]
    //     sendata.id = data.taskInfoId
    //   }
    //   if (sessionStorage.getItem('isguanlianTask') === 'true' && data.beanName.indexOf('project_custom') !== -1 && data.quoteTaskId && data.quoteTaskId !== -1) {
    //     // sendata.id = data.moduleId
    //     sendata.id = data.quoteTaskId
    //   }
    //   if (data.dataType === 4 || data.dataType === 3) {
    //     sendata.allDetails = data
    //   }
    //   this.taskDataList.push(sendata)
    // })
    // if (this.itemValue.dataType === 3) {
    // // 打开详情窗口
    //   this.$bus.on('openDataDtl', (value, id, bean) => { // 自定义详情
    //     let data = {
    //       bean: bean,
    //       dataId: id,
    //       moduleId: this.itemValue.module_id,
    //       moduleName: this.itemValue.module_name,
    //       highSeaId: '',
    //       highSeasAmdin: ''
    //     }
    //     this.data = data
    //     this.detailList.push(data)
    //   })
    //   // 关闭自定义详情窗口
    //   // this.$bus.off('closeDetailModal')
    //   this.$bus.on('closeDetailModal', value => {
    //     this.detailList.splice(value, 1)
    //     if (this.detailList.length === 0) {
    //       this.$bus.emit('closeTaskModal', JSON.stringify({dtlKey: this.dtlKey}))
    //     }
    //   })
    // }
    this.$bus.on('closeDynamicModal', (value) => {
      if (value) {
        this.openDynamic = false
      }
    })
    setTimeout(() => {
      this.editorimg()
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
    isArea () {
      if (this.firstField.includes('area')) {
        return true
      } else {
        return false
      }
    }
  },
  beforeDestroy () {
    // this.$bus.off('memoSaveSuccess')
    this.$bus.off('quoteRelationTask')
    this.$bus.off('updataPersonalDetailsData')
    // this.$bus.off('istaskOrMemoOrcuston')
    this.$bus.off('openDataDtl')
    // this.$bus.off('closeDetailModal')
    this.$bus.off('selectEmpDepRoleMulti')
    // this.$bus.off('openOtherDetails')
    this.$bus.off('personalQuoteTaskAdd')
  }
}
</script>

<style lang="scss" scoped>
.personnel{
  padding-top:0;
}
.personCardcssnext{
  position: absolute;left:-330px;top:40%;z-index:5;min-height:195px;width:360px;background: #FFFFFF;box-shadow: -2px -2px 4px 0 rgba(155,155,155,0.30), 2px 2px 4px 0 rgba(155,155,155,0.30);border-radius:5px;
  >div.firstchildDiv{
    padding:20px 0;display: flex;padding-bottom:10px;
    .topPicAddName{min-height:117px;}
    .personPic{
      width:120px;text-align: center;border-right:1px solid #ddd;height:160px;
      >div:first-child{
        .peoplePic{
          height:50px;width:50px;border-radius: 50%;text-align:center;line-height:50px;display:inline-block;
          p{color:#fff;background: #37AEFF;border-radius: 50%;}
          img{border-radius: 50%;width:100%;height:100%;}
        }
      }
      >div:last-child{
        display: flex;text-align:center;margin-top:15px;padding-left:10px;
        >div{cursor:pointer;border-radius:3px;width:40px;height:25px;line-height:25px;margin:0 5px;background: #549AFF;i{color: #fff;}}
        >div:last-child{i{font-size:18px;}}
      }
    }
    .depInfo{
      width:240px;padding:0 10px;
      >p{margin-bottom:15px;span:first-child{color:#A0A0AE;}}
    }
  }
  >div.secondchildDiv{
    height:40px;line-height: 40px;padding:0 20px;background: #F7F9FA;
    span{cursor: pointer;}
    .delPerer{float:right;color:#FA5555;}
    .editorPer{
      color:#888;
    }
  }
}
.iconCustomRx{
  font-size: 14px;margin-right:10px;color:#69696C;
}
.icon-shenpi-shijian.iconCustomRx{transform: rotate(-90deg);}
.icon-fuzhi.iconCustomRx{font-size: 16px;}
.subfourItemIn{pointer-events:none;cursor:default;opacity:.6;}
.subfourItemOut{pointer-events:auto;cursor:pointer;opacity:1;}
.checkAndTitle >div:first-child span.chooseCheckedActive,.checkedActive.chooseCheckedActive{background: #1890FF;border-color:#1890FF;>i{color:#fff;}}
.all-pain-box{
  height: 93vh;
  overflow-y: auto;
  overflow-x: hidden;
}
.xiezuoUser{
  height:30px;line-height: 30px;>div:first-child{float:right;>span{margin-right:20px;color:#ccc;}}
}
.xzuserList{
  padding:10px 0 10px 20px;
  >span{float:left;height:30px;width:30px;text-align: center;line-height: 30px;}
  >span:last-child{border:1px solid #ddd;color:#ccc;border-radius: 50%;&:hover{cursor: pointer;}}
  >span.peopleList{
    margin:0 14px 10px 0;position: relative;&:hover{cursor: pointer;>i.icon-pc-shanchu{display: block;}}
    >span{float:left;height:30px;width:30px;border-radius: 50%;overflow:hidden;}
    span.imgCss{img{width:100%;height:100%;border-radius: 50%;vertical-align: sub;}}
    span.nameList{background: #1890FF;color:#fff;text-align: center;line-height: 30px;font-size:12px;}
    i{position: absolute;top:-12px;right:-2px;font-size:12px;color:#CDCDCD;display: none;height:20px;}
  }
}
.addSubTaskAndQuoteNew{
  display: flex;padding-left:25px;margin-top:10px;
  span{color:#1890FF;&:hover{cursor: pointer;}}
  i{font-size:15px;color:#1890FF;margin-right:10px;font-weight: bold;}
}
.xiezuoPerson{
  border-top:1px dashed #ddd;
  border-bottom:1px dashed #ddd;
  margin: 0 20px;
  padding: 15px 0 5px 0;
}
.subTaskSpan{float: left;margin-left:5px;width:300px;white-space:nowrap;text-overflow: ellipsis;overflow: hidden;&:hover{color:#008FE5;}}
.xzuserList:after{content:'';display:table;clear:both;}
.addSubTask{
  padding:10px 20px;
  .subTaskHeader{
    margin:5px 0;
    >span.addspanstyle{
      color:#BBBBC3;font-size:14px;
    }
    >span:first-child{float:right;&:hover{cursor: pointer;color:#1890FF;}>i{margin-right:10px;font-weight: bold;}}
    >span:nth-child(2){i{color:#C1C1C8;margin-right:10px;}}
    .subTaskProgress{display: inline-block;width:380px;margin-left:20px;}
    .progressSub{margin-left:10px;color:#ccc;font-size:12px;>span{font-size:12px;}>span:first-child{color:#4FCCAF;}}
  }
  .subTaskInput{
    position: relative;margin:10px 0 0 25px;min-height:40px;line-height:40px;border: 1px solid #ddd;border-radius: 5px;background: #FBFBFB;padding:0 10px 0 10px;&:hover{cursor: pointer;}
    .subTaskInputName{
      display: inline-block;width:480px;margin-left:10px;
      .line-clamp-span{
        word-break: break-all;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        height: 40px;
        line-height: 21px;
      }
    }
    .rightAdduserAndTime{
      height:40px;line-height:40px;>div{float:left;}float:right;
      >div:last-child{&:hover{cursor:pointer;}margin-left:10px;>span.addUserIcon{display: inline-block;width:24px;height:24px;line-height: 24px;text-align: center;border-radius: 50%;border: 1px solid #ccc;i{font-size:12px;}}}
      >div:first-child{
        >i{font-size:22px;}&:hover{cursor:pointer;}position: relative;line-height: 21px;
      }
      .showPeople{
        position: relative;height:24px;line-height:24px;width:24px;border-radius:50%;overflow: hidden;>img{width:24px;height:24px;border-radius:50%;}
        >span.deliconperson{position:absolute;right:-5px;color:#A2A2A2;display:none;z-index:2;i{font-size:10px;color: #CDCDCD;}}
        .bgcolorUser{text-align:center;background: #1890FF;transform:scale(.9);color:#fff;height:27px;line-height:27px;width:27px;display: inline-block;border-radius:50%;overflow: hidden;font-size: 12px;}
        &:hover{span.deliconperson{display: block;}}
      }
      .chooseSubTaskTime{
        position: absolute;width: 12px;height: 12px;top: 14px;right: 5px;overflow: hidden;opacity: 0;
      }
      .addTimeSubTask{
        z-index:0;position: relative;padding:0 10px;background:#EDEDED;border-radius:10px;text-align:center;width:140px;display: inline-block;height:20px;line-height:20px;
        >i:first-child{font-size: 12px;color:#A7A7A7;}
        >i:last-child{position: absolute;top:-7px;right:-6px;font-size: 12px;color:#A7A7A7;height:13px;}
        >i.deliconshanchutime{display: none;color:#cdcdcd;}
        span{font-size: 12px;}
        &:hover{>i.deliconshanchutime{display: block;}}
      }
    }
  }
  .subTaskInput.taskListSubInput{
    position: relative;background: #fff;border:0;display:flex;
    >i{position: absolute;top:-10px;right:-5px;font-size: 15px;display: none}
    .rightAdduserAndTime .addTimeSubTask{background: #fff;border:0;}
    &:hover{>i{display: block;}}
  }
  .rightBtn{text-align: right;margin-top:10px;}
}
div.showOrHiddenAddSubTask{
  div.rightAdduserAndTime{
    margin-right:10px;
  }
}
.addSubTask .subTaskInput .rightAdduserAndTime div.taskList{
  height:40px;width:5px;position: relative;margin-right:18px;span span.bgcolorUser{color:#fff;width:27px;height:27px;line-height: 27px;}line-height: 21px;
  img{
    // position: absolute;left:-20px;top:8px;
  }
}
.associatedTask{
  padding:10px 20px;
  .addAssociatedTask{
    margin:5px 0;
    >span:first-child, >span:nth-child(2){float:right;margin-right:13px;color:#BBBBC3;font-size:14px;&:hover{color:#1890FF;cursor: pointer;}}
    >span:last-child{>i{color: #C1C1C8;margin-right: 10px;}}
  }
  .guanlianTitle{
    display: flex;padding-left:25px;margin:15px 0;
    >span:first-child{margin-right:10px;font-size:12px;color: #666666;}
    >span:last-child{flex:1;border-bottom:1px solid #E9E9E9;height:10px;}
  }
  .guanlidetails.guanlidetailsAndChoos{
    border-color:#1890FF;border-radius: 2px;
  }
  .guanlidetails{
    position:relative;
    margin: 8px 0;
    background: #fff;
    min-height:50px;
    border-radius: 4px;
    box-shadow: 0 0 5px #ccc;
    overflow: hidden;
    margin-left: 25px;
    background: #fff;&:hover{cursor: pointer;}
    border:1px solid transparent;
    .checkOutIcon{
      position:absolute;top:-20px;left:-20px;>i{position: absolute;color:#fff;top:-6px;right:-21px;font-size: 12px;transform: rotate(-42deg);}
      border:20px solid transparent;border-right-color: #178FFF;transform:rotate(45deg);
    }
    .guanliaShow{
      position: absolute;top:0;right:0;width:25px;height:25px;line-height:25px;text-align: center;background: #fff;display: none;
    }
    .approvalListDetail,.memoListDetail{
      height:50px; line-height: 50px;padding: 0 20px 0 10px;>span{float:left;margin-right:10px;}>span:nth-child(2){i{color:#F5A623;font-size:25px;}}position: relative;
      >span:nth-child(3){max-width:300px;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;}
      >span:nth-child(4){i{font-size: 12px;margin-right:5px;}}
      >span:nth-child(4),>span:nth-child(5){font-size:12px;color:#a2a2a8;}
      >span:nth-child(6), >span.picandnameChange{
        float:right;margin:0;line-height:50px;height:50px;
        >span:first-child{
          float: left;width:90px;height:50px;line-height:50px;>span{float:left;}
          >span:first-child{height:10px;width:10px;border-radius: 50%;margin:20px 10px 0 0;}
        }
        >span:last-child{
          float: left;height:30px;width:30px;border-radius: 50%;text-align: center;line-height: 30px;color:#fff;margin:10px 0 0 5px;
          img{width:30px;height:30px;border-radius: 50%;vertical-align: middle;}
        }
      }
      &:hover{.guanliaShow{display: block;}}
      span.showBgcolor.addBgcolor{background: #60CDFF;}
      >span.sixSpan .circleCss0{background: #FFA92E;}
      >span.sixSpan .circleCss1{background: #008FE5;}
      >span.sixSpan .circleCss2{background: #00A85B;}
      >span.sixSpan .circleCss3{background: #FA3F39;}
      >span.sixSpan .circleCss4{background: #CACACA;}
      >span.sixSpan .circleCss5{background: #00A85B;}
      >span.sixSpan .circleCss6{background: #00A85B;}
    }
    .approvalListDetail .peopleBgColor{background:#60CDFF;span{font-size:12px;}}
    .customListDetail{
      min-height:50px;padding: 10px 20px 10px 10px;position: relative;
      >div{float:left;margin-right:10px;}
      >div:nth-child(2){
        span{display:inline-block;height:27px;width:25px;text-align:center;line-height: 27px;background: #549AFF;color:#fff;border-radius:2px;}
      }
      >div:nth-child(3){
        max-width:560px;
        >div{
          max-width:560px;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;padding:5px 0;
        }
        >div:last-child{font-size: 12px;color:#AFAFB5;}
      }
      >div:nth-child(4){
        float: right;height:30px;width:30px;border-radius: 50%;text-align: center;line-height: 30px;color:#fff;background: #60CDFF;margin-right:0;
        img{width:30px;height:30px;border-radius: 50%;vertical-align: middle;}
      }
      &:hover{.guanliaShow{display: block;}}
      div.customItemStyle{
        width: 100%;height:22px;
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
      .custom_personal{
        border-radius: 50%;
        img{width:100%;height:100%;}
      }
    }
    .customListDetail:after,.taskListDetail:after{content:'';display: table;clear:both;}
    .taskListDetail{
      border-left:4px solid transparent;min-height:50px;padding: 10px 20px 10px 0;>div{float:left;margin-right:10px;}position: relative;
      >div:first-child{width:1px;height:10px;margin-right:6px;}
      >div:first-child,>div:nth-child(2){}
      >div:nth-child(4){
        >div{padding:5px;min-height:25px;width:580px;overflow: hidden;}
        >div.twoSubTaskList{font-size:12px;color:#AFAFB5;max-width:580px;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;}
        >div.threeSubTaskList{
          min-height:28px;padding-bottom:0;
          .threeSubTaskListTags{
            margin-bottom:5px;padding:2px 5px;background:#51D0B1;color:#fff;border-radius: 5px;font-size:12px;margin-right:5px;margin-bottom:5px;display: inline-block;overflow: hidden;
          }
        }
      }
      >div:nth-child(5){
        float: right;height:30px;width:30px;border-radius: 50%;text-align: center;line-height: 30px;color:#fff;background: #60CDFF;margin-right:0;
        span{font-size:12px;}
        img{width:30px;height:30px;border-radius: 50%;vertical-align: sub;}
      }
      .taskoverHidden{
        >span{float:left;height:25px;margin-right:10px;}
        .layoutSpan{span{font-size:12px;}}
        // >span.taskSuboverHidden{display:inline-block;height:25px;max-width:280px;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;}
        // >span:nth-child(2){color:#AFAFB5;margin:0 10px;font-size: 12px;}
        // >span:nth-child(3){color:red;font-size: 12px;}
        span.foreachSpantask{
          font-size:12px;
          span{font-size:12px;color:#A2A2A8;}
          max-width:190px;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;
        }
        .subTaskCount{i{font-size:14px;}span{font-size:12px;}color:#A2A2A8;}
        .subTaskCount{font-size:12px;i{font-size:12px;}span{font-size:12px;}color:#A2A2A8;}
      }
      &:hover{.guanliaShow{display: block;}}
    }
    .taskListDetail.file{border-left-color:#36CFC9;}
    .taskListDetail.stop{border-left-color:#8A96AD;}
    .taskListDetail.del{border-left-color:#FF5800;}
    .taskListDetail.use{border-left-color:#52C41A;}
  }
}
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
  transform: translateX(800px);
}
.personal-task-details{
  background: #FFFFFF;
  position: fixed;
  width: 780px;
  top: 0;
  bottom: 0;
  right: 0;
  z-index: 10;
  box-shadow: -2px 2px 4px 0 rgba(155,155,155,0.30), 2px -2px 4px 0 ;
  .el-main-wrap{
    overflow-x: hidden;
    .dynamic-shade{
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      z-index: 5;
    }
    .icon-CombinedShape{
      position: absolute;right:50px;top:56px;color:#F5A623;font-size:50px;
    }
  }
  .el-header{
    line-height: 60px;
    padding: 0 10px 0 30px;
    box-shadow: inset 0 -1px 0 0 #EBEBF0;
    z-index: 10;
    .linestyle{
      float:right;width:2px;margin-top:18px;margin-left:4px;
      height:24px;border-right:1px solid #ddd;
    }
    .el-button{
      width: 60px;

    }
    .el-dropdown{
      margin: 0 0 0 20px;
      height: 25px;
      line-height: 25px;
      i{
        vertical-align: middle;
        font-size: 18px;
        color:#999;
        cursor: pointer;
      }
    }
    i.el-icon-close{
      float: right;
      font-size: 18px;
      color: #A0A0AE;
      margin: 18px 10px;
      padding: 3px;
      cursor: pointer;
    }
    .activation{float: right;margin:0 10px;span{color:#fff;background: #F4A41F;padding:0 2px;font-size:12px;}}
    .heartIcon{float: right;margin:0 10px;i{&:hover{cursor: pointer;}}i.icon-xin2{color: #FA5555;}}
    .leftHeader{
      float: left;width: 380px;height:60px;
      span{float: left;}
      >span.subLeftHeader{display:inline-block; max-width: 105px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;margin-left:5px;&:hover{color:#2595FF;cursor: pointer;}}
      >span.subLeftHeader:first-child{margin-left:0;}
    }
  }
  .el-tabs-warp
  {
    .el-tabs__nav-wrap{
      background: #FFF;
    }
    .el-tabs__nav-wrap::after{
      height:1px;background-color:#EBEBF0;
    }

  }

  .el-main{
    padding: 0;
    position: relative;
    .el-tab-line{
        border-left: 4px solid #26D0E0;background: none; height: 20px; vertical-align: middle;line-height: 20px; position: absolute;left: 0;top:64px;z-index: 2;
    }
    .title{
      height: 55px;line-height: 55px; font-size: 18px;color: #17171A;overflow: hidden;text-overflow: ellipsis;white-space: nowrap; padding: 0 30px;
    }
  }
  .colum-box{
    padding: 10px 45px;
    .item{
      display: inline-block;
      width: 50%;border-bottom:1px solid #F2F2F2;
      .components{
        display: flex;
        line-height:36px;
        >label{flex: 0 0 100px;margin:0 16px 0 0;text-align:left;font-size: 12px;color: #69696C;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;
        }
        >div{display: block;flex:1;font-size: 14px;color: #4A4A4A;line-height: 20px;padding: 7.5px 0 0 0;
          >span{
            margin: 0 5px 0 0;
          }
          span.picklist{display: inline-block;max-width: 100px;min-width: 52px;height: 20px;line-height: 20px;font-size: 12px;color: #FFFFFF;padding: 0 8px;
            margin: 0 10px 0 0 ;border-radius: 4px;text-align: center;vertical-align: middle;box-sizing: border-box;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;
          }
          .white-font{color: #4A4A4A !important;height: 100%!important;line-height: 36px!important;font-size: 14px!important;padding: 0!important;text-align: left!important;
          }
        }
        >div.personnel{
          padding-top:3px;
          display: flex;
          flex-wrap: wrap;
          align-items: center;
          padding: 5px 0 0 0;
          .head-image-wrip{
            margin: 0 5px 5px 0;
          }
        }
        >div.textarea {line-height: 20px;max-height: 60px;overflow: auto;white-space: normal;
        }
        >div.picture{
          width: 100%;
          .item{display: flex;width: 100%;height: 46px;padding: 3px;margin: 0 0 8px 0;box-sizing: border-box;
            img{width: 40px;flex: 0 0 40px;height: 40px;margin: 0 15px 0 0;}
            .iconfont{font-size: 40px;margin: 10px 15px 0 0;}
            .content{
              flex:1;box-sizing: border-box;
              .title{max-width: 150px;height: auto;font-size: 14px;line-height: 20px;color: #4A4A4A;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;
                .download{
                  float: right;
                  a{font-size: 12px;color: #29AB91;cursor: pointer;}
                  span{display: inline-block;width: 1px;height: 14px;line-height: 14px;background: #E7E7E7;margin: 0 8px;vertical-align: middle;}
                }
              }
              .detail{
                line-height: 20px;position: relative;
                span{font-size: 12px;color: #A0A0AE;}
                .name{display: inline-block;width: 40px;overflow: hidden; text-overflow: ellipsis;white-space: nowrap;}
                .date{position: absolute;left: 40px; }
                .size{ position: absolute;left: 115px;}
                .icon-pc-paper-download{
                  position: absolute;left: 180px;margin:0;font-size:12px;color: #A0A0AE;cursor: pointer;
                }
              }
            }
          }
        }
        >span.subform{
          width: 100%; border:1px solid #E7E7E7;overflow: auto;
          .titles{display: flex;width: 100%;height: 30px;line-height: 30px;
            span{flex: 1;min-width: 160px;padding: 0 10px;border-right:1px solid #E7E7E7;
              &:last-child{ border-right: none;}
            }
          }
          .list{display: flex;width: 100%;height: 40px;line-height: 40px;
            .subform-child{flex: 1;min-width: 160px;padding: 0 10px;border-top: 1px solid #E7E7E7;border-right:1px solid #E7E7E7;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;
              &:last-child{border-right: none;}
              span.picklist{display: inline-block;max-width: 100px;min-width: 52px;height: 24px;line-height: 24px;font-size: 12px;color: #FFFFFF;
                padding: 0 8px;margin: 0 10px 0 0 ;border-radius: 37px;text-align: center;vertical-align: middle;box-sizing: border-box;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;margin-bottom:5px;
              }
              .white-font{
                color: #4A4A4A !important;height: 100%!important;line-height: 36px!important; font-size: 14px!important;padding: 0!important;text-align: left!important;
              }
              span.picture{
                width: 100%;
                .item{display: flex;width: 100%;height: 40px;box-sizing: border-box;
                  img{width: 40px;flex: 0 0 40px;height: 40px;margin: 0 15px 0 0; }
                  .iconfont{font-size: 40px;}
                  .content{
                    flex:1;box-sizing: border-box;
                    .title{height: auto;font-size: 14px;line-height: 20px;color: #4A4A4A;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;
                      .download{
                        float: right;
                        a{font-size: 12px; color: #29AB91;cursor: pointer;}
                        span{display: inline-block;width: 1px;height: 14px;line-height: 14px;background: #E7E7E7;margin: 0 8px;vertical-align: middle;}
                      }
                    }
                    .detail{
                      line-height: 20px;position: relative;
                      span{font-size: 12px;color: #A0A0AE;}
                      .name{display: inline-block;width: 40px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;}
                      .date{position: absolute;left: 40px;}
                      .size{position: absolute;left: 115px;}
                    }
                  }
                }
              }
            }
          }
        }
        >div.url{ color: #008FE5;cursor: pointer;}
      }
    }
    .top-bottom{
      .components{display: block;line-height: 30px;
        >label{display: block;}
        >span{display: block;}
      }
    }
  }
  .subform-box-card{
    padding:10px;border-radius: 3px;width:100%;border:1px solid #ddd;border-radius:4px;margin-bottom:10px;position: relative;
    .lineLayout{
      padding-bottom:20px;
    }
    .delete-card{
      position: absolute;right:-40px;top:10px;height:24px;line-height: 24px;width:24px;text-align: center;border-radius: 50%;
      box-shadow: 0 2px 4px 0 #C8C8C8;i{font-size:14px;color:#FA5555;}cursor: pointer;
    }
    .subform-box-card-item{
      display:flex;
      .card-title{width:100px;overflow:hidden; text-overflow:ellipsis;white-space:nowrap}
      .card-icon{width:30px;}
      .card-body{flex:1;}
      .upload-card{display: none;}
      .attachment-box {
        .picture{
          position: relative;
          display: inline-block;
          padding: 0 0 0 10px;
          img{
            width: 36px;
            height: 36px;
          }
          i{
            position: absolute;
            right: -5px;
            top: 0;
            font-size: 12px;
            color: #F94C4A;
            cursor: pointer;
          }
        }
        .attachment-show{
          position: relative;
          padding: 0 0 0 10px;
          >i{
            position: absolute;
            left: 35px;
            top: 0;
            font-size: 12px;
            color: #F94C4A;
            cursor: pointer;
          }
        }
      }
      .people-box{
        flex: 1;
        display: flex;
        flex-wrap: wrap;
        padding: 10px 0 0 10px;
        .people-item{
          position: relative;
          margin: 0 10px 10px 0;
          &:hover{
              i.el-icon-circle-close{visibility:visible;}cursor: pointer;
          }
          i.el-icon-circle-close{
            position: absolute;
            visibility: hidden;
            right: -10px;
            top: -5px;
            font-size: 12px;
            color: #F94C4A;
            cursor: pointer;
          }
        }
        .people-add{
          width: 30px;
          height: 32px;
          .iconfont{
            line-height: 32px;
            font-size: 30px;
            color: #ACB8C5;
            cursor: pointer;
          }
        }
      }
      .location-box,.reference-box{
        display: flex;i{cursor:pointer; margin-left:5px;}
      }
    }
  }
}
.checkAndTitle{
  padding:0 20px 0 0;background:#F4F5F5;min-height:50px;line-height:50px;
  >div{float:left;}
  >div:first-child{
    height:50px;line-height: 50px;border-left:4px solid transparent;width:5%;text-align:center;margin-right:10px;
    >span{
      position: relative;display: inline-block;height:20px;width:20px;border:1px solid #B9B9C1;border-radius: 3px;margin-top: 15px;&:hover{cursor: pointer;}
      >i{position: absolute;top:-16px;left:1px;color:#D7D7DB;font-size:12px;}
    }
  }
  >div:first-child.complete{border-left-color:red;}
  >div:last-child{
    width:90%;max-height:100px;
    overflow : hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color:#17171A;font-size:16px;
  }
}
.checkAndTitle:after{content:'';display:table;clear:both;}
.checkedActive{
  height:16px;width:16px;display: inline-block;border: 1px solid #B9B9C1;border-radius: 3px;position:relative;line-height:16px;
  i{
    position:absolute;font-size:12px;color: #D7D7DB;top:0;left:-1px;height:12px;transform:scale(.6);
  }
}
.associatedTask .guanlidetails .taskListDetail>div.checkedActive{margin: 7px 5px 0 0;}
.checkedActive.subtaskChecked{top:3px;}
</style>
<style lang="scss" >
.personal-task-details{
  .showTimeTooltip{font-size:12px;}
  .el-collapse{
    width: 100%;border:none;
    .el-collapse-item__header{
      height: 40px; line-height: 40px;padding: 0 15px;border-bottom: 1px solid #e6ebf5;
      .el-icon-circle-check{
        font-size: 16px;color:#51D0B1;margin:0 10px 0 0;
      }
      .el-collapse-item__arrow{
        font-size:16px;color: #424242;float: none;display: none;
      }
      i.iconfont{
        -webkit-transform: rotate(0deg);transform: rotate(0deg);transition: -webkit-transform .3s;transition: transform .3s;
        transition: transform .3s, -webkit-transform .3s;transition: transform .3s,-webkit-transform .3s;
      }
      span{
        font-size: 14px; color: #424242;
      }

    }
    .el-collapse-item__header.is-active{
        i.iconfont{
          -webkit-transform: rotate(-90deg); transform: rotate(-90deg);display: inline-block;transition: -webkit-transform .3s;transition: transform .3s;transition: transform .3s, -webkit-transform .3s;transition: transform .3s,-webkit-transform .3s;
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
  .el-tabs{
    margin: 0 0 16px 0;
    .el-tabs__header{
      margin: 0;background: #ffffff;
    }
    .el-tabs__nav-wrap::after{
      height: 1px;background:#EBEBF0;
    }
    .el-tabs__active-bar{
      top:0;

    }

    .el-tabs__item{
      text-align:center;padding:0 5px;

    }
    .el-tabs__item.is-active{
      color:#212121;border-left:1px solid #EBEBF0;border-right:1px solid #EBEBF0;
    }

    .el-tabs__content{
      padding:0 30px;
    }
    .el-tab-pane{
      margin:15px 0 0 0;background: #ffffff;
    }
    .el-collapse-item__wrap{
      padding:0; width: 100%;
    }
    .colum-box{
        padding:0 15px 10px 15px;
      .item{
        box-shadow: inset 0 -1px 0 0 #F2F2F2;padding-top: 8px;border:1px solid #F2F2F2;
      }
    }
    .reference-box{
      height: 60px;line-height: 60px;margin: -15px 0 0 0; border-bottom: 1px solid #E7E7E7;
      .control-box{
        display: inline-block;
        >span{
          vertical-align: middle;margin: 0 30px 0 0;
          span{
            color: #FF6D5D;
          }
        }
        a{
          padding: 3px 12px;cursor: pointer; background: #EFF1F4;border-radius: 4px;color:#69696C;
          i{
            font-size: 16px;margin: 0 8px 0 0;
          }
        }
        a:hover{
          background:#E6F1FC;color:#1890FF;
        }
      }
      .el-button{
        width:80px;height: 26px;line-height:26px; padding:0;
         i{
          font-size: 12px;
        }
      }
      .el-dropdown{
        float: right;width: 20px;height: 20px;line-height: 20px;margin: 20px 0;cursor: pointer;
        .iconfont {
          font-size:20px;
        }
      }
    }
    .list-box{
      overflow: hidden;height: calc(100% - 60px);
    }
  }
  .el-tab-nav{
    .el-tabs__item{max-width:100px;min-width: 90px;text-overflow: ellipsis;white-space: nowrap;overflow: hidden;padding:0 10px !important; }
  }
  .reference-tab{
    height: calc(100% - 80px);
    .el-tabs__content{
      .el-pane-warp{padding:20px;}
    }
  }
  .subTaskInputName{
    .el-textarea{min-height:40px;}
    .el-textarea__inner{border:0; background:#FBFBFB;padding-top:10px;resize:none; padding-right:0;}
  }
  .subTaskInput{
    .el-checkbox{position: absolute;top: 0;left: 9px;}
  }
}
.addXiezuoUser{
  .allpersonList{max-height:600px;overflow:auto;}
  .el-dialog{max-height:660px;}
  .el-dialog__header{border-bottom:1px solid #ddd;}
  .el-dialog__body{padding:0;}
  .el-dialog__footer{border-top:1px solid #ddd;background:#fff;padding:10px 20px;.el-button{padding:6px 10px;height:32px;width:65px;}}
  .titleHeader{width:100%;height:50px;text-align:center;line-height:50px;border-bottom:1px solid #ddd;}
  .listUser{
    height:60px;line-height:60px;padding:0 20px;&:hover{background:#F2F2F2;cursor:pointer;}position:relative;
    >span{float:left;height:60px;line-height:60px;margin-right:10px;}
    >span.addPicOrName{
      height:40px;width:40px;line-height:40px;text-align:center;border-radius:50%;margin-top:10px;
      img{width:100%;vertical-align:sub;border-radius:50%;}
    }
    >span.addPicOrName.isName{background:#409EFF;color:#fff;}
    >span.gouxuanStatus{
      position: absolute;top:0;right:5px;
      >i{font-size:12px;color:#208AF4;}
    }
    span.criclRed{
      span.subcriclRed{
        height:10px;width:10px;background:#F01B0C;border-radius:50%;margin-top:25px;display:inline-block;
      }
    }
  }
}
.subTaskInputName{
  .el-input__inner{border:0;background:#FBFBFB;height:35px;padding:0;}
}
.addSubTask{
  .el-button{padding:8px 20px;}
}
.chooseSubTaskTime {.el-input__suffix{display:none;}}
.taskListDetail{
  .el-checkbox__inner{width: 18px;height: 18px;}
  .el-checkbox__inner::after{height: 10px;left: 6px;}
}
.subTaskInput.taskListSubInput{.el-checkbox__inner{width:18px;height:18px;}}
.activtionAndEditor{
  .el-dialog__header{display:none;}
  .titleHeader{color:#9B9B9B;margin-bottom:10px;}
}
.subTaskProgress{.el-progress__text{display:none;}.el-progress-bar{padding-right:0;}}
.subTaskInput{.el-checkbox__inner{width:16px;height:16px;z-index:0;}}
.subform-box-card .subform-box-card-item{
  .select-group{
    .el-select{margin-right:10px;}
  }
  .el-select,.el-date-editor{width:100%;}
}
.personal-task-details{
  .module-other-wrip{
    .other-title,.comment-box{
      margin-left:15px;
    }
    .seestate-box{
      margin-bottom: 80px;margin-left:30px;
    }
  }
}
.myTaskEmployeePersonal{
  .el-dialog__header{
    display:none;
  }
  .el-dialog__body{
    padding:0;border-radius:5px;overflow: hidden;
  }
}
</style>
