import { ref } from 'vue'
import { defineStore } from 'pinia'
import api from '@/api/index.js'

export const useRouterStore = defineStore('router', () => {
    const dynamicRoutes = ref([])
    const permissions = ref({})

    async function initDynamicRoutes() {
        const res = await api.user.routesUser()
        if (res.data && res.data.data && res.data.data.length > 0) {
            dynamicRoutes.value = res.data.data
        }
    }

    function setDynamicRoutes(value) {
        dynamicRoutes.value = value
    }

    function getDynamicRoutes() {
        return dynamicRoutes.value
    }

    async function initPermissions() {
        const res = await api.user.permissionsUser()
        if (res.data && res.data.data && res.data.data.length > 0) {
            permissions.value = res.data.data
        }
    }

    function setPermissions(value) {
        permissions.value = value
    }

    function getPermissions() {
        return permissions.value
    }

    function hasPermission(permissionCode) {
        return permissions.value?.includes(permissionCode)
    }

    return {
        initDynamicRoutes,
        initPermissions,
        dynamicRoutes,
        getDynamicRoutes,
        setDynamicRoutes,
        permissions,
        getPermissions,
        setPermissions,
        hasPermission,
    }
})
