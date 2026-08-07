<template>
  <div class="container-fluid py-4">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
      <div>
        <h4 class="fw-bold mb-1">Quản lý nhân viên</h4>
        <p class="text-secondary small mb-0">Tạo tài khoản và quản lý nhân viên cửa hàng</p>
      </div>
    </div>

    <!-- Form -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-body">
        <h6 class="fw-bold mb-3">
          {{ isEditing ? 'Cập nhật nhân viên' : 'Tạo tài khoản nhân viên' }}
        </h6>

        <div v-if="errorMsg" class="alert alert-danger py-2">{{ errorMsg }}</div>

        <div class="row g-3">
          <div class="col-md-3">
            <label class="form-label small fw-semibold">Họ tên *</label>
            <input v-model.trim="newStaff.hoTen" class="form-control" placeholder="Họ tên" />
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-semibold">Email *</label>
            <input
              v-model.trim="newStaff.email"
              type="email"
              class="form-control"
              placeholder="Email"
              :disabled="isEditing"
            />
          </div>
          <div class="col-md-2" v-if="!isEditing">
            <label class="form-label small fw-semibold">Mật khẩu *</label>
            <input
              v-model="newStaff.matKhau"
              type="password"
              class="form-control"
              placeholder="Mật khẩu"
            />
          </div>
          <div class="col-md-2">
            <label class="form-label small fw-semibold">Mã NV *</label>
            <input
              v-model.trim="newStaff.maNhanVien"
              class="form-control"
              placeholder="Mã NV"
            />
          </div>
          <div class="col-md-2">
            <label class="form-label small fw-semibold">Số ĐT</label>
            <input
              v-model.trim="newStaff.soDienThoai"
              class="form-control"
              placeholder="Số điện thoại"
            />
          </div>
          <div class="col-md-2">
            <label class="form-label small fw-semibold">Ngày vào làm</label>
            <input v-model="newStaff.ngayVaoLam" type="date" class="form-control" />
          </div>
          <div class="col-md-auto d-flex align-items-end gap-2">
            <button
              type="button"
              class="btn btn-primary"
              :disabled="submitting"
              @click="isEditing ? updateStaff() : createStaff()"
            >
              {{ submitting ? 'Đang xử lý...' : isEditing ? 'Cập nhật' : 'Tạo mới' }}
            </button>
            <button
              v-if="isEditing"
              type="button"
              class="btn btn-outline-secondary"
              @click="resetForm"
            >
              Hủy
            </button>
          </div>
        </div>
        <small class="text-muted d-block mt-2">(*) Bắt buộc nhập</small>
      </div>
    </div>

    <!-- Search -->
    <div class="mb-3" style="max-width: 360px">
      <div class="input-group">
        <input
          v-model="searchQuery"
          class="form-control"
          placeholder="Tìm theo mã NV..."
          @input="searchStaff"
        />
        <button type="button" class="btn btn-outline-secondary" @click="fetchStaff">
          Làm mới
        </button>
      </div>
    </div>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"></div>
    </div>

    <!-- Table -->
    <div v-else class="card border-0 shadow-sm overflow-hidden">
      <div class="card-header bg-white border-0 border-bottom d-flex justify-content-between align-items-center py-3">
        <span class="fw-semibold">Danh sách nhân viên</span>
        <span class="badge text-bg-primary rounded-pill">Tổng NV: {{ staffList.length }}</span>
      </div>
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th style="width: 60px">STT</th>
              <th>Mã NV</th>
              <th>Họ tên</th>
              <th>Email</th>
              <th>Số ĐT</th>
              <th class="text-center">Trạng thái</th>
              <th>Ngày vào làm</th>
              <th class="text-center" style="width: 140px">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="staffList.length === 0">
              <td colspan="8" class="text-center text-muted py-4">Chưa có nhân viên nào</td>
            </tr>
            <tr v-for="(nv, index) in staffList" :key="nv.id">
              <td>{{ index + 1 }}</td>
              <td class="fw-semibold">{{ nv.maNhanVien }}</td>
              <td>{{ nv.nguoiDung?.hoTen }}</td>
              <td>{{ nv.nguoiDung?.email }}</td>
              <td>{{ nv.nguoiDung?.soDienThoai || '—' }}</td>
              <td class="text-center">
                <span
                  class="badge"
                  :class="nv.nguoiDung?.dangHoatDong ? 'bg-success' : 'bg-secondary'"
                >
                  {{ nv.nguoiDung?.dangHoatDong ? 'Hoạt động' : 'Khóa' }}
                </span>
              </td>
              <td>{{ formatDate(nv.ngayVaoLam) }}</td>
              <td class="text-center">
                <button
                  type="button"
                  class="btn btn-sm btn-outline-warning me-1"
                  @click="editStaff(nv)"
                >
                  Sửa
                </button>
                <button
                  type="button"
                  class="btn btn-sm btn-outline-danger"
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
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { getAuthHeaders } from '@/utils/adminAuth'

