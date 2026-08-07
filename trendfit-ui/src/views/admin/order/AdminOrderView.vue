<template>
  <div class="container-fluid py-4">
    <!-- Header đồng bộ UI admin -->
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
      <div>
        <h4 class="fw-bold mb-1">Quản lý đơn hàng</h4>
        <p class="text-secondary small mb-0">Duyệt đơn online, tại quầy và yêu cầu trả hàng</p>
      </div>
    </div>

    <!-- Tab phân loại -->
    <div class="d-flex flex-wrap gap-2 mb-4">
      <button
        type="button"
        class="btn btn-sm"
        :class="currentTab === 'all' ? 'btn-primary' : 'btn-outline-primary'"
        @click="switchTab('all')"
      >
        Tất cả ({{ stats.all }})
      </button>
      <button
        type="button"
        class="btn btn-sm"
        :class="currentTab === 'online' ? 'btn-primary' : 'btn-outline-primary'"
        @click="switchTab('online')"
      >
        Đơn online ({{ stats.online }})
      </button>
      <button
        type="button"
        class="btn btn-sm"
        :class="currentTab === 'return' ? 'btn-danger' : 'btn-outline-danger'"
        @click="switchTab('return')"
      >
        Yêu cầu trả hàng ({{ stats.returnOrders }})
      </button>
      <button
        type="button"
        class="btn btn-sm"
        :class="currentTab === 'null-user' ? 'btn-primary' : 'btn-outline-primary'"
        @click="switchTab('null-user')"
      >
        Vãng lai / tại quầy ({{ stats.nullUser }})
      </button>
    </div>

    <!-- Lọc theo ngày + luôn sắp xếp đơn mới nhất lên đầu (trong filteredOrders) -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-body py-3">
        <div class="row g-2 align-items-end">
          <div class="col-md-3">
            <label class="form-label small fw-semibold mb-1">Từ ngày</label>
            <input v-model="filterFromDate" type="date" class="form-control form-control-sm" />
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-semibold mb-1">Đến ngày</label>
            <input v-model="filterToDate" type="date" class="form-control form-control-sm" />
          </div>
          <div class="col-md-auto d-flex gap-2">
            <button type="button" class="btn btn-sm btn-outline-secondary" @click="clearDateFilter">
              Xóa lọc ngày
            </button>
          </div>
          <div class="col-md-auto ms-md-auto">
            <span class="small text-secondary">Sắp xếp: mới nhất trước</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
      <div class="text-muted mt-2">Đang tải dữ liệu đơn hàng...</div>
    </div>

    <div v-else-if="fetchError" class="alert alert-danger">
      <strong>Không tải được đơn hàng.</strong><br />
      {{ fetchError }}
      <div class="mt-2">
        <button type="button" class="btn btn-sm btn-outline-danger" @click="fetchOrders()">Thử lại</button>
      </div>
    </div>

    <!--
      Bảng đơn hàng — đã GỘP cột "Hành Động" + "Lịch Sử" + "In Ấn"
      thành 1 cột "Thao tác" (tránh trùng nút Xem lịch sử).
    -->
    <div v-else class="card border-0 shadow-sm overflow-hidden">
      <div class="card-header bg-white border-0 border-bottom d-flex justify-content-between align-items-center py-3">
        <span class="fw-semibold">Danh sách đơn hàng</span>
        <span class="badge text-bg-primary rounded-pill">Tổng: {{ filteredOrders.length }}</span>
      </div>
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th>Mã đơn</th>
              <th>Khách hàng</th>
              <th>Sản phẩm</th>
              <th class="text-end">Tổng tiền</th>
              <th>Trạng thái</th>
              <th class="text-center" style="min-width: 150px">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredOrders.length === 0">
              <td colspan="6" class="text-center text-muted py-4">
                Không có đơn hàng nào trong mục này.
              </td>
            </tr>
            <tr v-for="item in filteredOrders" :key="item.donHang?.id || item.id">
              <td>
                <strong>#{{ item.donHang.id }}</strong>
                <span v-if="isPosOrder(item)" class="badge bg-primary ms-1">POS</span>
                <br />
                <small class="text-muted">{{ formatDate(item.donHang.ngayDat) }}</small>
              </td>
              <td>
                <strong>{{ item.donHang.tenNguoiNhan }}</strong
                ><br />
                <i class="bi bi-telephone"></i> {{ item.donHang.soDienThoaiGiao }}<br />
                <small class="text-secondary">{{
                  item.donHang.diaChiGiao || 'Mua tại quầy'
                }}</small>
              </td>
              <td>
                <ul class="list-unstyled mb-0 small">
                  <li v-for="ct in (item.chiTietDonHangs || [])" :key="ct.id">
                    {{ ct.soLuong }}x {{ ct.tenSanPham || 'Sản phẩm' }}
                    <span v-if="ct.kichCoSize || ct.mauSac">
                      ({{ ct.kichCoSize || '-' }} / {{ ct.mauSac || '-' }})
                    </span>
                  </li>
                  <li v-if="!item.chiTietDonHangs || item.chiTietDonHangs.length === 0" class="text-muted">
                    Không có chi tiết
                  </li>
                </ul>
              </td>
              <td class="text-end">
                <div class="text-danger fw-bold">{{ formatPrice(item.donHang.tongThanhToan) }}</div>
                <small class="text-muted">Ship: {{ formatPrice(item.donHang.phiVanChuyen) }}</small>
              </td>
              <td>
                <span class="badge w-100 mb-1" :class="getStatusClass(item.donHang.trangThai)">
                  {{ getStatusLabel(item.donHang.trangThai) }}
                </span>
                <select
                  v-if="getAvailableStatuses(item.donHang.trangThai).length > 0"
                  class="form-select form-select-sm mt-2"
                  @change="
                    capNhatTrangThai(item.donHang.id, item.donHang.trangThai, $event.target.value)
                  "
                  :value="item.donHang.trangThai"
                >
                  <option :value="item.donHang.trangThai">-- Giữ nguyên --</option>
                  <option
                    v-for="status in getAvailableStatuses(item.donHang.trangThai)"
                    :key="status"
                    :value="status"
                  >
                    {{ getStatusLabel(status) }}
                  </option>
                </select>
                <div v-else class="text-muted small text-center mt-2">
                  <em>Cố định</em>
                </div>
              </td>
              <!-- Một cột Thao tác: lịch sử + trả hàng (nếu có) + in hóa đơn -->
              <td class="text-center">
                <div class="d-flex flex-column gap-1 align-items-center">
                  <button
                    type="button"
                    class="btn btn-sm btn-outline-secondary text-nowrap"
                    @click="xemLichSu(item.donHang.id)"
                  >
                    Lịch sử
                  </button>
                  <button
                    v-if="item.donHang.trangThai === 'YEU_CAU_TRA_HANG'"
                    type="button"
                    class="btn btn-sm btn-outline-danger text-nowrap"
                    @click="xemChiTietTraHang(item.donHang.id)"
                  >
                    Yêu cầu trả
                  </button>
                  <button
                    type="button"
                    class="btn btn-sm btn-outline-primary text-nowrap"
                    @click="printInvoice(item)"
                  >
                    In hóa đơn
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ===================== MODAL LỊCH SỬ DUYỆT ĐƠN HÀNG ===================== -->
    <div v-if="showHistoryModal" class="history-overlay" @click.self="showHistoryModal = false">
      <div class="history-modal bg-white rounded-3 shadow p-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="fw-bold mb-0">Lịch sử duyệt đơn hàng #{{ historyOrderId }}</h5>
          <button class="btn-close" @click="showHistoryModal = false"></button>
        </div>

        <div v-if="loadingHistory" class="text-center py-4">
          <div class="spinner-border text-dark" role="status"></div>
        </div>

        <div v-else-if="orderHistory.length === 0" class="text-muted">
          Đơn hàng này chưa có lịch sử thay đổi trạng thái nào.
        </div>

        <ul v-else class="list-group">
          <li v-for="h in orderHistory" :key="h.id" class="list-group-item">
            <div class="d-flex justify-content-between">
              <span>
                <span class="badge" :class="getStatusClass(h.trangThaiCu)">{{
                  getStatusLabel(h.trangThaiCu)
                }}</span>
                →
                <span class="badge" :class="getStatusClass(h.trangThaiMoi)">{{
                  getStatusLabel(h.trangThaiMoi)
                }}</span>
              </span>
              <small class="text-muted">{{ formatDate(h.ngayThayDoi) }}</small>
            </div>
            <div class="small mt-1">
              Người thực hiện: <strong>{{ h.tenNguoiThucHien }}</strong>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import print from 'print-js'
