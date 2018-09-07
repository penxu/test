<template>
  <el-container class="custom-drag-right-wrip">
    <div class="field-setting" >
      <p>字段设置</p>
      <el-collapse v-model="activeNames" v-show="isShowFieldSetting">
        <!-- 字段名称 -->
        <el-collapse-item name="1">
          <template slot="title">
            <i class="el-icon-caret-right"></i>字段名称
          </template>
          <div class="input-box">
            <el-input v-model="myComponent.label" placeholder="请输入内容..."></el-input>
          </div>
          <div class="input-box" v-if="isShowPointOut">
            <label for="">提示框</label>
            <el-input v-model="myComponent.field.pointOut" placeholder="请输入"></el-input>
          </div>
          <div class="input-box" v-if="isShowDefault">
            <label for="">默认值</label>
            <el-input v-model="myComponent.field.defaultValue" placeholder="请输入内容..."></el-input>
          </div>
        </el-collapse-item>
        <!-- 单选、多选 -->
        <el-collapse-item name="2">
          <template slot="title">
            <i class="el-icon-caret-right"></i>类型
          </template>
          <div class="input-box">
            <el-select v-model="defaultChooseType" @change="modelChange('chooseType',$event)" placeholder="请选择">
              <el-option v-for="chooseType in chooseTypeList" :key="chooseType.name" :label="chooseType.label" :value="chooseType.value">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 下拉选项  -->
        <el-collapse-item name="3" v-if="myComponent.type == 'picklist'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>选项内容
          </template>
          <div class="option-box">
            <div class="option" v-for="(select,index) in myComponent.entrys" :key="index">
              <el-input v-model="select.label" @change="clearDefault($event, 'pick')"></el-input>
              <el-color-picker v-model="select.color"></el-color-picker>
              <i class="el-icon-close" @click="handleDelItem(myComponent.type,index)"></i>
            </div>
          </div>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" @click="handleAddItem(myComponent.type)">添加</el-button>
          </div>
          <div class="input-box">
            <label for="">默认值</label>
            <el-select v-model="defaultEntrysValue" placeholder="-无-" clearable value-key="label" @change="modelChange('picklistDefaultValue',$event)" v-if="myComponent.field.chooseType === '0'">
              <el-option v-for="(item,index) in selectDefault" :key="index" :label="item.label" :value="item">
              </el-option>
            </el-select>
            <el-select v-model="myComponent.field.defaultEntrys" placeholder="-无-" value-key="label" multiple v-if="myComponent.field.chooseType === '1'">
              <el-option v-for="(item,index) in selectDefault" :key="index" :label="item.label" :value="item">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 电话 -->
        <el-collapse-item name="4" v-if="myComponent.type === 'phone'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>类型
          </template>
          <div class="input-box">
            <el-select v-model="phoneModel" placeholder="电话" @change="modelChange('phoneTypeChange',$event)">
              <el-option v-for="phone in phoneType" :key="phone.name" :label="phone.label" :value="phone">
              </el-option>
            </el-select>
          </div>
          <div class="input-box">
            <label for="">位数</label>
            <el-select placeholder="-无-" v-model="phoneLengthVal" @change="modelChange('phoneLength', $event)" :disabled="myComponent.field.phoneType === '0'">
              <el-option v-for="phoneLenth in phoneLengths"
              :key="phoneLenth.name"
              :label="phoneLenth.label"
              :value="phoneLenth">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 数字类型 -->
        <el-collapse-item name="5" v-if="myComponent.type === 'number'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>类型
          </template>
          <div class="input-box">
            <el-select v-model="numberModel" @change="modelChange('numberTypeChange',$event)">
              <el-option v-for="(number,index) in numberTypes" :key="index" :label="number.label" :value="number">
              </el-option>
            </el-select>
          </div>
          <div class="input-box" v-show="myComponent.field.numberType !== '1'">
            <label for="">小数位数</label>
              <el-select placeholder="-无-" v-model="myComponent.field.numberLenth">
                <el-option v-for="numberLenth in numberDecimals" :key="numberLenth.name" :label="numberLenth" :value="numberLenth">
                </el-option>
              </el-select>
          </div>
          <div class="input-box">
            <label for="">默认值</label>
            <el-input type="number" v-model="myComponent.field.defaultValue" placeholder="请输入内容..."></el-input>
          </div>
        </el-collapse-item>
        <!-- 数字范围 -->
        <el-collapse-item name="6" v-if="myComponent.type === 'number'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>输入范围
          </template>
          <div class="input-range">
            <el-input type="number" v-model="myComponent.field.betweenMin" placeholder="不填代表无限制"></el-input>
            <span>~</span>
            <el-input type="number" v-model="myComponent.field.betweenMax" placeholder="不填代表无限制"></el-input>
          </div>
        </el-collapse-item>
        <!-- 时间日期类型 -->
        <el-collapse-item name="7" v-if="myComponent.type === 'datetime'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>类型
          </template>
          <div class="input-box">
            <el-select v-model="myComponent.field.formatType" placeholder="年-月-日">
              <el-option v-for="date in dateList" :key="date.name" :label="date" :value="date">
              </el-option>
            </el-select>
          </div>
          <div class="input-box">
            <label for="">默认值</label>
              <el-select v-model="dateTimeModel" placeholder="-无-" @change="modelChange('datetimeId',$event)">
                <el-option v-for="time in dateTimeOptions" :key="time.id" :label="time.label" :value="time">
                </el-option>
              </el-select>
          </div>
          <div class="input-box" v-show="myComponent.field.defaultValueId === '2'">
            <el-date-picker v-model="myComponent.field.defaultValue" type="datetime" :format="myComponent.field.formatType"
              value-format="timestamp" placeholder="选择日期时间" @change="modelChange('datetimeVal',$event)">
            </el-date-picker>
          </div>
        </el-collapse-item>
        <!-- 附件 -->
        <el-collapse-item  name="8" v-if="myComponent.type === 'attachment'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>数量控制
          </template>
          <el-radio-group  size="mini" v-model="myComponent.field.countLimit">
            <el-radio label= '0'>不限制</el-radio>
            <el-radio label= '1'>限制</el-radio>
          </el-radio-group>
          <div class="line-box" v-show="myComponent.field.countLimit ==='1'">
            只允许上传<el-input type="number" placeholder="最大10" v-model="myComponent.field.maxCount"></el-input>个附件
          </div>
          <div class="line-box" v-show="myComponent.field.countLimit ==='1'">
            每个附件限制<el-input type="number" placeholder="最大100" v-model="myComponent.field.maxSize"></el-input>M
          </div>
        </el-collapse-item>
        <!-- 图片尺寸 -->
        <el-collapse-item  name="9" v-if="myComponent.type === 'picture'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>尺寸要求
          </template>
          <div class="input-box">
            <el-select  v-model="myComponent.field.imageSize" placeholder="">
              <el-option v-for="imageSize in imageSizeList"
                :key="imageSize.name"
                :label="imageSize"
                :value="imageSize">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 图片大小限制 -->
        <el-collapse-item  name="10" v-if="myComponent.type === 'picture'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>数量控制
          </template>
          <el-radio-group  size="mini" v-model="myComponent.field.countLimit">
            <el-radio label= '0'>不限制</el-radio>
            <el-radio label= '1'>限制</el-radio>
          </el-radio-group>
          <div class="line-box" v-show="myComponent.field.countLimit ==='1'">
            只允许上传<el-input type="number" placeholder="最大10" v-model="myComponent.field.maxCount"></el-input>张
          </div>
          <div class="line-box" v-show="myComponent.field.countLimit ==='1'">
            每个附件限制<el-input type="number" placeholder="最大100" v-model="myComponent.field.maxSize"></el-input>M
          </div>
        </el-collapse-item>
        <!-- 复选框  -->
        <el-collapse-item name="11" v-if="myComponent.type == 'multi'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>选项内容
          </template>
          <div class="option-box">
            <div class="option" v-for="(select,index) in myComponent.entrys" :key="index">
              <el-input v-model="select.label"></el-input>
              <i class="el-icon-close" @click="handleDelItem(myComponent.type,index)"></i>
            </div>
          </div>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" @click="handleAddItem(myComponent.type)">添加</el-button>
          </div>
          <div class="input-box">
            <label for="">默认值</label>
            <el-select v-model="defaultEntrysValue" placeholder="-无-" clearable value-key="label" @change="modelChange('multiDefaultValue',$event)" v-if="myComponent.field.chooseType === '0'">
              <el-option v-for="(item,index) in selectDefault" :key="index" :label="item.label" :value="item">
              </el-option>
            </el-select>
            <el-select v-model="myComponent.field.defaultEntrys" placeholder="-无-" value-key="label" multiple v-if="myComponent.field.chooseType === '1'">
              <el-option v-for="(item,index) in selectDefault" :key="index" :label="item.label" :value="item">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 多级下拉 -->
        <el-collapse-item name="12" v-if="myComponent.type === 'mutlipicklist'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>类型
          </template>
          <el-radio-group size="mini" v-model="myComponent.field.selectType">
            <el-radio-button label="0">二级下拉</el-radio-button>
            <el-radio-button label="1">三级下拉</el-radio-button>
          </el-radio-group>
          <el-tabs type="border-card" v-model="mutlipicklistActive">
            <el-tab-pane label="一级" name="mutlipicklist1" class="option-box">
              <div class="option" v-for="(item1,index) in myComponent.entrys" :key="index">
                <el-input v-model="item1.label" @change="clearDefault($event, 'mulPick')"></el-input>
                <el-color-picker v-model="item1.color"></el-color-picker>
                <i class="el-icon-close" @click="handleDelItem(myComponent.type,index)"></i>
              </div>
            </el-tab-pane>
            <el-tab-pane label="二级" name="mutlipicklist2" class="option-box">
              <div class="input-box">
                <label for="">一级选项</label>
                <el-select placeholder="-无-" v-model="multiSelect1_model" @change="modelChange('multiSelectOne',$event)">
                  <el-option v-for="item1 in myComponent.entrys" :key="item1.value" :label="item1.label" :value="item1">
                  </el-option>
                </el-select>
              </div>
              <div class="option" v-for="(item2,index2) in multiSelectTwoList" :key="index2">
                <el-input v-model="item2.label"  @change="clearDefault($event, 'mulPick')"></el-input>
                <el-color-picker v-model="item2.color"></el-color-picker>
                <i class="el-icon-close" @click="handleDelItem(myComponent.type + '2',index2)"></i>
              </div>
            </el-tab-pane>
            <el-tab-pane label="三级" name="mutlipicklist3" class="option-box" v-if="myComponent.field.selectType === '1'">
              <div class="input-box">
                <label for="">一级选项</label>
                <el-select placeholder="-无-" v-model="multiSelect1_model" @change="modelChange('multiSelectOne',$event)">
                  <el-option v-for="item1 in myComponent.entrys" :key="item1.value" :label="item1.label" :value="item1">
                  </el-option>
                </el-select>
              </div>
              <div class="input-box">
                <label for="">二级选项</label>
                <el-select placeholder="-无-" v-model="multiSelect2_model" @change="modelChange('multiSelectTwo',$event)">
                  <el-option v-for="item2 in multiSelectTwoList" :key="item2.value" :label="item2.label" :value="item2">
                  </el-option>
                </el-select>
              </div>
              <div class="option" v-for="(item3,index3) in multiSelectThreeList" :key="index3">
                <el-input v-model="item3.label"  @change="clearDefault($event, 'mulPick')"></el-input>
                <el-color-picker v-model="item3.color"></el-color-picker>
                <i class="el-icon-close" @click="handleDelItem(myComponent.type + '3',index3)"></i>
              </div>
            </el-tab-pane>
          </el-tabs>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" @click="handleAddItem('multipicklist')">添加选项</el-button>
          </div>
          <div class="input-box" v-show="mutlipicklistActive === 'mutlipicklist1'">
            <label for="">一级默认值</label>
            <el-select v-model="myComponent.defaultEntrys.oneDefaultValue" placeholder="-无-" @change="modelChange('multiDefaultValue1',$event)">
              <el-option v-for="item1 in selectDefault" :key="item1.name" :label="item1.label" :value="item1">
              </el-option>
            </el-select>
          </div>
          <div class="input-box" v-show="mutlipicklistActive === 'mutlipicklist2'">
            <label for="">二级默认值</label>
            <el-select placeholder="-无-" v-model="myComponent.defaultEntrys.twoDefaultValue" @change="modelChange('multiDefaultValue2',$event)">
              <el-option v-for="item2 in multiSelectTwoListDefault" :key="item2.name" :label="item2.label" :value="item2">
              </el-option>
            </el-select>
          </div>
          <div class="input-box" v-show="mutlipicklistActive === 'mutlipicklist3'">
            <label for="">三级默认值</label>
            <el-select placeholder="-无-" v-model="myComponent.defaultEntrys.threeDefaultValue" @change="modelChange('multiDefaultValue3',$event)">
              <el-option v-for="item3 in multiSelectThreeListDefault" :key="item3.name" :label="item3.label" :value="item3">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 人员 -->
        <el-collapse-item name="13" v-if="myComponent.type === 'personnel'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>默认值
          </template>
          <div class="input-box">
            <el-input v-model="defaultMemberValue" placeholder="请选择" @focus="selectMeber(myComponent.field.chooseType,'defined')" readonly></el-input>
          </div>
          <div class="input-box">
            <label for="">可选范围</label>
            <el-input v-model="memberChooseRange" placeholder="请选择" @focus="selectMeber('1','chooseRange')" readonly></el-input>
          </div>
        </el-collapse-item>
        <!-- 公式 -->
        <el-collapse-item name="14" v-if="isFormula">
          <template slot="title">
            <i class="el-icon-caret-right"></i>数据类型
          </template>
          <div class="formula-text">
            ={{myComponent.field.formulaCh}}
          </div>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" @click="showformulaSetting(myComponent.type)">设置公式</el-button>
          </div>
          <div class="input-box">
            <label for="">返回类型</label>
            <el-select placeholder="请选择类型" v-model="formulaReturnVal" @change="modelChange('returnTypeChange',$event)">
              <el-option v-for="item in formulaReturn" :key="item.name" :label="item.label" :value="item">
              </el-option>
            </el-select>
          </div>
          <div class="input-box" v-show="myComponent.field.numberType === '0' || myComponent.field.numberType === '2'">
            <label for="">小数位数</label>
            <el-select placeholder="-无-" v-model="myComponent.field.decimalLen">
              <el-option v-for="decimalLen in numberDecimals" :key="decimalLen.name" :label="decimalLen" :value="decimalLen">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 公式其他设置 -->
        <el-collapse-item name="15" v-if="isFormula">
          <template slot="title">
            <i class="el-icon-caret-right"></i>其他设置
          </template>
          <div class="checkbox-box">
            <el-checkbox v-model="myComponent.field.formulaCalculates" true-label='1' false-label='0'>新公式是否对旧数据重新计算</el-checkbox>
          </div>
        </el-collapse-item>
        <!-- 自动编号 -->
        <el-collapse-item name="16" v-if="myComponent.type === 'identifier'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>编号结果
          </template>
          <div class="formula-text">
            ={{numberingRes}}
          </div>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" @click="openIdentifier">设置编号规则</el-button>
          </div>
        </el-collapse-item>
        <!-- 关联关系 -->
        <el-collapse-item name="17" v-if="myComponent.type === 'reference'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>关联模块
          </template>
          <div class="input-box">
            <el-select v-model="myComponent.relevanceModule.moduleLabel" @change="modelChange('referenceModule',$event)">
              <el-option v-for="list in moduleList" :key="list.id" :label="list.chinese_name" :value="list">
              </el-option>
            </el-select>
          </div>
          <div class="input-box">
            <label for="">关联字段</label>
            <el-select v-model="myComponent.relevanceField.fieldLabel" @change="modelChange('referenceField',$event)">
              <el-option v-for="field in fieldList" :key="field.value" :label="field.label" :value="field">
              </el-option>
            </el-select>
          </div>
          <div class="refe-box">
            <label for="">列表搜索字段</label>
            <el-select v-model="myComponent.searchFields" value-key="fieldName" multiple>
              <el-option v-for="field in searchFieldList" :key="field.fieldName" :label="field.fieldLabel" :value="field">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 关联关系筛选条件 -->
        <el-collapse-item name="18" v-if="myComponent.type === 'reference'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>设置关联筛选条件
          </template>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" @click="editFilterCondition">添加筛选条件</el-button>
          </div>
          <div class="condition-text">
              <div class="item" v-for="(item,index) in myComponent.relevanceWhere" :key="index">
                {{index + 1}}. {{item.field_label}} <span>{{item.operator_label}}</span> {{item.result_value | toText(item.show_type)}}
              </div>
            </div>
        </el-collapse-item>
        <!-- 子表单 -->
        <el-collapse-item name="19" v-if="myComponent.type === 'subform'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>子表单字段
          </template>
          <draggable class="subform-box" :options="dragOption" v-model="myComponent.componentList">
            <div class="item" v-for="(dragItem,index) in myComponent.componentList" :key="index" @click="toSubformSetting(dragItem)">
              <span class="name">{{dragItem.label}}</span>
              <span class="bg"></span>
              <i class="el-icon-close" @click.stop="delSubformItem(index)"></i>
            </div>
          </draggable>
          <div class="input-box">
            <el-select placeholder="选择字段添加到子表单" v-model="subformModel" @change="addSubformItem($event)">
              <el-option v-for="component in componentList" :key="component.name" :label="component.label" :value="component" >
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 地址位置 -->
        <el-collapse-item name="20" v-if="myComponent.type === 'location'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>地址位置
          </template>
          <div class="checkbox-box">
            <el-checkbox true-label='1' false-label='0' v-model="myComponent.field.defaultValue">默认当前位置</el-checkbox>
          </div>
        </el-collapse-item>
        <!-- 字段宽度 -->
        <el-collapse-item name="21" v-if="myComponent.type !== 'subform'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>字段宽度
          </template>
          <el-radio-group size="mini" v-model="myComponent.width">
            <el-radio-button label="100%">显示单列</el-radio-button>
            <el-radio-button label="50%">显示双列</el-radio-button>
          </el-radio-group>
        </el-collapse-item>
        <!-- 字段结构 -->
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
        <el-collapse-item name="23" v-if="isShowfieldControl">
          <template slot="title">
            <i class="el-icon-caret-right"></i>字段控制
          </template>
          <el-radio-group size="mini" v-model="myComponent.field.fieldControl" :disabled="myComponent.name === 'personnel_principal'">
            <el-radio label='1'>只读</el-radio>
            <el-radio label='2'>必填</el-radio>
            <el-radio label='0'>不控制</el-radio>
          </el-radio-group>
        </el-collapse-item>
        <!-- 字段权限 -->
        <el-collapse-item name="24" v-if="isShowfieldAuth">
          <template slot="title">
            <i class="el-icon-caret-right"></i>字段权限
          </template>
          <div class="checkbox-box">
            <el-checkbox true-label='1' false-label='0' v-model="myComponent.field.addView" :disabled="myComponent.name === 'personnel_principal'">新增时显示</el-checkbox>
            <el-checkbox true-label='1' false-label='0' v-model="myComponent.field.editView" :disabled="myComponent.name === 'personnel_principal'">编辑时显示</el-checkbox>
          </div>
        </el-collapse-item>
        <!-- 查重 -->
        <el-collapse-item name="25" v-if="myComponent.field.repeatCheck">
          <template slot="title">
            <i class="el-icon-caret-right"></i>查重
          </template>
          <el-radio-group size="mini" v-model="myComponent.field.repeatCheck">
            <el-radio label='0'>不启用查重</el-radio>
            <el-radio label='1'>启用查重，允许保存数据</el-radio>
            <el-radio label='2'>启用查重，不允许保存数据</el-radio>
          </el-radio-group>
        </el-collapse-item>
        <!-- 终端 -->
        <!-- <el-collapse-item name="26">
          <template slot="title">
            <i class="el-icon-caret-right"></i>终端
          </template>
          <div class="checkbox-box">
            <el-checkbox v-model="myComponent.field.terminalPc" true-label='1' false-label='0' :disabled="myComponent.name === 'personnel_principal'">PC</el-checkbox>
            <el-checkbox v-model="myComponent.field.terminalApp" true-label='1' false-label='0' :disabled="myComponent.name === 'personnel_principal'">APP</el-checkbox>
          </div>
        </el-collapse-item>  -->
        <!-- 其他设置 -->
        <el-collapse-item name="26">
          <template slot="title">
            <i class="el-icon-caret-right"></i>其他设置
          </template>
          <div class="checkbox-box">
            <el-checkbox v-model="myComponent.field.isShowCard" true-label='1' false-label='0' :disabled="myComponent.name === 'personnel_principal'">是否显示在任务卡片上</el-checkbox>
            <el-checkbox v-model="myComponent.field.isFillReason" true-label='1' false-label='0' :disabled="myComponent.name === 'personnel_principal'" v-show="!ortherSetting">任务重新激活填写激活原因</el-checkbox>
            <el-checkbox v-model="myComponent.field.isFillReason" true-label='1' false-label='0' :disabled="myComponent.name === 'personnel_principal'" v-show="ortherSetting">更改时间填写修改原因</el-checkbox>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
    <!-- <transition name="slide">
      <defined-subform v-if="subformVisable" class="subform-dialog" :subformComponent="subformComponent" :components="myComponent.componentList"></defined-subform>
    </transition> -->
  </el-container>
