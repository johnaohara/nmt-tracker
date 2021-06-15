
var pmapTab = document.getElementById('contact-tab')
var logsTab = document.getElementById('profile-tab')
var tracking = document.getElementById('enableTracking');
var ansi_up = new AnsiUp;


pmapTab.addEventListener('show.bs.tab', function (event) {
    refreshPmap();
})

tracking.addEventListener('show.bs.tab', function (event) {
    getLogs();
})

var refreshPmap = function(){
    var pmapText = document.getElementById("pmap");
    pmapText.value = '';

    fetch('stats/pmap')
        .then(response => response.json())
        .then(arr => {

            arr.forEach(element => {
                pmapText.value +=  element + '\r\n';

            });
        })
        .catch(err => console.error(err));

}
var toggleTracking = function(){
    // alert(checkBox.checked);
    startStopNmt(tracking.checked ? 'start' : 'stop')
}

var refreshRuntimes = function(){
    var runtimes = document.getElementById("runtimes-list");
    runtimes.innerHTML = '';

    fetch('stats/processes')
        .then(response => response.json())
        .then(arr => {

            arr.forEach(runtime => {
                runtimes.innerHTML += '<li>' +
                    '<button class="btn align-items-center rounded" onclick="changeRuntime(\'' + runtime + '\')">' +
                    runtime +
                    '</button>' +
                    '</li>';
            });
        })
        .catch(err => console.error(err));

}

var changeRuntime = function(runtime){
    const Http = new XMLHttpRequest();
    const url='/control/processExpr/'+runtime;
    Http.open("GET", url);
    Http.send();

    Http.onreadystatechange = (e) => {
        console.log(Http.responseText)
    }
    document.getElementById("currentRuntime").innerText=runtime;
}

var startStopNmt = function(startStop){
    const Http = new XMLHttpRequest();
    const url='/control/nmt/'+startStop;
    Http.open("GET", url);
    Http.send();

    Http.onreadystatechange = (e) => {
        console.log(Http.responseText)
    }
}

var getLogs = function(){
    var logsText = document.getElementById("logs");
    logsText.innerHTML = '';

    fetch('control/logs')
        .then(response => response.json())
        .then(arr => {

            arr.forEach(element => {
                var html = ansi_up.ansi_to_html(element) + "</br>";
                logsText.innerHTML += html;
            });
        })
        .catch(err => console.error(err));

}



var svg = d3.select("#chart")
    .append("svg")
    .append("g")

svg.append("g")
    .attr("class", "slices");
svg.append("g")
    .attr("class", "labels");
svg.append("g")
    .attr("class", "total");
svg.append("g")
    .attr("class", "lines");

var width = 1200,
    height = 700,
    radius = Math.min(width, height) / 2;

var pie = d3.layout.pie()
    .sort(null)
    .value(function(d) {
        return d.value;
    });

var arc = d3.svg.arc()
    .outerRadius(radius * 0.8)
    .innerRadius(radius * 0.4);

var outerArc = d3.svg.arc()
    .innerRadius(radius * 0.9)
    .outerRadius(radius * 0.9);

svg.attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

// var nmtDomains = ['Java Heap', 'Class', 'Thread', 'Code', 'GC', 'Compiler', 'Internal', 'Other', 'Symbol',
//     'Native Memory Tracking', 'Shared class space', 'Arena Chunk', 'Logging', 'Arguments', 'Module', 'Synchronizer',
//     'Safepoint']

var nmtDomains = ['Other', 'Java Heap', 'Symbol',
    'Safepoint', 'Class', 'GC', 'Compiler', 'Thread', 'Code', 'Internal',
    'Logging', 'Arguments', 'Module', 'Synchronizer', 'Arguments',
    'Native Memory Tracking'
]


// var key = function(d){ return d.data.label + " (" + d.data.value + " KB)"; };
var key = function(d){ return d.data.key; };

var color = d3.scale.ordinal()
    .domain(nmtDomains)
    .range(["#ffd700",
        "#ffb14e",
        "#fa8775",
        "#ea5f94",
        "#cd34b5",
        "#9d02d7",
        "#0000ff",
        "#000000"]);

function displayNmtData(nmtData){
    var labels = color.domain();
    return labels.map(function(label){
        var val = nmtData[label];
        var key = label + " ( " + val + "KB)";
        // var key = label;
        return { label: label, key: key,  value: val }
        // return { label: label, key: label + " (" + nmtData[label] + " KB)",  value: nmtData[label] }
    });
}

function getData() {
    d3.json('/stats/nmt', function(nmtData) {
        change(displayNmtData(nmtData));
    });
}

refreshRuntimes();
getData();
setInterval(getData, 1000);


function change(data) {

    /* ------- PIE SLICES -------*/
    var slice = svg.select(".slices").selectAll("path.slice")
        .data(pie(data), key);

    slice.enter()
        .insert("path")
        .style("fill", function(d) { return color(d.data.label); })
        .attr("class", "slice");

    slice
        .transition().duration(1000)
        .attrTween("d", function(d) {
            this._current = this._current || d;
            var interpolate = d3.interpolate(this._current, d);
            this._current = interpolate(0);
            return function(t) {
                return arc(interpolate(t));
            };
        })

    slice.exit()
        .remove();

    /* ------- TEXT LABELS -------*/

    var text = svg.select(".labels").selectAll("text")
        .data(pie(data), key);

    text.enter()
        .append("text")
        .attr("dy", ".35em")
        .text(function(d) {
            // if(d.data.value == 'undefined' ||  d.data.value < 100) {
            //     return "";
            // }
            // else {
            return d.data.label + " (" + d.data.value + " KB)";
            // return d.data.key;
            // }
        });
    function midAngle(d){
        return d.startAngle + (d.endAngle - d.startAngle)/2;
    }

    text.transition().duration(1000)
        .attrTween("transform", function(d) {
            this._current = this._current || d;
            var interpolate = d3.interpolate(this._current, d);
            this._current = interpolate(0);
            return function(t) {
                var d2 = interpolate(t);
                var pos = outerArc.centroid(d2);
                pos[0] = radius * (midAngle(d2) < Math.PI ? 1 : -1);
                return "translate("+ pos +")";
            };
        })
        .styleTween("text-anchor", function(d){
            this._current = this._current || d;
            var interpolate = d3.interpolate(this._current, d);
            this._current = interpolate(0);
            return function(t) {
                var d2 = interpolate(t);
                return midAngle(d2) < Math.PI ? "start":"end";
            };
        });

    text.exit()
        .remove();

    /* ------- SLICE TO TEXT POLYLINES -------*/

    var polyline = svg.select(".lines").selectAll("polyline")
        .data(pie(data), key);

    polyline.enter()
        .append("polyline");

    polyline.transition().duration(1000)
        .attrTween("points", function(d){
            this._current = this._current || d;
            var interpolate = d3.interpolate(this._current, d);
            this._current = interpolate(0);
            return function(t) {
                var d2 = interpolate(t);
                var pos = outerArc.centroid(d2);
                pos[0] = radius * 0.95 * (midAngle(d2) < Math.PI ? 1 : -1);
                return [arc.centroid(d2), outerArc.centroid(d2), pos];
            };
        });

    polyline.exit()
        .remove();
};

