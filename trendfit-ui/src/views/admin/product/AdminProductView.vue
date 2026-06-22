<template>
  <div class="container-fluid py-4">
    <button @click="moFormThemMoi" class="btn btn-primary mb-3">+ THÊM SẢN PHẨM MỚI</button>

    <div v-if="hienThiForm" class="card p-4 mb-4 shadow">
      <h5 class="fw-bold">
        {{ dangSua ? 'Sửa sản phẩm #' + (formData.sanPham.id || 'N/A') : 'Thêm sản phẩm mới' }}
      </h5>
      <div class="row g-3">
        <div class="col-md-6">
          <input v-model="formData.sanPham.ten" class="form-control" placeholder="Tên sản phẩm" />
        </div>
        <div class="col-md-3">
          <select v-model="formData.sanPham.danhMuc" class="form-select">
            <option :value="null">-- Chọn Danh mục --</option>
            <option v-for="dm in metadata.danhMucs" :key="dm.id" :value="dm">{{ dm.ten }}</option>
          </select>
        </div>
        <div class="col-md-3">
          <select v-model="formData.sanPham.thuongHieu" class="form-select">
            <option :value="null">-- Chọn Thương hiệu --</option>
            <option v-for="th in metadata.thuongHieus" :key="th.id" :value="th">
              {{ th.ten }}
            </option>
          </select>
        </div>
      </div>

      <div class="mt-4">
        <h6>Biến thể (Size/Màu)</h6>
        <table class="table table-sm table-bordered">
          <thead class="table-light">
            <tr>
              <th>Size</th>
              <th>Màu</th>
              <th>SL Tồn</th>
              <th>Giá</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(v, idx) in formData.bienTheSanPhams" :key="idx">
              <td><input v-model="v.kichCoSize" class="form-control" /></td>
              <td><input v-model="v.mauSac" class="form-control" /></td>
              <td><input v-model.number="v.soLuongTon" type="number" class="form-control" /></td>
              <td><input v-model.number="v.gia" type="number" class="form-control" /></td>
              <td>
                <button
                  @click="formData.bienTheSanPhams.splice(idx, 1)"
                  class="btn btn-danger btn-sm"
                >
                  x
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <button
          @click="
            formData.bienTheSanPhams.push({ kichCoSize: '', mauSac: '', soLuongTon: 0, gia: 0 })
          "
          class="btn btn-dark btn-sm"
        >
          + Thêm dòng biến thể
        </button>
      </div>

      <div class="mt-3">
        <button @click="saveFullProduct" class="btn btn-success me-2">LƯU THAY ĐỔI</button>
        <button @click="hienThiForm = false" class="btn btn-secondary">Hủy</button>
      </div>
    </div>

    <table class="table table-hover align-middle">
      <thead class="table-dark">
        <tr>
          <th>ID</th>
          <th>Tên</th>
          <th>Danh Mục</th>
          <th>Thao tác</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="sp in danhSachSanPham" :key="sp.id">
          <td>{{ sp.id }}</td>
          <td>{{ sp.ten }}</td>
          <td>{{ sp.danhMuc?.ten || 'N/A' }}</td>
          <td>
            <button class="btn btn-warning btn-sm me-2" @click="kichHoatSuaForm(sp)">
              Sửa/Biến thể
            </button>
            <button class="btn btn-danger btn-sm" @click="hanhdongXoaCSDL(sp.id)">Xóa</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const API_BASE = 'http://localhost:8080/api/admin/products'
const danhSachSanPham = ref([])
const metadata = ref({ danhMucs: [], thuongHieus: [] })
const hienThiForm = ref(false)
const dangSua = ref(false)

const formData = ref({
  sanPham: { id: null, ten: '', moTa: '', danhMuc: null, thuongHieu: null },
  bienTheSanPhams: [],
  anhSanPhams: [],
})

const loadData = async () => {
  const [resSp, resMeta] = await Promise.all([
    axios.get(API_BASE),
    axios.get(`${API_BASE}/metadata`),
  ])
  danhSachSanPham.value = resSp.data
  metadata.value = resMeta.data
}

const saveFullProduct = async () => {
  try {
    if (dangSua.value) {
      await axios.put(`${API_BASE}/full`, formData.value)
    } else {
      await axios.post(`${API_BASE}/full`, formData.value)
    }
    alert('Lưu thành công!')
    hienThiForm.value = false
    loadData()
  } catch (err) {
    alert('Lỗi: Kiểm tra lại dữ liệu!')
    console.error(err)
  }
}

const kichHoatSuaForm = async (sp) => {
  dangSua.value = true
  hienThiForm.value = true

  // Gọi API lấy biến thể chi tiết theo ID sản phẩm
  try {
    const resVariants = await axios.get(`${API_BASE}/${sp.id}/variants`)

    // Ép kiểu để đảm bảo formData có ID chính xác
    formData.value = {
      sanPham: { ...sp },
      bienTheSanPhams: resVariants.data || [],
      anhSanPhams: [],
    }
  } catch (err) {
    console.error('Lỗi lấy biến thể:', err)
  }
}

const moFormThemMoi = () => {
  dangSua.value = false
  hienThiForm.value = true
  formData.value = {
    sanPham: { id: null, ten: '', moTa: '', danhMuc: null, thuongHieu: null },
    bienTheSanPhams: [],
    anhSanPhams: [],
  }
}

const hanhdongXoaCSDL = async (id) => {
  if (confirm('Bạn chắc chắn muốn xóa sản phẩm này?')) {
    await axios.delete(`${API_BASE}/${id}`)
    loadData()
  }
}

onMounted(loadData)
</script>
