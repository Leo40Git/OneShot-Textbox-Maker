package com.leo.ostbm;

import com.leo.ostbm.Resources.Facepic;
import com.leo.ostbm.StringUtil.SplitResult;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

public class TextboxUtil {

    public static final Map<Integer, Color> TEXTBOX_PRESET_COLORS;
    public static final Map<Integer, String> TEXTBOX_PRESET_COLOR_NAMES;
    private static final Map<Integer, ParsedTextbox> tpdCache = new HashMap<>();

    static {
        final Map<Integer, Color> colors = new HashMap<>();
        final Map<Integer, String> cnames = new HashMap<>();
        colors.put(0, new Color(255, 255, 255));
        cnames.put(0, "white");
        colors.put(1, new Color(255, 64, 64));
        cnames.put(1, "red");
        colors.put(2, new Color(0, 224, 0));
        cnames.put(2, "green");
        colors.put(3, new Color(255, 255, 0));
        cnames.put(3, "yellow");
        colors.put(4, new Color(64, 64, 255));
        cnames.put(4, "blue");
        colors.put(5, new Color(255, 64, 255));
        cnames.put(5, "purple");
        colors.put(6, new Color(64, 255, 255));
        cnames.put(6, "cyan");
        colors.put(7, new Color(128, 128, 128));
        cnames.put(7, "gray");
        TEXTBOX_PRESET_COLORS = Collections.unmodifiableMap(colors);
        TEXTBOX_PRESET_COLOR_NAMES = Collections.unmodifiableMap(cnames);
    }

