<template>
  <div class="container-fluid py-4 text-start font-admin">
    <div class="card rounded-0 border-0 shadow-sm p-4 mb-4 bg-white">
      <div class="row align-items-center g-3">
        <div class="col-12 col-md-4">
          <h4 class="fw-bold m-0 text-dark">TRUNG TÂM QUẢN TRỊ SẢN PHẨM TRENDFIT</h4>
          <small class="text-muted">CRUD & Tìm kiếm thời gian thực hệ thống</small>
        </div>
        <div class="col-12 col-md-5">
          <div class="input-group">
            <input
              type="text"
              v-model="tuKhoaTimKiem"
              @keyup.enter="taiDuLieuTuDatabase"
              class="form-control rounded-0 border-dark"
              placeholder="Nhập tên áo cần tìm kiếm (Ấn Enter)..."
            />
            <button @click="taiDuLieuTuDatabase" class="btn btn-dark rounded-0 px-3" type="button">
              Tìm ngay
            </button>
          </div>
        </div>
        <div class="col-12 col-md-3 text-md-end">
          <button
            @click="moFormThemMoi"
            class="btn btn-primary rounded-0 fw-bold w-100 py-2 text-uppercase"
          >
            + THÊM SẢN PHẨM MỚI
          </button>
        </div>
      </div>
    </div>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-dark"></div>
      <p class="mt-2 text-muted small">Đang nạp dữ liệu TrendFit từ CSDL MySQL...</p>
    </div>

    <div v-else class="card rounded-0 border-0 shadow-sm bg-white">
      <div class="table-responsive">
        <table class="table table-hover align-middle text-center m-0">
          <thead class="table-dark">
            <tr>
              <th>ID</th>
              <th class="text-start ps-4">Tên Sản Phẩm</th>
              <th>Danh Mục</th>
              <th>Thương Hiệu</th>
              <th>Chất Liệu</th>
              <th>Trạng Thái</th>
              <th>Thao Tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in danhSachSanPham" :key="item.id">
              <td class="fw-bold">#{{ item.id }}</td>
              <td class="text-start ps-4 fw-bold text-dark">{{ item.ten }}</td>
              <td>
                <span class="badge bg-secondary rounded-0">{{ item.danhMuc?.ten || 'Trống' }}</span>
              </td>
              <td>
                <span class="badge bg-info text-dark rounded-0">{{
                  item.thuongHieu?.ten || 'Trống'
                }}</span>
              </td>
              <td>{{ item.chatLieu || '-' }}</td>
              <td>
                <span
                  :class="item.dangBan ? 'badge bg-success' : 'badge bg-danger'"
                  class="rounded-0"
                >
                  {{ item.dangBan ? 'MỞ BÁN' : 'ẨN KHO' }}
                </span>
              </td>
              <td>
                <div class="d-flex gap-2 justify-content-center">
                  <button
                    @click="kichHoatSuaForm(item)"
                    class="btn btn-sm btn-outline-warning rounded-0"
                  >
                    Sửa
                  </button>
                  <button
                    @click="hanhdongXoaCSDL(item.id)"
                    class="btn btn-sm btn-outline-danger rounded-0"
                  >
                    Xóa
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="danhSachSanPham.length === 0">
              <td colspan="7" class="text-muted py-5 fw-bold bg-light">
                Không tìm thấy sản phẩm nào!
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="hienThiForm" class="card rounded-0 shadow-sm border-0 my-4 bg-white">
      <div class="card-header bg-dark text-white rounded-0 fw-bold py-3 text-uppercase">
        {{
          dangSua
            ? 'Cập nhật sản phẩm #' + formFullProduct.sanPham.id
            : 'Thêm mới tổ hợp sản phẩm TrendFit'
        }}
      </div>
      <div class="card-body p-4">
        <form @submit.prevent="hanhdongLuuCSDL" class="row g-3">
          <div class="col-md-6">
            <label class="form-label small fw-bold">Tên sản phẩm</label>
            <input
              type="text"
              v-model="formFullProduct.sanPham.ten"
              class="form-control rounded-0 border-dark"
              required
            />
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-bold">Mã Slug định vị</label>
            <input
              type="text"
              v-model="formFullProduct.sanPham.slug"
              class="form-control rounded-0 border-dark"
            />
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-bold">Chất liệu chính</label>
            <input
              type="text"
              v-model="formFullProduct.sanPham.chatLieu"
              class="form-control rounded-0 border-dark"
            />
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-bold">Xuất xứ</label>
            <input
              type="text"
              v-model="formFullProduct.sanPham.xuatXu"
              class="form-control rounded-0 border-dark"
            />
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-bold">Năm ra mắt</label>
            <input
              type="number"
              v-model="formFullProduct.sanPham.namRaMat"
              class="form-control rounded-0 border-dark"
            />
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-bold">Trạng thái kinh doanh</label>
            <select
              v-model="formFullProduct.sanPham.dangBan"
              class="form-select rounded-0 border-dark"
            >
              <option :value="true">MỞ BÁN</option>
              <option :value="false">ẨN KHO</option>
            </select>
          </div>
          <div class="col-12">
            <label class="form-label small fw-bold">Mô tả sản phẩm</label>
            <textarea
              v-model="formFullProduct.sanPham.moTa"
              class="form-control rounded-0 border-dark"
              rows="3"
            ></textarea>
          </div>
          <div class="col-12 d-flex gap-2 justify-content-end mt-4">
            <button
              type="button"
              @click="hienThiForm = false"
              class="btn btn-secondary rounded-0 px-4"
            >
              HỦY BỎ
            </button>
            <button type="submit" class="btn btn-primary rounded-0 px-5 fw-bold">
              XÁC NHẬN LƯU
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const API_BASE = 'http://localhost:8080/api/admin/products'

