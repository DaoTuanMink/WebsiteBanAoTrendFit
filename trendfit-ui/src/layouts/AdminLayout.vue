<template>
  <div class="admin-wrapper">
    <aside class="sidebar">
      <div class="p-3 fs-4 fw-bold text-white border-bottom border-secondary">TrendFit Admin</div>
      <nav class="nav flex-column p-2">
        <router-link to="/admin/products" class="nav-link text-white">Quản lý Sản phẩm</router-link>
        <router-link to="/admin/categories" class="nav-link text-white"
          >Quản lý Danh mục</router-link
        >
        <router-link to="/admin/brands" class="nav-link text-white"
          >Quản lý Thương hiệu</router-link
        >
        <router-link to="/admin/orders" class="nav-link text-white">Quản lý Đơn hàng</router-link>
      </nav>
    </aside>

    <main class="content">
      <router-view />
    </main>
  </div>
</template>

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
</style>
