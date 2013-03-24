package com.attask.downstreamdashboard;

import hudson.model.Run;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a build and it's downstream jobs.
 * Keeps the insertion order of runs, so they should all be in triggered-order.
 *
 * User: Joel Johnson
 * Date: 3/23/13
 * Time: 4:52 PM
 */
@ExportedBean
public class Row implements Serializable {
	@NotNull private final Map<String, Run> runs;
	@NotNull private final List<String> runNames;

	public Row(@NotNull Run parentRun, @NotNull List<Run> downstreamBuilds) {
		int size = downstreamBuilds.size() + 1;
		Map<String, Run> runs = new LinkedHashMap<String, Run>(size); //Keep insertion order
		List<String> runNames = new ArrayList<String>(size);

		runNames.add(parentRun.getExternalizableId());
		runs.put(parentRun.getExternalizableId(), parentRun);
		for (Run downstreamBuild : downstreamBuilds) {
			String key = downstreamBuild.getExternalizableId();
			runNames.add(key);
			runs.put(key, downstreamBuild);
		}

		this.runs = Collections.unmodifiableMap(runs);
		this.runNames = runNames;
	}

	@NotNull
	@Exported
	public Map<String, Run> getRuns() {
		return Collections.unmodifiableMap(runs);
	}

	@NotNull
	@Exported
	public List<String> getRunNames() {
		return Collections.unmodifiableList(runNames);
	}

	@Exported
	public int getSize() {
		return getRunNames().size();
	}

	@Override
	public String toString() {
		return "Row{" +
				"runNames=" + runNames +
				", size=" + getSize() +
				'}';
	}
}
