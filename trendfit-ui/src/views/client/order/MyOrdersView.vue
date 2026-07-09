<template>
  <LayoutHeader />
  <div class="container py-5">
    <h2 class="fw-bold mb-4">ĐƠN HÀNG CỦA TÔI</h2>

    <div class="nav nav-tabs mb-4">
      <button
        v-for="tab in [
          'TẤT CẢ',
          'CHO_XAC_NHAN',
          'DA_XAC_NHAN',
          'DANG_VAN_CHUYEN',
          'DA_THANH_CONG',
          'DA_HUY',
        ]"
        :key="tab"
        class="nav-link"
        :class="{ active: currentTab === tab }"
        @click="currentTab = tab"
      >
        {{ tab.replace('_', ' ') }}
      </button>
    </div>

    <div v-if="loading" class="text-center py-5">Đang tải...</div>
    <div v-else-if="filteredOrders.length === 0" class="text-center py-5">
      Không có đơn hàng nào!
    </div>

    <div v-else v-for="item in filteredOrders" :key="item.donHang.id" class="card shadow-sm mb-4">
      <div class="card-header d-flex justify-content-between bg-light">
        <span
          >Mã đơn: <strong>#{{ item.donHang.id }}</strong></span
        >
        <span class="badge" :class="getStatusClass(item.donHang.trangThai)">{{
          item.donHang.trangThai
        }}</span>
      </div>

      <div class="card-body">
        <div v-for="ct in item.chiTietDonHangs" :key="ct.id" class="d-flex mb-3">
          <div class="ms-3">
            <h6 class="mb-0">{{ ct.tenSanPham }}</h6>
            <small class="text-muted"
              >Size: {{ ct.kichCoSize || 'N/A' }} | Màu: {{ ct.mauSac || 'N/A' }}</small
            ><br />
            <small>x{{ ct.soLuong }} - {{ formatPrice(ct.donGia) }}</small>
          </div>
        </div>
      </div>

      <div class="card-footer d-flex justify-content-between align-items-center">
        <span
          >Tổng tiền:
          <strong class="text-danger">{{ formatPrice(item.donHang.tongThanhToan) }}</strong></span
        >
        <button
          v-if="item.donHang.trangThai === 'CHO_XAC_NHAN'"
          @click="huyDonHang(item.donHang.id)"
          class="btn btn-sm btn-outline-danger"
        >
          Hủy đơn hàng
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import LayoutHeader from '@/components/LayoutHeader.vue'

const allOrders = ref([])
const loading = ref(true)
const currentTab = ref('TẤT CẢ')
const userId = localStorage.getItem('user_id')

const filteredOrders = computed(() => {
  if (currentTab.value === 'TẤT CẢ') return allOrders.value
  return allOrders.value.filter((o) => o.donHang.trangThai === currentTab.value)
})

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await axios.get(`http://localhost:8080/api/public/orders/user/${userId}`)
    allOrders.value = res.data
  } catch (e) {
    console.error(e)
    alert('Không tải được đơn hàng')
  } finally {
    loading.value = false
  }
}

const huyDonHang = async (id) => {
  if (!confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) return
  try {
    // Gọi API cập nhật trạng thái sang DA_HUY
    await axios.put(`http://localhost:8080/api/admin/orders/${id}/status?status=DA_HUY`)
    alert('Đã hủy đơn hàng thành công!')
    loadOrders()
  } catch (e) {
    alert('Không thể hủy đơn hàng')
  }
}

const formatPrice = (v) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)
const getStatusClass = (status) => {
  const map = {
    CHO_XAC_NHAN: 'bg-warning text-dark',
    DA_XAC_NHAN: 'bg-info',
    DANG_VAN_CHUYEN: 'bg-primary',
    DA_THANH_CONG: 'bg-success',
    DA_HUY: 'bg-danger',
  }
  return map[status] || 'bg-secondary'
}

onMounted(loadOrders)
</script>
