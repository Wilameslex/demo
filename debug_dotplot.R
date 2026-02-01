library(clusterProfiler)
library(enrichplot)
library(org.My.eg.db)
library(ggplot2)
# 使用临时目录的实际基因
genes <- readLines("/mnt/d/workspace/demo/tmp/enrichment/00981d70-ebbe-4613-bb1e-7661fc06d1ee/genes.txt")
genes <- trimws(genes)
genes <- genes[genes != ""]
cat("基因数量:", length(genes), "\n")
cat("基因列表:", paste(genes, collapse=", "), "\n\n")
# 执行富集
ego <- enrichGO(
  gene = genes,
  OrgDb = org.My.eg.db,
  keyType = 'GID',
  ont = "ALL",
  pvalueCutoff = 0.05,
  qvalueCutoff = 0.05
)
# 检查结果
cat("ego对象类:", class(ego), "\n")
ego_df <- as.data.frame(ego)
cat("ego_df行数:", nrow(ego_df), "\n")
cat("ego_df列名:", paste(colnames(ego_df), collapse=", "), "\n\n")
if (nrow(ego_df) > 0) {
  cat("有富集结果，尝试生成dotplot\n")
  
  # 测试dotplot
  p <- dotplot(ego, showCategory = 15)
  cat("dotplot返回值类:", class(p), "\n")
  cat("dotplot是否为NULL:", is.null(p), "\n")
  
  # 测试+运算符
  cat("\n测试theme_minimal:\n")
  print(class(theme_minimal()))
  
  cat("\n测试+运算符:\n")
  tryCatch({
    p2 <- p + theme_minimal()
    cat("成功: p + theme_minimal()\n")
  }, error = function(e) {
    cat("失败:", conditionMessage(e), "\n")
  })
} else {
  cat("没有富集结果\n")
}
