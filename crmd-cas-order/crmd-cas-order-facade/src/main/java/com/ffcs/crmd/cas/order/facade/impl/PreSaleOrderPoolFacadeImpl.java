package com.ffcs.crmd.cas.order.facade.impl;

import com.ctg.itrdc.platform.common.entity.PageInfo;
import com.ctg.itrdc.platform.common.exception.RtManagerException;
import com.ctg.itrdc.platform.common.utils.type.*;
import com.ctg.itrdc.platform.pub.container.BeanLoader;
import com.ctg.udal.ddl.api.IDdlService;
import com.ffcs.crmd.cas.base.api.AcrossOrgDTO;
import com.ffcs.crmd.cas.base.cache.LocalCache;
import com.ffcs.crmd.cas.base.context.CasSessionContext;
import com.ffcs.crmd.cas.base.utils.CrmClassUtils;
import com.ffcs.crmd.cas.bean.crmbean.querycustorder.CustomerOrder;
import com.ffcs.crmd.cas.bean.crmbean.saveDocInfo.DocList;
import com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderOverTimeList.InParam;
import com.ffcs.crmd.cas.constants.CasConstant;
import com.ffcs.crmd.cas.constants.IntfConstant;
import com.ffcs.crmd.cas.core.ddd.facade.impl.CrmdBaseFacade;
import com.ffcs.crmd.cas.intf.api.dto.*;
import com.ffcs.crmd.cas.intf.api.facade.ICasToCrmFacade;
import com.ffcs.crmd.cas.intf.api.facade.ICasToPadFacade;
import com.ffcs.crmd.cas.order.api.dto.*;
import com.ffcs.crmd.cas.order.api.facade.IInteractionAssignOrgFacade;
import com.ffcs.crmd.cas.order.api.facade.IOrgSceneTypeRelFacade;
import com.ffcs.crmd.cas.order.api.facade.IPreSaleOrderFacade;
import com.ffcs.crmd.cas.order.api.facade.IPreSaleOrderPoolFacade;
import com.ffcs.crmd.cas.order.api.vo.OrgSceneTypeRelVo;
import com.ffcs.crmd.cas.order.api.vo.PreSaleOrderVo;
import com.ffcs.crmd.cas.order.entity.*;
import com.ffcs.crmd.cas.order.service.IPreSaleOrderPoolService;
import com.ffcs.crmd.cas.order.service.IPreSaleOrderProcService;
import com.ffcs.crmd.cas.order.service.IPreSaleOrderService;
import com.ffcs.crmd.cas.order.vo.PreSaleOrderOverTimeListVo;
import com.ffcs.crmd.cas.order.vo.PreSaleOrderPoolVo;
import com.ffcs.crmd.cas.sys.api.dto.*;
import com.ffcs.crmd.cas.sys.api.facade.ICasSysFacade;
import com.ffcs.crmd.cas.sys.entity.Organization;
import com.ffcs.crmd.cas.sys.entity.Staff;
import com.ffcs.crmd.cas.sys.service.IOrganizationService;
import com.ffcs.crmd.platform.data.utils.CrmEntityUtils;
import com.ffcs.crmd.platform.meta.entity.AttrSpec;
import com.ffcs.crmd.platform.pub.bean.CrmBeanUtils;
import com.ffcs.crmd.platform.pub.ex.ExceptionUtils;
import com.ffcs.crmd.platform.pub.facade.CrmSessionContext;
import com.ffcs.crmd.platform.pub.vo.RetVo;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.*;

@Service("preSaleOrderPoolFacade")
public class PreSaleOrderPoolFacadeImpl extends CrmdBaseFacade implements IPreSaleOrderPoolFacade {
    @Autowired
    IPreSaleOrderPoolService preSaleOrderPoolService;
    @Autowired
    IPreSaleOrderService preSaleOrderService;
    @Autowired
    IPreSaleOrderProcService preSaleOrderProcService;
    @Autowired
    ICasToPadFacade casToPadFacade;
    @Autowired
    IPreSaleOrderFacade preSaleOrderFacade;
    @Autowired
    ICasToCrmFacade casToCrmFacade;
    @Autowired
    ICasSysFacade casSysFacade;
    @Autowired
    private IInteractionAssignOrgFacade interactionAssignOrgFacade;
    @Autowired
    private IOrgSceneTypeRelFacade orgSceneTypeRelFacade;
    @Autowired
    private IOrganizationService organizationService;

    private LocalCache localCache = LocalCache.getInstance();

    @Override
    public int savePreSaleOrderPoolAccept(PreSaleOrderPoolDTO preSaleOrderPoolDTO) {
        return savePreSaleOrderPoolAccept(preSaleOrderPoolDTO, "1");
    }

    @Override
    public int savePreSaleOrderPoolAccept(PreSaleOrderPoolDTO preSaleOrderPoolDTO, String isLeaderAssign) {
        int result = 0;
        PreSaleOrderPool preSaleOrderPool = new PreSaleOrderPool();
        CrmBeanUtils.applyIf(preSaleOrderPool, preSaleOrderPoolDTO);
        Date date = new Date();
        preSaleOrderPool.setAcceptDate(DateUtils.dateToTimestamp(date));
        preSaleOrderPool.setIsLeaderAssign(isLeaderAssign);
        preSaleOrderPool.setUpdateDate(DateUtils.dateToTimestamp(date));
        preSaleOrderPool.setStatusCd(IntfConstant.PRE_POOL_STATUS_CD_ACCEPTED.getValue());
        result = this.preSaleOrderPoolService.update(preSaleOrderPool);
        return result;
    }

    @Override
    public PreSaleOrderPoolAmountDTO queryPreSaleOrderPoolAmount(AcrossOrgDTO acrossOrgDTO) {
        PreSaleOrderPoolAmountDTO dto = new PreSaleOrderPoolAmountDTO();
        PreSaleOrderPoolVo vo = convertAcrossOrgToVo(acrossOrgDTO);
        //1.查询总单量
        int totalAmount = preSaleOrderPoolService
                .queryPreSaleOrderPoolAmount(vo);
        dto.setTotalAmount(totalAmount);
        //2.查询未接收单量
        List<String> unAcceptStatusCds = new ArrayList<>();
        unAcceptStatusCds.add(CasConstant.PRE_POOL_STATUS_CD_NOT_ACCEPTED.getValue());
        vo.setStatusCdList(unAcceptStatusCds);
        int unAcceptAmount = preSaleOrderPoolService.queryPreSaleOrderPoolAmount(vo);
        dto.setUnAcceptAmount(unAcceptAmount);
        //3.查询可派单单量
        List<String> assignableStatusCds = new ArrayList<>();
        assignableStatusCds.add(CasConstant.PRE_POOL_STATUS_CD_NOT_ACCEPTED.getValue());
        assignableStatusCds.add(CasConstant.PRE_POOL_STATUS_CD_ACCEPTED.getValue());
        assignableStatusCds.add(CasConstant.PRE_POOL_STATUS_CD_LOCKED.getValue());
        vo.setStatusCdList(assignableStatusCds);
        int assignableAmount = preSaleOrderPoolService
                .queryPreSaleOrderPoolAmount(vo);
        dto.setAssignableAmount(assignableAmount);
        return dto;
    }

    @Override
    public PreSaleOrderPoolAmountDTO queryPreSaleOrderPoolReceiveAmount(AcrossOrgDTO acrossOrgDTO,Long orgId,
                                                                        Long staffId) {
        PreSaleOrderPoolAmountDTO dto = new PreSaleOrderPoolAmountDTO();
        PreSaleOrderPoolVo vo = convertAcrossOrgToVo(acrossOrgDTO);
        //1.个人接收的工单数
        vo.setStaffId(staffId);
        vo.setStatusCd(CasConstant.PRE_POOL_STATUS_CD_ACCEPTED.getValue());
        int staffReceivedAmount = preSaleOrderPoolService.queryPreSaleOrderPoolAmount(vo);
        dto.setStaffReceivedAmount(staffReceivedAmount);
        //2.团队工单池总数
        vo.setStatusCd(null);
        vo.setStaffId(null);    //清空上次查询条件
        vo.setAcceptOrgId(orgId);
        int orgAmount = preSaleOrderPoolService
                .queryPreSaleOrderPoolAmount(vo);
        dto.setOrgAmount(orgAmount);
        //3.查询未接收单量
        vo.setAcceptOrgId(null);        //清空上次查询条件
        List<String> unAcceptStatusCds = new ArrayList<>();
        unAcceptStatusCds.add(CasConstant.PRE_POOL_STATUS_CD_NOT_ACCEPTED.getValue());
        vo.setStatusCdList(unAcceptStatusCds);
        int unAcceptAmount = preSaleOrderPoolService.queryPreSaleOrderPoolAmount(vo);
        dto.setUnAcceptAmount(unAcceptAmount);
        return dto;
    }

