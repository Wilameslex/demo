<template>
  <el-container style="position: relative; min-height: 100%">
    <el-header style="background: #fff; box-shadow: 5px 0 5px #ccc">
      <div style="width: 1200px; margin: 0 auto">
        <!-- 将页面分为24等份 -->
        <el-row
          style="
            display: flex;
            justify-content: space-between;
            align-items: center;
          "
        >
          <el-col :span="4">
            <div style="display: flex; align-items: center">
              <img
                src="@/assets/logo.png"
                alt=""
                style="width: 35px; height: 35px; cursor: pointer"
              />
              <div
                style="font-size: 20px; font-weight: bold; margin-left: 10px"
              >
                EGBD
              </div>
              <div style="font-size: 5px; font-weight: bold; margin-left: 10px">
                Eriocheir sinensis Genome Breeding Database
              </div>
            </div>
          </el-col>
          <el-col :span="16">
            <el-menu router mode="horizontal">
              <el-menu-item index="/front/index">
                <span>Home</span></el-menu-item
              >

              <!-- 修改后的Data下拉菜单 -->
              <el-sub-menu index="data">
                <template slot="title">Data</template>
                <el-sub-menu index="transcriptome">
                  <template #title>
                    <span>Transcriptome</span>
                  </template>
                  <!-- 第二级：Transcriptome的子项 -->
                  <el-menu-item
                    @click="goToExpressionSearch"
                    index="transcriptome/expression"
                    >Expression</el-menu-item
                  >
                  <el-menu-item index="transcriptome/geneNetwork"
                    >Gene Network</el-menu-item
                  >
                </el-sub-menu>
                <el-menu-item @click="goToGeneSearch">Gene Info</el-menu-item>
                <el-menu-item @click="goToPathwaySearch">Pathway</el-menu-item>
                <el-menu-item>Genome Variation</el-menu-item>
              </el-sub-menu>

              <el-sub-menu index="tools">
                <template slot="title">Tools</template>
                <el-menu-item @click="goToEnrichment"
                  >GO/KEGG Erichment</el-menu-item
                >
              </el-sub-menu>

              <el-menu-item @click="$router.push('/download')">
                <el-icon><el-icon-download /></el-icon>
                <span>Download</span>
              </el-menu-item>
              <el-menu-item index="/front/index">
                <span>Data Share</span></el-menu-item
              >
              <el-menu-item index="/front/index">
                <span>About</span></el-menu-item
              >
            </el-menu>
          </el-col>
          <el-col :span="4">
            <div style="display: flex; justify-content: right">
              <el-avatar
                :size="30"
                shape="circle"
                style="width: 30px; height: 30px"
                :src="img"
              ></el-avatar>

              <el-dropdown>
                <span
                  class="el-dropdown-link"
                  style="
                    cursor: pointer;
                    color: #409eff;
                    position: relative;
                    left: 10px;
                    top: 5px;
                  "
                >
                  管理员<el-icon class="el-icon--right"
                    ><el-icon-arrow-down
                  /></el-icon>
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item>个人信息</el-dropdown-item>
                  <el-dropdown-item>修改密码</el-dropdown-item>
                  <el-dropdown-item>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-header>

    <div style="width: 1200px; margin: 0 auto">
      <div style="margin-top: 30px; margin-bottom: 100px">
        <!-- 添加路由视图 - 基因搜索和结果页面将显示在这里 -->
        <router-view />
      </div>
    </div>

    <el-footer
      style="
        padding-top: 10px;
        padding-bottom: 10px;
        box-sizing: border-box;
        text-align: center;
        color: #fff;
        background: #409eff;
        height: auto;
        position: absolute;
        bottom: 0;
        width: 100%;
      "
    >
      <p style="line-height: 30px; font-size: 14px">
        Realesed 2025 | Devoloped by wlx from Shanghai Ocean University, College
        of Fisheries and Science
      </p>
      <p style="line-height: 30px; font-size: 14px">
        Copyright © 2025 . All rights reserved.
      </p>
    </el-footer>
  </el-container>
</template>

<script>
import {
  Download as ElIconDownload,
  ArrowDown as ElIconArrowDown,
} from '@element-plus/icons'
export default {
  components: {
    ElIconDownload,
    ElIconArrowDown,
  },
  name: 'FrontLayout',
  data() {
    return {
      img: require('@/assets/logo.png'),
    }
  },
  methods: {
    // 添加跳转到基因搜索页面的方法
    goToGeneSearch() {
      this.$router.push({ name: 'GeneSearch' })
    },
    // 添加跳转到通路搜索页面的方法
    goToPathwaySearch() {
      this.$router.push({ name: 'PathwaySearch' })
    },
    goToExpressionSearch() {
      this.$router.push({ name: 'ExpressionSearch' })
    },
    goToEnrichment() {
      this.$router.push({ name: 'EnrichmentSearch' })
    },
  },
}
</script>

<style scoped>
/* 添加下拉菜单样式 */
.el-submenu .el-menu-item {
  padding: 0 20px;
  height: 36px;
  line-height: 36px;
}

/* 调整菜单项样式 */
.el-menu--horizontal > .el-submenu .el-submenu__title {
  height: 60px;
  line-height: 60px;
  border-bottom: 2px solid transparent;
  color: #303133;
}

.el-menu--horizontal > .el-submenu.is-active .el-submenu__title {
  border-bottom-color: #409eff;
  color: #409eff;
}
</style>
