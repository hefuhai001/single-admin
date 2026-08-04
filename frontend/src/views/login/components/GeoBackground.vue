<template>
    <div class="geo-background" aria-hidden="true">
        <div v-for="item in geoItems" :key="item.class" :class="['geo', item.class]"></div>
    </div>
</template>

<script setup>
const geoItems = [
    { class: 'geo-1' },
    { class: 'geo-2' },
    { class: 'geo-3' },
    { class: 'geo-4' },
    { class: 'geo-5' },
    { class: 'geo-6' },
    { class: 'geo-7' },
]
</script>

<style scoped>
.geo {
    position: absolute;
    border-radius: 50%;
    opacity: 0.12;
    pointer-events: none;
}

.dark .geo {
    opacity: 0.07;
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
    left: 8%;
    animation: float-3 7s ease-in-out infinite;
}

.geo-4 {
    width: 60px;
    height: 60px;
    background: var(--el-color-primary-light-7);
    bottom: 25%;
    right: 10%;
    animation: float-1 6s ease-in-out infinite;
}

.geo-5 {
    width: 140px;
    height: 140px;
    background: var(--el-color-primary-light-5);
    top: 65%;
    right: 5%;
    animation: float-2 9s ease-in-out infinite;
}

.geo-6 {
    width: 40px;
    height: 40px;
    background: var(--el-color-primary);
    top: 10%;
    right: 30%;
    animation: float-3 5s ease-in-out infinite;
}

.geo-7 {
    width: 200px;
    height: 200px;
    background: var(--el-color-primary-light-9);
    bottom: -60px;
    left: 25%;
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
</style>
