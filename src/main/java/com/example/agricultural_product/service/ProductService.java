package com.example.agricultural_product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.agricultural_product.dto.ProductCreateDTO;
import com.example.agricultural_product.dto.ProductUpdateDTO;
import com.example.agricultural_product.dto.RecommendedProductDTO;
import com.example.agricultural_product.pojo.Product;
import com.example.agricultural_product.service.impl.ProductServiceImpl;
import com.example.agricultural_product.vo.ProductTreeVO;

import java.util.List;


public interface ProductService extends IService<Product> {
	// 分页获取商品列表
	Page<Product> getProductList(Integer pageNum, Integer pageSize);
	
	// 根据名称搜索商品
	Page<Product> searchProducts(String keyword, Integer pageNum, Integer pageSize);

	// 获取某个农户的商品列表
	Page<Product> getProductsByFarmerId(Long farmerId, Integer pageNum, Integer pageSize);

	// 发布商品
	boolean publishProduct(Product product);
	

	// 修改商品库存
	boolean updateStock(Integer productId, Integer newStock);

	//修改图片
	boolean updateImagePath(Integer productId, Long farmerId, String newImagePath);

	//修改信息
	boolean updateProductInfo(ProductUpdateDTO updateDTO, Long farmerId);

	 //删除商品（通常是逻辑删除或标记为已删除状态）。
	boolean deleteProduct(Integer productId, Long farmerId);

	//快捷更新商品状态（上架/下架）。
	boolean updateProductStatus(Integer productId, Long farmerId, String newStatus);


	//查询商品，"active"（上架）、"inactive"（下架）或 null（全部）
	Page<Product> getAllProductsListByStatus(String status, Integer pageNum, Integer pageSize);

	//农户查询商品，"active"（上架）、"inactive"（下架）或 null（全部）
	Page<Product> getProductsByFarmerIdAndStatusPage(Long farmerId, String status, Integer pageNum, Integer pageSize);


	// 返回按 product_name 去重后的代表性 Product 列表（每个名字只保留一条）
	Page<Product> selectDistinctProductsByStatusPage(String status, Integer pageNum, Integer pageSize);
	// 根据商品状态，查询去重后的商品名称列表。
	Page<Product> getProductsByStatusPage( String status, String productName, Integer pageNum, Integer pageSize);

	Page<Product> selectDistinctProductsByFarmerIdAndStatusPage(Long farmerId, String status, Integer pageNum, Integer pageSize);

	// 根据农户ID和商品状态，查询去重后的商品名称列表。
	Page<Product> getProductsByFarmerIdAndStatusPage(Long farmerId, String status, String productName, Integer pageNum, Integer pageSize);


	// 添加树分类结构，按照一级分类 -> 二级分类 -> 产品名称 -> 商品列表，用户可见
	List<ProductTreeVO> getProductTreeByStatus(String status);

	// 添加树分类结构，按照一级分类 -> 二级分类 -> 产品名称 -> 商品列表，农户可见
	List<ProductTreeVO> getProductTreeByFarmerIdAndStatus(Long farmerId, String status);

	/**
	 * 智能推荐商品
	 * 基于评价统计字段的推荐算法
	 * 
	 * @param userId 用户ID（可选，用于个性化推荐）
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @param category 商品类别（可选过滤）
	 * @return 推荐商品列表
	 */
	Page<RecommendedProductDTO> recommendProducts(Long userId, Integer pageNum, Integer pageSize, String category);

	/**
	 * 获取热销商品推荐
	 * 基于评价数量排序
	 * 
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @return 热销商品列表
	 */
	Page<RecommendedProductDTO> getHotProducts(Integer pageNum, Integer pageSize);

	/**
	 * 获取高评分商品推荐
	 * 基于平均评分排序
	 * 
	 * @param pageNum 页码
	 * @param pageSize 页大小
	 * @param minRatingCount 最小评价数（过滤条件）
	 * @return 高评分商品列表
	 */
	Page<RecommendedProductDTO> getHighRatedProducts(Integer pageNum, Integer pageSize, Integer minRatingCount);

}