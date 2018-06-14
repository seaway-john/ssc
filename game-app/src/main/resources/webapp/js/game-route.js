gameApp
    .config(function ($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.otherwise("/config/setting");

        $stateProvider
            // Dashboard
            // .state('dashboard', {
            //     url: '/dashboard',
            //     templateUrl: 'templates/dashboard.html',
            //     controller: 'dashboardController'
            // })
            // Administration
            .state('admin', {
                url: '/admin',
                templateUrl: 'templates/common.html'
            })
            .state('admin.users', {
                url: '/users',
                templateUrl: 'templates/admin/users.html',
                controller: 'adminUsersController'
            })
            .state('admin.setting', {
                url: '/setting',
                templateUrl: 'templates/admin/user-setting.html',
                controller: 'adminUserSettingController'
            })
            // .state('admin.upgrade', {
            //     url: '/upgrade',
            //     templateUrl: 'templates/admin/upgrade.html',
            //     controller: 'adminUpgradeController'
            // })
            .state('admin.system', {
                url: '/system',
                templateUrl: 'templates/admin/system.html',
                controller: 'adminSystemController'
            })
            .state('admin.database', {
                url: '/database',
                templateUrl: 'templates/admin/database.html',
                controller: 'adminDatabaseController'
            })

            // Configuration
            .state('config', {
                url: '/config',
                templateUrl: 'templates/common.html'
            })
            .state('config.setting', {
                url: '/setting',
                templateUrl: 'templates/config/setting.html',
                controller: 'configSettingController'
            })
            .state('config.network', {
                url: '/network',
                templateUrl: 'templates/config/network.html',
                controller: 'configNetworkController'
            })
            .state('config.qrcode', {
                url: '/qrcode',
                templateUrl: 'templates/config/qrcode.html',
                controller: 'configQrCodeController'
            })
            .state('config.lottery-win', {
                url: '/lottery-win',
                templateUrl: 'templates/config/lottery-win.html',
                controller: 'configGameLotteryWinController'
            })
            .state('config.agents', {
                url: '/agents',
                templateUrl: 'templates/config/agents.html',
                controller: 'configAgentsController'
            })

            // Log
            .state('statistics', {
                url: '/statistics',
                templateUrl: 'templates/common.html'
            })
            .state('statistics.agent-journal-account', {
                url: '/agent-journal-account',
                templateUrl: 'templates/statistics/agent-journal-account.html',
                controller: 'statisticsAgentJournalAccountController'
            })
            .state('statistics.game-login-report', {
                url: '/game-login-report',
                templateUrl: 'templates/statistics/game-login-report.html',
                controller: 'statisticsGameLoginReportController'
            })

    });
