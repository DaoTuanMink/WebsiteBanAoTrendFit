<template>
  <div class="container py-5">
    <h2 class="fw-bold mb-4">ĐƠN HÀNG CỦA TÔI</h2>
    <div v-if="orders.length === 0" class="text-center">Chưa có đơn hàng nào!</div>
    <div v-else class="card shadow-sm">
      <table class="table table-hover">
        <thead class="table-light">
          <tr>
            <th>Mã đơn</th>
            <th>Ngày đặt</th>
            <th>Tổng tiền</th>
            <th>Trạng thái</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="o in orders" :key="o.id">
            <td class="fw-bold">#{{ o.id }}</td>
            <td>{{ new Date(o.ngayDat).toLocaleDateString() }}</td>
            <td class="text-danger">{{ formatPrice(o.tongThanhToan) }}</td>
            <td>
              <span class="badge" :class="getStatusClass(o.trangThai)">{{ o.trangThai }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const orders = ref([])
const userId = localStorage.getItem('user_id') // Giả sử bạn đã lưu user_id khi login

const loadOrders = async () => {
  try {
    const res = await axios.get(`http://localhost:8080/api/public/orders/user/${userId}`)
    orders.value = res.data
  } catch (e) {
    alert('Không tải được đơn hàng')
  }
}

const formatPrice = (v) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)

const getStatusClass = (status) => {
  const map = {
    CHO_XAC_NHAN: 'bg-warning',
    DA_XAC_NHAN: 'bg-info',
    DA_THANH_CONG: 'bg-success',
    DA_HUY: 'bg-danger',
  }
  return map[status] || 'bg-secondary'
}

onMounted(loadOrders)
</script>
