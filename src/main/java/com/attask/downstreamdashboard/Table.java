package com.attask.downstreamdashboard;

import hudson.EnvVars;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * User: Joel Johnson
 * Date: 3/24/13
 * Time: 4:30 PM
 */
@ExportedBean
public class Table {
	@Nullable private volatile SearchMap searchMap;
	@Nullable private WeakReference<AbstractProject> projectWeakReference;

	@NotNull private final String jobName;
	@NotNull private final String query;
	@NotNull private final String treeQuery;
	private final int count;

	public Table(@NotNull String jobName, int count, @NotNull String query, @NotNull String treeQuery) {
		this.jobName = jobName;
		this.count = count;
		this.query = query;
		this.treeQuery = treeQuery;
	}

	@Exported
	public List<Run> getRuns() {
		EnvVars vars = new EnvVars();
		vars.put("jobName", this.jobName);
		vars.put("currentUserId", Table.currentUserId());

		String query = vars.expand(this.query);

		SearchMap searchMap = getAndPopulateSearchMap(findProject());
		Set<Run> searchResult = searchMap.search(Arrays.asList(query));
		List<Run> runs = new ArrayList<Run>();
		Iterator<Run> iterator = searchResult.iterator();
		for(int index = 0; iterator.hasNext() && index < count; index++) {
			Run next = iterator.next();
			runs.add(next);
		}
		return runs;
	}

	@NotNull
	private SearchMap getAndPopulateSearchMap(@Nullable AbstractProject project) {
		SearchMap searchMap = this.searchMap;

		if(searchMap == null) {
			synchronized (this) {
				//noinspection ConstantConditions
				if(searchMap == null) {
					searchMap = new SearchMap();
					this.searchMap = searchMap;
				}
			}
		}

		if(project != null) {
			searchMap.populate(project);
		}
		return searchMap;
	}

	@Nullable
	private AbstractProject findProject() {
		AbstractProject result = null;
		if(projectWeakReference != null) {
			result = projectWeakReference.get();
		}

		// It's not a big deal to search for the project so we don't synchronize here.
		if(result == null) {
			AbstractProject nearest = AbstractProject.findNearest(getJobName());
			if (nearest != null && nearest.getName().equals(getJobName())) {
				projectWeakReference = new WeakReference<AbstractProject>(nearest);
				result = nearest;
			}
		}

		return result;
	}


	@Exported
	@NotNull
	public String getJobName() {
		return jobName;
	}

	@Exported
	public int getCount() {
		return count;
	}

	@Exported
	@NotNull
	public String getQuery() {
		return query;
	}

	@Exported
	@NotNull
	public String getTreeQuery() {
		return treeQuery;
	}

	@NotNull
	private static String currentUserId() {
		User user = User.current();
		if(user == null) {
			return "unknown";
		}
		return user.getId();
	}

	@Override
	public String toString() {
		return "Table{" +
				"jobName='" + jobName + '\'' +
				", query='" + query + '\'' +
				", treeQuery='" + treeQuery + '\'' +
				", count=" + count +
				'}';
	}
}
