var getLineChart = function () {
    var options = {
        chart: {
            type: 'lineChart',
            height: 350,
            margin: {
                top: 20,
                right: 45,
                bottom: 30,
                left: 50
            },
            x: function (d) { return d[0]; },
            y: function (d) { return d[1]; },
            useInteractiveGuideline: true,
            clipVoronoi: false,
            duration: 0,
            xAxis: {
                tickFormat: function (d) {
                    return d3.time.format('%m/%d %H:%M:%S')(new Date(d));
                },
                staggerLabels: true
            },
            yAxis: {
                tickFormat: function (d) {
                    return d3.format('d')(d);
                },
                axisLabelDistance: -10
            },
            tooltip: {
                valueFormatter: function (d, i) {
                    return d;
                }
            }
        }
    };
    return options;
}

var getMultiBarChart = function () {
    var options = {
        chart: {
            type: 'multiBarChart',
            height: 450,
            margin: {
                top: 20,
                right: 20,
                bottom: 50,
                left: 45
            },
            x: function (d) { return d[0]; },
            y: function (d) { return d[1]; },
            clipEdge: true,
            duration: 500,
            stacked: false,
            xAxis: {
                showMaxMin: false
            },
            yAxis: {
                tickFormat: function (d) {
                    return d3.format('d')(d);
                }
            }
        }
    }
    return options;
}

var getDiscreteBarChart = function () {
    var options = {
        chart: {
            type: 'discreteBarChart',
            height: 350,
            margin: {
                top: 20,
                right: 20,
                bottom: 50,
                left: 55
            },
            showValues: true,
            valueFormat: function (d) {
                return d3.format('d')(d);
            },
            duration: 500,
            xAxis: {},
            yAxis: {
                tickFormat: function (d) {
                    return d3.format('d')(d);
                }
            }
        }
    };
    return options;
}

var getPiechart = function () {
    var options = {
        chart: {
            type: 'pieChart',
            height: 450,
            donut: false,
            x: function (d) { return d.x; },
            y: function (d) { return d.y; },
            showLabels: true,
            pie: {
                startAngle: function (d) { return d.startAngle },
                endAngle: function (d) { return d.endAngle }
            },
            tooltip: {
                valueFormatter: function (d, i) {
                    return d;
                }
            },
            duration: 500,
            labelThreshold: 0.05,
            labelSunbeamLayout: false,
            legend: {
                margin: {
                    top: 5,
                    right: 70,
                    bottom: 5,
                    left: 0
                }
            }
        }
    };
    return options;
}

var nvd3FormatFromBit = function (d) {
    if (d < 1024)
        return d + "b";
    else if (d < 10240)
        return (d / 1024).toFixed(1) + "K";
    else if (d < 1048576)
        return (d / 1024).toFixed(0) + "K";
    else if (d < 10485760)
        return (d / 1048576).toFixed(1) + "M";
    else if (d < 1073741824)
        return (d / 1048576).toFixed(0) + "M";
    else if (d < 10737418240)
        return (d / 1073741824).toFixed(1) + "G";
    else
        return (d / 1073741824).toFixed(0) + "G";
};

var nvd3FormatToBit = function (data) {
    var res = 0;
    var regx = /\d*[gG]|\d*\.\d*[gG]/;
    var s = regx.exec(data);
    if (s != null && s[0].length) {
        res += s[0].substring(0, s[0].length - 1) * 1073741824;
    }
    regx = /\d*[mM]|\d*\.\d*[mM]/;
    s = regx.exec(data);
    if (s != null && s[0].length) {
        res += s[0].substring(0, s[0].length - 1) * 1048576;
    }
    regx = /\d*[kK]|\d*\.\d*[kK]/;
    s = regx.exec(data);
    if (s != null && s[0].length) {
        res += s[0].substring(0, s[0].length - 1) * 1024;
    }
    regx = /\d*[bB]|\d*\.\d*[bB]/;
    s = regx.exec(data);
    if (s != null && s[0].length) {
        res += s[0].substring(0, s[0].length - 1) * 1;
    }
    return res;
}

var nvd3FormatTime = function (d) {
    var time = "";
    var day = Math.floor(d / 86400000);
    if (day > 0) {
        time += day + "d ";
        d = d % 86400000;
    }
    var h = Math.floor(d / 3600000);
    if (h > 0) {
        time += h + "h ";
        d = d % 3600000;
    }
    var m = Math.floor(d / 60000);
    if (m > 0) {
        time += m + "m ";
        d = d % 60000;
    }
    var s = Math.floor(d / 1000);
    if (s > 0) {
        time += s + "s";
    }
    return time;
}