    public static ParsedTextbox parseTextbox(final String face, final String text) {
        if (text == null)
            return null;
        if (tpdCache.containsKey(Objects.hash(face, text)))
            return tpdCache.get(Objects.hash(face, text));
        final List<StyleSpan> styleSpans = new LinkedList<>();
        final List<TextboxError> errors = new LinkedList<>();
        final Map<Integer, List<TextboxModifier>> mods = new LinkedHashMap<>();
        final ParsedTextbox ret = new ParsedTextbox(styleSpans, errors, mods);
        final StringBuilder strippedBuilder = new StringBuilder();
        final String[] lines = text.split("\n");
        final int[] strippedChars = new int[lines.length];
        if (lines.length > 4)
            errors.add(new TextboxError(-1, "Too many lines: has " + lines.length + " lines, but maximum is 4 lines"));
        int styleOff = 0, modPos = 0;
        Color col = Color.WHITE;
        String format = "";
        for (int i = 0; i < lines.length; i++) {
            StyleSpan.StyleType defType = StyleSpan.StyleType.NORMAL;
            if (i > 3)
                defType = StyleSpan.StyleType.ERROR;
            final SplitResult sp = StringUtil.split(lines[i], '`');
            if (sp.partCount == 0) {
                // no mods here
                final String line = lines[i];
                strippedBuilder.append(line);
                styleSpans.add(new StyleSpan(defType, 0, line.length(), col, format));
                continue;
            }
            // first part is always not a modifier
            final int firstLen = sp.parts[0].length();
            if (firstLen > 0) {
                final String part = sp.parts[0];
                modPos += part.length();
                strippedBuilder.append(part);
                styleSpans.add(new StyleSpan(defType, styleOff, part.length(), col, format));
            }
            for (int j = 1; j < sp.partCount; j++) {
                final int ind = sp.partIndex[j];
                final String part = sp.parts[j];
                final char mod = part.charAt(0);
                if (!TextboxModifier.MOD_CHARS.containsKey(mod)) {
                    modPos += 2;
                    strippedBuilder.append("`" + mod);
                    styleSpans.add(new StyleSpan(StyleSpan.StyleType.ERROR, styleOff + ind - 1, 2));
                    errors.add(new TextboxError(i, "Unknown modifier: '" + mod + "'"));
                    continue;
                }
                final TextboxModifier.ModType modType = TextboxModifier.MOD_CHARS.get(mod);
                int modLen = 2;
                final boolean noArgsPossible = Arrays.binarySearch(modType.argNums, 0) >= 0;
                final boolean noArgs = part.indexOf('[') < 0;
                if (!noArgsPossible && noArgs) {
                    modPos += 2;
                    strippedBuilder.append("`" + mod);
                    styleSpans.add(new StyleSpan(StyleSpan.StyleType.ERROR, styleOff + ind - 1, 2));
                    errors.add(new TextboxError(i, "Bad modifier argument number: got 0 args for modifier '" + mod
                            + "', but that modifier does not accept 0 arguments"));
                    continue;
                }
                String[] args = new String[0];
                if (!noArgs) {
                    final int argsInd = 2;
                    final int end = part.indexOf(']');
                    if (end < 0) {
                        modPos += 2;
                        strippedBuilder.append("`" + mod);
                        styleSpans.add(new StyleSpan(StyleSpan.StyleType.ERROR, styleOff + ind - 1, 3));
                        errors.add(new TextboxError(i,
                                "Bad modifier format: args for modifier '" + mod + "' are never closed"));
                        continue;
                    }
                    if (argsInd == end) {
                        if (!noArgsPossible) {
                            modPos += 2;
                            strippedBuilder.append("`" + mod);
                            styleSpans.add(new StyleSpan(StyleSpan.StyleType.ERROR, styleOff + ind - 1, 2));
                            errors.add(new TextboxError(i, "Bad modifier argument number: got 0 args for modifier '"
                                    + mod + "', but that modifier does not accept that number of arguments"));
                            continue;
                        }
                    } else {
                        args = part.substring(argsInd, end).split(",");
                        if (Arrays.binarySearch(modType.argNums, args.length) < 0) {
                            modPos += 2;
                            strippedBuilder.append("`" + mod);
                            styleSpans.add(new StyleSpan(StyleSpan.StyleType.ERROR, styleOff + ind - 1, 2));
                            errors.add(new TextboxError(i,
                                    "Bad modifier argument number: got " + args.length + " args for modifier '" + mod
                                            + "', but that modifier does not accept that number of arguments"));
                            continue;
                        }
                    }
                    modLen += end;
                }
                strippedChars[i] += modLen;
                final TextboxModifier modObj = new TextboxModifier(modType, args);
                Main.LOGGER.trace("adding " + modObj + " to index " + modPos);
                ret.addModifier(modPos, modObj);
                styleSpans.add(new StyleSpan(StyleSpan.StyleType.MODIFIER, styleOff + ind - 1, modLen));
                final String normPart = part.substring(modLen - 1);
                modPos += normPart.length();
                strippedBuilder.append(normPart);
                Main.LOGGER.trace("next index will be " + modPos + " (after adding " + normPart.length() + ")");
                switch (modObj.type) {
                    case COLOR:
                        col = getColorModValue(modObj, Color.WHITE);
                        break;
                    case FORMAT:
                        if (modObj.args.length == 0)
                            format = "";
                        else
                            format = modObj.args[0].toLowerCase();
                        break;
                    default:
                        break;
                }
                styleSpans.add(new StyleSpan(defType, styleOff + ind - 1 + modLen, part.length(), col, format));
            }
            strippedBuilder.append('\n');
            styleOff += lines[i].length() + 1;
        }
        ret.strippedText = strippedBuilder.toString();
        // pass 2: length of stripped lines
        styleOff = 0;
        final String[] strippedLines = ret.strippedText.split("\n");
        final String face2 = face; // we need face for later
        for (int i = 0; i < strippedLines.length; i++) {
            int maxLen = 57;
            final boolean hasFace = !Resources.FACE_BLANK.equals(face2);
            if (hasFace)
                maxLen -= 10;
            final int len = strippedLines[i].length();
            if (len > maxLen) {
                styleSpans.add(new StyleSpan(StyleSpan.StyleType.ERROR, styleOff + strippedChars[i] + maxLen, len));
                errors.add(new TextboxError(i, "Line too long: has " + len + " characters , but maximum is " + maxLen
                        + " characters (with" + (!hasFace ? "out" : "") + " face)"));
            }
            styleOff += len + 1;
        }
        tpdCache.put(Objects.hash(face, text), ret);
        return ret;
    }

    public static BufferedImage drawTextbox(final String face, final String text, final boolean drawArrow,
                                            final int arrowOffset) {
        return drawTextbox(face, parseTextbox(face, text), drawArrow, arrowOffset);
    }

    public static BufferedImage drawTextbox(final String face, final ParsedTextbox tpd, final boolean drawArrow,
                                            final int arrowOffset) {
        final BufferedImage ret = new BufferedImage(608, 128, BufferedImage.TYPE_INT_ARGB);
        final Graphics g = ret.getGraphics();
        drawTextbox(g, face, tpd, 0, 0, drawArrow, arrowOffset);
        return ret;
    }

    public static BufferedImage drawTextbox(final String face, final String text, final boolean drawArrow) {
        return drawTextbox(face, text, drawArrow, 0);
    }

