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
        financing.setCreateTime(now);
        financing.setUpdateTime(now);
        financingMapper.insert(financing);

        // 添加发起人作为主申请人
        FinancingFarmer initiator = new FinancingFarmer();
        initiator.setFinancingId(financing.getFinancingId());
        initiator.setFarmerId(initiatingFarmerId);
        initiator.setRoleInFinancing("primary_applicant");
        initiator.setCreateTime(now);
        financingFarmerMapper.insert(initiator);

        // 添加共同申请人
        if (coApplicantIds != null && !coApplicantIds.isEmpty()) {
            for (Long farmerId : coApplicantIds) {
                FinancingFarmer coApplicant = new FinancingFarmer();
                coApplicant.setFinancingId(financing.getFinancingId());
                coApplicant.setFarmerId(farmerId);
                coApplicant.setRoleInFinancing("co_applicant");
                coApplicant.setCreateTime(now);
                financingFarmerMapper.insert(coApplicant);
            }
        }

        return financing.getFinancingId();
    }

    @Override
    public boolean submitFinancing(Long userId, Integer financingId) {
        if (userId == null || financingId == null) {
            return false;
        }

        Financing financing = financingMapper.selectById(financingId);
        if (financing == null || !financing.getInitiatingFarmerId().equals(userId)) {
            throw new RuntimeException("无权限提交该融资申请");
        }

        if (!"draft".equals(financing.getApplicationStatus())) {
            throw new RuntimeException("只能提交草稿状态的申请");
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
        if (financing == null) {
            return null;
        }

        // 验证用户是否参与该融资申请
        LambdaQueryWrapper<FinancingFarmer> query = new LambdaQueryWrapper<>();
        query.eq(FinancingFarmer::getFinancingId, financingId)
                .eq(FinancingFarmer::getFarmerId, userId);

        if (financingFarmerMapper.selectCount(query) == 0) {
            throw new RuntimeException("无权限查看该融资申请");
        }

        return financing;
    }

    @Override
    public boolean cancelFinancing(Long userId, Integer financingId) {
        if (userId == null || financingId == null) {
            return false;
        }

        Financing financing = financingMapper.selectById(financingId);
        if (financing == null || !financing.getInitiatingFarmerId().equals(userId)) {
            throw new RuntimeException("无权限取消该融资申请");
        }

        if ("completed".equals(financing.getApplicationStatus()) ||
                "cancelled".equals(financing.getApplicationStatus())) {
            throw new RuntimeException("该融资申请已完成或已取消，无法再次取消");
        }

        financing.setApplicationStatus("cancelled");
        financing.setUpdateTime(LocalDateTime.now());
        return financingMapper.updateById(financing) > 0;
    }

    @Override
    public Page<Financing> listPendingFinancings(Integer pageNum, Integer pageSize) {
        Page<Financing> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Financing> query = new LambdaQueryWrapper<>();
        query.eq(Financing::getApplicationStatus, "submitted")
                .orderByDesc(Financing::getCreateTime);
        return financingMapper.selectPage(page, query);
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
        offer.setOfferedInterestRate(interestRate);
        offer.setOfferStatus("offered");
        offer.setBankNotes(bankNotes);
        offer.setOfferTime(now);
        offer.setUpdateTime(now);

        boolean inserted = financingOfferMapper.insert(offer) > 0;

        // 更新融资申请状态
        if (inserted) {
            financing.setApplicationStatus("offers_received");
            financing.setUpdateTime(now);
            financingMapper.updateById(financing);
        }

        return inserted;
    }

    @Override
    public List<FinancingOffer> listOffers(Long userId, Integer financingId) {
        if (userId == null || financingId == null) {
            return List.of();
        }

        // 验证用户权限
        Financing financing = financingMapper.selectById(financingId);
        if (financing == null || !financing.getInitiatingFarmerId().equals(userId)) {
            throw new RuntimeException("无权限查看该融资申请的报价");
        }

        LambdaQueryWrapper<FinancingOffer> query = new LambdaQueryWrapper<>();
        query.eq(FinancingOffer::getFinancingId, financingId)
                .orderByAsc(FinancingOffer::getOfferedInterestRate);

        return financingOfferMapper.selectList(query);
    }

    @Override
    @Transactional
    public boolean acceptOffer(Long farmerId, Integer offerId) {
        if (farmerId == null || offerId == null) {
            return false;
        }

        FinancingOffer offer = financingOfferMapper.selectById(offerId);
        if (offer == null) {
            throw new RuntimeException("报价不存在");
        }

        Financing financing = financingMapper.selectById(offer.getFinancingId());
        if (financing == null || !financing.getInitiatingFarmerId().equals(farmerId)) {
            throw new RuntimeException("无权限接受该报价");
        }

        if (!"offered".equals(offer.getOfferStatus())) {
            throw new RuntimeException("该报价当前无法接受");
        }

        LocalDateTime now = LocalDateTime.now();

        // 更新被接受的报价状态
        offer.setOfferStatus("accepted_by_farmer");
        offer.setUpdateTime(now);
        financingOfferMapper.updateById(offer);

        // 更新融资申请状态
        financing.setApplicationStatus("offer_accepted");
        financing.setUpdateTime(now);
        financingMapper.updateById(financing);

        // 拒绝其他报价
        LambdaQueryWrapper<FinancingOffer> otherQuery = new LambdaQueryWrapper<>();
        otherQuery.eq(FinancingOffer::getFinancingId, offer.getFinancingId())
                .ne(FinancingOffer::getOfferId, offerId)
                .eq(FinancingOffer::getOfferStatus, "offered");

        List<FinancingOffer> otherOffers = financingOfferMapper.selectList(otherQuery);
        for (FinancingOffer other : otherOffers) {
            other.setOfferStatus("rejected_by_farmer");
            other.setUpdateTime(now);
            financingOfferMapper.updateById(other);
        }

        return true;
    }

    @Override
    public Page<FinancingOffer> listBankOffers(Long bankUserId, Integer pageNum, Integer pageSize) {
        if (bankUserId == null) {
            return new Page<>(pageNum, pageSize);
        }

        Page<FinancingOffer> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FinancingOffer> query = new LambdaQueryWrapper<>();
        query.eq(FinancingOffer::getBankUserId, bankUserId)
                .orderByDesc(FinancingOffer::getOfferTime);

        return financingOfferMapper.selectPage(page, query);
    }
}