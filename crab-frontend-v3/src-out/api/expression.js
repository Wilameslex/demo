import request from '@/utils/request'

export function searchExpression(data) {
  return request({
    url: '/expression/search', // 匹配后端的 @PostMapping("/search")
    method: 'post',
    data, // 直接使用传入的data对象
    headers: {
      'Content-Type': 'application/json',
    },
  })
}

export function getSamples(pipeline, transcriptome, searchType) {
  return request({
    url: '/expression/samples',
    method: 'get',
    params: {
      pipeline,
      transcriptome,
      searchType,
    },
  })
}
