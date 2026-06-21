<template>
  <div class="container py-5 text-start" v-if="product">
    <div class="row g-5">
      <div class="col-12 col-md-6">
        <img
          src="https://via.placeholder.com/500x600?text=TrendFit+Premium+Shirt"
          class="img-fluid border border-dark rounded-0 w-100 shadow-sm"
          alt="TrendFit"
        />
      </div>

      <div class="col-12 col-md-6">
        <span class="badge bg-danger rounded-0 mb-2">NEW ARRIVAL 2026</span>
        <h2 class="fw-bold text-uppercase text-dark mb-2">{{ product.ten }}</h2>
        <h4 class="text-danger fw-bold mb-4">350.000 VNĐ</h4>
        <hr class="border-dark" />

        <div class="p-3 bg-light border border-dark my-4">
          <h6 class="fw-bold text-dark text-uppercase mb-2">
            <i class="ri-information-line"></i> Thông số trang phục
          </h6>
          <table class="table table-sm table-borderless m-0 small">
            <tbody>
              <tr>
                <td class="fw-bold p-1" style="width: 150px">CHẤT LIỆU CHÍNH:</td>
                <td class="p-1 text-secondary">
                  {{ product.chatLieu || '100% Cotton Premium Co giãn 4 chiều' }}
                </td>
              </tr>
              <tr>
                <td class="fw-bold p-1">XUẤT XỨ SẢN XUẤT:</td>
                <td class="p-1 text-secondary">
                  {{ product.xuatXu || 'Made in Việt Nam (TrendFit Factory)' }}
                </td>
              </tr>
              <tr>
                <td class="fw-bold p-1">NĂM PHÁT HÀNH:</td>
                <td class="p-1 text-secondary">{{ product.namRaMat || '2026' }}</td>
              </tr>
              <tr>
                <td class="fw-bold p-1">MÃ SẢN PHẨM (SLUG):</td>
                <td class="p-1 text-secondary">
                  <code>{{ product.slug }}</code>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="mb-4">
          <label class="form-label fw-bold small text-dark"
            ><i class="ri-t-shirt-line"></i> BƯỚC 1: LỰA CHỌN KÍCH CỠ (SIZE):</label
          >
          <div class="d-flex gap-2">
            <button
              v-for="s in ['S', 'M', 'L', 'XL', 'XXL']"
              :key="s"
              type="button"
              @click="selectedSize = s"
              :class="selectedSize === s ? 'btn-dark' : 'btn-outline-dark'"
              class="btn rounded-0 px-3 fw-bold"
            >
              {{ s }}
            </button>
          </div>
        </div>

        <div class="mb-5">
          <label class="form-label fw-bold small text-dark"
            ><i class="ri-palette-line"></i> BƯỚC 2: LỰA CHỌN MÀU SẮC ÁO:</label
          >
          <div class="d-flex gap-2">
            <button
              v-for="m in ['Đen truyền thống', 'Trắng tinh khôi', 'Xám xi măng']"
              :key="m"
              type="button"
              @click="selectedColor = m"
              :class="selectedColor === m ? 'btn-dark' : 'btn-outline-dark'"
              class="btn rounded-0 px-3 fw-bold"
            >
              {{ m }}
            </button>
          </div>
        </div>

        <button
          @click="xuLyThemVaoGio"
          class="btn btn-dark btn-lg w-100 rounded-0 fw-bold py-3 text-uppercase fs-5 tracking-wide shadow"
        >
          <i class="ri-shopping-cart-2-line"></i> Thêm vào giỏ hàng ngay
        </button>
      </div>
    </div>
  </div>

  <div v-else class="text-center py-5">
    <div class="spinner-border text-dark"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const selectedSize = ref('L')
const selectedColor = ref('Đen truyền thống')

onMounted(async () => {
  try {
    const res = await axios.get(`http://localhost:8080/api/public/products/${route.params.id}`)
    product.value = res.data
  } catch (err) {
    alert('Sản phẩm áo TrendFit này không tồn tại trên hệ thống hoặc đã bị ẩn kho!')
    router.push('/')
  }
})

const xuLyThemVaoGio = () => {
  const khoGioHangCucBo = JSON.parse(localStorage.getItem('trendfit_cart') || '[]')
  const itemGioHang = {
    id: product.value.id,
    ten: product.value.ten,
    size: selectedSize.value,
    mau: selectedColor.value,
    soLuong: 1,
    giaGhiNhan: 350000,
  }

  // Nếu khách mua trùng mã áo, trùng cả size và màu sắc thì tăng số lượng lên 1
  const hangDaCoTrongGio = khoGioHangCucBo.find(
    (x) => x.id === itemGioHang.id && x.size === itemGioHang.size && x.mau === itemGioHang.mau,
  )
  if (hangDaCoTrongGio) {
    hangDaCoTrongGio.soLuong += 1
  } else {
    khoGioHangCucBo.push(itemGioHang)
  }

  localStorage.setItem('trendfit_cart', JSON.stringify(khoGioHangCucBo))
  alert('Đã bỏ sản phẩm áo vào giỏ hàng TrendFit thành công!')
  router.push('/cart')
}
</script>
