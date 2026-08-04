/**
 * ESLint 9 flat 配置（eslint.config.js）
 * 作用范围：所有 ts/mts/tsx/vue 文件
 * 1. 继承 eslint-plugin-vue 的「recommended」规则集
 * 2. 关闭 Prettier 与 ESLint 冲突的格式化规则（skipFormatting）
 * 3. 统一关闭与 TypeScript/Vue 开发体验冲突的默认规则
 * 4. 强制组件文件名及模板内组件名使用 kebab-case
 * 5. 其余规则一律禁用，交由 Prettier 统一格式化
 */

import pluginVue from 'eslint-plugin-vue'
import prettier from 'eslint-plugin-prettier'
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting'

export default [
    {
        name: 'app/files-to-lint',
        files: ['**/*.{ts,mts,tsx,vue}'],
    },
    {
        name: 'app/files-to-ignore',
        ignores: [
            '**/dist/**',
            '**/dist-ssr/**',
            '**/coverage/**',
            '**/packages/**',
            '**/build/**',
        ],
    },
    ...pluginVue.configs['flat/recommended'],
    skipFormatting,
    {
        languageOptions: {
            parserOptions: {
                ecmaVersion: 2020,
                ecmaFeatures: { jsx: true },
            },
        },
        plugins: { prettier },
        rules: {
            /* ===== TypeScript 相关 ===== */
            '@typescript-eslint/ban-ts-ignore': 'off', // 允许使用已废弃的 @ts-ignore 注释
            '@typescript-eslint/explicit-function-return-type': 'off', // 不要求给函数显式写返回类型
            '@typescript-eslint/no-explicit-any': 'off', // 允许使用 any 类型
            '@typescript-eslint/no-var-requires': 'off', // 允许使用 require() 导入模块
            '@typescript-eslint/no-empty-function': 'off', // 允许写空函数体
            '@typescript-eslint/no-use-before-define': 'off', // 允许在变量/函数定义之前使用它们
            '@typescript-eslint/ban-ts-comment': 'off', // 允许使用 @ts-comment 类指令（如 @ts-expect-error）
            '@typescript-eslint/ban-types': 'off', // 允许使用被标记为废弃的类型（如 Boolean、String）
            '@typescript-eslint/no-non-null-assertion': 'off', // 允许使用非空断言符 !
            '@typescript-eslint/explicit-module-boundary-types': 'off', // 不要求模块导出函数/变量写显式返回类型
            '@typescript-eslint/no-namespace': 'off', // 允许使用 namespace 关键字
            '@typescript-eslint/no-unused-vars': 'off', // 允许声明了但未使用的变量
            '@typescript-eslint/no-empty-object-type': 'off', // 允许空对象类型（如 interface Foo {}）

            /* ===== 基础 JS 规则 ===== */
            'space-before-function-paren': 'off', // 不强制要求函数圆括号前有空格
            'no-unused-vars': 'off', // 允许声明了但未使用的变量（TS 同名规则已关闭）
            'no-use-before-define': 'off', // 允许在定义前使用变量/函数
            'no-self-assign': 'off', // 允许变量自己给自己赋值（a = a）

            /* ===== Vue 规则 ===== */
            'vue/no-mutating-props': 'off', // 允许直接修改组件 props（会报警告但不再报错）
            'vue/no-template-shadow': 'off', // 允许模板内变量名覆盖外层作用域变量
            'vue/no-v-html': 'off', // 允许使用 v-html（存在 XSS 风险但不再报错）
            'vue/component-name-in-template-casing': ['error', 'kebab-case'], // 模板里调用的组件名必须用 kebab-case（my-component）
            'vue/component-definition-name-casing': ['error', 'kebab-case'], // 组件定义时的 name 字段必须用 kebab-case
            'vue/attributes-order': 'off', // 不强制属性按特定顺序书写
            'vue/one-component-per-file': 'off', // 允许一个 .vue 文件里写多个组件
            'vue/html-closing-bracket-newline': 'off', // 不强制 HTML 结束括号是否换行
            'vue/max-attributes-per-line': 'off', // 不限制每行最多写几个属性
            'vue/multiline-html-element-content-newline': 'off', // 不强制多行元素内容前后换行
            'vue/multi-word-component-names': 'off', // 允许组件名只用一个单词（如 Button）
            'vue/singleline-html-element-content-newline': 'off', // 不强制单行元素内容前后换行
            'vue/attribute-hyphenation': 'off', // 允许属性名用驼峰（:myProp）或连字符（:my-prop）
            'vue/html-self-closing': 'off', // 允许非空元素不写自关闭（如 <div></div> 可写成 <div/>）
            'vue/require-default-prop': 'off', // 不要求给可选 prop 提供默认值
            'vue/v-on-event-hyphenation': 'off', // 允许事件名用驼峰（@myEvent）或连字符（@my-event）
            'vue/block-lang': 'off', // 不要求 <script>/<style> 指定 lang（如 lang="ts"）
        },
    },
]
