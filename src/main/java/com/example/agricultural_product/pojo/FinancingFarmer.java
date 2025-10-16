package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_financing_farmers")
public class FinancingFarmer {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("financing_id")
    private Integer financingId;

    @TableField("farmer_id")
    private Long farmerId;

    @TableField("role_in_financing")
    private String roleInFinancing;

    @TableField("create_time")
    private LocalDateTime createTime;

    // Getter 和 Setter 方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFinancingId() {
        return financingId;
    }

    public void setFinancingId(Integer financingId) {
        this.financingId = financingId;
    }

    public Long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }

    public String getRoleInFinancing() {
        return roleInFinancing;
    }

    public void setRoleInFinancing(String roleInFinancing) {
        this.roleInFinancing = roleInFinancing;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}