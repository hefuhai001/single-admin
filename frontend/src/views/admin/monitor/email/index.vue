<template>
    <div class="h-full flex flex-col gap-4">
        <el-card shadow="never" class="flex-1 min-h-0 overflow-auto">
            <el-tabs v-model="activeTab">
                <!-- ==================== 简单文本 ==================== -->
                <el-tab-pane label="简单文本" name="simple">
                    <el-form
                        ref="simpleFormRef"
                        :model="simpleForm"
                        :rules="simpleRules"
                        label-width="90px"
                        class="max-w-2xl mt-4"
                    >
                        <el-form-item label="收件人" prop="to">
                            <el-input
                                v-model="simpleForm.to"
                                placeholder="单个收件人邮箱，如 example@qq.com"
                                clearable
                            />
                        </el-form-item>
                        <el-form-item label="收件人列表" prop="toList">
                            <el-input
                                v-model="simpleForm.toList"
                                placeholder="多个收件人，用逗号分隔（群发，与收件人至少填一项）"
                                clearable
                            />
                        </el-form-item>
                        <el-form-item label="抄送" prop="cc">
                            <el-input
                                v-model="simpleForm.cc"
                                placeholder="抄送邮箱，多个用逗号分隔（可选）"
                                clearable
                            />
                        </el-form-item>
                        <el-form-item label="邮件主题" prop="subject">
                            <el-input v-model="simpleForm.subject" placeholder="请输入邮件主题" clearable />
                        </el-form-item>
                        <el-form-item label="邮件内容" prop="content">
                            <el-input
                                v-model="simpleForm.content"
                                type="textarea"
                                :rows="6"
                                placeholder="请输入邮件正文内容"
                            />
                        </el-form-item>
                        <el-form-item>
                            <el-button
                                type="primary"
                                :loading="sending"
                                @click="handleSimpleSend"
                                v-if="routerStore.hasPermission('email:send')"
                            >
                                <el-icon><Promotion /></el-icon> 发送
                            </el-button>
                        </el-form-item>
                    </el-form>
                </el-tab-pane>

                <!-- ==================== HTML 邮件 ==================== -->
                <el-tab-pane label="HTML 邮件" name="html">
                    <el-form
                        ref="htmlFormRef"
                        :model="htmlForm"
                        :rules="htmlRules"
                        label-width="90px"
                        class="max-w-2xl mt-4"
                    >
                        <el-form-item label="收件人" prop="to">
                            <el-input
                                v-model="htmlForm.to"
                                placeholder="单个收件人邮箱，如 example@qq.com"
                                clearable
                            />
                        </el-form-item>
                        <el-form-item label="收件人列表" prop="toList">
                            <el-input
                                v-model="htmlForm.toList"
                                placeholder="多个收件人，用逗号分隔（群发，与收件人至少填一项）"
                                clearable
                            />
                        </el-form-item>
                        <el-form-item label="抄送" prop="cc">
                            <el-input
                                v-model="htmlForm.cc"
                                placeholder="抄送邮箱，多个用逗号分隔（可选）"
                                clearable
                            />
                        </el-form-item>
                        <el-form-item label="密送" prop="bcc">
                            <el-input
                                v-model="htmlForm.bcc"
                                placeholder="密送邮箱，多个用逗号分隔（可选）"
                                clearable
                            />
                        </el-form-item>
                        <el-form-item label="邮件主题" prop="subject">
                            <el-input v-model="htmlForm.subject" placeholder="请输入邮件主题" clearable />
                        </el-form-item>
                        <el-form-item label="HTML 内容" prop="content">
                            <el-input
                                v-model="htmlForm.content"
                                type="textarea"
                                :rows="6"
                                placeholder="请输入 HTML 代码，邮件客户端将渲染显示"
                            />
                        </el-form-item>
                        <el-form-item label="附件路径" prop="attachments">
                            <el-input
                                v-model="htmlForm.attachments"
                                placeholder="服务器本地绝对路径，多个用逗号分隔（可选），如 /tmp/report.pdf"
                                clearable
                            />
                        </el-form-item>
                        <el-form-item>
                            <el-button
                                type="primary"
                                :loading="sending"
                                @click="handleHtmlSend"
                                v-if="routerStore.hasPermission('email:send')"
                            >
                                <el-icon><Promotion /></el-icon> 发送
                            </el-button>
                        </el-form-item>
                    </el-form>
                </el-tab-pane>

                <!-- ==================== 模板邮件 ==================== -->
                <el-tab-pane label="模板邮件" name="template">
                    <el-form
                        ref="templateFormRef"
                        :model="templateForm"
                        :rules="templateRules"
                        label-width="90px"
                        class="max-w-2xl mt-4"
                    >
                        <el-form-item label="收件人" prop="to">
                            <el-input
                                v-model="templateForm.to"
                                placeholder="单个收件人邮箱，如 example@qq.com"
                                clearable
                            />
                        </el-form-item>
                        <el-form-item label="收件人列表" prop="toList">
                            <el-input
                                v-model="templateForm.toList"
                                placeholder="多个收件人，用逗号分隔（群发，与收件人至少填一项）"
                                clearable
                            />
                        </el-form-item>
                        <el-form-item label="邮件主题" prop="subject">
                            <el-input v-model="templateForm.subject" placeholder="请输入邮件主题" clearable />
                        </el-form-item>
                        <el-form-item label="模板名称" prop="templateName">
                            <el-select v-model="templateForm.templateName" placeholder="请选择模板" style="width: 100%">
                                <el-option label="mail/welcome（欢迎/激活邮件）" value="mail/welcome" />
                            </el-select>
                        </el-form-item>
                        <el-form-item label="用户昵称" prop="username">
                            <el-input v-model="templateForm.username" placeholder="模板变量 username" clearable />
                        </el-form-item>
                        <el-form-item label="验证码" prop="code">
                            <el-input v-model="templateForm.code" placeholder="模板变量 code" clearable />
                        </el-form-item>
                        <el-form-item label="激活链接" prop="link">
                            <el-input v-model="templateForm.link" placeholder="模板变量 link，如 https://example.com/activate" clearable />
                        </el-form-item>
                        <el-form-item label="有效期(分)" prop="expireMinutes">
                            <el-input-number v-model="templateForm.expireMinutes" :min="1" :max="1440" placeholder="模板变量 expireMinutes" />
                        </el-form-item>
                        <el-form-item>
                            <el-button
                                type="primary"
                                :loading="sending"
                                @click="handleTemplateSend"
                                v-if="routerStore.hasPermission('email:send')"
                            >
                                <el-icon><Promotion /></el-icon> 发送
                            </el-button>
                        </el-form-item>
                    </el-form>
                </el-tab-pane>

                <!-- ==================== 批量群发 ==================== -->
                <el-tab-pane label="批量群发" name="batch">
                    <div class="mt-4 flex flex-col gap-4">
                        <el-alert
                            title="每行一封独立邮件，支持新增多行批量发送"
                            type="info"
                            :closable="false"
                            show-icon
                        />
                        <el-table :data="batchList" border>
                            <el-table-column label="收件人" min-width="200">
                                <template #default="{ row }">
                                    <el-input v-model="row.to" placeholder="收件人邮箱，多个用逗号分隔" />
                                </template>
                            </el-table-column>
                            <el-table-column label="邮件主题" min-width="200">
                                <template #default="{ row }">
                                    <el-input v-model="row.subject" placeholder="邮件主题" />
                                </template>
                            </el-table-column>
                            <el-table-column label="邮件内容" min-width="280">
                                <template #default="{ row }">
                                    <el-input v-model="row.content" type="textarea" :rows="2" placeholder="邮件正文" />
                                </template>
                            </el-table-column>
                            <el-table-column label="HTML" width="80" align="center">
                                <template #default="{ row }">
                                    <el-switch v-model="row.html" />
                                </template>
                            </el-table-column>
                            <el-table-column label="操作" width="90" align="center">
                                <template #default="{ $index }">
                                    <el-button type="danger" link @click="handleRemoveBatchRow($index)">删除</el-button>
                                </template>
                            </el-table-column>
                        </el-table>
                        <div class="flex gap-4">
                            <el-button type="primary" plain @click="handleAddBatchRow">
                                <el-icon><Plus /></el-icon> 新增一行
                            </el-button>
                            <el-button
                                type="primary"
                                :loading="sending"
                                @click="handleBatchSend"
                                v-if="routerStore.hasPermission('email:batch')"
                            >
                                <el-icon><Promotion /></el-icon> 批量发送
                            </el-button>
                        </div>
                    </div>
                </el-tab-pane>
            </el-tabs>
        </el-card>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Promotion, Plus } from '@element-plus/icons-vue'
