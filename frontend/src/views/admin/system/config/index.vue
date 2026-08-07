<template>
    <div class="h-full flex flex-col gap-4">
        <el-card shadow="never" class="shrink-0">
            <el-form :model="queryParams" inline v-if="routerStore.hasPermission('config:query')">
                <el-form-item label="配置键">
                    <el-input
                        v-model="queryParams.configKey"
                        placeholder="请输入配置键（唯一标识，如：register.enable）"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="配置值">
                    <el-input
                        v-model="queryParams.configValue"
                        placeholder="请输入配置值（存储具体配置内容）"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="配置名称">
                    <el-input
                        v-model="queryParams.configName"
                        placeholder="请输入配置名称（如：是否开启注册）"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="配置分组">
                    <el-input
                        v-model="queryParams.configGroup"
                        placeholder="请输入配置分组（system/email/sms/oss等）"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="配置类型">
                    <el-select
                        v-model="queryParams.configType"
                        placeholder="请选择配置类型"
                        clearable
                        @keyup.enter="handleQuery"
                    >
                        <el-option
                            v-for="item in configTypeOptions"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="备注说明">
                    <el-input
                        v-model="queryParams.remark"
                        placeholder="请输入备注说明"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="handleQuery">
                        <el-icon>
                            <Search />
                        </el-icon>
                        搜索
                    </el-button>
                </el-form-item>
            </el-form>
            <el-button @click="handleQuery">
                <el-icon>
                    <Refresh />
                </el-icon>
                刷新
            </el-button>
            <el-button
                type="primary"
                @click="handleAdd"
                v-if="routerStore.hasPermission('config:add')"
            >
                <el-icon>
                    <Plus />
                </el-icon>
                新增
            </el-button>
            <el-button
                type="danger"
                :disabled="selectedIds.length === 0"
                @click="handleBatchDelete"
                v-if="routerStore.hasPermission('config:delete')"
            >
                <el-icon>
                    <Delete />
                </el-icon>
                批量删除
            </el-button>
        </el-card>

        <el-card shadow="never" class="flex-1 min-h-0">
            <el-table
                border
                style="margin-top: 20px"
                v-loading="loading"
                :data="tableData"
                @selection-change="handleSelectionChange"
            >
                <el-table-column type="selection" width="55" align="center" />
                <el-table-column label="序号" type="index" width="60" align="center" />
                <el-table-column
                    label="配置键"
                    prop="configKey"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="配置名称"
                    prop="configName"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="配置值"
                    prop="configValue"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="配置分组"
                    prop="configGroup"
                    min-width="100"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="配置类型"
                    prop="configType"
                    min-width="100"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="是否内置"
                    prop="isSystem"
                    min-width="100"
                    align="center"
                    show-overflow-tooltip
                >
                    <template #default="{ row }">
                        <el-tag :type="row.isSystem ? 'danger' : 'success'">{{
                            row.isSystem ? '是' : '否'
                        }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column
                    label="状态"
                    prop="status"
                    min-width="100"
                    align="center"
                    show-overflow-tooltip
                >
                    <template #default="{ row }">
                        <el-tag :type="row.status ? 'success' : 'danger'">{{
                            row.status ? '启用' : '停用'
                        }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column
                    label="备注说明"
                    prop="remark"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column label="操作" width="180" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            type="primary"
                            link
                            @click="handleEdit(row)"
                            v-if="routerStore.hasPermission('config:update')"
                        >
                            <el-icon>
                                <Edit />
                            </el-icon>
                            编辑
                        </el-button>
                        <el-button
                            type="danger"
                            link
                            @click="handleDelete(row)"
                            v-if="routerStore.hasPermission('config:delete')"
                        >
                            <el-icon>
                                <Delete />
                            </el-icon>
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <el-pagination
                background
                style="margin-top: 20px"
                class="flex justify-end items-center mt-10"
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
                <el-form-item label="配置键" prop="configKey">
                    <el-input
                        v-model="formData.configKey"
                        placeholder="请输入配置键（唯一标识，如：register.enable）"
                    />
                </el-form-item>
                <el-form-item label="配置值" prop="configValue">
                    <el-input
                        v-model="formData.configValue"
                        placeholder="请输入配置值（存储具体配置内容）"
                    />
                </el-form-item>
                <el-form-item label="配置名称" prop="configName">
                    <el-input
                        v-model="formData.configName"
                        placeholder="请输入配置名称（如：是否开启注册）"
                    />
                </el-form-item>
                <el-form-item label="配置分组" prop="configGroup">
                    <el-input
                        v-model="formData.configGroup"
                        placeholder="请输入配置分组（system/email/sms/oss等）"
                    />
                </el-form-item>
                <el-form-item label="配置类型" prop="configType">
                    <el-select v-model="formData.configType" placeholder="请选择配置类型">
                        <el-option
                            v-for="item in configTypeOptions"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="是否内置" prop="isSystem">
                    <el-radio-group v-model="formData.isSystem">
                        <el-radio :label="true">是</el-radio>
                        <el-radio :label="false">否</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="formData.status">
                        <el-radio :label="true">停用</el-radio>
                        <el-radio :label="false">启用</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="备注说明" prop="remark">
                    <el-input v-model="formData.remark" placeholder="请输入备注说明" />
                </el-form-item>
                <el-form-item label="创建时间" prop="createTime">
                    <el-date-picker
                        v-model="formData.createTime"
                        type="datetime"
                        placeholder="请选择创建时间"
                        style="width: 100%"
                    />
                </el-form-item>
                <el-form-item label="更新时间" prop="updateTime">
                    <el-date-picker
                        v-model="formData.updateTime"
                        type="datetime"
                        placeholder="请选择更新时间"
                        style="width: 100%"
                    />
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
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import api from '@/api/index.js'
import { useRouterStore } from '@/stores/modules/router.js'

const routerStore = useRouterStore()
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
    id: undefined,
    configKey: undefined,
    configValue: undefined,
    configName: undefined,
    configGroup: undefined,
    configType: undefined,
    remark: undefined,
})

