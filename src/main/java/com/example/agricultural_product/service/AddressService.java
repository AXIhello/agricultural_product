package com.example.agricultural_product.service;

import com.example.agricultural_product.entity.Address;
import java.util.List;

public interface AddressService {

    Address addAddress(Address address);

    List<Address> getAddressesByUserId(Long userId);

    Address updateAddress(Integer addressId, Address address);

    void deleteAddress(Integer addressId);

    void setDefaultAddress(Long userId, Integer addressId);
}
