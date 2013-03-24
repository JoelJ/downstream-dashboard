package com.attask.downstreamdashboard;

import hudson.Extension;
import hudson.model.*;
import jenkins.model.Jenkins;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * User: Joel Johnson
 * Date: 3/23/13
 * Time: 2:35 PM
 */
@ExportedBean
public class DownstreamDashboard extends View {
	private static final String DEFAULT_TREE_QUERY = "";

	private WeakReference<AbstractProject> projectWeakReference;
	private volatile SearchMap searchMap;
	private String jobName;
	private int defaultSize;
	private String treeQuery;

	@DataBoundConstructor
	public DownstreamDashboard(String name) {
		super(name);
	}

	@Override
	protected void submit(StaplerRequest request) throws IOException, ServletException, Descriptor.FormException {
		String jobName = RequestUtils.getParameter(request, "_.jobName");
		if(this.jobName == null || !this.jobName.equals(jobName)) {
			setJobName(jobName);
			searchMap = null;
		}
		setDefaultSize(RequestUtils.getParameter(request, "_.defaultSize", 50));
		setTreeQuery(RequestUtils.getParameter(request, "_.treeQuery", DEFAULT_TREE_QUERY));
	}

	/**
	 * Invalidates the search map so the entire job's build list is re-indexed.
	 */
	@SuppressWarnings("UnusedDeclaration")
	public void doInvalidate(StaplerRequest request, StaplerResponse response) {
		checkPermission(View.DELETE);
		searchMap = null;
	}

	@Exported
	public List<Run> getRuns() {
		StaplerRequest currentRequest = Stapler.getCurrentRequest();
		int count = defaultSize;
		List<String> search = Arrays.asList("projectName="+jobName);

		if(currentRequest != null) {
			search = RequestUtils.getParameters(currentRequest, "search", search);
			count = RequestUtils.getParameter(currentRequest, "count", count);
		}

		SearchMap searchMap = getAndPopulateSearchMap(findProject());
		Set<Run> searchResult = searchMap.search(search);
		List<Run> result = new ArrayList<Run>();
		Iterator<Run> iterator = searchResult.iterator();
		for(int index = 0; iterator.hasNext() && index < count; index++) {
			Run next = iterator.next();
			result.add(next);
		}
		return result;
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
		if(result == null && getJobName() != null) {
			AbstractProject nearest = AbstractProject.findNearest(getJobName());
			if (nearest != null && nearest.getName().equals(getJobName())) {
				projectWeakReference = new WeakReference<AbstractProject>(nearest);
				result = nearest;
			}
		}

		return result;
	}

	@Override
	public void onJobRenamed(Item item, String oldName, String newName) {
		if(oldName != null && oldName.equals(getJobName())) {
			setJobName(newName);
		}
	}

	@Override
	public Item doCreateItem(StaplerRequest request, StaplerResponse response) throws IOException, ServletException {
		return Jenkins.getInstance().doCreateItem(request, response);
	}

	@Override
	public boolean contains(TopLevelItem item) {
		return false;
	}

	@Override
	public Collection<TopLevelItem> getItems() {
		AbstractProject project = findProject();
		if(project instanceof TopLevelItem) {
			return Arrays.asList((TopLevelItem) project);
		} else {
			return Collections.emptyList();
		}
	}

	@Exported
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@Exported
	public int getDefaultSize() {
		return defaultSize;
	}

	public void setDefaultSize(int defaultSize) {
		this.defaultSize = defaultSize;
	}

	@Exported
	public String getTreeQuery() {
		return treeQuery;
	}

	public void setTreeQuery(String treeQuery) {
		this.treeQuery = treeQuery;
	}

	@Extension
	public static final class DescriptorImpl extends ViewDescriptor {
		@Override
		public String getDisplayName() {
			return "Downstream View";
		}
	}
}
