package com.ffcs.crmd.cas.intf.facade.impl;

import com.ctg.itrdc.platform.common.entity.PageInfo;
import com.ctg.itrdc.platform.common.exception.RtManagerException;
import com.ctg.itrdc.platform.common.utils.type.*;
import com.ffcs.crmd.cas.base.encrypt.Cryto;
import com.ffcs.crmd.cas.base.utils.CrmClassUtils;
import com.ffcs.crmd.cas.bean.casbean.query.preSaleOrderCount.OrderNumForStatusCd;
import com.ffcs.crmd.cas.bean.casbean.query.querypreorderflow.FlowInfo;
import com.ffcs.crmd.cas.bean.casbean.query.qureryintassignorg.Root;
import com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderOverTimeList.PreSaleOrderDetail;
import com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderProc.PreSaleOrderProc;
import com.ffcs.crmd.cas.constants.IntfConstant;
import com.ffcs.crmd.cas.intf.api.dto.*;
import com.ffcs.crmd.cas.intf.api.facade.ICasFacade;
import com.ffcs.crmd.cas.intf.api.facade.ICasToCrmFacade;
import com.ffcs.crmd.cas.intf.util.WsUtil;
import com.ffcs.crmd.cas.intf.util.crm.ToGb2312;
import com.ffcs.crmd.cas.order.api.dto.*;
import com.ffcs.crmd.cas.order.api.facade.*;
import com.ffcs.crmd.cas.order.api.vo.CancelPreOrderVo;
import com.ffcs.crmd.cas.sys.api.dto.StaffDTO;
import com.ffcs.crmd.cas.sys.api.facade.ICasSysFacade;
import com.ffcs.crmd.platform.pub.bean.CrmBeanUtils;
import com.ffcs.crmd.platform.pub.vo.RetVo;
import org.exolab.castor.xml.Unmarshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.Writer;
import java.util.*;

/**
 * .
 *
 * @author Luxb
 * @version Revision 1.0.0
 * @版权：福富软件 版权所有 (c) 2011
 * @see: @创建日期：2016/1/16 @功能说明：
 */
@Service("casFacade")
public class CasFacadeImpl implements ICasFacade {
	@Autowired
	IPreSaleOrderFacade preSaleOrderFacade;
	@Autowired
	IAutoGenOrderFacade autoGenOrderFacade;
	@Autowired
	ICasToCrmFacade casToCrmFacade;
	@Autowired
	IPreSaleOrderPoolFacade preSaleOrderPoolFacade;
	@Autowired
	ICasSysFacade casSysFacade;
	@Autowired
	IPreSaleOrderProcFacade preSaleOrderProcFacade;
	@Autowired
	IInteractionAssignOrgFacade assignOrgFacade;

