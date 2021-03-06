package com.ffcs.crmd.cas.order.facade.impl;

import com.ctg.itrdc.platform.common.entity.PageInfo;
import com.ctg.itrdc.platform.common.utils.type.CollectionUtils;
import com.ctg.itrdc.platform.common.utils.type.DateUtils;
import com.ctg.itrdc.platform.common.utils.type.NumberUtils;
import com.ctg.itrdc.platform.common.utils.type.StringUtils;
import com.ctg.itrdc.platform.pub.util.ApplicationContextUtil;
import com.ffcs.crmd.cas.base.context.CasSessionContext;
import com.ffcs.crmd.cas.base.utils.CrmClassUtils;
import com.ffcs.crmd.cas.constants.CasConstant;
import com.ffcs.crmd.cas.constants.OrderConstant;
import com.ffcs.crmd.cas.core.ddd.facade.impl.CrmdBaseFacade;
import com.ffcs.crmd.cas.intf.api.dto.AutoGenOrderInDTO;
import com.ffcs.crmd.cas.intf.api.facade.ICasToPkFacade;
import com.ffcs.crmd.cas.order.api.dto.AutoGenOrderDTO;
import com.ffcs.crmd.cas.order.api.dto.PreSaleOrderDTO;
import com.ffcs.crmd.cas.order.api.dto.PreSaleOrderPoolDTO;
import com.ffcs.crmd.cas.order.api.facade.IAutoGenOrderFacade;
import com.ffcs.crmd.cas.order.api.facade.IPreSaleOrderFacade;
import com.ffcs.crmd.cas.order.api.facade.IPreSaleOrderPoolFacade;
import com.ffcs.crmd.cas.order.entity.AutoGenOrder;
import com.ffcs.crmd.cas.order.entity.AutoGenOrderAttach;
import com.ffcs.crmd.cas.order.entity.PreSaleOrder;
import com.ffcs.crmd.cas.order.entity.PreSaleOrderProc;
import com.ffcs.crmd.cas.order.service.IAutoGenOrderAttachService;
import com.ffcs.crmd.cas.order.service.IAutoGenOrderService;
import com.ffcs.crmd.cas.order.service.IPreSaleOrderProcService;
import com.ffcs.crmd.cas.order.service.IPreSaleOrderService;
import com.ffcs.crmd.cas.order.vo.PreSaleOrderProcVo;
import com.ffcs.crmd.cas.sys.api.dto.StaffDTO;
import com.ffcs.crmd.platform.pub.bean.CrmBeanUtils;
import com.ffcs.crmd.platform.pub.vo.RetVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("autoGenOrderFacade")
public class AutoGenOrderFacadeImpl extends CrmdBaseFacade implements IAutoGenOrderFacade {
	@Autowired
	IAutoGenOrderService       autoGenOrderService;
	@Autowired
	IAutoGenOrderAttachService autoGenOrderAttachService;
	@Autowired
	IPreSaleOrderService       preSaleOrderService;
	@Autowired
	IPreSaleOrderProcService   preSaleOrderProcService;
	@Autowired
	IPreSaleOrderPoolFacade    preSaleOrderPoolFacade;
	@Autowired
	IPreSaleOrderFacade        preSaleOrderFacade;
	@Autowired
	ICasToPkFacade casToPkFacade;


	@Override
	public int saveAutoGenOrder(AutoGenOrderInDTO inDTO) {
		int result = 0;
		AutoGenOrder autoGenOrder = new AutoGenOrder(true);
		autoGenOrder.setPreSaleOrder(inDTO.getAutoGenOrder().getPreOrderNumber());
		autoGenOrder.setSceneType(inDTO.getAutoGenOrder().getSceneType());
		autoGenOrder.setSceneFlag(inDTO.getAutoGenOrder().getSceneFlag());
		autoGenOrder.setProdOfferId(NumberUtils.toLong(inDTO.getAutoGenOrder().getProdOfferId()));
		autoGenOrder.setAccNbr(inDTO.getAutoGenOrder().getAccNbr());
		autoGenOrder.setCustId(NumberUtils.toLong(inDTO.getAutoGenOrder().getCustId()));
		autoGenOrder.setStatusCd(inDTO.getAutoGenOrder().getStatusCd());
		autoGenOrder.setShardingId(NumberUtils.toLong(inDTO.getAutoGenOrder().getShardingId()));

		result += autoGenOrderService.save(autoGenOrder);

		AutoGenOrderAttach autoGenOrderAttach = new AutoGenOrderAttach(true);
		autoGenOrderAttach.setXmlMsg(inDTO.getAutoGenOrder().getXmlMsg());
		autoGenOrderAttach.setAutoGenOrderId(autoGenOrder.getAutoGenOrderId());
		autoGenOrderAttach.setShardingId(NumberUtils.toLong(inDTO.getAutoGenOrder().getShardingId()));
		result += autoGenOrderAttachService.save(autoGenOrderAttach);
		return result;
	}

