package com.trendfit.api.modules.marketing.controller;

import com.trendfit.api.modules.marketing.entity.MaGiamGia;
import com.trendfit.api.modules.marketing.service.MaGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vouchers")
@CrossOrigin
public class VoucherAdminController {
    @Autowired private MaGiamGiaService service;

    @GetMapping
    public List<MaGiamGia> getAll() { return service.getAll(); }

    @PostMapping
    public MaGiamGia create(@RequestBody MaGiamGia mg) { return service.save(mg); }

    @PutMapping("/{id}")
    public MaGiamGia update(@PathVariable Integer id, @RequestBody MaGiamGia mg) {
        mg.setId(id);
        return service.save(mg);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) { service.delete(id); }
}