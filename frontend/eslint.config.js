/**
 * ESLint 10 flat 配置
 *
 * Lint 职责划分（避免 oxlint / eslint 规则重叠）：
 * - oxlint：负责全量静态检查（correctness 类规则），速度最快，CI 中先执行；
 * - eslint：只负责 Vue SFC 模板与组件命名等 Vue 专属规则，oxlint 已覆盖的规则由
 *   eslint-plugin-oxlint 统一关闭；
 * - prettier：只负责格式化（pnpm format），不参与 lint 流程，格式化相关规则统一关闭。
 */
import pluginVue from 'eslint-plugin-vue'
import eslintConfigPrettier from 'eslint-config-prettier/flat'
import oxlint from 'eslint-plugin-oxlint'

export default [
    {
        name: 'app/files-to-ignore',
        ignores: ['**/dist/**', '**/dist-ssr/**', '**/coverage/**', '**/packages/**', '**/build/**'],
    },
    {
        name: 'app/files-to-lint',
        files: ['**/*.{js,mjs,cjs,ts,mts,tsx,vue}'],
    },
    ...pluginVue.configs['flat/recommended'],
    // 关闭所有与 Prettier 冲突的格式化规则
    eslintConfigPrettier,
    // 关闭 oxlint 已覆盖的规则，eslint 只保留 Vue 专属规则
    ...oxlint.configs['flat/recommended'],
    {
        name: 'app/rules',
        rules: {
            /* ===== 基础 JS 规则 ===== */
            'no-unused-vars': 'off', // 由 oxlint 负责未使用变量检查
            'no-use-before-define': 'off', // 由 oxlint 负责
            'no-self-assign': 'off', // 由 oxlint 负责

            /* ===== Vue 规则 ===== */
            'vue/no-mutating-props': 'off', // 允许直接修改组件 props
            'vue/no-template-shadow': 'off', // 允许模板内变量名覆盖外层作用域变量
            'vue/no-v-html': 'off', // 允许使用 v-html（编辑器富文本等场景）
            'vue/component-name-in-template-casing': ['error', 'PascalCase'],
            'vue/component-definition-name-casing': ['error', 'kebab-case'],
            'vue/attributes-order': 'off',
            'vue/one-component-per-file': 'off',
            'vue/html-closing-bracket-newline': 'off',
            'vue/max-attributes-per-line': 'off',
            'vue/multiline-html-element-content-newline': 'off',
            'vue/multi-word-component-names': 'off',
            'vue/singleline-html-element-content-newline': 'off',
            'vue/attribute-hyphenation': 'off',
            'vue/html-self-closing': 'off',
            'vue/require-default-prop': 'off',
            'vue/v-on-event-hyphenation': 'off',
            'vue/block-lang': 'off',
        },
    },
]
