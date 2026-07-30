<template>
  <div class="d-flex align-items-center justify-content-center min-vh-100 bg-light">
    <div class="card p-4 shadow-sm" style="max-width: 420px; width: 100%">
      <h4 class="fw-bold text-center mb-3">Quên mật khẩu</h4>

      <!-- Thông báo lỗi / thành công -->
      <div v-if="errorMsg" class="alert alert-danger py-2">{{ errorMsg }}</div>
      <div v-if="successMsg" class="alert alert-success py-2">{{ successMsg }}</div>

      <!-- ============ BƯỚC 1: nhập email để nhận mã xác thực ============ -->
      <form v-if="buoc === 1" @submit.prevent="guiMaXacThuc" class="d-grid gap-3">
        <p class="text-muted small mb-0">
          Nhập email đã đăng ký, hệ thống sẽ gửi mã xác thực gồm 6 chữ số về email đó.
        </p>
        <input
          v-model.trim="email"
          type="email"
          class="form-control"
          placeholder="Email đã đăng ký"
          required
          :disabled="loading"
        />
        <button type="submit" class="btn btn-dark" :disabled="loading">
          {{ loading ? 'Đang gửi...' : 'Gửi mã xác thực' }}
        </button>
      </form>

      <!-- ============ BƯỚC 2: nhập mã + mật khẩu mới ============ -->
      <form v-else @submit.prevent="datLaiMatKhau" class="d-grid gap-3">
        <p class="text-muted small mb-0">
          Mã xác thực đã được gửi tới <strong>{{ email }}</strong
          >. Vui lòng kiểm tra hộp thư (kể cả mục Spam) và nhập mã bên dưới trong vòng 5 phút.
        </p>
        <input
          v-model.trim="code"
          maxlength="6"
          class="form-control text-center fw-bold"
          style="letter-spacing: 4px; font-size: 1.2rem"
          placeholder="000000"
          required
          :disabled="loading"
        />
        <input
          v-model="matKhauMoi"
          type="password"
          class="form-control"
          placeholder="Mật khẩu mới"
          required
          :disabled="loading"
        />
        <input
          v-model="xacNhanMatKhau"
          type="password"
          class="form-control"
          placeholder="Xác nhận mật khẩu mới"
          required
          :disabled="loading"
        />
        <button type="submit" class="btn btn-dark" :disabled="loading">
          {{ loading ? 'Đang xử lý...' : 'Đặt lại mật khẩu' }}
        </button>
        <button type="button" class="btn btn-link btn-sm" @click="quayLaiBuoc1">
          ← Nhập lại email / gửi lại mã
        </button>
      </form>

      <div class="text-center mt-3">
        <router-link to="/login" class="small text-muted text-decoration-underline">
          ← Quay lại đăng nhập
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
// Trang "Quên mật khẩu" - luồng 2 bước bằng mã OTP gửi qua email, khớp với
// PasswordResetController ở backend (POST /api/auth/forgot-password và
// POST /api/auth/reset-password).
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const API = 'http://localhost:8080/api/auth'

const buoc = ref(1) // 1 = nhập email, 2 = nhập mã + mật khẩu mới
const email = ref('')
const code = ref('')
const matKhauMoi = ref('')
const xacNhanMatKhau = ref('')

const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

const guiMaXacThuc = async () => {
  errorMsg.value = ''
  successMsg.value = ''
  loading.value = true
  try {
    const res = await axios.post(`${API}/forgot-password`, { email: email.value })
    successMsg.value = res.data
    buoc.value = 2 // Chuyển sang bước nhập mã
  } catch (err) {
    errorMsg.value = layThongBaoLoi(err, 'Gửi mã xác thực thất bại')
  } finally {
    loading.value = false
  }
}

const datLaiMatKhau = async () => {
  errorMsg.value = ''
  successMsg.value = ''

  if (matKhauMoi.value !== xacNhanMatKhau.value) {
    errorMsg.value = 'Mật khẩu xác nhận không khớp!'
    return
  }

  loading.value = true
  try {
    const res = await axios.post(`${API}/reset-password`, {
      email: email.value,
      code: code.value,
      matKhauMoi: matKhauMoi.value,
    })
    successMsg.value = res.data
    // Đặt lại thành công -> tự động đưa về trang đăng nhập sau 1.5s
    setTimeout(() => router.push('/login'), 1500)
  } catch (err) {
    errorMsg.value = layThongBaoLoi(err, 'Đặt lại mật khẩu thất bại')
  } finally {
    loading.value = false
  }
}

const quayLaiBuoc1 = () => {
  buoc.value = 1
  code.value = ''
  matKhauMoi.value = ''
  xacNhanMatKhau.value = ''
  errorMsg.value = ''
  successMsg.value = ''
}

const layThongBaoLoi = (err, macDinh) => {
  const data = err?.response?.data
  if (typeof data === 'string' && data.trim()) return data
  return macDinh + (err?.message ? `: ${err.message}` : '')
}
</script>
