<template>
  <div class="pos-page">
    <div class="page-header">
      <div>
        <h2>Bán hàng tại quầy</h2>
        <p>Tạo đơn trực tiếp cho khách mua tại cửa hàng</p>
      </div>

      <button class="btn-refresh" @click="loadProducts">
        Tải lại sản phẩm
      </button>
    </div>

    <div class="pos-grid">
      <!-- DANH SÁCH SẢN PHẨM -->
      <section class="product-panel">
        <div class="search-box">
          <input
            v-model="keyword"
            type="text"
            placeholder="Tìm sản phẩm theo tên..."
          />
        </div>

        <div class="product-list">
          <div
            v-for="product in filteredProducts"
            :key="product.id"
            class="product-card"
          >
            <div>
              <h4>{{ product.ten }}</h4>
              <p>{{ product.danhMuc?.ten || 'Chưa có danh mục' }}</p>
            </div>

            <button @click="loadVariants(product)">
              Chọn biến thể
            </button>
          </div>
        </div>
      </section>

      <!-- GIỎ HÀNG -->
      <section class="cart-panel">
        <h3>Giỏ hàng tại quầy</h3>

        <div class="customer-box">
          <input
            v-model="customerName"
            type="text"
            placeholder="Tên khách hàng, bỏ trống nếu khách lẻ"
          />

          <input
            v-model="customerPhone"
            type="text"
            placeholder="Số điện thoại"
          />
        </div>

        <!-- BIẾN THỂ -->
        <div v-if="selectedProduct" class="variant-box">
          <h4>{{ selectedProduct.ten }}</h4>

          <div
            v-for="variant in variants"
            :key="variant.id"
            class="variant-row"
          >
            <div>
              <b>{{ variant.kichCoSize }} - {{ variant.mauSac }}</b>
              <span>SKU: {{ variant.maSku }}</span>
              <span>Tồn: {{ variant.soLuongTon }}</span>
              <span>Giá: {{ formatMoney(getPrice(variant)) }}</span>
            </div>

            <button
              :disabled="variant.soLuongTon <= 0"
              @click="addToCart(selectedProduct, variant)"
            >
              Thêm
            </button>
          </div>
        </div>

        <!-- BẢNG GIỎ HÀNG -->
        <div v-if="cart.length" class="cart-table">
          <table>
            <thead>
              <tr>
                <th>Sản phẩm</th>
                <th>SL</th>
                <th>Đơn giá</th>
                <th>Thành tiền</th>
                <th></th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="item in cart" :key="item.bienTheId">
                <td>
                  {{ item.ten }}
                  <br />
                  <small>{{ item.kichCoSize }} - {{ item.mauSac }}</small>
                </td>

                <td>
                  <input
                    v-model.number="item.quantity"
                    type="number"
                    min="1"
                    :max="item.soLuongTon"
                    @change="normalizeQuantity(item)"
                  />
                </td>

                <td>{{ formatMoney(item.gia) }}</td>
                <td>{{ formatMoney(item.gia * item.quantity) }}</td>

                <td>
                  <button class="btn-remove" @click="removeItem(item.bienTheId)">
                    Xóa
                  </button>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- MÃ GIẢM GIÁ -->
          <div class="voucher-box">
            <label>Mã giảm giá</label>

            <div class="voucher-input">
              <input
                v-model="voucherCode"
                type="text"
                placeholder="Nhập mã giảm giá"
                :disabled="!!appliedVoucher"
              />

              <button v-if="!appliedVoucher" @click="applyVoucher">
                Áp dụng
              </button>

              <button v-else class="btn-cancel" @click="removeVoucher">
                Hủy mã
              </button>
            </div>

            <p v-if="voucherMessage" class="voucher-message">
              {{ voucherMessage }}
            </p>
          </div>

          <!-- THANH TOÁN -->
          <div class="payment-box">
            <label>Phương thức thanh toán</label>

            <div class="payment-options">
              <button
                :class="{ active: paymentMethod === 'TIEN_MAT' }"
                @click="paymentMethod = 'TIEN_MAT'"
              >
                Tiền mặt
              </button>

              <button
                :class="{ active: paymentMethod === 'CHUYEN_KHOAN' }"
                @click="paymentMethod = 'CHUYEN_KHOAN'"
              >
                Chuyển khoản
              </button>
            </div>

            <div v-if="paymentMethod === 'TIEN_MAT'" class="cash-box">
              <input
                v-model.number="cashReceived"
                type="number"
                min="0"
                placeholder="Tiền khách đưa"
              />

              <div class="quick-cash">
                <button @click="cashReceived = totalPayable">Vừa đủ</button>
                <button @click="cashReceived = 100000">100.000</button>
                <button @click="cashReceived = 200000">200.000</button>
                <button @click="cashReceived = 500000">500.000</button>
              </div>
            </div>
          </div>

          <!-- TỔNG TIỀN -->
          <div class="total-box">
            <div>
              <span>Tổng tiền hàng</span>
              <strong>{{ formatMoney(totalAmount) }}</strong>
            </div>

            <div>
              <span>Giảm giá</span>
              <strong class="discount">- {{ formatMoney(discountAmount) }}</strong>
            </div>

            <div>
              <span>Khách cần trả</span>
              <strong class="payable">{{ formatMoney(totalPayable) }}</strong>
            </div>

            <div v-if="paymentMethod === 'TIEN_MAT'">
              <span>Tiền khách đưa</span>
              <strong>{{ formatMoney(cashReceived) }}</strong>
            </div>

            <div v-if="paymentMethod === 'TIEN_MAT'">
              <span>Tiền thừa</span>
              <strong>{{ formatMoney(changeAmount) }}</strong>
            </div>
          </div>

          <button
            class="btn-checkout"
            :disabled="isSubmitting"
            @click="checkout"
          >
            {{ isSubmitting ? 'Đang thanh toán...' : 'Thanh toán' }}
          </button>
        </div>

        <div v-else class="empty-cart">
          Chưa có sản phẩm nào trong giỏ hàng.
        </div>
      </section>
    </div>
        <div v-if="showInvoice && invoiceData" class="invoice-bg">
      <div class="invoice-box">
        <div class="invoice-actions">
          <button @click="printInvoice">
            In hóa đơn
          </button>

          <button @click="showInvoice = false">
            Đóng
          </button>
        </div>

        <div class="invoice-paper">
          <h2>HÓA ĐƠN BÁN HÀNG</h2>

          <p>Mã hóa đơn: {{ invoiceData.code }}</p>
          <p>Ngày tạo: {{ invoiceData.date }}</p>
          <p>Khách hàng: {{ invoiceData.customer }}</p>
          <p>Số điện thoại: {{ invoiceData.phone }}</p>
          <p>Thanh toán: {{ invoiceData.paymentMethod }}</p>

          <table class="invoice-table">
            <thead>
              <tr>
                <th>Sản phẩm</th>
                <th>SL</th>
                <th>Giá</th>
                <th>Tổng</th>
              </tr>
            </thead>

            <tbody>
              <tr v-for="(item, index) in invoiceData.items" :key="index">
                <td>
                  {{ item.name }}
                  <br />
                  <small>{{ item.size }} - {{ item.color }}</small>
                </td>
                <td>{{ item.qty }}</td>
                <td>{{ formatMoney(item.price) }}</td>
                <td>{{ formatMoney(item.total) }}</td>
              </tr>
            </tbody>
          </table>

          <div class="invoice-summary">
            <p>Tổng tiền hàng: {{ formatMoney(invoiceData.totalAmount) }}</p>
            <p>Giảm giá: {{ formatMoney(invoiceData.discount) }}</p>
            <p>Khách cần trả: {{ formatMoney(invoiceData.payable) }}</p>
            <p>Khách đã đưa: {{ formatMoney(invoiceData.paid) }}</p>
            <p>Tiền thừa: {{ formatMoney(invoiceData.change) }}</p>
          </div>

          <p class="invoice-thanks">
            Cảm ơn quý khách đã mua hàng tại TrendFit!
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'

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

