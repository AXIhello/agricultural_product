package com.example.agricultural_product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.agricultural_product.pojo.Financing;
import com.example.agricultural_product.pojo.FinancingOffer;
import com.example.agricultural_product.pojo.FinancingFarmer;


import java.math.BigDecimal;
import java.util.List;

public interface FinancingService extends IService<Financing> {

    /**
     * 创建融资申请
     */
    Integer createFinancing(Long initiatingFarmerId, BigDecimal amount, String purpose, Integer term, List<Long> coApplicantIds);

    /**
     * 提交融资申请（从草稿状态提交）
     */
    boolean submitFinancing(Long userId, Integer financingId);

    /**
     * 获取用户的融资申请列表（分页）
     */
    Page<Financing> listUserFinancings(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取融资申请详情（包含参与农户信息）
     */
    Financing getFinancingDetail(Long userId, Integer financingId);

    /**
     * 取消融资申请
     */
    boolean cancelFinancing(Long userId, Integer financingId);

    /**
     * 银行查看待审核的融资申请列表
     */
    Page<Financing> listSubmittedFinancings(Integer pageNum, Integer pageSize);

    /**
     * 银行提交报价
     */
    boolean submitOffer(Long bankUserId, Integer financingId, BigDecimal offeredAmount,
                        BigDecimal interestRate, String bankNotes);

    boolean rejectOffer(Long farmerId, Integer offerId);

    Integer applyBankProduct(Long userId, Integer productId, BigDecimal amount, String purpose, List<Long> coApplicantIds);

    Page<Financing> listProductApplications(Long bankUserId, Integer productId, Integer pageNum, Integer pageSize);


    // 银行按融资ID查看自己提交的报价
    List<FinancingOffer> listMyOffersForFinancing(Long bankUserId, Integer financingId);

    // 银行查看所有状态的融资申请（不按状态过滤）
    Page<Financing> listAllFinancings(Integer pageNum, Integer pageSize);

    /**
     * 查看某融资申请的所有报价
     */
    List<FinancingOffer> listOffers(Long userId, Integer financingId);

    boolean acceptOffer(Long farmerId, Integer offerId);

    Page<FinancingOffer> listBankOffers(Long bankUserId, Integer pageNum, Integer pageSize);

    /**
     * 银行拒绝融资申请
     */
    boolean rejectFinancing(Long bankUserId, Integer financingId, String rejectReason);

    /**
     * 标记拖欠还款
     */
    boolean markOverdue(Integer financingId);

    /**
     * 主申请人邀请共同申请人（支持追加邀请）
     */
    boolean inviteCoApplicants(Long inviterId, Integer financingId, List<Long> coApplicantIds);

    /**
     * 共同申请人响应邀请（accept/reject）
     */
    boolean respondInvitation(Long farmerId, Integer financingId, String action);

    /**
     * 查询融资下共同申请人及邀请状态
     */
    List<FinancingFarmer> listCoApplicants(Integer financingId);
}