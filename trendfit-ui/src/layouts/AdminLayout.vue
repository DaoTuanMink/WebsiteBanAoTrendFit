<template>
  <div class="admin-wrapper" style="display: flex">
    <aside class="sidebar" style="width: 250px; height: 100vh; background: #1e293b; color: white">
      <div class="p-3">TrendFit Admin</div>
      <nav class="nav flex-column p-2">
        <router-link to="/admin/products" class="nav-link text-white">Quản lý Sản phẩm</router-link>
        <router-link to="/admin/orders" class="nav-link text-white">Quản lý Đơn hàng</router-link>
      </nav>
    </aside>

    <main class="content" style="flex: 1; padding: 20px">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.admin-wrapper {
  display: flex;
  min-height: 100vh;
}

.admin-sidebar {
  width: 250px;
  background-color: #1e293b;
  color: white;
  padding: 20px;
}

.nav-menu a {
  display: block;
  color: #94a3b8;
  padding: 12px 0;
  text-decoration: none;
}

.nav-menu a:hover {
  color: white;
}

.admin-content {
  flex: 1;
  background-color: #f8fafc;
}

.content-body {
  padding: 24px;
}
</style>
