<template>
  <div class="container py-5">
    <h3 class="fw-bold mb-4">QUẢN LÝ NHÂN VIÊN</h3>

    <div class="card p-4 mb-5 shadow-sm">
      <h5>{{ isEditing ? 'Cập nhật nhân viên' : 'Tạo tài khoản nhân viên' }}</h5>
      <div class="row g-3">
        <div class="col-md-3">
          <input v-model="newStaff.hoTen" placeholder="Họ tên" class="form-control" />
        </div>
        <div class="col-md-3">
          <input
            v-model="newStaff.email"
            placeholder="Email"
            class="form-control"
            :disabled="isEditing"
          />
        </div>
        <div class="col-md-2" v-if="!isEditing">
          <input
            v-model="newStaff.matKhau"
            type="password"
            placeholder="Mật khẩu"
            class="form-control"
          />
        </div>
        <div class="col-md-2">
          <input v-model="newStaff.maNhanVien" placeholder="Mã NV" class="form-control" />
        </div>
        <div class="col-md-2">
          <input v-model="newStaff.soDienThoai" placeholder="Số ĐT" class="form-control" />
        </div>
        <div class="col-md-2">
          <input v-model="newStaff.ngayVaoLam" type="date" class="form-control" />
        </div>
        <div class="col-md-2">
          <button @click="isEditing ? updateStaff() : createStaff()" class="btn btn-dark w-100">
            {{ isEditing ? 'Cập nhật' : 'Tạo mới' }}
          </button>
        </div>
      </div>
    </div>

    <div class="input-group mb-3 w-50">
      <input
        v-model="searchQuery"
        @input="searchStaff"
        placeholder="Tìm theo mã NV..."
        class="form-control"
      />
    </div>

    <table class="table table-bordered table-hover align-middle">
      <thead class="table-dark">
        <tr>
          <th>STT</th>
          <th>Mã NV</th>
          <th>Họ tên</th>
          <th>Email</th>
          <th>Số ĐT</th>
          <th>Trạng thái</th>
          <th>Ngày vào làm</th>
          <th>Hành động</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(nv, index) in staffList" :key="nv.id">
          <td>{{ index + 1 }}</td>
          <td>{{ nv.maNhanVien }}</td>
          <td>{{ nv.nguoiDung?.hoTen }}</td>
          <td>{{ nv.nguoiDung?.email }}</td>
          <td>{{ nv.nguoiDung?.soDienThoai }}</td>
          <td>
            <span class="badge" :class="nv.nguoiDung?.dangHoatDong ? 'bg-success' : 'bg-secondary'">
              {{ nv.nguoiDung?.dangHoatDong ? 'Hoạt động' : 'Khóa' }}
            </span>
          </td>
          <td>{{ formatDate(nv.ngayVaoLam) }}</td>
          <td>
            <button @click="editStaff(nv)" class="btn btn-sm btn-warning me-2">Sửa</button>
            <button @click="deleteStaff(nv.id)" class="btn btn-sm btn-danger">Xóa</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const staffList = ref([])
const searchQuery = ref('')
const isEditing = ref(false)
const editingId = ref(null)

const newStaff = ref({ hoTen: '', email: '', matKhau: '', maNhanVien: '', chucVu: 'NV' })

const fetchStaff = async () => {
  const res = await axios.get('http://localhost:8080/api/admin/users/staff')
  staffList.value = res.data
}

const createStaff = async () => {
  await axios.post('http://localhost:8080/api/admin/users/create-staff', newStaff.value)
  alert('Đã tạo thành công!')
  resetForm()
  fetchStaff()
}

const editStaff = (nv) => {
  isEditing.value = true
  editingId.value = nv.id
  newStaff.value = {
    hoTen: nv.nguoiDung.hoTen,
    email: nv.nguoiDung.email,
    maNhanVien: nv.maNhanVien,
    chucVu: nv.chucVu,
  }
}

const updateStaff = async () => {
  await axios.put(`http://localhost:8080/api/admin/users/${editingId.value}`, newStaff.value)
  alert('Đã cập nhật!')
  resetForm()
  fetchStaff()
}

const deleteStaff = async (id) => {
  if (confirm('Bạn chắc chắn muốn xóa?')) {
    await axios.delete(`http://localhost:8080/api/admin/users/${id}`)
    fetchStaff()
  }
}

const searchStaff = async () => {
  if (!searchQuery.value) return fetchStaff()
  const res = await axios.get(
    `http://localhost:8080/api/admin/users/search?maNhanVien=${searchQuery.value}`,
  )
  staffList.value = res.data
}

const resetForm = () => {
  newStaff.value = { hoTen: '', email: '', matKhau: '', maNhanVien: '', chucVu: 'NV' }
  isEditing.value = false
  editingId.value = null
}

// Thêm hàm này vào trong phần <script setup>
const formatDate = (dateString) => {
  if (!dateString) return 'Chưa rõ'
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN') // Định dạng ngày kiểu Việt Nam: DD/MM/YYYY
}

onMounted(fetchStaff)
</script>
