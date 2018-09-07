<template>
  <el-container class="custom-subform-wrip">
    <div class="setting-detail">
      <p>字段设置</p>
      <div class="subform-save" @click="saveSubformSetting()"><span>保存返回</span></div>
      <el-collapse v-model="activeNames">
        <!-- 字段名称 -->
        <el-collapse-item name="1">
          <template slot="title">
            <i class="el-icon-caret-right"></i>字段名称
          </template>
          <div class="input-box">
            <el-input v-model="subformComponent.label" placeholder="请输入内容..." :disabled="true"></el-input>
          </div>
          <div class="input-box" v-if="isShowPointOut">
            <label for="">提示框</label>
            <el-input v-model="subformComponent.field.pointOut" placeholder="请输入" :disabled="true"></el-input>
          </div>
          <div class="input-box" v-if="isShowDefault">
            <label for="">默认值</label>
            <el-input v-model="subformComponent.field.defaultValue" placeholder="请输入内容..." :disabled="true"></el-input>
          </div>
        </el-collapse-item>
        <!-- 单选、多选 -->
        <el-collapse-item name="2" v-if="isShowChooseType">
          <template slot="title">
            <i class="el-icon-caret-right"></i>类型
          </template>
          <div class="input-box">
            <el-select v-model="defaultChooseType" @change="modelChange('chooseType',$event)" placeholder="请选择" disabled>
              <el-option v-for="chooseType in chooseTypeList" :key="chooseType.name" :label="chooseType.label" :value="chooseType.value">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 下拉选项  -->
        <el-collapse-item name="3" v-if="subformComponent.type == 'picklist'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>选项内容
          </template>
          <draggable class="option-box" :options="dragOption" v-model="subformComponent.entrys">
            <div class="option" v-for="(select,index) in subformComponent.entrys" :key="index">
               <el-input v-model="select.label" :disabled="true"></el-input>
               <el-color-picker v-model="select.color" disabled></el-color-picker>
               <i class="el-icon-close" @click="handleDelItem(subformComponent.type,index,select)"></i>
            </div>
          </draggable>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" disabled @click="handleAddItem(subformComponent.type)">添加</el-button>
          </div>
          <div class="input-box">
            <label for="">默认值</label>
            <el-select v-model="defaultEntrysValue" placeholder="-无-" clearable value-key="value" @change="modelChange('picklistDefaultValue',$event)" v-show="subformComponent.field.chooseType === '0'">
              <el-option v-for="(item,index) in selectDefault" :key="index" :label="item.label" :value="item">
              </el-option>
            </el-select>
            <el-select v-model="subformComponent.field.defaultEntrys" placeholder="-无-" value-key="value" multiple v-show="subformComponent.field.chooseType === '1'">
              <el-option v-for="(item,index) in selectDefault" :key="index" :label="item.label" :value="item">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 电话 -->
        <el-collapse-item name="4" v-if="subformComponent.type === 'phone'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>类型
          </template>
          <div class="input-box">
            <el-select v-model="phoneModel" placeholder="电话" disabled @change="modelChange('phoneTypeChange',$event)">
              <el-option v-for="phone in phoneType" :key="phone.name" :label="phone.label" :value="phone">
              </el-option>
            </el-select>
          </div>
          <div class="input-box">
            <label for="">位数</label>
            <el-select placeholder="-无-" v-model="phoneLengthVal" @change="modelChange('phoneLength', $event)" :disabled="subformComponent.field.phoneType === '0'">
              <el-option v-for="phoneLenth in phoneLengths"
              :key="phoneLenth.name"
              :label="phoneLenth.label"
              :value="phoneLenth">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 数字类型 -->
        <el-collapse-item name="5" v-if="subformComponent.type === 'number'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>类型
          </template>
          <div class="input-box">
            <el-select v-model="numberModel" disabled @change="modelChange('numberTypeChange',$event)">
              <el-option v-for="(number,index) in numberType" :key="index" :label="number.label" :value="number">
              </el-option>
            </el-select>
          </div>
          <div class="input-box" v-show="subformComponent.field.numberType !== '1'">
             <label for="">小数位数</label>
              <el-select placeholder="-无-" disabled v-model="subformComponent.field.numberLenth">
                <el-option v-for="numberLenth in numberDecimals" :key="numberLenth.name" :label="numberLenth" :value="numberLenth">
                </el-option>
              </el-select>
          </div>
          <div class="input-box">
            <label for="">默认值</label>
            <el-input type="number" v-model="subformComponent.field.defaultValue" placeholder="请输入内容..."></el-input>
          </div>
        </el-collapse-item>
        <!-- 数字范围 -->
        <el-collapse-item name="6" v-if="subformComponent.type === 'number'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>输入范围
          </template>
          <div class="input-range">
            <el-input type="number" v-model="subformComponent.field.betweenMin" placeholder="不填代表无限制"></el-input>
            <span>~</span>
            <el-input type="number" v-model="subformComponent.field.betweenMax" placeholder="不填代表无限制"></el-input>
          </div>
        </el-collapse-item>
        <!-- 时间日期类型 -->
        <el-collapse-item name="7" v-if="subformComponent.type === 'datetime'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>类型
          </template>
          <div class="input-box">
            <el-select v-model="subformComponent.field.formatType" placeholder="年-月-日" disabled>
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
          <div class="input-box" v-show="subformComponent.field.defaultValueId === '2'">
            <el-date-picker v-model="subformComponent.field.defaultValue" type="datetime" :format="subformComponent.field.formatType"
              value-format="timestamp" placeholder="选择日期时间" @change="modelChange('datetimeVal',$event)">
            </el-date-picker>
          </div>
        </el-collapse-item>
        <!-- 附件 -->
        <el-collapse-item  name="8" v-if="subformComponent.type === 'attachment'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>数量控制
          </template>
          <el-radio-group  size="mini" v-model="subformComponent.field.countLimit">
            <el-radio label= '0'>不限制</el-radio>
            <el-radio label= '1'>限制</el-radio>
          </el-radio-group>
          <div class="line-box" v-show="subformComponent.field.countLimit ==='1'">
            只允许上传<el-input type="number" placeholder="最大10" v-model="subformComponent.field.maxCount"></el-input>个附件
          </div>
          <div class="line-box" v-show="subformComponent.field.countLimit ==='1'">
            每个附件限制<el-input type="number" placeholder="最大100" v-model="subformComponent.field.maxSize"></el-input>M
          </div>
        </el-collapse-item>
        <!-- 图片尺寸 -->
        <el-collapse-item  name="9" v-if="subformComponent.type === 'picture'">
           <template slot="title">
            <i class="el-icon-caret-right"></i>尺寸要求
          </template>
          <div class="input-box">
            <el-select  v-model="subformComponent.field.imageSize" placeholder="" disabled>
              <el-option v-for="imageSize in imageSizeList"
                :key="imageSize.name"
                :label="imageSize"
                :value="imageSize">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 图片大小限制 -->
        <el-collapse-item  name="10" v-if="subformComponent.type === 'picture'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>数量控制
          </template>
          <el-radio-group  size="mini" v-model="subformComponent.field.countLimit">
            <el-radio label= '0'>不限制</el-radio>
            <el-radio label= '1'>限制</el-radio>
          </el-radio-group>
          <div class="line-box" v-show="subformComponent.field.countLimit ==='1'">
            只允许上传<el-input type="number" placeholder="最大10" v-model="subformComponent.field.maxCount"></el-input>张
          </div>
          <div class="line-box" v-show="subformComponent.field.countLimit ==='1'">
            每个附件限制<el-input type="number" placeholder="最大100" v-model="subformComponent.field.maxSize"></el-input>M
          </div>
        </el-collapse-item>
        <!-- 复选框  -->
        <el-collapse-item name="11" v-if="subformComponent.type == 'multi'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>选项内容
          </template>
          <draggable class="option-box" :options="dragOption" v-model="subformComponent.entrys">
            <div class="option" v-for="(select,index) in subformComponent.entrys" :key="index">
              <el-input v-model="select.label" :disabled="true"></el-input>
              <i class="el-icon-close" @click="handleDelItem(subformComponent.type,index,select)"></i>
            </div>
          </draggable>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" disabled @click="handleAddItem(subformComponent.type)">添加</el-button>
          </div>
          <div class="input-box">
            <label for="">默认值</label>
            <el-select v-model="defaultEntrysValue" placeholder="-无-" clearable value-key="value" @change="modelChange('multiDefaultValue',$event)" v-show="subformComponent.field.chooseType === '0'">
              <el-option v-for="(item,index) in selectDefault" :key="index" :label="item.label" :value="item">
              </el-option>
            </el-select>
            <el-select v-model="subformComponent.field.defaultEntrys" placeholder="-无-" value-key="value" multiple v-show="subformComponent.field.chooseType === '1'">
              <el-option v-for="(item,index) in selectDefault" :key="index" :label="item.label" :value="item">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 多级下拉 -->
        <el-collapse-item name="12" v-if="subformComponent.type === 'mutlipicklist'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>类型
          </template>
          <el-radio-group size="mini" v-model="subformComponent.field.selectType" disabled>
            <el-radio-button label="0">二级下拉</el-radio-button>
            <el-radio-button label="1">三级下拉</el-radio-button>
          </el-radio-group>
          <el-tabs type="border-card" v-model="mutlipicklistActive" @tab-click="multiPickChange">
            <el-tab-pane label="一级" name="mutlipicklist1" class="option-box">
              <div class="option" v-for="(item1,index) in subformComponent.entrys" :key="index">
                <el-input v-model="item1.label" @change="clearDefault($event, 'mulPick')" :disabled="true"></el-input>
                <el-color-picker v-model="item1.color" disabled></el-color-picker>
                <i class="el-icon-close" @click="handleDelItem(subformComponent.type,index,item1)"></i>
              </div>
            </el-tab-pane>
            <el-tab-pane label="二级" name="mutlipicklist2" class="option-box">
              <div class="input-box">
                <label for="">一级选项</label>
                <el-select placeholder="-无-" v-model="multiSelect1_model" @change="modelChange('multiSelectOne',$event)">
                  <el-option v-for="item1 in subformComponent.entrys" :key="item1.value" :label="item1.label" :value="item1">
                  </el-option>
                </el-select>
              </div>
              <div class="option" v-for="(item2,index2) in multiSelectTwoList" :key="index2">
                <el-input v-model="item2.label"  @change="clearDefault($event, 'mulPick')" :disabled="true"></el-input>
                <el-color-picker v-model="item2.color" disabled></el-color-picker>
                <i class="el-icon-close" @click="handleDelItem(subformComponent.type + '2',index2,item2)"></i>
              </div>
            </el-tab-pane>
            <el-tab-pane label="三级" name="mutlipicklist3" class="option-box" v-if="subformComponent.field.selectType === '1'">
              <div class="input-box">
                <label for="">一级选项</label>
                <el-select placeholder="-无-" v-model="multiSelect1_model" @change="modelChange('multiSelectOne',$event)">
                  <el-option v-for="item1 in subformComponent.entrys" :key="item1.value" :label="item1.label" :value="item1">
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
                <el-input v-model="item3.label"  @change="clearDefault($event, 'mulPick')" :disabled="true"></el-input>
                <el-color-picker v-model="item3.color" disabled></el-color-picker>
                <i class="el-icon-close" @click="handleDelItem(subformComponent.type + '3',index3,item3)"></i>
              </div>
            </el-tab-pane>
          </el-tabs>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" disabled @click="handleAddItem(subformComponent.type)">添加选项</el-button>
          </div>
          <div class="input-box" v-show="mutlipicklistActive === 'mutlipicklist1'">
            <label for="">一级默认值</label>
            <el-select v-model="subformComponent.defaultEntrys.oneDefaultValue" placeholder="-无-" clearable @change="modelChange('multiDefaultValue1',$event)">
              <el-option v-for="item1 in selectDefault" :key="item1.name" :label="item1.label" :value="item1">
              </el-option>
            </el-select>
          </div>
          <div class="input-box" v-show="mutlipicklistActive === 'mutlipicklist2'">
            <label for="">二级默认值</label>
            <el-select placeholder="-无-" v-model="subformComponent.defaultEntrys.twoDefaultValue" clearable @change="modelChange('multiDefaultValue2',$event)">
              <el-option v-for="item2 in multiSelectTwoListDefault" :key="item2.name" :label="item2.label" :value="item2">
              </el-option>
            </el-select>
          </div>
          <div class="input-box" v-show="mutlipicklistActive === 'mutlipicklist3'">
            <label for="">三级默认值</label>
            <el-select placeholder="-无-" v-model="subformComponent.defaultEntrys.threeDefaultValue" clearable @change="modelChange('multiDefaultValue3',$event)">
              <el-option v-for="item3 in multiSelectThreeListDefault" :key="item3.name" :label="item3.label" :value="item3">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 人员 -->
        <el-collapse-item name="13" v-if="subformComponent.type === 'personnel'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>默认值
          </template>
          <div class="input-box">
            <el-input v-model="defaultMemberValue" placeholder="请选择" @focus="selectMeber(subformComponent.field.chooseType,'subformDefinedPersonnel' + subformComponent.name)" readonly></el-input>
            <i class="el-icon-circle-close" @click="clearPeople('defaultPersonnel')"></i>
          </div>
          <div class="input-box">
            <label for="">可选范围</label>
            <el-input v-model="memberChooseRange" placeholder="请选择" @focus="selectMeber('1','subformChooseRangePersonnel' + subformComponent.name)" readonly></el-input>
            <i class="el-icon-circle-close" @click="clearPeople('chooseRange')"></i>
          </div>
        </el-collapse-item>
        <!-- 部门 -->
        <el-collapse-item name="14" v-if="subformComponent.type === 'department'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>默认值
          </template>
          <div class="input-box">
            <el-input v-model="defaultDepartmentValue" placeholder="请选择" @focus="selectMeber(subformComponent.field.chooseType,'subformDefaultDepartment' + subformComponent.name)" readonly></el-input>
            <i class="el-icon-circle-close" @click="clearPeople('defaultDepartment')"></i>
          </div>
          <div class="input-box">
            <label for="">可选范围</label>
            <el-input v-model="memberChooseRange" placeholder="请选择" @focus="selectMeber('1','subformChooseRangeDepartment' + subformComponent.name)" readonly></el-input>
            <i class="el-icon-circle-close" @click="clearPeople('chooseRange')"></i>
          </div>
        </el-collapse-item>
        <!-- 公式 -->
        <el-collapse-item name="15" v-if="isFormula">
          <template slot="title">
            <i class="el-icon-caret-right"></i>数据类型
          </template>
          <div class="formula-text">
            ={{subformComponent.field.formulaCh}}
          </div>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" disabled @click="showformulaSetting(subformComponent.type, subformComponent.name)">设置公式</el-button>
          </div>
          <div class="input-box">
            <label for="">返回类型</label>
            <el-select placeholder="请选择类型" v-model="formulaReturnVal" disabled @change="modelChange('formulaFormat',$event)">
              <el-option v-for="item in formulaReturn" :key="item.name" :label="item.label" :value="item">
              </el-option>
            </el-select>
          </div>
          <div class="input-box" v-show="subformComponent.field.numberType === '0' || subformComponent.field.numberType === '2'">
            <label for="">小数位数</label>
            <el-select placeholder="-无-" v-model="subformComponent.field.decimalLen" disabled>
              <el-option v-for="decimalLen in numberDecimals" :key="decimalLen.name" :label="decimalLen" :value="decimalLen">
              </el-option>
            </el-select>
          </div>
          <div class="input-box" v-show="subformComponent.field.numberType === '0' || subformComponent.field.numberType === '1' || subformComponent.field.numberType === '2'">
            <label for="">精确度</label>
            <el-select placeholder="-无-" v-model="subformComponent.field.accuracy">
              <el-option v-for="(accuracy,index) in accuracyList" :key="index" :label="accuracy.label" :value="accuracy.value">
              </el-option>
            </el-select>
          </div>
          <div class="input-box" v-show="subformComponent.field.numberType === '4'">
            <label for="">类型</label>
            <el-select v-model="subformComponent.field.decimalLen" placeholder="yyyy-MM-dd" disabled>
              <el-option v-for="date in dateList" :key="date.name" :label="date" :value="date">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 公式其他设置 -->
        <el-collapse-item name="16" v-if="isFormula">
          <template slot="title">
            <i class="el-icon-caret-right"></i>其他设置
          </template>
          <div class="checkbox-box">
            <el-checkbox v-model="subformComponent.field.formulaCalculates" true-label='1' false-label='0' disabled>新公式是否对旧数据重新计算</el-checkbox>
          </div>
        </el-collapse-item>
        <!-- 自动编号 -->
        <el-collapse-item name="17" v-if="subformComponent.type === 'identifier'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>编号结果
          </template>
          <div class="formula-text">
            ={{numberingRes}}
          </div>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" disabled @click="openIdentifier">设置编号规则</el-button>
          </div>
        </el-collapse-item>
        <!-- 关联关系 -->
        <el-collapse-item name="18" v-if="subformComponent.type === 'reference'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>关联模块
          </template>
          <div class="input-box">
            <el-select v-model="referenceModuleModel" value-key="id" disabled @change="modelChange('referenceModule',$event)">
              <el-option v-for="(list, index) in moduleList" :key="index" :label="list.chinese_name" :value="list">
              </el-option>
            </el-select>
          </div>
          <div class="input-box">
            <label for="">关联字段</label>
            <el-select v-model="referenceFieldModel" value-key="value" disabled @change="modelChange('referenceField',$event)">
              <el-option v-for="(field, index) in referenceFieldList" :key="index" :label="field.label" :value="field">
              </el-option>
            </el-select>
          </div>
           <div class="refe-box" v-show="searchFieldList.length > 0">
            <label for="">列表搜索字段</label>
            <el-select v-model="subformComponent.searchFields" value-key="fieldName" multiple disabled>
              <el-option v-for="field in searchFieldList" :key="field.fieldName" :label="field.fieldLabel" :value="field">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 关联关系筛选条件 -->
        <el-collapse-item name="19" v-if="subformComponent.type === 'reference'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>设置关联筛选条件
          </template>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" @click="editFilterCondition">添加筛选条件</el-button>
          </div>
          <div class="condition-text">
              <div class="item" v-for="(item,index) in subformComponent.relevanceWhere" :key="index">
                {{index + 1}}. {{item.field_label}} <span>{{item.operator_label}}</span> {{item.result_value | toText(item.show_type)}}
              </div>
            </div>
        </el-collapse-item>
        <!-- 地址位置 -->
        <el-collapse-item name="20" v-if="subformComponent.type === 'location'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>地址位置
          </template>
          <div class="checkbox-box">
            <el-checkbox true-label='1' false-label='0' v-model="subformComponent.field.defaultValue">默认当前位置</el-checkbox>
          </div>
        </el-collapse-item>
        <!-- 字段控制 -->
        <el-collapse-item name="21" v-if="isShowfieldControl">
          <template slot="title">
            <i class="el-icon-caret-right"></i>字段控制
          </template>
          <el-radio-group size="mini" v-model="subformComponent.field.fieldControl">
            <el-radio label='1'>只读</el-radio>
            <el-radio label='2'>必填</el-radio>
            <el-radio label='0'>不控制</el-radio>
          </el-radio-group>
        </el-collapse-item>
        <!-- 字段权限 -->
        <el-collapse-item name="22" v-if="isShowfieldAuth">
          <template slot="title">
            <i class="el-icon-caret-right"></i>字段权限
          </template>
          <div class="checkbox-box">
            <el-checkbox true-label='1' false-label='0' v-model="subformComponent.field.addView">新增时显示</el-checkbox>
            <el-checkbox true-label='1' false-label='0' v-model="subformComponent.field.editView">编辑时显示</el-checkbox>
          </div>
        </el-collapse-item>
        <!-- 查重 -->
        <el-collapse-item name="23" v-if="subformComponent.field.repeatCheck">
          <template slot="title">
            <i class="el-icon-caret-right"></i>查重
          </template>
          <el-radio-group size="mini" v-model="subformComponent.field.repeatCheck" >
            <el-radio label='0'>不启用查重</el-radio>
            <el-radio label='1'>启用查重，允许保存数据</el-radio>
            <el-radio label='2'>启用查重，不允许保存数据</el-radio>
          </el-radio-group>
        </el-collapse-item>
        <!-- 终端 -->
        <el-collapse-item name="24">
          <template slot="title">
            <i class="el-icon-caret-right"></i>终端
          </template>
          <div class="checkbox-box">
            <el-checkbox v-model="subformComponent.field.terminalPc" true-label='1' false-label='0'>PC</el-checkbox>
            <el-checkbox v-model="subformComponent.field.terminalApp" true-label='1' false-label='0'>APP</el-checkbox>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
  </el-container>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import {formulaReturn1, formulaReturn2} from '@/common/js/constant.js'