    @Override
    public int getPreSaleOrderPoolAmount(PreSaleOrderPoolDTO preSaleOrderPoolDTO, AcrossOrgDTO acrossOrgDTO) {
        PreSaleOrderPoolVo vo = new PreSaleOrderPoolVo();
        CrmBeanUtils.applyIf(vo, preSaleOrderPoolDTO);
        PreSaleOrderPoolVo confVo = convertAcrossOrgToVo(acrossOrgDTO);
        CrmBeanUtils.applyIf(vo, confVo, false);
        return preSaleOrderPoolService.queryPreSaleOrderPoolAmount(vo);
    }

    @Override
    public PageInfo queryPreSaleOrderPool(PreSaleOrderPoolDTO preSaleOrderPoolDTO, Long areaId,
        Long orgId, AcrossOrgDTO acrossOrgDTO) {
        //1.判断是否有配置跨团队受理并初始化
        PreSaleOrderPoolVo vo = convertAcrossOrgToVo(acrossOrgDTO);
        CrmBeanUtils.applyIf(vo, preSaleOrderPoolDTO, false);
        //2.查询工单池
        //2.1 查询条件：预受理单号或订单编号，先查出预受理单ID作为传入查询的条件
        List<Long> preOrderIds = new ArrayList<>();
        Map orderMap = new HashMap();
        if (!StringUtils.isNullOrEmpty(preSaleOrderPoolDTO.getPreOrderNbr())) {
            orderMap.put("preOrderNumber", preSaleOrderPoolDTO.getPreOrderNbr());
        } else if (!StringUtils.isNullOrEmpty(preSaleOrderPoolDTO.getCustSoNumber())) {
            orderMap.put("custSoNumber", preSaleOrderPoolDTO.getCustSoNumber());
        }
        if (orderMap.size() > 0) {
            List<PreSaleOrder> preSaleOrders = preSaleOrderService.queryPreSaleOrderList(orderMap);
            //转换为id list
            if (CollectionUtils.isNotEmpty(preSaleOrders)) {
                for (PreSaleOrder preSaleOrder : preSaleOrders) {
                    preOrderIds.add(preSaleOrder.getPreOrderId());
                }
            } else {
                return new PageInfo();
            }
            vo.setPreSaleOrderIds(preOrderIds);
        }
        //2.2 设置查询条件
        vo.setNotStatusCd(CasConstant.PRE_POOL_STATUS_CD_WAIT_FOR_AUDIT
            .getValue());    //过滤掉待审批状态 - chenjw 20150120 crm00060180

        // 仅配置被别的团队受理，团队ID置-1
        if (this.isOnlyCfgByOtherOrg(vo, areaId, orgId)) {
            vo.setOrgId(-1L);
        }
        // 设置本地网
        vo.setAreaId(CasSessionContext.getContext().getAreaId());
        // 未配置可受理，也未配置被受理，将当前团队传下去
        if (isCfgNone(vo, areaId, orgId)) {
            List<Long> tmpOrgList = new ArrayList<Long>();
            tmpOrgList.add(orgId);
            vo.setOrgAssignOrg(tmpOrgList);
        }
        PageInfo pageInfo = preSaleOrderPoolService.queryPreSaleOrderPool(vo);
        if (pageInfo != null && CollectionUtils.isNotEmpty(pageInfo.getList())) {
            List<PreSaleOrderPoolDTO> preSaleOrderPoolDTOs = CrmBeanUtils
                .copyList(pageInfo.getList(), PreSaleOrderPoolDTO.class);
            //优化：批量查出staffName/commonRegionName/orgName等
            Set<Long> staffIds = new HashSet<>();
            Set<Long> regionIds = new HashSet();
            Set<Long> orgIds = new HashSet();
            for (PreSaleOrderPoolDTO dto : preSaleOrderPoolDTOs) {
                staffIds.add(dto.getCreateStaff());
                staffIds.add(dto.getStaffId());
                regionIds.add(dto.getRegionCd());
                orgIds.add(dto.getOrgId());
            }
            Map<Long,String> staffNameMap = casSysFacade.queryStaffNames(staffIds);
            Map<Long,String> orgNameMap = casSysFacade.queryOrgNames(orgIds);
            Map<Long,String> regionNameMap = casSysFacade.queryRegionNames(regionIds);

            for (PreSaleOrderPoolDTO dto : preSaleOrderPoolDTOs) {
                PreSaleOrder preSaleOrder = preSaleOrderService
                    .getByIdAndShardingId(dto.getPreOrderId(), dto.getPreOrderId());
                if (preSaleOrder != null) {
                    //受理单号
                    dto.setPreOrderNbr(preSaleOrder.getPreOrderNumber());
                    //最后一单标识
                    dto.setIfEnd(preSaleOrder.getIfEnd());
                    //订单状态
                    dto.setOrderStatusCdName(CrmClassUtils
                        .getAttrValueNameByValue(CasConstant.PRE_SALE_ORDER.getValue(),
                            CasConstant.STATUS_CD.getValue(), preSaleOrder.getStatusCd()));
                    //产品
                    if (!StringUtils.isNullOrEmpty(preSaleOrder.getProdType())) {
                        String prodType = CrmClassUtils
                            .getAttrValueNameByValue(CasConstant.PRE_SALE_ORDER.getValue(),
                                CasConstant.PROD_TYPE.getValue(), preSaleOrder.getProdType());
                        dto.setProdType(prodType);
                    }
                    //动作
                    if (!StringUtils.isNullOrEmpty(preSaleOrder.getActionType())) {
                        String actionType = CrmClassUtils
                            .getAttrValueNameByValue(CasConstant.PRE_SALE_ORDER.getValue(),
                                CasConstant.ACTION_TYPE.getValue(), preSaleOrder.getActionType());
                        dto.setActionType(actionType);
                    }
                    //批量单数据
                    dto.setBatchAmount(preSaleOrder.getBatchAmount());
                    //预收费用 调接口  2.0代码：com.ffcs.crm2.order.dominate.PreSaleOrder#getPreFee
                    Long staffPositionId = this
                        .getStaffPositionId(preSaleOrder.getStaffId(), preSaleOrder.getOrgId());
                    Long preFee = preSaleOrderFacade
                        .getRemainPreFeeFromHb(preSaleOrder.getPreOrderNumber(),
                            preSaleOrder.getOrgId(),
                            NumberUtils.nullToLongZero(staffPositionId));
                    dto.setPreFee(NumberUtils.nullToDoubleZero(preFee) / 100.00);
                    if ("rec".equals(preSaleOrderPoolDTO.getFrom())) {
                        //是否展示"设置为全部受理"按钮   ——用于工单池接收界面
                        if (!preSaleOrderFacade
                            .existsPreSaleOrder(preSaleOrder.getPreOrderNumber(), null)) {
                            dto.setAllAcceptFlag(IntfConstant.RET_TRUE.getValue());
                        }
                    }
                    //预受理员工联系电话 --发送短信功能使用
                    dto.setMobilePhone(preSaleOrder.getMobilePhone());
                    //客户名称
                    dto.setCustName(preSaleOrder.getCustName());
                }
                //业务类型名称
                dto.setSceneTypeName(CrmClassUtils
                    .getAttrValueNameByValue(CasConstant.PRE_SALE_ORDER.getValue(),
                        CasConstant.SCENE_TYPE.getValue(),dto.getSceneType()));
                //工单池状态
                dto.setStatusCdName(CrmClassUtils
                    .getAttrValueNameByValue(CasConstant.PRE_SALE_ORDER_POOL.getValue(),
                        CasConstant.STATUS_CD.getValue(), dto.getStatusCd()));
                //预受理员工
                if (!StringUtils.isNullOrEmpty(dto.getCreateStaff())) {
                    dto.setCreateStaffName(staffNameMap.get(dto.getCreateStaff()));
                }
                //预受理团队
                if (!StringUtils.isNullOrEmpty(dto.getOrgId())) {
                    dto.setOrgName(orgNameMap.get(dto.getOrgId()));
                }
                //甩单操作点
                dto.setRegionCdName(regionNameMap.get(dto.getRegionCd()));
                //接收员工
                if (!StringUtils.isNullOrEmpty(dto.getStaffId())) {
                    dto.setStaffName(staffNameMap.get(dto.getStaffId()));
                }
                //是否展示“设置为部分受理”按钮，逻辑：状态为“已处理”并且是福州地区（此区域做成可配置）才展示   ——用于工单池界面
                AttrSpec attrSpec = CrmClassUtils
                    .getAttrSpecByCode(CasConstant.PRE_SALE_ORDER_POOL.getValue(),
                        CasConstant.PART_DEAL_AREA.getValue());
                if (attrSpec != null && attrSpec.getDefaultValue().equals(dto.getAreaId() + "")
                    && CasConstant.PRE_POOL_STATUS_CD_DEALED.getValue().equals(dto.getStatusCd())) {
                    dto.setPartAcceptFlag(IntfConstant.RET_TRUE.getValue());
                }

            }
            pageInfo.setList(preSaleOrderPoolDTOs);
        }
        return pageInfo;
    }

