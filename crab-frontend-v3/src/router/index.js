import { createRouter, createWebHistory } from 'vue-router'
import FrontLayout from "../views/layout/FrontLayout.vue"
import AdminLayout from "../views/layout/AdminLayout.vue"

//基因查询模块导入
import GeneSearch from "@/views/search/GeneSearch.vue"
import GeneResults from "@/views/result/GeneResults.vue"
//通路查询导入
import PathwaySearch from "@/views/search/PathwaySearch.vue"
import PathwayResults from "@/views/result/PathwayResults.vue"
import ExpressionSearch from "@/views/search/ExpressionSearch.vue"
import ExpressionResults from '@/views/result/ExpressionResults.vue'
import DownloadCenter from '@/views/DownloadCenter.vue'

//about部分导入
import ContactUs from "@/views/about/Contact.vue";
import Help from "@/views/about/Help.vue"
import {ElMessage} from "element-plus";
import {request} from "axios";


const routes = [
  //前台路由的创建
  {
    path: "/front",
    name: "",
    component: FrontLayout,
    redirect: "/front/index",
    children: [
      { path: "index", name: "index", component: () => import("../views/front/Index.vue") },
      { path: "test", name: "Test", component: () => import("../views/front/Test.vue") },

      {
        path: 'download',
        name: 'DownloadCenter',
        component: DownloadCenter,
        meta: { title: '数据下载中心' }
      },

      // 新增基因搜索相关路由
      {
        path: "gene-info/search",
        name: "GeneInfoSearch",
        component: GeneSearch,
        meta: { title: '基因信息搜索' }
      },
      {
        path: "gene-info/results",
        name: "GeneResults",
        component: GeneResults,
        meta: { title: '基因搜索结果' }
      },
      //新增通路搜索相关路由
      {
        path: "pathway/search",
        name: "PathwaySearch",
        component: PathwaySearch,
        meta: { title: 'KEGG通路搜索' }
      },
      {
        path: "genome-info/overview",
        name: "GenomeOverview",
        component: () => import("@/views/search/GenomeOverview.vue"),
        meta: { title: "Genome Overview", breadcrumb: ['Data', 'Genome Info', 'Genome Overview'] }
      },
      {
        path: "mitochondrion",
        name: "MitochondrionGenome",
        component: () => import("@/views/search/MitochondrionGenome.vue"),
        meta: { title: "Mitochondrion Genome" }
      },
      {
        path: "pathway/results",
        name: "PathwayResults",
        component: PathwayResults,
        meta: { title: 'KEGG通路搜索结果' }
      },
      {
        path: "data/genome-variation/search",
        name: "GenomeVariationSearch",
        component: () => import("@/views/search/GenomeVariationSearch.vue"),
        meta: { title: "Genome Variation Search" }
      },
      {
        path: "data/genome-variation/results",
        name: "GenomeVariationResults",
        component: () => import("@/views/result/GenomeVariationResults.vue"),
        meta: {title: "Genome Variation Results"}
      },
      {
        path: "transcriptome/process-expression/search",
        name: "ProcessExpressionSearch",
        component: () => import("@/views/search/ProcessExpressionSearch.vue"),
        meta: { title: "转录组整体表达查询" }
      },
      {
        path: "transcriptome/process-expression/results",
        name: "ProcessExpressionResults",
        component: () => import("@/views/result/ProcessExpressionResults.vue"),
        meta: { title: "转录组整体表达结果" }
      },
      {
        path: 'transcriptome/expression/search',
        name: 'ExpressionSearch',
        component: ExpressionSearch,
        meta: {
          title: '基因表达搜索',
          breadcrumb: ['Data', 'Transcriptome', 'Expression'] // 面包屑导航数据
        }
      },
      {
        path: 'transcriptome/expression/results', // 修正路径
        name: 'ExpressionResults',
        component: ExpressionResults,
        meta: { title: '基因表达结果' }
      },
      // 新增基因网络搜索路由
      {
        path: "/gene-network/search",
        name: "GeneNetworkSearch",
        component: () => import("@/views/search/GeneNetworkSearch.vue"),
        meta: {
          title: "Gene Network Search",
          breadcrumb: ['Data', 'Expression', 'Gene Network']
        }
      },
// 新增基因网络结果路由
      {
        path: "/gene-network/results",
        name: "GeneNetworkResults",
        component: () => import("@/views/result/GeneNetworkResults.vue"),
        meta: { title: "Gene Network Results" }
      },
      {
        path: "data/phenotype",
        name: "PhenotypeData",
        component: () => import("@/views/search/PhenotypeData.vue"),
        meta: {
          title: "Phenotype Data",
          breadcrumb: ['Data', 'Phenotype Data']
        }
      },
      {
        path: "enrichment/analyze",
        name: "EnrichmentSearch",
        component: () => import("@/views/search/SimpleEnrichmentSearch.vue"),
        meta: { title: '富集分析' }
      },
      {
        path: "enrichment/results",
        name: "EnrichmentResults",
        component: () => import("@/views/result/EnrichmentResults.vue"),
        meta: { title: '富集分析结果' }
      },
      {
        path: 'about/contact', // 与 el-menu-item 的 index="about/contact" 对应
        name: 'ContactUs',
        component: ContactUs
      },
      {
        path: 'about/help', // 注意：将原 index="about/Help" 改为小写 about/help，保持一致
        name: 'Help',
        component: Help
      },
      {
        path: "tools/sequence-fetch",
        name: "SequenceFetch",
        component: () => import("@/views/tools/SequenceFetch.vue"),
        meta: { title: "Sequence Fetch" }
      },
      {
        path: "tools/gene-search",
        name: "GeneSearch",
        component: () => import("@/views/tools/GeneSearch.vue"),
        meta: { title: "Gene Search" }
      }
    ]
  },

  // 根路径重定向到前台
  {
    path: "/",
    redirect: "/front/index"
  },

  // 管理员登录路由
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/Login.vue'),
    meta: { requiresAuth: false } // 不需要登录即可访问
  },

  //后台路由的创建
  {
    path: '/admin',
    name: 'Admin',
    component: AdminLayout,
    redirect: '/admin/home',
    meta: { requiresAuth: true }, // 需要登录才能访问
    children: [
      // 管理员首页
      {
        path: 'home',
        name: 'AdminHome',
        component: () => import('@/views/admin/AdminHome.vue')
      },
      // 管理员专属404：匹配/admin/xxx不存在的路径
      {
        path: ':pathMatch(.*)*', // 匹配/admin下的所有未定义路径
        name: 'Admin404',
        component: () => import('@/views/admin/Admin404.vue') // 新建管理员404页面
      },
      {
        path: 'geneInfo-admin', // 路由路径
        name: 'GeneInfoAdmin',
        component: () => import('@/views/admin/GeneInfoAdmin.vue'), // 对应前端页面路径
        meta: {
          title: '基因信息管理',
          requiresAuth: true // 需管理员登录
        }
      }
    ]
  },
  //404路由规则
  {
    path: "/:pathMatch(.*)*",
    name: "NotFound",
    component: () => import("../views/404.vue")
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const requiresAuth = to.meta.requiresAuth
  const adminToken = localStorage.getItem('adminToken')

  if (requiresAuth && !adminToken) {
    // 跳登录页时，携带原访问路径（to.fullPath）
    next({
      path: '/admin/login',
      query: { redirect: to.fullPath } // 比如访问/admin/gene-manage，query.redirect就是/admin/gene-manage
    })
  } else {
    next()
  }
})



export default router