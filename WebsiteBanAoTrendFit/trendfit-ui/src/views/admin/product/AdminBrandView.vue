<template>
  <div class="container-fluid py-4">
    <div class="card p-3 mb-4 shadow-sm">
      <h4 class="fw-bold">QUẢN TRỊ THƯƠNG HIỆU</h4>

      <div v-if="errorMsg" class="alert alert-danger py-2 mt-2 mb-0">{{ errorMsg }}</div>

      <div class="row g-2 mt-2">
        <div class="col-md-4">
          <input v-model.trim="formData.ten" class="form-control" placeholder="Tên thương hiệu..." />
        </div>
        <div class="col-md-2">
          <button @click="saveBrand" class="btn btn-dark w-100" :disabled="submitting">
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
            <th>Tên Thương Hiệu</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="thuongHieus.length === 0">
            <td colspan="3" class="text-center text-muted py-3">Chưa có thương hiệu nào</td>
          </tr>
          <tr v-for="th in thuongHieus" :key="th.id">
            <td>#{{ th.id }}</td>
            <td>{{ th.ten }}</td>
            <td>
              <button @click="kichHoatSua(th)" class="btn btn-sm btn-outline-warning me-2">
                Sửa
              </button>
              <button @click="xoaThuongHieu(th.id)" class="btn btn-sm btn-outline-danger">
                Xóa
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
// Trang CRUD Thương hiệu, tương tự AdminCategoryView.vue - dùng chung được
// bởi ADMIN và EMPLOYEE.
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { getAuthHeaders } from '@/utils/adminAuth'

const thuongHieus = ref([])
const API_URL = 'http://localhost:8080/api/admin/products/brands'
const formData = ref({ id: null, ten: '' })
const dangSua = ref(false)
const submitting = ref(false)
const errorMsg = ref('')

const loadData = async () => {
  try {
    const res = await axios.get(API_URL, { headers: getAuthHeaders() })
    thuongHieus.value = res.data
  } catch (err) {
    errorMsg.value = layThongBaoLoi(err, 'Không thể tải thương hiệu')
  }
}

const saveBrand = async () => {
  errorMsg.value = ''
  if (!formData.value.ten) {
    errorMsg.value = 'Vui lòng nhập tên thương hiệu!'
    return
  }

  submitting.value = true
  try {
    await axios.post(API_URL, formData.value, { headers: getAuthHeaders() })
    resetForm()
    loadData()
  } catch (err) {
    errorMsg.value = layThongBaoLoi(err, 'Lưu thương hiệu thất bại')
  } finally {
    submitting.value = false
  }
}

const kichHoatSua = (th) => {
  dangSua.value = true
  formData.value = { ...th }
}

const resetForm = () => {
  formData.value = { id: null, ten: '' }
  dangSua.value = false
  errorMsg.value = ''
}

const xoaThuongHieu = async (id) => {
  if (!confirm('Xóa thương hiệu này?')) return
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
