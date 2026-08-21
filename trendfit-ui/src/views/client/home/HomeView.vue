<template>
  <div class="tf-home bg-light text-dark">
    <LayoutHeader />

    <!-- HERO SECTION -->
    <section
      class="tf-hero position-relative d-flex align-items-center overflow-hidden"
      style="min-height: 85vh"
    >
      <div class="position-absolute w-100 h-100 tf-hero-bg">
        <img
          src="https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=1920&q=80"
          alt="TrendFit New Collection 2026"
          class="w-100 h-100 object-fit-cover"
        />
        <div class="tf-hero-overlay position-absolute top-0 start-0 w-100 h-100"></div>
      </div>
      <div class="container position-relative z-2 tf-anim-hero">
        <div class="row">
          <div class="col-lg-8 col-xl-6">
            <span
              class="badge rounded-pill tf-badge-glass mb-3 px-3 py-2 text-uppercase letter-spacing-1"
            >
              <i class="ri-sparkling-2-fill me-1"></i> New Collection 2026
            </span>
            <h1
              class="display-2 fw-bold text-white mb-3"
              style="font-family: 'Space Grotesk', sans-serif"
            >
              Dress Your <br /><span class="tf-gradient-text">Style</span>
            </h1>
            <p class="lead text-white-50 mb-4">
              Shop bán áo TrendFit — áo thun, sơ mi, hoodie. Form chuẩn, chất vải tốt.
            </p>
            <div class="d-flex flex-wrap gap-3">
              <router-link
                to="/ao"
                class="btn btn-primary btn-lg tf-btn-gradient border-0 px-4 fw-bold"
              >
                Mua ngay <i class="ri-arrow-right-line ms-1"></i>
              </router-link>
              <a href="#best-sellers" class="btn btn-outline-light btn-lg px-4 fw-bold">Khám phá</a>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- DANH MỤC NỔI BẬT -->
    <section class="py-5 tf-reveal">
      <div class="container-fluid px-4 px-lg-5 mt-4">
        <div class="mb-4">
          <span class="text-primary fw-bold small text-uppercase letter-spacing-1"
            >🔥 Danh mục nổi bật</span
          >
          <h2 class="fw-bold" style="font-family: 'Space Grotesk', sans-serif">
            Danh mục áo nổi bật
          </h2>
        </div>
        <div class="row g-4">
          <div v-for="cat in categories" :key="cat.name" class="col-6 col-md-3 tf-reveal-item">
            <router-link
              :to="cat.link"
              class="card text-white border-0 overflow-hidden tf-hover-card rounded-4"
            >
              <img
                :src="cat.img"
                :alt="cat.name"
                class="card-img h-100 object-fit-cover"
                style="aspect-ratio: 3/4"
              />
              <div
                class="card-img-overlay d-flex flex-column justify-content-end tf-overlay-gradient p-3 p-lg-4"
              >
                <h5
                  class="card-title fw-bold mb-0"
                  style="font-family: 'Space Grotesk', sans-serif"
                >
                  {{ cat.name }}
                </h5>
                <small class="text-white-50">{{ cat.count }}</small>
              </div>
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- SẢN PHẨM BÁN CHẠY -->
    <section id="best-sellers" class="py-5 bg-white tf-reveal">
      <div class="container-fluid px-4 px-lg-5 my-4">
        <div class="d-flex flex-wrap justify-content-between align-items-end mb-4 gap-3">
          <div>
            <span class="text-primary fw-bold small text-uppercase letter-spacing-1"
              >⭐ Sản phẩm bán chạy</span
            >
            <h2 class="fw-bold m-0" style="font-family: 'Space Grotesk', sans-serif">
              Được yêu thích nhất
            </h2>
          </div>
          <div class="nav nav-pills bg-light p-1 rounded-3 border">
            <button
              class="nav-link btn-sm fw-semibold text-secondary"
              :class="{ 'active bg-dark text-white': activeTab === 'NEW' }"
              @click="activeTab = 'NEW'"
            >
              Sản phẩm mới
            </button>
            <button
              class="nav-link btn-sm fw-semibold text-secondary"
              :class="{ 'active bg-dark text-white': activeTab === 'BEST' }"
              @click="activeTab = 'BEST'"
            >
              Bán chạy
            </button>
          </div>
        </div>

        <div v-if="loading" class="text-center py-5 text-secondary">
          <div class="spinner-border text-primary mb-3" role="status"></div>
          <p>Đang tải bộ sưu tập TrendFit...</p>
        </div>

        <div v-else class="row g-4">
          <div
            v-for="item in sanPhams"
            :key="item.sanPham.id"
            class="col-6 col-lg-3 tf-reveal-item"
          >
            <div class="card h-100 border-0 shadow-sm tf-hover-card rounded-4">
              <div
                class="position-relative overflow-hidden bg-light rounded-top-4"
                style="aspect-ratio: 3/4"
              >
                <img
                  :src="getAnhChinh(item.anhSanPhams)"
                  class="card-img-top h-100 object-fit-cover tf-zoom-img"
                  :alt="item.sanPham.ten"
                />
                <span class="badge bg-dark position-absolute top-0 start-0 m-3 px-2 py-1">NEW</span>
              </div>
              <div class="card-body p-3 p-lg-4">
                <small class="text-uppercase text-muted fw-bold" style="font-size: 11px">
                  {{ item.sanPham.chatLieu || 'Premium Cotton' }} ·
                  {{ item.sanPham.xuatXu || 'Việt Nam' }}
                </small>
                <router-link
                  :to="'/product/' + item.sanPham.id"
                  class="d-block text-dark text-decoration-none fw-bold mt-1 mb-2 text-truncate-2"
                  style="font-size: 15px; min-height: 2.8em"
                >
                  {{ item.sanPham.ten }}
                </router-link>
                <h5
                  class="text-danger fw-bold m-0"
                  style="font-family: 'Space Grotesk', sans-serif"
                >
                  {{ formatPrice(getMinPrice(item.bienTheSanPhams)) }}
                </h5>
              </div>
            </div>
          </div>
        </div>

        <div
          v-if="!loading && sanPhams.length === 0"
          class="text-center py-5 text-secondary fw-semibold"
        >
          Chưa có sản phẩm phù hợp. Hãy quay lại sau!
        </div>

        <div class="text-center mt-5">
          <router-link
            to="/ao"
            class="btn btn-outline-primary fw-bold px-4 py-2 rounded-3 border-2"
          >
            Xem tất cả sản phẩm <i class="ri-arrow-right-line align-middle"></i>
          </router-link>
        </div>
      </div>
    </section>

    <!-- KHUYẾN MÃI -->
    <section class="py-5 tf-reveal">
      <div class="container-fluid px-4 px-lg-5 my-4">
        <div class="mb-4">
          <span class="text-primary fw-bold small text-uppercase letter-spacing-1"
            >🎁 Khuyến mãi</span
          >
          <h2 class="fw-bold" style="font-family: 'Space Grotesk', sans-serif">
            Ưu đãi đang diễn ra
          </h2>
        </div>
        <div class="row g-4">
          <!-- Promo Main -->
          <div class="col-lg-6 tf-reveal-item">
            <div class="card text-white border-0 overflow-hidden tf-hover-card rounded-4 h-100">
              <img
                src="https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=1000&q=80"
                alt="Flash Sale"
                class="card-img h-100 object-fit-cover"
                style="min-height: 300px"
              />
              <div
                class="card-img-overlay d-flex flex-column justify-content-end tf-overlay-gradient p-4 p-md-5"
              >
                <span class="badge bg-primary align-self-start mb-3 px-3 py-2 letter-spacing-1"
                  >FLASH SALE</span
                >
                <h3 class="display-6 fw-bold mb-2" style="font-family: 'Space Grotesk', sans-serif">
                  Giảm đến 40%
                </h3>
                <p class="fs-5 text-white-50 mb-4">Áo thun & Hoodie chọn lọc</p>
                <router-link
                  to="/ao"
                  class="btn btn-primary tf-btn-gradient border-0 px-4 py-2 align-self-start fw-bold rounded-3"
                  >Mua ngay</router-link
                >
              </div>
            </div>
          </div>
          <!-- Promo Sub -->
          <div class="col-lg-6">
            <div class="row g-4 h-100">
              <div class="col-12 tf-reveal-item h-50">
                <div class="card text-white border-0 overflow-hidden tf-hover-card rounded-4 h-100">
                  <img
                    src="https://images.unsplash.com/photo-1483985988355-763728e1935b?w=600&q=80"
                    alt="Freeship"
                    class="card-img h-100 object-fit-cover"
                    style="min-height: 200px"
                  />
                  <div
                    class="card-img-overlay d-flex flex-column justify-content-end tf-overlay-gradient p-4"
                  >
                    <span class="badge bg-primary align-self-start mb-2 px-2 py-1 letter-spacing-1"
                      >FREESHIP</span
                    >
                    <h4 class="fw-bold mb-1" style="font-family: 'Space Grotesk', sans-serif">
                      Miễn phí ship
                    </h4>
                    <p class="text-white-50 mb-0 small">Đơn từ 499.000đ</p>
                  </div>
                </div>
              </div>
              <div class="col-12 tf-reveal-item h-50">
                <div class="card text-white border-0 overflow-hidden tf-hover-card rounded-4 h-100">
                  <img
                    src="https://images.unsplash.com/photo-1558769132-cb1aea458c5e?w=600&q=80"
                    alt="Member"
                    class="card-img h-100 object-fit-cover"
                    style="min-height: 200px"
                  />
                  <div
                    class="card-img-overlay d-flex flex-column justify-content-end tf-overlay-gradient p-4"
                  >
                    <span class="badge bg-primary align-self-start mb-2 px-2 py-1 letter-spacing-1"
                      >MEMBER</span
                    >
                    <h4 class="fw-bold mb-1" style="font-family: 'Space Grotesk', sans-serif">
                      Tích điểm x2
                    </h4>
                    <p class="text-white-50 mb-0 small">Thành viên TrendFit</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- LOOKBOOK -->
    <section
      class="position-relative overflow-hidden tf-reveal d-flex align-items-center justify-content-center text-center text-white"
      style="height: 480px"
    >
      <img
        src="https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=1920&q=80"
        alt="TrendFit Studio"
        class="position-absolute w-100 h-100 object-fit-cover z-0 tf-lookbook-img"
      />
      <div class="position-absolute w-100 h-100 bg-dark opacity-50 z-1"></div>
      <div class="position-relative z-2">
        <span class="text-primary fw-bold text-uppercase letter-spacing-1 small"
          >Studio Collection</span
        >
        <h2 class="display-4 fw-bold my-2" style="font-family: 'Space Grotesk', sans-serif">
          TRENDFIT STUDIO
        </h2>
        <p class="text-white-50 text-uppercase letter-spacing-1 mb-4 small">
          Lookbook Autumn — Spring 2026
        </p>
        <button
          class="btn btn-light rounded-circle shadow-lg tf-play-btn d-flex align-items-center justify-content-center mx-auto"
          style="width: 64px; height: 64px"
        >
          <i class="ri-play-fill fs-3 text-dark ms-1"></i>
        </button>
      </div>
    </section>

    <!-- BLOG -->
    <section class="py-5 bg-white tf-reveal">
      <div class="container-fluid px-4 px-lg-5 my-4">
        <div class="d-flex justify-content-between align-items-end mb-4">
          <div>
            <span class="text-primary fw-bold small text-uppercase letter-spacing-1"
              >📢 Blog thời trang</span
            >
            <h2 class="fw-bold m-0" style="font-family: 'Space Grotesk', sans-serif">
              Gợi ý phối áo
            </h2>
          </div>
          <a href="#" class="text-primary fw-bold text-decoration-none"
            >Xem tất cả <i class="ri-arrow-right-line align-middle"></i
          ></a>
        </div>
        <div class="row g-4">
          <div v-for="post in blogs" :key="post.title" class="col-md-6 col-lg-4 tf-reveal-item">
            <div class="card h-100 border-0 shadow-sm tf-hover-card rounded-4 overflow-hidden">
              <div class="overflow-hidden bg-light" style="aspect-ratio: 16/10">
                <img
                  :src="post.img"
                  :alt="post.title"
                  class="card-img-top w-100 h-100 object-fit-cover tf-zoom-img"
                />
              </div>
              <div class="card-body p-4">
                <span
                  class="text-primary fw-bold small text-uppercase letter-spacing-1 mb-2 d-block"
                  >{{ post.tag }}</span
                >
                <h5
                  class="fw-bold mb-3 text-dark"
                  style="font-family: 'Space Grotesk', sans-serif; line-height: 1.4"
                >
                  {{ post.title }}
                </h5>
                <p class="text-secondary small mb-3">{{ post.excerpt }}</p>
                <span class="text-muted small fw-semibold">{{ post.date }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ĐÁNH GIÁ -->
    <section class="py-5 bg-light tf-reveal text-center">
      <div class="container-fluid px-4 px-lg-5 my-4">
        <div class="mb-5">
          <span class="text-primary fw-bold small text-uppercase letter-spacing-1"
            >💬 Đánh giá khách hàng</span
          >
          <h2 class="fw-bold m-0" style="font-family: 'Space Grotesk', sans-serif">
            Khách nói gì về áo TrendFit
          </h2>
        </div>
        <div class="row g-4 text-start">
          <div v-for="r in reviews" :key="r.name" class="col-md-6 col-lg-4 tf-reveal-item">
            <div class="card h-100 border-0 shadow-sm p-4 rounded-4 tf-hover-card">
              <div class="text-warning mb-3 fs-5">
                <i v-for="n in 5" :key="n" class="ri-star-fill me-1"></i>
              </div>
              <p class="text-secondary mb-4 flex-grow-1" style="line-height: 1.6">“{{ r.text }}”</p>
              <div class="d-flex align-items-center gap-3">
                <div
                  class="rounded-3 tf-btn-gradient text-white d-flex align-items-center justify-content-center fw-bold fs-5 shadow-sm"
                  style="width: 48px; height: 48px"
                >
                  {{ r.name.charAt(0) }}
                </div>
                <div>
                  <strong class="d-block text-dark">{{ r.name }}</strong>
                  <span class="small text-muted">{{ r.role }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- NEWSLETTER -->
    <section class="py-5 tf-reveal">
      <div class="container-fluid px-4 px-lg-5 my-2">
        <div
          class="bg-dark text-white rounded-4 p-4 p-md-5 d-flex flex-column flex-lg-row align-items-center justify-content-between gap-4 shadow-lg tf-newsletter-box border border-secondary border-opacity-25"
        >
          <div>
            <h3 class="fw-bold mb-2" style="font-family: 'Space Grotesk', sans-serif">
              Đăng ký nhận khuyến mãi
            </h3>
            <p class="text-white-50 mb-0">
              Sản phẩm mới & ưu đãi độc quyền gửi thẳng vào email của bạn.
            </p>
          </div>
          <form class="d-flex w-100 gap-2" style="max-width: 450px" @submit.prevent>
            <input
              type="email"
              class="form-control bg-white bg-opacity-10 text-white border-secondary border-opacity-50 px-4"
              placeholder="Nhập email của bạn..."
              required
            />
            <button
              type="submit"
              class="btn btn-primary px-4 fw-bold tf-btn-gradient border-0 rounded-3"
            >
              Đăng ký
            </button>
          </form>
        </div>
      </div>
    </section>

    <LayoutFooter />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import axios from 'axios'
import LayoutHeader from '@/components/LayoutHeader.vue'
import LayoutFooter from '@/components/LayoutFooter.vue'

const sanPhams = ref([])
const activeTab = ref('NEW')
const loading = ref(true)

const categories = [
  {
    name: 'Áo thun',
    count: '120+ mẫu',
    link: '/ao',
    img: 'https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=600&q=80',
  },
  {
    name: 'Áo sơ mi',
    count: '80+ mẫu',
    link: '/ao',
    img: 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=600&q=80',
  },
  {
    name: 'Hoodie / Áo khoác',
    count: '45+ mẫu',
    link: '/ao',
    img: 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=600&q=80',
  },
  {
    name: 'Áo polo',
    count: '60+ mẫu',
    link: '/ao',
    img: 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=600&q=80',
  },
]

const blogs = [
  {
    tag: 'Style Guide',
    title: '5 cách mix áo thun basic mỗi ngày',
    excerpt: 'Gợi ý phối áo thun với quần jean, short, cargo cho look gọn gàng.',
    date: '28.07.2026',
    img: 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&q=80',
  },
  {
    tag: 'Trend',
    title: 'Chọn size áo thun chuẩn — tránh form rộng/chật',
    excerpt: 'Bảng size chi tiết giúp bạn chọn áo vừa vặn ngay lần đầu.',
    date: '22.07.2026',
    img: 'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=600&q=80',
  },
  {
    tag: 'Lookbook',
    title: 'Áo hoodie mặc layer mùa se lạnh',
    excerpt: 'Cách chồng áo hoodie + áo thun giữ ấm mà không cồng kềnh.',
    date: '15.07.2026',
    img: 'https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=600&q=80',
  },
]

const reviews = [
  {
    name: 'Minh Anh',
    role: 'Khách hàng thân thiết',
    text: 'Chất vải mềm, form chuẩn. Giao hàng nhanh, đóng gói đẹp. Sẽ ủng hộ dài dài.',
  },
  {
    name: 'Hoàng Nam',
    role: 'Mua lần đầu',
    text: 'Website dễ dùng, chọn size rõ ràng. Áo mặc rất thoải mái, đúng như mô tả.',
  },
  {
    name: 'Thu Hà',
    role: 'Reviewer thời trang',
    text: 'TrendFit đang làm rất tốt phần trải nghiệm mua sắm. Design hiện đại, sản phẩm chất.',
  },
]

const getAnhChinh = (anhList) => {
  if (anhList && anhList.length > 0) {
    const anh = anhList.find((a) => a.laAnhChinh === true) || anhList[0]
    return anh.urlAnh
  }
  return 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=500'
}

const taiDanhSachSanPham = async () => {
  try {
    loading.value = true
    const res = await axios.get('http://localhost:8080/api/public/products')
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

let observer = null

const setupReveal = () => {
  const prefersReduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (prefersReduced) {
    document.querySelectorAll('.tf-reveal, .tf-reveal-item').forEach((el) => {
      el.classList.add('is-visible')
    })
    return
  }

  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible')
          // Stagger children
          const items = entry.target.querySelectorAll('.tf-reveal-item')
          items.forEach((item, i) => {
            item.style.transitionDelay = `${i * 0.07}s`
            item.classList.add('is-visible')
          })
          observer.unobserve(entry.target)
        }
      })
    },
    { threshold: 0.12, rootMargin: '0px 0px -40px 0px' },
  )

  document.querySelectorAll('.tf-reveal').forEach((el) => observer.observe(el))
}

