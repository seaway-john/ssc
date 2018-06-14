gameApp.controller('configAgentsController', function ($scope, $http, $timeout, $log, $modal) {
    $scope.data = [];
    $scope.isAdmin = isAdminRole();

    $scope.gridOptions = getGridOptions($scope, UI_GRID_MULTI_SELECT);
    $scope.gridOptions.columnDefs = [
        {
            field: 'wxNickName',
            displayName: '微信昵称'
        },
        {
            field: 'wxUsername',
            displayName: '微信号'
        },
        {
            field: 'wxSex',
            displayName: '性别',
            cellTemplate: getSexCell("wxSex")
        },
        {
            field: 'enabled',
            displayName: '同意',
            cellTemplate: getBooleanCell("enabled")
        },
        {
            field: 'description',
            displayName: '备注'
        },
        {
            field: 'balance',
            displayName: '当前余粮'
        },
        {
            field: 'betInvest',
            displayName: '总押粮'
        },
        {
            field: 'betIncome',
            displayName: '总中粮'
        },
        {
            field: 'bankerUsername',
            displayName: '上庄用户名'
        },
        {
            field: 'bankerPassword',
            displayName: '上庄密码'
        },
        {
            field: 'created',
            displayName: '时间',
            cellFilter: DATE_FORMAT
        }
    ];

    $scope.refresh = function () {
        var url = GAME_URL_CONFIG_AGENTS + "/refresh";
        getJson($scope, $log, $http, $timeout, url,
            function (data, status) {
                $scope.data = data;
                $scope.gridApi.selection.clearSelectedRows();
            },
            function (data, status) {
            }
        );
    }

    $scope.edit = function () {
        $scope.set = {
            "title": "修改",
            "action": "update",
            "url": GAME_URL_CONFIG_AGENTS,
            "data": angular.copy($scope.selectedData[0])
        };
        setAgent();
    }

    var setAgent = function () {
        $modal.open({
            templateUrl: 'templates/common/modal/config/agent.html',
            controller: modalSetJson,
            resolve: {
                data: function () {
                    return $scope.set;
                }
            }
        }).result.then(function (data) {
            $scope.refresh();
        }, function () {
            $scope.refresh();
        });
    };

    $scope.agreen = function () {
        _.remove($scope.selectedData, function (d) {
            return d.enabled;
        });

        $modal.open({
            templateUrl: 'templates/common/confirm.html',
            controller: modalSetJson,
            resolve: {
                data: function () {
                    var data = {
                        "title": "同意",
                        "action": "agreen",
                        "content": "同意选择的代理请求？",
                        "warning": [
                            "同意后，代理即将使能并创建一个该代理的房间"
                        ],
                        "url": GAME_URL_CONFIG_AGENTS,
                        "data": $scope.selectedData
                    };
                    return data;
                }
            }
        }).result.then(function (data) {
            $scope.refresh();
        }, function () {
            $scope.refresh();
        });
    };

    $scope.delete = function () {
        $modal.open({
            templateUrl: 'templates/common/confirm.html',
            controller: modalSetJson,
            resolve: {
                data: function () {
                    var data = {
                        "title": "删除",
                        "action": "delete",
                        "content": "删除该代理？",
                        "warning": [
                            "该代理相关的所有记录将会被删除"
                        ],
                        "url": GAME_URL_CONFIG_AGENTS,
                        "data": $scope.selectedData[0]
                    };
                    return data;
                }
            }
        }).result.then(function (data) {
            $scope.refresh();
        }, function () {
            $scope.refresh();
        });
    };

    $scope.ignore = function () {
        $modal.open({
            templateUrl: 'templates/common/confirm.html',
            controller: modalSetJson,
            resolve: {
                data: function () {
                    var data = {
                        "title": "忽略",
                        "action": "ignore",
                        "content": "忽略所有代理请求？",
                        "warning": [
                            "所有新的请求将会被删除"
                        ],
                        "url": GAME_URL_CONFIG_AGENTS,
                        "data": null
                    };
                    return data;
                }
            }
        }).result.then(function (data) {
            $scope.refresh();
        }, function () {
            $scope.refresh();
        });
    };

    $scope.refresh();
});
