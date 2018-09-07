<template>
  <el-container class="main-project-details-wrap" @click="sceenProjectOutin=0">
    <el-container class="containerChangeBaseInfo">
      <!-- 项目基本信息的侧边栏显示和隐藏按钮 -->
      <!-- <div @click="showOrHidden" v-if="!isFromWorkTable" class="fixedBaseInfo" :class="showOrHiddenBaseInfo==='1'?'in':'out'">
        <i v-if="showOrHiddenBaseInfo==='1'" class="iconfont icon-shouqi1"></i>
        <i v-if="showOrHiddenBaseInfo==='0'" class="iconfont icon-tongyongshouqi"></i>
        <span class="spanTop" :class="showOrHiddenBaseInfo==='1'?'in':'out'"></span>
        <span class="spanBottom" :class="showOrHiddenBaseInfo==='1'?'in':'out'"></span>
      </div> -->
      <div class="fixedBaseInfo" @click="showOrHidden"  v-if="!isFromWorkTable" :class="showOrHiddenBaseInfo==='1'?'in':'out'" @mouseover="overShow" @mouseout="outHide">
        <p class="side-p-item"></p>
        <i class="iconfont icon-xuanrenkongjian-icon5" v-if="showOrHiddenBaseInfo==1"></i>
        <i class="iconfont icon-iconfontjiantou1" v-else></i>
      </div>
      <!-- 人员卡片 -->
      <div class="personCardcss" v-if="showPersonCard" @click.stop="hiddenoProper">
        <div class="isEnableActive" v-if="cardPersonDetails.is_enable==='0'">
          <span></span>
          <span>未激活</span>
        </div>
        <div class="firstchildDiv">
          <div class="personPic">
            <div class="topPicAddName">
              <div class="peoplePic">
                <p class="userGender" v-if="cardPersonDetails.sex">
                  <i class="iconfont icon-xingbie1" v-if="cardPersonDetails.sex==='1'"></i>
                  <i class="iconfont icon-xingbie" v-else></i>
                </p>
                <p class="userName" v-show="!cardPersonDetails.picture||cardPersonDetails.picture=='null'" v-text="edtiorName(cardPersonDetails.employee_name,2)">安其拉</p>
                <img v-show="cardPersonDetails.picture&&cardPersonDetails.picture!='null'" :src="cardPersonDetails.picture + '&TOKEN=' + token" alt="">
              </div>
              <div style="font-size:16px;margin:14px 0;" v-text="cardPersonDetails.employee_name">张三</div>
              <div style="color:#A0A0AE;" v-text="cardPersonDetails.role_name">java工程师</div>
            </div>
            <div class="twoButton">
              <el-button type="primary" @click="openEmailOrMemo(0)" :disabled="userInfo.sign_id == cardPersonDetails.sign_id">
                <i class="iconfont icon-pinglun"></i>
              </el-button>
              <el-button type="primary" @click="openEmailOrMemo(1)" :disabled="!cardPersonDetails.email"><i class="iconfont icon-youjian2"></i></el-button>
            </div>
          </div>
          <div class="depInfo">
            <p><span>部门 : </span><span v-text="cardPersonDetails.department_name">ggg</span></p>
            <p><span>上级 : </span><span v-text="cardPersonDetails.main_employee_name">ggg</span></p>
            <p><span>座机 : </span><span ></span></p>
            <p><span>手机 : </span><span v-text="cardPersonDetails.phone">ggg</span></p>
            <p><span>邮箱 : </span><span v-text="cardPersonDetails.email">ggg</span></p>
          </div>
        </div>
        <!-- 移除成员或者修改项目权限 -->
        <div class="secondchildDiv" v-if="judgeProjectRoleInfo(12)&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'&&cardPersonDetails.id!=projectBaseInfo.leader">
          <span class="delPerer" @click.stop="edtiorPerEvent(0,cardPersonNewDetails)">移除成员</span>
          <span class="editorPer">
            <el-popover placement="bottom-start" width="200" trigger="click" @show="edtiorPerEvent(1,cardPersonNewDetails)">
              <div>
                <div style="padding:10px;">修改项目权限</div>
                <div v-for="(v2,k2) in projectRoleList" :key="k2" class="authListcss" :class="v2.isactive?'authactive':''">
                  <el-checkbox v-model="v2.isactive" class="checkedStyleProTask" @change="handleCheckAllChange(v2)"></el-checkbox>
                  <span :class="v2.isactive?'spanauthactive':''">{{v2.name}}</span>
                </div>
              </div>
              <span slot="reference">修改项目角色</span>
            </el-popover>
          </span>
        </div>
      </div>
      <el-header class="topHeader">
        <div class="headerDiv selectId" v-if="!isFromWorkTable">
          <router-link to="./projectList">
            <div class="imgCss">
              <i class="iconfont icon-iconfontjiantou1"></i>
              <span>返回</span>
            </div>
          </router-link>
          <span @click="updateProjectAsterisk(submitData)"><i class="iconfont icon-xingxing" :class="submitData.star_level === 1?'active':''"></i></span>
          <div class="topheaderDropdown" @click="changeDropdown" v-on:mouseleave="hiddenSettingPopu">
            <span v-text="submitData.choosePro"></span><i class="el-icon-arrow-down el-icon--right"></i>
            <div class="dropdownItem" v-if="dropdownVisbli===1" @click.stop>
              <div style="padding:0 10px;">
                <el-input placeholder='搜索项目' class='search-text' v-model="keyword" @keyup.enter.native="searchClick" clearable style="padding:5px;">
                  <i slot='prefix' class='el-input__icon el-icon-search'></i>
                </el-input>
              </div>
              <div >
                <div v-for="(v, k) in options5" :key="k">
                  <div v-if="v.status === '0'" class="titleTop"><i class="iconfont icon-xingxing" style="color:#FF6F00;"></i>星标项目</div>
                  <div v-if="v.status === '1'" class="titleTop">所有项目</div>
                  <div class="itemBox" v-for="(v1,k1) in v.list" :key="k1" @click="handleCommand(v1)">
                    <img v-if="v1.pic_url" :src="v1.pic_url+ '&TOKEN=' + token" alt="" class="imgcssbgcolor">
                    <img v-else-if="!v1.pic_url && v1.system_default_pic==='one'" src="/static/img/project/1@2x.png" alt="" class="imgcssbgcolor">
                    <img v-else-if="!v1.pic_url && v1.system_default_pic==='two'" src="/static/img/project/2@2x.png" alt="" class="imgcssbgcolor">
                    <img v-else-if="!v1.pic_url && v1.system_default_pic==='three'" src="/static/img/project/3@2x.png" alt="" class="imgcssbgcolor">
                    <img v-else-if="!v1.pic_url && v1.system_default_pic==='four'" src="/static/img/project/4@2x.png" alt="" class="imgcssbgcolor">
                    <img v-else-if="!v1.pic_url && v1.system_default_pic==='five'" src="/static/img/project/5@2x.png" alt="" class="imgcssbgcolor">
                    <img v-else-if="!v1.pic_url && v1.system_default_pic==='six'" src="/static/img/project/6@2x.png" alt="" class="imgcssbgcolor">
                    <img v-else-if="!v1.pic_url && v1.system_default_pic" :src="'/static/img/project/system-template-'+v1.system_default_pic+'.png'" alt="" class="imgcssbgcolor">
                    <img v-else src="/static/img/project/1@2x.png" alt="" class="imgcssbgcolor">&nbsp;&nbsp;
                    <span v-text="v1.name">最新的项目管理</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="headerDiv selectId workTable-item" v-if="isFromWorkTable==='workTable'">
          <i class="iconfont icon-fanhui" @click="getTimeWorkList"></i>
          <span v-text="workTableFromName"></span>
          <!-- <el-select v-model="value8" placeholder="请选择">
            <el-option-group v-for="(group,key) in options3" :key="group.label" :label="group.label">
              <el-option v-for="item in group.options" :key="item.id" :label="item.name" :value="item.id" @click.native="getTimeWorkList(item, key)">
              </el-option>
            </el-option-group>
          </el-select> -->
        </div>
        <div class="headerDiv middleTab">
          <div :class="activeName==='1'?'activeTab':''" @click="changeTab('1')">
          <!-- <div :class="activeName==='1'?'activeTab':''"> -->
            <span>任务</span>
            <!-- <i class="el-icon-arrow-down el-icon--right" v-if="workbenchActive.three===1" @click.stop="taskGropBox=!taskGropBox"></i>
            <div class="taskGropBox" v-if="workbenchActive.three===1&&taskGropBox">
              <div>
                <span @click="kanbanPreviewDialog = true">添加分组</span>
              </div>
              <div v-for="(v,k) in taskGropDropdown" :key="k" class="itemList">
                <span v-text="v.name"></span>
              </div>
            </div> -->
          </div>
          <div :class="activeName==='2'?'activeTab':''" @click="changeTab('2')"><span>分享</span></div>
          <div v-if="!isFromWorkTable" :class="activeName==='3'?'activeTab':''" @click="changeTab('3')"><span>文库</span></div>
          <div v-if="isFromWorkTable==='workTable'" :class="activeName==='3'?'activeTab':''" @click="changeTab('5')"><span>日程</span></div>
          <div :class="activeName==='4'?'activeTab':''" @click="changeTab('4')"><span>统计</span></div>
        </div>
        <div class="headerDiv" style="width:30%;">
          <div class="headerTopRight">
            <div @click="openListChange" v-if="activeName==='1'&&!isFromWorkTable" class="moduleList" v-on:mouseleave="hiddenSettingPopu">
              <i class="iconfont icon-shitu"></i>
              <!-- <el-popover ref="popover4" placement="bottom" width="200" trigger="click" class="cenjiPreview"> -->
              <div v-if="showOrHiddenDiffModel===1">
                <div class="popoverClass" @click.stop="changeModle('one')" :class="workbenchActive.one===1?'active':''">
                  <span>项目工作台</span><i v-if="workbenchActive.one===1" class="iconfont icon-icon1"></i>
                </div>
                <div class="popoverClass" @click.stop="changeModle('two')" :class="workbenchActive.two===1?'active':''">
                  <span>层级视图</span><i v-if="workbenchActive.two===1" class="iconfont icon-icon1"></i>
                </div>
                <div class="popoverClass" @click.stop="changeModle('three')" :class="workbenchActive.three===1?'active':''">
                  <span>看板视图</span><i v-if="workbenchActive.three===1" class="iconfont icon-icon1"></i>
                </div>
                <div class="popoverClass" @click.stop="changeModle('four')" :class="workbenchActive.four===1?'active':''">
                  <span>表格视图</span><i v-if="workbenchActive.four===1" class="iconfont icon-icon1"></i>
                </div>
                <!-- <div class="popoverClass" @click.stop="changeModle('five')" :class="workbenchActive.five===1?'active':''">
                  <span>日历视图</span><i v-if="workbenchActive.five===1" class="iconfont icon-icon1"></i>
                </div>
                <div class="popoverClass" @click.stop="changeModle('six')" :class="workbenchActive.six===1?'active':''">
                  <span>甘特图</span><i v-if="workbenchActive.six===1" class="iconfont icon-icon1"></i>
                </div> -->
              </div>
              <!-- </el-popover> -->
            </div>
            <!-- <div @click="openMember"><i class="iconfont icon-duoren"></i></div> -->
            <!-- 筛选 -->
            <div @click="openSreenPro" v-if="activeName==='1'&&isFromWorkTable!=='workTable'" class="screensetting"><i class="iconfont icon-pc-filter"></i></div>
            <!-- 基本设置 -->
            <div @click="openSettingPopu" class="settingProBox" v-if="!isFromWorkTable" v-on:mouseleave="hiddenSettingPopu">
              <i class="iconfont icon-gerenshezhi1"></i>
              <div v-if="showOrHiddenSeeting===1">
                <div class="settingProNewItem" @click.stop="openDilog('base')"><span>基本设置</span></div>
                <div class="settingProNewItem" @click.stop="openDilog('tag')"><span>项目标签</span></div>
                <div class="settingProNewItem" @click.stop="openDilog('defined')"><span>自定义管理</span></div>
                <div class="settingProNewItem" @click.stop="openDilog('autoFlowManage')"><span>自动化管理</span></div>
                <!-- <div v-if="judgeProjectRoleInfo(10)" class="settingProNewItem" @click.stop="openDilog('export')"><span>导出项目任务</span></div> -->
                <div class="settingProNewItem" @click.stop="openDilog('export')"><span>导出项目任务</span></div>
                <div class="settingProNewItem" @click.stop="openDilog('import')"><span>导入项目任务</span></div>
                <div class="settingProNewItem" @click.stop="openDilog(7)"><span>保存为项目模板</span></div>
              </div>
            </div>
          </div>
        </div>
      </el-header>
      <el-dialog title="" :visible.sync="kanbanPreviewDialog" width="400px" id="kanbanPreviewDialog">
        <div>
          <el-form label-width="80px">
            <el-form-item label="任务分组">
              <el-input v-model="taskGroupName"></el-input>
            </el-form-item>
          </el-form>
        </div>
        <span slot="footer" class="dialog-footer">
          <div @click="perviewTemplate" class="footerTaskPreview">
            <i class="iconfont icon-mubanbianpai"></i>
            <span>选择产品模板</span>
          </div>
          <el-button @click="kanbanPreviewDialog = false">取 消</el-button>
          <el-button type="primary" @click="addTaskGroupChooseMoudel">确 定</el-button>
        </span>
      </el-dialog>
      <!-- <el-main v-if="activeName==='1'" style="position:relative;background: #EBEDF0;">
        <task-project></task-project>
      </el-main> -->
      <el-main style="position:relative;height: calc(100% - 85px);width: 100%;background:#EBEDF0;">
        <!-- 任务页面 -->
        <task-main v-if="activeName==='1'" :workbenchShowPage="workbenchShowPage"></task-main>
        <!-- 分享页面 -->
        <share-project v-if="activeName==='2'" :projectId="newProjectId"></share-project>
        <!-- 文库页面 -->
        <library-project v-if="activeName==='3'" :projectId="newProjectId"></library-project>
        <!-- 统计页面 -->
        <statistics-project v-if="activeName==='4'" :projectId="newProjectId"></statistics-project>
        <kanban-schedule v-if="activeName==='5'"></kanban-schedule>
        <!-- 侧边弹出设置 -->
        <!-- <div class="asideright"  :class="viewOutin?'asideright-in':'asideright-out'">
          <setting-project></setting-project>
        </div> -->
        <!-- 侧边弹出筛选 -->
        <div class="asiderightMask" v-if="sceenProjectOutin==1" @click="sceenProjectOutin=0"></div>
        <div class="asideright" :class="[sceenProjectOutin?'asideright-in':'asideright-out']" :style="{'right':showOrHiddenBaseInfo==='1'&&sceenProjectOutin?'350px':'0'}">
          <screen-project :projectId="projectId"></screen-project>
        </div>
        <!-- 侧边弹出成员 -->
        <!-- <div class="asideright" :class="memberOutin?'asideright-in':'asideright-out'">
          <member-list></member-list>
        </div> -->
      </el-main>

      <div class="shadeTaskDetails" v-if="ProjectTaskDetails || openCreateModal"></div>
      <!-- 任务/备忘录/审批等自定义详情 -->
      <transition name="slide">
        <DifferentTypesDetails v-if="ProjectTaskDetails" :listData="gotoDetailsData" :projectId="newProjectId"></DifferentTypesDetails>
      </transition>
      <!-- 任务详情内部，更多操作 设置/复制任务/删除任务等弹窗 -->
      <set-up-more></set-up-more>
      <!-- 新建任务/自定义等 -->
      <transition name="slide">
        <addTasktypeMain v-if="openCreateModal" :allTypeModules="modules" :allTypeData="dataDtl" :autoWorkFlow="autoWorkFlow" :status="showTypeAddTask" :projectId="newProjectId+''"></addTasktypeMain>
      </transition>
      <transition name="slide">
        <div class="showtaskcreateforpro" v-if="showTaskCreate">
          <list-task-create :editorData="editorData"></list-task-create>
        </div>
      </transition>
      <!-- 新增备忘录弹窗 -->
      <el-dialog title="新增备忘录" class="creatMemo" :close-on-click-modal="false" :visible.sync="dialogForCreate" width="760px">
        <div class="dialog-content">
          <memoEditor v-if="dialogForCreate" :memoDetail="memoDetail" :isEdit="true"></memoEditor>
        </div>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="dialogSureBtn()">确 定</el-button>
          <el-button @click="dialogForCreate = false">取 消</el-button>
        </div>
      </el-dialog>

      <!-- 基本设置弹窗 -->
      <setting-dilog></setting-dilog>
      <!-- 项目自动化管理 -->
      <project-auto-workflow></project-auto-workflow>
      <!-- 任务自定义 -->
      <transition name="project-defined-mask">
        <div class="project-defined-mask" v-show="switchProjectDefined"></div>
      </transition>
      <transition name="project-defined">
        <div class="project-defined-box" v-if="switchProjectDefined">
          <projectDefined ></projectDefined>
        </div>
      </transition>
    </el-container>
    <!-- 基本信息，项目成员，项目动态。侧边栏 -->
    <el-aside class="elAsideBaseInfo" v-if="!isFromWorkTable" :class="showOrHiddenBaseInfo==='1'?'activeIn':'activeOut'" :width="BaseInfoWidth">
      <p class="baseInfosetting"><i class="iconfont icon-jibenxinxi"></i><span>基本信息</span></p>
      <div class="projectBaseInfo">
        <p><span>项目名称</span><span v-text="projectBaseInfo.name" class="moreTextStyle">会员管理系统</span></p>
        <p><span>状态</span><span v-text="projectStatusStr[projectBaseInfo.project_status]" :class="'projectStatus'+projectBaseInfo.project_status">会员管理系统</span></p>
        <p><span>负责人</span><span v-text="projectBaseInfo.leader_name">会员管理系统</span></p>
        <p>
          <span>时间</span>
          <!-- <span class="startendTimeCss">开始</span> -->
          <el-tooltip class="item startendTimeCss" effect="dark" placement="top">
            <div slot="content" class="showTimeTooltip">开始时间：{{projectBaseInfo.start_time | formatDate('yyyy-MM-dd HH:mm')}}</div>
            <span>{{projectBaseInfo.start_time | formatDate('yyyy-MM-dd')}}</span>
          </el-tooltip>
          <!-- <span>{{projectBaseInfo.create_time | formatDate('yyyy-MM-dd')}}</span> -->
          <!-- <span class="startendTimeCss">结束</span> -->
          <span v-if="projectBaseInfo.start_time&&projectBaseInfo.end_time">~</span>
          <el-tooltip class="item startendTimeCss" effect="dark" placement="top">
            <div slot="content" class="showTimeTooltip">截止时间：{{projectBaseInfo.end_time | formatDate('yyyy-MM-dd HH:mm')}}</div>
            <span>{{projectBaseInfo.end_time | formatDate('yyyy-MM-dd')}}</span>
          </el-tooltip>
          <!-- <span>{{projectBaseInfo.create_time | formatDate('yyyy-MM-dd')}}</span> -->
        </p>
        <div>
          <span style="width:70px;">项目描述</span>
          <span class="ProjectDesc" :style="style" @mouseenter="isShowHid(0)" @mouseleave="isShowHid(1)" v-text="projectBaseInfo.note"></span>
          <div class="ProjectDescItem">
            <i></i>
            <span v-text="projectBaseInfo.note"></span>
          </div>
        </div>
        <p>
          <span>项目进度</span>
          <span class="progressCss" @click="openProjectProgress">
            <el-popover placement="bottom-start"  width="60" trigger="click" popper-class="myDettailsTask">
              <div class="showBoxItem">
                <div>总任务 <span v-text="StatisticsProcredd.taskscount"></span></div>
                <div>已完成 <span v-text="StatisticsProcredd.finishedcount"></span></div>
                <div>未完成 <span v-text="StatisticsProcredd.waitfinishcount"></span></div>
              </div>
              <div slot="reference">
                <el-progress v-if="projectBaseInfo.project_progress_status=='0'" :percentage="projectBaseInfo.project_progress_number"></el-progress>
                <el-progress v-if="projectBaseInfo.project_progress_status=='1'" :percentage="projectBaseInfo.project_progress_content"></el-progress>
              </div>
            </el-popover>
          </span>
        </p>
      </div>
      <div class="projectPerson">
        <p><i class="iconfont icon-chengyuan"></i>项目成员</p>
        <div class="personBoxList">
          <div v-for="(v, k) in showUserList" :key="k" class="personListCss" @click="openPersonCard(v,$event)">
            <div>
              <div class="personTopImgOrName noPictrue3">
                <span v-if="!v.employee_pic" v-text="edtiorName(v.employee_name,2)">v.employee_name</span>
                <img v-if="v.employee_pic" :src="v.employee_pic + '&TOKEN=' + token" alt="">
              </div>
            </div>
            <div class="divNamePerson" v-text="edtiorName(v.employee_name,3)" :class="v.active?'active':''"></div>
          </div>
          <!-- 添加项目成员 -->
          <div v-if="judgeProjectRoleInfo(11)&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'" class="personListCss">
            <div class="addperson" @click.stop="openUserList">
              <i class="iconfont icon-xinzengrenyuan"></i>
            </div>
          </div>
          <div @click.stop="showAllPeroson($event)" class="personListCss" v-if="copyshowUserList.length>13&&!showusersStatus">
            <div class="addperson" style="color:#446078;cursor:pointer;">
              更多...
            </div>
          </div>
        </div>
      </div>
      <div class="projectDynamic">
        <p><i class="iconfont icon-dongtai1"></i>项目动态</p>
        <div class="projectStatusNext" v-for="(v,k) in projectDynamicList" :key="k">
          <div>
            <p>
              <span v-text="v.employee_name" style="color:#26D0E0;"></span>
              <span v-text="v.content"></span>
            </p>
            <span class="timeProjectDy">{{v.datetime_time|formatDate('yyyy-MM-dd HH:mm:ss')}}</span>
          </div>
        </div>
        <div class="moresetting" v-if="projectDynamicListCopy.length > 10 && projectDynamicListCopy.length !== projectDynamicList.length">
          <span @click="openMoreDynamic">更多...</span>
        </div>
      </div>
    </el-aside>
    <el-dialog title="保存为项目模版" :visible.sync="addProjectPreviewVisible" width="500px" class="ProjectTemplateRemark">
      <div>
        <el-form label-width="100px" :model="formLabelAlign" :rules="rules">
          <el-form-item label="模板名称" prop="ProjectTemplateTitle">
            <el-input v-model="formLabelAlign.ProjectTemplateTitle" placeholder="请输入项目名称，限制25个字"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="addProjectPreviewVisible = false">取 消</el-button>
        <el-button type="primary" @click="addProjectPreComplate()">保 存</el-button>
      </span>
    </el-dialog>
    <el-dialog title="协作自动化" :visible.sync="workAutoVisible" width="700px" class="workAutoStyle">
      <div class="workAutoStyle-item">
        <p class="first-item">满足条件的记录自动执行设定的业务流程</p>
        <el-button class="senond-item" type="primary" @click="addWorkAuto(0)">新 增</el-button>
        <el-table
          :data="tableData"
          style="width: 100%"
          v-loading="getListLoading"
          element-loading-text="拼命加载中..."
          element-loading-spinner="el-icon-loading"
          element-loading-background="rgba(0, 0, 0, 0)">
          <el-table-column
            label="模块"
            width="120">
            <template slot-scope="scope">
              <span style="margin-left: 10px">{{scope.row.module_name}}</span>
            </template>
          </el-table-column>
          <el-table-column
            label="规则名称"
            width="120">
            <template slot-scope="scope">
              <span style="margin-left: 10px">{{scope.row.title}}</span>
            </template>
          </el-table-column>
          <el-table-column
            label="触发事件"
            width="120">
            <template slot-scope="scope">
              <span style="margin-left: 10px">{{scope.row.triggers | triggersToText}}</span>
            </template>
          </el-table-column>
          <el-table-column
            label="描述"
            width="120">
            <template slot-scope="scope">
              <span style="margin-left: 10px">{{scope.row.remark}}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template slot-scope="scope">
              <el-button type="text" @click="addWorkAuto(1, scope.$index, scope.row)">编辑</el-button>
              <el-button type="text" @click="delSettings(scope.$index, scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="clear">
          <div class="block ft-page pull-right">
            <el-pagination
              :current-page="pageNum"
              :page-sizes="[5, 10, 20, 50]"
              :page-size="pageSize"
              layout="total, sizes, prev, pager, next, jumper"
              :total="totalData"
              @size-change ="handleSizeChange($event)"
              @current-change = "handleCurrentChange($event)">
            </el-pagination>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="workAutoVisible = false">取 消</el-button>
        <el-button type="primary" @click="workAutoVisible = false">确 定</el-button>
      </span>
    </el-dialog>
    <ProjectImport></ProjectImport>
    <high-condition></high-condition>
  </el-container>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
