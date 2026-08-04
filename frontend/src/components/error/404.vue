<template>
    <div class="not-found-page">
        <!-- 浮动几何装饰 -->
        <div class="geo geo-1"></div>
        <div class="geo geo-2"></div>
        <div class="geo geo-3"></div>
        <div class="geo geo-4"></div>
        <div class="geo geo-5"></div>
        <div class="geo geo-6"></div>
        <div class="geo geo-7"></div>

        <div class="content">
            <div class="number-wrap">
                <span class="digit d1">4</span>
                <span class="digit-center">
                    <span class="planet"></span>
                    <span class="orbit orbit-1"></span>
                    <span class="orbit orbit-2"></span>
                    <span class="orbit-dot dot-1"></span>
                    <span class="orbit-dot dot-2"></span>
                </span>
                <span class="digit d2">4</span>
            </div>

            <h2 class="title">页面走丢了</h2>
            <p class="desc">您访问的页面可能已被移除、名称已更改，或暂时不可用</p>

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
.not-found-page {
    position: relative;
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    background: linear-gradient(160deg, #f0f5ff 0%, #e8f0fe 40%, #f5f3ff 100%);
}

.dark .not-found-page {
    background: linear-gradient(160deg, #0f172a 0%, #1e1b4b 40%, #0f172a 100%);
}

/* ===== 浮动几何装饰 ===== */
.geo {
    position: absolute;
    border-radius: 50%;
    opacity: 0.12;
    pointer-events: none;
}

.dark .geo {
    opacity: 0.08;
}

.geo-1 {
    width: 300px;
    height: 300px;
    background: var(--el-color-primary);
    top: -80px;
    right: -60px;
    animation: float-1 8s ease-in-out infinite;
}

.geo-2 {
    width: 180px;
    height: 180px;
    background: var(--el-color-primary-light-5);
    bottom: 10%;
    left: -40px;
    animation: float-2 10s ease-in-out infinite;
}

.geo-3 {
    width: 100px;
    height: 100px;
    background: var(--el-color-primary-light-3);
    top: 20%;
    left: 12%;
    animation: float-3 7s ease-in-out infinite;
}

.geo-4 {
    width: 60px;
    height: 60px;
    background: var(--el-color-primary-light-7);
    bottom: 25%;
    right: 15%;
    animation: float-1 6s ease-in-out infinite;
}

.geo-5 {
    width: 140px;
    height: 140px;
    background: var(--el-color-primary-light-5);
    top: 60%;
    right: 8%;
    animation: float-2 9s ease-in-out infinite;
}

.geo-6 {
    width: 40px;
    height: 40px;
    background: var(--el-color-primary);
    top: 15%;
    right: 25%;
    animation: float-3 5s ease-in-out infinite;
}

.geo-7 {
    width: 200px;
    height: 200px;
    background: var(--el-color-primary-light-9);
    bottom: -60px;
    left: 30%;
    animation: float-1 11s ease-in-out infinite;
}

@keyframes float-1 {
    0%,
    100% {
        transform: translateY(0) scale(1);
    }
    50% {
        transform: translateY(-20px) scale(1.05);
    }
}

@keyframes float-2 {
    0%,
    100% {
        transform: translate(0, 0) rotate(0deg);
    }
    50% {
        transform: translate(15px, -25px) rotate(10deg);
    }
}

@keyframes float-3 {
    0%,
    100% {
        transform: translate(0, 0);
    }
    33% {
        transform: translate(10px, -15px);
    }
    66% {
        transform: translate(-10px, 10px);
    }
}

/* ===== 内容区 ===== */
.content {
    position: relative;
    z-index: 1;
    text-align: center;
    padding: 2rem;
}

/* ===== 404 数字 ===== */
.number-wrap {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0;
    margin-bottom: 2rem;
    user-select: none;
}

.digit {
    font-size: 10rem;
    font-weight: 900;
    line-height: 1;
    background: linear-gradient(
        135deg,
        var(--el-color-primary) 0%,
        var(--el-color-primary-light-3) 100%
    );
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    animation: digit-bounce 3s ease-in-out infinite;
}

.dark .digit {
    background: linear-gradient(
        135deg,
        var(--el-color-primary-light-5) 0%,
        var(--el-color-primary-light-7) 100%
    );
    -webkit-background-clip: text;
    background-clip: text;
}

.d1 {
    animation-delay: 0s;
}
.d2 {
    animation-delay: 0.15s;
}

@keyframes digit-bounce {
    0%,
    100% {
        transform: translateY(0);
    }
    50% {
        transform: translateY(-8px);
    }
}

/* ===== 中间的星球 + 轨道 ===== */
.digit-center {
    position: relative;
    width: 10rem;
    height: 10rem;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.planet {
    position: absolute;
    width: 3.5rem;
    height: 3.5rem;
    border-radius: 50%;
    background: linear-gradient(135deg, var(--el-color-primary-light-5), var(--el-color-primary));
    box-shadow:
        0 0 30px rgba(59, 130, 246, 0.3),
        0 0 60px rgba(59, 130, 246, 0.15),
        inset -8px -8px 16px rgba(0, 0, 0, 0.15);
    animation: planet-pulse 4s ease-in-out infinite;
}

.dark .planet {
    box-shadow:
        0 0 30px rgba(96, 165, 250, 0.25),
        0 0 60px rgba(96, 165, 250, 0.1),
        inset -8px -8px 16px rgba(0, 0, 0, 0.3);
}

@keyframes planet-pulse {
    0%,
    100% {
        transform: scale(1);
        box-shadow:
            0 0 30px rgba(59, 130, 246, 0.3),
            0 0 60px rgba(59, 130, 246, 0.15);
    }
    50% {
        transform: scale(1.05);
        box-shadow:
            0 0 40px rgba(59, 130, 246, 0.4),
            0 0 80px rgba(59, 130, 246, 0.2);
    }
}

.orbit {
    position: absolute;
    border: 2px solid var(--el-color-primary-light-7);
    border-radius: 50%;
    opacity: 0.5;
}

.dark .orbit {
    border-color: var(--el-color-primary-light-5);
    opacity: 0.2;
}

.orbit-1 {
    width: 7rem;
    height: 7rem;
    animation: orbit-spin 8s linear infinite;
}

.orbit-2 {
    width: 9.5rem;
    height: 9.5rem;
    border-style: dashed;
    animation: orbit-spin 14s linear infinite reverse;
}

@keyframes orbit-spin {
    from {
        transform: rotate(0deg);
    }
    to {
        transform: rotate(360deg);
    }
}

.orbit-dot {
    position: absolute;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: var(--el-color-primary-light-5);
}

.dot-1 {
    top: 50%;
    left: 50%;
    transform: translate(-50%, -4rem);
    animation: orbit-spin 8s linear infinite;
    transform-origin: 0 4rem;
}

.dot-2 {
    top: 50%;
    left: 50%;
    transform: translate(-50%, -5.25rem);
    background: var(--el-color-primary-light-7);
    width: 7px;
    height: 7px;
    animation: orbit-spin 14s linear infinite reverse;
    transform-origin: 0 5.25rem;
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
    .digit {
        font-size: 6rem;
    }

    .digit-center {
        width: 6rem;
        height: 6rem;
    }

    .planet {
        width: 2.5rem;
        height: 2.5rem;
    }

    .orbit-1 {
        width: 4.5rem;
        height: 4.5rem;
    }

    .orbit-2 {
        width: 6rem;
        height: 6rem;
    }

    .dot-1 {
        transform: translate(-50%, -2.5rem);
        transform-origin: 0 2.5rem;
    }

    .dot-2 {
        transform: translate(-50%, -3.25rem);
        transform-origin: 0 3.25rem;
    }

    .title {
        font-size: 1.35rem;
    }

    .desc {
        font-size: 0.9rem;
    }
}
</style>
