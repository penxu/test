<template>
  <el-dialog :visible.sync="dialogVisible" :close-on-click-modal="false" class="main-setting-dilog-wrap" :width="status==='标签管理'?'500px':'800px'" id="settingBody" append-to-body>
    <template slot="title">
      <div v-text="status" class="titleStatus" :class="status==='标签管理'?'active':''"></div>
    </template>
    <div id="settingCss">
      <!-- 基本设置 -->
      <div v-if="status==='项目设置'">
        <el-container>
          <el-aside width="150px" style="border-right:1px solid #ddd;height:540px;">
            <el-menu :default-active="activeIndex" class="el-menu-vertical-demo" @select="handleOpen">
              <el-menu-item index="1">
                <i class="iconfont icon-jibenxinxi"></i>
                <span slot="title">基本信息</span>
              </el-menu-item>
              <el-menu-item index="2">
                <i class="iconfont icon-zhuangtai"></i>
                <span slot="title">状态设置</span>
              </el-menu-item>
              <el-menu-item index="3">
                <i class="iconfont icon-xiangmujindu"></i>
                <span slot="title">项目进度</span>
              </el-menu-item>
              <el-menu-item index="4">
                <i class="iconfont icon-quanxianshezhi"></i>
                <span slot="title">权限设置</span>
              </el-menu-item>
              <el-menu-item index="5">
                <i class="iconfont icon-renwutixing"></i>
                <span slot="title">任务提醒</span>
              </el-menu-item>
              <el-menu-item index="6">
                <i class="iconfont icon-renwushezhi"></i>
                <span slot="title">任务设置</span>
              </el-menu-item>
              <el-menu-item index="7">
                <i class="iconfont icon-yanse"></i>
                <span slot="title">外观</span>
              </el-menu-item>
            </el-menu>
          </el-aside>
          <!-- 基本信息 -->
          <el-main v-if="activeIndex==='1'">
            <el-form ref="form" :model="form" label-width="90px">
              <!-- <el-form-item label="项目图标" class="projectImg">
                <div class="showProImg"><img v-if="userDetails.picture" :src="userDetails.picture + '&TOKEN=' + token" alt=""></div>
                <div class="showProImg"><img :src="myImg + '&TOKEN=' + token" alt=""></div>
                <div v-if="projectBaseInfoRoleInfo.editorProInfo">
                  <p class="changeProjectImg" @click.stop="changeProjectPic = true">更换图标</p>
                  <p class="uploadImg">
                    <input type="file" id="upLoadFile" accept="image/*" @change="getFiles($event)" class="inputCss">
                    <i class="iconfont icon-icon-test"></i> 上传图片
                  </p>
                </div>
              </el-form-item> -->
              <el-form-item label="项目名称">
                <el-input v-if="projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'" v-model="form.name"></el-input>
                <span v-else v-text="form.name"></span>
              </el-form-item>
              <el-form-item label="项目状态">
                <span class="firststatusPro" :class="'firststatusPro'+project_status">{{['进行中','归档','暂停','删除'][project_status]}}</span>
              </el-form-item>
              <el-form-item label="负责人">
                <div class="imageOrText">
                  <span v-if="JSON.stringify(userDetails) != '{}'" class="firstImage">
                    <i v-if="projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'" @click.stop="userDetails={}" class="iconfont icon-shanchu"></i>
                    <img v-if="userDetails.picture&&userDetails.picture!='null'" :src="userDetails.picture + '&TOKEN=' + token" :title="userDetails.name">
                    <span v-else v-text="changeName(userDetails.name)" class="firststatusPro0"></span>
                  </span>
                  <span v-if="JSON.stringify(userDetails) == '{}'"  @click="openUserList" class="secondImage"><i class="iconfont icon-jiaren"></i></span>
                </div>
              </el-form-item>
              <el-form-item>
                <template slot="label">
                  <!-- <span>开始时间</span> -->
                  <span v-if="form.startTime||projectBaseInfoRoleInfo.editorProInfo">开始时间</span>
                  <span v-if="!form.startTime&&!projectBaseInfoRoleInfo.editorProInfo">截至时间</span>
                </template>
                <el-col :span="10" v-if="form.startTime||projectBaseInfoRoleInfo.editorProInfo">
                  <div class="dateTimeStyle" v-if="projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'">
                    <i class="iconfont icon-riqi"></i>
                    <span v-if="form.startTime"><span>{{form.startTime | formatDate('yyyy-MM-dd HH:mm')}}</span><i class="iconfont icon-shanchu" @click="form.startTime=''"></i></span>
                    <el-date-picker type="datetime" placeholder="选择日期" v-model="form.startTime" value-format="timestamp"></el-date-picker>
                    <span class="lianjie">~</span>
                  </div>
                  <span v-else>{{form.startTime | formatDate('yyyy-MM-dd HH:mm')}}</span>
                </el-col>
                <el-col class="line" :span="4" style="text-align:center;" v-if="form.startTime||projectBaseInfoRoleInfo.editorProInfo">截至时间</el-col>
                <el-col :span="10">
                  <div class="dateTimeStyle" v-if="projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'">
                    <i class="iconfont icon-riqi"></i>
                    <span v-if="form.endTime">
                      <span>{{form.endTime | formatDate('yyyy-MM-dd HH:mm')}}</span>
                      <i class="iconfont icon-shanchu" @click="form.endTime=''"></i>
                    </span>
                    <el-date-picker type="datetime" placeholder="选择日期" v-model="form.endTime" value-format="timestamp"></el-date-picker>
                  </div>
                  <span v-else>{{form.endTime | formatDate('yyyy-MM-dd HH:mm')}}</span>
                </el-col>
              </el-form-item>
              <el-form-item label="项目描述">
                <el-input type="textarea" v-model="form.describe" v-if="projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'" rows="4"></el-input>
                <span v-else v-text="form.describe"></span>
              </el-form-item>
              <el-form-item style="text-align:right;" v-if="projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'">
                <el-button type="primary" @click="saveData()">确定</el-button>
                <el-button @click="delEditor()">取消</el-button>
              </el-form-item>
            </el-form>
          </el-main>
          <!-- 状态设置 -->
          <el-main v-if="activeIndex==='2'">
            <div class="changeStatus">
              <div style="height:30px;line-height:30px;border:0;">
                <span style="font-size:16px;color: #333333;font-weight:bold;">当前项目状态 : </span>
                <span :class="'firststatusProNew'+project_status">{{['进行中','归档','暂停','删除'][project_status]}}</span>
              </div>
              <div v-if="project_status != '1' && project_status != '2'">
                <div><i class="iconfont icon-guidang"></i><span class="colorBold">归档项目</span></div>
                <div class="nextTextInfo">
                  <div>如果此项目已经结束了，你可以归档它，归档后项目将不允许再次编辑任务，只允许成员查看</div>
                  <div @click="statusSave(1)" class="statusProAll statusPro0" v-if="projectBaseInfoRoleInfo.editorProStatus">归档</div>
                  <div class="statusProAllNext" v-if="!projectBaseInfoRoleInfo.editorProStatus">归档</div>
                </div>
              </div>
              <div v-if="project_status != '1' && project_status != '2'">
                <div><i class="iconfont icon-zanting1"></i><span class="colorBold">暂停项目</span></div>
                <div class="nextTextInfo">
                  <div>如果此项目已经不再进行了，你可以暂停它，暂停后项目内任务将不允许再次操作，但还可以重新启动。</div>
                  <div @click="statusSave(2)" class="statusProAll statusPro1" v-if="projectBaseInfoRoleInfo.editorProStatus">暂停</div>
                  <div class="statusProAllNext" v-if="!projectBaseInfoRoleInfo.editorProStatus">暂停</div>
                </div>
              </div>
              <div v-if="project_status == '1' || project_status == '2'">
                <div><i class="iconfont icon-qiyongicon"></i><span class="colorBold">启用项目</span></div>
                <div class="nextTextInfo">
                  <div>如果此项目已经暂停或归档了，你可以再次启用它，启用后项目内任务可以再次操作</div>
                  <div @click="statusSave(0)" class="statusProAll statusPro3" v-if="projectBaseInfoRoleInfo.editorProStatus">启用</div>
                  <div class="statusProAllNext" v-if="!projectBaseInfoRoleInfo.editorProStatus">启用</div>
                </div>
              </div>
              <div>
                <div><i class="iconfont icon-shanchu3"></i><span class="colorBold">删除项目</span></div>
                <div class="nextTextInfo">
                  <div>如果此项目已经不再需要了，你可以删除它，删除后该项目下所有任务同时被删除。</div>
                  <div @click="statusSave(3)" class="statusProAll statusPro2" v-if="projectBaseInfoRoleInfo.editorProStatus">删除</div>
                  <div class="statusProAllNext" v-if="!projectBaseInfoRoleInfo.editorProStatus">删除</div>
                </div>
              </div>
            </div>
          </el-main>
          <!-- 项目进度 -->
          <el-main v-if="activeIndex==='3'" style="padding:20px">
            <el-form ref="form" :model="form" label-width="80px">
              <div class="progressNowTop">
                <span>项目当前进度</span>
                <span class="progressNowTopItem" v-text="form.progressStatus==='1'?form.progressContent:form.progressNumber">75</span>
                <span class="progressNowTopItem">%</span>
              </div>
              <el-form-item label="项目进度" class="Handwriting">
                <el-radio  v-if="projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'" v-model="form.progressStatus" label="0" >根据任务完成进度自动计算</el-radio>
                <el-radio  v-else v-model="form.progressStatus" disabled label="0" >根据任务完成进度自动计算</el-radio>
              </el-form-item>
              <el-form-item>
                <el-radio  v-if="projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'" v-model="form.progressStatus" label="1">手动填写</el-radio>
                <el-radio  else v-model="form.progressStatus" label="1" disabled>手动填写</el-radio>
                <span v-if="form.progressStatus=='1'&&projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'"><input class="pocenter" type="number" v-model="form.progressContent">%</span>
                <span v-if="form.progressStatus=='1'&&!projectBaseInfoRoleInfo.editorProInfo"><input class="pocenter" v-model="form.progressContent" :disabled="true"> %</span>
              </el-form-item>
            </el-form>
            <div class="bottomPro" v-if="projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'">
              <el-button type="primary" @click="submitProgress()">确认</el-button>
              <el-button @click="delEditor ()">取消</el-button>
            </div>
          </el-main>
          <!-- 权限设置 -->
          <el-main v-if="activeIndex==='4'" style="padding: 20px 0;">
            <div style="padding:10px 20px;margin-bottom:10px;">
              <p style="color:#3B3B3B;margin-bottom:10px;">可见范围：<span style="color:#379FFF;">不公开</span></p>
              <div v-if="projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'">
                <p><el-radio v-model="form.visualRange" label="0" @change="openOrNotpublic">不公开：只有加入的成员才能看见此项目</el-radio></p>
                <p><el-radio v-model="form.visualRange" label="1" @change="openOrNotpublic">公开：企业所有成员都可以看见此项目</el-radio></p>
              </div>
              <div v-else>
                <p><el-radio disabled v-model="form.visualRange" label="0" >不公开：只有加入的成员才能看见此项目</el-radio></p>
                <p><el-radio disabled v-model="form.visualRange" label="1" >公开：企业所有成员都可以看见此项目</el-radio></p>
              </div>
            </div>
            <div class="taskAndPro">
              <p>
                <span style="padding-left:20px;color:#3B3B3B;">操作权限</span>
                <span style="color: #999;font-size:12px;">(可对任务内的角色权限进行自定义)</span>
              </p>
              <div class="taskDiv" v-if="projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'">
                <div class="rowTaskCssStyls">
                  <div class="roleAndSetTable">
                    <span class="topFirstChild">操作</span>
                    <span class="bottomSecondChild">角色</span>
                    <i></i>
                  </div>
                  <div>编辑</div>
                  <div>完成</div>
                  <div>删除</div>
                  <div>移动</div>
                  <div>设置重复</div>
                  <div>添加协作人</div>
                  <div>移除协作人</div>
                  <div>新增子任务</div>
                  <div>编辑子任务</div>
                  <div>删除子任务</div>
                  <div>仅协作人可见</div>
                  <div>添加关联</div>
                  <div>取消关联</div>
                </div>
                <div class="rowTaskCssStyls">
                  <div>创建人</div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_1"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_2"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_3"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_4"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_5"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_6"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_7"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_8"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_9"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_10"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_11"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_12"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_13"></el-checkbox></div>
                </div>
                <div class="rowTaskCssStyls">
                  <div>执行人</div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_1"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_2"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_3"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_4"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_5"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_6"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_7"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_8"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_9"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_10"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_11"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_12"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_13"></el-checkbox></div>
                </div>
                <div class="rowTaskCssStyls">
                  <div>协作人</div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_1"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_2"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_3"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_4"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_5"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_6"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_7"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_8"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_9"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_10"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_11"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_12"></el-checkbox></div>
                  <div><el-checkbox @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_13"></el-checkbox></div>
                </div>
              </div>
              <div class="taskDiv" v-else>
                <div class="rowTaskCssStyls">
                  <div class="roleAndSetTable">
                    <span>操作</span>
                    <span>角色</span>
                    <i></i>
                  </div>
                  <div>编辑</div>
                  <div>完成</div>
                  <div>删除</div>
                  <div>移动</div>
                  <div>设置重复</div>
                  <div>添加协作人</div>
                  <div>移除协作人</div>
                  <div>新增子任务</div>
                  <div>编辑子任务</div>
                  <div>删除子任务</div>
                  <div>仅协作人可见</div>
                  <div>添加关联</div>
                  <div>取消关联</div>
                </div>
                <div class="rowTaskCssStyls">
                  <div>创建人</div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_1"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_2"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_3"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_4"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_5"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_6"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_7"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_8"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_9"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_10"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_11"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_12"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.creatUsers.auth_13"></el-checkbox></div>
                </div>
                <div class="rowTaskCssStyls">
                  <div>执行人</div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_1"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_2"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_3"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_4"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_5"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_6"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_7"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_8"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_9"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_10"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_11"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_12"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.implementUsers.auth_13"></el-checkbox></div>
                </div>
                <div class="rowTaskCssStyls">
                  <div>协作人</div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_1"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_2"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_3"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_4"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_5"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_6"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_7"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_8"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_9"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_10"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_11"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_12"></el-checkbox></div>
                  <div><el-checkbox disabled @change="taskAddPro" v-model="jurisdictionManager.cooperationUsers.auth_13"></el-checkbox></div>
                </div>
              </div>
            </div>
          </el-main>
          <!-- 任务提醒 -->
          <el-main v-if="activeIndex==='5'">
            <p style="font-size:16px;">提醒设置</p>
            <div class="projectBox">
              <p>在任务截止前，通过以下方式提醒任务执行人</p>
              <div v-if="projectBaseInfoRoleInfo.editorProRemind&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'">
                <el-select v-model="projectSetting.remindTitle" class="oneremindTitle" clearable>
                  <el-option
                    v-for="item in optionsTwo"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
                <el-select v-model="projectSetting.remindContent" placeholder="请选择" clearable >
                  <el-option
                    v-for="item in optionsThree"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
                <el-select v-model="projectSetting.remindUnit" placeholder="请选择" @change="changeTimeType" clearable >
                  <el-option
                    v-for="item in options"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
                <!-- <span @click="delSettingPro"><i class="el-icon-delete"></i></span> -->
              </div>
              <div class="notEditorProInfo" v-else>
                <el-select disabled v-model="projectSetting.remindTitle" class="oneremindTitle" placeholder="请选择">
                  <el-option
                    v-for="item in optionsTwo"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
                <el-select disabled v-model="projectSetting.remindContent" placeholder="请选择">
                  <el-option
                    v-for="item in optionsThree"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
                <el-select disabled v-model="projectSetting.remindUnit" placeholder="请选择" @change="changeTimeType">
                  <el-option
                    v-for="item in options"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
              </div>
            </div>
            <div>
              <el-checkbox-group v-model="projectSetting.remindType" v-if="projectBaseInfoRoleInfo.editorProRemind&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'">
                <el-checkbox label="企信"></el-checkbox>
                <el-checkbox label="企业微信"></el-checkbox>
                <el-checkbox label="钉钉"></el-checkbox>
                <el-checkbox label="邮件"></el-checkbox>
              </el-checkbox-group>
              <el-checkbox-group v-model="projectSetting.remindType" v-else>
                <el-checkbox label="企信" disabled></el-checkbox>
                <el-checkbox label="企业微信" disabled></el-checkbox>
                <el-checkbox label="钉钉" disabled></el-checkbox>
                <el-checkbox label="邮件" disabled></el-checkbox>
              </el-checkbox-group>
              <p style="padding:10px 0;">提示：成员必须绑定企业微信、钉钉或邮箱，才能收到提醒；</p>
              <div class="bottomPro" v-if="projectBaseInfoRoleInfo.editorProRemind&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'">
                <el-button type="primary" @click="submitSetting()">确认</el-button>
                <el-button @click="delEditor ()">取消</el-button>
              </div>
            </div>
          </el-main>
          <!-- 任务设置 -->
          <el-main v-if="activeIndex==='6'" class="taskSortAndSet">
            <!-- <div class="taskSort">
              <div>
                <span>任务排序</span>
                <span>拖动图标进行排序，排序后将字段显示在任务卡片上。</span>
              </div>
              <div class="showOrHiddenZiduan">
                <div>
                  <p>显示字段</p>
                  <div class="showZiduan">
                    <span style="border: 1px solid #CCCCCC;">名称</span>
                    <span style="border: 1px solid #CCCCCC;">截止时间</span>
                  </div>
                  <div class="showZiduan showziduanMove" v-if="projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'">
                    <draggable v-model="customSortList" :options="parentDraggable">
                      <span v-if="customSortList.length>0" class="canMove" v-for="(v,k) in customSortList" :key="k" >
                        <span v-text="v.label"></span>
                        <i class="iconfont icon-shanchu1" @click.stop="delTaskSort(v,k)" v-if="v.label"></i>
                      </span>
                      <span class="canMove noValue" v-if="customSortList.length===0">
                        <span></span>
                      </span>
                    </draggable>
                  </div>
                  <div class="showZiduan showziduanMove" v-else>
                    <div>
                      <span v-if="customSortList.length>0" class="canMove disablemove" v-for="(v,k) in customSortList" :key="k" >
                        <span v-text="v.label"></span>
                      </span>
                      <span class="canMove noValue disablemove" v-if="customSortList.length===0">
                        <span></span>
                      </span>
                    </div>
                  </div>
                </div>
                <div>
                  <p style="margin:15px 0 5px 0;">未显示字段</p>
                  <div class="hiddenZiduan" v-if="projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'">
                    <draggable v-model="fromBackstageList" :options="parentDraggable">
                      <span v-for="(v,k) in fromBackstageList" :key="k" v-if="fromBackstageList.length>0&&v.name!=='datetime_deadline'&&v.name!=='text_name'&&v.name!=='picklist_tag'&&v.name!=='personnel_execution'">
                        <span v-text="v.label"></span>
                      </span>
                      <span class="hiddenZiduanLayout" v-if="fromBackstageList.length===0">
                        <span></span>
                      </span>
                    </draggable>
                  </div>
                  <div class="hiddenZiduan" v-else>
                    <div>
                      <span v-for="(v,k) in fromBackstageList" :key="k" v-if="fromBackstageList.length>0&&v.name!=='datetime_deadline'&&v.name!=='text_name'&&v.name!=='picklist_tag'&&v.name!=='personnel_execution'" class="disablemove">
                        <span v-text="v.label"></span>
                      </span>
                      <span class="hiddenZiduanLayout disablemove" v-if="fromBackstageList.length===0">
                        <span></span>
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div> -->
            <div class="taskSet" v-if="projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'">
              <p>任务设置</p>
              <p><el-checkbox v-model="taskSet.isactiveContent">任务重新激活填写激活原因</el-checkbox></p>
              <p><el-checkbox v-model="taskSet.ertiorContent">更改截止时间填写修改原因</el-checkbox></p>
            </div>
            <div class="taskSet" v-else>
              <p>任务设置</p>
              <p><el-checkbox v-model="taskSet.isactiveContent" disabled>任务重新激活填写激活原因</el-checkbox></p>
              <p><el-checkbox v-model="taskSet.ertiorContent" disabled>更改截止时间填写修改原因</el-checkbox></p>
            </div>
            <!-- <div class="bottomPro" v-if="projectBaseInfoRoleInfo.editorProInfo"> -->
            <div class="bottomPro" v-if="projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'">
              <el-button type="primary" @click="saveSortAndSet">确认</el-button>
              <el-button @click="delEditor ()">取消</el-button>
            </div>
          </el-main>
          <!-- 外观 -->
          <el-main v-if="activeIndex==='7'" class="Appearance">
            <div class="AppearanceItem">
              <div class="systemPics">
                <img src="/static/img/project/1@2x.png" alt="">
                <span v-if="defalueImgList.one.active === 0&&projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'" @click="chooseImg('one',0)"></span>
                <i v-if="defalueImgList.one.active === 1" class="iconfont icon-Rectangle3"></i>
              </div>
              <div class="systemPics">
                <img src="/static/img/project/2@2x.png" alt="">
                <span v-if="defalueImgList.two.active === 0&&projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'" @click="chooseImg('two',0)"></span>
                <i v-if="defalueImgList.two.active === 1" class="iconfont icon-Rectangle3"></i>
              </div>
              <div class="systemPics">
                <img src="/static/img/project/3@2x.png" alt="">
                <span v-if="defalueImgList.three.active === 0&&projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'" @click="chooseImg('three',0)"></span>
                <i v-if="defalueImgList.three.active === 1" class="iconfont icon-Rectangle3"></i>
              </div>
              <div class="systemPics">
                <img src="/static/img/project/4@2x.png" alt="">
                <span v-if="defalueImgList.four.active === 0&&projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'" @click="chooseImg('four',0)"></span>
                <i v-if="defalueImgList.four.active === 1" class="iconfont icon-Rectangle3"></i>
              </div>
              <div class="systemPics">
                <img src="/static/img/project/5@2x.png" alt="">
                <span v-if="defalueImgList.five.active === 0&&projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'" @click="chooseImg('five',0)"></span>
                <i v-if="defalueImgList.five.active === 1" class="iconfont icon-Rectangle3"></i>
              </div>
              <div class="systemPics">
                <img src="/static/img/project/6@2x.png" alt="">
                <span v-if="defalueImgList.six.active === 0&&projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'" @click="chooseImg('six',0)"></span>
                <i v-if="defalueImgList.six.active === 1" class="iconfont icon-Rectangle3"></i>
              </div>
              <div class="systemPics" v-if="form.system_default_pic!='one'&&form.system_default_pic!='two'&&form.system_default_pic!='three'&&form.system_default_pic!='four'&&form.system_default_pic!='five'&&form.system_default_pic!='six'">
                <img :src="'/static/img/project/system-template-'+form.system_default_pic+'.png'" alt="">
                <span v-if="defalueImgList.next.active === 0&&projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'" @click="chooseImg(form.system_default_pic,0,1)"></span>
                <i v-if="defalueImgList.next.active === 1" class="iconfont icon-Rectangle3"></i>
              </div>
              <div class="myPic" v-if="myformImg.myImg">
                <img :src="myformImg.myImg + '&TOKEN=' + token" alt="">
                <span v-if="myformImg.active === 0&&projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'" @click="chooseImg('',1)"></span>
                <i v-if="myformImg.active === 1" class="iconfont icon-Rectangle3"></i>
              </div>
              <div class="addMyPic" v-if="projectBaseInfoRoleInfo.editorProInfo&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'">
                <div class="uploadImg">
                  <input type="file" id="upLoadFile" accept="image/*" @change="getFiles($event)" class="inputCss">
                  <i class="iconfont icon-add-group-chat"></i>
                </div>
                <p>上传图片</p>
              </div>
            </div>
          </el-main>
        </el-container>
      </div>
      <!-- 标签管理 -->
      <div v-if="status==='标签管理'">
        <el-container>
          <el-main style="height:540px;over-flow:auto;">
            <div>
              <div style="height:40px;margin-bottom:10px;">
                <el-col :span="18">
                  <el-input placeholder='搜索标签' v-model='searchTag' class='search-text' @keyup.enter.native="searchTagFun" clearable>
                    <i slot='prefix' class='el-input__icon el-icon-search'></i>
                  </el-input>
                </el-col>
                <el-col :span="6" v-if="judgeProjectRoleInfo(32)&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'">
                  <div class="addTagIcon" @click="searchAddTagFun('str', 1)"><i class="iconfont icon-iconset0186"></i>添加标签</div>
                </el-col>
              </div>
              <div class="tagParentAllCss" v-for="(v, k) in tagList" :key="k">
                <div class="" v-text="v.name">技术标签</div>
                <div class="tagListCss" v-for="(v1, k1) in v.childList" :key="k1">
                  <span class="tagfirstchild" v-if="judgeProjectRoleInfo(33)&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'"><i class="el-icon-delete" @click="delTag(v1)"></i></span>
                  <span class="taglastchild" v-text="v1.name" :style="{background:v1.colour?v1.colour:'#1890FF'}"></span>
                </div>
              </div>
            </div>
            <el-dialog title="新增标签" :visible.sync="tagVisible" :close-on-click-modal="false" append-to-body width="300px" class="wholeTags">
              <div>
                <el-input placeholder='搜索标签' v-model='searchAddTag' class='search-text' @keyup.enter.native="searchAddTagFun('str', 1)" clearable>
                  <i slot='prefix' class='el-input__icon el-icon-search'></i>
                </el-input>
                <div style="max-height: 400px;min-height:200px;overflow-y: auto;overflow-x: hidden;">
                  <div class="addTagPopup" v-for="(v, k) in tagAddList" :key="k" v-if="v.childList&&v.childList.length>0">
                    <div v-text="v.name">技术标签</div>
                    <div class="tagListAddCss" v-for="(v1, k1) in v.childList" :key="k1">
                      <span><el-checkbox v-model="v1.checked"></el-checkbox></span>
                      <i class="iconfont icon-paimingbiaoqian" :style="{color:v1.colour?v1.colour:'#1890FF'}"></i>
                      <span v-text="v1.name" class="add-tag-style-over"></span>
                    </div>
                  </div>
                </div>
                <!-- <div style="text-align: right;">
                  <el-button type="primary" style="background:#1890FF;" @click="saveAddTag">确 定</el-button>
                </div> -->
              </div>
              <div slot="footer" class="dialog-footer">
                <el-button type="primary" style="background:#1890FF;" @click="saveAddTag">确 定</el-button>
              </div>
            </el-dialog>
          </el-main>
        </el-container>
      </div>
    </div>
    <el-dialog width="800px" title="选择图片" :visible.sync="changeProjectPic" append-to-body class="changePicInbody">
        <div style="margin:10px 0;">
          <el-row :gutter="10">
            <el-col :span="6" v-for="v in 5" :key="v">
              <div class="imgboxDilog">
                <img src="../../../assets/custom/app_custom_app.png" alt="">
                <div class="settingDeldil" @click="chooseImg(v)">
                  <i class="iconfont icon-xuanzhongduigou"></i>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="changeProjectPic = false">确 定</el-button>
        </span>
    </el-dialog>
  </el-dialog>
