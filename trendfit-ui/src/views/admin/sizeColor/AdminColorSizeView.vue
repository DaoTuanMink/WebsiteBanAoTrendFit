<template>
  <div class="container-fluid py-4">
    <h2 class="mb-4">Quản lý Kích cỡ & Màu sắc</h2>

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
import { ref, onMounted } from 'vue'
import axios from 'axios'

const API_BASE = 'http://localhost:8080/api/admin'

const sizes = ref([])
const colors = ref([])
const newSize = ref('')
const newColor = ref('')

const loadSizes = async () => {
  const res = await axios.get(`${API_BASE}/products/sizes`)
  sizes.value = res.data
}

const loadColors = async () => {
  const res = await axios.get(`${API_BASE}/products/colors`)
  colors.value = res.data
}

const addSize = async () => {
  if (!newSize.value) return
  await axios.post(`${API_BASE}/products/sizes`, { tenKichCo: newSize.value })
  newSize.value = ''
  loadSizes()
}

const addColor = async () => {
  if (!newColor.value) return
  await axios.post(`${API_BASE}/products/colors`, { tenMau: newColor.value })
  newColor.value = ''
  loadColors()
}

const deleteSize = async (id) => {
  if (confirm('Xóa kích cỡ này?')) {
    await axios.delete(`${API_BASE}/products/sizes/${id}`)
    loadSizes()
  }
}

const deleteColor = async (id) => {
  if (confirm('Xóa màu này?')) {
    await axios.delete(`${API_BASE}/products/colors/${id}`)
    loadColors()
  }
}

onMounted(() => {
  loadSizes()
  loadColors()
})
</script>
