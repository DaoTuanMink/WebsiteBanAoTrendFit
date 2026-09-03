<template>
  <LayoutHeader />
  <div class="container py-5">
    <h2 class="fw-bold mb-4">HỒ SƠ CÁ NHÂN & SỔ ĐỊA CHỈ</h2>

    <div class="row g-4">
      <!-- Cột 1: Thông tin tài khoản -->
      <div class="col-md-5">
        <div class="card p-4 shadow-sm border-0 bg-white">
          <h5 class="fw-bold mb-3">Thông tin tài khoản</h5>

          <!-- Avatar và Nút đổi ảnh -->
          <div class="text-center mb-3 position-relative">
            <img
              :src="
                profile.anhDaiDien ||
                'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100'
              "
              class="rounded-circle border mb-2 shadow-sm"
              style="width: 90px; height: 90px; object-fit: cover"
            />
            <div>
              <label class="btn btn-sm btn-outline-dark mt-1" style="cursor: pointer">
                📁 Chọn ảnh từ máy
                <input type="file" @change="onFileSelected" accept="image/*" class="d-none" />
              </label>
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label small fw-semibold">Họ và tên</label>
            <input v-model="profile.hoTen" class="form-control" />
          </div>
          <div class="mb-3">
            <label class="form-label small fw-semibold">Email (Không đổi)</label>
            <input v-model="profile.email" class="form-control bg-light" disabled />
          </div>
          <div class="mb-3">
            <label class="form-label small fw-semibold">Số điện thoại</label>
            <input v-model="profile.soDienThoai" class="form-control" />
          </div>
          <div class="mb-3">
            <label class="form-label small fw-semibold">Link Ảnh đại diện (URL)</label>
            <input
              v-model="profile.anhDaiDien"
              class="form-control"
              placeholder="Hoặc dán link ảnh vào đây"
            />
          </div>
          <button @click="updateProfile" class="btn btn-dark w-100">Cập nhật thông tin</button>
        </div>
      </div>

      <!-- Cột 2: Sổ địa chỉ nhận hàng -->
      <div class="col-md-7">
        <div class="card p-4 shadow-sm border-0 bg-white">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="fw-bold m-0">Sổ địa chỉ giao hàng</h5>
            <button @click="moFormThemDC" class="btn btn-sm btn-outline-primary">
              + Thêm địa chỉ mới
            </button>
          </div>

          <div v-if="profile.danhSachDiaChi && profile.danhSachDiaChi.length > 0">
            <div
              v-for="dc in profile.danhSachDiaChi"
              :key="dc.id"
              class="border rounded p-3 mb-3 position-relative"
            >
              <div class="d-flex justify-content-between align-items-start">
                <div>
                  <h6 class="fw-bold mb-1">
                    {{ dc.tenNguoiNhan }}
                    <span class="text-muted fw-normal">({{ dc.soDienThoai }})</span>
                    <span v-if="dc.laMacDinh" class="badge bg-success ms-2">Mặc định</span>
                  </h6>
                  <p class="small text-secondary mb-1">
                    Địa chỉ: {{ dc.chiTiet || dc.duong }}, {{ dc.xaPhuong }}, {{ dc.tinhThanh }}
                  </p>
                </div>
                <div>
                  <button @click="suaDiaChi(dc)" class="btn btn-sm btn-light me-1">Sửa</button>
                  <button @click="xoaDiaChi(dc.id)" class="btn btn-sm btn-outline-danger">
                    Xóa
                  </button>
                </div>
              </div>
            </div>
          </div>
          <p v-else class="text-muted small">Bạn chưa có địa chỉ giao hàng nào.</p>
        </div>
      </div>
    </div>

    <!-- ===================== MODAL CẮT ẢNH AVATAR ===================== -->
    <div
      v-if="showCropModal"
      class="modal show d-block"
      tabindex="-1"
      style="background: rgba(0, 0, 0, 0.75)"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content p-4 text-center">
          <h5 class="fw-bold mb-3">Điều chỉnh ảnh đại diện</h5>

          <div
            class="crop-container position-relative mx-auto mb-3 overflow-hidden border rounded bg-dark"
            style="width: 300px; height: 300px; cursor: grab; user-select: none"
          >
            <canvas
              ref="cropCanvas"
              width="300"
              height="300"
              @mousedown="startDrag"
              @mousemove="onDrag"
              @mouseup="stopDrag"
              @mouseleave="stopDrag"
              @touchstart="startDrag"
              @touchmove="onDrag"
              @touchend="stopDrag"
              style="display: block"
            ></canvas>
            <!-- Lớp phủ khung tròn cắt ảnh -->
            <div
              class="crop-overlay position-absolute top-0 start-0 w-100 h-100"
              style="
                box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.6);
                border-radius: 50%;
                width: 220px;
                height: 220px;
                margin: 40px auto;
                pointer-events: none;
              "
            ></div>
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
              v-model.number="zoom"
              @input="drawCanvas"
            />
            <span class="fs-5 text-muted">⊕</span>
          </div>

          <div class="d-flex justify-content-end gap-2">
            <button @click="showCropModal = false" class="btn btn-secondary btn-sm px-3">
              Hủy
            </button>
            <button
              @click="confirmCropAndUpload"
              class="btn btn-dark btn-sm px-3"
              :disabled="uploading"
            >
              {{ uploading ? 'Đang tải lên...' : 'Áp dụng & Lưu' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Thêm/Sửa Địa Chỉ -->
    <div
      v-if="showModal"
      class="modal show d-block"
      tabindex="-1"
      style="background: rgba(0, 0, 0, 0.5)"
    >
      <div class="modal-dialog">
        <div class="modal-content p-4">
          <h5 class="fw-bold mb-3">{{ editingAddressId ? 'Sửa địa chỉ' : 'Thêm địa chỉ mới' }}</h5>

          <div class="mb-2">
            <label class="form-label small">Tên người nhận</label>
            <input v-model="addressForm.tenNguoiNhan" class="form-control" />
          </div>

          <div class="mb-2">
            <label class="form-label small">Số điện thoại nhận hàng</label>
            <input v-model="addressForm.soDienThoai" class="form-control" />
          </div>

          <!-- TÌM KIẾM & CHỌN TỈNH / THÀNH PHỐ -->
          <div class="mb-2 combobox-wrap" ref="tinhThanhWrapRef">
            <label class="form-label small d-block mb-1">Tỉnh / Thành phố</label>
            <div class="combobox">
              <input
                v-model="tinhThanhSearch"
                @focus="openTinhThanhDropdown = true"
                @blur="onTinhThanhBlur"
                class="form-control form-control-sm"
                autocomplete="off"
                placeholder="Gõ để tìm tỉnh/thành..."
              />
              <div v-if="openTinhThanhDropdown && tinhThanhGoiY.length > 0" class="combobox-list">
                <div
                  v-for="tinh in tinhThanhGoiY"
                  :key="tinh.code"
                  class="combobox-item"
                  :class="{ active: addressForm.tinhThanh === tinh.name }"
                  @mousedown.prevent="chonTinhThanh(tinh)"
                >
                  {{ tinh.name }}
                </div>
              </div>
            </div>
          </div>

          <!-- NHẬP TỰ DO XÃ / PHƯỜNG, QUẬN / HUYỆN -->
          <div class="mb-2">
            <label class="form-label small d-block mb-1">Xã/Phường, Quận/Huyện</label>
            <input
              v-model="addressForm.xaPhuong"
              class="form-control form-control-sm"
              placeholder="VD: Phường Bến Nghé, Quận 1"
            />
          </div>

          <div class="mb-3">
            <label class="form-label small">Địa chỉ cụ thể (Số nhà / Tên đường)</label>
            <input
              v-model="addressForm.chiTiet"
              placeholder="VD: Số 21 ngõ 70"
              class="form-control form-control-sm"
            />
          </div>

          <!-- NÚT BẤM CHỌN ĐỊA CHỈ MẶC ĐỊNH RÕ RÀNG -->
          <div class="mb-4 mt-2">
            <button
              type="button"
              class="btn btn-sm w-100 py-2 d-flex justify-content-center align-items-center fw-semibold transition-all"
              :class="addressForm.laMacDinh ? 'btn-success' : 'btn-outline-secondary'"
              @click="addressForm.laMacDinh = !addressForm.laMacDinh"
            >
              <span v-if="addressForm.laMacDinh">✔ Đã đặt làm địa chỉ mặc định</span>
              <span v-else>🔳 Đặt làm địa chỉ mặc định</span>
            </button>
          </div>

          <div class="d-flex justify-content-end gap-2">
            <button @click="showModal = false" class="btn btn-secondary px-4">Hủy</button>
            <button @click="saveAddress" class="btn btn-primary px-4 fw-bold">Lưu địa chỉ</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import axios from 'axios'
import LayoutHeader from '@/components/LayoutHeader.vue'

const CLOUD_NAME = 'dqciew3rk'
const UPLOAD_PRESET = 'trendfit_preset'

const userId = localStorage.getItem('user_id')
const profile = ref({ hoTen: '', email: '', soDienThoai: '', anhDaiDien: '', danhSachDiaChi: [] })

// Biến cho tính năng cắt ảnh Avatar
const showCropModal = ref(false)
const cropCanvas = ref(null)
const uploading = ref(false)
let imageObj = null
const zoom = ref(1)
const offsetX = ref(0)
const offsetY = ref(0)
let baseW = 300
let baseH = 300
let isDragging = false
let startX = 0
let startY = 0

const showModal = ref(false)
const editingAddressId = ref(null)
const addressForm = ref({
  tenNguoiNhan: '',
  soDienThoai: '',
  tinhThanh: '',
  xaPhuong: '',
  chiTiet: '',
  laMacDinh: false,
})

const danhSachTinhThanh = ref([])

const tinhThanhSearch = ref('')
const openTinhThanhDropdown = ref(false)
const tinhThanhWrapRef = ref(null)

const boDauTiengViet = (str) => {
  return (str || '')
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
}

const tinhThanhGoiY = computed(() => {
  const kw = boDauTiengViet(tinhThanhSearch.value)
  if (!kw) return danhSachTinhThanh.value
  return danhSachTinhThanh.value.filter((t) => boDauTiengViet(t.name).includes(kw))
})

const loadTinhThanh = async () => {
  try {
    const res = await axios.get('https://provinces.open-api.vn/api/?depth=1')
    danhSachTinhThanh.value = res.data || []
  } catch (err) {
    console.error('Không thể tải tỉnh thành:', err)
  }
}

const chonTinhThanh = (tinh) => {
  addressForm.value.tinhThanh = tinh.name
  tinhThanhSearch.value = tinh.name
  openTinhThanhDropdown.value = false
}

function onTinhThanhBlur() {
  setTimeout(() => {
    openTinhThanhDropdown.value = false
    if (tinhThanhSearch.value !== addressForm.value.tinhThanh) {
      tinhThanhSearch.value = addressForm.value.tinhThanh || ''
    }
  }, 250)
}

const loadProfile = async () => {
  if (!userId) return
  try {
    const res = await axios.get(`http://localhost:8080/api/public/profile/${userId}`)
    profile.value = res.data
  } catch (err) {
    console.error(err)
  }
}

// Xử lý chọn file ảnh từ máy (Khởi tạo phủ kín khung 300x300 để có dư địa kéo thả mượt mà)
const onFileSelected = (event) => {
  const file = event.target.files[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = (e) => {
    imageObj = new Image()
    imageObj.onload = () => {
      zoom.value = 1
      const canvasSize = 300
      const scale = Math.max(canvasSize / imageObj.width, canvasSize / imageObj.height)
      baseW = imageObj.width * scale
      baseH = imageObj.height * scale

      // Căn giữa ảnh
      offsetX.value = (canvasSize - baseW) / 2
      offsetY.value = (canvasSize - baseH) / 2

      showCropModal.value = true
      nextTick(() => drawCanvas())
    }
    imageObj.src = e.target.result
  }
  reader.readAsDataURL(file)
  event.target.value = ''
}

const drawCanvas = () => {
  const canvas = cropCanvas.value
  if (!canvas || !imageObj) return
  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  const canvasSize = 300
  const currentW = baseW * zoom.value
  const currentH = baseH * zoom.value

  // Vùng khung tròn crop nằm ở giữa: x từ 40 đến 260, y từ 40 đến 260
  const cropLeft = 40
  const cropTop = 40
  const cropRight = 260
  const cropBottom = 260

  // Giới hạn biên chuẩn Facebook: Vòng tròn cắt luôn nằm trọn trong ảnh, không bao giờ lộ khoảng trống
  const minX = cropRight - currentW
  const maxX = cropLeft
  const minY = cropBottom - currentH
  const maxY = cropTop

  if (offsetX.value < minX) offsetX.value = minX
  if (offsetX.value > maxX) offsetX.value = maxX
  if (offsetY.value < minY) offsetY.value = minY
  if (offsetY.value > maxY) offsetY.value = maxY

  ctx.save()
  ctx.drawImage(imageObj, offsetX.value, offsetY.value, currentW, currentH)
  ctx.restore()
}

const startDrag = (e) => {
  isDragging = true
  const clientX = e.clientX || (e.touches && e.touches[0].clientX)
  const clientY = e.clientY || (e.touches && e.touches[0].clientY)
  startX = clientX - offsetX.value
  startY = clientY - offsetY.value
}

const onDrag = (e) => {
  if (!isDragging) return
  const clientX = e.clientX || (e.touches && e.touches[0].clientX)
  const clientY = e.clientY || (e.touches && e.touches[0].clientY)
  if (clientX !== undefined && clientY !== undefined) {
    offsetX.value = clientX - startX
    offsetY.value = clientY - startY
    drawCanvas()
  }
}

const stopDrag = () => {
  isDragging = false
}

// Cắt đúng vùng tròn trung tâm (220x220) và upload lên Cloudinary
const confirmCropAndUpload = async () => {
  const canvas = cropCanvas.value
  if (!canvas) return

  const tempCanvas = document.createElement('canvas')
  tempCanvas.width = 220
  tempCanvas.height = 220
  const tCtx = tempCanvas.getContext('2d')

  // Cắt chính xác vùng tròn trung tâm (x=40, y=40, w=220, h=220)
  tCtx.drawImage(canvas, 40, 40, 220, 220, 0, 0, 220, 220)

  tempCanvas.toBlob(async (blob) => {
    if (!blob) return
    const formData = new FormData()
    formData.append('file', blob)
    formData.append('upload_preset', UPLOAD_PRESET)

    uploading.value = true
    try {
      const res = await axios.post(
        `https://api.cloudinary.com/v1_1/${CLOUD_NAME}/image/upload`,
        formData,
      )
      profile.value.anhDaiDien = res.data.secure_url
      showCropModal.value = false
      alert('Cắt và tải ảnh lên thành công! Bấm "Cập nhật thông tin" để lưu lại.')
    } catch (err) {
      alert('Tải ảnh lên thất bại!')
      console.error(err)
    } finally {
      uploading.value = false
    }
  }, 'image/png')
}

const updateProfile = async () => {
  try {
    await axios.put(`http://localhost:8080/api/public/profile/${userId}`, profile.value)
    alert('Cập nhật thông tin thành công!')
    loadProfile()
  } catch (err) {
    alert('Lỗi cập nhật!')
  }
}

const moFormThemDC = () => {
  editingAddressId.value = null
  addressForm.value = {
    tenNguoiNhan: profile.value.hoTen,
    soDienThoai: profile.value.soDienThoai,
    tinhThanh: '',
    xaPhuong: '',
    chiTiet: '',
    laMacDinh: false,
  }
  tinhThanhSearch.value = ''
  showModal.value = true
}

const suaDiaChi = (dc) => {
  editingAddressId.value = dc.id
  addressForm.value = {
    id: dc.id,
    tenNguoiNhan: dc.tenNguoiNhan,
    soDienThoai: dc.soDienThoai,
    tinhThanh: dc.tinhThanh || '',
    xaPhuong: dc.xaPhuong || '',
    chiTiet: dc.chiTiet || dc.duong || '',
    laMacDinh: dc.laMacDinh || false,
  }
  tinhThanhSearch.value = dc.tinhThanh || ''
  showModal.value = true
}

const saveAddress = async () => {
  if (!addressForm.value.tinhThanh || !addressForm.value.xaPhuong || !addressForm.value.chiTiet) {
    alert('Vui lòng điền đủ Tỉnh/Thành, Xã/Phường và Địa chỉ cụ thể!')
    return
  }

  try {
    const payload = {
      id: addressForm.value.id || null,
      tenNguoiNhan: addressForm.value.tenNguoiNhan,
      soDienThoai: addressForm.value.soDienThoai,
      tinhThanh: addressForm.value.tinhThanh,
      xaPhuong: addressForm.value.xaPhuong,
      duong: addressForm.value.chiTiet,
      laMacDinh: addressForm.value.laMacDinh || false,
    }

    await axios.post(`http://localhost:8080/api/public/profile/${userId}/address`, payload)
    alert('Lưu địa chỉ thành công!')
    showModal.value = false
    loadProfile()
  } catch (err) {
    alert('Lỗi lưu địa chỉ!')
    console.error(err)
  }
}

const xoaDiaChi = async (id) => {
  if (confirm('Xóa địa chỉ này?')) {
    try {
      await axios.delete(`http://localhost:8080/api/public/profile/address/${id}`)
      loadProfile()
    } catch (err) {
      alert('Xóa thất bại!')
    }
  }
}

onMounted(() => {
  loadProfile()
  loadTinhThanh()
})
</script>

<style scoped>
.modal {
  display: block;
}

.transition-all {
  transition: all 0.3s ease;
}

.combobox-wrap {
  position: relative;
}
.combobox {
  position: relative;
}
.combobox-list {
  position: absolute;
  z-index: 1060;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  max-height: 200px;
  overflow-y: auto;
  background: #fff;
  border: 1.5px solid #dee2e6;
  border-radius: 8px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  padding: 6px;
}
.combobox-item {
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  color: #212529;
  transition: background 0.1s ease-in-out;
}
.combobox-item:hover {
  background: #f1f3f5;
}
.combobox-item.active {
  background: #212529;
  color: #fff;
  font-weight: 600;
}
</style>
