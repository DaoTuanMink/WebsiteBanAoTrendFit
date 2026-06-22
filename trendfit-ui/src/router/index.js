import { createRouter, createWebHistory } from 'vue-router'

// Import các component chính
import HomeView from '@/views/client/home/HomeView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // --- TUYẾN ĐƯỜNG CHO KHÁCH HÀNG ---
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/ao',
      name: 'ao',
      component: () => import('@/views/client/ao/AoView.vue'),
    },

    // --- TUYẾN ĐƯỜNG ĐĂNG NHẬP ---
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },

    // --- TUYẾN ĐƯỜNG QUẢN TRỊ (Nested Routes) ---
    {
      path: '/admin',
      component: AdminLayout,
      children: [
        {
          path: 'products',
          component: () => import('@/views/admin/product/AdminProductView.vue'),
        },
        // ĐÃ DI CHUYỂN VÀO ĐÂY ĐỂ GIỮ NGUYÊN MENU SIDEBAR
        {
          path: 'categories',
          component: () => import('@/views/admin/product/AdminCategoryView.vue'),
        },
        {
          path: 'orders',
          component: () => import('@/views/admin/order/AdminOrderView.vue'),
        },
        {
          path: 'brands',
          component: () => import('@/views/admin/product/AdminBrandView.vue'),
        },
      ],
    },
  ],
})

// HỆ THỐNG NAVIGATION GUARD
router.beforeEach((to, from, next) => {
  const userRole = localStorage.getItem('user_role')

  if (to.path.startsWith('/admin')) {
    if (userRole === 'ADMIN' || userRole === 'EMPLOYEE') {
      next()
    } else {
      alert('Cảnh báo an ninh: Khu vực này chỉ dành riêng cho Ban quản trị!')
      next('/login')
    }
  } else if (to.path === '/login' && userRole) {
    if (userRole === 'ADMIN' || userRole === 'EMPLOYEE') {
      next('/admin/products')
    } else {
      next('/')
    }
  } else {
    next()
  }
})

export default router