	@Override
	public RetVo autoGenOrderCompleteInner(String preOrderNumber, String autoGenResult, String resultDesc, String pic, String errCode) {
		RetVo retVO = new RetVo();
		retVO.setResult(true);
		if (StringUtils.isNullOrEmpty(preOrderNumber)) {
			retVO.setResult(false);
			retVO.setMsgTitle("预受理订单号为空。");
			return retVO;
		}
		if (StringUtils.isNullOrEmpty(autoGenResult)) {
			retVO.setResult(false);
			retVO.setMsgTitle("处理结果为空。");
			return retVO;
		}

		AutoGenOrder autoGenOrder = autoGenOrderService.getOrderByPreOrderNumber(preOrderNumber);
		if (autoGenOrder == null) {
			retVO.setResult(false);
			retVO.setMsgTitle("自动处理表记录为空。");
			return retVO;
		}
		// 自动受理完成保存过程 - chenjw 20150721 crm00063643
		// 预受理处理过程事件标识
		Long eventId = null;
		if ("Y".equals(autoGenResult)) { // 处理成功
			eventId = NumberUtils.toLong(OrderConstant.PRE_PROC_EVENT_ID_AUTO_GEN_SUC.getValue());
			autoGenOrder.setReceiveDate(DateUtils.dateToTimestamp(DateUtils.getNowDate()));
			autoGenOrder.setStatusCd(OrderConstant.AUTO_GEN_ORDER_STATUS_GEN_SUC.getValue());
		} else { // 处理失败
			eventId = NumberUtils.toLong(OrderConstant.PRE_PROC_EVENT_ID_AUTO_GEN_FAIL.getValue());
			if (!OrderConstant.AUTO_GEN_ORDER_STATUS_COMPLETE.getValue().equals(autoGenOrder.getStatusCd())) {
				// 如果还没有竣工，则保存状态为普坤处理失败
				autoGenOrder.setReceiveDate(DateUtils.dateToTimestamp(DateUtils.getNowDate()));
				autoGenOrder.setStatusCd(OrderConstant.AUTO_GEN_ORDER_STATUS_GEN_FAIL.getValue());
			}
		}

		PreSaleOrder preSaleOrder = preSaleOrderService.queryFirstPreOrderByOrderNumber(preOrderNumber);
		List<PreSaleOrderProcVo> procVos = new ArrayList<PreSaleOrderProcVo>();
		StaffDTO staffDTO = new StaffDTO();
		if (preSaleOrder != null) {
			// 创建并保存预受理处理过程
			PreSaleOrderProcVo procVo = new PreSaleOrderProcVo();
			procVos.add(procVo);
			procVo.setAttrDesc(resultDesc);
			Long attrId = CrmClassUtils.getAttrSpecByCode(PreSaleOrderProc.class
					.getSimpleName(), "autoGenResultDesc") != null ? CrmClassUtils
					.getAttrSpecByCode(PreSaleOrderProc.class.getSimpleName(),
							"autoGenResultDesc").getId() : 0L;
			procVo.setAttrId(attrId);
			
			staffDTO.setStaffId(CasSessionContext.getContext().getStaffId());
			staffDTO.setStaffName("接口专用工号");
			preSaleOrderProcService.createPreSaleOrderProc(preSaleOrder,eventId,procVos, staffDTO);
		}
		IAutoGenOrderAttachService attachService = ApplicationContextUtil.getBean("autoGenOrderAttachService");
		AutoGenOrderAttach autoGenOrderAttach = attachService.getByIdAndShardingId(
				autoGenOrder.getId(), autoGenOrder.getShardingId());
		if (autoGenOrderAttach != null) {
			autoGenOrderAttach.setPic(pic);
		}
		autoGenOrderAttachService.update(autoGenOrderAttach);

		autoGenOrder.setResult(resultDesc);
		autoGenOrder.setErrCode(errCode);
		autoGenOrderService.update(autoGenOrder);
		return retVO;
	}

