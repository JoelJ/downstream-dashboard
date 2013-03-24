package com.attask.downstreamdashboard;

import hudson.Extension;
import hudson.model.*;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

/**
 * User: Joel Johnson
 * Date: 3/23/13
 * Time: 2:35 PM
 */
@ExportedBean
public class DownstreamDashboard extends View {
	private static final String DEFAULT_TREE_QUERY = "";
	private static final int DEFAULT_COUNT = 5;
	private static final String DEFAULT_SEARCH_QUERY = "projectName=${jobName}";

	private String jobName;
	private int count;
	private String query;
	private String treeQuery;

	private transient volatile Map<String, Table> tables;

	@DataBoundConstructor
	public DownstreamDashboard(String name) {
		super(name);
	}

	@Override
	protected void submit(StaplerRequest request) throws IOException, ServletException, Descriptor.FormException {
		String jobName = RequestUtils.getParameter(request, "_.jobName");
		if(this.jobName == null || !this.jobName.equals(jobName)) {
			setJobName(jobName);
			tables = null; //We changed the project we're looking at. So we need to throw away the index.
		}
		setCount(RequestUtils.getParameter(request, "_.count", DEFAULT_COUNT));
		setQuery(RequestUtils.getParameter(request, "_.query", DEFAULT_SEARCH_QUERY));
		setTreeQuery(RequestUtils.getParameter(request, "_.treeQuery", DEFAULT_TREE_QUERY));
	}

	/**
	 * Invalidates the search map so the entire job's build list is re-indexed.
	 */
	@SuppressWarnings("UnusedDeclaration")
	public void doInvalidate(StaplerRequest request, StaplerResponse response) {
		checkPermission(View.DELETE);
		tables = null;
	}

	@Exported
	public List<Table> getTables() {
		Map<String, Table> tables = this.tables;
		if(tables == null) {
			synchronized (this) {
				tables = this.tables;
				if(tables == null) {
					tables = new HashMap<String, Table>();
				}
			}
		}

		String jobName = this.jobName;
		int count = this.count;
		String query = this.query;
		String treeQuery = this.treeQuery;

		Table table = tables.get(jobName);
		if(table == null) {
			//noinspection SynchronizationOnLocalVariableOrMethodParameter
			synchronized (tables) {
				table = tables.get(jobName);
				if(table == null) {
					table = new Table(jobName, count, query, treeQuery);
					tables.put(jobName, table);
				}
			}
		}

		return Arrays.asList(table);
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
		AbstractProject project = AbstractProject.findNearest(getJobName());
		if(project instanceof TopLevelItem) {
			return Arrays.asList((TopLevelItem) project);
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * The job that is being displays and queried for downstream jobs.
	 */
	@Exported
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	/**
	 * The default size of the list being returned if none is specified in the request.
	 */
	@Exported
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * The value used in the API queries as the 'tree' parameter.
	 * This lets the user define how much data to collect from the server without blasting the results with depth.
	 *
	 * TODO: actually implement this feature
	 */
	@Exported
	public String getTreeQuery() {
		return treeQuery;
	}

	public void setTreeQuery(String treeQuery) {
		this.treeQuery = treeQuery;
	}

	@Exported
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	@Extension
	public static final class DescriptorImpl extends ViewDescriptor {
		@Override
		public String getDisplayName() {
			return "Downstream View";
		}
	}
}
