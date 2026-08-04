<template>
    <div class="h-full flex gap-4">
        <el-card shadow="never" class="min-w-60 shrink-0">
            <el-input
                v-model="filterText"
                placeholder="输入关键字过滤"
                prefix-icon="Search"
                clearable
                class="mb-4"
            />
            <el-tree
                ref="treeRef"
                :data="roleTreeData"
                :props="defaultProps"
                :filter-node-method="filterNode"
                node-key="id"
                :expand-on-click-node="false"
                highlight-current
                @node-click="handleNodeClick"
            >
                <template #default="{ node, data }">
                    <span>
                        <el-icon class="mr-2 text-blue-500">
                            <User />
                        </el-icon>
                        <span>{{ node.label }}</span>
                    </span>
                </template>
            </el-tree>
        </el-card>

        <div class="flex-1 flex flex-col gap-4">
            <el-card shadow="never" class="shrink-0">
                <el-form :model="queryParams" inline v-if="routerStore.hasPermission('user:query')">
                    <el-form-item label="用户名">
                        <el-input
                            v-model="queryParams.username"
                            placeholder="请输入用户名"
                            clearable
                            @keyup.enter="handleQuery"
                        />
                    </el-form-item>
                    <el-form-item label="">
                        <el-input
                            v-model="queryParams.institutionId"
                            placeholder="请输入"
                            clearable
                            @keyup.enter="handleQuery"
                        />
                    </el-form-item>
                    <el-form-item label="昵称">
                        <el-input
                            v-model="queryParams.nickname"
                            placeholder="请输入昵称"
                            clearable
                            @keyup.enter="handleQuery"
                        />
                    </el-form-item>
                    <el-form-item label="邮箱">
                        <el-input
                            v-model="queryParams.email"
                            placeholder="请输入邮箱"
                            clearable
                            @keyup.enter="handleQuery"
                        />
                    </el-form-item>
                    <el-form-item label="状态">
                        <el-select
                            class="min-w-48"
                            v-model="queryParams.status"
                            placeholder="请选择状态"
                            clearable
                        >
                            <el-option label="禁用" :value="0" />
                            <el-option label="正常" :value="1" />
                        </el-select>
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
                    v-if="routerStore.hasPermission('user:add')"
                    type="primary"
                    @click="handleAdd"
                >
                    <el-icon>
                        <Plus />
                    </el-icon>
                    新增
                </el-button>
                <el-button
                    v-if="routerStore.hasPermission('user:delete')"
                    type="danger"
                    plain
                    :disabled="selectedIds.length === 0"
                    @click="handleBatchDelete"
                >
                    <el-icon>
                        <Delete />
                    </el-icon>
                    批量删除
                </el-button>
            </el-card>

            <el-card class="flex-1 min-h-0" shadow="never">
                <el-table
                    border
                    v-loading="loading"
                    :data="tableData"
                    @selection-change="handleSelectionChange"
                >
                    <el-table-column type="selection" width="55" align="center" />
                    <el-table-column label="ID" prop="id" width="90" align="center" />
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
                        label="邮箱"
                        prop="email"
                        min-width="150"
                        align="center"
                        show-overflow-tooltip
                    />
                    <el-table-column
                        label="手机号"
                        prop="phone"
                        min-width="120"
                        align="center"
                        show-overflow-tooltip
                    />
                    <el-table-column
                        label="机构"
                        prop="institutionId"
                        min-width="120"
                        align="center"
                        show-overflow-tooltip
                    />
                    <el-table-column label="状态" prop="status" width="80" align="center">
                        <template #default="{ row }">
                            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                                {{ row.status === 1 ? '正常' : '禁用' }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column
                        label="创建时间"
                        prop="createdAt"
                        min-width="160"
                        align="center"
                        show-overflow-tooltip
                    />
                    <el-table-column label="操作" width="180" align="center" fixed="right">
                        <template #default="{ row }">
                            <el-button
                                type="primary"
                                link
                                @click="handleEdit(row)"
                                v-if="routerStore.hasPermission('user:update')"
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
                                v-if="routerStore.hasPermission('user:delete')"
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

        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
            <el-form
                ref="formRef"
                :model="formData"
                :rules="formRules"
                label-width="100px"
                style="padding-right: 60px"
            >
                <el-form-item label="用户名" prop="username">
                    <el-input v-model="formData.username" placeholder="请输入用户名" />
                </el-form-item>
                <el-form-item label="密码" prop="password">
                    <el-input
                        v-model="formData.password"
                        type="password"
                        placeholder="请输入密码"
                        show-password
                    />
                </el-form-item>
                <el-form-item label="昵称" prop="nickname">
                    <el-input v-model="formData.nickname" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                    <el-input v-model="formData.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                    <el-input v-model="formData.phone" placeholder="请输入手机号" />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="formData.status">
                        <el-radio :value="1">正常</el-radio>
                        <el-radio :value="0">禁用</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="角色" prop="roleIds">
                    <el-select
                        v-model="formData.roleIds"
                        placeholder="请选择角色"
                        multiple
                        clearable
                        style="width: 100%"
                    >
                        <el-option
                            v-for="role in roleTreeData"
                            :key="role.id"
                            :label="role.roleName"
                            :value="role.id"
                        />
                    </el-select>
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
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, User } from '@element-plus/icons-vue'
import api from '@/api/index.js'
import { useRouterStore } from '@/stores/modules/router'

