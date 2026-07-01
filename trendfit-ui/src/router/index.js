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

    {
      path: '/cart',
      name: 'cart',
      component: () => import('@/views/client/cart/CartView.vue'),
    },
    {
      path: '/checkout',
      name: 'checkout',
      component: () => import('@/views/client/cart/CheckoutView.vue'),
    },

    // --- TUYẾN ĐƯỜNG ĐĂNG NHẬP ---
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/product/:id',
      name: 'ProductDetail',
      component: () => import('@/views/client/product/ProductDetailView.vue'),
    },

    // --- TUYẾN ĐƯỜNG QUẢN TRỊ (Nested Routes) ---
    {
      path: '/admin',
      component: AdminLayout,
      children: [
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
  const userRole = localStorage.getItem('user_role')
  const isAdminOrEmployee = userRole === 'ADMIN' || userRole === 'EMPLOYEE'

  // 1. Kiểm tra nếu đang truy cập vào khu vực admin
  if (to.path.startsWith('/admin')) {
    if (isAdminOrEmployee) {
      next() // Cho phép vào
    } else {
      // Nếu không có quyền, đẩy về login
      alert('Cảnh báo an ninh: Khu vực này chỉ dành riêng cho Ban quản trị!')
      next('/login')
    }
  }
  // 2. Kiểm tra nếu đang ở trang login mà đã đăng nhập rồi
  // else if (to.path === '/login' && isAdminOrEmployee) {
  //   next('/admin/products')
  // }
  else if (to.path === '/login' && isAdminOrEmployee) {
  next('/admin/dashboard')
  

}
  // 3. Các trường hợp còn lại (bao gồm trang chủ '/'): Cho phép truy cập tự do
  else {
    next()
  }
})

export default router
