<template>
  <div class="cpChat-main main">
    <el-container>
      <el-aside width="220px">
        <div class="nav-header">企信
          <el-dropdown trigger="click" placement="bottom" @command="chatPopoverSet">
            <span class="el-dropdown-link">
              <i class="iconfont icon-pc-paper-more"></i>
            </span>
            <el-dropdown-menu slot="dropdown" class="popoverSet">
              <el-dropdown-item command="0"><i class="iconfont icon-add-group-chat"></i>创建群聊</el-dropdown-item>
              <el-dropdown-item command="2"><i class="iconfont icon-shouye-qixin"></i>发起私聊</el-dropdown-item>
              <el-dropdown-item command="1"><i class="iconfont icon-pc-background-role"></i>选择群聊</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>

        <div class="item" v-for="(chatItem, index) in sortChatList" :key="index" v-if="chatItem.is_hide == 0"  @click="switchChat($event, chatItem)"
        :class="(chatItem.id == chat_object.id && chatItem.chat_type == chat_object.chat_type) ? 'active':''">
          <img src="/static/img/im/company.png" v-if="chatItem.chat_type == 1 && chatItem.type == 0" />
          <img src="/static/img/im/group.png" v-if="chatItem.chat_type == 1 && chatItem.type == 1" />
          <img v-if="chatItem.chat_type == 2 && chatItem.picture" :src="chatItem.picture+'&TOKEN='+token" />
          <div class="simpName" v-if="chatItem.chat_type == 2 && !chatItem.picture">{{chatItem.employee_name | limitTo(-2)}}</div>
          <img :src="'/static/img/im/' + chatItem.type + '.png'" v-if="chatItem.chat_type == 3 && chatItem.type != 1" />
          <div class="simpName" v-if="chatItem.chat_type == 3 && chatItem.type == 1" :title="chatItem.icon_url">助手</div>

          <span v-if="chatItem.chat_type == 1" :class="chatItem.draftText ? 'draft':''">{{chatItem.name}}
            <div class="draftText" v-if="chatItem.draftText" v-html="traverseDraftText(chatItem)"></div>
          </span>
          <span v-if="chatItem.chat_type == 2" :class="chatItem.draftText ? 'draft':''">{{chatItem.employee_name}}
            <div class="draftText" v-if="chatItem.draftText" v-html="traverseDraftText(chatItem)"></div>
          </span>
          <span v-if="chatItem.chat_type == 3">{{chatItem.name}}</span>
          <i class="el-icon-close" @click="removeSession(chatItem)" v-if="chatItem.chat_type != 3 && !(chatItem.chat_type == 1 && chatItem.type == 0)"></i>
          <span class="num" v-if="chatItem.unread_nums > 0">{{(chatItem.unread_nums > 99) ? '99+' : chatItem.unread_nums}}</span>
          <div class="top_status" v-if="chatItem.top_status == 1"></div>
        </div>
      </el-aside>
      <el-container>
        <el-header height="60px">
          <!-- <img v-if="chat_object.chat_type == 2 && chat_object.picture" :src="chat_object.picture+'&TOKEN='+token" />
          <i class="iconfont icon-chat-company-group" v-if="chat_object.chat_type == 1 && chat_object.type == 0"></i>
          <i class="iconfont icon-chat-group" v-if="chat_object.chat_type == 1 && chat_object.type == 1"></i>
          <div class="simpName" v-if="chat_object.chat_type == 2 && !chat_object.picture">{{chat_object.employee_name | limitTo(-2)}}</div>
          <div class="simpName" v-if="chat_object.chat_type == 3">
            <i v-if="chat_object.type == 2" class="iconfont icon-nav-chat"></i>
            <i v-else-if="chat_object.type == 3" class="iconfont icon-shenpi"></i>
            <i v-else-if="chat_object.type == 4" class="iconfont icon-shouye-wenjianku"></i>
            <i v-else-if="chat_object.type == 5" class="iconfont icon-nav-memo"></i>
            <i v-else-if="chat_object.type == 6" class="iconfont icon-youjian1"></i>
            <span v-else>助手</span>
          </div> -->

          <img src="/static/img/im/company.png" v-if="chat_object.chat_type == 1 && chat_object.type == 0" />
          <img src="/static/img/im/group.png" v-if="chat_object.chat_type == 1 && chat_object.type == 1" />
          <img v-if="chat_object.chat_type == 2 && chat_object.picture" :src="chat_object.picture+'&TOKEN='+token" />
          <div class="simpName" v-if="chat_object.chat_type == 2 && !chat_object.picture">{{chat_object.employee_name | limitTo(-2)}}</div>
          <img :src="'/static/img/im/' + chat_object.type + '.png'" v-if="chat_object.chat_type == 3 && chat_object.type != 1" />
          <div class="simpName" v-if="chat_object.chat_type == 3 && chat_object.type == 1">助手</div>

          <span v-if="chat_object.chat_type == 1">{{chat_object.name}}</span>
          <span v-if="chat_object.chat_type == 2">{{chat_object.employee_name}}</span>
          <span v-if="chat_object.chat_type == 3">{{chat_object.name}}</span>
          <el-dropdown trigger="click" placement="bottom" @command="setCommand" v-if="chat_object.chat_type == 1">
            <span class="el-dropdown-link">
              <i class="el-icon-arrow-down"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="0" v-if="chat_object.principal == OneselfIMID && chat_object.type != 0">管理群聊成员</el-dropdown-item>
              <el-dropdown-item command="1" v-if="chat_object.principal == OneselfIMID">编辑群聊</el-dropdown-item>
              <el-dropdown-item command="2" v-if="chat_object.principal == OneselfIMID && chat_object.type != 0">解散群聊</el-dropdown-item>
              <el-dropdown-item command="3" v-if="chat_object.principal != OneselfIMID && chat_object.type != 0">退出群聊</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <i v-if="chat_object.chat_type != 3" class="iconfont icon-pc-background-personnel info" @click="openParticularsForm"></i>
          <i v-if="chat_object.chat_type == 3" class="iconfont icon-pc-paper-admini2 info" @click="openParticularsForm"></i>
          <i class="iconfont icon-pc-filter screening" v-if="chat_object.chat_type == 3 && chat_object.application_id" @click="screening"></i>
          <a class="allRead" href="javascript:;" v-if="chat_object.chat_type == 3 && chat_object.type != 2" @click="markAllRead">全部标记为已读</a>
        </el-header>
        <el-main id="content">
          <a href="javascript:;" v-if="more > 0 && msgType" @click="loadMore" style="margin-left: calc(50% - 27px);">加载更多...</a>
          <div class="item" v-if="msgType" v-for="(msg_item, index) in chat_msg" :key="index" v-bind:class="(msg_item.sender == OneselfIMID) ? 'right' : ''">
            <div class="time-title" v-bind:class="'time' + msg_item.timeType" v-if="msg_item.timeShow">
              <div class="left"></div>{{msg_item.timeTitle}}<div class="right"></div>
            </div>
            <avatar-component v-if="msg_item.msg.type != 7" :employeeData="{'name': msg_item.msg.senderName, 'picture': (msg_item.sender == employeeInfo.sign_id) ? employeeInfo.picture : msg_item.msg.senderAvatar, 'type': 1, 'sign_id': msg_item.sender}"></avatar-component>
            <div class="figureInfor" v-if="msg_item.msg.type != 7">
              <span class="name">{{msg_item.msg.senderName}}</span>
              <span class="time">{{msg_item.times | formatDate('HH:mm')}}</span>
            </div>
            <!-- 文本 -->
            <div class="content" v-if="msg_item.msg.type == 1">
              <span v-if="localTime-msg_item.clientTimes < 120000 && msg_item.sender == OneselfIMID && localTime-msg_item.clientTimes >= 0" class="revocation" @click="revocationChat(msg_item)">撤回</span>
              <span v-if="msg_item.warning != 1">
                <span v-if="msg_item.isRead.length == 0 && msg_item.sender == OneselfIMID && chat_object.chat_type == 2" class="isRead" style="color: #409eff;">未读</span>
                <span v-if="msg_item.isRead.length > 0 && msg_item.sender == OneselfIMID && chat_object.chat_type == 2" class="isRead" style="color: #409eff;">已读</span>

                <span v-if="msg_item.sender == OneselfIMID && chat_object.chat_type == 1 && chat_object.peoples.split(',').length > msg_item.isRead.length + 1" class="isRead" style="color: #409eff;" @click="readingUnread($event, msg_item, 0)">{{msg_item.isRead.length}}人已读</span>
                <span v-if="msg_item.isRead.length > 0 && msg_item.sender == OneselfIMID && chat_object.chat_type == 1 && chat_object.peoples.split(',').length <= msg_item.isRead.length + 1" class="isRead" style="color: #BBBBC3;">全部已读</span>
              </span>
              <span>
                <!-- <span v-for="atList in msg_item.msg.atList" :key="atList.id" class="at">@{{atList.name}}</span> -->
                <i v-if="msg_item.warning == 1" class="el-icon-warning error"></i>
                <span style="word-break: break-word;" v-html="traverseEmoji(msg_item.msg.msg)"></span>
              </span>
            </div>
            <!-- 图片 -->
            <div class="content" v-if="msg_item.msg.type == 2">
              <span v-if="localTime-msg_item.clientTimes < 120000 && msg_item.sender == OneselfIMID && localTime-msg_item.clientTimes >= 0" class="revocation" @click="revocationChat(msg_item)">撤回</span>
              <span v-if="msg_item.warning != 1">
                <span v-if="msg_item.isRead.length == 0 && msg_item.sender == OneselfIMID && chat_object.chat_type == 2" class="isRead" style="color: #409eff;">未读</span>
                <span v-if="msg_item.isRead.length > 0 && msg_item.sender == OneselfIMID && chat_object.chat_type == 2" class="isRead" style="color: #409eff;">已读</span>
                <span v-if="msg_item.sender == OneselfIMID && chat_object.chat_type == 1 && chat_object.peoples.split(',').length > msg_item.isRead.length + 1" class="isRead" style="color: #409eff;" @click="readingUnread($event, msg_item, 0)">{{msg_item.isRead.length}}人已读</span>
                <span v-if="msg_item.isRead.length > 0 && msg_item.sender == OneselfIMID && chat_object.chat_type == 1 && chat_object.peoples.split(',').length == msg_item.isRead.length + 1" class="isRead" style="color: #BBBBC3;">全部已读</span>
              </span>
              <img :src="msg_item.msg.fileUrl+'&TOKEN='+token+'&id='+msg_item.msg.fileId" @click="openFilePreview(msg_item.msg)">
            </div>
            <!-- 语音 -->
            <div class="content" v-if="msg_item.msg.type == 3">
              <span v-if="localTime-msg_item.clientTimes < 120000 && msg_item.sender == OneselfIMID && localTime-msg_item.clientTimes >= 0" class="revocation" @click="revocationChat(msg_item)">撤回</span>
              <span v-if="msg_item.warning != 1">
                <span v-if="msg_item.isRead.length == 0 && msg_item.sender == OneselfIMID && chat_object.chat_type == 2" class="isRead" style="color: #409eff;">未读</span>
                <span v-if="msg_item.isRead.length > 0 && msg_item.sender == OneselfIMID && chat_object.chat_type == 2" class="isRead" style="color: #BBBBC3;">已读</span>
                <span v-if="msg_item.sender == OneselfIMID && chat_object.chat_type == 1 && chat_object.peoples.split(',').length > msg_item.isRead.length + 1" class="isRead" style="color: #409eff;" @click="readingUnread($event, msg_item, 0)">{{msg_item.isRead.length}}人已读</span>
                <span v-if="msg_item.isRead.length > 0 && msg_item.sender == OneselfIMID && chat_object.chat_type == 1 && chat_object.peoples.split(',').length == msg_item.isRead.length + 1" class="isRead" style="color: #BBBBC3;">全部已读</span>
              </span>
              <!-- <video autoplay="autoplay" controls="" name="media" width="200" height="30"><source src="http://www.runoob.com/try/demo_source/horse.mp3" type="audio/mpeg"></video> -->
              <audio controls="" preload="auto" :src="msg_item.msg.fileUrl+'&TOKEN='+token+'&id='+msg_item.msg.fileId" width="200" height="30" @play="audioPaly($event)"></audio>
            </div>
            <!-- 文件 -->
            <div class="content" v-if="msg_item.msg.type == 4">
              <span v-if="localTime-msg_item.clientTimes < 120000 && msg_item.sender == OneselfIMID && localTime-msg_item.clientTimes >= 0" class="revocation" @click="revocationChat(msg_item)">撤回</span>
              <span v-if="msg_item.warning != 1">
                <span v-if="msg_item.isRead.length == 0 && msg_item.sender == OneselfIMID && chat_object.chat_type == 2" class="isRead" style="color: #409eff;">未读</span>
                <span v-if="msg_item.isRead.length > 0 && msg_item.sender == OneselfIMID && chat_object.chat_type == 2" class="isRead" style="color: #BBBBC3;">已读</span>
                <span v-if="msg_item.sender == OneselfIMID && chat_object.chat_type == 1 && chat_object.peoples.split(',').length > msg_item.isRead.length + 1" class="isRead" style="color: #409eff;" @click="readingUnread($event, msg_item, 0)">{{msg_item.isRead.length}}人已读</span>
                <span v-if="msg_item.isRead.length > 0 && msg_item.sender == OneselfIMID && chat_object.chat_type == 1 && chat_object.peoples.split(',').length == msg_item.isRead.length + 1" class="isRead" style="color: #BBBBC3;">全部已读</span>
              </span>
              <div class="file_box" @click="openFilePreview(msg_item.msg)">
                <img class="file" v-if="$root.$options.filters.ressuffix(msg_item.msg.fileName) == 'img'" :src="msg_item.msg.fileUrl+'&TOKEN='+token+'&id='+msg_item.msg.fileId" />
                <i :class="msg_item.msg.fileName | ressuffix" class="file"></i>
                <a style="color: #409eff;" href="javascript:;">{{msg_item.msg.fileName}}</a>
              </div>
            </div>
            <!-- 小视频 -->
            <div class="content" v-if="msg_item.msg.type == 5">
              <span v-if="localTime-msg_item.clientTimes < 120000 && msg_item.sender == OneselfIMID && localTime-msg_item.clientTimes >= 0" class="revocation" @click="revocationChat(msg_item)">撤回</span>
              <span v-if="msg_item.warning != 1">
                <span v-if="msg_item.isRead.length == 0 && msg_item.sender == OneselfIMID && chat_object.chat_type == 2" class="isRead" style="color: #409eff;">未读</span>
                <span v-if="msg_item.isRead.length > 0 && msg_item.sender == OneselfIMID && chat_object.chat_type == 2" class="isRead" style="color: #BBBBC3;">已读</span>
                <span v-if="msg_item.sender == OneselfIMID && chat_object.chat_type == 1 && chat_object.peoples.split(',').length > msg_item.isRead.length + 1" class="isRead" style="color: #409eff;" @click="readingUnread($event, msg_item, 0)">{{msg_item.isRead.length}}人已读</span>
                <span v-if="msg_item.isRead.length > 0 && msg_item.sender == OneselfIMID && chat_object.chat_type == 1 && chat_object.peoples.split(',').length == msg_item.isRead.length + 1" class="isRead" style="color: #BBBBC3;">全部已读</span>
              </span>
              <video controls="" name="media" :src="msg_item.msg.fileUrl+'&TOKEN='+token+'&id='+msg_item.msg.fileId" type="video/mp4" @play="videoPaly($event)"></video>
            </div>
            <!-- 位置 -->
            <div class="content" v-if="msg_item.msg.type == 6">
              <span v-if="localTime-msg_item.clientTimes < 120000 && msg_item.sender == OneselfIMID && localTime-msg_item.clientTimes >= 0" class="revocation" @click="revocationChat(msg_item)">撤回</span>
              <span v-if="msg_item.warning != 1">
                <span v-if="msg_item.isRead.length == 0 && msg_item.sender == OneselfIMID && chat_object.chat_type == 2" class="isRead" style="color: #409eff;">未读</span>
                <span v-if="msg_item.isRead.length > 0 && msg_item.sender == OneselfIMID && chat_object.chat_type == 2" class="isRead" style="color: #BBBBC3;">已读</span>
                <span v-if="msg_item.sender == OneselfIMID && chat_object.chat_type == 1 && chat_object.peoples.split(',').length > msg_item.isRead.length + 1" class="isRead" style="color: #409eff;" @click="readingUnread($event, msg_item, 0)">{{msg_item.isRead.length}}人已读</span>
                <span v-if="msg_item.isRead.length > 0 && msg_item.sender == OneselfIMID && chat_object.chat_type == 1 && chat_object.peoples.split(',').length == msg_item.isRead.length + 1" class="isRead" style="color: #BBBBC3;">全部已读</span>
              </span>
              深圳市南山区高新南一道21-2号</div>
            <!-- 提醒 -->
            <div class="content prompt" v-if="msg_item.msg.type == 7">
              {{msg_item.msg.msg}}
            </div>
          </div>
          <!-- 助手消息 -->
          <div class="item" v-if="!msgType" v-for="(as_item, index) in assistantMessageData" :key="index">
            <div class="time-title" v-bind:class="'time' + as_item.timeType" v-if="as_item.timeShow">
              <div class="left"></div>{{as_item.timeTitle}}<div class="right"></div>
            </div>
            <div class="panel" @click="readMessage(as_item);getModuleDetail(as_item)">
              <div class="unread" v-if="as_item.read_status == 0"></div>
              <img class="simpName" :src="'/static/img/im/' + chat_object.type + '.png'" v-if="chat_object.chat_type == 3 && chat_object.type != 1" />
              <div class="simpName" v-if="chat_object.chat_type == 3 && chat_object.type == 1">助手</div>
              <div class="figureInfor">
                <span class="name">{{as_item.bean_name_chinese || '企信'}}</span>
                <span class="time">{{as_item.datetime_create_time | formatDate('MM-dd HH:mm')}}</span>
              </div>
              <!-- 文本 -->
              <div class="content" v-if="as_item.type == 5">
                <div class="panel2">
                  <div class="panel-item"  v-for="(field, index2) in as_item.field_info" :key="index2">
                    <span>{{'名称：' + field.field_value}}</span>
                  </div>
                  <a href="javascript:;">{{as_item.push_content}}</a>
                </div>
              </div>

              <div class="content" v-if="as_item.type != 5">
                {{as_item.push_content}}
                <div class="panel2" v-if="as_item.bean_name">
                  <div class="panel-header">{{as_item.title}}</div>
                  <div class="panel-item"  v-for="(field2, index2) in as_item.field_info" :key="index2">
                    <span v-html="fieldHtml(field2)"></span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <a href="javascript:;" v-if="more > 0 && !msgType" @click="loadMore" style="margin-left: calc(50% - 27px);display: inline-block;margin-top: 10px;">加载更多...</a>
        </el-main>
        <el-footer v-show="msgType" height="150px">
          <div class="tool">
            <form id="sendFile">
              <i class="iconfont icon-pc-paper-upload"></i>
              <input type="file" @change="handleFile" name="fileList" />
            </form>
          <i class="iconfont icon-pc-paper-face" title="表情" @click="openEmojiForm($event)"></i>
          <i class="iconfont icon-pc-paper-2" title="@" v-if="chat_object.chat_type == 1" @click="openCreateGroupFormForm($event)" style="margin-left: -4px;"></i>
          </div>
          <!-- <el-input type="textarea" :rows="5" placeholder="按Enter发送消息" @keyup.enter.native="sendMessage($event)" resize='none'></el-input> -->
          <div id="messageBox2" class="exist" contenteditable="true" @keydown="sendMessage2($event)" placeholder="按Enter发送消息" v-on:mouseleave="saveRange"
          @pause="pastePicture($event)"></div>
        </el-footer>
      </el-container>
    </el-container>
    <!-- 弹出层 -->
    <div style="height: 0">
      <div id="textMenu" style="width:0;height: 0;"></div>
      <el-dialog title='创建群组' :visible.sync='createGroupForm' class='addForm createGroup'>
        <el-form :model='form'>
          <el-form-item>
            <!-- <el-input></el-input> -->
            <input type="text" class="createGroupName" v-model='form.name' placeholder="群组名称12字以内（必填）" />
          </el-form-item>
          <el-form-item label='群公告' :label-width='formLabelWidth'>
            <!-- <el-input v-model='form.notice' type="textarea" :rows="3" resize='none' placeholder="群聊公告100字以内（选填）"></el-input>
            <input type="text" class="createGroupName" v-model='form.notice' placeholder="群聊公告100字以内（选填）" /> -->
            <textarea v-model='form.notice' rows="3" resize='none' placeholder="群聊公告100字以内（选填）" style="width: calc(100% - 30px);"></textarea>
          </el-form-item>
          <el-form-item label='群组成员' :label-width='formLabelWidth'>
            <span v-for="(empItem, index) in prepareData" :key="index" style="float: left;">
              <div v-if="!empItem.picture" class="simpName">{{empItem.name | limitTo(-2)}}</div>
              <img v-if="empItem.picture" :src="empItem.picture+'&TOKEN='+token" />
            </span>

            <i class="iconfont icon-pc-fine-add-staff add-member" @click='getRoleMemberList'></i>
          </el-form-item>
        </el-form>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click="createGroup">确 定</el-button>
          <el-button @click='createGroupForm = false'>取 消</el-button>
        </div>
      </el-dialog>
      <el-dialog title='发起群聊' :visible.sync='groupChatForm' class='addForm groupChat'>
        <div class="item" v-for="(groupItem, index) in groupData" :key="index" @click="addGroupChat(groupItem)">
          <img src="/static/img/im/company.png" v-if="groupItem.chat_type == 1 && groupItem.type == 0" />
          <img src="/static/img/im/group.png" v-if="groupItem.chat_type == 1 && groupItem.type == 1" />
          {{groupItem.name}}
          <i class="iconfont icon-pc-paper-privat"></i></div>
      </el-dialog>
      <el-dialog title='发起私聊' :visible.sync='privateChatForm' class='addForm privateChat'>
        <!-- <el-tree :data='treeData' :props='defaultProps' :v-bind:text="treeData" accordion :expand-on-click-node='false' node-key="value" :render-content="renderContent" :default-expanded-keys="[1]" @node-click="privateChat"></el-tree> -->
        <el-tree :data='treeData' :props='defaultProps' :v-bind:text="treeData" accordion :expand-on-click-node='false' node-key="value" :render-content="renderContent" :default-expanded-keys="[1]" @node-click="privateChat"></el-tree>
      </el-dialog>

      <div class="side-shade" v-if="particularsForm"></div>
      <transition name="slide">
        <div class="particularsForm sideForm" v-if="particularsForm">
          <header>
            <span>成员</span>
            <i class="el-icon-close" @click="particularsForm = false"></i>
          </header>
          <div class="side-body">
            <div class="info" v-if="infoData.chat_type == 1">
              <div class="header">
                <img src="/static/img/im/company.png" v-if="groupInfo.chat_type == 1 && groupInfo.type == 0" />
                <img src="/static/img/im/group.png" v-if="groupInfo.chat_type == 1 && groupInfo.type == 1" />
                <span class="name">{{groupInfo.name}}</span>
              </div>
              <div class="notice">{{groupInfo.notice}}</div>
            </div>
            <div class="top">置顶聊天
              <!-- <el-checkbox v-model="infoChecked" @change="setTop"></el-checkbox> -->
              <el-switch v-model="infoChecked" active-color="#1890FF" inactive-color="#d8dce5" @change="setTop" style="width: 46px;height: 28px;"></el-switch>
            </div>
            <div class="member" v-if="infoData.chat_type == 1">
              <div class="header">成员<span class="num">（{{groupEmployeeInfo.length}}）</span></div>
              <div class="member-box">
                <div class="item" v-for="(item, index) in groupEmployeeInfo" :key="index">
                  <avatar-component :employeeData="{'id': item.id, 'name': item.employee_name, 'picture': item.picture, 'type': 1, 'sign_id': item.sign_id}"></avatar-component>
                  <span class="name">{{item.employee_name}}</span>
                  <span class="remark" v-if="groupInfo.principal == item.sign_id">（管理员）</span>
                  <!-- <span class="remark" v-if="groupInfo.principal != item.sign_id">（{{groupInfo.department_name + '-' + groupInfo.role_name}}）</span> -->
                </div>
              </div>
            </div>
          </div>
        </div>
      </transition>

      <el-dialog title='编辑群聊' :visible.sync='editGroupForm' class='addForm createGroup'>
        <el-form :model='form'>
          <el-form-item>
            <el-input v-model='form.name2' placeholder="群组名称12字以内（必填）"></el-input>
          </el-form-item>
          <el-form-item label='群公告' :label-width='formLabelWidth'>
            <el-input v-model='form.notice' type="textarea" :rows="3" resize='none' placeholder="群聊公告100字以内（选填）"></el-input>
          </el-form-item>
        </el-form>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click="modifyGroupInfo">确 定</el-button>
          <el-button @click='editGroupForm = false'>取 消</el-button>
        </div>
      </el-dialog>
      <el-dialog title="退出群聊" :visible.sync="quitGroupForm" class='deleteForm' modal>
        <span class="prompt">提示：退出后，你将不再接收该群组中的消息，请谨慎操作。</span>
        <span class="remark">你确定要退出群聊吗？</span>
        <span slot="footer" class="dialog-footer">
            <el-button @click="quitGroupForm = false">取 消</el-button>
            <el-button type="primary" @click="quitGroup">确 定</el-button>
        </span>
      </el-dialog>
      <el-dialog title="解散群聊" :visible.sync="releaseGroupForm" class='deleteForm' modal>
        <span class="prompt">提示：解散后，该群聊不可恢复，群聊中的成员将收到解散消息，请谨慎操作。</span>
        <span class="remark">你确定要解散群聊吗？</span>
        <span slot="footer" class="dialog-footer">
            <el-button @click="releaseGroupForm = false">取 消</el-button>
            <el-button type="primary" @click="releaseGroup">确 定</el-button>
        </span>
      </el-dialog>
      <el-dialog title="@群成员列表" :visible.sync="atGroupForm" id='atGroupForm' :modal='false' top="0">
        <a href="javascript:;" class="item" @click="atCheck(null)">所有人({{groupEmployeeInfo.length}}人)</a>
        <a href="javascript:;" class="item" v-for="(item, index) in groupEmployeeInfo" :key="index" @click="atCheck(item)" v-if="item.sign_id != OneselfIMID">{{item.employee_name}}</a>
      </el-dialog>
      <!-- 文件预览 -->
      <!-- <file-preview :fileData='msgFile'></file-preview> -->
      <!-- 筛选 -->
      <div class="side-shade" v-if="screeningForm"></div>
      <transition name="slide">
        <div class="particularsForm sideForm" v-if="screeningForm">
          <header>
            <span>筛选</span>
            <i class="el-icon-close" @click="screeningForm = false"></i>
          </header>
          <div class="side-body">
            <div class="st-item header"><span class="title">模块名称</span><span>查看</span></div>
            <div class="st-item" v-for="sItem in screenData" :key="sItem.id"><span class="title">{{sItem.chinese_name}}</span>
            <el-checkbox v-model="sItem.checked" @change="screenMsg(sItem)"></el-checkbox></div>
          </div>
        </div>
      </transition>
      <!-- 设置 -->
      <div class="side-shade" v-if="settingForm"></div>
      <transition name="slide">
        <div class="particularsForm sideForm" v-if="settingForm">
          <header>
            <span>设置</span>
            <i class="el-icon-close" @click="settingForm = false"></i>
          </header>
          <div class="side-body">
            <div class="info">
              <div class="header">
                <i></i>
                <span class="name">{{assisstantInfo.name}}</span>
                <span class="automaton">机器人</span>
              </div>
              <div class="notice">汇聚所有{{assisstantInfo.name}}信息</div>
            </div>
            <div class="top" style="border-bottom: none;" v-if="assisstantInfo.type != 2">只查看未读消息
              <el-switch v-model="assisstantInfo.show_type" active-color="#409eff" inactive-color="#d8dce5" @change="markReadOption" style="width: 46px;height: 28px;"></el-switch>
            </div>
            <div class="top" style="border-bottom: none;border-top: none;">置顶聊天
              <el-switch v-model="assisstantInfo.infoChecked" active-color="#409eff" inactive-color="#d8dce5" @change="setTop" style="width: 46px;height: 28px;"></el-switch>
            </div>
          </div>
        </div>
      </transition>

      <el-dialog title='已读未读' :visible.sync='isReadForm' class='msgReadForm' id="msgReadForm" :modal='false' width="240px">
        <header @click="msgReadTypessss()">
          <div style="float: left;"><a href="javascript:;" @click="msgReadType = 0" v-bind:class='msgReadType == 0 ? "active":""'>未读（{{msgUnreadNum}}）</a></div>
          <div><a href="javascript:;" @click="msgReadType = 1" v-bind:class='msgReadType == 1 ? "active":""'>已读（{{msgReadNum}}）</a></div>
        </header>
        <div class="item" v-for="item in readEmployeeList" :key="item.id" v-if="msgReadType == item.readType && item.sign_id != OneselfIMID">
          <img v-if="item.picture" :src="item.picture+'&TOKEN='+token" />
          <div class="simpName" v-if="!item.picture">{{item.employee_name | limitTo(-2)}}</div>
          <span class="name" :title="item.employee_name">{{item.employee_name}}</span>
          <span v-if="item.principal">（管理员）</span>
        </div>
      </el-dialog>
      <el-dialog title='2' :visible.sync='memoDetails' class='memoDetails' :modal='false'>
        <memo-details v-if="memoDetails" :memoDetailProp="{}" :dropOptionValue="0" :id='menoDataId' :modal='false' top='0'></memo-details>
      </el-dialog>
      <!-- 文件库详情 -->
      <transition name="slide">
        <file-dtail :fileObject='fileObject' v-if="fileForm"></file-dtail>
      </transition>

      <div class="shade" v-if="openDetail"></div>

      <!-- <transition name="slide">
        <module-detail v-if="openDetail" :data="moduleObecjt"></module-detail>
      </transition> -->
        <!-- 关联关系侧弹组件 -->
        <!-- <module-detail v-if="openDetail" :data="moduleObecjt"></module-detail> -->
        <!-- 自定义新增组件 (关联关系使用)-->
        <transition name="slide">
          <module-create-new v-if="openCreateModal" :modules="modules" :dtlData="dtlData"></module-create-new>
        </transition>
        <!-- 自定义详情组件 (关联关系模块详情) -->
        <transition-group name="slide" tag="div">
          <!-- <div class="shade" v-if="detailList.length > 0"></div> -->
          <module-detail v-for="(data,index) in detailList" :key="index" :data="data" :dtlKey="index"></module-detail>
        </transition-group>

        <!-- 2.ref="approvalDetail" 父组件调用子组件的方法,此时是传入row(审批详情单条数据) -->
        <approvalDetail ref="approvalDetail"></approvalDetail>

        <el-dialog title='email' :visible.sync='emailModel' class='emailModel' :modal='false'>
          <drafts></drafts>
        </el-dialog>
        <!-- <transition name="slide">
          <drafts v-if="emailModel"></drafts>
        </transition> -->
    </div>
  </div>
