<template>
  <div class="container-fluid py-3">
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
      <div>
        <h4 class="fw-bold mb-1">Bán hàng tại quầy</h4>
        <p class="text-secondary small mb-0">Tạo đơn trực tiếp cho khách mua tại cửa hàng</p>
      </div>
    </div>

    <div class="row g-4 align-items-start">
      <div class="col-12 col-xl-5">
        <PosProductPanel
          :keyword="keyword"
          :products="filteredProducts"
          :selected-product="selectedProduct"
          :variants="variants"
          :format-money="formatMoney"
          :get-price="getPrice"
          @update:keyword="keyword = $event"
          @reload="loadProducts"
          @select-product="loadVariants"
          @clear-product="selectedProduct = null; variants = []"
          @add-to-cart="(p, v) => addToCart(p, v)"
        />
      </div>

      <div class="col-12 col-xl-7">
        <PosCartPanel
          :cart="cart"
          :customer-name="customerName"
          :customer-phone="customerPhone"
          :voucher-code="voucherCode"
          :applied-voucher="appliedVoucher"
          :voucher-message="voucherMessage"
          :suggested-vouchers="suggestedVouchers"
          :loading-vouchers="isLoadingVouchers"
          :payment-method="paymentMethod"
          :cash-received="cashReceived"
          :total-amount="totalAmount"
          :discount-amount="discountAmount"
          :total-payable="totalPayable"
          :change-amount="changeAmount"
          :viet-qr-url="vietQrUrl"
          :transfer-note="noiDungChuyenKhoan"
          :pending-count="danhSachHoaDonCho.length"
          :saving-pending="dangLuuTam"
          :submitting="isSubmitting"
          :format-money="formatMoney"
          :discount-label="voucherDiscountLabel"
          @update:customerName="customerName = $event"
          @update:customerPhone="customerPhone = $event"
          @update:voucherCode="voucherCode = $event"
          @update:paymentMethod="paymentMethod = $event"
          @update:cashReceived="cashReceived = $event"
          @open-pending="moModalHoaDonCho"
          @save-pending="luuHoaDonCho"
          @normalize-qty="normalizeQuantity"
          @remove-item="removeItem"
          @reload-vouchers="loadVoucherSuggestions"
          @apply-voucher="applyVoucher"
          @remove-voucher="removeVoucher"
          @apply-suggested="applySuggestedVoucher"
          @checkout="checkout"
        />
      </div>
    </div>

    <PosPendingModal
      :show="showHoaDonChoModal"
      :list="danhSachHoaDonCho"
      :loading="dangTaiHoaDonCho"
      :cart-has-items="cart.length > 0"
      :format-money="formatMoney"
      @close="showHoaDonChoModal = false"
      @restore="goiLaiHoaDonCho"
      @remove="xoaHoaDonCho"
    />

    <PosInvoiceModal
      :show="showInvoice"
      :data="invoiceData"
      :format-money="formatMoney"
      @close="showInvoice = false"
      @print="printInvoice"
    />

    <!--
      Modal nhập ghi chú khi Lưu tạm hóa đơn chờ
      (thay cho prompt() xấu của trình duyệt)
    -->
    <template v-if="showNoteModal">
      <div class="modal fade show d-block" tabindex="-1" role="dialog" aria-modal="true">
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content border-0 shadow">
            <div class="modal-header border-0 pb-0">
              <h5 class="modal-title fw-bold">Lưu hóa đơn chờ</h5>
              <button type="button" class="btn-close" @click="showNoteModal = false"></button>
            </div>
            <div class="modal-body">
              <p class="text-secondary small mb-3">
                Giỏ hàng sẽ được cất tạm để phục vụ khách khác. Bạn có thể thêm ghi chú (không bắt buộc).
              </p>
              <label class="form-label small fw-semibold">Ghi chú</label>
              <input
                v-model.trim="pendingNote"
                type="text"
                class="form-control"
                placeholder='Ví dụ: khách đi thử size, chờ thanh toán...'
                @keyup.enter="xacNhanLuuHoaDonCho"
              />
            </div>
            <div class="modal-footer border-0 pt-0">
              <button type="button" class="btn btn-outline-secondary" @click="showNoteModal = false">
                Hủy
              </button>
              <button
                type="button"
                class="btn btn-primary"
                :disabled="dangLuuTam"
                @click="xacNhanLuuHoaDonCho"
              >
                {{ dangLuuTam ? 'Đang lưu...' : 'Lưu tạm' }}
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-backdrop fade show"></div>
    </template>

    <!--
      Modal xác nhận (Gọi lại / Xóa hóa đơn chờ)
      thay cho confirm() của trình duyệt
    -->
    <template v-if="showConfirmModal">
      <div class="modal fade show d-block" tabindex="-1" role="dialog" aria-modal="true">
        <div class="modal-dialog modal-dialog-centered modal-sm">
          <div class="modal-content border-0 shadow">
            <div class="modal-body text-center py-4 px-3">
              <div
                class="rounded-circle d-inline-flex align-items-center justify-content-center mb-3"
                :class="confirmTone === 'danger' ? 'bg-danger-subtle text-danger' : 'bg-warning-subtle text-warning'"
                style="width: 48px; height: 48px; font-size: 22px"
              >
                !
              </div>
              <h6 class="fw-bold mb-2">{{ confirmTitle }}</h6>
              <p class="text-secondary small mb-0">{{ confirmMessage }}</p>
            </div>
            <div class="modal-footer border-0 justify-content-center gap-2 pb-4">
              <button type="button" class="btn btn-outline-secondary btn-sm px-3" @click="huyXacNhan">
                Hủy
              </button>
              <button
                type="button"
                class="btn btn-sm px-3"
                :class="confirmTone === 'danger' ? 'btn-danger' : 'btn-warning'"
                @click="dongYXacNhan"
              >
                Đồng ý
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-backdrop fade show"></div>
    </template>

    <!-- Toast thông báo đẹp (thay alert) — góc phải dưới -->
    <div class="pos-toast-wrap">
      <transition name="pos-toast">
        <div
          v-if="toast.visible"
          class="pos-toast shadow"
          :class="'pos-toast-' + toast.type"
          role="status"
        >
          <div class="pos-toast-icon">{{ toastIcon }}</div>
          <div class="pos-toast-body">
            <div class="pos-toast-title">{{ toast.title }}</div>
            <div class="pos-toast-msg">{{ toast.message }}</div>
          </div>
          <button type="button" class="btn-close btn-close-white ms-2" @click="toast.visible = false"></button>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
