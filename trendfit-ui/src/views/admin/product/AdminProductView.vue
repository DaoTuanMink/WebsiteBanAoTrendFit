<template>
  <div class="container-fluid py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h1 class="h3 mb-0 text-gray-800">Quản lý Sản phẩm Admin</h1>
      <button @click="hienThiFormThemMoi" class="btn btn-primary">
        <i class="ri-add-line mr-1"></i> Thêm sản phẩm mới
      </button>
    </div>

    <!-- Thanh tìm kiếm và lọc -->
    <div class="card shadow mb-4">
      <div class="card-body row">
        <div class="col-md-4 mb-2">
          <input
            v-model="tuKhoATimKiem"
            @input="taiDuLieuTuDatabase"
            type="text"
            class="form-control"
            placeholder="Tìm theo tên sản phẩm hoặc mã..."
          />
        </div>
        <div class="col-md-3 mb-2">
          <select v-model="danhMucDuocChon" @change="taiDuLieuTuDatabase" class="form-control">
            <option value="">-- Tất cả danh mục --</option>
            <option v-for="dm in listDanhMucOption" :key="dm.id" :value="dm.id">
              {{ dm.ten }}
            </option>
          </select>
        </div>
        <div class="col-md-3 mb-2">
          <select v-model="thuongHieuDuocChon" @change="taiDuLieuTuDatabase" class="form-control">
            <option value="">-- Tất cả thương hiệu --</option>
            <option v-for="th in listThuongHieuOption" :key="th.id" :value="th.id">
              {{ th.ten }}
            </option>
          </select>
        </div>
      </div>
    </div>

    <!-- Trạng thái Loading -->
    <div v-if="loading" class="d-flex justify-content-center my-5">
      <div class="spinner-border text-primary" role="status">
        <span class="sr-only">Đang tải dữ liệu...</span>
      </div>
    </div>

    <!-- Bảng danh sách sản phẩm -->
    <div v-else class="card shadow mb-4">
      <div class="table-responsive">
        <table class="table table-bordered table-hover mb-0">
          <thead class="thead-light">
            <tr>
              <th>STT</th>
              <th>Hình ảnh</th>
              <th>Tên sản phẩm</th>
              <th>Danh mục</th>
              <th>Thương hiệu</th>
              <th>Giá cơ bản</th>
              <th>Trạng thái</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(sp, index) in danhSachSanPham" :key="sp.id">
              <td>{{ index + 1 }}</td>
              <td>
                <img
                  :src="sp.anhChinh || 'https://via.placeholder.com/50'"
                  class="img-thumbnail"
                  style="max-width: 60px; max-height: 60px"
                />
              </td>
              <td>
                <strong>{{ sp.ten }}</strong>
                <div class="small text-muted">Slug: {{ sp.slug }}</div>
              </td>
              <td>{{ sp.danhMuc?.ten || 'N/A' }}</td>
              <td>{{ sp.thuongHieu?.ten || 'N/A' }}</td>
              <td>{{ dinhDangGia(sp.giaCoBan) }}</td>
              <td>
                <span
                  :class="sp.trangThaiKinhDoanh ? 'badge badge-success' : 'badge badge-secondary'"
                >
                  {{ sp.trangThaiKinhDoanh ? 'Đang mở bán' : 'Ẩn / Kho' }}
                </span>
              </td>
              <td>
                <button @click="hienThiFormChinhSua(sp)" class="btn btn-sm btn-info mr-2">
                  <i class="ri-edit-line"></i> Sửa
                </button>
                <button @click="xoaSanPham(sp.id)" class="btn btn-sm btn-danger">
                  <i class="ri-delete-bin-line"></i> Xóa
                </button>
              </td>
            </tr>
            <tr v-if="danhSachSanPham.length === 0">
              <td colspan="8" class="text-center text-muted py-4">
                Không tìm thấy sản phẩm nào khớp với điều kiện.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal Form Thêm/Sửa Sản Phẩm -->
    <div
      v-if="hienThiForm"
      class="modal d-block"
      style="background: rgba(0, 0, 0, 0.5); overflow-y: auto"
    >
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ dangSua ? 'Cập nhật sản phẩm TrendFit' : 'Thêm sản phẩm TrendFit mới' }}
            </h5>
            <button type="button" class="close" @click="dongForm">&times;</button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="luuSanPham">
              <div class="row">
                <div class="col-md-6 form-group">
                  <label>Tên sản phẩm <span class="text-danger">*</span></label>
                  <input
                    v-model="formFullProduct.sanPham.ten"
                    type="text"
                    class="form-control"
                    required
                  />
                </div>
                <div class="col-md-6 form-group">
                  <label>Đường dẫn mẫu (Slug)</label>
                  <input
                    v-model="formFullProduct.sanPham.slug"
                    type="text"
                    class="form-control"
                    placeholder="Tự động sinh nếu để trống"
                  />
                </div>
              </div>

              <div class="row">
                <div class="col-md-6 form-group">
                  <label>Danh mục sản phẩm <span class="text-danger">*</span></label>
                  <select v-model="formFullProduct.sanPham.danhMucId" class="form-control" required>
                    <option value="">-- Chọn danh mục --</option>
                    <option v-for="dm in listDanhMucOption" :key="dm.id" :value="dm.id">
                      {{ dm.ten }}
                    </option>
                  </select>
                </div>
                <div class="col-md-6 form-group">
                  <label>Thương hiệu <span class="text-danger">*</span></label>
                  <select
                    v-model="formFullProduct.sanPham.thuongHieuId"
                    class="form-control"
                    required
                  >
                    <option value="">-- Chọn thương hiệu --</option>
                    <option v-for="th in listThuongHieuOption" :key="th.id" :value="th.id">
                      {{ th.ten }}
                    </option>
                  </select>
                </div>
              </div>

              <div class="row">
                <div class="col-md-6 form-group">
                  <label>Giá cơ bản (VNĐ) <span class="text-danger">*</span></label>
                  <input
                    v-model.number="formFullProduct.sanPham.giaCoBan"
                    type="number"
                    class="form-control"
                    min="0"
                    required
                  />
                </div>
                <div class="col-md-6 form-group">
                  <label>Trạng thái kinh doanh</label>
                  <select v-model="formFullProduct.sanPham.trangThaiKinhDoanh" class="form-control">
                    <option :value="true">Mở bán công khai</option>
                    <option :value="false">Ẩn vào kho lưu trữ</option>
                  </select>
                </div>
              </div>

              <div class="form-group">
                <label>Mô tả chi tiết sản phẩm</label>
                <textarea
                  v-model="formFullProduct.sanPham.moTa"
                  class="form-control"
                  rows="3"
                ></textarea>
              </div>

              <div class="form-group">
                <label>Đường dẫn Ảnh đại diện chính (URL)</label>
                <input
                  v-model="formFullProduct.sanPham.anhChinh"
                  type="text"
                  class="form-control"
                  placeholder="Nhập liên kết ảnh hặc URL..."
                />
              </div>

              <div class="modal-footer px-0 pb-0">
                <button type="button" class="btn btn-secondary" @click="dongForm">Hủy bỏ</button>
                <button type="submit" class="btn btn-success">Xác nhận Lưu</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const API_BASE = 'http://localhost:8080/api/admin/products'
