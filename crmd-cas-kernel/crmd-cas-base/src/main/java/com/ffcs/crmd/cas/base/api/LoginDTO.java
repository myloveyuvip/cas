package com.ffcs.crmd.cas.base.api;

import java.io.Serializable;

/**
 * Created by qn_guo on 2016/4/6.
 */
public class LoginDTO implements Serializable{
    /**
     *
     */
    private static final long serialVersionUID = 240706623089986397L;

    private String staffPositionId;

    private String orgId;

    private String systemUserCode;
    private String orgName;
    private String staffId;
    private String staffName;
    private String areaId;
    private String regionCd;
    private String regionName;
    private String regionType;
    private String channelCode;
    private String isWholeReginPri;
    private AcrossOrgDTO acrossOrgDTO = new AcrossOrgDTO();

    /**
     *  1）区域控件展现的文字为“所有”，代码值为空
     *  2）团队控件展现的文字为“所有”，代码值为空
     *  3）预受理员工控件中的团队设置为空
     */
    private String c3c4Assign ;
    /**
     *  1）区域控件展现的文字为“所有”，代码值为空 置灰
     *  2）团队控件展现的文字为“所有”，代码值为空
     *  3）预受理员工控件中的团队设置为空
     */
    private String orgAssign;
    /**
     * 1）区域控件展现的文字为“所有”，代码值为空
     * 2）团队控件展现的文字为“所有”，代码值为空
     * 3）预受理员工控件中的团队设置为空
     */
    private String c3c4orgAssign;

    /**
     * 1）区域控件展现的文字为“所有”，代码值为空
     * 2）团队控件设置为当前登陆的团队
     * 3）预受理员工控件中的团队设置为当前登陆的团队，此时这个控件只能查询当前团队下的员工
     */
    private String notC3c4orgAssign;
    /**
     * 未配置
     */
    private String notConfigured;

    public String getIsWholeReginPri() {
        return isWholeReginPri;
    }

    public void setIsWholeReginPri(String isWholeReginPri) {
        this.isWholeReginPri = isWholeReginPri;
    }

    public String getNotC3c4orgAssign() {
        return notC3c4orgAssign;
    }

    public void setNotC3c4orgAssign(String notC3c4orgAssign) {
        this.notC3c4orgAssign = notC3c4orgAssign;
    }

    public String getC3c4Assign() {
        return c3c4Assign;
    }

    public void setC3c4Assign(String c3c4Assign) {
        this.c3c4Assign = c3c4Assign;
    }

    public String getOrgAssign() {
        return orgAssign;
    }

    public void setOrgAssign(String orgAssign) {
        this.orgAssign = orgAssign;
    }

    public String getC3c4orgAssign() {
        return c3c4orgAssign;
    }

    public void setC3c4orgAssign(String c3c4orgAssign) {
        this.c3c4orgAssign = c3c4orgAssign;
    }

    public String getNotConfigured() {
        return notConfigured;
    }

    public void setNotConfigured(String notConfigured) {
        this.notConfigured = notConfigured;
    }

    public AcrossOrgDTO getAcrossOrgDTO() {
        return acrossOrgDTO;
    }

    public void setAcrossOrgDTO(AcrossOrgDTO acrossOrgDTO) {
        this.acrossOrgDTO = acrossOrgDTO;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getRegionCd() {
        return regionCd;
    }

    public void setRegionCd(String regionCd) {
        this.regionCd = regionCd;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getStaffPositionId() {
        return staffPositionId;
    }

    public void setStaffPositionId(String staffPositionId) {
        this.staffPositionId = staffPositionId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getSystemUserCode() {
        return systemUserCode;
    }

    public void setSystemUserCode(String systemUserCode) {
        this.systemUserCode = systemUserCode;
    }

    public String getRegionType() {
        return regionType;
    }

    public void setRegionType(String regionType) {
        this.regionType = regionType;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }
}
