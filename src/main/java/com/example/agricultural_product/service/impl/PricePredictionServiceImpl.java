package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.agricultural_product.dto.PredictionRequest;
import com.example.agricultural_product.mapper.ProductMapper;
import com.example.agricultural_product.pojo.Product;
import com.example.agricultural_product.service.PricePredictionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper; // 【新增】用于JSON解析
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy; // 【新增】懒加载，避免启动阻塞
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

// 【关键修复】：添加 @Lazy 注解，延迟该 Bean 的初始化，直到它被首次调用
@Service
@Lazy
public class PricePredictionServiceImpl implements PricePredictionService {

    @Autowired
    private ProductMapper productMapper;

    @Value("${python.script.path:D:/aprojects/agri/agricultural_product/src/main/python/LSTM50/prediction_now.py}")
    private String pythonScriptPath;

    // 尽管没有在代码中使用，但保留配置，避免报错
    @Value("${python.data.path:D:/aprojects/agri/agricultural_product/src/main/python/LSTM50/data50/lstm_data_collection.pkl}")
    private String lstmDataPath;

    // 【JSON 元数据路径】
    @Value("${python.metadata.path:D:/aprojects/agri/agricultural_product/src/main/python/LSTM50/data50/lstm_metadata_only.json}")
    private String jsonMetadataPath;

    @Value("${python.executable.path:D:/111111111/anaconda3/envs/agri_env/Scripts/python.exe}")
    private String pythonExecutable;

    private static final String LABEL_PRE_PREDICT = "预测";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final int PRICE_SCALE = 2;

    LocalDate endDate = LocalDate.now().plusDays(30);

    // ObjectMapper 用于解析 JSON
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // Key: taskId, Value: Map<"status", "COMPLETED"/"RUNNING"/"FAILED"> 和 "data" 或 "message"
    private final Map<String, Map<String, Object>> taskResults = new ConcurrentHashMap<>();

    // --- 元数据缓存 ---
    private volatile Map<String, Map<String, List<String>>> detailedMetadataCache = null;
    private static final Object cacheLock = new Object();

    // -----------------------------------------------------
    // 核心方法：loadAndAggregateDetailedMetadata
    // -----------------------------------------------------

    /**
     * 【关键修复】从 JSON 文件中加载和聚合详细元数据。
     * 替换了 ObjectInputStream 为 ObjectMapper，确保正确解析 JSON。
     */
    private Map<String, Map<String, List<String>>> loadAndAggregateDetailedMetadata() {
        if (detailedMetadataCache != null) {
            return detailedMetadataCache;
        }

        synchronized (cacheLock) {
            if (detailedMetadataCache != null) {
                return detailedMetadataCache;
            }

            File jsonFile = new File(jsonMetadataPath);
            if (!jsonFile.exists() || !jsonFile.canRead()) {
                // 注意：这里需要使用 jsonMetadataPath
                System.err.println("元数据 JSON 文件不存在或无权限：" + jsonMetadataPath);
                return Collections.emptyMap();
            }

            try (FileInputStream fileIn = new FileInputStream(jsonFile)) {

                // 1. 读取整个 JSON 文件内容
                TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
                // 【核心修复】：使用 OBJECT_MAPPER 读取 JSON 文件流
                Map<String, Object> loadedMap = OBJECT_MAPPER.readValue(fileIn, typeRef);

                if (loadedMap == null || !loadedMap.containsKey("data")) {
                    throw new IllegalArgumentException("JSON 元数据文件格式错误：缺少 'data' 节点。");
                }

                // 2. 提取 'data' 节点（这是一个 Map<产品名, 产品元数据对象>）
                Map<String, Map<String, Object>> productDataMap =
                        (Map<String, Map<String, Object>>) loadedMap.get("data");

                Map<String, Map<String, List<String>>> productSpecificMetadata = new HashMap<>();

                // 3. 遍历产品数据，提取 category_mappings
                for (Map.Entry<String, Map<String, Object>> entry : productDataMap.entrySet()) {
                    String prodName = entry.getKey().trim();
                    Object mappingsObj = entry.getValue().get("category_mappings");

                    if (mappingsObj instanceof Map) {
                        // 由于 Jackson 默认将 List<String> 读取为 List<Object>，这里使用 Object 接收
                        Map<String, List<Object>> rawMappings = (Map<String, List<Object>>) mappingsObj;
                        Map<String, List<String>> specificMeta = new HashMap<>();

                        // 转换 List<Object> 为 List<String> 并去重
                        specificMeta.put("pcats", convertObjectListToStringList(rawMappings.getOrDefault("prodPcat", Collections.emptyList())));
                        specificMeta.put("specs", convertObjectListToStringList(rawMappings.getOrDefault("specInfo", Collections.emptyList())));

                        productSpecificMetadata.put(prodName, specificMeta);
                    }
                }

                detailedMetadataCache = productSpecificMetadata;
                return detailedMetadataCache;

            } catch (Exception e) {
                // 异常处理更加精确
                System.err.println("解析详细元数据 JSON 文件失败，请检查文件格式：" + e.getMessage());
                e.printStackTrace();
                return Collections.emptyMap();
            }
        }
    }

