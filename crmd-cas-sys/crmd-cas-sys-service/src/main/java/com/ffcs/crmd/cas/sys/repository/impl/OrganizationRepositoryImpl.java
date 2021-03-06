package com.ffcs.crmd.cas.sys.repository.impl;

import com.ctg.itrdc.platform.common.entity.PageInfo;
import com.ctg.itrdc.platform.common.utils.type.NumberUtils;
import com.ctg.itrdc.platform.common.utils.type.StringUtils;
import com.ffcs.crmd.cas.constants.CasConstant;
import com.ffcs.crmd.cas.core.ddd.repository.impl.AbsCrmdBaseRepositoryImpl;
import com.ffcs.crmd.cas.sys.entity.Organization;
import com.ffcs.crmd.cas.sys.repository.IOrganizationRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("organizationRepository")
public class OrganizationRepositoryImpl extends AbsCrmdBaseRepositoryImpl<Organization, Long>
    implements IOrganizationRepository {

    public OrganizationRepositoryImpl() {
        super(Organization.class);
    }

	@Override
	public PageInfo<Organization> qryOrg(Map<String, Object> params, int page, int pageSize) {
		return  this.queryPageInfoByName("organizationRepository.qryOrg", Organization.class, params, page, pageSize);
	}

	@Override
	public List<Organization> qryOrgTree(Long orgId) {
		Map<String, Object> params = new HashMap<String, Object>();
		if (orgId != null) {// 上级团队
			params.put("parent_org_id", orgId);
		}
		params.put("statusCd", CasConstant.STATUS_CD_VAILID);
		return  this.queryListByName("organizationRepository.qryOrg", Organization.class, params);
	}

	@Override
	public List<Organization> qryOrgPosition(Long staffId) {
		Map<String, Object> params = new HashMap<String, Object>();
		if (StringUtils.isNullOrEmpty(staffId)){
			return null;
		}
		params.put("staff_id",staffId);
		return  this.queryListByName("organizationRepository.qryOrgPosition", Organization.class, params);
	}
	@Override
	public PageInfo qryOrganizaztionPageInfoByIdsAndParams(Map<String, Object> params, int page, int pageSize) {
		return this.queryPageInfoByName("organizationRepository.qryOrganizaztionPageInfoByIdsAndParams",
				Organization.class, params, page, pageSize);
	}

	@Override
	public List<Organization> qryOrgList(Map<String, Object> params) {
		return this.queryListByName("organizationRepository.qryOrg",
				Organization.class, params);
	}
}
