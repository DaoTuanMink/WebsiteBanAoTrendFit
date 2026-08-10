<template>
  <div class="container-fluid py-4">
    <!-- Header chung -->
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
      <div>
        <h4 class="fw-bold mb-1">Quản lý kích cỡ & màu sắc</h4>
        <p class="text-secondary small mb-0">Quản lý thuộc tính biến thể sản phẩm</p>
      </div>
    </div>

    <!-- Thông báo lỗi chung nếu có -->
    <div v-if="errorMsg" class="alert alert-danger py-2 mb-4">{{ errorMsg }}</div>

    <!-- BỐ CỤC CHIA ĐÔI MÀN HÌNH (TRÁI: KÍCH CỠ | PHẢI: MÀU SẮC) -->
    <div class="row g-4 align-items-start">
      <!-- ================= 1. QUẢN LÝ KÍCH CỠ (BÊN TRÁI) ================= -->
      <div class="col-lg-6">
        <!-- Form Thêm / Sửa Kích Cỡ -->
        <div class="card border-0 shadow-sm mb-4">
          <div class="card-header bg-white border-0 border-bottom py-3">
            <h6 class="fw-bold m-0 text-primary">
              {{ sizeForm.id ? 'Sửa kích cỡ #' + sizeForm.id : '+ Thêm kích cỡ mới' }}
            </h6>
          </div>
          <div class="card-body">
            <div class="row g-3 align-items-end">
              <div class="col-sm-8">
                <label class="form-label small fw-semibold">Tên kích cỡ</label>
                <input
                  v-model.trim="sizeForm.tenKichCo"
                  class="form-control form-control-sm"
                  placeholder="Nhập kích cỡ mới (S, M, L...)"
                  @keyup.enter="saveSize"
                />
              </div>
              <div class="col-sm-4 d-flex gap-2">
                <button
                  type="button"
                  class="btn btn-primary btn-sm flex-grow-1"
                  :disabled="sizeSubmitting"
                  @click="saveSize"
                >
                  {{ sizeForm.id ? 'Cập nhật' : 'Thêm' }}
                </button>
                <button
                  v-if="sizeForm.id"
                  type="button"
                  class="btn btn-outline-secondary btn-sm"
                  @click="resetSizeForm"
                >
                  Hủy
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Bảng danh sách Kích cỡ -->
        <div class="card border-0 shadow-sm overflow-hidden">
          <div
            class="card-header bg-white border-0 border-bottom d-flex justify-content-between align-items-center flex-wrap gap-2 py-3"
          >
            <span class="fw-semibold">Danh sách kích cỡ ({{ sortedSizes.length }})</span>
            <div class="d-flex align-items-center gap-2">
              <label class="small text-secondary text-nowrap mb-0">Sắp xếp:</label>
              <select v-model="sizeSortBy" class="form-select form-select-sm" style="width: 150px">
                <option value="id-asc">ID: Cũ đến mới</option>
                <option value="id-desc">ID: Mới đến cũ</option>
                <option value="name-asc">Tên: A - Z</option>
                <option value="name-desc">Tên: Z - A</option>
              </select>
            </div>
          </div>
          <div class="table-responsive custom-table-scroll">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light sticky-header">
                <tr>
                  <th style="width: 50px" class="text-center">STT</th>
                  <th style="width: 70px">ID</th>
                  <th>Tên kích cỡ</th>
                  <th class="text-center" style="width: 130px">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="sortedSizes.length === 0">
                  <td colspan="4" class="text-center text-muted py-4">Chưa có kích cỡ nào</td>
                </tr>
                <tr v-for="(size, index) in sortedSizes" :key="size.id">
                  <td class="text-center text-muted small">{{ index + 1 }}</td>
                  <td class="fw-semibold">#{{ size.id }}</td>
                  <td>{{ size.tenKichCo }}</td>
                  <td class="text-center">
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-warning me-1"
                      @click="kichHoatSuaSize(size)"
                    >
                      Sửa
                    </button>
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-danger"
                      @click="deleteSize(size.id)"
                    >
                      Xóa
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- ================= 2. QUẢN LÝ MÀU SẮC (BÊN PHẢI) ================= -->
      <div class="col-lg-6">
        <!-- Form Thêm / Sửa Màu Sắc -->
        <div class="card border-0 shadow-sm mb-4">
          <div class="card-header bg-white border-0 border-bottom py-3">
            <h6 class="fw-bold m-0 text-success">
              {{ colorForm.id ? 'Sửa màu sắc #' + colorForm.id : '+ Thêm màu sắc mới' }}
            </h6>
          </div>
          <div class="card-body">
            <div class="row g-3 align-items-end">
              <div class="col-sm-6">
                <label class="form-label small fw-semibold">Tên màu sắc</label>
                <input
                  v-model.trim="colorForm.tenMau"
                  class="form-control form-control-sm"
                  placeholder="Nhập tên màu (Đỏ, Xanh...)"
                  @keyup.enter="saveColor"
                />
              </div>
              <div class="col-sm-3">
                <label class="form-label small fw-semibold">Mã màu (Hex)</label>
                <input
                  v-model.trim="colorForm.maMau"
                  type="color"
                  class="form-control form-control-sm form-control-color w-100"
                  title="Chọn mã màu"
                />
              </div>
              <div class="col-sm-3 d-flex gap-2">
                <button
                  type="button"
                  class="btn btn-success btn-sm flex-grow-1"
                  :disabled="colorSubmitting"
                  @click="saveColor"
                >
                  {{ colorForm.id ? 'Cập nhật' : 'Thêm' }}
                </button>
                <button
                  v-if="colorForm.id"
                  type="button"
                  class="btn btn-outline-secondary btn-sm"
                  @click="resetColorForm"
                >
                  Hủy
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Bảng danh sách Màu sắc -->
        <div class="card border-0 shadow-sm overflow-hidden">
          <div
            class="card-header bg-white border-0 border-bottom d-flex justify-content-between align-items-center flex-wrap gap-2 py-3"
          >
            <span class="fw-semibold">Danh sách màu sắc ({{ sortedColors.length }})</span>
            <div class="d-flex align-items-center gap-2">
              <label class="small text-secondary text-nowrap mb-0">Sắp xếp:</label>
              <select v-model="colorSortBy" class="form-select form-select-sm" style="width: 150px">
                <option value="id-asc">ID: Cũ đến mới</option>
                <option value="id-desc">ID: Mới đến cũ</option>
                <option value="name-asc">Tên: A - Z</option>
                <option value="name-desc">Tên: Z - A</option>
              </select>
            </div>
          </div>
          <div class="table-responsive custom-table-scroll">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light sticky-header">
                <tr>
                  <th style="width: 50px" class="text-center">STT</th>
                  <th style="width: 70px">ID</th>
                  <th>Tên màu</th>
                  <th>Mã màu</th>
                  <th class="text-center" style="width: 130px">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="sortedColors.length === 0">
                  <td colspan="5" class="text-center text-muted py-4">Chưa có màu sắc nào</td>
                </tr>
                <tr v-for="(color, index) in sortedColors" :key="color.id">
                  <td class="text-center text-muted small">{{ index + 1 }}</td>
                  <td class="fw-semibold">#{{ color.id }}</td>
                  <td>{{ color.tenMau }}</td>
                  <td>
                    <span
                      class="d-inline-block rounded border align-middle me-1"
                      :style="{
                        background: color.maMau || '#ccc',
                        width: '24px',
                        height: '18px',
                      }"
                      :title="color.maMau"
                    ></span>
                    <small class="text-muted">{{ color.maMau || '—' }}</small>
                  </td>
                  <td class="text-center">
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-warning me-1"
                      @click="kichHoatSuaColor(color)"
                    >
                      Sửa
                    </button>
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-danger"
                      @click="deleteColor(color.id)"
                    >
                      Xóa
                    </button>
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
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { getAuthHeaders } from '@/utils/adminAuth'

