<template>
  <div class="container-fluid py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3 class="fw-bold m-0">QUẢN LÝ KHÁCH HÀNG</h3>
      <button @click="moFormMoi" class="btn btn-primary">+ THÊM KHÁCH HÀNG</button>
    </div>

    <!-- Bảng danh sách khách hàng -->
    <div class="card shadow-sm border-0">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-dark text-center">
              <tr>
                <th>ID</th>
                <th>Ảnh đại diện</th>
                <th>Họ và tên</th>
                <th>Email</th>
                <th>Số điện thoại</th>
                <th>Ngày tạo</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="c in customers" :key="c.id">
                <td class="text-center fw-bold">#{{ c.id }}</td>
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
                <td class="text-center">{{ c.soDienThoai || '---' }}</td>
                <td class="small text-muted text-center">{{ formatDateTime(c.ngayTao) }}</td>
                <td class="text-center">
                  <button
                    type="button"
                    class="btn btn-sm w-100"
                    :class="c.dangHoatDong ? 'btn-success' : 'btn-secondary'"
                    @click="toggleStatus(c.id)"
                  >
                    {{ c.dangHoatDong ? 'Hoạt động' : 'Đã khóa' }}
                  </button>
                </td>
                <td class="text-center">
                  <button class="btn btn-sm btn-warning me-1" @click="editCustomer(c)" title="Sửa">
                    ✏️
                  </button>
                  <button class="btn btn-sm btn-danger" @click="xoaCustomer(c.id)" title="Xóa">
                    🗑️
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

    <!-- Form Thêm / Sửa Khách Hàng -->
    <div v-if="showForm" class="card p-4 shadow mt-4 border-0 bg-light">
      <h5 class="fw-bold mb-3">
        {{ editMode ? 'Cập nhật thông tin khách hàng #' + form.id : 'Thêm khách hàng mới' }}
      </h5>
      <div class="row g-3">
        <div class="col-md-4">
          <label class="form-label small fw-semibold">Họ và tên (*)</label>
          <input v-model="form.hoTen" placeholder="Nhập họ tên" class="form-control" />
        </div>
        <div class="col-md-4">
          <label class="form-label small fw-semibold">Email (*)</label>
          <input
            v-model="form.email"
            :disabled="editMode"
            placeholder="nhansu@gmail.com"
            class="form-control"
          />
        </div>
        <div class="col-md-4" v-if="!editMode">
          <label class="form-label small fw-semibold">Mật khẩu (*)</label>
          <input
            v-model="form.matKhau"
            type="password"
            placeholder="Mật khẩu đăng nhập"
            class="form-control"
          />
        </div>
        <div class="col-md-4">
          <label class="form-label small fw-semibold">Số điện thoại</label>
          <input v-model="form.soDienThoai" placeholder="0987654321" class="form-control" />
        </div>
        <div class="col-md-8">
          <label class="form-label small fw-semibold">Link Ảnh đại diện (URL)</label>
          <input v-model="form.anhDaiDien" placeholder="https://..." class="form-control" />
        </div>
      </div>

      <div class="mt-4 d-flex gap-2">
        <button @click="saveCustomer" class="btn btn-success px-4">💾 Lưu lại</button>
        <button @click="showForm = false" class="btn btn-secondary px-4">Hủy</button>
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
  if (!dt) return '---'
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
