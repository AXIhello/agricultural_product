package com.example.agricultural_product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.pojo.ExpertAnswer;
import com.example.agricultural_product.pojo.ExpertQuestion;
import com.example.agricultural_product.service.ExpertAnswerService;
import com.example.agricultural_product.service.ExpertQuestionService;
import com.example.agricultural_product.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
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

	// 鉴权工具方法
	private boolean checkToken(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return false;
		}
		String token = authHeader.substring(7);
		return JwtUtil.validateToken(token);
	}

	@PostMapping
	public ResponseEntity<Boolean> publish(HttpServletRequest request, @RequestBody ExpertQuestion question) {
		if (!checkToken(request)) return ResponseEntity.status(401).body(false);
		boolean success = expertQuestionService.publish(question);
		return ResponseEntity.ok(success);
	}

	@PostMapping("/{id}/answers")
	public ResponseEntity<Boolean> createAnswer(HttpServletRequest request,
												@PathVariable("id") Integer questionId,
												@RequestBody ExpertAnswer answer) {
		if (!checkToken(request)) return ResponseEntity.status(401).body(false);

		ExpertQuestion q = expertQuestionService.getById(questionId);
		if (q == null || !"open".equalsIgnoreCase(q.getStatus())) {
			return ResponseEntity.ok(false);
		}
		answer.setQuestionId(questionId);
		boolean success = expertAnswerService.save(answer);
		return ResponseEntity.ok(success);
	}

	@GetMapping("/{id}/answers")
	public ResponseEntity<Page<ExpertAnswer>> listAnswers(HttpServletRequest request,
														  @PathVariable("id") Integer questionId,
														  @RequestParam(defaultValue = "1") Integer pageNum,
														  @RequestParam(defaultValue = "10") Integer pageSize) {
		if (!checkToken(request)) return ResponseEntity.status(401).build();
		return ResponseEntity.ok(expertAnswerService.listByQuestion(questionId, pageNum, pageSize));
	}

	@PostMapping("/{id}/accept/{answerId}")
	public ResponseEntity<Boolean> acceptAnswer(HttpServletRequest request,
												@PathVariable("id") Integer questionId,
												@PathVariable Integer answerId) {
		if (!checkToken(request)) return ResponseEntity.status(401).body(false);

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
	public ResponseEntity<Page<ExpertQuestion>> listOpen(HttpServletRequest request,
														 @RequestParam(defaultValue = "1") Integer pageNum,
														 @RequestParam(defaultValue = "10") Integer pageSize) {
		if (!checkToken(request)) return ResponseEntity.status(401).build();
		return ResponseEntity.ok(expertQuestionService.listOpen(pageNum, pageSize));
	}

	@GetMapping("/search")
	public ResponseEntity<Page<ExpertQuestion>> search(HttpServletRequest request,
													   @RequestParam String keyword,
													   @RequestParam(defaultValue = "1") Integer pageNum,
													   @RequestParam(defaultValue = "10") Integer pageSize) {
		if (!checkToken(request)) return ResponseEntity.status(401).build();
		return ResponseEntity.ok(expertQuestionService.search(keyword, pageNum, pageSize));
	}

	@GetMapping("/farmer/{farmerId}")
	public ResponseEntity<Page<ExpertQuestion>> listByFarmer(HttpServletRequest request,
															 @PathVariable Long farmerId,
															 @RequestParam(defaultValue = "1") Integer pageNum,
															 @RequestParam(defaultValue = "10") Integer pageSize) {
		if (!checkToken(request)) return ResponseEntity.status(401).build();
		return ResponseEntity.ok(expertQuestionService.listByFarmer(farmerId, pageNum, pageSize));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ExpertQuestion> get(HttpServletRequest request,
											  @PathVariable Integer id) {
		if (!checkToken(request)) return ResponseEntity.status(401).build();
		return ResponseEntity.ok(expertQuestionService.getById(id));
	}
}
