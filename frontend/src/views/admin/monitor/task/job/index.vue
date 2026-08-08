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
                <el-form-item label="任务分组">
                    <el-input
                        v-model="queryParams.jobGroup"
                        placeholder="请输入任务分组"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="状态">
                    <el-select
                        v-model="queryParams.status"
                        placeholder="请选择状态"
                        clearable
                        style="width: 120px"
                    >
                        <el-option label="停用" :value="0" />
                        <el-option label="运行" :value="1" />
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
            <el-button
                type="primary"
                @click="handleAdd"
                v-if="routerStore.hasPermission('job:add')"
            >
                <el-icon><Plus /></el-icon> 新增
            </el-button>
            <el-button
                type="danger"
                :disabled="selectedIds.length === 0"
                @click="handleBatchDelete"
                v-if="routerStore.hasPermission('job:delete')"
            >
                <el-icon><Delete /></el-icon> 批量删除
            </el-button>
            <el-button @click="goToLog" v-if="routerStore.hasPermission('job:query')">
                <el-icon><Document /></el-icon> 日志
            </el-button>
        </el-card>

        <el-card shadow="never" class="flex-1 min-h-0">
            <el-table
                border
                v-loading="loading"
                :data="tableData"
                @selection-change="handleSelectionChange"
            >
                <el-table-column type="selection" width="55" align="center" />
                <el-table-column label="ID" prop="id" width="90" align="center" />
                <el-table-column
                    label="任务名称"
                    prop="jobName"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="任务分组"
                    prop="jobGroup"
                    min-width="100"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="调用目标"
                    prop="invokeTarget"
                    min-width="180"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="Cron"
                    prop="cronExpression"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column label="并发" prop="concurrent" width="80" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.concurrent === 0 ? 'success' : 'info'">
                            {{ row.concurrent === 0 ? '允许' : '禁止' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="状态" prop="status" width="80" align="center">
                    <template #default="{ row }">
                        <el-switch
                            v-model="row.status"
                            :active-value="1"
                            :inactive-value="0"
                            inline-prompt
                            active-text="运行"
                            inactive-text="停用"
                            @change="(val) => handleChangeStatus(row, val)"
                        />
                    </template>
                </el-table-column>
                <el-table-column
                    label="创建时间"
                    prop="createTime"
                    min-width="160"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column label="操作" width="240" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            type="primary"
                            link
                            @click="handleEdit(row)"
                            v-if="routerStore.hasPermission('job:update')"
                        >
                            <el-icon><Edit /></el-icon> 编辑
                        </el-button>
                        <el-button
                            type="success"
                            link
                            @click="handleRun(row)"
                            v-if="routerStore.hasPermission('job:update')"
                        >
                            <el-icon><VideoPlay /></el-icon> 执行
                        </el-button>
                        <el-button
                            type="danger"
                            link
                            @click="handleDelete(row)"
                            v-if="routerStore.hasPermission('job:delete')"
                        >
                            <el-icon><Delete /></el-icon> 删除
                        </el-button>
                    </template>
                </el-table-column>
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

        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
            <el-form
                ref="formRef"
                :model="formData"
                :rules="formRules"
                label-width="100px"
                style="padding-right: 60px"
            >
                <el-form-item label="任务名称" prop="jobName">
                    <el-input v-model="formData.jobName" placeholder="请输入任务名称" />
                </el-form-item>
                <el-form-item label="任务分组" prop="jobGroup">
                    <el-input v-model="formData.jobGroup" placeholder="默认 DEFAULT" />
                </el-form-item>
                <el-form-item label="调用目标" prop="invokeTarget">
                    <el-input
                        v-model="formData.invokeTarget"
                        placeholder="类名#方法名，如 com.xxx.DemoJob#execute"
                    />
                </el-form-item>
                <el-form-item label="Cron" prop="cronExpression">
                    <el-input v-model="formData.cronExpression" placeholder="如 0 0 * * * ?" />
                </el-form-item>
                <el-form-item label="错过策略" prop="misfirePolicy">
                    <el-select
                        v-model="formData.misfirePolicy"
                        placeholder="请选择错过策略"
                        style="width: 100%"
                    >
                        <el-option label="立即执行" :value="1" />
                        <el-option label="执行一次" :value="2" />
                        <el-option label="放弃执行" :value="3" />
                    </el-select>
                </el-form-item>
                <el-form-item label="并发执行" prop="concurrent">
                    <el-radio-group v-model="formData.concurrent">
                        <el-radio :value="0">允许</el-radio>
                        <el-radio :value="1">禁止</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="formData.status">
                        <el-radio :value="1">运行</el-radio>
                        <el-radio :value="0">停用</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-input v-model="formData.remark" type="textarea" placeholder="请输入备注" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitLoading" @click="handleSubmit"
                    >确定</el-button
                >
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Document, VideoPlay } from '@element-plus/icons-vue'
import { useRouterStore } from '@/stores/modules/router'
import { useRouter } from 'vue-router'
import api from '@/api/index.js'

const routerStore = useRouterStore()
const router = useRouter()

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)

