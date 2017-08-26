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

public class Resources {

	public static final String FACE_BLANK = "(none)";

	private static Map<String, BufferedImage> faces;
	private static Map<String, ImageIcon> faceIcons;
	private static BufferedImage box;
	private static BufferedImage arrow;
	private static Font font;

	public static void initFont() throws FontFormatException, IOException {
		font = Font.createFont(Font.TRUETYPE_FONT, Resources.class.getResourceAsStream("/font.ttf"));
		font = font.deriveFont(Font.PLAIN, 20);
	}

	public static void initImages() throws IOException, URISyntaxException {
		box = ImageIO.read(Resources.class.getResourceAsStream("/box.png"));
		arrow = ImageIO.read(Resources.class.getResourceAsStream("/arrow.png"));
		faces = new HashMap<>();
		faceIcons = new HashMap<>();
		addFace(FACE_BLANK, new BufferedImage(96, 96, BufferedImage.TYPE_4BYTE_ABGR));
		File facesFolder = new File(Resources.class.getResource("/faces").toURI().getPath());
		for (File face : facesFolder.listFiles())
			addFace(face);
		faces = faces.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		faceIcons = faceIcons.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public static Font getFont() {
		return font;
	}

	public static BufferedImage getBox() {
		return box;
	}

	public static BufferedImage getArrow() {
		return arrow;
	}

	public static String[] getFaces() {
		return faces.keySet().toArray(new String[] {});
	}

	public static BufferedImage getFace(String name) {
		return faces.get(name);
	}

	public static void addFace(File face) throws IOException {
		String faceName = face.getName();
		faceName = faceName.substring(0, faceName.lastIndexOf('.'));
		BufferedImage image = ImageIO.read(face);
		addFace(faceName, image);
	}

	public static void addFace(String name, BufferedImage face) {
		if (face.getWidth() != 96 || face.getHeight() != 96)
			throw new IllegalArgumentException("face must be 96 in width and 96 in height!");
		faces.put(name, face);
		addFaceIcon(name);
	}

	private static void addFaceIcon(String face) {
		if (!faces.containsKey(face))
			return;
		final int smolSize = 48;
		BufferedImage imageSmol = new BufferedImage(smolSize, smolSize, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics g = imageSmol.getGraphics();
		g.drawImage(faces.get(face), 0, 0, smolSize, smolSize, null);
		faceIcons.put(face, new ImageIcon(imageSmol));
	}

	public static ImageIcon getFaceIcon(String name) {
		return faceIcons.get(name);
	}

}
