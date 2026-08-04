<template>
  <div class="dash">
    <div class="dash-head">
      <p class="dash-eyebrow">BÁO CÁO KINH DOANH</p>
      <h1 class="dash-title">Thống kê doanh số</h1>
      <p class="dash-desc">Doanh thu · đơn hàng · khách · tồn kho — shop bán áo TrendFit</p>
    </div>

    <div class="filter-bar">
      <div class="filter-group">
        <label>Nhóm biểu đồ</label>
        <div class="seg">
          <button
            v-for="opt in typeOptions"
            :key="opt.v"
            type="button"
            :class="{ on: filter.type === opt.v }"
            @click="filter.type = opt.v; loadDashboard()"
          >{{ opt.l }}</button>
        </div>
      </div>
      <div class="filter-group">
        <label>Từ ngày</label>
        <input type="date" v-model="filter.from" @change="loadDashboard" />
      </div>
      <div class="filter-group">
        <label>Đến ngày</label>
        <input type="date" v-model="filter.to" @change="loadDashboard" />
      </div>
      <button type="button" class="btn-filter" @click="loadDashboard">Lọc dữ liệu</button>
    </div>
    <p class="filter-note">
      Khoảng <b>{{ filter.from }}</b> → <b>{{ filter.to }}</b>
      · nhóm theo <b>{{ typeText }}</b>
      · doanh thu chỉ tính đơn <b>thành công</b>
    </p>

    <div v-if="loading" class="state-box">
      <div class="spinner"></div>
      <span>Đang tải thống kê...</span>
    </div>

    <template v-else>
      <!-- TÀI CHÍNH — đậm -->
      <section class="section">
        <h2 class="section-title">Tài chính</h2>
        <div class="kpi-row">
          <article class="kpi k-indigo">
            <p class="kpi-label">Doanh thu</p>
            <p class="kpi-val">{{ formatMoney(dashboard.totalRevenue) }}</p>
            <p class="kpi-hint">Đơn thành công</p>
          </article>
          <article class="kpi k-amber">
            <p class="kpi-label">Tiền vốn</p>
            <p class="kpi-val">{{ formatMoney(dashboard.totalImportCost) }}</p>
            <p class="kpi-hint">Chỉ SP đã nhập giá vốn</p>
          </article>
          <article class="kpi k-green">
            <p class="kpi-label">Lợi nhuận gộp</p>
            <p class="kpi-val">{{ formatMoney(dashboard.grossProfit) }}</p>
            <p class="kpi-hint">Doanh thu − vốn</p>
          </article>
          <article class="kpi k-sky">
            <p class="kpi-label">Tỷ suất lãi</p>
            <p class="kpi-val">{{ formatPercent(dashboard.profitRate) }}</p>
            <p class="kpi-hint">Lãi / doanh thu</p>
          </article>
          <article class="kpi k-violet">
            <p class="kpi-label">Giá trị đơn TB</p>
            <p class="kpi-val">{{ formatMoney(dashboard.avgOrderValue) }}</p>
            <p class="kpi-hint">AOV</p>
          </article>
        </div>
      </section>

      <!-- VẬN HÀNH — đậm -->
      <section class="section">
        <h2 class="section-title">Vận hành</h2>
        <div class="ops-row">
          <div class="ops-card">
            <span class="ops-n">{{ dashboard.totalOrders }}</span>
            <span class="ops-l">Tổng đơn</span>
          </div>
          <div class="ops-card ok">
            <span class="ops-n">{{ dashboard.totalSuccessOrders }}</span>
            <span class="ops-l">Thành công · {{ successRate }}%</span>
          </div>
          <div class="ops-card wait">
            <span class="ops-n">{{ dashboard.totalPendingOrders }}</span>
            <span class="ops-l">Đang xử lý</span>
          </div>
          <div class="ops-card bad">
            <span class="ops-n">{{ dashboard.totalFailedOrders }}</span>
            <span class="ops-l">Hủy / thất bại</span>
          </div>
          <div class="ops-card">
            <span class="ops-n">{{ dashboard.totalCustomers }}</span>
            <span class="ops-l">Khách có đơn</span>
          </div>
          <div class="ops-card">
            <span class="ops-n">{{ dashboard.totalProductsSold }}</span>
            <span class="ops-l">Áo đã bán (SL)</span>
          </div>
          <div class="ops-card">
            <span class="ops-n">{{ dashboard.totalStock }}</span>
            <span class="ops-l">Tồn kho hiện tại</span>
          </div>
        </div>
      </section>

      <div class="charts">
        <!-- Doanh số -->
        <section class="card chart-main">
          <header class="card-h">
            <div>
              <h3>Doanh số theo {{ typeText }}</h3>
              <p>Chỉ đơn thành công</p>
            </div>
          </header>
          <div v-if="!dashboard.revenueChart.length" class="empty">Chưa có dữ liệu doanh số</div>
          <div v-else class="bars">
            <div
              v-for="(item, i) in dashboard.revenueChart"
              :key="item.label + '-' + i"
              class="bar-row"
            >
              <div class="bar-meta">
                <span class="bar-rank">{{ i + 1 }}</span>
                <div>
                  <div class="bar-label">{{ item.label }}</div>
                  <div class="bar-sub">{{ item.orderCount || 0 }} đơn · {{ getRevenueShare(item.revenue) }}</div>
                </div>
              </div>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: getRevenuePercent(item.revenue) + '%' }"></div>
              </div>
              <div class="bar-money">{{ formatMoney(item.revenue) }}</div>
            </div>
          </div>
        </section>

        <!-- Trạng thái — 3 nhóm rõ ràng -->
        <section class="card chart-side">
          <header class="card-h">
            <div>
              <h3>Trạng thái đơn hàng</h3>
              <p>Gộp nhóm · tổng = {{ totalStatusOrders }} đơn</p>
            </div>
          </header>
          <div v-if="!statusItems.length" class="empty">Chưa có dữ liệu</div>
          <div v-else class="status-box">
            <div class="donut" :style="pieStyle">
              <div class="donut-core">
                <strong>{{ totalStatusOrders }}</strong>
                <span>đơn</span>
              </div>
            </div>
            <ul class="legend">
              <li v-for="s in statusItems" :key="s.status">
                <span class="lg-dot" :style="{ background: s.color }"></span>
                <div class="lg-body">
                  <div class="lg-top">
                    <span class="lg-name">{{ s.label }}</span>
                    <span class="lg-count">{{ s.count }}</span>
                    <span class="lg-pct">{{ s.percent }}%</span>
                  </div>
                  <div class="lg-track">
                    <div class="lg-fill" :style="{ width: s.percent + '%', background: s.color }"></div>
                  </div>
                </div>
              </li>
            </ul>
            <p class="match" :class="{ warn: statusMismatch }">
              {{ statusMismatch ? 'Legend lệch API — kiểm tra DB' : 'Khớp tổng đơn' }}
            </p>
          </div>
        </section>
      </div>

      <!-- Top SP -->
      <section class="card">
        <header class="card-h">
          <div>
            <h3>Top sản phẩm bán chạy</h3>
            <p>Chỉ sản phẩm còn trong kho và đã nhập giá vốn</p>
          </div>
        </header>
        <div class="table-scroll">
          <table class="tbl">
            <thead>
              <tr>
                <th>STT</th>
                <th>Sản phẩm</th>
                <th>SL bán</th>
                <th>Doanh thu</th>
                <th>Vốn</th>
                <th>Lãi</th>
                <th>Tỷ suất</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, i) in topProductsFiltered" :key="i">
                <td><span class="rank" :class="'r' + Math.min(i + 1, 3)">{{ i + 1 }}</span></td>
                <td>
                  <div class="prod-name">{{ item.productName }}</div>
                </td>
                <td class="num">{{ item.quantitySold }}</td>
                <td class="num">{{ formatMoney(item.revenue) }}</td>
                <td class="num">
                  <template v-if="hasCost(item)">{{ formatMoney(item.importCost) }}</template>
                  <span v-else class="muted" title="Không khớp biến thể / chưa nhập giá vốn">—</span>
                </td>
                <td class="num">
                  <template v-if="hasCost(item)">
                    <span :class="Number(item.profit) >= 0 ? 'pos' : 'neg'">{{ formatMoney(item.profit) }}</span>
                  </template>
                  <span v-else class="muted">—</span>
                </td>
                <td class="num">
                  <template v-if="hasCost(item)">
                    {{ formatPercent(item.margin != null ? item.margin : calcRate(item.profit, item.revenue)) }}
                  </template>
                  <span v-else class="muted">—</span>
                </td>
              </tr>
              <tr v-if="!topProductsFiltered.length">
                <td colspan="7" class="empty-td">Chưa có sản phẩm bán chạy trong khoảng này</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
