<script setup>
/**
 * Trang Quên mật khẩu – luồng 2 bước (OTP 6 số qua email)
 * Khớp với backend: PasswordResetController
 *   POST /api/auth/forgot-password  { email }
 *   POST /api/auth/reset-password   { email, code, matKhauMoi }
 *
 * Giao diện đã đồng bộ với LoginView & RegisterView.
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

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
    const res = await axios.post(
      `${API}/forgot-password`,
      { email: email.value.trim() },
      { timeout: 15000 },
    )
    successMsg.value = typeof res.data === 'string' ? res.data : 'Đã gửi mã xác thực thành công!'
    buoc.value = 2
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
  if (matKhauMoi.value.length < 4) {
    errorMsg.value = 'Mật khẩu mới phải có ít nhất 4 ký tự!'
    return
  }

  loading.value = true
  try {
    const res = await axios.post(
      `${API}/reset-password`,
      {
        email: email.value.trim(),
        code: code.value.trim(),
        matKhauMoi: matKhauMoi.value,
      },
      { timeout: 10000 },
    )
    successMsg.value = typeof res.data === 'string' ? res.data : 'Đặt lại mật khẩu thành công!'
    setTimeout(() => router.push('/login'), 1800)
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
  if (err?.code === 'ECONNABORTED') {
    return 'Hết thời gian chờ. Kiểm tra backend đang chạy chưa (port 8080).'
  }
  const data = err?.response?.data
  if (typeof data === 'string' && data.trim()) return data
  if (data && typeof data === 'object') {
    return data.message || data.error || JSON.stringify(data)
  }
  if (err?.request) {
    return 'Không kết nối được server. Hãy chạy backend Spring Boot (localhost:8080).'
  }
  return macDinh + (err?.message ? `: ${err.message}` : '')
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <!-- Brand giống Login / Register -->
      <div class="login-brand">
        <div class="login-logo">TF</div>
        <h1>TRENDFIT</h1>
        <p>Shop bán áo · Quên mật khẩu</p>
      </div>

      <!-- Thông báo -->
      <div v-if="errorMsg" class="login-error">{{ errorMsg }}</div>
      <div v-if="successMsg" class="login-success">{{ successMsg }}</div>

      <!-- ============ BƯỚC 1: Nhập email ============ -->
      <form v-if="buoc === 1" @submit.prevent="guiMaXacThuc" class="login-form">
        <p class="form-desc">
          Nhập email đã đăng ký, hệ thống sẽ gửi mã xác thực gồm 6 chữ số về email đó.
        </p>

        <label>Email đã đăng ký</label>
        <input
          v-model.trim="email"
          type="email"
          placeholder="Nhập địa chỉ email"
          autocomplete="email"
          required
          :disabled="loading"
        />

        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? 'Đang gửi mã...' : 'Gửi mã xác thực' }}
        </button>
      </form>

      <!-- ============ BƯỚC 2: Nhập mã + mật khẩu mới ============ -->
      <form v-else @submit.prevent="datLaiMatKhau" class="login-form">
        <p class="form-desc">
          Mã xác thực đã được gửi tới <strong>{{ email }}</strong
          >. Kiểm tra hộp thư (kể cả Spam) và nhập mã trong vòng <strong>5 phút</strong>.
        </p>

        <label>Mã xác thực (6 số)</label>
        <input
          v-model.trim="code"
          type="text"
          maxlength="6"
          class="code-input"
          placeholder="000000"
          required
          :disabled="loading"
        />

        <label>Mật khẩu mới</label>
        <input
          v-model="matKhauMoi"
          type="password"
          placeholder="Nhập mật khẩu mới"
          autocomplete="new-password"
          required
          :disabled="loading"
        />

        <label>Xác nhận mật khẩu mới</label>
        <input
          v-model="xacNhanMatKhau"
          type="password"
          placeholder="Nhập lại mật khẩu mới"
          autocomplete="new-password"
          required
          :disabled="loading"
        />

        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? 'Đang xử lý...' : 'Đặt lại mật khẩu' }}
        </button>

        <button type="button" class="btn-link" @click="quayLaiBuoc1" :disabled="loading">
          ← Nhập lại email / Gửi lại mã
        </button>
      </form>

      <p class="login-register">
        <router-link to="/login">← Quay lại đăng nhập</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
/* ===== Đồng bộ 100% với LoginView & RegisterView ===== */
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(circle at 20% 20%, rgba(99, 102, 241, 0.15), transparent 40%),
    radial-gradient(circle at 80% 80%, rgba(139, 92, 246, 0.12), transparent 40%), #0f172a;
  font-family: 'Inter', system-ui, sans-serif;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 32px 28px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.35);
}

.login-brand {
  text-align: center;
  margin-bottom: 24px;
}

.login-logo {
  width: 48px;
  height: 48px;
  margin: 0 auto 12px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6 50%, #06b6d4);
  color: #fff;
  font-weight: 800;
  font-size: 16px;
  font-family: 'Space Grotesk', sans-serif;
}

.login-brand h1 {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 1.4rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  margin: 0 0 4px;
  color: #0f172a;
}

.login-brand p {
  margin: 0;
  font-size: 13px;
  color: #64748b;
}

.login-error {
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 13px;
  margin-bottom: 16px;
  line-height: 1.45;
}

.login-success {
  background: #f0fdf4;
  color: #15803d;
  border: 1px solid #bbf7d0;
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 13px;
  margin-bottom: 16px;
  line-height: 1.45;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
  margin: 0 0 8px;
}

.login-form label {
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #64748b;
  margin-top: 8px;
}

.login-form input {
  padding: 12px 14px;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.login-form input:focus {
  border-color: #6366f1;
}

.code-input {
  text-align: center;
  font-weight: 700;
  font-size: 1.25rem !important;
  letter-spacing: 6px;
}

.login-btn {
  margin-top: 16px;
  padding: 13px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-weight: 700;
  font-size: 14px;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  cursor: pointer;
  transition:
    transform 0.2s,
    box-shadow 0.2s;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.35);
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-link {
  margin-top: 8px;
  background: none;
  border: none;
  color: #64748b;
  font-size: 13px;
  cursor: pointer;
  text-align: center;
  padding: 6px;
}

.btn-link:hover:not(:disabled) {
  color: #4f46e5;
}

.login-register {
  text-align: center;
  margin: 18px 0 0;
  font-size: 13px;
  color: #64748b;
}

.login-register a {
  color: #4f46e5;
  font-weight: 700;
  text-decoration: none;
}

.login-register a:hover {
  text-decoration: underline;
}
</style>
