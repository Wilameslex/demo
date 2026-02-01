import * as XLSX from 'xlsx';

export const exportToTxt = () => {
    // TXT导出实现
    console.log('TXT导出功能待实现')
}

export const exportToCsv = () => {
    // CSV导出实现
    console.log('CSV导出功能待实现')
}

// 优化后的Excel导出函数
export const exportToExcel = (data, fileName, sheetName = "Sheet1") => {
    if (!data || data.length === 0) {
        console.warn('导出Excel失败：数据为空')
        return
    }

    try {
        const normalizedData = data.map(item => {
            if (item.expressionValues) {
                return {
                    gene_id: item.geneId,
                    ...item.expressionValues
                }
            }
            return item
        })

        const worksheet = XLSX.utils.json_to_sheet(normalizedData)
        const workbook = XLSX.utils.book_new()
        XLSX.utils.book_append_sheet(workbook, worksheet, sheetName)
        XLSX.writeFile(workbook, `${fileName}.xlsx`)
    } catch (error) {
        console.error('导出Excel失败:', error)
        throw new Error('导出Excel时发生错误')
    }
}

// 新增：专门处理ExpressionData格式的导出函数
export const exportExpressionData = (data, fileName) => {
    if (!data || data.length === 0) return

    const exportData = data.map(item => {
        const row = { gene_id: item.geneId }
        Object.entries(item.expressionValues).forEach(([sample, value]) => {
            row[sample] = value
        })
        return row
    })

    exportToExcel(exportData, fileName, "Expression Data")
}

// 新增：导出为CSV（针对ExpressionData）
export const exportExpressionToCsv = (data, fileName) => {
    // 1. 严格处理空数据
    if (!data || data.length === 0) {
        throw new Error('无基因表达数据可导出，请先选择样本并加载数据');
    }

    // 2. 提取所有样本列 + 检查数据格式
    const allSamples = new Set();
    data.forEach((item, index) => {
        // 检查基因ID是否存在（避免 undefined）
        if (!item.geneId) {
            throw new Error(`数据格式错误：第 ${index + 1} 条数据缺少基因ID（geneId）`);
        }
        // 检查样本表达值字段（优先用 expressionValues，兼容可能的 samples 字段）
        const sampleData = item.expressionValues || item.samples;
        if (!sampleData || typeof sampleData !== 'object') {
            throw new Error(`数据格式错误：基因 ${item.geneId} 缺少有效样本表达值（需 expressionValues 或 samples 字段）`);
        }
        // 收集所有样本列
        Object.keys(sampleData).forEach(sample => {
            allSamples.add(sample);
        });
    });

    // 3. 生成 CSV 内容（添加 UTF-8 BOM 编码，避免乱码）
    const columns = ['gene_id', ...Array.from(allSamples)];
    let csvContent = '\uFEFF' + columns.join(',') + '\n'; // \uFEFF 解决浏览器识别问题

    // 4. 填充数据行（兼容 expressionValues/samples 字段）
    data.forEach(item => {
        const row = [item.geneId]; // 基因ID列
        const sampleData = item.expressionValues || item.samples; // 兼容两种字段名
        // 遍历所有样本列，填充对应值
        columns.slice(1).forEach(sample => {
            const value = sampleData[sample];
            // 处理数值格式（避免科学计数法，空值用空字符串）
            const formattedValue = value === undefined || value === null ? '' : Number(value).toString();
            row.push(formattedValue);
        });
        csvContent += row.join(',') + '\n';
    });

    // 5. 触发浏览器下载（原有逻辑不变）
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');

    link.setAttribute('href', url);
    link.setAttribute('download', `${fileName}.csv`);
    link.style.visibility = 'hidden';

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url); // 释放内存
};