    public static BufferedImage drawTextbox(final String face, final ParsedTextbox tpd, final boolean drawArrow) {
        return drawTextbox(face, tpd, drawArrow, 0);
    }

    public static void drawTextbox(final Graphics g, final String face, final String text, final int x, final int y,
                                   final boolean drawArrow, final int arrowOffset) {
        drawTextbox(g, face, parseTextbox(face, text), x, y, drawArrow, arrowOffset);
    }

    public static void drawTextbox(final Graphics g, final String face, final ParsedTextbox tpd, final int x,
                                   final int y, final boolean drawArrow, final int arrowOffset) {
        g.drawImage(Resources.getTextboxImage(), x, y, null);
        final Facepic faceObj = Resources.getFace(face);
        if (faceObj != null)
            g.drawImage(faceObj.getImage(), x + 496, y + 16, null);
        if (drawArrow)
            g.drawImage(Resources.getTextboxArrow(), x + 299, y + 118 + arrowOffset, null);
        g.setColor(Color.WHITE);
        drawTextboxString(g, tpd, x + 20, y + 10);
    }

    public static void drawTextbox(final Graphics g, final String face, final String text, final int x, final int y,
                                   final boolean drawArrow) {
        drawTextbox(g, face, text, x, y, drawArrow, 0);
    }

    private static Color getColorModValue(final TextboxModifier mod, final Color defaultColor) {
        if (mod.type != TextboxModifier.ModType.COLOR)
            return defaultColor;
        Color retColor = defaultColor;
        final String[] cdata = mod.args;
        if (cdata.length == 3)
            retColor = new Color(Integer.parseInt(cdata[0]), Integer.parseInt(cdata[1]), Integer.parseInt(cdata[2]));
        else if (cdata.length == 1) {
            Integer preset = -1;
            try {
                preset = Integer.parseInt(cdata[0]);
                if (TEXTBOX_PRESET_COLORS.containsKey(preset))
                    retColor = TEXTBOX_PRESET_COLORS.get(preset);
            } catch (final NumberFormatException e) {
                String col = cdata[0];
                if (col.toLowerCase().startsWith("h:")) {
                    if (col.length() < 8)
                        return retColor;
                    col = col.substring(0, 8);
                    try {
                        retColor = Color.decode("0x" + col.substring(2));
                        return retColor;
                    } catch (final NumberFormatException e1) {
                        // ignore this error
                    }
                }
                final String cname = col.toLowerCase();
                for (final Map.Entry<Integer, String> entry : TEXTBOX_PRESET_COLOR_NAMES.entrySet())
                    if (cname.equals(entry.getValue())) {
                        retColor = TEXTBOX_PRESET_COLORS.get(entry.getKey());
                        break;
                    }
            }
        }
        return retColor;
    }

