-- 评价数据生成脚本
-- 为商品推荐系统生成真实的订单和评价数据

-- 首先创建一些订单数据
INSERT INTO `tb_order` (`user_id`, `order_date`, `total_amount`, `status`, `create_time`) VALUES
-- 购买者1的订单
((SELECT user_id FROM users WHERE user_name = 'buyer001' LIMIT 1), '2024-11-15 10:30:00', 58.30, 'completed', '2024-11-15 10:30:00'),
((SELECT user_id FROM users WHERE user_name = 'buyer001' LIMIT 1), '2024-11-20 14:15:00', 42.50, 'completed', '2024-11-20 14:15:00'),

-- 购买者2的订单  
((SELECT user_id FROM users WHERE user_name = 'buyer002' LIMIT 1), '2024-11-18 09:45:00', 95.80, 'completed', '2024-11-18 09:45:00'),
((SELECT user_id FROM users WHERE user_name = 'buyer002' LIMIT 1), '2024-11-25 16:20:00', 73.20, 'completed', '2024-11-25 16:20:00'),

-- 购买者3的订单
((SELECT user_id FROM users WHERE user_name = 'buyer003' LIMIT 1), '2024-11-12 11:00:00', 156.70, 'completed', '2024-11-12 11:00:00'),
((SELECT user_id FROM users WHERE user_name = 'buyer003' LIMIT 1), '2024-11-28 13:30:00', 89.40, 'completed', '2024-11-28 13:30:00'),

-- 购买者4的订单
((SELECT user_id FROM users WHERE user_name = 'buyer004' LIMIT 1), '2024-11-08 15:45:00', 127.60, 'completed', '2024-11-08 15:45:00'),
((SELECT user_id FROM users WHERE user_name = 'buyer004' LIMIT 1), '2024-11-22 10:15:00', 64.80, 'completed', '2024-11-22 10:15:00'),

-- 购买者5的订单
((SELECT user_id FROM users WHERE user_name = 'buyer005' LIMIT 1), '2024-11-10 12:20:00', 198.50, 'completed', '2024-11-10 12:20:00'),
((SELECT user_id FROM users WHERE user_name = 'buyer005' LIMIT 1), '2024-11-26 17:00:00', 112.30, 'completed', '2024-11-26 17:00:00');

-- 创建订单明细数据（需要根据实际商品ID调整）
-- 为了简化，我们用子查询获取商品ID

-- 订单1的明细（有机富士苹果 + 农家土鸡蛋）
INSERT INTO `tb_order_item` (`order_id`, `product_id`, `quantity`, `unit_price`, `status`, `is_reviewed`) VALUES
((SELECT LAST_INSERT_ID()), 
 (SELECT product_id FROM tb_product WHERE product_name = '有机富士苹果' LIMIT 1), 
 2, 25.80, 'REVIEWED', 1),
((SELECT LAST_INSERT_ID() - 9), 
 (SELECT product_id FROM tb_product WHERE product_name = '农家土鸡蛋' LIMIT 1), 
 3, 2.50, 'REVIEWED', 1);

-- 由于订单ID的复杂性，我们用更直接的方法
-- 先查询获取必要的ID，然后手动插入数据

-- 为了简化测试，我们直接创建评价数据，假设订单已存在
-- 这里我们为每个商品创建对应数量的评价

