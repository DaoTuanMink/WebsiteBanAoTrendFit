<template>
  <div class="sales-page">
    <div class="hero-section">
      <span class="page-tag">BÁO CÁO KINH DOANH</span>

      <h1>Thống kê doanh số</h1>

      <p>
        Theo dõi doanh thu, hiệu quả xử lý đơn hàng và sản phẩm bán chạy của cửa hàng TrendFit
      </p>
    </div>

    <div class="filter-box">
      <select v-model="filter.type" @change="loadDashboard">
        <option value="day">Theo ngày</option>
        <option value="month">Theo tháng</option>
        <option value="year">Theo năm</option>
      </select>

      <input type="date" v-model="filter.from" @change="loadDashboard" />
      <input type="date" v-model="filter.to" @change="loadDashboard" />

      <button @click="loadDashboard">Lọc dữ liệu</button>
    </div>

    <div v-if="loading" class="loading-box">
      Đang tải dữ liệu thống kê...
    </div>

    <template v-else>
      <div class="summary-grid">
        <div class="summary-card revenue">
          <p>Doanh số đạt được</p>
          <h2>{{ formatMoney(dashboard.totalRevenue) }}</h2>
          <span>Tổng doanh thu trong thời gian đã chọn</span>
        </div>

        <div class="summary-card success">
          <p>Đơn hàng thành công</p>
          <h2>{{ dashboard.totalSuccessOrders }}</h2>
          <span>Số đơn đã giao / hoàn thành</span>
        </div>

        <div class="summary-card failed">
          <p>Đơn hàng thất bại / đã hủy</p>
          <h2>{{ dashboard.totalFailedOrders }}</h2>
          <span>Số đơn bị hủy hoặc không thành công</span>
        </div>

        <div class="summary-card rate">
          <p>Tỷ lệ thành công</p>
          <h2>{{ successRate }}%</h2>
          <span>Hiệu quả xử lý đơn hàng</span>
        </div>
      </div>

      <div class="panel-grid">
        <div class="panel panel-large">
          <div class="panel-header">
            <h3>Doanh số theo thời gian</h3>
            <p>Biểu diễn doanh thu theo {{ typeText }}</p>
          </div>

          <div v-if="dashboard.revenueChart.length === 0" class="empty-box">
            Chưa có dữ liệu doanh số
          </div>

          <div v-else class="revenue-list">
            <div
              v-for="(item, index) in dashboard.revenueChart"
              :key="item.label"
              class="revenue-item"
            >
              <div class="revenue-left">
                <div class="rank-badge">{{ index + 1 }}</div>

                <div>
                  <div class="period-label">{{ item.label }}</div>
                  <div class="period-sub">
                    Chiếm {{ getRevenueShare(item.revenue) }} tổng doanh số
                  </div>
                </div>
              </div>

              <div class="revenue-center">
                <div class="bar-track">
                  <div
                    class="bar-fill"
                    :style="{ width: getRevenuePercent(item.revenue) + '%' }"
                  ></div>
                </div>
              </div>

              <div class="revenue-right">
                {{ formatMoney(item.revenue) }}
              </div>
            </div>
          </div>
        </div>

        <div class="panel panel-small">
          <div class="panel-header">
            <h3>Tỷ trọng trạng thái đơn hàng</h3>
            <p>Biểu đồ tròn thể hiện tỷ lệ các trạng thái đơn hàng</p>
          </div>

          <div v-if="dashboard.orderStatusChart.length === 0" class="empty-box">
            Chưa có dữ liệu trạng thái đơn hàng
          </div>

          <div v-else class="pie-section">
            <div class="donut-chart" :style="pieChartStyle">
              <div class="donut-center">
                <strong>{{ totalStatusOrders }}</strong>
                <span>đơn hàng</span>
              </div>
            </div>

            <div class="status-list">
              <div
                v-for="item in statusItems"
                :key="item.label"
                class="status-item"
              >
                <div class="status-main">
                  <div class="status-name">
                    <span
                      class="status-dot"
                      :style="{ backgroundColor: item.color }"
                    ></span>
                    <span>{{ item.label }}</span>
                  </div>

                  <strong>{{ item.count }} đơn</strong>
                </div>

                <div class="status-track">
                  <div
                    class="status-fill"
                    :style="{
                      width: item.percent + '%',
                      backgroundColor: item.color
                    }"
                  ></div>
                </div>

                <div class="status-percent">
                  {{ item.percent }}%
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-header">
          <h3>Top 5 sản phẩm bán chạy nhất</h3>
          <p>Những sản phẩm có số lượng bán cao nhất trong thời gian đã chọn</p>
        </div>

        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>STT</th>
                <th>Tên sản phẩm</th>
                <th>Số lượng bán</th>
                <th>Doanh thu</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="(item, index) in dashboard.topProducts" :key="index">
                <td>{{ index + 1 }}</td>
                <td>{{ item.productName }}</td>
                <td>{{ item.quantitySold }}</td>
                <td>{{ formatMoney(item.revenue) }}</td>
              </tr>

              <tr v-if="dashboard.topProducts.length === 0">
                <td colspan="4" class="empty-table">
                  Chưa có dữ liệu sản phẩm bán chạy
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import axios from 'axios'

