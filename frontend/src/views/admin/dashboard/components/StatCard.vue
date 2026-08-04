<template>
    <div
        class="stat-card relative overflow-hidden rounded-xl p-5 bg-white dark:bg-[#2a2b2c] border border-gray-100 dark:border-white/10 transition-all duration-300 hover:shadow-lg hover:-translate-y-0.5"
    >
        <div class="flex items-center justify-between">
            <div>
                <p class="text-sm text-gray-500 dark:text-gray-400 mb-1">{{ title }}</p>
                <p class="text-2xl font-bold text-gray-800 dark:text-gray-100">{{ value }}</p>
                <p class="text-xs mt-2" :class="trend >= 0 ? 'text-green-500' : 'text-red-500'">
                    <span>{{ trend >= 0 ? '+' : '' }}{{ trend }}%</span>
                    <span class="text-gray-400 ml-1">较上周</span>
                </p>
            </div>
            <div
                class="w-12 h-12 rounded-xl flex items-center justify-center text-white text-xl"
                :style="{ background: `linear-gradient(135deg, ${color}, ${colorEnd})` }"
            >
                <el-icon :size="24"><component :is="icon" /></el-icon>
            </div>
        </div>
        <div
            class="absolute -right-4 -bottom-4 w-24 h-24 rounded-full opacity-5"
            :style="{ background: color }"
        ></div>
    </div>
</template>

<script setup>
defineProps({
    title: { type: String, default: '' },
    value: { type: [String, Number], default: 0 },
    icon: { type: [String, Object], default: '' },
    color: { type: String, default: '#3b82f6' },
    colorEnd: { type: String, default: '#60a5fa' },
    trend: { type: Number, default: 0 },
})
</script>
