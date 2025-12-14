package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.agricultural_product.dto.PredictionRequest;
import com.example.agricultural_product.mapper.ProductMapper;
import com.example.agricultural_product.pojo.Product;
import com.example.agricultural_product.service.PricePredictionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
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

@Service
@Lazy
public class PricePredictionServiceImpl implements PricePredictionService {

    @Autowired
    private ProductMapper productMapper;

    // 【关键修复1】注入自身代理对象，解决 @Async 同类调用失效导致的主线程阻塞(500错误)问题
    @Autowired
    @Lazy
    private PricePredictionService self;

    @Value("${python.script.path:D:/aprojects/agri/agricultural_product/src/main/python/LSTM50/prediction_now.py}")
    private String pythonScriptPath;

    @Value("${python.data.path:D:/aprojects/agri/agricultural_product/src/main/python/LSTM50/data50/lstm_data_collection.pkl}")
    private String lstmDataPath;

    @Value("${python.metadata.path:D:/aprojects/agri/agricultural_product/src/main/python/LSTM50/data50/lstm_metadata_only.json}")
    private String jsonMetadataPath;

    @Value("${python.executable.path:python}")
    private String pythonExecutable;

    private static final String LABEL_PRE_PREDICT = "预测";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final int PRICE_SCALE = 2;

    LocalDate endDate = LocalDate.now().plusDays(30);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Map<String, Map<String, Object>> taskResults = new ConcurrentHashMap<>();

    private volatile Map<String, Map<String, List<String>>> detailedMetadataCache = null;
    private static final Object cacheLock = new Object();

    // 元数据加载逻辑
    private Map<String, Map<String, List<String>>> loadAndAggregateDetailedMetadata() {
        if (detailedMetadataCache != null) return detailedMetadataCache;

        synchronized (cacheLock) {
            if (detailedMetadataCache != null) return detailedMetadataCache;

            File jsonFile = new File(jsonMetadataPath);
            if (!jsonFile.exists() || !jsonFile.canRead()) {
                System.err.println("元数据 JSON 文件不存在或无权限：" + jsonMetadataPath);
                return Collections.emptyMap();
            }

            try (FileInputStream fileIn = new FileInputStream(jsonFile)) {
                TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
                Map<String, Object> loadedMap = OBJECT_MAPPER.readValue(fileIn, typeRef);

                if (loadedMap == null || !loadedMap.containsKey("data")) {
                    throw new IllegalArgumentException("JSON 元数据文件格式错误：缺少 'data' 节点。");
                }

                Map<String, Map<String, Object>> productDataMap =
                        (Map<String, Map<String, Object>>) loadedMap.get("data");

                Map<String, Map<String, List<String>>> productSpecificMetadata = new HashMap<>();

                for (Map.Entry<String, Map<String, Object>> entry : productDataMap.entrySet()) {
                    String prodName = entry.getKey().trim();
                    Object mappingsObj = entry.getValue().get("category_mappings");

                    if (mappingsObj instanceof Map) {
                        Map<String, List<Object>> rawMappings = (Map<String, List<Object>>) mappingsObj;
                        Map<String, List<String>> specificMeta = new HashMap<>();

                        specificMeta.put("pcats", convertObjectListToStringList(rawMappings.getOrDefault("prodPcat", Collections.emptyList())));
                        specificMeta.put("specs", convertObjectListToStringList(rawMappings.getOrDefault("specInfo", Collections.emptyList())));

                        productSpecificMetadata.put(prodName, specificMeta);
                    }
                }
                detailedMetadataCache = productSpecificMetadata;
                return detailedMetadataCache;

            } catch (Exception e) {
                System.err.println("解析详细元数据 JSON 文件失败：" + e.getMessage());
                e.printStackTrace();
                return Collections.emptyMap();
            }
        }
    }

    private List<String> convertObjectListToStringList(List<Object> rawList) {
        if (rawList == null) return Collections.emptyList();
        Set<String> uniqueStrings = new HashSet<>();
        for (Object item : rawList) {
            if (item != null) uniqueStrings.add(item.toString().trim());
        }
        return new ArrayList<>(uniqueStrings);
    }


    // 业务逻辑

    @Override
    public String startPredictionTask(PredictionRequest request) {
        validateRequest(request);

        if (!LABEL_PRE_PREDICT.equals(request.getLabelPre())) {
            handleDatabaseCrud(request);
            return "CRUD_SUCCESS";
        } else {
            String taskId = UUID.randomUUID().toString();

            taskResults.put(taskId, Map.of("status", "RUNNING", "message", "预测任务已启动，正在模型运算中..."));

            self.executeLstmPredictionAsync(taskId, request);

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
            // 执行核心预测逻辑
            List<Product> predictions = handleLstmPredictionInternal(request);

            // 成功
            taskResults.put(taskId, Map.of(
                    "status", "COMPLETED",
                    "message", "价格预测成功",
                    "data", predictions
            ));

        } catch (IllegalArgumentException e) {
            taskResults.put(taskId, Map.of(
                    "status", "FAILED",
                    "message", "请求参数校验失败：" + e.getMessage()
            ));
        } catch (RuntimeException e) {
            // 捕获系统执行错误（包含 Python 返回的具体中文错误）
            taskResults.put(taskId, Map.of(
                    "status", "FAILED",
                    "message", "系统执行错误：" + e.getMessage()
            ));
        } catch (Exception e) {
            taskResults.put(taskId, Map.of(
                    "status", "FAILED",
                    "message", "未知错误：" + e.getMessage()
            ));
        }
    }

