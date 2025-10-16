package com.example.agricultural_product.service.impl;

import com.example.agricultural_product.pojo.User;
import com.example.agricultural_product.mapper.UserMapper;
import com.example.agricultural_product.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User findByUserName(String userName) {
        return userMapper.findByUserName(userName);
    }

    @Override
    public User findById(Long userId) {
        return userMapper.findByUserId(userId);
    }

    public int registerUser(User user) {
        return userMapper.insert(user);
    }

    @Override
    public Long getUserIdByUserName(String userName) {
        // 根据用户名查询用户，并返回用户 ID
        User user = findByUserName(userName); //  调用 findByUserName 获取用户
        if (user != null) {
            return user.getUserId(); //  如果找到用户，返回用户 ID
        }
        return null; // 如果用户不存在，返回 null
    }

    @Override
// 更新地区
    public boolean updateRegion(Long userId, String region) {
        User user = new User();
        user.setUserId(userId);
        user.setRegion(region);
        return userMapper.updateById(user) > 0;
    }

    // 更新信用分
    public boolean updateCreditScore(Long userId, Integer creditScore) {
        User user = new User();
        user.setUserId(userId);
        user.setCreditScore(creditScore);
        return userMapper.updateById(user) > 0;
    }



}

