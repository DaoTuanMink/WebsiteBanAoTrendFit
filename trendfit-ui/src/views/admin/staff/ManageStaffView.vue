<template>
  <div class="container-fluid py-4 position-relative">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
      <div>
        <h4 class="fw-bold mb-1">Quản lý nhân viên</h4>
        <p class="text-secondary small mb-0">Tạo tài khoản và quản lý nhân viên cửa hàng</p>
      </div>
      <button
        v-if="!isEditing && !showForm"
        type="button"
        class="btn btn-primary shadow-sm"
        @click="moFormMoi"
      >
        + Tạo tài khoản nhân viên
      </button>
    </div>

    <!-- Thông báo lỗi chung nếu có -->
    <div v-if="errorMsg" class="alert alert-danger py-2 mb-4">{{ errorMsg }}</div>

    <!-- BỐ CỤC CHIA ĐÔI MÀN HÌNH -->
    <div class="row g-4 align-items-start position-relative">
      <!-- BÊN TRÁI: BẢNG DANH SÁCH NHÂN VIÊN & BỘ LỌC -->
      <div :class="isEditing || showForm ? 'col-lg-7' : 'col-12'" class="transition-all">
        <div class="card border-0 shadow-sm overflow-hidden">
          <div
            class="card-header bg-white border-0 border-bottom d-flex justify-content-between align-items-center flex-wrap gap-2 py-3"
          >
            <span class="fw-semibold">Danh sách nhân viên ({{ filteredStaffList.length }})</span>

            <!-- THANH TÌM KIẾM & LÀM MỚI -->
            <div class="d-flex align-items-center gap-2">
              <input
                v-model="searchQuery"
                type="text"
                class="form-control form-control-sm"
                placeholder="Tìm mã NV..."
                style="width: 160px"
                @input="searchStaff"
              />
              <button
                type="button"
                class="btn btn-outline-secondary btn-sm text-nowrap"
                @click="fetchStaff"
              >
                Làm mới
              </button>
            </div>
          </div>

          <div v-if="loading" class="text-center py-5">
            <div class="spinner-border text-primary" role="status"></div>
          </div>

          <div v-else class="table-responsive custom-table-scroll">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light sticky-header">
                <tr>
                  <th style="width: 50px" class="text-center">STT</th>
                  <th>Mã NV</th>
                  <th>Họ tên</th>
                  <th>Email</th>
                  <th>Số ĐT</th>
                  <th class="text-center">Trạng thái</th>
                  <th>Ngày vào làm</th>
                  <th class="text-center" style="width: 130px">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="filteredStaffList.length === 0">
                  <td colspan="8" class="text-center text-muted py-4">Chưa có nhân viên nào</td>
                </tr>
                <tr
                  v-for="(nv, index) in filteredStaffList"
                  :key="nv.id"
                  :class="{ 'table-active': isEditing && editingId === nv.id }"
                >
                  <td class="text-center text-muted small">{{ index + 1 }}</td>
                  <td class="fw-semibold">#{{ nv.maNhanVien }}</td>
                  <td class="fw-semibold text-dark">{{ nv.nguoiDung?.hoTen }}</td>
                  <td class="text-secondary small">{{ nv.nguoiDung?.email }}</td>
                  <td>{{ nv.nguoiDung?.soDienThoai || '—' }}</td>
                  <td class="text-center">
                    <span
                      class="badge py-1 px-2 font-xs"
                      :class="nv.nguoiDung?.dangHoatDong ? 'bg-success' : 'bg-secondary'"
                    >
                      {{ nv.nguoiDung?.dangHoatDong ? 'Hoạt động' : 'Khóa' }}
                    </span>
                  </td>
                  <td class="small text-muted">{{ formatDate(nv.ngayVaoLam) }}</td>
                  <td class="text-center">
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-warning me-1 py-0 px-2"
                      @click="editStaff(nv)"
                    >
                      Sửa
                    </button>
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-danger py-0 px-2"
                      @click="deleteStaff(nv.id)"
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

      <!-- BÊN PHẢI: FORM THÊM / SỬA GHIM CỐ ĐỊNH -->
      <div v-if="isEditing || showForm" class="col-lg-5">
        <div class="card border-0 shadow-sm always-fixed-form">
          <div class="card-body p-4">
            <div class="d-flex justify-content-between align-items-center mb-3 border-bottom pb-2">
              <h5 class="fw-bold m-0 text-primary">
                {{ isEditing ? 'Cập nhật nhân viên #' + editingId : 'Tạo tài khoản nhân viên' }}
              </h5>
              <button type="button" class="btn-close" @click="resetForm"></button>
            </div>

            <div class="row g-3">
              <div class="col-12">
                <label class="form-label small fw-semibold">Họ tên (*)</label>
                <input
                  v-model.trim="newStaff.hoTen"
                  class="form-control form-control-sm"
                  placeholder="Nhập họ tên..."
                />
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">Email (*)</label>
                <input
                  v-model.trim="newStaff.email"
                  type="email"
                  class="form-control form-control-sm"
                  placeholder="email@example.com"
                  :disabled="isEditing"
                />
              </div>
              <div class="col-12" v-if="!isEditing">
                <label class="form-label small fw-semibold">Mật khẩu (*)</label>
                <input
                  v-model="newStaff.matKhau"
                  type="password"
                  class="form-control form-control-sm"
                  placeholder="Mật khẩu đăng nhập"
                />
              </div>
              <div class="col-6">
                <label class="form-label small fw-semibold">Mã NV (*)</label>
                <input
                  v-model.trim="newStaff.maNhanVien"
                  class="form-control form-control-sm"
                  placeholder="VD: NV01"
                />
              </div>
              <div class="col-6">
                <label class="form-label small fw-semibold">Số ĐT</label>
                <input
                  v-model.trim="newStaff.soDienThoai"
                  class="form-control form-control-sm"
                  placeholder="0987654321"
                />
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">Ngày vào làm</label>
                <input
                  v-model="newStaff.ngayVaoLam"
                  type="date"
                  class="form-control form-control-sm"
                />
              </div>
            </div>

            <small class="text-muted d-block mt-3">(*) Các trường bắt buộc nhập</small>

            <div class="d-flex gap-2 mt-4 pt-3 border-top">
              <button
                type="button"
                class="btn btn-primary btn-sm flex-grow-1"
                :disabled="submitting"
                @click="isEditing ? updateStaff() : createStaff()"
              >
                {{ submitting ? 'Đang xử lý...' : isEditing ? 'Cập nhật' : 'Tạo mới' }}
              </button>
              <button type="button" class="btn btn-outline-secondary btn-sm" @click="resetForm">
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

