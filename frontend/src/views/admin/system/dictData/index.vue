<template>
    <div class="h-full flex flex-col gap-4">
        <el-card shadow="never" class="shrink-0">
            <el-form :model="queryParams" inline v-if="routerStore.hasPermission('dictData:query')">
                        <el-form-item label="所属字典类型">
                            <el-input v-model="queryParams.dictType"
                                      placeholder="请输入所属字典类型（关联dict_type）" clearable
                                      @keyup.enter="handleQuery"/>
                        </el-form-item>
                        <el-form-item label="显示标签">
                            <el-input v-model="queryParams.dictLabel"
                                      placeholder="请输入显示标签（如：男、女）" clearable
                                      @keyup.enter="handleQuery"/>
                        </el-form-item>
                        <el-form-item label="存储值">
                            <el-input v-model="queryParams.dictValue"
                                      placeholder="请输入存储值（如：1、0）" clearable
                                      @keyup.enter="handleQuery"/>
                        </el-form-item>
                        <el-form-item label="排序">
                            <el-input v-model="queryParams.dictSort"
                                      placeholder="请输入排序" clearable
                                      @keyup.enter="handleQuery"/>
                        </el-form-item>
                        <el-form-item label="样式属性">
                            <el-input v-model="queryParams.cssClass"
                                      placeholder="请输入样式属性（前端用）" clearable
                                      @keyup.enter="handleQuery"/>
                        </el-form-item>
                        <el-form-item label="表格列表样式">
                            <el-input v-model="queryParams.listClass"
                                      placeholder="请输入表格列表样式" clearable
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
            <el-button type="primary" @click="handleAdd" v-if="routerStore.hasPermission('dictData:add')">
                <el-icon>
                    <Plus/>
                </el-icon>
                新增
            </el-button>
            <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete" v-if="routerStore.hasPermission('dictData:delete')">
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
                        <el-table-column label="所属字典类型（关联dict_type）" prop="dictType"
                                         min-width="120" align="center" show-overflow-tooltip/>
                        <el-table-column label="显示标签（如：男、女）" prop="dictLabel"
                                         min-width="120" align="center" show-overflow-tooltip/>
                        <el-table-column label="存储值（如：1、0）" prop="dictValue"
                                         min-width="120" align="center" show-overflow-tooltip/>
                        <el-table-column label="排序" prop="dictSort"
                                         min-width="120" align="center" show-overflow-tooltip/>
                        <el-table-column label="状态" prop="status" min-width="120" align="center">
                            <template #default="{ row }">
                                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                                    {{ row.status === 1 ? '启用' : '停用' }}
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="是否默认值" prop="isDefault" min-width="120" align="center">
                            <template #default="{ row }">
                                <el-tag :type="row.isDefault === 1 ? 'success' : 'info'">
                                    {{ row.isDefault === 1 ? '是' : '否' }}
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="样式属性（前端用）" prop="cssClass"
                                         min-width="120" align="center" show-overflow-tooltip/>
                        <el-table-column label="表格列表样式" prop="listClass"
                                         min-width="120" align="center" show-overflow-tooltip/>
                <el-table-column label="操作" width="180" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" link @click="handleEdit(row)" v-if="routerStore.hasPermission('dictData:update')">
                            <el-icon>
                                <Edit/>
                            </el-icon>
                            编辑
                        </el-button>
                        <el-button type="danger" link @click="handleDelete(row)" v-if="routerStore.hasPermission('dictData:delete')">
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
                        <el-form-item label="所属字典" prop="dictType">
                                <el-input v-model="formData.dictType"
                                          placeholder="请输入所属字典类型（关联dict_type）"/>
                        </el-form-item>
                        <el-form-item label="显示标签" prop="dictLabel">
                                <el-input v-model="formData.dictLabel"
                                          placeholder="请输入显示标签（如：男、女）"/>
                        </el-form-item>
                        <el-form-item label="存储值" prop="dictValue">
                                <el-input v-model="formData.dictValue"
                                          placeholder="请输入存储值（如：1、0）"/>
                        </el-form-item>
                        <el-form-item label="排序" prop="dictSort">
                                <el-input-number v-model="formData.dictSort"
                                                 placeholder="请输入排序"
                                                 style="width: 100%"/>
                        </el-form-item>
                        <el-form-item label="状态" prop="status">
                                <el-radio-group v-model="formData.status">
                                    <el-radio :value="1">启用</el-radio>
                                    <el-radio :value="0">停用</el-radio>
                                </el-radio-group>
                        </el-form-item>
                        <el-form-item label="是否默认值" prop="isDefault">
                                <el-radio-group v-model="formData.isDefault">
                                    <el-radio :value="1">是</el-radio>
                                    <el-radio :value="0">否</el-radio>
                                </el-radio-group>
                        </el-form-item>
                        <el-form-item label="样式属性" prop="cssClass">
                                <el-input v-model="formData.cssClass"
                                          placeholder="请输入样式属性（前端用）"/>
                        </el-form-item>
                        <el-form-item label="列表样式" prop="listClass">
                                <el-input v-model="formData.listClass"
                                          placeholder="请输入表格列表样式"/>
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
        dictType: undefined,
        dictLabel: undefined,
        dictValue: undefined,
        dictSort: undefined,
        cssClass: undefined,
        listClass: undefined
    })

    const formData = reactive({
        id: undefined, 
        dictType: undefined, 
        dictLabel: undefined, 
        dictValue: undefined, 
        dictSort: undefined, 
        status: undefined, 
        isDefault: undefined, 
        cssClass: undefined, 
        listClass: undefined
    })

    const formRules = {
        dictType: [{
            required: true,
            message: '请输入所属字典类型（关联dict_type）',
            trigger: 'blur'
        }], 
        dictLabel: [{
            required: true,
            message: '请输入显示标签（如：男、女）',
            trigger: 'blur'
        }], 
        dictValue: [{
            required: true,
            message: '请输入存储值（如：1、0）',
            trigger: 'blur'
        }], 
        dictSort: [{
            required: true,
            message: '请输入排序',
            trigger: 'blur'
        }], 
        status: [{
            required: true,
            message: '请选择状态',
            trigger: 'change'
        }], 
        isDefault: [{
            required: true,
            message: '请选择是否默认值',
            trigger: 'change'
        }]
    }

    const dialogTitle = computed(() => formData.id ? '编辑' : '新增')

    const handleQuery = async () => {
        loading.value = true
        try {
            const {data} = await api.dictData.pageDictData(queryParams)
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
        queryParams.dictType = undefined
        queryParams.dictLabel = undefined
        queryParams.dictValue = undefined
        queryParams.dictSort = undefined
        queryParams.cssClass = undefined
        queryParams.listClass = undefined
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
            const {data} = await api.dictData.getDictDataById(row.id)
            Object.assign(formData, data.data)
            dialogVisible.value = true
        } catch (error) {
            ElMessage.error('获取详情失败')
        }
    }

    const handleDelete = async (row) => {
        try {
            await ElMessageBox.confirm('确定要删除该记录吗？', '提示', {type: 'warning'})
            await api.dictData.deleteDictData(row.id)
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
            await api.dictData.batchDeleteDictData(selectedIds.value)
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
                await api.dictData.updateDictData(formData)
                ElMessage.success('更新成功')
            } else {
                await api.dictData.saveDictData(formData)
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
        formData.dictType = undefined
        formData.dictLabel = undefined
        formData.dictValue = undefined
        formData.dictSort = undefined
        formData.status = undefined
        formData.isDefault = undefined
        formData.cssClass = undefined
        formData.listClass = undefined
        formRef.value?.resetFields()
    }

    handleQuery()
</script>

<style scoped>
</style>