const routerStore = useRouterStore()

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const treeRef = ref(null)

const filterText = ref('')
const roleTreeData = ref([])

const defaultProps = {
    children: 'children',
    label: 'roleName',
}

const queryParams = reactive({
    current: 1,
    size: 10,
    institutionId: undefined,
    username: undefined,
    nickname: undefined,
    email: undefined,
    phone: undefined,
    status: undefined,
    roleId: undefined,
})

const formData = reactive({
    id: undefined,
    institutionId: undefined,
    username: undefined,
    password: undefined,
    nickname: undefined,
    email: undefined,
    phone: undefined,
    status: 1,
    roleIds: [],
})

const formRules = {
    institutionId: [
        {
            required: true,
            message: '请输入机构',
            trigger: 'blur',
        },
    ],
    username: [
        {
            required: true,
            message: '请输入用户名',
            trigger: 'blur',
        },
    ],
    password: [
        {
            required: true,
            message: '请输入密码',
            trigger: 'blur',
        },
    ],
    nickname: [
        {
            required: true,
            message: '请输入昵称',
            trigger: 'blur',
        },
    ],
    email: [
        {
            required: true,
            message: '请输入邮箱',
            trigger: 'blur',
        },
        {
            type: 'email',
            message: '请输入正确的邮箱格式',
            trigger: 'blur',
        },
    ],
    status: [
        {
            required: true,
            message: '请选择状态',
            trigger: 'change',
        },
    ],
}

const dialogTitle = computed(() => (formData.id ? '编辑用户' : '新增用户'))

watch(filterText, (val) => {
    treeRef.value?.filter(val)
})

const filterNode = (value, data) => {
    if (!value) return true
    return data.roleName.includes(value)
}

const loadRoleTree = async () => {
    try {
        const { data } = await api.role.list()
        const list = data.data || []
        roleTreeData.value = list.map((item) => ({
            ...item,
            children: [],
        }))
        roleTreeData.value.unshift({ id: 0, roleName: '全部' })
    } catch (error) {
        ElMessage.error('加载角色树失败')
    }
}

const handleNodeClick = (data) => {
    if (data.id === 0) {
        queryParams.roleId = undefined
        handleQuery()
        return
    }
    queryParams.roleId = data.id
    handleQuery()
}

const handleQuery = async () => {
    loading.value = true
    try {
        const { data } = await api.user.pageUser(queryParams)
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
    queryParams.institutionId = undefined
    queryParams.username = undefined
    queryParams.nickname = undefined
    queryParams.email = undefined
    queryParams.phone = undefined
    queryParams.status = undefined
    queryParams.roleId = undefined
    treeRef.value?.setCurrentKey(null)
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
        const { data } = await api.user.getUserById(row.id)
        Object.assign(formData, data.data)
        dialogVisible.value = true
    } catch (error) {
        ElMessage.error('获取详情失败')
    }
}

const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm('确定要删除该记录吗？', '提示', { type: 'warning' })
        await api.user.deleteUser(row.id)
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
        await api.user.batchDeleteUser(selectedIds.value)
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
            await api.user.updateUser(formData)
            ElMessage.success('更新成功')
        } else {
            await api.user.saveUser(formData)
            ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        handleQuery()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('操作失败')
        }
    } finally {
        submitLoading.value = false
    }
}

const resetForm = () => {
    formData.id = undefined
    formData.institutionId = undefined
    formData.username = undefined
    formData.password = undefined
    formData.nickname = undefined
    formData.email = undefined
    formData.phone = undefined
    formData.status = 1
    formData.roleIds = []
    formRef.value?.resetFields()
}

onMounted(() => {
    loadRoleTree()
    handleQuery()
})
</script>

<style scoped>
:deep(.el-tree-node__content) {
    padding: 20px;
}
</style>
