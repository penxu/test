<template>
  <div class="epistolize-main-wrap" @click="hideSendMenu()">
    <!-- 回复加写信页面 -->
    <div class="replay">
      <!-- 写信页面的按钮栏 -->
      <div class="title">
        <div v-if="this.navId !== undefined">
          <div class="green"  @click="sendBefore"><i class="iconfont icon-Rectangle4" style="margin-right:10px;"></i>发送</div>
          <div class="delete" @click="openSendOntime()">定时发送</div>
          <div class="delete" @click="previewParticulars">预览</div>
          <div class="delete" @click="saveDraft">存草稿</div>
          <div class="delete" @click="undoCloses1">取消</div>
        </div>
        <div v-if="dataContent !== ''">
          <!-- 转发的发送接口 -->
          <div class="green" v-if="judgeData === 'transmit'" @click="forwardingSend"><i class="iconfont icon-Rectangle4" style="margin-right:10px;"></i>发送</div>
          <!-- 回复和再次编辑发送的接口 -->
          <div class="green" v-if="judgeData === 'Forwarded' || judgeData === 'AttachmentsForwarded'" @click="interfaceSend()"><i class="iconfont icon-Rectangle4" style="margin-right:10px;"></i>发送</div>
          <!-- 普通发送 -->
          <div class="green" v-if="judgeData === 'againCompile' || judgeData === '' || judgeData === 'sendEmailAddressee' || judgeData === 'sendEmail' || judgeData === 'againCompileSend'" @click="sendBefore"><i class="iconfont icon-Rectangle4" style="margin-right:10px;"></i>发送</div>
          <div class="delete" @click="openSendOntime()">定时发送</div>
          <div class="delete" @click="previewParticulars">预览</div>
          <div class="delete" v-if="dataContent.mail_box_id === 3 && judgeData !== 'sendEmailAddressee'" @click="compileSaveDraft">存草稿</div>
          <div class="delete" v-if="dataContent.mail_box_id !== 3 && judgeData !== 'sendEmailAddressee'" @click="saveDraft">存草稿</div>
          <div class="delete" v-if="dataContent.mail_box_id === 3 && judgeData === 'sendEmailAddressee'" @click="saveDraft">存草稿</div>
          <div class="delete" @click="undoCloses2">取消</div>
          <div class="delete" v-if="dataContent.mail_box_id === 3 && judgeData !== 'sendEmailAddressee'" @click="dedtermineButton(1,'删除')">删除</div>
          <div class="delete" v-if="dataContent.mail_box_id === 3 && judgeData !== 'sendEmailAddressee'" @click="dedtermineButton(2,'彻底删除')">彻底删除</div>
        </div>
      </div>
      <!-- 收发件各种输入栏 -->
      <div class="left">
        <ul>
          <!-- 发件人输入框 -->
          <li style="overflow: inherit; height: 40px;">
            <span class="titleList">发件人：</span>
            <div class="left">
              <span @click.stop="clickBoxShow" v-if="checkAddressers.account"><span class="nickname">{{checkAddressers.nickname}}</span>&nbsp;   &#60;{{checkAddressers.account}}&#62; </span><i v-if="addressers.length>1" @click.stop="clickBoxShow" class="el-icon-caret-bottom" :class="{rotate:rotate}" style="margin-left:10px;"></i>
              <ul v-if="boxShow && addressers.length>1" style="z-index: 1000;">
                <li v-for="(item, index) in addressers" :key="index" @click="checkAddressersa(item)" style="color:#888;"><span style="color:#000;">{{item.nickname}}</span>&nbsp;   &#60;{{item.account}}&#62; </li>
              </ul>
            </div>
            <div class="right1">
              <!-- 取消抄送人 -->
              <span @click="copyTo1" v-if="Cut">
                <span :class="{copyToStyle:Cut1}">{{copyTo}}</span>&nbsp;-&nbsp;
              </span>
              <!-- 取消密送人 -->
              <span @click="copyTo2" v-if="Cut">
                <span :class="{blindCarbonCopyStyle:Cut2}">{{blindCarbonCopy}}</span>&nbsp;|&nbsp;
              </span>
              <!-- 群发单显 -->
              <span  @click="copyTo3">
                <span :class="{massTextingStyle:Cut3}">{{massTexting}}</span>
              </span>
              </div>
          </li>
          <!-- 收件人输入框 -->
          <li v-if="Cut" @click="clickFocus(0)">
            <span class="titleList">收件人：</span>
            <div class="contacts" v-for="(item,index) in addressBook0" :key="index">
              <span>{{item.name}}</span>   &#60;{{item.mail_address}}&#62;
              <i class="el-icon-error" style="margin-left:10px;cursor: pointer;" @click="deleteAddressBook(addressBook0,index)"></i>
            </div>
            <div style="display:inline-block;float: left;margin: 7px; 0 0 10px;position: relative;">
              <input type="text" v-on:blur="changeCount($event,0)" v-model="FocusContent1" @input="changeValue($event)" @keyup="listenSpaceKey($event, 0)" class="addAccessory"/>
              <span style="float: left;display: inline-block;opacity: 0;">{{FocusContent1}}</span>
            </div>
            <i class="iconfont icon-qunfa" @click="personnel(0)"></i>
          </li>
          <!-- 抄送人输入框 -->
          <li v-if="Cut1 || monochrome"  @click="clickFocus(1)">
            <span class="titleList">抄送人：</span>
            <div class="contacts" v-for="(item,index) in addressBook1" :key="index"><span>{{item.name}}</span>   &#60;{{item.mail_address}}&#62;  <i class="el-icon-error" style="margin-left:10px;cursor: pointer;" @click="deleteAddressBook(addressBook1,index)"></i></div>
            <div style="display:inline-block;float: left;margin: 7px; 0 0 10px;position: relative;">
              <input type="text" v-on:blur="changeCount($event,1)" v-model="FocusContent2" @input="changeValue($event)" @keyup="listenSpaceKey($event, 1)" class="addAccessory"/>
              <span style="float: left;display: inline-block;opacity: 0;">{{FocusContent2}}</span>
            </div>
            <i class="iconfont icon-qunfa" @click="personnel(1)"></i>
          </li>
          <!-- 密送人输入框 -->
          <li v-if="Cut2 || monochrome" @click="clickFocus(2)">
            <span class="titleList">密送人：</span>
            <div class="contacts" v-for="(item,index) in addressBook2" :key="index"><span>{{item.name}}</span>   &#60;{{item.mail_address}}&#62; <i class="el-icon-error" style="margin-left:10px;cursor: pointer;" @click="deleteAddressBook(addressBook2,index)"></i></div>
            <div style="display:inline-block;float: left;margin: 7px; 0 0 10px;position: relative;">
              <input type="text" v-on:blur="changeCount($event,2)" v-model="FocusContent3" @input="changeValue($event)" @keyup="listenSpaceKey($event, 2)" class="addAccessory"/>
              <span style="float: left;display: inline-block;opacity: 0;">{{FocusContent3}}</span>
            </div>
            <i class="iconfont icon-qunfa" @click="personnel(2)"></i>
          </li>
          <!-- 群发单显输入框 -->
          <li v-if="Cut3" @click="clickFocus(0)">
            <span class="titleList">群发单显：</span>
            <div class="contacts" v-for="(item,index) in addressBook0" :key="index"><span>{{item.name}}</span>   &#60;{{item.mail_address}}&#62; <i class="el-icon-error" style="margin-left:10px;cursor: pointer;" @click="deleteAddressBook(addressBook0,index)"></i></div>
            <div style="display:inline-block;float: left;margin: 7px; 0 0 10px;position: relative;">
              <input type="email" v-on:blur="changeCount($event,0)" v-model="FocusContent1" @input="changeValue($event)" class="addAccessory"/>
              <span style="float: left;display: inline-block;opacity: 0;">{{FocusContent1}}</span>
            </div>
            <i class="iconfont icon-qunfa" @click="personnel(0)"></i>
          </li>
          <!-- 主题输入框 -->
          <li><span class="titleList">主&nbsp;&nbsp;&nbsp;题：</span> <input type="text" class="subject" maxlength="120" v-model="sendData.subject"></li>
        </ul>
        <div class="richtext">
          <!-- 添加附件 -->
          <div class="accessory">
            <input type="file" name="fileList"  @change="beforeUpload($event)">
            <span style="color: #008FE5;"><i class="iconfont icon-fujianx" style="font-size: 18px;color: #008FE5;"></i>添加附件</span>
            <span>&nbsp;|&nbsp;&nbsp;&nbsp;<i class="iconfont icon-wenjianjia" style="margin-right:5px;"></i><span @click="fileAddAccessory" style="color:#999;cursor: pointer;">从文件库中添加附件</span></span>
            <div class="accessoryStyle-box">
              <div class="accessoryStyle" v-for="(item,index) in fileData" :key="index">
                <img v-if="item.fileType.fileType === 'img'" :src="item.file_url+'&TOKEN='+token" style="width: 30px; height: 30px; margin:5px 15px;"/>
                <!-- <i v-if="item.fileType.fileType!=='img'" :class="item.fileType.icon" style="color: #30B8C7;font-size:30px;margin:0 15px;"></i> -->
                <!-- <i v-if="item.file_type==='png'" :class="{'iconfont icon-file-png':item.file_type==='png'}" style="color: #30B8C7;font-size:30px;margin:0 15px;"></i>
                <i v-if="item.file_type==='jpg'" :class="{'iconfont icon-file-jpg':item.file_type==='jpg'}" style="color: #30B8C7;font-size:30px;margin:0 15px;"></i> -->
                <i v-else :class="showFileIcon(item.file_type)" style="color: #30B8C7;font-size:30px;margin:0 15px;float: left;"></i>
                <span class="accessoryStyle-name" :title="item.file_name">{{item.file_name}}</span>
                <span style="margin:2px 10px;color:#999;float: left;">{{item.reSize}}</span>
                <span style="color:red;margin-right:8px;margin-top:2px;cursor: pointer;float:right;" @click="deletedData(index)">删除</span>
              </div>
            </div>
          </div>
          <!-- 富文本编辑器 -->
          <!-- <mailUeditor v-if="judgeCompile" ref="richtextAddSign" :addSignData = "addSignData" :editorContent="dataContent.mail_content" :isEdit="true"></mailUeditor> -->
          <mailUeditor ref="richtextAddSign" :addSignData = "addSignData" :ueFromEditorData="{name:'xiexin'}" :editorContent="dataContent.mail_content" :isEdit="true"></mailUeditor>
          <!-- 底部各种备注操作栏 -->
          <div class="checkboxs">
            <el-checkbox v-model="checked1" >紧急</el-checkbox>
            <el-checkbox v-model="checked2"  >需要回执</el-checkbox>
            <el-checkbox v-model="checked3" @click.native="text()" >纯文本</el-checkbox>
            <el-checkbox v-model="checked4" >对邮件加密</el-checkbox>
            <!-- <el-checkbox v-model="checked5" >追踪邮件</el-checkbox> 暂时关闭该功能 -->
            <el-checkbox v-model="checked6" @change="useSign()">使用签名</el-checkbox>
            <!-- 签名多选列表 -->
            <el-select v-model="value4" placeholder="请选择" @change="richtextAddSigns()" v-if="checked6" size="mini">
              <el-option
                v-for="(item) in options"
                :key="item.id"
                :label="item.title"
                :value="item.id">
              </el-option>
            </el-select>
          </div>
          <!-- 转发的发送接口 -->
          <div class="green" v-if="judgeData === 'transmit' && navId === undefined" @click="forwardingSend()"><i class="iconfont icon-Rectangle4" style="margin-right:10px;"></i>发送</div>
          <!-- 回复和再次编辑发送的接口 -->
          <div class="green" v-if="this.navId === undefined && (judgeData === 'Forwarded' || judgeData === 'AttachmentsForwarded')" @click="interfaceSend()"><i class="iconfont icon-Rectangle4" style="margin-right:10px;"></i>发送</div>
          <!-- 普通发送 -->
          <div class="green" v-if="this.navId !== undefined || judgeData === 'againCompileSend' || judgeData === 'againCompile' || judgeData === '' || judgeData === 'sendEmailAddressee' || judgeData === 'sendEmail'" @click="sendBefore"><i class="iconfont icon-Rectangle4" style="margin-right:10px;"></i>发送</div>
          <div class="delete" v-if="this.navId !== undefined" @click="undoCloses1">取消</div>
          <div class="delete"  v-if="dataContent !== ''" @click="undoCloses2">取消</div>
        </div>
      </div>
      <!-- 右边最近联系人列表 -->
      <div class="right">
        <div class="RecentContacts">最近联系人</div>
        <ul>
          <li v-for="(item, index) in proson" :key="index" @click="addRecent(item)">
            <span class="headPortrait" v-show="item.employee_name===''">{{item.mail_account | limitTo(2)}}</span>
            <span class="headPortrait" v-show="item.employee_name!==''">{{item.employee_name | limitTo(1)}}</span>
            <div class="right-box">
              <p class="Name">{{item.employee_name}}</p>
              <span class="Email">{{item.mail_account}}</span>
            </div>
          </li>
        </ul>
      </div>
      <div class="popUpBox">
        <!-- 选择联系人弹窗 -->
        <el-dialog title='选择联系人' :visible.sync='seleteForm' width="1046px" class='employeeRadio candidateBox candidateBox5'>
          <div class="header" style="height: 50px;">
            <span :class="{control:controlStyle === 1}" @click="selectModules">从模块中选择</span>
            <span :class="{control:controlStyle === 2}" @click="controlStyle = 2;personnel(num)">从通讯录中选择</span>
          </div>
          <!-- 弹窗内容 -->
          <div class="content">
            <div v-if="controlStyle === 2" class=" linkman">
              <el-table ref='multipleTable' :data='tableList' tooltip-effect='dark' style='width: 100%' @selection-change='handleSelectionChange'>
                <el-table-column type='selection' width='55'></el-table-column>
                <el-table-column label='姓名'>
                  <template slot-scope='scope'><span class="nameOmit">{{ scope.row.name }}</span></template>
                </el-table-column>
                <el-table-column prop='mail_address' label='邮件地址'></el-table-column>
                <el-table-column label='添加时间'>
                  <template slot-scope='scope'>{{ scope.row.create_time | formatDate('yyyy-MM-dd HH:mm') }}</template>
                </el-table-column>
              </el-table>
            </div>
            <!-- 分页 -->
            <div class="Pagination" v-if="controlStyle === 2">
              <el-pagination
                @size-change='handleSizeChange'
                @current-change='handleCurrentChange'
                :current-page='pageNum'
                :page-sizes='[10, 20, 50, 100]'
                :page-size='pageSize'
                layout='total, sizes, prev, pager, next, jumper'
                :total='tableTotal'>
              </el-pagination>
            </div>
            <div v-if="controlStyle === 1" class="selectModule">
              <div class="select" style="margin: 20px;">
                <span style="margin-right:20px;">模块</span>
                <el-select v-model="value2" @change="modules(value2)" :placeholder="chinese_name2" :disabled="chinese_name2 === ''">
                  <el-option
                    v-for="(item,index) in options2"
                    :key="index"
                    :label="item.chinese_name"
                    :value="item.id">
                  </el-option>
                </el-select>
                <span style="margin:0 20px;">分类</span>
                <el-select v-model="value3" @change="classify(ids)" :placeholder="chinese_name3" :disabled="chinese_name3 === ''">
                  <el-option
                    v-for="(item, i) in options3"
                    :key="i"
                    :label="item.name"
                    :id="ids = item.id"
                    :value="item.name">
                  </el-option>
                </el-select>
              </div>
              <!-- 从模块中选择联系人列表 -->
              <ModuleList :bean="bean" :fieldName="-1"></ModuleList>
              <!-- <module-list :bean="bean" :fieldName="reference.fieldName" :query="reference.queryWhere" :dataId="dataDetail.id"></module-list> -->
            </div>
          </div>
          <!-- 弹窗底部 -->
          <div slot='footer' class='dialog-footer'  v-if="controlStyle === 2">
            <el-button @click="seleteForm = false">取 消</el-button>
            <el-button type="primary" @click="confirm">确 定</el-button>
          </div>
          <div slot='footer' class='dialog-footer'  v-if="controlStyle === 1">
            <el-button @click="seleteForm = false">取 消</el-button>
            <el-button type="primary" @click="confirmEmail">确 定</el-button>
          </div>
        </el-dialog>
        <!-- 定时时间弹窗 -->
        <el-dialog title='定时发送' width="600px" :visible.sync='seleteForm1'>
            <div class="content" style="padding-left:20px;overflow: auto;">
              <div style="font-size:20px;line-height:45px;margin-top:10px;">发送时间</div>
                <div class="block" style="margin-right:10px;">
                  <el-date-picker v-model="value1" :clearable="false" format="yyyy-MM-dd HH:mm" @change="FDate(value1)" type="datetime" placeholder="请选择时间">
                  </el-date-picker>
                </div>
                <div v-if="value1 !== '' && value1 !== undefined">本邮件将在 <span style="font-size: 16px;color: #409EFF;line-height: 45px;">{{new Date(value1).getTime() | formatDate('yyyy年  MM月  dd日  HH时 mm分')}}</span> 发送到对方邮箱</div>
                <div slot='footer' class='dialog-footer' style="float:right;">
                  <el-button @click="seleteForm1 = false">取 消</el-button>
                  <!-- 转发的定时发送-确定 -->
                  <el-button type="primary" v-if="judgeData === 'transmit'" @click="forwardingSend()" :disabled="schedule">确 定</el-button>
                  <!-- 回复和再次编辑的定时发送-确定 -->
                  <el-button type="primary" v-else-if="judgeData === 'Forwarded' || judgeData === 'AttachmentsForwarded'" @click="interfaceSend()" :disabled="schedule">确 定</el-button>
                  <el-button type="primary" v-else-if="dataContent.mail_box_id === 3" @click="sendBefore()" :disabled="schedule">确 定</el-button>
                  <el-button type="primary" v-else @click="sendBefore()" :disabled="schedule">确 定</el-button>
                </div>
            </div>
        </el-dialog>
        <!-- 文件库弹窗 -->
        <el-dialog title='从文件库中选择附件' :visible.sync='seleteForm2' class='employeeRadio candidateBox candidateBox6'>
            <div class="titles">
              <span @click="selectFileName($event, -1)" class="documentLibrary">文件库</span>
              <span v-for="(item,index) in filenavDataTitle" :key="index" class="fileName" @click="selectFileName($event, item, index)"> &nbsp;>&nbsp; <span>{{item.name}}</span></span>
            </div>
            <div class="content">
              <div v-for="(item,index) in filenavData" :key="index" class="item-nav" :class="item.className" @click="selectLibrary(item.id, item)"
              v-if="filenavDataJudge1 && item.status === '1' && (item.id === 1 || item.id === 2 || item.id === 3)">
                <i class="iconfont" :class="'icon-pc-paper-nav' + item.id"></i>
                <span>{{item.name}}</span>
              </div>
              <div class="item-content"  v-for="(item,index) in filenavDataContent" :key="index" :id="item.id.toString()" @click="selectLibraryFile(item.id, item, item.sign)" v-if="filenavDataJudge2">
                <i v-if="item.type !== '1' && item.sign === '0' " class="iconfont icon-pc-paper-file folder" :style="{color: item.color}"></i>
                <i v-if="item.type === '1' && item.sign === '0' " class="iconfont icon-pc-private-folder folder" :style="{color: item.color}"></i>
                <i v-if="item.sign === '1' " :class=" item.name | ressuffix" class="folder"></i>
                <img v-if="item.sign === '1' && item.siffix === '.png' || item.siffix === '.jpg' || item.siffix === '.gif'"  :src="previewUrl + '&id=' + item.id + '&time=' + ((new Date()).getTime())" onerror="javascript:this.src='/static/img/img-err.png';" />
                <span class="file-title">{{item.name}}</span>
                <span style="float:right;">{{item.create_time | formatDate('yyyy-MM-dd HH:mm')}}</span>
              </div>
              <!-- 模块列表 -->
              <div class="item-content"  v-for="(item,index) in filenavDataContent1" :id="item.model_id.toString()" :key="index" @click="selectModuleFile(item.model_id, item, item.sign)" v-if="filenavDataJudge3">
                <i v-if="item.type != '1' && item.sign == '0' " class="iconfont icon-pc-paper-file folder" :style="{color: item.color}"></i>
                <i v-if="item.type == '1' && item.sign == '0' " class="iconfont icon-pc-private-folder folder" :style="{color: item.color}"></i>
                <i v-if="item.sign == '1' " :class=" item.name | ressuffix" class="folder"></i>
                <img v-if="item.sign === '1' && item.siffix === '.png' || item.siffix === '.jpg' || item.siffix === '.gif'"  :src="previewUrl + '&id=' + item.id + '&time=' + ((new Date()).getTime())" onerror="javascript:this.src='/static/img/img-err.png';" />
                <span class="file-title">{{item.name}}</span>
                <span style="float:right;">{{item.create_time | formatDate('yyyy-MM-dd HH:mm')}}</span>
              </div>
            </div>
            <div class="footer">
              <el-button @click="seleteForm2 = false">取 消</el-button>
              <el-button type="primary" @click="addFileConfirm">确 定</el-button>
            </div>
        </el-dialog>
        <!-- 公共弹框 -->
        <el-dialog title="提示" :visible.sync="seleteForm3" width="30%">
          <span>你确定要{{promptMessage}}选中的邮件吗？</span>
          <span slot="footer" class="dialog-footer">
            <el-button @click="seleteForm3 = false">取 消</el-button>
            <el-button type="primary" @click="deleteConfirm">确 定</el-button>
          </span>
        </el-dialog>
        <!-- 选人弹窗 -->
        <div class="cuiban">
          <el-dialog :title="'选择审批人'" :visible.sync="cuibanVisible">
            <!-- 审批人 -->
            <div class="chaoson-content" v-if="this.getProcessTypeForMail !== 0">
              <span class="next"><span class="tag">*</span> 下一环节<br> &nbsp;&nbsp;&nbsp;审批人</span>
              <!-- 单人盒子 -->
              <div class="empitem">
                <div class="empitem-item" v-for="(item,index) in dataOne" :key="item.value">
                  <div class="simpName">
                    {{item.name | limitTo(-2)}}
                  </div>
                  <span class="fullName">{{item.name}}<i class="iconfont empitem-tag" @click="dataOne.splice(index,1)">&#xe6e9;</i></span>
                </div>
                <span class="iconfont icon" @click="openemployeeForm(1,'通过')" >&#xe683;</span>
              </div>
            </div>
            <!-- 抄送人 -->
            <div v-if="isCcTo === 1" class="chaoson-content">
              <span class="next"><span class="tag"> </span>&nbsp;&nbsp;&nbsp;抄送人</span>
              <!-- 单人盒子 -->
              <div class="empitem">
                <div class="empitem-item" v-for="(item,index) in dataOneForccTo" :key="item.value">
                  <div class="simpName">
                    {{item.name | limitTo(-2)}}
                  </div>
                  <span class="fullName">{{item.name}}<i class="iconfont empitem-tag" @click="dataOneForccTo.splice(index,1)">&#xe6e9;</i></span>
                </div>
                <span class="iconfont icon" @click="openemployeeFormForCcTo(1,'抄送')" >&#xe683;</span>
              </div>
            </div>
            <span slot="footer" class="dialog-footer">
              <el-button type="primary" @click="sendSure(1)">确 定</el-button>
              <el-button @click="cuibanVisible = false">取 消</el-button>
            </span>
          </el-dialog>
        </div>
        <!-- 选人弹窗(自定义) -->
        <div class="cuiban">
          <el-dialog :title="'指定下一审批人'" :visible.sync="userDefinedVisible">
            <!-- 审批人 -->
            <div class="chaoson-content">
              <span class="next"><span class="tag">*</span> 下一环节<br> &nbsp;&nbsp;&nbsp;审批人</span>
              <!-- 单人盒子 -->
              <div class="empitem">
                <div class="empitem-item" v-for="(item,index) in dataOne" :key="item.value">
                  <div class="simpName">
                    {{item.name | limitTo(-2)}}
                  </div>
                  <span class="fullName">{{item.name}}<i class="iconfont empitem-tag" @click="dataOne.splice(index,1)">&#xe6e9;</i></span>
                </div>
                <span class="iconfont icon" @click="userDefinedSelect = true" >&#xe683;</span>
              </div>
            </div>
            <!-- 抄送人 -->
            <div v-if="isCcTo === 1" class="chaoson-content">
              <span class="next"><span class="tag"> </span>&nbsp;&nbsp;&nbsp;抄送人</span>
              <!-- 单人盒子 -->
              <div class="empitem">
                <div class="empitem-item" v-for="(item,index) in dataOneForccTo" :key="item.value">
                  <div class="simpName">
                    {{item.name | limitTo(-2)}}
                  </div>
                  <span class="fullName">{{item.name}}<i class="iconfont empitem-tag" @click="dataOneForccTo.splice(index,1)">&#xe6e9;</i></span>
                </div>
                <span class="iconfont icon" @click="openemployeeFormForCcTo(1,'抄送')" >&#xe683;</span>
              </div>
            </div>
            <span slot="footer" class="dialog-footer">
              <el-button type="primary" @click="sendSure(1)">确 定</el-button>
              <el-button @click="userDefinedVisible = false">取 消</el-button>
            </span>
          </el-dialog>
        </div>
        <!-- 指定范围的选人弹窗 -->
        <div class="user-defined-select">
          <el-dialog title='选择成员' :visible.sync='userDefinedSelect' class='candidateBox candidateBox2 employeeRadio'>
            <div class="header">
                <!-- <div class="left"><span class="active">下一环节审批人</span>
                </div> -->
            </div>
            <div class="content">
                <div class="left">
                  <ul>
                    <li class="userDefinedItem" v-for="(item,index) in userDefinedList" :key="index" @click="activePeople(index,item)">
                      <span class="tree-expand">
                        <span class="tree-label">
                            <img class="userPicture"  v-if="item.picture!==''" :src="item.picture+'&TOKEN='+token"/>
                            <div v-if="item.picture===''" class="simpName backImage">{{item.employee_name | limitTo(-2)}}</div>
                          <span>{{item.employee_name}}</span>
                        </span>
                        <i class="iconfont" :class="'icon-pc-member-sign'+ ((index === itemChecked) ? '-ok':'') + ' ' + 'emp'"></i>
                      </span>
                    </li>
                  </ul>
                </div>
            </div>
            <div slot='footer' class='dialog-footer'>
                <el-button @click="userDefinedSelect = false">取 消</el-button>
                <el-button type="primary" @click="userDefinedSure()">确 定</el-button>
            </div>
          </el-dialog>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import mailUeditor from './mail-ueditor'
