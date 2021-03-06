/**
 * 公共组件查询
 */

// 创建公共组件模块
var compServiceApp = angular.module("compServiceApp", [ "commonApp" ]);

// 创建公共组件服务
compServiceApp.service("compService", [ "commonService", function(commonService) {
	//团队查询。
	this.qryOrgInfoData = function(params, sback, eback) {
		commonService.call("sys/orgQuery/qryOrgInfo", params, sback, eback);
	}
	//团队树查询。
	this.qryOrgTree = function(params, sback, eback) {
		commonService.call("sys/orgQuery/qryOrgTree", params, sback, eback);
	}
	//员工查询
	this.qryStaffInfoData = function(params, sback, eback) {
		commonService.call("sys/staffQuery/qryStaffInfo", params, sback, eback);
	}
	//业务类型查询
	this.qrySceneType = function(params, sback, eback) {
		commonService.call("sys/sysQuery/qrySceneType", params, sback, eback);
	}
	//区域查询
	this.qryComRegion = function(params, sback, eback) {
		commonService.call("sys/regionQuery/qryCommonRegion", params, sback, eback);
	}
	//根据区域名称查询
	this.qryCommonRegionByName =  function(params, sback, eback) {
		commonService.call("sys/regionQuery/qryCommonRegionByName", params, sback, eback);
	}
	//订单状态查询
	this.qryStatus = function(params, sback, eback) {
		commonService.call("sys/sysQuery/qryStatus", params, sback, eback);
	}
	//接收员工查询
	this.qryReceiveStaffInfo = function(params, sback, eback) {
		commonService.call("sys/receiveStaff/qryReceiveStaffInfo", params, sback, eback);
	}
	//查询区域配置
	this.qryAssignCommonRegion =  function(params, sback, eback) {
		commonService.call("sys/regionQuery/qryAssignCommonRegion", params, sback, eback);
	}
	//查询团队配置
	this.qryAssignOrg =  function(params, sback, eback) {
		commonService.call("sys/orgQuery/qryAssignOrg", params, sback, eback);
	}
	//查询二次受理团队配置
	this.qryAssignOrg =  function(params, sback, eback) {
		commonService.call("sys/orgQuery/qryAssignOrg", params, sback, eback);
	}
} ]);