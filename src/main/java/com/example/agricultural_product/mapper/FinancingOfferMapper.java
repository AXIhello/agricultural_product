package com.example.agricultural_product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.agricultural_product.pojo.Financing;
import com.example.agricultural_product.pojo.FinancingOffer;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FinancingOfferMapper extends BaseMapper<FinancingOffer> {
    /**
     * 查询我发起的，或者我作为共同申请人（已接受）的融资申请
     */
    @Select("SELECT DISTINCT f.* " +
        "FROM tb_financing f " +
        "LEFT JOIN tb_financing_farmers ff ON f.financing_id = ff.financing_id " +
        "WHERE f.initiating_farmer_id = #{userId} " +
        "   OR (ff.farmer_id = #{userId} AND ff.invitation_status = 'accepted') " +
        "ORDER BY f.create_time DESC")
    Page<Financing> selectMyRelatedFinancing(Page<Financing> page, @Param("userId") Long userId);
}