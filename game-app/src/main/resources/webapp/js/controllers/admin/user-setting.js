gameApp.controller('adminUserSettingController', function ($scope, $http, $timeout, $log, $modal, growlCenterService) {
    $scope.data = {
        "username": localStorage.getItem('currentUser'),
        "currentPwd": "",
        "newPwd": "",
        "confirmNewPwd": ""
    };
    
    $scope.refresh = function () {
    	$scope.data.currentPwd = "";
    	$scope.data.newPwd = "";
    	$scope.data.confirmNewPwd = "";
    }

    $scope.ok = function () {
        var url = GAME_URL_ADMIN_USER + "/change-password";
        var data = $scope.data;

        setJson($scope, $log, $http, $timeout, url, data,
            function (data, status) {
                if (data) {
                    growlCenterService.growl('密码已经被更新', 'success')
                } else {
                    growlCenterService.growl('更新失败，当前密码不正确', 'warning')
                }

                $scope.refresh();
                $log.info("Change password: ", data, ", status: ", status);
            },
            function (data, status) {
                growlCenterService.growl(data, 'danger')
                $log.info("Failed to change password: ", data, ", status: ", status);
            }
        );
    };
});