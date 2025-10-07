package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.CartItem;
import com.example.agricultural_product.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

	@Autowired
	private CartService cartService;

	@PostMapping("/add")
	public ResponseEntity<Boolean> addToCart(@RequestParam Long userId,
	                                        @RequestParam Integer productId,
	                                        @RequestParam(defaultValue = "1") Integer quantity) {
		return ResponseEntity.ok(cartService.addToCart(userId, productId, quantity));
	}

	@PostMapping("/update")
	public ResponseEntity<Boolean> updateQuantity(@RequestParam Long userId,
	                                             @RequestParam Integer productId,
	                                             @RequestParam Integer quantity) {
		return ResponseEntity.ok(cartService.updateQuantity(userId, productId, quantity));
	}

	@DeleteMapping("/item")
	public ResponseEntity<Boolean> removeItem(@RequestParam Long userId,
	                                         @RequestParam Integer productId) {
		return ResponseEntity.ok(cartService.removeItem(userId, productId));
	}

	@DeleteMapping("/clear")
	public ResponseEntity<Boolean> clear(@RequestParam Long userId) {
		return ResponseEntity.ok(cartService.clearCart(userId));
	}

	@PostMapping("/checkout")
	public ResponseEntity<Integer> checkout(@RequestParam Long userId,
	                                       @RequestParam Integer addressId,
	                                       @RequestParam List<Integer> productIds) {
		return ResponseEntity.ok(cartService.checkout(userId, addressId, productIds));
	}

	@GetMapping
	public ResponseEntity<Page<CartItem>> list(@RequestParam Long userId,
	                                          @RequestParam(defaultValue = "1") Integer pageNum,
	                                          @RequestParam(defaultValue = "10") Integer pageSize) {
		return ResponseEntity.ok(cartService.listCartItems(userId, pageNum, pageSize));
	}
}
