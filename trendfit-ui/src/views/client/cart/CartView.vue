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
          <h5 class="mb-3">Tổng thanh toán</h5>
          <div class="d-flex justify-content-between mb-3 fw-bold h5">
            <span>Tạm tính:</span>
            <span class="text-danger">{{ formatPrice(totalPrice) }}</span>
          </div>
          <button @click="proceedToCheckout" class="btn btn-dark w-100 py-3 text-uppercase fw-bold">
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
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const cart = ref([])
const userId = localStorage.getItem('user_id') // Lấy ID tài khoản đang đăng nhập

// 1. Khi load trang: ưu tiên lấy từ Database nếu có tài khoản, nếu không thì lấy LocalStorage
onMounted(async () => {
  if (userId) {
    try {
      const res = await axios.get(`http://localhost:8080/api/public/cart/${userId}`)
      cart.value = res.data
    } catch (err) {
      console.error('Lỗi tải giỏ hàng từ server:', err)
      // Fallback về localStorage nếu lỗi mạng
      cart.value = JSON.parse(localStorage.getItem('cart') || '[]')
    }
  } else {
    cart.value = JSON.parse(localStorage.getItem('cart') || '[]')
  }
})

// 2. Hàm lưu giỏ hàng: Đồng thời lưu LocalStorage và đẩy lên Database nếu có tài khoản
const saveCart = async () => {
  // Luôn lưu bản sao vào localStorage để dự phòng
  localStorage.setItem('cart', JSON.stringify(cart.value))

  // Nếu người dùng có tài khoản, đồng bộ ngầm lên Database
  if (userId) {
    const payload = {
      userId: Number(userId),
      items: cart.value.map((i) => ({
        bienTheId: i.bienTheId,
        quantity: i.quantity,
      })),
    }
    try {
      await axios.post('http://localhost:8080/api/public/cart/sync', payload)
    } catch (err) {
      console.error('Lỗi đồng bộ giỏ hàng lên server:', err)
    }
  }
}

const totalPrice = computed(() =>
  cart.value.reduce((sum, item) => sum + Number(item.gia) * Number(item.quantity), 0),
)

const updateQuantity = (index, delta) => {
  cart.value[index].quantity += delta
  if (cart.value[index].quantity < 1) {
    cart.value[index].quantity = 1
  }
  saveCart()
}

const removeItem = (index) => {
  cart.value.splice(index, 1)
  saveCart()
}

const proceedToCheckout = () => {
  // Trước khi sang trang thanh toán, đảm bảo giỏ hàng mới nhất đã được lưu
  saveCart()
  router.push('/checkout')
}

const formatPrice = (v) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)
</script>
