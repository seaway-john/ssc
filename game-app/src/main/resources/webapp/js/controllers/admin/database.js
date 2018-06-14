gameApp.controller('adminDatabaseController', function ($scope, $http, $timeout, $log, $upload, $modal, growlCenterService) {
    $scope.data = [];

    $scope.gridOptions = getGridOptions($scope, UI_GRID_MULTI_SELECT);
    $scope.gridOptions.columnDefs = [
        {
            field: 'fileName',
            displayName: '文件名'
        },
        {
            field: 'lastModifiedTime',
            displayName: '更新时间',
            cellFilter: DATE_FORMAT
        },
        {
            field: 'size',
            displayName: '大小（Byte）',
            cellFilter: 'dataToFix'
        }
    ];

    $scope.refresh = function () {
        var url = GAME_URL_ADMIN_DATABASE + "/refresh";
        getJson($scope, $log, $http, $timeout, url,
            function (data, status) {
                $scope.data = data;
                $scope.gridApi.selection.clearSelectedRows();
                $log.info("Get database: ", data, ", status: ", status);
            },
            function (data, status) {
                $log.info("Failed to get database: ", data, ", status: ", status);
            }
        );
    };

    $scope.backup = function () {
        var url = GAME_URL_ADMIN_DATABASE + "/backup";
        getJson($scope, $log, $http, $timeout, url,
            function (data, status) {
                if (data.status) {
                    growlCenterService.growl(data.message, 'success');
                } else {
                    growlCenterService.growl(data.message, 'warning');
                }
                $scope.refresh();
                $log.info("Backup database: ", data, ", status: ", status);
            },
            function (data, status) {
                growlCenterService.growl(data, 'error');
                $log.info("Failed to backup database: ", data, ", status: ", status);
            }
        );
    };

    $scope.restore = function () {
        $modal.open({
            templateUrl: 'templates/common/confirm.html',
            controller: modalController,
            resolve: {
                data: function () {
                    var data = {
                        "title": "恢复数据库",
                        "content": "确认恢复所选择的数据库？",
                        "data": $scope.selectedData[0]
                    };
                    return data;
                }
            }
        }).result.then(function (data) {
        	var fileName = data.fileName;
            setJson($scope, $log, $http, $timeout, GAME_URL_ADMIN_DATABASE + "/restore", data,
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
        }, function () {
            $log.info('Modal dismissed at: ' + new Date());
        });
    };

    $scope.upload = function () {
        $("#uploadFileButton").click();
    }

    $scope.onFileSelect = function ($files) {
        var url = GAME_URL_ADMIN_DATABASE + "/upload";
        for (var i = 0; i < $files.length; i++) {
            var file = $files[i];
            $log.debug("Printing file: " + file);

            uploadFile($scope, $log, $upload, $timeout, url, file,
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
                }
            );
        }
    };

    $scope.delete = function () {
        $modal.open({
            templateUrl: 'templates/common/confirm.html',
            controller: modalSetJson,
            resolve: {
                data: function () {
                    var data = {
                        "title": "删除数据库",
                        "action": "delete",
                        "content": "确认删除所选择的数据库？",
                        "url": GAME_URL_ADMIN_DATABASE,
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

    $scope.refresh();
});