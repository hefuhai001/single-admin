<template>
    <div class="h-full flex flex-col gap-4">
        <el-card shadow="never" class="shrink-0">
            <el-form :model="queryParams" inline>
                <el-form-item label="用户名">
                    <el-input
                        v-model="queryParams.username"
                        placeholder="请输入用户名"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="handleQuery">
                        <el-icon><Search /></el-icon> 搜索
                    </el-button>
                    <el-button @click="handleRefresh">
                        <el-icon><Refresh /></el-icon> 刷新
                    </el-button>
                </el-form-item>
            </el-form>
        </el-card>

        <el-card shadow="never" class="flex-1 min-h-0">
            <el-table
                border
                v-loading="loading"
                :data="filteredList"
                @selection-change="handleSelectionChange"
            >
                <el-table-column type="selection" width="55" align="center" />
                <el-table-column label="用户ID" prop="userId" width="90" align="center" />
                <el-table-column
                    label="用户名"
                    prop="username"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="昵称"
                    prop="nickname"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="设备标识"
                    prop="deviceId"
                    min-width="240"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="登录IP"
                    prop="loginIp"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="登录时间"
                    prop="loginTime"
                    min-width="160"
                    align="center"
                />
                <el-table-column label="操作" width="120" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            type="danger"
                            link
                            @click="handleKick(row)"
                            v-if="routerStore.hasPermission('monitor:online:kick')"
                        >
                            强退
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getOnlineUsers, kickOnlineUser } from '@/api/modules/monitor/online.js'
import { useRouterStore } from '@/stores/modules/router.js'

const routerStore = useRouterStore()

const loading = ref(false)
const tableData = ref([])
const selectedIds = ref([])
const queryParams = ref({
    username: ''
})

const filteredList = computed(() => {
    if (!queryParams.value.username) {
        return tableData.value
    }
    const keyword = queryParams.value.username.toLowerCase()
    return tableData.value.filter(
        item =>
            (item.username && item.username.toLowerCase().includes(keyword)) ||
            (item.nickname && item.nickname.toLowerCase().includes(keyword))
    )
})

function handleSelectionChange(rows) {
    selectedIds.value = rows.map(row => row.userId)
}

function handleQuery() {
    // 搜索由 computed 自动处理
}

async function loadList() {
    loading.value = true
    try {
        const res = await getOnlineUsers()
        tableData.value = res.data?.data || []
    } catch (error) {
        console.error('获取在线用户失败', error)
    } finally {
        loading.value = false
    }
}

function handleRefresh() {
    queryParams.value.username = ''
    loadList()
}

async function handleKick(row) {
    try {
        await ElMessageBox.confirm(
            `确定要强制用户 "${row.username || row.nickname || row.userId}" 下线吗？`,
            '提示',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        )
        await kickOnlineUser(row.token)
        ElMessage.success('强退成功')
        loadList()
    } catch (error) {
        if (error !== 'cancel') {
            console.error('强退失败', error)
        }
    }
}

onMounted(() => {
    loadList()
})
</script>