const paymentMethod = ref('TIEN_MAT')
const cashReceived = ref(0)

const isSubmitting = ref(false)

const showInvoice = ref(false)
const invoiceData = ref(null)

const filteredProducts = computed(() => {
  const key = keyword.value.trim().toLowerCase()

  if (!key) {
    return products.value
  }

  return products.value.filter((product) =>
    product.ten?.toLowerCase().includes(key)
  )
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

  if (
    appliedVoucher.value.tienGiam !== null &&
    appliedVoucher.value.tienGiam !== undefined
  ) {
    return Math.min(Number(appliedVoucher.value.tienGiam || 0), totalAmount.value)
  }

  const voucher = appliedVoucher.value
  const type = String(voucher.loai || '').toUpperCase()
  const value = Number(voucher.giaTriGiam || 0)

  let discount = 0

  if (type === 'PERCENT' || type === 'PHAN_TRAM' || type === '%') {
    discount = totalAmount.value * value / 100

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

watch(totalAmount, () => {
  if (appliedVoucher.value) {
    validateAppliedVoucherAgain()
  }
})

onMounted(() => {
  loadProducts()
})

async function loadProducts() {
  try {
    const response = await fetch(`${API_BASE}/admin/products`)
    products.value = await response.json()
  } catch (error) {
    alert('Không tải được danh sách sản phẩm')
  }
}

async function loadVariants(product) {
  try {
    selectedProduct.value = product

    const response = await fetch(`${API_BASE}/admin/products/${product.id}/variants`)
    variants.value = await response.json()
  } catch (error) {
    alert('Không tải được biến thể sản phẩm')
  }
}

function getPrice(variant) {
  return Number(variant.giaSale || variant.gia || 0)
}

function addToCart(product, variant) {
  const existed = cart.value.find((item) => item.bienTheId === variant.id)

  if (existed) {
    if (existed.quantity < variant.soLuongTon) {
      existed.quantity += 1
    }

    return
  }

  cart.value.push({
    bienTheId: variant.id,
    ten: product.ten,
    kichCoSize: variant.kichCoSize,
    mauSac: variant.mauSac,
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

async function applyVoucher() {
  voucherMessage.value = ''

  if (!cart.value.length) {
    voucherMessage.value = 'Vui lòng thêm sản phẩm trước khi áp dụng mã'
    return
  }

  if (!voucherCode.value.trim()) {
    voucherMessage.value = 'Vui lòng nhập mã giảm giá'
    return
  }

  try {
    const response = await fetch(`${API_BASE}/admin/vouchers/check`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        ma: voucherCode.value.trim(),
        tongDon: totalAmount.value,
      }),
    })

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(errorText || 'Mã giảm giá không hợp lệ')
    }

    const voucher = await response.json()

    appliedVoucher.value = voucher
    voucherCode.value = voucher.ma
    voucherMessage.value = `Đã áp dụng mã ${voucher.ma}`
  } catch (error) {
    appliedVoucher.value = null
    voucherMessage.value = error.message || 'Không áp dụng được mã giảm giá'
  }
}

function validateAppliedVoucherAgain() {
  const minOrder = Number(appliedVoucher.value?.donHangToiThieu || 0)

  if (totalAmount.value < minOrder) {
    voucherMessage.value = 'Mã giảm giá đã bị hủy vì đơn hàng không còn đạt giá trị tối thiểu'
    appliedVoucher.value = null
    voucherCode.value = ''
  }
}

async function checkout() {
  if (!cart.value.length) {
    alert('Vui lòng thêm sản phẩm vào giỏ hàng')
    return
  }

  const role = localStorage.getItem('user_role')

  if (role !== 'ADMIN' && role !== 'EMPLOYEE') {
    alert('Bạn không có quyền bán hàng tại quầy')
    return
  }

  if (
    paymentMethod.value === 'TIEN_MAT' &&
    Number(cashReceived.value || 0) < totalPayable.value
  ) {
    alert('Tiền khách đưa chưa đủ')
    return
  }

  const payload = {
    hoTen: customerName.value || 'Khách lẻ',
    sdt: customerPhone.value || '',
    diaChi: 'Bán tại quầy',
    phuongThucThanhToan: paymentMethod.value,

    tongTienHang: totalAmount.value,
    tienGiam: discountAmount.value,
    tongThanhToan: totalPayable.value,

    tienKhachDua:
      paymentMethod.value === 'TIEN_MAT'
        ? Number(cashReceived.value || 0)
        : totalPayable.value,

    tienThua:
      paymentMethod.value === 'TIEN_MAT'
        ? changeAmount.value
        : 0,

    voucherId: appliedVoucher.value?.id || null,
    maVoucher: appliedVoucher.value?.ma || null,

    items: cart.value.map((item) => ({
      bienTheId: item.bienTheId,
      quantity: item.quantity,
      ten: item.ten,
      gia: item.gia,
    })),
  }

  try {
    isSubmitting.value = true

    const response = await fetch(`${API_BASE}/admin/pos-orders`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Role': role,
      },
      body: JSON.stringify(payload),
    })

    if (!response.ok) {
  const errorText = await response.text()
  throw new Error(errorText || 'Thanh toán thất bại')
}

const createdOrder = await response.json()

invoiceData.value = {
  code: createdOrder.id || 'POS',
  date: new Date().toLocaleString('vi-VN'),
  customer: customerName.value || 'Khách lẻ',
  phone: customerPhone.value || '',
  paymentMethod: paymentMethod.value === 'TIEN_MAT' ? 'Tiền mặt' : 'Chuyển khoản',

  items: cart.value.map((item) => ({
    name: item.ten,
    size: item.kichCoSize,
    color: item.mauSac,
    qty: item.quantity,
    price: item.gia,
    total: Number(item.gia || 0) * Number(item.quantity || 0),
  })),

  totalAmount: totalAmount.value,
  discount: discountAmount.value,
  payable: totalPayable.value,
  paid: paymentMethod.value === 'TIEN_MAT'
    ? Number(cashReceived.value || 0)
    : totalPayable.value,
  change: paymentMethod.value === 'TIEN_MAT'
    ? changeAmount.value
    : 0,
}

showInvoice.value = true

alert('Thanh toán thành công')

cart.value = []
customerName.value = ''
customerPhone.value = ''
voucherCode.value = ''
appliedVoucher.value = null
voucherMessage.value = ''
paymentMethod.value = 'TIEN_MAT'
cashReceived.value = 0
variants.value = []
selectedProduct.value = null

await loadProducts()
  } catch (error) {
    alert(error.message)
  } finally {
    isSubmitting.value = false
  }
}
function printInvoice() {
  const invoiceElement = document.querySelector('.invoice-paper')

  if (!invoiceElement) {
    alert('Không tìm thấy nội dung hóa đơn để in')
    return
  }

  const printWindow = window.open('', '_blank', 'width=800,height=600')

  if (!printWindow) {
    alert('Trình duyệt đang chặn cửa sổ in. Vui lòng cho phép popup.')
    return
  }

  printWindow.document.write(`
    <html>
      <head>
        <title>Hóa đơn bán hàng</title>
        <style>
          body {
            font-family: Arial, sans-serif;
            padding: 20px;
            color: #000;
          }

          h2 {
            text-align: center;
            margin-bottom: 20px;
          }

          p {
            margin: 6px 0;
            font-size: 14px;
          }

          table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 14px;
          }

          th,
          td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: left;
            font-size: 14px;
          }

          th {
            background: #f3f4f6;
          }

          small {
            color: #555;
          }

          .invoice-summary {
            margin-top: 14px;
            border-top: 1px dashed #999;
            padding-top: 12px;
            font-weight: bold;
          }

          .invoice-thanks {
            text-align: center;
            margin-top: 18px;
            font-weight: bold;
          }

          @media print {
            body {
              width: 80mm;
              margin: 0 auto;
              padding: 10px;
            }
          }
        </style>
      </head>

      <body>
        ${invoiceElement.innerHTML}
      </body>
    </html>
  `)

  printWindow.document.close()
  printWindow.focus()

  setTimeout(() => {
    printWindow.print()
    printWindow.close()
  }, 300)
}

