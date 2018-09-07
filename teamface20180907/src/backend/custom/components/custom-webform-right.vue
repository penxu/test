<template>
  <el-container class="webform-right-container">
    <el-tabs v-model="activeName" >
      <!-- <div class="field-setting" > -->
        <el-tab-pane label="字段设置" name="first">
          <div class="property-box">
            <div v-if="!isShowFieldSetting" style="padding: 150px 60px 0 60px; color: #bbb; text-align: center;">
              <span style="font-size: 21px;">请选择字段</span>
            </div>
            <el-collapse v-model="activeNames" v-show="isShowFieldSetting">
              <!-- 字段名称 -->
                <el-collapse-item name="1" v-show="myComponent.type !== 'submitArea'">
                  <template slot="title">
                    <i class="el-icon-caret-right"></i>字段名称
                  </template>
                  <div class="input-box">
                    <el-input v-model="myComponent.label" placeholder="请输入内容..." :disabled="true"></el-input>
                  </div>
                </el-collapse-item>
                <!-- 字段结构 -->
                <div v-if="judgeFrom">
                  <el-collapse-item name="22">
                    <template slot="title">
                      <i class="el-icon-caret-right"></i>字段结构
                    </template>
                    <el-radio-group size="mini" v-model="myComponent.field.structure">
                      <el-radio-button label="1">左右布局</el-radio-button>
                      <el-radio-button label="0">上下布局</el-radio-button>
                    </el-radio-group>
                  </el-collapse-item>
                  <!-- 字段控制 -->
                  <el-collapse-item name="23">
                    <template slot="title">
                      <i class="el-icon-caret-right"></i>字段控制
                    </template>
                    <el-radio-group size="mini" v-model="myComponent.field.fieldControl" :disabled="myComponent.name === 'personnel_principal'">
                      <el-radio label='1'>只读</el-radio>
                      <el-radio label='2'>必填</el-radio>
                      <el-radio label='0'>不控制</el-radio>
                    </el-radio-group>
                  </el-collapse-item>
              </div>
              <!-- 文本描述 -->
              <el-collapse-item name="24" v-if="myComponent.type === 'description'">
                <template slot="title">
                  <i class="el-icon-caret-right"></i>内容
                </template>
                <div style="height: 300px;">
                  <webformUeditor :editorContent="myComponent.text"></webformUeditor>
                </div>
              </el-collapse-item>
              <!-- 按钮 -->
              <el-collapse-item name="24" v-if="myComponent.type === 'button'">
                <template slot="title">
                  <i class="el-icon-caret-right"></i>按钮样式
                </template>
                <div class="button-box">
                  <div class="flex-box type">
                    <el-radio-group v-model="myComponent.styleType" size="small">
                      <el-radio-button label="0">默认</el-radio-button>
                      <el-radio-button label="1">自定义</el-radio-button>
                    </el-radio-group>
                  </div>
                  <div v-show="myComponent.styleType === '0'">
                    <div>按钮文字
                    <div class="input-box"><el-input v-model="myComponent.field.text"></el-input> </div>
                    </div>
                    <div class="flex-box item-box">
                    <div>按钮颜色</div>
                      <el-color-picker v-model="myComponent.field.color" size="medium" class="colorpicker"></el-color-picker>
                    </div>
                    <div class="flex-box item-box">
                    <div class="text">文字大小</div>
                      <div class="flex-box add-box">
                        <!-- <div class="emety"></div> -->
                        <div  @click="handleReduce('fontSize')"> <i class="el-icon-minus"></i></div>
                        <div class="flex-box"><el-input v-model="myComponent.field.fontSize"></el-input><div style="padding-right: 3px">px</div></div>
                        <div><i class="iconfont icon-pc-paper-additi" @click="handlePlus('fontSize')"></i></div>
                      </div>
                    </div>
                    <div class="item-box">
                    <div>按钮图标</div>
                      <div class="icon-box clear">
                        <div class="pull-left icon-item " :class="{'active-icon': item.isActive}"  v-for="(item, index) in iconList" :key="index" @click="handleSelIcon(item.icon)"><i class="iconfont" :class="item.icon"></i> <span v-show="item.icon === ''">无</span></div>
                          <div>
                        </div>
                      </div>
                    </div>
                    <div class="flex-box item-box">
                    <div class="text">按钮圆角</div>
                      <div class="flex-box add-box">
                        <!-- <div></div> -->
                        <div  @click="handleReduce('radius')"><i class="el-icon-minus"></i></div>
                        <div class="flex-box"><el-input v-model="myComponent.field.radius" type="number"></el-input><div style="padding-right: 3px">px</div></div>
                        <div><i class="iconfont icon-pc-paper-additi" @click="handlePlus('radius')"></i></div>
                      </div>
                    </div>
                    <div class="flex-box item-box">
                    <div class="text">按钮高度</div>
                      <div class="flex-box add-box">
                        <div @click="handleReduce('height')"><i class="el-icon-minus"></i></div>
                        <div class="flex-box"><el-input v-model="myComponent.field.height" type="number"></el-input><div style="padding-right: 3px">px</div></div>
                        <div><i class="iconfont icon-pc-paper-additi" @click="handlePlus('height')"></i></div>
                      </div>
                    </div>
                    <div class=" item-box">
                    <div class="text">按钮宽度</div>
                    <div style="margin-top: 10px;">
                      <!-- <el-radio-group v-model="myComponent.field.text" size="small"> -->
                        <el-radio v-model="myComponent.field.widthType" label="0">适应文字</el-radio>
                        <el-radio v-model="myComponent.field.widthType" label="1">自定义</el-radio>
                      <!-- </el-radio-group> -->
                    </div>
                    <div class="input-box" style="margin-top: 15px;" v-show="myComponent.field.widthType === '1'"><el-input v-model="myComponent.field.width"></el-input></div>
                    </div>
                  </div>
                </div>
                <!-- 上传图片 -->
                <div v-show="myComponent.styleType === '1'">
                  <div class="pull-left uploadBtn-box">
                    <label for="uploadBtn" >
                      <i class="el-icon-upload2"></i>
                      <span>上传按钮样式</span>
                      <input type="file" id="uploadBtn" v-show="false" accept="" @change="handleUploadBtn($event)">
                    </label>
                    <!-- <div class="file">
                      <i class="iconfont icon-pc-paper-xls"></i>
                      <span>{{templateFile.file_name}}</span>
                      <i class="el-icon-circle-close" @click="delTemplateFile"></i>
                    </div> -->
                  </div>
                  <div style="margin-bottom: 20px;margin-top: 5px;">
                    <span style="font-size: 12px;">尺寸</span>
                    <span style="padding-left: 15px; color: #666666;font-size: 12px;">建议宽高 ＜375像素x80像素</span>
                  </div>
                </div>
              </el-collapse-item>
              <!-- 按钮位置 -->
              <el-collapse-item name="24" v-if="myComponent.type === 'button'">
                <template slot="title">
                  <i class="el-icon-caret-right"></i>按钮位置
                </template>
                <div>
                  <el-radio-group v-model="myComponent.position">
                    <el-radio label="left" style="display: inline-block">居左</el-radio>
                    <el-radio label="center" style="display: inline-block; margin-left: 20px;">居中</el-radio>
                    <el-radio label="right" style="display: inline-block; margin-left: 20px;">居右</el-radio>
                  </el-radio-group>
                </div>
              </el-collapse-item>
              <el-collapse-item name="24" v-if="myComponent.type === 'button'">
                <template slot="title">
                  <i class="el-icon-caret-right"></i>点击事件
                </template>
                <div class="item-box">
                  <div class="btn-position flex-box">
                    <div class="text">选择类型</div>
                    <el-select v-model="myComponent.eventType" placeholder="请选择" value-key="value">
                      <el-option
                        v-for="item in btnEventList"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                      </el-option>
                    </el-select>
                  </div>
                  <div style="margin-top: 15px;">
                    <div  v-show="myComponent.eventType === '1'">
                      <div class="text">链接地址</div>
                      <div class="input-box"><el-input v-model="myComponent.field.eventText"><template slot="prepend">Http://</template></el-input></div>
                    </div>
                    <div v-show="myComponent.eventType === '2'">
                      <div class="text" >手机号码</div>
                      <div class="input-box"><el-input v-model="myComponent.field.eventText"></el-input></div>
                    </div>
                    <div v-show="myComponent.eventType === '3'">
                      <div class="text">手机号码</div>
                      <div class="input-box"><el-input v-model="myComponent.field.eventText"></el-input></div>
                    </div>
                    <div v-show="myComponent.eventType === '4'">
                      <div class="text">发邮件</div>
                      <div class="input-box"><el-input v-model="myComponent.field.eventText"></el-input></div>
                    </div>
                  </div>
                </div>
              </el-collapse-item>
              <!-- 线型 -->
              <el-collapse-item name="24" v-if="myComponent.type === 'splitLine'">
                <template slot="title">
                  <i class="el-icon-caret-right"></i>线型
                </template>
                <div class="item-box">
                  <el-popover
                    placement="bottom-end"
                    width="240"
                    trigger="click"
                    content="">
                    <div slot="reference" class="splitLineBtn flex-box" id="splitLineId">
                      <div class="line-type">
                        <div style="border-top: 1px solid #333333" v-show="myComponent.field.lineType === 'solid'"></div>
                        <div style="border-top: 1px dashed #333333" v-show="myComponent.field.lineType === 'dashed'"></div>
                        <div style="border-top: 1px dotted #333333" v-show="myComponent.field.lineType === 'dotted'"></div>
                        <div style="border-top: 3px double #333333" v-show="myComponent.field.lineType === 'double'"></div>
                      </div>
                      <div><i class="iconfont icon-nav-on-module"></i></div>
                    </div>
                    <div v-for="lineType in lineTypeList" :key="lineType.type" class="webform-line-box" @click="handleSelLineType('style',lineType)">
                      <div :style="{borderTop: lineType.type}" style="height: 1px;"></div>
                    </div>
                  </el-popover>
                </div>
              </el-collapse-item>
              <!-- 线宽 -->
              <el-collapse-item name="24" v-if="myComponent.type === 'splitLine'">
                <template slot="title">
                  <i class="el-icon-caret-right"></i>线宽
                </template>
                <div class="item-box">
                  <el-popover
                    placement="bottom-end"
                    width="240"
                    trigger="click"
                    content="">
                    <div slot="reference" class="splitLineBtn flex-box" id="lineWidthId">
                      <div class="line-width">
                        <div style="background: #333;" :style="{height: myComponent.field.lineWidth}"></div>
                      </div>
                      <div><i class="iconfont icon-nav-on-module"></i></div>
                    </div>
                    <div v-for="lineWidth in lineWidthList" :key="lineWidth.value" class="line-box" @click="handleSelLineType('width',lineWidth)">
                      <div :style="{height: lineWidth.value}" :title="lineWidth.label" style="background: #333;">
                      </div>
                    </div>
                  </el-popover>
                </div>
              </el-collapse-item>
              <!-- 线宽 -->
              <el-collapse-item name="24" v-if="myComponent.type === 'splitLine'">
                <template slot="title">
                  <i class="el-icon-caret-right"></i>线条颜色
                </template>
                <div class="">
                  <el-color-picker v-model="myComponent.field.lineColor"></el-color-picker>
                </div>
              </el-collapse-item>
              <!-- 图片 -->
              <el-collapse-item name="23" v-if="myComponent.type === 'imageShow'">
                <template slot="title">
                  <i class="el-icon-caret-right"></i>图片
                </template>
                <div class="img-item">
                  <draggable :options="dragOptions">
                    <div v-for="(imgItem, imgIndex) in myComponent.imageList" :key="imgItem.name">
                      <div class="imgshow-box">
                        <div class="img-btn-box">
                          <label :for="'uploadImg' + imgIndex" v-show="imgItem.imageUrl === ''">
                            <div><i class="el-icon-upload2"></i></div>
                            <span>点击上传</span>
                            <input type="file" :id="'uploadImg' + imgIndex" v-show="false" accept="" @change="handleUploadImg($event, imgIndex)">
                          </label>
                          <img :src="imgItem.imageUrl" alt="" v-show="imgItem.imageUrl !== ''">
                        </div>
                        <div class="flex-box img-handle">
                          <div><el-checkbox  v-model="imgItem.isJumpUrl" true-label="1" false-label="0">链接地址</el-checkbox ></div>
                          <div><i class="iconfont icon-huishouzhan1" @click="handleDelImg(imgIndex)"></i><i class="iconfont icon-liebiao drag-handle"></i></div>
                        </div>
                      </div>
                      <div v-show="imgItem.isJumpUrl === '1'" class="url-box"><el-input placeholder="请输入内容" v-model="imgItem.jumpUrl" size="small"> <template slot="prepend">http://</template> </el-input></div>
                    </div>
                  </draggable>
                  <div class="addImg-btn"><el-button plain size="small" @click="handleAddImg('img')"><i class="el-icon-plus"></i> 添加</el-button></div>
                  <div>
                    <div class="margin-bottom: 10px">展示样式</div>
                    <div v-show="myComponent.imageList.length === 1" class="img-grid-box">
                      <div class="grid-item">
                        <i class="el-icon-success"></i><img src="/static/img/webform/1picture.png" alt="">
                      </div>
                      </div>
                    <div v-show="myComponent.imageList.length === 2" class="img-grid-box">
                      <div class="grid-item">
                        <i class="el-icon-success"></i><img src="/static/img/webform/2picture.png" alt="">
                      </div>
                    </div>
                    <div v-show="myComponent.imageList.length === 3" class="img-grid-box">
                      <div v-for="(gridImg, index) in gridImgList.threeImg" :key="index" @click="handleSelGridImg('3', gridImg)" class="grid-item">
                        <i class="el-icon-success" v-show="gridImg.isUse"></i>
                        <img :src="gridImg.url" alt="">
                      </div>
                      <!-- <div><i class="el-icon-success" v-show="imgItem.field.grid === '3-2'"></i><img src="/static/img/webform/3picture2.png" alt=""></div>
                      <div><i class="el-icon-success" v-show="imgItem.field.grid === '3-3'"></i><img src="/static/img/webform/3picture3.png" alt=""></div> -->
                    </div>
                    <div v-show="myComponent.imageList.length === 4" class="img-grid-box">
                      <div v-for="(gridImg, index) in gridImgList.fourthImg" :key="index" @click="handleSelGridImg('4', gridImg)" class="grid-item">
                        <i class="el-icon-success" v-show="gridImg.isUse"></i>
                        <img :src="gridImg.url" alt="">
                      </div>
                    </div>
                  </div>
                </div>
              </el-collapse-item>
              <!-- 图片轮播 -->
              <el-collapse-item name="23" v-if="myComponent.type === 'imagecarousel'">
                <template slot="title">
                  <i class="el-icon-caret-right"></i>轮播数量
                </template>
                <div class="img-item">
                  <draggable :options="dragOptions">
                    <div v-for="(imgItem, imgIndex) in myComponent.imageList" :key="imgItem.name">
                      <div class="imgshow-box">
                        <div class="img-btn-box">
                          <label :for="'uploadImg' + imgIndex" v-show="imgItem.img === ''">
                            <div><i class="el-icon-upload2"></i></div>
                            <span>点击上传</span>
                            <input type="file" :id="'uploadImg' + imgIndex" v-show="false" accept="" @change="handleUploadImg($event, imgIndex)">
                          </label>
                          <img :src="imgItem.img" alt="" v-show="imgItem.img !== ''">
                        </div>
                        <div class="flex-box img-handle">
                          <div><el-checkbox  v-model="imgItem.isJumpUrl" true-label="1" false-label="0">链接地址/描述</el-checkbox ></div>
                          <div><i class="iconfont icon-huishouzhan1" @click="handleDelImg(imgIndex)"></i><i class="iconfont icon-liebiao drag-handle"></i></div>
                        </div>
                      </div>
                      <div v-show="imgItem.isJumpUrl === '1'" class="url-box">
                        <el-input
                          type="textarea"
                          :rows="3.5"
                          placeholder="请输入描述内容"
                          v-model="imgItem.title"
                          maxlength="25">
                        </el-input>
                      </div>
                      <div v-show="imgItem.isJumpUrl === '1'" class="url-box"><el-input placeholder="请输入..." v-model="imgItem.url" size="small"> <template slot="prepend">http://</template> </el-input></div>
                    </div>
                  </draggable>
                  <div class="addImg-btn"><el-button plain size="small" @click="handleAddImg('carousel')"><i class="el-icon-plus"></i> 添加</el-button></div>
                </div>
              </el-collapse-item>
              <!-- 图片轮播-基础设置 -->
              <el-collapse-item name="23" v-if="myComponent.type === 'imagecarousel'">
                <template slot="title">
                  <i class="el-icon-caret-right"></i>基础设置
                </template>
                <div class="flex-box item-box">
                <div class="text">轮播速度</div>
                  <div class="flex-box add-box">
                    <!-- <div ></div> -->
                    <div  @click="handleReduce('interval')"> <i class="el-icon-minus"></i></div>
                    <div class="flex-box"><el-input v-model="myComponent.field.interval" type="number"></el-input><div>秒</div></div>
                    <div><i class="iconfont icon-pc-paper-additi" @click="handlePlus('interval')"></i></div>
                  </div>
                </div>
                <div class="flex-box item-box">
                <div class="text">图片高度</div>
                  <div class="flex-box add-box">
                    <!-- <div ></div> -->
                    <div  @click="handleReduce('height')"><i class="el-icon-minus"></i></div>
                    <div class="flex-box"><el-input v-model="myComponent.field.height" type="number"></el-input><div>px</div></div>
                    <div><i class="iconfont icon-pc-paper-additi" @click="handlePlus('height')"></i></div>
                  </div>
                </div>
              </el-collapse-item>
              <!-- 视频地址 -->
              <el-collapse-item name="23" v-if="myComponent.type === 'video'">
                <template slot="title">
                  <i class="el-icon-caret-right"></i>视频地址
                  <el-popover
                    placement="top-bottom"
                    width="200"
                    trigger="hover"
                    content="">
                    <span style="font-size: 12px">支持腾讯视频、优酷、爱奇艺、土豆。</span>
                  <i class="el-icon-warning" slot="reference" style="color: #F7BA2A"></i>
                  </el-popover>
                </template>
                <div class="url-box">
                  <el-input
                    type="textarea"
                    :rows="3"
                    placeholder="请将外部媒体地址粘贴至输入框"
                    v-model="myComponent.url">
                  </el-input>
                </div>
              </el-collapse-item>
              <!-- 静态地图 -->
              <el-collapse-item name="23" v-if="myComponent.type === 'staticMap'">
                <template slot="title">
                  <i class="el-icon-caret-right"></i>显示地点
                </template>
                <div class="url-box">
                  <el-input
                    type="textarea"
                    :rows="3"
                    placeholder="输入目标地址"
                    v-model="myComponent.address"
                    id="searchMap">
                  </el-input>
                </div>
                <div style="margin-bottom: 20px;"><el-checkbox  v-model="myComponent.isMarkMsg" true-label="1" false-label="0">在地图标记点上显示信息框</el-checkbox ></div>
              </el-collapse-item>
              <!-- 地图比例 -->
              <!-- <el-collapse-item name="23" v-if="myComponent.type === 'staticMap'">
                <template slot="title">
                  <i class="el-icon-caret-right"></i>地图比例
                </template>
                <div class="flex-box staticmap-box">
                  <div class="text">小</div>
                  <el-slider
                    v-model="myComponent.scale"
                    :step=1
                    :min=3
                    :max=18
                    @change="handleMapScale">
                  </el-slider>
                  <div class="text">大</div>
                </div>
              </el-collapse-item> -->
              <!-- 定时器 -->
              <el-collapse-item name="23" v-if="myComponent.type === 'timer'">
                <template slot="title">
                  <i class="el-icon-caret-right"></i>计时器类型
                </template>
                <div>
                  <el-radio-group v-model="myComponent.form">
                    <el-radio label="0" style="display: inline-block">计时</el-radio>
                    <el-radio label="1" style="display: inline-block; margin-left: 40px;">倒计时</el-radio>
                  </el-radio-group>
                </div>
                <div v-show="myComponent.form === '0'">
                  <div class="flex-box">
                    <div style="width: 90px;line-height: 32px;">显示格式</div>
                    <div>
                    <el-select v-model="myComponent.formatType" placeholder="请选择" value-key="value" size="small">
                      <el-option
                        v-for="item in timerTypeList"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                        v-show ="item.value === '2'|| item.value === '3'">
                      </el-option>
                    </el-select>
                    </div>
                  </div>
                  <div style="margin-top: 10px;">
                    <div>初始值(秒)</div>
                    <el-input size="small" placeholder="请输入..." v-model="myComponent.initialTime"></el-input>
                  </div>
                </div>
                <div v-show="myComponent.form === '1'">
                  <div class="flex-box">
                    <div style="width: 90px;line-height: 32px;">显示格式</div>
                    <div>
                    <el-select v-model="myComponent.countDownType" placeholder="请选择" value-key="value" size="small">
                      <el-option
                        v-for="item in timerTypeList"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                        >
                      </el-option>
                    </el-select>
                    </div>
                  </div>
                  <div style="margin-top: 10px;">
                    <div>到达日期</div>
                    <el-date-picker
                      v-model="myComponent.arrivalTime"
                      type="datetime"
                      placeholder="选择日期时间" size="small"
                      value-format="timestamp"
                      :picker-options="pickdateOptions">
                    </el-date-picker>
                  </div>
                </div>
              </el-collapse-item>
              <!-- 提交区 -->
              <div v-if="myComponent.type === 'submitArea'">
                <!-- 提交区-按钮文字 -->
                <el-collapse-item name="23">
                  <template slot="title">
                    <i class="el-icon-caret-right"></i>按钮文字
                  </template>
                  <div class="input-box"><el-input v-model="myComponent.field.text"></el-input> </div>
                </el-collapse-item>
                <!-- 提交区-按钮宽度 -->
                <el-collapse-item name="23">
                  <template slot="title">
                    <i class="el-icon-caret-right"></i>按钮宽度
                  </template>
                  <div class="input-box">
                    <el-select v-model="myComponent.field.widthType" placeholder="请选择" value-key="value" size="small">
                      <el-option
                        v-for="item in sumbitWidthList"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                      </el-option>
                    </el-select>
                  </div>
                </el-collapse-item>
                <!-- 提交区- 提交反馈 -->
                <el-collapse-item name="23">
                  <template slot="title">
                    <i class="el-icon-caret-right"></i>提交反馈
                  </template>
                  <div class="input-box">
                    <el-select v-model="myComponent.field.feedbackType" placeholder="请选择" value-key="value" size="small">
                      <el-option
                        v-for="item in sumbitTypeList"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                      </el-option>
                    </el-select>
                  </div>
                  <div class="input-box" v-show="myComponent.field.feedbackType === '0'">
                    <div>标题</div>
                    <el-input v-model="myComponent.field.title"></el-input>
                  </div>
                  <div class="input-box" v-show="myComponent.field.feedbackType === '1'">
                    <div>跳转链接</div>
                    <div class="url-box"><el-input placeholder="请输入内容" v-model="myComponent.field.jumpUrl" size="small"> <template slot="prepend">http://</template> </el-input></div>
                  </div>
                </el-collapse-item>
                <!-- 提交区- 描述 -->
                <el-collapse-item name="23">
                  <template slot="title">
                    <i class="el-icon-caret-right"></i>描述
                  </template>
                  <div class="input-box">
                    <el-input v-model="myComponent.field.description" placeholder="我们已收到您的反馈，感谢填写。" :rows="5" type="textarea"></el-input>
                  </div>
                </el-collapse-item>
              </div>
            </el-collapse>
          </div>
        </el-tab-pane>
      <!-- </div> -->
      <!-- <div class="form-setting"> -->
        <el-tab-pane label="表单设置" name="second" class="form-setting">
          <el-collapse v-model="activeNames" v-show="isShowFieldSetting">
              <!-- 表单名称 -->
            <el-collapse-item name="1">
              <template slot="title">
                <i class="el-icon-caret-right"></i>表单名称
              </template>
              <div class="input-box">
                <el-input v-model="formProps.title" placeholder="请输入内容..." maxlength="25"></el-input>
              </div>
            </el-collapse-item>
            <!-- 启用外观设置 -->
            <el-collapse-item name="2">
              <template slot="title">
                <i class="el-icon-caret-right"></i>启用外观设置
              </template>
              <el-radio-group size="mini" v-model="formProps.isAppearance">
                <el-radio label='1' style="display: inline-block">是</el-radio>
                <el-radio label='0' style="display: inline-block; padding-left: 76px;">否</el-radio>
              </el-radio-group>
            </el-collapse-item>
            <!-- 填写权限 -->
            <el-collapse-item name="2">
              <template slot="title">
                <i class="el-icon-caret-right"></i>填写权限
              </template>
              <el-radio-group size="mini" v-model="formProps.accessAuth">
                <el-radio label='1' style="display: inline-block">公开访问</el-radio>
                <el-radio label='0' style="display: inline-block; padding-left: 37px;">凭密码访问</el-radio>
              </el-radio-group>
              <div class="input-box" v-show="formProps.accessAuth === '0'">
                <el-input v-model="formProps.accessPassword" placeholder="设置6位密码" maxlength="6"></el-input>
              </div>
            </el-collapse-item>
            <!-- 链接拓展 -->
            <el-collapse-item name="2">
              <template slot="title">
                <i class="el-icon-caret-right"></i>链接拓展
                <el-popover
                  placement="top-bottom"
                  width="264"
                  trigger="hover"
                  content="">
                  <span style="font-size: 12px">对同一个表单赋予扩展属性值使该表单拥有多个链接地址，用于标识不同数据。</span>
                <i class="el-icon-warning" slot="reference" style="color: #F7BA2A"></i>
                </el-popover>
              </template>
              <div class="input-box">
                <el-button type="primary" size="small" style="width: 100%;" @click="handlelinkDialog">扩展外链列表({{linkNum}}个)</el-button>
              </div>
            </el-collapse-item>
              <!-- 分享设置 -->
            <el-collapse-item name="1">
              <template slot="title">
                <i class="el-icon-caret-right"></i>分享设置
                <el-popover
                  placement="top-bottom"
                  width="264"
                  trigger="hover"
                  content="">
                  <span style="font-size: 12px">分享设置信息用于分享表单时显示的样式内容，用于分享到第三方平台。</span>
                <i class="el-icon-warning" slot="reference" style="color: #F7BA2A"></i>
                </el-popover>
              </template>
              <div class="input-box">
                <div style="line-height: 30px;">标题</div>
                <el-input v-model="formProps.shareTitle" placeholder="请输入内容..."></el-input>
              </div>
            </el-collapse-item>
            <!-- 描述 -->
            <el-collapse-item name="1">
              <template slot="title">
                <i class="el-icon-caret-right"></i>描述
              </template>
              <div class="input-box">
                <el-input v-model="formProps.shareDescription" placeholder="请输入内容..." :rows="5" type="textarea"></el-input>
                <!-- <div style="margin-top: 20px">
                  <div >图片 <span style="font-size: 10px; color: #666666">建议使用300*300或更大尺寸的图片</span></div>
                    <div class="upload-box form-image">
                      <el-button icon="el-icon-upload" size="small">上传图片</el-button>
                      <input type="file" id="fileElem" accept="image/*" class="upload-input" @change="handleUpload($event)">
                      <div><span></span></div>
                    </div>
                </div> -->
              </div>
            </el-collapse-item>
            <!-- 签到设置 -->
            <!-- 是否启用签到 -->
            <el-collapse-item name="2">
              <template slot="title">
                <i class="el-icon-caret-right"></i>是否签到
              </template>
              <el-radio-group size="mini" v-model="formProps.isSignIn">
                <el-radio label='1' style="display: inline-block">启用</el-radio>
                <el-radio label='0' style="display: inline-block; padding-left: 37px;">关闭</el-radio>
              </el-radio-group>
            </el-collapse-item>
            <div v-show="formProps.isSignIn === '1'">
              <!-- 活动主题 -->
              <el-collapse-item name="1">
                <template slot="title">
                  <i class="el-icon-caret-right"></i>活动主题
                </template>
                <div class="input-box">
                  <el-input v-model="formProps.signInMsg.title" placeholder="请输入..." ></el-input>
                </div>
              </el-collapse-item>
              <!-- 描述 -->
              <el-collapse-item name="1">
                <template slot="title">
                  <i class="el-icon-caret-right"></i>描述
                </template>
                <div class="input-box">
                  <el-input v-model="formProps.signInMsg.content" placeholder="请输入内容..." :rows="5" type="textarea"></el-input>
                </div>
              </el-collapse-item>
              <!-- 验证字段 -->
              <el-collapse-item name="1">
                <template slot="title">
                  <i class="el-icon-caret-right"></i>验证字段
                </template>
                    <div class="input-box">
                      <el-select v-model="formProps.signInMsg.verfifyField" placeholder="请选择" value-key="name" size="small">
                        <el-option
                          v-for="item in currentLayout"
                          :key="item.value"
                          :label="item.label"
                          :value="item.name"
                          v-show="!item.source">
                        </el-option>
                      </el-select>
                    </div>
              </el-collapse-item>
              <!-- 签到限制 -->
              <el-collapse-item name="1">
                <template slot="title">
                  <i class="el-icon-caret-right"></i>签到限制
                </template>
                <div class="input-box">
                  <el-checkbox v-model="formProps.signInMsg.isOpenSign" true-label="1"  false-label="0">允许开放式签到</el-checkbox>
                  <!-- <el-checkbox v-model="formProps.signInMsg.isClientOnce" true-label="1"  false-label="0">每台设备限签一次</el-checkbox> -->
                </div>
              </el-collapse-item>
            </div>
          </el-collapse>
        </el-tab-pane>
      <!-- </div> -->
    </el-tabs>
    <!-- 弹窗 -->
    <div v-if="linkVisable.show">
      <webformLinkDialog :linkVisableObj="linkVisable" :linkData="formProps.expandLink"></webformLinkDialog>
    </div>
  </el-container>
