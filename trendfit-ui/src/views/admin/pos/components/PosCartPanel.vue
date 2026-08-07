<template>
  <section class="card border-0 shadow-sm">
    <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center flex-wrap gap-2">
      <h5 class="fw-bold mb-0">Giỏ hàng tại quầy</h5>
      <div class="d-flex gap-2">
        <button
          type="button"
          class="btn btn-outline-secondary btn-sm position-relative"
          @click="$emit('open-pending')"
        >
          Hóa đơn chờ
          <span
            v-if="pendingCount > 0"
            class="badge rounded-pill bg-danger position-absolute top-0 start-100 translate-middle"
          >
            {{ pendingCount }}
          </span>
        </button>
        <button
          type="button"
          class="btn btn-outline-warning btn-sm"
          :disabled="!cart.length || savingPending"
          @click="$emit('save-pending')"
        >
          {{ savingPending ? 'Đang lưu...' : 'Lưu tạm' }}
        </button>
      </div>
    </div>

    <div class="card-body">
      <!-- Khách hàng -->
      <div class="mb-3">
        <h6 class="fw-bold mb-2">Thông tin khách hàng</h6>
        <div class="row g-2">
          <div class="col-md-6">
            <input
              :value="customerName"
              type="text"
              class="form-control"
              placeholder="Tên khách (bỏ trống = khách lẻ)"
              @input="$emit('update:customerName', $event.target.value)"
            />
          </div>
          <div class="col-md-6">
            <input
              :value="customerPhone"
              type="text"
              class="form-control"
              placeholder="Số điện thoại"
              @input="$emit('update:customerPhone', $event.target.value)"
            />
          </div>
        </div>
      </div>

      <div v-if="!cart.length" class="alert alert-secondary text-center mb-0">
        Chưa có sản phẩm nào trong giỏ hàng.
      </div>

      <template v-else>
        <!-- Bảng giỏ — max-height để không dài khi nhiều dòng -->
        <div class="table-responsive mb-3 cart-scroll">
          <table class="table table-bordered table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>Sản phẩm</th>
                <th class="text-center" style="width: 90px">SL</th>
                <th class="text-end">Đơn giá</th>
                <th class="text-end">Thành tiền</th>
                <th class="text-center" style="width: 70px"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in cart" :key="item.bienTheId">
                <td>
                  <div class="fw-semibold">{{ item.ten }}</div>
                  <small class="text-secondary">{{ item.tenKichCo }} - {{ item.tenMau }}</small>
                </td>
                <td>
                  <input
                    v-model.number="item.quantity"
                    type="number"
                    min="1"
                    :max="item.soLuongTon"
                    class="form-control form-control-sm text-center"
                    @change="$emit('normalize-qty', item)"
                  />
                </td>
                <td class="text-end text-nowrap">{{ formatMoney(item.gia) }}</td>
                <td class="text-end fw-semibold text-nowrap">
                  {{ formatMoney(item.gia * item.quantity) }}
                </td>
                <td class="text-center">
                  <button
                    type="button"
                    class="btn btn-outline-danger btn-sm"
                    @click="$emit('remove-item', item.bienTheId)"
                  >
                    Xóa
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Voucher -->
        <div class="card mb-3">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-start gap-2 mb-2">
              <div>
                <h6 class="fw-bold mb-0">Mã giảm giá</h6>
                <div class="small text-secondary">Nhập mã hoặc chọn gợi ý</div>
              </div>
              <button
                type="button"
                class="btn btn-outline-secondary btn-sm"
                :disabled="loadingVouchers"
                @click="$emit('reload-vouchers')"
              >
                {{ loadingVouchers ? 'Đang tải...' : 'Làm mới' }}
              </button>
            </div>
            <div class="input-group">
              <input
                :value="voucherCode"
                type="text"
                class="form-control text-uppercase"
                placeholder="VD: SALE10"
                :disabled="!!appliedVoucher"
                @input="$emit('update:voucherCode', $event.target.value)"
                @keyup.enter="$emit('apply-voucher')"
              />
              <button
                v-if="!appliedVoucher"
                type="button"
                class="btn btn-primary"
                @click="$emit('apply-voucher')"
              >
                Áp dụng
              </button>
              <button v-else type="button" class="btn btn-danger" @click="$emit('remove-voucher')">
                Hủy mã
              </button>
            </div>
            <div
              v-if="voucherMessage"
              class="alert mt-2 mb-0 py-2"
              :class="appliedVoucher ? 'alert-success' : 'alert-warning'"
            >
              {{ voucherMessage }}
            </div>

            <div v-if="suggestedVouchers.length" class="mt-3 d-grid gap-2">
              <div
                v-for="v in suggestedVouchers"
                :key="v.id || v.ma"
                class="border rounded-3 p-2"
                :class="{
                  'border-success bg-success-subtle': v.eligible,
                  'border-primary': appliedVoucher?.ma === v.ma,
                }"
              >
                <div class="d-flex justify-content-between align-items-start gap-2">
                  <div class="min-w-0">
                    <span class="badge text-bg-primary me-1">{{ v.ma }}</span>
                    <span class="badge" :class="v.eligible ? 'text-bg-success' : 'text-bg-warning'">
                      {{ v.eligible ? 'Dùng được' : v.reason }}
                    </span>
                    <div class="small mt-1">
                      {{ v.ten || discountLabel(v) }}
                      <span v-if="v.eligible" class="text-success fw-semibold">
                        · Giảm ~{{ formatMoney(v.estimatedDiscount) }}
                      </span>
                    </div>
                  </div>
                  <button
                    type="button"
                    class="btn btn-sm"
                    :class="v.eligible ? 'btn-outline-primary' : 'btn-outline-secondary'"
                    :disabled="!v.eligible || !!appliedVoucher"
                    @click="$emit('apply-suggested', v)"
                  >
                    {{ appliedVoucher?.ma === v.ma ? 'Đã dùng' : 'Chọn' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Thanh toán -->
        <div class="card mb-3">
          <div class="card-body">
            <h6 class="fw-bold mb-2">Phương thức thanh toán</h6>
            <div class="row g-2 mb-3">
              <div class="col-6">
                <button
                  type="button"
                  class="btn w-100"
                  :class="paymentMethod === 'TIEN_MAT' ? 'btn-primary' : 'btn-outline-primary'"
                  @click="$emit('update:paymentMethod', 'TIEN_MAT')"
                >
                  Tiền mặt
                </button>
              </div>
              <div class="col-6">
                <button
                  type="button"
                  class="btn w-100"
                  :class="paymentMethod === 'CHUYEN_KHOAN' ? 'btn-primary' : 'btn-outline-primary'"
                  @click="$emit('update:paymentMethod', 'CHUYEN_KHOAN')"
                >
                  Chuyển khoản
                </button>
              </div>
            </div>

            <div v-if="paymentMethod === 'TIEN_MAT'">
              <label class="form-label">Tiền khách đưa</label>
              <input
                :value="cashReceived"
                type="number"
                min="0"
                class="form-control mb-2"
                placeholder="Nhập số tiền"
                @input="$emit('update:cashReceived', Number($event.target.value) || 0)"
              />
              <div class="row row-cols-2 row-cols-md-4 g-2">
                <div class="col">
                  <button
                    type="button"
                    class="btn btn-outline-secondary btn-sm w-100"
                    @click="$emit('update:cashReceived', totalPayable)"
                  >
                    Vừa đủ
                  </button>
                </div>
                <div v-for="amt in [100000, 200000, 500000]" :key="amt" class="col">
                  <button
                    type="button"
                    class="btn btn-outline-secondary btn-sm w-100"
                    @click="$emit('update:cashReceived', amt)"
                  >
                    {{ (amt / 1000).toFixed(0) }}.000
                  </button>
                </div>
              </div>
            </div>

            <div v-else class="text-center py-2">
              <p class="text-muted small mb-2">Quét mã QR bằng app ngân hàng</p>
              <img
                :src="vietQrUrl"
                alt="QR chuyển khoản"
                class="img-fluid border rounded"
                style="max-width: 240px"
              />
              <p class="fw-bold fs-5 mt-2 mb-0 text-primary">{{ formatMoney(totalPayable) }}</p>
              <p class="small text-muted mb-0">{{ transferNote }}</p>
            </div>
          </div>
        </div>

        <!-- Tổng -->
        <div class="card bg-light border-0 mb-3">
          <div class="card-body py-3">
            <div class="d-flex justify-content-between mb-1">
              <span class="text-secondary">Tổng tiền hàng</span>
              <strong>{{ formatMoney(totalAmount) }}</strong>
            </div>
            <div class="d-flex justify-content-between mb-1">
              <span class="text-secondary">Giảm giá</span>
              <strong class="text-danger">- {{ formatMoney(discountAmount) }}</strong>
            </div>
            <hr class="my-2" />
            <div class="d-flex justify-content-between align-items-center mb-1">
              <span class="fw-bold">Khách cần trả</span>
              <strong class="fs-4 text-primary">{{ formatMoney(totalPayable) }}</strong>
            </div>
            <template v-if="paymentMethod === 'TIEN_MAT'">
              <div class="d-flex justify-content-between mb-1">
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
          :disabled="submitting"
          @click="$emit('checkout')"
        >
          <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
          {{ submitting ? 'Đang thanh toán...' : 'Thanh toán' }}
        </button>
      </template>
    </div>
  </section>
</template>

<script setup>
defineProps({
  cart: { type: Array, default: () => [] },
  customerName: { type: String, default: '' },
  customerPhone: { type: String, default: '' },
  voucherCode: { type: String, default: '' },
  appliedVoucher: { type: Object, default: null },
  voucherMessage: { type: String, default: '' },
  suggestedVouchers: { type: Array, default: () => [] },
  loadingVouchers: { type: Boolean, default: false },
  paymentMethod: { type: String, default: 'TIEN_MAT' },
  cashReceived: { type: Number, default: 0 },
  totalAmount: { type: Number, default: 0 },
  discountAmount: { type: Number, default: 0 },
  totalPayable: { type: Number, default: 0 },
  changeAmount: { type: Number, default: 0 },
  vietQrUrl: { type: String, default: '' },
  transferNote: { type: String, default: '' },
  pendingCount: { type: Number, default: 0 },
  savingPending: { type: Boolean, default: false },
  submitting: { type: Boolean, default: false },
  formatMoney: { type: Function, required: true },
  discountLabel: { type: Function, required: true },
})

defineEmits([
  'update:customerName',
  'update:customerPhone',
  'update:voucherCode',
  'update:paymentMethod',
  'update:cashReceived',
  'open-pending',
  'save-pending',
  'normalize-qty',
  'remove-item',
  'reload-vouchers',
  'apply-voucher',
  'remove-voucher',
  'apply-suggested',
  'checkout',
])
</script>

<style scoped>
.cart-scroll {
  max-height: 260px;
  overflow-y: auto;
}
.min-w-0 {
  min-width: 0;
}
</style>
