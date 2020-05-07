package com.leo.ostbm;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class Resources {

	public static final String FACE_BLANK = "(none)";
	private static final String[] DUMMY_STRING_ARRAY = new String[] { };
	private static List<String> ignoredFaces;
	private static Map<String, Facepic> faces;
	private static List<BufferedImage> appIcons;
	private static Map<Icon, ImageIcon> icons;
	private static BufferedImage textboxImage;
	private static BufferedImage textboxImageOpaque;
	private static BufferedImage textboxArrow;

	public static void checkResFolder() {
		final File resFolder = new File("res");
		if (!resFolder.exists()) {
			JOptionPane.showMessageDialog(null,
					"The resources folder doesn't exist!\nPlease make sure there's a \"res\" folder next to the application that contains the faces.",
					"Resources folder doesn't exist!", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	public static void initFonts() throws FontFormatException, IOException {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("res/font/font.ttf")));
		ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("res/font/font-b.ttf")));
		ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("res/font/font-i.ttf")));
		ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("res/font/font-bi.ttf")));
	}

	public static void initAppIcons() throws IOException {
		appIcons = new LinkedList<>();
		final String[] sizes = new String[] { "16", "32", "64" };
		for (final String size : sizes)
			appIcons.add(ImageIO.read(Resources.class.getResourceAsStream("/appicon" + size + ".png")));
	}

	public static void initImages() throws IOException, URISyntaxException {
		textboxImage = ImageIO.read(new File("res/textboxImage.png"));
		textboxImageOpaque = ImageIO.read(new File("res/textboxImageOpaque.png"));
		textboxArrow = ImageIO.read(new File("res/textboxArrow.png"));
		final BufferedImage iconSheet = ImageIO.read(Resources.class.getResourceAsStream("/icons.png"));
		icons = new HashMap<>();
		int ix = 0, iy = 0;
		for (final Icon icon : Icon.values()) {
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
		ignoredFaces = new ArrayList<>();
		if (Config.getBoolean(Config.KEY_HIDE_SOLSTICE_FACES, true)) {
			boolean listLoaded = false;
			final File solsticeList = new File("res/faces/solstice_facepics.txt");
			if (solsticeList.exists()) {
				listLoaded = true;
				try (FileReader fr = new FileReader(solsticeList);
				     BufferedReader br = new BufferedReader(fr)) {
					while (br.ready())
						ignoredFaces.add(br.readLine());
				} catch (IOException e) {
					Main.LOGGER.warn("could not load solstice facepic list (exception while reading file)", e);
					ignoredFaces.clear();
					listLoaded = false;
				}
			} else
				Main.LOGGER.warn("could not load solstice facepic list (file does not exist)");
			if (!listLoaded)
				JOptionPane.showMessageDialog(null, "The Solstice facepics listing (\"" + solsticeList + "\") could not be loaded!\nSolstice facepics will not be hidden!", "Could not hide Solstice facepics!", JOptionPane.WARNING_MESSAGE);
		}
		faces = new HashMap<>();
		addFace(FACE_BLANK, null, new BufferedImage(96, 96, BufferedImage.TYPE_4BYTE_ABGR));
		final File facesFolder = new File("res/faces");
		if (!facesFolder.exists()) {
			JOptionPane.showMessageDialog(null,
					"The faces folder doesn't exist!\nPlease make sure there's a \"faces\" folder inside the resources folder (\"res\").",
					"Faces folder doesn't exist!", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		addFaces(facesFolder);
		sortFaces();
	}

	@Contract(pure = true)
	public static BufferedImage getTextboxImage() {
		return textboxImage;
	}

	@Contract(pure = true)
	public static BufferedImage getTextboxImageOpaque() {
		return textboxImageOpaque;
	}

	@Contract(pure = true)
	public static BufferedImage getTextboxArrow() {
		return textboxArrow;
	}

	@Contract(pure = true)
	public static List<BufferedImage> getAppIcons() {
		return appIcons;
	}

	public static ImageIcon getIcon(final Icon icon) {
		return icons.get(icon);
	}

	@NotNull
	public static String[] getFaces() {
		return faces.keySet().toArray(DUMMY_STRING_ARRAY);
	}

	public static Facepic getFace(final String name) {
		return faces.get(name);
	}

	@Nullable
	public static String addFace(@NotNull final File face) throws IOException {
		String faceName = face.getName();
		if (!faceName.contains(".")
				|| !faceName.substring(faceName.lastIndexOf(".") + 1).equalsIgnoreCase("png"))
			return null;
		faceName = faceName.substring(0, faceName.lastIndexOf('.'));
		if (faces.containsKey(faceName))
			throw new IOException("Duplicate facepic name: " + faceName);
		final BufferedImage image = ImageIO.read(face);
		faceName = addFace(faceName, face, image);
		return faceName;
	}

	public static String addFace(String name, final File file, @NotNull final BufferedImage face) {
		final int width = face.getWidth(), height = face.getHeight();
		if ((width != 96 || height != 96) && (width != 48 || height != 48))
			throw new IllegalArgumentException("Face dimensions must be 96 by 96 or 48 by 48!");
		final String origName = name;
		int nameCount = 1;
		while (faces.containsKey(name)) {
			nameCount++;
			name = origName + nameCount;
		}
		faces.put(name, new Facepic(name, file, face));
		return name;
	}

	public static void addFaces(@NotNull final File dir) throws IOException {
		if (dir.isFile()) {
			addFace(dir);
			return;
		}
		for (final File face : Objects.requireNonNull(dir.listFiles()))
			if (face.isDirectory())
				addFaces(face);
			else {
				if (ignoredFaces.contains(face.getName()))
					continue;
				try {
					addFace(face);
				} catch (final Exception e) {
					Main.LOGGER.error("Error while loading facepic!", e);
					JOptionPane.showMessageDialog(null,
							"Could not load facepic " + face.getName() + "!\n(at " + face.getAbsolutePath() + ")\n" + e,
							"Could not load facepic!", JOptionPane.ERROR_MESSAGE);
				}
			}
	}

	public static void addCustomFace(String name, final File file, @NotNull final BufferedImage face) throws IOException {
		File customDir = new File("res/faces/custom");
		if (!customDir.exists()) {
			if (!customDir.mkdirs())
				throw new IOException("could not create " + customDir + " directory");
		}
		File newFile = new File(customDir + "/" + file.getName());
		if (newFile.exists()) {
			JOptionPane.showMessageDialog(null, "A face with the name " + newFile.getName() + " already exists in the custom face directory!\n" +
					"Please replace it and then reload all facepics.", "Custom face already exists!", JOptionPane.WARNING_MESSAGE);
			return;
		}
		FileOutputStream fos = new FileOutputStream(newFile);
		ImageIO.write(face, "png", fos);
		fos.close();
		addFace(name, newFile, face);
	}

	public static void sortFaces() {
		faces = faces.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public enum Icon {
		NEW_PROJECT("New Project"),
		LOAD_PROJECT("Load Project"),
		SAVE_PROJECT("Save Project"),
		SAVE_PROJECT_AS("Save Project As..."),
		EXIT("Exit"),
		CHECK_FOR_UPDATES("Check for Updates"),
		ABOUT("About"),
		FACE_FOLDER("Open Facepic Folder"),
		ADD_FACE("Add Facepic"),
		ADD_TEXTBOX("Add Textbox"),
		REMOVE_TEXTBOX("Remove Textbox"),
		INSERT_TEXTBOX_BEFORE("Insert Textbox Before"),
		INSERT_TEXTBOX_AFTER("Insert Textbox After"),
		MOVE_TEXTBOX_UP("Move Textbox Up"),
		MOVE_TEXTBOX_DOWN("Move Textbox Down"),
		SETTINGS("Settings"),
		CLONE_TEXTBOX("Clone Textbox");

		String description;

		@Contract(pure = true)
		Icon(final String description) {
			this.description = description;
		}

		@Contract(pure = true)
		public String getDescription() {
			return description;
		}
	}

	public static class Facepic {
		private final String name;
		private final File file;
		private final BufferedImage image;
		private ImageIcon icon;

		public Facepic(final String name, final File file, final BufferedImage image) {
			this.name = name;
			this.file = file;
			this.image = image;
			makeIcon();
		}

		private void makeIcon() {
			final int smolSize = 48;
			final BufferedImage imageSmol = new BufferedImage(smolSize, smolSize, BufferedImage.TYPE_4BYTE_ABGR);
			final Graphics g = imageSmol.getGraphics();
			g.drawImage(image, 0, 0, smolSize, smolSize, null);
			icon = new ImageIcon(imageSmol, "small icon for face " + name);
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
	}

}
