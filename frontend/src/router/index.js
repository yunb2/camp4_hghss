import Vue from 'vue'
import VueRouter from 'vue-router'
import HghssPage from '../views/HghssPage.vue'
import RecruitPage from '../views/RecruitPage'
import ResetPasswd from '../components/template/ResetPasswd'
import ChangePasswd from '../components/template/ChangePasswd'

// import Home from '../views/Home.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'HghssPage',
    component: HghssPage
  },
  {
    path: '/recruit',
    name: 'RecruitPage',
    component: RecruitPage
  },
  {
    path: '/resume',
    name: 'ResumePage',
    component: RecruitPage
  },
  {
    path: '/password/reset',
    name: 'ResetPasswd',
    component: ResetPasswd
  },
  {
    path: '/password/change',
    name: 'ChangePasswd',
    component: ChangePasswd
  },
  // {
  //   path: '/about',
  //   name: 'about',
  //   // route level code-splitting
  //   // this generates a separate chunk (about.[hash].js) for this route
  //   // which is lazy-loaded when the route is visited.
  //   component: () => import(/* webpackChunkName: "about" */ '../views/About.vue')
  // }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