    // 辅助方法：将 List<Object> 转换为 List<String>
    private List<String> convertObjectListToStringList(List<Object> rawList) {
        if (rawList == null) {
            return Collections.emptyList();
        }
        Set<String> uniqueStrings = new HashSet<>();
        for (Object item : rawList) {
            if (item != null) {
                uniqueStrings.add(item.toString().trim());
            }
        }
        return new ArrayList<>(uniqueStrings);
    }


    // -----------------------------------------------------
    // 其他方法（保持不变）
    // -----------------------------------------------------

    @Override
    public String startPredictionTask(PredictionRequest request) {
        // 1. 入参校验
        validateRequest(request);

        // 2. 分支处理
        if (!LABEL_PRE_PREDICT.equals(request.getLabelPre())) {
            // 分支1：非“预测”→ 操作tb_product表（同步执行）
            handleDatabaseCrud(request);
            return "CRUD_SUCCESS"; // 立即返回成功标识
        } else {
            // 分支2：“预测”→ 异步执行LSTM价格预测
            String taskId = UUID.randomUUID().toString();

            // 初始化任务状态为 RUNNING，立即响应
            taskResults.put(taskId, Map.of("status", "RUNNING", "message", "预测任务已启动，正在模型运算中..."));

            // 异步启动耗时的预测任务
            executeLstmPredictionAsync(taskId, request);

            return taskId;
        }
    }

    @Override
    public Map<String, Object> getPredictionResult(String taskId) {
        Map<String, Object> result = taskResults.get(taskId);
        if (result == null) {
            return Map.of("status", "NOT_FOUND", "message", "任务ID不存在或已过期");
        }
        return result;
    }

    @Async
    public void executeLstmPredictionAsync(String taskId, PredictionRequest request) {
        try {
            // 运行 Python 脚本 (耗时 1-2 分钟的逻辑)
            List<Product> predictions = handleLstmPredictionInternal(request);

            // 任务成功完成，存储结果
            taskResults.put(taskId, Map.of(
                    "status", "COMPLETED",
                    "message", "价格预测成功",
                    "data", predictions
            ));

        } catch (IllegalArgumentException e) {
            // 业务校验失败 (如参数不合法)
            taskResults.put(taskId, Map.of(
                    "status", "FAILED",
                    "message", "请求参数校验失败：" + e.getMessage()
            ));
        } catch (RuntimeException e) {
            // 系统执行失败 (如 Python 脚本执行失败，IO异常等)
            taskResults.put(taskId, Map.of(
                    "status", "FAILED",
                    "message", "系统执行错误：" + e.getMessage()
            ));
        } catch (Exception e) {
            // 捕获所有其他意外异常
            taskResults.put(taskId, Map.of(
                    "status", "FAILED",
                    "message", "未知错误：" + e.getMessage()
            ));
        }
    }


