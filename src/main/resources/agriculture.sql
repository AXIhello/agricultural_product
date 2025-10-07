-- src/main/resources/agriculture.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0; -- 禁用外键检查，方便创建表结构

-- ----------------------------
-- Table structure for users
-- ----------------------------
CREATE TABLE IF NOT EXISTS `users`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号，用于登录',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密后的密码',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `user_name`(`user_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1011 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表 (基础信息，采用用户指定结构)' ROW_FORMAT = Dynamic;

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
CREATE TABLE IF NOT EXISTS `tb_product`  (
  `product_id` int NOT NULL AUTO_INCREMENT COMMENT '产品ID',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '产品名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '产品描述',
  `price` decimal(10, 2) NOT NULL COMMENT '价格',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存',
  `farmer_id` bigint NOT NULL COMMENT '农户用户ID，关联users表',
  `status` enum('active','inactive') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'active' COMMENT '产品状态：active（上架）、inactive（下架）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`product_id`) USING BTREE,
  INDEX `idx_farmer_id`(`farmer_id` ASC) USING BTREE,
  CONSTRAINT `fk_product_farmer` FOREIGN KEY (`farmer_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 10005 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '农产品信息表 (基础信息)' ROW_FORMAT = Dynamic;

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
  `consultation_id` int NOT NULL AUTO_INCREMENT COMMENT '咨询ID',
  `farmer_id` bigint NOT NULL COMMENT '农户用户ID，关联users表',
  `expert_id` bigint NOT NULL COMMENT '专家用户ID，关联users表',
  `consultation_time` datetime NOT NULL COMMENT '预约的咨询时间',
  `status` enum('scheduled','completed','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'scheduled' COMMENT '状态：scheduled（已预约）、completed（已完成）、cancelled（已取消）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`consultation_id`) USING BTREE,
  INDEX `idx_farmer_id`(`farmer_id` ASC) USING BTREE,
  INDEX `idx_expert_id`(`expert_id` ASC) USING BTREE,
  CONSTRAINT `fk_consultation_expert` FOREIGN KEY (`expert_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_consultation_farmer` FOREIGN KEY (`farmer_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '专家咨询预约表 (基础信息)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for tb_financing
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_financing`  (
  `financing_id` int NOT NULL AUTO_INCREMENT COMMENT '融资ID',
  `initiating_farmer_id` bigint NOT NULL COMMENT '发起融资申请的主农户/联系人ID，关联users表',
  `amount` decimal(12, 2) NOT NULL COMMENT '申请融资金额',
  `purpose` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '融资用途描述',
  `application_status` enum('draft','submitted','under_review_by_banks','offers_received','offer_accepted','loan_disbursed','completed','cancelled','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'draft' COMMENT '融资申请整体状态：草稿、已提交、银行审核中、已收到报价、已接受报价、贷款已发放、已完成、已取消、已拒绝',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`financing_id`) USING BTREE,
  INDEX `idx_initiating_farmer_id`(`initiating_farmer_id` ASC) USING BTREE,
  CONSTRAINT `fk_financing_initiator` FOREIGN KEY (`initiating_farmer_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '融资信息表 (支持联合申请和多银行匹配)' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for tb_financing_farmers
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_financing_farmers`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `financing_id` int NOT NULL COMMENT '融资申请ID，关联tb_financing表',
  `farmer_id` bigint NOT NULL COMMENT '参与该融资申请的农户ID，关联users表',
  `role_in_financing` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'co_applicant' COMMENT '农户在该融资中的角色（如：主申请人、共同申请人、担保人）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_financing_farmer`(`financing_id` ASC, `farmer_id` ASC) USING BTREE,
  INDEX `idx_financing_id`(`financing_id` ASC) USING BTREE,
  INDEX `idx_farmer_id`(`farmer_id` ASC) USING BTREE,
  CONSTRAINT `fk_ff_farmer` FOREIGN KEY (`farmer_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_ff_financing` FOREIGN KEY (`financing_id`) REFERENCES `tb_financing` (`financing_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '融资申请参与农户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for tb_financing_offers
-- ----------------------------
CREATE TABLE IF NOT EXISTS `tb_financing_offers`  (
  `offer_id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `financing_id` int NOT NULL COMMENT '融资申请ID，关联tb_financing表',
  `bank_user_id` bigint NOT NULL COMMENT '提供报价/审批的银行用户ID，关联users表',
  `offered_amount` decimal(12, 2) NULL DEFAULT NULL COMMENT '银行愿意提供的金额',
  `offered_interest_rate` decimal(5, 4) NULL DEFAULT NULL COMMENT '银行提供的年利率（例如0.05表示5%）',
  `offer_status` enum('pending_review','offered','accepted_by_farmer','rejected_by_farmer','cancelled_by_bank','loan_agreement_signed') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'offered' COMMENT '报价状态：待农户审核、已报价、农户已接受、农户已拒绝、银行已取消、贷款协议已签署',
  `bank_notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '银行的附加说明或条件',
  `offer_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报价/响应时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`offer_id`) USING BTREE,
  UNIQUE INDEX `idx_financing_bank_offer`(`financing_id` ASC, `bank_user_id` ASC) USING BTREE,
  INDEX `idx_financing_id`(`financing_id` ASC) USING BTREE,
  INDEX `idx_bank_user_id`(`bank_user_id` ASC) USING BTREE,
  CONSTRAINT `fk_fo_bank` FOREIGN KEY (`bank_user_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_fo_financing` FOREIGN KEY (`financing_id`) REFERENCES `tb_financing` (`financing_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '融资银行报价/匹配表' ROW_FORMAT = DYNAMIC;

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
  `unit_desired` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'kg' COMMENT '期望单位（如：kg, 斤, 个, 箱）',
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

SET FOREIGN_KEY_CHECKS = 1; -- 重新启用外键检查