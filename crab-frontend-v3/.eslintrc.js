module.exports = {
    root: true,
    env: {
        node: true,
        browser: true,
        es2021: true
    },
    extends: [
        'plugin:vue/vue3-essential',
        'eslint:recommended'
    ],
    parserOptions: {
        parser: '@babel/eslint-parser',
        requireConfigFile: false,
        ecmaVersion: 2021,
        sourceType: 'module'
    },
    rules: {
        // 关闭未使用变量的警告
        'no-unused-vars': 'off',
        // 关闭Vue组件名必须多词的规则
        'vue/multi-word-component-names': 'off',
        // 关闭已弃用的slot属性警告
        'vue/no-deprecated-slot-attribute': 'off',
        // 关闭未定义变量的警告（如Vue全局变量）
        'no-undef': 'off'
    },
    globals: {
        // 定义全局变量
        'Vue': 'readonly',
        'process': 'readonly'
    }
}