// Trang "Bán hàng tại quầy" (POS) — dùng chung được bởi ADMIN và EMPLOYEE.
// File này gọi API bằng fetch() (thay vì axios), nên PHẢI tự gắn header xác
// thực thủ công vào từng lệnh fetch bằng getAuthHeaders() — khác với các
// trang khác dùng axios có thể tự động gắn qua interceptor.
import { computed, onMounted, ref, watch } from 'vue'
import { getAuthHeaders } from '@/utils/adminAuth'
import PosProductPanel from './components/PosProductPanel.vue'
import PosCartPanel from './components/PosCartPanel.vue'
import PosPendingModal from './components/PosPendingModal.vue'
import PosInvoiceModal from './components/PosInvoiceModal.vue'

const API_BASE = 'http://localhost:8080/api'

const products = ref([])
const variants = ref([])
const selectedProduct = ref(null)

const keyword = ref('')
const cart = ref([])

const customerName = ref('')
const customerPhone = ref('')

const voucherCode = ref('')
const appliedVoucher = ref(null)
const voucherMessage = ref('')
const vouchers = ref([])
const isLoadingVouchers = ref(false)

const paymentMethod = ref('TIEN_MAT')
const cashReceived = ref(0)

// ===================== QR THANH TOÁN (VietQR) =====================
// TRƯỚC ĐÂY: chọn "Chuyển khoản" không hiển thị mã QR nào cả - thu ngân
// phải tự đọc số tài khoản cho khách chuyển thủ công, dễ nhầm số tiền.
//
// Dùng dịch vụ công khai VietQR.io để tạo ảnh QR động theo ĐÚNG số tiền cần
// thu của đơn hiện tại - không cần gọi API riêng, không cần đăng ký API key,
// chỉ cần build 1 URL ảnh theo chuẩn của họ. Tài liệu:
// https://www.vietqr.io/danh-sach-api/link-tao-ma-nhanh/
//
// ⚠️ THAY 3 GIÁ TRỊ DƯỚI ĐÂY BẰNG THÔNG TIN TÀI KHOẢN NGÂN HÀNG THẬT CỦA CỬA
// HÀNG trước khi dùng trong thực tế (hiện đang là giá trị mẫu để demo):
const BANK_CODE = 'MB' // Mã ngân hàng viết tắt theo chuẩn VietQR (VD: VCB, TCB, MB, ACB, BIDV...)
const BANK_ACCOUNT_NO = '0563663591' // Số tài khoản nhận tiền của cửa hàng
const BANK_ACCOUNT_NAME = 'Phan The Bac' // Tên chủ tài khoản, KHÔNG dấu

// Nội dung chuyển khoản: giúp đối soát - nhìn vào lịch sử biến động số dư,
// nhân viên biết ngay giao dịch nào tương ứng với khách nào.
const noiDungChuyenKhoan = computed(() => {
  const soDienThoai = customerPhone.value?.trim()
  return soDienThoai ? `TrendFit ${soDienThoai}` : 'Thanh toan TrendFit'
})

// URL ảnh QR, tự cập nhật lại mỗi khi tổng tiền cần trả thay đổi (nhờ computed)
const vietQrUrl = computed(() => {
  const amount = Math.max(0, Math.round(totalPayable.value || 0))
  const noiDung = encodeURIComponent(noiDungChuyenKhoan.value)
  const ten = encodeURIComponent(BANK_ACCOUNT_NAME)
  return `https://img.vietqr.io/image/${BANK_CODE}-${BANK_ACCOUNT_NO}-compact2.png?amount=${amount}&addInfo=${noiDung}&accountName=${ten}`
})
// =====================================================================

const isSubmitting = ref(false)

const showInvoice = ref(false)
const invoiceData = ref(null)

const filteredProducts = computed(() => {
  const key = keyword.value.trim().toLowerCase()

  if (!key) {
    return products.value
  }

  return products.value.filter((product) => product.ten?.toLowerCase().includes(key))
})

const totalAmount = computed(() => {
  return cart.value.reduce((sum, item) => {
    return sum + Number(item.gia || 0) * Number(item.quantity || 0)
  }, 0)
})

