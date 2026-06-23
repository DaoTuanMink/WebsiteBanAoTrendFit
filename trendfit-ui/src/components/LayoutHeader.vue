<template>
  <nav class="trendfit-nav bg-black text-white sticky-top">
    <div
      class="container-fluid px-4 px-md-5 py-3 d-flex justify-content-between align-items-center"
    >
      <router-link to="/" class="text-white text-decoration-none fw-black fs-4 tracking-widest m-0">
        TRENDFIT
      </router-link>

      <div class="d-none d-xl-flex gap-4 small fw-bold text-uppercase align-items-center">
        <router-link to="/" class="nav-link text-white-50 hover-white">Trang chủ</router-link>
        <router-link to="/ao" class="nav-link text-white fw-bold border-bottom border-danger pb-1"
          >Áo</router-link
        >
        <router-link to="#" class="nav-link text-white-50 hover-white"
          >Online Exclusive</router-link
        >

        <span
          v-if="userRole === 'ADMIN' || userRole === 'EMPLOYEE'"
          class="text-warning cursor-pointer"
          @click="router.push('/admin/products')"
        >
          <i class="bi bi-speedometer2 me-1"></i> Quản trị
        </span>
      </div>

      <div class="d-flex gap-3 align-items-center">
        <div class="icon-box"><i class="bi bi-search"></i></div>

        <router-link v-if="!username" to="/login" class="text-white icon-box" title="Đăng nhập">
          <i class="bi bi-person"></i>
        </router-link>

        <div v-else class="d-flex align-items-center gap-2">
          <span class="small border border-secondary px-2 py-1 bg-dark text-white text-uppercase">{{
            username
          }}</span>
          <div class="icon-box text-danger border-danger" @click="dangXuat" title="Đăng xuất">
            <i class="bi bi-box-arrow-right"></i>
          </div>
        </div>

        <router-link to="/cart" class="text-white position-relative icon-box">
          <i class="bi bi-cart3"></i>
          <span v-if="cartCount > 0" class="badge-count">{{ cartCount }}</span>
        </router-link>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref('')
const userRole = ref('')
const cartCount = ref(0) // Biến lưu số lượng sản phẩm

const kiemTraTrangThai = () => {
  username.value = localStorage.getItem('username') || ''
  userRole.value = localStorage.getItem('user_role') || ''

  // Lấy giỏ hàng từ localStorage (giả sử bạn lưu key là 'cart')
  const cart = JSON.parse(localStorage.getItem('cart') || '[]')
  // Tính tổng số lượng (mỗi item có thuộc tính quantity)
  cartCount.value = cart.reduce((total, item) => total + (item.quantity || 1), 0)
}

const dangXuat = () => {
  if (confirm('Bạn muốn đăng xuất?')) {
    localStorage.clear()
    window.location.reload()
  }
}

// Cập nhật giỏ hàng khi trang được tải
onMounted(() => {
  kiemTraTrangThai()

  // Lắng nghe sự kiện nếu bạn muốn cập nhật số lượng ngay khi người dùng thêm hàng vào giỏ
  window.addEventListener('storage', kiemTraTrangThai)
})

onMounted(kiemTraTrangThai)
</script>

<style scoped>
.trendfit-nav {
  z-index: 1050;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}
.nav-link {
  text-decoration: none;
  transition: 0.3s;
}
.hover-white:hover {
  color: #fff !important;
}
.icon-box {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: 0.2s;
}
.icon-box:hover {
  background: #fff;
  color: #000;
}
.badge-count {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #dc3545;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 50%;
}
</style>
