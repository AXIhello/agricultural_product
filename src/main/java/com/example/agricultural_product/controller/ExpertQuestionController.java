package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.ExpertAnswer;
import com.example.agricultural_product.pojo.ExpertQuestion;
import com.example.agricultural_product.service.ExpertAnswerService;
import com.example.agricultural_product.service.ExpertQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expert-questions")
public class ExpertQuestionController {

	@Autowired
	private ExpertQuestionService expertQuestionService;

	@Autowired
	private ExpertAnswerService expertAnswerService;

	@PostMapping
	public ResponseEntity<Boolean> publish(@RequestBody ExpertQuestion question) {
		boolean success = expertQuestionService.publish(question);
		return ResponseEntity.ok(success);
	}

	// 新增回答（仅 open 状态可回答，不改变问题状态）
	@PostMapping("/{id}/answers")
	public ResponseEntity<Boolean> createAnswer(@PathVariable("id") Integer questionId,
	                                           @RequestBody ExpertAnswer answer) {
		ExpertQuestion q = expertQuestionService.getById(questionId);
		if (q == null || !"open".equalsIgnoreCase(q.getStatus())) {
			return ResponseEntity.ok(false);
		}
		answer.setQuestionId(questionId);
		boolean success = expertAnswerService.save(answer);
		return ResponseEntity.ok(success);
	}

	// 分页列出某问题下的回答
	@GetMapping("/{id}/answers")
	public ResponseEntity<Page<ExpertAnswer>> listAnswers(@PathVariable("id") Integer questionId,
	                                                     @RequestParam(defaultValue = "1") Integer pageNum,
	                                                     @RequestParam(defaultValue = "10") Integer pageSize) {
		return ResponseEntity.ok(expertAnswerService.listByQuestion(questionId, pageNum, pageSize));
	}

	// 采纳回答（采纳后置为 answered，阻止后续回答）
	@PostMapping("/{id}/accept/{answerId}")
	public ResponseEntity<Boolean> acceptAnswer(@PathVariable("id") Integer questionId,
	                                           @PathVariable Integer answerId) {
		boolean success = expertQuestionService.acceptAnswer(questionId, answerId);
		if (success) {
			ExpertAnswer a = expertAnswerService.getById(answerId);
			if (a != null) {
				a.setIsAccepted(1);
				expertAnswerService.updateById(a);
			}
		}
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