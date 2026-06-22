<template>
  <div class="admin-orders bg-white min-vh-100">
    <div class="container py-5 text-start">
      <h3 class="fw-bold text-dark mb-2">QUẢN LÝ HÓA ĐƠN & ĐƠN HÀNG</h3>
      <p class="text-muted small mb-4">Nghiệp vụ: Kiểm duyệt và chuyển trạng thái đơn hàng</p>

      <div v-if="loading" class="text-center">Đang tải dữ liệu...</div>

      <div class="table-responsive" v-else>
        <table class="table table-bordered align-middle text-center">
          <thead class="table-dark">
            <tr>
              <th>Mã Đơn</th>
              <th>Khách Hàng</th>
              <th>Tổng Tiền</th>
              <th>Trạng Thái</th>
              <th>Hành Động</th>
              <th>In Ấn</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in danhSachDonHang" :key="item.donHang.id">
              <td class="fw-bold">#{{ item.donHang.id }}</td>
              <td>{{ item.donHang.tenNguoiNhan }}</td>
              <td class="text-danger fw-bold">{{ formatPrice(item.donHang.tongThanhToan) }}</td>
              <td>
                <span class="badge" :class="getStatusClass(item.donHang.trangThai)">
                  {{ item.donHang.trangThai }}
                </span>
              </td>
              <td>
                <select
                  class="form-select form-select-sm d-inline-block w-auto"
                  @change="capNhatTrangThai(item.donHang.id, $event.target.value)"
                  :value="item.donHang.trangThai"
                >
                  <option value="CHO_XAC_NHAN">Chờ xác nhận</option>
                  <option value="DA_XAC_NHAN">Đã xác nhận</option>
                  <option value="DANG_VAN_CHUYEN">Đang vận chuyển</option>
                  <option value="DA_THANH_CONG">Đã thành công</option>
                  <option value="DA_HUY">Đã hủy</option>
                </select>
              </td>
              <td>
                <button
                  v-if="item.donHang.trangThai === 'DA_THANH_CONG'"
                  @click="printInvoice(item)"
                  class="btn btn-sm btn-outline-primary"
                >
                  In hóa đơn
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import print from 'print-js'

const danhSachDonHang = ref([])
const loading = ref(true)

const fetchOrders = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/admin/orders')
    danhSachDonHang.value = res.data
  } catch (err) {
    console.error('Lỗi tải đơn hàng:', err)
  } finally {
    loading.value = false
  }
}

const capNhatTrangThai = async (id, status) => {
  if (!confirm('Bạn có chắc muốn chuyển trạng thái đơn hàng này?')) return
  try {
    await axios.put(`http://localhost:8080/api/admin/orders/${id}/status?status=${status}`)
    alert('Cập nhật thành công!')
    fetchOrders()
  } catch (err) {
    alert('Lỗi cập nhật: ' + (err.response?.data || err.message))
  }
}

const getStatusClass = (status) => {
  const classes = {
    CHO_XAC_NHAN: 'bg-warning text-dark',
    DA_XAC_NHAN: 'bg-info',
    DANG_VAN_CHUYEN: 'bg-primary',
    DA_THANH_CONG: 'bg-success',
    DA_HUY: 'bg-danger',
  }
  return classes[status] || 'bg-secondary'
}

const printInvoice = (item) => {
  const order = item.donHang
  const details = item.chiTietDonHangs

  const rows = details
    .map(
      (d) => `
    <tr>
      <td style="border-bottom: 1px solid #eee; padding: 8px;">${d.tenSanPham}</td>
      <td style="border-bottom: 1px solid #eee; padding: 8px; text-align: center;">${d.kichCoSize} / ${d.mauSac}</td>
      <td style="border-bottom: 1px solid #eee; padding: 8px; text-align: center;">${d.soLuong}</td>
      <td style="border-bottom: 1px solid #eee; padding: 8px; text-align: right;">${new Intl.NumberFormat('vi-VN').format(d.donGia)} đ</td>
    </tr>
  `,
    )
    .join('')

  const printContent = `
    <div style="font-family: sans-serif; padding: 40px; max-width: 600px; margin: auto;">
      <h2 style="text-align: center;">HÓA ĐƠN BÁN HÀNG - TRENDFIT</h2>
      <p><strong>Mã đơn:</strong> #${order.id}</p>
      <p><strong>Khách hàng:</strong> ${order.tenNguoiNhan}</p>
      <p><strong>Địa chỉ:</strong> ${order.diaChiGiao}</p>
      <table style="width: 100%; border-collapse: collapse; margin-top: 20px;">
        <thead>
          <tr style="background: #f4f4f4;">
            <th style="padding: 10px; text-align: left;">Sản phẩm</th>
            <th style="padding: 10px;">Size/Màu</th>
            <th style="padding: 10px;">SL</th>
            <th style="padding: 10px; text-align: right;">Đơn giá</th>
          </tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>
      <h3 style="text-align: right; margin-top: 20px;">Tổng thanh toán: ${new Intl.NumberFormat('vi-VN').format(order.tongThanhToan)} đ</h3>
      <p style="text-align: center; margin-top: 50px; font-style: italic;">Cảm ơn quý khách đã mua sắm tại TrendFit!</p>
    </div>
  `

  print({ printable: printContent, type: 'raw-html' })
}

const formatPrice = (v) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)

onMounted(fetchOrders)
</script>
