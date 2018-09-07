/**
 * 组件中的常数可以写在这个文件中
 */

// 基础字段
const component1 = [
  {
    label: '单行文本',
    icon: 'icon-danhangwenben',
    type: 'text',
    isDrag: true,
    typeText: '单行文本',
    field: {}
  },
  {
    label: '多行文本',
    icon: 'icon-duohangwenben',
    type: 'textarea',
    isDrag: true,
    typeText: '多行文本',
    field: {}
  },
  {
    label: '富文本',
    icon: 'icon-fuwenben',
    type: 'multitext',
    isDrag: true,
    typeText: '富文本',
    field: {}
  },
  {
    label: '下拉选项',
    icon: 'icon-xialaxuanxiang',
    type: 'picklist',
    isDrag: true,
    typeText: '下拉选项',
    field: {}
  },
  {
    label: '电话',
    icon: 'icon-dianhua',
    type: 'phone',
    isDrag: true,
    typeText: '电话',
    field: {}
  },
  {
    label: '电子邮箱',
    icon: 'icon-youxiang',
    type: 'email',
    isDrag: true,
    typeText: '电子邮箱',
    field: {}
  },
  {
    label: '数字',
    icon: 'icon-shuzi',
    type: 'number',
    isDrag: true,
    typeText: '数字',
    field: {}
  },
  {
    label: '日期/时间',
    icon: 'icon-riqishijian',
    type: 'datetime',
    isDrag: true,
    typeText: '日期/时间',
    field: {}
  },
  {
    label: '附件',
    icon: 'icon-fujian',
    type: 'attachment',
    isDrag: true,
    typeText: '附件',
    field: {}
  },
  {
    label: '超链接',
    icon: 'icon-chaolianjie',
    type: 'url',
    isDrag: true,
    typeText: '超链接',
    field: {}
  },
  {
    label: '定位',
    icon: 'icon-dingwei',
    type: 'location',
    isDrag: true,
    typeText: '定位',
    field: {}
  },
  {
    label: '图片',
    icon: 'icon-tupian',
    type: 'picture',
    isDrag: true,
    typeText: '图片',
    field: {}
  },
  {
    label: '复选框',
    icon: 'icon-fuxuankuang',
    type: 'multi',
    isDrag: true,
    typeText: '复选框',
    field: {}
  },
  {
    label: '人员',
    icon: 'icon-renyuan',
    type: 'personnel',
    isDrag: true,
    typeText: '人员',
    field: {}
  },
  {
    label: '部门',
    icon: 'icon-bumen',
    type: 'department',
    isDrag: true,
    typeText: '部门',
    field: {}
  },
  {
    label: '多级下拉',
    icon: 'icon-duojixiala1',
    type: 'mutlipicklist',
    isDrag: true,
    typeText: '多级下拉',
    field: {}
  },
  {
    label: '简单公式',
    icon: 'icon-jiandangongshi1',
    type: 'formula',
    isDrag: true,
    typeText: '简单公式',
    field: {}
  },
  {
    label: '省市区',
    icon: 'icon-shengshiqu',
    type: 'area',
    isDrag: true,
    typeText: '省市区',
    field: {}
  }
]
// 高级类组件
const component2 = [
  {
    label: '自动编号',
    icon: 'icon-zidongbianhao',
    type: 'identifier',
    isDrag: true,
    typeText: '自动编号',
    field: {}
  },
  {
    label: '关联关系',
    icon: 'icon-guanlianguanxi',
    type: 'reference',
    isDrag: true,
    typeText: '关联关系',
    field: {}
  },
  {
    label: '子表单',
    icon: 'icon-zibiaodan',
    type: 'subform',
    isDrag: true,
    typeText: '子表单',
    field: {}
  },
  {
    label: '函数公式',
    icon: 'icon-hanshugongshi',
    type: 'functionformula',
    isDrag: true,
    typeText: '函数公式',
    field: {}
  },
  {
    label: '高级公式',
    icon: 'icon-gaojigongshi',
    type: 'seniorformula',
    isDrag: true,
    typeText: '高级公式',
    field: {}
  },
  {
    label: '条码',
    icon: 'icon-zujiantiaoma',
    type: 'barcode',
    isDrag: true,
    typeText: '条码',
    field: {}
  }
]
// 子表单组件
const subformList = [
  {
    'name': 'subform_text_',
    'label': '单行文本',
    'type': 'text',
    'typeText': '单行文本',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'pointOut': '',
      'repeatCheck': '0',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultValue': ''
    }
  },
  {
    'name': 'subform_textarea_',
    'label': '多行文本',
    'type': 'textarea',
    'typeText': '多行文本',
    'width': '100%',
    'state': '0',
    'remove': '1',
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'pointOut': '',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultValue': ''
    }
  },
  {
    'name': 'subform_multitext_',
    'label': '富文本',
    'type': 'multitext',
    'typeText': '富文本',
    'width': '100%',
    'state': '0',
    'remove': '1',
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'pointOut': '',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultValue': ''
    }
  },
  {
    'name': 'subform_picklist_',
    'label': '下拉选项',
    'type': 'picklist',
    'typeText': '下拉选项',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'chooseType': '0',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultEntrys': []
    },
    'entrys': [
      {
        'value': '0',
        'label': '选项1',
        'color': '#FFFFFF'
      },
      {
        'value': '1',
        'label': '选项2',
        'color': '#FFFFFF'
      },
      {
        'value': '2',
        'label': '选项3',
        'color': '#FFFFFF'
      }
    ]
  },
  {
    'name': 'subform_phone_',
    'label': '电话',
    'type': 'phone',
    'typeText': '电话',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'pointOut': '',
      'phoneType': '0',
      'phoneLenth': '0',
      'repeatCheck': '0',
      'terminalPc': '1',
      'terminalApp': '1'
    }
  },
  {
    'name': 'subform_email_',
    'label': '电子邮箱',
    'type': 'email',
    'typeText': '电子邮箱',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'field': {
      'pointOut': '',
      'structure': '1',
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'repeatCheck': '0',
      'terminalPc': '1',
      'terminalApp': '1'
    }
  },
  {
    'name': 'subform_number_',
    'label': '数字',
    'type': 'number',
    'typeText': '数字',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'pointOut': '',
      'numberType': '0',
      'numberLenth': '2',
      'betweenMin': '',
      'betweenMax': '',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultValue': ''
    }
  },
  {
    'name': 'subform_datetime_',
    'label': '日期/时间',
    'type': 'datetime',
    'typeText': '日期/时间',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'field': {
      'formatType': 'yyyy-MM-dd',
      'defaultValueId': '0',
      'defaultValue': '',
      'structure': '1',
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'terminalPc': '1',
      'terminalApp': '1'
    }
  },
  {
    'name': 'subform_attachment_',
    'label': '附件',
    'type': 'attachment',
    'typeText': '附件',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'countLimit': '0',
      'maxCount': '',
      'maxSize': '',
      'terminalPc': '1',
      'terminalApp': '1'
    },
    'entrys': []
  },
  {
    'name': 'subform_url_',
    'label': '超链接',
    'type': 'url',
    'typeText': '超链接',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'field': {
      'defaultValue': '',
      'structure': '1',
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'terminalPc': '1',
      'terminalApp': '1'
    }
  },
  {
    'name': 'subform_location_',
    'label': '定位',
    'type': 'location',
    'typeText': '定位',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'pointOut': '',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultValue': '1'
    }
  },
  {
    'name': 'subform_picture_',
    'label': '图片',
    'type': 'picture',
    'typeText': '图片',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'field': {
      'structure': '1',
      'imageSize': '30px*30px',
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'terminalPc': '1',
      'terminalApp': '1',
      'countLimit': '0',
      'maxCount': '',
      'maxSize': ''
    },
    'entrys': []
  },
  {
    'name': 'subform_multi_',
    'label': '复选框',
    'type': 'multi',
    'typeText': '复选框',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'chooseType': '0',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultEntrys': []
    },
    'entrys': [
      {
        'value': '0',
        'label': '选项1'
      },
      {
        'value': '1',
        'label': '选项2'
      }
    ]
  },
  {
    'name': 'subform_mutlipicklist_',
    'label': '多级下拉',
    'type': 'mutlipicklist',
    'typeText': '多级下拉',
    'width': '100%',
    'state': '0',
    'remove': '1',
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'selectType': '0',
      'terminalPc': '1',
      'terminalApp': '1'
    },
    'defaultEntrys': {
      'oneDefaultValue': '', // 一级下拉选项默认值
      'oneDefaultValueId': '', // 一级下拉选项默认值ID
      'oneDefaultValueColor': '', // 一级下拉选项默认值颜色
      'twoDefaultValue': '', // 二级下拉选默认值
      'twoDefaultValueId': '', // 二级下拉选默认值ID
      'twoDefaultValueColor': '', // 二级下拉选默认值颜色
      'threeDefaultValue': '', // 三级下拉选项默认值
      'threeDefaultValueId': '', // 三级下拉选项默认值ID
      'threeDefaultValueColor': '' // 三级下拉选项默认值颜色
    },
    'entrys': [
      {
        'value': '0', // 一级下拉选项key
        'label': '一级选项1', // 一级下拉选项val
        'color': '#FFFFFF', // 一级下拉选项颜色
        'subList': [
          // 二级下拉选项值
          {
            'value': '0', // 二级下拉选项key
            'label': '二级选项1', // 二级下拉选项val
            'color': '#FFFFFF', // 二级下拉选项颜色
            'subList': [
              // 三级下拉选项值
              {
                'value': '0', // 三级级下拉选项key
                'label': '三级选项1', // 三级级下拉选项val
                'color': '#FFFFFF' // 三级级下拉选项颜色
              },
              {
                'value': '1', // 三级级下拉选项key
                'label': '三级选项2', // 三级级下拉选项val
                'color': '#FFFFFF' // 三级级下拉选项颜色
              }
            ]
          },
          {
            'value': '1', // 二级下拉选项key
            'label': '二级选项2', // 二级下拉选项val
            'color': '#FFFFFF', // 二级下拉选项颜色
            'subList': [
              // 三级下拉选项值
              {
                'value': '0', // 三级级下拉选项key
                'label': '三级选项1', // 三级级下拉选项val
                'color': '#FFFFFF' // 三级级下拉选项颜色
              },
              {
                'value': '1', // 三级级下拉选项key
                'label': '三级选项2', // 三级级下拉选项val
                'color': '#FFFFFF' // 三级级下拉选项颜色
              }
            ]
          }
        ]
      },
      {
        'value': '1', // 一级下拉选项key
        'label': '一级选项2', // 一级下拉选项val
        'color': '#FFFFFF', // 一级下拉选项颜色
        'subList': [
          // 二级下拉选项值
          {
            'value': '0', // 二级下拉选项key
            'label': '二级选项1', // 二级下拉选项val
            'color': '#FFFFFF', // 二级下拉选项颜色
            'subList': [
              // 三级下拉选项值
              {
                'value': '0', // 三级级下拉选项key
                'label': '三级选项1', // 三级级下拉选项val
                'color': '#FFFFFF' // 三级级下拉选项颜色
              },
              {
                'value': '1', // 三级级下拉选项key
                'label': '三级选项2', // 三级级下拉选项val
                'color': '#FFFFFF' // 三级级下拉选项颜色
              }
            ]
          },
          {
            'value': '1', // 二级下拉选项key
            'label': '二级选项2', // 二级下拉选项val
            'color': '#FFFFFF', // 二级下拉选项颜色
            'subList': [
              // 三级下拉选项值
              {
                'value': '0', // 三级级下拉选项key
                'label': '三级选项1', // 三级级下拉选项val
                'color': '#FFFFFF' // 三级级下拉选项颜色
              },
              {
                'value': '1', // 三级级下拉选项key
                'label': '三级选项2', // 三级级下拉选项val
                'color': '#FFFFFF' // 三级级下拉选项颜色
              }
            ]
          }
        ]
      }
    ]
  },
  {
    'name': 'subform_personnel_',
    'label': '人员',
    'type': 'personnel',
    'typeText': '人员',
    'width': '50%',
    'state': '0',
    'remove': '0',
    'field': {
      'chooseType': '0',
      'defaultPersonnel': [],
      'chooseRange': [],
      'structure': '1',
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'terminalPc': '1',
      'terminalApp': '1'
    }
  },
  {
    'name': 'subform_department_',
    'label': '部门',
    'type': 'department',
    'typeText': '部门',
    'width': '50%',
    'state': '0',
    'remove': '0',
    'field': {
      'chooseType': '0',
      'defaultDepartment': [],
      'chooseRange': [],
      'structure': '1',
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'terminalPc': '1',
      'terminalApp': '1'
    }
  },
  {
    'name': 'subform_area_',
    'label': '省市区',
    'type': 'area',
    'typeText': '省市区',
    'width': '100%',
    'state': '0',
    'remove': '0',
    'field': {
      'structure': '1',
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'terminalPc': '1',
      'terminalApp': '1'
    }
  },
  // {
  //   'name': 'subform_identifier_',
  //   'label': '自动编号',
  //   'type': 'identifier',
  //   'typeText': '自动编号',
  //   'width': '100%',
  //   'state': '0',
  //   'remove': '0',
  //   'field': {
  //     'structure': '1',
  //     'fieldControl': '0',
  //     'addView': '0',
  //     'editView': '0',
  //     'terminalPc': '1',
  //     'terminalApp': '1',
  //     'defaultValue': ''
  //   },
  //   'numbering': {
  //     'dateValue': '',
  //     'fixedValue': '',
  //     'serialValue': ''
  //   }
  // },
  {
    'name': 'subform_reference_',
    'label': '关联关系',
    'type': 'reference',
    'typeText': '关联关系',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'pointOut': '',
      'structure': '1',
      'terminalPc': '1',
      'terminalApp': '1'
    },
    'relevanceModule': {
      'moduleLabel': '',
      'moduleName': ''
    },
    'relevanceField': {
      'fieldLabel': '',
      'fieldName': ''
    },
    'searchFields': [],
    'relevanceWhere': [],
    'seniorWhere': ''
  },
  {
    'name': 'subform_formula_',
    'label': '简单公式',
    'type': 'formula',
    'typeText': '简单公式',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'field': {
      'structure': '1',
      'fieldControl': '1',
      'addView': '0',
      'editView': '0',
      'formulaEn': '',
      'formulaCh': '',
      'numberType': '0',
      'decimalLen': '2',
      'accuracy': '0',
      'formulaCalculates': '0',
      'terminalPc': '1',
      'terminalApp': '1'
    }
  },
  {
    'name': 'subform_functionformula_',
    'label': '函数公式',
    'type': 'functionformula',
    'typeText': '函数公式',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'field': {
      'structure': '1',
      'fieldControl': '1',
      'addView': '0',
      'editView': '0',
      'formulaEn': '',
      'formulaCh': '',
      'numberType': '0',
      'decimalLen': '2',
      'accuracy': '0',
      'formulaCalculates': '0',
      'terminalPc': '1',
      'terminalApp': '1'
    }
  }
]
// 公式符号
const symbolList = [
  {code: '+', name: '加 +'},
  {code: '-', name: '减 -'},
  {code: '*', name: '乘 *'},
  {code: '/', name: '除 /'},
  {code: '^', name: '乘方 ^'},
  {code: '(', name: '左括号 ('},
  {code: ')', name: '右括号 )'},
  {code: '&', name: '串联 &'},
  {code: '==', name: '等于 =='},
  {code: '<>', name: '不等于 <>'},
  {code: '>', name: '大于 >'},
  {code: '<', name: '小于 <'},
  {code: '>=', name: '大于或等于 >='},
  {code: '<=', name: '小于或等于 <='}
]
// 公式类型描述1
const formulaType = [
  {
    name: '日期和时间',
    list: [
      {
        code: 'YEAR()',
        name: 'YEAR',
        title: 'YEAR(date)',
        describe: '返回指定日期字段中年份的正数。',
        example: '示例：YEAR（2017-12-28）=2017'
      },
      {
        code: 'MONTH()',
        name: 'MONTH',
        title: 'MONTH(date)',
        describe: '返回指定日期字段中月份的正数。',
        example: '示例：MONTH（2017-12-28）=12'
      },
      {
        code: 'DAY()',
        name: 'DAY',
        title: 'DAY(date)',
        describe: '返回指定日期字段中某月的天数。',
        example: '示例：DAY（2017-12-28）=28'
      },
      {
        code: 'NOW()',
        name: 'NOW',
        title: 'NOW(date)',
        describe: '返回表示当前时刻的日期时间。',
        example: '示例：NOW()  =  2017-12-28 12:00:00'
      },
      {
        code: 'TODAY()',
        name: 'TODAY',
        title: 'TODAY(date)',
        describe: '返回表示当前时刻的日期。',
        example: '示例：TODAY()  =  2017-12-28'
      },
      {
        code: 'DATEDIFF()',
        name: 'DATEDIFF',
        title: 'DATEDIFF(datepart,startdate,enddate)',
        describe: '返回两个日期之间的天数。 Y:年、M:月、D:日、H:时、N:分、S:秒；',
        example: '示例：DATEDIFF（"' + 'D' + '",2017-12-25,2017-12-28）=3'
      },
      {
        code: 'DATEADD()',
        name: 'DATEADD',
        title: 'DATEADD(datepart,number,date)',
        describe: '在向指定日期加上一段时间的基础上，返回新的 datetime 值。Y:年、M:月、D:日、H:时、N:分、S:秒；',
        example: '示例：DATEADD（"' + 'D' + '",1,2017-12-28）=2017-12-29'
      },
      {
        code: 'EXTRACT()',
        name: 'EXTRACT',
        title: 'EXTRACT(“d”,2017-12-25,DATE（）)',
        describe: '返回两个日期之间的时间。Y:年、M:月、D:日、H:时、N:分、S:秒',
        example: '示例：EXTRACT（“d”,2017-12-25,DATE（）），实时计算结果值。'
      }
    ]
  },
  {
    name: '数字',
    list: [
      {
        code: 'ABS()',
        name: 'ABS',
        title: 'ABS(number)',
        describe: '返回数值的绝对值（不含正负号的数字）。',
        example: '示例：ABS（10-25）=15'
      }
    ]
  },
  {
    name: '逻辑',
    list: [
      // {
      //   code: 'CASE WHEN THEN ELSE END',
      //   name: 'CASE',
      //   title: 'CASE',
      //   describe: '根据一系列值检查一个给定表达式。如果表达式等于其中一个值，则返回相应结果。如果它不等于任何值，则返回ELSE结果。',
      //   example: '示例：客户信用等级= CASEWHEN 订单总金额>100000 THEN "A级"WHEN 订单总金额<100000 AND 订单总金额  >10000  THEN "B级"ELSE "C级"END'
      // },
      {
        code: 'IF THEN \nELSE IF THEN \nEND',
        name: 'IF',
        title: 'IF THEN',
        describe: '如果条件满足（布尔表达式返回 TRUE 时），则在 IF 关键字及其条件之后执行 语句。可选的 ELSE 关键字引入备用的 语句，当不满足 IF 条件时，就执行这个(ELSE)语句。',
        example: "示例：IF 分数==100 THEN 'AA'ELSE IF 分数>=90 THEN 'A' ELSE IF 分数>=80 THEN 'B' ELSE IF 分数>=70 THEN 'C' ELSE IF 分数>=60 THEN 'D' ELSE 'F' END"
      },
      {
        code: 'ISNULL()',
        name: 'ISNULL',
        title: 'ISNULL()',
        describe: '确定表达式是否为空（空白），如果是则返回 TRUE（真）。如果它包含一个值，则该函数返回 FALSE（假）。',
        example: '示例：ISNULL(订单.总金额,0)若订单.总金额不为NULL则显示订单.总金额，若为NULL则显示逗号后面的0'
      }
    ]
  },
  {
    name: '文本',
    list: [
      {
        code: 'LEFT()',
        name: 'LEFT',
        title: 'LEFT（text,num_chars）',
        describe: '返回从文本字符串开头算起的指定数量的字符。',
        example: '示例：LEFT（132467,3）=132'
      },
      {
        code: 'LENGTH()',
        name: 'LENGTH',
        title: 'LENGTH(text)',
        describe: '返回指定的文本字符串中所含的字符数。',
        example: '示例：LEN（13 12）=5'
      },
      {
        code: 'RIGHT()',
        name: 'RIGHT',
        title: 'RIGHT（text,num_chars）',
        describe: '返回从文本字符串右边算起的指定数量的字符',
        example: '示例：RIGHT（13255467,3）=467'
      },
      {
        code: 'TEXT()',
        name: 'TEXT',
        title: 'TEXT()',
        describe: '将一个值转换为使用标准显示格式的文本。',
        example: ''
      },
      {
        code: 'VALUE()',
        name: 'VALUE',
        title: 'VALUE()',
        describe: '将文本字符串转换为数字类型.',
        example: ''
      }
    ]
  }
]
// 公式类型描述2
const formulaType2 = [
  {
    name: '汇总',
    list: [
      {
        code: 'COUNT()',
        name: 'COUNT',
        title: 'COUNT()',
        describe: '返回组中的各项项数。',
        example: '示例：COUNT(产品.产品编号) 返回组中的项数'
      },
      {
        code: 'SUM()',
        name: 'SUM',
        title: 'SUM()',
        describe: '返回组中各项的和。',
        example: '示例：客户成交总金额=SUM（合同.总金额）'
      },
      {
        code: 'MIN()',
        name: 'MIN',
        title: 'MIN()',
        describe: '返回一系列数字中的最小数字。',
        example: '示例：学员最低成绩=MIN（成绩.分数）'
      },
      {
        code: 'MAX()',
        name: 'MAX',
        title: 'MAX()',
        describe: '返回一系列数字中的最大数字。',
        example: '示例：学员最低成绩=MAN（成绩.分数）  '
      },
      {
        code: 'AVG()',
        name: 'AVG',
        title: 'AVG()',
        describe: '返回组中的平均值。',
        example: '示例：会员平均消费=AVG（销售订单.金额）'
      }
    ]
  }
]
// 公式返回类型-简单
const formulaReturn1 = [
  { value: '0', label: '数字' },
  { value: '1', label: '整数' },
  { value: '2', label: '百分比' }
]
// 公式返回类型-复杂
const formulaReturn2 = [
  { value: '0', label: '数字' },
  { value: '1', label: '整数' },
  { value: '2', label: '百分比' },
  { value: '3', label: '文本' },
  { value: '4', label: '日期时间' }
]
// 24种颜色值
const colors24 = [
  '#19BFA4',
  '#2CCCDA',
  '#2EBCFF',
  '#4E8AF9',
  '#7076FA',
  '#C472EE',
  '#EF7EDE',
  '#F969AA',
  '#FC587B',
  '#FA5A55',
  '#FF7748',
  '#FFA416',
  '#FFD234',
  '#98D75A',
  '#66C060',
  '#38BA5D'
]
// 应用默认图标
const defaultAppIcon = [
  'icon-apps-library-1',
  'icon-apps-library-2',
  'icon-apps-library-3',
  'icon-apps-library-4',
  'icon-apps-library-5',
  'icon-apps-library-6',
  'icon-apps-library-7',
  'icon-apps-library-8'
]
// 模块默认图标
const defaultModuleIcon = [
  'icon-modules-library-1',
  'icon-modules-library-2',
  'icon-modules-library-3',
  'icon-modules-library-4',
  'icon-modules-library-5',
  'icon-modules-library-6',
  'icon-modules-library-7',
  'icon-modules-library-8',
  'icon-modules-library-9',
  'icon-modules-library-10',
  'icon-modules-library-11',
  'icon-modules-library-12',
  'icon-modules-library-13',
  'icon-modules-library-14'
]
// 创建默认模块
const defaultModule = {
  'bean': '',
  'title': '',
  'version': '0',
  'icon_type': '0',
  'icon_url': '',
  'icon_color': '',
  'applicationId': '',
  'applicationName': '',
  'pageNum': 0,
  'pageName': '标准页面',
  'commentControl': '1',
  'dynamicControl': '1',
  'terminalPc': '1',
  'terminalApp': '1',
  'layout': [
    {
      'title': '基本信息',
      'name': 'basic',
      'isSpread': '0',
      'isHideColumnName': '0',
      'isHideInCreate': '0',
      'isHideInDetail': '0',
      'terminalPc': '1',
      'terminalApp': '1',
      'dragOptions': {
        'animation': 100,
        'group': { 'name': 'compontents', 'pull': true, 'put': true },
        'ghostClass': 'ghost',
        'filter': '.no-drag'
      },
      'rows': [
        {
          'name': 'text_',
          'label': '名称',
          'type': 'text',
          'typeText': '单行文本',
          'width': '50%',
          'state': '1',
          'remove': '1',
          'active': '0',
          'field': {
            'fieldControl': '0',
            'addView': '1',
            'editView': '1',
            'structure': '1',
            'pointOut': '',
            'repeatCheck': '0',
            'terminalPc': '1',
            'terminalApp': '1',
            'defaultValue': ''
          }
        },
        {
          'name': 'personnel_principal',
          'label': '负责人',
          'type': 'personnel',
          'typeText': '人员',
          'width': '50%',
          'state': '1',
          'remove': '0',
          'active': '0',
          'field': {
            'chooseType': '0',
            'fieldControl': '2',
            'addView': '1',
            'editView': '1',
            'structure': '1',
            'terminalPc': '1',
            'terminalApp': '1',
            'defaultPersonnel': [{
              'checked': true,
              'id': -1,
              'name': '当前用户',
              'type': 3,
              'value': '3-personnel_create_by_superior'
            }],
            'chooseRange': []
          }
        }
      ]
    }
  ],
  'appearance': {
    'themeId': 0,
    'pageWidth': 780,
    'pageBgType': 0,
    'pageBgImg': '',
    'pageBgColor': '#CFE7FA',
    'headerBgType': 0,
    'headerBgImg': '',
    'headerBgColor': '#1989FA',
    'headerBgOpacity': '不透明',
    'headerModuleName': '',
    'headerTextColor': '#FFFFFF',
    'headerTextSize': 72,
    'headerModuleDescribe': '',
    'describeTextColor': '#FFFFFF',
    'describeTextSize': 18,
    'contentBgcolor': '#FFFFFF',
    'contentBgOpacity': '不透明',
    'commitBgType': 0,
    'commitBgImg': '',
    'commitBgColor': '#FFFFFF',
    'commitBgOpacity': '不透明',
    'commitButtonColor': '#4A90E2',
    'commitButtonShow': 1,
    'commitButtonTextFirst': '取消',
    'commitButtonTextSecond': '保存',
    'commitButtonWidth': 0,
    'commitButtonPosition': 2
  },
  'enableLayout': {
    'layout': [
      {
        'title': '基本信息',
        'name': 'basic',
        'isSpread': '0',
        'isHideColumnName': '0',
        'isHideInCreate': '0',
        'isHideInDetail': '0',
        'terminalPc': '1',
        'terminalApp': '1',
        'dragOptions': {
          'animation': 100,
          'group': { 'name': 'compontents', 'pull': true, 'put': true },
          'ghostClass': 'ghost',
          'filter': '.no-drag'
        },
        'rows': [
          {
            'name': 'text_',
            'label': '名称',
            'type': 'text',
            'typeText': '单行文本',
            'width': '50%',
            'state': '1',
            'remove': '1',
            'active': '0',
            'field': {
              'fieldControl': '0',
              'addView': '1',
              'editView': '1',
              'structure': '1',
              'pointOut': '',
              'repeatCheck': '0',
              'terminalPc': '1',
              'terminalApp': '1',
              'defaultValue': ''
            }
          },
          {
            'name': 'personnel_principal',
            'label': '负责人',
            'type': 'personnel',
            'typeText': '人员',
            'width': '50%',
            'state': '1',
            'remove': '0',
            'active': '0',
            'field': {
              'chooseType': '0',
              'fieldControl': '2',
              'addView': '1',
              'editView': '1',
              'structure': '1',
              'terminalPc': '1',
              'terminalApp': '1',
              'defaultPersonnel': [{
                'checked': true,
                'id': -1,
                'name': '当前用户',
                'type': 3,
                'value': '3-personnel_create_by_superior'
              }],
              'chooseRange': []
            }
          }
        ]
      }
    ]
  },
  'disableLayout': {
    'rows': []
  }
}
// 主题外观
const themes = [
  {id: 0, text: '休闲', color: '#CFE7FA', url: 'theme-s-0.png', background: 'theme-l-0.png'},
  {id: 1, text: '轻松', color: '#E1FCF9 ', url: 'theme-s-1.png', background: 'theme-l-1.png'},
  {id: 2, text: '商务', color: '#D1D6EB', url: 'theme-s-2.png', background: 'theme-l-2.png'},
  {id: 3, text: '合作', color: '#F4EEDB', url: 'theme-s-3.png', background: 'theme-l-3.png'},
  {id: 4, text: '会议', color: '#E0BED2', url: 'theme-s-4.png', background: 'theme-l-4.png'},
  {id: 5, text: '向上', color: '#B9CAD8', url: 'theme-s-5.png', background: 'theme-l-5.png'},
  {id: 6, text: '行政', color: '#C2E7FF', url: 'theme-s-6.png', background: 'theme-l-6.png'},
  {id: 7, text: '趣味', color: '#FFCBBC', url: 'theme-s-7.png', background: 'theme-l-7.png'},
  {id: 8, text: '寂静', color: '#D1D6EB', url: 'theme-s-8.png', background: 'theme-l-8.png'},
  {id: 9, text: '都市', color: '#D2CCEA', url: 'theme-s-9.png', background: 'theme-l-9.png'},
  {id: 10, text: '清凉', color: '#B4F2FA', url: 'theme-s-10.png', background: 'theme-l-10.png'},
  {id: 11, text: '梦幻', color: '#C6EBFF', url: 'theme-s-11.png', background: 'theme-l-11.png'},
  {id: 12, text: '日出', color: '#E0BED2', url: 'theme-s-12.png', background: 'theme-l-12.png'},
  {id: 13, text: '初夏', color: '#FFF9E8', url: 'theme-s-13.png', background: 'theme-l-13.png'},
  {id: 14, text: '热情', color: '#FBE6D6', url: 'theme-s-14.png', background: 'theme-l-14.png'},
  {id: 15, text: '遥远', color: '#E0C6BE', url: 'theme-s-15.png', background: 'theme-l-15.png'},
  {id: 16, text: '夜深', color: '#CCDCEA', url: 'theme-s-16.png', background: 'theme-l-16.png'},
  {id: 17, text: '工作', color: '#E8F3FF', url: 'theme-s-17.png', background: 'theme-l-17.png'}
]
/** 正则 */
const regular = {
  ipRegularJUdge: /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])(\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])){3}$/, /* IP */
  phone: /^1[3|4|5|7|8][0-9]\d{8}$/,
  pwdRegx: /^[0-9A-Za-z]{6,18}$/,
  externalPwd: /^[0-9A-Za-z]{6,18}$/,
  pwdRegx0: /[\s\S]/,
  pwdRegx1: /[A-Za-z0-9]/,
  pwdRegx2: /[\s\S]/,
  pwdRegx3: /[\s\S]/,
  pwdRegx4: /[\s\S]/
}
// 任务自定义默认7个字段
const taskDefaultFields = [
  {
    name: 'default_name',
    label: '名称',
    type: 'text',
    typeText: '单行文本',
    width: '100%',
    state: '1',
    remove: '0',
    active: '0',
    field: {
      fieldControl: '2', // 字段控制('0'都不选 1只读 2必填)
      addView: '1', // 新增显示
      editView: '1', // 编辑显示
      structure: '1', // 结构(0上下布局 1左右布局)
      pointOut: '', // 提示框
      repeatCheck: '0', // 查重(0不查重 1允许保存 2不允许保存)
      terminalPc: '1', // PC终端
      terminalApp: '1', // APP终端
      defaultValue: '',
      isShowCard: '0', // 是否展示任务卡片(0 不显示，1 显示)
      isFillReason: '0' // 激活原因(0 不填写，1 填写)
    }
  },
  {
    'name': 'default_reference', // 英文名称
    'label': '关联', // 中文名称
    'type': 'reference', // 组件类型
    'typeText': '关联关系', // 组件类型描述
    'width': '100%', // 组件宽度
    'state': '0', // 组件启禁用
    'remove': '0', // 可移除组件
    'active': '0', // 是否选中
    'field': {
      'fieldControl': '0', // 字段控制(0都不选 1只读 2必填)
      'addView': '1', // 新增显示
      'editView': '1', // 编辑显示
      'pointOut': '', // 提示框
      'structure': '1', // 结构(0上下布局 1左右布局)
      'isShowCard': '0', // 是否展示任务卡片(0 不显示，1 显示)
      'isFillReason': '0' // 激活原因(0 不填写，1 填写)
    },
    'relevanceModule': { // 关联模块
      'moduleLabel': '##', // 关联模块中文名称
      'moduleName': '##' // 关联模块英文名称
    },
    'relevanceField': { // 关联字段
      'fieldLabel': '##', // 关联字段中文名称
      'fieldName': '##' // 关联字段英文名称
    },
    'searchFields': [ // 搜索字段
      {
        'fieldLabel': '##', // 搜索字段中文名称
        'fieldName': '##' // 搜索字段英文名称
      }
    ],
    'relevanceWhere': [ // 关联筛选条件
      {
        'fieldLabel': '', // 条件字段中文名称
        'fieldName': '', // 条件字段英文名称
        'operatorType': '', // 条件运算符
        'value': '' // 条件匹配值
      }
    ],
    'seniorWhere': '1 AND 2'
  },
  {
    'name': 'default_tag', // 英文名称
    'label': '标签', // 中文名称
    'type': 'tag', // 组件类型
    'typeText': '下拉选项', // 组件类型描述
    'width': '100%', // 组件宽度
    'remove': '0', // 可移除组件
    'field': {
      'fieldControl': '0',
      'structure': '1' // 结构(0上下布局 1左右布局)
    }
  },
  {
    'name': 'richtext' + new Date().getTime() + '0', // 英文名称
    'label': '描述', // 中文名称
    'type': 'richtext', // 组件类型
    'typeText': '富文本', // 组件类型描述
    'width': '100%', // 组件宽度
    'state': '0', // 组件启禁用
    'remove': '1', // 可移除组件
    'active': '0', // 是否选中
    'field': {
      'fieldControl': '0', // 字段控制(0都不选 1只读 2必填)
      'addView': '1', // 新增显示
      'editView': '1', // 编辑显示
      'structure': '1', // 结构(0上下布局 1左右布局)
      'pointOut': '', // 提示框
      'repeatCheck': '0', // 查重(0不查重 1允许保存 2不允许保存)
      'defaultValue': '', // 默认值
      'isShowCard': '0', // 是否展示任务卡片(0 不显示，1 显示)
      'isFillReason': '0' // 激活原因(0 不填写，1 填写)
    }
  },
  {
    'name': 'default_executor', // 英文名称
    'label': '执行人', // 中文名称
    'type': 'personnel', // 组件类型
    'typeText': '人员', // 组件类型描述
    'width': '50%', // 组件宽度
    'state': '0', // 组件启禁用
    'remove': '0', // 可移除组件
    'active': '0', // 是否选中
    'field': {
      'chooseType': '0', // 选择类型：0单选、1多选
      'defaultPersonnel': [// 默认人员
        // 人员对象
      ],
      'chooseRange': [ // 可选范围(公司:gs_id、部门:bm_id、人员:ry_id)
        // 范围对象
      ],
      'structure': '1', // 结构
      'fieldControl': '0', // 字段控制(0都不选 1只读 2必填)
      'addView': '1', // 新增显示
      'editView': '1' // 编辑显示
    }
  },
  {
    'name': 'default_deadline', // 英文名称
    'label': '截止时间', // 中文名称
    'type': 'datetime', // 组件类型
    'typeText': '日期时间', // 组件类型描述
    'width': '50%', // 组件宽度
    'state': '0', // 组件启禁用
    'remove': '0', // 可移除组件
    'active': '0', // 是否选中
    'field': {
      'datetimeType': 'YYYY-MM-DD', // 日期格式
      'defaultValueId': '', // 默认值
      'defaultValue': '', // 默认值
      'structure': '1', // 结构
      'fieldControl': '0', // 字段控制(0都不选 1只读 2必填)
      'addView': '1', // 新增显示
      'editView': '1' // 编辑显示
    }
  },
  {
    'name': 'attachment_' + new Date().getTime() + '1', // 英文名称
    'label': '附件', // 中文名称
    'type': 'attachment', // 组件类型
    'typeText': '附件', // 组件类型描述
    'width': '100%', // 组件宽度
    'state': '1', // 组件启禁用
    'remove': '1', // 可移除组件
    'active': '0', // 是否选中
    'field': {
      'fieldControl': '0', // 字段控制(0都不选 1只读 2必填)
      'addView': '1', // 新增显示
      'editView': '1', // 编辑显示
      'structure': '1', // 结构(0上下布局 1左右布局)
      'countLimit': '0', // 数量限制(0不限制 1限制)
      'maxCount': '#max#', // 最大上传数
      'maxSize': '#max#' // 最大上传大小
    }
  }
]
const TFParameter = {
  website: 'https://www.teamface.cn/index.html',
  TEAMFACEPWD: 'hjhq2017Teamface'
}
// web表单辅助组件、
const webformComponents = [
  {
    name: 'default_name',
    label: '文本描述',
    type: 'description',
    typeText: '文本描述',
    text: ''
  },
  {
    name: 'default_name',
    label: '分割线',
    type: 'splitLine',
    typeText: '分割线',
    fields: {
      lineType: '', // solid: 实线， dashed：虚线，dotted，点画线，double 双实线
      lineColor: '#000',
      lineWidth: '1px'
    }
  },
  {
    name: 'default_name',
    label: '按钮',
    type: 'button',
    typeText: '文本描述',
    fields: {
      text: '',
      color: '',
      fontSize: '',
      bold: false,
      icon: '',
      radius: '1px',
      height: '',
      width: 'auto/15px',
      widthType: '0'
    },
    position: '',
    event: {value: '1', label: '跳转链接'}, // 0 无， 1， 跳转链接， 2 拨打电话 3 发短信， 4 发邮件
    eventText: '',
    styleType: '0' // 0 默认，1 自定义
  },
  {
    name: 'default_name',
    label: '图片展示',
    type: 'imageShow',
    typeText: '文本描述',
    count: '1',
    imageList: [
      {imageUrl: '', isJumpUrl: false, jumpUrl: ''}
    ]
  },
  {
    name: 'default_name',
    label: '图片轮播',
    type: 'imagecarousel',
    typeText: '文本描述',
    imageList: [
      {imageUrl: '', isJumpUrl: false, jumpUrl: ''}
    ],
    duration: '3s',
    height: '200px'
  },
  {
    name: 'default_name',
    label: '视频展示',
    type: 'video',
    typeText: '文本描述',
    url: ''
  },
  {
    name: 'default_name',
    label: '静态地图',
    type: 'staticMap',
    typeText: '文本描述',
    address: '',
    isMarkMsg: false,
    markMsg: '',
    scale: ''
  },
  {
    name: 'default_name',
    label: '计时器',
    type: 'timer',
    typeText: '文本描述',
    form: {value: '0', label: '计时'},
    formatType: {value: '0', label: '天'},
    initialTime: '',
    arrivalTime: ''
  },
  {
    name: 'default_name',
    label: '签到',
    type: 'attendance',
    typeText: '文本描述',
    title: '',
    description: '',
    verifyField: {value: '0', label: '姓名'},
    isAllowOpenSignIn: false,
    qrCode: ''
  }
]
/** 行业 */
const appIndustry = {
  'industry': [{id: 1, label: '电商平台'}, {id: 2, label: 'IT互联网'}, {id: 3, label: '教育培训'}, {id: 4, label: '房产中介'}, {id: 5, label: '金融保险'}, {id: 6, label: '物流运输'}, {id: 7, label: '家政服务'}, {id: 8, label: '汽车服务'}, {id: 9, label: '医疗制药'}, {id: 10, label: '律师案件'}, {id: 11, label: '农林牧渔业'}, {id: 12, label: '广告媒体'}, {id: 13, label: '其他'}], 'classification': [{id: 1, label: '行政办公'}, {id: 2, label: '人力资源'}, {id: 3, label: '客户管理'}, {id: 4, label: '会员管理'}, {id: 5, label: '售后管理'}, {id: 6, label: '进销存'}, {id: 7, label: '仓储管理'}, {id: 8, label: '其他'}]
}
// 二维码样式编号
const barcodeStyleList = [
  {bgImg: 'CODE39.png', code: 'CODE 39'},
  {bgImg: 'CODE128.png', code: 'CODE 128'},
  {bgImg: 'CODABAR.png', code: 'CODABAR'},
  {bgImg: 'EAN8.png', code: 'EAN 8'},
  {bgImg: 'EAN13.png', code: 'EAN 13'},
  {bgImg: 'EAN128.png', code: 'EAN128'},
  {bgImg: 'UPC-A.png', code: 'UPC-A'},
  {bgImg: 'UPC-E.png', code: 'UPC-E'},
  {bgImg: 'PostNet.png', code: 'PostNet'},
  {bgImg: 'ITF-14.png', code: 'ITF-14'},
  {bgImg: 'Interleaved2of5.png', code: 'Interleaved 2of5'},
  {bgImg: 'PDF417.png', code: 'PDF417'},
  {bgImg: 'DataMatrix.png', code: 'Data Matrix'}
]
// 常用字段
const commonField = [
  {
    'name': 'text_',
    'label': '公司名称',
    'type': 'text',
    'typeText': '单行文本',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'icon': 'icon-danhangwenben',
    'isDrag': true,
    'field': {
      'fieldControl': '2',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'pointOut': '',
      'repeatCheck': '2',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultValue': ''
    }
  },
  {
    'name': 'text_',
    'label': '姓名',
    'type': 'text',
    'typeText': '单行文本',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'icon': 'icon-danhangwenben',
    'isDrag': true,
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'pointOut': '',
      'repeatCheck': '0',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultValue': ''
    }
  },
  {
    'name': 'phone_',
    'label': '电话',
    'type': 'phone',
    'typeText': '电话',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'icon': 'icon-dianhua',
    'isDrag': true,
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'pointOut': '',
      'phoneType': '0',
      'phoneLenth': '0',
      'repeatCheck': '0',
      'terminalPc': '1',
      'terminalApp': '1'
    }
  },
  {
    'name': 'picklist_',
    'label': '行业',
    'type': 'picklist',
    'typeText': '下拉选项',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'icon': 'icon-xialaxuanxiang',
    'isDrag': true,
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'chooseType': '0',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultEntrys': []
    },
    'entrys': [
      {
        'value': '0',
        'label': '金融',
        'color': '#FFFFFF'
      },
      {
        'value': '1',
        'label': '咨询',
        'color': '#FFFFFF'
      },
      {
        'value': '2',
        'label': '娱乐',
        'color': '#FFFFFF'
      },
      {
        'value': '3',
        'label': '媒体',
        'color': '#FFFFFF'
      },
      {
        'value': '4',
        'label': '零售',
        'color': '#FFFFFF'
      },
      {
        'value': '5',
        'label': '能源',
        'color': '#FFFFFF'
      },
      {
        'value': '6',
        'label': '服务',
        'color': '#FFFFFF'
      },
      {
        'value': '7',
        'label': '制造业',
        'color': '#FFFFFF'
      },
      {
        'value': '8',
        'label': '通讯',
        'color': '#FFFFFF'
      },
      {
        'value': '9',
        'label': '酒店',
        'color': '#FFFFFF'
      },
      {
        'value': '10',
        'label': '保险',
        'color': '#FFFFFF'
      },
      {
        'value': '11',
        'label': '银行',
        'color': '#FFFFFF'
      },
      {
        'value': '12',
        'label': '服装',
        'color': '#FFFFFF'
      },
      {
        'value': '13',
        'label': '政府',
        'color': '#FFFFFF'
      },
      {
        'value': '14',
        'label': '高科技',
        'color': '#FFFFFF'
      },
      {
        'value': '15',
        'label': '教育',
        'color': '#FFFFFF'
      },
      {
        'value': '16',
        'label': '电信',
        'color': '#FFFFFF'
      },
      {
        'value': '17',
        'label': '公共事业',
        'color': '#FFFFFF'
      },
      {
        'value': '18',
        'label': '非盈利事业',
        'color': '#FFFFFF'
      },
      {
        'value': '19',
        'label': '其它',
        'color': '#FFFFFF'
      }
    ]
  },
  {
    'name': 'text_',
    'label': 'QQ',
    'type': 'text',
    'typeText': '单行文本',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'icon': 'icon-danhangwenben',
    'isDrag': true,
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'pointOut': '',
      'repeatCheck': '0',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultValue': ''
    }
  },
  {
    'name': 'picklist_',
    'label': '客户级别',
    'type': 'picklist',
    'typeText': '下拉选项',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'icon': 'icon-xialaxuanxiang',
    'isDrag': true,
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'chooseType': '0',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultEntrys': []
    },
    'entrys': [
      {
        'value': '0',
        'label': 'A（非常重要）',
        'color': '#FFFFFF'
      },
      {
        'value': '1',
        'label': 'B（重要）',
        'color': '#FFFFFF'
      },
      {
        'value': '2',
        'label': 'C（一般）',
        'color': '#FFFFFF'
      }
    ]
  },
  {
    'name': 'phone_',
    'label': '手机号码',
    'type': 'phone',
    'typeText': '电话',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'icon': 'icon-dianhua',
    'isDrag': true,
    'field': {
      'fieldControl': '2',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'pointOut': '',
      'phoneType': '1',
      'phoneLenth': '1',
      'repeatCheck': '2',
      'terminalPc': '1',
      'terminalApp': '1'
    }
  },
  {
    'name': 'email_',
    'label': '电子邮箱',
    'type': 'email',
    'typeText': '电子邮箱',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'icon': 'icon-youxiang',
    'isDrag': true,
    'field': {
      'pointOut': '',
      'structure': '1',
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'repeatCheck': '0',
      'terminalPc': '1',
      'terminalApp': '1'
    }
  },
  {
    'name': 'department_',
    'label': '部门',
    'type': 'department',
    'typeText': '部门',
    'width': '50%',
    'state': '0',
    'remove': '0',
    'icon': 'icon-bumen',
    'isDrag': true,
    'field': {
      'chooseType': '0',
      'defaultDepartment': [],
      'chooseRange': [],
      'structure': '1',
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'terminalPc': '1',
      'terminalApp': '1'
    }
  },
  {
    'name': 'text_',
    'label': '职位',
    'type': 'text',
    'typeText': '单行文本',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'icon': 'icon-danhangwenben',
    'isDrag': true,
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'pointOut': '',
      'repeatCheck': '0',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultValue': ''
    }
  },
  {
    'name': 'picklist_',
    'label': '性别',
    'type': 'picklist',
    'typeText': '下拉选项',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'icon': 'icon-xialaxuanxiang',
    'isDrag': true,
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'chooseType': '0',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultEntrys': []
    },
    'entrys': [
      {
        'value': '0',
        'label': '男',
        'color': '#FFFFFF'
      },
      {
        'value': '1',
        'label': '女',
        'color': '#FFFFFF'
      },
      {
        'value': '2',
        'label': '未知',
        'color': '#FFFFFF'
      }
    ]
  },
  {
    'name': 'location_',
    'label': '地址',
    'type': 'location',
    'typeText': '定位',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'icon': 'icon-dingwei',
    'isDrag': true,
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'pointOut': '',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultValue': '1'
    }
  },
  {
    'name': 'text_',
    'label': '传真',
    'type': 'text',
    'typeText': '单行文本',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'icon': 'icon-danhangwenben',
    'isDrag': true,
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'pointOut': '',
      'repeatCheck': '0',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultValue': ''
    }
  },
  {
    'name': 'text_',
    'label': '微信',
    'type': 'text',
    'typeText': '单行文本',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'icon': 'icon-danhangwenben',
    'isDrag': true,
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'pointOut': '',
      'repeatCheck': '0',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultValue': ''
    }
  },
  {
    'name': 'datetime_',
    'label': '开始日期',
    'type': 'datetime',
    'typeText': '日期/时间',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'icon': 'icon-riqishijian',
    'isDrag': true,
    'field': {
      'formatType': 'yyyy-MM-dd',
      'defaultValueId': '0',
      'defaultValue': '',
      'structure': '1',
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'terminalPc': '1',
      'terminalApp': '1'
    }
  },
  {
    'name': 'datetime_',
    'label': '结束日期',
    'type': 'datetime',
    'typeText': '日期/时间',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'icon': 'icon-riqishijian',
    'isDrag': true,
    'field': {
      'formatType': 'yyyy-MM-dd',
      'defaultValueId': '0',
      'defaultValue': '',
      'structure': '1',
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'terminalPc': '1',
      'terminalApp': '1'
    }
  },
  {
    'name': 'picklist_',
    'label': '优先级',
    'type': 'picklist',
    'typeText': '下拉选项',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'icon': 'icon-xialaxuanxiang',
    'isDrag': true,
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'chooseType': '0',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultEntrys': []
    },
    'entrys': [
      {
        'value': '0',
        'label': '高',
        'color': '#FFFFFF'
      },
      {
        'value': '1',
        'label': '中',
        'color': '#FFFFFF'
      },
      {
        'value': '2',
        'label': '低',
        'color': '#FFFFFF'
      }
    ]
  },
  {
    'name': 'picklist_',
    'label': '跟进状态',
    'type': 'picklist',
    'typeText': '下拉选项',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'icon': 'icon-xialaxuanxiang',
    'isDrag': true,
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'chooseType': '0',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultEntrys': [
        {
          'value': '0',
          'label': '初步沟通',
          'color': '#FFFFFF'
        }
      ]
    },
    'entrys': [
      {
        'value': '0',
        'label': '初步沟通',
        'color': '#FFFFFF'
      },
      {
        'value': '1',
        'label': '见面拜访',
        'color': '#FFFFFF'
      },
      {
        'value': '2',
        'label': '确定意向',
        'color': '#FFFFFF'
      },
      {
        'value': '3',
        'label': '商务洽谈',
        'color': '#FFFFFF'
      },
      {
        'value': '4',
        'label': '正式报价',
        'color': '#FFFFFF'
      },
      {
        'value': '5',
        'label': '签约合作',
        'color': '#FFFFFF'
      },
      {
        'value': '6',
        'label': '停滞客户',
        'color': '#FFFFFF'
      },
      {
        'value': '7',
        'label': '流失客户',
        'color': '#FFFFFF'
      }
    ]
  },
  {
    'name': 'picklist_',
    'label': '来源',
    'type': 'picklist',
    'typeText': '下拉选项',
    'width': '50%',
    'state': '0',
    'remove': '1',
    'icon': 'icon-xialaxuanxiang',
    'isDrag': true,
    'field': {
      'fieldControl': '0',
      'addView': '1',
      'editView': '1',
      'structure': '1',
      'chooseType': '0',
      'terminalPc': '1',
      'terminalApp': '1',
      'defaultEntrys': []
    },
    'entrys': [
      {
        'value': '0',
        'label': '媒体-广告',
        'color': '#FFFFFF'
      },
      {
        'value': '1',
        'label': '外部推荐',
        'color': '#FFFFFF'
      },
      {
        'value': '2',
        'label': '关键字',
        'color': '#FFFFFF'
      },
      {
        'value': '3',
        'label': '研讨会-内部',
        'color': '#FFFFFF'
      },
      {
        'value': '4',
        'label': '员工推荐',
        'color': '#FFFFFF'
      },
      {
        'value': '5',
        'label': '研讨会-合作伙伴',
        'color': '#FFFFFF'
      },
      {
        'value': '6',
        'label': '直接邮件',
        'color': '#FFFFFF'
      },
      {
        'value': '7',
        'label': '展览会',
        'color': '#FFFFFF'
      },
      {
        'value': '8',
        'label': '公共关系',
        'color': '#FFFFFF'
      },
      {
        'value': '9',
        'label': '合作伙伴',
        'color': '#FFFFFF'
      },
      {
        'value': '10',
        'label': '其它',
        'color': '#FFFFFF'
      }
    ]
  }
]
// 项目自定义默认4个字段
export { component1, component2, subformList, symbolList, formulaType, formulaType2, colors24, defaultAppIcon, defaultModuleIcon, themes, regular, taskDefaultFields, defaultModule, TFParameter, webformComponents, appIndustry, barcodeStyleList, formulaReturn1, formulaReturn2, commonField }
