<template>
  <div class="checkout-view container py-5">
    <h2 class="fw-bold mb-4">THANH TOÁN ĐƠN HÀNG</h2>
    <div class="row">
      <div class="col-md-7">
        <div class="card p-4 shadow-sm border-0">
          <h5 class="mb-3">Thông tin giao hàng</h5>
          <input
            v-model="form.hoTen"
            class="form-control mb-3"
            placeholder="Họ và tên người nhận"
          />
          <input v-model="form.sdt" class="form-control mb-3" placeholder="Số điện thoại" />
          <input
            v-model="form.diaChi"
            class="form-control mb-3"
            placeholder="Địa chỉ cụ thể (số nhà, đường, phường/xã...)"
          />

          <!-- ===================== TỈNH/THÀNH NHẬN HÀNG (tìm rồi chọn, không gõ tay) ===================== -->
          <div class="mb-3 combobox-wrap" ref="tinhThanhWrapRef">
            <label class="form-label d-block mb-2">Tỉnh / Thành phố nhận hàng</label>
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
                  :class="{ active: form.tinhThanh === tt.ten }"
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
          <!-- =============================================================================================== -->

          <!-- ===================== PHÍ VẬN CHUYỂN (chỉ áp dụng đơn online) ===================== -->
          <div class="mb-3">
            <label class="form-label d-flex justify-content-between align-items-center">
              <span>Phí vận chuyển</span>
              <small class="text-muted">Tự động tính theo khu vực nhận hàng</small>
            </label>
            <!--
              CHỈ HIỂN THỊ, KHÔNG CHO SỬA: trước đây ô này là <input> tự do,
              khách có thể tự gõ số 0 hoặc bất kỳ giá trị nào để "qua mặt" phí
              ship thật. Số hiển thị ở đây chỉ mang tính TƯƠNG ĐỐI để khách
              xem trước - con số CHÍNH THỨC luôn do BACKEND tự tính lại tại
              OrderService.taoDonHang() (không đọc giá trị này từ request),
              nên dù có sửa được ô input cũng không có tác dụng gì với đơn
              hàng thật.
            -->
            <div class="form-control bg-light">{{ formatPrice(phiVanChuyen) }}</div>
            <div v-if="totalPrice >= NGUONG_MIEN_PHI_SHIP" class="form-text text-success">
              Đơn hàng từ {{ formatPrice(NGUONG_MIEN_PHI_SHIP) }} được miễn phí vận chuyển!
            </div>
          </div>
          <!-- =================================================================================== -->


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
          <!-- ===================================================================== -->

          <div class="input-group mb-3">
            <input v-model="voucherCode" class="form-control" placeholder="Nhập mã giảm giá" />
            <button @click="apDungVoucher" class="btn btn-outline-secondary" type="button">
              Áp dụng
            </button>
          </div>

          <div v-if="appliedVoucher" class="d-flex justify-content-between text-success">
            <span>Giảm giá ({{ appliedVoucher.ma }}):</span>
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
            class="d-flex justify-content-between mb-2"
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

// 1. Chỉ đọc các sản phẩm được chọn từ trang Cart truyền sang qua sessionStorage
const cart = ref(JSON.parse(sessionStorage.getItem('checkout_items') || '[]'))

const form = ref({ hoTen: '', sdt: '', diaChi: '', tinhThanh: '', phuongThucThanhToan: 'COD' })

