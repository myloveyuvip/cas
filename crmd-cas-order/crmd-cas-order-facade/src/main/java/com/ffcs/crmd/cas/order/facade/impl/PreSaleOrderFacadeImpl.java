package com.ffcs.crmd.cas.order.facade.impl;

import com.ctg.itrdc.platform.common.entity.PageInfo;
import com.ctg.itrdc.platform.common.exception.RtManagerException;
import com.ctg.itrdc.platform.common.utils.bean.BeanUtils;
import com.ctg.itrdc.platform.common.utils.type.*;
import com.ctg.itrdc.platform.pub.util.ApplicationContextUtil;
import com.ffcs.crmd.cas.base.context.CasSessionContext;
import com.ffcs.crmd.cas.base.utils.CrmClassUtils;
import com.ffcs.crmd.cas.bean.casbean.cancel.cancelordercommit.PreOrder;
import com.ffcs.crmd.cas.bean.casbean.presaleordercommit.PreSaleOrderInfo;
import com.ffcs.crmd.cas.bean.crmbean.queryParty.PartyContactInfo;
import com.ffcs.crmd.cas.bean.crmbean.querycustorder.CustomerOrder;
import com.ffcs.crmd.cas.constants.CasConstant;
import com.ffcs.crmd.cas.constants.IntfConstant;
import com.ffcs.crmd.cas.constants.OrderConstant;
import com.ffcs.crmd.cas.core.ddd.entity.impl.AbsCrmdBaseEntity;
import com.ffcs.crmd.cas.core.ddd.facade.impl.CrmdBaseFacade;
import com.ffcs.crmd.cas.intf.api.dto.*;
import com.ffcs.crmd.cas.intf.api.facade.ICasToCrmFacade;
import com.ffcs.crmd.cas.intf.api.facade.ICasToPadFacade;
import com.ffcs.crmd.cas.intf.api.facade.ICasToSmsFacade;
import com.ffcs.crmd.cas.intf.api.facade.ICrmdToHbFacade;
import com.ffcs.crmd.cas.order.api.dto.*;
import com.ffcs.crmd.cas.order.api.facade.IAutoGenOrderFacade;
import com.ffcs.crmd.cas.order.api.facade.IPreSaleOrderFacade;
import com.ffcs.crmd.cas.order.api.vo.CancelPreOrderVo;
import com.ffcs.crmd.cas.order.api.vo.PreSaleOrderVo;
import com.ffcs.crmd.cas.order.entity.*;
import com.ffcs.crmd.cas.order.repository.IIsaleAcctItemRepository;
import com.ffcs.crmd.cas.order.service.*;
import com.ffcs.crmd.cas.order.vo.OrderOverTimeStatVo;
import com.ffcs.crmd.cas.order.vo.PreSaleOrderProcVo;
import com.ffcs.crmd.cas.sys.api.dto.AttrValueDTO;
import com.ffcs.crmd.cas.sys.api.dto.OrganizationDTO;
import com.ffcs.crmd.cas.sys.api.dto.StaffDTO;
import com.ffcs.crmd.cas.sys.api.dto.StaffPositionDTO;
import com.ffcs.crmd.cas.sys.api.facade.ICasSysFacade;
import com.ffcs.crmd.cas.sys.entity.Organization;
import com.ffcs.crmd.cas.sys.entity.Staff;
import com.ffcs.crmd.cas.sys.entity.StaffPosition;
import com.ffcs.crmd.cas.sys.service.IStaffService;
import com.ffcs.crmd.platform.data.utils.CrmEntityUtils;
import com.ffcs.crmd.platform.meta.entity.AttrSpec;
import com.ffcs.crmd.platform.meta.entity.AttrValue;
import com.ffcs.crmd.platform.pub.bean.CrmBeanUtils;
import com.ffcs.crmd.platform.pub.ex.ExceptionUtils;
import com.ffcs.crmd.platform.pub.vo.RetVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service("preSaleOrderFacade")
public class PreSaleOrderFacadeImpl extends CrmdBaseFacade implements IPreSaleOrderFacade {
	@Autowired
	IPreSaleOrderService preSaleOrderService;
	@Autowired
	private IPreSaleAcctItemService      preSaleAcctItemService;
	@Autowired
	private IPreSaleOrderAttrService     preSaleOrderAttrService;
	@Autowired
	private ICasSysFacade                casSysFacade;
	@Autowired
	private ICrmdToHbFacade              crmdToHbFacade;
	@Autowired
	private ICasToSmsFacade              casToSmsFacade;
	@Autowired
	private ICasToPadFacade              casToPadFacade;
	@Autowired
	private ICasToCrmFacade              casToCrmFacade;
	@Autowired
	private IPreSaleOrderProcService     preSaleOrderProcService;
	@Autowired
	private IPreSaleOrderProcAttrService preSaleOrderProcAttrService;
	@Autowired
	private IPreSaleOrderPoolService     preSaleOrderPoolService;
	@Autowired
	private IPreSaleOrderProcHisService  preSaleOrderProcHisService;
	@Autowired
	private IIntfPreOrderService         intfPreOrderService;
	@Autowired
	private IIntfPreOrderAttachService   intfPreOrderAttachService;
	@Autowired
	private IPreSaleOrderHisService      preSaleOrderHisService;
	@Autowired
	private IStaffService                staffService;
	@Autowired
	private IAutoGenOrderFacade          autoGenOrderFacade;

	@Override
	public PreSaleOrderDTO getById(Long preSaleOrderId, Long shardingId) {
		PreSaleOrder preSaleOrder = preSaleOrderService.getByIdAndShardingId(preSaleOrderId, shardingId);
		PreSaleOrderDTO preSaleOrderDTO = new PreSaleOrderDTO();
		CrmBeanUtils.applyIf(preSaleOrderDTO, preSaleOrder);
		return preSaleOrderDTO;
	}

