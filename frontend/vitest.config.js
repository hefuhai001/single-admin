import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'

export default defineConfig({
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
        },
    },
})