// ===================== TỈNH/THÀNH ĐỂ TÌM-VÀ-CHỌN (combobox) =====================
// Danh sách 34 tỉnh/thành phố của Việt Nam sau sáp nhập (hiệu lực từ
// 01/07/2025, theo Nghị quyết 202/2025/QH15 và điều chỉnh vùng kinh tế -
// xã hội tại Nghị quyết 306/NQ-CP). Mỗi tỉnh/thành được gắn `mien` để tính
// phí ship - PHẢI khớp với danh sách TINH_MIEN ở
// OrderService.tinhPhiShipGoiY() bên backend, sửa ở đâu phải sửa đồng bộ cả
// 2 nơi.
const danhSachTinhThanh = [
  // --- Miền Bắc (15) ---
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
  // --- Miền Trung (11) ---
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
  // --- Miền Nam (8) ---
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

// Lọc danh sách theo chữ khách gõ (không phân biệt dấu/hoa-thường) - khách
// CHỈ ĐƯỢC BẤM CHỌN từ danh sách này, không thể tự gõ tỉnh/thành tự do.
const tinhThanhGoiY = computed(() => {
  const kw = boThauKhongDau(tinhThanhSearch.value)
  if (!kw) return danhSachTinhThanh
  return danhSachTinhThanh.filter((tt) => boThauKhongDau(tt.ten).includes(kw))
})

function chonTinhThanh(tt) {
  form.value.tinhThanh = tt.ten
  tinhThanhSearch.value = tt.ten
  openTinhThanhDropdown.value = false
}

// Khi rời khỏi ô: nếu khách gõ chữ nhưng không bấm chọn (hoặc gõ sai với
// tỉnh/thành đã chọn), reset lại ô hiển thị về đúng giá trị ĐÃ CHỌN để
// tránh lưu nhầm 1 chuỗi tự do không có trong danh sách.
function onTinhThanhBlur() {
  openTinhThanhDropdown.value = false
  if (tinhThanhSearch.value !== form.value.tinhThanh) {
    tinhThanhSearch.value = form.value.tinhThanh || ''
  }
}

// Bấm ra ngoài khu vực combobox thì đóng danh sách gợi ý lại
function onClickNgoaiCombobox(e) {
  if (tinhThanhWrapRef.value && !tinhThanhWrapRef.value.contains(e.target)) {
    openTinhThanhDropdown.value = false
  }
}
onMounted(() => document.addEventListener('mousedown', onClickNgoaiCombobox))
onUnmounted(() => document.removeEventListener('mousedown', onClickNgoaiCombobox))
// ==================================================================================

const totalPrice = computed(() => cart.value.reduce((sum, i) => sum + i.gia * i.quantity, 0))

const formatPrice = (v) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)

const voucherCode = ref('')
const appliedVoucher = ref(null)

// Tính số tiền được giảm
const giamGia = computed(() => {
  if (!appliedVoucher.value) return 0
  const v = appliedVoucher.value
  if (v.loai === 'PERCENT') return (totalPrice.value * v.giaTriGiam) / 100
  return v.giaTriGiam // Trường hợp FIXED
})

// ===================== PHÍ VẬN CHUYỂN (chỉ áp dụng cho đơn ĐẶT ONLINE) =====================
// Yêu cầu nghiệp vụ:
//   - Bán tại quầy (offline, xem AdminPosView.vue): phí ship = 0, khách trả ngay.
//   - Bán online (trang này): khách nhập địa chỉ nhận hàng -> hệ thống TỰ
//     TÍNH GỢI Ý phí ship theo địa chỉ + giá trị đơn hàng, khách/hệ thống
//     vẫn có thể NHẬP LẠI số khác nếu cần -> phí ship được CỘNG vào tổng
//     thanh toán -> đơn chuyển trạng thái "Chờ xác nhận" (xem
//     OrderService.taoDonHang(), trangThai mặc định = CHO_XAC_NHAN) để nhân
//     viên xác nhận rồi mới giao cho đơn vị vận chuyển.
const NGUONG_MIEN_PHI_SHIP = 500000 // Đơn từ 500k trở lên được miễn phí ship
const phiVanChuyen = ref(0)

// Gợi ý phí ship theo TỈNH/THÀNH khách chọn (tra ra vùng miền của tỉnh đó
// rồi áp bảng giá theo vùng). NẾU SAU NÀY SỬA BẢNG GIÁ, PHẢI SỬA ĐỒNG BỘ VỚI
// TINH_MIEN + PHI_SHIP_THEO_VUNG ở OrderService.tinhPhiShipGoiY() bên backend.
const PHI_SHIP_THEO_VUNG = {
  BAC: 20000,
  TRUNG: 30000,
  NAM: 35000,
}

function goiYPhiShip(tenTinhThanh, tongTienHang) {
  if (tongTienHang >= NGUONG_MIEN_PHI_SHIP) return 0
  const tt = danhSachTinhThanh.find((t) => t.ten === tenTinhThanh)
  if (!tt) return 0
  return PHI_SHIP_THEO_VUNG[tt.mien] ?? 35000
}

// Mỗi khi khách chọn tỉnh/thành hoặc giỏ hàng thay đổi giá trị -> tự cập
// nhật lại GỢI Ý phí ship.
watch(
  () => [form.value.tinhThanh, totalPrice.value],
  () => {
    phiVanChuyen.value = goiYPhiShip(form.value.tinhThanh, totalPrice.value)
  },
  { immediate: true },
)
// ==============================================================================================

