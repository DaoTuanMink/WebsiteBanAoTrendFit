<template>
  <div class="container py-5">
    <h2 class="fw-bold mb-4">HỒ SƠ CÁ NHÂN & SỔ ĐỊA CHỈ</h2>

    <div class="row g-4">
      <!-- Cột 1: Thông tin tài khoản -->
      <div class="col-md-5">
        <div class="card p-4 shadow-sm border-0 bg-white">
          <h5 class="fw-bold mb-3">Thông tin tài khoản</h5>

          <!-- Avatar và Nút đổi ảnh từ máy qua Cloudinary -->
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
                <input type="file" @change="uploadAvatar" accept="image/*" class="d-none" />
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
                    Đường: {{ dc.duong }} | Xã/Phường: {{ dc.phuongXa }} | Tỉnh/Thành:
                    {{ dc.tinhThanh }}
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

    <!-- Modal Thêm/Sửa Địa Chỉ (Có tích hợp Combobox Tỉnh/Thành chuẩn như Checkout) -->
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

          <!-- COMBOBOX TỈNH / THÀNH PHỐ Y HỆT TRANG CHECKOUT -->
          <div class="mb-2 combobox-wrap" ref="tinhThanhWrapRef">
            <label class="form-label small d-block mb-1">Tỉnh / Thành phố</label>
            <div class="combobox">
              <input
                v-model="tinhThanhSearch"
                @focus="openTinhThanhDropdown = true"
                @blur="onTinhThanhBlur"
                class="form-control"
                autocomplete="off"
                placeholder="Gõ để tìm... (VD: Hà Nội, Đà Nẵng, Hồ Chí Minh)"
              />
              <div v-if="openTinhThanhDropdown" class="combobox-list">
                <div
                  v-for="tt in tinhThanhGoiY"
                  :key="tt.ten"
                  class="combobox-item"
                  :class="{ active: addressForm.tinhThanh === tt.ten }"
                  @mousedown.prevent="chonTinhThanh(tt)"
                >
                  {{ tt.ten }}
                </div>
                <div v-if="tinhThanhGoiY.length === 0" class="combobox-empty">
                  Không tìm thấy tỉnh/thành phù hợp
                </div>
              </div>
            </div>
          </div>

          <div class="mb-2">
            <label class="form-label small">Phường / Xã</label>
            <input
              v-model="addressForm.phuongXa"
              placeholder="VD: Phường Dịch Vọng"
              class="form-control"
            />
          </div>

          <div class="mb-2">
            <label class="form-label small">Địa chỉ cụ thể (Đường / Số nhà)</label>
            <input v-model="addressForm.duong" placeholder="VD: Số 10 ngõ 5" class="form-control" />
          </div>

          <div class="form-check mb-3">
            <input
              type="checkbox"
              v-model="addressForm.laMacDinh"
              class="form-check-input"
              id="defaultCheck"
            />
            <label class="form-check-label small" for="defaultCheck"
              >Đặt làm địa chỉ mặc định</label
            >
          </div>

          <div class="d-flex justify-content-end gap-2">
            <button @click="showModal = false" class="btn btn-secondary btn-sm px-3">Hủy</button>
            <button @click="saveAddress" class="btn btn-primary btn-sm px-3">Lưu địa chỉ</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import axios from 'axios'

const CLOUD_NAME = 'dqciew3rk'
const UPLOAD_PRESET = 'trendfit_preset'

const userId = localStorage.getItem('user_id')
const profile = ref({ hoTen: '', email: '', soDienThoai: '', anhDaiDien: '', danhSachDiaChi: [] })

const showModal = ref(false)
const editingAddressId = ref(null)
const addressForm = ref({
  tenNguoiNhan: '',
  soDienThoai: '',
  tinhThanh: '',
  phuongXa: '',
  duong: '',
  laMacDinh: false,
})