</template>
<script>
import WebformUeditor from '@/backend/custom/components/webform-editer'
import webformLinkDialog from '@/backend/custom/components/webfrom-link-dialog'
import { mapGetters, mapState, mapMutations } from 'vuex'
import {HTTPServer} from '@/common/js/ajax.js'
import draggable from 'vuedraggable'
export default {
  name: 'CustomWebformRight',
  components: {
    draggable,
    // DefinedSubform
    WebformUeditor,
    webformLinkDialog
  },
  data () {
    return {
      myApplication: [],
      myModule: {},
      myColumn: {},
      myComponent: {
        // 拖拽传过来的数据
        field: {},
        numbering: {},
        imageList: []
      },
      settingsType: 0,
      activeNames: [ // 折叠板的具体展开选项
        '1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11',
        '12', '13', '14', '15', '16', '17', '18', '19', '20',
        '21', '22', '23', '24', '25', '26', '27', '28', '29'
      ],
      client_height: 0,
      dateList: ['yyyy', 'yyyy-MM', 'yyyy-MM-dd', 'yyyy-MM-dd HH', 'yyyy-MM-dd HH:mm', 'yyyy-MM-dd HH:mm:ss'],
      dateTimeOptions: [{ value: '0', label: '-无-' }, { value: '1', label: '当前时间' }, { value: '2', label: '指定时间' }],
      imageSizeList: ['30px*30px', '60px*60px'], // 图片大小
      isShowFieldSetting: true,
      linkVisable: {show: false},
      activeName: 'second',
      formProps: {},
      iconList: [{icon: '', isActive: true}, {icon: 'icon-apps-library-3-b'}, {icon: 'icon-apps-library-2-b'}, {icon: 'icon-modules-library-13-b'}, {icon: 'icon-apps-library-8-b'}, {icon: 'icon-system-library-8'}, {icon: 'icon-add-group-chat2'}, {icon: 'icon-apps-library-7-b'}, {icon: 'icon-modules-library-7-b'}, {icon: 'icon-modules-library-4-b'}, {icon: 'icon-modules-library-13-b'}, {icon: 'icon-modules-library-12-b'}, {icon: 'icon-modules-library-10-b'}, {icon: 'icon-apps-library-4-b'}],
      'btnEventList': [{label: '无', value: '0'}, {label: '跳转链接', value: '1'}, {label: '拨打电话', value: '2'}, {label: '发短信', value: '3'}, {label: '发邮件', value: '4'}],
      lineTypeList: [{'type': '1px solid #333333', 'value': 'solid', 'label': '实线'}, {'type': '1px dashed #333333', 'value': 'dashed', 'label': '虚线'}, {'type': '1px dotted #333333', 'value': 'dotted', 'label': '点划线'}, {'type': '3px double #333333', 'value': 'double', 'label': '双实线'}],
      lineWidthList: [{'value': '1px', 'label': '1px'}, {'value': '2px', 'label': '3px'}, {'value': '5px', 'label': '5px'}, {'value': '10px', 'label': '10px'}],
      dragOptions: {
        'animation': 200,
        'group': { name: 'compontents', pull: false, put: false },
        'sort': true,
        'ghostClass': 'component-box',
        'filter': '.no-drag',
        'handle': '.drag-handle'
      },
      gridImgList: {
        threeImg: [
          {isUse: true, grid: '3-1', url: '/static/img/webform/3picture3.png'},
          {isUse: false, grid: '3-2', url: '/static/img/webform/3picture.png'},
          {isUse: false, grid: '3-3', url: '/static/img/webform/3picture2.png'}
        ],
        fourthImg: [
          {isUse: true, grid: '4-1', url: '/static/img/webform/4picture1.png'},
          {isUse: false, grid: '4-2', url: '/static/img/webform/4picture2.png'},
          {isUse: false, grid: '4-3', url: '/static/img/webform/4picture3.png'}
        ]
      },
      timerTypeList: [
        {value: '0', label: '天'},
        {value: '1', label: '时'},
        {value: '2', label: '时：分：秒'},
        {value: '3', label: '天：时：分：秒'}
      ],
      sumbitTypeList: [
        {value: '0', label: '提交成功提示'},
        {value: '1', label: '跳转链接'}
      ],
      sumbitWidthList: [
        {value: '0', label: '自适应'},
        {value: '1', label: '与表单同宽'}
      ],
      // 禁用日期
      pickdateOptions: {
        disabledDate: (time) => {
          return new Date().getTime() - time.getTime() > 0
        }
      },
      currentLayout: []
    }
  },
  created () {
    // this.ajaxGetApplication()
    this.myModule = {
      bean: 'bean' + new Date().getTime(),
      title: '',
      version: '0', // 模块版本
      icon: 'icon-mokuai-xian4', // 模块图标
      applicationId: this.$route.query.appId, // 应用id
      applicationName: this.$route.query.appName, // 应用名称
      pageNum: '0', // 模块页码
      commentControl: '1', // 详情评论控件
      dynamicControl: '1', // 详情动态控件
      terminalPc: '1', // PC终端
      terminalApp: '1', // APP终端
      enableLayout: {}, // 使用字段布局
      disableLayout: {}// 未使用字段布局
    }
    this.formProps = JSON.parse(JSON.stringify(this.getFromProps()))
    console.log(this.formProps, '渲染前....')
  },
  methods: {
    // 获取屏幕高度
    getHeight () {
      this.client_height = document.documentElement.clientHeight - 160 + 'px'
      console.log(this.client_height)
    },
    // 上传按钮图片
    handleUploadBtn (event) {
      console.log(event, '')
      let formdata = new FormData()
      formdata.append('file', event.target.files[0])
      console.log(event.target.files[0], 'event.target.files')
      event.target.value = ''
      // 上传文件,获取路径
      HTTPServer.emailImageUpload(formdata).then((res) => {
        console.log(res, '上传成功')
        this.myComponent.imgSrc = res[0].file_url
        this.$message({type: 'success', message: '上传成功！', showClose: true})
      })
    },
    // 上传图片组件
    handleUploadImg (event, index) {
      console.log(event, index, '上传图片.....')
      let formdata = new FormData()
      formdata.append('file', event.target.files[0])
      console.log(event.target.files[0], 'event.target.files')
      event.target.value = ''
      // 上传文件,获取路径
      HTTPServer.emailImageUpload(formdata).then((res) => {
        console.log(res, '上传成功')
        if (this.myComponent.type === 'imageShow') {
          this.myComponent.imageList[index].imageUrl = res[0].file_url
        } else {
          this.myComponent.imageList[index].img = res[0].file_url
        }
        this.$message({type: 'success', message: '上传成功！', showClose: true})
        console.log(this.myComponent, '上传后的组件')
      })
    },
    // 打开外链
    handlelinkDialog () {
      this.renderLinkVisable = true
      this.linkVisable.show = true
      console.log(this.linkVisable, 'show')
    },
    // 选择线框类型
    handleSelLineType (type, data) {
      if (type === 'style') {
        this.myComponent.field.lineType = data.value
        document.getElementById('splitLineId').click()
      } else {
        this.myComponent.field.lineWidth = data.value
        document.getElementById('lineWidthId').click()
      }
    },
    // 加法
    handlePlus (type) {
      this.myComponent.field[type] += 1
      console.log(this.myComponent.field, '增加后')
    },
    // 减法
    handleReduce (type) {
      this.myComponent.field[type] -= 1
      console.log(this.myComponent.field, '减少后')
    },
    // 选择图标
    handleSelIcon (data) {
      this.myComponent.field.icon = data
    },
    // 点击删除图片
    handleDelImg (index) {
      this.myComponent.imageList.splice(index, 1)
    },
    // 点击添加图片
    handleAddImg (type) {
      let len = this.myComponent.imageList.length
      if (type === 'img') {
        if (len < 4) {
          let imgObj = {
            imageUrl: '', isJumpUrl: '0', jumpUrl: ''
          }
          this.myComponent.imageList.push(imgObj)
          if (this.myComponent.imageList.length === 3) {
            this.myComponent.field.grid = '3-1'
          } else if (this.myComponent.imageList.length === 4) {
            this.myComponent.field.grid = '4-1'
          }
        } else {
          this.$message({type: 'warning', message: '最多添加4张图片！', showClose: true})
        }
      } else {
        if (len < 5) {
          let imgObj = { img: '', isJumpUrl: '0', url: '', title: '' }
          this.myComponent.imageList.push(imgObj)
        } else {
          this.$message({type: 'warning', message: '最多添加5张图片！', showClose: true})
        }
      }
    },
    //  点击选择图片布局
    handleSelGridImg (type, imgItem) {
      console.log(type, imgItem, '单前点击使用的布局')
      if (type === '3') {
        this.gridImgList.threeImg.map((item, itemIndex) => {
          item.isUse = false
        })
      } else {
        this.gridImgList.fourthImg.map((item, itemIndex) => {
          item.isUse = false
        })
      }
      console.log(this.gridImgList.threeImg, '3张的布局')
      imgItem.isUse = true
      this.myComponent.field.grid = imgItem.grid
    },
    // 改变地图比例
    handleMapScale (data) {
      console.log(data, '改变地图比例')
      this.$bus.emit('changeMapSize', data)
    },
    ...mapMutations({
      setFromProps: 'SET_WEBFOTM_PROPS',
      setWebformLayout: 'SET_WEBFORM_LAYOUT'
    })
  },
  // 页面加载完成
  mounted () {
    this.getHeight()
    this.$bus.on('ajaxSendModule', value => {
      delete value.layout
      this.myModule = value
    })
    this.$bus.off('sendTaskProperty')
    this.$bus.on('sendTaskProperty', (singlevalue, allValue) => {
      if (singlevalue) {
        this.settingsType = 2
        this.activeName = 'first'
        console.log(singlevalue, '传递')
        this.myComponent = singlevalue
        this.isShowFieldSetting = true
        // if (singlevalue.type === 'reference') {
        //   this.ajaxGetModuleList()
        //   if (singlevalue.relevanceModule.moduleName) {
        //     let bean = {bean: singlevalue.relevanceModule.moduleName}
        //     this.ajaxGetFieldList(bean)
        //   }
        // } else if (singlevalue.type === 'formula' || singlevalue.type === 'functionformula' || singlevalue.type === 'seniorformula') {
        // // 处理公式列表
        //   console.log(allValue, '公式传递')
        //   this.formulaItems = []
        //   if (allValue) {
        //     allValue.map((item, index) => {
        //       let subform = item.componentList ? item.componentList : []
        //       this.formulaItems.push(
        //         {label: item.label,
        //           value: item.name,
        //           type: item.type,
        //           subform: subform
        //         })
        //     })
        //   }
        // } else if (singlevalue.name === 'picklist_tag') {
        //   this.isShowFieldSetting = false
        // }
      }
    })
    this.$bus.off('linkArr')
    this.$bus.on('linkArr', value => {
      this.formProps.expandLink = JSON.parse(JSON.stringify(value))
    })
    console.log(this.formProps, '表单属性')
  },
  computed: {
    judgeFrom () {
      if (this.myComponent.type === 'description' || this.myComponent.type === 'splitLine' || this.myComponent.type === 'button' || this.myComponent.type === 'imageShow' || this.myComponent.type === 'imagecarousel' || this.myComponent.type === 'video' || this.myComponent.type === 'staticMap' || this.myComponent.type === 'timer' || this.myComponent.type === 'attendance' || this.myComponent.type === 'submitArea') {
        return false
      } else {
        return true
      }
    },
    linkNum () {
      if (this.formProps.expandLink.length) {
        console.log(this.formProps.expandLink.length, '拓展链接')
        return this.formProps.expandLink.length
      }
    },
    ...mapGetters({
      getFromProps: 'getFromProps',
      getEnableLayout: 'getEnableLayout'
    }),
    ...mapState({
      currentIndex: state => state.taskCustom.currentIndex,
      webform_layout: state => state.webformCustom.webform_layout
    })
  },
  watch: {
    myComponent: {
      handler: function (val, oldval) {
        console.log(val, '改变组件设置')
        // this.setWebformLayout(JSON.parse(JSON.stringify(this.myComponent)))
      },
      deep: true
    },
    formProps: {
      handler: function (val, oldval) {
        console.log(val, '改变表单设置')
        this.setFromProps(this.formProps)
      },
      deep: true
    },
    'webform_layout.enableLayout': {
      handler: function (val, oldval) {
        console.log(val, '右边最新布局')
        this.currentLayout = this.getEnableLayout()
      },
      deep: true
    }
  },
  filters: {
  }
}
</script>

