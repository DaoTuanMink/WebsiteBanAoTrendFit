import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// --- CHÈN 2 DÒNG NÀY ĐỂ KÍCH HOẠT BOOTSTRAP 5 ---
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
// -----------------------------------------------
import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080',
})

// Interceptor: Luôn đính kèm thông tin vào header
api.interceptors.request.use((config) => {
  const userId = localStorage.getItem('user_id')
  const userRole = localStorage.getItem('user_role')

  if (userId) {
    config.headers['NhanVien-ID'] = userId
    config.headers['User-Role'] = userRole
  }
  return config
})

export default api

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