const discountAmount = computed(() => {
  if (!appliedVoucher.value) {
    return 0
  }

  if (appliedVoucher.value.tienGiam !== null && appliedVoucher.value.tienGiam !== undefined) {
    return Math.min(Number(appliedVoucher.value.tienGiam || 0), totalAmount.value)
  }

  const voucher = appliedVoucher.value
  const type = String(voucher.loai || '').toUpperCase()
  const value = Number(voucher.giaTriGiam || 0)

  let discount = 0

  if (type === 'PERCENT' || type === 'PHAN_TRAM' || type === '%') {
    discount = (totalAmount.value * value) / 100

    if (voucher.giaTriToiDa !== null && voucher.giaTriToiDa !== undefined) {
      discount = Math.min(discount, Number(voucher.giaTriToiDa))
    }
  } else {
    discount = value
  }

  return Math.min(discount, totalAmount.value)
})

const totalPayable = computed(() => {
  return Math.max(totalAmount.value - discountAmount.value, 0)
})

const changeAmount = computed(() => {
  if (paymentMethod.value !== 'TIEN_MAT') {
    return 0
  }

  return Math.max(Number(cashReceived.value || 0) - totalPayable.value, 0)
})

const suggestedVouchers = computed(() => {
  if (!cart.value.length) {
    return []
  }

  return vouchers.value
    .map((voucher) => evaluateVoucher(voucher))
    .filter((voucher) => voucher.isVisible)
    .sort((first, second) => {
      if (first.eligible !== second.eligible) {
        return first.eligible ? -1 : 1
      }

      if (first.eligible && second.eligible) {
        return second.estimatedDiscount - first.estimatedDiscount
      }

      return first.missingAmount - second.missingAmount
    })
    .slice(0, 6)
})

watch(totalAmount, () => {
  if (appliedVoucher.value) {
    validateAppliedVoucherAgain()
  }
})


function extractArray(payload) {
  if (Array.isArray(payload)) return payload
  if (Array.isArray(payload?.content)) return payload.content
  if (Array.isArray(payload?.data)) return payload.data
  if (Array.isArray(payload?.items)) return payload.items
  return []
}

function normalizeProduct(item) {
  const source = item?.sanPham ?? item?.product ?? item ?? {}

  return {
    ...source,
    id: source.id ?? source.sanPhamId ?? source.maSanPham ?? item?.id ?? item?.sanPhamId ?? null,
    ten: source.ten ?? source.tenSanPham ?? source.name ?? 'Sản phẩm chưa có tên',
    danhMuc: source.danhMuc ?? source.category ?? null,
    _variants: item?.bienTheSanPhams ?? item?.variants ?? null,
  }
}

async function loadProducts() {
  try {
    const response = await fetch(`${API_BASE}/admin/products`, {
      headers: getAuthHeaders(),
    })

    if (!response.ok) {
      const message = await response.text()
      throw new Error(message || `Không tải được sản phẩm (${response.status})`)
    }

    const payload = await response.json()
    const rawProducts = extractArray(payload)

    products.value = rawProducts.map(normalizeProduct)

    console.log('Dữ liệu sản phẩm POS:', products.value)
  } catch (error) {
    console.error('Lỗi tải sản phẩm POS:', error)
    products.value = []
    alert(error.message || 'Không tải được danh sách sản phẩm')
  }
}

async function loadVariants(product) {
  const productId = product?.id ?? product?.sanPhamId ?? product?.maSanPham ?? null

  if (!productId) {
    console.error('Sản phẩm không có ID:', product)
    alert('Sản phẩm không có ID nên chưa thể tải biến thể')
    return
  }

  try {
    selectedProduct.value = product
    variants.value = []

    if (Array.isArray(product._variants)) {
      variants.value = product._variants
      return
    }

    const response = await fetch(`${API_BASE}/admin/products/${productId}/variants`, {
      headers: getAuthHeaders(),
    })

    if (!response.ok) {
      const message = await response.text()
      throw new Error(message || `Không tải được biến thể (${response.status})`)
    }

    const payload = await response.json()
    variants.value = extractArray(payload)
  } catch (error) {
    console.error('Lỗi tải biến thể POS:', error)
    variants.value = []
    alert(error.message || 'Không tải được biến thể sản phẩm')
  }
}

// const filteredProducts = computed(() => {
//   if (!products.value) return []
//   return products.value.filter((p) => p.ten?.toLowerCase().includes(keyword.value.toLowerCase()))
// })

function addToCart(product, variant) {
  const existed = cart.value.find((i) => i.bienTheId === variant.id)
  if (existed) {
    if (existed.quantity < variant.soLuongTon) {
      existed.quantity += 1
    }

    return
  }

  cart.value.push({
    bienTheId: variant.id,
    ten: product.ten,
    // Mã SKU của biến thể - TRƯỚC ĐÂY bị bỏ sót khi thêm vào giỏ, khiến hóa
    // đơn in ra không thể hiển thị mã sản phẩm dù dữ liệu này đã có sẵn ở
    // bước chọn biến thể (xem cột "SKU: ..." lúc chọn size/màu).
    maSku: variant.maSku || '',
    tenKichCo: variant.kichCo?.tenKichCo || 'N/A',
    tenMau: variant.mauSac?.tenMau || 'N/A',
    soLuongTon: variant.soLuongTon,
    quantity: 1,
    gia: getPrice(variant),
  })
}

