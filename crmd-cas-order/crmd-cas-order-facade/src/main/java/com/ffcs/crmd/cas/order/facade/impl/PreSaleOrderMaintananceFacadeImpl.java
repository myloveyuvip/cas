package com.ffcs.crmd.cas.order.facade.impl;

import com.ctg.itrdc.platform.common.entity.PageInfo;
import com.ctg.itrdc.platform.common.utils.type.ArrayUtils;
import com.ctg.itrdc.platform.common.utils.type.CollectionUtils;
import com.ctg.itrdc.platform.common.utils.type.NumberUtils;
import com.ctg.itrdc.platform.common.utils.type.StringUtils;
import com.ffcs.crmd.cas.base.context.CasSessionContext;
import com.ffcs.crmd.cas.bean.crmbean.createconnect.CustOrder;
import com.ffcs.crmd.cas.constants.CasConstant;
import com.ffcs.crmd.cas.core.ddd.facade.impl.CrmdBaseFacade;
import com.ffcs.crmd.cas.intf.api.facade.ICasToCrmFacade;
import com.ffcs.crmd.cas.order.api.dto.PreSaleOrderDTO;
import com.ffcs.crmd.cas.order.api.dto.PreSaleOrderHisDTO;
import com.ffcs.crmd.cas.order.api.dto.PreSaleOrderMaintanceDTO;
import com.ffcs.crmd.cas.order.api.dto.PreSaleOrderMaintanceDetailDTO;
import com.ffcs.crmd.cas.order.api.facade.IPreSaleOrderFacade;
import com.ffcs.crmd.cas.order.api.facade.IPreSaleOrderMaintananceFacade;
import com.ffcs.crmd.cas.order.api.vo.PreSaleOrderVo;
import com.ffcs.crmd.cas.order.entity.PreSaleOrder;
import com.ffcs.crmd.cas.order.entity.PreSaleOrderHis;
import com.ffcs.crmd.cas.sys.api.dto.AttrValueDTO;
import com.ffcs.crmd.platform.pub.bean.CrmBeanUtils;
import com.ffcs.crmd.platform.pub.ex.ExceptionUtils;
import com.ffcs.crmd.platform.pub.vo.RetVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

/**
 * Created by YULIYAO on 2016/3/21.
 */
