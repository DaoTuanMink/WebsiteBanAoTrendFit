<template>
  <div class="checkout-view container py-5">
    <h2 class="fw-bold mb-4">THANH TOÁN ĐƠN HÀNG</h2>
    <div class="row g-4">
      <!-- CỘT TRÁI: THÔNG TIN GIAO HÀNG -->
      <div class="col-lg-7">
        <div class="card p-4 shadow-sm border-0">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="m-0 fw-bold">Thông tin giao hàng</h5>
            <div v-if="danhSachDiaChi.length > 0" class="dropdown">
              <button
                class="btn btn-sm btn-outline-dark dropdown-toggle fw-semibold"
                type="button"
                data-bs-toggle="dropdown"
              >
                📍 Chọn từ sổ địa chỉ
              </button>
              <ul class="dropdown-menu shadow-sm">
                <li v-for="dc in danhSachDiaChi" :key="dc.id">
                  <a class="dropdown-item small py-2" href="#" @click.prevent="chonDiaChiCoSan(dc)">
                    <strong>{{ dc.tenNguoiNhan }}</strong> ({{ dc.soDienThoai }})<br />
                    <span class="text-muted"
                      >{{ dc.chiTiet }}, {{ dc.xaPhuong }}, {{ dc.tinhThanh }}</span
                    >
                  </a>
                </li>
              </ul>
            </div>
          </div>

          <div class="row g-3 mb-3">
            <div class="col-md-6">
              <input
                v-model="form.hoTen"
                class="form-control bg-light"
                placeholder="Họ và tên người nhận"
              />
            </div>
            <div class="col-md-6">
              <input v-model="form.sdt" class="form-control bg-light" placeholder="Số điện thoại" />
            </div>
            <div class="col-12">
              <input
                v-model="form.soNhaDuong"
                class="form-control bg-light"
                placeholder="Số nhà, tên đường (ví dụ: Số 21 ngõ 70)"
              />
            </div>
          </div>

          <!-- TÌM KIẾM & CHỌN TỈNH / THÀNH PHỐ -->
          <div class="mb-3 combobox-wrap" ref="tinhThanhWrapRef">
            <label class="form-label fw-semibold small">Tỉnh / Thành phố nhận hàng</label>
            <div class="combobox">
              <input
                v-model="tinhThanhSearch"
                @focus="openTinhThanhDropdown = true"
                @blur="onTinhThanhBlur"
                class="form-control bg-light"
                autocomplete="off"
                placeholder="Gõ để tìm tỉnh/thành (VD: Hà Nội, Hồ Chí Minh)..."
              />
              <div v-if="openTinhThanhDropdown && tinhThanhGoiY.length > 0" class="combobox-list">
                <div
                  v-for="tinh in tinhThanhGoiY"
                  :key="tinh.code"
                  class="combobox-item"
                  :class="{ active: form.tinhThanh === tinh.name }"
                  @mousedown.prevent="chonTinhThanh(tinh)"
                >
                  {{ tinh.name }}
                </div>
              </div>
            </div>
          </div>

          <!-- NHẬP TỰ DO QUẬN / HUYỆN & XÃ / PHƯỜNG -->
          <div class="mb-4">
            <label class="form-label fw-semibold small">Xã/Phường, Quận/Huyện</label>
            <input
              v-model="form.xaPhuong"
              class="form-control bg-light"
              placeholder="VD: Phường Bến Nghé, Quận 1"
            />
          </div>

          <!-- PHÍ VẬN CHUYỂN -->
          <div class="mb-4">
            <label class="form-label fw-semibold d-flex justify-content-between align-items-center">
              <span>Phí vận chuyển</span>
              <span v-if="totalPrice >= NGUONG_MIEN_PHI_SHIP" class="badge bg-success"
                >Miễn phí ship</span
              >
            </label>
            <div class="form-control bg-light fw-bold text-dark d-flex justify-content-between">
              <span>Cước phí ước tính:</span>
              <span
                :class="{
                  'text-decoration-line-through text-muted fw-normal':
                    totalPrice >= NGUONG_MIEN_PHI_SHIP,
                }"
              >
                {{ formatPrice(phiVanChuyen) }}
              </span>
            </div>
            <div v-if="totalPrice >= NGUONG_MIEN_PHI_SHIP" class="form-text text-success mt-2">
              🎉 Đơn hàng của bạn đã đạt điều kiện miễn phí vận chuyển!
            </div>
          </div>

          <h5 class="fw-bold mb-3 mt-4">Phương thức thanh toán</h5>
          <select v-model="form.phuongThucThanhToan" class="form-select bg-light mb-4">
            <option value="COD">Thanh toán khi nhận hàng (COD)</option>
            <option value="CHUYEN_KHOAN">Chuyển khoản ngân hàng</option>
          </select>

          <!-- QR THANH TOÁN (VietQR) -->
          <div
            v-if="form.phuongThucThanhToan === 'CHUYEN_KHOAN'"
            class="text-center border border-primary rounded p-4 mb-4 bg-primary bg-opacity-10"
          >
            <h6 class="fw-bold text-primary mb-3">Quét mã QR để thanh toán</h6>
            <img
              :src="vietQrUrl"
              alt="Mã QR chuyển khoản"
              class="img-fluid border rounded shadow-sm bg-white p-2"
              style="max-width: 220px"
            />
            <div class="mt-3">
              <p class="fw-bold fs-4 mb-1 text-danger">{{ formatPrice(finalPrice) }}</p>
              <p class="small text-muted mb-0">
                Nội dung CK: <strong>{{ noiDungChuyenKhoan }}</strong>
              </p>
            </div>
          </div>

          <!-- MÃ GIẢM GIÁ -->
          <label class="form-label fw-semibold small">Mã giảm giá (Nếu có)</label>
          <div class="input-group mb-2">
            <input
              v-model="voucherCode"
              class="form-control bg-light"
              placeholder="Nhập mã giảm giá..."
            />
            <button @click="apDungVoucher" class="btn btn-dark px-4 fw-semibold" type="button">
              Áp dụng
            </button>
          </div>
          <div v-if="appliedVoucher" class="text-success small fw-semibold">
            <i class="bi bi-check-circle-fill me-1"></i> Đã áp dụng mã:
            {{ appliedVoucher.ma || appliedVoucher.ten }}
          </div>
        </div>
      </div>

      <!-- CỘT PHẢI: CHI TIẾT ĐƠN HÀNG -->
      <div class="col-lg-5">
        <div class="card p-4 shadow-sm border-0 sticky-sidebar bg-white rounded-4">
          <h5 class="fw-bold mb-4">Đơn hàng của bạn</h5>

          <div v-if="cart.length === 0" class="text-center py-4 text-muted">
            <p class="mb-2">Chưa có sản phẩm nào.</p>
            <router-link to="/cart" class="btn btn-outline-dark btn-sm"
              >Quay lại giỏ hàng</router-link
            >
          </div>

          <!-- HIỂN THỊ DANH SÁCH SẢN PHẨM CHI TIẾT -->
          <div class="order-items-wrap mb-4">
            <div
              v-for="(item, index) in cart"
              :key="index"
              class="d-flex align-items-center mb-3 pb-3 border-bottom"
            >
              <!-- Hình ảnh có số lượng -->
              <div class="position-relative">
                <img
                  :src="item.anhChinh || 'https://placehold.co/100x100?text=TrendFit'"
                  alt="Sản phẩm"
                  class="rounded-3 border object-fit-cover"
                  style="width: 70px; height: 80px"
                />
                <span
                  class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-dark border border-white"
                >
                  {{ item.quantity }}
                </span>
              </div>

              <!-- Thông tin chi tiết -->
              <div class="ms-3 flex-grow-1">
                <h6 class="mb-1 fw-bold fs-6 text-truncate-2" style="font-size: 0.95rem">
                  {{ item.ten }}
                </h6>
                <p class="mb-1 small text-muted">{{ item.mauSac }} / {{ item.kichCoSize }}</p>
                <div class="fw-semibold text-danger" style="font-size: 0.95rem">
                  {{ formatPrice(item.gia * item.quantity) }}
                </div>
              </div>
            </div>
          </div>

          <!-- TỔNG KẾT TÀI CHÍNH -->
          <div class="summary-section text-muted small mb-3">
            <div class="d-flex justify-content-between mb-2">
              <span>Tạm tính:</span>
              <span class="fw-medium text-dark">{{ formatPrice(totalPrice) }}</span>
            </div>
            <div v-if="appliedVoucher" class="d-flex justify-content-between mb-2 text-success">
              <span>Giảm giá:</span>
              <span class="fw-bold">-{{ formatPrice(giamGia) }}</span>
            </div>
            <div class="d-flex justify-content-between mb-2">
              <span>Phí vận chuyển:</span>
              <span class="fw-medium text-dark">{{
                phiVanChuyen > 0 && totalPrice < NGUONG_MIEN_PHI_SHIP
                  ? formatPrice(phiVanChuyen)
                  : 'Miễn phí'
              }}</span>
            </div>
          </div>

          <hr class="border-secondary opacity-25" />

          <div class="d-flex justify-content-between align-items-center mb-4 mt-3">
            <span class="fw-bold fs-5">Tổng thanh toán:</span>
            <span class="fw-bold fs-4 text-danger">{{ formatPrice(finalPrice) }}</span>
          </div>

          <button
            @click="confirmOrder"
            class="btn btn-primary w-100 py-3 fw-bold fs-6 rounded-3"
            style="background-color: #6366f1; border: none"
            :disabled="cart.length === 0"
          >
            XÁC NHẬN ĐẶT HÀNG
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()

