<template>
  <div class="container-fluid py-4 position-relative">
    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
      <div>
        <h4 class="fw-bold mb-1">Quản lý sản phẩm</h4>
        <p class="text-secondary small mb-0">Thêm, sửa, xóa sản phẩm và biến thể</p>
      </div>
      <button
        v-if="!hienThiForm"
        type="button"
        class="btn btn-primary shadow-sm"
        @click="moFormThemMoi"
      >
        + Thêm sản phẩm mới
      </button>
    </div>

    <!-- BỐ CỤC CHIA ĐÔI MÀN HÌNH -->
    <div class="row g-4 align-items-start position-relative">
      <!-- BÊN TRÁI: BẢNG DANH SÁCH SẢN PHẨM & BỘ LỌC -->
      <div :class="hienThiForm ? 'col-lg-7' : 'col-12'" class="transition-all">
        <div class="card border-0 shadow-sm overflow-hidden">
          <div
            class="card-header bg-white border-0 border-bottom d-flex justify-content-between align-items-center flex-wrap gap-2 py-3"
          >
            <span class="fw-semibold">Danh sách sản phẩm ({{ danhSachHienThi.length }})</span>

            <!-- Ô BỘ LỌC & SẮP XẾP -->
            <div class="d-flex align-items-center gap-2">
              <label class="small text-secondary text-nowrap mb-0">Sắp xếp:</label>
              <select v-model="sortBy" class="form-select form-select-sm" style="width: 210px">
                <option value="default">Mặc định</option>
                <option value="stock-desc">Tồn kho: Nhiều đến ít</option>
                <option value="stock-asc">Tồn kho: Ít đến nhiều</option>
                <option value="id-asc">Tạo đầu tiên đến cuối</option>
                <option value="id-desc">Tạo cuối cùng đến đầu</option>
              </select>
            </div>
          </div>

          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light">
                <tr>
                  <th style="width: 50px" class="text-center">STT</th>
                  <th style="width: 70px">ID</th>
                  <th>Tên sản phẩm</th>
                  <th>Danh mục</th>
                  <th>Thương hiệu</th>
                  <th class="text-center">Tổng tồn kho</th>
                  <th class="text-center" style="width: 130px">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="danhSachHienThi.length === 0">
                  <td colspan="7" class="text-center text-muted py-4">Chưa có sản phẩm nào</td>
                </tr>
                <tr
                  v-for="(sp, index) in danhSachHienThi"
                  :key="sp.sanPham.id"
                  :class="{ 'table-active': hienThiForm && formData.sanPham.id === sp.sanPham.id }"
                >
                  <td class="text-center text-muted small">{{ index + 1 }}</td>
                  <td class="fw-semibold">#{{ sp.sanPham.id }}</td>
                  <td>
                    <div class="fw-semibold text-dark">{{ sp.sanPham.ten }}</div>
                    <small class="text-muted">{{ sp.sanPham.gioiTinh || 'Unisex' }}</small>
                  </td>
                  <td>{{ sp.sanPham.danhMuc?.ten || '—' }}</td>
                  <td>{{ sp.sanPham.thuongHieu?.ten || '—' }}</td>
                  <td class="text-center">
                    <span class="badge bg-light text-dark border px-2 py-1">
                      {{ tinhTongTonKho(sp.bienTheSanPhams) }}
                    </span>
                  </td>
                  <td class="text-center">
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-warning me-1"
                      @click="kichHoatSuaForm(sp.sanPham)"
                    >
                      Sửa
                    </button>
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-danger"
                      @click="deleteProduct(sp.sanPham.id)"
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

      <!-- BÊN PHẢI: FORM THÊM / SỬA GHIM CỐ ĐỊNH -->
      <div v-if="hienThiForm" class="col-lg-5">
        <div class="card border-0 shadow-sm always-fixed-form">
          <div class="card-body p-4">
            <div class="d-flex justify-content-between align-items-center mb-3 border-bottom pb-2">
              <h5 class="fw-bold m-0 text-primary">
                {{
                  dangSua ? 'Sửa sản phẩm #' + (formData.sanPham.id || 'N/A') : 'Thêm sản phẩm mới'
                }}
              </h5>
              <button type="button" class="btn-close" @click="hienThiForm = false"></button>
            </div>

            <!-- THÔNG TIN CHUNG -->
            <div class="row g-3">
              <div class="col-12">
                <label class="form-label small fw-semibold">Tên sản phẩm</label>
                <input
                  v-model="formData.sanPham.ten"
                  class="form-control form-control-sm"
                  required
                  placeholder="Nhập tên sản phẩm..."
                />
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">Danh mục</label>
                <select v-model="formData.sanPham.danhMuc" class="form-select form-select-sm">
                  <option :value="null">-- Chọn Danh mục --</option>
                  <option v-for="dm in metadata.danhMucs" :key="dm.id" :value="dm">
                    {{ dm.ten }}
                  </option>
                </select>
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">Thương hiệu</label>
                <select v-model="formData.sanPham.thuongHieu" class="form-select form-select-sm">
                  <option :value="null">-- Chọn Thương hiệu --</option>
                  <option v-for="th in metadata.thuongHieus" :key="th.id" :value="th">
                    {{ th.ten }}
                  </option>
                </select>
              </div>
              <div class="col-md-4">
                <label class="form-label small fw-semibold">Giới tính</label>
                <select v-model="formData.sanPham.gioiTinh" class="form-select form-select-sm">
                  <option value="Nam">Nam</option>
                  <option value="Nữ">Nữ</option>
                  <option value="Unisex">Unisex</option>
                </select>
              </div>
              <div class="col-md-4">
                <label class="form-label small fw-semibold">Chất liệu</label>
                <input v-model="formData.sanPham.chatLieu" class="form-control form-control-sm" />
              </div>
              <div class="col-md-4">
                <label class="form-label small fw-semibold">Xuất xứ</label>
                <input v-model="formData.sanPham.xuatXu" class="form-control form-control-sm" />
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">Năm ra mắt</label>
                <input
                  v-model.number="formData.sanPham.namRaMat"
                  type="number"
                  class="form-control form-control-sm"
                />
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">Trạng thái chung</label>
                <button
                  type="button"
                  class="btn btn-sm w-100"
                  :class="formData.sanPham.dangBan !== false ? 'btn-success' : 'btn-secondary'"
                  @click="
                    formData.sanPham.dangBan = formData.sanPham.dangBan === false ? true : false
                  "
                >
                  {{ formData.sanPham.dangBan !== false ? 'Đang bán' : 'Ngừng bán' }}
                </button>
              </div>
              <div class="col-12">
                <label class="form-label small fw-semibold">Mô tả chi tiết</label>
                <textarea
                  v-model="formData.sanPham.moTa"
                  class="form-control form-control-sm"
                  rows="2"
                ></textarea>
              </div>
            </div>

            <!-- BIẾN THỂ -->
            <div class="mt-4 pt-3 border-top">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <h6 class="fw-bold m-0 small text-uppercase text-secondary">Biến thể sản phẩm</h6>
                <button type="button" class="btn btn-outline-dark btn-sm" @click="themBienTheMoi">
                  + Thêm biến thể
                </button>
              </div>

              <div
                v-if="formData.bienTheSanPhams.length === 0"
                class="text-muted small fst-italic text-center py-2 bg-light rounded"
              >
                Chưa có biến thể nào được thêm.
              </div>

              <div
                v-for="(v, idx) in formData.bienTheSanPhams"
                :key="idx"
                class="p-2 border rounded mb-2 bg-light bg-opacity-50"
              >
                <div class="row g-2 align-items-center">
                  <div class="col-md-5">
                    <select v-model="v.kichCo" class="form-select form-select-sm">
                      <option :value="null">-- Size --</option>
                      <option v-for="kc in metadata.kichCos" :key="kc.id" :value="kc">
                        {{ kc.tenKichCo }}
                      </option>
                    </select>
                  </div>
                  <div class="col-md-5">
                    <select v-model="v.mauSac" class="form-select form-select-sm">
                      <option :value="null">-- Màu --</option>
                      <option v-for="ms in metadata.mauSacs" :key="ms.id" :value="ms">
                        {{ ms.tenMau }}
                      </option>
                    </select>
                  </div>
                  <div class="col-md-2 text-end">
                    <button
                      type="button"
                      class="btn btn-outline-danger btn-sm w-100"
                      @click="formData.bienTheSanPhams.splice(idx, 1)"
                    >
                      X
                    </button>
                  </div>
                  <div class="col-md-4">
                    <input
                      v-model="v.maSku"
                      class="form-control form-control-sm"
                      placeholder="Mã SKU"
                    />
                  </div>
                  <div class="col-md-4">
                    <input
                      v-model.number="v.soLuongTon"
                      type="number"
                      class="form-control form-control-sm text-center"
                      placeholder="Tồn kho"
                    />
                  </div>
                  <div class="col-md-4">
                    <input
                      v-model.number="v.giaNhap"
                      type="number"
                      class="form-control form-control-sm"
                      placeholder="Giá nhập"
                    />
                  </div>
                  <div class="col-md-6">
                    <input
                      v-model.number="v.gia"
                      type="number"
                      class="form-control form-control-sm"
                      placeholder="Giá bán"
                    />
                  </div>
                  <div class="col-md-6">
                    <button
                      type="button"
                      class="btn btn-sm w-100"
                      :class="v.dangBan ? 'btn-success' : 'btn-secondary'"
                      @click="v.dangBan = !v.dangBan"
                    >
                      {{ v.dangBan ? 'Đang bán' : 'Ẩn' }}
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- ẢNH SẢN PHẨM & Ô TÍCH CHỌN ẢNH CHÍNH -->
            <div class="mt-4 pt-3 border-top">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <h6 class="fw-bold m-0 small text-uppercase text-secondary">
                  Ảnh sản phẩm (Tích chọn 1 ảnh chính)
                </h6>
                <button type="button" class="btn btn-outline-dark btn-sm" @click="themAnhMoi">
                  + Thêm dòng ảnh
                </button>
              </div>

              <div
                v-if="formData.anhSanPhams.length === 0"
                class="text-muted small fst-italic text-center py-2 bg-light rounded"
              >
                Chưa có ảnh nào được thêm.
              </div>

              <div
                v-for="(img, idx) in formData.anhSanPhams"
                :key="idx"
                class="input-group input-group-sm mb-2 align-items-center gap-2"
              >
                <input
                  v-model="img.urlAnh"
                  class="form-control"
                  placeholder="Dán link ảnh hoặc upload..."
                />
                <label :for="'upload-' + idx" class="btn btn-outline-primary mb-0 text-nowrap"
                  >File</label
                >
                <input
                  :id="'upload-' + idx"
                  type="file"
                  accept="image/*"
                  class="d-none"
                  @change="handleImageUpload($event, idx)"
                />

                <!-- Ô tích chọn ảnh chính rõ ràng -->
                <div class="form-check m-0 d-flex align-items-center">
                  <input
                    type="checkbox"
                    :checked="img.laAnhChinh"
                    @change="chonAnhChinh(idx)"
                    class="form-check-input custom-main-checkbox"
                    :id="'main-img-' + idx"
                  />
                  <label
                    class="form-check-label small ms-2 fw-semibold text-nowrap"
                    :for="'main-img-' + idx"
                    style="cursor: pointer"
                  >
                    Ảnh chính
                  </label>
                </div>

                <button
                  type="button"
                  class="btn btn-outline-danger btn-sm"
                  @click="formData.anhSanPhams.splice(idx, 1)"
                >
                  X
                </button>
              </div>
            </div>

            <!-- NÚT HÀNH ĐỘNG -->
            <div class="d-flex gap-2 mt-4 pt-3 border-top">
              <button
                type="button"
                class="btn btn-primary btn-sm flex-grow-1"
                @click="saveFullProduct"
              >
                Lưu thay đổi
              </button>
              <button
                type="button"
                class="btn btn-outline-secondary btn-sm"
                @click="hienThiForm = false"
              >
                Đóng
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

  <!-- MODAL CẮT ẢNH SẢN PHẨM (KHẮC PHỤC HOÀN TOÀN LỖI MÀN HÌNH ĐEN) -->
  <div
    v-if="showProductCropModal"
    class="modal show d-block"
    tabindex="-1"
    style="background: rgba(0, 0, 0, 0.75)"
  >
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content p-4 text-center">
        <h5 class="fw-bold mb-3">Điều chỉnh khung hiển thị ảnh sản phẩm</h5>
        <p class="text-muted small mb-3">
          Kéo thả và thu phóng để chọn phần đẹp nhất hiển thị ngoài trang chủ.
        </p>

        <div
          class="crop-container position-relative mx-auto mb-3 overflow-hidden border rounded bg-dark shadow-sm"
          style="width: 300px; height: 350px; cursor: grab; user-select: none"
        >
          <canvas
            ref="productCropCanvas"
            width="300"
            height="350"
            @mousedown="startProductDrag"
            @mousemove="onProductDrag"
            @mouseup="stopProductDrag"
            @mouseleave="stopProductDrag"
            style="display: block"
          ></canvas>
        </div>

        <!-- Thanh trượt Zoom -->
        <div class="d-flex align-items-center gap-3 px-4 mb-4">
          <span class="fs-5 text-muted">⊖</span>
          <input
            type="range"
            class="form-range"
            min="1"
            max="3"
            step="0.05"
            v-model.number="productZoom"
            @input="drawProductCanvas"
          />
          <span class="fs-5 text-muted">⊕</span>
        </div>

        <div class="d-flex justify-content-end gap-2">
          <button @click="showProductCropModal = false" class="btn btn-secondary btn-sm px-3">
            Hủy
          </button>
          <button
            @click="confirmCropProductImage"
            class="btn btn-primary btn-sm px-3"
            :disabled="uploadingImg"
          >
            {{ uploadingImg ? 'Đang tải lên...' : 'Xác nhận & Lưu' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
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
const sortBy = ref('default')
const showScrollTopBtn = ref(false)

const formData = ref({
  sanPham: {},
  bienTheSanPhams: [],
  anhSanPhams: [],
})

const tinhTongTonKho = (bienThes) => {
  if (!bienThes || !Array.isArray(bienThes)) return 0
  return bienThes.reduce((sum, v) => sum + (Number(v.soLuongTon) || 0), 0)
}

const danhSachHienThi = computed(() => {
  let list = [...danhSachSanPham.value]

  if (sortBy.value === 'stock-desc') {
    list.sort((a, b) => tinhTongTonKho(b.bienTheSanPhams) - tinhTongTonKho(a.bienTheSanPhams))
  } else if (sortBy.value === 'stock-asc') {
    list.sort((a, b) => tinhTongTonKho(a.bienTheSanPhams) - tinhTongTonKho(b.bienTheSanPhams))
  } else if (sortBy.value === 'id-asc') {
    list.sort((a, b) => (a.sanPham.id || 0) - (b.sanPham.id || 0))
  } else if (sortBy.value === 'id-desc') {
    list.sort((a, b) => (b.sanPham.id || 0) - (a.sanPham.id || 0))
  }

  return list
})

const handleScroll = () => {
  showScrollTopBtn.value = window.scrollY > 300
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

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

// Logic chọn ảnh chính độc lập (Tích chọn ảnh này thì các ảnh khác tự động bỏ tích)
const chonAnhChinh = (index) => {
  formData.value.anhSanPhams.forEach((img, idx) => {
    img.laAnhChinh = idx === index
  })
}

// Biến trạng thái cắt ảnh sản phẩm
const showProductCropModal = ref(false)
const productCropCanvas = ref(null)
let activeImageIndex = null
let rawProductImageObj = null
const productZoom = ref(1)
const productOffsetX = ref(0)
const productOffsetY = ref(0)
let baseProdW = 300
let baseProdH = 350
let isProdDragging = false
let startProdX = 0
let startProdY = 0
const uploadingImg = ref(false)

const handleImageUpload = (event, idx) => {
  const file = event.target.files[0]
  if (!file) return
  activeImageIndex = idx

  const reader = new FileReader()
  reader.onload = (e) => {
    rawProductImageObj = new Image()
    rawProductImageObj.onload = async () => {
      productZoom.value = 1
      const canvasW = 300
      const canvasH = 350
      const scale = Math.max(
        canvasW / rawProductImageObj.width,
        canvasH / rawProductImageObj.height,
      )
      baseProdW = rawProductImageObj.width * scale
      baseProdH = rawProductImageObj.height * scale
      productOffsetX.value = (canvasW - baseProdW) / 2
      productOffsetY.value = (canvasH - baseProdH) / 2

      // Mở modal và chờ Vue render DOM hoàn tất
      showProductCropModal.value = true
      await nextTick()

      // Đảm bảo thẻ canvas đã sẵn sàng trong DOM trước khi vẽ
      setTimeout(() => {
        drawProductCanvas()
      }, 100)
    }
    rawProductImageObj.src = e.target.result
  }
  reader.readAsDataURL(file)
  event.target.value = ''
}

const drawProductCanvas = () => {
  const canvas = productCropCanvas.value
  if (!canvas || !rawProductImageObj) return
  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  const currentW = baseProdW * productZoom.value
  const currentH = baseProdH * productZoom.value

  const minX = canvas.width - currentW
  const maxX = 0
  const minY = canvas.height - currentH
  const maxY = 0

  if (currentW <= canvas.width) {
    productOffsetX.value = (canvas.width - currentW) / 2
  } else {
    if (productOffsetX.value < minX) productOffsetX.value = minX
    if (productOffsetX.value > maxX) productOffsetX.value = maxX
  }

  if (currentH <= canvas.height) {
    productOffsetY.value = (canvas.height - currentH) / 2
  } else {
    if (productOffsetY.value < minY) productOffsetY.value = minY
    if (productOffsetY.value > maxY) productOffsetY.value = maxY
  }

  ctx.save()
  ctx.drawImage(rawProductImageObj, productOffsetX.value, productOffsetY.value, currentW, currentH)
  ctx.restore()
}

const startProductDrag = (e) => {
  isProdDragging = true
  startProdX = e.clientX - productOffsetX.value
  startProdY = e.clientY - productOffsetY.value
}

const onProductDrag = (e) => {
  if (!isProdDragging) return
  productOffsetX.value = e.clientX - startProdX
  productOffsetY.value = e.clientY - startProdY
  drawProductCanvas()
}

const stopProductDrag = () => {
  isProdDragging = false
}

const confirmCropProductImage = () => {
  const canvas = productCropCanvas.value
  if (!canvas) return

  canvas.toBlob(
    async (blob) => {
      if (!blob) return
      const uploadForm = new FormData()
      uploadForm.append('file', blob)
      uploadForm.append('upload_preset', UPLOAD_PRESET)

      uploadingImg.value = true
      try {
        const res = await axios.post(
          `https://api.cloudinary.com/v1_1/${CLOUD_NAME}/image/upload`,
          uploadForm,
        )
        if (activeImageIndex !== null) {
          formData.value.anhSanPhams[activeImageIndex].urlAnh = res.data.secure_url
        }
        showProductCropModal.value = false
        alert('✅ Cắt và tải ảnh sản phẩm lên thành công!')
      } catch (err) {
        alert('❌ Upload ảnh thất bại!')
        console.error(err)
      } finally {
        uploadingImg.value = false
      }
    },
    'image/jpeg',
    0.9,
  )
}

const themBienTheMoi = () => {
  formData.value.bienTheSanPhams.push({
    kichCo: null,
    mauSac: null,
    maSku: '',
    soLuongTon: '',
    giaNhap: '',
    gia: '',
    dangBan: true,
  })
}

const themAnhMoi = () => {
  const isFirst = formData.value.anhSanPhams.length === 0
  formData.value.anhSanPhams.push({ urlAnh: '', laAnhChinh: isFirst })
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
        dangBan: sp.dangBan !== false,
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

onMounted(() => {
  loadData()
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

/* Tùy chỉnh ô tích chọn (Checkbox) dạng bo góc xanh hiện đại */
.form-check-input.custom-main-checkbox {
  width: 1.35rem;
  height: 1.35rem;
  border-radius: 6px;
  border: 2px solid #cbd5e1;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  background-color: #fff;
}

.form-check-input.custom-main-checkbox:checked {
  background-color: #2563eb;
  border-color: #2563eb;
}

.form-check-input.custom-main-checkbox:focus {
  box-shadow: 0 0 0 0.25rem rgba(37, 99, 235, 0.25);
  border-color: #2563eb;
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
