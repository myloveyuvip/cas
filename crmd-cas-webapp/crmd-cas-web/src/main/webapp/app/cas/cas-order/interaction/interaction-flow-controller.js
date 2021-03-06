/**
 * 工单池流程
 * add by guoqn
 */
// module
var interactionMainApp = angular.module("interactionFlowApp", ["casOrderServiceApp", "orgCompApp", "regionCompApp", "executeOrgCompApp", "ui.select", "bsTable", "ngSelect2"]);
// controller

interactionMainApp.controller("interactionFlowCtrl", ["$scope", "casOrderService", "commonService", "$compile", function ($scope, casOrderService, commonService, $compile) {
    $scope.errorTips = {};
    $scope.interactionFlowSaveInfo = {};
    $scope.interactionFlowQueryInfo = {};
    //业务大类下拉
    $scope.typeGroupConfig = {
        minimumResultsForSearch: -1,
        data: [],
        placeholder: '请选择...'
    };
    //状态下拉
    $scope.statusConfig = {
        minimumResultsForSearch: -1,
        data: [],
        placeholder: '请选择...'
    };
    var statusCd;
    // 流程表格初始化
    $('#interactionFlowTable').bootstrapTable({
        // 加载成功后重新编译，试点formatter后的dom元素支持angularjs
        onResetView: function () {
            $compile($('#interactionFlowTable'))($scope);
        },
        // 条件
        queryParams: function (params) {
            //查询条件
            var selfParams = {
                interactionFlowName: $scope.interactionFlowName
            }
            return $.extend({}, params, selfParams);
        },
        // 服务请求
        ajax: function (render) {
            try {
                if (!render.data) {
                    ffc.util.loadTableData(render);
                } else {
                    // 服务请求
                    casOrderService.queryInterActionFlowData(render.data, function (result) {
                        if (!result.result){
                            if (!ffc.util.isEmpty(result.msgTitle)) {
                                MESSAGE_DIALOG.error(result.msgTitle +"");
                            }
                            ffc.util.loadTableData(render);
                            return false;
                        }
                        ffc.util.setPageResult($scope, 'interactionFlowErrorAlert', render, result);
                    });
                }
            } catch (e) {
                ffc.util.loadTableData(render);
            }
        },
        // 点击行事件
        onClickRow: function (row, $element) {
            // 点击行后 更改流程id
            $scope.interactionFlowId = !ffc.util.isEmpty(row.interactionFlowId) ? row.interactionFlowId : "";
            // 点击行后 触发当前tab
            ffc.util.tabsActiveTriggerNg(["#b1_c1"]);
        }
    });
    // 页面下拉框数据
    $scope.initAllListBox = function () {
        casOrderService.queryAllBoxListForInteractionFlow("", function (result) {
            // 初始化业务组大类
            if (!ffc.util.isEmpty(result)
                && !ffc.util.isEmpty(result.object)
                && !ffc.util.isEmpty(result.object.custItemTypeGroupList)) {
                $scope.typeGroups = result.object.custItemTypeGroupList;
                $scope.typeGroupConfig.data = $scope.typeGroups;
                $scope.interactionFlowSaveInfo.custItemTypeGroup = $scope.typeGroups[0].id;
            }
            //状态类型
            if (!ffc.util.isEmpty(result)
                && !ffc.util.isEmpty(result.object)
                && 0 < result.object.statusCdList.length) {
                $scope.statusTypes = result.object.statusCdList;
                $scope.statusConfig.data = $scope.statusTypes;
                $scope.statusCdType = $scope.statusTypes[1].id;
            }
        }, function (msg) {
            MESSAGE_DIALOG.error("queryAllBoxListForInteractionFlowError" + msg);
        });
    };
    // 初始化
    commonService.initFrom(ffc.util.getPageMetas($scope), function () {
        if (ffc.util.isEmpty($scope.statusTypes)
            || ffc.util.isEmpty($scope.typeGroups)) {
            $scope.initAllListBox();
        }
    });

    //查询按钮
    $scope.queryFlowList = function () {
        $("#intetactionAssignOrgTable").bootstrapTable('removeAll');
        $("#interactionFlowTable").bootstrapTable("selectPage", 1);
        $scope.interactionFlowId = undefined;
    };
    //编辑
    $scope.modifyInteractionFlow = function (rowIndex, elm) {
        $scope.interactionModifyFlowName ="工单池流程编辑页面";
        $scope.interactionFlowSaveInfo = {};
        elm.stopPropagation();
        var allTable = $('#interactionFlowTable')
            .bootstrapTable("getData");
        var selectRow = allTable[rowIndex];
        $scope.interactionFlowSaveInfo = selectRow;
        $scope.statusCdType = selectRow.statusCd;
        $('#interaction_flow_modify').modal('show');
    };
    //删除
    $scope.delInteractionFlow = function (rowIndex, elm) {
        var allTable = $('#interactionFlowTable')
            .bootstrapTable("getData");
        var selectRow = allTable[rowIndex];
        $scope.interactionFlowSaveInfo = {};
        $scope.interactionFlowSaveInfo = selectRow;
        var deleteInfo = {
            interactionFlowId: selectRow.interactionFlowId
        };
        elm.stopPropagation();
        MESSAGE_DIALOG.loading();
        casOrderService.qryAssignOrgCountData(deleteInfo, function (result) {
            if (!result.result) {  //校验是否存在转派团队
                MESSAGE_DIALOG.warning("流程存在接触单转派团队,不允许删除！");
            } else {
                MESSAGE_DIALOG.close();
                MESSAGE_DIALOG.confirm("确认删除？", function () {
                    casOrderService.delInteractionFlowData($scope.interactionFlowSaveInfo,
                        function (result) {
                            if (result.result != undefined
                                && result.result == true) {
                                MESSAGE_DIALOG.alert("删除成功！");
                                $scope.queryFlowList();
                            } else {
                                MESSAGE_DIALOG.warning(result.msgTitle + ".删除失败！");
                            }
                        }, function (msg) {
                            MESSAGE_DIALOG.error("删除失败！" + msg);
                        });
                });
            }
        }, function (msg) {
            MESSAGE_DIALOG.error(msg);
        });
    };
    //新增
    $scope.addInteractionFlow = function () {
        $scope.interactionFlowSaveInfo = {};
        $scope.interactionModifyFlowName ="新增工单池流程页面";
        $scope.statusCdType = "1000";
        $scope.interactionFlowSaveInfo.custItemTypeGroup = $scope.typeGroups[0].id;
        $('#interaction_flow_modify').modal('show');
    };
    //保存
    $scope.saveInteractionFlow = function () {
        if ("" == $scope.statusCdType
            || undefined == $scope.statusCdType) {
            MESSAGE_DIALOG.warning("状态不能为空!");
            return;
        }
        $scope.interactionFlowSaveInfo.statusCd = $scope.statusCdType;
        if ("" == $scope.interactionFlowSaveInfo.interactionFlowName
            || undefined == $scope.interactionFlowSaveInfo.interactionFlowName) {
            MESSAGE_DIALOG.warning("流程名称不能为空!");
            return;
        }
        MESSAGE_DIALOG.loading();
        casOrderService.saveInteractionFlowData($scope.interactionFlowSaveInfo, function (result) {
            if (result.result != undefined && result.result == true) {
                MESSAGE_DIALOG.alert("保存成功！");
                $('#interaction_flow_modify').modal('hide');
                $scope.queryFlowList();
            } else {
                MESSAGE_DIALOG.warning(result.msgTitle + ",保存失败！");
            }
        }, function (msg) {
            MESSAGE_DIALOG.error("保存失败！" + msg);
        });
    };
}]);
//格式化列
var operationFormatter = function (value, row, index) {
    var modifyBtn = '<a ng-click="modifyInteractionFlow(' + index + ',$event)">编辑</a>';
    var delBtn = '<a ng-click="delInteractionFlow(' + index + ',$event)">删除</a>';
    return modifyBtn + " | " + delBtn;
}

//选中颜色
$(function () {
    $('#interactionFlowTable tbody').delegate('tr', 'click', function () {
        $(this).addClass("tr-onClick").siblings("tr").removeClass("tr-onClick");
    });
});