<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const username = ref('')
const password = ref('')
const errorMsg = ref('')
const loading = ref(false)

/**
 * Đăng nhập TrendFit
 * - Backend nhận { username, password } trong đó username = email (hoặc alias)
 * - Sau login lưu user_id, user_role, username vào localStorage
 */
const xuLyDangNhap = async () => {
  errorMsg.value = ''
  loading.value = true

  try {
    const response = await axios.post(
      'http://localhost:8080/api/auth/login',
      {
        username: username.value.trim(),
        password: password.value,
      },
      { timeout: 10000 },
    )

    const data = response.data

    if (!data || data.status !== 'success') {
      errorMsg.value = 'Đăng nhập thất bại. Vui lòng thử lại.'
      return
    }

    // Lưu phiên đăng nhập
    localStorage.setItem('user_id', String(data.id))
    localStorage.setItem('user_role', data.vaiTro)
    localStorage.setItem('username', data.username || username.value.trim())

    if (data.vaiTro === 'ADMIN') {
      alert(`Xin chào ${data.username}! Đang vào trang quản trị...`)
      router.push('/admin/dashboard')
    } else if (data.vaiTro === 'EMPLOYEE') {
      alert(`Xin chào ${data.username}! Đang vào giao diện bán hàng tại quầy...`)
      router.push('/admin/ban-hang-tai-quay')
    } else {
      alert(`Chào mừng quay lại, ${data.username}!`)
      router.push('/')
    }
  } catch (error) {
    console.error('Login error:', error)

    if (error.code === 'ECONNABORTED') {
      errorMsg.value = 'Hết thời gian chờ. Kiểm tra backend đang chạy chưa (port 8080).'
    } else if (error.response) {
      const d = error.response.data
      if (typeof d === 'string') {
        errorMsg.value = d
      } else if (d && typeof d === 'object') {
        errorMsg.value = d.message || d.error || JSON.stringify(d)
      } else {
        errorMsg.value = `Lỗi server (${error.response.status})`
      }
    } else if (error.request) {
      errorMsg.value =
        'Không kết nối được server. Hãy chạy backend Spring Boot (localhost:8080) rồi thử lại.'
    } else {
      errorMsg.value = error.message || 'Lỗi không xác định'
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <div class="login-logo">TF</div>
        <h1>TRENDFIT</h1>
        <p>Shop bán áo · Đăng nhập hệ thống</p>
      </div>

      <div v-if="errorMsg" class="login-error">{{ errorMsg }}</div>

      <form @submit.prevent="xuLyDangNhap" class="login-form">
        <label>Tài khoản / Email</label>
        <input
          v-model="username"
          type="text"
          placeholder="Nhập email hoặc tài khoản"
          autocomplete="username"
          :disabled="loading"
          required
        />

        <label>Mật khẩu</label>
        <input
          v-model="password"
          type="password"
          placeholder="Nhập mật khẩu"
          autocomplete="current-password"
          :disabled="loading"
          required
        />

        <div class="login-forgot">
          <router-link to="/quen-mat-khau">Quên mật khẩu?</router-link>
        </div>

        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? 'Đang xác thực...' : 'Đăng nhập' }}
        </button>
      </form>

      <p class="login-register">
        Chưa có tài khoản?
        <router-link to="/register">Đăng ký ngay</router-link>
      </p>
    </div>
  </div>
</template>

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
.login-forgot {
  text-align: right;
  margin: 4px 0 8px;
}
.login-forgot a {
  font-size: 12px;
  color: #64748b;
}
.login-btn {
  margin-top: 8px;
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