function normalizeQuantity(item) {
  if (!item.quantity || item.quantity < 1) {
    item.quantity = 1
  }

  if (item.quantity > item.soLuongTon) {
    item.quantity = item.soLuongTon
  }
}

function removeItem(bienTheId) {
  cart.value = cart.value.filter((item) => item.bienTheId !== bienTheId)

  if (!cart.value.length) {
    removeVoucher()
    cashReceived.value = 0
  }
}

async function loadVoucherSuggestions() {
  try {
    isLoadingVouchers.value = true

    const response = await fetch(`${API_BASE}/admin/vouchers`, {
      headers: getAuthHeaders(),
    })

    if (!response.ok) {
      const message = await response.text()
      throw new Error(message || `Không tải được voucher (${response.status})`)
    }

    const payload = await response.json()
    vouchers.value = extractArray(payload).map(normalizeVoucher)
  } catch (error) {
    console.error('Lỗi tải voucher gợi ý:', error)
    vouchers.value = []
  } finally {
    isLoadingVouchers.value = false
  }
}

function normalizeVoucher(voucher = {}) {
  return {
    ...voucher,
    id: voucher.id ?? null,
    ma: String(voucher.ma || '')
      .trim()
      .toUpperCase(),
    ten: voucher.ten || '',
    loai: String(voucher.loai || '')
      .trim()
      .toUpperCase(),
    giaTriGiam: Number(voucher.giaTriGiam || 0),
    giaTriToiDa:
      voucher.giaTriToiDa === null || voucher.giaTriToiDa === undefined
        ? null
        : Number(voucher.giaTriToiDa),
    donHangToiThieu: Number(voucher.donHangToiThieu || 0),
    gioiHanSuDung:
      voucher.gioiHanSuDung === null || voucher.gioiHanSuDung === undefined
        ? null
        : Number(voucher.gioiHanSuDung),
    soLanDaDung: Number(voucher.soLanDaDung || 0),
    dangHoatDong: voucher.dangHoatDong !== false,
  }
}

function evaluateVoucher(rawVoucher) {
  const voucher = normalizeVoucher(rawVoucher)
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  const startDate = voucher.ngayBatDau ? new Date(`${voucher.ngayBatDau}T00:00:00`) : null
  const endDate = voucher.ngayKetThuc ? new Date(`${voucher.ngayKetThuc}T23:59:59`) : null

  const remainingUses =
    voucher.gioiHanSuDung === null ? null : Math.max(voucher.gioiHanSuDung - voucher.soLanDaDung, 0)

  const minimumOrder = Number(voucher.donHangToiThieu || 0)
  const missingAmount = Math.max(minimumOrder - totalAmount.value, 0)

  let reason = ''
  let helpText = ''

  if (!voucher.ma) {
    reason = 'Thiếu mã'
  } else if (!voucher.dangHoatDong) {
    reason = 'Đã khóa'
  } else if (startDate && today < startDate) {
    reason = 'Chưa bắt đầu'
  } else if (endDate && today > endDate) {
    reason = 'Hết hạn'
  } else if (remainingUses !== null && remainingUses <= 0) {
    reason = 'Hết lượt'
  } else if (missingAmount > 0) {
    reason = `Thiếu ${formatMoney(missingAmount)}`
    helpText = `Thêm ${formatMoney(missingAmount)} để dùng mã này`
  }

  const eligible = !reason

  return {
    ...voucher,
    minimumOrder,
    missingAmount,
    remainingUses,
    eligible,
    reason,
    helpText,
    estimatedDiscount: eligible ? estimateVoucherDiscount(voucher, totalAmount.value) : 0,
    isVisible:
      Boolean(voucher.ma) &&
      voucher.dangHoatDong &&
      (!endDate || today <= endDate) &&
      (remainingUses === null || remainingUses > 0),
  }
}

function estimateVoucherDiscount(voucher, orderTotal) {
  const total = Number(orderTotal || 0)
  const type = String(voucher.loai || '').toUpperCase()
  const value = Number(voucher.giaTriGiam || 0)

  let discount = 0

  if (type === 'PERCENT' || type === 'PHAN_TRAM' || type === '%') {
    discount = (total * value) / 100

    if (voucher.giaTriToiDa !== null && voucher.giaTriToiDa !== undefined) {
      discount = Math.min(discount, Number(voucher.giaTriToiDa))
    }
  } else {
    discount = value
  }

  return Math.max(Math.min(discount, total), 0)
}

function voucherDiscountLabel(voucher) {
  const type = String(voucher.loai || '').toUpperCase()
  const value = Number(voucher.giaTriGiam || 0)

  if (type === 'PERCENT' || type === 'PHAN_TRAM' || type === '%') {
    const maximum =
      voucher.giaTriToiDa !== null && voucher.giaTriToiDa !== undefined
        ? `, tối đa ${formatMoney(voucher.giaTriToiDa)}`
        : ''

    return `Giảm ${value}%${maximum}`
  }

  return `Giảm ${formatMoney(value)}`
}

function formatVoucherDate(value) {
  if (!value) return ''

  const date = new Date(`${value}T00:00:00`)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return date.toLocaleDateString('vi-VN')
}

