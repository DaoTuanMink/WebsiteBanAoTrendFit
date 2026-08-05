<template>
  <div class="admin-orders bg-white min-vh-100">
    <div class="container py-5 text-start">
      <h3 class="fw-bold text-dark mb-3">QUẢN LÝ HÓA ĐƠN & ĐƠN HÀNG</h3>

      <!-- THANH TAB PHÂN LOẠI ĐƠN HÀNG -->
      <div class="d-flex flex-wrap gap-2 mb-4">
        <button
          class="btn btn-sm"
          :class="currentTab === 'all' ? 'btn-dark' : 'btn-outline-dark'"
          @click="switchTab('all')"
        >
          Tất cả đơn ({{ stats.all }})
        </button>

        <button
          class="btn btn-sm"
          :class="currentTab === 'online' ? 'btn-dark' : 'btn-outline-dark'"
          @click="switchTab('online')"
        >
          📦 Đơn Online ({{ stats.online }})
        </button>

        <button
          class="btn btn-sm"
          :class="currentTab === 'return' ? 'btn-danger' : 'btn-outline-danger'"
          @click="switchTab('return')"
        >
          🔄 Yêu cầu trả hàng ({{ stats.returnOrders }})
        </button>

        <button
          class="btn btn-sm"
          :class="currentTab === 'null-user' ? 'btn-dark' : 'btn-outline-dark'"
          @click="switchTab('null-user')"
        >
          👤 Vãng lai và tại quầy ({{ stats.nullUser }})
        </button>
      </div>

      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-dark" role="status"></div>
        <div class="text-muted mt-2">Đang tải dữ liệu đơn hàng...</div>
      </div>

      <div v-else-if="fetchError" class="alert alert-danger">
        <strong>Không tải được đơn hàng.</strong><br />
        {{ fetchError }}
        <div class="mt-2">
          <button class="btn btn-sm btn-outline-danger" @click="fetchOrders()">Thử lại</button>
        </div>
      </div>

      <div class="table-responsive shadow-sm rounded-3" v-else>
        <table class="table table-bordered align-middle text-center mb-0">
          <thead class="table-dark">
            <tr>
              <th>Mã Đơn</th>
              <th>Khách Hàng</th>
              <th>Sản Phẩm & Đổi Trả</th>
              <th>Tổng Tiền</th>
              <th>Trạng Thái</th>
              <th>Hành Động</th>
              <th>Lịch Sử</th>
              <th>In Ấn</th>
            </tr>
          </thead>
          <tbody class="text-start">
            <tr v-if="filteredOrders.length === 0">
              <td colspan="8" class="text-center text-muted py-4">
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
                  <li v-for="ct in item.chiTietDonHangs || []" :key="ct.id">
                    {{ ct.soLuong }}x {{ ct.tenSanPham || 'Sản phẩm' }}
                    <span v-if="ct.kichCoSize || ct.mauSac">
                      ({{ ct.kichCoSize || '-' }} / {{ ct.mauSac || '-' }})
                    </span>
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
                  <option :value="item.donHang.trangThai">-- Chuyển trạng thái --</option>
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
              <td class="text-center">
                <div class="d-flex flex-column gap-1">
                  <button
                    class="btn btn-sm btn-outline-secondary text-nowrap"
                    @click="xemLichSu(item.donHang.id)"
                  >
                    Xem lịch sử
                  </button>
                  <button
                    v-if="item.donHang.trangThai === 'YEU_CAU_TRA_HANG'"
                    class="btn btn-sm btn-outline-danger text-nowrap"
                    @click="xemChiTietTraHang(item.donHang.id)"
                  >
                    Xem yêu cầu trả
                  </button>
                </div>
              </td>
              <td class="text-center">
                <button
                  class="btn btn-sm btn-outline-secondary text-nowrap"
                  @click="xemLichSu(item.donHang.id)"
                >
                  Xem lịch sử
                </button>
              </td>
              <td class="text-center">
                <button class="btn btn-sm btn-outline-dark text-nowrap" @click="printInvoice(item)">
                  In Hóa Đơn
                </button>
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

    <!-- ===================== MODAL XEM CHI TIẾT YÊU CẦU TRẢ HÀNG ===================== -->
    <div
      v-if="showReturnDetailModal"
      class="history-overlay"
      @click.self="showReturnDetailModal = false"
    >
      <div class="history-modal bg-white rounded-3 shadow p-4" style="max-width: 600px">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="fw-bold mb-0 text-danger">
            🔄 Chi tiết yêu cầu trả hàng (#{{ activeReturnOrderId }})
          </h5>
          <button class="btn-close" @click="showReturnDetailModal = false"></button>
        </div>

        <div v-if="loadingReturnDetail" class="text-center py-4">
          <div class="spinner-border text-danger" role="status"></div>
        </div>

        <div v-else>
          <div class="mb-3">
            <label class="form-label fw-semibold text-secondary small"
              >Lý do khách hàng yêu cầu:</label
            >
            <div class="p-3 bg-light rounded border text-dark">
              {{ returnDetailData.lyDo || 'Không có lý do chi tiết.' }}
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label fw-semibold text-secondary small"
              >Ảnh / Video minh chứng:</label
            >
            <div
              v-if="returnDetailData.moTaChiTiet"
              class="text-center border rounded p-2 bg-black bg-opacity-10"
            >
              <!-- Nếu là Video -->
              <video
                v-if="isVideoFile(returnDetailData.moTaChiTiet)"
                :src="returnDetailData.moTaChiTiet"
                controls
                class="w-100 rounded"
                style="max-height: 350px"
              ></video>
              <!-- Nếu là Ảnh -->
              <img
                v-else
                :src="returnDetailData.moTaChiTiet"
                alt="Minh chứng trả hàng"
                class="img-fluid rounded"
                style="max-height: 350px; object-fit: contain"
              />
            </div>
            <div v-else class="text-muted fst-italic small">
              Không có tệp hình ảnh hoặc video minh chứng được tải lên.
            </div>
          </div>
        </div>

        <div class="d-flex justify-content-end mt-4">
          <button class="btn btn-secondary btn-sm" @click="showReturnDetailModal = false">
            Đóng
          </button>
        </div>
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

// State cho modal lịch sử
const showHistoryModal = ref(false)
const historyOrderId = ref(null)
const orderHistory = ref([])
const loadingHistory = ref(false)

// State cho modal xem chi tiết trả hàng (Ảnh/Video)
const showReturnDetailModal = ref(false)
const activeReturnOrderId = ref(null)
const returnDetailData = ref({ lyDo: '', moTaChiTiet: '' })
const loadingReturnDetail = ref(false)

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

// Lọc danh sách hiển thị theo tab
const filteredOrders = computed(() => {
  if (currentTab.value === 'online') {
    return danhSachDonHang.value.filter((item) => !isPosOrder(item) && item.donHang.nguoiDung)
  }
  if (currentTab.value === 'return') {
    return danhSachDonHang.value.filter((item) => item.donHang.trangThai === 'YEU_CAU_TRA_HANG')
  }
  if (currentTab.value === 'null-user') {
    return danhSachDonHang.value.filter((item) => isPosOrder(item) || !item.donHang.nguoiDung)
  }
  return danhSachDonHang.value
})

const switchTab = (tab) => {
  currentTab.value = tab
}

const fetchError = ref('')

const fetchOrders = async () => {
  loading.value = true
  fetchError.value = ''
  try {
    const headers = getAuthHeaders()
    if (!headers['User-Role']) {
      fetchError.value = 'Chưa đăng nhập hoặc phiên hết hạn. Vui lòng đăng nhập lại.'
      danhSachDonHang.value = []
      return
    }

    const res = await axios.get('http://localhost:8080/api/admin/orders', { headers })
    danhSachDonHang.value = Array.isArray(res.data) ? res.data : []
  } catch (err) {
    console.error('Lỗi tải đơn hàng:', err)
    fetchError.value = err.response?.data || 'Không thể kết nối đến máy chủ.'
    danhSachDonHang.value = []
  } finally {
    loading.value = false
  }
}

const getAvailableStatuses = (currentStatus) => {
  const transitions = {
    CHO_XAC_NHAN: ['DA_XAC_NHAN', 'DA_HUY'],
    DA_XAC_NHAN: ['DANG_VAN_CHUYEN', 'DA_HUY'],
    DANG_VAN_CHUYEN: ['DA_THANH_CONG'],
    DA_THANH_CONG: [],
    YEU_CAU_TRA_HANG: ['DA_TRA_HANG', 'DA_THANH_CONG'],
    DA_TRA_HANG: [],
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

  if (!confirm(`Xác nhận đổi trạng thái đơn hàng #${id} sang ${getStatusLabel(newStatus)}?`)) return

  try {
    await axios.put(
      `http://localhost:8080/api/admin/orders/${id}/status?status=${newStatus}`,
      {},
      { headers: getAuthHeaders() },
    )
    alert('✅ Cập nhật trạng thái thành công!')
    fetchOrders()
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

// Mở form modal hiển thị chi tiết lý do và ảnh/video trả hàng
const xemChiTietTraHang = async (id) => {
  activeReturnOrderId.value = id
  showReturnDetailModal.value = true
  loadingReturnDetail.value = true
  returnDetailData.value = { lyDo: '', moTaChiTiet: '' }

  try {
    const res = await axios.get(`http://localhost:8080/api/admin/orders/${id}/return-details`, {
      headers: getAuthHeaders(),
    })
    returnDetailData.value = res.data || {}
  } catch (e) {
    console.error('Lỗi tải thông tin trả hàng:', e)
    alert('Không thể lấy chi tiết yêu cầu trả hàng của đơn này!')
  } finally {
    loadingReturnDetail.value = false
  }
}

// Kiểm tra xem URL có phải là video hay không
const isVideoFile = (url) => {
  if (!url) return false
  const lower = url.toLowerCase()
  return (
    lower.endsWith('.mp4') ||
    lower.endsWith('.webm') ||
    lower.endsWith('.ogg') ||
    lower.includes('/video/upload/')
  )
}

const getStatusClass = (status) => {
  const classes = {
    CHO_XAC_NHAN: 'bg-warning text-dark',
    DA_XAC_NHAN: 'bg-info',
    DANG_VAN_CHUYEN: 'bg-primary',
    DA_THANH_CONG: 'bg-success',
    YEU_CAU_TRA_HANG: 'bg-secondary text-white',
    DA_TRA_HANG: 'bg-dark text-white',
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
    YEU_CAU_TRA_HANG: 'Yêu cầu trả hàng',
    DA_TRA_HANG: 'Đã trả hàng',
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
      <td style="text-align: center; border-bottom: 1px solid #ddd;">${d.kichCoSize} / ${d.mauSac}</td>
      <td style="text-align: center; border-bottom: 1px solid #ddd;">${d.soLuong}</td>
      <td style="text-align: right; border-bottom: 1px solid #ddd;">${formatPrice(d.donGia)}</td>
      <td style="text-align: right; border-bottom: 1px solid #ddd;">${formatPrice(d.soLuong * d.donGia)}</td>
    </tr>
  `,
    )
    .join('')

  const printContent = `
    <div style="font-family: Arial, sans-serif; padding: 20px;">
      <h1 style="text-align: center;">HÓA ĐƠN BÁN HÀNG</h1>
      <hr>
      <div style="display: flex; justify-content: space-between;">
        <div>
          <p><strong>Khách hàng:</strong> ${order.tenNguoiNhan}</p>
          <p><strong>SĐT:</strong> ${order.soDienThoaiGiao}</p>
          <p><strong>Địa chỉ:</strong> ${order.diaChiGiao || 'Mua tại quầy'}</p>
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

onMounted(() => fetchOrders())
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
