<template>
  <div class="tf-home">
    <LayoutHeader />

    <section class="tf-hero">
      <div class="tf-hero-bg">
        <img
          src="https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=1920&q=80"
          alt="TrendFit New Collection 2026"
        />
        <div class="tf-hero-overlay"></div>
      </div>
      <div class="tf-hero-content tf-anim-hero">
        <span class="tf-hero-eyebrow"><i class="ri-sparkling-2-fill"></i> New Collection 2026</span>
        <h1 class="tf-hero-title">Dress Your<br /><span class="tf-gradient-text">Style</span></h1>
        <p class="tf-hero-sub">
          Shop bán áo TrendFit — áo thun, sơ mi, hoodie. Form chuẩn, chất vải tốt.
        </p>
        <div class="tf-hero-cta">
          <router-link to="/ao" class="tf-btn-primary"
            >Mua ngay <i class="ri-arrow-right-line"></i
          ></router-link>
          <a href="#best-sellers" class="tf-btn-ghost">Khám phá</a>
        </div>
      </div>
    </section>

    <section class="tf-section tf-reveal">
      <div class="container-fluid px-4 px-lg-5">
        <div class="tf-section-head">
          <div>
            <span class="tf-section-eyebrow">🔥 Danh mục nổi bật</span>
            <h2 class="tf-section-title">Danh mục áo nổi bật</h2>
          </div>
        </div>
        <div class="tf-cat-grid">
          <router-link
            v-for="cat in categories"
            :key="cat.name"
            :to="cat.link"
            class="tf-cat-card tf-reveal-item"
          >
            <div class="tf-cat-img"><img :src="cat.img" :alt="cat.name" /></div>
            <div class="tf-cat-info">
              <span class="tf-cat-name">{{ cat.name }}</span>
              <span class="tf-cat-count">{{ cat.count }}</span>
            </div>
            <div class="tf-cat-arrow"><i class="ri-arrow-right-up-line"></i></div>
          </router-link>
        </div>
      </div>
    </section>

    <section id="best-sellers" class="tf-section tf-section-soft tf-reveal">
      <div class="container-fluid px-4 px-lg-5">
        <div class="tf-section-head">
          <div>
            <span class="tf-section-eyebrow">⭐ Sản phẩm bán chạy</span>
            <h2 class="tf-section-title">Được yêu thích nhất</h2>
          </div>
          <div class="tf-tabs">
            <button
              class="tf-tab"
              :class="{ active: activeTab === 'NEW' }"
              @click="activeTab = 'NEW'"
            >
              Sản phẩm mới
            </button>
            <button
              class="tf-tab"
              :class="{ active: activeTab === 'BEST' }"
              @click="activeTab = 'BEST'"
            >
              Bán chạy
            </button>
          </div>
        </div>
        <div v-if="loading" class="tf-loading">
          <div class="tf-spinner"></div>
          <p>Đang tải bộ sưu tập TrendFit...</p>
        </div>
        <div v-else class="tf-product-grid">
          <div
            v-for="item in sanPhams"
            :key="item.sanPham.id"
            class="tf-product-card tf-reveal-item"
          >
            <div class="tf-product-media">
              <img
                :src="getAnhChinh(item.anhSanPhams)"
                :alt="item.sanPham.ten"
                class="tf-product-img"
              />
              <span class="tf-product-badge">NEW</span>
              <div class="tf-product-actions">
                <button class="tf-action-btn" title="Yêu thích">
                  <i class="ri-heart-3-line"></i>
                </button>
                <router-link
                  :to="'/product/' + item.sanPham.id"
                  class="tf-action-btn"
                  title="Xem nhanh"
                  ><i class="ri-eye-line"></i
                ></router-link>
              </div>
              <router-link :to="'/product/' + item.sanPham.id" class="tf-product-cta"
                >Xem chi tiết</router-link
              >
            </div>
            <div class="tf-product-body">
              <span class="tf-product-meta"
                >{{ item.sanPham.chatLieu || 'Premium Cotton' }} ·
                {{ item.sanPham.xuatXu || 'Việt Nam' }}</span
              >
              <router-link :to="'/product/' + item.sanPham.id" class="tf-product-name">{{
                item.sanPham.ten
              }}</router-link>
              <p class="tf-product-price">{{ formatPrice(getMinPrice(item.bienTheSanPhams)) }}</p>
            </div>
          </div>
        </div>
        <div v-if="!loading && sanPhams.length === 0" class="tf-empty">
          Chưa có sản phẩm phù hợp. Hãy quay lại sau!
        </div>
        <div class="text-center mt-5">
          <router-link to="/ao" class="tf-btn-outline"
            >Xem tất cả sản phẩm <i class="ri-arrow-right-line"></i
          ></router-link>
        </div>
      </div>
    </section>

    <section class="tf-section tf-reveal">
      <div class="container-fluid px-4 px-lg-5">
        <div class="tf-section-head">
          <div>
            <span class="tf-section-eyebrow">🎁 Khuyến mãi</span>
            <h2 class="tf-section-title">Ưu đãi đang diễn ra</h2>
          </div>
        </div>
        <div class="tf-promo-grid">
          <div class="tf-promo-card tf-promo-main tf-reveal-item">
            <img
              src="https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=1000&q=80"
              alt="Flash Sale"
            />
            <div class="tf-promo-content">
              <span class="tf-promo-tag">FLASH SALE</span>
              <h3>Giảm đến 40%</h3>
              <p>Áo thun &amp; Hoodie chọn lọc</p>
              <router-link to="/ao" class="tf-btn-primary tf-btn-sm">Mua ngay</router-link>
            </div>
          </div>
          <div class="tf-promo-card tf-reveal-item">
            <img
              src="https://images.unsplash.com/photo-1483985988355-763728e1935b?w=600&q=80"
              alt="Freeship"
            />
            <div class="tf-promo-content">
              <span class="tf-promo-tag">FREESHIP</span>
              <h3>Miễn phí ship</h3>
              <p>Đơn từ 499.000đ</p>
            </div>
          </div>
          <div class="tf-promo-card tf-reveal-item">
            <img
              src="https://images.unsplash.com/photo-1558769132-cb1aea458c5e?w=600&q=80"
              alt="Member"
            />
            <div class="tf-promo-content">
              <span class="tf-promo-tag">MEMBER</span>
              <h3>Tích điểm x2</h3>
              <p>Thành viên TrendFit</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="tf-lookbook tf-reveal">
      <img
        src="https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=1920&q=80"
        alt="TrendFit Studio"
      />
      <div class="tf-lookbook-overlay"></div>
      <div class="tf-lookbook-content">
        <span class="tf-section-eyebrow light">Studio Collection</span>
        <h2 class="tf-lookbook-title">TRENDFIT STUDIO</h2>
        <p>Lookbook Autumn — Spring 2026</p>
        <button class="tf-play-btn" type="button"><i class="ri-play-fill"></i></button>
      </div>
    </section>

    <section class="tf-section tf-reveal">
      <div class="container-fluid px-4 px-lg-5">
        <div class="tf-section-head">
          <div>
            <span class="tf-section-eyebrow">📢 Blog thời trang</span>
            <h2 class="tf-section-title">Gợi ý phối áo</h2>
          </div>
          <a href="#" class="tf-link-more">Xem tất cả <i class="ri-arrow-right-line"></i></a>
        </div>
        <div class="tf-blog-grid">
          <article v-for="post in blogs" :key="post.title" class="tf-blog-card tf-reveal-item">
            <div class="tf-blog-img"><img :src="post.img" :alt="post.title" /></div>
            <div class="tf-blog-body">
              <span class="tf-blog-tag">{{ post.tag }}</span>
              <h3 class="tf-blog-title">{{ post.title }}</h3>
              <p class="tf-blog-excerpt">{{ post.excerpt }}</p>
              <span class="tf-blog-date">{{ post.date }}</span>
            </div>
          </article>
        </div>
      </div>
    </section>

    <section class="tf-section tf-section-soft tf-reveal">
      <div class="container-fluid px-4 px-lg-5">
        <div class="tf-section-head center">
          <div>
            <span class="tf-section-eyebrow">💬 Đánh giá khách hàng</span>
            <h2 class="tf-section-title">Khách nói gì về áo TrendFit</h2>
          </div>
        </div>
        <div class="tf-review-grid">
          <div v-for="r in reviews" :key="r.name" class="tf-review-card tf-reveal-item">
            <div class="tf-review-stars"><i v-for="n in 5" :key="n" class="ri-star-fill"></i></div>
            <p class="tf-review-text">“{{ r.text }}”</p>
            <div class="tf-review-author">
              <div class="tf-review-avatar">{{ r.name.charAt(0) }}</div>
              <div>
                <strong>{{ r.name }}</strong
                ><span>{{ r.role }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="tf-newsletter tf-reveal">
      <div class="container-fluid px-4 px-lg-5">
        <div class="tf-newsletter-box">
          <div class="tf-newsletter-text">
            <h3>Đăng ký nhận khuyến mãi</h3>
            <p>Sản phẩm mới &amp; ưu đãi độc quyền gửi thẳng vào email của bạn.</p>
          </div>
          <form class="tf-newsletter-form" @submit.prevent>
            <input type="email" placeholder="Nhập email của bạn..." required />
            <button type="submit" class="tf-btn-primary">Đăng ký</button>
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
.tf-home {
  background: #f8fafc;
  color: #0f172a;
  font-family: 'Inter', system-ui, sans-serif;
  -webkit-font-smoothing: antialiased;
}
.tf-hero {
  position: relative;
  height: min(88vh, 720px);
  min-height: 520px;
  display: flex;
  align-items: center;
  overflow: hidden;
}
.tf-hero-bg {
  position: absolute;
  inset: 0;
}
.tf-hero-bg img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center 30%;
}
.tf-hero-overlay {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(
      120deg,
      rgba(10, 12, 24, 0.88) 0%,
      rgba(15, 23, 42, 0.55) 50%,
      rgba(10, 12, 24, 0.4) 100%
    ),
    radial-gradient(ellipse at 70% 40%, rgba(99, 102, 241, 0.25), transparent 50%);
}
.tf-hero-content {
  position: relative;
  z-index: 2;
  padding: 0 1.5rem;
  max-width: 720px;
  margin-left: clamp(1rem, 6vw, 5rem);
}
.tf-hero-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #a5b4fc;
  background: rgba(99, 102, 241, 0.15);
  border: 1px solid rgba(99, 102, 241, 0.35);
  padding: 6px 14px;
  border-radius: 99px;
  margin-bottom: 20px;
}
.tf-hero-title {
  font-family: 'Space Grotesk', sans-serif;
  font-size: clamp(2.75rem, 7vw, 4.5rem);
  font-weight: 700;
  line-height: 1.05;
  letter-spacing: -0.03em;
  color: #fff;
  margin: 0 0 16px;
}
.tf-gradient-text {
  background: linear-gradient(135deg, #818cf8 0%, #c084fc 45%, #22d3ee 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.tf-hero-sub {
  font-size: 1.1rem;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.7);
  max-width: 420px;
  margin-bottom: 32px;
}
.tf-hero-cta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.tf-btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 28px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-weight: 700;
  font-size: 14px;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  border: none;
  border-radius: 12px;
  text-decoration: none;
  box-shadow: 0 8px 28px rgba(99, 102, 241, 0.45);
  transition:
    transform 0.2s,
    box-shadow 0.2s;
  cursor: pointer;
}
.tf-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 36px rgba(99, 102, 241, 0.55);
  color: #fff;
}
.tf-btn-sm {
  padding: 10px 20px;
  font-size: 13px;
}
.tf-btn-ghost {
  display: inline-flex;
  align-items: center;
  padding: 14px 28px;
  background: transparent;
  color: #fff;
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 0.04em;
  border: 1.5px solid rgba(255, 255, 255, 0.35);
  border-radius: 12px;
  text-decoration: none;
  transition: all 0.2s;
}
.tf-btn-ghost:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: #fff;
  color: #fff;
}
.tf-btn-outline {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: transparent;
  color: #4f46e5;
  font-weight: 700;
  font-size: 14px;
  border: 1.5px solid #c7d2fe;
  border-radius: 12px;
  text-decoration: none;
  transition: all 0.2s;
}
.tf-btn-outline:hover {
  background: #eef2ff;
  border-color: #6366f1;
  color: #4338ca;
}
.tf-section {
  padding: 72px 0;
}
.tf-section-soft {
  background: linear-gradient(180deg, #f1f5f9 0%, #eef2ff 100%);
}
.tf-section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 36px;
}
.tf-section-head.center {
  justify-content: center;
  text-align: center;
}
.tf-section-eyebrow {
  display: block;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: #6366f1;
  margin-bottom: 6px;
}
.tf-section-eyebrow.light {
  color: #a5b4fc;
}
.tf-section-title {
  font-family: 'Space Grotesk', sans-serif;
  font-size: clamp(1.5rem, 3vw, 2rem);
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #0f172a;
  margin: 0;
}
.tf-link-more {
  font-size: 14px;
  font-weight: 600;
  color: #6366f1;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.tf-cat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}
.tf-cat-card {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  text-decoration: none;
  background: #0f172a;
  aspect-ratio: 3/4;
  transition: transform 0.3s;
}
.tf-cat-card:hover {
  transform: translateY(-4px);
}
.tf-cat-img {
  position: absolute;
  inset: 0;
}
.tf-cat-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}
.tf-cat-card:hover .tf-cat-img img {
  transform: scale(1.06);
}
.tf-cat-img::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(10, 12, 24, 0.85) 0%, transparent 55%);
}
.tf-cat-info {
  position: absolute;
  left: 20px;
  bottom: 20px;
  z-index: 2;
  color: #fff;
}
.tf-cat-name {
  display: block;
  font-family: 'Space Grotesk', sans-serif;
  font-size: 1.25rem;
  font-weight: 700;
}
.tf-cat-count {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}
.tf-cat-arrow {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(8px);
  border-radius: 10px;
  color: #fff;
  font-size: 18px;
  opacity: 0;
  transform: translateY(6px);
  transition: all 0.3s;
  z-index: 2;
}
.tf-cat-card:hover .tf-cat-arrow {
  opacity: 1;
  transform: translateY(0);
}
.tf-tabs {
  display: flex;
  gap: 4px;
  background: #fff;
  padding: 4px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}
