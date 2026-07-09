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
        <table class="table align-middle">
          <thead class="table-light">
            <tr>
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
                <div class="d-flex align-items-center">
                  <img :src="item.anhChinh" width="50" class="me-2" />
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
                  <span class="px-3 border">{{ item.quantity }}</span>
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
        <div class="card p-4 shadow-sm">
          <h5 class="mb-3">Tổng thanh toán</h5>
          <div class="d-flex justify-content-between mb-2">
            <span>Tạm tính:</span>
            <span>{{ formatPrice(totalPrice) }}</span>
          </div>
          <button @click="$router.push('/checkout')" class="btn btn-dark w-100 py-2">
            THANH TOÁN
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import LayoutHeader from '@/components/LayoutHeader.vue'
import { ref, computed, onMounted } from 'vue'

const cart = ref([])

onMounted(() => {
  cart.value = JSON.parse(localStorage.getItem('cart') || '[]')
})

const totalPrice = computed(() =>
  cart.value.reduce((sum, item) => sum + item.gia * item.quantity, 0),
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

const saveCart = () => {
  localStorage.setItem('cart', JSON.stringify(cart.value))
}

const formatPrice = (v) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)
</script>
