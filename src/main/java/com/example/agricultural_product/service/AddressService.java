package com.example.agricultural_product.service;

import com.example.agricultural_product.entity.Address;

import java.util.List;

public interface AddressService {

    Address addAddress(Address address);

    List<Address> getAddressesByUserId(Long userId);

    Address updateAddress(Integer addressId, Address address);

    // 增加 userId 参数，保证用户只能操作自己的地址
    void deleteAddress(Long userId, Integer addressId);

    void setDefaultAddress(Long userId, Integer addressId);
}
