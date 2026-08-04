<!-- components/SvgIcon.vue -->
<template>
    <svg :class="['svg-icon', spin && 'svg-icon-spin']" :style="iconStyle" aria-hidden="true">
        <use :href="symbolId" :fill="color" />
    </svg>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
    prefix: {
        type: String,
        default: 'icon',
    },
    color: {
        type: String,
        default: 'currentColor',
    },
    size: {
        type: [Number, String],
        default: 16,
    },
    spin: {
        type: Boolean,
        default: false,
    },
    name: {
        type: String,
        required: true,
    },
})

const symbolId = computed(() => `#${props.prefix}-${props.name}`)

const iconStyle = computed(() => {
    const { size } = props
    const s = typeof size === 'number' ? `${size}px` : size
    return {
        width: s,
        height: s,
    }
})
</script>

<style scoped>
.svg-icon {
    display: inline-block;
    overflow: hidden;
    vertical-align: -0.15em;
    fill: currentColor;
    transition: all 0.3s;
}

.svg-icon-spin {
    animation: rotate 1s linear infinite;
}

@keyframes rotate {
    from {
        transform: rotate(0deg);
    }
    to {
        transform: rotate(360deg);
    }
}
</style>
