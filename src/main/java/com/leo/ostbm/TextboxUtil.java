package com.leo.ostbm;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.leo.ostbm.Resources.Facepic;
import com.leo.ostbm.util.UnmodifiableMapWrapper;

public class TextboxUtil {

	public static class Textbox {
		public String face;
		public String text;

		public Textbox(String face, String text) {
			this.face = face;
			this.text = text;
		}

		public Textbox(String text) {
			this(Resources.FACE_BLANK, text);
		}

		public Textbox() {
			this(Resources.FACE_BLANK, "");
		}

		@Override
		public String toString() {
			String t = parseTextbox(text).strippedText.trim();
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

		public enum ModType {
			FACE(0, 1), COLOR(0, 1, 3), DELAY(1), INTERRUPT;

			private int[] argNums;

			ModType() {
				argNums = new int[] { 0 };
			}

			ModType(int argNum) {
				argNums = new int[] { argNum };
			}

			ModType(int... argNums) {
				this.argNums = argNums;
			}

			public int[] getArgumentNumbers() {
				return argNums;
			}
		}

		public ModType type;
		public String[] args;
		public int length;
		public int position;
	}

	public static class TextboxParseData {
		public String strippedText;
		public Map<Integer, List<TextboxModifier>> mods;

		public TextboxParseData() {
			mods = new HashMap<>();
		}
	}

	private static final Map<String, TextboxParseData> tpdCache = new HashMap<>();

	public static TextboxParseData parseTextbox(String text) {
		if (tpdCache.containsKey(text))
			return tpdCache.get(text);
		TextboxParseData ret = new TextboxParseData();
		StringBuilder strippedBuilder = new StringBuilder();
		List<String> tokenList = new ArrayList<>();
		StringBuilder tokenBuilder = new StringBuilder();
		char[] chars = text.toCharArray();
		for (char c : chars) {
			if (c == '\\') {
				tokenList.add(tokenBuilder.toString());
				tokenBuilder.setLength(0);
			}
			tokenBuilder.append(c);
		}
		if (tokenBuilder.length() != 0)
			tokenList.add(tokenBuilder.toString());
		int posDelta = 0, pos = 0, realPos = 0;
		for (String token : tokenList) {
			int realLength = token.replaceAll("\n", "").length();
			boolean doModCheck = true;
			if (!token.startsWith("\\") || token.length() <= 1)
				doModCheck = false;
			if (doModCheck) {
				char modChar = token.charAt(1);
				TextboxModifier.ModType modType = null;
				switch (modChar) {
				case 'd':
					modType = TextboxModifier.ModType.DELAY;
					break;
				case 'c':
					modType = TextboxModifier.ModType.COLOR;
					break;
				case 'i':
					modType = TextboxModifier.ModType.INTERRUPT;
					break;
				case '@':
					modType = TextboxModifier.ModType.FACE;
					break;
				}
				if (modType != null) {
					TextboxModifier mod = new TextboxModifier();
					mod.type = modType;
					int[] argns = modType.getArgumentNumbers();
					String[] args = new String[10];
					boolean noArgsPossible = Arrays.binarySearch(argns, 0) >= 0;
					boolean noArgs = noArgsPossible && argns.length == 1;
					char third = 0;
					if (token.length() > 2)
						third = token.charAt(2);
					if (!noArgs && (third != '[' || token.indexOf(']') == -1)) {
						if (noArgsPossible)
							noArgs = true;
						else {
							strippedBuilder.append(token);
							continue;
						}
					}
					if (noArgs) {
						mod.length = 2;
						posDelta = 2;
						token = token.substring(2);
						mod.args = new String[0];
					} else {
						String argStr = token.substring(3, token.indexOf(']'));
						mod.length = 4 + argStr.length();
						posDelta = token.indexOf(']') + 1;
						token = token.substring(token.indexOf(']') + 1);
						String[] argStrs = argStr.split(",");
						if (Arrays.binarySearch(argns, argStrs.length) >= 0) {
							args = new String[argStrs.length];
							for (int i = 0; i < args.length; i++)
								args[i] = argStrs[i];
							mod.args = args;
						} else {
							mod.args = new String[0];
						}
					}
					mod.position = realPos;
					int pos2 = pos;
					if (mod.type == TextboxModifier.ModType.DELAY) {
						mod.length++;
						pos--;
						if (pos < 0)
							pos = 0;
					}
					if (ret.mods.containsKey(pos))
						ret.mods.get(pos).add(mod);
					else {
						List<TextboxModifier> modList = new LinkedList<>();
						modList.add(mod);
						ret.mods.put(pos, modList);
					}
					if (mod.type == TextboxModifier.ModType.DELAY)
						pos = pos2;
				}
			}
			pos += realLength;
			pos -= posDelta;
			posDelta = 0;
			realPos += realLength;
			strippedBuilder.append(token);
		}
		ret.strippedText = strippedBuilder.toString();
		tpdCache.put(text, ret);
		return ret;
	}