	@Override
	public PageInfo queryAutoGenOrderPage(AutoGenOrderDTO autoGenOrderDTO) {
		List<AutoGenOrderDTO> autoGenOrderDTOs = new ArrayList<AutoGenOrderDTO>();
		Map param = dto2Map(autoGenOrderDTO);

		PageInfo pageInfo = autoGenOrderService
				.queryAutoGenOrder(param, autoGenOrderDTO.getPageSize(),
						autoGenOrderDTO.getPageNumber());
		if (pageInfo!=null && CollectionUtils.isNotEmpty(pageInfo.getList())) {
			autoGenOrderDTOs = CrmBeanUtils.copyList(pageInfo.getList(), AutoGenOrderDTO.class);
			pageInfo.setList(autoGenOrderDTOs);
		}
		return pageInfo;
	}

	@Override
	public List<AutoGenOrderDTO> queryAutoGenOrder(AutoGenOrderDTO autoGenOrderDTO) {
		List<AutoGenOrderDTO> autoGenOrderDTOs = new ArrayList<AutoGenOrderDTO>();
		Map param = new HashMap();
		//参数：是否有轮洵传入的查询取模
		if (autoGenOrderDTO.getModel() != 0 && autoGenOrderDTO.getModelCount() != 0) {
			param.put("model", autoGenOrderDTO.getModel());
			param.put("modelCount", autoGenOrderDTO.getModelCount());
		}
		//参数：状态
		if (!StringUtils.isNullOrEmpty(autoGenOrderDTO.getStatusCd())) {
			param.put("statusCd", autoGenOrderDTO.getStatusCd());
		}
		//参数：编码
		if (!StringUtils.isNullOrEmpty(autoGenOrderDTO.getPreSaleOrder())) {
			param.put("preSaleOrder", autoGenOrderDTO.getPreSaleOrder());
		}
		List<AutoGenOrder> autoGenOrders = autoGenOrderService.queryAutoGenOrder(param);
		if (CollectionUtils.isNotEmpty(autoGenOrders)) {
			autoGenOrderDTOs = CrmBeanUtils.copyList(autoGenOrders, AutoGenOrderDTO.class);
		}
		return autoGenOrderDTOs;
	}

	/**
	 * 将查询的DTO转换为map
	 * @author YULIYAO 2016/3/16
	 * return 
	 */
	private Map dto2Map(AutoGenOrderDTO autoGenOrderDTO) {
		Map param = new HashMap();
		//参数：是否有轮洵传入的查询取模
		if (autoGenOrderDTO.getModelCount() != 0) {
			param.put("model", autoGenOrderDTO.getModel());
			param.put("modelCount", autoGenOrderDTO.getModelCount());
		}
		//参数：状态
		if (!StringUtils.isNullOrEmpty(autoGenOrderDTO.getStatusCd())) {
			param.put("statusCd", autoGenOrderDTO.getStatusCd());
		}
		return param;
	}
	
	/**
	 * 轮洵查询：创建状态，关联的P单为待受理，关联的工单池接收员工为空或者是接口工号
	 * @author YULIYAO 2016/3/15
	 * return
	 */
	@Override
	public List<AutoGenOrderDTO> queryAutoGenOrderSentPk(int modelCount, int model, int pageSize) {
		if (modelCount == 0) {
			return null;
		}
		List<AutoGenOrderDTO> autoGenOrderDTOs = new ArrayList<AutoGenOrderDTO>();
		PageInfo pageInfo = autoGenOrderService
				.queryAutoGenOrderSentPk(modelCount, model, pageSize);
		if (pageInfo!=null && CollectionUtils.isNotEmpty(pageInfo.getList())) {
			autoGenOrderDTOs = CrmBeanUtils.copyList(pageInfo.getList(), AutoGenOrderDTO.class);
		}
		return autoGenOrderDTOs;
	}

	/**
	 * 查询自动受理单报文
	 * @author YULIYAO 2016/3/15
	 * return
	 */
	@Override
	public String getXmlMsg(Long autoGenOrderId) {
		AutoGenOrderAttach autoGenOrderAttach = autoGenOrderAttachService.get(autoGenOrderId);
		return autoGenOrderAttach == null ? null : autoGenOrderAttach.getXmlMsg();
	}

	/**
	 * 保存
	 * @author YULIYAO 2016/3/15
	 * return 
	 */
	@Override
	public int save(AutoGenOrderDTO autoGenOrderDTO) {
		if (autoGenOrderDTO != null) {
			//新增
			if (StringUtils.isNullOrEmpty(autoGenOrderDTO.getAutoGenOrderId())
					|| autoGenOrderDTO.getAutoGenOrderId() == 0) {
				AutoGenOrder autoGenOrder = new AutoGenOrder(true);
				CrmBeanUtils.applyIf(autoGenOrder, autoGenOrderDTO, false);
				return autoGenOrder.save();
			} else {    //修改
				AutoGenOrder autoGenOrder = new AutoGenOrder();
				CrmBeanUtils.applyIf(autoGenOrder, autoGenOrderDTO);
				return autoGenOrder.update();
			}
		}
		return 0;
	}

