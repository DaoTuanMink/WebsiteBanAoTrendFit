<template>
  <LayoutHeader />
  <div class="cart-view container py-5">
    <h2 class="fw-bold mb-4">GIỎ HÀNG CỦA BẠN</h2>

    <div v-if="cart.length === 0" class="text-center py-5">
      <p>Giỏ hàng trống.</p>
      <router-link to="/ao" class="btn btn-dark">Mua sắm ngay</router-link>
    </div>

    <div v-else class="row">
      <div class="col-md-8">
        <!-- Nút Chọn tất cả -->
        <div
          class="mb-3 d-flex align-items-center"
          style="cursor: pointer"
          @click="toggleSelectAll"
        >
          <div class="custom-checkbox-btn me-2" :class="{ checked: isAllSelected }">
            <svg v-if="isAllSelected" class="checkmark" viewBox="0 0 24 24">
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
          </div>
          <span class="fw-bold">Chọn tất cả ({{ cart.length }})</span>
        </div>

        <table class="table align-middle">
          <thead class="table-light">
            <tr>
              <th style="width: 50px">Chọn</th>
              <th>Sản phẩm</th>
              <th>Chi tiết</th>
              <th>Đơn giá</th>
              <th>Số lượng</th>
              <th>Thành tiền</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in cart" :key="index">
              <td>
                <!-- Dùng thẻ div custom làm nút tích chọn giống hệt ảnh mẫu, màu xanh dương -->
                <div
                  class="custom-checkbox-btn"
                  :class="{ checked: item.selected }"
                  @click="toggleItemSelection(index)"
                >
                  <svg v-if="item.selected" class="checkmark" viewBox="0 0 24 24">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                </div>
              </td>
              <td>
                <div class="d-flex align-items-center">
                  <img
                    :src="item.anhChinh || 'https://via.placeholder.com/50'"
                    width="50"
                    class="me-2 rounded"
                  />
                  <span>{{ item.ten }}</span>
                </div>
              </td>
              <td>{{ item.mauSac }} / {{ item.kichCoSize }}</td>
              <td>{{ formatPrice(item.gia) }}</td>
              <td>
                <div class="btn-group btn-group-sm">
                  <button @click="updateQuantity(index, -1)" class="btn btn-outline-secondary">
                    -
                  </button>
                  <span class="px-3 border d-flex align-items-center">{{ item.quantity }}</span>
                  <button @click="updateQuantity(index, 1)" class="btn btn-outline-secondary">
                    +
                  </button>
                </div>
              </td>
              <td class="fw-bold">{{ formatPrice(item.gia * item.quantity) }}</td>
              <td>
                <button @click="removeItem(index)" class="btn btn-sm btn-outline-danger">
                  Xóa
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="col-md-4">
        <div class="card p-4 shadow-sm border-0 bg-light">
          <h5 class="mb-3">Thanh toán</h5>
          <div class="d-flex justify-content-between mb-3 fw-bold h5">
            <span>Đã chọn ({{ selectedCount }}):</span>
            <span class="text-danger">{{ formatPrice(selectedTotalPrice) }}</span>
          </div>
          <button
            @click="proceedToCheckout"
            class="btn btn-dark w-100 py-3 text-uppercase fw-bold"
            :disabled="selectedCount === 0"
          >
            MUA HÀNG ({{ selectedCount }})
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import LayoutHeader from '@/components/LayoutHeader.vue'
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const cart = ref([])
const userId = localStorage.getItem('user_id')

onMounted(async () => {
  if (userId) {
    try {
      const res = await axios.get(`http://localhost:8080/api/public/cart/${userId}`)
      cart.value = res.data.map((i) => ({ ...i, selected: i.selected ?? false }))
    } catch (err) {
      console.error('Lỗi tải giỏ hàng:', err)
      cart.value = JSON.parse(localStorage.getItem('cart') || '[]').map((i) => ({
        ...i,
        selected: i.selected ?? false,
      }))
    }
  } else {
    cart.value = JSON.parse(localStorage.getItem('cart') || '[]').map((i) => ({
      ...i,
      selected: i.selected ?? false,
    }))
  }
})

const saveCart = async () => {
  localStorage.setItem('cart', JSON.stringify(cart.value))
  if (userId) {
    try {
      const payload = {
        userId: Number(userId),
        items: cart.value.map((i) => ({ bienTheId: i.bienTheId, quantity: i.quantity })),
      }
      await axios.post('http://localhost:8080/api/public/cart/sync', payload)
    } catch (err) {
      console.error('Lỗi đồng bộ:', err)
    }
  }
}

// Hàm click vào từng nút chọn sản phẩm
const toggleItemSelection = (index) => {
  cart.value[index].selected = !cart.value[index].selected
  saveCart()
}

// Checkbox "Chọn tất cả"
const isAllSelected = computed(() => {
  return cart.value.length > 0 && cart.value.every((i) => i.selected)
})

const toggleSelectAll = () => {
  const newState = !isAllSelected.value
  cart.value.forEach((i) => (i.selected = newState))
  saveCart()
}

const selectedCount = computed(() => cart.value.filter((i) => i.selected).length)

const selectedTotalPrice = computed(() =>
  cart.value
    .filter((i) => i.selected)
    .reduce((sum, item) => sum + Number(item.gia) * Number(item.quantity), 0),
)

const updateQuantity = (index, delta) => {
  cart.value[index].quantity += delta
  if (cart.value[index].quantity < 1) cart.value[index].quantity = 1
  saveCart()
}

const removeItem = (index) => {
  cart.value.splice(index, 1)
  saveCart()
}

const proceedToCheckout = () => {
  const itemsToCheckout = cart.value.filter((i) => i.selected)
  if (itemsToCheckout.length === 0)
    return alert('Vui lòng chọn ít nhất một sản phẩm để thanh toán!')

  sessionStorage.setItem('checkout_items', JSON.stringify(itemsToCheckout))
  router.push('/checkout')
}

const formatPrice = (v) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)
</script>

<style scoped>
/* Thiết kế nút chọn dạng hình vuông bo góc giống hệt ảnh mẫu */
.custom-checkbox-btn {
  width: 24px;
  height: 24px;
  border: 2px solid #ccc;
  border-radius: 4px;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
}

.custom-checkbox-btn:hover {
  border-color: #0d6efd; /* Màu xanh dương chủ đạo của trang */
}

/* Khi được chọn (checked) -> Đổi sang nền xanh dương chuẩn giao diện web */
.custom-checkbox-btn.checked {
  background-color: #0d6efd;
  border-color: #0d6efd;
}

/* Vẽ dấu tích trắng bên trong khi được chọn */
.checkmark {
  width: 14px;
  height: 14px;
  stroke: white;
  stroke-width: 3;
  fill: none;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.cursor-pointer {
  cursor: pointer;
}
</style>
