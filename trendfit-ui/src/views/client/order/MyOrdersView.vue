<template>
  <LayoutHeader />
  <div class="container py-5">
    <h2 class="fw-bold mb-4">ĐƠN HÀNG CỦA TÔI</h2>

    <div class="nav nav-tabs mb-4 overflow-x-auto flex-nowrap">
      <button
        v-for="tab in [
          'TẤT CẢ',
          'CHO_XAC_NHAN',
          'DA_XAC_NHAN',
          'DANG_VAN_CHUYEN',
          'DA_THANH_CONG',
          'YEU_CAU_TRA_HANG',
          'DA_TRA_HANG',
          'DA_HUY',
        ]"
        :key="tab"
        class="nav-link text-nowrap"
        :class="{ active: currentTab === tab }"
        @click="currentTab = tab"
      >
        {{ formatTabName(tab) }}
      </button>
    </div>

    <div v-if="loading" class="text-center py-5">Đang tải...</div>
    <div v-else-if="filteredOrders.length === 0" class="text-center py-5">
      Không có đơn hàng nào!
    </div>

    <div v-else v-for="item in filteredOrders" :key="item.donHang.id" class="card shadow-sm mb-4">
      <div class="card-header d-flex justify-content-between align-items-center bg-light">
        <span
          >Mã đơn: <strong>#{{ item.donHang.id }}</strong></span
        >
        <span class="badge" :class="getStatusClass(item.donHang.trangThai)">
          {{ getStatusLabel(item.donHang.trangThai) }}
        </span>
      </div>

      <div class="card-body">
        <div v-for="ct in item.chiTietDonHangs" :key="ct.id" class="d-flex mb-3 align-items-center">
          <div>
            <h6 class="mb-0">{{ ct.tenSanPham }}</h6>
            <small class="text-muted"
              >Size: {{ ct.kichCoSize || 'N/A' }} | Màu: {{ ct.mauSac || 'N/A' }}</small
            ><br />
            <small>x{{ ct.soLuong }} - {{ formatPrice(ct.donGia) }}</small>
          </div>
        </div>

        <!-- Hiển thị thông tin trả hàng nếu có -->
        <div
          v-if="
            item.donHang.trangThai === 'YEU_CAU_TRA_HANG' ||
            item.donHang.trangThai === 'DA_TRA_HANG'
          "
          class="alert alert-warning mt-3 small"
        >
          <strong>Trạng thái đổi trả:</strong> Đã gửi yêu cầu đổi trả sản phẩm.
        </div>
      </div>

      <div class="card-footer d-flex justify-content-between align-items-center">
        <span
          >Tổng tiền:
          <strong class="text-danger">{{ formatPrice(item.donHang.tongThanhToan) }}</strong></span
        >

        <div>
          <button
            v-if="item.donHang.trangThai === 'CHO_XAC_NHAN'"
            @click="huyDonHang(item.donHang.id)"
            class="btn btn-sm btn-outline-danger me-2"
          >
            Hủy đơn hàng
          </button>

          <button
            v-if="item.donHang.trangThai === 'DA_THANH_CONG'"
            @click="moModalTraHang(item.donHang.id)"
            class="btn btn-sm btn-warning"
          >
            📦 Yêu cầu trả hàng
          </button>
        </div>
      </div>
    </div>

    <!-- MODAL YÊU CẦU TRẢ HÀNG -->
    <div
      v-if="showReturnModal"
      class="modal show d-block"
      tabindex="-1"
      style="background: rgba(0, 0, 0, 0.5)"
    >
      <div class="modal-dialog">
        <div class="modal-content p-4">
          <h5 class="fw-bold mb-3">Yêu cầu trả hàng đơn #${{ returningOrderId }}</h5>

          <div class="mb-3">
            <label class="form-label small fw-semibold">Lý do trả hàng (*)</label>
            <textarea
              v-model="returnForm.lyDo"
              class="form-control"
              rows="3"
              placeholder="Nhập lý do bạn muốn đổi trả sản phẩm..."
            ></textarea>
          </div>

          <div class="mb-3">
            <label class="form-label small fw-semibold"
              >Ảnh minh chứng sản phẩm lỗi/không vừa</label
            >
            <input type="file" @change="uploadReturnImage" accept="image/*" class="form-control" />
            <div v-if="returnForm.anhMinhChung" class="mt-2">
              <img :src="returnForm.anhMinhChung" class="img-thumbnail" style="max-height: 120px" />
            </div>
          </div>

          <div class="d-flex justify-content-end gap-2">
            <button @click="showReturnModal = false" class="btn btn-secondary btn-sm">Hủy</button>
            <button @click="guiYeuCauTraHang" class="btn btn-primary btn-sm">Gửi yêu cầu</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import LayoutHeader from '@/components/LayoutHeader.vue'

