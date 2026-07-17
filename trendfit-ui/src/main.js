import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'

import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import './assets/trendfit-light.css'

// Tạo cấu hình Axios dùng chung
const api = axios.create({
  baseURL: 'http://localhost:8080',
})

// Tự động thêm thông tin người dùng vào header
api.interceptors.request.use(
  (config) => {
    const userId = localStorage.getItem('user_id')
    const userRole = localStorage.getItem('user_role')

    if (userId) {
      config.headers['NhanVien-ID'] = userId
      config.headers['User-Role'] = userRole
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// Chỉ được tạo và mount ứng dụng đúng một lần
const app = createApp(App)

app.config.globalProperties.$api = api
app.provide('api', api)

app.use(router)
app.mount('#app')