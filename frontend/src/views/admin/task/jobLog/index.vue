<template>
    <div class="h-full flex flex-col gap-4">
        <el-card shadow="never" class="shrink-0">
            <el-form :model="queryParams" inline v-if="routerStore.hasPermission('job:query')">
                <el-form-item label="任务名称">
                    <el-input
                        v-model="queryParams.jobName"
                        placeholder="请输入任务名称"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="执行状态">
                    <el-select
                        v-model="queryParams.status"
                        placeholder="请选择状态"
                        clearable
                        style="width: 120px"
                    >
                        <el-option label="失败" :value="0" />
                        <el-option label="成功" :value="1" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="handleQuery">
                        <el-icon><Search /></el-icon> 搜索
                    </el-button>
                </el-form-item>
            </el-form>
            <el-button @click="resetQuery">
                <el-icon><Refresh /></el-icon> 刷新
            </el-button>
            <el-button type="danger" @click="handleClean" v-if="routerStore.hasPermission('job:delete')">
                <el-icon><Delete /></el-icon> 清空
            </el-button>
            <el-button @click="goBack">
                <el-icon><Back /></el-icon> 返回
            </el-button>
        </el-card>

        <el-card shadow="never" class="flex-1 min-h-0">
            <el-table
                border
                v-loading="loading"
                :data="tableData"
            >
                <el-table-column label="ID" prop="id" width="90" align="center" />
                <el-table-column label="任务名称" prop="jobName" min-width="120" align="center" show-overflow-tooltip />
                <el-table-column label="任务分组" prop="jobGroup" min-width="100" align="center" show-overflow-tooltip />
                <el-table-column label="调用目标" prop="invokeTarget" min-width="180" align="center" show-overflow-tooltip />
                <el-table-column label="执行信息" prop="jobMessage" min-width="180" align="center" show-overflow-tooltip />
                <el-table-column label="状态" prop="status" width="80" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                            {{ row.status === 1 ? '成功' : '失败' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="异常信息" prop="exceptionInfo" min-width="180" align="center" show-overflow-tooltip />
                <el-table-column label="执行时间" prop="createTime" min-width="160" align="center" show-overflow-tooltip />
            </el-table>

            <el-pagination
                background
                class="flex justify-end items-center mt-8"
                v-model:current-page="queryParams.current"
                v-model:page-size="queryParams.size"
                :page-sizes="[10, 20, 50, 100]"
                :total="total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleQuery"
                @current-change="handleQuery"
            />
        </el-card>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete, Back } from '@element-plus/icons-vue'
import { useRouterStore } from '@/stores/modules/router.js'
import { useRouter } from 'vue-router'
import api from '@/api/index.js'

const routerStore = useRouterStore()
const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
    current: 1,
    size: 10,
    jobName: undefined,
    status: undefined,
})

const handleQuery = async () => {
    loading.value = true
    try {
        const { data } = await api.jobLog.pageJobLog(queryParams)
        tableData.value = data.data.records || []
        total.value = data.data.total || 0
    } catch (error) {
        ElMessage.error('查询失败')
    } finally {
        loading.value = false
    }
}

const resetQuery = () => {
    queryParams.current = 1
    queryParams.jobName = undefined
    queryParams.status = undefined
    handleQuery()
}

const handleClean = async () => {
    try {
        await ElMessageBox.confirm('确定要清空所有日志吗？', '提示', { type: 'warning' })
        await api.jobLog.cleanJobLog()
        ElMessage.success('清空成功')
        handleQuery()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('清空失败')
        }
    }
}

const goBack = () => {
    router.back()
}

handleQuery()
</script>

<style scoped>
.el-pagination {
    margin-top: 20px;
    justify-content: flex-end;
}
</style>
