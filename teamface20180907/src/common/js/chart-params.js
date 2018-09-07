// 图表的参数
const chartParams = {
  bar: { // 柱状图
    'tooltip': {
      'trigger': 'axis',
      'axisPointer': {
        'type': 'shadow'
      }
    },
    'legend': {
      'left': 'left'
    },
    'xAxis': [
      {
        'type': 'category',
        'data': ['列1', '列2', '列3', '列4', '列5', '列6'],
        'axisLabel': {
          // 'fontSize': 10,
          'rotate': 0
        }
      }
    ],
    'yAxis': {},
    'series': [
      {
        'data': [5, 20, 36, 10, 15, 20],
        'name': '数值',
        'type': 'bar',
        'label': {
          'show': true,
          'position': 'top',
          'fontSize': 10
        },
        'barMaxWidth': 50,
        'barMinHeight': 5
      }
    ],
    'dataZoom': [
      {
        'type': 'inside'
      }
    ],
    'grid': {
      'left': 50,
      'top': 50,
      'bottom': 50,
      'right': 50,
      'containLabel': true
    }
  },
  stackBar: { // 堆叠柱状图
    'tooltip': {
      'trigger': 'axis',
      'axisPointer': {
        'type': 'shadow'
      }
    },
    'legend': {
      'left': 'left'
    },
    'xAxis': [
      {
        'data': ['列1', '列2', '列3', '列4', '列5', '列6'],
        'axisLabel': {
          'rotate': 0
        }
      }
    ],
    'yAxis': {},
    'series': [
      {
        'data': [5, 20, 36, 10, 15, 20],
        'type': 'bar',
        'stack': 'stackbart',
        'name': '数值1',
        'label': {
          'show': true,
          'position': 'insideTop',
          'fontSize': 10
        },
        'barMaxWidth': 50,
        'barMinHeight': 5
      },
      {
        'data': [5, 25, 42, 75, 20, 7],
        'type': 'bar',
        'stack': 'stackbart',
        'name': '数值2',
        'label': {
          'show': true,
          'position': 'insideTop',
          'fontSize': 10
        },
        'barMaxWidth': 50,
        'barMinHeight': 5
      },
      {
        'data': [10, 53, 63, 42, 19, 40],
        'type': 'bar',
        'stack': 'stackbart',
        'name': '数值3',
        'label': {
          'show': true,
          'position': 'insideTop',
          'fontSize': 10
        },
        'barMaxWidth': 50,
        'barMinHeight': 5
      }
    ],
    'grid': {
      'left': 50,
      'top': 50,
      'bottom': 50,
      'right': 50,
      'containLabel': true
    },
    'dataZoom': [
      {
        'type': 'inside'
      }
    ]
  },
  strip: { // 条形图
    'grid': {
      'left': 50,
      'top': 50,
      'bottom': 50,
      'right': 50,
      'containLabel': true
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      left: 'left'
    },
    xAxis: {
      type: 'value'
    },
    yAxis: [{
      type: 'category',
      data: ['横1', '横2', '横3', '横4', '横5', '横6'],
      'axisLabel': {
        'rotate': 0
      }
    }],
    series: [
      {
        name: '数值',
        type: 'bar',
        data: [5, 20, 36, 10, 15, 20],
        label: {
          'show': true,
          'position': 'right'
        },
        'barMaxWidth': 50,
        'barMinHeight': 5
      }
    ]
  },
  stackStrip: { // 堆叠条形图
    tooltip: {
      trigger: 'axis',
      axisPointer: { // 坐标轴指示器，坐标轴触发有效
        type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
      }
    },
    legend: {
      left: 'left'
    },
    xAxis: {
      type: 'value'
    },
    yAxis: [
      {
        type: 'category',
        data: ['横1', '横2', '横3', '横4', '横5', '横6'],
        axisLabel: {
          rotate: 0
        }
      }
    ],
    series: [
      {
        'data': [5, 20, 36, 10, 15, 20],
        'type': 'bar',
        'stack': 'stackbart',
        'name': '数值1',
        'label': {
          'show': true,
          'position': 'insideRight'
        },
        'barMaxWidth': 50,
        'barMinHeight': 5
      },
      {
        'data': [5, 20, 36, 10, 15, 20],
        'type': 'bar',
        'stack': 'stackbart',
        'name': '数值2',
        'label': {
          'show': true,
          'position': 'insideRight'
        },
        'barMaxWidth': 50,
        'barMinHeight': 5
      },
      {
        'data': [5, 20, 36, 10, 15, 20],
        'type': 'bar',
        'stack': 'stackbart',
        'name': '数值3',
        'label': {
          'show': true,
          'position': 'insideRight'
        },
        'barMaxWidth': 50,
        'barMinHeight': 5
      }
    ],
    'grid': {
      'left': 40,
      'top': 40,
      'bottom': 40,
      'right': 40,
      'containLabel': true
    }
    // 'dataZoom': [
    //   {
    //     'type': 'inside'
    //   }
    // ]
  },
  scatter: { // 散点图
    // 'backgroundColor': this.dashBoardData.chart_bgcolor,
    // 'color': this.dashBoardData.chart_color,
    'tooltip': {
    },
    'legend': {
      'left': 'left'
    },
    'xAxis': [{
      'type': 'category',
      'data': ['列1', '列2', '列3', '列4', '列5', '列6'],
      'axisLabel': {
        // 'fontSize': 10,
        'rotate': 0
      }
    }],
    'yAxis': {},
    'series': [{
      'symbolSize': 20,
      'data': [5, 20, 36, 10, 15, 20],
      'name': '数值',
      'type': 'scatter',
      'label': {
        'show': true,
        'position': 'top'
      },
      'barMaxWidth': 50,
      'barMinHeight': 5
    }],
    'grid': {
      'left': 80,
      'top': 50,
      'bottom': 80,
      'right': 80
    },
    'dataZoom': [
      {
        'type': 'inside'
      }
    ]
  },
  pie1: { // 饼图1
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b} : {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '数值',
        type: 'pie',
        radius: '55%',
        // center: ['50%', '60%'],
        label: {
          show: true,
          position: 'outside',
          formatter: '{b} : {c} ({d}%)'
        },
        data: [
          {value: 335, name: '数值1'},
          {value: 310, name: '数值2'},
          {value: 234, name: '数值3'},
          {value: 135, name: '数值4'},
          {value: 548, name: '数值5'}
        ],
        itemStyle: {
          emphasis: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  },
  pie2: { // 饼图2
    // 'backgroundColor': this.dashBoardData.chart_bgcolor,
    // 'color': this.dashBoardData.chart_color,
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b} : {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '数值',
        type: 'pie',
        radius: '55%',
        // center: ['50%', '50%'],
        data: [
          {value: 335, name: '数值1'},
          {value: 310, name: '数值2'},
          {value: 234, name: '数值3'},
          {value: 135, name: '数值4'},
          {value: 348, name: '数值5'}
        ],
        roseType: 'radius',
        label: {
          show: true,
          position: 'outside',
          formatter: '{b} : {c} ({d}%)'
        }
      }
    ]
  },
  annular: { // 环行图
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      x: 'left'
      // data: ['直接访问', '邮件营销', '联盟广告', '视频广告', '搜索引擎']
    },
    series: [
      {
        name: '数值',
        type: 'pie',
        // radius: '55%',
        radius: ['40%', '60%'],
        avoidLabelOverlap: true,
        data: [
          {value: 335, name: '数值1'},
          {value: 310, name: '数值2'},
          {value: 234, name: '数值3'},
          {value: 135, name: '数值4'},
          {value: 348, name: '数值5'}
        ],
        label: {
          show: true,
          position: 'outside',
          formatter: '{b} : {c} ({d}%)'
        }
      }
    ]
  },
  funnel: { // 漏斗图
    tooltip: {
      trigger: 'item',
      formatter: '{b} : {c} <br> 占比：{d}%'
    },
    legend: {
      orient: 'vertical',
      x: 'left'
      // data: ['展现', '点击', '访问', '咨询', '订单']
    },
    calculable: true,
    series: [
      {
        name: '数值',
        type: 'funnel',
        min: 0,
        max: 100,
        minSize: '0%',
        maxSize: '70%',
        sort: 'descending',
        gap: 1,
        data: [
          {value: 60, name: '数值1'},
          {value: 40, name: '数值2'},
          {value: 20, name: '数值3'},
          {value: 80, name: '数值4'},
          {value: 100, name: '数值5'}
        ],
        label: {
          show: true,
          position: 'right',
          formatter: '{b} : {c} ({d}%)'
        }
      }
    ]
  },
  map: { // 地图
    tooltip: {
      trigger: 'item',
      formatter: null
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    visualMap: {
      min: 0,
      max: 2500,
      left: 'left',
      top: 'bottom',
      text: ['高', '低'], // 文本，默认为数值文本
      calculable: true,
      show: false,
      inRange: {
        color: []
      }
    },
    geo: {
      map: 'china',
      label: {
        show: false,
        emphasis: {
          show: false
        }
      },
      roam: true,
      itemStyle: {
        // normal: {
        //   areaColor: '#ccc',
        //   borderColor: '#ddd'
        // }
        // emphasis: {
        //   areaColor: '#2a333d'
        // }
      }
    },
    series: [
      {
        name: '数值',
        type: 'map',
        // mapType: 'china',
        geoIndex: 0,
        label: {
          normal: {
            show: false
          },
          emphasis: {
            show: false,
            textStyle: {
              color: '#fff'
            }
          }
        },
        data: [
          {name: '广东', value: '500'},
          {name: '四川', value: '300'},
          {name: '江苏', value: '1000'}
        ]
      }
    ]
  },
  gauge: { // 仪表图
    tooltip: {
      formatter: '{a} : {c}'
    },
    axisTick: {
      splitNumber: 5
    },
    series: [
      {
        axisLine: { // 坐标轴线
          lineStyle: { // 属性lineStyle控制线条样式
            color: [
              [0.3, '#35CCCC'], [0.6, '#259999'], [1, '#156363']
            ]
          }
        },
        max: 100,
        min: 0,
        name: '数值',
        type: 'gauge',
        detail: {formatter: '{value}'},
        data: [{value: '87'}
        ]
      }
    ]
  },
  value: { // 数值
    name: '数值',
    value: '100000'
  },
  line: { // 折线图
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    legend: {
      left: 'left'
    },
    xAxis: [{
      type: 'category',
      data: ['横1', '横2', '横3', '横4', '横5', '横6'],
      boundaryGap: false,
      'axisLabel': {
        'fontSize': 12,
        'rotate': 0
      }
    }],
    yAxis: {
      type: 'value'
    },
    label: {
      'show': true,
      'position': 'top',
      'padding': [0, 0, 5, 10]
    },
    series: [{
      name: '数值',
      data: [5, 20, 36, 10, 15, 20],
      type: 'line'
    }],
    'grid': {
      'left': 50,
      'top': 55,
      'bottom': 50,
      'right': 50,
      'containLabel': true
    },
    'dataZoom': [
      {
        'type': 'inside'
      }
    ]
  },
  area: { // 面积图
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    legend: {
      left: 'left'
    },
    xAxis: [{
      type: 'category',
      boundaryGap: false,
      data: ['横1', '横2', '横3', '横4', '横5', '横6'],
      'axisLabel': {
        'fontSize': 12,
        'rotate': 0
      }
    }],
    yAxis: {
      type: 'value'
    },
    label: {
      'show': true,
      'position': 'top',
      'padding': [0, 0, 5, 10]
    },
    series: [{
      name: '数值',
      data: [5, 20, 36, 10, 15, 20],
      type: 'line',
      areaStyle: {}
    }],
    'grid': {
      'left': 50,
      'top': 55,
      'bottom': 50,
      'right': 50,
      'containLabel': true
    },
    'dataZoom': [
      {
        'type': 'inside'
      }
    ]
  }
}
// 图表的颜色
const chartStyleList = [
  { background: ['#35CCCC', '#259999', '#156363', '#4671D5', '#6C8CD5', '#1240AB', '#2A4480', '#FCB274'], selected: true },
  { background: ['#FF4D4F', '#73D13D', '#13C2C2', '#FFEC3D', '#9254DE', '#F759AB', '#2F54EB', '#FFA940'], selected: false },
  { background: ['#6B0848', '#A40A3C', '#EC610A', '#FDC300', '#E61C5D', '#930B77', '#3B0E88', '#726A95'], selected: false },
  { background: ['#FF0003', '#FFEB00', '#06A722', '#122022', '#1322C2', '#F47F00', '#E70FF3', '#409FFF'], selected: false },
  { background: ['#307672', '#1E8494', '#3DDEAD', '#51ADCD', '#A5ECD7', '#259577', '#51ADCF', '#76B39E'], selected: false },
  { background: ['#37A2DA', '#1DCEFA', '#67E0E3', '#9FE6B8', '#FEDC5C', '#FB9F7F', '#F97293', '#E062AE'], selected: false },
  { background: ['#91BDFF', '#8278EA', '#9D96F5', '#E7BCF3', '#E690D1', '#E062AE', '#F97293', '#FB9F7F'], selected: false },
  { background: ['#DD6B66', '#759AA0', '#E69D87', '#8DC1A9', '#EA7E52', '#EEDD77', '#73A373', '#73B9BC'], selected: false },
  { background: ['#F49F41', '#91CA8C', '#7289AB', '#73B9BC', '#73A373', '#EEDD77', '#E97B4E', '#E69D87'], selected: false },
  { background: ['#546570', '#6E7074', '#BDA29A', '#CA8622', '#749F83', '#91C7AE', '#D48165', '#61A0A8'], selected: false },
  { background: ['#5187BA', '#DF585C', '#F0953A', '#95C7E4', '#F6BD8A', '#5FA755', '#C6CDD5', '#8186EC'], selected: false },
  { background: ['#219FEA', '#FBA23E', '#39CE54', '#F65646', '#B361FB', '#FBD842', '#56E0ED', '#DE9BF2'], selected: false },
  { background: ['#B056D2', '#EB3875', '#E59594', '#E9508A', '#BB71AD', '#D45A8B', '#F37366', '#DE9BF2'], selected: false }
]
const projecChart = {
  statusOption: {
    'tooltip': {
      'trigger': 'axis',
      'axisPointer': {
        'type': 'shadow'
      }
    },
    'color': ['#FCC064', '#65A6F2', '#55D0C6', '#FB7293'],
    'legend': {
      'left': 'center'
    },
    'xAxis': {
      'type': 'category',
      'data': ['列1', '列2', '列3', '列4', '列5', '列6'],
      'axisLabel': {
        'rotate': 0,
        'color': '#666666'
      },
      'axisLine': {
        'lineStyle': {
          'color': '#D8D8D8'
        }
      },
      'axisTick': {show: false}
    },
    'yAxis': {
      'axisLabel': {
        'rotate': 0,
        'color': '#666666'
      },
      'axisLine': {
        'lineStyle': {
          'color': '#D8D8D8'
        }
      },
      'axisTick': {show: false},
      'splitLine': {
        'lineStyle': {
          'type': 'dashed'
        }
      }
    },
    'series': [
      {
        'data': [5, 20, 36, 10, 15, 20],
        'name': '新增任务',
        'type': 'bar',
        'label': {
          'show': false
        },
        'barMaxWidth': 50,
        'barMinHeight': 5,
        'taskType': 'add'
      },
      {
        'data': [5, 20, 36, 10, 15, 20],
        'name': '进行中',
        'type': 'bar',
        'label': {
          'show': false
        },
        'barMaxWidth': 50,
        'barMinHeight': 5,
        'taskType': 'ongoing'
      },
      {
        'data': [5, 20, 36, 10, 15, 20],
        'name': '已完成任务',
        'type': 'bar',
        'label': {
          'show': false
        },
        'barMaxWidth': 50,
        'barMinHeight': 5,
        'taskType': 'done'
      },
      {
        'data': [5, 20, 36, 10, 15, 20],
        'name': '延期任务',
        'type': 'bar',
        'label': {
          'show': false
        },
        'barMaxWidth': 50,
        'barMinHeight': 5,
        'taskType': 'delay'
      }
    ],
    'dataZoom': [
      {
        'type': 'inside'
      }
    ],
    'grid': {
      'left': 50,
      'top': 50,
      'bottom': 50,
      'right': 50,
      'containLabel': true
    }
  },
  topOption: {
    'grid': {
      'left': 0,
      'top': 5,
      'bottom': 20,
      // 'right': 50,
      'containLabel': true
    },
    color: ['#55D0C6', '#65A6F2', '#55D0C6', '#FB7293'],
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      show: false
    },
    xAxis: {
      axisLine: {
        lineStyle: {
          color: '#D8D8D8'
        }
      },
      axisTick: {show: false},
      'axisLabel': {
        'rotate': 0,
        color: '#666666'
      }
    },
    yAxis: {
      type: 'category',
      data: ['张大仙发', '莫凡', '曹建华', '横4', '横5'],
      'axisLabel': {
        'rotate': 0,
        color: '#666666',
        // margin: 20,
        // align: 'right',
        padding: [0, 50, 0, 0]
      },
      axisTick: {show: false},
      axisLine: {
        lineStyle: {
          color: '#D8D8D8'
        }
      }
    },
    series:
      {
        name: '数量',
        type: 'bar',
        data: [5, 20, 36, 10, 15],
        label: {
          'show': true,
          'position': 'right'
        },
        'barMaxWidth': 50,
        'barMinHeight': 5
        // barWidth: 15
      }
  }
}
export { chartParams, chartStyleList, projecChart }