import draggable from 'vuedraggable'
export default {
  name: 'SubformSettingPage',
  components: {
    draggable
  },
  props: ['subformComponent', 'components', 'subformName'],
  data () {
    return {
      activeNames: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24'], // 折叠板的具体展开选项
      chooseTypeList: [{ value: '0', label: '单选' }, { value: '1', label: '多选' }], // 复选框组件类型
      dateList: ['yyyy', 'yyyy-MM', 'yyyy-MM-dd', 'yyyy-MM-dd HH', 'yyyy-MM-dd HH:mm', 'yyyy-MM-dd HH:mm:ss'],
      dateTimeOptions: [{ value: '0', label: '-无-' }, { value: '1', label: '当前时间' }, { value: '2', label: '指定时间' }],
      imageSizeList: ['30px*30px', '60px*60px'], // 图片大小
      formulaReturn: [],
      mutlipicklistActive: 'mutlipicklist1',
      multiSelect1_model: '',
      multiSelect2_model: '',
      multiSelectTwoList: [],
      multiSelectThreeList: [],
      multiSelectTwoListDefault: [],
      multiSelectThreeListDefault: [],
      moduleList: [],
      fieldList: [],
      referenceFieldList: [],
      phoneType: [{label: '电话', value: '0'}, {label: '手机号码', value: '1'}],
      phoneLengths: [{ value: '0', label: '不限' }, {value: '1', label: '11'}],
      numberType: [ // 数字类型
        {value: '0', label: '数字'},
        {value: '1', label: '整数'},
        {value: '2', label: '百分比'}
      ],
      numberDecimals: ['1', '2', '3', '4'],
      accuracyList: [{ value: '0', label: '四舍五入' }, {value: '1', label: '去尾法'}, {value: '2', label: '进一法'}] // 精确度
    }
  },
  created () {
    // 关联关系
    if (this.subformComponent.type === 'reference') {
      this.ajaxGetModuleList(null)
      if (this.subformComponent.relevanceModule.moduleName) {
        let bean = {bean: this.subformComponent.relevanceModule.moduleName}
        this.ajaxGetFieldList(bean)
      }
    }
    this.formulaReturn = this.subformComponent.type === 'formula' ? formulaReturn1 : formulaReturn2
  },
  methods: {
    // model 发生变化
    modelChange (property, data, listIndex) {
      switch (property) {
        case 'datetimeVal':
          this.subformComponent.field.defaultValue = String(data)
          console.log(this.subformComponent, '修改后')
          break
        case 'datetimeId':
          if (data.value === '2') {
            this.subformComponent.field.defaultValueId = data.value
            this.subformComponent.field.defaultValue = String(new Date().getTime())
          } else {
            this.subformComponent.field.defaultValueId = data.value
            this.subformComponent.field.defaultValue = ''
          }
          console.log(this.subformComponent, '修改后')
          break
        case 'chooseType':
          this.subformComponent.field.chooseType = data
          if (this.subformComponent.field.defaultPersonnel) {
            this.subformComponent.field.defaultPersonnel = []
          }
          console.log(this.subformComponent, '修改后')
          break
        case 'picklistDefaultValue':
          if (data.value) {
            this.subformComponent.field.defaultEntrys = [data]
          } else {
            this.subformComponent.field.defaultEntrys = []
          }
          console.log(this.subformComponent, '修改后')
          break
        case 'multiDefaultValue':
          if (data.value) {
            this.subformComponent.field.defaultEntrys = [data]
          } else {
            this.subformComponent.field.defaultEntrys = []
          }
          console.log(this.subformComponent, '修改后')
          break
        case 'referenceModule':
          this.subformComponent.relevanceModule.moduleLabel = data.chinese_name
          this.subformComponent.relevanceModule.moduleName = data.english_name
          this.subformComponent.relevanceField.fieldLabel = ''
          this.subformComponent.searchFields = []
          console.log(this.subformComponent, '修改后')
          let bean = {bean: data.english_name}
          this.ajaxGetFieldList(bean)
          break
        case 'referenceField':
          this.subformComponent.relevanceField.fieldLabel = data.label
          this.subformComponent.relevanceField.fieldName = data.value
          console.log(this.subformComponent, '修改后')
          break
        case 'phoneTypeChange':
          this.subformComponent.field.phoneType = data.value
          if (data.value === '0') {
            this.subformComponent.field.phoneLenth = '0'
          }
          console.log(this.subformComponent, '修改后')
          break
        case 'phoneLength':
          this.subformComponent.field.phoneLenth = data.value
          console.log(this.subformComponent.field, '修改后')
          break
        case 'numberTypeChange':
          this.subformComponent.field.numberType = data.value
          console.log(this.subformComponent, '修改后')
          break
        case 'formulaFormat':
          this.subformComponent.field.numberType = data.value
          if (data.value === '4') {
            this.subformComponent.field.decimalLen = 'yyyy-MM-dd'
          } else {
            this.subformComponent.field.decimalLen = '2'
            if (data.value === '1') {
              this.subformComponent.field.decimalLen = '0'
            }
          }
          console.log(this.subformComponent, '修改后')
          break
        case 'multiSelectOne':
          this.multiSelect2_model = ''
          this.multiSelectTwoList = data.subList
          this.multiSelectThreeList = []
          console.log(this.subformComponent, '修改后')
          break
        case 'multiSelectTwo':
          this.multiSelectThreeList = data.subList
          console.log(this.subformComponent, '修改后')
          break
        case 'multiDefaultValue1':
          this.multiSelectTwoListDefault = data.subList
          if (data) {
            this.subformComponent.defaultEntrys.oneDefaultValue = data.label
            this.subformComponent.defaultEntrys.oneDefaultValueId = data.value
            this.subformComponent.defaultEntrys.oneDefaultValueColor = data.color
          } else {
            this.subformComponent.defaultEntrys.oneDefaultValue = ''
            this.subformComponent.defaultEntrys.oneDefaultValueId = ''
            this.subformComponent.defaultEntrys.oneDefaultValueColor = ''
          }
          this.subformComponent.defaultEntrys.twoDefaultValue = ''
          this.subformComponent.defaultEntrys.twoDefaultValueId = ''
          this.subformComponent.defaultEntrys.twoDefaultValueColor = ''
          this.subformComponent.defaultEntrys.threeDefaultValue = ''
          this.subformComponent.defaultEntrys.threeDefaultValueId = ''
          this.subformComponent.defaultEntrys.threeDefaultValueColor = ''
          console.log(this.subformComponent.defaultEntrys, '修改后')
          break
        case 'multiDefaultValue2':
          this.multiSelectThreeListDefault = data.subList
          if (data) {
            this.subformComponent.defaultEntrys.twoDefaultValue = data.label
            this.subformComponent.defaultEntrys.twoDefaultValueId = data.value
            this.subformComponent.defaultEntrys.twoDefaultValueColor = data.color
          } else {
            this.subformComponent.defaultEntrys.twoDefaultValue = ''
            this.subformComponent.defaultEntrys.twoDefaultValueId = ''
            this.subformComponent.defaultEntrys.twoDefaultValueColor = ''
          }
          this.subformComponent.defaultEntrys.threeDefaultValue = ''
          this.subformComponent.defaultEntrys.threeDefaultValueId = ''
          this.subformComponent.defaultEntrys.threeDefaultValueColor = ''
          console.log(this.subformComponent.defaultEntrys, '修改后')
          break
        case 'multiDefaultValue3':
          if (data) {
            this.subformComponent.defaultEntrys.threeDefaultValue = data.label
            this.subformComponent.defaultEntrys.threeDefaultValueId = data.value
            this.subformComponent.defaultEntrys.threeDefaultValueColor = data.color
          } else {
            this.subformComponent.defaultEntrys.threeDefaultValue = ''
            this.subformComponent.defaultEntrys.threeDefaultValueId = ''
            this.subformComponent.defaultEntrys.threeDefaultValueColor = ''
          }
          console.log(this.subformComponent.defaultEntrys, '修改后')
          break
        default :
          break
      }
    },
    // 多级下拉变化
    multiPickChange (data) {
      let list = JSON.parse(JSON.stringify(this.subformComponent.entrys))
      let id = this.subformComponent.defaultEntrys.oneDefaultValueId
      list.map((item) => {
        if (item.value === id) {
          this.multiSelectTwoListDefault = item.subList
        }
      })
      if (data.name === 'mutlipicklist3') {
        let id2 = this.subformComponent.defaultEntrys.twoDefaultValueId
        this.multiSelectTwoListDefault.map((item) => {
          if (item.value === id2) {
            this.multiSelectThreeListDefault = item.subList
          }
        })
      }
    },
    // 保存子表单设置
    saveSubformSetting () {
      this.$bus.emit('send_subformVisbale', false)
    },
    // 公式弹出框
    showformulaSetting (type, name) {
      let list = this.getFormulaList(this.components)
      let data = {
        id: name,
        isOpen: true,
        type: type,
        name: this.subformComponent.label,
        textarea: this.subformComponent.field.formulaCh,
        chinese: this.subformComponent.field.formulaCh,
        english: this.subformComponent.field.formulaEn,
        fieldList: list
      }
      this.$bus.emit('openFormula', data)
    },
    // 删除人员默认值及范围
    clearPeople (name) {
      this.subformComponent.field[name] = []
    },
    // 设置高级筛选条件
    editFilterCondition () {
      let bean = this.subformComponent.relevanceModule.moduleName
      let value = {
        bean: bean,
        name: this.subformComponent.name,
        conditions: this.subformComponent.relevanceWhere,
        highWhere: this.subformComponent.seniorWhere
      }
      if (bean) {
        this.$bus.emit('openHighCondition', value)
      }
    },
    // 打开自动编号
    openIdentifier () {
      let value = {
        isOpen: true,
        name: this.subformComponent.name,
        fixedValue: this.subformComponent.numbering.fixedValue,
        dateValue: this.subformComponent.numbering.dateValue,
        serialValue: this.subformComponent.numbering.serialValue
      }
      this.$bus.emit('openIdentifer', value)
    },
    // 选择人员
    selectMeber (type, key) {
      let navKey, list
      let types = type === '0' ? 1 : ''
      if (key === 'subformDefinedPersonnel' + this.subformComponent.name) {
        navKey = type === '0' ? '1,3' : '1'
        list = this.subformComponent.field.defaultPersonnel
      } else if (key === 'subformDefaultDepartment' + this.subformComponent.name) {
        navKey = type === '0' ? '0,3' : '0,3'
        list = this.subformComponent.field.defaultDepartment
      } else if (key === 'subformChooseRangePersonnel' + this.subformComponent.name) {
        navKey = type === '0' ? '1,3' : '1,0'
        list = this.subformComponent.field.chooseRange
      } else if (key === 'subformChooseRangeDepartment' + this.subformComponent.name) {
        navKey = type === '0' ? '0,3' : '0'
        list = this.subformComponent.field.chooseRange
      }
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
    // 下拉菜单改变时清空默认值
    clearDefault (event, type) {
      if (type === 'pick') {
        this.subformComponent.field.defaultValue = ''
        this.subformComponent.field.defaultValueColor = ''
        this.subformComponent.field.defaultValueId = ''
      } else {
        this.subformComponent.defaultEntrys.oneDefaultValue = ''
        this.subformComponent.defaultEntrys.oneDefaultValueColor = ''
        this.subformComponent.defaultEntrys.oneDefaultValueId = ''
        this.subformComponent.defaultEntrys.threeDefaultValue = ''
        this.subformComponent.defaultEntrys.threeDefaultValueColor = ''
        this.subformComponent.defaultEntrys.threeDefaultValueId = ''
        this.subformComponent.defaultEntrys.twoDefaultValue = ''
        this.subformComponent.defaultEntrys.twoDefaultValueColor = ''
        this.subformComponent.defaultEntrys.twoDefaultValueId = ''
      }
    },
    // 添加选项
    handleAddItem (property) {
      let valueList = []
      this.subformComponent.entrys.map((item) => {
        valueList.push(item.value)
      })
      let max = Math.max.apply(Math, valueList)
      switch (property) {
        case 'multi':
          let len = this.subformComponent.entrys.length
          if (len === 0) {
            this.subformComponent.entrys.push({
              label: '新选项',
              value: '0'
            })
            return
          }
          let item = {
            label: '新选项',
            value: String(max + 1)
          }
          this.subformComponent.entrys.push(item)
          console.log(this.subformComponent, '修改后')
          break
        case 'mutlipicklist':
          if (this.mutlipicklistActive === 'mutlipicklist1') {
            // 一级下拉添加选项
            let len1 = this.subformComponent.entrys.length
            let item1
            if (len1 === 0) {
              item1 = {
                label: '一级新选项',
                value: '0',
                color: '#FFFFFF',
                subList: []
              }
            } else {
              let lastItemValue1 = parseInt(this.subformComponent.entrys[len1 - 1].value)
              item1 = {
                label: '一级新选项',
                value: String(lastItemValue1 + 1),
                color: '#FFFFFF',
                subList: []
              }
            }
            this.subformComponent.entrys.push(item1)
          } else if (this.mutlipicklistActive === 'mutlipicklist2') {
            // 二级下拉添加选项
            let len2 = this.multiSelectTwoList.length
            let item2
            if (len2 === 0) {
              item2 = {
                label: '二级新选项',
                value: '0',
                color: '#FFFFFF',
                subList: []
              }
            } else {
              let lastItemValue2 = parseInt(this.multiSelectTwoList[len2 - 1].value)
              item2 = {
                label: '二级新选项',
                value: String(lastItemValue2 + 1),
                color: '#FFFFFF',
                subList: []
              }
            }
            if (this.multiSelect1_model) {
              this.multiSelectTwoList.push(item2)
            }
          } else if (this.mutlipicklistActive === 'mutlipicklist3') {
            // 三级下拉添加选项
            let len3 = this.multiSelectThreeList.length
            let item3
            if (len3 === 0) {
              item3 = {
                label: '三级新选项',
                value: '0',
                color: '#FFFFFF'
              }
            } else {
              let lastItemValue3 = parseInt(this.multiSelectThreeList[len3 - 1].value)
              item3 = {
                label: '三级新选项',
                value: String(lastItemValue3 + 1),
                color: '#FFFFFF'
              }
            }
            if (this.multiSelect2_model) {
              this.multiSelectThreeList.push(item3)
            }
          }
          break
        case 'picklist':
          let lens = this.subformComponent.entrys.length
          if (lens === 0) {
            this.subformComponent.entrys.push({
              label: '新选项',
              value: '0',
              color: '#FFFFFF'
            })
            return
          }
          let items = {
            label: '新选项',
            value: String(max + 1),
            color: '#FFFFFF'
          }
          this.subformComponent.entrys.push(items)
          break
        default:
          break
      }
    },
    // 删除选项
    handleDelItem (property, index, select) {
      switch (property) {
        case 'multi': case 'picklist':
          this.subformComponent.entrys.splice(index, 1)
          this.subformComponent.field.defaultEntrys.map((item, index2) => {
            if (JSON.stringify(select) === JSON.stringify(item)) {
              this.subformComponent.field.defaultEntrys.splice(index2, 1)
            }
          })
          break
        case 'mutlipicklist':
          this.subformComponent.entrys.splice(index, 1)
          if (select.value === this.subformComponent.defaultEntrys.oneDefaultValueId) {
            this.subformComponent.defaultEntrys.oneDefaultValue = ''
            this.subformComponent.defaultEntrys.oneDefaultValueId = ''
            this.subformComponent.defaultEntrys.oneDefaultValueColor = ''
            this.subformComponent.defaultEntrys.twoDefaultValue = ''
            this.subformComponent.defaultEntrys.twoDefaultValueId = ''
            this.subformComponent.defaultEntrys.twoDefaultValueColor = ''
            this.subformComponent.defaultEntrys.threeDefaultValue = ''
            this.subformComponent.defaultEntrys.threeDefaultValueId = ''
            this.subformComponent.defaultEntrys.threeDefaultValueColor = ''
          }
          break
        case 'mutlipicklist2':
          this.multiSelectTwoList.splice(index, 1)
          if (select.value === this.subformComponent.defaultEntrys.twoDefaultValueId) {
            this.subformComponent.defaultEntrys.twoDefaultValue = ''
            this.subformComponent.defaultEntrys.twoDefaultValueId = ''
            this.subformComponent.defaultEntrys.twoDefaultValueColor = ''
            this.subformComponent.defaultEntrys.threeDefaultValue = ''
            this.subformComponent.defaultEntrys.threeDefaultValueId = ''
            this.subformComponent.defaultEntrys.threeDefaultValueColor = ''
          }
          break
        case 'mutlipicklist3':
          this.multiSelectThreeList.splice(index, 1)
          if (select.value === this.subformComponent.defaultEntrys.threeDefaultValueId) {
            this.subformComponent.defaultEntrys.threeDefaultValue = ''
            this.subformComponent.defaultEntrys.threeDefaultValueId = ''
            this.subformComponent.defaultEntrys.threeDefaultValueColor = ''
          }
          break
        default:
          break
      }
    },
    // 获取公式字段列表
    getFormulaList (list) {
      let formulaItems = []
      if (list) {
        list.map((list, index) => {
          list.rows.map((item, index) => {
            if (item.name === this.subformName && item.componentList) {
              item.componentList.map((item2) => {
                formulaItems.push(
                  {label: item2.label,
                    value: item2.name,
                    type: item2.type,
                    subform: []
                  }
                )
              })
            }
          })
        })
      }
      return formulaItems
    },
    // 获取关联模块列表
    ajaxGetModuleList (data) {
      HTTPServer.getAllModule(data, 'Loading').then((res) => {
        this.moduleList = res
      })
    },
    // 获取关联字段列表
    ajaxGetFieldList (data) {
      HTTPServer.getModuleFieldList(data, 'Loading').then((res) => {
        this.fieldList = res
        let fieldList = []
        res.map((item) => {
          if (item.type === 'text' || item.type === 'textarea' || item.type === 'phone' || item.type === 'identifier' ||
          item.type === 'email' || item.type === 'number' || item.type === 'url' || item.type === 'datetime' || item.type === 'reference') {
            fieldList.push(item)
          }
        })
        this.referenceFieldList = fieldList
      })
    }
  },
  mounted () {
    // 获取人员单选数据
    this.$bus.on('selectMemberRadio', (value) => {
      if (value.prepareKey === 'subformDefinedPersonnel' + this.subformComponent.name) {
        this.subformComponent.field.defaultPersonnel = value.prepareData
      } else if (value.prepareKey === 'subformDefaultDepartment' + this.subformComponent.name) {
        this.subformComponent.field.defaultDepartment = value.prepareData
      }
    })
    // 获取人员多选数据
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value.prepareKey === 'subformDefinedPersonnel' + this.subformComponent.name) {
        this.subformComponent.field.defaultPersonnel = value.prepareData
      } else if (value.prepareKey === 'subformDefaultDepartment' + this.subformComponent.name) {
        this.subformComponent.field.defaultDepartment = value.prepareData
      } else if (value.prepareKey === 'subformChooseRangePersonnel' + this.subformComponent.name || value.prepareKey === 'subformChooseRangeDepartment' + this.subformComponent.name) {
        this.subformComponent.field.chooseRange = value.prepareData
      }
    })
    // 获取部门单选数据
    this.$bus.on('selectDepartmentRadio', (value) => {
      if (value.prepareKey === 'subformDefinedPersonnel' + this.subformComponent.name) {
        this.subformComponent.field.defaultPersonnel = value.prepareData
      } else if (value.prepareKey === 'subformDefaultDepartment' + this.subformComponent.name) {
        this.subformComponent.field.defaultDepartment = value.prepareData
      }
    })
    // 获取部门多选数据
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value.prepareKey === 'subformDefinedPersonnel' + this.subformComponent.name) {
        this.subformComponent.field.defaultPersonnel = value.prepareData
      } else if (value.prepareKey === 'subformDefaultDepartment' + this.subformComponent.name) {
        this.subformComponent.field.defaultDepartment = value.prepareData
      } else if (value.prepareKey === 'subformChooseRangePersonnel' + this.subformComponent.name || value.prepareKey === 'subformChooseRangeDepartment' + this.subformComponent.name) {
        this.subformComponent.field.chooseRange = value.prepareData
      }
    })
    // 获取公式
    this.$bus.on('setFormula', (value) => {
      if (value.name === this.subformComponent.name) {
        this.subformComponent.field.formulaEn = value.en
        this.subformComponent.field.formulaCh = value.ch
        this.subformComponent.field.belongBean = value.belongBean
        this.subformComponent.field.referenceField = value.referenceField
      }
    })
    // 获取自动编号规则
    this.$bus.on('setIdentifer', (role, name) => {
      if (name === this.subformComponent.name) {
        this.subformComponent.numbering = role
      }
    })
    // 获取高级条件
    this.$bus.on('setHighCondition', (value) => {
      if (value.name === this.subformComponent.name) {
        this.subformComponent.relevanceWhere = value.relevanceWhere
        this.subformComponent.seniorWhere = value.seniorWhere
      }
    })
  },
  computed: {
    // 子表单拖拽属性
    dragOption () {
      return {
        animation: 100,
        group: { name: 'property', pull: true, put: true },
        sort: true,
        ghostClass: 'ghost'
      }
    },
    // 关联关系搜索字段
    searchFieldList () {
      let list = []
      this.fieldList.map((item) => {
        list.push({fieldLabel: item.label, fieldName: item.value})
      })
      return list
    },
    // 编号结果
    numberingRes () {
      return (
        this.subformComponent.numbering.fixedValue +
        this.subformComponent.numbering.dateValue +
        this.subformComponent.numbering.serialValue
      )
    },
    // 默认选择类型
    defaultChooseType: {
      get: function () {
        let label = this.subformComponent.field.chooseType === '0' ? '单选' : '多选'
        return label
      },
      set: function (newValue) {
      }
    },
    // 人员默认值
    defaultMemberValue () {
      let member = []
      this.subformComponent.field.defaultPersonnel.map((item, index) => {
        member.push(item.name)
      })
      return member.toString()
    },
    // 部门默认值
    defaultDepartmentValue () {
      let depart = []
      this.subformComponent.field.defaultDepartment.map((item, index) => {
        depart.push(item.name)
      })
      console.log(depart.toString())
      return depart.toString()
    },
    // 控制默认值是否展示
    isShowDefault () {
      let isShow
      if (
        this.subformComponent.type === 'picture' ||
        this.subformComponent.type === 'datetime' ||
        this.subformComponent.type === 'multi' ||
        this.subformComponent.type === 'formula' ||
        this.subformComponent.type === 'reference' ||
        this.subformComponent.type === 'location' ||
        this.subformComponent.type === 'personnel' ||
        this.subformComponent.type === 'department' ||
        this.subformComponent.type === 'email' ||
        this.subformComponent.type === 'number' ||
        this.subformComponent.type === 'attachment' ||
        this.subformComponent.type === 'phone' ||
        this.subformComponent.type === 'area' ||
        this.subformComponent.type === 'mutlipicklist' ||
        this.subformComponent.type === 'subform' ||
        this.subformComponent.type === 'functionformula' ||
        this.subformComponent.type === 'seniorformula' ||
        this.subformComponent.type === 'identifier' ||
        this.subformComponent.type === 'picklist'
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
        this.subformComponent.type === 'text' ||
        this.subformComponent.type === 'textarea' ||
        this.subformComponent.type === 'phone' ||
        this.subformComponent.type === 'email' ||
        this.subformComponent.type === 'location' ||
        this.subformComponent.type === 'reference' ||
        this.subformComponent.type === 'number'
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
      switch (this.subformComponent.type) {
        case 'picklist': case 'multi': case 'personnel': case 'department':
          isShow = true
          break
        default:
          isShow = false
          break
      }
      return isShow && this.subformComponent.name !== 'personnel_principal'
    },
    // 是否显示字段控制
    isShowfieldControl () {
      let isShow
      switch (this.subformComponent.type) {
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
      switch (this.subformComponent.type) {
        case 'identifier': case 'formula': case 'functionformula': case 'seniorformula':
          isShow = false
          break
        default:
          isShow = true
          break
      }
      return isShow
    },
    // 下拉选项默认值数组
    selectDefault () {
      let list = JSON.parse(JSON.stringify(this.subformComponent.entrys))
      return list
    },
    // 下拉选项默认值
    defaultEntrysValue: {
      get: function () {
        if (this.subformComponent.field.defaultEntrys.length > 0) {
          let obj = this.subformComponent.field.defaultEntrys[0]
          return obj
        } else {
          return {}
        }
      },
      set: function (newValue) {
      }
    },
    // 公式
    isFormula () {
      let isShow
      switch (this.subformComponent.type) {
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
        if (this.subformComponent.field.phoneType === '0') {
          text = '电话'
        } else if (this.subformComponent.field.phoneType === '1') {
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
        if (this.subformComponent.field.phoneLenth === '0') {
          text = '不限'
        } else if (this.subformComponent.field.phoneLenth === '1') {
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
        if (this.subformComponent.field.defaultValueId === '0') {
          text = '-无-'
        } else if (this.subformComponent.field.defaultValueId === '1') {
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
        if (this.subformComponent.field.numberType === '0') {
          text = '数字'
        } else if (this.subformComponent.field.numberType === '1') {
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
        if (this.subformComponent.field.numberType === '0') {
          text = '数字'
        } else if (this.subformComponent.field.numberType === '1') {
          text = '整数'
        } else if (this.subformComponent.field.numberType === '2') {
          text = '百分比'
        } else if (this.subformComponent.field.numberType === '3') {
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
        this.subformComponent.field.chooseRange.map((item, index) => {
          list.push(item.name)
        })
        return list.toString()
      },
      set: function (newValue) {
      }

    },
    // 关联模块model
    referenceModuleModel: {
      get: function () {
        let obj = {}
        this.moduleList.map((item) => {
          if (item.english_name === this.subformComponent.relevanceModule.moduleName) {
            obj = item
          }
        })
        return obj
      },
      set: function (newValue) {
      }
    },
    // 关联字段model
    referenceFieldModel: {
      get: function () {
        let obj = {}
        this.referenceFieldList.map((item) => {
          if (item.value === this.subformComponent.relevanceField.fieldName) {
            obj = item
          }
        })
        return obj
      },
      set: function (newValue) {
      }
    }
  },
  watch: {
    subformComponent (value) {
      console.log(this.subformComponent, '传过来的子表单')
    }
  },
  filters: {
    toText (value, type) {
      let text
      let list = []
      switch (type) {
        case 'personnel':
          if (value) {
            value.map((item) => {
              list.push(item.name)
            })
            text = list.toString()
          }
          break
        case 'picklist': case 'multi':
          if (value) {
            value.map((item) => {
              list.push(item.label)
            })
            text = list.toString()
          }
          break
        case 'datetime':
          if (value) {
            list = value.split(',')
            text = new Date(Number(list[0])).toLocaleDateString() + '到' + new Date(Number(list[1])).toLocaleDateString()
          }
          break
        default:
          text = value
          break
      }
      return text
    }
  },
  beforeDestroy () {
    // this.$bus.off('selectMemberRadio')
    // this.$bus.off('selectEmpDepRoleMulti')
    this.$bus.off('setFormula')
    this.$bus.off('setIdentifer')
    this.$bus.off('setHighCondition')
  }
}
</script>
<style lang="scss" scoped>
.defined-subform{
  width:300px;
  right:0;
  background: #ffffff;
  .setting-detail{
    overflow-x: hidden;
    overflow-y: auto;
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
</style>
<style lang="scss">
.custom-subform-wrip{
  @import '../../../../static/css/defined-property.scss';
  .setting-detail{
    width: 100%;
    >p{
      height: 50px;
      line-height: 50px;
      font-size: 16px;
      color: #212121;
      text-align:center;
      border-bottom: 1px solid #267DC5;
    }
    .subform-save {
      width: 100%;
      height: 40px;
      line-height: 40px;
      margin:0 0 20px;
      background: #267DC5;
      color: #FFFFFF;
      border-radius: 2px 2px 0 0;
      text-align: center;
      cursor: pointer;
    }
    >.el-collapse{
      height: calc(100% - 110px);
    }
  }
}
</style>
