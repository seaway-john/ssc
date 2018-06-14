gameApp.controller('headerController', function ($scope, $http, $timeout, $log, $modal) {

    var getVersion = function () {
        var url = GAME_URL_HEADER + "/version";
        getJson($scope, $log, $http, $timeout, url,
            function (data, status) {
                $scope.version = "v" + data.version;
                $log.info("Get image info: ", data, ", status: ", status);
            },
            function (data, status) {
                $log.error("Failed to get image info: ", data, ", status: ", status);
            }
        );
    };

    $scope.fullScreen = function () {
        //Launch
        function launchIntoFullscreen(element) {
            if (element.requestFullscreen) {
                element.requestFullscreen();
            } else if (element.mozRequestFullScreen) {
                element.mozRequestFullScreen();
            } else if (element.webkitRequestFullscreen) {
                element.webkitRequestFullscreen();
            } else if (element.msRequestFullscreen) {
                element.msRequestFullscreen();
            }
        }

        //Exit
        function exitFullscreen() {
            if (document.exitFullscreen) {
                document.exitFullscreen();
            } else if (document.mozCancelFullScreen) {
                document.mozCancelFullScreen();
            } else if (document.webkitExitFullscreen) {
                document.webkitExitFullscreen();
            }
        }

        if (exitFullscreen()) {
            launchIntoFullscreen(document.documentElement);
        } else {
            launchIntoFullscreen(document.documentElement);
        }
    }


    //Skin Switch
    $scope.currentSkin = localStorage.getItem('currentSkin');
    if (isEmpty($scope.currentSkin)) {
        $scope.currentSkin = 'bluegray';
        localStorage.setItem('currentSkin', 'bluegray');
    }

    $scope.skinList = [
        'bluegray',
        'cyan',
        'teal',
        'blue',
        'purple'
    ]

    $scope.skinSwitch = function (color) {
        $scope.currentSkin = color;
        localStorage.setItem('currentSkin', color);
    }
    
    getVersion();
});