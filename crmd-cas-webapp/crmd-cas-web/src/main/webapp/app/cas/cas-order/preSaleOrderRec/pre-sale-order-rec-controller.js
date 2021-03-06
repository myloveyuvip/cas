var preSaleOrderRecApp = angular.module("preSaleOrderRecApp", ["casOrderServiceApp", 'ui.select', "bsTable",
    "ngSelect2", "orgCompApp", "staffCompApp", "sceneTypeCompApp", "regionCompApp", "saas.iframe", "ngJqueryFileUploadApp"]);
preSaleOrderRecApp.controller("preSaleOrderRecCtrl", ["$scope", "casOrderService", "commonService",
    "$compile", "$sce", function ($scope, casOrderService, commonService, $compile, $sce) {
        $scope.qryPreSaleOrderRecInfo = {};
        $scope.recInfoParam = {};
        $scope.singleBackPreOrderInfo = {};//退单信息
        $scope.loginSession = {}
        $scope.showOnClickBtn = false;
        var defaultConfig = {};
        //退单原因
        $scope.backReasonConfig = {
            minimumResultsForSearch: -1,
            data: [],
            placeholder: '请选择...'
        };
        //业务类型
        $scope.serviceTypeConfig = {
            minimumResultsForSearch: -1,
            data: [],
            placeholder: '请选择...'
        };
        //工单来源
        $scope.preOrderSrcsConfg = {
            minimumResultsForSearch: -1,
            data: [],
            placeholder: '请选择...'
        }
        //获取session区域信息
        $scope.regionList = [{
            regionName: ffc.util.isEmpty(ffc.context.loginInfo.regionName) ? "请选择..." : ffc.context.loginInfo.regionName,
            regionId: ffc.util.isEmpty(ffc.context.loginInfo.regionCd) ? null : ffc.context.loginInfo.regionCd,
            regionType: ffc.util.isEmpty(ffc.context.loginInfo.regionType) ? null : ffc.context.loginInfo.regionType
        }];
        //获取session团队信息
        $scope.orgList = [{
            orgName: ffc.util.isEmpty(ffc.context.loginInfo.orgName) ? "请选择..." : ffc.context.loginInfo.orgName,
            orgId: ffc.util.isEmpty(ffc.context.loginInfo.orgId) ? null : ffc.context.loginInfo.orgId
        }];
        //初始化员工控件
        $scope.staffList = [{
            staffName: "请选择...",
            staffId: null
        }];

        /**
         * 初始化配置信息
         */
        if ("1" == ffc.context.loginInfo.c3c4Assign || "1" == ffc.context.loginInfo.c3c4orgAssign) {
            $scope.regionList = [{
                regionName: "所有",
                regionId: null
            }];
            $scope.orgList = [{
                orgName: "所有",
                orgId: null
            }];
            defaultConfig.regionName = "所有";
            defaultConfig.regionId = null;
            defaultConfig.orgName = "所有";
            defaultConfig.orgId = null;
        } else if ("1" == ffc.context.loginInfo.orgAssign) {
            $scope.regionList = [{
                regionName: "所有",
                regionId: null,
                btnDis: true
            }];
            $scope.orgList = [{
                orgName: "所有",
                orgId: null
            }];
            defaultConfig.regionName = "所有";
            defaultConfig.regionId = null;
            defaultConfig.orgName = "所有";
            defaultConfig.orgId = null;
        } else if ("1" == ffc.context.loginInfo.notC3c4orgAssign) {
            $scope.regionList = [{
                regionName: "所有",
                regionId: null
            }];
            defaultConfig.regionName = "所有";
            defaultConfig.regionId = null;
        } else if ("1" == ffc.context.loginInfo.notConfigured) {  //未配置
            $scope.regionList = [{
                regionName: "未配置",
                regionId: null,
                btnDis: true
            }];
            $scope.orgList = [{
                orgName: "未配置",
                orgId: null,
                btnDis: true
            }];
            $scope.staffList[0].btnDis = true;
            defaultConfig.regionName = "未配置";
            defaultConfig.regionId = null;
            defaultConfig.orgName = "未配置";
            defaultConfig.orgId = null;
        }
        //时间选择器
        $('.form_date').datetimepicker({
            language: 'zh-CN',
            weekStart: 1,
            todayBtn: 1,
            autoclose: 1,
            todayHighlight: 1,
            startView: 2,
            minView: 2,
            forceParse: 0
        });
        // tab切换触发angular事件
        ffc.util.tabsShownTriggerNg();
        // 表格初始化
        $("#preSaleOrderRecTable").bootstrapTable({
            rowAttributes: function (row, index) {
                return {
                    "title": "销售人员联系号码:" + row.mobilePhone
                };
            },
            // 加载成功后重新编译，试点formatter后的dom元素支持angularjs
            onResetView: function () {
                $compile($('#preSaleOrderRecTable'))($scope);
            },
            // 条件
            queryParams: function (params) {
                //查询条件
                var selfParams = {
                    preOrderNumber: $scope.qryPreSaleOrderRecInfo.preOrderNumber,
                    orgId: ffc.util.isEmpty($scope.orgList) ? null : $scope.orgList[0].orgId, //预受理团队
                    createStaff: ffc.util.isEmpty($scope.staffList) ? null : $scope.staffList[0].staffId, //预受理员工
                    commonRegionId: ffc.util.isEmpty($scope.regionList) ? null : $scope.regionList[0].regionId, //预受理区域
                    regionType: ffc.util.isEmpty($scope.regionList) ? null : $scope.regionList[0].regionType, //区域类型
                    beginTime: ffc.util.tableDateFormatter($scope.qryPreSaleOrderRecInfo.beginTime?$scope.qryPreSaleOrderRecInfo.beginTime+" 00:00:00":null),
                    endTime: ffc.util.tableDateFormatter($scope.qryPreSaleOrderRecInfo.endTime?$scope.qryPreSaleOrderRecInfo.endTime+" 23:59:59":null),
                    sceneType: $scope.sceneTypeData,
                    serviceType: $scope.qryPreSaleOrderRecInfo.serviceType,
                    preOrderSrc: $scope.qryPreSaleOrderRecInfo.preOrderSrc,
                    from: 'rec',
                    fromQryBtn: $scope.fromQryBtn,
                    acrossOrgDTO : ffc.context.loginInfo.acrossOrgDTO,
                    status: '11000,' //传接收状态，

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
                        casOrderService.qryPreSaleOrderPools(render.data, function (result) {
                            $scope.showOnClickBtn = false;
                            if (!result.result) {
                                if (!ffc.util.isEmpty(result.msgTitle)) {
                                    MESSAGE_DIALOG.warning(result.msgTitle + "");
                                }
                                ffc.util.loadTableData(render);
                                return false;
                            }
                            ffc.util.setPageResult($scope, 'preSaleOrderRecErrorAlert', render, result);
                        }, function (msg) {
                            MESSAGE_DIALOG.error("查询异常" + msg);
                        });
                    }
                } catch (e) {
                    ffc.util.loadTableData(render);
                }
            },
            // 点击行事件
            onClickRow: function (row, $element) {
                $scope.preOrderPoolInfo = {};
                // 点击行后 更改流程id
                $scope.preOrderId = !ffc.util.isEmpty(row.preOrderId) ? row.preOrderId : "";
                $scope.preOrderNbr = !ffc.util.isEmpty(row.preOrderNbr) ? row.preOrderNbr : "";
                $scope.preStatusCd = !ffc.util.isEmpty(row.statusCd) ? row.statusCd : "";
                $scope.showOnClickBtn = true;
                $scope.preOrderPoolInfo = row;
                // 点击行后 触发当前tab
                ffc.util.tabsActiveTriggerNg(["#b1_c1", "#b1_c2"]);
            }
        });
        //查询
        $scope.qryPreSaleOrderRec = function () {
            $scope.fromQryBtn = "qrybtn";
            $("#preSaleOrderRecProcTable").bootstrapTable('removeAll');
            $("#preSaleOrderDetailTable").bootstrapTable('removeAll');

            $("#preSaleOrderRecTable").bootstrapTable("selectPage", 1);
            preSaleAccount();
        }
        //重置
        $scope.resetQryInfo = function () {
            $scope.qryPreSaleOrderRecInfo = {}
            //获取session区域信息
            $scope.regionList = [{
                regionName:ffc.util.isEmpty(defaultConfig.regionName) ? null :defaultConfig.regionName,
                regionId: ffc.util.isEmpty(defaultConfig.regionId) ? null :defaultConfig.regionId,
            }];
            //获取session团队信息
            $scope.orgList = [{
                orgName: ffc.util.isEmpty(defaultConfig.orgName) ? null :defaultConfig.orgName,
                orgId: ffc.util.isEmpty(defaultConfig.orgId) ? null :defaultConfig.orgId
            }];
            $scope.staffList = [{
                staffName: "请选择...",
                staffId: null
            }];
            initAllListBox();
        }
        /**
         * 计算工单池数量
         * 1、接收的工单数
         * 2、团队工单池总数
         * 3、其中未被接收数
         */
        var preSaleAccount = function () {
            $scope.qryAmountInfo = {}
            var acrossOrgDTO = ffc.context.loginInfo.acrossOrgDTO;
            casOrderService.qryPreSaleOrderRecAmount(acrossOrgDTO, function (result) {
                if (result.result && result.object != null) {
                    $scope.qryAmountInfo.countAll = result.object.staffReceivedAmount;
                    $scope.qryAmountInfo.countTD = result.object.orgAmount;
                    $scope.qryAmountInfo.countWJS = result.object.unAcceptAmount;
                    $scope.qryAmountInfo.staffName = result.object.staffName;
                } else {
                    MESSAGE_DIALOG.warning("" + result.msgTitle)
                }
            }, function (msg) {
                MESSAGE_DIALOG.error("" + msg)
            })
        }
        // 页面下拉框数据
        var initAllListBox = function () {
            casOrderService.queryAllBoxListForPreSaleOrderPool("", function (result) {
                //状态类型
                if (!ffc.util.isEmpty(result)
                    && !ffc.util.isEmpty(result.object)) {
                    //撤销原因
                    if (0 < result.object.backReasonList.length) {
                        $scope.backReasonTypes = result.object.backReasonList;
                        $scope.backReasonConfig.data = $scope.backReasonTypes;
                        $scope.singleBackPreOrderInfo.backReason = $scope.backReasonTypes[0];
                        $scope.singleBackPreOrderInfo.backReasonId = "";
                    }
                    //业务类型
                    if (result.object.serviceTypeList.length > 0) {
                        $scope.serviceTypes = result.object.serviceTypeList;
                        $scope.serviceTypeConfig.data = $scope.serviceTypes;
                        $scope.qryPreSaleOrderRecInfo.serviceType = "";
                    }
                    //受理类型
                    if (result.object.sceneTypeList.length > 0) {
                        $scope.sceneTypes = result.object.sceneTypeList;
                        var dataList = [];
                        for (var i = 0; i < $scope.sceneTypes.length; i++) {
                            var sceneTypeList = {};
                            sceneTypeList['sceneType'] = $scope.sceneTypes[i].attrValue;
                            sceneTypeList['sceneTypeName'] = $scope.sceneTypes[i].attrValueName;
                            dataList.push(sceneTypeList);
                        }
                        $scope.sceneTypeList = dataList;
                    }
                    //工单来源
                    if (result.object.preOrderSrcs.length > 0) {
                        $scope.preOrderSrcs = result.object.preOrderSrcs;
                        $scope.preOrderSrcsConfg.data = $scope.preOrderSrcs;
                        $scope.qryPreSaleOrderRecInfo.preOrderSrc = "";
                    }
                }

            }, function (res) {
                MESSAGE_DIALOG.error(res);
            });
        };

        // 初始化
        commonService.initFrom(ffc.util.getPageMetas($scope), function () {
            //preSaleAccount();
            initAllListBox();
            $('#preSaleOrderRecTable tbody').delegate('tr', 'click', function () {
                $(this).addClass("tr-onClick").siblings("tr").removeClass("tr-onClick");
            });
        });
        //接收工单
        $scope.recPreSaleOrder = function () {
            MESSAGE_DIALOG.loading();
            var acrossOrgDTO = ffc.context.loginInfo.acrossOrgDTO;
            casOrderService.recPreSaleOrderData(acrossOrgDTO, function (result) {
                if (result.result) {
                    MESSAGE_DIALOG.close();
                    preSaleAccount();
                    $scope.qryPreSaleOrderRec();
                } else {
                    MESSAGE_DIALOG.warning("接收失败" + result.msgTitle);
                }
            }, function (msg) {
                MESSAGE_DIALOG.error("" + msg);
            });
        }
        //工单分布
        $scope.qryPreOrderDistribute = function () {
            $('#pre-order-distribute').modal('show');
            $('#preOrderDistributeTable').bootstrapTable('showLoading');
            casOrderService.qryPreOrderDistributeData("", function (result) {
                $('#preOrderDistributeTable').bootstrapTable('hideLoading');
                if (!result.result || undefined == result.dataList
                    || result.dataList.length == 0) {
                    return false;
                }
                $('#preOrderDistributeTable').bootstrapTable("load", result.dataList);
            }, function (msg) {
                MESSAGE_DIALOG.error("" + msg);
            });
        }
        //关闭工单分布页面
        $scope.closePreOrder = function () {
            $('#pre-order-distribute').modal('hide');
        }

        //P单详情
        $scope.qryPreOrderDetail = function () {
            if (undefined == $scope.preOrderId) {
                MESSAGE_DIALOG.warning("请先选择一条记录!");
                return false;
            }
            if (ffc.util.isEmpty($scope.preOrderNbr)) {
                MESSAGE_DIALOG.alert("您选中的记录的P单编号为空!.");
                return false;
            }
            var preOrderNbr = $scope.preOrderNbr;
            // 动态依赖注入
            var injector = angular.injector(["casOrderServiceApp", "ng"]);
            injector.invoke(['casOrderService', function (casOrderService) {
                var param = "pageCls=dtOrder&keyFlag=0&keyType=bycustsonbr&keyValue=" + preOrderNbr + "&auto=1";
                casOrderService.invokeToOrder(param);
            }]);
        }
        //退单
        $scope.sendBackPreOrder = function (rowIndex, event) {
            $scope.singleBackPreOrderInfo = {};
            event.stopPropagation();
            var allTable = $('#preSaleOrderRecTable')
                .bootstrapTable("getData");
            var selectRow = allTable[rowIndex];
            $scope.singleBackPreOrderInfo = selectRow;
            $('#single_back_pre_order').modal('show');
        }
        //确认退单
        $scope.confirmSendBackPreOrder = function () {
            $scope.singleBackPreOrderInfo.preOrderNbr
                = $scope.singleBackPreOrderInfo.preOrderNbr;
            if (undefined == $scope.singleBackPreOrderInfo.backReason
                || undefined == $scope.singleBackPreOrderInfo.backReason.attrId) {
                MESSAGE_DIALOG.warning("请选择一种退单原因!");
                return;
            }
            $scope.singleBackPreOrderInfo.backReasonMsg
                = $scope.singleBackPreOrderInfo.backReason.attrValueName;
            $scope.singleBackPreOrderInfo.cancelReasonId
                = $scope.singleBackPreOrderInfo.backReason.attrValueId;
            if (ffc.util.isEmpty($scope.singleBackPreOrderInfo.backReasonMsg)) {
                MESSAGE_DIALOG.warning("原因描述不能为空!");
                return;
            }
            $scope.singleBackPreOrderInfo.attrValueDTO = $scope.singleBackPreOrderInfo.backReason;
            $scope.singleBackPreOrderInfo.cancelReasonValue = $scope.singleBackPreOrderInfo.backReasonId;
            MESSAGE_DIALOG.loading();
            casOrderService.sendBackPreOrder($scope.singleBackPreOrderInfo, function (result) {
                if (result.result) {
                    MESSAGE_DIALOG.alert("退单成功!");
                    $scope.qryPreSaleOrderRec();
                    preSaleAccount();
                    $('#single_back_pre_order').modal('hide');
                } else {
                    MESSAGE_DIALOG.warning("退单失败!" + result.msgTitle);
                }
            }, function (msg) {
                MESSAGE_DIALOG.error("" + msg);
            });
        }
        //发送短信
        $scope.sendMessage = function (rowIndex, event) {
            $scope.sendMessageInfo = {};
            event.stopPropagation();
            var allTable = $('#preSaleOrderRecTable')
                .bootstrapTable("getData");
            var selectRow = allTable[rowIndex];
            $scope.sendMessageInfo = selectRow;
            $("#send_message_pre_order").modal('show');
        }
        //确认发送短信
        $scope.confirmSendMessage = function () {
            if (undefined == $scope.sendMessageInfo.mobilePhone ||
                undefined == $scope.sendMessageInfo.messageValue) {
                MESSAGE_DIALOG.warning("联系电话和短信内容为必填项.");
                return;
            }
            casOrderService.sendMessageData($scope.sendMessageInfo, function (result) {
                if (result.result) {
                    $("#send_message_pre_order").modal('hide');
                    MESSAGE_DIALOG.alert("" + result.msgTitle);
                } else {
                    MESSAGE_DIALOG.warning("发送短信失败." + result.msgTitle)
                }
            }, function (msg) {
                MESSAGE_DIALOG._error("" + msg);
            })
        }

        //设置为全部受理
        $scope.returnToAllAccept = function (rowIndex, event) {
            $scope.recInfoParam = {};
            event.stopPropagation();
            var allTable = $('#preSaleOrderRecTable')
                .bootstrapTable("getData");
            var selectRow = allTable[rowIndex];
            $scope.recInfoParam = selectRow;
            MESSAGE_DIALOG.loading();
            casOrderService.returnToAllAcceptData($scope.recInfoParam, function (result) {
                if (result.result) {
                    MESSAGE_DIALOG.alert("设置为全部受理成功!");
                    $scope.qryPreSaleOrderRec();
                    preSaleAccount();
                } else {
                    MESSAGE_DIALOG.warning("设置为全部受理失败" + result.msgTitle);
                }
            }, function (msg) {
                MESSAGE_DIALOG.error("" + msg);
            })
        }
        //记录受理备注
        $scope.addRemarkInfo = function () {
            $scope.saveRemarkInfo = {};
            if (undefined == $scope.preOrderId) {
                MESSAGE_DIALOG.warning("请先选择一条记录!");
                return false;
            }
            $scope.saveRemarkInfo.preOrderNbr = $scope.preOrderNbr;
            $scope.saveRemarkInfo.preOrderId = $scope.preOrderId;
            $('#add_order_remark').modal('show');
        }
        //确认记录受理备注
        $scope.confirmAddRemarkInfo = function () {
            MESSAGE_DIALOG.loading();
            casOrderService.saveRemark($scope.saveRemarkInfo, function (result) {
                if (result.result) {
                    MESSAGE_DIALOG.alert("保存成功!");
                    window.setTimeout("MESSAGE_DIALOG.close()", 1000);
                    $('#add_order_remark').modal('hide');
                } else {
                    MESSAGE_DIALOG.alert("保存失败!:" + result.msgTitle);
                }

            }, function (msg) {
                MESSAGE_DIALOG.error("" + msg);
            });
        }
        //展示照片 $sce.trustAsResourceUrl("data:image/png;base64,"+result.object)
        $scope.showPic = function () {
            if (undefined == $scope.preOrderId) {
                MESSAGE_DIALOG.warning("请先选择一条记录!");
                return false;
            }
            $scope.options = {
                src: '',
                height: '500',
                width: '750',
                scrolling: 'yes'
            }
            $scope.inParam = {};
            $scope.inParam.preOrderId = $scope.preOrderId;
            $scope.inParam.preOrderNbr = $scope.preOrderNbr;
            $scope.inParam =$scope.preOrderPoolInfo;
            MESSAGE_DIALOG.loading();
            casOrderService.showPic($scope.inParam, function (result) {
                MESSAGE_DIALOG.close();
                if (result.result) {
                    $scope.options.src = result.object + "";
                    $('#show_pic').modal('show');
                } else {
                    MESSAGE_DIALOG.warning(result.msgTitle + "")
                }
            }, function (msg) {
                MESSAGE_DIALOG.error("" + msg);
            });
        }
        /*//附件上传
         $scope.upload = function () {
         if (undefined == $scope.preOrderId) {
         MESSAGE_DIALOG.warning("请先选择一条记录!");
         return false;
         }
         if ($scope.uploadFile) {
         $scope.doUpload($scope.uploadFile);
         }
         }
         //执行上传
         $scope.doUpload = function (file) {
         MESSAGE_DIALOG.loading();
         Upload.upload(
         {
         url: ffc.context.contextPath + '/order/preSaleOrderPool/uploadFile',
         data: {
         uploadFile: file,
         preOrderId: $scope.preOrderId
         }
         }).then(
         function (resp) {
         //console.log(resp);
         if (undefined != resp.data && false == resp.data.result) {
         MESSAGE_DIALOG.error("附件: '" + resp.config.data.uploadFile.name
         + " ',上传失败:\n" + resp.data.msgTitle);
         return false;
         }
         MESSAGE_DIALOG.alert("附件上传成功!");
         window.setTimeout("MESSAGE_DIALOG.close()", 1000);
         }, function (resp) {
         if (undefined != resp.data && false == resp.data.result) {
         MESSAGE_DIALOG.error("附件" + resp.config.data.uploadFile.name
         + "上传失败:\n" + resp.data.msgTitle);
         }
         }, function (evt) {

         });
         }*/
        /**
         * 附件上传
         */
        $scope.submit = function (e, data) {
            data.formData =
            {
                preOrderId: $scope.preOrderId
            }
        }

        $scope.uploadFinished = function (e, data) {
            if (!ffc.util.isEmpty(data.result) && !ffc.util.isEmpty(data.result.result)
                && data.result.result == true) {
                MESSAGE_DIALOG.alert("文件：" + data.files[0].name + "，上传成功！");
            } else {
                MESSAGE_DIALOG.error("文件上传失败，请核查！" + data.result.msgTitle);
            }
        }
        $scope.fail = function (e, data) {
            MESSAGE_DIALOG.error("文件上传失败，请核查！" + data.result.msgTitle);
        }
        $scope.progress = function (e, data) {
            MESSAGE_DIALOG.loading();
        }

    }]);
/**
 * 格式化列
 * @param value
 * @param row
 * @param index
 */
var recFormatter = function (value, row, index) {
    var firstBtn = '<a ng-click="sendBackPreOrder(' + index + ',$event)">退单</a>';
    var secondBtn = '<a ng-click="sendMessage(' + index + ',$event)">发送短信</a>';
    var thirdBtn;
    //判断关联的订单中，是否存在预受理状态的订单.
    if (!ffc.util.isEmpty(row.allAcceptFlag)) {
        thirdBtn = '<a ng-click="returnToAllAccept(' + index + ',$event)">设置为全部受理</a>'
    }
    firstBtn = ffc.util.isEmpty(firstBtn) ? "" : firstBtn + " | ";
    secondBtn = ffc.util.isEmpty(secondBtn) ? "" : secondBtn;
    thirdBtn = ffc.util.isEmpty(thirdBtn) ? "" : thirdBtn + " | ";
    return firstBtn + thirdBtn + secondBtn;
}