const API_PUBLIC = 'http://localhost:8080/api/public'

const danhSachSanPham = ref([])
const loading = ref(true)
const hienThiForm = ref(false)
const dangSua = ref(false)

const tuKhoATimKiem = ref('')
const danhMucDuocChon = ref('')
const thuongHieuDuocChon = ref('')

const listDanhMucOption = ref([])
const listThuongHieuOption = ref([])

const formFullProduct = ref(createEmptyForm())

function createEmptyForm() {
  return {
    sanPham: {
      id: null,
      ten: '',
      slug: '',
      moTa: '',
      giaCoBan: 0,
      anhChinh: '',
      danhMucId: '',
      thuongHieuId: '',
      trangThaiKinhDoanh: true,
    },
  }
}

const taiDuLieuTuDatabase = async () => {
  loading.value = true
  try {
    const response = await axios.get(API_BASE, {
      params: {
        search: tuKhoATimKiem.value,
        danhMucId: danhMucDuocChon.value,
        thuongHieuId: thuongHieuDuocChon.value,
      },
    })
    danhSachSanPham.value = response.data
  } catch (error) {
    console.error('Lỗi nạp danh sách sản phẩm:', error)
    alert('Không thể kết nối đến máy chủ Backend!')
  } finally {
    loading.value = false // Luôn giải phóng trạng thái xoay vòng kể cả khi lỗi
  }
}

const taiDanhMucVaThuongHieu = async () => {
  try {
    const [resDM, resTH] = await Promise.all([
      axios.get(`${API_PUBLIC}/categories`),
      axios.get(`${API_PUBLIC}/brands`),
    ])
    listDanhMucOption.value = resDM.data
    listThuongHieuOption.value = resTH.data
  } catch (error) {
    console.error('Lỗi nạp danh mục/thương hiệu:', error)
  }
}

const hienThiFormThemMoi = () => {
  formFullProduct.value = createEmptyForm()
  dangSua.value = false
  hienThiForm.value = true
}

const hienThiFormChinhSua = (sp) => {
  dangSua.value = true
  formFullProduct.value.sanPham = {
    id: sp.id,
    ten: sp.ten,
    slug: sp.slug,
    moTa: sp.moTa,
    giaCoBan: sp.giaCoBan,
    anhChinh: sp.anhChinh,
    danhMucId: sp.danhMuc?.id || '',
    thuongHieuId: sp.thuongHieu?.id || '',
    trangThaiKinhDoanh: sp.trangThaiKinhDoanh,
  }
  hienThiForm.value = true
}

const dongForm = () => {
  hienThiForm.value = false
}

const luuSanPham = async () => {
  try {
    if (dangSua.value) {
      await axios.put(
        `${API_BASE}/${formFullProduct.value.sanPham.id}`,
        formFullProduct.value.sanPham,
      )
      alert('Cập nhật sản phẩm TrendFit thành công!')
    } else {
      await axios.post(API_BASE, formFullProduct.value.sanPham)
      alert('Thêm mới sản phẩm TrendFit thành công!')
    }
    hienThiForm.value = false
    taiDuLieuTuDatabase()
  } catch (error) {
    console.error('Lỗi khi lưu sản phẩm:', error)
    alert('Thao tác thất bại, vui lòng kiểm tra lại thông tin.')
  }
}

const xoaSanPham = async (id) => {
  if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này khỏi hệ thống TrendFit?')) {
    try {
      await axios.delete(`${API_BASE}/${id}`)
      alert('Đã xóa sản phẩm thành công.')
      taiDuLieuTuDatabase()
    } catch (error) {
      console.error('Lỗi khi xóa sản phẩm:', error)
      alert('Không thể xóa sản phẩm. Sản phẩm có thể đang tồn tại trong đơn hàng!')
    }
  }
}

const dinhDangGia = (gia) => {
  if (!gia) return '0 đ'
  return gia.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' })
}

onMounted(() => {
  taiDuLieuTuDatabase()
  taiDanhMucVaThuongHieu()
})
</script>
