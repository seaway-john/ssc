gameApp.controller('configQrCodeController', function ($scope, $http, $timeout, $log, $modal, $upload) {
    $scope.data = {};

    $scope.refresh = function () {
        var url = GAME_URL_CONFIG_QRCODE + "/refresh";
        getJson($scope, $log, $http, $timeout, url,
            function (data, status) {
                $scope.data.invitedCode = data.state;
                $scope.data.wechatUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + data.appId + "&redirect_uri=" + data.url + "&response_type=code&scope=snsapi_userinfo&state=" + data.state + "#wechat_redirect";
                $scope.data.qrCodeUrl = "http://pan.baidu.com/share/qrcode?w=300&h=300&url=" + encodeURIComponent($scope.data.wechatUrl);
            },
            function (data, status) {
            }
        );
    }

    $scope.reGenerate = function () {
        var url = GAME_URL_CONFIG_QRCODE + "/re-generate";
        getJson($scope, $log, $http, $timeout, url,
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