</template>

<script>
import tool from '@/common/js/tool.js'
import AvatarComponent from '@/common/components/employee-avatar'/** 显示成员信息的头像组件 */
import Portrait from '@/common/components/portrait'
import {plist, emojiUrl, traverseEmoji, locationEmoji} from '@/common/js/emoji.js'
import chatjs from '@/common/js/chat.2.js'
import FileDtail from '@/common/module-dtl/library-dtl'/** 文件库详情 */
import ModuleDetail from '@/frontend/module/module-detail'
import ModuleCreateNew from '@/frontend/module/module-create-new'// 新增界面
import approvalDetail from '@/frontend/approval/approval-detail.vue'/** 审批详情组件 */
import MemoDetails from '@/frontend/memo/memo-detail'/** 备忘录详情组件 */
import Drafts from '@/frontend/Email/Email_interface/mail-detail-helper.vue'/** 邮件详情组件 */
// import filePreview from '@/common/components/file-preview'/** 文件预览 */
import {HTTPServer} from '@/common/js/ajax.js'
import TreeRender from '@/common/module-dtl/employee-render'
export default {
  name: 'IM',
  components: {Portrait, AvatarComponent, ModuleDetail, FileDtail, approvalDetail, MemoDetails, ModuleCreateNew, Drafts},
  data () {
    return {
      fileObject: {},
      fileForm: false,
      msgFile: {},
      moduleObecjt: {},
      openDetail: false,
      msgType: true,
      chat_msg: [], // 聊天信息 type:1文本,2图片3语音4文件,5小视频6位置7提醒
      usCmdID: 1, // 命令类型ID   uint16_t
      ucVer: 1, // 版本号 uint8_t
      ucDeviceType: 1, // 设备类型     uint8_t
      ucFlag: 1, // 标志位：IM_TYPE_REQUEST---请求   IM_TYPE_RESPONSE---响应 uint8_t
      ServerTimes: ((new Date()).getTime()), // int64_t  //服务器时间戳.由服务器来填充
      clientTimes: ((new Date()).getTime()), // 客户端填充时间戳,微秒, int64_t
      RAND: Math.ceil(Math.random() * 1000),
      wsconnectionNum: 0,
      chat_index: 0,
      localTime: ((new Date()).getTime()),
      chatList: [],
      assistantMessageData: [],
      currentPage: 1,
      createGroupForm: false,
      formLabelWidth: '74px',
      more: false,
      visible2: false,
      groupChatForm: false,
      privateChatForm: false,
      particularsForm: false,
      editGroupForm: false,
      quitGroupForm: false,
      releaseGroupForm: false,
      atGroupForm: false,
      screeningForm: false,
      screenData: [],
      settingForm: false,
      assisstantInfo: {},
      chat_object: {},
      infoData: {},
      groupInfo: {},
      groupEmployeeInfo: {},
      isfocus: 0,
      infoChecked: false,
      isReadForm: false,
      readEmployeeList: [],
      msgUnreadNum: 0,
      msgReadNum: 0,
      msgReadType: 0,
      treeData: [],
      defaultProps: {
        children: 'childList',
        label: 'name'
      },
      prepareData: [],
      changeGroupMemberObj: {},
      groupData: [],
      timePoint: [],
      plist: plist,
      emojiUrl: emojiUrl,
      // traverseEmoji: traverseEmoji,
      locationEmoji: locationEmoji,
      form: {
        name: '',
        notice: '',
        peoples: '',
        classify: '3',
        department: '',
        postname: '',
        delivery: false
      },
      openCreateModal: false,
      modules: {},
      dtlData: {},
      detailList: [],
      memoDetails: false,
      menoDataId: 0,
      memoDetail: {},
      emailModel: false
    }
  },
  /* 页面加载后执行 */
  mounted () {
    var userInfo = JSON.parse(sessionStorage.userInfo)
    this.token = JSON.parse(sessionStorage.requestHeader).TOKEN
    this.companyId = userInfo.companyInfo.id
    this.employeeInfo = userInfo.employeeInfo
    // this.getListInfo()
    this.OneselfIMID = this.employeeInfo.sign_id // 自己的IMID
    this.senderID = this.employeeInfo.sign_id // 发送者ID号,登录请求包可以不填充. int64_t
    this.receiverID = 0 // 接收者ID.当群发的时候为群ID
    this.$bus.off('overloadEmployee')
    this.$bus.on('overloadEmployee', (value) => {
      if (value) {
        userInfo = JSON.parse(sessionStorage.userInfo)
        this.employeeInfo = userInfo.employeeInfo
      }
    })
    if (this.chatList.length === 0) {
      var returnData = chatjs.returnData()
      this.chatList = returnData.list
      this.ServerTimes = returnData.ServerTimes
      this.timeInterval = returnData.timeInterval
      this.updateChatObject(this.chatList)
    }
    let _this = this
    setInterval(function () {
      if (_this.localTime) {
        _this.localTime += 1000
      }
    }, 1000)
    this.$bus.off('checkemoji')
    this.$bus.on('checkemoji', (value) => {
      if (value) {
        if (value.data && value.id === 'chat') {
          document.getElementById('messageBox2').focus()
          this._insertimg('<img class="emoji" src="' + this.emojiUrl + value.data.string + '.gif" title="' + value.data.key + '" />')
        }
      }
    })
    // this.$bus.off('selectEmpDepRoleMulti')
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value) {
        this.prepareData = value.prepareData
        this.changeGroupMemberObj = value
      }
    })
    // this.$bus.off('closeMemoDetail')
    this.$bus.on('closeMemoDetail', (value) => {
      this.memoDetails = false
    })
    // 关闭邮件详情窗口
    // this.$bus.off('closeEmailDetailModal')
    this.$bus.on('closeEmailDetailModal', value => {
      this.emailModel = false
    })
    // 关闭自定义详情窗口
    // this.$bus.off('closeDetailModal')
    this.$bus.on('closeDetailModal', value => {
      this.openDetail = false
      this.detailList.splice(value)
    })
    // 打开新增编辑窗口(关联关系)
    // this.$bus.off('customOpenCreateModal')
    this.$bus.on('customOpenCreateModal', (modules, data) => {
      this.modules = modules
      this.dtlData = data
      this.openCreateModal = true
    })
    // 关闭新增窗口(关联关系)
    // this.$bus.off('customCloseCreateModal')
    this.$bus.on('customCloseCreateModal', value => {
      this.openCreateModal = false
    })
    this.$bus.off('transmit')
    this.$bus.on('transmit', (data) => {
      console.log('Chat监听', data)
      switch (data.type) {
        case 1 :// 登录
          this.ServerTimes = data.data.ServerTimes
          this.timeInterval = data.data.timeInterval
          this.historyMsg()
          break
        case 5 :// 对聊
          this.readStorage(data.data)
          break
        case 6 :// 群聊
          this.readStorage(data.data)
          break
        case 7: // 推送的信息
          // this.readStorage()
          var msg = data.data.msg
          console.log('chat-im', data)
          if (this.chat_object.id === msg.assistant_id) {
            if (msg.type === 16 || msg.type === 17) {
              this.setAssistantMessage(msg)
            } if (msg.type === 20) {
              /** 修改助手推送 */
              this.setAssistantMessage(msg)
            } else {
              this.getAssistantMessage({'id': msg.assistant_id, 'beanName': msg.bean_name || ''})
            }
          }
          break
        case 8: // 单用户消息
          if (data.tag1 === 1003) {
            this.setAssistantMessage(data.data)
          } else {
            this.chatList[data.tag1] = data.data
            this.$set(this.chatList, data.tag1, data.data)
            if (this.chat_object.id === data.data.id) {
              this.chat_object = data.data
            }
          }
          break
        case 11: // 单用户消息
          this.readStorage(data.data)
          break
        case 12: // 单用户消息
          this.readStorage(data.data)
          break
        case 18 :// 个人消息已读
          this.changeGroupState(data.data)
          break
        case 19 :// 群消息已读
          this.changeGroupState(data.data)
          break
        case 24: // 撤销个人聊天
          if (this.chat_object.receiver_id === data.data.senderID) {
            this.modifyRevocationReply(24, data.data)
          }
          break
        case 25: // 撤销个人聊天成功 <----服务器返回的成功命令
          if (this.chat_object.receiver_id === data.data.receiverID) {
            this.modifyRevocationReply(25, data.data)
          }
          break
        case 26: // 撤销群聊天成功
          if (this.chat_object.id === data.data.receiverID) {
            this.modifyRevocationReply(26, data.data)
          }
          break
        case 27: // 撤销群聊天成功  <---服务器返回的成功命令
          if (this.chat_object.id === data.data.receiverID) {
            this.modifyRevocationReply(27, data.data)
          }
          break
        case 'chat_object': // 设置当前聊天对象
          this.chatList = data.data
          if (this.chat_object.receiver_id !== data.tag1.receiver_id || !this.chat_object.receiver_id) {
            this.chat_object = Object.assign({}, this.chat_object, data.tag1)
            console.log('chat_object', this.chat_object)
            this.runCurrentObj()
          }
          break
        case 'chatList': // 获取回话列表
          this.chatList = data.data
          if (!data.tag1) {
            if (this.chat_object.is_hide === '1') this.chat_object = {}
            this.updateChatObject(data.data)
          } else if (data.tag1) {
            if (data.tag1.type === 1002 && this.chat_object.id === data.tag1.assistant_id) {
              this.chat_object.icon_url = data.tag1.icon_url
              this.chat_object.icon_type = data.tag1.icon_type
              this.chat_object.icon_color = data.tag1.icon_color
              this.chat_object.icon_url = data.tag1.application_name
            } else if (data.tag1.type === 1004 && this.chat_object.id === data.tag1.assistant_id) {
              this.chat_object = {}
              this.updateChatObject(data.data)
            }
            // var contains = tool.contains(this.chatList, 'id', data.tag1, 'assistant_id')
            // console.log(contains)
            // if (contains) {
            //   this.$set(this.chatList, contains.i, this.chatList[contains.i])
            //   /** 视图更新 object */
            //   if (this.chat_object.id === data.tag1.assistant_id) this.chat_object = Object.assign({}, this.form, contains)
            // }
          }
          console.log(this.chatList)
          break
        case 'unread_nums': // 获取回话列表
          if (this.chat_object.id !== data.data.msg.chatId) {
            chatjs.statisticalChatList(2, data.data.msg.chatId)
          }
          break
      }
    })
  },
  destroyed () {
    // console.log(2134324234234)
    // this.$bus.off('selectEmpDepRoleMulti')
  },
  watch: {
    prepareData (data) {
      if (this.changeGroupMemberObj.prepareKey === ('group' + this.employeeInfo.id) && !this.changeGroupMemberObj.seleteForm) {
        var arr = []
        var sendData = []
        for (var i = 0; i < this.prepareData.length; i++) {
          if (this.prepareData[i].sign_id !== this.OneselfIMID) {
            arr.push(this.prepareData[i].sign_id)
          }
          sendData.push(this.prepareData[i])
        }
        this.changeGroupMember({'id': this.chat_object.id, 'peoples': arr.toString()}, sendData)
      }
    }
  },
  created () {
    /** 处理粘贴图片 */
    setTimeout(() => {
      document.getElementById('messageBox2').addEventListener('paste', function (e) {
        console.log(e)
        tool.pastePicture(e)
      })
    }, 800)
  },
  computed: {
    sortChatList () {
      return this.sortKey(this.chatList)
    }
  },
  methods: {
    /** 排序 */
    sortKey (array) {
      var list = this.chatList.sort(this.by('top_status', this.by('latest_push_time')))
      return list
    },
    /** 排序 */
    by (name, minor) {
      return function (o, p) {
        var a
        var b
        if (o && p && typeof o === 'object' && typeof p === 'object') {
          a = o[name]
          b = p[name]
          if (a === b) {
            return typeof minor === 'function' ? minor(o, p) : 0
          }
          if (typeof a === typeof b) {
            return a < b ? 1 : -1
          }
          return typeof a < typeof b ? 1 : -1
        } else {
          console.log('error')
        }
      }
    },
    /** 路劲解析 */
    traverseEmoji (str) {
      var params = traverseEmoji(str)
      var reg = /(https?|http|ftp|file):\/\/[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]/g
      // var reg = /((http|ftp|https):\/\/)?[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&amp;:/~\+#]*[\w\-\@?^=%&amp;/~\+#])?/g
      // var reg = /((http|ftp|https):\/\/)?[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]/g
      // var reg2 = /^(((ht|f)tp(s?))\:\/\/)?(www.|[a-zA-Z].)[a-zA-Z0-9\-\.]+\.(com|edu|gov|mil|net|org|biz|info|name|museum|us|ca|uk)(\:[0-9]+)*(/($|[a-zA-Z0-9\./\,\;\?\'\\\+&amp;%\$#\=~_\-]+))*$/g
      var arr = params.match(reg)
      // params = params.replace(reg, (item) => {
      // })
      if (arr) {
        arr.map((item, index) => {
          params = params.replace(new RegExp(item, 'ig'), '<a target="_blank" href="' + item + '">' + item + '</a>')
        })
      }
      params = params.replace(new RegExp('\\n', 'g'), '<br>')
      return params
    },
    /** 助手字段解析 */
    fieldInit (data) {
      var arr = []
      var list
      var value = data.field_value
      if (data.type === 'datetime') {
        return tool.formatDate(value, 'yyyy-MM-dd HH:mm')
      } else if (data.type === 'area') {
        console.log(value)
        value.split(',').map((item) => {
          console.log(item)
          var area1 = item.split(':')
          arr.push(area1[1])
        })
        return arr.join('')
      } else if (data.type === 'location') {
        return JSON.parse(value).value
      } else if (data.type === 'reference') {
        list = JSON.parse(value)
        list.map((item) => {
          arr.push(item.name)
        })
        return arr.join(';')
      } else if (data.type === 'picklist') {
        list = JSON.parse(value)
        list.map((item) => {
          arr.push(item.label)
        })
        return arr.join('')
      } else if (data.type === 'multi') {
        list = JSON.parse(value)
        list.map((item) => {
          arr.push(item.label)
        })
        return arr.join('')
      } else {
        return value
      }
    },
    /** 助手字段解析 */
    fieldHtml (data) {
      var str = data.field_label + '：'
      return str + (data.field_value ? this.fieldInit(data) : '')
    },
    /** 音频单播放 */
    audioPaly (event) {
      tool.audioPaly(event)
    },
    /** 视频单播放 */
    videoPaly (event) {
      tool.videoPaly(event)
    },
    /** 重新更新聊天对象 */
    updateChatObject (data) {
      data = this.sortKey(data)
      console.log('chat_object', this.chat_object)
      if (!this.chat_object.id) {
        for (var i = 0; i < data.length; i++) {
          if (data[i].is_hide === '0') {
            this.chat_object = data[i]
            break
          }
        }
        this.runCurrentObj()
      }
    },
    /** 运行当前对象 */
    runCurrentObj () {
      this.chatId = this.chat_object.id
      if (this.chat_object.chat_type === 3) {
        this.findModuleList()
        this.msgType = false
        if (!this.chat_object.application_id && this.chat_object.type === 2) {
          if (this.chat_object.unread_nums) {
            this.markAllRead()
          }
        }
      } else {
        this.historyMsg()
        chatjs.emptyChatListNum(this.chat_object.chat_type, this.chat_object.id)
        if (this.chat_object.chat_type === 1) {
          this.getGroupInfo(this.chat_object.id)
        }
      }
    },
    /** 修改撤销命令 本地内容 */
    modifyRevocationReply (type, data) {
      if (type === 24 || type === 25) {
        for (var j = 0; j < this.chat_msg.length; j++) {
          if (this.chat_msg[j].clientTimes === data.clientTimes && this.chat_msg[j].RAND === data.RAND) {
            this.chat_msg[j].msg.type = 7
            this.chat_msg[j].msg.msg = (type === 25) ? '你撤回了一条消息' : '对方撤回了一条消息'
            this.$set(this.chat_msg, j, this.chat_msg[j])
          }
        }
      } else {
        for (var z = 0; z < this.chat_msg.length; z++) {
          if (this.chat_msg[z].clientTimes === data.clientTimes && this.chat_msg[z].RAND === data.RAND) {
            this.chat_msg[z].msg.type = 7
            this.chat_msg[z].msg.msg = this.chat_msg[z].msg.senderName + '撤回了一条消息'
            this.$set(this.chat_msg, z, this.chat_msg[z])
          }
        }
      }
    },
    /** 发送历史消息命令 */
    historyMsg () {
      if (this.chat_object.chat_type !== 1 && this.chat_object.chat_type !== 2) {
        return
      }
      var msgId = this.companyId + '-' + this.OneselfIMID + '-' + this.chat_object.id
      var IMessage = JSON.parse(localStorage.getItem(msgId))
      if (!IMessage) {
        this.usCmdID = 20// 离线命令
        let senddata = this.init_packet()
        let historyBuffer = new ArrayBuffer(19)
        let hisoryBlob = new DataView(historyBuffer)
        hisoryBlob.setUint8(0, (this.chat_object.chat_type === 2) ? 1 : 2, true)
        this.translate((this.chat_object.chat_type === 2) ? this.chat_object.receiver_id : this.chat_object.id, 'setId', hisoryBlob)
        let historytTime = new Date().getTime()
        this.translate(historytTime, 'setTimeStamp', hisoryBlob)
        hisoryBlob.setUint16(17, 99, true) // 获取记录条数
        let historyMsg = new Blob([senddata, hisoryBlob])
        chatjs.send(historyMsg)
        setTimeout(() => {
          this.readStorage()
        }, 1000)
      } else {
        /** 待定代码 */
        this.readStorage()
      }
    },
    /** 获取模块详情 */
    getModuleDetail (data) {
      console.log(data)
      if (data.bean_name === 'file_library') {
        this.getFuncAuthWithCommunal({'bean': data.bean_name, 'dataId': data.data_id, 'style': data.style}, data)
      } else if (data.bean_name === 'memo') {
        this.getFuncAuthWithCommunal({'bean': data.bean_name, 'style': 100, 'dataId': data.data_id}, data)
      } else if (data.bean_name === 'email') {
        var jsondata = {'id': data.data_id, 'type': 1}
        HTTPServer.getEmailListParticulars(jsondata, 'Loading').then((res) => {
          this.emailModel = true
          res.at = '@'
          setTimeout(() => {
            this.$bus.emit('listenHelperParticularsId', res)
          }, 100)
        })
      } else if (data.type === 3) {
        this.getFuncAuthWithCommunal({'bean': data.bean_name, 'dataId': data.data_id}, data)
      } else if (data.type === 4 || data.bean_name === 'approval') {
        var paramFields = JSON.parse(data.param_fields)
        HTTPServer.queryApprovalData({'dataId': paramFields.dataId, 'type': paramFields.fromType, 'taskKey': paramFields.taskKey, 'moduleBean': paramFields.moduleBean, 'processInstanceId': paramFields.processInstanceId}, 'loading').then((res) => {
          this.$refs.approvalDetail.apprDetail(res, data)
        })
      }
    },
    // 判断数据权限
    getFuncAuthWithCommunal (jsondata, data) {
      this.fileForm = false
      HTTPServer.getFuncAuthWithCommunal(jsondata, 'loading').then((res) => {
        if (res.readAuth === 0 || res.readAuth === 2) {
          this.$message.error('无权查看或数据已删除!')
          return
        } else if (res.readAuth === 3) {
          this.$message.error('该数据已转移至公海池!')
          return
        }
        if (data.bean_name === 'file_library') {
          this.fileForm = true
          setTimeout(() => {
            this.fileObject = {'fileForm': true, 'fileData': {'id': data.data_id}, 'navObject': {'id': data.style}}
          }, 200)
        } else if (data.bean_name === 'memo') {
          this.menoDataId = data.data_id
          this.memoDetails = true
        } else if (data.type === 3) {
          this.openDetail = true
          this.moduleObecjt = {'dataId': data.data_id, 'bean': data.bean_name, 'moduleName': data.bean_name_chinese, 'auth': []}
          // this.moduleObecjt = {'dataId': item.id, 'bean': item.module, 'moduleName': item.beanNameChinese, 'moduleId': item.moduleId}
          this.detailList.push(this.moduleObecjt)
        }
      })
    },
    /** 开启表情 */
    openEmojiForm (event) {
      this.$bus.emit('getMmojiData', {'id': 'chat'})
      locationEmoji(event)
    },
    /** 返回本地数据 Key */
    returnMsgId (id) {
      return this.companyId + '-' + this.OneselfIMID + '-' + (id || this.chat_object.id)
    },
    /** 聊天设置 */
    chatPopoverSet (type) {
      if (type === '0') {
        this.openCreateGroupForm()
      } else if (type === '1') {
        this.openGroupChatForm()
      } else {
        this.openPrivateChatForm()
      }
    },
    /** 获取可编辑div中的光标坐标 */
    getSelectionCoords (win) {
      win = win || window
      var doc = win.document
      var sel = doc.selection
      var range
      var rects
      var rect
      var x = 0
      var y = 0
      if (sel) {
        if (sel.type !== 'Control') {
          range = sel.createRange()
          range.collapse(true)
          x = range.boundingLeft
          y = range.boundingTop
        }
      } else if (win.getSelection) {
        sel = win.getSelection()
        if (sel.rangeCount) {
          range = sel.getRangeAt(0).cloneRange()
          if (range.getClientRects) {
            range.collapse(true)
            rects = range.getClientRects()
            if (rects.length > 0) {
              rect = rects[0]
            }
            // 光标在行首时，rect为undefined
            if (rect) {
              x = rect.left
              y = rect.top
            }
          }
          // Fall back to inserting a temporary element
          if ((x === 0 && y === 0) || rect === undefined) {
            var span = doc.createElement('span')
            if (span.getClientRects) {
              // Ensure span has dimensions and position by
              // adding a zero-width space character
              span.appendChild(doc.createTextNode('\u200b'))
              range.insertNode(span)
              rect = span.getClientRects()[0]
              x = rect.left
              y = rect.top
              var spanParent = span.parentNode
              spanParent.removeChild(span)

              // Glue any broken text nodes back together
              spanParent.normalize()
            }
          }
        }
      }
      return { x: x, y: y }
    },
    /** 上传文件 */
    pasteFile (formData) {
      formData.append('bean', 'chat')
      HTTPServer.commonUpload(formData, '').then((res) => {
        this.clientTimes = (new Date()).getTime() + this.timeInterval
        this.RAND = this.RAND + 1
        res.map((item) => {
          var jsondata = {
            'senderName': this.employeeInfo.name,
            'senderAvatar': this.employeeInfo.picture,
            'type': 2,
            'fileUrl': item.file_url,
            'fileName': item.file_name,
            'fileSize': item.file_size,
            'fileType': item.file_type,
            'chatId': this.chatId,
            'times': this.clientTimes,
            'clientTimes': this.clientTimes
          }
          this.ws_sendmessage(jsondata)
        })
      })
    },
    atFun () {
      this.isfocus = 0
      if (this.chat_object.chat_type === 1) {
        event.returnValue = false
        this.atlist = []
        this.getGroupInfo(this.chat_object.id, 'at')
        var rect = this.getSelectionCoords()
        var left = (rect.x < 310) ? (rect.x + 25) : (rect.x + 10)
        var atGroupStyle = document.getElementById('atGroupForm').childNodes[0].style
        let myVar = setInterval(() => {
          if (this.atlist.length > 0) {
            this.atGroupForm = true
            var top = (this.groupEmployeeInfo.length > 5) ? 180 : (this.groupEmployeeInfo.length * 30)
            atGroupStyle.top = (rect.y - top) + 'px'
            atGroupStyle.left = left + 'px'
            clearInterval(myVar)
          }
        }, 100)
      }
    },
    /** 发送消息 */
    sendMessage2 (event) {
      if (event.keyCode === 13) {
        event.preventDefault()
        var str = ''
        var atArr = []
        var messageBox = document.getElementById('messageBox2')
        /** 遍历@ */
        if (this.chat_object.chat_type === 1) {
          var arr = []
          var atNode = messageBox.getElementsByClassName('at')
          for (var i = 0; i < atNode.length; i++) {
            var empId = atNode[i].getAttribute('data-id')
            var contains = tool.contains(this.groupEmployeeInfo, 'id', parseInt(empId))
            if (empId === '0') {
              arr.push({'id': '0', 'name': '所有人'})
            }
            if (contains) {
              arr.push(this.groupEmployeeInfo[contains.i])
            }
          }
          for (var j = 0; j < arr.length; j++) {
            var contains2 = tool.contains(atArr, 'id', arr[j], 'sign_id')
            if (arr[j].id === '0') {
              atArr.push({'id': '0', 'name': '所有人'})
            }
            if (!contains2 && arr[j].id !== '0') {
              atArr.push({'id': arr[j].sign_id, 'name': arr[j].employee_name})
            }
          }
          atArr = (atArr.length > 0) ? atArr : []
        }
        var emArr = []
        var bascArr = []
        var img = messageBox.getElementsByTagName('img')
        for (var EM = 0; EM < img.length; EM++) {
          if (img[EM].getAttribute('class')) {
            emArr.push(img[EM].getAttribute('title'))
          } else {
            bascArr.push(img[EM].getAttribute('src').split(',')[1])
          }
        }
        var arrHTML = messageBox.innerHTML.split(/<img[^>]+>/g)
        for (var RE = 0; RE < arrHTML.length; RE++) {
          arrHTML[RE] = this.removeHTMLTag(arrHTML[RE])
          if (arrHTML[RE]) str += arrHTML[RE]
          if (emArr[RE]) str += emArr[RE]
        }
        if (!str && atArr.length === 0 && bascArr.length === 0) {
          this.$message.error('不能发送空白消息！')
          return
        }
        bascArr.map((item) => {
          this.pasteFile(tool.basc64ToFile(item))
        })
        if (atArr.length > 0) {
          var atStr = ''
          for (var at = 0; at < atArr.length; at++) {
            atStr += '@' + atArr[at].name
          }
          str = atStr + ' ' + str
        }
        event.target.innerHTML = ''
        /** 以防截图冲突 */
        if (!str && atArr.length === 0) {
          return
        }
        this.clientTimes = (new Date()).getTime() + this.timeInterval
        this.RAND = this.RAND + 1
        var jsondata = {'msg': str, 'type': 1, 'times': this.clientTimes, 'clientTimes': this.clientTimes, 'chatId': this.chatId, 'senderAvatar': this.employeeInfo.picture, 'senderName': this.employeeInfo.name, 'atList': atArr}
        if (!atArr) {
          delete jsondata.atList
        }
        this.ws_sendmessage(jsondata)
      }
      /** @ 键 处理 */
      if (event.key === '@') {
        this.atFun()
      }
      /** @ 键 处理 */
      if (event.key === 'Process' && event.code === 'Digit2' && event.shiftKey) {
        this.atFun()
      }
      // if (this.atGroupForm && event.key === 'ArrowDown') {
      //   if (this.isfocus < this.groupEmployeeInfo.length){
      //     this.isfocus++
      //   }
      //   document.getElementById('atGroupForm').getElementsByClassName('el-dialog__body')[0].scrollTo(0, this.isfocus * 30)
      // }
      // if (this.atGroupForm && event.key === 'ArrowUp') {
      //   if (this.isfocus > 0) this.isfocus--
      //   document.getElementById('atGroupForm').getElementsByClassName('el-dialog__body')[0].scrollTo(0, this.isfocus * 30)
      // }
    },
    saveRange () {
      document.getElementById('messageBox2').focus()
      var selection = window.getSelection ? window.getSelection() : document.selection
      var range = selection.createRange ? selection.createRange() : selection.getRangeAt(0)
      this._range = range
    },
    removeHTMLTag (str) {
      str = str.replace(/<\/?[^>]*>/g, '') // 去除HTML tag
      str = str.replace(/[ | ]*\n/g, '\n') // 去除行尾空白
      str = str.replace(/&nbsp;/ig, ' ')// 去掉&nbsp;
      // str = str.replace(/\s/g, '') // 将空格去掉
      str = str.replace(/<[^>]+>/g, '')
      return str
    },
    /** 插入内容 */
    _insertimg (str) {
      if (!this._range) {
        document.getElementById('messageBox2').focus()
        var doms = document.createElement('span')
        if (typeof str === 'string') doms.innerHTML = str
        document.getElementById('messageBox2').appendChild(doms)
        return
      }
      var selection
      if (!window.getSelection) {
        document.getElementById('messageBox2').focus()
        selection = window.getSelection ? window.getSelection() : document.selection
        var range = selection.createRange ? selection.createRange() : selection.getRangeAt(0)
        range.pasteHTML(str)
        range.collapse(false)
        range.select()
      } else {
        document.getElementById('messageBox2').focus()
        selection = window.getSelection ? window.getSelection() : document.selection
        selection.addRange(this._range)
        range = this._range
        range.collapse(false)
        var hasR = range.createContextualFragment(str)
        var hasRlastChild = hasR.lastChild
        while (hasRlastChild && hasRlastChild.nodeName.toLowerCase() === 'br' && hasRlastChild.previousSibling && hasRlastChild.previousSibling.nodeName.toLowerCase() === 'br') {
          var e = hasRlastChild
          hasRlastChild = hasRlastChild.previousSibling
          hasR.removeChild(e)
        }
        range.insertNode(hasR)
        if (hasRlastChild) {
          range.setEndAfter(hasRlastChild)
          range.setStartAfter(hasRlastChild)
        }
        selection.removeAllRanges()
        selection.addRange(range)
        var reg = /@<input[^<>]+>/g
        var dom = document.getElementById('messageBox2').innerHTML
        var match = document.getElementById('messageBox2').innerHTML.match(reg)
        if (match) {
          match.map((item, index) => {
            dom = dom.replace(item, item.substring(1, item.length))
          })
          document.getElementById('messageBox2').innerHTML = dom
        }
      }
    },
    /** 查看已读未读群主人员 */
    msgReadTypessss () {
      for (var i = 0; i < this.readEmployeeList.length; i++) {
        this.$set(this.readEmployeeList, i, this.readEmployeeList[i])
      }
    },
    /** 查看已读未读群主人员 */
    readingUnread (event, data) {
      HTTPServer.getGroupInfo({'groupId': this.chat_object.id}, '').then((res) => {
        this.isReadForm = true
        var getLeft = event.clientX - event.offsetX
        var getTop = event.clientY - event.offsetY
        let msgReadForm = document.getElementById('msgReadForm')
        var empData = res.employeeInfo
        var principal = res.groupInfo.principal
        this.msgReadType = 0
        this.msgUnreadNum = -1
        this.msgReadNum = 0
        this.readEmployeeList = []
        for (var i = 0; i < empData.length; i++) {
          var jsondata = {'sign_id': empData[i].sign_id, 'employee_name': empData[i].employee_name, 'picture': empData[i].picture}
          jsondata.principal = (empData[i].sign_id === principal)
          if (data.isRead.indexOf(empData[i].sign_id) >= 0) {
            jsondata.readType = 1
            this.msgReadNum++
          } else {
            jsondata.readType = 0
            this.msgUnreadNum++
          }
          jsondata.readType = (data.isRead.indexOf(empData[i].sign_id) >= 0) ? 1 : 0
          this.readEmployeeList.push(jsondata)
        }
        msgReadForm.childNodes[0].style.left = getLeft - 245 + 'px'
        msgReadForm.childNodes[0].style.top = getTop - 220 + 'px'
      })
    },
    /** 加载更多... */
    loadMore () {
      if (this.chat_object.chat_type === 3) {
        var module = this.chat_object.module || []
        this.currentPage++
        var bean = []
        for (var b = 0; b < module.length; b++) {
          bean.push(module[b].english_name)
        }
        this.getAssistantMessage({'id': this.chat_object.id, 'beanName': bean.toString() || ''})
      } else {
        // var arr = JSON.parse(localStorage.getItem(this.returnMsgId()))
        // var len = arr.length - this.chat_msg.length
        // this.more = (len > 20) ? Math.ceil(len / 20) : 0
        // if (arr) {
        //   this.chat_msg = arr.slice((len - 20 > 0) ? len - 20 : 0, arr.length)
        // } else {
        //   this.chat_msg = []
        // }
        var IMessage = JSON.parse(localStorage.getItem(this.returnMsgId()))
        var le = 0
        var len = le - this.chat_msg.length
        this.more = (len > 20) ? Math.ceil(len / 20) : 0
        var arr = []
        for (var key in IMessage) {
          var data = IMessage[key]
          if (data.msg.type === 4) {
            var fileType = tool.determineFileType(data.msg.fileType.substr(1, data.msg.fileType.length))
            if (fileType.fileType === 'img') {
              data.msg.type = 2
            } else if (fileType.fileType === 'video') {
              data.msg.type = 5
            } else if (fileType.fileType === 'voice') {
              data.msg.type = 3
            }
          }
          var timeText = this.getTimeText(data.clientTimes)
          if (timeText.text) {
            data.timeShow = timeText.show
            data.timeTitle = timeText.text
            data.timeType = timeText.type
          }
          if (le === 0) {
            data.timeShow = true
          }
          le++
          arr.push(data)
        }
        this.chat_msg = arr.slice((len - 20 > 0) ? len - 20 : 0, le)
        var content = document.getElementById('content')
        setTimeout(function () {
          if (content) {
            content.scrollTop = content.scrollHeight - this.scrollTop
            this.scrollTop = content.scrollHeight
          }
        }, 100)
      }
    },
    /** 输出 @ */
    atCheck (data) {
      data = data || {'id': '0', 'employee_name': '所有人'}
      if (data.sign_id === this.OneselfIMID) {
        return
      }
      document.getElementById('messageBox2').focus()
      var width = this.calcStringPixelsCount('@' + data.employee_name, 14)
      this._insertimg('<input class="at" data-id="' + data.id + '" readonly="readonly" style="width: ' + width + 'px" value="@' + data.employee_name + '" />&nbsp;')
      document.getElementById('messageBox2').focus()
      this.atGroupForm = false
    },
    /** 回话列表-时间处理 */
    getTimeText (argument) {
      var ServerTimes = ((new Date()).getTime()) + this.timeInterval
      var msgDate = new Date(argument)
      var timeCha = ServerTimes - msgDate.getTime() - 1000
      var weekday = ['星期天', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      var jsondata = {}
      var serverDate = new Date(ServerTimes)
      var todayT = serverDate.getHours() * 60 * 60 * 1000 + serverDate.getMinutes() * 60 * 1000 + serverDate.getSeconds() * 1000
      var yestodayT = todayT + 24 * 60 * 60 * 1000
      var repeats = true
      if (this.timePoint[0]) {
        repeats = !(this.timePoint[0] === msgDate.getFullYear() && this.timePoint[1] === msgDate.getMonth() + 1 && this.timePoint[2] === msgDate.getDate())
      }
      this.timePoint[0] = msgDate.getFullYear()
      this.timePoint[1] = msgDate.getMonth() + 1
      this.timePoint[2] = msgDate.getDate()
      // if (timeCha < 0) {
      //   jsondata.text = msgDate.getFullYear() + '年' + (msgDate.getMonth() + 1) + '月' + msgDate.getDate() + '日' + weekday[msgDate.getDay()]
      //   jsondata.type = 4
      // }
      if (timeCha > yestodayT) {
        var weeks = (serverDate.getDay() === 0) ? 7 : serverDate.getDay()
        var weekdayT = todayT + (weeks - 1) * 24 * 60 * 60 * 1000
        if (weeks - 2 > 0 && timeCha < weekdayT) {
          jsondata.text = weekday[msgDate.getDay()]
          jsondata.type = 2
        } else {
          jsondata.text = (msgDate.getMonth() + 1) + '月' + msgDate.getDate() + '日' + weekday[msgDate.getDay()]
          jsondata.type = 3
        }
      } else if (timeCha > todayT && timeCha < yestodayT) {
        jsondata.text = '昨天'
        jsondata.type = 1
      } else if (timeCha <= todayT) {
        jsondata.text = '今天'
        jsondata.type = 0
      }
      jsondata.show = repeats
      return jsondata
    },
    /** 添加本地数据 */
    addStorage (data) {
      var msgId = this.companyId + '-' + this.OneselfIMID + '-' + data.msg.chatId
      var IMessage = JSON.parse(localStorage.getItem(msgId))
      data.isRead = []
      if (IMessage) {
        var contains = tool.contains(IMessage, 'times', data, 'times')
        if (!contains) {
          IMessage.push(data)
        }
      } else {
        IMessage = []
        IMessage.push(data)
      }
      localStorage.setItem(msgId, JSON.stringify(IMessage))
      if (this.chat_object.id === data.msg.chatId && this.usCmdID !== 20) {
        var jsondata = {
          msg: data.msg,
          times: data.times,
          sender: data.sender,
          RAND: data.RAND,
          clientTimes: data.clientTimes,
          warning: data.warning
        }
        jsondata.isRead = []
        this.readStorage(jsondata)
      }
    },
    /** 读取本地 */
    readStorage (data) {
      chatjs.emptyChatListNum(this.chat_object.chat_type, this.chat_object.id)
      this.ServerTimes = ((new Date()).getTime()) + this.timeInterval
      var timeText = ''
      var bool = !!data
      if (data) {
        bool = (this.chat_object.id === data.msg.chatId)
      }
      if (bool) {
        // if (data.msg.type === 4) {
        //   fileType = tool.determineFileType(data.msg.fileType.substr(1, data.msg.fileType.length))
        //   if (fileType.fileType === 'img') {
        //     data.msg.type = 2
        //   } else if (fileType.fileType === 'video') {
        //     data.msg.type = 5
        //   } else if (fileType.fileType === 'voice') {
        //     data.msg.type = 3
        //   }
        // }
        timeText = this.getTimeText(data.clientTimes)
        if (timeText.text) {
          data.timeShow = timeText.show
          data.timeTitle = timeText.text
          data.timeType = timeText.type
        }
        if (this.chat_msg.length === 0) {
          data.timeShow = true
        }
        this.chat_msg.push(data)
      } else {
        var IMessage = JSON.parse(localStorage.getItem(this.returnMsgId()))
        var le = 0
        var arr = []
        this.chat_msg = []
        for (var key in IMessage) {
          var obj = IMessage[key]
          // if (obj.msg.type === 4) {
          //   fileType = tool.determineFileType(obj.msg.fileType.substr(1, obj.msg.fileType.length))
          //   if (fileType.fileType === 'img') {
          //     obj.msg.type = 2
          //   } else if (fileType.fileType === 'video') {
          //     obj.msg.type = 5
          //   } else if (fileType.fileType === 'voice') {
          //     obj.msg.type = 3
          //   }
          // }
          timeText = this.getTimeText(obj.clientTimes)
          if (timeText.text) {
            obj.timeShow = timeText.show
            obj.timeTitle = timeText.text
            obj.timeType = timeText.type
          }
          if (le === 0) {
            obj.timeShow = true
          }
          le++
          arr.push(obj)
        }
        var len = le - this.chat_msg.length
        this.more = (len > 20) ? Math.ceil(len / 20) : 0
        this.chat_msg = arr.slice((len - 20 > 0) ? len - 20 : 0, le)
      }
      this.ws_sendread()
      if (!data || data.sender === this.OneselfIMID) {
        var content = document.getElementById('content')
        setTimeout(function () {
          if (content) {
            content.scrollTop = content.scrollHeight
            this.scrollTop = content.scrollHeight
          }
        }, this.chat_msg.length * 20)
      }
    },
    /** 管理群成员 */
    changeGroupMember (jsondata, sendData) {
      setTimeout(() => {
        this.memberNum = 0
      }, 800)
      HTTPServer.changeGroupMember(jsondata, 'Loading').then((res) => {
        var groupData = res.groupInfo
        var contains = tool.contains(this.chatList, 'id', groupData, 'id')
        if (contains) {
          this.chatList[contains.i] = groupData
          this.$set(this.chatList, contains.i, groupData)
          chatjs.updateChatData(this.chatList)
        }
        this.chat_object = groupData
        var addName = []
        console.log('changeGroupMember', contains.peoples)
        var peoples = contains.peoples.split(',')
        sendData.map((item) => {
          if (!peoples.includes(item.sign_id.toString()) && !addName.includes(item.name)) {
            addName.push(item.name)
          }
        })
        if (addName.length > 0) {
          this.usCmdID = 6
          this.receiverID = groupData.id
          var jsondata = {'msg': '欢迎' + addName.join('、') + '加入本群', 'type': 7, 'times': (new Date()).getTime() + this.timeInterval, 'chatId': this.chatId, 'senderAvatar': this.employeeInfo.picture, 'senderName': this.employeeInfo.name}
          this.ws_sendmessage(jsondata)
        }

        var removeName = []
        this.groupEmployeeInfo.map((item) => {
          var contains2 = tool.contains(sendData, 'sign_id', item, 'sign_id')
          if (!contains2) {
            removeName.push(item.employee_name)
          }
        })
        if (removeName.length > 0) {
          this.usCmdID = 6
          this.receiverID = groupData.id
          var jsondata2 = {'msg': '管理员将' + removeName.join('、') + '移出群聊', 'type': 7, 'times': ((new Date()).getTime()) + this.timeInterval, 'chatId': this.chatId, 'senderAvatar': this.employeeInfo.picture, 'senderName': this.employeeInfo.name}
          setTimeout(() => {
            this.ws_sendmessage(jsondata2)
          }, 100)
        }
      })
    },
    /** 群设置列表项 */
    setCommand (type) {
      this.setId = JSON.parse(JSON.stringify(this.chat_object.id))
      if (type === '0') {
        this.getGroupInfo(this.chat_object.id, 1)
      } else if (type === '1') {
        this.form.id = this.chat_object.id
        this.form.name2 = this.chat_object.name
        this.form.notice = this.chat_object.notice
        this.editGroupForm = true
      } else if (type === '2') {
        this.releaseGroupForm = true
      } else {
        this.quitGroupForm = true
      }
    },
    /** 修改群设置 */
    modifyGroupInfo () {
      var name = this.form.name2.trim()
      var notice = this.form.notice.trim()
      if (!name) {
        this.$message.error('群组名称不能为空！')
        return
      }
      if (name.length > 12) {
        this.$message.error('群组名称不能超过12个字符！')
        return
      }
      if (notice.length > 100) {
        this.$message.error('群组公告不能超过100个字符！')
        return
      }
      var jsondata = {'id': this.form.id, 'name': this.form.name2, 'notice': this.form.notice}
      HTTPServer.modifyGroupInfo(jsondata, '').then((res) => {
        this.editGroupForm = false
        this.chat_object.name = this.form.name2
        this.chat_object.notice = this.form.notice
        for (var i = 0; i < this.chatList.length; i++) {
          if (this.chatList[i].id === this.form.id) {
            this.chatList[i].name = this.form.name2
            this.chatList[i].notice = this.form.notice
          }
        }
        chatjs.updateChatList3(this.chatList)
      })
    },
    /** 退出群 */
    quitGroup () {
      HTTPServer.quitGroup({'id': this.chat_object.id}, '').then((res) => {
        this.usCmdID = 6
        this.receiverID = this.chat_object.id
        var jsondata = {'msg': this.employeeInfo.name + '退出群聊', 'type': 7, 'times': ((new Date()).getTime()) + this.timeInterval, 'chatId': this.chatId, 'senderAvatar': this.employeeInfo.picture, 'senderName': this.employeeInfo.name}
        this.ws_sendmessage(jsondata, 5)
        this.quitGroupForm = false
        var contains = tool.contains(this.chatList, 'id', this.chat_object, 'id')
        if (contains) {
          this.chatList.splice(contains.i, 1)
          this.$set(this.chatList, contains.i, this.chatList[contains.i])
        }
        chatjs.clearReply(contains.id)
        this.chat_object = {}
        this.updateChatObject(this.chatList)
        chatjs.updateChatList3(this.chatList)
      })
    },
    /** 解散群 */
    releaseGroup () {
      HTTPServer.releaseGroup({'id': this.chat_object.id}, 'Loading').then((res) => {
        this.releaseGroupForm = false
        var contains = tool.contains(this.chatList, 'id', this.chat_object, 'id')
        if (contains) {
          this.chatList.splice(contains.i, 1)
          this.$set(this.chatList, contains.i, this.chatList[contains.i])
        }
        chatjs.clearReply(contains.id)
        this.chat_object = {}
        this.updateChatObject(this.chatList)
        chatjs.updateChatList3(this.chatList)
      })
    },
    /** 置顶 */
    setTop () {
      var jsondata = {'id': this.chat_object.id, 'chat_type': this.chat_object.chat_type}
      HTTPServer.imsetTop(jsondata, 'Loading').then((res) => {
        this.chat_object.top_status = (this.chat_object.top_status === '1') ? '0' : '1'
        // chatjs.updateChatList2(this.chat_object)
      })
    },
    /** 开启详情弹窗 */
    openParticularsForm () {
      if (this.chat_object.chat_type === 1) {
        this.getGroupInfo(this.chat_object.id)
        this.particularsForm = true
      } else if (this.chat_object.chat_type === 2) {
        this.infoData = this.chat_object
        this.infoChecked = (this.chat_object.top_status === '1')
        this.particularsForm = true
      } else {
        this.getAssisstantInfo()
      }
    },
    /** 获取群信息 */
    getGroupInfo (id, type) {
      HTTPServer.getGroupInfo({'groupId': id}, 'Loading').then((res) => {
        this.groupEmployeeInfo = res.employeeInfo
        if (type === 1) {
          this.prepareData = []
          for (var i = 0; i < this.groupEmployeeInfo.length; i++) {
            this.prepareData.push({'id': this.groupEmployeeInfo[i].id, 'name': this.groupEmployeeInfo[i].employee_name, 'picture': this.groupEmployeeInfo[i].picture, 'type': 1, 'sign_id': this.groupEmployeeInfo[i].sign_id, 'value': '1-' + this.groupEmployeeInfo[i].id})
          }
          this.getRoleMemberList('group' + this.employeeInfo.id)
        } else if (type === 'at') {
          this.atlist = res.employeeInfo
        } else {
          this.groupInfo = res.groupInfo
          this.infoData = res
          this.infoData.chat_type = 1
          this.infoChecked = (this.groupInfo.top_status === '1')
          // setTimeout(() => {
          //   var model = document.getElementsByClassName('particularsForm')
          //   var info = model[0].getElementsByClassName('info')[0].clientHeight
          //   model[0].getElementsByClassName('member')[0].style.height = 'calc(100% - ' + (info + 54) + 'px)'
          // }, 500)
        }
      })
    },
    // 初始化数据包头
    init_packet () {
      // 数据包头
      let OneselfIMID = this.OneselfIMID
      let usCmdID = this.usCmdID
      let ucVer = this.ucVer
      let ucDeviceType = this.ucDeviceType
      let ucFlag = this.ucFlag
      let ServerTimes = this.ServerTimes
      let senderID = this.senderID
      let receiverID = this.receiverID
      let clientTimes = this.clientTimes
      let RAND = this.RAND
      let buffer = new ArrayBuffer(49)
      let headData = new DataView(buffer)
      // 设置自己的IMID
      this.translate(OneselfIMID, 'oneselfIMID', headData)
      // 命令类型
      this.translate(usCmdID, 'usCmdID', headData)
      // 版本号
      this.translate(ucVer, 'ucVer', headData)
      // 设备类型
      this.translate(ucDeviceType, 'ucDeviceType', headData)
      // 标志位
      this.translate(ucFlag, 'ucFlag', headData)
      // 服务器时间戳
      headData.setUint8(21, ServerTimes)
      // 发送者ID
      this.translate(senderID, 'senderID', headData)
      // 接受者ID
      this.translate(receiverID, 'receiverID', headData)
      // 客户端时间戳
      this.translate(clientTimes, 'clientTimes', headData)
      // 信息标识随机数
      this.translate(RAND, 'RAND', headData)
      return headData.buffer // 返回数据包头
    },
    translate (num, type, headData) { // 写入64位数据
      let str16 = num.toString(16)
      if (str16.length % 2 !== 0) {
        str16 = '0' + str16
      }
      let arr16 = str16.split('').reverse()
      let len = arr16.length
      let index = 0
      for (let i = 0; i <= len; i++) {
        if (i % 2 === 0 && i !== 0) {
          let data = Number('0x' + `${arr16[i - 1]}` + `${arr16[i - 2]}`)
          switch (type) {
            case 'oneselfIMID':
              headData.setUint8(0 + index, data) // 设置自己的IMID
              index++
              break
            case 'usCmdID':
              headData.setUint8(8 + index + index, data)
              index++
              break
            case 'ucVer':
              headData.setUint8(10 + index, data)
              index++
              break
            case 'ucDeviceType':
              headData.setUint8(11 + index, data)
              index++
              break
            case 'ucFlag':
              headData.setUint8(12 + index, data)
              index++
              break
            case 'senderID':
              headData.setUint8(21 + index, data)
              index++
              break
            case 'receiverID':
              headData.setUint8(29 + index, data)
              index++
              break
            case 'clientTimes':
              headData.setUint8(37 + index, data)
              index++
              break
            case 'RAND':
              headData.setUint8(45 + index, data)
              index++
              break
            case 'setId':
              headData.setUint8(1 + index, data)
              index++
              break
            case 'setTimeStamp':
              headData.setUint8(9 + index, data)
              index++
              break
          }
        }
      }
    },
    /** 消息撤回 */
    revocationChat (data) {
      console.log(data)
      // var msgDate = new Date(data.times)
      var timeCha = ((new Date()).getTime()) - data.times
      if (timeCha >= 120000) {
        this.$message.error('消息已超过2分钟')
        return
      }
      this.senderID = this.OneselfIMID
      this.receiverID = (this.chat_object.chat_type === 1) ? this.chat_object.id : this.chat_object.receiver_id
      this.RAND = data.RAND || this.RAND
      this.ServerTimes = data.ServerTimes || this.ServerTimes
      this.clientTimes = data.times || this.clientTimes
      this.usCmdID = (this.chat_object.chat_type === 1) ? 26 : 24
      let senddata = this.init_packet()
      let chatblob = new Blob([senddata, JSON.stringify({})])
      chatjs.send(chatblob)
    },
    /** 修改群已读未读状态 */
    changeGroupState (data) {
      if (this.chat_object.id === data.id) {
        for (var i = 0; i < this.chat_msg.length; i++) {
          if (this.chat_msg[i].isRead.indexOf(data.sendId) < 0) {
            this.chat_msg[i].isRead.push(data.sendId)
            this.$set(this.chat_msg, i, this.chat_msg[i])
          }
        }
      }
    },
    // 取64位的值
    getVal_64 (startNum, endNum, DataView) { // 获取64位数据
      let data16 = []
      for (let i = startNum; i < endNum; i++) {
        if (i < endNum) {
          let data = DataView.getUint8(i)
          let _str16 = data.toString(16)
          if (_str16.length === 1) {
            _str16 = '0' + _str16
          }
          data16.splice(0, 0, _str16)
        }
      }
      let dataStr = ''
      data16.forEach(element => {
        dataStr = dataStr + element
      })
      return Number('0x' + dataStr)
    },
    /** 发送 已读未读指令 */
    ws_sendread () {
      if (this.chat_msg.length <= 0) {
        return
      }
      if (!this.chat_msg[this.chat_msg.length - 1].RAND) {
        return
      }
      let senddata
      let chatblob
      if (this.chat_object.chat_type === 1) {
        this.usCmdID = 19
        this.receiverID = this.chat_object.id
        var IMessage = JSON.parse(localStorage.getItem(this.returnMsgId()))
        if (IMessage) {
          for (var key in IMessage) {
            var data = IMessage[key]
            if (data.sender !== this.OneselfIMID && !data.send) {
              data.send = true
              this.senderID = data.sender
              this.RAND = data.RAND
              this.clientTimes = data.clientTimes
              senddata = this.init_packet()
              chatblob = new Blob([senddata, JSON.stringify({})])
              chatjs.send(chatblob)
              console.log('成功发送群已读未读指令-' + data.msg.msg)
            }
            IMessage[data.id] = data
          }
          localStorage.setItem(this.returnMsgId(), JSON.stringify(IMessage))
        }
      } else {
        var lastmsg = this.chat_msg[this.chat_msg.length - 1]
        if (lastmsg.sender !== this.employeeInfo.sign_id) {
          this.usCmdID = 18
          this.receiverID = this.employeeInfo.sign_id
          this.senderID = this.chat_object.receiver_id
          this.RAND = this.chat_msg[this.chat_msg.length - 1].RAND
          this.clientTimes = this.chat_msg[this.chat_msg.length - 1].clientTimes
          senddata = this.init_packet()
          chatblob = new Blob([senddata, JSON.stringify({})])
          chatjs.send(chatblob)
          console.log('成功发送个人已读未读指令-' + this.chat_msg[this.chat_msg.length - 1].msg.msg)
        }
      }
      this.usCmdID = (this.chat_object.chat_type === 1) ? 6 : 5
    },
    // 发送websokcer 聊天信息
    ws_sendmessage (jsondata, ucFlag) {
      this.ucFlag = ucFlag || 1
      this.usCmdID = (this.chat_object.chat_type === 1) ? 6 : 5
      this.receiverID = (this.chat_object.chat_type === 1) ? this.chat_object.id : this.chat_object.receiver_id
      this.senderID = this.employeeInfo.sign_id
      this.clientTimes = (new Date()).getTime() + this.timeInterval
      this.chat_index++
      this.RAND++
      let senddata = this.init_packet()
      let chatblob = new Blob([senddata, JSON.stringify(jsondata)])
      this.my_msg = {
        msg: jsondata,
        times: this.clientTimes,
        sender: this.OneselfIMID
      }
      if (chatblob.size > 8000) {
        this.$message.error('消息内容超长，请分条发送！')
        return
      }
      chatjs.send(chatblob, this.my_msg)
      this.ucFlag = 1
    },
    openCreateGroupFormForm (event) {
      this.getGroupInfo(this.chat_object.id)
      document.getElementById('atGroupForm').childNodes[0].style.top = event.pageY - 20 + 'px'
      document.getElementById('atGroupForm').childNodes[0].style.left = event.pageX + 25 + 'px'
      this.atGroupForm = true
    },
    /** 插入节点 */
    insertHtmlAtCaret (html) {
      var sel, range
      if (window.getSelection) {
        sel = window.getSelection()
        if (sel.getRangeAt && sel.rangeCount) {
          range = sel.getRangeAt(0)
          range.deleteContents()
          var el = document.createElement('div')
          el.innerHTML = html
          var frag = document.createDocumentFragment()
          var node
          var lastNode
          while ((node = el.firstChild)) {
            lastNode = frag.appendChild(node)
          }
          range.insertNode(frag)
          if (lastNode) {
            range = range.cloneRange()
            range.setStartAfter(lastNode)
            range.collapse(true)
            sel.removeAllRanges()
            sel.addRange(range)
          }
        }
      } else if (document.selection && document.selection.type !== 'Control') { // IE < 9
        document.selection.createRange().pasteHTML(html)
      }
    },
    /** 草稿 html */
    traverseDraftText (data) {
      return '<span style="color: red;font-size: 12px;">[草稿]</span>' + data.draftText
    },
    // 草稿
    draftInit (data) {
      if (data) {
        document.getElementById('messageBox2').innerHTML = data.draftHtml || ''
        this.chatList.map((item, index) => {
          if (item.id === this.chat_object.id) {
            this.chatList[index].draftHtml = ''
            this.chatList[index].draftText = ''
          }
        })
        chatjs.updateChatList3(this.chatList)
      } else {
        this.chatList.map((item, index) => {
          if (item.id === this.chat_object.id) {
            var messageBox = document.getElementById('messageBox2')
            this.chatList[index].draftHtml = messageBox.innerHTML
            var at = messageBox.getElementsByClassName('at')
            var text = messageBox.innerHTML
            text = text.replace(new RegExp('\\' + '&nbsp;', 'ig'), '')
            if (at) {
              for (var i = 0; i < at.length; i++) {
                text = text.replace(new RegExp('\\' + at[i].outerHTML, 'ig'), at[i].value)
              }
            }
            this.chatList[index].draftText = text
          }
        })
        chatjs.updateChatList3(this.chatList)
      }
    },
    /** 切换聊天对象 */
    switchChat (event, data) {
      if (event) {
        if (event.target.nodeName === 'I') {
          return
        }
      }
      console.log(data)
      this.draftInit()
      this.msgType = true
      this.chat_object = data
      this.chat_msg = []
      this.currentPage = 1
      for (var i = 0; i < this.chatList.length; i++) {
        this.chatId = data.id
        if (data.chat_type === 2) {
          if (this.chatList[i].receiver_id === data.receiver_id && this.chatList[i].chat_type === data.chat_type) {
            this.usCmdID = 5
            this.receiverID = data.receiver_id
          }
        } else if (data.chat_type === 1) {
          if (this.chatList[i].id === data.id && this.chatList[i].chat_type === data.chat_type) {
            this.usCmdID = 6
            this.receiverID = data.id
          }
        } else {
          if (this.chatList[i].id === data.id) {
            this.msgType = false
            if (!data.application_id && data.type === 2) {
              if (this.chat_object.unread_nums) {
                this.markAllRead()
              } else {
                this.getAssistantMessage({'id': this.chat_object.id, 'beanName': ''})
              }
            } else {
              this.findModuleList()
            }
          }
        }
      }
      if (data.chat_type !== 3) {
        this.draftInit(data)
        this.readStorage()
      }
      this.historyMsg() /** 获取历史消息 */
    },
    /** 上传文件 */
    handleFile () {
      var formData = new FormData(document.getElementById('sendFile'))
      document.getElementById('sendFile').getElementsByTagName('input')[0].value = ''
      formData.append('bean', 'chat')
      HTTPServer.commonUpload(formData, 'Loading').then((data) => {
        this.clientTimes = (new Date()).getTime() + this.timeInterval
        this.RAND = this.RAND + 1
        var jsondata = {
          'senderName': this.employeeInfo.name,
          'senderAvatar': this.employeeInfo.picture,
          'type': 4,
          'fileUrl': data[0].file_url,
          'fileName': data[0].file_name,
          'fileSize': data[0].file_size,
          'fileType': data[0].file_type,
          'chatId': this.chatId,
          'times': this.clientTimes,
          'clientTimes': this.clientTimes
        }
        this.ws_sendmessage(jsondata)
      })
    },
    /** 创建群组 */
    openCreateGroupForm () {
      this.createGroupForm = true
      this.visible2 = false
      this.form.name = ''
      this.form.notice = ''
      this.prepareData = [{'id': this.employeeInfo.id, 'name': this.employeeInfo.name, 'picture': this.employeeInfo.picture, 'type': 1, 'sign_id': this.employeeInfo.sign_id, 'value': '1-' + this.employeeInfo.id}]
    },
    /** 创建群组 */
    createGroup () {
      var name = this.form.name.trim()
      var notice = this.form.notice.trim()
      if (!name) {
        this.$message.error('群组名称不能为空！')
        return
      }
      if (name.length > 12) {
        this.$message.error('群组名称不能超过12个字符！')
        return
      }
      if (notice.length > 100) {
        this.$message.error('群组公告不能超过100个字符！')
        return
      }
      var empID = []
      var empName = []
      for (var i = 0; i < this.prepareData.length; i++) {
        if (this.prepareData[i].sign_id !== this.OneselfIMID) {
          empID.push(this.prepareData[i].sign_id)
          empName.push(this.prepareData[i].name)
        }
      }
      if (empID.length < 1) {
        this.$message.error('至少一名群成员！')
        return
      }
      var jsondata = {
        'name': name, // 群名称
        'notice': notice, // 群公告
        'peoples': empID.toString(), // 群成员，
        'type': '1' // 群聊类型设置 0：总群 1：新建群聊
      }
      HTTPServer.addGroupChat(jsondata, 'Loading').then((res) => {
        var data = res.groupInfo
        this.msgType = true
        this.createGroupForm = false
        this.chatList.unshift(data)
        this.chat_object = data
        this.chatId = data.id
        this.usCmdID = 6
        this.more = 0
        this.receiverID = data.id
        for (var i = 0; i < this.chatList.length; i++) {
          this.chatList[i].checked = false
        }
        this.chatList[0].checked = true
        this.chat_msg = []
        var jsondata = {'msg': '欢迎' + empName.join('、') + '加入本群', 'type': 7, 'times': ((new Date()).getTime()) + this.timeInterval, 'chatId': this.chatId, 'senderAvatar': this.employeeInfo.picture, 'senderName': this.employeeInfo.name}
        this.ws_sendmessage(jsondata)
      })
    },
    /** 获取部门角色成员 */
    getRoleMemberList (key) {
      this.$bus.emit('commonMember', {'prepareData': this.prepareData, 'prepareKey': key, 'seleteForm': true, 'banData': ['1-' + this.employeeInfo.id], 'navKey': '1', 'removeData': ['1-' + this.employeeInfo.id]}) // 给父组件传值
    },
    /** 移除回话 */
    removeSession (data) {
      var jsondata = {'id': data.id, 'status': '1', 'chat_type': data.chat_type}
      var total = 0
      this.chatList.map((item, index) => {
        if (item.id === data.id) {
          this.chatList[index].is_hide = '1'
          this.chatList[index].unread_nums = 0
          this.$set(this.chatList, index, this.chatList[index])
          if (item.id === this.chat_object.id) {
            this.chat_object = {}
            this.updateChatObject(this.chatList)
          }
        }
        total += parseInt(item.unread_nums || 0)
      })
      chatjs.updateChatData(this.chatList)
      this.$bus.emit('chat-unread-nums', total) /** 总计未读数 */
      chatjs.hideSession(jsondata)
    },
    /** 单聊所有企业成员 */
    renderContent (h, {node, data, store}) {
      // return (
      //   <span style="flex: 1; display: in; align-items: center; justify-content: space-between; font-size: 14px; padding-right: 8px;" class={'type' + data.type}>
      //     <span>
      //       <span class="simpName">{data.name.substr(0, 2)}</span>
      //       <span>{node.label}</span>
      //     </span>
      //     <span>
      //       <i class="iconfont icon-pc-paper-privat"></i>
      //     </span>
      //   </span>
      // )
      let that = this// 指向vue
      return h(TreeRender, {
        props: {
          DATA: data, // 节点数据
          NODE: node, // 节点内容
          STORE: store
        },
        on: {// 绑定方法
          nodeAdd: (s, d, n) => that.handleAdd(s, d, n),
          nodeEdit: (s, d, n) => that.handleEdit(s, d, n),
          nodeDel: (s, d, n) => that.handleDelete(s, d, n)
        }
      })
    },
    /** 获取单聊所有企业成员 */
    privateChat (data) {
      if (data.type === 1) {
        this.chat_object = {}
        this.msgType = true
        chatjs.addSingleChat(data)
        this.privateChatForm = false
      }
    },
    /** 选人初始化 */
    funInit (arr) {
      for (var i = 0; i < arr.length; i++) {
        arr[i].type = 0
        if (arr[i].sign === 'gs') {
          arr[i].type = 4
        }
        if (arr[i].childList) {
          if (arr[i].childList.length > 0) {
            this.funInit(arr[i].childList)
          }
        }
        if (arr[i].users) {
          if (arr[i].users.length > 0) {
            var users = arr[i].users
            for (var j = 0; j < users.length; j++) {
              if (users[j].sign_id !== this.OneselfIMID) {
                arr[i].childList.push({ 'id': users[j].id, 'name': users[j].employee_name, 'picture': users[j].picture, 'type': 1, 'sign_id': users[j].sign_id, 'value': users[j].value })
              }
            }
          }
          delete arr[i].users
        }
      }
    },
    /** 获取单聊所有企业成员 */
    openPrivateChatForm () {
      this.visible2 = false
      HTTPServer.findUsersByCompany({'companyId': 1}, '').then((res) => {
        this.treeData = res
        this.funInit(this.treeData)
        this.privateChatForm = true
      })
    },
    /** 添加群聊 */
    addGroupChat (data) {
      if (data.is_hide === '1') {
        chatjs.hideSession({'id': data.id, 'status': '0', 'chat_type': data.chat_type}, data)
      }
      data.is_hide = '0'
      this.groupChatForm = false
      this.switchChat(null, data)
    },
    /** 获取企信当前用户所有组的信息 */
    openGroupChatForm () {
      this.visible2 = false
      HTTPServer.getAllGroupsInfo({}, 'Loading').then((res) => {
        this.groupData = res
        this.groupChatForm = true
      })
    },
    /** 获取长度 */
    calcStringPixelsCount (str, strFontSize) {
      // 字符串字符个数
      var stringCharsCount = str.length

      // 字符串像素个数
      var stringPixelsCount = 0

      // JS 创建HTML元素：span
      var elementPixelsLengthRuler = document.createElement('span')
      elementPixelsLengthRuler.style.fontSize = strFontSize // 设置span的fontsize
      elementPixelsLengthRuler.style.visibility = 'hidden' // 设置span不可见
      elementPixelsLengthRuler.style.height = '0'
      elementPixelsLengthRuler.style.position = 'absolute'
      elementPixelsLengthRuler.style.display = 'inline-block'
      elementPixelsLengthRuler.style.wordBreak = 'break-all !important' // 打断单词

      // 添加span
      document.getElementById('textMenu').appendChild(elementPixelsLengthRuler)

      for (var i = 0; i < stringCharsCount; i++) {
        // 判断字符是否为空格，如果是用&nbsp;替代，原因如下：
        // 1）span计算单个空格字符（ ），其像素长度为0
        // 2）空格字符在字符串的开头或者结果，计算时会忽略字符串
        if (str[i] === ' ') {
          elementPixelsLengthRuler.innerHTML = '&nbsp;'
        } else {
          elementPixelsLengthRuler.innerHTML = str[i]
        }
        stringPixelsCount += elementPixelsLengthRuler.offsetWidth
      }

      return stringPixelsCount
    },
    /** 修改消息未读状态 */
    readMessage (data) {
      if (parseInt(data.read_status) === 1) {
        return
      }
      HTTPServer.IMreadMessage({'id': data.id, 'assistantId': data.assistant_id}, 'Loading').then((res) => {
        var contains = tool.contains(this.assistantMessageData, 'id', data, 'id')
        if (contains) {
          data.read_status = 1
          this.$set(this.assistantMessageData, contains.i, data)
        }
        var jsondata = JSON.parse(localStorage.unreadData)
        var total = 0
        this.chatList.map((item, index) => {
          if (item.id === data.assistant_id) {
            item.unread_nums = parseInt(this.chatList[index].unread_nums) - 1
            this.$set(this.chatList, index, item)
            jsondata[this.companyId][item.id + '-' + this.OneselfIMID] = item.unread_nums
          }
          total += parseInt(item.unread_nums || 0)
        })
        chatjs.updateChatData(this.chatList)
        this.$bus.emit('chat-unread-nums', total) /** 总计未读数 */
        localStorage.unreadData = JSON.stringify(jsondata)
      })
    },
    /** 获取小助手设置相关信息 */
    getAssisstantInfo () {
      HTTPServer.getAssisstantInfo({'assisstantId': this.chat_object.id}, '').then((data) => {
        this.settingForm = true
        this.assisstantInfo = data
        this.assisstantInfo.show_type = data.show_type !== '0'
        this.assisstantInfo.infoChecked = data.top_status !== '0'
      })
    },
    /** 只查看未读消息 */
    markReadOption () {
      HTTPServer.markReadOption({'id': this.chat_object.id}, 'Loading').then((res) => {
        this.currentPage = 1
        this.getAssistantMessage({'id': this.chat_object.id, 'beanName': ''})
      })
    },
    /** 模块消息筛选 */
    screenMsg (data) {
      var contains = tool.contains(this.screenData, 'id', data, 'id')
      if (contains) {
        this.$set(this.screenData, contains.i, data)
      }
      var contains2 = tool.contains(this.chatList, 'application_id', data, 'application_id')
      if (contains2) {
        this.chatList[contains2.i].module = []
        for (var s = 0; s < this.screenData.length; s++) {
          if (this.screenData.checked) {
            this.chatList[contains2.i].module.push(this.screenData[s])
          }
        }
      }
      var arr = []
      for (var i = 0; i < this.screenData.length; i++) {
        if (this.screenData[i].checked) {
          arr.push(this.screenData[i].english_name)
        }
      }
      if (arr.length > 0) {
        this.getAssistantMessage({'id': this.chat_object.id, 'beanName': arr.toString() || ''})
      } else {
        this.assistantMessageData = []
      }
    },
    /** 助手全部标记已读 */
    markAllRead () {
      HTTPServer.markAllRead({'id': this.chat_object.id, 'assistantId': this.chat_object.id}, '').then((res) => {
        this.findModuleList()
        chatjs.emptyChatListNum(3, this.chat_object.id)
        if (this.chat_object.type !== 2) {
          this.$message({
            type: 'success',
            message: '操作成功!'
          })
        }
      })
    },
    /** 筛选 */
    screening () {
      HTTPServer.IMfindModuleList({'application_id': this.chat_object.application_id}, '').then((res) => {
        this.screeningForm = true
        this.screenData = res
        for (var i = 0; i < this.screenData.length; i++) {
          this.screenData[i].checked = true
        }
      })
    },
    /** 获取模块列表 */
    findModuleList () {
      if (!this.chat_object.application_id) {
        this.getAssistantMessage({'id': this.chat_object.id, 'beanName': ''})
        return
      }
      HTTPServer.IMfindModuleList({'application_id': this.chat_object.application_id}, '').then((data) => {
        var contains = tool.contains(this.chatList, 'application_id', data[0], 'application_id')
        if (contains) {
          this.chatList[contains.i].module = data
        }
        var arr = []
        for (var i = 0; i < data.length; i++) {
          arr.push(data[i].english_name)
        }
        this.getAssistantMessage({'id': this.chat_object.id, 'beanName': arr.toString()})
      })
    },
    /** 获取助手消息列表 */
    getAssistantMessage (jsondata) {
      jsondata.pageNo = this.currentPage
      jsondata.pageSize = 20
      HTTPServer.getAssistantMessage(jsondata, 'Loading').then((res) => {
        var arr = res.dataList
        var pageInfo = res.pageInfo
        this.assistantMessageData = (this.currentPage === 1) ? [] : this.assistantMessageData
        for (var i = 0; i < arr.length; i++) {
          var timeText = this.getTimeText(arr[i].datetime_create_time)
          if (timeText.text) {
            arr[i].timeShow = timeText.show
            arr[i].timeTitle = timeText.text
            arr[i].timeType = timeText.type
          }
          if (i === 0) {
            arr[i].timeShow = true
          }
          this.assistantMessageData.push(arr[i])
        }
        this.more = (this.currentPage < pageInfo.totalPages) ? this.currentPage : 0
      })
    },
    /** 修改-助手消息列表 */
    setAssistantMessage (data) {
      var jsondata
      var total = 0
      if (data.type === 16) { // 单条已读
        jsondata = JSON.parse(localStorage.unreadData)
        var contains = tool.contains(this.assistantMessageData, 'id', data, 'data_id')
        if (contains) {
          if (this.assistantMessageData[contains.i].read_status === '0') {
            this.assistantMessageData[contains.i].read_status = '1'
            this.$set(this.assistantMessageData, contains.i, this.assistantMessageData[contains.i])
            total = 0
            this.chatList.map((item, index) => {
              if (item.id === data.assistant_id) {
                item.unread_nums = parseInt(this.chatList[index].unread_nums) - 1
                this.$set(this.chatList, index, item)
                jsondata[this.companyId][item.id + '-' + this.OneselfIMID] = item.unread_nums
              }
              total += parseInt(item.unread_nums || 0)
            })
            chatjs.updateChatData(this.chatList)
            this.$bus.emit('chat-unread-nums', total) /** 总计未读数 */
            localStorage.unreadData = JSON.stringify(jsondata)
          }
        }
      } else if (data.type === 17) { // 多条已读
        jsondata = JSON.parse(localStorage.unreadData)
        this.assistantMessageData.map((item, index) => {
          item.read_status = '1'
          this.$set(this.assistantMessageData, index, item)
        })
        total = 0
        this.chatList.map((item, index) => {
          if (item.id === data.assistant_id) {
            item.unread_nums = 0
            this.$set(this.chatList, index, item)
            jsondata[this.companyId][item.id + '-' + this.OneselfIMID] = item.unread_nums
          }
          total += parseInt(item.unread_nums || 0)
        })
        chatjs.updateChatData(this.chatList)
        this.$bus.emit('chat-unread-nums', total) /** 总计未读数 */
        localStorage.unreadData = JSON.stringify(jsondata)
      } else if (data.type === 20) { // 审批操作
        var contains2 = tool.contains(this.assistantMessageData, 'id', data, 'im_apr_id')
        if (contains2) {
          this.assistantMessageData[contains2.i].param_fields = JSON.stringify(data.param_fields)
          // this.$set(this.assistantMessageData, contains2.i, this.assistantMessageData[contains2.i])
        } else {
          this.getAssistantMessage({'id': this.chat_object.id, 'beanName': ''})
        }
        console.log(this.assistantMessageData)
      } else if (data.type === 1003) { // 修改小助手消息模块名称、图标
        this.assistantMessageData.map((item, index) => {
          console.log(item)
          if (item.bean_name === data.bean) {
            item.icon_url = data.icon_url
            item.icon_type = data.icon_type
            item.icon_color = data.icon_color
            item.bean_name_chinese = data.module_name
            this.$set(this.assistantMessageData, index, item)
          }
        })
        console.log(this.assistantMessageData)
      }
    },
    /** 预览文件 */
    openFilePreview (data) {
      console.log(data)
      // this.msgFile = {}
      data.fileForm = true
      data.file_name = data.fileName
      data.file_type = data.fileType
      data.file_size = data.fileSize
      data.file_url = data.fileUrl + '&TOKEN=' + this.token
      this.$bus.emit('file-preview', data)
    }
  }
}
</script>

