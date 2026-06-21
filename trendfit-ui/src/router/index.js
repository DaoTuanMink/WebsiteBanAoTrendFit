import { createRouter, createWebHistory } from 'vue-router'

// 1. Import các phân khu dành cho KHÁCH HÀNG (CLIENT)
import HomeView from '../views/client/home/HomeView.vue'
import ProductDetailView from '../views/client/product/ProductDetailView.vue'
import CartView from '../views/client/cart/CartView.vue' // Thêm mới trang Giỏ hàng
import CheckoutView from '../views/client/checkout/CheckoutView.vue' // Thêm mới trang Thanh toán

// 2. Import phân khu XÁC THỰC (AUTH)
import LoginView from '../views/auth/LoginView.vue'

// 3. Import các phân khu dành cho QUẢN TRỊ (ADMIN)
import AdminProductView from '../views/admin/product/AdminProductView.vue'
import AdminOrderView from '../views/admin/order/AdminOrderView.vue'

import AoView from '../views/client/ao/AoView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // --- TUYẾN ĐƯỜNG CHO KHÁCH HÀNG ---
    { path: '/', name: 'home', component: HomeView },
    { path: '/product/:id', name: 'product-detail', component: ProductDetailView },
    { path: '/cart', name: 'cart', component: CartView }, // Route giỏ hàng mới
    { path: '/checkout', name: 'checkout', component: CheckoutView }, // Route đặt hàng mới

    // --- TUYẾN ĐƯỜNG ĐĂNG NHẬP ---
    { path: '/login', name: 'login', component: LoginView },

    // --- TUYẾN ĐƯỜNG QUẢN TRỊ (ADMIN) ---
    { path: '/admin/products', name: 'admin-product', component: AdminProductView },
    { path: '/admin/orders', name: 'admin-order', component: AdminOrderView },
    { path: '/ao', name: 'ao', component: AoView },
  ],
})

// 4. HỆ THỐNG NAVIGATION GUARD (BẢO VỆ CHẶN CỬA PHÂN QUYỀN)
router.beforeEach((to, from, next) => {
  const userRole = localStorage.getItem('user_role') // Bốc quyền từ trình duyệt ra đối chiếu

  // Tình huống 1: Người dùng cố ý gõ link vào thẳng vùng quản trị /admin/...
  if (to.path.startsWith('/admin')) {
    if (userRole === 'ADMIN' || userRole === 'EMPLOYEE') {
      next() // Đúng vai trò Nhân viên/Quản lý -> Mở cửa cho qua
    } else {
      alert('Cảnh báo an ninh: Khu vực này chỉ dành riêng cho Ban quản trị!')
      next('/login') // Đá bay người lạ hoặc khách hàng thường về màn hình đăng nhập
    }
  }
  // Tình huống 2: Đã đăng nhập rồi nhưng vẫn bấm cố vào lại trang /login
  else if (to.path === '/login' && userRole) {
    if (userRole === 'ADMIN' || userRole === 'EMPLOYEE') {
      next('/admin/products') // Đá thẳng vào trang quản lý sản phẩm
    } else {
      next('/') // Đá khách hàng về trang chủ mua sắm đồ TrendFit
    }
  }
  // Các trang công cộng còn lại (bao gồm /cart và /checkout) cho phép ra vào tự do
  else {
    next()
  }
})

export default router
