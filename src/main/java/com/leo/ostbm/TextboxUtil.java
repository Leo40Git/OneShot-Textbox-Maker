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

		enum ModType {
			FACE(1), COLOR(0, 1, 3), DELAY(1), INTERRUPT;

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

	public static TextboxParseData parseTextbox(String text) {
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
		int nextPos = 0, pos = nextPos, realPos = 0;
		for (String token : tokenList) {
			int realLength = token.length();
			nextPos += realLength;
			System.out.println("token=" + token);
			boolean doModCheck = true;
			if (!token.startsWith("\\")) {
				System.out.println("not a mod");
				doModCheck = false;
			} else if (token.length() <= 1) {
				System.out.println("too short");
				doModCheck = false;
			}
			if (doModCheck) {
				char modChar = token.charAt(1);
				System.out.println("modChar=" + modChar);
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
				System.out.println("modType=" + modType);
				if (modType != null) {
					TextboxModifier mod = new TextboxModifier();
					mod.type = modType;
					int[] argns = modType.getArgumentNumbers();
					String[] args = new String[10];
					boolean noArgsPossible = Arrays.binarySearch(argns, 0) >= 0;
					boolean noArgs = noArgsPossible && argns.length == 1;
					char third = 0;
					if (token.length() > 3)
						third = token.charAt(3);
					if (!noArgs && (third == '[' || token.indexOf(']') == -1)) {
						if (noArgsPossible)
							noArgs = true;
						else {
							System.out.println("missing argument decleration");
							strippedBuilder.append(token);
							continue;
						}
					}
					if (noArgs) {
						mod.length = 2;
						nextPos -= 2;
						token = token.substring(2);
						mod.args = new String[0];
					} else {
						String argStr = token.substring(3, token.indexOf(']'));
						mod.length = 4 + argStr.length();
						nextPos -= token.indexOf(']') + 1;
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
					System.out.println("mod position in unstripped string is " + mod.position);
					if (ret.mods.containsKey(pos))
						ret.mods.get(pos).add(mod);
					else {
						List<TextboxModifier> modList = new LinkedList<>();
						modList.add(mod);
						ret.mods.put(pos, modList);
					}
				}
			}
			if (nextPos > strippedBuilder.length())
				nextPos = strippedBuilder.length();
			System.out.println("strippedToken=" + token);
			System.out.println("pos=" + pos + ",nextPos=" + nextPos);
			System.out.println("mod has been put at position " + pos);
			System.out.println("next position is " + nextPos);
			strippedBuilder.append(token);
			pos = nextPos;
			realPos += realLength;
		}
		ret.strippedText = strippedBuilder.toString();
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

	public static final Map<Integer, Color> TEXTBOX_PRESET_COLORS = new HashMap<>();
	public static final Map<Integer, String> TEXTBOX_PRESET_COLOR_NAMES = new HashMap<>();

	static {
		TEXTBOX_PRESET_COLORS.put(0, new Color(255, 255, 255));
		TEXTBOX_PRESET_COLOR_NAMES.put(0, "white");
		TEXTBOX_PRESET_COLORS.put(1, new Color(255, 64, 64));
		TEXTBOX_PRESET_COLOR_NAMES.put(1, "red");
		TEXTBOX_PRESET_COLORS.put(2, new Color(0, 224, 0));
		TEXTBOX_PRESET_COLOR_NAMES.put(2, "green");
		TEXTBOX_PRESET_COLORS.put(3, new Color(255, 255, 0));
		TEXTBOX_PRESET_COLOR_NAMES.put(3, "yellow");
		TEXTBOX_PRESET_COLORS.put(4, new Color(64, 64, 255));
		TEXTBOX_PRESET_COLOR_NAMES.put(4, "blue");
		TEXTBOX_PRESET_COLORS.put(5, new Color(255, 64, 255));
		TEXTBOX_PRESET_COLOR_NAMES.put(5, "purple");
		TEXTBOX_PRESET_COLORS.put(6, new Color(64, 255, 255));
		TEXTBOX_PRESET_COLOR_NAMES.put(6, "cyan");
		TEXTBOX_PRESET_COLORS.put(7, new Color(128, 128, 128));
		TEXTBOX_PRESET_COLOR_NAMES.put(7, "gray");
	}

	private static void drawTextboxString(Graphics g, TextboxParseData tpd, int x, int y) {
		g.setFont(Resources.getTextboxFont());
		final int startX = x;
		final Color defaultCol = g.getColor();
		final FontMetrics fm = g.getFontMetrics();
		final int lineSpace = fm.getHeight() + 1;
		String text = tpd.strippedText;
		for (String line : text.split("\n")) {
			y += lineSpace;
			x = startX;
			char[] chars = line.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				System.out.println("drawing character " + i);
				if (tpd.mods.containsKey(i)) {
					System.out.println("has mod(s)!");
					List<TextboxModifier> mods = tpd.mods.get(i);
					for (TextboxModifier mod : mods) {
						if (mod.type == TextboxModifier.ModType.COLOR) {
							System.out.println("mod is color mod!");
							String[] cdata = mod.args;
							if (cdata.length == 3) {
								System.out.println("3 args, custom color");
								g.setColor(new Color(Integer.parseInt(cdata[0]), Integer.parseInt(cdata[1]),
										Integer.parseInt(cdata[2])));
							} else if (cdata.length == 1) {
								System.out.println("1 arg, preset color");
								Integer preset = Integer.parseInt(cdata[0]);
								if (TEXTBOX_PRESET_COLORS.containsKey(preset)) {
									System.out.println("switching to preset color " + preset + ": "
											+ TEXTBOX_PRESET_COLOR_NAMES.get(preset));
									g.setColor(TEXTBOX_PRESET_COLORS.get(preset));
								} else {
									System.out.println(
											"nonexistent preset color " + preset + ", resetting to default color");
									g.setColor(defaultCol);
								}
							} else {
								System.out.println("no args, reset to default color");
								g.setColor(defaultCol);
							}
						}
					}
				}
				char c = chars[i];
				g.drawString(Character.toString(c), x, y);
				x += fm.charWidth(c);
			}
			g.setColor(defaultCol);
		}
	}

	public static List<BufferedImage> makeTextboxAnimation(List<Textbox> boxes) {
		List<BufferedImage> ret = new ArrayList<>();
		for (int i = 0; i < boxes.size(); i++) {
			Textbox box = boxes.get(i);
			// add a blank textbox frame
			ret.add(drawTextbox(box.face, "", false));
			TextboxParseData tpd = parseTextbox(box.text);
			TextboxParseData tpd2 = new TextboxParseData();
			tpd2.strippedText = "";
			tpd2.mods = tpd.mods;
			String text = tpd.strippedText;
			String face = box.face;
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
								e.printStackTrace();
								newDelay = delay;
							}
							delay = newDelay;
							break;
						case FACE:
							String newFace = mod.args[0];
							if (Resources.getFace(newFace) != null)
								face = newFace;
							break;
						default:
							break;
						}
					}
				}
				char c = text.charAt(l);
				tpd2.strippedText += c;
				for (int d = 0; d < delay; d++)
					ret.add(drawTextbox(face, tpd2, false));
				delay = 1;
			}
			boolean endsWithInterrupt = false;
			System.out.println("looking for mod at end of string (" + (text.length()) + ")");
			if (tpd.mods.containsKey(text.length())) {
				System.out.println("found mod at end of string");
				List<TextboxModifier> mods = tpd.mods.get(text.length());
				if (mods.get(mods.size() - 1).type == TextboxModifier.ModType.INTERRUPT) {
					System.out.println("mod is INTERRUPT");
					endsWithInterrupt = true;
				} else {
					System.out.println("mod is not INTERRUPT");
				}
			} else {
				System.out.println("no mod at end of string");
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
