<template>
  <!--
    PosProductPanel — cột trái POS.
    Thay combobox bằng MODAL tìm sản phẩm:
      - Nút "Tìm / chọn sản phẩm" mở modal
      - Trong modal: ô tìm + danh sách SP (cuộn)
      - Chọn SP → đóng modal → hiện panel biến thể (size/màu) bên dưới
    Logic API / giỏ hàng vẫn ở AdminPosView (cha).
  -->
  <section class="card border-0 shadow-sm overflow-hidden h-100">
    <div class="card-header bg-white border-0 border-bottom py-3 d-flex justify-content-between align-items-center">
      <div>
        <h5 class="fw-bold mb-0">Sản phẩm</h5>
        <small class="text-secondary">{{ products.length }} sản phẩm có sẵn</small>
      </div>
      <button type="button" class="btn btn-outline-primary btn-sm" @click="$emit('reload')">
        Tải lại
      </button>
    </div>

    <div class="card-body">
      <!-- Nút mở modal tìm SP -->
      <button
        type="button"
        class="btn btn-primary w-100 mb-3"
        @click="openPicker"
      >
        {{ selectedProduct ? 'Đổi sản phẩm khác' : 'Tìm / chọn sản phẩm' }}
      </button>

      <!-- SP đang chọn (tóm tắt) -->
      <div v-if="selectedProduct" class="alert alert-primary py-2 small mb-3">
        <strong>Đang chọn:</strong> {{ selectedProduct.ten }}
        <span class="text-secondary">
          · {{ selectedProduct.danhMuc?.ten || 'Không danh mục' }}
        </span>
      </div>

      <!--
        PANEL BIẾN THỂ — sau khi chọn SP từ modal.
        max-height + overflow để nhiều size/màu vẫn gọn.
      -->
      <div v-if="selectedProduct" class="card border-primary">
        <div class="card-header bg-primary-subtle d-flex justify-content-between gap-2 py-2">
          <div class="min-w-0">
            <div class="small text-secondary">Chọn size / màu</div>
            <strong class="text-truncate d-block">{{ selectedProduct.ten }}</strong>
          </div>
          <button
            type="button"
            class="btn-close"
            aria-label="Bỏ chọn"
            @click="$emit('clear-product')"
          ></button>
        </div>
        <div class="card-body p-2 variant-scroll">
          <div v-if="variants.length" class="list-group list-group-flush">
            <div
              v-for="variant in variants"
              :key="variant.id"
              class="list-group-item px-2 py-2"
            >
              <div class="d-flex justify-content-between align-items-center gap-2">
                <div class="min-w-0">
                  <div class="fw-bold small">
                    {{ variant.kichCo?.tenKichCo }} - {{ variant.mauSac?.tenMau }}
                  </div>
                  <div class="text-secondary" style="font-size: 12px">
                    SKU: {{ variant.maSku }} · Tồn: {{ variant.soLuongTon }}
                  </div>
                  <div class="fw-semibold text-primary small">
                    {{ formatMoney(getPrice(variant)) }}
                  </div>
                </div>
                <button
                  type="button"
                  class="btn btn-primary btn-sm text-nowrap"
                  :disabled="variant.soLuongTon <= 0"
                  @click="$emit('add-to-cart', selectedProduct, variant)"
                >
                  {{ variant.soLuongTon > 0 ? 'Thêm' : 'Hết' }}
                </button>
              </div>
            </div>
          </div>
          <div v-else class="alert alert-warning mb-0 py-2 small">
            Sản phẩm này chưa có biến thể.
          </div>
        </div>
      </div>

      <div v-else class="alert alert-secondary text-center mb-0 small">
        Bấm "Tìm / chọn sản phẩm" để mở danh sách và thêm vào giỏ.
      </div>
    </div>

    <!--
      ===================== MODAL TÌM / CHỌN SẢN PHẨM =====================
      - Ô tìm lọc theo tên (keyword 2 chiều với cha qua update:keyword)
      - Danh sách cuộn; click 1 dòng = chọn SP + đóng modal
    -->
    <template v-if="showPicker">
      <div class="modal fade show d-block" tabindex="-1" role="dialog" aria-modal="true">
        <div class="modal-dialog modal-dialog-scrollable modal-lg">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title fw-bold">Tìm sản phẩm</h5>
              <button type="button" class="btn-close" aria-label="Đóng" @click="closePicker"></button>
            </div>
            <div class="modal-body">
              <div class="input-group mb-3">
                <span class="input-group-text">Tìm</span>
                <input
                  ref="searchInput"
                  :value="keyword"
                  type="text"
                  class="form-control"
                  placeholder="Gõ tên sản phẩm..."
                  @input="$emit('update:keyword', $event.target.value)"
                />
              </div>

              <div v-if="products.length" class="list-group list-group-flush picker-list">
                <button
                  v-for="product in products"
                  :key="product.id"
                  type="button"
                  class="list-group-item list-group-item-action d-flex justify-content-between align-items-center gap-2"
                  :class="{ active: selectedProduct?.id === product.id }"
                  @click="pickProduct(product)"
                >
                  <div class="min-w-0 text-start">
                    <div class="fw-semibold text-truncate">{{ product.ten }}</div>
                    <small :class="selectedProduct?.id === product.id ? 'text-white-50' : 'text-secondary'">
                      {{ product.danhMuc?.ten || 'Chưa có danh mục' }}
                    </small>
                  </div>
                  <span class="badge rounded-pill" :class="selectedProduct?.id === product.id ? 'text-bg-light text-dark' : 'text-bg-primary'">
                    Chọn
                  </span>
                </button>
              </div>
              <div v-else class="alert alert-secondary text-center mb-0">
                Không tìm thấy sản phẩm phù hợp.
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-outline-secondary" @click="closePicker">Đóng</button>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-backdrop fade show"></div>
    </template>
  </section>
</template>

<script setup>
/**
 * Props: keyword, products (đã lọc), selectedProduct, variants, formatMoney, getPrice
 * Emits: update:keyword | reload | select-product | clear-product | add-to-cart
 *
 * showPicker: state local của modal — không cần đẩy lên cha.
 */
import { ref, nextTick } from 'vue'

const props = defineProps({
  keyword: { type: String, default: '' },
  products: { type: Array, default: () => [] },
  selectedProduct: { type: Object, default: null },
  variants: { type: Array, default: () => [] },
  formatMoney: { type: Function, required: true },
  getPrice: { type: Function, required: true },
})

const emit = defineEmits([
  'update:keyword',
  'reload',
  'select-product',
  'clear-product',
  'add-to-cart',
])

const showPicker = ref(false)
const searchInput = ref(null)

/** Mở modal và focus vào ô tìm. */
async function openPicker() {
  showPicker.value = true
  await nextTick()
  searchInput.value?.focus()
}

function closePicker() {
  showPicker.value = false
}

/**
 * Chọn 1 SP trong modal:
 * - emit select-product cho cha (loadVariants)
 * - đóng modal
 */
function pickProduct(product) {
  emit('select-product', product)
  closePicker()
}
</script>

<style scoped>
.variant-scroll {
  max-height: 320px;
  overflow-y: auto;
}
.picker-list {
  max-height: 420px;
  overflow-y: auto;
}
.min-w-0 {
  min-width: 0;
}
</style>
