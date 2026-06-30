package com.trendfit.controller;

import com.trendfit.model.Order;
import com.trendfit.service.OrderService;
import com.trendfit.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
public class OrderAdminController {
    @Autowired private OrderService orderService;
    @Autowired private ProductService productService;

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody String status) {
        Order order = orderService.findById(id);
        // Logic tồn kho: Hủy đơn thì cộng lại, Thành công thì trừ (nếu chưa trừ)
        if ("DA_HUY".equals(status)) { productService.restoreStock(order); }
        order.setStatus(status);
        orderService.save(order);
        return ResponseEntity.ok("OK");
    }
}
