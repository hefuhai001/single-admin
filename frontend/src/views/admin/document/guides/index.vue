<template>
    <div class="h-full flex flex-col gap-4">
        <el-card shadow="never" class="shrink-0">
            <el-form :model="queryParams" inline v-if="routerStore.hasPermission('guides:query')">
                <el-form-item label="指南ID">
                    <el-input
                        v-model="queryParams.id"
                        placeholder="请输入指南ID"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="所属机构">
                    <el-select
                        class="min-w-48"
                        v-model="queryParams.institutionId"
                        placeholder="请选择所属机构"
                        clearable
                        filterable
                        @keyup.enter="handleQuery"
                    >
                        <el-option
                            v-for="inst in institutionList"
                            :key="inst.id"
                            :label="inst.institutionName"
                            :value="inst.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="指南标题">
                    <el-input
                        v-model="queryParams.guideTitle"
                        placeholder="请输入指南标题"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="指南类型">
                    <el-select
                        class="min-w-48"
                        v-model="queryParams.guideType"
                        placeholder="请选择指南类型"
                        clearable
                        @keyup.enter="handleQuery"
                    >
                        <el-option label="通用指南" value="general" />
                        <el-option label="操作指南" value="operation" />
                        <el-option label="质控指南" value="quality" />
                        <el-option label="其他" value="other" />
                    </el-select>
                </el-form-item>
                <el-form-item label="状态">
                    <el-select
                        class="min-w-48"
                        v-model="queryParams.status"
                        placeholder="请选择状态"
                        clearable
                        @keyup.enter="handleQuery"
                    >
                        <el-option label="有效" value="active" />
                        <el-option label="已过期" value="expired" />
                        <el-option label="已归档" value="archived" />
                    </el-select>
                </el-form-item>
                <el-form-item label="发布人ID">
                    <el-input
                        v-model="queryParams.createdBy"
                        placeholder="请输入发布人ID"
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
                v-if="routerStore.hasPermission('guides:add')"
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
                v-if="routerStore.hasPermission('guides:delete')"
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
                <el-table-column label="ID" prop="id" width="90" align="center" />
                <el-table-column
                    label="所属机构"
                    prop="institutionId"
                    min-width="140"
                    align="center"
                    show-overflow-tooltip
                >
                    <template #default="{ row }">
                        {{ getInstitutionName(row.institutionId) }}
                    </template>
                </el-table-column>
                <el-table-column
                    label="指南标题"
                    prop="guideTitle"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="指南内容"
                    prop="guideContent"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="指南附件"
                    prop="guideFileUrl"
                    min-width="140"
                    align="center"
                    show-overflow-tooltip
                >
                    <template #default="{ row }">
                        <span v-if="!row.guideFileUrl">-</span>
                        <div v-else>
                            <el-button
                                v-if="isImage(row.guideFileUrl)"
                                link
                                type="primary"
                                @click="previewAttachment(row.guideFileUrl)"
                            >
                                预览
                            </el-button>
                            <el-button
                                link
                                type="primary"
                                :disabled="downloadingUrl === row.guideFileUrl"
                                :loading="downloadingUrl === row.guideFileUrl"
                                @click="downloadAttachment(row.guideFileUrl)"
                            >
                                下载
                            </el-button>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column
                    label="指南类型"
                    prop="guideType"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                >
                    <template #default="{ row }">
                        <el-tag v-if="row.guideType === 'general'" type="primary">通用指南</el-tag>
                        <el-tag v-else-if="row.guideType === 'operation'" type="success"
                            >操作指南
                        </el-tag>
                        <el-tag v-else-if="row.guideType === 'quality'" type="warning"
                            >质控指南
                        </el-tag>
                        <el-tag v-else-if="row.guideType === 'other'" type="info">其他</el-tag>
                        <span v-else>{{ row.guideType }}</span>
                    </template>
                </el-table-column>
                <el-table-column
                    label="状态"
                    prop="status"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                >
                    <template #default="{ row }">
                        <el-tag v-if="row.status === 'active'" type="success">有效</el-tag>
                        <el-tag v-else-if="row.status === 'expired'" type="danger">已过期</el-tag>
                        <el-tag v-else-if="row.status === 'archived'" type="info">已归档</el-tag>
                        <span v-else>{{ row.status }}</span>
                    </template>
                </el-table-column>
                <el-table-column
                    label="发布时间"
                    prop="publishedAt"
                    min-width="140"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="发布人"
                    prop="createdBy"
                    min-width="100"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column label="操作" width="180" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            type="primary"
                            link
                            @click="handleEdit(row)"
                            v-if="routerStore.hasPermission('guides:update')"
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
                            v-if="routerStore.hasPermission('guides:delete')"
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
                <el-form-item label="所属机构" prop="institutionId">
                    <el-select
                        v-model="formData.institutionId"
                        placeholder="请选择所属机构"
                        clearable
                        filterable
                        style="width: 100%"
                    >
                        <el-option
                            v-for="inst in institutionList"
                            :key="inst.id"
                            :label="inst.institutionName"
                            :value="inst.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="指南标题" prop="guideTitle">
                    <el-input v-model="formData.guideTitle" placeholder="请输入指南标题" />
                </el-form-item>
                <el-form-item label="指南内容" prop="guideContent">
                    <el-input
                        v-model="formData.guideContent"
                        type="textarea"
                        :rows="4"
                        placeholder="请输入指南内容"
                    />
                </el-form-item>
                <el-form-item label="指南附件" prop="guideFileUrl">
                    <el-upload
                        action="#"
                        :auto-upload="true"
                        :http-request="handleUpload"
                        :show-file-list="false"
                    >
                        <el-button type="primary">
                            <el-icon><Upload /></el-icon>
                            上传附件
                        </el-button>
                    </el-upload>
                    <div v-if="formData.guideFileUrl" class="mt-2 flex items-center gap-2">
                        <el-link
                            :href="formData.guideFileUrl"
                            target="_blank"
                            type="primary"
                            :title="formData.guideFileUrl"
                            style="
                                max-width: 300px;
                                overflow: hidden;
                                text-overflow: ellipsis;
                                white-space: nowrap;
                            "
                        >
                            {{ formData.guideFileUrl.split('/').pop() || formData.guideFileUrl }}
                        </el-link>
                        <el-button
                            link
                            type="danger"
                            size="small"
                            @click="formData.guideFileUrl = undefined"
                        >
                            移除
                        </el-button>
                    </div>
                </el-form-item>
                <el-form-item label="指南类型" prop="guideType">
                    <el-select
                        class="min-w-48"
                        v-model="formData.guideType"
                        placeholder="请选择指南类型"
                        style="width: 100%"
                    >
                        <el-option label="通用指南" value="general" />
                        <el-option label="操作指南" value="operation" />
                        <el-option label="质控指南" value="quality" />
                        <el-option label="其他" value="other" />
                    </el-select>
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-select
                        class="min-w-48"
                        v-model="formData.status"
                        placeholder="请选择状态"
                        style="width: 100%"
                    >
                        <el-option label="有效" value="active" />
                        <el-option label="已过期" value="expired" />
                        <el-option label="已归档" value="archived" />
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
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Upload } from '@element-plus/icons-vue'
import { useRouterStore } from '@/stores/modules/router'
import { useAuthStore } from '@/stores/modules/auth.js'
import api from '@/api/index.js'

