<template>
  <div class="download-center">
    <el-card shadow="hover">
      <div class="header">
        <h1><i class="el-icon-download"></i> 基因组数据下载中心</h1>
        <p class="sub-title">所有数据仅供科研使用，请遵守数据使用协议</p>
      </div>

      <div class="directory-browser">
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item>
            <span @click="handleHomeClick" style="cursor: pointer">Home</span>
          </el-breadcrumb-item>
          <el-breadcrumb-item
              v-for="(part, index) in currentPathParts"
              :key="index">
              <span @click="navigateToParent(index)" style="cursor: pointer">
                {{ part }}
              </span>
          </el-breadcrumb-item>
        </el-breadcrumb>

        <el-table
            :data="fileList"
            v-loading="loading"
            :default-sort="{ prop: 'name', order: 'ascending' }"
            stripe
            style="width: 100%">
          <el-table-column label="名称" min-width="300">
            <template #default="{row}">
              <i :class="row.type === 'directory' ? 'el-icon-folder' : 'el-icon-document'"></i>
              <span v-if="row.type === 'directory'">
                <a @click="navigateTo(row.path)">{{ row.name }}</a>
              </span>
              <span v-else>
                <a @click="downloadFile(row)">{{ row.name }}</a>
              </span>
            </template>
          </el-table-column>

          <el-table-column prop="lastModified" label="修改日期" width="180">
            <template #default="{row}">
              {{ formatDate(row.lastModified) }}
            </template>
          </el-table-column>

          <el-table-column label="大小" width="120">
            <template #default="{row}">
              {{ row.type === 'file' ? formatFileSize(row.size) : '-' }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios';
export default {
  name: 'DownloadCenter',
  data() {
    return {
      currentPath: '/',
      fileList: [],
      loading: false
    };
  },
  computed: {
    currentPathParts() {
      return this.currentPath.split('/').filter(part => part);
    }
  },
  mounted() {
    // 临时测试数据
    // this.fileList = [
    //   {
    //     name: "test_dir",
    //     type: "directory",
    //     path: "/test_dir",
    //     size: 0,
    //     lastModified: new Date().toISOString()
    //   },
    //   {
    //     name: "test.txt",
    //     type: "file",
    //     path: "/test.txt",
    //     size: 1024,
    //     lastModified: new Date().toISOString()
    //   }
    // ];
    this.loadFiles();
  },
  watch: {
    currentPath() {
      this.loadFiles();
    }
  },
  methods: {
    handleHomeClick() {
      if (this.currentPath !== '/') {
        this.currentPath = '/';
      }
    },
    navigateToParent(index) {
      const newPath = '/' + this.currentPathParts.slice(0, index).join('/');
      if (this.currentPath !== newPath) {
        this.currentPath = newPath;
      }
    },
// 确保环境变量已加载

    async loadFiles() {
      try {
        this.loading = true;

        // 确保路径以斜杠开头
        const requestPath = this.currentPath.startsWith('/')
            ? this.currentPath
            : '/' + this.currentPath;

        console.log('请求路径:', requestPath);
        console.log('FILE_API:', '/file-api');

        const response = await axios.get('/file-api/files', {
          params: {
            path: requestPath
          },
          timeout: 10000
        });

        console.log('收到响应', response);

        // 处理响应数据 - 添加路径格式化
        this.fileList = response.data.files.map(file => ({
          ...file,
          path: decodeURIComponent(file.path)
              .replace(/\\/g, '/') // 确保统一使用正斜杠
        }));

        // 更新当前路径为服务器返回的验证后路径
        this.currentPath = response.data.currentPath || '/';

      } catch (error) {
        console.error('API请求失败:', {
          config: error.config,
          url: error.config?.url,
          status: error.response?.status,
          data: error.response?.data
        });

        this.$message.error(
            `加载失败: ${error.response?.data?.error || error.message}`
        );

        // 备用数据逻辑（仅用于开发测试）
        if (process.env.NODE_ENV === 'development') {
          this.fileList = [
            {
              name: "test_dir",
              type: "directory",
              path: "/test_dir",
              size: 0,
              lastModified: new Date().toISOString()
            },
            {
              name: "test.txt",
              type: "file",
              path: "/test.txt",
              size: 1024,
              lastModified: new Date().toISOString()
            }
          ];
        }
      } finally {
        this.loading = false;
      }
    },

    navigateTo(path) {
      // 解码路径并确保以斜杠开头
      const decodedPath = decodeURIComponent(path)
          .replace(/\\/g, '/')
          .replace(/\/+/g, '/');

      const normalizedPath = decodedPath.startsWith('/')
          ? decodedPath
          : '/' + decodedPath;

      if (this.currentPath !== normalizedPath) {
        this.currentPath = normalizedPath;
      }
    },

    async downloadFile(file) {
      try {
        // 确保路径以斜杠开头
        const filePath = file.path.startsWith('/') ? file.path : '/' + file.path;

        // 双重编码路径参数（浏览器会自动解码一次）
        const encodedPath = encodeURIComponent(encodeURIComponent(filePath));

        // 构造下载URL
        const downloadUrl = `/file-api/download?path=${encodedPath}`;

        // 方法1：直接使用window.open（推荐）
        window.open(downloadUrl, '_blank');

        // // 方法2：创建隐藏链接（备用方案）
        // const link = document.createElement('a');
        // link.href = downloadUrl;
        // link.target = '_blank';
        // link.download = file.name;
        // link.style.display = 'none';
        // document.body.appendChild(link);
        // link.click();
        // setTimeout(() => {
        //   document.body.removeChild(link);
        // }, 100);

        // 显示下载提示
        this.$message.success(`开始下载: ${file.name}`);

      } catch (error) {
        console.error('下载失败:', error);
        this.$message.error(`下载失败: ${file.name}`);
        this.$notify.error({
          title: '下载错误',
          message: error.response?.data?.detail || error.message,
          duration: 5000
        });
      }
    },

    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString();
    },

    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes';
      const k = 1024;
      const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }
  }
}

</script>

<style scoped>
.download-center {
  max-width: 1200px;
  margin: 20px auto;
  padding: 15px;
}

.header {
  text-align: center;
  margin-bottom: 20px;
}

.breadcrumb {
  padding: 10px 0;
  margin-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.el-table a {
  color: #409EFF;
  cursor: pointer;
  text-decoration: none;
}

.el-table a:hover {
  text-decoration: underline;
}

.el-icon-folder {
  color: #E6A23C;
  margin-right: 8px;
}

.el-icon-document {
  color: #909399;
  margin-right: 8px;
}
</style>