	@Override
	public int remove(AutoGenOrderDTO autoGenOrderDTO) {
		if (autoGenOrderDTO != null && !StringUtils
				.isNullOrEmpty(autoGenOrderDTO.getAutoGenOrderId())) {
			AutoGenOrder autoGenOrder = new AutoGenOrder();
			CrmBeanUtils.applyIf(autoGenOrder, autoGenOrderDTO);
			return autoGenOrder.remove();
		}
		return 0;
	}

	/**
	 * 送普坤
	 * @author YULIYAO 2016/4/19
	 * return 
	 */
	@Override
	public RetVo sendToPk(AutoGenOrderDTO autoGenOrderDTO) {
		RetVo retVo = new RetVo();
		String inXml = this.getXmlMsg(autoGenOrderDTO.getAutoGenOrderId());
		// 送普坤结果
		boolean result = false;
		if (!StringUtils.isNullOrEmpty(inXml)) {
			autoGenOrderDTO.setSendDate(DateUtils.dateToTimestamp(new Date()));
			retVo = casToPkFacade.autoGenOrder(inXml);
			if (retVo != null && retVo.getResult()) {
				result = true;
				autoGenOrderDTO.setStatusCd(CasConstant.AUTO_GEN_ORDER_STATUS_SEND_PK_SUC.getValue());
			} else {
				autoGenOrderDTO.setStatusCd(CasConstant.AUTO_GEN_ORDER_STATUS_SEND_PK_FAIL.getValue());
				autoGenOrderDTO.setResult(retVo != null ? retVo.getMsgTitle() : "");
			}
		} else {
			autoGenOrderDTO.setStatusCd(CasConstant.AUTO_GEN_ORDER_STATUS_SEND_PK_FAIL.getValue());
			autoGenOrderDTO.setResult("送普坤报文为空。");
		}
		this.save(autoGenOrderDTO);
		if (!result) {
			// 送普坤失败则保存工单池记录为未接收
			PreSaleOrderDTO preSaleOrderDTO = preSaleOrderFacade.getFirstPreSaleOrder(autoGenOrderDTO.getPreSaleOrder());
			if (preSaleOrderDTO != null) {
				PreSaleOrderPoolDTO orderPoolDTO = preSaleOrderPoolFacade.getByPreOrderId(preSaleOrderDTO.getPreOrderId());
				// 如果工单池状态是已接收，并且接收员工是接口专用工号
				if (orderPoolDTO != null && CasConstant.PRE_POOL_STATUS_CD_ACCEPTED.getValue()
						.equals(orderPoolDTO.getStatusCd())
						&& CasConstant.INTF_STAFF_ID.getValue().equals(""+orderPoolDTO.getStaffId())) {
					orderPoolDTO.setStatusCd(CasConstant.PRE_POOL_STATUS_CD_NOT_ACCEPTED.getValue());
					orderPoolDTO.setStaffId(null);
					orderPoolDTO.setAcceptOrgId(null);
					orderPoolDTO.setIsLeaderAssign("0");
					orderPoolDTO.setAcceptDate(null);
					preSaleOrderPoolFacade.save(orderPoolDTO);
				}
			}
		} else {
			// 送普坤成功，判断工单池的接收员工如果不是接口工号，则设置为接口工号
			PreSaleOrderDTO preSaleOrderDTO = preSaleOrderFacade.getFirstPreSaleOrder(autoGenOrderDTO.getPreSaleOrder());
			if (preSaleOrderDTO != null) {
				PreSaleOrderPoolDTO orderPoolDTO = preSaleOrderPoolFacade.getByPreOrderId(preSaleOrderDTO.getPreOrderId());
				if (orderPoolDTO != null
						&& !CasConstant.INTF_STAFF_ID.getValue().equals(""+orderPoolDTO.getStaffId())) {
					orderPoolDTO.setStaffId(NumberUtils.nullToLongZero(CasConstant.INTF_STAFF_ID.getValue()));
					orderPoolDTO.setAcceptOrgId(
							NumberUtils.nullToLongZero(CasConstant.INTF_ORG_ID.getValue()));
					preSaleOrderPoolFacade.savePreSaleOrderPoolAccept(orderPoolDTO, "0");
				}
			}
		}
		return retVo;
	}
}
