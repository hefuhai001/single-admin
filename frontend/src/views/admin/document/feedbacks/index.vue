<template>
    <div class="h-full flex flex-col gap-4">
        <el-card shadow="never" class="shrink-0">
            <el-form
                :model="queryParams"
                inline
                v-if="routerStore.hasPermission('feedbacks:query')"
            >
                <el-form-item label="反馈ID">
                    <el-input
                        v-model="queryParams.id"
                        placeholder="请输入反馈ID"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="机构ID">
                    <el-input
                        v-model="queryParams.institutionId"
                        placeholder="请输入机构ID"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="反馈类型">
                    <el-select
                        class="min-w-48"
                        v-model="queryParams.feedbackType"
                        placeholder="请选择反馈类型"
                        clearable
                        @keyup.enter="handleQuery"
                    >
                        <el-option label="疑问" value="question" />
                        <el-option label="建议" value="suggestion" />
                        <el-option label="投诉" value="complaint" />
                        <el-option label="问题报告" value="issue" />
                        <el-option label="其他" value="other" />
                    </el-select>
                </el-form-item>
                <el-form-item label="反馈标题">
                    <el-input
                        v-model="queryParams.feedbackTitle"
                        placeholder="请输入反馈标题"
                        clearable
                        @keyup.enter="handleQuery"
                    />
                </el-form-item>
                <el-form-item label="优先级">
                    <el-select
                        class="min-w-48"
                        v-model="queryParams.priority"
                        placeholder="请选择优先级"
                        clearable
                        @keyup.enter="handleQuery"
                    >
                        <el-option label="低" value="low" />
                        <el-option label="中" value="medium" />
                        <el-option label="高" value="high" />
                        <el-option label="紧急" value="urgent" />
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
                        <el-option label="待处理" value="pending" />
                        <el-option label="处理中" value="processing" />
                        <el-option label="已解决" value="resolved" />
                        <el-option label="已关闭" value="closed" />
                        <el-option label="已驳回" value="rejected" />
                    </el-select>
                </el-form-item>
                <el-form-item label="反馈人ID">
                    <el-input
                        v-model="queryParams.feedbackBy"
                        placeholder="请输入反馈人ID"
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
                v-if="routerStore.hasPermission('feedbacks:add')"
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
                v-if="routerStore.hasPermission('feedbacks:delete')"
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
                    label="反馈机构"
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
                    label="反馈类型"
                    prop="feedbackType"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                >
                    <template #default="{ row }">
                        <el-tag v-if="row.feedbackType === 'question'" type="primary">疑问</el-tag>
                        <el-tag v-else-if="row.feedbackType === 'suggestion'" type="success"
                            >建议
                        </el-tag>
                        <el-tag v-else-if="row.feedbackType === 'complaint'" type="danger"
                            >投诉
                        </el-tag>
                        <el-tag v-else-if="row.feedbackType === 'issue'" type="warning"
                            >问题报告
                        </el-tag>
                        <el-tag v-else-if="row.feedbackType === 'other'" type="info">其他</el-tag>
                        <span v-else>{{ row.feedbackType }}</span>
                    </template>
                </el-table-column>
                <el-table-column
                    label="反馈标题"
                    prop="feedbackTitle"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="反馈内容"
                    prop="feedbackContent"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column
                    label="附件"
                    prop="attachmentUrl"
                    min-width="140"
                    align="center"
                    show-overflow-tooltip
                >
                    <template #default="{ row }">
                        <span v-if="!row.attachmentUrl">-</span>
                        <div v-else>
                            <el-button
                                v-if="isImage(row.attachmentUrl)"
                                link
                                type="primary"
                                @click="previewAttachment(row.attachmentUrl)"
                            >
                                预览
                            </el-button>
                            <el-button
                                link
                                type="primary"
                                :disabled="downloadingUrl === row.attachmentUrl"
                                :loading="downloadingUrl === row.attachmentUrl"
                                @click="downloadAttachment(row.attachmentUrl)"
                            >
                                下载
                            </el-button>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column
                    label="优先级"
                    prop="priority"
                    min-width="120"
                    align="center"
                    show-overflow-tooltip
                >
                    <template #default="{ row }">
                        <el-tag v-if="row.priority === 'low'" type="info">低</el-tag>
                        <el-tag v-else-if="row.priority === 'medium'" type="primary">中</el-tag>
                        <el-tag v-else-if="row.priority === 'high'" type="warning">高</el-tag>
                        <el-tag v-else-if="row.priority === 'urgent'" type="danger">紧急</el-tag>
                        <span v-else>{{ row.priority }}</span>
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
                        <el-tag v-if="row.status === 'pending'" type="info">待处理</el-tag>
                        <el-tag v-else-if="row.status === 'processing'" type="primary"
                            >处理中
                        </el-tag>
                        <el-tag v-else-if="row.status === 'resolved'" type="success">已解决</el-tag>
                        <el-tag v-else-if="row.status === 'closed'" type="warning">已关闭</el-tag>
                        <el-tag v-else-if="row.status === 'rejected'" type="danger">已驳回</el-tag>
                        <span v-else>{{ row.status }}</span>
                    </template>
                </el-table-column>
                <el-table-column
                    label="反馈人"
                    prop="feedbackBy"
                    min-width="100"
                    align="center"
                    show-overflow-tooltip
                />
                <el-table-column label="操作" width="180" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            type="primary"
                            link
                            @click="handleReply(row)"
                            v-if="routerStore.hasPermission('feedbacks:update') && !row.replyContent"
                        >
                            <el-icon>
                                <Edit />
                            </el-icon>
                            回复
                        </el-button>
                        <el-button
                            type="danger"
                            link
                            @click="handleDelete(row)"
                            v-if="routerStore.hasPermission('feedbacks:delete')"
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
                <el-form-item label="反馈机构" prop="institutionId">
                    <el-select
                        v-model="formData.institutionId"
                        placeholder="请选择反馈机构"
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
                <el-form-item label="反馈类型" prop="feedbackType">
                    <el-select
                        class="min-w-48"
                        v-model="formData.feedbackType"
                        placeholder="请选择反馈类型"
                        style="width: 100%"
                    >
                        <el-option label="疑问" value="question" />
                        <el-option label="建议" value="suggestion" />
                        <el-option label="投诉" value="complaint" />
                        <el-option label="问题报告" value="issue" />
                        <el-option label="其他" value="other" />
                    </el-select>
                </el-form-item>
                <el-form-item label="反馈标题" prop="feedbackTitle">
                    <el-input v-model="formData.feedbackTitle" placeholder="请输入反馈标题" />
                </el-form-item>
                <el-form-item label="反馈内容" prop="feedbackContent">
                    <el-input
                        v-model="formData.feedbackContent"
                        type="textarea"
                        :rows="4"
                        placeholder="请输入反馈内容"
                    />
                </el-form-item>
                <el-form-item label="优先级" prop="priority">
                    <el-select
                        class="min-w-48"
                        v-model="formData.priority"
                        placeholder="请选择优先级"
                        style="width: 100%"
                    >
                        <el-option label="低" value="low" />
                        <el-option label="中" value="medium" />
                        <el-option label="高" value="high" />
                        <el-option label="紧急" value="urgent" />
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

        <el-dialog v-model="replyDialogVisible" title="回复反馈" width="600px" @close="resetReplyForm">
            <el-form
                ref="replyFormRef"
                :model="replyFormData"
                :rules="replyFormRules"
                label-width="100px"
                style="padding-right: 60px"
            >
                <el-form-item label="反馈内容">
                    <el-input
                        v-model="replyFormData.feedbackContent"
                        type="textarea"
                        :rows="4"
                        disabled
                    />
                </el-form-item>
                <el-form-item label="回复内容" prop="replyContent">
                    <el-input
                        v-model="replyFormData.replyContent"
                        type="textarea"
                        :rows="4"
                        placeholder="请输入回复内容"
                    />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-select
                        class="min-w-48"
                        v-model="replyFormData.status"
                        placeholder="请选择状态"
                        style="width: 100%"
                    >
                        <el-option label="处理中" value="processing" />
                        <el-option label="已解决" value="resolved" />
                        <el-option label="已关闭" value="closed" />
                        <el-option label="已驳回" value="rejected" />
                    </el-select>
                </el-form-item>
                <el-form-item label="附件">
                    <el-upload
                        action="#"
                        :auto-upload="true"
                        :http-request="handleReplyUpload"
                        :show-file-list="false"
                    >
                        <el-button type="primary">
                            <el-icon><Upload /></el-icon>
                            上传附件
                        </el-button>
                    </el-upload>
                    <div v-if="replyFormData.attachmentUrl" class="mt-2">
                        <el-link
                            :href="replyFormData.attachmentUrl"
                            target="_blank"
                            type="primary"
                        >
                            {{ replyFormData.attachmentUrl }}
                        </el-link>
                        <el-button
                            link
                            type="danger"
                            size="small"
                            @click="replyFormData.attachmentUrl = undefined"
                            class="ml-2"
                        >
                            移除
                        </el-button>
                    </div>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="replyDialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="submitLoading" @click="handleReplySubmit"
                    >确定回复
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
const replyDialogVisible = ref(false)
const replyFormRef = ref(null)
const institutionList = ref([])
const replySubmitLoading = ref(false)

