<template>
  <div class="container-fluid py-4">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
      <div>
        <h4 class="fw-bold mb-1">Quản lý sản phẩm</h4>
        <p class="text-secondary small mb-0">Thêm, sửa, xóa sản phẩm và biến thể</p>
      </div>
      <button type="button" class="btn btn-primary" @click="moFormThemMoi">+ Thêm sản phẩm mới</button>
    </div>

    <!-- Form thêm/sửa -->
    <div v-if="hienThiForm" class="card border-0 shadow-sm mb-4">
      <div class="card-body">
        <h6 class="fw-bold mb-3">
          {{ dangSua ? 'Sửa sản phẩm #' + (formData.sanPham.id || 'N/A') : 'Thêm sản phẩm mới' }}
        </h6>

        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label small fw-semibold">Tên sản phẩm</label>
            <input v-model="formData.sanPham.ten" class="form-control" required />
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-semibold">Danh mục</label>
            <select v-model="formData.sanPham.danhMuc" class="form-select">
              <option :value="null">-- Chọn Danh mục --</option>
              <option v-for="dm in metadata.danhMucs" :key="dm.id" :value="dm">{{ dm.ten }}</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-semibold">Thương hiệu</label>
            <select v-model="formData.sanPham.thuongHieu" class="form-select">
              <option :value="null">-- Chọn Thương hiệu --</option>
              <option v-for="th in metadata.thuongHieus" :key="th.id" :value="th">{{ th.ten }}</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-semibold">Giới tính</label>
            <select v-model="formData.sanPham.gioiTinh" class="form-select">
              <option value="Nam">Nam</option>
              <option value="Nữ">Nữ</option>
              <option value="Unisex">Unisex</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-semibold">Chất liệu</label>
            <input v-model="formData.sanPham.chatLieu" class="form-control" />
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-semibold">Xuất xứ</label>
            <input v-model="formData.sanPham.xuatXu" class="form-control" />
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-semibold">Năm ra mắt</label>
            <input v-model.number="formData.sanPham.namRaMat" type="number" class="form-control" />
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-semibold">Trạng thái</label>
            <button
              type="button"
              class="btn btn-sm w-100"
              :class="formData.sanPham.dangBan !== false ? 'btn-success' : 'btn-secondary'"
              @click="formData.sanPham.dangBan = formData.sanPham.dangBan === false ? true : false"
            >
              {{ formData.sanPham.dangBan !== false ? 'Đang bán' : 'Ngừng bán' }}
            </button>
          </div>
        </div>

        <div class="mt-3">
          <label class="form-label small fw-semibold">Mô tả chi tiết</label>
          <textarea v-model="formData.sanPham.moTa" class="form-control" rows="3"></textarea>
        </div>

        <!-- Biến thể -->
        <div class="mt-4">
          <h6 class="fw-bold">Biến thể (Size / Màu / SKU / Giá / Trạng thái)</h6>
          <div class="table-responsive">
            <table class="table table-sm table-bordered align-middle">
              <thead class="table-light text-center">
                <tr>
                  <th>Size</th>
                  <th>Màu</th>
                  <th>Mã SKU</th>
                  <th>Tồn kho</th>
                  <th>Giá nhập</th>
                  <th>Giá gốc</th>
                  <th>Giá sale</th>
                  <th>Trạng thái</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(v, idx) in formData.bienTheSanPhams" :key="idx">
                  <td>
                    <select v-model="v.kichCo" class="form-select form-select-sm">
                      <option :value="null">-- Size --</option>
                      <option v-for="kc in metadata.kichCos" :key="kc.id" :value="kc">{{ kc.tenKichCo }}</option>
                    </select>
                  </td>
                  <td>
                    <select v-model="v.mauSac" class="form-select form-select-sm">
                      <option :value="null">-- Màu --</option>
                      <option v-for="ms in metadata.mauSacs" :key="ms.id" :value="ms">{{ ms.tenMau }}</option>
                    </select>
                  </td>
                  <td><input v-model="v.maSku" class="form-control form-control-sm" /></td>
                  <td><input v-model.number="v.soLuongTon" type="number" class="form-control form-control-sm text-center" /></td>
                  <td><input v-model.number="v.giaNhap" type="number" class="form-control form-control-sm" /></td>
                  <td><input v-model.number="v.gia" type="number" class="form-control form-control-sm" /></td>
                  <td><input v-model.number="v.giaSale" type="number" class="form-control form-control-sm" /></td>
                  <td class="text-center">
                    <button
                      type="button"
                      class="btn btn-sm w-100"
                      :class="v.dangBan ? 'btn-success' : 'btn-secondary'"
                      @click="v.dangBan = !v.dangBan"
                    >
                      {{ v.dangBan ? 'Bán' : 'Ẩn' }}
                    </button>
                  </td>
                  <td class="text-center">
                    <button type="button" class="btn btn-outline-danger btn-sm" @click="formData.bienTheSanPhams.splice(idx, 1)">Xóa</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <button type="button" class="btn btn-outline-dark btn-sm" @click="themBienTheMoi">+ Thêm biến thể</button>
        </div>

        <!-- Ảnh -->
        <div class="mt-4">
          <h6 class="fw-bold">Ảnh sản phẩm</h6>
          <table class="table table-sm table-bordered align-middle">
            <thead class="table-light">
              <tr>
                <th>URL ảnh</th>
                <th class="text-center">Ảnh chính</th>
                <th>Upload</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(img, idx) in formData.anhSanPhams" :key="idx">
                <td><input v-model="img.urlAnh" class="form-control form-control-sm" /></td>
                <td class="text-center"><input type="checkbox" v-model="img.laAnhChinh" /></td>
                <td>
                  <label :for="'upload-' + idx" class="btn btn-outline-primary btn-sm mb-0">Chọn file</label>
                  <input :id="'upload-' + idx" type="file" accept="image/*" class="d-none" @change="handleImageUpload($event, idx)" />
                </td>
                <td>
                  <button type="button" class="btn btn-outline-danger btn-sm" @click="formData.anhSanPhams.splice(idx, 1)">Xóa</button>
                </td>
              </tr>
            </tbody>
          </table>
          <button type="button" class="btn btn-outline-dark btn-sm" @click="themAnhMoi">+ Thêm dòng ảnh</button>
        </div>

        <div class="d-flex gap-2 mt-4">
          <button type="button" class="btn btn-primary" @click="saveFullProduct">Lưu</button>
          <button type="button" class="btn btn-outline-secondary" @click="hienThiForm = false">Hủy</button>
        </div>
      </div>
    </div>

    <!-- Bảng danh sách + tổng số -->
    <div class="card border-0 shadow-sm overflow-hidden">
      <div class="card-header bg-white border-0 border-bottom d-flex justify-content-between align-items-center py-3">
        <span class="fw-semibold">Danh sách sản phẩm</span>
        <span class="badge text-bg-primary rounded-pill">Tổng: {{ danhSachSanPham.length }}</span>
      </div>
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th style="width: 70px">ID</th>
              <th>Tên</th>
              <th>Danh mục</th>
              <th>Thương hiệu</th>
              <th>Giới tính</th>
              <th class="text-center" style="width: 140px">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="danhSachSanPham.length === 0">
              <td colspan="6" class="text-center text-muted py-4">Chưa có sản phẩm nào</td>
            </tr>
            <tr v-for="sp in danhSachSanPham" :key="sp.sanPham.id">
              <td class="fw-semibold">#{{ sp.sanPham.id }}</td>
              <td>{{ sp.sanPham.ten }}</td>
              <td>{{ sp.sanPham.danhMuc?.ten || '—' }}</td>
              <td>{{ sp.sanPham.thuongHieu?.ten || '—' }}</td>
              <td>{{ sp.sanPham.gioiTinh || '—' }}</td>
              <td class="text-center">
                <button type="button" class="btn btn-sm btn-outline-warning me-1" @click="kichHoatSuaForm(sp.sanPham)">Sửa</button>
                <button type="button" class="btn btn-sm btn-outline-danger" @click="deleteProduct(sp.sanPham.id)">Xóa</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { getAuthHeaders } from '@/utils/adminAuth'

