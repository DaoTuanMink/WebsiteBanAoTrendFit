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
          <div class="col" v-for="item in sanPhams" :key="item.id">
            <div class="trendfit-product-card">
              <div class="trendfit-img-container overflow-hidden position-relative mb-3 bg-light">
                <img
                  :src="
                    item.anhChinh ||
                    'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=500'
                  "
                  class="w-100 img-product-dynamic"
                  style="height: 300px; object-fit: cover"
                  alt="product"
                />
                <router-link
                  :to="'/product/' + item.id"
                  class="trendfit-quick-add position-absolute bottom-0 start-0 end-0 btn btn-dark rounded-0 py-2 text-white text-decoration-none text-uppercase fw-bold text-center"
                >
                  Xem chi tiết
                </router-link>
              </div>

              <div class="trendfit-info px-1">
                <div class="d-flex gap-1 mb-2">
                  <span
                    v-for="(color, idx) in getUniqueColors(item.bienTheSanPhams)"
                    :key="idx"
                    :style="{ backgroundColor: colorMap[color] || '#ccc' }"
                    class="rounded-circle border"
                    style="width: 14px; height: 14px"
                    :title="color"
                  ></span>
                </div>

                <span class="text-muted text-uppercase font-size-10 d-block mb-1">
                  {{ item.chatLieu || 'Premium Cotton' }}
                </span>
                <router-link
                  :to="'/product/' + item.id"
                  class="trendfit-title d-block mb-1 text-decoration-none text-dark fw-semibold"
                >
                  {{ item.ten }}
                </router-link>
                <p class="trendfit-price fw-bold text-danger m-0">
                  {{ formatPrice(getMinPrice(item.bienTheSanPhams)) }}
                </p>
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

// Map tên màu sang mã Hex để hiển thị chấm tròn
const colorMap = {
  Đen: '#000000',
  Trắng: '#FFFFFF',
  Xanh: '#0000FF',
  Đỏ: '#FF0000',
  Xám: '#808080',
  Navy: '#000080',
}

const getUniqueColors = (variants) => {
  if (!variants) return []
  return [...new Set(variants.map((v) => v.mauSac))]
}

const getMinPrice = (variants) => {
  if (!variants || variants.length === 0) return null
  const prices = variants.map((v) => v.gia)
  return Math.min(...prices)
}

const formatPrice = (value) => {
  if (!value) return 'Liên hệ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

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
.trendfit-product-card {
  transition: transform 0.3s ease;
}
.trendfit-product-card:hover {
  transform: translateY(-5px);
}
.img-product-dynamic {
  transition: transform 0.5s ease;
}
.trendfit-img-container:hover .img-product-dynamic {
  transform: scale(1.05);
}
.trendfit-quick-add {
  opacity: 0;
  transition: opacity 0.3s ease;
  background-color: rgba(0, 0, 0, 0.8) !important;
}
.trendfit-img-container:hover .trendfit-quick-add {
  opacity: 1;
}
.trendfit-title {
  font-size: 14px;
  line-height: 1.4;
  height: 2.8em;
  overflow: hidden;
}
.trendfit-price {
  font-size: 15px;
}
.font-size-10 {
  font-size: 10px;
}
</style>
