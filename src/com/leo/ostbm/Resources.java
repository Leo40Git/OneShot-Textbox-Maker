package com.leo.ostbm;

import java.awt.Font;
import java.awt.FontFormatException;
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

	private static Map<String, BufferedImage> faces;
	private static Map<String, ImageIcon> faceIcons;
	private static BufferedImage box;
	private static Font font;

	public static void initFont() throws FontFormatException, IOException {
		font = Font.createFont(Font.TRUETYPE_FONT, Resources.class.getResourceAsStream("/font.ttf"));
		font = font.deriveFont(Font.PLAIN, 20);
	}

	public static void initImages() throws IOException, URISyntaxException {
		box = ImageIO.read(Resources.class.getResourceAsStream("/box.png"));
		faces = new HashMap<>();
		File facesFolder = new File(Resources.class.getResource("/faces").toURI().getPath());
		for (File face : facesFolder.listFiles()) {
			String faceName = face.getName();
			faceName = faceName.substring(0, faceName.lastIndexOf('.'));
			faces.put(faceName, ImageIO.read(face));
		}
		faces = faces.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		faceIcons = new HashMap<>();
		for (Map.Entry<String, BufferedImage> entry : faces.entrySet()) {
			faceIcons.put(entry.getKey(), new ImageIcon(entry.getValue()));
		}
	}

	public static Font getFont() {
		return font;
	}

	public static BufferedImage getBox() {
		return box;
	}

	public static String[] getFaces() {
		return faces.keySet().toArray(new String[] {});
	}

	public static BufferedImage getFace(String name) {
		return faces.get(name);
	}

	public static ImageIcon getFaceIcon(String name) {
		return faceIcons.get(name);
	}

}
