<template>
    <div class="bg-gray-50 text-gray-900 dark:bg-neutral-50 dark:text-gray-100 h-auto">
        <main class="p-6">
            <div class="grid gap-8">
                <section
                    class="bg-white dark:bg-gray-800 rounded-2xl shadow-lg p-6 border border-gray-200 dark:border-gray-700"
                >
                    <h3 class="text-lg font-semibold mb-4 flex items-center gap-2">
                        <span class="w-4 h-4 rounded-full bg-blue-500"></span>
                        主色调
                    </h3>
                    <div class="grid grid-cols-5 md:grid-cols-10 gap-2">
                        <div
                            v-for="shade in [50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950]"
                            :key="shade"
                            class="text-center"
                        >
                            <div :class="`bg-theme-${shade} h-8 rounded-lg mb-1 shadow-sm`"></div>
                            <span class="text-xs text-gray-500 dark:text-gray-400 font-mono">{{
                                shade
                            }}</span>
                        </div>
                    </div>
                </section>

                <section
                    class="bg-white dark:bg-gray-800 rounded-2xl shadow-lg p-6 border border-gray-200 dark:border-gray-700"
                >
                    <h3 class="text-lg font-semibold mb-4 flex items-center gap-2">
                        <span class="w-4 h-4 rounded-full bg-purple-500"></span>
                        辅助色
                    </h3>
                    <div class="grid grid-cols-5 md:grid-cols-10 gap-2">
                        <div
                            v-for="shade in [50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950]"
                            :key="shade"
                            class="text-center"
                        >
                            <div :class="`bg-aided-${shade} h-8 rounded-lg mb-1 shadow-sm`"></div>
                            <span class="text-xs text-gray-500 dark:text-gray-400 font-mono">{{
                                shade
                            }}</span>
                        </div>
                    </div>
                </section>

                <section
                    class="bg-white dark:bg-gray-800 rounded-2xl shadow-lg p-6 border border-gray-200 dark:border-gray-700"
                >
                    <h3 class="text-lg font-semibold mb-4 flex items-center gap-2">
                        <span class="w-4 h-4 rounded-full bg-neutral-500"></span>
                        中性色
                    </h3>
                    <div class="grid grid-cols-5 md:grid-cols-10 gap-2">
                        <div
                            v-for="shade in [50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 950]"
                            :key="shade"
                            class="text-center"
                        >
                            <div :class="`bg-neutral-${shade} h-8 rounded-lg mb-1 shadow-sm`"></div>
                            <span class="text-xs text-gray-500 dark:text-gray-400 font-mono">{{
                                shade
                            }}</span>
                        </div>
                    </div>
                </section>
            </div>
        </main>
    </div>
</template>

<script setup></script>

<style scoped></style>
