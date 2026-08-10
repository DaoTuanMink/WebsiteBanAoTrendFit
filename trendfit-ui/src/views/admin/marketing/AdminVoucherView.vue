<template>
  <div class="container-fluid py-4 position-relative">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
      <div>
        <h4 class="fw-bold mb-1">Quản lý mã giảm giá</h4>
        <p class="text-secondary small mb-0">Tạo và quản lý voucher / chương trình khuyến mãi</p>
      </div>
      <button v-if="!showForm" type="button" class="btn btn-primary shadow-sm" @click="moFormMoi">
        + Tạo voucher mới
      </button>
    </div>

    <!-- BỐ CỤC CHIA ĐÔI MÀN HÌNH -->
    <div class="row g-4 align-items-start position-relative">
      <!-- BÊN TRÁI: BẢNG DANH SÁCH VOUCHER & BỘ LỌC -->
      <div :class="showForm ? 'col-lg-7' : 'col-12'" class="transition-all">
        <div class="card border-0 shadow-sm overflow-hidden">
          <div
            class="card-header bg-white border-0 border-bottom d-flex justify-content-between align-items-center flex-wrap gap-2 py-3"
          >
            <span class="fw-semibold">Danh sách voucher ({{ filteredVouchers.length }})</span>

            <!-- THANH BỘ LỌC & SẮP XẾP -->
            <div class="d-flex align-items-center flex-wrap gap-2">
              <input
                v-model="searchKeyword"
                type="text"
                class="form-control form-control-sm"
                placeholder="Tìm mã / tên..."
                style="width: 140px"
              />
              <select
                v-model="filterStatus"
                class="form-select form-select-sm"
                style="width: 130px"
              >
                <option value="all">Tất cả trạng thái</option>
                <option value="active">Đang hoạt động</option>
                <option value="locked">Đã khóa</option>
              </select>
              <select v-model="sortBy" class="form-select form-select-sm" style="width: 150px">
                <option value="id-desc">Mới nhất (ID)</option>
                <option value="id-asc">Cũ nhất (ID)</option>
                <option value="value-desc">Giảm: Cao đến thấp</option>
                <option value="value-asc">Giảm: Thấp đến cao</option>
              </select>
            </div>
          </div>

          <div class="table-responsive custom-table-scroll">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light sticky-header text-center">
                <tr>
                  <th style="width: 50px">STT</th>
                  <th>Mã code</th>
                  <th class="text-start">Tên chương trình</th>
                  <th>Loại & giá trị</th>
                  <th>Giảm tối đa</th>
                  <th>Đơn tối thiểu</th>
                  <th>Thời hạn</th>
                  <th>Đã dùng / giới hạn</th>
                  <th>Trạng thái</th>
                  <th style="width: 130px">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(v, index) in filteredVouchers"
                  :key="v.id"
                  :class="{ 'table-active': showForm && form.id === v.id }"
                >
                  <td class="text-center text-muted small">{{ index + 1 }}</td>
                  <td class="fw-bold text-primary text-center">{{ v.ma }}</td>
                  <td class="text-start text-dark fw-semibold">{{ v.ten }}</td>
                  <td class="text-center">
                    <span class="badge bg-info text-dark">
                      {{ v.loai === 'PERCENT' ? v.giaTriGiam + '%' : formatCurrency(v.giaTriGiam) }}
                    </span>
                  </td>
                  <td class="text-end small">
                    {{ v.giaTriToiDa ? formatCurrency(v.giaTriToiDa) : 'Không giới hạn' }}
                  </td>
                  <td class="text-end small">
                    {{ v.donHangToiThieu ? formatCurrency(v.donHangToiThieu) : '0 đ' }}
                  </td>
                  <td class="small text-center text-muted">
                    {{ v.ngayBatDau || '—' }} <br />đến<br />
                    {{ v.ngayKetThuc || '—' }}
                  </td>
                  <td class="text-center small">
                    <span class="fw-semibold">{{ v.soLanDaDung || 0 }}</span>
                    /
                    {{
                      v.gioiHanSuDung !== null && v.gioiHanSuDung !== undefined
                        ? v.gioiHanSuDung
                        : '∞'
                    }}
                  </td>
                  <td class="text-center">
                    <button
                      type="button"
                      class="btn btn-sm py-0 px-2 font-xs"
                      :class="v.dangHoatDong ? 'btn-success' : 'btn-secondary'"
                      @click="toggleStatus(v)"
                    >
                      {{ v.dangHoatDong ? 'Hoạt động' : 'Đã khóa' }}
                    </button>
                  </td>
                  <td class="text-center">
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-warning me-1 py-0 px-2"
                      @click="editVoucher(v)"
                    >
                      Sửa
                    </button>
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-danger py-0 px-2"
                      @click="xoaVoucher(v.id)"
                    >
                      Xóa
                    </button>
                  </td>
                </tr>
                <tr v-if="filteredVouchers.length === 0">
                  <td colspan="10" class="text-center py-4 text-muted">
                    Không tìm thấy mã giảm giá phù hợp.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- BÊN PHẢI: FORM THÊM / SỬA GHIM CỐ ĐỊNH -->
      <div v-if="showForm" class="col-lg-5">
        <div class="card border-0 shadow-sm always-fixed-form">
          <div class="card-body p-4">
            <div class="d-flex justify-content-between align-items-center mb-3 border-bottom pb-2">
              <h5 class="fw-bold m-0 text-primary">
                {{ editMode ? 'Cập nhật voucher #' + form.id : 'Tạo voucher mới' }}
              </h5>
              <button type="button" class="btn-close" @click="showForm = false"></button>
            </div>

            <div class="row g-3">
              <div class="col-12">
                <label class="form-label small fw-semibold">Mã code (*)</label>
                <input
                  v-model="form.ma"
                  class="form-control form-control-sm"
                  placeholder="VD: SALE50K"
                />
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">Tên chương trình</label>
                <input
                  v-model="form.ten"
                  class="form-control form-control-sm"
                  placeholder="VD: Siêu sale hè 2026"
                />
              </div>
              <div class="col-6">
                <label class="form-label small fw-semibold">Loại giảm giá</label>
                <select v-model="form.loai" class="form-select form-select-sm">
                  <option value="PERCENT">Phần trăm (%)</option>
                  <option value="FIXED">Số tiền (đ)</option>
                </select>
              </div>
              <div class="col-6">
                <label class="form-label small fw-semibold">Giá trị giảm (*)</label>
                <input
                  v-model.number="form.giaTriGiam"
                  type="number"
                  class="form-control form-control-sm"
                  placeholder="10 hoặc 50000"
                />
              </div>
              <div class="col-6">
                <label class="form-label small fw-semibold">Giới hạn lượt dùng</label>
                <input
                  v-model.number="form.gioiHanSuDung"
                  type="number"
                  class="form-control form-control-sm"
                  placeholder="Trống = vô hạn"
                />
              </div>
              <div class="col-6">
                <label class="form-label small fw-semibold">Giảm tối đa (cho %)</label>
                <input
                  v-model.number="form.giaTriToiDa"
                  type="number"
                  class="form-control form-control-sm"
                  placeholder="VD: 100000"
                />
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">Đơn tối thiểu</label>
                <input
                  v-model.number="form.donHangToiThieu"
                  type="number"
                  class="form-control form-control-sm"
                  placeholder="VD: 200000"
                />
              </div>
              <div class="col-6">
                <label class="form-label small fw-semibold">Ngày bắt đầu</label>
                <input v-model="form.ngayBatDau" type="date" class="form-control form-control-sm" />
              </div>
              <div class="col-6">
                <label class="form-label small fw-semibold">Ngày kết thúc</label>
                <input
                  v-model="form.ngayKetThuc"
                  type="date"
                  class="form-control form-control-sm"
                />
              </div>
            </div>

            <div class="d-flex gap-2 mt-4 pt-3 border-top">
              <button type="button" class="btn btn-primary btn-sm flex-grow-1" @click="saveVoucher">
                Lưu voucher
              </button>
              <button
                type="button"
                class="btn btn-outline-secondary btn-sm"
                @click="showForm = false"
              >
                Hủy
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- NÚT CUỘN VỀ ĐẦU TRANG Ở GÓC DƯỚI BÊN PHẢI -->
    <button
      v-show="showScrollTopBtn"
      type="button"
      class="btn btn-dark shadow rounded-circle scroll-top-btn"
      @click="scrollToTop"
      title="Về đầu trang"
    >
      ↑
    </button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import { getAuthHeaders } from '@/utils/adminAuth'

