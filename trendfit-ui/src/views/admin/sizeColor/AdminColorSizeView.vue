<template>
  <div class="container-fluid py-4">
    <h2 class="mb-4">Quản lý Kích cỡ & Màu sắc</h2>

    <div v-if="errorMsg" class="alert alert-danger py-2">{{ errorMsg }}</div>

    <div class="row">
      <!-- Quản lý Kích cỡ -->
      <div class="col-lg-6">
        <div class="card mb-4">
          <div class="card-header bg-dark text-white">
            <h5 class="mb-0">📏 Danh sách Kích cỡ</h5>
          </div>
          <div class="card-body">
            <div class="input-group mb-3">
              <input
                v-model="newSize"
                class="form-control"
                placeholder="Nhập kích cỡ mới (S, M, L...)"
              />
              <button @click="addSize" class="btn btn-success">Thêm</button>
            </div>

            <table class="table table-hover">
              <thead class="table-light">
                <tr>
                  <th>ID</th>
                  <th>Tên Kích cỡ</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="size in sizes" :key="size.id">
                  <td>{{ size.id }}</td>
                  <td>{{ size.tenKichCo }}</td>
                  <td>
                    <button @click="deleteSize(size.id)" class="btn btn-danger btn-sm">Xóa</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Quản lý Màu sắc -->
      <div class="col-lg-6">
        <div class="card mb-4">
          <div class="card-header bg-dark text-white">
            <h5 class="mb-0">🎨 Danh sách Màu sắc</h5>
          </div>
          <div class="card-body">
            <div class="input-group mb-3">
              <input
                v-model="newColor"
                class="form-control"
                placeholder="Nhập tên màu (Đỏ, Xanh...)"
              />
              <button @click="addColor" class="btn btn-success">Thêm</button>
            </div>

            <table class="table table-hover">
              <thead class="table-light">
                <tr>
                  <th>ID</th>
                  <th>Tên Màu</th>
                  <th>Mã màu</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="color in colors" :key="color.id">
                  <td>{{ color.id }}</td>
                  <td>{{ color.tenMau }}</td>
                  <td>
                    <span
                      :style="{ background: color.maMau, padding: '4px 12px', borderRadius: '4px' }"
                    ></span>
                  </td>
                  <td>
                    <button @click="deleteColor(color.id)" class="btn btn-danger btn-sm">
                      Xóa
                    </button>
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
// Trang CRUD Kích cỡ & Màu sắc, dùng chung được bởi ADMIN và EMPLOYEE.
// Mọi request đều phải gắn header xác thực (getAuthHeaders) vì
// "/api/admin/**" đã được AuthInterceptor bảo vệ toàn bộ.
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
    await axios.post(
      `${API_BASE}/products/sizes`,
      { tenKichCo: newSize.value },
      { headers: getAuthHeaders() },
    )
    newSize.value = ''
    loadSizes()
  } catch (err) {
    alert(layThongBaoLoi(err, 'Thêm kích cỡ thất bại'))
  }
}

const addColor = async () => {
  if (!newColor.value) return
  try {
    await axios.post(
      `${API_BASE}/products/colors`,
      { tenMau: newColor.value },
      { headers: getAuthHeaders() },
    )
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
