<template>
  <div class="container-fluid py-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3 class="fw-bold m-0">QUẢN LÝ MÃ GIẢM GIÁ (VOUCHER)</h3>
      <button @click="moFormMoi" class="btn btn-primary">+ TẠO VOUCHER MỚI</button>
    </div>

    <!-- Bảng danh sách voucher hiển thị đầy đủ các trường -->
    <div class="card shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-dark text-center">
              <tr>
                <th>Mã Code</th>
                <th>Tên chương trình</th>
                <th>Loại & Giá trị</th>
                <th>Giảm tối đa</th>
                <th>Đơn tối thiểu</th>
                <th>Thời hạn</th>
                <th>Đã dùng / Giới hạn</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="v in vouchers" :key="v.id">
                <td class="fw-bold text-primary text-center">{{ v.ma }}</td>
                <td>{{ v.ten }}</td>
                <td class="text-center">
                  <span class="badge bg-info text-dark">
                    {{ v.loai === 'PERCENT' ? v.giaTriGiam + '%' : formatCurrency(v.giaTriGiam) }}
                  </span>
                </td>
                <td class="text-end">
                  {{ v.giaTriToiDa ? formatCurrency(v.giaTriToiDa) : 'Không giới hạn' }}
                </td>
                <td class="text-end">
                  {{ v.donHangToiThieu ? formatCurrency(v.donHangToiThieu) : '0 đ' }}
                </td>
                <td class="small text-center text-muted">
                  {{ v.ngayBatDau || '---' }} <br />đến<br />
                  {{ v.ngayKetThuc || '---' }}
                </td>
                <td class="text-center">
                  <span class="fw-semibold">{{ v.soLanDaDung || 0 }}</span> /
                  {{
                    v.gioiHanSuDung !== null && v.gioiHanSuDung !== undefined
                      ? v.gioiHanSuDung
                      : '∞'
                  }}
                </td>
                <td class="text-center">
                  <!-- Nút bấm chuyển đổi trạng thái Hoạt động / Khóa trực tiếp tại bảng -->
                  <button
                    type="button"
                    class="btn btn-sm w-100"
                    :class="v.dangHoatDong ? 'btn-success' : 'btn-secondary'"
                    @click="toggleStatus(v)"
                  >
                    {{ v.dangHoatDong ? 'Hoạt động' : 'Đã khóa' }}
                  </button>
                </td>
                <td class="text-center">
                  <button class="btn btn-sm btn-warning me-1" @click="editVoucher(v)" title="Sửa">
                    ✏️
                  </button>
                  <button class="btn btn-sm btn-danger" @click="xoaVoucher(v.id)" title="Xóa">
                    🗑️
                  </button>
                </td>
              </tr>
              <tr v-if="vouchers.length === 0">
                <td colspan="9" class="text-center py-4 text-muted">
                  Chưa có mã giảm giá nào trong hệ thống.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Form Thêm / Sửa Voucher đầy đủ trường -->
    <div v-if="showForm" class="card p-4 shadow mt-4 border-0 bg-light">
      <h5 class="fw-bold mb-3">
        {{ editMode ? 'Cập nhật Voucher #' + form.id : 'Tạo Voucher mới' }}
      </h5>
      <div class="row g-3">
        <div class="col-md-3">
          <label class="form-label small fw-semibold">Mã Code (*)</label>
          <input v-model="form.ma" placeholder="VD: SALE50K" class="form-control" />
        </div>
        <div class="col-md-3">
          <label class="form-label small fw-semibold">Tên chương trình</label>
          <input v-model="form.ten" placeholder="VD: Siêu sale hè 2026" class="form-control" />
        </div>
        <div class="col-md-2">
          <label class="form-label small fw-semibold">Loại giảm giá</label>
          <select v-model="form.loai" class="form-select">
            <option value="PERCENT">Phần trăm (%)</option>
            <option value="FIXED">Số tiền (đ)</option>
          </select>
        </div>
        <div class="col-md-2">
          <label class="form-label small fw-semibold">Giá trị giảm (*)</label>
          <input
            v-model.number="form.giaTriGiam"
            type="number"
            placeholder="VD: 10 hoặc 50000"
            class="form-control"
          />
        </div>
        <div class="col-md-2">
          <label class="form-label small fw-semibold">Giới hạn số lượt dùng</label>
          <input
            v-model.number="form.gioiHanSuDung"
            type="number"
            placeholder="Để trống = Vô hạn"
            class="form-control"
          />
        </div>

        <div class="col-md-3">
          <label class="form-label small fw-semibold">Giá trị giảm tối đa (Cho %)</label>
          <input
            v-model.number="form.giaTriToiDa"
            type="number"
            placeholder="VD: 100000"
            class="form-control"
          />
        </div>
        <div class="col-md-3">
          <label class="form-label small fw-semibold">Đơn hàng tối thiểu</label>
          <input
            v-model.number="form.donHangToiThieu"
            type="number"
            placeholder="VD: 200000"
            class="form-control"
          />
        </div>
        <div class="col-md-3">
          <label class="form-label small fw-semibold">Ngày bắt đầu</label>
          <input v-model="form.ngayBatDau" type="date" class="form-control" />
        </div>
        <div class="col-md-3">
          <label class="form-label small fw-semibold">Ngày kết thúc</label>
          <input v-model="form.ngayKetThuc" type="date" class="form-control" />
        </div>
      </div>

      <div class="mt-4 d-flex gap-2">
        <button @click="saveVoucher" class="btn btn-success px-4">💾 Lưu Voucher</button>
        <button @click="showForm = false" class="btn btn-secondary px-4">Hủy</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { getAuthHeaders } from '@/utils/adminAuth'

