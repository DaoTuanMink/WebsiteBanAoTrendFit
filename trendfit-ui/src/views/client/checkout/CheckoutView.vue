<template>
  <div class="container py-5 text-start">
    <h3 class="fw-bold mb-5 text-uppercase text-dark">
      <i class="ri-bank-card-line"></i> THỦ TỤC THANH TOÁN ĐƠN HÀNG
    </h3>

    <form @submit.prevent="xuLyGiaoDichDatHang" class="row g-5">
      <div class="col-12 col-md-7">
        <h5 class="fw-bold mb-4 text-dark border-bottom pb-2 text-uppercase">
          Địa chỉ & Người nhận hàng
        </h5>
        <div class="mb-3">
          <label class="form-label small fw-bold text-dark"
            >Họ và tên người nhận <span class="text-danger">*</span></label
          >
          <input
            type="text"
            v-model="formOrderData.tenNguoiNhan"
            class="form-control rounded-0 border-dark"
            placeholder="Nhập tên người mua..."
            required
          />
        </div>
        <div class="mb-3">
          <label class="form-label small fw-bold text-dark"
            >Số điện thoại khách hàng <span class="text-danger">*</span></label
          >
          <input
            type="text"
            v-model="formOrderData.soDienThoai"
            class="form-control rounded-0 border-dark"
            placeholder="Nhập số điện thoại để shipper gọi..."
            required
          />
        </div>
        <div class="mb-4">
          <label class="form-label small fw-bold text-dark"
            >Địa chỉ nhận áo chính xác <span class="text-danger">*</span></label
          >
          <input
            type="text"
            v-model="formOrderData.diaChiGiaoHang"
            class="form-control rounded-0 border-dark"
            placeholder="Số nhà, tên đường, quận/huyện, thành phố..."
            required
          />
        </div>
        <div class="mb-3">
          <label class="form-label small fw-bold text-dark">Hình thức thanh toán thương mại</label>
          <select
            v-model="formOrderData.phuongThucThanhToan"
            class="form-select rounded-0 border-dark fw-bold"
          >
            <option value="COD">Nhận áo thanh toán tiền mặt (COD)</option>
            <option value="CHUYEN_KHOAN">Chuyển khoản ngân hàng 24/7 (Internet Banking)</option>
          </select>
        </div>
      </div>

      <div class="col-12 col-md-5">
        <div class="card rounded-0 border-dark bg-white shadow-sm p-4">
          <h5 class="fw-bold text-dark text-center text-uppercase mb-3">
            TÓM TẮT ĐƠN HÀNG TRỰC TUYẾN
          </h5>
          <hr class="border-dark" />
          <div class="d-flex justify-content-between my-2 small text-secondary">
            <span>Tổng số lượng sản phẩm:</span>
            <span>{{ tinhTongSoAo }} chiếc</span>
          </div>
          <div class="d-flex justify-content-between my-2 small text-secondary">
            <span>Phí vận chuyển giao hàng:</span>
            <span class="text-success fw-bold">MIỄN PHÍ (FREE)</span>
          </div>
          <hr class="my-2" />
          <div
            class="d-flex justify-content-between align-items-center my-3 text-danger fw-bold h4"
          >
            <span>TỔNG TIỀN ĐƠN:</span>
            <span>{{ tinhTongTien.toLocaleString('vi-VN') }} đ</span>
          </div>

          <button
            type="submit"
            class="btn btn-danger w-100 rounded-0 fw-bold py-3 mt-4 text-uppercase tracking-wide fs-5"
          >
            XÁC NHẬN ĐẶT HÀNG NGAY
          </button>
        </div>
      </div>
    </form>
  </div>
</template>

<script setup>
import { reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const mangSanPhamTrongGio = JSON.parse(localStorage.getItem('trendfit_cart') || '[]')

const tinhTongSoAo = computed(() =>
  mangSanPhamTrongGio.reduce((sum, item) => sum + item.soLuong, 0),
)
const tinhTongTien = computed(() =>
  mangSanPhamTrongGio.reduce((sum, item) => sum + item.giaGhiNhan * item.soLuong, 0),
)

const formOrderData = reactive({
  tenNguoiNhan: '',
  soDienThoai: '',
  diaChiGiaoHang: '',
  phuongThucThanhToan: 'COD',
  tongTien: tinhTongTien.value,
  // Cấu trúc mảng mapping khớp với Entity ChiTietDonHang ở Backend
  chiTietDonHangs: mangSanPhamTrongGio.map((item) => ({
    soLuong: item.soLuong,
    giaBan: item.giaGhiNhan,
  })),
})

const xuLyGiaoDichDatHang = async () => {
  if (mangSanPhamTrongGio.length === 0) {
    alert('Giỏ hàng của bạn trống, không có gì để tạo hóa đơn!')
    return
  }
  try {
    // Gọi API chốt luồng lưu đơn hàng trạng thái CHO_XAC_NHAN sang Backend
    await axios.post('http://localhost:8080/api/public/orders/place', formOrderData)
    alert('Đặt hàng TrendFit thành công! Đơn hàng của bạn đang ở trạng thái [CHỜ XÁC NHẬN].')
    localStorage.removeItem('trendfit_cart') // Giải phóng giỏ hàng sạch sẽ sau khi đặt thành công
    router.push('/')
  } catch (err) {
    console.error(err)
    alert('Không thể kết nối đến CSDL MySQL. Vui lòng bật Server Backend!')
  }
}
</script>
