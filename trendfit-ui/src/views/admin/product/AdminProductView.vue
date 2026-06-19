<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import API_BASE from '../../../config/api.js'

const danhSachSanPham = ref([])
const loading = ref(true)
const hienThiForm = ref(false)
const dangSua = ref(false)
const tuKhoaTimKiem = ref('')

const listDanhMucOption = ref([])
const listThuongHieuOption = ref([])

const formFullProduct = ref(createEmptyForm())
const tamBienThe = ref({ kichCoSize: 'M', mauSac: 'Đen', gia: 350000, soLuongTon: 100, maSku: '' })
const tamAnhUrl = ref('')

function createEmptyForm() {
  return {
    sanPham: {
      id: null,
      ten: '',
      slug: '',
      gioiTinh: 'Nam',
      chatLieu: '',
      xuatXu: 'Việt Nam',
      namRaMat: 2026,
      moTa: '',
      thanhPhanChatLieu: '',
      dangBan: true,
      danhMuc: { id: null },
      thuongHieu: { id: null },
    },
    bienThes: [],
    anhs: [],
  }
}

const taiDanhMucVaThuongHieu = async () => {
  try {
    const [dmRes, thRes] = await Promise.all([
      axios.get(`${API_BASE}/api/admin/products/categories`),
      axios.get(`${API_BASE}/api/admin/products/brands`),
    ])
    listDanhMucOption.value = dmRes.data
    listThuongHieuOption.value = thRes.data
  } catch (error) {
    console.error(error)
    alert('Không thể tải danh mục hoặc thương hiệu từ hệ thống!')
  }
}

const taiDuLieuTuDatabase = async () => {
  try {
    loading.value = true
    const response = await axios.get(`${API_BASE}/api/admin/products`, {
      params: { search: tuKhoaTimKiem.value || undefined },
    })
    danhSachSanPham.value = response.data
  } catch (error) {
    console.error(error)
    alert('Không thể kết nối tới cơ sở dữ liệu hệ thống!')
  } finally {
    loading.value = false
  }
}

const hanhdongLuuCSDL = async () => {
  try {
    if (formFullProduct.value.bienThes.length === 0 || formFullProduct.value.anhs.length === 0) {
      alert('Yêu cầu bắt buộc: Phải có ít nhất 1 biến thể kho và 1 hình ảnh!')
      return
    }

    let response
    if (dangSua.value && formFullProduct.value.sanPham.id) {
      response = await axios.put(
        `${API_BASE}/api/admin/products/${formFullProduct.value.sanPham.id}/full`,
        formFullProduct.value,
      )
    } else {
      response = await axios.post(`${API_BASE}/api/admin/products/full`, formFullProduct.value)
    }

    alert(response.data.message || 'Lưu dữ liệu thành công!')
    hienThiForm.value = false
    taiDuLieuTuDatabase()
  } catch (error) {
    console.error(error)
    alert(
      error.response?.data?.message ||
        'Thao tác thất bại! Vui lòng kiểm tra lại kết nối hoặc dữ liệu trùng lặp.',
    )
  }
}

const hanhdongXoaCSDL = async (id) => {
  if (
    confirm(
      'Cảnh báo hệ thống: Hành động này sẽ xóa vĩnh viễn sản phẩm khỏi MySQL. Bạn chắc chắn muốn xóa?',
    )
  ) {
    try {
      const response = await axios.delete(`${API_BASE}/api/admin/products/${id}`)
      alert(response.data.message || 'Đã xóa bản ghi sản phẩm khỏi hệ thống thành công!')
      taiDuLieuTuDatabase()
    } catch (error) {
      alert(error.response?.data?.message || 'Không thể xóa sản phẩm này!')
    }
  }
}

