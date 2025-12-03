-- 商品推荐系统测试数据
-- 用于测试基于评价统计的推荐算法

-- 首先插入一些农户用户（如果不存在）
INSERT IGNORE INTO `users` (`user_name`, `password`, `name`, `email`, `role`, `region`, `credit_score`) VALUES
('farmer001', '$2a$10$encrypted_password_hash', '张农户', 'zhang@farm.com', 'farmer', '山东省济南市', 85),
('farmer002', '$2a$10$encrypted_password_hash', '李农户', 'li@farm.com', 'farmer', '山东省青岛市', 78),
('farmer003', '$2a$10$encrypted_password_hash', '王农户', 'wang@farm.com', 'farmer', '江苏省南京市', 92),
('farmer004', '$2a$10$encrypted_password_hash', '赵农户', 'zhao@farm.com', 'farmer', '浙江省杭州市', 88),
('farmer005', '$2a$10$encrypted_password_hash', '陈农户', 'chen@farm.com', 'farmer', '广东省广州市', 76);

-- 插入测试商品数据（包含评价统计字段）
INSERT INTO `tb_product` (
    `product_name`, `description`, `price`, `stock`, `farmer_id`, `status`, 
    `image_path`, `prodCat`, `prodPcat`, `unitInfo`, `specInfo`, `place`,
    `average_rating`, `rating_count`
) VALUES
-- 高评分高热度商品
('有机富士苹果', '新鲜采摘的有机富士苹果，口感甜脆，营养丰富', 25.80, 500, 
 (SELECT user_id FROM users WHERE user_name = 'farmer001' LIMIT 1), 'active', 
 '/uploads/products/apple_001.jpg', '水果', '苹果', '斤', '特级', '山东烟台', 4.8, 156),

('精品大米', '东北优质大米，粒粒饱满，香味浓郁', 8.50, 1000, 
 (SELECT user_id FROM users WHERE user_name = 'farmer002' LIMIT 1), 'active', 
 '/uploads/products/rice_001.jpg', '粮食', '大米', '斤', '一级', '黑龙江五常', 4.9, 203),

('新鲜草莓', '温室培育的新鲜草莓，酸甜可口', 35.00, 200, 
 (SELECT user_id FROM users WHERE user_name = 'farmer003' LIMIT 1), 'active', 
 '/uploads/products/strawberry_001.jpg', '水果', '草莓', '斤', '特级', '江苏南京', 4.7, 128),

-- 中等评分中等热度商品
('农家土鸡蛋', '散养土鸡蛋，营养价值高', 2.50, 800, 
 (SELECT user_id FROM users WHERE user_name = 'farmer001' LIMIT 1), 'active', 
 '/uploads/products/egg_001.jpg', '畜产品', '鸡蛋', '个', '土鸡蛋', '山东济南', 4.3, 67),

('新鲜白菜', '无公害白菜，绿色健康', 3.20, 600, 
 (SELECT user_id FROM users WHERE user_name = 'farmer004' LIMIT 1), 'active', 
 '/uploads/products/cabbage_001.jpg', '蔬菜', '白菜', '斤', '优质', '浙江杭州', 4.1, 45),

('有机胡萝卜', '有机种植胡萝卜，营养丰富', 6.80, 400, 
 (SELECT user_id FROM users WHERE user_name = 'farmer002' LIMIT 1), 'active', 
 '/uploads/products/carrot_001.jpg', '蔬菜', '胡萝卜', '斤', '有机', '山东青岛', 4.4, 89),

-- 高评分低热度商品（新品或小众商品）
('野生蓝莓', '山区野生蓝莓，抗氧化效果好', 58.00, 150, 
 (SELECT user_id FROM users WHERE user_name = 'farmer005' LIMIT 1), 'active', 
 '/uploads/products/blueberry_001.jpg', '水果', '蓝莓', '斤', '野生', '广东韶关', 4.9, 23),

('古法黑芝麻', '传统工艺制作的黑芝麻', 28.00, 180, 
 (SELECT user_id FROM users WHERE user_name = 'farmer003' LIMIT 1), 'active', 
 '/uploads/products/sesame_001.jpg', '干货', '芝麻', '斤', '古法制作', '江苏南京', 4.6, 18),

-- 低评分高热度商品（价格便宜但质量一般）
('普通土豆', '日常食用土豆，价格实惠', 2.80, 1200, 
 (SELECT user_id FROM users WHERE user_name = 'farmer004' LIMIT 1), 'active', 
 '/uploads/products/potato_001.jpg', '蔬菜', '土豆', '斤', '普通', '浙江杭州', 3.8, 134),

