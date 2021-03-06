<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" import="com.ffcs.crmd.cas.base.utils.JspUtil" %>
<!DOCTYPE html>
<html>
<head>
    <jsp:include page="/app/public/include/header-inc.jsp"></jsp:include>
    <link rel="stylesheet"
          href="<%=JspUtil.path(request)%>/app/cas/cas-order/css/cas-order.css?<%=JspUtil.version()%>">
    <title>预受理缴费</title>
</head>
<body ng-app="preSaleOrderApp" ng-controller="preSaleOrderCtrl">
<div class="container-fluid">
    <div class="well well-default well-padding">
        <form class="form-horizontal bg-lighter-blue">
            <div class="form-group">
                <label class="control-label col-sm-1 ">预受理员工</label>
                <div class="col-sm-2">
                    <div staff-comp staff-data-bind="staffList" checkbox="false"></div>
                </div>
                <label class="control-label col-sm-1">受理时间从</label>
                <div class="col-sm-3 date-time date-time-ie8">
                    <div class="form-inline">
                        <div class="input-group date form_date form-date2" data-date=""
                             data-date-format="yyyy/mm/dd" >
                            <input ng-model="qryPreSaleOrderInfo.beginTime"
                                   class="form-control input-date2 input-date2-ie8 dateTime-btn"
                                   size="16" type="text" value="" readonly> <span
                                class="input-group-addon comp-btn"><span
                                class="glyphicon glyphicon-calendar"></span></span><span
                                class="input-group-addon comp-btn"><span
                                class="glyphicon glyphicon-remove"><span
                                class="glyphicon fa-times"></span></span></span>
                        </div>
                        <span class="control-label">至</span>
                        <div class="input-group date form_date form-date2" data-date=""
                             data-date-format="yyyy/mm/dd">
                            <input ng-model="qryPreSaleOrderInfo.endTime"
                                   class="form-control input-date2 input-date2-ie8 dateTime-btn"
                                   size="16" type="text" value="" readonly> <span
                                class="input-group-addon comp-btn"><span
                                class="glyphicon glyphicon-calendar"></span></span><span
                                class="input-group-addon comp-btn"><span
                                class="glyphicon glyphicon-remove"><span
                                class="glyphicon fa-times"></span></span></span>
                        </div>
                    </div>
                </div>
                <label class="control-label col-sm-1">区域</label>
                <div class="col-sm-2">
                    <div ffc-drop-tree="dropConfig" drop-data="dropConfig.dropData"
                         style="display: inline-block; vertical-align: middle;">
                        <div js-tree="dropConfig.treeConfig" tree="dropConfig.instance" tree-events=""
                             tree-data="dropConfig.treeData"
                             selected-data="dropConfig.selectedData"
                             tree-types="dropConfig.treeTypes" data-multiple="true"
                             animation="false"></div>
                    </div>
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-sm-1">预受理单号</label>
                <div class="col-sm-2">
                    <input type="text" class="form-control" placeholder="请输入预受理订单号"
                           ng-model="qryPreSaleOrderInfo.preOrderNumber">
                </div>
                <label class="control-label col-sm-1">预受理团队</label>
                <div class="col-sm-3 date-time date-time-ie8">
                    <div org-comp org-data-bind="orgList" checkbox="false"></div>
                </div>
                <label class="control-label col-sm-1">状态</label>
                <div class="col-sm-2">
                    <input select2 ng-model="qryPreSaleOrderInfo.statusCd" id="statusCd"
                           select2-model="statusTypes" config="statusConfig"
                           class="form-control" type="text"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-5 col-sm-offset-7" style="text-align: right;padding-right: 30px;">
                    <button type="button" class="btn btn-blue" ng-click="qryPreSaleOrders()">查询
                    </button>
                    <button type="button" class="btn btn-ui-blue" ng-click="resetQryInfo()">重置
                    </button>
                    <button type="button" class="btn btn-ui-blue" ng-click="robotAccept()" ng-show="isRobot">
                        机器人受理
                    </button>
                    <button type="button" class="btn btn-ui-blue" ng-click="exportExcelData()">
                        导出受理单
                    </button>
                </div>
            </div>
        </form>
        <div class="bg-default text-right">
        </div>
        <table id="preSaleOrderTable" class="table table-hover fjNet-table"
               data-side-pagination="server" data-click-to-select="true" data-toggle="table"
               data-row-style="rowStyle" data-pagination="true"
               data-page-size="5" data-query-params-type="" data-page-list="[5, 10, 20]">
            <thead>
            <tr>
                <th data-field="preOrderNumber" data-align="center">受理单号</th>
                <th data-field="custName" data-align="center">客户名称</th>
                <th data-field="sceneTypeName" data-align="center">业务类型</th>
                <th data-field="staffName" data-align="center">预受理员工</th>
                <th data-field="orgName" data-align="center">预受理团队</th>
                <th data-field="createDate" data-formatter="ffc.util.tableDateFormatter"
                    data-align="center">预受理时间
                </th>
                <th data-field="realAcceptTime" data-formatter="ffc.util.tableDateFormatter"
                    data-align="center">实际受理时间
                </th>
                <th data-field="statusCdName" data-align="center">状态</th>
                <th data-align="center" data-formatter="preSaleOrderFormatter"
                    data-align="center">操作
                </th>
            </tr>
            </thead>
        </table>
    </div>
    <div class="well well-default">
        <ul class="nav nav-tab-default">
            <li class="active">
                <a role="tab" data-toggle="tab" href="#b1_c1">处理过程</a></li>
        </ul>
        <div class="tab-content nav-tab-content-default">
            <div class="tab-pane fade in active" id="b1_c1"
                 ng-controller="preSaleOrderProcCtrl">
                <button class="hidden" id="b1_c1_ngtabclick"
                        ng-click="triggerPreSaleOrderProc(preOrderId,preOrderNumber)"></button>
                <jsp:include page="../preSaleOrderProc/pre-sale-order-proc.jsp"></jsp:include>
            </div>
        </div>
    </div>
