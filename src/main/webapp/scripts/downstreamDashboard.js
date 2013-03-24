function TableControl($scope, $http, $location) {
	$scope.loadTable = function() {
		$scope.nextUpdate = null;
		var query = "?depth=3" + ($location.url().replace('?', '&'));
		var url = location.pathname + "api/json"+query;
		console.log("requesting:", url);

		$http.get(url).success( function( data ) {
			$scope.table = data;
			for(var runIndex = data.runs.length - 1; runIndex >= 0; runIndex--) {
				var run = data.runs[runIndex];
				if(run.actions) {
					$scope.normalizeActions(run);
				}
			}

			if($scope.autoRefresh) {
				console.log("scheduling auto refresh");
				$scope.nextUpdate = setTimeout($scope.loadTable, 10000);
			} else {
				console.log("skipping auto refresh");
			}
		});
	};

	$scope.normalizeActions = function(run) {
		for (var actionIndex = run.actions.length - 1; actionIndex >= 0; actionIndex--) {
			var action = run.actions[actionIndex];
			if (action.downstreamBuilds) {
				run.downstreamBuilds = action.downstreamBuilds;
				for (var downstreamIndex = run.downstreamBuilds.length - 1; downstreamIndex >= 0; downstreamIndex--) {
					var downstreamBuild = run.downstreamBuilds[downstreamIndex];
					$scope.normalizeActions(downstreamBuild);
				}
			} else if (action.failCount) {
				run.failCount = action.failCount;
				run.totalCount = action.totalCount;
				run.skipCount = action.skipCount;
				run.urlName = action.urlName;
			}
		}
	};

	$scope.setSearch = function(value) {
		if($scope.pendingSearchTimeout) {
			clearTimeout($scope.pendingSearchTimeout);
		}

		$scope.pendingSearchTimeout = setTimeout(function() {
			$scope.cancelUpdate();
			if(!$scope.search) {
				$scope.search = null;
			}
			$location.replace();
			$location.search('search', $scope.search);
			$scope.loadTable();
		}, 1000);
	};

	$scope.autoRefresh = !location.search.match("disableAutoRefresh=true");
	$scope.toggleAutoRefresh = function() {
		if($scope.autoRefresh) {
			console.log("enabling auto refresh");
			$scope.loadTable();
		} else {
			console.log("disabling auto refresh");
			$scope.cancelUpdate();
		}
	};

	$scope.cancelUpdate = function() {
		if($scope.nextUpdate) {
			clearTimeout($scope.nextUpdate);
			$scope.nextUpdate = null;
		}
	};

	$scope.loadTable();
}