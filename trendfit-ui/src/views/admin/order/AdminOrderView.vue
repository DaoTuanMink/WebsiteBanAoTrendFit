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
        <button type="button" class="btn btn-sm btn-outline-danger" @click="fetchOrders()">
          Thử lại
        </button>
      </div>
    </div>

    <!-- Bảng danh sách đơn hàng -->
    <div v-else class="card border-0 shadow-sm overflow-hidden">
      <div
        class="card-header bg-white border-0 border-bottom d-flex justify-content-between align-items-center py-3"
      >
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
              <th class="text-center" style="min-width: 160px">Thao tác</th>
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
              <!-- Cột Thao tác với màu sắc nút hài hòa, dễ nhìn -->
              <td class="text-center">
                <div class="d-flex flex-column gap-1 align-items-center">
                  <button
                    type="button"
                    class="btn btn-sm btn-info text-dark fw-semibold text-nowrap w-100 py-1"
                    @click="xemChiTietDonHang(item)"
                  >
                    🔍 Xem chi tiết
                  </button>
                  <button
                    type="button"
                    class="btn btn-sm btn-outline-secondary text-nowrap w-100 py-1"
                    @click="xemLichSu(item.donHang.id)"
                  >
                    🕒 Lịch sử
                  </button>
                  <button
                    v-if="item.donHang.trangThai === 'YEU_CAU_TRA_HANG'"
                    type="button"
                    class="btn btn-sm btn-outline-danger text-nowrap w-100 py-1"
                    @click="xemChiTietTraHang(item.donHang.id)"
                  >
                    🔄 Yêu cầu trả
                  </button>
                  <button
                    type="button"
                    class="btn btn-sm btn-outline-dark text-nowrap w-100 py-1"
                    @click="printInvoice(item)"
                  >
                    🖨️ In hóa đơn
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ===================== MODAL XEM CHI TIẾT ĐƠN HÀNG ===================== -->
    <div v-if="showDetailModal" class="history-overlay" @click.self="showDetailModal = false">
      <div class="checkout-style-modal bg-white rounded-4 shadow-lg p-4">
        <div class="d-flex justify-content-between align-items-center mb-4 border-bottom pb-3">
          <div>
            <h4 class="fw-bold mb-1 text-dark">
              Chi tiết đơn hàng #{{ activeDetailItem?.donHang?.id }}
            </h4>
            <p class="text-secondary small mb-0">
              Ngày đặt: {{ formatDate(activeDetailItem?.donHang?.ngayDat) }}
            </p>
          </div>
          <button class="btn-close" @click="showDetailModal = false"></button>
        </div>

        <div class="row g-4 align-items-start">
          <div class="col-lg-7">
            <div class="p-3 border rounded-3 bg-light mb-3">
              <h6 class="fw-bold text-primary mb-3">📦 Thông tin giao hàng</h6>
              <div class="row g-2 small">
                <div class="col-6">
                  <span class="text-muted">Họ tên người nhận:</span><br />
                  <strong>{{ activeDetailItem?.donHang?.tenNguoiNhan || '—' }}</strong>
                </div>
                <div class="col-6">
                  <span class="text-muted">Số điện thoại:</span><br />
                  <strong>{{ activeDetailItem?.donHang?.soDienThoaiGiao || '—' }}</strong>
                </div>
                <div class="col-12 mt-2">
                  <span class="text-muted">Địa chỉ giao hàng:</span><br />
                  <strong>{{ activeDetailItem?.donHang?.diaChiGiao || 'Mua tại quầy' }}</strong>
                </div>
              </div>
            </div>

            <div class="p-3 border rounded-3 bg-light mb-3">
              <h6 class="fw-bold text-primary mb-3">💳 Phương thức thanh toán</h6>
              <div class="small">
                <strong>{{
                  activeDetailItem?.donHang?.phuongThucThanhToan || 'Tiền mặt (COD)'
                }}</strong>
              </div>
            </div>

            <div v-if="getVoucherCode(activeDetailItem)" class="p-3 border rounded-3 bg-light">
              <h6 class="fw-bold text-primary mb-2">🏷️ Mã giảm giá đã dùng</h6>
              <span class="badge bg-success">{{ getVoucherCode(activeDetailItem) }}</span>
            </div>
          </div>

          <div class="col-lg-5">
            <div class="p-4 border rounded-3 bg-white shadow-sm">
              <h6 class="fw-bold mb-3 border-bottom pb-2">🛍️ Tóm tắt đơn hàng</h6>

              <div class="product-list-scroll mb-3" style="max-height: 200px; overflow-y: auto">
                <div
                  v-for="d in activeDetailItem?.chiTietDonHangs || []"
                  :key="d.id"
                  class="d-flex justify-content-between align-items-center mb-2 pb-2 border-bottom small"
                >
                  <div>
                    <strong class="text-dark">{{ d.tenSanPham || 'Sản phẩm' }}</strong>
                    <div class="text-muted" style="font-size: 11px">
                      Size: {{ d.kichCoSize || '-' }} | Màu: {{ d.mauSac || '-' }} (SL:
                      {{ d.soLuong }})
                    </div>
                  </div>
                  <div class="text-end fw-semibold">
                    {{ formatPrice(Number(d.soLuong || 0) * Number(d.donGia || 0)) }}
                  </div>
                </div>
              </div>

              <div class="d-flex justify-content-between small mb-2">
                <span class="text-secondary">Tạm tính:</span>
                <strong class="text-dark">{{ formatPrice(getTamTinh(activeDetailItem)) }}</strong>
              </div>
              <div class="d-flex justify-content-between small mb-2">
                <span class="text-secondary">Phí vận chuyển:</span>
                <strong class="text-dark">{{
                  formatPrice(activeDetailItem?.donHang?.phiVanChuyen)
                }}</strong>
              </div>
              <div class="d-flex justify-content-between small mb-3 pb-2 border-bottom">
                <span class="text-secondary">Giảm giá:</span>
                <strong class="text-danger"
                  >- {{ formatPrice(getTienGiam(activeDetailItem)) }}</strong
                >
              </div>

              <div class="d-flex justify-content-between mb-4">
                <span class="fw-bold text-dark">Tổng thanh toán:</span>
                <strong class="text-danger fs-5">{{
                  formatPrice(activeDetailItem?.donHang?.tongThanhToan)
                }}</strong>
              </div>

              <div class="d-flex gap-2">
                <button
                  type="button"
                  class="btn btn-dark w-100 py-2 fw-semibold"
                  @click="printInvoice(activeDetailItem)"
                >
                  🖨️ In hóa đơn
                </button>
                <button
                  type="button"
                  class="btn btn-outline-secondary px-3"
                  @click="showDetailModal = false"
                >
                  Đóng
                </button>
              </div>
            </div>
          </div>
        </div>
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
              <video
                v-if="isVideoFile(returnDetailData.moTaChiTiet)"
                :src="returnDetailData.moTaChiTiet"
                controls
                class="w-100 rounded"
                style="max-height: 350px"
              ></video>
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
const currentTab = ref('all')
const filterFromDate = ref('')
const filterToDate = ref('')

