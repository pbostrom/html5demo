$("#es").on("click", function(event) {
    var source = new EventSource("/event-source");
    source.onmessage = function (msg) {
	alert(msg.data);
    };
});

var lp = function(event) {
    $.ajax({
	url: "/long-poll"
    }).done(function(data) {
	alert(data);
	lp(null);
    });
}

$("#lp").on("click", lp);

$("#ws").on("click", function(event) {
    var socket = new WebSocket("ws://localhost:8080/socket");
    socket.onmessage = function(msg){  
	alert(msg.data);
    };
    var reset = $("<button id='reset'>Reset Count</button>");
    reset.on("click", function(event) {
	socket.send("reset");
    });
    $("#ws").after(reset);
});

$("#inc").on("click", function(event) {
    $.ajax({
	url: "/increment"
    }).done(function(data) {
	$("#count").text(data);
    });
});

$("#go").on("click", function(e) {
    $("input").each(function(i) {
	console.log($(this).val());
    });
});
