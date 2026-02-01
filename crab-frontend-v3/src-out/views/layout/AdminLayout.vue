<template>
  <el-container style="height: 100%">
    <el-aside style="width: 200px; background: #000; overflow-x: hidden">
      <!--            <h3 style="text-align: center;color: #fff;height: 30px;line-height: 30px;margin-top: 11px">在线管理系统</h3>-->

      <div style="display: flex; justify-content: center">
        <img
          :src="img"
          alt=""
          style="width: 30px; height: 30px; position: relative; top: 10px"
        />
        <div
          style="
            margin-left: 10px;
            font-size: 19px;
            color: #fff;
            height: 30px;
            line-height: 30px;
            margin-top: 11px;
          "
        >
          中华绒螯蟹基因组数据库在线管理系统
        </div>
      </div>
      <el-menu
        background-color="#000"
        text-color="#FFF"
        :default-openeds="[]"
        router
        default-active="home"
        style="border: none"
      >
        <el-menu-item index="home"
          ><el-icon><el-icon-s-home /></el-icon
          ><span slot="title">首页</span></el-menu-item
        >
        <el-menu-item index="other"
          ><el-icon><el-icon-s-home /></el-icon
          ><span slot="title">数据编辑</span></el-menu-item
        >
        <el-menu-item
          ><el-icon><el-icon-s-home /></el-icon
          ><span slot="title">信息交流</span></el-menu-item
        >
        <el-menu-item
          ><el-icon><el-icon-s-home /></el-icon
          ><span slot="title">用户管理</span></el-menu-item
        >

        <el-sub-menu index="">
          <template slot="title">
            <el-icon><el-icon-s-home /></el-icon>
          </template>
          <el-menu-item
            ><el-icon><el-icon-s-home /></el-icon
            ><span slot="title">二级导航</span></el-menu-item
          >
          <el-menu-item
            ><el-icon><el-icon-s-home /></el-icon
            ><span slot="title">二级导航</span></el-menu-item
          >
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header
        style="
          text-align: right;
          font-size: 12px;
          background: skyblue;
          height: 60px;
        "
      >
        <el-avatar
          :size="30"
          shape="circle"
          style="
            width: 30px;
            height: 30px;
            position: relative;
            top: 15px;
            right: 10px;
          "
          :src="img"
        ></el-avatar>

        <el-dropdown>
          <span
            class="el-dropdown-link"
            style="
              cursor: pointer;
              position: relative;
              top: 6px;
              color: #409eff;
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
      </el-header>
      <el-main style="padding: 10px">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import {
  SHome as ElIconSHome,
  ArrowDown as ElIconArrowDown,
} from '@element-plus/icons'
export default {
  components: {
    ElIconSHome,
    ElIconArrowDown,
  },
  name: 'AdminLayout',
  data() {
    return {
      //在vue中导入图片
      img: require('@/assets/logo.png'),
    }
  },
}
</script>
