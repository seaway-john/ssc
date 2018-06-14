var formatDateTime = function (d) {
    var date = new Date();
    date.setTime(d);
    var y = date.getFullYear();
    var m = date.getMonth() + 1;
    m = m < 10 ? ('0' + m) : m;
    var d = date.getDate();
    d = d < 10 ? ('0' + d) : d;
    var h = date.getHours();
    h = h < 10 ? ('0' + h) : h;
    var minute = date.getMinutes();
    minute = minute < 10 ? ('0' + minute) : minute;
    var second = date.getSeconds();
    second = second < 10 ? ('0' + second) : second;
    return y + '-' + m + '-' + d + ' ' + h + ':' + minute + ':' + second;
};

var add0 = function (d) {
    return d < 10 ? '0' + d : d
}

var getStrDateByTimestamp = function (timestamp) {
    var time = new Date(timestamp);
    var y = time.getFullYear();
    var m = time.getMonth() + 1;
    var d = time.getDate();
    var h = time.getHours();
    var mm = time.getMinutes();
    var s = time.getSeconds();
    return y + '-' + add0(m) + '-' + add0(d) + ' ' + add0(h) + ':' + add0(mm) + ':' + add0(s);
}

var toInt = function (data, val) {
    if (_.isArray(data) && _.isArray(val)) {
        return toIntArray(data, val);
    } else if (!_.isArray(data) && _.isArray(val)) {
        return toIntArray([data], val)[0];
    } else if (!_.isArray(data) && !_.isArray(val)) {
        return toIntArray([data], [val])[0];
    } else {
        return toIntArray(data, [val]);
    }
};

var toIntArray = function (data, val) {
    _.forEach(data, function (d) {
        _.forEach(val, function (v) {
            if (!isEmpty(d[v])) {
                d[v] = parseInt(d[v]);
            }
        })
    })
    return data;
};

var isEmpty = function (v) {
    return (v == undefined) || (v == null) || ((v + "").trim() == "");
}

var isEmptyObject = function (obj) {
    for (var name in obj) {
        return false;
    }
    return true;
}

var isArray = function (obj) {
    return Object.prototype.toString.call(obj) === '[object Array]';
}

var isRange = function (val, min, max) {
    return (!isNaN(val) && parseInt(val) >= min && parseInt(val) <= max);
}

var isRangeList = function (str, min, max) {
    if (isEmpty(str)) {
        return false;
    }

    var arr = str.split(",");
    for (var i = 0; i < arr.length; i++) {
        var arr1 = arr[i].split("-");
        if (arr1.length == 1) {
            if (!isRange(arr1[0], min, max)) {
                return false;
            }
        } else if (arr1.length == 2) {
            if (!isRange(arr1[0], min, max)) {
                return false;
            }
            if (!isRange(arr1[1], min, max)) {
                return false;
            }
            if (parseInt(arr1[0]) > parseInt(arr1[1])) {
                return false;
            }
        } else {
            return false;
        }
    }
    return true;
}

var isIpAddress = function (ipAddress) {
    if (isEmpty(ipAddress)) {
        return false;
    }

    var reg = /^((25[0-5]|2[0-4]\d|1\d{2}|[1-9]?\d)($|(?!\.$)\.)){4}$/;
    return reg.test(ipAddress);
}

var getRange = function (min, max) {
    var json = [];
    for (var i = min; i <= max; i++) {
        json.push(i);
    }
    return json;
}

var getJson = function ($scope, $log, $http, $timeout, url, successCallback,
    errorCallback) {
    $timeout(function () {
        $http({
            method: 'GET',
            url: url
        }).success(function (data, status, headers, config) {
            if (angular.isDefined(successCallback)) {
                successCallback(data, status);
            }
        }).error(function (data, status) {
            if (angular.isDefined(errorCallback)) {
                errorCallback(data, status);
            }
        })
    });
};

var setJson = function ($scope, $log, $http, $timeout, url, data,
    successCallback, errorCallback) {
    $timeout(function () {
        $http({
            method: 'POST',
            url: url,
            data: data
        }).success(function (data, status, headers, config) {
            if (angular.isDefined(successCallback)) {
                successCallback(data, status);
            }
        }).error(function (data, status) {
            if (angular.isDefined(errorCallback)) {
                errorCallback(data, status);
            }
        })
    });
};

