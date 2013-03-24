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

	private List<TableConfiguration> tableConfigurations;

	private transient volatile Map<TableConfiguration, Table> tables;

	@DataBoundConstructor
	public DownstreamDashboard(String name) {
		super(name);
	}

	@Override
	protected void submit(StaplerRequest request) throws IOException, ServletException, Descriptor.FormException {
		List<TableConfiguration> tableConfigurations = new ArrayList<TableConfiguration>();

		String jobName = RequestUtils.getParameter(request, "_.jobName");
		int count = RequestUtils.getParameter(request, "_.count", DEFAULT_COUNT);
		String query = RequestUtils.getParameter(request, "_.query", DEFAULT_SEARCH_QUERY);
		String treeQuery = RequestUtils.getParameter(request, "_.treeQuery", DEFAULT_TREE_QUERY);
		TableConfiguration configuration = new TableConfiguration(jobName, count, query, treeQuery);
		tableConfigurations.add(configuration);

		this.tableConfigurations = tableConfigurations;
		tables = null;
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
		Map<TableConfiguration, Table> tables = this.tables;
		if(tables == null) {
			synchronized (this) {
				tables = this.tables;
				if(tables == null) {
					tables = new HashMap<TableConfiguration, Table>();
					this.tables = tables;
				}
			}
		}

		List<Table> result = new ArrayList<Table>(tableConfigurations.size());
		for (TableConfiguration tableConfiguration : tableConfigurations) {
			Table table = tables.get(tableConfiguration);
			if(table == null) {
				//noinspection SynchronizationOnLocalVariableOrMethodParameter
				synchronized (tables) {
					table = tables.get(tableConfiguration);
					if(table == null) {
						table = new Table(tableConfiguration);
						tables.put(tableConfiguration, table);
					}
				}
			}
			result.add(table);
		}

		return result;
	}


	@Override
	public void onJobRenamed(Item item, String oldName, String newName) {
		if(tables != null && oldName != null) {
			for (TableConfiguration tableConfiguration : tableConfigurations) {
				if(oldName.equals(tableConfiguration.getJobName())) {
					tableConfiguration.setJobName(newName);
					//noinspection SynchronizeOnNonFinalField
					synchronized (tables) {
						tables.remove(tableConfiguration);
					}
				}
			}
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
		return Collections.emptyList();
	}

	@Extension
	public static final class DescriptorImpl extends ViewDescriptor {
		@Override
		public String getDisplayName() {
			return "Downstream View";
		}
	}
}
