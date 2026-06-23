<template>
  <div class="container-fluid py-4">
    <h3 class="fw-bold mb-4">QUẢN LÝ MÃ GIẢM GIÁ</h3>

    <button @click="moFormMoi" class="btn btn-primary mb-3">+ TẠO VOUCHER MỚI</button>

    <table class="table table-hover align-middle bg-white border">
      <thead class="table-dark">
        <tr>
          <th>Mã</th>
          <th>Tên</th>
          <th>Giá trị</th>
          <th>Hạn dùng</th>
          <th>Đã dùng/Giới hạn</th>
          <th>Trạng thái</th>
          <th>Thao tác</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="v in vouchers" :key="v.id">
          <td class="fw-bold text-primary">{{ v.ma }}</td>
          <td>{{ v.ten }}</td>
          <td>{{ v.giaTriGiam }} {{ v.loai === 'PERCENT' ? '%' : 'đ' }}</td>
          <td class="small">{{ v.ngayBatDau }} - {{ v.ngayKetThuc }}</td>
          <td>{{ v.soLanDaDung }} / {{ v.gioiHanSuDung }}</td>
          <td>
            <span :class="v.dangHoatDong ? 'badge bg-success' : 'badge bg-danger'">
              {{ v.dangHoatDong ? 'Hoạt động' : 'Đã khóa' }}
            </span>
          </td>
          <td>
            <button class="btn btn-sm btn-warning me-2" @click="editVoucher(v)">Sửa</button>
            <button class="btn btn-sm btn-danger" @click="xoaVoucher(v.id)">Xóa</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="showForm" class="card p-4 shadow mt-4">
      <h5>{{ editMode ? 'Cập nhật Voucher' : 'Tạo Voucher mới' }}</h5>
      <div class="row g-3">
        <div class="col-md-3">
          <input v-model="form.ma" placeholder="Mã Code" class="form-control" />
        </div>
        <div class="col-md-3">
          <input v-model="form.ten" placeholder="Tên chương trình" class="form-control" />
        </div>
        <div class="col-md-2">
          <select v-model="form.loai" class="form-select">
            <option value="PERCENT">Phần trăm (%)</option>
            <option value="FIXED">Số tiền (đ)</option>
          </select>
        </div>
        <div class="col-md-2">
          <input
            v-model="form.giaTriGiam"
            type="number"
            placeholder="Giá trị"
            class="form-control"
          />
        </div>
        <div class="col-md-2">
          <input
            v-model="form.gioiHanSuDung"
            type="number"
            placeholder="Giới hạn"
            class="form-control"
          />
        </div>
        <div class="col-md-3">
          <input v-model="form.ngayBatDau" type="date" class="form-control" />
        </div>
        <div class="col-md-3">
          <input v-model="form.ngayKetThuc" type="date" class="form-control" />
        </div>
      </div>
      <div class="mt-3">
        <button @click="saveVoucher" class="btn btn-success me-2">Lưu</button>
        <button @click="showForm = false" class="btn btn-secondary">Đóng</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const API = 'http://localhost:8080/api/admin/vouchers'
const vouchers = ref([])
const showForm = ref(false)
const editMode = ref(false)
const form = ref({})

const loadVouchers = async () => {
  const res = await axios.get(API)
  vouchers.value = res.data
}

const moFormMoi = () => {
  form.value = { ma: '', ten: '', loai: 'PERCENT', dangHoatDong: true }
  editMode.value = false
  showForm.value = true
}

const saveVoucher = async () => {
  try {
    if (editMode.value) await axios.put(`${API}/${form.value.id}`, form.value)
    else await axios.post(API, form.value)
    alert('Lưu thành công!')
    showForm.value = false
    loadVouchers()
  } catch (e) {
    alert('Lỗi lưu voucher!')
  }
}

const xoaVoucher = async (id) => {
  if (confirm('Xóa voucher này?')) {
    await axios.delete(`${API}/${id}`)
    loadVouchers()
  }
}

onMounted(loadVouchers)
</script>