    private List<Product> handleLstmPredictionInternal(PredictionRequest request) {
        // [Python 预测执行逻辑]
        List<Product> predictions = new ArrayList<>();
        Process process = null;
        BufferedReader inputReader = null;
        BufferedReader errorReader = null;
        LocalDate finalEndDate = this.endDate;
        LocalDate startDateForOutput = LocalDate.now();

        try {
            // 注意：这里是PREDICT模式，使用原始参数
            String[] command = {
                    pythonExecutable,
                    pythonScriptPath,
                    "--mode", "PREDICT",
                    "--product", request.getProductName().trim(),
                    "--spec", request.getSpecInfo().trim(),
                    "--pcat", request.getProdPcat().trim(),
                    "--end-date", endDate.format(DATE_FORMATTER)
            };

            ProcessBuilder processBuilder = new ProcessBuilder(command)
                    .directory(new File(System.getProperty("user.dir")))
                    .redirectErrorStream(false);
            process = processBuilder.start();

            // 注意：使用 Process.waitFor() 时，如果 I/O 缓冲区未清空，可能会导致死锁或超时，
            // 异步读取错误流和输入流是必要的。

            inputReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            // 异步读取错误信息 (防止缓冲区满阻塞)
            StringBuilder errorMsg = new StringBuilder();
            BufferedReader finalErrorReader = errorReader;
            Thread errorThread = new Thread(() -> {
                String errLine;
                try {
                    while ((errLine = finalErrorReader.readLine()) != null) {
                        errorMsg.append(errLine).append("\n");
                    }
                } catch (IOException e) {
                    System.err.println("Error reading Python error stream: " + e.getMessage());
                }
            });
            errorThread.start();

            // 解析预测结果
            String line;
            while ((line = inputReader.readLine()) != null) {
                line = line.trim();
                if (StringUtils.isEmpty(line) || line.startsWith("---METADATA_START---")) continue;
                Product predProduct = parsePredictionResult(line, request);
                if (predProduct != null) {
                    LocalDate forecastDate = predProduct.getForecastDate();

                    if (!forecastDate.isBefore(startDateForOutput) && !forecastDate.isAfter(finalEndDate)) {
                        predictions.add(predProduct);
                    }
                }
            }

            int exitCode = process.waitFor();
            errorThread.join(5000); // 等待错误读取线程结束 (最多5秒)

            if (exitCode != 0) {
                // 如果Python脚本返回非零退出码，抛出异常
                throw new RuntimeException(String.format("Python脚本执行失败（退出码：%d），错误信息：%s", exitCode, errorMsg));
            }
            if (predictions.isEmpty()) {
                throw new RuntimeException("预测成功，但未返回有效价格数据");
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("价格预测执行失败：" + e.getMessage(), e);
        } finally {
            closeResource(inputReader);
            closeResource(errorReader);
            if (process != null) process.destroy();
        }
        return predictions;
    }


    private void handleDatabaseCrud(PredictionRequest request) {
        Product product = convertToProductEntity(request);
        if (product.getProductId() == null) {
            product.setCreateTime(LocalDateTime.now());
            product.setUpdateTime(LocalDateTime.now());
            productMapper.insert(product);
        } else {
            product.setUpdateTime(LocalDateTime.now());
            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<Product>()
                    .eq(Product::getProductId, product.getProductId());
            productMapper.update(product, queryWrapper);
        }
    }

    private Product convertToProductEntity(PredictionRequest request) {
        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice().setScale(PRICE_SCALE, RoundingMode.HALF_UP));
        product.setStock(request.getStock());
        product.setStatus(request.getStatus());
        product.setImagePath(request.getImagePath());
        product.setProdCat(request.getProdCat());
        product.setProdPcat(request.getProdPcat());
        product.setUnitInfo(request.getUnitInfo());
        product.setSpecInfo(request.getSpecInfo());
        product.setPlace(request.getPlace());
        return product;
    }