const danhSachSanPham = ref([])
const loading = ref(true)
const hienThiForm = ref(false)
const dangSua = ref(false)
const tuKhoaTimKiem = ref('')

const formFullProduct = ref(createEmptyForm())

function createEmptyForm() {
  return {
    sanPham: {
      id: null,
      ten: '',
      slug: '',
      chatLieu: 'Cotton',
      xuatXu: 'Việt Nam',
      namRaMat: 2026,
      moTa: '',
      dangBan: true,
    },
  }
}

const taiDuLieuTuDatabase = async () => {
  try {
    loading.value = true
    const response = await axios.get(`${API_BASE}?search=${tuKhoaTimKiem.value}`)
    danhSachSanPham.value = response.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const hanhdongLuuCSDL = async () => {
  try {
    if (dangSua.value && formFullProduct.value.sanPham.id) {
      await axios.put(
        `${API_BASE}/${formFullProduct.value.sanPham.id}`,
        formFullProduct.value.sanPham,
      )
      alert('Cập nhật thành công!')
    } else {
      await axios.post(API_BASE, formFullProduct.value.sanPham)
      alert('Thêm mới thành công!')
    }
    hienThiForm.value = false
    taiDuLieuTuDatabase()
  } catch (error) {
    alert('Thao tác lỗi, vui lòng kiểm tra lại!')
  }
}

const hanhdongXoaCSDL = async (id) => {
  if (confirm('Bạn chắc chắn muốn xóa vĩnh viễn sản phẩm này?')) {
    try {
      await axios.delete(`${API_BASE}/${id}`)
      alert('Đã xóa bắng ghi thành công!')
      taiDuLieuTuDatabase()
    } catch (error) {
      alert('Lỗi ràng buộc dữ liệu khóa ngoại!')
    }
  }
}

const kichHoatSuaForm = (item) => {
  dangSua.value = true
  hienThiForm.value = true
  formFullProduct.value.sanPham = { ...item }
}

const moFormThemMoi = () => {
  dangSua.value = false
  hienThiForm.value = true
  formFullProduct.value = createEmptyForm()
}

onMounted(() => {
  taiDuLieuTuDatabase()
})
</script>
