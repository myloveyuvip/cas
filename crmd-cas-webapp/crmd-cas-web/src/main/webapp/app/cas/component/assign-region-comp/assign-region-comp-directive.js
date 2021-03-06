/**
 * add by luxb
 */
var regionCompApp = angular.module("regionCompApp", ["regionCompCtrlApp"]);

// 将模板放到缓存中
regionCompApp.run(function ($templateCache) {
    $templateCache.put("regionComp.html",
        '<div>'
        + '<div data-toggle="popover" data-placement="bottom" data-content="该组件不可用！">'
        + '<input id = "regionInput" ' +
        ' ng-click="showRegion()" class="form-control  btn-default2 comp-btn" ng-model="regionName" />'
        + '</div>'
        + '</div>')

});

regionCompApp.directive("regionComp", function () {
    return {
        scope: {
            regionName: '=', // 用于界面输入框的展示
            regionList: '=regionDataBind' // 保存团队的数据
        },
        restrict: "AE",
        templateUrl: "regionComp.html",
        replace: true,
        transclude: true,
        link: function (scope, element, attrs) {
            //表格是否可以复选
            if (attrs.checkbox != undefined && attrs.checkbox == "false") {
                var table = $('#regionTable');
                if (table != undefined) {
                    $('#regionTable').bootstrapTable('hideColumn', 'check');
                }
            }

            //弹出团队查询界面
            scope.showRegion = function () {
                $('#region-query').modal();
            };
            // 根据属性设置输入框的readonly属性
            if (attrs.readonly == "true") {
                $('#regionInput').attr("readonly", "readonly");
            } else {
                $('#regionInput').removeAttr("readonly");
            }
            // 监听选择班组的变化,并将值赋给scope.regionName，展示在界面上
            scope.$watch("regionList", function (newValue, oldValue) {
                var temp = "";
                if (newValue != undefined) {
                    for (var i = 0; i < newValue.length; i++) {
                        if (i == newValue.length - 1) {
                            temp = temp + newValue[i].regionName;
                        } else {
                            temp = temp + newValue[i].regionName + ",";
                        }
                    }
                }
                scope.regionName = temp;
            });

            $('#regionTable tbody').delegate('tr', 'click', function () {
                $(this).addClass("tr-onClick").siblings("tr").removeClass("tr-onClick");
            });
        },
        controller: "regionCompCtrl"
    }
});