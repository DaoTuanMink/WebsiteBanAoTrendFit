<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <div class="login-logo">TF</div>
        <h1>TRENDFIT</h1>
        <p>Shop bán áo · Tạo tài khoản mới</p>
      </div>

      <div v-if="errorMsg" class="login-error">{{ errorMsg }}</div>

      <form @submit.prevent="xuLyDangKy" class="login-form">
        <label>Họ và tên</label>
        <input
          v-model="form.hoTen"
          type="text"
          placeholder="Nhập họ và tên của bạn"
          :disabled="loading"
          required
        />

        <label>Email</label>
        <input
          v-model="form.email"
          type="email"
          placeholder="Nhập địa chỉ email"
          :disabled="loading"
          required
        />

        <label>Mật khẩu</label>
        <input
          v-model="form.matKhau"
          type="password"
          placeholder="Nhập mật khẩu"
          :disabled="loading"
          required
        />

        <label>Xác nhận mật khẩu</label>
        <input
          v-model="confirmPassword"
          type="password"
          placeholder="Nhập lại mật khẩu"
          :disabled="loading"
          required
        />

        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? 'Đang xử lý...' : 'Đăng ký' }}
        </button>
      </form>

      <p class="login-register">
        Đã có tài khoản?
        <router-link to="/login">Đăng nhập ngay</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const form = ref({ hoTen: '', email: '', matKhau: '' })
const confirmPassword = ref('')
const errorMsg = ref('')
const loading = ref(false)

const xuLyDangKy = async () => {
  errorMsg.value = ''

  // Kiểm tra xác nhận mật khẩu có khớp nhau không
  if (form.value.matKhau !== confirmPassword.value) {
    errorMsg.value = 'Mật khẩu xác nhận không khớp. Vui lòng kiểm tra lại.'
    return
  }

  loading.value = true

  try {
    await axios.post('http://localhost:8080/api/auth/register', form.value, { timeout: 10000 })
    alert('Đăng ký thành công! Vui lòng đăng nhập.')
    router.push('/login')
  } catch (err) {
    console.error('Register error:', err)
    if (err.code === 'ECONNABORTED') {
      errorMsg.value = 'Hết thời gian chờ. Kiểm tra backend đang chạy chưa (port 8080).'
    } else if (err.response) {
      const d = err.response.data
      if (typeof d === 'string') {
        errorMsg.value = d
      } else if (d && typeof d === 'object') {
        errorMsg.value = d.message || d.error || JSON.stringify(d)
      } else {
        errorMsg.value = `Lỗi server (${err.response.status})`
      }
    } else if (err.request) {
      errorMsg.value = 'Không kết nối được server. Hãy chạy backend Spring Boot (localhost:8080).'
    } else {
      errorMsg.value = err.message || 'Lỗi không xác định'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
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
.login-form {
  display: flex;
  flex-direction: column;
  gap: 6px;
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
.login-register {
  text-align: center;
  margin: 18px 0 0;
  font-size: 13px;
  color: #64748b;
}
.login-register a {
  color: #4f46e5;
  font-weight: 700;
}
</style>
