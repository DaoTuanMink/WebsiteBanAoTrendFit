
<template>
  <div class="admin-product">
    <h2>Quản lý sản phẩm</h2>
    
    <!-- Bộ lọc sản phẩm -->
    <div class="filter-section" style="margin-bottom: 20px; padding: 15px; border: 1px solid #ccc;">
      <input v-model="searchQuery" placeholder="Tìm theo tên..." />
      
      <input v-model.number="minPrice" type="number" placeholder="Giá tối thiểu" />
      
      <select v-model="selectedSize">
        <option value="">Tất cả Size</option>
        <option value="S">S</option>
        <option value="M">M</option>
        <option value="L">L</option>
        <option value="XL">XL</option>
      </select>
      
      <select v-model="selectedColor">
        <option value="">Tất cả màu</option>
        <option value="Đỏ">Đỏ</option>
        <option value="Xanh">Xanh</option>
        <option value="Đen">Đen</option>
        <option value="Trắng">Trắng</option>
      </select>
    </div>

    <!-- Bảng danh sách sản phẩm -->
    <table>
      <thead>
        <tr>
          <th>Tên Sản Phẩm</th>
          <th>Giá</th>
          <th>Size</th>
          <th>Màu</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in filteredProducts" :key="item.id">
          <td>{{ item.name }}</td>
          <td>{{ item.price }}</td>
          <td>{{ item.size }}</td>
          <td>{{ item.color }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

// Giả lập dữ liệu sản phẩm
const products = ref([
  { id: 1, name: 'Áo thun Form rộng', price: 150000, size: 'M', color: 'Đen' },
  { id: 2, name: 'Áo sơ mi Caro', price: 250000, size: 'L', color: 'Đỏ' },
  { id: 3, name: 'Áo hoodie TrendFit', price: 350000, size: 'XL', color: 'Xanh' }
]);

const searchQuery = ref('');
const minPrice = ref(0);
const selectedSize = ref('');
const selectedColor = ref('');

// Logic lọc phía Client
const filteredProducts = computed(() => {
  return products.value.filter(p => {
    const matchesName = p.name.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchesPrice = p.price >= (minPrice.value || 0);
    const matchesSize = selectedSize.value === '' || p.size === selectedSize.value;
    const matchesColor = selectedColor.value === '' || p.color === selectedColor.value;
    
    return matchesName && matchesPrice && matchesSize && matchesColor;
  });
});
</script>
