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

  // Lấy thông tin từ LocalStorage
  const idFromStorage = localStorage.getItem('user_id')
  const roleFromStorage = localStorage.getItem('user_role') // Giả sử bạn lưu role khi login
  const numericId = idFromStorage ? parseInt(idFromStorage) : null

  const payload = {
    ...form.value,
    tongTienHang: totalPrice.value, // Đảm bảo truyền đủ tiền hàng
    tongThanhToan: finalPrice.value,
    tienGiam: giamGia.value,
    voucherId: appliedVoucher.value ? appliedVoucher.value.id : null,

    // Logic gán ID tùy theo vai trò
    // Nếu là Admin(1) hoặc NV(2) -> gán vào creatorId
    // Nếu là Khách hàng(3+) -> gán vào userId
    creatorId: roleFromStorage === 'ADMIN' || roleFromStorage === 'EMPLOYEE' ? numericId : null,
    userId: roleFromStorage === 'CUSTOMER' ? numericId : null,

    items: cart.value.map((i) => ({
      bienTheId: i.bienTheId,
      quantity: i.quantity,
      ten: i.ten,
      gia: i.gia,
    })),
  }

  console.log('Payload gửi đi:', JSON.stringify(payload))

  try {
    await axios.post('http://localhost:8080/api/public/orders', payload)
    localStorage.removeItem('cart')
    alert('Đặt hàng thành công!')
    router.push('/')
  } catch (err) {
    console.error(err)
    alert('Có lỗi xảy ra: ' + (err.response?.data?.message || 'Vui lòng thử lại'))
  }
}

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
    // Gọi API kiểm tra (bạn cần viết API này ở VoucherAdminController hoặc một PublicController)
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
</script>
