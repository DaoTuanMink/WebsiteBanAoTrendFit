<template>
  <div class="container py-5 text-start">
    <h3 class="fw-bold mb-4 text-dark">
      <i class="ri-shopping-bag-line"></i> GIỎ HÀNG TRENDFIT CỦA BẠN
    </h3>
    <hr class="border-dark mb-4" />

    <div class="row g-4" v-if="cartItems.length > 0">
      <div class="col-12 col-lg-8">
        <div
          class="card rounded-0 border-dark p-3 mb-3 shadow-sm bg-white"
          v-for="(item, index) in cartItems"
          :key="index"
        >
          <div class="row align-items-center g-3">
            <div class="col-12 col-md-6">
              <h5 class="fw-bold m-0 text-uppercase text-dark">{{ item.ten }}</h5>
              <div class="mt-1">
                <span class="badge bg-secondary rounded-0 me-2">Size: {{ item.size }}</span>
                <span class="badge bg-info text-dark rounded-0">Màu: {{ item.mau }}</span>
              </div>
            </div>
            <div class="col-6 col-md-3 d-flex align-items-center gap-2">
              <button
                @click="giamSoLuongMua(index)"
                class="btn btn-sm btn-outline-dark rounded-0 fw-bold px-2"
              >
                -
              </button>
              <span class="fw-bold text-dark px-3 border border-dark bg-light py-1">{{
                item.soLuong
              }}</span>
              <button
                @click="tangSoLuongMua(index)"
                class="btn btn-sm btn-outline-dark rounded-0 fw-bold px-2"
              >
                +
              </button>
            </div>
            <div class="col-6 col-md-3 text-end">
              <span class="fw-bold text-danger fs-5"
                >{{ (item.giaGhiNhan * item.soLuong).toLocaleString('vi-VN') }} đ</span
              >
            </div>
          </div>
        </div>
      </div>

      <div class="col-12 col-lg-4">
        <div class="card rounded-0 border-dark bg-light shadow-sm p-4 text-center">
          <h5 class="fw-bold text-dark mb-3 text-uppercase">TÓM TẮT ĐƠN HÀNG</h5>
          <div class="d-flex justify-content-between mb-2 small text-secondary">
            <span>Tổng số lượng áo:</span>
            <span>{{ tongSoLuongAo }} chiếc</span>
          </div>
          <hr class="my-2" />
          <div class="d-flex justify-content-between align-items-center mb-4">
            <span class="fw-bold text-dark">TỔNG TIỀN TẠM TÍNH:</span>
            <span class="text-danger fw-bold h3 m-0"
              >{{ tongTienHoaDon.toLocaleString('vi-VN') }} đ</span
            >
          </div>
          <router-link
            to="/checkout"
            class="btn btn-dark w-100 rounded-0 fw-bold py-3 text-uppercase tracking-wider"
          >
            TIẾN HÀNH ĐẶT HÀNG <i class="ri-arrow-right-line"></i>
          </router-link>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-5 border border-dashed border-dark bg-light my-5">
      <i class="ri-shopping-cart-line display-1 text-muted d-block mb-3"></i>
      <h5 class="text-muted fw-bold">Giỏ hàng của bạn đang trống rỗng!</h5>
      <p class="small text-secondary mb-4">
        Hãy ra trang chủ lựa chọn những chiếc áo TrendFit cực chất nhé.
      </p>
      <router-link to="/" class="btn btn-dark rounded-0 fw-bold px-4 py-2 text-uppercase"
        >QUAY LẠI MUA SẮM</router-link
      >
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const cartItems = ref(JSON.parse(localStorage.getItem('trendfit_cart') || '[]'))

const tongSoLuongAo = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.soLuong, 0)
})

const tongTienHoaDon = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.giaGhiNhan * item.soLuong, 0)
})

const tangSoLuongMua = (index) => {
  cartItems.value[index].soLuong++
  capNhatBoNhoCucBo()
}

const giamSoLuongMua = (index) => {
  if (cartItems.value[index].soLuong > 1) {
    cartItems.value[index].soLuong--
  } else {
    // Nếu số lượng về 0 thì xóa hẳn bản ghi áo đó khỏi giỏ
    cartItems.value.splice(index, 1)
  }
  capNhatBoNhoCucBo()
}

const capNhatBoNhoCucBo = () => {
  localStorage.setItem('trendfit_cart', JSON.stringify(cartItems.value))
}
</script>
