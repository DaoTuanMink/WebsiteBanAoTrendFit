<template>
  <div class="container py-5">
    <h3 class="fw-bold mb-4">QUẢN LÝ NHÂN VIÊN & PHÂN QUYỀN</h3>

    <div class="card p-4 mb-5 shadow-sm">
      <h5 class="mb-3">Tạo tài khoản nhân viên</h5>
      <div class="row g-3">
        <div class="col-md-3">
          <input v-model="newStaff.hoTen" placeholder="Họ tên" class="form-control" />
        </div>
        <div class="col-md-3">
          <input v-model="newStaff.email" placeholder="Email" class="form-control" />
        </div>
        <div class="col-md-2">
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
          <button @click="createStaff" class="btn btn-dark w-100">Tạo mới</button>
        </div>
      </div>
    </div>

    <table class="table table-bordered table-hover align-middle">
      <thead class="table-dark">
        <tr>
          <th>ID</th>
          <th>Mã NV</th>
          <th>Họ tên</th>
          <th>Email</th>
          <th>Trạng thái</th>
          <th>Vai trò</th>
          <th>Hành động</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="nv in staffList" :key="nv.id">
          <td>{{ nv.id }}</td>
          <td>{{ nv.maNhanVien }}</td>
          <td>{{ nv.nguoiDung.hoTen }}</td>
          <td>{{ nv.nguoiDung.email }}</td>
          <td>
            <span class="badge" :class="nv.nguoiDung.dangHoatDong ? 'bg-success' : 'bg-secondary'">
              {{ nv.nguoiDung.dangHoatDong ? 'Hoạt động' : 'Khóa' }}
            </span>
          </td>
          <td>{{ nv.nguoiDung.vaiTro }}</td>
          <td>
            <button @click="openPermissionModal(nv)" class="btn btn-sm btn-primary">
              Phân quyền
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="showModal" class="modal d-block" style="background: rgba(0, 0, 0, 0.5)">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content p-4">
          <h5 class="mb-3">Phân quyền: {{ selectedStaff?.nguoiDung?.hoTen }}</h5>
          <div class="mb-3" v-for="mod in modules" :key="mod">
            <label class="fw-bold d-block mb-2">{{ mod }}</label>
            <div class="d-flex gap-3">
              <label><input type="checkbox" v-model="permissions[mod].docDuoc" /> Xem</label>
              <label><input type="checkbox" v-model="permissions[mod].suaDuoc" /> Sửa</label>
              <label><input type="checkbox" v-model="permissions[mod].themDuoc" /> Thêm</label>
              <label><input type="checkbox" v-model="permissions[mod].xoaDuoc" /> Xóa</label>
            </div>
          </div>
          <div class="d-flex justify-content-end gap-2 mt-3">
            <button @click="showModal = false" class="btn btn-secondary">Đóng</button>
            <button @click="savePermissions" class="btn btn-success">Lưu thay đổi</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const staffList = ref([])
const showModal = ref(false)
const selectedStaff = ref(null)
const newStaff = ref({ hoTen: '', email: '', matKhau: '', maNhanVien: '', chucVu: 'NV' })
const modules = ['DON_HANG', 'SAN_PHAM']
const permissions = ref({
  DON_HANG: { docDuoc: false, themDuoc: false, suaDuoc: false, xoaDuoc: false },
  SAN_PHAM: { docDuoc: false, themDuoc: false, suaDuoc: false, xoaDuoc: false },
})

const fetchStaff = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/admin/staff')
    staffList.value = res.data
  } catch (err) {
    console.error('Lỗi khi tải danh sách nhân viên:', err)
  }
}

const createStaff = async () => {
  try {
    await axios.post('http://localhost:8080/api/admin/users/create-staff', newStaff.value)
    alert('Đã tạo nhân viên thành công!')
    fetchStaff()
    newStaff.value = { hoTen: '', email: '', matKhau: '', maNhanVien: '', chucVu: 'NV' }
  } catch (err) {
    alert('Tạo nhân viên thất bại!')
  }
}

const openPermissionModal = (nv) => {
  selectedStaff.value = nv
  showModal.value = true
  // Ở đây bạn nên gọi API lấy quyền hiện tại của nv và gán vào biến permissions
}

const savePermissions = async () => {
  const data = Object.keys(permissions.value).map((m) => ({ module: m, ...permissions.value[m] }))
  await axios.post(
    `http://localhost:8080/api/admin/users/${selectedStaff.value.id}/permissions`,
    data,
  )
  alert('Đã cập nhật phân quyền!')
  showModal.value = false
}

onMounted(fetchStaff)
</script>
