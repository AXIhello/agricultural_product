package com.example.agricultural_product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.agricultural_product.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE user_name = #{userName}")
    User findByUserName(String userName);
    // int insertUser(User user);
    @Select("SELECT * FROM users WHERE user_id = #{userId}")
        User findByUserId(Long userId);
}