const cart = ref(JSON.parse(sessionStorage.getItem('checkout_items') || '[]'))
const form = ref({
  hoTen: '',
  sdt: '',
  soNhaDuong: '',
  tinhThanh: '',
  xaPhuong: '',
  phuongThucThanhToan: 'COD',
})
const danhSachDiaChi = ref([])
const danhSachTinhThanh = ref([])

// Biến cho tính năng tìm kiếm (combobox) Tỉnh/Thành phố
const tinhThanhSearch = ref('')
const openTinhThanhDropdown = ref(false)
const tinhThanhWrapRef = ref(null)

const boDauTiengViet = (str) => {
  return (str || '')
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
}

// Lọc tỉnh thành theo từ khóa tìm kiếm
const tinhThanhGoiY = computed(() => {
  const keyword = boDauTiengViet(tinhThanhSearch.value)
  if (!keyword) return danhSachTinhThanh.value
  return danhSachTinhThanh.value.filter((t) => boDauTiengViet(t.name).includes(keyword))
})

const totalPrice = computed(() => cart.value.reduce((sum, i) => sum + i.gia * i.quantity, 0))
const formatPrice = (v) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)

// Tải 63 tỉnh thành
const loadTinhThanh = async () => {
  try {
    const res = await axios.get('https://provinces.open-api.vn/api/?depth=1')
    danhSachTinhThanh.value = res.data || []
  } catch (err) {
    console.error('Không thể tải danh sách tỉnh thành:', err)
  }
}

