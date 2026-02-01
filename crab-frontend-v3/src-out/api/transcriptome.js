import request from '@/utils/request'

export function searchExpression(params) {
  return request({
    url: '/api/transcriptome/expression',
    method: 'post',
    data: params,
  })
}
