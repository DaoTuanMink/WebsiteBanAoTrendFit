<template>
  <div class="trendfit-homepage bg-white text-start">
    <LayoutHeader />

    <div
      id="trendfitHeroCarousel"
      class="carousel slide"
      data-bs-ride="carousel"
      data-bs-interval="4000"
    >
      <div class="carousel-inner">
        <div class="carousel-item active">
          <img
            src="https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=1600"
            class="d-block w-100 img-carousel-trendfit"
            alt="TrendFit Banner 1"
          />
        </div>
      </div>
    </div>

    <div class="container-fluid px-4 px-md-5 pt-5">
      <section class="row g-3 my-4">
        <div class="col-12 col-md-6 position-relative categories-block">
          <img
            src="https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=800"
            class="w-100 img-cover-block"
            alt="Áo thun"
          />
          <div class="block-label">ÁO THUN TRENDY</div>
        </div>
        <div class="col-12 col-md-6 position-relative categories-block">
          <img
            src="https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=800"
            class="w-100 img-cover-block"
            alt="Phụ kiện"
          />
          <div class="block-label">PHỤ KIỆN CAO CẤP</div>
        </div>
      </section>

      <section class="mb-5 mt-5">
        <div class="text-center mb-4">
          <div class="d-inline-flex gap-4 border-bottom pb-2">
            <span
              class="tab-trendfit text-uppercase fw-bold fs-5 cursor-pointer"
              :class="{ 'tab-trendfit-active': activeTab === 'NEW' }"
              @click="activeTab = 'NEW'"
              >SẢN PHẨM MỚI</span
            >
            <span
              class="tab-trendfit text-uppercase fw-bold fs-5 cursor-pointer"
              :class="{ 'tab-trendfit-active': activeTab === 'BEST' }"
              @click="activeTab = 'BEST'"
              >BÁN CHẠY NHẤT</span
            >
          </div>
        </div>

        <div v-if="loading" class="text-center my-5 py-5">
          <div class="spinner-border text-dark" role="status"></div>
          <p class="mt-2 text-muted small fw-bold">Đang kết nối kho áo TrendFit...</p>
        </div>

        <div v-else class="row row-cols-2 row-cols-md-3 row-cols-lg-4 g-4">
          <div class="col" v-for="item in sanPhams" :key="item.sanPham.id">
            <div class="trendfit-product-card">
              <div class="trendfit-img-container overflow-hidden position-relative mb-3 bg-light">
                <img
                  :src="getAnhChinh(item.anhSanPhams)"
                  class="w-100 img-product-dynamic"
                  alt="product"
                />
                <div class="trendfit-tags position-absolute top-2 start-2 d-grid gap-1">
                  <span class="tag-new-trendfit">NEW</span>
                </div>
                <router-link
                  :to="'/product/' + item.sanPham.id"
                  class="trendfit-quick-add position-absolute bottom-0 start-0 end-0 btn btn-dark rounded-0 py-2 text-uppercase fw-bold text-xs text-center text-decoration-none text-white"
                >
                  Xem chi tiết / Đặt mua
                </router-link>
              </div>

              <div class="trendfit-info text-start px-1">
                <span class="text-muted text-uppercase font-size-10 d-block mb-1">
                  {{ item.sanPham.chatLieu || 'Premium Cotton' }} |
                  {{ item.sanPham.xuatXu || 'Việt Nam' }}
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

        <div v-if="!loading && sanPhams.length === 0" class="text-center text-muted py-5 fw-bold">
          Không tìm thấy trang phục TrendFit nào khớp với điều kiện lọc!
        </div>
      </section>

      <section class="w-100 my-5 position-relative text-center video-banner-section">
        <img
          src="https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=1600"
          class="w-100 rounded-0"
          style="height: 480px; object-fit: cover; filter: brightness(0.65)"
          alt="Lookbook Central"
        />
        <div
          class="position-absolute top-50 start-50 translate-middle text-white text-center w-100"
        >
          <h2 class="fw-black text-uppercase display-4 mb-3">TRENDFIT STUDIO COLLECTION</h2>
          <p class="small text-uppercase tracking-widest mb-4 text-white-50">
            TrendFit Fashion Lookbook Autumn-Spring
          </p>
          <div class="play-btn-circle d-inline-flex align-items-center justify-content-center">
            <i class="ri-play-fill fs-3 text-dark"></i>
          </div>
        </div>
      </section>

      <section class="w-100 my-5 position-relative rounded overflow-hidden newsletter-box">
        <img
          src="https://images.unsplash.com/photo-1483985988355-763728e1935b?w=1600"
          class="w-100"
          style="height: 320px; object-fit: cover; filter: brightness(0.5)"
          alt="Newsletter BG"
        />
        <div
          class="position-absolute top-50 start-50 translate-middle overlay-blur-box text-center p-4 rounded text-white"
        >
          <h4 class="fw-black text-uppercase mb-2 tracking-wider">ĐĂNG KÝ NHẬN KHUYẾN MÃI</h4>
          <p class="small text-white-50 mb-4">
            Đăng ký để nhận thông tin sản phẩm mới và các chương trình ưu đãi hấp dẫn từ TrendFit
          </p>
          <div class="d-flex gap-2 max-width-input mx-auto">
            <input
              type="email"
              placeholder="Nhập email của bạn..."
              class="form-control rounded-0 bg-white border-0 text-dark small px-3 shadow-none"
            />
            <button class="btn btn-dark rounded-0 px-4 text-uppercase fw-bold text-xs">
              Đăng ký
            </button>
          </div>
        </div>
      </section>
    </div>

    <LayoutFooter />
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'
import LayoutHeader from '@/components/LayoutHeader.vue'
import LayoutFooter from '@/components/LayoutFooter.vue'

