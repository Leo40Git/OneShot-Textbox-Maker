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

	private static Map<String, BufferedImage> faces;
	private static Map<String, ImageIcon> faceIcons;
	private static Map<String, File> faceFiles;
	private static BufferedImage box;
	private static BufferedImage arrow;
	private static Font fontBase;
	private static Font fontBox;

	public static void initFonts() throws FontFormatException, IOException {
		fontBase = Font.createFont(Font.TRUETYPE_FONT, Resources.class.getResourceAsStream("/font.ttf"));
		fontBox = fontBase.deriveFont(Font.PLAIN, 20);
	}

	public static void initImages() throws IOException, URISyntaxException {
		box = ImageIO.read(Resources.class.getResourceAsStream("/box.png"));
		arrow = ImageIO.read(Resources.class.getResourceAsStream("/arrow.png"));
		faces = new HashMap<>();
		faceIcons = new HashMap<>();
		faceFiles = new HashMap<>();
		addFace(FACE_BLANK, new BufferedImage(96, 96, BufferedImage.TYPE_4BYTE_ABGR));
		File facesFolder = new File("faces");
		if (!facesFolder.exists()) {
			JOptionPane.showMessageDialog(null,
					"The faces folder doesn't exist!\nPlease make sure there's a \"faces\" folder next to the application that contains the faces.",
					"Faces folder doesn't exist!", JOptionPane.ERROR_MESSAGE);
		}
		File ignoreSolstice = new File(facesFolder.getPath() + "/nospoilers");
		addFaces(facesFolder, ignoreSolstice.exists() && !new File("ignorenospoilers").exists());
		sortFaces();
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

	static final String[] DUMMY_STRING_ARRAY = new String[] {};

	public static String[] getFaces() {
		return faces.keySet().toArray(DUMMY_STRING_ARRAY);
	}

	public static BufferedImage getFace(String name) {
		return faces.get(name);
	}

	public static String addFace(File face) throws IOException {
		String faceName = face.getName();
		if (!faceName.contains(".")
				|| !faceName.substring(faceName.lastIndexOf(".") + 1, faceName.length()).equalsIgnoreCase("png"))
			return null;
		faceName = faceName.substring(0, faceName.lastIndexOf('.'));
		BufferedImage image = ImageIO.read(face);
		faceName = addFace(faceName, image);
		faceFiles.put(faceName, face);
		return faceName;
	}

	public static String addFace(String name, BufferedImage face) {
		if (face.getWidth() != 96 || face.getHeight() != 96)
			throw new IllegalArgumentException("Face dimensions must be 96 by 96!");
		while (faces.containsKey(name))
			name += "-";
		faces.put(name, face);
		addFaceIcon(name);
		return name;
	}

	public static String addFace(File file, String name, BufferedImage face) {
		name = addFace(name, face);
		faceFiles.put(name, file);
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

	private static void addFaceIcon(String face) {
		if (!faces.containsKey(face))
			return;
		final int smolSize = 48;
		BufferedImage imageSmol = new BufferedImage(smolSize, smolSize, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = imageSmol.getGraphics();
		g.drawImage(faces.get(face), 0, 0, smolSize, smolSize, null);
		faceIcons.put(face, new ImageIcon(imageSmol, "icon for face " + face));
	}

	public static ImageIcon getFaceIcon(String name) {
		return faceIcons.get(name);
	}

	public static File getFaceFile(String face) {
		return faceFiles.get(face);
	}

	public static void sortFaces() {
		faces = faces.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		faceIcons = faceIcons.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		faceFiles = faceFiles.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

}
