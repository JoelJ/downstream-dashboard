package com.attask.downstreamdashboard;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * User: Joel Johnson
 * Date: 3/24/13
 * Time: 5:30 PM
 */
@ExportedBean
public class TableConfiguration {
	private String jobName;
	private final int count;
	private final String query;
	private final String treeQuery;

	@DataBoundConstructor
	public TableConfiguration(String jobName, int count, String query, String treeQuery) {
		this.jobName = jobName;
		this.count = count;
		this.query = query;
		this.treeQuery = treeQuery;
	}

	@Exported
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@Exported
	public int getCount() {
		return count;
	}

	@Exported
	public String getQuery() {
		return query;
	}

	@Exported
	public String getTreeQuery() {
		return treeQuery;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TableConfiguration that = (TableConfiguration) o;

		if (count != that.count) return false;
		if (jobName != null ? !jobName.equals(that.jobName) : that.jobName != null) return false;
		if (query != null ? !query.equals(that.query) : that.query != null) return false;
		//noinspection RedundantIfStatement
		if (treeQuery != null ? !treeQuery.equals(that.treeQuery) : that.treeQuery != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = jobName != null ? jobName.hashCode() : 0;
		result = 31 * result + count;
		result = 31 * result + (query != null ? query.hashCode() : 0);
		result = 31 * result + (treeQuery != null ? treeQuery.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "TableConfiguration{" +
				"jobName='" + jobName + '\'' +
				", count=" + count +
				", query='" + query + '\'' +
				", treeQuery='" + treeQuery + '\'' +
				'}';
	}
}