async function requestVoucherCheck() {
  const requestOptions = {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
    },
    body: JSON.stringify({
      ma: voucherCode.value.trim().toUpperCase(),
      tongDon: totalAmount.value,
    }),
  }

  let response = await fetch(`${API_BASE}/admin/vouchers/check`, requestOptions)

  // Repo gốc đặt API kiểm tra voucher ở /api/public/vouchers/check.
  // Fallback này giúp trang POS chạy được với cả hai cấu hình backend.
  // Đồng thời "/api/admin/vouchers" chỉ dành cho ADMIN (xem AuthInterceptor),
  // nên khi người đang bán hàng là NHÂN VIÊN (EMPLOYEE) sẽ bị từ chối
  // (401/403) — lúc đó cũng chuyển sang endpoint public để họ vẫn áp được
  // mã giảm giá khi thu ngân.
  if ([404, 405, 401, 403].includes(response.status)) {
    response = await fetch(`${API_BASE}/public/vouchers/check`, requestOptions)
  }

  return response
}

async function applyVoucher() {
  voucherMessage.value = ''

  if (!cart.value.length) {
    voucherMessage.value = 'Vui lòng thêm sản phẩm trước khi áp dụng mã'
    return
  }

  if (!voucherCode.value.trim()) {
    voucherMessage.value = 'Vui lòng nhập hoặc chọn một mã giảm giá'
    return
  }

  try {
    const response = await requestVoucherCheck()

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(errorText || 'Mã giảm giá không hợp lệ')
    }

    const voucher = normalizeVoucher(await response.json())

    appliedVoucher.value = voucher
    voucherCode.value = voucher.ma
    voucherMessage.value = `Đã áp dụng mã ${voucher.ma}, giảm ${formatMoney(estimateVoucherDiscount(voucher, totalAmount.value))}`
  } catch (error) {
    appliedVoucher.value = null
    voucherMessage.value = cleanVoucherError(error.message) || 'Không áp dụng được mã giảm giá'
  }
}

async function applySuggestedVoucher(voucher) {
  if (!voucher?.eligible) {
    return
  }

  voucherCode.value = voucher.ma
  await applyVoucher()
}

function cleanVoucherError(message) {
  if (!message) return ''

  try {
    const parsed = JSON.parse(message)
    return parsed.message || parsed.error || message
  } catch {
    return String(message).replace(/^"|"$/g, '')
  }
}

function removeVoucher() {
  appliedVoucher.value = null
  voucherCode.value = ''
  voucherMessage.value = ''
}

function validateAppliedVoucherAgain() {
  const minOrder = Number(appliedVoucher.value?.donHangToiThieu || 0)

  if (totalAmount.value < minOrder) {
    voucherMessage.value = 'Mã giảm giá đã bị hủy vì đơn hàng không còn đạt giá trị tối thiểu'
    appliedVoucher.value = null
    voucherCode.value = ''
    return
  }

  voucherMessage.value = `Đang áp dụng mã ${appliedVoucher.value.ma}, giảm ${formatMoney(discountAmount.value)}`
}

// const totalAmount = computed(() => cart.value.reduce((sum, i) => sum + i.gia * i.quantity, 0))

// ===================== HÓA ĐƠN CHỜ (đơn tạm / treo giỏ hàng) =====================
// Cho phép thu ngân "cất" giỏ hàng đang dở dang sang một chỗ để phục vụ ngay
// khách tiếp theo, rồi gọi lại đúng đơn đó sau. Xem chú thích chi tiết ở
// backend: HoaDonCho.java (entity) + HoaDonChoService.java.
const showHoaDonChoModal = ref(false)
const danhSachHoaDonCho = ref([])
const dangTaiHoaDonCho = ref(false)
const dangLuuTam = ref(false)

// Tải danh sách hóa đơn chờ hiện có (gọi ngay lúc mở trang để biết có bao
// nhiêu đơn đang treo, và mỗi lần mở modal để cập nhật mới nhất).
async function taiDanhSachHoaDonCho() {
  try {
    const res = await fetch(`${API_BASE}/admin/hoa-don-cho`, {
      headers: getAuthHeaders(),
    })
    if (!res.ok) throw new Error(await res.text())
    danhSachHoaDonCho.value = await res.json()
  } catch (err) {
    console.error('Lỗi tải danh sách hóa đơn chờ:', err)
  }
}

function moModalHoaDonCho() {
  showHoaDonChoModal.value = true
  dangTaiHoaDonCho.value = true
  taiDanhSachHoaDonCho().finally(() => {
    dangTaiHoaDonCho.value = false
  })
}

// ===================== UI thông báo đẹp (toast + modal) =====================
// Thay alert/prompt/confirm của trình duyệt — xấu và không đồng bộ với admin UI.
const toast = ref({
  visible: false,
  type: 'success', // success | error | warning | info
  title: '',
  message: '',
})
let toastTimer = null

const toastIcon = computed(() => {
  if (toast.value.type === 'success') return '✓'
  if (toast.value.type === 'error') return '!'
  if (toast.value.type === 'warning') return '!'
  return 'i'
})

function showToast(type, title, message, duration = 3200) {
  if (toastTimer) clearTimeout(toastTimer)
  toast.value = { visible: true, type, title, message }
  toastTimer = setTimeout(() => {
    toast.value.visible = false
  }, duration)
}

// Modal ghi chú khi lưu tạm
const showNoteModal = ref(false)
const pendingNote = ref('')

// Modal xác nhận gọi lại / xóa
const showConfirmModal = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const confirmTone = ref('warning') // warning | danger
const confirmAction = ref(null) // callback khi bấm Đồng ý

