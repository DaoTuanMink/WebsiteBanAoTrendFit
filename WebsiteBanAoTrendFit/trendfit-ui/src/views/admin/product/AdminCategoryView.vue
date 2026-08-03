<template>
  <div class="container-fluid py-4">
    <div class="card p-3 mb-4 shadow-sm">
      <h4 class="fw-bold">QUẢN TRỊ DANH MỤC</h4>

      <div v-if="errorMsg" class="alert alert-danger py-2 mt-2 mb-0">{{ errorMsg }}</div>

      <div class="row g-2 mt-2">
        <div class="col-md-4">
          <input v-model.trim="formData.ten" class="form-control" placeholder="Tên danh mục..." />
        </div>
        <div class="col-md-2">
          <button @click="saveDanhMuc" class="btn btn-dark w-100" :disabled="submitting">
            {{ dangSua ? 'Cập nhật' : 'Thêm mới' }}
          </button>
          <button v-if="dangSua" @click="resetForm" class="btn btn-link btn-sm">Hủy</button>
        </div>
      </div>
    </div>

    <div class="card shadow-sm">
      <table class="table table-hover align-middle">
        <thead class="table-dark">
          <tr>
            <th>ID</th>
            <th>Tên Danh Mục</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="danhMucs.length === 0">
            <td colspan="3" class="text-center text-muted py-3">Chưa có danh mục nào</td>
          </tr>
          <tr v-for="dm in danhMucs" :key="dm.id">
            <td>#{{ dm.id }}</td>
            <td>{{ dm.ten }}</td>
            <td>
              <button @click="kichHoatSua(dm)" class="btn btn-sm btn-outline-warning me-2">
                Sửa
              </button>
              <button @click="xoaDanhMuc(dm.id)" class="btn btn-sm btn-outline-danger">Xóa</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
// Trang CRUD Danh Mục sản phẩm (dùng cho cả ADMIN và EMPLOYEE, xem
// AuthInterceptor - "/api/admin/products/**" không nằm trong nhóm ADMIN_ONLY).
// Do đó vẫn PHẢI gắn header "User-Role"/"NhanVien-ID" (getAuthHeaders) để
// backend biết ai đang gọi, dù không giới hạn chỉ riêng ADMIN.
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { getAuthHeaders } from '@/utils/adminAuth'

const danhMucs = ref([])
const API_URL = 'http://localhost:8080/api/admin/products/categories'
const formData = ref({ id: null, ten: '' })
const dangSua = ref(false)
const submitting = ref(false)
const errorMsg = ref('')

const loadData = async () => {
  try {
    const res = await axios.get(API_URL, { headers: getAuthHeaders() })
    danhMucs.value = res.data
  } catch (err) {
    errorMsg.value = layThongBaoLoi(err, 'Không thể tải danh mục')
  }
}

const saveDanhMuc = async () => {
  errorMsg.value = ''
  if (!formData.value.ten) {
    errorMsg.value = 'Vui lòng nhập tên danh mục!'
    return
  }

  submitting.value = true
  try {
    // POST để thêm hoặc cập nhật (vì controller save của bạn xử lý được cả 2)
    await axios.post(API_URL, formData.value, { headers: getAuthHeaders() })
    resetForm()
    loadData()
  } catch (err) {
    errorMsg.value = layThongBaoLoi(err, 'Lưu danh mục thất bại')
  } finally {
    submitting.value = false
  }
}

const kichHoatSua = (dm) => {
  dangSua.value = true
  formData.value = { ...dm }
}

const resetForm = () => {
  formData.value = { id: null, ten: '' }
  dangSua.value = false
  errorMsg.value = ''
}

const xoaDanhMuc = async (id) => {
  if (!confirm('Xóa danh mục này?')) return
  try {
    await axios.delete(`${API_URL}/${id}`, { headers: getAuthHeaders() })
    loadData()
  } catch (err) {
    alert(layThongBaoLoi(err, 'Xóa thất bại'))
  }
}

const layThongBaoLoi = (err, macDinh) => {
  const data = err?.response?.data
  if (typeof data === 'string' && data.trim()) return data
  return macDinh + (err?.message ? `: ${err.message}` : '')
}

onMounted(loadData)
</script>
