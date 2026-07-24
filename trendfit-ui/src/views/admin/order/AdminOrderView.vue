<template>
  <div class="admin-orders bg-white min-vh-100">
    <div class="container py-5 text-start">
      <h3 class="fw-bold text-dark mb-2">QUẢN LÝ HÓA ĐƠN & ĐƠN HÀNG</h3>

      <div class="mb-4">
        <button class="btn btn-sm btn-dark me-2" @click="fetchOrders('all')">Tất cả đơn</button>
        <button class="btn btn-sm btn-outline-dark" @click="fetchOrders('null-user')">
          Đơn vãng lai (null user)
        </button>
      </div>

      <div v-if="loading" class="text-center">Đang tải dữ liệu...</div>

      <div class="table-responsive" v-else>
        <table class="table table-bordered align-middle text-center">
          <thead class="table-dark">
            <tr>
              <th>Mã Đơn</th>
              <th>Khách Hàng</th>
              <th>Tổng Tiền</th>
              <th>Trạng Thái</th>
              <th>Hành Động</th>
              <th>Lịch Sử Duyệt</th>
              <th>In Ấn</th>
            </tr>
          </thead>
          <tbody class="text-start">
            <tr v-for="item in danhSachDonHang" :key="item.donHang.id">
              <td>
                <strong>#{{ item.donHang.id }}</strong
                ><br />
                <small class="text-muted">{{ formatDate(item.donHang.ngayDat) }}</small>
              </td>
              <td>
                <strong>{{ item.donHang.tenNguoiNhan }}</strong
                ><br />
                <i class="bi bi-telephone"></i> {{ item.donHang.soDienThoaiGiao }}<br />
                <small>{{ item.donHang.diaChiGiao }}</small>
              </td>
              <td>
                <ul class="list-unstyled mb-0 small">
                  <li v-for="ct in item.chiTietDonHangs" :key="ct.id">
                    {{ ct.soLuong }}x {{ ct.tenSanPham }} ({{ ct.kichCoSize }} / {{ ct.mauSac }})
                  </li>
                </ul>
              </td>
              <td class="text-end">
                <div class="text-danger fw-bold">{{ formatPrice(item.donHang.tongThanhToan) }}</div>
                <small class="text-muted"
                  >Phí ship: {{ formatPrice(item.donHang.phiVanChuyen) }}</small
                >
              </td>
              <td>
                <span class="badge w-100 mb-1" :class="getStatusClass(item.donHang.trangThai)">
                  {{ getStatusLabel(item.donHang.trangThai) }}
                </span>
                <select
                  v-if="getAvailableStatuses(item.donHang.trangThai).length > 0"
                  class="form-select form-select-sm mt-2"
                  @change="capNhatTrangThai(item.donHang.id, item.donHang.trangThai, $event.target.value)"
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
                <div v-else class="text-muted small mt-2">
                  <em>Không thể chuyển trạng thái</em>
                </div>
              </td>
              <td>
                <button
                  class="btn btn-sm btn-outline-secondary"
                  @click="xemLichSu(item.donHang.id)"
                >
                  Xem lịch sử
                </button>
              </td>
              <td>
                <button class="btn btn-sm btn-outline-dark" @click="printInvoice(item)">
                  In Hóa Đơn
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ===================== MODAL LỊCH SỬ DUYỆT ĐƠN HÀNG (Thành) ===================== -->
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
    <!-- =================================================================================== -->
  </div>
</template>

<script setup>
// Trang quản lý Đơn hàng — CHỈ dành cho ADMIN (nằm trong ADMIN_ONLY_PATHS của
// AuthInterceptor, và route "/admin/orders" cũng có meta.requiresAdmin=true).
// Dùng chung helper getAuthHeaders() thay vì hardcode 'ADMIN' để nhất quán
// với các trang admin khác - nếu sau này nới quyền cho EMPLOYEE thì chỉ cần
// sửa 1 chỗ (AuthInterceptor + router) mà không phải sửa lại từng trang.
import { ref, onMounted } from 'vue'
import axios from 'axios'
import print from 'print-js'
import { getAuthHeaders } from '@/utils/adminAuth'

const danhSachDonHang = ref([])
const loading = ref(true)

// State cho modal "Lịch sử duyệt đơn hàng" (yêu cầu Thành)
const showHistoryModal = ref(false)
const historyOrderId = ref(null)
const orderHistory = ref([])
const loadingHistory = ref(false)

// Lấy dữ liệu với tham số lọc
const fetchOrders = async (type = 'all') => {
  loading.value = true
  try {
    let url = 'http://localhost:8080/api/admin/orders'
    if (type === 'null-user') {
      url = 'http://localhost:8080/api/admin/orders/null-user'
    }

    const res = await axios.get(url, { headers: getAuthHeaders() })

    danhSachDonHang.value = res.data
  } catch (err) {
    console.error('Lỗi tải đơn hàng:', err)
  } finally {
    loading.value = false
  }
}

