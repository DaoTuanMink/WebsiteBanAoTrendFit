package com.trendfit.api.modules.user.repository;

import com.trendfit.api.modules.user.entity.PhanQuyen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhanQuyenRepository extends JpaRepository<PhanQuyen, Integer> {
    void deleteByNhanVienId(Integer nhanVienId);
    
    // Kiểm tra quyền sửa
    boolean existsByNhanVienIdAndModuleAndSuaDuocTrue(Integer nhanVienId, String module);
    
    // Kiểm tra quyền thêm
    boolean existsByNhanVienIdAndModuleAndThemDuocTrue(Integer nhanVienId, String module);
    
    // Kiểm tra quyền xóa (nếu cần)
    boolean existsByNhanVienIdAndModuleAndXoaDuocTrue(Integer nhanVienId, String module);
    
    // Kiểm tra quyền xem
    boolean existsByNhanVienIdAndModuleAndDocDuocTrue(Integer nhanVienId, String module);
}