package com.leo.ostbm;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
		REMOVE_TEXTBOX("Remove Textbox");

		String description;

		Icon(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}

	private static Map<Icon, ImageIcon> icons;
	private static BufferedImage box;
	private static BufferedImage arrow;
	private static Font fontBase;
	private static Font fontBox;

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
		fontBase = Font.createFont(Font.TRUETYPE_FONT, new File("res/font.ttf"));
		fontBox = fontBase.deriveFont(Font.PLAIN, 20);
	}

	public static void initImages() throws IOException, URISyntaxException {
		box = ImageIO.read(new File("res/box.png"));
		arrow = ImageIO.read(new File("res/arrow.png"));
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
		faces = new HashMap<>();
		addFace(FACE_BLANK, null, new BufferedImage(96, 96, BufferedImage.TYPE_4BYTE_ABGR));
		File facesFolder = new File("res/faces");
		if (!facesFolder.exists()) {
			JOptionPane.showMessageDialog(null,
					"The faces folder doesn't exist!\nPlease make sure there's a \"faces\" folder inside the resources folder (\"res\").",
					"Faces folder doesn't exist!", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		addFaces(facesFolder, false);
		sortFaces();
		loadingCustom = true;
	}

	public static Font getFontBox() {
		return fontBox;
	}

	public static BufferedImage getBox() {
		return box;
	}

	public static BufferedImage getArrow() {
		return arrow;
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
					System.err.println("Ignoring exception from trying to load \"" + face.getName()
							+ "\" from directory \"" + dir.getPath() + "\":");
					e.printStackTrace();
				}
			}
	}

	public static void sortFaces() {
		faces = faces.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

}
