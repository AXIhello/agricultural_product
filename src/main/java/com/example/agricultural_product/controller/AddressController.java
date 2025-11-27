package com.example.agricultural_product.controller;
import com.example.agricultural_product.pojo.Address;
import com.example.agricultural_product.pojo.Product;
import com.example.agricultural_product.service.AddressService;
import com.example.agricultural_product.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    // ===== JWT 鉴权方法 =====
    private boolean checkToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);
        return JwtUtil.validateToken(token);
    }

    // 获取单个地址详情
    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddress(HttpServletRequest request,
                                              @PathVariable Integer id) {
        if (!checkToken(request)) return ResponseEntity.status(401).build();
        Address saved = addressService.getById(id);
        return ResponseEntity.ok(saved);
    }


    // 1. 新增地址
    @PostMapping("/add")
    public ResponseEntity<Address> addAddress(@RequestBody Address address, HttpServletRequest request) {
        Claims claims = JwtUtil.getClaimsFromRequest(request);
        Long userId = claims.get("userId", Long.class);
        address.setUserId(userId);

        Address saved = addressService.addAddress(address);
        return ResponseEntity.ok(saved);
    }

    // 2. 查询当前用户的所有地址
    @GetMapping("/user")
    public ResponseEntity<List<Address>> getUserAddresses(HttpServletRequest request) {
        Claims claims = JwtUtil.getClaimsFromRequest(request);
        Long userId = claims.get("userId", Long.class);

        List<Address> addresses = addressService.getAddressesByUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    // 3. 修改地址
    @PutMapping("/update/{addressId}")
    public ResponseEntity<Address> updateAddress(@PathVariable Integer addressId,
                                                 @RequestBody Address address,
                                                 HttpServletRequest request) {
        Claims claims = JwtUtil.getClaimsFromRequest(request);
        Long userId = claims.get("userId", Long.class);
        address.setUserId(userId);

        Address updated = addressService.updateAddress(addressId, address);
        return ResponseEntity.ok(updated);
    }

    // 4. 删除地址
    @DeleteMapping("/delete/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Integer addressId,
                                              HttpServletRequest request) {
        Claims claims = JwtUtil.getClaimsFromRequest(request);
        Long userId = claims.get("userId", Long.class);

        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    // 5. 设置默认地址
    @PutMapping("/set-default/{addressId}")
    public ResponseEntity<Void> setDefaultAddress(@PathVariable Integer addressId,
                                                  HttpServletRequest request) {
        Claims claims = JwtUtil.getClaimsFromRequest(request);
        Long userId = claims.get("userId", Long.class);

        addressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取当前用户的默认地址
     */
    @GetMapping("/default")
    public ResponseEntity<Address> getDefaultAddress(HttpServletRequest request) {
        // 1. 从请求中解析 JWT 获取 userId
        Claims claims = JwtUtil.getClaimsFromRequest(request);
        Long userId = claims.get("userId", Long.class);

        // 2. 调用 Service 查询默认地址
        Address defaultAddress = addressService.getDefaultAddress(userId);

        // 3. 返回结果
        if (defaultAddress != null) {
            return ResponseEntity.ok(defaultAddress);
        } else {
            return ResponseEntity.noContent().build(); // 或者返回 404
        }
    }

}
