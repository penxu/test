<template>
  <div class="library-mian main">
    <el-container>
      <el-aside width="220px">
        <header>文件库</header>
        <el-input placeholder="搜索文件" v-model="searchFileName" @keyup.enter.native="searchFile"><i slot="prefix" class="el-input__icon el-icon-search"></i></el-input>
        <div v-for="(item,index) in filenavData" :key="index" class="item-nav" :class="item.className" @click="fileTypeNav($event, item)">
          <i class="iconfont" :class="'icon-pc-paper-nav' + item.id"></i><span>{{item.name}}</span>
        </div>
      </el-aside>
      <el-main :class="seachTable ? 'seachTable': ''">
        <header v-if="!seachTable"><i class="iconfont" :class="'icon-pc-paper-nav' + navObject.id"></i>
        <div id="urlBox" class="name">
          <span v-for="(furl, index) in folderUrl" :key="index" @click="jumpFolder(furl)">
          <i class="el-icon-arrow-right" v-if="index != 0"></i>{{furl.label || furl.name}}</span>
        </div>
          <a v-if="(navObject.id == 1 && !parentObject.id) || (navObject.id == 1 && parentObject.is_manage == 1) || navObject.id == 3" href="javascript:;" class="addfolder" @click="openAddfolderForm">添加文件夹</a>
          <form v-if="(navObject.id == 1 && parentObject.id && parentObject.upload == 1) || navObject.id == 3" id="uploadfile0" class="uploadForm"><a class="text">上传文件</a> <input type="file" name="fileList"  @change="beforeUpload($event)"></form>
        </header>
        <header v-if="seachTable"><i class="iconfont icon-pc-paper-seek"></i><span class="name">搜索文件库</span></header>
        <el-table :data="tableData3" @row-click='rowDblclick' v-if="tableData3.length > 0">
          <el-table-column label="文件名称" width="240">
            <template slot-scope="scope">
              <i v-if="scope.row.type != 1 && scope.row.sign == 0 && navObject.id != 2" class="iconfont icon-pc-paper-file folder" :style="{color: scope.row.color}"></i>
              <i v-if="scope.row.type == 1 && scope.row.sign == 0 && navObject.id != 2" class="iconfont icon-pc-private-folder folder" :style="{color: scope.row.color}"></i>
              <i v-if="scope.row.sign == 1 && scope.row.fileType.fileType != 'img'" :class=" scope.row.name | ressuffix" class="folder"></i>
              <i v-if="navObject.id == 2 && scope.row.sign == 0" class="iconfont icon-pc-paper-file folder" :style="{color: scope.row.color}"></i>
              <img v-if="scope.row.sign == 1 && scope.row.fileType.fileType == 'img'" :src="previewUrl + '&id=' + scope.row.id + '&time=' + ((new Date()).getTime())"
                 onerror="javascript:this.src='/static/img/img-err.png';" />
              <span class="name" v-html="traverseSearchName(scope.row.name)"></span>
            </template>
          </el-table-column>
          <el-table-column label="大小" width="100">
            <template slot-scope="scope">{{ scope.row.size | fileSize('-') }}</template>
          </el-table-column>
          <el-table-column label="最后更新时间" width="150">
            <template slot-scope="scope">{{ scope.row.create_time | formatDate('yyyy-MM-dd HH:mm') }}</template>
          </el-table-column>
          <el-table-column prop="employee" label="更新人" width="200" v-if="navObject.id != 5">
            <template slot-scope="scope">{{ scope.row.employee_name }}
              <a href="javascript:;" class="operation" v-if="navObject.id == 4 && !parentObject.id" @click="cancelSharedForm = true;setObject = scope.row">取消共享</a>
              <el-dropdown trigger="click" placement="bottom" @command="listSet" v-if="(navObject.id == 1 && parentObject.id && scope.row.sign == 0 && scope.row.is_manage == 1) || (navObject.id == 3 && scope.row.sign == 0)">
                <span class="el-dropdown-link">
                  <i class="iconfont icon-pc-paper-more operation" @click="setObject = scope.row"></i>
                </span>
                <el-dropdown-menu slot="dropdown" class="popoverSet foldersetItem">
                  <el-dropdown-item command="0" v-if="navObject.id == 1">管理员设置</el-dropdown-item>
                  <el-dropdown-item command="1">管理文件夹</el-dropdown-item>
                  <el-dropdown-item command="2">移动</el-dropdown-item>
                  <el-dropdown-item command="3">共享</el-dropdown-item>
                  <el-dropdown-item command="4">删除</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
              <i class="iconfont icon-pc-paper-instal operation" @click="openFolderAdminsetForm(scope.row)" v-if="!parentObject.id && navObject.id == 1 && scope.row.is_manage == 1"></i>
              <i v-if="scope.row.download == 1 || scope.row.is_manage == 1" class="iconfont icon-pc-paper-download operation" title="下载" @click="download(scope.row)"></i>
              <div style="display: none;">{{navObject}} {{scope.row}}</div>
            </template>
          </el-table-column>
          <el-table-column prop="employee" label="共享人" width="200" v-if="navObject.id == 5">
            <template slot-scope="scope">{{ scope.row.employee_name }}
              <a v-if="!parentObject.id" href="javascript:;" class="operation" @click="exitSharedForm = true;setObject = scope.row">退出共享</a>
              <i class="iconfont icon-pc-paper-download operation" title="下载" @click="download(scope.row)"></i>
              <div style="display: none;">{{navObject}}</div>
            </template>
          </el-table-column>
        </el-table>
        <div class="no-data" v-if="tableData3.length == 0">
          <img src="/static/img/no-data.png" />
          <p>抱歉～没有搜索到相关的内容～</p>
        </div>
        <el-pagination v-if="!seachTable" @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum'
          :page-sizes='[10, 20, 50, 100]' :page-size='pageSize' layout='total, sizes, prev, pager, next, jumper' :total='totalRows'></el-pagination>
      </el-main>
    </el-container>
    <div style="height: 0;">
      <el-dialog title='添加文件夹' :visible.sync='addfolderForm' class='addForm addfolderForm' :close-on-click-modal='false'>
        <el-form :model='form'>
          <el-form-item label='文件夹名称' :label-width='formLabelWidth' class="must">
            <el-input v-model='form.name' :maxlength='12'></el-input>
          </el-form-item>
          <el-form-item label='选择颜色' :label-width='formLabelWidth' class="colorbox">
            <el-row :gutter="20">
              <el-col :span="4"  v-for="color in colorData" :key="color.id">
                <div class="grid-content bg-purple" :style="{background: color.name}" @click="checkColor(color)">
                  <i v-if="color.checked" class="el-icon-check"></i>
                </div>
              </el-col>
            </el-row>
          </el-form-item>
          <el-form-item label='文件夹类型'  class="must" :label-width='formLabelWidth' v-if="navObject.id == 1 && !parentObject.id">
            <el-select v-model='form.classify' placeholder='公开（企业所有成员可见此文件夹）' @change="folderMemberData = []">
              <el-option label='公开（企业所有成员可见此文件夹）' value='0'></el-option>
              <el-option label='私有（只有加入成员可见此文件夹）' value='1'></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label='管理员' :label-width='formLabelWidth' class="member-item manage must" v-if="navObject.id == 1">
            <div class="member-box">
              <span v-for="(manage,index) in folderManageData" :key="index">
                <div v-if="!manage.picture" class="simpName backImage">{{manage.name | limitTo(-2)}}</div>
                <img v-if="manage.picture" :src="manage.picture + '&TOKEN=' + token" />
              </span>
              <img src="/static/img/add-member.png" class="add-member" @click="addFolderManage(1)" />
            </div>
          </el-form-item>
          <el-form-item label='成员' :label-width='formLabelWidth' class="member-item must"  v-if="navObject.id == 1 && !parentObject.id && form.classify == 1">
            <div class="member-box">
              <span v-for="(member,index) in folderMemberData" :key="index">
                <div v-if="!member.picture" class="simpName backImage">{{member.name | limitTo(-2)}}</div>
                <img v-if="member.picture" :src="member.picture + '&TOKEN=' + token" />
              </span>
              <span><img src="/static/img/add-member.png" class="add-member" @click="addFolderManage(2)" /></span>
            </div>
          </el-form-item>
        </el-form>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='addfolder'>确 定</el-button>
          <el-button @click='addfolderForm = false'>取 消</el-button>
        </div>
      </el-dialog>

    <div class="side-shade" v-if="folderAdminsetForm" @click="folderAdminsetForm = false"></div>
    <transition name="slide">
      <div class="particularsForm sideForm" v-if="folderAdminsetForm">
        <div class="side-body">
          <div class="folder-name">
            <i v-if="setObject.type == 0" class="iconfont icon-pc-paper-file folder" :style="{color: setObject.color}"></i>
            <i v-if="setObject.type == 1" class="iconfont icon-pc-private-folder folder" :style="{color: setObject.color}"></i>
            {{setObject.name}}
            <i class="iconfont icon-pc-paper-compil edit" @click="openeditfolderForm" v-if="setObject.is_manage == 1"></i>
            <i class="iconfont icon-pc-widget-small" @click="folderAdminsetForm = false"></i>
          </div>
          <p>文件夹类型</p>
          <div style="color: #4A4A4A;margin: 2px 0 0 0;">{{setObject.type == 0 ? '公开' : '私有'}}</div>
          <div class="line"></div>
          <a href="javascript:;" class="addadmin" @click="addFolderManage(4)"><i class="el-icon-plus"></i>添加管理员</a>
          <div class="admin-item" v-for="(manage, index) in folderManageData" :key="index">
            <img v-if="manage.picture" :src="manage.picture + '&TOKEN=' + token" />
            <div class="simpName" v-if="!manage.picture">{{manage.employee_name | limitTo(-2)}}</div>
            <span class="name">{{manage.employee_name }}</span>
            <span class="role">（根目录管理员）</span>
          </div>
          <div class="line"></div>
          <p class="role-nav"><span>成员</span><span style="margin-left: 62px;">权限</span><a href="javascript:;" @click="saveMemberRole">保存</a></p>
          <div class="role-list">
            <div>
              <div></div>
              <div></div>
              <div><el-checkbox v-model="uploadAllCheck" @change='allUpload'>全选</el-checkbox></div>
              <div><el-checkbox v-model="downloadAllCheck" @change='allDownload'>全选</el-checkbox></div>
            </div>
            <div v-for="(setItem, index) in folderMemberData" :key="index">
              <div>
                <img v-if="setItem.picture" :src="setItem.picture + '&TOKEN=' + token" />
                <div class="simpName" v-if="!setItem.picture">{{setItem.employee_name | limitTo(-2)}}</div>
              <span class="name">{{setItem.employee_name }}</span></div>
              <div><el-checkbox v-model="setItem.previewCheck" label="预览" disabled></el-checkbox></div>
              <div><el-checkbox v-model="setItem.uploadCheck" @change="memberCheck">上传</el-checkbox></div>
              <div><el-checkbox v-model="setItem.downloadCheck" @change="memberCheck">下载</el-checkbox></div>
            </div>
          </div>
          <a href="javascript:;" class="memberManage" @click="addFolderManage(5)" v-if="setObject.type == 1">管理成员</a>
          <a href="javascript:;" class="deleteFolder" @click="deletefolderForm = true">删除文件夹</a>
        </div>
      </div>
    </transition>
