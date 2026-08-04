<template>
    <div class="h-full flex flex-col gap-4">
        <el-card shadow="never" class="shrink-0">
            <el-form
                :model="queryParams"
                inline
                v-if="routerStore.hasPermission('permission:query')"
            >
                <el-form-item label="权限名称">
                    <el-input
                        v-model="queryParams.permissionName"
                        placeholder="请输入权限名称"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="权限类型">
                    <el-select
                        v-model="queryParams.permissionType"
                        placeholder="请选择权限类型"
                        clearable
                        class="min-w-48"
                    >
                        <el-option label="接口权限" :value="1" />
                        <el-option label="路由权限" :value="2" />
                    </el-select>
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
                v-if="routerStore.hasPermission('permission:add')"
            >
                <el-icon>
                    <Plus />
                </el-icon>
                新增
            </el-button>
            <el-button @click="toggleExpandAll">
                <el-icon>
                    <Expand />
                </el-icon>
                {{ isExpandedAll ? '收起全部' : '展开全部' }}
            </el-button>
        </el-card>

        <el-card shadow="never" class="flex-1 min-h-0">
            <el-table
                ref="tableRef"
                border
                v-loading="loading"
                :data="tableData"
                row-key="id"
                :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
            >
                <el-table-column label="权限名称" prop="permissionName" min-width="180" />
                <el-table-column
                    label="权限编码"
                    prop="permissionCode"
                    min-width="150"
                    align="center"
                />
                <el-table-column
                    label="权限类型"
                    prop="permissionType"
                    min-width="100"
                    align="center"
                >
                    <template #default="{ row }">
                        <el-tag v-if="row.permissionType === 1" type="warning">接口权限</el-tag>
                        <el-tag v-else-if="row.permissionType === 2" type="success"
                            >路由权限</el-tag
                        >
                    </template>
                </el-table-column>
                <el-table-column
                    label="路由路径"
                    prop="routePath"
                    min-width="150"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="组件路径"
                    prop="componentPath"
                    min-width="150"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="重定向"
                    prop="redirect"
                    min-width="100"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column label="图标" prop="icon" min-width="60" align="center">
                    <template #default="{ row }">
                        <div v-if="row.icon" class="flex justify-center items-center">
                            <component :is="row.icon" class="text-lg text-gray-600" />
                        </div>
                        <span v-else class="text-gray-400">-</span>
                    </template>
                </el-table-column>
                <el-table-column label="排序" prop="sort" min-width="60" align="center" />
                <el-table-column label="状态" prop="status" min-width="80" align="center">
                    <template #default="{ row }">
                        <el-tag v-if="row.status === 1" type="success">正常</el-tag>
                        <el-tag v-else type="danger">禁用</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="创建时间" prop="createdAt" min-width="200" align="center" />
                <el-table-column label="操作" width="180" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            type="primary"
                            link
                            @click="handleEdit(row)"
                            v-if="routerStore.hasPermission('permission:update')"
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
                            v-if="routerStore.hasPermission('permission:delete')"
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
            <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
                <el-form-item label="权限名称" prop="permissionName">
                    <el-input v-model="formData.permissionName" placeholder="请输入权限名称" />
                </el-form-item>
                <el-form-item label="权限编码" prop="permissionCode">
                    <el-input v-model="formData.permissionCode" placeholder="请输入权限编码" />
                </el-form-item>
                <el-form-item label="权限类型" prop="permissionType">
                    <el-radio-group v-model="formData.permissionType">
                        <el-radio :label="1">接口权限</el-radio>
                        <el-radio :label="2">路由权限</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="父权限" prop="parentId">
                    <el-tree-select
                        v-model="formData.parentId"
                        :data="permissionTreeData"
                        :props="{ label: 'permissionName', value: 'id', children: 'children' }"
                        check-strictly
                        clearable
                        placeholder="请选择父权限"
                        style="width: 100%"
                    />
                </el-form-item>
                <el-form-item
                    label="路由路径"
                    prop="routePath"
                    v-if="formData.permissionType === 2"
                >
                    <el-input v-model="formData.routePath" placeholder="请输入路由路径" />
                </el-form-item>
                <el-form-item
                    label="组件路径"
                    prop="componentPath"
                    v-if="formData.permissionType === 2"
                >
                    <el-input v-model="formData.componentPath" placeholder="请输入组件路径" />
                </el-form-item>
                <el-form-item label="重定向" prop="redirect" v-if="formData.permissionType === 2">
                    <el-input v-model="formData.redirect" placeholder="请输入重定向地址" />
                </el-form-item>
                <el-form-item label="菜单图标" prop="icon" v-if="formData.permissionType === 2">
                    <el-select
                        v-model="formData.icon"
                        placeholder="请选择菜单图标"
                        clearable
                        filterable
                    >
                        <el-option
                            v-for="icon in iconOptions"
                            :key="icon.value"
                            :label="icon.label"
                            :value="icon.value"
                        >
                            <div class="flex items-center gap-2">
                                <component :is="icon.value" class="text-base text-gray-600" />
                                <span>{{ icon.label }}</span>
                            </div>
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="排序序号" prop="sort">
                    <el-input-number
                        v-model="formData.sort"
                        placeholder="请输入排序序号"
                        style="width: 100%"
                    />
                </el-form-item>
                <el-form-item label="是否显示" prop="visible">
                    <el-radio-group v-model="formData.visible">
                        <el-radio :label="1">显示</el-radio>
                        <el-radio :label="0">隐藏</el-radio>
                    </el-radio-group>
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
                    >确定</el-button
                >
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
// 动态导入 Element Plus 图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { Delete, Edit, Plus, Refresh, Search, Expand } from '@element-plus/icons-vue'
import api from '@/api/index.js'
import { useRouterStore } from '@/stores/modules/router'

