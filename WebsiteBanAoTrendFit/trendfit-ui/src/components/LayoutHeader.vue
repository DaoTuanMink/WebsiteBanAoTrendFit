<template>
  <header class="tf-header">
    <!-- Top bar subtle -->
    <div class="tf-topbar d-none d-md-block">
      <div class="container-fluid px-4 px-lg-5 d-flex justify-content-between align-items-center">
        <span class="tf-topbar-text">
          <i class="ri-truck-line me-1"></i> Miễn phí vận chuyển đơn từ 499K
        </span>
        <span class="tf-topbar-text">
          New Collection 2026 — Dress Your Style
        </span>
      </div>
    </div>

    <nav class="tf-nav">
      <div class="container-fluid px-4 px-lg-5">
        <div class="tf-nav-inner">
          <!-- Logo -->
          <router-link to="/" class="tf-logo">
            <span class="tf-logo-mark">TF</span>
            <span class="tf-logo-text">TRENDFIT</span>
          </router-link>

          <!-- Main menu -->
          <ul class="tf-menu d-none d-lg-flex">
            <li>
              <router-link to="/" class="tf-menu-link" active-class="is-active">
                Trang chủ
              </router-link>
            </li>
            <li>
              <router-link to="/ao" class="tf-menu-link" active-class="is-active">
                Áo
              </router-link>
            </li>
            <li>
              <router-link to="/ao" class="tf-menu-link">
                Quần
              </router-link>
            </li>
            <li>
              <router-link to="/ao" class="tf-menu-link">
                Phụ kiện
              </router-link>
            </li>
            <li v-if="userRole === 'ADMIN' || userRole === 'EMPLOYEE'">
              <span class="tf-menu-link tf-admin-link" @click="router.push('/admin/products')">
                <i class="ri-dashboard-3-line me-1"></i> Quản trị
              </span>
            </li>
          </ul>

          <!-- Actions -->
          <div class="tf-actions">
            <!-- Search -->
            <button class="tf-icon-btn" type="button" title="Tìm kiếm" @click="toggleSearch">
              <i class="ri-search-line"></i>
            </button>

            <!-- Wishlist -->
            <button class="tf-icon-btn" type="button" title="Yêu thích">
              <i class="ri-heart-3-line"></i>
            </button>

            <!-- User -->
            <router-link
              v-if="!username"
              to="/login"
              class="tf-icon-btn"
              title="Đăng nhập"
            >
              <i class="ri-user-3-line"></i>
            </router-link>

            <div v-else class="tf-user-wrap">
              <router-link to="/my-orders" class="tf-icon-btn" title="Đơn hàng">
                <i class="ri-box-3-line"></i>
              </router-link>
              <router-link to="/history-order" class="tf-icon-btn" title="Lịch sử">
                <i class="ri-history-line"></i>
              </router-link>
              <span class="tf-user-badge">{{ username }}</span>
              <button class="tf-icon-btn tf-logout" title="Đăng xuất" @click="dangXuat">
                <i class="ri-logout-box-r-line"></i>
              </button>
            </div>

            <!-- Cart -->
            <router-link to="/cart" class="tf-icon-btn tf-cart-btn" title="Giỏ hàng">
              <i class="ri-shopping-bag-3-line"></i>
              <span v-if="cartCount > 0" class="tf-cart-badge">{{ cartCount }}</span>
            </router-link>

            <!-- Mobile menu toggle -->
            <button class="tf-icon-btn d-lg-none" type="button" @click="mobileOpen = !mobileOpen">
              <i :class="mobileOpen ? 'ri-close-line' : 'ri-menu-3-line'"></i>
            </button>
          </div>
        </div>

        <!-- Search panel -->
        <div class="tf-search-panel" :class="{ open: searchOpen }">
          <div class="tf-search-inner">
            <i class="ri-search-line tf-search-icon"></i>
            <input
              ref="searchInput"
              v-model="searchQuery"
              type="text"
              class="tf-search-input"
              placeholder="Tìm áo, quần, phụ kiện..."
              @keyup.enter="doSearch"
            />
            <button class="tf-search-close" @click="searchOpen = false">
              <i class="ri-close-line"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- Mobile menu -->
      <div class="tf-mobile-menu" :class="{ open: mobileOpen }">
        <router-link to="/" class="tf-mobile-link" @click="mobileOpen = false">Trang chủ</router-link>
        <router-link to="/ao" class="tf-mobile-link" @click="mobileOpen = false">Áo</router-link>
        <router-link to="/ao" class="tf-mobile-link" @click="mobileOpen = false">Quần</router-link>
        <router-link to="/ao" class="tf-mobile-link" @click="mobileOpen = false">Phụ kiện</router-link>
      </div>
    </nav>
  </header>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref('')
const userRole = ref('')
const cartCount = ref(0)
const searchOpen = ref(false)
const searchQuery = ref('')
const searchInput = ref(null)
const mobileOpen = ref(false)

const kiemTraTrangThai = () => {
  username.value = localStorage.getItem('username') || ''
  userRole.value = localStorage.getItem('user_role') || ''
  const cart = JSON.parse(localStorage.getItem('cart') || '[]')
  cartCount.value = cart.reduce((total, item) => total + (item.quantity || 1), 0)
}

const dangXuat = () => {
  if (confirm('Bạn muốn đăng xuất?')) {
    localStorage.clear()
    window.location.reload()
  }
}

const toggleSearch = async () => {
  searchOpen.value = !searchOpen.value
  if (searchOpen.value) {
    await nextTick()
    searchInput.value?.focus()
  }
}

const doSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ path: '/ao', query: { q: searchQuery.value.trim() } })
    searchOpen.value = false
  }
}

