<template>
  <!-- KHU VỰC CHUYỂN TRANG ĐỘNG CỦA VUE -->
  <!-- Mọi giao diện Header, Banner, Ruột sản phẩm, Footer của từng trang sẽ tự động lốt vào đây -->
  <main class="mt-0 overflow-hidden">
    <router-view />
  </main>
</template>

<style>
/* Reset nhẹ hệ thống hiển thị toàn trang cho mượt mà */
body {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  background-color: #ffffff;
}

/* Hiệu ứng trỏ chuột cho các icon hệ thống */
.cursor-pointer {
  cursor: pointer;
}
</style>
