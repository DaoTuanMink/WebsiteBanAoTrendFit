package com.trendfit.api.modules.interaction.service;

import com.trendfit.api.modules.interaction.dto.DanhGiaRequestDTO;
import com.trendfit.api.modules.interaction.dto.DanhGiaResponseDTO;
import com.trendfit.api.modules.interaction.entity.DanhGia;
import com.trendfit.api.modules.interaction.repository.DanhGiaRepository;
import com.trendfit.api.modules.order.entity.ChiTietDonHang;
import com.trendfit.api.modules.order.repository.ChiTietDonHangRepository;
import com.trendfit.api.modules.product.entity.SanPham;
import com.trendfit.api.modules.product.repository.SanPhamRepository;
import com.trendfit.api.modules.user.entity.NguoiDung;
import com.trendfit.api.modules.user.repository.NguoiDungRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DanhGiaService {

    private final DanhGiaRepository danhGiaRepository;
    private final SanPhamRepository sanPhamRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;

    public DanhGiaService(
            DanhGiaRepository danhGiaRepository,
            SanPhamRepository sanPhamRepository,
            NguoiDungRepository nguoiDungRepository,
            ChiTietDonHangRepository chiTietDonHangRepository
    ) {
        this.danhGiaRepository = danhGiaRepository;
        this.sanPhamRepository = sanPhamRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.chiTietDonHangRepository = chiTietDonHangRepository;
    }

    public List<DanhGiaResponseDTO> layDanhGiaTheoSanPham(Integer sanPhamId) {
    return danhGiaRepository
            .findDanhGiaHienThiTheoSanPham(sanPhamId)
            .stream()
            .map(this::toDTO)
            .toList();
}

    public boolean coTheDanhGia(Integer nguoiDungId, Integer sanPhamId) {
        List<ChiTietDonHang> daMua = chiTietDonHangRepository.findChiTietDaMuaSanPham(
                nguoiDungId,
                sanPhamId
        );

        boolean daDanhGia = danhGiaRepository.existsByNguoiDung_IdAndSanPham_Id(
                nguoiDungId,
                sanPhamId
        );

        return !daMua.isEmpty() && !daDanhGia;
    }

    @Transactional
    public DanhGiaResponseDTO taoDanhGia(DanhGiaRequestDTO request) {
        if (request.getNguoiDungId() == null) {
            throw new RuntimeException("Thiếu người dùng");
        }

        if (request.getSanPhamId() == null) {
            throw new RuntimeException("Thiếu sản phẩm");
        }

        if (request.getSaoXepHang() == null || request.getSaoXepHang() < 1 || request.getSaoXepHang() > 5) {
            throw new RuntimeException("Số sao phải từ 1 đến 5");
        }

        SanPham sanPham = sanPhamRepository.findById(request.getSanPhamId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        NguoiDung nguoiDung = nguoiDungRepository.findById(request.getNguoiDungId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        List<ChiTietDonHang> chiTietDaMua = chiTietDonHangRepository.findChiTietDaMuaSanPham(
                request.getNguoiDungId(),
                request.getSanPhamId()
        );

        // if (chiTietDaMua.isEmpty()) {
        //     throw new RuntimeException("Bạn chỉ có thể đánh giá sản phẩm sau khi mua hàng thành công");
        // }

        // boolean daDanhGia = danhGiaRepository.existsByNguoiDung_IdAndSanPham_Id(
        //         request.getNguoiDungId(),
        //         request.getSanPhamId()
        // );

        // if (daDanhGia) {
        //     throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi");
        // }

        DanhGia danhGia = new DanhGia();
        danhGia.setNguoiDung(nguoiDung);
        danhGia.setSanPham(sanPham);
        // danhGia.setChiTietDonHang(chiTietDaMua.get(0));
        if (!chiTietDaMua.isEmpty()) {
    danhGia.setChiTietDonHang(chiTietDaMua.get(0));
}
        danhGia.setSaoXepHang(request.getSaoXepHang());
        danhGia.setTieuDe(request.getTieuDe());
        danhGia.setNoiDung(request.getNoiDung());

        // Cho hiển thị ngay sau khi khách đánh giá.
        // Nếu sau này muốn admin duyệt thì đổi thành false.
        danhGia.setDaDuyet(true);
        danhGia.setLuotThich(0);

        DanhGia saved = danhGiaRepository.save(danhGia);

        capNhatThongKeDanhGia(sanPham.getId());

        return toDTO(saved);
    }

    private void capNhatThongKeDanhGia(Integer sanPhamId) {
        SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        Double diemTrungBinh = danhGiaRepository.tinhDiemTrungBinh(sanPhamId);
        long tongLuot = danhGiaRepository.countBySanPham_IdAndDaDuyetTrue(sanPhamId);

        sanPham.setDanhGiaTrungBinh(diemTrungBinh.floatValue());
        sanPham.setTongLuotDanhGia((int) tongLuot);

        sanPhamRepository.save(sanPham);
    }

    private DanhGiaResponseDTO toDTO(DanhGia danhGia) {
        String tenNguoiDung = "Khách hàng";

        if (danhGia.getNguoiDung() != null && danhGia.getNguoiDung().getHoTen() != null) {
            tenNguoiDung = danhGia.getNguoiDung().getHoTen();
        }

        Integer nguoiDungId = danhGia.getNguoiDung() != null ? danhGia.getNguoiDung().getId() : null;
        Integer sanPhamId = danhGia.getSanPham() != null ? danhGia.getSanPham().getId() : null;

        return new DanhGiaResponseDTO(
                danhGia.getId(),
                nguoiDungId,
                tenNguoiDung,
                sanPhamId,
                danhGia.getSaoXepHang(),
                danhGia.getTieuDe(),
                danhGia.getNoiDung(),
                danhGia.getDaDuyet(),
                danhGia.getLuotThich(),
                danhGia.getNgayTao()
        );
    }
}