<template>
  <div class="ao-view bg-white text-start">
    <LayoutHeader />

    <div class="container py-5 mt-4">
      <div class="row">
        <!-- CỘT BÊN TRÁI: BỘ LỌC CHUẨN GIAO DIỆN -->
        <div class="col-lg-3 mb-4">
          <div class="filter-sidebar border rounded-3 p-3 bg-white shadow-sm">
            <!-- TIÊU ĐỀ TỔNG: BỘ LỌC -->
            <div
              class="filter-header d-flex justify-content-between align-items-center py-2 border-bottom cursor-pointer"
              @click="toggleSection('all')"
            >
              <span class="fw-bold text-uppercase fs-6">Bộ lọc</span>
              <i :class="sections.all ? 'bi bi-chevron-up' : 'bi bi-chevron-down'"></i>
            </div>

            <!-- Khối này sẽ ẩn/hiện toàn bộ danh sách bên dưới khi bấm vào chữ BỘ LỌC -->
            <div v-show="sections.all">
              <!-- 1. Danh mục -->
              <div class="filter-section border-bottom py-3">
                <div
                  class="d-flex justify-content-between align-items-center cursor-pointer"
                  @click="toggleSection('category')"
                >
                  <span class="fw-semibold text-dark">Danh mục</span>
                  <i :class="sections.category ? 'bi bi-chevron-up' : 'bi bi-chevron-down'"></i>
                </div>
                <div v-show="sections.category" class="mt-3">
                  <div class="filter-pill-grid">
                    <div
                      v-for="cat in categories"
                      :key="cat.id"
                      @click="toggleCategory(cat.id)"
                      class="filter-pill-box text-center py-2 px-3 border rounded cursor-pointer small"
                      :class="{ active: selectedCategories.includes(cat.id) }"
                    >
                      {{ cat.ten }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- 2. Thương hiệu -->
              <div class="filter-section border-bottom py-3">
                <div
                  class="d-flex justify-content-between align-items-center cursor-pointer"
                  @click="toggleSection('brand')"
                >
                  <span class="fw-semibold text-dark">Thương hiệu</span>
                  <i :class="sections.brand ? 'bi bi-chevron-up' : 'bi bi-chevron-down'"></i>
                </div>
                <div v-show="sections.brand" class="mt-3">
                  <div class="filter-pill-grid">
                    <div
                      v-for="brand in brands"
                      :key="brand.id"
                      @click="toggleBrand(brand.id)"
                      class="filter-pill-box text-center py-2 px-3 border rounded cursor-pointer small"
                      :class="{ active: selectedBrands.includes(brand.id) }"
                    >
                      {{ brand.ten }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- 3. Màu sắc -->
              <div class="filter-section border-bottom py-3">
                <div
                  class="d-flex justify-content-between align-items-center cursor-pointer"
                  @click="toggleSection('color')"
                >
                  <span class="fw-semibold text-dark">Màu sắc</span>
                  <i :class="sections.color ? 'bi bi-chevron-up' : 'bi bi-chevron-down'"></i>
                </div>
                <div v-show="sections.color" class="mt-3">
                  <div class="d-flex flex-wrap gap-2">
                    <div
                      v-for="colorObj in availableColors"
                      :key="colorObj.tenMau"
                      @click="toggleColor(colorObj.tenMau)"
                      class="color-pill px-3 py-1 border rounded-pill small cursor-pointer d-flex align-items-center gap-2"
                      :class="{ active: selectedColors.includes(colorObj.tenMau) }"
                    >
                      <span
                        class="rounded-circle border"
                        :style="{
                          backgroundColor: colorObj.maMau || '#ccc',
                          width: '12px',
                          height: '12px',
                          display: 'inline-block',
                        }"
                      ></span>
                      {{ colorObj.tenMau }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- 4. Kích cỡ -->
              <div class="filter-section py-3">
                <div
                  class="d-flex justify-content-between align-items-center cursor-pointer"
                  @click="toggleSection('size')"
                >
                  <span class="fw-semibold text-dark">Kích cỡ</span>
                  <i :class="sections.size ? 'bi bi-chevron-up' : 'bi bi-chevron-down'"></i>
                </div>
                <div v-show="sections.size" class="mt-3">
                  <div class="size-grid">
                    <div
                      v-for="size in availableSizes"
                      :key="size"
                      @click="toggleSize(size)"
                      class="size-box text-center py-2 border rounded cursor-pointer"
                      :class="{ active: selectedSizes.includes(size) }"
                    >
                      {{ size }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- Nút xóa bộ lọc nhanh -->
              <div v-if="hasActiveFilters" class="mt-3 pt-2 border-top text-center">
                <button @click="resetFilters" class="btn btn-sm btn-outline-dark w-100 py-1">
                  Xóa tất cả bộ lọc
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- CỘT BÊN PHẢI: HIỂN THỊ SẢN PHẨM -->
        <div class="col-lg-9">
          <div
            class="d-flex justify-content-between align-items-center mb-4 p-3 bg-light rounded border-0"
          >
            <span class="text-muted small">
              Tìm thấy <b class="text-dark">{{ filteredProducts.length }}</b> sản phẩm
            </span>

            <div class="d-flex align-items-center gap-2">
              <label class="small text-muted text-nowrap">Sắp xếp:</label>
              <select v-model="sortBy" class="form-select form-select-sm">
                <option value="default">Mặc định</option>
                <option value="price-asc">Giá: Thấp đến Cao</option>
                <option value="price-desc">Giá: Cao đến Thấp</option>
                <option value="name">Tên: A - Z</option>
              </select>
            </div>
          </div>

          <div v-if="loading" class="text-center my-5">
            <div class="spinner-border text-dark" role="status"></div>
          </div>

          <div
            v-else-if="filteredProducts.length === 0"
            class="text-center py-5 border rounded bg-light"
          >
            <p class="text-muted mb-3">Không tìm thấy sản phẩm phù hợp với bộ lọc.</p>
            <button @click="resetFilters" class="btn btn-dark btn-sm">Xem tất cả</button>
          </div>

          <div v-else>
            <!-- Lưới Sản Phẩm (Sử dụng mảng đã phân trang: paginatedProducts) -->
            <div class="row row-cols-2 row-cols-md-3 g-4">
              <div class="col" v-for="item in paginatedProducts" :key="item.sanPham?.id">
                <div
                  class="trendfit-product-card h-100 border rounded-3 p-2 bg-white position-relative"
                >
                  <!-- Cập nhật aspect-ratio chuẩn khớp khung cắt ảnh -->
                  <div
                    class="trendfit-img-container overflow-hidden position-relative mb-3 bg-light rounded-2"
                  >
                    <!-- Link bọc cả ảnh để click vào ảnh cũng chuyển trang -->
                    <router-link
                      :to="'/product/' + item.sanPham?.id"
                      class="d-block"
                      @click="scrollToTop"
                    >
                      <img
                        :src="getAnhChinh(item.anhSanPhams)"
                        class="w-100 img-product-dynamic"
                        style="aspect-ratio: 300 / 350; object-fit: cover; object-position: center"
                        alt="product"
                      />
                    </router-link>

                    <router-link
                      :to="'/product/' + item.sanPham?.id"
                      class="trendfit-quick-add position-absolute bottom-0 start-0 end-0 btn btn-dark rounded-0 py-2 text-white text-decoration-none text-uppercase fw-bold text-center"
                      @click="scrollToTop"
                    >
                      Xem chi tiết
                    </router-link>
                  </div>

                  <div class="trendfit-info px-1">
                    <div
                      class="d-flex justify-content-between font-size-10 text-uppercase text-muted mb-1"
                    >
                      <span>{{ item.sanPham?.danhMuc?.ten || 'Chưa phân loại' }}</span>
                      <span class="fw-bold text-dark">{{
                        item.sanPham?.thuongHieu?.ten || 'No Brand'
                      }}</span>
                    </div>

                    <div class="d-flex gap-1 mb-2">
                      <span
                        v-for="(colorInfo, idx) in getUniqueColorsWithHex(item.bienTheSanPhams)"
                        :key="idx"
                        :style="{ backgroundColor: colorInfo.maMau || '#ccc' }"
                        class="rounded-circle border"
                        style="width: 14px; height: 14px"
                        :title="colorInfo.tenMau"
                      ></span>
                    </div>

                    <span class="text-muted text-uppercase font-size-10 d-block mb-1">
                      {{ item.sanPham?.chatLieu || 'Premium Cotton' }}
                    </span>

                    <router-link
                      :to="'/product/' + item.sanPham?.id"
                      class="trendfit-title d-block mb-1 text-decoration-none text-dark fw-semibold"
                      @click="scrollToTop"
                    >
                      {{ item.sanPham?.ten }}
                    </router-link>

                    <p class="trendfit-price fw-bold text-danger m-0">
                      {{ formatPrice(getMinPrice(item.bienTheSanPhams)) }}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Điều Hướng Phân Trang -->
            <div
              v-if="totalPages > 1"
              class="d-flex justify-content-center align-items-center gap-2 mt-5"
            >
              <button
                class="btn btn-outline-dark btn-sm px-3 fw-semibold"
                :disabled="currentPage === 1"
                @click="prevPage"
              >
                &laquo; Trước
              </button>

              <div class="d-flex gap-1">
                <button
                  v-for="page in totalPages"
                  :key="page"
                  class="btn btn-sm fw-semibold"
                  style="width: 34px"
                  :class="currentPage === page ? 'btn-dark' : 'btn-outline-dark'"
                  @click="goToPage(page)"
                >
                  {{ page }}
                </button>
              </div>

              <button
                class="btn btn-outline-dark btn-sm px-3 fw-semibold"
                :disabled="currentPage === totalPages"
                @click="nextPage"
              >
                Sau &raquo;
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <LayoutFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import LayoutHeader from '@/components/LayoutHeader.vue'
import LayoutFooter from '@/components/LayoutFooter.vue'

const sanPhams = ref([])
const categories = ref([])
const brands = ref([])
const loading = ref(true)

// Phân trang
const currentPage = ref(1)
const itemsPerPage = 20

const sections = ref({
  all: true,
  category: true,
  brand: true,
  color: true,
  size: true,
})

const toggleSection = (key) => {
  if (key === 'all') {
    sections.value.all = !sections.value.all
  } else {
    sections.value[key] = !sections.value[key]
  }
}

const toggleCategory = (catId) => {
  const idx = selectedCategories.value.indexOf(catId)
  if (idx > -1) selectedCategories.value.splice(idx, 1)
  else selectedCategories.value.push(catId)
}

const toggleBrand = (brandId) => {
  const idx = selectedBrands.value.indexOf(brandId)
  if (idx > -1) selectedBrands.value.splice(idx, 1)
  else selectedBrands.value.push(brandId)
}

const selectedCategories = ref([])
const selectedBrands = ref([])
const selectedColors = ref([])
const selectedSizes = ref([])
const sortBy = ref('default')

// Lắng nghe sự thay đổi của bộ lọc/sắp xếp để tự động quay về trang 1
watch(
  [selectedCategories, selectedBrands, selectedColors, selectedSizes, sortBy],
  () => {
    currentPage.value = 1
  },
  { deep: true },
)

const availableColors = computed(() => {
  const colorMap = new Map()
  sanPhams.value.forEach((item) => {
    ;(item.bienTheSanPhams || []).forEach((v) => {
      const mau = v.mauSac
      if (mau && mau.tenMau) {
        const tenTrim = mau.tenMau.trim()
        if (!colorMap.has(tenTrim)) {
          colorMap.set(tenTrim, {
            tenMau: tenTrim,
            maMau: mau.maMau || '#cccccc',
          })
        }
      }
    })
  })
  return Array.from(colorMap.values())
})

const availableSizes = computed(() => {
  const sizeSet = new Set()
  sanPhams.value.forEach((item) => {
    ;(item.bienTheSanPhams || []).forEach((v) => {
      const s = v.kichCo?.tenKichCo?.trim()
      if (s) sizeSet.add(s)
    })
  })
  return [...sizeSet]
})

const toggleColor = (color) => {
  const idx = selectedColors.value.indexOf(color)
  if (idx > -1) selectedColors.value.splice(idx, 1)
  else selectedColors.value.push(color)
}

const toggleSize = (size) => {
  const idx = selectedSizes.value.indexOf(size)
  if (idx > -1) selectedSizes.value.splice(idx, 1)
  else selectedSizes.value.push(size)
}

const hasActiveFilters = computed(() => {
  return (
    selectedCategories.value.length > 0 ||
    selectedBrands.value.length > 0 ||
    selectedColors.value.length > 0 ||
    selectedSizes.value.length > 0
  )
})

const resetFilters = () => {
  selectedCategories.value = []
  selectedBrands.value = []
  selectedColors.value = []
  selectedSizes.value = []
  sortBy.value = 'default'
  currentPage.value = 1
}

const filteredProducts = computed(() => {
  let result = [...sanPhams.value]

  if (selectedCategories.value.length > 0) {
    result = result.filter((item) => {
      const catId = item.sanPham?.danhMuc?.id
      return selectedCategories.value.includes(catId)
    })
  }

  if (selectedBrands.value.length > 0) {
    result = result.filter((item) => {
      const brandId = item.sanPham?.thuongHieu?.id
      return selectedBrands.value.includes(brandId)
    })
  }

  if (selectedColors.value.length > 0) {
    result = result.filter((item) => {
      const variants = item.bienTheSanPhams || []
      return variants.some((v) => selectedColors.value.includes(v.mauSac?.tenMau?.trim()))
    })
  }

  if (selectedSizes.value.length > 0) {
    result = result.filter((item) => {
      const variants = item.bienTheSanPhams || []
      return variants.some((v) => selectedSizes.value.includes(v.kichCo?.tenKichCo?.trim()))
    })
  }

  if (sortBy.value === 'price-asc') {
    result.sort((a, b) => getMinPrice(a.bienTheSanPhams) - getMinPrice(b.bienTheSanPhams))
  } else if (sortBy.value === 'price-desc') {
    result.sort((a, b) => getMinPrice(b.bienTheSanPhams) - getMinPrice(a.bienTheSanPhams))
  } else if (sortBy.value === 'name') {
    result.sort((a, b) => {
      const nameA = (a.sanPham?.ten || '').toLowerCase()
      const nameB = (b.sanPham?.ten || '').toLowerCase()
      return nameA.localeCompare(nameB)
    })
  }

  return result
})

// === LOGIC PHÂN TRANG ===
const totalPages = computed(() => {
  return Math.ceil(filteredProducts.value.length / itemsPerPage)
})

const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return filteredProducts.value.slice(start, end)
})

const goToPage = (page) => {
  currentPage.value = page
  scrollToTop()
}

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    scrollToTop()
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    scrollToTop()
  }
}

