<template>
  <div class="cpChat-main main">
    <el-container>
      <el-aside width="220px">
        <div class="nav-header">企信
          <i class="iconfont icon-add-chat-2" @click="getMemberDialog"></i>
          <i class="iconfont icon-add-group-chat2" @click="openGroupChatForm"></i>
        </div>
        <el-select class="search" size="small" v-model="listSearch" filterable remote placeholder="搜索" no-match-text="暂无搜索结果～" @focus="initlistSearch" @change="listSearchChange" :remote-method="remoteMethod" :popper-class="listSearchInput ? 'list-search-dialog listSearch':'list-search-dialog'">
          <el-option-group v-for="group in chatSeachList" :key="group.id" :label="group.label" v-if="group.row.length > 0">
            <el-option v-for="(item, index) in group.row" :key="index" :label="item.employee_name || item.name" :value="group.id + '-' + (item.receiver_id || item.id)">
              <div>
                <chat-avatar :avatar="item"></chat-avatar>
                <span class="name">{{item.name}}</span>
              </div>
            </el-option>
          </el-option-group>
        </el-select>
        <div class="chat-list">
          <div class="item" v-for="(chatItem, index) in sortChatList" :key="index" v-if="chatItem.is_hide == 0"  @click="switchChat($event, chatItem)"
          :class="(chatItem.id == chat_object.id && chatItem.chat_type == chat_object.chat_type) ? 'active':''">
            <chat-avatar :avatar="chatItem"></chat-avatar>
            <!-- 群 -->
            <span v-if="chatItem.chat_type == 1" :class="chatItem.draftText ? 'draft':''">{{chatItem.name}}
              <div class="draftText" v-if="chatItem.draftText" v-html="traverseDraftText(chatItem)"></div>
            </span>
            <!-- 成员 -->
            <span v-if="chatItem.chat_type == 2" :class="chatItem.draftText ? 'draft':''">{{chatItem.employee_name}}
              <div class="draftText" v-if="chatItem.draftText" v-html="traverseDraftText(chatItem)"></div>
            </span>
            <!-- 助手 -->
            <span v-if="chatItem.chat_type == 3">{{chatItem.name}}</span>

            <i class="el-icon-close" @click="removeSession(chatItem)" v-if="chatItem.chat_type != 3 && !(chatItem.chat_type == 1 && chatItem.type == 0)"></i>
            <span class="num" v-if="chatItem.unread_nums > 0">{{(chatItem.unread_nums > 99) ? '99+' : chatItem.unread_nums}}</span>
            <div class="top_status" v-if="chatItem.top_status == 1"></div>
          </div>
        </div>
      </el-aside>
      <el-container>
        <el-header height="60px">
          <chat-avatar :avatar="chat_object"></chat-avatar>
          <!-- <span v-if="chat_object.chat_type == 1">{{chat_object.name}}</span> -->
          <el-input v-if="chat_object.chat_type == 1" v-model="groupInfo.name" placeholder="请输入" size="mini" maxlength="12" :readonly="(groupInfo.type == 0 && groupInfo.principal != OneselfIMID) || !isGroupName2" @blur="modifyGroupInfo" @focus="editGroupName(1)"></el-input>
          <span v-if="chat_object.chat_type == 2">{{chat_object.employee_name}}</span>
          <span v-if="chat_object.chat_type == 3">{{chat_object.name}}</span>
          <i class="iconfont icon-pc-paper-admini2 info" @click="openParticularsForm"></i>
          <i class="iconfont icon-pc-filter screening" v-if="chat_object.chat_type == 3 && chat_object.application_id" @click="screening"></i>
          <a class="allRead" href="javascript:;" v-if="chat_object.chat_type == 3 && chat_object.type != 2" @click="markAllRead">全部标记为已读</a>
          <i class="iconfont icon-add-group-member" v-if="chat_object.chat_type == 1 && groupInfo.type == 1 && groupInfo.principal == OneselfIMID" style="float: right;font-size: 27px;margin: 17px 20px 0 0;" @click="getGroupInfo(chat_object.id, 1)"></i>
        </el-header>
        <el-container :class="(chat_object.chat_type == 1) ?'group-container':''">
          <el-container style="padding: 0;">
            <el-main id="content">
              <a href="javascript:;" v-if="chat_msg.length > 0 && chat_object.chat_type != 3 && readMore" @click="loadMore" style="margin-left: calc(50% - 46px);">查看更多消息</a>
              <!-- 聊天内容 -->
              <div class="item" v-if="chat_object.chat_type != 3" v-for="(msg_item, index) in chat_msg" :key="index" v-bind:class="(msg_item.sender == OneselfIMID) ? 'right' : ''">
                <div class="time-title" v-bind:class="'time' + msg_item.timeType" v-if="msg_item.timeShow">
                  <div class="left"></div>{{msg_item.timeTitle}}<div class="right"></div>
                </div>
                <avatar-component v-if="msg_item.msg.type != 7" :employeeData="{'name': msg_item.msg.senderName, 'picture': (msg_item.sender == employeeInfo.sign_id) ? employeeInfo.picture : msg_item.msg.senderAvatar, 'type': 1, 'sign_id': msg_item.sender}"></avatar-component>
                <div class="figureInfor" v-if="msg_item.msg.type != 7 && chat_object.chat_type == 1 && msg_item.sender != OneselfIMID">
                  <span class="name">{{msg_item.msg.senderName}}</span>
                </div>
                <!-- 聊天消息 -->
                <div class="content" v-if="msg_item.msg.type != 7">
                  <span class="time" :class="(chat_object.chat_type == 1 && msg_item.sender != OneselfIMID) ?'group2':''">
                    <span class="name">{{msg_item.msg.senderName}}</span>
                    {{msg_item.clientTimes | formatDate('MM-dd HH:mm')}}
                  </span>
                  <div class="popper_arrow"></div>
                  <span v-if="msg_item.msg.type == 1" v-html="traverseEmoji(msg_item.msg.msg)"></span>
                  <img v-else-if="msg_item.msg.type == 2" :src="msg_item.msg.fileUrl+'&TOKEN='+token+'&id='+msg_item.msg.fileId" @click="openFilePreview(msg_item.msg)">
                  <audio v-else-if="msg_item.msg.type == 3" controls="" preload="auto" :src="msg_item.msg.fileUrl+'&TOKEN='+token+'&id='+msg_item.msg.fileId" width="240" height="30" @play="audioPaly($event)"></audio>
                  <div class="file_box" v-else-if="msg_item.msg.type == 4" @click="openFilePreview(msg_item.msg, $event)">
                    <img class="file" v-if="$root.$options.filters.ressuffix(msg_item.msg.fileName) == 'img'" :src="msg_item.msg.fileUrl+'&TOKEN='+token+'&id='+msg_item.msg.fileId" />
                    <i :class="msg_item.msg.fileName | ressuffix" class="file"></i>
                    <div>
                      <a class="file-name" href="javascript:;">{{msg_item.msg.fileName}}</a>
                      <p>
                        <span class="file-size">{{msg_item.msg.fileSize | fileSize('-')}}</span>
                        <a class="download" target="_blank" :href="msg_item.msg.fileUrl+'&TOKEN='+token+'&id='+msg_item.msg.fileId"><i title="下载" class="iconfont icon-pc-paper-download download"></i></a>
                      </p>
                    </div>
                  </div>
                  <video v-if="msg_item.msg.type == 5" controls="" name="media" :src="msg_item.msg.videoUrl+'&TOKEN='+token+'&id='+msg_item.msg.fileId" type="video/mp4" @play="videoPaly($event)"></video>
                  <span v-if="msg_item.msg.type == 6">深圳市南山区高新南一道21-2号</span>
                  <div v-if="msg_item.warning != 1 && msg_item.msg.type != 7 && msg_item.sender == OneselfIMID" class="im-handle">
                    <span v-if="msg_item.isRead.length == 0 && chat_object.chat_type == 2" class="isRead">未读</span>
                    <span v-if="msg_item.isRead.length > 0 && chat_object.chat_type == 2" class="isRead" style="color: #BBBBC3;">已读</span>
                    <span v-if="chat_object.chat_type == 1 && chat_object.peoples.split(',').length > msg_item.isRead.length + 1" class="isRead" @click="readingUnread($event, msg_item, 0)">{{msg_item.isRead.length}}人已读</span>
                    <span v-if="msg_item.isRead.length > 0 && chat_object.chat_type == 1 && chat_object.peoples.split(',').length == msg_item.isRead.length + 1" class="isRead" style="color: #BBBBC3;">全部已读</span>
                    <span v-if="localTime-msg_item.clientTimes < 120000 && msg_item.sender == OneselfIMID && localTime-msg_item.clientTimes >= 0" class="revocation" @click="revocationChat(msg_item)">撤回</span>
                  </div>
                </div>
                <div class="prompt" v-if="msg_item.msg.type == 7">
                  <span>{{msg_item.msg.msg}}</span>
                </div>
              </div>
              <!-- 助手消息列表 -->
              <div class="item apply-item" v-if="chat_object.chat_type == 3" v-for="(as_item, index) in assistantMessageData" :key="index">
                <div class="time-title" v-bind:class="'time' + as_item.timeType" v-if="as_item.timeShow">
                  <div class="left"></div>{{as_item.timeTitle}}<div class="right"></div>
                </div>
                <header>
                  <div class="unread" v-if="as_item.read_status == 0"></div>
                  <div class="simpName" v-if="chat_object.type == 1" :style='{background:as_item.icon_color}'>
                    <img v-if="as_item.icon_type == 1" :src="as_item.icon_url+'&TOKEN='+token">
                    <i v-if="as_item.icon_type != 1" class="iconfont" :class="as_item.icon_url"></i>
                  </div>
                  <div class="simpName" v-if="chat_object.type != 1" style="background: none;">
                    <img class="simpName" :src="'/static/img/im/' + chat_object.type + '.png'" />
                  </div>
                </header>
                <div class="module">
                  <span class="name">{{as_item.bean_name_chinese}}</span>
                  <span class="time">{{as_item.datetime_create_time | formatDate('HH:mm')}}</span>
                </div>
                <div class="panel" @click="readMessage(as_item);getModuleDetail(as_item)">
                  <span class="arrow-left"></span>
                  <span class="panel-content" :class="(as_item.type == 2) ? 'at':''">{{as_item.push_content}}</span>
                  <div class="panel-field" v-for="(field2, index2) in as_item.field_info" :key="index2">
                    <span v-html="fieldHtml(field2)"></span>
                  </div>
                </div>
              </div>
              <a href="javascript:;" v-if="more && chat_object.chat_type == 3" @click="loadMore" style="margin-left: calc(50% - 46px);display: inline-block;margin-top: 10px;">查看更多消息</a>
            </el-main>
            <el-footer v-show="chat_object.chat_type != 3" height="210px">
              <div class="tool">
                <form id="sendFile">
                  <i class="iconfont icon-pc-paper-upload"></i>
                  <input type="file" @change="handleFile" name="fileList" />
                </form>
              <i class="iconfont icon-pc-paper-face" title="表情" @click="openEmojiForm($event)"></i>
              <i class="iconfont icon-pc-paper-2" title="@" v-if="chat_object.chat_type == 1" @click="openCreateGroupFormForm($event)" style="margin-left: -4px;"></i>
              </div>
              <!-- <el-input type="textarea" :rows="5" placeholder="按Enter发送消息" @keyup.enter.native="sendMessage($event)" resize='none'></el-input> -->
              <div id="messageBox2" class="exist" contenteditable="true" @keydown="sendMessage2($event)" placeholder="按Enter发送消息" v-on:mouseleave="saveRange" @pause="pastePicture($event)"></div>
            </el-footer>
          </el-container>
          <!-- 群详情 -->
          <el-aside width="160px" v-if="chat_object.chat_type == 1" class="group-info">
            <div class="notice" v-if="groupInfo.notice">
              <div class="header">群公告</div>
              <div class="notice-content">{{groupInfo.notice}}</div>
            </div>
            <div class="notice" v-if="!groupInfo.notice">
              <img :src="'/static/img/im/group-notice.png'" />
              <div class="notice-none">暂无群公告</div>
            </div>
            <div class="member">
              <div class="header">群成员·{{groupEmployeeInfo.length}}</div>
              <div class="group-member">
                <div class="member-item" v-for="(item, index) in groupEmployeeInfo" :key="index">
                  <avatar-component :employeeData="{'id': item.id, 'name': item.employee_name, 'picture': item.picture, 'type': 1, 'sign_id': item.sign_id, 'is_name': true}"></avatar-component>
                  <!-- <span class="name">{{item.employee_name}}</span> -->
                  <el-tooltip v-if="groupInfo.principal == item.sign_id" effect="dark" content="管理员" placement="top">
                    <i class="iconfont icon-group-manage"></i>
                  </el-tooltip>
                </div>
              </div>
            </div>
          </el-aside>
        </el-container>
      </el-container>
    </el-container>
    <!-- 弹出层 -->
    <div style="height: 0">
      <div id="textMenu" style="width:0;height: 0;"></div>
      <el-dialog title='发起群聊' :visible.sync='groupChatForm' class='addForm groupChat' :modal='false'>
        <div class="item" v-for="(groupItem, index) in groupData" :key="index" @click="addGroupChat(groupItem)">
          <img src="/static/img/im/company.png" v-if="groupItem.chat_type == 1 && groupItem.type == 0" />
          <img src="/static/img/im/group.png" v-if="groupItem.chat_type == 1 && groupItem.type == 1" />
          <span class="name">{{groupItem.name}}</span>
          <i class="iconfont icon-pc-paper-privat"></i></div>
      </el-dialog>

      <!-- 成员、群详情（设置 -->
      <div class="side-shade" v-if="particularsForm"></div>
      <transition name="slide">
        <div class="particularsForm sideForm" v-if="particularsForm">
          <header>
            <span v-if="chat_object.chat_type == 1">群设置</span>
            <span v-if="chat_object.chat_type == 2">会话设置</span>
            <i class="el-icon-close" @click="particularsForm = false"></i>
          </header>
          <div class="side-body" :class="(chat_object.chat_type == 2) ? 'session':''">
            <div class="info" v-if="chat_object.chat_type == 1">
              <div class="header">
                <img src="/static/img/im/company.png" v-if="groupInfo.type == 0" />
                <img src="/static/img/im/group.png" v-if="groupInfo.type == 1" />
                <el-input v-model="groupInfo.name" placeholder="请输入" size="mini" :readonly="(groupInfo.type == 0 && groupInfo.principal != OneselfIMID) || !isGroupName" id="editGroupName" @blur="modifyGroupInfo"></el-input>
                <i class="iconfont icon-pc-paper-name" @click="editGroupName" v-if="groupInfo.type == 1 && groupInfo.principal == OneselfIMID"></i>
                <div class="total">群聊全员·{{groupEmployeeInfo.length}}人</div>
              </div>
              <div class="notice-header">群公告</div>
              <el-input class="notice" type="textarea" v-model="groupInfo.notice" size="mini" :rows="3" :readonly="groupInfo.type == 0 || groupInfo.principal != OneselfIMID" resize="none" @blur="modifyGroupInfo"></el-input>
            </div>
            <div class="info" v-if="chat_object.chat_type == 2">
              <div class="header">
                <chat-avatar :avatar="chat_object"></chat-avatar>
                <span class="name">{{chat_object.name}}</span>
              </div>
              <div class="notice">{{chat_object.notice}}</div>
            </div>
            <div class="top" v-if="chat_object.chat_type == 1 && groupInfo.type == 1 && groupInfo.principal == OneselfIMID" style="cursor: pointer;" @click="groupCommand(5)">
              转让群聊<i class="iconfont icon-xuanrenkongjian-icon5"></i>
            </div>
            <div class="top" v-if="chat_object.chat_type == 1">置顶聊天
              <el-switch v-model="infoChecked" active-color="#1890FF" inactive-color="#d8dce5" @change="setTop" style="width: 46px;height: 28px;"></el-switch>
            </div>
            <div class="top" v-if="chat_object.chat_type == 2">置顶会话
              <el-switch v-model="infoChecked" active-color="#1890FF" inactive-color="#d8dce5" @change="setTop" style="width: 46px;height: 28px;"></el-switch>
            </div>
            <el-button type="danger" v-if="chat_object.chat_type == 1 && groupInfo.type == 1 && groupInfo.principal == OneselfIMID" @click="groupCommand(3)">解散群聊</el-button>
            <el-button type="danger" v-if="chat_object.chat_type == 1 && groupInfo.type == 1 && groupInfo.principal != OneselfIMID" @click="groupCommand(4)">退出群聊</el-button>
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
      <el-dialog title="退出群聊" :visible.sync="quitGroupForm" class='prompt-dialog2' modal>
        <span class="content">退出后，你将不再接受该群组中的消息，请谨慎操作。你确定要退出群聊吗？</span>
        <span slot="footer" class="dialog-footer">
            <el-button @click="quitGroupForm = false">取 消</el-button>
            <el-button type="primary" @click="quitGroup">确 定</el-button>
        </span>
      </el-dialog>
      <el-dialog title="解散群聊" :visible.sync="releaseGroupForm" class='prompt-dialog2' modal>
        <span class="prompt">提示：解散后，该群聊不可恢复，群聊中的成员将收到解散消息，请谨慎操作。</span>
        <span class="remark">你确定要解散群聊吗？</span>
        <span slot="footer" class="dialog-footer">
            <el-button @click="releaseGroupForm = false">取 消</el-button>
            <el-button type="primary" @click="releaseGroup">确 定</el-button>
        </span>
      </el-dialog>
      <el-dialog title="@群成员列表" :visible.sync="atGroupForm" id='atGroupForm' :modal='false' top="0">
        <!-- <el-input v-model="seachGroupMember" placeholder="请输入内容" prefix-icon="el-icon-search" size="small"></el-input> -->
        <div class="group-member">
          <a href="javascript:;" class="item" v-for="(item, index) in atlist" :key="index" @click="atCheck(item)" v-show="item.employee_name.includes(seachGroupMember)">
            {{item.employee_name}}<span v-if="item.id === '0'">({{atlist.length}}人)</span>
          </a>
        </div>
      </el-dialog>
      <el-dialog title="选择成员" :visible.sync="transferGroupDialog" class="transferGroupDialog">
        <el-input v-model="seachGroupMember" placeholder="请输入内容" prefix-icon="el-icon-search" size="small"></el-input>
        <header>{{chat_object.name}}</header>
        <div class="group-member">
          <div class="member-item" v-for="(item, index) in groupEmployeeInfo" :key="index" @click="transferId = item.sign_id" v-show="item.employee_name.includes(seachGroupMember)" v-if="item.sign_id != OneselfIMID">
            <img v-if="item.picture" :src="item.picture+'&TOKEN='+token" />
            <div class="simpName" v-if="!item.picture">{{item.employee_name | limitTo(-2)}}</div>
            <span class="name">{{item.employee_name}}</span>
            <i class="iconfont " :class="(transferId == item.sign_id) ? 'icon-pc-member-sign-ok':'icon-pc-member-sign'"></i>
          </div>
        </div>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click="transferGroup">确 定</el-button>
          <el-button @click='transferGroupDialog = false'>取 消</el-button>
        </div>
      </el-dialog>
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
            <div class="info" style="padding-bottom: 45px;border-bottom: none;">
              <div class="header">
                <chat-avatar :avatar="chat_object"></chat-avatar>
                <span class="name">{{assisstantInfo.name}}</span>
                <span class="automaton">机器人</span>
              </div>
              <!-- <div class="notice">汇聚所有{{assisstantInfo.name}}信息</div> -->
            </div>
            <div class="top" style="border-bottom: none;" v-if="assisstantInfo.type != 2">只查看未读消息
              <el-switch v-model="assisstantInfo.show_type" active-color="#409eff" inactive-color="#d8dce5" @change="markReadOption" style="width: 46px;height: 28px;"></el-switch>
            </div>
            <div class="top" style="border-bottom: 1px solid #e7e7e7;border-top: none;">置顶聊天
              <el-switch v-model="assisstantInfo.infoChecked" active-color="#409eff" inactive-color="#d8dce5" @change="setTop" style="width: 46px;height: 28px;"></el-switch>
            </div>
          </div>
        </div>
      </transition>

      <el-dialog title='已读未读' :visible.sync='isReadForm' class='msgReadForm' :modal='false' width="336px" top="25vh">
        <header>消息详情</header>
        <i class="dialog-close iconfont icon-pc-paper-cancel" @click="isReadForm = false"></i>
        <div class="header" @click="msgReadTypessss()">
          <div style="float: left;"><a href="javascript:;" @click="msgReadType = 1" v-bind:class='msgReadType == 1 ? "active":""'>已读（{{msgReadNum}}）</a></div>
          <div><a href="javascript:;" @click="msgReadType = 0" v-bind:class='msgReadType == 0 ? "active":""'>未读（{{msgUnreadNum}}）</a></div>
        </div>
        <div class="employee-box">
          <div class="item" v-for="item in readEmployeeList" :key="item.id" v-if="msgReadType == item.readType && item.sign_id != OneselfIMID">
            <img v-if="item.picture" :src="item.picture+'&TOKEN='+token" />
            <div class="simpName" v-if="!item.picture">{{item.employee_name | limitTo(-2)}}</div>
            <span class="name" :title="item.employee_name">{{item.employee_name}}</span>
            <!-- <span v-if="item.principal">（管理员）</span> -->
          </div>
        </div>
      </el-dialog>

      <!-- 小助手详情 -->
      <external-detail :externalDatas="externalData"></external-detail>
    </div>
  </div>
