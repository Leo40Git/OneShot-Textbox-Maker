package com.leo.ostbm;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {

    public static SplitResult split(final String s, final char delim) {
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
        return new SplitResult(size, arr.toArray(new String[size]), pos.stream().mapToInt(i->i).toArray());
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

}