const API = 'http://localhost:8080/api/admin/vouchers'

const apiAdmin = axios.create()
apiAdmin.interceptors.request.use((config) => {
  config.headers = { ...config.headers, ...getAuthHeaders() }
  return config
})

const vouchers = ref([])
const showForm = ref(false)
const editMode = ref(false)
const form = ref({})
const showScrollTopBtn = ref(false)

// State cho bộ lọc & sắp xếp
const searchKeyword = ref('')
const filterStatus = ref('all') // 'all', 'active', 'locked'
const sortBy = ref('id-desc') // 'id-desc', 'id-asc', 'value-desc', 'value-asc'

const formatCurrency = (val) => {
  if (!val) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

// Danh sách sau khi lọc và sắp xếp
const filteredVouchers = computed(() => {
  let list = [...vouchers.value]

  // 1. Lọc theo từ khóa (Mã code hoặc Tên chương trình)
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase().trim()
    list = list.filter(
      (v) =>
        (v.ma && v.ma.toLowerCase().includes(keyword)) ||
        (v.ten && v.ten.toLowerCase().includes(keyword)),
    )
  }

  // 2. Lọc theo trạng thái
  if (filterStatus.value === 'active') {
    list = list.filter((v) => v.dangHoatDong === true)
  } else if (filterStatus.value === 'locked') {
    list = list.filter((v) => v.dangHoatDong === false)
  }

  // 3. Sắp xếp
  if (sortBy.value === 'id-desc') {
    list.sort((a, b) => (b.id || 0) - (a.id || 0))
  } else if (sortBy.value === 'id-asc') {
    list.sort((a, b) => (a.id || 0) - (b.id || 0))
  } else if (sortBy.value === 'value-desc') {
    list.sort((a, b) => (b.giaTriGiam || 0) - (a.giaTriGiam || 0))
  } else if (sortBy.value === 'value-asc') {
    list.sort((a, b) => (a.giaTriGiam || 0) - (b.giaTriGiam || 0))
  }

  return list
})

