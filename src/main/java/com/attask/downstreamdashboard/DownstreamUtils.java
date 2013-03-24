package com.attask.downstreamdashboard;

import com.attask.jenkins.DownstreamBuildsAction;
import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: Joel Johnson
 * Date: 3/23/13
 * Time: 2:46 PM
 */
public class DownstreamUtils {
	public static boolean containsDownstream(@NotNull Run run, @NotNull TopLevelItem toFind) {
		return containsDownstream(run, toFind, new HashSet<Run>());
	}

	public static boolean containsDownstream(@NotNull Run run, @NotNull TopLevelItem toFind, Set<Run> visited) {
		DownstreamBuildsAction action = run.getAction(DownstreamBuildsAction.class);
		if(action == null) {
			return false;
		}
		List<Run> downstreamBuilds = action.getDownstreamBuilds();
		if(downstreamBuilds == null) {
			return false;
		}

		for (Run downstreamBuild : downstreamBuilds) {
			if(toFind.equals(downstreamBuild)) {
				return true;
			}
		}

		for (Run downstreamBuild : downstreamBuilds) {
			if(!visited.contains(downstreamBuild)) {
				visited.add(downstreamBuild);
				return containsDownstream(downstreamBuild, toFind, visited);
			}
		}

		return false;
	}

	@NotNull
	public static List<Run> getDownstreamBuilds(@NotNull Actionable run) {
		List<Run> downstreamBuilds = null;

		DownstreamBuildsAction action = run.getAction(DownstreamBuildsAction.class);
		if(action != null) {
			downstreamBuilds = action.getDownstreamBuilds();

		}
		return downstreamBuilds != null ? downstreamBuilds : Collections.<Run>emptyList();
	}
}
