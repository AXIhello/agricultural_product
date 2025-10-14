package com.example.agricultural_product.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 用户申请专家或银行身份的实体类
 */
@Data
@TableName("user_application")
public class UserApplication {
    private Long id;               // 主键
    private String userName;       // 用户名
    private String password;       // 密码
    private String applyRole;      // 申请角色 (expert/bank)
    @TableField("real_name")
    private String realName;       // 真实姓名
    private String email;          // 邮箱
    @TableField("attachment_path")
    private String attachmentPath; // 附件路径
    private String status;         // 审核状态
    private String reason;         // 审核理由
    private Date createTime;       // 创建时间
}