import { getAuthHeaders } from '@/utils/adminAuth'

const danhSachDonHang = ref([])
const loading = ref(true)
const currentTab = ref('all') // 'all', 'online', 'return', 'null-user'
// Lọc theo khoảng ngày (input type="date" → chuỗi YYYY-MM-DD)
const filterFromDate = ref('')
const filterToDate = ref('')

// State cho modal lịch sử
const showHistoryModal = ref(false)
const historyOrderId = ref(null)
const orderHistory = ref([])
const loadingHistory = ref(false)

// Thống kê số lượng đơn theo từng tab
const stats = computed(() => {
  const all = danhSachDonHang.value.length
  const pos = danhSachDonHang.value.filter((item) => isPosOrder(item)).length
  const online = all - pos
  // Đơn vãng lai (khách vãng lai tính trên danh sách hoặc gọi riêng API)
  const nullUser = danhSachDonHang.value.filter((item) => !item.donHang.nguoiDung).length
  return { all, online, pos, nullUser }
})

// Kiểm tra xem có phải đơn POS (tại quầy) hay không
const isPosOrder = (item) => {
  const address = item.donHang.diaChiGiao || ''
  return address.toLowerCase().includes('quầy') || address.toLowerCase().includes('pos')
}

// Thống kê số lượng đơn theo từng tab phân loại
const stats = computed(() => {
  const all = danhSachDonHang.value.length
  const online = danhSachDonHang.value.filter(
    (item) => !isPosOrder(item) && item.donHang.nguoiDung,
  ).length
  const returnOrders = danhSachDonHang.value.filter(
    (item) => item.donHang.trangThai === 'YEU_CAU_TRA_HANG',
  ).length
  const nullUser = danhSachDonHang.value.filter(
    (item) => isPosOrder(item) || !item.donHang.nguoiDung,
  ).length
  return { all, online, returnOrders, nullUser }
})

