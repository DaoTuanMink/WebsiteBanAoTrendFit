<template>
  <div class="admin-shell">
    <button
      v-if="sidebarOpen"
      type="button"
      class="admin-overlay d-lg-none"
      aria-label="Đóng menu"
      @click="sidebarOpen = false"
    ></button>

    <aside class="admin-sidebar" :class="{ 'is-open': sidebarOpen }">
      <div class="admin-brand">
        <router-link to="/admin/dashboard" class="admin-brand-link" @click="sidebarOpen = false">
          <span class="admin-brand-mark">TF</span>
          <span>
            <strong>TrendFit</strong>
            <small>ADMIN CENTER</small>
          </span>
        </router-link>

        <button
          type="button"
          class="admin-sidebar-close d-lg-none"
          aria-label="Đóng menu"
          @click="sidebarOpen = false"
        >
          ×
        </button>
      </div>

      <div class="admin-user-card">
        <div class="admin-avatar">{{ userInitial }}</div>
        <div class="min-w-0">
          <strong class="admin-user-name">{{ username }}</strong>
          <span class="admin-role-badge">{{ roleLabel }}</span>
        </div>
      </div>

      <nav class="admin-nav" aria-label="Điều hướng quản trị">
        <p class="admin-nav-label">TỔNG QUAN</p>

        <router-link
          v-for="item in overviewItems"
          :key="item.to"
          :to="item.to"
          class="admin-nav-link"
          @click="sidebarOpen = false"
        >
          <span class="admin-nav-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>

        <p class="admin-nav-label mt-3">QUẢN LÝ CỬA HÀNG</p>

        <router-link
          v-for="item in visibleManagementItems"
          :key="item.to"
          :to="item.to"
          class="admin-nav-link"
          @click="sidebarOpen = false"
        >
          <span class="admin-nav-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="admin-sidebar-footer">
        <router-link to="/" class="admin-secondary-action" @click="sidebarOpen = false">
          <span>⌂</span>
          <span>Về trang chủ</span>
        </router-link>

        <button type="button" class="admin-logout" @click="dangXuat">
          <span>↪</span>
          <span>Đăng xuất</span>
        </button>
      </div>
    </aside>

    <section class="admin-workspace">
      <header class="admin-topbar">
        <div class="d-flex align-items-center gap-3 min-w-0">
          <button
            type="button"
            class="admin-menu-button d-lg-none"
            aria-label="Mở menu"
            @click="sidebarOpen = true"
          >
            ☰
          </button>

          <div class="min-w-0">
            <p class="admin-breadcrumb mb-1">TREND FIT / QUẢN TRỊ</p>
            <h1 class="admin-page-title mb-0">{{ currentPageTitle }}</h1>
          </div>
        </div>

        <div class="admin-topbar-user d-none d-sm-flex">
          <span class="admin-status-dot"></span>
          <div>
            <strong>{{ username }}</strong>
            <small>{{ roleLabel }}</small>
          </div>
        </div>
      </header>

      <main class="admin-content">
        <router-view />
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const sidebarOpen = ref(false)

const username = computed(() => localStorage.getItem('username') || 'Người dùng TrendFit')
const userRole = computed(() => localStorage.getItem('user_role') || 'EMPLOYEE')
const roleLabel = computed(() => (userRole.value === 'ADMIN' ? 'Quản trị viên' : 'Nhân viên'))
const userInitial = computed(() => username.value.trim().charAt(0).toUpperCase() || 'T')

const overviewItems = [
  { to: '/admin/dashboard', label: 'Thống kê doanh số', icon: '▦' },
  { to: '/admin/ban-hang-tai-quay', label: 'Bán hàng tại quầy', icon: '◈' },
]

const managementItems = [
  { to: '/admin/products', label: 'Sản phẩm', icon: '◆' },
  { to: '/admin/categories', label: 'Danh mục', icon: '▤' },
  { to: '/admin/brands', label: 'Thương hiệu', icon: '◉' },
  { to: '/admin/sizes-colors', label: 'Kích cỡ & màu sắc', icon: '◐' },
  { to: '/admin/orders', label: 'Đơn hàng', icon: '▧', roles: ['ADMIN'] },
  { to: '/admin/vouchers', label: 'Phiếu giảm giá', icon: '◇', roles: ['ADMIN'] },
  { to: '/admin/staff', label: 'Nhân viên', icon: '♙', roles: ['ADMIN'] },
]

const visibleManagementItems = computed(() =>
  managementItems.filter((item) => !item.roles || item.roles.includes(userRole.value)),
)

const allItems = computed(() => [...overviewItems, ...visibleManagementItems.value])

const currentPageTitle = computed(() => {
  const currentItem = allItems.value.find((item) => route.path.startsWith(item.to))
  return currentItem?.label || 'Trang quản trị'
})

watch(
  () => route.fullPath,
  () => {
    sidebarOpen.value = false
  },
)

function dangXuat() {
  if (!window.confirm('Bạn muốn đăng xuất khỏi hệ thống?')) return

  localStorage.clear()
  router.replace('/')
}
</script>
