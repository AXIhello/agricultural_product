package com.example.agricultural_product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.agricultural_product.pojo.UserApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserApplicationMapper extends BaseMapper<UserApplication> {

    /**
     * 根据ID从数据库查找UserApplication。
     * @param id 申请的ID (String类型)。
     * @return 找到的UserApplication对象，如果未找到则返回null。
     */
    @Select("SELECT * FROM user_application WHERE id = #{id}") // 假设你的表名是 user_application
    UserApplication selectById(@Param("id") String id);
}
