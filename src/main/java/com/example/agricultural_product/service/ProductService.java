package com.example.agricultural_product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.agricultural_product.dto.ProductCreateDTO;
import com.example.agricultural_product.dto.ProductUpdateDTO;
import com.example.agricultural_product.pojo.Product;
import com.example.agricultural_product.service.impl.ProductServiceImpl;

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


	// 根据商品状态，查询去重后的商品名称列表。
	Page<Product> getProductsByStatusPage( String status, String productName, Integer pageNum, Integer pageSize);

	// 根据农户ID和商品状态，查询去重后的商品名称列表。
	Page<Product> getProductsByFarmerIdAndStatusPage(Long farmerId, String status, String productName, Integer pageNum, Integer pageSize);

	// 根据商品状态，查询去重后的商品名称列表。先返回名字，再返回对应名字
	List<String> getDistinctProductNamesByFarmerIdAndStatus(String status);

	// 根据农户ID和商品状态，查询去重后的商品名称列表。
	List<String> getDistinctProductNamesByFarmerIdAndStatus(Long farmerId, String status);



}