// Chọn tỉnh
const chonTinhThanh = (tinh) => {
  form.value.tinhThanh = tinh.name
  tinhThanhSearch.value = tinh.name
  openTinhThanhDropdown.value = false
  form.value.xaPhuong = '' // Xóa dữ liệu quận/huyện cũ khi chọn tỉnh mới
  tinhThanhGoiYPhiShip(tinh.name)
}

function onTinhThanhBlur() {
  setTimeout(() => {
    openTinhThanhDropdown.value = false
    if (tinhThanhSearch.value !== form.value.tinhThanh) {
      tinhThanhSearch.value = form.value.tinhThanh || ''
    }
  }, 200)
}

// ===================== TÍNH PHÍ SHIP CHI TIẾT THEO TỪNG NHÓM TỈNH THÀNH =====================
const NGUONG_MIEN_PHI_SHIP = 500000
const phiVanChuyen = ref(0)

function tinhThanhGoiYPhiShip(tenTinhThanh) {
  if (totalPrice.value >= NGUONG_MIEN_PHI_SHIP) {
    phiVanChuyen.value = 0
    return
  }

  const tinh = (tenTinhThanh || '').toLowerCase()

  const nhomTrongDiem = ['hà nội', 'h hồ chí minh', 'đà nẵng']
  const nhomMienBacGan = [
    'hải phòng',
    'bắc ninh',
    'hưng yên',
    'vĩnh phúc',
    'bắc giang',
    'hải dương',
    'nam định',
    'hà nam',
    'thái bình',
    'ninh bình',
    'phú thọ',
    'thái nguyên',
  ]
  const nhomMienTrungTayNguyen = [
    'thừa thiên huế',
    'quảng nam',
    'quảng ngãi',
    'bình định',
    'khánh hòa',
    'ninh thuận',
    'bình thuận',
    'lâm đồng',
    'gia lai',
    'đắk lắk',
    'đắk nông',
    'kon tum',
    'thanh hóa',
    'nghệ an',
    'hà tĩnh',
    'quảng bình',
    'quảng trị',
    'phú yên',
  ]
  const nhomMienNamGan = [
    'bình dương',
    'đồng nai',
    'long an',
    'bà rịa - vũng tàu',
    'tây ninh',
    'tiền giang',
  ]

  if (nhomTrongDiem.some((t) => tinh.includes(t))) {
    phiVanChuyen.value = 20000
  } else if (
    nhomMienBacGan.some((t) => tinh.includes(t)) ||
    nhomMienNamGan.some((t) => tinh.includes(t))
  ) {
    phiVanChuyen.value = 25000
  } else if (nhomMienTrungTayNguyen.some((t) => tinh.includes(t))) {
    phiVanChuyen.value = 35000
  } else {
    phiVanChuyen.value = 45000
  }
}

