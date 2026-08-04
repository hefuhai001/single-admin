<template>
    <div class="h-full flex flex-col gap-4">
        <el-card shadow="never" class="shrink-0">
            <el-form :model="queryParams" inline v-if="routerStore.hasPermission('role:query')">
                <el-form-item label="角色ID">
                    <el-input
                        v-model="queryParams.id"
                        placeholder="请输入角色ID"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="角色名称">
                    <el-input
                        v-model="queryParams.roleName"
                        placeholder="请输入角色名称"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="角色编码">
                    <el-input
                        v-model="queryParams.roleCode"
                        placeholder="请输入角色编码"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="角色描述">
                    <el-input
                        v-model="queryParams.description"
                        placeholder="请输入角色描述"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="状态">
                    <el-select
                        v-model="queryParams.status"
                        placeholder="请选择状态"
                        clearable
                        class="min-w-48"
                    >
                        <el-option label="正常" :value="1" />
                        <el-option label="禁用" :value="0" />
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
                type="primary"
                @click="handleAdd"
                v-if="routerStore.hasPermission('role:add')"
            >
                <el-icon>
                    <Plus />
                </el-icon>
                新增
            </el-button>
            <el-button
                v-if="routerStore.hasPermission('role:delete')"
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
                    label="角色名称"
                    prop="roleName"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="角色编码"
                    prop="roleCode"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="角色描述"
                    prop="description"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="状态 0-禁用 1-正常"
                    prop="status"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="创建时间"
                    prop="createdAt"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="更新时间"
                    prop="updatedAt"
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
                            v-if="routerStore.hasPermission('role:update')"
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
                            v-if="routerStore.hasPermission('role:delete')"
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

        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
            <el-form
                ref="formRef"
                :model="formData"
                :rules="formRules"
                label-width="100px"
                style="padding-right: 60px"
            >
                <el-form-item label="角色名称" prop="roleName">
                    <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
                </el-form-item>
                <el-form-item label="角色编码" prop="roleCode">
                    <el-input v-model="formData.roleCode" placeholder="请输入角色编码" />
                </el-form-item>
                <el-form-item label="角色描述" prop="description">
                    <el-input v-model="formData.description" placeholder="请输入角色描述" />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="formData.status">
                        <el-radio :label="1">正常</el-radio>
                        <el-radio :label="0">禁用</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="权限配置">
                    <el-tree
                        ref="permissionTreeRef"
                        :data="permissionTreeData"
                        show-checkbox
                        node-key="id"
                        check-strictly
                        :props="{ label: 'permissionName', children: 'children' }"
                        class="w-full h-80 overflow-y-auto border rounded border-gray-300 px-4 py-2"
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
import { ref, reactive, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
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
const permissionTreeRef = ref(null)
const permissionTreeData = ref([])

const queryParams = reactive({
    current: 1,
    size: 10,
    id: undefined,
    roleName: undefined,
    roleCode: undefined,
    description: undefined,
    status: undefined,
})

const formData = reactive({
    id: undefined,
    roleName: undefined,
    roleCode: undefined,
    description: undefined,
    status: 1,
    permissionIds: [],
})

const formRules = {
    roleName: [
        {
            required: true,
            message: '请输入角色名称',
            trigger: 'blur',
        },
    ],
    roleCode: [
        {
            required: true,
            message: '请输入角色编码',
            trigger: 'blur',
        },
    ],
    description: [
        {
            required: true,
            message: '请输入角色描述',
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

const dialogTitle = computed(() => (formData.id ? '编辑角色表' : '新增角色表'))

const handleQuery = async () => {
    loading.value = true
    try {
        const { data } = await api.role.pageRole(queryParams)
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
    queryParams.roleName = undefined
    queryParams.roleCode = undefined
    queryParams.description = undefined
    queryParams.status = undefined
    handleQuery()
}

const handleSelectionChange = (selection) => {
    selectedIds.value = selection.map((item) => item.id)
}

const handleAdd = async () => {
    resetForm()
    await loadPermissionTree()
    dialogVisible.value = true
}

const handleEdit = async (row) => {
    resetForm()
    try {
        const { data } = await api.role.getRoleById(row.id)
        Object.assign(formData, data.data)

        // 先加载权限树数据
        await loadPermissionTree()

        // 先打开弹窗，确保 el-tree 渲染后再设置选中节点
        // el-dialog 默认懒渲染，未打开前 el-tree 不存在，permissionTreeRef 为 null
        dialogVisible.value = true

        // 设置权限树选中的权限ID（check-strictly 模式下父子不联动，需要回显所有已绑定权限）
        if (data.data.permissions && data.data.permissions.length > 0) {
            const permissionIds = getAllPermissionIds(data.data.permissions)
            nextTick(() => {
                permissionTreeRef.value?.setCheckedKeys(permissionIds)
            })
        }
    } catch (error) {
        ElMessage.error('获取详情失败')
    }
}

// 递归获取所有权限ID（包括父权限和子权限）
const getAllPermissionIds = (permissions) => {
    const ids = []
    const traverse = (nodes) => {
        nodes.forEach((node) => {
            ids.push(node.id)
            if (node.children && node.children.length > 0) {
                traverse(node.children)
            }
        })
    }
    traverse(permissions)
    return ids
}

// 加载权限树数据
const loadPermissionTree = async () => {
    try {
        const { data } = await api.permission.treePermission()
        permissionTreeData.value = data.data || []
    } catch (error) {
        ElMessage.error('加载权限树失败')
    }
}

const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm('确定要删除该记录吗？', '提示', { type: 'warning' })
        await api.role.deleteRole(row.id)
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
        await api.role.batchDeleteRole(selectedIds.value)
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

        // 获取所有选中的权限ID（check-strictly 模式下父子不联动，不会有半选状态）
        const checkedPermissionIds = permissionTreeRef.value?.getCheckedKeys() || []

        submitLoading.value = true

        const submitData = {
            ...formData,
            permissionIds: checkedPermissionIds,
        }

        if (formData.id) {
            await api.role.updateRole(submitData)
            ElMessage.success('更新成功')
        } else {
            await api.role.saveRole(submitData)
            ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        handleQuery()
    } catch (error) {
        // 表单校验失败时 error 为普通对象，不提示"操作失败"；接口异常时显示后端错误信息
        if (error instanceof Error) {
            ElMessage.error(error?.response?.data?.msg || '操作失败')
        }
    } finally {
        submitLoading.value = false
    }
}

const resetForm = () => {
    formData.id = undefined
    formData.roleName = undefined
    formData.roleCode = undefined
    formData.description = undefined
    formData.status = 1
    formData.permissionIds = []
    formRef.value?.resetFields()
    permissionTreeRef.value?.setCheckedKeys([])
}

handleQuery()
</script>

<style scoped></style>
