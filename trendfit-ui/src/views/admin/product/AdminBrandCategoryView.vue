<template>
  <div class="container-fluid py-4">
    <!-- Header chung -->
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
      <div>
        <h4 class="fw-bold mb-1">Quản trị Thương hiệu & Danh mục</h4>
        <p class="text-secondary small mb-0">
          Quản lý danh sách thương hiệu và danh mục sản phẩm của hệ thống
        </p>
      </div>
    </div>

    <!-- Thông báo lỗi chung nếu có -->
    <div v-if="errorMsg" class="alert alert-danger py-2 mb-4">{{ errorMsg }}</div>

    <!-- BỐ CỤC CHIA ĐÔI MÀN HÌNH (TRÁI: THƯƠNG HIỆU | PHẢI: DANH MỤC) -->
    <div class="row g-4 align-items-start">
      <!-- ================= 1. PHẦN QUẢN LÝ THƯƠNG HIỆU (BÊN TRÁI) ================= -->
      <div class="col-lg-6">
        <div class="card border-0 shadow-sm mb-4">
          <div class="card-header bg-white border-0 border-bottom py-3">
            <h6 class="fw-bold m-0 text-primary">
              {{ brandForm.id ? 'Sửa thương hiệu #' + brandForm.id : '+ Thêm thương hiệu mới' }}
            </h6>
          </div>
          <div class="card-body">
            <div class="row g-3 align-items-end">
              <div class="col-sm-8">
                <label class="form-label small fw-semibold">Tên thương hiệu</label>
                <input
                  v-model.trim="brandForm.ten"
                  class="form-control form-control-sm"
                  placeholder="Nhập tên thương hiệu..."
                  @keyup.enter="saveBrand"
                />
              </div>
              <div class="col-sm-4 d-flex gap-2">
                <button
                  type="button"
                  class="btn btn-primary btn-sm flex-grow-1"
                  :disabled="brandSubmitting"
                  @click="saveBrand"
                >
                  {{ brandForm.id ? 'Cập nhật' : 'Thêm' }}
                </button>
                <button
                  v-if="brandForm.id"
                  type="button"
                  class="btn btn-outline-secondary btn-sm"
                  @click="resetBrandForm"
                >
                  Hủy
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Bảng danh sách Thương hiệu có thanh cuộn riêng -->
        <div class="card border-0 shadow-sm overflow-hidden">
          <div
            class="card-header bg-white border-0 border-bottom d-flex justify-content-between align-items-center flex-wrap gap-2 py-3"
          >
            <span class="fw-semibold">Danh sách thương hiệu ({{ sortedBrands.length }})</span>
            <div class="d-flex align-items-center gap-2">
              <label class="small text-secondary text-nowrap mb-0">Sắp xếp:</label>
              <select v-model="brandSortBy" class="form-select form-select-sm" style="width: 150px">
                <option value="id-asc">ID: Cũ đến mới</option>
                <option value="id-desc">ID: Mới đến cũ</option>
                <option value="name-asc">Tên: A - Z</option>
                <option value="name-desc">Tên: Z - A</option>
              </select>
            </div>
          </div>
          <div class="table-responsive custom-table-scroll">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light sticky-header">
                <tr>
                  <th style="width: 50px" class="text-center">STT</th>
                  <th style="width: 70px">ID</th>
                  <th>Tên thương hiệu</th>
                  <th class="text-center" style="width: 130px">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="sortedBrands.length === 0">
                  <td colspan="4" class="text-center text-muted py-4">Chưa có thương hiệu nào</td>
                </tr>
                <tr v-for="(th, index) in sortedBrands" :key="th.id">
                  <td class="text-center text-muted small">{{ index + 1 }}</td>
                  <td class="fw-semibold">#{{ th.id }}</td>
                  <td>{{ th.ten }}</td>
                  <td class="text-center">
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-warning me-1"
                      @click="kichHoatSuaBrand(th)"
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

      <!-- ================= 2. PHẦN QUẢN LÝ DANH MỤC (BÊN PHẢI) ================= -->
      <div class="col-lg-6">
        <div class="card border-0 shadow-sm mb-4">
          <div class="card-header bg-white border-0 border-bottom py-3">
            <h6 class="fw-bold m-0 text-success">
              {{ categoryForm.id ? 'Sửa danh mục #' + categoryForm.id : '+ Thêm danh mục mới' }}
            </h6>
          </div>
          <div class="card-body">
            <div class="row g-3 align-items-end">
              <div class="col-sm-8">
                <label class="form-label small fw-semibold">Tên danh mục</label>
                <input
                  v-model.trim="categoryForm.ten"
                  class="form-control form-control-sm"
                  placeholder="Nhập tên danh mục..."
                  @keyup.enter="saveCategory"
                />
              </div>
              <div class="col-sm-4 d-flex gap-2">
                <button
                  type="button"
                  class="btn btn-success btn-sm flex-grow-1"
                  :disabled="categorySubmitting"
                  @click="saveCategory"
                >
                  {{ categoryForm.id ? 'Cập nhật' : 'Thêm' }}
                </button>
                <button
                  v-if="categoryForm.id"
                  type="button"
                  class="btn btn-outline-secondary btn-sm"
                  @click="resetCategoryForm"
                >
                  Hủy
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Bảng danh sách Danh mục có thanh cuộn riêng -->
        <div class="card border-0 shadow-sm overflow-hidden">
          <div
            class="card-header bg-white border-0 border-bottom d-flex justify-content-between align-items-center flex-wrap gap-2 py-3"
          >
            <span class="fw-semibold">Danh sách danh mục ({{ sortedCategories.length }})</span>
            <div class="d-flex align-items-center gap-2">
              <label class="small text-secondary text-nowrap mb-0">Sắp xếp:</label>
              <select
                v-model="categorySortBy"
                class="form-select form-select-sm"
                style="width: 150px"
              >
                <option value="id-asc">ID: Cũ đến mới</option>
                <option value="id-desc">ID: Mới đến cũ</option>
                <option value="name-asc">Tên: A - Z</option>
                <option value="name-desc">Tên: Z - A</option>
              </select>
            </div>
          </div>
          <div class="table-responsive custom-table-scroll">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light sticky-header">
                <tr>
                  <th style="width: 50px" class="text-center">STT</th>
                  <th style="width: 70px">ID</th>
                  <th>Tên danh mục</th>
                  <th class="text-center" style="width: 130px">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="sortedCategories.length === 0">
                  <td colspan="4" class="text-center text-muted py-4">Chưa có danh mục nào</td>
                </tr>
                <tr v-for="(dm, index) in sortedCategories" :key="dm.id">
                  <td class="text-center text-muted small">{{ index + 1 }}</td>
                  <td class="fw-semibold">#{{ dm.id }}</td>
                  <td>{{ dm.ten }}</td>
                  <td class="text-center">
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-warning me-1"
                      @click="kichHoatSuaCategory(dm)"
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
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { getAuthHeaders } from '@/utils/adminAuth'

