<template>
  <div class="admin-wrapper">
    <aside class="sidebar d-flex flex-column">
      <div class="p-3 fs-4 fw-bold text-white border-bottom border-secondary">TrendFit Admin</div>

      <nav class="nav flex-column p-2 flex-grow-1">
        <router-link to="/admin/products" class="nav-link text-white">Quản lý Sản phẩm</router-link>
        <router-link to="/admin/categories" class="nav-link text-white"
          >Quản lý Danh mục</router-link
        >
        <router-link to="/admin/brands" class="nav-link text-white"
          >Quản lý Thương hiệu</router-link
        >
        <router-link to="/admin/orders" class="nav-link text-white">Quản lý Đơn hàng</router-link>
        <router-link to="/admin/ban-hang-tai-quay" class="nav-link text-white">Bán hàng tại quầy </router-link>
        <router-link to="/admin/dashboard"  class="nav-link text-white"> Thống Kê Doanh Số</router-link>
        <router-link to="/admin/vouchers" class="nav-link text-white">Quản lý Voucher</router-link>
      </nav>

      <div class="sidebar-footer p-3 border-top border-secondary">
        <div class="user-info text-white mb-3">
          <small class="text-secondary d-block"
            >Quyền: <span class="badge bg-success">Admin</span></small
          >
          <span class="fw-bold">{{ fullName }}</span>
        </div>

        <router-link to="/" class="btn btn-outline-light btn-sm w-100 mb-2">
          <i class="bi bi-house"></i> Về trang chủ
        </router-link>

        <button @click="dangXuat" class="btn btn-danger btn-sm w-100">
          <i class="bi bi-box-arrow-right"></i> Đăng xuất
        </button>
      </div>
    </aside>

    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const role = computed(() => localStorage.getItem('user_role') || 'GUEST')
const fullName = computed(() => localStorage.getItem('full_name') || 'Nhân viên')

const dangXuat = () => {
  if (confirm('Bạn muốn đăng xuất?')) {
    // 1. Xóa sạch mọi thứ
    localStorage.clear()

    // 2. Thay vì dùng router.push, hãy dùng window.location.href
    // để ép trình duyệt chuyển hướng hoàn toàn ra khỏi ứng dụng Vue
    window.location.href = '/'
  }
}
</script>

<style scoped>
.admin-wrapper {
  display: flex;
  min-height: 100vh;
}
.sidebar {
  width: 250px;
  background: #1e293b;
  color: white;
}
.content {
  flex: 1;
  padding: 20px;
  background-color: #f8fafc;
}
.nav-link:hover {
  background: #334155;
}
.sidebar-footer {
  background: #0f172a;
}
</style>
