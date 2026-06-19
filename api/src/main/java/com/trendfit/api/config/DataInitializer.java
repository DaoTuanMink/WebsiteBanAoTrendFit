package com.trendfit.api.config;

import com.trendfit.api.modules.product.entity.DanhMuc;
import com.trendfit.api.modules.product.entity.ThuongHieu;
import com.trendfit.api.modules.product.repository.DanhMucRepository;
import com.trendfit.api.modules.product.repository.ThuongHieuRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final DanhMucRepository danhMucRepository;
    private final ThuongHieuRepository thuongHieuRepository;

    public DataInitializer(DanhMucRepository danhMucRepository, ThuongHieuRepository thuongHieuRepository) {
        this.danhMucRepository = danhMucRepository;
        this.thuongHieuRepository = thuongHieuRepository;
    }

    @Override
    public void run(String... args) {
        if (danhMucRepository.count() == 0) {
            seedDanhMuc("Áo sơ mi", "ao-so-mi", 1);
            seedDanhMuc("Áo thun", "ao-thun", 2);
            seedDanhMuc("Phụ kiện", "phu-kien", 3);
        }

        if (thuongHieuRepository.count() == 0) {
            seedThuongHieu("Owen", "Việt Nam");
            seedThuongHieu("TrendFit", "Việt Nam");
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