	public static BufferedImage drawTextbox(String face, String text, boolean drawArrow, int arrowOffset) {
		return drawTextbox(face, parseTextbox(text), drawArrow, arrowOffset);
	}

	public static BufferedImage drawTextbox(String face, TextboxParseData tpd, boolean drawArrow, int arrowOffset) {
		BufferedImage ret = new BufferedImage(608, 128, BufferedImage.TYPE_INT_ARGB);
		Graphics g = ret.getGraphics();
		drawTextbox(g, face, tpd, 0, 0, drawArrow, arrowOffset);
		return ret;
	}

	public static BufferedImage drawTextbox(String face, String text, boolean drawArrow) {
		return drawTextbox(face, text, drawArrow, 0);
	}

	public static BufferedImage drawTextbox(String face, TextboxParseData tpd, boolean drawArrow) {
		return drawTextbox(face, tpd, drawArrow, 0);
	}

	public static void drawTextbox(Graphics g, String face, String text, int x, int y, boolean drawArrow,
			int arrowOffset) {
		drawTextbox(g, face, parseTextbox(text), x, y, drawArrow, arrowOffset);
	}

	public static void drawTextbox(Graphics g, String face, TextboxParseData tpd, int x, int y, boolean drawArrow,
			int arrowOffset) {
		g.drawImage(Resources.getTextboxImage(), x, y, null);
		Facepic faceObj = Resources.getFace(face);
		if (faceObj != null)
			g.drawImage(faceObj.getImage(), x + 496, y + 16, null);
		if (drawArrow)
			g.drawImage(Resources.getTextboxArrow(), x + 299, y + 118 + arrowOffset, null);
		g.setColor(Color.WHITE);
		drawTextboxString(g, tpd, x + 20, y + 10);
	}

	public static void drawTextbox(Graphics g, String face, String text, int x, int y, boolean drawArrow) {
		drawTextbox(g, face, text, x, y, drawArrow, 0);
	}

	public static final Map<Integer, Color> TEXTBOX_PRESET_COLORS;
	public static final Map<Integer, String> TEXTBOX_PRESET_COLOR_NAMES;

	static {
		Map<Integer, Color> colors = new HashMap<>();
		Map<Integer, String> cnames = new HashMap<>();
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
		TEXTBOX_PRESET_COLORS = new UnmodifiableMapWrapper<>(colors);
		TEXTBOX_PRESET_COLOR_NAMES = new UnmodifiableMapWrapper<>(cnames);
	}

	public static Color getColorModValue(TextboxModifier mod, Color defaultColor) {
		if (mod.type != TextboxModifier.ModType.COLOR)
			return defaultColor;
		Color retColor = defaultColor;
		String[] cdata = mod.args;
		if (cdata.length == 3) {
			retColor = new Color(Integer.parseInt(cdata[0]), Integer.parseInt(cdata[1]), Integer.parseInt(cdata[2]));
		} else if (cdata.length == 1) {
			Integer preset = -1;
			try {
				preset = Integer.parseInt(cdata[0]);
				if (TEXTBOX_PRESET_COLORS.containsKey(preset))
					retColor = TEXTBOX_PRESET_COLORS.get(preset);
			} catch (NumberFormatException e) {
				String col = cdata[0];
				if (col.toLowerCase().startsWith("h:")) {
					if (col.length() < 8) {
						return retColor;
					}
					col = col.substring(0, 8);
					try {
						retColor = Color.decode("0x" + col.substring(2));
						return retColor;
					} catch (NumberFormatException e1) {
						// ignore this error
					}
				}
				String cname = col.toLowerCase();
				for (Map.Entry<Integer, String> entry : TEXTBOX_PRESET_COLOR_NAMES.entrySet()) {
					if (cname.equals(entry.getValue())) {
						retColor = TEXTBOX_PRESET_COLORS.get(entry.getKey());
						break;
					}
				}
			}
		}
		return retColor;
	}

