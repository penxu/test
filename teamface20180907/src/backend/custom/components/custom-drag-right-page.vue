<template>
  <el-container class="custom-drag-right-wrip">
    <div class="module-setting" v-if="settingsType === 0">
      <p>模块设置</p>
      <el-collapse v-model="activeNames">
        <el-collapse-item name="2">
          <template slot="title">
            <i class="el-icon-caret-right"></i>详情控件
          </template>
          <div class="checkbox-box">
            <el-checkbox v-model="myModule.commentControl" true-label='1' false-label='0' disabled>评论</el-checkbox>
            <el-checkbox v-model="myModule.dynamicControl" true-label='1' false-label='0' disabled>动态</el-checkbox>
          </div>
        </el-collapse-item>
        <el-collapse-item name="3">
          <template slot="title">
            <i class="el-icon-caret-right"></i>终端
          </template>
          <div class="checkbox-box">
            <el-checkbox v-model="myModule.terminalPc" true-label='1' false-label='0' disabled>PC</el-checkbox>
            <el-checkbox v-model="myModule.terminalApp" true-label='1' false-label='0' disabled>APP</el-checkbox>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
    <div class="colum-setting" v-if="settingsType === 1">
      <p>分栏设置</p>
      <el-collapse v-model="activeNames">
        <!-- 分栏-名称 -->
        <el-collapse-item name="28">
          <template slot="title">
            <i class="el-icon-caret-right"></i>分栏名称
          </template>
          <div class="input-box">
            <el-input v-model="myColumn.title" placeholder="请输入内容..." :disabled="true"></el-input>
          </div>
        </el-collapse-item>
        <!-- 分栏-显示 -->
        <el-collapse-item name="29">
          <template slot="title">
            <i class="el-icon-caret-right"></i>显示/隐藏
          </template>
          <div class="checkbox-box">
            <el-checkbox v-model="myColumn.isSpread" true-label='1' false-label='0'>是否默认收起分栏</el-checkbox>
            <el-checkbox v-model="myColumn.isHideColumnName" true-label='1' false-label='0'>在新增／编辑页隐藏分栏名称</el-checkbox>
            <el-checkbox v-model="myColumn.isHideInCreate" true-label='1' false-label='0'>在新增／编辑页隐藏</el-checkbox>
            <el-checkbox v-model="myColumn.isHideInDetail" true-label='1' false-label='0'>在详情页隐藏</el-checkbox>
          </div>
        </el-collapse-item>
        <!-- 分栏-终端 -->
        <el-collapse-item name="30">
          <template slot="title">
            <i class="el-icon-caret-right"></i>终端
          </template>
          <div class="checkbox-box">
            <el-checkbox v-model="myColumn.terminalPc" true-label='1' false-label='0'>PC</el-checkbox>
            <el-checkbox v-model="myColumn.terminalApp" true-label='1' false-label='0'>APP</el-checkbox>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
    <div class="field-setting" v-if="settingsType === 2">
      <p>字段设置</p>
      <el-collapse v-model="activeNames">
        <!-- 字段名称 -->
        <el-collapse-item name="1">
          <template slot="title">
            <i class="el-icon-caret-right"></i>字段名称
          </template>
          <div class="input-box">
            <el-input v-model="myComponent.label" placeholder="请输入内容..." :disabled="true"></el-input>
          </div>
          <div class="input-box" v-if="isShowPointOut">
            <label for="">提示框</label>
            <el-input v-model="myComponent.field.pointOut" placeholder="请输入" :disabled="true"></el-input>
          </div>
          <div class="input-box" v-if="isShowDefault">
            <label for="">默认值</label>
            <el-input v-model="myComponent.field.defaultValue" placeholder="请输入内容..." :disabled="true"></el-input>
          </div>
        </el-collapse-item>
        <!-- 单选、多选 -->
        <el-collapse-item name="2" v-if="isShowChooseType">
          <template slot="title">
            <i class="el-icon-caret-right"></i>类型
          </template>
          <div class="input-box">
            <el-select v-model="defaultChooseType" @change="modelChange('chooseType',$event)" placeholder="请选择" disabled >
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
          <draggable class="option-box" :options="dragOption" v-model="myComponent.entrys">
            <div class="option" v-for="(select,index) in myComponent.entrys" :key="index">
              <el-input v-model="select.label" @change="clearDefault($event, 'pick')" :disabled="true"></el-input>
              <el-color-picker v-model="select.color" disabled></el-color-picker>
              <i class="el-icon-close" @click="handleDelItem(myComponent.type,index,select)"></i>
            </div>
          </draggable>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" disabled @click="handleAddItem(myComponent.type)">添加</el-button>
          </div>
          <div class="input-box">
            <label for="">默认值</label>
            <el-select v-model="defaultEntrysValue" placeholder="-无-" clearable value-key="value" @change="modelChange('picklistDefaultValue',$event)" v-show="myComponent.field.chooseType === '0'">
              <el-option v-for="(item,index) in selectDefault" :key="index" :label="item.label" :value="item">
              </el-option>
            </el-select>
            <el-select v-model="myComponent.field.defaultEntrys" placeholder="-无-" value-key="value" multiple v-show="myComponent.field.chooseType === '1'">
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
            <el-select v-model="phoneModel" placeholder="电话" disabled @change="modelChange('phoneTypeChange',$event)">
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
            <el-select v-model="numberModel" disabled @change="modelChange('numberTypeChange',$event)">
              <el-option v-for="(number,index) in numberTypes" :key="index" :label="number.label" :value="number">
              </el-option>
            </el-select>
          </div>
          <div class="input-box" v-show="myComponent.field.numberType !== '1'">
            <label for="">小数位数</label>
              <el-select placeholder="-无-" disabled v-model="myComponent.field.numberLenth">
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
            <el-select v-model="myComponent.field.formatType" placeholder="年-月-日" disabled>
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
            <el-select  v-model="myComponent.field.imageSize" placeholder="" disabled>
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
          <draggable class="option-box" :options="dragOption" v-model="myComponent.entrys">
            <div class="option" v-for="(select,index) in myComponent.entrys" :key="index">
              <el-input v-model="select.label" :disabled="true"></el-input>
              <i class="el-icon-close" @click="handleDelItem(myComponent.type,index,select)"></i>
            </div>
          </draggable>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" disabled @click="handleAddItem(myComponent.type)">添加</el-button>
          </div>
          <div class="input-box">
            <label for="">默认值</label>
            <el-select v-model="defaultEntrysValue" placeholder="-无-" clearable value-key="value" @change="modelChange('multiDefaultValue',$event)" v-show="myComponent.field.chooseType === '0'">
              <el-option v-for="(item,index) in selectDefault" :key="index" :label="item.label" :value="item">
              </el-option>
            </el-select>
            <el-select v-model="myComponent.field.defaultEntrys" placeholder="-无-" value-key="value" multiple v-show="myComponent.field.chooseType === '1'">
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
          <el-radio-group size="mini" v-model="myComponent.field.selectType" disabled>
            <el-radio-button label="0">二级下拉</el-radio-button>
            <el-radio-button label="1">三级下拉</el-radio-button>
          </el-radio-group>
          <el-tabs type="border-card" v-model="mutlipicklistActive" @tab-click="multiPickChange">
            <el-tab-pane label="一级" name="mutlipicklist1" class="option-box">
              <div class="option" v-for="(item1,index) in myComponent.entrys" :key="index">
                <el-input v-model="item1.label" @change="clearDefault($event, 'mulPick')" :disabled="true"></el-input>
                <el-color-picker v-model="item1.color" disabled></el-color-picker>
                <i class="el-icon-close" @click="handleDelItem(myComponent.type,index,item1)"></i>
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
                <el-input v-model="item2.label"  @change="clearDefault($event, 'mulPick')" :disabled="true"></el-input>
                <el-color-picker v-model="item2.color" disabled></el-color-picker>
                <i class="el-icon-close" @click="handleDelItem(myComponent.type + '2',index2,item2)"></i>
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
                <el-input v-model="item3.label"  @change="clearDefault($event, 'mulPick')" :disabled="true"></el-input>
                <el-color-picker v-model="item3.color" disabled></el-color-picker>
                <i class="el-icon-close" @click="handleDelItem(myComponent.type + '3',index3)"></i>
              </div>
            </el-tab-pane>
          </el-tabs>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" disabled @click="handleAddItem(myComponent.type)">添加选项</el-button>
          </div>
          <div class="input-box" v-show="mutlipicklistActive === 'mutlipicklist1'">
            <label for="">一级默认值</label>
            <el-select v-model="myComponent.defaultEntrys.oneDefaultValue" placeholder="-无-" clearable @change="modelChange('multiDefaultValue1',$event)">
              <el-option v-for="item1 in selectDefault" :key="item1.name" :label="item1.label" :value="item1">
              </el-option>
            </el-select>
          </div>
          <div class="input-box" v-show="mutlipicklistActive === 'mutlipicklist2'">
            <label for="">二级默认值</label>
            <el-select placeholder="-无-" v-model="myComponent.defaultEntrys.twoDefaultValue" clearable @change="modelChange('multiDefaultValue2',$event)">
              <el-option v-for="item2 in multiSelectTwoListDefault" :key="item2.name" :label="item2.label" :value="item2">
              </el-option>
            </el-select>
          </div>
          <div class="input-box" v-show="mutlipicklistActive === 'mutlipicklist3'">
            <label for="">三级默认值</label>
            <el-select placeholder="-无-" v-model="myComponent.defaultEntrys.threeDefaultValue" clearable @change="modelChange('multiDefaultValue3',$event)">
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
            <el-input v-model="defaultMemberValue" placeholder="请选择" @focus="selectMeber(myComponent.field.chooseType,'definedPersonnel')" readonly></el-input>
            <i class="el-icon-circle-close" @click="clearPeople('defaultPersonnel')"></i>
          </div>
          <div class="input-box">
            <label for="">可选范围</label>
            <el-input v-model="memberChooseRange" placeholder="请选择" @focus="selectMeber('1','chooseRangePersonnel')" readonly></el-input>
            <i class="el-icon-circle-close" @click="clearPeople('chooseRange')"></i>
          </div>
        </el-collapse-item>
        <!-- 部门 -->
        <el-collapse-item name="14" v-if="myComponent.type === 'department'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>默认值
          </template>
          <div class="input-box">
            <el-input v-model="defaultDepartmentValue" placeholder="请选择" @focus="selectMeber(myComponent.field.chooseType,'defaultDepartment')" readonly></el-input>
            <i class="el-icon-circle-close" @click="clearPeople('defaultDepartment')"></i>
          </div>
          <div class="input-box">
            <label for="">可选范围</label>
            <el-input v-model="memberChooseRange" placeholder="请选择" @focus="selectMeber('1','chooseRangeDepartment')" readonly></el-input>
            <i class="el-icon-circle-close" @click="clearPeople('chooseRange')"></i>
          </div>
        </el-collapse-item>
        <!-- 公式 -->
        <el-collapse-item name="15" v-if="isFormula">
          <template slot="title">
            <i class="el-icon-caret-right"></i>数据类型
          </template>
          <div class="formula-text">
            ={{myComponent.field.formulaCh}}
          </div>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" disabled @click="showformulaSetting(myComponent.type, myComponent.name)">设置公式</el-button>
          </div>
          <div class="input-box">
            <label for="">返回类型</label>
            <el-select placeholder="请选择类型" v-model="formulaReturnVal" disabled @change="modelChange('formulaFormat',$event)">
              <el-option v-for="item in formulaReturn" :key="item.name" :label="item.label" :value="item">
              </el-option>
            </el-select>
          </div>
          <div class="input-box" v-show="myComponent.field.numberType === '0' || myComponent.field.numberType === '2'">
            <label for="">小数位数</label>
            <el-select placeholder="-无-" v-model="myComponent.field.decimalLen" disabled>
              <el-option v-for="decimalLen in numberDecimals" :key="decimalLen.name" :label="decimalLen" :value="decimalLen">
              </el-option>
            </el-select>
          </div>
          <div class="input-box" v-show="myComponent.field.numberType === '0' || myComponent.field.numberType === '1' || myComponent.field.numberType === '2'">
            <label for="">精确度</label>
            <el-select placeholder="-无-" v-model="myComponent.field.accuracy">
              <el-option v-for="(accuracy,index) in accuracyList" :key="index" :label="accuracy.label" :value="accuracy.value">
              </el-option>
            </el-select>
          </div>
          <div class="input-box" v-show="myComponent.field.numberType === '4'">
            <label for="">类型</label>
            <el-select v-model="myComponent.field.decimalLen" placeholder="yyyy-MM-dd" disabled>
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
            <el-checkbox v-model="myComponent.field.formulaCalculates" true-label='1' false-label='0' disabled>新公式是否对旧数据重新计算</el-checkbox>
          </div>
        </el-collapse-item>
        <!-- 自动编号 -->
        <el-collapse-item name="17" v-if="myComponent.type === 'identifier'">
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
        <el-collapse-item name="18" v-if="myComponent.type === 'reference'">
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
              <el-option v-for="(field,index) in referenceFieldList" :key="index" :label="field.label" :value="field">
              </el-option>
            </el-select>
          </div>
          <div class="refe-box" v-show="searchFieldList.length > 0">
            <label for="">列表搜索字段</label>
            <el-select v-model="myComponent.searchFields" value-key="fieldName" multiple disabled>
              <el-option v-for="field in searchFieldList" :key="field.fieldName" :label="field.fieldLabel" :value="field">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 关联关系筛选条件 -->
        <el-collapse-item name="19" v-if="myComponent.type === 'reference'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>设置关联筛选条件
          </template>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" @click="editFilterCondition" disabled>添加筛选条件</el-button>
          </div>
          <div class="condition-text">
              <div class="item" v-for="(item,index) in myComponent.relevanceWhere" :key="index">
                {{index + 1}}. {{item.field_label}} <span>{{item.operator_label}}</span> {{item.result_value | toText(item.show_type)}}
              </div>
            </div>
        </el-collapse-item>
        <!-- 条形码 -->
        <el-collapse-item name="31" v-if="myComponent.type === 'barcode'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>类型
          </template>
          <div class="input-box">
            <el-select placeholder="请选择类型" v-model="myComponent.field.codeType">
              <el-option v-for="(item,index) in codeTypeList" :key="index" :label="item.label" :value="item.value">
              </el-option>
            </el-select>
          </div>
          <div class="bg-box" v-show="myComponent.field.codeType === '1'">
            <div class="img" :style="{'background-image': 'url(/static/img/custom/'+ codeStyleBg(myComponent.field.codeStyle) +')'}"></div>
          </div>
          <div class="add-btn" v-show="myComponent.field.codeType === '1'">
            <el-button plain size="mini" @click="openBarcodeStyle">更换样式</el-button>
          </div>
        </el-collapse-item>
        <!-- 子表单 -->
        <el-collapse-item name="20" v-if="myComponent.type === 'subform'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>子表单字段
          </template>
          <draggable class="subform-box" :options="dragOption" v-model="myComponent.componentList">
            <div class="item" v-for="(dragItem,index) in myComponent.componentList" :key="index" @click="toSubformSetting(dragItem, myComponent.name)">
              <span class="name">{{dragItem.label}}</span>
              <span class="bg">{{dragItem.typeText}}</span>
              <i class="el-icon-close" @click.stop="delSubformItem(index)"></i>
            </div>
          </draggable>
          <div class="input-box">
            <el-select placeholder="选择字段添加到子表单" v-model="subformModel" disabled @change="addSubformItem($event)">
              <el-option v-for="component in componentList" :key="component.name" :label="component.label" :value="component" >
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 地址位置 -->
        <el-collapse-item name="21" v-if="myComponent.type === 'location'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>地址位置
          </template>
          <div class="checkbox-box">
            <el-checkbox true-label='1' false-label='0' v-model="myComponent.field.defaultValue">默认当前位置</el-checkbox>
          </div>
        </el-collapse-item>
        <!-- 字段宽度 -->
        <el-collapse-item name="22" v-if="myComponent.type !== 'subform' && myComponent.type !== 'attachment' && myComponent.type !== 'picture'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>字段宽度
          </template>
          <el-radio-group size="mini" v-model="myComponent.width">
            <el-radio-button label="100%">显示单列</el-radio-button>
            <el-radio-button label="50%">显示双列</el-radio-button>
          </el-radio-group>
        </el-collapse-item>
        <!-- 字段结构 -->
        <el-collapse-item name="23">
          <template slot="title">
            <i class="el-icon-caret-right"></i>字段结构
          </template>
          <el-radio-group size="mini" v-model="myComponent.field.structure">
            <el-radio-button label="1">左右布局</el-radio-button>
            <el-radio-button label="0">上下布局</el-radio-button>
          </el-radio-group>
        </el-collapse-item>
        <!-- 字段控制 -->
        <el-collapse-item name="24" v-if="isShowfieldControl">
          <template slot="title">
            <i class="el-icon-caret-right"></i>字段控制
          </template>
          <el-radio-group size="mini" v-model="myComponent.field.fieldControl">
            <el-radio label='1' :disabled="myComponent.type === 'barcode'">只读</el-radio>
            <el-radio label='2'>必填</el-radio>
            <el-radio label='0'>不控制</el-radio>
          </el-radio-group>
        </el-collapse-item>
        <!-- 字段权限 -->
        <el-collapse-item name="25" v-if="isShowfieldAuth">
          <template slot="title">
            <i class="el-icon-caret-right"></i>字段权限
          </template>
          <div class="checkbox-box">
            <el-checkbox true-label='1' false-label='0' v-model="myComponent.field.addView">新增时显示</el-checkbox>
            <el-checkbox true-label='1' false-label='0' v-model="myComponent.field.editView">编辑时显示</el-checkbox>
          </div>
        </el-collapse-item>
        <!-- 查重 -->
        <el-collapse-item name="26" v-if="myComponent.field.repeatCheck">
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
        <el-collapse-item name="27">
          <template slot="title">
            <i class="el-icon-caret-right"></i>终端
          </template>
          <div class="checkbox-box">
            <el-checkbox v-model="myComponent.field.terminalPc" true-label='1' false-label='0' :disabled="myComponent.name === 'personnel_principal'">PC</el-checkbox>
            <el-checkbox v-model="myComponent.field.terminalApp" true-label='1' false-label='0' :disabled="myComponent.name === 'personnel_principal'">APP</el-checkbox>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
    <transition name="slide">
      <custom-subform-page v-if="settingsType === 3" :subformComponent="subformComponent" :components="allField" :subformName="subformName"></custom-subform-page>
    </transition>
  </el-container>
