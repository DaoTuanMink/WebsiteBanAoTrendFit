<template>
  <div class="container-fluid py-4 position-relative">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
      <div>
        <h4 class="fw-bold mb-1">Quản lý khách hàng</h4>
        <p class="text-secondary small mb-0">Danh sách và chỉnh sửa thông tin khách hàng</p>
      </div>
      <button v-if="!showForm" type="button" class="btn btn-primary shadow-sm" @click="moFormMoi">
        + Thêm khách hàng
      </button>
    </div>

    <!-- BỐ CỤC CHIA ĐÔI MÀN HÌNH -->
    <div class="row g-4 align-items-start position-relative">
      <!-- BÊN TRÁI: BẢNG DANH SÁCH KHÁCH HÀNG & BỘ LỌC -->
      <div :class="showForm ? 'col-lg-7' : 'col-12'" class="transition-all">
        <div class="card border-0 shadow-sm overflow-hidden">
          <div
            class="card-header bg-white border-0 border-bottom d-flex justify-content-between align-items-center flex-wrap gap-2 py-3"
          >
            <span class="fw-semibold">Danh sách khách hàng ({{ filteredCustomers.length }})</span>

            <!-- THANH BỘ LỌC & SẮP XẾP -->
            <div class="d-flex align-items-center flex-wrap gap-2">
              <input
                v-model="searchKeyword"
                type="text"
                class="form-control form-control-sm"
                placeholder="Tìm tên, email, sĐT..."
                style="width: 150px"
              />
              <select
                v-model="filterStatus"
                class="form-select form-select-sm"
                style="width: 130px"
              >
                <option value="all">Tất cả trạng thái</option>
                <option value="active">Đang hoạt động</option>
                <option value="locked">Đã khóa</option>
              </select>
              <select v-model="sortBy" class="form-select form-select-sm" style="width: 140px">
                <option value="id-desc">Mới nhất (ID)</option>
                <option value="id-asc">Cũ nhất (ID)</option>
                <option value="name-asc">Tên: A - Z</option>
                <option value="name-desc">Tên: Z - A</option>
              </select>
            </div>
          </div>

          <div class="table-responsive custom-table-scroll">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light sticky-header text-center">
                <tr>
                  <th style="width: 50px">STT</th>
                  <th style="width: 60px">ID</th>
                  <th style="width: 60px">Ảnh</th>
                  <th class="text-start">Họ và tên</th>
                  <th class="text-start">Email</th>
                  <th>Số điện thoại</th>
                  <th>Ngày tạo</th>
                  <th>Trạng thái</th>
                  <th style="width: 130px">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(c, index) in filteredCustomers"
                  :key="c.id"
                  :class="{ 'table-active': showForm && form.id === c.id }"
                >
                  <td class="text-center text-muted small">{{ index + 1 }}</td>
                  <td class="text-center fw-semibold">#{{ c.id }}</td>
                  <td class="text-center">
                    <img
                      :src="
                        c.anhDaiDien ||
                        'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100'
                      "
                      class="rounded-circle border"
                      style="width: 32px; height: 32px; object-fit: cover"
                      alt="Avatar"
                    />
                  </td>
                  <td class="fw-semibold text-dark">{{ c.hoTen }}</td>
                  <td class="text-secondary small">{{ c.email }}</td>
                  <td class="text-center">{{ c.soDienThoai || '—' }}</td>
                  <td class="small text-muted text-center">{{ formatDateTime(c.ngayTao) }}</td>
                  <td class="text-center">
                    <button
                      type="button"
                      class="btn btn-sm py-0 px-2 font-xs"
                      :class="c.dangHoatDong ? 'btn-success' : 'btn-secondary'"
                      @click="toggleStatus(c.id)"
                    >
                      {{ c.dangHoatDong ? 'Hoạt động' : 'Đã khóa' }}
                    </button>
                  </td>
                  <td class="text-center">
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-warning me-1 py-0 px-2"
                      @click="editCustomer(c)"
                    >
                      Sửa
                    </button>
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-danger py-0 px-2"
                      @click="xoaCustomer(c.id)"
                    >
                      Xóa
                    </button>
                  </td>
                </tr>
                <tr v-if="filteredCustomers.length === 0">
                  <td colspan="9" class="text-center py-4 text-muted">
                    Không tìm thấy khách hàng phù hợp.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- BÊN PHẢI: FORM THÊM / SỬA GHIM CỐ ĐỊNH -->
      <div v-if="showForm" class="col-lg-5">
        <div class="card border-0 shadow-sm always-fixed-form">
          <div class="card-body p-4">
            <div class="d-flex justify-content-between align-items-center mb-3 border-bottom pb-2">
              <h5 class="fw-bold m-0 text-primary">
                {{ editMode ? 'Cập nhật khách hàng #' + form.id : 'Thêm khách hàng mới' }}
              </h5>
              <button type="button" class="btn-close" @click="showForm = false"></button>
            </div>

            <div class="row g-3">
              <div class="col-12">
                <label class="form-label small fw-semibold">Họ và tên (*)</label>
                <input
                  v-model="form.hoTen"
                  class="form-control form-control-sm"
                  placeholder="Nhập họ tên"
                />
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">Email (*)</label>
                <input
                  v-model="form.email"
                  :disabled="editMode"
                  class="form-control form-control-sm"
                  placeholder="email@example.com"
                />
              </div>
              <div class="col-12" v-if="!editMode">
                <label class="form-label small fw-semibold">Mật khẩu (*)</label>
                <input
                  v-model="form.matKhau"
                  type="password"
                  class="form-control form-control-sm"
                  placeholder="Mật khẩu đăng nhập"
                />
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">Số điện thoại</label>
                <input
                  v-model="form.soDienThoai"
                  class="form-control form-control-sm"
                  placeholder="0987654321"
                />
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">Link ảnh đại diện (URL)</label>
                <input
                  v-model="form.anhDaiDien"
                  class="form-control form-control-sm"
                  placeholder="https://..."
                />
              </div>
            </div>

            <div class="d-flex gap-2 mt-4 pt-3 border-top">
              <button
                type="button"
                class="btn btn-primary btn-sm flex-grow-1"
                @click="saveCustomer"
              >
                Lưu lại
              </button>
              <button
                type="button"
                class="btn btn-outline-secondary btn-sm"
                @click="showForm = false"
              >
                Hủy
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- NÚT CUỘN VỀ ĐẦU TRANG Ở GÓC DƯỚI BÊN PHẢI -->
    <button
      v-show="showScrollTopBtn"
      type="button"
      class="btn btn-dark shadow rounded-circle scroll-top-btn"
      @click="scrollToTop"
      title="Về đầu trang"
    >
      ↑
    </button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import { getAuthHeaders } from '@/utils/adminAuth'