</template>

<script>
import tool from '@/common/js/tool.js'
import AvatarComponent from '@/common/components/employee-avatar.1'/** 显示成员信息的头像组件 */
import chatAvatar from '@/frontend/companyLetter/chat-avatar'/** 显示成员信息的头像组件 */
import Portrait from '@/common/components/portrait'
import {plist, emojiUrl, traverseEmoji, locationEmoji} from '@/common/js/emoji.js'
import chatjs from '@/common/js/chat.2.js'
import {HTTPServer} from '@/common/js/ajax.js'
import ExternalDetail from '@/frontend/companyLetter/external-detail'/** 显示成员信息的头像组件 */
import $ from 'jquery'
// const $ = window.jQuery
export default {
  name: 'IM',
  components: {Portrait, AvatarComponent, chatAvatar, ExternalDetail},
  data () {
    return {
      token: '',
      localfun: undefined,
      msgFile: {},
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
      listSearch: '',
      listSearchInput: '',
      chatList: [],
      atlist: [],
      isfocus: 0,
      chatSeachList: [],
      assistantMessageData: [],
      currentPage: 1,
      formLabelWidth: '74px',
      more: false,
      readMore: true,
      visible2: false,
      groupChatForm: false,
      particularsForm: false,
      isGroupName: false,
      isGroupName2: false,
      editGroupForm: false,
      quitGroupForm: false,
      releaseGroupForm: false,
      atGroupForm: false,
      transferGroupDialog: false,
      seachGroupMember: '',
      transferId: '',
      screeningForm: false,
      screenData: [],
      settingForm: false,
      assisstantInfo: {},
      chat_object: {},
      groupInfo: {},
      groupEmployeeInfo: {},
      infoChecked: false,
      isReadForm: false,
      readEmployeeList: [],
      msgUnreadNum: 0,
      msgReadNum: 0,
      msgReadType: 0,
      treeData: [],
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
      externalData: {}
    }
  },
  /* 页面加载后执行 */
  mounted () {
    var userInfo = JSON.parse(sessionStorage.userInfo)
    this.token = JSON.parse(sessionStorage.requestHeader).TOKEN
    this.companyId = userInfo.companyInfo.id
    this.employeeInfo = userInfo.employeeInfo
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
      /** 获取外部链接过来的聊天对象 */
      var singId = this.$route.params.sing_id
      if (singId) {
        var sing = tool.contains(this.chatList, 'receiver_id', singId)
        setTimeout(() => {
          chatjs.addSingleChat({'sign_id': sing.receiver_id})
        }, 200)
      } else {
        this.updateChatObject(this.chatList)
      }
    }
    setTimeout(() => { // @组件
      $.fn.atwho.debug = true
      console.log($.fn)
    }, 200)
    this.localfun = setInterval(() => {
      this.localTime += 1000
    }, 1000)
    // this.localfun()
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
    this.$bus.off('transmit')
    this.$bus.on('transmit', (data) => {
      console.log('Chat监听', data)
      let tag1 = data.tag1
      let response = data.data
      switch (data.type) {
        case 1 :// 登录
          this.ServerTimes = response.ServerTimes
          this.timeInterval = response.timeInterval
          this.historyMsg()
          break
        case 5 :// 对聊
          this.readStorage(response)
          break
        case 6 :// 群聊
          this.readStorage(response)
          break
        case 7: // 推送的信息
          // this.readStorage()
          var msg = response.msg
          if (this.chat_object.id === msg.assistant_id) {
            if (msg.type === 16 || msg.type === 17) {
              this.setAssistantMessage(msg)
            } else if (msg.type === 20) {
              /** 修改助手推送 */
              this.setAssistantMessage(msg)
            } else {
              this.getAssistantMessage({'id': msg.assistant_id, 'beanName': msg.bean_name || ''})
            }
          }
          break
        case 8: // 群推送消息
          if (tag1 === '1003') {
            this.setAssistantMessage(response)
          } else {
            var groupInfo = response.groupInfo
            var contains = tool.contains(this.chatList, 'id', groupInfo, 'id')
            if (contains) {
              this.chatList[contains.i] = groupInfo
              this.$set(this.chatList, contains.i, groupInfo)
              if (this.chat_object.id === groupInfo.id) {
                this.chat_object = groupInfo
                this.groupEmployeeInfo = response.employeeInfo
                this.groupInfo = groupInfo
                this.infoChecked = (this.groupInfo.top_status === '1')
              }
            }
          }
          break
        case 11: // 单用户消息
          this.readStorage(response)
          break
        case 12: // 单用户消息
          this.readStorage(response)
          break
        case 18 :// 个人消息已读
          this.changeGroupState(response)
          break
        case 19 :// 群消息已读
          this.changeGroupState(response)
          break
        case 24: // 撤销个人聊天
          if (this.chat_object.receiver_id === response.senderID) {
            this.modifyRevocationReply(24, response)
          }
          break
        case 25: // 撤销个人聊天成功 <----服务器返回的成功命令
          if (this.chat_object.receiver_id === response.receiverID) {
            this.modifyRevocationReply(25, response)
          }
          break
        case 26: // 撤销群聊天成功
          if (this.chat_object.id === response.receiverID) {
            this.modifyRevocationReply(26, response)
          }
          break
        case 27: // 撤销群聊天成功  <---服务器返回的成功命令
          if (this.chat_object.id === response.receiverID) {
            this.modifyRevocationReply(27, response)
          }
          break
        case 'history': // 获取历史消息完成后执行
          if ((response === this.chat_object.id && tag1 === 2) || (response === this.chat_object.receiver_id && tag1 === 1)) {
            console.log('chat_msg', this.chat_msg)
            this.readStorage(null, this.chat_msg.length)
          }
          break
        case 'chat_object': // 设置当前聊天对象
          this.chatList = response
          if (this.chat_object.receiver_id !== tag1.receiver_id || !this.chat_object.receiver_id) {
            this.chat_object = Object.assign({}, this.chat_object, tag1)
            this.switchChat(null, tag1)
          }
          break
        case 'chatList': // 获取回话列表
          this.chatList = response
          if (!tag1) {
            if (this.chat_object.is_hide === '1') this.chat_object = {}
            this.updateChatObject(response)
          } else if (tag1) {
            if (tag1.type === 1002 && this.chat_object.id === tag1.assistant_id) {
              this.chat_object.icon_url = tag1.icon_url
              this.chat_object.icon_type = tag1.icon_type
              this.chat_object.icon_color = tag1.icon_color
              this.chat_object.name = tag1.application_name
            } else if (tag1.type === 1004 && this.chat_object.id === tag1.assistant_id) {
              this.chat_object = {}
              this.updateChatObject(response)
            } else if (tag1.type === 1) {
              if (this.chat_object.id === tag1.group_id) {
                this.chat_object = {}
                this.updateChatObject(response)
              } else if (!this.chat_object.application_id && this.chat_object.type === 2) {
                this.markAllRead()
              }
            }
          }
          break
        case 'unread_nums': // 获取回话列表
          if (this.chat_object.id !== response.msg.chatId) {
            chatjs.statisticalChatList(2, response.msg.chatId)
          }
          break
      }
    })
  },
  watch: {
    prepareData (data) {
      if (this.changeGroupMemberObj.prepareKey === 'edit-group-member' && !this.changeGroupMemberObj.seleteForm) {
        var arr = []
        var sendData = []
        for (var i = 0; i < this.prepareData.length; i++) {
          if (this.prepareData[i].sign_id !== this.OneselfIMID) {
            arr.push(this.prepareData[i].sign_id)
          }
          sendData.push(this.prepareData[i])
        }
        this.changeGroupMember({'id': this.chat_object.id, 'peoples': arr.toString()}, sendData)
      } else if (this.changeGroupMemberObj.prepareKey === 'add-single-or-group' && !this.changeGroupMemberObj.seleteForm) {
        console.log('添加单聊、群聊', this.changeGroupMemberObj)
        var member = this.changeGroupMemberObj.prepareData
        if (member.length === 1) {
          chatjs.addSingleChat({'sign_id': member[0].sign_id})
        } else if (member.length > 1) {
          this.createGroup()
        }
      }
    }
  },
  created () {
    /** 处理粘贴图片 */
    setTimeout(() => {
      let that = this
      document.getElementById('messageBox2').addEventListener('paste', function (e) {
        let strObj = false
        var pasteInit = function (data) {
          var blob = data.getAsFile()
          var reader = new FileReader()
          var browser = tool.browserInfo()
          reader.readAsDataURL(blob)
          reader.onload = function (ev) {
            var img = new Image()
            img.src = ev.target.result
            if (browser.chrome || browser.safari) {
              that._insertimg('<img data-src="paste" src="' + ev.target.result + '" />', 'messageBox2')
            }
          }
        }
        var items = e.clipboardData.items
        if (items.length > 0) {
          if (items[0].type === 'text/plain') {
            strObj = true
          } else if (items[0].kind === 'file') {
            strObj = true
            pasteInit(items[0])
          }
        }
        for (var i = 0, len = e.clipboardData.items.length; i < len; i++) {
          var item = e.clipboardData.items[i]
          if (item.kind === 'string') {
            item.getAsString(function (str) {
              // console.log('string', str)
            })
          } else if (item.kind === 'file') {
            // console.log('file', item.getAsFile())
            if (!strObj) pasteInit(item)
          } else {
            // console.log('其他', item)
          }
        }
      })
      // document.getElementById('messageBox2').addEventListener('paste', function (e) {
      //   var cbd = e.clipboardData
      //   var ua = window.navigator.userAgent
      //   // 如果是 Safari 直接 return
      //   if (!(e.clipboardData && e.clipboardData.items)) {
      //     return
      //   }
      //   // Mac平台下Chrome49版本以下 复制Finder中的文件的Bug Hack掉
      //   if (cbd.items && cbd.items.length === 2 && cbd.items[0].kind === 'string' && cbd.items[1].kind === 'file' &&
      //     cbd.types && cbd.types.length === 2 && cbd.types[0] === 'text/plain' && cbd.types[1] === 'Files' &&
      //     ua.match(/Macintosh/i) && Number(ua.match(/Chrome\/(\d{2})/i)[1]) < 49) {
      //     return
      //   }
      //   for (var i = 0; i < cbd.items.length; i++) {
      //     var item = cbd.items[i]
      //     if (item.kind === 'file') {
      //       var blob = item.getAsFile()
      //       if (blob.size === 0) {
      //         return
      //       }
      //       // blob 就是从剪切板获得的文件 可以进行上传或其他操作
      //       tool.pastePicture(e)
      //     }
      //   }
      // }, false)
    }, 800)
  },
  computed: {
    sortChatList () {
      return this.sortKey(this.chatList)
      // return this.sortKey(this.distinct(this.chatList))
    }
  },
  methods: {
    initAtData () {
      var names = $.map(this.atlist, (item, i) => {
        if (item.sing_id !== this.OneselfIMID) {
          return {'id': item.sign_id, 'name': item.employee_name, 'people': ''}
        }
      })
      names.unshift({'id': '0', 'name': '所有人', 'people': '(' + names.length + '人)'})
      /* eslint-disable */
      setTimeout(() => {
        var atConfig = {
          at: '@',
          data: names,
          headerTpl: '<div class="atwho-header">Member List<small>↑&nbsp;↓&nbsp;</small></div>',
          insertTpl: '<span data-id="${id}" contenteditable="false">${name}</span>',
          displayTpl: '<li>${name} ${people}</li>',
          limit: 200,
          startWithSpace: false, // 是否已空格开始
          lookUpOnClick: false
        }
        $('#messageBox2').atwho(atConfig)
      }, 200)
    },
    /** 去重 */
    distinct (array) {
      var id = []
      var arr = []
      array.map((item) => {
        if (id.indexOf(item.id) < 0) {
          id.push(item.id)
          arr.push(item)
        }
      })
      return arr
    },
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
      params = params.replace(reg, (item) => {
        return ('<a target="_blank" href="' + item + '">' + item + '</a>')
      })
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
    updateChatObject () {
      if (!this.chat_object.id) {
        for (var i = 0; i < this.chatList.length; i++) {
          if (this.chatList[i].is_hide === '0') {
            this.chat_object = this.chatList[i]
            break
          }
        }
        this.runCurrentObj()
      }
    },
    /** 运行当前对象 */
    runCurrentObj () {
      this.chatId = this.chat_object.id
      this.currentPage = 1
      if (this.chat_object.chat_type === 3) {
        this.findModuleList()
        if (!this.chat_object.application_id && this.chat_object.type === 2) {
          if (this.chat_object.unread_nums) {
            this.markAllRead()
          }
        }
      } else {
        chatjs.emptyChatListNum(this.chat_object.chat_type, this.chat_object.id)
        if (this.chat_object.chat_type === 1) {
          this.getGroupInfo(this.chat_object.id)
        }
        var msgId = this.companyId + '-' + this.OneselfIMID + '-' + this.chat_object.id
        var IMessage = JSON.parse(localStorage.getItem(msgId))
        if (IMessage) {
          this.readStorage()
        } else {
          this.historyMsg()
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
    historyMsg (type) {
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
        hisoryBlob.setUint16(17, 40, true) // 获取记录条数
        let historyMsg = new Blob([senddata, hisoryBlob])
        chatjs.send(historyMsg)
      }
    },
    /** 发送历史消息命令 end this.readMore = false */
    historyEnd () {
      if (this.chat_object.chat_type !== 1 && this.chat_object.chat_type !== 2) {
        return
      }
      if (this.chat_msg.length > 0) {
        var endmsg = this.chat_msg[0]
        console.log('最后一条', endmsg)
        this.usCmdID = 20// 离线命令
        let senddata = this.init_packet()
        let historyBuffer = new ArrayBuffer(19)
        let hisoryBlob = new DataView(historyBuffer)
        hisoryBlob.setUint8(0, (this.chat_object.chat_type === 2) ? 1 : 2, true)
        this.translate((this.chat_object.chat_type === 2) ? this.chat_object.receiver_id : this.chat_object.id, 'setId', hisoryBlob)
        this.translate(endmsg.clientTimes, 'setTimeStamp', hisoryBlob)
        hisoryBlob.setUint16(17, 20, true) // 获取记录条数
        let historyMsg = new Blob([senddata, hisoryBlob])
        chatjs.send(historyMsg)
      }
    },
    /** 获取模块详情 */
    getModuleDetail (data) {
      if (data.bean_name === 'file_library') {
        this.getFuncAuthWithCommunal({'bean': data.bean_name, 'dataId': data.data_id, 'style': data.style}, data)
      } else if (data.bean_name === 'memo') {
        this.getFuncAuthWithCommunal({'bean': data.bean_name, 'style': 100, 'dataId': data.data_id}, data)
      } else if (data.bean_name === 'email') {
        var jsondata = {'id': data.data_id, 'type': 1}
        HTTPServer.getEmailListParticulars(jsondata, 'Loading').then((res) => {
          this.externalData = {'emailModel': true, 'bean_name': 'email', 'res': res}
        })
      } else if (data.type === 26) {
        this.externalData = {'gotoDetailsData': JSON.parse(data.param_fields), type: 26}
      } else if (data.type === 3) {
        this.getFuncAuthWithCommunal({'bean': data.bean_name, 'dataId': data.data_id}, data)
      } else if (data.type === 4) {
        var paramFields = JSON.parse(data.param_fields)
        HTTPServer.queryApprovalData({'dataId': paramFields.dataId, 'type': paramFields.fromType, 'taskKey': paramFields.taskKey, 'moduleBean': paramFields.moduleBean, 'processInstanceId': paramFields.processInstanceId}, 'loading').then((res) => {
          this.externalData = {'type': 4, 'bean_name': 'approval', 'res': res, 'data': data}
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
          this.externalData = {'bean_name': 'file_library', 'fileForm': true, 'fileData': {'id': data.data_id}, 'navObject': {'id': data.style}}
        } else if (data.bean_name === 'memo') {
          this.externalData = {'bean_name': 'memo', 'memoDetails': true, 'menoDataId': data.data_id}
        } else if (data.type === 3) {
          this.externalData = {'type': 3, 'openDetail': true, 'dataId': data.data_id, 'bean': data.bean_name, 'moduleName': data.bean_name_chinese, 'auth': []}
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
      this.seachGroupMember = ''
      if (this.chat_object.chat_type === 1) {
        event.returnValue = false
        this.atlist = [{'id': '0', 'employee_name': '所有人'}]
        this.groupEmployeeInfo.map((item, index) => {
          if (item.sign_id !== this.OneselfIMID) {
            this.atlist.push(item)
          }
        })
        this.atGroupForm = true
        let atGroupForm = document.getElementById('atGroupForm')
        setTimeout(() => {
          var rect = this.getSelectionCoords()
          console.log(rect)
          var left = (rect.x < 315) ? (rect.x + 25) : (rect.x + 10)
          var atBody = atGroupForm.childNodes[0]
          atBody.style.bottom = (document.body.clientHeight - rect.y) + 'px'
          atBody.style.left = left + 'px'
          atGroupForm.getElementsByClassName('group-member')[0].scrollTop = 0
          atGroupForm.getElementsByClassName('el-input__inner')[0].focus()
        }, 100)
      }
    },
    /** 发送消息 */
    sendMessage2 (event) {
      var messageBox = document.getElementById('messageBox2').cloneNode(true)
      if (event.keyCode === 13) {
        if (document.getElementById('atwho-ground-messageBox2')) {
          if (document.getElementById('atwho-ground-messageBox2').childNodes[0].style.display === 'block') {
            return
          }
        }
        event.preventDefault()
        var html = ''
        var atArr = []
        /** 遍历@ */
        if (this.chat_object.chat_type === 1) {
          var atNode = messageBox.getElementsByClassName('atwho-inserted')
          for (var i = 0; i < atNode.length; i++) {
            var atId = atNode[i].childNodes[0].getAttribute('data-id')
            if (atId !== '0') atId = parseInt(atId)
            atArr.push({id: atId, name: atNode[i].innerText})
            atNode[i].childNodes[0].innerText = '@' + atNode[i].innerText
          }
        }
        var img = messageBox.getElementsByTagName('img')
        var bascArr = []
        for (var EM = 0; EM < img.length; EM++) {
          if (img[EM].getAttribute('data-src') === 'paste') {
            bascArr.push(img[EM].getAttribute('src').split(',')[1])
          }
        }
        html = messageBox.innerHTML.replace(/<img[^>]+>/g, (item) => {
          let tit = ''
          item.replace(/\[(.+?)\]/g, (title) => {
            tit = title
            return tit
          })
          return tit
        })
        html = this.removeHTMLTag(html)
        if (!html && atArr.length === 0 && bascArr.length === 0) {
          this.$message.error('不能发送空白消息！')
          return
        }
        event.target.innerHTML = ''
        /** 发送截图 */
        bascArr.map((item) => {
          this.pasteFile(tool.basc64ToFile(item))
        })
        /** 以防截图冲突 */
        if (!html && atArr.length === 0) {
          return
        }
        this.clientTimes = (new Date()).getTime() + this.timeInterval
        this.RAND = this.RAND + 1
        var jsondata = {'msg': html, 'type': 1, 'times': this.clientTimes, 'clientTimes': this.clientTimes, 'chatId': this.chatId, 'senderAvatar': this.employeeInfo.picture, 'senderName': this.employeeInfo.name, 'atList': atArr}
        if (!atArr) {
          delete jsondata.atList
        }
        this.ws_sendmessage(jsondata)
      }
    },
    saveRange () {
      if (this.atGroupForm) {
        return
      }
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
      var selection
      var range
      if (!this._range) {
        document.getElementById('messageBox2').focus()
        selection = window.getSelection ? window.getSelection() : document.selection
        range = selection.createRange ? selection.createRange() : selection.getRangeAt(0)
        this._range = range
      }
      if (!window.getSelection) {
        document.getElementById('messageBox2').focus()
        selection = window.getSelection ? window.getSelection() : document.selection
        range = selection.createRange ? selection.createRange() : selection.getRangeAt(0)
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
      }
      var reg = /@<input[^<>]+>/g
      var dom = document.getElementById('messageBox2').innerHTML
      var match = dom.match(reg)
      if (match) {
        if (match.length > 0) {
          setTimeout(() => {
            document.getElementById('messageBox2').innerHTML = dom.replace(reg, (html) => {
              return html.substring(1, html.length)
            })
          }, 20)
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
        var empData = res.employeeInfo
        var principal = res.groupInfo.principal
        this.msgReadType = 1
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
        var arr = chatjs.readStorage(this.chat_object.id)
        if (this.chat_msg.length === arr.length) {
          // 获取老版历史记录
          this.historyEnd()
        } else {
          arr.map((item, index) => {
            var timeText = this.getTimeText(item.clientTimes)
            if (timeText.text) {
              item.timeShow = timeText.show
              item.timeTitle = timeText.text
              item.timeType = timeText.type
            }
          })
          if (arr.length > 0) arr[0].timeShow = true
          var len = 0
          if (arr.length - this.currentPage * 20 <= 20) {
            this.currentPage++
            len = 0
          } else if (arr.length > this.currentPage * 20) {
            this.currentPage++
            len = arr.length - this.currentPage * 20
          }
          this.chat_msg = arr.slice(len, arr.length)
          var content = document.getElementById('content')
          setTimeout(function () {
            if (content) {
              content.scrollTop = content.scrollHeight - this.scrollTop
              this.scrollTop = content.scrollHeight
            }
          }, 100)
        }
      }
    },
    /** 输出 @ */
    atCheck (data) {
      if (data.sign_id === this.OneselfIMID) {
        return
      }
      document.getElementById('messageBox2').focus()
      // var width = this.calcStringPixelsCount('@' + data.employee_name, 14)
      // this._insertimg('<input class="at" data-id="' + data.id + '" readonly="readonly" style="width: ' + width + 'px" value="@' + data.employee_name + '" />&nbsp;')
      // <span class="atwho-inserted" data-atwho-at-query="@" contenteditable="false"><span data-id="10101" contenteditable="false">胡蓉蓉</span></span>&nbsp;
      this._insertimg('<span class="atwho-inserted" data-atwho-at-query="@" contenteditable="false"><span data-id="' + data.id + '" contenteditable="false">' + data.employee_name + '</span></span>&nbsp;')
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
    readStorage (data, length) {
      chatjs.emptyChatListNum(this.chat_object.chat_type, this.chat_object.id)
      this.ServerTimes = ((new Date()).getTime()) + this.timeInterval
      var timeText = ''
      var bool = !!data
      if (data) {
        bool = (this.chat_object.id === data.msg.chatId)
      }
      if (bool) {
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
        this.readMore = true
        var IMessage = chatjs.readStorage(this.chat_object.id)
        IMessage = IMessage.sort(tool.compare('clientTimes'))
        this.currentPage = 1
        IMessage.map((item, index) => {
          var timeText = this.getTimeText(item.clientTimes)
          if (timeText.text) {
            item.timeShow = timeText.show
            item.timeTitle = timeText.text
            item.timeType = timeText.type
          }
        })
        if (IMessage.length > 0) IMessage[0].timeShow = true
        this.chat_msg = IMessage.slice((IMessage.length > 20 && !length) ? (IMessage.length - 20) : 0, IMessage.length)
        if (length === IMessage.length) {
          this.readMore = false
        }
      }
      this.ws_sendread()
      let getTop = (num) => {
        var content = document.getElementById('content')
        if (!num) content.style.opacity = 0
        setTimeout(function () {
          if (content) {
            content.scrollTop = content.scrollHeight - (length ? this.scrollTop : 0)
            this.scrollTop = content.scrollHeight
            content.style.opacity = 1
          }
        }, this.chat_msg.length * 20)
      }
      if (data) {
        if (data.sender === this.OneselfIMID) getTop(1)
      } else {
        getTop()
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
    groupCommand (type) {
      this.setId = JSON.parse(JSON.stringify(this.chat_object.id))
      if (type === 1) {
        this.getGroupInfo(this.chat_object.id, 1)
      } else if (type === 2) {
        this.form.id = this.chat_object.id
        this.form.name2 = this.chat_object.name
        this.form.notice = this.chat_object.notice
        this.editGroupForm = true
      } else if (type === 3) { // 解散群聊
        this.releaseGroupForm = true
      } else if (type === 4) {
        this.quitGroupForm = true
      } else if (type === 5) { // 转让群聊
        this.seachGroupMember = ''
        this.transferId = ''
        this.transferGroupDialog = true
      }
    },
    /** 修改群设置 */
    modifyGroupInfo () {
      this.isGroupName = false
      this.isGroupName2 = false
      if (this.groupInfo.type === 0 || (this.groupInfo.principal !== this.OneselfIMID)) {
        return
      }
      var name = this.groupInfo.name.trim()
      var notice = this.groupInfo.notice.trim()
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
      var jsondata = {'id': this.groupInfo.id, 'name': this.groupInfo.name, 'notice': this.groupInfo.notice}
      HTTPServer.modifyGroupInfo(jsondata, '').then((res) => {
        this.chat_object.name = name
        this.chat_object.notice = notice
        var contains = tool.contains(this.chatList, 'id', this.chat_object, 'id')
        if (contains) {
          this.chatList[contains.i].name = name
          this.chatList[contains.i].notice = notice
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
        this.particularsForm = false
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
        this.particularsForm = false
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
    /** 转让群聊 */
    transferGroup () {
      if (!this.transferId) {
        this.$message.error('请选择转让成员！')
        return
      }
      HTTPServer.transferGroup({'id': this.chat_object.id, 'signId': this.transferId}, 'Loading').then((res) => {
        this.transferGroupDialog = false
        var contains = tool.contains(this.chatList, 'id', this.chat_object, 'id')
        if (contains) {
          this.chatList[contains.i].principal = this.transferGroup
          this.$set(this.chatList, contains.i, this.chatList[contains.i])
          this.chat_object = this.chatList[contains.i]
        }
        this.getGroupInfo(this.chat_object.id)
      })
    },
    /** 置顶 */
    setTop () {
      var jsondata = {'id': this.chat_object.id, 'chat_type': this.chat_object.chat_type}
      HTTPServer.imsetTop(jsondata, 'Loading').then((res) => {
        this.chat_object.top_status = (this.chat_object.top_status === '1') ? '0' : '1'
      })
    },
    /** 开启详情弹窗 */
    openParticularsForm () {
      if (this.chat_object.chat_type === 1) {
        this.particularsForm = true
      } else if (this.chat_object.chat_type === 2) {
        HTTPServer.getQueryEmployeeInfo({'sign_id': this.chat_object.receiver_id}, '').then((res) => {
          this.infoChecked = (this.chat_object.top_status === '1')
          this.particularsForm = true
          var postName = res.employeeInfo.post_name
          this.chat_object.notice = res.departmentInfo[0].department_name + (postName ? ('-' + postName) : '')
        })
      } else {
        this.getAssisstantInfo()
      }
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
      var msgDate = new Date(data.times)
      var timeCha = ((new Date()).getTime()) - msgDate.getTime()
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
      this.atlist = [{'id': '0', 'employee_name': '所有人'}]
      this.groupEmployeeInfo.map((item, index) => {
        if (item.sign_id !== this.OneselfIMID) {
          this.atlist.push(item)
        }
      })
      document.getElementById('atGroupForm').childNodes[0].style.bottom = '150px'
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
      this.draftInit()
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
            if (!data.application_id && data.type === 2) { // 企信小助手
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
        if (data.chat_type === 1) {
          this.getGroupInfo(data.id)
        }
      }
      this.historyMsg() /** 获取历史消息 */
    },
    /** 搜索选择 */
    listSearchChange (key) {
      var id = key.split('-')
      var type = id[0]
      id = parseInt(id[1])
      if (type === '1') {
        chatjs.addSingleChat({'sign_id': id})
      } else {
        var contains = tool.contains(this.chatList, 'id', id)
        delete contains.i
        if (contains.is_hide === '1') {
          this.draftInit()
          this.chat_object = contains
          this.chat_msg = []
          this.currentPage = 1
        } else {
          this.switchChat(null, contains)
        }
        chatjs.updateChatList2(contains)
      }
      setTimeout(() => {
        this.listSearch = ''
      }, 100)
    },
    remoteMethod (query) {
      this.listSearchInput = query
      // var data = JSON.parse(JSON.stringify(this.chatSeachList))
      var init = (data) => {
        var arr = [{'id': 1, 'label': '联系人', 'row': []}, {'id': 2, 'label': '群聊', 'row': []}, {'id': 3, 'label': '小助手', 'row': []}]
        data.map((item) => {
          item.row.map((row) => {
            var name = row.employee_name || row.name
            if (name.indexOf(query) >= 0) {
              if (row.chat_type === 1) {
                arr[1].row.push(row)
              } else if (row.chat_type === 2) {
                arr[0].row.push(row)
              } else if (row.chat_type === 3) {
                arr[2].row.push(row)
              }
            }
          })
        })
        return arr
      }
      this.chatSeachList = init(JSON.parse(JSON.stringify(this.chatSeachList2)))
    },
    /** 初始化列表搜索数据 */
    initlistSearch () {
      this.listSearchInput = ''
      HTTPServer.findUsersByCompany({}, '').then((res) => {
        this.funInit(res)
        var arr = []
        this.chatSeachList = [{'id': 1, 'label': '联系人', 'row': []}, {'id': 2, 'label': '群聊', 'row': []}, {'id': 3, 'label': '小助手', 'row': []}]
        this.chatList.map((item, index) => {
          item.name = item.employee_name || item.name
          if (item.chat_type === 1) {
            this.chatSeachList[1].row.push(item)
          } else if (item.chat_type === 2) {
            this.chatSeachList[0].row.push(item)
            arr.push(item)
          } else if (item.chat_type === 3) {
            this.chatSeachList[2].row.push(item)
          }
        })
        this.treeData.map((item, index) => {
          var contains = tool.contains(arr, 'receiver_id', item, 'receiver_id')
          if (!contains && item.sign_id !== this.OneselfIMID) {
            item.chat_type = 2
            this.chatSeachList[0].row.push(item)
          }
        })
        this.chatSeachList2 = JSON.parse(JSON.stringify(this.chatSeachList))
      })
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
    createGroup () {
      var empName = []
      var empID = []
      this.prepareData.map((item) => {
        empName.push(item.name)
        empID.push(item.sign_id)
      })
      var groupName = this.employeeInfo.name + '、' + empName.join('、')
      if (groupName.length > 12) groupName = groupName.substring(0, 11) + '…'
      var jsondata = {
        'name': groupName, // 群名称
        'notice': '', // 群公告
        'peoples': empID.join(','), // 群成员，
        'type': '1' // 群聊类型设置 0：总群 1：新建群聊
      }
      HTTPServer.addGroupChat(jsondata, 'Loading').then((res) => {
        this.readMore = false
        this.groupInfo = res.groupInfo
        this.groupEmployeeInfo = res.employeeInfo
        res.groupInfo.latest_push_time = new Date().getTime()
        this.chatList.unshift(res.groupInfo)
        chatjs.updateChatData(this.chatList)
        this.atlist = res.employeeInfo
        this.initAtData()
        this.chat_object = res.groupInfo
        this.chatId = res.groupInfo.id
        this.infoChecked = (this.groupInfo.top_status === '1')
        this.usCmdID = 6
        this.currentPage = 1
        this.receiverID = res.groupInfo.id
        this.chat_msg = []
        var jsondata = {'msg': '欢迎' + empName.join('、') + '加入本群', 'type': 7, 'times': ((new Date()).getTime()) + this.timeInterval, 'chatId': this.chatId, 'senderAvatar': this.employeeInfo.picture, 'senderName': this.employeeInfo.name}
        this.ws_sendmessage(jsondata)
      })
    },
    /** 添加单聊、创建群聊 */
    getMemberDialog () {
      this.$bus.emit('commonMember', {'prepareData': [], 'prepareKey': 'add-single-or-group', 'seleteForm': true, 'banData': ['1-' + this.employeeInfo.id], 'navKey': '1', 'removeData': ['1-' + this.employeeInfo.id]}) // 给父组件传值
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
    /** 选人初始化 */
    funInit (arr) {
      this.treeData = []
      let that = this
      var arrInit = function (arr) {
        arr.map((item) => {
          if (item.childList) {
            if (item.childList.length > 0) {
              arrInit(item.childList)
            }
          }
          if (item.users) {
            if (item.users.length > 0) {
              var users = item.users
              users.map((user) => {
                that.treeData.push({ 'id': user.id, 'name': user.employee_name, 'picture': user.picture, 'chat_type': 2, 'receiver_id': user.sign_id })
              })
            }
          }
        })
      }
      arrInit(arr)
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
    /** 获取群信息 */
    getGroupInfo (id, type) {
      HTTPServer.getGroupInfo({'groupId': id}, 'Loading').then((res) => {
        this.groupEmployeeInfo = res.employeeInfo
        this.atlist = res.employeeInfo
        this.initAtData()
        if (type === 1) {
          var arr = []
          for (var i = 0; i < this.groupEmployeeInfo.length; i++) {
            arr.push({'id': this.groupEmployeeInfo[i].id, 'name': this.groupEmployeeInfo[i].employee_name, 'picture': this.groupEmployeeInfo[i].picture, 'type': 1, 'sign_id': this.groupEmployeeInfo[i].sign_id, 'value': '1-' + this.groupEmployeeInfo[i].id})
          }
          this.$bus.emit('commonMember', {'prepareData': arr, 'prepareKey': 'edit-group-member', 'seleteForm': true, 'banData': ['1-' + this.employeeInfo.id], 'navKey': '1', 'removeData': ['1-' + this.employeeInfo.id]}) // 给父组件传值
        } else {
          this.groupInfo = res.groupInfo
          this.infoChecked = (this.groupInfo.top_status === '1')
        }
      })
    },
    // 修改群名称
    editGroupName (type) {
      if (this.groupInfo.type === '0' || this.groupInfo.principal !== this.OneselfIMID) {
        return
      }
      if (type === 1) {
        this.isGroupName2 = true
      } else {
        document.getElementById('editGroupName').focus()
        this.isGroupName = true
      }
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
        var total = 0
        this.chatList.map((item, index) => {
          if (item.id === data.assistant_id) {
            item.unread_nums = parseInt(this.chatList[index].unread_nums) - 1
            this.$set(this.chatList, index, item)
          }
          total += parseInt(item.unread_nums || 0)
        })
        chatjs.updateChatData(this.chatList)
        this.$bus.emit('chat-unread-nums', total) /** 总计未读数 */
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
      var total = 0
      if (data.type === 16) { // 单条已读
        var contains = tool.contains(this.assistantMessageData, 'id', data, 'data_id')
        if (contains) {
          if (this.assistantMessageData[contains.i].read_status === '0') {
            this.assistantMessageData[contains.i].read_status = '1'
            this.$set(this.assistantMessageData, contains.i, this.assistantMessageData[contains.i])
            this.chatList.map((item, index) => {
              if (item.id === data.assistant_id) {
                item.unread_nums = parseInt(this.chatList[index].unread_nums) - 1
                this.$set(this.chatList, index, item)
              }
            })
          }
        }
      } else if (data.type === 17) { // 多条已读
        this.assistantMessageData.map((item, index) => {
          item.read_status = '1'
          this.$set(this.assistantMessageData, index, item)
        })
        total = 0
        this.chatList.map((item, index) => {
          if (item.id === data.assistant_id) {
            item.unread_nums = 0
            this.$set(this.chatList, index, item)
          }
          total += parseInt(item.unread_nums || 0)
        })
        chatjs.updateChatData(this.chatList)
        this.$bus.emit('chat-unread-nums', total) /** 总计未读数 */
      } else if (data.type === 20) { // 审批操作
        var contains2 = tool.contains(this.assistantMessageData, 'id', data, 'im_apr_id')
        if (contains2) {
          this.assistantMessageData[contains2.i].param_fields = JSON.stringify(data.param_fields)
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
    openFilePreview (data, event) {
      if (event) {
        if (event.target.className.indexOf('download') >= 0) {
          return
        }
      }
      console.log(data)
      data.fileForm = true
      if (data.type === 2) {
        data.file_name = data.fileName
        data.file_type = data.fileType
        data.file_size = data.fileSize
        data.file_url = data.fileUrl + '&TOKEN=' + this.token
        this.$bus.emit('file-preview', data)
      } else {
        data.file_name = data.fileName
        data.file_type = data.fileType
        data.file_size = data.fileSize
        var fileid = data.fileId ? ('&id=' + data.fileId) : ''
        data.file_url = data.fileUrl + '&TOKEN=' + this.token + fileid
        this.$bus.emit('file-preview', data)
      }
    }
  },
  destroyed () {
    console.log('页面离开')
    window.clearInterval(this.localfun)
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
    .iconfont{font-size: 30px;float: left;margin: 16px 10px 0 0;line-height: 1;cursor: pointer;}
    img{width: 30px;height: 30px;border-radius: 50%;float: left;margin: 16px 10px 0 0;}
    .simpName{width: 30px;height: 30px;float: left;margin: 15px 10px 0 0;line-height: 30px;font-size: 12px;
      .iconfont{font-size: 18px;color: #fff;margin: 6px 0 0 6px;}
    }
    i.info{font-size: 20px;float: right;margin: 21px 0 0 0;line-height: 1;}
    .el-dropdown{height: 20px;line-height: 20px;}
    i.el-icon-arrow-down{font-size: 20px;}
    .allRead{float: right;line-height: 20px;margin: 20px 20px 0 0;}
    .screening{float: right;line-height: 20px;margin: 20px 20px 0 0;font-size: 20px;}
    .chat-avatar-mian{
      float: left;
    }
    .assistant{width: 30px; height: 30px;float: left;margin: 16px 10px 0 0;line-height: 30px;font-size: 12px;border-radius: 50%;
      .iconfont{font-size: 18px;color: #fff;margin: 6px 0 0 6px;}
      img{
        width: 30px;height: 30px;margin: 0;
      }
    }
    input[readonly]{
      border: none;
    }
    .el-input{
      width: 200px;
      margin-left: -10px;
    }
  }
  .el-aside{background: #EBEDF0;
    .nav-header{height: 59px;line-height: 60px;font-size: 18px;color: #4A4A4A;padding: 0 15px;
      i{font-size: 24px;float: right;margin: 18px 0 0 15px;line-height: 1;color: #666;cursor: pointer;}
      .el-dropdown{float: right;}
    }
    .search{
      width: calc(100% - 30px);
      margin: 0 0 12px 15px;
      .el-input__suffix{
        left: 5px;
        right: auto;
      }
      .el-select__caret{
        transform: rotateZ(0);
      }
      .el-input__icon:before{
        content: "\E619";
      }
      .el-input__inner{
        padding: 0 0 0 31px;
      }
    }
    .chat-list{
      height: calc(100% - 104px);
      overflow-y: auto;
    }
    .item{height: 50px;line-height: 50px;padding: 0 15px;cursor: pointer;margin-top: 2px;position: relative;
      .chat-avatar-mian{
        float: left;
      }
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
      .assistant{width: 30px; height: 30px;float: left;margin: 10px 10px 0 0;line-height: 30px;font-size: 12px;border-radius: 50%;
        .iconfont{font-size: 18px;color: #fff;margin: 6px 0 0 6px;}
        img{
          margin-top: 0;
        }
      }
      i{font-size: 16px;float: right;margin: 18px 0 0 0;display: none;}
      .num{
        display: inline-block;
        padding: 1px 3px;
        background: #FF3B30;
        border-radius: 50px;
        line-height: 1;
        font-size: 12px;
        color: #FFF;
        width: auto;
        position: absolute;
        left: 35px;
        top: 7px;
        right: auto;
        margin: 0;
      }
      .top_status{position: absolute;width: 0;height: 0;right: 0;top: 0;border-top: 8px solid #409eff;border-left: 8px solid transparent;}
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
    .exist{height: 174px;padding: 5px 15px;overflow-y: auto;-moz-user-modify: read-write-plaintext-only;-webkit-user-modify: read-write-plaintext-only;
      .at{display: inline-block;color: #409eff;width: auto;padding-left: 0;border: none;height: 17px;}
      .at:focus{border: none;box-shadow: none;}
      .emoji{width: 30px;}
      img{max-width: 400px;min-width: 30px;min-height: 30px;}
      .atwho-inserted{
        >span::before{
          content: "@";
          display: inline-block;
        }
      }
    }
    .exist[contenteditable]:empty:before{
      content: attr(placeholder);
      color: #CCCCCC;
    }
    .exist[contenteditable]:focus{
        content:none;
    }
  }
  .el-main{
    overflow-y: auto;padding: 10px 20px 20px;
    .item{
      overflow: hidden;padding-top: 20px;
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
        line-height: 1;
        margin-bottom: 6px;
        .name{
          font-size: 12px;
          color: #8693A2;
        }
        .time{
          font-size: 12px;
          color: #8693A2;
        }
      }
      .content{margin: 0 0 0 5px;padding: 10px 16px;max-width: 60%;display: inline-block;word-wrap: break-word;text-align: left;position: relative;
        *{
          font-size: 14px;
          color: #212121;
        }
        background: #F2F2F2;
        border-radius: 4px;
        .time{
          display: none;
          position: absolute;
          font-size: 12px;
          color: #8693A2;
          top: -17px;
          left: 0;
          line-height: 1;
          min-width: 200px;
          .name{
            opacity: 0;
            display: none;
          }
        }
        .group2{
          top: -20px;
          left: -5px;
          .name{
            display: inline-block;
          }
        }
        .popper_arrow{
          display: inline-block;
          margin: -4px 0 0 -19px;
          float: left;
          width: 6px;
          height: 6px;
          background: #f2f2f2;
          transform: rotate(45deg);
          -ms-transform: rotate(45deg);
          -webkit-transform: rotate(45deg);
        }
        img{max-width: 100%;max-height: 400px;min-width: 200px;}
        video{max-width: 100%;min-width: 200px;max-height: 600px;}
        .emojiImg{width: 28px;min-width: 1px;}
        >span{display: inline-block;max-width: 100%;word-wrap: break-word;font-size: 14px;color: #4A4A4A;word-break: break-all;}
        .at{color: #409eff;margin-right: 5px;height: 18px;}
        a{color: #409eff;text-decoration: 1px solid #409eff;}
        .file_box{display: inline-block;
          margin: 3px 0;
          width: 218px;
          .file-name{
            font-size: 12px;
            color: #000;
            display: inline-block;
            margin: 0 0 2px 0;
            white-space: nowrap;
            text-overflow: ellipsis;
            overflow: hidden;
            width: calc(100% - 44px);
            float: left;
          }
          .file-size{
            font-size: 12px;
            color: #999;
          }
          .download{
            font-size: 12px;
            color: #999;
            cursor: pointer;
            margin-left: 10px;
          }
          .file{
            font-size: 34px;
            float: left;
            margin: 0 10px 0 0;
            color: #4786D1;
            cursor: pointer;
          }
          img.file{
            width: 34px;
            height: 34px;
            min-width: 1px;
          }
        }
      }
      .prompt{
        text-align: center;
        span{
          display: inline-block;
          padding: 4px 16px;
          background: #F3F5F8;
          border-radius: 4px;
          font-size: 12px;
          color: #999;
          line-height: 20px;
          max-width: 80%;
        }
      }
      .content:hover{
        .time{
          display: inline-block;
        }
      }
      .error{color: red;font-size: 12px;}
    }
    .item.right{text-align: right;padding-right: 10px;
      .simpName{float: right;}
      .avatar{float: right;margin-left: 10px;}
      .content{margin: 0;float: right;background: #E0EAF9;
        .time{
          left: auto;
          right: 0;
          text-align: right;
        }
        .popper_arrow{
          float: right;
          margin: 0 -19px 0 0;
          background: #E0EAF9;
        }
        span{text-align: left;}
        .im-handle{
          position: absolute;
          left: -128px;
          bottom: 0;
          width: 120px;
          line-height: 36px;
          text-align: right;
          span{
            color: #4A90E2;
            display: inline-block;
            line-height: 1;
          }
          .revocation{margin: 0 0 0 8px;font-size: 14px;color: #409eff;cursor: pointer;}
          .isRead{cursor: pointer;}
        }
      }
    }
    .apply-item{
      header{
        float: left;
        width: 36px;
        position: relative;
        margin: 0 15px 0 0;
        .simpName{
          margin: 0 15px 0 0;
          img{
            width: 36px;
            height: 36px;
            margin: 0;
            float: left;
            border-radius: 50%;
          }
        }
        .iconfont{
          font-size: 26px;
        }
        .unread{
          width: 10px;
          height: 10px;
          background: #FF6260;
          border: 1px solid #fff;
          border-radius: 50%;
          position: absolute;
          right: 0;
        }
      }
      .module{
        margin-bottom: 6px;
        .name{
          font-size: 14px;
          color: #42484F;
          margin-right: 10px;
        }
        .time{
          font-size: 12px;
          color: #8693A2;
        }
      }
      .panel{
        padding: 15px 20px;
        border: 1px solid #D9D9D9;
        border-radius: 3px;
        width: 50%;
        margin-left: 51px;
        position: relative;
        word-wrap: break-word;
        .panel-content{
          font-size: 16px;
          color: #4A4A4A;
          margin-bottom: 5px;
        }
        .panel-content.at{
          overflow: hidden;
          text-overflow: ellipsis;
          display: box;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }
        .panel-field{
          margin-top: 5px;
          font-size: 14px;
          color: #A0A0AE;
        }
        .arrow-left {
            width: 0;
            height: 0;
            border-top: 6px solid transparent;
            border-bottom: 6px solid transparent;
            border-right: 6px solid #ccc;
            position: absolute;
            left: -6px;
            top: 5px;
        }
        .arrow-left::before {
            content: '';
            width: 0;
            height: 0;
            border-top: 6px solid transparent;
            border-bottom: 6px solid transparent;
            border-right: 6px solid #fff;
            display: inline-block;
            margin: -6px 0 0 1px;
            float: left;
        }
      }
    }
  }
  .is-vertical{padding: 0 25px;}
  .group-container{
    width: calc(100% + 25px);
  }
  .group-info{
    background: #F6F7F9;
    border-left: 1px solid #E7E8E9;
    .notice{
      padding: 0 10px;
      border-bottom: 1px solid #e7e7e7;
      margin-bottom: 12px;
      text-align: center;
      height: 130px;
      .header{
        line-height: 34px;
        font-size: 12px;
        color: #3A3A3A;
      }
      .notice-content{
        height: 96px;
        font-size: 12px;
        color: #666;
        overflow-y: auto;
        text-align: left;
      }
      >img{
        width: 50px;
        height: 50px;
        margin-top: 32px;
      }
      .notice-none{
        font-size: 14px;
        color: #999;
        margin-top: 18px;
      }
    }
    .member{
      height: calc(100% - 143px);
      padding: 0 0 0 20px;
      .header{
        line-height: 27px;
      }
      .group-member{
        height: calc(100% - 27px);
        overflow-y: auto;
        .avatar-mian-1{
          float: left;
          >.simpName{
            width: 26px;
            height: 26px;
            font-size: 12px;
            line-height: 26px;
            float: left;
            margin: 5px 0 0 0;
          }
          >img{
            width: 26px;
            height: 26px;
            border-radius: 50%;
            float: left;
            margin: 5px 0 0 0;
          }
        }
        .member-item{
          line-height: 36px;
          overflow: hidden;
          cursor: pointer;
          .name{
            line-height: 16px;
            max-width: 80px;
            height: 16px;
            display: inline-block;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            margin: 10px 0 0 5px;
            float: left;
          }
          >.iconfont{
            color: #FF6F00;
          }
        }
      }
    }
  }
}
.el-popper{
  width: 200px;padding: 10px 0;
  .item{height: 40px;line-height: 40px;padding: 0 20px;cursor: pointer;
    i{font-size: 16px;margin: 11px 10px 0 0;float: left;}
  }
  .item:hover{background: #F2F2F2;}

}
.groupChat{
  .el-dialog{
    width: 300px;
    margin: 46px 0 0 200px!important;
  }
  .el-dialog__header{
    padding: 8px 20px 7px;
  }
  .el-dialog__headerbtn{
    top: 11px;
  }
  .el-dialog__body{
    padding: 10px 0;
    max-height: 450px;
    overflow-y: auto;
  }
  .item{font-size: 14px;height: 44px;line-height: 44px;padding: 0 30px 0 20px;cursor: pointer;
    img{float: left;margin: 7px 10px 0 0;line-height: 1;width: 30px;}
    .name{
      display: inline-block;
      width: calc(100% - 60px);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .iconfont:first-child{float: left;margin: 7px 10px 0 0;font-size: 30px;line-height: 1;color: #409eff;}
    .iconfont:last-child{float: right;margin: 17px 0 0 0;font-size: 14px;cursor: pointer;line-height: 1;}
  }
  .item:hover{background: #f2f2f2;
    .iconfont:last-child{
      color: #409eff;
    }
  }
}
.side-shade{
  background: none;
}
.particularsForm{
  height: calc(100% - 60px);
  top: 60px;
  box-shadow: 2px -2px 4px 0 rgba(155,155,155,0.30), -2px 2px 4px 0 rgba(155,155,155,0.30);
  header{
    height: 50px;
    line-height: 50px;
  }
  .notice-header{
    color: #333;
    font-size: 14px;
    margin: 25px 0 7px 0;
  }
  .side-body{padding: 17px 20px 20px;overflow-y: auto;height: calc(100% - 50px);}
  .info{
    padding-bottom: 20px;
    border-bottom: 1px solid #e7e7e7;
    .header{min-height: 43px;
      img{width: 40px;float: left;margin: 3px 10px 0 0;border-radius: 50%;height: 40px;}
      .name{display: inline-block;margin-top: 13px;font-size: 14px;color: #333;}
      .total{
        font-size: 12px;
        color: #666;
      }
      .chat-avatar-mian{
        .simpName{
          width: 40px;
          height: 40px;
          line-height: 40px;
        }
      }
      .assistant{
        width: 40px;
        height: 40px;
        line-height: 40px;
        border-radius: 50%;
        float: left;
        margin: 3px 10px 0 0;
        text-align: center;
        .iconfont{
          color: #fff;
          font-size: 26px;
        }
        img{
          margin: 0 10px 0 0;
        }
      }
    }
    .el-input{
      width: calc(100% - 100px);
      margin-left: -5px;
      .el-input__inner{
        padding: 0 5px;
      }
      .el-input__inner[readonly]{
        border: none;
      }
    }
    .icon-pc-paper-name{
      float: right;
      margin-top: 4px;
    }
    .notice{
      textarea{
        font-size: 14px;
        color: #333;
      }
      textarea[readonly] {
        padding: 0;
        border: none;
      }
    }
    .automaton{float: right;background: #FBFBFB;border: 1px solid #E7E7E7;border-radius: 3px;padding: 2px 8px;color: #A0A0AE;margin: 12px 0 0 0;font-size: 12px;}
  }
  .session{
    .info{
      border-bottom: none;
      .chat-avatar-mian{
        float: left;
      }
      .name{
        margin: 3px 0 0 8px;
        font-size: 14px;
        color: #333;
        line-height: 40px;
      }
      .notice{
        margin-top: 15px;
      }
    }
    .top{
      border-bottom: 1px solid #e7e7e7;
    }
  }
  .top{
    line-height: 44px;
    .icon-xuanrenkongjian-icon5{
      float: right;
    }
    .el-switch{float: right;margin: 8px 0 0 0;
      .el-switch__core{width: 44px!important;height: 22px;border-radius: 14px;
        .el-switch__button{width: 24px;height: 24px;}
      }
      .el-switch__core:after{width: 18px;height: 18px;}
    }
    .el-switch.is-checked{
      .el-switch__core::after{margin-left: -19px;}
    }
    .el-checkbox{float: right;margin: 0 10px 0 0;}
  }
  .el-button--danger{
    position: absolute;
    bottom: 50px;
    width: calc(100% - 40px);
    font-size: 16px;
  }
  .st-item{line-height: 38px;
    span{display: inline-block;font-size: 16px;color: #000;}
    .title{width: calc(100% - 110px);padding-left: 5px;}
    .el-checkbox{margin-left: 6px;}
  }
  .st-item.header{line-height: 40px;
    span{
      font-size: 16px;color: #666;
    }
  }
}
#atGroupForm{
  .el-dialog{margin: 0;width: 200px;position: absolute;top: auto;
    .el-dialog__header{display: none;}
    .el-dialog__body{padding: 12px 0 10px;
      .el-input{
        width: calc(100% - 20px);
        margin-left: 10px;
      }
      .group-member{
        max-height: 180px;overflow-y: auto;
      }
      .item{line-height: 24px;cursor: pointer;padding-left: 15px;margin-top: 1px;display: inline-block;width: 100%;}
      .item:hover{background: #f2f2f2;}
      .item.is-focus{background: #f2f2f2;}
    }
  }
}
.transferGroupDialog{
  .el-dialog{
    width: 300px;
    .el-dialog__header{
      padding: 15px 20px 10px;
    }
    .el-dialog__body{
      padding: 10px 17px;
      header{
        font-size: 14px;
        color: #000;
        line-height: 40px;
        margin-top: 8px;
      }
      .group-member{
        max-height: 320px;
        overflow-y: auto;
        min-height: 100px;
        .member-item{
          padding: 0 30px 0 20px;
          line-height: 38px;
          cursor: pointer;
          .simpName,img{
            width: 30px;
            height: 30px;
            line-height: 30px;
            font-size: 12px;
            float: left;
            border-radius: 50%;
            margin: 4px 7px 0 0;
          }
          .iconfont{
            float: right;
            font-size: 16px;
          }
          .icon-pc-member-sign-ok{
            color: #1890ff;
          }
        }
      }
    }
  }
}
.msgReadForm{margin: 0;padding: 4px 0;min-height: 200px;
  .el-dialog{margin: 0 auto;}
  .el-dialog__header{display: none;}
  .el-dialog__body{padding: 0;height: 456px;}
  header{
    text-align: center;
    line-height: 50px;
    font-size: 14px;
    color: #212121;
  }
  .dialog-close{
    position: absolute;
    right: 10px;
    top: 18px;
    color: red;
    font-size: 14px;
    line-height: 1;
  }
  .header{
    div{display: inline-block;width: 50%;text-align: center;line-height: 25px;
      a{color: #999;font-size: 14px;}
      a.active{color: #549AFF;}
    }
  }
  .employee-box{
    height: calc(100% - 75px);
    overflow-y: auto;
    padding: 15px 0;
  }
  .item{line-height: 44px;height: 44px;padding-left: 40px;
    .name{
      display: block;
      overflow: hidden;
      word-break: keep-all;
      text-overflow: ellipsis;
      max-width: 80px;
      float: left;
      line-height: 20px;
      height: 20px;
      margin: 12px 0 0 0;
    }
    img{width: 34px;height: 34px;border-radius: 50%;float: left;margin: 5px 18px 0 0;}
    .simpName{display: inline-block;width: 34px;line-height: 34px;text-align: center;border-radius: 50%;float: left;margin: 5px 18px 0 0;color: #fff;}
  }
  .item:hover{
    background: #e5e5e5;
  }
}
  .memoDetails{
    z-index: 5!important;
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
.list-search-dialog{
  width: 300px;
  display: none;
  .el-select-dropdown__wrap{
    max-height: 406px;
  }
  .el-select-group__title{
    font-size: 14px;
    color: #666;
    padding-left: 12px;
  }
  .el-select-dropdown__item{
    height: 40px;
    line-height: 40px;
    padding: 0 10px;
    .chat-avatar-mian{
      display: inline-block;
      width: 30px;
      height: 30px;
      line-height: 30px;
      float: left;
      margin: 5px 10px 0 0;
      .simpName{
        font-size: 12px;
        color: #fff;
      }
      img{
        width: 30px;
        height: 30px;
        border-radius: 50%;
      }
      .assistant{width: 30px; height: 30px;line-height: 30px;font-size: 12px;border-radius: 50%;
        .iconfont{font-size: 18px;color: #fff;margin: 6px 0 0 6px;}
      }
      .name{
        color: #333;
      }
    }
  }
  .el-select-dropdown__item.hover, .el-select-dropdown__item:hover{
    background: #EBEDF0;
  }
  .el-select-group__wrap:not(:last-of-type)::after{
    display: none;
  }
  .el-select-group__wrap:not(:last-of-type){
    padding-bottom: 0;
  }
  .el-select-dropdown__empty{
    overflow: hidden;
    padding: 50px 0;
  }
  p.el-select-dropdown__empty::before{
    content: "";
    display: inline-block;
    background: url(/static/img/no-data.png);
    width: 100%;
    height: 80px;
    background-size: 80px;
    background-repeat: no-repeat;
    text-align: center;
    margin: 0 0 0 111px;
  }
}
.list-search-dialog.listSearch{
  display: inline-block;
}
.atwho-container{
  .atwho-view{
    min-width: 140px;
    padding: 12px 0 10px;
  }
  .atwho-header{
    display: none;
  }
  .atwho-view-ul{
    .cur{
      background: #1890FF;
      color: #fff;
    }
    li{
      padding: 0 10px;
      font-size: 14px;
      height: 24px;
      line-height: 24px;
      border: none;
    }
  }
}
</style>
<style>
.cpChat-main .el-container .el-main .item .avatar-mian-1{float: left;margin-right: 10px;}
.cpChat-main .el-container .el-main .item.right .avatar-mian-1{float: right;margin-left: 10px;}
</style>
