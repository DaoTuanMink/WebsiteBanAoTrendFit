<template>
  <div class="ao-view bg-white text-start">
    <LayoutHeader />

    <div class="container py-5">
      <section class="mb-5 mt-5">
        <h3 class="text-center mb-4 fw-bold">SẢN PHẨM MỚI</h3>

        <!-- ===================== THANH TÌM KIẾM & BỘ LỌC (client-side) ===================== -->
        <div class="filter-bar bg-light border rounded-3 p-3 mb-4">
          <div class="row g-3 align-items-end">
            <div class="col-12 col-md-4">
              <label class="form-label small fw-bold mb-1">Tìm theo tên sản phẩm</label>
              <input
                v-model.trim="filters.keyword"
                type="text"
                class="form-control"
                placeholder="Ví dụ: áo thun, hoodie..."
              />
            </div>

            <div class="col-6 col-md-3">
              <label class="form-label small fw-bold mb-1">Khoảng giá</label>
              <select v-model="filters.priceRange" class="form-select">
                <option value="all">Tất cả mức giá</option>
                <option value="0-200000">Dưới 200.000đ</option>
                <option value="200000-500000">200.000đ - 500.000đ</option>
                <option value="500000-1000000">500.000đ - 1.000.000đ</option>
                <option value="1000000-999999999">Trên 1.000.000đ</option>
              </select>
            </div>

            <div class="col-6 col-md-2">
              <label class="form-label small fw-bold mb-1">Kích cỡ</label>
              <select v-model="filters.size" class="form-select">
                <option value="">Tất cả size</option>
                <option v-for="size in allSizes" :key="size" :value="size">{{ size }}</option>
              </select>
            </div>

            <div class="col-6 col-md-2">
              <label class="form-label small fw-bold mb-1">Màu sắc</label>
              <select v-model="filters.color" class="form-select">
                <option value="">Tất cả màu</option>
                <option v-for="color in allColors" :key="color" :value="color">{{ color }}</option>
              </select>
            </div>

            <div class="col-6 col-md-1 d-grid">
              <button class="btn btn-outline-dark" @click="resetFilters" title="Xóa bộ lọc">
                Xóa
              </button>
            </div>
          </div>
        </div>
        <!-- ============================================================================== -->

        <div v-if="loading" class="text-center my-5">
          <div class="spinner-border text-dark" role="status"></div>
        </div>

        <div v-else>
          <p class="text-muted mb-3 small">
            Tìm thấy <b>{{ filteredSanPhams.length }}</b> sản phẩm
          </p>

          <div v-if="filteredSanPhams.length === 0" class="alert alert-info text-center">
            Không tìm thấy sản phẩm phù hợp với bộ lọc hiện tại.
          </div>

          <div v-else class="row row-cols-2 row-cols-md-4 g-4">
            <div class="col" v-for="item in filteredSanPhams" :key="item.sanPham.id">
              <div class="trendfit-product-card">
                <div class="trendfit-img-container overflow-hidden position-relative mb-3 bg-light">
                  <img
                    :src="getAnhChinh(item.anhSanPhams)"
                    class="w-100 img-product-dynamic"
                    style="height: 300px; object-fit: cover"
                    alt="product"
                  />
                  <router-link
                    :to="'/product/' + item.sanPham.id"
                    class="trendfit-quick-add position-absolute bottom-0 start-0 end-0 btn btn-dark rounded-0 py-2 text-white text-decoration-none text-uppercase fw-bold text-center"
                  >
                    Xem chi tiết
                  </router-link>
                </div>

                <div class="trendfit-info px-1">
                  <div
                    class="d-flex justify-content-between font-size-10 text-uppercase text-muted mb-1"
                  >
                    <span>{{ item.sanPham.danhMuc?.ten || 'Chưa phân loại' }}</span>
                    <span class="fw-bold text-dark">{{
                      item.sanPham.thuongHieu?.ten || 'No Brand'
                    }}</span>
                  </div>

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
                    {{ item.sanPham.chatLieu || 'Premium Cotton' }}
                  </span>

                  <router-link
                    :to="'/product/' + item.sanPham.id"
                    class="trendfit-title d-block mb-1 text-decoration-none text-dark fw-semibold"
                  >
                    {{ item.sanPham.ten }}
                  </router-link>

                  <p class="trendfit-price fw-bold text-danger m-0">
                    {{ formatPrice(getMinPrice(item.bienTheSanPhams)) }}
                  </p>
                </div>
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
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import LayoutHeader from '@/components/LayoutHeader.vue'
import LayoutFooter from '@/components/LayoutFooter.vue'