function formatMoney(value) {
  return Number(value || 0).toLocaleString('vi-VN') + ' đ'
}
</script>

<style scoped>
.pos-page {
  color: #0f172a;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
}

.page-header p {
  margin: 6px 0 0;
  color: #64748b;
}

.btn-refresh,
.product-card button,
.variant-row button,
.voucher-input button,
.btn-checkout {
  border: none;
  border-radius: 10px;
  background: #2563eb;
  color: white;
  padding: 10px 14px;
  cursor: pointer;
  font-weight: 600;
}

.pos-grid {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 20px;
}

.product-panel,
.cart-panel {
  background: white;
  border-radius: 18px;
  padding: 20px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.search-box input,
.customer-box input,
.voucher-input input,
.cash-box input {
  width: 100%;
  border: 1px solid #dbe3ef;
  border-radius: 12px;
  padding: 12px 14px;
  outline: none;
  font-size: 15px;
}

.customer-box {
  display: grid;
  gap: 10px;
  margin-bottom: 16px;
}

.product-list {
  display: grid;
  gap: 12px;
  margin-top: 16px;
  max-height: 650px;
  overflow-y: auto;
}

.product-card,
.variant-row {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: center;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 14px;
}

.product-card h4,
.variant-box h4 {
  margin: 0 0 6px;
}

.product-card p,
.variant-row span {
  display: block;
  margin: 2px 0;
  color: #64748b;
  font-size: 13px;
}

.variant-box {
  border: 1px dashed #cbd5e1;
  border-radius: 14px;
  padding: 14px;
  margin-bottom: 16px;
}

.variant-row {
  margin-top: 10px;
}

.variant-row button:disabled {
  background: #94a3b8;
  cursor: not-allowed;
}

.cart-table table {
  width: 100%;
  border-collapse: collapse;
}

.cart-table th,
.cart-table td {
  border-bottom: 1px solid #e2e8f0;
  padding: 10px;
  text-align: left;
}

.cart-table input {
  width: 76px;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 6px;
}

.btn-remove {
  border: none;
  background: #ef4444;
  color: white;
  border-radius: 8px;
  padding: 7px 10px;
  cursor: pointer;
}

.voucher-box,
.payment-box {
  margin-top: 16px;
  padding: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
}

.voucher-box label,
.payment-box label {
  display: block;
  margin-bottom: 10px;
  color: #475569;
  font-weight: 600;
}

.voucher-input {
  display: flex;
  gap: 10px;
}

.voucher-input input {
  flex: 1;
}

.voucher-input button {
  white-space: nowrap;
}

.btn-cancel {
  background: #ef4444 !important;
}

.voucher-message {
  margin: 8px 0 0;
  color: #2563eb;
  font-size: 14px;
}

.payment-options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.payment-options button,
.quick-cash button {
  border: 1px solid #cbd5e1;
  background: white;
  color: #0f172a;
  border-radius: 10px;
  padding: 10px;
  cursor: pointer;
  font-weight: 600;
}

.payment-options button.active {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
}

.cash-box {
  margin-top: 12px;
}

.quick-cash {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-top: 10px;
}

.total-box {
  margin: 18px 0;
  display: grid;
  gap: 10px;
}

.total-box div {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-box span {
  color: #475569;
}

.total-box strong {
  font-size: 18px;
}

.total-box .discount {
  color: #ef4444;
}

.total-box .payable {
  font-size: 24px;
  color: #2563eb;
}

.btn-checkout {
  width: 100%;
  padding: 14px;
  font-size: 16px;
}

.empty-cart {
  border: 1px dashed #cbd5e1;
  color: #64748b;
  text-align: center;
  padding: 30px;
  border-radius: 14px;
}

@media (max-width: 1100px) {
  .pos-grid {
    grid-template-columns: 1fr;
  }
}

.invoice-bg {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.55);
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 30px;
  overflow-y: auto;
}

.invoice-box {
  width: 520px;
  max-width: 100%;
  background: white;
  border-radius: 16px;
  padding: 18px;
}

.invoice-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-bottom: 12px;
}

