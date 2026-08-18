<template>
  <div class="checkout-view container py-5">
    <h2 class="fw-bold mb-4">THANH TOÁN ĐƠN HÀNG</h2>
    <div class="row">
      <div class="col-md-7">
        <div class="card p-4 shadow-sm border-0">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="m-0">Thông tin giao hàng</h5>
            <div v-if="danhSachDiaChi.length > 0" class="dropdown">
              <button
                class="btn btn-sm btn-outline-dark dropdown-toggle"
                type="button"
                data-bs-toggle="dropdown"
              >
                📍 Chọn từ sổ địa chỉ
              </button>
              <ul class="dropdown-menu shadow">
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

          <input
            v-model="form.hoTen"
            class="form-control mb-3"
            placeholder="Họ và tên người nhận"
          />
          <input v-model="form.sdt" class="form-control mb-3" placeholder="Số điện thoại" />
          <input
            v-model="form.soNhaDuong"
            class="form-control mb-3"
            placeholder="Số nhà, tên đường (ví dụ: Số 21 ngõ 70)"
          />

          <!-- ===================== TÌM KIẾM & CHỌN TỈNH / THÀNH PHỐ ===================== -->
          <div class="mb-3 combobox-wrap" ref="tinhThanhWrapRef">
            <label class="form-label fw-semibold">Tỉnh / Thành phố nhận hàng</label>
            <div class="combobox">
              <input
                v-model="tinhThanhSearch"
                @focus="openTinhThanhDropdown = true"
                @blur="onTinhThanhBlur"
                class="form-control"
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

          <!-- ===================== TÌM KIẾM & CHỌN XÃ / PHƯỜNG ===================== -->
          <div class="mb-3 combobox-wrap" ref="xaPhuongWrapRef">
            <label class="form-label fw-semibold">Xã / Phường</label>
            <div class="combobox">
              <input
                v-model="xaPhuongSearch"
                :disabled="!form.tinhThanh || loadingXa"
                @focus="openXaPhuongDropdown = true"
                @blur="onXaPhuongBlur"
                class="form-control"
                autocomplete="off"
                :placeholder="
                  loadingXa ? 'Đang tải danh sách xã/phường...' : 'Gõ để tìm xã/phường...'
                "
              />
              <div v-if="openXaPhuongDropdown && xaPhuongGoiY.length > 0" class="combobox-list">
                <div
                  v-for="xa in xaPhuongGoiY"
                  :key="xa.code"
                  class="combobox-item"
                  :class="{ active: form.xaPhuong === xa.name }"
                  @mousedown.prevent="chonXaPhuong(xa)"
                >
                  {{ xa.name }}
                </div>
              </div>
            </div>
          </div>

          <!-- ===================== PHÍ VẬN CHUYỂN ===================== -->
          <div class="mb-3">
            <label class="form-label d-flex justify-content-between align-items-center">
              <span>Phí vận chuyển</span>
              <small class="text-muted">Tính theo khu vực tỉnh thành</small>
            </label>
            <div class="form-control bg-light fw-bold text-dark">
              {{ formatPrice(phiVanChuyen) }}
            </div>
            <div v-if="totalPrice >= NGUONG_MIEN_PHI_SHIP" class="form-text text-success">
              🎉 Đơn hàng từ {{ formatPrice(NGUONG_MIEN_PHI_SHIP) }} được miễn phí vận chuyển!
            </div>
          </div>

          <h5 class="mt-3">Phương thức thanh toán</h5>
          <select v-model="form.phuongThucThanhToan" class="form-select mb-3">
            <option value="COD">Thanh toán khi nhận hàng (COD)</option>
            <option value="CHUYEN_KHOAN">Chuyển khoản ngân hàng</option>
          </select>

          <!-- ===================== QR THANH TOÁN (VietQR) ===================== -->
          <div
            v-if="form.phuongThucThanhToan === 'CHUYEN_KHOAN'"
            class="text-center border rounded p-3 mb-3 bg-light"
          >
            <p class="text-muted small mb-2">
              Quét mã QR bên dưới bằng app ngân hàng để chuyển khoản trước khi đơn được xử lý
            </p>
            <img
              :src="vietQrUrl"
              alt="Mã QR chuyển khoản"
              class="img-fluid border rounded bg-white"
              style="max-width: 240px"
            />
            <p class="fw-bold fs-5 mt-2 mb-0 text-primary">{{ formatPrice(finalPrice) }}</p>
            <p class="small text-muted mb-0">Nội dung CK: {{ noiDungChuyenKhoan }}</p>
          </div>

          <div class="input-group mb-3">
            <input v-model="voucherCode" class="form-control" placeholder="Nhập mã giảm giá" />
            <button @click="apDungVoucher" class="btn btn-outline-secondary" type="button">
              Áp dụng
            </button>
          </div>

          <div v-if="appliedVoucher" class="d-flex justify-content-between text-success">
            <span>Giảm giá ({{ appliedVoucher.ma || appliedVoucher.ten }}) :</span>
            <span>-{{ formatPrice(giamGia) }}</span>
          </div>

          <div class="d-flex justify-content-between">
            <span>Phí vận chuyển:</span>
            <span>{{ phiVanChuyen > 0 ? formatPrice(phiVanChuyen) : 'Miễn phí' }}</span>
          </div>

          <div class="d-flex justify-content-between fw-bold h5">
            <span>Tổng thanh toán:</span>
            <span class="text-danger">{{ formatPrice(finalPrice) }}</span>
          </div>
        </div>
      </div>

      <div class="col-md-5">
        <div class="card p-4 shadow-sm bg-light border-0">
          <h5 class="mb-3">Đơn hàng của bạn (Sản phẩm đã chọn)</h5>

          <div v-if="cart.length === 0" class="text-muted small mb-3">
            Chưa có sản phẩm nào được chọn thanh toán.
            <router-link to="/cart">Quay lại giỏ hàng</router-link>
          </div>

          <div
            v-for="(item, index) in cart"
            :key="index"
            class="d-flex justify-content-between mb-2 small"
          >
            <span>{{ item.ten }} (x{{ item.quantity }})</span>
            <span>{{ formatPrice(item.gia * item.quantity) }}</span>
          </div>
          <hr />

          <div class="d-flex justify-content-between small text-muted">
            <span>Tạm tính:</span>
            <span>{{ formatPrice(totalPrice) }}</span>
          </div>
          <div v-if="appliedVoucher" class="d-flex justify-content-between small text-success">
            <span>Giảm giá:</span>
            <span>-{{ formatPrice(giamGia) }}</span>
          </div>
          <div class="d-flex justify-content-between small text-muted mb-2">
            <span>Phí vận chuyển:</span>
            <span>{{ phiVanChuyen > 0 ? formatPrice(phiVanChuyen) : 'Miễn phí' }}</span>
          </div>

          <div class="d-flex justify-content-between fw-bold h5">
            <span>Tổng thanh toán:</span>
            <span class="text-danger">{{ formatPrice(finalPrice) }}</span>
          </div>
          <button
            @click="confirmOrder"
            class="btn btn-dark w-100 mt-3 py-3"
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
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
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
const danhSachXaPhuong = ref([])
const loadingXa = ref(false)

