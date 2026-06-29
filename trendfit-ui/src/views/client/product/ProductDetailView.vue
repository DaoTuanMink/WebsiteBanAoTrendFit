<template>
  <div class="product-detail-view bg-white">
    <LayoutHeader />

    <div class="container py-5" v-if="product">
      <div class="row">
        <div class="col-md-6">
          <img
            :src="mainImage || fallbackImage"
            class="img-fluid border mb-3 main-product-image"
            alt="Ảnh sản phẩm"
          />

          <div class="d-flex gap-2 flex-wrap">
            <img
              v-for="(img, i) in product.anhSanPhams"
              :key="i"
              :src="img.urlAnh"
              width="80"
              height="80"
              style="object-fit: cover"
              class="border p-1 cursor-pointer thumb-image"
              :class="{ active: mainImage === img.urlAnh }"
              @click="mainImage = img.urlAnh"
              alt="Ảnh phụ"
            />
          </div>
        </div>

        <div class="col-md-6">
          <h2 class="fw-bold mb-2">{{ product.sanPham.ten }}</h2>

          <!-- Lượt đánh giá -->
          <div class="product-rating mb-3">
            <span class="stars">
              {{ renderStars(product.sanPham.danhGiaTrungBinh) }}
            </span>

            <span class="rating-score">
              {{ Number(product.sanPham.danhGiaTrungBinh || 0).toFixed(1) }}/5
            </span>

            <span class="rating-count">
              ({{ product.sanPham.tongLuotDanhGia || 0 }} lượt đánh giá)
            </span>
          </div>

          <p class="h3 text-danger fw-bold mb-3">
            {{ formatPrice(selectedPrice) }}
          </p>

          <p class="text-muted mb-4">
            {{ product.sanPham.moTa }}
          </p>

          <div class="mb-3">
            <label class="form-label">
              Màu sắc: <b>{{ selectedColor || 'Chưa chọn' }}</b>
            </label>

            <div class="d-flex gap-2 flex-wrap">
              <button
                v-for="color in uniqueColors"
                :key="color"
                @click="selectColor(color)"
                class="btn"
                :class="selectedColor === color ? 'btn-dark' : 'btn-outline-dark'"
              >
                {{ color }}
              </button>
            </div>
          </div>

          <div class="mb-4">
            <label class="form-label">
              Kích cỡ: <b>{{ selectedSize || 'Chưa chọn' }}</b>
            </label>

            <div class="d-flex gap-2 flex-wrap">
              <button
                v-for="size in availableSizes"
                :key="size"
                @click="selectedSize = size"
                class="btn"
                :class="selectedSize === size ? 'btn-dark' : 'btn-outline-dark'"
              >
                {{ size }}
              </button>
            </div>
          </div>

          <button @click="addToCart" class="btn btn-dark btn-lg w-100 py-3 text-uppercase">
            Thêm vào giỏ hàng
          </button>
        </div>
      </div>

      <div class="review-section mt-5">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h4 class="fw-bold mb-0">Đánh giá sản phẩm</h4>

          <span class="text-muted small">
            {{ product.sanPham.tongLuotDanhGia || 0 }} lượt đánh giá
          </span>
        </div>

        <!-- FORM ĐÁNH GIÁ LUÔN HIỆN ĐỂ TEST -->
        <div class="review-form mb-4">
          <h6 class="fw-bold mb-3">Viết đánh giá của bạn</h6>

          <div class="mb-3">
            <label class="form-label">Số sao</label>

            <select v-model.number="reviewForm.saoXepHang" class="form-select review-select">
              <option :value="5">5 sao - Rất tốt</option>
              <option :value="4">4 sao - Tốt</option>
              <option :value="3">3 sao - Bình thường</option>
              <option :value="2">2 sao - Chưa tốt</option>
              <option :value="1">1 sao - Tệ</option>
            </select>
          </div>

          <div class="mb-3">
            <label class="form-label">Tiêu đề</label>

            <input
              v-model="reviewForm.tieuDe"
              class="form-control"
              placeholder="Ví dụ: Sản phẩm đẹp, chất lượng tốt"
            />
          </div>

          <div class="mb-3">
            <label class="form-label">Nội dung đánh giá</label>

            <textarea
              v-model="reviewForm.noiDung"
              class="form-control"
              rows="4"
              placeholder="Chia sẻ cảm nhận của bạn sau khi mua sản phẩm..."
            ></textarea>
          </div>

          <button class="btn btn-dark px-4" @click="submitReview">Gửi đánh giá</button>
        </div>

        <!-- DANH SÁCH ĐÁNH GIÁ -->
        <div v-if="reviews.length">
          <div v-for="review in reviews" :key="review.id" class="review-item">
            <div class="d-flex justify-content-between align-items-start gap-3">
              <div>
                <strong>{{ review.tenNguoiDung || 'Khách hàng' }}</strong>

                <div class="small text-muted">
                  {{ formatDate(review.ngayTao) }}
                </div>
              </div>

              <span class="stars">
                {{ renderStars(review.saoXepHang) }}
              </span>
            </div>

            <h6 v-if="review.tieuDe" class="fw-bold mt-3 mb-1">
              {{ review.tieuDe }}
            </h6>

            <p class="mb-0 text-muted">
              {{ review.noiDung }}
            </p>
          </div>
        </div>

        <div v-else class="text-muted">Sản phẩm chưa có đánh giá nào.</div>
      </div>

      <!-- Sản phẩm liên quan -->
      <div v-if="relatedProducts.length" class="related-section mt-5">
        <h4 class="fw-bold mb-4">Sản phẩm liên quan</h4>

        <div class="row row-cols-2 row-cols-md-4 g-4">
          <div v-for="item in relatedProducts" :key="item.id" class="col">
            <router-link
              :to="'/product/' + item.id"
              class="related-card text-decoration-none text-dark d-block"
            >
              <div class="related-img-wrap bg-light mb-3">
                <img :src="getProductImage(item)" class="related-img" alt="Sản phẩm liên quan" />
              </div>

              <h6 class="related-name">
                {{ item.ten }}
              </h6>

              <div class="small mb-1">
                <span class="stars">
                  {{ renderStars(item.danhGiaTrungBinh) }}
                </span>

                <span class="text-muted">
                  {{ Number(item.danhGiaTrungBinh || 0).toFixed(1) }}
                  ({{ item.tongLuotDanhGia || 0 }})
                </span>
              </div>
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="container py-5 text-center">
      <div class="spinner-border text-dark" role="status"></div>
    </div>

    <LayoutFooter />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import LayoutHeader from '@/components/LayoutHeader.vue'
