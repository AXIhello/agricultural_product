package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.ExpertQuestion;
import com.example.agricultural_product.pojo.dto.AnswerQuestionRequest;
import com.example.agricultural_product.service.ExpertQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expert-questions")
public class ExpertQuestionController {

	@Autowired
	private ExpertQuestionService expertQuestionService;

	@PostMapping
	public ResponseEntity<Boolean> publish(@RequestBody ExpertQuestion question) {
		boolean success = expertQuestionService.publish(question);
		return ResponseEntity.ok(success);
	}

	@PostMapping("/{id}/answer")
	public ResponseEntity<Boolean> answer(@PathVariable("id") Integer questionId,
	                                     @RequestBody AnswerQuestionRequest request) {
		boolean success = expertQuestionService.answerQuestion(questionId, request.getExpertId(), request.getAnswerContent());
		return ResponseEntity.ok(success);
	}

	@GetMapping
	public ResponseEntity<Page<ExpertQuestion>> listOpen(
			@RequestParam(defaultValue = "1") Integer pageNum,
			@RequestParam(defaultValue = "10") Integer pageSize) {
		return ResponseEntity.ok(expertQuestionService.listOpen(pageNum, pageSize));
	}

	@GetMapping("/search")
	public ResponseEntity<Page<ExpertQuestion>> search(
			@RequestParam String keyword,
			@RequestParam(defaultValue = "1") Integer pageNum,
			@RequestParam(defaultValue = "10") Integer pageSize) {
		return ResponseEntity.ok(expertQuestionService.search(keyword, pageNum, pageSize));
	}

	@GetMapping("/farmer/{farmerId}")
	public ResponseEntity<Page<ExpertQuestion>> listByFarmer(
			@PathVariable Long farmerId,
			@RequestParam(defaultValue = "1") Integer pageNum,
			@RequestParam(defaultValue = "10") Integer pageSize) {
		return ResponseEntity.ok(expertQuestionService.listByFarmer(farmerId, pageNum, pageSize));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ExpertQuestion> get(@PathVariable Integer id) {
		return ResponseEntity.ok(expertQuestionService.getById(id));
	}
} 