onMounted(async () => {
  await taiDanhSachSanPham()
  await nextTick()
  setupReveal()
})

onUnmounted(() => {
  if (observer) observer.disconnect()
})
</script>

<style scoped>
/* CHỈ GIỮ LẠI NHỮNG CSS TÙY CHỈNH MÀ BOOTSTRAP KHÔNG HỖ TRỢ SẴN */
.letter-spacing-1 {
  letter-spacing: 0.1em;
}
.text-truncate-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Gradient & Colors */
.tf-gradient-text {
  background: linear-gradient(135deg, #818cf8 0%, #c084fc 45%, #22d3ee 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.tf-hero-overlay {
  background: linear-gradient(
    120deg,
    rgba(10, 12, 24, 0.88) 0%,
    rgba(15, 23, 42, 0.55) 50%,
    rgba(10, 12, 24, 0.4) 100%
  );
}
.tf-overlay-gradient {
  background: linear-gradient(to top, rgba(10, 12, 24, 0.85) 0%, transparent 55%);
}
.tf-badge-glass {
  background: rgba(99, 102, 241, 0.15);
  border: 1px solid rgba(99, 102, 241, 0.35);
  color: #a5b4fc;
}
.tf-btn-gradient {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}
.tf-btn-gradient:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 36px rgba(99, 102, 241, 0.55);
}
.tf-newsletter-box {
  background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 50%, #0f172a 100%);
}

