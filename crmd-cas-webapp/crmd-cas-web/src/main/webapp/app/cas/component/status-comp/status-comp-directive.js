/**
 * add by guoqn
 */
var statusCompApp = angular.module("statusCompApp", ["statusCompCtrlApp"]);

// 将模板放到缓存中
statusCompApp.run(function ($templateCache) {
    $templateCache.put("statusComp.html",
        '<div >'
        + '<div data-toggle="popover" data-placement="bottom" data-content="该组件不可用！">'
        + '<input id = "statusInput" ng-click="showStatus()" class="form-control btn-default2 comp-btn" ng-model="statusName" />'
        + '</div>'
        + '</div>')

});

statusCompApp.directive("statusComp", function () {
    return {
        scope: {
            statusName: '=', // 用于界面输入框的展示
            statusList: '=statusDataBind' // 保存业务类型数据
        },
        restrict: "AE",
        templateUrl: "statusComp.html",
        replace: true,
        transclude: true,
        link: function (scope, element, attrs) {
            if (attrs.statusDataBind == undefined) {
                // 将按钮置灰,展示提示信息
                //$('#statusCompId').attr("disabled", "true");
                //提示信息展现
                $("[data-toggle='popover']").bind('mouseover', function () {
                    $(this).popover("show");
                    $(this).on("mouseleave", function () {
                        $(this).popover('hide');
                    });
                });
                $("[data-toggle='popover']").bind('mouseleave', function () {
                    setTimeout(function () {
                        if (!$(".popover:hover").length) {
                            $(this).popover("hide")
                        }
                    }, 50);
                });
            }

            //弹出业务类型查询界面
            scope.showStatus = function () {
                $('#status-query').modal();
            };

            //表格是否可以复选
            if (attrs.checkbox != undefined && attrs.checkbox == "false") {
                var table = $('#statusTable');
                if (table != undefined) {
                    $('#statusTable').bootstrapTable('hideColumn', 'check');
                }
            }

            // 根据属性设置输入框的readonly属性
            if (attrs.readonly == "true") {
                $('#statusInput').attr("readonly", "readonly");
            } else {
                $('#statusInput').removeAttr("readonly");
            }
            // 监听选择班组的变化,并将值赋给scope.orgName，展示在界面上
            scope.$watch("statusList", function (newValue, oldValue) {
                var temp = "";
                if (newValue != undefined) {
                    for (var i = 0; i < newValue.length; i++) {
                        if (i == newValue.length - 1) {
                            temp = temp + newValue[i].statusName;
                        } else {
                            temp = temp + newValue[i].statusName + ",";
                        }
                    }
                }
                scope.statusName = temp;
            });

            $('#statusTable tbody').delegate('tr', 'click', function () {
                $(this).addClass("tr-onClick").siblings("tr").removeClass("tr-onClick");
            });
        },
        controller: "statusCompCtrl"
    }
});