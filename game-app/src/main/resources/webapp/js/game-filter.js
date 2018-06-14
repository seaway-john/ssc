gameApp
    .filter("deviceStatusToLabel", function () {
        return function (input) {
            var label = "label-danger";
            if (!angular.isDefined(input)) {
                return label;
            }

            switch (input) {
                case "ONLINE":
                    label = "label-success";
                    break;
                case "OFFLINE":
                    label = "label-default";
                    break;
                case "STANDBY":
                    label = "label-warning";
                    break;
                default:
                    label = "label-danger";
                    break;
            }

            return label;
        };
    })
    .filter("moduleStatusToLabel", function () {
        return function (input) {
            var label = "label-danger";
            if (!angular.isDefined(input)) {
                return label;
            }

            input = input.toLowerCase();
            switch (input) {
                case "running":
                case "active":
                    label = "label-success";
                    break;
                case "standby":
                    label = "label-warning";
                    break;
                default:
                    label = "label-danger";
                    break;
            }

            return label;
        };
    })
    .filter("apStatusToLabel", function () {
        return function (input) {
            var label = "label-danger";
            if (!angular.isDefined(input)) {
                return label;
            }

            switch (input) {
                case "INITIAL":
                    label = "label-danger";
                    break;
                case "ACTIVE":
                    label = "label-success";
                    break;
                case "IDLE":
                    label = "label-warning";
                    break;
                case "DISCONNECT":
                    label = "label-default";
                    break;
                default:
                    label = "label-danger";
                    break;
            }

            return label;
        };
    })
    .filter("dataToFix", function () {
        return function (input) {
            if (!angular.isDefined(input) || !angular.isNumber(input)) {
                input = 0;
            }

            var value = input;
            var unit = "";

            if (input >= 1024 * 1024 * 1024) {
                value = (input / (1024 * 1024 * 1024)).toFixed(2);
                unit = " G";
            } else if (input >= 1024 * 1024) {
                value = (input / (1024 * 1024)).toFixed(2);
                unit = " M";
            } else if (input >= 1024) {
                value = (input / 1024).toFixed(2);
                unit = " K";
            }

            return value + unit;
        };
    })
    .filter("secondsToTime", function () {
        return function (input) {
            if (!angular.isDefined(input)) {
                return input;
            }
            if (!angular.isNumber(input)) {
                return input;
            }

            if (input == 0) {
                return input;
            }
            
            var day = Math.floor(input / (24 * 60 * 60));
            if (day > 0) {
            	input = input % (24 * 60 * 60);
            }
            var hour = Math.floor(input / (60 * 60));
            if (hour > 0) {
            	input = input % (60 * 60);
            }
            var minute = Math.floor(input / 60);
            if (minute > 0) {
            	input = input % 60;
            }
            var second = input;

            var result = "";
            if (day != 0) {
            	result += day + "d ";
            }
            if (!isEmpty(result) || hour > 0) {
            	result += hour + "h ";
            }
            if (!isEmpty(result) || minute > 0) {
            	result += minute + "m ";
            }
            result += second + "s";

            return result;
        };
    });
