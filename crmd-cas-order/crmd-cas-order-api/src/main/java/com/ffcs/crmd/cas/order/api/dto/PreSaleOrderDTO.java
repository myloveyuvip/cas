package com.ffcs.crmd.cas.order.api.dto;

import com.ffcs.crmd.cas.core.ddd.api.dto.impl.CrmdBaseDTO;

import java.sql.Timestamp;

public class PreSaleOrderDTO extends CrmdBaseDTO {
    private static final long serialVersionUID = 1598179584838497003L;

    private Long preOrderId;

    /**
     *
     */
    private Long regionCd;

    /**
     *
     */
    private Long areaId;

    /**
     *
     */
    private Long lockedStaff;

    /**
     *
     */
    private String orderFrom;

    /**
     *
     */
    private String sceneType;

    /**
     *
     */
    private Long custId;

    /**
     *
     */
    private Long customerInteractionEventId;

    /**
     * ҵ
     */
    private String serviceType;

    /**
     *
     */
    private Long extCustOrderId;

    /**
     *
     */
    private String orderType;

    /**
     *
     */
    private Long shardingId;

    /**
     *
     */
    private String actionType;

    /**
     *
     */
    private String priority;

    /**
     *
     */
    private String preOrderNumber;

    /**
     *
     */
    private Long seq;

    /**
     *
     */
    private Timestamp acceptTime;

    /**
     *
     */
    private Long orgId;

    /**
     *
     */
    private Long channelD;

    /**
     *
     */
    private String lockedStatus;

    /**
     *
     */
    private Long dtimestamp;

    /**
     *
     */
    private Long dversion;

    /**
     * Ա
     */
    private Long staffId;

    /**
     * ԤԼʱ
     */
    private Timestamp bookTime;

    /**
     *
     */
    private String prodType;

    /**
     *
     */
    private String handlePeopleName;

    /**
     *
     */
    private Timestamp lockedTime;

    /**
     *
     */
    private String preHandleFlag;

    /**
     *
     */
    private String ifEnd;

    /**
     *
     */
    private Long lanId;

    /**
     * 业务类型名称
     */
    private String sceneTypeName;

    /**
     * 员工名称
     */
    private String staffName;

    /**
     * 组织名称
     */
    private String orgName;

    /**
     * 状态名称
     */
    private String statusCdName;

    /**
     * 真实受理时间
     */
    private Timestamp realAcceptTime;

    private Long batchAmount;

    /**
     * 机器人受理标识
     * false 不展示
     * true 展示
     */
    private boolean isRobotDeal;

    /**
     * 正式单编码
     */
    private String custSoNumber;

    /**
     * 客户名称
     */
    private String custName;

    /**
     * 员工电话
     */
    private String mobilePhone;

    //订单来源
    private String preOrderSrc;
    /**
     * 实时受理带预受理费用已缴完费标识
     */
    private String realAcceptHasPayFlag;
    /**
     * 发展员工.
     */
    private String devStaff;
    /**
     * 发展团队.
     */
    private String devTeam;

    public String getDevStaff() {
		return devStaff;
	}

	public void setDevStaff(String devStaff) {
		this.devStaff = devStaff;
	}

	public String getDevTeam() {
		return devTeam;
	}

	public void setDevTeam(String devTeam) {
		this.devTeam = devTeam;
	}

	public void setPreOrderSrc(String preOrderSrc) {
        this.preOrderSrc = preOrderSrc;
    }

    public String getPreOrderSrc() {
        return this.preOrderSrc;
    }

    public boolean isRobotDeal() {
        return isRobotDeal;
    }

