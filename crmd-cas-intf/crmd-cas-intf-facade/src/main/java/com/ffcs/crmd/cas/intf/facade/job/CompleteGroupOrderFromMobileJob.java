package com.ffcs.crmd.cas.intf.facade.job;

import com.ctg.itrdc.platform.common.entity.PageInfo;
import com.ctg.itrdc.platform.common.utils.type.CollectionUtils;
import com.ctg.itrdc.platform.common.utils.type.NumberUtils;
import com.ctg.itrdc.platform.common.utils.type.StringUtils;
import com.ctg.itrdc.platform.pub.util.ApplicationContextUtil;
import com.ffcs.crmd.cas.base.utils.CrmClassUtils;
import com.ffcs.crmd.cas.constants.CasConstant;
import com.ffcs.crmd.cas.constants.IntfConstant;
import com.ffcs.crmd.cas.intf.api.facade.ICasToCrmFacade;
import com.ffcs.crmd.cas.order.api.dto.PreSaleOrderDTO;
import com.ffcs.crmd.cas.order.api.facade.IPreSaleOrderFacade;
import com.ffcs.crmd.platform.meta.entity.AttrSpec;
import com.ffcs.crmd.platform.pub.vo.RetVo;
import com.ffcs.crmd.tsp.common.constant.TspAppConstant;
import com.ffcs.crmd.tsp.common.exception.TspJobExecutionException;
import com.ffcs.crmd.tsp.common.job.entity.TspJobDataMap;
import com.ffcs.crmd.tsp.task.job.api.IJobBatch;
import com.ffcs.crmd.tsp.task.job.api.IJobExecutionContext;

import java.util.List;

/**
 * 集团单省内缴费归档处理
 * Created by YULIYAO on 2016/4/7.
 */
public class CompleteGroupOrderFromMobileJob implements IJobBatch<PreSaleOrderDTO>{

    private IPreSaleOrderFacade preSaleOrderFacade = ApplicationContextUtil
        .getBean("preSaleOrderFacade");

    private ICasToCrmFacade casToCrmFacade = ApplicationContextUtil.getBean("casToCrmFacade");

    @Override
    public void execute(IJobExecutionContext<PreSaleOrderDTO> iJobExecutionContext)
        throws TspJobExecutionException {
        //获取配置参数
        TspJobDataMap dataMap = iJobExecutionContext.getTspJobDataMap();
        int model = NumberUtils.nullToIntegerZero(dataMap.get(TspAppConstant.TSP_PARAM_MODEL_VALUE));
        int procCount = NumberUtils.nullToIntegerZero(dataMap.get(TspAppConstant.TSP_PARAM_JOB_DETAIL_TOTAL));
        //1.查询需要缴费归档的预销售单
        PageInfo pageInfo = preSaleOrderFacade
            .queryCompleteGroupOrderAuto(model, procCount, getPerSize());
        if (pageInfo != null && CollectionUtils.isNotEmpty(pageInfo.getList())) {
            List<PreSaleOrderDTO> preSaleOrderDTOList = pageInfo.getList();
            iJobExecutionContext.addParam(preSaleOrderDTOList);
        }
    }

    @Override
    public void businessProcess(PreSaleOrderDTO preSaleOrderDTO) throws TspJobExecutionException {
        //1.调接口——正式单设置最后一张单，预受理标识置空，设置可重复启动流程，启动流程,保存营业日报信息
        if (preSaleOrderDTO.getExtCustOrderId() != null
            && preSaleOrderDTO.getExtCustOrderId() != 0) {
            RetVo retVo = casToCrmFacade
                .noticeToDealOrder(preSaleOrderDTO.getExtCustOrderId(), "1");
            String isAccept = (String) retVo.getObject();
            if ("1".equals(isAccept)) {
                //2.P单修改状态，设置最后一张单
                preSaleOrderDTO.setIfEnd("1");
                preSaleOrderDTO.setStatusCd(CasConstant.PRE_SALE_ORDER_STATUS_CD_ON_WAY.getValue());
                preSaleOrderFacade.save(preSaleOrderDTO);
                //3.通知移动客户端
                preSaleOrderFacade.sendStateChangeToPad(preSaleOrderDTO);
            }
        }
    }

    private int getPerSize() {
        int perSize = NumberUtils.toInt(IntfConstant.DEFAULT_CIRCLE_PER_SIZE.getValue());
        AttrSpec attrSpec = CrmClassUtils.getAttrSpecByCode(IntfConstant.UI_META_CLASSSNAME.getValue(),
            IntfConstant.ATTR_SPEC_ORDER_DEAL_PER_SIZE.getValue());
        if (attrSpec != null && !StringUtils.isNullOrEmpty(attrSpec.getDefaultValue())) {
            perSize = NumberUtils.toInt(attrSpec.getDefaultValue());
        }
        return perSize;
    }
}
