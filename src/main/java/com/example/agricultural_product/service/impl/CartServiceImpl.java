package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agricultural_product.mapper.CartItemMapper;
import com.example.agricultural_product.mapper.CartMapper;
import com.example.agricultural_product.mapper.OrderItemMapper;
import com.example.agricultural_product.mapper.OrderMapper;
import com.example.agricultural_product.mapper.ProductMapper;
import com.example.agricultural_product.pojo.Cart;
import com.example.agricultural_product.pojo.CartItem;
import com.example.agricultural_product.pojo.Order;
import com.example.agricultural_product.pojo.OrderItem;
import com.example.agricultural_product.pojo.Product;
import com.example.agricultural_product.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

	@Autowired
	private CartMapper cartMapper;

	@Autowired
	private CartItemMapper cartItemMapper;

	@Autowired
	private ProductMapper productMapper;

	@Autowired
	private OrderMapper orderMapper;

	@Autowired
	private OrderItemMapper orderItemMapper;

	@Override
	public boolean addToCart(Long userId, Integer productId, Integer quantity) {
		if (userId == null || productId == null || quantity == null || quantity <= 0) {
			return false;
		}
		Product product = productMapper.selectById(productId);
		if (product == null || product.getStatus() == null || !"active".equalsIgnoreCase(product.getStatus())) {
			return false;
		}
		LambdaQueryWrapper<Cart> cartQ = new LambdaQueryWrapper<>();
		cartQ.eq(Cart::getUserId, userId);
		Cart cart = this.getOne(cartQ);
		LocalDateTime now = LocalDateTime.now();
		if (cart == null) {
			cart = new Cart();
			cart.setUserId(userId);
			cart.setCreateTime(now);
			cart.setUpdateTime(now);
			cartMapper.insert(cart);
		}
		LambdaQueryWrapper<CartItem> itemQ = new LambdaQueryWrapper<>();
		itemQ.eq(CartItem::getCartId, cart.getCartId()).eq(CartItem::getProductId, productId);
		CartItem item = cartItemMapper.selectOne(itemQ);
		if (item == null) {
			item = new CartItem();
			item.setCartId(cart.getCartId());
			item.setProductId(productId);
			item.setQuantity(quantity);
			item.setPriceAtAdd(product.getPrice() == null ? BigDecimal.ZERO : product.getPrice());
			item.setAddTime(now);
			cartItemMapper.insert(item);
		} else {
			item.setQuantity(item.getQuantity() + quantity);
			cartItemMapper.updateById(item);
		}
		cart.setUpdateTime(now);
		cartMapper.updateById(cart);
		return true;
	}

	@Override
	public Page<CartItem> listCartItems(Long userId, Integer pageNum, Integer pageSize) {
		if (userId == null) {
			return new Page<>(pageNum, pageSize);
		}
		LambdaQueryWrapper<Cart> cartQ = new LambdaQueryWrapper<>();
		cartQ.eq(Cart::getUserId, userId);
		Cart cart = this.getOne(cartQ);
		Page<CartItem> page = new Page<>(pageNum, pageSize);
		if (cart == null) {
			return page;
		}
		LambdaQueryWrapper<CartItem> itemQ = new LambdaQueryWrapper<>();
		itemQ.eq(CartItem::getCartId, cart.getCartId()).orderByDesc(CartItem::getAddTime);
		page.setRecords(cartItemMapper.selectList(itemQ));
		return page;
	}

	@Override
	public boolean updateQuantity(Long userId, Integer productId, Integer quantity) {
		if (userId == null || productId == null || quantity == null || quantity <= 0) {
			return false;
		}
		LambdaQueryWrapper<Cart> cartQ = new LambdaQueryWrapper<>();
		cartQ.eq(Cart::getUserId, userId);
		Cart cart = this.getOne(cartQ);
		if (cart == null) {
			return false;
		}
		LambdaQueryWrapper<CartItem> itemQ = new LambdaQueryWrapper<>();
		itemQ.eq(CartItem::getCartId, cart.getCartId()).eq(CartItem::getProductId, productId);
		CartItem item = cartItemMapper.selectOne(itemQ);
		if (item == null) {
			return false;
		}
		item.setQuantity(quantity);
		cartItemMapper.updateById(item);
		cart.setUpdateTime(LocalDateTime.now());
		cartMapper.updateById(cart);
		return true;
	}

	@Override
	public boolean removeItem(Long userId, Integer productId) {
		if (userId == null || productId == null) {
			return false;
		}
		LambdaQueryWrapper<Cart> cartQ = new LambdaQueryWrapper<>();
		cartQ.eq(Cart::getUserId, userId);
		Cart cart = this.getOne(cartQ);
		if (cart == null) {
			return false;
		}
		LambdaQueryWrapper<CartItem> itemQ = new LambdaQueryWrapper<>();
		itemQ.eq(CartItem::getCartId, cart.getCartId()).eq(CartItem::getProductId, productId);
		int deleted = cartItemMapper.delete(itemQ);
		if (deleted > 0) {
			cart.setUpdateTime(LocalDateTime.now());
			cartMapper.updateById(cart);
		}
		return deleted > 0;
	}

	@Override
	public boolean clearCart(Long userId) {
		if (userId == null) {
			return false;
		}
		LambdaQueryWrapper<Cart> cartQ = new LambdaQueryWrapper<>();
		cartQ.eq(Cart::getUserId, userId);
		Cart cart = this.getOne(cartQ);
		if (cart == null) {
			return true;
		}
		LambdaQueryWrapper<CartItem> itemQ = new LambdaQueryWrapper<>();
		itemQ.eq(CartItem::getCartId, cart.getCartId());
		cartItemMapper.delete(itemQ);
		cart.setUpdateTime(LocalDateTime.now());
		cartMapper.updateById(cart);
		return true;
	}

	@Override
	public Integer checkout(Long userId, Integer addressId, List<Integer> productIds) {
		if (userId == null || addressId == null || productIds == null || productIds.isEmpty()) {
			return null;
		}
		LambdaQueryWrapper<Cart> cartQ = new LambdaQueryWrapper<>();
		cartQ.eq(Cart::getUserId, userId);
		Cart cart = this.getOne(cartQ);
		if (cart == null) {
			return null;
		}
		LambdaQueryWrapper<CartItem> itemQ = new LambdaQueryWrapper<>();
		itemQ.eq(CartItem::getCartId, cart.getCartId()).in(CartItem::getProductId, productIds);
		List<CartItem> items = cartItemMapper.selectList(itemQ);
		if (items == null || items.isEmpty()) {
			return null;
		}
		BigDecimal total = BigDecimal.ZERO;
		List<OrderItem> orderItems = new ArrayList<>();
		for (CartItem ci : items) {
			Product p = productMapper.selectById(ci.getProductId());
			if (p == null || p.getStatus() == null || !"active".equalsIgnoreCase(p.getStatus())) {
				return null;
			}
			if (p.getStock() == null || p.getStock() < ci.getQuantity()) {
				return null;
			}
			BigDecimal unitPrice = p.getPrice() == null ? BigDecimal.ZERO : p.getPrice();
			BigDecimal line = unitPrice.multiply(BigDecimal.valueOf(ci.getQuantity()));
			total = total.add(line);
			OrderItem oi = new OrderItem();
			oi.setProductId(ci.getProductId());
			oi.setQuantity(ci.getQuantity());
			oi.setUnitPrice(unitPrice);
			oi.setCreateTime(LocalDateTime.now());
			orderItems.add(oi);
		}
		Order order = new Order();
		order.setUserId(userId);
		order.setOrderDate(LocalDateTime.now());
		order.setTotalAmount(total);
		order.setStatus("created");
		order.setShippingAddressId(addressId);
		order.setCreateTime(LocalDateTime.now());
		order.setUpdateTime(LocalDateTime.now());
		orderMapper.insert(order);
		for (OrderItem oi : orderItems) {
			Product p = productMapper.selectById(oi.getProductId());
			p.setStock(p.getStock() - oi.getQuantity());
			productMapper.updateById(p);
			oi.setOrderId(order.getOrderId());
			orderItemMapper.insert(oi);
		}
		LambdaQueryWrapper<CartItem> delQ = new LambdaQueryWrapper<>();
		delQ.eq(CartItem::getCartId, cart.getCartId()).in(CartItem::getProductId, productIds);
		cartItemMapper.delete(delQ);
		cart.setUpdateTime(LocalDateTime.now());
		cartMapper.updateById(cart);
		return order.getOrderId();
	}
}
