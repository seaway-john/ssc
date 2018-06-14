gameApp.controller('statisticsAgentJournalAccountController', function ($scope, $http, $timeout, $log, $modal) {
    $scope.data = [];

    $scope.gridOptions = getGridOptions($scope, UI_GRID_ONLY_SHOW);
    $scope.gridOptions.columnDefs = [
        {
            field: 'wxNickName',
            displayName: '微信昵称'
        },
        {
            field: 'wxSex',
            displayName: '性别',
            cellTemplate: getSexCell("wxSex")
        },
        {
            field: 'description',
            displayName: '备注'
        },
        {
            field: 'balance',
            displayName: '当前余粮'
        },
        {
            field: 'betInvest',
            displayName: '押粮流水'
        },
        {
            field: 'betIncome',
            displayName: '中粮流水'
        },
        {
            field: 'rebateRate',
            displayName: '回水返点率（%）'
        },
        {
            field: 'betRebate',
            displayName: '押粮回水'
        },
        {
            field: 'created',
            displayName: '时间',
            cellFilter: DATE_FORMAT
        }
    ];

    $scope.refresh = function () {
        var url = GAME_URL_STATISTICS_AGENT_JOURNAL_ACCOUNT + "/table/refresh";
        url += "?dayStart=" + $scope.dayStart;
        url += "&dayEnd=" + $scope.dayEnd;

        getJson($scope, $log, $http, $timeout, url,
            function (data, status) {
                $scope.data = data;
            },
            function (data, status) {
            }
        );
    }

    var week = new Date().getDay();
    if (week == 0) {
        week = 7;
    }

    $scope.menus = [];
    $scope.menus.push({"dayStart": 0, "dayEnd": 0, "text": "今天"});
    $scope.menus.push({"dayStart": 1, "dayEnd": 1, "text": "昨天"});
    $scope.menus.push({"dayStart": 2, "dayEnd": 2, "text": "前天"});
    $scope.menus.push({"dayStart": (week - 1), "dayEnd": 0, "text": "本周"});
    $scope.menus.push({"dayStart": (week + 6), "dayEnd": week, "text": "上周"});
    $scope.menus.push({"dayStart": -1, "dayEnd": -1, "text": "全部"});


    $scope.changeDayRange = function(menu) {
        $scope.dayStart = menu.dayStart;
        $scope.dayEnd = menu.dayEnd;
        $scope.selectedText = menu.text;

        $scope.refresh();
    }

    $scope.changeDayRange($scope.menus[0]);
});
