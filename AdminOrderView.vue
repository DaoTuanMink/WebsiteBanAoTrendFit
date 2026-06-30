<template>
  <div class="order-management">
    <h2>Quản lý đơn hàng</h2>
    <table>
      <tr v-for="order in orders" :key="order.id">
        <td>{{ order.code }}</td>
        <td>
          <select v-model="order.status" @change="updateStatus(order)">
            <option value="CHO_XAC_NHAN">Chờ xác nhận</option>
            <option value="DA_XAC_NHAN">Đã xác nhận</option>
            <option value="DANG_VAN_CHUYEN">Đang vận chuyển</option>
            <option value="DA_THANH_CONG">Đã thành công</option>
            <option value="DA_HUY">Đã hủy</option>
          </select>
        </td>
      </tr>
    </table>
  </div>
</template>
