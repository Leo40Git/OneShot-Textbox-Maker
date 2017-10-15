package com.leo.ostbm;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class Resources {

	public static final String FACE_BLANK = "(none)";

	public static class Facepic {
		private String name;
		private File file;
		private BufferedImage image;
		private ImageIcon icon;
		private boolean custom;

		public Facepic(String name, File file, BufferedImage image, boolean custom) {
			this.name = name;
			this.file = file;
			this.image = image;
			makeIcon();
			this.custom = custom;
		}

		private void makeIcon() {
			final int smolSize = 48;
			BufferedImage imageSmol = new BufferedImage(smolSize, smolSize, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics g = imageSmol.getGraphics();
			g.drawImage(image, 0, 0, smolSize, smolSize, null);
			icon = new ImageIcon(imageSmol, "icon for face " + name);
		}

		public String getName() {
			return name;
		}

		public File getFile() {
			return file;
		}

		public BufferedImage getImage() {
			return image;
		}

		public ImageIcon getIcon() {
			return icon;
		}

		public boolean isCustom() {
			return custom;
		}
	}

	private static Map<String, Facepic> faces;
	private static boolean loadingCustom;

	public enum Icon {
		NEW_PROJECT("New Project"),
		LOAD_PROJECT("Load Project"),
		SAVE_PROJECT("Save Project"),
		SAVE_PROJECT_AS("Save Project As..."),
		EXIT("Exit"),
		CHECK_FOR_UPDATES("Check for Updates"),
		ABOUT("About"),
		FACE_FOLDER("Open Facepic Folder"),
		ADD_FACE("Add Custom Facepic"),
		ADD_TEXTBOX("Add Textbox"),
		REMOVE_TEXTBOX("Remove Textbox"),
		INSERT_TEXTBOX_BEFORE("Insert Textbox Before"),
		INSERT_TEXTBOX_AFTER("Insert Textbox After"),
		MOVE_TEXTBOX_UP("Move Textbox Up"),
		MOVE_TEXTBOX_DOWN("Move Textbox Down"),
		SETTINGS("Settings");

		String description;

		Icon(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}

	private static List<BufferedImage> appIcons;
	private static Map<Icon, ImageIcon> icons;
	private static BufferedImage textboxImage;
	private static BufferedImage textboxArrow;
	private static Font fontBase;
	private static Font textboxFont;

	public static void checkResFolder() {
		File resFolder = new File("res");
		if (!resFolder.exists()) {
			JOptionPane.showMessageDialog(null,
					"The resources folder doesn't exist!\nPlease make sure there's a \"res\" folder next to the application that contains the faces.",
					"Resources folder doesn't exist!", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	public static void initFonts() throws FontFormatException, IOException {
		fontBase = Font.createFont(Font.TRUETYPE_FONT, new File("res/textboxFont.ttf"));
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		textboxFont = fontBase.deriveFont(Font.BOLD, 20);
		ge.registerFont(textboxFont);
	}

	public static void initImages() throws IOException, URISyntaxException {
		textboxImage = ImageIO.read(new File("res/textboxImage.png"));
		textboxArrow = ImageIO.read(new File("res/textboxArrow.png"));
		appIcons = new LinkedList<>();
		final String[] sizes = new String[] { "16", "32", "64" };
		for (String size : sizes)
			appIcons.add(ImageIO.read(Resources.class.getResourceAsStream("/appicon" + size + ".png")));
		BufferedImage iconSheet = ImageIO.read(Resources.class.getResourceAsStream("/icons.png"));
		icons = new HashMap<>();
		int ix = 0, iy = 0;
		for (Icon icon : Icon.values()) {
			icons.put(icon, new ImageIcon(iconSheet.getSubimage(ix, iy, 16, 16), icon.getDescription()));
			ix += 16;
			if (ix == iconSheet.getWidth()) {
				ix = 0;
				iy += 16;
			}
		}
		initFaces();
	}

	public static void initFaces() throws IOException {
		loadingCustom = false;
		faces = new HashMap<>();
		addFace(FACE_BLANK, null, new BufferedImage(96, 96, BufferedImage.TYPE_4BYTE_ABGR));
		File facesFolder = new File("res/faces");
		if (!facesFolder.exists()) {
			JOptionPane.showMessageDialog(null,
					"The faces folder doesn't exist!\nPlease make sure there's a \"faces\" folder inside the resources folder (\"res\").",
					"Faces folder doesn't exist!", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		addFaces(facesFolder, Config.getBoolean(Config.KEY_HIDE_SOLSTICE_FACES, true));
		sortFaces();
		loadingCustom = true;
	}

	public static Font getTextboxFont() {
		return textboxFont;
	}

	public static BufferedImage getTextboxImage() {
		return textboxImage;
	}

	public static BufferedImage getTextboxArrow() {
		return textboxArrow;
	}
	
	public static final int APPICON_16 = 0;
	public static final int APPICON_32 = 1;
	public static final int APPICON_64 = 2;
	
	public static List<BufferedImage> getAppIcons() {
		return appIcons;
	}

	public static ImageIcon getIcon(Icon icon) {
		return icons.get(icon);
	}

	static final String[] DUMMY_STRING_ARRAY = new String[] {};

	public static String[] getFaces() {
		return faces.keySet().toArray(DUMMY_STRING_ARRAY);
	}

	public static Facepic getFace(String name) {
		return faces.get(name);
	}

	public static String addFace(File face) throws IOException {
		String faceName = face.getName();
		if (!faceName.contains(".")
				|| !faceName.substring(faceName.lastIndexOf(".") + 1, faceName.length()).equalsIgnoreCase("png"))
			return null;
		faceName = faceName.substring(0, faceName.lastIndexOf('.'));
		BufferedImage image = ImageIO.read(face);
		faceName = addFace(faceName, face, image);
		return faceName;
	}

	public static String addFace(String name, File file, BufferedImage face) {
		if (face.getWidth() != 96 || face.getHeight() != 96)
			throw new IllegalArgumentException("Face dimensions must be 96 by 96!");
		while (faces.containsKey(name))
			name += "-";
		faces.put(name, new Facepic(name, file, face, loadingCustom));
		return name;
	}

	public static void addFaces(File dir, boolean ignoreSolstice) throws IOException {
		if (dir.isFile()) {
			addFace(dir);
			return;
		}
		for (File face : dir.listFiles())
			if (face.isDirectory()) {
				if (ignoreSolstice) {
					if (!"solstice".equals(face.getName()))
						addFaces(face, false);
				} else
					addFaces(face, false);
			} else {
				try {
					addFace(face);
				} catch (Exception e) {
					Main.LOGGER.error("Error while loading facepic!", e);
					JOptionPane.showMessageDialog(null,
							"Could not load facepic " + face.getName() + "!\n(at " + face.getAbsolutePath() + ")\n" + e,
							"Could not load facepic!", JOptionPane.ERROR_MESSAGE);
				}
			}
	}

	public static void sortFaces() {
		faces = faces.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

}
