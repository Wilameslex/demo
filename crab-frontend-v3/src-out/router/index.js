import { createRouter, createWebHistory } from 'vue-router'
import FrontLayout from '../views/layout/FrontLayout.vue'
import AdminLayout from '../views/layout/AdminLayout.vue'

//基因查询模块导入
import GeneSearch from '@/views/search/GeneSearch.vue'
import GeneResults from '@/views/result/GeneResults.vue'
//通路查询导入
import PathwaySearch from '@/views/search/PathwaySearch.vue'
import PathwayResults from '@/views/result/PathwayResults.vue'
import ExpressionSearch from '@/views/search/ExpressionSearch.vue'
import ExpressionResults from '@/views/result/ExpressionResults.vue'
import DownloadCenter from '@/views/DownloadCenter.vue'

const routes = [
  //前台路由的创建
  {
    path: '/front',
    name: '',
    component: FrontLayout,
    redirect: '/front/index',
    children: [
      {
        path: 'index',
        name: 'index',
        component: () => import('../views/front/Index.vue'),
      },
      {
        path: 'test',
        name: 'Test',
        component: () => import('../views/front/Test.vue'),
      },

      {
        path: '/download',
        name: 'DownloadCenter',
        component: DownloadCenter,
        meta: { title: '数据下载中心' },
      },

      // 新增基因搜索相关路由
      {
        path: '/gene-info/search',
        name: 'GeneSearch',
        component: GeneSearch,
        meta: { title: '基因信息搜索' },
      },
      {
        path: '/gene-info/results',
        name: 'GeneResults',
        component: GeneResults,
        meta: { title: '基因搜索结果' },
      },
      //新增通路搜索相关路由
      {
        path: '/pathway/search',
        name: 'PathwaySearch',
        component: PathwaySearch,
        meta: { title: 'KEGG通路搜索' },
      },
      {
        path: '/pathway/results',
        name: 'PathwayResults',
        component: PathwayResults,
        meta: { title: 'KEGG通路搜索结果' },
      },
      {
        path: '/transcriptome/expression/search',
        name: 'ExpressionSearch',
        component: ExpressionSearch,
        meta: {
          title: '基因表达搜索',
          breadcrumb: ['Data', 'Transcriptome', 'Expression'], // 面包屑导航数据
        },
      },
      {
        path: '/transcriptome/expression/results', // 修正路径
        name: 'ExpressionResults',
        component: ExpressionResults,
        meta: { title: '基因表达结果' },
      },
      {
        path: '/enrichment/analyze',
        name: 'EnrichmentSearch',
        component: () => import('@/views/search/SimpleEnrichmentSearch.vue'),
        meta: { title: '富集分析' },
      },
      {
        path: '/enrichment/results',
        name: 'EnrichmentResults',
        component: () => import('@/views/result/EnrichmentResults.vue'),
        meta: { title: '富集分析结果' },
      },
    ],
  },

  //后台路由的创建
  {
    path: '/',
    name: '',
    component: AdminLayout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'home',
        component: () => import('../views/admin/Home.vue'),
      },
    ],
  },
  //404路由规则
  {
    path: '*',
    name: 'NotFound',
    component: () => import('../views/404.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
})

router.beforeEach((to, from, next) => {
  if (to.path.startsWith('/file-api')) {
    console.warn('非法前端路由访问')
    next('/404')
  } else {
    next()
  }
})

export default router
