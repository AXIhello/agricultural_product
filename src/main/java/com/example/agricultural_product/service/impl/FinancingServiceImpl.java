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
    public Integer createFinancing(Long initiatingFarmerId, BigDecimal amount, String purpose, List<Long> coApplicantIds) {
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
        mainFarmer.setRoleInFinancing("主申请人");
        financingFarmerMapper.insert(mainFarmer);

        // 添加联合申请人
        if (coApplicantIds != null && !coApplicantIds.isEmpty()) {
            for (Long coApplicantId : coApplicantIds) {
                FinancingFarmer coFarmer = new FinancingFarmer();
                coFarmer.setFinancingId(financingId);
                coFarmer.setFarmerId(coApplicantId);
                coFarmer.setRoleInFinancing("共同申请人");
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
            !"pending".equals(financing.getApplicationStatus())) {
            return false;
        }

        financing.setApplicationStatus("cancelled");
        financing.setUpdateTime(LocalDateTime.now());
        boolean result = financingMapper.updateById(financing) > 0;
        
        // 4.2 取消融资申请，所有参与人都扣除信用分
        if (result) {
            List<Long> allFarmerIds = getAllFarmerIds(financingId);
            for (Long farmerId : allFarmerIds) {
                userService.updateCreditScore(farmerId, "loan_cancel");
            }
        }
        
        return result;
    }

    @Override
    public Page<Financing> listSubmittedFinancings(Integer pageNum, Integer pageSize) {
        Page<Financing> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Financing> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Financing::getApplicationStatus, "submitted","pending")
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

        // 首个报价成功后，将融资状态置为 pending
        if (ok) {
            financing.setApplicationStatus("pending");
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
            // 主申请人获得更高的信用分
            userService.updateCreditScore(farmerId, "loan_success");
            
            // 4.4 共同申请人也增加信用分
            List<Long> coApplicantIds = getCoApplicantIds(financing.getFinancingId());
            for (Long coApplicantId : coApplicantIds) {
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
            List<Long> allFarmerIds = getAllFarmerIds(financingId);
            for (Long farmerId : allFarmerIds) {
                userService.updateCreditScore(farmerId, "loan_rejected");
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
            List<Long> allFarmerIds = getAllFarmerIds(financingId);
            for (Long farmerId : allFarmerIds) {
                userService.updateCreditScore(farmerId, "loan_default");
            }
        }

        return result;
    }
}