var nvd3Domain = function (data) {
    var max = _.max(data, function (d) { return d[1]; });
    return max[1];
}

var nvd3Dynamic = function (res, length, data) {
    if (isEmpty(data)) {
        data = 0;
    }
    if (res.length < length) {
        res = [];
        for (var i = 0; i < length - 1; i++) {
            res.push([i, 0]);
        }
        res.push([i, data]);
    } else {
        res.shift();
        _.forEach(res, function (r) {
            r[0] = r[0] - 1;
        })
        res.push([length - 1, data]);
    }
    return res;
}

var Gauge = function (placeholderName, configuration) {

    this.placeholderName = placeholderName;

    var self = this; // for internal d3 functions

    this.configure = function (configuration) {
        this.config = configuration;

        this.config.size = this.config.size * 0.9;

        this.config.raduis = this.config.size * 0.97 / 2;
        this.config.cx = this.config.size / 2;
        this.config.cy = this.config.size / 2;

        this.config.min = undefined != configuration.min ? configuration.min : 0;
        this.config.max = undefined != configuration.max ? configuration.max : 100;
        this.config.range = this.config.max - this.config.min;

        this.config.majorTicks = configuration.majorTicks || 5;
        this.config.minorTicks = configuration.minorTicks || 2;

        this.config.greenColor = configuration.greenColor || "#109618";
        this.config.yellowColor = configuration.yellowColor || "#FF9900";
        this.config.redColor = configuration.redColor || "#DC3912";

        this.config.transitionDuration = configuration.transitionDuration || 500;
    }

    this.render = function () {
        this.body = d3.select("." + this.placeholderName)
            .append("svg:svg")
            .attr("class", "gauge")
            .attr("width", this.config.size)
            .attr("height", this.config.size);


        this.body.append("svg:circle")
            .attr("cx", this.config.cx)
            .attr("cy", this.config.cy)
            .attr("r", this.config.raduis)
            .style("fill", "#ccc")
            .style("stroke", "#000")
            .style("stroke-width", "0.5px");

        this.body.append("svg:circle")
            .attr("cx", this.config.cx)
            .attr("cy", this.config.cy)
            .attr("r", 0.9 * this.config.raduis)
            .style("fill", "#fff")
            .style("stroke", "#e0e0e0")
            .style("stroke-width", "2px");

        for (var index in this.config.greenZones) {
            this.drawBand(this.config.greenZones[index].from, this.config.greenZones[index].to, self.config.greenColor);
        }

        for (var index in this.config.yellowZones) {
            this.drawBand(this.config.yellowZones[index].from, this.config.yellowZones[index].to, self.config.yellowColor);
        }

        for (var index in this.config.redZones) {
            this.drawBand(this.config.redZones[index].from, this.config.redZones[index].to, self.config.redColor);
        }

        if (undefined != this.config.label) {
            var fontSize = Math.round(this.config.size / 9);
            this.body.append("svg:text")
                .attr("x", this.config.cx)
                .attr("y", this.config.cy / 2 + fontSize / 2)
                .attr("dy", fontSize / 2)
                .attr("text-anchor", "middle")
                .text(this.config.label)
                .style("font-size", fontSize + "px")
                .style("fill", "#333")
                .style("stroke-width", "0px");
        }

        var fontSize = Math.round(this.config.size / 16);
        var majorDelta = this.config.range / (this.config.majorTicks - 1);
        for (var major = this.config.min; major <= this.config.max; major += majorDelta) {
            var minorDelta = majorDelta / this.config.minorTicks;
            for (var minor = major + minorDelta; minor < Math.min(major + majorDelta, this.config.max); minor += minorDelta) {
                var point1 = this.valueToPoint(minor, 0.75);
                var point2 = this.valueToPoint(minor, 0.85);

                this.body.append("svg:line")
                    .attr("x1", point1.x)
                    .attr("y1", point1.y)
                    .attr("x2", point2.x)
                    .attr("y2", point2.y)
                    .style("stroke", "#666")
                    .style("stroke-width", "1px");
            }

            var point1 = this.valueToPoint(major, 0.7);
            var point2 = this.valueToPoint(major, 0.85);

            this.body.append("svg:line")
                .attr("x1", point1.x)
                .attr("y1", point1.y)
                .attr("x2", point2.x)
                .attr("y2", point2.y)
                .style("stroke", "#333")
                .style("stroke-width", "2px");

            if (major == this.config.min || major == this.config.max) {
                var point = this.valueToPoint(major, 0.63);

                this.body.append("svg:text")
                    .attr("x", point.x)
                    .attr("y", point.y)
                    .attr("dy", fontSize / 3)
                    .attr("text-anchor", major == this.config.min ? "start" : "end")
                    .text(major)
                    .style("font-size", fontSize + "px")
                    .style("fill", "#333")
                    .style("stroke-width", "0px");
            }
        }

        var pointerContainer = this.body.append("svg:g").attr("class", "pointerContainer");

        var midValue = (this.config.min + this.config.max) / 2;

        var pointerPath = this.buildPointerPath(midValue);

        var pointerLine = d3.svg.line()
            .x(function (d) { return d.x })
            .y(function (d) { return d.y })
            .interpolate("basis");

        pointerContainer.selectAll("path")
            .data([pointerPath])
            .enter()
            .append("svg:path")
            .attr("d", pointerLine)
            .style("fill", "#dc3912")
            .style("stroke", "#c63310")
            .style("fill-opacity", 0.7)

        pointerContainer.append("svg:circle")
            .attr("cx", this.config.cx)
            .attr("cy", this.config.cy)
            .attr("r", 0.12 * this.config.raduis)
            .style("fill", "#4684EE")
            .style("stroke", "#666")
            .style("opacity", 1);

        var fontSize = Math.round(this.config.size / 10);
        pointerContainer.selectAll("text")
            .data([midValue])
            .enter()
            .append("svg:text")
            .attr("x", this.config.cx)
            .attr("y", this.config.size - this.config.cy / 4 - fontSize)
            .attr("dy", fontSize / 2)
            .attr("text-anchor", "middle")
            .style("font-size", fontSize + "px")
            .style("fill", "#000")
            .style("stroke-width", "0px");

        this.redraw(this.config.min, 0);
    }

    this.buildPointerPath = function (value) {
        var delta = this.config.range / 13;

        var head = valueToPoint(value, 0.85);
        var head1 = valueToPoint(value - delta, 0.12);
        var head2 = valueToPoint(value + delta, 0.12);

        var tailValue = value - (this.config.range * (1 / (270 / 360)) / 2);
        var tail = valueToPoint(tailValue, 0.28);
        var tail1 = valueToPoint(tailValue - delta, 0.12);
        var tail2 = valueToPoint(tailValue + delta, 0.12);

        return [head, head1, tail2, tail, tail1, head2, head];

        function valueToPoint(value, factor) {
            var point = self.valueToPoint(value, factor);
            point.x -= self.config.cx;
            point.y -= self.config.cy;
            return point;
        }
    }

    this.drawBand = function (start, end, color) {
        if (0 >= end - start) return;

        this.body.append("svg:path")
            .style("fill", color)
            .attr("d", d3.svg.arc()
                .startAngle(this.valueToRadians(start))
                .endAngle(this.valueToRadians(end))
                .innerRadius(0.65 * this.config.raduis)
                .outerRadius(0.85 * this.config.raduis))
            .attr("transform", function () { return "translate(" + self.config.cx + ", " + self.config.cy + ") rotate(270)" });
    }

    this.redraw = function (value, transitionDuration) {
        var pointerContainer = this.body.select(".pointerContainer");

        pointerContainer.selectAll("text").text(Math.round(value) + "%");

        var pointer = pointerContainer.selectAll("path");
        pointer.transition()
            .duration(undefined != transitionDuration ? transitionDuration : this.config.transitionDuration)
            .attrTween("transform", function () {
                var pointerValue = value;
                if (value > self.config.max) pointerValue = self.config.max + 0.02 * self.config.range;
                else if (value < self.config.min) pointerValue = self.config.min - 0.02 * self.config.range;
                var targetRotation = (self.valueToDegrees(pointerValue) - 90);
                var currentRotation = self._currentRotation || targetRotation;
                self._currentRotation = targetRotation;

                return function (step) {
                    var rotation = currentRotation + (targetRotation - currentRotation) * step;
                    return "translate(" + self.config.cx + ", " + self.config.cy + ") rotate(" + rotation + ")";
                }
            });
    }

    this.valueToDegrees = function (value) {
        return value / this.config.range * 270 - (this.config.min / this.config.range * 270 + 45);
    }

    this.valueToRadians = function (value) {
        return this.valueToDegrees(value) * Math.PI / 180;
    }

    this.valueToPoint = function (value, factor) {
        return {
            x: this.config.cx - this.config.raduis * factor * Math.cos(this.valueToRadians(value)),
            y: this.config.cy - this.config.raduis * factor * Math.sin(this.valueToRadians(value))
        };
    }

    // initialization
    this.configure(configuration);
}