/**
 * NOTE NHÓM — Dashboard UI
 * - Trạng thái gộp 3 nhóm: Thành công / Đang xử lý / Đã hủy (dễ đọc, khớp KPI)
 * - Top SP: tên lấy từ chi_tiet_don_hang (snapshot lúc bán) — có thể khác tên SP hiện tại
 * - Vốn/lãi chỉ khi costKnown (có gia_nhap)
 */
import { computed, onMounted, ref } from 'vue'
import axios from 'axios'
import { getAuthHeaders } from '@/utils/adminAuth'

const loading = ref(false)
const typeOptions = [
  { v: 'day', l: 'Ngày' },
  { v: 'month', l: 'Tháng' },
  { v: 'year', l: 'Năm' },
]
const filter = ref({ type: 'month', from: '2026-01-01', to: '2026-12-31' })

const emptyDash = () => ({
  totalRevenue: 0, totalImportCost: 0, grossProfit: 0, profitRate: 0, avgOrderValue: 0,
  totalSuccessOrders: 0, totalFailedOrders: 0, totalOrders: 0, totalPendingOrders: 0,
  totalCustomers: 0, totalProductsSold: 0, totalStock: 0,
  revenueChart: [], orderStatusChart: [], topProducts: [],
})
const dashboard = ref(emptyDash())

