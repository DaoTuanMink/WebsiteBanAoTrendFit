<template>
  <div class="admin-orders bg-white min-vh-100">
    <div class="container py-5 text-start">
      <h3 class="fw-bold text-dark mb-2">QUẢN LÝ HÓA ĐƠN & ĐƠN HÀNG</h3>

      <div class="mb-4">
        <button class="btn btn-sm btn-dark me-2" @click="fetchOrders('all')">Tất cả đơn</button>
        <button class="btn btn-sm btn-outline-dark" @click="fetchOrders('null-user')">
          Đơn vãng lai (null user)
        </button>
      </div>

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
          <tbody class="text-start">
            <tr v-for="item in danhSachDonHang" :key="item.donHang.id">
              <td>
                <strong>#{{ item.donHang.id }}</strong
                ><br />
                <small class="text-muted">{{ formatDate(item.donHang.ngayDat) }}</small>
              </td>
              <td>
                <strong>{{ item.donHang.tenNguoiNhan }}</strong
                ><br />
                <i class="bi bi-telephone"></i> {{ item.donHang.soDienThoaiGiao }}<br />
                <small>{{ item.donHang.diaChiGiao }}</small>
              </td>
              <td>
                <ul class="list-unstyled mb-0 small">
                  <li v-for="ct in item.chiTietDonHangs" :key="ct.id">
                    {{ ct.soLuong }}x {{ ct.tenSanPham }} ({{ ct.kichCoSize }} / {{ ct.mauSac }})
                  </li>
                </ul>
              </td>
              <td class="text-end">
                <div class="text-danger fw-bold">{{ formatPrice(item.donHang.tongThanhToan) }}</div>
                <small class="text-muted"
                  >Phí ship: {{ formatPrice(item.donHang.phiVanChuyen) }}</small
                >
              </td>
              <td>
                <span class="badge w-100 mb-1" :class="getStatusClass(item.donHang.trangThai)">
                  {{ item.donHang.trangThai }}
                </span>
                <select
                  class="form-select form-select-sm"
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
                <button class="btn btn-sm btn-outline-dark" @click="printInvoice(item)">
                  In Hóa Đơn
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

// Lấy dữ liệu với tham số lọc
const fetchOrders = async (type = 'all') => {
  loading.value = true
  try {
    // Gọi API tương ứng với tab Admin đã chọn
    let url = 'http://localhost:8080/api/admin/orders'
    if (type === 'null-user') {
      url = 'http://localhost:8080/api/admin/orders/null-user'
    }
    const res = await axios.get(url)
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
      <td style="padding: 8px; border-bottom: 1px solid #ddd;">${d.tenSanPham}</td>
      <td style="text-align: center;">${d.kichCoSize} / ${d.mauSac}</td>
      <td style="text-align: center;">${d.soLuong}</td>
      <td style="text-align: right;">${formatPrice(d.donGia)}</td>
      <td style="text-align: right;">${formatPrice(d.soLuong * d.donGia)}</td>
    </tr>
  `,
    )
    .join('')

  const printContent = `
    <div style="font-family: Arial, sans-serif; padding: 20px;">
      <h1 style="text-align: center;">HÓA ĐƠN ĐẶT HÀNG</h1>
      <hr>
      <div style="display: flex; justify-content: space-between;">
        <div>
          <p><strong>Khách hàng:</strong> ${order.tenNguoiNhan}</p>
          <p><strong>SĐT:</strong> ${order.soDienThoaiGiao}</p>
          <p><strong>Địa chỉ:</strong> ${order.diaChiGiao}</p>
        </div>
        <div>
          <p><strong>Mã đơn:</strong> #${order.id}</p>
          <p><strong>Ngày đặt:</strong> ${formatDate(order.ngayDat)}</p>
          <p><strong>Thanh toán:</strong> ${order.phuongThucThanhToan || 'Tiền mặt'}</p>
        </div>
      </div>
      <table style="width: 100%; border-collapse: collapse; margin-top: 20px;">
        <thead style="background: #eee;">
          <tr><th style="text-align: left; padding: 8px;">Sản phẩm</th><th>Size/Màu</th><th>SL</th><th>Đơn giá</th><th>Thành tiền</th></tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>
      <div style="margin-top: 20px; text-align: right;">
        <p>Tạm tính: ${formatPrice(order.tongTienHang)}</p>
        <p>Giảm giá: ${formatPrice(order.tienGiam)}</p>
        <h2 style="color: red;">Tổng thanh toán: ${formatPrice(order.tongThanhToan)}</h2>
      </div>
      <p style="margin-top: 30px; font-style: italic;">Ghi chú: ${order.ghiChu || 'Không có'}</p>
    </div>
  `
  print({ printable: printContent, type: 'raw-html' })
}

const formatPrice = (v) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)
const formatDate = (dateString) => {
  if (!dateString) return 'Chưa có ngày'
  const options = {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }
  return new Date(dateString).toLocaleDateString('vi-VN', options)
}

onMounted(() => fetchOrders('all'))
</script>
