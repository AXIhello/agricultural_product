package com.example.agricultural_product.service;
import com.example.agricultural_product.pojo.User;
public interface UserService {
    User findByUserName(String userName);
    int registerUser(User newUser);
    Long getUserIdByUserName(String userName);
    
    String getUserName(Long userId);
    User findById(Long userId);
    boolean updateRegion(Long userId, String region);
    boolean updateCreditScore(Long userId, Integer creditScore);

    void updateCreditScore(Long userId, String action);
}

