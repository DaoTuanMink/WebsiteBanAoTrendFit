<script setup>
import axios from "axios";
import { nextTick, ref } from "vue";
import API_BASE from "@/config/api";

const isOpen = ref(false);
const isSending = ref(false);
const message = ref("");
const chatBody = ref(null);

const messages = ref([
  {
    role: "bot",
    text: "Xin chào! 👋 Tôi là trợ lý AI của TrendFit. Tôi có thể tìm sản phẩm theo giá, size, màu, giới tính, chất liệu, thương hiệu và so sánh sản phẩm.",
    products: []
  }
]);

const suggestions = [
  "Áo thun nam size S màu đen dưới 400k",
  "Sản phẩm nào đang giảm giá?",
  "Áo nào rẻ nhất?",
  "So sánh áo Basic Essential và Relax Fit"
];

const welcomeMessage = {
  role: "bot",
  text: "Xin chào! 👋 Tôi là trợ lý AI của TrendFit. Tôi có thể tìm sản phẩm theo giá, size, màu, giới tính, chất liệu, thương hiệu và so sánh sản phẩm.",
  products: []
};

const resetChat = async () => {
  messages.value = [{ ...welcomeMessage }];
  message.value = "";
  await scrollToBottom();
};

const scrollToBottom = async () => {
  await nextTick();

  if (chatBody.value) {
    chatBody.value.scrollTop = chatBody.value.scrollHeight;
  }
};

const formatPrice = (price) => {
  if (price === null || price === undefined) {
    return "Liên hệ";
  }

  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND"
  }).format(Number(price));
};

const sendMessage = async (text = null) => {
  const content = (text ?? message.value).trim();

  if (!content || isSending.value) return;

  // Gửi lịch sử ngắn để bot hiểu câu hỏi nối tiếp.
  const history = messages.value
    .filter(item => item.text && item.text.trim())
    .slice(-10)
    .map(item => ({
      role: item.role,
      content: item.text.trim()
    }));

  messages.value.push({
    role: "user",
    text: content,
    products: []
  });

  message.value = "";
  isSending.value = true;
  await scrollToBottom();

  try {
    const response = await axios.post(`${API_BASE}/api/chat`, {
      message: content,
      history: history
    });

    messages.value.push({
      role: "bot",
      text: response.data.reply,
      products: Array.isArray(response.data.products)
        ? response.data.products
        : []
    });
  } catch (error) {
    const apiMessage = error.response?.data?.message;

    messages.value.push({
      role: "bot",
      text: apiMessage ||
        "Không thể kết nối tới chatbot. Hãy kiểm tra backend đang chạy ở cổng 8080 rồi thử lại.",
      products: []
    });
  } finally {
    isSending.value = false;
    await scrollToBottom();
  }
};
</script>