function moXacNhan({ title, message, tone = 'warning', onConfirm }) {
  confirmTitle.value = title
  confirmMessage.value = message
  confirmTone.value = tone
  confirmAction.value = onConfirm
  showConfirmModal.value = true
}

function huyXacNhan() {
  showConfirmModal.value = false
  confirmAction.value = null
}

async function dongYXacNhan() {
  const fn = confirmAction.value
  showConfirmModal.value = false
  confirmAction.value = null
  if (typeof fn === 'function') await fn()
}

// Mở modal ghi chú thay vì prompt()
function luuHoaDonCho() {
  if (!cart.value.length) {
    showToast('warning', 'Giỏ hàng trống', 'Chưa có sản phẩm nào để lưu tạm.')
    return
  }
  pendingNote.value = ''
  showNoteModal.value = true
}

// Xác nhận lưu tạm sau khi nhập ghi chú (hoặc bỏ trống)
async function xacNhanLuuHoaDonCho() {
  dangLuuTam.value = true
  try {
    const payload = {
      tenKhachHang: customerName.value,
      soDienThoai: customerPhone.value,
      phuongThucThanhToan: paymentMethod.value,
      maVoucher: appliedVoucher.value?.ma || null,
      voucherId: appliedVoucher.value?.id || null,
      ghiChu: pendingNote.value || '',
      items: cart.value.map((i) => ({
        bienTheId: Number(i.bienTheId),
        ten: i.ten,
        maSku: i.maSku || '',
        tenKichCo: i.tenKichCo,
        tenMau: i.tenMau,
        quantity: Number(i.quantity),
        gia: Number(i.gia),
        soLuongTon: Number(i.soLuongTon),
      })),
    }

    const res = await fetch(`${API_BASE}/admin/hoa-don-cho`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeaders(),
      },
      body: JSON.stringify(payload),
    })

    if (!res.ok) throw new Error(await res.text())

    // Dọn trống giỏ + khách + voucher
    cart.value = []
    customerName.value = ''
    customerPhone.value = ''
    appliedVoucher.value = null
    voucherCode.value = ''
    voucherMessage.value = ''

    showNoteModal.value = false
    taiDanhSachHoaDonCho()
    showToast('success', 'Đã lưu hóa đơn chờ', 'Giỏ hàng đã được dọn trống, có thể phục vụ khách tiếp theo.')
  } catch (err) {
    showToast('error', 'Lưu tạm thất bại', err.message || 'Không thể lưu hóa đơn chờ.')
  } finally {
    dangLuuTam.value = false
  }
}

