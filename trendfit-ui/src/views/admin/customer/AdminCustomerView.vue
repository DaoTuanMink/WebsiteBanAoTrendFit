<template>
  <div class="container-fluid py-4">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
      <div>
        <h4 class="fw-bold mb-1">Quản lý khách hàng</h4>
        <p class="text-secondary small mb-0">Danh sách và chỉnh sửa thông tin khách hàng</p>
      </div>
      <button type="button" class="btn btn-primary" @click="moFormMoi">+ Thêm khách hàng</button>
    </div>

    <!-- Form Thêm / Sửa -->
    <div v-if="showForm" class="card border-0 shadow-sm mb-4">
      <div class="card-body">
        <h6 class="fw-bold mb-3">
          {{ editMode ? 'Cập nhật khách hàng #' + form.id : 'Thêm khách hàng mới' }}
        </h6>
        <div class="row g-3">
          <div class="col-md-4">
            <label class="form-label small fw-semibold">Họ và tên (*)</label>
            <input v-model="form.hoTen" class="form-control" placeholder="Nhập họ tên" />
          </div>
          <div class="col-md-4">
            <label class="form-label small fw-semibold">Email (*)</label>
            <input
              v-model="form.email"
              :disabled="editMode"
              class="form-control"
              placeholder="email@example.com"
            />
          </div>
          <div class="col-md-4" v-if="!editMode">
            <label class="form-label small fw-semibold">Mật khẩu (*)</label>
            <input
              v-model="form.matKhau"
              type="password"
              class="form-control"
              placeholder="Mật khẩu đăng nhập"
            />
          </div>
          <div class="col-md-4">
            <label class="form-label small fw-semibold">Số điện thoại</label>
            <input v-model="form.soDienThoai" class="form-control" placeholder="0987654321" />
          </div>
          <div class="col-md-8">
            <label class="form-label small fw-semibold">Link ảnh đại diện (URL)</label>
            <input v-model="form.anhDaiDien" class="form-control" placeholder="https://..." />
          </div>
        </div>
        <div class="d-flex gap-2 mt-4">
          <button type="button" class="btn btn-primary" @click="saveCustomer">Lưu lại</button>
          <button type="button" class="btn btn-outline-secondary" @click="showForm = false">
            Hủy
          </button>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="card border-0 shadow-sm overflow-hidden">
      <div class="card-header bg-white border-0 border-bottom d-flex justify-content-between align-items-center py-3">
        <span class="fw-semibold">Danh sách khách hàng</span>
        <span class="badge text-bg-primary rounded-pill">Tổng KH: {{ customers.length }}</span>
      </div>
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light text-center">
            <tr>
              <th>ID</th>
              <th>Ảnh</th>
              <th class="text-start">Họ và tên</th>
              <th class="text-start">Email</th>
              <th>Số điện thoại</th>
              <th>Ngày tạo</th>
              <th>Trạng thái</th>
              <th style="width: 140px">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in customers" :key="c.id">
              <td class="text-center fw-semibold">#{{ c.id }}</td>
              <td class="text-center">
                <img
                  :src="
                    c.anhDaiDien ||
                    'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100'
                  "
                  class="rounded-circle border"
                  style="width: 38px; height: 38px; object-fit: cover"
                  alt="Avatar"
                />
              </td>
              <td class="fw-semibold">{{ c.hoTen }}</td>
              <td>{{ c.email }}</td>
              <td class="text-center">{{ c.soDienThoai || '—' }}</td>
              <td class="small text-muted text-center">{{ formatDateTime(c.ngayTao) }}</td>
              <td class="text-center">
                <button
                  type="button"
                  class="btn btn-sm"
                  :class="c.dangHoatDong ? 'btn-success' : 'btn-secondary'"
                  @click="toggleStatus(c.id)"
                >
                  {{ c.dangHoatDong ? 'Hoạt động' : 'Đã khóa' }}
                </button>
              </td>
              <td class="text-center">
                <button
                  type="button"
                  class="btn btn-sm btn-outline-warning me-1"
                  @click="editCustomer(c)"
                >
                  Sửa
                </button>
                <button
                  type="button"
                  class="btn btn-sm btn-outline-danger"
                  @click="xoaCustomer(c.id)"
                >
                  Xóa
                </button>
              </td>
            </tr>
            <tr v-if="customers.length === 0">
              <td colspan="8" class="text-center py-4 text-muted">
                Chưa có dữ liệu khách hàng nào.
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

const API = 'http://localhost:8080/api/admin/customers'

const apiAdmin = axios.create()
apiAdmin.interceptors.request.use((config) => {
  config.headers = { ...config.headers, ...getAuthHeaders() }
  return config
})

const customers = ref([])
const showForm = ref(false)
const editMode = ref(false)
const form = ref({})

const formatDateTime = (dt) => {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('vi-VN')
}

const loadCustomers = async () => {
  try {
    const res = await apiAdmin.get(API)
    customers.value = res.data
  } catch (err) {
    alert('Không thể tải danh sách khách hàng: ' + (err.response?.data || err.message))
  }
}

const moFormMoi = () => {
  form.value = {
    hoTen: '',
    email: '',
    matKhau: '',
    soDienThoai: '',
    anhDaiDien: '',
    dangHoatDong: true,
  }
  editMode.value = false
  showForm.value = true
}

const editCustomer = (c) => {
  form.value = { ...c }
  editMode.value = true
  showForm.value = true
}

const toggleStatus = async (id) => {
  try {
    await apiAdmin.put(`${API}/${id}/toggle-status`)
    loadCustomers()
  } catch (err) {
    alert('Đổi trạng thái thất bại: ' + (err.response?.data || err.message))
  }
}

const xoaCustomer = async (id) => {
  if (confirm('Bạn có chắc chắn muốn xóa tài khoản khách hàng này?')) {
    try {
      await apiAdmin.delete(`${API}/${id}`)
      loadCustomers()
    } catch (err) {
      alert('Xóa thất bại: ' + (err.response?.data || err.message))
    }
  }
}

const saveCustomer = async () => {
  if (!form.value.hoTen || !form.value.email) {
    alert('Vui lòng điền đủ Họ tên và Email!')
    return
  }

  try {
    if (editMode.value) {
      await apiAdmin.put(`${API}/${form.value.id}`, form.value)
    } else {
      await apiAdmin.post(API, form.value)
    }
    alert('Lưu thông tin thành công!')
    showForm.value = false
    loadCustomers()
  } catch (e) {
    alert('Lỗi: ' + (e.response?.data || e.message))
  }
}

onMounted(loadCustomers)
</script>

<style scoped>
.table th,
.table td {
  vertical-align: middle;
}
</style>