    private Product parsePredictionResult(String line, PredictionRequest request) {
        try {
            String[] parts = line.split(",", 2);
            if (parts.length != 2) {
                System.err.println("无效预测格式：" + line);
                return null;
            }
            Product product = new Product();
            product.setProductName(request.getProductName().trim());
            product.setSpecInfo(request.getSpecInfo().trim());
            product.setProdPcat(request.getProdPcat().trim());
            product.setForecastDate(LocalDate.parse(parts[0].trim(), DATE_FORMATTER));
            product.setPredictedPrice(new BigDecimal(parts[1].trim()).setScale(PRICE_SCALE, RoundingMode.HALF_UP));
            return product;
        } catch (DateTimeParseException | NumberFormatException e) {
            System.err.println("解析预测数据失败：" + line + "，错误：" + e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, List<String>> getProductCategoryAndSpec(String productName) {
        Map<String, Map<String, List<String>>> allDetails = loadAndAggregateDetailedMetadata();

        String matchedKey = allDetails.keySet().stream()
                .filter(key -> key.trim().equals(productName.trim()))
                .findFirst().orElse(null);

        if (matchedKey != null) {
            return allDetails.get(matchedKey);
        }

        return Collections.emptyMap();
    }

    private void validateRequest(PredictionRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.hasText(request.getLabelPre(), "labelPre（操作标识）不能为空");

        Assert.hasText(request.getProductName(), "产品名称（productName）不能为空");
        Assert.hasText(request.getProdCat(), "产品大类（prodCat）不能为空");
        Assert.hasText(request.getProdPcat(), "产品小类（prodPcat）不能为空");
        Assert.hasText(request.getSpecInfo(), "产品规格（specInfo）不能为空");
        Assert.hasText(request.getUnitInfo(), "单位（unitInfo）不能为空");
        Assert.notNull(request.getPrice(), "售价（price）不能为空");
        Assert.notNull(request.getStock(), "库存（stock）不能为空");
        Assert.hasText(request.getStatus(), "商品状态（status）不能为空");

        if (!LABEL_PRE_PREDICT.equals(request.getLabelPre())) {
            try {
                // 此处会触发元数据加载
                Map<String, List<String>> metadata = getProductCategoryAndSpec(request.getProductName());

                if (!metadata.isEmpty()) {
                    List<String> knownSpecs = metadata.getOrDefault("specs", Collections.emptyList());
                    List<String> knownPCats = metadata.getOrDefault("pcats", Collections.emptyList());

                    Assert.isTrue(knownPCats.contains(request.getProdPcat()),
                            String.format("产品类别 '%s' 不在模型已知的类别列表中。", request.getProdPcat()));

                    Assert.isTrue(knownSpecs.contains(request.getSpecInfo()),
                            String.format("产品规格 '%s' 不在模型已知的规格列表中。", request.getSpecInfo()));
                } else {
                    System.err.println("产品名称 '" + request.getProductName() + "' 在模型数据中不存在，无法进行 prodPcat/specInfo 核实。");
                }

            } catch (Exception e) {
                System.err.println("元数据校验过程中发生异常：" + e.getMessage());
            }
        }

        if (LABEL_PRE_PREDICT.equals(request.getLabelPre())) {
            Assert.notNull(endDate, "预测截止日期（forecastDate）不能为空");
            Assert.isTrue(!endDate.isBefore(LocalDate.now()), "预测截止日期不能早于当前日期");

            File scriptFile = new File(pythonScriptPath);

            // 增强版断言：输出更详细的信息
            Assert.isTrue(
                    scriptFile.exists() && scriptFile.canRead(),
                    "Python预测脚本检查失败："
                            + "\n  脚本路径: " + pythonScriptPath
                            + "\n  是否存在: " + scriptFile.exists()
                            + "\n  是否可读: " + scriptFile.canRead()
                            + "\n  文件大小: " + (scriptFile.exists() ? scriptFile.length() + " bytes" : "N/A")
            );

            // 将检测信息输出到 console（可选，但方便排查）
            System.out.println("预测脚本检测正常：");
            System.out.println("路径：" + pythonScriptPath);
            System.out.println("存在：" + scriptFile.exists());
            System.out.println("可读：" + scriptFile.canRead());
            System.out.println("大小：" + scriptFile.length() + " bytes");
        }

    }

    private void closeResource(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException e) {
                System.err.println("资源关闭失败：" + e.getMessage());
            }
        }
    }

    @Override
    public List<String> getPredictableProducts() {
        return new ArrayList<>(loadAndAggregateDetailedMetadata().keySet());
    }
}