<style lang='scss'>
@import '../../common/scss/emoji.scss';
@import '../../common/scss/dialog.scss';

.cpChat-main{
  .simpName{border-radius: 50%;}
  >.el-container{
  height: 100%;
  .el-header{line-height: 60px;font-size: 18px;color: #69696C;border-bottom: 1px solid #e7e7e7;
    .iconfont{font-size: 30px;float: left;margin: 16px 10px 0 0;line-height: 1;}
    img{width: 30px;height: 30px;border-radius: 50%;float: left;margin: 16px 10px 0 0;}
    .simpName{width: 30px;height: 30px;float: left;margin: 15px 10px 0 0;line-height: 30px;font-size: 12px;
      .iconfont{font-size: 18px;color: #fff;margin: 6px 0 0 6px;}
    }
    i.info{font-size: 20px;float: right;margin: 21px 0 0 0;line-height: 1;}
    .el-dropdown{height: 20px;line-height: 20px;}
    i.el-icon-arrow-down{font-size: 20px;}
    .allRead{float: right;line-height: 20px;margin: 20px 20px 0 0;}
    .screening{float: right;line-height: 20px;margin: 20px 20px 0 0;font-size: 20px;}
  }
  .el-aside{background: #EBEDF0;
  .nav-header{height: 60px;line-height: 60px;font-size: 18px;color: #4A4A4A;padding: 0 15px;
    i{font-size: 20px;float: right;margin: 20px 1px 0 0;line-height: 1;}
    .el-dropdown{float: right;}
  }
    .item{height: 50px;line-height: 50px;padding: 0 15px;cursor: pointer;margin-top: 2px;position: relative;
      >span{white-space: nowrap;text-overflow: ellipsis;display: inline-block;width: 114px;overflow: hidden;line-height: 1;float: left;margin: 20px 0 0 0px;}
      >span.draft{
        color: #000;line-height: 20px;margin: 8px 0 0 0;
        .draftText{color: #ccc;line-height: 1;margin: 2px 0 0 0;overflow: hidden;height: 20px;white-space: nowrap;text-overflow: ellipsis;
          img{float: none;margin: 0;width: 20px;height: 20px;border-radius: 0;}
        }
      }
      .iconfont{font-size: 30px;color: #409eff;float: left;margin: 10px 10px 0 0;display: inline-block;line-height: 1;}
      img{width: 30px;height: 30px;border-radius: 50%;float: left;margin: 10px 10px 0 0;}
      .simpName{width: 30px; height: 30px;float: left;margin: 10px 10px 0 0;line-height: 30px;font-size: 12px;
        .iconfont{font-size: 18px;color: #fff;margin: 6px 0 0 6px;}
      }
      i{font-size: 16px;float: right;margin: 18px 0 0 0;display: none;}
      .num{display: inline-block;padding: 0 10px;background: #F76967;border-radius: 16px;position: absolute;line-height: 20px;right: 35px;top: -2px;font-size: 12px;color: #FFF;
      width: auto;}
      .top_status{position: absolute;width: 0;height: 0;right: 0;top: 0;border-top: 6px solid #409eff;border-left: 6px solid transparent;}
    }
    .item:hover{background: #D7DCE0;
      i{display: inline-block;}
    }
    .item.active{background: #D7DCE0;
      i{display: inline-block;}
    }
  }
  .el-footer{border-top:1px solid #e7e7e7; padding: 0;overflow: hidden;
    .tool{
      #sendFile{
        display: inline-block;width: 19px;height: 16px;overflow: hidden;margin-right: 20px;float: left;
        input{display: inline-block;float: left;opacity: 0;margin: -16px 0 0 0;}
        i{font-size: 16px;float: left;color: #A0A0AE;}
      }
      margin: 8px 0 8px 15px;
      i{font-size: 16px;margin-right: 20px;color: #A0A0AE;}
      a{font-size: 16px;color: #A0A0AE;}
    }
    textarea{border: none;}
    textarea:focus{box-shadow: none;}
    .exist{height: 115px;padding: 5px 15px;overflow-y: auto;
      .at{display: inline-block;color: #409eff;width: auto;padding-left: 0;border: none;height: 17px;}
      .at:focus{border: none;box-shadow: none;}
      .emoji{width: 30px;}
      img{max-width: 100%;}
    }
  }
  .el-main{
    overflow-y: auto;padding: 10px 20px 20px;
    .item{
      overflow: hidden;margin-top: 20px;
      .time-title{text-align: center;font-size: 16px;color: #A0A0AE;margin-bottom: 20px;position: relative;
        div{display: inline-block;height: 1px;width: calc(50% - 36px);position: absolute;top: 10px;}
        .left{background: -webkit-linear-gradient(right, #fff , #e7e7e7);background: -o-linear-gradient(left, #fff, #e7e7e7);background: -moz-linear-gradient(left, #fff, #e7e7e7); background: linear-gradient(left, #fff , #e7e7e7);left: 0;}
        .right{background: -webkit-linear-gradient(left, #fff , #e7e7e7);background: -o-linear-gradient(right, #fff, #e7e7e7);background: -moz-linear-gradient(right, #fff, #e7e7e7); background: linear-gradient(right, #fff , #e7e7e7);right: 0;}
      }
      .time-title.time1{
        div{width: calc(50% - 36px);}
      }
      .time-title.time2{
        div{width: calc(50% - 44px);}
      }
      .time-title.time3{
        div{width: calc(50% - 76px);}
      }
      .time-title.time4{
        div{width: calc(50% - 102px);}
      }
      .simpName{display: inline-block;width: 36px;height: 36px;line-height: 36px;float: left;}
      img.simpName{background: none;}
      .avatar{width: 36px;height: 36px;float: left;border: 1px solid #e7e7e7;border-radius: 50%;}
      .figureInfor{
        .name{font-size: 14px;margin-left: 10px;}
        .time{font-size: 12px;margin-left: 10px;color: #A0A0AE;}
      }
        .panel{border: 1px solid #E7E7E7;border-radius: 3px;margin-top: 10px;padding: 20px 0 20px 20px;width: 85%;min-width: 570px;
        .unread{float: right;width: 10px;height: 10px;background: #FF6260;margin: -5px 15px 0 0;border-radius: 50%;}
          .figureInfor{
            .time{font-size: 12px;margin: 5px 0 0 8px;color: #A0A0AE;display: inline-block;}
          }
        }
      .content{margin: 12px 50px 0 50px;line-height: 20px;font-size: 16px;color: #647082;
        img{max-width: 200px;}
        .emojiImg{width: 32px;}
        span{display: inline-block;max-width: 100%;word-wrap: break-word;font-size: 14px;color: #4A4A4A;word-break: break-all;}
        .revocation{margin: 0 0 0 -40px;font-size: 14px;color: #409eff;cursor: pointer;}
        .at{color: #409eff;margin-right: 5px;height: 18px;}
        .panel2{border-radius: 3px;margin-top: 10px;width: 100%;
          .panel-header{font-size: 18px;color: #4A4A4A;margin: 3px 0 8px 0;}
          .panel-item{font-size: 14px;color: #797979;line-height: 26px;}
        }
        .isRead{margin-right: 10px;cursor: pointer;}
        a{color: #409eff;text-decoration: 1px solid #409eff;}
        .file_box{display: inline-block;height: 40px;padding-top: 5px;margin-top: 10px;}
        .file{font-size: 47px;float: left;margin: 0 10px 0 0;}
        img.file{width: 50px;height: 50px;margin: -20px 10px 0 0;}
      }
      .content.prompt{text-align: center;color: #A0A0AE;}
      .error{color: red;font-size: 12px;}
    }
    .item.right{text-align: right;padding-right: 10px;
      .simpName{float: right;}
      .avatar{float: right;margin-left: 10px;}
      .content{margin: 5px 45px 0 50px;
        span{text-align: left;}
      }
    }
  }
  .is-vertical{padding: 0 25px;}
}
.el-popper{
  width: 200px;padding: 10px 0;
  .item{height: 40px;line-height: 40px;padding: 0 20px;cursor: pointer;
    i{font-size: 16px;margin: 11px 10px 0 0;float: left;}
  }
  .item:hover{background: #F2F2F2;}

}
.createGroup{
  .el-dialog__body{padding: 30px 20px 0;}
  .el-dialog__footer{text-align: left;padding: 10px 0 30px 94px;}
  .add-member{font-size: 40px;float: left;}
  .simpName{display: inline-block;width: 40px;height: 40px;border-radius: 50%;line-height: 40px;float: left;margin-right: 10px;font-size: 15px;color: #fff;text-align: center;margin-bottom: 10px;}
  img{width: 40px;height: 40px;border-radius: 50%;line-height: 40px;float: left;margin-right: 10px;margin-bottom: 10px;}
  .el-form-item{margin-bottom: 20px;}
  .el-form-item__content{
    span{margin: 0;}
  }
  textarea{width: calc(100% - 30px);border: 1px solid #dcdfe6;}
  .createGroupName{width: calc(100% - 30px);height: 40px;border: 1px solid #dcdfe6;padding: 5px 15px;}
}
.groupChat{
  .el-dialog__body{padding: 20px 20px 30px;}
  .item{font-size: 14px;height: 50px;line-height: 50px;padding: 0 20px;
    img{float: left;margin: 7px 10px 0 0;line-height: 1;width: 36px;}
    .iconfont:first-child{float: left;margin: 7px 10px 0 0;font-size: 36px;line-height: 1;color: #409eff;}
    .iconfont:last-child{float: right;margin: 15px 0 0 0;font-size: 24px;cursor: pointer;line-height: 1;}
    .iconfont:last-child:hover{color: #409eff;}
  }
  .item:hover{background: #f2f2f2;}
}
  .privateChat{
    .el-dialog{min-height: 300px;}
    .el-dialog__body{padding: 15px 30px 20px;}
    header{
      span{font-size: 16px;color: #4a4a4a;margin-right: 25px;}
      span.active{color: #17171A;}
      border-bottom: 1px solid #f2f2f2;margin-bottom: 20px;line-height: 50px;
      .iconfont{float: right;line-height: 1;margin: 23px 0 0 0;}
      .iconfont:hover{color: #409eff;}
    }
    .item{height: 50px;line-height: 50px;padding-left: 20PX;
      .iconfont:first-child{font-size: 40px;float: left;margin: 0 10px 0 0;color: #409eff;}
      .iconfont:last-child{float: right;font-size: 20px;line-height: 1;margin: 16px 15PX 0 0;}
      .iconfont:last-child:hover{color: #409eff;}
    }
    .item:hover{background: #F2F2F2;}
    .el-tree-node__content{height: auto;margin-top: 1px;
      .tree-expand{display: inline-block;width: 100%;}
      .icon-pc-paper-privat{float: right;font-size: 20px;line-height: 1;margin: 16px 15PX 0 0;}
      .type0,.type4{
        .simpName{display: none;}
      }
      .type1{
        height: 50px;line-height: 50px;
        .iconfont{float: right;font-size: 20px;line-height: 1;margin: 16px 15PX 0 0;display: inline-block;}
        .iconfont:hover{color: #409eff;}
        img{height: 40px;width: 40px;border-radius: 50%;float: left;margin: 5px 10px 0 0;}
        .simpName{display: inline-block;height: 40px;line-height: 40px;text-align: center;width: 40px;border-radius: 50%;color: #fff;font-size: 14px;float: left;margin: 5px 10px 0 0;}
      }
    }
  }
.particularsForm{
  .side-body{padding: 10px 20px 20px;overflow-y: auto;}
  .info{margin-bottom: 20px;
    .header{height: 80px;
      img{width: 60px;float: left;margin: 2px 20px 0 0;}
      .name{display: inline-block;margin-top: 18px;font-size: 18px;}
    }
    .notice{font-size: 14px;color: #A0A0AE;}
    .automaton{float: right;background: #FBFBFB;border: 1px solid #E7E7E7;border-radius: 3px;padding: 0 6px;color: #A0A0AE;margin: 18px 0 0 0;}
  }
  .top{border-top: 1px solid #e7e7e7;
    border-bottom: 1px solid #e7e7e7;
    line-height: 58px;
    .el-switch{float: right;margin: 16px 10px 0 0;
      .el-switch__core{width: 48px!important;height: 28px;border-radius: 14px;
        .el-switch__button{width: 24px;height: 24px;}
      }
      .el-switch__core:after{width: 24px;height: 24px;}
    }
    .el-switch.is-checked{
      .el-switch__core::after{margin-left: -25px;}
    }
    .el-checkbox{float: right;margin: 0 10px 0 0;}
  }
  .member{
    .header{line-height: 60px;font-size: 16px;height: 59px;
      .num{font-size: 14px;color: #A0A0AE;}
    }
    .member-box{height: calc(100% - 40px);overflow-y: auto;
      .item{height: 60px;line-height: 60px;
        .avatar-mian{float: left;}
        img{width: 36px;height: 36px;border-radius: 50%;float: left;margin: 10px 0 0 0;}
        .simpName{width: 36px;height: 36px;text-align: center;line-height: 36px;display: inline-block;color: #fff;}
        .name{margin-left: 14px;}
        .remark{font-size: 12px;color: #A0A0AE;}
      }
    }
  }
  .st-item{line-height: 35px;
    span{display: inline-block;}
    .title{width: calc(100% - 110px);padding-left: 5px;}
    .el-checkbox{margin-left: 6px;}
  }
  .st-item.header{line-height: 40px;border-top: 1px solid #e7e7e7;padding-top: 5px;}
}
#atGroupForm{
  .el-dialog{margin: 0;width: 200px;
    .el-dialog__header{display: none;}
    .el-dialog__body{padding: 9px 0 10px;max-height: 180px;overflow-y: auto;
      .item{line-height: 30px;cursor: pointer;padding-left: 15px;margin-top: 1px;display: inline-block;width: 100%;}
      .item:hover{background: #f2f2f2;}
      .item.is-focus{background: #f2f2f2;}
    }
  }
}

.msgReadForm{margin: 0;padding: 4px 0;min-height: 200px;
.el-dialog{margin: 0;}
.el-dialog__header{display: none;}
.el-dialog__body{padding: 30px 0 30px 20px;}
  header{
    div{display: inline-block;width: 50%;text-align: center;line-height: 50px;
      a{color: #69696C;font-size: 16px;}
      a.active{color: #17171A;}
    }
  }
  .item{line-height: 50px;height: 50px;padding-left: 20px;
    .name{
      display: block;
      overflow: hidden;
      word-break: keep-all;
      text-overflow: ellipsis;
      max-width: 80px;
      float: left;
      line-height: 20px;
      height: 20px;
      margin: 15px 0 0 0;
    }
    img{width: 36px;line-height: 36px;height: 36px;border-radius: 50%;float: left;margin: 7px 14px 0 0;}
    .simpName{display: inline-block;width: 36px;line-height: 36px;text-align: center;width: 36px;border-radius: 50%;float: left;margin: 7px 14px 0 0;color: #fff;}
  }
}
  .memoDetails{
    >.el-dialog{margin: 0!important;height: 100%;width: 800px;border-left: 1px solid #ccc;overflow-x: hidden;float: right;
      >.el-dialog__header{display: none;}
      >.el-dialog__body{padding: 0;
        .geo-position{padding: 9px 11px!important;;
          .geo-down{margin-top: 0!important;}
        }
      }
    }
  }
  .emailModel{
    >.el-dialog{margin: 0!important;height: 100%;width: 800px;border-left: 1px solid #ccc;overflow-x: hidden;float: right;
      >.el-dialog__header{display: none;}
      >.el-dialog__body{padding: 0;
      }
    }
  }
}

.popoverSet{padding: 10px 0;width: 220px;
  li{line-height: 40px;padding: 0 20px;cursor: pointer;
    i{float: left;margin: 12px 10px 0 0;line-height: 1;}
  }
 }
</style>
<style>
.cpChat-main .el-container .el-main .item .avatar-mian{float: left;margin-right: 10px;}
.cpChat-main .el-container .el-main .item.right .avatar-mian{float: right;margin-left: 10px;}
</style>
