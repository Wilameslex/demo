import request from '@/utils/request';

export function searchExpression(data) {
    return request({
        url: '/expression/search', // 匹配后端的 @PostMapping("/search")
        method: 'post',
        data, // 直接使用传入的data对象
        headers: {
            'Content-Type': 'application/json'
        }
    });
}

export function getSamples(pipeline, transcriptome, searchType) {
    return request({
        url: '/expression/samples',
        method: 'get',
        params: {
            pipeline,
            transcriptome,
            searchType
        }
    });
}
// 🔴 新增：导出getProcessExprData（匹配后端接口路径）
export function getProcessExprData(data) {
    return request({
        url: '/expression/process/data', // 后端接口路径（必须和Controller的@RequestMapping一致）
        method: 'post',
        data, // 传递process/pipeline/searchType参数
        headers: {'Content-Type': 'application/json'}
    });
}