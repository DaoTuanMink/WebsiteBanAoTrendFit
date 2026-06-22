<template>
  <div class="product-detail-view bg-white">
    <LayoutHeader />

    <div class="container py-5" v-if="product">
      <div class="row">
        <div class="col-md-6">
          <img
            :src="mainImage"
            class="img-fluid border mb-3"
            style="width: 100%; height: 500px; object-fit: cover"
          />
          <div class="d-flex gap-2">
            <img
              v-for="(img, i) in product.anhSanPhams"
              :key="i"
              :src="img.urlAnh"
              width="80"
              height="80"
              style="object-fit: cover"
              class="border p-1 cursor-pointer"
              @click="mainImage = img.urlAnh"
            />
          </div>
        </div>

        <div class="col-md-6">
          <h2 class="fw-bold mb-3">{{ product.sanPham.ten }}</h2>
          <p class="h3 text-danger fw-bold mb-3">{{ formatPrice(selectedPrice) }}</p>
          <p class="text-muted mb-4">{{ product.sanPham.moTa }}</p>

          <div class="mb-3">
            <label class="form-label"
              >Màu sắc: <b>{{ selectedColor }}</b></label
            >
            <div class="d-flex gap-2">
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
            <label class="form-label"
              >Kích cỡ: <b>{{ selectedSize }}</b></label
            >
            <div class="d-flex gap-2">
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

onMounted(async () => {
  const id = route.params.id
  const res = await axios.get(`http://localhost:8080/api/public/products/${id}`)
  product.value = res.data
  mainImage.value =
    product.value.anhSanPhams.find((a) => a.laAnhChinh)?.urlAnh ||
    product.value.anhSanPhams[0]?.urlAnh
})

const uniqueColors = computed(() => [
  ...new Set(product.value?.bienTheSanPhams.map((v) => v.mauSac) || []),
])

const availableSizes = computed(
  () =>
    product.value?.bienTheSanPhams
      .filter((v) => v.mauSac === selectedColor.value)
      .map((v) => v.kichCoSize) || [],
)

const selectedPrice = computed(() => {
  const variant = product.value?.bienTheSanPhams.find(
    (v) => v.mauSac === selectedColor.value && v.kichCoSize === selectedSize.value,
  )
  return variant ? variant.gia : 0
})

// Khi chọn màu mới, reset size để tránh lỗi không chọn được size của màu đó
const selectColor = (color) => {
  selectedColor.value = color
  selectedSize.value = null
}

const formatPrice = (v) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)

const addToCart = () => {
  // 1. Kiểm tra điều kiện chọn
  if (!selectedColor.value || !selectedSize.value) {
    return alert('Vui lòng chọn đầy đủ màu và size!')
  }

  // 2. Tìm biến thể đã chọn
  const variant = product.value.bienTheSanPhams.find(
    (v) => v.mauSac === selectedColor.value && v.kichCoSize === selectedSize.value,
  )

  if (!variant) return alert('Sản phẩm không hợp lệ!')

  // 3. Tạo object sản phẩm giỏ hàng
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

  // 4. Lấy giỏ hàng hiện tại từ localStorage
  let cart = JSON.parse(localStorage.getItem('cart') || '[]')

  // 5. Kiểm tra nếu sản phẩm đã tồn tại trong giỏ thì tăng số lượng
  const existingItem = cart.find((i) => i.bienTheId === cartItem.bienTheId)

  if (existingItem) {
    existingItem.quantity += 1
  } else {
    cart.push(cartItem)
  }

  // 6. Lưu lại vào localStorage
  localStorage.setItem('cart', JSON.stringify(cart))

  alert('Đã thêm sản phẩm vào giỏ hàng!')
}
</script>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}
.btn.active {
  background-color: #000;
  color: #fff;
}
</style>