// Cuộn mượt lên trên cùng
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const getUniqueColorsWithHex = (variants) => {
  if (!variants || !Array.isArray(variants)) return []
  const map = new Map()
  variants.forEach((v) => {
    if (v.mauSac && v.mauSac.tenMau) {
      const name = v.mauSac.tenMau.trim()
      if (!map.has(name)) {
        map.set(name, {
          tenMau: name,
          maMau: v.mauSac.maMau || '#ccc',
        })
      }
    }
  })
  return Array.from(map.values())
}

const getMinPrice = (variants) => {
  if (!variants || variants.length === 0) return 0
  const prices = variants.map((v) => Number(v.giaSale || v.gia || 0))
  return Math.min(...prices)
}

const formatPrice = (v) => {
  if (!v || v === 0) return 'Liên hệ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)
}

const getAnhChinh = (anhList) => {
  if (anhList && Array.isArray(anhList) && anhList.length > 0) {
    const anhChinh = anhList.find((a) => a.laAnhChinh === true || a.laAnhChinh === 1)
    return (
      anhChinh?.urlAnh ||
      anhList[0]?.urlAnh ||
      'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=500'
    )
  }
  return 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=500'
}

onMounted(async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/public/products')
    sanPhams.value = res.data

    const catMap = new Map()
    const brandMap = new Map()

    res.data.forEach((item) => {
      const sp = item.sanPham
      if (sp) {
        if (sp.danhMuc && sp.danhMuc.id) {
          catMap.set(sp.danhMuc.id, { id: sp.danhMuc.id, ten: sp.danhMuc.ten })
        }
        if (sp.thuongHieu && sp.thuongHieu.id) {
          brandMap.set(sp.thuongHieu.id, { id: sp.thuongHieu.id, ten: sp.thuongHieu.ten })
        }
      }
    })

    categories.value = [...catMap.values()]
    brands.value = [...brandMap.values()]
  } catch (err) {
    console.error('Lỗi tải dữ liệu:', err)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.filter-pill-grid {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: 220px;
  overflow-y: auto;
  padding-right: 4px;
}

.filter-pill-box {
  background: #f8f9fa;
  border-color: #dee2e6 !important;
  text-align: left !important;
  transition: all 0.2s ease;
}

.filter-pill-box:hover {
  border-color: #000 !important;
  background-color: #f1f3f5;
}

.filter-pill-box.active {
  background-color: #000 !important;
  color: #fff !important;
  border-color: #000 !important;
}

.filter-sidebar {
  position: sticky;
  top: 90px;
  background: #fff;
}
.cursor-pointer {
  cursor: pointer;
}
.rotate-180 {
  transform: rotate(180deg);
  transition: transform 0.2s ease;
}

.size-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.size-box {
  background: #f8f9fa;
  border-color: #dee2e6 !important;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
}
.size-box:hover {
  border-color: #000 !important;
}
.size-box.active {
  background-color: #000 !important;
  color: #fff !important;
  border-color: #000 !important;
}

.color-pill {
  background: #f8f9fa;
  border-color: #dee2e6 !important;
  font-size: 13px;
  transition: all 0.2s ease;
}
.color-pill.active {
  background-color: #000 !important;
  color: #fff !important;
  border-color: #000 !important;
}

.trendfit-product-card {
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
}
.trendfit-product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08);
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