import tool from '@/common/js/tool.js'
import Comment from '@/common/components/comment'/** 评论组件 */
import approvalGraph from '@/frontend/approval/approval-graph' /** 流程 */
import ModuleList from '@/frontend/module/module-list'
import {HTTPServer, baseURL} from '@/common/js/ajax.js'
import employeeRadio from '@/common/components/employee-radio'/** 成员单选 */
export default {
  name: 'EpistolizeMain',
  components: {mailUeditor, ModuleList, employeeRadio, approvalGraph, Comment},
  data () {
    return {
      FocusContent1: '',
      FocusContent2: '',
      FocusContent3: '',
      previewUrl: 'http://192.168.1.188:8081/custom-gateway/library/file/downloadWithoutRecord?type=1&TOKEN=' + (JSON.parse((sessionStorage.requestHeader))).TOKEN,
      value1: new Date(),
      value4: '',
      checked1: false,
      checked2: false,
      EmailFlowId: '',
      promptMessage: '',
      options: [],
      judgeCompile: false,
      flag: false,
      checked3: false,
      checked4: false,
      title: '',
      dataContent: '',
      checked5: false,
      folderAdminsetForm: false,
      commentDatas: {},
      checked6: false,
      judgeRecentContacts: '',
      Cut1: true, // 取消抄送人
      Cut2: false, // 取消密送
      Cut3: false, // 群发单显
      rotate: false,
      judgeCloses1: false,
      judgeCloses2: false,
      judgeCloses3: false,
      Cut: true,
      copyTo: '取消抄送',
      blindCarbonCopy: '密送',
      massTexting: '群发单显',
      monochrome: false,
      proson: '',
      form: {},
      jsondata: {},
      filenavData: [],
      filenavDataTitle: [],
      filenavDataContent: [],
      filenavDataContent1: [],
      filenavDataJudge1: true,
      filenavDataJudge2: false,
      filenavDataJudge3: false,
      sendData: {},
      seleteForm: false,
      seleteForm1: false,
      seleteForm2: false,
      seleteForm3: false,
      controlStyle: 2,
      commentDatasId: '',
      pageSize: 20,
      pageNum: 1,
      tableTotal: 0,
      judgeData: '',
      tableList: [],
      moduleTableList: [],
      input5: '',
      multipleSelection: '',
      num: '',
      ids: '',
      addressBook0: [], // 收件人
      addressBook1: [], // 抄送人
      addressBook2: [], // 密送人
      addressers: [],
      checkAddressers: {}, // 发件人
      boxShow: false,
      schedule: true,
      fileData: [],
      initialize: '',
      result: '',
      addSignData: '', // 发送给富文本组件的名片数据
      keepRepeating: '',
      options2: [],
      options3: [],
      value2: '',
      value3: '',
      bean: {},
      beanArr: [],
      navId: '',
      judgeWhetherComplete: false,
      styleId: '',
      chinese_name2: '',
      chinese_name3: '',
      idsArr: '',
      transmitBean: '',
      judgmentValue: '',
      arr: [],
      getProcessTypeForMail: '', // 邮件审批-流程类型
      employeeRadioDatas: {}, // 成员单选
      cuibanVisible: false, // 单选弹窗
      dataOne: [], // 单人控件(审批人)
      dataOneForccTo: [], // 单人控件(抄送人)
      personnelApprover: '', // 邮件审批-选中审批人
      userDefinedSelect: false, // 指定范围弹窗
      userDefinedVisible: false, // 选人弹窗(自定义)
      userDefinedList: [], // 指定审批人
      token: '',
      judgeCount: [],
      isCcTo: '', // 邮件审批-是否需要抄送人
      itemChecked: 999, // 选中的人 - 自定义选人弹窗
      personnelCcTo: '', // 邮件审批-选中的抄送人
      addressBook0Length: 0 // addressBook0的长度
    }
  },
  mounted () {
    var RouterObject = this.$router.history.current.query
    this.navId = RouterObject.navId
    this.judgeData = RouterObject.judgeData
    this.sendData.single_show = 0
    this.sendData.bcc_setting = 0
    this.sendData.cc_setting = 0
    // 获取最近联系人、绑定的发件人邮箱、签名
    this.queryRegisterUserList()
    this.$bus.off('editorContents')
    this.$bus.off('listenToInbox')
    this.$bus.on('editorContents', (value) => {
      this.form.content = value
      this.$refs.richtextAddSign.toBeText(this.checked3) // 设置纯文本
      // if (this.checked3) { // 纯文本
      //   this.$refs.richtextAddSign.toBeText(this.checked3) // 设置纯文本
      // }
    })
    // 路由点击重新获取列表
    this.$bus.on('listenToInbox', (value) => {
      this.pageNum = 1
      this.pageSize = 20
      // 给列表组件复位用
      this.$bus.emit('noRecordListRequest')
      this.queryRegisterUserList()
    })
    this.$bus.on('checkedData', (bean, value) => {
      this.idsArr = value
      this.transmitBean = bean
    })
    /** 成员单选 */
    this.$bus.on('selectMemberRadio', (value) => {
      if (value.prepareKey === '通过') {
        this.dataOne = value.prepareData
      }
    })
    /** 多选集合窗口 */
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value.prepareKey === '抄送') {
        this.dataOneForccTo = value.prepareData
      }
    })
  },
  watch: {
    $route (to, from) {
      this.navId = to.query.navId
      this.judgeCompile = false
      this.dataContent = ''
      this.addressBook0 = []
      this.addressBook1 = []
      this.addressBook2 = []
      this.sendData = {}
      this.fileData = []
      this.value1 = new Date()
      this.bean = {}
      this.beanArr = []
      this.tableList = []
      this.judgeCount = []
    },
    seleteForm1 () {
      if (!this.seleteForm1) {
        this.value1 = new Date()
      }
    },
    seleteForm2 () {
      // 从文件库中选择附件弹窗
      if (!this.seleteForm2) {
        // 清空已选中的文件
        this.filenavDataContent = []
        this.filenavDataContent1 = []
      } else {
        // 如果选择的是文件夹,则文件内容复位,避免点击弹窗确定时依然可以调用接口
        this.fileContent = []
      }
    }
  },
  methods: {
    // 打开定时发送弹窗 如果是存草稿调用 data传true
    openSendOntime (data) {
      // 非空验证
      this.SendParameterConversion(data)
      if (this.judgeWhetherComplete) {
        this.seleteForm1 = true
      }
    },
    hideSendMenu () {
      this.boxShow = false
    },
    // 文件图标
    showFileIcon (fileTypeData) {
      return 'iconfont ' + tool.determineFileType(fileTypeData).icon
    },
    // 判断是否JSON字符串
    isJSON (str) {
      if (typeof str === 'string') {
        try {
          JSON.parse(str)
          return true
        } catch (e) {
          console.log(e)
          return false
        }
      }
      console.log('It is not a string!')
    },
    // 使用签名单选框点击
    useSign () {
      console.log(this.checked6, 'this.checked6')
      if (this.checked6) {
        // 如果勾选了使用签名遍历this.options,获取默认签名,直接使用默认的签名
        this.options.some((item, index) => {
          // 有默认签名时
          if (item.signature_default === '0') {
            this.addSignData = this.options[index].content
            this.value4 = this.options[index].id
            // this.pleaceHolder = this.options[index].title
            return true
          } else {
            // 无默认签名
            this.value4 = ''
            // this.pleaceHolder = '请选择'
          }
        })
      } else {
        // 取消勾选 使用签名,则去掉签名显示
        // this.value4 = ''
        // if (this.isJSON(this.addSignData)) {
        //   // 名片签名
        //   this.addSignData = '{}'
        // } else {
        //   // 文本签名
        //   this.addSignData = ''
        // }
      }
    },
    // 从路由获取数据
    getRouterData () {
      this.judgeCompile = true
      if (!(this.navId !== undefined)) {
        if (this.$router.history.current.query.account === undefined) {
          this.dataContent = JSON.parse(sessionStorage.EmailData || '{}')
        } else {
          // 企信调用写信接口
          this.dataContent = {
            'from_recipient': this.$router.history.current.query.account,
            'from_recipient_name': this.$router.history.current.query.nickname
          }
        }
        this.reply()
      }
    },
    // 选人控件(审批人)
    openemployeeForm (type, prepareKey) {
      this.$bus.emit('commonMember', {'prepareData': this.dataOne, 'prepareKey': prepareKey, 'seleteForm': true, 'banData': [], 'type': type, 'navKey': '1'})
    },
    // 选人控件(抄送人)
    openemployeeFormForCcTo (type, prepareKey) {
      this.$bus.emit('commonMember', {'prepareData': this.dataOneForccTo, 'prepareKey': prepareKey, 'seleteForm': true, 'banData': [], 'navKey': '1'})
    },
    // 最近联系人、绑定的邮箱、签名
    queryRegisterUserList () {
      // 发件人请求
      HTTPServer.queryPersonnelAccount({}, 'Loading').then((res) => {
        this.addressers = res
        for (var i = 0; i < this.addressers.length; i++) {
          // 获取默认的账号
          if (this.addressers[i].account_default === '1') {
            this.checkAddressers = this.addressers[i]
          }
        }
        // 如果返回的列表没有默认账号,则取列表第一个
        if (JSON.stringify(this.checkAddressers) === '{}') {
          this.checkAddressers = this.addressers[0] || {}
        }
        this.form.to_recipients = this.checkAddressers.account
        // 最近联系人列表请求
        HTTPServer.getEmailContactsList({}, 'Loading').then((res) => {
          this.proson = res
          // 签名列表请求
          var jsondata = {'pageSize': 10, 'pageNum': 1, 'status': 0}
          HTTPServer.mailSignatureQueryList(jsondata, 'Loading').then((res) => {
            this.options = res.dataList
            for (var i = 0; i < this.options.length; i++) {
              if (this.options[i].signature_default === '0') {
                this.initialize = this.options[i].id
                this.value4 = this.options[i].id
              }
            }
            // 从缓存获取邮件数据
            this.getRouterData()
          })
        })
      })
    },
    // 取消抄送人
    copyTo1 () {
      this.Cut1 = !this.Cut1
      this.copyTo = this.copyTo === '抄送' ? '取消抄送' : '抄送'
    },
    // 取消密送人
    copyTo2 () {
      this.Cut2 = !this.Cut2
      this.blindCarbonCopy = this.blindCarbonCopy === '密送' ? '取消密送' : '密送'
    },
    // 纯文本触发
    text () { // 纯文本
      setTimeout(() => {
        this.$refs.richtextAddSign.toBeText(this.checked3) // 设置纯文本
      }, 10)
    },
    // 群发单显
    copyTo3 () {
      this.massTexting = this.massTexting === '群发单显' ? '取消群发单显' : '群发单显'
      if (this.massTexting === '取消群发单显') {
        this.addressBook0Length = this.addressBook0.length
        this.sendData.single_show = 1
        console.log(this.sendData, 'this.sendData.single_show = 1')
        // 使用群发单显时 抄送人addressBook1/密送addressBook2/收件人addressBook0(都是数组) 合并至群发单显中
        if (this.Cut1) {
          this.addressBook0 = this.addressBook0.concat(this.addressBook1)
        }
        if (this.Cut2) {
          this.addressBook0 = this.addressBook0.concat(this.addressBook2)
        }
        console.log(this.addressBook0, 'this.addressBook0')
        // 遍历this.addressBook0 将里面的item改为字符串(以便去重)
        let arrForaddressBook0 = []
        this.addressBook0.map((item, index) => {
          arrForaddressBook0.push(JSON.stringify(item))
        })
        // 数组去重
        arrForaddressBook0 = Array.from(new Set(arrForaddressBook0))

        // 去重之后,把里面的item解析回来
        let arrLast = []
        arrForaddressBook0.map((item, index) => {
          arrLast.push(JSON.parse(item))
        })
        this.addressBook0 = arrLast
        this.Cut = !this.Cut // 收件人的显示与否
        this.Cut3 = !this.Cut3// 群发单显
        this.Cut2 = false // 密送
        this.Cut1 = false // 抄送
        this.copyTo = '抄送'
        this.blindCarbonCopy = '密送'
      } else {
        // 取消群发单显
        this.sendData.single_show = 0
        // 判断抄送人/密送人是否有值
        // 有值的则显示
        // 群发单显去重(去掉抄送人/密送人里面的值)
        console.log(this.addressBook0, 'addressBook0++++')
        this.addressBook0 = this.addressBook0.splice(0, this.addressBook0Length)
        console.log(this.addressBook0, 'this.addressBook0')
        // if (this.addressBook1.length > 0) {
        //   // 群发单显去重(抄送人数组)
        //   this.addressBook0.map((item, index) => {
        //     this.addressBook1.map((val, i) => {
        //       if (item.mail_address === val.mail_address) {
        //         this.addressBook0.splice(index, 1)
        //         console.log(this.addressBook0)
        //       }
        //     })
        //   })
        // }
        // if (this.addressBook2.length > 0) {
        //   // 群发单显去重(密送人数组)
        //   this.addressBook0.map((item, index) => {
        //     this.addressBook2.map((val, i) => {
        //       if (item.mail_address === val.mail_address) {
        //         this.addressBook0.splice(index, 1)
        //         console.log(this.addressBook0)
        //       }
        //     })
        //   })
        // }
        console.log(this.sendData, 'this.sendData.single_show = 0')

        this.Cut = !this.Cut // 收件人的显示与否
        this.Cut3 = !this.Cut3// 群发单显
        if (this.addressBook2.length > 0) {
          this.Cut2 = !this.Cut2 // 密送
          this.blindCarbonCopy = '取消密送'
        }
        if (this.addressBook1.length > 0) {
          this.Cut1 = !this.Cut1 // 抄送
          this.copyTo = '取消抄送'
        }
      }
    },
    // 点击li给里面的input焦点
    clickFocus (index) {
      var INPUT = document.getElementsByClassName('addAccessory')[index]
      INPUT.focus()
    },
    // 动态添加宽度
    changeValue (e) {
      // e.target.style.width = 10 + e.target.nextElementSibling.offsetWidth + 'px'
    },
    // input失去焦点事件
    changeCount (e, dataId) {
      var EMAIL = new RegExp('^[a-zA-Z0-9][a-zA-Z0-9_\\.]+[a-zA-Z0-9]@[a-z0-9]{2,7}(\\.[a-z]{2,3}){1,3}$')
      var data = e.target.value
      this.judgeRecentContacts = dataId
      if (data.length < 50) {
        if (data !== '') {
          if (EMAIL.test(data.trim())) {
            if (dataId === 0) {
              // 收件人
              this.RecentContacts(this.addressBook0, data, '')
            } else if (dataId === 1) {
              // 抄送人
              this.RecentContacts(this.addressBook1, data, '')
            } else if (dataId === 2) {
              // 密送人
              this.RecentContacts(this.addressBook2, data, '')
            }
          } else {
            this.$message({
              message: '邮箱格式不正确',
              type: 'warning'
            })
          }
        }
      } else {
        this.$message({
          message: '邮箱格式超出限制',
          type: 'warning'
        })
      }
      // e.target.style.width = 10 + 'px'
      e.target.value = ''
      this.FocusContent1 = ''
      this.FocusContent2 = ''
      this.FocusContent3 = ''
    },
    // 监听输入空格
    listenSpaceKey (event, dataId) {
      // console.log(event.target.value.indexOf(' '))
      if (event.keyCode === 32) {
        // 再判断是否有空格出来
        if (!(event.target.value.indexOf(' ') === -1)) {
          // alert('有空格出来')
          this.changeCount(event, dataId)
        }
      }
    },
    // 富文本添加"回复"字段
    replyForEditor () {
      var userName = this.dataContent.from_recipient_name === undefined ? '' : this.dataContent.from_recipient_name
      var timeData3 = '<p>回复：' + tool.formatDate(this.dataContent.create_time, 'yyyy-MM-dd  HH:mm:ss') + '&nbsp;&nbsp;&nbsp;&nbsp;<span>' + userName + '</span>&nbsp;&nbsp;&nbsp;<span>' + this.dataContent.from_recipient + '</span>&nbsp;&nbsp;&nbsp;写道：</p>'
      this.$refs.richtextAddSign.addReply(timeData3, this.dataContent.mail_content) // 富文本
    },
    // 回复
    reply () {
      this.form.id = this.dataContent.id
      this.commentDatasId = this.dataContent.id
      this.EmailFlowId = this.dataContent.process_instance_id
      // 紧急状态
      this.checked1 = this.dataContent.is_emergent === '1'
      // 签名状态(待后台返回字段) todo
      // var userName = this.dataContent.from_recipient_name === undefined ? '' : this.dataContent.from_recipient_name
      if (this.judgeData === 'sendEmail') { // 发邮件
        this.addressBook0[0] = {'mail_address': this.dataContent.from_recipient, 'name': this.dataContent.from_recipient_name} // 收件人
      } else if (this.judgeData === 'sendEmailAddressee') { // 已发送详情的发邮件
        this.addressBook0[0] = {'mail_address': this.dataContent.to_recipients[0].mail_account, 'name': this.dataContent.to_recipients[0].employee_name} // 收件人
      } else if (this.judgeData === 'Forwarded') { // 回复全部
        this.checkAddressers = {'nickname': this.dataContent.to_recipients[0].employee_name, 'account': this.dataContent.to_recipients[0].mail_account, 'id': this.dataContent.account_id} // 发件人
        this.addressBook0[0] = {'mail_address': this.dataContent.from_recipient, 'name': this.dataContent.from_recipient_name} // 收件人
        for (var j = 0; j < this.dataContent.cc_recipients.length; j++) {
          this.addressBook1[j] = {'mail_address': this.dataContent.cc_recipients[j].mail_account, 'name': this.dataContent.cc_recipients[j].employee_name} // 抄送
        }
        for (var k = 0; k < this.dataContent.bcc_recipients.length;) {
          this.addressBook2[k] = {'mail_address': this.dataContent.bcc_recipients[k].mail_account, 'name': this.dataContent.bcc_recipients[k].employee_name} // 密送
        }
        if (this.dataContent.subject.indexOf('Re:') === -1) {
          this.sendData.subject = 'Re:' + this.dataContent.subject // 主题
        } else {
          this.sendData.subject = this.dataContent.subject // 主题
        }
        // var timeData2 = '<p>回复：' + tool.formatDate(this.dataContent.create_time, 'yyyy-MM-dd  HH:mm:ss') + '&nbsp;&nbsp;&nbsp;&nbsp;<span>' + userName + '</span>&nbsp;&nbsp;&nbsp;&nbsp;<span>&#60;' + this.dataContent.from_recipient + '&#62;</span>&nbsp;&nbsp;写道：</p>'
        // this.$refs.richtextAddSign.addReply(timeData2, this.dataContent.mail_content) // 富文本
        // 富文本添加回复字段
        this.replyForEditor()
        this.form.content = this.dataContent.mail_content
      } else if (this.judgeData === 'AttachmentsForwarded') { // 回复全部(带附件)
        // if (typeof (this.dataContent.from_recipient) === 'object') {
        // 1.已删除
        if (this.dataContent.mail_box_id === 4) {
          // 如果是已删除详情点击回复过来的,需处理收件人/发件人问题 (与收件箱详情发过来的处理逻辑相反) todo
          // 收发件人翻转处理
          this.dataContent.from_recipient = this.dataContent.to_recipients
          // this.dataContent.to_recipients = []
          // 键名更换
          var arrTemp = []
          this.dataContent.from_recipient.map((item, index) => {
            let obj = {}
            obj.mail_address = item.mail_account
            obj.name = item.employee_name
            arrTemp.push(obj)
          })
          this.addressBook0 = arrTemp
          // todo 这里有可能出现多个回复人,和小帅确认样式后再遍历数组拼接成字符串
          // 富文本添加回复字段
          this.replyForEditor()
        } else {
          // 2.非已删除详情点击回复过来的
          // 收件人需处理
          this.checkAddressers = {'nickname': this.dataContent.nickname, 'account': this.dataContent.to_account, 'id': this.dataContent.account_id} // 发件人
          this.addressBook0[0] = {'mail_address': this.dataContent.from_recipient, 'name': this.dataContent.from_recipient_name} // 收件人
          // 富文本添加回复字段
          this.replyForEditor()
        }
        for (var a = 0; a < this.dataContent.cc_recipients.length; a++) {
          this.addressBook1[a] = {'mail_address': this.dataContent.cc_recipients[a].mail_account, 'name': this.dataContent.cc_recipients[a].employee_name} // 抄送
        }
        for (var b = 0; b < this.dataContent.bcc_recipients.length; b++) {
          this.addressBook2[b] = {'mail_address': this.dataContent.bcc_recipients[b].mail_account, 'name': this.dataContent.bcc_recipients[b].employee_name} // 密送
        }
        if (this.dataContent.subject.indexOf('Re:') === -1) {
          this.sendData.subject = 'Re:' + this.dataContent.subject // 主题
        } else {
          this.sendData.subject = this.dataContent.subject // 主题
        }

        // // 富文本回复处理
        // var timeData3 = '<p>回复：' + tool.formatDate(this.dataContent.create_time, 'yyyy-MM-dd  HH:mm:ss') + '&nbsp;&nbsp;&nbsp;&nbsp;<span>' + userName + '</span>&nbsp;&nbsp;&nbsp;&nbsp;<span>&#60;' + this.dataContent.from_recipient + '&#62;</span>&nbsp;&nbsp;写道：</p>'

        // this.$refs.richtextAddSign.addReply(timeData3, this.dataContent.mail_content) // 富文本

        this.form.content = this.dataContent.mail_content
      } else if (this.judgeData === 'transmit') { // 转发
        // 收件箱的转发,发件人特殊处理
        if (this.dataContent.mail_box_id === 1) {
          this.checkAddressers = {'nickname': this.dataContent.nickname, 'account': this.dataContent.to_account, 'id': this.dataContent.account_id} // 发件人
        }
        if (this.dataContent.subject.indexOf('Fw:') === -1) {
          this.sendData.subject = 'Fw:' + this.dataContent.subject // 主题
        } else {
          this.sendData.subject = this.dataContent.subject // 主题
        }
        var toRecipients = ''
        for (var i = 0; i < this.dataContent.to_recipients.length; i++) {
          toRecipients += '<span>&nbsp;' + this.dataContent.to_recipients[i].mail_account + '&nbsp;</span>'
        }
        var timeData4 = '<p>转发：<span>' + this.dataContent.subject + '</span><p>发件人：&nbsp;' + this.dataContent.from_recipient + '&nbsp;</p><p>时间：' + tool.formatDate(this.dataContent.create_time, 'yyyy-MM-dd  HH:mm:ss') + '</p><span>收件人：' + toRecipients + '</span></p><br>'
        this.$refs.richtextAddSign.addReply(timeData4, this.dataContent.mail_content) // 富文本
        this.form.content = this.dataContent.mail_content
      } else if (this.judgeData === 'againCompile') { // 草稿箱的编辑
        if (Number(this.dataContent.single_show) === 1) {
          this.copyTo3() // 写信的群发单显
        }
        this.checkAddressers = {'nickname': this.dataContent.from_recipient_name, 'account': this.dataContent.from_recipient, 'id': this.dataContent.account_id} // 发件人
        for (var v = 0; v < this.dataContent.to_recipients.length; v++) {
          // todo
          this.addressBook0[v] = {'mail_address': this.dataContent.to_recipients[v].mail_account, 'name': this.dataContent.to_recipients[v].employee_name} // 收件人
        }
        for (var n = 0; n < this.dataContent.cc_recipients.length; n++) {
          this.addressBook1[n] = {'mail_address': this.dataContent.cc_recipients[n].mail_account, 'name': this.dataContent.cc_recipients[n].employee_name} // 抄送
        }
        for (var m = 0; m < this.dataContent.bcc_recipients.length; m++) {
          this.addressBook2[m] = {'mail_address': this.dataContent.bcc_recipients[m].mail_account, 'name': this.dataContent.bcc_recipients[m].employee_name} // 密送
        }
        this.sendData.subject = this.dataContent.subject // 主题
        this.$refs.richtextAddSign.addReply(1, this.dataContent.mail_content) // 富文本
        this.form.content = this.dataContent.mail_content
      } else if (this.judgeData === 'againCompileSend') { // 再次编辑发送
        // 发送时需要去掉id(因为再次编辑发送是生成新邮件)
        delete this.dataContent.id
        if (Number(this.dataContent.single_show) === 1) {
          this.copyTo3() // 写信的群发单显
        }
        this.checkAddressers = {'nickname': this.dataContent.from_recipient_name, 'account': this.dataContent.from_recipient, 'id': this.dataContent.account_id} // 发件人
        if (this.judgeData.sendJudge !== 4) {
          for (var id = 0; id < this.dataContent.to_recipients.length; id++) {
            // todo
            this.addressBook0[id] = {'mail_address': this.dataContent.to_recipients[id].mail_account, 'name': this.dataContent.to_recipients[id].employee_name} // 收件人
          }
          for (var e = 0; e < this.dataContent.cc_recipients.length; e++) {
            this.addressBook1[e] = {'mail_address': this.dataContent.cc_recipients[e].mail_account, 'name': this.dataContent.cc_recipients[e].employee_name} // 抄送
          }
          for (var r = 0; r < this.dataContent.bcc_recipients.length; r++) {
            this.addressBook2[r] = {'mail_address': this.dataContent.bcc_recipients[r].mail_account, 'name': this.dataContent.bcc_recipients[r].employee_name} // 密送
          }
        }
        this.sendData.subject = this.dataContent.subject // 主题
        this.$refs.richtextAddSign.addReply(1, this.dataContent.mail_content) // 富文本
        this.form.content = this.dataContent.mail_content
      }
      if (this.judgeData !== 'sendEmail' && this.judgeData !== 'sendEmailAddressee') {
        this.result = this.dataContent.timer_task_time
      }
      if (this.judgeData !== 'Forwarded' && this.judgeData !== 'sendEmail' && this.judgeData !== 'sendEmailAddressee') {
        this.fileData = this.dataContent.attachments_name
        for (var is = 0; is < this.fileData.length; is++) {
          if (this.fileData[is].file_type !== '.png' || this.fileData[is].file_type !== '.jpg' || this.fileData[is].file_type !== 'png' || this.fileData[is].file_type !== 'jpg') {
            this.fileData[is].fileType = tool.determineFileType(this.fileData[is].file_type)
          }
        }
      }
    },
    // 流程
    flow () {
      this.title = '流程'
      var jsondata = {'moduleBean': 'mail_box_scope', 'processInstanceId': this.EmailFlowId}
      HTTPServer.getApprovalFlowList(jsondata, 'Loading').then((res) => {
        this.folderAdminsetForm = true
        this.$bus.emit('getApprovalGraphData', res)
      })
    },
    // 评论
    commentsEvent () {
      this.title = '评论'
      this.folderAdminsetForm = true
      this.commentDatas = {'id': this.commentDatasId, 'bean': 'email', 'getlist': true}
    },
    /** 上传文件 */
    beforeUpload (event) {
      var formdata = new FormData()
      formdata.append('userlogo', event.target.files[0])
      formdata.append('bean', 'user')
      event.target.value = ''
      HTTPServer.commonUpload(formdata, 'Loading').then((res) => {
        var fileSize = res[0]
        if (Math.ceil(fileSize.file_size / 1024) < 1024) {
          fileSize.reSize = Math.ceil(fileSize.file_size / 1024) + 'kb'
        } else if (Math.ceil(fileSize.file_size / 1024 / 1024) < 1024) {
          fileSize.reSize = Math.ceil(fileSize.file_size / 1024 / 1024) + 'MB'
        } else if (Math.ceil(fileSize.file_size / 1024 / 1024 / 1024) < 1024) {
          fileSize.reSize = Math.ceil(fileSize.file_size / 1024 / 1024 / 1024) + 'GB'
        }
        if (fileSize.file_type !== 'png' || fileSize.file_type !== 'jpg') {
          fileSize.fileType = tool.determineFileType(fileSize.file_type)
        }
        this.fileData.push(fileSize)
        console.log(this.fileData)
      })
      this.form.fileData = this.fileData
    },
    // 从文件库中添加附件确定
    addFileConfirm () {
      if (this.fileContent.id) {
        // 判断权限
        HTTPServer.queryEmailFileAuth({id: this.fileContent.id}).then((res) => {
          console.log(this.fileContent, 'this.fileContent')
          if (res.mailAuth === 0) {
            this.$message({
              message: '没有权限',
              type: 'warning'
            })
          } else if (res.mailAuth === 1) {
            // 文件拼接
            var content = this.fileContent
            var fileType = content.siffix.split('.')[1]
            var filetype = fileType
            var fileUrl = baseURL + 'library/file/download?id=' + content.id
            var fileSize = content
            if (Math.ceil(fileSize.size / 1024) < 1024) {
              fileSize = Math.ceil(fileSize.size / 1024) + 'kb'
            } else if (Math.ceil(fileSize.size / 1024 / 1024) < 1024) {
              fileSize = Math.ceil(fileSize.size / 1024 / 1024) + 'MB'
            } else if (Math.ceil(fileSize.size / 1024 / 1024 / 1024) < 1024) {
              fileSize = Math.ceil(fileSize.size / 1024 / 1024 / 1024) + 'GB'
            }
            if (fileType !== 'png' || fileType !== 'jpg') {
              fileType = tool.determineFileType(fileType)
            }
            var file = {'file_url': fileUrl, 'file_size': content.size, 'file_name': content.name, 'file_type': filetype, 'reSize': fileSize, 'fileType': fileType}
            console.log(this.fileContent, fileType, fileSize, file)
            this.fileData.push(file)
            this.seleteForm2 = false
          }
        })
      }
    },
    // 请求附件目录
    fileAddAccessory () {
      HTTPServer.queryfileCatalog({}, 'Loading').then((res) => {
        this.filenavData = res
        this.filenavDataJudge1 = true
        this.filenavDataJudge2 = false
        this.filenavDataJudge3 = false
        this.filenavDataTitle = []
        this.seleteForm2 = true
      })
    },
    // 从文件库中添加附件的请求加标题和模块文件
    selectLibrary (styleId, content) {
      this.styleId = styleId

      if (styleId === 2) {
        var jsondata = {'style': styleId, 'pageNum': this.pageNum, 'pageSize': this.pageSize}
        HTTPServer.queryAppFileList(jsondata, 'Loading').then((res) => {
          this.filenavDataJudge1 = false
          this.filenavDataJudge2 = false
          this.filenavDataJudge3 = true
          this.filenavDataContent1 = res.dataList
          console.log(this.filenavDataContent1, '1')
          this.filenavDataTitle.push(content)
        })
      } else {
        var jsondatas = {'style': styleId, 'pageNum': this.pageNum, 'pageSize': this.pageSize, 'sign': 2}
        HTTPServer.queryFileList(jsondatas, 'Loading').then((res) => {
          this.filenavDataJudge1 = false
          this.filenavDataJudge2 = true
          this.filenavDataJudge3 = false
          this.filenavDataContent = res.dataList
          console.log(this.filenavDataContent)
          this.filenavDataTitle.push(content)
        })
      }
    },
    // 从文件库中添加附件的请求文件或文件夹
    selectLibraryFile (id, content, sign) {
      if (sign === '0' && this.styleId !== 2) { //   文件夹
        var jsondata = {'style': this.styleId, 'pageNum': this.pageNum, 'pageSize': this.pageSize, 'id': id, 'sign': 2}
        HTTPServer.queryFilePartList(jsondata, 'Loading').then((res) => {
          this.filenavDataJudge1 = false
          this.filenavDataJudge2 = true
          this.filenavDataJudge3 = false
          this.filenavDataContent = res.dataList
          this.filenavDataTitle.push(content)
        })
        // 如果选择的是文件夹,则文件内容复位,避免点击弹窗确定时依然可以调用接口
        this.fileContent = []
      } else if (sign === '1') { //  文件
        // 文件拼接
        var className = document.getElementsByClassName('item-content')
        for (var l = 0; l < className.length; l++) {
          className[l].setAttribute('class', 'item-content')
        }
        this.fileContent = content
        document.getElementById(id).setAttribute('class', 'item-content addClass')
      }
    },
    // 模块附件
    selectModuleFile (id, content, sign) {
      // 如果选择的是文件夹,则文件内容复位,避免点击弹窗确定时依然可以调用接口
      this.fileContent = []
      if (sign === '0' && this.filenavDataTitle.length === 1) {
        var jsondatas = {'style': this.styleId, 'pageNum': this.pageNum, 'pageSize': this.pageSize, 'id': id, 'sign': 2}
        HTTPServer.queryModuleFileList(jsondatas, 'Loading').then((res) => {
          this.filenavDataJudge1 = false
          this.filenavDataJudge2 = false
          this.filenavDataJudge3 = true
          this.filenavDataContent1 = res.dataList
          this.filenavDataTitle.push(content)
        })
      } else if (sign === '0' && this.filenavDataTitle.length > 1) {
        var datas = {'style': this.styleId, 'pageNum': this.pageNum, 'pageSize': this.pageSize, 'id': id, 'sign': 2}
        HTTPServer.queryModulePartFileList(datas, 'Loading').then((res) => {
          this.filenavDataJudge1 = false
          this.filenavDataJudge2 = false
          this.filenavDataJudge3 = true
          this.filenavDataContent1 = res.dataList
          this.filenavDataTitle.push(content)
        })
      } else if (sign === '1' && content.siffix !== '') {
        // 文件拼接
        var className = document.getElementsByClassName('item-content')
        for (var i = 0; i < className.length; i++) {
          className[i].setAttribute('class', 'item-content')
        }
        this.fileContent = content
        document.getElementById(id).setAttribute('class', 'item-content addClass')
      }
    },
    // 选择标题返回
    selectFileName (e, content, id) {
      if (content === -1) {
        this.fileAddAccessory()
        this.filenavDataTitle = []
      } else if (this.styleId === 2) {
        if (this.filenavDataTitle[0].name === e.path[0].innerText) {
          this.filenavDataTitle = []
          this.selectLibrary(content.id, content)
        } else if (this.filenavDataTitle[1].name === e.path[0].innerText) {
          for (var j = 1; j < this.filenavDataTitle.length; j++) {
            this.filenavDataTitle.splice(j, this.filenavDataTitle.length)
          }
          this.selectModuleFile(content.model_id, content, '0')
        } else if (this.filenavDataTitle[2].name === e.path[0].innerText) {
          for (var k = 2; k < this.filenavDataTitle.length; k++) {
            this.filenavDataTitle.splice(k, (this.filenavDataTitle.length - k))
          }
          this.selectModuleFile(content.model_id, content, '0')
        } else {
          for (var p = id; p < this.filenavDataTitle.length; p++) {
            this.filenavDataTitle.splice(p, (this.filenavDataTitle.length - p))
          }
          this.selectModuleFile(content.model_id, content, '1')
        }
      } else {
        if (this.filenavDataTitle[0].name === e.path[0].innerText) {
          this.filenavDataTitle = []
          this.selectLibrary(this.styleId, content)
        } else if (this.filenavDataTitle[1].name === e.path[0].innerText) {
          for (var s = 1; s < this.filenavDataTitle.length; s++) {
            this.filenavDataTitle.splice(s, this.filenavDataTitle.length)
          }
          this.selectLibraryFile(content.id, content, '0')
        } else {
          for (var q = id; q < this.filenavDataTitle.length; q++) {
            this.filenavDataTitle.splice(q, (this.filenavDataTitle.length - q))
          }
          this.selectLibraryFile(content.id, content, '0')
        }
      }
    },
    // 附件删除
    deletedData (dataNumber) {
      this.fileData.splice(dataNumber, 1)
    },
    /** 列表选择 */
    handleSelectionChange (val) {
      this.multipleSelection = val
      console.log(this.multipleSelection)
    },
    /** 显示每页条数--选择联系人弹窗 */
    handleSizeChange (val) {
      this.pageSize = val
      // this.pageNum = 1
      // this.queryRegisterUserList()
      this.personnel(this.num)
    },
    /** 跳转第几页--选择联系人弹窗 */
    handleCurrentChange (val) {
      this.pageNum = val
      // this.queryRegisterUserList()
      this.personnel(this.num)
    },
    checkAddressersa (name) {
      this.boxShow = this.boxShow !== true
      this.checkAddressers = name
    },
    clickBoxShow () {
      this.boxShow = this.boxShow !== true
      this.rotate = this.rotate !== true
      this.form.to_recipients = this.checkAddressers
    },
    // 选择时间做的判断逻辑
    FDate (value) {
      this.result = new Date(value).getTime()
      var presentTimestamp = new Date().getTime()
      if (this.result > presentTimestamp) {
        this.schedule = false
      } else {
        this.$message.error('请不要选择已过期的时间')
        this.schedule = true
        this.value1 = new Date()
      }
    },
    // 选择联系人
    confirm () {
      // 非空验证
      if (this.multipleSelection.length < 1) {
        this.$message.error('请选择联系人')
        return false
      }
      this.seleteForm = false
      for (var i = 0; i < this.multipleSelection.length; i++) {
        if (this.num === 0) {
          this.RecentContacts(this.addressBook0, this.multipleSelection[i].mail_address, this.multipleSelection[i].name)
          this.form.addressBook = this.addressBook0
        } else if (this.num === 1) {
          this.RecentContacts(this.addressBook1, this.multipleSelection[i].mail_address, this.multipleSelection[i].name)
        } else if (this.num === 2) {
          this.RecentContacts(this.addressBook2, this.multipleSelection[i].mail_address, this.multipleSelection[i].name)
        }
      }
    },
    // 删除/收件人/密送/抄送人/密送人
    deleteAddressBook (arr, dataId) {
      for (var i = 0; i < this.judgeCount.length; i++) {
        if (arr[dataId].mail_address === this.judgeCount[i]) {
          this.judgeCount.splice(i, 1)
        }
      }
      arr.splice(dataId, 1)
    },
    // 签名的触发事件
    richtextAddSigns () {
      var dataContent = ''
      for (var i = 0; i < this.options.length; i++) {
        if (this.value4 === this.options[i].id) {
          dataContent = this.options[i].content
        }
      }
      this.addSignData = dataContent
    },
    // 取消1
    undoCloses1 () {
      console.log(this.judgeData, 'judgeData')
      console.log(this.navId, 'navId--取消')
      if (this.judgeData === 'AttachmentsForwarded' && this.navId === 0) {
        // 收件箱详情跳回列表
        this.$router.push({path: '/frontend/EmailMain/EmailInbox'})
      } else if (this.judgeData === 'againCompile') {
        // 草稿箱详情跳回列表
        this.$router.push({path: '/frontend/EmailMain/EmailDrafts'})
      } else if (this.judgeData === 'againCompileSend') {
        // 发件箱详情跳回列表
        this.$router.push({path: '/frontend/EmailMain/EmailSent'})
      } else if (this.judgeData === 'AttachmentsForwarded' && this.navId === 3) {
        // 已删除详情跳回列表
        this.$router.push({path: '/frontend/EmailMain/EmailDeleted'})
      } else if (this.judgeData === 'transmit') {
        if (this.navId === 0) {
          // 收件箱详情跳回列表
          this.$router.push({path: '/frontend/EmailMain/EmailInbox'})
        } else if (this.navId === 1) {
          // 发件箱详情跳回列表
          this.$router.push({path: '/frontend/EmailMain/EmailSent'})
        } else if (this.navId === 3) {
          // 已删除详情跳回列表
          this.$router.push({path: '/frontend/EmailMain/EmailDeleted'})
        }
      } else {
        this.$router.go(-1)
      }
      this.$bus.emit('ControlsColorNavigationBar', this.navId)
    },
    // 取消2
    undoCloses2 () {
      // 企信过来的,直接返回即可
      if (this.$router.history.current.query.account !== undefined) {
        this.$router.go(-1)
        return
      }
      if (this.judgeData !== 'againCompile') {
        this.$bus.emit('listenToUpPage', this.dataContent)
      } else {
        this.$router.go(-1)
      }
    },
    // 点击弹出联系人弹框 - 从通讯录中选择 - 获取列表数据
    personnel (num) {
      var jsondata = {'pageNum': this.pageNum, 'pageSize': this.pageSize}
      HTTPServer.mailCatalogqueryList(jsondata, 'Loading').then((res) => {
        this.tableList = res.dataList
        // 打开选择联系人弹窗
        this.seleteForm = true
        this.num = num
        this.controlStyle = 2
        var pageInfo = res.pageInfo
        this.pageSize = pageInfo.pageSize
        this.pageNum = pageInfo.pageNum
        this.tableTotal = pageInfo.totalRows
      })
    },
    // 删除操作的各个请求
    completelyCancel (judge) {
      var jsondata = {'id': this.dataContent.id}
      if (judge === 1) {
        HTTPServer.EmailDeleteCancel(jsondata, 'Loading').then((res) => {
          this.$router.go(-1)
          this.$message({
            message: '删除成功',
            type: 'success'
          })
          // 刷新草稿箱总数
          this.$bus.emit('listenToGetTitleCount', true)
        })
      } else if (judge === 2) {
        HTTPServer.EmailCompletelyCancel(jsondata, 'Loading').then((res) => {
          this.$router.go(-1)
          this.$message({
            message: '彻底删除成功',
            type: 'success'
          })
          // 刷新草稿箱总数
          this.$bus.emit('listenToGetTitleCount', true)
        })
      }
      this.seleteForm3 = false
    },
    // 弹出框
    dedtermineButton (n, prompt) {
      this.promptMessage = prompt
      this.seleteForm3 = true
      this.judgmentValue = n
    },
    // 弹出框的确定按钮
    deleteConfirm () {
      if (this.judgmentValue === 1) {
        this.completelyCancel(1)
      } else if (this.judgmentValue === 2) {
        this.completelyCancel(2)
      }
      this.dialogVisible = false
    },
    // 预览
    previewParticulars () {
      this.form.subject = this.sendData.subject // 标题
      this.form.from_recipient = {'account': this.checkAddressers.account, 'nickname': this.checkAddressers.nickname} // 发件人
      if (this.addressBook0 !== []) {
        this.form.to_recipients = []
        for (var j = 0; j < this.addressBook0.length; j++) {
          this.form.to_recipients.push({'employee_name': this.addressBook0[j].name, 'mail_account': this.addressBook0[j].mail_address})
        } // 收件人
      }

      if (this.addressBook1 !== []) {
        this.form.cc_recipients = []
        for (var s = 0; s < this.addressBook1.length; s++) {
          this.form.cc_recipients.push({'employee_name': this.addressBook1[s].name, 'mail_account': this.addressBook1[s].mail_address})
        } // 抄送人
      }

      if (this.addressBook2 !== []) {
        this.form.bcc_recipients = []
        for (var a = 0; a < this.addressBook2.length; a++) {
          this.form.bcc_recipients.push({'employee_name': this.addressBook2[a].name, 'mail_account': this.addressBook2[a].mail_address})
        } // 密送人
      }

      if (this.Cut3) {
        this.form.massTexting = true
      } else {
        this.form.massTexting = false
      }
      this.form.mail_content = this.form.content // 内容
      if (this.fileData) {
        this.form.attachments_name = this.fileData // 附件
      }
      sessionStorage.particularsContent = JSON.stringify(this.form)
      // 只使用了 this.checkAddressers.nickname和this.checkAddressers.account
      window.open(window.location.origin + '#/Emailpreview?from_recipient=' + JSON.stringify(this.checkAddressers))
      sessionStorage.removeItem('particularsContent')
    },
    // Cut1: true, // 取消抄送人
    // Cut2: false, // 取消密送
    // 判断是否 取消抄送人 取消密送
    isCloseCcto () {
      if (!this.Cut1) {
        // 取消抄送人
        this.jsondata.cc_recipients = []
      }
      if (!this.Cut2) {
        // 取消密送
        this.jsondata.bcc_recipients = []
      }
    },
    // 存草稿
    saveDraft () {
      this.SendParameterConversion(true)
      delete this.jsondata.id
      // 判断是否 取消抄送人 取消密送
      this.isCloseCcto()
      if (this.judgeWhetherComplete) {
        HTTPServer.EmailSaveDraft(this.jsondata, 'Loading').then((res) => {
          this.$router.push({path: '/frontend/EmailMain/EmailDrafts'})
          this.$bus.emit('ControlsColorNavigationBar', 2) // 2是草稿箱在导航条的位置
          this.judgeWhetherComplete = false
          this.$message({
            message: '存草稿成功',
            type: 'success'
          })
          // 刷新草稿箱总数
          this.$bus.emit('listenToGetTitleCount')
        })
      }
    },
    // 编辑存草稿
    compileSaveDraft () {
      this.SendParameterConversion(true)
      delete this.jsondata.personnel_approverBy
      delete this.jsondata.personnel_ccTo
      // 判断是否 取消抄送人 取消密送
      this.isCloseCcto()
      HTTPServer.compileSaveDraft(this.jsondata, 'Loading').then((res) => {
        this.$router.go(-1)
        this.$message({
          message: '存草稿成功',
          type: 'success'
        })
        // 刷新草稿箱总数
        this.$bus.emit('listenToGetTitleCount')
      })
    },
    // 发送/定时发送
    send (approvalTag) {
      this.SendParameterConversion(false)
      console.log(this.jsondata, 'jsondata')
      // 需要邮件的id就是在草稿箱发送的接口。没有id就是在写信发送的接口
      // delete this.jsondata.id
      if (this.flag) {
        if (!this.seleteForm1) {
          this.result = ''
          this.jsondata.timer_task_time = ''
        }
        // 判断是否 取消抄送人 取消密送
        this.isCloseCcto()
        HTTPServer.EmailSendTransmission(this.jsondata, 'Loading').then((res) => {
          if (this.jsondata.personnel_approverBy === '') {
            if (this.result === '') {
              this.$router.push({path: '/frontend/EmailMain/EmailSent'})
              this.$bus.emit('ControlsColorNavigationBar', 1)
              this.$message({
                message: '发送成功',
                type: 'success'
              })
            } else if (this.result !== '') {
              this.$router.push({path: '/frontend/EmailMain/EmailDrafts'})
              this.$bus.emit('ControlsColorNavigationBar', 2)
              this.$message({
                message: '定时发送成功',
                type: 'success'
              })
            } else {
              this.$router.go(-1)
            }
            this.$bus.emit('listenToGetTitleCount', true)
          } else {
            this.undoCloses1()
          }
          // 需要审批的邮件,加个提示
          if (approvalTag) {
            this.$message({
              message: '您的邮件已进入审批环节，请在审批模块查看',
              type: 'success'
            })
          }
        })
      }
    },
    // 回复的发送接口
    interfaceSend () {
      this.SendParameterConversion(false)
      if (this.flag) {
        if (!this.seleteForm1) {
          this.jsondata.timer_task_time = ''
        }
        // 判断是否 取消抄送人 取消密送
        this.isCloseCcto()
        HTTPServer.EmailInterfaceSend(this.jsondata, 'Loading').then((res) => {
          this.$router.push({path: '/frontend/EmailMain/EmailSent'})
          this.$bus.emit('ControlsColorNavigationBar', 1)
          this.$message({
            message: '回复成功',
            type: 'success'
          })
        })
      }
    },
    // 转发的发送接口
    forwardingSend () {
      this.SendParameterConversion(false)
      if (this.flag) {
        if (!this.seleteForm1) {
          this.jsondata.timer_task_time = ''
        }
        // 判断是否 取消抄送人 取消密送
        this.isCloseCcto()
        HTTPServer.EmailForwardingSend(this.jsondata, 'Loading').then((res) => {
          this.$router.push({path: '/frontend/EmailMain/EmailSent'})
          this.$bus.emit('ControlsColorNavigationBar', 1)
          this.$message({
            message: '转发成功',
            type: 'success'
          })
        })
      }
    },
    // 发送的参数转换(非空验证)
    SendParameterConversion (drafts) {
      var judge = false
      if (drafts) {
        // 存草稿无需非空验证
        judge = true
        // if (this.addressBook0.length) {
        //   judge = true
        // } else {
        //   this.$message.error('收件人不能为空')
        //   // 清空定时的值
        //   this.result = ''
        // }
      } else {
        if (this.checkAddressers.account !== '' && this.checkAddressers.account !== undefined) {
          if (this.addressBook0.length) {
            if (this.sendData.subject !== '' && this.sendData.subject !== undefined) {
              if (this.form.content !== '' && this.form.content !== undefined) {
                judge = true
              } else {
                this.$message.error('邮件内容不能为空')
                // 清空定时的值
                this.result = ''
              }
            } else {
              this.$message.error('主题不能为空')
              // 清空定时的值
              this.result = ''
            }
          } else {
            this.$message.error('收件人不能为空')
            // 清空定时的值
            this.result = ''
          }
        } else {
          this.$message.error('发件人不能为空')
        }
      }
      if (judge) {
        var recipients = [] // 收件人
        var copyRecipients = [] // 抄送人
        var setRecipient = [] // 密送人
        // var fileAccessory = []  // 附件
        for (var i = 0; i < this.addressBook0.length; i++) {
          recipients[i] = {employee_name: this.addressBook0[i].name, mail_account: this.addressBook0[i].mail_address}
        } // 收件人

        for (var j = 0; j < this.addressBook1.length; j++) {
          copyRecipients[j] = {employee_name: this.addressBook1[j].name, mail_account: this.addressBook1[j].mail_address}
        } // 抄送人

        for (var a = 0; a < this.addressBook2.length; a++) {
          setRecipient[a] = {employee_name: this.addressBook2[a].name, mail_account: this.addressBook2[a].mail_address}
        } // 密送人
        if (this.initialize !== this.value4.id) {
          this.sendData.signature_id = this.value4.id // 签名ID
        } else {
          this.sendData.signature_id = this.initialize // 默认的签名ID
        }
        if (this.copyTo === '取消抄送' && copyRecipients.length) {
          this.sendData.cc_setting = 1
        } else {
          this.sendData.cc_setting = 0
        }
        if (this.blindCarbonCopy === '取消密送' && setRecipient.length) {
          this.sendData.bcc_setting = 1
        } else {
          this.sendData.bcc_setting = 0
        }
        this.jsondata = {
          'id': this.dataContent.id,
          'personnel_ccTo': this.personnelCcTo,
          'personnel_approverBy': this.personnelApprover,
          'is_emergent': this.checked1 ? 1 : 0,
          'is_notification': this.checked2 ? 1 : 0,
          'is_plain': this.checked3 ? 1 : 0,
          'is_encryption': this.checked4 ? 1 : 0,
          'is_track': this.checked5 ? 1 : 0,
          'is_signature': this.checked6 ? 1 : 0,
          'mail_source': 0,
          'account_id': this.checkAddressers.id,
          'from_recipient': this.checkAddressers.account,
          'from_recipient_name': this.checkAddressers.nickname,
          'mail_content': this.form.content,
          'to_recipients': recipients, // 收件人数组
          'cc_recipients': copyRecipients,
          'bcc_recipients': setRecipient,
          'cc_setting': this.sendData.cc_setting,
          'bcc_setting': this.sendData.bcc_setting,
          'single_show': this.sendData.single_show || 0,
          'subject': this.sendData.subject,
          'attachments_name': this.fileData,
          'signature_id': this.sendData.signature_id,
          'timer_task_time': this.result
        }
        this.judgeWhetherComplete = true
        this.flag = true
        delete this.jsondata.data4
        delete this.jsondata.selectReply
        delete this.jsondata.sendState
        delete this.jsondata.SendToMeNumber
        delete this.jsondata.judgeId
      }
    },
    // 发送邮件之前,先判断审批类型再显示相应弹窗
    sendBefore () {
      // 点击发送按钮时判断流程类型
      if (this.getProcessTypeForMail === 1) {
        // 自由流程
        this.cuibanVisible = true
      } else if (this.getProcessTypeForMail === 0 && (this.userDefinedList.length > 0)) {
        // 固定流程且有指定审批人
        // 弹指定审批人选的弹窗
        // this.userDefinedSelect = true
        this.userDefinedVisible = true
      } else if (this.getProcessTypeForMail === 2) {
        // 没有设流程,直接执行发送请求
        this.send()
      } else if (this.isCcTo === 1 && this.getProcessTypeForMail === 0) {
        // 需要弹窗选择抄送人
        this.cuibanVisible = true
      } else {
        // 固定流程 直接发送即可
        this.send(true)
      }
    },
    // 选人弹窗,确认操作 todo
    sendSure () {
      // 下一环节审批人(必填项),已选人赋值给this.personnelApprover,再执行发送请求
      // if (this.dataOne.length < 1 && this.getProcessTypeForMail !== 0) {
      // 固定流程且需要指定审批人的,需要做判断(不是说固定流程就无需选人)
      if (this.dataOne.length < 1 && (this.getProcessTypeForMail === 0 && (this.userDefinedList.length > 0))) {
        this.$message.error('审批人不能为空')
        return false
      }
      // 固定流程无需选人(但是固定流程且需要指定审批人的清空需要选人)
      if (this.getProcessTypeForMail !== 0 || (this.getProcessTypeForMail === 0 && (this.userDefinedList.length > 0))) {
        // 审批人
        this.personnelApprover = this.dataOne[0].id
      }
      let arr = []
      this.dataOneForccTo.map((value, index) => {
        arr.push(value.id)
      })
      // 抄送人
      this.personnelCcTo = arr.join(',')
      this.cuibanVisible = false
      this.send(true)
      this.dataOne = [] // this.dataOne复位
      this.dataOneForccTo = [] // this.dataOneForccTo复位
    },
    // 添加最近联系人到发送人
    addRecent (data) {
      if (this.judgeRecentContacts !== '') {
        if (this.judgeRecentContacts === 0) {
          this.RecentContacts(this.addressBook0, data.mail_account, data.employee_name)
        } else if (this.judgeRecentContacts === 1) {
          this.RecentContacts(this.addressBook1, data.mail_account, data.employee_name)
        } else if (this.judgeRecentContacts === 2) {
          this.RecentContacts(this.addressBook2, data.mail_account, data.employee_name)
        }
      } else {
        this.$message({
          message: '请选择需要添加的位置',
          type: 'warning'
        })
      }
    },
    // 自定义选人
    activePeople (index, item) {
      // dataOne排他
      this.dataOne = []
      this.itemChecked = index
      // 将选中的人加进dataOne
      item.name = item.employee_name
      this.dataOne.push(item)
      console.log(this.dataOne, 'this.dataOne自定义')
    },
    // 自定义选人确定
    userDefinedSure () {
      // 关闭自定义选人弹窗
      this.userDefinedSelect = false
    },
    // 自定义选人确定发送数据 todo
    // userDefinedSendData () {
    //   if (this.dataOne.length < 1) {
    //     this.$message.error('审批人不能为空')
    //     return false
    //   }

    //   this.personnelApprover = this.dataOne[0].id
    //   this.send(1)
    //   this.dataOne = [] // this.dataOne复位
    // },
    // 选择最近联系人的去重方法
    // arr 已选的邮箱地址数组
    // mailAccount 正在添加进来的邮箱号
    // employeeName 正在添加进来的邮箱名(同上)
    RecentContacts (arr, mailAccount, employeeName) {
      // var arrCount = []
      var addData = {'mail_address': mailAccount, 'name': employeeName}
      var repeat = false
      var id = 0
      // 遍历 已选的邮箱地址数组
      for (var j = 0; j < arr.length; j++) {
        // 判断是否已存在
        if (mailAccount === arr[j].mail_address) {
          repeat = true
          id = j
        }
      }
      // 判断邮箱是否在arr存在
      if (repeat) {
        // 邮箱已存在的
        arr.splice(id, 1, arr[id])
        this.$message({
          message: '邮箱已重复',
          type: 'warning'
        })
      } else {
        // 邮箱不存在
        arr.push(addData)
        // this.judgeCount.push(mailAccount)
        // for (var i = 0; i < this.judgeCount.length;) {
        //   var count = 0
        //   for (var w = i; w < this.judgeCount.length; w++) {
        //     if (this.judgeCount[i] === this.judgeCount[w]) {
        //       count++
        //     }
        //   }
        //   arrCount.push({
        //     date: this.judgeCount[i],
        //     count: count
        //   })
        //   i += count
        // }
        // for (var k = 0; k < arrCount.length; k++) {
        //   if (arrCount[k].count > 1 && mailAccount === arrCount[k].date) {
        //     this.$message({
        //       message: '邮箱已重复',
        //       type: 'warning'
        //     })
        //     arr.splice(arr.length - 1, 1)
        //     this.judgeCount.splice(k, 1)
        //   }
        // }
      }
    },
    // 从模块中选择联系人
    selectModules () {
      HTTPServer.SelectContactFromModule({}, 'Loading').then((res) => {
        this.options2 = res
        for (var i = 0; i < this.options2.length; i++) {
          this.beanArr.push(this.options2[i])
        }
        this.chinese_name2 = this.options2[0].chinese_name
        this.value2 = this.options2[0].id
        this.bean = this.beanArr[0].english_name
        this.slideComment(this.options2[0].id)
      })
    },
    // 获取模块分类的子菜单的公共请求方法
    slideComment (moduleId) {
      var jsondata = {'moduleId': moduleId}
      HTTPServer.CommonRequestMethodSubmenusModuleClassification(jsondata, 'Loading').then((res) => {
        this.options3 = res
        this.chinese_name3 = this.options3[0].name
        this.value3 = this.options3[0].name
        this.controlStyle = 1
      })
    },
    // 选择模块时发生的改变
    modules (moduleId) {
      for (var i = 0; i < this.beanArr.length; i++) {
        if (this.beanArr[i].id === moduleId) {
          this.bean = this.beanArr[i].english_name
        }
      }
      console.log(moduleId, this.bean)
      this.slideComment(moduleId)
      this.classify(this.ids)
    },
    // 选择分类时发生的改变
    classify (menuId) {
      let value = {bean: this.bean, menuId: menuId}
      this.$bus.emit('refreshList', value)
    },
    // 选择模块时获取到联系人的Email
    confirmEmail () {
      this.seleteForm = false
      var contentData = []
      var jsondata = {'ids': this.idsArr.toString(), 'bean': this.transmitBean}
      HTTPServer.selectModuleGetEmailContact(jsondata, 'Loading').then((res) => {
        contentData = res
        for (var i = 0; i < contentData.length; i++) {
          for (var j = 0; j < contentData[i].email_fields.length; j++) {
            if (this.num === 0) {
              this.RecentContacts(this.addressBook0, contentData[i].email_fields[j].value, '')
            } else if (this.num === 1) {
              this.RecentContacts(this.addressBook1, contentData[i].email_fields[j].value, '')
            } else if (this.num === 2) {
              this.RecentContacts(this.addressBook2, contentData[i].email_fields[j].value, '')
            }
          }
        }
        this.form.addressBook = this.addressBook0
      })
    },
    // 获取邮件审批流程类型
    getProcessType () {
      HTTPServer.getProcessTypeForMail({moduleBean: 'mail_box_scope'}).then((res) => {
        this.getProcessTypeForMail = res.processType
        this.userDefinedList = res.choosePersonnel || []
        this.isCcTo = res.ccTo
      })
    }
  },
  created () {
    console.log(this.navId, 'this.navId')
    console.log(this.judgeData, 'this.judgeData')
    this.getProcessType()
    this.token = (JSON.parse((sessionStorage.requestHeader))).TOKEN
  },
  beforeDestory () {
    this.$bus.off('listenToList')
    this.$bus.off('listenToDeleteButton')
  },
  updated () {
    console.log(this.fileData, 'this.fileData')
  }
}
</script>
<style lang="scss" scoped>
.epistolize-main-wrap{
  height: 100%;
  overflow: hidden;
  .upTime{
          color:rgb(0, 143, 229);
          cursor: pointer;
          font-size: 15px;
        }
    .delete, .green{
            display: inline-block;
            text-align: center;
            line-height: 34px;
            width: 95px;
            padding: 0 10px;
            // float: left;
            border-radius: 5px;
            margin-right: 10px;
            border: 1px solid #ccc;
            color: #69696C;
            cursor: pointer;
          }
          .green{
            background: #409EFF;
            color: #fff;
            border:none;
            line-height: 34px;
          }
    .replay{
      overflow: auto;
      height: 100%;
      overflow-x: hidden;
        .title{
        padding:12px 0;
        border-bottom: 1px solid #ccc;
        overflow: auto;
        .approveComment{
          float: right;
          margin-right:20px;
          .commentBox{
              display: inline-block;
              float: right;
              margin-top: 10px;
              color:#999;
              margin-left: 20px;
              cursor:pointer;
            }
            .commentBox:hover{
              color:#409EFF;
            }
            .commentUpbox{
              height: 100%;
              width: 100%;
              position: absolute;
              z-index: 1000;
              left: 0;
              top: 0;
              background: rgba(0, 0, 0, 0.4);
              .commentContentBox{
                    float: right;
                    width: 350px;
                    height: 100%;
                    background-color: #fff;
                  .commentTitle{
                      padding: 10px 0px;
                      border-bottom: 1px solid #E7E7E7;
                      .boxTitle{
                        border-left: 4px solid #409EFF;
                        span{
                            font-size: 20px;
                            margin-left: 20px;
                            line-height: 35px;
                        }
                        .el-icon-close{
                            float: right;
                            line-height: 35px;
                            font-size: 23px;
                            margin-right: 10px;
                            cursor:pointer;
                        }
                      }
                  }

                }
            }
          }
      }
      .left{
        width: 73%;
        float: left;
        ul{
          margin-bottom:12px;
          position: relative;
          // 收件人/发件人输入框
          li{
            border-bottom: 1px solid #ccc;
            padding: 5px 0;
            overflow: hidden;
            position: relative;
            span.titleList{
              float: left;
              width: 70px;
              display: block;
              height: 30px;
              line-height: 30px;
            }
            .left{
              float: left;
              width: 50%;
              color:#888;
              line-height: 30px;
              span, i{
                cursor: pointer;
              }
              .nickname{
                color:#000;
              }
              .rotate,.rotate1{
                transition: all 0.3s;
                transform: rotate(-180deg);
              }
              ul{
                position: absolute;
                border: 1px solid #D9D9D9;
                box-shadow: 0 1px 4px 0 rgba(0,0,0,0.20);
                border-radius: 4px;
                width: 300px;
                padding: 10px 0;
                top: 45px;
                background-color: #fff;
                  li{
                  padding: 3px 0;
                  border-bottom: none;
                  line-height: 30px;
                  padding-left:20px;
                  cursor: pointer;
                }
                li:hover{
                  background-color: #409EFF;
                  color:#fff;
                }
              }

            }
            .right1{
              float: right;
              line-height: 30px;
              span{
                color:#999;
                cursor: pointer;
              }
              span:hover{
                color:#409EFF;
              }
            }
            .contacts{
                background: #E6F3FC;
                border: 1px solid #BDE0F9;
                border-radius: 100px;
                display: inline-block;
                line-height: 25px;
                padding: 0 10px;
                overflow: hidden;
                text-overflow:ellipsis;
                white-space: nowrap;
                color: #999;
                float: left;
                margin: 2px;
              span{
                color:#000;
              }
            }
            input.addAccessory{
              // width: 10px;
              background-color: #fff;
              padding-left:0px;
              border:none;
              position: absolute;
              float: right;
            }
            input.subject{
              background-color: #fff;
              padding-left:0px;
              border:none;
              width: calc(100% - 70px);
              height: 30px;
            }
            .icon-qunfa{
                    font-size:20px;
                    cursor:pointer;
                    float: right;
                    margin-right: 20px;
                    position: absolute;
                    right: 0;
                    bottom: 11px;
                  }
          }
        }
        .richtext {
          padding-bottom: 20px;
        }
        .checkboxs{
          margin-top: 10px;
          margin-bottom:13px;
          width: 850px;
        }
      }
      .right{
        width: 20%;
        float: right;
        height:835px;
        border: 1px solid #ccc;
        background: #FAFAFA;
        border-top: none;
        .RecentContacts{
          text-align: center;
          font-size: 16px;
          padding:  10px 0;
          // width: 80%;
          // margin-left: 10%;
          border-bottom:1px solid #ccc;
          // margin-bottom:10px;
        }
        ul{
          overflow: auto;
          height: calc(100% - 42px);
          li{
            padding: 8px 0 8px 20px;
            cursor: pointer;
            span.headPortrait{
                  width: 36px;
                  height: 36px;
                  display: inline-block;
                  border-radius: 50%;
                  font-size: 17px;
                  text-align: center;
                  line-height: 32px;
                  color: #409EFF;
                  border: 2px solid #409EFF;
                  margin-right: 10%;
                  float: left;
            }
            .right-box{
              width:60%;
              display: inline-block;
              .Name{
                height:15px;
                line-height: 15px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }
            }
          }
          li:hover{
            background: #F2F2F2;
          }
        }
      }
    }
    .accessory{
          position: relative;
          margin-bottom: 10px;
        input{
          width: 90px;
          position: absolute;
          left: 0;
          top: 0;
          opacity: 0;
        }
        .accessoryStyle-box {
          overflow: hidden;
          margin-top: 8px;
          .accessoryStyle{
            padding: 10px 0;
            background-color: #F2F2F2;
            border-radius: 4px;
            padding: 5px 4px;
            margin: 5px 0;
            line-height: 40px;
            overflow: hidden;
            width: 31.33333%;
            float: left;
            margin-right: 15px;
            img{
              display: inline-block;
              width: 50px;
              height: 50px;
              margin-right:5px;
              float: left;
            }
            .accessoryStyle-name {
              overflow: hidden;
              text-overflow:ellipsis;
              white-space: nowrap;
              max-width: calc(100% - 172px);
              float: left;
            }
          }
        }
    }
    .copyToStyle, .blindCarbonCopyStyle, .massTextingStyle{
      color: #409EFF !important ;
    }
    .header{
      padding-top: 20px;
      height: 50px;
      span{
        border: 1px solid #409EFF;
        color: #409EFF;
        padding:5px 8px;
        margin-left: 20px;
        cursor: pointer;
        border-radius: 5px;
      }
      span.control{
        background-color: #409EFF;
        color:#fff;
      }
    }
    .Pagination{
        overflow: auto;
        border-top: 1px solid #e6ebf5;
        padding-top: 10px;
        .el-pagination{
          float: right;
        }
    }
    .icon-renyuanjieshao{
      cursor: pointer;
    }
    .popUpBox{
      .titles{
        padding: 15px 30px;
        border-bottom:1px solid #eee;
        span{
          font-size: 16px;
          cursor: pointer;
        }
        :last-child {
          >span {
            color: #409EFF;
          }
        }
        .fileName:hover, .documentLibrary:hover{
          color: #409EFF;
        }
      }
      .content{
        background: #fff;
        .item-nav,.item-content{
          line-height: 60px;
          padding: 0 30px;
          border-bottom:1px solid #eee;
          i.iconfont{
            margin-right: 20px;
            font-size:25px;
          }
        }
        .item-nav:hover,.item-content:hover{
          background-color: #F2F2F2;
        }
        .addClass{
          background-color: #eee;
        }
        .item-content{
          overflow: hidden;
          img{
            width: 40px;
            height: 40px;
            float: left;
            margin: 10px 10px 0 0;
          }
          .file-title {
            overflow: hidden;
            text-overflow:ellipsis;
            white-space: nowrap;
            float: left;
            width: 300px;
          }
          .folder {
            float: left;
          }
        }
        .nameOmit{
          overflow: hidden;
          text-overflow:ellipsis;
          white-space: nowrap;
        }
      }
      .linkman{
        height: 485px;
        overflow: auto;
      }
      .footer{
        float: right;
        margin: 30px;
      }
    }
}
</style>
<style lang="scss">
.epistolize-main-wrap{
  .popUpBox .title .icon-shezhilie{
      display: none;
    }
    .popUpBox .candidateBox .content .el-icon-arrow-right{
      margin: 0;
      font-size: 12px;
    }
    .popUpBox .el-container .el-checkbox-group{
      overflow: hidden;
    }
    .popUpBox .el-container .el-pagination{
      position: relative !important;
    }
    .popUpBox .el-container .el-checkbox-group .list.border-list{
      border:none !important;
    }
    .popUpBox .el-container .el-checkbox-group{
      height: 350px !important;
    }
    .popUpBox .selectModule .el-container .el-checkbox-group{
      overflow:auto;
    }

    // 弹框公共样式
    // @import '../../../common/scss/dialog.scss';
    // 单人组件样式
    @import '../../../common/scss/employee.scss';
    // 选人控件
    .empitem{
      margin-bottom: 20px;
      float: left;
      .empitem-item {
        float: left;
        margin-left: 16px;
        margin-bottom: 10px;
        a{
          margin-right: 20px;
          float: left;
          margin-top: 10px;
        }
        .simpName{
          height: 36px;
          width: 36px;
          float: left;
          line-height: 36px;
          text-align: center;
          background: #409EFF;
          color: #fff;
          margin: 0 10px auto 0px;
          border-radius: 50%;
          font-size: 12px;
        }
        .fullName {
          line-height: 37px;
          font-size: 16px;
          color: #17171A;
          position: relative;
          .empitem-tag {
                color: #F94C4A;
                position: absolute;
                font-size: 12px;
                line-height: 12px;
                top: -5px;
                right: -11px;
            &:hover{
              cursor: pointer;
            }
          }
        }
      }
      .icon {
        float: left;
      }
    }
    // 选人弹框
    .cuiban {
      .el-dialog {
        width: 540px;
        .el-dialog__title {
          font-size: 16px;
        }
        .el-dialog__footer {
          padding: 50px 16px 10px 16px;
        }
        .el-dialog__body {
          line-height: 0;
          padding: 0;
        }
        // 抄送内容
        .chaoson-content,.approver {
          width: 500px;
          padding: 9px 13px;
          margin: 20px 9px 20px 0;
          position: relative;
          overflow: hidden;
          padding-left: 102px;
          .title {
            display: inline-block;
            font-size: 14px;
            color: #4A4A4A;
            margin-top: 18px;
            position: absolute;
            left: 44px;

          }
          .icon {
            display: inline-block;
            width: 36px;
            height: 36px;
            border-radius: 50%;
            margin-left: 16px;
            text-align: center;
            line-height: 36px;
            background-color:  #ECEFF1;
            &:hover{
              cursor: pointer;
            }
          }
          .next {
            font-size: 14px;
            color: #4A4A4A;
            line-height: 19px;
            margin-right: 12px;
            position: absolute;
            left: 19px;
            .tag {
              color: red;
            }
          }
          .next-other {
            .tag {
              color: red;
            }
            font-size: 14px;
            color: #4A4A4A;
            line-height: 19px;
            position: absolute;
            left: 19px;
            top: 17px;
          }
          .icons {
            margin-left: 10px;
          }
        }
        .rule {
          height: 30px;
          font-size: 14px;
          color: #4A4A4A;
          margin: 24px 0px 2px 20px;
          .el-radio{
            margin-left: 14px;
          }
        }
        .el-select {
          margin-left: 16px;
        }
      }
    }
    // 指定列表的选人组件
    .user-defined-select {
      .userDefinedItem {
        overflow: hidden;
        line-height: 38px!important;
        border-bottom: none!important;
        .simpName {
          margin-left: 14px;
        }
        .userPicture {
          height: 30px;
          width: 30px;
          float: left;
          margin: 4px 10px auto 0px;
          margin-left: 14px;
          border-radius: 50%;
        }
        .emp {
          float: right;
          margin-right: 20px;
        }
        &:hover {
          cursor: pointer;
          background-color:rgba(240, 240, 240, 240);
        }
      }
      .active {
        border:none;
        padding:0;
      }
      .content {
        .left {
          >ul {
            > li {
              padding: 0;
            }
          }
        }
      }
    }
}

</style>