const API_URL = 'http://localhost:8080/api/admin/users'

const staffList = ref([])
const searchQuery = ref('')
const isEditing = ref(false)
const showForm = ref(false)
const editingId = ref(null)
const loading = ref(false)
const submitting = ref(false)
const errorMsg = ref('')
const showScrollTopBtn = ref(false)

const newStaff = ref({
  hoTen: '',
  email: '',
  matKhau: '',
  maNhanVien: '',
  chucVu: 'NV',
  soDienThoai: '',
  ngayVaoLam: '',
})

// Lọc danh sách nhân viên theo từ khóa tìm kiếm
const filteredStaffList = computed(() => {
  if (!searchQuery.value.trim()) return staffList.value
  const keyword = searchQuery.value.toLowerCase().trim()
  return staffList.value.filter(
    (nv) =>
      (nv.maNhanVien && nv.maNhanVien.toLowerCase().includes(keyword)) ||
      (nv.nguoiDung?.hoTen && nv.nguoiDung.hoTen.toLowerCase().includes(keyword)) ||
      (nv.nguoiDung?.email && nv.nguoiDung.email.toLowerCase().includes(keyword)),
  )
})

const handleScroll = () => {
  showScrollTopBtn.value = window.scrollY > 300
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const fetchStaff = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await axios.get(`${API_URL}/staff`, { headers: getAuthHeaders() })
    staffList.value = res.data
  } catch (err) {
    errorMsg.value = layThongBaoLoi(err, 'Không thể tải danh sách nhân viên')
  } finally {
    loading.value = false
  }
}

