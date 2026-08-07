<template>
  <div class="container-fluid py-4">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
      <div>
        <h4 class="fw-bold mb-1">Quản trị thương hiệu</h4>
        <p class="text-secondary small mb-0">Thêm, sửa, xóa thương hiệu sản phẩm</p>
      </div>
    </div>

    <div v-if="errorMsg" class="alert alert-danger py-2">{{ errorMsg }}</div>

    <!-- Form -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-body">
        <div class="row g-3 align-items-end">
          <div class="col-md-5">
            <label class="form-label small fw-semibold">Tên thương hiệu</label>
            <input
              v-model.trim="formData.ten"
              class="form-control"
              placeholder="Nhập tên thương hiệu..."
              @keyup.enter="saveBrand"
            />
          </div>
          <div class="col-md-auto d-flex gap-2">
            <button
              type="button"
              class="btn btn-primary"
              :disabled="submitting"
              @click="saveBrand"
            >
              {{ dangSua ? 'Cập nhật' : 'Thêm mới' }}
            </button>
            <button
              v-if="dangSua"
              type="button"
              class="btn btn-outline-secondary"
              @click="resetForm"
            >
              Hủy
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Table -->
    <div class="card border-0 shadow-sm overflow-hidden">
      <div class="card-header bg-white border-0 border-bottom d-flex justify-content-between align-items-center py-3">
        <span class="fw-semibold">Danh sách thương hiệu</span>
        <span class="badge text-bg-primary rounded-pill">Tổng: {{ thuongHieus.length }}</span>
      </div>
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th style="width: 80px">ID</th>
              <th>Tên thương hiệu</th>
              <th class="text-center" style="width: 160px">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="thuongHieus.length === 0">
              <td colspan="3" class="text-center text-muted py-4">Chưa có thương hiệu nào</td>
            </tr>
            <tr v-for="th in thuongHieus" :key="th.id">
              <td class="fw-semibold">#{{ th.id }}</td>
              <td>{{ th.ten }}</td>
              <td class="text-center">
                <button
                  type="button"
                  class="btn btn-sm btn-outline-warning me-1"
                  @click="kichHoatSua(th)"
                >
                  Sửa
                </button>
                <button
                  type="button"
                  class="btn btn-sm btn-outline-danger"
                  @click="xoaThuongHieu(th.id)"
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