const loading = ref(false)

const filter = ref({
  type: 'month',
  from: '2026-01-01',
  to: '2026-12-31',
})

const dashboard = ref({
  totalRevenue: 0,
  totalSuccessOrders: 0,
  totalFailedOrders: 0,
  revenueChart: [],
  orderStatusChart: [],
  topProducts: [],
})

const statusColors = [
  '#16a34a',
  '#dc2626',
  '#2563eb',
  '#f59e0b',
  '#7c3aed',
  '#0f172a',
]

const loadDashboard = async () => {
  try {
    loading.value = true

    const res = await axios.get('http://localhost:8080/api/admin/analytics/dashboard', {
      params: {
        type: filter.value.type,
        from: filter.value.from,
        to: filter.value.to,
      },
    })

    dashboard.value = {
      totalRevenue: res.data.totalRevenue || 0,
      totalSuccessOrders: res.data.totalSuccessOrders || 0,
      totalFailedOrders: res.data.totalFailedOrders || 0,
      revenueChart: res.data.revenueChart || [],
      orderStatusChart: res.data.orderStatusChart || [],
      topProducts: res.data.topProducts || [],
    }
  } catch (error) {
    console.error('Không tải được dữ liệu dashboard:', error)
  } finally {
    loading.value = false
  }
}

const successRate = computed(() => {
  const success = Number(dashboard.value.totalSuccessOrders || 0)
  const failed = Number(dashboard.value.totalFailedOrders || 0)
  const total = success + failed

  if (total === 0) {
    return 0
  }

  return ((success / total) * 100).toFixed(1)
})

const typeText = computed(() => {
  if (filter.value.type === 'day') {
    return 'ngày'
  }

  if (filter.value.type === 'year') {
    return 'năm'
  }

  return 'tháng'
})

const maxRevenue = computed(() => {
  const revenues = dashboard.value.revenueChart.map((item) => Number(item.revenue || 0))
  return Math.max(...revenues, 0)
})

const totalRevenueValue = computed(() => {
  return dashboard.value.revenueChart.reduce((sum, item) => {
    return sum + Number(item.revenue || 0)
  }, 0)
})

const totalStatusOrders = computed(() => {
  return dashboard.value.orderStatusChart.reduce((sum, item) => {
    return sum + Number(item.count || 0)
  }, 0)
})

const statusItems = computed(() => {
  return dashboard.value.orderStatusChart.map((item, index) => {
    const count = Number(item.count || 0)
    const percent =
      totalStatusOrders.value === 0
        ? 0
        : Number(((count / totalStatusOrders.value) * 100).toFixed(1))

    return {
      label: item.statusLabel || item.status || 'Không xác định',
      count,
      percent,
      color: statusColors[index % statusColors.length],
    }
  })
})

const pieChartStyle = computed(() => {
  if (statusItems.value.length === 0 || totalStatusOrders.value === 0) {
    return {
      background: '#e5e7eb',
    }
  }

  let start = 0

  const segments = statusItems.value.map((item) => {
    const end = start + item.percent
    const segment = `${item.color} ${start}% ${end}%`
    start = end
    return segment
  })

  return {
    background: `conic-gradient(${segments.join(', ')})`,
  }
})

const getRevenuePercent = (value) => {
  if (maxRevenue.value === 0) {
    return 0
  }

  return ((Number(value || 0) / maxRevenue.value) * 100).toFixed(1)
}

