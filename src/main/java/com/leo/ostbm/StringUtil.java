package com.leo.ostbm;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {

	@NotNull
	@Contract("_, _ -> new")
	public static SplitResult split(@NotNull final String s, final char delim) {
		List<String> arr = new ArrayList<>();
		List<Integer> pos = new ArrayList<>();
		int foundPosition;
		int startIndex = 0;
		while ((foundPosition = s.indexOf(delim, startIndex)) > -1) {
			arr.add(s.substring(startIndex, foundPosition));
			pos.add(startIndex);
			startIndex = foundPosition + 1;
		}
		arr.add(s.substring(startIndex));
		pos.add(startIndex);
		final int size = arr.size();
		return new SplitResult(size, arr.toArray(new String[size]), pos.stream().mapToInt(i -> i).toArray());
	}

	public static class SplitResult {
		public final int partCount;
		public final String[] parts;
		public final int[] partIndex;

		public SplitResult(final int partCount, @NotNull final String[] parts, final int[] partPositions) {
			if (partCount != parts.length)
				throw new RuntimeException("parts length does not match partCount");
			if (partCount != partPositions.length)
				throw new RuntimeException("partPositions length does not match partCount");
			this.partCount = partCount;
			this.parts = parts;
			partIndex = partPositions;
		}
	}

}
