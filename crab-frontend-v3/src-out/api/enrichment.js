import request from '@/utils/request'

export function runEnrichmentAnalysis(data) {
  return request({
    url: '/api/enrichment/analyze',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'application/json', // 明确指定JSON格式
    },
    timeout: 60000,

    // 添加响应拦截处理
    transformResponse: [
      function (response) {
        try {
          const res = JSON.parse(response)
          // 检查后端返回的格式
          if (res && res.task_id) {
            return { task_id: res.task_id }
          }
          return res
        } catch (e) {
          return response
        }
      },
    ],
  })
}

export function getEnrichmentResults(params) {
  return request({
    url: '/api/enrichment/results',
    method: 'get',
    params,
  })
}

// 添加到导出对象中
export default {
  runEnrichmentAnalysis(data) {
    return request({
      url: '/api/enrichment/analyze',
      method: 'post',
      data,
      headers: {
        'Content-Type': 'application/json',
      },
      timeout: 60000,
    })
  },

  getEnrichmentResults(params) {
    return request({
      url: '/api/enrichment/results',
      method: 'get',
      params,
    })
  },
}