// Lọc theo tab + khoảng ngày, rồi sắp xếp đơn mới nhất lên đầu
const filteredOrders = computed(() => {
  let list = danhSachDonHang.value

  // 1) Lọc theo tab
  if (currentTab.value === 'online') {
    list = list.filter((item) => !isPosOrder(item) && item.donHang.nguoiDung)
  } else if (currentTab.value === 'return') {
    list = list.filter((item) => item.donHang.trangThai === 'YEU_CAU_TRA_HANG')
  } else if (currentTab.value === 'null-user') {
    list = list.filter((item) => isPosOrder(item) || !item.donHang.nguoiDung)
  }

  // 2) Lọc theo ngày đặt (so sánh theo ngày local, bỏ qua giờ)
  if (filterFromDate.value || filterToDate.value) {
    list = list.filter((item) => {
      const raw = item.donHang?.ngayDat
      if (!raw) return false
      const d = new Date(raw)
      if (Number.isNaN(d.getTime())) return false
      // Chuẩn hóa về YYYY-MM-DD theo local
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const key = `${y}-${m}-${day}`
      if (filterFromDate.value && key < filterFromDate.value) return false
      if (filterToDate.value && key > filterToDate.value) return false
      return true
    })
  }

  // 3) Mới nhất lên đầu (ngayDat giảm dần; fallback theo id)
  return [...list].sort((a, b) => {
    const ta = new Date(a.donHang?.ngayDat || 0).getTime()
    const tb = new Date(b.donHang?.ngayDat || 0).getTime()
    if (tb !== ta) return tb - ta
    return Number(b.donHang?.id || 0) - Number(a.donHang?.id || 0)
  })
})

const switchTab = (tab) => {
  currentTab.value = tab
}

const clearDateFilter = () => {
  filterFromDate.value = ''
  filterToDate.value = ''
}

// Lấy dữ liệu từ Backend
const fetchError = ref('')

const fetchOrders = async (type = 'all') => {
  loading.value = true
  fetchError.value = ''
  try {
    // Kiểm tra đã đăng nhập admin/nhân viên chưa
    const headers = getAuthHeaders()
    if (!headers['User-Role']) {
      fetchError.value =
        'Chưa đăng nhập hoặc phiên hết hạn. Vui lòng đăng nhập bằng tài khoản admin / nhanvien.'
      danhSachDonHang.value = []
      return
    }

    let url = 'http://localhost:8080/api/admin/orders'
    if (type === 'null-user') {
      url = 'http://localhost:8080/api/admin/orders/null-user'
    }

    const res = await axios.get(url, { headers })
    // Đảm bảo luôn là mảng
    danhSachDonHang.value = Array.isArray(res.data) ? res.data : []
  } catch (err) {
    console.error('Lỗi tải đơn hàng:', err)
    const status = err.response?.status
    const msg = err.response?.data
    if (status === 401) {
      fetchError.value = 'Thiếu xác thực. Đăng nhập lại (admin/nhanvien - mật khẩu 123).'
    } else if (status === 403) {
      fetchError.value =
        'Không có quyền xem đơn hàng. Cần tài khoản ADMIN hoặc EMPLOYEE (đã mở quyền cho nhân viên).'
    } else if (!err.response) {
      fetchError.value = 'Không kết nối được backend (localhost:8080). Hãy chạy API rồi F5.'
    } else {
      fetchError.value =
        typeof msg === 'string' ? msg : `Lỗi tải đơn hàng (${status || 'network'})`
    }
    danhSachDonHang.value = []
  } finally {
    loading.value = false
  }
}