<template>
  <div class="chatbot">
    <button
      v-if="!isOpen"
      type="button"
      class="chat-button"
      aria-label="Mở chatbot"
      @click="isOpen = true"
    >
      💬
    </button>

    <div v-if="isOpen" class="chat-window">
      <div class="chat-header">
        <div>
          <strong>TrendFit AI</strong>
          <span>● Đang hoạt động</span>
        </div>

        <div class="header-actions">
          <button type="button" class="reset-button" aria-label="Xóa hội thoại" @click="resetChat">
            Làm mới
          </button>
          <button type="button" class="close-button" aria-label="Đóng chatbot" @click="isOpen = false">
            ×
          </button>
        </div>
      </div>

      <div ref="chatBody" class="chat-body">
        <div
          v-for="(item, index) in messages"
          :key="index"
          class="message-row"
          :class="item.role"
        >
          <div class="message-content">
            <div class="message">
              {{ item.text }}
            </div>

            <div
              v-if="
                item.role === 'bot' &&
                Array.isArray(item.products) &&
                item.products.length > 0
              "
              class="chat-products"
            >
              <RouterLink
                v-for="product in item.products"
                :key="product.id"
                :to="product.detailUrl"
                class="chat-product-card"
                @click="isOpen = false"
              >
                <img
                  v-if="product.imageUrl"
                  :src="product.imageUrl"
                  :alt="product.name"
                  class="chat-product-image"
                />

                <div
                  v-else
                  class="chat-product-image image-placeholder"
                >
                  TrendFit
                </div>

                <div class="chat-product-info">
                  <div class="product-title-line">
                    <strong :title="product.name">{{ product.name }}</strong>
                    <span v-if="product.discountPercent > 0" class="discount-badge">
                      -{{ product.discountPercent }}%
                    </span>
                  </div>

                  <div class="price-line">
                    <span class="chat-product-price">{{ formatPrice(product.price) }}</span>
                    <del v-if="product.discountPercent > 0" class="original-price">
                      {{ formatPrice(product.originalPrice) }}
                    </del>
                  </div>

                  <small v-if="product.brand || product.category" class="product-meta">
                    {{ [product.brand, product.category].filter(Boolean).join(" • ") }}
                  </small>

                  <small v-if="product.material || product.gender" class="product-meta">
                    {{ [product.material, product.gender].filter(Boolean).join(" • ") }}
                  </small>

                  <div v-if="product.sizes?.length" class="variant-line">
                    <span class="variant-label">Size:</span>
                    <span v-for="size in product.sizes" :key="size" class="variant-chip">{{ size }}</span>
                  </div>

                  <div v-if="product.colors?.length" class="variant-line">
                    <span class="variant-label">Màu:</span>
                    <span v-for="color in product.colors" :key="color" class="variant-chip">{{ color }}</span>
                  </div>

                  <small v-if="product.stock > 0">
                    Còn {{ product.stock }} sản phẩm
                  </small>

                  <small v-else class="out-of-stock">
                    Tạm hết hàng
                  </small>

                  <small v-if="product.soldQuantity > 0">
                    Đã bán {{ product.soldQuantity }}
                  </small>

                  <span class="view-product">
                    Xem sản phẩm →
                  </span>
                </div>
              </RouterLink>
            </div>
          </div>
        </div>

        <div v-if="isSending" class="message-row bot">
          <div class="message loading-message">
            <span class="loading-dot"></span>
            <span class="loading-dot"></span>
            <span class="loading-dot"></span>
            AI đang trả lời...
          </div>
        </div>

        <div
          v-if="messages.length === 1"
          class="suggestions"
        >
          <button
            v-for="suggestion in suggestions"
            :key="suggestion"
            type="button"
            :disabled="isSending"
            @click="sendMessage(suggestion)"
          >
            {{ suggestion }}
          </button>
        </div>
      </div>

      <form
        class="chat-input"
        @submit.prevent="sendMessage()"
      >
        <input
          v-model="message"
          type="text"
          maxlength="2000"
          :disabled="isSending"
          placeholder="Nhập câu hỏi..."
          autocomplete="off"
        />

        <button
          type="submit"
          :disabled="isSending || !message.trim()"
          aria-label="Gửi câu hỏi"
        >
          ➤
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.chatbot {
  position: fixed;
  right: 25px;
  bottom: 25px;
  z-index: 9999;
}

.chat-button {
  width: 60px;
  height: 60px;
  border: none;
  border-radius: 50%;
  background: #111827;
  color: white;
  font-size: 25px;
  cursor: pointer;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.25);
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}

.chat-button:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.3);
}

.chat-window {
  width: 390px;
  height: 600px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 18px;
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.25);
}

.chat-header {
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #111827;
  color: white;
}

.chat-header strong {
  display: block;
  font-size: 17px;
}

.chat-header span {
  display: block;
  margin-top: 3px;
  color: #86efac;
  font-size: 11px;
}

