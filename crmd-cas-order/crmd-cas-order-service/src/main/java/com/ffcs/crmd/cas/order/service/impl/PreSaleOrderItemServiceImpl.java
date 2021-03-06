package com.ffcs.crmd.cas.order.service.impl;

import com.ctg.itrdc.platform.common.utils.type.CollectionUtils;
import com.ffcs.crmd.cas.core.ddd.service.impl.AbsCrmdGenericServiceImpl;
import com.ffcs.crmd.cas.order.entity.PreSaleOrderItem;
import com.ffcs.crmd.cas.order.repository.IPreSaleOrderItemRepository;
import com.ffcs.crmd.cas.order.service.IPreSaleOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("preSaleOrderItemService")
public class PreSaleOrderItemServiceImpl extends AbsCrmdGenericServiceImpl<PreSaleOrderItem, Long>
    implements IPreSaleOrderItemService {

   	@Autowired
	IPreSaleOrderItemRepository preSaleOrderItemRepository;

	@Override
	public int removeItemByOrderId(Long preSaleOrderId, Long shardingId) {
		int result = 0;
		Map param = new HashMap<>();
		param.put("preOrderId", preSaleOrderId);
		param.put("shardingId", shardingId);
		List<PreSaleOrderItem> preSaleOrderItems = PreSaleOrderItem.repository().queryPreOrderItem(param);
		if (CollectionUtils.isNotEmpty(preSaleOrderItems)) {
			for (PreSaleOrderItem orderItem : preSaleOrderItems) {
				result += orderItem.remove();
			}
		}
		return result;
	}

	@Override
	public List<PreSaleOrderItem> queryPreSaleOrderItems(Long preSaleOrderId) {
		Map param = new HashMap<>();
		param.put("preOrderId", preSaleOrderId);
		param.put("shardingId", preSaleOrderId);
		List<PreSaleOrderItem> preSaleOrderItems = PreSaleOrderItem.repository().queryPreOrderItem(param);
		return preSaleOrderItems;
	}
}