const API_BASE = 'http://localhost:8080/api/admin'

// State cho Kích cỡ
const sizes = ref([])
const sizeForm = ref({ id: null, tenKichCo: '' })
const sizeSubmitting = ref(false)
const sizeSortBy = ref('id-asc')

// State cho Màu sắc
const colors = ref([])
const colorForm = ref({ id: null, tenMau: '', maMau: '#000000' })
const colorSubmitting = ref(false)
const colorSortBy = ref('id-asc')

const errorMsg = ref('')

const layThongBaoLoi = (err, macDinh) => {
  const data = err?.response?.data
  if (typeof data === 'string' && data.trim()) return data
  return macDinh + (err?.message ? `: ${err.message}` : '')
}

const loadSizes = async () => {
  try {
    const res = await axios.get(`${API_BASE}/products/sizes`, { headers: getAuthHeaders() })
    sizes.value = res.data
  } catch (err) {
    errorMsg.value = layThongBaoLoi(err, 'Không thể tải danh sách kích cỡ')
  }
}

const loadColors = async () => {
  try {
    const res = await axios.get(`${API_BASE}/products/colors`, { headers: getAuthHeaders() })
    colors.value = res.data
  } catch (err) {
    errorMsg.value = layThongBaoLoi(err, 'Không thể tải danh sách màu sắc')
  }
}

