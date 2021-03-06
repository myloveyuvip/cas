package com.ffcs.crmd.cas.order.api.dto;

import com.ffcs.crmd.cas.core.ddd.api.dto.impl.CrmdBaseDTO;


public class InteractionAssignOrgDTO extends CrmdBaseDTO {
	private static final long serialVersionUID = 1598179584838497003L;

	private Long interactionAssignOrgId;

	/**
	 * 
	 */
	private Long regionCd;

	/**
	 * 
	 */
	private Long shardingId;

	/**
	 * 
	 */
	private Long productId;

	/**
	 * ҵ
	 */
	private String custItemType;

	/**
	 * 
	 */
	private Long areaId;

	/**
	 * 
	 */
	private Long interactionFlowId;

	/**
	 * 
	 */
	private Long commonRegionId;

	/**
	 * 
	 */
	private Long cfourRegionId;

	/**
	 * ¼
	 */
	private Long orgId;

	/**
	 * 录单团队名称
	 */
	private String orgName;

	/**
	 * 
	 */
	private Long dtimestamp;

	/**
	 * 
	 */
	private Long dversion;

	/**
	 * 
	 */
	private Long executeOrgId;

	/**
	 * 团队名称
	 */
	private String executeOrgName;

	/**
	 * ҵ
	 */
	private String chooseType;

	/**
	 * 三级区域名称
	 */
	private String commonRegionName;

	/**
	 * 四级区域名称
	 */
	private String cfourRegionName;


	public void setInteractionAssignOrgId(Long interactionAssignOrgId){
		this.interactionAssignOrgId = interactionAssignOrgId;
	}

	public Long getInteractionAssignOrgId(){
		return this.interactionAssignOrgId;
	}

	public void setRegionCd(Long regionCd){
		this.regionCd = regionCd;
	}

	public Long getRegionCd(){
		return this.regionCd;
	}

	public void setShardingId(Long shardingId){
		this.shardingId = shardingId;
	}

	public Long getShardingId(){
		return this.shardingId;
	}

	public void setProductId(Long productId){
		this.productId = productId;
	}

	public Long getProductId(){
		return this.productId;
	}

	public void setCustItemType(String custItemType){
		this.custItemType = custItemType;
	}

	public String getCustItemType(){
		return this.custItemType;
	}

	public void setAreaId(Long areaId){
		this.areaId = areaId;
	}

	public Long getAreaId(){
		return this.areaId;
	}

	public void setInteractionFlowId(Long interactionFlowId){
		this.interactionFlowId = interactionFlowId;
	}

	public Long getInteractionFlowId(){
		return this.interactionFlowId;
	}

	public void setCommonRegionId(Long commonRegionId){
		this.commonRegionId = commonRegionId;
	}

	public Long getCommonRegionId(){
		return this.commonRegionId;
	}

	public void setCfourRegionId(Long cfourRegionId){
		this.cfourRegionId = cfourRegionId;
	}

	public Long getCfourRegionId(){
		return this.cfourRegionId;
	}

	public void setOrgId(Long orgId){
		this.orgId = orgId;
	}

	public Long getOrgId(){
		return this.orgId;
	}

	public void setDtimestamp(Long dtimestamp){
		this.dtimestamp = dtimestamp;
	}

	public Long getDtimestamp(){
		return this.dtimestamp;
	}

	public void setDversion(Long dversion){
		this.dversion = dversion;
	}

	public Long getDversion(){
		return this.dversion;
	}

	public void setExecuteOrgId(Long executeOrgId){
		this.executeOrgId = executeOrgId;
	}

	public Long getExecuteOrgId(){
		return this.executeOrgId;
	}

	public void setChooseType(String chooseType){
		this.chooseType = chooseType;
	}

	public String getChooseType(){
		return this.chooseType;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getExecuteOrgName() {
		return executeOrgName;
	}

	public void setExecuteOrgName(String executeOrgName) {
		this.executeOrgName = executeOrgName;
	}

	public String getCommonRegionName() {
		return commonRegionName;
	}

	public void setCommonRegionName(String commonRegionName) {
		this.commonRegionName = commonRegionName;
	}

	public String getCfourRegionName() {
		return cfourRegionName;
	}

	public void setCfourRegionName(String cfourRegionName) {
		this.cfourRegionName = cfourRegionName;
	}
}