</template>
<script>
import draggable from 'vuedraggable'
import {HTTPServer} from '@/common/js/ajax.js'
import { mapState, mapMutations } from 'vuex'
export default {
  name: 'SettingDilog',
  components: {draggable},
  data () {
    return {
      projectId: '',
      tagAddList: [],
      projectRoleInfoList: '', // 权限信息列表
      projectBaseInfoRoleInfo: { // 判断各权限具体值
        editorProInfo: '', // 修改项目信息权限
        editorProStatus: '', // 设置项目状态
        editorProRemind: '' // 设置提醒
      },
      defalueImgList: { // 系统默认图片
        one: {active: 0, value: 'one'},
        two: {active: 0, value: 'two'},
        three: {active: 0, value: 'three'},
        four: {active: 0, value: 'four'},
        five: {active: 0, value: 'five'},
        six: {active: 0, value: 'six'},
        next: {active: 0, value: 'next'}
      },
      layout: [], // 任务自定义布局
      changeProjectPic: false, // 更换图片弹窗
      oneSortList: [{label: '截止时间'}],
      customSortList: [], // 自己拖动排序列表
      fromBackstageList: [], // 获取的自定义列表
      projectBaseInfo: { // 项目基本信息
        id: null, // 记录ID
        project_id: null, // 项目ID
        name: '项目协作', // 项目名称
        leader: null, // 项目负责人
        project_progress_status: 0, // 0自动计算 2手动填写
        project_progress_content: 50, // 手动填写内容（正整数）
        start_time: '', // 项目开始时间（时间戳）
        end_time: '', // 项目截止时间（时间戳）
        visual_range: 0, // 项目可见范围（0不公开 1公开）
        describe: '', // 项目描述
        project_remind_title: '系统消息提示', // 提示主题（预留）
        project_remind_content: '1-60', // 项目提醒内容
        project_remind_unit: '分钟', // 项目提醒单位
        project_remind_type: '0', // 提醒方式（0企信 1企业微信 2钉钉 3邮件)
        project_status: 2 // 项目状态（0进行中（启用） 1归档 2暂停 3删除 )
      },
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      myformImg: {
        myImg: '',
        active: 0
      },
      project_status: null, // 项目状态
      taskactiveName: '1', // 权限管理
      tagVisible: false, // 添加标签
      options: [{
        value: '0',
        label: '分钟'
      }, {
        value: '1',
        label: '小时'
      }, {
        value: '2',
        label: '天'
      }],
      optionsThree: [], // 选择具体数字
      optionsTwo: [ // 系统消息提示
        {value: '0', label: '系统消息提示'}
      ],
      activeIndex: '1',
      data: '',
      dialogVisible: false,
      status: '',
      userDetails: {},
      form: { // 项目信息
        id: '',
        name: '',
        leader: '',
        visualRange: '0', // 可见范围
        startTime: '',
        endTime: '',
        progressStatus: '1',
        progressContent: null,
        describe: '',
        system_default_pic: ''
      },
      taskSet: {
        isactiveContent: false, // 任务激活原因是否开启
        ertiorContent: false // 任务截至事件是否开启填写原因
      },
      projectSetting: { // 项目设置
        id: null, // 项目ID
        remindTitle: '0', // 提示主题（预留） 0系统消息提示
        remindContent: null, // 项目提醒内容
        remindUnit: null, // 项目提醒单位 0分钟 1小时 2天
        remindType: []
      },
      searchTag: '',
      searchAddTag: '',
      tagList: [],
      jurisdictionManager: { // 权限管理列表
        creatUsers: {}, // 创建人
        implementUsers: {}, // 执行人
        cooperationUsers: {} // 协作人
      }
    }
  },
  watch: {
    'form.endTime' () {
      if (this.form.endTime && this.form.startTime) {
        if (this.form.endTime < this.form.startTime) {
          this.$message({
            message: '结束时间必须大于开始时间',
            type: 'warning'
          })
          this.form.endTime = ''
          return false
        }
      }
    },
    'form.startTime' () {
      if (this.form.startTime && this.form.endTime) {
        if (this.form.endTime < this.form.startTime) {
          this.$message({
            message: '结束时间必须大于开始时间',
            type: 'warning'
          })
          this.form.startTime = ''
          return false
        }
      }
    },
    'projectSetting.remindUnit' (oldval, newval) {
      if (!oldval) {
        this.projectSetting.remindContent = ''
      }
    }
  },
  mounted () {
    this.$bus.on('selectMemberRadio', (data) => {
      this.userDetails = data.prepareData[0]
      this.form.leader = data.prepareData[0].id
    })
    this.$bus.on('BaseSetting', (data) => {
      this.projectRoleInfoList = JSON.parse(sessionStorage.getItem('projectRoleInfo'))
      if (this.projectRoleInfoList) {
        this.projectBaseInfoRoleInfo.editorProInfo = this.judgeProjectRoleInfo(7)
        this.projectBaseInfoRoleInfo.editorProStatus = this.judgeProjectRoleInfo(9)
        this.projectBaseInfoRoleInfo.editorProRemind = this.judgeProjectRoleInfo(8)
      }
      console.log(this.projectBaseInfoRoleInfo)
      this.activeIndex = '1'
      let comeFromData = JSON.parse(data)
      this.projectId = parseInt(comeFromData.projectId)
      switch (comeFromData.status) {
        case 'base': // 基本设置
          this.status = '项目设置'
          this.dialogVisible = true
          this.getBaseInfoAndAuthInfo(this.projectId)
          break
        case 'tag': // 标签
          this.status = '标签管理'
          this.dialogVisible = true
          this.searchTagFun()
          this.getBaseInfoAndAuthInfo(this.projectId)
          break
      }
    })
  },
  methods: {
    getBaseInfoAndAuthInfo (id) { // 获取项目基本信息和权限信息
      let that = this
      HTTPServer.projectQueryById({id: id}, 'Loading').then((res) => { // 根据id获取项目基本信息
        that.projectBaseInfo = res
        let newobj = JSON.parse(JSON.stringify(res))
        this.userDetails = {picture: newobj.leader_pic, name: newobj.leader_name, id: newobj.leader} // 负责人
        that.form.id = newobj.id
        that.form.name = newobj.name
        that.form.leader = newobj.leader
        that.form.visualRange = newobj.visual_range_status
        that.form.startTime = newobj.start_time
        that.form.endTime = newobj.end_time
        that.form.progressStatus = newobj.project_progress_status
        that.form.progressContent = newobj.project_progress_content
        that.form.progressNumber = newobj.project_progress_number
        that.form.describe = newobj.note
        for (let j in that.defalueImgList) {
          that.defalueImgList[j].active = 0
        }
        that.form.system_default_pic = newobj.system_default_pic
        if (newobj.system_default_pic && newobj.system_default_pic !== 'one' && newobj.system_default_pic !== 'two' && newobj.system_default_pic !== 'three' && newobj.system_default_pic !== 'four' && newobj.system_default_pic !== 'five' && newobj.system_default_pic !== 'six') {
          that.defalueImgList['next'].active = 1
        } else if (newobj.system_default_pic) {
          that.defalueImgList[that.form.system_default_pic].active = 1
        }
        that.myformImg.myImg = newobj.pic_url
        if (!that.form.system_default_pic && that.myformImg.myImg) {
          that.myformImg.active = 1
        }
        that.projectSetting.id = newobj.id
        that.projectSetting.remindTitle = newobj.project_remind_title
        that.projectSetting.remindContent = newobj.project_remind_content ? newobj.project_remind_content : ''
        that.projectSetting.remindUnit = newobj.project_remind_unit
        if (newobj.project_complete_status === '1') {
          that.taskSet.isactiveContent = true
        } else { that.taskSet.isactiveContent = false }
        if (newobj.project_time_status === '1') {
          that.taskSet.ertiorContent = true
        } else { that.taskSet.ertiorContent = false }
        if (newobj.project_labels_content) {
          that.customSortList = JSON.parse(newobj.project_labels_content)
        } else {
          that.customSortList = []
        }
        let arr1 = []
        if (newobj.project_remind_type.indexOf('0') !== -1) { arr1.push('企信') }
        if (newobj.project_remind_type.indexOf('1') !== -1) { arr1.push('企业微信') }
        if (newobj.project_remind_type.indexOf('2') !== -1) { arr1.push('钉钉') }
        if (newobj.project_remind_type.indexOf('3') !== -1) { arr1.push('邮件') }
        that.projectSetting.remindType = arr1
        that.project_status = newobj.project_status
        this.optionsThree = this.editortiemTyope(that.projectSetting.remindUnit)
      })
      this.getTaskAuth(id)
    },
    handleOpen (k) { // 打开相应操作
      this.activeIndex = k
      // if (k === '6') {
      //   this.getProjectLayout(this.projectId)
      // }
    },
    getProjectLayout (bean) { // 获取任务自定义布局
      HTTPServer.getAllLayout({'bean': 'project_custom_' + this.projectId}, 'loading').then((res) => {
        if (JSON.stringify(res) !== '{}') {
          this.layout = res.enableLayout.rows
          let arr = []
          res.enableLayout.rows.forEach((v, k) => {
            v.isactive = 0
            this.customSortList.forEach((v1, k1) => {
              if (v.name === v1.name) {
                v.isactive = 1
              }
            })
            if (v.isactive === 0) {
              if (v.name !== 'datetime_deadline' && v.name !== 'text_name' && v.name !== 'picklist_tag' && v.name !== 'personnel_execution' && v.label !== '开始时间' && v.type !== 'attachment' && v.type !== 'subform' && v.type !== 'picture') {
                arr.push(v)
              }
            }
          })
          this.fromBackstageList = arr
        }
      })
    },
    openUserList () { // 打开选择项目负责人弹窗
      let userList = []
      if (JSON.stringify(this.userDetails) === '{}') {
        userList = []
      } else {
        userList = [this.userDetails]
      }
      this.$bus.emit('commonMember', {'type': 1, 'prepareData': userList, 'prepareKey': 1, 'seleteForm': true, 'banData': []})
    },
    searchTagFun () { // 搜索标签
      let that = this
      let sendTag = {
        id: this.projectId,
        type: 0,
        keyword: that.searchTag
      }
      HTTPServer.queryLabelsList(sendTag, 'Loading').then((res) => {
        that.tagList = res
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
    getTaskAuth (id) { // 获取任务权限
      let that = this
      HTTPServer.queryTaskAuthList({id: id}, 'Loading').then((res) => {
        res.forEach((v, k) => {
          for (let i = 1; i < 14; i++) {
            if (v['auth_' + i] === '1') {
              v['auth_' + i] = true
            } else {
              v['auth_' + i] = false
            }
          }
          if (v.role_type === '0') {
            that.jurisdictionManager.creatUsers = v
          } else if (v.role_type === '1') {
            that.jurisdictionManager.implementUsers = v
          } else if (v.role_type === '2') {
            that.jurisdictionManager.cooperationUsers = v
          }
        })
      })
    },
    delEditor () { // 取消编辑
      this.getBaseInfoAndAuthInfo(this.projectId)
    },
    editortiemTyope (type) { // 根据提醒类型不同更换不同数字 的 方法
      let arr = []
      let index = type === '0' ? 60 : type === '1' ? 24 : 7
      for (let i = 1; i < index + 1; i++) {
        arr.push({value: i + '', label: i + ''})
      }
      // arr.push({value: '-1', label: '请选择'})
      return arr
    },
    changeTimeType (type) { // 根据提醒类型不同更换不同数字
      this.optionsThree = this.editortiemTyope(type)
      this.projectSetting.remindContent = '1'
    },
    searchAddTagFun (str, status) { // 添加标签搜索
      let sendTag = {
        id: this.projectId,
        type: 1,
        keyword: this.searchTag
      }
      if (str) {
        this.tagVisible = true
        sendTag.type = 1
        sendTag.keyword = this.searchAddTag
      }
      sendTag.type = status === 0 ? 0 : 1
      HTTPServer.queryAllTagList(sendTag, 'Loading').then((res) => {
        let arr = []
        this.tagList.forEach((v, k) => {
          if (v.childList && v.childList.length > 0) {
            v.childList.map((v1, k1) => {
              arr.push(v1.id)
            })
          }
        })
        res.map((v, k) => {
          if (v.childList && v.childList.length > 0) {
            v.childList.map((v1, k1) => {
              arr.map((v2, k2) => {
                if (v1.id === v2) {
                  v1.checked = true
                }
              })
            })
          }
        })
        this.tagAddList = res
      })
    },
    delTag (val) { // 删除标签
      // let index = null
      let that = this
      let sendTag = []
      this.$confirm('该标签在当前项目已被任务所使用，移除后本项目内所有任务将移除该标签。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        that.tagList.forEach((v, k) => {
          if (v.childList && v.childList.length > 0) {
            v.childList.map((v1, k1) => {
              if (v1.id !== val.id) {
                sendTag.push(v1.id)
              }
            })
          }
        })
        // that.tagList.splice(index, 1)
        HTTPServer.editLabels({id: this.projectId, ids: sendTag.join(',')}, 'Loading').then((res) => {
          this.searchTagFun()
        })
      }).catch(() => {})
    },
    saveAddTag () { // 项目标签变更保存
      let sendTag = []
      this.tagAddList.forEach((v, k) => {
        if (v.childList && v.childList.length > 0) {
          v.childList.map((v1, k1) => {
            if (v1.checked) {
              sendTag.push(v1.id)
            }
          })
        }
      })
      HTTPServer.editLabels({id: this.projectId, ids: sendTag.join(',')}, 'Loading').then((res) => {
        // this.searchAddTagFun('', 0)
        this.tagVisible = false
        this.searchTagFun()
        // this.tagList = res
      })
    },
    saveData () { // 项目信息保存
      if (!this.form.name) {
        this.$message({
          message: '项目名称必填',
          type: 'warning'
        })
        return false
      }
      if (!this.form.leader) {
        this.$message({
          message: '请选择负责人',
          type: 'warning'
        })
        return false
      }
      if (!this.form.endTime) {
        this.$message({
          message: '请选择结束时间',
          type: 'warning'
        })
        return false
      }
      if (this.projectBaseInfo.leader !== this.form.leader) {
        this.$confirm('确定要转移项目负责人吗？转移后您将被移出该项目。', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.saveDataBase('true')
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
      } else {
        this.saveDataBase()
      }
    },
    openOrNotpublic (str) { // 权限，公开不公开
      this.taskAddPro()
    },
    saveDataBase (v) {
      let senddata = {
        id: this.form.id, // 项目ID
        name: this.form.name, // 项目名称
        picUrl: this.myformImg.myImg, // 项目图片
        leader: this.form.leader, // 项目负责人
        startTime: this.form.startTime, // 项目开始时间（时间戳
        endTime: this.form.endTime, // 项目截止时间（时间戳
        note: this.form.describe, // 项目描述
        systemDefaultPic: this.form.system_default_pic // 系统默认图片
      }
      HTTPServer.saveInformation(senddata, 'Loading').then((res) => { // 基本设置的保存项目信息
        this.$message({
          showClose: true,
          message: '设置成功',
          type: 'success'
        })
        if (v && v === 'true') {
          this.$router.push({path: '/frontend/project/projectList'})
        } else {
          this.getBaseInfoAndAuthInfo(this.projectId)
        }
      })
    },
    submitProgress () { // 保存项目进度
      if (this.form.progressStatus === '1') {
        let reg = /^[0-9]*[1-9][0-9]*$/
        if (!reg.test(this.form.progressContent) || !this.form.progressContent) {
          this.form.progressContent = null
          this.$message({
            message: '项目进度为必填的正整数',
            type: 'warning'
          })
          return false
        }
      }
      let senddata = {
        id: this.form.id, // 项目ID
        project_progress_status: parseInt(this.form.progressStatus), // 0自动计算 1手动填写
        project_progress_content: parseInt(this.form.progressContent) // 手动填写内容（正整数)
      }
      HTTPServer.editProgress(senddata, 'Loading').then((res) => { // 基本设置的保存项目信息
        this.$message({
          showClose: true,
          message: '设置成功',
          type: 'success'
        })
        this.getBaseInfoAndAuthInfo(this.projectId)
      })
    },
    saveSortAndSet () { // 保存任务设置
      let senddata = {
        id: this.projectId,
        project_complete_status: this.taskSet.isactiveContent ? 1 : 0,
        project_time_status: this.taskSet.ertiorContent ? 1 : 0,
        project_labels_content: JSON.stringify(this.customSortList)
      }
      HTTPServer.editTaskSort(senddata, 'Loading').then((res) => { // 基本设置的保存项目信息
        this.$message({
          showClose: true,
          message: '设置成功',
          type: 'success'
        })
        this.getBaseInfoAndAuthInfo(this.projectId)
      })
    },
    delSettingPro () { // 清除设置提醒信息
      this.projectSetting.remindContent = '-1'
      this.projectSetting.remindUnit = '0'
    },
    editTaskAuth (data) { // 修改权限
      HTTPServer.editTaskAuth(data, 'Loading').then((res) => {
        console.log(res)
      })
    },
    submitSetting () { // 项目设置保存
      let senddata = JSON.parse(JSON.stringify(this.projectSetting))
      senddata.remindType = [] // 0企信 1企业微信 2钉钉 3邮件
      this.projectSetting.remindType.forEach((v, k) => {
        switch (v) {
          case '企信':
            senddata.remindType.push(0)
            break
          case '企业微信':
            senddata.remindType.push(1)
            break
          case '钉钉':
            senddata.remindType.push(2)
            break
          case '邮件':
            senddata.remindType.push(3)
            break
        }
      })
      senddata.remindType = senddata.remindType.join(',')
      if (senddata.remindContent === '-1') {
        this.$message({
          message: '请选择具体数字',
          type: 'warning'
        })
        return false
      }
      senddata.remindContent = parseInt(senddata.remindContent)
      senddata.remindTitle = parseInt(senddata.remindTitle)
      senddata.remindUnit = parseInt(senddata.remindUnit)
      HTTPServer.saveSetting(senddata, 'Loading').then((res) => {
        this.$message({
          showClose: true,
          message: '设置成功',
          type: 'success'
        })
        this.getBaseInfoAndAuthInfo(this.projectId)
      })
    },
    statusSave (status) { // 状态保存
      let that = this
      let senddata = {
        id: this.projectId,
        status: status
      }
      // let str = ''
      // switch (status) {
      //   case 1:
      //     str = '确定要归档该项目吗？归档后您将回到协作首页。'
      //     break
      //   case 2:
      //     str = '确定要暂停该项目吗？暂停后您将回到协作首页。'
      //     break
      //   case 3:
      //     str = '确定要删除该项目吗？删除后您将回到协作首页。'
      //     break
      //   case 4:
      //     str = '确定要启动该项目吗？启动后您将回到协作首页。'
      //     break
      // }
      if (status === 3) {
        this.$confirm('确定要删除该项目吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          HTTPServer.editStatus(senddata, 'Loading').then((res) => {
            that.$message({
              showClose: true,
              message: '设置成功',
              type: 'success'
            })
            this.$router.push({path: '/frontend/project/projectList'})
          })
        }).catch(() => {})
      } else {
        HTTPServer.editStatus(senddata, 'Loading').then((res) => {
          that.$message({
            showClose: true,
            message: '设置成功',
            type: 'success'
          })
          this.$bus.$emit('projectStatusChange')
          this.dialogVisible = false
          this.projectStatusChange(status + '')
          // location.reload()
          // this.getBaseInfoAndAuthInfo(this.projectId)
        })
      }
    },
    handleClick () {
      console.log(this.jurisdictionManager)
    },
    taskAddPro () { // 权限管理
      let obj = JSON.parse(JSON.stringify(this.jurisdictionManager))
      let senddata = [
        obj.creatUsers,
        obj.implementUsers,
        obj.cooperationUsers
      ]
      senddata.forEach((v, k) => {
        for (let i = 1; i < 14; i++) {
          v['auth_' + i] = v['auth_' + i] ? 1 : 0
        }
      })
      this.editTaskAuth({id: parseInt(this.projectId), authArr: senddata, visual_range_status: parseInt(this.form.visualRange)})
    },
    getFiles (event) { // 上传图片
      let formdata = new FormData()
      formdata.append('file', event.target.files[0])
      // 上传文件,获取路径
      HTTPServer.emailImageUpload(formdata).then((res) => {
        this.myformImg.myImg = res[0].file_url
        for (let i in this.defalueImgList) {
          this.defalueImgList[i].active = 0
        }
        this.form.system_default_pic = ''
        this.myformImg.active = 1
        this.saveData()
      })
    },
    addTaskSort (v, k) { // 增减自定义字段
      v.isactive = 1
      this.customSortList.push(v)
    },
    reduceTaskSort (v, k) {
      v.isactive = 0
      let index = null
      this.customSortList.forEach((val, key) => {
        if (v.name === val.name) {
          index = key
        }
      })
      this.customSortList.splice(index, 1)
    },
    delTaskSort (v, k) { // 减少自定义字段
      // this.fromBackstageList.forEach((val, key) => {
      //   if (v.name === val.name) {
      //     val.isactive = 0
      //   }
      // })
      this.fromBackstageList.push(v)
      this.customSortList.splice(k, 1)
    },
    chooseImg (v, status, nextStaus) {
      for (let i in this.defalueImgList) {
        this.defalueImgList[i].active = 0
      }
      if (status === 0) {
        if (nextStaus) {
          this.defalueImgList['next'].active = 1
        } else {
          this.defalueImgList[v].active = 1
        }
        this.myformImg.active = 0
        this.myformImg.myImg = ''
        this.form.system_default_pic = v
        this.form.pic_url = ''
      } else {
        this.myformImg.active = 1
        this.form.system_default_pic = ''
      }
      this.saveData()
    },
    bgColorRandom () {
      return '#' + Math.floor(Math.random() * 0xffffff).toString(16)
    },
    changeName (name) {
      return name.slice(-2)
    },
    // 状态管理
    ...mapMutations({
      projectStatusChange: 'PROJECT_STATUS'
    })
  },
  computed: {
    parentDraggable () {
      return {
        animation: 200,
        group: { name: 'compontents', pull: true, put: true },
        sort: true,
        ghostClass: 'ghost'
      }
    },
    ...mapState({
      projectStatus: state => state.projectData.project_status
    })
  },
  beforeDestroy () {
    this.$bus.off('BaseSetting')
  }
}
</script>
<style lang='scss' scoped>
#settingBody{
  .titleStatus{font-size:18px;color: #323232;}
  .titleStatus.active{text-align:left}
}
  #settingCss{
    height:540px;
    .pocenter{
      width: 90px;
      height: 25px;
      margin-left: 10px;
      border: 1px solid #ddd;
      padding-left: 15px;
    }
    .uploadImg{
      position: relative; display: inline-block;width: 133px;height:32px;line-height: 32px;text-align: center;border-radius: 5px;
      &:hover{cursor: pointer;}
      .inputCss{
        position: absolute;top: 0; left: 0;opacity: 0; width: 50px;height: 50px;&:hover{cursor: pointer;}
      }
    }
    .delImgDil{
      >i{position: absolute;top: -12px;right: -7px;color:#DCDCDC;&:hover{cursor: pointer;}}
    }
    .el-icon-circle-plus-outline{
      cursor: pointer;
    }
    .projectBox{
      padding:20px 0;
      .inputNumber{
        width: 100px;
        height:30px;
        border: 1px solid #ddd;
        padding-left: 15px;
      }
      >div{
        margin-top: 10px;
        height: 30px;
        >span:last-child{
          &:hover{
            cursor: pointer;
          }
          float: right;
          height: 30px;
          line-height: 30px;
          margin-right: 60px;
        }
      }
    }
    .bottomPro{
      text-align: left;
      margin-top: 20px;
    }
    .changeStatus{
      .icon-guidang{color:#82C5FA;font-size:32px;}
      .icon-zanting1{color:#FFBF00;font-size:32px;}
      .icon-qiyongicon{color:#92D66D;font-size:32px;}
      .icon-shanchu3{color:#F56C6C;font-size:32px;}
      height: 90%;
      >div{
        margin: 20px 30px 20px 0;
        border-bottom: 1px solid #ddd;
        height: 100px;
        >div:first-child{
          color: #666666;height:32px;line-height: 32px;
          >i{float:left;}
          >span{float:left;margin-left:10px;}
        }
      }
      .nextTextInfo{
        height:50px;
        margin-top:15px;
        >div{float:left;}
        >div:first-child{width:70%;color:#999;}
      }
      .statusProAll,.statusProAllNext{
        height:36px;line-height: 36px;width:70px;text-align: center;border:1px solid #ddd;&:hover{cursor: pointer;border:0;color:#fff;}
        border-radius: 5px;margin: 0 0 0 50px;
      }
      .statusProAllNext{background: #F5F5F5;&:hover{cursor: not-allowed;border:1px solid #ddd;color:#606266;}}
      .statusPro0:hover{background: #1A8FFF;}
      .statusPro1:hover{background: #FF6600;}
      .statusPro2:hover{background: #EB5F59;}
      .statusPro3:hover{background: #0EAD1E;}
    }
    .tagListCss{
      margin: 5px 10px;
      padding: 2px 20px;
      padding-left:5px;
      height: 40px;
      border-bottom: 1px solid #ddd;
      >span.tagfirstchild{
        &:hover{cursor: pointer;}
        float: right;
        margin-right: 20px;
      }
      >span.taglastchild{
        display: inline-block; padding: 0 10px;color: #fff;border-radius: 4px;
        max-width: 330px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    .taskAndPro{
      .taskDiv{
        margin:10px 20px;overflow:auto;width:600px;height:200px;background:#F1F1F1;
        .rowDiv:first-child{border-bottom: 1px solid #ddd;}
        .rowDiv{
          padding: 5px 10px;
          .rightRowCenter{
            .el-col{text-align: center;}
          }
        }
        .rowTaskCssStyls{
          width: 205%;border:1px solid #ddd;border-bottom:0;background:#fff;
          >div{text-align: center;height:45px;line-height:45px;width:100px;float:left;border-left:1px solid #ddd;}
          >div:nth-child(1){width:80px;border-left:0}
          >div:nth-child(2),>div:nth-child(3),>div:nth-child(4),>div:nth-child(5){width:60px;}
          >div.roleAndSetTable{
            position: relative;
            >span{position: absolute;font-size:12px;color:#999;}
            >span.topFirstChild{top:-10px;right:10px;}
            >span.bottomSecondChild{left:10px;bottom:-10px;}
            >i{position: absolute;width: 100px;border-top:1px solid #DFDFDF; transform: rotate(29deg);left: -15px;top: 19px;}
          }
        }
        .rowTaskCssStyls:nth-child(4){border-bottom:1px solid #ddd;}
        .rowTaskCssStyls:after{content:'';display: table;clear: both;}
      }
    }
    .firststatusPro{
      display: inline-block;
      padding: 0 15px;
      border-radius: 2px;
      color: #fff;
      height: 28px;
      line-height: 28px;
      border-radius: 5px;
    }
    .firststatusPro0{background: #92D66D;}
    .firststatusPro1{background: #82C5FA;}
    .firststatusPro2{background: #FFBF00;}
    .firststatusProNew0{color:#92D66D;font-size:16px;}
    .firststatusProNew1{color:#82C5FA;font-size:16px;}
    .firststatusProNew2{color:#FFBF00;font-size:16px;}
    .colorBold{color: #212121;font-weight: bold;}
    .imageOrText{
      height:40px;line-height:40px;
      >span.firstImage{
        display:inline-block;position: relative;height:40px;width:40px;border-radius:50%;&:hover{>i{display: block;}}
        >img{width:100%;height:100%;vertical-align: sub;border-radius:50%;}
        >i{
          position: absolute;top: -13px;right: -3px; font-size: 12px;height:12px;width:12px;color: #DBDBDB;border:1px solid #fff;&:hover{cursor: pointer;}display: none;
        }
        >span{display: inline-block;width:40px;height:40px;line-height: 40px;text-align: center;border-radius: 50%;color:#fff;}
      }
      >i{font-size: 27px;}
      >span.secondImage{
        display:inline-block;width:40px;height:40px;text-align: center;line-height: 40px;&:hover{cursor: pointer;}border: 1px solid #A8A8B4;
        border-radius: 50%;
        i{font-size:20px;}
      }
    }
    .addTagIcon{font-size:14px;height:40px;line-height: 40px;padding-left:20px;i{margin-right:10px;}color:#868686;&:hover{cursor: pointer;color:#1890FF;}}
    .tagParentAllCss{
      max-height:400px;overflow: auto;
    }
    #upLoadFile{&:hover{cursor: pointer;}}
    .showProImg{
      width:170px;height:100px;img{width:100%;height:100%;}margin-right:20px;border-radius: 5px;overflow: hidden;
    }
    .taskSortAndSet{
      .taskSort{
        >div:first-child{>span:first-child{}>span:last-child{font-size:12px;}}
        .showOrHiddenZiduan{
          margin-top:10px;
          .showZiduan{
            .canMove{
              height:30px;text-align:center;width:110px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;
              &:hover{cursor:move;border-color:#F7F7F7;box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.18);}
            }
            .canMove.noValue{
              height:30px;box-shadow:0 0 10px #fff;background: #F3F8FF;border:0;cursor: default;
            }
            .canMove.disablemove{ &:hover{cursor:default;box-shadow: 0 0 0 #fff;}border:1px solid rgb(204, 204, 204);box-shadow: 0 0 0 #fff;}
          }
          .showZiduan,.hiddenZiduan{min-height:40px;
            >div>span,>span,.canMove{
              float:left;padding:2px 20px;border-radius:2px;margin:5px 0;margin-right:10px;position: relative;border:1px solid transparent;
              i{position: absolute;top:0;right:0;font-size:12px;color:#F83D3D;cursor:pointer;}
            }
          }
          .showZiduan:after,.hiddenZiduan:after{content:'';display: table;clear:both;}
          .hiddenZiduan{
            >div>span{
              height:30px;
              text-align:center;width:110px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;box-shadow: 0 1px 4px 0 #C8C8C8;&:hover{cursor:move;border-color:#F7F7F7;box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.18);}
            }
            >div>span.disablemove{&:hover{cursor:default;box-shadow: 0 0 0 #fff;}border:1px solid rgb(204, 204, 204);box-shadow: 0 0 0 #fff;}
            >div>span.hiddenZiduanLayout{
              height:30px;box-shadow:0 0 10px #fff;background: #F3F8FF;border:0;cursor: default;
            }
          }
          .showziduanMove{
            >div{
              >span{box-shadow: 0 1px 4px 0 #C8C8C8;border-radius: 2px;}
            }
            border-bottom:1px dashed #ddd;padding:5px 0;padding-bottom:10px;
          }
        }
      }
      .taskSet{
        margin-top:10px;
        >p{margin-bottom:10px;}
        >p:first-child{margin:5px 0 20px 0;font-size:16px;}
      }
    }
    .Appearance{
      padding-right:10px;
      .AppearanceItem{
        display: flex;flex-direction: row; flex-wrap: wrap;
        >div{width:190px;height:120px;border-radius: 4px;margin:0 15px 15px 0;}
        .systemPics,.myPic{
          position: relative;
          >img{width:100%;height:100%;border-radius: 4px;}
          >span{
            display:none;position: absolute;top:10px;right:10px;height:16px;width:16px;border: 1px solid #fff;border-radius: 50%;cursor: pointer;
          }
          &:hover{>span{display: block;}}
          >i{
            position: absolute;top:6px;right:10px;font-size: 18px;color:#fff;cursor: pointer;
          }
        }
        .addMyPic{
          background: #EBEDF0;text-align:center;box-shadow: 0 2px 3px 0 rgba(200,200,200,0.50);
          >div{
            height:50px; width:50px;margin:30px auto 0 auto;
            >i{font-size:50px;color:#1890FF;cursor: pointer;}
          }
          >p{color:#69696C;}
        }
      }
    }
    .dateTimeStyle{
      position:relative;
    }
    .progressNowTop{span{font-size: 16px;color: #424242}>span.progressNowTopItem{color: #0888FF;}}
  }
</style>
<style lang='scss'>
#settingCss {
  .el-menu-vertical-demo:not(.el-menu--collapse){
    width:149px;
  }
  .el-menu{
    border: 0;
  }
  .el-menu-item{
    text-align:center;
    border-bottom: 1px solid #ddd;
  }
  .projectBox {
    .el-input__inner{
      width:100px;
      height:30px;
    }
  }
  .el-button {
    padding:10px 20px;
  }
  .oneremindTitle{
    .el-input__inner{width:150px;}
  }
  .projectImg{
    >div>div{float:left;}
    p.changeProjectImg{
      width:133px;text-align:center;height:32px;line-height:30px;border: 1px solid #ddd;border-radius: 5px;margin:12px 0;&:hover{cursor:pointer;}
    }
  }
  .dateTimeStyle{
    &:hover{
      color:#5EAFFB;
      cursor: pointer;
      .el-input__inner{cursor: pointer;}
      >span.lianjie{color:#606266;}
      >span{i.icon-shanchu{display:block;}}
    }
    >span{position:relative;i.icon-shanchu{position:absolute;top:-10px;right:-15px;color:#DBDBDB;font-size:12px;margin-left:5px;display:none;&:hover{cursor: pointer;}}}
    .icon-riqi{font-size:20px;}
    .el-date-editor.el-date-editor--datetime{
      position: absolute;left:0;top:0;opacity: 0;width:20px;
      .el-input__inner{padding:0;}
      .el-input__prefix,.el-input__suffix{display:none;}
    }
    >span.lianjie{position:absolute;right:0;top:0;}
  }
  .notEditorProInfo{
    .el-input__suffix-inner{display:none;}
  }
  .Handwriting{
    margin-bottom:0;
    .el-form-item__label{text-align:left;}
  }
  .el-input__inner{background:#FBFBFB;}
}
#settingBody{
  .el-dialog__header{padding:20px;background:#fff;text-align:center;span,.el-dialog__close.el-icon.el-icon-close{color:#4A4A4A;}}
  .el-dialog{width: 800px;}
  .el-dialog__body{
    padding:0;
    border-top: 1px solid #ddd;
  }
  .el-form-item__content{text-align:left;}
  .el-menu-item{text-align:left;padding-left:20px;border-right:3px solid transparent;i{margin-right:10px;font-size:20px;}}
  .el-menu-item.is-active{
    border-right:3px solid #42A7FF;
  }
}
.addTagPopup{
  >div:first-child{min-height:30px;line-height: 30px;color:#A4A4B2;margin-top:10px;}
}
.tagListAddCss{
  margin: 5px 10px;
  padding-left:5px;
  height: 30px;line-height:30px;&:hover{background:#F2F2F2;cursor:pointer;}
  >span:first-child{
    &:hover{cursor: pointer;}
    float: right;
    margin-right: 20px;
  }
  >span:last-child{}
  .icon-paimingbiaoqian{
    float: left;
  }
  .add-tag-style-over{
    display:inline-block;
    max-width:170px;
    overflow:hidden;
    text-overflow:ellipsis;
    white-space:nowrap
  }
}
.changePicInbody{
  .el-dialog__header{border-bottom:1px solid #ddd;}
  .el-dialog__footer{
    border-top:1px solid #ddd;text-align:right;
  }
  .imgboxDilog{
    height:100px;background:#ddd;margin-bottom:10px;border-radius:5px;position: relative;
    >img{width:100%;height:100%;border-radius:5px;opacity: .7;}
  }
  .settingDeldil{
    position: absolute;top: 5px;right: 10px;
    i{font-size:22px;&:hover{cursor: pointer;color: #fff;}}
    i.iconTop{
      opacity: 1;
    }
  }
}
.wholeTags{
  .el-dialog__header{border-bottom:1px solid #ddd;}
  .el-input__inner{background:#FBFBFB;}
  .el-dialog__footer{border-top:1px solid #ddd;padding:10px 20px;}
  .dialog-footer{
    .el-button.el-button--primary{padding:8px 13px;width:65px;}
  }
}
</style>
