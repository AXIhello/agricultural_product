package com.example.agricultural_product.config;

import com.example.agricultural_product.mapper.UserMapper;
import com.example.agricultural_product.pojo.User;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private UserMapper userMapper;

    @Override
    public void run(String... args) {
        // 1. 从 SQL 文件加载并执行脚本来建表
        org.springframework.core.io.Resource sqlScriptResource = new ClassPathResource("agriculture.sql");

        if (sqlScriptResource.exists()) {
            System.out.println("正在执行 SQL 脚本: agriculture.sql");
            try {
                // 使用 ResourceDatabasePopulator 来执行 SQL 脚本
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();

                // 设置 SQL 脚本的编码
                populator.setSqlScriptEncoding(StandardCharsets.UTF_8.name());

                // 添加 SQL 脚本资源，现在直接添加 ClassPathResource 即可
                populator.addScript(sqlScriptResource);

                // 配置脚本执行选项 (这些都是 ScriptUtils 内部使用的默认值，但你可以明确设置)
                populator.setContinueOnError(false); // 遇到错误是否继续执行后续语句
                populator.setIgnoreFailedDrops(false); // 是否忽略删除表失败的错误
                populator.setSeparator(ScriptUtils.DEFAULT_STATEMENT_SEPARATOR); // SQL语句分隔符，默认是分号 ";"
                populator.setCommentPrefix(ScriptUtils.DEFAULT_COMMENT_PREFIX); // 注释前缀，默认是 "--"
                populator.setBlockCommentStartDelimiter(ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER); // 块注释开始，默认 "/*"
                populator.setBlockCommentEndDelimiter(ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);   // 块注释结束，默认 "*/"

                // 执行脚本。ResourceDatabasePopulator 会从 JdbcTemplate 获取 DataSource
                populator.execute(jdbcTemplate.getDataSource());

                System.out.println("SQL 脚本 agriculture.sql 执行成功！");
            } catch (Exception e) {
                System.err.println("SQL 脚本 agriculture.sql 执行失败: " + e.getMessage());
                // 根据需要，你可以选择在此处抛出异常或更优雅地处理
                throw new RuntimeException("初始化数据库失败：无法执行 SQL 脚本。", e);
            }
        } else {
            System.err.println("未找到 SQL 脚本文件: src/main/resources/agriculture.sql");
            // 如果文件不存在，你可能希望程序停止或打印警告
            // throw new RuntimeException("初始化数据库失败：未找到 SQL 脚本文件 agriculture.sql");
        }


        // 2. 检查是否已有 admin 账号
        // 注意：如果 agriculture.sql 中没有创建 users 表，那么这里的查询会失败。
        // 请确保 agriculture.sql 包含 users 表的创建语句。
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE user_name = 'admin'",
                Long.class
        );

        if (count != null && count == 0) {
            // 插入默认管理员
            User user = new User();
            user.setUserName("admin");   // 登录账号
            user.setPassword(encoder.encode("123456")); // 加密密码
            user.setName("管理员");      // 昵称
            user.setRole("admin");
            user.setEmail("admin@example.com"); //

            userMapper.insert(user);
            System.out.println("初始化用户成功: admin / 123456");
        }

        // 3. 初始化 deepseek AI 系统用户（user_name=kimi_ai，显示名“Kimi AI”）
        Long kimiCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE user_name = ?",
                Long.class,
                "deepseek_ai"
        );
        if (kimiCount != null && kimiCount == 0) {
            User ai = new User();
            ai.setUserName("deepseek_ai");              // 登录名（不用于人类登录）
            ai.setPassword(encoder.encode(UUID.randomUUID().toString())); // 随机密码占位
            ai.setName("deepseek AI");                  // 显示名
            ai.setRole("system");                   // 系统角色
            ai.setEmail("deepseek.ai@local");           // 占位邮箱，可为空
            userMapper.insert(ai);

            // 可选：打印生成的 ID，便于配置 moonshot.responder-id
            try {
                Long id = jdbcTemplate.queryForObject(
                        "SELECT user_id FROM users WHERE user_name = ?",
                        Long.class,
                        "kimi_ai"
                );
                System.out.println("初始化系统用户成功: deepseek AI (user_name=deepseek_ai, user_id=" + id + ")");
            } catch (Exception ignore) {
                System.out.println("初始化系统用户成功: deepseek AI (user_name=deepseek_ai)");
            }
        }
    }
}