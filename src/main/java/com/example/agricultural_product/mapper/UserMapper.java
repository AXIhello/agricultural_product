package com.example.agricultural_product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.agricultural_product.pojo.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE user_name = #{userName}")
    User findByUserName(String userName);

    @Select("SELECT * FROM users WHERE user_id = #{userId}")
    User findByUserId(Long userId);

    @Update("""
        UPDATE users
        SET 
            name = #{name},
            email = #{email},
            region = #{region},
            credit_score = #{creditScore},
            historical_success_rate = #{historicalSuccessRate},
            average_financing_amount = #{averageFinancingAmount},
            financing_activity_level = #{financingActivityLevel}
        WHERE user_id = #{userId}
    """)
    int updateUserProfile(User user);

    @Update("UPDATE users SET region = #{region} WHERE user_id = #{userId}")
    int updateRegion(@Param("userId") Long userId, @Param("region") String region);

    @Update("UPDATE users SET credit_score = #{creditScore} WHERE user_id = #{userId}")
    int updateCreditScore(@Param("userId") Long userId, @Param("creditScore") Integer creditScore);

    @Update("UPDATE users SET region = #{region}, credit_score = #{creditScore} WHERE user_id = #{userId}")
    int updateRegionAndCredit(@Param("userId") Long userId,
                              @Param("region") String region,
                              @Param("creditScore") Integer creditScore);
    
}
