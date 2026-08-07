<template>
  <div class="container-fluid py-4">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
      <div>
        <h4 class="fw-bold mb-1">Quản trị danh mục</h4>
        <p class="text-secondary small mb-0">Thêm, sửa, xóa danh mục sản phẩm</p>
      </div>
    </div>

    <div v-if="errorMsg" class="alert alert-danger py-2">{{ errorMsg }}</div>

    <!-- Form -->
    <div class="card border-0 shadow-sm mb-4">
      <div class="card-body">
        <div class="row g-3 align-items-end">
          <div class="col-md-5">
            <label class="form-label small fw-semibold">Tên danh mục</label>
            <input
              v-model.trim="formData.ten"
              class="form-control"
              placeholder="Nhập tên danh mục..."
              @keyup.enter="saveDanhMuc"
            />
          </div>
          <div class="col-md-auto d-flex gap-2">
            <button
              type="button"
              class="btn btn-primary"
              :disabled="submitting"
              @click="saveDanhMuc"
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
        <span class="fw-semibold">Danh sách danh mục</span>
        <span class="badge text-bg-primary rounded-pill">Tổng: {{ danhMucs.length }}</span>
      </div>
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th style="width: 80px">ID</th>
              <th>Tên danh mục</th>
              <th class="text-center" style="width: 160px">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="danhMucs.length === 0">
              <td colspan="3" class="text-center text-muted py-4">Chưa có danh mục nào</td>
            </tr>
            <tr v-for="dm in danhMucs" :key="dm.id">
              <td class="fw-semibold">#{{ dm.id }}</td>
              <td>{{ dm.ten }}</td>
              <td class="text-center">
                <button
                  type="button"
                  class="btn btn-sm btn-outline-warning me-1"
                  @click="kichHoatSua(dm)"
                >
                  Sửa
                </button>
                <button
                  type="button"
                  class="btn btn-sm btn-outline-danger"
                  @click="xoaDanhMuc(dm.id)"
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