.tf-tab {
  padding: 8px 18px;
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  background: transparent;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.tf-tab.active {
  background: #0f172a;
  color: #fff;
}
.tf-product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}
.tf-product-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  transition:
    box-shadow 0.3s,
    transform 0.3s;
}
.tf-product-card:hover {
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.1);
  transform: translateY(-4px);
}
.tf-product-media {
  position: relative;
  aspect-ratio: 3/4;
  overflow: hidden;
  background: #f1f5f9;
}
.tf-product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}
.tf-product-card:hover .tf-product-img {
  transform: scale(1.05);
}
.tf-product-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: #0f172a;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.08em;
  padding: 4px 8px;
  border-radius: 6px;
  z-index: 2;
}
.tf-product-actions {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  opacity: 0;
  transform: translateX(8px);
  transition: all 0.3s;
  z-index: 2;
}
.tf-product-card:hover .tf-product-actions {
  opacity: 1;
  transform: translateX(0);
}
.tf-action-btn {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  background: #fff;
  border: none;
  border-radius: 10px;
  color: #0f172a;
  font-size: 16px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  text-decoration: none;
  transition:
    background 0.2s,
    color 0.2s;
}
.tf-action-btn:hover {
  background: #6366f1;
  color: #fff;
}
.tf-product-cta {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px;
  background: #0f172a;
  color: #fff;
  text-align: center;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  text-decoration: none;
  transform: translateY(100%);
  transition: transform 0.3s;
  z-index: 2;
}
.tf-product-card:hover .tf-product-cta {
  transform: translateY(0);
}
.tf-product-body {
  padding: 16px;
}
.tf-product-meta {
  display: block;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: #94a3b8;
  margin-bottom: 6px;
}
.tf-product-name {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
  text-decoration: none;
  line-height: 1.4;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.8em;
}
.tf-product-name:hover {
  color: #4f46e5;
}
.tf-product-price {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 1.05rem;
  font-weight: 700;
  color: #ef4444;
  margin: 0;
}
.tf-promo-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr;
  gap: 20px;
}
.tf-promo-card {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  aspect-ratio: 4/3;
  min-height: 200px;
}
.tf-promo-main {
  aspect-ratio: auto;
  min-height: 280px;
}
.tf-promo-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.tf-promo-content {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 24px;
  background: linear-gradient(to top, rgba(10, 12, 24, 0.9) 0%, transparent 60%);
  color: #fff;
}
.tf-promo-tag {
  display: inline-block;
  align-self: flex-start;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  background: rgba(99, 102, 241, 0.9);
  padding: 4px 10px;
  border-radius: 6px;
  margin-bottom: 10px;
}
.tf-promo-content h3 {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 1.35rem;
  font-weight: 700;
  margin: 0 0 4px;
  color: #fff;
}
.tf-promo-content p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0 0 12px;
}
.tf-lookbook {
  position: relative;
  height: 420px;
  overflow: hidden;
}
.tf-lookbook img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: brightness(0.55);
}
.tf-lookbook-overlay {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at center, rgba(99, 102, 241, 0.2), transparent 70%);
}
.tf-lookbook-content {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #fff;
  z-index: 2;
}
.tf-lookbook-title {
  font-family: 'Space Grotesk', sans-serif;
  font-size: clamp(2rem, 5vw, 3.25rem);
  font-weight: 700;
  letter-spacing: 0.08em;
  margin: 8px 0 6px;
}
.tf-lookbook-content p {
  font-size: 14px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.65);
  margin-bottom: 24px;
}
.tf-play-btn {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: none;
  background: #fff;
  color: #0f172a;
  font-size: 24px;
  display: grid;
  place-items: center;
  cursor: pointer;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  transition: transform 0.2s;
}
.tf-play-btn:hover {
  transform: scale(1.08);
}
.tf-blog-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}
.tf-blog-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  transition:
    box-shadow 0.3s,
    transform 0.3s;
}
.tf-blog-card:hover {
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
  transform: translateY(-3px);
}
.tf-blog-img {
  aspect-ratio: 16/10;
  overflow: hidden;
}
.tf-blog-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s;
}
.tf-blog-card:hover .tf-blog-img img {
  transform: scale(1.04);
}
.tf-blog-body {
  padding: 20px;
}
.tf-blog-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #6366f1;
  margin-bottom: 8px;
}
.tf-blog-title {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 1.05rem;
  font-weight: 700;
  line-height: 1.35;
  color: #0f172a;
  margin: 0 0 8px;
}
.tf-blog-excerpt {
  font-size: 14px;
  line-height: 1.55;
  color: #64748b;
  margin: 0 0 12px;
}
.tf-blog-date {
  font-size: 12px;
  font-weight: 600;
  color: #94a3b8;
}
.tf-review-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}
.tf-review-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  border: 1px solid #e2e8f0;
}
.tf-review-stars {
  color: #f59e0b;
  font-size: 16px;
  margin-bottom: 14px;
  display: flex;
  gap: 2px;
}
.tf-review-text {
  font-size: 15px;
  line-height: 1.65;
  color: #334155;
  margin: 0 0 20px;
}
.tf-review-author {
  display: flex;
  align-items: center;
  gap: 12px;
}
.tf-review-avatar {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-weight: 700;
  font-size: 16px;
  display: grid;
  place-items: center;
}
.tf-review-author strong {
  display: block;
  font-size: 14px;
  color: #0f172a;
}
.tf-review-author span {
  font-size: 12px;
  color: #94a3b8;
}
.tf-newsletter {
  padding: 0 0 72px;
}
.tf-newsletter-box {
  background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 50%, #0f172a 100%);
  border-radius: 20px;
  padding: 40px 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  flex-wrap: wrap;
  border: 1px solid rgba(99, 102, 241, 0.3);
  box-shadow: 0 20px 60px rgba(99, 102, 241, 0.15);
}
.tf-newsletter-text h3 {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #fff;
  margin: 0 0 6px;
}
.tf-newsletter-text p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}
.tf-newsletter-form {
  display: flex;
  gap: 10px;
  flex: 1;
  max-width: 420px;
}
.tf-newsletter-form input {
  flex: 1;
  padding: 14px 18px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  outline: none;
}
.tf-newsletter-form input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}
.tf-newsletter-form input:focus {
  border-color: #818cf8;
  background: rgba(255, 255, 255, 0.12);
}
.tf-loading {
  text-align: center;
  padding: 64px 0;
  color: #64748b;
}
.tf-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e2e8f0;
  border-top-color: #6366f1;
  border-radius: 50%;
  margin: 0 auto 16px;
  animation: spin 0.7s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
