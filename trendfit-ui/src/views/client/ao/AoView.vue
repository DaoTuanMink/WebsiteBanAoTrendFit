<template>
  <div class="ao-view bg-white text-start">
    <LayoutHeader />

    <div class="container py-5">
      <section class="mb-5 mt-5">
        <h3 class="text-center mb-4 fw-bold">SẢN PHẨM MỚI</h3>

        <div v-if="loading" class="text-center my-5">
          <div class="spinner-border text-dark" role="status"></div>
        </div>

        <div v-else class="row row-cols-2 row-cols-md-4 g-4">
          <div class="col" v-for="item in sanPhams.slice(0, 20)" :key="item.id">
            <div class="trendfit-product-card">
              <div class="trendfit-img-container overflow-hidden position-relative mb-3 bg-light">
                <img
                  :src="
                    item.anhChinh ||
                    'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=500'
                  "
                  class="w-100 img-product-dynamic"
                  alt="product"
                />
                <router-link
                  :to="'/product/' + item.id"
                  class="trendfit-quick-add position-absolute bottom-0 start-0 end-0 btn btn-dark rounded-0 py-2 text-white text-decoration-none text-uppercase fw-bold text-xs text-center"
                >
                  Xem chi tiết
                </router-link>
              </div>
              <div class="trendfit-info px-1">
                <span class="text-muted text-uppercase font-size-10 d-block mb-1">
                  {{ item.chatLieu || 'Premium Cotton' }}
                </span>
                <router-link
                  :to="'/product/' + item.id"
                  class="trendfit-title d-block mb-1 text-decoration-none text-dark fw-semibold"
                >
                  {{ item.ten }}
                </router-link>
                <p class="trendfit-price fw-bold text-danger m-0">350.000 đ</p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <LayoutFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import LayoutHeader from '@/components/LayoutHeader.vue'
import LayoutFooter from '@/components/LayoutFooter.vue'

const sanPhams = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/public/products')
    sanPhams.value = res.data
  } catch (err) {
    console.error('Lỗi tải sản phẩm:', err)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
/* Bạn copy lại các style .img-cover-block, .block-label, .trendfit-product-card... từ file cũ sang đây */
</style>
