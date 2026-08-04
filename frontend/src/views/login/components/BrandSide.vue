<template>
    <div class="brand-side">
        <div class="brand-content">
            <div class="brand-icon">
                <svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="0" y="0" width="6" height="80" rx="3" fill="white" opacity="0.9" />
                    <rect x="0" y="74" width="80" height="6" rx="3" fill="white" opacity="0.9" />
                    <circle cx="26" cy="42" r="10" fill="white" opacity="0.85" />
                    <circle cx="68" cy="50" r="3" fill="white" opacity="0.5" />
                    <circle cx="54" cy="36" r="4" fill="white" opacity="0.4" />
                    <circle cx="28" cy="14" r="6" fill="white" opacity="0.3" />
                    <circle cx="52" cy="52" r="6" fill="white" opacity="0.55" />
                    <circle cx="68" cy="22" r="6" fill="white" opacity="0.85" />
                </svg>
            </div>
            <h1 class="brand-title">Single Admin Base</h1>
            <p class="brand-subtitle">企业级一站式解决方案</p>
            <div class="brand-features">
                <div v-for="feature in features" :key="feature" class="feature-item">
                    <div class="feature-dot"></div>
                    <span>{{ feature }}</span>
                </div>
            </div>
        </div>
        <!-- 品牌区装饰圆 -->
        <div class="brand-deco brand-deco-1"></div>
        <div class="brand-deco brand-deco-2"></div>
    </div>
</template>

<script setup>
const features = ['智能问卷设计', '高效数据采集', '多维度分析']
</script>

<style scoped>
.brand-side {
    position: relative;
    width: 42%;
    padding: 3rem 2.5rem;
    background: linear-gradient(
        160deg,
        var(--el-color-primary) 0%,
        var(--el-color-primary-light-3) 100%
    );
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
}

.brand-content {
    position: relative;
    z-index: 1;
    color: white;
}

.brand-icon {
    width: 64px;
    height: 64px;
    margin-bottom: 1.5rem;
    animation: icon-float 4s ease-in-out infinite;
}

.brand-icon svg {
    width: 100%;
    height: 100%;
}

@keyframes icon-float {
    0%,
    100% {
        transform: translateY(0);
    }
    50% {
        transform: translateY(-6px);
    }
}

.brand-title {
    font-size: 1.75rem;
    font-weight: 800;
    margin: 0 0 0.5rem;
    letter-spacing: 0.02em;
}

.brand-subtitle {
    font-size: 0.95rem;
    opacity: 0.8;
    margin: 0 0 2rem;
}

.brand-features {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
}

.feature-item {
    display: flex;
    align-items: center;
    gap: 0.6rem;
    font-size: 0.9rem;
    opacity: 0.85;
}

.feature-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: white;
    opacity: 0.7;
    flex-shrink: 0;
}

.brand-deco {
    position: absolute;
    border-radius: 50%;
    background: white;
    pointer-events: none;
}

.brand-deco-1 {
    width: 200px;
    height: 200px;
    opacity: 0.06;
    top: -60px;
    right: -40px;
    animation: float-1 7s ease-in-out infinite;
}

.brand-deco-2 {
    width: 120px;
    height: 120px;
    opacity: 0.05;
    bottom: -30px;
    left: -20px;
    animation: float-2 9s ease-in-out infinite;
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

@media (max-width: 768px) {
    .brand-side {
        width: 100%;
        padding: 2.5rem 2rem 2rem;
    }

    .brand-features {
        display: none;
    }
}

@media (max-width: 480px) {
    .brand-title {
        font-size: 1.4rem;
    }

    .brand-subtitle {
        font-size: 0.85rem;
        margin-bottom: 0;
    }

    .brand-icon {
        width: 48px;
        height: 48px;
        margin-bottom: 1rem;
    }
}
</style>
