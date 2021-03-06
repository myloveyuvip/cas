package com.ffcs.crmd.cas.order.control;

import com.ctg.itrdc.platform.common.utils.type.NumberUtils;
import com.ctg.itrdc.platform.common.utils.type.StringUtils;
import com.ffcs.crmd.cas.base.api.AcrossOrgDTO;
import com.ffcs.crmd.cas.base.context.CasSessionContext;
import com.ffcs.crmd.cas.base.utils.TransUtil;
import com.ffcs.crmd.cas.constants.CasConstant;
import com.ffcs.crmd.cas.constants.IntfConstant;
import com.ffcs.crmd.cas.core.control.CrmdBaseController;
import com.ffcs.crmd.cas.order.api.dto.*;
import com.ffcs.crmd.cas.order.api.facade.IPreSaleOrderFacade;
import com.ffcs.crmd.cas.order.api.facade.IPreSaleOrderPoolFacade;
import com.ffcs.crmd.cas.sys.api.dto.AttrValueDTO;
import com.ffcs.crmd.cas.sys.api.dto.StaffDTO;
import com.ffcs.crmd.cas.sys.api.facade.ICasSysFacade;
import com.ffcs.crmd.platform.pub.ex.ExceptionUtils;
import com.ffcs.crmd.platform.pub.vo.RetVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * 预受理工单接收
 * Created by qn_guo on 2016/2/26.
 */
@Controller
@RequestMapping("/order/PreSaleOrderRec")
@ResponseBody
public class PreSaleOrderRecController extends CrmdBaseController {
    @Autowired
    private ICasSysFacade casSysFacade;
    @Autowired
    private IPreSaleOrderFacade preSaleOrderFacade;
    @Autowired
    private IPreSaleOrderPoolFacade preSaleOrderPoolFacade;

    /**
     * 退单
     *
     * @param preSaleOrderPoolDTO
     * @return
     */
    @RequestMapping("/sendBackPreSaleOrder")
    public RetVo sendBackPreSaleOrder(@RequestBody PreSaleOrderPoolDTO preSaleOrderPoolDTO) {
        try {
            RetVo retVo = new RetVo(true);
            String result = preSaleOrderPoolFacade.goBack(preSaleOrderPoolDTO, preSaleOrderPoolDTO.getAttrValueDTO(),
                    preSaleOrderPoolDTO.getStaffName(), preSaleOrderPoolDTO.getMobilePhone(),
                    preSaleOrderPoolDTO.getBackReasonMsg());
            retVo.setMsgTitle(result);
            return retVo;
        } catch (Exception e) {
            e.printStackTrace();
            RetVo retVo = new RetVo(false);
            retVo.setMsgTitle(e.getMessage());
            return retVo;
        }
    }

    /**
     * 接收工单
     *
     * @return
     */
    @RequestMapping("/recPreSaleOrder")
    public RetVo recPreSaleOrder(@RequestBody AcrossOrgDTO acrossOrgDTO) {
        try {
            RetVo retVo = new RetVo(true);
            Long staffId = CasSessionContext.getContext().getStaffId();
            Long orgId = CasSessionContext.getContext().getOrgId();
            Long areaId = CasSessionContext.getContext().getAreaId();
            boolean result = preSaleOrderPoolFacade.acceptOrder(staffId, orgId, areaId,acrossOrgDTO);
            if (!result) {
                retVo.setResult(false);
            }
            return retVo;
        } catch (Exception e) {
            e.printStackTrace();
            RetVo retVo = new RetVo(false);
            retVo.setMsgTitle(e.getMessage());
            return retVo;
        }
    }