</div>
<!--补收款采集页面start-->
<div id="pay_remain_money" class="modal fade" tabindex="-1"
     role="dialog" data-backdrop="true" aria-hidden="true">
    <div class="modal-dialog" style="width: 30%;height: 50%;">
        <div class="modal-content">
            <div class="modal-header">
                <i class="mark"></i>
                <span class="modal-title">补收款页面</span>
                <a type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</a>
            </div>
            <div class="modal-body">
                <div class="form-group">
                    <div class="row"
                         style="background-color: #fffabf;height: 31px;padding-top: 5px;">
                        <div class="col-sm-1 glyphicon glyphicon-info-sign">
                        </div>
                        <div class="col-sm-11">
                            请根据业务,补收对应的款项
                        </div>
                    </div>
                </div>
                <form class="form-horizontal">
                    <div class="form-group">
                        <label for="payResource" class="col-sm-4 control-label">社采终端款</label>
                        <div class="col-sm-6">
                            <input id="payResource" class="form-control inputBigGlass"
                                   ng-model="payRemainSaveInfo.payResource"
                                   onkeyup="clearNoNum(this)">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="payStore" class="col-sm-4 control-label">预存费用</label>
                        <div class="col-sm-6">
                            <input id="payStore" class="form-control inputBigGlass"
                                   ng-model="payRemainSaveInfo.payStore"
                                   onkeyup="clearNoNum(this)">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="paySample" class="col-sm-4 control-label">普通现金</label>
                        <div class="col-sm-6">
                            <input id="paySample" class="form-control inputBigGlass"
                                   ng-model="payRemainSaveInfo.paySample"
                                   onkeyup="clearNoNum(this)">
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer bg-gray">
                <div class="text-center">
                    <button class="btn btn-blue"
                            ng-click="confirmPayRemain()">确定收款
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
<!--补收款采集页面end-->
<!--撤单信息采集页面start-->
<div id="cancel_pre_order" class="modal fade" tabindex="-1"
     role="dialog" data-backdrop="true" aria-hidden="true">
    <div class="modal-dialog" style="width: 50%;height: 40%;">
        <div class="modal-content">
            <div class="modal-header">
                <i class="mark"></i>
                <span class="modal-title">撤销确认</span>
                <a type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</a>
            </div>
            <div class="modal-body">
                <form class="form-horizontal">
                    <div class="form-group">
                        <label class="col-sm-2 control-label">受理订单: </label>
                        <div class="col-sm-10">
                            <input class="form-control"
                                   ng-model="cancelPreOrderSaveInfo.preOrderNumber" disabled>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-2 control-label">销售人员: </label>
                        <div class="col-sm-10">
                            <input class="form-control"
                                   ng-model="cancelPreOrderSaveInfo.staffName" disabled>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-2 control-label">联系电话: </label>
                        <div class="col-sm-10">
                            <input class="form-control"
                                   ng-model="cancelPreOrderSaveInfo.mobilePhone" disabled>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="cancelReason" class="col-sm-2 control-label">撤销原因: </label>
                        <div class="col-sm-10">
                            <input select2 id="cancelReason"
                                   ng-model="qryPreSaleOrderInfo.backReasonId"
                                   select2-model="qryPreSaleOrderInfo.backReason"
                                   config="backReasonConfig"
                                   class="form-control" type="text"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-2 control-label">原因描述: </label>
                        <div class="col-sm-10">
                                <textarea class="form-control"
                                          ng-model="qryPreSaleOrderInfo.backReason.attrValueName">
                                </textarea>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer bg-gray">
                <div class="text-center">
                    <button class="btn btn-blue"
                            ng-click="confirmCancelPreOrder()">确定撤销
                    </button>
                    <button class="btn btn-grey" ng-click="closeCancelPreOrder()">取消</button>
                </div>
            </div>
        </div>
    </div>