<!--
    <transition name="slide">
      <el-dialog title='文件夹右侧管理窗口' :visible.sync='folderAdminsetForm' class='particularsForm' top='0' width="360px">
        <div class="folder-name">
          <i v-if="setObject.type == 0" class="iconfont icon-pc-paper-file folder" :style="{color: setObject.color}"></i>
          <i v-if="setObject.type == 1" class="iconfont icon-pc-private-folder folder" :style="{color: setObject.color}"></i>
          {{setObject.name}}
          <i class="iconfont icon-pc-paper-compil edit" @click="openeditfolderForm" v-if="setObject.is_manage == 1"></i>
          <i class="iconfont icon-pc-widget-small" @click="folderAdminsetForm = false"></i>
        </div>
        <p>文件夹类型</p>
        <div style="color: #4A4A4A;margin: 2px 0 0 0;">{{setObject.type == 0 ? '公开' : '私有'}}</div>
        <div class="line"></div>
        <a href="javascript:;" class="addadmin" @click="addFolderManage(4)"><i class="el-icon-plus"></i>添加管理员</a>
        <div class="admin-item" v-for="(manage, index) in folderManageData" :key="index">
          <img v-if="manage.picture" :src="manage.picture + '&TOKEN=' + token" />
          <div class="simpName" v-if="!manage.picture">{{manage.employee_name | limitTo(-2)}}</div>
          <span class="name">{{manage.employee_name }}</span>
          <span class="role">（根目录管理员）</span>
        </div>
        <div class="line"></div>
        <p class="role-nav"><span>成员</span><span style="margin-left: 62px;">权限</span><a href="javascript:;" @click="saveMemberRole">保存</a></p>
        <div class="role-list">
          <div>
            <div></div>
            <div></div>
            <div><el-checkbox v-model="uploadAllCheck" @change='allUpload'>全选</el-checkbox></div>
            <div><el-checkbox v-model="downloadAllCheck" @change='allDownload'>全选</el-checkbox></div>
          </div>
          <div v-for="(setItem, index) in folderMemberData" :key="index">
            <div>
              <img v-if="setItem.picture" :src="setItem.picture + '&TOKEN=' + token" />
              <div class="simpName" v-if="!setItem.picture">{{setItem.employee_name | limitTo(-2)}}</div>
            <span class="name">{{setItem.employee_name }}</span></div>
            <div><el-checkbox v-model="setItem.previewCheck" label="预览" disabled></el-checkbox></div>
            <div><el-checkbox v-model="setItem.uploadCheck" @change="memberCheck">上传</el-checkbox></div>
            <div><el-checkbox v-model="setItem.downloadCheck" @change="memberCheck">下载</el-checkbox></div>
          </div>
        </div>
        <a href="javascript:;" class="memberManage" @click="addFolderManage(5)" v-if="setObject.type == 1">管理成员</a>
        <a href="javascript:;" class="deleteFolder" @click="deletefolderForm = true">删除文件夹</a>
      </el-dialog>
    </transition> -->
      <el-dialog title='管理文件夹' :visible.sync='editfolderForm' class='addForm addfolderForm' :close-on-click-modal='false'>
        <el-form :model='form'>
          <el-form-item label='文件夹名称' :label-width='formLabelWidth'>
            <el-input v-model='form.name' :maxlength='12'></el-input>
          </el-form-item>
          <el-form-item label='选择颜色' :label-width='formLabelWidth' class="colorbox">
            <el-row :gutter="20">
              <el-col :span="4"  v-for="color in colorData" :key="color.id">
                <div class="grid-content bg-purple" :style="{background: color.name}" @click="checkColor(color)">
                  <i v-if="color.checked" class="el-icon-check"></i>
                </div>
              </el-col>
            </el-row>
          </el-form-item>
        </el-form>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='editfolder'>确 定</el-button>
          <el-button @click='editfolderForm = false'>取 消</el-button>
        </div>
      </el-dialog>
      <el-dialog title='共享文件夹' :visible.sync='sharedfolderForm' class='sharedfolderForm'>
        <el-form :model='form'>
          <el-form-item label='共享成员' :label-width='formLabelWidth'>
            <i class="el-icon-circle-plus" @click="addFolderManage(3, sharedMemberData)"></i>
            <!-- <div><img src="http://www.runoob.com/images/pulpit.jpg" /><span class="name">彭华娣</span><i class="el-icon-circle-close"></i></div> -->
            <div v-for="(shared,index) in sharedMemberData" :key="index">
              <span class="simpName" v-if="!shared.picture">{{shared.name | limitTo(-2)}}</span>
              <img v-if="shared.picture" :src="shared.picture + '&TOKEN=' + token" />
              <span class="name">{{shared.name}}</span>
              <i class="el-icon-circle-close" @click="removeSharedMember(shared)"></i>
            </div>
          </el-form-item>
        </el-form>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='shareFileLibaray'>确 定</el-button>
          <el-button @click='sharedfolderForm = false'>取 消</el-button>
        </div>
      </el-dialog>
      <el-dialog title='删除文件夹' :visible.sync='deletefolderForm' class='deleteForm'>
        <div class="content">删除后此文件夹中的文件和子文件夹将会一并删除且不可恢复。</div>
        <div class="prompt">确定要删除吗？</div>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='deletefolder'>确 定</el-button>
          <el-button @click='deletefolderForm = false'>取 消</el-button>
        </div>
      </el-dialog>
      <el-dialog title='移动到...' :visible.sync='movefolderForm' class='addForm movefolderForm' :close-on-click-modal='false'>
        <el-form :model='form'>
          <el-form-item label='选择文件夹' :label-width='formLabelWidth'>
            <el-input v-model='form.directory.name' @focus="opendirectoryForm" readonly="readonly"></el-input>
          </el-form-item>
        </el-form>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='movefolder'>确 定</el-button>
          <el-button @click='movefolderForm = false'>取 消</el-button>
        </div>
      </el-dialog>
      <el-dialog title='文件夹目录' :visible.sync='directoryForm' class='directoryForm' :modal="false" :show-close="false">
        <el-tree v-if="directoryForm" ref="folderTree" :props='defaultProps' accordion :expand-on-click-node='false' lazy :load="loadFolderNode" node-key="id" @node-click="checkDirectory"></el-tree>
      </el-dialog>
      <el-dialog title='公开链接' :visible.sync='publicLinkForm' class='addForm openLinkForm'>
        <header>
          <div :class="sharedLink ? 'active': ''" @click="sharedCheck(0)">链接共享</div>
          <div :class="sharedEmail ? 'active': ''" @click="sharedCheck(1)">邮箱共享</div>
          <div :class="sharedQrCode ? 'active': ''" @click="sharedCheck(2)">二维码共享</div>
        </header>
        <div v-if="sharedLink">
          <el-form :model='form'>
            <el-form-item label='链接地址' :label-width='formLabelWidth'>
              <el-input v-model='form.link'></el-input>
            </el-form-item>
            <el-form-item label='访问密码' :label-width='formLabelWidth'>
              <el-input v-model='form.passWord'></el-input>
            </el-form-item>
            <el-form-item label='访问有效期' :label-width='formLabelWidth'>
              <el-input v-model='form.validity'></el-input>
            </el-form-item>
          </el-form>
        </div>
        <div v-if="sharedEmail">
          <el-form :model='form'>
            <div style="padding-left: 10px;">输入邮箱进行分享</div>
            <el-input placeholder="请输入内容" v-model="form.email" class="email">
              <el-button slot="append">分享</el-button>
            </el-input>
            <el-form-item label='访问有效期' :label-width='formLabelWidth'>
              <el-input v-model='form.validity'></el-input>
            </el-form-item>
          </el-form>
        </div>
        <div v-if="sharedQrCode">
          <el-form :model='form'>
            <div class="QrCode"><canvas id='qrcode2' style="width: 180px; height: 180px;"></canvas></div>
            <div style="text-align: center;margin: 10px 0 30px;">扫描上面的二维码完成分享</div>
            <el-form-item label='访问有效期' :label-width='formLabelWidth'>
              <el-input v-model='form.validity'></el-input>
            </el-form-item>
          </el-form>
        </div>
      </el-dialog>
      <el-dialog title='重命名' :visible.sync='editfileForm' class='addForm addfolderForm' :close-on-click-modal='false'>
        <el-form :model='form'>
          <el-form-item label='文件名称' :label-width='formLabelWidth'>
            <el-input v-model='form.name' :maxlength='20'></el-input>
          </el-form-item>
        </el-form>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='editfile'>确 定</el-button>
          <el-button @click='editfileForm = false'>取 消</el-button>
        </div>
      </el-dialog>
      <el-dialog title='复制到...' :visible.sync='copyfileForm' class='addForm movefolderForm'>
        <el-form :model='form'>
          <el-form-item label='选择文件夹' :label-width='formLabelWidth'>
            <el-input v-model='form.directory.name' @focus="opendirectoryForm" readonly="readonly"></el-input>
          </el-form-item>
        </el-form>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='copyfile'>确 定</el-button>
          <el-button @click='copyfileForm = false'>取 消</el-button>
        </div>
      </el-dialog>
      <el-dialog title='删除文件' :visible.sync='deletefileForm' class='deleteForm'>
        <div class="content">删除文件后历史版本也会被一并删除，且不可恢复。</div>
        <div class="prompt">确定要删除吗？</div>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='deletefile'>确 定</el-button>
          <el-button @click='deletefileForm = false'>取 消</el-button>
        </div>
      </el-dialog>
      <el-dialog title='取消共享' :visible.sync='cancelSharedForm' class='deleteForm'>
        <div class="content">取消共享后所有共享成员将不可见该文件夹或文件。</div>
        <div class="prompt">你确定要取消共享吗？</div>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='cancelShared'>确 定</el-button>
          <el-button @click='cancelSharedForm = false'>取 消</el-button>
        </div>
      </el-dialog>
      <el-dialog title='退出共享' :visible.sync='exitSharedForm' class='deleteForm'>
        <div class="content">退出共享后将不可见该文件夹或文件。</div>
        <div class="prompt">你确定要退出吗？</div>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='exitShared'>确 定</el-button>
          <el-button @click='exitSharedForm = false'>取 消</el-button>
        </div>
      </el-dialog>
      <file-dtail :fileObject='fileObject'></file-dtail>
    </div>
  </div>
