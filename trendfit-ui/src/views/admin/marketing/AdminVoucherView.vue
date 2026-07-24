<template>
  <div class="container-fluid py-4">
    <h3 class="fw-bold mb-4">QUẢN LÝ MÃ GIẢM GIÁ</h3>

    <button @click="moFormMoi" class="btn btn-primary mb-3">+ TẠO VOUCHER MỚI</button>

    <table class="table table-hover align-middle bg-white border">
      <thead class="table-dark">
        <tr>
          <th>Mã</th>
          <th>Tên</th>
          <th>Giá trị</th>
          <th>Hạn dùng</th>
          <th>Đã dùng/Giới hạn</th>
          <th>Trạng thái</th>
          <th>Thao tác</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="v in vouchers" :key="v.id">
          <td class="fw-bold text-primary">{{ v.ma }}</td>
          <td>{{ v.ten }}</td>
          <td>
            {{ v.loai === 'PERCENT' ? v.giaTriGiam + '%' : formatCurrency(v.giaTriGiam) }}
          </td>
          <td class="small">{{ v.ngayBatDau }} - {{ v.ngayKetThuc }}</td>
          <td>{{ v.soLanDaDung }} / {{ v.gioiHanSuDung }}</td>
          <td>
            <span :class="v.dangHoatDong ? 'badge bg-success' : 'badge bg-danger'">
              {{ v.dangHoatDong ? 'Hoạt động' : 'Đã khóa' }}
            </span>
          </td>
          <td>
            <button class="btn btn-sm btn-warning me-2" @click="editVoucher(v)">Sửa</button>
            <button class="btn btn-sm btn-danger" @click="xoaVoucher(v.id)">Xóa</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="showForm" class="card p-4 shadow mt-4">
      <h5>{{ editMode ? 'Cập nhật Voucher' : 'Tạo Voucher mới' }}</h5>
      <div class="row g-3">
        <div class="col-md-3">
          <input v-model="form.ma" placeholder="Mã Code" class="form-control" />
        </div>
        <div class="col-md-3">
          <input v-model="form.ten" placeholder="Tên chương trình" class="form-control" />
        </div>
        <div class="col-md-2">
          <select v-model="form.loai" class="form-select">
            <option value="PERCENT">Phần trăm (%)</option>
            <option value="FIXED">Số tiền (đ)</option>
          </select>
        </div>
        <div class="col-md-2">
          <input
            v-model="form.giaTriGiam"
            type="number"
            placeholder="Giá trị"
            class="form-control"
          />
        </div>
        <div class="col-md-2">
          <input
            v-model="form.gioiHanSuDung"
            type="number"
            placeholder="Giới hạn"
            class="form-control"
          />
        </div>
        <div class="col-md-3">
          <input v-model="form.ngayBatDau" type="date" class="form-control" />
        </div>
        <div class="col-md-3">
          <input v-model="form.ngayKetThuc" type="date" class="form-control" />
        </div>
      </div>
      <div class="mt-3">
        <button @click="saveVoucher" class="btn btn-success me-2">Lưu</button>
        <button @click="showForm = false" class="btn btn-secondary">Đóng</button>
      </div>
    </div>
  </div>
</template>

<script setup>
// Quản lý Voucher/Mã giảm giá — CHỈ dành cho ADMIN (nằm trong ADMIN_ONLY_PATHS
// của AuthInterceptor). Dùng axios instance riêng tự gắn header xác thực.
import { ref, onMounted } from 'vue'
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

const formatCurrency = (val) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
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
  form.value = { ma: '', ten: '', loai: 'PERCENT', dangHoatDong: true }
  editMode.value = false
  showForm.value = true
}

const xoaVoucher = async (id) => {
  if (confirm('Xóa voucher này?')) {
    try {
      await apiAdmin.delete(`${API}/${id}`)
      loadVouchers()
    } catch (err) {
      alert('Xóa thất bại: ' + (err.response?.data || err.message))
    }
  }
}

// ... (các import giữ nguyên)

const editVoucher = (v) => {
  // Clone object v để tránh thay đổi trực tiếp trên danh sách khi chưa lưu
  form.value = { ...v }
  editMode.value = true
  showForm.value = true
}

const saveVoucher = async () => {
  // Validate cơ bản
  if (!form.value.ma || !form.value.giaTriGiam) {
    alert('Vui lòng nhập đủ Mã và Giá trị giảm!')
    return
  }

  try {
    if (editMode.value) {
      await apiAdmin.put(`${API}/${form.value.id}`, form.value)
    } else {
      await apiAdmin.post(API, form.value)
    }
    alert('Lưu thành công!')
    showForm.value = false
    loadVouchers()
  } catch (e) {
    alert('Lỗi lưu voucher: ' + (e.response?.data?.message || 'Có lỗi xảy ra'))
  }
}

onMounted(loadVouchers)
</script>
