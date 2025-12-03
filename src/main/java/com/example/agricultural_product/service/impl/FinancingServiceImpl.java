package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agricultural_product.dto.RecommendedUserDTO;
import com.example.agricultural_product.mapper.BankProductMapper;
import com.example.agricultural_product.mapper.FinancingFarmerMapper;
import com.example.agricultural_product.mapper.FinancingMapper;
import com.example.agricultural_product.mapper.FinancingOfferMapper;
import com.example.agricultural_product.mapper.UserMapper;
import com.example.agricultural_product.pojo.BankProduct;
import com.example.agricultural_product.pojo.Financing;
import com.example.agricultural_product.pojo.FinancingFarmer;
import com.example.agricultural_product.pojo.FinancingOffer;
import com.example.agricultural_product.pojo.User;
import com.example.agricultural_product.service.FinancingService;
import com.example.agricultural_product.service.UserService;
import com.example.agricultural_product.service.UserMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FinancingServiceImpl extends ServiceImpl<FinancingMapper, Financing> implements FinancingService {

    @Autowired
    private FinancingMapper financingMapper;

    @Autowired
    private FinancingFarmerMapper financingFarmerMapper;

    @Autowired
    private FinancingOfferMapper financingOfferMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private BankProductMapper bankProductMapper;

    @Autowired
    private UserMetricsService userMetricsService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取融资申请的所有参与农户ID（包括主申请人和共同申请人）
     */
    private List<Long> getAllFarmerIds(Integer financingId) {
        LambdaQueryWrapper<FinancingFarmer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinancingFarmer::getFinancingId, financingId);
        return financingFarmerMapper.selectList(wrapper)
                .stream()
                .map(FinancingFarmer::getFarmerId)
                .toList();
    }

    /**
     * 仅取“已接受”的共同申请人
     */
    private List<Long> getAcceptedCoApplicantIds(Integer financingId) {
        LambdaQueryWrapper<FinancingFarmer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinancingFarmer::getFinancingId, financingId)
               .eq(FinancingFarmer::getRoleInFinancing, "共同申请人")
               .eq(FinancingFarmer::getInvitationStatus, "accepted");
        return financingFarmerMapper.selectList(wrapper)
                .stream()
                .map(FinancingFarmer::getFarmerId)
                .toList();
    }

    /**
     * 取所有“已接受”的参与人（主申请人 + 已接受的共同申请人）
     */
    private List<Long> getAllAcceptedFarmerIds(Integer financingId) {
        // 主申请人
        LambdaQueryWrapper<FinancingFarmer> q1 = new LambdaQueryWrapper<>();
        q1.eq(FinancingFarmer::getFinancingId, financingId)
          .eq(FinancingFarmer::getRoleInFinancing, "主申请人");
        List<Long> main = financingFarmerMapper.selectList(q1).stream().map(FinancingFarmer::getFarmerId).toList();
        // 已接受的共同申请人
        List<Long> co = getAcceptedCoApplicantIds(financingId);
        return new java.util.ArrayList<>() {{
            addAll(main);
            addAll(co);
        }};
    }

    @Override
    @Transactional
    public Integer applyBankProduct(Long userId, Integer productId, BigDecimal amount, String purpose, List<Long> coApplicantIds) {
        BankProduct product = bankProductMapper.selectById(productId);
        if (product == null || !"active".equals(product.getStatus())) return null;
        if (amount.compareTo(product.getMinAmount()) < 0 || amount.compareTo(product.getMaxAmount()) > 0) return null;

        Financing financing = new Financing();
        financing.setInitiatingFarmerId(userId);
        financing.setAmount(amount);
        financing.setPurpose(purpose != null ? purpose : product.getProductName());
        financing.setTerm(product.getTermMonths());
        financing.setProductId(productId);
        financing.setApplicationStatus("submitted"); // 直接进入已提交，等待银行处理
        financing.setCreateTime(LocalDateTime.now());
        financing.setUpdateTime(LocalDateTime.now());
        financingMapper.insert(financing);
        Integer financingId = financing.getFinancingId();

        // 主申请人
        FinancingFarmer mainFarmer = new FinancingFarmer();
        mainFarmer.setFinancingId(financingId);
        mainFarmer.setFarmerId(userId);
        mainFarmer.setRoleInFinancing("主申请人");
        financingFarmerMapper.insert(mainFarmer);

        return financingId;
    }

    @Override
    public Page<Financing> listProductApplications(Long bankUserId, Integer productId, Integer pageNum, Integer pageSize) {
        // 找到该银行的产品集合（可按单个productId筛选）
        List<BankProduct> products;
        if (productId != null) {
            BankProduct p = bankProductMapper.selectById(productId);
            if (p == null || !p.getBankUserId().equals(bankUserId)) {
                return new Page<>(pageNum, pageSize);
            }
            products = List.of(p);
        } else {
            products = bankProductMapper.selectList(
                    new LambdaQueryWrapper<BankProduct>().eq(BankProduct::getBankUserId, bankUserId)
            );
        }
        if (products.isEmpty()) return new Page<>(pageNum, pageSize);

        Set<Integer> pids = products.stream().map(BankProduct::getProductId).collect(Collectors.toSet());
        Page<Financing> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Financing> qw = new LambdaQueryWrapper<>();
        qw.in(Financing::getProductId, pids)
                .orderByDesc(Financing::getCreateTime);
        return financingMapper.selectPage(page, qw);
    }

    /**
     * 获取共同申请人ID列表（不包括主申请人）
     */
    private List<Long> getCoApplicantIds(Integer financingId) {
        LambdaQueryWrapper<FinancingFarmer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinancingFarmer::getFinancingId, financingId)
                .eq(FinancingFarmer::getRoleInFinancing, "共同申请人");
        return financingFarmerMapper.selectList(wrapper)
                .stream()
                .map(FinancingFarmer::getFarmerId)
                .toList();
    }


    @Override
    @Transactional
    public Integer createFinancing(Long initiatingFarmerId, BigDecimal amount, String purpose, Integer term, List<Long> coApplicantIds) {
        Financing financing = new Financing();
        financing.setInitiatingFarmerId(initiatingFarmerId);
        financing.setAmount(amount);
        financing.setPurpose(purpose);
        financing.setApplicationStatus("draft");
        financing.setCreateTime(LocalDateTime.now());
        financing.setUpdateTime(LocalDateTime.now());
        financing.setTerm(term); // ✅ 新增：保存融资期限

        financingMapper.insert(financing);
        Integer financingId = financing.getFinancingId();

        // 添加主申请人（直接为 accepted）
        FinancingFarmer mainFarmer = new FinancingFarmer();
        mainFarmer.setFinancingId(financingId);
        mainFarmer.setFarmerId(initiatingFarmerId);
        mainFarmer.setRoleInFinancing("主申请人");
        mainFarmer.setInvitationStatus("accepted");
        mainFarmer.setInvitedBy(initiatingFarmerId);
        mainFarmer.setDecisionTime(LocalDateTime.now());
        financingFarmerMapper.insert(mainFarmer);

        // 预填共同申请人为 pending
        if (coApplicantIds != null && !coApplicantIds.isEmpty()) {
            for (Long coApplicantId : coApplicantIds) {
                // 防重复
                LambdaQueryWrapper<FinancingFarmer> existsQ = new LambdaQueryWrapper<>();
                existsQ.eq(FinancingFarmer::getFinancingId, financingId)
                       .eq(FinancingFarmer::getFarmerId, coApplicantId);
                if (financingFarmerMapper.selectCount(existsQ) > 0) continue;

                FinancingFarmer coFarmer = new FinancingFarmer();
                coFarmer.setFinancingId(financingId);
                coFarmer.setFarmerId(coApplicantId);
                coFarmer.setRoleInFinancing("共同申请人");
                coFarmer.setInvitationStatus("pending");
                coFarmer.setInvitedBy(initiatingFarmerId);
                financingFarmerMapper.insert(coFarmer);
            }
        }

        return financingId;
    }

    @Override
    public boolean submitFinancing(Long userId, Integer financingId) {
        Financing financing = financingMapper.selectById(financingId);
        if (financing == null || !financing.getInitiatingFarmerId().equals(userId)) {
            return false;
        }
        if (!"draft".equals(financing.getApplicationStatus())) {
            return false;
        }

        financing.setApplicationStatus("submitted");
        financing.setUpdateTime(LocalDateTime.now());
        return financingMapper.updateById(financing) > 0;
    }

    @Override
    public Page<Financing> listAllFinancings(Integer pageNum, Integer pageSize) {
        Page<Financing> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Financing> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Financing::getCreateTime);
        return financingMapper.selectPage(page, wrapper);
    }

    @Override
    public List<FinancingOffer> listMyOffersForFinancing(Long bankUserId, Integer financingId) {
        LambdaQueryWrapper<FinancingOffer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinancingOffer::getFinancingId, financingId)
                .eq(FinancingOffer::getBankUserId, bankUserId)
                .orderByDesc(FinancingOffer::getOfferTime);
        return financingOfferMapper.selectList(wrapper);
    }

    @Override
    public Page<Financing> listUserFinancings(Long userId, Integer pageNum, Integer pageSize) {
        Page<Financing> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Financing> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Financing::getInitiatingFarmerId, userId)
                .orderByDesc(Financing::getCreateTime);
        return financingMapper.selectPage(page, wrapper);
    }

    @Override
    public Financing getFinancingDetail(Long userId, Integer financingId) {
        Financing financing = financingMapper.selectById(financingId);
        if (financing == null) return null;

        // 查询参与农户
        LambdaQueryWrapper<FinancingFarmer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinancingFarmer::getFinancingId, financingId);
        List<FinancingFarmer> farmers = financingFarmerMapper.selectList(wrapper);
        financing.setFarmers(farmers);

        return financing;
    }

    @Override
    @Transactional
    public boolean rejectOffer(Long farmerId, Integer offerId) {
        // 1. 校验报价是否存在
        FinancingOffer offer = financingOfferMapper.selectById(offerId);
        if (offer == null) {
            return false;
        }

        // 2. 校验融资申请及归属（仅主申请人可操作）
        Financing financing = financingMapper.selectById(offer.getFinancingId());
        if (financing == null || !financing.getInitiatingFarmerId().equals(farmerId)) {
            return false;
        }

        // 3. 仅允许拒绝待处理报价
        if (!"pending".equals(offer.getOfferStatus())) {
            return false;
        }

        // 4. 更新报价状态为已拒绝
        offer.setOfferStatus("rejected");
        return financingOfferMapper.updateById(offer) > 0;
    }

    @Override
    @Transactional
    public boolean cancelFinancing(Long userId, Integer financingId) {
        Financing financing = financingMapper.selectById(financingId);
        if (financing == null || !financing.getInitiatingFarmerId().equals(userId)) {
            return false;
        }
        if (!"draft".equals(financing.getApplicationStatus()) && 
            !"submitted".equals(financing.getApplicationStatus())) {
            return false;
        }

        financing.setApplicationStatus("cancelled");
        financing.setUpdateTime(LocalDateTime.now());
        boolean result = financingMapper.updateById(financing) > 0;

        // 仅对“已接受”的参与人进行信用分调整
        if (result) {
            List<Long> accepted = getAllAcceptedFarmerIds(financingId);
            for (Long fid : accepted) {
                userService.updateCreditScore(fid, "loan_cancel");
            }
        }
        return result;
    }

    @Override
    public Page<Financing> listSubmittedFinancings(Integer pageNum, Integer pageSize) {
        Page<Financing> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Financing> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Financing::getApplicationStatus, "submitted")
                .orderByDesc(Financing::getCreateTime);
        return financingMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public boolean submitOffer(Long bankUserId, Integer financingId, BigDecimal offeredAmount,
                               BigDecimal interestRate, String bankNotes) {
        // 仅允许对 submitted 的融资申请报价
        Financing financing = financingMapper.selectById(financingId);
        if (financing == null || !"submitted".equals(financing.getApplicationStatus())) {
            return false;
        }

        FinancingOffer offer = new FinancingOffer();
        offer.setFinancingId(financingId);
        offer.setBankUserId(bankUserId);
        offer.setOfferedAmount(offeredAmount);
        offer.setOfferedInterestRate(interestRate);
        offer.setBankNotes(bankNotes);
        offer.setOfferStatus("pending");
        offer.setOfferTime(LocalDateTime.now());

        boolean ok = financingOfferMapper.insert(offer) > 0;

        if (ok) {
            financing.setUpdateTime(LocalDateTime.now());
            financingMapper.updateById(financing);
        }

        return ok;
    }

    @Override
    public List<FinancingOffer> listOffers(Long userId, Integer financingId) {
        Financing financing = financingMapper.selectById(financingId);
        if (financing == null || !financing.getInitiatingFarmerId().equals(userId)) {
            return null;
        }

        LambdaQueryWrapper<FinancingOffer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinancingOffer::getFinancingId, financingId)
                .orderByDesc(FinancingOffer::getOfferTime);
        return financingOfferMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public boolean acceptOffer(Long farmerId, Integer offerId) {
        FinancingOffer offer = financingOfferMapper.selectById(offerId);
        if (offer == null) return false;

        Financing financing = financingMapper.selectById(offer.getFinancingId());
        if (financing == null || !financing.getInitiatingFarmerId().equals(farmerId)) {
            return false;
        }

        // 更新报价状态为已接受
        offer.setOfferStatus("accepted");
        financingOfferMapper.updateById(offer);

        // 更新融资申请状态为已批准
        financing.setApplicationStatus("approved");
        financing.setUpdateTime(LocalDateTime.now());
        boolean result = financingMapper.updateById(financing) > 0;

        // 4.1 成功完成融资，所有参与人增加信用分
        if (result) {
            // 主申请人 +10
            userService.updateCreditScore(farmerId, "loan_success");
            // 已接受的共同申请人 +5
            List<Long> coAccepted = getAcceptedCoApplicantIds(financing.getFinancingId());
            for (Long coApplicantId : coAccepted) {
                userService.updateCreditScore(coApplicantId, "joint_loan_success");
            }
        }

        // 拒绝其他待处理的报价
        LambdaQueryWrapper<FinancingOffer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinancingOffer::getFinancingId, offer.getFinancingId())
                .eq(FinancingOffer::getOfferStatus, "pending")
                .ne(FinancingOffer::getOfferId, offerId);
        List<FinancingOffer> otherOffers = financingOfferMapper.selectList(wrapper);
        for (FinancingOffer otherOffer : otherOffers) {
            otherOffer.setOfferStatus("rejected");
            financingOfferMapper.updateById(otherOffer);
        }

        return result;
    }

    @Override
    public Page<FinancingOffer> listBankOffers(Long bankUserId, Integer pageNum, Integer pageSize) {
        Page<FinancingOffer> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FinancingOffer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinancingOffer::getBankUserId, bankUserId)
                .orderByDesc(FinancingOffer::getOfferTime);
        return financingOfferMapper.selectPage(page, wrapper);
    }

    /**
     * 4.3 银行拒绝融资申请，所有参与人都扣分
     */
    @Override
    @Transactional
    public boolean rejectFinancing(Long bankUserId, Integer financingId, String rejectReason) {
        Financing financing = financingMapper.selectById(financingId);
        if (financing == null) return false;

        financing.setApplicationStatus("rejected");
        financing.setUpdateTime(LocalDateTime.now());
        boolean result = financingMapper.updateById(financing) > 0;

        // 被银行拒绝融资，所有参与人都扣除信用分
        if (result) {
            List<Long> accepted = getAllAcceptedFarmerIds(financingId);
            for (Long fid : accepted) {
                userService.updateCreditScore(fid, "loan_rejected");
            }
        }
        return result;
    }

    /**
     * 4.5 拖欠还款，所有参与人都大幅扣分
     */
    @Override
    @Transactional
    public boolean markOverdue(Integer financingId) {
        Financing financing = financingMapper.selectById(financingId);
        if (financing == null) return false;

        financing.setApplicationStatus("overdue");
        financing.setUpdateTime(LocalDateTime.now());
        boolean result = financingMapper.updateById(financing) > 0;

        // 拖欠还款，所有参与人都大幅扣除信用分
        if (result) {
            List<Long> accepted = getAllAcceptedFarmerIds(financingId);
            for (Long fid : accepted) {
                userService.updateCreditScore(fid, "loan_default");
            }
        }
        return result;
    }

    /**
     * 主申请人追加邀请共同申请人
     */
    @Override
    @Transactional
    public boolean inviteCoApplicants(Long inviterId, Integer financingId, List<Long> coApplicantIds) {
        Financing financing = financingMapper.selectById(financingId);
        if (financing == null || !financing.getInitiatingFarmerId().equals(inviterId)) return false;

        if (coApplicantIds == null || coApplicantIds.isEmpty()) return true;

        for (Long coId : coApplicantIds) {
            // 已存在记录则尝试更新为 pending（允许重新邀请被拒绝/取消的）
            LambdaQueryWrapper<FinancingFarmer> qw = new LambdaQueryWrapper<>();
            qw.eq(FinancingFarmer::getFinancingId, financingId)
              .eq(FinancingFarmer::getFarmerId, coId)
              .eq(FinancingFarmer::getRoleInFinancing, "共同申请人");
            FinancingFarmer existing = financingFarmerMapper.selectOne(qw);
            if (existing == null) {
                FinancingFarmer rec = new FinancingFarmer();
                rec.setFinancingId(financingId);
                rec.setFarmerId(coId);
                rec.setRoleInFinancing("共同申请人");
                rec.setInvitationStatus("pending");
                rec.setInvitedBy(inviterId);
                financingFarmerMapper.insert(rec);
            } else {
                UpdateWrapper<FinancingFarmer> uw = new UpdateWrapper<>();
                uw.eq("id", existing.getId())
                  .set("invitation_status", "pending")
                  .set("invited_by", inviterId)
                  .set("decision_time", null);
                financingFarmerMapper.update(null, uw);
            }
        }
        return true;
    }

    /**
     * 共同申请人响应邀请
     */
    @Override
    @Transactional
    public boolean respondInvitation(Long farmerId, Integer financingId, String action) {
        // 仅 pending 状态可响应
        LambdaQueryWrapper<FinancingFarmer> qw = new LambdaQueryWrapper<>();
        qw.eq(FinancingFarmer::getFinancingId, financingId)
          .eq(FinancingFarmer::getFarmerId, farmerId)
          .eq(FinancingFarmer::getRoleInFinancing, "共同申请人")
          .eq(FinancingFarmer::getInvitationStatus, "pending");
        FinancingFarmer rec = financingFarmerMapper.selectOne(qw);
        if (rec == null) return false;

        String next = "reject".equalsIgnoreCase(action) ? "rejected" : "accepted";
        UpdateWrapper<FinancingFarmer> uw = new UpdateWrapper<>();
        uw.eq("id", rec.getId())
          .set("invitation_status", next)
          .set("decision_time", LocalDateTime.now());
        return financingFarmerMapper.update(null, uw) == 1;
    }

    /**
     * 查询共同申请人及邀请状态
     */
    @Override
    public List<FinancingFarmer> listCoApplicants(Integer financingId) {
        LambdaQueryWrapper<FinancingFarmer> qw = new LambdaQueryWrapper<>();
        qw.eq(FinancingFarmer::getFinancingId, financingId)
          .eq(FinancingFarmer::getRoleInFinancing, "共同申请人")
          .orderByAsc(FinancingFarmer::getId);
        return financingFarmerMapper.selectList(qw);
    }

    @Transactional
    public boolean updateFinancingStatus(Integer financingId, String status) {
        Financing financing = financingMapper.selectById(financingId);
        if (financing == null) {
            return false;
        }

        financing.setApplicationStatus(status);
        financing.setUpdateTime(LocalDateTime.now());
        
        boolean updated = financingMapper.updateById(financing) > 0;
        
        if (updated) {
            // 触发用户指标更新
            userMetricsService.updateUserFinancingMetrics(financing.getInitiatingFarmerId());
        }
        
        return updated;
    }

    @Override
    public Page<RecommendedUserDTO> recommendCoApplicants(Long currentUserId, Integer pageNum, Integer pageSize) {
        // 获取当前用户信息
        User currentUser = userMapper.findByUserId(currentUserId);
        if (currentUser == null) {
            return new Page<>();
        }

        // 查询所有农户用户（排除当前用户和非农户角色）
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "farmer") // 只查询农户
                .ne(User::getUserId, currentUserId) // 排除当前用户
                .isNotNull(User::getCreditScore) // 必须有信用分
                .orderByDesc(User::getCreditScore) // 按信用分降序
                .orderByDesc(User::getHistoricalSuccessRate); // 按历史成功率降序

        // 分页查询
        Page<User> userPage = new Page<>(pageNum, pageSize);
        userPage = userMapper.selectPage(userPage, wrapper);

        // 转换为DTO并计算推荐分数
        Page<RecommendedUserDTO> resultPage = new Page<>();
        resultPage.setCurrent(userPage.getCurrent());
        resultPage.setSize(userPage.getSize());
        resultPage.setTotal(userPage.getTotal());

        List<RecommendedUserDTO> recommendedUsers = userPage.getRecords().stream()
                .map(user -> {
                    RecommendedUserDTO dto = new RecommendedUserDTO();
                    dto.setUserId(user.getUserId());
                    dto.setUserName(user.getUserName());
                    dto.setName(user.getName());
                    dto.setRegion(user.getRegion());
                    dto.setCreditScore(user.getCreditScore());
                    dto.setHistoricalSuccessRate(user.getHistoricalSuccessRate());
                    dto.setAverageFinancingAmount(user.getAverageFinancingAmount());
                    dto.setFinancingActivityLevel(user.getFinancingActivityLevel());
                    
                    // 判断是否同地区
                    boolean sameRegion = currentUser.getRegion() != null && 
                                       currentUser.getRegion().equals(user.getRegion());
                    dto.setSameRegion(sameRegion);
                    
                    // 计算推荐分数
                    double score = calculateRecommendScore(user, currentUser);
                    dto.setRecommendScore(score);
                    
                    return dto;
                })
                .sorted((u1, u2) -> {
                    // 先按同地区排序，同地区的在前
                    int regionCompare = Boolean.compare(u2.getSameRegion(), u1.getSameRegion());
                    if (regionCompare != 0) {
                        return regionCompare;
                    }
                    // 再按信用分降序排序
                    int creditCompare = Integer.compare(
                        u2.getCreditScore() != null ? u2.getCreditScore() : 0,
                        u1.getCreditScore() != null ? u1.getCreditScore() : 0
                    );
                    if (creditCompare != 0) {
                        return creditCompare;
                    }
                    // 最后按推荐分数降序排序
                    return Double.compare(u2.getRecommendScore(), u1.getRecommendScore());
                })
                .collect(Collectors.toList());

        resultPage.setRecords(recommendedUsers);
        return resultPage;
    }

    /**
     * 计算推荐分数
     * 综合考虑信用分、历史成功率、地区匹配等因素
     */
    private double calculateRecommendScore(User candidate, User currentUser) {
        double score = 0.0;
        
        // 信用分权重 40%
        if (candidate.getCreditScore() != null) {
            score += candidate.getCreditScore() * 0.4;
        }
        
        // 历史成功率权重 30%
        if (candidate.getHistoricalSuccessRate() != null) {
            score += candidate.getHistoricalSuccessRate().doubleValue() * 0.3;
        }
        
        // 地区匹配权重 20%（同地区加分）
        if (currentUser.getRegion() != null && 
            currentUser.getRegion().equals(candidate.getRegion())) {
            score += 20;
        }
        
        // 融资活跃度权重 10%
        if (candidate.getFinancingActivityLevel() != null) {
            switch (candidate.getFinancingActivityLevel()) {
                case "high":
                    score += 10;
                    break;
                case "medium":
                    score += 5;
                    break;
                case "low":
                    score += 2;
                    break;
            }
        }
        
        return score;
    }
}