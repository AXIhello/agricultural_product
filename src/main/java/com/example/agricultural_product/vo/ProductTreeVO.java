package com.example.agricultural_product.vo; // 这里改为正确的包名

import com.example.agricultural_product.pojo.Product;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品树形结构视图对象
 * 结构：一级分类 -> 二级分类 -> 产品名称 -> 商品列表
 */
@Data
public class ProductTreeVO {
    // 第一层：一级分类 (对应 prodCat，如：水果)
    private String name;
    private List<SubCategory> subCategories = new ArrayList<>();

    // 第二层：二级分类 (对应 prodPcat，如：保鲜水果) - 静态内部类
    @Data
    public static class SubCategory {
        private String name;
        private List<ProductNameGroup> productTypes = new ArrayList<>();
    }

    // 第三层：具体产品名 (对应 productName，如：草莓) - 静态内部类
    @Data
    public static class ProductNameGroup {
        private String name;
        private List<Product> products = new ArrayList<>(); // 点击进去后的具体商品列表
    }
}