('一般苹果', '普通苹果，价格亲民', 12.00, 800, 
 (SELECT user_id FROM users WHERE user_name = 'farmer001' LIMIT 1), 'active', 
 '/uploads/products/apple_002.jpg', '水果', '苹果', '斤', '普通', '山东济南', 3.5, 98),

-- 更多多样化商品
('有机番茄', '有机种植番茄，口感鲜美', 8.50, 300, 
 (SELECT user_id FROM users WHERE user_name = 'farmer002' LIMIT 1), 'active', 
 '/uploads/products/tomato_001.jpg', '蔬菜', '番茄', '斤', '有机', '山东青岛', 4.5, 76),

('新鲜玉米', '甜玉米，适合煮食', 4.50, 500, 
 (SELECT user_id FROM users WHERE user_name = 'farmer005' LIMIT 1), 'active', 
 '/uploads/products/corn_001.jpg', '蔬菜', '玉米', '根', '甜玉米', '广东广州', 4.2, 52),

('精品花生', '优质花生，粒大饱满', 12.80, 400, 
 (SELECT user_id FROM users WHERE user_name = 'farmer003' LIMIT 1), 'active', 
 '/uploads/products/peanut_001.jpg', '干货', '花生', '斤', '精品', '江苏南京', 4.4, 63),

('农家蜂蜜', '纯天然蜂蜜，甜度适中', 68.00, 100, 
 (SELECT user_id FROM users WHERE user_name = 'farmer001' LIMIT 1), 'active', 
 '/uploads/products/honey_001.jpg', '特产', '蜂蜜', '瓶', '纯天然', '山东济南', 4.8, 41),

('新鲜莴苣', '嫩绿莴苣，爽脆可口', 5.20, 350, 
 (SELECT user_id FROM users WHERE user_name = 'farmer004' LIMIT 1), 'active', 
 '/uploads/products/lettuce_001.jpg', '蔬菜', '莴苣', '斤', '新鲜', '浙江杭州', 4.1, 38),

-- 一些库存较少的商品
('特级核桃', '优质核桃，营养价值高', 45.00, 80, 
 (SELECT user_id FROM users WHERE user_name = 'farmer005' LIMIT 1), 'active', 
 '/uploads/products/walnut_001.jpg', '干货', '核桃', '斤', '特级', '广东韶关', 4.7, 29),

('有机小白菜', '有机小白菜，绿色健康', 6.80, 200, 
 (SELECT user_id FROM users WHERE user_name = 'farmer002' LIMIT 1), 'active', 
 '/uploads/products/bok_choy_001.jpg', '蔬菜', '小白菜', '斤', '有机', '山东青岛', 4.3, 34),

('山区红薯', '山区种植红薯，香甜软糯', 4.20, 600, 
 (SELECT user_id FROM users WHERE user_name = 'farmer003' LIMIT 1), 'active', 
 '/uploads/products/sweet_potato_001.jpg', '蔬菜', '红薯', '斤', '山区种植', '江苏南京', 4.2, 87),

('新鲜韭菜', '新鲜韭菜，香味浓郁', 8.50, 150, 
 (SELECT user_id FROM users WHERE user_name = 'farmer001' LIMIT 1), 'active', 
 '/uploads/products/chives_001.jpg', '蔬菜', '韭菜', '斤', '新鲜', '山东济南', 4.0, 26),

('农家小米', '优质小米，营养丰富', 18.00, 300, 
 (SELECT user_id FROM users WHERE user_name = 'farmer004' LIMIT 1), 'active', 
 '/uploads/products/millet_001.jpg', '粮食', '小米', '斤', '优质', '浙江杭州', 4.6, 55);

-- 插入一些购买用户（用于生成评价数据）
INSERT IGNORE INTO `users` (`user_name`, `password`, `name`, `email`, `role`, `region`) VALUES
('buyer001', '$2a$10$encrypted_password_hash', '购买者一', 'buyer1@test.com', 'buyer', '北京市'),
('buyer002', '$2a$10$encrypted_password_hash', '购买者二', 'buyer2@test.com', 'buyer', '上海市'),
('buyer003', '$2a$10$encrypted_password_hash', '购买者三', 'buyer3@test.com', 'buyer', '广州市'),
('buyer004', '$2a$10$encrypted_password_hash', '购买者四', 'buyer4@test.com', 'buyer', '深圳市'),
('buyer005', '$2a$10$encrypted_password_hash', '购买者五', 'buyer5@test.com', 'buyer', '杭州市');

-- 查询刚插入的商品和用户ID，为后续评价数据做准备
-- 注意：以下是查询语句，用于验证数据插入情况
-- SELECT product_id, product_name, average_rating, rating_count FROM tb_product WHERE create_time >= CURDATE();
-- SELECT user_id, user_name, role FROM users WHERE user_name LIKE 'buyer%' OR user_name LIKE 'farmer%';