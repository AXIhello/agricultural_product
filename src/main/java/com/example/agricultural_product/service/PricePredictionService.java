package com.example.agricultural_product.service;

import com.example.agricultural_product.dto.PredictionRequest;
import java.util.List;
import java.util.Map;

public interface PricePredictionService {

    /**
     * 【替换原 predictPrices】核心业务方法：根据labelPre（预测或CRUD）处理请求。
     * 如果是预测请求，将启动异步任务并立即返回任务ID；
     * 如果是CRUD请求，将同步执行并返回成功标识 (例如 "CRUD_SUCCESS")。
     *
     * @param request 预测或CRUD请求
     * @return 任务ID (String) 或 CRUD成功标识
     */
    String startPredictionTask(PredictionRequest request);

    /**
     * 【新增】根据任务ID查询预测任务的执行状态和结果。
     *
     * @param taskId 异步预测任务ID
     * @return 包含状态 ("status": "RUNNING"/"COMPLETED"/"FAILED") 和数据 ("data" 或 "message") 的 Map
     */
    Map<String, Object> getPredictionResult(String taskId);

    /**
     * 获取所有可预测的产品名称列表。
     */
    List<String> getPredictableProducts();

    /**
     * 根据产品名称查询该产品在模型数据中已知的规格 (specInfo) 和类别 (prodPcat) 列表。
     *
     * @param productName 待查询的产品名称
     * @return 包含 "specs" 和 "pcats" 键的 Map<String, List<String>>
     */
    Map<String, List<String>> getProductCategoryAndSpec(String productName);

    // =========================================================
    // 【新增】 为了支持 Service 内部通过 self 代理调用异步方法
    // =========================================================

    /**
     * 执行具体的异步预测逻辑。
     * <p>
     * 注意：此方法主要供 Service 内部通过 self 代理对象调用，以确保 @Async 注解生效。
     * Controller 层通常不需要直接调用此方法，而是调用 startPredictionTask。
     * </p>
     * @param taskId 任务ID
     * @param request 预测请求参数
     */
    void executeLstmPredictionAsync(String taskId, PredictionRequest request);
}