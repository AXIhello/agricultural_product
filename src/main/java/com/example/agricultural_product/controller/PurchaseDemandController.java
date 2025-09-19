package com.example.agricultural_product.controller;

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
} 