const CLOUD_NAME = 'dqciew3rk'
const UPLOAD_PRESET = 'trendfit_preset'

const API_BASE = 'http://localhost:8080/api/admin/products'

const apiAdmin = axios.create()
apiAdmin.interceptors.request.use((config) => {
  config.headers = { ...config.headers, ...getAuthHeaders() }
  return config
})

const danhSachSanPham = ref([])
const metadata = ref({ danhMucs: [], thuongHieus: [], kichCos: [], mauSacs: [] })
const hienThiForm = ref(false)
const dangSua = ref(false)

const formData = ref({
  sanPham: {},
  bienTheSanPhams: [],
  anhSanPhams: [],
})

const loadData = async () => {
  try {
    const [resSp, resMeta] = await Promise.all([
      apiAdmin.get(API_BASE),
      apiAdmin.get(`${API_BASE}/metadata`),
    ])
    danhSachSanPham.value = resSp.data
    metadata.value = resMeta.data
  } catch (err) {
    console.error('Lỗi load data:', err)
  }
}

const handleImageUpload = async (event, idx) => {
  const file = event.target.files[0]
  if (!file) return

  const uploadForm = new FormData()
  uploadForm.append('file', file)
  uploadForm.append('upload_preset', UPLOAD_PRESET)

  try {
    const res = await axios.post(
      `https://api.cloudinary.com/v1_1/${CLOUD_NAME}/image/upload`,
      uploadForm,
    )
    formData.value.anhSanPhams[idx].urlAnh = res.data.secure_url
  } catch (err) {
    alert('Upload ảnh thất bại!')
    console.error(err)
  }
}

