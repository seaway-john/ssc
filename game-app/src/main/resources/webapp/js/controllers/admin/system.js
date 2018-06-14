gameApp.controller('adminSystemController', function ($scope, $http, $state, $timeout, $log, $modal, growlCenterService) {
    var getData = function (val, free) {
        return [
            {
                x: "Used",
                y: val,
                color: COLOR_CYAN
            },
            {
                x: "Free",
                y: free,
                color: COLOR_GREEN
            }];
    }

    var getOption = function (tle) {
        return {
            chart: {
                type: 'pieChart',
                height: 150,
                donut: true,
                title: tle,
                x: function (d) { return d.x; },
                y: function (d) { return d.y; },
                showLabels: false,
                showLegend: false,
                pie: {
                    startAngle: function (d) { return d.startAngle },
                    endAngle: function (d) { return d.endAngle }
                },
                tooltip: {
                    valueFormatter: function (d, i) {
                        return d.toFixed(2) + " %";
                    }
                },
                duration: 500,
                labelThreshold: 0.05,
                labelSunbeamLayout: false,
                legend: {
                    margin: {
                        top: 0,
                        right: 0,
                        bottom: 0,
                        left: 0
                    }
                }
            }
        }
    };

    $scope.refresh = function () {
        var url = GAME_URL_ADMIN_SYSTEM + "/process/refresh";
        getJson($scope, $log, $http, $timeout, url,
            function (data, status) {
                _.forEach(data, function (d) {
                    var cpuRate = 0;
                    var ramRate = 0;
                    _.forEach(d.info, function (i) {
                        cpuRate += i.cpuRate;
                        ramRate += i.ramRate;
                        i.ram = nvd3FormatFromBit(i.ram * 1024);
                    });
                    d.cpuData = getData(cpuRate, 100 - cpuRate);
                    d.ramData = getData(ramRate, 100 - ramRate);
                })

                $scope.data = [];
                var datas = [];
                for (var i = 0; i < data.length; i++) {
                    datas.push(data[i]);

                    if (datas.length >= 2) {
                        $scope.data.push(datas);
                        datas = [];
                    }
                }
            },
            function (data, status) {
            }
        );
    };

    $scope.restart = function (type) {
        var url = GAME_URL_ADMIN_SYSTEM + "/process/restart?name=" + type;
        getJson($scope, $log, $http, $timeout, url,
            function (data, status) {
                if (data.status) {
                    growlCenterService.growl(data.message, 'success');
                } else {
                    growlCenterService.growl(data.message, 'warning');
                }
                $scope.refresh();
            },
            function (data, status) {
                growlCenterService.growl(data, 'error');
                $scope.refresh();
            }
        );
    }

    $scope.reboot = function () {
        $modal.open({
            templateUrl: 'templates/common/confirm.html',
            controller: modalGetJson,
            resolve: {
                data: function () {
                    var data = {
                        "title": "重启系统",
                        "action": "reboot",
                        "content": "确认重启系统？一定要在凌晨未使用期间操作",
                        "url": GAME_URL_ADMIN_SYSTEM
                    };
                    return data;
                }
            }
        }).result.then(function (data, status) {
            if (data[0]) {
                if (data[1].status) {
                    growlCenterService.growl(data[1].message, 'success');
                } else {
                    growlCenterService.growl(data[1].message, 'warning');
                }
            } else {
                growlCenterService.growl(data[1], 'error');
            }
        }, function (data, status) {
            growlCenterService.growl(data, 'error');
        });
    };

    $scope.cpuOp = getOption("CPU");
    $scope.ramOp = getOption("RAM");

    $scope.refresh();
});