import { createRouter, createWebHistory } from 'vue-router'

// Import các component chính
import HomeView from '@/views/client/home/HomeView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ==================== CLIENT ROUTES ====================
    { path: '/', name: 'home', component: HomeView },
    { path: '/ao', name: 'ao', component: () => import('@/views/client/ao/AoView.vue') },
    {
      path: '/product/:id',
      name: 'ProductDetail',
      component: () => import('@/views/client/product/ProductDetailView.vue'),
    },
    { path: '/cart', name: 'cart', component: () => import('@/views/client/cart/CartView.vue') },
    {
      path: '/checkout',
      name: 'checkout',
      component: () => import('@/views/client/checkout/CheckoutView.vue'),
    },
    {
      path: '/my-orders',
      name: 'my-orders',
      component: () => import('@/views/client/order/MyOrdersView.vue'),
    },
    {
      path: '/history-order',
      name: 'history-order',
      component: () => import('@/views/client/historyOrder/HistoryOrderView.vue'),
    },

    // ==================== AUTH ====================
    { path: '/login', name: 'login', component: LoginView },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/RegisterView.vue'),
    },

    // ==================== ADMIN ROUTES ====================
    {
      path: '/admin',
      component: AdminLayout,
      children: [
        { path: '', redirect: '/admin/dashboard' },

        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('@/views/admin/dashboard/AdminDashboardView.vue'),
        },
        {
          path: 'products',
          name: 'admin-products',
          component: () => import('@/views/admin/product/AdminProductView.vue'),
        },
        {
          path: 'categories',
          name: 'admin-categories',
          component: () => import('@/views/admin/product/AdminCategoryView.vue'),
        },
        {
          path: 'brands',
          name: 'admin-brands',
          component: () => import('@/views/admin/product/AdminBrandView.vue'),
        },
        {
          path: 'sizes-colors',
          name: 'admin-sizes-colors',
          component: () => import('@/views/admin/sizeColor/AdminColorSizeView.vue'),
        },
        {
          path: 'orders',
          name: 'admin-orders',
          component: () => import('@/views/admin/order/AdminOrderView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'staff',
          name: 'admin-staff',
          component: () => import('@/views/admin/staff/ManageStaffView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'vouchers',
          name: 'admin-vouchers',
          component: () => import('@/views/admin/marketing/AdminVoucherView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'ban-hang-tai-quay',
          name: 'admin-pos',
          component: () => import('@/views/admin/pos/AdminPosView.vue'),
        },
      ],
    },
  ],
})

// ==================== NAVIGATION GUARD ====================
router.beforeEach((to, from, next) => {
  const userRole = localStorage.getItem('user_role')
  const isLoggedIn = !!localStorage.getItem('user_id')

  if (to.path.startsWith('/admin')) {
    if (!isLoggedIn) {
      next('/login')
      return
    }

    // Chỉ ADMIN mới vào được các route nhạy cảm
    if (to.meta.requiresAdmin && userRole !== 'ADMIN') {
      alert('Bạn không có quyền truy cập khu vực này!')
      next('/admin/dashboard')
      return
    }

    next()
  } else if (to.path === '/login' && isLoggedIn) {
    next('/admin/dashboard')
  } else {
    next()
  }
})

export default router
