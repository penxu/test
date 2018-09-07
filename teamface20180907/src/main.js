// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import store from './store'
import VueBus from './common/js/bus.js'
import Element from 'element-ui'
import YS from './common/YS-frame/YS.js'
import 'element-ui/lib/theme-chalk/index.css'
// 导入 table 和 分页组件
import {VTable, VPagination} from 'vue-easytable'
import { HTTPServer } from '@/common/js/ajax.js'
// import '../static/css/element-variables.scss'
/** npm install md5 */
/** npm install jquery */

/** 过滤器 */
import * as filters from '@/filter/filter.js'
Object.keys(filters).forEach(key => {
  Vue.filter(key, filters[key])
})
// 注册到全局
Vue.component(VTable.name, VTable)
Vue.component(VPagination.name, VPagination)
Vue.use(Element)
Vue.use(VueBus)
Vue.config.productionTip = false
Vue.prototype.YS = YS
Vue.prototype.$http = HTTPServer
/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  components: { App },
  template: '<App/>'
})
