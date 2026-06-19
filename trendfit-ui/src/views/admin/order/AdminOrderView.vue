<script setup>
import { ref } from 'vue'
import LayoutHeader from '@/components/LayoutHeader.vue'
import LayoutFooter from '@/components/LayoutFooter.vue'

// Dữ liệu giả định để hiển thị danh sách hóa đơn theo yêu cầu của cô
const danhSachDonHang = ref([
  { id: 101, khachHang: 'Nguyễn Văn A', tongTien: 495000, trangThai: 'Chờ duyệt' },
  { id: 102, khachHang: 'Trần Thị B', tongTien: 385000, trangThai: 'Đang giao' },
])
</script>

<template>
  <div class="admin-orders bg-white min-vh-100">
    <LayoutHeader />
    <div class="container py-5 text-start">
      <h3 class="fw-bold text-dark mb-2">QUẢN LÝ HÓA ĐƠN & ĐƠN HÀNG</h3>
      <p class="text-muted small mb-4">
        Nghiệp vụ: Nhân viên kiểm duyệt và chuyển trạng thái đơn hàng
      </p>

      <div class="table-responsive">
        <table class="table table-bordered align-middle text-center">
          <thead class="table-dark">
            <tr>
              <th>Mã Đơn</th>
              <th>Khách Hàng</th>
              <th>Tổng Tiền</th>
              <th>Trạng Thái</th>
              <th>Hành Động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in danhSachDonHang" :key="order.id">
              <td class="fw-bold">#{{ order.id }}</td>
              <td>{{ order.khachHang }}</td>
              <td class="text-danger fw-bold">{{ order.tongTien.toLocaleString() }} đ</td>
              <td>
                <span class="badge bg-warning text-dark">{{ order.trangThai }}</span>
              </td>
              <td>
                <button class="btn btn-sm btn-dark rounded-0">Duyệt đơn</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <LayoutFooter />
  </div>
</template>
