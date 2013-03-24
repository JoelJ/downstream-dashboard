package com.attask.downstreamdashboard;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * User: Joel Johnson
 * Date: 3/23/13
 * Time: 2:39 PM
 */
public class RequestUtils {
	@Nullable
	public static String getParameter(@NotNull StaplerRequest request, @NotNull String name, @Nullable String defaultValue) {
		String result = request.getParameter(name);
		if(result == null || result.isEmpty()) {
			result = defaultValue;
		}
		return result;
	}

	@Nullable
	public static String getParameter(@NotNull StaplerRequest request, @NotNull String name) {
		return getParameter(request, name, null);
	}

	public static List<String> getParameters(StaplerRequest currentRequest, String name, List<String> defaultValue) {
		String[] parameterValues = currentRequest.getParameterValues(name);
		if(parameterValues == null || parameterValues.length == 0) {
			return defaultValue;
		}
		return Arrays.asList(parameterValues);
	}

	@NotNull
	public static List<String> getParameters(@NotNull StaplerRequest currentRequest, @NotNull String name) {
		return getParameters(currentRequest, name, Collections.<String>emptyList());
	}

	public static int getParameter(@NotNull StaplerRequest currentRequest, @NotNull String name, int defaultValue) {
		String parameter = getParameter(currentRequest, name);
		if(parameter == null) {
			return defaultValue;
		}

		return Integer.parseInt(parameter);
	}
}
