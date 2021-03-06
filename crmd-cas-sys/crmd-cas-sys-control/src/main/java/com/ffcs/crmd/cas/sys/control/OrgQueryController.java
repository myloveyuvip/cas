package com.ffcs.crmd.cas.sys.control;

import com.ctg.itrdc.platform.common.entity.PageInfo;
import com.ctg.itrdc.platform.common.utils.json.JSONUtils;
import com.ctg.itrdc.platform.common.utils.type.CollectionUtils;
import com.ctg.itrdc.platform.common.utils.type.StringUtils;
import com.ffcs.crmd.cas.base.context.CasSessionContext;
import com.ffcs.crmd.cas.base.utils.PageUtil;
import com.ffcs.crmd.cas.core.control.CrmdBaseController;
import com.ffcs.crmd.cas.core.ddd.api.dto.TreeNodeDTO;
import com.ffcs.crmd.cas.sys.api.dto.CommonRegionDTO;
import com.ffcs.crmd.cas.sys.api.dto.OrganizationDTO;
import com.ffcs.crmd.cas.sys.api.facade.ICasSysFacade;
import com.ffcs.crmd.cas.sys.vo.SysQueryVo;
import com.ffcs.crmd.platform.pub.vo.RetVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/sys/orgQuery")
@ResponseBody
public class OrgQueryController extends CrmdBaseController {

    @Autowired
    private ICasSysFacade casSysFacade;

    /**
     * 查询团队信息.
     *
     * @return sysQueryVo
     * @author Luxb
     * 2016年1月4日 Luxb
     */
    @RequestMapping("/qryOrgInfo")
    public RetVo qryOrgInfo(@RequestBody SysQueryVo sysQueryVo) {
        PageInfo<OrganizationDTO> pageInfo = new PageInfo<>();
        try {
            RetVo retVo = new RetVo(true);
            if (StringUtils.isNullOrEmpty(sysQueryVo.getFromQryBtn())) {
                return retVo;
            }
            OrganizationDTO organizationDTO = new OrganizationDTO();
            organizationDTO.setOrgId(sysQueryVo.getOrgId());
            organizationDTO.setOrgName(sysQueryVo.getOrgName());
            organizationDTO.setPageNumber(sysQueryVo.getPageNumber());
            organizationDTO.setPageSize(sysQueryVo.getPageSize());
            if (sysQueryVo.getAcrossOrgDTO() != null) {
                if ((sysQueryVo.getAcrossOrgDTO().getC3AssignOrg() == null
                        || sysQueryVo.getAcrossOrgDTO().getC3AssignOrg().size() == 0)
                        && (sysQueryVo.getAcrossOrgDTO().getC4AssignOrg() == null
                        || sysQueryVo.getAcrossOrgDTO().getC4AssignOrg().size() == 0)
                        && (sysQueryVo.getAcrossOrgDTO().getOrgAssignOrg() == null
                        || sysQueryVo.getAcrossOrgDTO().getOrgAssignOrg().size() == 0)) {
                    List<Long> curOrgId = new ArrayList<>();
                    curOrgId.add(CasSessionContext.getContext().getOrgId());
                    sysQueryVo.getAcrossOrgDTO().setOrgAssignOrg(curOrgId);
                    pageInfo = casSysFacade.qryOrganizaztionPageInfoByIdsAndParams(
                            sysQueryVo.getAcrossOrgDTO(), organizationDTO, organizationDTO.getPageNumber()
                            , organizationDTO.getPageSize());
                } else {
                    pageInfo = casSysFacade.qryOrganizaztionPageInfoByIdsAndParams(
                            sysQueryVo.getAcrossOrgDTO(), organizationDTO, organizationDTO.getPageNumber()
                            , organizationDTO.getPageSize());
                }
            }
            retVo.setPageInfo(pageInfo);
            return retVo;
        } catch (Exception e) {
            e.printStackTrace();
            RetVo retVo = new RetVo(false);
            retVo.setMsgTitle(e.getMessage());
            return retVo;
        }
    }

    /**
     * 查詢團隊樹.
     *
     * @param sysQueryVo
     * @return
     * @author Luxb
     * 2016年1月7日 Luxb
     */
    @RequestMapping("/qryOrgTree")
    RetVo qryOrgTree(@RequestBody SysQueryVo sysQueryVo) {
        List<TreeNodeDTO> treeNodeDTOs = null;
        try {
            RetVo retVo = new RetVo(true);
            Long orgId = null;
            if (!StringUtils.isNullOrEmpty(sysQueryVo.getId())
                    && "#".equals(sysQueryVo.getId())) {
                orgId = CasSessionContext.getContext().getOrgId();
                treeNodeDTOs = casSysFacade.qryOrgRoot(orgId);
            } else {
                orgId = Long.parseLong(sysQueryVo.getId());
                treeNodeDTOs = casSysFacade.qryOrgTree(orgId);
            }
            retVo.setDataList(treeNodeDTOs);
            return retVo;
        } catch (Exception e) {
            e.printStackTrace();
            RetVo retVo = new RetVo(false, e);
            retVo.setExceptions(null);
            return retVo;
        }
    }

    /**
     * 配置界面查询团队
     *
     * @param organizationDTO
     * @return add by guoqn
     */
    @RequestMapping("/qryAssignOrg")
    public RetVo qryAssignOrg(@RequestBody OrganizationDTO organizationDTO) {
        RetVo retVo = new RetVo(true);
        PageInfo pageInfo = new PageInfo();
        if (StringUtils.isNullOrEmpty(organizationDTO.getFromQryBtn())) {
            return retVo;
        }
        if (!StringUtils.isNullOrEmpty(organizationDTO.getOrgName())) {
            pageInfo = casSysFacade.qryByName(organizationDTO);
        } else {
            if (StringUtils.isNullOrEmpty(organizationDTO.getParentOrgId())) {
                organizationDTO.setParentOrgId(CasSessionContext.getContext().getOrgId());
            }
            pageInfo = casSysFacade.qryOrg(organizationDTO);
        }
        retVo.setPageInfo(pageInfo);
        return retVo;
    }
}
