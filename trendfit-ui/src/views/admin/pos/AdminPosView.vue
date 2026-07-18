<template>
  <div class="container-fluid py-3">
    <!-- TIÊU ĐỀ TRANG -->
    <div class="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
      <div>
        <h2 class="fw-bold mb-1">Bán hàng tại quầy</h2>
        <p class="text-secondary mb-0">Tạo đơn trực tiếp cho khách mua tại cửa hàng</p>
      </div>

      <button type="button" class="btn btn-primary" @click="loadProducts">
        Tải lại sản phẩm
      </button>
    </div>

    <div class="row g-4 align-items-start">
      <!-- CỘT DANH SÁCH SẢN PHẨM -->
      <div class="col-12 col-xl-5">
        <section class="card border-0 shadow-sm">
          <div class="card-header bg-white py-3">
            <h5 class="fw-bold mb-0">Danh sách sản phẩm</h5>
          </div>

          <div class="card-body">
            <div class="input-group mb-3">
              <span class="input-group-text">Tìm</span>
              <input
                v-model="keyword"
                type="text"
                class="form-control"
                placeholder="Tìm sản phẩm theo tên..."
              />
            </div>

            <div v-if="filteredProducts.length" class="d-grid gap-3">
              <div
                v-for="product in filteredProducts"
                :key="product.id"
                class="border rounded-3 p-3"
              >
                <div class="d-flex justify-content-between align-items-center gap-3">
                  <div class="flex-grow-1">
                    <h6 class="fw-bold mb-1">{{ product.ten }}</h6>
                    <div class="small text-secondary">
                      {{ product.danhMuc?.ten || 'Chưa có danh mục' }}
                    </div>
                  </div>

                  <button
                    type="button"
                    class="btn btn-outline-primary btn-sm text-nowrap"
                    :disabled="!product.id"
                    @click="loadVariants(product)"
                  >
                    {{ product.id ? 'Chọn biến thể' : 'Thiếu ID' }}
                  </button>
                </div>
              </div>
            </div>

            <div v-else class="alert alert-secondary text-center mb-0">
              Không tìm thấy sản phẩm phù hợp.
            </div>
          </div>
        </section>
      </div>

      <!-- CỘT GIỎ HÀNG VÀ THANH TOÁN -->
      <div class="col-12 col-xl-7">
        <section class="card border-0 shadow-sm">
          <div class="card-header bg-white py-3">
            <h5 class="fw-bold mb-0">Giỏ hàng tại quầy</h5>
          </div>

          <div class="card-body">
            <!-- THÔNG TIN KHÁCH HÀNG -->
            <div class="mb-4">
              <h6 class="fw-bold mb-3">Thông tin khách hàng</h6>

              <div class="row g-3">
                <div class="col-12 col-md-6">
                  <label class="form-label">Tên khách hàng</label>
                  <input
                    v-model="customerName"
                    type="text"
                    class="form-control"
                    placeholder="Bỏ trống nếu là khách lẻ"
                  />
                </div>

                <div class="col-12 col-md-6">
                  <label class="form-label">Số điện thoại</label>
                  <input
                    v-model="customerPhone"
                    type="text"
                    class="form-control"
                    placeholder="Nhập số điện thoại"
                  />
                </div>
              </div>
            </div>

            <!-- DANH SÁCH BIẾN THỂ -->
            <div v-if="selectedProduct" class="card border-primary mb-4">
              <div class="card-header bg-primary-subtle d-flex justify-content-between gap-3">
                <div>
                  <div class="small text-secondary">Sản phẩm đang chọn</div>
                  <strong>{{ selectedProduct.ten }}</strong>
                </div>

                <button
                  type="button"
                  class="btn-close"
                  aria-label="Đóng"
                  @click="selectedProduct = null; variants = []"
                ></button>
              </div>

              <div class="card-body">
                <div v-if="variants.length" class="list-group">
                  <div
                    v-for="variant in variants"
                    :key="variant.id"
                    class="list-group-item"
                  >
                    <div class="d-flex justify-content-between align-items-center gap-3">
                      <div>
                        <div class="fw-bold">
                          {{ variant.kichCo?.tenKichCo }} - {{ variant.mauSac?.tenMau }}
                        </div>
                        <div class="small text-secondary">SKU: {{ variant.maSku }}</div>
                        <div class="small text-secondary">Tồn kho: {{ variant.soLuongTon }}</div>
                        <div class="fw-semibold text-primary">
                          {{ formatMoney(getPrice(variant)) }}
                        </div>
                      </div>

                      <button
                        type="button"
                        class="btn btn-primary btn-sm"
                        :disabled="variant.soLuongTon <= 0"
                        @click="addToCart(selectedProduct, variant)"
                      >
                        {{ variant.soLuongTon > 0 ? 'Thêm' : 'Hết hàng' }}
                      </button>
                    </div>
                  </div>
                </div>

                <div v-else class="alert alert-warning mb-0">
                  Sản phẩm này chưa có biến thể.
                </div>
              </div>
            </div>

            <!-- GIỎ HÀNG TRỐNG -->
            <div v-if="!cart.length" class="alert alert-secondary text-center mb-0">
              Chưa có sản phẩm nào trong giỏ hàng.
            </div>

            <!-- NỘI DUNG GIỎ HÀNG -->
            <div v-else>
              <div class="table-responsive mb-4">
                <table class="table table-bordered table-hover align-middle mb-0">
                  <thead class="table-light">
                    <tr>
                      <th>Sản phẩm</th>
                      <th class="text-center">Số lượng</th>
                      <th class="text-end">Đơn giá</th>
                      <th class="text-end">Thành tiền</th>
                      <th class="text-center">Thao tác</th>
                    </tr>
                  </thead>

                  <tbody>
                    <tr v-for="item in cart" :key="item.bienTheId">
                      <td>
                        <div class="fw-semibold">{{ item.ten }}</div>
                        <small class="text-secondary">
                          {{ item.tenKichCo }} - {{ item.tenMau }}
                        </small>
                      </td>

                      <td>
                        <input
                          v-model.number="item.quantity"
                          type="number"
                          min="1"
                          :max="item.soLuongTon"
                          class="form-control form-control-sm mx-auto"
                          @change="normalizeQuantity(item)"
                        />
                      </td>

                      <td class="text-end text-nowrap">
                        {{ formatMoney(item.gia) }}
                      </td>

                      <td class="text-end fw-semibold text-nowrap">
                        {{ formatMoney(item.gia * item.quantity) }}
                      </td>

                      <td class="text-center">
                        <button
                          type="button"
                          class="btn btn-outline-danger btn-sm"
                          @click="removeItem(item.bienTheId)"
                        >
                          Xóa
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <!-- MÃ GIẢM GIÁ -->
              <div class="card mb-4 voucher-card">
                <div class="card-body">
                  <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                    <div>
                      <h6 class="fw-bold mb-1">Mã giảm giá</h6>
                      <div class="small text-secondary">
                        Chọn mã gợi ý hoặc nhập mã thủ công cho khách.
                      </div>
                    </div>

                    <button
                      type="button"
                      class="btn btn-outline-secondary btn-sm text-nowrap"
                      :disabled="isLoadingVouchers"
                      @click="loadVoucherSuggestions"
                    >
                      <span
                        v-if="isLoadingVouchers"
                        class="spinner-border spinner-border-sm me-1"
                        aria-hidden="true"
                      ></span>
                      {{ isLoadingVouchers ? 'Đang tải' : 'Làm mới mã' }}
                    </button>
                  </div>

                  <div class="input-group">
                    <input
                      v-model.trim="voucherCode"
                      type="text"
                      class="form-control text-uppercase"
                      placeholder="Nhập mã, ví dụ: SALE10"
                      :disabled="!!appliedVoucher"
                      @keyup.enter="applyVoucher"
                    />

                    <button
                      v-if="!appliedVoucher"
                      type="button"
                      class="btn btn-primary"
                      @click="applyVoucher"
                    >
                      Áp dụng
                    </button>

                    <button
                      v-else
                      type="button"
                      class="btn btn-danger"
                      @click="removeVoucher"
                    >
                      Hủy mã
                    </button>
                  </div>

                  <div
                    v-if="voucherMessage"
                    class="alert mt-3 mb-0"
                    :class="appliedVoucher ? 'alert-success' : 'alert-warning'"
                  >
                    {{ voucherMessage }}
                  </div>

                  <div class="mt-4">
                    <div class="d-flex justify-content-between align-items-center gap-3 mb-2">
                      <div class="fw-bold">Gợi ý phù hợp với đơn hàng</div>
                      <span class="badge text-bg-light border">
                        Tổng đơn: {{ formatMoney(totalAmount) }}
                      </span>
                    </div>

                    <div v-if="!cart.length" class="alert alert-light border mb-0">
                      Thêm sản phẩm vào giỏ để hệ thống xác định mã phù hợp.
                    </div>

                    <div v-else-if="isLoadingVouchers" class="text-center py-4">
                      <div class="spinner-border text-primary" role="status"></div>
                      <div class="small text-secondary mt-2">Đang tìm mã giảm giá...</div>
                    </div>

                    <div v-else-if="suggestedVouchers.length" class="d-grid gap-2">
                      <div
                        v-for="voucher in suggestedVouchers"
                        :key="voucher.id || voucher.ma"
                        class="voucher-suggestion"
                        :class="{
                          'is-eligible': voucher.eligible,
                          'is-applied': appliedVoucher?.ma === voucher.ma,
                        }"
                      >
                        <div class="d-flex justify-content-between align-items-start gap-3">
                          <div class="min-w-0">
                            <div class="d-flex align-items-center flex-wrap gap-2 mb-1">
                              <span class="voucher-code">{{ voucher.ma }}</span>
                              <span
                                class="badge"
                                :class="voucher.eligible ? 'text-bg-success' : 'text-bg-warning'"
                              >
                                {{ voucher.eligible ? 'Dùng được' : voucher.reason }}
                              </span>
                            </div>

                            <div class="fw-semibold text-dark">
                              {{ voucher.ten || voucherDiscountLabel(voucher) }}
                            </div>

                            <div class="small text-secondary mt-1">
                              {{ voucherDiscountLabel(voucher) }}
                              <span v-if="voucher.minimumOrder > 0">
                                · Đơn tối thiểu {{ formatMoney(voucher.minimumOrder) }}
                              </span>
                            </div>

                            <div class="small mt-1">
                              <span v-if="voucher.eligible" class="text-success fw-semibold">
                                Đơn này dự kiến giảm {{ formatMoney(voucher.estimatedDiscount) }}
                              </span>
                              <span v-else class="text-warning fw-semibold">
                                {{ voucher.helpText }}
                              </span>
                            </div>

                            <div class="small text-secondary mt-1">
                              <span v-if="voucher.ngayKetThuc">
                                Hạn: {{ formatVoucherDate(voucher.ngayKetThuc) }}
                              </span>
                              <span v-if="voucher.remainingUses !== null">
                                · Còn {{ voucher.remainingUses }} lượt
                              </span>
                            </div>
                          </div>

                          <button
                            type="button"
                            class="btn btn-sm text-nowrap"
                            :class="voucher.eligible ? 'btn-outline-primary' : 'btn-outline-secondary'"
                            :disabled="!voucher.eligible || !!appliedVoucher"
                            @click="applySuggestedVoucher(voucher)"
                          >
                            {{ appliedVoucher?.ma === voucher.ma ? 'Đã dùng' : 'Chọn mã' }}
                          </button>
                        </div>
                      </div>
                    </div>

                    <div v-else class="alert alert-light border mb-0">
                      Chưa có mã giảm giá đang hoạt động hoặc phù hợp với đơn này.
                    </div>
                  </div>
                </div>
              </div>

              <!-- PHƯƠNG THỨC THANH TOÁN -->
              <div class="card mb-4">
                <div class="card-body">
                  <h6 class="fw-bold mb-3">Phương thức thanh toán</h6>

                  <div class="row g-2 mb-3">
                    <div class="col-6">
                      <button
                        type="button"
                        class="btn w-100"
                        :class="paymentMethod === 'TIEN_MAT' ? 'btn-primary' : 'btn-outline-primary'"
                        @click="paymentMethod = 'TIEN_MAT'"
                      >
                        Tiền mặt
                      </button>
                    </div>

                    <div class="col-6">
                      <button
                        type="button"
                        class="btn w-100"
                        :class="paymentMethod === 'CHUYEN_KHOAN' ? 'btn-primary' : 'btn-outline-primary'"
                        @click="paymentMethod = 'CHUYEN_KHOAN'"
                      >
                        Chuyển khoản
                      </button>
                    </div>
                  </div>

                  <div v-if="paymentMethod === 'TIEN_MAT'">
                    <label class="form-label">Tiền khách đưa</label>
                    <input
                      v-model.number="cashReceived"
                      type="number"
                      min="0"
                      class="form-control mb-3"
                      placeholder="Nhập số tiền khách đưa"
                    />

                    <div class="row row-cols-2 row-cols-md-4 g-2">
                      <div class="col">
                        <button
                          type="button"
                          class="btn btn-outline-secondary btn-sm w-100"
                          @click="cashReceived = totalPayable"
                        >
                          Vừa đủ
                        </button>
                      </div>

                      <div class="col">
                        <button
                          type="button"
                          class="btn btn-outline-secondary btn-sm w-100"
                          @click="cashReceived = 100000"
                        >
                          100.000
                        </button>
                      </div>

                      <div class="col">
                        <button
                          type="button"
                          class="btn btn-outline-secondary btn-sm w-100"
                          @click="cashReceived = 200000"
                        >
                          200.000
                        </button>
                      </div>

                      <div class="col">
                        <button
                          type="button"
                          class="btn btn-outline-secondary btn-sm w-100"
                          @click="cashReceived = 500000"
                        >
                          500.000
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- TỔNG TIỀN -->
              <div class="card bg-light border-0 mb-4">
                <div class="card-body">
                  <div class="d-flex justify-content-between mb-2">
                    <span class="text-secondary">Tổng tiền hàng</span>
                    <strong>{{ formatMoney(totalAmount) }}</strong>
                  </div>

                  <div class="d-flex justify-content-between mb-2">
                    <span class="text-secondary">Giảm giá</span>
                    <strong class="text-danger">- {{ formatMoney(discountAmount) }}</strong>
                  </div>

                  <hr />

                  <div class="d-flex justify-content-between align-items-center mb-2">
                    <span class="fw-bold">Khách cần trả</span>
                    <strong class="fs-4 text-primary">{{ formatMoney(totalPayable) }}</strong>
                  </div>

                  <template v-if="paymentMethod === 'TIEN_MAT'">
                    <div class="d-flex justify-content-between mb-2">
                      <span class="text-secondary">Tiền khách đưa</span>
                      <strong>{{ formatMoney(cashReceived) }}</strong>
                    </div>

                    <div class="d-flex justify-content-between">
                      <span class="text-secondary">Tiền thừa</span>
                      <strong class="text-success">{{ formatMoney(changeAmount) }}</strong>
                    </div>
                  </template>
                </div>
              </div>

              <button
                type="button"
                class="btn btn-success btn-lg w-100"
                :disabled="isSubmitting"
                @click="checkout"
              >
                <span
                  v-if="isSubmitting"
                  class="spinner-border spinner-border-sm me-2"
                  aria-hidden="true"
                ></span>
                {{ isSubmitting ? 'Đang thanh toán...' : 'Thanh toán' }}
              </button>
            </div>
          </div>
        </section>
      </div>
    </div>

    <!-- MODAL HÓA ĐƠN -->
    <template v-if="showInvoice && invoiceData">
      <div class="modal fade show d-block" tabindex="-1" role="dialog" aria-modal="true">
        <div class="modal-dialog modal-lg modal-dialog-scrollable">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title fw-bold">Hóa đơn bán hàng</h5>
              <button
                type="button"
                class="btn-close"
                aria-label="Đóng"
                @click="showInvoice = false"
              ></button>
            </div>

            <div class="modal-body">
              <div class="invoice-paper p-3">
                <h2 class="text-center fw-bold mb-4">HÓA ĐƠN BÁN HÀNG</h2>

                <div class="row g-2 mb-3">
                  <div class="col-12 col-md-6">
                    <p class="mb-1"><strong>Mã hóa đơn:</strong> {{ invoiceData.code }}</p>
                    <p class="mb-1"><strong>Ngày tạo:</strong> {{ invoiceData.date }}</p>
                    <p class="mb-1"><strong>Khách hàng:</strong> {{ invoiceData.customer }}</p>
                  </div>

                  <div class="col-12 col-md-6">
                    <p class="mb-1"><strong>Số điện thoại:</strong> {{ invoiceData.phone }}</p>
                    <p class="mb-1">
                      <strong>Thanh toán:</strong> {{ invoiceData.paymentMethod }}
                    </p>
                  </div>
                </div>

                <div class="table-responsive">
                  <table class="invoice-table table table-bordered align-middle">
                    <thead class="table-light">
                      <tr>
                        <th>Sản phẩm</th>
                        <th class="text-center">SL</th>
                        <th class="text-end">Giá</th>
                        <th class="text-end">Tổng</th>
                      </tr>
                    </thead>

                    <tbody>
                      <tr v-for="(item, index) in invoiceData.items" :key="index">
                        <td>
                          {{ item.name }}
                          <br />
                          <small class="text-secondary">{{ item.tenKichCo }} - {{ item.tenMau }}</small>
                        </td>
                        <td class="text-center">{{ item.qty }}</td>
                        <td class="text-end">{{ formatMoney(item.price) }}</td>
                        <td class="text-end">{{ formatMoney(item.total) }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <div class="invoice-summary border-top pt-3 mt-3">
                  <div class="d-flex justify-content-between mb-2">
                    <span>Tổng tiền hàng</span>
                    <strong>{{ formatMoney(invoiceData.totalAmount) }}</strong>
                  </div>
                  <div class="d-flex justify-content-between mb-2">
                    <span>Giảm giá</span>
                    <strong>{{ formatMoney(invoiceData.discount) }}</strong>
                  </div>
                  <div class="d-flex justify-content-between mb-2">
                    <span>Khách cần trả</span>
                    <strong>{{ formatMoney(invoiceData.payable) }}</strong>
                  </div>
                  <div class="d-flex justify-content-between mb-2">
                    <span>Khách đã đưa</span>
                    <strong>{{ formatMoney(invoiceData.paid) }}</strong>
                  </div>
                  <div class="d-flex justify-content-between">
                    <span>Tiền thừa</span>
                    <strong>{{ formatMoney(invoiceData.change) }}</strong>
                  </div>
                </div>

                <p class="invoice-thanks text-center fw-bold text-primary mt-4 mb-0">
                  Cảm ơn quý khách đã mua hàng tại TrendFit!
                </p>
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="showInvoice = false">
                Đóng
              </button>
              <button type="button" class="btn btn-primary" @click="printInvoice">
                In hóa đơn
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="modal-backdrop fade show"></div>
    </template>
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
const vouchers = ref([])
const isLoadingVouchers = ref(false)

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

onMounted(() => {
  loadProducts()
  loadVoucherSuggestions()
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
    id:
      source.id ??
      source.sanPhamId ??
      source.maSanPham ??
      item?.id ??
      item?.sanPhamId ??
      null,
    ten:
      source.ten ??
      source.tenSanPham ??
      source.name ??
      'Sản phẩm chưa có tên',
    danhMuc: source.danhMuc ?? source.category ?? null,
    _variants: item?.bienTheSanPhams ?? item?.variants ?? null,
  }
}

async function loadProducts() {
  try {
    const response = await fetch(`${API_BASE}/admin/products`)

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
  const productId =
    product?.id ?? product?.sanPhamId ?? product?.maSanPham ?? null

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

    const response = await fetch(
      `${API_BASE}/admin/products/${productId}/variants`,
    )

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

    const response = await fetch(`${API_BASE}/admin/vouchers`)

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
    ma: String(voucher.ma || '').trim().toUpperCase(),
    ten: voucher.ten || '',
    loai: String(voucher.loai || '').trim().toUpperCase(),
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
    voucher.gioiHanSuDung === null
      ? null
      : Math.max(voucher.gioiHanSuDung - voucher.soLanDaDung, 0)

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
    },
    body: JSON.stringify({
      ma: voucherCode.value.trim().toUpperCase(),
      tongDon: totalAmount.value,
    }),
  }

  let response = await fetch(`${API_BASE}/admin/vouchers/check`, requestOptions)

  // Repo gốc đặt API kiểm tra voucher ở /api/public/vouchers/check.
  // Fallback này giúp trang POS chạy được với cả hai cấu hình backend.
  if (response.status === 404 || response.status === 405) {
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

  if (paymentMethod.value === 'TIEN_MAT' && Number(cashReceived.value || 0) < totalPayable.value) {
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
      paymentMethod.value === 'TIEN_MAT' ? Number(cashReceived.value || 0) : totalPayable.value,

    tienThua: paymentMethod.value === 'TIEN_MAT' ? changeAmount.value : 0,

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
.voucher-card {
  overflow: hidden;
}

.voucher-suggestion {
  padding: 13px 14px;
  border: 1px solid #dfe7f2;
  border-radius: 12px;
  background: #ffffff;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
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
</style>
