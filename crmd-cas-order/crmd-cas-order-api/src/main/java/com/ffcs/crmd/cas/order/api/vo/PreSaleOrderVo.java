package com.ffcs.crmd.cas.order.api.vo;

import com.ffcs.crmd.cas.core.ddd.api.vo.CrmdBaseConditionVo;
import com.ffcs.crmd.cas.sys.api.dto.AttrValueDTO;

import java.sql.Timestamp;
import java.util.List;

public class PreSaleOrderVo extends CrmdBaseConditionVo {

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
     * 查询条件：受理时间-开始时间	格式：yyyy-MM-dd HH:mm:ss
     */
    private String beginTime;

    /**
     * 查询条件：受理时间-结束时间	格式：yyyy-MM-dd HH:mm:ss
     */
    private String endTime;

    /**
     * 状态
     */
    private String statusCd;

    private List<Long> commmonRegionIds;

    /**
     * 员工岗位ID
     */
    private Long staffPositionId;

    /**
     * 撤销时，用于传入主数据配置的撤销原因
     */
    private AttrValueDTO attrValueDTO;

    /**
     * 撤销原因
     */
    private String cancelReason;

    /**
     * attrValueId
     */
    private String cancelReasonId;

    /**
     * attrValue
     */
    private String cancelReasonValue;

    /**
     * 用于接收前端区域
     */
    private String commonRegionStr;

    /**
     * 是否导出
     */
    private String isExport;

    /**
     * 正式单编码
     */
    private String custSoNumber;
    /**
     * from查询按钮
     */
    private String fromQryBtn;

    /**
     * 是否查询已缴完费标识
     */
    private boolean isQueryPayFlag;

    public String getFromQryBtn() {
        return fromQryBtn;
    }

    public void setFromQryBtn(String fromQryBtn) {
        this.fromQryBtn = fromQryBtn;
    }

    public String getIsExport() {
        return isExport;
    }

    public void setIsExport(String isExport) {
        this.isExport = isExport;
    }

    public String getCancelReasonId() {
        return cancelReasonId;
    }

    public void setCancelReasonId(String cancelReasonId) {
        this.cancelReasonId = cancelReasonId;
    }

    public String getCancelReasonValue() {
        return cancelReasonValue;
    }

    public void setCancelReasonValue(String cancelReasonValue) {
        this.cancelReasonValue = cancelReasonValue;
    }

    public String getCommonRegionStr() {
        return commonRegionStr;
    }

    public void setCommonRegionStr(String commonRegionStr) {
        this.commonRegionStr = commonRegionStr;
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

    public String getCustSoNumber() {
        return custSoNumber;
    }

    public void setCustSoNumber(String custSoNumber) {
        this.custSoNumber = custSoNumber;
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

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }

    public List<Long> getCommmonRegionIds() {
        return commmonRegionIds;
    }

    public void setCommmonRegionIds(List<Long> commmonRegionIds) {
        this.commmonRegionIds = commmonRegionIds;
    }

    public Long getStaffPositionId() {
        return staffPositionId;
    }

    public void setStaffPositionId(Long staffPositionId) {
        this.staffPositionId = staffPositionId;
    }

    public AttrValueDTO getAttrValueDTO() {
        return attrValueDTO;
    }

    public void setAttrValueDTO(AttrValueDTO attrValueDTO) {
        this.attrValueDTO = attrValueDTO;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public boolean isQueryPayFlag() {
        return isQueryPayFlag;
    }

    public void setQueryPayFlag(boolean queryPayFlag) {
        isQueryPayFlag = queryPayFlag;
    }
}
