<template>
  <div class="container py-5 d-flex justify-content-center">
    <div class="card p-4 shadow-sm" style="width: 400px">
      <h3 class="text-center mb-4">ĐĂNG KÝ TÀI KHOẢN</h3>
      <form @submit.prevent="xuLyDangKy">
        <input v-model="form.hoTen" class="form-control mb-3" placeholder="Họ và tên" required />
        <input
          v-model="form.email"
          class="form-control mb-3"
          placeholder="Email"
          type="email"
          required
        />
        <input
          v-model="form.matKhau"
          class="form-control mb-3"
          placeholder="Mật khẩu"
          type="password"
          required
        />
        <button type="submit" class="btn btn-dark w-100">ĐĂNG KÝ</button>
      </form>
      <div class="mt-3 text-center">
        <small>Đã có tài khoản? <router-link to="/login">Đăng nhập</router-link></small>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const form = ref({ hoTen: '', email: '', matKhau: '' })

const xuLyDangKy = async () => {
  try {
    await axios.post('http://localhost:8080/api/auth/register', form.value)
    alert('Đăng ký thành công! Vui lòng đăng nhập.')
    router.push('/login')
  } catch (err) {
    alert('Đăng ký thất bại: ' + (err.response?.data || 'Vui lòng kiểm tra lại'))
  }
}
</script>
