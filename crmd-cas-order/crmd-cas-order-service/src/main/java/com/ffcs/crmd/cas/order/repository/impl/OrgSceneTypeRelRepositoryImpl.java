package com.ffcs.crmd.cas.order.repository.impl;

import com.ctg.itrdc.platform.common.utils.type.StringUtils;
import com.ffcs.crmd.cas.core.ddd.repository.impl.AbsCrmdBaseRepositoryImpl;
import com.ffcs.crmd.cas.order.api.vo.OrgSceneTypeRelVo;
import com.ffcs.crmd.cas.order.entity.OrgSceneTypeRel;
import com.ffcs.crmd.cas.order.repository.IOrgSceneTypeRelRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("orgSceneTypeRelRepository")
public class OrgSceneTypeRelRepositoryImpl extends AbsCrmdBaseRepositoryImpl<OrgSceneTypeRel, Long>
    implements IOrgSceneTypeRelRepository {

    public OrgSceneTypeRelRepositoryImpl() {
        super(OrgSceneTypeRel.class);
    }

    @Override
    public List<OrgSceneTypeRel> queryOrgSceneTypeRel(OrgSceneTypeRelVo orgSceneTypeRelVo) {
        Map params = new HashMap<>();
        //参数：团队ID
        if (!StringUtils.isNullOrEmpty(orgSceneTypeRelVo.getOrgId())&&orgSceneTypeRelVo.getOrgId()!=0L) {
            params.put("orgId", orgSceneTypeRelVo.getOrgId());
        }
        //参数：受理类型
        if (!StringUtils.isNullOrEmpty(orgSceneTypeRelVo.getSceneType())) {
            params.put("sceneType", orgSceneTypeRelVo.getSceneType());
        }
        //参数：状态
        if (!StringUtils.isNullOrEmpty(orgSceneTypeRelVo.getStatusCd())) {
            params.put("statusCd", orgSceneTypeRelVo.getStatusCd());
        }
        //参数：区域ID
        if (!StringUtils.isNullOrEmpty(orgSceneTypeRelVo.getAreaId())) {
            params.put("areaId", orgSceneTypeRelVo.getAreaId());
        }
        return this.queryListByName("orgSceneTypeRelRepository.queryOrgSceneTypeRel",
            OrgSceneTypeRel.class, params);
    }
}