// Biến cho tính năng tìm kiếm (combobox)
const tinhThanhSearch = ref('')
const openTinhThanhDropdown = ref(false)
const tinhThanhWrapRef = ref(null)

const xaPhuongSearch = ref('')
const openXaPhuongDropdown = ref(false)
const xaPhuongWrapRef = ref(null)

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

// Lọc xã phường theo từ khóa tìm kiếm
const xaPhuongGoiY = computed(() => {
  const keyword = boDauTiengViet(xaPhuongSearch.value)
  if (!keyword) return danhSachXaPhuong.value
  return danhSachXaPhuong.value.filter((x) => boDauTiengViet(x.name).includes(keyword))
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
const chonTinhThanh = async (tinh) => {
  form.value.tinhThanh = tinh.name
  tinhThanhSearch.value = tinh.name
  openTinhThanhDropdown.value = false

  form.value.xaPhuong = ''
  xaPhuongSearch.value = ''
  danhSachXaPhuong.value = []

  loadingXa.value = true
  try {
    const resTinh = await axios.get(`https://provinces.open-api.vn/api/p/${tinh.code}?depth=2`)
    const districts = resTinh.data?.districts || []
    let allWards = []
    for (const dist of districts) {
      try {
        const resHuyen = await axios.get(`https://provinces.open-api.vn/api/d/${dist.code}?depth=2`)
        if (resHuyen.data && resHuyen.data.wards) {
          allWards = allWards.concat(resHuyen.data.wards)
        }
      } catch (e) {}
    }
    allWards.sort((a, b) => a.name.localeCompare(b.name))
    danhSachXaPhuong.value = allWards
  } catch (err) {
    console.error('Không thể tải danh sách xã phường:', err)
  } finally {
    loadingXa.value = false
  }

  tinhThanhGoiYPhiShip(tinh.name)
}

const chonXaPhuong = (xa) => {
  form.value.xaPhuong = xa.name
  xaPhuongSearch.value = xa.name
  openXaPhuongDropdown.value = false
}

function onTinhThanhBlur() {
  setTimeout(() => {
    openTinhThanhDropdown.value = false
    if (tinhThanhSearch.value !== form.value.tinhThanh) {
      tinhThanhSearch.value = form.value.tinhThanh || ''
    }
  }, 200)
}

function onXaPhuongBlur() {
  setTimeout(() => {
    openXaPhuongDropdown.value = false
    if (xaPhuongSearch.value !== form.value.xaPhuong) {
      xaPhuongSearch.value = form.value.xaPhuong || ''
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

  // 1. Nội thành / Vùng trọng điểm kinh tế (Thấp nhất)
  const nhomTrongDiem = ['hà nội', 'hồ chí minh', 'đà nẵng']
  // 2. Các tỉnh lân cận / Miền Bắc gần
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
  // 3. Các tỉnh miền Trung / Tây Nguyên
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
  // 4. Các tỉnh miền Nam gần
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
    // Các tỉnh vùng sâu, vùng xa, miền núi phía Bắc / Tây Nam Bộ xa
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

async function chonDiaChiCoSan(dc) {
  form.value.hoTen = dc.tenNguoiNhan
  form.value.sdt = dc.soDienThoai
  form.value.soNhaDuong = dc.chiTiet || dc.duong || ''
  form.value.tinhThanh = dc.tinhThanh || ''
  tinhThanhSearch.value = dc.tinhThanh || ''
  tinhThanhGoiYPhiShip(form.value.tinhThanh)

  if (form.value.tinhThanh) {
    const selectedTinh = danhSachTinhThanh.value.find((t) => t.name === form.value.tinhThanh)
    if (selectedTinh) {
      try {
        const resTinh = await axios.get(
          `https://provinces.open-api.vn/api/p/${selectedTinh.code}?depth=2`,
        )
        const districts = resTinh.data?.districts || []
        let allWards = []
        for (const dist of districts) {
          try {
            const resHuyen = await axios.get(
              `https://provinces.open-api.vn/api/d/${dist.code}?depth=2`,
            )
            if (resHuyen.data && resHuyen.data.wards) {
              allWards = allWards.concat(resHuyen.data.wards)
            }
          } catch (e) {}
        }
        allWards.sort((a, b) => a.name.localeCompare(b.name))
        danhSachXaPhuong.value = allWards
      } catch (e) {
        console.error(e)
      }
    }
  }
  form.value.xaPhuong = dc.xaPhuong || ''
  xaPhuongSearch.value = dc.xaPhuong || ''
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

const finalPrice = computed(
  () => totalPrice.value - giamGia.value + Number(phiVanChuyen.value || 0),
)

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
    return alert('Vui lòng chọn đầy đủ Tỉnh/Thành phố và Xã/Phường nhận hàng!')
  }
  if (cart.value.length === 0) {
    return alert('Không có sản phẩm nào để thanh toán!')
  }

  const idFromStorage = localStorage.getItem('user_id')
  const roleFromStorage = localStorage.getItem('user_role')
  const numericId = idFromStorage ? parseInt(idFromStorage) : null

  const fullAddress = `${form.value.soNhaDuong}, ${form.value.xaPhuong}, ${form.value.tinhThanh}`

  const payload = {
    hoTen: form.value.hoTen,
    sdt: form.value.sdt,
    diaChi: fullAddress,
    tinhThanh: form.value.tinhThanh,
    xaPhuong: form.value.xaPhuong,
    phuongThucThanhToan: form.value.phuongThucThanhToan,
    tongTienHang: totalPrice.value,
    phiVanChuyen: Number(phiVanChuyen.value || 0),
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
