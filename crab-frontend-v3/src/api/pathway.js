import request from '@/utils/request'

export function searchPathway(data) {
    return request({
        url: '/pathway/search',
        method: 'post',
        data,
        transformRequest: [function (data) {  // 确保正确序列化
            return JSON.stringify(data);
        }],
        headers: {
            'Content-Type': 'application/json'  // 明确指定JSON类型
        }
    })
}