const BRAND_API = 'http://localhost:8080/api/admin/products/brands'
const CATEGORY_API = 'http://localhost:8080/api/admin/products/categories'

// State cho Thương hiệu
const thuongHieus = ref([])
const brandForm = ref({ id: null, ten: '' })
const brandSubmitting = ref(false)
const brandSortBy = ref('id-asc')

// State cho Danh mục
const danhMucs = ref([])
const categoryForm = ref({ id: null, ten: '' })
const categorySubmitting = ref(false)
const categorySortBy = ref('id-asc')

const errorMsg = ref('')

// Load dữ liệu
const loadData = async () => {
  errorMsg.value = ''
  try {
    const [resBrands, resCategories] = await Promise.all([
      axios.get(BRAND_API, { headers: getAuthHeaders() }),
      axios.get(CATEGORY_API, { headers: getAuthHeaders() }),
    ])
    thuongHieus.value = resBrands.data
    danhMucs.value = resCategories.data
  } catch (err) {
    errorMsg.value = layThongBaoLoi(err, 'Không thể tải dữ liệu thương hiệu hoặc danh mục')
  }
}

// Sắp xếp thương hiệu
const sortedBrands = computed(() => {
  let list = [...thuongHieus.value]
  if (brandSortBy.value === 'id-asc') {
    list.sort((a, b) => (a.id || 0) - (b.id || 0))
  } else if (brandSortBy.value === 'id-desc') {
    list.sort((a, b) => (b.id || 0) - (a.id || 0))
  } else if (brandSortBy.value === 'name-asc') {
    list.sort((a, b) => (a.ten || '').localeCompare(b.ten || ''))
  } else if (brandSortBy.value === 'name-desc') {
    list.sort((a, b) => (b.ten || '').localeCompare(a.ten || ''))
  }
  return list
})