const CLOUD_NAME = 'dqciew3rk'
const UPLOAD_PRESET = 'trendfit_preset'

const allOrders = ref([])
const loading = ref(true)
const currentTab = ref('TẤT CẢ')
const userId = localStorage.getItem('user_id')

// State Modal trả hàng
const showReturnModal = ref(false)
const returningOrderId = ref(null)
const returnForm = ref({ lyDo: '', anhMinhChung: '' })

const filteredOrders = computed(() => {
  if (currentTab.value === 'TẤT CẢ') return allOrders.value
  return allOrders.value.filter((o) => o.donHang.trangThai === currentTab.value)
})

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await axios.get(`http://localhost:8080/api/public/orders/user/${userId}`)
    allOrders.value = res.data
  } catch (e) {
    console.error(e)
    alert('Không tải được đơn hàng')
  } finally {
    loading.value = false
  }
}

const huyDonHang = async (id) => {
  if (!confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) return
  try {
    await axios.put(`http://localhost:8080/api/public/orders/${id}/status?status=DA_HUY`)
    alert('Đã hủy đơn hàng thành công!')
    loadOrders()
  } catch (e) {
    console.error(e)
    alert('Không thể hủy đơn hàng: ' + (e.response?.data || e.message))
  }
}

const moModalTraHang = (id) => {
  returningOrderId.value = id
  returnForm.value = { lyDo: '', anhMinhChung: '' }
  showReturnModal.value = true
}

const uploadReturnImage = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  const formData = new FormData()
  formData.append('file', file)
  formData.append('upload_preset', UPLOAD_PRESET)

  try {
    const res = await axios.post(
      `https://api.cloudinary.com/v1_1/${CLOUD_NAME}/image/upload`,
      formData,
    )
    returnForm.value.anhMinhChung = res.data.secure_url
  } catch (err) {
    alert('Tải ảnh minh chứng thất bại!')
  }
}

const guiYeuCauTraHang = async () => {
  if (!returnForm.value.lyDo.trim()) {
    alert('Vui lòng nhập lý do trả hàng!')
    return
  }

  try {
    await axios.put(
      `http://localhost:8080/api/public/orders/${returningOrderId.value}/request-return`,
      {
        lyDo: returnForm.value.lyDo,
        anhMinhChung: returnForm.value.anhMinhChung,
      },
    )
    alert('Gửi yêu cầu trả hàng thành công! Quản trị viên sẽ xem xét.')
    showReturnModal.value = false
    loadOrders()
  } catch (e) {
    alert('Lỗi: ' + (e.response?.data || e.message))
  }
}

const formatPrice = (v) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v)

const getStatusClass = (status) => {
  const map = {
    CHO_XAC_NHAN: 'bg-warning text-dark',
    DA_XAC_NHAN: 'bg-info',
    DANG_VAN_CHUYEN: 'bg-primary',
    DA_THANH_CONG: 'bg-success',
    YEU_CAU_TRA_HANG: 'bg-secondary text-white',
    DA_TRA_HANG: 'bg-dark text-white',
    DA_HUY: 'bg-danger',
  }
  return map[status] || 'bg-secondary'
}

const getStatusLabel = (status) => {
  const labels = {
    CHO_XAC_NHAN: 'Chờ xác nhận',
    DA_XAC_NHAN: 'Đã xác nhận',
    DANG_VAN_CHUYEN: 'Đang vận chuyển',
    DA_THANH_CONG: 'Đã thành công',
    YEU_CAU_TRA_HANG: 'Yêu cầu trả hàng',
    DA_TRA_HANG: 'Đã trả hàng',
    DA_HUY: 'Đã hủy',
  }
  return labels[status] || status
}

const formatTabName = (tab) => {
  if (tab === 'TẤT CẢ') return 'TẤT CẢ'
  return getStatusLabel(tab)
}

onMounted(loadOrders)
</script>