// Trạng thái đơn hàng hợp lệ
const getAvailableStatuses = (currentStatus) => {
  const transitions = {
    CHO_XAC_NHAN: ['DA_XAC_NHAN', 'DA_HUY'],
    DA_XAC_NHAN: ['DANG_VAN_CHUYEN', 'DA_HUY'],
    DANG_VAN_CHUYEN: ['DA_THANH_CONG'],
    DA_THANH_CONG: [],
    DA_HUY: [],
  }
  return transitions[currentStatus] || []
}

const capNhatTrangThai = async (id, currentStatus, newStatus) => {
  if (newStatus === currentStatus) return

  const available = getAvailableStatuses(currentStatus)
  if (!available.includes(newStatus)) {
    alert(`❌ Không thể chuyển trạng thái này!`)
    return
  }

  if (!confirm(`Xác nhận đổi trạng thái đơn hàng #${id}?`)) return

  try {
    await axios.put(
      `http://localhost:8080/api/admin/orders/${id}/status?status=${newStatus}`,
      {},
      { headers: getAuthHeaders() },
    )
    alert('✅ Cập nhật trạng thái thành công!')
    fetchOrders(currentTab.value === 'null-user' ? 'null-user' : 'all')
  } catch (err) {
    alert('❌ Lỗi cập nhật: ' + (err.response?.data || err.message))
  }
}

const xemLichSu = async (id) => {
  historyOrderId.value = id
  showHistoryModal.value = true
  loadingHistory.value = true
  orderHistory.value = []

  try {
    const res = await axios.get(`http://localhost:8080/api/admin/orders/${id}/history`, {
      headers: getAuthHeaders(),
    })
    orderHistory.value = res.data
  } catch (err) {
    console.error('Lỗi tải lịch sử:', err)
  } finally {
    loadingHistory.value = false
  }
}

const getStatusClass = (status) => {
  const classes = {
    CHO_XAC_NHAN: 'bg-warning text-dark',
    DA_XAC_NHAN: 'bg-info',
    DANG_VAN_CHUYEN: 'bg-primary',
    DA_THANH_CONG: 'bg-success',
    DA_HUY: 'bg-danger',
  }
  return classes[status] || 'bg-secondary'
}

const getStatusLabel = (status) => {
  const labels = {
    CHO_XAC_NHAN: 'Chờ xác nhận',
    DA_XAC_NHAN: 'Đã xác nhận',
    DANG_VAN_CHUYEN: 'Đang vận chuyển',
    DA_THANH_CONG: 'Đã thành công',
    DA_HUY: 'Đã hủy',
  }
  return labels[status] || status
}