.close-button {
  border: none;
  background: transparent;
  color: white;
  font-size: 26px;
  cursor: pointer;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.reset-button {
  padding: 6px 9px;
  border: 1px solid #475569;
  border-radius: 8px;
  background: transparent;
  color: #e5e7eb;
  font-size: 11px;
  cursor: pointer;
}

.reset-button:hover {
  background: #1f2937;
}

.chat-body {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  scroll-behavior: smooth;
  background: #f9fafb;
}

.message-row {
  display: flex;
  margin-bottom: 12px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-content {
  max-width: 88%;
}

.message-row.user .message-content {
  display: flex;
  justify-content: flex-end;
}

.message {
  max-width: 100%;
  padding: 10px 13px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.message-row.bot .message {
  border: 1px solid #e5e7eb;
  background: white;
  color: #1f2937;
}

.message-row.user .message {
  background: #111827;
  color: white;
}

.loading-message {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #6b7280 !important;
  font-style: italic;
}

.loading-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #6b7280;
  animation: loading-bounce 1.2s infinite;
}

.loading-dot:nth-child(2) {
  animation-delay: 0.15s;
}

.loading-dot:nth-child(3) {
  margin-right: 4px;
  animation-delay: 0.3s;
}

@keyframes loading-bounce {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.4;
  }

  30% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

.chat-products {
  display: flex;
  flex-direction: column;
  gap: 9px;
  margin-top: 9px;
}

.chat-product-card {
  display: flex;
  gap: 10px;
  padding: 8px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: white;
  color: #1f2937;
  text-decoration: none;
  transition:
    border-color 0.2s,
    box-shadow 0.2s,
    transform 0.2s;
}

.chat-product-card:hover {
  border-color: #111827;
  box-shadow: 0 5px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.chat-product-image {
  width: 76px;
  height: 88px;
  flex-shrink: 0;
  border-radius: 8px;
  object-fit: cover;
  background: #f3f4f6;
}

.image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  font-size: 11px;
}

.chat-product-info {
  min-width: 0;
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 3px;
}

.chat-product-info strong {
  display: -webkit-box;
  overflow: hidden;
  font-size: 13px;
  line-height: 1.35;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.product-title-line,
.price-line,
.variant-line {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.product-title-line strong {
  flex: 1;
}

.discount-badge {
  padding: 2px 5px;
  border-radius: 5px;
  background: #fee2e2;
  color: #b91c1c;
  font-size: 10px;
  font-weight: 700;
}

.chat-product-price {
  color: #dc2626;
  font-size: 13px;
  font-weight: 700;
}

.original-price {
  color: #9ca3af;
  font-size: 10px;
}

.product-meta {
  text-transform: capitalize;
}

.variant-line {
  font-size: 10px;
}

.variant-label {
  color: #6b7280;
}

.variant-chip {
  padding: 1px 5px;
  border: 1px solid #e5e7eb;
  border-radius: 999px;
  background: #f9fafb;
  color: #374151;
}

.chat-product-info small {
  color: #6b7280;
  font-size: 11px;
}

.chat-product-info .out-of-stock {
  color: #dc2626;
}

.view-product {
  margin-top: auto;
  color: #111827;
  font-size: 11px;
  font-weight: 600;
}

.suggestions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 10px;
}

.suggestions button {
  padding: 9px 12px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  background: white;
  font-size: 13px;
  text-align: left;
  cursor: pointer;
}

.suggestions button:hover {
  background: #f3f4f6;
}

.chat-input {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid #e5e7eb;
  background: white;
}

.chat-input input {
  min-width: 0;
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  outline: none;
}

.chat-input input:focus {
  border-color: #111827;
}

.chat-input button {
  width: 42px;
  border: none;
  border-radius: 10px;
  background: #111827;
  color: white;
  font-size: 17px;
  cursor: pointer;
}

.chat-input button:disabled,
.chat-input input:disabled,
.suggestions button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

@media (max-width: 480px) {
  .chatbot {
    right: 12px;
    bottom: 12px;
  }

  .chat-window {
    width: calc(100vw - 24px);
    height: min(600px, calc(100vh - 24px));
  }
}
</style>
