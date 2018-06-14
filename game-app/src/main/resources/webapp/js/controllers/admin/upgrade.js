gameApp.controller('adminUpgradeController', function ($scope, $http, $state, $timeout, $log, $upload, $modal, growlCenterService) {
    $scope.image = {};
    $scope.data = [];

    $scope.gridOptions = getGridOptions($scope, UI_GRID_MULTI_SELECT);
    $scope.gridOptions.columnDefs = [
        {
            field: 'fileName',
            displayName: 'File Name'
        },
        {
            field: 'lastModifiedTime',
            displayName: 'Last Modified',
            cellFilter: DATE_FORMAT
        },
        {
            field: 'size',
            displayName: 'Size(B)',
            cellFilter: 'dataToFix'
        }
    ];

    $scope.getImageInfo = function () {
        var url = GAME_URL_ADMIN_UPGRADE + "/image-info";
        getJson($scope, $log, $http, $timeout, url,
            function (data, status) {
                $scope.image = data;
                $log.info("Get image info: ", data, ", status: ", status);
            },
            function (data, status) {
                $log.error("Failed to get image info: ", data, ", status: ", status);
            }
        );
    };

    $scope.refresh = function () {
        var url = GAME_URL_ADMIN_UPGRADE + "/refresh";
        getJson($scope, $log, $http, $timeout, url,
            function (data, status) {
                $scope.data = data;
                $scope.gridApi.selection.clearSelectedRows();
                $log.info("Get upgrade: ", data, ", status: ", status);
            },
            function (data, status) {
                $log.error("Failed to get upgrade: ", data, ", status: ", status);
            }
        );
    };

    $scope.upgrade = function () {
        $modal.open({
            templateUrl: 'templates/common/confirm.html',
            controller: modalSetJson,
            resolve: {
                data: function () {
                    var data = {
                        "title": "Upgrade Image",
                        "action": "upgrade",
                        "content": "It will lose connectivity for 1-2 minutes, Do you want to upgrade the selected image?",
                        "url": GAME_URL_ADMIN_UPGRADE,
                        "data": $scope.selectedData[0]
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

    $scope.upload = function () {
        $("#uploadFileButton").click();
    }

    $scope.onFileSelect = function ($files) {
        var url = GAME_URL_ADMIN_UPGRADE + "/upload";
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
                	growlCenterService.growl(data.message, 'warning');
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
                        "title": "Delete Version",
                        "action": "delete",
                        "content": "Do you want to delete the selected file?",
                        "url": GAME_URL_ADMIN_UPGRADE,
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

    $scope.getImageInfo();
    $scope.refresh();
});