	@Override
	public String queryPreOrderStatus(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.query.querypreorderstatus.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.query.querypreorderstatus.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.query.querypreorderstatus.OutParam _outParam = new com.ffcs.crmd.cas.bean.casbean.query.querypreorderstatus.OutParam();
		
		try {
			root = (com.ffcs.crmd.cas.bean.casbean.query.querypreorderstatus.Root) Unmarshaller.unmarshal(
					com.ffcs.crmd.cas.bean.casbean.query.querypreorderstatus.Root.class,
					new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
			_inParam = root.getMsgBody().getInParam();
			QueryPreOrderStatusInDTO dto = new QueryPreOrderStatusInDTO();
			CrmBeanUtils.applyIf(dto, _inParam);
			QueryPreOrderStatusOutDTO outDTO = preSaleOrderFacade.queryPreOrderStatus(dto);
			_outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
			if (outDTO != null) {
				CrmBeanUtils.applyIf(_outParam, outDTO, false);
			}
		} catch (Exception e) {
			_outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			com.ffcs.crmd.cas.bean.casbean.query.querypreorderstatus.Error _error = new com.ffcs.crmd.cas.bean.casbean.query.querypreorderstatus.Error();
			_error.setId("11");
			_error.setMessage(e.getMessage());
			_outParam.setError(_error);
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.query.querypreorderstatus.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	@Override
	public String queryPreOrderIsRelation(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.query.querypreorderisrelation.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.query.querypreorderisrelation.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.query.querypreorderisrelation.OutParam _outParam = null;

		root = (com.ffcs.crmd.cas.bean.casbean.query.querypreorderisrelation.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.query.querypreorderisrelation.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();
		QueryPreSaleOrderIsRelationInDTO dto = new QueryPreSaleOrderIsRelationInDTO();
		CrmBeanUtils.applyIf(dto, _inParam);
		QueryPreSaleOrderIsRelationOutDTO outDTO = preSaleOrderFacade.queryPreSaleOrderIsRelation(dto);
		if (outDTO != null) {
			_outParam = new com.ffcs.crmd.cas.bean.casbean.query.querypreorderisrelation.OutParam();
			CrmBeanUtils.applyIf(_outParam, outDTO);
			_outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.query.querypreorderisrelation.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	@Override
	public String saveAutoGenOrder(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.autogenordersave.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.autogenordersave.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.autogenordersave.OutParam _outParam = null;

		root = (com.ffcs.crmd.cas.bean.casbean.autogenordersave.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.autogenordersave.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();
		AutoGenOrderInDTO dto = new AutoGenOrderInDTO();
		CrmBeanUtils.applyIf(dto, _inParam);
		int i = autoGenOrderFacade.saveAutoGenOrder(dto);
		if (i > 0) {
			_outParam = new com.ffcs.crmd.cas.bean.casbean.autogenordersave.OutParam();
			_outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.autogenordersave.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	@Override
	public String autoGenOrderComplete(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.autogenordercomplete.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.autogenordercomplete.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.autogenordercomplete.OutParam _outParam = new com.ffcs.crmd.cas.bean.casbean.autogenordercomplete.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error _error = null;

		root = (com.ffcs.crmd.cas.bean.casbean.autogenordercomplete.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.autogenordercomplete.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();
		String preOrderNumber = _inParam.getPreSaleOrder();
		if (preOrderNumber.indexOf("-N") > 0) {
			preOrderNumber = preOrderNumber.replace("-N", "");
		}
		String autoGenResult = _inParam.getAutoGenResult();
		String resultDesc = _inParam.getResultDesc();
		String pic = Cryto.base64Encode(_inParam.getPic());
		String errCode = _inParam.getErrCode();
		RetVo retVO = autoGenOrderFacade.autoGenOrderCompleteInner(preOrderNumber, autoGenResult, resultDesc, pic,
				errCode);
		if (retVO != null && retVO.getResult()) { // 成功
			_outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
		} else { // 失败
			_outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			_error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			_outParam.setError(_error);
			_error.setId("111");
			_error.setMessage(retVO.getMsgTitle());// 2015-07-22 chenj
			// crm00063652
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.autogenordersave.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	@Override
	public String dropPreSaleOrderToCrm(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.droppresaleordertocrm.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.droppresaleordertocrm.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.droppresaleordertocrm.OutParam _outParam = new com.ffcs.crmd.cas.bean.casbean.droppresaleordertocrm.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error _error = null;

		root = (com.ffcs.crmd.cas.bean.casbean.droppresaleordertocrm.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.droppresaleordertocrm.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();
		RetVo retVo = preSaleOrderFacade.saveDropPreSaleOrder(_inParam.getPreOrderNumber(), _inParam.getDropDesc(),
				_inParam.getDropResult());
		if (retVo != null && retVo.getResult()) {
			_outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
		} else {
			_outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			_error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			_error.setId(retVo.getRetCode());
			_error.setMessage(retVo.getMsgTitle());
			_outParam.setError(_error);
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.droppresaleordertocrm.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	@Override
	public String modPreSaleOrder(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.modpresaleorder.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.modpresaleorder.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.modpresaleorder.OutParam _outParam = new com.ffcs.crmd.cas.bean.casbean.modpresaleorder.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error _error = null;

		root = (com.ffcs.crmd.cas.bean.casbean.modpresaleorder.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.modpresaleorder.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();
		String _preOrderNumber = _inParam.getPreOrderNumber();
		String _receiveStaffId = _inParam.getReceiveStaffId();
		String _receiveOrgId = _inParam.getReceiveOrgId();
		String _remark = _inParam.getRemark();
		RetVo retVo = preSaleOrderFacade.intfModPreSaleOrderPool(_preOrderNumber, _receiveStaffId, _receiveOrgId,
				_remark);
		if (retVo != null && retVo.getResult()) {
			_outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
		} else {
			_outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			_error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			_error.setId(retVo.getRetCode());
			_error.setMessage(retVo.getMsgTitle());
			_outParam.setError(_error);
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.modpresaleorder.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	@Override
	public String preSaleOrderHasFee(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.query.presaleorderhasfee.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.query.presaleorderhasfee.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.query.presaleorderhasfee.OutParam _outParam = new com.ffcs.crmd.cas.bean.casbean.query.presaleorderhasfee.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error _error = null;

		root = (com.ffcs.crmd.cas.bean.casbean.query.presaleorderhasfee.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.query.presaleorderhasfee.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();
		String preOrderNumber = _inParam.getPreOrderNumber();
		RetVo retVo = preSaleOrderFacade.orderHasFee(preOrderNumber);
		if (retVo != null && retVo.getResult()) {
			_outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
			_outParam.setIsFee("0");
			_outParam.setPreOrderNumber(StringUtils.strnull(retVo.getObject()));
		} else {
			_outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			_error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			_outParam.setIsFee("1");
			_error.setId(retVo.getRetCode());
			_error.setMessage(retVo.getMsgTitle());
			_outParam.setError(_error);
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.query.presaleorderhasfee.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	@Override
	public String preCompletePayment(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.hbbean.precompletepayment.Root root = null;
		com.ffcs.crmd.cas.bean.hbbean.precompletepayment.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.hbbean.precompletepayment.OutParam _outParam = new com.ffcs.crmd.cas.bean.hbbean.precompletepayment.OutParam();
		com.ffcs.crmd.cas.bean.hbbean.comm.Error _error = null;

		root = (com.ffcs.crmd.cas.bean.hbbean.precompletepayment.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.hbbean.precompletepayment.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();
		String preOrderNumber = _inParam.getPreCustSoNum();
		Long reqType = _inParam.getReqType();
		RetVo retVo = preSaleOrderFacade.preCompletePayment(preOrderNumber, reqType);
		if (retVo != null && retVo.getResult()) {
			_outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
		} else {
			_outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			_error = new com.ffcs.crmd.cas.bean.hbbean.comm.Error();
			_error.setId("11");
			_error.setMessage(retVo.getMsgTitle());
			_outParam.setError(_error);
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.hbbean.precompletepayment.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	@Override
	public String cancelOrderCommit(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.cancel.cancelordercommit.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.cancel.cancelordercommit.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.cancel.cancelordercommit.OutParam _outParam = new com.ffcs.crmd.cas.bean.casbean.cancel.cancelordercommit.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error _error = null;

		root = (com.ffcs.crmd.cas.bean.casbean.cancel.cancelordercommit.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.cancel.cancelordercommit.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();
		Long orgId = NumberUtils.nullToLongZero(_inParam.getTeamId());
		Long staffPositionId = NumberUtils.nullToLongZero(_inParam.getStaffPositionId());
		if (_inParam.getPreOrder() == null) {
			throw new Exception("撤销失败！预受理订单信息为空！");
		}
		com.ffcs.crmd.cas.bean.casbean.cancel.cancelordercommit.CustomerOrder[] vCustomerOrders = _inParam
				.getCustomerOrder();
		String preOrderNumber = StringUtils.strnull(_inParam.getPreOrder().getPreCustSoNum());
		RetVo retVO = preSaleOrderFacade.validateInParam(orgId, staffPositionId, preOrderNumber);
		// false异常 by laiyongmin
		if (!retVO.getResult()) {
			throw new Exception(retVO.getMsgTitle());
		}
		// edit by laiyongmin 对象异常问题
		// PreSaleOrder preSaleOrder = (PreSaleOrder) retVO.getObject();
		PreSaleOrderDTO preSaleOrderDTO = new PreSaleOrderDTO();
		CrmBeanUtils.applyIf(preSaleOrderDTO, retVO.getObject());
		// 调用crm接口判断关联的订单是否可以撤销
		// 传入的参数 订单id串、报文中传入的订单数量
		String custOrderId = preSaleOrderFacade.getExtCustOrderIds(preSaleOrderDTO.getPreOrderNumber());
		retVO = casToCrmFacade.checkCancel(custOrderId, orgId, staffPositionId, _inParam.getCustomerOrder());
		if (!retVO.getResult()) {
			throw new Exception(retVO.getMsgTitle());
		}
		// 根据crm返回的可以撤销的订单流水号
		String isCancel = retVO.getRetCode();
		List<com.ffcs.crmd.cas.bean.casbean.cancel.cancelordercommit.CustomerOrder> custOrders = null;
		if ("0".equals(isCancel)) {
			com.ffcs.crmd.cas.bean.crmbean.checkcancel.CustSoNumbers[] custSoNumbers = ((com.ffcs.crmd.cas.bean.crmbean.checkcancel.OutParam) retVO
					.getObject()).getCustSoNumbers();
			custOrders = new ArrayList<com.ffcs.crmd.cas.bean.casbean.cancel.cancelordercommit.CustomerOrder>();
			if (custSoNumbers != null && custSoNumbers.length > 0) {
				for (com.ffcs.crmd.cas.bean.crmbean.checkcancel.CustSoNumbers str : custSoNumbers) {
					for (com.ffcs.crmd.cas.bean.casbean.cancel.cancelordercommit.CustomerOrder custOrder : vCustomerOrders) {
						if (str.getCustSoNumber().equals(custOrder.getCustSoNumber())) {
							custOrders.add(custOrder);
						}
					}
				}
			}
		}

		RetVo ret2 = preSaleOrderFacade.doPreFeeCancel(orgId, _inParam.getLatnId(), staffPositionId,
				_inParam.getPreOrder(), custOrders, preSaleOrderDTO);
		if (!ret2.getResult()) {
			throw new Exception(ret2.getMsgTitle());
		} else {
			// 撤销原因
			String cancelReason = "";
			if (vCustomerOrders != null && vCustomerOrders.length > 0) {
				com.ffcs.crmd.cas.bean.casbean.cancel.cancelordercommit.CustomerOrder custOrder = vCustomerOrders[0];
				if (custOrder.getCancelInfo() != null) {
					cancelReason = custOrder.getCancelInfo().getCancelReason();
				}
			}
			retVO = preSaleOrderFacade.saveOrderCancel(preSaleOrderDTO, staffPositionId,
					_inParam.getPreOrder().getAmount(), retVO.getRetCode(), cancelReason);
		}

		_outParam.setResult("10");
		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.cancel.cancelordercommit.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	@Override
	public String queryPreOrder(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.query.querypreOrder.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.query.querypreOrder.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.query.querypreOrder.OutParam _outParam = new com.ffcs.crmd.cas.bean.casbean.query.querypreOrder.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error _error = null;

		root = (com.ffcs.crmd.cas.bean.casbean.query.querypreOrder.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.query.querypreOrder.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();
		String extCustOrderId = _inParam.getExtCustOrderId();
		String qryType = _inParam.getQryType();
		String preOrderNumber = _inParam.getPreOrderNumber();
		RetVo retVo = null;
		Map map = new HashMap();
		map.put("extCustOrderId", NumberUtils.toLong(extCustOrderId));
		map.put("preOrderNumber", preOrderNumber);
		map.put("qryType", qryType);
		map.put("qryHis", _inParam.getQryHis());
		retVo = preSaleOrderFacade.queryPreSaleOrder(map);
		if (retVo != null && retVo.getResult()) {
			_outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
			com.ffcs.crmd.cas.bean.casbean.presaleorder.PreSaleOrder preSaleOrder = new com.ffcs.crmd.cas.bean.casbean.presaleorder.PreSaleOrder();
			PreSaleOrderDTO preSaleOrderDTO = (PreSaleOrderDTO) retVo.getObject();
			if (preSaleOrderDTO == null) {
				preSaleOrder = null;
			} else {
				convertDtoToBean(preSaleOrder, preSaleOrderDTO);
				if (!StringUtils.isNullOrEmpty(preSaleOrderDTO.getPreOrderNumber())) {
					String lastExtCustOrderId = preSaleOrderFacade
							.getLastCustOrderIdByPreOrderNbr(preSaleOrderDTO.getPreOrderNumber());
					_outParam.setLastExtCustOrderId(lastExtCustOrderId);
				}
				if (!StringUtils.isNullOrEmpty(preSaleOrderDTO.getPreOrderNumber())) {
					String extCustOrderIds = preSaleOrderFacade.getExtCustOrderIds(preSaleOrderDTO.getPreOrderNumber());
					_outParam.setExtCustOrderIds(extCustOrderIds);
				}
				if (preSaleOrderDTO.getPreOrderId() != null) {
					// 获取工单池信息
					PreSaleOrderPoolDTO dto = preSaleOrderFacade.getPreSaleOrderByPreOrderId(preSaleOrder.getPreOrderId());
					if (dto != null) {
						com.ffcs.crmd.cas.bean.casbean.query.querypreOrder.OrderPoolInfo orderPoolInfo = new com.ffcs.crmd.cas.bean.casbean.query.querypreOrder.OrderPoolInfo();
						_outParam.setOrderPoolInfo(orderPoolInfo);
						orderPoolInfo.setStatusCd(dto.getStatusCd());
						orderPoolInfo.setOrderPoolRemark(dto.getRemark());
					}
				}
				_outParam.setPreSaleOrder(preSaleOrder);
			}
		} else {
			_outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			_error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			_error.setId("11");
			_error.setMessage(retVo.getMsgTitle());
			_outParam.setError(_error);
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.query.querypreOrder.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}
	/**
	 * 
	 */
	private void convertDtoToBean(
			com.ffcs.crmd.cas.bean.casbean.presaleorder.PreSaleOrder preSaleOrder,
			PreSaleOrderDTO preSaleOrderDTO) {
		if (preSaleOrder != null && preSaleOrderDTO != null) {
			if (preSaleOrderDTO.getPreOrderId() != null) {
				preSaleOrder.setPreOrderId(preSaleOrderDTO.getPreOrderId());
			}
			if (preSaleOrderDTO.getCustomerInteractionEventId() != null) {
				preSaleOrder.setCustomerInteractionEventId(preSaleOrderDTO.getCustomerInteractionEventId());
			}
			if (preSaleOrderDTO.getStaffId() != null) {
				preSaleOrder.setStaffId(preSaleOrderDTO.getStaffId());
			}
			if (preSaleOrderDTO.getCustId() != null) {
				preSaleOrder.setCustId(preSaleOrderDTO.getCustId());
			}
			preSaleOrder.setPreOrderNumber(preSaleOrderDTO.getPreOrderNumber());
			preSaleOrder.setSceneType(preSaleOrderDTO.getSceneType());
			preSaleOrder.setOrderType(preSaleOrderDTO.getOrderType());
			preSaleOrder.setHandlePeopleName(preSaleOrderDTO.getHandlePeopleName());
			preSaleOrder.setPriority(preSaleOrderDTO.getPriority());
			if (preSaleOrderDTO.getLanId() != null) {
				preSaleOrder.setLanId(preSaleOrderDTO.getLanId());
			}
			preSaleOrder.setRemark(preSaleOrderDTO.getRemark());
			preSaleOrder.setPreHandleFlag(preSaleOrderDTO.getPreHandleFlag());
			if (preSaleOrderDTO.getChannelD() != null) {
				preSaleOrder.setChannelId(preSaleOrderDTO.getChannelD());
			}
			if (preSaleOrderDTO.getOrgId() != null) {
				preSaleOrder.setOrgId(preSaleOrderDTO.getOrgId());
			}
			preSaleOrder.setOrderFrom(preSaleOrderDTO.getOrderFrom());
			if (preSaleOrderDTO.getExtCustOrderId() != null) {
				preSaleOrder.setExtCustOrderId(preSaleOrderDTO.getExtCustOrderId());
			}
			preSaleOrder.setIfEnd(preSaleOrderDTO.getIfEnd());
			if (preSaleOrderDTO.getSeq() != null) {
				preSaleOrder.setSeq(preSaleOrderDTO.getSeq());
			}
			preSaleOrder.setPreOrderSrc(preSaleOrderDTO.getPreOrderSrc());
			preSaleOrder.setProdType(preSaleOrderDTO.getProdType());
			preSaleOrder.setActionType(preSaleOrderDTO.getActionType());
			preSaleOrder.setServiceType(preSaleOrderDTO.getServiceType());
			preSaleOrder.setStatusCd(preSaleOrderDTO.getStatusCd());
			if (preSaleOrderDTO.getAreaId() != null) {
				preSaleOrder.setAreaId(preSaleOrderDTO.getAreaId());
			}
			if (preSaleOrderDTO.getRegionCd() != null) {
				preSaleOrder.setRegionCd(preSaleOrderDTO.getRegionCd());
			}
			if (preSaleOrderDTO.getBatchAmount() != null) {
				preSaleOrder.setBatchAmount(preSaleOrderDTO.getBatchAmount() + "");
			}
			preSaleOrder.setDevStaff(preSaleOrderDTO.getDevStaff());
			preSaleOrder.setDevTeam(preSaleOrderDTO.getDevTeam());
		}
	}

	@Override
	public String preSaleOrderDeal(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.presaleorderdeal.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.presaleorderdeal.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.presaleorderdeal.OutParam _outParam = new com.ffcs.crmd.cas.bean.casbean.presaleorderdeal.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error _error = null;

		root = (com.ffcs.crmd.cas.bean.casbean.presaleorderdeal.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.presaleorderdeal.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();
		boolean isNeedPay = "1".equals(_inParam.getIsNeedPay()) ? true : false;
		Long custOrderId = _inParam.getCustOrderId();
		PreSaleOrderDealInDTO inDTO = new PreSaleOrderDealInDTO();
		CrmBeanUtils.applyIf(inDTO, _inParam.getPreSaleOrder());
		RetVo retVo = preSaleOrderFacade.preSaleOrderDeal(isNeedPay, custOrderId, inDTO);
		if (retVo != null && retVo.getResult()) {
			_outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
		} else {
			_outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			_error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			_error.setId("11");
			_error.setMessage(retVo.getMsgTitle());
			_outParam.setError(_error);
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.presaleorderdeal.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	@Override
	public String preOrderCommitCheck(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.preordercommitcheck.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.preordercommitcheck.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.preordercommitcheck.OutParam _outParam = new com.ffcs.crmd.cas.bean.casbean.preordercommitcheck.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error _error = null;

		root = (com.ffcs.crmd.cas.bean.casbean.preordercommitcheck.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.preordercommitcheck.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();
		Long custOrderId = _inParam.getCustOrderId();
		Long areaId = _inParam.getAreaId();
		Map map = new HashMap();
		map.put("extCustOrderId", custOrderId);
		map.put("qryType", "1");
		RetVo retVo = preSaleOrderFacade.queryPreSaleOrder(map);
		if (retVo != null && retVo.getResult()) {
			_outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
			PreSaleOrderDTO preSaleOrderDTO = (PreSaleOrderDTO) retVo.getObject();
			com.ffcs.crmd.cas.bean.casbean.presaleorder.PreSaleOrder preSaleOrder = new com.ffcs.crmd.cas.bean.casbean.presaleorder.PreSaleOrder();
			CrmBeanUtils.applyIf(preSaleOrder, preSaleOrderDTO);
			_outParam.setPreSaleOrder(preSaleOrder);
			boolean isAllow = CrmClassUtils.isFixedAttrValue("ComDomainValue", "allowTwoEndOrderArea",
					StringUtils.strnull(areaId));
			_outParam.setIsAllow("0");
			if (isAllow) {
				_outParam.setIsAllow("1");
			}
			String extCustOrderIds = preSaleOrderFacade.getExtCustOrderIds(preSaleOrderDTO.getPreOrderNumber());
			_outParam.setExtCustOrderIds(extCustOrderIds);
		} else {
			_outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			_error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			_error.setId("11");
			_error.setMessage(retVo.getMsgTitle());
			_outParam.setError(_error);
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.presaleorderdeal.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	@Override
	public String preSaleOrderCommit(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.presaleordercommit.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.presaleordercommit.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.presaleordercommit.OutParam _outParam = new com.ffcs.crmd.cas.bean.casbean.presaleordercommit.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error _error = null;

		root = (com.ffcs.crmd.cas.bean.casbean.presaleordercommit.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.presaleordercommit.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();

		RetVo retVo = preSaleOrderFacade.preSaleOrderCommit(_inParam);
		if (retVo != null && retVo.getResult()) {
			_outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
			if ("true".equals(retVo.getRetCode())) {

			}
		} else {
			_outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			_error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			_error.setId("12");
			_error.setMessage(retVo.getMsgTitle());
			_outParam.setError(_error);
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.presaleordercommit.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	@Override
	public String preSaleOrderPoolCommit(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.presaleorderpoolcommit.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.presaleorderpoolcommit.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.presaleorderpoolcommit.OutParam _outParam = new com.ffcs.crmd.cas.bean.casbean.presaleorderpoolcommit.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error _error = null;

		root = (com.ffcs.crmd.cas.bean.casbean.presaleorderpoolcommit.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.presaleorderpoolcommit.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();
		com.ffcs.crmd.cas.bean.casbean.presaleorder.PreSaleOrderPool preSaleOrderPool = _inParam.getPreSaleOrderPool();
		PreSaleOrderPoolInDTO inDTO = new PreSaleOrderPoolInDTO();
		CrmBeanUtils.applyIf(inDTO, preSaleOrderPool);
		RetVo retVo = preSaleOrderFacade.savePreSaleOrderPool(inDTO);
		if (retVo != null && retVo.getResult()) {
			_outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
		} else {
			_outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			_error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			_error.setId("11");
			_error.setMessage(retVo.getMsgTitle());
			_outParam.setError(_error);
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.presaleorderpoolcommit.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	@Override
	public String intfPreOrderCommit(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.intfpreordercommit.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.intfpreordercommit.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.intfpreordercommit.OutParam _outParam = new com.ffcs.crmd.cas.bean.casbean.intfpreordercommit.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error _error = null;

		root = (com.ffcs.crmd.cas.bean.casbean.intfpreordercommit.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.intfpreordercommit.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();
		com.ffcs.crmd.cas.bean.casbean.intfpreordercommit.IntfPreOrder[] intfP = _inParam.getIntfPreOrder();
		List<IntfPreOrderInDTO> inDtos = new ArrayList<IntfPreOrderInDTO>();
		if (intfP != null && intfP.length > 0) {
			for (com.ffcs.crmd.cas.bean.casbean.intfpreordercommit.IntfPreOrder intfPreOrder : intfP) {
				IntfPreOrderInDTO inDto = new IntfPreOrderInDTO();
				CrmBeanUtils.applyIf(inDto, intfPreOrder);
				inDtos.add(inDto);
			}
		}
		RetVo retVo = preSaleOrderFacade.saveIntfPreOrder(inDtos);
		if (retVo != null && retVo.getResult()) {
			_outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
		} else {
			_outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			_error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			_error.setId("11");
			_error.setMessage(retVo.getMsgTitle());
			_outParam.setError(_error);
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.intfpreordercommit.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	@Override
	public String queryPreOrderFlow(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.query.querypreorderflow.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.query.querypreorderflow.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.query.querypreorderflow.OutParam _outParam = new com.ffcs.crmd.cas.bean.casbean.query.querypreorderflow.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error _error = null;

		root = (com.ffcs.crmd.cas.bean.casbean.query.querypreorderflow.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.query.querypreorderflow.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();

		RetVo retVo = preSaleOrderFacade.queryPreOrderFlow(_inParam.getPreOrderNumber());
		if (retVo != null && retVo.getResult()) {
			_outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
			List<QueryPreOrderFlowOutDTO> outs = (List<QueryPreOrderFlowOutDTO>) retVo.getObject();
			if (outs != null && outs.size() > 0) {
				for (QueryPreOrderFlowOutDTO out : outs) {
					FlowInfo flowInfo = new FlowInfo();
					CrmBeanUtils.applyIf(flowInfo, out);
					_outParam.addFlowInfo(flowInfo);
				}
			}
		} else {
			_outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			_error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			_error.setMessage(retVo.getMsgTitle());
			_error.setId(retVo.getRetCode());
			_outParam.setError(_error);
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.query.querypreorderflow.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	@Override
	public String queryPreOrderInfo(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.query.querypreorderinfo.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.query.querypreorderinfo.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.query.querypreorderinfo.OutParam _outParam = new com.ffcs.crmd.cas.bean.casbean.query.querypreorderinfo.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error _error = null;

		root = (com.ffcs.crmd.cas.bean.casbean.query.querypreorderinfo.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.query.querypreorderinfo.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();

		RetVo retVo = preSaleOrderFacade.queryPreOrderInfo(_inParam.getPreOrderNumber());
		if (retVo != null && retVo.getResult()) {
			_outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
			QueryPreOrderInfoOutDTO outDTO = (QueryPreOrderInfoOutDTO) retVo.getObject();
			com.ffcs.crmd.cas.bean.casbean.query.querypreorderinfo.ReceiveStaff receiveStaff = new com.ffcs.crmd.cas.bean.casbean.query.querypreorderinfo.ReceiveStaff();
			if (outDTO != null) {
				CrmBeanUtils.applyIf(receiveStaff, outDTO);
			}
			_outParam.setReceiveStaff(receiveStaff);
		} else {
			_outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			_error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			_error.setMessage(retVo.getMsgTitle());
			_error.setId(retVo.getRetCode());
			_outParam.setError(_error);
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.query.querypreorderinfo.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	@Override
	public String queryPreOrderForOrder(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.query.querypreorderfororder.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.query.querypreorderfororder.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.query.querypreorderfororder.OutParam _outParam = new com.ffcs.crmd.cas.bean.casbean.query.querypreorderfororder.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error _error = null;

		root = (com.ffcs.crmd.cas.bean.casbean.query.querypreorderfororder.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.query.querypreorderfororder.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();
		RetVo retVo = preSaleOrderFacade.queryPreOrderForOrder(_inParam.getExtCustOrderId());
		if (retVo != null && retVo.getResult()) {
			_outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
			com.ffcs.crmd.cas.bean.casbean.query.querypreorderfororder.PreSaleOrderInfo preSaleOrderInfo = new com.ffcs.crmd.cas.bean.casbean.query.querypreorderfororder.PreSaleOrderInfo();
			PreSaleOrderInfoOutDTO outDTO = (PreSaleOrderInfoOutDTO) retVo.getObject();
			CrmBeanUtils.applyIf(preSaleOrderInfo, outDTO);
			_outParam.setPreSaleOrderInfo(preSaleOrderInfo);
		} else {
			_outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			_error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			_error.setMessage(retVo.getMsgTitle());
			_error.setId(retVo.getRetCode());
			_outParam.setError(_error);
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.query.querypreorderfororder.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	/**
	 * 根据销售员工/销售团队查询回退单列表
	 *
	 * @author YULIYAO 2016/4/6 return
	 */
	@Override
	public String queryReturnList(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.query.queryReturnList.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.query.queryReturnList.InParam inParam = null;
		com.ffcs.crmd.cas.bean.casbean.query.queryReturnList.OutParam outParam = new com.ffcs.crmd.cas.bean.casbean.query.queryReturnList.OutParam();
		root = (com.ffcs.crmd.cas.bean.casbean.query.queryReturnList.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.query.queryReturnList.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		inParam = root.getMsgBody().getInParam();
		PageInfo pageInfo = null;
		outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
		outParam.setPreOrderNum(0);
		try {
			pageInfo = preSaleOrderFacade.queryReturnList(NumberUtils.toLong(inParam.getSaleOrgId()),
					NumberUtils.toLong(inParam.getSaleStaffId()), inParam.getCurrentPage(), inParam.getPerPageNum());

			if (pageInfo != null && CollectionUtils.isNotEmpty(pageInfo.getList())) {
				List<PreSaleOrderDTO> preSaleOrderDTOs = pageInfo.getList();
				for (PreSaleOrderDTO preSaleOrderDTO : preSaleOrderDTOs) {
					com.ffcs.crmd.cas.bean.casbean.query.queryReturnList.ReturnInfo returnInfo = new com.ffcs.crmd.cas.bean.casbean.query.queryReturnList.ReturnInfo();
					returnInfo.setPreOrderNumber(preSaleOrderDTO.getPreOrderNumber());
					returnInfo.setPreStatusCd(preSaleOrderDTO.getStatusCd());
					// 传custOrderId给crm，由Crm查询出编码
					if (!StringUtils.isNullOrEmpty(preSaleOrderDTO.getExtCustOrderId())) {
						if (preSaleOrderDTO.getSeq() == 1) {
							returnInfo.setCustSoNumber(preSaleOrderDTO.getExtCustOrderId() + "");
						} else {
							PreSaleOrderDTO firstPreSaleOrder = preSaleOrderFacade
									.getFirstPreSaleOrder(preSaleOrderDTO.getPreOrderNumber());
							if (firstPreSaleOrder != null) {
								returnInfo.setCustSoNumber(firstPreSaleOrder.getExtCustOrderId() + "");
							}

						}
					}
					// 获取工单池
					PreSaleOrderPoolDTO preSaleOrderPoolDTO = preSaleOrderPoolFacade
							.getByPreOrderId(preSaleOrderDTO.getPreOrderId());
					if (preSaleOrderPoolDTO != null) {
						returnInfo.setStatusCd(preSaleOrderPoolDTO.getStatusCd());
						returnInfo.setAcceptDate(DateUtils.date2Str(preSaleOrderPoolDTO.getAcceptDate()));
						returnInfo.setReturnRemark(preSaleOrderPoolDTO.getRemark());
					}
					// 获取退单属性
					PreSaleOrderProcAttrDTO preSaleOrderProcAttrDTO = preSaleOrderProcFacade
							.getLastBackProcAttr(preSaleOrderDTO.getPreOrderId());

					if (preSaleOrderProcAttrDTO != null) {
						returnInfo.setReturnDate(DateUtils.date2Str(preSaleOrderProcAttrDTO.getCreateDate()));
						PreSaleOrderProcDTO preSaleOrderProcDTO = preSaleOrderProcFacade
								.getById(preSaleOrderProcAttrDTO.getPreSaleOrderProcId());
						returnInfo.setReturnStaffId(preSaleOrderProcDTO.getStaffId() + "");
						StaffDTO staffDTO = casSysFacade.getStaffById(preSaleOrderProcDTO.getStaffId());
						if (staffDTO != null) {
							returnInfo.setReturnStaffCode(staffDTO.getStaffCode());
							returnInfo.setReturnStaffName(staffDTO.getStaffName());
						}
						returnInfo.setReturnType(preSaleOrderProcAttrDTO.getAttrValue());
						returnInfo.setReturnReason(preSaleOrderProcAttrDTO.getAttrDesc());
					}
					outParam.addReturnInfo(returnInfo);
				}
				outParam.setPreOrderNum(pageInfo.getList().size());
			}
		} catch (Exception e) {
			outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			com.ffcs.crmd.cas.bean.casbean.comm.Error error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			error.setMessage(e.getMessage());
			outParam.setError(error);
		}
		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.query.queryReturnList.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), outParam);
		return result;
	}

	/**
	 * 获取报文头。
	 *
	 * @param inXml
	 * @return
	 */
	public com.ffcs.crmd.cas.bean.casbean.comm.MsgHead getRespMsgHead(String inXml, String from) {
		String msgType = WsUtil.getXmlContent(inXml, "msgType");
		String serial = WsUtil.getXmlContent(inXml, "serial");
		String sysSign = WsUtil.getXmlContent(inXml, "sysSign");
		final com.ffcs.crmd.cas.bean.casbean.comm.MsgHead head = new com.ffcs.crmd.cas.bean.casbean.comm.MsgHead();
		head.setFrom("CAS");
		head.setTo(from);
		head.setSysSign(sysSign);
		head.setSerial(serial);
		head.setMsgType(msgType);
		final String time = DateUtils.date2Str(new Date(), "yyyyMMddHHmmss");
		head.setTime(time);
		return head;
	}

	@Override
	public String queryPreSaleOrder(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.query.queryPreSaleOrder.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.query.queryPreSaleOrder.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.query.queryPreSaleOrder.OutParam _outParam = new com.ffcs.crmd.cas.bean.casbean.query.queryPreSaleOrder.OutParam();
		
		try {
			root = (com.ffcs.crmd.cas.bean.casbean.query.queryPreSaleOrder.Root) Unmarshaller.unmarshal(
					com.ffcs.crmd.cas.bean.casbean.query.queryPreSaleOrder.Root.class,
					new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
			_inParam = root.getMsgBody().getInParam();
			String preOrderNumber = _inParam.getPreOrderNumber();
			RetVo retVo = preSaleOrderFacade.queryPreSaleOrder(preOrderNumber);
			if (retVo != null && retVo.getResult()) {
				_outParam.setResult("0");
				if (retVo.getObject() != null) {
					QueryPreSaleOrderOutDTO outDTO = (QueryPreSaleOrderOutDTO) retVo.getObject();
					CrmBeanUtils.applyIf(_outParam, outDTO);
				}
			} else {
				_outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
				com.ffcs.crmd.cas.bean.casbean.comm.Error _error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
				_error.setMessage(retVo.getMsgTitle());
				_error.setId(retVo.getRetCode());
				_outParam.setError(_error);
			}
		} catch (Exception e) {
			_outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			com.ffcs.crmd.cas.bean.casbean.comm.Error _error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			_error.setId("11");
			_error.setMessage(e.getMessage());
			_outParam.setError(_error);
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.query.queryPreSaleOrder.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	@Override
	public String preSaleOrderCount(String inXml) {
		try {
			com.ffcs.crmd.cas.bean.casbean.query.preSaleOrderCount.Root input = (com.ffcs.crmd.cas.bean.casbean.query.preSaleOrderCount.Root) Unmarshaller
					.unmarshal(com.ffcs.crmd.cas.bean.casbean.query.preSaleOrderCount.Root.class,
							new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
			com.ffcs.crmd.cas.bean.casbean.query.preSaleOrderCount.Root output = new com.ffcs.crmd.cas.bean.casbean.query.preSaleOrderCount.Root();
			output.setMsgHead(input.getMsgHead());
			com.ffcs.crmd.cas.bean.casbean.query.preSaleOrderCount.MsgBody msgBody = new com.ffcs.crmd.cas.bean.casbean.query.preSaleOrderCount.MsgBody();
			// 获取输出
			msgBody.setOutParam(this.getPreSaleOrderCount(input.getMsgBody().getInParam()));
			output.setMsgBody(msgBody);
			final Writer w = new java.io.StringWriter();
			org.exolab.castor.xml.Marshaller.marshal(output, w);
			return ToGb2312.delNameSpace(w.toString());
		} catch (Exception e) {
			throw new RtManagerException(
					WsUtil.getOutXmlByException(inXml, 1, IntfConstant.PRE_SALE_ORDER_COUNT_FAIL.getValue(), e));
		}
	}

	/**
	 * 根据销售员工/销售团队和日期范围查询预受理单统计及清单.
	 *
	 * @param inParam
	 * @return
	 * @author zhangyangyi 2016-2-29 zhangyangyi
	 */
	private com.ffcs.crmd.cas.bean.casbean.query.preSaleOrderCount.OutParam getPreSaleOrderCount(
			com.ffcs.crmd.cas.bean.casbean.query.preSaleOrderCount.InParam inParam) {
		com.ffcs.crmd.cas.bean.casbean.query.preSaleOrderCount.OutParam outParam = new com.ffcs.crmd.cas.bean.casbean.query.preSaleOrderCount.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
		outParam.setResult("0");
		int number = 0;
		if (StringUtils.isNullOrEmpty(inParam.getBeginDate()) || StringUtils.isNullOrEmpty(inParam.getEndDate())) {
			outParam.setResult("1");
			error.setId(IntfConstant.ERROR_ID_ABNORMAL_CONNTENT.getValue());
			error.setMessage("开始时间和结束时间不能为空");
			outParam.setError(error);
			return outParam;
		}
		if (StringUtils.isNullOrEmpty(inParam.getSaleStaffId()) && StringUtils.isNullOrEmpty(inParam.getSaleOrgId())) {
			outParam.setResult("1");
			error.setId(IntfConstant.ERROR_ID_ABNORMAL_CONNTENT.getValue());
			error.setMessage("销售员工ID或销售团队ID需要至少一个非空");
			outParam.setError(error);
			return outParam;
		}
		// 是否存在状态参数
		if (!StringUtils.isNullOrEmpty(inParam.getStatusCd())) {
			PageInfo pageInfo = preSaleOrderFacade.getPreOrderByDevStaffAndDevTeam(inParam.getSaleStaffId(),
					inParam.getSaleOrgId(), inParam.getStatusCd(), inParam.getBeginDate(), inParam.getEndDate(),
					inParam.getCurrentPage(), inParam.getPerPageNum());
			List<PreSaleOrderDTO> list = pageInfo.getList();
			number = NumberUtils.toInt(pageInfo.getTotal());
			if (list != null && list.size() > 0) {
				for (PreSaleOrderDTO mobiDto : list) {
					if (mobiDto.getPreOrderId() != null) {
						PreSaleOrderDTO preSaleOrderDTO = preSaleOrderFacade.getById(mobiDto.getPreOrderId(),mobiDto.getShardingId());
						if (preSaleOrderDTO != null) {
							com.ffcs.crmd.cas.bean.casbean.query.preSaleOrderCount.OrderCountInfo vOrderCountInfo = new com.ffcs.crmd.cas.bean.casbean.query.preSaleOrderCount.OrderCountInfo();
							vOrderCountInfo.setStatusCd(preSaleOrderDTO.getStatusCd());
							vOrderCountInfo.setCustSoNumber(preSaleOrderDTO.getCustSoNumber());
							vOrderCountInfo.setPreOrderNumber(preSaleOrderDTO.getPreOrderNumber());
							vOrderCountInfo.setSaleDate(DateUtils.date2Str(preSaleOrderDTO.getCreateDate()));
							PreSaleOrderPoolDTO preSaleOrderPoolDTO = preSaleOrderPoolFacade
									.getByPreOrderId(preSaleOrderDTO.getPreOrderId());
							if (preSaleOrderPoolDTO != null) {
								vOrderCountInfo.setAcceptDate(DateUtils.date2Str(preSaleOrderPoolDTO.getAcceptDate()));
							} else {
								vOrderCountInfo.setAcceptDate(DateUtils.date2Str(preSaleOrderDTO.getAcceptTime()));
							}
							outParam.addOrderCountInfo(vOrderCountInfo);
						}
					} else {
						outParam.setResult("1");
						error.setId(IntfConstant.ERROR_ID_ABNORMAL_CONNTENT.getValue());
						error.setMessage("preSaleOrderId为空");
						outParam.setError(error);
						return outParam;
					}
				}
			}

			// 因为不能union all 所以使用加减的方式来查询剩余的数据
			if (number < inParam.getPerPageNum()) {
				inParam.setPerPageNum(inParam.getPerPageNum() - number);
				// 查询历史
				PageInfo hisPageInfo = preSaleOrderFacade.getHisPreOrderByDevStaffAndDevTeam(inParam.getSaleStaffId(),
						inParam.getSaleOrgId(), inParam.getStatusCd(), inParam.getBeginDate(), inParam.getEndDate(),
						inParam.getCurrentPage(), inParam.getPerPageNum());
				List<PreSaleOrderHisDTO> hisList = hisPageInfo.getList();
				if (hisList != null && hisList.size() > 0) {
					for (PreSaleOrderHisDTO hisDTO : hisList) {
						if (hisDTO.getPreOrderId() != null) {
							number++;
							PreSaleOrderHisDTO preSaleOrderHisDTO = preSaleOrderFacade
									.getHisById(hisDTO.getPreOrderId());
							if (preSaleOrderHisDTO.getPreOrderId() != null) {
								com.ffcs.crmd.cas.bean.casbean.query.preSaleOrderCount.OrderCountInfo vOrderCountInfo = new com.ffcs.crmd.cas.bean.casbean.query.preSaleOrderCount.OrderCountInfo();
								vOrderCountInfo.setStatusCd(preSaleOrderHisDTO.getStatusCd());
								vOrderCountInfo.setCustSoNumber(preSaleOrderHisDTO.getCustSoNumber());
								vOrderCountInfo.setPreOrderNumber(preSaleOrderHisDTO.getPreOrderNumber());
								vOrderCountInfo.setSaleDate(DateUtils.date2Str(preSaleOrderHisDTO.getCreateDate()));
								PreSaleOrderPoolHisDTO preSaleOrderPoolHisDTO = preSaleOrderPoolFacade
										.getHisPoolByPreSaleId(preSaleOrderHisDTO.getPreOrderId());
								if (preSaleOrderPoolHisDTO.getPreOrderId() != null) {
									vOrderCountInfo
											.setAcceptDate(DateUtils.date2Str(preSaleOrderPoolHisDTO.getAcceptDate()));
								} else {
									vOrderCountInfo
											.setAcceptDate(DateUtils.date2Str(preSaleOrderHisDTO.getAcceptTime()));
								}
								outParam.addOrderCountInfo(vOrderCountInfo);
							}
						} else {
							outParam.setResult("1");
							error.setId(IntfConstant.ERROR_ID_ABNORMAL_CONNTENT.getValue());
							error.setMessage("preSaleOrderId为空");
							outParam.setError(error);
							return outParam;
						}
					}
				}
			}
			outParam.setPreOrderNum(StringUtils.strnull(number));

		} else {
			Map<String, Object> maps = preSaleOrderFacade.getPreOrderStatusAndStatusNumber(inParam.getSaleStaffId(),
					inParam.getSaleOrgId(), inParam.getBeginDate(), inParam.getEndDate());
			if (maps != null && maps.size() > 0) {
				for (Map.Entry<String, Object> map : maps.entrySet()) {
					String statusCd = StringUtils.strnull(map.getKey());
					String preOrderNum = StringUtils.strnull(map.getValue());
					OrderNumForStatusCd orderCd = new OrderNumForStatusCd();
					orderCd.setStatusCd(statusCd);
					orderCd.setPreOrderNum(preOrderNum);
					outParam.addOrderNumForStatusCd(orderCd);
				}
			}
		}

		return outParam;
	}

	@Override
	public String cancelOrderQuery(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.cancel.cancelorderquery.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.cancel.cancelorderquery.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.cancel.cancelorderquery.OutParam _outParam = new com.ffcs.crmd.cas.bean.casbean.cancel.cancelorderquery.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error _error = null;

		root = (com.ffcs.crmd.cas.bean.casbean.cancel.cancelorderquery.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.cancel.cancelorderquery.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();
		Long orgId = NumberUtils.nullToLongZero(_inParam.getTeamId());
		Long staffPositionId = NumberUtils.nullToLongZero(_inParam.getStaffPositionId());
		String preOrderNumber = StringUtils.strnull(_inParam.getPreCustSoNum());
		Long landId = NumberUtils.nullToLongZero(_inParam.getLatnId());

		RetVo retVO = preSaleOrderFacade.validateInParam(orgId, staffPositionId, preOrderNumber);
		if (!retVO.getResult()) {
			throw new Exception(retVO.getMsgTitle());
		}

		RetVo retVo = preSaleOrderFacade.buildPreOrder(landId, orgId, staffPositionId, preOrderNumber);
		if (!retVo.getResult()) {
			throw new Exception(retVO.getMsgTitle());
		}
		_outParam.setPreOrder((com.ffcs.crmd.cas.bean.casbean.cancel.cancelorderquery.PreOrder) retVo.getObject());

		String extCustOrderIds = preSaleOrderFacade.getExtCustOrderIds(preOrderNumber);
		if (!StringUtils.isNullOrEmpty(extCustOrderIds)) {
			_outParam.setExtCustOrderIds(extCustOrderIds);
		}
		_outParam.setResult("0");
		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.cancel.cancelorderquery.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}
	@Override
	public String returnToPreSale(String inXml) throws Exception {
		String result = "";
		com.ffcs.crmd.cas.bean.casbean.save.returntopresale.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.save.returntopresale.InParam _inParam = null;
		com.ffcs.crmd.cas.bean.casbean.save.returntopresale.OutParam _outParam = new com.ffcs.crmd.cas.bean.casbean.save.returntopresale.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error _error = null;

		root = (com.ffcs.crmd.cas.bean.casbean.save.returntopresale.Root) Unmarshaller.unmarshal(
				com.ffcs.crmd.cas.bean.casbean.save.returntopresale.Root.class,
				new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		_inParam = root.getMsgBody().getInParam();
		
		String preOrderNumber = _inParam.getPreOrderNumber();
		Long handleStaffId = NumberUtils.nullToLongZero(_inParam.getHandleStaffId());
		String reason = _inParam.getReason();
		String srcFlag = _inParam.getSrcFlag();
		String reasonType = _inParam.getReasonType();

		RetVo retVo = preSaleOrderFacade.returnToPreSale(preOrderNumber, handleStaffId, reason, srcFlag, reasonType);
		
		if (retVo != null && retVo.getResult()) {
			_outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
		} else {
			_outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			_error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			_error.setMessage("回退失败！" + retVo.getMsgTitle());
			_error.setId("11");
			_outParam.setError(_error);
		}

		result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.save.returntopresale.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), _outParam);
		return result;
	}

	@Override
	public String queryintAssignOrg(String inXml) throws Exception {
		com.ffcs.crmd.cas.bean.casbean.query.qureryintassignorg.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.query.qureryintassignorg.OutParam outParam = new com.ffcs.crmd.cas.bean.casbean.query.qureryintassignorg.OutParam();
		outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
		root = (Root) Unmarshaller
				.unmarshal(Root.class,new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
		String execusteOrgId = root.getMsgBody().getInParam().getExecuteOrgId();
		String regionCds = root.getMsgBody().getInParam().getRegionCds();
		String[] regionArrays = regionCds.split(",");
		List<String> regionLists = new ArrayList<>();
		if (ArrayUtils.isNotEmpty(regionArrays)) {
			regionLists = Arrays.asList(regionArrays);
			List<InteractionAssignOrgDTO> orgDTOs = assignOrgFacade
					.queryAssignOrgIntf(NumberUtils.toLong(execusteOrgId), regionLists);
			List<com.ffcs.crmd.cas.bean.casbean.query.qureryintassignorg.ReturnInfo> returnInfos = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(orgDTOs)) {
				for (InteractionAssignOrgDTO orgDTO : orgDTOs) {
					com.ffcs.crmd.cas.bean.casbean.query.qureryintassignorg.ReturnInfo returnInfo =
							new com.ffcs.crmd.cas.bean.casbean.query.qureryintassignorg.ReturnInfo();
					returnInfo.setOrgId(orgDTO.getOrgId());
					returnInfo.setCommonReginId(orgDTO.getCommonRegionId());
					returnInfo.setOrgName(orgDTO.getOrgName());
					outParam.addReturnInfo(returnInfo);
				}
			}
		}
		String result = WsUtil
				.toXml(com.ffcs.crmd.cas.bean.casbean.query.qureryintassignorg.Root.class,
						getRespMsgHead(inXml, root.getMsgHead().getFrom()),outParam);
		return result;
	}
	@Override
	public String checkCouldCancel(String inXml) throws Exception {
		com.ffcs.crmd.cas.bean.casbean.checkcouldcancel.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.checkcouldcancel.OutParam outParam = new com.ffcs.crmd.cas.bean.casbean.checkcouldcancel.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error error = null;
		outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
		try {
			root = (com.ffcs.crmd.cas.bean.casbean.checkcouldcancel.Root) Unmarshaller.unmarshal(
					com.ffcs.crmd.cas.bean.casbean.checkcouldcancel.Root.class,
					new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
			
			com.ffcs.crmd.cas.bean.casbean.checkcouldcancel.InParam inParam = root.getMsgBody().getInParam();
			
			String preOrderNumber = inParam.getPreOrderNumber();
			String orgIdStr = inParam.getOrgId();
			
			RetVo retVo = preSaleOrderFacade.checkCouldCancelForIntf(preOrderNumber, orgIdStr);
			
			if (retVo != null && retVo.getResult() && retVo.getObject() != null) {
				
				outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
				
				PreSaleOrderDTO orderDto = (PreSaleOrderDTO) retVo.getObject();
				com.ffcs.crmd.cas.bean.casbean.checkcouldcancel.PreOrderInfo preOrderInfo = new com.ffcs.crmd.cas.bean.casbean.checkcouldcancel.PreOrderInfo();
				outParam.setPreOrderInfo(preOrderInfo);
				preOrderInfo.setPreOrderNumber(orderDto.getPreOrderNumber());
				preOrderInfo.setOrgId(orderDto.getOrgId() + "");
				preOrderInfo.setStaffId(orderDto.getStaffId() + "");
				preOrderInfo.setStatusCd(orderDto.getStatusCd());
				if (!StringUtils.isNullOrEmpty(orderDto.getPreOrderNumber())) {
					String extCustOrderIds = preSaleOrderFacade.getExtCustOrderIds(orderDto.getPreOrderNumber());
					preOrderInfo.setExtCustOrderIds(extCustOrderIds);
				}
			} else {
				outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
				error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
				error.setId("1");
				error.setMessage(retVo.getMsgTitle());
				outParam.setError(error);
			}
			
		} catch (Exception e) {
			outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			error.setId("4");
			error.setMessage(e.getMessage());
			outParam.setError(error);
		}
		String result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.checkcouldcancel.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), outParam);
		return result;
	}
	@Override
	public String cancelPreOrder(String inXml) throws Exception {
		com.ffcs.crmd.cas.bean.casbean.cancelpreorder.Root root = null;
		com.ffcs.crmd.cas.bean.casbean.cancelpreorder.OutParam outParam = new com.ffcs.crmd.cas.bean.casbean.cancelpreorder.OutParam();
		com.ffcs.crmd.cas.bean.casbean.comm.Error error = null;
		outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
		
		try {
			root = (com.ffcs.crmd.cas.bean.casbean.cancelpreorder.Root) Unmarshaller.unmarshal(
					com.ffcs.crmd.cas.bean.casbean.cancelpreorder.Root.class,
					new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
			
			com.ffcs.crmd.cas.bean.casbean.cancelpreorder.InParam inParam = root.getMsgBody().getInParam();
			CancelPreOrderVo vo = new CancelPreOrderVo();
			vo.setPreOrderNumber(inParam.getPreOrderNumber());
			vo.setCancelFlag(inParam.getCancelFlag());
			vo.setCancelReason(inParam.getCancelReason());
			if (!StringUtils.isNullOrEmpty(inParam.getStaffId())) {
				vo.setStaffId(NumberUtils.nullToLongZero(inParam.getStaffId()));
			}
			if (!StringUtils.isNullOrEmpty(inParam.getOrgId())) {
				vo.setOrgId(NumberUtils.nullToLongZero(inParam.getOrgId()));
			}
			if (!StringUtils.isNullOrEmpty(inParam.getStaffPositionId())) {
				vo.setStaffPositionId(NumberUtils.nullToLongZero(inParam.getStaffPositionId()));
			}
			vo.setAmount(NumberUtils.nullToLongZero(inParam.getAmount()));
			
			RetVo retVo = preSaleOrderFacade.cancelPreOrder(vo);
			
			if (retVo == null || !retVo.getResult()) {
				outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
				error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
				error.setId("1");
				error.setMessage(retVo.getDetailMsg());
				outParam.setError(error);
			}
			
			
		} catch (Exception e) {
			outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			error = new com.ffcs.crmd.cas.bean.casbean.comm.Error();
			error.setId("4");
			error.setMessage(e.getMessage());
			outParam.setError(error);
		}
		
		String result = WsUtil.toXml(com.ffcs.crmd.cas.bean.casbean.cancelpreorder.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), outParam);
		return result;
	}

	@Override
	public String qryPreSaleOrderProc(String inXml) throws Exception {
		com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderProc.Root root = null;
		com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderProc.OutParam outParam = new com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderProc.OutParam();
		com.ffcs.crmd.cas.bean.devops.comm.Error error = null;
		outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
		try {
			root = (com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderProc.Root) Unmarshaller.unmarshal(
					com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderProc.Root.class,
					new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
			com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderProc.InParam inParam = root.getMsgBody().getInParam();
			RetVo retVo = new RetVo();
			List<com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderProc.PreSaleOrderProc> preSaleOrderProcs = null;
			if(inParam != null){
				retVo = preSaleOrderProcFacade.queryPreSaleOrderProcForIntf(inParam);
			}
			if(retVo != null && retVo.getResult()){
				preSaleOrderProcs = (List<PreSaleOrderProc>) retVo.getObject();
			}
			
			if(preSaleOrderProcs != null && preSaleOrderProcs.size()>0){
				outParam.setPreSaleOrderProc(preSaleOrderProcs.toArray(new PreSaleOrderProc[preSaleOrderProcs.size()]));
			}else{
				outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
				error = new com.ffcs.crmd.cas.bean.devops.comm.Error();
				error.setId("1");
				error.setMessage("未查询到任何信息");
				outParam.setError(error);
			}
		} catch (Exception e) {
			outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			error = new com.ffcs.crmd.cas.bean.devops.comm.Error();
			error.setId("4");
			error.setMessage(e.getMessage());
			outParam.setError(error);
		}
		String result = WsUtil.toXml(com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderProc.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), outParam);
		return result;
	}

	@Override
	public String qryPreSaleOrderOverTimeList(String inXml) throws Exception {
		com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderOverTimeList.Root root = null;
		com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderOverTimeList.OutParam outParam = new com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderOverTimeList.OutParam();
		com.ffcs.crmd.cas.bean.devops.comm.Error error = null;
		outParam.setResult(IntfConstant.RESULT_TRUE.getValue());
		try {
			root = (com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderOverTimeList.Root) Unmarshaller.unmarshal(
					com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderOverTimeList.Root.class,
					new InputSource(new ByteArrayInputStream(inXml.getBytes("utf-8"))));
			com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderOverTimeList.InParam inParam = root.getMsgBody().getInParam();
			RetVo retVo = new RetVo();
			List<com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderOverTimeList.PreSaleOrderDetail> preSaleOrderDetails = null;
			if(inParam != null){
				retVo = preSaleOrderPoolFacade.qryPreSaleOrderOverTimeList(inParam);
			}
			if(retVo != null && retVo.getResult()){
				preSaleOrderDetails = (List<PreSaleOrderDetail>) retVo.getObject();
			}
			
			if(preSaleOrderDetails != null && preSaleOrderDetails.size()>0){
				outParam.setPreSaleOrderDetail(preSaleOrderDetails.toArray(new PreSaleOrderDetail[preSaleOrderDetails.size()]));
			}else{
				outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
				error = new com.ffcs.crmd.cas.bean.devops.comm.Error();
				error.setId("1");
				error.setMessage("未查询到任何信息");
				outParam.setError(error);
			}
		} catch (Exception e) {
			outParam.setResult(IntfConstant.RESULT_FALSE.getValue());
			error = new com.ffcs.crmd.cas.bean.devops.comm.Error();
			error.setId("4");
			error.setMessage(e.getMessage());
			outParam.setError(error);
		}
		String result = WsUtil.toXml(com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderOverTimeList.Root.class,
				getRespMsgHead(inXml, root.getMsgHead().getFrom()), outParam);
		return result;
	}
}
