<template>
  <div class="container-fluid py-4">
    <div class="d-flex justify-content-between mb-4">
      <h3>Bán hàng tại quầy (POS)</h3>
      <button class="btn btn-outline-primary" @click="loadProducts">Tải lại</button>
    </div>

    <div class="row">
      <div class="col-md-7">
        <input v-model="keyword" class="form-control mb-3" placeholder="Tìm sản phẩm..." />
        <div class="row g-2">
          <div v-for="p in filteredProducts" :key="p.id" class="col-md-4">
            <div class="card p-2 text-center" style="cursor: pointer" @click="loadVariants(p)">
              <p class="mb-0 fw-bold">{{ p.ten }}</p>
            </div>
          </div>
        </div>

        <div v-if="selectedProduct" class="mt-4 p-3 border rounded">
          <h5>Biến thể: {{ selectedProduct.ten }}</h5>
          <div
            v-for="v in variants"
            :key="v.id"
            class="d-flex justify-content-between p-2 border-bottom"
          >
            <span
              >{{ v.kichCo?.tenKichCo }} - {{ v.mauSac?.tenMau }} (Tồn: {{ v.soLuongTon }})</span
            >
            <button
              class="btn btn-sm btn-primary"
              :disabled="v.soLuongTon <= 0"
              @click="addToCart(selectedProduct, v)"
            >
              Thêm
            </button>
          </div>
        </div>
      </div>

      <div class="col-md-5">
        <div class="card p-3">
          <h5>Đơn hàng</h5>
          <table class="table">
            <tr v-for="item in cart" :key="item.bienTheId">
              <td>
                {{ item.ten }}<br />
                <small class="text-muted">{{ item.kichCoSize }} - {{ item.mauSac }}</small>
              </td>
              <td><input type="number" v-model="item.quantity" style="width: 50px" /></td>
              <td>{{ formatMoney(item.gia * item.quantity) }}</td>
              <td>
                <button class="btn btn-sm btn-danger" @click="removeItem(item.bienTheId)">x</button>
              </td>
            </tr>
          </table>
          <hr />
          <div class="d-flex justify-content-between align-items-center">
            <strong>Tổng: {{ formatMoney(totalAmount) }}</strong>
            <button class="btn btn-success" @click="checkout">Thanh toán</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'

const API_BASE = 'http://localhost:8080/api'
const products = ref([]),
  variants = ref([]),
  selectedProduct = ref(null),
  keyword = ref(''),
  cart = ref([])

async function loadProducts() {
  try {
    const res = await fetch(`${API_BASE}/admin/products`)
    const data = await res.json()
    console.log('Danh sách sản phẩm từ API:', data) // Kiểm tra log này ở F12 Console
    products.value = data
  } catch (error) {
    console.error('Lỗi khi fetch:', error)
  }
}

async function loadVariants(product) {
  selectedProduct.value = product
  const res = await fetch(`${API_BASE}/admin/products/${product.id}/variants`)
  variants.value = await res.json()
}

const filteredProducts = computed(() => {
  if (!products.value) return []
  return products.value.filter((p) => p.ten?.toLowerCase().includes(keyword.value.toLowerCase()))
})

function addToCart(product, variant) {
  const existed = cart.value.find((i) => i.bienTheId === variant.id)
  if (existed) {
    if (existed.quantity < variant.soLuongTon) existed.quantity++
  } else {
    cart.value.push({
      bienTheId: variant.id,
      ten: product.ten,
      kichCoSize: variant.kichCo?.tenKichCo || 'N/A',
      mauSac: variant.mauSac?.tenMau || 'N/A',
      soLuongTon: variant.soLuongTon,
      quantity: 1,
      gia: variant.giaSale || variant.gia || 0,
    })
  }
}

function removeItem(id) {
  cart.value = cart.value.filter((i) => i.bienTheId !== id)
}

const totalAmount = computed(() => cart.value.reduce((sum, i) => sum + i.gia * i.quantity, 0))

async function checkout() {
  if (!cart.value.length) return alert('Giỏ hàng trống')

  const payload = {
    hoTen: customerName.value || 'Khách lẻ',
    sdt: customerPhone.value || '0000000000',
    diaChi: 'Bán tại quầy',
    phuongThucThanhToan: paymentMethod.value || 'TIEN_MAT',
    // Đảm bảo ép kiểu Number cho các trường BigDecimal
    tongTienHang: Number(totalAmount.value),
    tienGiam: Number(discountAmount.value || 0),
    tongThanhToan: Number(totalPayable.value),
    tienKhachDua: Number(cashReceived.value || 0),
    tienThua: Number(changeAmount.value || 0),
    voucherId: appliedVoucher.value?.id || null,
    maVoucher: appliedVoucher.value?.ma || null,
    userId: null,
    creatorId: 1, // Nên lấy từ localStorage.getItem('user_id')
    items: cart.value.map((i) => ({
      bienTheId: Number(i.bienTheId),
      quantity: Number(i.quantity),
      ten: i.ten,
      gia: Number(i.gia),
    })),
  }

  try {
    const res = await fetch(`${API_BASE}/admin/pos-orders`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + localStorage.getItem('token'), // Nếu có bảo mật
      },
      body: JSON.stringify(payload),
    })

    if (res.ok) {
      alert('Thanh toán thành công!')
      cart.value = []
    } else {
      const errorText = await res.text()
      alert('Lỗi: ' + errorText) // Xem lỗi cụ thể từ Backend trả về
    }
  } catch (err) {
    console.error(err)
    alert('Không thể kết nối tới server')
  }
}

function formatMoney(v) {
  return Number(v).toLocaleString('vi-VN') + ' đ'
}

onMounted(loadProducts)
</script>