const sanPhams = ref([])
const listDanhMuc = ref([])
const listThuongHieu = ref([])
const activeTab = ref('NEW')
const loading = ref(true)

const filters = reactive({
  search: '',
  danhMucId: '',
  thuongHieuId: '',
})

const getAnhChinh = (anhList) => {
  if (anhList && anhList.length > 0) {
    // Ưu tiên lấy ảnh có laAnhChinh == true, nếu không có thì lấy ảnh đầu tiên
    const anh = anhList.find((a) => a.laAnhChinh === true) || anhList[0]
    return anh.urlAnh
  }
  // Ảnh mặc định nếu không có ảnh
  return 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=500'
}

const taiDanhSachSanPham = async () => {
  try {
    loading.value = true
    // Đồng bộ gọi API Client thu thập danh sách áo real-time
    const res = await axios.get('http://localhost:8080/api/public/products', {
      params: {
        search: filters.search,
        danhMucId: filters.danhMucId || null,
        thuongHieuId: filters.thuongHieuId || null,
      },
    })
    sanPhams.value = res.data
  } catch (err) {
    console.error('Lỗi kết nối API public sản phẩm:', err)
  } finally {
    loading.value = false
  }
}

const formatPrice = (v) => {
  if (!v || v === 0) return 'Liên hệ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)
}

const getMinPrice = (variants) => {
  if (!variants || variants.length === 0) return 0
  const prices = variants.map((v) => Number(v.gia || 0)).filter((p) => p > 0)
  return prices.length > 0 ? Math.min(...prices) : 0
}

onMounted(async () => {
  await taiDanhSachSanPham()

  // Gọi API lấy danh mục và thương hiệu thật từ DB
  try {
    const [dmRes, thRes] = await Promise.all([
      axios.get('http://localhost:8080/api/public/categories'),
      axios.get('http://localhost:8080/api/public/brands'),
    ])
    listDanhMuc.value = dmRes.data
    listThuongHieu.value = thRes.data
  } catch (e) {
    console.error('Lỗi tải danh mục/thương hiệu:', e)
  }
})
</script>

<style scoped>
.font-size-10 {
  font-size: 10px;
}
.fw-black {
  font-weight: 900;
}
.cursor-pointer {
  cursor: pointer;
}
.img-carousel-trendfit {
  height: 580px;
  object-fit: cover;
}
.img-product-dynamic {
  height: 380px;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.trendfit-product-card:hover .img-product-dynamic {
  transform: scale(1.04);
}
.img-cover-block {
  height: 340px;
  object-fit: cover;
  filter: brightness(0.85);
  transition: filter 0.3s;
}
.categories-block:hover .img-cover-block {
  filter: brightness(1);
  cursor: pointer;
}
.block-label {
  position: absolute;
  bottom: 25px;
  left: 50%;
  transform: translateX(-50%);
  background: #fff;
  color: #000;
  padding: 8px 24px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}
.tab-trendfit {
  color: #94a3b8;
}
.tab-trendfit-active {
  color: #000 !important;
  border-bottom: 3px solid #000;
  padding-bottom: 6px;
}
.trendfit-quick-add {
  transform: translateY(100%);
  transition: transform 0.3s ease;
}
.trendfit-product-card:hover .trendfit-quick-add {
  transform: translateY(0);
}
.tag-new-trendfit {
  background: #000;
  color: #fff;
  font-size: 9px;
  font-weight: 700;
  padding: 2px 6px;
}
.trendfit-title {
  font-size: 13px;
  height: 36px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}
.play-btn-circle {
  width: 60px;
  height: 60px;
  background: #fff;
  border-radius: 50%;
  cursor: pointer;
  transition: transform 0.2s;
}
.play-btn-circle:hover {
  transform: scale(1.1);
}
.overlay-blur-box {
  background: rgba(0, 0, 0, 0.65);
  backdrop-filter: blur(6px);
  width: 85%;
  max-width: 550px;
}
.max-width-input {
  max-width: 400px;
}
</style>
