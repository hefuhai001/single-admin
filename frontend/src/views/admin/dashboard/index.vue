<template>
    <div class="h-full flex flex-col gap-5 p-1">
        <!-- 统计卡片 -->
        <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
            <StatCard
                title="用户总数"
                :value="overview.userCount ?? 0"
                :icon="User"
                color="#3b82f6"
                colorEnd="#60a5fa"
                :trend="overview.userTrend ?? 0"
            />
            <StatCard
                title="患者总数"
                :value="overview.patientCount ?? 0"
                :icon="UserFilled"
                color="#10b981"
                colorEnd="#34d399"
                :trend="overview.patientTrend ?? 0"
            />
            <StatCard
                title="问卷总数"
                :value="overview.questionnaireCount ?? 0"
                :icon="Document"
                color="#f59e0b"
                colorEnd="#fbbf24"
                :trend="overview.questionnaireTrend ?? 0"
            />
            <StatCard
                title="问卷答案数量"
                :value="overview.answerCount ?? 0"
                :icon="EditPen"
                color="#8b5cf6"
                colorEnd="#a78bfa"
                :trend="overview.answerTrend ?? 0"
            />
        </div>

        <!-- 图表区域 -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 flex-1 min-h-0">
            <UserStatsChart :data="userTrendData" />
            <PatientStatsChart :data="patientGenderData" />
            <QuestionnaireStatsChart :data="questionnaireStatusData" />
            <FileStatsChart :data="answerDistributionData" />
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, UserFilled, Document, EditPen } from '@element-plus/icons-vue'
import StatCard from './components/StatCard.vue'
import UserStatsChart from './components/UserStatsChart.vue'
import PatientStatsChart from './components/PatientStatsChart.vue'
import QuestionnaireStatsChart from './components/QuestionnaireStatsChart.vue'
import FileStatsChart from './components/FileStatsChart.vue'
import {
    getOverview,
    getUserTrend,
    getPatientGender,
    getQuestionnaireStatus,
    getAnswerDistribution,
} from '@/api/modules/stats.js'

const overview = ref({})
const userTrendData = ref([])
const patientGenderData = ref([])
const questionnaireStatusData = ref([])
const answerDistributionData = ref([])

async function loadDashboardData() {
    // const [overviewRes, userTrendRes, patientGenderRes, questionnaireStatusRes, answerDistRes] =
    //     await Promise.allSettled([
    //         getOverview(),
    //         getUserTrend(),
    //         getPatientGender(),
    //         getQuestionnaireStatus(),
    //         getAnswerDistribution(),
    //     ])
    // if (overviewRes.status === 'fulfilled') {
    //     overview.value = overviewRes.value.data?.data ?? {}
    // }
    // if (userTrendRes.status === 'fulfilled') {
    //     userTrendData.value = userTrendRes.value.data?.data ?? []
    // }
    // if (patientGenderRes.status === 'fulfilled') {
    //     patientGenderData.value = patientGenderRes.value.data?.data ?? []
    // }
    // if (questionnaireStatusRes.status === 'fulfilled') {
    //     questionnaireStatusData.value = questionnaireStatusRes.value.data?.data ?? []
    // }
    // if (answerDistRes.status === 'fulfilled') {
    //     answerDistributionData.value = answerDistRes.value.data?.data ?? []
    // }
}

onMounted(() => {
    loadDashboardData()
})
</script>