const validateForm = () => {
  if (!newStaff.value.hoTen) return 'Vui lòng nhập họ tên'
  if (!isEditing.value && !newStaff.value.email) return 'Vui lòng nhập email'
  if (!isEditing.value && !newStaff.value.matKhau) return 'Vui lòng nhập mật khẩu'
  if (!newStaff.value.maNhanVien) return 'Vui lòng nhập mã nhân viên'
  return null
}

const moFormMoi = () => {
  resetForm()
  showForm.value = true
}

const createStaff = async () => {
  errorMsg.value = ''
  const loi = validateForm()
  if (loi) {
    errorMsg.value = loi
    return
  }

  submitting.value = true
  try {
    await axios.post(`${API_URL}/create-staff`, newStaff.value, { headers: getAuthHeaders() })
    alert('✅ Đã tạo nhân viên thành công!')
    resetForm()
    fetchStaff()
  } catch (err) {
    errorMsg.value = layThongBaoLoi(err, 'Tạo nhân viên thất bại')
  } finally {
    submitting.value = false
  }
}

const editStaff = (nv) => {
  errorMsg.value = ''
  isEditing.value = true
  showForm.value = false
  editingId.value = nv.id
  newStaff.value = {
    hoTen: nv.nguoiDung?.hoTen || '',
    email: nv.nguoiDung?.email || '',
    maNhanVien: nv.maNhanVien || '',
    chucVu: nv.chucVu || 'NV',
    soDienThoai: nv.nguoiDung?.soDienThoai || '',
    ngayVaoLam: nv.ngayVaoLam || '',
  }
}

const updateStaff = async () => {
  errorMsg.value = ''
  const loi = validateForm()
  if (loi) {
    errorMsg.value = loi
    return
  }

  submitting.value = true
  try {
    await axios.put(`${API_URL}/${editingId.value}`, newStaff.value, { headers: getAuthHeaders() })
    alert('✅ Đã cập nhật!')
    resetForm()
    fetchStaff()
  } catch (err) {
    errorMsg.value = layThongBaoLoi(err, 'Cập nhật thất bại')
  } finally {
    submitting.value = false
  }
}

const deleteStaff = async (id) => {
  if (!confirm('Bạn chắc chắn muốn xóa nhân viên này?')) return
  try {
    await axios.delete(`${API_URL}/${id}`, { headers: getAuthHeaders() })
    fetchStaff()
  } catch (err) {
    alert(layThongBaoLoi(err, 'Xóa thất bại'))
  }
}

const searchStaff = () => {
  // Tìm kiếm cục bộ mượt mà qua computed property filteredStaffList
}

const resetForm = () => {
  newStaff.value = {
    hoTen: '',
    email: '',
    matKhau: '',
    maNhanVien: '',
    chucVu: 'NV',
    soDienThoai: '',
    ngayVaoLam: '',
  }
  isEditing.value = false
  showForm.value = false
  editingId.value = null
  errorMsg.value = ''
}

const formatDate = (dateString) => {
  if (!dateString) return 'Chưa rõ'
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN')
}

const layThongBaoLoi = (err, macDinh) => {
  const data = err?.response?.data
  if (typeof data === 'string' && data.trim()) return data
  if (data?.message) return data.message
  return macDinh + (err?.message ? `: ${err.message}` : '')
}

onMounted(() => {
  fetchStaff()
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