</template>
<script>
// import DefinedSubform from '@/backend/custom_manager/custom_drag/defined-subform'
import draggable from 'vuedraggable'
import {subformList} from '@/common/js/constant.js'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'TaskDragRight',
  components: {
    draggable
    // DefinedSubform
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
        entrys: [
          {
            subList: [
              {
                subList: []
              }
            ]
          }
        ]
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
      chooseTypeList: [{ value: '0', label: '单选' }, { value: '1', label: '多选' }], // 复选框组件类型
      formulaReturn: [
        { value: '0', label: '数字' },
        { value: '1', label: '整数' },
        { value: '2', label: '百分比' },
        { value: '3', label: '文本' },
        { value: '4', label: '日期时间' }
      ],
      defaultEntrysValue: '',
      mutlipicklistActive: 'mutlipicklist1',
      multiSelect1_model: '',
      multiSelect2_model: '',
      multiSelectTwoList: [],
      multiSelectThreeList: [],
      multiSelectTwoListDefault: [],
      multiSelectThreeListDefault: [],
      moduleList: [],
      fieldList: [],
      phoneType: [{label: '电话', value: '0'}, {label: '手机', value: '1'}],
      phoneLengths: [{ value: '0', label: '不限' }, {value: '1', label: '11'}],
      numberTypes: [ // 数字类型
        {value: '0', label: '数字'},
        {value: '1', label: '整数'},
        {value: '2', label: '百分比'}
      ],
      numberDecimals: ['1', '2', '3', '4'], // 数字小数位数
      subformVisable: false, // 子表单设置弹出控制
      componentList: subformList, // 子表单选择列表
      formulaItems: [],
      subformModel: '',
      referceModel: '',
      subformComponent: {},
      isShowFieldSetting: false
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
  },
  methods: {
    // 更新模块信息
    updateMsg (modules) {
      this.$bus.emit('sendModule', modules)
    },
    // 获取屏幕高度
    getHeight () {
      this.client_height = document.documentElement.clientHeight - 160 + 'px'
      console.log(this.client_height)
    },
    // 打开图标库
    openIconLibrary () {
      this.$bus.emit('openIconLibrary', 'icon')
    },
    // 简单公式弹出框
    showformulaSetting (type) {
      let data = {
        isOpen: true,
        type: type,
        name: this.myComponent.label,
        textarea: this.myComponent.field.formulaCh,
        fieldList: this.formulaItems
      }
      this.$bus.emit('openFormula', data)
    },
    // 删除列表搜索字段
    delSearchList (index) {
      this.myComponent.searchFields.splice(index, 1)
    },
    // 设置高级筛选条件
    editFilterCondition () {
      let bean = this.myComponent.relevanceModule.moduleName
      let value = {
        bean: bean,
        conditions: this.myComponent.relevanceWhere,
        highWhere: this.myComponent.seniorWhere
      }
      if (bean) {
        this.$bus.emit('openHighCondition', value)
      }
    },
    // 打开自动编号
    openIdentifier () {
      let value = {
        isOpen: true,
        fixedValue: this.myComponent.numbering.fixedValue,
        dateValue: this.myComponent.numbering.dateValue,
        serialValue: this.myComponent.numbering.serialValue
      }
      this.$bus.emit('openIdentifer', value)
    },
    // 选择人员
    selectMeber (type, key) {
      let types = type === '0' ? 1 : ''
      let navKey = type === '0' ? '' : '0,1'
      let list = key === 'defined' ? this.myComponent.field.defaultPersonnel : this.myComponent.field.chooseRange
      // 给父组件传值
      this.$bus.emit('commonMember',
        { 'type': types,
          'prepareData': list,
          'prepareKey': key,
          'seleteForm': true,
          'banData': [],
          'navKey': navKey,
          'removeData': [],
          'index': 0
        })
    },
    // model 发生变化
    modelChange (property, data, listIndex) {
      switch (property) {
        case 'applicationName':
          this.myModule.applicationId = data.id
          this.myModule.applicationName = data.name
          console.log(this.myModule, '修改后')
          break
        case 'datetimeVal':
          this.myComponent.field.defaultValue = String(data)
          console.log(this.myComponent, '修改后')
          break
        case 'datetimeId':
          if (data.value === '2') {
            this.myComponent.field.defaultValueId = data.value
            this.myComponent.field.defaultValue = String(new Date().getTime())
          } else {
            this.myComponent.field.defaultValueId = data.value
            this.myComponent.field.defaultValue = ''
          }
          console.log(this.myComponent, '修改后')
          break
        case 'chooseType':
          this.myComponent.field.chooseType = data
          if (this.myComponent.field.defaultPersonnel) {
            this.myComponent.field.defaultPersonnel = []
          }
          console.log(this.myComponent, '修改后')
          break
        case 'picklistDefaultValue':
          this.myComponent.field.defaultEntrys = [data]
          this.defaultEntrysValue = data.label
          console.log(this.myComponent, '修改后')
          break
        case 'multiDefaultValue':
          this.myComponent.field.defaultEntrys = [data]
          this.defaultEntrysValue = data.label
          console.log(this.myComponent, '修改后')
          break
        case 'referenceModule':
          this.myComponent.relevanceModule.moduleLabel = data.chinese_name
          this.myComponent.relevanceModule.moduleName = data.english_name
          this.myComponent.relevanceField.fieldLabel = ''
          this.myComponent.searchFields = []
          console.log(this.myComponent, '修改后')
          let bean = {bean: data.english_name}
          this.ajaxGetFieldList(bean)
          break
        case 'referenceField':
          this.myComponent.relevanceField.fieldLabel = data.label
          this.myComponent.relevanceField.fieldName = data.value
          console.log(this.myComponent, '修改后')
          break
        case 'phoneTypeChange':
          this.myComponent.field.phoneType = data.value
          if (data.value === '0') {
            this.myComponent.field.phoneLenth = '0'
          }
          console.log(this.myComponent, '修改后')
          break
        case 'phoneLength':
          this.myComponent.field.phoneLenth = data.value
          console.log(this.myComponent.field, '修改后')
          break
        case 'numberTypeChange':
          this.myComponent.field.numberType = data.value
          console.log(this.myComponent, '修改后')
          break
        case 'returnTypeChange':
          this.myComponent.field.numberType = data.value
          console.log(this.myComponent, '修改后')
          break
        case 'multiSelectOne':
          this.multiSelectTwoList = data.subList
          console.log(this.myComponent, '修改后')
          break
        case 'multiSelectTwo':
          this.multiSelectThreeList = data.subList
          console.log(this.myComponent, '修改后')
          break
        case 'multiDefaultValue1':
          this.multiSelectTwoListDefault = data.subList
          this.multiSelectTwoListDefault.push({
            label: '-无-',
            value: '-1',
            color: '',
            subList: []
          })
          if (data.value !== '-1') {
            this.myComponent.defaultEntrys.oneDefaultValue = data.label
            this.myComponent.defaultEntrys.oneDefaultValueId = data.value
            this.myComponent.defaultEntrys.oneDefaultValueColor = data.color
          } else {
            this.myComponent.defaultEntrys.oneDefaultValue = ''
            this.myComponent.defaultEntrys.oneDefaultValueId = ''
            this.myComponent.defaultEntrys.oneDefaultValueColor = ''
          }
          this.myComponent.defaultEntrys.twoDefaultValue = ''
          this.myComponent.defaultEntrys.twoDefaultValueId = ''
          this.myComponent.defaultEntrys.twoDefaultValueColor = ''
          this.myComponent.defaultEntrys.threeDefaultValue = ''
          this.myComponent.defaultEntrys.threeDefaultValueId = ''
          this.myComponent.defaultEntrys.threeDefaultValueColor = ''
          console.log(this.myComponent.defaultEntrys, '修改后')
          break
        case 'multiDefaultValue2':
          this.multiSelectThreeListDefault = data.subList
          this.multiSelectThreeListDefault.push({
            label: '-无-',
            value: '-1',
            color: '',
            subList: []
          })
          if (data.value !== '-1') {
            this.myComponent.defaultEntrys.twoDefaultValue = data.label
            this.myComponent.defaultEntrys.twoDefaultValueId = data.value
            this.myComponent.defaultEntrys.twoDefaultValueColor = data.color
          } else {
            this.myComponent.defaultEntrys.twoDefaultValue = ''
            this.myComponent.defaultEntrys.twoDefaultValueId = ''
            this.myComponent.defaultEntrys.twoDefaultValueColor = ''
          }
          this.myComponent.defaultEntrys.threeDefaultValue = ''
          this.myComponent.defaultEntrys.threeDefaultValueId = ''
          this.myComponent.defaultEntrys.threeDefaultValueColor = ''
          console.log(this.myComponent.defaultEntrys, '修改后')
          break
        case 'multiDefaultValue3':
          if (data.value !== '-1') {
            this.myComponent.defaultEntrys.threeDefaultValue = data.label
            this.myComponent.defaultEntrys.threeDefaultValueId = data.value
            this.myComponent.defaultEntrys.threeDefaultValueColor = data.color
          } else {
            this.myComponent.defaultEntrys.threeDefaultValue = ''
            this.myComponent.defaultEntrys.threeDefaultValueId = ''
            this.myComponent.defaultEntrys.threeDefaultValueColor = ''
          }
          console.log(this.myComponent.defaultEntrys, '修改后')
          break
        default :
          break
      }
    },
    // 下拉菜单改变时清空默认值
    clearDefault (event, type) {
      if (type === 'pick') {
        this.myComponent.field.defaultValue = ''
        this.myComponent.field.defaultValueColor = ''
        this.myComponent.field.defaultValueId = ''
      } else {
        this.myComponent.defaultEntrys.oneDefaultValue = ''
        this.myComponent.defaultEntrys.oneDefaultValueColor = ''
        this.myComponent.defaultEntrys.oneDefaultValueId = ''
        this.myComponent.defaultEntrys.threeDefaultValue = ''
        this.myComponent.defaultEntrys.threeDefaultValueColor = ''
        this.myComponent.defaultEntrys.threeDefaultValueId = ''
        this.myComponent.defaultEntrys.twoDefaultValue = ''
        this.myComponent.defaultEntrys.twoDefaultValueColor = ''
        this.myComponent.defaultEntrys.twoDefaultValueId = ''
      }
    },
    // 添加选项
    handleAddItem (property) {
      switch (property) {
        case 'multi':
          let len = this.myComponent.entrys.length
          if (len === 0) {
            this.myComponent.entrys.push({
              label: '新选项',
              value: '0'
            })
            return
          }
          let lastItemValue = parseInt(this.myComponent.entrys[len - 1].value)
          let item = {
            label: '新选项',
            value: String(lastItemValue + 1)
          }
          this.myComponent.entrys.push(item)
          console.log(this.myComponent, '修改后')
          break
        case 'multipicklist':
          if (this.mutlipicklistActive === 'mutlipicklist1') {
            // 一级下拉添加选项
            let len1 = this.myComponent.entrys.length
            if (len1 === 0) {
              this.myComponent.entrys.push({
                label: '一级新选项',
                value: '0',
                color: '#FFFFFF',
                subList: []
              })
            } else {
              let lastItemValue1 = parseInt(this.myComponent.entrys[len1 - 1].value)
              let item1 = {
                label: '一级新选项',
                value: String(lastItemValue1 + 1),
                color: '#FFFFFF',
                subList: []
              }
              this.myComponent.entrys.push(item1)
            }
          } else if (this.mutlipicklistActive === 'mutlipicklist2') {
            // 二级下拉添加选项
            let len2 = this.multiSelectTwoList.length
            if (len2 === 0) {
              this.multiSelectTwoList.push({
                label: '二级新选项',
                value: '0',
                color: '#FFFFFF',
                subList: []
              })
            } else {
              let lastItemValue2 = parseInt(this.multiSelectTwoList[len2 - 1].value)
              let item2 = {
                label: '二级新选项',
                value: String(lastItemValue2 + 1),
                color: '#FFFFFF',
                subList: []
              }
              this.multiSelectTwoList.push(item2)
            }
          } else if (this.mutlipicklistActive === 'mutlipicklist3') {
            // 三级下拉添加选项
            let len3 = this.multiSelectThreeList.length
            if (len3 === 0) {
              this.multiSelectThreeList.push({
                label: '三级新选项',
                value: '0',
                color: '#FFFFFF'
              })
            } else {
              let lastItemValue3 = parseInt(this.multiSelectThreeList[len3 - 1].value)
              let item3 = {
                label: '三级新选项',
                value: String(lastItemValue3 + 1),
                color: '#FFFFFF'
              }
              this.multiSelectThreeList.push(item3)
            }
          }
          break
        case 'picklist':
          let lens = this.myComponent.entrys.length
          if (lens === 0) {
            this.myComponent.entrys.push({
              label: '新选项',
              value: '0',
              color: '#FFFFFF'
            })
            return
          }
          let lastItemValues = parseInt(this.myComponent.entrys[lens - 1].value)
          let items = {
            label: '新选项',
            value: String(lastItemValues + 1),
            color: '#FFFFFF'
          }
          this.myComponent.entrys.push(items)
          break
        default:
          break
      }
    },
    // 删除选项
    handleDelItem (property, index) {
      switch (property) {
        case 'multi': case 'picklist': case 'mutlipicklist':
          this.myComponent.entrys.splice(index, 1)
          break
        case 'mutlipicklist2':
          this.multiSelectTwoList.splice(index, 1)
          break
        case 'mutlipicklist3':
          this.multiSelectThreeList.splice(index, 1)
          break
        default:
          break
      }
    },
    // 添加子表单选项
    addSubformItem (data) {
      let timestampName = new Date().getTime()
      let property = JSON.parse(JSON.stringify(data))
      property.name += timestampName
      this.subformModel = ''
      this.myComponent.componentList.push(property)
    },
    // 删除子表单选项
    delSubformItem (index) {
      this.myComponent.componentList.splice(index, 1)
    },
    // 子表单设置是否显示
    toSubformSetting (property) {
      this.subformVisable = true
      this.subformComponent = property
    },
    // 获取关联模块列表
    ajaxGetModuleList () {
      HTTPServer.getAppAllModuleList(null, 'Loading').then((res) => {
        if (res) {
          this.moduleList = res
        }
      })
    },
    // 获取关联字段列表
    ajaxGetFieldList (data) {
      HTTPServer.getModuleFieldList(data, 'Loading').then((res) => {
        if (JSON.stringify(res) !== '{}') {
          this.fieldList = res
        }
      })
    },
    // 获取应用列表
    ajaxGetApplication (data) {
      HTTPServer.getApplicationList(data, 'Loading').then((res) => {
        this.myApplication = res
      })
    }
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
        console.log(singlevalue, '传递')
        this.myComponent = singlevalue
        this.isShowFieldSetting = true
        if (singlevalue.type === 'reference') {
          this.ajaxGetModuleList()
          if (singlevalue.relevanceModule.moduleName) {
            let bean = {bean: singlevalue.relevanceModule.moduleName}
            this.ajaxGetFieldList(bean)
          }
        } else if (singlevalue.type === 'formula' || singlevalue.type === 'functionformula' || singlevalue.type === 'seniorformula') {
        // 处理公式列表
          console.log(allValue, '公式传递')
          this.formulaItems = []
          if (allValue) {
            allValue.map((item, index) => {
              let subform = item.componentList ? item.componentList : []
              this.formulaItems.push(
                {label: item.label,
                  value: item.name,
                  type: item.type,
                  subform: subform
                })
            })
          }
        } else if (singlevalue.type === 'tag') {
          this.isShowFieldSetting = false
        }
      }
    })
    // 设置模块名称
    this.$bus.on('sendModuleName', value => {
      this.settingsType = 0
      this.myModule.title = value
      this.$bus.emit('sendModule', this.myModule)
    })
    // 设置分栏
    this.$bus.on('sendColumn', value => {
      this.myColumn = value
      this.settingsType = 1
    })
    // 获取人员单选数据
    this.$bus.on('selectMemberRadio', (value) => {
      if (value.prepareKey === 'defined') {
        this.myComponent.field.defaultPersonnel = value.prepareData
      }
    })
    // 获取人员多选数据
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value.prepareKey === 'defined') {
        this.myComponent.field.defaultPersonnel = value.prepareData
      } else if (value.prepareKey === 'chooseRange') {
        this.myComponent.field.chooseRange = value.prepareData
      }
    })
    // 关闭子表单
    this.$bus.on('send_subformVisbale', (value) => {
      this.subformVisable = value
    })
    // 获取公式
    this.$bus.on('setFormula', (value) => {
      this.myComponent.field.formulaEn = value.en
      this.myComponent.field.formulaCh = value.ch
      this.myComponent.field.belongBean = value.belongBean
    })
    // 获取自动编号规则
    this.$bus.on('setIdentifer', (value) => {
      this.myComponent.numbering = value
    })
    // 获取高级条件
    this.$bus.on('setHighCondition', (value) => {
      this.myComponent.relevanceWhere = value.relevanceWhere
      this.myComponent.seniorWhere = value.seniorWhere
    })
    // 设置模块图标
    this.$bus.on('setModuleIcon', (value) => {
      this.myModule.icon = value
    })
  },
  computed: {
    // 关联关系搜索字段
    searchFieldList () {
      let list = []
      this.fieldList.map((item) => {
        list.push({fieldLabel: item.label, fieldName: item.value})
      })
      return list
    },
    // 子表单拖拽属性
    dragOption () {
      return {
        animation: 100,
        group: { name: 'property', pull: true, put: true },
        sort: true,
        ghostClass: 'ghost'
      }
    },
    // 编号结果
    numberingRes () {
      return (
        this.myComponent.numbering.fixedValue +
        this.myComponent.numbering.dateValue +
        this.myComponent.numbering.serialValue
      )
    },
    // 默认选择类型
    defaultChooseType: {
      get: function () {
        let label = this.myComponent.field.chooseType === '0' ? '单选' : '多选'
        return label
      },
      set: function (newValue) {
      }
    },
    // 人员默认值
    defaultMemberValue () {
      let member = []
      this.myComponent.field.defaultPersonnel.map((item, index) => {
        member.push(item.name)
      })
      return member.toString()
    },
    // 控制默认值是否展示
    isShowDefault () {
      let isShow
      if (
        this.myComponent.type === 'picture' ||
        this.myComponent.type === 'datetime' ||
        this.myComponent.type === 'multi' ||
        this.myComponent.type === 'formula' ||
        this.myComponent.type === 'reference' ||
        this.myComponent.type === 'location' ||
        this.myComponent.type === 'personnel' ||
        this.myComponent.type === 'email' ||
        this.myComponent.type === 'number' ||
        this.myComponent.type === 'attachment' ||
        this.myComponent.type === 'phone' ||
        this.myComponent.type === 'area' ||
        this.myComponent.type === 'mutlipicklist' ||
        this.myComponent.type === 'subform' ||
        this.myComponent.type === 'functionformula' ||
        this.myComponent.type === 'seniorformula' ||
        this.myComponent.type === 'identifier' ||
        this.myComponent.type === 'picklist'
      ) {
        isShow = false
      } else {
        isShow = true
      }
      return isShow
    },
    // 控制提示框是否展示
    isShowPointOut () {
      let isShow
      if (
        this.myComponent.type === 'text' ||
        this.myComponent.type === 'textarea' ||
        this.myComponent.type === 'phone' ||
        this.myComponent.type === 'email' ||
        this.myComponent.type === 'location' ||
        this.myComponent.type === 'reference' ||
        this.myComponent.type === 'number'
      ) {
        isShow = true
      } else {
        isShow = false
      }
      return isShow
    },
    // 是否显示选择类型
    isShowChooseType () {
      let isShow
      switch (this.myComponent.type) {
        case 'picklist': case 'multi': case 'personnel':
          isShow = true
          break
        default:
          isShow = false
          break
      }
      return isShow && this.myComponent.name !== 'personnel_principal'
    },
    // 是否显示字段控制
    isShowfieldControl () {
      let isShow
      switch (this.myComponent.type) {
        case 'identifier': case 'subform': case 'formula': case 'functionformula': case 'seniorformula':
          isShow = false
          break
        default:
          isShow = true
          break
      }
      return isShow
    },
    // 是否显示字段权限
    isShowfieldAuth () {
      let isShow
      switch (this.myComponent.type) {
        case 'identifier': case 'formula': case 'functionformula': case 'seniorformula':
          isShow = false
          break
        default:
          isShow = true
          break
      }
      return isShow
    },
    // 下拉选项默认值
    selectDefault () {
      let list = JSON.parse(JSON.stringify(this.myComponent.entrys))
      return list
    },
    // 公式
    isFormula () {
      let isShow
      switch (this.myComponent.type) {
        case 'formula': case 'functionformula': case 'seniorformula':
          isShow = true
          break
        default:
          isShow = false
          break
      }
      return isShow
    },
    // 电话类型默认值
    phoneModel: {
      get: function () {
        let text
        if (this.myComponent.field.phoneType === '0') {
          text = '电话'
        } else if (this.myComponent.field.phoneType === '1') {
          text = '手机'
        }
        return text
      },
      set: function (newValue) {
      }
    },
    // 电话位数
    phoneLengthVal: {
      get: function () {
        let text
        if (this.myComponent.field.phoneLenth === '0') {
          text = '不限'
        } else if (this.myComponent.field.phoneLenth === '1') {
          text = '11'
        }
        return text
      },
      set: function (newValue) {
      }
    },
    // 时间默认值
    dateTimeModel: {
      get: function () {
        let text
        if (this.myComponent.field.defaultValueId === '0') {
          text = '-无-'
        } else if (this.myComponent.field.defaultValueId === '1') {
          text = '当前时间'
        } else {
          text = '指定时间'
        }
        return text
      },
      set: function (newValue) {
      }
    },
    // 数字类型
    numberModel: {
      get: function () {
        let text
        if (this.myComponent.field.numberType === '0') {
          text = '数字'
        } else if (this.myComponent.field.numberType === '1') {
          text = '整数'
        } else {
          text = '百分比'
        }
        return text
      },
      set: function (newValue) {
      }
    },
    // 公式返回类型
    formulaReturnVal: {
      get: function () {
        let text
        if (this.myComponent.field.numberType === '0') {
          text = '数字'
        } else if (this.myComponent.field.numberType === '1') {
          text = '整数'
        } else if (this.myComponent.field.numberType === '2') {
          text = '百分比'
        } else if (this.myComponent.field.numberType === '3') {
          text = '文本'
        } else {
          text = '日期时间'
        }
        return text
      },
      set: function (newValue) {
      }
    },
    // 人员选择范围
    memberChooseRange: {
      get: function () {
        let list = []
        this.myComponent.field.chooseRange.map((item, index) => {
          list.push(item.name)
        })
        return list.toString()
      },
      set: function (newValue) {
      }

    },
    // 其他时间的设置
    ortherSetting: {
      get: function () {
        let isDate
        if (this.myComponent.type === 'datetime') {
          isDate = true
        } else {
          isDate = false
        }
        return isDate
      }
    }
  },
  watch: {
    myComponent (value) {
      console.log(this.myComponent, '传过来的数据')
    },
    myColumn () {
      console.log(this.myColumn, '传过来的分栏数据')
    }
  },
  filters: {
    toText (value, type) {
      let text
      let list = []
      switch (type) {
        case 'personnel':
          value.map((item) => {
            list.push(item.name)
          })
          text = list.toString()
          break
        case 'picklist': case 'multi':
          value.map((item) => {
            list.push(item.label)
          })
          text = list.toString()
          break
        case 'datetime':
          list = value.split(',')
          text = new Date(Number(list[0])).toLocaleDateString() + '到' + new Date(Number(list[1])).toLocaleDateString()
          break
        default:
          text = value
          break
      }
      return text
    }
  }
}
</script>
<style lang="scss" scoped>
.setting-detail{
  width: 300px;
  overflow: auto;
}
.fade-enter-active, .fade-leave-active {
    transition: opacity .5s
  }
.fade-enter, .fade-leave-to /* .fade-leave-active in below version 2.1.8 */ {
  opacity: 0
}
</style>

<style lang="scss">
.custom-drag-right-wrip{
  flex: 0 0 300px;
  background: #FFFFFF;
  text-align: left;
  border-top: 2px solid #e7e7e7;
  @import '../../../../../static/css/defined-property.scss';
  .module-setting,.colum-setting,.field-setting{
    width: 100%;
    >p{
      height: 50px;
      line-height: 50px;
      margin:0 0 20px;
      font-size: 16px;
      color: #212121;
      text-align:center;
      border-bottom: 1px solid #267DC5;
    }
  }
}
</style>