// ===================== DANH SÁCH TỈNH THÀNH (Chuẩn giống Checkout) =====================
const danhSachTinhThanh = [
  { ten: 'Tuyên Quang', mien: 'BAC' },
  { ten: 'Cao Bằng', mien: 'BAC' },
  { ten: 'Lai Châu', mien: 'BAC' },
  { ten: 'Lào Cai', mien: 'BAC' },
  { ten: 'Thái Nguyên', mien: 'BAC' },
  { ten: 'Điện Biên', mien: 'BAC' },
  { ten: 'Lạng Sơn', mien: 'BAC' },
  { ten: 'Sơn La', mien: 'BAC' },
  { ten: 'Phú Thọ', mien: 'BAC' },
  { ten: 'TP. Hà Nội', mien: 'BAC' },
  { ten: 'TP. Hải Phòng', mien: 'BAC' },
  { ten: 'Bắc Ninh', mien: 'BAC' },
  { ten: 'Quảng Ninh', mien: 'BAC' },
  { ten: 'Hưng Yên', mien: 'BAC' },
  { ten: 'Ninh Bình', mien: 'BAC' },
  { ten: 'Thanh Hóa', mien: 'TRUNG' },
  { ten: 'Nghệ An', mien: 'TRUNG' },
  { ten: 'Hà Tĩnh', mien: 'TRUNG' },
  { ten: 'Quảng Trị', mien: 'TRUNG' },
  { ten: 'TP. Huế', mien: 'TRUNG' },
  { ten: 'TP. Đà Nẵng', mien: 'TRUNG' },
  { ten: 'Quảng Ngãi', mien: 'TRUNG' },
  { ten: 'Gia Lai', mien: 'TRUNG' },
  { ten: 'Đắk Lắk', mien: 'TRUNG' },
  { ten: 'Khánh Hòa', mien: 'TRUNG' },
  { ten: 'Lâm Đồng', mien: 'TRUNG' },
  { ten: 'Đồng Nai', mien: 'NAM' },
  { ten: 'Tây Ninh', mien: 'NAM' },
  { ten: 'TP. Hồ Chí Minh', mien: 'NAM' },
  { ten: 'Đồng Tháp', mien: 'NAM' },
  { ten: 'An Giang', mien: 'NAM' },
  { ten: 'Vĩnh Long', mien: 'NAM' },
  { ten: 'TP. Cần Thơ', mien: 'NAM' },
  { ten: 'Cà Mau', mien: 'NAM' },
]

const boThauKhongDau = (s) =>
  (s || '')
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
const tinhThanhSearch = ref('')
const openTinhThanhDropdown = ref(false)
const tinhThanhWrapRef = ref(null)

const tinhThanhGoiY = computed(() => {
  const kw = boThauKhongDau(tinhThanhSearch.value)
  if (!kw) return danhSachTinhThanh
  return danhSachTinhThanh.filter((tt) => boThauKhongDau(tt.ten).includes(kw))
})

function chonTinhThanh(tt) {
  addressForm.value.tinhThanh = tt.ten
  tinhThanhSearch.value = tt.ten
  openTinhThanhDropdown.value = false
}

function onTinhThanhBlur() {
  openTinhThanhDropdown.value = false
  if (tinhThanhSearch.value !== addressForm.value.tinhThanh) {
    tinhThanhSearch.value = addressForm.value.tinhThanh || ''
  }
}

function onClickNgoaiCombobox(e) {
  if (tinhThanhWrapRef.value && !tinhThanhWrapRef.value.contains(e.target)) {
    openTinhThanhDropdown.value = false
  }
}
onMounted(() => {
  loadProfile()
  document.addEventListener('mousedown', onClickNgoaiCombobox)
})
onUnmounted(() => document.removeEventListener('mousedown', onClickNgoaiCombobox))
// ======================================================================================

const loadProfile = async () => {
  if (!userId) return
  try {
    const res = await axios.get(`http://localhost:8080/api/public/profile/${userId}`)
    profile.value = res.data
  } catch (err) {
    console.error(err)
  }
}

const uploadAvatar = async (event) => {
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
    profile.value.anhDaiDien = res.data.secure_url
    alert('Tải ảnh lên thành công! Bấm "Cập nhật thông tin" để lưu lại.')
  } catch (err) {
    alert('Upload ảnh thất bại!')
    console.error(err)
  }
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
    phuongXa: '',
    duong: '',
    laMacDinh: false,
  }
  tinhThanhSearch.value = ''
  showModal.value = true
}

const suaDiaChi = (dc) => {
  editingAddressId.value = dc.id
  addressForm.value = { ...dc }
  tinhThanhSearch.value = dc.tinhThanh || ''
  showModal.value = true
}

const saveAddress = async () => {
  if (!addressForm.value.tinhThanh) {
    alert('Vui lòng chọn Tỉnh / Thành phố!')
    return
  }
  try {
    await axios.post(
      `http://localhost:8080/api/public/profile/${userId}/address`,
      addressForm.value,
    )
    alert('Lưu địa chỉ thành công!')
    showModal.value = false
    loadProfile()
  } catch (err) {
    alert('Lỗi lưu địa chỉ!')
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
</script>

<style scoped>
.modal {
  display: block;
}

/* ===================== COMBOBOX TỈNH/THÀNH ===================== */
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
.combobox-empty {
  padding: 8px 12px;
  font-size: 0.85rem;
  color: #adb5bd;
  text-align: center;
}
</style>