import api from '@/api/index.js'
import { useRouterStore } from '@/stores/modules/router.js'

const routerStore = useRouterStore()

const activeTab = ref('simple')
const sending = ref(false)

// ==================== 简单文本 ====================
const simpleFormRef = ref()
const simpleForm = reactive({
    to: '',
    toList: '',
    cc: '',
    subject: '',
    content: '',
})
const simpleRules = {
    to: [{ required: true, message: '请填写收件人或收件人列表', trigger: 'blur' }],
    subject: [{ required: true, message: '请输入邮件主题', trigger: 'blur' }],
    content: [{ required: true, message: '请输入邮件内容', trigger: 'blur' }],
}

// ==================== HTML 邮件 ====================
const htmlFormRef = ref()
const htmlForm = reactive({
    to: '',
    toList: '',
    cc: '',
    bcc: '',
    subject: '',
    content: '',
    attachments: '',
})
const htmlRules = {
    to: [{ required: true, message: '请填写收件人或收件人列表', trigger: 'blur' }],
    subject: [{ required: true, message: '请输入邮件主题', trigger: 'blur' }],
    content: [{ required: true, message: '请输入 HTML 内容', trigger: 'blur' }],
}

// ==================== 模板邮件 ====================
const templateFormRef = ref()
const templateForm = reactive({
    to: '',
    toList: '',
    subject: '',
    templateName: 'mail/welcome',
    username: '',
    code: '',
    link: '',
    expireMinutes: 30,
})
const templateRules = {
    to: [{ required: true, message: '请填写收件人或收件人列表', trigger: 'blur' }],
    subject: [{ required: true, message: '请输入邮件主题', trigger: 'blur' }],
    templateName: [{ required: true, message: '请选择模板', trigger: 'change' }],
}