</div>
<!--撤单信息采集页面end-->

<jsp:include page="/app/public/include/footer-inc.jsp"></jsp:include>
<script type="text/javascript"
        src="<%=JspUtil.path(request)%>/app/cas/cas-order/cas-order-service.js?<%=JspUtil.version()%>"></script>
<script type="text/javascript"
        src="<%=JspUtil.path(request)%>/app/cas/cas-order/preSaleOrder/pre-sale-order-controller.js?<%=JspUtil.version()%>"></script>
<!--团队-->
<jsp:include
        page="/app/cas/component/assign-org-comp/assign_org_comp.jsp"></jsp:include>
<script type="text/javascript"
        src="<%=JspUtil.path(request)%>/app/cas/component/assign-org-comp/assign-org-comp-controller.js?<%=JspUtil.version()%>"></script>
<script type="text/javascript"
        src="<%=JspUtil.path(request)%>/app/cas/component/assign-org-comp/assign-org-comp-directive.js?<%=JspUtil.version()%>"></script>
<!--业务类型-->
<jsp:include
        page="/app/cas/component/scenetype-comp/scenetype_comp.jsp"></jsp:include>
<script type="text/javascript"
        src="<%=JspUtil.path(request)%>/app/cas/component/scenetype-comp/scenetype-comp-controller.js?<%=JspUtil.version()%>"></script>
<script type="text/javascript"
        src="<%=JspUtil.path(request)%>/app/cas/component/scenetype-comp/scenetype-comp-directive.js?<%=JspUtil.version()%>"></script>
<!-- 员工选择 -->
<jsp:include
        page="/app/cas/component/staff-comp/staff_comp.jsp"></jsp:include>
<script type="text/javascript"
        src="<%=JspUtil.path(request)%>/app/cas/component/staff-comp/staff-comp-controller.js?<%=JspUtil.version()%>"></script>
<script type="text/javascript"
        src="<%=JspUtil.path(request)%>/app/cas/component/staff-comp/staff-comp-directive.js?<%=JspUtil.version()%>"></script>
<script type="text/javascript"
        src="<%=JspUtil.path(request)%>/app/cas/component/comp-service.js?<%=JspUtil.version()%>"></script>
<!--处理过程-->
<script type="text/javascript"
        src="<%=JspUtil.path(request)%>/app/cas/cas-order/preSaleOrderProc/pre-sale-order-proc-controller.js?<%=JspUtil.version()%>"></script>
<!--数字放大镜-->
<script type="text/javascript"
        src="<%=JspUtil.path(request)%>/resources/app/app-component/app-bigGlass-comp/bigGlass.js?<%=JspUtil.version()%>"></script>
</body>

</html>