import ShareProject from '@/frontend/project/project-details/share-project' // 分享
import LibraryProject from '@/frontend/project/project-details/library-project' // 文库
import StatisticsProject from '@/frontend/project/project-details/statistics-project' // 统计
import KanbanSchedule from '@/frontend/project/project-details/kanban-schedule' // 统计
import TaskMain from '@/frontend/project/project-details/task-main'
// import SettingProject from '@/frontend/project/project-details/setting-project'
import ScreenProject from '@/frontend/project/project-details/screen-project' // 筛选
import SettingDilog from '@/frontend/project/project-details/setting-dilog' // 基本设置弹窗
// import MemberList from '@/frontend/project/project-details/member-list'

import DifferentTypesDetails from '@/frontend/project/project-details/different-types-details' // 不同详情的页面
import addTasktypeMain from '@/frontend/project/add_alltype_task/add-tastype-main' // 打开不同的新增界面
import memoEditor from '@/frontend/memo/memo-editor' //  备忘录

import projectDefined from '@/frontend/project/project-details/project-defined' // 任务自定义
import ListTaskCreate from '@/frontend/project/project-details/list-task-create'// 自定义新增界面
import ProjectAutoWorkflow from '@/frontend/project/project-details/project-auto-workflow' // 项目工作流
import setUpMore from '@/frontend/project/components/set-up-more'
// import ProjectImport from '../components/import-export'
import ProjectImport from '@/frontend/project/project-details/project-import'
import HighCondition from '@/common/alert/high-condition' // 高级条件
import { mapState, mapMutations } from 'vuex'
document.onclick = function (e) {
  let ele = document.getElementsByClassName('personCardcss')[0]
  let ele2 = document.getElementsByClassName('personCardcssnext')[0]
  if (ele) {
    ele.style.display = 'none'
  }
  if (ele2) {
    ele2.style.display = 'none'
  }
  let ele1 = document.getElementsByClassName('divNamePerson')
  if (ele1) {
    let index = ele1.length
    for (let i = 0; i < index; i++) {
      ele1[i].className = 'divNamePerson'
    }
  }
}
export default {
  name: 'DetailsMain',
  components: { ShareProject, LibraryProject, StatisticsProject, TaskMain, SettingDilog, ScreenProject, DifferentTypesDetails, addTasktypeMain, memoEditor, projectDefined, setUpMore, ProjectAutoWorkflow, ProjectImport, HighCondition, ListTaskCreate, KanbanSchedule },
  data () {
    return {
      style: {
        '-webkit-box-orient': 'vertical'
      },
      isFromWorkTable: false,
      showTaskCreate: false,
      editorData: {},
      workTableFromName: '', // 时间工作台跳转过来的项目名称
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      isshowRoleBox: false,
      StatisticsProcredd: {
        finishedcount: null, // 完成数
        waitfinishcount: null, // 未完成数
        taskscount: null // 任务总数
      },
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      totalData: 0,
      showusersStatus: false,
      formLabelAlign: {
        ProjectTemplateTitle: ''
      },
      rules: {
        ProjectTemplateTitle: [
          { required: true, message: '必填项', trigger: 'blur' },
          { min: 0, max: 25, message: '限制25个字以内', trigger: 'blur' }
        ]
      },
      addProjectPreviewVisible: false, // 保存为项目模版
      ProjectTemplateRemark: '', // 项目模版备注
      cardPersonNewDetails: {},
      autoWorkFlow: false,
      workAutoVisible: false,
      getListLoading: false,
      cardPersonDetails: {},
      projectDynamicList: [], // 项目动态
      projectDynamicListCopy: [],
      showPersonCard: false, // 显示隐藏人员卡片
      dropdownVisbli: 0, // 选择项目的下拉
      taskGropBox: false, // 看板视图显示隐藏
      taskGropDropdown: [], // 看板视图列表
      projectRoleList: [], // 项目角色列表
      taskGroupName: '', // 项目分组名称
      data: '',
      judgeIsScreen: 'false', // 判断是否筛选
      keyword: '',
      layout: {},
      checkedData: [],
      projectId: '',
      workbenchShowPage: '',
      ProjectTaskDetails: false,
      userInfo: {},
      projectRoleInfoList: '', // 人员权限数据
      dialogForCreate: false, // 备忘录新增
      memoDetail: {}, // 备忘录数据
      openCreateModal: false, // 任务新增
      gotoDetailsData: {},
      kanbanPreviewDialog: false,
      searchProject: '', // 搜索项目
      memberOutin: 0, // 成员相关
      viewOutin: 0, // 设置相关
      sceenProjectOutin: 0, // 筛选相关
      showOrHiddenSeeting: 0, // 显示或隐藏设置弹窗
      showOrHiddenDiffModel: 0, // 显示或隐藏选择视图弹窗
      activeName: '', // 任务 分享 文库 等相关切换显示
      showModuleBox: 0,
      options5: [{list: [], status: '0'}, {list: [], status: '1'}], // 星标项目和全部项目
      options5Copy: [{list: [], status: '0'}, {list: [], status: '1'}],
      value7: '1',
      workbenchActive: {
        one: 1,
        two: 0,
        three: 0,
        four: 0,
        five: 0,
        six: 0
      },
      submitData: {
        choosePro: '',
        id: '',
        star_level: 0 // 0 空 1 星标
      },
      modules: {}, //
      dataDtl: {}, //
      showTypeAddTask: '',
      switchProjectDefined: false,
      showOrHiddenBaseInfo: '1', // 显示或者隐藏基本信息栏
      BaseInfoWidth: '0px',
      projectBaseInfo: {}, // 项目的基本信息
      copyshowUserList: [],
      showUserList: [], // 项目成员列表
      userList: [], // 存放成员列表信息
      projectStatusStr: ['进行中', '归档', '暂停', '删除'],
      options3: [{
        label: '时间工作台',
        options: [{
          id: 0,
          label: '个人任务'
        }, {
          id: 9,
          name: '001'
        }]
      }, {
        label: '自定义工作台',
        options: []
      }],
      value8: 0
    }
  },
  mounted () {
    console.log(this)
    sessionStorage.setItem('judgeIsScreen', 'false')
    this.userInfo = (JSON.parse(sessionStorage.getItem('userInfo'))).employeeInfo
    if (localStorage.getItem('localStorageBaseInfo')) {
      console.log(localStorage.getItem('localStorageBaseInfo'))
      this.showOrHiddenBaseInfo = localStorage.getItem('localStorageBaseInfo') // 显示或者隐藏基本信息栏
      if (this.showOrHiddenBaseInfo === '1') {
        this.BaseInfoWidth = '350px'
      } else {
        this.BaseInfoWidth = '0'
      }
    }
    if (JSON.parse(sessionStorage.getItem('chooseProName'))) {
      this.submitData.choosePro = JSON.parse(sessionStorage.getItem('chooseProName')).name
      this.submitData.id = JSON.parse(sessionStorage.getItem('chooseProName')).id
      this.submitData.star_level = JSON.parse(sessionStorage.getItem('chooseProName')).star_level
    }
    this.projectId = this.$route.query.projectId
    // 从工作台跳转过来的才有
    this.isFromWorkTable = this.$route.query.status
    this.workTableFromName = this.$route.query.projectName
    if (this.isFromWorkTable === 'workTable') {
      // this.changeModle('three')
    } else {
      if (localStorage.getItem('showDiffTaskList')) {
        let status = JSON.parse(localStorage.getItem('showDiffTaskList'))
        this.changeModle(status)
      } else {
        this.changeModle('one')
      }
    }
    // this.$bus.on('changeProjectId', (projectId) => {
    //   this.projectId = projectId
    // })
    // let MainProactiveName = sessionStorage.getItem('MainProactiveName')
    // if (MainProactiveName) {
    //   this.activeName = MainProactiveName
    // } else {
    //   this.activeName = '1'
    // }
    this.$bus.$on('Aside', (data) => { // 侧边栏
      if (data === 'close') {
        this.viewOutin = 0
      }
    })
    this.$bus.$on('ScreenAside', (data) => { // 侧边栏关闭
      if (data === 'close') {
        this.sceenProjectOutin = 0
      }
    })
    // this.$bus.$on('MemberAside', (data) => { // 人员新增
    //   if (data === 'close') {
    //     this.memberOutin = 0
    //   }
    // })
    this.$bus.$on('closeTaskDetail', (data) => { // 关闭不同类型的详情
      this.ProjectTaskDetails = false
    })
    this.$bus.$on('closeDetailModal', (data) => { // 关闭自定义详情
      this.ProjectTaskDetails = false
    })
    this.$bus.$on('diffTypesDetails', (data) => { // 打开不同类型的详情
      this.gotoDetailsData = JSON.parse(data)
      this.ProjectTaskDetails = true
    })
    this.$bus.$on('taskOpenCreatModal', (data) => { // 打开新建或者编辑自定义弹窗
      let allDetails = JSON.parse(data)
      this.autoWorkFlow = allDetails.autoWorkFlow
      this.modules = allDetails.modules
      this.showTypeAddTask = allDetails.status
      this.openCreateModal = true
      if (allDetails.isEditorTaskData) { // 判断是否是编辑任务
        setTimeout(() => {
          this.$bus.$emit('editorTaskDetailData', data)
        }, 200)
      }
      let ele = document.getElementById('AddTask')
      let element = ''
      if (allDetails.autoWorkFlow) {
        element = document.getElementsByClassName('project-atuo-dialog')[0]
      }
      setTimeout(() => {
        let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
        let ele2 = document.getElementsByClassName('add-tasktype-main-warp')[0]
        if (ele2 && ele1 && ele) {
          ele1.style.zIndex = parseInt(ele.style.zIndex) + 1
          ele2.style.zIndex = parseInt(ele.style.zIndex) + 2
        }
        if (allDetails.autoWorkFlow && element) { // 自动化中的新建任务
          ele1.style.zIndex = parseInt(element.style.zIndex) + 1
          ele2.style.zIndex = parseInt(element.style.zIndex) + 2
        }
      }, 10)
    })
    this.$bus.on('customOpenCreateModal', (modules, data) => { // 打开自定义编辑
      this.modules = modules
      this.dataDtl = data
      this.showTypeAddTask = 'custom'
      this.openCreateModal = true
      let ele = document.getElementById('AddTask')
      setTimeout(() => {
        let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
        let ele2 = document.getElementsByClassName('add-tasktype-main-warp')[0]
        if (ele2 && ele && ele1) {
          ele1.style.zIndex = parseInt(ele.style.zIndex) + 1
          ele2.style.zIndex = parseInt(ele.style.zIndex) + 2
        }
      }, 10)
    })
    this.$bus.$on('customCloseCreateModal', () => { // 关闭自定义弹窗
      this.openCreateModal = false
      let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
      if (ele1) {
        ele1.style.zIndex = 5
      }
    })
    this.$bus.$on('closeProTaskCreateModal', () => { // 关闭任务自定义弹窗
      this.openCreateModal = false
      let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
      if (ele1) {
        ele1.style.zIndex = 5
      }
    })
    this.$bus.$on('addNewMemorandum', () => { // 备忘录弹窗开启
      this.dialogForCreate = true
    })
    this.$bus.$on('afterMemoSave1', value => { // 备忘录弹窗关闭
      if (value) {
        this.dialogForCreate = false
      }
    })
    // this.$bus.on('BaseSetting', value => { // 基础设置
    //   if (JSON.parse(value).status === 'defined') {
    //     console.log(value, '0000')
    //     this.switchProjectDefined = true
    //   }
    // })
    this.$bus.on('fromDetailCrumbs', value => { // 从详情跳转到层级界面，并打开相应的分组或者列表
      let data = JSON.parse(value)
      this.projectId = data.projectId
      this.ProjectTaskDetails = false
      this.changeModle(data.showDiffTaskList)
      this.changeTab(data.MainProactiveName)
      this.$bus.$emit('fromDetailCrumbsTwo', value)
    })
    this.$bus.on('projectDefined', value => { // 任务自定义设置
      this.switchProjectDefined = false
    })
    this.$bus.on('selectEmpDepRoleMulti', (data) => { // 监听获取成员
      if (data.prepareKey === 'detailsMain') {
        this.userList = JSON.parse(JSON.stringify(data.prepareData))
        this.addUserList(data.prepareData)
      }
    })
    this.$bus.on('kanbanPreviewList', (res) => { // 打开看板视图项目下的分组列表
      this.taskGropDropdown = JSON.parse(res)
    })
    this.$bus.$on('openPersonalTaskCreate', value => { // 新建个人任务
      if (value) {
        let data = JSON.parse(value)
        this.editorData = data.editor
      }
      this.showTaskCreate = true
    })
    this.$bus.$on('closeProTaskCreateModal', (res) => { // 关闭个人任务弹窗
      this.showTaskCreate = false
      // this.openCreateModal = false
      // let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
      // if (ele1) {
      //   ele1.style.zIndex = 5
      // }
    })
    this.getAllPro('') // 获取所有项目
    this.getBaseSetting(this.projectId) // 获取基本设置
    this.getMemberList(this.projectId) // 获取成员列表
    this.getDynmicList({id: this.projectId, bean: 'project_dynamic'}) // 获取项目动态
    this.getProjectRoleInfo({eid: this.userInfo.id, projectId: this.projectId}) // 获取项目角色
  },
  methods: {
    getAllPro (keyword) { // 获取所有项目
      let _this = this
      HTTPServer.queryAllWebList({'keyword': keyword, type: 0}, 'Loading').then((res) => {
        let arr = []
        _this.options5Copy[1].list = JSON.parse(JSON.stringify(res.dataList))
        _this.options5[1].list = JSON.parse(JSON.stringify(res.dataList))
        res.dataList.forEach((v, k) => {
          if (v.star_level === 1) {
            arr.push(v)
          }
        })
        _this.options5[0].list = arr
        _this.options5Copy[0].list = JSON.parse(JSON.stringify(arr))
        res.dataList.forEach((v, k) => {
          if (v.id === this.projectId) {
            this.formLabelAlign.ProjectTemplateTitle = v.name
            this.submitData.choosePro = v.name
            this.submitData.id = v.id
            this.submitData.star_level = v.star_level
            sessionStorage.setItem('chooseProName', JSON.stringify({name: v.name, id: v.id}))
          }
        })
      })
    },
    getBaseSetting (id, status) { // 获取项目基本设置信息
      HTTPServer.projectQueryById({id: id}, 'Loading').then((res) => {
        if (status && status === 1) {
          this.projectBaseInfo = res
          return false
        }
        let MainProactiveName = sessionStorage.getItem('MainProactiveName')
        if (MainProactiveName) {
          this.activeName = MainProactiveName
        } else {
          this.activeName = '1'
        }
        this.projectBaseInfo = res
        // 判断是不是项目负责人
        if (res.leader === this.userInfo.id) {
          for (let i in this.workbenchActive) {
            if (i === 'one') {
              this.workbenchActive[i] = 1
            } else {
              this.workbenchActive[i] = 0
            }
          }
          this.workbenchShowPage = 'one'
        }
        // 判断是不是从详情页面跳转的
        if (sessionStorage.getItem('isFromOtherPage')) {
          for (let i in this.workbenchActive) {
            if (i === 'two') {
              this.workbenchActive[i] = 1
            } else {
              this.workbenchActive[i] = 0
            }
          }
          this.workbenchShowPage = 'two'
        }
        // 判断是不是从工作台跳转过来的
        if (this.isFromWorkTable === 'workTable') {
          for (let i in this.workbenchActive) {
            if (i === 'three') {
              this.workbenchActive[i] = 1
            } else {
              this.workbenchActive[i] = 0
            }
          }
          this.workbenchShowPage = 'three'
          // 显示时间工作台-相关项目
          // 获取父列表
          this.value8 = this.projectId
          this.getWorkbenchType()
          // 获取子列表
          this.getWorkbenchSonType('0')
        }
      })
    },
    getMemberList (id) { // 根据项目id获取项目成员
      HTTPServer.MemberQueryList({'id': id}, 'Loading').then((res) => {
        let arr = []
        res.dataList.forEach((v, k) => {
          v.isshowRole = false
          v.active = false
          v.randomNumber = Math.floor(Math.random() * 4 + 1)
          if (k < 13) {
            arr.push(v)
          }
        })
        this.copyshowUserList = JSON.parse(JSON.stringify(res.dataList))
        this.showUserList = arr
        this.showusersStatus = false
      })
    },
    getDynmicList (data) { // 获取项目动态
      HTTPServer.getDynmicList(data, 'Loading').then((res) => {
        this.projectDynamicListCopy = JSON.parse(JSON.stringify(res))
        if (res && res.length > 10) {
          this.projectDynamicList = res.slice(0, 10)
        } else {
          this.projectDynamicList = res
        }
      })
    },
    openMoreDynamic () { // 点击更多显示更多项目动态
      HTTPServer.getDynmicList({id: this.projectId, bean: 'project_dynamic'}, 'Loading').then((res) => {
        this.projectDynamicListCopy = JSON.parse(JSON.stringify(res))
        let index = this.projectDynamicList.length + 10
        if (index > this.projectDynamicListCopy.length) {
          index = this.projectDynamicListCopy.length
        }
        this.projectDynamicList = this.projectDynamicListCopy.slice(0, index + 10)
      })
    },
    showAllPeroson (e) { // 显示所有项目成员
      // e.currentTarget.style.display = 'none'
      this.showusersStatus = true
      this.showUserList = this.copyshowUserList
    },
    getMainNode (id) { // 获取zhu节点
      HTTPServer.queryMainNode({'id': id}, 'Loading').then((res) => {
        this.taskGropDropdown = res.dataList
      })
    },
    openUserList () { // 打开选择成员弹窗
      let arr = []
      this.copyshowUserList.forEach((v, k) => {
        arr.push({id: v.employee_id, picture: v.employee_pic, name: v.employee_name, value: '1-' + v.employee_id})
      })
      this.userList = arr
      let senddata = {
        type: 3, 'prepareData': this.userList, 'prepareKey': 'detailsMain', 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': [], 'invitationOutMember': true
      }
      this.$bus.emit('commonMember', senddata)
    },
    getProjectRoleInfo (data) { // 获取项目权限
      HTTPServer.queryManagementRoleInfo(data, 'Loading').then((res) => {
        // 缓存项目的权限
        if (res && res.priviledge) {
          sessionStorage.setItem('projectRoleInfo', JSON.stringify(res.priviledge.priviledge_ids))
          this.projectRoleInfoList = res.priviledge.priviledge_ids
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
    perviewTemplate () { // 预览模版
      this.kanbanPreviewDialog = true
      this.$bus.$emit('openpreviewFromToMain')
      // let data = {}
      // data.status = 'open'
      // this.$bus.$emit('PreviewTemplateStatus', JSON.stringify(data))
    },
    addUserList (list) { // 添加成员
      let arr = []
      list.forEach((v, k) => {
        arr.push(v.id)
      })
      let senddata = {
        projectId: this.projectId,
        employeeIds: arr.join(',')
      }
      HTTPServer.MemberSave(senddata, 'Loading').then((res) => {
        this.getMemberList(this.projectId)
      })
    },
    editorMemberRole (v) { // 获取角色
      HTTPServer.getRoleMembersList({pageNum: 1, pageSize: 100, projectId: this.projectId}, 'Loading').then((res) => {
        this.projectRoleList = []
        res.dataList.forEach((v, k) => {
          v.isactive = false
          if (v.id === parseInt(this.cardPersonNewDetails.project_role)) {
            v.isactive = true
          }
        })
        this.projectRoleList = res.dataList
      })
    },
    changeTab (v) {
      sessionStorage.setItem('MainProactiveName', v)
      if (v !== '1') {
        sessionStorage.setItem('judgeIsScreen', 'false')
      }
      this.activeName = v
    },
    openListChange () { // 模版显示切换
      // this.showModuleBox = this.showModuleBox === 1 ? 0 : 1
      this.showOrHiddenDiffModel = 1
    },
    openMember () { // 打开成员列表
      this.memberOutin = this.memberOutin === 1 ? 0 : 1
      if (this.memberOutin === 1) {
        this.viewOutin = 0
        this.sceenProjectOutin = 0
      }
    },
    openSreenPro () { // 打开筛选
      this.sceenProjectOutin = 1
      if (this.sceenProjectOutin === 1) {
        this.viewOutin = 0
        this.memberOutin = 0
      }
      this.$bus.$emit('queryProjectTaskConditions', 'project', this.workbenchActive)
    },
    openSettingPopu () { // 打开设置
      this.showOrHiddenSeeting = 1
      // this.viewOutin = this.viewOutin ? 0 : 1
      // if (this.viewOutin === 1) {
      //   this.sceenProjectOutin = 0
      //   this.memberOutin = 0
      // }
    },
    hiddenSettingPopu () { // 隐藏弹窗
      this.showOrHiddenSeeting = 0
      this.showOrHiddenDiffModel = 0
      this.dropdownVisbli = 0
    },
    updateProjectAsterisk (v) { // 设置星标项目
      let star = v.star_level === 1 ? 0 : 1
      HTTPServer.updateProjectAsterisk({id: v.id, star_level: star}, 'Loading').then((res) => {
        console.log(res)
        this.getAllPro()
      })
    },
    hiddenoProper () {
      // let ele = document.getElementsByClassName('el-popover')[0]
      // if (ele) {
      //   ele.style.display = 'none'
      // }
    },
    openPersonCard (v, e) { // 打开人员卡片弹窗
      this.cardPersonNewDetails = v
      this.cardPersonDetails = {}
      this.getqueryEmployeeInfo(v)
      let offsetLeft = e.currentTarget.offsetLeft
      let clientY = window.event.clientY
      this.showUserList.forEach((val, key) => {
        val.active = false
      })
      v.active = true
      this.showPersonCard = true
      setTimeout(() => {
        let ele = document.getElementsByClassName('personCardcss')[0]
        ele.style.display = 'block'
        ele.style.right = '-' + (offsetLeft - 10) + 'px'
        ele.style.top = (clientY - 100) + 'px'
      }, 10)
    },
    getqueryEmployeeInfo (v) { // 获取成员详细信息
      HTTPServer.queryProjectEmployee({employeeId: v.employee_id}, 'Loading').then((res) => {
        this.cardPersonDetails = res
      })
    },
    // openEmailOrMemo (v) {
    //   this.$message({
    //     message: '功能正在开发中',
    //     type: 'warning'
    //   })
    // },
    openEmailOrMemo (type) { // 发邮件或者聊天
      if (type === 0) {
        this.$router.push({ path: '/frontend/cpChat', 'params': {sign_id: this.cardPersonDetails.sign_id} })
      } else {
        this.$router.push({path: '/frontend/EmailMain/EmailEpistolize', 'query': {judgeData: 'sendEmail', nickname: this.cardPersonDetails.email_name, account: this.cardPersonDetails.email}})
      }
    },
    addProjectPreComplate () { // 项目模板的保存
      if (!this.formLabelAlign.ProjectTemplateTitle) {
        this.$message({
          message: '项目名称为必填',
          type: 'warning'
        })
        return false
      }
      HTTPServer.saveProjectToTemplate({projectId: this.projectId, tempName: this.formLabelAlign.ProjectTemplateTitle}, 'Loading').then((res) => {
        this.addProjectPreviewVisible = false
        this.formLabelAlign.ProjectTemplateTitle = this.submitData.choosePro
        this.$message({
          message: '保存成功',
          type: 'success'
        })
      })
    },
    edtiorPerEvent (v, val) { // 移除或者修改人员角色
      if (v === 0) {
        this.$confirm('确定要删除该项目成员吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          HTTPServer.MemberDelete({id: val.id}, 'Loading').then((res) => {
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
            this.getMemberList(this.projectId)
          })
        }).catch(() => {
          let ele = document.getElementsByClassName('personCardcss')[0]
          if (ele) {
            ele.style.display = 'block'
          }
        })
      } else {
        this.projectRoleList = []
        this.editorMemberRole(val)
      }
    },
    handleCheckAllChange (val) { // 修改项目权限
      this.projectRoleList.forEach((v, k) => {
        v.isactive = false
      })
      val.isactive = true
      let senddata = {
        id: this.cardPersonNewDetails.id,
        projectRole: val.id,
        projectTaskRole: ''
      }
      HTTPServer.MemberUpdate(senddata, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '修改成功!'
        })
        this.showPersonCard = false
        this.getMemberList(this.projectId)
      })
    },
    changeModle (v) { // 切换视图显示
      this.showOrHiddenDiffModel = 0
      for (let i in this.workbenchActive) {
        if (i === v) {
          this.workbenchActive[i] = 1
        } else {
          this.workbenchActive[i] = 0
        }
      }
      this.workbenchShowPage = v
      localStorage.setItem('showDiffTaskList', JSON.stringify(v))
      // this.$bus.$emit('previewClassify', v)
    },
    searchClick () { // 搜索项目enter键
      let arr = []
      let arr1 = []
      this.options5Copy[0].list.map((v, k) => {
        if (v.name.indexOf(this.keyword) !== -1) {
          arr.push(v)
        }
      })
      this.options5Copy[1].list.map((v, k) => {
        if (v.name.indexOf(this.keyword) !== -1) {
          arr1.push(v)
        }
      })
      this.options5[0].list = arr
      this.options5[1].list = arr1
      // this.getAllPro(this.keyword)
      console.log(this.searchProject)
    },
    handleCommand (command) { // 搜索后，点击相应的项目
      this.$bus.$emit('changeProjectId', command.id)
      this.formLabelAlign.ProjectTemplateTitle = command.name
      this.projectId = command.id
      this.submitData.choosePro = command.name
      this.submitData.id = command.id
      this.submitData.star_level = command.star_level
      sessionStorage.setItem('chooseProName', JSON.stringify({name: command.name, id: command.id}))
      sessionStorage.setItem('storageProjectId', command.id)
      HTTPServer.projectQueryById({id: this.projectId}, 'Loading').then((res) => {
        this.projectBaseInfo = res
      })
      this.getMemberList(this.projectId) // 获取成员列表
      // this.dropdownVisbli = this.dropdownVisbli === 1 ? 0 : 1
      this.$router.push({path: '/frontend/project/detailsMain', query: {projectId: command.id}})
    },
    changeDropdown () {
      // setTimeout(() => {
      //   let ele = document.getElementsByClassName('el-dropdown-menu')[0]
      //   ele.setAttribute('style', 'height:700px;overflow:auto;padding:10px 20px;left: 85px;width:400px;top: 40px;')
      // }, 10)
      this.dropdownVisbli = this.dropdownVisbli === 1 ? 0 : 1
    },
    addTaskGroupChooseMoudel () { // 看板视图，新建分组
      if (!this.taskGroupName || this.taskGroupName.length > 25) {
        this.$message({
          message: '分组名称必填，且不能大于25个字！',
          type: 'warning'
        })
        return false
      }
      let isrepeat = 0
      this.taskGropDropdown.forEach((v, k) => {
        if (v.name === this.taskGroupName) {
          isrepeat = 1
        }
      })
      if (isrepeat === 1) {
        this.$message({
          message: '分组名称不能重复！',
          type: 'warning'
        })
        return false
      }
      HTTPServer.saveMainNode({'id': parseInt(this.projectId), name: this.taskGroupName}, 'Loading').then((res) => {
        this.getMainNode(this.projectId)
        this.kanbanPreviewDialog = false
      })
    },
    dialogSureBtn () {
      this.$bus.$emit('dialogSureBtn', 1)
      setTimeout(() => {
        console.log(this.memoDetail)
      }, 100)
    },
    showOrHidden (status) { // 显示或者隐藏基本信息栏
      this.showOrHiddenBaseInfo = this.showOrHiddenBaseInfo === '1' ? '0' : '1'
      let ele1 = document.getElementsByClassName('projectDynamic')[0]
      let ele2 = document.getElementsByClassName('personBoxList')[0]
      let ele3 = document.getElementsByClassName('projectBaseInfo')[0]
      if (this.showOrHiddenBaseInfo === '1') {
        if (ele1 && ele2 && ele3) {
          ele1.style.width = '100%'
          ele2.style.width = '100%'
          ele3.style.width = '100%'
        }
        this.BaseInfoWidth = '350px'
      } else {
        if (ele1 && ele2 && ele3) {
          ele1.style.width = '349px'
          ele2.style.width = '349px'
          ele3.style.width = '349px'
        }
        this.BaseInfoWidth = '0'
      }
      localStorage.setItem('localStorageBaseInfo', this.showOrHiddenBaseInfo)
    },
    openDilog (v) {
      if (v === 'import' || v === 'export') {
        if (v === 'import') {
          this.$bus.$emit('openImportDialog', {bean: this.projectId, progressActive: 1, importButton: '下一步', finishStatus: 'success'})
        } else {
          console.log('导出')
          this.getAllLayout()
        }
        return false
      }
      if (v === 7) {
        this.addProjectPreviewVisible = true
        return false
      }
      if (v === 'defined') {
        this.switchProjectDefined = true
        return false
      }
      if (v === 'autoFlowManage') {
        let senddata = {
          project_id: this.projectId,
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }
        this.getprojectQueryAutomationList(senddata)
        this.workAutoVisible = true
        return false
      }
      this.showOrHiddenSeeting = 0
      let data = {
        projectId: this.projectId,
        status: v
      }
      this.$bus.emit('BaseSetting', JSON.stringify(data))
    },
    getAllLayout () { // 获取项目自定义布局
      HTTPServer.getAllLayout({'bean': 'project_custom_' + this.projectId}, 'loading').then((res) => {
        this.layout = res.enableLayout.rows
        this.getAllProTasks()
      })
    },
    getAllProTasks () { // 获取项目下所有任务
      HTTPServer.queryAllTaskList({'projectId': this.projectId}, 'loading').then((res) => {
        this.checkedData = res.dataList
        this.exportProjectTask()
      })
    },
    // 根据组件类型显示列表字段
    getType (name) {
      let type = name.split('_')[0]
      return type
    },
    exportProjectTask () { // 导出项目任务
      let tableHeader = []
      this.layout.map((item, index) => {
        if (item.type !== 'multitext') { // 过滤富文本
          tableHeader.push({
            colname: item.name,
            coltext: item.label
          })
        }
      })
      let tableList = []
      this.checkedData.map((item, index) => {
        let obj = {}
        this.layout.map((item2, index) => {
          let value
          if (this.getType(item2.name) === 'personnel' || this.getType(item2.name) === 'department' || this.getType(item2.name) === 'reference') {
            let person = []
            if (Array.isArray(item[item2.name])) {
              item[item2.name].map((item4, index4) => {
                person.push(item4.name)
              })
            }
            value = person.toString()
          } else if (this.getType(item2.name) === 'picklist' || this.getType(item2.name) === 'multi' || this.getType(item2.name) === 'mutlipicklist') {
            let picklist = []
            if (Array.isArray(item[item2.name])) {
              item[item2.name].map((item4, index4) => {
                picklist.push(item4.name)
              })
            }
            value = picklist.toString()
          } else if (this.getType(item2.name) === 'datetime') {
            if (item[item2.name]) {
              value = tool.formatDate(item[item2.name], 'yyyy-MM-dd HH:mm:ss')
            } else {
              value = ''
            }
          } else if (this.getType(item2.name) === 'area') {
            // 未实现
            value = item[item2.name]
          } else if (this.getType(item2.name) !== 'multitext') {
            value = item[item2.name]
          }
          obj[item2.name] = value
        })
        tableList.push(obj)
      })
      tool.JSONToCSV(tableList, tableHeader, '项目任务')
    },
    addWorkAuto (v, index, row) { // 打开或编辑自动化
      this.workAutoVisible = false
      this.$bus.emit('BaseSettingAutoWork', JSON.stringify({projectId: this.projectId, status: v === 1 ? 'editorAutoWork' : 'autoFlowManage', data: row}))
    },
    delSettings (index, row) { // 删除自动化列表
      HTTPServer.projectDeleteAutomation({id: row.id, projectId: this.projectId, title: row.title}, 'Loading').then((res) => {
        let senddata = {
          project_id: this.projectId,
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }
        this.getprojectQueryAutomationList(senddata)
      })
    },
    getprojectQueryAutomationList (data) { // 获取自动化列表
      HTTPServer.projectQueryAutomationList(data, 'Loading').then((res) => {
        this.tableData = res.dataList
        this.totalData = res.pageInfo.totalRows
        this.pageNum = res.pageInfo.pageNum
        this.pageSize = res.pageInfo.pageSize
      })
    },
    openProjectProgress () { // 点击显示项目进度
      HTTPServer.queryTaskAnalysisByProjectId({projectId: this.projectId}, 'Loading').then((res) => {
        this.StatisticsProcredd = res
      })
    },
    // 分页
    handleSizeChange (data) {
      console.log(data, '每页数量')
      this.pageSize = data
      let datas = {
        project_id: this.projectId,
        pageNum: this.pageNum,
        pageSize: this.pageSize
      }
      this.ajaxGetWorkFlowAutoSettingsList(datas)
    },
    // 分页
    handleCurrentChange (data) {
      console.log(data, '当前页')
      this.pageNum = data
      let datas = {
        project_id: this.projectId,
        pageNum: this.pageNum,
        pageSize: this.pageSize
      }
      this.ajaxGetWorkFlowAutoSettingsList(datas)
    },
    isShowHid (v) { // 显示隐藏描述
      let ele1 = document.getElementsByClassName('ProjectDesc')[0]
      let ele = document.getElementsByClassName('ProjectDescItem')[0]
      if (v === 0) {
        ele.style.display = 'block'
        ele.style.top = (ele1.offsetHeight + 5) + 'px'
      } else {
        ele.style.display = 'none'
      }
    },
    edtiorName (name, status) {
      if (name) {
        let newname = status === 2 ? name.slice(-2) : name.slice(-3)
        return newname
      }
    },
    // 获取工作台父列表
    getWorkbenchType () {
      HTTPServer.queryListByFilterAuth().then(res => {
        console.log(res, '父列表')
        this.options3[1].options = []
        res.map((v, k) => {
          v.name = v.workbench_name
          if (k !== 0) {
            this.options3[1].options.push(v)
          }
        })
        this.workbenchTypeData = res
      })
    },
    // 获取工作台子列表
    getWorkbenchSonType (fromType) {
      // formType 0:时间工作台  1:企业工作流
      HTTPServer.queryWorkflowListBy({'fromType': fromType}).then(res => {
        res.map((v, k) => {
          if (k === 0) {
            v.name = '个人任务'
          }
        })
        this.options3[0].options = res
      })
    },
    getTimeWorkList (item, key) {
      this.$router.push({path: '/frontend/workbench'})
      // if (this.workbenchType === '时间工作台') {
      // if (key === 0) {
      //   if (item.id !== 0) {
      //     this.$bus.$emit('changeProjectId', item.id)
      //     this.projectId = item.id
      //     HTTPServer.projectQueryById({id: this.projectId}, 'Loading').then((res) => {
      //       this.projectBaseInfo = res
      //     })
      //     this.$router.push({path: '/frontend/project/detailsMain', query: {projectId: item.id, status: 'workTable'}})
      //   } else {
      //     this.$router.push({path: '/frontend/workbench', query: {status: 'perosonalTask', id: 0}})
      //   }
      // } else {
      //   this.$router.push({path: '/frontend/workbench', query: {status: 'workTable', id: item.id}})
      // }
    },
    overShow () {
      let ele = document.getElementsByClassName('elAsideBaseInfo')[0]
      ele.setAttribute('style', 'border-left-color: #3DA8F5')
    },
    outHide () {
      let ele = document.getElementsByClassName('elAsideBaseInfo')[0]
      ele.setAttribute('style', 'border-left-color: transparent')
    },
    // 状态管理
    ...mapMutations({
      projectStatusChange: 'PROJECT_STATUS'
    })
  },
  watch: {
    keyword () {
      if (!this.keyword) {
        this.options5[0].list = this.options5Copy[0].list
        this.options5[1].list = this.options5Copy[1].list
      }
    },
    projectStatus: {
      handler: function (val, oldval) {
        this.getBaseSetting(this.projectId, 1)
      },
      deep: true
    }
  },
  filters: {
    // 触发事件对应label
    triggersToText (type) {
      let text
      if (type === '0') {
        text = '在新建记录保存后'
      } else if (type === '1') {
        text = '在编辑记录保存后'
      } else if (type === '2') {
        text = '在新建或编辑记录保存后'
      } else if (type === '3') {
        text = '被完成'
      } else if (type === '4') {
        text = '被移动'
      } else {
        text = '被重新激活'
      }
      return text
    }
  },
  beforeDestroy () {
    this.$bus.off('Aside')
    this.$bus.off('ScreenAside')
    // this.$bus.off('MemberAside')
    this.$bus.off('closeTaskDetail')
    this.$bus.off('closeDetailModal')
    this.$bus.off('diffTypesDetails')
    this.$bus.off('taskOpenCreatModal')
    this.$bus.off('closeProTaskCreateModal')
    this.$bus.off('addNewMemorandum')
    this.$bus.off('afterMemoSave1')
    this.$bus.off('selectEmpDepRoleMulti')
    this.$bus.off('customCloseCreateModal')
    this.$bus.off('fromDetailCrumbs')
    this.$bus.off('customOpenCreateModal')
    this.$bus.off('kanbanPreviewList')
  },
  computed: {
    newProjectId () {
      return this.projectId
    },
    ...mapState({
      projectStatus: state => state.projectData.project_status
    })
  }
}
</script>
<style lang="scss" scoped>
  .main-project-details-wrap{
    height: 100%;
    .el-main{
      padding: 0
    }
    .topHeader{
      line-height: 60px;font-size: 20px;border-bottom: 1px solid #ddd;
      .topheaderDropdown{
        display: flex;max-width:250px;height:62px;
        >span{font-size:18px;color:#4A4A4A;max-width:250px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;}position: relative;&:hover{cursor: pointer;}
        >i{
          height: 20px;
          margin-top: 23px;
        }
        >div.dropdownItem{
          position: absolute;top:50px;left:-70px;background:#fff;width:400px;max-height:680px;overflow: auto;box-shadow: 2px -2px 4px 0 rgba(155,155,155,0.30), -2px 2px 4px 0 rgba(155,155,155,0.30);z-index:5;border-radius: 5px;padding:10px;
        }
      }
      .imgCss{
        display: inline-block;width: 70px;
        &:hover{cursor: pointer;}
        >span{font-size: 16px;}
      }
      .headerDiv{
        float:left;
        .bgColorIcon{
          background: #ddd;
        }
      }
      .headerDiv:first-child,.headerDiv:last-child{
        width: 30%;
        text-align: left;
      }
      .headerDiv:last-child{
        text-align: right;
      }
      .headerDiv:nth-child(2){
        width: 40%;
        text-align: center;
        &:hover{
          cursor: pointer;
        }
        .el-col:hover{
          color: #409EFF;
        }
      }
      .middleTab>div:first-child{
        position: relative;
        i.el-icon--right{
          position: absolute;right:-20px;top:25px;
        }
      }
      .middleTab>div{
        display: inline-block;
        .taskGropBox{
          position: absolute;background: #fff;width:200px;box-shadow: 2px 2px 5px #ddd;border:1px solid #ddd;border-radius: 5px;
          padding:10px 20px;top:50px;left:-120px;z-index:5;
          >div.itemList{&:hover{background: #DDDDDD;}}
          >div{height:40px;line-height: 40px;
            >span{color:#000;font-size:14px;}
          }
        }
        margin: 0 20px;
        &:hover{
          >span{
            height: 59px;
            border-bottom: 2px solid #409EFF;
          }
        }
        >span{
          font-size: 18px;display: inline-block;
        }
      }
      .activeTab{
        color: #409EFF;
        height: 59px;
        border-bottom: 2px solid #409EFF;
      }
      // #selectId .el-input--suffix .el-input__inner{
      //   text-align: center;
      //   border: 0;
      //   font-size: 17px;
      // }
      .selectId .icon-xingxing{
        font-size: 18px;margin-right:5px;color:#ddd;cursor: pointer;
      }
      .selectId .icon-xingxing.active{ color: #FF6F00;}
      .selectId{
        display: flex;
      }
      #projectTaskDro{
        width: 200px;
        >span{
          display: inline-block;width: 200px;font-size: 17px;text-align: right;cursor: pointer;
        }
      }
      .headerTopRight{
        float: right;
        >div.settingProBox{
          i{font-size: 20px;}
        }
        >div{
          >i{font-size: 18px;}
          float: left;text-align: center;height: 59px;
          &:hover{cursor: pointer;}
        }
        >div.moduleList{width:100px;}
        >div.screensetting{padding: 0 10px;}
        >div.settingProBox{width:100px;}
      }
    }
    .asideright{
      position:fixed;top:0;right:0;height: 100%;
      border: 1px solid #ddd;
      box-shadow: -5px 5px 10px #ddd;
      // overflow: hidden;
      background: #fff;
      z-index: 5
    }
    .asiderightMask{
      position:fixed;width:100%;height: 100%;top:0;right:0;bottom:0;left:0;
      background: transparent;
      z-index: 4
    }
    .memberaside-in{
      width: 250px;
      height: 80%;
      transition: width .5s linear;
    }
    .icon-shitu,.icon-pc-filter,.icon-gerenshezhi1{color:#A0A0AE;}
    .memberaside-out{
      width: 0;
      height: 80%;
      transition: width .5s linear;
      border: 0;
    }
    .asideright-in{
      width: 300px;
      transition: width .5s linear;
    }
    .asideright-out{
      width: 0;
      transition: width .5s linear;
      border: 0;
    }
    .asiderightPosition-in{
      right: 350px;
      transition: right .5s linear;
    }
    .asiderightPosition-out{
      right: 0;
      transition: right .5s linear;
    }
    .containerChangeBaseInfo{
      position: relative;
      // .fixedBaseInfo{
      //   position: absolute;top:33%;height:80px;line-height:80px;width:15px;text-align:center;&:hover{cursor:pointer;}
      //   z-index:1;
      //   i{color:#7C9CBC;}
      //   i.icon-shouqi1{position: absolute;left:-3px;}
      //   >span{position: absolute;border:10px solid transparent;width:0;height:0;}
      //   >span.spanTop.out{top:-10px;;border-bottom-color:#EBEDF0;left:-10px;transform: rotate(-45deg);}
      //   >span.spanBottom.out{bottom:-10px;border-top-color:#EBEDF0;left:-10px;transform: rotate(45deg);}
      //   >span.spanTop.in{top:-10px;;border-bottom-color:#fff;left:4px;transform: rotate(45deg);}
      //   >span.spanBottom.in{bottom:-10px;border-top-color:#fff;left:4px;transform: rotate(-45deg);}
      // }
      .fixedBaseInfo{
        cursor: pointer;
        position: absolute;
        border-radius: 4px 0 0 4px;
        background: #D0D0D0;
        text-align: center;
        top:52.6%;
        height:30px;
        line-height: 30px;
        width:15px;
        right:0;
        z-index:2;
        .side-p-item{
          height:14px;width:2px;background: #fff;margin: 8px 0 0 7px;
        }
        >i.icon-iconfontjiantou1,>i.icon-xuanrenkongjian-icon5{
          font-size:12px;color:#E9EAEE;display: none;
        }
        &:hover{
          background: #3DA8F5;
          >i{
            color:#fff;display: block;
          }
          .side-p-item{
            display: none;
          }
        }
      }
      // .fixedBaseInfo.in{right:-15px;background: #EBEDF0;border-left:1px solid #EEEEEE;}
      // .fixedBaseInfo.out{right:0;background: #fff;}
      .personCardcss{
        position: absolute;right:0;top:40%;z-index:5;min-height:195px;width:360px;background: #FFFFFF;box-shadow: -2px -2px 4px 0 rgba(155,155,155,0.30), 2px 2px 4px 0 rgba(155,155,155,0.30);border-radius:5px;
        >div.firstchildDiv{
          padding:20px 0;display: flex;padding-bottom:10px;
          .topPicAddName{min-height:117px;img{height:50px;width:50px;}}
          .personPic{
            width:120px;text-align: center;border-right:1px solid #ddd;height:160px;
            >div:first-child{
              .peoplePic{
                height:50px;width:50px;border-radius: 50%;text-align:center;line-height:50px;display:inline-block;position: relative;
                p.userName{color:#fff;background: #37AEFF;border-radius: 50%;}
                img{border-radius: 50%;}
                p.userGender{
                  position: absolute;bottom:0;right:0;height:15px;width:15px;border:1px solid #fff;border-radius: 50%;background:#8DB7F7;text-align:center;line-height:15px;
                  i{
                    font-size:12px;color:#fff;position: absolute;top:0;left:0;transform: scale(.8);
                  }
                }
              }
            }
            >div:last-child{
              display: flex;text-align:center;margin-top:15px;padding-left:10px;
              >div{cursor:pointer;border-radius:3px;width:40px;height:25px;line-height:25px;margin:0 5px;background: #549AFF;i{color: #fff;}}
              >div:last-child{i{font-size:18px;}}
            }
            >div.twoButton{
              .el-button{
                padding: 3px 11px;
              }
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
        >div.isEnableActive{
          position: absolute;top:0;width:80px;right:0;display: flex;height:30px;line-height:30px;margin-top:5px;
          >span:first-child{width:10px;height:10px;border-radius: 50%;background: #FAAD14;margin:10px 10px 0 0;}
          >span:last-child{color: rgba(0,0,0,0.45);}
        }
      }
    }
    .elAsideBaseInfo{
      box-shadow: 0 0 8px #ddd;border-left:2px solid transparent;
      .baseInfosetting{
        width:100%;
        i{margin-right:10px;font-size:20px;color:#999;}span{font-size: 16px;color:#333333;}padding:15px 15px;height:60px;border-bottom:1px solid #D9D9D9;line-height: 30px;
      }
      .projectBaseInfo{
        width:100%;padding:10px 0;margin-bottom:10px;
        >p{
          height:30px;line-height: 30px;padding:0 20px;display: flex;
          >span:first-child{width:70px;text-align: left;color:#666666;}
          .startendTimeCss{text-align: left;}
        }
        .moreTextStyle{
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          width: 250px;
        }
        .projectStatus0{padding:0 15px;border-radius: 3px;background: #92D66D;color:#fff;height:28px;}
        .projectStatus1{padding:0 15px;border-radius: 3px;background: #82C5FA;color:#fff;height:28px;}
        .projectStatus2{padding:0 15px;border-radius: 3px;background: #FFBF00;color:#fff;height:28px;}
        .progressCss{
          cursor: pointer;.el-progress.el-progress--line{width:200px;margin-top: 8px;}
        }
        >div{
          padding:0 20px;position: relative;margin:3px 0;
          .ProjectDescItem{
            display: none;
            position: absolute;left:5px;top:55px;width:320px;background: #fff;padding:10px 20px;border-radius: 5px;z-index:5;
            box-shadow: 0 0 10px #ddd;
            i{position: absolute;width:10px;height:10px;border:1px solid #ddd;border-right:0;border-bottom:0;top:-5px;left:170px;transform: rotate(45deg);background: #fff;}
            span{color:#666;}
          }
          >span{float:left;}
          >span:first-child{width:80px;text-align: left;color:#666;}
          >span.ProjectDesc{
            width:215px;
            // max-height: 115px;
            overflow : hidden;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-line-clamp: 6;
            -webkit-box-orient: vertical;
          }
        }
        >div:after{content: '';display: table;clear: both;}
      }
      .projectPerson{
        >p{border-bottom:1px solid #ddd;padding:10px 0 10px 15px;font-size: 16px;color:#333333;i{margin-right:10px;font-size:20px;color:#999;}}width:100%;position: relative;
        .moudelBox{
          position: absolute;top: 0;right: 0;bottom: 0;left: 0;overflow: auto; margin: 0;background-color:rgba(0,0,0,0.2);z-index: 5;
          .chooseProRoleBox{
            margin:20px auto;background: #fff;width:100px;border: 1px solid #ddd;
          }
        }
        .personBoxList{width:100%;padding: 10px 16px;overflow:hidden;>div{float:left;}>div:last-child{height:80px;width:40px;line-height: 80px;}}
        .personListCss{
          height:80px;width:40px;margin:5px 10px;position: relative;
          .addperson{height:40px;width:40px;text-align: center;line-height:40px;i{font-size:40px;color:#A0A0AE;}}
          .chooseProRole{
            position:absolute;top:0;left:-100px;z-index:10;background: #fff;width:100px;border: 1px solid #ddd;
          }
          &:hover{.divNamePerson{color:#37AEFF;}cursor: pointer;}
          .personTopImgOrName{
            width:40px;height:40px;text-align: center;line-height: 40px;border-radius:50%;
            img{width:100%;height:100%;vertical-align: sub;border-radius: 50%;}
          }
          .noPictrue1{color:#fff;background: #30C790;}
          .noPictrue2{color:#fff;background: #FF8181;}
          .noPictrue3{color:#fff;background: #37AEFF;}
          .noPictrue4{color:#fff;background: #0ACFC5;}
          >div.divNamePerson{height:40px;line-height: 40px;text-align: center;font-size: 12px;}
          .divNamePerson.active{color:#37AEFF;}
          >span.delperson,>span.editorperson{
            position:absolute;top:-8px;i{font-size: 12px;}&:hover{cursor:pointer;}
          }
          >span.delperson{right:-5px;}
          >span.editorperson{left:-5px}
        }
        >div.personBoxList:after{content:'';display:block;clear:both;}
      }
      .projectDynamic{
        border:0;width:100%;
        >p{padding:0 0 10px 15px;font-size: 16px;color: #333333;border-bottom:1px solid #ddd;i{margin-right: 10px;font-size: 20px;color: #999;}}
        .projectStatusNext{
          >div{padding:0 0 8px 10px;font-size: 16px;border-bottom:1px dashed #ddd;}
          padding:10px 20px;
          .timeProjectDy{color:#ccc;font-size:12px;}
        }
        .moresetting{
          span{color:#446078;cursor:pointer;padding:0 20px 10px 30px;}
        }
      }
    }
    .settingProBox{
      position: relative;
      >div{
        width:220px;max-height:350px;position: absolute;top:58px;right:-5px;background:#fff;z-index:4;box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);border-radius:5px;
        .settingProNewItem{
          height:50px;line-height:50px;padding:0 20px;&:hover{cursor:pointer;background:#F2F2F2;span{color:#1890FF;}}text-align: left;
        }
      }
    }
    .moduleList{
      position: relative;
      >div{
        width:220px;min-height:50px;position: absolute;top:58px;right:-5px;background:#fff;z-index:3;box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);border-radius:5px;
        .popoverClass{
          padding: 0 20px; height:50px; line-height: 50px;text-align: left;&:hover{cursor: pointer; color:#409EFF;background: #F2F2F2;}position: relative;
          i{position: absolute;top: 0;right: 20px;}
        }
      }
    }
  }
  .popoverClass.active{color:#409EFF;}
  .workTable-item{
    >i{
      font-size:20px;cursor: pointer;margin-right:40px;
    }
    >span{
      font-size:16px;max-width:300px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
</style>
<style lang="scss">
// #selectId .el-input__inner{
//   text-align: center; border: 0; font-size: 17px;padding: 0;
// }
#chooseimgitem{
  .el-dialog__header{border: 1px solid #ddd;}
  .el-dialog__body{padding: 0;}
  div.imgMain{
    height: 400px;padding-top: 20px;
    >div:first-child{
      height: 330px;padding: 0 15px;overflow: auto;
      >div.itemImg{
        float: left;height: 80px;width: 120px;margin: 5px 10px; background: #ddd;
        img{width:100%;height:100%;}
        &:hover{cursor: pointer;}
      }
      >div.addImg{
        &:hover{cursor: pointer;}
        float: left; position: relative; width: 100%;padding-left: 10px; height: 50px; line-height: 50px;
        i{font-size:30px;color:#BBBABA;}
        span{
          position: absolute;top:0;left:0;left: 50px;color: #BBBABA;
        }
      }
    }
    >div:last-child{
      height: 50px;text-align: center;line-height: 50px;border-top: 1px solid #ddd;
      span{
        padding: 5px 20px;border-radius: 5px;background: #009999;color: #fff;&:hover{cursor:pointer;}
      }
    }
  }
}
.titleTop{
  height:40px;line-height:40px;font-size:16px;color:#747477;i{color: #FF6F00;}border-bottom:1px solid #D8D8D8;padding:0 10px;margin-bottom:10px;
}
.itemBox{
  height:50px;line-height:50px;padding:5px 20px 0 20px;&:hover{background-color: #F2F2F2;color: #66b1ff;cursor: pointer;}display: flex;
  >img,>span{height: 40px;line-height: 40px;}
  >img{
    height:40px;width:60px;border-radius:5px;margin-right: 10px;
  }
  >span{
    color:#212121;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
.itemBox.active{background:#ECF5FF;}
.shadeTaskDetails{
  position: fixed;
  top:0;
  left:0;
  width:100%;
  height:100%;
  background: rgba(0, 0, 0, 0.4);
  z-index: 5;
}
.project-defined-box {
  position: fixed;
  top:0;
  right:0;
  width:100%;
  height: 100%;
  z-index:2;
}
.project-defined-enter-active, .project-defined-leave-active {
  transition: all .8s linear;
}
.project-defined-enter, .project-defined-leave-to {
  transform: translateX(90%);
  transition: all .8s linear;
}
.project-defined-mask {
  position: absolute;
  height: 100%;
  width: 100%;
  background: #000;
  opacity: 0.5;
  top:0;
  // z-index:1001;
}
.project-defined-mask-enter-active, .project-defined-mask-enter-active {
  transition: all .8s linear;
}
.project-defined-mask-enter, .project-defined-mask-leave-to {
  // opacity: .5;
  transition: all .8s linear;
}
.main-project-details-wrap{
  .elAsideBaseInfo{
    // border-left:1px solid #ddd;
  }
  .elAsideBaseInfo.activeOut{width:0;transition:width .5s linear;}
  .elAsideBaseInfo.activeIn{width:350px;transition:width .5s linear;}
}
.el-message-box__wrapper{
 .el-message-box>.el-message-box__header{background:#fff;.el-message-box__title span{color:#000;}.el-message-box__close.el-icon-close{color:#000;}}
}
#kanbanPreviewDialog{
  .el-dialog__header{display:none;}
  .footerTaskPreview{
    float:left;&:hover{cursor: pointer;}>span{color:#58ACFA;}>i{color:#4CD1C4;font-size:20px;}margin-top:5px;
  }
}
.checkedStyleProTask{
  float: right;.el-checkbox__label{display:none;}
}
.authListcss{
  padding:0 10px;height:40px;line-height: 40px;&:hover{cursor: pointer;background: #F2F2F2;span{color:#1A8AF0;}}
  .spanauthactive{color:#1A8AF0;}
}
.authListcss.authactive{background: #F2F2F2;}
.ProjectTemplateRemark{
  .el-dialog__header{border-bottom:1px solid #E7E7E7;.el-dialog__title{font-size:16px;}}
  .el-dialog__footer{
    border-top:1px solid #E7E7E7;padding:15px 20px;
    .el-button{
      width:65px;height:32px;line-height:32px;padding:0;text-align:center;
    }
  }
}
.main-project-details-wrap{
  .workAutoStyle{
    .el-dialog__header{
      padding:15px 20px;border-bottom:1px solid #ddd;
    }
    .el-dialog__body{
      padding:10px 20px;
    }
    .el-dialog__footer{
      padding:10px 20px;border-top:1px solid #ddd;
      .el-button{
        height:32px;width:65px;line-height:32px;padding:0;text-align:center;
      }
    }
    .workAutoStyle-item{
      .first-item{font-size:16px;color:#999;padding:5px 0;}
      .senond-item{height:28px;width:65px;line-height:28px;padding:0;text-align:center;margin:10px 0;}
    }
    .clear{margin-top:10px;}
  }
}
.myDettailsTask.el-popper{
  color:#fff;opacity: .8;background:#686868;margin-top:5px;text-align:center;padding:10px 0;
  .popper__arrow{
    display:none;
  }
  .showBoxItem{
    >div>span{
      margin-left:10px;
    }
  }
}
</style>
<style lang="scss">
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
    transform: translateX(900px);
  }
.showtaskcreateforpro{
  background: #FFFFFF;
  position: fixed;
  width: 900px;
  top: 0;
  bottom: 0;
  right: 0;
  z-index: 11;
  box-shadow: -2px 2px 4px 0 rgba(155, 155, 155, 0.3), 2px -2px 4px 0;
}
</style>
