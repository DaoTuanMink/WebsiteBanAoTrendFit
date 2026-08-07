<template>
  <template v-if="show && data">
    <div class="modal fade show d-block" tabindex="-1" role="dialog" aria-modal="true">
      <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title fw-bold">Hóa đơn bán hàng</h5>
            <button type="button" class="btn-close" aria-label="Đóng" @click="$emit('close')"></button>
          </div>
          <div class="modal-body">
            <div class="p-3">
              <h2 class="text-center fw-bold mb-4">HÓA ĐƠN BÁN HÀNG</h2>
              <div class="row g-2 mb-3">
                <div class="col-md-6">
                  <p class="mb-1"><strong>Mã hóa đơn:</strong> {{ data.code }}</p>
                  <p class="mb-1"><strong>Ngày tạo:</strong> {{ data.date }}</p>
                  <p class="mb-1"><strong>Khách hàng:</strong> {{ data.customer }}</p>
                </div>
                <div class="col-md-6">
                  <p class="mb-1"><strong>Số điện thoại:</strong> {{ data.phone }}</p>
                  <p class="mb-1"><strong>Thanh toán:</strong> {{ data.paymentMethod }}</p>
                </div>
              </div>
              <div class="table-responsive">
                <table class="table table-bordered align-middle">
                  <thead class="table-light">
                    <tr>
                      <th class="text-center">STT</th>
                      <th>Mã SP</th>
                      <th>Sản phẩm</th>
                      <th class="text-center">SL</th>
                      <th class="text-end">Giá</th>
                      <th class="text-end">Tổng</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(item, index) in data.items" :key="index">
                      <td class="text-center">{{ index + 1 }}</td>
                      <td>{{ item.maSku || '—' }}</td>
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
              <div class="border-top pt-3 mt-3">
                <div class="d-flex justify-content-between mb-2">
                  <span>Tổng tiền hàng</span>
                  <strong>{{ formatMoney(data.totalAmount) }}</strong>
                </div>
                <div class="d-flex justify-content-between mb-2">
                  <span>Giảm giá</span>
                  <strong>{{ formatMoney(data.discount) }}</strong>
                </div>
                <div class="d-flex justify-content-between mb-2">
                  <span>Khách cần trả</span>
                  <strong>{{ formatMoney(data.payable) }}</strong>
                </div>
                <div class="d-flex justify-content-between mb-2">
                  <span>Khách đã đưa</span>
                  <strong>{{ formatMoney(data.paid) }}</strong>
                </div>
                <div class="d-flex justify-content-between">
                  <span>Tiền thừa</span>
                  <strong>{{ formatMoney(data.change) }}</strong>
                </div>
              </div>
              <p class="text-center fw-bold text-primary mt-4 mb-0">
                Cảm ơn quý khách đã mua hàng tại TrendFit!
              </p>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-outline-secondary" @click="$emit('close')">Đóng</button>
            <button type="button" class="btn btn-primary" @click="$emit('print')">In hóa đơn</button>
          </div>
        </div>
      </div>
    </div>
    <div class="modal-backdrop fade show"></div>
  </template>
</template>

<script setup>
defineProps({
  show: { type: Boolean, default: false },
  data: { type: Object, default: null },
  formatMoney: { type: Function, required: true },
})
defineEmits(['close', 'print'])
</script>