onMounted(() => {
  kiemTraTrangThai()
  window.addEventListener('storage', kiemTraTrangThai)
})
</script>

<style scoped>
.tf-header {
  position: sticky;
  top: 0;
  z-index: 1050;
  background: rgba(10, 12, 20, 0.92);
  backdrop-filter: blur(16px) saturate(1.4);
  -webkit-backdrop-filter: blur(16px) saturate(1.4);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.tf-topbar {
  background: linear-gradient(90deg, #0f172a 0%, #1e1b4b 50%, #0f172a 100%);
  border-bottom: 1px solid rgba(99, 102, 241, 0.25);
  font-size: 12px;
  letter-spacing: 0.04em;
  padding: 6px 0;
  color: rgba(255, 255, 255, 0.7);
}

.tf-topbar-text {
  font-weight: 500;
}

.tf-nav {
  position: relative;
}

.tf-nav-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  gap: 24px;
}

/* Logo */
.tf-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  flex-shrink: 0;
}

.tf-logo-mark {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #6366f1, #8b5cf6 50%, #06b6d4);
  color: #fff;
  font-family: 'Space Grotesk', sans-serif;
  font-weight: 700;
  font-size: 14px;
  border-radius: 10px;
  letter-spacing: -0.02em;
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.4);
}

.tf-logo-text {
  font-family: 'Space Grotesk', sans-serif;
  font-weight: 700;
  font-size: 1.15rem;
  letter-spacing: 0.12em;
  color: #fff;
}

/* Menu */
.tf-menu {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.tf-menu-link {
  display: block;
  padding: 8px 14px;
  font-size: 13.5px;
  font-weight: 600;
  letter-spacing: 0.03em;
  color: rgba(255, 255, 255, 0.65);
  text-decoration: none;
  border-radius: 8px;
  transition: color 0.2s, background 0.2s;
  cursor: pointer;
}

.tf-menu-link:hover,
.tf-menu-link.is-active {
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

.tf-admin-link {
  color: #fbbf24 !important;
}

/* Actions */
.tf-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.tf-icon-btn {
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.04);
  color: rgba(255, 255, 255, 0.85);
  border-radius: 10px;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
  position: relative;
}

.tf-icon-btn:hover {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
}

.tf-cart-btn {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.25), rgba(139, 92, 246, 0.25));
  border-color: rgba(99, 102, 241, 0.4);
}

.tf-cart-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #ef4444;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  border-radius: 99px;
  display: grid;
  place-items: center;
  box-shadow: 0 2px 6px rgba(239, 68, 68, 0.5);
}

.tf-user-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tf-user-badge {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 6px;
  color: rgba(255, 255, 255, 0.9);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tf-logout:hover {
  color: #f87171 !important;
  border-color: rgba(248, 113, 113, 0.4);
}

/* Search panel */
.tf-search-panel {
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.3s ease, padding 0.3s ease;
  padding: 0 0;
}

.tf-search-panel.open {
  max-height: 72px;
  padding: 0 0 12px;
}

.tf-search-inner {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 10px 16px;
}

.tf-search-icon {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.5);
}

.tf-search-input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: #fff;
  font-size: 15px;
  font-weight: 500;
}

.tf-search-input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.tf-search-close {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.5);
  font-size: 20px;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.tf-search-close:hover {
  color: #fff;
}

/* Mobile menu */
.tf-mobile-menu {
  display: none;
  flex-direction: column;
  padding: 0 16px 16px;
  gap: 4px;
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.3s ease;
}

.tf-mobile-menu.open {
  display: flex;
  max-height: 280px;
}

.tf-mobile-link {
  padding: 12px 14px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 600;
  font-size: 14px;
  text-decoration: none;
  border-radius: 8px;
  transition: background 0.2s;
}

.tf-mobile-link:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

@media (max-width: 991.98px) {
  .tf-mobile-menu.open {
    display: flex;
  }
}

.tf-header {
  animation: tfHeaderIn 0.5s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes tfHeaderIn {
  from {
    opacity: 0;
    transform: translateY(-12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.tf-logo-mark {
  transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.3s ease;
}
.tf-logo:hover .tf-logo-mark {
  transform: scale(1.06) rotate(-3deg);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.55);
}

.tf-menu-link {
  position: relative;
  transition: color 0.25s ease, background 0.25s ease;
}
.tf-menu-link::after {
  content: '';
  position: absolute;
  left: 14px;
  right: 14px;
  bottom: 4px;
  height: 2px;
  background: linear-gradient(90deg, #6366f1, #22d3ee);
  border-radius: 2px;
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1);
}
.tf-menu-link:hover::after,
.tf-menu-link.is-active::after {
  transform: scaleX(1);
}

.tf-icon-btn {
  transition: all 0.25s cubic-bezier(0.22, 1, 0.36, 1);
}
.tf-icon-btn:active {
  transform: scale(0.92);
}

.tf-cart-badge {
  animation: tfBadgePop 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}
@keyframes tfBadgePop {
  from { transform: scale(0); }
  to { transform: scale(1); }
}

.tf-search-panel {
  transition: max-height 0.35s cubic-bezier(0.22, 1, 0.36, 1),
              padding 0.35s ease,
              opacity 0.3s ease;
  opacity: 0;
}
.tf-search-panel.open {
  opacity: 1;
}

.tf-mobile-menu {
  transition: max-height 0.35s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.3s ease;
  opacity: 0;
}
.tf-mobile-menu.open {
  opacity: 1;
}

@media (prefers-reduced-motion: reduce) {
  .tf-header { animation: none; }
  .tf-logo-mark,
  .tf-menu-link,
  .tf-menu-link::after,
  .tf-icon-btn,
  .tf-search-panel,
  .tf-mobile-menu {
    transition: none !important;
  }
}

</style>
