package com.trendfit.api.modules.marketing.service;

import com.trendfit.api.modules.marketing.entity.MaGiamGia;
import com.trendfit.api.modules.marketing.repository.MaGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class MaGiamGiaService {
    @Autowired private MaGiamGiaRepository repo;

    public List<MaGiamGia> getAll() { return repo.findAll(); }

    @Transactional
    public MaGiamGia save(MaGiamGia mg) { return repo.save(mg); }

    public void delete(Integer id) { repo.deleteById(id); }

    public MaGiamGia kiemTraVoucher(String ma, BigDecimal tongDon) {
    MaGiamGia mg = repo.findByMa(ma)
        .orElseThrow(() -> new RuntimeException("Mã không tồn tại!"));
        
    if (!mg.getDangHoatDong()) throw new RuntimeException("Mã đã bị khóa!");
    
    // Kiểm tra ngày tháng an toàn (nếu ngày bắt đầu/kết thúc có thể null)
    if (mg.getNgayBatDau() != null && LocalDate.now().isBefore(mg.getNgayBatDau())) 
        throw new RuntimeException("Mã chưa đến ngày sử dụng!");
    if (mg.getNgayKetThuc() != null && LocalDate.now().isAfter(mg.getNgayKetThuc())) 
        throw new RuntimeException("Mã đã hết hạn!");

    // CỐ ĐỊNH LỖI Ở ĐÂY: Kiểm tra null trước khi so sánh
    BigDecimal toiThieu = mg.getDonHangToiThieu() != null ? mg.getDonHangToiThieu() : BigDecimal.ZERO;
    if (tongDon.compareTo(toiThieu) < 0) 
        throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu là " + toiThieu + "đ!");

    if (mg.getSoLanDaDung() != null && mg.getSoLanDaDung() >= mg.getGioiHanSuDung()) 
        throw new RuntimeException("Mã đã hết lượt sử dụng!");
        
    return mg;
}

    public BigDecimal tinhSoTienGiam(MaGiamGia voucher, BigDecimal tongDonHang) {
    if ("PERCENT".equals(voucher.getLoai())) {
        BigDecimal giam = tongDonHang.multiply(voucher.getGiaTriGiam().divide(new BigDecimal(100)));
        // Nếu có giới hạn giá trị giảm tối đa (giaTriToiDa)
        if (voucher.getGiaTriToiDa() != null && giam.compareTo(voucher.getGiaTriToiDa()) > 0) {
            return voucher.getGiaTriToiDa();
        }
        return giam;
    } else {
        // Trường hợp FIXED (Số tiền cố định)
        return voucher.getGiaTriGiam();
    }
}
}