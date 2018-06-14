gameApp
    .directive('toggleSubmenu', function () {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                element.click(function () {
                    element.parent().toggleClass('toggled');
                    element.parent().find('ul').stop(true, false).slideToggle(200);
                })
            }
        }
    })

    .directive('changeLayout', function () {
        return {
            restrict: 'A',
            scope: {
                changeLayout: '='
            },

            link: function (scope, element, attr) {

                //Default State
                scope.changeLayout = '1';
                element.prop('checked', true);

                //Change State
                element.on('change', function () {
                    if (element.is(':checked')) {
                        localStorage.setItem('ma-layout-status', 1);
                        scope.$apply(function () {
                            scope.changeLayout = '1';
                        })
                    }
                    else {
                        localStorage.setItem('ma-layout-status', 0);
                        scope.$apply(function () {
                            scope.changeLayout = '0';
                        })
                    }
                })
            }
        }
    })

    .directive('notDefault', function ($http, $timeout) {
        return {
            require: 'ngModel',
            link: function (scope, ele, attrs, c) {
                scope.$watch(attrs.ngModel, function (n) {
                    if (!n) return;
                    if (n === "default") {
                        c.$setValidity('unique', false);
                    } else {
                        c.$setValidity('unique', true);
                    }
                });
            }
        };
    })

    .directive('ensureUnique', function ($http, $timeout) {
        return {
            require: 'ngModel',
            link: function (scope, ele, attrs, c) {
                scope.$watch(attrs.ngModel, function (n) {
                    if (!n || (n.length < attrs.ngMinlength || n.length > attrs.ngMaxlength)
                        || "edit" == scope.modal.action) return;
                    $timeout(function () {
                        $http({
                            method: 'GET',
                            url: scope.modal.url + "/unique?name=" + n
                        }).success(function (data) {
                            c.$setValidity('unique', data.isUnique);
                        }).error(function (data) {
                            c.$setValidity('unique', false);
                        });
                    }, 300);
                });
            }
        };
    })

    .directive('groupUnique', function ($http, $timeout, $log) {
        return {
            require: 'ngModel',
            link: function (scope, ele, attrs, c) {
                var value_1, value_2 = 0;
                scope.$watch(attrs.ngModel, function (n) {
                    if (!n || (n.length < attrs.ngMinlength || n.length > attrs.ngMaxlength)
                        || "edit" == scope.modal.action) return;
                    value_1 = n;
                    $timeout(fun, 300);
                });
                scope.$watch(attrs.bind, function (n) {
                    value_2 = n;
                    $timeout(fun, 300);
                });
                var fun = function () {
                    if (!isEmpty(value_1)) {
                        if (isEmpty(value_2)) {
                            value_2 = 0;
                        }
                        var data = attrs.ngModel.replace("modal.data.", "") + "=" + value_1 + "&" + attrs.bind.replace("modal.data.", "") + "=" + value_2;
                        $http({
                            method: 'GET',
                            url: scope.modal.url + "/unique?" + data
                        }).success(function (data) {
                            c.$setValidity('unique', data.isUnique);
                        }).error(function (data) {
                            c.$setValidity('unique', false);
                        })
                    }
                }
            }
        };
    })

    // =========================================================================
    // MAINMENU COLLAPSE
    // =========================================================================

    .directive('toggleSidebar', function () {
        return {
            restrict: 'A',
            scope: {
                modelLeft: '=',
                modelRight: '='
            },

            link: function (scope, element, attr) {
                element.on('click', function () {

                    if (element.data('target') === 'mainmenu') {
                        if (scope.modelLeft === false) {
                            scope.$apply(function () {
                                scope.modelLeft = true;
                            })
                        }
                        else {
                            scope.$apply(function () {
                                scope.modelLeft = false;
                            })
                        }
                    }

                    if (element.data('target') === 'chat') {
                        if (scope.modelRight === false) {
                            scope.$apply(function () {
                                scope.modelRight = true;
                            })
                        }
                        else {
                            scope.$apply(function () {
                                scope.modelRight = false;
                            })
                        }

                    }
                })
            }
        }
    })

    // =========================================================================
    // INPUT MASK
    // =========================================================================

    .directive('inputMask', function () {
        return {
            restrict: 'A',
            scope: {
                inputMask: '='
            },
            link: function (scope, element) {
                element.mask(scope.inputMask.mask);
            }
        }
    })