    /**
     * 内部方法：调用 Python 脚本
     */
    private List<Product> handleLstmPredictionInternal(PredictionRequest request) {
        List<Product> predictions = new ArrayList<>();
        Process process = null;
        BufferedReader inputReader = null;
        BufferedReader errorReader = null;
        LocalDate finalEndDate = this.endDate;
        LocalDate startDateForOutput = LocalDate.now();

        try {
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

            // 【关键修复】强制 Python 输出 UTF-8，解决 Windows 控制台乱码问题
            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");

            process = processBuilder.start();

            inputReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8));

            // 异步读取错误流
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

            // --- 循环解析输出 ---
            String line;
            while ((line = inputReader.readLine()) != null) {
                line = line.trim();
                if (StringUtils.isEmpty(line) || line.startsWith("---METADATA_START---")) continue;

                // 【新增】识别 Python 返回的 JSON 格式错误提示
                if (line.startsWith("{")) {
                    try {
                        Map<String, Object> pyResult = OBJECT_MAPPER.readValue(line, new TypeReference<Map<String, Object>>() {});

                        // 检测是否为失败状态
                        if ("failed".equals(pyResult.get("status"))) {
                            String msg = (String) pyResult.get("msg");
                            // 抛出运行时异常，中断流程，将具体原因传给上层
                            throw new RuntimeException(msg);
                        }
                    } catch (Exception e) {
                        // 如果是手动抛出的 RuntimeException，继续往上抛
                        if (e instanceof RuntimeException && !(e instanceof IllegalArgumentException)) {
                            throw (RuntimeException) e;
                        }
                        // 否则可能是解析失败，忽略，继续尝试解析 CSV
                    }
                }

                // 常规解析：CSV 格式日期价格
                Product predProduct = parsePredictionResult(line, request);
                if (predProduct != null) {
                    LocalDate forecastDate = predProduct.getForecastDate();
                    if (!forecastDate.isBefore(startDateForOutput) && !forecastDate.isAfter(finalEndDate)) {
                        predictions.add(predProduct);
                    }
                }
            }

            int exitCode = process.waitFor();
            errorThread.join(5000);

            if (exitCode != 0) {
                throw new RuntimeException(String.format("Python脚本执行失败（退出码：%d），错误信息：%s", exitCode, errorMsg));
            }

            // 如果 predictions 为空，说明没有获取到有效数据
            if (predictions.isEmpty()) {
                throw new RuntimeException("缺少历史数据，未返回有效价格预测");
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
            if (parts.length != 2) return null;

            Product product = new Product();
            product.setProductName(request.getProductName().trim());
            product.setSpecInfo(request.getSpecInfo().trim());
            product.setProdPcat(request.getProdPcat().trim());
            product.setForecastDate(LocalDate.parse(parts[0].trim(), DATE_FORMATTER));
            product.setPredictedPrice(new BigDecimal(parts[1].trim()).setScale(PRICE_SCALE, RoundingMode.HALF_UP));
            return product;
        } catch (DateTimeParseException | NumberFormatException e) {
            return null;
        }
    }

    @Override
    public Map<String, List<String>> getProductCategoryAndSpec(String productName) {
        Map<String, Map<String, List<String>>> allDetails = loadAndAggregateDetailedMetadata();
        String matchedKey = allDetails.keySet().stream()
                .filter(key -> key.trim().equals(productName.trim()))
                .findFirst().orElse(null);
        return matchedKey != null ? allDetails.get(matchedKey) : Collections.emptyMap();
    }

    private void validateRequest(PredictionRequest request) {
        Assert.notNull(request, "请求参数不能为空");
        Assert.hasText(request.getLabelPre(), "labelPre（操作标识）不能为空");
        Assert.hasText(request.getProductName(), "产品名称（productName）不能为空");

        if (!LABEL_PRE_PREDICT.equals(request.getLabelPre())) {
            try {
                Map<String, List<String>> metadata = getProductCategoryAndSpec(request.getProductName());
                if (!metadata.isEmpty()) {
                    List<String> knownSpecs = metadata.getOrDefault("specs", Collections.emptyList());
                    List<String> knownPCats = metadata.getOrDefault("pcats", Collections.emptyList());
                    Assert.isTrue(knownPCats.contains(request.getProdPcat()),
                            String.format("产品类别 '%s' 不在模型已知的类别列表中。", request.getProdPcat()));
                    Assert.isTrue(knownSpecs.contains(request.getSpecInfo()),
                            String.format("产品规格 '%s' 不在模型已知的规格列表中。", request.getSpecInfo()));
                }
            } catch (Exception e) {
                System.err.println("元数据校验异常：" + e.getMessage());
            }
        }

        if (LABEL_PRE_PREDICT.equals(request.getLabelPre())) {
            Assert.notNull(endDate, "预测截止日期（forecastDate）不能为空");
            File scriptFile = new File(pythonScriptPath);
            Assert.isTrue(scriptFile.exists() && scriptFile.canRead(), "Python脚本文件不存在或不可读");
        }
    }

    private void closeResource(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException e) {
                // quiet close
            }
        }
    }

    @Override
    public List<String> getPredictableProducts() {
        return new ArrayList<>(loadAndAggregateDetailedMetadata().keySet());
    }
}