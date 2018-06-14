var portalSigninApp = angular.module('portalSigninApp', [
    'ngAnimate',
    'ngResource',
    'ui.router',
    'ui.bootstrap',
    'angular-loading-bar',
    'oc.lazyLoad',
    'nouislider',
    'angularFileUpload'
]);

portalSigninApp.controller('signinController', function($scope, $log) {
    $scope.current_user = localStorage.getItem('currentUser');

    $scope.checkForm = function() {
        if ($scope.signinForm.$invalid) {
            return false;
        }

        return true;
    }
});
