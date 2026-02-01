#!/usr/bin/env Rscript

custom_lib <- Sys.getenv("R_LIBS_USER", unset = .libPaths()[1])
if (!dir.exists(custom_lib)) {
  custom_lib <- .libPaths()[1]
}
.libPaths(c(custom_lib, .libPaths()))
cat("使用R库路径:", custom_lib, "\n")

suppressPackageStartupMessages({
  library(optparse)
  library(clusterProfiler)
  library(enrichplot)
  library(tidyverse)
  library(jsonlite)
  library(ggplot2)
  library(org.My.eg.db)
})
if (!"org.My.eg.db" %in% loadedNamespaces()) {
  stop("未能加载 org.My.eg.db 包！请检查路径：", custom_lib)
} else {
  message("成功加载 org.My.eg.db 包")
}

# 1. 先解析命令行参数（确保opt对象优先定义）
option_list <- list(
  make_option(c("-i", "--input"), type = "character", default = NULL,
              help = "Input gene list file", metavar = "character"),
  make_option(c("-o", "--output"), type = "character", default = "enrichment_results.json",
              help = "Output result file", metavar = "character"),
  make_option(c("-m", "--image"), type = "character", default = "enrichment_chart.png",
              help = "Output image file", metavar = "character"),
  make_option(c("-e", "--emfile"), type = "character", default = NULL,
              help = "Path to emmaper annotations file", metavar = "character")
)

opt_parser <- OptionParser(option_list = option_list)
opt <- parse_args(opt_parser)

# 2. 检查必要参数
if (is.null(opt$input)) {
  stop("Input file must be specified with --input", call. = FALSE)
}
if (is.null(opt$emfile)) {
  stop("EMFILE path must be specified with --emfile", call. = FALSE)
}

# 3. 处理EMFILE路径（依赖已定义的opt对象）
EMFILE_PATH <- normalizePath(opt$emfile)

# 检查EMFILE是否存在
if (!file.exists(EMFILE_PATH)) {
  stop(paste0(
    "EMFILE 不存在！\n",
    "预期路径：", EMFILE_PATH, "\n",
    "请检查路径/文件/权限"
  ))
} else {
  message(paste0("成功找到 EMFILE：\n", EMFILE_PATH))
}

# 预加载并缓存emfile数据
load_emfile_data <- function() {
  if (exists(".emfile_cache", envir = .GlobalEnv)) {
    return(get(".emfile_cache", envir = .GlobalEnv))
  }

  message("Loading and processing emfile data...")

  emapper <- read_delim(
    EMFILE_PATH,
    delim = "\t",
    escape_double = FALSE,
    col_names = FALSE,
    comment = "#",
    trim_ws = TRUE,
    show_col_types = FALSE
  )

  emapper <- emapper %>%
    dplyr::select(
      GID = X1,
      KO = X12,
      Pathway = X13
    )

  pathway2gene <- emapper %>%
    dplyr::select(Pathway, GID) %>%
    separate_rows(Pathway, sep = ",", convert = FALSE) %>%
    filter(str_detect(Pathway, "ko")) %>%
    mutate(Pathway = str_remove(Pathway, "ko"))

  get_path2name <- function() {
    keggpathid2name.df <- clusterProfiler:::kegg_list("pathway")
    keggpathid2name.df[,1] <- gsub("path:map", "", keggpathid2name.df[,1])
    colnames(keggpathid2name.df) <- c("path_id", "path_name")
    return(keggpathid2name.df)
  }

  pathway2name <- get_path2name()

  .emfile_cache <- list(
    pathway2gene = pathway2gene,
    pathway2name = pathway2name
  )

  assign(".emfile_cache", .emfile_cache, envir = .GlobalEnv)
  message("Emfile data loaded and cached")

  return(.emfile_cache)
}

# 读取基因列表
genes <- readLines(opt$input)
genes <- trimws(genes)
genes <- genes[genes != ""]

# 加载emfile数据
emfile_data <- load_emfile_data()
pathway2gene <- emfile_data$pathway2gene
pathway2name <- emfile_data$pathway2name

# 执行KEGG富集分析
tryCatch({
  de_ekp <- enricher(
    gene = genes,
    TERM2GENE = pathway2gene,
    TERM2NAME = pathway2name,
    pvalueCutoff = 0.05,
    qvalueCutoff = 0.05
  )

  de_ekp_df <- as.data.frame(de_ekp)

  if (nrow(de_ekp_df) > 0) {
    de_ekp_df$ID <- paste0("map", de_ekp_df$ID)
    de_ekp_df <- merge(de_ekp_df, pathway2name, by.x = "ID", by.y = "path_id", all.x = TRUE)
  }

  results <- list(
    analysis_type = "KEGG",
    gene_count = length(genes),
    enriched_pathways = nrow(de_ekp_df),
    results = de_ekp_df,
    chart_image = basename(opt$image)  # 使用文件名而非完整路径，避免前端处理问题
  )

  write_json(results, opt$output)

  if (nrow(de_ekp_df) > 0) {
    plot_data <- de_ekp_df %>%
      head(15) %>%
      mutate(Description = ifelse(is.na(path_name), Description, path_name))

    p <- ggplot(plot_data, aes(x = -log10(pvalue), y = reorder(Description, -log10(pvalue)))) +
      geom_point(aes(size = Count, color = -log10(pvalue))) +
      scale_color_gradient(low = "blue", high = "red") +
      labs(
        title = "KEGG Pathway Enrichment",
        x = "-log10(p-value)",
        y = "Pathway"
      ) +
      theme_minimal() +
      theme(
        axis.text.y = element_text(size = 10),
        axis.title = element_text(size = 12),
        plot.title = element_text(size = 14, face = "bold", hjust = 0.5)
      )

    ggsave(opt$image, p, width = 12, height = 8, dpi = 300)
  } else {
    p <- ggplot() +
      annotate("text", x = 0.5, y = 0.5,
               label = "No significant enrichment results found",
               size = 6) +
      theme_void()

    ggsave(opt$image, p, width = 10, height = 6, dpi = 300)
  }

  quit(status = 0)
}, error = function(e) {
  error_file <- file.path(dirname(opt$output), "error.log")  # 错误日志保存到输出目录
  cat("ERROR:", conditionMessage(e), "\n", file = error_file)
  cat("TRACEBACK:\n", file = error_file, append = TRUE)
  cat(traceback(), file = error_file, sep = "\n", append = TRUE)
  message("Error in KEGG enrichment analysis: ", conditionMessage(e))
  quit(status = 1)
})