    private static void drawTextboxString(final Graphics g, final ParsedTextbox tpd, int x, int y) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setFont(Resources.getTextboxFont());
        final int startX = x;
        final Color defaultCol = g2.getColor();
        FontMetrics fm = g2.getFontMetrics();
        final int lineSpace = fm.getHeight() + 1;
        final String text = tpd.strippedText;
        int currentChar = 0;
        final String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];
            y += lineSpace;
            x = startX;
            final char[] chars = line.toCharArray();
            for (final char c : chars) {
                if (tpd.mods.containsKey(currentChar)) {
                    final List<TextboxModifier> mods = tpd.mods.get(currentChar);
                    for (int j = 0; j < mods.size(); j++) {
                        final TextboxModifier mod = mods.get(j);
                        Main.LOGGER.trace(i + ":" + currentChar + " - mod " + j + " is " + mod);
                        switch (mod.type) {
                            case COLOR:
                                g.setColor(getColorModValue(mod, defaultCol));
                                break;
                            case FORMAT:
                                Font f = g2.getFont();
                                final String format = mod.args.length == 0 ? "" : mod.args[0].toLowerCase();
                                Map<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
                                if (format.contains("i")) {
                                    map.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
                                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                                } else {
                                    map.put(TextAttribute.POSTURE, -1);
                                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                                }
                                if (format.contains("u"))
                                    map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                                else
                                    map.put(TextAttribute.UNDERLINE, -1);
                                if (format.contains("s"))
                                    map.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                                else
                                    map.put(TextAttribute.STRIKETHROUGH, -1);
                                f = f.deriveFont(map);
                                g2.setFont(f);
                                fm = g2.getFontMetrics();
                                break;
                            default:
                                break;
                        }
                    }
                }
                g2.drawString(Character.toString(c), x, y);
                x += fm.charWidth(c);
                currentChar++;
            }
        }
        g.setColor(defaultCol);
    }

    public static List<BufferedImage> makeTextboxAnimation(final List<Textbox> boxes) {
        final List<BufferedImage> ret = new ArrayList<>();
        for (int i = 0; i < boxes.size(); i++) {
            final Textbox box = boxes.get(i);
            final ParsedTextbox tpd = parseTextbox(box.face, box.text);
            final ParsedTextbox tpd2 = new ParsedTextbox(null, null, tpd.mods);
            tpd2.strippedText = "";
            final String text = tpd.strippedText;
            String face = box.face;
            boolean instant = false;
            int speed = 1;
            int delay = speed;
            if (tpd.mods.containsKey(0)) {
                final List<TextboxModifier> mods = tpd.mods.get(0);
                if (mods.get(0).type == TextboxModifier.ModType.INSTANT_INTERRUPT)
                    instant = true;
                else
                    for (int j = 0; j < mods.size(); j++) {
                        final TextboxModifier mod = mods.get(j);
                        switch (mod.type) {
                            case DELAY:
                                Integer newDelay = delay;
                                try {
                                    newDelay = Integer.parseInt(mod.args[0]);
                                    newDelay = Math.max(1, newDelay);
                                } catch (final NumberFormatException e) {
                                    Main.LOGGER.error("Error while parsing delay!", e);
                                    newDelay = delay;
                                }
                                delay = newDelay + speed;
                                break;
                            case SPEED:
                                Integer newSpeed = speed;
                                try {
                                    newSpeed = Integer.parseInt(mod.args[0]);
                                    newSpeed = Math.max(1, newSpeed);
                                } catch (final NumberFormatException e) {
                                    Main.LOGGER.error("Error while parsing speed!", e);
                                    newSpeed = speed;
                                }
                                speed = newSpeed;
                                break;
                            default:
                                break;
                        }
                    }
            }
            boolean endsWithInterrupt = false;
            if (tpd.mods.containsKey(text.length())) {
                final List<TextboxModifier> mods = tpd.mods.get(text.length());
                if (mods.get(mods.size() - 1).type == TextboxModifier.ModType.INSTANT_INTERRUPT)
                    endsWithInterrupt = true;
            }
            if (instant)
                ret.add(drawTextbox(face, tpd, false));
            else {
                if (delay == 1)
                    delay = speed;
                // add a blank textbox frame
                for (int d = 0; d < delay; d++)
                    ret.add(drawTextbox(box.face, "", false));
                for (int l = 0; l < text.length() - 1; l++) {
                    if (tpd.mods.containsKey(l + 1)) {
                        final List<TextboxModifier> mods = tpd.mods.get(l + 1);
                        for (final TextboxModifier mod : mods)
                            switch (mod.type) {
                                case DELAY:
                                    Integer newDelay = delay;
                                    try {
                                        newDelay = Integer.parseInt(mod.args[0]);
                                        newDelay = Math.max(0, newDelay);
                                    } catch (final NumberFormatException e) {
                                        Main.LOGGER.error("Error while parsing delay!", e);
                                        newDelay = delay;
                                    }
                                    delay = newDelay + speed;
                                    break;
                                case SPEED:
                                    Integer newSpeed = speed;
                                    try {
                                        newSpeed = Integer.parseInt(mod.args[0]);
                                        newSpeed = Math.max(1, newSpeed);
                                    } catch (final NumberFormatException e) {
                                        Main.LOGGER.error("Error while parsing speed!", e);
                                        newSpeed = speed;
                                    }
                                    speed = newSpeed;
                                    break;
                                default:
                                    break;
                            }
                    }
                    if (tpd.mods.containsKey(l)) {
                        final List<TextboxModifier> mods = tpd.mods.get(l);
                        for (final TextboxModifier mod : mods)
                            switch (mod.type) {
                                case FACE:
                                    if (mod.args.length == 0) {
                                        face = Resources.FACE_BLANK;
                                        break;
                                    }
                                    final String newFace = mod.args[0];
                                    if (Resources.getFace(newFace) != null)
                                        face = newFace;
                                    break;
                                default:
                                    break;
                            }
                    }
                    final char c = text.charAt(l);
                    tpd2.strippedText += c;
                    for (int d = 0; d < delay; d++)
                        ret.add(drawTextbox(face, tpd2, false));
                    delay = speed;
                }
            }
            if (!endsWithInterrupt)
                if (i == boxes.size() - 1) {
                    final BufferedImage frame = drawTextbox(face, tpd, false);
                    for (int d = 0; d < 48; d++)
                        ret.add(frame);
                } else {
                    int arrowOffset = 0, dir = 1;
                    for (int d = 0; d < 16; d++) {
                        arrowOffset += dir;
                        if (dir == 1) {
                            if (arrowOffset == 1)
                                dir = -1;
                        } else if (dir == -1)
                            if (arrowOffset == -1)
                                dir = 1;
                        final BufferedImage frame = drawTextbox(face, tpd, true, arrowOffset);
                        ret.add(frame);
                        ret.add(frame);
                        ret.add(frame);
                    }
                }
        }
        return ret;
    }

    public static class Textbox {
        public String face;
        public String text;

        public Textbox(final String face, final String text) {
            this.face = face;
            this.text = text;
        }

        public Textbox(final Textbox other) {
            this(other.face, other.text);
        }

        public Textbox(final String text) {
            this(Resources.FACE_BLANK, text);
        }

        public Textbox() {
            this(Resources.FACE_BLANK, "");
        }

        @Override
        public String toString() {
            String t = parseTextbox(face, text).strippedText.trim();
            t = t.replace('\n', ' ');
            final int maxLen = 27;
            if (t.length() > maxLen)
                t = t.substring(0, maxLen) + "...";
            if (t.isEmpty())
                t = "(empty)";
            else
                t = "\"" + t + "\"";
            return t;
        }
    }

    public static class TextboxModifier {

        public static final Map<Character, ModType> MOD_CHARS;

        static {
            final Map<Character, ModType> mc = new HashMap<>();
            for (final ModType type : ModType.values()) {
                if (mc.containsKey(type.getModChar()))
                    throw new RuntimeException("Duplicate mod character key!");
                mc.put(type.getModChar(), type);
            }
            MOD_CHARS = Collections.unmodifiableMap(mc);
        }

        public final ModType type;
        public final String[] args;
        public TextboxModifier(final ModType type, final String[] args) {
            this.type = type;
            this.args = args;
        }

        @Override
        public String toString() {
            return "TextboxModifier [type=" + type + ", args=" + Arrays.toString(args) + "]";
        }

        public enum ModType {
            FACE('@', 0, 1), COLOR('c', 0, 1, 3), DELAY('d', 1), INSTANT_INTERRUPT('i'), SPEED('s', 1),
            UNICODE_INSERT('u', 1), FORMAT('f', 0, 1);

            private char modChar;
            private int[] argNums;

            ModType(final char modChar) {
                this.modChar = modChar;
                argNums = new int[]{0};
            }

            ModType(final char modChar, final int argNum) {
                this.modChar = modChar;
                argNums = new int[]{argNum};
            }

            ModType(final char modChar, final int... argNums) {
                this.modChar = modChar;
                this.argNums = argNums;
            }

            public char getModChar() {
                return modChar;
            }

            public int[] getArgumentNumbers() {
                return argNums;
            }
        }
    }

    public static class StyleSpan {
        public final StyleType type;
        public final int pos;
        public final int length;
        public final Color color;
        public final String format;
        public StyleSpan(final StyleType type, final int pos, final int length, final Color color,
                         final String format) {
            this.type = type;
            this.pos = pos;
            this.length = length;
            this.color = color;
            this.format = format;
        }

        public StyleSpan(final StyleType type, final int pos, final int length) {
            this(type, pos, length, null, null);
        }

        public enum StyleType {
            NORMAL, MODIFIER, ERROR
        }
    }

    public static class TextboxError {
        public final int lineNum;
        public final String message;

        public TextboxError(final int lineNum, final String message) {
            this.lineNum = lineNum;
            this.message = message;
        }
    }

    public static class ParsedTextbox {
        public final List<StyleSpan> styleSpans;
        public final List<TextboxError> errors;
        public final Map<Integer, List<TextboxModifier>> mods;
        public String strippedText;

        public ParsedTextbox(final List<StyleSpan> styleSpans, final List<TextboxError> errors,
                             final Map<Integer, List<TextboxModifier>> mods) {
            this.styleSpans = styleSpans;
            this.errors = errors;
            this.mods = mods;
        }

        public void addModifier(final int pos, final TextboxModifier mod) {
            List<TextboxModifier> list = mods.get(pos);
            if (list == null) {
                list = new LinkedList<>();
                mods.put(pos, list);
            }
            list.add(mod);
        }
    }

}
