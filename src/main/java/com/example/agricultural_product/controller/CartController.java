package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.CartItem;
import com.example.agricultural_product.service.CartService;
import com.example.agricultural_product.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

	@Autowired
	private CartService cartService;

	// 从 JWT token 获取 userId
	private Long getUserIdFromToken(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			return JwtUtil.getUserId(token);
		}
		throw new RuntimeException("未提供有效的 JWT token");
	}

	@PostMapping("/add")
	public ResponseEntity<Boolean> addToCart(HttpServletRequest request,
											 @RequestParam Integer productId,
											 @RequestParam(defaultValue = "1") Integer quantity) {
		Long userId = getUserIdFromToken(request);
		return ResponseEntity.ok(cartService.addToCart(userId, productId, quantity));
	}

	@PostMapping("/update")
	public ResponseEntity<Boolean> updateQuantity(HttpServletRequest request,
												  @RequestParam Integer productId,
												  @RequestParam Integer quantity) {
		Long userId = getUserIdFromToken(request);
		return ResponseEntity.ok(cartService.updateQuantity(userId, productId, quantity));
	}

	@DeleteMapping("/item")
	public ResponseEntity<Boolean> removeItem(HttpServletRequest request,
											  @RequestParam Integer productId) {
		Long userId = getUserIdFromToken(request);
		return ResponseEntity.ok(cartService.removeItem(userId, productId));
	}

	@DeleteMapping("/clear")
	public ResponseEntity<Boolean> clear(HttpServletRequest request) {
		Long userId = getUserIdFromToken(request);
		return ResponseEntity.ok(cartService.clearCart(userId));
	}

	@PostMapping("/checkout")
	public ResponseEntity<Integer> checkout(HttpServletRequest request,
											@RequestParam Integer addressId,
											@RequestParam List<Integer> productIds) {
		Long userId = getUserIdFromToken(request);
		return ResponseEntity.ok(cartService.checkout(userId, addressId, productIds));
	}

	@GetMapping
	public ResponseEntity<Page<CartItem>> list(HttpServletRequest request,
											   @RequestParam(defaultValue = "1") Integer pageNum,
											   @RequestParam(defaultValue = "10") Integer pageSize) {
		Long userId = getUserIdFromToken(request);
		return ResponseEntity.ok(cartService.listCartItems(userId, pageNum, pageSize));
	}
}
