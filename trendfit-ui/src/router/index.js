import { createRouter, createWebHistory } from 'vue-router'

// Import các component chính
import HomeView from '@/views/client/home/HomeView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/my-orders',
      name: 'my-orders',
      component: () => import('@/views/client/order/MyOrdersView.vue'),
    },
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

    {
      path: '/cart',
      name: 'cart',
      component: () => import('@/views/client/cart/CartView.vue'),
    },
    {
      path: '/checkout',
      name: 'checkout',
      component: () => import('@/views/client/checkout/CheckoutView.vue'),
    },

    // --- TUYẾN ĐƯỜNG ĐĂNG NHẬP ---
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/RegisterView.vue'),
    },
    {
      path: '/product/:id',
      name: 'ProductDetail',
      component: () => import('@/views/client/product/ProductDetailView.vue'),
    },
    {
      path: '/history-order',
      name: 'history-order',
      component: () => import('@/views/client/historyOrder/HistoryOrderView.vue'),
    },

    // --- TUYẾN ĐƯỜNG QUẢN TRỊ (Nested Routes) ---
    {
      path: '/admin',
      component: AdminLayout,
      children: [
        {
          path: 'staff', // Route con của /admin
          component: () => import('@/views/admin/staff/ManageStaffView.vue'),
        },
        //
        {
          path: '',
          redirect: '/admin/dashboard',
          // Khi người dùng truy cập /admin, hệ thống tự chuyển về dashboard.
          // Tránh lỗi màn hình trắng do /admin không có component con mặc định.
        },
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('@/views/admin/dashboard/AdminDashboardView.vue'),

          // Route quản lý sản phẩm dành cho ADMIN và EMPLOYEE.
          // Không comment route này vì menu admin và redirect sau đăng nhập có thể trỏ tới /admin/products.
          meta: {
            roles: ['ADMIN', 'EMPLOYEE'],
          },
        },
        {
          path: 'products',
          name: 'admin-products',
          component: () => import('@/views/admin/product/AdminProductView.vue'),

          // quản lí sản phẩm , giá bán và tồn kho
          meta: {
            roles: ['ADMIN', 'EMPLOYEE'],
          },
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
        {
          path: 'vouchers',
          component: () => import('@/views/admin/marketing/AdminVoucherView.vue'),
        },

        {
          path: 'ban-hang-tai-quay',
          name: 'admin-pos',
          component: () => import('@/views/admin/pos/AdminPosView.vue'),
          meta: {
            roles: ['ADMIN', 'EMPLOYEE'],
          },
        },
      ],
    },
  ],
})

// HỆ THỐNG NAVIGATION GUARD
// ... (các import giữ nguyên)

router.beforeEach((to, from, next) => {
  const userRole = localStorage.getItem('user_role') // 'ADMIN' hoặc 'EMPLOYEE'
  const isLoggedIn = !!localStorage.getItem('user_id')

  // 1. Nếu vào khu vực Admin
  if (to.path.startsWith('/admin')) {
    if (!isLoggedIn) {
      next('/login') // Chưa đăng nhập thì đẩy về login
    }
    // Kiểm tra quyền riêng cho các route nhạy cảm
    else if (
      (to.path === '/admin/orders' ||
        to.path === '/admin/vouchers' ||
        to.path === '/admin/staff') &&
      userRole !== 'ADMIN'
    ) {
      alert('Bạn không có quyền truy cập khu vực này!')
      next('/admin/dashboard') // Đẩy về trang chủ admin nếu là nhân viên cố vào
    } else {
      next() // Cho phép vào (các route còn lại như products, pos, dashboard thì cả 2 đều vào được)
    }
  }
  // 2. Nếu đã đăng nhập mà vào login thì đẩy về dashboard admin
  else if (to.path === '/login' && isLoggedIn) {
    next('/admin/dashboard')
  } else {
    next()
  }
})

export default router