const sanPhams = ref([])
const loading = ref(true)

const filters = ref({
  keyword: '',
  priceRange: 'all',
  size: '',
  color: '',
})

const colorMap = {
  Đen: '#000000',
  Trắng: '#FFFFFF',
  Xanh: '#0000FF',
  Đỏ: '#FF0000',
  Xám: '#808080',
  Navy: '#000080',
}

const getColorName = (variant) => {
  if (!variant) return ''
  if (variant.mauSac && typeof variant.mauSac === 'object') {
    return String(variant.mauSac.tenMau || '').trim()
  }
  return String(variant.mauSac || '').trim()
}

const getSizeName = (variant) => {
  if (!variant) return ''
  if (variant.kichCo && typeof variant.kichCo === 'object') {
    return String(variant.kichCo.tenKichCo || '').trim()
  }
  return String(variant.kichCo || '').trim()
}

const getUniqueColors = (variants) => {
  if (!variants) return []
  return [...new Set(variants.map((v) => getColorName(v)).filter(Boolean))]
}

const getUniqueSizes = (variants) => {
  if (!variants) return []
  return [...new Set(variants.map((v) => getSizeName(v)).filter(Boolean))]
}

const getMinPrice = (variants) => {
  if (!variants || variants.length === 0) return 0
  const prices = variants.map((v) => Number(v.giaSale ?? v.gia ?? 0)).filter((v) => v > 0)
  if (prices.length === 0) return 0
  return Math.min(...prices)
}

const formatPrice = (v) => {
  if (!v || v === 0) return 'Liên hệ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)
}

const getAnhChinh = (anhList) => {
  if (anhList && anhList.length > 0) {
    const anh = anhList.find((a) => a.laAnhChinh === true) || anhList[0]
    return anh.urlAnh
  }
  return 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=500'
}

const allSizes = computed(() => {
  const set = new Set()
  sanPhams.value.forEach((item) => {
    getUniqueSizes(item.bienTheSanPhams).forEach((s) => set.add(s))
  })
  return [...set].sort()
})

const allColors = computed(() => {
  const set = new Set()
  sanPhams.value.forEach((item) => {
    getUniqueColors(item.bienTheSanPhams).forEach((c) => set.add(c))
  })
  return [...set].sort()
})

const normalize = (str) =>
  String(str || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()

const filteredSanPhams = computed(() => {
  const keyword = normalize(filters.value.keyword)
  const [minPrice, maxPrice] =
    filters.value.priceRange === 'all'
      ? [null, null]
      : filters.value.priceRange.split('-').map(Number)

  return sanPhams.value.filter((item) => {
    if (keyword && !normalize(item.sanPham.ten).includes(keyword)) {
      return false
    }

    if (minPrice !== null) {
      const price = getMinPrice(item.bienTheSanPhams)
      if (price < minPrice || price > maxPrice) return false
    }

    if (filters.value.size) {
      const sizes = getUniqueSizes(item.bienTheSanPhams)
      if (!sizes.includes(filters.value.size)) return false
    }

    if (filters.value.color) {
      const colors = getUniqueColors(item.bienTheSanPhams)
      if (!colors.includes(filters.value.color)) return false
    }

    return true
  })
})

const resetFilters = () => {
  filters.value = { keyword: '', priceRange: 'all', size: '', color: '' }
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
.filter-bar {
  position: sticky;
  top: 0;
  z-index: 5;
}
</style>