// ==================== 批量群发 ====================
const batchList = ref([
    { to: '', subject: '', content: '', html: false },
])

// 逗号分隔字符串 -> 数组（过滤空项）
function parseList(str) {
    if (!str) return undefined
    const arr = str
        .split(',')
        .map((item) => item.trim())
        .filter((item) => item)
    return arr.length > 0 ? arr : undefined
}

// 组装 EmailRequest（兼容单个 to 与多收件人列表）
function buildRequest(form) {
    return {
        to: form.to || undefined,
        toList: parseList(form.toList),
        cc: parseList(form.cc),
        bcc: parseList(form.bcc),
        subject: form.subject,
        content: form.content,
        attachments: parseList(form.attachments),
    }
}

async function handleSimpleSend() {
    const valid = await simpleFormRef.value.validate().catch(() => false)
    if (!valid) return
    sending.value = true
    try {
        const res = await api.email.sendSimpleEmail(buildRequest(simpleForm))
        ElMessage.success(res.data?.msg || '发送成功')
        simpleFormRef.value.resetFields()
    } catch (error) {
        console.error('发送简单邮件失败', error)
    } finally {
        sending.value = false
    }
}

async function handleHtmlSend() {
    const valid = await htmlFormRef.value.validate().catch(() => false)
    if (!valid) return
    sending.value = true
    try {
        const res = await api.email.sendHtmlEmail(buildRequest(htmlForm))
        ElMessage.success(res.data?.msg || '发送成功')
        htmlFormRef.value.resetFields()
    } catch (error) {
        console.error('发送 HTML 邮件失败', error)
    } finally {
        sending.value = false
    }
}

async function handleTemplateSend() {
    const valid = await templateFormRef.value.validate().catch(() => false)
    if (!valid) return
    sending.value = true
    try {
        const params = {
            to: templateForm.to || undefined,
            toList: parseList(templateForm.toList),
            subject: templateForm.subject,
            templateName: templateForm.templateName,
            variables: {
                username: templateForm.username,
                code: templateForm.code,
                link: templateForm.link,
                expireMinutes: templateForm.expireMinutes,
            },
        }
        const res = await api.email.sendTemplateEmail(params)
        ElMessage.success(res.data?.msg || '发送成功')
        templateFormRef.value.resetFields()
    } catch (error) {
        console.error('发送模板邮件失败', error)
    } finally {
        sending.value = false
    }
}

function handleAddBatchRow() {
    batchList.value.push({ to: '', subject: '', content: '', html: false })
}

function handleRemoveBatchRow(index) {
    batchList.value.splice(index, 1)
}

async function handleBatchSend() {
    const list = batchList.value.filter((row) => row.to && row.subject && row.content)
    if (list.length === 0) {
        ElMessage.warning('请至少填写一行完整的收件人、主题和内容')
        return
    }
    sending.value = true
    try {
        const params = list.map((row) => ({
            to: row.to || undefined,
            subject: row.subject,
            content: row.content,
            html: row.html,
        }))
        const res = await api.email.sendBatchEmails(params)
        ElMessage.success(res.data?.msg || '发送成功')
        batchList.value = [{ to: '', subject: '', content: '', html: false }]
    } catch (error) {
        console.error('批量发送失败', error)
    } finally {
        sending.value = false
    }
}
</script>