const handleScroll = () => {
  showScrollTopBtn.value = window.scrollY > 300
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const loadVouchers = async () => {
  try {
    const res = await apiAdmin.get(API)
    vouchers.value = res.data
  } catch (err) {
    alert('Không thể tải danh sách voucher: ' + (err.response?.data || err.message))
  }
}

const moFormMoi = () => {
  form.value = {
    ma: '',
    ten: '',
    loai: 'PERCENT',
    giaTriGiam: null,
    giaTriToiDa: null,
    donHangToiThieu: null,
    gioiHanSuDung: null,
    ngayBatDau: '',
    ngayKetThuc: '',
    dangHoatDong: true,
  }
  editMode.value = false
  showForm.value = true
}

const editVoucher = (v) => {
  form.value = { ...v }
  editMode.value = true
  showForm.value = true
}

const toggleStatus = async (v) => {
  try {
    const updated = { ...v, dangHoatDong: !v.dangHoatDong }
    await apiAdmin.put(`${API}/${v.id}`, updated)
    v.dangHoatDong = updated.dangHoatDong
  } catch (err) {
    alert('Không thể đổi trạng thái: ' + (err.response?.data?.message || err.message))
  }
}

const xoaVoucher = async (id) => {
  if (confirm('Bạn có chắc chắn muốn xóa voucher này?')) {
    try {
      await apiAdmin.delete(`${API}/${id}`)
      loadVouchers()
    } catch (err) {
      alert('Xóa thất bại: ' + (err.response?.data || err.message))
    }
  }
}

const saveVoucher = async () => {
  if (!form.value.ma || form.value.giaTriGiam === null || form.value.giaTriGiam === undefined) {
    alert('Vui lòng nhập đủ Mã Code và Giá trị giảm!')
    return
  }

  try {
    if (editMode.value) {
      await apiAdmin.put(`${API}/${form.value.id}`, form.value)
    } else {
      await apiAdmin.post(API, form.value)
    }
    alert('Lưu voucher thành công!')
    showForm.value = false
    loadVouchers()
  } catch (e) {
    alert('Lỗi lưu voucher: ' + (e.response?.data?.message || 'Có lỗi xảy ra'))
  }
}

onMounted(() => {
  loadVouchers()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.table th,
.table td {
  vertical-align: middle;
}
.transition-all {
  transition: all 0.3s ease;
}

/* Giới hạn chiều cao bảng và có thanh cuộn riêng */
.custom-table-scroll {
  max-height: 520px;
  overflow-y: auto;
}

/* Cố định tiêu đề bảng khi cuộn dọc danh sách */
.sticky-header th {
  position: sticky;
  top: 0;
  z-index: 10;
  background-color: #f8f9fa !important;
  box-shadow: inset 0 -1px 0 #dee2e6;
}

.custom-table-scroll::-webkit-scrollbar {
  width: 6px;
}
.custom-table-scroll::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 4px;
}

/* Ghim cố định form bên phải */
.always-fixed-form {
  position: fixed;
  top: 20px;
  right: 25px;
  width: 40%;
  max-height: calc(100vh - 40px);
  overflow-y: auto;
  z-index: 1020;
  box-shadow: 0 1rem 3rem rgba(0, 0, 0, 0.175) !important;
  background-color: #ffffff;
}

.always-fixed-form::-webkit-scrollbar {
  width: 6px;
}
.always-fixed-form::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 4px;
}

/* Nút cuộn về đầu trang ở góc dưới bên phải */
.scroll-top-btn {
  position: fixed;
  bottom: 25px;
  right: 25px;
  width: 45px;
  height: 45px;
  font-size: 20px;
  z-index: 1030;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.scroll-top-btn:hover {
  transform: translateY(-3px);
}

@media (max-width: 991.98px) {
  .always-fixed-form {
    position: relative;
    top: 0;
    right: 0;
    width: 100%;
  }
}
</style>
