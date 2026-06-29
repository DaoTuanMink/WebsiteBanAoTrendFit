<template>
  <div class="container py-5">
    <h3 class="fw-bold mb-4">QUẢN LÝ NHÂN VIÊN & PHÂN QUYỀN</h3>

    <div class="card p-4 mb-5">
      <h5>Tạo tài khoản nhân viên</h5>
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

    <table class="table table-bordered">
      <thead class="table-dark">
        <tr>
          <th>Mã NV</th>
          <th>Họ tên</th>
          <th>Email</th>
          <th>Hành động</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="nv in staffList" :key="nv.id">
          <td>{{ nv.maNhanVien }}</td>
          <td>{{ nv.nguoiDung.hoTen }}</td>
          <td>{{ nv.nguoiDung.email }}</td>
          <td>
            <button @click="openPermissionModal(nv)" class="btn btn-sm btn-primary">
              Phân quyền
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="showModal" class="modal d-block" style="background: rgba(0, 0, 0, 0.5)">
      <div class="modal-dialog">
        <div class="modal-content p-4">
          <h5>Phân quyền cho: {{ selectedStaff.nguoiDung.hoTen }}</h5>
          <div class="form-check form-switch" v-for="mod in modules" :key="mod">
            <label>{{ mod }}</label>
            <input type="checkbox" class="form-check-input" v-model="permissions[mod].docDuoc" />
            Xem
            <input type="checkbox" class="form-check-input" v-model="permissions[mod].suaDuoc" />
            Sửa
          </div>
          <button @click="savePermissions" class="btn btn-success mt-3">Lưu phân quyền</button>
          <button @click="showModal = false" class="btn btn-secondary mt-3">Đóng</button>
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
  DON_HANG: { docDuoc: false, suaDuoc: false },
  SAN_PHAM: { docDuoc: false, suaDuoc: false },
})

const fetchStaff = async () => {
  const res = await axios.get('http://localhost:8080/api/admin/staff') // Bạn cần viết API này nhé
  staffList.value = res.data
}

const createStaff = async () => {
  await axios.post('http://localhost:8080/api/admin/users/create-staff', newStaff.value)
  alert('Đã tạo nhân viên!')
  fetchStaff()
}

const savePermissions = async () => {
  // Chuyển đổi object permissions thành mảng để gửi lên backend
  const data = Object.keys(permissions.value).map((m) => ({ module: m, ...permissions.value[m] }))
  await axios.post(
    `http://localhost:8080/api/admin/users/${selectedStaff.value.id}/permissions`,
    data,
  )
  alert('Đã lưu!')
  showModal.value = false
}

onMounted(fetchStaff)
</script>