.tf-empty {
  text-align: center;
  padding: 48px;
  color: #64748b;
  font-weight: 600;
}
@media (max-width: 1199.98px) {
  .tf-product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 991.98px) {
  .tf-cat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .tf-product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .tf-promo-grid {
    grid-template-columns: 1fr;
  }
  .tf-promo-main {
    min-height: 240px;
  }
  .tf-blog-grid,
  .tf-review-grid {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 575.98px) {
  .tf-hero {
    min-height: 460px;
  }
  .tf-cat-grid {
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }
  .tf-newsletter-box {
    padding: 28px 20px;
  }
  .tf-newsletter-form {
    flex-direction: column;
    max-width: 100%;
  }
}

/* ========== ANIMATIONS ========== */
.tf-anim-hero .tf-hero-eyebrow,
.tf-anim-hero .tf-hero-title,
.tf-anim-hero .tf-hero-sub,
.tf-anim-hero .tf-hero-cta {
  opacity: 0;
  transform: translateY(28px);
  animation: tfFadeUp 0.85s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}
.tf-anim-hero .tf-hero-eyebrow {
  animation-delay: 0.15s;
}
.tf-anim-hero .tf-hero-title {
  animation-delay: 0.3s;
}
.tf-anim-hero .tf-hero-sub {
  animation-delay: 0.45s;
}
.tf-anim-hero .tf-hero-cta {
  animation-delay: 0.6s;
}

.tf-hero-bg img {
  animation: tfKenBurns 18s ease-in-out infinite alternate;
}

@keyframes tfFadeUp {
  from {
    opacity: 0;
    transform: translateY(28px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes tfKenBurns {
  from {
    transform: scale(1);
  }
  to {
    transform: scale(1.08);
  }
}

.tf-reveal {
  opacity: 0;
  transform: translateY(36px);
  transition:
    opacity 0.7s cubic-bezier(0.22, 1, 0.36, 1),
    transform 0.7s cubic-bezier(0.22, 1, 0.36, 1);
}
.tf-reveal.is-visible {
  opacity: 1;
  transform: translateY(0);
}

.tf-reveal-item {
  opacity: 0;
  transform: translateY(24px) scale(0.98);
  transition:
    opacity 0.55s cubic-bezier(0.22, 1, 0.36, 1),
    transform 0.55s cubic-bezier(0.22, 1, 0.36, 1);
}
.tf-reveal-item.is-visible {
  opacity: 1;
  transform: translateY(0) scale(1);
}

.tf-btn-primary,
.tf-btn-ghost,
.tf-btn-outline {
  transition:
    transform 0.25s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.25s ease,
    background 0.25s ease,
    border-color 0.25s ease,
    color 0.25s ease;
}
.tf-btn-primary:active {
  transform: translateY(0) scale(0.97);
}

.tf-cat-card,
.tf-product-card,
.tf-blog-card,
.tf-review-card,
.tf-promo-card {
  transition:
    transform 0.35s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.35s ease;
}

.tf-product-img,
.tf-cat-img img,
.tf-blog-img img {
  transition: transform 0.6s cubic-bezier(0.22, 1, 0.36, 1);
}

.tf-product-actions,
.tf-product-cta,
.tf-cat-arrow {
  transition:
    opacity 0.35s cubic-bezier(0.22, 1, 0.36, 1),
    transform 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}

.tf-tab {
  transition:
    background 0.25s ease,
    color 0.25s ease,
    transform 0.2s ease;
}
.tf-tab:active {
  transform: scale(0.96);
}

.tf-play-btn {
  transition:
    transform 0.3s cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 0.3s ease;
}
.tf-play-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.35);
}

.tf-newsletter-box {
  transition:
    transform 0.4s ease,
    box-shadow 0.4s ease;
}
.tf-newsletter-box:hover {
  transform: translateY(-2px);
  box-shadow: 0 24px 64px rgba(99, 102, 241, 0.22);
}

@media (prefers-reduced-motion: reduce) {
  .tf-anim-hero .tf-hero-eyebrow,
  .tf-anim-hero .tf-hero-title,
  .tf-anim-hero .tf-hero-sub,
  .tf-anim-hero .tf-hero-cta,
  .tf-hero-bg img {
    animation: none !important;
    opacity: 1 !important;
    transform: none !important;
  }
  .tf-reveal,
  .tf-reveal-item {
    opacity: 1 !important;
    transform: none !important;
    transition: none !important;
  }
}
</style>
