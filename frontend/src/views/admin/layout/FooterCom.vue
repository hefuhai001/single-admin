<template>
    <div
        class="text-center text-xs text-gray-400 dark:text-gray-500 leading-6 bg-white dark:bg-[#1d1e1f]"
    >
        Copyright &copy; 2026 {{ siteTitle }}
        <a
            v-if="icp"
            href="https://beian.miit.gov.cn/"
            target="_blank"
            rel="noopener noreferrer"
            class="ml-2 hover:underline"
        >{{ icp }}</a>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import { useConfigStore } from '@/stores/modules/config'

const configStore = useConfigStore()
const siteTitle = computed(() => configStore.getValue('site.title', 'Single Admin'))
const icp = computed(() => configStore.getValue('site.icp', ''))
</script>

<style scoped></style>
