<template>
    <div class="h-full flex flex-col gap-4">
        <el-card shadow="never" class="shrink-0">
            <el-form
                :model="queryParams"
                inline
                v-if="routerStore.hasPermission('institutions:query')"
            >
                <el-form-item label="机构ID">
                    <el-input
                        v-model="queryParams.id"
                        placeholder="请输入机构ID"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="上级机构">
                    <el-input
                        v-model="queryParams.parentId"
                        placeholder="请输入上级机构"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="机构编码">
                    <el-input
                        v-model="queryParams.institutionCode"
                        placeholder="请输入机构编码"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="机构名称">
                    <el-input
                        v-model="queryParams.institutionName"
                        placeholder="请输入机构名称"
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
            <el-button @click="resetQuery">
                <el-icon>
                    <Refresh />
                </el-icon>
                刷新
            </el-button>
            <el-button
                type="primary"
                @click="handleAdd"
                v-if="routerStore.hasPermission('institutions:add')"
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
                v-if="routerStore.hasPermission('institutions:delete')"
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
                row-key="id"
                :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
                default-expand-all
                @selection-change="handleSelectionChange"
            >
                <el-table-column type="selection" width="55" align="center" />
                <el-table-column
                    label="机构ID"
                    prop="id"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="机构编码"
                    prop="institutionCode"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="机构名称"
                    prop="institutionName"
                    min-width="200"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="上级机构ID"
                    prop="parentId"
                    min-width="100"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="状态"
                    prop="status"
                    min-width="100"
                    align="center"
                    show-overflow-tooltip
                >
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                            {{ row.status === 1 ? '启用' : '禁用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="180" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            type="primary"
                            link
                            @click="handleEdit(row)"
                            v-if="routerStore.hasPermission('institutions:update')"
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
                            v-if="routerStore.hasPermission('institutions:delete')"
                        >
                            <el-icon>
                                <Delete />
                            </el-icon>
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
            <el-form
                ref="formRef"
                :model="formData"
                :rules="formRules"
                label-width="100px"
                style="padding-right: 60px"
            >
                <el-form-item label="上级机构" prop="parentId">
                    <el-input-number
                        v-model="formData.parentId"
                        placeholder="请输入上级机构"
                        style="width: 100%"
                    />
                </el-form-item>
                <el-form-item label="机构编码" prop="institutionCode">
                    <el-input v-model="formData.institutionCode" placeholder="请输入机构编码" />
                </el-form-item>
                <el-form-item label="机构名称" prop="institutionName">
                    <el-input v-model="formData.institutionName" placeholder="请输入机构名称" />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="formData.status">
                        <el-radio :label="1">正常</el-radio>
                        <el-radio :label="0">禁用</el-radio>
                    </el-radio-group>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitLoading" @click="handleSubmit"
                    >确定
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { useRouterStore } from '@/stores/modules/router'
import api from '@/api/index.js'

const routerStore = useRouterStore()

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const selectedIds = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)

const queryParams = reactive({
    id: undefined,
    parentId: undefined,
    institutionCode: undefined,
    institutionName: undefined,
})

const formData = reactive({
    id: undefined,
    parentId: undefined,
    institutionCode: undefined,
    institutionName: undefined,
    status: undefined,
})

const formRules = {
    parentId: [
        {
            required: false,
            message: '请输入上级机构',
            trigger: 'blur',
        },
    ],
    institutionCode: [
        {
            required: true,
            message: '请输入机构编码',
            trigger: 'blur',
        },
    ],
    institutionName: [
        {
            required: true,
            message: '请输入机构名称',
            trigger: 'blur',
        },
    ],
    status: [
        {
            required: true,
            message: '请输入状态: 0-禁用,1-启用',
            trigger: 'blur',
        },
    ],
}

const dialogTitle = computed(() =>
    formData.id ? '编辑机构表(支持父子层级)' : '新增机构表(支持父子层级)',
)

const handleQuery = async () => {
    loading.value = true
    try {
        const { data } = await api.institutions.treeInstitutions()
        const allData = data.data || []

        // 如果有搜索条件，过滤数据
        if (
            queryParams.id ||
            queryParams.parentId ||
            queryParams.institutionCode ||
            queryParams.institutionName
        ) {
            tableData.value = filterTreeData(allData, queryParams)
        } else {
            tableData.value = allData
        }
    } catch (error) {
        ElMessage.error('查询失败')
    } finally {
        loading.value = false
    }
}

// 递归过滤树形数据
const filterTreeData = (data, filters) => {
    return data.filter((item) => {
        // 检查当前节点是否匹配过滤条件
        const matches =
            (!filters.id || item.id?.toString().includes(filters.id.toString())) &&
            (!filters.parentId ||
                item.parentId?.toString().includes(filters.parentId.toString())) &&
            (!filters.institutionCode || item.institutionCode?.includes(filters.institutionCode)) &&
            (!filters.institutionName || item.institutionName?.includes(filters.institutionName))

        // 如果当前节点有子节点，递归过滤子节点
        if (item.children && item.children.length > 0) {
            const filteredChildren = filterTreeData(item.children, filters)
            if (filteredChildren.length > 0) {
                item.children = filteredChildren
                return true
            }
        }

        return matches
    })
}

const resetQuery = () => {
    queryParams.id = undefined
    queryParams.parentId = undefined
    queryParams.institutionCode = undefined
    queryParams.institutionName = undefined
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
        const { data } = await api.institutions.getInstitutionsById(row.id)
        Object.assign(formData, data.data)
        dialogVisible.value = true
    } catch (error) {
        ElMessage.error('获取详情失败')
    }
}

const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm('确定要删除该记录吗？', '提示', { type: 'warning' })
        await api.institutions.deleteInstitutions(row.id)
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
        await api.institutions.batchDeleteInstitutions(selectedIds.value)
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
            await api.institutions.updateInstitutions(formData)
            ElMessage.success('更新成功')
        } else {
            await api.institutions.saveInstitutions(formData)
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
    formData.parentId = undefined
    formData.institutionCode = undefined
    formData.institutionName = undefined
    formData.status = undefined
    formRef.value?.resetFields()
}

handleQuery()
</script>

<style scoped>
.search-card {
    margin-bottom: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.header-buttons {
    display: flex;
    gap: 10px;
}

.el-pagination {
    margin-top: 20px;
    justify-content: flex-end;
}
</style>
