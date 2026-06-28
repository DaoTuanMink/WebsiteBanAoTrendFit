package com.trendfit.api.modules.user.repository;

import com.trendfit.api.modules.user.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {
<<<<<<< HEAD
    // JpaRepository đã có sẵn findById, save, v.v.
    NguoiDung findByEmail(String email);
=======
>>>>>>> 29a34e0a08660a46b8f5001113a666cdf02e1ca0
}