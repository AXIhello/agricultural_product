package com.example.agricultural_product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.agricultural_product.pojo.Financing;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FinancingMapper extends BaseMapper<Financing> {

    /**
     * 银行查看所有融资申请（分页）
     * 包含发起人名称和产品名称
     */
    @Select("SELECT " +
            "tf.financing_id, " +
            "tf.initiating_farmer_id, " +
            "tf.amount, " +
            "tf.purpose, " +
            "tf.term, " +
            "tf.product_id, " +
            "tf.application_status, " +
            "tf.create_time, " +
            "tf.update_time, " +
            "COALESCE(tbp.product_name, '农户自助申请') AS productName, " + 
            "COALESCE(u.user_name, CONCAT('用户', tf.initiating_farmer_id)) AS initiatingFarmerName " +
            "FROM tb_financing tf " +
            "LEFT JOIN tb_bank_products tbp ON tf.product_id = tbp.product_id " +
            "LEFT JOIN users u ON tf.initiating_farmer_id = u.user_id " +
            "ORDER BY tf.create_time DESC")
    Page<Financing> selectAllFinancingsWithDetails(Page<Financing> page);

    
   /**
     * 查询某个用户参与的融资申请，不管是主申请人还是共同申请人，
     * 并关联产品名称和发起农户名称
     *
     * @param userId 用户ID
     * @return 包含产品名称和发起农户名称的Financing列表
     */
    @Select("SELECT " +
            "tf.financing_id, " +
            "tf.initiating_farmer_id, " +
            "tf.amount, " +
            "tf.purpose, " +
            "tf.term, " +
            "tf.product_id, " +
            "tf.application_status, " +
            "tf.create_time, " +
            "tf.update_time, " +
            "tbp.product_name AS productName, " + 
            "tfa.user_name AS initiatingFarmerName " +
            "FROM tb_financing tf " +
            "LEFT JOIN tb_bank_products tbp ON tf.product_id = tbp.product_id " +
            "LEFT JOIN users tfa ON tf.initiating_farmer_id = tfa.user_id " +
            "INNER JOIN tb_financing_farmers tff ON tf.financing_id = tff.financing_id " +
            "WHERE tff.farmer_id = #{userId} " +
            "ORDER BY tf.create_time DESC")
    List<Financing> selectFinancingByUserIdWithProductAndFarmerName(@Param("userId") Long userId);
    
    /**
     * 分页查询某个用户参与的融资申请
     */
    @Select("SELECT " +
            "tf.financing_id, " +
            "tf.initiating_farmer_id, " +
            "tf.amount, " +
            "tf.purpose, " +
            "tf.term, " +
            "tf.product_id, " +
            "tf.application_status, " +
            "tf.create_time, " +
            "tf.update_time, " +
            "tbp.product_name AS productName, " + 
            "tfa.user_name AS initiatingFarmerName " +
            "FROM tb_financing tf " +
            "LEFT JOIN tb_bank_products tbp ON tf.product_id = tbp.product_id " +
            "LEFT JOIN users tfa ON tf.initiating_farmer_id = tfa.user_id " +
            "INNER JOIN tb_financing_farmers tff ON tf.financing_id = tff.financing_id " +
            "WHERE tff.farmer_id = #{userId} " +
            "ORDER BY tf.create_time DESC")
    Page<Financing> selectFinancingPageByUserIdWithProductAndFarmerName(
            Page<Financing> page, 
            @Param("userId") Long userId);


}