const API = 'http://localhost:8080/api/admin/vouchers'

const apiAdmin = axios.create()
apiAdmin.interceptors.request.use((config) => {
  config.headers = { ...config.headers, ...getAuthHeaders() }
  return config
})

const vouchers = ref([])
const showForm = ref(false)
const editMode = ref(false)
const form = ref({})

const formatCurrency = (val) => {
  if (!val) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

const loadVouchers = async () => {
  try {
    const res = await apiAdmin.get(API)
    vouchers.value = res.data
  } catch (err) {
    alert('Không thể tải danh sách voucher: ' + (err.response?.data || err.message))
  }
}

const moFormMoi = () => {
  form.value = {
    ma: '',
    ten: '',
    loai: 'PERCENT',
    giaTriGiam: null,
    giaTriToiDa: null,
    donHangToiThieu: null,
    gioiHanSuDung: null,
    ngayBatDau: '',
    ngayKetThuc: '',
    dangHoatDong: true,
  }
  editMode.value = false
  showForm.value = true
}

const editVoucher = (v) => {
  form.value = { ...v }
  editMode.value = true
  showForm.value = true
}

// Nút chuyển đổi nhanh trạng thái hoạt động ngay trên bảng
const toggleStatus = async (v) => {
  try {
    const updated = { ...v, dangHoatDong: !v.dangHoatDong }
    await apiAdmin.put(`${API}/${v.id}`, updated)
    v.dangHoatDong = updated.dangHoatDong
  } catch (err) {
    alert('Không thể đổi trạng thái: ' + (err.response?.data?.message || err.message))
  }
}

const xoaVoucher = async (id) => {
  if (confirm('Bạn có chắc chắn muốn xóa voucher này?')) {
    try {
      await apiAdmin.delete(`${API}/${id}`)
      loadVouchers()
    } catch (err) {
      alert('Xóa thất bại: ' + (err.response?.data || err.message))
    }
  }
}

const saveVoucher = async () => {
  if (!form.value.ma || form.value.giaTriGiam === null || form.value.giaTriGiam === undefined) {
    alert('Vui lòng nhập đủ Mã Code và Giá trị giảm!')
    return
  }

  try {
    if (editMode.value) {
      await apiAdmin.put(`${API}/${form.value.id}`, form.value)
    } else {
      await apiAdmin.post(API, form.value)
    }
    alert('Lưu voucher thành công!')
    showForm.value = false
    loadVouchers()
  } catch (e) {
    alert('Lỗi lưu voucher: ' + (e.response?.data?.message || 'Có lỗi xảy ra'))
  }
}

onMounted(loadVouchers)
</script>

<style scoped>
.table th,
.table td {
  vertical-align: middle;
}
</style>
