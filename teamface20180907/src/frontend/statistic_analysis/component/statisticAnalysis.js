// import {utilClass} from '@/common/js/tool.js'
import {geoCoordMap} from '@/common/js/geoCoordMap.js'

function traverseField (params) {
  if (!params) return
  var label = params.label
  var perArray = ['按人', '按部门']
  var dateArray = ['按年', '按半年', '按季度', '按月', '按日']
  var locationArray = ['无', '按省', '按市', '按区']
  if (params.type.indexOf('personnel') >= 0) {
    label = label + '（' + perArray[parseInt(params.format)] + '）'
  } else if (params.type.indexOf('datetime') >= 0) {
    label = label + '（' + dateArray[parseInt(params.format)] + '）'
  } else if (params.type.indexOf('location') >= 0) {
    label = label + '（' + locationArray[parseInt(params.format)] + '）'
  }
  return label
}

/** 下拉切换（日期、人物） */
function filedCommand (event, index, columndFieldList) {
  columndFieldList[index].format = event
  // traverseField(columndFieldList[index])
  return columndFieldList
  // traverseField(columndFieldList)
}
// 处理仪表盘数据(发送前)
function handleChartData (data) {
  data.chartList.map((item, index) => {
    item.option.backgroundColor = data.chart_bgcolor
    item.option.color = data.chart_color
    switch (item.type) {
      case '0': // 柱状图
        let len0 = item.dimensionFields.length
        if (len0 === 2 && item.option.xAxis.length !== 2) {
          let Obj0 = {
            'type': 'category',
            'data': [],
            // 'position': 'bottom',
            'offset': 13,
            'axisTick': {
              'show': false
              // 'length': 50
            },
            'axisLabel': {
              'rotate': 0
            }
          }
          item.option.xAxis.push(Obj0)
        } else if (len0 === 1) {
          item.option.xAxis.splice(1, 1)
        }
        item.option.series.map((item0, index0) => {
          item.showVal ? item0.label.show = true : item0.label.show = false
        })
        let valueObj0 = item.option.series[0]
        item.option.series = []
        // let valueObj0 = {'data': [], 'name': '', 'type': 'bar'}
        item.valueFields.map((item0, index0) => {
          valueObj0.name = item0.label
          item.option.series.push(JSON.parse(JSON.stringify(valueObj0)))
        })
        break
      case '1': // 堆叠柱状图
        let len1 = item.dimensionFields.length
        if (len1 === 2 && item.option.xAxis.length !== 2) {
          let Obj1 = {
            'type': 'category',
            'data': [],
            // 'position': 'bottom',
            'offset': 13,
            'axisTick': {
              'show': false
              // 'length': 50
            },
            'axisLabel': {
              'rotate': 0
            }
          }
          item.option.xAxis.push(Obj1)
        } else if (len1 === 1) {
          item.option.xAxis.splice(1, 1)
        }
        item.option.series.map((item1, index1) => {
          item.showVal ? item1.label.show = true : item1.label.show = false
        })
        let valueObj1 = item.option.series[0]
        item.option.series = []
        // let valueObj1 = { 'data': [], 'name': '', 'type': 'bar', 'stack': 'stackbar' }
        item.valueFields.map((item1, index1) => {
          valueObj1.name = item1.label
          item.option.series.push(JSON.parse(JSON.stringify(valueObj1)))
        })
        break
      case '2': // 条形图
        let len2 = item.dimensionFields.length
        if (len2 === 2 && item.option.yAxis.length !== 2) {
          let Obj2 = {
            'type': 'category',
            'data': [],
            // 'position': '',
            'offset': 13,
            'axisTick': {
              'show': false
              // 'length': 70
            },
            'axisLabel': {
              'rotate': 0
            }
          }
          item.option.yAxis.push(Obj2)
        } else if (len2 === 1) {
          item.option.yAxis.splice(1, 1)
        }
        item.option.series.map((item2, index2) => {
          item.showVal ? item2.label.show = true : item2.label.show = false
        })
        let valueObj2 = item.option.series[0]
        item.option.series = []
        // let valueObj2 = { 'data': [], 'name': '', 'type': 'bar' }
        item.valueFields.map((item2, index2) => {
          valueObj2.name = item2.label
          console.log(valueObj2, 'valueObj2')
          item.option.series.push(JSON.parse(JSON.stringify(valueObj2)))
        })
        break
      case '3': // 堆叠条形图
        if (item.dimensionFields.length === 2 && item.option.yAxis.length !== 2) {
          let Obj2 = {
            'type': 'category',
            'data': [],
            // 'position': '',
            'offset': 13,
            'axisTick': {
              'show': false
              // 'length': 70
            },
            'axisLabel': {
              'rotate': 0
            }
          }
          item.option.yAxis.push(Obj2)
        } else if (len2 === 1) {
          item.option.yAxis.splice(1, 1)
        }
        item.option.series.map((item3, index3) => {
          item.showVal ? item3.label.show = true : item3.label.show = false
        })
        let valueObj3 = item.option.series[0]
        item.option.series = []
        // let valueObj3 = { 'data': [], 'name': '', 'type': 'bar', 'stack': 'stripbar' }
        item.valueFields.map((item3, index3) => {
          valueObj3.name = item3.label
          item.option.series.push(JSON.parse(JSON.stringify(valueObj3)))
        })
        break
      case '4': // 散点图
        let len4 = item.dimensionFields.length
        if (len4 === 2 && item.option.xAxis.length !== 2) {
          let Obj4 = {
            'type': 'category',
            'data': [],
            'offset': 13,
            'axisTick': {
              'show': false
            },
            'axisLabel': {
              'rotate': 0
            }
          }
          item.option.xAxis.push(Obj4)
        } else if (len4 === 1) {
          item.option.xAxis.splice(1, 1)
        }
        item.option.series.map((item4, index4) => {
          item.showVal ? item4.label.show = true : item4.label.show = false
        })
        let valueObj4 = item.option.series[0]
        item.option.series = []
        item.valueFields.map((item4, index4) => {
          valueObj4.name = item4.label
          item.option.series.push(JSON.parse(JSON.stringify(valueObj4)))
        })
        break
      case '5': // 饼图
        // item.option.series.data = []
        // let valueObj5 = { name: '', value: '' }
        item.valueFields.map((item5, index5) => {
          item.option.series[0].name = item5.label
          // item.option.series.data.push(valueObj5)
        })
        // item.option.series.map()
        if (item.showVal && item.showPercentage) {
          item.option.series[0].label.formatter = '{b} : {c} ({d}%)'
        } else if (item.showVal && !item.showPercentage) {
          item.option.series[0].label.formatter = '{b} : {c}'
        } else if (!item.showVal && item.showPercentage) {
          item.option.series[0].label.formatter = '{b} : {d}%'
        } else {
          item.option.series[0].label.formatter = '{b}'
        }
        break
      case '6': // 饼图2
        // item.option.series.data = []
        // let valueObj5 = { name: '', value: '' }
        item.valueFields.map((item6, index6) => {
          item.option.series[0].name = item6.label
          // item.option.series.data.push(valueObj5)
        })
        if (item.showVal && item.showPercentage) {
          item.option.series[0].label.formatter = '{b} : {c} ({d}%)'
        } else if (item.showVal && !item.showPercentage) {
          item.option.series[0].label.formatter = '{b} : {c}'
        } else if (!item.showVal && item.showPercentage) {
          item.option.series[0].label.formatter = '{b} : {d}%'
        } else {
          item.option.series[0].label.formatter = '{b}'
        }
        break
      case '7': // 环形图
        item.valueFields.map((item7, index7) => {
          item.option.series[0].name = item7.label
        })
        if (item.showVal && item.showPercentage) {
          item.option.series[0].label.formatter = '{b} : {c} ({d}%)'
        } else if (item.showVal && !item.showPercentage) {
          item.option.series[0].label.formatter = '{b} : {c}'
        } else if (!item.showVal && item.showPercentage) {
          item.option.series[0].label.formatter = '{b} : {d}%'
        } else {
          item.option.series[0].label.formatter = '{b}'
        }
        break
      case '8': // 漏斗图
        item.valueFields.map((item7, index7) => {
          item.option.series[0].name = item7.label
        })
        if (item.showVal && item.showPercentage) {
          item.option.series[0].label.formatter = '{b} : {c} ({d}%)'
        } else if (item.showVal && !item.showPercentage) {
          item.option.series[0].label.formatter = '{b} : {c}'
        } else if (!item.showVal && item.showPercentage) {
          item.option.series[0].label.formatter = '{b} : {d}%'
        } else {
          item.option.series[0].label.formatter = '{b}'
        }
        break
      case '9': // 地图
        item.option.visualMap.inRange.color = data.chart_color
        if (item.dimensionFields[0].name.includes('province') || item.dimensionFields[0].format === '1') { // 省的情况
          let mapObj = {
            name: '',
            type: 'map',
            // mapType: 'china',
            geoIndex: 0,
            label: {
              normal: {
                show: false
              },
              emphasis: {
                show: false
              }
            },
            roam: false,
            data: [{
              name: '广东',
              value: '2000'
            }]
          }
          item.option.series = []
          item.option.series.push(mapObj)
        } else {
          let mapObj2 = {
            name: '',
            type: 'scatter',
            coordinateSystem: 'geo',
            roam: false,
            symbolSize: 10,
            label: {
              show: false,
              normal: {
                show: false,
                formatter: '{b}',
                position: 'right'
              }
            },
            data: [
              {
                name: '',
                value: '',
                district: [ // 区
                ]}
            ]
          }
          item.option.series = []
          item.option.series.push(mapObj2)
        }
        item.valueFields.map((item9, index9) => {
          item.option.series[index9].name = item9.label
        })
        break
      case '10': // 仪表图
        item.option.series.map((chart, index) => {
          chart.min = parseInt(item.min_value)
          chart.max = parseInt(item.max_value)
          let temVal = chart.max - chart.min
          chart.axisLine.lineStyle.color = []
          if (item.target_value.length !== 0) {
            item.target_value.map((target, idx) => {
              console.log(target / temVal, '颜色')
              let tarColor = [(target / temVal).toString(), item.option.color[idx]]
              console.log(tarColor, '目标颜色')
              chart.axisLine.lineStyle.color.push(tarColor)
              console.log(chart.axisLine.lineStyle.color, '处理后')
            })
          }
          let len = item.target_value.length
          chart.axisLine.lineStyle.color.push(['1', item.option.color[len]])
          /** modify 2018-6-19 */
          item.valueFields.map((valueItem, valueIndex) => {
            chart.name = valueItem.label
          })
        })
        break
      case '11': // 数值
        break
      case '12': // 折线图
        let len12 = item.dimensionFields.length
        if (len12 === 2 && item.option.xAxis.length !== 2) {
          let Obj12 = {
            'type': 'category',
            'data': [],
            'offset': 13,
            'axisTick': {
              'show': false
            },
            'axisLabel': {
              'rotate': 0
            },
            boundaryGap: false
          }
          item.option.xAxis.push(Obj12)
        } else if (len12 === 1) {
          item.option.xAxis.splice(1, 1)
        }
        item.showVal ? item.option.label.show = true : item.option.label.show = false
        let valueObj12 = item.option.series[0]
        item.option.series = []
        // let valueObj12 = {'data': [], 'name': '', 'type': 'line'}
        item.valueFields.map((item12, index12) => {
          valueObj12.name = item12.label
          item.option.series.push(JSON.parse(JSON.stringify(valueObj12)))
        })
        break
      case '13': // 面积图
        let len13 = item.dimensionFields.length
        if (len13 === 2 && item.option.xAxis.length !== 2) {
          let Obj0 = {
            'type': 'category',
            'data': [],
            'offset': 25,
            'axisTick': {
              'show': false
            },
            'axisLabel': {
              'rotate': 0
            },
            boundaryGap: false
          }
          item.option.xAxis.push(Obj0)
        } else if (len13 === 1) {
          item.option.xAxis.splice(1, 1)
        }
        item.showVal ? item.option.label.show = true : item.option.label.show = false
        let valueObj13 = item.option.series[0]
        item.option.series = []
        // let valueObj13 = {'data': [], 'name': '', 'type': 'line'}
        item.valueFields.map((item13, index13) => {
          valueObj13.name = item13.label
          item.option.series.push(JSON.parse(JSON.stringify(valueObj13)))
        })
        break

      default:
        break
    }
  })
  return data
}
// 处理后台返回的的数据(发送后)
function handleReceiveData (data) {
  switch (data.type) {
    case '0': case '1': case '4': case '12': case '13': // 柱状图，堆叠柱状图，折线图，面积图,散点图
      data.option.xAxis.map((x0, xIndex) => {
        let isPlus = x0.data.some((item, itemIndex) => { // 判断是否有超过6个字的横坐标
          return item.length > 6
        })
        if (isPlus) {
          x0.axisLabel.rotate = 40
          x0.axisLabel.formatter = function (value, index) {
            if (value.length > 6) {
              value = value.substring(0, 6) + '...'
            }
            return value
          }
        } else {
          x0.axisLabel.rotate = 0
        }
      })
      console.log(data, '柱状图')
      break

    //   break
    case '2': case '3':// 条形图,堆叠条形图
      data.option.yAxis.map((x2, xIndex) => {
        let isPlus = x2.data.some((item, itemIndex) => { // 判断是否有超过6个字的横坐标
          return item.length > 6
        })
        if (isPlus) {
          x2.axisLabel.rotate = 40
          x2.axisLabel.formatter = function (value, index) {
            if (value.length > 6) {
              value = value.substring(0, 6) + '...'
            }
            return value
          }
        } else {
          x2.axisLabel.rotate = 0
        }
      })
      break
    case '5': // 饼图

      break
    case '6': // 饼图

      break
    case '7': // 环形图

      break
    case '8': // 漏斗图

      break
    case '9': // 地图

      break
    case '10': // 仪表图

      break
    case '11': // 数值
      break
    // case '12': // 折线图

    //   break
    // case '13': // 面积图

    //   break

    default:
      break
  }
  return data
}
// 字段去重
function filterDuplicate (dataArr, x) { // dataArr: 要去重的数组 // x：维度为true, y: 数值为false
  dataArr = JSON.parse(JSON.stringify(dataArr))
  let flag = dataArr[0].bean.substring(0, 16)
  let hash1 = {}
  let hash2 = {}
  let newValueFields = []
  let isMulti = false
  JSON.parse(JSON.stringify(dataArr)).map((item, index) => { // 判断来源的模块 去重
    if (item.type.indexOf('datetime') >= 0) {
      item.format = item.format || 4
    }
    if (item.type.indexOf('personnel') >= 0 || item.type.indexOf('location') >= 0) {
      item.format = item.format || 0
    }
    if (item.bean.includes(flag)) {
      if (!hash1[item.name]) {
        hash1[item.name] = true
        newValueFields.push(item)
      }
    } else {
      if (!hash2[item.name]) {
        hash2[item.name] = true
        newValueFields.push(item)
        isMulti = true
      }
    }
  })
  if (isMulti) {
    newValueFields.map((item, index) => {
      if (!(item.label.includes(item.title + '.'))) {
        item.label = item.title + '.' + item.label
      }
    })
  } else {
    newValueFields.map((item, index) => {
      if (item.label.includes(item.title + '.')) {
        let _index = item.label.indexOf('.')
        let len = item.label.length
        item.label = item.label.substring(_index + 1, len)
      }
    })
  }
  return newValueFields
}
// 处理省市区、定位数据
/** @params mapdata: 省市区数据 */
function handleMapData (chartData) {
  console.log(chartData, '要处理的地图数据')
  // let isProvince = false
  chartData.option.series.map((item9, index9) => {
    let res = []
    item9.data.map((geoItem, geoIndex) => {
      // if (geoItem.name.includes('内蒙古') || geoItem.name.includes('黑龙江')) {
      //   geoItem.name = geoItem.name.substring(0, 3)
      // } else {
      //   geoItem.name = geoItem.name.substring(0, 2)
      // }
      // 处理坐标
      if ((chartData.dimensionFields[0].name.includes('province') || chartData.dimensionFields[0].format === '1')) { // 省的情况
        // let val
        // if (typeof geoItem.value === 'string') {
        //   val = geoItem.value
        // } else if (geoItem.value instanceof Array) {
        //   val = geoItem.value.pop()
        // }
        // let geoCoord = geoCoordMap[geoItem.name]
        // if (geoCoord) {
        //   res.push({
        //     name: geoItem.name,
        //     value: geoCoord.concat(val),
        //     district: geoItem.district
        //   })
        // }
        // item9.data = res
        if (geoItem.name.includes('内蒙古') || geoItem.name.includes('黑龙江')) {
          geoItem.name = geoItem.name.substring(0, 3)
        } else {
          geoItem.name = geoItem.name.substring(0, 2)
        }
      } else { // 市、区的情况
        let val
        if (typeof geoItem.value === 'string') {
          val = geoItem.value
        } else if (geoItem.value instanceof Array) {
          val = geoItem.value.pop()
        }
        let geoCoord = geoCoordMap[geoItem.name]
        if (geoCoord) {
          res.push({
            name: geoItem.name,
            value: geoCoord.concat(val),
            district: geoItem.district
          })
        }
        item9.data = res
      }
    })
  })
  if (chartData.dimensionFields[0].name.includes('province') || chartData.dimensionFields[0].format === '1') { // 省的情况
  } else {
    chartData.option.tooltip.formatter = function (params) {
      console.log(params, '8888888888')
      let res = `${params.name}`
      // TODO, 处理是区的情况
      if (params.data.district) { // 区的情况
        params.data.district.map((disItem, index) => {
          res += `</br>${disItem.label} : ${disItem.value}`
        })
      } else {
        res += ` : ${params.value[2]}` // 市的情况
      }
      // console.log(res, 'res')
      return res
    }
  }
  return chartData
}
export {traverseField, filedCommand, handleChartData, filterDuplicate, handleMapData, handleReceiveData}