    /**
     * 是否仅配置被别的团队受理.
     *
     * @param vo
     * @param areaId
     * @param orgId
     * @return
     */
    private boolean isOnlyCfgByOtherOrg(PreSaleOrderPoolVo vo, Long areaId, Long orgId) {
        if (CollectionUtils.isEmpty(vo.getOrgAssignOrg()) && CollectionUtils
                .isEmpty(vo.getC4AssignOrg()) && CollectionUtils.isEmpty(vo.getC3AssignOrg())) {
            if (vo.getNotInOrgAssignOrg() != null && vo.getNotInOrgAssignOrg().contains(orgId) || vo.getNotInC4AssignOrg() != null && vo.getNotInC4AssignOrg().contains(
                    CrmSessionContext.getContext().getRegionId())
                    || vo.getNotInC3AssignOrg() != null && vo.getNotInC3AssignOrg().contains(areaId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否未配置.
     * 既没有配置可以受理别的团队，也没有配置本团队可以被别的团队受理.
     */
    public boolean isCfgNone(PreSaleOrderPoolVo vo, Long areaId, Long orgId) {
        if (CollectionUtils.isEmpty(vo.getOrgAssignOrg())
                && CollectionUtils.isEmpty(vo.getC4AssignOrg())
                && CollectionUtils.isEmpty(vo.getC3AssignOrg())) {
            if ((vo.getNotInOrgAssignOrg() == null
                    || !vo.getNotInOrgAssignOrg().contains(orgId))
                    && (vo.getNotInC4AssignOrg() == null || !vo.getNotInC4AssignOrg().contains(CrmSessionContext.getContext().getRegionId()))
                    && (vo.getNotInC3AssignOrg() == null || !vo.getNotInC3AssignOrg().contains(areaId))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AcrossOrgDTO getAcrossOrgInit(Long areaId, Long orgId) {
        // 初始化获取所有工单池配置
        List<InteractionAssignOrgDTO> allAssignOrgs = this.initAllAssignOrgs(areaId);
        // 设置可受理的目标团队或区域列表
        AcrossOrgDTO acrossOrgDTO = this
            .setDestOrgListOrRegionList(allAssignOrgs, orgId, areaId);
        return acrossOrgDTO;
    }

    /**
     * 跨团队受理处理初始化.
     *
     */
    private PreSaleOrderPoolVo convertAcrossOrgToVo(AcrossOrgDTO acrossOrgDTO) {
        PreSaleOrderPoolVo preSaleOrderPoolVo = new PreSaleOrderPoolVo();
        preSaleOrderPoolVo.setOrgAssignOrg(acrossOrgDTO.getOrgAssignOrg());
        preSaleOrderPoolVo.setC4AssignOrg(acrossOrgDTO.getC4AssignOrg());
        preSaleOrderPoolVo.setC3AssignOrg(acrossOrgDTO.getC3AssignOrg());
        preSaleOrderPoolVo.setNotInOrgAssignOrg(acrossOrgDTO.getNotInOrgAssignOrg());
        preSaleOrderPoolVo.setNotInC4AssignOrg(acrossOrgDTO.getNotInC4AssignOrg());
        preSaleOrderPoolVo.setNotInC3AssignOrg(acrossOrgDTO.getNotInC3AssignOrg());
        preSaleOrderPoolVo.setSceneTypes(acrossOrgDTO.getSceneTypes());
        return preSaleOrderPoolVo;
    }

    /**
     * 初始化获取所有工单池配置.
     *
     * @author chenjw
     * 2014-6-30
     */
    public List<InteractionAssignOrgDTO> initAllAssignOrgs(Long areaId) {
        // 新增开关，默认不使用缓存 - chenjw 20150422 crm00061844
        InteractionAssignOrgDTO interactionAssignOrgDTO = new InteractionAssignOrgDTO();
        if (!CrmClassUtils.hasSwitch("switch_crm00061844")) {
            interactionAssignOrgDTO.setAreaId(areaId);
        }
        return interactionAssignOrgFacade.queryAllAssignOrgs(interactionAssignOrgDTO);
    }

    /**
     * 设置可受理的目标团队或区域列表.
     *
     * @author chenjw
     * 2014-6-25
     */
    public AcrossOrgDTO setDestOrgListOrRegionList(List<InteractionAssignOrgDTO> allAssignOrgs, Long orgId, Long areaId) {
        AcrossOrgDTO acrossOrgDTO = new AcrossOrgDTO();

        List<Long> orgAssignOrg = new ArrayList<>();
        List<Long> c4AssignOrg = new ArrayList<>();
        List<Long> c3AssignOrg = new ArrayList<>();
        List<Long> notInOrgAssignOrg = new ArrayList<>();
        List<Long> notInC3AssignOrg = new ArrayList<>();
        List<Long> notInC4AssignOrg = new ArrayList<>();
        List<String> sceneTypes = new ArrayList<>();    //受理类型列表
        // 根据值是否为空设置各个列表
        if (allAssignOrgs != null && allAssignOrgs.size() > 0) {
            for (InteractionAssignOrgDTO assignOrgDTO : allAssignOrgs) {

                // 初始化当前团队可以受理的目标团队或区域--begin
                Long curOrgId = orgId;
                if (curOrgId != null && assignOrgDTO.getExecuteOrgId() != null
                        && curOrgId.equals(assignOrgDTO.getExecuteOrgId())) {
                    // 录单团队非空
                    if (!StringUtils.isNullOrEmpty(assignOrgDTO.getOrgId())) {
                        orgAssignOrg.add(assignOrgDTO.getOrgId());
                    } else if (!StringUtils.isNullOrEmpty(assignOrgDTO.getCfourRegionId())) {
                        c4AssignOrg.add(assignOrgDTO.getCfourRegionId());
                    } else if (!StringUtils.isNullOrEmpty(assignOrgDTO.getCommonRegionId())) {
                        // C3区域非空
                        c3AssignOrg.add(assignOrgDTO.getCommonRegionId());
                    } else {
                        throw new RtManagerException("初始化失败！工单池配置中，存在录单团队、C4区域和C3区域均为空的记录！\ninteraction_assign_org_id:" +
                                assignOrgDTO.getInteractionAssignOrgId());
                    }
                }
                // 初始化工单池配置中，所有有目标团队的配置(含本团队可受理的配置，用于去重)
                // 仅添加本地网下的团队
                if (assignOrgDTO.getOrgId() != null) {
                    OrganizationDTO orgDTO = casSysFacade.getById(assignOrgDTO.getOrgId());
                    if (orgDTO != null && orgDTO.getCommonRegionId() != null) {
                        CommonRegionDTO regionDTO = casSysFacade.getCommonRegionById(orgDTO.getCommonRegionId());
                        List<CommonRegionDTO> list = casSysFacade.queryRegionLeafToRoot(regionDTO.getCommonRegionId());
                        if (list != null && list.size() > 0) {
                            for (CommonRegionDTO tmp : list) {
                                if (tmp.getCommonRegionId().equals(areaId)) {
                                    notInOrgAssignOrg.add(assignOrgDTO.getOrgId());
                                    break;
                                }
                            }
                        }
                    }
                }
                // edit by chenjw 20141230 crm00059799
                if (CrmClassUtils.hasSwitch("switch_crm00059799")) {
                    if (assignOrgDTO.getCfourRegionId() != null
                            && assignOrgDTO.getOrgId() == null) {
                        notInC4AssignOrg.add(assignOrgDTO.getCfourRegionId());
                    }
                    if (assignOrgDTO.getCommonRegionId() != null
                            && assignOrgDTO.getCfourRegionId() == null
                            && assignOrgDTO.getOrgId() == null) {
                        notInC3AssignOrg.add(assignOrgDTO.getCommonRegionId());
                    }
                } else {
                    // 初始化工单池配置中，所有有C4区域的配置(含本团队可受理的配置，用于去重)
                    if (assignOrgDTO.getCfourRegionId() != null) {
                        notInC4AssignOrg.add(assignOrgDTO.getCfourRegionId());
                    }
                    // 初始化工单池配置中，所有有C3区域的配置
                    if (assignOrgDTO.getCommonRegionId() != null) {
                        notInC3AssignOrg.add(assignOrgDTO.getCommonRegionId());
                    }
                }

            }
        }
        // 设置受理类型列表
        if (orgId != null) {
            OrgSceneTypeRelVo vo = new OrgSceneTypeRelVo();
            vo.setOrgId(orgId);
            List<OrgSceneTypeRelDTO> orgSceneTypeRelDTOs = orgSceneTypeRelFacade
                    .queryOrgSceneTypeRel(vo);
            if (orgSceneTypeRelDTOs != null && orgSceneTypeRelDTOs.size() > 0) {
                for (OrgSceneTypeRelDTO rel : orgSceneTypeRelDTOs) {
                    if (!StringUtils.isNullOrEmpty(rel.getSceneType())) {
                        sceneTypes.add(rel.getSceneType());
                    }
                }
            }
        }
        acrossOrgDTO.setOrgAssignOrg(orgAssignOrg);
        acrossOrgDTO.setC4AssignOrg(c4AssignOrg);
        acrossOrgDTO.setC3AssignOrg(c3AssignOrg);
        acrossOrgDTO.setNotInOrgAssignOrg(notInOrgAssignOrg);
        acrossOrgDTO.setNotInC4AssignOrg(notInC4AssignOrg);
        acrossOrgDTO.setNotInC3AssignOrg(notInC3AssignOrg);
        acrossOrgDTO.setSceneTypes(sceneTypes);
        return acrossOrgDTO;
    }

    @Override
    public int savePreSaleOrderPoolAcceptBatch(List<PreSaleOrderPoolDTO> dtos, String staffId) {
        int result = 0;
        if (dtos != null && dtos.size() > 0) {
            for (PreSaleOrderPoolDTO dto : dtos) {
                //可指派的工单状态包括未接收、已接收和锁定！并且派单对象不能是当前接收员工！
                if (IntfConstant.PRE_POOL_STATUS_CD_ACCEPTED.getValue()
                        .equals(dto.getStatusCd())
                        || IntfConstant.PRE_POOL_STATUS_CD_NOT_ACCEPTED.getValue()
                        .equals(dto.getStatusCd())
                        || IntfConstant.PRE_POOL_STATUS_CD_LOCK.getValue()
                        .equals(dto.getStatusCd())) {
                    if (dto.getStaffId() != null && staffId
                            .equals(StringUtils.strnull(dto.getStaffId()))) {
                        continue;
                    }
                    dto.setStaffId(NumberUtils.toLong(staffId));
                    dto.setAcceptOrgId(CasSessionContext.getContext().getOrgId());
                    result += savePreSaleOrderPoolAccept(dto);
                }
            }
            if (result <= 0) {
                ExceptionUtils.throwEx("当前选择的记录中，不存在可指派的工单！可指派的工单状态包括未接收、已接收和锁定！并且派单对象不能是当前接收员工！");
            }
        } else {
            ExceptionUtils.throwEx("派单失败，请联系维护人员！");
        }
        return result;
    }

    @Override
    public int returnToAccept(PreSaleOrderPoolDTO preSaleOrderPoolDTO) {
        int result = 0;
        Map param = new HashMap();
        param.put("preOrderId", preSaleOrderPoolDTO.getPreOrderId());
        List<PreSaleOrder> orders = preSaleOrderService.queryPreSaleOrderList(param);
        if (orders != null && orders.size() > 0) {
            PreSaleOrder preSaleOrder = orders.get(0);
            preSaleOrder.setIfEnd("");
            preSaleOrder.setStatusCd(IntfConstant.PRE_SALE_ORDER_STATUS_CD_PART_DEAL.getValue());
            //状态变更通知移动客户端
            PreSaleOrderDTO preSaleOrderDTO = new PreSaleOrderDTO();
            CrmBeanUtils.applyIf(preSaleOrderDTO, preSaleOrder);
            RetVo retVo = preSaleOrderFacade.sendStateChangeToPad(preSaleOrderDTO);
            if (!retVo.getResult()) {
                ExceptionUtils.throwEx(retVo.getMsgTitle());
                return result;
            }

            //预受理订单保存
            result += preSaleOrderService.update(preSaleOrder);

            PreSaleOrderPool preSaleOrderPool = new PreSaleOrderPool();
            CrmBeanUtils.applyIf(preSaleOrderPool, preSaleOrderPoolDTO);
            if (StringUtils.isNullOrEmpty(preSaleOrderPoolDTO.getStaffId())) {
                preSaleOrderPool.setStatusCd(IntfConstant.PRE_POOL_STATUS_CD_NOT_ACCEPTED.getValue());
            } else {
                preSaleOrderPool.setStatusCd(IntfConstant.PRE_POOL_STATUS_CD_ACCEPTED.getValue());
            }
            //工单池记录保存
            result += preSaleOrderPoolService.update(preSaleOrderPool);

            //预受理订单过程处理属性保存
            StaffDTO staffDTO = new StaffDTO();
            staffDTO.setStaffId(CasSessionContext.getContext().getStaffId());
            Staff staff = Staff.repository().getById(NumberUtils.nullToLongZero(CasSessionContext.getContext().getStaffId()));
            staffDTO.setStaffName(staff != null ? staff.getStaffName() : "");
            result += preSaleOrderProcService.createPreSaleOrderProc(preSaleOrder, NumberUtils.toLong(IntfConstant.PRE_PROC_EVENT_ID_SET_PART.getValue()), null, staffDTO);
        }
        return result;
    }


    @Override
    public List<StaffDTO> queryStaffList(Long orgId) {
        return casSysFacade.queryStaffList(orgId);
    }

    @Override
    public boolean checkPhotoNotReDeal(PreSaleOrderDTO preSaleOrderDTO) {
        Long seq = preSaleOrderDTO.getSeq();
        boolean result = false;
        Long extCustOrderId = null;
        String preOrderNumber = preSaleOrderDTO.getPreOrderNumber();
        if (seq != null && seq != 0) {
            extCustOrderId = preSaleOrderDTO.getExtCustOrderId();
        } else {
            PreSaleOrder preSaleOrder = preSaleOrderService.queryFirstPreOrderByOrderNumber(preSaleOrderDTO.getPreOrderNumber());
            if (preSaleOrder != null) {
                extCustOrderId = preSaleOrder.getExtCustOrderId();
            }
        }
        String extCustOrderIds = preSaleOrderFacade.getExtCustOrderIds(preOrderNumber);
        RetVo retVo = casToCrmFacade.checkPhotoNotReDeal(extCustOrderId + "", extCustOrderIds);
        if (!retVo.getResult()) {
        	ExceptionUtils.throwEx("设置失败！" + retVo.getMsgTitle());
        }
        if (retVo.getObject() != null) {
            CheckPhotoNotReDealOutDTO outDTO = (CheckPhotoNotReDealOutDTO) retVo.getObject();
            if ("0".equals(outDTO.getIsPhoto())) {
                result = true;
            }
        }

        return result;
    }

    /**
     * 设置为全部受理.
     *
     * @author YULIYAO 2016/2/24
     * return
     */
    @Override
    public boolean completePool(PreSaleOrderPoolDTO preSaleOrderPoolDTO) {
        PreSaleOrderPool preSaleOrderPool = new PreSaleOrderPool();
        CrmBeanUtils.applyIf(preSaleOrderPool, preSaleOrderPoolDTO);
        PreSaleOrderDTO preSaleOrderDTO = preSaleOrderFacade.getById(preSaleOrderPool.getPreOrderId(),preSaleOrderPool.getShardingId());
        if (preSaleOrderDTO == null) {
            return false;
        }
        //1.验证全部受理约束条件
        if (CrmClassUtils.hasSwitch("switch_crm00059732")) {
            // 1.1 如果当前P订单仅仅关联一张正式订单，并且这张正式订单还没有二次受理（包括拍照甩单二次受理但是没有关联其他业务），则限制设置
            if (preSaleOrderDTO != null && checkPhotoNotReDeal(preSaleOrderDTO)
                    && !CasConstant.PRE_SALE_ORDER_STATUS_CD_CANCEL_COMPLETE.getValue()
                    .equals(preSaleOrderDTO.getStatusCd())) {
                ExceptionUtils.throwEx("设置失败！当前拍照甩单未关联任何其他业务，不能标识为全部受理，请稍候再试");
                return false;
            }
        }
        //1.2 crm00061150判断是否已经有关联Intf_Dep_Order,如果是true，则不允许设置
        CheckRelIntfDepOrderInDTO intfDepDTO = new CheckRelIntfDepOrderInDTO();
        intfDepDTO.setPreOrderNumber(preSaleOrderDTO.getPreOrderNumber());
        RetVo retVo = casToCrmFacade.checkRelIntfDepOrder(intfDepDTO);
        if (retVo.getObject() != null) {
            CheckRelIntfDepOrderOutDTO intfDepOutDTO = (CheckRelIntfDepOrderOutDTO) retVo
                    .getObject();
            if ("0".equals(intfDepOutDTO.getIsRel())) {
                ExceptionUtils.throwEx("设置失败！当前甩单有关联集团订单正在处理，当前订单不能标识为最后一张单,请稍候再试！");
                return false;
            }
        }

        //1.3 如果有预收费用，并且还没有全部支出，则不允许设置为全部受理
        PreSaleOrderVo vo = new PreSaleOrderVo();
        CrmBeanUtils.applyIf(vo, preSaleOrderPoolDTO);
        Long remainPreFee = preSaleOrderFacade.getRemainPreFeeFromHb(vo.getPreOrderNumber(), vo.getOrgId(), vo.getStaffPositionId());
        if (!StringUtils.isNullOrEmpty(remainPreFee) && remainPreFee > 0) {
            ExceptionUtils.throwEx("设置失败！当前订单还有预收费用，请先关联最后一张订单，或者撤销退费！");
            return false;
        }

        //2.判断预受理单关联的正式单全部都进二表，如果是则将P订单状态设置为已竣工，并将P订单以及相关记录挪到二表，否则判断是否存在已竣工订单，如果存在，则P订单状态设置为部分竣工，通知移动客户端部分竣工，否则P订单状态设置全部受理
        QueryCustOrderOutDTO orderOutDTO = preSaleOrderFacade.queryCustOrder(preSaleOrderDTO.getPreOrderNumber(), null, "0");
        if (ArrayUtils.isEmpty(orderOutDTO.getCustomerOrder())) {
            //2.1 全部进二表，将P订单状态设置为已竣工，并将P订单以及相关记录挪到二表
            preSaleOrderDTO.setIfEnd("1");
            if (preSaleOrderFacade.existCompleteOrder(preSaleOrderDTO.getPreOrderNumber(), null)) {
                preSaleOrderDTO
                        .setStatusCd(CasConstant.PRE_SALE_ORDER_STATUS_CD_COMPLETE.getValue());
            } else {
                preSaleOrderDTO.setStatusCd(
                        CasConstant.PRE_SALE_ORDER_STATUS_CD_CANCEL_COMPLETE.getValue());
            }
            preSaleOrderFacade.sendStateChangeToPad(preSaleOrderDTO);
            preSaleOrderFacade.remove(preSaleOrderDTO);
            // 设置为全部受理，如果移到二表，则往预受理过程历史表新增记录 - chenjw 20150518 crm00062196
            PreSaleOrderProcHis preSaleOrderProcHis = new PreSaleOrderProcHis();
            Long hisId = CrmEntityUtils.getSeq("SEQ_PRE_SALE_ORDER_PROC_HIS_ID");
            preSaleOrderProcHis.setHisId(hisId);
            IDdlService service = (IDdlService) BeanLoader
                    .getBean(IDdlService.class);
            preSaleOrderProcHis.setPreSaleOrderProcId(service.getSeq("SEQ_PRE_SALE_ORDER_PROC_ID"));
            preSaleOrderProcHis.setShardingId(preSaleOrderDTO.getPreOrderId());
            preSaleOrderProcHis.setCustomerInteractionEventId(
                    NumberUtils.nullToLongZero(CasConstant.PRE_PROC_EVENT_ID_ALL_DONE.getValue()));
            String remark = CrmClassUtils.getAttrValueNameByValue(
                    PreSaleOrderProc.class.getSimpleName(),
                    "customerInteractionEventId",
                    CasConstant.PRE_PROC_EVENT_ID_ALL_DONE.getValue());
            preSaleOrderProcHis.setRemark(remark);
            preSaleOrderProcHis.setStaffId(CasSessionContext.getContext().getStaffId());
            Long staffId = CasSessionContext.getContext().getStaffId();
            if (staffId != null && staffId > 0) {
                StaffDTO staffDTO = casSysFacade
                        .getStaffById(staffId);
                preSaleOrderProcHis.setHandlePeopleName(staffDTO.getStaffName());
            }
            preSaleOrderProcHis.setAcceptTime(new Timestamp(new Date().getTime()));
            preSaleOrderProcHis.setStatusCd(CasConstant.STATUS_CD_VAILID.getValue());
            //以下字段底层会自动保存
            /*preSaleOrderProcHis.setCreateStaff(CasSessionContext.getStaffId());
            preSaleOrderProcHis.setRegionCd(CrmSessionContext.getContext().getRegionId());
			preSaleOrderProcHis
					.setAreaId(NumberUtils.nullToLongZero(SessionContext.getValue("areaId")));
			preSaleOrderProcHis.setCreateDate(new Timestamp(new Date().getTime()));
			preSaleOrderProcHis.setUpdateDate(new Timestamp(new Date().getTime()));
			preSaleOrderProcHis.setStatusDate(new Timestamp(new Date().getTime()));*/
            preSaleOrderProcHis.save();
        } else {
            //2.2 判断是否存在已竣工订单，如果存在，则P订单状态设置为部分竣工，通知移动客户端部分竣工，否则P订单状态设置全部受理
            if (!preSaleOrderFacade.existsPreSaleOrder(preSaleOrderDTO.getPreOrderNumber(), null)) {
                preSaleOrderDTO.setIfEnd("1");
                preSaleOrderPool.setStatusCd(CasConstant.PRE_POOL_STATUS_CD_DEALED.getValue());
                preSaleOrderPool.update();
                if (preSaleOrderFacade.existCompleteOrder(preSaleOrderDTO.getPreOrderNumber(), null)) {
                    preSaleOrderDTO.setStatusCd(CasConstant.PRE_SALE_ORDER_STATUS_CD_COMPLETING.getValue());
                } else {
                    preSaleOrderDTO.setStatusCd(CasConstant.PRE_SALE_ORDER_STATUS_CD_ON_WAY.getValue());
                }
                //crm00064173 zhangyangyi 15-09-09 判断如果有未缴费的订单，则不通知实名制客户端修改状态
                preSaleOrderFacade.sendStateChangeToPad(preSaleOrderDTO);
                PreSaleOrder preSaleOrder = new PreSaleOrder();
                CrmBeanUtils.applyIf(preSaleOrder, preSaleOrderDTO);
                preSaleOrder.update();
                // 设置为全部受理时，新增预受理过程 - chenjw 20150518 crm00062196
                PreSaleOrderProc preSaleOrderProc = new PreSaleOrderProc(true);
                String remark = CrmClassUtils.getAttrValueNameByValue(
                        PreSaleOrderProc.class.getSimpleName(),
                        "customerInteractionEventId",
                        CasConstant.PRE_PROC_EVENT_ID_ALL_DONE.getValue());
                preSaleOrderProc.setRemark(remark);
                preSaleOrderProc.setStaffId(CasSessionContext.getContext().getStaffId());
                StaffDTO staffDTO = casSysFacade
                        .getStaffById(CasSessionContext.getContext().getStaffId());
                if (staffDTO != null) {
                    preSaleOrderProc.setHandlePeopleName(staffDTO.getStaffName());
                }
                preSaleOrderProc.setAcceptTime(new Timestamp(new Date().getTime()));
                preSaleOrderProc.setStatusCd(CasConstant.STATUS_CD_VAILID.getValue());
                preSaleOrderProc.setPreOrderId(preSaleOrderDTO.getPreOrderId());
                preSaleOrderProc.setShardingId(preSaleOrderDTO.getShardingId());
                preSaleOrderProc.save();
            } else {
                ExceptionUtils.throwEx("设置失败！存在预受理状态的正式订单，请先处理");
                return false;
            }
        }
        return true;
    }


    /**
     * 继续受理
     *
     * @author YULIYAO 2016/2/25
     * return
     */
    @Override
    public String continueAccept(Long extCustOrderId) {
        //1.判断是否允许继续受理
        if (extCustOrderId == null) {
            ExceptionUtils.throwEx("预受理工单对应的正式单ID为空！");
        }
        //调customerOrder扩展属性isNeedSecondDeal查询是否需要二次受理  com/ffcs/crm2/order/manager/impl/PreSaleOrderManagerImpl.java:685
        RetVo retVo = casToCrmFacade.continueAcceptConfirm(extCustOrderId);
        if (!retVo.getResult()) {
            ExceptionUtils.throwEx(retVo.getMsgTitle());
        }
        return StringUtils.strnull(retVo.getObject());
    }


    /**
     * 根据预受理编号查询正式单，含一表和二表
     *
     * @author YULIYAO 2016/2/27
     * return
     */
    @Override
    public List<CustomerOrderDTO> queryCustomerOrder(String preOrderNumber) {
        List<CustomerOrderDTO> resultList = new ArrayList<>();
        QueryCustOrderOutDTO orderOutDTO = preSaleOrderFacade
                .queryCustOrder(preOrderNumber, null, "2");
        if (ArrayUtils.isNotEmpty(orderOutDTO.getCustomerOrder())) {
            List<CustomerOrder> customerOrderList = CollectionUtils.arrayToList(orderOutDTO.getCustomerOrder());
            resultList.addAll(CrmBeanUtils.copyList(customerOrderList, CustomerOrderDTO.class));
        }
        if (ArrayUtils.isNotEmpty(orderOutDTO.getCustomerOrderHis())) {
            List<CustomerOrder> customerOrderHises = CollectionUtils
                    .arrayToList(orderOutDTO.getCustomerOrderHis());
            resultList.addAll(CrmBeanUtils.copyList(customerOrderHises, CustomerOrderDTO.class));
        }
        return resultList;
    }

    /**
     * 退单
     *
     * @author YULIYAO 2016/2/29
     * return
     */
    @Override
    public String goBack(PreSaleOrderPoolDTO preSaleOrderPoolDTO, AttrValueDTO attrValueDTO, String staffName,
                         String staffPhone, String backReason) {
        String result = "";
        //1.判断传入值
        if (StringUtils.isNullOrEmpty(staffName)) {
            ExceptionUtils.throwEx("请填写姓名！");
        }
        if (StringUtils.isNullOrEmpty(staffPhone)) {
            ExceptionUtils.throwEx("请填写联系电话！");
        }
        if (StringUtils.isNullOrEmpty(backReason)) {
            ExceptionUtils.throwEx("请填写退单原因！");
        }
        if (attrValueDTO == null) {
            ExceptionUtils.throwEx("请选择退单原因类型！");
        }

        // 将旧的退单原因保留
        String oldReason = "";
        if (!StringUtils.isNullOrEmpty(getRemarkContent(preSaleOrderPoolDTO.getRemark(), CasConstant.GO_BACK_TAG.getValue()))) {
            oldReason = getRemarkContent(preSaleOrderPoolDTO.getRemark(),
                    CasConstant.GO_BACK_TAG_REASON.getValue());
            cleanGoBackContent(preSaleOrderPoolDTO);
        }
        String reason = oldReason + "\n----" + DateUtils.date2Str(new Date())
                + "----" + staffName + "----\n" + backReason;
        preSaleOrderPoolDTO.setRemark(
                getNewRemark(preSaleOrderPoolDTO.getRemark(), staffName, staffPhone, reason));

        // 创建并保存退单处理过程 - crm00060466
        Long eventId = NumberUtils
                .nullToLongZero(CasConstant.PRE_PROC_EVENT_ID_POOL_RET.getValue());
        List<AttrValueDTO> attrValueDTOs = new ArrayList<>();
        attrValueDTOs.add(attrValueDTO);
        preSaleOrderFacade.createPreProc(preSaleOrderPoolDTO.getPreOrderId(),preSaleOrderPoolDTO.getShardingId(), CasSessionContext.getContext().getStaffId(), attrValueDTOs, eventId, backReason);
        // 取配置判断是否需要班长审核
        if (CrmClassUtils.hasSwitch("retOrderNeedAudit")) {
            preSaleOrderPoolDTO.setStatusCd(CasConstant.PRE_POOL_STATUS_CD_GOBACKING.getValue());
            preSaleOrderPoolDTO.setUpdateDate(new Timestamp(new Date().getTime()));
            save(preSaleOrderPoolDTO);
            result = "处理成功！当前工单目前处于待回退状态，请联系班长及时处理！";
        } else {
            // 退单后，优先级设置为最高 - 20151031 crm00066743
            preSaleOrderPoolDTO.setPriority(1L);
            preSaleOrderPoolDTO.setStatusCd(CasConstant.PRE_POOL_STATUS_CD_GOBACKED.getValue());
            preSaleOrderPoolDTO.setUpdateDate(new Timestamp(new Date().getTime()));
            save(preSaleOrderPoolDTO);
            // 确认回退后，发送短信
            String msgContent = "";
            StringBuffer sb = new StringBuffer();
            sb.append("您的预受理甩单已被退单，请及时处理。预受理单号：");
            sb.append(preSaleOrderPoolDTO.getPreOrderNbr());
            sb.append("，CRM订单号：");
            QueryCustOrderOutDTO orderOutDTO = preSaleOrderFacade
                    .queryCustOrder(preSaleOrderPoolDTO.getPreOrderNbr(), null, "2");
            if (ArrayUtils.isNotEmpty(orderOutDTO.getCustomerOrder())) {
                for (CustomerOrder customerOrder : orderOutDTO.getCustomerOrder()) {
                    sb.append(customerOrder.getCustSoNumber() + ",");
                }
            }
            if (ArrayUtils.isNotEmpty(orderOutDTO.getCustomerOrderHis())) {
                for (CustomerOrder customerOrderHis : orderOutDTO.getCustomerOrderHis()) {
                    sb.append(customerOrderHis.getCustSoNumber() + ",");
                }
            }
            sb.append("退单原因：" + StringUtils.strnull(backReason));
            RetVo retVo = preSaleOrderFacade.sentMsg(staffPhone, sb.toString(), 1L);
            if (retVo != null && !retVo.getResult()) {
                ExceptionUtils.throwEx("当前工单成功回退！但是短信通知发送失败！");
            }
        }
        return result;
    }

    /**
     * 获取备注中指定字段值.
     *
     * @param tagName
     * @author chenjw
     * 2014-5-14
     */
    public String getRemarkContent(String remark, String tagName) {
        if (StringUtils.isNullOrEmpty(tagName) || StringUtils.isNullOrEmpty(remark)) {
            return "";
        }
        int startIndex = -1;
        int endIndex = -1;
        final String str_maskStartStr = "<" + tagName + ">";
        final String str_maskEndStr = "</" + tagName + ">";

        if (remark != null) {
            startIndex = remark.indexOf(str_maskStartStr);
            endIndex = remark.indexOf(str_maskEndStr);
            if (startIndex != -1) {
                final int contentStart = remark.indexOf('>', startIndex) + 1;
                return remark.substring(contentStart, endIndex);
            }
        }
        return "";
    }

    /**
     * 清空退单信息.
     *
     * @return
     * @author chenjw
     * 2014-5-14
     */
    public void cleanGoBackContent(PreSaleOrderPoolDTO preSaleOrderPoolDTO) {
        if (!StringUtils.isNullOrEmpty(preSaleOrderPoolDTO.getRemark())) {
            String beginMark = "<" + CasConstant.GO_BACK_TAG.getValue() + ">";
            int beginIndex = -1;
            beginIndex = preSaleOrderPoolDTO.getRemark().indexOf(beginMark);
            if (beginIndex != -1) {
                preSaleOrderPoolDTO.setRemark(preSaleOrderPoolDTO.getRemark().subSequence(0, beginIndex).toString());
            }

        }
    }

    /**
     * 生成新的备注
     *
     * @author YULIYAO 2016/2/29
     * return
     */
    private String getNewRemark(String remark, String staffName, String staffPhone, String backReason) {
        if (!StringUtils.isNullOrEmpty(remark)) {
            String beginMark = "<" + CasConstant.GO_BACK_TAG.getValue() + ">";
            int beginIndex = -1;
            beginIndex = remark.indexOf(beginMark);
            if (beginIndex != -1) {
                remark = remark.subSequence(0, beginIndex).toString();
            }
        }
        StringBuffer sbf = new StringBuffer();
        sbf.append("<" + CasConstant.GO_BACK_TAG.getValue() + ">");
        sbf.append("<" + CasConstant.GO_BACK_TAG_STAFF_NAME.getValue() + ">");
        sbf.append(staffName);
        sbf.append("</" + CasConstant.GO_BACK_TAG_STAFF_NAME.getValue() + ">");
        sbf.append("<" + CasConstant.GO_BACK_TAG_PHONE_NUMBER.getValue() + ">");
        sbf.append(staffPhone);
        sbf.append("</" + CasConstant.GO_BACK_TAG_PHONE_NUMBER.getValue() + ">");
        sbf.append("<" + CasConstant.GO_BACK_TAG_REASON.getValue() + ">");
        sbf.append(backReason);
        sbf.append("</" + CasConstant.GO_BACK_TAG_REASON.getValue() + ">");
        sbf.append("</" + CasConstant.GO_BACK_TAG.getValue() + ">");
        return StringUtils.strnull(remark) + sbf.toString();
    }

    /**
     * 更新
     *
     * @author YULIYAO 2016/2/29
     * return
     */
    @Override
    public int update(PreSaleOrderPoolDTO preSaleOrderPoolDTO) {
        PreSaleOrderPool preSaleOrderPool = new PreSaleOrderPool();
        CrmBeanUtils.applyIf(preSaleOrderPool, preSaleOrderPoolDTO);
        return preSaleOrderPool.update();
    }

    /**
     * 接收工单
     * 接收三张工单池中“未接收”状态的订单，设置工单池记录的接收员工为当前登陆员工，设置接收团队为当前登陆团队，更新接收时间，设置“是否班长派单”标识为0，表示不是班长派单，状态设置为已接收
     *
     * @author YULIYAO 2016/3/2
     * return
     */
    @Override
    public boolean acceptOrder(Long staffId, Long orgId, Long areaId, AcrossOrgDTO acrossOrgDTO) {
        RetVo retVo = new RetVo();
        retVo.setResult(true);
        PreSaleOrderPoolDTO preSaleOrderPoolDTO = new PreSaleOrderPoolDTO();
        preSaleOrderPoolDTO.setStatusCd(CasConstant.PRE_POOL_STATUS_CD_NOT_ACCEPTED.getValue());
        preSaleOrderPoolDTO.setPageNumber(1);
        preSaleOrderPoolDTO.setPageSize(3);
        PageInfo pageInfo = this.queryPreSaleOrderPool(preSaleOrderPoolDTO, areaId, orgId, acrossOrgDTO);
        if (pageInfo != null && CollectionUtils.isNotEmpty(pageInfo.getList())) {
            for (int i = 0; i < pageInfo.getList().size(); i++) {
                PreSaleOrderPoolDTO dto = (PreSaleOrderPoolDTO) pageInfo.getList().get(i);
                dto.setStaffId(staffId);
                dto.setAcceptDate(new Timestamp(new Date().getTime()));
                dto.setIsLeaderAssign("0");
                dto.setAcceptOrgId(orgId);
                dto.setUpdateDate(new Timestamp(new Date().getTime()));
                dto.setStatusCd(CasConstant.PRE_POOL_STATUS_CD_ACCEPTED.getValue());
                update(dto);
            }
        } else {
            ExceptionUtils.throwEx("当前工单池中不存在未接收状态的工单！");
        }
        return true;
    }

    /**
     * 根据预受理单ID，查询工单池
     *
     * @author YULIYAO 2016/3/15
     * return
     */
    @Override
    public PreSaleOrderPoolDTO getByPreOrderId(Long preOrderId) {
        PreSaleOrderPoolDTO preSaleOrderPoolDTO = new PreSaleOrderPoolDTO();
        PreSaleOrderPool preSaleOrderPool = preSaleOrderPoolService.getByPreOrderId(preOrderId);
        if (preSaleOrderPool != null) {
            CrmBeanUtils.applyIf(preSaleOrderPoolDTO, preSaleOrderPool);
        }
        return preSaleOrderPoolDTO;
    }

    /**
     * 保存
     *
     * @author YULIYAO 2016/3/15
     * return
     */
    public int save(PreSaleOrderPoolDTO orderPoolDTO) {
        if (orderPoolDTO != null) {
            //新增
            if (orderPoolDTO.getPreSaleOrderPoolId() == null
                    || orderPoolDTO.getPreSaleOrderPoolId() == 0) {
                PreSaleOrderPool preSaleOrderPool = new PreSaleOrderPool(true);
                CrmBeanUtils.applyIf(preSaleOrderPool, orderPoolDTO, false);
                return preSaleOrderPool.save();
            } else {    //修改
                PreSaleOrderPool preSaleOrderPool = new PreSaleOrderPool();
                CrmBeanUtils.applyIf(preSaleOrderPool, orderPoolDTO);
                return preSaleOrderPool.update();
            }
        }
        return 0;
    }

    @Override
    public PreSaleOrderPoolHisDTO getHisPoolByPreSaleId(Long preOrderId) {
        PreSaleOrderPoolHisDTO preSaleOrderPoolHisDTO = new PreSaleOrderPoolHisDTO();
        PreSaleOrderPoolHis preSaleOrderPoolHis =
                preSaleOrderPoolService.getPreSaleOrderPoolHisByOrderId(preOrderId);
        if (preSaleOrderPoolHis != null) {
            CrmBeanUtils.applyIf(preSaleOrderPoolHisDTO, preSaleOrderPoolHis);
        }
        return preSaleOrderPoolHisDTO;
    }

    @Override
    public List<AutoAssignDTO> qryPreOrderDistribute(Long staffId) {
        List<Organization> organizations = organizationService.qryPreOrderDisList(staffId);
        if (organizations == null || organizations.size() < 1) return null;
        List<AutoAssignDTO> autoAssignDTOs = new ArrayList<>();
        for (Organization org : organizations) {
            AreaCodeDTO areaCodeDTO = casSysFacade.getC3AreaCodeByCommonRegionId(org.getCommonRegionId());
            if (areaCodeDTO == null) {
                continue;
            }
            CommonRegionDTO commonRegionDTO = casSysFacade.getCommonRegionById(areaCodeDTO.getRegionId());
            if (commonRegionDTO == null){
                continue;
            }

            PreSaleOrderPoolVo vo = convertAcrossOrgToVo(getAcrossOrgInit(commonRegionDTO.getCommonRegionId(),org.getOrgId()));
            AutoAssignDTO autoAssignDTO = new AutoAssignDTO();
            autoAssignDTO.setOrgId(org.getOrgId());
            autoAssignDTO.setRegionCd(org.getCommonRegionId());
            autoAssignDTO.setOrgName(org.getOrgName());
            List<String> unAcceptStatusCds = new ArrayList<>();
            unAcceptStatusCds.add(CasConstant.PRE_POOL_STATUS_CD_NOT_ACCEPTED.getValue());
            vo.setStatusCdList(unAcceptStatusCds);
            int unAcceptAmount = preSaleOrderPoolService.queryPreSaleOrderPoolAmount(vo);
            autoAssignDTO.setUnAcceptOrderAmount(unAcceptAmount);
            autoAssignDTOs.add(autoAssignDTO);
        }
        return autoAssignDTOs;
    }

    /**
     * 通过staff跟订单对象的orgId去获取任职资格
     *
     * @param staffId
     * @param orgId
     * @return
     */
    public Long getStaffPositionId(Long staffId, Long orgId) {
        // 通过staff跟订单对象的orgId去获取任职资格
        // 现网逻辑：com.ffcs.crm2.order.dominate.PreSaleOrder.buildPreFeeQueryInParam()
        StaffPositionDTO staffPositionDTO = new StaffPositionDTO();
        staffPositionDTO.setStaffId(staffId);
        staffPositionDTO.setOrgId(orgId);
        List<StaffPositionDTO> staffPositionDTOs = casSysFacade.queryStaffPosition(staffPositionDTO);
        if (CollectionUtils.isNotEmpty(staffPositionDTOs)) {
            return staffPositionDTOs.get(0).getStaffPositionId();
        }
        return null;
    }


    @Override
    public void uploadFiles(Long preOrderId, String fileName, byte[] bytes) {
        PreSaleOrder preSaleOrder = preSaleOrderService
                .getByIdAndShardingId(preOrderId, preOrderId);
        if (preSaleOrder == null) {
            ExceptionUtils.throwEx("预受理订单为空");
        }
        //2.遍历调接口上传
        StringBuffer errorMsgSb = new StringBuffer();
        StringBuffer successMsgSb = new StringBuffer();
        String content = "";
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        List illegalSuffixs = Arrays.asList("txt", "text", "htm", "html");
        if (illegalSuffixs.contains(suffix)) {
            errorMsgSb.append(fileName).append(":保存失败！请使用WORD格式上传");
            ExceptionUtils.throwEx(errorMsgSb.toString());
        }
        content = Base64.encodeBase64String(bytes);
        content = content.replaceAll("\r\n", "");
        String channel = CasSessionContext.getContext().getChannelCode();
        SaveDocInfoInDTO docInfoInDTO = new SaveDocInfoInDTO();
        com.ffcs.crmd.cas.bean.crmbean.saveDocInfo.DocList doc = newDocList(preSaleOrder.getPreOrderNumber(), fileName, suffix, content);
        doc.setDocInfo(content);
        doc.setUploadType("1002");
        DocList[] docLists = {doc};
        docInfoInDTO.setDocList(docLists);
        RetVo retVo = casToCrmFacade.saveDocInfo(docInfoInDTO);
        if (retVo.getResult()) {
            successMsgSb.append(fileName).append(":保存成功.");
        } else {
            errorMsgSb.append("case:" + retVo.getMsgTitle());
            errorMsgSb.append(fileName).append(";保存失败.");
        }
        //edit by laiyongmin ,调整代码逻辑
        //4.抛出异常
        if (!StringUtils.isNullOrEmpty(errorMsgSb)) {
            ExceptionUtils.throwEx(errorMsgSb.toString());
        }
        //3.保存预受理处理过程
        AttrValueDTO attrValueDTO = new AttrValueDTO();
        attrValueDTO.setAttrId(0L);
        attrValueDTO.setAttrDesc(errorMsgSb.toString());
        preSaleOrderFacade
                .createPreProc(preOrderId, preOrderId, CasSessionContext.getContext().getStaffId(), Arrays.asList(attrValueDTO),
                        NumberUtils.toLong(CasConstant.PRE_PROC_EVENT_ID_UPLOAD_FILE.getValue()),
                        successMsgSb + errorMsgSb.toString());

    }

    private DocList newDocList(String preOrderNumber, String fileName, String suffix, String content) {
        com.ffcs.crmd.cas.bean.crmbean.saveDocInfo.DocList doc = new com.ffcs.crmd.cas.bean.crmbean.saveDocInfo.DocList();
        doc.setOptType("ADD");
        //            doc.setDocId(docInfo.getDocId() + "");
        doc.setDocAppObject(preOrderNumber);
        doc.setDocAppObjectType("SALE_ORDER_NBR");
        doc.setDocName(fileName);
        doc.setFileName(fileName);
        doc.setFileFormat(suffix);
        return doc;
    }

    private PreSaleOrder validateCond(Long preOrderId, MultipartFile[] multipartFiles) {
        if (preOrderId == null || preOrderId == 0) {
            ExceptionUtils.throwEx("您未选择记录，或者所选记录没对应的预受理ID");
        }
        PreSaleOrder preSaleOrder = preSaleOrderService
                .getByIdAndShardingId(preOrderId, preOrderId);
        if (preSaleOrder == null) {
            ExceptionUtils.throwEx("预受理订单为空");
        }
        if (multipartFiles == null) {
            ExceptionUtils.throwEx("文档为空");
        }
        return preSaleOrder;
    }

    /**
     * 查询工单池初始界面区域控件
     * @author YULIYAO 2016/5/11
     * return
     */
    @Override
    public PageInfo queryInitPoolRegion(AcrossOrgDTO acrossOrgDTO, String regionName, int pageNum, int pageSize) {
        Map param = new HashMap();
        List<Long> c3AssignOrgs = acrossOrgDTO.getC3AssignOrg();
        List<Long> notInC4AssignOrgs = acrossOrgDTO.getNotInC4AssignOrg();
        List<Long> c4AssignOrgs = acrossOrgDTO.getC4AssignOrg();
        if (CollectionUtils.isNotEmpty(c3AssignOrgs)) {
            param.put("c3AssignOrgs", c3AssignOrgs);
            if (CollectionUtils.isNotEmpty(notInC4AssignOrgs)) {
                param.put("notInC4AssignOrgs", notInC4AssignOrgs);
            }
        }
        if (CollectionUtils.isNotEmpty(c4AssignOrgs)) {
            param.put("c4AssignOrgs", c4AssignOrgs);
        }
        if (!StringUtils.isNullOrEmpty(regionName)) {
            param.put("regionName", regionName);
        }
        if (CollectionUtils.isNotEmpty(c3AssignOrgs) && CollectionUtils
            .isNotEmpty(c4AssignOrgs)) {
            param.put("union", 1);
        }
        param.put("regionTypes", Arrays.asList(1300, 1499));
        PageInfo pageInfo = preSaleOrderPoolService.queryInitPoolRegion(param, pageNum, pageSize);
        if (pageInfo != null && CollectionUtils.isNotEmpty(pageInfo.getList())) {
            pageInfo.setList(CrmBeanUtils.copyList(pageInfo.getList(), CommonRegionDTO.class));
        }
        return pageInfo;
    }

	@Override
	public RetVo qryPreSaleOrderOverTimeList(InParam inParam) {
		Map<String ,Object> map = new HashMap<String ,Object>();
		map.put("beginTime", inParam.getBeginTime());
		map.put("endTime", inParam.getEndTime());
		if(!StringUtils.isNullOrEmpty(inParam.getAcceptOrgId())){
			map.put("acceptOrgId", NumberUtils.toLong(inParam.getAcceptOrgId()));
		}
		if(!StringUtils.isNullOrEmpty(inParam.getAreaId())){
			map.put("areaId", NumberUtils.toLong(inParam.getAreaId()));
		}
		if(!StringUtils.isNullOrEmpty(inParam.getStaffId())){
			map.put("staffId", NumberUtils.toLong(inParam.getStaffId()));
		}
		List<PreSaleOrderOverTimeListVo> preSaleOrderOverTimeListVos= preSaleOrderPoolService.qryPreSaleOrderOverTimeList(map);
		RetVo retVo = new RetVo();
		if(preSaleOrderOverTimeListVos != null &&  preSaleOrderOverTimeListVos.size()>0){
			List<com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderOverTimeList.PreSaleOrderDetail> PreSaleOrderDetails = CrmBeanUtils
					.copyList(preSaleOrderOverTimeListVos,
							com.ffcs.crmd.cas.bean.devops.qryPreSaleOrderOverTimeList.PreSaleOrderDetail.class);
			retVo.setResult(true);
			retVo.setObject(PreSaleOrderDetails);
		}else{
			retVo.setResult(false);
		}
		return retVo;
	}

}
