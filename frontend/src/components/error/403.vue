<template>
    <div class="forbidden-page">
        <!-- 浮动几何装饰 -->
        <div class="geo geo-1"></div>
        <div class="geo geo-2"></div>
        <div class="geo geo-3"></div>
        <div class="geo geo-4"></div>
        <div class="geo geo-5"></div>

        <div class="content">
            <div class="shield-wrap">
                <div class="shield">
                    <svg
                        viewBox="0 0 120 140"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                        class="shield-svg"
                    >
                        <path
                            d="M60 6L10 30V68C10 98.4 31.6 126.8 60 134C88.4 126.8 110 98.4 110 68V30L60 6Z"
                            fill="var(--el-color-primary-light-9)"
                            stroke="var(--el-color-primary-light-5)"
                            stroke-width="3"
                            class="shield-body"
                        />
                        <path
                            d="M60 6L10 30V68C10 98.4 31.6 126.8 60 134C88.4 126.8 110 98.4 110 68V30L60 6Z"
                            fill="url(#shieldGrad)"
                            opacity="0.3"
                        />
                        <defs>
                            <linearGradient
                                id="shieldGrad"
                                x1="60"
                                y1="6"
                                x2="60"
                                y2="134"
                                gradientUnits="userSpaceOnUse"
                            >
                                <stop stop-color="var(--el-color-primary)" />
                                <stop offset="1" stop-color="var(--el-color-primary-light-3)" />
                            </linearGradient>
                        </defs>
                    </svg>
                    <div class="lock-icon">
                        <svg viewBox="0 0 40 48" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect
                                x="4"
                                y="20"
                                width="32"
                                height="24"
                                rx="4"
                                fill="var(--el-color-primary)"
                                class="lock-body"
                            />
                            <path
                                d="M12 20V14C12 9.58 15.58 6 20 6C24.42 6 28 9.58 28 14V20"
                                stroke="var(--el-color-primary)"
                                stroke-width="4"
                                stroke-linecap="round"
                                class="lock-shackle"
                            />
                            <circle cx="20" cy="32" r="4" fill="white" opacity="0.9" />
                            <rect
                                x="18.5"
                                y="34"
                                width="3"
                                height="6"
                                rx="1.5"
                                fill="white"
                                opacity="0.9"
                            />
                        </svg>
                    </div>
                </div>
                <div class="shield-glow"></div>
            </div>

            <h2 class="title">访问受限</h2>
            <p class="desc">抱歉，您没有权限访问该页面，请联系管理员获取授权</p>

            <div class="actions">
                <el-button type="primary" size="large" @click="goHome">
                    <el-icon class="mr-1"><HomeFilled /></el-icon>
                    返回首页
                </el-button>
                <el-button size="large" @click="goBack">
                    <el-icon class="mr-1"><Back /></el-icon>
                    返回上页
                </el-button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { HomeFilled, Back } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => router.push('/')
const goBack = () => router.back()
</script>

