package com.attask.downstreamdashboard;

import hudson.model.*;
import hudson.scm.ChangeLogSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: Joel Johnson
 * Date: 3/23/13
 * Time: 3:15 PM
 */
public class SearchMap {
	private final Map<String, Set<String>> map = new HashMap<String, Set<String>>();

	private int oldest = 0;
	private int size = 0;
	private final Object populateLock = new Object();

	public Set<Run> search(@NotNull Iterable<String> ids) {
		Set<Run> result = new TreeSet<Run>(new RunNumberComparator());
		for (String id : ids) {
			Set<String> buildIds = map.get(id);
			if (buildIds != null) {
				for (String buildId : buildIds) {
					Run<?, ?> run = Run.fromExternalizableId(buildId);
					result.add(run);
				}
			}
		}
		return result;
	}

	public void populate(@NotNull AbstractProject project) {
		synchronized (populateLock) {
			Run lastBuild = project.getLastBuild();
			if (lastBuild == null) {
				return;
			}

			if (lastBuild.getNumber() <= oldest) {
				return;
			}

			for (int number = lastBuild.getNumber(); number > oldest; number--) {
				Run build = project.getBuildByNumber(number);
				if(build == null) {
					continue;
				}
				String externalizableId = build.getExternalizableId();

				size++;
				put("projectName", project.getName(), externalizableId);
				populateWithChangeInformation(build, externalizableId);
				populateWithCauseInformation(build, externalizableId);
				populateWithParameters(build, externalizableId);
			}

			oldest = lastBuild.getNumber();
		}
	}

	private void populateWithParameters(Run build, String externalizableId) {
		ParametersAction action = build.getAction(ParametersAction.class);
		if (action != null) {
			List<ParameterValue> parameters = action.getParameters();
			if (parameters != null) {
				for (ParameterValue parameter : parameters) {
					if (parameter instanceof StringParameterValue) {
						put("parameter-"+parameter.getName(), ((StringParameterValue) parameter).value, externalizableId);
					}
				}
			}
		}
	}

	private void populateWithCauseInformation(Run build, String externalizableId) {
		//noinspection unchecked
		List<Cause> causes = (List<Cause>) build.getCauses();
		if (causes != null) {
			for (Cause cause : causes) {
				if (cause instanceof Cause.UserIdCause) {
					Cause.UserIdCause userIdCause = (Cause.UserIdCause) cause;
					String userId = userIdCause.getUserId();
					if (userId != null) {
						put("causeId", userId, externalizableId);
					}

					String userName = userIdCause.getUserName();
					if (userName != null) {
						put("causeName", userName, externalizableId);
					}
				}
			}
		}
	}

	private void populateWithChangeInformation(Run build, String id) {
		if (build instanceof AbstractBuild) {
			ChangeLogSet changeSet = ((AbstractBuild) build).getChangeSet();
			if (changeSet != null) {
				Object[] items = changeSet.getItems();
				if (items != null) {
					for (Object item : items) {
						if (item != null && item instanceof ChangeLogSet.Entry) {
							ChangeLogSet.Entry changeSetItem = (ChangeLogSet.Entry) item;
							populateChangeEntry(id, changeSetItem);
						}
					}
				}
			}
		}
	}

	private void populateChangeEntry(String runId, ChangeLogSet.Entry changeSetItem) {
		String commitId = changeSetItem.getCommitId();
		if (commitId != null) {
			put("revision", commitId, runId);
		}

		User committer = changeSetItem.getAuthor();
		if (committer != null) {
			put("committerId", committer.getId(), runId);
			put("committerName", committer.getFullName(), runId);
		}

		//TODO: Branch
	}

	private void put(String id, String key, String value) {
		synchronized (populateLock) {
			Set<String> strings = map.get(id + "=" + key);
			if (strings == null) {
				strings = new HashSet<String>();
				map.put(id + "=" + key, strings);
			}
			strings.add(value);
		}
	}

	/**
	 * Sorts by build number. Highest first.
	 */
	private static class RunNumberComparator implements Comparator<Run> {
		public int compare(Run o1, Run o2) {
			if (o1 == null) {
				if (o2 == null) {
					return 0;
				} else {
					return -1;
				}
			}
			if (o2 == null) {
				return 1;
			}

			return ((Integer) o2.getNumber()).compareTo(o1.getNumber());
		}
	}

	@Override
	public String toString() {
		return "SearchMap{" +
				"size=" + size +
				", oldest=" + oldest +
				'}';
	}
}
