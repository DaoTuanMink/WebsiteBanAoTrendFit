<template>
  <div class="container-fluid py-4">
    <div class="card p-3 mb-4 shadow-sm">
      <h4 class="fw-bold">QUẢN TRỊ DANH MỤC</h4>
      <div class="row g-2 mt-2">
        <div class="col-md-4">
          <input v-model="formData.ten" class="form-control" placeholder="Tên danh mục..." />
        </div>
        <div class="col-md-2">
          <button @click="saveDanhMuc" class="btn btn-dark w-100">
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
import { ref, onMounted } from 'vue'
import axios from 'axios'

const danhMucs = ref([])
const API_URL = 'http://localhost:8080/api/admin/products/categories'
const formData = ref({ id: null, ten: '' })
const dangSua = ref(false)

const loadData = async () => {
  const res = await axios.get(API_URL)
  danhMucs.value = res.data
}

const saveDanhMuc = async () => {
  if (!formData.value.ten) return alert('Vui lòng nhập tên!')

  // POST để thêm hoặc cập nhật (vì controller save của bạn xử lý được cả 2)
  await axios.post(API_URL, formData.value)
  resetForm()
  loadData()
}

const kichHoatSua = (dm) => {
  dangSua.value = true
  formData.value = { ...dm }
}

const resetForm = () => {
  formData.value = { id: null, ten: '' }
  dangSua.value = false
}

const xoaDanhMuc = async (id) => {
  if (confirm('Xóa danh mục này?')) {
    await axios.delete(`${API_URL}/${id}`)
    loadData()
  }
}

onMounted(loadData)
</script>
