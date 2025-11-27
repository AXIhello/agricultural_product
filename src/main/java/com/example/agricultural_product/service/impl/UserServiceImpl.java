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
    public String getUserName(Long userId) {
        return userMapper.getUserName(userId);
    }

    @Override
    // 更新地区
    public boolean updateRegion(Long userId, String region) {
        User user = new User();
        user.setUserId(userId);
        user.setRegion(region);
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean updateCreditScore(Long userId, Integer score) {
        // 如果需要手动设置分数，可以直接更新
        User user = userMapper.findByUserId(userId);
        if (user != null) {
            user.setCreditScore(score);
            int rows = userMapper.updateById(user);
            return rows > 0; // 返回是否更新成功
        }
        return false; // 用户不存在，返回 false
    }

    @Override
    public void updateCreditScore(Long userId, String action) {
        User user = userMapper.findByUserId(userId);
        if (user == null) return;

        Integer currentScore = user.getCreditScore() != null ? user.getCreditScore() : 60;

        switch (action) {
            case "loan_success":
                currentScore += 10;
                break;
            case "loan_cancel":
                currentScore -= 2;
                break;
            case "loan_rejected":
                currentScore -= 3;
                break;
            case "joint_loan_success":
                currentScore += 5;
                break;
            case "loan_default":
                currentScore -= 15;
                break;
            default:
                break;
        }

        // 保证分数范围 0~100
        currentScore = Math.max(0, Math.min(100, currentScore));

        user.setCreditScore(currentScore);
        userMapper.updateById(user);
    }
}