<style scoped>
.forbidden-page {
    position: relative;
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    background: linear-gradient(160deg, #fef2f2 0%, #fff1f2 40%, #fdf4ff 100%);
}

.dark .forbidden-page {
    background: linear-gradient(160deg, #0f172a 0%, #1c1917 40%, #0f172a 100%);
}

/* ===== 浮动几何装饰 ===== */
.geo {
    position: absolute;
    border-radius: 50%;
    opacity: 0.1;
    pointer-events: none;
}

.dark .geo {
    opacity: 0.06;
}

.geo-1 {
    width: 280px;
    height: 280px;
    background: var(--el-color-primary);
    top: -100px;
    left: -80px;
    animation: float-1 9s ease-in-out infinite;
}

.geo-2 {
    width: 160px;
    height: 160px;
    background: var(--el-color-primary-light-5);
    bottom: 5%;
    right: -30px;
    animation: float-2 11s ease-in-out infinite;
}

.geo-3 {
    width: 80px;
    height: 80px;
    background: var(--el-color-primary-light-3);
    top: 25%;
    right: 12%;
    animation: float-3 7s ease-in-out infinite;
}

.geo-4 {
    width: 120px;
    height: 120px;
    background: var(--el-color-primary-light-7);
    bottom: 30%;
    left: 8%;
    animation: float-1 8s ease-in-out infinite;
}

.geo-5 {
    width: 50px;
    height: 50px;
    background: var(--el-color-primary-light-5);
    top: 12%;
    left: 30%;
    animation: float-2 6s ease-in-out infinite;
}

@keyframes float-1 {
    0%,
    100% {
        transform: translateY(0) scale(1);
    }
    50% {
        transform: translateY(-18px) scale(1.04);
    }
}

@keyframes float-2 {
    0%,
    100% {
        transform: translate(0, 0) rotate(0deg);
    }
    50% {
        transform: translate(12px, -22px) rotate(8deg);
    }
}

@keyframes float-3 {
    0%,
    100% {
        transform: translate(0, 0);
    }
    33% {
        transform: translate(8px, -12px);
    }
    66% {
        transform: translate(-8px, 8px);
    }
}

/* ===== 内容区 ===== */
.content {
    position: relative;
    z-index: 1;
    text-align: center;
    padding: 2rem;
}

/* ===== 盾牌 + 锁 ===== */
.shield-wrap {
    position: relative;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 2.5rem;
}

.shield {
    position: relative;
    width: 140px;
    height: 160px;
    animation: shield-float 4s ease-in-out infinite;
}

.shield-svg {
    width: 100%;
    height: 100%;
    filter: drop-shadow(0 4px 20px rgba(59, 130, 246, 0.15));
}

.dark .shield-svg {
    filter: drop-shadow(0 4px 20px rgba(96, 165, 250, 0.1));
}

.shield-body {
    transition:
        fill 0.3s,
        stroke 0.3s;
}

.lock-icon {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -45%);
    width: 44px;
    height: 52px;
    animation: lock-shake 4s ease-in-out infinite;
}

.lock-body {
    animation: lock-pulse 3s ease-in-out infinite;
}

.lock-shackle {
    animation: shackle-rattle 4s ease-in-out infinite;
}

@keyframes shield-float {
    0%,
    100% {
        transform: translateY(0);
    }
    50% {
        transform: translateY(-6px);
    }
}

@keyframes lock-pulse {
    0%,
    100% {
        opacity: 1;
    }
    50% {
        opacity: 0.85;
    }
}

@keyframes lock-shake {
    0%,
    85%,
    100% {
        transform: translate(-50%, -45%) rotate(0deg);
    }
    88% {
        transform: translate(-50%, -45%) rotate(-3deg);
    }
    91% {
        transform: translate(-50%, -45%) rotate(3deg);
    }
    94% {
        transform: translate(-50%, -45%) rotate(-2deg);
    }
    97% {
        transform: translate(-50%, -45%) rotate(2deg);
    }
}

@keyframes shackle-rattle {
    0%,
    85%,
    100% {
        transform: translateY(0);
    }
    88% {
        transform: translateY(-2px);
    }
    91% {
        transform: translateY(1px);
    }
    94% {
        transform: translateY(-1px);
    }
    97% {
        transform: translateY(0);
    }
}

.shield-glow {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 200px;
    height: 200px;
    border-radius: 50%;
    background: radial-gradient(circle, var(--el-color-primary-light-8) 0%, transparent 70%);
    opacity: 0.5;
    animation: glow-pulse 4s ease-in-out infinite;
    pointer-events: none;
}

.dark .shield-glow {
    background: radial-gradient(circle, rgba(59, 130, 246, 0.15) 0%, transparent 70%);
    opacity: 0.6;
}

@keyframes glow-pulse {
    0%,
    100% {
        transform: translate(-50%, -50%) scale(1);
        opacity: 0.5;
    }
    50% {
        transform: translate(-50%, -50%) scale(1.15);
        opacity: 0.3;
    }
}

/* ===== 文字 ===== */
.title {
    font-size: 1.75rem;
    font-weight: 700;
    color: #1e293b;
    margin: 0 0 0.75rem;
    letter-spacing: -0.02em;
}

.dark .title {
    color: #e2e8f0;
}

.desc {
    font-size: 1rem;
    color: #64748b;
    margin: 0 0 2.5rem;
    line-height: 1.6;
    max-width: 400px;
    margin-left: auto;
    margin-right: auto;
}

.dark .desc {
    color: #94a3b8;
}

/* ===== 按钮 ===== */
.actions {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 1rem;
    flex-wrap: wrap;
}

.actions :deep(.el-button) {
    border-radius: 10px;
    font-weight: 500;
    padding: 12px 28px;
    height: auto;
}

.mr-1 {
    margin-right: 4px;
}

/* ===== 响应式 ===== */
@media (max-width: 640px) {
    .shield {
        width: 110px;
        height: 126px;
    }

    .lock-icon {
        width: 34px;
        height: 40px;
    }

    .shield-glow {
        width: 150px;
        height: 150px;
    }

    .title {
        font-size: 1.35rem;
    }

    .desc {
        font-size: 0.9rem;
    }
}
</style>