// In hóa đơn từ trang Quản lý đơn hàng.
// Bổ sung đầy đủ: tạm tính, phí ship, mã giảm giá + số tiền giảm, tổng thanh toán.
const printInvoice = (item) => {
  const order = item.donHang
  const details = item.chiTietDonHangs || []

  const rows = details
    .map(
      (d, index) => `
    <tr>
      <td style="text-align: center; padding: 8px; border-bottom: 1px solid #ddd;">${index + 1}</td>
      <td style="padding: 8px; border-bottom: 1px solid #ddd;">${d.tenSanPham || 'Sản phẩm'}</td>
      <td style="text-align: center; border-bottom: 1px solid #ddd;">${d.kichCoSize || '-'} / ${d.mauSac || '-'}</td>
      <td style="text-align: center; border-bottom: 1px solid #ddd;">${d.soLuong}</td>
      <td style="text-align: right; border-bottom: 1px solid #ddd;">${formatPrice(d.donGia)}</td>
      <td style="text-align: right; border-bottom: 1px solid #ddd;">${formatPrice(Number(d.soLuong || 0) * Number(d.donGia || 0))}</td>
    </tr>
  `,
    )
    .join('')

  // Mã giảm giá: ưu tiên maVoucher / maGiamGia / voucher.ma (tùy backend trả về)
  const maGiamGia =
    order.maVoucher ||
    order.maGiamGia ||
    order.voucher?.ma ||
    order.maCode ||
    null

  // ---- Tính tiền in hóa đơn cho khớp số liệu ----
  // Có đơn backend để tienGiam = 0 nhưng tongThanhToan đã trừ giảm → suy ra số giảm
  // để hóa đơn không bị "Tạm tính 269k + giảm 0 = tổng 242k" (vô lý).
  const phiShip = Number(order.phiVanChuyen || 0)
  const tongHangFromItems = details.reduce(
    (sum, d) => sum + Number(d.soLuong || 0) * Number(d.donGia || 0),
    0,
  )
  const tamTinh = Number(order.tongTienHang || 0) || tongHangFromItems
  const tongThanhToan = Number(order.tongThanhToan || 0)
  let tienGiam = Number(order.tienGiam || 0)
  if (tienGiam <= 0) {
    const inferred = tamTinh + phiShip - tongThanhToan
    if (inferred > 0.5) tienGiam = inferred
  }

  const printContent = `
    <div style="font-family: Arial, sans-serif; padding: 20px; max-width: 800px; margin: auto;">
      <h1 style="text-align: center; margin-bottom: 8px;">HÓA ĐƠN BÁN HÀNG</h1>
      <hr style="border: 0; border-top: 1px solid #ccc; margin: 12px 0 20px;" />

      <div style="display: flex; justify-content: space-between; gap: 24px; margin-bottom: 16px;">
        <div>
          <p style="margin: 4px 0;"><strong>Khách hàng:</strong> ${order.tenNguoiNhan || '—'}</p>
          <p style="margin: 4px 0;"><strong>SĐT:</strong> ${order.soDienThoaiGiao || '—'}</p>
          <p style="margin: 4px 0;"><strong>Địa chỉ:</strong> ${order.diaChiGiao || 'Mua tại quầy'}</p>
        </div>
        <div>
          <p style="margin: 4px 0;"><strong>Mã đơn:</strong> #${order.id}</p>
          <p style="margin: 4px 0;"><strong>Ngày đặt:</strong> ${formatDate(order.ngayDat)}</p>
          <p style="margin: 4px 0;"><strong>Thanh toán:</strong> ${order.phuongThucThanhToan || 'Tiền mặt'}</p>
          ${
            maGiamGia
              ? `<p style="margin: 4px 0;"><strong>Mã giảm giá:</strong> ${maGiamGia}</p>`
              : ''
          }
        </div>
      </div>

      <table style="width: 100%; border-collapse: collapse; margin-top: 12px;">
        <thead style="background: #eee;">
          <tr>
            <th style="text-align: center; padding: 8px;">STT</th>
            <th style="text-align: left; padding: 8px;">Sản phẩm</th>
            <th style="text-align: center; padding: 8px;">Size/Màu</th>
            <th style="text-align: center; padding: 8px;">SL</th>
            <th style="text-align: right; padding: 8px;">Đơn giá</th>
            <th style="text-align: right; padding: 8px;">Thành tiền</th>
          </tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>

      <div style="margin-top: 20px; text-align: right; line-height: 1.7;">
        <p style="margin: 4px 0;">Tạm tính: ${formatPrice(tamTinh)}</p>
        <p style="margin: 4px 0;">Phí vận chuyển: ${formatPrice(phiShip)}</p>
        <p style="margin: 4px 0;">
          Giảm giá${maGiamGia ? ` (${maGiamGia})` : ''}: ${formatPrice(tienGiam)}
        </p>
        <h2 style="color: red; margin: 12px 0 0;">
          Tổng thanh toán: ${formatPrice(tongThanhToan)}
        </h2>
      </div>

      <p style="margin-top: 36px; text-align: center; font-style: italic; color: #4f46e5;">
        Cảm ơn quý khách đã mua hàng tại TrendFit!
      </p>
    </div>
  `
  print({ printable: printContent, type: 'raw-html' })
}

const formatPrice = (v) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v || 0)

const formatDate = (dateString) => {
  if (!dateString) return 'Chưa có ngày'
  return new Date(dateString).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

onMounted(() => fetchOrders('all'))
</script>

<style scoped>
.history-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
}

.history-modal {
  width: 90%;
  max-width: 520px;
  max-height: 80vh;
  overflow-y: auto;
}
</style>