const routerStore = useRouterStore()

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const permissionTreeData = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const tableRef = ref(null)
const isExpandedAll = ref(false)

const queryParams = reactive({
    permissionName: undefined,
    permissionType: undefined,
    status: undefined,
})

const formData = reactive({
    id: undefined,
    permissionCode: undefined,
    permissionName: undefined,
    permissionType: 2,
    parentId: 0,
    routePath: undefined,
    componentPath: undefined,
    redirect: undefined,
    icon: undefined,
    sort: 0,
    visible: 1,
    status: 1,
})

// 图标选项列表 - 动态生成
const iconOptions = Object.keys(ElementPlusIconsVue)
    .map((iconName) => ({
        value: iconName,
        label: iconName,
    }))
    .sort((a, b) => a.label.localeCompare(b.label)) // 按图标名称排序

const formRules = {
    permissionName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
    permissionCode: [{ required: true, message: '请输入权限编码', trigger: 'blur' }],
    permissionType: [{ required: true, message: '请选择权限类型', trigger: 'change' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const dialogTitle = computed(() => (formData.id ? '编辑权限' : '新增权限'))

const handleQuery = async () => {
    loading.value = true
    try {
        const { data } = await api.permission.treePermission()
        let permissions = data.data || []

        // 根据查询条件过滤
        if (
            queryParams.permissionName ||
            queryParams.permissionType ||
            queryParams.status !== undefined
        ) {
            permissions = filterTreeData(permissions, queryParams)
        }

        tableData.value = permissions
    } catch (error) {
        ElMessage.error('查询失败')
    } finally {
        loading.value = false
    }
}

// 递归过滤树形数据
const filterTreeData = (data, params) => {
    return data.filter((item) => {
        let match = true

        if (params.permissionName && !item.permissionName.includes(params.permissionName)) {
            match = false
        }
        if (params.permissionType !== undefined && item.permissionType !== params.permissionType) {
            match = false
        }
        if (params.status !== undefined && item.status !== params.status) {
            match = false
        }

        if (item.children && item.children.length > 0) {
            const filteredChildren = filterTreeData(item.children, params)
            if (filteredChildren.length > 0) {
                item.children = filteredChildren
                return true
            }
        }

        return match
    })
}

const resetQuery = () => {
    queryParams.permissionName = undefined
    queryParams.permissionType = undefined
    queryParams.status = undefined
    handleQuery()
}

const handleAdd = async () => {
    resetForm()
    await loadPermissionTree()
    dialogVisible.value = true
}

const handleEdit = async (row) => {
    resetForm()
    await loadPermissionTree()
    try {
        const { data } = await api.permission.getPermissionById(row.id)
        Object.assign(formData, data.data)
        dialogVisible.value = true
    } catch (error) {
        ElMessage.error('获取详情失败')
    }
}

const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm('确定要删除该记录吗？', '提示', { type: 'warning' })
        await api.permission.deletePermission(row.id)
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
            await api.permission.updatePermission(formData)
            ElMessage.success('更新成功')
        } else {
            await api.permission.savePermission(formData)
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
    formData.permissionCode = undefined
    formData.permissionName = undefined
    formData.permissionType = 2
    formData.parentId = 0
    formData.routePath = undefined
    formData.componentPath = undefined
    formData.redirect = undefined
    formData.icon = undefined
    formData.sort = 0
    formData.visible = 1
    formData.status = 1
    formRef.value?.resetFields()
}

const loadPermissionTree = async () => {
    try {
        const { data } = await api.permission.treePermission()
        permissionTreeData.value = data.data || []
        permissionTreeData.value.unshift({ id: 0, permissionName: '根节点' })
    } catch (error) {
        ElMessage.error('加载权限树失败')
    }
}

const toggleExpandAll = () => {
    if (isExpandedAll.value) {
        tableData.value.forEach((row) => {
            tableRef.value?.toggleRowExpansion(row, false)
            if (row.children && row.children.length > 0) {
                row.children.forEach((child) => {
                    tableRef.value?.toggleRowExpansion(child, false)
                })
            }
        })
        isExpandedAll.value = false
    } else {
        const expandAllRows = (rows) => {
            rows.forEach((row) => {
                tableRef.value?.toggleRowExpansion(row, true)
                if (row.children && row.children.length > 0) {
                    expandAllRows(row.children)
                }
            })
        }
        expandAllRows(tableData.value)
        isExpandedAll.value = true
    }
}

handleQuery()
</script>

<style scoped>
svg {
    width: 1em;
    height: 1em;
}
</style>
