import { defineConfig, devices } from '@playwright/test'

/**
 * Playwright E2E 配置。
 *
 * 用例通过路由拦截（e2e/helpers/mockApi.js）桩掉后端接口，
 * 因此无需启动 Spring Boot 与 MySQL/Redis，即可在 CI 中稳定运行。
 * 需要针对真实后端跑时，设置 E2E_BASE_URL 指向已部署的前端地址。
 */
// 与 webServer 的 --host 127.0.0.1 保持一致，避免 localhost 解析到 IPv6 时探测失败
const baseURL = process.env.E2E_BASE_URL || 'http://127.0.0.1:9998'

export default defineConfig({
    testDir: './e2e',
    fullyParallel: true,
    forbidOnly: !!process.env.CI,
    retries: process.env.CI ? 2 : 0,
    workers: process.env.CI ? 1 : undefined,
    // Vite 首次访问会做依赖预构建并整页重载，给足超时避免误判为用例失败
    timeout: 60000,
    expect: { timeout: 15000 },
    reporter: process.env.CI
        ? [['list'], ['html', { open: 'never', outputFolder: 'playwright-report' }]]
        : [['list']],
    use: {
        baseURL,
        actionTimeout: 15000,
        navigationTimeout: 30000,
        trace: 'on-first-retry',
        screenshot: 'only-on-failure',
    },
    projects: [
        {
            name: 'chromium',
            use: {
                ...devices['Desktop Chrome'],
                // 本地使用系统已安装的 Chrome（避免下载浏览器）；CI 使用 Playwright 自带 chromium
                channel: process.env.CI ? undefined : 'chrome',
            },
        },
    ],
    webServer: process.env.E2E_BASE_URL
        ? undefined
        : {
              // 使用构建产物 + preview：dev 模式首次访问会触发依赖预构建并整页重载，
              // 用例容易在重载窗口内抓到空白页，preview 无此问题。
              command: 'pnpm build && pnpm preview --port 9998 --strictPort --host 127.0.0.1',
              url: baseURL,
              // 始终启动干净的 preview：复用残留 dev server 会因依赖预构建整页重载导致白屏
              reuseExistingServer: false,
              timeout: 180000,
          },
})