/** 3 màu cố định — không rối nhiều tone xanh */
const colorMap = {
  THANH_CONG: '#16a34a',
  DANG_XU_LY: '#f59e0b',
  HUY: '#ef4444',
  KHAC: '#94a3b8',
}

const loadDashboard = async () => {
  loading.value = true
  try {
    if (filter.value.from && filter.value.to && filter.value.from > filter.value.to) {
      const t = filter.value.from; filter.value.from = filter.value.to; filter.value.to = t
    }
    const res = await axios.get('http://localhost:8080/api/admin/analytics/dashboard', {
      params: { type: filter.value.type, from: filter.value.from, to: filter.value.to },
      headers: getAuthHeaders(),
    })
    const d = res.data || {}
    dashboard.value = {
      totalRevenue: d.totalRevenue || 0,
      totalImportCost: d.totalImportCost || 0,
      grossProfit: d.grossProfit || 0,
      profitRate: d.profitRate || 0,
      avgOrderValue: d.avgOrderValue || 0,
      totalSuccessOrders: Number(d.totalSuccessOrders || 0),
      totalFailedOrders: Number(d.totalFailedOrders || 0),
      totalOrders: Number(d.totalOrders || 0),
      totalPendingOrders: Number(d.totalPendingOrders || 0),
      totalCustomers: Number(d.totalCustomers || 0),
      totalProductsSold: Number(d.totalProductsSold || 0),
      totalStock: Number(d.totalStock || 0),
      revenueChart: Array.isArray(d.revenueChart) ? d.revenueChart : [],
      orderStatusChart: Array.isArray(d.orderStatusChart) ? d.orderStatusChart : [],
      topProducts: Array.isArray(d.topProducts) ? d.topProducts : [],
    }
  } catch (e) {
    console.error(e)
    alert(e.response?.data?.message || e.response?.data || e.message || 'Không tải được thống kê')
    dashboard.value = emptyDash()
  } finally {
    loading.value = false
  }
}

