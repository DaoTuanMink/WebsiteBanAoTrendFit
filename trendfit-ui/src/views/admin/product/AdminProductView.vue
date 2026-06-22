<template>
  <div class="container-fluid py-4 text-start font-admin">
    <div class="card rounded-0 border-0 shadow-sm p-4 mb-4 bg-white">
      <div class="row align-items-center g-3">
        <div class="col-md-6">
          <h4 class="fw-bold m-0 text-dark">QUẢN TRỊ SẢN PHẨM TRENDFIT</h4>
        </div>
      </div>
    </div>

    <div class="card rounded-0 border-0 shadow-sm bg-white">
      <div class="table-responsive">
        <table class="table table-hover align-middle text-center m-0">
          <thead class="table-dark">
            <tr>
              <th>ID</th>
              <th class="text-start">Tên Sản Phẩm</th>
              <th>Trạng Thái</th>
              <th>Thao Tác</th>
              <th>Biến Thể</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in danhSachSanPham" :key="item.id">
              <td class="fw-bold">#{{ item.id }}</td>
              <td class="text-start fw-bold text-dark">{{ item.ten }}</td>
              <td>
                <span :class="item.dangBan ? 'badge bg-success' : 'badge bg-danger'">
                  {{ item.dangBan ? 'MỞ BÁN' : 'ẨN' }}
                </span>
              </td>
              <td>
                <button class="btn btn-sm btn-outline-warning me-2">Sửa</button>
                <button class="btn btn-sm btn-outline-danger">Xóa</button>
              </td>
              <td>
                <button class="btn btn-sm btn-info" @click="openVariantModal(item)">
                  <i class="ri-settings-4-line"></i> Quản lý
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal fade" id="variantModal" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Biến thể: {{ selectedProduct?.ten }}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <table class="table table-bordered">
              <thead>
                <tr>
                  <th>Size</th>
                  <th>Màu</th>
                  <th>SL Tồn</th>
                  <th>Giá</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(v, index) in variants" :key="index">
                  <td><input v-model="v.kichCoSize" class="form-control" placeholder="M" /></td>
                  <td><input v-model="v.mauSac" class="form-control" placeholder="Đen" /></td>
                  <td>
                    <input v-model.number="v.soLuongTon" type="number" class="form-control" />
                  </td>
                  <td><input v-model.number="v.gia" type="number" class="form-control" /></td>
                  <td class="text-center">
                    <button @click="variants.splice(index, 1)" class="btn btn-danger btn-sm">
                      x
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
            <button
              @click="variants.push({ kichCoSize: '', mauSac: '', soLuongTon: 0, gia: 0 })"
              class="btn btn-sm btn-dark"
            >
              + Thêm dòng
            </button>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
            <button @click="saveAllVariants" class="btn btn-primary">Lưu thay đổi</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import * as bootstrap from 'bootstrap'

const danhSachSanPham = ref([])
const variants = ref([])
const selectedProduct = ref(null)
const API_BASE = 'http://localhost:8080/api/admin/products'
let variantModal = null

onMounted(() => {
  taiDuLieuTuDatabase()
  // Khởi tạo modal một lần duy nhất
  variantModal = new bootstrap.Modal(document.getElementById('variantModal'))
})

const taiDuLieuTuDatabase = async () => {
  try {
    const res = await axios.get(API_BASE)
    danhSachSanPham.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const openVariantModal = async (product) => {
  selectedProduct.value = product
  try {
    const res = await axios.get(`${API_BASE}/${product.id}/variants`)
    variants.value = res.data
    variantModal.show()
  } catch (e) {
    alert('Không thể tải biến thể!')
  }
}

const saveAllVariants = async () => {
  try {
    await axios.post(`${API_BASE}/${selectedProduct.value.id}/variants`, variants.value)
    alert('Đã lưu biến thể thành công!')
    variantModal.hide()
  } catch (e) {
    alert('Lỗi lưu biến thể!')
  }
}
</script>