const kichHoatSuaForm = async (item) => {
  try {
    const response = await axios.get(`${API_BASE}/api/admin/products/${item.id}/full`)
    const data = response.data

    dangSua.value = true
    hienThiForm.value = true
    formFullProduct.value = {
      sanPham: {
        ...data.sanPham,
        danhMuc: data.sanPham.danhMuc ? { id: data.sanPham.danhMuc.id } : { id: listDanhMucOption.value[0]?.id },
        thuongHieu: data.sanPham.thuongHieu
          ? { id: data.sanPham.thuongHieu.id }
          : { id: listThuongHieuOption.value[0]?.id },
      },
      bienThes: (data.bienThes || []).map(({ id, sanPham, ...rest }) => ({ ...rest })),
      anhs: (data.anhs || []).map(({ id, sanPham, ...rest }) => ({ ...rest })),
    }
  } catch (error) {
    console.error(error)
    alert('Không thể tải chi tiết sản phẩm để chỉnh sửa!')
  }
}

const moFormThemMoi = () => {
  dangSua.value = false
  hienThiForm.value = true
  formFullProduct.value = createEmptyForm()
  formFullProduct.value.sanPham.danhMuc.id = listDanhMucOption.value[0]?.id ?? null
  formFullProduct.value.sanPham.thuongHieu.id = listThuongHieuOption.value[0]?.id ?? null
  formFullProduct.value.bienThes = []
  formFullProduct.value.anhs = []
}

const bockThemBienThe = () => {
  if (!tamBienThe.value.kichCoSize || !tamBienThe.value.mauSac || tamBienThe.value.gia <= 0) {
    alert('Vui lòng nhập đầy đủ thông tin biến thể!')
    return
  }
  tamBienThe.value.maSku =
    `${formFullProduct.value.sanPham.slug || 'sku'}-${tamBienThe.value.mauSac}-${tamBienThe.value.kichCoSize}`.toUpperCase()
  formFullProduct.value.bienThes.push({ ...tamBienThe.value })
}

const xoaBienThe = (index) => {
  formFullProduct.value.bienThes.splice(index, 1)
}

const bockThemAnh = () => {
  if (tamAnhUrl.value.trim() !== '') {
    formFullProduct.value.anhs.push({
      urlAnh: tamAnhUrl.value,
      altText: formFullProduct.value.sanPham.ten || 'Product Image',
      laAnhChinh: formFullProduct.value.anhs.length === 0,
      thuTu: formFullProduct.value.anhs.length + 1,
    })
    tamAnhUrl.value = ''
  }
}

const xoaAnh = (index) => {
  formFullProduct.value.anhs.splice(index, 1)
  formFullProduct.value.anhs.forEach((anh, i) => {
    anh.thuTu = i + 1
    anh.laAnhChinh = i === 0
  })
}

onMounted(async () => {
  await taiDanhMucVaThuongHieu()
  await taiDuLieuTuDatabase()
})
</script>

