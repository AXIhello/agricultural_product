package com.example.agricultural_product.service.impl;

import com.example.agricultural_product.entity.Address;
import com.example.agricultural_product.repository.AddressRepository;
import com.example.agricultural_product.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Override
    @Transactional
    public Address addAddress(Address address) {
        List<Address> existing = addressRepository.findByUserId(address.getUserId());
        if (existing.isEmpty()) {
            address.setIsDefault(true);
        } else if (Boolean.TRUE.equals(address.getIsDefault())) {
            Address defaultAddress = addressRepository.findByUserIdAndIsDefaultTrue(address.getUserId());
            if (defaultAddress != null) {
                defaultAddress.setIsDefault(false);
                addressRepository.save(defaultAddress);
            }
        }

        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());
        return addressRepository.save(address);
    }

    @Override
    public List<Address> getAddressesByUserId(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Address updateAddress(Integer addressId, Address address) {
        Optional<Address> optional = addressRepository.findById(addressId);
        if (optional.isEmpty()) {
            throw new RuntimeException("Address not found with id: " + addressId);
        }

        Address existing = optional.get();
        // 校验地址是否属于当前用户
        if (!existing.getUserId().equals(address.getUserId())) {
            throw new RuntimeException("Cannot update address that does not belong to the current user");
        }

        existing.setRecipientName(address.getRecipientName());
        existing.setPhoneNumber(address.getPhoneNumber());
        existing.setProvince(address.getProvince());
        existing.setCity(address.getCity());
        existing.setDistrict(address.getDistrict());
        existing.setStreetAddress(address.getStreetAddress());
        existing.setPostalCode(address.getPostalCode());
        existing.setUpdateTime(LocalDateTime.now());

        return addressRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Integer addressId) {
        Optional<Address> optional = addressRepository.findById(addressId);
        if (optional.isEmpty()) {
            throw new RuntimeException("Address not found with id: " + addressId);
        }

        Address address = optional.get();
        if (!address.getUserId().equals(userId)) {
            throw new RuntimeException("Cannot delete address that does not belong to the current user");
        }

        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long userId, Integer addressId) {
        Address oldDefault = addressRepository.findByUserIdAndIsDefaultTrue(userId);
        if (oldDefault != null) {
            oldDefault.setIsDefault(false);
            addressRepository.save(oldDefault);
        }

        Optional<Address> newDefault = addressRepository.findById(addressId);
        if (newDefault.isPresent()) {
            Address addr = newDefault.get();
            if (!addr.getUserId().equals(userId)) {
                throw new RuntimeException("Cannot set default address that does not belong to the current user");
            }
            addr.setIsDefault(true);
            addr.setUpdateTime(LocalDateTime.now());
            addressRepository.save(addr);
        } else {
            throw new RuntimeException("Address not found with id: " + addressId);
        }
    }
}