const showDetailModal = ref(false)
const activeDetailItem = ref(null)

const showHistoryModal = ref(false)
const historyOrderId = ref(null)
const orderHistory = ref([])
const loadingHistory = ref(false)

const showReturnDetailModal = ref(false)
const activeReturnOrderId = ref(null)
const returnDetailData = ref({ lyDo: '', moTaChiTiet: '' })
const loadingReturnDetail = ref(false)

const isPosOrder = (item) => {
  const address = item.donHang.diaChiGiao || ''
  return address.toLowerCase().includes('quầy') || address.toLowerCase().includes('pos')
}

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

const filteredOrders = computed(() => {
  let list = danhSachDonHang.value

  if (currentTab.value === 'online') {
    list = list.filter((item) => !isPosOrder(item) && item.donHang.nguoiDung)
  } else if (currentTab.value === 'return') {
    list = list.filter((item) => item.donHang.trangThai === 'YEU_CAU_TRA_HANG')
  } else if (currentTab.value === 'null-user') {
    list = list.filter((item) => isPosOrder(item) || !item.donHang.nguoiDung)
  }

  if (filterFromDate.value || filterToDate.value) {
    list = list.filter((item) => {
      const raw = item.donHang?.ngayDat
      if (!raw) return false
      const d = new Date(raw)
      if (Number.isNaN(d.getTime())) return false
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const key = `${y}-${m}-${day}`
      if (filterFromDate.value && key < filterFromDate.value) return false
      if (filterToDate.value && key > filterToDate.value) return false
      return true
    })
  }

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

const xemChiTietDonHang = (item) => {
  activeDetailItem.value = item
  showDetailModal.value = true
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

const getVoucherCode = (item) => {
  const order = item?.donHang || {}
  const rawVoucher = order.maVoucher || order.maGiamGia || order.voucher || order.maCode || null

  if (!rawVoucher) return null

  // Nếu backend trả về dạng object hoặc chuỗi JSON
  if (typeof rawVoucher === 'object') {
    return rawVoucher.ma || rawVoucher.ten || rawVoucher.code || 'Mã giảm giá'
  }

  if (typeof rawVoucher === 'string') {
    // Nếu chuỗi có dạng JSON (bắt đầu bằng dấu ngoặc nhọn)
    if (rawVoucher.trim().startsWith('{')) {
      try {
        const parsed = JSON.parse(rawVoucher)
        return parsed.ma || parsed.ten || parsed.code || 'Mã giảm giá'
      } catch (e) {
        return rawVoucher
      }
    }
    return rawVoucher
  }

  return null
}

const getTamTinh = (item) => {
  const details = item?.chiTietDonHangs || []
  const order = item?.donHang || {}
  const sumItems = details.reduce(
    (sum, d) => sum + Number(d.soLuong || 0) * Number(d.donGia || 0),
    0,
  )
  return Number(order.tongTienHang || 0) || sumItems
}

const getTienGiam = (item) => {
  const order = item?.donHang || {}
  let tienGiam = Number(order.tienGiam || 0)
  if (tienGiam <= 0) {
    const tamTinh = getTamTinh(item)
    const phiShip = Number(order.phiVanChuyen || 0)
    const tongThanhToan = Number(order.tongThanhToan || 0)
    const inferred = tamTinh + phiShip - tongThanhToan
    if (inferred > 0.5) tienGiam = inferred
  }
  return tienGiam
}

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

  const maGiamGia = getVoucherCode(item)
  const phiShip = Number(order.phiVanChuyen || 0)
  const tamTinh = getTamTinh(item)
  const tongThanhToan = Number(order.tongThanhToan || 0)
  const tienGiam = getTienGiam(item)

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

.checkout-style-modal {
  width: 92%;
  max-width: 900px;
  max-height: 85vh;
  overflow-y: auto;
}
</style>