const API_URL = 'http://localhost:8080/api/admin/users'

const staffList = ref([])
const searchQuery = ref('')
const isEditing = ref(false)
const editingId = ref(null)
const loading = ref(false)
const submitting = ref(false)
const errorMsg = ref('')

const newStaff = ref({
  hoTen: '',
  email: '',
  matKhau: '',
  maNhanVien: '',
  chucVu: 'NV',
  soDienThoai: '',
  ngayVaoLam: '',
})

// Tải danh sách nhân viên. Vì "/api/admin/users/**" giờ yêu cầu quyền
// ADMIN (xem AuthInterceptor), phải gắn header xác thực ở MỌI request.
const fetchStaff = async () => {
  loading.value = true
  try {
    const res = await axios.get(`${API_URL}/staff`, { headers: getAuthHeaders() })
    staffList.value = res.data
  } catch (err) {
    errorMsg.value = layThongBaoLoi(err, 'Không thể tải danh sách nhân viên')
  } finally {
    loading.value = false
  }
}

// Kiểm tra dữ liệu bắt buộc trước khi gửi lên server, tránh gửi request
// "rỗng" khiến backend lưu bản ghi thiếu email/mã NV (đây chính là nguyên
// nhân trước đây có nhân viên bị trống cột Email/Mã NV trong bảng).
const validateForm = () => {
  if (!newStaff.value.hoTen) return 'Vui lòng nhập họ tên'
  // Email chỉ bắt buộc khi TẠO MỚI. Khi đang SỬA, ô Email bị khóa (disabled)
  // và API cập nhật (capNhatNhanVien) cũng không đụng tới email, nên KHÔNG
  // được bắt buộc nhập lại ở đây - nếu không nhân viên có email trống từ
  // trước (do lỗi phiên bản cũ) sẽ không bao giờ bấm "Cập nhật" được nữa.
  if (!isEditing.value && !newStaff.value.email) return 'Vui lòng nhập email'
  if (!isEditing.value && !newStaff.value.matKhau) return 'Vui lòng nhập mật khẩu'
  if (!newStaff.value.maNhanVien) return 'Vui lòng nhập mã nhân viên'
  return null
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
    // QUAN TRỌNG: hiển thị lỗi THẬT từ server (ví dụ email đã tồn tại) thay
    // vì im lặng không làm gì — đây là nguyên nhân khiến trước đây tưởng
    // "bấm không thêm được" trong khi thực chất backend đã báo lỗi nhưng
    // không có nơi nào hiển thị nó ra cho người dùng thấy.
    errorMsg.value = layThongBaoLoi(err, 'Tạo nhân viên thất bại')
  } finally {
    submitting.value = false
  }
}

const editStaff = (nv) => {
  errorMsg.value = ''
  isEditing.value = true
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

const searchStaff = async () => {
  if (!searchQuery.value) return fetchStaff()
  try {
    const res = await axios.get(`${API_URL}/search`, {
      params: { maNhanVien: searchQuery.value },
      headers: getAuthHeaders(),
    })
    staffList.value = res.data
  } catch (err) {
    errorMsg.value = layThongBaoLoi(err, 'Tìm kiếm thất bại')
  }
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
  editingId.value = null
  errorMsg.value = ''
}

const formatDate = (dateString) => {
  if (!dateString) return 'Chưa rõ'
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN')
}

// Rút gọn logic đọc thông báo lỗi trả về từ backend (Spring có thể trả về
// chuỗi text thuần hoặc object JSON tùy loại lỗi), kèm giá trị mặc định.
const layThongBaoLoi = (err, macDinh) => {
  const data = err?.response?.data
  if (typeof data === 'string' && data.trim()) return data
  if (data?.message) return data.message
  return macDinh + (err?.message ? `: ${err.message}` : '')
}

onMounted(fetchStaff)
</script>
