package com.trendfit.api.modules.order.controller;

import com.trendfit.api.modules.order.dto.OrderRequestDTO;
import com.trendfit.api.modules.order.dto.ReturnRequestDTO;
import com.trendfit.api.modules.order.entity.DonHang;
import com.trendfit.api.modules.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/orders")
@CrossOrigin("*")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

   @PostMapping
public ResponseEntity<?> placeOrder(@RequestBody OrderRequestDTO dto) {
    // Thêm log này ở Controller để xem từ bước đầu tiên Backend có nhận được gì không
    System.out.println("Controller nhận được userId: " + dto.getUserId());
    orderService.taoDonHang(dto);
    return ResponseEntity.ok("Đặt hàng thành công!");
}

@GetMapping("/user/{userId}")
public ResponseEntity<?> getOrdersByUser(@PathVariable Integer userId) {
    return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
}

@PutMapping("/{id}/status")
public ResponseEntity<?> updateOrderStatus(
        @PathVariable Integer id,
        @RequestParam String status) {

    try {
        orderService.capNhatTrangThaiDonHang(id, status);
        return ResponseEntity.ok("Cập nhật trạng thái thành công!");
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

@PutMapping("/{id}/request-return")
public ResponseEntity<?> requestReturn(
        @PathVariable Integer id,
        @RequestBody ReturnRequestDTO dto) {
    try {
        orderService.yeuCauTraHang(id, dto.getLyDo(), dto.getAnhMinhChung());
        return ResponseEntity.ok("Gửi yêu cầu trả hàng thành công!");
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

@GetMapping("/{id}/return-details")
public ResponseEntity<?> getReturnDetails(@PathVariable Integer id) {
    // Gọi thông qua orderService thay vì gọi trực tiếp repository
    Object chiTiet = orderService.getChiTietHoanTra(id);
    if (chiTiet != null) {
        return ResponseEntity.ok(chiTiet);
    }
    return ResponseEntity.notFound().build();
}

@GetMapping("/admin/orders/{id}/return-details")
public ResponseEntity<?> getAdminReturnDetails(@PathVariable Integer id) {
    Object chiTiet = orderService.getChiTietHoanTra(id);
    if (chiTiet != null) {
        return ResponseEntity.ok(chiTiet);
    }
    return ResponseEntity.notFound().build();
}
}