const API = 'http://localhost:8080/api/admin/customers'

const apiAdmin = axios.create()
apiAdmin.interceptors.request.use((config) => {
  config.headers = { ...config.headers, ...getAuthHeaders() }
  return config
})

const customers = ref([])
const showForm = ref(false)
const editMode = ref(false)
const form = ref({})
const showScrollTopBtn = ref(false)

// State cho bộ lọc & sắp xếp
const searchKeyword = ref('')
const filterStatus = ref('all') // 'all', 'active', 'locked'
const sortBy = ref('id-desc') // 'id-desc', 'id-asc', 'name-asc', 'name-desc'

const formatDateTime = (dt) => {
  if (!dt) return '—'
  return new Date(dt).toLocaleString('vi-VN')
}

// Danh sách sau khi lọc và sắp xếp
const filteredCustomers = computed(() => {
  let list = [...customers.value]

  // 1. Lọc theo từ khóa (Họ tên, Email, Số điện thoại)
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase().trim()
    list = list.filter(
      (c) =>
        (c.hoTen && c.hoTen.toLowerCase().includes(keyword)) ||
        (c.email && c.email.toLowerCase().includes(keyword)) ||
        (c.soDienThoai && c.soDienThoai.toLowerCase().includes(keyword)),
    )
  }

  // 2. Lọc theo trạng thái hoạt động
  if (filterStatus.value === 'active') {
    list = list.filter((c) => c.dangHoatDong === true)
  } else if (filterStatus.value === 'locked') {
    list = list.filter((c) => c.dangHoatDong === false)
  }

  // 3. Sắp xếp
  if (sortBy.value === 'id-desc') {
    list.sort((a, b) => (b.id || 0) - (a.id || 0))
  } else if (sortBy.value === 'id-asc') {
    list.sort((a, b) => (a.id || 0) - (b.id || 0))
  } else if (sortBy.value === 'name-asc') {
    list.sort((a, b) => (a.hoTen || '').localeCompare(b.hoTen || ''))
  } else if (sortBy.value === 'name-desc') {
    list.sort((a, b) => (b.hoTen || '').localeCompare(a.hoTen || ''))
  }

  return list
})

