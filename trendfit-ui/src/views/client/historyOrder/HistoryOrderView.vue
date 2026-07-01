<template>
  <div class="container mt-4">
    <h2 class="mb-4">Lịch sử đặt hàng</h2>

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

const userId = localStorage.getItem("user_id");

console.log("userId =", userId);

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

const formatMoney = (money) => {
  if (!money) return "0 đ";

  return Number(money).toLocaleString("vi-VN") + " đ";
};

const formatDate = (date) => {
  if (!date) return "";

  return new Date(date).toLocaleString("vi-VN");
};

onMounted(loadHistory);
</script>