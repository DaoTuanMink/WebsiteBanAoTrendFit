<template>
  <div class="checkout-view container py-5">
    <h2 class="fw-bold mb-4">THANH TOÁN ĐƠN HÀNG</h2>
    <div class="row">
      <div class="col-md-7">
        <div class="card p-4 shadow-sm border-0">
          <h5 class="mb-3">Thông tin giao hàng</h5>
          <input
            v-model="form.hoTen"
            class="form-control mb-3"
            placeholder="Họ và tên người nhận"
          />
          <input v-model="form.sdt" class="form-control mb-3" placeholder="Số điện thoại" />
          <input v-model="form.diaChi" class="form-control mb-3" placeholder="Địa chỉ giao hàng" />

          <h5 class="mt-3">Phương thức thanh toán</h5>
          <select v-model="form.phuongThucThanhToan" class="form-select mb-3">
            <option value="COD">Thanh toán khi nhận hàng (COD)</option>
            <option value="CHUYEN_KHOAN">Chuyển khoản ngân hàng</option>
          </select>
        </div>
      </div>

      <div class="col-md-5">
        <div class="card p-4 shadow-sm bg-light border-0">
          <h5 class="mb-3">Đơn hàng của bạn</h5>
          <div
            v-for="(item, index) in cart"
            :key="index"
            class="d-flex justify-content-between mb-2"
          >
            <span>{{ item.ten }} (x{{ item.quantity }})</span>
            <span>{{ formatPrice(item.gia * item.quantity) }}</span>
          </div>
          <hr />
          <div class="d-flex justify-content-between fw-bold h5">
            <span>Tổng thanh toán:</span>
            <span class="text-danger">{{ formatPrice(totalPrice) }}</span>
          </div>
          <button @click="confirmOrder" class="btn btn-dark w-100 mt-3 py-3">
            XÁC NHẬN ĐẶT HÀNG
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const cart = ref(JSON.parse(localStorage.getItem('cart') || '[]'))
const form = ref({ hoTen: '', sdt: '', diaChi: '', phuongThucThanhToan: 'COD' })

const totalPrice = computed(() => cart.value.reduce((sum, i) => sum + i.gia * i.quantity, 0))

const formatPrice = (v) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)

const confirmOrder = async () => {
  if (!form.value.hoTen || !form.value.sdt || !form.value.diaChi) {
    return alert('Vui lòng điền đủ thông tin!')
  }

  const payload = {
    ...form.value,
    tongTien: totalPrice.value,
    items: cart.value.map((i) => ({
      bienTheId: i.bienTheId,
      quantity: i.quantity,
      ten: i.ten,
      gia: i.gia,
    })),
  }

  try {
    // Gọi API lưu đơn hàng
    await axios.post('http://localhost:8080/api/public/orders', payload)

    // Đặt hàng xong thì xóa giỏ hàng
    localStorage.removeItem('cart')
    alert('Đặt hàng thành công! Cảm ơn bạn đã mua hàng tại TrendFit.')
    router.push('/') // Về trang chủ
  } catch (err) {
    console.error(err)
    alert('Có lỗi xảy ra khi đặt hàng, vui lòng thử lại!')
  }
}
</script>