	@Override
	public PageInfo queryPreSaleOrder(PreSaleOrderVo preSaleOrderVo) {
		PageInfo pageInfo = preSaleOrderService.queryPreSaleOrder(preSaleOrderVo);
		if (pageInfo != null && CollectionUtils.isNotEmpty(pageInfo.getList())) {
			try {
				List<PreSaleOrderDTO> preSaleOrderDTOs = BeanUtils.copyList(pageInfo.getList(), PreSaleOrderDTO.class);
				//优化：批量查出员工名称/团队名称等
				Set staffIds = new HashSet();
				Set orgIds = new HashSet();
				for (PreSaleOrderDTO dto : preSaleOrderDTOs) {
					staffIds.add(dto.getStaffId());
					orgIds.add(dto.getOrgId());
				}
				Map<Long, String> staffNameMap = casSysFacade.queryStaffNames(staffIds);
				Map<Long, String> orgNameMap = casSysFacade.queryOrgNames(orgIds);
				for (PreSaleOrderDTO preSaleOrderDTO : preSaleOrderDTOs) {
					// 2.查询业务类型
					if (!StringUtils.isNullOrEmpty(preSaleOrderDTO.getSceneType())) {
						String sceneTypeName = CrmClassUtils.getAttrValueNameByValue(
								CasConstant.PRE_SALE_ORDER.getValue(), CasConstant.SCENE_TYPE.getValue(),
								preSaleOrderDTO.getSceneType());
						preSaleOrderDTO.setSceneTypeName(sceneTypeName);
					}
					// 3.查询预受理员工名称
					if (!StringUtils.isNullOrEmpty(preSaleOrderDTO.getStaffId())) {
						preSaleOrderDTO.setStaffName(staffNameMap.get(preSaleOrderDTO.getStaffId()));
					}
					// 4.查询参预受理团队名称
					if (!StringUtils.isNullOrEmpty(preSaleOrderDTO.getOrgId())) {
						preSaleOrderDTO.setOrgName(orgNameMap.get(preSaleOrderDTO.getOrgId()));
					}
					// 5.查询状态名称
					if (!StringUtils.isNullOrEmpty(preSaleOrderDTO.getStatusCd())) {
						String statusCdName = CrmClassUtils.getAttrValueNameByValue(
								CasConstant.PRE_SALE_ORDER.getValue(), CasConstant.STATUS_CD.getValue(),
								preSaleOrderDTO.getStatusCd());
						preSaleOrderDTO.setStatusCdName(statusCdName);
					}
					// 6.真实受理时间：从proc表中按acceptTime倒序取出第一条
					PreSaleOrderProc proc = new PreSaleOrderProc();
					proc.setPreOrderId(preSaleOrderDTO.getPreOrderId());
					List<PreSaleOrderProc> preSaleOrderProcs = preSaleOrderProcService.queryPreSaleOrderProc(proc);
					if (preSaleOrderProcs != null && preSaleOrderProcs.size() > 0) {
						Timestamp realAcceptTime = preSaleOrderProcs.get(0).getAcceptTime();
						preSaleOrderDTO.setRealAcceptTime(realAcceptTime);
					}
					// 7.P订单属性值realAcceptHasPayFlag（实时受理带预受理费用已缴完费标识）
					if (preSaleOrderVo.isQueryPayFlag()) {
						PreSaleOrderAttr preSaleOrderAttr = preSaleOrderAttrService
								.getByOrderAndAttrId(preSaleOrderDTO.getPreOrderId(), 950020570L);
						if (preSaleOrderAttr != null) {
							preSaleOrderDTO.setRealAcceptHasPayFlag(preSaleOrderAttr.getAttrValue());
						}
					}
				}
				pageInfo.setList(preSaleOrderDTOs);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return pageInfo;
	}

	/**
	 * 通过预受理编号查询预受理单ID
	 *
	 * @author YULIYAO 2016/4/1 return
	 */
	@Override
	public List<Long> queryExtCustOrderIdByNo(String preOrderNumber) {
		List<Long> extCustOrderIds = new ArrayList<>();
		if (!StringUtils.isNullOrEmpty(preOrderNumber)) {
			Map param = new HashMap();
			param.put("preOrderNumber", preOrderNumber);
			List<PreSaleOrder> preSaleOrders = preSaleOrderService.queryPreSaleOrderList(param);
			if (CollectionUtils.isNotEmpty(preSaleOrders)) {
				for (PreSaleOrder preSaleOrder : preSaleOrders) {
					if (preSaleOrder.getExtCustOrderId() != null && preSaleOrder.getExtCustOrderId() != 0) {
						extCustOrderIds.add(preSaleOrder.getExtCustOrderId());
					}
				}
			}
		}
		return extCustOrderIds;

	}

	@Override
	public boolean reFee(Long preOrderId, List<AttrValueDTO> attrValueDTOList) {
		// 转化attrValueDTOList
		List<AttrValue> attrValues = CrmBeanUtils.copyList(attrValueDTOList, AttrValue.class);
		// 设置员工,员工优先取扩展属性表
		AttrSpec payStaffSpec = CrmClassUtils.getAttrSpecByCode(CasConstant.PRE_SALE_ORDER.getValue(),
						CasConstant.JAVACODE_PRE_SALE_PAY_STAFF_POSITION.getValue());
		StaffPositionDTO staffPositionDTO = null;
		if (payStaffSpec != null) {
			PreSaleOrderAttr preSaleOrderAttr = preSaleOrderAttrService
					.getByOrderAndAttrId(preOrderId, payStaffSpec.getAttrId());
			if (preSaleOrderAttr != null) {
				staffPositionDTO = casSysFacade
						.getStaffPositionById(NumberUtils.toLong(preSaleOrderAttr.getAttrValue()));
			}
		}
		Long staffId = staffPositionDTO != null ?
				staffPositionDTO.getStaffId() :
				CasSessionContext.getContext().getStaffId();
		preSaleOrderService.submitReFee(preOrderId, attrValues, staffId);
		return true;
	}

	@Override
	public String cancel(PreSaleOrderVo vo) {
		String retMsg = "";
		// 1.判断是否满足撤销条件
		PreSaleOrder preSaleOrder = preSaleOrderService.getByIdAndShardingId(vo.getPreOrderId(), vo.getPreOrderId());
		if (preSaleOrder == null) {
			ExceptionUtils.throwEx("不存在此订单");
		}
		// 1.1 如果P单关联的正式订单存在在途单，则不允许撤销 crm00061645 add:针对批量单撤销不进2表的情况，这里判断是否撤销状态
		QueryCustOrderOutDTO orderOutDTO = queryCustOrder(preSaleOrder.getPreOrderNumber(),
				preSaleOrder.getExtCustOrderId(), "0");
		if (ArrayUtils.isNotEmpty(orderOutDTO.getCustomerOrder())) {
			for (CustomerOrder customerOrder : orderOutDTO.getCustomerOrder()) {
				if (!CasConstant.CUST_ORDER_STATUS_CD_CANCEL.getValue()
						.equals(customerOrder.getStatusCd())) {
					ExceptionUtils.throwEx("关联的正式订单存在在途单，不允许撤销!");
				}
			}
		}
		// 1.2 FJCRMV2.0_BUG_翼支付付款方式的甩单不允许做撤销 - chenjw 20150722 crm00063695
		if (CrmClassUtils.hasSwitch("switch_crm00063695")) {
			if (checkHasYZFPreFee(vo.getPreOrderNumber(), vo.getOrgId(), vo.getStaffPositionId())) {
				ExceptionUtils.throwEx("当前预受理订单有预收费用，并且缴费方式是翼支付支付，暂时不支持撤销。");
			}
		}
		// 2.添加处理过程记录
		AttrValueDTO attrValueDTO = vo.getAttrValueDTO();
		if (attrValueDTO == null) {
			// 如果没有选择退单原因类型，则仅保存attr_id,及默认原因
			attrValueDTO = new AttrValueDTO();
			Long attrId = CrmClassUtils.getAttrSpecByCode(PreSaleOrderProc.class.getSimpleName(),
					"backReasonList") != null
					? CrmClassUtils.getAttrSpecByCode(PreSaleOrderProc.class.getSimpleName(), "backReasonList")
					.getId()
					: 0L;
			attrValueDTO.setAttrId(attrId);
			attrValueDTO.setAttrValueId(NumberUtils.nullToLongZero(IntfConstant.GO_BACK_ATTR_ID.getValue()));
			attrValueDTO.setAttrValue(IntfConstant.GO_BACK_ATTR_VALUE_NAME.getValue());
		}
		Long eventId = NumberUtils.nullToLongZero(IntfConstant.PRE_PROC_EVENT_ID_CANCEL.getValue());
		createPreProc(preSaleOrder.getPreOrderId(), preSaleOrder.getShardingId(),
				CasSessionContext.getContext().getStaffId(), Arrays.asList(attrValueDTO), eventId,
				vo.getCancelReason());
		// 4.撤销P订单，如果没有剩余预收费用，则将P订单移到二表 - chenjw 20150513 crm00061645
		preSaleOrder.setIfEnd("1"); // 标识为已结单
		StaffPositionDTO staffPositionDTO = casSysFacade
				.getStaffPositionByStaffIdAndOrgId(preSaleOrder.getStaffId(),
						preSaleOrder.getOrgId());
		Long staffPositionId =
				staffPositionDTO == null ? 0L : staffPositionDTO.getStaffPositionId();
		Long remainPreFee = getRemainPreFeeFromHb(vo.getPreOrderNumber(), preSaleOrder.getOrgId(), staffPositionId);
		if (remainPreFee != null && remainPreFee > 0L) {
			// 4.1 有剩余预收费用
			preSaleOrder.setStatusCd(IntfConstant.PRE_SALE_ORDER_STATUS_CD_BACK_PAYING.getValue()); // P单状态设置为撤销退费
			preSaleOrder.update();
			retMsg = "撤销成功！请退费。";
		} else {
			// 4.2 没有剩余预收费用
			preSaleOrder.setStatusCd(IntfConstant.PRE_SALE_ORDER_STATUS_CD_CANCEL_COMPLETE.getValue()); // 标识为已撤销
			preSaleOrder.update();
			// 4.2.1 关联费用项标识为已撤销
			PreSaleAcctItem paramItem = new PreSaleAcctItem();
			paramItem.setPreOrderId(vo.getPreOrderId());
			paramItem.setShardingId(vo.getPreOrderId());
			List<PreSaleAcctItem> acctItems = preSaleAcctItemService.queryPreSaleAcctItem(paramItem);
			if (CollectionUtils.isNotEmpty(acctItems)) {
				for (PreSaleAcctItem acctItem : acctItems) {
					acctItem.setStatusCd(IntfConstant.PRE_SALE_ACCT_ITEM_STATUS_CANCEL.getValue());
					acctItem.update();
				}
			}
			// 4.2.2 P单记录以及相关挪到历史表
			preSaleOrderService.remove(preSaleOrder);
			retMsg = "撤销成功！";
			// 5.通知移动客户端已撤销
			PreSaleOrderDTO preSaleOrderDTO = new PreSaleOrderDTO();
			CrmBeanUtils.applyIf(preSaleOrderDTO, preSaleOrder);
			stateChange(preSaleOrderDTO, IntfConstant.MOBI_PRE_ORDER_STATUS_CD_REMOVED.getValue());
		}
		// 6.向销售人员发送短信
		String msgContent = "您受理的预受理订单：" + preSaleOrder.getPreOrderNumber() + "已被撤销。撤销原因："+vo.getCancelReason();
		sentMsg(preSaleOrder.getMobilePhone(), msgContent, 1L);
		return retMsg;
	}

	/**
	 *
	 * @author YULIYAO 2016/1/20 return
	 */
	@Override
	public RetVo callIntfPreFeeQuery(String preOrderNumber, Long orgId, Long staffPositionId) {
		PreFeeQueryInDTO input = new PreFeeQueryInDTO();
		input.setPreCustSoNum(preOrderNumber);
		input.setTeamId(NumberUtils.nullToZero(orgId));
		if (orgId != null && orgId != 0) {
			OrganizationDTO organizationDTO = casSysFacade.getById(orgId);
			input.setLatnId(organizationDTO != null ? StringUtils.strnull(organizationDTO.getAreaCode()) : null);
		}
		input.setStaffPositionId(NumberUtils.nullToZero(staffPositionId));
		return crmdToHbFacade.preFeeQuery(input);
	}

	/**
	 * 判断P单下的预收费用中，是否存在翼支付缴费的费用项.
	 *
	 * @return
	 * @author chenjw 2015-7-22
	 */
	private boolean checkHasYZFPreFee(String preOrderNumber, Long orgId, Long staffPositionId) {
		boolean result = false;
		RetVo retVo = callIntfPreFeeQuery(preOrderNumber, orgId, staffPositionId);
		if (retVo != null && retVo.getResult() && retVo.getObject() != null) {
			PreFeeQueryOutDTO output = (PreFeeQueryOutDTO) retVo.getObject();
			if (output.getFeeItem() != null && output.getFeeItemCount() > 0) {
				for (int i = 0; i < output.getFeeItemCount(); i++) {
					// 金额大于0并且是已缴费状态，并且支付方式是翼支付支付
					if (IntfConstant.PRE_FEE_STATE_PAYED.getValue().equals(output.getFeeItem()[i].getState())
							&& !StringUtils.isNullOrEmpty(output.getFeeItem()[i].getAmount())
							&& output.getFeeItem()[i].getAmount() > 0 && !IntfConstant.PRE_FEE_CHARGE_METHOD_YZF
									.getValue().equals(output.getFeeItem()[i].getChargeMethod())) {
						return true;
					}
				}
			}
		}
		return result;
	}

	/**
	 * 调用计费查询剩余的预存款.
	 *
	 * @return
	 * @author chenjw 2014-12-4
	 */
	@Override
	public Long getRemainPreFeeFromHb(String preOrderNumber, Long orgId, Long staffPositionId) {
		Long result = 0L;
		RetVo retVo = callIntfPreFeeQuery(preOrderNumber, orgId, staffPositionId);
		if (!retVo.getResult()) {
			ExceptionUtils.throwEx("查询计费剩余预存款失败，失败原因:" + retVo.getMsgTitle());
		}
		if (retVo.getObject() != null) {
			com.ffcs.crmd.cas.bean.hbbean.prefeequery.OutParam output = (com.ffcs.crmd.cas.bean.hbbean.prefeequery.OutParam) retVo
					.getObject();
			if (output.getFeeItem() != null && output.getFeeItemCount() > 0) {
				for (int i = 0; i < output.getFeeItemCount(); i++) {
					if (IntfConstant.PRE_FEE_STATE_PAYED.getValue().equals(output.getFeeItem()[i].getState())) {
						result += output.getFeeItem()[i].getAmount();
					}
				}
			}
		}
		return result;
	}

	@Override
	public RetVo stateChange(PreSaleOrderDTO preSaleOrderDTO, String statusCd) {
		RetVo retVO = new RetVo();
		retVO.setResult(true);
		StatusChangeNoticeInDTO noticeInDTO = new StatusChangeNoticeInDTO();
		if (preSaleOrderDTO != null) {
			noticeInDTO.setSTATUS(statusCd);
			noticeInDTO.setDESC("状态变更");
			noticeInDTO.setPRE_SALE_ORDER_NO(preSaleOrderDTO.getPreOrderNumber());
			List<String> custSoNumbers = new ArrayList<String>();
			// 2016/1/22 调接口预受理编号关联的正式单
			// 1.根据P单查询关联的订单，订单包含：在用表订单跟历史表订单
			// 2.根据extCustOrderId查询订单在用表记录，并且不在第一步查询的订单里面
			// 以上逻辑会在crm端处理
			QueryCustOrderOutDTO outDTO = queryCustOrder(preSaleOrderDTO.getPreOrderNumber(),
					preSaleOrderDTO.getExtCustOrderId(), "2");
			if (ArrayUtils.isNotEmpty(outDTO.getCustomerOrder())) {
				for (CustomerOrder customerOrder : outDTO.getCustomerOrder()) {
					custSoNumbers.add(customerOrder.getCustSoNumber());
				}
			}
			if (ArrayUtils.isNotEmpty(outDTO.getCustomerOrderHis())) {
				for (CustomerOrder customerOrder : outDTO.getCustomerOrderHis()) {
					custSoNumbers.add(customerOrder.getCustSoNumber());
				}
			}
			noticeInDTO.setSALE_ORDER_NO(custSoNumbers.toArray(new String[0]));
			retVO = casToPadFacade.statusChangeNotice(noticeInDTO, preSaleOrderDTO.getOrderFrom());
		}
		return retVO;
	}

	/**
	 * 费用收费
	 *
	 * @author YULIYAO 2016/1/25 return
	 */
	@Override
	public RetVo pay(PreSaleOrderDTO preSaleOrderDTO, Long staffPositionId, Long orgId, Long areaId) {
		RetVo retVo = new RetVo(true);

		PreSaleOrder preSaleOrder = PreSaleOrder.repository().getById(preSaleOrderDTO.getPreOrderId());

		// 1.设置缴费员工——扩展属性
		// 1.1 查询属性对应的主数据ID
		AttrSpec attrSpec = CrmClassUtils.getAttrSpecByCode(CasConstant.PRE_SALE_ORDER.getValue(),
				CasConstant.JAVACODE_PRE_SALE_PAY_STAFF_POSITION.getValue());
		// 1.2 存在更新，不存在则新增
		PreSaleOrderAttr preSaleOrderAttr = preSaleOrderAttrService.getByOrderAndAttrId(preSaleOrderDTO.getPreOrderId(),
				attrSpec.getAttrId());
		if (preSaleOrderAttr != null) {
			preSaleOrderAttr.setAttrValue(StringUtils.strnull(staffPositionId));
			preSaleOrderAttr.update();
		} else {
			PreSaleOrderAttr preSaleOrderAttrNew = new PreSaleOrderAttr(true);
			preSaleOrderAttrNew.setPreOrderId(preSaleOrderDTO.getPreOrderId());
			preSaleOrderAttrNew.setShardingId(preSaleOrderDTO.getPreOrderId());
			preSaleOrderAttrNew.setAttrId(attrSpec.getAttrId());
			preSaleOrderAttrNew.setAttrValue(StringUtils.strnull(staffPositionId));
			preSaleOrderAttrNew.save();
		}
		// 2.判断是否已经送过账务处理通知，有则直接弹出缴费界面，没有则送通知
		if (CasConstant.PRE_SALE_ORDER_STATUS_CD_WAIT_FOR_PAY.getValue().equals(preSaleOrderDTO.getStatusCd())) {
			AttrSpec announceAttr = CrmClassUtils.getAttrSpecByCode(CasConstant.PRE_SALE_ORDER.getValue(),
					CasConstant.PRE_ANNOUNCE_CHARGE_FLAG.getValue());
			PreSaleOrderAttr AnnounceChargAttr = preSaleOrderAttrService
					.getByOrderAndAttrId(preSaleOrderDTO.getPreOrderId(), announceAttr.getAttrId());
			// 如果没送过账务处理通知，则送通知
			if (AnnounceChargAttr == null || StringUtils.isNullOrEmpty(AnnounceChargAttr.getAttrValue())) {
				PreAnnounceChargeInDTO inDTO = new PreAnnounceChargeInDTO();
				retVo = preSaleOrderService.announceCharge(preSaleOrder, getHbReqType(preSaleOrder));
				if (!retVo.getResult()) {
					return retVo;
				} else {

				}
				/*
				 * crmdToHbFacade.announceCharge(inDTO,) RetVO retVO =
				 * preSaleOrderManager.announceCharge(order,
				 * order.getHbReqType()); //
				 * preSaleOrderManager.saveAcctItemAndOrder(order.
				 * getPreSaleAcctItem(), //
				 * PreSaleConstants.PRE_SALE_ACCT_ITEM_STATUS_PAY, order,
				 * statusCd, "缴费");
				 *
				 * com.ffcs.crm2.intf.hbbean.preAnnounceCharge.OutParam out =
				 * (com.ffcs.crm2.intf.hbbean.preAnnounceCharge.OutParam) retVO
				 * .getObject(); if
				 * (Constants.RET_FALSE.equals(retVO.getResult()) ||
				 * "1".equals(out.getResult())) { msgBox.showInfo((out != null ?
				 * ("错误ID:" + out.getError().getId() + "--" + out
				 * .getError().getMessage()) : retVO.getRetMsg()), "账务处理通知失败",
				 * null); }
				 */
			}
		}
		// callPay(order, this.self, null);

		// 获取本地网标识
		String latnId = null;
		if (!StringUtils.isNullOrEmpty(areaId) && casSysFacade.getAreaCodeByRegionId(areaId) != null) {
			latnId = casSysFacade.getAreaCodeByRegionId(areaId).getAreaNbr();
		}
		// 获取订单流水号
		Long reqType = getHbReqType(preSaleOrder);
		String info = "<info><staffPositionId>" + staffPositionId + "</staffPositionId><teamId>" + orgId
				+ "</teamId><latnId>" + latnId + "</latnId><preCustSoNum>" + preSaleOrderDTO.getPreOrderNumber()
				+ "</preCustSoNum><reqType>" + reqType + "</reqType></info>";
		String url = CrmClassUtils.getOutSysUrlByCode(IntfConstant.HB_PRE_SALE_PAY_URL.getValue());// +
																									// info;
		retVo.setObject(url + info);
		return retVo;
	}

	@Override
	public QueryPreOrderStatusOutDTO queryPreOrderStatus(QueryPreOrderStatusInDTO inDto) {
		String preOrderNumber = inDto.getPreOrderNumber();
		QueryPreOrderStatusOutDTO queryPreOrderStatusOutDTO = new QueryPreOrderStatusOutDTO();
		
		if (StringUtils.isNullOrEmpty(preOrderNumber)) {
			return null;
		}
		Map param = new HashMap();
		param.put("preOrderNumber", preOrderNumber);
		param.put("seq", "1");
		List<PreSaleOrder> pList = preSaleOrderService.queryPreSaleOrderList(param);
		if (pList != null && pList.size() > 0) {
			PreSaleOrder preSaleOrder = pList.get(0);
			com.ffcs.crmd.cas.bean.casbean.query.querypreorderstatus.StatusInfo statusInfo = new com.ffcs.crmd.cas.bean.casbean.query.querypreorderstatus.StatusInfo();
			statusInfo.setStatusCd(preSaleOrder.getStatusCd());
			String statusName = CrmClassUtils.getAttrValueNameByValue("PreSaleOrder", "statusCd",
					preSaleOrder.getStatusCd());
			statusInfo.setStatusCdName(statusName);
			statusInfo.setPreOrderNumber(preSaleOrder.getPreOrderNumber());
			statusInfo.setCustOrderId(preSaleOrder.getExtCustOrderId() + "");
			statusInfo.setCustSoNumber(preSaleOrder.getCustSoNumber());
			// 获取工单池对象
			PreSaleOrderPool preSaleOrderPool = preSaleOrderPoolService
					.getPreSaleOrderPoolByOrderId(preSaleOrder.getPreOrderId());
			if (preSaleOrderPool != null) {
				statusInfo.setPoolStatusCd(preSaleOrderPool.getStatusCd());
				String poolStatusName = CrmClassUtils.getAttrValueNameByValue("PreSaleOrderPool", "statusCd",
						preSaleOrderPool.getStatusCd());
				statusInfo.setPoolStatusCdName(poolStatusName);
				// 收单时间
				statusInfo.setAcceptDate(DateUtils.date2Str(preSaleOrderPool.getAcceptDate()));
				// 退单信息备注
				statusInfo.setReturnRemark(preSaleOrderPool.getRemark());
			}
			Map map = new HashMap();
			map.put("preOrderId", preSaleOrder.getPreOrderId());
			AttrSpec attrSpec = CrmClassUtils.getAttrSpecByCode("PreSaleOrderProc", "backReasonList");
			if (attrSpec != null) {
				Long backAttrSpecId = attrSpec.getId();
				map.put("attrId", backAttrSpecId);
			}
			// 退单原因
			PreSaleOrderProcAttr preSaleOrderProcAttr = preSaleOrderProcAttrService.queryLastBackProcAttr(map);
			if (preSaleOrderProcAttr != null) {
				statusInfo.setStatusReasonCd(preSaleOrderProcAttr.getAttrValue());
				statusInfo.setStatusReasonDesc(preSaleOrderProcAttr.getAttrDesc());
				statusInfo.setReturnDate(DateUtils.date2Str(preSaleOrderProcAttr.getCreateDate()));
				statusInfo.setReturnType(preSaleOrderProcAttr.getAttrValue());
				statusInfo.setReturnReason(preSaleOrderProcAttr.getAttrDesc());
				// 退单人员信息
				Long staffId = preSaleOrderProcAttr.getCreateStaff();
				if (staffId != null && staffId != 0) {
					statusInfo.setReturnStaffId(staffId + "");
					StaffDTO staffDTO = casSysFacade.getStaffById(staffId);
					if (staffDTO != null) {
						statusInfo.setReturnStaffName(staffDTO.getStaffName());
						statusInfo.setReturnStaffCode(staffDTO.getStaffCode());
						statusInfo.setReturnStaffPhone(getPartyContactPhone(staffId+""));
					}

				}

			}
			queryPreOrderStatusOutDTO.setStatusInfo(statusInfo);
		} else {
			// 历史表记录采集
			List<PreSaleOrderHis> preSaleOrderHises = preSaleOrderService.queryPreSaleOrderHisList(param);
			if (preSaleOrderHises != null && preSaleOrderHises.size() > 0) {
				PreSaleOrderHis preSaleOrderHis = preSaleOrderHises.get(0);
				com.ffcs.crmd.cas.bean.casbean.query.querypreorderstatus.StatusInfo statusInfo = new com.ffcs.crmd.cas.bean.casbean.query.querypreorderstatus.StatusInfo();
				statusInfo.setStatusCd(preSaleOrderHis.getStatusCd());
				String statusName = CrmClassUtils.getAttrValueNameByValue("PreSaleOrder", "statusCd",
						preSaleOrderHis.getStatusCd());
				statusInfo.setStatusCdName(statusName);
				statusInfo.setPreOrderNumber(preSaleOrderHis.getPreOrderNumber());
				statusInfo.setCustOrderId(preSaleOrderHis.getExtCustOrderId() + "");
				statusInfo.setCustSoNumber(preSaleOrderHis.getCustSoNumber());

				// 进二表的订单，工单池状态都返回已处理
				statusInfo.setPoolStatusCd("12000");
				statusInfo.setPoolStatusCdName("已处理");

				// 获取工单池对象
				PreSaleOrderPool preSaleOrderPool = preSaleOrderPoolService
						.getPreSaleOrderPoolByOrderId(preSaleOrderHis.getPreOrderId());
				if (preSaleOrderPool != null) {
					// 收单时间
					statusInfo.setAcceptDate(DateUtils.date2Str(preSaleOrderPool.getAcceptDate()));
					// 退单信息备注
					statusInfo.setReturnRemark(preSaleOrderPool.getRemark());
				} else {
					PreSaleOrderPoolHis preSaleOrderPoolHis = preSaleOrderPoolService
							.getPreSaleOrderPoolHisByOrderId(preSaleOrderHis.getPreOrderId());
					if (preSaleOrderPoolHis != null) {
						// 收单时间
						statusInfo.setAcceptDate(DateUtils.date2Str(preSaleOrderPoolHis.getAcceptDate()));
						// 退单信息备注
						statusInfo.setReturnRemark(preSaleOrderPoolHis.getRemark());
					}
				}

				Map map = new HashMap();
				map.put("preOrderId", preSaleOrderHis.getPreOrderId());
				AttrSpec attrSpec = CrmClassUtils.getAttrSpecByCode("PreSaleOrderProc", "backReasonList");
				if (attrSpec != null) {
					Long backAttrSpecId = attrSpec.getId();
					map.put("attrId", backAttrSpecId);
				}
				map.put("his", "true");

				// 退单原因
				PreSaleOrderProcAttr preSaleOrderProcAttr = preSaleOrderProcAttrService.queryLastBackProcAttr(map);
				if (preSaleOrderProcAttr != null) {
					statusInfo.setStatusReasonCd(preSaleOrderProcAttr.getAttrValue());
					statusInfo.setStatusReasonDesc(preSaleOrderProcAttr.getAttrDesc());
					statusInfo.setReturnDate(DateUtils.date2Str(preSaleOrderProcAttr.getCreateDate()));
					statusInfo.setReturnType(preSaleOrderProcAttr.getAttrValue());
					statusInfo.setReturnReason(preSaleOrderProcAttr.getAttrDesc());
					// 退单人员信息
					Long staffId = preSaleOrderProcAttr.getCreateStaff();
					if (staffId != null && staffId != 0) {
						statusInfo.setReturnStaffId(staffId + "");
						StaffDTO staffDTO = casSysFacade.getStaffById(staffId);
						if (staffDTO != null) {
							statusInfo.setReturnStaffName(staffDTO.getStaffName());
							statusInfo.setReturnStaffCode(staffDTO.getStaffCode());
							statusInfo.setReturnStaffPhone(getPartyContactPhone(staffId+""));
						}

					}
				}
				queryPreOrderStatusOutDTO.setStatusInfo(statusInfo);
			} else {
				return null;
			}
		}
		return queryPreOrderStatusOutDTO;
	}

	@Override
	public RetVo sendStateChangeToPad(PreSaleOrderDTO preSaleOrderDTO) {
		RetVo retVo = null;
		String sendStatusCd = "";
		if (IntfConstant.PRE_SALE_ORDER_STATUS_CD_COMPLETE.getValue().equals(preSaleOrderDTO.getStatusCd())) {
			sendStatusCd = IntfConstant.MOBI_PRE_ORDER_STATUS_CD_COMPLETED.getValue();
		} else if (IntfConstant.PRE_SALE_ORDER_STATUS_CD_COMPLETING.getValue().equals(preSaleOrderDTO.getStatusCd())) {
			sendStatusCd = IntfConstant.MOBI_PRE_ORDER_STATUS_CD_COMPLETING.getValue();
		} else if (IntfConstant.PRE_SALE_ORDER_STATUS_CD_ON_WAY.getValue().equals(preSaleOrderDTO.getStatusCd())) {
			sendStatusCd = IntfConstant.MOBI_PRE_ORDER_STATUS_CD_CONVERTED.getValue();
		} else if (IntfConstant.PRE_SALE_ORDER_STATUS_CD_PART_DEAL.getValue().equals(preSaleOrderDTO.getStatusCd())) {
			sendStatusCd = IntfConstant.MOBI_PRE_ORDER_STATUS_CD_CONVERTING.getValue();
		} else if (IntfConstant.PRE_SALE_ORDER_STATUS_CD_CANCEL_COMPLETE.getValue()
				.equals(preSaleOrderDTO.getStatusCd())) {
			sendStatusCd = IntfConstant.MOBI_PRE_ORDER_STATUS_CD_REMOVED.getValue();
		} else if (IntfConstant.PRE_SALE_ORDER_STATUS_CD_WAIT_FOR_PAY.getValue()
				.equals(preSaleOrderDTO.getStatusCd())) {
			sendStatusCd = IntfConstant.MOBI_PRE_ORDER_STATUS_CD_GIVE.getValue();
		} else if (IntfConstant.PRE_SALE_ORDER_STATUS_CD_WAIT_FOR_PAY.getValue()
				.equals(preSaleOrderDTO.getStatusCd())) {
			sendStatusCd = IntfConstant.MOBI_PRE_ORDER_STATUS_CD_PAYED.getValue();
		}
		if (!StringUtils.isNullOrEmpty(sendStatusCd)) {
			if (IntfConstant.CHANNEL_CODE_O2O.getValue().equals(preSaleOrderDTO.getOrderFrom())) {
				if (IntfConstant.MOBI_PRE_ORDER_STATUS_CD_COMPLETED.getValue().equals(sendStatusCd)) {
					retVo = stateChange(preSaleOrderDTO, sendStatusCd);
				}
			} else {
				retVo = stateChange(preSaleOrderDTO, sendStatusCd);
			}

		}
		return retVo;
	}

	/**
	 *
	 * 方法功能: 获取销帐类型.
	 *
	 * @return
	 * @author: lzq @修改记录：
	 *          ==============================================================
	 *          <br>
	 *          日期:2013-5-8 lzq 创建方法，并实现其功能
	 *          ==============================================================
	 *          <br>
	 */
	public Long getHbReqType(PreSaleOrder preSaleOrder) {
		if (preSaleOrder == null) {
			return -1L;
		}
		if (CasConstant.PRE_SALE_ORDER_STATUS_CD_BACK_PAYING.getValue().equals(preSaleOrder.getStatusCd())) {
			return NumberUtils.nullToLongZero(CasConstant.ANNOUNCE_CHARGE_REQ_TYPE_2.getValue());
		}
		PreSaleAcctItem tmpAcctItem = new PreSaleAcctItem();
		tmpAcctItem.setPreOrderId(preSaleOrder.getId());
		tmpAcctItem.setShardingId(preSaleOrder.getId());
		List<PreSaleAcctItem> preSaleAcctItems = preSaleAcctItemService.queryPreSaleAcctItem(tmpAcctItem);
		if (CollectionUtils.isNotEmpty(preSaleAcctItems)) {
			for (PreSaleAcctItem acctItem : preSaleAcctItems) {
				if (CasConstant.PRE_SALE_ACCT_ITEM_STATUS_NO_PAY.getValue().equals(acctItem.getStatusCd())) {
					if (CasConstant.PRE_SALE_ACCT_ITEM_TYPE_NORMAL.getValue().equals(acctItem.getAcctItemType())) {
						return NumberUtils.nullToLongZero(CasConstant.ANNOUNCE_CHARGE_REQ_TYPE_0.getValue());
					} else {
						return NumberUtils.nullToLongZero(CasConstant.ANNOUNCE_CHARGE_REQ_TYPE_3.getValue());
					}
				}
			}
		}
		return -1L;
	}

	@Override
	public RetVo completePreOrder(PreSaleOrderDTO preSaleOrderDTO) {
		RetVo retVO = new RetVo();
		retVO.setResult(true);
		// 1.如果关联的正式订单中，存在一表订单，则限制归档
		QueryCustOrderOutDTO orderOutDTO = queryCustOrder(preSaleOrderDTO.getPreOrderNumber(), null, "0");
		if (orderOutDTO != null && ArrayUtils.isNotEmpty(orderOutDTO.getCustomerOrder())) {
			retVO.setResult(false);
			StringBuffer stringBuffer = new StringBuffer();
			for (CustomerOrder customerOrder : orderOutDTO.getCustomerOrder()) {
				stringBuffer.append("[" + customerOrder.getCustSoNumber() + "]");
			}
			retVO.setMsgTitle("归档失败！当前P订单关联的正式订单中，存在在途的订单！" + stringBuffer.toString());
			return retVO;
		}
		// 2.如果存在剩余预收费用，则限制归档
		PreFeeQueryInDTO input = this.buildPreFeeQueryInParam(preSaleOrderDTO);
		retVO = crmdToHbFacade.preFeeQuery(input);
		Long result = 0L;
		if (retVO.getResult() && retVO.getObject() != null) {
			com.ffcs.crmd.cas.bean.hbbean.prefeequery.OutParam outParam = (com.ffcs.crmd.cas.bean.hbbean.prefeequery.OutParam) retVO
					.getObject();
			if (outParam.getFeeItem() != null && outParam.getFeeItemCount() > 0) {
				for (int i = 0; i < outParam.getFeeItemCount(); i++) {
					if (IntfConstant.PRE_FEE_STATE_PAYED.getValue().equals(outParam.getFeeItem()[i].getState())) {
						result += outParam.getFeeItem()[i].getAmount();
					}
				}
			}
		} else {
			return retVO;
		}
		if (result > 0) {
			retVO.setResult(false);
			retVO.setMsgTitle("当前预受理订单存在剩余预收费用，请关联新的正式订单或者撤销退费。");
			return retVO;
		}

		PreSaleOrder preSaleOrder = new PreSaleOrder();
		CrmBeanUtils.applyIf(preSaleOrder, preSaleOrderDTO);
		preSaleOrder.setIfEnd("1");
		// 调用crm服务判断预受理单是否有竣工的订单
		boolean existCompleteOrder = existCompleteOrder(preSaleOrderDTO.getPreOrderNumber(), null);
		if (existCompleteOrder) {
			// 设置为处理完成
			preSaleOrder.setStatusCd(IntfConstant.PRE_SALE_ORDER_STATUS_CD_COMPLETE.getValue());
			// 状态通知
			preSaleOrderDTO.setStatusCd(IntfConstant.PRE_SALE_ORDER_STATUS_CD_COMPLETE.getValue());
			retVO = sendStateChangeToPad(preSaleOrderDTO);
			if (!retVO.getResult()) {
				return retVO;
			}
			// 删除
			preSaleOrderService.remove(preSaleOrder);
			retVO.setMsgTitle("P订单标记为处理完成！");
		} else {
			// 设置为撤销
			preSaleOrder.setStatusCd(IntfConstant.PRE_SALE_ORDER_STATUS_CD_CANCEL_COMPLETE.getValue());
			// 状态通知
			preSaleOrderDTO.setStatusCd(IntfConstant.PRE_SALE_ORDER_STATUS_CD_CANCEL_COMPLETE.getValue());
			retVO = sendStateChangeToPad(preSaleOrderDTO);
			// 删除
			preSaleOrderService.remove(preSaleOrder);
			retVO.setMsgTitle("P订单标记为已撤销！");
		}
		return retVO;
	}

	private PreFeeQueryInDTO buildPreFeeQueryInParam(PreSaleOrderDTO preSaleOrderDTO) {
		PreFeeQueryInDTO input = new PreFeeQueryInDTO();
		input.setPreCustSoNum(preSaleOrderDTO.getPreOrderNumber());
		input.setTeamId(NumberUtils.nullToZero(preSaleOrderDTO.getOrgId()));

		OrganizationDTO organizationDTO = null;
		if (!StringUtils.isNullOrEmpty(preSaleOrderDTO.getOrgId())) {
			organizationDTO = casSysFacade.getById(preSaleOrderDTO.getOrgId());
		}
		input.setLatnId(organizationDTO != null ? StringUtils.strnull(organizationDTO.getAreaCode()) : null);
		// 通过staff跟订单对象的orgId去获取任职资格
		// 现网逻辑：com.ffcs.crm2.order.dominate.PreSaleOrder.buildPreFeeQueryInParam()
		StaffPositionDTO staffPositionDTO = new StaffPositionDTO();
		staffPositionDTO.setStaffId(preSaleOrderDTO.getStaffId());
		staffPositionDTO.setOrgId(preSaleOrderDTO.getOrgId());
		List<StaffPositionDTO> staffPositionDTOs = casSysFacade.queryStaffPosition(staffPositionDTO);
		if (CollectionUtils.isNotEmpty(staffPositionDTOs)) {
			input.setStaffPositionId(staffPositionDTOs.get(0).getStaffPositionId());
		}
		return input;
	}

	@Override
	public QueryPreSaleOrderIsRelationOutDTO queryPreSaleOrderIsRelation(QueryPreSaleOrderIsRelationInDTO inDTO) {
		PreSaleOrder preSaleOrder = preSaleOrderService.queryFirstPreOrderByOrderNumber(inDTO.getPreOrderNumber());
		QueryPreSaleOrderIsRelationOutDTO outDTO = new QueryPreSaleOrderIsRelationOutDTO();
		outDTO.setResult("0");
		if (preSaleOrder != null) {
			com.ffcs.crmd.cas.bean.casbean.presaleorder.PreSaleOrder pre = new com.ffcs.crmd.cas.bean.casbean.presaleorder.PreSaleOrder();
			//
			if (preSaleOrder.getAcceptTime() != null) {
				pre.setAcceptTime(StringUtils.strnull(preSaleOrder.getAcceptTime()));
			}
			pre.setActionType(preSaleOrder.getActionType());
			if (preSaleOrder.getAreaId() != null) {
				pre.setAreaId(NumberUtils.nullToLongZero(preSaleOrder.getAreaId()));
			}
			if (preSaleOrder.getBookTime() != null) {
				pre.setBookTime(StringUtils.strnull(preSaleOrder.getBookTime()));
			}
			if (preSaleOrder.getChannelD() != null) {
				pre.setChannelId(NumberUtils.nullToLongZero(preSaleOrder.getChannelD()));
			}
			if (preSaleOrder.getCustomerInteractionEventId() != null) {
				pre.setCustomerInteractionEventId(
						NumberUtils.nullToLongZero(preSaleOrder.getCustomerInteractionEventId()));
			}
			if (preSaleOrder.getCreateStaff() != null) {
				pre.setCreateStaff(NumberUtils.nullToLongZero(preSaleOrder.getCreateStaff()));
			}
			if (preSaleOrder.getCustId() != null) {
				pre.setCustId(NumberUtils.nullToLongZero(preSaleOrder.getCustId()));
			}
			if (preSaleOrder.getExtCustOrderId() != null) {
				pre.setExtCustOrderId(NumberUtils.nullToLongZero(preSaleOrder.getExtCustOrderId()));
			}
			pre.setHandlePeopleName(preSaleOrder.getHandlePeopleName());
			pre.setIfEnd(preSaleOrder.getIfEnd());
			if (preSaleOrder.getLanId() != null) {
				pre.setLanId(NumberUtils.nullToLongZero(preSaleOrder.getLanId()));
			}
			if (preSaleOrder.getLockedStaff() != null) {
				pre.setLockedStaff(NumberUtils.nullToLongZero(preSaleOrder.getLockedStaff()));
			}
			pre.setLockedStatus(preSaleOrder.getLockedStatus());
			if (preSaleOrder.getLockedTime() != null) {
				pre.setLockedTime(StringUtils.strnull(preSaleOrder.getLockedTime()));
			}
			pre.setOrderFrom(preSaleOrder.getOrderFrom());
			pre.setOrderType(preSaleOrder.getOrderType());
			if (preSaleOrder.getOrgId() != null) {
				pre.setOrgId(NumberUtils.nullToLongZero(preSaleOrder.getOrgId()));
			}
			pre.setPreHandleFlag(preSaleOrder.getPreHandleFlag());
			if (preSaleOrder.getPreOrderId() != null) {
				pre.setPreOrderId(NumberUtils.nullToLongZero(preSaleOrder.getPreOrderId()));
			}
			pre.setPreOrderNumber(preSaleOrder.getPreOrderNumber());
			pre.setPriority(preSaleOrder.getPriority());
			pre.setProdType(preSaleOrder.getProdType());
			if (preSaleOrder.getRegionCd() != null) {
				pre.setRegionCd(NumberUtils.nullToLongZero(preSaleOrder.getRegionCd()));
			}
			pre.setRemark(preSaleOrder.getRemark());
			pre.setSceneType(preSaleOrder.getSceneType());
			if (preSaleOrder.getSeq() != null) {
				pre.setSeq(NumberUtils.nullToLongZero(preSaleOrder.getSeq()));
			}
			pre.setServiceType(preSaleOrder.getServiceType());
			if (preSaleOrder.getStaffId() != null) {
				pre.setStaffId(NumberUtils.nullToLongZero(preSaleOrder.getStaffId()));
			}
			pre.setStatusCd(preSaleOrder.getStatusCd());
			if (preSaleOrder.getStatusDate() != null) {
				pre.setStatusDate(StringUtils.strnull(preSaleOrder.getStatusDate()));
			}
			if (preSaleOrder.getUpdateStaff() != null) {
				pre.setUpdateStaff(NumberUtils.nullToLongZero(preSaleOrder.getUpdateStaff()));
			}

			outDTO.setPreSaleOrder(pre);
			PreSaleOrderDTO preSaleOrderDTO = new PreSaleOrderDTO();
			CrmBeanUtils.applyIf(preSaleOrderDTO, preSaleOrder);
			PreFeeQueryInDTO preFeeQueryInDTO = buildPreFeeQueryInParam(preSaleOrderDTO);

			RetVo retVO = crmdToHbFacade.preFeeQuery(preFeeQueryInDTO);
			Long result = 0L;
			if (retVO.getResult() && retVO.getObject() != null) {
				com.ffcs.crmd.cas.bean.hbbean.prefeequery.OutParam outParam = (com.ffcs.crmd.cas.bean.hbbean.prefeequery.OutParam) retVO
						.getObject();
				if (outParam.getFeeItem() != null && outParam.getFeeItemCount() > 0) {
					for (int i = 0; i < outParam.getFeeItemCount(); i++) {
						// 金额大于0并且是未收费状态
						// 并且并且费用名称不是预受理回退 - chenjw 20150701 crm00063289
						if (IntfConstant.PRE_FEE_STATE_WAIT_FOR_PAY.getValue()
								.equals(outParam.getFeeItem()[i].getState())
								&& !StringUtils.isNullOrEmpty(outParam.getFeeItem()[i].getAmount())
								&& outParam.getFeeItem()[i].getAmount() > 0L
								&& !"预受理回退".equals(outParam.getFeeItem()[i].getFeeItemName())) {
							result += outParam.getFeeItem()[i].getAmount();
						}
					}
				}
			}
			outDTO.setPreFee(result);
		} else {
			outDTO.setResult("1");
			com.ffcs.crmd.cas.bean.casbean.comm.Error error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			error.setMessage("校验失败！根据预受理订单号【" + inDTO.getPreOrderNumber() + "未找到预受理订单！");
		}
		return outDTO;
	}

	@Override
	public RetVo saveDropPreSaleOrder(String preOrderNumber, String dropDesc, String dropResult) {
		RetVo retVo = new RetVo();
		retVo.setResult(true);
		if (StringUtils.isNullOrEmpty(preOrderNumber)) {
			retVo.setResult(false);
			retVo.setRetCode("1990");
			retVo.setMsgTitle("预受理P订单号不允许为空！");
			return retVo;
		}
		PreSaleOrder preSaleOrder = preSaleOrderService.queryFirstPreOrderByOrderNumber(preOrderNumber);
		if (preSaleOrder == null) {
			retVo.setResult(false);
			retVo.setRetCode("1997");
			retVo.setMsgTitle("根据预受理P订单号：" + preOrderNumber + "找不到对应的预受理订单！或者关联的订单已经全部处理完成。");
			return retVo;
		}

		// 查找对应工单池记录
		PreSaleOrderPool preSaleOrderPool = preSaleOrderPoolService
				.getPreSaleOrderPoolByOrderId(preSaleOrder.getPreOrderId());
		if (preSaleOrderPool == null) {
			retVo.setResult(false);
			retVo.setRetCode("2006");
			retVo.setMsgTitle("根据预受理订单：" + preOrderNumber + "找不到对应的工单记录，或者对应工单已经竣工！");
			return retVo;
		}

		if (!OrderConstant.PRE_POOL_STATUS_CD_GOBACKED.getValue().equals(preSaleOrderPool.getStatusCd())) {
			retVo.setResult(false);
			retVo.setRetCode("orderStatusCdNotCollect");
			retVo.setMsgTitle("预受理订单：" + preOrderNumber + "已重送成功，请不要再次发起重送！");
			return retVo;
		}

		List<PreSaleOrderProcVo> procVos = new ArrayList<PreSaleOrderProcVo>();
		PreSaleOrderProcVo procVo = new PreSaleOrderProcVo();
		procVos.add(procVo);
		Long attrId = CrmClassUtils.getAttrSpecByCode(PreSaleOrderProc.class.getSimpleName(), "reSendDesc") != null
				? CrmClassUtils.getAttrSpecByCode(PreSaleOrderProc.class.getSimpleName(), "reSendDesc").getId() : 0L;
		procVo.setAttrId(attrId);
		procVo.setAttrDesc(dropDesc);

		// 放到最后统一保存
		// preSaleOrderProcService.createPreSaleOrderProc(preSaleOrder, 1010L,
		// procVos);

		// 要保存的预受理P订单属性
		List<PreSaleOrderAttr> attrs = new ArrayList<>();

		AttrSpec attrSpec = CrmClassUtils.getAttrSpecByCode("PreSaleOrder", "retDesc");
		if (attrSpec != null) {
			PreSaleOrderAttr preSaleOrderAttrNew = new PreSaleOrderAttr(true);
			preSaleOrderAttrNew.setPreOrderId(preSaleOrder.getPreOrderId());
			preSaleOrderAttrNew.setShardingId(preSaleOrder.getPreOrderId());
			preSaleOrderAttrNew.setAttrId(attrSpec.getAttrId());
			preSaleOrderAttrNew.setAttrValue(dropDesc);
			// preSaleOrderAttrNew.save();
			attrs.add(preSaleOrderAttrNew);
		}
		AttrSpec attrSpec2 = CrmClassUtils.getAttrSpecByCode("PreSaleOrder", "retResult");
		if (attrSpec2 != null) {
			PreSaleOrderAttr preSaleOrderAttrNew = new PreSaleOrderAttr(true);
			preSaleOrderAttrNew.setPreOrderId(preSaleOrder.getPreOrderId());
			preSaleOrderAttrNew.setShardingId(preSaleOrder.getPreOrderId());
			preSaleOrderAttrNew.setAttrId(attrSpec2.getAttrId());
			preSaleOrderAttrNew.setAttrValue(dropResult);
			// preSaleOrderAttrNew.save();
			attrs.add(preSaleOrderAttrNew);
		}

		// 如果P订单上有待审批标识，则重送时，将工单池保存为待审批状态 - chenjw 20150120 crm00060180
		String statusCd = OrderConstant.PRE_POOL_STATUS_CD_ACCEPTED.getValue();
		AttrSpec attrSpec1 = CrmClassUtils.getAttrSpecByCode("PreSaleOrder", "mcApproveFlag");
		if (attrSpec1 != null) {
			PreSaleOrderAttr preSaleOrderAttr = preSaleOrderAttrService
					.getByOrderAndAttrId(preSaleOrder.getPreOrderId(), attrSpec1.getAttrId());
			if (preSaleOrderAttr != null) {
				statusCd = OrderConstant.PRE_POOL_STATUS_CD_WAIT_FOR_AUDIT.getValue();
			} else {
				preSaleOrderPool.setAcceptDate(DateUtils.dateToTimestamp(DateUtils.getNowDate()));
			}
		}
		preSaleOrderPool.setStatusCd(statusCd);
		// preSaleOrderPoolService.update(preSaleOrderPool);
		StaffDTO staffDTO = new StaffDTO();
		staffDTO.setStaffId(preSaleOrder.getStaffId());
		Staff staff = Staff.repository().getById(NumberUtils.nullToLongZero(preSaleOrder.getStaffId()));
		staffDTO.setStaffName(staff != null ? staff.getStaffName() : "");
		preSaleOrderService.savePreSaleOrderForReSend(preSaleOrder, preSaleOrderPool, attrs, procVos, staffDTO);

		// 发送短信
		SendMessageInDTO sendMessageInDTO = new SendMessageInDTO();
		sendMessageInDTO.setAreaCode("0591");
		sendMessageInDTO.setType(1L);
		//调接口员工获取电话号码
		String[] staffIds = new String[1];
		staffIds[0] = preSaleOrderPool.getStaffId() + "";
		RetVo staffRetVo = casToCrmFacade.qryCustInfo(staffIds,"1");
		if (staffRetVo.getResult()) {
			PartyContactInfo[] partyContactInfos = (PartyContactInfo[]) staffRetVo.getObject();
			if (ArrayUtils.isNotEmpty(partyContactInfos)) {
				sendMessageInDTO.setAccNbr(partyContactInfos[0].getMobilePhone());
				StringBuffer sbf = new StringBuffer();
				sbf.append("您回退的预受理甩单已被重送，请及时处理。预受理单号：");
				sbf.append(StringUtils.strnull(preSaleOrder.getPreOrderNumber()));
				sbf.append("。重送描述：");
				sbf.append(StringUtils.strnull(dropDesc));
				sendMessageInDTO.setMsg(sbf.toString());
				casToSmsFacade.sendMessage(sendMessageInDTO);
			}
		}

		return retVo;
	}

	@Override
	public boolean existCompleteOrder(String preOrderNumber, Long extCustOrderId) {
		boolean result = false;
		QueryCustOrderOutDTO orderOutDTO  = queryCustOrder(preOrderNumber, extCustOrderId, "1");
		if (ArrayUtils.isNotEmpty(orderOutDTO.getCustomerOrderHis())) {
			for (CustomerOrder customerOrder : orderOutDTO.getCustomerOrderHis()) {
				if (OrderConstant.CUSTOMER_ORDER_STATUS_COMPLETE.getValue().equals(customerOrder.getStatusCd())) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	@Override
	public boolean existsPreSaleOrder(String preOrderNumber, Long extCustOrderId) {
		boolean result = false;
		QueryCustOrderOutDTO orderOutDTO = queryCustOrder(preOrderNumber, extCustOrderId, "0");
		if (ArrayUtils.isNotEmpty(orderOutDTO.getCustomerOrder())) {
			for (CustomerOrder customerOrder : orderOutDTO.getCustomerOrder()) {
				if (OrderConstant.CUSTOMER_ORDER_STATUS_PREACCEPT.getValue().equals(customerOrder.getStatusCd())) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * 根据p单获取关联的订单id字符串。
	 *
	 * @param preOrderNumber
	 * @return
	 */
	@Override
	public String getExtCustOrderIds(String preOrderNumber) {
		String extCustOrderIds = "";
		Map map = new HashMap();
		map.put("preOrderNumber", preOrderNumber);
		List<PreSaleOrder> preSaleOrders = preSaleOrderService.queryPreSaleOrderList(map);
		if (preSaleOrders != null && preSaleOrders.size() > 0) {
			for (PreSaleOrder preSaleOrder : preSaleOrders) {
				extCustOrderIds += preSaleOrder.getExtCustOrderId() + ",";
			}
		}
		if (!StringUtils.isNullOrEmpty(extCustOrderIds)) {
			return extCustOrderIds.substring(0, extCustOrderIds.lastIndexOf(","));
		}
		return extCustOrderIds;
	}

	@Override
	public int remove(PreSaleOrderDTO preSaleOrderDTO) {
		PreSaleOrder preSaleOrder = new PreSaleOrder();
		CrmBeanUtils.applyIf(preSaleOrder, preSaleOrderDTO);
		if (!StringUtils.isNullOrEmpty(preSaleOrder.getPreOrderNumber())) {
			// 移除订单映射表
			casToCrmFacade.removeCustOrderMap(preSaleOrder.getPreOrderNumber());
		}
		// 删除相关属性及处理过程等
		return preSaleOrderService.remove(preSaleOrder);
	}

	@Override
	public QueryCustOrderOutDTO queryCustOrder(String preOrderNumber, Long extCustOrderId, String qryType) {
		// 1.根据预受理单号查询预受理订单
		String extCustOrderIds = getExtCustOrderIds(preOrderNumber);
		// 2.根据订单Id去获取订单列表
		QueryCustOrderInDTO in = new QueryCustOrderInDTO();
		in.setQryType(qryType);
		if (extCustOrderId != null && extCustOrderId != 0) {
			if (!StringUtils.isNullOrEmpty(extCustOrderIds)) {
				if (!extCustOrderIds.contains(extCustOrderId + "")) {
					extCustOrderIds += "," + extCustOrderId;
				}
			} else {
				extCustOrderIds = extCustOrderId + "";
			}
		}
		in.setExtCustOrderId(extCustOrderIds);
		RetVo retVo = casToCrmFacade.queryCustomerOrder(in);
		// 如果查询接口失败，则向外抛出异常
		if (!retVo.getResult()) {
			ExceptionUtils.throwEx(retVo.getMsgTitle());
		}
		QueryCustOrderOutDTO orderOutDTO = (QueryCustOrderOutDTO) retVo.getObject();
		return orderOutDTO;
	}

	@Override
	public RetVo intfModPreSaleOrderPool(String preOrderNumber, String receiveStaffId, String receiveOrgId,
			String remark) {
		RetVo retVO = new RetVo();
		retVO.setResult(true);
		/** -- 校验输入 begin -- **/
		if (StringUtils.isNullOrEmpty(preOrderNumber)) {
			retVO.setResult(false);
			retVO.setMsgTitle("预受理单号为空！");
			return retVO;
		}
		Long staffId = NumberUtils.toLong(receiveStaffId);
		Staff staff = Staff.repository().getById(staffId);
		if (staff == null) {
			retVO.setResult(false);
			retVO.setMsgTitle("接收员工为空！");
			return retVO;
		}
		//目前组织的查询还得调用分中心服务
		Long orgId = NumberUtils.nullToLongZero(receiveOrgId);
		OrganizationDTO org = casSysFacade.getById(orgId);
		if (org == null) {
			retVO.setResult(false);
			retVO.setMsgTitle("接收团队为空！");
			return retVO;
		}
		PreSaleOrder preSaleOrder = preSaleOrderService.queryFirstPreOrderByOrderNumber(preOrderNumber);
		if (preSaleOrder == null) {
			retVO.setResult(false);
			retVO.setMsgTitle("预受理订单为空！");
			return retVO;
		}
		PreSaleOrderPool orderPool = preSaleOrderPoolService.getPreSaleOrderPoolByOrderId(preSaleOrder.getPreOrderId());
		if (orderPool == null) {
			retVO.setResult(false);
			retVO.setMsgTitle("工单池记录为空！");
			return retVO;
		}
		// 暂时写死，只能修改泉州地区的工单
		if (orderPool.getAreaId() == null || !orderPool.getAreaId().equals(6L)) {
			retVO.setResult(false);
			retVO.setMsgTitle("只允许修改泉州地区的工单！");
			return retVO;
		}
		if (!OrderConstant.PRE_POOL_STATUS_CD_NOT_ACCEPTED.getValue().equals(orderPool.getStatusCd())
				&& !OrderConstant.PRE_POOL_STATUS_CD_ACCEPTED.getValue().equals(orderPool.getStatusCd())) {
			retVO.setResult(false);
			retVO.setMsgTitle("只允许修改未接收状态或者已接收状态的工单！");
			return retVO;
		}
		if (OrderConstant.PRE_POOL_STATUS_CD_ACCEPTED.getValue().equals(orderPool.getStatusCd())
				&& staffId.equals(orderPool.getStaffId())) {
			retVO.setResult(false);
			retVO.setMsgTitle("当前被派单员工与原接收员工一致！");
			return retVO;
		}
		/** -- 校验输入 end -- **/

		/** -- 变更 begin -- **/
		if (!StringUtils.isNullOrEmpty(remark)) {
			String oldRemark = "";
			AttrSpec attrSpec = CrmClassUtils.getAttrSpecByCode(preSaleOrder.getEntityName(), "intfAssignOrderRemark");
			if (attrSpec != null) {
				PreSaleOrderAttr attr = preSaleOrderAttrService.getByOrderAndAttrId(preSaleOrder.getPreOrderId(),
						attrSpec.getAttrId());
				if (attr != null) {
					oldRemark = attr.getAttrValue();
					oldRemark += "\n----" + DateUtils.convertDateToStr(DateUtils.getNowDate(), "yyyy-MM-dd HH:mm:ss")
							+ "----\n" + remark;
					attr.setAttrValue(oldRemark);
					preSaleOrderAttrService.update(attr);
				}
			}
		}

		orderPool.setStaffId(staffId);
		orderPool.setAcceptDate(DateUtils.dateToTimestamp(DateUtils.getNowDate()));
		orderPool.setIsLeaderAssign("1");
		orderPool.setAcceptOrgId(NumberUtils.toLong(receiveOrgId));
		orderPool.setStatusCd(OrderConstant.PRE_POOL_STATUS_CD_ACCEPTED.getValue());
		preSaleOrderPoolService.update(orderPool);
		return retVO;
	}

	@Override
	public RetVo orderHasFee(String preOrderNumber) {
		RetVo retVo = new RetVo();
		retVo.setResult(false);
		PreSaleOrder preSaleOrder = preSaleOrderService.queryFirstPreOrderByOrderNumber(preOrderNumber);
		if (preSaleOrder != null) {
			PreSaleAcctItem preSaleAcctItem = new PreSaleAcctItem();
			preSaleAcctItem.setPreOrderId(preSaleOrder.getPreOrderId());
			preSaleAcctItem.setShardingId(preSaleOrder.getPreOrderId());
			List<PreSaleAcctItem> acctItems = preSaleAcctItemService.queryPreSaleAcctItem(preSaleAcctItem);
			if (acctItems != null && acctItems.size() > 0) {
				for (PreSaleAcctItem item : acctItems) {
					// 存在已销帐或者未销帐的费用项
					if (IntfConstant.PRE_SALE_ACCT_ITEM_STATUS_PAY.getValue().equals(item.getStatusCd())
							|| IntfConstant.PRE_SALE_ACCT_ITEM_STATUS_NO_PAY.getValue().equals(item.getStatusCd())) {
						retVo.setResult(true);
						retVo.setObject(preSaleOrder.getPreOrderNumber());
						break;
					}
				}
			}
		}
		return retVo;
	}

	/**
	 * 发送短信
	 *
	 * @author YULIYAO 2016/3/1 0：实时发送（任意时间调用接口立刻发送到用户终端） 1：非作息时间发送（8:00-12:30
	 *         return
	 */
	@Override
	public RetVo sentMsg(String phoneNumber, String msgContent, long type) {
		if (StringUtils.isNullOrEmpty(phoneNumber) || StringUtils.isNullOrEmpty(msgContent)) {
			ExceptionUtils.throwEx("手机号码或短信内容不能为空!");
		}
		SendMessageInDTO inParam = new SendMessageInDTO();
		inParam.setAccNbr(phoneNumber);
		inParam.setAreaCode("0591");
		inParam.setType(type);
		inParam.setMsg(msgContent);
		RetVo retVo = casToSmsFacade.sendMessage(inParam);
		if (retVo.getResult()) {
			retVo.setMsgTitle("短信通知发送成功！发送号码：" + phoneNumber);
		}
		return retVo;
	}

	@Override
	public RetVo preCompletePayment(String preOrderNumber, Long reqType) {
		RetVo retVo = new RetVo();
		retVo.setResult(true);

		PreSaleOrder preSaleOrder = null;
		if (StringUtils.isNullOrEmpty(reqType)) {
			retVo.setResult(false);
			retVo.setMsgTitle("入参错误，reqType类型为空");
			return retVo;
		}
		String reqTypeStr = reqType + "";

		if (!StringUtils.isNullOrEmpty(preOrderNumber)) {
			preSaleOrder = preSaleOrderService.queryFirstPreOrderByOrderNumber(preOrderNumber);
		}
		if (preSaleOrder != null) {
			try {
				String acctItemType = "";
				String acctStatus = "";
				if (IntfConstant.ANNOUNCE_CHARGE_REQ_TYPE_0.getValue().equals(reqTypeStr)
						|| IntfConstant.ANNOUNCE_CHARGE_REQ_TYPE_3.getValue().equals(reqTypeStr)) {
					if (IntfConstant.ANNOUNCE_CHARGE_REQ_TYPE_0.getValue().equals(reqTypeStr)) {
						acctItemType = IntfConstant.PRE_SALE_ACCT_ITEM_TYPE_NORMAL.getValue();
					} else if (IntfConstant.ANNOUNCE_CHARGE_REQ_TYPE_3.getValue().equals(reqTypeStr)) {
						acctItemType = IntfConstant.PRE_SALE_ACCT_ITEM_TYPE_RE_FEE.getValue();
					}
					acctStatus = IntfConstant.PRE_SALE_ACCT_ITEM_STATUS_PAY.getValue();
					PreSaleAcctItem temp = new PreSaleAcctItem();
					temp.setPreOrderId(preSaleOrder.getPreOrderId());
					temp.setShardingId(preSaleOrder.getPreOrderId());
					List<PreSaleAcctItem> acctItems = preSaleAcctItemService.queryPreSaleAcctItem(temp);
					// 要保存的预受理费用项
					List<PreSaleAcctItem> itemsForSave = new ArrayList<PreSaleAcctItem>();
					for (PreSaleAcctItem preAcctItem : acctItems) {
						if (acctItemType.equals(preAcctItem.getAcctItemType())
								&& IntfConstant.PRE_SALE_ACCT_ITEM_STATUS_NO_PAY.getValue()
										.equals(preAcctItem.getStatusCd())) {
							// 设置为已缴费状态
							preAcctItem.setStatusCd(acctStatus);
							itemsForSave.add(preAcctItem);
						}
					}

					if (itemsForSave == null || itemsForSave.size() == 0) {
						retVo.setResult(false);
						retVo.setMsgTitle("预受理订单" + preOrderNumber + "不存在需要销帐的账目项！");
					} else {
						// 销帐完成，清除账务处理完成标记
						// 要删除的预受理订单属性
						List<PreSaleOrderAttr> attrsForRemove = new ArrayList<>();
						AttrSpec attrSpec = CrmClassUtils.getAttrSpecByCode(preSaleOrder.getEntityName(),
								OrderConstant.PRE_ANNOUNCE_CHARGE_FLAG.getValue());
						if (attrSpec != null) {
							PreSaleOrderAttr attr = preSaleOrderAttrService
									.getByOrderAndAttrId(preSaleOrder.getPreOrderId(), attrSpec.getAttrId());
							if (attr != null) {
								attrsForRemove.add(attr);
							}
						}
						// 缴费完成后，P单状态统一设置为待受理
						preSaleOrder.setStatusCd(IntfConstant.PRE_SALE_ORDER_STATUS_CD_WAIT_FOR_DEAL.getValue());
						// saveAcctItemAndOrder(itemsForSave, acctStatus,
						// preSaleOrder,
						// IntfConstant.PRE_SALE_ORDER_STATUS_CD_WAIT_FOR_DEAL.getValue(),
						// tip);
						// O2O系统缴费完成不需要通知
						if (!IntfConstant.CHANNEL_CODE_O2O.getValue().equals(preSaleOrder.getOrderFrom())) {
							// 预受理订单销帐完成，通知移动客户端 edit by chenjw 2013-09-12
							// crm00043708
							PreSaleOrderDTO dto = new PreSaleOrderDTO();
							CrmBeanUtils.applyIf(dto, preSaleOrder);
							stateChange(dto, IntfConstant.MOBI_PRE_ORDER_STATUS_CD_PAYED.getValue());
						}

						// 获取集团费用项
						List<ISaleAcctItemInDTO> dtos = new ArrayList<>();
						IIsaleAcctItemRepository isaleAcctItemRepository = ApplicationContextUtil
								.getBean("isaleAcctItemRepository");
						Map<String, Object> param = new HashMap<>();
						param.put("saleSerial", preSaleOrder.getPreOrderNumber());
						List<IsaleAcctItem> isaleAcctItems = IsaleAcctItem.repository().queryISaleAcctItem(param);

						if (isaleAcctItems != null && isaleAcctItems.size() > 0) {
							for (IsaleAcctItem isaleAcctItem : isaleAcctItems) {
								ISaleAcctItemInDTO tmpDto = new ISaleAcctItemInDTO();
								tmpDto.setExtAcctItemId(NumberUtils.nullToLongZero(isaleAcctItem.getExtAcctItemId()));
								tmpDto.setRealAmount(isaleAcctItem.getRealAmount() + "");
								tmpDto.setSaleSerial(isaleAcctItem.getSaleSerial());
								dtos.add(tmpDto);
							}
						}

						RetVo tmpRet = casToCrmFacade.autoRelCustOrder(preSaleOrder.getPreOrderNumber(),
								preSaleOrder.getExtCustOrderId(),  dtos);
						// 当前预受理P订单关联的正式订单是实时订单，并且订单来自移动客户端，并且状态是预受理，则调用计费自动算费
						// 并启动流程 - chenjw 20140518
						// 调用crm接口服务处理
						// 要保存的预受理订单属性
						List<PreSaleOrderAttr> attrsForSave = new ArrayList<>();
						if (tmpRet.getResult()) {
							AttrSpec spec = CrmClassUtils.getAttrSpecByCode(preSaleOrder.getEntityName(),
									"realAcceptHasPayFlag");
							if (spec != null) {
								PreSaleOrderAttr attr = new PreSaleOrderAttr(true);
								attr.setPreOrderId(preSaleOrder.getId());
								attr.setShardingId(preSaleOrder.getPreOrderId());
								attr.setAttrId(spec.getAttrId());
								attr.setAttrValue("1");
								attrsForSave.add(attr);
							}
						}
						StaffDTO staffDTO = new StaffDTO();

						Long staffId = getPreSalePayStaffId(preSaleOrder.getPreOrderId());
						if (staffId == null) {
							staffId = NumberUtils
									.nullToLongZero(CasSessionContext.getContext().getStaffId());
						}
						staffDTO.setStaffId(staffId);
						staffDTO.setStaffName(casSysFacade.getStaffNameById(staffId));
						preSaleOrderService.savePreForCompletePayment(preSaleOrder, itemsForSave, attrsForSave,
								attrsForRemove, staffDTO);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				retVo.setResult(false);
				retVo.setMsgTitle("预受理订单" + preOrderNumber + "帐目项更新失败:" + e.getMessage());
			}

		} else {
			retVo.setResult(false);
			retVo.setMsgTitle("预受理订单" + preOrderNumber + "不存在！");
		}
		return retVo;
	}

	public void saveAcctItemAndOrder(List<PreSaleAcctItem> acctItems, String acctItemStatusCd, PreSaleOrder order,
			String orderStatusCd, String remark) {
		for (PreSaleAcctItem acctItem : acctItems) {
			acctItem.setStatusCd(acctItemStatusCd);
			preSaleAcctItemService.update(acctItem);
		}
		// if (!StringUtils.isNullOrEmpty(order.getStatusCd()) &&
		// !orderStatusCd.equals(order.getStatusCd())) {
		// PreSaleOrderProc preSaleOrderProc = new PreSaleOrderProc(true);
		// preSaleOrderProc.setPreOrderId(order.getPreOrderId());
		// // 如果缴费前有保存缴费员工，则设置 - chenjw 20140606 crm00055635
		// Long payStaffId = this.getPreSalePayStaffIdByOrder(order);
		// if (payStaffId != null) {
		// preSaleOrderProc.setStaffId(payStaffId);
		// } else {
		// preSaleOrderProc.setStaffId(CasSessionContext.getStaffId());
		// }
		// preSaleOrderProc.setAcceptTime(DateUtils.dateToTimestamp(DateUtils.getNowDate()));
		// preSaleOrderProc.setStatusCd(order.getStatusCd());
		// preSaleOrderProc.setRemark(remark);
		// preSaleOrderProcService.save(preSaleOrderProc);
		// }
		// 创建并保存预受理处理过程
		StaffDTO staffDTO = new StaffDTO();
		staffDTO.setStaffId(CasSessionContext.getContext().getStaffId());
		Staff staff = Staff.repository().getById(NumberUtils.nullToLongZero(CasSessionContext.getContext().getStaffId()));
		staffDTO.setStaffName(staff != null ? staff.getStaffName() : "");
		preSaleOrderProcService.createPreSaleOrderProc(order,
				NumberUtils.toLong(IntfConstant.PRE_PROC_EVENT_ID_PAYED.getValue()), null, staffDTO);
	}

	/**
	 * 转变更单
	 *
	 * @author YULIYAO 2016/3/1 return
	 */
	@Override
	public RetVo convertToModOrder(CustomerOrderDTO customerOrderDTO) {
		if (StringUtils.isNullOrEmpty(customerOrderDTO.getCustomerOrderId())) {
			ExceptionUtils.throwEx("订单ID为空，转变更单失败!");
		}
		RetVo retVo = casToCrmFacade
				.convertToModOrder(NumberUtils.nullToLongZero(customerOrderDTO.getCustomerOrderId()));
		return retVo;
	}

	@Override
	public RetVo validateInParam(Long orgId, Long staffPositionId, String preOrderNumber) {
		RetVo retVO = new RetVo();
		retVO.setResult(true);
		// 团队
		Organization org = Organization.repository().getById(NumberUtils.nullToLongZero(orgId));
		if (org == null) {
			retVO.setResult(false);
			retVO.setMsgTitle("根据团队标识[" + orgId + "]找不到对应的团队");
			return retVO;
		}
		// 岗位
		StaffPosition staffPosition = StaffPosition.repository().getById(NumberUtils.nullToLongZero(staffPositionId));
		if (staffPosition == null) {
			retVO.setResult(false);
			retVO.setMsgTitle("根据岗位标识[" + staffPositionId + "]找不到对应的岗位！");
			return retVO;
		}
		// 员工
		Staff staff = Staff.repository().getById(staffPosition.getStaffId());
		if (staff == null) {
			retVO.setResult(false);
			retVO.setMsgTitle("根据岗位标识[" + staffPositionId + "]找不到对应的员工！");
			return retVO;
		}
		// P订单号
		PreSaleOrder preSaleOrder = preSaleOrderService.queryFirstPreOrderByOrderNumber(preOrderNumber);
		if (preSaleOrder == null) {
			retVO.setResult(false);
			retVO.setMsgTitle("根据P订单号[" + preOrderNumber + "]找不到对应的预受理订单，或者关联的订单已经全部处理完成。");
			return retVO;
		}

		// 判断是否可以撤销
		retVO = checkCouldCancel(orgId, preSaleOrder);
		if (!retVO.getResult()) {
			return retVO;
		}

		retVO.setObject(preSaleOrder);
		return retVO;
	}

	@Override
	public RetVo buildPreOrder(Long lantId, Long orgId, Long staffPositionId, String preOrderNumber) {
		RetVo resultVo = new RetVo();
		resultVo.setResult(true);
		// 构造输入
		PreFeeQueryInDTO inDTO = new PreFeeQueryInDTO();
		inDTO.setLatnId(StringUtils.strnull(lantId));
		inDTO.setPreCustSoNum(preOrderNumber);
		inDTO.setStaffPositionId(staffPositionId);
		inDTO.setTeamId(orgId);
		com.ffcs.crmd.cas.bean.casbean.cancel.cancelorderquery.PreOrder vPreOrder = new com.ffcs.crmd.cas.bean.casbean.cancel.cancelorderquery.PreOrder();
		vPreOrder.setPreCustSoNum(preOrderNumber);
		resultVo.setObject(vPreOrder);
		// 调用计费查询
		RetVo retVO = crmdToHbFacade.preFeeQuery(inDTO);
		if (retVO.getResult() && retVO.getObject() != null) {
			com.ffcs.crmd.cas.bean.hbbean.prefeequery.OutParam output = (com.ffcs.crmd.cas.bean.hbbean.prefeequery.OutParam) retVO
					.getObject();
			if (output.getFeeItem() != null && output.getFeeItemCount() > 0) {
				for (int i = 0; i < output.getFeeItemCount(); i++) {
					com.ffcs.crmd.cas.bean.casbean.cancel.cancelorderquery.FeeItem vFeeItem = new com.ffcs.crmd.cas.bean.casbean.cancel.cancelorderquery.FeeItem();
					vFeeItem.setAmount(output.getFeeItem()[i].getAmount());
					vFeeItem.setFeeItemClass(output.getFeeItem()[i].getFeeItemClass());
					vFeeItem.setFeeItemName(output.getFeeItem()[i].getFeeItemName());
					vFeeItem.setPreFeeId(output.getFeeItem()[i].getPreFeeId());
					vFeeItem.setState(output.getFeeItem()[i].getState());
					vFeeItem.setChargeMethod(output.getFeeItem()[i].getChargeMethod());
					vPreOrder.addFeeItem(vFeeItem);
				}
			}
		} else {
			resultVo.setResult(false);
			resultVo.setMsgTitle("调用计费查询预受理费用失败！[" + retVO.getMsgTitle() + "]");
		}
		return resultVo;
	}

	/**
	 * 判断是否可以撤销.
	 *
	 * @param orgId
	 * @return
	 */
	protected RetVo checkCouldCancel(Long orgId, PreSaleOrder preSaleOrder) {
		RetVo retVO = new RetVo();
		retVO.setResult(true);
		// 处理完成、撤销竣工的P订单不允许撤销
		if (IntfConstant.PRE_SALE_ORDER_STATUS_CD_COMPLETE.getValue().equals(preSaleOrder.getStatusCd())
				|| IntfConstant.PRE_SALE_ORDER_STATUS_CD_CANCEL_COMPLETE.getValue().equals(preSaleOrder.getStatusCd())) {
			retVO.setResult(false);
			retVO.setMsgTitle("处理完成、撤销竣工的P订单不允许撤销！");
			return retVO;
		}

		// 计费江水莲要求 -- 如果有向计费发过预受理帐务处理通知，撤销P订单，需要限制撤销团队与预受理团队一致
		// 对应CRM处理为：如果存在已销帐的费用项，则限制撤销团队
		PreSaleAcctItem qry = new PreSaleAcctItem();
		qry.setPreOrderId(preSaleOrder.getPreOrderId());
		qry.setShardingId(preSaleOrder.getPreOrderId());
		List<PreSaleAcctItem> acctItems = preSaleAcctItemService.queryPreSaleAcctItem(qry);
		if (acctItems != null && acctItems.size() > 0) {
			for (PreSaleAcctItem preSaleAcctItem : acctItems) {
				if (IntfConstant.PRE_SALE_ACCT_ITEM_STATUS_PAY.getValue().equals(preSaleAcctItem.getStatusCd())) {
					Organization curOrg = Organization.repository().getById(NumberUtils.nullToLongZero(orgId));
					if (curOrg == null) {
						retVO.setResult(false);
						retVO.setMsgTitle("当前团队为空！");
						return retVO;
					}
					if (!curOrg.getId().equals(preSaleOrder.getOrgId())) {
						retVO.setResult(false);
						Organization org = Organization.repository().getById(preSaleOrder.getOrgId());
						retVO.setMsgTitle(
								"当前团队[" + curOrg.getOrgName() + "]与预受理团队[" + org.getOrgName() + "]不一致，撤销后有可能导致费用异常！");
						return retVO;
					}

				}
			}
		}
		if (CrmClassUtils.hasSwitch("switch_crm00063695")) {
			// FJCRMV2.0_BUG_翼支付付款方式的甩单不允许做撤销 - chenjw 20150722 crm00063695
			if (checkHasYZFPreFee(preSaleOrder)) {
				retVO.setResult(false);
				retVO.setMsgTitle("当前预受理订单有预收费用，并且缴费方式是翼支付支付，暂时不支持撤销。");
				return retVO;
			}
		}
		return retVO;

	}

	/**
	 * 判断P单下的预收费用中，是否存在翼支付缴费的费用项.
	 *
	 * @return
	 * @author chenjw 2015-7-22
	 */
	public boolean checkHasYZFPreFee(PreSaleOrder preSaleOrder) {
		boolean result = false;
		PreSaleOrderDTO dto = new PreSaleOrderDTO();
		CrmBeanUtils.applyIf(dto, preSaleOrder);
		// 构造输入
		PreFeeQueryInDTO inDTO = buildPreFeeQueryInParam(dto);
		// 调用计费查询
		RetVo retVO = crmdToHbFacade.preFeeQuery(inDTO);
		if (retVO.getResult() && retVO.getObject() != null) {
			com.ffcs.crmd.cas.bean.hbbean.prefeequery.OutParam output = (com.ffcs.crmd.cas.bean.hbbean.prefeequery.OutParam) retVO
					.getObject();
			if (output.getFeeItem() != null && output.getFeeItemCount() > 0) {
				for (int i = 0; i < output.getFeeItemCount(); i++) {
					// 金额大于0并且是已缴费状态，并且支付方式是翼支付支付
					if (IntfConstant.PRE_FEE_STATE_PAYED.getValue().equals(output.getFeeItem()[i].getState())
							&& !StringUtils.isNullOrEmpty(output.getFeeItem()[i].getAmount())
							&& output.getFeeItem()[i].getAmount() > 0L && IntfConstant.PRE_FEE_CHARGE_METHOD_YZF
									.getValue().equals(output.getFeeItem()[i].getChargeMethod())) {
						return true;
					}
				}
			}
		}
		return result;
	}

	@Override
	public RetVo doPreFeeCancel(Long orgId, String latnId, Long staffPositionId, PreOrder vPreOrder,
			List<com.ffcs.crmd.cas.bean.casbean.cancel.cancelordercommit.CustomerOrder> customerOrders,
			PreSaleOrderDTO preSaleOrderDTO) {
		RetVo retVO = new RetVo();
		retVO.setResult(true);
		// 构建输入
		com.ffcs.crmd.cas.bean.hbbean.prefeecancel.InParam input = new com.ffcs.crmd.cas.bean.hbbean.prefeecancel.InParam();
		input.setTeamId(orgId);
		input.setLatnId(latnId);
		input.setStaffPositionId(staffPositionId);

		// 预受理费用
		com.ffcs.crmd.cas.bean.hbbean.prefeecancel.PreFee vPreFee = new com.ffcs.crmd.cas.bean.hbbean.prefeecancel.PreFee();
		vPreFee.setPreCustSoNum(vPreOrder.getPreCustSoNum());
		vPreFee.setAmount(vPreOrder.getAmount());
		input.setPreFee(vPreFee);

		// 客户订单费用
		if (customerOrders != null && customerOrders.size() > 0) {
			for (com.ffcs.crmd.cas.bean.casbean.cancel.cancelordercommit.CustomerOrder vCustomerOrder : customerOrders) {
				// 有费用才送计费
				if (vCustomerOrder.getAcctItem() != null && vCustomerOrder.getAcctItem().length > 0) {
					// 构建费用项
					com.ffcs.crmd.cas.bean.hbbean.prefeecancel.OrderFee vOrderFee = new com.ffcs.crmd.cas.bean.hbbean.prefeecancel.OrderFee();
					vOrderFee.setCustSoNumber(vCustomerOrder.getCustSoNumber());
					for (int i = 0; i < vCustomerOrder.getAcctItem().length; i++) {
						com.ffcs.crmd.cas.bean.hbbean.prefeecancel.AcctItem vDestAcctItem = new com.ffcs.crmd.cas.bean.hbbean.prefeecancel.AcctItem();
						CrmBeanUtils.applyIf(vCustomerOrder.getAcctItem(i), vDestAcctItem);
						vOrderFee.addAcctItem(vDestAcctItem);
					}
					input.addOrderFee(vOrderFee);
				}
			}
		}
		// 有预受理费用或者有正式订单费用才送计费
		if ((!StringUtils.isNullOrEmpty(vPreOrder.getAmount()) && vPreOrder.getAmount() > 0)
				|| (input.getOrderFee() != null && input.getOrderFee().length > 0)) {
			// 调用预受理费用退费接口前，在这个地方再调计费查询下剩余的预受理费用 - chenjw 20150123 crm00060285
			PreSaleOrderVo vo = new PreSaleOrderVo();
			CrmBeanUtils.applyIf(vo, preSaleOrderDTO);
			Long remainPreFee = getRemainPreFeeFromHb(vo.getPreOrderNumber(), vo.getOrgId(), vo.getStaffPositionId());
			// 如果剩余预收费用与传入费用不一致，则报错
			if (remainPreFee != null && !remainPreFee.equals(vPreOrder.getAmount())) {
				retVO.setResult(false);
				retVO.setMsgTitle("订单的剩余预收费用与要退的预收费用金额不一致，请重新查询。");
				return retVO;
			}
			// 发起撤销
			retVO = crmdToHbFacade.preFeeCancel(input);
		}
		return retVO;
	}

	@Override
	public RetVo saveOrderCancel(PreSaleOrderDTO preSaleOrderDTO,
			Long staffPositionId, Long amount, String retCode,
			String cancelReason) {
		RetVo retVo = new RetVo();
		retVo.setResult(true);
		// 接口发起撤销，如果撤销成功，则统一设置为最后一张单
		PreSaleOrder preSaleOrder = new PreSaleOrder();
		preSaleOrderDTO.setIfEnd("1");
		// 计费返回成功
		if (StringUtils.isNullOrEmpty(amount) && !"0".equals(StringUtils.strnull(amount))) {
			// 保存退费员工
			if (staffPositionId != null) {
				AttrSpec attrSpec = CrmClassUtils.getAttrSpecByCode(preSaleOrder.getEntityName(), "preSalePayStaff");
				if (attrSpec != null) {
					PreSaleOrderAttr preSaleOrderAttr = new PreSaleOrderAttr(true);
					preSaleOrderAttr.setPreOrderId(preSaleOrderDTO.getPreOrderId());
					preSaleOrderAttr.setShardingId(preSaleOrderDTO.getPreOrderId());
					preSaleOrderAttr.setAttrId(attrSpec.getAttrId());
					preSaleOrderAttr.setAttrValue(StringUtils.strnull(staffPositionId));
					preSaleOrderAttrService.save(preSaleOrderAttr);
				}
			}
		}
		// 预受理费用项设置为已退费
		PreSaleAcctItem preSaleAcctItem = new PreSaleAcctItem();
		preSaleAcctItem.setPreOrderId(preSaleOrderDTO.getPreOrderId());
		preSaleAcctItem.setShardingId(preSaleOrderDTO.getPreOrderId());
		List<PreSaleAcctItem> preAcctItems = preSaleAcctItemService.queryPreSaleAcctItem(preSaleAcctItem);
		if (preAcctItems != null && preAcctItems.size() > 0) {
			for (PreSaleAcctItem acctItem : preAcctItems) {
				acctItem.setStatusCd(IntfConstant.PRE_SALE_ACCT_ITEM_STATUS_REPAY.getValue());
				preSaleAcctItemService.update(acctItem);
			}
		}
		// 撤销正式订单返回的标识，1表示可以撤销P订单
		CrmBeanUtils.applyIf(preSaleOrder, preSaleOrderDTO);
		if ("1".equals(StringUtils.strnull(retCode))) {
			// 通知移动客户端已撤销
			preSaleOrderDTO.setStatusCd(IntfConstant.PRE_SALE_ORDER_STATUS_CD_CANCEL_COMPLETE.getValue());
			sendStateChangeToPad(preSaleOrderDTO);
			preSaleOrder.setStatusCd(IntfConstant.PRE_SALE_ORDER_STATUS_CD_CANCEL_COMPLETE.getValue());
			preSaleOrderService.remove(preSaleOrder);
			// 往预受理过程历史表新增记录 -- zhangyangyi crm00063974 2015-08-14
			StaffPosition staffPosition = StaffPosition.repository()
					.getById(NumberUtils.nullToLongZero(staffPositionId));
			if (staffPosition != null) {
				Staff staff = Staff.repository().getById(staffPosition.getStaffId());
				StaffDTO staffDTO = new StaffDTO();
				CrmBeanUtils.applyIf(staffDTO, staff);
				saveProcHis(preSaleOrderDTO, 1006L, staffDTO, cancelReason);
			}
		} else {
			preSaleOrderService.update(preSaleOrder);
		}
		return retVo;
	}

	@Override
	public void saveProcHis(PreSaleOrderDTO preSaleOrderDTO, Long eventId, StaffDTO staff, String cancelReason) {
		PreSaleOrderProc preSaleOrderProc = new PreSaleOrderProc(true);
		PreSaleOrderProcHis preSaleOrderProcHis = new PreSaleOrderProcHis();
		Long hisId = CrmEntityUtils.getSeq("SEQ_PRE_SALE_ORDER_PROC_HIS_ID");
		preSaleOrderProcHis.setHisId(hisId);
		preSaleOrderProcHis.setPreSaleOrderProcId(preSaleOrderProc.getPreSaleOrderProcId());
		String remark = CrmClassUtils.getAttrValueNameByValue(preSaleOrderProc.getEntityName(),
				"customerInteractionEventId", eventId + "");
		preSaleOrderProcHis.setRemark(remark);
		preSaleOrderProcHis.setPreOrderId(preSaleOrderDTO.getPreOrderId());
		preSaleOrderProcHis.setShardingId(preSaleOrderDTO.getPreOrderId());
		preSaleOrderProcHis.setCustomerInteractionEventId(eventId);
		preSaleOrderProcHis.setStaffId(staff.getStaffId());
		preSaleOrderProcHis.setHandlePeopleName(staff.getStaffName());
		preSaleOrderProcHis.setAcceptTime(DateUtils.dateToTimestamp(DateUtils.getNowDate()));
		preSaleOrderProcHis.setStatusCd("1000");
		preSaleOrderProcHis.setStatusDate(DateUtils.dateToTimestamp(DateUtils.getNowDate()));
		preSaleOrderProcHis.setAreaId(CasSessionContext.getContext().getAreaId());
		preSaleOrderProcHis.setRegionCd(CasSessionContext.getContext().getRegionCd());
		preSaleOrderProcHis.setCreateDate(DateUtils.dateToTimestamp(DateUtils.getNowDate()));
		preSaleOrderProcHis.setCreateStaff(staff.getStaffId());
		preSaleOrderProcHis.setUpdateDate(DateUtils.dateToTimestamp(DateUtils.getNowDate()));
		preSaleOrderProcHisService.save(preSaleOrderProcHis);
		// 撤销原因
		if (!StringUtils.isNullOrEmpty(cancelReason)) {
			PreSaleOrderProcAttr attr = new PreSaleOrderProcAttr(true);
			PreSaleOrderProcAttrHis attrHis = new PreSaleOrderProcAttrHis();
			attrHis.setHisId(CrmEntityUtils.getSeq("SEQ_PRE_ORDER_PROC_ATTR_HIS_ID"));
			attrHis.setPreOrderProcAttrId(attr.getPreOrderProcAttrId());
			attrHis.setPreSaleOrderProcId(preSaleOrderProcHis.getPreSaleOrderProcId());
			attrHis.setShardingId(preSaleOrderDTO.getPreOrderId());
			attrHis.setAttrId(0L);
			attrHis.setAttrDesc(cancelReason);
			attrHis.setStatusCd("1000");
			attrHis.setCreateStaff(staff.getStaffId());
			attrHis.setUpdateStaff(staff.getStaffId());
			attrHis.setStatusDate(DateUtils.dateToTimestamp(DateUtils.getNowDate()));
			attrHis.setCreateDate(DateUtils.dateToTimestamp(DateUtils.getNowDate()));
			attrHis.setUpdateDate(DateUtils.dateToTimestamp(DateUtils.getNowDate()));
			IPreSaleOrderProcAttrHisService attrHisService = ApplicationContextUtil.getBean("preSaleOrderProcAttrHisService");
			attrHisService.save(attrHis);
		}
	}

	@Override
	public RetVo queryPreSaleOrder(Map map) {
		RetVo retVo = new RetVo();
		retVo.setResult(false);
		String qryType = StringUtils.strnull(map.remove("qryType"));
		String preOrderNumber = StringUtils.strnull(map.remove("preOrderNumber"));
		String qryHis = StringUtils.strnull(map.remove("qryHis"));
		String extCustOrderId = StringUtils.strnull(map.remove("extCustOrderId"));
		if (!StringUtils.isNullOrEmpty(preOrderNumber)) {
			PreSaleOrderDTO preSaleOrderDTO = null;
			PreSaleOrder preSaleOrder = preSaleOrderService.queryFirstPreOrderByOrderNumber(preOrderNumber);
			if (preSaleOrder == null){
				retVo.setResult(true);
				if(!"1".equals(qryHis)) {
					PreSaleOrderHis preSaleOrderHis = preSaleOrderService
							.queryFirstPreSaleOrderHisByOrderNumber(preOrderNumber);
					if (preSaleOrderHis != null) {
						preSaleOrderDTO = new PreSaleOrderDTO();
						CrmBeanUtils.applyIf(preSaleOrderDTO, preSaleOrderHis);
						retVo.setResult(true);
					}
				}
			} else {
				preSaleOrderDTO = new PreSaleOrderDTO();
				CrmBeanUtils.applyIf(preSaleOrderDTO, preSaleOrder);
				// 发展员工
				PreSaleOrderAttr attr = preSaleOrderAttrService.getByOrderAndAttrId(preSaleOrder.getPreOrderId(), 950020903L);
				if (attr != null) {
					preSaleOrderDTO.setDevStaff(attr.getAttrValue() + "");
				}
				// 发展团队
				attr = preSaleOrderAttrService.getByOrderAndAttrId(preSaleOrder.getPreOrderId(), 950020904L);
				if (attr != null) {
					preSaleOrderDTO.setDevTeam(attr.getAttrValue() + "");
				}
				retVo.setResult(true);
			}
			if (preSaleOrderDTO != null) {
				retVo.setObject(preSaleOrderDTO);
			}
		} else if (!StringUtils.isNullOrEmpty(extCustOrderId)) {
			retVo.setResult(true);
			map.put("extCustOrderId", extCustOrderId);
			List<PreSaleOrder> list = preSaleOrderService.queryPreSaleOrderList(map);
			if (list != null && list.size() > 0) {
				PreSaleOrderDTO preSaleOrderDTO = new PreSaleOrderDTO();
				CrmBeanUtils.applyIf(preSaleOrderDTO, list.get(0));
				if ("1".equals(qryType) && !"1"
						.equals(StringUtils.strnull(preSaleOrderDTO.getSeq()))) {
					PreSaleOrder preSaleOrder = preSaleOrderService
							.queryFirstPreOrderByOrderNumber(preSaleOrderDTO.getPreOrderNumber());
					CrmBeanUtils.applyIf(preSaleOrderDTO, preSaleOrder);
				}
				// 发展员工
				PreSaleOrderAttr attr = preSaleOrderAttrService.getByOrderAndAttrId(preSaleOrderDTO.getPreOrderId(), 950020903L);
				if (attr != null) {
					preSaleOrderDTO.setDevStaff(attr.getAttrValue() + "");
				}
				// 发展团队
				attr = preSaleOrderAttrService.getByOrderAndAttrId(preSaleOrderDTO.getPreOrderId(), 950020904L);
				if (attr != null) {
					preSaleOrderDTO.setDevTeam(attr.getAttrValue() + "");
				}
				retVo.setObject(preSaleOrderDTO);
			} else if (!"1".equals(qryHis)) {
				List<PreSaleOrderHis> hisList = preSaleOrderService.queryPreSaleOrderHisList(map);
				if (hisList != null && hisList.size() > 0) {
					PreSaleOrderDTO dto = new PreSaleOrderDTO();
					CrmBeanUtils.applyIf(dto, hisList.get(0));
					if ("1".equals(qryType) && !"1".equals(StringUtils.strnull(dto.getSeq()))) {
						PreSaleOrderHis hisOrder = preSaleOrderService
								.queryFirstPreSaleOrderHisByOrderNumber(dto.getPreOrderNumber());
						CrmBeanUtils.applyIf(dto, hisOrder);
					}
					retVo.setObject(dto);
				}
			}
		} else {
			retVo.setMsgTitle("无有效查询参数!");
		}
		return retVo;
	}

	@Override
	public RetVo preSaleOrderDeal(boolean isNeedPay, Long extCustOrderId, PreSaleOrderDealInDTO inDTO) {
		RetVo retVo = new RetVo();
		retVo.setResult(true);
		String ifEnd = inDTO.getIfEnd();
		Long preOrderId = inDTO.getPreOrderId();
		PreSaleOrder preSaleOrder = PreSaleOrder.repository().getById(preOrderId);
		if (!StringUtils.isNullOrEmpty(ifEnd)) {
			preSaleOrder.setIfEnd("1");
			String isOnWay = inDTO.getIsOnWay();
			if ("1".equals(isOnWay)) {
				PreSaleOrderPool preSaleOrderPool = preSaleOrderPoolService
						.getPreSaleOrderPoolByOrderId(preSaleOrder.getPreOrderId());
				if (preSaleOrderPool != null) {
					preSaleOrderPool.setStatusCd(IntfConstant.PRE_POOL_STATUS_CD_DEALED.getValue());
					preSaleOrderPoolService.update(preSaleOrderPool);
				}
				preSaleOrder.setStatusCd(IntfConstant.PRE_SALE_ORDER_STATUS_CD_ON_WAY.getValue());
				StaffDTO staffDTO = new StaffDTO();
				staffDTO.setStaffId(CasSessionContext.getContext().getStaffId());
				Staff staff = Staff.repository().getById(NumberUtils.nullToLongZero(CasSessionContext.getContext().getStaffId()));
				staffDTO.setStaffName(staff != null ? staff.getStaffName() : "");
				preSaleOrderProcService.createPreSaleOrderProc(preSaleOrder, 1011L, null, staffDTO);
			}
		} else {
			preSaleOrder.setStatusCd(IntfConstant.PRE_SALE_ORDER_STATUS_CD_PART_DEAL.getValue());
			preSaleOrderService.update(preSaleOrder);
		}
		if (isNeedPay) {
			PreSaleOrderDTO preSaleOrderDTO = new PreSaleOrderDTO();
			CrmBeanUtils.applyIf(preSaleOrderDTO, preSaleOrder);
			sendStateChangeToPad(preSaleOrderDTO);
		}
		if (extCustOrderId != 0) {
			// Todo 预留，新生成的p单
		} else {
			// todo 宁夏的暂时不做
			// reDealSendMsg(preSaleOrder);
		}
		return retVo;
	}

	@Override
	public RetVo savePreSaleOrder(final PreSaleOrderInfo info) {
		RetVo retVo = new RetVo();
		retVo.setResult(true);
		// 保存前先判断数据库中是否已经存在
		PreSaleOrderDTO oldPreSaleOrder = this.getFirstPreSaleOrder(info.getPreSaleOrder().getPreOrderNumber());
		if (oldPreSaleOrder != null) {
			retVo.setResult(false);
			retVo.setMsgTitle("P订单生成失败，P订单：" + info.getPreSaleOrder().getPreOrderNumber() + "已存在，不能再新增");
			return retVo;
		}
		try {
			preSaleOrderService.savePreSaleOrder(info);
		} catch (Exception e) {
			retVo.setResult(false);
			retVo.setMsgTitle(e.getMessage());
			e.printStackTrace();
		}
//		if (!preAnnounceChargFlag && preSaleOrder.getPreSaleAcctItems() != null
//				&& preSaleOrder.getPreSaleAcctItems().size() > 0) {
//			preSaleOrderService.announceCharge(preSaleOrder, getHbReqType(preSaleOrder));
//		}
//		preSaleOrderService.savePreSaleOrder(preSaleOrder);
		return retVo;
	}



	@Override
	public RetVo savePreSaleOrderPool(PreSaleOrderPoolInDTO inDTO) {
		RetVo retVo = new RetVo();
		retVo.setResult(true);
		PreSaleOrderPool preSaleOrderPool = new PreSaleOrderPool(true);
		RetVo retVoTmp = convertBeanToPreSaleOrderPool(inDTO, preSaleOrderPool);
		if (retVoTmp.getResult()) {
			preSaleOrderPoolService.save(preSaleOrderPool);
		}
		return retVo;
	}

	@Override
	public RetVo saveIntfPreOrder(List<IntfPreOrderInDTO> inDTO) {
		RetVo retVo = new RetVo();
		retVo.setResult(true);
		if (inDTO != null && inDTO.size() > 0) {
			for (IntfPreOrderInDTO dto : inDTO) {
				IntfPreOrder intfPreOrder = new IntfPreOrder(true);
				// intfPreOrder.setIntfPreOrderId(dto.getIntfPreOrderId());
				PreSaleOrder preSaleOrder = preSaleOrderService
						.queryFirstPreOrderByOrderNumber(dto.getPreOrderNumber());
				if (preSaleOrder == null) {
					retVo.setResult(false);
					retVo.setMsgTitle("根据P订单号：" + dto.getPreOrderNumber() + "找不到对应的P订单");
					return retVo;
				}
				intfPreOrder.setShardingId(preSaleOrder.getPreOrderId());
				intfPreOrder.setPreOrderNumber(dto.getPreOrderNumber());
				intfPreOrder.setChannelNbr(dto.getChannelNbr());
				intfPreOrder.setTextType(dto.getTextType());
				intfPreOrder.setStatusCd(dto.getStatusCd());
				intfPreOrder.setErrMsg(dto.getErrMsg());
				intfPreOrder.setRemark(dto.getRemark());
				intfPreOrderService.save(intfPreOrder);
				if (!StringUtils.isNullOrEmpty(dto.getTextContent())) {
					IntfPreOrderAttach attach = new IntfPreOrderAttach(true);
					attach.setIntfPreOrderId(intfPreOrder.getIntfPreOrderId());
					attach.setShardingId(preSaleOrder.getPreOrderId());
					attach.setTextContent(dto.getTextContent());
					attach.setStatusCd(dto.getStatusCd());
					intfPreOrderAttachService.save(attach);
				}
			}
		}
		return retVo;
	}

	@Override
	public int saveIntfPreOrder(IntfPreOrderDTO intfPreOrderDTO) {
		if (intfPreOrderDTO == null) {
			return 0;
		}
		// 判断新增还是修改
		IntfPreOrder intfPreOrder;
		if (intfPreOrderDTO.getIntfPreOrderId() == null || intfPreOrderDTO.getIntfPreOrderId() == 0) {
			intfPreOrder = new IntfPreOrder(true);
		} else {
			intfPreOrder = new IntfPreOrder();
		}
		CrmBeanUtils.applyIf(intfPreOrder, intfPreOrderDTO, false);
		if (intfPreOrderDTO.getIntfPreOrderId() == null || intfPreOrderDTO.getIntfPreOrderId() == 0) {
			return intfPreOrder.save();
		} else {
			return intfPreOrder.update();

		}
	}

	@Override
	public List<OrderOverTimeStatDTO> queryOrderOverTimeStat(int procCount, int model) {
		List<OrderOverTimeStatDTO> orderOverTimeStatDTOs = new ArrayList<>();
		List<OrderOverTimeStatVo> statVos = preSaleOrderService.queryOrderOverTimeStat(procCount, model);
		if (CollectionUtils.isNotEmpty(statVos)) {
			orderOverTimeStatDTOs = CrmBeanUtils.copyList(statVos, OrderOverTimeStatDTO.class);
		}
		return orderOverTimeStatDTOs;
	}

	/**
	 * 查询轮询泉州工单池自动发送
	 *
	 * @param procCount
	 * @param model
	 * @param perSize
	 * @return
	 */
	@Override
	public List<IntfPreOrderDTO> queryIntfOrderWaitSent(int procCount, int model, int perSize) {
		List<IntfPreOrderDTO> intfPreOrderDTOs = new ArrayList<>();
		List<IntfPreOrder> intfPreOrders = preSaleOrderService.queryIntfOrderWaitSent(procCount, model, perSize);
		if (CollectionUtils.isNotEmpty(intfPreOrders)) {
			intfPreOrderDTOs = CrmBeanUtils.copyList(intfPreOrders, IntfPreOrderDTO.class);
		}
		return intfPreOrderDTOs;
	}

	/**
	 * 查询同编码比当前对象ID小的数据，返回最小的id
	 *
	 * @author YULIYAO 2016/3/11 return
	 */
	@Override
	public Long getRelaIntfPreOrderId(IntfPreOrderDTO intfPreOrderDTO) {
		if (intfPreOrderDTO != null && !StringUtils.isNullOrEmpty(intfPreOrderDTO.getIntfPreOrderId())
				&& !StringUtils.isNullOrEmpty(intfPreOrderDTO.getPreOrderNumber())) {
			Map param = new HashMap();
			// 状态
			List statusList = new ArrayList();
			statusList.add("70A");
			statusList.add("70E");
			statusList.add("70F");
			param.put("statusCdList", statusList);
			// id
			param.put("intfPreOrderIdEnd", intfPreOrderDTO.getIntfPreOrderId());
			// 编码
			param.put("preOrderNumber", intfPreOrderDTO.getPreOrderNumber());
			// 条数
			param.put("perSize", 1);
			List<IntfPreOrderDTO> intfPreOrderDTOs = preSaleOrderService.queryIntfPreOrder(param);
			if (CollectionUtils.isNotEmpty(intfPreOrderDTOs)) {
				return intfPreOrderDTOs.get(0).getIntfPreOrderId();
			}
		}
		return null;
	}

	/**
	 * 获取报文
	 *
	 * @author YULIYAO 2016/3/12 return
	 */
	@Override
	public String getIntfPreOrderText(Long intfPreOrderId) {
		IntfPreOrderAttach intfPreOrderAttach = intfPreOrderAttachService.get(intfPreOrderId);
		return intfPreOrderAttach == null ? null : intfPreOrderAttach.getTextContent();
	}

	/**
	 * 根据编码查询第一张P单
	 *
	 * @author YULIYAO 2016/3/15 return
	 */
	@Override
	public PreSaleOrderDTO getFirstPreSaleOrder(String preOrderNumber) {
		if (StringUtils.isNullOrEmpty(preOrderNumber)) {
			return null;
		}
		PreSaleOrderDTO preSaleOrderDTO = new PreSaleOrderDTO();
		PreSaleOrder preSaleOrder = preSaleOrderService.queryFirstPreOrderByOrderNumber(preOrderNumber);
		if (preSaleOrder == null) {
			return null;
		}
		CrmBeanUtils.applyIf(preSaleOrderDTO, preSaleOrder);
		return preSaleOrderDTO;
	}

	/**
	 * 从预受理单历史表获取第一张单
	 *
	 * @author YULIYAO 2016/3/24 return
	 */
	@Override
	public PreSaleOrderHisDTO getFirstPreSaleOrderHis(String preOrderNumber) {
		if (StringUtils.isNullOrEmpty(preOrderNumber)) {
			return null;
		}
		PreSaleOrderHisDTO preSaleOrderHisDTO = new PreSaleOrderHisDTO();
		Map param = new HashMap();
		param.put("preOrderNumber", preOrderNumber);
		List<PreSaleOrderHis> preSaleOrderHises = preSaleOrderService.queryPreSaleOrderHisList(param);
		if (CollectionUtils.isNotEmpty(preSaleOrderHises)) {
			CrmBeanUtils.applyIf(preSaleOrderHisDTO, preSaleOrderHises.get(0));
			return preSaleOrderHisDTO;
		} else {
			return null;
		}
	}

	@Override
	public int save(PreSaleOrderDTO preSaleOrderDTO) {
		if (preSaleOrderDTO != null) {
			if (preSaleOrderDTO.getPreOrderId() == null || preSaleOrderDTO.getPreOrderId() == 0) {
				// 新增
				PreSaleOrder preSaleOrder = new PreSaleOrder(true);
				CrmBeanUtils.applyIf(preSaleOrder, preSaleOrderDTO, false);
				return preSaleOrder.save();
			} else {
				// 修改
				PreSaleOrder preSaleOrder = new PreSaleOrder();
				CrmBeanUtils.applyIf(preSaleOrder, preSaleOrderDTO);
				return preSaleOrder.update();
			}
		}
		return 0;
	}

	@Override
	public RetVo queryPreOrderFlow(String preOrderNumber) {
		RetVo retVo = new RetVo();
		retVo.setResult(true);
		if (StringUtils.isNullOrEmpty(preOrderNumber)) {
			retVo.setRetCode("11");
			retVo.setResult(false);
			retVo.setMsgTitle("预受理订单号为空。");
			return retVo;
		}
		PreSaleOrder preSaleOrder = preSaleOrderService.queryFirstPreOrderByOrderNumber(preOrderNumber);
		if (preSaleOrder == null) {
			retVo.setRetCode("12");
			retVo.setResult(false);
			retVo.setMsgTitle("预受理订单为空或者已结单。");
			return retVo;
		}
		PreSaleOrderProc tmp = new PreSaleOrderProc();
		tmp.setPreOrderId(preSaleOrder.getPreOrderId());
		tmp.setShardingId(preSaleOrder.getShardingId());
		List<PreSaleOrderProc> procs = preSaleOrderProcService.queryPreSaleOrderProc(tmp);
		if (procs != null && procs.size() > 0) {
			List<QueryPreOrderFlowOutDTO> outs = new ArrayList<QueryPreOrderFlowOutDTO>();
			for (PreSaleOrderProc proc : procs) {
				QueryPreOrderFlowOutDTO out = new QueryPreOrderFlowOutDTO();
				// 处理员工名称
				String staffName = proc.getHandlePeopleName();
				if (StringUtils.isNullOrEmpty(staffName) && !StringUtils.isNullOrEmpty(proc.getStaffId())) {
					Staff staff = Staff.repository().getById(proc.getStaffId());
					if (staff != null) {
						staffName = staff.getStaffName();
					}
				}
				if (StringUtils.isNullOrEmpty(staffName)) {
					staffName = "接口专用工号";
				}
				out.setStaffName(staffName);
				// 时间
				out.setOperateDate(DateUtils.date2Str(proc.getCreateDate()));
				// 动作
				out.setActionName("");
				Long eventId = proc.getCustomerInteractionEventId();
				if (eventId != null && eventId != 0) {
					String attrValue = CrmClassUtils.getAttrValueNameByValue("PreSaleOrderProc",
							"customerInteractionEventId", StringUtils.strnull(eventId));
					out.setActionName(attrValue);
				}

				// 描述
				// 先获取处理过程属性
				Map param = new HashMap();
				param.put("preSaleOrderProcId", proc.getPreSaleOrderProcId());
				param.put("shardingId", proc.getShardingId());
				List<PreSaleOrderProcAttr> procAttrs = preSaleOrderProcAttrService.queryPreSaleOrderProcAttr(param);
				if (procAttrs != null && procAttrs.size() > 0) {
					// 暂时取第一个属性
					out.setDesc(procAttrs.get(0).getAttrDesc());
				}
				outs.add(out);
			}
			retVo.setObject(outs);
		}
		return retVo;
	}

	@Override
	public RetVo queryPreOrderInfo(String preOrderNumber) {
		RetVo retVo = new RetVo();
		retVo.setResult(true);
		if (StringUtils.isNullOrEmpty(preOrderNumber)) {
			retVo.setRetCode("11");
			retVo.setResult(false);
			retVo.setMsgTitle("预受理订单号为空。");
			return retVo;
		}
		PreSaleOrder preSaleOrder = preSaleOrderService.queryFirstPreOrderByOrderNumber(preOrderNumber);
		QueryPreOrderInfoOutDTO outDTO = new QueryPreOrderInfoOutDTO();
		if (preSaleOrder != null) {
			PreSaleOrderPool preSaleOrderPool = preSaleOrderPoolService
					.getPreSaleOrderPoolByOrderId(preSaleOrder.getPreOrderId());
			if (preSaleOrderPool != null) {
				if (preSaleOrderPool.getStaffId() != null) {
					Staff staff = Staff.repository().getById(preSaleOrderPool.getStaffId());
					if (staff != null) {
						outDTO.setStaffName(staff.getStaffName());
						outDTO.setContactPhone(getPartyContactPhone(staff.getStaffId()+""));
					}
				}
				if (preSaleOrderPool.getAcceptOrgId() != null) {
					Organization org = Organization.repository().getById(preSaleOrderPool.getAcceptOrgId());
					outDTO.setOrganizationName(org.getOrgName() != null ? org.getOrgName() : null);
				}
				retVo.setObject(outDTO);
			}
		} else {
			PreSaleOrderHis preSaleOrderHis = preSaleOrderService
					.queryFirstPreSaleOrderHisByOrderNumber(preOrderNumber);
			if (preSaleOrderHis != null) {
				PreSaleOrderPoolHis preSaleOrderPoolHis = preSaleOrderPoolService
						.getPreSaleOrderPoolHisByOrderId(preSaleOrderHis.getPreOrderId());
				if (preSaleOrderPoolHis != null) {
					if (preSaleOrderPoolHis.getStaffId() != null) {
						Staff staff = Staff.repository().getById(preSaleOrderPoolHis.getStaffId());
						if (staff != null) {
							outDTO.setStaffName(staff.getStaffName());
							outDTO.setContactPhone(getPartyContactPhone(staff.getStaffId()+""));
						}
					}
					if (preSaleOrderPoolHis.getAcceptOrgId() != null) {
						Organization org = Organization.repository().getById(preSaleOrderPoolHis.getAcceptOrgId());
						outDTO.setOrganizationName(org.getOrgName() != null ? org.getOrgName() : null);
					}
				}
			} else {
				retVo.setRetCode("12");
				retVo.setResult(false);
				retVo.setMsgTitle("预受理订单为空。");
			}
		}
		return retVo;
	}

	@Override
	public RetVo queryPreOrderForOrder(Long extCustOrderId) {
		RetVo retVo = new RetVo();
		retVo.setResult(false);
		PreSaleOrderInfoOutDTO outDTO = new PreSaleOrderInfoOutDTO();
		AbsCrmdBaseEntity entity = preSaleOrderService.getPreSaleOrderHisByExtOrderId(extCustOrderId);
		com.ffcs.crmd.cas.bean.casbean.presaleorder.PreSaleOrder vPreSaleOrder = new com.ffcs.crmd.cas.bean.casbean.presaleorder.PreSaleOrder();
		String preFee = "";
		String assistInfo = "";
		AttrSpec attrSpec = CrmClassUtils.getAttrSpecByCode(PreSaleOrder.class.getSimpleName(), "assistSalePersonInfo");
		if (entity != null) {
			if (entity instanceof PreSaleOrder) {
				PreSaleOrder preSaleOrder = (PreSaleOrder) entity;
				convertSaleOrderToBean(preSaleOrder, vPreSaleOrder);
				preFee = getPreFee(preSaleOrder);
				PreSaleOrderProc proc = new PreSaleOrderProc();
				proc.setShardingId(preSaleOrder.getShardingId());
				proc.setPreOrderId(preSaleOrder.getPreOrderId());
				List<PreSaleOrderProc> procs = preSaleOrderProcService.queryPreSaleOrderProc(proc);
				if (procs != null && procs.size() > 0) {
					for (PreSaleOrderProc p : procs) {
						com.ffcs.crmd.cas.bean.casbean.query.querypreorderfororder.PreSaleOrderProc pp = new com.ffcs.crmd.cas.bean.casbean.query.querypreorderfororder.PreSaleOrderProc();
						pp.setAttrDesc(vPreSaleOrder.getRemark());
						pp.setStaffId(vPreSaleOrder.getStaffId());
						pp.setCreateDate(StringUtils.strnull(p.getCreateDate()));
						String name = CrmClassUtils.getAttrValueNameByValue("PreSaleOrderProc",
								"customerInteractionEventId", StringUtils.strnull(p.getCustomerInteractionEventId()));
						if (!StringUtils.isNullOrEmpty(name)) {
							pp.setCustomerInteractionEventIdName(name);
						}
						Map param = new HashMap();
						param.put("preSaleOrderProcId", p.getPreSaleOrderProcId());
						param.put("shardingId", p.getShardingId());
						List<PreSaleOrderProcAttr> procAttrs = preSaleOrderProcAttrService
								.queryPreSaleOrderProcAttr(param);
						if (procAttrs != null && procAttrs.size() > 0) {
							pp.setAttrDesc(procAttrs.get(0).getAttrDesc());
						}
						outDTO.addPreSaleOrderProc(pp);
					}
				}

				if (attrSpec != null) {
					PreSaleOrderAttr attr = preSaleOrderAttrService.getByOrderAndAttrId(preSaleOrder.getPreOrderId(),
							attrSpec.getAttrId());
					if (attr != null) {
						assistInfo = attr.getAttrValue();
					}
				}
			} else {
				PreSaleOrderHis preSaleOrderHis = (PreSaleOrderHis) entity;
				convertSaleOrderHisToBean(preSaleOrderHis, vPreSaleOrder);
				preFee = getPreFee(preSaleOrderHis);
				PreSaleOrderProc proc = new PreSaleOrderProc();
				proc.setShardingId(preSaleOrderHis.getPreOrderId());
				proc.setPreOrderId(preSaleOrderHis.getPreOrderId());
				List<PreSaleOrderProcHis> procs = preSaleOrderProcService.queryPreSaleOrderProcHis(proc);
				if (procs != null && procs.size() > 0) {
					for (PreSaleOrderProcHis p : procs) {
						com.ffcs.crmd.cas.bean.casbean.query.querypreorderfororder.PreSaleOrderProc pp = new com.ffcs.crmd.cas.bean.casbean.query.querypreorderfororder.PreSaleOrderProc();
						pp.setAttrDesc(vPreSaleOrder.getRemark());
						pp.setStaffId(vPreSaleOrder.getStaffId());
						pp.setCreateDate(StringUtils.strnull(p.getCreateDate()));
						String name = CrmClassUtils.getAttrValueNameByValue("PreSaleOrderProc",
								"customerInteractionEventId", StringUtils.strnull(p.getCustomerInteractionEventId()));
						if (!StringUtils.isNullOrEmpty(name)) {
							pp.setCustomerInteractionEventIdName(name);
						}
						outDTO.addPreSaleOrderProc(pp);
					}
				}
				if (attrSpec != null) {
					PreSaleOrderAttr attr = preSaleOrderAttrService.getByOrderAndAttrId(preSaleOrderHis.getPreOrderId(),
							attrSpec.getAttrId());
					if (attr != null) {
						assistInfo = attr.getAttrValue();
					}
				}
			}
			outDTO.setPreSaleOrder(vPreSaleOrder);
			outDTO.setPreFee(preFee);
			outDTO.setAssistInfo(assistInfo);
			retVo.setResult(true);
			retVo.setObject(outDTO);
		}
		return retVo;
	}

	/**
	 * 获取预收费用（已销帐的费用）.
	 *
	 * @param entity
	 * @return
	 */
	private String getPreFee(AbsCrmdBaseEntity entity) {
		if (entity == null) {
			return "";
		}
		Long result = 0L;
		// 构建输入
		PreFeeQueryInDTO inDTO = null;
		PreSaleOrderDTO preSaleOrderDTO = new PreSaleOrderDTO();
		if (entity instanceof PreSaleOrder) {
			PreSaleOrder preSaleOrder = (PreSaleOrder) entity;
			CrmBeanUtils.applyIf(preSaleOrderDTO, preSaleOrder);
			inDTO = buildPreFeeQueryInParam(preSaleOrderDTO);
		} else if (entity instanceof PreSaleOrderHis) {
			PreSaleOrderHis preSaleOrderHis = (PreSaleOrderHis) entity;
			CrmBeanUtils.applyIf(preSaleOrderDTO, preSaleOrderHis);
			inDTO = buildPreFeeQueryInParam(preSaleOrderDTO);
		}
		if (inDTO != null) {
			// 调用计费查询
			RetVo retVo = crmdToHbFacade.preFeeQuery(inDTO);
			if (retVo.getResult() && retVo.getObject() != null) {
				com.ffcs.crmd.cas.bean.hbbean.prefeequery.OutParam output = (com.ffcs.crmd.cas.bean.hbbean.prefeequery.OutParam) retVo
						.getObject();
				if (output.getFeeItem() != null && output.getFeeItemCount() > 0) {
					for (int i = 0; i < output.getFeeItemCount(); i++) {
						// 金额大于0并且是未收费状态
						// 并且并且费用名称不是预受理回退 - chenjw 20150701 crm00063289
						if (IntfConstant.PRE_FEE_STATE_PAYED.getValue().equals(output.getFeeItem()[i].getState())
								&& !StringUtils.isNullOrEmpty(output.getFeeItem()[i].getAmount())
								&& output.getFeeItem()[i].getAmount() > 0
								&& !"预受理回退".equals(output.getFeeItem()[i].getFeeItemName())) {
							result += output.getFeeItem()[i].getAmount();
						}
					}
				}
			}
		}
		return StringUtils.strnull(result);
	}

	/**
	 * 保存处理过程及属性
	 *
	 * @author YULIYAO 2016/3/18 return
	 */
	@Override
	public void createPreProc(Long preSaleOrderId, Long shardingId, Long staffId, List<AttrValueDTO> attrValueDTOs, Long eventId,
			String attrDesc) {
		if (preSaleOrderId == null || staffId == 0 || eventId == null) {
			return;
		}
		// 备注设置为交互事件名称 - chenjw 20150525 crm00062196
		String remark = CrmClassUtils.getAttrValueNameByValue(PreSaleOrderProc.class.getSimpleName(),
				"customerInteractionEventId", eventId + "");
		// 1.保存处理过程
		PreSaleOrderProc proc = new PreSaleOrderProc(true);
		proc.setPreOrderId(preSaleOrderId);
		proc.setShardingId(shardingId);
		proc.setStatusCd(CasConstant.STATUS_CD_VAILID.getValue());
		proc.setCustomerInteractionEventId(eventId);
		proc.setStaffId(staffId);
		proc.setHandlePeopleName(casSysFacade.getStaffNameById(staffId));
		proc.setLanId(CasSessionContext.getContext().getAreaId());
		proc.setRemark(remark);
		proc.setAcceptTime(new Timestamp(new Date().getTime()));
		proc.save();
		if (CollectionUtils.isNotEmpty(attrValueDTOs)) {
			for (AttrValueDTO attrValueDTO : attrValueDTOs) {
				// 2.保存处理过程属性
				PreSaleOrderProcAttr procAttr = new PreSaleOrderProcAttr(true);
				procAttr.setPreSaleOrderProcId(proc.getId());
				procAttr.setShardingId(shardingId);
				procAttr.setAttrId(attrValueDTO.getAttrId());
				procAttr.setAttrValueId(attrValueDTO.getAttrValueId());
				procAttr.setAttrValue(attrValueDTO.getAttrValue());
				procAttr.setAttrDesc(attrDesc);
				procAttr.save();
			}
		}
	}

	/**
	 * 保存处理过程及属性历史表
	 *
	 * @author YULIYAO 2016/3/18 return
	 */
	@Override
	public void createPreProcHis(Long preSaleOrderId, Long shardingId, Long staffId, List<AttrValueDTO> attrValueDTOs, Long eventId,
			String attrDesc) {
		if (preSaleOrderId == null || staffId == 0 || eventId == null) {
			return;
		}
		// 备注设置为交互事件名称 - chenjw 20150525 crm00062196
		String remark = CrmClassUtils.getAttrValueNameByValue(PreSaleOrderProc.class.getSimpleName(),
				"customerInteractionEventId", eventId + "");
		// 1.保存处理过程
		PreSaleOrderProcHis procHis = new PreSaleOrderProcHis();
		Long hisId = CrmEntityUtils.getSeq("SEQ_PRE_SALE_ORDER_PROC_HIS_ID");
		procHis.setHisId(hisId);
		PreSaleOrderProc proc = new PreSaleOrderProc(true);
		procHis.setPreSaleOrderProcId(proc.getId());
		procHis.setPreOrderId(preSaleOrderId);
		procHis.setShardingId(shardingId);
		procHis.setStatusCd(CasConstant.STATUS_CD_VAILID.getValue());
		procHis.setCustomerInteractionEventId(eventId);
		procHis.setStaffId(staffId);
		procHis.setHandlePeopleName(casSysFacade.getStaffNameById(staffId));
		procHis.setLanId(CasSessionContext.getContext().getAreaId());
		procHis.setRemark(remark);
		procHis.setAcceptTime(new Timestamp(new Date().getTime()));
		procHis.save();
		if (CollectionUtils.isNotEmpty(attrValueDTOs)) {
			for (AttrValueDTO attrValueDTO : attrValueDTOs) {
				// 2.保存处理过程属性
				PreSaleOrderProcAttrHis procAttrHis = new PreSaleOrderProcAttrHis();
				procAttrHis.setHisId(CrmEntityUtils.getSeq("SEQ_PRE_ORDER_PROC_ATTR_HIS_ID"));
				procAttrHis.setPreOrderProcAttrId(CrmEntityUtils.getSeq("SEQ_PRE_ORDER_PROC_ATTR_ID"));
				procAttrHis.setPreSaleOrderProcId(procHis.getPreSaleOrderProcId());
				procAttrHis.setShardingId(shardingId);
				procAttrHis.setAttrId(attrValueDTO.getAttrId());
				procAttrHis.setAttrValueId(attrValueDTO.getAttrValueId());
				procAttrHis.setAttrValue(attrValueDTO.getAttrValue());
				procAttrHis.setAttrDesc(attrDesc);
				procAttrHis.save();
			}
		}
	}

	/**
	 * 根据正式单ID查询预受理订单，取第一条
	 *
	 * @author YULIYAO 2016/3/22 return
	 */
	@Override
	public PreSaleOrderDTO getByExtCustOrderId(String customerOrderId) {
		PreSaleOrderDTO preSaleOrderDTO = new PreSaleOrderDTO();
		Map param = new HashMap();
		param.put("extCustOrderId", customerOrderId);
		List<PreSaleOrder> preSaleOrders = preSaleOrderService.queryPreSaleOrderList(param);
		if (CollectionUtils.isNotEmpty(preSaleOrders)) {
			CrmBeanUtils.applyIf(preSaleOrderDTO, preSaleOrders.get(0));
			return preSaleOrderDTO;
		} else {
			return null;
		}
	}

	/**
	 * 根据正式单编号查询预受理订单历史表，取第一条
	 *
	 * @author YULIYAO 2016/3/22 return
	 */
	@Override
	public PreSaleOrderHisDTO getHisByCustOrderNumber(String custOrderNumber) {
		PreSaleOrderHisDTO preSaleOrderHisDTO = new PreSaleOrderHisDTO();
		Map param = new HashMap();
		param.put("custSoNumber", custOrderNumber);
		List<PreSaleOrderHis> preSaleOrderHises = preSaleOrderHisService.queryPreSaleOrderHis(param);
		if (CollectionUtils.isNotEmpty(preSaleOrderHises)) {
			CrmBeanUtils.applyIf(preSaleOrderHisDTO, preSaleOrderHises.get(0));
		} else {
			return null;
		}
		return preSaleOrderHisDTO;
	}

	/**
	 * 删除预受理订单历史数据
	 *
	 * @author YULIYAO 2016/3/24 return
	 */
	@Override
	public int deletePreOrderHis(PreSaleOrderHisDTO hisDTO) {
		int result = 0;
		if (hisDTO != null && hisDTO.getHisId() != null) {
			PreSaleOrderHis preSaleOrderHis = new PreSaleOrderHis();
			CrmBeanUtils.applyIf(preSaleOrderHis, hisDTO);
			return preSaleOrderHis.remove();
		}
		return result;
	}

	/**
	 * 查询已竣工的预受理单，供轮洵调用
	 *
	 * @author YULIYAO 2016/4/1 return
	 */
	@Override
	public List<PreSaleOrderDTO> queryCompleteOrderAuto(int model, int procCount, int diffDate) {
		Map map = new HashMap();
		if (procCount > 0) {
			map.put("model", model);
			map.put("procCount", procCount);
		}
		if (diffDate > 0) {
			map.put("diffDate", diffDate);
		}
		List<PreSaleOrder> preSaleOrderList = preSaleOrderService.queryCompleteOrderAuto(map);
		if (CollectionUtils.isNotEmpty(preSaleOrderList)) {
			return CrmBeanUtils.copyList(preSaleOrderList, PreSaleOrderDTO.class);
		}
		return null;
	}

	/**
	 * 根据销售员工/销售团队查询回退单列表
	 *
	 * @author YULIYAO 2016/4/6 return
	 */
	@Override
	public PageInfo queryReturnList(Long saleOrgId, Long saleStaffId, int currentPage, int perPageNum) {
		Map param = new HashMap();
		if (saleOrgId == 0 && saleStaffId == 0) {
			ExceptionUtils.throwEx("销售员工ID或销售团队ID需要至少一个非空");
		}
		if (saleOrgId != 0) {
			param.put("saleOrgId", saleOrgId);
		}
		if (saleStaffId != 0) {
			param.put("saleStaffId", saleStaffId);
		}
		PageInfo pageInfo = preSaleOrderService.queryReturnList(param, currentPage, perPageNum);
		if (pageInfo != null && CollectionUtils.isNotEmpty(pageInfo.getList())) {
			pageInfo.setList(CrmBeanUtils.copyList(pageInfo.getList(), PreSaleOrderDTO.class));
		}
		return pageInfo;
	}



	/**
	 * 工单池对象转换。
	 *
	 * @param inDTO
	 * @param pool
	 */
	private RetVo convertBeanToPreSaleOrderPool(PreSaleOrderPoolInDTO inDTO, PreSaleOrderPool pool) {
		RetVo retVo = new RetVo();
		retVo.setResult(true);
		// pool.setPreSaleOrderPoolId(inDTO.getPreSaleOrderPoolId());
		// pool.setPreOrderId(inDTO.getPreOrderId());
		String preOrderNumber = inDTO.getPreOrderNumber();
		PreSaleOrderDTO preSaleOrderDTO = this.getFirstPreSaleOrder(preOrderNumber);
		if (preSaleOrderDTO == null) {
			throw new RtManagerException("根据P单号：" + preOrderNumber + "找不到对应的P订单");
		}
		// 判断数据库中是否已经存在对应的工单池记录
		PreSaleOrderPool oldPool = preSaleOrderPoolService
				.getPreSaleOrderPoolByOrderId(preSaleOrderDTO.getPreOrderId());
		if (oldPool != null) {
			retVo.setResult(false);
			retVo.setMsgTitle("P单号：" + preOrderNumber + "对应的工单池记录已存在，不能再新增");
			return retVo;
		}
		pool.setPreOrderId(preSaleOrderDTO.getPreOrderId());
		pool.setCustOrderId(inDTO.getCustOrderId());
		pool.setStaffId(inDTO.getStaffId());
		pool.setRemark(inDTO.getRemark());
		pool.setVersion(inDTO.getVersion());
		pool.setIsLeaderAssign(inDTO.getIsLeaderAssign());
		pool.setOrgId(inDTO.getOrgId());
		pool.setAcceptOrgId(inDTO.getAcceptOrgId());
		pool.setSceneType(inDTO.getSceneType());
		pool.setServiceType(inDTO.getServiceType());
		pool.setPriority(inDTO.getPriority());
		pool.setStatusCd(inDTO.getStatusCd());
		return retVo;
	}

	/**
	 * 对象转换成bean,
	 *
	 * @param dto
	 * @param vPreSaleOrder
	 */
	private void convertSaleOrderToBean(PreSaleOrder vPreSaleOrder,
			com.ffcs.crmd.cas.bean.casbean.presaleorder.PreSaleOrder dto) {
		dto.setPreOrderId(vPreSaleOrder.getPreOrderId());
		if (vPreSaleOrder.getCustomerInteractionEventId() != null) {
			dto.setCustomerInteractionEventId(vPreSaleOrder.getCustomerInteractionEventId());
		}
		dto.setStaffId(vPreSaleOrder.getStaffId());
		dto.setCustId(vPreSaleOrder.getCustId());
		dto.setPreOrderNumber(vPreSaleOrder.getPreOrderNumber());
		dto.setSceneType(vPreSaleOrder.getSceneType());
		dto.setLockedStatus(vPreSaleOrder.getLockedStatus());
		if (vPreSaleOrder.getLockedStaff() != null) {
			dto.setLockedStaff(vPreSaleOrder.getLockedStaff());
		}
		if (!StringUtils.isNullOrEmpty(vPreSaleOrder.getLockedTime())) {
			dto.setLockedTime(StringUtils.strnull(vPreSaleOrder.getLockedTime()));
		}
		dto.setOrderFrom(vPreSaleOrder.getOrderFrom());
		dto.setHandlePeopleName(vPreSaleOrder.getHandlePeopleName());
		dto.setPriority(vPreSaleOrder.getPriority());
		if (!StringUtils.isNullOrEmpty(vPreSaleOrder.getAcceptTime())) {
			dto.setAcceptTime(StringUtils.strnull(vPreSaleOrder.getAcceptTime()));
		}
		if (vPreSaleOrder.getLanId() != null) {
			dto.setLanId(vPreSaleOrder.getLanId());
		}
		dto.setRemark(vPreSaleOrder.getRemark());
		dto.setPreHandleFlag(vPreSaleOrder.getPreHandleFlag());
		if (!StringUtils.isNullOrEmpty(vPreSaleOrder.getBookTime())) {
			dto.setBookTime(StringUtils.strnull(vPreSaleOrder.getBookTime()));
		}
		dto.setOrgId(vPreSaleOrder.getOrgId());
		dto.setOrderType(vPreSaleOrder.getOrderType());
		dto.setExtCustOrderId(vPreSaleOrder.getExtCustOrderId());
		dto.setIfEnd(vPreSaleOrder.getIfEnd());
		if (vPreSaleOrder.getSeq() != null) {
			dto.setSeq(vPreSaleOrder.getSeq());
		}
		dto.setProdType(vPreSaleOrder.getProdType());
		dto.setActionType(vPreSaleOrder.getActionType());
		dto.setServiceType(vPreSaleOrder.getServiceType());
		dto.setStatusCd(vPreSaleOrder.getStatusCd());
		dto.setAreaId(vPreSaleOrder.getAreaId());
		dto.setRegionCd(vPreSaleOrder.getRegionCd());
		dto.setCreateStaff(vPreSaleOrder.getCreateStaff());
		if (vPreSaleOrder.getUpdateStaff() != null) {
			dto.setUpdateStaff(vPreSaleOrder.getUpdateStaff());
		}
	}

	/**
	 * 对象转换成bean,
	 *
	 * @param dto
	 * @param vPreSaleOrder
	 */
	private void convertSaleOrderHisToBean(PreSaleOrderHis vPreSaleOrder,
			com.ffcs.crmd.cas.bean.casbean.presaleorder.PreSaleOrder dto) {
		dto.setPreOrderId(vPreSaleOrder.getPreOrderId());
		dto.setCustomerInteractionEventId(vPreSaleOrder.getCustomerInteractionEventId());
		dto.setStaffId(vPreSaleOrder.getStaffId());
		dto.setCustId(vPreSaleOrder.getCustId());
		dto.setPreOrderNumber(vPreSaleOrder.getPreOrderNumber());
		dto.setSceneType(vPreSaleOrder.getSceneType());
		dto.setLockedStatus(vPreSaleOrder.getLockedStatus());
		dto.setLockedStaff(vPreSaleOrder.getLockedStaff());
		if (!StringUtils.isNullOrEmpty(vPreSaleOrder.getLockedTime())) {
			dto.setLockedTime(StringUtils.strnull(vPreSaleOrder.getLockedTime()));
		}
		dto.setOrderFrom(vPreSaleOrder.getOrderFrom());
		dto.setHandlePeopleName(vPreSaleOrder.getHandlePeopleName());
		dto.setPriority(vPreSaleOrder.getPriority());
		if (!StringUtils.isNullOrEmpty(vPreSaleOrder.getAcceptTime())) {
			dto.setAcceptTime(StringUtils.strnull(vPreSaleOrder.getAcceptTime()));
		}
		dto.setLanId(vPreSaleOrder.getLanId());
		dto.setRemark(vPreSaleOrder.getRemark());
		dto.setPreHandleFlag(vPreSaleOrder.getPreHandleFlag());
		if (!StringUtils.isNullOrEmpty(vPreSaleOrder.getBookTime())) {
			dto.setBookTime(StringUtils.strnull(vPreSaleOrder.getBookTime()));
		}
		dto.setOrgId(vPreSaleOrder.getOrgId());
		dto.setOrderType(vPreSaleOrder.getOrderType());
		dto.setExtCustOrderId(vPreSaleOrder.getExtCustOrderId());
		dto.setIfEnd(vPreSaleOrder.getIfEnd());
		dto.setSeq(vPreSaleOrder.getSeq());
		dto.setProdType(vPreSaleOrder.getProdType());
		dto.setActionType(vPreSaleOrder.getActionType());
		dto.setServiceType(vPreSaleOrder.getServiceType());
		dto.setStatusCd(vPreSaleOrder.getStatusCd());
		dto.setAreaId(vPreSaleOrder.getAreaId());
		dto.setRegionCd(vPreSaleOrder.getRegionCd());
		dto.setCreateStaff(vPreSaleOrder.getCreateStaff());
		dto.setUpdateStaff(vPreSaleOrder.getUpdateStaff());
	}

	@Override
	public RetVo preSaleOrderCommit(com.ffcs.crmd.cas.bean.casbean.presaleordercommit.InParam _inParam) {
		RetVo retVo = new RetVo();
		retVo.setResult(true);

		String sceneType = _inParam.getSceneType();
		if ("101".equals(sceneType)) {
			// 甩单场景
			if (_inParam.getPreSaleOrderInfo() == null) {
				retVo.setResult(false);
				retVo.setMsgTitle("甩单失败，甩单信息为空");
				return retVo;
			}
			retVo = this.savePreSaleOrder(_inParam.getPreSaleOrderInfo());
		} else if ("102".equals(sceneType)) {
			// 订单确认场景，包括接口订单，包括集团订单
			if (_inParam.getNewOrderInfo() == null) {
				retVo.setResult(false);
				retVo.setMsgTitle("订单确认失败，订单确认信息为空");
				return retVo;
			}
			retVo = this.saveNewCustOrderInfo(_inParam.getNewOrderInfo());
		} else if ("103".equals(sceneType) || "104".equals(sceneType)) {
			// 订单竣工场景，包括撤销竣工
			if (_inParam.getCompleteOrderInfo() == null) {
				retVo.setResult(false);
				retVo.setMsgTitle("订单竣工通知失败，订单竣工信息为空");
				return retVo;
			}
			retVo = this.saveCompleteOrderInfo(_inParam.getCompleteOrderInfo(), sceneType);
		} else {
			retVo.setResult(false);
			retVo.setMsgTitle("场景类型sceneType：" + sceneType + "未定义，已定义的类型有：101：甩单，102：订单确认，103：订单竣工/撤销竣工，104：订单竣工且查P单无记录");
		}

		return retVo;
	}

	private RetVo saveNewCustOrderInfo(com.ffcs.crmd.cas.bean.casbean.presaleordercommit.NewOrderInfo newOrderInfo) {
		RetVo retVo = new RetVo();
		retVo.setResult(true);

		String preOrderNumber = newOrderInfo.getPreOrderNumber();
		// 根据P订单号判断数据库中是否存在
		PreSaleOrder oldPreSaleOrder = preSaleOrderService.queryFirstPreOrderByOrderNumber(preOrderNumber);
		if (oldPreSaleOrder == null) {
			retVo.setResult(false);
			retVo.setMsgTitle("订单确认失败，根据P订单号：" + preOrderNumber + "找不到对应的P订单");
			return retVo;
		}
		// 撤销场景
		if ("cancel".equals(newOrderInfo.getActionCd())) {
			// 有最后一张单标识
			if (!StringUtils.isNullOrEmpty(newOrderInfo.getIfEnd())) {
				oldPreSaleOrder.setStatusCd(newOrderInfo.getPreOrderStatus());
				oldPreSaleOrder.setIfEnd("");
				oldPreSaleOrder.update();
				PreSaleOrderPool orderPool = preSaleOrderPoolService
						.getPreSaleOrderPoolByOrderId(oldPreSaleOrder.getId());
				if (orderPool != null) {
					if (orderPool.getStaffId() != null) {
						orderPool.setStatusCd(IntfConstant.PRE_POOL_STATUS_CD_ACCEPTED.getValue());
					} else {
						orderPool.setStatusCd(IntfConstant.PRE_POOL_STATUS_CD_NOT_ACCEPTED.getValue());
					}
					orderPool.update();
				}
			}
		} else { // 默认是订单确认场景
			/** 如果是关联新的订单，则先根据新的订单ID判断是否已经存在--begin **/
			com.ffcs.crmd.cas.bean.casbean.presaleordercommit.CustOrderInfo custOrderInfo = newOrderInfo
					.getCustOrderInfo();
			if (custOrderInfo != null) {
				String custOrderIdStr = custOrderInfo.getCustOrderId();
				if (StringUtils.isNullOrEmpty(custOrderIdStr)) {
					retVo.setResult(false);
					retVo.setMsgTitle("保存预受理订单关联失败！正式订单ID为空");
					return retVo;
				}
				Long custOrderId = NumberUtils.nullToLongZero(custOrderIdStr);
				PreSaleOrder tmp = preSaleOrderService.getSelfPreSaleOrderByExtOrderId(custOrderId);
				// 如果已经存在，则不保存
				if (tmp != null) {
					retVo.setResult(true);
					return retVo;
				}
			}
			/** 如果是关联新的订单，则先根据新的订单ID判断是否已经存在--end **/
			
			PreSaleOrderPool orderPool = preSaleOrderPoolService
					.getPreSaleOrderPoolByOrderId(oldPreSaleOrder.getPreOrderId());
			// 受理FJ订单时，判断工单池记录是未接收状态，则状态设置为已接收，判断对应的接收员工接收团队是否为空，如果为空，则设置为处理员工和处理团队
			if (orderPool != null && IntfConstant.PRE_POOL_STATUS_CD_NOT_ACCEPTED.getValue()
					.equals(orderPool.getStatusCd())) {
				orderPool.setStatusCd(IntfConstant.PRE_POOL_STATUS_CD_ACCEPTED.getValue());
				// 接收员工
				if (!StringUtils.isNullOrEmpty(newOrderInfo.getHandleStaffId())
						&& StringUtils.isNullOrEmpty(orderPool.getStaffId())) {
					orderPool.setStaffId(NumberUtils.nullToLongZero(newOrderInfo.getHandleStaffId()));
				}
				// 接收团队
				if (!StringUtils.isNullOrEmpty(newOrderInfo.getHandleOrgId())
						&& StringUtils.isNullOrEmpty(orderPool.getAcceptOrgId())) {
					orderPool.setAcceptOrgId(NumberUtils.nullToLongZero(newOrderInfo.getHandleOrgId()));
				}
				// 接收时间
				if (orderPool.getAcceptDate() == null) {
					orderPool.setAcceptDate(DateUtils.dateToTimestamp(new Date()));
				}
			}
			StaffDTO staffDTO = null;
			if (!StringUtils.isNullOrEmpty(newOrderInfo.getIfEnd())) {
				oldPreSaleOrder.setIfEnd("1");
				if (IntfConstant.PRE_SALE_ORDER_STATUS_CD_ON_WAY.getValue().equals(newOrderInfo.getPreOrderStatus())) {
					// P订单状态设置为在途
					oldPreSaleOrder.setStatusCd(IntfConstant.PRE_SALE_ORDER_STATUS_CD_ON_WAY.getValue());
					// 通知移动客户端
					this.sendStateChangeToPad(oldPreSaleOrder);
					// 创建预受理处理过程
					if (!StringUtils.isNullOrEmpty(newOrderInfo.getHandleStaffId())) {
						Staff staff = Staff.repository()
								.getById(NumberUtils.nullToLongZero(newOrderInfo.getHandleStaffId()));
						if (staff != null) {
							staffDTO = new StaffDTO();
							staffDTO.setStaffId(staff.getId());
							staffDTO.setStaffName(staff.getStaffName());
						}
					}
					if (orderPool != null) {
						// 工单池设置为已处理
						orderPool.setStatusCd(IntfConstant.PRE_POOL_STATUS_CD_DEALED.getValue());
					}
				}
			} else {
				// 设置为部分受理
				oldPreSaleOrder.setStatusCd(IntfConstant.PRE_SALE_ORDER_STATUS_CD_PART_DEAL.getValue());
				// 通知移动客户端
				this.sendStateChangeToPad(oldPreSaleOrder);
			}
			// 创建新的PRE_SALE_ORDER记录
			PreSaleOrder newPreSaleOrder = null;
			if (newOrderInfo.getCustOrderInfo() != null) {
				
				newPreSaleOrder = new PreSaleOrder(true);
				newPreSaleOrder.setShardingId(oldPreSaleOrder.getPreOrderId());
				// 新的记录状态统一设置为在途
				newPreSaleOrder.setStatusCd(IntfConstant.PRE_SALE_ORDER_STATUS_CD_ON_WAY.getValue());
				newPreSaleOrder.setPreOrderNumber(preOrderNumber);
				newPreSaleOrder.setSceneType(oldPreSaleOrder.getSceneType());
				newPreSaleOrder.setPreHandleFlag(oldPreSaleOrder.getPreHandleFlag());
				convertBeanToOrder(custOrderInfo, newPreSaleOrder);
			}
			preSaleOrderService.savePreForNormalCommit(oldPreSaleOrder, orderPool, newPreSaleOrder, staffDTO);
		}

		return retVo;
	}

	/**
	 * .
	 */
	private void convertBeanToOrder(com.ffcs.crmd.cas.bean.casbean.presaleordercommit.CustOrderInfo custOrderInfo,
			PreSaleOrder preSaleOrder) {
		preSaleOrder.setExtCustOrderId(NumberUtils.nullToLongZero(custOrderInfo.getCustOrderId()));
		// 序列统一设置为2
		preSaleOrder.setSeq(2L);
		preSaleOrder.setOrderFrom(custOrderInfo.getOrderFrom());
		preSaleOrder.setChannelD(NumberUtils.nullToLongZero(custOrderInfo.getChannelId()));
		preSaleOrder.setCustId(NumberUtils.nullToLongZero(custOrderInfo.getCustId()));
		preSaleOrder.setStaffId(NumberUtils.nullToLongZero(custOrderInfo.getStaffId()));
		preSaleOrder.setOrgId(NumberUtils.nullToLongZero(custOrderInfo.getOrgId()));
		preSaleOrder.setOrderType(custOrderInfo.getCustOrderType());
		preSaleOrder.setCustSoNumber(custOrderInfo.getCustSoNumber());
		preSaleOrder.setCustName(custOrderInfo.getCustName());
		preSaleOrder.setMobilePhone(custOrderInfo.getMobilePhone());
		preSaleOrder.setCreateStaff(NumberUtils.nullToLongZero(custOrderInfo.getStaffId()));
		preSaleOrder.setUpdateStaff(NumberUtils.nullToLongZero(custOrderInfo.getStaffId()));
		preSaleOrder.setAcceptTime(DateUtils.dateToTimestamp(new Date()));
		// 本地网标识
		if (!StringUtils.isNullOrEmpty(custOrderInfo.getAreaId())
			&& "0".equals(custOrderInfo.getAreaId())) {
			preSaleOrder.setAreaId(NumberUtils.nullToLongZero(custOrderInfo.getAreaId()));
		}
		// 区域
		if (!StringUtils.isNullOrEmpty(custOrderInfo.getRegionCd())
			&& "0".equals(custOrderInfo.getRegionCd())) {
			preSaleOrder.setRegionCd(NumberUtils.nullToLongZero(custOrderInfo.getRegionCd()));
		}
	}

	private RetVo saveCompleteOrderInfo(com.ffcs.crmd.cas.bean.casbean.presaleordercommit.CompleteOrderInfo orderInfo,
			String sceneType) {
		RetVo retVo = new RetVo();
		retVo.setResult(true);

		try {
			if ("104".equals(sceneType)) { // 104场景重新计算P订单状态
				String preOrderNumber = orderInfo.getPreOrderNumber();
				// 根据P订单号判断数据库中是否存在
				PreSaleOrder oldPreSaleOrder = preSaleOrderService.queryFirstPreOrderByOrderNumber(preOrderNumber);
				if (oldPreSaleOrder == null) {
					ExceptionUtils.throwEx("订单确认失败，根据P订单号：" + preOrderNumber + "找不到对应的P订单");
				}
				String newStatus = this.calcCompletePreStatusByOrder(oldPreSaleOrder);
				if (StringUtils.isNullOrEmpty(newStatus)
						&& StringUtils.isNullOrEmpty(orderInfo.getGroupOrderFlag())) {
					retVo.setResult(true);
					retVo.setMsgTitle("P订单：" + preOrderNumber + "无需处理");
					return retVo;
				}
				orderInfo.setPreOrderStatus(newStatus);
			}
			PreSaleOrder preSaleOrder = preSaleOrderService.saveCompleteOrderInfo(orderInfo);
			if (preSaleOrder != null) {
				sendStateChangeToPad(preSaleOrder);
			}
		} catch (Exception e) {
			retVo.setResult(false);
			retVo.setMsgTitle(e.getMessage());
			e.printStackTrace();
		}
		return retVo;
	}
	/**
	 * FJ订单竣工时，计算P订单的新状态.
	 * 
	 * @param preSaleOrder
	 * @return
	 */
	private String calcCompletePreStatusByOrder(PreSaleOrder preSaleOrder) {
		String result = "";
		QueryCustOrderOutDTO orderOutDTO = queryCustOrder(preSaleOrder.getPreOrderNumber(), null, "0");
		if (!StringUtils.isNullOrEmpty(preSaleOrder.getIfEnd())) {
			if (ArrayUtils.isEmpty(orderOutDTO.getCustomerOrder())) {
				Long remainPreFee = this.getRemainPreFeeFromHb(preSaleOrder);
				if (remainPreFee == null || remainPreFee <= 0) {
					result = IntfConstant.PRE_SALE_ORDER_STATUS_CD_COMPLETE.getValue();
				}
			} else {
				boolean existsPreAccept = false;
				for (CustomerOrder customerOrder : orderOutDTO.getCustomerOrder()) {
					if (OrderConstant.CUSTOMER_ORDER_STATUS_PREACCEPT.getValue().equals(customerOrder.getStatusCd())) {
						existsPreAccept = true;
						break;
					}
				}
				if (!existsPreAccept) {
					result = IntfConstant.PRE_SALE_ORDER_STATUS_CD_COMPLETING.getValue();
				}
			}
		}
		return result;
	}
	/**
	 * 获取剩余预收费用.
	 * 
	 * @param preSaleOrder
	 * @return
	 */
	private Long getRemainPreFeeFromHb(PreSaleOrder preSaleOrder) {
		Long spId = null;
		StaffPositionDTO staffPositionDTO = new StaffPositionDTO();
        staffPositionDTO.setStaffId(preSaleOrder.getStaffId());
        staffPositionDTO.setOrgId(preSaleOrder.getOrgId());
        List<StaffPositionDTO> staffPositionDTOs = casSysFacade.queryStaffPosition(staffPositionDTO);
        if (CollectionUtils.isNotEmpty(staffPositionDTOs)) {
            spId = staffPositionDTOs.get(0).getStaffPositionId();
        }
        return this.getRemainPreFeeFromHb(preSaleOrder.getPreOrderNumber(), preSaleOrder.getOrgId(), spId);
	}

	/**
	 * .
	 *
	 * @return
	 */
	private RetVo sendStateChangeToPad(PreSaleOrder preSaleOrder) {
		PreSaleOrderDTO dto = new PreSaleOrderDTO();
		CrmBeanUtils.applyIf(dto, preSaleOrder);
		return this.sendStateChangeToPad(dto);
	}

	@Override
	public PreSaleOrderPoolDTO getPreSaleOrderByPreOrderId(Long preOrderId) {
		if (preOrderId != null) {
			PreSaleOrderPool orderPool = preSaleOrderPoolService
					.getPreSaleOrderPoolByOrderId(preOrderId);
			if (orderPool != null) {
				PreSaleOrderPoolDTO dto = new PreSaleOrderPoolDTO();
				CrmBeanUtils.applyIf(dto, orderPool);
				return dto;
			}
		}
		return null;
	}

	@Override
	public RetVo queryPreSaleOrder(String preOrderNumber) {
		RetVo retVo = new RetVo();
		QueryPreSaleOrderOutDTO outParam = new QueryPreSaleOrderOutDTO();
		outParam.setResult("0");
		com.ffcs.crmd.cas.bean.casbean.comm.Error error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
		if (StringUtils.isNullOrEmpty(preOrderNumber)) {
			outParam.setResult("1");
			error.setId("1");
			error.setMessage("P单号不能为空");
			outParam.setError(error);
			retVo.setResult(false);
			retVo.setObject(outParam);
			return retVo;
		} else {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("preOrderNumber", preOrderNumber);
			List<PreSaleOrder> preSaleOrders = preSaleOrderService.queryPreSaleOrderList(param);
			List<PreSaleOrderHis> preSaleOrderHiss = preSaleOrderService.queryPreSaleOrderHisList(param);
			if (preSaleOrders != null && preSaleOrders.size() > 0) {
				com.ffcs.crmd.cas.bean.casbean.query.queryPreSaleOrder.PreSaleOrder vPreSaleOrder = new com.ffcs.crmd.cas.bean.casbean.query.queryPreSaleOrder.PreSaleOrder();
				for (PreSaleOrder preSaleOrder : preSaleOrders) {
					vPreSaleOrder.setStatusCd(preSaleOrder.getStatusCd());
					// 必填项，初始默认值
					vPreSaleOrder.setAcceptDate("");
					vPreSaleOrder.setAcceptStaffName("");
					if (preSaleOrder.getSeq() == 1) {// 第一张单设置收单时间，收单员工，团队，员工号码
						PreSaleOrderPool pool = preSaleOrderPoolService.getByPreOrderId(preSaleOrder.getPreOrderId());
						if (pool != null) {
							vPreSaleOrder.setAcceptDate(DateUtils.date2Str(pool.getAcceptDate()));
							vPreSaleOrder.setAcceptOrgId(StringUtils.strnull(pool.getAcceptOrgId()));
							// 集中受理无法获取，传staffId给CRM特殊处理
							if (pool.getStaffId() != null) {
								Staff staff = staffService.get(pool.getStaffId());
								if (staff != null) {
									vPreSaleOrder.setAcceptStaffName(!StringUtils.isNullOrEmpty(staff.getStaffName())
											? staff.getStaffName() : "");
								}
								vPreSaleOrder.setAcceptStaffPhone(StringUtils.strnull(pool.getStaffId()));
							}
						} else {
							vPreSaleOrder.setAcceptDate(DateUtils.date2Str(preSaleOrder.getAcceptTime()));
							vPreSaleOrder.setAcceptOrgId(preSaleOrder.getOrgId() + "");
							// 集中受理无法获取，传staffId给CRM特殊处理
							if (preSaleOrder.getStaffId() != null) {
								Staff staff = staffService.get(preSaleOrder.getStaffId());
								if (staff != null) {
									vPreSaleOrder.setAcceptStaffName(!StringUtils.isNullOrEmpty(staff.getStaffName())
											? staff.getStaffName() : "");
								}
								vPreSaleOrder.setAcceptStaffPhone(StringUtils.strnull(preSaleOrder.getStaffId()));
							}
						}
					}
					if (!StringUtils.isNullOrEmpty(preSaleOrder.getExtCustOrderId())) {
						com.ffcs.crmd.cas.bean.casbean.query.queryPreSaleOrder.OrderInfo vOrderInfo = new com.ffcs.crmd.cas.bean.casbean.query.queryPreSaleOrder.OrderInfo();
						vOrderInfo.setIsFirst("NO");
						if (preSaleOrder.getSeq() == 1L) {
							vOrderInfo.setIsFirst("YES");
						}
						// 集中受理无法获取，传custOrderId给CRM特殊处理
						vOrderInfo.setCustSoNumber(StringUtils.strnull(preSaleOrder.getExtCustOrderId()));
						vPreSaleOrder.addOrderInfo(vOrderInfo);
					}
				}
				outParam.setPreSaleOrder(vPreSaleOrder);
			} else if (preSaleOrderHiss != null && preSaleOrderHiss.size() > 0) {
				com.ffcs.crmd.cas.bean.casbean.query.queryPreSaleOrder.PreSaleOrder vPreSaleOrder = new com.ffcs.crmd.cas.bean.casbean.query.queryPreSaleOrder.PreSaleOrder();
				for (PreSaleOrderHis preSaleOrderHis : preSaleOrderHiss) {
					vPreSaleOrder.setStatusCd(preSaleOrderHis.getStatusCd());
					if (preSaleOrderHis.getSeq() == 1) {// 第一张单设置收单时间，收单员工，团队，员工号码
						PreSaleOrderPool pool = preSaleOrderPoolService
								.getByPreOrderId(preSaleOrderHis.getPreOrderId());
						if (pool != null) {
							vPreSaleOrder.setAcceptDate(DateUtils.date2Str(pool.getAcceptDate()));
							vPreSaleOrder.setAcceptOrgId(StringUtils.strnull(pool.getAcceptOrgId()));
							// 集中受理无法获取，传staffId给CRM特殊处理
							if (pool.getStaffId() != null) {
								Staff staff = staffService.get(pool.getStaffId());
								if (staff != null) {
									vPreSaleOrder.setAcceptStaffName(StringUtils.isNullOrEmpty(staff.getStaffName())
											? staff.getStaffName() : "");
								}
								vPreSaleOrder.setAcceptStaffPhone(StringUtils.strnull(pool.getStaffId()));
							}
						} else {
							vPreSaleOrder.setAcceptDate(DateUtils.date2Str(preSaleOrderHis.getAcceptTime()));
							vPreSaleOrder.setAcceptOrgId(preSaleOrderHis.getOrgId() + "");
							// 集中受理无法获取，传staffId给CRM特殊处理
							if (preSaleOrderHis.getStaffId() != null) {
								Staff staff = staffService.get(preSaleOrderHis.getStaffId());
								if (staff != null) {
									vPreSaleOrder.setAcceptStaffName(StringUtils.isNullOrEmpty(staff.getStaffName())
											? staff.getStaffName() : "");
								}
								vPreSaleOrder.setAcceptStaffPhone(StringUtils.strnull(preSaleOrderHis.getStaffId()));
							}
						}
					}

					if (!StringUtils.isNullOrEmpty(preSaleOrderHis.getExtCustOrderId())) {
						com.ffcs.crmd.cas.bean.casbean.query.queryPreSaleOrder.OrderInfo vOrderInfo = new com.ffcs.crmd.cas.bean.casbean.query.queryPreSaleOrder.OrderInfo();
						vOrderInfo.setIsFirst("NO");
						if (preSaleOrderHis.getSeq() == 1L) {
							vOrderInfo.setIsFirst("YES");
						}
						// 集中受理无法获取，传custOrderId给CRM特殊处理
						vOrderInfo.setCustSoNumber(StringUtils.strnull(preSaleOrderHis.getExtCustOrderId()));
						vPreSaleOrder.addOrderInfo(vOrderInfo);
					}
				}
				outParam.setPreSaleOrder(vPreSaleOrder);
			}
		}
		retVo.setResult(true);
		retVo.setObject(outParam);
		return retVo;
	}

	@Override
	public PageInfo getPreOrderByDevStaffAndDevTeam(String saleStaffId, String saleOrgId,String statusCd, String beginDate,
			String endDate, int currentPage, int perPageNum) {
		PageInfo pageInfo = preSaleOrderService.getPreOrderByDevStaffAndDevTeam(saleStaffId, saleOrgId,statusCd, beginDate,
				endDate, currentPage, perPageNum);
		if (pageInfo != null && pageInfo.getList() != null) {
			List<PreSaleOrderDTO> hisDTOList = CrmBeanUtils.copyList(pageInfo.getList(), PreSaleOrderDTO.class);
			pageInfo.setList(hisDTOList);
		}
		return pageInfo;
	}

	@Override
	public PreSaleOrderHisDTO getHisById(Long preSaleOrderId) {
		Map mapParms = new HashMap();
		mapParms.put("preOrderId", preSaleOrderId);
		mapParms.put("shardingId", preSaleOrderId);
		List<PreSaleOrderHis> preSaleOrderHis = preSaleOrderHisService.queryPreSaleOrderHis(mapParms);
		PreSaleOrderHisDTO preSaleOrderHisDTO = new PreSaleOrderHisDTO();
		if (preSaleOrderHis != null && preSaleOrderHis.size() > 0) {
			CrmBeanUtils.applyIf(preSaleOrderHisDTO, preSaleOrderHis.get(0));
		}
		return preSaleOrderHisDTO;
	}

	@Override
	public PageInfo getHisPreOrderByDevStaffAndDevTeam(String saleStaffId, String saleOrgId,String statusCd, String beginDate,
			String endDate, int currentPage, int perPageNum) {
		PageInfo pageInfo = preSaleOrderService.getHisPreOrderByDevStaffAndDevTeam(saleStaffId, saleOrgId,statusCd, beginDate,
				endDate, currentPage, perPageNum);
		if (pageInfo != null && pageInfo.getList() != null) {
			List<PreSaleOrderHisDTO> hisDTOList = CrmBeanUtils.copyList(pageInfo.getList(), PreSaleOrderHisDTO.class);
			pageInfo.setList(hisDTOList);
		}
		return pageInfo;
	}

	/**
	 * 集团单省内缴费归档处理
	 *
	 * @author YULIYAO 2016/4/7 return
	 */
	@Override
	public PageInfo queryCompleteGroupOrderAuto(int model, int procCount, int perSize) {
		PageInfo pageInfo = preSaleOrderService.queryCompleteGroupOrderAuto(model, procCount, perSize);
		if (pageInfo != null && CollectionUtils.isNotEmpty(pageInfo.getList())) {
			List<PreSaleOrderDTO> preSaleOrderDTOs = new ArrayList<>();
			pageInfo.setList(CrmBeanUtils.copyList(pageInfo.getList(), PreSaleOrderDTO.class));
		}
		return pageInfo;
	}
	@Override
	public String getLastCustOrderIdByPreOrderNbr(String preOrderNumber) {
		String result = "";
		if (!StringUtils.isNullOrEmpty(preOrderNumber)) {
			result = preSaleOrderService.getLastCustOrderIdByPreOrderNbr(preOrderNumber);
		}
		return result;
	}
	@Override
	public Map<String, Object> getPreOrderStatusAndStatusNumber(String saleStaffId, String saleOrgId,
			String beginDate, String endDate) {
		return preSaleOrderService.getPreOrderStatusAndStatusNumber(saleStaffId, saleOrgId, beginDate, endDate);
	}

	/**
	 * 是否显示机器人受理按钮
	 * @author YULIYAO 2016/4/19
	 * return
	 */
	@Override
	public boolean isShowRobotBtn(PreSaleOrderDTO preSaleOrderDTO) {
		//只针对4G单产品新装的单子做展示
		if (CasConstant.PRE_SALE_ORDER_STATUS_CD_WAIT_FOR_DEAL.getValue()
				.equals(preSaleOrderDTO.getStatusCd()) && CasConstant.SCENE_TYPE_4G_NEW_USER
						.getValue().equals(preSaleOrderDTO.getSceneType())) {
			//根据自动受理表，状态是新增的才允许送
			AutoGenOrderDTO autoQueryDTO = new AutoGenOrderDTO();
			autoQueryDTO.setPreSaleOrder(preSaleOrderDTO.getPreOrderNumber());
			List<AutoGenOrderDTO> autoGenOrderDTOs = autoGenOrderFacade
					.queryAutoGenOrder(autoQueryDTO);
			if (CollectionUtils.isNotEmpty(autoGenOrderDTOs)) {
				AutoGenOrderDTO ago = autoGenOrderDTOs.get(0);
				if (CasConstant.AUTO_GEN_ORDER_STATUS_CREATE.getValue().equals(ago.getStatusCd())
						|| CasConstant.AUTO_GEN_ORDER_STATUS_SEND_PK_FAIL.getValue()
						.equals(ago.getStatusCd())) {
					//只针对待受理状态的单子做展示
					PreSaleOrderPool preSaleOrderPool = preSaleOrderPoolService
							.getByPreOrderId(preSaleOrderDTO.getPreOrderId());
					if (preSaleOrderPool != null && !StringUtils
							.isNullOrEmpty(preSaleOrderPool.getStaffId())) {
						return true;
					}
				}


			}
		}
		return false;
	}

	/**
	 * 机器人受理按钮响应
	 * @author YULIYAO 2016/4/19
	 * return
	 */
	@Override
	public void autoGen(PreSaleOrderDTO preSaleOrderDTO) {
		AutoGenOrderDTO autoQueryDTO = new AutoGenOrderDTO();
		autoQueryDTO.setPreSaleOrder(preSaleOrderDTO.getPreOrderNumber());
		List<AutoGenOrderDTO> autoGenOrderDTOs = autoGenOrderFacade
				.queryAutoGenOrder(autoQueryDTO);
		if (CollectionUtils.isNotEmpty(autoGenOrderDTOs)) {
			AutoGenOrderDTO ago = autoGenOrderDTOs.get(0);
			if (CasConstant.AUTO_GEN_ORDER_STATUS_CREATE.getValue().equals(ago.getStatusCd())
					|| CasConstant.AUTO_GEN_ORDER_STATUS_SEND_PK_FAIL.getValue()
					.equals(ago.getStatusCd())) {
				RetVo retVo = autoGenOrderFacade.sendToPk(ago);
				if (!retVo.getResult()) {
					ExceptionUtils.throwEx("操作失败!错误信息："+retVo.getMsgTitle());
				}
			} else {
				ExceptionUtils.throwEx("操作失败！已经发起过机器人受理！");
			}
		} else {
			ExceptionUtils.throwEx("操作失败！自动受理记录为空！");
		}
	}
	@Override
	public RetVo returnToPreSale(String preOrderNumber, Long handleStaffId, String reason, String srcFlag, String reasonType) {
		RetVo retVo = new RetVo();
		retVo.setResult(true);
		if (StringUtils.isNullOrEmpty(preOrderNumber)) {
			retVo.setResult(false);
			retVo.setDetailMsg("预受理订单号为空");
			return retVo;
		}
		if (StringUtils.isNullOrEmpty(reason)) {
			retVo.setResult(false);
			retVo.setDetailMsg("退单原因为空");
			return retVo;
		}
		if (handleStaffId == null) {
			handleStaffId = 51447L; // 默认接口工号
		}
		Staff staff = Staff.repository().getById(handleStaffId);
		if (staff == null) {
			retVo.setResult(false);
			retVo.setDetailMsg("退单员工为空");
			return retVo;
		}
		PreSaleOrder preSaleOrder = preSaleOrderService.queryFirstPreOrderByOrderNumber(preOrderNumber);
		if (preSaleOrder == null) {
			retVo.setResult(false);
			retVo.setDetailMsg("根据P订单号：" + preOrderNumber + "找不到对应的P订单！");
			return retVo;
		}
		PreSaleOrderPool orderPool = preSaleOrderPoolService
				.getPreSaleOrderPoolByOrderId(preSaleOrder.getPreOrderId());
		if (orderPool == null) {
			retVo.setResult(false);
			retVo.setDetailMsg("根据P订单：" + preOrderNumber + "找不到对应的工单池记录！");
			return retVo;
		}
		if (IntfConstant.PRE_POOL_STATUS_CD_GOBACKED.getValue().equals(orderPool.getStatusCd())) {
			retVo.setResult(true);
			retVo.setDetailMsg("回退成功！");
			return retVo;
		}
		// 将旧的退单原因保留
		String oldReason = "";
		if (!StringUtils.isNullOrEmpty(orderPool.getGoBackContent())) {
			oldReason = orderPool.getGoBackedReason();
			orderPool.cleanGoBackContent();
		}
		String reasonAll = oldReason + "\n----" + DateUtils.convertDateToStr(DateUtils.getNowDate(), "yyyy-MM-dd hh24:mm:ss")
				+ "----" + staff.getStaffName() + "----\n" + reason;
		// 设置退单原因到remark字段
		orderPool.setGoBackContent(staff.getStaffName(), "", reasonAll);
		
		orderPool.setStatusCd(IntfConstant.PRE_POOL_STATUS_CD_GOBACKED.getValue());
		orderPool.setPriority(1L);
		
		// 保存退单处理过程
		List<PreSaleOrderProcVo> procVos = new ArrayList<PreSaleOrderProcVo>();
		PreSaleOrderProcVo procVo = new PreSaleOrderProcVo();
		procVos.add(procVo);
		
		// 设置退单原因
		procVo.setAttrDesc(reason);
		AttrValue attrValue = CrmClassUtils.getAttrValueByValueName(
				PreSaleOrderProc.class.getSimpleName(), "backReasonList", reasonType);
		AttrValue pfAttrValue = CrmClassUtils.getAttrValueByValueName(
				PreSaleOrderProc.class.getSimpleName(), "backReasonList", "H01");
		if (attrValue != null) {
			procVo.setAttrId(attrValue.getAttrId());
			procVo.setAttrValueId(attrValue.getAttrValueId());
			procVo.setAttrValue(attrValue.getAttrValueName());
		} else if ("audit".equals(srcFlag) && pfAttrValue != null) {
			procVo.setAttrId(pfAttrValue.getAttrId());
			procVo.setAttrValueId(pfAttrValue.getAttrValueId());
			procVo.setAttrValue(pfAttrValue.getAttrValueName());
		} else {
			// 如果没有选择退单原因类型，则仅保存attr_id
			Long attrId = CrmClassUtils.getAttrSpecByCode(PreSaleOrderProc.class
					.getSimpleName(), "backReasonList") != null ? CrmClassUtils
							.getAttrSpecByCode(PreSaleOrderProc.class.getSimpleName(),
							"backReasonList").getId()
							: 0L;
			procVo.setAttrId(attrId);
			//g.zhangyy crm00062335  保存到数据库中时，若为空，需要默认为“Z01_其他”
			procVo.setAttrValueId(NumberUtils.toLong(IntfConstant.GO_BACK_ATTR_ID.getValue()));
			procVo.setAttrValue(IntfConstant.GO_BACK_ATTR_VALUE_NAME.getValue());
		}
		Long eventId = NumberUtils.toLong(IntfConstant.PRE_PROC_EVENT_ID_POOL_RET.getValue());
		if ("audit".equals(srcFlag)) {
			eventId = NumberUtils.toLong(IntfConstant.PRE_PROC_EVENT_ID_AUDIT_RET.getValue());
		}
		StaffDTO staffDTO = new StaffDTO();
		staffDTO.setStaffId(staff.getStaffId());
		staffDTO.setStaffName(staff.getStaffName());
		
		preSaleOrderService.savePreSaleOrderForReturn(preSaleOrder, orderPool, staffDTO, procVos, eventId);
		
		return retVo;
	}

	@Override
	public RetVo preSaleOrderPayDeal(PreSaleOrderDTO preSaleOrderDTO) {
		RetVo retVo = new RetVo(true);
		PreSaleOrder preSaleOrder = PreSaleOrder.repository().getById(preSaleOrderDTO.getPreOrderId());
		//退费中处理
		if (IntfConstant.PRE_SALE_ORDER_STATUS_CD_BACK_PAYING.getValue().equals(preSaleOrder.getStatusCd())) {
			
			preSaleOrder.setIfEnd("1");
			//费用项处理。
			preSaleOrderService.cancelOrderForAcctItem(preSaleOrder,"撤销退费完成");
			// 通知移动客户端已撤销
			stateChange(preSaleOrderDTO, IntfConstant.MOBI_PRE_ORDER_STATUS_CD_REMOVED.getValue());
			//费用项处理
			preSaleOrderService.remove(preSaleOrder);
		} else {
			//逻辑待补充，目前先空跑
		}
		
		return retVo;
	
	}
	@Override
	public RetVo checkCouldCancelForIntf(String preOrderNumber, String orgIdStr) {
		RetVo retVo = new RetVo();
		retVo.setResult(true);
		
		if (StringUtils.isNullOrEmpty(preOrderNumber)) {
			retVo.setResult(false);
			retVo.setMsgTitle("P订单号为空");
			return retVo;
		}
		PreSaleOrder preSaleOrder = preSaleOrderService.queryFirstPreOrderByOrderNumber(preOrderNumber);
		if (preSaleOrder == null) {
			retVo.setResult(false);
			retVo.setMsgTitle("根据P订单号[" + preOrderNumber + "]找不到对应的预受理订单，或者关联的订单已经全部处理完成");
			return retVo;
		}
		retVo = this.checkCouldCancel(NumberUtils.nullToLongZero(orgIdStr), preSaleOrder);
		
		PreSaleOrderDTO preSaleOrderDTO = new PreSaleOrderDTO();
		CrmBeanUtils.applyIf(preSaleOrderDTO, preSaleOrder);
		
		retVo.setObject(preSaleOrderDTO);
		
		return retVo;
	}
	@Override
	public RetVo cancelPreOrder(CancelPreOrderVo cancelPreOrderVo) {
		RetVo retVo = new RetVo();
		retVo.setResult(true);
		
		if (cancelPreOrderVo == null) {
			retVo.setResult(false);
			retVo.setDetailMsg("撤销信息为空");
			return retVo;
		}
		String preOrderNumber = cancelPreOrderVo.getPreOrderNumber();
		if (StringUtils.isNullOrEmpty(preOrderNumber)) {
			retVo.setResult(false);
			retVo.setDetailMsg("预受理订单号为空");
			return retVo;
		}
		PreSaleOrder preSaleOrder = preSaleOrderService.queryFirstPreOrderByOrderNumber(preOrderNumber);
		if (preSaleOrder == null) {
			retVo.setResult(false);
			retVo.setDetailMsg("根据P订单号[" + preOrderNumber + "]找不到对应的预受理订单，或者关联的订单已经全部处理完成");
			return retVo;
		}
		// 撤销前先校验是否可以撤销
		retVo = this.checkCouldCancel(cancelPreOrderVo.getOrgId(), preSaleOrder);
		if (retVo != null && !retVo.getResult()) {
			return retVo;
		}
		Long staffId = null;
		if (cancelPreOrderVo.getStaffId() != null) {
			staffId = NumberUtils.nullToLongZero(cancelPreOrderVo.getStaffId());
		} else {
			staffId = 51447L;
		}
		Staff staff = Staff.repository().getById(staffId);
		if (staff == null) {
			retVo.setResult(false);
			retVo.setDetailMsg("撤销员工为空");
			return retVo;
		}
		StaffDTO staffDTO = new StaffDTO();
		staffDTO.setStaffId(staff.getId());
		staffDTO.setStaffName(staff.getStaffName());
		
		// 保存
		preSaleOrderService.saveCancelPreOrder(preSaleOrder,
				cancelPreOrderVo.getCancelFlag(), staffDTO,
				cancelPreOrderVo.getStaffPositionId(),
				cancelPreOrderVo.getCancelReason(),
				cancelPreOrderVo.getAmount());
		// 通知移动客户端已撤销
		if ("1".equals(cancelPreOrderVo.getCancelFlag())) {
			this.sendStateChangeToPad(preSaleOrder);
		}
		
		return retVo;
	}

	/**
	 * 根据预受理订单ID删除预受理订单历史记录
	 * @author YULIYAO 2016/5/16
	 * return
	 */
	@Override
	public int deletePreOrderHisByOrderId(Long preOrderId, Long shardingId) {
		int result = 0;
		Map param = new HashMap();
		param.put("preOrderId", preOrderId);
		param.put("shardingId", shardingId);
		List<PreSaleOrderHis> preSaleOrderHises = preSaleOrderService
				.queryPreSaleOrderHisList(param);
		if (CollectionUtils.isNotEmpty(preSaleOrderHises)) {
			for (PreSaleOrderHis preSaleOrderHis : preSaleOrderHises) {
				result += preSaleOrderHis.remove();
			}
		}
		return result;
	}

	/**
	 * 针对员工的联系方式  接收员工
	 *
	 * @param staffId
	 */
	public String getPartyContactPhone(String staffId) {
		String phone = "";
		String[] staffIds = new String[]{staffId};
		RetVo retVo = casToCrmFacade.qryCustInfo(staffIds, "1");
		if (retVo.getResult()) {
			PartyContactInfo[] partyContactInfos = (PartyContactInfo[]) retVo.getObject();
			for (int i = 0; i < partyContactInfos.length; i++) {
				if (!StringUtils.isNullOrEmpty(partyContactInfos[i].getMobilePhone())) {
					phone = partyContactInfos[i].getMobilePhone();
				} else if (!StringUtils.isNullOrEmpty(partyContactInfos[i].getOfficePhone())) {
					phone = partyContactInfos[i].getOfficePhone();
				} else {
					phone = partyContactInfos[i].getHomePhone();
				}
			}
		}
		return phone;
	}

	/**
	 * 通过纵表属性“预受理缴费员工岗位”获取员工ID.
	 *
	 * @param preSaleOrderId
	 * @return
	 * @author chenjw
	 * 2014-6-6
	 */
	public Long getPreSalePayStaffId(Long preSaleOrderId) {
		AttrSpec attrSpec = CrmClassUtils.getAttrSpecByCode(CasConstant.PRE_SALE_ORDER.getValue(),
				CasConstant.JAVACODE_PRE_SALE_PAY_STAFF_POSITION.getValue());
		//获取属性
		PreSaleOrderAttr preSaleOrderAttr = preSaleOrderAttrService.getByOrderAndAttrId(preSaleOrderId,
				attrSpec.getAttrId());

		if (preSaleOrderAttr != null) {
			StaffPositionDTO staffPositionDTO = casSysFacade.getStaffPositionById(NumberUtils.toLong(preSaleOrderAttr.getAttrValue()));
			if (staffPositionDTO != null) {
				return staffPositionDTO.getStaffId();
			}
		}
		return null;
	}

}


