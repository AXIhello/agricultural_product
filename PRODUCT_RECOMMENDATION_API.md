# 商品推荐系统API文档

## 接口概述
基于商品评价统计字段（平均评分、评价数量）的智能推荐算法，为用户提供个性化商品推荐。

## 推荐算法说明

### 核心评分体系
推荐系统采用多维度评分机制：

1. **质量分数** (Quality Score): 基于平均评分 (average_rating)
   - 计算公式: `averageRating × 20` (5星制转100分制)
   - 权重: 60%

2. **热度分数** (Popularity Score): 基于评价数量 (rating_count)
   - 计算公式: `log(ratingCount + 1) × 10`
   - 权重: 40%

3. **综合推荐分数**:
   ```
   推荐分数 = 评分 × 0.6 + 热度因子 × 2.0 × 0.4
   其中热度因子 = min(评价数量/100.0, 1.0)
   ```

### 评价等级划分
- **卓越**: ≥4.8分
- **优秀**: ≥4.5分
- **良好**: ≥4.0分
- **一般**: ≥3.5分
- **较差**: <3.5分

## API接口详情

### 1. 智能推荐商品
**接口URL:** `GET /api/products/recommend`

**描述:** 基于评价统计的智能推荐算法，综合考虑商品质量和热度。

**请求参数:**
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页大小 |
| category | String | 否 | - | 商品类别过滤 |

**请求头:**
```
Authorization: Bearer <JWT_TOKEN> (可选，用于个性化推荐)
```

**响应示例:**
```json
{
    "current": 1,
    "size": 10,
    "total": 25,
    "records": [
        {
            "productId": 123,
            "productName": "优质苹果",
            "description": "新鲜甜美的红富士苹果",
            "price": 15.80,
            "stock": 100,
            "farmerId": 456,
            "status": "active",
            "imagePath": "/uploads/apple.jpg",
            "prodCat": "水果",
            "prodPcat": "苹果",
            "place": "山东烟台",
            "averageRating": 4.8,
            "ratingCount": 126,
            "recommendScore": 3.68,
            "recommendReason": "高评分商品，热销商品，现货充足，产地直供",
            "popularityScore": 48.7,
            "qualityScore": 96.0,
            "ratingLevel": "卓越",
            "hasStock": true,
            "createTime": "2024-01-15T10:30:00"
        }
    ]
}
```

### 2. 热销商品推荐
**接口URL:** `GET /api/products/hot`

**描述:** 按评价数量排序的热销商品推荐。

**请求参数:**
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页大小 |

**响应格式:** 同智能推荐接口

### 3. 高评分商品推荐
**接口URL:** `GET /api/products/high-rated`

**描述:** 按平均评分排序的高质量商品推荐。

**请求参数:**
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页大小 |
| minRatingCount | Integer | 否 | 5 | 最小评价数过滤 |

**过滤条件:**
- 平均评分 ≥ 4.0
- 评价数量 ≥ minRatingCount
- 有库存 (stock > 0)
- 状态为活跃 (status = "active")

## 使用示例

### 1. 获取智能推荐
```bash
curl -X GET "http://localhost:8080/api/products/recommend?pageNum=1&pageSize=10&category=水果" \
  -H "Authorization: Bearer your_jwt_token"
```

### 2. 获取热销商品
```bash
curl -X GET "http://localhost:8080/api/products/hot?pageNum=1&pageSize=5"
```

### 3. 获取高评分商品
```bash
curl -X GET "http://localhost:8080/api/products/high-rated?pageNum=1&pageSize=10&minRatingCount=10"
```

## 推荐理由生成规则

系统会根据商品特征自动生成推荐理由：

- **高评分商品**: 平均评分 ≥ 4.5
- **热销商品**: 评价数量 ≥ 50
- **销量不错**: 评价数量 ≥ 20
- **现货充足**: 库存 > 0
- **产地直供**: 有产地信息

## 数据库字段依赖

推荐算法依赖以下字段：
- `average_rating`: 商品平均评分 (0.0-5.0)
- `rating_count`: 评价数量
- `stock`: 库存数量
- `status`: 商品状态
- `prod_cat`: 商品大类
- `prod_pcat`: 商品小类
- `place`: 产地信息

## 性能优化

1. **数据库索引建议:**
   ```sql
   CREATE INDEX idx_product_recommend 
   ON tb_product(status, average_rating DESC, rating_count DESC);
   
   CREATE INDEX idx_product_category_rating 
   ON tb_product(prod_cat, average_rating DESC, rating_count DESC);
   ```

2. **缓存策略:**
   - 热销商品列表可缓存30分钟
   - 高评分商品列表可缓存1小时
   - 智能推荐结果可缓存15分钟

## 注意事项

1. 只推荐状态为 "active" 的商品
2. 只推荐有库存的商品 (stock > 0)
3. 只推荐有评价数据的商品
4. 支持按商品类别过滤
5. 推荐分数实时计算，反映最新评价状态
6. JWT token可选，用于未来个性化推荐扩展