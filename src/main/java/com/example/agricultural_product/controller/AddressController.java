package com.example.agricultural_product.controller;

import com.example.agricultural_product.entity.Address;
import com.example.agricultural_product.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    // 1. 新增地址
    @PostMapping("/add")
    public ResponseEntity<Address> addAddress(@RequestBody Address address) {
        Address saved = addressService.addAddress(address);
        return ResponseEntity.ok(saved);
    }

    // 2. 查询某用户的所有地址
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Address>> getUserAddresses(@PathVariable Long userId) {
        List<Address> addresses = addressService.getAddressesByUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    // 3. 修改地址
    @PutMapping("/update/{addressId}")
    public ResponseEntity<Address> updateAddress(@PathVariable Integer addressId, @RequestBody Address address) {
        Address updated = addressService.updateAddress(addressId, address);
        return ResponseEntity.ok(updated);
    }

    // 4. 删除地址
    @DeleteMapping("/delete/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Integer addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }

    // 5. 设置默认地址
    @PutMapping("/set-default/{userId}/{addressId}")
    public ResponseEntity<Void> setDefaultAddress(@PathVariable Long userId, @PathVariable Integer addressId) {
        addressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok().build();
    }
}
