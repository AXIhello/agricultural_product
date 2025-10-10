package com.example.agricultural_product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.agricultural_product.pojo.Cart;
import com.example.agricultural_product.pojo.CartItem;

// import java.util.List; // 不再需要

public interface CartService extends IService<Cart> {
	boolean addToCart(Long userId, Integer productId, Integer quantity);

	Page<CartItem> listCartItems(Long userId, Integer pageNum, Integer pageSize);

	boolean updateQuantity(Long userId, Integer productId, Integer quantity);

	boolean removeItem(Long userId, Integer productId);

	boolean clearCart(Long userId);

    // 购物车不再提供结算功能
}
