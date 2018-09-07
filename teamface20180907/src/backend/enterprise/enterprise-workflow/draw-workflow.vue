<template>
  <div class="draw-box">
    <!-- <i class="iconfont icon-yulan preview" v-show="this.from === 'view'" @click="hanleFullScreen"></i> -->
    <div class="handle-box flex-box" v-show="this.from === 'edit'">
      <div class="flex-box palette">
        <i class="iconfont icon-Rectangle1"></i>
        <div id= "PaletteDiv1" class=""></div>
      </div>
      <div class="flex-box palette">
        <i class="iconfont icon-Rectangle1"></i>
        <div id= "PaletteDiv2" class="palette"></div>
      </div>
    <div class="handle-btn">
      <el-button type="text" icon="el-icon-refresh" @click="handleReset()">重置</el-button>
      <el-button type="text" icon="iconfont icon-shenpi-chexiao" @click="handleUndo()">撤销</el-button>
      <el-button type="text" icon="el-icon-delete" @click="handleDeleteNode()">删除</el-button>
      </div>
    </div>
    <div class="drawDiv" :class=" {showDiv: this.from === 'view', editDiv: this.from === 'edit'}">
      <div id="drawDiv">

      </div>
      <!-- <div id="drawFullDiv">

      </div> -->
    </div>
  </div>