    /**
     * 工单数,团队工单数，未被接受数
     *
     * @return
     */
    @RequestMapping("/qryPreSaleOrderRecAmount")
    public RetVo qryPreSaleOrderRecAmount(@RequestBody AcrossOrgDTO acrossOrgDTO) {
        try {
            RetVo retVo = new RetVo(true);
            PreSaleOrderPoolDTO dto = new PreSaleOrderPoolDTO();
            dto.setStatusCd(CasConstant.PRE_POOL_STATUS_CD_NOT_ACCEPTED.getValue());
            String sceneTypeStr = "101,102,103,104,105,106,107,108,109,110,";
            dto.setSceneTypeList(TransUtil.getStrListbyStr(sceneTypeStr));
            Long areaId = CasSessionContext.getContext().getAreaId();
            Long orgId = CasSessionContext.getContext().getOrgId();
            Long staffId = CasSessionContext.getContext().getStaffId();
            PreSaleOrderPoolAmountDTO amountDTO = preSaleOrderPoolFacade
                    .queryPreSaleOrderPoolReceiveAmount(acrossOrgDTO, orgId, staffId);
            StaffDTO staffDTO = casSysFacade.getStaffById(staffId);
            if (staffDTO != null && !StringUtils.isNullOrEmpty(staffDTO.getStaffName())) {
                amountDTO.setStaffName(staffDTO.getStaffName());
            }
            retVo.setObject(amountDTO);
            return retVo;
        } catch (Exception e) {
            e.printStackTrace();
            RetVo retVo = new RetVo(false);
            retVo.setMsgTitle(e.getMessage());
            return retVo;
        }
    }

    /**
     * 工单分布
     *
     * @return
     */
    @RequestMapping("/qryPreOrderDistribute")
    public RetVo qryPreOrderDistribute() {
        try {
            RetVo retVo = new RetVo(true);
            Long staffId = CasSessionContext.getContext().getStaffId();
            Long orgId = CasSessionContext.getContext().getOrgId();
            Long areaId = CasSessionContext.getContext().getAreaId();
            List<AutoAssignDTO> assignlist = preSaleOrderPoolFacade.qryPreOrderDistribute(staffId);
            if (assignlist != null && assignlist.size() > 0) {
                retVo.setDataList(assignlist);
            }
            return retVo;
        } catch (Exception e) {
            e.printStackTrace();
            RetVo retVo = new RetVo(false);
            retVo.setMsgTitle(e.getMessage());
            return retVo;
        }
    }

    /**
     * 订单信息
     *
     * @return
     */
    @RequestMapping("/qryPreSaleOrderDetail")
    public RetVo qryPreSaleOrderDetail(@RequestBody PreSaleOrderPoolDTO preSaleOrderPoolDTO) {
        try {
            RetVo retVo = new RetVo(true);
            if (!StringUtils.isNullOrEmpty(preSaleOrderPoolDTO.getPreOrderNbr())) {

                List<CustomerOrderDTO> customerOrderDTOs
                        = preSaleOrderPoolFacade.queryCustomerOrder(preSaleOrderPoolDTO.getPreOrderNbr());
                if (customerOrderDTOs != null) {
                    for (CustomerOrderDTO dto : customerOrderDTOs) {
                        //用来做前端是否展现继续受理状态
                        dto.setPreStatusCd(preSaleOrderPoolDTO.getStatusCd());
                    }
                }
                retVo.setDataList(customerOrderDTOs);
            }
            return retVo;
        } catch (Exception e) {
            e.printStackTrace();
            RetVo retVo = new RetVo(false);
            retVo.setMsgTitle(e.getMessage());
            return retVo;
        }
    }

    /**
     * 设置为全部受理
     *
     * @return
     */
    @RequestMapping("/returnToAllAccept")
    public RetVo returnToAllAccept(@RequestBody PreSaleOrderPoolDTO preSaleOrderPoolDTO) {
        try {
            RetVo retVo = new RetVo(true);
            boolean result = preSaleOrderPoolFacade.completePool(preSaleOrderPoolDTO);
            if (!result) {
                retVo.setResult(false);
            }
            return retVo;
        } catch (Exception e) {
            e.printStackTrace();
            RetVo retVo = new RetVo(false);
            retVo.setMsgTitle(e.getMessage());
            return retVo;
        }
    }

    /**
     * 继续受理
     *
     * @return
     */
    @RequestMapping("/keepAccept")
    public RetVo keepAccept(@RequestBody CustomerOrderDTO customerOrderDTO) {
        try {
            RetVo retVo = new RetVo(true);
            if (StringUtils.isNullOrEmpty(customerOrderDTO.getCustomerOrderId())) {
                retVo.setResult(false);
                return retVo;
            }
            String url = preSaleOrderPoolFacade
                    .continueAccept(NumberUtils.toLong(
                            customerOrderDTO.getCustomerOrderId()));
            if (StringUtils.isNullOrEmpty(url)) {
                retVo.setResult(false);
                return retVo;
            }
            retVo.setObject(url);
            return retVo;
        } catch (Exception e) {
            e.printStackTrace();
            RetVo retVo = new RetVo(false);
            retVo.setMsgTitle(e.getMessage());
            return retVo;
        }
    }

