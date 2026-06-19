import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// --- CHÈN 2 DÒNG NÀY ĐỂ KÍCH HOẠT BOOTSTRAP 5 ---
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
// -----------------------------------------------

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
