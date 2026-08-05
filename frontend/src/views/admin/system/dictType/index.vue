<template>
    <div class="h-full flex flex-col gap-4">
        <el-card shadow="never" class="shrink-0">
            <el-form :model="queryParams" inline v-if="routerStore.hasPermission('dictType:query')">
                        <el-form-item label="">
                            <el-input v-model="queryParams.id"
                                      placeholder="请输入" clearable
                                      @keyup.enter="handleQuery"/>
                        </el-form-item>
                        <el-form-item label="字典名称（如：性别）">
                            <el-input v-model="queryParams.dictName"
                                      placeholder="请输入字典名称（如：性别）" clearable
                                      @keyup.enter="handleQuery"/>
                        </el-form-item>
                        <el-form-item label="字典类型（唯一标识，如：sex）">
                            <el-input v-model="queryParams.dictType"
                                      placeholder="请输入字典类型（唯一标识，如：sex）" clearable
                                      @keyup.enter="handleQuery"/>
                        </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="handleQuery">
                        <el-icon>
                            <Search/>
                        </el-icon>
                        搜索
                    </el-button>
                </el-form-item>
            </el-form>
            <el-button @click="handleQuery">
                <el-icon>
                    <Refresh/>
                </el-icon>
                刷新
            </el-button>
            <el-button type="primary" @click="handleAdd" v-if="routerStore.hasPermission('dictType:add')">
                <el-icon>
                    <Plus/>
                </el-icon>
                新增
            </el-button>
            <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete" v-if="routerStore.hasPermission('dictType:delete')">
                <el-icon>
                    <Delete/>
                </el-icon>
                批量删除
            </el-button>
        </el-card>

        <el-card shadow="never" class="flex-1 min-h-0">
            <el-table border style="margin-top: 20px" v-loading="loading" :data="tableData" @selection-change="handleSelectionChange">
                <el-table-column type="selection" width="55" align="center"/>
                <el-table-column label="序号" type="index" width="60" align="center"/>
                        <el-table-column label="字典名称（如：性别）" prop="dictName"
                                         min-width="120" align="center" show-overflow-tooltip/>
                        <el-table-column label="字典类型（唯一标识，如：sex）" prop="dictType"
                                         min-width="120" align="center" show-overflow-tooltip/>
                        <el-table-column label="状态（0停用 1启用）" prop="status"
                                         min-width="120" align="center" show-overflow-tooltip/>
                <el-table-column label="操作" width="180" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" link @click="handleEdit(row)" v-if="routerStore.hasPermission('dictType:update')">
                            <el-icon>
                                <Edit/>
                            </el-icon>
                            编辑
                        </el-button>
                        <el-button type="danger" link @click="handleDelete(row)" v-if="routerStore.hasPermission('dictType:delete')">
                            <el-icon>
                                <Delete/>
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
            <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" style="padding-right: 60px">
                        <el-form-item label="字典名称（如：性别）" prop="dictName">
                                <el-input v-model="formData.dictName"
                                          placeholder="请输入字典名称（如：性别）"/>
                        </el-form-item>
                        <el-form-item label="字典类型（唯一标识，如：sex）" prop="dictType">
                                <el-input v-model="formData.dictType"
                                          placeholder="请输入字典类型（唯一标识，如：sex）"/>
                        </el-form-item>
                        <el-form-item label="状态（0停用 1启用）" prop="status">
                                <el-input v-model="formData.status"
                                          placeholder="请输入状态（0停用 1启用）"/>
                        </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
    import {ref, reactive, computed} from 'vue'
    import {ElMessage, ElMessageBox} from 'element-plus'
    import {Search, Refresh, Plus, Edit, Delete} from '@element-plus/icons-vue'
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
        dictName: undefined,
        dictType: undefined
    })

    const formData = reactive({
        id: undefined, 
        dictName: undefined, 
        dictType: undefined, 
        status: undefined
    })

    const formRules = {
        dictName: [{
            required: true,
            message: '请输入字典名称（如：性别）',
            trigger: 'blur'
        }], 
        dictType: [{
            required: true,
            message: '请输入字典类型（唯一标识，如：sex）',
            trigger: 'blur'
        }], 
        status: [{
            required: true,
            message: '请输入状态（0停用 1启用）',
            trigger: 'blur'
        }]
    }

    const dialogTitle = computed(() => formData.id ? '编辑' : '新增')

    const handleQuery = async () => {
        loading.value = true
        try {
            const {data} = await api.dictType.pageDictType(queryParams)
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
        queryParams.dictName = undefined
        queryParams.dictType = undefined
        handleQuery()
    }

    const handleSelectionChange = (selection) => {
        selectedIds.value = selection.map(item => item.id)
    }

    const handleAdd = () => {
        resetForm()
        dialogVisible.value = true
    }

    const handleEdit = async (row) => {
        resetForm()
        try {
            const {data} = await api.dictType.getDictTypeById(row.id)
            Object.assign(formData, data.data)
            dialogVisible.value = true
        } catch (error) {
            ElMessage.error('获取详情失败')
        }
    }

    const handleDelete = async (row) => {
        try {
            await ElMessageBox.confirm('确定要删除该记录吗？', '提示', {type: 'warning'})
            await api.dictType.deleteDictType(row.id)
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
            await ElMessageBox.confirm('确定要删除选中的记录吗？', '提示', {type: 'warning'})
            await api.dictType.batchDeleteDictType(selectedIds.value)
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
                await api.dictType.updateDictType(formData)
                ElMessage.success('更新成功')
            } else {
                await api.dictType.saveDictType(formData)
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
        formData.dictName = undefined
        formData.dictType = undefined
        formData.status = undefined
        formRef.value?.resetFields()
    }

    handleQuery()
</script>

<style scoped>
</style>
