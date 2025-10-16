package com.example.agricultural_product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agricultural_product.mapper.FinancingFarmerMapper;
import com.example.agricultural_product.mapper.FinancingMapper;
import com.example.agricultural_product.mapper.FinancingOfferMapper;
import com.example.agricultural_product.pojo.Financing;
import com.example.agricultural_product.pojo.FinancingFarmer;
import com.example.agricultural_product.pojo.FinancingOffer;
import com.example.agricultural_product.service.FinancingService;
import com.example.agricultural_product.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    @Transactional
    public Integer createFinancing(Long initiatingFarmerId, BigDecimal amount, String purpose, List<Long> coApplicantIds) {
        if (initiatingFarmerId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("融资申请信息不完整");
        }

        LocalDateTime now = LocalDateTime.now();

        // 创建融资申请
        Financing financing = new Financing();
        financing.setInitiatingFarmerId(initiatingFarmerId);
        financing.setAmount(amount);
        financing.setPurpose(purpose);
        financing.setApplicationStatus("draft");
        financing.setCreateTime(LocalDateTime.now());
        financing.setUpdateTime(LocalDateTime.now());
        
        financingMapper.insert(financing);
        Integer financingId = financing.getFinancingId();

        // 添加主申请人
        FinancingFarmer mainFarmer = new FinancingFarmer();
        mainFarmer.setFinancingId(financingId);
        mainFarmer.setFarmerId(initiatingFarmerId);
        mainFarmer.setRoleInFinancing("主申请人"); // 修改为使用 roleInFinancing
        financingFarmerMapper.insert(mainFarmer);

        // 添加联合申请人
        if (coApplicantIds != null && !coApplicantIds.isEmpty()) {
            for (Long coApplicantId : coApplicantIds) {
                FinancingFarmer coFarmer = new FinancingFarmer();
                coFarmer.setFinancingId(financingId);
                coFarmer.setFarmerId(coApplicantId);
                coFarmer.setRoleInFinancing("共同申请人"); // 修改为使用 roleInFinancing
                financingFarmerMapper.insert(coFarmer);
            }
        }

        return financingId;
    }

    @Override
    public boolean submitFinancing(Long userId, Integer financingId) {
        if (userId == null || financingId == null) {
            return false;
        }

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
    public Page<Financing> listUserFinancings(Long userId, Integer pageNum, Integer pageSize) {
        if (userId == null) {
            return new Page<>(pageNum, pageSize);
        }

        // 查询用户参与的所有融资申请
        LambdaQueryWrapper<FinancingFarmer> farmerQuery = new LambdaQueryWrapper<>();
        farmerQuery.eq(FinancingFarmer::getFarmerId, userId);
        List<FinancingFarmer> farmerList = financingFarmerMapper.selectList(farmerQuery);

        if (farmerList.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }

        List<Integer> financingIds = farmerList.stream()
                .map(FinancingFarmer::getFinancingId)
                .toList();

        Page<Financing> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Financing> query = new LambdaQueryWrapper<>();
        query.in(Financing::getFinancingId, financingIds)
                .orderByDesc(Financing::getCreateTime);

        return financingMapper.selectPage(page, query);
    }

    @Override
    public Financing getFinancingDetail(Long userId, Integer financingId) {
        if (userId == null || financingId == null) {
            return null;
        }

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
    public boolean cancelFinancing(Long userId, Integer financingId) {
        if (userId == null || financingId == null) {
            return false;
        }

        Financing financing = financingMapper.selectById(financingId);
        if (financing == null || !financing.getInitiatingFarmerId().equals(userId)) {
            return false;
        }
        if (!"draft".equals(financing.getApplicationStatus()) && 
            !"pending".equals(financing.getApplicationStatus())) {
            return false;
        }

        financing.setApplicationStatus("cancelled");
        financing.setUpdateTime(LocalDateTime.now());
        boolean result = financingMapper.updateById(financing) > 0;
        
        // 4.2 取消融资申请，扣除信用分
        if (result) {
            userService.updateCreditScore(userId, "loan_cancel");
        }
        
        return result;
    }

    @Override
    public Page<Financing> listPendingFinancings(Integer pageNum, Integer pageSize) {
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
        if (bankUserId == null || financingId == null || offeredAmount == null || interestRate == null) {
            throw new RuntimeException("报价信息不完整");
        }

        Financing financing = financingMapper.selectById(financingId);
        if (financing == null) {
            throw new RuntimeException("融资申请不存在");
        }

        if (!"submitted".equals(financing.getApplicationStatus())) {
            throw new RuntimeException("该融资申请当前不接受报价");
        }

        // 检查是否已经报价
        LambdaQueryWrapper<FinancingOffer> existQuery = new LambdaQueryWrapper<>();
        existQuery.eq(FinancingOffer::getFinancingId, financingId)
                .eq(FinancingOffer::getBankUserId, bankUserId);
        if (financingOfferMapper.selectCount(existQuery) > 0) {
            throw new RuntimeException("您已对该融资申请提交报价");
        }

        LocalDateTime now = LocalDateTime.now();

        FinancingOffer offer = new FinancingOffer();
        offer.setFinancingId(financingId);
        offer.setBankUserId(bankUserId);
        offer.setOfferedAmount(offeredAmount);
        offer.setOfferedInterestRate(interestRate); // 修正
        offer.setBankNotes(bankNotes);
        offer.setOfferStatus("submitted");
        offer.setOfferTime(LocalDateTime.now());    // 修正
        return financingOfferMapper.insert(offer) > 0;
    }

    @Override
    public List<FinancingOffer> listOffers(Long userId, Integer financingId) {
        if (userId == null || financingId == null) {
            return null;
        }

        // 验证用户权限
        Financing financing = financingMapper.selectById(financingId);
        if (financing == null || !financing.getInitiatingFarmerId().equals(userId)) {
            return null;
        }

        LambdaQueryWrapper<FinancingOffer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinancingOffer::getFinancingId, financingId)
               .orderByDesc(FinancingOffer::getOfferTime); // 修正
        return financingOfferMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public boolean acceptOffer(Long farmerId, Integer offerId) {
        if (farmerId == null || offerId == null) {
            return false;
        }

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

        // 4.1 成功完成融资（接受报价），增加信用分
        if (result) {
            userService.updateCreditScore(farmerId, "loan_success");
            
            // 4.4 参与联合贷款成功，为所有参与农户增加信用分
            LambdaQueryWrapper<FinancingFarmer> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FinancingFarmer::getFinancingId, financing.getFinancingId())
                   .eq(FinancingFarmer::getRoleInFinancing, "co_applicant"); // 用角色替代 isInitiator
            List<FinancingFarmer> coApplicants = financingFarmerMapper.selectList(wrapper);
            
            for (FinancingFarmer coApplicant : coApplicants) {
                userService.updateCreditScore(coApplicant.getFarmerId(), "joint_loan_success");
            }
        }

        // 拒绝其他待处理的报价
        LambdaQueryWrapper<FinancingOffer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinancingOffer::getFinancingId, offer.getFinancingId())
                .eq(FinancingOffer::getOfferStatus, "submitted")
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
        if (bankUserId == null) {
            return new Page<>(pageNum, pageSize);
        }

        Page<FinancingOffer> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FinancingOffer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinancingOffer::getBankUserId, bankUserId)
               .orderByDesc(FinancingOffer::getOfferTime); // 修正
        return financingOfferMapper.selectPage(page, wrapper);
    }

    /**
     * 4.3 银行拒绝融资申请
     */
    @Override
    @Transactional
    public boolean rejectFinancing(Long bankUserId, Integer financingId, String rejectReason) {
        Financing financing = financingMapper.selectById(financingId);
        if (financing == null) return false;

        financing.setApplicationStatus("rejected");
        financing.setUpdateTime(LocalDateTime.now());
        boolean result = financingMapper.updateById(financing) > 0;

        // 被银行拒绝融资，扣除信用分
        if (result) {
            userService.updateCreditScore(financing.getInitiatingFarmerId(), "loan_rejected");
        }

        return result;
    }

    /**
     * 4.5 拖欠还款
     */
    @Override
    @Transactional
    public boolean markOverdue(Integer financingId) {
        Financing financing = financingMapper.selectById(financingId);
        if (financing == null) return false;

        financing.setApplicationStatus("overdue");
        financing.setUpdateTime(LocalDateTime.now());
        boolean result = financingMapper.updateById(financing) > 0;

        // 拖欠还款，大幅扣除信用分
        if (result) {
            userService.updateCreditScore(financing.getInitiatingFarmerId(), "loan_default");
            
            // 联合申请人也扣除信用分
            LambdaQueryWrapper<FinancingFarmer> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FinancingFarmer::getFinancingId, financingId)
                   .eq(FinancingFarmer::getRoleInFinancing, "共同申请人");
            List<FinancingFarmer> coApplicants = financingFarmerMapper.selectList(wrapper);
            
            for (FinancingFarmer coApplicant : coApplicants) {
                userService.updateCreditScore(coApplicant.getFarmerId(), "loan_default");
            }
        }

        return result;
    }
}