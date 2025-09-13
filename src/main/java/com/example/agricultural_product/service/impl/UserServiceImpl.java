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

    public int registerUser(User user) {
        return userMapper.insertUser(user);
    }
}

