<template>
  <div class="container-fluid py-4">
    <button @click="moFormThemMoi" class="btn btn-primary mb-3">+ THÊM SẢN PHẨM MỚI</button>

    <!-- Form thêm/sửa -->
    <div v-if="hienThiForm" class="card p-4 mb-4 shadow">
      <h5 class="fw-bold">
        {{ dangSua ? 'Sửa sản phẩm #' + (formData.sanPham.id || 'N/A') : 'Thêm sản phẩm mới' }}
      </h5>

      <!-- Thông tin cơ bản -->
      <div class="row g-3">
        <div class="col-md-6">
          <label class="form-label">Tên sản phẩm</label>
          <input v-model="formData.sanPham.ten" class="form-control" required />
        </div>

        <div class="col-md-3">
          <label class="form-label">Danh mục</label>
          <select v-model="formData.sanPham.danhMuc" class="form-select">
            <option :value="null">-- Chọn Danh mục --</option>
            <option v-for="dm in metadata.danhMucs" :key="dm.id" :value="dm">{{ dm.ten }}</option>
          </select>
        </div>
        <div class="col-md-3">
          <label class="form-label">Thương hiệu</label>
          <select v-model="formData.sanPham.thuongHieu" class="form-select">
            <option :value="null">-- Chọn Thương hiệu --</option>
            <option v-for="th in metadata.thuongHieus" :key="th.id" :value="th">
              {{ th.ten }}
            </option>
          </select>
        </div>

        <div class="col-md-3">
          <label class="form-label">Giới tính</label>
          <select v-model="formData.sanPham.gioiTinh" class="form-select">
            <option value="Nam">Nam</option>
            <option value="Nữ">Nữ</option>
            <option value="Unisex">Unisex</option>
          </select>
        </div>
        <div class="col-md-3">
          <label class="form-label">Chất liệu</label>
          <input v-model="formData.sanPham.chatLieu" class="form-control" />
        </div>
        <div class="col-md-3">
          <label class="form-label">Xuất xứ</label>
          <input v-model="formData.sanPham.xuatXu" class="form-control" />
        </div>
        <div class="col-md-3">
          <label class="form-label">Năm ra mắt</label>
          <input v-model.number="formData.sanPham.namRaMat" type="number" class="form-control" />
        </div>
      </div>
      <div class="col-md-3">
        <label class="form-label">Trạng thái sản phẩm</label>
        <button
          type="button"
          class="btn btn-sm w-100 mt-1"
          :class="formData.sanPham.dangBan !== false ? 'btn-success' : 'btn-secondary'"
          @click="formData.sanPham.dangBan = formData.sanPham.dangBan === false ? true : false"
        >
          {{ formData.sanPham.dangBan !== false ? '🟢 Đang bán' : '⚪ Ngừng bán' }}
        </button>
      </div>

      <div class="mt-3">
        <label class="form-label">Mô tả chi tiết</label>
        <textarea v-model="formData.sanPham.moTa" class="form-control" rows="3"></textarea>
      </div>

      <!-- Biến thể sản phẩm (Đã bổ sung SKU, Giá sale, Trạng thái) -->
      <div class="mt-4">
        <h6>Biến thể (Size / Màu / SKU / Giá / Trạng thái)</h6>
        <div class="table-responsive">
          <table class="table table-sm table-bordered align-middle">
            <thead class="table-light text-center">
              <tr>
                <th style="width: 12%">Size</th>
                <th style="width: 12%">Màu</th>
                <th style="width: 12%">Mã SKU</th>
                <th style="width: 10%">Tồn kho</th>
                <th style="width: 11%">Giá nhập</th>
                <th style="width: 11%">Giá gốc</th>
                <th style="width: 11%">Giá sale</th>
                <th style="width: 11%">Trạng thái</th>
                <th style="width: 5%"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(v, idx) in formData.bienTheSanPhams" :key="idx">
                <td>
                  <select v-model="v.kichCo" class="form-select form-select-sm">
                    <option :value="null">-- Size --</option>
                    <option v-for="kc in metadata.kichCos" :key="kc.id" :value="kc">
                      {{ kc.tenKichCo }}
                    </option>
                  </select>
                </td>
                <td>
                  <select v-model="v.mauSac" class="form-select form-select-sm">
                    <option :value="null">-- Màu --</option>
                    <option v-for="ms in metadata.mauSacs" :key="ms.id" :value="ms">
                      {{ ms.tenMau }}
                    </option>
                  </select>
                </td>
                <td>
                  <input
                    v-model="v.maSku"
                    type="text"
                    class="form-control form-control-sm"
                    placeholder="VD: SKU-SP-M-XANH"
                  />
                </td>
                <td>
                  <input
                    v-model.number="v.soLuongTon"
                    type="number"
                    class="form-control form-control-sm text-center"
                  />
                </td>
                <td>
                  <input
                    v-model.number="v.giaNhap"
                    type="number"
                    class="form-control form-control-sm"
                    placeholder="Giá vốn"
                  />
                </td>
                <td>
                  <input
                    v-model.number="v.gia"
                    type="number"
                    class="form-control form-control-sm"
                    placeholder="Giá bán"
                  />
                </td>
                <td>
                  <input
                    v-model.number="v.giaSale"
                    type="number"
                    class="form-control form-control-sm"
                    placeholder="Giá khuyến mãi"
                  />
                </td>
                <td class="text-center">
                  <!-- Nút chuyển đổi trạng thái Đang bán / Ngừng bán -->
                  <button
                    type="button"
                    class="btn btn-sm w-100"
                    :class="v.dangBan ? 'btn-success' : 'btn-secondary'"
                    @click="v.dangBan = !v.dangBan"
                  >
                    {{ v.dangBan ? 'Đang bán' : 'Ngừng bán' }}
                  </button>
                </td>
                <td class="text-center">
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
        </div>
        <button @click="themBienTheMoi" class="btn btn-dark btn-sm">+ Thêm biến thể</button>
      </div>

      <!-- Ảnh sản phẩm -->
      <div class="mt-4">
        <h6>Ảnh sản phẩm</h6>
        <table class="table table-sm table-bordered">
          <thead class="table-light">
            <tr>
              <th>URL Ảnh</th>
              <th>Ảnh chính</th>
              <th>Upload từ máy</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(img, idx) in formData.anhSanPhams" :key="idx">
              <td>
                <input
                  v-model="img.urlAnh"
                  class="form-control form-control-sm"
                  placeholder="https://"
                />
              </td>
              <td class="text-center"><input type="checkbox" v-model="img.laAnhChinh" /></td>
              <td>
                <label :for="'upload-' + idx" class="btn btn-outline-primary btn-sm mb-0"
                  >📤 Chọn ảnh</label
                >
                <input
                  type="file"
                  :id="'upload-' + idx"
                  class="d-none"
                  accept="image/*"
                  @change="handleImageUpload($event, idx)"
                />
              </td>
              <td>
                <button @click="formData.anhSanPhams.splice(idx, 1)" class="btn btn-danger btn-sm">
                  x
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <button @click="themAnhMoi" class="btn btn-dark btn-sm">+ Thêm dòng ảnh</button>
      </div>

      <div class="mt-4">
        <button @click="saveFullProduct" class="btn btn-success me-2">💾 LƯU</button>
        <button @click="hienThiForm = false" class="btn btn-secondary">Hủy</button>
      </div>
    </div>

    <!-- Bảng danh sách -->
    <div class="card shadow">
      <div class="card-body">
        <table class="table table-hover align-middle">
          <thead class="table-dark">
            <tr>
              <th>ID</th>
              <th>Tên</th>
              <th>Danh mục</th>
              <th>Thương hiệu</th>
              <th>Giới tính</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="sp in danhSachSanPham" :key="sp.sanPham.id">
              <td>{{ sp.sanPham.id }}</td>
              <td>{{ sp.sanPham.ten }}</td>
              <td>{{ sp.sanPham.danhMuc?.ten }}</td>
              <td>{{ sp.sanPham.thuongHieu?.ten }}</td>
              <td>{{ sp.sanPham.gioiTinh }}</td>
              <td>
                <button class="btn btn-warning btn-sm me-2" @click="kichHoatSuaForm(sp.sanPham)">
                  Sửa
                </button>
                <button class="btn btn-danger btn-sm" @click="deleteProduct(sp.sanPham.id)">
                  Xóa
                </button>
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
