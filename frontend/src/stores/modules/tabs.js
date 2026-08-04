import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const buildFullPath = (path, query) => {
    if (!query || Object.keys(query).length === 0) return path
    const qs = new URLSearchParams(query).toString()
    return `${path}?${qs}`
}

export const useTabsStore = defineStore(
    'tabs',
    () => {
        const router = useRouter()

        const visitedTabs = ref([])

        const activeTab = ref('')

        const cachedTabs = computed(() => {
            return visitedTabs.value.filter((tab) => !tab.noCache).map((tab) => tab.name)
        })

        const addTab = (route) => {
            const { path, name, meta, query } = route

            if (!name) return

            const fullPath = buildFullPath(path, query)
            const existing = visitedTabs.value.find((tab) => tab.path === path)

            if (!existing) {
                visitedTabs.value.push({
                    path,
                    name,
                    title: meta.title || name,
                    affix: meta.affix || false,
                    noCache: meta.noCache || false,
                    query: query || {},
                    fullPath,
                })
            } else {
                existing.query = query || {}
                existing.fullPath = fullPath
            }

            activeTab.value = fullPath
        }

        const navigateToTab = (tab) => {
            router.push({ path: tab.path, query: tab.query || {} })
        }

        const closeTab = (fullPath) => {
            const index = visitedTabs.value.findIndex((tab) => tab.fullPath === fullPath)

            if (index === -1) return

            const tab = visitedTabs.value[index]

            if (tab.affix) return

            visitedTabs.value.splice(index, 1)

            if (activeTab.value === fullPath) {
                const nextTab = visitedTabs.value[index] || visitedTabs.value[index - 1]
                if (nextTab) {
                    activeTab.value = nextTab.fullPath
                    navigateToTab(nextTab)
                }
            }
        }

        const closeOtherTabs = (fullPath) => {
            visitedTabs.value = visitedTabs.value.filter((tab) => {
                return tab.affix || tab.fullPath === fullPath
            })

            if (activeTab.value !== fullPath) {
                activeTab.value = fullPath
                const tab = visitedTabs.value.find((t) => t.fullPath === fullPath)
                if (tab) navigateToTab(tab)
            }
        }

        const closeLeftTabs = (fullPath) => {
            const index = visitedTabs.value.findIndex((tab) => tab.fullPath === fullPath)

            if (index === -1) return

            visitedTabs.value = visitedTabs.value.filter((tab, i) => {
                return tab.affix || i >= index
            })

            if (!visitedTabs.value.find((tab) => tab.fullPath === activeTab.value)) {
                activeTab.value = fullPath
                const tab = visitedTabs.value.find((t) => t.fullPath === fullPath)
                if (tab) navigateToTab(tab)
            }
        }

        const closeRightTabs = (fullPath) => {
            const index = visitedTabs.value.findIndex((tab) => tab.fullPath === fullPath)

            if (index === -1) return

            visitedTabs.value = visitedTabs.value.filter((tab, i) => {
                return tab.affix || i <= index
            })

            if (!visitedTabs.value.find((tab) => tab.fullPath === activeTab.value)) {
                activeTab.value = fullPath
                const tab = visitedTabs.value.find((t) => t.fullPath === fullPath)
                if (tab) navigateToTab(tab)
            }
        }

        const closeAllTabs = () => {
            visitedTabs.value = visitedTabs.value.filter((tab) => tab.affix)

            const lastTab = visitedTabs.value[visitedTabs.value.length - 1]
            if (lastTab) {
                activeTab.value = lastTab.fullPath
                navigateToTab(lastTab)
            }
        }

        const setActiveTab = (fullPath) => {
            activeTab.value = fullPath
            const tab = visitedTabs.value.find((t) => t.fullPath === fullPath)
            if (tab) navigateToTab(tab)
        }

        const initTabs = (routes) => {
            routes.forEach((route) => {
                if (route.meta && route.meta.affix) {
                    addTab(route)
                }
            })
        }

        return {
            visitedTabs,
            activeTab,
            cachedTabs,
            addTab,
            closeTab,
            closeOtherTabs,
            closeLeftTabs,
            closeRightTabs,
            closeAllTabs,
            setActiveTab,
            initTabs,
        }
    },
    {
        persist: {
            key: 'tabs',
            storage: localStorage,
        },
    },
)