	private static void drawTextboxString(Graphics g, TextboxParseData tpd, int x, int y) {
		g.setFont(Resources.getTextboxFont());
		final int startX = x;
		final Color defaultCol = g.getColor();
		final FontMetrics fm = g.getFontMetrics();
		final int lineSpace = fm.getHeight() + 1;
		String text = tpd.strippedText;
		int currentChar = 0;
		for (String line : text.split("\n")) {
			y += lineSpace;
			x = startX;
			char[] chars = line.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				if (tpd.mods.containsKey(currentChar)) {
					List<TextboxModifier> mods = tpd.mods.get(currentChar);
					for (TextboxModifier mod : mods) {
						if (mod.type == TextboxModifier.ModType.COLOR)
							g.setColor(getColorModValue(mod, defaultCol));
					}
				}
				char c = chars[i];
				g.drawString(Character.toString(c), x, y);
				x += fm.charWidth(c);
				currentChar++;
			}
		}
		g.setColor(defaultCol);
	}

	public static List<BufferedImage> makeTextboxAnimation(List<Textbox> boxes) {
		List<BufferedImage> ret = new ArrayList<>();
		for (int i = 0; i < boxes.size(); i++) {
			Textbox box = boxes.get(i);
			TextboxParseData tpd = parseTextbox(box.text);
			TextboxParseData tpd2 = new TextboxParseData();
			tpd2.strippedText = "";
			tpd2.mods = tpd.mods;
			String text = tpd.strippedText;
			String face = box.face;
			boolean instant = false;
			if (tpd.mods.containsKey(0)) {
				List<TextboxModifier> mods = tpd.mods.get(0);
				if (mods.get(0).type == TextboxModifier.ModType.INTERRUPT)
					instant = true;
			}
			boolean endsWithInterrupt = false;
			if (tpd.mods.containsKey(text.length())) {
				List<TextboxModifier> mods = tpd.mods.get(text.length());
				if (mods.get(mods.size() - 1).type == TextboxModifier.ModType.INTERRUPT)
					endsWithInterrupt = true;
			}
			if (!instant)
				// add a blank textbox frame
				ret.add(drawTextbox(box.face, "", false));
			for (int l = 0; l < text.length() - 1; l++) {
				int delay = 1;
				if (tpd.mods.containsKey(l)) {
					List<TextboxModifier> mods = tpd.mods.get(l);
					for (TextboxModifier mod : mods) {
						switch (mod.type) {
						case DELAY:
							Integer newDelay = delay;
							try {
								newDelay = Integer.parseInt(mod.args[0]);
							} catch (NumberFormatException e) {
								Main.LOGGER.error("Error while parsing delay!", e);
								newDelay = delay;
							}
							delay = newDelay;
							break;
						case FACE:
							if (mod.args.length == 0) {
								face = Resources.FACE_BLANK;
								break;
							}
							String newFace = mod.args[0];
							if (Resources.getFace(newFace) != null)
								face = newFace;
							break;
						default:
							break;
						}
					}
				}
				if (!instant) {
					char c = text.charAt(l);
					tpd2.strippedText += c;
					for (int d = 0; d < delay; d++)
						ret.add(drawTextbox(face, tpd2, false));
				}
				delay = 1;
			}
			if (instant) {
				ret.add(drawTextbox(face, tpd, false));
			}
			if (!endsWithInterrupt) {
				if (i == boxes.size() - 1) {
					BufferedImage frame = drawTextbox(face, tpd, false);
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
						BufferedImage frame = drawTextbox(face, tpd, true, arrowOffset);
						ret.add(frame);
						ret.add(frame);
						ret.add(frame);
					}
				}
			}
		}
		return ret;
	}

}
