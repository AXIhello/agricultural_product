package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.PurchaseDemand;
import com.example.agricultural_product.service.PurchaseDemandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-demands")
public class PurchaseDemandController {

	@Autowired
	private PurchaseDemandService purchaseDemandService;

	@PostMapping
	public ResponseEntity<Boolean> publish(@RequestBody PurchaseDemand demand) {
		boolean success = purchaseDemandService.publish(demand);
		return ResponseEntity.ok(success);
	}

	// 获取开放的求购需求列表
	@GetMapping
	public ResponseEntity<Page<PurchaseDemand>> list(
			@RequestParam(defaultValue = "1") Integer pageNum,
			@RequestParam(defaultValue = "10") Integer pageSize) {
		return ResponseEntity.ok(purchaseDemandService.getDemandList(pageNum, pageSize));
	}

	// 关键词搜索
	@GetMapping("/search")
	public ResponseEntity<Page<PurchaseDemand>> search(
			@RequestParam String keyword,
			@RequestParam(defaultValue = "1") Integer pageNum,
			@RequestParam(defaultValue = "10") Integer pageSize) {
		return ResponseEntity.ok(purchaseDemandService.searchDemands(keyword, pageNum, pageSize));
	}

	// 获取某个买家的求购需求
	@GetMapping("/buyer/{buyerId}")
	public ResponseEntity<Page<PurchaseDemand>> listByBuyer(
			@PathVariable Long buyerId,
			@RequestParam(defaultValue = "1") Integer pageNum,
			@RequestParam(defaultValue = "10") Integer pageSize) {
		return ResponseEntity.ok(purchaseDemandService.getDemandsByBuyer(buyerId, pageNum, pageSize));
	}

	// 获取单个求购需求详情
	@GetMapping("/{id}")
	public ResponseEntity<PurchaseDemand> get(@PathVariable Integer id) {
		return ResponseEntity.ok(purchaseDemandService.getById(id));
	}
} 