watch(
  () => totalPrice.value,
  () => {
    tinhThanhGoiYPhiShip(form.value.tinhThanh)
  },
)

const fetchUserAddresses = async () => {
  const userId = localStorage.getItem('user_id')
  if (!userId) return
  try {
    const res = await axios.get(`http://localhost:8080/api/public/profile/${userId}`)
    if (!form.value.hoTen) form.value.hoTen = res.data.hoTen || ''
    if (!form.value.sdt) form.value.sdt = res.data.soDienThoai || ''
    danhSachDiaChi.value = res.data.danhSachDiaChi || []

    const macDinh = danhSachDiaChi.value.find((d) => d.laMacDinh) || danhSachDiaChi.value[0]
    if (macDinh) {
      chonDiaChiCoSan(macDinh)
    }
  } catch (err) {
    console.error('Không thể tải sổ địa chỉ:', err)
  }
}

function chonDiaChiCoSan(dc) {
  form.value.hoTen = dc.tenNguoiNhan
  form.value.sdt = dc.soDienThoai
  form.value.soNhaDuong = dc.chiTiet || dc.duong || ''
  form.value.tinhThanh = dc.tinhThanh || ''
  tinhThanhSearch.value = dc.tinhThanh || ''
  form.value.xaPhuong = dc.xaPhuong || ''
  tinhThanhGoiYPhiShip(form.value.tinhThanh)
}

onMounted(() => {
  loadTinhThanh()
  fetchUserAddresses()
})

const voucherCode = ref('')
const appliedVoucher = ref(null)

const giamGia = computed(() => {
  if (!appliedVoucher.value) return 0
  const v = appliedVoucher.value
  if (v.loai === 'PERCENT') return (totalPrice.value * v.giaTriGiam) / 100
  return v.giaTriGiam
})

const finalPrice = computed(() => {
  let phi = phiVanChuyen.value
  if (totalPrice.value >= NGUONG_MIEN_PHI_SHIP) {
    phi = 0
  }
  return totalPrice.value - giamGia.value + Number(phi)
})

const BANK_CODE = 'MB'
const BANK_ACCOUNT_NO = '0563663591'
const BANK_ACCOUNT_NAME = 'Phan The Bac'

const noiDungChuyenKhoan = computed(() => {
  const sdt = form.value.sdt?.trim()
  return sdt ? `TrendFit ${sdt}` : 'Thanh toan TrendFit'
})

const vietQrUrl = computed(() => {
  const amount = Math.max(0, Math.round(finalPrice.value || 0))
  const noiDung = encodeURIComponent(noiDungChuyenKhoan.value)
  const ten = encodeURIComponent(BANK_ACCOUNT_NAME)
  return `https://img.vietqr.io/image/${BANK_CODE}-${BANK_ACCOUNT_NO}-compact2.png?amount=${amount}&addInfo=${noiDung}&accountName=${ten}`
})