.invoice-actions button {
  border: none;
  border-radius: 10px;
  background: #2563eb;
  color: white;
  padding: 10px 14px;
  cursor: pointer;
  font-weight: 600;
}

.invoice-actions button:last-child {
  background: #ef4444;
}

.invoice-paper {
  background: white;
  color: #0f172a;
}

.invoice-paper h2 {
  text-align: center;
  margin: 0 0 16px;
}

.invoice-paper p {
  margin: 6px 0;
}

.invoice-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 14px;
}

.invoice-table th,
.invoice-table td {
  border: 1px solid #e2e8f0;
  padding: 8px;
  text-align: left;
  font-size: 14px;
}

.invoice-table th {
  background: #f8fafc;
}

.invoice-table small {
  color: #64748b;
}

.invoice-summary {
  margin-top: 14px;
  border-top: 1px dashed #cbd5e1;
  padding-top: 12px;
  font-weight: 600;
}

.invoice-thanks {
  text-align: center;
  margin-top: 18px !important;
  color: #2563eb;
  font-weight: 700;
}

@media print {
  body * {
    visibility: hidden;
  }

  .invoice-bg,
  .invoice-bg * {
    visibility: visible;
  }

  .invoice-bg {
    position: fixed;
    inset: 0;
    background: white;
    padding: 0;
    display: block;
  }

  .invoice-box {
    width: 80mm;
    max-width: 80mm;
    border-radius: 0;
    padding: 10px;
    margin: 0 auto;
  }

  .invoice-actions {
    display: none;
  }
}
</style>