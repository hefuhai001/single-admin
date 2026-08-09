<template>
    <div class="h-full flex flex-col gap-4">
        <el-card shadow="never" class="shrink-0">
            <div class="flex items-center gap-4 flex-wrap">
                <el-select
                    v-model="currentBucket"
                    placeholder="请选择存储桶"
                    style="width: 220px"
                    @change="handleBucketChange"
                >
                    <el-option
                        v-for="bucket in bucketList"
                        :key="bucket.name"
                        :label="bucket.name"
                        :value="bucket.name"
                    />
                </el-select>
                <el-button
                    type="primary"
                    @click="openCreateBucket"
                    v-if="routerStore.hasPermission('minio:bucket:add')"
                >
                    <el-icon>
                        <Plus />
                    </el-icon>
                    创建存储桶
                </el-button>
                <el-button
                    type="danger"
                    plain
                    :disabled="!currentBucket"
                    @click="handleDeleteBucket"
                    v-if="routerStore.hasPermission('minio:bucket:delete')"
                >
                    <el-icon>
                        <Delete />
                    </el-icon>
                    删除存储桶
                </el-button>
                <el-upload
                    :action="uploadAction"
                    :headers="uploadHeaders"
                    :data="{ bucket: currentBucket }"
                    :before-upload="beforeUpload"
                    :on-success="handleUploadSuccess"
                    :show-file-list="false"
                    :disabled="!currentBucket"
                >
                    <el-button
                        type="primary"
                        :disabled="!currentBucket"
                        v-if="routerStore.hasPermission('minio:object:upload')"
                    >
                        <el-icon>
                            <Upload />
                        </el-icon>
                        上传文件
                    </el-button>
                </el-upload>
                <el-input
                    v-model="queryParams.prefix"
                    placeholder="输入前缀过滤"
                    clearable
                    style="width: 200px"
                    @keyup.enter="handleQuery"
                />
                <el-button @click="resetQuery">
                    <el-icon>
                        <Refresh />
                    </el-icon>
                    刷新
                </el-button>
                <el-button
                    type="danger"
                    plain
                    :disabled="selectedObjects.length === 0"
                    @click="handleBatchDelete"
                    v-if="routerStore.hasPermission('minio:object:delete')"
                >
                    <el-icon>
                        <Delete />
                    </el-icon>
                    批量删除
                </el-button>
            </div>
        </el-card>

        <el-card shadow="never" class="flex-1 min-h-0">
            <el-table
                border
                v-loading="loading"
                :data="tableData"
                @selection-change="handleSelectionChange"
            >
                <el-table-column type="selection" width="55" align="center" />
                <el-table-column
                    label="对象名称"
                    prop="objectName"
                    min-width="240"
                    show-overflow-tooltip
                />
                <el-table-column label="大小" prop="size" width="120" align="center">
                    <template #default="{ row }">
                        {{ formatSize(row.size) }}
                    </template>
                </el-table-column>
                <el-table-column
                    label="最后修改时间"
                    prop="lastModified"
                    min-width="160"
                    align="center"
                />
                <el-table-column label="操作" width="220" align="center" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            type="primary"
                            link
                            @click="handlePreview(row)"
                            v-if="routerStore.hasPermission('minio:object:preview')"
                        >
                            <el-icon>
                                <View />
                            </el-icon>
                            预览
                        </el-button>
                        <el-button
                            type="primary"
                            link
                            @click="handleDownload(row)"
                            v-if="routerStore.hasPermission('minio:object:download')"
                        >
                            <el-icon>
                                <Download />
                            </el-icon>
                            下载
                        </el-button>
                        <el-button
                            type="danger"
                            link
                            @click="handleDelete(row)"
                            v-if="routerStore.hasPermission('minio:object:delete')"
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
                class="flex justify-end items-center mt-4"
                v-model:current-page="queryParams.current"
                v-model:page-size="queryParams.size"
                :page-sizes="[10, 20, 50, 100]"
                :total="total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleQuery"
                @current-change="handleQuery"
            />
        </el-card>

        <el-dialog v-model="bucketDialogVisible" title="创建存储桶" width="400px">
            <el-form
                ref="bucketFormRef"
                :model="bucketForm"
                :rules="bucketRules"
                label-width="100px"
            >
                <el-form-item label="存储桶名称" prop="name">
                    <el-input
                        v-model="bucketForm.name"
                        placeholder="小写字母、数字、中划线，3-63 字符"
                    />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="bucketDialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="bucketSubmitLoading" @click="handleCreateBucket"
                    >确定
                </el-button>
            </template>
        </el-dialog>

        <el-dialog v-model="previewVisible" title="文件预览" width="600px">
            <div class="flex justify-center">
                <el-image
                    v-if="isImage(previewObjectName)"
                    :src="previewUrl"
                    fit="contain"
                    style="max-height: 500px"
                />
                <div v-else>该文件类型暂不支持在线预览，请下载查看</div>
            </div>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Download, View, Refresh, Upload } from '@element-plus/icons-vue'
import { useRouterStore } from '@/stores/modules/router'
import { useAuthStore } from '@/stores/modules/auth'
import api from '@/api/index.js'

const routerStore = useRouterStore()
const authStore = useAuthStore()

const loading = ref(false)
const bucketList = ref([])
const tableData = ref([])
const total = ref(0)
const selectedObjects = ref([])
const currentBucket = ref('')

