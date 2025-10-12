package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_addresses")
public class Address {

    @TableId(value = "address_id", type = IdType.AUTO)
    private Integer addressId;

    @TableField("user_id")
    private Long userId;

    @TableField("recipient_name")
    private String recipientName;

    @TableField("phone_number")
    private String phoneNumber;

    @TableField("province")
    private String province;

    @TableField("city")
    private String city;

    @TableField("district")
    private String district;

    @TableField("street_address")
    private String streetAddress;

    @TableField("postal_code")
    private String postalCode;

    @TableField("is_default")
    private Boolean isDefault;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}