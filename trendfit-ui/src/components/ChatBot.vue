<script setup>
import axios from "axios";
import { nextTick, ref } from "vue";
import API_BASE from "@/config/api";

const isOpen = ref(false);
const isSending = ref(false);
const message = ref("");
const messages = ref([
  {
    role: "bot",
    text: "Xin chào! 👋 Tôi là trợ lý AI của TrendFit. Tôi có thể giúp bạn tìm sản phẩm, tư vấn size, màu sắc và giải đáp các câu hỏi về mua hàng."
  }
]);

const suggestions = [
  "Sản phẩm nào đang bán chạy?",
  "Có áo nào dưới 500.000đ không?",
  "Tôi nên chọn size nào?",
];

const chatBody = ref(null);

const scrollToBottom = async () => {
  await nextTick();

  if (chatBody.value) {
    chatBody.value.scrollTop = chatBody.value.scrollHeight;
  }
};

const sendMessage = async (text = null) => {
  const content = (text ?? message.value).trim();

  if (!content || isSending.value) return;

  messages.value.push({
    role: "user",
    text: content
  });

  message.value = "";
  isSending.value = true;
  await scrollToBottom();

  try {
    const response = await axios.post(`${API_BASE}/api/chat`, {
      message: content
    });

    messages.value.push({
      role: "bot",
      text: response.data.reply
    });
  } catch (error) {
    const apiMessage = error.response?.data?.message;

    messages.value.push({
      role: "bot",
      text: apiMessage || "Không thể kết nối tới chatbot. Hãy kiểm tra backend đang chạy ở cổng 8080 rồi thử lại."
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
      class="chat-button"
      @click="isOpen = true"
      aria-label="Mở chatbot"
    >
      💬
    </button>

    <div v-if="isOpen" class="chat-window">
      <div class="chat-header">
        <div>
          <strong>TrendFit AI</strong>
          <span>● Đang hoạt động</span>
        </div>

        <button class="close-button" @click="isOpen = false" aria-label="Đóng chatbot">
          ×
        </button>
      </div>

      <div ref="chatBody" class="chat-body">
        <div
          v-for="(item, index) in messages"
          :key="index"
          class="message-row"
          :class="item.role"
        >
          <div class="message">
            {{ item.text }}
          </div>
        </div>

        <div v-if="isSending" class="message-row bot">
          <div class="message loading-message">AI đang trả lời...</div>
        </div>

        <div v-if="messages.length === 1" class="suggestions">
          <button
            v-for="suggestion in suggestions"
            :key="suggestion"
            :disabled="isSending"
            @click="sendMessage(suggestion)"
          >
            {{ suggestion }}
          </button>
        </div>
      </div>

      <form class="chat-input" @submit.prevent="sendMessage()">
        <input
          v-model="message"
          type="text"
          maxlength="2000"
          :disabled="isSending"
          placeholder="Nhập câu hỏi..."
        />

        <button type="submit" :disabled="isSending || !message.trim()" aria-label="Gửi câu hỏi">
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
  transition: 0.2s;
}

.chat-button:hover {
  transform: translateY(-3px) scale(1.05);
}

.chat-window {
  width: 370px;
  height: 520px;
  background: white;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 16px;
  background: #111827;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header strong {
  display: block;
  font-size: 17px;
}

.chat-header span {
  display: block;
  margin-top: 3px;
  font-size: 11px;
  color: #86efac;
}

.close-button {
  border: none;
  background: transparent;
  color: white;
  font-size: 26px;
  cursor: pointer;
}

.chat-body {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  background: #f9fafb;
}

.message-row {
  display: flex;
  margin-bottom: 12px;
}

.message-row.user {
  justify-content: flex-end;
}

.message {
  max-width: 80%;
  padding: 10px 13px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.message-row.bot .message {
  background: white;
  color: #1f2937;
  border: 1px solid #e5e7eb;
}

.message-row.user .message {
  background: #111827;
  color: white;
}

.loading-message {
  color: #6b7280 !important;
  font-style: italic;
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
  text-align: left;
  cursor: pointer;
  font-size: 13px;
}

.suggestions button:hover {
  background: #f3f4f6;
}

.chat-input {
  display: flex;
  padding: 12px;
  border-top: 1px solid #e5e7eb;
  background: white;
  gap: 8px;
}

.chat-input input {
  flex: 1;
  min-width: 0;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
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
  cursor: pointer;
  font-size: 17px;
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
    height: min(520px, calc(100vh - 24px));
  }
}
</style>