<style lang="scss">
.webform-right-container{
  flex: 0 0 300px;
  background: #FFFFFF;
  text-align: left;
  box-shadow: 0 2px 4px 0 rgba(154,158,161,0.75);
  height: 100%;
  .el-tabs {
    height: 100%;
    width: 100%;
    .el-tabs__header {
      margin: 0;
    }
    .el-tabs__content {
      padding-top: 15px;
      height: calc(100% - 40px);
      overflow: auto;
      box-sizing: border-box;
      .el-tab-pane {
        height: 100%;
        .el-collapse {
          height: 100%;
          overflow: inherit;
          .el-collapse-item:not(:last-child) {
            border-bottom: 1px solid #E7E7E7;
          }
        }
      }
    }
  }
  .form-setting {
    // height: calc(100% - 93px);
    // overflow: auto;
    .el-textarea__inner, .el-input__inner {
      background: #FBFBFB
    }
    .form-image {
      margin-top: 10px;
    }
    .upload-box {
      position: relative;
      width: 100%;
      .el-button {
        width: 100%;
      }
      .upload-input {
        opacity: 0;
        position: absolute;
        left: 0;
        top: 0;
        cursor: pointer;
        font-size:0;
        height: 100%;
        width: 100%;
      }
    }
  }
  .el-tabs__nav {
    width: 100%;
    .el-tabs__item {
      width: 50%;
      font-size: 16px;
      text-align: center;
      // border-bottom: 4px solid #1890FF;
    }
    .el-tabs__active-bar {
      height: 4px;
    }
  }
  .property-box {
    height: 100%;
    .button-box {
      div.type {
        span.el-radio-button__inner {
          width: 115px;
          display: inline-block;
        }
      }
    }
    .item-box {
      margin: 20px 0;
      .colorpicker {
        margin-top: -5px;
        margin-left: 10px;
      }
      .add-box {
        border: 1px solid #E7E7E7;
        line-height: 33px;
        border-radius: 2px;
        margin-top: -5px;
        div.emety:nth-child(1), >div:nth-child(1), >div:nth-child(3) {
          text-align: center;
          width: 33px;
          i {
            line-height: 33px;
            color: #666666;
            cursor: pointer;
          }
        }
        >div:nth-child(2) {
          width: 62px;
          .el-input__inner {
            height: 32px;
            border: none;
            padding: 3px;
            // width: 50px;
          }
        }
        >div:not(:last-child) {
          border-right: 1px solid #E7E7E7;
          height: 33px
        }
      }
      >div.text {
        margin-right: 10px;
        // line-height: 33px;
      }
      .icon-box {
        border-left: 1px solid #E7E7E7;
        border-top: 1px solid #E7E7E7;
        width: 100%;
        .icon-item {
          height: 33px;
          width: calc(100%/7);
          border-right: 1px solid #E7E7E7;
          border-bottom: 1px solid #E7E7E7;
          text-align: center;
          cursor: pointer;
          i {
            color: #1890FF;
          }
        }
      }
      .btn-position {
        margin-top: -15px;
        .text {
          width: 80px;
        }
        .el-select {
          margin-top: -5px;
        }
        .el-input__inner {
          height: 32px;
        }
      }
      .input-box {
        .el-input-group__prepend {
          padding: 0 7px;
        }
      }
      .splitLineBtn {
        height: 33px;
        border: 1px solid #E7E7E7;
        border-radius: 2px;
        justify-content: space-between;
        padding-left: 5px;
        position: relative;
        cursor: pointer;
        >div:nth-child(2) {
          height: 100%;
          line-height: 31px;
          border-left: 1px solid #E7E7E7;
          width: 33px;
          text-align: center;
          height: 31px;
        }
        div.line-width {
          height: 31px;
          padding: 0 10px;
          position: relative;
          div {
            width: 180px;
            position:absolute;
            top:0;
            left:10px;
            right:0;
            bottom:0;
            margin:auto;
            box-sizing: border-box;
          }
        }
        div.line-type {
          height: 31px;
          padding: 0 10px;
          position: relative;
          div {
            width: 180px;
            margin-top: 17px;
            box-sizing: border-box;
          }
        }
      }
    }
    .uploadBtn-box {
      border: 1px solid #E7E7E7;
      border-radius: 2px;
      width: 100%;
      text-align: center;
      height: 30px;
      // margin-bottom: 20px;
      line-height: 30px;
      // cursor: pointer;
      span {
        cursor: pointer;
      }
    }
    .imgshow-box {
      border: 1px solid #E7E7E7;
      border-radius: 2px;
      margin-bottom: 10px;
      position: relative;
      .img-btn-box {
        text-align: center;
        height: 60px;
        border: 1px dashed #ddd;
        background: #EFF4FF;
        cursor: pointer;
        span {
          color: #1890FF;
          font-size: 12px;
        }
        img {
          width: 100%;
          height: 100%;
        }
      }
      .img-handle {
        padding: 0 10px;
        justify-content: space-between;
        height: 37px;
        >div:nth-child(2) {
          line-height: 37px;
          >i:nth-child(2) {
            padding-left: 5px;
            cursor: move;
          }
        }
        .el-checkbox {
          span {
            line-height: 37px;
          }
        }
      }
    }
    .url-box {
      margin-bottom: 10px;
      .el-input-group__prepend {
        padding: 0 3px;
      }
      .el-textarea {
        .el-textarea__inner {
          background: #FBFBFB;
        }
      }
    }
    .img-item {
      .addImg-btn {
        margin-bottom: 10px;
        .el-button {
          width: 100%;
        }
      }
      .img-grid-box {
        position: relative;
        cursor: pointer;
        margin-top: 10px;
        margin-bottom: 10px;
        .grid-item {
          position: relative;
          margin-bottom: 10px;
          img {
            width: 100%;
          }
          i {
            position: absolute;
            right: 3px;
            bottom: 3px;
            color:  #2E92FF;
          }
        }
      }
    }
    .staticmap-box {
      justify-content: space-between;
      .text {
        line-height: 38px;
      }
      .el-slider {
        width: 80%;
      }
    }
  }
  @import '../../../../static/css/defined-property.scss';
.slide-subform-enter-active, .slide-subform-leave-active {
  transition: all .3s linear;
}
.slide-subform-enter, .slide-subform-leave-to{
  transform:  translateX(300px);
  transition: all .2s linear;
}
}
.webform-line-box {
  height: 34px;
  cursor: pointer;
  line-height: 34px;
  position: relative;
  text-align: center;
  div {
    height: 1px;
    position: absolute;
    left: 0;
    width: 100%;
    top: 50%;
  }
  &:hover {
    background: #1890FF;
  }
}
</style>