@Service("preSaleOrderMaintananceFacade")
public class PreSaleOrderMaintananceFacadeImpl extends CrmdBaseFacade implements
    IPreSaleOrderMaintananceFacade {

    @Autowired
    private IPreSaleOrderFacade preSaleOrderFacade;

    @Autowired
    private ICasToCrmFacade casToCrmFacade;

    /**
     * 根据正式单单号查询订单关联的预受理单
     * 返回值:PreSaleOrderHisDTO中
     *  1.ExtCustOrderId为null则表示此正式单号不存在
     *  2.HisId为null则表示预受理在用表
     *  3.HisId不为null则表示在历史表
     * @author YULIYAO 2016/3/22
     * return 
     */
    @Override
    public PreSaleOrderHisDTO queryOrderRelPreOrder(String custOrderNumber) {
        PreSaleOrderHisDTO hisDTO = new PreSaleOrderHisDTO();
        hisDTO.setCustSoNumber(custOrderNumber);    //这里将查询的正式单编号传回前端，以便下次关联时传入
        //1.根据正式单编号查询关联的预受理单,含在用表和历史表
        PreSaleOrderVo vo = new PreSaleOrderVo();
        vo.setCustSoNumber(custOrderNumber);
        vo.setPageSize(1);
        vo.setPageNumber(1);
        vo.setStaffId(null);
        vo.setRegionCd(null);
        vo.setOrgId(null);
        PageInfo pageInfo = preSaleOrderFacade.queryPreSaleOrder(vo);
        if (pageInfo != null && CollectionUtils.isNotEmpty(pageInfo.getList())) {
            PreSaleOrderDTO preSaleOrderDTO = (PreSaleOrderDTO) pageInfo.getList().get(0);
            CrmBeanUtils.applyIf(hisDTO, preSaleOrderDTO);
            hisDTO.setHisId(null);
        } else {
            PreSaleOrderHisDTO preSaleOrderHisDTO = preSaleOrderFacade
                .getHisByCustOrderNumber(custOrderNumber);
            if (preSaleOrderHisDTO != null) {
                hisDTO = preSaleOrderHisDTO;
            }
        }
        return hisDTO;
    }

    /**
     * 将正式单与新的预受理单建立关联
     * @author YULIYAO 2016/3/22
     * return 
     */
    @Override
    public boolean connectNewPreOrder(PreSaleOrderHisDTO oldPreDTO, String preOrderNumber,
        String originator) {
        //1. 查询要关联的预受理单,做关联前校验
        PreSaleOrderDTO preSaleOrderDTO = preSaleOrderFacade.getFirstPreSaleOrder(preOrderNumber);
        PreSaleOrderHisDTO preSaleOrderHisDTO = null;
        if (preSaleOrderDTO == null) {
            preSaleOrderHisDTO = preSaleOrderFacade
                .getFirstPreSaleOrderHis(preOrderNumber);
        }
        if (preSaleOrderDTO == null && preSaleOrderHisDTO == null) {
            ExceptionUtils.throwEx("要关联P单不存在！");
        }
        //2. 保存或更新正式单的属性-preSaleOrderNumber
        //调接口保存订单属性-preSaleOrderNumber，服务端判断是在用表还是历史表,插入或更新对应的属性表
        Map<String, String> info = new HashMap<>();
        info.put("custSoNumber", oldPreDTO.getCustSoNumber());
        info.put("preOrderNumber", preOrderNumber);
        String isRel = StringUtils.isNullOrEmpty(oldPreDTO.getPreOrderNumber()) ? "2" : "1";
        info.put("isRel", isRel);
        RetVo retVo = casToCrmFacade.dealConnect(Arrays.asList(info));
        if (retVo.getResult()) {
            if (retVo.getObject() == null && ArrayUtils.isEmpty(retVo.getObject())) {
                ExceptionUtils.throwEx("FJ单不存在!");
            }
        } else {
            ExceptionUtils.throwEx("保存订单属性异常：" + retVo.getMsgTitle());
        }
        CustOrder[] custOrders = (CustOrder[]) retVo.getObject();
        CustOrder custOrder = custOrders[0];
        if ("0".equals(custOrder.getIsHis()) && preSaleOrderHisDTO != null) {
            ExceptionUtils.throwEx("FJ单未竣工，但是P单已竣工，关联失败！");
        }
        connectNewPreOrderBatch(oldPreDTO,preOrderNumber,originator,custOrder);
        return true;
    }

    /**
     * 批量导入要关联的单号，做关联前判断，并返回检验结果
     * @author YULIYAO 2016/3/25
     * return 
     */
    @Override
    public PreSaleOrderMaintanceDTO checkBatchConnect(
        List<PreSaleOrderMaintanceDetailDTO> orderMaintanceDTOs) {
        int canRelate = 0;
        int cannotRelate = 0;
        PreSaleOrderMaintanceDTO preSaleOrderMaintanceDTO = new PreSaleOrderMaintanceDTO();
        preSaleOrderMaintanceDTO.setPreSaleOrderMaintanceDetailDTOList(orderMaintanceDTOs);
        //调接口查询正式单情况
        Map<String,com.ffcs.crmd.cas.bean.crmbean.querycustorderbynum.CustOrder> fjOrderMap = queryCustOrderIntf(orderMaintanceDTOs);
        if (CollectionUtils.isNotEmpty(orderMaintanceDTOs)) {
            for (PreSaleOrderMaintanceDetailDTO detailDTO : orderMaintanceDTOs) {
                StringBuffer reasonBf = new StringBuffer();
                //1.检验FJ单号
                PreSaleOrderHisDTO hisDTO = new PreSaleOrderHisDTO();
                detailDTO.setPreSaleOrderHisDTO(hisDTO);
                if (StringUtils.isNullOrEmpty(detailDTO.getCustSoNumber())) {
                    reasonBf.append("FJ单号为空 ,");
                } else {
                    hisDTO = this
                        .queryOrderRelPreOrder(detailDTO.getCustSoNumber());
                    if ("1".equals(hisDTO.getSeq()+"")) {
                        reasonBf.append("FJ单是第一张单 ,");
                    }
                    if (fjOrderMap.containsKey(detailDTO.getCustSoNumber())) {
                        detailDTO.setOldPreOrderNumber(hisDTO.getPreOrderNumber());
                        detailDTO.setPreSaleOrderHisDTO(hisDTO);
                    } else {
                        reasonBf.append("FJ单不存在 ,");
                    }
                }
                //2.检验P单号
                if (StringUtils.isNullOrEmpty(detailDTO.getTargetPreOrderNumber())) {
                    reasonBf.append("P单号为空 ,");
                } else {
                    PreSaleOrderDTO preSaleOrderDTO = preSaleOrderFacade.getFirstPreSaleOrder(detailDTO.getTargetPreOrderNumber());
                    PreSaleOrderHisDTO preSaleOrderHisDTO = null;
                    if (preSaleOrderDTO == null) {
                        preSaleOrderHisDTO = preSaleOrderFacade
                            .getFirstPreSaleOrderHis(detailDTO.getTargetPreOrderNumber());
                    }
                    if (preSaleOrderDTO == null && preSaleOrderHisDTO == null) {
                        reasonBf.append("要关联P单不存在！");
                    }
                    if (preSaleOrderHisDTO != null && fjOrderMap
                        .containsKey(detailDTO.getCustSoNumber()) && "0"
                        .equals(fjOrderMap.get(detailDTO.getCustSoNumber()).getIsHis())) {
                        reasonBf.append("FJ单未竣工，但是P单已竣工，无法关联！");
                    }
                    if (detailDTO.getTargetPreOrderNumber().equals(hisDTO.getPreOrderNumber())) {
                        reasonBf.append("P单号已关联,");
                    }
                }
                if (StringUtils.isNullOrEmpty(reasonBf)) {
                    detailDTO.setCanRelate(true);
                    canRelate++;
                } else {
                    detailDTO.setCanRelate(false);
                    String reason = reasonBf.toString().endsWith(",") ?
                        reasonBf.toString().substring(0, reasonBf.length() - 1) :
                        reasonBf.toString();
                    detailDTO.setCanntRelReason(reason);
                    cannotRelate++;
                }
            }
            preSaleOrderMaintanceDTO.setCanConnectCount(canRelate);
            preSaleOrderMaintanceDTO.setCanntConnectCount(cannotRelate);
            preSaleOrderMaintanceDTO.setTotalCount(orderMaintanceDTOs.size());
        }

        return preSaleOrderMaintanceDTO;
    }

    /**
     * 调接口查正式单
     * @author YULIYAO 2016/4/18
     * return 
     */
    private Map<String,com.ffcs.crmd.cas.bean.crmbean.querycustorderbynum.CustOrder> queryCustOrderIntf(List<PreSaleOrderMaintanceDetailDTO> orderMaintanceDTOs) {
        Map<String,com.ffcs.crmd.cas.bean.crmbean.querycustorderbynum.CustOrder> orderMap = new HashMap();
        List<String> custSoNumbers = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(orderMaintanceDTOs)) {
            for (PreSaleOrderMaintanceDetailDTO detailDTO : orderMaintanceDTOs) {
                custSoNumbers.add(detailDTO.getCustSoNumber());
            }
            RetVo retVo = casToCrmFacade.queryCustOrderByNum(custSoNumbers);
            if (retVo.getResult()) {
                com.ffcs.crmd.cas.bean.crmbean.querycustorderbynum.CustOrder[] custOrders = (com.ffcs.crmd.cas.bean.crmbean.querycustorderbynum.CustOrder[]) retVo
                    .getObject();
                if (ArrayUtils.isNotEmpty(custOrders)) {
                    for (com.ffcs.crmd.cas.bean.crmbean.querycustorderbynum.CustOrder custOrder : custOrders) {
                        orderMap.put(custOrder.getCustSoNumber(),custOrder);
                    }
                }
            } else {
                ExceptionUtils.throwEx("查询正式单调用接口失败!");
            }
        }
        return orderMap;
    }

    /**
     * 批量关联
     * @author YULIYAO 2016/3/28
     * return
     */
    @Override
    public boolean batchConnect(List<PreSaleOrderMaintanceDetailDTO> detailDTOs,
        String originator) {
        //1.可关联的列表
        Map<String, PreSaleOrderMaintanceDetailDTO> validMap = new HashMap<>(); //使用map对批量的列表进行去重
        List<Map<String, String>> connectMaps = new ArrayList<>();
        Map<String, com.ffcs.crmd.cas.bean.crmbean.createconnect.CustOrder> orderMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(detailDTOs)) {
            for (PreSaleOrderMaintanceDetailDTO detailDTO : detailDTOs) {
                if (detailDTO.isCanRelate()) {
                    validMap.put(detailDTO.getCustSoNumber(), detailDTO);
                    Map<String, String> connectMap = new HashMap();
                    connectMap.put("custSoNumber", detailDTO.getCustSoNumber());
                    connectMap.put("preOrderNumber", detailDTO.getTargetPreOrderNumber());
                    String isRel = StringUtils.isNullOrEmpty(detailDTO.getPreSaleOrderHisDTO().getPreOrderNumber()) ? "2" : "1";
                    connectMap.put("isRel", isRel);
                    connectMaps.add(connectMap);
                }

            }
            //批量调接口添加正式单属性-预受理编码
            RetVo retVo = casToCrmFacade.dealConnect(connectMaps);
            if (retVo.getResult()) {
                com.ffcs.crmd.cas.bean.crmbean.createconnect.CustOrder[] custOrders = (CustOrder[]) retVo
                    .getObject();
                if (ArrayUtils.isNotEmpty(custOrders)) {
                    for (com.ffcs.crmd.cas.bean.crmbean.createconnect.CustOrder custOrder : custOrders) {
                        orderMap.put(custOrder.getCustSoNumber(), custOrder);
                    }
                }
            }
            List<PreSaleOrderMaintanceDetailDTO> validList = new ArrayList<>(validMap.values());
            //关联
            for (PreSaleOrderMaintanceDetailDTO detailDTO : validList) {
                if (orderMap.containsKey(detailDTO.getCustSoNumber())) {
                    connectNewPreOrderBatch(detailDTO.getPreSaleOrderHisDTO(),
                        detailDTO.getTargetPreOrderNumber(), originator, orderMap.get(detailDTO.getCustSoNumber()));
                }

            }
        }
        //
        return true;
    }

    private void connectNewPreOrderBatch(PreSaleOrderHisDTO oldPreDTO,
        String targetPreOrderNumber, String originator, com.ffcs.crmd.cas.bean.crmbean.createconnect.CustOrder custOrder) {
        //查询要关联的P单
        PreSaleOrderDTO targetPreOrderDTO = preSaleOrderFacade.getFirstPreSaleOrder(targetPreOrderNumber);
        PreSaleOrderHisDTO targetPreOrderHidDTO = null;
        if (targetPreOrderDTO == null) {
            targetPreOrderHidDTO = preSaleOrderFacade
                .getFirstPreSaleOrderHis(targetPreOrderNumber);
        }
        long staffId = CasSessionContext.getContext().getStaffId() == null ? NumberUtils
            .nullToLongZero(CasConstant.INTF_STAFF_ID.getValue())
            : CasSessionContext.getContext().getStaffId();
        //删除旧关联
        if (oldPreDTO.getHisId() == null && !StringUtils.isNullOrEmpty(oldPreDTO.getPreOrderNumber())) {
            PreSaleOrderDTO oldPreOrderDTO = new PreSaleOrderDTO();
            CrmBeanUtils.applyIf(oldPreOrderDTO, oldPreDTO);
            preSaleOrderFacade.remove(oldPreOrderDTO);
            preSaleOrderFacade.deletePreOrderHisByOrderId(oldPreOrderDTO.getPreOrderId(),
                oldPreOrderDTO.getShardingId());

        }else if(oldPreDTO.getHisId() != null){
            preSaleOrderFacade.deletePreOrderHis(oldPreDTO);
        }
        if (targetPreOrderDTO != null) {
            //3.2.1 要关联的是在用表记录，则新增在用表关联记录，否则新增历史表关联记录
            PreSaleOrder preSaleOrderToSave = new PreSaleOrder(true);
            String str = "select max(pso.seq) from pre_sale_order pso where 1=1 and pso.pre_order_number = ?";
            Long seq = null;
            if (!StringUtils.isNullOrEmpty(targetPreOrderDTO.getPreOrderNumber())) {
                seq = PreSaleOrder.repository().jdbcQueryForLong(str, targetPreOrderDTO.getPreOrderNumber())+1;
            }
            preSaleOrderToSave.setShardingId(targetPreOrderDTO.getShardingId());
            preSaleOrderToSave.setStaffId(staffId);
            preSaleOrderToSave.setPreOrderNumber(targetPreOrderDTO.getPreOrderNumber());
            preSaleOrderToSave.setSceneType(targetPreOrderDTO.getSceneType());
            preSaleOrderToSave.setAcceptTime(new Timestamp(new Date().getTime()));
            preSaleOrderToSave.setStatusCd(targetPreOrderDTO.getStatusCd());
            preSaleOrderToSave.setCustSoNumber(oldPreDTO.getCustSoNumber());
            preSaleOrderToSave.setCustName(oldPreDTO.getCustName());
            preSaleOrderToSave.setMobilePhone(oldPreDTO.getMobilePhone());
            preSaleOrderToSave.setSeq(seq);
            if (custOrder != null) {
                preSaleOrderToSave.setCustId(custOrder.getCustId());
                preSaleOrderToSave.setOrderType(custOrder.getOrderType());
                preSaleOrderToSave.setChannelD(custOrder.getChannelId());
                preSaleOrderToSave.setOrgId(custOrder.getOrgId());
                preSaleOrderToSave.setAreaId(custOrder.getAreaId());
                preSaleOrderToSave.setRegionCd(custOrder.getRegionCd());
                preSaleOrderToSave
                    .setExtCustOrderId(custOrder.getCustOrderId());
            }
            preSaleOrderToSave.save();
        } else {
            PreSaleOrderHis preSaleOrderHisToSave = new PreSaleOrderHis(true);
            String str = "select max(pso.seq) from pre_sale_order_his pso where 1=1 and pso.pre_order_number = ?";
            Long seq = null;
            if (!StringUtils.isNullOrEmpty(targetPreOrderHidDTO.getPreOrderNumber())) {
                seq = PreSaleOrder.repository().jdbcQueryForLong(str, targetPreOrderHidDTO.getPreOrderNumber())+1;
            }
            preSaleOrderHisToSave.setPreOrderId(targetPreOrderHidDTO.getPreOrderId());
            preSaleOrderHisToSave.setShardingId(targetPreOrderHidDTO.getShardingId());
            preSaleOrderHisToSave.setStaffId(staffId);
            preSaleOrderHisToSave.setPreOrderNumber(targetPreOrderHidDTO.getPreOrderNumber());
            preSaleOrderHisToSave.setSceneType(targetPreOrderHidDTO.getSceneType());
            preSaleOrderHisToSave.setAcceptTime(new Timestamp(new Date().getTime()));
            preSaleOrderHisToSave.setStatusCd(targetPreOrderHidDTO.getStatusCd());
            preSaleOrderHisToSave.setCustSoNumber(oldPreDTO.getCustSoNumber());
            preSaleOrderHisToSave.setCustName(oldPreDTO.getCustName());
            preSaleOrderHisToSave.setMobilePhone(oldPreDTO.getMobilePhone());
            preSaleOrderHisToSave.setSeq(seq);
            if (custOrder != null) {
                preSaleOrderHisToSave.setCustId(custOrder.getCustId());
                preSaleOrderHisToSave.setOrderType(custOrder.getOrderType());
                preSaleOrderHisToSave.setChannelD(custOrder.getChannelId());
                preSaleOrderHisToSave.setOrgId(custOrder.getOrgId());
                preSaleOrderHisToSave.setAreaId(custOrder.getAreaId());
                preSaleOrderHisToSave.setRegionCd(custOrder.getRegionCd());
                preSaleOrderHisToSave.setExtCustOrderId(custOrder.getCustOrderId());
            }
            preSaleOrderHisToSave.save();
        }

        //4. 保存处理过程
        String maintanceDesc = getMaintanceDesc(originator, oldPreDTO.getCustSoNumber(), oldPreDTO.getPreOrderNumber(), targetPreOrderNumber);
        List<AttrValueDTO> attrValueDTOs = new ArrayList<>();
        AttrValueDTO attrValueDTO = new AttrValueDTO();
        attrValueDTO.setAttrId(0L);
        attrValueDTO.setAttrDesc(maintanceDesc);
        attrValueDTOs.add(attrValueDTO);
        if (targetPreOrderDTO != null) {
            preSaleOrderFacade
                .createPreProc(targetPreOrderDTO.getPreOrderId(), targetPreOrderDTO.getShardingId(), staffId, attrValueDTOs, NumberUtils
                    .toLong(CasConstant.PRE_PROC_EVENT_ID_ADD_MAINTANANCE_LOG.getValue()),maintanceDesc);
        } else if (targetPreOrderHidDTO != null) {
            preSaleOrderFacade.createPreProcHis(targetPreOrderHidDTO.getPreOrderId(), targetPreOrderHidDTO.getShardingId(), staffId, attrValueDTOs, NumberUtils
                .toLong(CasConstant.PRE_PROC_EVENT_ID_ADD_MAINTANANCE_LOG.getValue()),maintanceDesc);
        }
    }

    /**
     * 组装关联过程描述
     * @author YULIYAO 2016/3/24
     * return 
     */
    private String getMaintanceDesc(String originator, String custSoNumber, String oldPreNum,
        String newPreNum) {
        StringBuffer log4maintance = new StringBuffer();
        if (!StringUtils.isNullOrEmpty(originator)) {
            log4maintance.append("关联提出人：").append(originator);
        }
        if (!StringUtils.isNullOrEmpty(custSoNumber)) {
            log4maintance.append(" FJ单号：").append(custSoNumber);
        }
        log4maintance.append(" 原关联P单号：").append(oldPreNum);
        log4maintance.append(" 要关联P单号：").append(newPreNum);
        return log4maintance.toString();
    }


}
