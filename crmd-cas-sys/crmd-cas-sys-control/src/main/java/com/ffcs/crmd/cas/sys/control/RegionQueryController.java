package com.ffcs.crmd.cas.sys.control;

import com.ctg.itrdc.platform.common.entity.PageInfo;
import com.ctg.itrdc.platform.common.utils.type.CollectionUtils;
import com.ctg.itrdc.platform.common.utils.type.StringUtils;
import com.ffcs.crmd.cas.base.context.CasSessionContext;
import com.ffcs.crmd.cas.base.utils.PageUtil;
import com.ffcs.crmd.cas.constants.CasConstant;
import com.ffcs.crmd.cas.core.control.CrmdBaseController;
import com.ffcs.crmd.cas.core.ddd.api.dto.TreeNodeDTO;
import com.ffcs.crmd.cas.order.api.facade.IPreSaleOrderPoolFacade;
import com.ffcs.crmd.cas.sys.api.dto.CommonRegionDTO;
import com.ffcs.crmd.cas.sys.api.dto.OrganizationDTO;
import com.ffcs.crmd.cas.sys.api.facade.ICasSysFacade;
import com.ffcs.crmd.cas.sys.api.facade.ICommonRegionFacade;
import com.ffcs.crmd.platform.pub.vo.RetVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/sys/regionQuery")
@ResponseBody
public class RegionQueryController extends CrmdBaseController {

    @Autowired
    private ICommonRegionFacade commonRegionFacade;
    @Autowired
    private IPreSaleOrderPoolFacade preSaleOrderPoolFacade;
    @Autowired
    private ICasSysFacade casSysFacade;

    /**
     * 查询区域树
     *
     * @param dto
     * @return
     */
    @RequestMapping("/qryComRegion")
    public RetVo qryCommonRegion(@RequestBody TreeNodeDTO dto) {
        List<TreeNodeDTO> treeNodeDTOs = null;
        RetVo retVo = new RetVo(true);
        if (StringUtils.isNullOrEmpty(dto.getId())) {
            treeNodeDTOs = commonRegionFacade
                    .queryTreeNodeByCommonRegionId(1L);
        } else {
            treeNodeDTOs = commonRegionFacade.queryTreeNodeByUpRegionId(Long
                    .parseLong(dto.getId()));
        }
        retVo.setDataList(treeNodeDTOs);
        return retVo;
    }

    /**
     * 根据条件查询
     *
     * @param dto
     * @return
     */
    @RequestMapping("/qryCommonRegionByName")
    public RetVo qryCommonRegionByName(@RequestBody CommonRegionDTO dto) {
        try {
            RetVo retVo = new RetVo(true);
            if (StringUtils.isNullOrEmpty(dto.getFromQryBtn())) {
                return retVo;
            }
            PageInfo pageInfo = new PageInfo();
            if (dto.getAcrossOrgDTO() != null) {
                if ((dto.getAcrossOrgDTO().getC4AssignOrg() != null
                        && dto.getAcrossOrgDTO().getC4AssignOrg().size() > 0)
                        || (dto.getAcrossOrgDTO().getC3AssignOrg() != null
                        && dto.getAcrossOrgDTO().getC3AssignOrg().size() > 0)) {
                    pageInfo = preSaleOrderPoolFacade.queryInitPoolRegion(
                            dto.getAcrossOrgDTO(), dto.getRegionName(), dto.getPageNumber(), dto.getPageSize());
                } else {
                    List<Long> curRegionId = new ArrayList<>();
                    curRegionId.add(CasSessionContext.getContext().getRegionCd());
                    dto.getAcrossOrgDTO().setC3AssignOrg(curRegionId);
                    pageInfo = preSaleOrderPoolFacade.queryInitPoolRegion(
                            dto.getAcrossOrgDTO(), dto.getRegionName(), dto.getPageNumber(), dto.getPageSize());
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
     * 针对配置查询
     *
     * @param dto
     * @return
     */
    @RequestMapping("/qryAssignCommonRegion")
    public RetVo qryAssignCommonRegion(@RequestBody CommonRegionDTO dto) {
        RetVo retVo = new RetVo(true);
        PageInfo pageInfo = new PageInfo();
        try {
            OrganizationDTO organizationDTO = casSysFacade.getById(CasSessionContext.getContext().getOrgId());
            if (organizationDTO != null) {
                CommonRegionDTO commonRegionDTO = commonRegionFacade.
                        getCommonRegionByCommonRegionId(organizationDTO.getCommonRegionId());
                if (commonRegionDTO != null) {
                    if (CasConstant.REGION_TYPE_C3.getValue().equals(commonRegionDTO.getRegionType()) ||
                            CasConstant.COMMON_REGION_ID_FJ.getValue().equals(
                                    StringUtils.strnull(commonRegionDTO.getCommonRegionId()))) {
                        List<CommonRegionDTO> commonRegionDTOList = new ArrayList<>();
                        commonRegionDTOList.add(commonRegionDTO);
                        List leafRegions = casSysFacade
                            .queryC3C4RegionRootToLeaf(organizationDTO.getCommonRegionId());
                        if (CollectionUtils.isNotEmpty(leafRegions)) {
                            commonRegionDTOList.addAll(leafRegions);
                        }
                        List<CommonRegionDTO> commonRegionListWithName = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(commonRegionDTOList)) {
                            if (!StringUtils.isNullOrEmpty(dto.getRegionName())) {  //根据名称查询
                                for (CommonRegionDTO commonRegionDTO1 : commonRegionDTOList) {
                                    if (commonRegionDTO1.getRegionName().contains(dto.getRegionName())) {
                                        commonRegionListWithName.add(commonRegionDTO1);
                                    }
                                }
                                pageInfo = PageUtil.list2PageInfo(commonRegionListWithName, dto.getPageNumber(), dto.getPageSize());
                            } else {
                                pageInfo = PageUtil.list2PageInfo(commonRegionDTOList, dto.getPageNumber(), dto.getPageSize());
                            }
                        }
                    } else if (CasConstant.REGION_TYPE_C4.getValue().equals(commonRegionDTO.getRegionType())) {
                        dto.setCommonRegionId(organizationDTO.getCommonRegionId());
                        pageInfo = commonRegionFacade.qryCommonRegionByName(dto);
                    }
                }
            }
            retVo.setPageInfo(pageInfo);
        } catch (Exception e) {
            e.printStackTrace();
            retVo.setResult(false);
            retVo.setMsgTitle(e.getMessage());
        }
        return retVo;
    }
}
