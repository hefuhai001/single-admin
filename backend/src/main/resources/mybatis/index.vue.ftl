<template>
    <div class="h-full flex flex-col gap-4">
        <el-card shadow="never" class="shrink-0">
            <el-form :model="queryParams" inline v-if="routerStore.hasPermission('${table.entityPath?replace('Entity','')}:query')">
                <#list table.fields as field>
                    <#if field.propertyType == "String">
                        <el-form-item label="${field.comment!field.propertyName}">
                            <el-input v-model="queryParams.${field.propertyName}"
                                      placeholder="请输入${field.comment!field.propertyName}" clearable
                                      @keyup.enter="handleQuery"/>
                        </el-form-item>
                    <#elseif field.propertyType == "Long" || field.propertyType == "Integer">
                        <el-form-item label="${field.comment!field.propertyName}">
                            <el-input v-model="queryParams.${field.propertyName}"
                                      placeholder="请输入${field.comment!field.propertyName}" clearable
                                      @keyup.enter="handleQuery"/>
                        </el-form-item>
                    </#if>
                </#list>
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
            <el-button type="primary" @click="handleAdd" v-if="routerStore.hasPermission('${table.entityPath?replace('Entity','')}:add')">
                <el-icon>
                    <Plus/>
                </el-icon>
                新增
            </el-button>
            <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete" v-if="routerStore.hasPermission('${table.entityPath?replace('Entity','')}:delete')">
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
                <#list table.fields as field>
                    <#if !field.keyFlag>
                        <el-table-column label="${field.comment!field.propertyName}" prop="${field.propertyName}"
                                         min-width="120" align="center" show-overflow-tooltip/>
                    </#if>
                </#list>
                <el-table-column label="操作" width="180" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" link @click="handleEdit(row)" v-if="routerStore.hasPermission('${table.entityPath?replace('Entity','')}:update')">
                            <el-icon>
                                <Edit/>
                            </el-icon>
                            编辑
                        </el-button>
                        <el-button type="danger" link @click="handleDelete(row)" v-if="routerStore.hasPermission('${table.entityPath?replace('Entity','')}:delete')">
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
                <#list table.fields as field>
                    <#if !field.keyFlag>
                        <el-form-item label="${field.comment!field.propertyName}" prop="${field.propertyName}">
                            <#if field.propertyType == "String">
                                <el-input v-model="formData.${field.propertyName}"
                                          placeholder="请输入${field.comment!field.propertyName}"/>
                            <#elseif field.propertyType == "Long" || field.propertyType == "Integer">
                                <el-input-number v-model="formData.${field.propertyName}"
                                                 placeholder="请输入${field.comment!field.propertyName}"
                                                 style="width: 100%"/>
                            <#elseif field.propertyType == "Date">
                                <el-date-picker v-model="formData.${field.propertyName}" type="datetime"
                                                placeholder="请选择${field.comment!field.propertyName}"
                                                style="width: 100%"/>
                            <#else>
                                <el-input v-model="formData.${field.propertyName}"
                                          placeholder="请输入${field.comment!field.propertyName}"/>
                            </#if>
                        </el-form-item>
                    </#if>
                </#list>
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
        size: 10<#list table.fields as field><#if field.propertyType == "String" || field.propertyType == "Long" || field.propertyType == "Integer">,
        ${field.propertyName}: undefined</#if></#list>
    })

    const formData = reactive({
        <#list table.fields as field>
        ${field.propertyName}: undefined<#if field?has_next>, </#if>
        </#list>
    })

    const formRules = {
        <#list table.fields as field>
        <#if !field.keyFlag>
        ${field.propertyName}: [{
            required: true,
            message: '请输入${field.comment!field.propertyName}',
            trigger: 'blur'
        }]<#if field?has_next>, </#if>
        </#if>
        </#list>
    }

    const dialogTitle = computed(() => formData.id ? '编辑${table.comment!}' : '新增${table.comment!}')

    const handleQuery = async () => {
        loading.value = true
        try {
            const {data} = await api.${table.entityPath?replace('Entity','')}.page${table.entityPath?cap_first?replace('Entity','')}(queryParams)
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
        <#list table.fields as field>
        <#if field.propertyType == "String" || field.propertyType == "Long" || field.propertyType == "Integer">
        queryParams.${field.propertyName} = undefined
        </#if>
        </#list>
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
            const {data} = await api.${table.entityPath?replace('Entity','')}.get${table.entityPath?cap_first?replace('Entity','')}ById(row.id)
            Object.assign(formData, data.data)
            dialogVisible.value = true
        } catch (error) {
            ElMessage.error('获取详情失败')
        }
    }

    const handleDelete = async (row) => {
        try {
            await ElMessageBox.confirm('确定要删除该记录吗？', '提示', {type: 'warning'})
            await api.${table.entityPath?replace('Entity','')}.delete${table.entityPath?cap_first?replace('Entity','')}(row.id)
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
            await api.${table.entityPath?replace('Entity','')}.batchDelete${table.entityPath?cap_first?replace('Entity','')}(selectedIds.value)
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
                await api.${table.entityPath?replace('Entity','')}.update${table.entityPath?cap_first?replace('Entity','')}(formData)
                ElMessage.success('更新成功')
            } else {
                await api.${table.entityPath?replace('Entity','')}.save${table.entityPath?cap_first?replace('Entity','')}(formData)
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
        <#list table.fields as field>
        formData.${field.propertyName} = undefined
        </#list>
        formRef.value?.resetFields()
    }

    handleQuery()
</script>

<style scoped>
</style>
