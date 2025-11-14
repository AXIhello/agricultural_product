-- src/main/resources/agriculture.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0; -- 禁用外键检查，方便创建表结构

-- ----------------------------
-- Table structure for users
-- ----------------------------
CREATE TABLE IF NOT EXISTS `users` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_name` varchar(50) NOT NULL COMMENT '账号，用于登录',
  `password` varchar(255) NOT NULL COMMENT '加密后的密码',
  `name` varchar(100) DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) DEFAULT NULL,
  `role` varchar(50) NOT NULL COMMENT '角色',
  -- ↓↓↓ 新增字段 ↓↓↓
  `region` varchar(50) DEFAULT NULL COMMENT '用户所在地区',
  `credit_score` int DEFAULT NULL COMMENT '用户信用分，可为空',
  `historical_success_rate` decimal(5,2) DEFAULT NULL COMMENT '历史融资成功率(百分比)',
  `average_financing_amount` decimal(12,2) DEFAULT NULL COMMENT '平均融资金额',
  `financing_activity_level` enum('low','medium','high') DEFAULT NULL COMMENT '用户融资活跃度等级（低/中/高）',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE KEY `user_name` (`user_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表 (支持智能推荐的扩展版)';


-- ----------------------------
-- Table structure for tb_addresses
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_addresses`  (
  `address_id` int NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` bigint NOT NULL COMMENT '用户ID，关联users表',
  `recipient_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收件人姓名',
  `phone_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系电话',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '区/县',
  `street_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细街道地址',
  `postal_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮政编码',
  `is_default` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为默认地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`address_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户收货地址表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_expert_profiles
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_expert_profiles`  (
  `expert_id` bigint NOT NULL COMMENT '专家用户ID，关联users表，作为主键和外键',
  `specialization` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '专长领域（例如：种植技术、病虫害防治、土壤改良、畜牧养殖）',
  `bio` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '个人简介',
  `photo_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像图片URL',
  `average_rating` decimal(2, 1) NULL DEFAULT 0.0 COMMENT '平均评分（0.0-5.0）',
  `consultation_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '咨询费用（按次或按小时）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`expert_id`) USING BTREE,
  CONSTRAINT `fk_expert_profile_user` FOREIGN KEY (`expert_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '专家档案表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for tb_agricultural_knowledge
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_agricultural_knowledge`  (
  `knowledge_id` int NOT NULL AUTO_INCREMENT COMMENT '知识ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '知识标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '知识内容',
  `expert_id` bigint NULL DEFAULT NULL COMMENT '发布专家ID，直接关联tb_expert_profiles表',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`knowledge_id`) USING BTREE,
  INDEX `idx_expert_id`(`expert_id` ASC) USING BTREE,
  CONSTRAINT `fk_knowledge_expert_profile` FOREIGN KEY (`expert_id`) REFERENCES `tb_expert_profiles` (`expert_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '农业知识表 (基础信息)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for tb_cart
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_cart`  (
  `cart_id` int NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` bigint NOT NULL COMMENT '用户ID，关联users表，一个用户一个购物车',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`cart_id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_product
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_product` (
    `product_id` int NOT NULL AUTO_INCREMENT COMMENT '产品ID',
    `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '产品名称',
    `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '产品描述',
    `price` decimal(10,2) NOT NULL COMMENT '价格',
    `stock` int NOT NULL DEFAULT '0' COMMENT '库存',
    `farmer_id` bigint NOT NULL COMMENT '农户用户ID，关联users表',
    `status` enum('active','inactive') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'active' COMMENT '产品状态：active（上架）、inactive（下架）',
    `image_path` varchar(512) NULL COMMENT '本地图片相对路径或URL',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `prodCat` text NOT NULL COMMENT '产品大类',
    `prodPcat` text NOT NULL COMMENT '产品小类',
    `unitInfo` text NOT NULL COMMENT '产品单位',
    `specInfo` text COMMENT '产品信息',
    `place` text COMMENT '产地',
    PRIMARY KEY (`product_id`) USING BTREE,
    KEY `idx_farmer_id` (`farmer_id`) USING BTREE,
    CONSTRAINT `fk_product_farmer` FOREIGN KEY (`farmer_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10007 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='农产品信息表 (基础信息)';
-- ----------------------------
-- Table structure for tb_cart_items
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_cart_items`  (
  `item_id` int NOT NULL AUTO_INCREMENT COMMENT '购物车明细ID',
  `cart_id` int NOT NULL COMMENT '购物车ID，关联tb_cart表',
  `product_id` int NOT NULL COMMENT '产品ID，关联tb_product表',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '购买数量',
  `price_at_add` decimal(10, 2) NOT NULL COMMENT '加入购物车时的商品单价',
  `add_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入购物车时间',
  PRIMARY KEY (`item_id`) USING BTREE,
  UNIQUE INDEX `idx_cart_product`(`cart_id` ASC, `product_id` ASC) USING BTREE,
  INDEX `idx_cart_id`(`cart_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  CONSTRAINT `fk_cart_item_cart` FOREIGN KEY (`cart_id`) REFERENCES `tb_cart` (`cart_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_cart_item_product` FOREIGN KEY (`product_id`) REFERENCES `tb_product` (`product_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车商品明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_expert_consultation
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_expert_consultation`  ( 
  `consultation_id` INT NOT NULL AUTO_INCREMENT COMMENT '咨询ID',
  `farmer_id` BIGINT NOT NULL COMMENT '农户用户ID，关联users表',
  `expert_id` BIGINT NOT NULL COMMENT '专家用户ID，关联users表',
  `slot_id` INT NULL COMMENT '工作时间段ID，关联 tb_expert_working_slots.slot_id',
  `consultation_time` DATETIME NOT NULL COMMENT '预约的咨询时间',
  `status` ENUM('scheduled','completed','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'scheduled' COMMENT '状态：scheduled（已预约）、completed（已完成）、cancelled（已取消）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`consultation_id`) USING BTREE,
  INDEX `idx_farmer_id`(`farmer_id` ASC) USING BTREE,
  INDEX `idx_expert_id`(`expert_id` ASC) USING BTREE,
  INDEX `idx_slot_farmer`(`slot_id`, `farmer_id`) USING BTREE,
  CONSTRAINT `fk_consultation_expert` FOREIGN KEY (`expert_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_consultation_farmer` FOREIGN KEY (`farmer_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_consultation_slot` FOREIGN KEY (`slot_id`) REFERENCES `tb_expert_working_slots` (`slot_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB 
AUTO_INCREMENT = 4 
DEFAULT CHARSET = utf8mb4 
COLLATE = utf8mb4_0900_ai_ci 
COMMENT = '专家咨询预约表 (基础信息)' 
ROW_FORMAT = DYNAMIC;

-- 新增：银行固定产品表
CREATE TABLE IF NOT EXISTS `tb_bank_products` (
  `product_id` INT NOT NULL AUTO_INCREMENT COMMENT '固定产品ID',
  `bank_user_id` BIGINT NOT NULL COMMENT '银行用户ID，关联users.user_id',
  `product_name` VARCHAR(100) NOT NULL COMMENT '产品名称',
  `description` TEXT NULL COMMENT '产品说明',
  `term_months` INT NOT NULL COMMENT '固定期限(月)',
  `interest_rate` DECIMAL(5,4) NOT NULL COMMENT '年利率(如0.0500为5%)',
  `min_amount` DECIMAL(12,2) NOT NULL COMMENT '最小申请金额',
  `max_amount` DECIMAL(12,2) NOT NULL COMMENT '最大申请金额',
  `status` ENUM('active','inactive') NOT NULL DEFAULT 'active' COMMENT '产品状态',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`product_id`) USING BTREE,
  INDEX `idx_bank_user_id`(`bank_user_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  CONSTRAINT `fk_bank_product_bank_user` FOREIGN KEY (`bank_user_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='银行固定贷款产品表';

-- 更新：为融资表补充term与绑定的product_id
CREATE TABLE IF NOT EXISTS `tb_financing` (
  `financing_id` INT NOT NULL AUTO_INCREMENT COMMENT '融资ID',
  `initiating_farmer_id` BIGINT NOT NULL COMMENT '发起人ID，关联users表',
  `amount` DECIMAL(12,2) NOT NULL COMMENT '申请融资金额',
  `purpose` TEXT NULL COMMENT '用途',
  `term` INT NULL COMMENT '期限(月)',
  `product_id` INT NULL COMMENT '绑定的固定产品ID',
  `application_status` ENUM('draft','submitted','cancelled','approved','rejected','overdue')
      NOT NULL DEFAULT 'draft' COMMENT '申请状态',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`financing_id`) USING BTREE,
  INDEX `idx_initiating_farmer_id` (`initiating_farmer_id`) USING BTREE,
  INDEX `idx_product_id` (`product_id`) USING BTREE,
  CONSTRAINT `fk_financing_initiator` FOREIGN KEY (`initiating_farmer_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_financing_product` FOREIGN KEY (`product_id`) REFERENCES `tb_bank_products` (`product_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='融资信息表(支持绑定银行固定产品)';


-- ----------------------------
-- Table structure for tb_financing_farmers
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_financing_farmers`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `financing_id` int NOT NULL COMMENT '融资申请ID，关联tb_financing表',
  `farmer_id` bigint NOT NULL COMMENT '参与该融资申请的农户ID，关联users表',
  `role_in_financing` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'co_applicant' COMMENT '农户在该融资中的角色（如：主申请人、共同申请人、担保人）',
  `invitation_status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '邀请状态：pending/accepted/rejected/cancelled',
  `invited_by` bigint NULL COMMENT '邀请人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `decision_time` datetime NULL COMMENT '受邀人决定时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_financing_farmer`(`financing_id` ASC, `farmer_id` ASC) USING BTREE,
  INDEX `idx_financing_id`(`financing_id` ASC) USING BTREE,
  INDEX `idx_farmer_id`(`farmer_id` ASC) USING BTREE,
  INDEX `idx_financing_status` (`financing_id`,`invitation_status`) USING BTREE,
  CONSTRAINT `fk_ff_farmer` FOREIGN KEY (`farmer_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_ff_financing` FOREIGN KEY (`financing_id`) REFERENCES `tb_financing` (`financing_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '融资申请参与农户表' ROW_FORMAT = DYNAMIC;
-- ----------------------------
-- Table structure for tb_financing_offers
-- ----------------------------
CREATE TABLE IF NOT EXISTS`tb_financing_offers` (
  `offer_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `financing_id` int NOT NULL COMMENT '融资申请ID，关联tb_financing表',
  `bank_user_id` bigint NOT NULL COMMENT '提供报价/审批的银行用户ID，关联users表',
  `offered_amount` decimal(12,2) DEFAULT NULL COMMENT '银行愿意提供的金额',
  `offered_interest_rate` decimal(5,4) DEFAULT NULL COMMENT '银行提供的年利率（例如0.05表示5%）',
  `offer_status` enum('pending','accepted','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '报价状态：pending, accepted, rejected',
  `bank_notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '银行的附加说明或条件',
  `offer_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报价/响应时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`offer_id`) USING BTREE,
  UNIQUE KEY `idx_financing_bank_offer` (`financing_id`,`bank_user_id`) USING BTREE,
  KEY `idx_financing_id` (`financing_id`) USING BTREE,
  KEY `idx_bank_user_id` (`bank_user_id`) USING BTREE,
  CONSTRAINT `fk_fo_bank` FOREIGN KEY (`bank_user_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_fo_financing` FOREIGN KEY (`financing_id`) REFERENCES `tb_financing` (`financing_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='融资银行报价/匹配表';


-- ----------------------------
-- Table structure for tb_order
-- IMPORTANT: This table was missing in your original SQL.
-- Adding a basic version to support foreign keys in tb_order_item.
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_order` (
  `order_id` int NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '下单用户ID',
  `order_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单日期',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '总金额',
  `status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单状态',
  `shipping_address_id` int NULL COMMENT '收货地址ID，关联tb_addresses',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`order_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_order_address` FOREIGN KEY (`shipping_address_id`) REFERENCES `tb_addresses` (`address_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 100004 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表';

-- ----------------------------
-- Table structure for tb_order_item
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_order_item`  (
  `item_id` int NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
  `order_id` int NOT NULL COMMENT '订单ID',
  `product_id` int NOT NULL COMMENT '产品ID',
  `quantity` int NOT NULL COMMENT '购买数量',
  `unit_price` decimal(10, 2) NOT NULL COMMENT '下单时的单价',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`item_id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `tb_order` (`order_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_order_item_product` FOREIGN KEY (`product_id`) REFERENCES `tb_product` (`product_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单明细表 (基础信息)' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_purchase_demands
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_purchase_demands`  (
  `demand_id` int NOT NULL AUTO_INCREMENT COMMENT '求购需求ID',
  `buyer_id` bigint NOT NULL COMMENT '发起求购的买家ID，关联users表',
  `product_name_desired` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '期望产品名称/类别',
  `quantity_desired` decimal(10, 2) NOT NULL COMMENT '期望数量',
  `max_price_per_unit` decimal(10, 2) NULL DEFAULT NULL COMMENT '最高接受单价',
  `details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '详细需求描述',
  `delivery_date_desired` date NULL DEFAULT NULL COMMENT '期望交货日期',
  `status` enum('open','matched','closed','expired') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'open' COMMENT '求购需求状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`demand_id`) USING BTREE,
  INDEX `idx_buyer_id`(`buyer_id` ASC) USING BTREE,
  CONSTRAINT `fk_demand_buyer` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '买家求购需求表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Expert Q&A: Questions posted by farmers, multiple answers, acceptance supported
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_expert_questions` (
  `question_id` int NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  `farmer_id` bigint NOT NULL COMMENT '提问农户用户ID，关联users表',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题内容',
  `status` enum('open','answered') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'open' COMMENT '问题状态：open（未采纳）、answered（已采纳）',
  `accepted_answer_id` int NULL DEFAULT NULL COMMENT '被采纳的回答ID（可为空）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`question_id`) USING BTREE,
  INDEX `idx_farmer_id`(`farmer_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_question_farmer` FOREIGN KEY (`farmer_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_question_accepted_answer` FOREIGN KEY (`accepted_answer_id`) REFERENCES `tb_expert_answers` (`answer_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '专家问答-问题表' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `tb_expert_answers` (
  `answer_id` int NOT NULL AUTO_INCREMENT COMMENT '回答ID',
  `question_id` int NOT NULL COMMENT '对应问题ID',
  `responder_id` bigint NOT NULL COMMENT '回答者用户ID（不限专家）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '回答内容',
  `is_accepted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否被采纳（冗余标识）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`answer_id`) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE,
  INDEX `idx_responder_id`(`responder_id` ASC) USING BTREE,
  CONSTRAINT `fk_answer_question` FOREIGN KEY (`question_id`) REFERENCES `tb_expert_questions` (`question_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_answer_user` FOREIGN KEY (`responder_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '专家问答-回答表' ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `user_application` ( 
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `user_name` VARCHAR(50) NOT NULL COMMENT '申请人账号',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密后）',
  `real_name` VARCHAR(100) DEFAULT NULL COMMENT '真实姓名',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `apply_role` VARCHAR(50) NOT NULL COMMENT '申请角色（expert/bank）',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态（pending, approved, rejected）',
  `reason` VARCHAR(255) DEFAULT NULL COMMENT '审核拒绝原因',
  `attachment_path` TEXT DEFAULT NULL COMMENT '附件路径(JSON格式，存多张图片)',
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_name_unique` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='专家/银行身份申请表';
-- 一对一聊天：会话表
CREATE TABLE IF NOT EXISTS `tb_chat_session` (
  `session_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_a_id` BIGINT NOT NULL COMMENT '用户A(较小ID)',
  `user_b_id` BIGINT NOT NULL COMMENT '用户B(较大ID)',
  `last_message_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近消息时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`session_id`) USING BTREE,
  UNIQUE KEY `uniq_user_pair` (`user_a_id`, `user_b_id`) USING BTREE,
  KEY `idx_user_a` (`user_a_id`) USING BTREE,
  KEY `idx_user_b` (`user_b_id`) USING BTREE,
  CONSTRAINT `fk_chat_session_user_a` FOREIGN KEY (`user_a_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_chat_session_user_b` FOREIGN KEY (`user_b_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='一对一聊天会话表';

-- 一对一聊天：消息表
CREATE TABLE IF NOT EXISTS `tb_chat_message` (
  `message_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` BIGINT NOT NULL COMMENT '会话ID',
  `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
  `content` VARCHAR(2000) NOT NULL COMMENT '消息内容',
  `msg_type` ENUM('text','image','auto') NOT NULL DEFAULT 'text' COMMENT '消息类型',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  `send_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`message_id`) USING BTREE,
  KEY `idx_session_time` (`session_id`, `send_time`) USING BTREE,
  KEY `idx_receiver_unread` (`receiver_id`, `is_read`) USING BTREE,
  CONSTRAINT `fk_chat_msg_session` FOREIGN KEY (`session_id`) REFERENCES `tb_chat_session` (`session_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_chat_msg_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_chat_msg_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='一对一聊天消息表';


-- 专家工作时间段
CREATE TABLE IF NOT EXISTS `tb_expert_working_slots` (
  `slot_id` INT NOT NULL AUTO_INCREMENT COMMENT '时间段ID',
  `expert_id` BIGINT NOT NULL COMMENT '专家用户ID，关联 users.user_id',
  `work_date` DATE NOT NULL COMMENT '日期',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `capacity` INT NOT NULL DEFAULT 1 COMMENT '可预约名额',
  `booked_count` INT NOT NULL DEFAULT 0 COMMENT '已预约数',
  `status` ENUM('open','closed') NOT NULL DEFAULT 'open' COMMENT '是否开放预约',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`slot_id`),
  INDEX `idx_expert_date`(`expert_id`, `work_date`),
  CONSTRAINT `fk_slot_expert_user` FOREIGN KEY (`expert_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家工作时间段';

-- 自动回复规则表（卖家配置）
CREATE TABLE IF NOT EXISTS `tb_auto_reply_rules` (
  `rule_id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `seller_id` bigint NOT NULL COMMENT '卖家用户ID',
  `keyword` varchar(100) NOT NULL COMMENT '关键词',
  `match_type` enum('contains','exact','regex') NOT NULL DEFAULT 'contains' COMMENT '匹配类型',
  `reply_text` text NOT NULL COMMENT '回复文本',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `priority` int NOT NULL DEFAULT 100 COMMENT '优先级（越小越优先）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`rule_id`),
  KEY `idx_seller` (`seller_id`),
  CONSTRAINT `fk_auto_reply_seller` FOREIGN KEY (`seller_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天自动回复规则（卖家端）';

SET FOREIGN_KEY_CHECKS = 1; -- 重新启用外键检查