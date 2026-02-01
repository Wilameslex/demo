library(clusterProfiler)
library(enrichplot)
library(ggplotify)
library(org.My.eg.db)
library(ggplot2)
genes <- readLines("/mnt/d/workspace/demo/tmp/enrichment/1422e6b7-449f-45b0-9eb7-eccf3f72840c/genes.txt")
genes <- trimws(genes[genes != ""])
ego <- enrichGO(
  gene = genes,
  OrgDb = org.My.eg.db,
  keyType = 'GID',
  ont = "ALL",
  pvalueCutoff = 0.05,
  qvalueCutoff = 0.05
)
ego_df <- as.data.frame(ego)
cat("ego_df行数:", nrow(ego_df), "\n")
if (nrow(ego_df) > 0) {
  cat("测试1: 生成dotplot\n")
  p <- enrichplot::dotplot(ego, showCategory = 15)
  cat("dotplot类:", class(p), "\n")
  
  cat("\n测试2: 使用ggplotify::as.ggplot转换\n")
  p2 <- ggplotify::as.ggplot(p)
  cat("转换后类:", class(p2), "\n")
  
  cat("\n测试3: 尝试+运算符\n")
  tryCatch({
    p3 <- p2 + theme_minimal()
    cat("成功!\n")
  }, error = function(e) {
    cat("失败:", conditionMessage(e), "\n")
  })
  
  cat("\n测试4: 直接打印dotplot\n")
  print(p)
  cat("\n测试5: 直接保存dotplot\n")
  ggsave("test_direct.png", p, width = 10, height = 6, dpi = 300)
}
