<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

// Nhúng 2 thành phần dùng chung vào trang chi tiết khách hàng
import LayoutHeader from '@/components/LayoutHeader.vue'
import LayoutFooter from '@/components/LayoutFooter.vue'

const route = useRoute()
const productId = route.params.id

// Trạng thái dữ liệu động từ Backend public
const sanPham = ref(null)
const bienTheList = ref([])
const loading = ref(true)

// Trạng thái người dùng chọn mua
const mauDuocChon = ref('')
const sizeDuocChon = ref('')
const soLuongMua = ref(1)

const layAnhMacDinh = (slugDanhMuc) => {
  if (slugDanhMuc === 'ao-thun') {
    return 'https://images.unsplash.com/photo-1618354691373-d851c5c3a990?w=600'
  }
  return 'https://images.unsplash.com/photo-1624222247344-550fb8ef5521?w=600'
}

const taiChiTietSanPham = async () => {
  try {
    loading.value = true
    // Gọi cổng api public không cần quyền đăng nhập của khách hàng
    const spResponse = await axios.get(`http://localhost:8080/api/public/products/${productId}`)
    sanPham.value = spResponse.data

    const vtResponse = await axios.get(
      `http://localhost:8080/api/public/products/${productId}/variants`,
    )
    bienTheList.value = vtResponse.data

    if (bienTheList.value.length > 0) {
      mauDuocChon.value = bienTheList.value[0].mauSac
      sizeDuocChon.value = bienTheList.value[0].kichCoSize
    }
  } catch (error) {
    console.error('Lỗi khi tải chi tiết sản phẩm:', error)
  } finally {
    loading.value = false
  }
}

const danhSachMauUnique = computed(() => {
  return Array.from(new Set(bienTheList.value.map((item) => item.mauSac)))
})

const danhSachSizeUnique = computed(() => {
  return Array.from(new Set(bienTheList.value.map((item) => item.kichCoSize)))
})

const bienTheHienTai = computed(() => {
  return bienTheList.value.find(
    (item) => item.mauSac === mauDuocChon.value && item.kichCoSize === sizeDuocChon.value,
  )
})

const themVaoGioHang = () => {
  if (!bienTheHienTai.value) {
    alert('Vui lòng chọn đầy đủ Màu sắc và Kích cỡ!')
    return
  }
  alert(`Đã thêm ${soLuongMua.value} chiếc ${sanPham.value.ten} vào giỏ hàng thành công!`)
}
</script>

<template>
  <div class="product-detail-layout bg-white min-vh-100">
    <LayoutHeader />

    <div class="container py-5">
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-dark" role="status"></div>
        <p class="mt-2 text-muted small text-uppercase tracking-widest">
          Đang kết nối kho dữ liệu...
        </p>
      </div>

      <div v-else-if="sanPham" class="row g-5 text-start mt-2">
        <div class="col-12 col-md-6">
          <div class="border overflow-hidden bg-light">
            <img
              :src="layAnhMacDinh(sanPham.danhMuc?.slug)"
              class="w-100 main-detail-image"
              alt="Product image"
            />
          </div>
        </div>

        <div class="col-12 col-md-6 d-flex flex-column justify-content-between">
          <div>
            <span class="text-muted text-uppercase font-size-11 tracking-widest d-block mb-2">
              {{ sanPham.danhMuc?.ten }} / {{ sanPham.gioiTinh }}
            </span>
            <h1 class="fw-black text-uppercase fs-3 mb-3 tracking-wide text-dark">
              {{ sanPham.ten }}
            </h1>

            <div class="mb-4 pb-3 border-bottom">
              <span v-if="bienTheHienTai" class="fs-4 fw-bold text-danger">
                {{ bienTheHienTai.gia?.toLocaleString() }} đ
              </span>
              <span v-else class="text-muted font-size-13">Sản phẩm hiện đang cập nhật giá</span>
            </div>

            <div class="mb-4">
              <label class="fw-bold small text-uppercase tracking-wider text-muted d-block mb-2"
                >Màu sắc: {{ mauDuocChon }}</label
              >
              <div class="d-flex gap-2">
                <button
                  v-for="mau in danhSachMauUnique"
                  :key="mau"
                  @click="mauDuocChon = mau"
                  class="btn btn-sm rounded-0 text-uppercase btn-option"
                  :class="mauDuocChon === mau ? 'btn-dark' : 'btn-outline-dark'"
                >
                  {{ mau }}
                </button>
              </div>
            </div>

            <div class="mb-4">
              <label class="fw-bold small text-uppercase tracking-wider text-muted d-block mb-2"
                >Kích cỡ (Size): {{ sizeDuocChon }}</label
              >
              <div class="d-flex gap-2">
                <button
                  v-for="size in danhSachSizeUnique"
                  :key="size"
                  @click="sizeDuocChon = size"
                  class="btn btn-sm rounded-0 btn-option"
                  :class="sizeDuocChon === size ? 'btn-dark' : 'btn-outline-dark'"
                >
                  {{ size }}
                </button>
              </div>
            </div>

            <div class="mb-4">
              <label class="fw-bold small text-uppercase tracking-wider text-muted d-block mb-2"
                >Số lượng</label
              >
              <div class="d-flex align-items-center gap-1 qty-box">
                <button
                  @click="soLuongMua > 1 ? soLuongMua-- : null"
                  class="btn btn-outline-dark btn-sm rounded-0 px-3"
                >
                  -
                </button>
                <input
                  type="number"
                  v-model.number="soLuongMua"
                  class="form-control form-control-sm rounded-0 text-center border-dark bg-white"
                  readonly
                />
                <button @click="soLuongMua++" class="btn btn-outline-dark btn-sm rounded-0 px-3">
                  +
                </button>
              </div>
            </div>
          </div>

          <div class="d-grid gap-2 mt-4">
            <button
              @click="themVaoGioHang"
              class="btn btn-dark rounded-0 py-3 text-uppercase fw-bold tracking-widest text-sm"
            >
              Thêm vào giỏ hàng
            </button>
          </div>

          <div class="mt-5 pt-4 border-top">
            <h5 class="fw-bold small text-uppercase tracking-wider text-dark mb-2">
              Đặc tính chất liệu
            </h5>
            <ul class="list-unstyled text-muted small d-grid gap-2 ps-1">
              <li>
                • Chất liệu: <strong>{{ sanPham.chatLieu }}</strong>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <div v-else class="text-center py-5">
        <h4 class="text-danger fw-bold">SẢN PHẨM KHÔNG TỒN TẠI HỆ THỐNG</h4>
        <router-link to="/" class="btn btn-dark rounded-0 mt-3 px-4 btn-sm"
          >QUAY LẠI TRANG CHỦ</router-link
        >
      </div>
    </div>

    <LayoutFooter />
  </div>
</template>

<style scoped>
.font-size-11 {
  font-size: 11px;
}
.font-size-13 {
  font-size: 13px;
}
.fw-black {
  font-weight: 900;
}
.main-detail-image {
  height: 550px;
  object-fit: cover;
}
.btn-option {
  min-width: 55px;
  font-size: 12px;
  font-weight: 600;
}
.qty-box {
  max-width: 130px;
}
input[type='number']::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
</style>
