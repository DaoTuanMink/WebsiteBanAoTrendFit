package com.trendfit.api.modules.order.controller;

import com.trendfit.api.modules.order.dto.OrderRequestDTO;
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

}