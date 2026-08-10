import { createRouter, createWebHistory } from 'vue-router'

// Import các component chính
import HomeView from '@/views/client/home/HomeView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import ForgotPasswordView from '@/views/auth/ForgotPasswordView.vue'
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
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/client/profile/ProfileView.vue'),
    },

    // ==================== AUTH ====================
    { path: '/login', name: 'login', component: LoginView },
    { path: '/quen-mat-khau', name: 'forgot-password', component: ForgotPasswordView },
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
        // Đã xóa đường dẫn 'categories' riêng biệt ở đây
        {
          path: 'brands',
          name: 'admin-brands',
          // Trỏ đến component gộp Thương hiệu & Danh mục vừa tạo
          component: () => import('@/views/admin/product/AdminBrandCategoryView.vue'),
        },
        {
          path: 'sizes-colors',
          name: 'admin-sizes-colors',
          component: () => import('@/views/admin/sizeColor/AdminColorSizeView.vue'),
        },
        // BỔ SUNG ROUTE QUẢN LÝ KHÁCH HÀNG
        {
          path: 'customers',
          name: 'admin-customers',
          component: () => import('@/views/admin/customer/AdminCustomerView.vue'),
        },
        {
          path: 'orders',
          name: 'admin-orders',
          // NOTE: cho phép cả EMPLOYEE vào duyệt đơn (khớp AuthInterceptor)
          component: () => import('@/views/admin/order/AdminOrderView.vue'),
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
    // Chưa đăng nhập -> bắt buộc đăng nhập trước
    if (!isLoggedIn) {
      next('/login')
      return
    }

    // Khách hàng (CUSTOMER) hoặc vai trò lạ -> không có cửa vào khu vực quản trị
    if (userRole !== 'ADMIN' && userRole !== 'EMPLOYEE') {
      alert('Bạn không có quyền truy cập khu vực quản trị!')
      next('/')
      return
    }

    // Nhân viên (EMPLOYEE) cố vào route chỉ dành cho ADMIN -> chặn lại
    if (to.meta.requiresAdmin && userRole !== 'ADMIN') {
      alert('Chức năng này chỉ dành cho Quản trị viên (ADMIN)!')
      next('/admin/dashboard')
      return
    }

    next()
  } else if (to.path === '/login' && isLoggedIn) {
    // Đã đăng nhập rồi mà cố vào lại trang login: đưa về đúng khu vực của họ
    if (userRole === 'ADMIN' || userRole === 'EMPLOYEE') {
      next('/admin/dashboard')
    } else {
      next('/')
    }
  } else {
    next()
  }
})

export default router
