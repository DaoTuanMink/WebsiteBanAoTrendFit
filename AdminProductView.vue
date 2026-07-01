<template>
  <div class="admin-product">
    <h2>Quản lý sản phẩm</h2>
    <div class="filters">
      <input v-model="searchQuery" placeholder="Tìm theo tên..." />
      <input v-model.number="minPrice" placeholder="Giá tối thiểu" />
    </div>
    <table>
      <tr v-for="item in filteredProducts" :key="item.id">
        <td>{{ item.name }}</td>
        <td>{{ item.price }}</td>
      </tr>
    </table>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
const products = ref([]);
const searchQuery = ref('');
const minPrice = ref(0);
const filteredProducts = computed(() => products.value.filter(p => 
  p.name.includes(searchQuery.value) && p.price >= minPrice.value
));
</script>