import LayoutFooter from '@/components/LayoutFooter.vue'

const route = useRoute()

const product = ref(null)
const mainImage = ref('')
const selectedColor = ref(null)
const selectedSize = ref(null)

const reviews = ref([])
const canReview = ref(false)

const fallbackImage = 'https://placehold.co/500x600?text=TrendFit'

const reviewForm = ref({
  saoXepHang: 5,
  tieuDe: '',
  noiDung: '',
})

const currentUserId = computed(() => {
  const id =
    localStorage.getItem('user_id') ||
    localStorage.getItem('nguoiDungId') ||
    localStorage.getItem('nguoi_dung_id')

  return id ? Number(id) : null
})

const uniqueColors = computed(() => [
  ...new Set(product.value?.bienTheSanPhams?.map((v) => v.mauSac) || []),
])

const availableSizes = computed(() => {
  return (
    product.value?.bienTheSanPhams
      ?.filter((v) => v.mauSac === selectedColor.value)
      ?.map((v) => v.kichCoSize) || []
  )
})

const selectedPrice = computed(() => {
  const variant = product.value?.bienTheSanPhams?.find(
    (v) => v.mauSac === selectedColor.value && v.kichCoSize === selectedSize.value,
  )

  if (variant) {
    return variant.gia
  }

  return getMinPrice(product.value?.bienTheSanPhams)
})

const relatedProducts = computed(() => {
  return product.value?.sanPhamLienQuan || []
})

const loadProduct = async () => {
  const id = route.params.id

  // 1. Chặn ngay nếu id không tồn tại hoặc bằng 'undefined'
  if (!id || id === 'undefined' || id === undefined) {
    console.warn('ID sản phẩm chưa sẵn sàng, đợi router...')
    return
  }

  try {
    const res = await axios.get(`http://localhost:8080/api/public/products/${id}`)
    product.value = res.data
    // ... code xử lý ảnh ...
  } catch (error) {
    console.error(error)
  }
}

const loadReviews = async () => {
  const id = route.params.id
  if (!id) return

  try {
    const res = await axios.get(`http://localhost:8080/api/public/reviews/product/${id}`)
    reviews.value = res.data || []
  } catch (error) {
    console.error('Lỗi tải đánh giá:', error)
    reviews.value = []
  }
}

const checkCanReview = async () => {
  canReview.value = true
}

