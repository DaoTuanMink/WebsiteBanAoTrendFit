<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

// Import 2 thanh giao diện dùng chung vừa quy hoạch vào đây
import LayoutHeader from '@/components/LayoutHeader.vue'
import LayoutFooter from '@/components/LayoutFooter.vue'

const listAo = ref([])
const listPhuKien = ref([])
const activeTab = ref('NEW')

const layAnhMacDinh = (slugDanhMuc) => {
  if (slugDanhMuc === 'ao-thun') {
    return 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=500'
  }
  return 'https://images.unsplash.com/photo-1624222247344-550fb8ef5521?w=500'
}

const taiDuLieuTrangChu = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/products')
    const allProducts = response.data

    listAo.value = allProducts.filter(
      (item) => item.danhMuc?.id === 1 || item.danhMuc?.slug === 'ao-thun',
    )
    listPhuKien.value = allProducts.filter(
      (item) => item.danhMuc?.id === 3 || item.danhMuc?.slug === 'phu-kien',
    )
  } catch (error) {
    console.error('Lỗi kết nối Backend, đang chạy dữ liệu dự phòng:', error)
  }
}

onMounted(() => {
  taiDuLieuTrangChu()
})
</script>

<template>
  <div class="owen-homepage bg-white">
    <LayoutHeader />

    <div
      id="owenHeroCarousel"
      class="carousel slide"
      data-bs-ride="carousel"
      data-bs-interval="4000"
    >
      <div class="carousel-inner">
        <div class="carousel-item active">
          <img
            src="https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=1600"
            class="d-block w-100 img-carousel-owen"
            alt="Owen Banner 1"
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

      <section class="mb-5">
        <div class="text-center mb-4">
          <div class="d-inline-flex gap-4 border-bottom pb-2">
            <span
              class="tab-owen text-uppercase fw-bold fs-5 cursor-pointer"
              :class="{ 'tab-owen-active': activeTab === 'NEW' }"
              @click="activeTab = 'NEW'"
              >Sản phẩm mới</span
            >
            <span
              class="tab-owen text-uppercase fw-bold fs-5 cursor-pointer"
              :class="{ 'tab-owen-active': activeTab === 'BEST' }"
              @click="activeTab = 'BEST'"
              >Bán chạy nhất</span
            >
          </div>
        </div>

        <div class="row row-cols-2 row-cols-md-3 row-cols-lg-4 g-4">
          <div class="col" v-for="item in listAo" :key="item.id">
            <div class="owen-product-card">
              <div class="owen-img-container overflow-hidden position-relative mb-3 bg-light">
                <img
                  :src="layAnhMacDinh(item.danhMuc?.slug)"
                  class="w-100 img-product-dynamic"
                  alt="product"
                />
                <div class="owen-tags position-absolute top-2 start-2 d-grid gap-1">
                  <span class="tag-new-owen">NEW</span>
                </div>
                <router-link
                  :to="'/product/' + item.id"
                  class="owen-quick-add position-absolute bottom-0 start-0 end-0 btn btn-dark rounded-0 py-2 text-uppercase fw-bold text-xs text-center text-decoration-none text-white"
                >
                  Xem chi tiết / Chọn Size
                </router-link>
              </div>
              <div class="owen-info text-start px-1">
                <span class="text-muted text-uppercase font-size-10 d-block mb-1"
                  >{{ item.chatLieu }} | {{ item.gioiTinh }}</span
                >
                <router-link
                  :to="'/product/' + item.id"
                  class="owen-title d-block mb-1 text-decoration-none text-dark fw-semibold"
                  >{{ item.ten }}</router-link
                >
                <p class="owen-price fw-bold text-danger m-0">Giá từ biến thể</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="w-100 my-5 position-relative text-center video-banner-section">
        <img
          src="https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=1600"
          class="w-100 rounded-0"
          style="height: 480px; object-fit: cover; filter: brightness(0.75)"
          alt="Lookbook Central"
        />
        <div class="position-absolute top-50 start-50 translate-middle text-white text-center">
          <h2 class="fw-black text-uppercase display-4 mb-3">TRENDFIT STUDIO COLLECTION</h2>
          <p class="small text-uppercase tracking-widest mb-4 text-white-50">
            Owen Fashion Lookbook Autumn-Spring
          </p>
          <div class="play-btn-circle d-inline-flex align-items-center justify-content-center">
            <i class="bi bi-play-fill fs-3 text-dark"></i>
          </div>
        </div>
      </section>

      <section class="mb-5">
        <div class="border-bottom pb-2 mb-4">
          <h2 class="fs-4 fw-black text-uppercase tracking-wider m-0 text-dark">Phụ Kiện Đi Kèm</h2>
        </div>
        <div class="row row-cols-2 row-cols-md-3 row-cols-lg-4 g-4">
          <div class="col" v-for="item in listPhuKien" :key="item.id">
            <div class="owen-product-card">
              <div class="owen-img-container overflow-hidden position-relative mb-3 bg-light">
                <img
                  :src="layAnhMacDinh(item.danhMuc?.slug)"
                  class="w-100 img-product-dynamic"
                  alt="accessory"
                />
                <router-link
                  :to="'/product/' + item.id"
                  class="owen-quick-add position-absolute bottom-0 start-0 end-0 btn btn-dark rounded-0 py-2 text-uppercase fw-bold text-xs text-center text-decoration-none text-white"
                >
                  Xem chi tiết
                </router-link>
              </div>
              <div class="owen-info text-start px-1">
                <span class="text-muted text-uppercase font-size-10 d-block mb-1">{{
                  item.chatLieu
                }}</span>
                <router-link
                  :to="'/product/' + item.id"
                  class="owen-title d-block mb-1 text-decoration-none text-dark fw-semibold"
                  >{{ item.ten }}</router-link
                >
                <p class="owen-price fw-bold text-dark m-0">Giá từ biến thể</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="w-100 my-5 position-relative rounded overflow-hidden newsletter-box">
        <img
          src="https://images.unsplash.com/photo-1483985988355-763728e1935b?w=1600"
          class="w-100"
          style="height: 320px; object-fit: cover"
          alt="Newsletter BG"
        />
        <div
          class="position-absolute top-50 start-50 translate-middle overlay-blur-box text-center p-4 rounded text-white"
        >
          <h4 class="fw-black text-uppercase mb-2 tracking-wider">ĐĂNG KÝ NHẬN KHUYẾN MÃI</h4>
          <p class="small text-white-50 mb-4">
            Đăng ký để nhận thông tin sản phẩm mới và các chương trình ưu đãi hấp dẫn
          </p>
          <div class="d-flex gap-2 max-width-input mx-auto">
            <input
              type="email"
              placeholder="Nhập email của bạn..."
              class="form-control rounded-0 bg-white border-0 text-dark small px-3"
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

<style scoped>
.font-size-11 {
  font-size: 11px;
}
.font-size-10 {
  font-size: 10px;
}
.fw-black {
  font-weight: 900;
}

.img-carousel-owen {
  height: 580px;
  object-fit: cover;
}
.img-product-dynamic {
  height: 380px;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.owen-product-card:hover .img-product-dynamic {
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

.tab-owen {
  color: #94a3b8;
}
.tab-owen-active {
  color: #000 !important;
  border-bottom: 3px solid #000;
  padding-bottom: 6px;
}

.owen-quick-add {
  transform: translateY(100%);
  transition: transform 0.3s ease;
}
.owen-product-card:hover .owen-quick-add {
  transform: translateY(0);
}
.tag-new-owen {
  background: #000;
  color: #fff;
  font-size: 9px;
  font-weight: 700;
  padding: 2px 6px;
}
.owen-title {
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
.cursor-pointer {
  cursor: pointer;
}
</style>
