var modalSetJson = function ($scope, $http, $timeout, $log, $modalInstance, data) {
    $scope.modal = data;

    $scope.ok = function () {
        var url = $scope.modal.url + "/" + $scope.modal.action;
        setJson($scope, $log, $http, $timeout, url, $scope.modal.data,
            function (data, status) {
                $modalInstance.close([data, status, true]);
            },
            function (data, status) {
                $modalInstance.close([data, status, false]);
            }
        );
    }

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    }
}

var modalGetJson = function ($scope, $http, $timeout, $log, $modalInstance, data) {
    $scope.modal = data;

    $scope.ok = function () {
        var url = $scope.modal.url + "/" + $scope.modal.action;
        getJson($scope, $log, $http, $timeout, url,
            function (data, status) {
                $modalInstance.close([true, data, status]);
            },
            function (data, status) {
                $modalInstance.close([false, data, status]);
            }
        );
    }

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    }
}

var modalController = function ($scope, $modalInstance, data) {
    $scope.modal = data;

    $scope.ok = function () {
        $modalInstance.close($scope.modal.data);

    }

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    }
}