/* Hover Effects */
.tf-hover-card {
  transition:
    transform 0.35s ease,
    box-shadow 0.35s ease;
}
.tf-hover-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.1) !important;
}
.tf-zoom-img {
  transition: transform 0.6s ease;
}
.tf-hover-card:hover .tf-zoom-img {
  transform: scale(1.06);
}
.tf-play-btn {
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
}
.tf-play-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.35) !important;
}

/* Animations */
.tf-hero-bg img,
.tf-lookbook-img {
  animation: tfKenBurns 20s ease-in-out infinite alternate;
}
@keyframes tfKenBurns {
  from {
    transform: scale(1);
  }
  to {
    transform: scale(1.08);
  }
}

.tf-anim-hero > * {
  opacity: 0;
  transform: translateY(28px);
  animation: tfFadeUp 0.85s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}
.tf-anim-hero > *:nth-child(1) {
  animation-delay: 0.15s;
}
.tf-anim-hero > *:nth-child(2) {
  animation-delay: 0.3s;
}
.tf-anim-hero > *:nth-child(3) {
  animation-delay: 0.45s;
}
.tf-anim-hero > *:nth-child(4) {
  animation-delay: 0.6s;
}

@keyframes tfFadeUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Scroll Reveal */
.tf-reveal {
  opacity: 0;
  transform: translateY(36px);
  transition:
    opacity 0.7s ease,
    transform 0.7s ease;
}
.tf-reveal.is-visible {
  opacity: 1;
  transform: translateY(0);
}

.tf-reveal-item {
  opacity: 0;
  transform: translateY(24px) scale(0.98);
  transition:
    opacity 0.55s ease,
    transform 0.55s ease;
}
.tf-reveal-item.is-visible {
  opacity: 1;
  transform: translateY(0) scale(1);
}

@media (prefers-reduced-motion: reduce) {
  .tf-anim-hero > *,
  .tf-hero-bg img,
  .tf-reveal,
  .tf-reveal-item {
    animation: none !important;
    opacity: 1 !important;
    transform: none !important;
    transition: none !important;
  }
}
</style>