<template>
  <div class="container-fluid py-4 bg-light min-vh-100 text-start font-admin">
    <div class="card rounded-0 border-0 shadow-sm p-4 mb-4 bg-white">
      <div class="row align-items-center g-3">
        <div class="col-12 col-md-4">
          <h4 class="fw-bold m-0 text-dark">TRUNG TÂM QUẢN TRỊ SẢN PHẨM</h4>
          <small class="text-muted">Nghiệp vụ CRUD & Tìm kiếm Real-time chuẩn CSDL</small>
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
              <i class="bi bi-search"></i> Tìm ngay
            </button>
          </div>
        </div>
        <div class="col-12 col-md-3 text-md-end">
          <button
            @click="moFormThemMoi"
            class="btn btn-primary rounded-0 fw-bold w-100 py-2 text-uppercase"
            :disabled="listDanhMucOption.length === 0 || listThuongHieuOption.length === 0"
          >
            + THÊM SẢN PHẨM MỚI
          </button>
        </div>
      </div>
    </div>

    <div v-if="hienThiForm" class="card rounded-0 shadow-sm border-0 mb-4 bg-white">
      <div class="card-header bg-dark text-white rounded-0 fw-bold py-3 text-uppercase">
        {{
          dangSua
            ? 'Chế độ: Cập nhật sản phẩm #' + formFullProduct.sanPham.id
            : 'Chế độ: Thêm mới tổ hợp sản phẩm'
        }}
      </div>
      <div class="card-body p-4">
        <form @submit.prevent="hanhdongLuuCSDL" class="row g-3">
          <h5 class="fw-bold text-primary border-start border-4 border-primary ps-2 mb-2">
            1. THÔNG TIN GỐC SẢN PHẨM
          </h5>
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
              required
            />
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-bold">Phân loại Danh Mục</label>
            <select
              v-model="formFullProduct.sanPham.danhMuc.id"
              class="form-select rounded-0 border-dark"
            >
              <option v-for="dm in listDanhMucOption" :key="dm.id" :value="dm.id">
                {{ dm.ten }}
              </option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-bold">Hãng Thương Hiệu</label>
            <select
              v-model="formFullProduct.sanPham.thuongHieu.id"
              class="form-select rounded-0 border-dark"
            >
              <option v-for="th in listThuongHieuOption" :key="th.id" :value="th.id">
                {{ th.ten }}
              </option>
            </select>
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
            <label class="form-label small fw-bold">Giới tính</label>
            <select
              v-model="formFullProduct.sanPham.gioiTinh"
              class="form-select rounded-0 border-dark"
            >
              <option value="Nam">Nam</option>
              <option value="Nữ">Nữ</option>
              <option value="Unisex">Unisex</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="form-label small fw-bold">Trạng thái kinh doanh</label>
            <select
              v-model="formFullProduct.sanPham.dangBan"
              class="form-select rounded-0 border-dark"
            >
              <option :value="true">Mở bán</option>
              <option :value="false">Ẩn kho</option>
            </select>
          </div>

          <h5 class="fw-bold text-success border-start border-4 border-success ps-2 mt-4 mb-2">
            2. HÌNH ẢNH ALBUM
          </h5>
          <div class="col-12 d-flex gap-2">
            <input
              type="text"
              v-model="tamAnhUrl"
              class="form-control rounded-0 border-dark"
              placeholder="Dán link ảnh web..."
            />
            <button type="button" @click="bockThemAnh" class="btn btn-success rounded-0 text-nowrap">
              + CHÈN
            </button>
          </div>
          <div
            class="col-12 d-flex gap-2 flex-wrap bg-light p-2"
            v-if="formFullProduct.anhs.length > 0"
          >
            <div v-for="(anh, i) in formFullProduct.anhs" :key="i" class="position-relative">
              <img
                :src="anh.urlAnh"
                style="height: 60px; width: 60px; object-fit: cover"
                class="border"
              />
              <button
                type="button"
                @click="xoaAnh(i)"
                class="btn btn-danger btn-sm position-absolute top-0 end-0 p-0 px-1"
                style="font-size: 10px; line-height: 1"
              >
                x
              </button>
            </div>
          </div>

          <h5 class="fw-bold text-warning border-start border-4 border-warning ps-2 mt-4 mb-2">
            3. CẤU HÌNH BIẾN THỂ KHO HÀNG
          </h5>
          <div class="col-12 bg-light p-3 border row g-2 m-0 text-dark">
            <div class="col-md-2">
              <input
                type="text"
                v-model="tamBienThe.kichCoSize"
                class="form-control form-control-sm rounded-0"
                placeholder="Size (M/L)"
              />
            </div>
            <div class="col-md-2">
              <input
                type="text"
                v-model="tamBienThe.mauSac"
                class="form-control form-control-sm rounded-0"
                placeholder="Màu sắc"
              />
            </div>
            <div class="col-md-4">
              <input
                type="number"
                v-model.number="tamBienThe.gia"
                class="form-control form-control-sm rounded-0"
                placeholder="Giá bán"
              />
            </div>
            <div class="col-md-2">
              <input
                type="number"
                v-model.number="tamBienThe.soLuongTon"
                class="form-control form-control-sm rounded-0"
                placeholder="Kho"
              />
            </div>
            <div class="col-md-2">
              <button
                type="button"
                @click="bockThemBienThe"
                class="btn btn-warning btn-sm rounded-0 w-100 fw-bold text-dark"
              >
                + GHI
              </button>
            </div>
          </div>
          <div class="col-12" v-if="formFullProduct.bienThes.length > 0">
            <span
              class="badge bg-secondary me-2 p-2 mb-2 rounded-0 d-inline-flex align-items-center gap-2"
              v-for="(bt, idx) in formFullProduct.bienThes"
              :key="idx"
            >
              Size: {{ bt.kichCoSize }} | Màu: {{ bt.mauSac }} | Giá:
              {{ Number(bt.gia).toLocaleString() }} đ | Tồn: {{ bt.soLuongTon }} cái
              <button type="button" @click="xoaBienThe(idx)" class="btn btn-light btn-sm py-0 px-1">
                x
              </button>
            </span>
          </div>

          <div class="col-12 d-flex gap-2 justify-content-end mt-4 pt-3 border-top">
            <button
              type="button"
              @click="hienThiForm = false"
              class="btn btn-secondary rounded-0 px-4"
            >
              HỦY BỎ
            </button>
            <button type="submit" class="btn btn-primary rounded-0 px-5 fw-bold text-uppercase">
              XÁC NHẬN LƯU CSDL
            </button>
          </div>
        </form>
      </div>
    </div>

    <div class="card rounded-0 border-0 shadow-sm bg-white">
      <div class="card-body p-0">
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-dark"></div>
          <p class="mt-2 text-muted small">Đang bốc danh sách lọc CSDL...</p>
        </div>
        <div v-else class="table-responsive">
          <table class="table table-hover align-middle text-center m-0">
            <thead class="table-dark">
              <tr>
                <th>Mã ID</th>
                <th class="text-start ps-4">Tên Sản Phẩm</th>
                <th>Danh Mục</th>
                <th>Thương Hiệu</th>
                <th>Chất Liệu</th>
                <th>Kinh Doanh</th>
                <th>Thao Tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in danhSachSanPham" :key="item.id">
                <td class="fw-bold">#{{ item.id }}</td>
                <td class="text-start ps-4 fw-bold text-dark">{{ item.ten }}</td>
                <td>
                  <span class="badge bg-secondary rounded-0">{{
                    item.danhMuc?.ten || 'ID: ' + item.danhMuc?.id
                  }}</span>
                </td>
                <td>
                  <span class="badge bg-info text-dark rounded-0">{{
                    item.thuongHieu?.ten || 'ID: ' + item.thuongHieu?.id
                  }}</span>
                </td>
                <td>{{ item.chatLieu || '-' }}</td>
                <td>
                  <span
                    :class="item.dangBan ? 'badge bg-success' : 'badge bg-danger'"
                    class="rounded-0 px-2 py-1"
                    >{{ item.dangBan ? 'MỞ BÁN' : 'ẨN KHO' }}</span
                  >
                </td>
                <td>
                  <div class="d-flex gap-2 justify-content-center">
                    <button
                      @click="kichHoatSuaForm(item)"
                      class="btn btn-sm btn-outline-warning rounded-0 px-3"
                    >
                      Sửa
                    </button>
                    <button
                      @click="hanhdongXoaCSDL(item.id)"
                      class="btn btn-sm btn-outline-danger rounded-0 px-3"
                    >
                      Xóa
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="danhSachSanPham.length === 0">
                <td colspan="7" class="text-muted py-5 fw-bold bg-light">
                  Không tìm thấy sản phẩm nào khớp với từ khóa của bạn!
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.font-admin {
  font-family: 'Segoe UI', system-ui, sans-serif;
}
th {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
td {
  font-size: 14px;
}
</style>