// Cập nhật hàm thêm biến thể mới đầy đủ các trường khớp với Entity Java
const themBienTheMoi = () => {
  formData.value.bienTheSanPhams.push({
    kichCo: null,
    mauSac: null,
    maSku: '',
    soLuongTon: 0,
    giaNhap: 0,
    gia: 0,
    giaSale: null,
    dangBan: true, // Mặc định là đang bán hiển thị cho khách
  })
}

const themAnhMoi = () => {
  formData.value.anhSanPhams.push({ urlAnh: '', laAnhChinh: false })
}

const saveFullProduct = async () => {
  try {
    if (dangSua.value) {
      await apiAdmin.put(`${API_BASE}/full`, formData.value)
    } else {
      await apiAdmin.post(`${API_BASE}/full`, formData.value)
    }
    alert('✅ Lưu thành công!')
    hienThiForm.value = false
    loadData()
  } catch (err) {
    alert('❌ Lỗi lưu: ' + (err.response?.data?.message || err.message))
  }
}

const kichHoatSuaForm = async (sp) => {
  dangSua.value = true
  hienThiForm.value = true

  try {
    const [resVariants, resImages] = await Promise.all([
      apiAdmin.get(`${API_BASE}/${sp.id}/variants`),
      apiAdmin.get(`${API_BASE}/${sp.id}/images`),
    ])

    formData.value = {
      sanPham: {
        ...sp,
        dangBan: sp.dangBan !== false, // Đảm bảo luôn nhận đúng true/false
      },
      bienTheSanPhams: resVariants.data || [],
      anhSanPhams: resImages.data || [],
    }
  } catch (err) {
    console.error(err)
  }
}

const moFormThemMoi = () => {
  dangSua.value = false
  hienThiForm.value = true
  formData.value = {
    sanPham: {
      ten: '',
      moTa: '',
      danhMuc: null,
      thuongHieu: null,
      gioiTinh: 'Unisex',
      chatLieu: '',
      xuatXu: 'Việt Nam',
      namRaMat: new Date().getFullYear(),
      dangBan: true,
    },
    bienTheSanPhams: [],
    anhSanPhams: [],
  }
}

const deleteProduct = async (id) => {
  if (!confirm('Xóa sản phẩm này?')) return
  try {
    await apiAdmin.delete(`${API_BASE}/${id}`)
    alert('✅ Xóa thành công!')
    loadData()
  } catch (err) {
    alert('❌ Lỗi xóa: ' + (err.response?.data || err.message))
  }
}

onMounted(loadData)
</script>

<style scoped>
/* Tùy chỉnh nhỏ giúp bảng biến thể gọn gàng hơn trên màn hình quản trị */
.table th,
.table td {
  vertical-align: middle;
}
</style>
