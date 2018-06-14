gameApp.controller('statisticsGameLoginReportController', function ($scope, $http, $timeout, $log, $modal) {
    $scope.data = [];
    $scope.pageItems = PAGINATION_ITEMS;

    var getRoleCell = function (field) {
        return "<div style='padding: 5px;'>" +
            "    <div>" +
            "        <label ng-if='row.entity." + field + "' class='label bgm-green'>代理</label>" +
            "        <label ng-if='!row.entity." + field + "' class='label bgm-amber'>用户</label>" +
            "    </div>" +
            "</div>";
    }

    $scope.gridOptions = getGridOptions($scope, UI_GRID_ONLY_SHOW);
    $scope.gridOptions.columnDefs = [
        {
            field: 'wxNickName',
            displayName: '微信昵称'
        },
        {
            field: 'agent',
            displayName: '角色',
            cellTemplate: getRoleCell("agent")
        },
        {
            field: 'roomName',
            displayName: '登录房间'
        },
        {
            field: 'created',
            displayName: '登录时间',
            cellFilter: DATE_FORMAT
        }
    ];

    $scope.pageable = {
        "size": 10,
        "number": 1,
        "sort": [
            {
                "direction": "DESC",
                "property": "id"
            }
        ]
    };

    $scope.setItemsPerPage = function (item) {
        $scope.pageable.size = item;
        $scope.refresh();
    }

    $scope.refresh = function () {
        var url = getPageUrl(GAME_URL_STATISTICS_GAME_LOGIN_REPORT, $scope.pageable);

        getJson($scope, $log, $http, $timeout, url,
            function (data, status) {
                data.number += 1;
                $scope.pageable = data;
                $scope.data = ($scope.pageable.content === null ? [] : $scope.pageable.content);
            },
            function (data, status) {
            }
        );
    };

    $scope.refresh();
});