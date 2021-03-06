package com.ffcs.crmd.cas.sys.service.impl;

import com.ctg.itrdc.platform.common.entity.PageInfo;
import com.ffcs.crmd.cas.core.ddd.service.impl.AbsCrmdGenericServiceImpl;
import com.ffcs.crmd.cas.sys.entity.Organization;
import com.ffcs.crmd.cas.sys.repository.IOrganizationRepository;
import com.ffcs.crmd.cas.sys.service.IOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("organizationService")
public class OrganizationServiceImpl extends AbsCrmdGenericServiceImpl<Organization, Long>
    implements IOrganizationService {

   	@Autowired
	IOrganizationRepository organizationRepository;

	@Override
	public PageInfo qryOrg(Map<String, Object> params, int page, int pageSize) {
		return organizationRepository.qryOrg(params , page , pageSize);
	}
	@Override
	public List<Organization> qryOrgList(Map<String, Object> params) {
		return organizationRepository.qryOrgList(params);
	}

	@Override
	public List<Organization> qryOrgTree(Long orgId) {
		return organizationRepository.qryOrgTree(orgId);
	}

	@Override
	public List<Organization> qryPreOrderDisList(Long staffId) {
		return organizationRepository.qryOrgPosition(staffId);
	}

	/**
	 * 查询团队信息
	 * @author YULIYAO 2016/4/29
	 * return
	 */
	public List<Organization> queryOrganization(Map param) {
		return organizationRepository
				.queryListByName("organizationRepository.qryOrg", Organization.class, param);
	}
	@Override
	public PageInfo qryOrganizaztionPageInfoByIdsAndParams(Map<String, Object> params, int page, int pageSize) {
		return organizationRepository.qryOrganizaztionPageInfoByIdsAndParams(params, page, pageSize);
	}
}
