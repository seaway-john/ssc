gameApp.controller('configGameLotteryWinController', function ($scope, $http, $timeout, $log, $modal, growlCenterService) {
    $scope.data = [];
    $scope.isAdmin = isAdminRole();
    $scope.pageItems = PAGINATION_ITEMS;

    var getStatusCell = function (field) {
        return "<div style='padding: 5px;'>" +
            "    <div ng-switch on='row.entity." + field + "'>" +
            "        <label ng-switch-when='NEW' class='label label-info'>下注中</label>" +
            "        <label ng-switch-when='DEADLINE' class='label label-warning'>开奖中</label>" +
            "        <label ng-switch-when='PUBLISH' class='label label-success'>已开奖</label>" +
            "        <label ng-switch-default class='label label-default'>{{row.entity." + field + "}}</label>" +
            "    </div>" +
            "</div>";
    }

    $scope.gridOptions = getGridOptions($scope, UI_GRID_MULTI_SELECT);
    $scope.gridOptions.columnDefs = [
        {
            field: 'sequence',
            displayName: '期数'
        },
        {
            field: 'awardNumber',
            displayName: '开奖号码'
        },
        {
            field: 'status',
            displayName: '状态',
            cellTemplate: getStatusCell("status")
        },
        {
            field: 'created',
            displayName: '创建时间',
            cellFilter: DATE_FORMAT
        },
        {
            field: 'publishDate',
            displayName: '开奖时间',
            cellFilter: DATE_FORMAT
        }
    ];

    $scope.pageable = {
        "size": 10,
        "number": 1,
        "sort": [
            {
                "direction": "DESC",
                "property": "sequence"
            }
        ]
    };

    $scope.setItemsPerPage = function (item) {
        $scope.pageable.size = item;
        $scope.refresh();
    }

    $scope.refresh = function () {
        var url = getPageUrl(GAME_URL_CONFIG_LOTTERY_WIN, $scope.pageable);

        getJson($scope, $log, $http, $timeout, url,
            function (data, status) {
                data.number += 1;
                $scope.pageable = data;
                $scope.data = ($scope.pageable.content === null ? [] : $scope.pageable.content);
                $scope.gridApi.selection.clearSelectedRows();
            },
            function (data, status) {
            }
        );
    };

    $scope.edit = function () {
        if ($scope.selectedData[0].status != "DEADLINE") {
            growlCenterService.growl("只能手动开开奖中的期数", 'warning');
            return;
        }

        $scope.set = {
            "title": "手动开奖",
            "action": "publish",
            "url": GAME_URL_CONFIG_LOTTERY_WIN,
            "data": angular.copy($scope.selectedData[0])
        };
        publish();
    }

    var publish = function () {
        $modal.open({
            templateUrl: 'templates/common/modal/config/lottery-win.html',
            controller: modalSetJson,
            resolve: {
                data: function () {
                    return $scope.set;
                }
            }
        }).result.then(function (data) {
            $scope.refresh();
        }, function () {
            $scope.refresh();
        });
    };

    $scope.refresh();
});