const typeText = computed(() =>
  filter.value.type === 'day' ? 'ngày' : filter.value.type === 'year' ? 'năm' : 'tháng'
)
const successRate = computed(() => {
  const t = dashboard.value.totalOrders
  return t ? ((dashboard.value.totalSuccessOrders / t) * 100).toFixed(1) : '0.0'
})
const maxRevenue = computed(() =>
  Math.max(...dashboard.value.revenueChart.map((x) => Number(x.revenue || 0)), 0)
)
const sumRevenueChart = computed(() =>
  dashboard.value.revenueChart.reduce((s, x) => s + Number(x.revenue || 0), 0)
)
const totalStatusOrders = computed(() =>
  dashboard.value.orderStatusChart.reduce((s, x) => s + Number(x.count || 0), 0)
)
const statusMismatch = computed(() => {
  const api = dashboard.value.totalOrders
  return api > 0 && api !== totalStatusOrders.value
})
const statusItems = computed(() => {
  const total = totalStatusOrders.value
  return dashboard.value.orderStatusChart.map((item) => {
    const count = Number(item.count || 0)
    const status = String(item.status || '').toUpperCase()
    return {
      status,
      label: item.statusLabel || item.status || 'Khác',
      count,
      percent: total ? Number(((count / total) * 100).toFixed(1)) : 0,
      color: colorMap[status] || '#94a3b8',
    }
  })
})
const pieStyle = computed(() => {
  const total = totalStatusOrders.value
  if (!statusItems.value.length || !total) return { background: '#e2e8f0' }
  let start = 0
  const segs = statusItems.value.map((s) => {
    const end = start + (s.count / total) * 100
    const seg = `${s.color} ${start}% ${end}%`
    start = end
    return seg
  })
  return { background: `conic-gradient(${segs.join(', ')})` }
})

const getRevenuePercent = (v) =>
  maxRevenue.value ? ((Number(v || 0) / maxRevenue.value) * 100).toFixed(1) : 0
const getRevenueShare = (v) =>
  sumRevenueChart.value
    ? ((Number(v || 0) / sumRevenueChart.value) * 100).toFixed(1) + '%'
    : '0%'
const calcRate = (p, r) => {
  const rv = Number(r || 0)
  return rv ? (Number(p || 0) / rv) * 100 : 0
}
/** Chỉ hiện SP có giá nhập — ẩn snapshot đơn cũ / SP đã xóa (vốn null) */
const topProductsFiltered = computed(() =>
  (dashboard.value.topProducts || []).filter((item) => hasCost(item))
)
const hasCost = (item) => {
  if (!item) return false
  if (item.costKnown === true || item.costKnown === 'true' || item.costKnown === 1) return true
  if (item.costKnown === false || item.costKnown === 'false' || item.costKnown === 0) return false
  return Number(item.importCost) > 0
}
const formatPercent = (v) => Number(v || 0).toFixed(2) + '%'
const formatMoney = (v) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(v || 0))

onMounted(loadDashboard)
</script>

