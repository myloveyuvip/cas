package com.ffcs.crmd.cas.sys.repository;


import java.util.List;
import java.util.Map;

import com.ctg.itrdc.platform.common.entity.PageInfo;
import com.ffcs.crmd.cas.core.ddd.repository.ICrmdBaseRepository;
import com.ffcs.crmd.cas.sys.api.dto.OrganizationDTO;
import com.ffcs.crmd.cas.sys.entity.Organization;

public interface IOrganizationRepository extends ICrmdBaseRepository<Organization,Long>  {
	/**
	 * 根据团队名称获取团队信息列表.
	 * @param params
	 * @param page
	 * @param pageSize
     * @return
	 * @author Luxb
	 * 2016年1月4日 Luxb
     */
	PageInfo<Organization> qryOrg(Map<String, Object> params,int page, int pageSize);
	/**
	 * 獲取團隊樹數據.
	 * @param orgId
	 * @return
	 * @author Luxb
	 * 2016年1月7日 Luxb
	 */
	List<Organization> qryOrgTree(Long orgId);

	/**
	 * 获取员工关联团队
	 * @param staffId
	 * @return
     */
	List<Organization> qryOrgPosition(Long staffId);
	/**
	 * 工单池预受理团队.
	 * 
	 * @param params
	 * @param page
	 * @param pageSize
	 * @return
	 */
	PageInfo qryOrganizaztionPageInfoByIdsAndParams(Map<String, Object> params, int page, int pageSize);

	List<Organization> qryOrgList(Map<String, Object> params);
}
