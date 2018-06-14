gameApp.controller('adminUsersController', function ($scope, $http, $timeout, $log, $modal) {
    $scope.data = [];
    $scope.roles = [];
    $scope.currentUser = localStorage.getItem('currentUser');

    $scope.gridOptions = getGridOptions($scope, UI_GRID_RADIO_SELECT);
    $scope.gridOptions.columnDefs = [
        {
            field: 'username',
            displayName: '用户名'
        },
        {
            field: 'roleDescription',
            displayName: '权限'
        },
        {
            field: 'name',
            displayName: '名称'
        },
        {
            field: 'createdBy',
            displayName: '创建者'
        },
        {
            field: 'created',
            displayName: '创建时间',
            cellFilter: DATE_FORMAT
        },
        {
            field: 'lastUpdate',
            displayName: '更新时间',
            cellFilter: DATE_FORMAT
        },
        {
            field: 'enabled',
            displayName: '使能',
            cellTemplate: getBooleanCell("enabled")
        }
    ];

    $scope.refresh = function () {
        var url = GAME_URL_ADMIN_USER + "/refresh";
        getJson($scope, $log, $http, $timeout, url,
            function (data, status) {
                $scope.data = data;
                _.forEach($scope.data, function (user) {
                    _.forEach($scope.roles, function (role) {
                        if (role.roleName == user.roleName) {
                            user.roleDescription = role.description;
                            return false;
                        }
                    })
                });
                $scope.gridApi.selection.clearSelectedRows();
                $log.info("Get user: ", data, ", status: ", status);
            },
            function (data, status) {
                $log.info("Failed to get user: ", data, ", status: ", status);
            }
        );
    };

    $scope.getRoles = function () {
        var url = GAME_URL_ADMIN_USER + "/role/refresh";
        getJson($scope, $log, $http, $timeout, url,
            function (data, status) {
                $scope.roles = data;
                $log.info("Get roles: ", data, ", status: ", status);
                $scope.refresh();
            },
            function (data, status) {
                $log.info("Failed to get roles: ", data, ", status: ", status);
            }
        );
    };

    $scope.add = function () {
        $scope.set = {
            "title": "添加",
            "action": "add",
            "roles": $scope.roles,
            "currentUser": $scope.currentUser,
            "url": GAME_URL_ADMIN_USER,
            "data": {
                "roleName": $scope.roles[0].roleName,
                "enabled": true
            }
        };
        setUser();
    };

    $scope.edit = function () {
        $scope.set = {
            "title": "修改",
            "action": "edit",
            "roles": $scope.roles,
            "currentUser": $scope.currentUser,
            "url": GAME_URL_ADMIN_USER,
            "data": angular.copy($scope.selectedData[0])
        };
        delete $scope.set.data.roleDescription;
        setUser();
    };

    var setUser = function () {
        $modal.open({
            templateUrl: 'templates/common/modal/admin/users.html',
            controller: modalSetJson,
            resolve: {
                data: function () {
                    return $scope.set;
                }
            }
        }).result.then(function (data) {
            $scope.refresh();
        }, function () {
            $log.info('Modal dismissed at: ' + new Date());
        });
    };

    $scope.delete = function () {
        _.forEach($scope.selectedData, function (d) {
            delete d.roleDescription;
        });
        $modal.open({
            templateUrl: 'templates/common/confirm.html',
            controller: modalSetJson,
            resolve: {
                data: function () {
                    var data = {
                        "title": "删除用户",
                        "action": "delete",
                        "content": "确认删除所选择的用户？",
                        "url": GAME_URL_ADMIN_USER,
                        "data": $scope.selectedData
                    };
                    return data;
                }
            }
        }).result.then(function (data) {
            $scope.refresh();
        }, function () {
            $log.info('Modal dismissed at: ' + new Date());
        });
    };

    $scope.getRoles();
});