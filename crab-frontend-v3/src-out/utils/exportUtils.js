import * as XLSX from 'xlsx'

export const exportToTxt = () => {
  // TXT导出实现
  console.log('TXT导出功能待实现')
}

export const exportToCsv = () => {
  // CSV导出实现
  console.log('CSV导出功能待实现')
}

// 优化后的Excel导出函数
export const exportToExcel = (data, fileName, sheetName = 'Sheet1') => {
  if (!data || data.length === 0) {
    console.warn('导出Excel失败：数据为空')
    return
  }

  try {
    const normalizedData = data.map((item) => {
      if (item.expressionValues) {
        return {
          gene_id: item.geneId,
          ...item.expressionValues,
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

  const exportData = data.map((item) => {
    const row = { gene_id: item.geneId }
    Object.entries(item.expressionValues).forEach(([sample, value]) => {
      row[sample] = value
    })
    return row
  })

  exportToExcel(exportData, fileName, 'Expression Data')
}

// 新增：导出为CSV（针对ExpressionData）
export const exportExpressionToCsv = (data, fileName) => {
  if (!data || data.length === 0) return

  const allSamples = new Set()
  data.forEach((item) => {
    Object.keys(item.expressionValues).forEach((sample) => {
      allSamples.add(sample)
    })
  })

  const columns = ['gene_id', ...Array.from(allSamples)]
  let csvContent = columns.join(',') + '\n'

  data.forEach((item) => {
    const row = [item.geneId]
    columns.slice(1).forEach((sample) => {
      const value = item.expressionValues[sample] || ''
      row.push(value.toString())
    })
    csvContent += row.join(',') + '\n'
  })

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)

  link.setAttribute('href', url)
  link.setAttribute('download', `${fileName}.csv`)
  link.style.visibility = 'hidden'

  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