    public void setRobotDeal(boolean robotDeal) {
        isRobotDeal = robotDeal;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public void setPreOrderId(Long preOrderId) {
        this.preOrderId = preOrderId;
    }

    public Long getPreOrderId() {
        return this.preOrderId;
    }

    public void setRegionCd(Long regionCd) {
        this.regionCd = regionCd;
    }

    public Long getRegionCd() {
        return this.regionCd;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Long getAreaId() {
        return this.areaId;
    }

    public void setLockedStaff(Long lockedStaff) {
        this.lockedStaff = lockedStaff;
    }

    public Long getLockedStaff() {
        return this.lockedStaff;
    }

    public void setOrderFrom(String orderFrom) {
        this.orderFrom = orderFrom;
    }

    public String getOrderFrom() {
        return this.orderFrom;
    }

    public void setSceneType(String sceneType) {
        this.sceneType = sceneType;
    }

    public String getSceneType() {
        return this.sceneType;
    }

    public void setCustId(Long custId) {
        this.custId = custId;
    }

    public Long getCustId() {
        return this.custId;
    }

    public void setCustomerInteractionEventId(Long customerInteractionEventId) {
        this.customerInteractionEventId = customerInteractionEventId;
    }

    public Long getCustomerInteractionEventId() {
        return this.customerInteractionEventId;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceType() {
        return this.serviceType;
    }

    public void setExtCustOrderId(Long extCustOrderId) {
        this.extCustOrderId = extCustOrderId;
    }

    public Long getExtCustOrderId() {
        return this.extCustOrderId;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getOrderType() {
        return this.orderType;
    }

    public void setShardingId(Long shardingId) {
        this.shardingId = shardingId;
    }

    public Long getShardingId() {
        return this.shardingId;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionType() {
        return this.actionType;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getPriority() {
        return this.priority;
    }

    public void setPreOrderNumber(String preOrderNumber) {
        this.preOrderNumber = preOrderNumber;
    }

    public String getPreOrderNumber() {
        return this.preOrderNumber;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public Long getSeq() {
        return this.seq;
    }

    public void setAcceptTime(Timestamp acceptTime) {
        this.acceptTime = acceptTime;
    }

    public Timestamp getAcceptTime() {
        return this.acceptTime;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getOrgId() {
        return this.orgId;
    }

    public void setChannelD(Long channelD) {
        this.channelD = channelD;
    }

    public Long getChannelD() {
        return this.channelD;
    }

    public void setLockedStatus(String lockedStatus) {
        this.lockedStatus = lockedStatus;
    }

    public String getLockedStatus() {
        return this.lockedStatus;
    }

    public void setDtimestamp(Long dtimestamp) {
        this.dtimestamp = dtimestamp;
    }

    public Long getDtimestamp() {
        return this.dtimestamp;
    }

    public void setDversion(Long dversion) {
        this.dversion = dversion;
    }

    public Long getDversion() {
        return this.dversion;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public Long getStaffId() {
        return this.staffId;
    }

    public void setBookTime(Timestamp bookTime) {
        this.bookTime = bookTime;
    }

    public Timestamp getBookTime() {
        return this.bookTime;
    }

    public void setProdType(String prodType) {
        this.prodType = prodType;
    }

    public String getProdType() {
        return this.prodType;
    }

    public void setHandlePeopleName(String handlePeopleName) {
        this.handlePeopleName = handlePeopleName;
    }

    public String getHandlePeopleName() {
        return this.handlePeopleName;
    }

    public void setLockedTime(Timestamp lockedTime) {
        this.lockedTime = lockedTime;
    }

    public Timestamp getLockedTime() {
        return this.lockedTime;
    }

    public void setPreHandleFlag(String preHandleFlag) {
        this.preHandleFlag = preHandleFlag;
    }

    public String getPreHandleFlag() {
        return this.preHandleFlag;
    }

    public void setIfEnd(String ifEnd) {
        this.ifEnd = ifEnd;
    }

    public String getIfEnd() {
        return this.ifEnd;
    }

    public void setLanId(Long lanId) {
        this.lanId = lanId;
    }

    public Long getLanId() {
        return this.lanId;
    }

    public String getSceneTypeName() {
        return sceneTypeName;
    }

    public void setSceneTypeName(String sceneTypeName) {
        this.sceneTypeName = sceneTypeName;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getStatusCdName() {
        return statusCdName;
    }

    public void setStatusCdName(String statusCdName) {
        this.statusCdName = statusCdName;
    }

    public Timestamp getRealAcceptTime() {
        return realAcceptTime;
    }

    public void setRealAcceptTime(Timestamp realAcceptTime) {
        this.realAcceptTime = realAcceptTime;
    }

    public Long getBatchAmount() {
        return batchAmount;
    }

    public void setBatchAmount(Long batchAmount) {
        this.batchAmount = batchAmount;
    }

    public String getCustSoNumber() {
        return custSoNumber;
    }

    public void setCustSoNumber(String custSoNumber) {
        this.custSoNumber = custSoNumber;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getRealAcceptHasPayFlag() {
        return realAcceptHasPayFlag;
    }

    public void setRealAcceptHasPayFlag(String realAcceptHasPayFlag) {
        this.realAcceptHasPayFlag = realAcceptHasPayFlag;
    }
}
