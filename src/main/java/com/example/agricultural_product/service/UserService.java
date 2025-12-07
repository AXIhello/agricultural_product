package com.example.agricultural_product.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.agricultural_product.pojo.User;
import java.util.List;
public interface UserService extends IService<User>{
    User findByUserName(String userName);
    int registerUser(User newUser);
    Long getUserIdByUserName(String userName);

    String getUserName(Long userId);
    User findById(Long userId);
    boolean updateRegion(Long userId, String region);
    boolean updateCreditScore(Long userId, Integer creditScore);

    void updateCreditScore(Long userId, String action);

    // ========== 管理员用户管理相关 ==========
    /** 更新用户完整信息（不处理密码加密逻辑，由调用方控制） */
    int updateUser(User user);

    /** 查询所有用户列表 */
    List<User> findAll();

    /** 按ID删除用户 */
    boolean deleteById(Long userId);
}