const handleScroll = () => {
  showScrollTopBtn.value = window.scrollY > 300
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const loadCustomers = async () => {
  try {
    const res = await apiAdmin.get(API)
    customers.value = res.data
  } catch (err) {
    alert('Không thể tải danh sách khách hàng: ' + (err.response?.data || err.message))
  }
}

const moFormMoi = () => {
  form.value = {
    hoTen: '',
    email: '',
    matKhau: '',
    soDienThoai: '',
    anhDaiDien: '',
    dangHoatDong: true,
  }
  editMode.value = false
  showForm.value = true
}

const editCustomer = (c) => {
  form.value = { ...c }
  editMode.value = true
  showForm.value = true
}

const toggleStatus = async (id) => {
  try {
    await apiAdmin.put(`${API}/${id}/toggle-status`)
    loadCustomers()
  } catch (err) {
    alert('Đổi trạng thái thất bại: ' + (err.response?.data || err.message))
  }
}

const xoaCustomer = async (id) => {
  if (confirm('Bạn có chắc chắn muốn xóa tài khoản khách hàng này?')) {
    try {
      await apiAdmin.delete(`${API}/${id}`)
      loadCustomers()
    } catch (err) {
      alert('Xóa thất bại: ' + (err.response?.data || err.message))
    }
  }
}

const saveCustomer = async () => {
  if (!form.value.hoTen || !form.value.email) {
    alert('Vui lòng điền đủ Họ tên và Email!')
    return
  }

  try {
    if (editMode.value) {
      await apiAdmin.put(`${API}/${form.value.id}`, form.value)
    } else {
      await apiAdmin.post(API, form.value)
    }
    alert('Lưu thông tin thành công!')
    showForm.value = false
    loadCustomers()
  } catch (e) {
    alert('Lỗi: ' + (e.response?.data || e.message))
  }
}

onMounted(() => {
  loadCustomers()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.table th,
.table td {
  vertical-align: middle;
}
.transition-all {
  transition: all 0.3s ease;
}

/* Giới hạn chiều cao bảng và có thanh cuộn riêng */
.custom-table-scroll {
  max-height: 520px;
  overflow-y: auto;
}

/* Cố định tiêu đề bảng khi cuộn dọc danh sách */
.sticky-header th {
  position: sticky;
  top: 0;
  z-index: 10;
  background-color: #f8f9fa !important;
  box-shadow: inset 0 -1px 0 #dee2e6;
}

.custom-table-scroll::-webkit-scrollbar {
  width: 6px;
}
.custom-table-scroll::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 4px;
}

/* Ghim cố định form bên phải */
.always-fixed-form {
  position: fixed;
  top: 20px;
  right: 25px;
  width: 40%;
  max-height: calc(100vh - 40px);
  overflow-y: auto;
  z-index: 1020;
  box-shadow: 0 1rem 3rem rgba(0, 0, 0, 0.175) !important;
  background-color: #ffffff;
}

.always-fixed-form::-webkit-scrollbar {
  width: 6px;
}
.always-fixed-form::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 4px;
}

/* Nút cuộn về đầu trang ở góc dưới bên phải */
.scroll-top-btn {
  position: fixed;
  bottom: 25px;
  right: 25px;
  width: 45px;
  height: 45px;
  font-size: 20px;
  z-index: 1030;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.scroll-top-btn:hover {
  transform: translateY(-3px);
}

@media (max-width: 991.98px) {
  .always-fixed-form {
    position: relative;
    top: 0;
    right: 0;
    width: 100%;
  }
}
</style>
