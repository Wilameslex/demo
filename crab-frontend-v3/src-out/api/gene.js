import request from '@/utils/request'

export function searchGenes(data) {
  return request({
    url: '/genes/search', // 注意这里不带/api前缀（代理会自动添加）
    method: 'post',
    data,
  })
}

export function batchSearchGenes(genes) {
  return request({
    url: '/genes/batch-search',
    method: 'post',
    data: { genes },
  })
}
export function exportAllGenes(data) {
  return request({
    url: '/genes/export',
    method: 'post',
    data,
    responseType: 'blob', // 重要：指定响应类型为二进制
  })
}