</template>
<script>
import { mapState, mapGetters, mapMutations } from 'vuex'
import {subformList, barcodeStyleList, formulaReturn1, formulaReturn2} from '@/common/js/constant.js'
import {HTTPServer} from '@/common/js/ajax.js'
import draggable from 'vuedraggable'
import CustomSubformPage from '@/backend/custom/components/custom-subform-page'
export default {
  name: 'CustomDragRightPage',
  components: {
    draggable,
    CustomSubformPage
  },
  props: {
    pageNum: {
      type: Number,
      default: 0
    }
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
        '21', '22', '23', '24', '25', '26', '27', '28', '29', '30', '31'
      ],
      dateList: ['yyyy', 'yyyy-MM', 'yyyy-MM-dd', 'yyyy-MM-dd HH', 'yyyy-MM-dd HH:mm', 'yyyy-MM-dd HH:mm:ss'],
      dateTimeOptions: [{ value: '0', label: '-无-' }, { value: '1', label: '当前时间' }, { value: '2', label: '指定时间' }],
      imageSizeList: ['30px*30px', '60px*60px'], // 图片大小
      chooseTypeList: [{ value: '0', label: '单选' }, { value: '1', label: '多选' }], // 复选框组件类型
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
      phoneType: [{label: '电话', value: '0'}, {label: '手机', value: '1'}],
      phoneLengths: [{ value: '0', label: '不限' }, {value: '1', label: '11'}],
      numberTypes: [ // 数字类型
        {value: '0', label: '数字'},
        {value: '1', label: '整数'},
        {value: '2', label: '百分比'}
      ],
      numberDecimals: ['1', '2', '3', '4'], // 数字小数位数
      accuracyList: [{ value: '0', label: '四舍五入' }, {value: '1', label: '去尾法'}, {value: '2', label: '进一法'}], // 精确度
      codeTypeList: [{ value: '0', label: '国际编码' }, {value: '1', label: '自定义编码'}],
      componentList: subformList, // 子表单选择列表
      formulaItems: [],
      subformModel: '',
      subformName: '',
      subformComponent: {},
      allField: []
    }
  },
  created () {
    let myModule = this.custom_layout
    this.myModule = JSON.parse(JSON.stringify(myModule))
  },
  methods: {
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
          if (data.value) {
            this.myComponent.field.defaultEntrys = [data]
          } else {
            this.myComponent.field.defaultEntrys = []
          }
          console.log(this.myComponent, '修改后')
          break
        case 'multiDefaultValue':
          if (data.value) {
            this.myComponent.field.defaultEntrys = [data]
          } else {
            this.myComponent.field.defaultEntrys = []
          }
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
        case 'formulaFormat':
          this.myComponent.field.numberType = data.value
          if (data.value === '4') {
            this.myComponent.field.decimalLen = 'yyyy-MM-dd'
          } else {
            this.myComponent.field.decimalLen = '2'
            if (data.value === '1') {
              this.myComponent.field.decimalLen = '0'
            }
          }
          console.log(this.myComponent, '修改后')
          break
        case 'multiSelectOne':
          this.multiSelect2_model = ''
          this.multiSelectTwoList = data.subList
          this.multiSelectThreeList = []
          console.log(this.myComponent, '修改后')
          break
        case 'multiSelectTwo':
          this.multiSelectThreeList = data.subList
          console.log(this.myComponent, '修改后')
          break
        case 'multiDefaultValue1':
          this.multiSelectTwoListDefault = data.subList
          if (data) {
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
          if (data) {
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
          if (data) {
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
    // 多级下拉变化
    multiPickChange (data) {
      let list = JSON.parse(JSON.stringify(this.myComponent.entrys))
      let id = this.myComponent.defaultEntrys.oneDefaultValueId
      list.map((item) => {
        if (item.value === id) {
          this.multiSelectTwoListDefault = item.subList
        }
      })
      if (data.name === 'mutlipicklist3') {
        let id2 = this.myComponent.defaultEntrys.twoDefaultValueId
        this.multiSelectTwoListDefault.map((item) => {
          if (item.value === id2) {
            this.multiSelectThreeListDefault = item.subList
          }
        })
      }
    },
    // 公式弹出框
    showformulaSetting (type, name) {
      let data = {
        id: name,
        isOpen: true,
        type: type,
        name: this.myComponent.label,
        textarea: this.myComponent.field.formulaCh,
        chinese: this.myComponent.field.formulaCh,
        english: this.myComponent.field.formulaEn,
        fieldList: this.formulaItems
      }
      this.$bus.emit('openFormula', data)
    },
    // 删除人员默认值及范围
    clearPeople (name) {
      this.myComponent.field[name] = []
    },
    // 设置高级筛选条件
    editFilterCondition () {
      let bean = this.myComponent.relevanceModule.moduleName
      let value = {
        bean: bean,
        name: this.myComponent.name,
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
        name: this.myComponent.name,
        fixedValue: this.myComponent.numbering.fixedValue,
        dateValue: this.myComponent.numbering.dateValue,
        serialValue: this.myComponent.numbering.serialValue
      }
      this.$bus.emit('openIdentifer', value)
    },
    // 打开编码样式弹框
    openBarcodeStyle () {
      let value = {
        isOpen: true,
        name: this.myComponent.name,
        code: this.myComponent.field.codeStyle
      }
      this.$bus.emit('openBarcodeStyle', value)
    },
    // 选择人员
    selectMeber (type, key) {
      let navKey, list
      let types = type === '0' ? 1 : ''
      if (key === 'definedPersonnel') {
        navKey = type === '0' ? '1,3' : '1'
        list = this.myComponent.field.defaultPersonnel
      } else if (key === 'defaultDepartment') {
        navKey = type === '0' ? '0,3' : '0,3'
        list = this.myComponent.field.defaultDepartment
      } else if (key === 'chooseRangePersonnel') {
        navKey = type === '0' ? '1,3' : '1,0'
        list = this.myComponent.field.chooseRange
      } else if (key === 'chooseRangeDepartment') {
        navKey = type === '0' ? '0,3' : '0'
        list = this.myComponent.field.chooseRange
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
      let valueList = []
      this.myComponent.entrys.map((item) => {
        valueList.push(item.value)
      })
      let max = Math.max.apply(Math, valueList)
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
          let item = {
            label: '新选项',
            value: String(max + 1)
          }
          this.myComponent.entrys.push(item)
          console.log(this.myComponent, '修改后')
          break
        case 'mutlipicklist':
          if (this.mutlipicklistActive === 'mutlipicklist1') {
            // 一级下拉添加选项
            let len1 = this.myComponent.entrys.length
            let item1
            if (len1 === 0) {
              item1 = {
                label: '一级新选项',
                value: '0',
                color: '#FFFFFF',
                subList: []
              }
            } else {
              let lastItemValue1 = parseInt(this.myComponent.entrys[len1 - 1].value)
              item1 = {
                label: '一级新选项',
                value: String(lastItemValue1 + 1),
                color: '#FFFFFF',
                subList: []
              }
            }
            this.myComponent.entrys.push(item1)
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
          let lens = this.myComponent.entrys.length
          if (lens === 0) {
            this.myComponent.entrys.push({
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
          this.myComponent.entrys.push(items)
          break
        default:
          break
      }
    },
    // 删除选项
    handleDelItem (property, index, select) {
      switch (property) {
        case 'multi': case 'picklist':
          this.myComponent.entrys.splice(index, 1)
          this.myComponent.field.defaultEntrys.map((item, index2) => {
            if (JSON.stringify(select) === JSON.stringify(item)) {
              this.myComponent.field.defaultEntrys.splice(index2, 1)
            }
          })
          break
        case 'mutlipicklist':
          this.myComponent.entrys.splice(index, 1)
          if (select.value === this.myComponent.defaultEntrys.oneDefaultValueId) {
            this.myComponent.defaultEntrys.oneDefaultValue = ''
            this.myComponent.defaultEntrys.oneDefaultValueId = ''
            this.myComponent.defaultEntrys.oneDefaultValueColor = ''
            this.myComponent.defaultEntrys.twoDefaultValue = ''
            this.myComponent.defaultEntrys.twoDefaultValueId = ''
            this.myComponent.defaultEntrys.twoDefaultValueColor = ''
            this.myComponent.defaultEntrys.threeDefaultValue = ''
            this.myComponent.defaultEntrys.threeDefaultValueId = ''
            this.myComponent.defaultEntrys.threeDefaultValueColor = ''
          }
          break
        case 'mutlipicklist2':
          this.multiSelectTwoList.splice(index, 1)
          if (select.value === this.myComponent.defaultEntrys.twoDefaultValueId) {
            this.myComponent.defaultEntrys.twoDefaultValue = ''
            this.myComponent.defaultEntrys.twoDefaultValueId = ''
            this.myComponent.defaultEntrys.twoDefaultValueColor = ''
            this.myComponent.defaultEntrys.threeDefaultValue = ''
            this.myComponent.defaultEntrys.threeDefaultValueId = ''
            this.myComponent.defaultEntrys.threeDefaultValueColor = ''
          }
          break
        case 'mutlipicklist3':
          this.multiSelectThreeList.splice(index, 1)
          if (select.value === this.myComponent.defaultEntrys.threeDefaultValueId) {
            this.myComponent.defaultEntrys.threeDefaultValue = ''
            this.myComponent.defaultEntrys.threeDefaultValueId = ''
            this.myComponent.defaultEntrys.threeDefaultValueColor = ''
          }
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
      this.$confirm('确定删除自定义字段(不可恢复)？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.myComponent.componentList.splice(index, 1)
      }).catch(() => {
      })
    },
    // 子表单设置是否显示
    toSubformSetting (property, name) {
      this.settingsType = 3
      this.subformComponent = property
      this.subformName = name
      console.log('子表单字段', property)
    },
    // 获取二维码样式图片
    codeStyleBg (style) {
      let bgImg = 'CODE39.png'
      barcodeStyleList.map((item) => {
        if (item.code === style) {
          bgImg = item.bgImg
        }
      })
      return bgImg
    },
    // 获取关联模块列表
    ajaxGetModuleList () {
      HTTPServer.getAppAllModuleList(null, 'Loading').then((res) => {
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
    },
    ...mapMutations({
      setLayout: 'CUSTOM_LAYOUT'
    })
  },
  mounted () {
    // 设置属性
    this.$bus.on('sendProperty', (value, allValue) => {
      this.allField = allValue
      this.settingsType = 2
      console.log(value, '传递')
      this.myComponent = value
      if (value.type === 'reference') {
        this.ajaxGetModuleList()
        if (value.relevanceModule.moduleName) {
          let bean = {bean: value.relevanceModule.moduleName}
          this.ajaxGetFieldList(bean)
        }
      } else if (value.type === 'formula' || value.type === 'functionformula' || value.type === 'seniorformula') {
        // 处理公式列表
        console.log(allValue, '公式传递')
        this.formulaReturn = value.type === 'formula' ? formulaReturn1 : formulaReturn2
        this.formulaItems = []
        if (allValue) {
          allValue.map((list, index) => {
            list.rows.map((item, index) => {
              let subform = item.componentList ? item.componentList : []
              this.formulaItems.push(
                {
                  bean: this.$route.query.bean,
                  label: item.label,
                  value: item.name,
                  type: item.type,
                  subform: subform
                })
            })
          })
        }
      }
    })
    // 设置模块名称
    this.$bus.on('sendModuleName', value => {
      this.settingsType = 0
      this.myModule = value
    })
    // 设置分栏
    this.$bus.on('sendColumn', value => {
      this.myColumn = value
      this.settingsType = 1
    })
    // 获取人员单选数据
    this.$bus.on('selectMemberRadio', (value) => {
      if (value.prepareKey === 'definedPersonnel') {
        this.myComponent.field.defaultPersonnel = value.prepareData
      } else if (value.prepareKey === 'defaultDepartment') {
        this.myComponent.field.defaultDepartment = value.prepareData
      }
    })
    // 获取人员多选数据
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value.prepareKey === 'definedPersonnel') {
        this.myComponent.field.defaultPersonnel = value.prepareData
      } else if (value.prepareKey === 'defaultDepartment') {
        this.myComponent.field.defaultDepartment = value.prepareData
      } else if (value.prepareKey === 'chooseRangePersonnel' || value.prepareKey === 'chooseRangeDepartment') {
        this.myComponent.field.chooseRange = value.prepareData
      }
    })
    // 获取部门单选数据
    this.$bus.on('selectDepartmentRadio', (value) => {
      if (value.prepareKey === 'definedPersonnel') {
        this.myComponent.field.defaultPersonnel = value.prepareData
      } else if (value.prepareKey === 'defaultDepartment') {
        this.myComponent.field.defaultDepartment = value.prepareData
      }
    })
    // 获取部门多选数据
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value.prepareKey === 'definedPersonnel') {
        this.myComponent.field.defaultPersonnel = value.prepareData
      } else if (value.prepareKey === 'defaultDepartment') {
        this.myComponent.field.defaultDepartment = value.prepareData
      } else if (value.prepareKey === 'chooseRangePersonnel' || value.prepareKey === 'chooseRangeDepartment') {
        this.myComponent.field.chooseRange = value.prepareData
      }
    })
    // 关闭子表单
    this.$bus.on('send_subformVisbale', (value) => {
      this.settingsType = 2
    })
    // 获取公式
    this.$bus.on('setFormula', (value) => {
      if (value.name === this.myComponent.name) {
        this.myComponent.field.formulaEn = value.en
        this.myComponent.field.formulaCh = value.ch
        this.myComponent.field.belongBean = value.belongBean
        this.myComponent.field.referenceField = value.referenceField
      }
    })
    // 获取自动编号规则
    this.$bus.on('setIdentifer', (role, name) => {
      if (name === this.myComponent.name) {
        this.myComponent.numbering = role
      }
    })
    // 获取高级条件
    this.$bus.on('setHighCondition', (value) => {
      if (value.name === this.myComponent.name) {
        this.myComponent.relevanceWhere = value.relevanceWhere
        this.myComponent.seniorWhere = value.seniorWhere
      }
    })
    // 获取条形码样式
    this.$bus.on('setBarcodeStyle', (value) => {
      if (value.name === this.myComponent.name) {
        this.myComponent.field.codeStyle = value.code
      }
    })
  },
  computed: {
    ...mapGetters([
      'custom_layout'
    ]),
    ...mapState({
      custom_layout: state => state.custom.custom_layout
    }),
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
    // 部门默认值
    defaultDepartmentValue () {
      let depart = []
      this.myComponent.field.defaultDepartment.map((item, index) => {
        depart.push(item.name)
      })
      console.log(depart.toString())
      return depart.toString()
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
        this.myComponent.type === 'department' ||
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
        this.myComponent.type === 'picklist' ||
        this.myComponent.type === 'barcode'
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
        this.myComponent.type === 'number' ||
        this.myComponent.type === 'barcode'
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
        case 'picklist': case 'multi': case 'personnel': case 'department':
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
    // 下拉选项默认值数组
    selectDefault () {
      let list = JSON.parse(JSON.stringify(this.myComponent.entrys))
      return list
    },
    // 下拉选项默认值
    defaultEntrysValue: {
      get: function () {
        if (this.myComponent.field.defaultEntrys.length > 0) {
          let obj = this.myComponent.field.defaultEntrys[0]
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
    // 关联模块model
    referenceModuleModel: {
      get: function () {
        let obj = {}
        this.moduleList.map((item) => {
          if (item.english_name === this.myComponent.relevanceModule.moduleName) {
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
          if (item.value === this.myComponent.relevanceField.fieldName) {
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
    myModule: {
      deep: true,
      handler: function (newVal, oldval) {
        this.$bus.emit('modulesSetting', newVal)
        // let allLayout = JSON.parse(JSON.stringify(this.myModule))
        // this.setLayout(allLayout)
        console.log(newVal, '传过来的模块')
      }
    },
    myComponent (value) {
      console.log(this.myComponent, '传过来的数据')
    },
    myColumn () {
      console.log(this.myColumn, '传过来的分栏数据')
    },
    pageNum (newVal) {
      console.log(newVal, '子页面切换')
      this.settingsType = 0
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
    this.$bus.off('ajaxSendModule')
    this.$bus.off('sendPropertysendProperty')
    this.$bus.off('sendModuleName')
    this.$bus.off('sendColumn')
    this.$bus.off('send_subformVisbale')
    this.$bus.off('setFormula')
    this.$bus.off('setIdentifer')
    this.$bus.off('setHighCondition')
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
  flex: 0 0 300px !important;
  background: #FFFFFF;
  text-align: left;
  border-top: 2px solid #e7e7e7;
  @import '../../../../static/css/defined-property.scss';
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
