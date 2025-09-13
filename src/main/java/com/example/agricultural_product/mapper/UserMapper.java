package com.example.agricultural_product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.agricultural_product.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
