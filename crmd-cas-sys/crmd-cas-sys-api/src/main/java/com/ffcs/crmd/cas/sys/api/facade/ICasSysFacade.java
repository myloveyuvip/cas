package com.ffcs.crmd.cas.sys.api.facade;

import com.ctg.itrdc.platform.common.entity.PageInfo;
import com.ffcs.crmd.cas.base.api.AcrossOrgDTO;
import com.ffcs.crmd.cas.core.ddd.api.dto.TreeNodeDTO;
import com.ffcs.crmd.cas.core.ddd.api.facade.ICrmdBaseFacade;
import com.ffcs.crmd.cas.sys.api.dto.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * .
 *
 * @author Luxb
 * @version Revision 1.0.0
 * @版权：福富软件 版权所有 (c) 2011
 * @see:
 * @创建日期：2016/1/15
 * @功能说明：
 */
public interface ICasSysFacade extends ICrmdBaseFacade {

    /**
     * 根据区域ID获取C3级区域编码.
     *
     * @param areaId
     * @return
     * @author renl
     *         2012-9-7 renl
     */
    String getLatnIdByAreaId(Long areaId);

    /**
     * 获取主数据配置的业务类型。
     * @param className
     * @param propertyName
     * @return
     */
    List<SceneTypeDTO> qrySceneType(String className, String propertyName);
    /**
     * 根据条件获取员工信息.
     * @param staffDTO
     * @return
     * @author Luxb
     * 2016年1月7日 Luxb
     */
    PageInfo<StaffDTO> qryStaff(StaffDTO staffDTO);

    /**
     * 获取员工名称
     * @author YULIYAO 2016/1/12
     * return
     */
    String getStaffNameById(Long staffId);

    /**
     * 根据团队名称获取团队信息.
     * @param organizationDTO
     * @return
     * @author Luxb
     * 2016年1月4日 Luxb
     */
    PageInfo qryOrg(OrganizationDTO organizationDTO);
    /**
     * 團隊樹查詢.
     * @param orgId
     * @return
     * @author Luxb
     * 2016年1月7日 Luxb
     */
    List<TreeNodeDTO> qryOrgTree(Long orgId);

    /**
     * 通过id查询团队
     * @author YULIYAO 2016/1/8
     * return
     */
    OrganizationDTO getById(Long orgId);

    /**
     * 通过ID查询团队名称
     * @param orgId
     * @return
     */
    String getNameById(Long orgId);

    /**
     * 根据类code，属性code获取属性值列表.
     * @author YULIYAO 2016/1/12
     * return
     */
    List<AttrValueDTO> getAttrValues(String className, String propertyName);

    /**
     * 根据ID查询区域
     * @author YULIYAO 2016/1/18
     * return 
     */
    CommonRegionDTO getCommonRegionById(Long commonRegionId);

    List<CommonRegionDTO> queryRegionRootToLeaf(Long commonRegionId);

    /**
     * 根据ID查询员工
     * @author YULIYAO 2016/1/21
     * return
     */
    StaffDTO getStaffById(Long staffId);

    Map<Long, String> queryStaffNames(Set<Long> staffIds);

    Map<Long, String> queryOrgNames(Set<Long> orgIds);

    /**
     * 根据code获取外系统地址
     * @param code
     * @author GUOQN 2016/1/25
     * @return
     */
    String getOutSysUrlByCode(String code);

    AreaCodeDTO getAreaCodeByRegionId(Long regionId);

    /**
     * 根据orgId获取员工列表。
     * @param orgId
     * @return
     */
    List<StaffDTO> queryStaffList(Long orgId);

    String getCommonRegionNameById(Long commonRegionId);

    /**
     *
     * @author YULIYAO 2016/2/16
     * return 
     */
    List<CommonRegionDTO> queryRegionLeafToRoot(Long commonRegionId);

    /**
     * 查询岗位
     * @author YULIYAO 2016/3/4
     * return 
     */
    public List<StaffPositionDTO> queryStaffPosition(StaffPositionDTO staffPositionDTO);

    StaffPositionDTO getStaffPositionByStaffIdAndOrgId(Long staffId, Long orgId);

    /**
     * 根据code查询attrspec
     */
    AttrSpecDTO qryAttrSpecByCode(String javaCode);

    /**
     * 根据主数据属性名称获取属性值
     * @param className
     * @param javaCode
     * @param attrValueName
     * @return
     */
    String getAttrValueByName(String className,String javaCode,String attrValueName);

    /**
     * 根据主键获取岗位
     * @author YULIYAO 2016/5/4
     * return 
     */
    StaffPositionDTO getStaffPositionById(Long staffPositionId);
    /**
     * 工单池预受理团队.
     * 
     * @param acrossOrgDTO
     * @param organizationDTO
     * @param currentPage
     * @param perPageCount
     * @return
     */
	PageInfo qryOrganizaztionPageInfoByIdsAndParams(AcrossOrgDTO acrossOrgDTO,
			OrganizationDTO organizationDTO, int currentPage, int perPageCount);

    /**
     * 根据区域ID获取C3级
     *
     * @author YULIYAO 2016/3/4
     * return
     */
    AreaCodeDTO getC3AreaCodeByCommonRegionId(Long commonRegionId);

    /**
     * 获取组织树的根节点
     */
    List<TreeNodeDTO> qryOrgRoot(Long orgId);

    /**
     * 根据名称查询
     * @param organizationDTO
     * @return
     */
    PageInfo qryByName(OrganizationDTO organizationDTO);

    /**
     * 批量查询区域名称
     * @author YULIYAO 2016/5/17
     * return 
     */
    Map queryRegionNames(Set<Long> regionIds);

    /**
     * 只查询到C4区域
     * @param commonRegionId
     * @return
     */
    List<CommonRegionDTO> queryC3C4RegionRootToLeaf(Long commonRegionId);
}