const queryParams = reactive({
    current: 1,
    size: 10,
    jobName: undefined,
    jobGroup: undefined,
    status: undefined,
})

const formData = reactive({
    id: undefined,
    jobName: undefined,
    jobGroup: 'DEFAULT',
    invokeTarget: undefined,
    cronExpression: undefined,
    misfirePolicy: 1,
    concurrent: 1,
    status: 1,
    remark: undefined,
})

const formRules = {
    jobName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
    invokeTarget: [{ required: true, message: '请输入调用目标', trigger: 'blur' }],
    cronExpression: [{ required: true, message: '请输入 Cron 表达式', trigger: 'blur' }],
    misfirePolicy: [{ required: true, message: '请选择错过策略', trigger: 'change' }],
    concurrent: [{ required: true, message: '请选择是否并发', trigger: 'change' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const dialogTitle = computed(() => (formData.id ? '编辑定时任务' : '新增定时任务'))

const handleQuery = async () => {
    loading.value = true
    try {
        const { data } = await api.job.pageJob(queryParams)
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
    queryParams.jobGroup = undefined
    queryParams.status = undefined
    handleQuery()
}

const handleSelectionChange = (selection) => {
    selectedIds.value = selection.map((item) => item.id)
}

const handleAdd = () => {
    resetForm()
    dialogVisible.value = true
}

const handleEdit = async (row) => {
    resetForm()
    try {
        const { data } = await api.job.getJobById(row.id)
        Object.assign(formData, data.data)
        dialogVisible.value = true
    } catch (error) {
        ElMessage.error('获取详情失败')
    }
}

const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm('确定要删除该记录吗？', '提示', { type: 'warning' })
        await api.job.deleteJob(row.id)
        ElMessage.success('删除成功')
        handleQuery()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('删除失败')
        }
    }
}

const handleBatchDelete = async () => {
    try {
        await ElMessageBox.confirm('确定要删除选中的记录吗？', '提示', { type: 'warning' })
        await api.job.batchDeleteJob(selectedIds.value)
        ElMessage.success('删除成功')
        handleQuery()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('删除失败')
        }
    }
}

const handleChangeStatus = async (row, val) => {
    try {
        await api.job.changeJobStatus({ id: row.id, status: val })
        ElMessage.success('状态更新成功')
    } catch (error) {
        row.status = val === 1 ? 0 : 1
        ElMessage.error('状态更新失败')
    }
}

const handleRun = async (row) => {
    try {
        await api.job.runJob(row.id)
        ElMessage.success('执行成功')
    } catch (error) {
        ElMessage.error('执行失败')
    }
}

const handleSubmit = async () => {
    try {
        await formRef.value.validate()
        submitLoading.value = true
        if (formData.id) {
            await api.job.updateJob(formData)
            ElMessage.success('更新成功')
        } else {
            await api.job.saveJob(formData)
            ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        handleQuery()
    } catch (error) {
        ElMessage.error('操作失败')
    } finally {
        submitLoading.value = false
    }
}

const resetForm = () => {
    formData.id = undefined
    formData.jobName = undefined
    formData.jobGroup = 'DEFAULT'
    formData.invokeTarget = undefined
    formData.cronExpression = undefined
    formData.misfirePolicy = 1
    formData.concurrent = 1
    formData.status = 1
    formData.remark = undefined
    formRef.value?.resetFields()
}

const goToLog = () => {
    router.push('/admin/task/jobLog')
}

handleQuery()
</script>

<style scoped>
.el-pagination {
    margin-top: 20px;
    justify-content: flex-end;
}
</style>