// Sắp xếp danh mục
const sortedCategories = computed(() => {
  let list = [...danhMucs.value]
  if (categorySortBy.value === 'id-asc') {
    list.sort((a, b) => (a.id || 0) - (b.id || 0))
  } else if (categorySortBy.value === 'id-desc') {
    list.sort((a, b) => (b.id || 0) - (a.id || 0))
  } else if (categorySortBy.value === 'name-asc') {
    list.sort((a, b) => (a.ten || '').localeCompare(b.ten || ''))
  } else if (categorySortBy.value === 'name-desc') {
    list.sort((a, b) => (b.ten || '').localeCompare(a.ten || ''))
  }
  return list
})

// CRUD Thương hiệu
const saveBrand = async () => {
  errorMsg.value = ''
  if (!brandForm.value.ten) {
    errorMsg.value = 'Vui lòng nhập tên thương hiệu!'
    return
  }

  brandSubmitting.value = true
  try {
    await axios.post(BRAND_API, brandForm.value, { headers: getAuthHeaders() })
    resetBrandForm()
    loadData()
  } catch (err) {
    errorMsg.value = layThongBaoLoi(err, 'Lưu thương hiệu thất bại')
  } finally {
    brandSubmitting.value = false
  }
}

const kichHoatSuaBrand = (th) => {
  brandForm.value = { ...th }
}

const resetBrandForm = () => {
  brandForm.value = { id: null, ten: '' }
}

const xoaThuongHieu = async (id) => {
  if (!confirm('Xóa thương hiệu này?')) return
  try {
    await axios.delete(`${BRAND_API}/${id}`, { headers: getAuthHeaders() })
    loadData()
  } catch (err) {
    alert(layThongBaoLoi(err, 'Xóa thương hiệu thất bại'))
  }
}

// CRUD Danh mục
const saveCategory = async () => {
  errorMsg.value = ''
  if (!categoryForm.value.ten) {
    errorMsg.value = 'Vui lòng nhập tên danh mục!'
    return
  }

  categorySubmitting.value = true
  try {
    await axios.post(CATEGORY_API, categoryForm.value, { headers: getAuthHeaders() })
    resetCategoryForm()
    loadData()
  } catch (err) {
    errorMsg.value = layThongBaoLoi(err, 'Lưu danh mục thất bại')
  } finally {
    categorySubmitting.value = false
  }
}

const kichHoatSuaCategory = (dm) => {
  categoryForm.value = { ...dm }
}

const resetCategoryForm = () => {
  categoryForm.value = { id: null, ten: '' }
}

const xoaDanhMuc = async (id) => {
  if (!confirm('Xóa danh mục này?')) return
  try {
    await axios.delete(`${CATEGORY_API}/${id}`, { headers: getAuthHeaders() })
    loadData()
  } catch (err) {
    alert(layThongBaoLoi(err, 'Xóa danh mục thất bại'))
  }
}

const layThongBaoLoi = (err, macDinh) => {
  const data = err?.response?.data
  if (typeof data === 'string' && data.trim()) return data
  return macDinh + (err?.message ? `: ${err.message}` : '')
}

onMounted(loadData)
</script>

<style scoped>
.table th,
.table td {
  vertical-align: middle;
}

/* Giới hạn chiều cao bảng và tạo thanh cuộn riêng biệt */
.custom-table-scroll {
  max-height: 460px;
  overflow-y: auto;
}

/* Giữ cố định header của bảng khi cuộn danh sách bên trong */
.sticky-header th {
  position: sticky;
  top: 0;
  z-index: 10;
  background-color: #f8f9fa !important;
  box-shadow: inset 0 -1px 0 #dee2e6;
}

/* Tùy chỉnh thanh cuộn gọn gàng */
.custom-table-scroll::-webkit-scrollbar {
  width: 6px;
}
.custom-table-scroll::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 4px;
}
</style>
