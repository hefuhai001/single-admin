import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vitest/config'
import { loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'

// 与 Vite 共用项目根目录的 .env
const rootDir = fileURLToPath(new URL('../', import.meta.url))
const env = loadEnv('test', rootDir)

export default defineConfig({
    envDir: rootDir,
    // CI / 未创建 .env 的环境也能拿到默认值
    define: {
        'import.meta.env.VITE_API_URL': JSON.stringify(env.VITE_API_URL || '/api'),
    },
    plugins: [vue(), vueJsx()],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url)),
        },
    },
    test: {
        environment: 'jsdom',
        globals: true,
        setupFiles: ['./tests/setup.js'],
        include: ['tests/**/*.spec.js'],
        restoreMocks: true,
        coverage: {
            provider: 'v8',
            reporter: ['text', 'text-summary', 'html', 'json-summary'],
            reportsDirectory: './coverage',
            include: ['src/**/*.{js,vue}'],
            exclude: ['src/main.js', 'src/router/modules/viewRegistry.generated.js'],
            thresholds: {
                lines: 75,
                statements: 75,
                branches: 75,
            },
        },
    },
})
