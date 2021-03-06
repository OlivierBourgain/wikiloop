
function Wikiloop($scope) {

    $scope.update = function() {
        var term = $("input").val();
        if (!term) return;
        $scope.pages = null;
        $scope.term = term;
        $.ajax({
            //url: "http://www.obourgain.com/wikiloop/loop/"+term,
            url: "http://vps234916.ovh.net:8012/wikiloop/loop/"+term,
            dataType: "jsonp",
            cache : "true",
            success: function(data) {
                $scope.pages = data.body;
                var first = term;
                $scope.nonloop = -1;
                $scope.loop = -1;
                $scope.first = null;
                for(var i = 0; i < data.body.length; i++) {
                    if (data.body[i].inLoop) {
                        if ($scope.first == null) $scope.first = data.body[i].name;
                        $scope.loop++;
                    } else {
                        $scope.nonloop++;
                    }
                }
                $scope.$apply()
            }
        });
    }
}


// Autocomplétion sur recherche.
$(function(){
    $( "#search-box" ).autocomplete({
	    source: function(request, response) {
	        return $.ajax({
	            url: "http://fr.wikipedia.org/w/api.php",
	            dataType: "jsonp",
	            data: {
	                action: "opensearch",
	                format: "json",
	                search: request.term
	            },
	            success: function(data) {
	                $("results").empty();
	                response(data[1]);
	            }
	        });
	    },
	    select: function( event, ui ) {
	    	angular.element('#search-btn').triggerHandler('click');
	    },
    });
    $("#search-box").on("click", function () {
        $(this).select();
    });
});