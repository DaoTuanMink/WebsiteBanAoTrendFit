<template>
  <div class="container mt-4 mb-5">
    <h2 class="mb-4">Lịch sử hoạt động của tôi</h2>

    <!-- ===================== TỔNG QUAN HOẠT ĐỘNG (Việt) ===================== -->
    <div class="row g-3 mb-4">
      <div class="col-6 col-md-3">
        <div class="card text-center shadow-sm h-100">
          <div class="card-body">
            <div class="text-muted small">Số đơn đã mua thành công</div>
            <div class="fs-3 fw-bold">{{ summary?.tongSoDonHang ?? 0 }}</div>
          </div>
        </div>
      </div>
      <div class="col-6 col-md-3">
        <div class="card text-center shadow-sm h-100">
          <div class="card-body">
            <div class="text-muted small">Tổng số tiền đã chi tiêu</div>
            <div class="fs-4 fw-bold text-danger">
              {{ formatMoney(summary?.tongTienDaChi) }}
            </div>
          </div>
        </div>
      </div>
      <div class="col-12 col-md-6">
        <div class="card shadow-sm h-100">
          <div class="card-body">
            <div class="text-muted small mb-2">Sản phẩm bạn mua nhiều nhất</div>
            <div v-if="!summary?.sanPhamMuaNhieuNhat?.length" class="text-muted">
              Chưa có dữ liệu mua hàng.
            </div>
            <ol v-else class="mb-0 ps-3">
              <li v-for="sp in summary.sanPhamMuaNhieuNhat" :key="sp.sanPhamId ?? sp.tenSanPham">
                <strong>{{ sp.tenSanPham }}</strong>
                — đã mua {{ sp.soLuongDaMua }} sản phẩm
                (tổng {{ formatMoney(sp.tongTienDaChi) }})
              </li>
            </ol>
          </div>
        </div>
      </div>
    </div>
    <!-- ======================================================================= -->

    <h5 class="mb-3">Danh sách đơn hàng đã hoàn thành</h5>

    <div v-if="orders.length === 0" class="alert alert-info">
      Bạn chưa có đơn hàng nào đã hoàn thành.
    </div>

    <table v-else class="table table-bordered table-hover">
      <thead class="table-dark">
        <tr>
          <th>Mã đơn</th>
          <th>Ngày đặt</th>
          <th>Người nhận</th>
          <th>Thanh toán</th>
          <th>Tổng tiền</th>
          <th>Trạng thái</th>
        </tr>
      </thead>

      <tbody>
        <tr v-for="order in orders" :key="order.id">
          <td>{{ order.maDonHang }}</td>
          <td>{{ formatDate(order.ngayDat) }}</td>
          <td>{{ order.tenNguoiNhan }}</td>
          <td>{{ order.phuongThucThanhToan }}</td>
          <td>{{ formatMoney(order.tongThanhToan) }}</td>
          <td>
            <span class="badge bg-success">
              {{ order.trangThai }}
            </span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import axios from "axios";
import { onMounted, ref } from "vue";
import API_BASE from "@/config/api";

const orders = ref([]);
const summary = ref(null);

const userId = localStorage.getItem("user_id");

const loadHistory = async () => {
  try {
    const res = await axios.get(
      `${API_BASE}/api/history-order/${userId}`
    );

    orders.value = res.data;
  } catch (error) {
    console.error(error);
  }
};

const loadSummary = async () => {
  try {
    const res = await axios.get(
      `${API_BASE}/api/history-order/${userId}/summary`
    );

    summary.value = res.data;
  } catch (error) {
    console.error(error);
  }
};

const formatMoney = (money) => {
  if (!money) return "0 đ";

  return Number(money).toLocaleString("vi-VN") + " đ";
};

const formatDate = (date) => {
  if (!date) return "";

  return new Date(date).toLocaleString("vi-VN");
};

onMounted(() => {
  loadHistory();
  loadSummary();
});
</script>