    /**
     * 详情
     *
     * @return
     */
    @RequestMapping("/toOrderDetail")
    public RetVo toOrderDetail(@RequestBody CustomerOrderDTO customerOrderDTO) {
        try {
            RetVo retVo = new RetVo(true);
            return retVo;
        } catch (Exception e) {
            e.printStackTrace();
            RetVo retVo = new RetVo(false);
            retVo.setMsgTitle(e.getMessage());
            return retVo;
        }
    }

    /**
     * 转变更单
     *
     * @return
     */
    @RequestMapping("/onConvertToModOrder")
    public RetVo onConvertToModOrder(@RequestBody CustomerOrderDTO customerOrderDTO) {
        try {
            RetVo retVo = preSaleOrderFacade.convertToModOrder(customerOrderDTO);
            return retVo;
        } catch (Exception e) {
            e.printStackTrace();
            RetVo retVo = new RetVo(false);
            retVo.setMsgTitle(e.getMessage());
            return retVo;
        }
    }

    /**
     * 记录受理备注
     *
     * @param preSaleOrderPoolDTO
     * @return
     */
    @RequestMapping("/saveRemark")
    public RetVo saveRemark(@RequestBody PreSaleOrderPoolDTO preSaleOrderPoolDTO) {
        try {
            RetVo retVo = new RetVo(true);
            List<AttrValueDTO> attrValueDTOs = new ArrayList<>();
            AttrValueDTO dto = new AttrValueDTO();
            dto.setAttrId(0L);
            attrValueDTOs.add(dto);
            if (StringUtils.isNullOrEmpty(preSaleOrderPoolDTO.getPreOrderId())) {
                ExceptionUtils.throwEx("preOrderId为空,请重新选择!");
            }
            preSaleOrderFacade.createPreProc(preSaleOrderPoolDTO.getPreOrderId(),preSaleOrderPoolDTO.getPreOrderId()
                    , CasSessionContext.getContext().getStaffId(), attrValueDTOs
                    , NumberUtils.toLong(CasConstant.PRE_PROC_EVENT_ID_ADD_REMARK.getValue())
                    , preSaleOrderPoolDTO.getRemark());
            return retVo;
        } catch (Exception e) {
            e.printStackTrace();
            RetVo retVo = new RetVo(false);
            retVo.setMsgTitle(e.getMessage());
            return retVo;
        }
    }

    /**
     * 展示照片
     *
     * @param preSaleOrderPoolDTO
     * @return
     */
    @RequestMapping("/showPic")
    public RetVo showPic(@RequestBody PreSaleOrderPoolDTO preSaleOrderPoolDTO) {
        try {
            RetVo retVo = new RetVo(true);
            if (StringUtils.isNullOrEmpty(preSaleOrderPoolDTO.getPreOrderId())) {
                ExceptionUtils.throwEx("preOrderId为空,请重新选择!");
            }
            if (StringUtils.isNullOrEmpty(preSaleOrderPoolDTO.getPreOrderNbr())) {
                ExceptionUtils.throwEx("P单编号为空!");
            }
            PreSaleOrderDTO preSaleOrderDTO =
                    preSaleOrderFacade.getById(preSaleOrderPoolDTO.getPreOrderId(),preSaleOrderPoolDTO.getShardingId());
            if (StringUtils.isNullOrEmpty(preSaleOrderDTO.getOrderFrom())) {
                ExceptionUtils.throwEx("orderForm为空!");
            }
            StringBuffer sbf = new StringBuffer();
            String url = casSysFacade.getAttrValueByName(
                    IntfConstant.INTF_URL_CLASS.getValue(), CasConstant.MOBI_CERT_PHOTO_URL.getValue(),
                    preSaleOrderDTO.getOrderFrom());
            sbf.append(url);
            if (url.indexOf("?") == -1) {
                sbf.append("?");
            }
            sbf.append("saleOrderNbr=" + preSaleOrderPoolDTO.getPreOrderNbr());
            retVo.setObject(sbf.toString());
            return retVo;
        } catch (Exception e) {
            e.printStackTrace();
            RetVo retVo = new RetVo(false);
            retVo.setMsgTitle(e.getMessage());
            return retVo;
        }
    }

}
