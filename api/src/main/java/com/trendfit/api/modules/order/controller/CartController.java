package com.trendfit.api.modules.order.controller;

import com.trendfit.api.modules.order.dto.CartSyncDTO;
import com.trendfit.api.modules.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/cart")
@CrossOrigin("*")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getCart(@PathVariable Integer userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncCart(@RequestBody CartSyncDTO dto) {
        try {
            cartService.syncCart(dto);
            return ResponseEntity.ok("Đồng bộ giỏ hàng thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}