// Sắp xếp kích cỡ
const sortedSizes = computed(() => {
  let list = [...sizes.value]
  if (sizeSortBy.value === 'id-asc') {
    list.sort((a, b) => (a.id || 0) - (b.id || 0))
  } else if (sizeSortBy.value === 'id-desc') {
    list.sort((a, b) => (b.id || 0) - (a.id || 0))
  } else if (sizeSortBy.value === 'name-asc') {
    list.sort((a, b) => (a.tenKichCo || '').localeCompare(b.tenKichCo || ''))
  } else if (sizeSortBy.value === 'name-desc') {
    list.sort((a, b) => (b.tenKichCo || '').localeCompare(a.tenKichCo || ''))
  }
  return list
})

// Sắp xếp màu sắc
const sortedColors = computed(() => {
  let list = [...colors.value]
  if (colorSortBy.value === 'id-asc') {
    list.sort((a, b) => (a.id || 0) - (b.id || 0))
  } else if (colorSortBy.value === 'id-desc') {
    list.sort((a, b) => (b.id || 0) - (a.id || 0))
  } else if (colorSortBy.value === 'name-asc') {
    list.sort((a, b) => (a.tenMau || '').localeCompare(b.tenMau || ''))
  } else if (colorSortBy.value === 'name-desc') {
    list.sort((a, b) => (b.tenMau || '').localeCompare(a.tenMau || ''))
  }
  return list
})

// CRUD Kích cỡ
const saveSize = async () => {
  errorMsg.value = ''
  if (!sizeForm.value.tenKichCo) {
    errorMsg.value = 'Vui lòng nhập tên kích cỡ!'
    return
  }

  sizeSubmitting.value = true
  try {
    await axios.post(`${API_BASE}/products/sizes`, sizeForm.value, { headers: getAuthHeaders() })
    resetSizeForm()
    loadSizes()
  } catch (err) {
    alert(layThongBaoLoi(err, 'Lưu kích cỡ thất bại'))
  } finally {
    sizeSubmitting.value = false
  }
}

const kichHoatSuaSize = (size) => {
  sizeForm.value = { ...size }
}

const resetSizeForm = () => {
  sizeForm.value = { id: null, tenKichCo: '' }
}

const deleteSize = async (id) => {
  if (!confirm('Xóa kích cỡ này?')) return
  try {
    await axios.delete(`${API_BASE}/products/sizes/${id}`, { headers: getAuthHeaders() })
    loadSizes()
  } catch (err) {
    alert(layThongBaoLoi(err, 'Xóa kích cỡ thất bại'))
  }
}

// CRUD Màu sắc
const saveColor = async () => {
  errorMsg.value = ''
  if (!colorForm.value.tenMau) {
    errorMsg.value = 'Vui lòng nhập tên màu sắc!'
    return
  }

  const payload = {
    id: colorForm.value.id,
    tenMau: colorForm.value.tenMau.trim(),
    maMau: colorForm.value.maMau || '#000000',
  }

  colorSubmitting.value = true
  try {
    await axios.post(`${API_BASE}/products/colors`, payload, { headers: getAuthHeaders() })
    resetColorForm()
    loadColors()
  } catch (err) {
    // Hiển thị trực tiếp câu thông báo từ backend (ví dụ: "Tên màu sắc này đã tồn tại...")
    errorMsg.value = err.response?.data || 'Lưu màu sắc thất bại'
  } finally {
    colorSubmitting.value = false
  }
}

const kichHoatSuaColor = (color) => {
  colorForm.value = { ...color }
}

const resetColorForm = () => {
  colorForm.value = { id: null, tenMau: '', maMau: '#000000' }
}

const deleteColor = async (id) => {
  if (!confirm('Xóa màu này?')) return
  try {
    await axios.delete(`${API_BASE}/products/colors/${id}`, { headers: getAuthHeaders() })
    loadColors()
  } catch (err) {
    // Lấy chính xác thông báo lỗi từ backend trả về
    const msg = err.response?.data || err.message
    alert(typeof msg === 'string' ? msg : 'Xóa màu sắc thất bại do đang được sử dụng!')
  }
}

onMounted(() => {
  loadSizes()
  loadColors()
})
</script>

<style scoped>
.table th,
.table td {
  vertical-align: middle;
}

/* Giới hạn chiều cao bảng và tạo thanh cuộn riêng biệt */
.custom-table-scroll {
  max-height: 460px;
  overflow-y: auto;
}

/* Giữ cố định header của bảng khi cuộn danh sách bên trong */
.sticky-header th {
  position: sticky;
  top: 0;
  z-index: 10;
  background-color: #f8f9fa !important;
  box-shadow: inset 0 -1px 0 #dee2e6;
}

/* Tùy chỉnh thanh cuộn gọn gàng */
.custom-table-scroll::-webkit-scrollbar {
  width: 6px;
}
.custom-table-scroll::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 4px;
}
</style>
