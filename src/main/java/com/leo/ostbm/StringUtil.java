package com.leo.ostbm;

public class StringUtil {

	public static class SplitResult {
		public final int partCount;
		public final String[] parts;
		public final int[] partIndex;

		public SplitResult(final int partCount, final String[] parts, final int[] partPositions) {
			if (partCount != parts.length)
				throw new RuntimeException("parts length does not match partCount");
			if (partCount != partPositions.length)
				throw new RuntimeException("partPositions length does not match partCount");
			this.partCount = partCount;
			this.parts = parts;
			partIndex = partPositions;
		}
	}

	public static SplitResult split(final String s, final char delim) {
		final String[] parts = s.split(Character.toString(delim));
		final int partCount = parts.length;
		final int[] partPos = new int[partCount];
		final SplitResult ret = new SplitResult(partCount, parts, partPos);
		if (partCount == 0)
			return ret;
		int off = 0;
		for (int i = 0; i < partCount; i++) {
			partPos[i] = off;
			off += parts[i].length() + 1;
		}
		return ret;
	}

	public static void main(final String[] args) {
		final String s = "/a/bc/d";
		final SplitResult sp = split(s, '/');
		System.out.println(
				String.format("Original string is \"%s\", split by delimiter \"/\" into %d parts", s, sp.partCount));
		final char[] c = new char[s.length()];
		for (int i = 0; i < sp.partCount; i++) {
			System.out.println(
					String.format("Part %d is \"%s\" (originally at index %d)", i, sp.parts[i], sp.partIndex[i]));
			if (sp.partIndex[i] != 0)
				c[sp.partIndex[i] - 1] = '/';
			final char[] partC = sp.parts[i].toCharArray();
			System.arraycopy(partC, 0, c, sp.partIndex[i], partC.length);
		}
		System.out.println(String.format("Reconstruction: \"%s\"", new String(c)));
	}

}
