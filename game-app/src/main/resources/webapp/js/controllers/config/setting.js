gameApp.controller('configSettingController', function ($scope, $http, $timeout, $log, $modal, $upload) {
    $scope.data = {};

    $scope.refresh = function () {
        var url = GAME_URL_CONFIG_SETTING + "/refresh";
        getJson($scope, $log, $http, $timeout, url,
            function (data, status) {
                $scope.data = data;
            },
            function (data, status) {
            }
        );
    }

    $scope.save = function () {
        var url = GAME_URL_CONFIG_SETTING + "/update";
        setJson($scope, $log, $http, $timeout, url, $scope.data,
            function (data, status) {
                $scope.refresh();
            },
            function (data, status) {
                $scope.refresh();
            }
        );
    }

    $scope.refresh();
});