// Tổng tiền sau giảm giá VÀ đã cộng phí vận chuyển
const finalPrice = computed(
  () => totalPrice.value - giamGia.value + Number(phiVanChuyen.value || 0),
)

// ===================== QR THANH TOÁN (VietQR) =====================
// Dùng dịch vụ công khai VietQR.io để tạo ảnh QR động theo ĐÚNG số tiền cần
// thu (đã gồm phí ship) - không cần gọi API riêng, không cần API key.
// ⚠️ THAY 3 GIÁ TRỊ DƯỚI ĐÂY BẰNG THÔNG TIN TÀI KHOẢN NGÂN HÀNG THẬT CỦA CỬA
// HÀNG trước khi dùng trong thực tế (hiện đang là giá trị mẫu để demo), và
// giữ NHẤT QUÁN với 3 giá trị tương ứng trong AdminPosView.vue.
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
// =====================================================================

const apDungVoucher = async () => {
  try {
    const res = await axios.post(`http://localhost:8080/api/public/vouchers/check`, {
      ma: voucherCode.value,
      tongDon: totalPrice.value,
    })
    appliedVoucher.value = res.data
    alert('Áp dụng mã thành công!')
  } catch (err) {
    // Backend trả lỗi dạng chuỗi thuần (ví dụ "Mã đã hết hạn!"), không phải
    // object có field .message - đọc trực tiếp err.response.data trước.
    const thongBaoLoi = err.response?.data
    alert(
      (typeof thongBaoLoi === 'string' && thongBaoLoi) ||
        thongBaoLoi?.message ||
        'Mã không hợp lệ!',
    )
  }
}

// 2. Xử lý đặt hàng và chỉ xóa những món đã mua khỏi giỏ hàng chung
const confirmOrder = async () => {
  if (!form.value.hoTen || !form.value.sdt || !form.value.diaChi) {
    return alert('Vui lòng điền đủ thông tin!')
  }

  if (!form.value.tinhThanh) {
    return alert('Vui lòng tìm và chọn tỉnh/thành nhận hàng!')
  }

  if (cart.value.length === 0) {
    return alert('Không có sản phẩm nào để thanh toán!')
  }

  const idFromStorage = localStorage.getItem('user_id')
  const roleFromStorage = localStorage.getItem('user_role')
  const numericId = idFromStorage ? parseInt(idFromStorage) : null

  const payload = {
    ...form.value,
    // Gộp địa chỉ cụ thể + tỉnh/thành đã chọn thành 1 chuỗi địa chỉ đầy đủ để
    // lưu vào đơn hàng (form.diaChi vẫn giữ nguyên chỉ là phần cụ thể, dùng
    // hiển thị lại trên form nếu khách bấm quay lại sửa).
    diaChi: `${form.value.diaChi}, ${form.value.tinhThanh}`,
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
    // Bước 1: Gửi request tạo đơn hàng lên Backend
    await axios.post('http://localhost:8080/api/public/orders', payload)

    // Bước 2: Lấy toàn bộ giỏ hàng gốc trên LocalStorage ra
    let fullCart = JSON.parse(localStorage.getItem('cart') || '[]')

    // Lọc bỏ những món vừa thanh toán, chỉ giữ lại những món KHÔNG ĐƯỢC CHỌN
    const checkedOutIds = cart.value.map((i) => i.bienTheId)
    const remainingCart = fullCart.filter((i) => !checkedOutIds.includes(i.bienTheId))

    // Bước 3: Cập nhật lại giỏ hàng còn lại vào LocalStorage
    localStorage.setItem('cart', JSON.stringify(remainingCart))

    // Bước 4: Đồng bộ giỏ hàng mới (đã loại bỏ món đã mua) lên Database
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

    // Xóa bộ nhớ tạm thanh toán
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
/* ===================== COMBOBOX TÌM & CHỌN TỈNH/THÀNH ===================== */
.combobox-wrap {
  position: relative;
}

.combobox {
  position: relative;
}

.combobox-list {
  position: absolute;
  z-index: 20;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  max-height: 240px;
  overflow-y: auto;
  background: #fff;
  border: 1.5px solid #dee2e6;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  padding: 6px;
}

.combobox-item {
  padding: 9px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.92rem;
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
  padding: 10px 12px;
  font-size: 0.88rem;
  color: #adb5bd;
  text-align: center;
}
</style>