const queryParams = reactive({
    current: 1,
    size: 10,
    id: undefined,
    institutionId: undefined,
    feedbackType: undefined,
    feedbackTitle: undefined,
    feedbackContent: undefined,
    attachmentUrl: undefined,
    priority: undefined,
    status: undefined,
    replyContent: undefined,
    repliedBy: undefined,
    feedbackBy: undefined,
})

const formData = reactive({
    id: undefined,
    institutionId: undefined,
    feedbackType: undefined,
    feedbackTitle: undefined,
    feedbackContent: undefined,
    attachmentUrl: undefined,
    priority: undefined,
    status: undefined,
    replyContent: undefined,
    repliedBy: undefined,
    repliedAt: undefined,
    feedbackBy: undefined,
})

const replyFormData = reactive({
    id: undefined,
    feedbackContent: undefined,
    replyContent: undefined,
    status: undefined,
    attachmentUrl: undefined,
})

const formRules = {
    institutionId: [
        {
            required: true,
            message: '请选择反馈机构',
            trigger: 'change',
        },
    ],
    feedbackType: [
        {
            required: true,
            message: '请选择反馈类型',
            trigger: 'change',
        },
    ],
    feedbackTitle: [
        {
            required: true,
            message: '请输入反馈标题',
            trigger: 'blur',
        },
    ],
    feedbackContent: [
        {
            required: true,
            message: '请输入反馈内容',
            trigger: 'blur',
        },
    ],
    priority: [
        {
            required: true,
            message: '请选择优先级',
            trigger: 'change',
        },
    ],
}