</template>
<script>
import go from 'gojs'
import { GuidedDraggingTool } from '@/common/js/GuidDragginTool.js'
export default {
  name: 'drawTool',
  props: ['draw', 'from', 'isFull'],
  data () {
    return {
      process: null,
      $$: null, // 初始化Go对象
      Palette1: null, // 初始化拖动面板
      palette2: null, // 拖动面板
      drawData: this.draw
    }
  },
  methods: {
    // 初始化流程图
    initProcess () {
      this.$$ = go.GraphObject.make
      this.process = this.$$(go.Diagram, 'drawDiv',
        {
          'initialContentAlignment': go.Spot.Center,
          'allowDrop': true, // must be true to accept drops from the Palette
          'LinkDrawn': this.showLinkLabel, // this DiagramEvent listener is defined below
          'LinkRelinked': this.showLinkLabel,
          'scrollsPageOnFocus': true,
          'undoManager.isEnabled': true, // enable undo & redo
          'draggingTool': new GuidedDraggingTool(),
          'allowDelete': true,
          'isEnabled': this.from === 'edit',
          'ModelChanged': (e) => { // 保存并更新视图
            if (e.isTransactionFinished) {
              console.log(e.model.nodeDataArray, e.model.linkDataArray, '初始化......')
            }
          }
        })
      this.process.nodeTemplateMap.add('', // 默认的分类
        this.$$(go.Node, 'Spot', this.setNodeStyle(),
        // the main object is a Panel that surrounds a TextBlock with a rectangular Shape
          {selectable: true, selectionAdornmentTemplate: this.nodeSelectionAdornmentTemplate()},
          this.$$(go.Panel, 'Auto',
            this.$$(go.Shape, 'Rectangle',
              { fill: '#fff', stroke: null, name: 'Icon' },
              new go.Binding('figure', 'figure')),
            this.$$(go.TextBlock,
              {
                font: 'normal 14px  Helvetica Neue, PingFang SC, Microsoft Yahei',
                margin: 8,
                maxSize: new go.Size(160, NaN),
                wrap: go.TextBlock.WrapFit,
                editable: true
                // isUnderline: true
              },
              new go.Binding('text').makeTwoWay())
          ),
          // four named ports, one on each side:
          this.makeNodePort('T', go.Spot.Top, false, true),
          this.makeNodePort('L', go.Spot.Left, true, true),
          this.makeNodePort('R', go.Spot.Right, true, true),
          this.makeNodePort('B', go.Spot.Bottom, true, false)
        ))
      this.process.nodeTemplateMap.add('Start', // 开始节点
        this.$$(go.Node, 'Spot', this.setNodeStyle(),
          {selectable: true, selectionAdornmentTemplate: this.nodeSelectionAdornmentTemplate()},
          this.$$(go.Panel, 'Auto',
            this.$$(go.Shape, 'Circle',
              { minSize: new go.Size(50, 50), fill: '#ffffff', stroke: null, name: 'Icon' }),
            this.$$(go.TextBlock, 'Start',
              { font: 'normal 14px  Helvetica Neue, PingFang SC, Microsoft Yahei', stroke: '#3F84E9', editable: false },
              new go.Binding('text').makeTwoWay())
          ),
          // three named ports, one on each side except the top, all output only:
          this.makeNodePort('L', go.Spot.Left, true, false),
          this.makeNodePort('R', go.Spot.Right, true, false),
          this.makeNodePort('B', go.Spot.Bottom, true, false)
        ))
      this.process.nodeTemplateMap.add('End', // 结束节点
        this.$$(go.Node, 'Spot', this.setNodeStyle(),
          {selectable: true, selectionAdornmentTemplate: this.nodeSelectionAdornmentTemplate()},
          this.$$(go.Panel, 'Auto',
            this.$$(go.Shape, 'Circle',
              { minSize: new go.Size(50, 50), fill: '#ffffff', stroke: null, name: 'Icon' }),
            this.$$(go.TextBlock, 'End',
              { font: 'normal 14px  Helvetica Neue, PingFang SC, Microsoft Yahei', editable: false, name: '' },
              new go.Binding('text').makeTwoWay())
          ),
          // three named ports, one on each side except the bottom, all input only:
          this.makeNodePort('T', go.Spot.Top, false, true),
          this.makeNodePort('L', go.Spot.Left, false, true),
          this.makeNodePort('R', go.Spot.Right, false, true)
        ))
      this.initPalette()
      this.initLinkTemplate()
      this.process.model = new go.GraphLinksModel(this.drawData.nodeDataArray, this.drawData.linkDataArray)
      this.process.model.linkFromPortIdProperty = 'fromPort'
      this.process.model.linkToPortIdProperty = 'toPort'
      console.log(this.drawData, '初始化流程图')
    },
    // 初始化连接线样式
    initLinkTemplate () {
      this.process.linkTemplate =
        this.$$(go.Link,
          {
            routing: go.Link.AvoidsNodes,
            curve: go.Link.JumpOver,
            corner: 5,
            toShortLength: 4,
            relinkableFrom: true,
            relinkableTo: true,
            reshapable: true,
            resegmentable: true,
            // mouse-overs subtly highlight links:
            mouseEnter: function (e, link) { link.findObject('HIGHLIGHT').stroke = 'rgba(30,144,255,0.2)' },
            mouseLeave: function (e, link) { link.findObject('HIGHLIGHT').stroke = 'transparent' }
          },
          new go.Binding('points').makeTwoWay(),
          this.$$(go.Shape, // the highlight shape, normally transparent
            { isPanelMain: true, strokeWidth: 8, stroke: 'transparent', name: 'HIGHLIGHT' }),
          this.$$(go.Shape, // the link path shape
            { isPanelMain: true, stroke: '#B3BFCB', strokeWidth: 2 }),
          this.$$(go.Shape, // 箭头
            {toArrow: 'standard', stroke: null, fill: '#B3BFCB'}),
          this.$$(go.Panel, 'Auto', // the link label, normally not visible
            {visible: false, name: 'LABEL', segmentIndex: 2, segmentFraction: 0.5},
            new go.Binding('visible', 'visible').makeTwoWay(),
            this.$$(go.Shape, 'RoundedRectangle', // 线上的字
              { fill: '#F8F8F8', stroke: null }),
            this.$$(go.TextBlock, 'Y',
              {
                textAlign: 'center',
                // font: '10pt helvetica, arial, sans-serif',
                stroke: '#333333',
                editable: true
              },
              new go.Binding('text').makeTwoWay())
          )
        )
    },
    // 初始化拖动面板
    initPalette () {
      let initTime = new Date().getTime()
      this.palette1 = this.$$(go.Palette, 'PaletteDiv1', // must name or refer to the DIV HTML element
        {
          scrollsPageOnFocus: false,
          'animationManager.isEnabled': false,
          // nodeTemplateMap: this.process.nodeTemplateMap, // share the templates used by myDiagram
          model: new go.GraphLinksModel([ // specify the contents of the Palette
            // {category: 'Start', text: 'Start', key: `start${initTime}`},
            {text: '添加节点', key: `step${initTime}`}
            // {text: '添加条件', figure: 'Diamond', key: `judge${initTime}`}
            // {category: 'End', text: 'End', key: `end${initTime}`}
            // { category: 'Comment', text: 'Comment' }
          ])
        })
      this.palette1.nodeTemplate =
        this.$$(go.Node, 'Spot',
          this.$$(go.TextBlock, { cursor: 'move', verticalAlignment: go.Spot.Center, height: 28, stroke: '#69696C', font: 'normal 14px Helvetica Neue, PingFang SC, Microsoft Yahei', textAlign: 'right' },
            new go.Binding('text', 'text')),
          { locationSpot: go.Spot.Center },
          {
            selectionAdorned: false // don't bother with any selection adornment
            // selectionChanged: this.onSelectionChanged
          }
        )
      this.palette2 = this.$$(go.Palette, 'PaletteDiv2', // must name or refer to the DIV HTML element
        {
          scrollsPageOnFocus: false,
          'animationManager.isEnabled': false,
          nodeTemplateMap: this.palette1.nodeTemplateMap, // share the templates used by myDiagram
          model: new go.GraphLinksModel([ // specify the contents of the Palette
            // {category: 'Start', text: 'Start', key: `start${initTime}`},
            // {text: '添加节点', key: `step${initTime}`}
            {text: '添加条件', figure: 'Diamond', key: `judge${initTime}`}
            // {category: 'End', text: 'End', key: `end${initTime}`}
            // {category: 'Comment', text: 'Comment' }
          ])
        })
    },
    // 流程节点样式
    setNodeStyle () {
      return [
        // The Node.location comes from the "loc" property of the node data,
        // converted by the Point.parse static method.
        // If the Node.location is changed, it updates the "loc" property of the node data,
        // converting back using the Point.stringify static method.
        new go.Binding('location', 'loc', go.Point.parse).makeTwoWay(go.Point.stringify),
        {
          // the Node.location is at the center of each node
          locationSpot: go.Spot.Center,
          isShadowed: true,
          shadowColor: '#ddd',
          // shadowOffset: {x: 2, y: 2},
          shadowBlur: 1,
          // handle mouse enter/leave events to show/hide the ports
          mouseEnter: (e, obj) => { this.showPorts(obj.part, true) },
          mouseLeave: (e, obj) => { this.showPorts(obj.part, false) }
        },
        {selectionChanged: this.onSelectionChanged}
        // { selectionAdorned: this.nodeSelectionAdornmentTemplate }// don't bother with any selection adornment
      ]
    },
    // 拖动提示点
    makeNodePort (name, spot, output, input) {
      // the port is basically just a small circle that has a white stroke when it is made visible
      return this.$$(go.Shape, 'Rectangle',
        {
          fill: 'transparent',
          stroke: null, // this is changed to "white" in the showPorts function
          desiredSize: new go.Size(6, 6),
          alignment: spot,
          alignmentFocus: spot, // align the port on the main Shape
          portId: name, // declare this object to be a "port"
          fromSpot: spot,
          toSpot: spot, // declare where links may connect at this port
          fromLinkable: output,
          toLinkable: input, // declare whether the user may draw links to/from here
          cursor: 'pointer' // show a different cursor to indicate potential link point CFF0FC
        })
    },
    // 是否展示拖动提示点
    showPorts (node, show) {
      let diagram = node.diagram
      if (!diagram || diagram.isReadOnly || !diagram.allowLink) return
      node.ports.each(function (port) {
        port.fill = (show ? '#8BC0FF' : null)
      })
    },
    // 点击删除节点
    handleDeleteNode () {
      if (!this.process.allowDelete) {
        this.$message({type: 'warning', showClose: true, message: '初始化节点不能删除！'})
        return
      }
      this.process.commandHandler.deleteSelection()
    },
    // 点击撤销
    handleUndo () {
      this.process.commandHandler.undo()
    },
    // 控制线上的文字是否展示
    showLinkLabel (e) {
      let label = e.subject.findObject('LABEL')
      if (label !== null) label.visible = (e.subject.fromNode.data.figure === 'Diamond')
    },
    // 选中样式
    nodeSelectionAdornmentTemplate () {
      let AdornmentTemplate = this.$$(go.Adornment, 'Auto',
        this.$$(go.Shape, { fill: null, stroke: 'deepskyblue', strokeWidth: 1.5, strokeDashArray: [4, 2] }), this.$$(go.Placeholder)
      )
      return AdornmentTemplate
    },
    //  改变选择
    onSelectionChanged (node) {
      console.log(node, '改变选择')
      /* *modify by 2018-2-6 */
      // this.currentKey = node.data.key
      if (node.isSelected) { // 处理字段数据互相影响
        if (node.data.key === 'start' || node.data.key === 'end') {
          this.process.allowDelete = false
        } else {
          this.process.allowDelete = true
        }
      }
    },
    // 点击重置
    handleReset () {
      this.$confirm('重置后流程会恢复初始化样式，已设置好的工作流会被还原。你确定要重置吗？', '', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.drawData.nodeDataArray = [
          {'category': 'Start', 'text': '开始', 'key': 'start', 'loc': '0 0'},
          {'category': 'End', 'text': '结束', 'key': 'end', 'loc': '0 400'}
        ]
        this.drawData.linkDataArray = [
          {'from': 'start', 'to': 'end', 'fromPort': 'B', 'toPort': 'T'}
        ]
        this.process.model = new go.GraphLinksModel(this.drawData.nodeDataArray, this.drawData.linkDataArray)
      })
    }
  },
  mounted () {
    // this.initProcess()
    this.initProcess()
  },
  watch: {
    draw (val) {
      console.log(val, 'bianbainhbain')
      this.drawData = val
      this.initProcess()
    },
    isFull (val) {
      this.process.requestUpdate()
      this.process.model = new go.GraphLinksModel(this.drawData.nodeDataArray, this.drawData.linkDataArray)
    }
  }
}
</script>
<style lang="scss">
  .draw-box {
    height: 100%;
    width: 100%;
    position: relative;
    .drawDiv {
      background: #F5F6F7;
      // height: calc(100% - 40px);
      #drawDiv,#drawFullDiv {
        height: 100%;
        canvas {
          &:focus {
            outline: none;
          }
        }
      }
    }
    .showDiv {
      height: 100%;
    }
    .editDiv {
      height: calc(100% - 40px)
    }
    .handle-box {
      padding-left: 20px;
      width: 100%;
      border: 1px solid #E7E7E7;
      box-sizing: border-box;
      height: 40px;
      .palette {
        // margin-left: 10px;
        width: 90px;
        height: 40px;
        position: relative;
        #PaletteDiv2, #PaletteDiv1 {
          width: 100%;
          height: 100%;
          canvas {
            &:focus {
              outline: none;
            }
          }
        }
        i {
          position: absolute;
          left: -5px;
          top: 9px;
        }
      }
      .handle-btn {
        .el-button {
          padding: 9px 0;
        }
        .el-button--text {
          color: #69696C;
        }
      }
    }
  }
</style>