</template>

<script>
import {HTTPServer, baseURL, ajaxGetRequest} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
import Comment from '@/common/components/comment'/** 评论组件 */
import FileDtail from '@/common/module-dtl/library-dtl'/** 评论组件 */
export default {
  name: 'Library',
  components: {Comment, FileDtail},
  data () {
    return {
      fileObject: {},
      employeeInfo: (JSON.parse(sessionStorage.userInfo)).employeeInfo,
      token: (JSON.parse((sessionStorage.requestHeader))).TOKEN,
      fileUrl: baseURL + 'library/file/download?TOKEN=' + (JSON.parse((sessionStorage.requestHeader))).TOKEN,
      previewUrl: baseURL + 'library/file/downloadWithoutRecord?type=1&TOKEN=' + (JSON.parse((sessionStorage.requestHeader))).TOKEN,
      bean: 'file_library',
      navObject: {'id': 1, 'name': '公司文件'},
      parentObject: {},
      folderUrl: [{'id': 1, 'name': '公司文件'}],
      filenavData: [{'id': 1, 'name': '公司文件', 'className': 'active'}, {'id': 2, 'name': '应用文件'}, {'id': 4, 'name': '我共享的'}, {'id': 5, 'name': '与我共享'}],
      searchFileName: '',
      seachTable: false,
      tableData3: [],
      pageSize: 20,
      pageNum: 1,
      totalRows: 50,
      formLabelWidth: '97px',
      addfolderForm: false,
      colorData: [{'id': 1, 'name': '#F9B239', 'checked': true}, {'id': 2, 'name': '#FF1D32'}, {'id': 3, 'name': '#FF7777'}, {'id': 4, 'name': '#DD361A'}, {'id': 5, 'name': '#DB6A20'}, {'id': 6, 'name': '#F57221'}, {'id': 7, 'name': '#FFEB57'}, {'id': 8, 'name': '#B4E550'}, {'id': 9, 'name': '#73B32D'}, {'id': 10, 'name': '#119E29'}, {'id': 11, 'name': '#00781B'}, {'id': 12, 'name': '#51D0B1'}, {'id': 13, 'name': '#00C2C1'}, {'id': 14, 'name': '#5CC1FC'}, {'id': 15, 'name': '#0091FA'}, {'id': 16, 'name': '#3057E5'}, {'id': 17, 'name': '#0051AF'}, {'id': 18, 'name': '#FF85BE'}, {'id': 19, 'name': '#F52E94'}, {'id': 20, 'name': '#9856D9'}, {'id': 21, 'name': '#5821A7'}, {'id': 22, 'name': '#C0C0C0'}, {'id': 23, 'name': '#8C8C8C'}, {'id': 24, 'name': '#000000'}],
      form: {
        name: '',
        classify: '0',
        directory: {}
      },
      folderManageData: [],
      folderMemberData: [],
      sharedMemberData: [],
      fileDtailData: {},
      fabulousData: {},
      fileVersionData: [],
      fileDownLoadData: [],
      fileDtailForm: false,
      popoverFileset: false,
      fileDynamic: true,
      fileVersion: false,
      fileRecord: false,
      folderAdminsetForm: false,
      editfolderForm: false,
      sharedfolderForm: false,
      sharedfileForm: false,
      deletefolderForm: false,
      movefolderForm: false,
      directoryForm: false,
      TreeObject: {},
      folderTree: [],
      defaultProps: {
        children: 'childList',
        label: 'name'
      },
      publicLinkForm: false,
      sharedLink: false,
      sharedEmail: false,
      sharedQrCode: false,
      editfileForm: false,
      copyfileForm: false,
      deletefileForm: false,
      cancelSharedForm: false,
      exitSharedForm: false,
      folderDetail: {},
      uploadAllCheck: false,
      downloadAllCheck: false,
      setObject: {}
    }
  },
  /* 页面加载后执行 */
  mounted () {
    console.log(this)
    window.addEventListener('resize', this.handler)
    this.queryfileCatalog()
    this.getFilesList()
    /** 快速新增添加 */
    if (this.$route.params.addForm) {
      this.openAddfolderForm()
    }
    /** 多选集合窗口 */
    // this.$bus.off('selectEmpDepRoleMulti')
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      console.log(1111, value)
      if (value) {
        if (value.prepareKey === 'chat1') {
          this.folderManageData = value.prepareData
        } else if (value.prepareKey === 'chat2') {
          this.folderMemberData = value.prepareData
        } else if (value.prepareKey === 'chat3') {
          this.sharedMemberData = value.prepareData
        } else if (value.prepareKey === 'chat4') {
          this.savaFolderManage(value.prepareData)
        } else if (value.prepareKey === 'chat5') {
          this.memberManage(value.prepareData)
        } else if (value.prepareKey === 'chat6') {
          this.savaFolderManage(value.prepareData)
        } else if (value.prepareKey === 'library-dtl-shared-member') {
          this.$bus.emit('file-shared-member', value)
        } else if (value.prepareKey === 'at-comment-member') {
          this.$bus.emit('comment-member', value)
        }
      }
    })
    this.$bus.off('returnFileList')
    this.$bus.on('returnFileList', (value) => {
      if (value) {
        this.getFilesList()
      }
    })
  },
  methods: {
    /** 头部自适应 */
    handler () {
      if (window.location.href.indexOf('frontend/library') < 0) {
        return
      }
      var len = this.folderUrl.length
      let that = this
      setTimeout(function () {
        var urlBox = document.getElementById('urlBox')
        if (urlBox.offsetWidth + 400 >= urlBox.parentNode.offsetWidth) {
          for (var i = len - 2; i >= 0; i--) {
            if (!that.folderUrl[i].label && i !== 0) {
              that.folderUrl[i].label = '...'
              that.$set(that.folderUrl, i, that.folderUrl[i])
              break
            }
          }
        } else {
          for (var j = 1; j < len; j++) {
            if (that.folderUrl[j].label) {
              delete that.folderUrl[j].label
            }
            that.$set(that.folderUrl, j, that.folderUrl[j])
          }
        }
      }, 200)
    },
    /** 成员初始化 */
    funInit (arr) {
      for (var i = 0; i < arr.length; i++) {
        arr[i].type = 0
        if (arr[i].sign === 'gs') {
          arr[i].type = 3
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
              arr[i].childList.push({ 'id': users[j].id, 'name': users[j].employee_name, 'picture': users[j].picture, 'type': 1, 'sign_id': users[j].sign_id, 'value': users[j].value })
            }
          }
          delete arr[i].users
        }
      }
    },
    /** 文件夹成员管理 */
    memberManage (arr) {
      if (arr.length < 1) {
        this.$message.error('成员不能为空！')
        return
      }
      var removeArr = []
      for (var r = 0; r < this.folderMember.length; r++) {
        var contains = tool.contains(arr, 'id', this.folderMember[r], 'id')
        if (!contains) {
          removeArr.push(this.folderMember[r].id)
        }
      }
      console.log(this.folderMember)
      var addArr = []
      for (var a = 0; a < arr.length; a++) {
        var contains2 = tool.contains(this.folderMember, 'id', arr[a], 'id')
        if (!contains2) {
          addArr.push(arr[a].id)
        }
      }
      for (var m = 0; m < addArr.length; m++) {
        var contains3 = tool.contains(this.folderManageData, 'id', addArr[m], 'id')
        if (contains3) {
          addArr.splice(contains3.i, 1)
        }
      }
      var jsondata = {
        'id': this.setObject.id,
        'member_id': addArr.toString(),
        'delete_id': removeArr.toString()
      }
      HTTPServer.fileEditMember(jsondata, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '编辑成功!'
        })
        if (this.folderAdminsetForm) {
          this.openFolderAdminsetForm(this.setObject)
        }
      })
    },
    /** 遍历内容 */
    traverseSearchName (value) {
      value = this.seachTable ? value.replace(new RegExp(this.searchFileName, 'ig'), '<span class="searchFileName">' + this.searchFileName + '</span>') : value
      return value
    },
    /** 获取文件、文件夹列表 */
    getFilesList (jsondata) {
      jsondata = jsondata || {'style': this.navObject.id}
      jsondata.pageSize = this.pageSize
      jsondata.pageNum = this.pageNum
      jsondata.sign = 2
      if (this.parentObject.id) {
        if (this.navObject.id === 4 || this.navObject.id === 5) {
          jsondata.style = this.parentObject.table_id || this.navObject.id
        } else {
          jsondata.id = this.parentObject.id
        }
        if (this.navObject.id === 2) {
          if (this.parentObject.type === 0) {
            jsondata.id = this.parentObject.model_id
            this.queryModuleFileList(jsondata)
          } else {
            jsondata.id = this.parentObject.model_id
            this.queryModulePartFileList(jsondata)
          }
        } else {
          this.queryFilePartList(jsondata)
        }
      } else {
        if (this.navObject.id === 2) {
          this.queryAppFileList(jsondata)
        } else {
          this.queryFileList(jsondata)
        }
      }
    },
    /** 获取公司文件根目录列表 */
    queryFileList (jsondata) {
      HTTPServer.queryFileList(jsondata, 'Loading').then((res) => {
        this.tableData3 = res.dataList
        for (var i = 0; i < this.tableData3.length; i++) {
          if (this.tableData3[i].sign === '1') {
            this.tableData3[i].fileType = tool.determineFileType(this.tableData3[i].siffix.substr(1, this.tableData3[i].siffix.length))
          }
          if (this.navObject.id !== 1) {
            this.tableData3[i].is_manage = 1
            this.tableData3[i].upload = 1
            this.tableData3[i].download = 1
          }
        }
        var pageInfo = res.pageInfo
        this.totalRows = pageInfo.totalRows
      })
    },
    /** 公司子级目录 */
    queryFilePartList (jsondata) {
      HTTPServer.queryFilePartList(jsondata, 'Loading').then((res) => {
        this.tableData3 = res.dataList
        var type
        if (this.folderUrl.length > 1) {
          type = this.folderUrl[1].type
        }
        for (var i = 0; i < this.tableData3.length; i++) {
          this.tableData3[i].type = type || 0
          this.seachRowData = this.seachRowData || {}
          this.seachRowData.upload = this.seachRowData.upload || 0
          this.seachRowData.download = this.seachRowData.download || 0
          // this.tableData3[i].is_manage = this.parentObject.is_manage || this.seachRowData
          this.tableData3[i].upload = this.parentObject.upload || this.seachRowData.upload
          this.tableData3[i].download = this.parentObject.download || this.seachRowData.download
          if (this.tableData3[i].sign === '1') {
            this.tableData3[i].fileType = tool.determineFileType(this.tableData3[i].siffix.substr(1, this.tableData3[i].siffix.length))
          }
          if (this.navObject.id !== 1) {
            this.tableData3[i].is_manage = 1
            this.tableData3[i].upload = 1
            this.tableData3[i].download = 1
          }
        }
        var pageInfo = res.pageInfo
        this.totalRows = pageInfo.totalRows
      })
    },
    /** 应用--应用文件夹 */
    queryAppFileList (jsondata) {
      HTTPServer.queryAppFileList(jsondata, 'Loading').then((res) => {
        this.tableData3 = res.dataList
        for (var i = 0; i < this.tableData3.length; i++) {
          this.tableData3[i].fileType = tool.determineFileType(this.tableData3[i].siffix.substr(1, this.tableData3[i].siffix.length))
          this.tableData3[i].download = ''
        }
        var pageInfo = res.pageInfo
        this.totalRows = pageInfo.totalRows
      })
    },
    /** 应用--模块文件夹 */
    queryModuleFileList (jsondata) {
      HTTPServer.queryModuleFileList(jsondata, 'Loading').then((res) => {
        this.tableData3 = res.dataList
        for (var i = 0; i < this.tableData3.length; i++) {
          this.tableData3[i].fileType = tool.determineFileType(this.tableData3[i].siffix.substr(1, this.tableData3[i].siffix.length))
          this.tableData3[i].download = 1
        }
        var pageInfo = res.pageInfo
        this.totalRows = pageInfo.totalRows
      })
    },
    /** 应用--模块文件 */
    queryModulePartFileList (jsondata) {
      HTTPServer.queryModulePartFileList(jsondata, 'Loading').then((res) => {
        this.tableData3 = res.dataList
        for (var i = 0; i < this.tableData3.length; i++) {
          this.tableData3[i].download = 1
          this.tableData3[i].fileType = tool.determineFileType(this.tableData3[i].siffix.substr(1, this.tableData3[i].siffix.length))
        }
        var pageInfo = res.pageInfo
        this.totalRows = pageInfo.totalRows
      })
    },
    /** 加载 文件夹目录 */
    loadFolderNode (node, resolve) {
      let jsondata = {'style': this.shiftOther ? 1 : this.navObject.id}
      jsondata.pageSize = 99999
      jsondata.pageNum = 1
      jsondata.sign = 0
      var arr = []
      let that = this
      this.resolve = this.resolve || resolve
      var queryFileList = function () {
        HTTPServer.queryFileList(jsondata, '').then((res) => {
          var folderTree = res.dataList
          for (var i = 0; i < folderTree.length; i++) {
            if (folderTree[i].id !== that.setObject.id) {
              arr.push(folderTree[i])
            }
          }
          if (!node) {
            that.folderTree = folderTree
          } else {
            resolve(arr)
          }
        })
      }
      if (!node) {
        queryFileList()
      } else if (node.level === 0) {
        queryFileList()
      } else {
        jsondata.id = node.data.id
        HTTPServer.queryFilePartList(jsondata, '').then((res) => {
          var folderTree = res.dataList
          for (var i = 0; i < folderTree.length; i++) {
            if (folderTree[i].id !== this.setObject.id) {
              arr.push(folderTree[i])
            }
          }
          resolve(arr)
        })
      }
    },
    /** 链接跳转 */
    jumpFolder (data) {
      var contains = tool.contains(this.folderUrl, 'id', data, 'id')
      if (!data.url) {
        contains = this.folderUrl[0]
        contains.i = 0
      }
      if (contains) {
        if (contains.i > 0) {
          this.folderUrl = this.folderUrl.slice(0, contains.i + 1)
          this.parentObject = this.folderUrl[contains.i]
        } else {
          this.parentObject = {}
          this.folderUrl = [{'id': data.id, 'parent_id': '', 'name': data.name}]
        }
      }
      this.handler()
      this.pageSize = 20
      this.pageNum = 1
      this.getFilesList()
    },
    /** 显示每页条数 */
    handleSizeChange (val) {
      this.pageSize = val
      this.pageNum = 1
      this.getFilesList()
    },
    /** 跳转第几页 */
    handleCurrentChange (val) {
      this.pageNum = val
      this.getFilesList()
    },
    /** 退出共享 */
    exitShared () {
      HTTPServer.fileQuitShare({'id': this.setObject.file_id}, '').then((res) => {
        this.getFilesList()
        this.exitSharedForm = false
      })
    },
    /** 取消共享 */
    cancelShared () {
      HTTPServer.fileCancelShare({'id': this.setObject.file_id}, '').then((res) => {
        this.getFilesList()
        this.cancelSharedForm = false
      })
    },
    /** 删除文件 */
    deletefile () {
      HTTPServer.delFileLibrary({'id': this.setObject.id}, '').then((res) => {
        this.$message({
          type: 'success',
          message: '删除成功!'
        })
        this.deletefileForm = false
        this.fileDtailForm = false
        this.getFilesList()
      })
    },
    /** 复制文件 */
    opencopyfileForm () {
      this.popoverFileset = false
      this.copyfileForm = true
    },
    /** 复制文件至...  */
    copyfile () {
      var jsondata = {
        'current_id': this.form.directory.id,
        'worn_id': this.setObject.id,
        'style': this.navObject.id
      }
      HTTPServer.copyFileLibrary(jsondata, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '复制成功!'
        })
        this.deletefileForm = false
        this.fileDtailForm = false
        this.copyfileForm = false
        this.getFilesList()
      })
    },
    /** 文件重命名 */
    opemeditfileForm () {
      this.popoverFileset = false
      this.editfileForm = true
      var suffix = this.fileDtailData.name.replace(/.+\./, '')
      this.form.name = this.fileDtailData.name.substr(0, this.fileDtailData.name.length - suffix.length - 1)
    },
    /** 文件重命名 */
    editfile () {
      var jsondata = {
        'id': this.fileDtailData.id,
        'name': this.form.name + '.' + this.fileDtailData.name.replace(/.+\./, '')
      }
      HTTPServer.fileEditRename(jsondata, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '重命名成功!'
        })
        this.fileDtail()
        this.getFilesList()
        this.editfileForm = false
      })
    },
    /** 文件设置 */
    filePopoverSet (type) {
      this.setObject = this.fileDtailData
      if (type === '0') {
        this.openpublicLinkForm()
      } else if (type === '1') {
        this.opemeditfileForm()
      } else if (type === '2') {
        // this.beforeUpload(1)
      } else if (type === '3') {
        this.shiftOther = false
        this.folderTree = []
        this.openmovefolderForm()
      } else if (type === '4') {
        this.shiftOther = false
        this.folderTree = []
        this.loadFolderNode()
        this.opencopyfileForm()
      } else if (type === '5') {
        this.opensharedfolderForm(1)
      } else if (type === '6') {
        this.deletefileForm = true
        this.popoverFileset = false
      } else if (type === '7') {
        this.shiftOther = true
        this.folderTree = []
        this.loadFolderNode()
        this.openmovefolderForm()
      }
    },
    /** 打开目录下拉框 */
    opendirectoryForm (event) {
      this.directoryForm = true
      document.getElementsByClassName('directoryForm')[0].childNodes[0].style.top = tool.getTop(event.target) + 50 + 'px'
      document.getElementsByClassName('directoryForm')[0].childNodes[0].style.left = tool.getLeft(event.target) + 'px'
      document.getElementsByClassName('directoryForm')[0].childNodes[0].style.width = event.target.clientWidth + 'px'
    },
    /** 选择目录 */
    checkDirectory (data) {
      this.form.directory = data
      this.directoryForm = false
    },
    /** 确认 - 移动文件夹 */
    movefolder () {
      if (!this.form.directory.id) {
        this.$message.error('请选择目录！')
        return
      }
      var jsondata = {
        'current_id': this.form.directory.id,
        'worn_id': this.setObject.id,
        'style': this.shiftOther ? 1 : this.navObject.id
      }
      HTTPServer.shiftFileLibrary(jsondata, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '移动成功!'
        })
        this.movefolderForm = false
        this.getFilesList()
      })
    },
    /** 移动文件夹 */
    openmovefolderForm () {
      this.form.directory = {}
      this.movefolderForm = true
      this.folderTree = []
    },
    /** 删除文件夹 */
    deletefolder () {
      HTTPServer.delFileLibrary({'id': this.setObject.id}, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '删除成功!'
        })
        this.deletefolderForm = false
        this.folderAdminsetForm = false
        this.getFilesList()
      })
    },
    /** 打开共享弹窗 */
    opensharedfolderForm (type) {
      this.sharedMemberData = []
      if (type === 0) {
        this.sharedfolderForm = true
        this.shareData = this.setObject
      } else {
        this.sharedfileForm = true
        this.shareData = this.fileDtailData
      }
    },
    /** 文件夹共享 */
    shareFileLibaray () {
      if (this.sharedMemberData.length === 0) {
        this.$message.error('共享成员不能为空!')
        return
      }
      var arr = []
      for (var i = 0; i < this.sharedMemberData.length; i++) {
        arr.push(this.sharedMemberData[i].id)
      }
      HTTPServer.shareFileLibaray({'id': this.shareData.id, 'share_by': arr.toString(), 'style': this.navObject.id}, 'Loading').then((res) => {
        this.sharedfolderForm = false
      })
    },
    /** 移除共享成员 */
    removeSharedMember (data) {
      var contains = tool.contains(this.sharedMemberData, 'id', data, 'id')
      if (contains) {
        this.sharedMemberData.splice(contains.i, 1)
      }
    },
    /** 编辑文件夹 */
    openeditfolderForm () {
      if (this.setObject.is_manage === 0) {
        return this.$message.error('您没有操作该文件夹的权限！')
      }
      this.editfolderForm = true
      this.form.name = this.setObject.name
      for (var i = 0; i < this.colorData.length; i++) {
        delete this.colorData[i].checked
        if (this.colorData[i].name === this.setObject.color) {
          this.colorData[i].checked = true
        }
      }
    },
    /** 编辑文件夹提交 */
    editfolder () {
      if (!this.form.name) {
        this.$message.error('文件夹名称不能为空！')
        return
      }
      var colors = ''
      for (var i = 0; i < this.colorData.length; i++) {
        if (this.colorData[i].checked) {
          colors = this.colorData[i].name
        }
      }
      var jsondata = {
        'id': this.setObject.id,
        'name': this.form.name,
        'color': colors
      }
      HTTPServer.editFolder(jsondata, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '编辑成功!'
        })
        this.editfolderForm = false
        this.getFilesList()
        this.setObject.name = this.form.name
        this.setObject.color = colors
      })
    },
    /** 保存管理员 */
    savaFolderManage (arr) {
      if (arr.length === 0) {
        this.$message.error('管理员不能为空！')
        return
      }
      console.log(this.folderManage)
      var removeArr = []
      for (var r = 0; r < this.folderManage.length; r++) {
        var contains = tool.contains(arr, 'id', this.folderManage[r], 'id')
        if (!contains) {
          removeArr.push(this.folderManage[r].id)
        }
      }
      var addArr = []
      for (var a = 0; a < arr.length; a++) {
        var contains2 = tool.contains(this.folderManage, 'id', arr[a], 'id')
        if (!contains2) {
          addArr.push(arr[a].id)
        }
      }
      var jsondata = {
        'id': this.setObject.id,
        'manage_id': addArr.toString(),
        'delete_id': removeArr.toString(),
        'file_level': this.parentObject.id ? undefined : 1
      }
      console.log(jsondata)
      HTTPServer.fileEditManageStaff(jsondata, '').then((res) => {
        this.$message({
          type: 'success',
          message: '保存成功!'
        })
        if (this.folderAdminsetForm) {
          this.openFolderAdminsetForm(this.setObject)
        }
      })
    },
    /** 打开文件夹右侧管理窗口 */
    openFolderAdminsetForm (data) {
      this.setObject = data
      HTTPServer.queryFolderInitDetail({'style': this.navObject.id, 'id': this.setObject.id}, '').then((data) => {
        this.folderAdminsetForm = true
        this.folderDetail = data.basics
        this.folderManageData = data.manage || []
        var setting = data.setting || []
        // this.folderMemberData
        var uploadNum = 0
        var downloadNum = 0
        for (var i = 0; i < setting.length; i++) {
          setting[i].previewCheck = true
          setting[i].downloadCheck = (setting[i].download === '1')
          setting[i].uploadCheck = (setting[i].upload === '1')
          if (setting[i].download === '1') downloadNum++
          if (setting[i].upload === '1') uploadNum++
        }
        this.downloadAllCheck = (setting.length === downloadNum && downloadNum !== 0)
        this.uploadAllCheck = (setting.length === uploadNum && uploadNum !== 0)
        this.folderMemberData = setting
        console.log(this.folderMemberData)
      })
    },
    /** 保存成员权限 */
    saveMemberRole () {
      var member = []
      for (var i = 0; i < this.folderMemberData.length; i++) {
        member.push({'id': this.folderMemberData[i].id, 'upload': this.folderMemberData[i].uploadCheck ? 1 : 0, 'download': this.folderMemberData[i].downloadCheck ? 1 : 0, 'preview': this.folderMemberData[i].previewCheck ? 1 : 0})
      }
      var jsondata = {
        'data': member,
        'id': this.setObject.id
      }
      HTTPServer.fileUpdateSetting(jsondata, 'Loading').then((res) => {
        this.$message({type: 'success', message: '保存成功!'})
      })
    },
    /** 成员权限选择 */
    memberCheck () {
      console.log(this.folderMemberData)
      var uploadNum = 0
      var downloadNum = 0
      for (var i = 0; i < this.folderMemberData.length; i++) {
        if (this.folderMemberData[i].downloadCheck) downloadNum++
        if (this.folderMemberData[i].uploadCheck) uploadNum++
      }
      this.downloadAllCheck = (this.folderMemberData.length === downloadNum && downloadNum !== 0)
      this.uploadAllCheck = (this.folderMemberData.length === uploadNum && uploadNum !== 0)
    },
    /** 上传权限 */
    allUpload (data) {
      for (var i = 0; i < this.folderMemberData.length; i++) {
        this.folderMemberData[i].uploadCheck = data
        this.$set(this.folderMemberData, i, this.folderMemberData[i])
      }
    },
    /** 下载权限 */
    allDownload (data) {
      for (var i = 0; i < this.folderMemberData.length; i++) {
        this.folderMemberData[i].downloadCheck = data
        this.$set(this.folderMemberData, i, this.folderMemberData[i])
      }
      console.log(this.folderMemberData)
    },
    /** 双击列表行   进入文件夹、文件详情 */
    rowDblclick (row, event) {
      var className = event.target.className
      console.log(className)
      if (className.indexOf('iconfont') >= 0 && className.indexOf('folder') < 0 && className.indexOf('operation') >= 0) {
        return
      }
      if (className.indexOf('operation') >= 0) {
        return
      }
      if (row.sign === '1') {
        this.setObject = row
        this.fileVersion = false
        this.fileRecord = false
        this.fileObject = {'fileForm': true, 'fileData': row, 'navObject': this.navObject}
      } else {
        this.tableData3 = []
        this.parentObject = row
        this.pageSize = 20
        this.pageNum = 1
        if (this.seachTable) {
          this.seachTable = false
          this.seachRowData = row
          this.getBlurResultParentInfo(row)
        } else {
          this.seachTable = false
          var jsondata = {'style': this.navObject.id, 'id': row.id}
          this.getFilesList(jsondata)
          this.folderUrl.push({})
          for (var i in row) {
            this.folderUrl[this.folderUrl.length - 1][i] = row[i]
          }
          this.handler()
        }
      }
    },
    /** 获取文件夹路劲 */
    getBlurResultParentInfo (data) {
      HTTPServer.getBlurResultParentInfo({'id': data.id}, '').then((res) => {
        var arr = res
        this.folderUrl = [this.navObject]
        for (var i = 0; i < arr.length; i++) {
          this.folderUrl.push({'id': arr[i].id, 'parent_id': arr[i].parent_id, 'name': arr[i].name, 'type': data.type})
        }
        var jsondata = {'style': this.navObject.id, 'id': data.id}
        this.getFilesList(jsondata)
        this.handler()
      })
    },
    /** 导航切换 */
    fileTypeNav (event, data) {
      this.seachTable = false
      for (var i = 0; i < this.filenavData.length; i++) {
        delete this.filenavData[i].className
        if (this.filenavData[i].id === data.id) {
          this.filenavData[i].className = 'active'
          this.$set(this.filenavData, i, this.filenavData[i])
        }
      }
      this.parentObject = {}
      this.navObject = data
      this.pageSize = 20
      this.pageNum = 1
      this.getFilesList()
      this.folderUrl = [{'id': data.id, 'parent_id': data.parent_id, 'name': data.name}]
    },
    /** 搜索文件夹 */
    searchFile () {
      console.log(this.searchFileName)
      if (!this.searchFileName) {
        this.tableData3 = []
        this.seachTable = false
        this.getFilesList()
        return
      }
      if (this.folderUrl.length < 3 && this.navObject.id === 2) {
        this.$message.error('只能按照模块搜索数据')
        return
      }
      console.log(this.parentObject) // model_id
      this.tableData3 = []
      this.seachTable = true
      this.pageSize = 20
      this.pageNum = 1
      var fileId = (this.navObject.id === 2) ? this.parentObject.model_id : this.parentObject.id
      var jsondata = {'style': this.navObject.id, 'content': this.searchFileName, 'pageSize': this.pageSize, 'pageNum': this.pageNum, 'fileId': fileId}
      HTTPServer.fileBlurSearchFile(jsondata, 'Loading').then((res) => {
        this.tableData3 = res
        for (var i = 0; i < this.tableData3.length; i++) {
          this.tableData3[i].type = (this.tableData3[i].role_type === 1 || this.tableData3[i].role_type === 2) ? 1 : 0
          this.tableData3[i].is_manage = (this.tableData3[i].role_type === 1) || 0
          if (this.tableData3[i].sign === '1') {
            this.tableData3[i].fileType = tool.determineFileType(this.tableData3[i].siffix.substr(1, this.tableData3[i].siffix.length))
          }
        }
      })
    },
    /** 文件设置列表开启 */
    listSet (type) {
      if (type === '0') {
        this.addFolderManage(6)
      } else if (type === '1') {
        this.openeditfolderForm()
      } else if (type === '2') {
        this.openmovefolderForm()
      } else if (type === '3') {
        this.opensharedfolderForm(0)
      } else {
        this.deletefolderForm = true
      }
    },
    /** 选择颜色 */
    checkColor (data) {
      for (var i = 0; i < this.colorData.length; i++) {
        this.colorData[i].checked = false
        if (this.colorData[i].id === data.id) {
          this.colorData[i].checked = true
          this.$set(this.colorData, i, this.colorData[i])
        }
      }
    },
    /** 添加文件夹管理员 */
    addFolderManage (key, data) {
      console.log(key, data)
      var empArr = []
      if (key === 1) {
        this.folderMemberData.map((item) => {
          empArr.push(item.value)
        })
        this.$bus.emit('commonMember', {'prepareData': this.folderManageData, 'prepareKey': 'chat' + key, 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': empArr, 'haveData': this.haveData}) // 给父组件传值
      } else if (key === 2) {
        this.folderManageData.map((item) => {
          empArr.push(item.value)
        })
        this.$bus.emit('commonMember', {'prepareData': this.folderMemberData, 'prepareKey': 'chat' + key, 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': empArr, 'haveData': []}) // 给父组件传值
      } else if (key === 3) {
        this.$bus.emit('commonMember', {'prepareData': data || [], 'prepareKey': 'chat' + key, 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': ['1-' + this.employeeInfo.id]}) // 给父组件传值
      } else if (key === 4 || key === 6) {
        HTTPServer.queryFolderInitDetail({'style': this.navObject.id, 'id': this.setObject.id}, '').then((res) => {
          var data = res
          var removeArr = []
          for (var m = 0; m < data.manage.length; m++) {
            empArr.push({'id': data.manage[m].employee_id, 'name': data.manage[m].employee_name, 'picture': data.manage[m].picture, 'value': 1 + '-' + data.manage[m].employee_id, 'type': 1})
            if (data.manage[m].sign_type === '1') {
              removeArr.push(1 + '-' + data.manage[m].employee_id)
            }
          }
          this.folderManage = empArr
          this.$bus.emit('commonMember', {'prepareData': empArr, 'prepareKey': 'chat' + key, 'seleteForm': true, 'banData': (key === 4) ? ['1-' + this.employeeInfo.id] : removeArr, 'navKey': '1', 'removeData': []}) // 给父组件传值
        })
      } else if (key === 5) {
        for (var m = 0; m < this.folderMemberData.length; m++) {
          empArr.push({'id': this.folderMemberData[m].employee_id, 'name': this.folderMemberData[m].employee_name, 'picture': this.folderMemberData[m].picture, 'value': 1 + '-' + this.folderMemberData[m].employee_id, 'type': 1})
        }
        var removeData = []
        for (var re = 0; re < this.folderManageData.length; re++) {
          removeData.push('1-' + this.folderManageData[re].employee_id)
        }
        this.folderMember = empArr
        this.$bus.emit('commonMember', {'prepareData': empArr || [], 'prepareKey': 'chat' + key, 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': removeData}) // 给父组件传值
      } else {
        this.$bus.emit('commonMember', {'prepareData': data || [], 'prepareKey': 'chat' + key, 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': []}) // 给父组件传值
      }
    },
    /** 添加文件夹成员 */
    addFolderMember (key, data) {
      this.$bus.emit('commonMember', {'prepareData': data || [], 'prepareKey': 'chat' + key, 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': []}) // 给父组件传值
    },
    /** 打开新建文件夹窗口 */
    openAddfolderForm () {
      this.addfolderForm = true
      for (var i = 0; i < this.colorData.length; i++) {
        this.colorData[i].checked = false
        if (i === 0) {
          this.colorData[i].checked = true
          this.$set(this.colorData, i, this.colorData[i])
        }
      }
      this.form.name = ''
      this.form.classify = '0'
      this.folderManageData = []
      this.folderMemberData = []
      this.haveData = []
      this.heavyArr = []
      let that = this
      if (this.navObject.id === 1) {
        setTimeout(function () {
          document.getElementsByClassName('addfolderForm')[0].getElementsByClassName('manage')[0].getElementsByClassName('el-form-item__label')[0].innerText = that.parentObject.id ? '子管理员' : '管理员'
        }, 50)
      }
      console.log(this.parentObject)
      if (this.parentObject.id && this.navObject.id === 1) {
        HTTPServer.queryFolderInitDetail({'style': this.navObject.id, 'id': this.parentObject.id}, '').then((res) => {
          for (var m = 0; m < res.manage.length; m++) {
            this.folderManageData.push({'id': res.manage[m].employee_id, 'name': res.manage[m].employee_name, 'picture': res.manage[m].picture, 'value': 1 + '-' + res.manage[m].employee_id, 'type': 1})
            this.heavyArr.push({'id': res.manage[m].employee_id, 'name': res.manage[m].employee_name, 'picture': res.manage[m].picture, 'value': 1 + '-' + res.manage[m].employee_id, 'type': 1})
            this.haveData.push(1 + '-' + res.manage[m].employee_id)
          }
          for (var s = 0; s < res.setting.length; s++) {
            this.haveData.push(1 + '-' + res.setting[s].employee_id)
          }
        })
      }
    },
    /** 新建文件夹 */
    addfolder () {
      if (!this.form.name) {
        this.$message.error('文件夹名称不能为空!')
        return
      }
      if (this.form.name.length > 12) {
        this.$message.error('文件夹名称不能超过12个字符!')
        return
      }
      if (this.folderManageData.length === 0 && this.navObject.id === 1) {
        this.$message.error('管理员不能为空!')
        return
      }
      if (this.folderMemberData.length === 0 && parseInt(this.form.classify) === 1 && this.navObject.id === 1) {
        this.$message.error('成员不能为空!')
        return
      }
      var empId = []
      for (var h = 0; h < this.folderManageData.length; h++) {
        if (this.heavyArr.length > 0) {
          var contains = tool.contains(this.heavyArr, 'value', this.folderManageData[h], 'value')
          if (!contains) {
            empId.push(this.folderManageData[h].id)
          }
        } else {
          empId.push(this.folderManageData[h].id)
        }
      }
      var menberArr = []
      for (var m = 0; m < this.folderMemberData.length; m++) {
        var ccc = tool.contains(this.folderManageData, 'id', this.folderMemberData[m], 'id')
        if (!ccc) {
          menberArr.push(this.folderMemberData[m].id)
        }
      }
      var colors = this.colorData[0].name
      for (var i = 0; i < this.colorData.length; i++) {
        if (this.colorData[i].checked) {
          colors = this.colorData[i].name
        }
      }
      var jsondata = {
        'name': this.form.name,
        'color': colors,
        'type': (this.navObject.id === 1 && !this.parentObject.id) ? this.form.classify : '',
        'manage_by': (this.navObject.id === 1) ? empId.toString() : '',
        'member_by': (this.navObject.id === 1 && !this.parentObject.id && this.form.classify) ? menberArr.toString() : '',
        'style': this.navObject.id,
        'parent_id': this.parentObject.id || ''
      }
      HTTPServer.savaFileLibrary(jsondata, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '新增成功!'
        })
        this.getFilesList()
        this.addfolderForm = false
      })
    },
    /** 上传文件 */
    beforeUpload (event) {
      console.log(event.target.files[0])
      if (!event.target.value) {
        return
      }
      var file = event.target.files[0]
      if ((file.size / 1024 / 1024) > 99.9) {
        this.$message.error('文件大小不能超过100M!')
        return
      }
      var formData = new FormData(document.getElementById('uploadfile0'))
      formData.append('bean', 'library')
      formData.append('id', this.parentObject.id || '')
      formData.append('style', this.navObject.id)
      formData.append('url', this.parentObject.url || '')
      event.target.value = ''
      HTTPServer.fileLibraryUpload(formData, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '上传成功!'
        })
        this.getFilesList()
      })
    },
    /** 获取模块导航 */
    queryfileCatalog () {
      HTTPServer.queryfileCatalog({}, 'Loading').then((res) => {
        var contains = tool.contains(res, 'id', 3)
        if (contains) {
          this.filenavData.splice(2, 0, res[contains.i])
        }
      })
    },
    /** 文件库下载文件夹 */
    batchDownload (data) {
      ajaxGetRequest({'id': data.id, 'TOKEN': this.token}, 'library/file/batchDownload')
        .then((response) => {
          console.log('文件库下载文件夹', response)
          location.href = baseURL + 'library/file/batchDownload?TOKEN=' + this.token + '&id=' + data.id
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 文件库下载文件 */
    fileDownload (data) {
      ajaxGetRequest({'id': data.id, 'TOKEN': this.token}, 'library/file/download')
        .then((response) => {
          console.log('文件库下载文件', response)
          this.saveFile(baseURL + 'library/file/downloadWithoutRecord?TOKEN=' + this.token + '&id=' + data.id, data.name)
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 文件库下载文件夹 */
    download (data) {
      if (data.sign === '1') {
        this.fileDownload(data)
      } else {
        this.batchDownload(data)
      }
    },
    downloadHistoryFile (data) {
      this.saveFile(baseURL + 'library/file/downloadHistoryFile?TOKEN=' + (JSON.parse((sessionStorage.requestHeader))).TOKEN + '&id=' + data.id, data.name)
    },
    saveFile (data, filename) {
      console.log(data, filename)
      var saveLink = document.createElementNS('http://www.w3.org/1999/xhtml', 'a')
      saveLink.href = data
      saveLink.download = filename
      console.log(saveLink)
      var event = document.createEvent('MouseEvents')
      event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null)
      saveLink.dispatchEvent(event)
    },
    downFile (data, fileName) {
      var blob = new Blob([data])
      console.log(blob)
      if (window.navigator.msSaveOrOpenBlob) {
        navigator.msSaveBlob(blob, fileName)
      } else {
        var link = document.createElement('a')
        link.href = window.URL.createObjectURL(blob)
        link.download = fileName
        link.click()
        window.URL.revokeObjectURL(link.href)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
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
  transform: translateX(-360px);
}
</style>
<style lang='scss'>
@import '../../common/scss/dialog.scss';
.library-mian{
  .el-dialog__wrapper{
    .must{
      .el-form-item__label::before{content: "*";display: inline-block;color: red;line-height: 1;margin: 0 5px 0 0;}
    }
  }

  .el-container{height: 100%;
    .el-aside{background: #EBEDF0;
      header{height: 60px;line-height: 60px;font-size: 18px;color: #4A4A4A;padding: 0 0 0 15px;}
      >.el-input{width: calc(100% - 30px);margin-left: 15px;margin-bottom: 9px;height: 36px;
        input{border: none;height: 36px;}
      }
      input:focus{box-shadow: none;border: none;}
      .item-nav{line-height: 40px;padding: 0 15px;margin-top: 1px;cursor: pointer;
        i{
          float: left;
          line-height: 1;
          margin: 12px 8px 0 0;
        }
      }
      .item-nav:hover{background: #D7DCE0;}
      .item-nav.active{background: #D7DCE0;}
    }
    .el-main{padding: 0 30px 0 25px;height: 100%;
      header{line-height: 60px;border-bottom: 1px solid #e7e7e7;height: 59px;
        >i{font-size: 24px;float: left;margin: 0 10px 0 0;}
        .addfolder{float: right;border: 1px solid #409eff;border-radius: 3px;width: 100px;height: 28px;text-align: center;line-height: 28px;font-size: 14px;color: #409eff;
        margin: 15px 0;}
        form{float: right;border-radius: 3px;width: 100px;height: 30px;text-align: center;line-height: 30px;font-size: 14px;color: #fff;background: #409eff;
        margin: 15px 10px 0 0;}
        .name{display: inline-block;
          span{line-height: 1;font-size: 18px;color: #4A4A4A;cursor: pointer;}
          span:last-child{color: #409eff;}
          span:first-child{color: #4A4A4A;}
        }
      }
      .el-table{height: calc(100% - 120px);width: 100%;
        colgroup{display: none;}
        .el-table__header-wrapper{
          th{padding: 18px 0;font-size: 14px;color: #17171A;}
        }
      .el-table__body-wrapper{overflow-y: auto;height: calc(100% - 60px);}
        table{width: 100%!important;
          .el-table__row{cursor: pointer;}
          img{width: 30px;height: 30px;float: left;margin: 0 10px 0 0;}
          td{padding: 0;height: 60px;line-height: 60px;font-size: 16px;color: #4A4A4A;}
          .cell{line-height: inherit;
            .folder{font-size: 30px;float: left;margin: 0 10px 0 0;line-height: 1;}
            .name{white-space: nowrap;text-overflow: ellipsis;display: inline-block;width: calc(100% - 60px);overflow: hidden;line-height: 19px;float: left;margin: 7px 0 0 0;}
            img+.name{margin: 5px 0 0 0;}
          }
          .el-dropdown{float: right;margin: 21px 10px 0 0;line-height: 1;
            .iconfont:first-child{margin: 0;}
          }
          .operation{float: right;font-size: 18px;margin: 21px 10px 0 0;line-height: 1;color: #A0A0AE;cursor: pointer;
            i{font-size: 20px;}
          }
          a.operation{font-size: 14px;color: #A0A0AE;margin: 23px 30px 0 0!important;}
          .operation:first-child{margin: 21px 30px 0 0;}
          .el-table_1_column_1{
            .name{white-space: nowrap;text-overflow: ellipsis;display: inline-block;overflow: hidden;width: calc(100% - 60px);line-height: 20px;float: left;margin: 8px 0 0 0;}
          }
        }
      }
      .el-table__empty-block{width: 100%!important;height: auto;}
      .el-pagination{text-align: right;padding: 15px 20px;}
      .searchFileName{color: #1890FF;}
      .no-data{text-align: center;height: calc(100% - 120px);
        img{margin-top: 250px;width: 200px;}
        p{font-size: 16px;color: #A0A0AE;margin-top: 36px;padding-left: 50px;}
      }
    }
    .seachTable{background: #FFF;box-shadow: 2px -2px 4px 0 rgba(155,155,155,0.50), -2px 2px 4px 0 rgba(155,155,155,0.50);
      .operation{display: none;}
      .el-table{
        height: calc(100% - 60px);
      }
    }

    .uploadForm{display: inline-block;width: 100px;height: 30px;overflow: hidden;margin-right: 20px;cursor: pointer;
      input{display: inline-block;float: left;opacity: 0;margin: -28px 0 0 0;}
      .text{font-size: 14px;float: left;width: 100px;height: 30px;background: #409eff;border-radius: 3px;line-height: 30px;text-align: center;color: #fff;}
    }
  }

  .addfolderForm{
    .el-dialog{width: 600px;}
    .color-box{
      .color-item{display: inline-block;width: 24px;height: 24px;margin: 5px 10px 0 0;border-radius: 3px;}
    }
    .colorbox{
      .el-form-item__label{width: 92px!important;}
    }
    .el-row{width: calc(100% - 15px);
      .el-col{width: 34px;padding: 5px!important;
        .grid-content{height: 24px;background: red;border-radius: 3px;cursor: pointer;}
        .grid-content{
          i{color: #fff;font-size: 16px;float: left;margin: 4px 0 0 4px;}
        }
      }
    }
    .member-box{display: inline-block;float: left;
    >div{float: left;}
      .simpName{width: 36px;height: 36px;line-height: 36px;display: inline-block;background: #409eff;color: #fff;font-size: 12px;border-radius: 50%;text-align: center;}
      span{float: left;line-height: 1;width: 46px;height: 46px;
          img{width: 36px;height: 36px;border-radius: 50%;}
      }
    }
    .icon-add-member{font-size: 36px;margin-top: 3px;}
    .add-member{width: 36px;margin-top: 2px;cursor: pointer;}
  }
  .el-button{padding: 7px 23px;}
  .fileDtailForm{
    .el-dialog{width: 1020px;}
    .el-dialog__header{display: none;}
    .el-dialog__body{padding: 0;height: 700px;
      .left{background: #69696C;width: 700px;height: 100%;display: inline-block;position: relative;
        header{height: 50px;line-height: 50px;color: #fff;font-size: 14px;padding: 0 4px 0 30px;
          i{font-size: 18px;float: right;margin: 16px 20px 0 0;line-height: 1;}
          .el-dropdown{float: right;margin: 16px 20px 0 0;line-height: 1;width: 20px;height: 20px;
            i{margin: 0;color: #fff;}
          }
        }
        .preview{width: 640px;height: 620px;background: #fff;margin-left: 36px;word-wrap: break-word;text-align: center;line-height: 620px;
          >img{max-width: 100%;}
          >video{max-width: 100%;}
          >audio{max-width: 100%;}
        }
        .tool{width: 160px;height: 38px;position: absolute;z-index: 1;bottom: 38px;left: 276px;padding: 0 7px;
          i{font-size: 18px;color: #fff;margin: 9px 6px 0 11px;float: left;cursor: pointer;}
        }
        .back{width: 160px;height: 38px;position: absolute;background: #000;opacity: 0.7;bottom: 38px;left: 276px;}
      }
      .right{height: 100%;width: 320px;float: right;
        >img,>input{display: none;}
        header{line-height: 50px;height: 50px;font-size: 14px;color: #69696C;padding-left: 20px;
          .praise{font-size: 18px;color: #f2f2f2;}
          .praise.active{color: #FF6260;}
          .el-icon-close{float: right;font-size: 20px;margin: 15px;cursor: pointer;}
        }
        .right-nav{height: 29px;border-bottom: 1px solid #E7E7E7;
          a{margin-left: 30px;font-size: 14px;color: #A0A0AE;
            i{margin-right: 8px;font-size: 16px;}
          }
          a.active{color: #69696C;}
          a:first-child{margin-left: 20px;}
        }
        .content{height: 620px;padding: 10px 0;overflow-y: auto;}
        .simpName{display: inline-block;height: 30px;line-height: 30px;text-align: center;width: 30px;border-radius: 50%;color: #fff;font-size: 12px;
        float: left;margin-left: 10px;}
        .file-record{
          .el-table{width: 100%;height: 100%;
          table{width: 100%!important;}
            th{border: none;padding: 10px 0;
              .cell{font-size: 12px;color: #A0A0AE;line-height: 1;}
            }
            td{padding: 0;height: 38px;border: none;}
            .cell{font-size: 12px;color: #69696C;line-height: 30px;
              img{width: 30px;height: 30px;float: left;margin: 0 10px 0 0;border-radius: 50%;}
            }
          }
          .el-table::before{display: none;}
        }
        .file-version{
          .item{padding: 0 20px;height: 60px;line-height: 60px;cursor: pointer;margin-top: 1px;
            >span{float: left;border: 1px solid #FF8E53;border-radius: 3px;height: 30px;line-height: 30px;font-size: 14px;color: #FF8E53;width: 70px;text-align: center;
            margin: 14px 16px 0 0;}
            .file-describe{display: inline-block;width: 156px;font-size: 14px;color: #69696C;line-height: 1.2;padding-top: 10px;
              span{font-size: 12px;color: #A0A0AE;display: inline-block;margin-top: 8px;}
              .name{text-overflow: ellipsis;white-space: nowrap;overflow: hidden;}
            }
            >i{font-size: 16px;float: right;margin-top: 20px;line-height: 1;}
          }
          .item:hover{background: #f2f2f2;}
          .item.active{background: #f2f2f2;
            >span{background: #FF8E53;color: #fff;font-size: 12px;}
          }
          .item:first-child{margin-top: 0;}
        }
      }
    }
  }
  .particularsForm{width: 360px;
    .side-body{height: 100%;padding: 60px 20px 20px;overflow-y: auto;}
    .icon-pc-widget-small{font-size: 16px;color: #979797;float: right;cursor: pointer;margin-top: -40px;}
    .el-dialog{margin: 0;float: right;height: 100%;}
    .el-dialog__body{height: 100%;padding: 60px 20px 20px;overflow-y: auto;}
    .el-dialog__title{display: none;}
    .folder-name{height: 58px;line-height: 30px;font-size: 16px;
      img{width: 30px;float: left;margin-right: 12px;}
      .folder{font-size: 30px;float: left;margin-right: 12px;}
      .edit{font-size: 20px;margin-left: 24px;color: #A0A0AE;cursor: pointer;}
    }
    p{color: #69696C;}
    .line{height: 1px;background: #E7E7E7;margin: 15px 0 20px;}
    .addadmin{font-size: 14px;color: #008FE5;
      i{margin-right: 8px;}
    }
    .admin-item{height: 40px;line-height: 40px;
      img{width: 30px;height: 30px;border-radius: 50%;float: left;margin: 5px 10px 0 0;}
      .simpName{display: inline-block;height: 30px;line-height: 30px;text-align: center;width: 30px;border-radius: 50%;color: #fff;font-size: 12px;
      float: left;margin: 5px 10px 0 0;}
      .role{font-size: 12px;color: #69696C;}
      a{float: right;line-height: 1;margin: 14px 20px 0 0;color: #409eff;}
    }
    .role-nav{
      span{font-size: 16px;color: #4A4A4A;}
      a{height: 26px;background: #409eff;line-height: 26px;width: 50px;text-align: center;border-radius: 3px;color: #fff;font-size: 12px;margin-left: 105px;display: inline-block;}
    }
    .role-list{
      img{width: 30px;height: 30px;border-radius: 50%;float: left;margin: 5px 8px 0 0;}
      .simpName{display: inline-block;height: 30px;line-height: 30px;text-align: center;width: 30px;border-radius: 50%;color: #fff;font-size: 12px;
      float: left;margin: 5px 8px 0 0;}
      .name{display: inline-block;width: 50px;overflow: hidden;line-height: 16px;margin: -11px -4px -4px 0;text-overflow: ellipsis;white-space: nowrap;}
      >div{
        >div{display: inline-block;width: 20%;height: 40px;line-height: 40px;}
        >div:first-child{width: 28%;}
      }
    }
    .deleteFolder{display: inline-block;width: 100%;background: #E7E7E7;border-radius: 4px;font-size: 14px;color: #F94C4A;height: 30px;line-height: 30px;text-align: center;margin-top: 30px;}
    .memberManage{display: inline-block;width: 100%;background: #E7E7E7;border-radius: 4px;font-size: 14px;color: #F94C4A;height: 30px;line-height: 30px;text-align: center;
    margin: 30px 0 -10px;}
  }
  .sharedfolderForm{
    .el-form-item__content{overflow: hidden;
      >i{font-size: 36px;color: #ECEFF1;float: left;margin: 2px 15px 0 0;}
      >div{display: inline-block;margin-right: 15px;
        i{color: #F94C4A;font-size: 12px;float: right;margin: 6px 0 0 -4px;cursor: pointer;}
      }
      img{width: 30px;height: 30px;border-radius: 50%;float: left;margin: 5px 8px 0 0;}
      .simpName{display: inline-block;height: 30px;line-height: 30px;text-align: center;width: 30px;border-radius: 50%;color: #fff;font-size: 12px;
      float: left;margin: 5px 8px 0 0;}
    }
  }
  .deleteForm{
    .el-dialog{width: 400px;}
    .content{font-size: 14px;color: #69696C;}
    .prompt{font-size: 16px;color: #4A4A4A;margin-top: 14px;}
  }
  .directoryForm{
    .el-dialog{min-height: 240px;overflow-y: auto;width: 435px;margin: 0!important;}
    .el-dialog__header{display: none;}
    .el-dialog__body{padding: 0;max-height: 360px;}
    .el-tree-node__content{height: 40px;line-height: 40px;margin-bottom: 1px;
      .el-tree-node__expand-icon{font-size: 14px;}
    }
  }
  .openLinkForm{
    .el-dialog{width: 600px;}
    .el-dialog__body{padding: 5px 20px 40px;}
    header{height: 50px;margin-bottom: 20px;border-bottom: 1px solid #f2f2f2;
      div{width: 84px;display: inline-block;height: 47px;line-height: 50px;text-align: center;border-bottom: 3px solid #fff;font-size: 16px;color: #69696C;cursor: pointer;margin-left: 30px;}
      div.active{color: #409eff;border-bottom: 3px solid #409eff;}
      div:first-child{margin-left: 10px;}
    }
    .email{margin: 15px 0 20px;width: calc(100% - 30px);
      input{width: calc(100% - 10px);margin-left: 10px;}
    }
    .QrCode{width: 180px;height: 180px;margin-left: calc(50% - 90px);border: 1px solid #f2f2f2;}
  }
}
.popoverSet{padding: 10px 0;width: 200px;
  li{line-height: 40px;padding: 0 20px;cursor: pointer;color: #4A4A4A;height: 40px;
    i{float: left;margin: 12px 10px 0 0;font-size: 16px;line-height: 1;}
    .uploadForm{display: inline-block;width: calc(100% + 40px);height: 40px;overflow: hidden;margin: 0 -20px;
      input{display: inline-block;float: left;opacity: 0;margin: -40px 0 0 0;height: 40px;}
      .text{font-size: 14px;float: left;width: 100px;height: 40px;border-radius: 3px;line-height: 40px;text-align: center;margin-left: 20px;color: #4A4A4A;
        i{margin: 12px 4px 0 0;}
      }
    }
  }
  li:hover{background: #F2F2F2;
    span{color: #409eff;}
    .iconfont{color: #409eff;}
    .uploadForm{
      a{color: #409eff;}
    }
  }
 }
</style>
