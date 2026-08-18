package com.trendfit.api.modules.user.service;

import com.trendfit.api.modules.user.dto.UserProfileDTO;
import com.trendfit.api.modules.user.entity.DiaChi;
import com.trendfit.api.modules.user.entity.NguoiDung;
import com.trendfit.api.modules.user.repository.DiaChiRepository;
import com.trendfit.api.modules.user.repository.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserProfileService {

    @Autowired private NguoiDungRepository nguoiDungRepository;
    @Autowired private DiaChiRepository diaChiRepository;

    public UserProfileDTO getProfile(Integer userId) {
        NguoiDung user = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setHoTen(user.getHoTen());
        dto.setEmail(user.getEmail());
        dto.setSoDienThoai(user.getSoDienThoai());
        dto.setAnhDaiDien(user.getAnhDaiDien());

        List<DiaChi> listDc = diaChiRepository.findByNguoiDungId(userId);
        List<UserProfileDTO.DiaChiDTO> listDcDto = listDc.stream().map(dc -> {
            UserProfileDTO.DiaChiDTO d = new UserProfileDTO.DiaChiDTO();
            d.setId(dc.getId());
            d.setTenNguoiNhan(dc.getTenNguoiNhan());
            d.setSoDienThoai(dc.getSoDienThoai());
            d.setTinhThanh(dc.getTinhThanh());
            d.setXaPhuong(dc.getPhuongXa());
            d.setDuong(dc.getDuong());
            d.setLaMacDinh(dc.getLaMacDinh());
            return d;
        }).toList();

        dto.setDanhSachDiaChi(listDcDto);
        return dto;
    }

    @Transactional
    public void updateProfile(Integer userId, UserProfileDTO dto) {
        NguoiDung user = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));
        user.setHoTen(dto.getHoTen());
        user.setSoDienThoai(dto.getSoDienThoai());
        user.setAnhDaiDien(dto.getAnhDaiDien());
        nguoiDungRepository.save(user);
    }

    @Transactional
    public void saveOrUpdateAddress(Integer userId, UserProfileDTO.DiaChiDTO dcDto) {
        NguoiDung user = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

        DiaChi dc;
        if (dcDto.getId() != null) {
            dc = diaChiRepository.findById(dcDto.getId()).orElse(new DiaChi());
            dc.setNguoiDung(user);
        } else {
            dc = new DiaChi();
            dc.setNguoiDung(user);
        }

        dc.setTenNguoiNhan(dcDto.getTenNguoiNhan());
        dc.setSoDienThoai(dcDto.getSoDienThoai());
        dc.setTinhThanh(dcDto.getTinhThanh());
        dc.setPhuongXa(dcDto.getXaPhuong()); // Gán rõ ràng giá trị xã/phường
        dc.setDuong(dcDto.getDuong());
        dc.setLaMacDinh(dcDto.getLaMacDinh() != null ? dcDto.getLaMacDinh() : false);

        // Nếu chọn là địa chỉ mặc định thì các địa chỉ khác chuyển về false
        if (Boolean.TRUE.equals(dc.getLaMacDinh())) {
            List<DiaChi> all = diaChiRepository.findByNguoiDungId(userId);
            for (DiaChi item : all) {
                if (item.getId() != null && !item.getId().equals(dc.getId())) {
                    item.setLaMacDinh(false);
                    diaChiRepository.save(item);
                }
            }
        }

        diaChiRepository.save(dc);
    }

    @Transactional
    public void deleteAddress(Integer addressId) {
        diaChiRepository.deleteById(addressId);
    }
}