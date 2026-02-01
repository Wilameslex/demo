#!/usr/bin/env Rscript

# 设置库路径
custom_lib <- Sys.getenv("R_LIBS_USER", unset = .libPaths()[1])
if (!dir.exists(custom_lib)) {
    custom_lib <- .libPaths()[1]
}
.libPaths(c(custom_lib, .libPaths()))
cat("使用R库路径:", custom_lib, "\n")

# 抑制特定警告
options(warn = -1)  # 临时关闭所有警告

# 在脚本结束时恢复
on.exit(options(warn = 0))

tryCatch({
  # ==== 第一步：参数解析（必须最先执行） ====
  # 确保在访问opt之前先定义它
  if (!requireNamespace("optparse", quietly = TRUE)) {
    stop("optparse 包未安装，请运行: install.packages('optparse')")
  }
  library(optparse)

  # 创建选项列表
  option_list <- list(
    make_option(c("-i", "--input"), type = "character", default = NULL,
                help = "Input gene list file", metavar = "character"),
    make_option(c("-o", "--output"), type = "character", default = "enrichment_results.json",
                help = "Output result file", metavar = "character"),
    make_option(c("-m", "--image"), type = "character", default = "enrichment_chart.png",
                help = "Output image file", metavar = "character")
  )

  opt_parser <- OptionParser(option_list = option_list)
  opt <- parse_args(opt_parser)

  # 验证参数
    if (is.null(opt$input)) {
      stop("错误：未指定输入文件！请使用 --input 参数")
    }

    # 创建输出目录
    output_dir <- dirname(opt$output)
    if (!dir.exists(output_dir)) {
      dir.create(output_dir, recursive = TRUE, showWarnings = FALSE)
    }

    image_dir <- dirname(opt$image)
    if (!dir.exists(image_dir)) {
      dir.create(image_dir, recursive = TRUE, showWarnings = FALSE)
    }

  # ==== 第二步：环境诊断（在参数解析后） ====
  cat("===== R 环境诊断 =====\n")
    cat("R版本:", R.version.string, "\n")
    cat("工作目录:", getwd(), "\n")

    # 修复库路径输出
    cat("库路径:\n")
    cat(paste("-", .libPaths(), collapse = "\n"), "\n")

    cat("输入文件:", opt$input, "\n")
    cat("输出文件:", opt$output, "\n")
    cat("图像文件:", opt$image, "\n")

  # ==== 第三步：包加载检查 ====
  cat("===== 包加载检查 =====\n")
  required_packages <- c("optparse", "clusterProfiler", "org.My.eg.db",
                        "ggplot2", "jsonlite", "GO.db")

  # 检查并加载包
  for (pkg in required_packages) {
    if (!requireNamespace(pkg, quietly = TRUE)) {
      cat("包缺失:", pkg, "\n")
      # 尝试安装缺失包
      install.packages(pkg, repos = "https://cloud.r-project.org")
      if (!requireNamespace(pkg, quietly = TRUE)) {
        stop(paste("安装包失败:", pkg))
      }
    } else {
            # 安全获取包版本
            pkg_version <- tryCatch({
              as.character(packageVersion(pkg))
            }, error = function(e) "unknown")

            cat("包已加载:", pkg, "版本", pkg_version, "\n")
          }

          # 加载包到命名空间
          suppressPackageStartupMessages(library(pkg, character.only = TRUE))
        }

        # 验证关键函数是否存在
          cat("验证关键函数...\n")
          if (!exists("enrichGO")) {
            stop("clusterProfiler 包未正确加载: enrichGO 函数不存在")
          }
          if (!exists("dotplot")) {
            stop("ggplot2 包未正确加载: dotplot 函数不存在")
          }


  # ==== 第四步：主要分析逻辑 ====
  # 5. 读取基因列表
  cat("===== 读取基因列表 =====\n")
  genes <- suppressWarnings(readLines(opt$input))
  genes <- trimws(genes)
  genes <- genes[genes != ""]
  cat("读取到", length(genes), "个基因\n")

  # 6. 执行GO富集分析
  cat("===== 执行GO富集分析 =====\n")
  ego <- enrichGO(
    gene = genes,
    OrgDb = org.My.eg.db,
    keyType = 'GID',
    ont = "ALL",
    pvalueCutoff = 0.05,
    qvalueCutoff = 0.05
  )

  # 7. 处理结果
  cat("===== 处理结果 =====\n")
    ego_df <- as.data.frame(ego)

    # 创建输出目录（如果需要）
    dir.create(dirname(opt$output), showWarnings = FALSE, recursive = TRUE)
    dir.create(dirname(opt$image), showWarnings = FALSE, recursive = TRUE)

  # 8. 保存结果为JSON
  cat("===== 保存结果为JSON =====\n")
    results <- list(
      analysis_type = "GO",
      gene_count = length(genes),
      enriched_terms = nrow(ego_df),
      results = ego_df,
      chart_image = "chart.png"
    )
    write_json(results, opt$output)

  # 9. 生成图表
  cat("===== 生成图表 =====\n")
    if (nrow(ego_df) > 0) {
      plot_df <- ego_df[1:min(15, nrow(ego_df)), ]
      plot_df$GeneRatio_numeric <- sapply(strsplit(plot_df$GeneRatio, "/"), function(x) as.numeric(x[1])/as.numeric(x[2]))
      plot_df$Description <- factor(plot_df$Description, levels = rev(plot_df$Description))
      
      p <- ggplot(plot_df, aes(x = GeneRatio_numeric, y = Description)) +
        geom_point(aes(size = Count, color = p.adjust)) +
        scale_color_gradient(low = "red", high = "blue", name = "p.adjust") +
        scale_size_continuous(name = "Count", range = c(3, 8)) +
        theme_minimal() +
        theme(
          axis.text.y = element_text(size = 10),
          axis.text.x = element_text(size = 10),
          axis.title = element_text(size = 12, face = "bold"),
          legend.title = element_text(size = 10, face = "bold"),
          legend.text = element_text(size = 9),
          plot.title = element_text(size = 14, face = "bold", hjust = 0.5)
        ) +
        labs(
          title = "GO Enrichment Analysis",
          x = "Gene Ratio",
          y = NULL
        )
      ggsave(opt$image, p, width = 10, height = 6, dpi = 300)
    } else {
      p <- ggplot() +
        annotate("text", x = 0.5, y = 0.5,
                 label = "No significant GO terms found\n(p < 0.05)",
                 size = 6, color = "darkred") +
        theme_void() +
        labs(title = "GO Enrichment Results")
      ggsave(opt$image, p, width = 8, height = 6, dpi = 300)
    }

    cat("===== 分析完成 =====\n")

  }, error = function(e) {
    # 错误处理 - 使用安全的日志记录
    error_file <- "error.log"

    # 记录错误信息
    cat("ERROR:", conditionMessage(e), "\n", file = error_file)

    # 尝试获取traceback - 安全方式
    tryCatch({
      tb <- traceback(3)  # 获取3层调用栈
      cat("TRACEBACK:\n", file = error_file, append = TRUE)
      cat(paste(tb, collapse = "\n"), file = error_file, sep = "\n", append = TRUE)
    }, error = function(tb_err) {
      cat("无法获取traceback:", conditionMessage(tb_err), "\n",
          file = error_file, append = TRUE)
    })

    # 打印到控制台
    message("ERROR: ", conditionMessage(e))
    quit(status = 1)
  })