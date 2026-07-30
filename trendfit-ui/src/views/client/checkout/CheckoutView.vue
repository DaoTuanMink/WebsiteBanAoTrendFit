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

          <div class="input-group mb-3">
            <input v-model="voucherCode" class="form-control" placeholder="Nhập mã giảm giá" />
            <button @click="apDungVoucher" class="btn btn-outline-secondary" type="button">
              Áp dụng
            </button>
          </div>

          <div v-if="appliedVoucher" class="d-flex justify-content-between text-success">
            <span>Giảm giá ({{ appliedVoucher.ma }}):</span>
            <span>-{{ formatPrice(giamGia) }}</span>
          </div>

          <div class="d-flex justify-content-between fw-bold h5">
            <span>Tổng thanh toán:</span>
            <span class="text-danger">{{ formatPrice(finalPrice) }}</span>
          </div>
        </div>
      </div>

      <div class="col-md-5">
        <div class="card p-4 shadow-sm bg-light border-0">
          <h5 class="mb-3">Đơn hàng của bạn (Sản phẩm đã chọn)</h5>

          <div v-if="cart.length === 0" class="text-muted small mb-3">
            Chưa có sản phẩm nào được chọn thanh toán.
            <router-link to="/cart">Quay lại giỏ hàng</router-link>
          </div>

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
          <button
            @click="confirmOrder"
            class="btn btn-dark w-100 mt-3 py-3"
            :disabled="cart.length === 0"
          >
            XÁC NHẬN ĐẶT HÀNG
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()

// 1. Chỉ đọc các sản phẩm được chọn từ trang Cart truyền sang qua sessionStorage
const cart = ref(JSON.parse(sessionStorage.getItem('checkout_items') || '[]'))

const form = ref({ hoTen: '', sdt: '', diaChi: '', phuongThucThanhToan: 'COD' })

const totalPrice = computed(() => cart.value.reduce((sum, i) => sum + i.gia * i.quantity, 0))

const formatPrice = (v) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)

const voucherCode = ref('')
const appliedVoucher = ref(null)

// Tính số tiền được giảm
const giamGia = computed(() => {
  if (!appliedVoucher.value) return 0
  const v = appliedVoucher.value
  if (v.loai === 'PERCENT') return (totalPrice.value * v.giaTriGiam) / 100
  return v.giaTriGiam // Trường hợp FIXED
})

// Tổng tiền sau giảm
const finalPrice = computed(() => totalPrice.value - giamGia.value)

const apDungVoucher = async () => {
  try {
    const res = await axios.post(`http://localhost:8080/api/public/vouchers/check`, {
      ma: voucherCode.value,
      tongDon: totalPrice.value,
    })
    appliedVoucher.value = res.data
    alert('Áp dụng mã thành công!')
  } catch (err) {
    alert(err.response?.data?.message || 'Mã không hợp lệ!')
  }
}

// 2. Xử lý đặt hàng và chỉ xóa những món đã mua khỏi giỏ hàng chung
const confirmOrder = async () => {
  if (!form.value.hoTen || !form.value.sdt || !form.value.diaChi) {
    return alert('Vui lòng điền đủ thông tin!')
  }

  if (cart.value.length === 0) {
    return alert('Không có sản phẩm nào để thanh toán!')
  }

  const idFromStorage = localStorage.getItem('user_id')
  const roleFromStorage = localStorage.getItem('user_role')
  const numericId = idFromStorage ? parseInt(idFromStorage) : null

  const payload = {
    ...form.value,
    tongTienHang: totalPrice.value,
    tongThanhToan: finalPrice.value,
    tienGiam: giamGia.value,
    voucherId: appliedVoucher.value ? appliedVoucher.value.id : null,
    creatorId: roleFromStorage === 'ADMIN' || roleFromStorage === 'EMPLOYEE' ? numericId : null,
    userId: roleFromStorage === 'CUSTOMER' ? numericId : null,
    items: cart.value.map((i) => ({
      bienTheId: i.bienTheId,
      quantity: i.quantity,
      ten: i.ten,
      gia: i.gia,
    })),
  }

  try {
    // Bước 1: Gửi request tạo đơn hàng lên Backend
    await axios.post('http://localhost:8080/api/public/orders', payload)

    // Bước 2: Lấy toàn bộ giỏ hàng gốc trên LocalStorage ra
    let fullCart = JSON.parse(localStorage.getItem('cart') || '[]')

    // Lọc bỏ những món vừa thanh toán, chỉ giữ lại những món KHÔNG ĐƯỢC CHỌN
    const checkedOutIds = cart.value.map((i) => i.bienTheId)
    const remainingCart = fullCart.filter((i) => !checkedOutIds.includes(i.bienTheId))

    // Bước 3: Cập nhật lại giỏ hàng còn lại vào LocalStorage
    localStorage.setItem('cart', JSON.stringify(remainingCart))

    // Bước 4: Đồng bộ giỏ hàng mới (đã loại bỏ món đã mua) lên Database
    if (numericId) {
      try {
        await axios.post('http://localhost:8080/api/public/cart/sync', {
          userId: numericId,
          items: remainingCart.map((i) => ({ bienTheId: i.bienTheId, quantity: i.quantity })),
        })
      } catch (syncErr) {
        console.error('Lỗi cập nhật giỏ hàng trên server:', syncErr)
      }
    }

    // Xóa bộ nhớ tạm thanh toán
    sessionStorage.removeItem('checkout_items')

    alert('Đặt hàng thành công!')
    router.push('/')
  } catch (err) {
    console.error(err)
    alert('Có lỗi xảy ra: ' + (err.response?.data?.message || 'Vui lòng thử lại'))
  }
}
</script>