const routerStore = useRouterStore()
const authStore = useAuthStore()

const loading = ref(false)
const submitLoading = ref(false)
const downloadingUrl = ref(null)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const institutionList = ref([])

const queryParams = reactive({
    current: 1,
    size: 10,
    id: undefined,
    institutionId: undefined,
    guideTitle: undefined,
    guideContent: undefined,
    guideFileUrl: undefined,
    guideType: undefined,
    status: undefined,
    createdBy: undefined,
})

const formData = reactive({
    id: undefined,
    institutionId: undefined,
    guideTitle: undefined,
    guideContent: undefined,
    guideFileUrl: undefined,
    guideType: undefined,
    status: undefined,
    publishedAt: undefined,
    createdBy: undefined,
    isDeleted: undefined,
})

const formRules = {
    institutionId: [
        {
            required: true,
            message: '请选择所属机构',
            trigger: 'change',
        },
    ],
    guideTitle: [
        {
            required: true,
            message: '请输入指南标题',
            trigger: 'blur',
        },
    ],
    guideContent: [
        {
            required: true,
            message: '请输入指南内容',
            trigger: 'blur',
        },
    ],
    guideFileUrl: [
        {
            required: true,
            message: '请上传指南附件',
            trigger: 'change',
        },
    ],
    guideType: [
        {
            required: true,
            message: '请选择指南类型',
            trigger: 'change',
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

const dialogTitle = computed(() => (formData.id ? '编辑机构指南' : '新增机构指南'))

const handleQuery = async () => {
    loading.value = true
    try {
        const { data } = await api.guides.pageGuides(queryParams)
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
    queryParams.institutionId = undefined
    queryParams.guideTitle = undefined
    queryParams.guideContent = undefined
    queryParams.guideFileUrl = undefined
    queryParams.guideType = undefined
    queryParams.status = undefined
    queryParams.createdBy = undefined
    handleQuery()
}

const handleSelectionChange = (selection) => {
    selectedIds.value = selection.map((item) => item.id)
}

const handleAdd = () => {
    resetForm()
    formData.status = 'active'
    formData.createdBy = authStore.userInfo?.id
    dialogVisible.value = true
}

const handleEdit = async (row) => {
    resetForm()
    try {
        const { data } = await api.guides.getGuidesById(row.id)
        Object.assign(formData, data.data)
        dialogVisible.value = true
    } catch (error) {
        ElMessage.error('获取详情失败')
    }
}

const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm('确定要删除该记录吗？', '提示', { type: 'warning' })
        await api.guides.deleteGuides(row.id)
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
        await api.guides.batchDeleteGuides(selectedIds.value)
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
        const submitData = { ...formData }
        delete submitData.publishedAt
        delete submitData.createdAt
        delete submitData.updatedAt
        if (formData.id) {
            delete submitData.createdBy
            await api.guides.updateGuides(submitData)
            ElMessage.success('更新成功')
        } else {
            await api.guides.saveGuides(submitData)
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
    formData.institutionId = undefined
    formData.guideTitle = undefined
    formData.guideContent = undefined
    formData.guideFileUrl = undefined
    formData.guideType = undefined
    formData.status = undefined
    formData.publishedAt = undefined
    formData.createdBy = undefined
    formData.isDeleted = undefined
    formRef.value?.resetFields()
}

const handleUpload = async (options) => {
    const { file } = options
    const fd = new FormData()
    fd.append('file', file)
    try {
        const { data } = await api.oss.uploadFile(fd)
        formData.guideFileUrl = data.data.permanentUrl || data.data.url
        ElMessage.success('上传成功')
        formRef.value?.validateField('guideFileUrl')
    } catch (error) {
        ElMessage.error('上传失败')
    }
}

const isImage = (url) => {
    if (!url) return false
    const ext = url.split('.').pop().toLowerCase()
    return ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].includes(ext)
}

const previewAttachment = (url) => {
    window.open(url, '_blank')
}

const downloadAttachment = async (url) => {
    if (!url || downloadingUrl.value === url) return
    downloadingUrl.value = url
    try {
        const response = await fetch(url)
        const blob = await response.blob()
        const blobUrl = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = blobUrl
        link.download = url.split('/').pop() || 'attachment'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(blobUrl)
    } catch (error) {
        ElMessage.error('下载失败')
    } finally {
        downloadingUrl.value = null
    }
}

const getInstitutionName = (id) => {
    const institution = institutionList.value.find((item) => item.id === id)
    return institution ? institution.institutionName : id
}

const loadInstitutionList = async () => {
    try {
        const { data } = await api.institutions.listInstitutions()
        institutionList.value = data.data || []
    } catch (e) {
        // ignore
    }
}

loadInstitutionList()
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
