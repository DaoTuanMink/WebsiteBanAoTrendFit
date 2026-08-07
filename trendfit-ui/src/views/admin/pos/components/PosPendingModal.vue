<template>
  <template v-if="show">
    <div class="modal fade show d-block" tabindex="-1" role="dialog" aria-modal="true">
      <div class="modal-dialog modal-lg modal-dialog-scrollable">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title fw-bold">Danh sách hóa đơn chờ</h5>
            <button type="button" class="btn-close" aria-label="Đóng" @click="$emit('close')"></button>
          </div>
          <div class="modal-body">
            <div v-if="loading" class="text-center py-4">
              <div class="spinner-border" role="status"></div>
            </div>
            <div v-else-if="!list.length" class="text-muted text-center py-4">
              Chưa có hóa đơn chờ nào. Bấm "Lưu tạm" ở giỏ hàng để cất lại đơn đang dở.
            </div>
            <table v-else class="table table-hover align-middle">
              <thead class="table-light">
                <tr>
                  <th>Khách hàng</th>
                  <th>Số SP</th>
                  <th>Tổng tiền</th>
                  <th>Ghi chú</th>
                  <th>Thời gian</th>
                  <th>Người lưu</th>
                  <th class="text-end">Hành động</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="hd in list" :key="hd.id">
                  <td>
                    <div class="fw-semibold">{{ hd.tenKhachHang || 'Khách lẻ' }}</div>
                    <div class="small text-muted">{{ hd.soDienThoai || '—' }}</div>
                  </td>
                  <td>{{ hd.soLuongSanPham }}</td>
                  <td class="fw-semibold">{{ formatMoney(hd.tongTien) }}</td>
                  <td class="small text-muted">{{ hd.ghiChu || '—' }}</td>
                  <td class="small">{{ new Date(hd.ngayTao).toLocaleString('vi-VN') }}</td>
                  <td class="small">{{ hd.tenNguoiTao }}</td>
                  <td class="text-end">
                    <button
                      type="button"
                      class="btn btn-sm btn-success me-1"
                      @click="$emit('restore', hd.id)"
                    >
                      Gọi lại
                    </button>
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-danger"
                      @click="$emit('remove', hd.id)"
                    >
                      Xóa
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
            <div v-if="cartHasItems" class="alert alert-warning border-0 small mb-0 mt-3 d-flex gap-2 align-items-start">
              <span class="fw-bold">Lưu ý:</span>
              <span>Giỏ hàng hiện tại đang có sản phẩm. Nếu bấm <strong>Gọi lại</strong>, giỏ hiện tại sẽ bị thay thế hoàn toàn.</span>
            </div>
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
  list: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  cartHasItems: { type: Boolean, default: false },
  formatMoney: { type: Function, required: true },
})
defineEmits(['close', 'restore', 'remove'])
</script>
