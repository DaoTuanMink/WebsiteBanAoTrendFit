<template>
  <footer class="tf-footer">
    <div class="container-fluid px-4 px-lg-5">
      <div class="tf-footer-grid">
        <!-- Brand -->
        <div class="tf-footer-brand">
          <div class="tf-footer-logo">
            <span class="tf-logo-mark">TF</span>
            <span class="tf-logo-text">TRENDFIT</span>
          </div>
          <p class="tf-footer-desc">
            Fashion Tech 2026 — Nền tảng thời trang hiện đại, tối ưu trải nghiệm mua sắm thông minh.
          </p>
          <div class="tf-social">
            <a href="#" class="tf-social-btn" aria-label="Facebook"><i class="ri-facebook-fill"></i></a>
            <a href="#" class="tf-social-btn" aria-label="Instagram"><i class="ri-instagram-line"></i></a>
            <a href="#" class="tf-social-btn" aria-label="TikTok"><i class="ri-tiktok-fill"></i></a>
            <a href="#" class="tf-social-btn" aria-label="YouTube"><i class="ri-youtube-fill"></i></a>
          </div>
        </div>

        <!-- Links -->
        <div class="tf-footer-col">
          <h6 class="tf-footer-title">Khám phá</h6>
          <ul class="tf-footer-links">
            <li><router-link to="/">Trang chủ</router-link></li>
            <li><router-link to="/ao">Áo</router-link></li>
            <li><a href="#">Quần</a></li>
            <li><a href="#">Phụ kiện</a></li>
            <li><a href="#">Bộ sưu tập mới</a></li>
          </ul>
        </div>

        <div class="tf-footer-col">
          <h6 class="tf-footer-title">Hỗ trợ</h6>
          <ul class="tf-footer-links">
            <li><a href="#">Chính sách thành viên</a></li>
            <li><a href="#">Bảo mật thông tin</a></li>
            <li><a href="#">Tra cứu đơn hàng</a></li>
            <li><a href="#">Đổi trả & hoàn tiền</a></li>
            <li><a href="#">Liên hệ</a></li>
          </ul>
        </div>

        <div class="tf-footer-col">
          <h6 class="tf-footer-title">Thanh toán</h6>
          <div class="tf-pay-icons">
            <i class="ri-visa-line" title="Visa"></i>
            <i class="ri-mastercard-line" title="Mastercard"></i>
            <i class="ri-bank-card-line" title="ATM"></i>
            <i class="ri-wallet-3-line" title="Ví điện tử"></i>
          </div>
          <p class="tf-footer-note mt-3">
            <i class="ri-map-pin-line me-1"></i>
            Tầng 17, Tòa nhà Xanh, Cầu Giấy, Hà Nội
          </p>
        </div>
      </div>

      <div class="tf-footer-bottom">
        <span>© 2026 TrendFit Studio. All rights reserved.</span>
        <span class="tf-footer-tech">Built with Vue · Fashion × Tech</span>
      </div>
    </div>
  </footer>
</template>

<script setup>
</script>

<style scoped>
.tf-footer {
  background: #0a0c14;
  color: rgba(255, 255, 255, 0.7);
  padding: 64px 0 0;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  margin-top: 80px;
}

.tf-footer-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr 1fr;
  gap: 40px;
  padding-bottom: 48px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.tf-footer-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.tf-logo-mark {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #6366f1, #8b5cf6 50%, #06b6d4);
  color: #fff;
  font-family: 'Space Grotesk', sans-serif;
  font-weight: 700;
  font-size: 14px;
  border-radius: 10px;
}

.tf-logo-text {
  font-family: 'Space Grotesk', sans-serif;
  font-weight: 700;
  font-size: 1.1rem;
  letter-spacing: 0.12em;
  color: #fff;
}

.tf-footer-desc {
  font-size: 14px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.55);
  max-width: 280px;
  margin-bottom: 20px;
}

.tf-social {
  display: flex;
  gap: 8px;
}

.tf-social-btn {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.7);
  font-size: 18px;
  transition: all 0.2s;
  text-decoration: none;
}

.tf-social-btn:hover {
  background: rgba(99, 102, 241, 0.3);
  color: #fff;
  border-color: rgba(99, 102, 241, 0.5);
}

.tf-footer-title {
  font-family: 'Space Grotesk', sans-serif;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #fff;
  margin-bottom: 18px;
}

.tf-footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tf-footer-links a {
  color: rgba(255, 255, 255, 0.55);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.2s;
}

.tf-footer-links a:hover {
  color: #a5b4fc;
}

.tf-pay-icons {
  display: flex;
  gap: 12px;
  font-size: 28px;
  color: rgba(255, 255, 255, 0.45);
}

.tf-footer-note {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);
  margin: 0;
}

.tf-footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
}

.tf-footer-tech {
  font-family: 'Space Grotesk', sans-serif;
  font-weight: 500;
  letter-spacing: 0.04em;
  color: rgba(165, 180, 252, 0.6);
}

@media (max-width: 991.98px) {
  .tf-footer-grid {
    grid-template-columns: 1fr 1fr;
    gap: 32px;
  }
}

@media (max-width: 575.98px) {
  .tf-footer-grid {
    grid-template-columns: 1fr;
  }
  .tf-footer-bottom {
    flex-direction: column;
    gap: 8px;
    text-align: center;
  }
}

.tf-footer {
  animation: tfFooterIn 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
}
@keyframes tfFooterIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
.tf-social-btn {
  transition: all 0.25s cubic-bezier(0.22, 1, 0.36, 1);
}
.tf-social-btn:hover {
  transform: translateY(-3px);
}
.tf-footer-links a {
  transition: color 0.2s ease, padding-left 0.25s ease;
}
.tf-footer-links a:hover {
  padding-left: 4px;
}

</style>
