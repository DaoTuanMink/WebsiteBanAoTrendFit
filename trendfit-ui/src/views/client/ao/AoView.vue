<template>
  <div class="container py-5 text-start">
    <h2 class="fw-bold mb-4 text-uppercase">Danh mục Áo TrendFit</h2>
    <div class="row g-4">
      <div class="col-6 col-md-3" v-for="item in listAo" :key="item.id">
        <div class="card border-0 shadow-sm">
          <img
            :src="
              item.anhChinh || 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=500'
            "
            class="card-img-top rounded-0"
          />
          <div class="card-body">
            <h6 class="fw-bold">{{ item.ten }}</h6>
            <router-link :to="'/product/' + item.id" class="btn btn-outline-dark rounded-0 w-100"
              >CHỌN SIZE/MÀU</router-link
            >
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
const listAo = ref([])
onMounted(async () => {
  const res = await axios.get('http://localhost:8080/api/public/products')
  listAo.value = res.data.filter((item) => item.danhMuc?.slug !== 'phu-kien')
})
</script>