const submitReview = async () => {
  if (!reviewForm.value.noiDung.trim()) {
    alert('Vui lòng nhập nội dung đánh giá.')
    return
  }

  const userId = currentUserId.value || 1

  try {
    await axios.post('http://localhost:8080/api/public/reviews', {
      nguoiDungId: userId,
      sanPhamId: Number(route.params.id),
      saoXepHang: reviewForm.value.saoXepHang,
      tieuDe: reviewForm.value.tieuDe,
      noiDung: reviewForm.value.noiDung,
    })

    alert('Gửi đánh giá thành công!')

    reviewForm.value = {
      saoXepHang: 5,
      tieuDe: '',
      noiDung: '',
    }

    await initPage()
  } catch (error) {
    console.error(error)
    alert(error.response?.data?.message || 'Không thể gửi đánh giá.')
  }
}

const selectColor = (color) => {
  selectedColor.value = color
  selectedSize.value = null
}

const getMinPrice = (variants) => {
  if (!variants || variants.length === 0) return 0

  const prices = variants.map((v) => Number(v.gia || 0)).filter((v) => v > 0)

  if (prices.length === 0) return 0

  return Math.min(...prices)
}

const getProductImage = (item) => {
  return (
    item.anhChinh ||
    item.urlAnh ||
    item.anhSanPhams?.find((a) => a.laAnhChinh)?.urlAnh ||
    item.anhSanPhams?.[0]?.urlAnh ||
    fallbackImage
  )
}

const formatPrice = (v) => {
  if (!v) return 'Liên hệ'

  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(v)
}

const formatDate = (dateString) => {
  if (!dateString) return ''

  return new Date(dateString).toLocaleString('vi-VN')
}

const renderStars = (value) => {
  const rating = Math.round(Number(value) || 0)

  return '★'.repeat(rating) + '☆'.repeat(5 - rating)
}

const addToCart = () => {
  if (!selectedColor.value || !selectedSize.value) {
    return alert('Vui lòng chọn đầy đủ màu và size!')
  }

  const variant = product.value.bienTheSanPhams.find(
    (v) => v.mauSac === selectedColor.value && v.kichCoSize === selectedSize.value,
  )

  if (!variant) return alert('Sản phẩm không hợp lệ!')

  const cartItem = {
    bienTheId: variant.id,
    ten: product.value.sanPham.ten,
    anhChinh: mainImage.value,
    mauSac: selectedColor.value,
    kichCoSize: selectedSize.value,
    gia: variant.gia,
    maSku: variant.maSku,
    quantity: 1,
  }

  let cart = JSON.parse(localStorage.getItem('cart') || '[]')

  const existingItem = cart.find((i) => i.bienTheId === cartItem.bienTheId)

  if (existingItem) {
    existingItem.quantity += 1
  } else {
    cart.push(cartItem)
  }

  localStorage.setItem('cart', JSON.stringify(cart))

  alert('Đã thêm sản phẩm vào giỏ hàng!')
}

const initPage = async () => {
  // Bọc vào try catch để đảm bảo an toàn, lỗi hàm này không ảnh hưởng hàm khác
  product.value = null // Reset trạng thái loading khi chuyển trang hoàn toàn
  await loadProduct()
  await loadReviews()
  await checkCanReview()
}

onMounted(initPage)

watch(
  () => route.params.id,
  (newId) => {
    // Chỉ gọi khi newId tồn tại và không phải là 'undefined'
    if (newId && newId !== 'undefined') {
      initPage()
    }
  },
  { immediate: true }, // Gọi ngay lần đầu nếu id đã có
)
</script>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}

.main-product-image {
  width: 100%;
  height: 500px;
  object-fit: cover;
}

.thumb-image {
  opacity: 0.75;
  transition: 0.2s ease;
}

.thumb-image:hover,
.thumb-image.active {
  opacity: 1;
  border-color: #000 !important;
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.stars {
  color: #f5b301;
  letter-spacing: 1px;
  white-space: nowrap;
}

.rating-score {
  font-weight: 700;
}

.rating-count {
  color: #777;
}

.review-section,
.related-section {
  border-top: 1px solid #eee;
  padding-top: 32px;
}

.review-form {
  background: #fafafa;
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 20px;
}

.review-select {
  max-width: 260px;
}

.review-item {
  border-bottom: 1px solid #eee;
  padding: 18px 0;
}

.related-card {
  transition: transform 0.3s ease;
}

.related-card:hover {
  transform: translateY(-5px);
}

.related-img-wrap {
  width: 100%;
  height: 300px;
  overflow: hidden;
}

.related-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.related-card:hover .related-img {
  transform: scale(1.05);
}

.related-name {
  font-size: 14px;
  line-height: 1.4;
  height: 2.8em;
  overflow: hidden;
}

.btn.active {
  background-color: #000;
  color: #fff;
}
</style>