// Xác định những trạng thái được phép chuyển tới từ trạng thái hiện tại
const getAvailableStatuses = (currentStatus) => {
  const transitions = {
    CHO_XAC_NHAN: ['DA_XAC_NHAN', 'DA_HUY'],
    DA_XAC_NHAN: ['DANG_VAN_CHUYEN', 'DA_HUY'],
    DANG_VAN_CHUYEN: ['DA_THANH_CONG'], // Không được hủy khi đang giao
    DA_THANH_CONG: [], // Trạng thái cuối, không chuyển được
    DA_HUY: [], // Trạng thái cuối, không chuyển được
  }
  return transitions[currentStatus] || []
}

const capNhatTrangThai = async (id, currentStatus, newStatus) => {
  // Kiểm tra xem trạng thái mới có hợp lệ không
  if (newStatus === currentStatus) {
    return // Không thay đổi
  }

  const available = getAvailableStatuses(currentStatus)
  if (!available.includes(newStatus)) {
    alert(`❌ Không thể chuyển từ "${getStatusLabel(currentStatus)}" sang "${getStatusLabel(newStatus)}"`)
    return
  }

  const confirmMsg = `Chuyển từ "${getStatusLabel(currentStatus)}" sang "${getStatusLabel(newStatus)}"?`
  if (!confirm(confirmMsg)) return

  try {
    // getAuthHeaders() đã gồm cả "NhanVien-ID" -> backend dùng để ghi lại
    // "ai đã duyệt/thay đổi trạng thái đơn hàng" (xem OrderService.capNhatTrangThaiDonHang)
    await axios.put(
      `http://localhost:8080/api/admin/orders/${id}/status?status=${newStatus}`,
      {},
      { headers: getAuthHeaders() },
    )
    alert('✅ Cập nhật thành công!')
    fetchOrders()
  } catch (err) {
    alert('❌ Lỗi cập nhật: ' + (err.response?.data || err.message))
  }
}

// Mở modal và tải lịch sử duyệt/thay đổi trạng thái của 1 đơn hàng
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
    console.error('Lỗi tải lịch sử đơn hàng:', err)
    alert('❌ Không thể tải lịch sử đơn hàng: ' + (err.response?.data || err.message))
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

const printInvoice = (item) => {
  const order = item.donHang
  const details = item.chiTietDonHangs
  const rows = details
    .map(
      (d) => `
    <tr>
      <td style="padding: 8px; border-bottom: 1px solid #ddd;">${d.tenSanPham}</td>
      <td style="text-align: center;">${d.kichCoSize} / ${d.mauSac}</td>
      <td style="text-align: center;">${d.soLuong}</td>
      <td style="text-align: right;">${formatPrice(d.donGia)}</td>
      <td style="text-align: right;">${formatPrice(d.soLuong * d.donGia)}</td>
    </tr>
  `,
    )
    .join('')

  const printContent = `
    <div style="font-family: Arial, sans-serif; padding: 20px;">
      <h1 style="text-align: center;">HÓA ĐƠN ĐẶT HÀNG</h1>
      <hr>
      <div style="display: flex; justify-content: space-between;">
        <div>
          <p><strong>Khách hàng:</strong> ${order.tenNguoiNhan}</p>
          <p><strong>SĐT:</strong> ${order.soDienThoaiGiao}</p>
          <p><strong>Địa chỉ:</strong> ${order.diaChiGiao}</p>
        </div>
        <div>
          <p><strong>Mã đơn:</strong> #${order.id}</p>
          <p><strong>Ngày đặt:</strong> ${formatDate(order.ngayDat)}</p>
          <p><strong>Thanh toán:</strong> ${order.phuongThucThanhToan || 'Tiền mặt'}</p>
        </div>
      </div>
      <table style="width: 100%; border-collapse: collapse; margin-top: 20px;">
        <thead style="background: #eee;">
          <tr><th style="text-align: left; padding: 8px;">Sản phẩm</th><th>Size/Màu</th><th>SL</th><th>Đơn giá</th><th>Thành tiền</th></tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>
      <div style="margin-top: 20px; text-align: right;">
        <p>Tạm tính: ${formatPrice(order.tongTienHang)}</p>
        <p>Giảm giá: ${formatPrice(order.tienGiam)}</p>
        <h2 style="color: red;">Tổng thanh toán: ${formatPrice(order.tongThanhToan)}</h2>
      </div>
      <p style="margin-top: 30px; font-style: italic;">Ghi chú: ${order.ghiChu || 'Không có'}</p>
    </div>
  `
  print({ printable: printContent, type: 'raw-html' })
}

const formatPrice = (v) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)
const formatDate = (dateString) => {
  if (!dateString) return 'Chưa có ngày'
  const options = {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }
  return new Date(dateString).toLocaleDateString('vi-VN', options)
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