<style scoped>
.dash {
  font-family: 'Inter', system-ui, sans-serif;
  -webkit-font-smoothing: antialiased;
  color: #0f172a;
  max-width: 1180px;
}
.dash-head { margin-bottom: 18px; }
.dash-eyebrow {
  margin: 0 0 6px; font-size: 11px; font-weight: 800;
  letter-spacing: 0.1em; color: #7c3aed;
}
.dash-title {
  margin: 0 0 6px;
  font-family: 'Space Grotesk', 'Inter', sans-serif;
  font-size: 1.7rem; font-weight: 700; letter-spacing: -0.03em;
}
.dash-desc { margin: 0; font-size: 14px; color: #64748b; }

.filter-bar {
  display: flex; flex-wrap: wrap; gap: 14px; align-items: flex-end;
  background: #fff; border: 1px solid #e8eaf0; border-radius: 16px;
  padding: 16px 18px; box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}
.filter-group { display: flex; flex-direction: column; gap: 6px; }
.filter-group label {
  font-size: 11px; font-weight: 800; letter-spacing: 0.05em;
  text-transform: uppercase; color: #94a3b8;
}
.filter-group input[type="date"] {
  padding: 10px 12px; border: 1.5px solid #e2e8f0; border-radius: 10px;
  font-size: 14px; font-weight: 600; font-family: inherit; color: #0f172a;
  background: #fafbff; min-width: 150px;
}
.filter-group input:focus {
  outline: none; border-color: #a78bfa;
  box-shadow: 0 0 0 3px rgba(124, 58, 237, 0.12);
}
.seg {
  display: inline-flex; background: #f1f5f9; border-radius: 10px; padding: 3px; gap: 2px;
}
.seg button {
  border: none; background: transparent; padding: 8px 14px; border-radius: 8px;
  font-size: 13px; font-weight: 700; color: #64748b; cursor: pointer; font-family: inherit;
}
.seg button.on {
  background: #fff; color: #7c3aed;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
}
.btn-filter {
  border: none; padding: 11px 18px; border-radius: 10px;
  background: linear-gradient(135deg, #7c3aed, #6366f1);
  color: #fff; font-weight: 800; font-size: 13px; cursor: pointer;
  font-family: inherit; box-shadow: 0 6px 16px rgba(124, 58, 237, 0.28);
}
.filter-note {
  margin: 10px 0 20px; font-size: 12.5px; color: #94a3b8;
}
.filter-note b { color: #475569; font-weight: 700; }

.state-box {
  display: flex; align-items: center; justify-content: center; gap: 12px;
  padding: 48px; color: #64748b; font-weight: 700;
}
.spinner {
  width: 22px; height: 22px; border: 2.5px solid #e2e8f0; border-top-color: #7c3aed;
  border-radius: 50%; animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.section { margin-bottom: 22px; }
.section-title {
  margin: 0 0 12px;
  font-size: 12px; font-weight: 800; letter-spacing: 0.08em;
  text-transform: uppercase; color: #64748b;
}

/* KPI đậm */
.kpi-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}
.kpi {
  background: #fff;
  border: 1px solid #e8eaf0;
  border-radius: 16px;
  padding: 18px 16px 16px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  border-top: 4px solid #cbd5e1;
}
.k-indigo { border-top-color: #7c3aed; }
.k-amber { border-top-color: #f59e0b; }
.k-green { border-top-color: #16a34a; }
.k-sky { border-top-color: #0ea5e9; }
.k-violet { border-top-color: #9333ea; }

.kpi-label {
  margin: 0 0 8px;
  font-size: 12px; font-weight: 800;
  letter-spacing: 0.04em; text-transform: uppercase;
  color: #64748b;
}
.kpi-val {
  margin: 0;
  font-family: 'Space Grotesk', 'Inter', sans-serif;
  font-size: 1.35rem; font-weight: 700;
  color: #0f172a; line-height: 1.2;
  letter-spacing: -0.02em;
  word-break: break-word;
}
.kpi-hint {
  margin: 8px 0 0;
  font-size: 12px; font-weight: 600; color: #94a3b8;
}

/* Ops đậm */
.ops-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 10px;
}
.ops-card {
  background: #fff;
  border: 1px solid #e8eaf0;
  border-radius: 14px;
  padding: 16px 10px;
  text-align: center;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03);
}
.ops-n {
  display: block;
  font-family: 'Space Grotesk', sans-serif;
  font-size: 1.55rem; font-weight: 700;
  color: #0f172a; line-height: 1.15;
  letter-spacing: -0.02em;
}
.ops-l {
  display: block; margin-top: 6px;
  font-size: 12px; font-weight: 700; color: #64748b;
}
.ops-card.ok .ops-n { color: #15803d; }
.ops-card.wait .ops-n { color: #b45309; }
.ops-card.bad .ops-n { color: #b91c1c; }

.charts {
  display: grid;
  grid-template-columns: 1.45fr 1fr;
  gap: 14px;
  margin-bottom: 14px;
}
.card {
  background: #fff; border: 1px solid #e8eaf0; border-radius: 16px;
  padding: 20px; box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  margin-bottom: 14px;
}
.card-h { margin-bottom: 16px; }
.card-h h3 {
  margin: 0 0 4px;
  font-family: 'Space Grotesk', sans-serif;
  font-size: 1.08rem; font-weight: 700;
}
.card-h p { margin: 0; font-size: 13px; color: #94a3b8; font-weight: 500; }
.card-h code {
  font-size: 11.5px; background: #f1f5f9; padding: 1px 5px; border-radius: 4px;
}
.empty {
  padding: 36px; text-align: center; color: #94a3b8; font-weight: 700; font-size: 14px;
}

.bars { display: flex; flex-direction: column; gap: 14px; }
.bar-row {
  display: grid;
  grid-template-columns: minmax(130px, 1.15fr) 1.6fr auto;
  gap: 12px; align-items: center;
}
.bar-meta { display: flex; gap: 10px; align-items: center; min-width: 0; }
.bar-rank {
  width: 26px; height: 26px; border-radius: 8px; background: #f1f5f9;
  display: grid; place-items: center; font-size: 11px; font-weight: 800; color: #64748b;
  flex-shrink: 0;
}
.bar-label { font-size: 13.5px; font-weight: 800; color: #0f172a; }
.bar-sub { font-size: 11.5px; color: #94a3b8; margin-top: 1px; font-weight: 600; }
.bar-track { height: 10px; background: #f1f5f9; border-radius: 99px; overflow: hidden; }
.bar-fill {
  height: 100%; border-radius: 99px;
  background: linear-gradient(90deg, #7c3aed, #a78bfa);
}
.bar-money {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 14px; font-weight: 700; white-space: nowrap;
}

.status-box { display: flex; flex-direction: column; align-items: center; gap: 18px; }
.donut {
  width: 140px; height: 140px; border-radius: 50%;
  display: grid; place-items: center;
}
.donut-core {
  width: 88px; height: 88px; border-radius: 50%; background: #fff;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  box-shadow: inset 0 0 0 1px #e8eaf0;
}
.donut-core strong {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 1.5rem; font-weight: 700; line-height: 1;
}
.donut-core span { font-size: 11px; color: #94a3b8; font-weight: 700; margin-top: 2px; }

.legend { list-style: none; margin: 0; padding: 0; width: 100%; }
.legend li {
  display: flex; gap: 10px; align-items: flex-start;
  margin-bottom: 14px;
}
.lg-dot {
  width: 12px; height: 12px; border-radius: 50%; margin-top: 4px; flex-shrink: 0;
}
.lg-body { flex: 1; min-width: 0; }
.lg-top {
  display: flex; align-items: baseline; gap: 8px; margin-bottom: 5px;
}
.lg-name { flex: 1; font-weight: 700; font-size: 13.5px; color: #0f172a; }
.lg-count { font-weight: 800; font-size: 14px; color: #0f172a; }
.lg-pct { font-size: 12px; font-weight: 700; color: #94a3b8; min-width: 42px; text-align: right; }
.lg-track { height: 6px; background: #f1f5f9; border-radius: 99px; overflow: hidden; }
.lg-fill { height: 100%; border-radius: 99px; }
.match {
  margin: 0; font-size: 12px; color: #64748b; font-weight: 600; text-align: center;
}
.match.warn { color: #b45309; font-weight: 800; }

.table-scroll { overflow-x: auto; }
.tbl {
  width: 100%; border-collapse: separate; border-spacing: 0; font-size: 13.5px;
}
.tbl th {
  text-align: left; padding: 12px 14px;
  font-size: 11px; font-weight: 800; letter-spacing: 0.05em;
  text-transform: uppercase; color: #94a3b8;
  background: #f8fafc; border-bottom: 1px solid #e8eaf0;
}
.tbl td {
  padding: 14px; border-bottom: 1px solid #f1f5f9;
  vertical-align: middle; color: #334155; font-weight: 600;
}
.tbl tbody tr:hover td { background: #fafbff; }
.tbl tbody tr:last-child td { border-bottom: none; }
.num {
  font-family: 'Space Grotesk', 'Inter', sans-serif;
  font-weight: 700; white-space: nowrap;
}
.rank {
  display: inline-grid; place-items: center;
  width: 28px; height: 28px; border-radius: 8px;
  background: #f1f5f9; font-size: 12px; font-weight: 800; color: #64748b;
}
.rank.r1 { background: #ede9fe; color: #6d28d9; }
.rank.r2 { background: #e0e7ff; color: #4338ca; }
.rank.r3 { background: #f3e8ff; color: #7e22ce; }
.prod-name { font-weight: 700; color: #0f172a; line-height: 1.35; }
.pos { color: #15803d; font-weight: 800; }
.neg { color: #b91c1c; font-weight: 800; }
.muted { color: #cbd5e1; font-weight: 700; }
.empty-td { text-align: center; color: #94a3b8; padding: 28px !important; }

@media (max-width: 1100px) {
  .kpi-row { grid-template-columns: repeat(3, 1fr); }
  .ops-row { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 900px) {
  .charts { grid-template-columns: 1fr; }
  .kpi-row { grid-template-columns: repeat(2, 1fr); }
  .ops-row { grid-template-columns: repeat(3, 1fr); }
  .bar-row { grid-template-columns: 1fr; gap: 6px; }
}
</style>