const formData = reactive({
    id: undefined,
    configKey: undefined,
    configValue: undefined,
    configName: undefined,
    configGroup: undefined,
    configType: undefined,
    isSystem: undefined,
    status: undefined,
    remark: undefined,
    createTime: undefined,
    updateTime: undefined,
})

const configTypeOptions = [
    { label: '字符串', value: 'string' },
    { label: '整数', value: 'integer' },
    { label: '布尔', value: 'boolean' },
    { label: '数组', value: 'array' },
    { label: 'JSON', value: 'json' },
]

const formRules = {
    configKey: [
        {
            required: true,
            message: '请输入配置键（唯一标识，如：register.enable）',
            trigger: 'blur',
        },
    ],
    configValue: [
        {
            required: true,
            message: '请输入配置值（存储具体配置内容）',
            trigger: 'blur',
        },
    ],
    configName: [
        {
            required: true,
            message: '请输入配置名称（如：是否开启注册）',
            trigger: 'blur',
        },
    ],
    configGroup: [
        {
            required: true,
            message: '请输入配置分组（system/email/sms/oss等）',
            trigger: 'blur',
        },
    ],
    configType: [
        {
            required: true,
            message: '请输入配置类型（string/integer/boolean/array/json）',
            trigger: 'blur',
        },
    ],
    isSystem: [
        {
            required: true,
            message: '请输入是否系统内置（1-是，不可删除；0-否）',
            trigger: 'blur',
        },
    ],
    status: [
        {
            required: true,
            message: '请输入状态（0停用 1启用）',
            trigger: 'blur',
        },
    ],
    remark: [
        {
            required: true,
            message: '请输入备注说明',
            trigger: 'blur',
        },
    ],
    createTime: [
        {
            required: true,
            message: '请输入创建时间',
            trigger: 'blur',
        },
    ],
    updateTime: [
        {
            required: true,
            message: '请输入更新时间',
            trigger: 'blur',
        },
    ],
}

const dialogTitle = computed(() => (formData.id ? '编辑系统配置表' : '新增系统配置表'))

const handleQuery = async () => {
    loading.value = true
    try {
        const { data } = await api.config.pageConfig(queryParams)
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
    queryParams.id = undefined
    queryParams.configKey = undefined
    queryParams.configValue = undefined
    queryParams.configName = undefined
    queryParams.configGroup = undefined
    queryParams.configType = undefined
    queryParams.remark = undefined
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
        const { data } = await api.config.getConfigById(row.id)
        Object.assign(formData, data.data)
        dialogVisible.value = true
    } catch (error) {
        ElMessage.error('获取详情失败')
    }
}

const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm('确定要删除该记录吗？', '提示', { type: 'warning' })
        await api.config.deleteConfig(row.id)
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
        await api.config.batchDeleteConfig(selectedIds.value)
        ElMessage.success('删除成功')
        handleQuery()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('删除失败')
        }
    }
}

const handleSubmit = async () => {
    try {
        await formRef.value.validate()
        submitLoading.value = true
        if (formData.id) {
            await api.config.updateConfig(formData)
            ElMessage.success('更新成功')
        } else {
            await api.config.saveConfig(formData)
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
    formData.configKey = undefined
    formData.configValue = undefined
    formData.configName = undefined
    formData.configGroup = undefined
    formData.configType = undefined
    formData.isSystem = undefined
    formData.status = undefined
    formData.remark = undefined
    formData.createTime = undefined
    formData.updateTime = undefined
    formRef.value?.resetFields()
}

handleQuery()
</script>

<style scoped></style>