var uploadFile = function ($scope, $log, $upload, $timeout, url, file,
    successCallback, errorCallback) {
    $timeout(function () {
        $upload.upload({
            method: 'POST',
            url: url,
            file: file
        }).progress(function (evt) {
            $log.info('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
        }).success(function (data, status, headers, config) {
            if (angular.isDefined(successCallback)) {
                successCallback(data, status);
            }
        }).error(function (data, status) {
            if (angular.isDefined(errorCallback)) {
                errorCallback(data, status);
            }
        })
    });
};

var getBooleanCell = function (field) {
    return getBooleanString(field, "true");
}

var getBooleanString = function (field, value) {
    return "<div style='padding: 5px;'>" +
        "        <span ng-switch on='row.entity." + field + "'>" +
        "        <i ng-switch-when=" + value + " class='zmdi zmdi-check' style='font-weight:bolder; color:green;'></i>" +
        "        <i ng-switch-default class='zmdi zmdi-block' style='font-weight:bolder; color:red;'></i>" +
        "        </span>" +
        "</div>";
}

var getUtilCell = function (field) {
    return "<div class='media'>" +
        "        <div>{{row.entity." + field + "}}</div>" +
        "        <div class='media-body'>" +
        "        <div class='progress'>" +
        "            <div class='progress-bar progress-bar-info' role='progressbar' aria-valuenow='{{row.entity." + field + "}}' aria-valuemin='0' aria-valuemax='100' style='width: {{row.entity." + field + "}}%'>" +
        "        </div>" +
        "        </div>" +
        "        </div>" +
        "</div>";
}

var getRatioCell = function (molecular, denominator) {
    return "<div class='media'>" +
        "        <div>{{row.entity." + molecular + "}} / {{row.entity." + denominator + "}}</div>" +
        "        <div class='media-body'>" +
        "        <div class='progress'>" +
        "            <div class='progress-bar progress-bar-info' role='progressbar' aria-valuenow='{{row.entity." + molecular + "}}' aria-valuemin='0' aria-valuemax='100' " +
        "               style='width: {{(row.entity." + molecular + "+row.entity." + denominator + ")==0?0:row.entity." + molecular + "*100/(row.entity." + molecular + "+row.entity." + denominator + ")}}%'></div>" +
        "            <div class='progress-bar progress-bar-success' role='progressbar' aria-valuenow='{{row.entity." + denominator + "}}' aria-valuemin='0' aria-valuemax='100' " +
        "               style='width: {{(row.entity." + molecular + "+row.entity." + denominator + ")==0?0:row.entity." + denominator + "*100/(row.entity." + molecular + "+row.entity." + denominator + ")}}%'></div>" +
        "        </div>" +
        "        </div>" +
        "</div>";
}

var getEventLevel = function (field) {
    return "<div style='padding: 5px;'>" +
        "    <div ng-switch on='row.entity." + field + "'>" +
        "        <label ng-switch-when='INFO' class='label label-info'>{{row.entity." + field + "}}</label>" +
        "        <label ng-switch-when='WARNING' class='label label-warning'>{{row.entity." + field + "}}</label>" +
        "        <label ng-switch-when='CRITICAL' class='label label-primary'>{{row.entity." + field + "}}</label>" +
        "        <label ng-switch-when='ALARM' class='label bgm-indigo'>{{row.entity." + field + "}}</label>" +
        "        <label ng-switch-when='ERROR' class='label label-danger'>{{row.entity." + field + "}}</label>" +
        "        <label ng-switch-default class='label label-default'>{{row.entity." + field + "}}</label>" +
        "    </div>" +
        "</div>";
}

var getStatus = function (field) {
    return "<div style='padding: 5px;'>" +
        "    <div ng-switch on='row.entity." + field + "'>" +
        "        <label ng-switch-when='AAA_AUTH' class='label bgm-amber'>{{row.entity." + field + "}}</label>" +
        "        <label ng-switch-when='AAA_AUTH_NO_URL' class='label bgm-amber'>{{row.entity." + field + "}}</label>" +
        "        <label ng-switch-when='DHCP_AUTH' class='label bgm-lime'>{{row.entity." + field + "}}</label>" +
        "        <label ng-switch-when='ONLINE' class='label bgm-green'>{{row.entity." + field + "}}</label>" +
        "        <label ng-switch-when='DISCONNECT' class='label bgm-gray'>{{row.entity." + field + "}}</label>" +
        "        <label ng-switch-default class='label label-default'>{{row.entity." + field + "}}</label>" +
        "    </div>" +
        "</div>";
}

var getActive = function (field) {
    return "<div style='padding: 5px;'>" +
        "    <div>" +
        "        <label ng-if='row.entity." + field + "==\"active\"' class='label bgm-green'>{{row.entity." + field + "}}</label>" +
        "        <label ng-if='row.entity." + field + "!=\"active\"' class='label bgm-orange'>{{row.entity." + field + "}}</label>" +
        "    </div>" +
        "</div>";
}

var getSexCell = function (field) {
    return "<div style='padding: 5px;'>" +
        "    <div>" +
        "        <label ng-if='row.entity." + field + "' class='label bgm-green'>男</label>" +
        "        <label ng-if='!row.entity." + field + "' class='label bgm-amber'>女</label>" +
        "    </div>" +
        "</div>";
}

var modalInformation = function ($scope, $http, $timeout, $log, $modalInstance, data) {
    $scope.modal = data;
    $scope.ok = function () {
        $modalInstance.dismiss('cancel');
    }
}

var getGridOptions = function ($scope, type) {
    var gridOptions = {
        data: 'data',
        columnDefs: [],
        enableGridMenu: true,
        enableSorting: true,
        useExternalSorting: false,
        showGridFooter: true,
        enableHorizontalScrollbar: 1,
        enableVerticalScrollbar: 1,
        enablePagination: true,
        enablePaginationControls: true,
        paginationPageSizes: [10, 15, 20],
        paginationCurrentPage: 1,
        paginationPageSize: 10,
        totalItems: 0,
        useExternalPagination: true,
        enableFooterTotalSelected: true,
        enableFullRowSelection: false,
        enableRowHeaderSelection: true,
        enableRowSelection: true,
        enableSelectAll: true,
        enableSelectionBatchEvent: true,
        enableFiltering: true,
        modifierKeysToMultiSelect: false,
        multiSelect: true,
        noUnselect: false,
        selectionRowHeaderWidth: 30,
        onRegisterApi: function (gridApi) {
            $scope.gridApi = gridApi;
            gridApi.selection.on.rowSelectionChanged($scope, function (row) {
                $scope.selectedData = gridApi.selection.getSelectedRows();
            });
            gridApi.selection.on.rowSelectionChangedBatch($scope, function (rows) {
                $scope.selectedData = gridApi.selection.getSelectedRows();
            });
        }
    };

    switch (type) {
        case UI_GRID_ONLY_SHOW:
            gridOptions.enableRowSelection = false;
            //gridOptions.enableFiltering = false;
            gridOptions.enableRowHeaderSelection = false;
            gridOptions.onRegisterApi = undefined;
            break;
        case UI_GRID_RADIO_SELECT:
            gridOptions.multiSelect = false;
            gridOptions.enableRowHeaderSelection = false;
            gridOptions.enableFullRowSelection = true;
            break;
        case UI_GRID_MULTI_SELECT:
            gridOptions.multiSelect = true;
            break;
    }

    return gridOptions;
}

var groupByIP = function (d) {
    var ip = [];

    if (!isEmpty(d)) {
        ip.push([d[0]]);
        for (var i = 1; i < d.length; i++) {
            var flag = true;
            for (var j = 0; j < ip.length && flag; j++) {
                if (d[i].ipAddress == ip[j][0].ipAddress) {
                    ip[j].push(d[i]);
                    flag = false;
                }
            }
            if (flag) {
                ip.push([d[i]])
            }
        }
        for (var j = 0; j < ip.length; j++) {
            ip[j].sort(function (a, b) { return a.scanTimestamp - b.scanTimestamp; });
        }
    }

    return ip;
}

var groupMac = function (o) {
    var arr = [];
    if (isEmptyObject(o)) {
        return arr;
    }

    for (var i in o) {
        arr.push({ mac: i, data: o[i] });
    }
    return arr;
};

var fillData = function (startTs, data) {
    for (var i = 0; i < data.length; i++) {
        var ip = data[i];
        if (ip.length == 1) {
            var ip1 = angular.copy(ip[0]);
            ip1.scanTimestamp = startTs;
            ip.unshift(ip1);
        } else {
            if (ip[0].status != ip[1].status) {
                var ip1 = angular.copy(ip[0]);
                ip1.scanTimestamp = startTs;
                ip.unshift(ip1);
            }
        }
    }
    return data;
}

var interpolation = function (data) {
    if (data.length < 2) {
        return data;
    }
    var preTs = data[0][0];
    var re = [];
    for (var i = 1; i < data.length; i++) {
        if ((data[i][0] - preTs) > 120000) {
            re.push([preTs, 0]);
            re.push([data[i][0], 0]);
        }
        preTs = data[i][0];
        re.push(data[i]);
    }
    return re;
}

var alertResult = function (result, title, tip, time) {
    if (result) {
        swal({
            title: title,
            text: tip,
            type: "success",
            timer: time,
            showConfirmButton: false
        }).then(function (json_data) { }, function (dismiss) { });
    } else {
        swal({
            title: title,
            text: tip,
            type: "error",
            timer: time,
            showConfirmButton: false
        }).then(function (json_data) { }, function (dismiss) { });
    }
}

var isAdminRole = function () {
    var adminRole = localStorage.getItem('isAdminRole');

    if (isEmpty(adminRole)) {
        adminRole = false;
        var role = localStorage.getItem('currentRole');
        if (_.includes(role, "ADMIN")) {
            adminRole = true;
        }
        localStorage.setItem('isAdminRole', adminRole);
    }

    return adminRole;
}

var validateMac = function (mac) {
    var expre = /([a-fA-F0-9]{2}:){5}[a-fA-F0-9]{2}/;
    var regexp = new RegExp(expre);
    if (regexp.test(mac)) {
        return true;
    } else {
        return false;
    }
}

var getPageUrl = function (url, pageable) {
    var size = 10;
    var page = 0;
    var sort = "";
    if (!isEmpty(pageable)) {
        size = pageable.size;
        page = pageable.number - 1;

        if(!isEmpty(pageable.sort)) {
            _.forEach(pageable.sort, function (data) {
                sort += "&sort=" + data.property + "," + data.direction;
            });
        }
    }

    url += "/page?size=" + size + "&page=" + page;
    if (!isEmpty(sort)) {
        url += sort;
    }

    return url;
}
