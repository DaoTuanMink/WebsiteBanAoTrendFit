<template>
  <div class="container-fluid py-4">
    <div class="card p-3 mb-4 shadow-sm">
      <h4 class="fw-bold">QUẢN TRỊ THƯƠNG HIỆU</h4>
      <div class="row g-2 mt-2">
        <div class="col-md-4">
          <input v-model="formData.ten" class="form-control" placeholder="Tên thương hiệu..." />
        </div>
        <div class="col-md-2">
          <button @click="saveBrand" class="btn btn-dark w-100">
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
import { ref, onMounted } from 'vue'
import axios from 'axios'

const thuongHieus = ref([])
// Đổi API endpoint sang /brands
const API_URL = 'http://localhost:8080/api/admin/products/brands'
const formData = ref({ id: null, ten: '' })
const dangSua = ref(false)

const loadData = async () => {
  const res = await axios.get(API_URL)
  thuongHieus.value = res.data
}

const saveBrand = async () => {
  if (!formData.value.ten) return alert('Vui lòng nhập tên!')

  await axios.post(API_URL, formData.value)
  resetForm()
  loadData()
}

const kichHoatSua = (th) => {
  dangSua.value = true
  formData.value = { ...th }
}

const resetForm = () => {
  formData.value = { id: null, ten: '' }
  dangSua.value = false
}

const xoaThuongHieu = async (id) => {
  if (confirm('Xóa thương hiệu này?')) {
    await axios.delete(`${API_URL}/${id}`)
    loadData()
  }
}

onMounted(loadData)
</script>
