<template>
  <div>
    <el-divider content-position="left">el-row的使用</el-divider>
    <el-card>
      <el-row>
        <el-col :span="8"><div class="grid-content bg-purple"></div></el-col>
        <el-col :span="8"><div class="grid-content bg-purple-light"></div></el-col>
        <el-col :span="8"><div class="grid-content bg-purple"></div></el-col>
      </el-row>
      <el-row>
        <el-col :span="4"><div class="grid-content bg-purple"></div></el-col>
        <el-col :span="16"><div class="grid-content bg-purple-light"></div></el-col>
        <el-col :span="4"><div class="grid-content bg-purple"></div></el-col>
      </el-row>
    </el-card>

    <el-divider content-position="left">el-button的使用</el-divider>
    <el-card>
      <el-row>
        <el-button>默认按钮</el-button>
        <el-button type="primary">主要按钮</el-button>
        <el-button type="success">成功按钮</el-button>
        <el-button type="info">信息按钮</el-button>
        <el-button type="warning">警告按钮</el-button>
        <el-button type="danger">危险按钮</el-button>
      </el-row>

    </el-card>


    <el-divider content-position="left">Form表单</el-divider>
    <el-card>
      <el-input
          style="width: 300px"
          placeholder="请输入内容"
          v-model="input"
          clearable>
      </el-input>
      <el-input
          type="textarea"
          :rows="2"
          placeholder="请输入内容"
          v-model="input">
      </el-input>

    </el-card>


    <el-divider content-position="left"></el-divider>
    <el-card>

    </el-card>

  </div>
</template>
<script>
export default {

  data(){
    return {
      input:""
    }
  }
}
</script>
<style>

.el-col {
  border-radius: 4px;
}
.bg-purple-dark {
  background: #99a9bf;
}
.bg-purple {
  background: #d3dce6;
}
.bg-purple-light {
  background: #e5e9f2;
}
.grid-content {
  border-radius: 4px;
  min-height: 36px;
}
.row-bg {
  padding: 10px 0;
  background-color: #f9fafc;
}
</style>

