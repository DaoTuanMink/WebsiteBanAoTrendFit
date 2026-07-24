<template>
  <div class="container py-5">
    <h3 class="fw-bold mb-4">QUẢN LÝ NHÂN VIÊN</h3>

    <!-- Chỉ ADMIN mới thấy được trang này (được chặn ở router + backend),
         nên form dưới đây luôn cho phép tạo/sửa nhân viên. -->
    <div class="card p-4 mb-5 shadow-sm">
      <h5>{{ isEditing ? 'Cập nhật nhân viên' : 'Tạo tài khoản nhân viên' }}</h5>

      <!-- Khu vực hiển thị lỗi từ server (ví dụ email đã tồn tại) -->
      <div v-if="errorMsg" class="alert alert-danger py-2 mt-2 mb-0">{{ errorMsg }}</div>

      <div class="row g-3 mt-1">
        <div class="col-md-3">
          <input
            v-model.trim="newStaff.hoTen"
            placeholder="Họ tên *"
            class="form-control"
            required
          />
        </div>
        <div class="col-md-3">
          <!--
            LƯU Ý: Email BẮT BUỘC và phải DUY NHẤT trong toàn hệ thống, vì cột
            "email" ở bảng nguoi_dung có ràng buộc UNIQUE (dùng chung cho cả
            khách hàng lẫn nhân viên/admin - xem NguoiDung.java).
            Khi đang SỬA (isEditing), khoá không cho đổi email để tránh trùng
            với 1 tài khoản khác đã tồn tại; muốn đổi email hãy xoá và tạo lại.
          -->
          <input
            v-model.trim="newStaff.email"
            type="email"
            placeholder="Email *"
            class="form-control"
            :disabled="isEditing"
            required
          />
        </div>
        <div class="col-md-2" v-if="!isEditing">
          <input
            v-model="newStaff.matKhau"
            type="password"
            placeholder="Mật khẩu *"
            class="form-control"
            required
          />
        </div>
        <div class="col-md-2">
          <input
            v-model.trim="newStaff.maNhanVien"
            placeholder="Mã NV *"
            class="form-control"
            required
          />
        </div>
        <div class="col-md-2">
          <input v-model.trim="newStaff.soDienThoai" placeholder="Số ĐT" class="form-control" />
        </div>
        <div class="col-md-2">
          <input v-model="newStaff.ngayVaoLam" type="date" class="form-control" />
        </div>
        <div class="col-md-2">
          <button
            @click="isEditing ? updateStaff() : createStaff()"
            class="btn btn-dark w-100"
            :disabled="submitting"
          >
            {{ submitting ? 'Đang xử lý...' : isEditing ? 'Cập nhật' : 'Tạo mới' }}
          </button>
        </div>
        <div class="col-md-2" v-if="isEditing">
          <button @click="resetForm" class="btn btn-outline-secondary w-100">Hủy</button>
        </div>
      </div>
      <small class="text-muted d-block mt-2">(*) Bắt buộc nhập</small>
    </div>

    <div class="input-group mb-3 w-50">
      <input
        v-model="searchQuery"
        @input="searchStaff"
        placeholder="Tìm theo mã NV..."
        class="form-control"
      />
    </div>

    <div v-if="loading" class="text-center py-4">
      <div class="spinner-border" role="status"></div>
    </div>

    <table v-else class="table table-bordered table-hover align-middle">
      <thead class="table-dark">
        <tr>
          <th>STT</th>
          <th>Mã NV</th>
          <th>Họ tên</th>
          <th>Email</th>
          <th>Số ĐT</th>
          <th>Trạng thái</th>
          <th>Ngày vào làm</th>
          <th>Hành động</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="staffList.length === 0">
          <td colspan="8" class="text-center text-muted py-3">Chưa có nhân viên nào</td>
        </tr>
        <tr v-for="(nv, index) in staffList" :key="nv.id">
          <td>{{ index + 1 }}</td>
          <td>{{ nv.maNhanVien }}</td>
          <td>{{ nv.nguoiDung?.hoTen }}</td>
          <td>{{ nv.nguoiDung?.email }}</td>
          <td>{{ nv.nguoiDung?.soDienThoai }}</td>
          <td>
            <span class="badge" :class="nv.nguoiDung?.dangHoatDong ? 'bg-success' : 'bg-secondary'">
              {{ nv.nguoiDung?.dangHoatDong ? 'Hoạt động' : 'Khóa' }}
            </span>
          </td>
          <td>{{ formatDate(nv.ngayVaoLam) }}</td>
          <td>
            <button @click="editStaff(nv)" class="btn btn-sm btn-warning me-2">Sửa</button>
            <button @click="deleteStaff(nv.id)" class="btn btn-sm btn-danger">Xóa</button>
          </td>
        </tr>
      </tbody>
    </table>
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
