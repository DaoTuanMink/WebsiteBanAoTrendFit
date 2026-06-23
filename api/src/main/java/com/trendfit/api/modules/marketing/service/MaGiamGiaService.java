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
        if (LocalDate.now().isBefore(mg.getNgayBatDau()) || LocalDate.now().isAfter(mg.getNgayKetThuc())) 
            throw new RuntimeException("Mã đã hết hạn!");
        if (tongDon.compareTo(mg.getDonHangToiThieu()) < 0) 
            throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu!");
        if (mg.getSoLanDaDung() >= mg.getGioiHanSuDung()) 
            throw new RuntimeException("Mã đã hết lượt sử dụng!");
            
        return mg;
    }
}