const queryParams = reactive({
    bucket: '',
    prefix: undefined,
    current: 1,
    size: 10,
    recursive: true,
})

const uploadAction = computed(() => import.meta.env.VITE_API_URL + '/admin/minio/objects/upload')
const uploadHeaders = computed(() => ({
    Authorization: 'Bearer ' + authStore.token,
}))

const bucketDialogVisible = ref(false)
const bucketSubmitLoading = ref(false)
const bucketFormRef = ref(null)
const bucketForm = reactive({
    name: '',
})
const bucketRules = {
    name: [
        { required: true, message: '请输入存储桶名称', trigger: 'blur' },
        {
            pattern: /^[a-z0-9][a-z0-9-]{1,61}[a-z0-9]$/,
            message: '存储桶名称格式不正确',
            trigger: 'blur',
        },
    ],
}

const previewVisible = ref(false)
const previewUrl = ref('')
const previewObjectName = ref('')

const loadBuckets = async () => {
    try {
        const { data } = await api.minio.listBuckets()
        bucketList.value = data.data || []
        if (bucketList.value.length && !currentBucket.value) {
            currentBucket.value = bucketList.value[0].name
            handleBucketChange()
        }
    } catch (error) {
        ElMessage.error('加载存储桶失败')
    }
}

const handleBucketChange = () => {
    queryParams.bucket = currentBucket.value
    queryParams.current = 1
    handleQuery()
}

const handleQuery = async () => {
    if (!currentBucket.value) {
        tableData.value = []
        total.value = 0
        return
    }
    loading.value = true
    try {
        queryParams.bucket = currentBucket.value
        const { data } = await api.minio.listObjects(queryParams)
        tableData.value = data.data.records || []
        total.value = data.data.total || 0
    } catch (error) {
        ElMessage.error('查询失败')
    } finally {
        loading.value = false
    }
}

const resetQuery = () => {
    queryParams.prefix = undefined
    queryParams.current = 1
    handleQuery()
}

const handleSelectionChange = (selection) => {
    selectedObjects.value = selection.map((item) => item.objectName)
}

const beforeUpload = () => {
    if (!currentBucket.value) {
        ElMessage.warning('请先选择存储桶')
        return false
    }
    return true
}

const handleUploadSuccess = () => {
    ElMessage.success('上传成功')
    handleQuery()
}

const openCreateBucket = () => {
    bucketForm.name = ''
    bucketDialogVisible.value = true
}

const handleCreateBucket = async () => {
    try {
        await bucketFormRef.value.validate()
        bucketSubmitLoading.value = true
        await api.minio.createBucket(bucketForm.name)
        ElMessage.success('创建成功')
        bucketDialogVisible.value = false
        await loadBuckets()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('创建失败')
        }
    } finally {
        bucketSubmitLoading.value = false
    }
}

const handleDeleteBucket = async () => {
    try {
        await ElMessageBox.confirm('删除存储桶前请确保为空，是否继续？', '提示', {
            type: 'warning',
        })
        await api.minio.deleteBucket(currentBucket.value)
        ElMessage.success('删除成功')
        currentBucket.value = ''
        await loadBuckets()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('删除失败')
        }
    }
}

const handlePreview = async (row) => {
    try {
        const { data } = await api.minio.previewFile({
            bucket: currentBucket.value,
            objectName: row.objectName,
        })
        previewUrl.value = data.data
        previewObjectName.value = row.objectName
        previewVisible.value = true
    } catch (error) {
        ElMessage.error('获取预览地址失败')
    }
}

const handleDownload = async (row) => {
    try {
        const { data } = await api.minio.downloadFile({
            bucket: currentBucket.value,
            objectName: row.objectName,
        })
        const blob = new Blob([data])
        const url = URL.createObjectURL(blob)
        const filename = row.objectName.includes('/')
            ? row.objectName.substring(row.objectName.lastIndexOf('/') + 1)
            : row.objectName
        const a = document.createElement('a')
        a.href = url
        a.download = filename
        document.body.appendChild(a)
        a.click()
        URL.revokeObjectURL(url)
        document.body.removeChild(a)
    } catch (error) {
        ElMessage.error('下载失败')
    }
}

const handleDelete = async (row) => {
    try {
        await ElMessageBox.confirm('确定要删除该文件吗？', '提示', { type: 'warning' })
        await api.minio.deleteObject({
            bucket: currentBucket.value,
            objectName: row.objectName,
        })
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
        await ElMessageBox.confirm('确定要删除选中的文件吗？', '提示', { type: 'warning' })
        await api.minio.batchDeleteObjects(currentBucket.value, selectedObjects.value)
        ElMessage.success('批量删除成功')
        handleQuery()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('批量删除失败')
        }
    }
}

const formatSize = (bytes) => {
    if (bytes == null || bytes === 0) {
        return '0 B'
    }
    const units = ['B', 'KB', 'MB', 'GB', 'TB']
    let i = 0
    let size = bytes
    while (size >= 1024 && i < units.length - 1) {
        size /= 1024
        i++
    }
    return size.toFixed(2) + ' ' + units[i]
}

const isImage = (name) => {
    return /\.(png|jpg|jpeg|gif|webp|bmp)$/i.test(name)
}

onMounted(() => {
    loadBuckets()
})
</script>

<style scoped>
.app-container {
    padding: 0;
}
.mt-4 {
    margin-top: 16px;
}
</style>
