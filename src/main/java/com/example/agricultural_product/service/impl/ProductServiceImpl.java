package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agricultural_product.dto.ProductUpdateDTO;
import com.example.agricultural_product.mapper.ProductMapper;
import com.example.agricultural_product.pojo.Product;
import com.example.agricultural_product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.agricultural_product.vo.ProductTreeVO;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

	@Autowired
	private ProductMapper productMapper;

	private void normalizeImagePath(Page<Product> page) {
		if (page == null || page.getRecords() == null) return;

		page.getRecords().forEach(p -> {

			if (p != null){
				String path = p.getImagePath();

				boolean hasImage = path != null && !path.trim().isEmpty();

				p.setHasImage(hasImage);

				if(!hasImage){
					p.setImagePath(null);
				}	

			}
		});
	}

	@Override
	public Page<Product> getProductList(Integer pageNum, Integer pageSize) {
		Page<Product> page = new Page<>(pageNum, pageSize);
		LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(Product::getStatus, "active")
		       .orderByDesc(Product::getCreateTime);
		Page<Product> result = page(page, wrapper);
		normalizeImagePath(result);
		return result;
	}

	@Override
	public Page<Product> searchProducts(String keyword, Integer pageNum, Integer pageSize) {
		Page<Product> page = new Page<>(pageNum, pageSize);
		LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(Product::getStatus, "active")
		       .like(Product::getProductName, keyword)
		       .orderByDesc(Product::getCreateTime);
		Page<Product> result = page(page, wrapper);
		normalizeImagePath(result);
		return result;
	}

	public Page<Product> selectDistinctProductsByStatusPage(String status, Integer pageNum, Integer pageSize) {
		int offset = (pageNum - 1) * pageSize;
		List<Product> list = productMapper.selectDistinctProductsByStatus(status, offset, pageSize);

		Page<Product> page = new Page<>();
		page.setRecords(list);
		page.setCurrent(pageNum);
		page.setSize(pageSize);
		page.setTotal(productMapper.countDistinctProductsByStatus(status)); // 计算总条数
		return page;
	}

	public Page<Product> selectDistinctProductsByFarmerIdAndStatusPage(Long farmerId, String status, Integer pageNum, Integer pageSize) {
		int offset = (pageNum - 1) * pageSize;
		List<Product> list = productMapper.selectDistinctProductsByFarmerIdAndStatus(farmerId, status, offset, pageSize);

		Page<Product> page = new Page<>();
		page.setRecords(list);
		page.setCurrent(pageNum);
		page.setSize(pageSize);
		page.setTotal(productMapper.countDistinctProductsByFarmerIdAndStatus(farmerId, status));
		return page;
	}



	@Override
	public Page<Product> getProductsByFarmerId(Long farmerId, Integer pageNum, Integer pageSize) {
		Page<Product> page = new Page<>(pageNum, pageSize);
		LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(Product::getFarmerId, farmerId)
		       .eq(Product::getStatus, "active")
		       .orderByDesc(Product::getCreateTime);
		Page<Product> result = page(page, wrapper);
		normalizeImagePath(result);
		return result;
	}

	@Override
	public boolean publishProduct(Product product) {
		if (product == null) {
			return false;
		}
		// 默认发布为 active，设置创建和更新时间
		if (product.getStatus() == null) {
			product.setStatus("active");
		}
		LocalDateTime now = LocalDateTime.now();
		if (product.getCreateTime() == null) {
			product.setCreateTime(now);
		}
		product.setUpdateTime(now);
		return this.save(product);
	}

	@Override
	public boolean updateStock(Integer productId, Integer newStock) {
		if (productId == null || newStock == null || newStock < 0) {
			return false;
		}
		Product product = getById(productId);
		if (product == null) {
			return false;
		}
		product.setStock(newStock);
		product.setUpdateTime(LocalDateTime.now());
		return updateById(product);
	}

	@Override
	@Transactional // 确保更新操作的原子性
	public boolean updateImagePath(Integer productId, Long farmerId, String newImagePath) {
		if (productId == null || farmerId == null || newImagePath == null) {
			return false;
		}

		Product product = getById(productId);

		// 1. 验证商品是否存在
		if (product == null) {
			// 商品不存在
			return false;
		}

		// 2. 验证权限：确保修改者是该商品的农户
		if (!product.getFarmerId().equals(farmerId)) {
			// 权限不足 (如需更详细的日志，可以在这里记录)
			return false;
		}

		// 3. 更新图片路径和更新时间
		product.setImagePath(newImagePath);
		// updateTime 会由数据库的 ON UPDATE CURRENT_TIMESTAMP 自动更新，
		// 但在 Service 层手动设置可以保证逻辑的可见性和跨数据库的兼容性。
		product.setUpdateTime(LocalDateTime.now());

		return updateById(product);
	}


	@Override
	@Transactional
	public boolean updateProductInfo(ProductUpdateDTO updateDTO, Long farmerId) {
		if (updateDTO == null || updateDTO.getProductId() == null || farmerId == null) {
			return false;
		}

		Product existingProduct = getById(updateDTO.getProductId());

		// 1. 检查商品是否存在
		if (existingProduct == null) {
			return false;
		}

		// 2. 权限验证：确保操作者是商品的农户
		if (!existingProduct.getFarmerId().equals(farmerId)) {
			return false; // 403 Forbidden
		}

		// 3. 复制 DTO 中非空字段到现有实体
		// 注意：使用 BeanUtils.copyProperties 时需要小心，如果 DTO 字段为 null，会覆盖实体字段。
		// 推荐手动设置，或使用工具类/BeanUtils 的扩展来忽略 null 值。

		// 这里使用手动设置/判断来避免 null 覆盖非空值，并只更新允许修改的字段。
		if (updateDTO.getProductName() != null) {
			existingProduct.setProductName(updateDTO.getProductName());
		}
		if (updateDTO.getDescription() != null) {
			existingProduct.setDescription(updateDTO.getDescription());
		}
		if (updateDTO.getPrice() != null) {
			existingProduct.setPrice(updateDTO.getPrice());
		}
		if (updateDTO.getStock() != null) {
			existingProduct.setStock(updateDTO.getStock());
		}
		if (updateDTO.getStatus() != null && ("active".equals(updateDTO.getStatus()) || "inactive".equals(updateDTO.getStatus()))) {
			existingProduct.setStatus(updateDTO.getStatus());
		}
		if (updateDTO.getProdCat() != null) {
			existingProduct.setProdCat(updateDTO.getProdCat());
		}
		if (updateDTO.getProdPcat() != null) {
			existingProduct.setProdPcat(updateDTO.getProdPcat());
		}
		if (updateDTO.getUnitInfo() != null) {
			existingProduct.setUnitInfo(updateDTO.getUnitInfo());
		}
		if (updateDTO.getSpecInfo() != null) {
			existingProduct.setSpecInfo(updateDTO.getSpecInfo());
		}
		if (updateDTO.getPlace() != null) {
			existingProduct.setPlace(updateDTO.getPlace());
		}

		// 4. 设置更新时间
		existingProduct.setUpdateTime(LocalDateTime.now());

		// 5. 执行更新
		return updateById(existingProduct);
	}

	@Override
	@Transactional
	public boolean deleteProduct(Integer productId, Long farmerId) {
		if (productId == null || farmerId == null) {
			return false;
		}

		Product product = getById(productId);

		// 1. 检查商品是否存在
		if (product == null) {
			return false;
		}

		// 2. 权限验证：确保操作者是商品的农户
		if (!product.getFarmerId().equals(farmerId)) {
			return false; // 权限不足
		}

		// 3. 执行物理删除
		// 注意：在实际生产环境中，更推荐使用逻辑删除（即添加一个 is_deleted 字段并设置它）
		return removeById(productId);

		// 如果使用逻辑删除，代码应为:
    /*
    product.setStatus("deleted"); // 或者设置 is_deleted = true
    product.setUpdateTime(LocalDateTime.now());
    return updateById(product);
    */
	}

	@Override
	@Transactional
	public boolean updateProductStatus(Integer productId, Long farmerId, String newStatus) {
		if (productId == null || farmerId == null || newStatus == null ||
				(!newStatus.equals("active") && !newStatus.equals("inactive"))) {
			return false;
		}

		// 1. 验证权限和存在性：先通过查询来确定该商品确实属于该农户
		LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(Product::getProductId, productId)
				.eq(Product::getFarmerId, farmerId);

		// 2. 执行更新
		Product updateProduct = new Product();
		updateProduct.setUpdateTime(LocalDateTime.now());
		updateProduct.setStatus(newStatus);

		// 3. 使用 Mybatis-Plus 的 update(entity, wrapper) 执行轻量级更新
		// 只有当查询条件 (productId 和 farmerId) 都满足时，更新才会成功
		return this.update(updateProduct, wrapper);

		// 或者先 getById() 然后进行权限判断并 updateById()，但上面的方式更高效。
	}


	@Override
	public Page<Product> getAllProductsListByStatus(String status, Integer pageNum, Integer pageSize) {
		Page<Product> page = new Page<>(pageNum, pageSize);
		LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

		// 1. 状态过滤逻辑
		if (status != null) {
			String lowerStatus = status.toLowerCase();
			if ("active".equals(lowerStatus) || "inactive".equals(lowerStatus)) {
				wrapper.eq(Product::getStatus, lowerStatus);
			}
			// 否则，status 视为无效，查询所有状态 (或根据实际需求返回空列表/抛异常)
		}

		// 2. 排序
		wrapper.orderByDesc(Product::getCreateTime);

		Page<Product> result = page(page, wrapper);
		normalizeImagePath(result); // 使用您的图片路径规范化方法
		return result;
	}

	// 新增方法：根据农户ID和商品状态查询商品列表（带分页）
	// 此方法可以替换您原有的 getProductsByFarmerId 方法，使其支持所有状态
	@Override
	public Page<Product> getProductsByFarmerIdAndStatusPage(Long farmerId, String status, Integer pageNum, Integer pageSize) {
		Page<Product> page = new Page<>(pageNum, pageSize);
		LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

		// 1. 核心过滤条件：农户ID
		wrapper.eq(Product::getFarmerId, farmerId);

		// 2. 状态过滤逻辑
		if (status != null) {
			String lowerStatus = status.toLowerCase();
			if ("active".equals(lowerStatus) || "inactive".equals(lowerStatus)) {
				wrapper.eq(Product::getStatus, lowerStatus);
			}
		}

		// 3. 排序
		wrapper.orderByDesc(Product::getCreateTime);

		Page<Product> result = page(page, wrapper);
		normalizeImagePath(result); // 使用您的图片路径规范化方法
		return result;
	}


	@Override
	public Page<Product> getProductsByStatusPage(String status, String productName, Integer pageNum, Integer pageSize) {
		Page<Product> page = new Page<>(pageNum, pageSize);
		LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();


		// 1. 状态过滤
		if (status != null) {
			String lowerStatus = status.toLowerCase();
			if ("active".equals(lowerStatus) || "inactive".equals(lowerStatus)) {
				wrapper.eq(Product::getStatus, lowerStatus);
			}
		}

		// 2. 新增名称过滤 (精确匹配)
		if (productName != null && !productName.trim().isEmpty()) {
			wrapper.eq(Product::getProductName, productName); // 关键新增行
		}

		wrapper.orderByDesc(Product::getCreateTime);

		Page<Product> result = page(page, wrapper);
		normalizeImagePath(result);
		return result;
	}


	@Override
	public Page<Product> getProductsByFarmerIdAndStatusPage(Long farmerId, String status, String productName, Integer pageNum, Integer pageSize) {
		Page<Product> page = new Page<>(pageNum, pageSize);
		LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

		wrapper.eq(Product::getFarmerId, farmerId);

		// 1. 状态过滤
		if (status != null) {
			String lowerStatus = status.toLowerCase();
			if ("active".equals(lowerStatus) || "inactive".equals(lowerStatus)) {
				wrapper.eq(Product::getStatus, lowerStatus);
			}
		}

		// 2. 新增名称过滤 (精确匹配)
		if (productName != null && !productName.trim().isEmpty()) {
			wrapper.eq(Product::getProductName, productName); // 关键新增行
		}

		wrapper.orderByDesc(Product::getCreateTime);

		Page<Product> result = page(page, wrapper);
		normalizeImagePath(result);
		return result;
	}


	// 【通用私有方法】将 List<Product> 转换为 List<ProductTreeVO>，(把这段复杂的逻辑提出来，给下面两个方法共用)
	private List<ProductTreeVO> buildTreeFromList(List<Product> products) {
		if (products == null || products.isEmpty()) {
			return new ArrayList<>();
		}

		// 屏蔽图片路径
		products.forEach(p -> { if (p != null) p.setImagePath(null); });

		// 分组：一级 -> 二级 -> 名称 -> 列表
		Map<String, Map<String, Map<String, List<Product>>>> groupedMap = products.stream()
				.collect(Collectors.groupingBy(p -> p.getProdCat() == null ? "其他" : p.getProdCat(),
						Collectors.groupingBy(p -> p.getProdPcat() == null ? "其他" : p.getProdPcat(),
								Collectors.groupingBy(p -> p.getProductName() == null ? "未知商品" : p.getProductName())
						)));

		List<ProductTreeVO> result = new ArrayList<>();

		for (Map.Entry<String, Map<String, Map<String, List<Product>>>> catEntry : groupedMap.entrySet()) {
			ProductTreeVO treeVO = new ProductTreeVO();
			treeVO.setName(catEntry.getKey()); // 一级

			List<ProductTreeVO.SubCategory> subList = new ArrayList<>();
			for (Map.Entry<String, Map<String, List<Product>>> subEntry : catEntry.getValue().entrySet()) {
				ProductTreeVO.SubCategory subVO = new ProductTreeVO.SubCategory();
				subVO.setName(subEntry.getKey()); // 二级

				List<ProductTreeVO.ProductNameGroup> nameList = new ArrayList<>();
				for (Map.Entry<String, List<Product>> nameEntry : subEntry.getValue().entrySet()) {
					ProductTreeVO.ProductNameGroup groupVO = new ProductTreeVO.ProductNameGroup();
					groupVO.setName(nameEntry.getKey()); // 名称
					groupVO.setProducts(nameEntry.getValue()); // 列表
					nameList.add(groupVO);
				}
				subVO.setProductTypes(nameList);
				subList.add(subVO);
			}
			treeVO.setSubCategories(subList);
			result.add(treeVO);
		}
		return result;
	}


	//查询所有商品树 (管理员或游客用)
	@Override
	public List<ProductTreeVO> getProductTreeByStatus(String status) {
		LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
		if (status != null) {
			String lowerStatus = status.toLowerCase();
			if ("active".equals(lowerStatus) || "inactive".equals(lowerStatus)) {
				wrapper.eq(Product::getStatus, lowerStatus);
			}
		}
		// 排序优化展示
		wrapper.orderByAsc(Product::getProdCat)
				.orderByAsc(Product::getProdPcat)
				.orderByAsc(Product::getProductName);

		List<Product> list = this.list(wrapper);

		// 调用通用方法
		return buildTreeFromList(list);
	}

	// 查询指定农户的商品树 (农户后台管理用)
	@Override
	public List<ProductTreeVO> getProductTreeByFarmerIdAndStatus(Long farmerId, String status) {
		LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

		// 关键点：强制加上 FarmerId 过滤
		wrapper.eq(Product::getFarmerId, farmerId);

		if (status != null) {
			String lowerStatus = status.toLowerCase();
			if ("active".equals(lowerStatus) || "inactive".equals(lowerStatus)) {
				wrapper.eq(Product::getStatus, lowerStatus);
			}
		}

		// 排序
		wrapper.orderByAsc(Product::getProdCat)
				.orderByAsc(Product::getProdPcat)
				.orderByAsc(Product::getProductName);

		List<Product> list = this.list(wrapper);

		// 调用通用方法
		return buildTreeFromList(list);
	}


}