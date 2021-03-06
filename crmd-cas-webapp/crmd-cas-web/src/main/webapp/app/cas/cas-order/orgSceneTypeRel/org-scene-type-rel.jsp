<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" import="com.ffcs.crmd.cas.base.utils.JspUtil" %>
<!DOCTYPE html>
<head>
    <jsp:include page="/app/public/include/header-inc.jsp"></jsp:include>
    <title>团队关联受理类型</title>
</head>
<body ng-app="orgSceneTypeRelApp" ng-controller="orgSceneTypeRelCtrl">
<div class="panel-body">
    <div class="container-fluid">
        <div class="well well-default well-padding">
            <div class="">
                <form class="form-inline bg-lighter-blue bg-padding" style="padding-top: 8px;">
                    <div class="form-group">
                        <label>受理团队:</label>
                        <input type="text" class="form-control" ng-model="orgName"
                               placeholder="受理团队">
                    </div>
                    <div class="form-group">
                        <label>业务类型:</label>
                        <div scene-type-comp scene-type-data-bind="sceneTypeList"
                             checkbox="false"
                             style="display: inline-block;width:200px;"></div>
                    </div>
                    <button type="submit" ng-click="qryOrgSceneTypeRel()"
                            class="btn btn-blue">查询
                    </button>
                    <button type="button" ng-click="reset()"
                            class="btn btn-ui-blue">清空
                    </button>
                </form>
                <div class="text-right bg-default">
                    <button type="button" ng-click="addOrgSceneTypeRel()"
                            class="btn btn-white-orange btn-xs">新增受理类型
                    </button>
                </div>
            </div>
            <table id="orgSceneTypeRelTable" class="table table-hover fjNet-table"
                   data-side-pagination="server" data-click-to-select="true"
                   data-row-style="rowStyle" data-pagination="true" data-height="400"
                   data-page-size="10" data-query-params-type=""
                   data-page-list="[5, 10, 20]">
                <thead>
                <tr>
                    <th data-field="orgName" data-align="center">受理团队</th>
                    <th data-field="sceneTypeNames" data-align="center">业务类型</th>
                    <th data-width="100" data-align="center" data-formatter="operationOrgFormatter">
                        操作
                    </th>
                </tr>
                </thead>
            </table>
        </div>
        <div id="org_scene_type_rel_modify" class="modal fade" tabindex="-1"
             role="dialog" data-backdrop="true" aria-hidden="true">
            <div class="modal-dialog" style="width: 60%;">
                <div class="modal-content">
                    <div class="modal-header">
                        <i class="mark"></i>
                        <span class="modal-title">{{OrgSceneTypeName}}</span>
                        <a type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</a>
                    </div>
                    <div class="modal-body">
                        <form>
                            <div class="row">
                                <div class="col-sm-2" style="text-align: right;margin-top: 10px;">
                                    <label>受理团队:</label>
                                </div>
                                <div class="col-sm-10">
                                    <input class="form-control" type="text" ng-model="modifyName"
                                           ng-hide="isShow"
                                           disabled/>
                                    <div org-comp org-data-bind="orgList" checkbox="false"
                                         readonly="true"
                                         ng-show="isShow"></div>
                                </div>
                            </div>
                            <div class="row" style="height: 400px;">
                                <div class="col-sm-2" style="text-align: right;margin-top: 10px;">
                                    <label>业务类型:</label>
                                    <div class="checkbox">
                                        <label>
                                            <input type="checkbox" id="selectAll"> 全选
                                        </label>
                                    </div>
                                </div>
                                <div class="col-sm-10" id="checkList">
                                    <div class="row">
                                        <div ng-repeat="four in sceneType_fours">
                                            <div class="col-sm-3">
                                                <div class="checkbox">
                                                    <label>
                                                        <input type="checkbox"
                                                               ng-checked="four.checked"
                                                               ng-model="four.checked">{{four.attrValueName}}
                                                    </label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div style="border-bottom:1px dashed #DDDDDD; height:1px;width:95%;"></div>
                                    <div class="row">
                                        <div ng-repeat="school in sceneType_schools">
                                            <div class="col-sm-3">
                                                <div class="checkbox">
                                                    <label>
                                                        <input type="checkbox"
                                                               ng-checked="school.checked"
                                                               ng-model="school.checked">{{school.attrValueName}}
                                                    </label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div style="border-bottom:1px dashed #DDDDDD; height:1px;width:95%;"></div>
                                    <div class="row">
                                        <div ng-repeat="band in sceneType_bands">
                                            <div class="col-sm-3">
                                                <div class="checkbox">
                                                    <label>
                                                        <input type="checkbox"
                                                               ng-checked="band.checked"
                                                               ng-model="band.checked">{{band.attrValueName}}
                                                    </label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div style="border-bottom:1px dashed #DDDDDD; height:1px;width:95%;"></div>
                                    <div class="row">
                                        <div ng-repeat="other in sceneType_others">
                                            <div class="col-sm-3">
                                                <div class="checkbox">
                                                    <label>
                                                        <input type="checkbox"
                                                               ng-checked="other.checked"
                                                               ng-model="other.checked">{{other.attrValueName}}
                                                    </label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div align="center">
                                <button class="btn btn-blue"
                                        ng-click="saveOrgSceneTypeRel()">保存
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<jsp:include page="/app/public/include/footer-inc.jsp"></jsp:include>
<!-- 业务类型二级界面 -->
<jsp:include
        page="/app/cas/component/scenetype-comp/scenetype_comp.jsp"></jsp:include>
<script type="text/javascript"
        src="<%=JspUtil.path(request)%>/app/cas/cas-order/cas-order-service.js?<%=JspUtil.version()%>"></script>
<script type="text/javascript"
        src="<%=JspUtil.path(request)%>/app/cas/cas-order/orgSceneTypeRel/org-scene-type-rel-controller.js?<%=JspUtil.version()%>"></script>
<script type="text/javascript"
        src="<%=JspUtil.path(request)%>/app/cas/component/scenetype-comp/scenetype-comp-controller.js?<%=JspUtil.version()%>"></script>
<script type="text/javascript"
        src="<%=JspUtil.path(request)%>/app/cas/component/scenetype-comp/scenetype-comp-directive.js?<%=JspUtil.version()%>"></script>
<script type="text/javascript"
        src="<%=JspUtil.path(request)%>/app/cas/component/comp-service.js?<%=JspUtil.version()%>"></script>
<!--团队-->
<jsp:include
        page="/app/cas/component/assign-org-comp/assign_org_comp.jsp"></jsp:include>
<script type="text/javascript"
        src="<%=JspUtil.path(request)%>/app/cas/component/assign-org-comp/assign-org-comp-controller.js?<%=JspUtil.version()%>"></script>
<script type="text/javascript"
        src="<%=JspUtil.path(request)%>/app/cas/component/assign-org-comp/assign-org-comp-directive.js?<%=JspUtil.version()%>"></script>


</body>
</html>
