package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agricultural_product.mapper.AddressMapper;
import com.example.agricultural_product.pojo.Address;
import com.example.agricultural_product.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    @Transactional
    public Address addAddress(Address address) {
        // 查询当前用户已有地址
        List<Address> existing = this.list(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, address.getUserId()));

        // 如果没有地址，设为默认地址
        if (existing.isEmpty()) {
            address.setIsDefault(true);
        } else if (Boolean.TRUE.equals(address.getIsDefault())) {
            // 如果新添加的地址设为默认，取消原默认地址
            Address defaultAddress = this.getOne(new LambdaQueryWrapper<Address>()
                    .eq(Address::getUserId, address.getUserId())
                    .eq(Address::getIsDefault, true));
            if (defaultAddress != null) {
                defaultAddress.setIsDefault(false);
                this.updateById(defaultAddress);
            }
        }

        // 设置时间
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());
        this.save(address);
        return address;
    }

    @Override
    public List<Address> getAddressesByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId));
    }

    @Override
    @Transactional
    public Address updateAddress(Integer addressId, Address address) {
        Address existing = this.getById(addressId);
        if (existing == null) {
            throw new RuntimeException("Address not found with id: " + addressId);
        }

        // 校验是否属于当前用户
        if (!existing.getUserId().equals(address.getUserId())) {
            throw new RuntimeException("Cannot update address that does not belong to the current user");
        }

        // 更新字段
        existing.setRecipientName(address.getRecipientName());
        existing.setPhoneNumber(address.getPhoneNumber());
        existing.setProvince(address.getProvince());
        existing.setCity(address.getCity());
        existing.setDistrict(address.getDistrict());
        existing.setStreetAddress(address.getStreetAddress());
        existing.setPostalCode(address.getPostalCode());
        existing.setUpdateTime(LocalDateTime.now());

        this.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Integer addressId) {
        Address address = this.getById(addressId);
        if (address == null) {
            throw new RuntimeException("Address not found with id: " + addressId);
        }

        if (!address.getUserId().equals(userId)) {
            throw new RuntimeException("Cannot delete address that does not belong to the current user");
        }

        this.removeById(addressId);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long userId, Integer addressId) {
        // 取消原默认地址
        Address oldDefault = this.getOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, true));
        if (oldDefault != null) {
            oldDefault.setIsDefault(false);
            this.updateById(oldDefault);
        }

        // 设置新的默认地址
        Address addr = this.getById(addressId);
        if (addr == null) {
            throw new RuntimeException("Address not found with id: " + addressId);
        }

        if (!addr.getUserId().equals(userId)) {
            throw new RuntimeException("Cannot set default address that does not belong to the current user");
        }

        addr.setIsDefault(true);
        addr.setUpdateTime(LocalDateTime.now());
        this.updateById(addr);
    }
}
