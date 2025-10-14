package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agricultural_product.mapper.UserApplicationMapper;
import com.example.agricultural_product.mapper.UserMapper;
import com.example.agricultural_product.pojo.User;
import com.example.agricultural_product.pojo.UserApplication;
import com.example.agricultural_product.service.AdminApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminApplicationServiceImpl implements AdminApplicationService {

    private final UserApplicationMapper applicationMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /** 查询所有申请（不分页） */
    @Override
    public List<UserApplication> getAllApplications() {
        QueryWrapper<UserApplication> query = new QueryWrapper<>();
        query.orderByDesc("create_time");
        return applicationMapper.selectList(query);
    }

    /** 分页查询申请 */
    @Override
    public IPage<UserApplication> getApplicationsPage(int page, int size, String status) {
        Page<UserApplication> p = new Page<>(page, size);
        QueryWrapper<UserApplication> query = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            query.eq("status", status);
        }
        query.orderByDesc("create_time");
        return applicationMapper.selectPage(p, query);
    }

    /** 审核通过 */
    @Override
    @Transactional
    public String approveApplication(Long id) {
        UserApplication app = applicationMapper.selectById(id);
        if (app == null) return "申请不存在";
        if ("approved".equals(app.getStatus())) return "该申请已通过审核";

        // 检查用户是否已存在
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("user_name", app.getUserName());
        User existingUser = userMapper.selectOne(query);

        if (existingUser == null) {
            User user = new User();
            user.setUserName(app.getUserName());
            user.setPassword(passwordEncoder.encode(app.getPassword()));
            user.setName(app.getRealName());
            user.setEmail(app.getEmail());
            user.setRole(app.getApplyRole());
            userMapper.insert(user);
        }

        app.setStatus("approved");
        app.setReason(null);
        applicationMapper.updateById(app);

        return "审核通过并已创建用户";
    }

    /** 审核拒绝 */
    @Override
    @Transactional
    public String rejectApplication(Long id, String reason) {
        UserApplication app = applicationMapper.selectById(id);
        if (app == null) return "申请不存在";

        app.setStatus("rejected");
        app.setReason(reason);
        applicationMapper.updateById(app);

        return "已拒绝该申请";
    }
}