const getRevenueShare = (value) => {
  if (totalRevenueValue.value === 0) {
    return '0%'
  }

  return ((Number(value || 0) / totalRevenueValue.value) * 100).toFixed(1) + '%'
}

const formatMoney = (value) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(Number(value || 0))
}

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped>
.sales-page {
  min-height: 100vh;
  padding: 28px;
  background: #f4f6fb;
  color: #0f172a;
}

.hero-section {
  margin-bottom: 20px;
}

.page-tag {
  display: inline-block;
  padding: 6px 12px;
  background: #e8eefc;
  color: #1d4ed8;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  margin-bottom: 10px;
}

.hero-section h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
}

.hero-section p {
  margin-top: 8px;
  color: #64748b;
  font-size: 16px;
}

.filter-box {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 22px;
}

.filter-box select,
.filter-box input,
.filter-box button {
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid #dbe2ea;
  font-size: 15px;
  background: #ffffff;
}

.filter-box button {
  background: linear-gradient(135deg, #0f172a, #1e293b);
  color: white;
  font-weight: 600;
  cursor: pointer;
  border: none;
}

.loading-box {
  padding: 30px;
  border-radius: 18px;
  background: white;
  text-align: center;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.06);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  margin-bottom: 22px;
}

.summary-card {
  background: white;
  border-radius: 18px;
  padding: 22px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  border: 1px solid #eef2f7;
}

.summary-card p {
  margin: 0;
  color: #64748b;
  font-size: 15px;
}

.summary-card h2 {
  margin: 10px 0 8px;
  font-size: 22px;
  font-weight: 800;
}

.summary-card span {
  color: #94a3b8;
  font-size: 13px;
}

.summary-card.success h2 {
  color: #16a34a;
}

.summary-card.failed h2 {
  color: #dc2626;
}

.summary-card.rate h2 {
  color: #2563eb;
}

.panel-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 18px;
  margin-bottom: 22px;
}

.panel {
  background: white;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  border: 1px solid #eef2f7;
}

.panel-header {
  margin-bottom: 18px;
}

.panel-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
}

.panel-header p {
  margin-top: 6px;
  color: #64748b;
  font-size: 14px;
}

.empty-box {
  text-align: center;
  color: #94a3b8;
  padding: 32px 0;
}

.revenue-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.revenue-item {
  display: grid;
  grid-template-columns: 220px 1fr 160px;
  gap: 16px;
  align-items: center;
}

.revenue-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rank-badge {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #0f172a;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.period-label {
  font-weight: 700;
  font-size: 16px;
}

.period-sub {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

.bar-track {
  width: 100%;
  height: 16px;
  background: #e5e7eb;
  border-radius: 999px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #0f172a, #2563eb);
}

.revenue-right {
  text-align: right;
  font-weight: 700;
  color: #334155;
}

.pie-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.donut-chart {
  width: 220px;
  height: 220px;
  border-radius: 50%;
  position: relative;
  margin-bottom: 24px;
}

.donut-chart::after {
  content: '';
  position: absolute;
  inset: 48px;
  background: white;
  border-radius: 50%;
}

.donut-center {
  position: absolute;
  inset: 0;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.donut-center strong {
  font-size: 28px;
  font-weight: 800;
}

.donut-center span {
  color: #64748b;
  font-size: 13px;
}

.status-list {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-item {
  padding-bottom: 12px;
  border-bottom: 1px solid #edf2f7;
}

.status-main {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.status-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.status-track {
  height: 10px;
  border-radius: 999px;
  background: #e5e7eb;
  overflow: hidden;
}

.status-fill {
  height: 100%;
  border-radius: 999px;
}

.status-percent {
  margin-top: 6px;
  text-align: right;
  color: #64748b;
  font-size: 13px;
}

.table-wrapper {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead th {
  background: #f8fafc;
  color: #334155;
  font-weight: 700;
}

th,
td {
  padding: 14px 16px;
  text-align: left;
  border-bottom: 1px solid #edf2f7;
}

tbody tr:hover {
  background: #f8fbff;
}

.empty-table {
  text-align: center;
  color: #94a3b8;
  padding: 20px;
}

@media (max-width: 1200px) {
  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .panel-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .sales-page {
    padding: 18px;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .revenue-item {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .revenue-right {
    text-align: left;
  }
}
</style>