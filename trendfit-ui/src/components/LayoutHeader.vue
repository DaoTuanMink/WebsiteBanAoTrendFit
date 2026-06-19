<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref('')
const userRole = ref('')

// Kiểm tra trạng thái đăng nhập khi header được mount lên màn hình
const kiemTraTrangThai = () => {
  username.value = localStorage.getItem('username') || ''
  userRole.value = localStorage.getItem('user_role') || ''
}

// Hàm xử lý đăng xuất nhanh cho Admin / Nhân viên / Khách hàng
const dangXuat = () => {
  if (confirm('Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?')) {
    localStorage.clear() // Xóa sạch token và role trong bộ nhớ
    username.value = ''
    userRole.value = ''
    alert('Đã đăng xuất hệ thống thành công!')
    router.push('/login') // Đá về trang đăng nhập
  }
}

onMounted(() => {
  kiemTraTrangThai()
})
</script>

<template>
  <div class="owen-fixed-black-nav bg-black text-white sticky-top">
    <div
      class="container-fluid px-4 px-md-5 py-3 d-flex justify-content-between align-items-center"
    >
      <router-link to="/" class="text-white text-decoration-none fw-black fs-4 tracking-widest m-0"
        >OWEN</router-link
      >

      <div
        class="d-none d-xl-flex gap-4 small fw-semibold text-uppercase text-white-50 align-items-center"
      >
        <span class="text-white"
          >Bộ sưu tập <i class="bi bi-chevron-down ms-1 font-size-10"></i
        ></span>
        <span class="text-white">Hàng mới về</span>
        <span>Áo <i class="bi bi-chevron-down ms-1 font-size-10"></i></span>
        <span>Phụ kiện <i class="bi bi-chevron-down ms-1 font-size-10"></i></span>
        <span>Chỉ bán Online</span>
        <span>Ưu đãi <i class="bi bi-chevron-down ms-1 font-size-10"></i></span>
        <span
          v-if="userRole === 'ADMIN' || userRole === 'EMPLOYEE'"
          class="text-warning cursor-pointer"
          @click="router.push('/admin/products')"
        >
          <i class="bi bi-speedometer2 me-1"></i> Vào trang Quản trị
        </span>
        <span v-else>Hệ thống cửa hàng</span>
      </div>

      <div class="d-flex gap-2 align-items-center">
        <div class="icon-square-box"><i class="bi bi-search"></i></div>

        <div class="d-flex align-items-center gap-2">
          <router-link
            v-if="!username"
            to="/login"
            class="text-white text-decoration-none"
            title="Đăng nhập"
          >
            <div class="icon-square-box"><i class="bi bi-person"></i></div>
          </router-link>

          <div v-else class="d-flex align-items-center gap-2">
            <span
              class="small font-size-11 text-white-50 border border-secondary px-2 py-1 text-uppercase bg-dark"
            >
              {{ username }} ({{ userRole }})
            </span>
            <div
              class="icon-square-box text-danger border-danger-subtle"
              @click="dangXuat"
              title="Đăng xuất hệ thống"
            >
              <i class="bi bi-box-arrow-right"></i>
            </div>
          </div>
        </div>

        <router-link to="/cart" class="text-white text-decoration-none">
          <div class="icon-square-box position-relative">
            <i class="bi bi-cart3"></i>
            <span class="badge-count-orange">0</span>
          </div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fw-black {
  font-weight: 900;
}
.font-size-10 {
  font-size: 10px;
}
.font-size-11 {
  font-size: 11px;
}
.cursor-pointer {
  cursor: pointer;
}

.owen-fixed-black-nav {
  position: sticky;
  top: 0;
  z-index: 1050;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.icon-square-box {
  width: 36px;
  height: 36px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  color: white;
}
.icon-square-box:hover {
  background-color: #fff;
  color: #000;
  border-color: #fff;
}
.badge-count-orange {
  position: absolute;
  top: -4px;
  right: -6px;
  background-color: #f97316;
  color: white;
  font-size: 8px;
  font-weight: bold;
  padding: 1px 4px;
  border-radius: 4px;
}
</style>