const replyFormRules = {
    replyContent: [
        {
            required: true,
            message: '请输入回复内容',
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

const dialogTitle = computed(() => '新增机构反馈表')

const handleQuery = async () => {
    loading.value = true
    try {
        const { data } = await api.feedbacks.pageFeedbacks(queryParams)
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
    queryParams.feedbackType = undefined
    queryParams.feedbackTitle = undefined
    queryParams.feedbackContent = undefined
    queryParams.attachmentUrl = undefined
    queryParams.priority = undefined
    queryParams.status = undefined
    queryParams.replyContent = undefined
    queryParams.repliedBy = undefined
    queryParams.feedbackBy = undefined
    handleQuery()
}

const handleSelectionChange = (selection) => {
    selectedIds.value = selection.map((item) => item.id)
}

const handleAdd = () => {
    resetForm()
    formData.status = 'pending'
    formData.feedbackBy = authStore.userInfo?.id
    dialogVisible.value = true
}

const handleReply = async (row) => {
    resetReplyForm()
    try {
        const { data } = await api.feedbacks.getFeedbacksById(row.id)
        replyFormData.id = data.data.id
        replyFormData.feedbackContent = data.data.feedbackContent
        replyFormData.status = data.data.status === 'pending' ? 'processing' : data.data.status
        replyFormData.attachmentUrl = data.data.attachmentUrl
        replyDialogVisible.value = true
    } catch (error) {
        ElMessage.error('获取详情失败')
    }
}

const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm('确定要删除该记录吗？', '提示', { type: 'warning' })
        await api.feedbacks.deleteFeedbacks(row.id)
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
        await api.feedbacks.batchDeleteFeedbacks(selectedIds.value)
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
        await api.feedbacks.saveFeedbacks(formData)
        ElMessage.success('新增成功')
        dialogVisible.value = false
        handleQuery()
    } catch (error) {
        ElMessage.error('操作失败')
    } finally {
        submitLoading.value = false
    }
}

const handleReplySubmit = async () => {
    try {
        await replyFormRef.value.validate()
        submitLoading.value = true
        await api.feedbacks.updateFeedbacks({
            id: replyFormData.id,
            replyContent: replyFormData.replyContent,
            status: replyFormData.status,
            repliedBy: authStore.userInfo?.id,
            attachmentUrl: replyFormData.attachmentUrl,
        })
        ElMessage.success('回复成功')
        replyDialogVisible.value = false
        handleQuery()
    } catch (error) {
        ElMessage.error('回复失败')
    } finally {
        submitLoading.value = false
    }
}

const handleReplyUpload = async (options) => {
    const { file } = options
    const formData = new FormData()
    formData.append('file', file)
    try {
        const { data } = await api.minio.uploadFile(formData)
        replyFormData.attachmentUrl = data.data.permanentUrl || data.data.url
        ElMessage.success('上传成功')
    } catch (error) {
        ElMessage.error('上传失败')
    }
}

const resetForm = () => {
    formData.id = undefined
    formData.institutionId = undefined
    formData.feedbackType = undefined
    formData.feedbackTitle = undefined
    formData.feedbackContent = undefined
    formData.attachmentUrl = undefined
    formData.priority = undefined
    formData.status = undefined
    formData.replyContent = undefined
    formData.repliedBy = undefined
    formData.repliedAt = undefined
    formData.feedbackBy = undefined
    formRef.value?.resetFields()
}

const resetReplyForm = () => {
    replyFormData.id = undefined
    replyFormData.feedbackContent = undefined
    replyFormData.replyContent = undefined
    replyFormData.status = undefined
    replyFormData.attachmentUrl = undefined
    replyFormRef.value?.resetFields()
}

const getInstitutionName = (id) => {
    const institution = institutionList.value.find((item) => item.id === id)
    return institution ? institution.institutionName : id
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