// Gọi lại 1 hóa đơn chờ vào giỏ hiện tại
function goiLaiHoaDonCho(id) {
  const run = async () => {
    try {
      const res = await fetch(`${API_BASE}/admin/hoa-don-cho/${id}`, {
        headers: getAuthHeaders(),
      })
      if (!res.ok) throw new Error(await res.text())
      const chiTiet = await res.json()

      customerName.value = chiTiet.tenKhachHang || ''
      customerPhone.value = chiTiet.soDienThoai || ''
      paymentMethod.value = chiTiet.phuongThucThanhToan || 'TIEN_MAT'
      cart.value = (chiTiet.items || []).map((i) => ({
        bienTheId: i.bienTheId,
        ten: i.ten,
        maSku: i.maSku || '',
        tenKichCo: i.tenKichCo,
        tenMau: i.tenMau,
        quantity: i.quantity,
        gia: i.gia,
        soLuongTon: i.soLuongTon,
      }))

      if (chiTiet.maVoucher) {
        // Không tự áp voucher — yêu cầu bấm "Áp dụng" lại để kiểm tra còn hiệu lực
        voucherCode.value = chiTiet.maVoucher
        voucherMessage.value =
          'Vui lòng bấm "Áp dụng" lại để kiểm tra mã giảm giá này còn hiệu lực không.'
      }

      await fetch(`${API_BASE}/admin/hoa-don-cho/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
      })

      showHoaDonChoModal.value = false
      taiDanhSachHoaDonCho()
      showToast('success', 'Đã gọi lại hóa đơn', 'Giỏ hàng đã được khôi phục, tiếp tục thanh toán.')
    } catch (err) {
      showToast('error', 'Gọi lại thất bại', err.message || 'Không thể gọi lại hóa đơn chờ.')
    }
  }

  // Nếu giỏ đang có hàng → hỏi xác nhận trước khi thay thế
  if (cart.value.length > 0) {
    moXacNhan({
      title: 'Thay thế giỏ hàng?',
      message: 'Giỏ hàng hiện tại sẽ bị thay thế hoàn toàn bởi hóa đơn chờ này.',
      tone: 'warning',
      onConfirm: run,
    })
    return
  }
  run()
}

// Xóa hẳn 1 hóa đơn chờ
function xoaHoaDonCho(id) {
  moXacNhan({
    title: 'Xóa hóa đơn chờ?',
    message: 'Thao tác này không thể hoàn tác. Bạn chắc chắn muốn xóa?',
    tone: 'danger',
    onConfirm: async () => {
      try {
        const res = await fetch(`${API_BASE}/admin/hoa-don-cho/${id}`, {
          method: 'DELETE',
          headers: getAuthHeaders(),
        })
        if (!res.ok) throw new Error(await res.text())
        taiDanhSachHoaDonCho()
        showToast('success', 'Đã xóa', 'Hóa đơn chờ đã được xóa khỏi danh sách.')
      } catch (err) {
        showToast('error', 'Xóa thất bại', err.message || 'Không thể xóa hóa đơn chờ.')
      }
    },
  })
}
// ===================================================================================

async function checkout() {
  if (!cart.value.length) return alert('Giỏ hàng trống')

  const resetModal = () => {
    selectedProduct.value = null
    variants.value = []
  }

  const payload = {
    hoTen: customerName.value || 'Khách lẻ',
    sdt: customerPhone.value || '0000000000',
    diaChi: 'Bán tại quầy',
    phuongThucThanhToan: paymentMethod.value || 'TIEN_MAT',
    // Đảm bảo ép kiểu Number cho các trường BigDecimal
    tongTienHang: Number(totalAmount.value),
    // Bán tại quầy (offline): khách nhận hàng ngay tại chỗ, KHÔNG phát sinh
    // phí vận chuyển. Backend cũng tự ép cứng về 0 (xem
    // OrderService.taoDonHangTaiQuay) - gửi tường minh ở đây để code rõ ràng,
    // tránh gây hiểu nhầm là bị bỏ sót.
    phiVanChuyen: 0,
    tienGiam: Number(discountAmount.value || 0),
    tongThanhToan: Number(totalPayable.value),
    tienKhachDua: Number(cashReceived.value || 0),
    tienThua: Number(changeAmount.value || 0),
    voucherId: appliedVoucher.value?.id || null,
    maVoucher: appliedVoucher.value?.ma || null,
    userId: null,
    // TRƯỚC ĐÂY: hardcode cứng creatorId = 1, khiến MỌI đơn bán tại quầy đều
    // bị gán cho đúng 1 tài khoản bất kể ai thực sự đang đăng nhập thao tác
    // - sai lệch hoàn toàn việc truy vết "ai đã bán đơn này". Giờ lấy đúng
    // từ tài khoản đang đăng nhập (được lưu lúc login, xem LoginView.vue).
    creatorId: Number(localStorage.getItem('user_id')) || null,
    items: cart.value.map((i) => ({
      bienTheId: Number(i.bienTheId),
      quantity: Number(i.quantity),
      ten: i.ten,
      gia: Number(i.gia),
    })),
  }

  try {
    // Lấy role và user_id từ localStorage để gửi kèm request
    const userRole = localStorage.getItem('role') || localStorage.getItem('vaiTro') || 'ADMIN'
    const userId = localStorage.getItem('user_id') || 1

    payload.creatorId = Number(userId)

    const res = await fetch(`${API_BASE}/admin/pos-orders`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Role': userRole, // Sử dụng biến userRole đã định nghĩa
        ...getAuthHeaders(),
      },
      body: JSON.stringify(payload),
    })

    if (!res.ok) {
      const errorText = await res.text()
      throw new Error(errorText || 'Thanh toán thất bại')
    }

    const createdOrder = await res.json()

    invoiceData.value = {
      code: createdOrder.id || 'POS',
      date: new Date().toLocaleString('vi-VN'),
      customer: customerName.value || 'Khách lẻ',
      phone: customerPhone.value || '',
      paymentMethod: paymentMethod.value === 'TIEN_MAT' ? 'Tiền mặt' : 'Chuyển khoản',

      items: cart.value.map((item) => ({
        name: item.ten,
        maSku: item.maSku || '',
        tenKichCo: item.tenKichCo,
        tenMau: item.tenMau,
        qty: item.quantity,
        price: item.gia,
        total: Number(item.gia || 0) * Number(item.quantity || 0),
      })),

      totalAmount: totalAmount.value,
      discount: discountAmount.value,
      payable: totalPayable.value,
      paid:
        paymentMethod.value === 'TIEN_MAT' ? Number(cashReceived.value || 0) : totalPayable.value,
      change: paymentMethod.value === 'TIEN_MAT' ? changeAmount.value : 0,
    }

    showInvoice.value = true

    cart.value = []
    appliedVoucher.value = null
    voucherCode.value = ''
    cashReceived.value = 0
  } catch (err) {
    console.error(err)
    alert('Không thể kết nối tới server')
  }
}

function formatMoney(v) {
  return Number(v).toLocaleString('vi-VN') + ' đ'
}

function getPrice(variant) {
  if (!variant) return 0
  return Number(variant.giaSale ?? variant.gia ?? 0)
}

function printInvoice() {
  if (!invoiceData.value) {
    alert('Không có thông tin hóa đơn để in!')
    return
  }

  const data = invoiceData.value

  const rows = data.items
    .map(
      (d, index) =>
        `<tr>
      <td style="text-align: center; padding: 8px; border-bottom: 1px solid #ddd;">${index + 1}</td>
      <td style="padding: 8px; border-bottom: 1px solid #ddd;">${d.maSku || '—'}</td>
      <td style="padding: 8px; border-bottom: 1px solid #ddd;">${d.name}</td>
      <td style="text-align: center; border-bottom: 1px solid #ddd;">${d.tenKichCo} / ${d.tenMau}</td>
      <td style="text-align: center; border-bottom: 1px solid #ddd;">${d.qty}</td>
      <td style="text-align: right; border-bottom: 1px solid #ddd;">${formatMoney(d.price)}</td>
      <td style="text-align: right; border-bottom: 1px solid #ddd;">${formatMoney(d.total)}</td>
    </tr>`,
    )
    .join('')

  const printContent = `
    <div style="font-family: Arial, sans-serif; padding: 20px; max-width: 800px; margin: auto;">
      <h1 style="text-align: center;">HÓA ĐƠN BÁN HÀNG</h1>
      <hr style="border: 0; border-top: 1px solid #ccc; margin: 15px 0;">
      
      <div style="display: flex; justify-content: space-between; margin-bottom: 20px;">
        <div>
          <p style="margin: 4px 0;"><strong>Khách hàng:</strong> ${data.customer}</p>
          <p style="margin: 4px 0;"><strong>SĐT:</strong> ${data.phone || 'Không có'}</p>
          <p style="margin: 4px 0;"><strong>Địa chỉ:</strong> Bán tại quầy</p>
        </div>
        <div>
          <p style="margin: 4px 0;"><strong>Mã đơn:</strong> #${data.code}</p>
          <p style="margin: 4px 0;"><strong>Ngày đặt:</strong> ${data.date}</p>
          <p style="margin: 4px 0;"><strong>Thanh toán:</strong> ${data.paymentMethod}</p>
        </div>
      </div>

      <table style="width: 100%; border-collapse: collapse; margin-top: 20px;">
        <thead style="background: #eee;">
          <tr>
            <th style="text-align: center; padding: 8px;">STT</th>
            <th style="text-align: left; padding: 8px;">Mã SP</th>
            <th style="text-align: left; padding: 8px;">Sản phẩm</th>
            <th style="text-align: center; padding: 8px;">Size/Màu</th>
            <th style="text-align: center; padding: 8px;">SL</th>
            <th style="text-align: right; padding: 8px;">Đơn giá</th>
            <th style="text-align: right; padding: 8px;">Thành tiền</th>
          </tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>

      <div style="margin-top: 20px; text-align: right; line-height: 1.6;">
        <p style="margin: 4px 0;">Tạm tính: ${formatMoney(data.totalAmount)}</p>
        <p style="margin: 4px 0;">Giảm giá: ${formatMoney(data.discount)}</p>
        <p style="margin: 4px 0;">Khách đưa: ${formatMoney(data.paid)}</p>
        <p style="margin: 4px 0;">Tiền thừa: ${formatMoney(data.change)}</p>
        <h2 style="color: red; margin: 10px 0;">Tổng thanh toán: ${formatMoney(data.payable)}</h2>
      </div>

      <p style="margin-top: 40px; text-align: center; font-style: italic; font-weight: bold; color: #4f46e5;">
        Cảm ơn quý khách đã mua hàng tại TrendFit!
      </p>
    </div>
  `

  const printWindow = window.open('', '_blank', 'width=900,height=650')
  if (printWindow) {
    printWindow.document.write(
      '<html><head><title>Hóa đơn #' +
        data.code +
        '</title></head><body>' +
        printContent +
        '</body></html>',
    )
    printWindow.document.close()
    printWindow.focus()

    // Kích hoạt lệnh in trực tiếp từ cửa sổ chính, tránh lỗi cú pháp script bên trong
    setTimeout(() => {
      printWindow.print()
      printWindow.close()
    }, 500)
  }
}

onMounted(() => {
  loadProducts()
  loadVoucherSuggestions()
  taiDanhSachHoaDonCho()
})
</script>

<style scoped>
.voucher-card {
  overflow: hidden;
}

.voucher-suggestion {
  padding: 13px 14px;
  border: 1px solid #dfe7f2;
  border-radius: 12px;
  background: #ffffff;
  transition:
    border-color 0.18s ease,
    box-shadow 0.18s ease,
    transform 0.18s ease;
}

.voucher-suggestion:hover {
  border-color: #c7d2fe;
  box-shadow: 0 8px 22px rgba(99, 102, 241, 0.08);
  transform: translateY(-1px);
}

.voucher-suggestion.is-eligible {
  border-color: #bbf7d0;
  background: linear-gradient(135deg, #ffffff, #f0fdf4);
}

.voucher-suggestion.is-applied {
  border-color: #818cf8;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
}

.voucher-code {
  display: inline-flex;
  padding: 4px 9px;
  border: 1px dashed #818cf8;
  border-radius: 8px;
  background: #eef2ff;
  color: #4f46e5;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.05em;
}

.min-w-0 {
  min-width: 0;
}

/* Toast góc phải dưới — thay alert() */
.pos-toast-wrap {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 1080;
  max-width: 380px;
}
.pos-toast {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 14px 16px;
  border-radius: 14px;
  color: #fff;
  backdrop-filter: blur(6px);
}
.pos-toast-success { background: linear-gradient(135deg, #059669, #10b981); }
.pos-toast-error { background: linear-gradient(135deg, #dc2626, #ef4444); }
.pos-toast-warning { background: linear-gradient(135deg, #d97706, #f59e0b); }
.pos-toast-info { background: linear-gradient(135deg, #2563eb, #3b82f6); }
.pos-toast-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  flex-shrink: 0;
}
.pos-toast-title { font-weight: 700; font-size: 14px; }
.pos-toast-msg { font-size: 13px; opacity: 0.95; margin-top: 2px; }
.pos-toast-enter-active,
.pos-toast-leave-active { transition: all 0.25s ease; }
.pos-toast-enter-from,
.pos-toast-leave-to {
  opacity: 0;
  transform: translateY(12px);
}
</style>

