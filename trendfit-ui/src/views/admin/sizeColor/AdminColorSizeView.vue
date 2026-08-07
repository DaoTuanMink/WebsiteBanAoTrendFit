<template>
  <div class="container-fluid py-4">
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
      <div>
        <h4 class="fw-bold mb-1">Quản lý kích cỡ & màu sắc</h4>
        <p class="text-secondary small mb-0">Quản lý thuộc tính biến thể sản phẩm</p>
      </div>
    </div>

    <div v-if="errorMsg" class="alert alert-danger py-2">{{ errorMsg }}</div>

    <div class="row g-4">
      <!-- Kích cỡ -->
      <div class="col-lg-6">
        <div class="card border-0 shadow-sm overflow-hidden h-100">
          <div class="card-header bg-white border-0 border-bottom d-flex justify-content-between align-items-center py-3">
            <span class="fw-semibold">Danh sách kích cỡ</span>
            <span class="badge text-bg-primary rounded-pill">Tổng: {{ sizes.length }}</span>
          </div>
          <div class="card-body border-bottom">
            <div class="input-group">
              <input
                v-model="newSize"
                class="form-control"
                placeholder="Nhập kích cỡ mới (S, M, L...)"
                @keyup.enter="addSize"
              />
              <button type="button" class="btn btn-primary" @click="addSize">Thêm</button>
            </div>
          </div>
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light">
                <tr>
                  <th style="width: 70px">ID</th>
                  <th>Tên kích cỡ</th>
                  <th class="text-center" style="width: 100px">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="sizes.length === 0">
                  <td colspan="3" class="text-center text-muted py-3">Chưa có kích cỡ</td>
                </tr>
                <tr v-for="size in sizes" :key="size.id">
                  <td class="fw-semibold">#{{ size.id }}</td>
                  <td>{{ size.tenKichCo }}</td>
                  <td class="text-center">
                    <button type="button" class="btn btn-sm btn-outline-danger" @click="deleteSize(size.id)">Xóa</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Màu sắc -->
      <div class="col-lg-6">
        <div class="card border-0 shadow-sm overflow-hidden h-100">
          <div class="card-header bg-white border-0 border-bottom d-flex justify-content-between align-items-center py-3">
            <span class="fw-semibold">Danh sách màu sắc</span>
            <span class="badge text-bg-primary rounded-pill">Tổng: {{ colors.length }}</span>
          </div>
          <div class="card-body border-bottom">
            <div class="input-group">
              <input
                v-model="newColor"
                class="form-control"
                placeholder="Nhập tên màu (Đỏ, Xanh...)"
                @keyup.enter="addColor"
              />
              <button type="button" class="btn btn-primary" @click="addColor">Thêm</button>
            </div>
          </div>
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light">
                <tr>
                  <th style="width: 70px">ID</th>
                  <th>Tên màu</th>
                  <th>Mã màu</th>
                  <th class="text-center" style="width: 100px">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="colors.length === 0">
                  <td colspan="4" class="text-center text-muted py-3">Chưa có màu sắc</td>
                </tr>
                <tr v-for="color in colors" :key="color.id">
                  <td class="fw-semibold">#{{ color.id }}</td>
                  <td>{{ color.tenMau }}</td>
                  <td>
                    <span
                      class="d-inline-block rounded border"
                      :style="{ background: color.maMau || '#ccc', width: '28px', height: '20px' }"
                      :title="color.maMau"
                    ></span>
                    <small class="text-muted ms-1">{{ color.maMau || '—' }}</small>
                  </td>
                  <td class="text-center">
                    <button type="button" class="btn btn-sm btn-outline-danger" @click="deleteColor(color.id)">Xóa</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { getAuthHeaders } from '@/utils/adminAuth'

const API_BASE = 'http://localhost:8080/api/admin'
const sizes = ref([])
const colors = ref([])
const newSize = ref('')
const newColor = ref('')
const errorMsg = ref('')

const layThongBaoLoi = (err, macDinh) => {
  const data = err?.response?.data
  if (typeof data === 'string' && data.trim()) return data
  return macDinh + (err?.message ? `: ${err.message}` : '')
}

const loadSizes = async () => {
  try {
    const res = await axios.get(`${API_BASE}/products/sizes`, { headers: getAuthHeaders() })
    sizes.value = res.data
  } catch (err) {
    errorMsg.value = layThongBaoLoi(err, 'Không thể tải danh sách kích cỡ')
  }
}

const loadColors = async () => {
  try {
    const res = await axios.get(`${API_BASE}/products/colors`, { headers: getAuthHeaders() })
    colors.value = res.data
  } catch (err) {
    errorMsg.value = layThongBaoLoi(err, 'Không thể tải danh sách màu sắc')
  }
}

const addSize = async () => {
  if (!newSize.value) return
  try {
    await axios.post(`${API_BASE}/products/sizes`, { tenKichCo: newSize.value }, { headers: getAuthHeaders() })
    newSize.value = ''
    loadSizes()
  } catch (err) {
    alert(layThongBaoLoi(err, 'Thêm kích cỡ thất bại'))
  }
}

const addColor = async () => {
  if (!newColor.value) return
  try {
    await axios.post(`${API_BASE}/products/colors`, { tenMau: newColor.value }, { headers: getAuthHeaders() })
    newColor.value = ''
    loadColors()
  } catch (err) {
    alert(layThongBaoLoi(err, 'Thêm màu sắc thất bại'))
  }
}

const deleteSize = async (id) => {
  if (!confirm('Xóa kích cỡ này?')) return
  try {
    await axios.delete(`${API_BASE}/products/sizes/${id}`, { headers: getAuthHeaders() })
    loadSizes()
  } catch (err) {
    alert(layThongBaoLoi(err, 'Xóa thất bại'))
  }
}

const deleteColor = async (id) => {
  if (!confirm('Xóa màu này?')) return
  try {
    await axios.delete(`${API_BASE}/products/colors/${id}`, { headers: getAuthHeaders() })
    loadColors()
  } catch (err) {
    alert(layThongBaoLoi(err, 'Xóa thất bại'))
  }
}

onMounted(() => {
  loadSizes()
  loadColors()
})
</script>
