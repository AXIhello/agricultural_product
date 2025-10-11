package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tb_financing_offers")
public class FinancingOffer {
    @TableId(value = "offer_id", type = IdType.AUTO)
    private Integer offerId;

    @TableField("financing_id")
    private Integer financingId;

    @TableField("bank_user_id")
    private Long bankUserId;

    @TableField("offered_amount")
    private BigDecimal offeredAmount;

    @TableField("offered_interest_rate")
    private BigDecimal offeredInterestRate;

    @TableField("offer_status")
    private String offerStatus;

    @TableField("bank_notes")
    private String bankNotes;

    @TableField("offer_time")
    private LocalDateTime offerTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}