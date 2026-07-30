package com.trendfit.api.config;

import com.trendfit.api.modules.product.entity.DanhMuc;
import com.trendfit.api.modules.product.entity.ThuongHieu;
import com.trendfit.api.modules.product.repository.DanhMucRepository;
import com.trendfit.api.modules.product.repository.ThuongHieuRepository;
import com.trendfit.api.modules.user.entity.NguoiDung;
import com.trendfit.api.modules.user.repository.NguoiDungRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final DanhMucRepository danhMucRepository;
    private final ThuongHieuRepository thuongHieuRepository;
    private final NguoiDungRepository nguoiDungRepository; // Thêm repository người dùng

    public DataInitializer(DanhMucRepository danhMucRepository, 
                           ThuongHieuRepository thuongHieuRepository,
                           NguoiDungRepository nguoiDungRepository) {
        this.danhMucRepository = danhMucRepository;
        this.thuongHieuRepository = thuongHieuRepository;
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @Override
    public void run(String... args) {
        // 1. Seed Danh Mục
        if (danhMucRepository.count() == 0) {
            seedDanhMuc("Áo sơ mi", "ao-so-mi", 1);
            seedDanhMuc("Áo thun", "ao-thun", 2);
            seedDanhMuc("Phụ kiện", "phu-kien", 3);
        }

        // 2. Seed Thương Hiệu
        if (thuongHieuRepository.count() == 0) {
            seedThuongHieu("Owen", "Việt Nam");
            seedThuongHieu("TrendFit", "Việt Nam");
        }

        // 3. Seed Tài khoản Admin mặc định (Phục vụ đăng nhập & test giỏ hàng)
        if (nguoiDungRepository.findByEmail("admin") == null) {
            NguoiDung admin = new NguoiDung();
            admin.setEmail("admin");
            admin.setHoTen("Quản Trị Viên");
            admin.setMatKhau("123");
            admin.setVaiTro("ADMIN");
            admin.setDangHoatDong(true);
            nguoiDungRepository.save(admin);
            System.out.println(">>> Đã tự động tạo tài khoản Admin mặc định thành công!");
        }
    }

    private void seedDanhMuc(String ten, String slug, int thuTu) {
        DanhMuc dm = new DanhMuc();
        dm.setTen(ten);
        dm.setSlug(slug);
        dm.setThuTu(thuTu);
        dm.setDangHoatDong(true);
        danhMucRepository.save(dm);
    }

    private void seedThuongHieu(String ten, String quocGia) {
        ThuongHieu th = new ThuongHieu();
        th.setTen(ten);
        th.setQuocGia(quocGia);
        th.setDangHoatDong(true);
        thuongHieuRepository.save(th);
    }
}