package com.ffcs.crmd.cas.order.repository.impl;

import com.ctg.itrdc.platform.common.entity.PageInfo;
import com.ctg.itrdc.platform.common.utils.type.StringUtils;
import com.ffcs.crmd.cas.core.ddd.repository.impl.AbsCrmdBaseRepositoryImpl;
import com.ffcs.crmd.cas.order.entity.PreSaleOrderProc;
import com.ffcs.crmd.cas.order.entity.PreSaleOrderProcAttr;
import com.ffcs.crmd.cas.order.entity.PreSaleOrderProcHis;
import com.ffcs.crmd.cas.order.repository.IPreSaleOrderProcRepository;
import com.ffcs.crmd.cas.order.vo.PreSaleOrderProcForIntfVo;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("preSaleOrderProcRepository")
public class PreSaleOrderProcRepositoryImpl
    extends AbsCrmdBaseRepositoryImpl<PreSaleOrderProc, Long>
    implements IPreSaleOrderProcRepository {

    public PreSaleOrderProcRepositoryImpl() {
        super(PreSaleOrderProc.class);
    }

    @Override
    public List<PreSaleOrderProc> queryPreSaleOrderProc(PreSaleOrderProc vo) {
        Map param = new HashMap();
        //预受理单号id
        if (!StringUtils.isNullOrEmpty(vo.getPreOrderId())) {
            param.put("preOrderId", vo.getPreOrderId());
            param.put("shardingId", vo.getPreOrderId());
        }
        return this.queryListByName("preSaleOrderRepository.queryPreSaleOrderProc",
            PreSaleOrderProc.class, param);
    }

    @Override
    public PageInfo queryPreSaleOrderProcPage(PreSaleOrderProc vo, int pageNumber, int pageSize) {
        Map param = new HashMap();
        //预受理单号id
        if (!StringUtils.isNullOrEmpty(vo.getPreOrderId())) {
            param.put("preOrderId", vo.getPreOrderId());
            param.put("shardingId", vo.getPreOrderId());
        }
        return this.queryPageInfoByName("preSaleOrderRepository.queryPreSaleOrderProc",
            PreSaleOrderProc.class, param, pageNumber, pageSize);
    }

    @Override
    public PageInfo queryPreSaleOrderProcWithAttr(PreSaleOrderProc vo, int pageNumber, int pageSize) {
        Map param = new HashMap();
        //预受理单号id
        if (!StringUtils.isNullOrEmpty(vo.getPreOrderId())) {
            param.put("preOrderId", vo.getPreOrderId());
            param.put("shardingId", vo.getPreOrderId());
        }
        return this.queryPageInfoByName("preSaleOrderRepository.queryPreSaleOrderProcWithAttr",
            PreSaleOrderProc.class, param, pageNumber, pageSize);
    }

    @Override
    public List<PreSaleOrderProcHis> queryPreSaleOrderProcHis(PreSaleOrderProc proc) {
        Map param = new HashMap();
        //预受理单号id
        if (!StringUtils.isNullOrEmpty(proc.getPreOrderId())) {
            param.put("preOrderId", proc.getPreOrderId());
            param.put("shardingId", proc.getPreOrderId());
        }
        return this.queryListByName("preSaleOrderRepository.queryPreSaleOrderProcHis",
                PreSaleOrderProcHis.class, param);
    }

    /**
     * 查询处理过程属性
     * @author YULIYAO 2016/4/7
     * return
     */
    @Override
    public List<PreSaleOrderProcAttr> queryPreSaleOrderProcAttr(Map map) {
        return this.queryListByName("preSaleOrderRepository.queryPreSaleOrderProcAttr",
            PreSaleOrderProcAttr.class, map);
    }

	@Override
	public List<PreSaleOrderProcForIntfVo> qryPreSaleOrderProcForIntf(Map map) {
		return this.queryListByName("preSaleOrderRepository.qryPreSaleOrderProcForIntf",
				PreSaleOrderProcForIntfVo.class, map);
	}
	
	@Override
	public List<PreSaleOrderProcForIntfVo> qryPreSaleOrderProcHisForIntf(Map map) {
		return this.queryListByName("preSaleOrderRepository.qryPreSaleOrderProcHisForIntf",
				PreSaleOrderProcForIntfVo.class, map);
	}
}
