<template>
  <div class="contact-us-container">
    <!-- 主标题 + 黄色下划线 -->
    <h1 class="main-title">Contact Us</h1>

    <!-- 学术顾问板块 -->
    <section class="contact-section">
      <h2>Academic Advisor</h2>
      <p>Prof. Chenghui Wang : wangch@shou.edu.cn</p>
      <p>Prof. Jun Wang : wangjun@shou.edu.cn</p>
    </section>

    <!-- 数据收集与分析板块 -->
    <section class="contact-section">
      <h2>Data Analyser & Web Developer</h2>
      <p>Dr. Lingxiao Wang : lingxiaowang2025@163.com</p>
    </section>

    <!-- 网页开发板块 -->
<!--    <section class="contact-section">-->
<!--      <h2>Web Developer</h2>-->
<!--      <p>Hanmingzi Liu : 1054932082@qq.com</p>-->
<!--    </section>-->
  </div>
</template>

<style scoped>
/* 容器：居中 + 内边距 */
.contact-us-container {
  padding: 30px;
  max-width: 1200px; /* 限制最大宽度，避免过宽 */
  margin: 0 auto;    /* 水平居中 */
}

/* 主标题：字体 + 黄色下划线 */
.main-title {
  font-size: 28px;
  color: #333; /* 深灰色，贴近示例标题色 */
  margin-bottom: 10px;
  border-bottom: 3px solid #278EF5; /* 黄色下划线（可根据需求微调色值） */
  padding-bottom: 10px;
}

/* 每个板块：间距 + 标题样式 */
.contact-section {
  margin-top: 30px;
}

.contact-section h2 {
  font-size: 22px;
  color: #222; /* 板块标题深黑色 */
  margin-bottom: 15px;
}

.contact-section p {
  font-size: 16px;
  line-height: 1.6; /* 行高优化可读性 */
  margin-bottom: 8px;
}
</style>
