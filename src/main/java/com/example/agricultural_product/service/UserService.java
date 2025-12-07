package com.example.agricultural_product.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.agricultural_product.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService; 
public interface UserService extends IService<User>{
    User findByUserName(String userName);
    int registerUser(User newUser);
    Long getUserIdByUserName(String userName);
    
    String getUserName(Long userId);
    User findById(Long userId);
    boolean updateRegion(Long userId, String region);
    boolean updateCreditScore(Long userId, Integer creditScore);

    void updateCreditScore(Long userId, String action);
}

