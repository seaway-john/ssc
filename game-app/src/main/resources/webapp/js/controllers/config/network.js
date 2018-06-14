gameApp.controller('configNetworkController', function ($scope, $http, $timeout, $log, $modal) {
    $scope.data = [];
    $scope.isAdmin = isAdminRole();

    $scope.gridOptions = getGridOptions($scope, UI_GRID_MULTI_SELECT);
    $scope.gridOptions.columnDefs = [
        {
            field: 'name',
            displayName: '名称'
        },
        {
            field: 'url',
            displayName: '线路地址'
        },
        {
            field: 'delay',
            displayName: '延迟（毫秒）'
        }
    ];

    $scope.refresh = function () {
        var url = GAME_URL_CONFIG_NETWORK + "/refresh";
        getJson($scope, $log, $http, $timeout, url,
            function (data, status) {
                $scope.data = data;
                $scope.gridApi.selection.clearSelectedRows();
            },
            function (data, status) {
            }
        );
    }

    $scope.poll = function () {
        var url = GAME_URL_CONFIG_NETWORK + "/poll";
        getJson($scope, $log, $http, $timeout, url,
            function (data, status) {
                $scope.refresh();
            },
            function (data, status) {
                $scope.refresh();
            }
        );
    }

    $scope.edit = function () {
        $scope.set = {
            "title": "修改",
            "action": "update",
            "url": GAME_URL_CONFIG_NETWORK,
            "data": angular.copy($scope.selectedData[0])
        };
        setNetwork();
    }

    var setNetwork = function () {
        $modal.open({
            templateUrl: 'templates/common/modal/config/network.html',
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

    $scope.refresh();
});
