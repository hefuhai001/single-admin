import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import path from 'path'

// https://vite.dev/config/
// 全项目唯一的 .env 位于项目根目录（frontend/ 的上一级）
const rootDir = fileURLToPath(new URL('../', import.meta.url))

// 各 mode 的默认代理目标；根目录 .env 中的 VITE_* 变量可覆盖
const proxyTargets = {
    development: {
        api: 'http://127.0.0.1:9999',
        ws: 'ws://127.0.0.1:9999',
        actuator: 'http://127.0.0.1:9999',
    },
    test: {
        api: 'http://39.98.51.161:9000/api',
        ws: 'ws://39.98.51.161:9000/api',
        actuator: 'http://39.98.51.161:9999/actuator',
    },
    production: {
        api: 'https://next.xsmartanalysis.com/api',
        ws: 'ws://next.xsmartanalysis.com/api',
        actuator: 'http://39.98.51.161:9999/actuator',
    },
}

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, rootDir)
    const proxyTarget = proxyTargets[mode] || proxyTargets.development

    return {
        envDir: rootDir,
        // CI / 未创建 .env 的环境也能构建出可用产物（默认值可被 .env 或 build args 覆盖）
        define: {
            'import.meta.env.VITE_API_URL': JSON.stringify(env.VITE_API_URL || '/api'),
        },
        plugins: [
            vue(),
            vueJsx(),
            vueDevTools(),
            AutoImport({
                resolvers: [ElementPlusResolver()],
            }),
            Components({
                resolvers: [ElementPlusResolver()],
            }),
            createSvgIconsPlugin({
                // 指定需要缓存的图标文件夹
                iconDirs: [path.resolve(process.cwd(), 'src/assets/icons')],
                // 指定symbolId格式
                symbolId: 'icon-[name]',
                // 使用 SVGO 自动处理 fill 属性
                svgoOptions: {
                    plugins: [
                        {
                            name: 'removeAttrs',
                            params: {
                                // 移除 fill 属性（除了 none）
                                attrs: ['fill'],
                            },
                        },
                    ],
                },
                // 自定义插入位置
                inject: 'body-last',
                // 自定义DOM ID
                customDomId: '__svg__icons__dom__',
            }),
        ],
        resolve: {
            alias: {
                '@': fileURLToPath(new URL('./src', import.meta.url)),
            },
        },
        build: {
            target: 'esnext',
            minify: 'terser',
            terserOptions: {
                compress: {
                    drop_console: true,
                    drop_debugger: true,
                },
            },
            rollupOptions: {
                output: {
                    // 手动分包：只按当前实际依赖划分，避免引入未使用的库判断
                    manualChunks: (id) => {
                        if (!id.includes('node_modules')) {
                            return undefined
                        }
                        // 图表库（打包体积最大的部分）
                        if (id.includes('echarts')) {
                            return 'charts'
                        }
                        // UI 组件库（含图标）
                        if (id.includes('element-plus') || id.includes('@element-plus')) {
                            return 'ui-vendor'
                        }
                        // 通用工具库
                        if (id.includes('axios') || id.includes('dayjs')) {
                            return 'utils'
                        }
                        // 其余第三方依赖
                        return 'vendor'
                    },
                },
            },
            cssCodeSplit: true,
            assetsInlineLimit: 4096,
        },
        server: {
            port: 9998,
            open: false,
            proxy: {
                '/api': {
                    target: env.VITE_API_PROXY || proxyTarget.api,
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/^\/api/, ''),
                    configure: (proxy) => {
                        proxy.on('error', (err) => {
                            console.log('代理错误:', err)
                        })
                        proxy.on('proxyReq', (proxyReq, req) => {
                            console.log('代理请求:', req.method, req.url)
                        })
                    },
                },
                '/ws': {
                    target: env.VITE_WS_PROXY || proxyTarget.ws,
                    changeOrigin: true,
                    ws: true,
                    rewrite: (path) => path.replace(/^\/ws/, ''),
                    configure: (proxy) => {
                        proxy.on('error', (err) => {
                            console.log('WebSocket 代理错误:', err)
                        })
                        proxy.on('proxyReqWs', (proxyReq, req) => {
                            console.log('WebSocket 连接:', req.url)
                        })
                    },
                },
                '/actuator': {
                    target: env.VITE_ACTUATOR_PROXY || proxyTarget.actuator,
                    changeOrigin: true,
                    headers: {
                        Authorization: 'Basic ' + btoa('admin:admin123'),
                    },
                    configure: (proxy) => {
                        proxy.on('error', (err) => {
                            console.log('代理错误:', err)
                        })
                        proxy.on('proxyReq', (proxyReq, req) => {
                            console.log('代理请求:', req.method, req.url)
                        })
                    },
                },
            },
        },
    }
})
