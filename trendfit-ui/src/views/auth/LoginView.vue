<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const username = ref('')
const password = ref('')
const errorMsg = ref('')
const loading = ref(false)

const xuLyDangNhap = async () => {
  try {
    errorMsg.value = ''
    loading.value = true

    const response = await axios.post('http://localhost:8080/api/auth/login', {
      username: username.value,
      password: password.value,
    })

    const data = response.data
    if (data.status === 'success') {
      // 1. Lưu thông tin xác thực vào LocalStorage của trình duyệt
      localStorage.setItem('user_id', data.id) // <--- THÊM DÒNG NÀY
      localStorage.setItem('user_role', data.vaiTro)
      localStorage.setItem('user_token', data.token)
      localStorage.setItem('username', data.username)

      // 2. Thuật toán điều hướng Actor đi đúng vùng không gian làm việc
      if (data.vaiTro === 'ADMIN' || data.vaiTro === 'EMPLOYEE') {
        alert(`Xin chào ${data.username}! Hệ thống đang chuyển hướng vào Trang quản trị...`)
        router.push('/admin/products') // Chuyển hướng thẳng vào trang quản lý kho đồ
      } else {
        alert(`Chào mừng quay trở lại, ${data.username}!`)
        router.push('/') // Khách hàng ở lại trang mua sắm
      }
    }
  } catch (error) {
    if (error.response) {
      // Server trả về mã lỗi (401, 500...)
      errorMsg.value = error.response.data || 'Lỗi server'
      console.log('Lỗi từ server:', error.response.data)
    } else {
      errorMsg.value = 'Không thể kết nối tới server'
    }
  }
}
</script>

<template>
  <div
    class="login-container bg-light min-vh-100 d-flex align-items-center justify-content-center py-5"
  >
    <div
      class="card rounded-0 shadow-sm border-0 p-4 w-100 mx-3"
      style="max-width: 400px; text-align: left"
    >
      <div class="text-center mb-4">
        <h3 class="fw-black tracking-widest text-dark m-0">TRENDFIT</h3>
        <small class="text-muted text-uppercase tracking-wider font-size-11"
          >Xác thực quyền hạn người dùng</small
        >
      </div>

      <div v-if="errorMsg" class="alert alert-danger rounded-0 small py-2">{{ errorMsg }}</div>

      <form @submit.prevent="xuLyDangNhap" class="d-grid gap-3">
        <div>
          <label class="form-label small fw-bold text-muted text-uppercase tracking-wider"
            >Tài khoản truy cập</label
          >
          <input
            type="text"
            v-model="username"
            class="form-control rounded-0 border-dark"
            placeholder="admin / nhanvien / khachhang"
            :disabled="loading"
            required
          />
        </div>
        <div>
          <label class="form-label small fw-bold text-muted text-uppercase tracking-wider"
            >Mật khẩu bảo mật</label
          >
          <input
            type="password"
            v-model="password"
            class="form-control rounded-0 border-dark"
            placeholder="Nhập mật khẩu (123)"
            :disabled="loading"
            required
          />
        </div>
        <button
          type="submit"
          class="btn btn-dark rounded-0 py-2.5 text-uppercase fw-bold tracking-widest mt-2 small"
          :disabled="loading"
        >
          {{ loading ? 'Đang xác thực...' : 'Đăng nhập hệ thống' }}
        </button>
        <div class="text-center mt-3">
          <small class="text-muted">
            Chưa có tài khoản?
            <router-link to="/register" class="text-dark fw-bold text-decoration-underline">
              Đăng ký ngay
            </router-link>
          </small>
        </div>
      </form>

      <div class="mt-4 pt-3 border-top font-size-11 text-muted bg-white p-2 border">
        <p class="m-0 mb-1"><strong>Tài khoản thử nghiệm nhanh (Mật khẩu: 123):</strong></p>
        <ul class="ps-3 m-0">
          <li>Tài khoản Quản lý: <code class="text-danger fw-bold">admin</code></li>
          <li>Tài khoản Nhân viên: <code class="text-warning fw-bold">nhanvien</code></li>
          <li>Tài khoản Khách hàng: <code class="text-success fw-bold">khachhang</code></li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fw-black {
  font-weight: 900;
}
.font-size-11 {
  font-size: 11px;
}
.login-container {
  font-family: 'Segoe UI', sans-serif;
}
</style>
