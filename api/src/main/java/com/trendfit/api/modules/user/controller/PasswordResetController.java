package com.trendfit.api.modules.user.controller;

import com.trendfit.api.modules.user.entity.NguoiDung;
import com.trendfit.api.modules.user.repository.NguoiDungRepository;
import com.trendfit.api.modules.user.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * ============================================================================
 *  CHỨC NĂNG "QUÊN MẬT KHẨU" — dùng chung cho cả 3 loại tài khoản
 *  (Khách hàng / Nhân viên / Admin thật trong DB — riêng tài khoản demo
 *  "admin/123" hard-code ở AuthenController thì không áp dụng được, vì nó
 *  không tồn tại trong bảng nguoi_dung).
 * ============================================================================
 * LUỒNG HOẠT ĐỘNG (kiểu OTP qua email, không dùng link vì đơn giản hơn để demo):
 *   Bước 1) FE gọi POST /api/auth/forgot-password  { email }
 *           -> Backend sinh 1 mã ngẫu nhiên 6 chữ số, lưu vào
 *              NguoiDung.resetCode + resetCodeHetHan (hết hạn sau 5 phút),
 *              rồi gửi mã qua email (EmailService.sendVerificationCode).
 *   Bước 2) Người dùng mở email, lấy mã, quay lại FE nhập:
 *           email + mã 6 số + mật khẩu mới
 *           -> FE gọi POST /api/auth/reset-password { email, code, matKhauMoi }
 *   Backend kiểm tra mã đúng & còn hạn thì cập nhật mật khẩu mới, đồng thời
 *   xóa resetCode/resetCodeHetHan để mã không dùng lại được lần 2.
 *
 * LƯU Ý CẤU HÌNH: để gửi được email thật, phải điền đúng thông tin Gmail ở
 * "application.properties" (spring.mail.username / spring.mail.password -
 * dùng App Password của Gmail, KHÔNG phải mật khẩu đăng nhập Gmail thường).
 * Nếu chưa cấu hình, gọi API sẽ báo lỗi 500 khi gửi mail thất bại.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class PasswordResetController {

    @Autowired private NguoiDungRepository nguoiDungRepository;
    @Autowired private EmailService emailService;

    private static final int SO_PHUT_HET_HAN = 5;

    /**
     * Bước 1: Người dùng nhập email trên trang "Quên mật khẩu", hệ thống gửi
     * mã xác thực 6 số về email đó.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "").trim();
        if (email.isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập email!");
        }

        NguoiDung user = nguoiDungRepository.findByEmail(email);
        if (user == null) {
            // Không tiết lộ chi tiết "email không tồn tại" trong hệ thống thật
            // (tránh lộ thông tin cho kẻ dò email), nhưng ở đây trả lỗi rõ ràng
            // để tiện demo/chấm điểm đồ án.
            return ResponseEntity.status(404).body("Email này chưa được đăng ký trong hệ thống!");
        }

        String code = sinhMaNgauNhien6So();
        user.setResetCode(code);
        user.setResetCodeHetHan(LocalDateTime.now().plusMinutes(SO_PHUT_HET_HAN));
        nguoiDungRepository.save(user);

        try {
            emailService.sendVerificationCode(user.getEmail(), code, user.getHoTen());
        } catch (Exception e) {
            // Gửi mail thất bại thường do chưa cấu hình đúng Gmail/App Password
            // trong application.properties - trả lỗi rõ ràng thay vì 500 mù mờ.
            return ResponseEntity.status(500).body(
                    "Không gửi được email. Vui lòng kiểm tra lại cấu hình email của hệ thống (application.properties)."
            );
        }

        return ResponseEntity.ok("Đã gửi mã xác thực tới email của bạn. Mã có hiệu lực trong " + SO_PHUT_HET_HAN + " phút.");
    }

    /**
     * Bước 2: Người dùng nhập mã vừa nhận + mật khẩu mới để hoàn tất đặt lại.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "").trim();
        String code = body.getOrDefault("code", "").trim();
        String matKhauMoi = body.getOrDefault("matKhauMoi", "");

        if (email.isEmpty() || code.isEmpty() || matKhauMoi.isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng nhập đầy đủ thông tin!");
        }
        if (matKhauMoi.length() < 4) {
            return ResponseEntity.badRequest().body("Mật khẩu mới phải có ít nhất 4 ký tự!");
        }

        NguoiDung user = nguoiDungRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(404).body("Email không tồn tại!");
        }

        if (user.getResetCode() == null || !user.getResetCode().equals(code)) {
            return ResponseEntity.status(400).body("Mã xác thực không đúng!");
        }

        if (user.getResetCodeHetHan() == null || user.getResetCodeHetHan().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(400).body("Mã xác thực đã hết hạn, vui lòng yêu cầu gửi lại mã mới!");
        }

        // TODO (bảo mật): giống các chỗ khác trong hệ thống, mật khẩu hiện lưu
        // dạng plain text để đồng bộ với cơ chế đăng nhập hiện có (AuthenController.login
        // đang so sánh bằng equals()). Khi nâng cấp bảo mật, băm bằng BCrypt ở
        // đây và sửa lại toàn bộ nơi tạo tài khoản + đăng nhập cho khớp.
        user.setMatKhau(matKhauMoi);
        user.setResetCode(null);
        user.setResetCodeHetHan(null);
        nguoiDungRepository.save(user);

        return ResponseEntity.ok("Đặt lại mật khẩu thành công! Vui lòng đăng nhập bằng mật khẩu mới.");
    }

    // Sinh ngẫu nhiên 1 chuỗi 6 chữ số (000000 - 999999), dùng SecureRandom
    // thay vì Math.random() để khó đoán trước hơn.
    private String sinhMaNgauNhien6So() {
        SecureRandom random = new SecureRandom();
        int soNgauNhien = random.nextInt(1_000_000); // 0 -> 999999
        return String.format("%06d", soNgauNhien);
    }
}
