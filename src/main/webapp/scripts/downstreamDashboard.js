function TableControl($scope, $http, $location) {
	$scope.loadTable = function() {
		$scope.nextUpdate = null;
		var query = "?tree=" + $scope.treeQuery + ($location.url().replace('?', '&'));
		var url = location.pathname + "api/json"+query;
		console.log("requesting:", url);

		$http.get(url).success( function( data ) {
			if(data.tables) {
				for(var tableIndex = data.tables.length - 1; tableIndex >= 0; tableIndex--) {
					var table = data.tables[tableIndex];
					if(table) {
						for(var runIndex = table.runs.length - 1; runIndex >= 0; runIndex--) {
							var run = table.runs[runIndex];
							if(run.actions) {
								$scope.normalizeActions(run);
							}
						}
					}
				}
			}
			$scope.data = data;

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
			} else if (action.failCount !== undefined) {
				run.failCount = action.failCount;
				run.totalCount = action.totalCount;
				run.skipCount = action.skipCount;
				run.urlName = action.urlName;
			}
		}
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

	$scope.treeQuery = DOWNSTREAM_DASHBOARD.treeQuery;

	$scope.loadTable();
}