var work = {
  tagInit: function (list, dom) {
    setTimeout(() => {
      var children = dom.children
      var width = 0
      for (var i = 0; i < children.length; i++) {
        if (children[i].className.indexOf('tag-item') >= 0) {
          width += children[i].clientWidth
        }
        delete list.hidden
        if (i > 0) {
          if (width + (i - 1) * 10 > 222) {
            list.hidden = true
          }
        }
      }
    }, 20)
  }
}
export default work