const apDungVoucher = async () => {
  try {
    const res = await axios.post(`http://localhost:8080/api/public/vouchers/check`, {
      ma: voucherCode.value,
      tongDon: totalPrice.value,
    })
    appliedVoucher.value = res.data
    alert('Áp dụng mã thành công!')
  } catch (err) {
    const thongBaoLoi = err.response?.data
    alert(
      (typeof thongBaoLoi === 'string' && thongBaoLoi) ||
        thongBaoLoi?.message ||
        'Mã không hợp lệ!',
    )
  }
}

const confirmOrder = async () => {
  if (!form.value.hoTen || !form.value.sdt || !form.value.soNhaDuong) {
    return alert('Vui lòng điền đủ họ tên, SĐT và số nhà/tên đường!')
  }
  if (!form.value.tinhThanh || !form.value.xaPhuong) {
    return alert('Vui lòng chọn đầy đủ Tỉnh/Thành phố và Xã/Phường/Quận/Huyện nhận hàng!')
  }
  if (cart.value.length === 0) {
    return alert('Không có sản phẩm nào để thanh toán!')
  }

  const idFromStorage = localStorage.getItem('user_id')
  const roleFromStorage = localStorage.getItem('user_role')
  const numericId = idFromStorage ? parseInt(idFromStorage) : null

  const fullAddress = `${form.value.soNhaDuong}, ${form.value.xaPhuong}, ${form.value.tinhThanh}`

  let phiFinal = phiVanChuyen.value
  if (totalPrice.value >= NGUONG_MIEN_PHI_SHIP) {
    phiFinal = 0
  }

  const payload = {
    hoTen: form.value.hoTen,
    sdt: form.value.sdt,
    diaChi: fullAddress,
    tinhThanh: form.value.tinhThanh,
    xaPhuong: form.value.xaPhuong,
    phuongThucThanhToan: form.value.phuongThucThanhToan,
    tongTienHang: totalPrice.value,
    phiVanChuyen: Number(phiFinal || 0),
    tongThanhToan: finalPrice.value,
    tienGiam: giamGia.value,
    voucherId: appliedVoucher.value ? appliedVoucher.value.id : null,
    creatorId: roleFromStorage === 'ADMIN' || roleFromStorage === 'EMPLOYEE' ? numericId : null,
    userId: roleFromStorage === 'CUSTOMER' ? numericId : null,
    items: cart.value.map((i) => ({
      bienTheId: i.bienTheId,
      quantity: i.quantity,
      ten: i.ten,
      gia: i.gia,
    })),
  }

  try {
    await axios.post('http://localhost:8080/api/public/orders', payload)

    let fullCart = JSON.parse(localStorage.getItem('cart') || '[]')
    const checkedOutIds = cart.value.map((i) => i.bienTheId)
    const remainingCart = fullCart.filter((i) => !checkedOutIds.includes(i.bienTheId))

    localStorage.setItem('cart', JSON.stringify(remainingCart))

    if (numericId) {
      try {
        await axios.post('http://localhost:8080/api/public/cart/sync', {
          userId: numericId,
          items: remainingCart.map((i) => ({ bienTheId: i.bienTheId, quantity: i.quantity })),
        })
      } catch (syncErr) {
        console.error('Lỗi cập nhật giỏ hàng trên server:', syncErr)
      }
    }

    sessionStorage.removeItem('checkout_items')

    alert('Đặt hàng thành công!')
    router.push('/')
  } catch (err) {
    console.error(err)
    alert('Có lỗi xảy ra: ' + (err.response?.data?.message || 'Vui lòng thử lại'))
  }
}
</script>

<style scoped>
.sticky-sidebar {
  position: sticky;
  top: 20px;
}

.order-items-wrap {
  max-height: 380px;
  overflow-y: auto;
  padding-right: 8px;
}

.order-items-wrap::-webkit-scrollbar {
  width: 6px;
}
.order-items-wrap::-webkit-scrollbar-thumb {
  background-color: #dee2e6;
  border-radius: 4px;
}

.text-truncate-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.combobox-wrap {
  position: relative;
}
.combobox {
  position: relative;
}
.combobox-list {
  position: absolute;
  z-index: 99;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  max-height: 220px;
  overflow-y: auto;
  background: #fff;
  border: 1.5px solid #dee2e6;
  border-radius: 10px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  padding: 4px;
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
