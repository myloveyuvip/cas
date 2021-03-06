<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<div class="">
    <form class="form-inline bg-default bg-padding" style="">
        <div class="form-group">
            <label>销售团队:</label>
            <input type="text" class="form-control" ng-model="orgName"
                   placeholder="请输入销售团队名称">
        </div>
        <div class="form-group">
            <label>二次受理团队:</label>
            <input type="text" class="form-control" ng-model="executeOrgName"
                   placeholder="请输入二次受理团队名称">
        </div>
        <button type="button" ng-click="queryAssignOrg()"
                class="btn btn-blue">查询
        </button>
    </form>
    <div class="text-right bg-default">
        <button type="button" ng-click="addInteractionAssignOrg()"
                class="btn btn-white-orange btn-xs">新增转派团队
        </button>
    </div>
</div>
<div>
    <table id="intetactionAssignOrgTable" class="table table-hover fjNet-table"
           data-toggle="table"
           data-side-pagination="server" data-click-to-select="true"
           data-row-style="rowStyle" data-pagination="true" data-height="350"
           data-page-size="10" data-query-params-type=""
           data-page-list="[5, 10, 20]">
        <thead>
        <tr>
            <th data-field="commonRegionName" data-align="center">C3区域</th>
            <th data-field="cfourRegionName" data-align="center">C4区域</th>
            <th data-field="orgName" data-align="center">销售团队</th>
            <th data-field="executeOrgName" data-align="center">二次受理团队</th>
            <th data-align="center" data-formatter="operationOrgFormatter">操作</th>
        </tr>
        </thead>
    </table>
</div>
<div id="interaction_assign_org_modify" class="modal fade" tabindex="-1"
     role="dialog" data-backdrop="true" aria-hidden="true">
    <div class="modal-dialog" style="width: 600px;  ">
        <div class="modal-content">
            <div class="modal-header">
                <i class="mark"></i>
                <span class="modal-title">{{assignOrgModifyName}}</span>
                <a type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</a>
            </div>
            <div class="modal-body" style="height: 250px;">
                <div class="col-sm-2"></div>
                <div class="col-sm-8">
                    <form class="form-horizontal">
                        <div class="form-group" style="margin-bottom: 5px;">
                            <label class="col-sm-4 control-label"
                                   style="text-align: center;">销售团队:</label>
                        </div>
                        <div style="border: 1px solid #DDDDDD;">
                            <div class="form-group" style="margin-bottom: 5px;margin-top: 10px;">
                                <label class="col-sm-4 control-label"
                                       style="text-align: right;">区域:</label>
                                <div class="col-sm-6">
                                    <div region-comp region-data-bind="regionList" checkbox="false"></div>
                                </div>
                            </div>
                            <div class="form-group" style="margin-bottom: 10px;margin-top: 5px;">
                                <label class="col-sm-4 control-label"
                                       style="text-align: right;">团队:</label>
                                <div class="col-sm-6">
                                    <div org-comp org-data-bind="orgList" checkbox="false"></div>
                                </div>
                            </div>
                        </div>
                        <div class="form-group" style="margin-top: 5px;">
                            <label class="col-sm-4 control-label"
                                   style="text-align: right;">二次受理团队:</label>
                            <div class="col-sm-6">
                                <div execute-org-comp execute-org-data-bind="executeOrgList"
                                     checkbox="false"></div>
                            </div>
                        </div>
                    </form>
                </div>
                <div class="col-sm-2"></div>
            </div>
            <div class="modal-footer bg-gray">
                <div class="text-center">
                    <button class="btn btn-blue"
                            ng-click="saveInteractionAssignOrg()">保存
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