-- 1. 有机富士苹果的评价（156个评价，平均4.8分）
INSERT INTO `tb_order_item_review` (
    `item_id`, `order_id`, `product_id`, `user_id`, `rating`, `content`, 
    `is_anonymous`, `status`, `create_time`
) VALUES
-- 5分评价 (约70%)
(1001, 1001, (SELECT product_id FROM tb_product WHERE product_name = '有机富士苹果' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer001' LIMIT 1), 5, 
 '苹果非常新鲜，口感甜脆，包装也很好，五星推荐！', 0, 'published', '2024-11-16 10:00:00'),

(1002, 1002, (SELECT product_id FROM tb_product WHERE product_name = '有机富士苹果' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer002' LIMIT 1), 5, 
 '有机苹果就是不一样，甜度刚好，孩子很喜欢吃。', 0, 'published', '2024-11-19 14:30:00'),

(1003, 1003, (SELECT product_id FROM tb_product WHERE product_name = '有机富士苹果' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer003' LIMIT 1), 5, 
 '品质很棒，山东烟台的苹果确实好，会回购的。', 0, 'published', '2024-11-13 16:20:00'),

(1004, 1004, (SELECT product_id FROM tb_product WHERE product_name = '有机富士苹果' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer004' LIMIT 1), 4, 
 '苹果很甜，就是价格稍微贵一点，但是质量确实好。', 0, 'published', '2024-11-09 11:15:00'),

(1005, 1005, (SELECT product_id FROM tb_product WHERE product_name = '有机富士苹果' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer005' LIMIT 1), 5, 
 '收到的苹果很新鲜，个头大小均匀，卖家包装用心。', 0, 'published', '2024-11-11 15:45:00');

-- 2. 精品大米的评价（203个评价，平均4.9分）
INSERT INTO `tb_order_item_review` (
    `item_id`, `order_id`, `product_id`, `user_id`, `rating`, `content`, 
    `is_anonymous`, `status`, `create_time`
) VALUES
(2001, 2001, (SELECT product_id FROM tb_product WHERE product_name = '精品大米' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer001' LIMIT 1), 5, 
 '五常大米名不虚传，煮出来的米饭香味扑鼻，粒粒分明。', 0, 'published', '2024-11-16 20:00:00'),

(2002, 2002, (SELECT product_id FROM tb_product WHERE product_name = '精品大米' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer002' LIMIT 1), 5, 
 '大米质量非常好，口感软糯，全家都很喜欢。', 0, 'published', '2024-11-19 19:30:00'),

(2003, 2003, (SELECT product_id FROM tb_product WHERE product_name = '精品大米' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer003' LIMIT 1), 5, 
 '东北大米就是好，这个价格很值得，已经回购多次了。', 0, 'published', '2024-11-13 21:20:00'),

(2004, 2004, (SELECT product_id FROM tb_product WHERE product_name = '精品大米' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer004' LIMIT 1), 5, 
 '包装严实，大米很干净，没有碎米，品质很棒。', 0, 'published', '2024-11-09 18:15:00'),

(2005, 2005, (SELECT product_id FROM tb_product WHERE product_name = '精品大米' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer005' LIMIT 1), 4, 
 '大米确实不错，就是快递有点慢，其他都很满意。', 0, 'published', '2024-11-11 20:45:00');

-- 3. 新鲜草莓的评价（128个评价，平均4.7分）
INSERT INTO `tb_order_item_review` (
    `item_id`, `order_id`, `product_id`, `user_id`, `rating`, `content`, 
    `is_anonymous`, `status`, `create_time`
) VALUES
(3001, 3001, (SELECT product_id FROM tb_product WHERE product_name = '新鲜草莓' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer001' LIMIT 1), 5, 
 '草莓很新鲜，甜度刚好，个头也大，孩子很爱吃。', 0, 'published', '2024-11-17 10:00:00'),

(3002, 3002, (SELECT product_id FROM tb_product WHERE product_name = '新鲜草莓' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer002' LIMIT 1), 4, 
 '草莓品质不错，就是有几个稍微软了一点，总体满意。', 0, 'published', '2024-11-20 14:30:00'),

(3003, 3003, (SELECT product_id FROM tb_product WHERE product_name = '新鲜草莓' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer003' LIMIT 1), 5, 
 '温室草莓质量真不错，酸甜适中，包装也很用心。', 0, 'published', '2024-11-14 16:20:00'),

(3004, 3004, (SELECT product_id FROM tb_product WHERE product_name = '新鲜草莓' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer004' LIMIT 1), 4, 
 '草莓挺新鲜的，味道不错，价格合理，会再来买。', 0, 'published', '2024-11-10 11:15:00'),

(3005, 3005, (SELECT product_id FROM tb_product WHERE product_name = '新鲜草莓' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer005' LIMIT 1), 5, 
 '收到的草莓很新鲜，颜色漂亮，全家都很喜欢。', 0, 'published', '2024-11-12 15:45:00');

-- 4. 农家土鸡蛋的评价（67个评价，平均4.3分）
INSERT INTO `tb_order_item_review` (
    `item_id`, `order_id`, `product_id`, `user_id`, `rating`, `content`, 
    `is_anonymous`, `status`, `create_time`
) VALUES
(4001, 4001, (SELECT product_id FROM tb_product WHERE product_name = '农家土鸡蛋' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer001' LIMIT 1), 4, 
 '土鸡蛋确实比普通鸡蛋香，蛋黄很黄，营养价值高。', 0, 'published', '2024-11-18 10:00:00'),

(4002, 4002, (SELECT product_id FROM tb_product WHERE product_name = '农家土鸡蛋' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer002' LIMIT 1), 4, 
 '鸡蛋质量不错，包装很好，没有破损，会继续购买。', 0, 'published', '2024-11-21 14:30:00'),

(4003, 4003, (SELECT product_id FROM tb_product WHERE product_name = '农家土鸡蛋' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer003' LIMIT 1), 5, 
 '农家散养的就是不一样，蛋清浓稠，蛋黄颜色深。', 0, 'published', '2024-11-15 16:20:00'),

(4004, 4004, (SELECT product_id FROM tb_product WHERE product_name = '农家土鸡蛋' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer004' LIMIT 1), 4, 
 '土鸡蛋味道确实好，就是价格比超市贵一些。', 0, 'published', '2024-11-11 11:15:00'),

(4005, 4005, (SELECT product_id FROM tb_product WHERE product_name = '农家土鸡蛋' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer005' LIMIT 1), 4, 
 '鸡蛋新鲜度不错，煎出来很香，孩子爱吃。', 0, 'published', '2024-11-13 15:45:00');

-- 5. 普通土豆的评价（134个评价，平均3.8分，热度高但评分一般）
INSERT INTO `tb_order_item_review` (
    `item_id`, `order_id`, `product_id`, `user_id`, `rating`, `content`, 
    `is_anonymous`, `status`, `create_time`
) VALUES
(5001, 5001, (SELECT product_id FROM tb_product WHERE product_name = '普通土豆' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer001' LIMIT 1), 4, 
 '土豆价格便宜，质量一般，适合日常食用。', 0, 'published', '2024-11-19 10:00:00'),

(5002, 5002, (SELECT product_id FROM tb_product WHERE product_name = '普通土豆' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer002' LIMIT 1), 3, 
 '土豆有些发芽了，品质一般，但是价格确实便宜。', 0, 'published', '2024-11-22 14:30:00'),

(5003, 5003, (SELECT product_id FROM tb_product WHERE product_name = '普通土豆' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer003' LIMIT 1), 4, 
 '普通土豆，价格实惠，炒菜煮汤都可以。', 0, 'published', '2024-11-16 16:20:00'),

(5004, 5004, (SELECT product_id FROM tb_product WHERE product_name = '普通土豆' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer004' LIMIT 1), 3, 
 '土豆个头不太均匀，有些小，但价格便宜可以接受。', 0, 'published', '2024-11-12 11:15:00'),

(5005, 5005, (SELECT product_id FROM tb_product WHERE product_name = '普通土豆' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer005' LIMIT 1), 4, 
 '家庭日常用土豆，性价比还可以，就是包装简单了点。', 0, 'published', '2024-11-14 15:45:00');

-- 6. 野生蓝莓的评价（23个评价，平均4.9分，高评分低热度）
INSERT INTO `tb_order_item_review` (
    `item_id`, `order_id`, `product_id`, `user_id`, `rating`, `content`, 
    `is_anonymous`, `status`, `create_time`
) VALUES
(6001, 6001, (SELECT product_id FROM tb_product WHERE product_name = '野生蓝莓' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer001' LIMIT 1), 5, 
 '野生蓝莓确实不同凡响，抗氧化效果很好，值得购买。', 0, 'published', '2024-11-20 10:00:00'),

(6002, 6002, (SELECT product_id FROM tb_product WHERE product_name = '野生蓝莓' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer002' LIMIT 1), 5, 
 '山区野生的就是不一样，味道浓郁，营养价值高。', 0, 'published', '2024-11-23 14:30:00'),

(6003, 6003, (SELECT product_id FROM tb_product WHERE product_name = '野生蓝莓' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer003' LIMIT 1), 5, 
 '虽然价格贵一些，但是品质确实好，推荐给注重养生的朋友。', 0, 'published', '2024-11-17 16:20:00'),

(6004, 6004, (SELECT product_id FROM tb_product WHERE product_name = '野生蓝莓' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer004' LIMIT 1), 4, 
 '蓝莓很新鲜，就是数量不太多，希望能增加供应。', 0, 'published', '2024-11-13 11:15:00'),

(6005, 6005, (SELECT product_id FROM tb_product WHERE product_name = '野生蓝莓' LIMIT 1), 
 (SELECT user_id FROM users WHERE user_name = 'buyer005' LIMIT 1), 5, 
 '野生蓝莓确实珍贵，口感和营养都很棒，会继续关注。', 0, 'published', '2024-11-15 15:45:00');

-- 注意：以上的item_id和order_id是示例值，实际使用时需要根据真实的数据库记录调整
-- 这个脚本主要用于演示如何创建评价数据，实际部署时可能需要调整ID值