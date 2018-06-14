gameApp.factory('portalHttpInterceptor', ['$q', '$rootScope', '$location', '$window', '$log', function ($q, $rootScope, $location, $window, $log) {
    return {
        'request': function (request) {
            return request;
        },
        'requestError': function (rejection) {
            doRedirectInternal('/signin', rejection.status);
            return $q.reject(rejection);
        },
        'response': function (response) {
            if (response.status == 200) {
                var authRequired = response.headers("AUTH-REQUIRED");
                if (angular.equals("true", authRequired)) {
                    doRedirectInternal("/signin", response.status);
                }
            }
            return response;
        },
        'responseError': function (rejection) {
            $log.debug("Received responseError status " + rejection.status);
            if (rejection.status == 0) {
                doRedirectExternal("http://www.google.com", rejection.status);
                return $q.reject(rejection);
            }
            else if (rejection.status == 401) {
                doRedirectInternal('/signin', rejection.status);
                return $q.reject(rejection);
            }
            else if (rejection.status == 403) {
                $log.debug("Handle unauthorized...");
                alert("Not authorized");
                return $q.reject(rejection);
            }
            else if (rejection.status == 302) {
                doRedirectInternal('/signin', rejection.status);
                return $q.reject(rejection);
            }
            else {
                return $q.reject(rejection);
            }
        }
    };

    function doRedirectExternal(target, status) {
        $log.debug("Redirected to " + target + " for status" + status);
        $window.location.href = target;
    };

    function doRedirectInternal(target, status) {
        target = GAME_URL + target;
        $log.debug("Redirecting to internal " + target + " for status " + status);
        $window.location.href = target;
    };
}]);

gameApp.config(['$httpProvider', function ($httpProvider) {
    $httpProvider.interceptors.push('portalHttpInterceptor');
}]);