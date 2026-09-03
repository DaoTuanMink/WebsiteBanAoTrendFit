<template>
  <LayoutHeader />
  <div class="container-fluid py-5 bg-light min-vh-100">
    <div class="container">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 class="fw-bold mb-1" style="font-family: 'Space Grotesk', sans-serif">
            Thống kê chi tiêu
          </h2>
          <p class="text-secondary mb-0">
            Theo dõi thói quen mua sắm và lịch sử đơn hàng của bạn tại TrendFit.
          </p>
        </div>
      </div>

      <!-- ===================== KPI CARDS (TỔNG QUAN) ===================== -->
      <div class="row g-4 mb-4">
        <!-- Tổng chi tiêu -->
        <div class="col-md-4">
          <div
            class="card border-0 shadow-sm rounded-4 h-100 bg-dark text-white overflow-hidden position-relative"
          >
            <div class="position-absolute top-0 end-0 p-3 opacity-25">
              <i class="ri-wallet-3-fill display-1"></i>
            </div>
            <div class="card-body p-4 position-relative z-1">
              <p class="text-white-50 small text-uppercase fw-bold letter-spacing-1 mb-2">
                Tổng tiền đã chi tiêu
              </p>
              <h2 class="display-6 fw-bold mb-0">{{ formatMoney(summary?.tongTienDaChi) }}</h2>
              <div class="mt-3 small">
                <span class="badge bg-white bg-opacity-25 text-white rounded-pill">
                  Thành viên VIP
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Số đơn hàng -->
        <div class="col-md-4">
          <div class="card border-0 shadow-sm rounded-4 h-100">
            <div class="card-body p-4 d-flex flex-column justify-content-center">
              <div class="d-flex align-items-center justify-content-between mb-2">
                <p class="text-muted small text-uppercase fw-bold mb-0">Đơn hàng thành công</p>
                <div
                  class="bg-primary bg-opacity-10 text-primary rounded-circle d-flex align-items-center justify-content-center"
                  style="width: 40px; height: 40px"
                >
                  <i class="ri-shopping-bag-3-fill fs-5"></i>
                </div>
              </div>
              <h2 class="fw-bold text-dark mb-0">
                {{ summary?.tongSoDonHang ?? 0 }} <span class="fs-6 text-muted fw-normal">đơn</span>
              </h2>
              <p class="text-secondary small mt-2 mb-0">
                Trung bình: <strong class="text-dark">{{ formatMoney(trungBinhMoiDon) }}</strong> /
                đơn
              </p>
            </div>
          </div>
        </div>

        <!-- Tổng sản phẩm -->
        <div class="col-md-4">
          <div class="card border-0 shadow-sm rounded-4 h-100">
            <div class="card-body p-4 d-flex flex-column justify-content-center">
              <div class="d-flex align-items-center justify-content-between mb-2">
                <p class="text-muted small text-uppercase fw-bold mb-0">Áo đã mua</p>
                <div
                  class="bg-success bg-opacity-10 text-success rounded-circle d-flex align-items-center justify-content-center"
                  style="width: 40px; height: 40px"
                >
                  <i class="ri-t-shirt-fill fs-5"></i>
                </div>
              </div>
              <h2 class="fw-bold text-dark mb-0">
                {{ shoppingStats.totalItems }}
                <span class="fs-6 text-muted fw-normal">sản phẩm</span>
              </h2>
              <p class="text-secondary small mt-2 mb-0">
                Từ <strong class="text-dark">{{ shoppingStats.brands.length }}</strong> thương hiệu
                khác nhau
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- ===================== PHÂN TÍCH CHI TIẾT ===================== -->
      <div class="row g-4 mb-5">
        <!-- Top Sản Phẩm -->
        <div class="col-lg-6">
          <div class="card border-0 shadow-sm rounded-4 h-100">
            <div class="card-header bg-white border-0 pt-4 pb-0 px-4">
              <h5 class="fw-bold mb-0">
                <i class="ri-fire-fill text-danger me-2"></i>Sản phẩm mua nhiều nhất
              </h5>
            </div>
            <div class="card-body p-4">
              <div
                v-if="!summary?.sanPhamMuaNhieuNhat?.length"
                class="text-muted text-center py-4 bg-light rounded-3"
              >
                Chưa có dữ liệu mua hàng.
              </div>
              <ul v-else class="list-group list-group-flush">
                <li
                  v-for="(sp, index) in summary.sanPhamMuaNhieuNhat.slice(0, 5)"
                  :key="sp.sanPhamId ?? sp.tenSanPham"
                  class="list-group-item px-0 py-3 border-bottom-dashed d-flex align-items-center justify-content-between border-0"
                >
                  <div class="d-flex align-items-center gap-3">
                    <div class="fw-bold text-secondary" style="width: 20px">#{{ index + 1 }}</div>
                    <div>
                      <h6 class="mb-1 fw-bold text-dark">{{ sp.tenSanPham }}</h6>
                      <small class="text-muted"
                        >Đã mua:
                        <strong class="text-primary">{{ sp.soLuongDaMua }}</strong> cái</small
                      >
                    </div>
                  </div>
                  <!-- Sửa lỗi hiển thị 0đ: Nếu Backend có trả tiền thì hiện, không thì chỉ hiện số lượng -->
                  <div class="text-end" v-if="sp.tongTienDaChi || sp.tongTien">
                    <div class="fw-bold text-danger">
                      {{ formatMoney(sp.tongTienDaChi || sp.tongTien) }}
                    </div>
                  </div>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Biểu đồ Phân bổ (Category & Brand) -->
        <div class="col-lg-6">
          <div class="card border-0 shadow-sm rounded-4 h-100">
            <div class="card-header bg-white border-0 pt-4 pb-0 px-4">
              <h5 class="fw-bold mb-0">
                <i class="ri-pie-chart-2-fill text-primary me-2"></i>Sở thích mua sắm
              </h5>
            </div>
            <div class="card-body p-4">
              <!-- Thống kê Thương hiệu -->
              <h6 class="fw-bold text-secondary small text-uppercase mb-3">
                Thương hiệu yêu thích
              </h6>
              <div v-if="shoppingStats.brands.length === 0" class="text-muted small mb-4">
                Chưa đủ dữ liệu phân tích.
              </div>
              <div v-for="brand in shoppingStats.brands.slice(0, 3)" :key="brand.name" class="mb-3">
                <div class="d-flex justify-content-between small mb-1">
                  <span class="fw-semibold">{{ brand.name }}</span>
                  <span class="text-muted">{{ brand.count }} áo ({{ brand.percent }}%)</span>
                </div>
                <div class="progress" style="height: 8px">
                  <div
                    class="progress-bar bg-dark"
                    role="progressbar"
                    :style="{ width: brand.percent + '%' }"
                  ></div>
                </div>
              </div>

              <hr class="my-4 border-secondary opacity-10" />

              <!-- Thống kê Danh mục -->
              <h6 class="fw-bold text-secondary small text-uppercase mb-3">Danh mục hay mua</h6>
              <div v-if="shoppingStats.categories.length === 0" class="text-muted small">
                Chưa đủ dữ liệu phân tích.
              </div>
              <div v-for="cat in shoppingStats.categories.slice(0, 3)" :key="cat.name" class="mb-3">
                <div class="d-flex justify-content-between small mb-1">
                  <span class="fw-semibold">{{ cat.name }}</span>
                  <span class="text-muted">{{ cat.count }} áo ({{ cat.percent }}%)</span>
                </div>
                <div class="progress" style="height: 8px">
                  <div
                    class="progress-bar bg-primary"
                    role="progressbar"
                    :style="{ width: cat.percent + '%' }"
                  ></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ===================== BẢNG LỊCH SỬ ĐƠN HÀNG ===================== -->
      <div class="card border-0 shadow-sm rounded-4 overflow-hidden">
        <div
          class="card-header bg-white border-bottom p-4 d-flex justify-content-between align-items-center"
        >
          <h5 class="fw-bold mb-0">Lịch sử đơn hàng</h5>
          <span class="badge bg-primary rounded-pill px-3">{{ orders.length }} đơn</span>
        </div>

        <div class="card-body p-0">
          <div v-if="orders.length === 0" class="text-center py-5">
            <i class="ri-file-list-3-line text-muted" style="font-size: 4rem"></i>
            <p class="text-muted mt-3 mb-0">Bạn chưa có đơn hàng nào.</p>
            <router-link to="/ao" class="btn btn-outline-dark mt-3 px-4 rounded-pill"
              >Mua sắm ngay</router-link
            >
          </div>

          <div v-else class="table-responsive">
            <table class="table table-hover align-middle mb-0 text-nowrap">
              <thead class="table-light">
                <tr>
                  <th class="ps-4 py-3 text-secondary small text-uppercase">Mã đơn</th>
                  <th class="py-3 text-secondary small text-uppercase">Ngày đặt</th>
                  <th class="py-3 text-secondary small text-uppercase">Thanh toán</th>
                  <th class="text-end py-3 text-secondary small text-uppercase">Tổng tiền</th>
                  <th class="pe-4 text-center py-3 text-secondary small text-uppercase">
                    Trạng thái
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="order in orders" :key="order.donHang?.id">
                  <td class="ps-4 py-3 fw-bold text-dark">
                    #{{ order.donHang?.maDonHang || order.donHang?.id }}
                  </td>
                  <td class="py-3 text-muted">{{ formatDate(order.donHang?.ngayDat) }}</td>
                  <td class="py-3">
                    <span class="badge bg-light text-dark border">
                      {{
                        order.donHang?.phuongThucThanhToan === 'TIEN_MAT'
                          ? 'Tiền mặt'
                          : order.donHang?.phuongThucThanhToan || 'N/A'
                      }}
                    </span>
                  </td>
                  <td class="text-end py-3 fw-bold text-danger">
                    {{ formatMoney(order.donHang?.tongThanhToan) }}
                  </td>
                  <td class="pe-4 text-center py-3">
                    <span
                      class="badge px-3 py-2 rounded-pill"
                      :class="getStatusClass(order.donHang?.trangThai)"
                    >
                      {{ getStatusLabel(order.donHang?.trangThai) }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import axios from 'axios'
import { onMounted, ref, computed } from 'vue'
import API_BASE from '@/config/api'
import LayoutHeader from '@/components/LayoutHeader.vue'

const orders = ref([])
const summary = ref(null)
const userId = localStorage.getItem('user_id')

const loadHistory = async () => {
  try {
    // THAY ĐỔI API: Dùng API này để lấy được cả Đơn hàng (donHang) và Chi tiết sản phẩm (chiTietDonHangs)
    const res = await axios.get(`${API_BASE}/api/public/orders/user/${userId}`)
    orders.value = res.data || []
  } catch (error) {
    console.error('Lỗi tải lịch sử:', error)
  }
}

const loadSummary = async () => {
  try {
    const res = await axios.get(`${API_BASE}/api/history-order/${userId}/summary`)
    summary.value = res.data
  } catch (error) {
    console.error('Lỗi tải tóm tắt:', error)
  }
}

// Hàm định dạng tiền tệ
const formatMoney = (money) => {
  if (!money) return '0 đ'
  return Number(money).toLocaleString('vi-VN') + ' đ'
}

// Hàm định dạng ngày tháng
const formatDate = (date) => {
  if (!date) return '—'
  return new Date(date).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const getStatusClass = (status) => {
  if (status === 'DA_THANH_CONG')
    return 'bg-success bg-opacity-10 text-success border border-success border-opacity-25'
  if (status === 'DA_HUY')
    return 'bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25'
  if (status === 'CHO_XAC_NHAN')
    return 'bg-warning bg-opacity-10 text-warning border border-warning border-opacity-25'
  return 'bg-secondary bg-opacity-10 text-secondary border border-secondary border-opacity-25'
}

const getStatusLabel = (status) => {
  const labels = {
    CHO_XAC_NHAN: 'Chờ xác nhận',
    DA_XAC_NHAN: 'Đã xác nhận',
    DANG_VAN_CHUYEN: 'Đang giao',
    DA_THANH_CONG: 'Hoàn thành',
    YEU_CAU_TRA_HANG: 'Yêu cầu trả',
    DA_TRA_HANG: 'Đã trả hàng',
    DA_HUY: 'Đã hủy',
  }
  return labels[status] || status
}

// Tự động tính Trung bình mỗi đơn
const trungBinhMoiDon = computed(() => {
  if (!summary.value || summary.value.tongSoDonHang === 0) return 0
  return summary.value.tongTienDaChi / summary.value.tongSoDonHang
})

// THUẬT TOÁN TỰ ĐỘNG PHÂN TÍCH SỞ THÍCH MUA SẮM DỰA TRÊN ĐƠN HÀNG ĐÃ MUA
const shoppingStats = computed(() => {
  const cats = {}
  const brands = {}
  let totalItems = 0

  // Quét qua toàn bộ lịch sử đơn hàng (đã được sửa tương thích với OrderResponseDTO)
  orders.value.forEach((orderWrapper) => {
    // Lấy mảng chiTietDonHangs bên trong DTO
    const items = orderWrapper.chiTietDonHangs || []

    items.forEach((item) => {
      // Lấy thông tin từ biến thể sản phẩm
      const sp = item.bienTheSanPham?.sanPham || {}
      const qty = Number(item.soLuong || 1)

      // Chỉ tính các đơn hàng đã thành công hoặc chờ xác nhận (không tính đơn đã hủy)
      if (orderWrapper.donHang?.trangThai !== 'DA_HUY') {
        totalItems += qty
        const catName = sp.danhMuc?.ten || 'Áo khác'
        const brandName = sp.thuongHieu?.ten || 'No Brand'

        cats[catName] = (cats[catName] || 0) + qty
        brands[brandName] = (brands[brandName] || 0) + qty
      }
    })
  })

  // Hàm chuyển đổi Object thành Mảng, tính phần trăm và sắp xếp giảm dần
  const formatAndSort = (dataMap) => {
    return Object.entries(dataMap)
      .map(([name, count]) => ({
        name,
        count,
        percent: totalItems > 0 ? Math.round((count / totalItems) * 100) : 0,
      }))
      .sort((a, b) => b.count - a.count)
  }

  return {
    categories: formatAndSort(cats),
    brands: formatAndSort(brands),
    totalItems,
  }
})

onMounted(() => {
  if (userId) {
    loadHistory()
    loadSummary()
  }
})
</script>

<style scoped>
.letter-spacing-1 {
  letter-spacing: 0.05em;
}
.border-bottom-dashed {
  border-bottom: 1px dashed #dee2e6;
}
/* Hiệu ứng rê chuột vào dòng bảng */
.table-hover tbody tr:hover {
  background-color: #f8f9fa;
  transition: background-color 0.2s ease;
}
</style>
